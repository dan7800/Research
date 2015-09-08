/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class CashAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_cash_account";
	protected final String strObjectName = "CashAccountBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ACC_NUMBER = "acc_number";
	public static final String ACC_TYPE = "acc_type"; // / filled with GL Code
	public static final String CURRENCY = "currency";
	public static final String LEVEL_LOW = "level_low";
	public static final String LEVEL_HIGH = "level_high";
	public static final String FACILITY_AMOUNT = "facility_amount";
	public static final String OVERDRAFT_LIMIT = "overdraft_limit";
	public static final String SIGNATORY1 = "signatory1";
	public static final String SIGNATORY2 = "signatory2";
	public static final String SIGNATORY3 = "signatory3";
	public static final String SIGNATORY4 = "signatory4";
	public static final String SIGNATURE = "signature";
	public static final String PC_CENTER = "pc_center";
	public static final String ADD1 = "add1";
	public static final String ADD2 = "add2";
	public static final String ADD3 = "add3";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String PHONE = "phone";
	public static final String CONTACT_PERSON = "contact_person";
	public static final String FAX = "fax";
	public static final String USERID_CREATE = "userid_create";
	public static final String USERID_UPDATE = "userid_update";
	public static final String CREATE_TIME = "createtime";
	public static final String LAST_UPDATE = "lastupdate";
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String code;
	private String name;
	private String description;
	private String accountNumber;
	private String accountType;
	private String currency;
	private BigDecimal levelLow;
	private BigDecimal levelHigh;
	private BigDecimal facilityAmount;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
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

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public String getCode()
	{
		return this.code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public String getAccountType()
	{
		return this.accountType;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public BigDecimal getLevelLow()
	{
		return this.levelLow;
	}

	public void setLevelLow(BigDecimal levelLow)
	{
		this.levelLow = levelLow;
	}

	public BigDecimal getLevelHigh()
	{
		return this.levelHigh;
	}

	public void setLevelHigh(BigDecimal levelHigh)
	{
		this.levelHigh = levelHigh;
	}

	public BigDecimal getFacilityAmount()
	{
		return this.facilityAmount;
	}

	public void setFacilityAmount(BigDecimal facilityAmount)
	{
		this.facilityAmount = facilityAmount;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public void setOverdraftLimit(BigDecimal overdraftLimit)
	{
		this.overdraftLimit = overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public void setSignature(byte[] signature)
	{
		this.signature = signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
	}

	public void setPCCenter(Integer pcCenter)
	{
		this.pcCenter = pcCenter;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public String getState()
	{
		return this.state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public void setContactPerson(String contactPerson)
	{
		this.contactPerson = contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public void setFax(String fax)
	{
		this.fax = fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public void setUserIdCreate(Integer userIdCreate)
	{
		this.userIdCreate = userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public CashAccountObject getValueObject()
	{
		CashAccountObject bankAccObj = new CashAccountObject();
		bankAccObj.pkId = this.pkId;
		bankAccObj.code = this.code;
		bankAccObj.name = this.name;
		bankAccObj.description = this.description;
		bankAccObj.accountNumber = this.accountNumber;
		bankAccObj.accountType = this.accountType;
		bankAccObj.currency = this.currency;
		bankAccObj.levelLow = this.levelLow;
		bankAccObj.levelHigh = this.levelHigh;
		bankAccObj.facilityAmount = this.facilityAmount;
		bankAccObj.overdraftLimit = this.overdraftLimit;
		bankAccObj.signatory1 = this.signatory1;
		bankAccObj.signatory2 = this.signatory2;
		bankAccObj.signatory3 = this.signatory3;
		bankAccObj.signatory4 = this.signatory4;
		bankAccObj.signature = this.signature;
		bankAccObj.pcCenter = this.pcCenter;
		bankAccObj.add1 = this.add1;
		bankAccObj.add2 = this.add2;
		bankAccObj.add3 = this.add3;
		bankAccObj.state = this.state;
		bankAccObj.country = this.country;
		bankAccObj.phone = this.phone;
		bankAccObj.contactPerson = this.contactPerson;
		bankAccObj.fax = this.fax;
		bankAccObj.userIdCreate = this.userIdCreate;
		bankAccObj.userIdUpdate = this.userIdUpdate;
		bankAccObj.createTime = this.createTime;
		bankAccObj.lastUpdate = this.lastUpdate;
		bankAccObj.status = this.status;
		return bankAccObj;
	}

	public void setValueObject(CashAccountObject cao) throws Exception
	{
		if (cao == null)
		{
			throw new Exception("Object undefined");
		}
		this.code = cao.code;
		this.name = cao.name;
		this.description = cao.description;
		this.accountNumber = cao.accountNumber;
		this.accountType = cao.accountType;
		this.currency = cao.currency;
		this.levelLow = cao.levelLow;
		this.levelHigh = cao.levelHigh;
		this.facilityAmount = cao.facilityAmount;
		this.overdraftLimit = cao.overdraftLimit;
		this.signatory1 = cao.signatory1;
		this.signatory2 = cao.signatory2;
		this.signatory3 = cao.signatory3;
		this.signatory4 = cao.signatory4;
		this.signature = cao.signature;
		this.pcCenter = cao.pcCenter;
		this.add1 = cao.add1;
		this.add2 = cao.add2;
		this.add3 = cao.add3;
		this.state = cao.state;
		this.country = cao.country;
		this.phone = cao.phone;
		this.contactPerson = cao.contactPerson;
		this.fax = cao.fax;
		this.userIdCreate = cao.userIdCreate;
		this.userIdUpdate = cao.userIdUpdate;
		this.createTime = cao.createTime;
		this.lastUpdate = cao.lastUpdate;
		this.status = cao.status;
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

	public Integer ejbCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
				facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature, pcCenter,
				add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate, createTime,
				lastUpdate, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.code = code;
			this.name = name;
			this.description = description;
			this.accountNumber = accountNumber;
			this.accountType = accountType;
			this.currency = currency;
			this.levelLow = levelLow;
			this.levelHigh = levelHigh;
			this.facilityAmount = facilityAmount;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
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

	public void ejbPostCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
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

	private Integer insertNewRow(String code, String name, String description, String accountNumber,
			String accountType, String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", "
					+ LEVEL_HIGH + ", " + FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", "
					+ SIGNATORY2 + ", " + SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", "
					+ ADD1 + ", " + ADD2 + ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", "
					+ CONTACT_PERSON + ", " + FAX + ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME
					+ ", " + LAST_UPDATE + ", " + STATUS + " ) " + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, accountNumber);
			ps.setString(6, accountType);
			ps.setString(7, currency);
			ps.setBigDecimal(8, levelLow);
			ps.setBigDecimal(9, levelHigh);
			ps.setBigDecimal(10, facilityAmount);
			ps.setBigDecimal(11, overdraftLimit);
			ps.setString(12, signatory1);
			ps.setString(13, signatory2);
			ps.setString(14, signatory3);
			ps.setString(15, signatory4);
			ps.setBytes(16, signature);
			ps.setInt(17, pcCenter.intValue());
			ps.setString(18, add1);
			ps.setString(19, add2);
			ps.setString(20, add3);
			ps.setString(21, state);
			ps.setString(22, country);
			ps.setString(23, phone);
			ps.setString(24, contactPerson);
			ps.setString(25, fax);
			ps.setInt(26, userIdCreate.intValue());
			ps.setInt(27, userIdUpdate.intValue());
			ps.setTimestamp(28, createTime);
			ps.setTimestamp(29, lastUpdate);
			ps.setString(30, status);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws SQLException
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?  AND " + STATUS + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement += " AND " + fieldName2 + " = ? ";
			}
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, STATUS_ACTIVE);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(3, value2);
			}
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				CashAccountObject caObj = new CashAccountObject();
				caObj.pkId = new Integer(rs.getInt(PKID));
				caObj.code = rs.getString(CODE);
				caObj.name = rs.getString(NAME);
				caObj.description = rs.getString(DESCRIPTION);
				caObj.accountNumber = rs.getString(ACC_NUMBER);
				caObj.accountType = rs.getString(ACC_TYPE);
				caObj.currency = rs.getString(CURRENCY);
				caObj.levelLow = rs.getBigDecimal(LEVEL_LOW);
				caObj.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				caObj.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				caObj.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				caObj.signatory1 = rs.getString(SIGNATORY1);
				caObj.signatory2 = rs.getString(SIGNATORY2);
				caObj.signatory3 = rs.getString(SIGNATORY3);
				caObj.signatory4 = rs.getString(SIGNATORY4);
				caObj.signature = rs.getBytes(SIGNATURE);
				caObj.pcCenter = new Integer(rs.getString(PC_CENTER));
				caObj.add1 = rs.getString(ADD1);
				caObj.add2 = rs.getString(ADD2);
				caObj.add3 = rs.getString(ADD3);
				caObj.state = rs.getString(STATE);
				caObj.country = rs.getString(COUNTRY);
				caObj.phone = rs.getString(PHONE);
				caObj.contactPerson = rs.getString(CONTACT_PERSON);
				caObj.fax = rs.getString(FAX);
				caObj.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				caObj.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				caObj.createTime = rs.getTimestamp(CREATE_TIME);
				caObj.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				caObj.status = rs.getString(STATUS);
				vecValObj.add(caObj);
			} // / end while
		}// end try
		catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	} // end of selectValueObjectsGiven

	// ////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE pkid =?";
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt(PKID));
				this.code = rs.getString(CODE);
				this.name = rs.getString(NAME);
				this.description = rs.getString(DESCRIPTION);
				this.accountNumber = rs.getString(ACC_NUMBER);
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.levelLow = rs.getBigDecimal(LEVEL_LOW);
				this.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				this.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				this.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				this.signatory1 = rs.getString(SIGNATORY1);
				this.signatory2 = rs.getString(SIGNATORY2);
				this.signatory3 = rs.getString(SIGNATORY3);
				this.signatory4 = rs.getString(SIGNATORY4);
				this.signature = rs.getBytes(SIGNATURE);
				this.pcCenter = new Integer(rs.getString(PC_CENTER));
				this.add1 = rs.getString(ADD1);
				this.add2 = rs.getString(ADD2);
				this.add3 = rs.getString(ADD3);
				this.state = rs.getString(STATE);
				this.country = rs.getString(COUNTRY);
				this.phone = rs.getString(PHONE);
				this.contactPerson = rs.getString(CONTACT_PERSON);
				this.fax = rs.getString(FAX);
				this.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				this.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				this.createTime = rs.getTimestamp(CREATE_TIME);
				this.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				this.status = rs.getString(STATUS);
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET " + CODE + " = ?, " + NAME + " = ?, " + DESCRIPTION
					+ " = ?, " + ACC_NUMBER + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY + " = ?, " + LEVEL_LOW
					+ " = ?, " + LEVEL_HIGH + " = ?, " + FACILITY_AMOUNT + " = ?, " + OVERDRAFT_LIMIT + " = ?, "
					+ SIGNATORY1 + " = ?, " + SIGNATORY2 + " = ?, " + SIGNATORY3 + " = ?, " + SIGNATORY4 + " = ?, "
					+ SIGNATURE + " = ?, " + PC_CENTER + " = ?, " + ADD1 + " = ?, " + ADD2 + " = ?, " + ADD3 + " = ?, "
					+ STATE + " = ?, " + COUNTRY + " = ?, " + PHONE + " = ?, " + CONTACT_PERSON + " = ?, " + FAX
					+ " = ?, " + USERID_CREATE + " = ?, " + USERID_UPDATE + " = ?, " + CREATE_TIME + " = ?, "
					+ LAST_UPDATE + " = ?, " + STATUS + " = ? WHERE " + PKID + " = ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.code);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.accountNumber);
			ps.setString(5, this.accountType);
			ps.setString(6, this.currency);
			ps.setBigDecimal(7, this.levelLow);
			ps.setBigDecimal(8, this.levelHigh);
			ps.setBigDecimal(9, this.facilityAmount);
			ps.setBigDecimal(10, this.overdraftLimit);
			ps.setString(11, this.signatory1);
			ps.setString(12, this.signatory2);
			ps.setString(13, this.signatory3);
			ps.setString(14, this.signatory4);
			ps.setBytes(15, this.signature);
			ps.setInt(16, this.pcCenter.intValue());
			ps.setString(17, this.add1);
			ps.setString(18, this.add2);
			ps.setString(19, this.add3);
			ps.setString(20, this.state);
			ps.setString(21, this.country);
			ps.setString(22, this.phone);
			ps.setString(23, this.contactPerson);
			ps.setString(24, this.fax);
			ps.setInt(25, this.userIdCreate.intValue());
			ps.setInt(26, this.userIdUpdate.intValue());
			ps.setTimestamp(27, this.createTime);
			ps.setTimestamp(28, this.lastUpdate);
			ps.setString(29, this.status);
			ps.setInt(30, this.pkId.intValue());
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
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME;
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
		ArrayList objectSet = new ArrayList();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return objectSet;
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class CashAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_cash_account";
	protected final String strObjectName = "CashAccountBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ACC_NUMBER = "acc_number";
	public static final String ACC_TYPE = "acc_type"; // / filled with GL Code
	public static final String CURRENCY = "currency";
	public static final String LEVEL_LOW = "level_low";
	public static final String LEVEL_HIGH = "level_high";
	public static final String FACILITY_AMOUNT = "facility_amount";
	public static final String OVERDRAFT_LIMIT = "overdraft_limit";
	public static final String SIGNATORY1 = "signatory1";
	public static final String SIGNATORY2 = "signatory2";
	public static final String SIGNATORY3 = "signatory3";
	public static final String SIGNATORY4 = "signatory4";
	public static final String SIGNATURE = "signature";
	public static final String PC_CENTER = "pc_center";
	public static final String ADD1 = "add1";
	public static final String ADD2 = "add2";
	public static final String ADD3 = "add3";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String PHONE = "phone";
	public static final String CONTACT_PERSON = "contact_person";
	public static final String FAX = "fax";
	public static final String USERID_CREATE = "userid_create";
	public static final String USERID_UPDATE = "userid_update";
	public static final String CREATE_TIME = "createtime";
	public static final String LAST_UPDATE = "lastupdate";
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String code;
	private String name;
	private String description;
	private String accountNumber;
	private String accountType;
	private String currency;
	private BigDecimal levelLow;
	private BigDecimal levelHigh;
	private BigDecimal facilityAmount;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
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

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public String getCode()
	{
		return this.code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public String getAccountType()
	{
		return this.accountType;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public BigDecimal getLevelLow()
	{
		return this.levelLow;
	}

	public void setLevelLow(BigDecimal levelLow)
	{
		this.levelLow = levelLow;
	}

	public BigDecimal getLevelHigh()
	{
		return this.levelHigh;
	}

	public void setLevelHigh(BigDecimal levelHigh)
	{
		this.levelHigh = levelHigh;
	}

	public BigDecimal getFacilityAmount()
	{
		return this.facilityAmount;
	}

	public void setFacilityAmount(BigDecimal facilityAmount)
	{
		this.facilityAmount = facilityAmount;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public void setOverdraftLimit(BigDecimal overdraftLimit)
	{
		this.overdraftLimit = overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public void setSignature(byte[] signature)
	{
		this.signature = signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
	}

	public void setPCCenter(Integer pcCenter)
	{
		this.pcCenter = pcCenter;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public String getState()
	{
		return this.state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public void setContactPerson(String contactPerson)
	{
		this.contactPerson = contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public void setFax(String fax)
	{
		this.fax = fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public void setUserIdCreate(Integer userIdCreate)
	{
		this.userIdCreate = userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public CashAccountObject getValueObject()
	{
		CashAccountObject bankAccObj = new CashAccountObject();
		bankAccObj.pkId = this.pkId;
		bankAccObj.code = this.code;
		bankAccObj.name = this.name;
		bankAccObj.description = this.description;
		bankAccObj.accountNumber = this.accountNumber;
		bankAccObj.accountType = this.accountType;
		bankAccObj.currency = this.currency;
		bankAccObj.levelLow = this.levelLow;
		bankAccObj.levelHigh = this.levelHigh;
		bankAccObj.facilityAmount = this.facilityAmount;
		bankAccObj.overdraftLimit = this.overdraftLimit;
		bankAccObj.signatory1 = this.signatory1;
		bankAccObj.signatory2 = this.signatory2;
		bankAccObj.signatory3 = this.signatory3;
		bankAccObj.signatory4 = this.signatory4;
		bankAccObj.signature = this.signature;
		bankAccObj.pcCenter = this.pcCenter;
		bankAccObj.add1 = this.add1;
		bankAccObj.add2 = this.add2;
		bankAccObj.add3 = this.add3;
		bankAccObj.state = this.state;
		bankAccObj.country = this.country;
		bankAccObj.phone = this.phone;
		bankAccObj.contactPerson = this.contactPerson;
		bankAccObj.fax = this.fax;
		bankAccObj.userIdCreate = this.userIdCreate;
		bankAccObj.userIdUpdate = this.userIdUpdate;
		bankAccObj.createTime = this.createTime;
		bankAccObj.lastUpdate = this.lastUpdate;
		bankAccObj.status = this.status;
		return bankAccObj;
	}

	public void setValueObject(CashAccountObject cao) throws Exception
	{
		if (cao == null)
		{
			throw new Exception("Object undefined");
		}
		this.code = cao.code;
		this.name = cao.name;
		this.description = cao.description;
		this.accountNumber = cao.accountNumber;
		this.accountType = cao.accountType;
		this.currency = cao.currency;
		this.levelLow = cao.levelLow;
		this.levelHigh = cao.levelHigh;
		this.facilityAmount = cao.facilityAmount;
		this.overdraftLimit = cao.overdraftLimit;
		this.signatory1 = cao.signatory1;
		this.signatory2 = cao.signatory2;
		this.signatory3 = cao.signatory3;
		this.signatory4 = cao.signatory4;
		this.signature = cao.signature;
		this.pcCenter = cao.pcCenter;
		this.add1 = cao.add1;
		this.add2 = cao.add2;
		this.add3 = cao.add3;
		this.state = cao.state;
		this.country = cao.country;
		this.phone = cao.phone;
		this.contactPerson = cao.contactPerson;
		this.fax = cao.fax;
		this.userIdCreate = cao.userIdCreate;
		this.userIdUpdate = cao.userIdUpdate;
		this.createTime = cao.createTime;
		this.lastUpdate = cao.lastUpdate;
		this.status = cao.status;
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

	public Integer ejbCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
				facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature, pcCenter,
				add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate, createTime,
				lastUpdate, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.code = code;
			this.name = name;
			this.description = description;
			this.accountNumber = accountNumber;
			this.accountType = accountType;
			this.currency = currency;
			this.levelLow = levelLow;
			this.levelHigh = levelHigh;
			this.facilityAmount = facilityAmount;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
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

	public void ejbPostCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
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

	private Integer insertNewRow(String code, String name, String description, String accountNumber,
			String accountType, String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", "
					+ LEVEL_HIGH + ", " + FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", "
					+ SIGNATORY2 + ", " + SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", "
					+ ADD1 + ", " + ADD2 + ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", "
					+ CONTACT_PERSON + ", " + FAX + ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME
					+ ", " + LAST_UPDATE + ", " + STATUS + " ) " + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, accountNumber);
			ps.setString(6, accountType);
			ps.setString(7, currency);
			ps.setBigDecimal(8, levelLow);
			ps.setBigDecimal(9, levelHigh);
			ps.setBigDecimal(10, facilityAmount);
			ps.setBigDecimal(11, overdraftLimit);
			ps.setString(12, signatory1);
			ps.setString(13, signatory2);
			ps.setString(14, signatory3);
			ps.setString(15, signatory4);
			ps.setBytes(16, signature);
			ps.setInt(17, pcCenter.intValue());
			ps.setString(18, add1);
			ps.setString(19, add2);
			ps.setString(20, add3);
			ps.setString(21, state);
			ps.setString(22, country);
			ps.setString(23, phone);
			ps.setString(24, contactPerson);
			ps.setString(25, fax);
			ps.setInt(26, userIdCreate.intValue());
			ps.setInt(27, userIdUpdate.intValue());
			ps.setTimestamp(28, createTime);
			ps.setTimestamp(29, lastUpdate);
			ps.setString(30, status);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws SQLException
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?  AND " + STATUS + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement += " AND " + fieldName2 + " = ? ";
			}
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, STATUS_ACTIVE);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(3, value2);
			}
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				CashAccountObject caObj = new CashAccountObject();
				caObj.pkId = new Integer(rs.getInt(PKID));
				caObj.code = rs.getString(CODE);
				caObj.name = rs.getString(NAME);
				caObj.description = rs.getString(DESCRIPTION);
				caObj.accountNumber = rs.getString(ACC_NUMBER);
				caObj.accountType = rs.getString(ACC_TYPE);
				caObj.currency = rs.getString(CURRENCY);
				caObj.levelLow = rs.getBigDecimal(LEVEL_LOW);
				caObj.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				caObj.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				caObj.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				caObj.signatory1 = rs.getString(SIGNATORY1);
				caObj.signatory2 = rs.getString(SIGNATORY2);
				caObj.signatory3 = rs.getString(SIGNATORY3);
				caObj.signatory4 = rs.getString(SIGNATORY4);
				caObj.signature = rs.getBytes(SIGNATURE);
				caObj.pcCenter = new Integer(rs.getString(PC_CENTER));
				caObj.add1 = rs.getString(ADD1);
				caObj.add2 = rs.getString(ADD2);
				caObj.add3 = rs.getString(ADD3);
				caObj.state = rs.getString(STATE);
				caObj.country = rs.getString(COUNTRY);
				caObj.phone = rs.getString(PHONE);
				caObj.contactPerson = rs.getString(CONTACT_PERSON);
				caObj.fax = rs.getString(FAX);
				caObj.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				caObj.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				caObj.createTime = rs.getTimestamp(CREATE_TIME);
				caObj.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				caObj.status = rs.getString(STATUS);
				vecValObj.add(caObj);
			} // / end while
		}// end try
		catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	} // end of selectValueObjectsGiven

	// ////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE pkid =?";
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt(PKID));
				this.code = rs.getString(CODE);
				this.name = rs.getString(NAME);
				this.description = rs.getString(DESCRIPTION);
				this.accountNumber = rs.getString(ACC_NUMBER);
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.levelLow = rs.getBigDecimal(LEVEL_LOW);
				this.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				this.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				this.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				this.signatory1 = rs.getString(SIGNATORY1);
				this.signatory2 = rs.getString(SIGNATORY2);
				this.signatory3 = rs.getString(SIGNATORY3);
				this.signatory4 = rs.getString(SIGNATORY4);
				this.signature = rs.getBytes(SIGNATURE);
				this.pcCenter = new Integer(rs.getString(PC_CENTER));
				this.add1 = rs.getString(ADD1);
				this.add2 = rs.getString(ADD2);
				this.add3 = rs.getString(ADD3);
				this.state = rs.getString(STATE);
				this.country = rs.getString(COUNTRY);
				this.phone = rs.getString(PHONE);
				this.contactPerson = rs.getString(CONTACT_PERSON);
				this.fax = rs.getString(FAX);
				this.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				this.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				this.createTime = rs.getTimestamp(CREATE_TIME);
				this.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				this.status = rs.getString(STATUS);
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET " + CODE + " = ?, " + NAME + " = ?, " + DESCRIPTION
					+ " = ?, " + ACC_NUMBER + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY + " = ?, " + LEVEL_LOW
					+ " = ?, " + LEVEL_HIGH + " = ?, " + FACILITY_AMOUNT + " = ?, " + OVERDRAFT_LIMIT + " = ?, "
					+ SIGNATORY1 + " = ?, " + SIGNATORY2 + " = ?, " + SIGNATORY3 + " = ?, " + SIGNATORY4 + " = ?, "
					+ SIGNATURE + " = ?, " + PC_CENTER + " = ?, " + ADD1 + " = ?, " + ADD2 + " = ?, " + ADD3 + " = ?, "
					+ STATE + " = ?, " + COUNTRY + " = ?, " + PHONE + " = ?, " + CONTACT_PERSON + " = ?, " + FAX
					+ " = ?, " + USERID_CREATE + " = ?, " + USERID_UPDATE + " = ?, " + CREATE_TIME + " = ?, "
					+ LAST_UPDATE + " = ?, " + STATUS + " = ? WHERE " + PKID + " = ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.code);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.accountNumber);
			ps.setString(5, this.accountType);
			ps.setString(6, this.currency);
			ps.setBigDecimal(7, this.levelLow);
			ps.setBigDecimal(8, this.levelHigh);
			ps.setBigDecimal(9, this.facilityAmount);
			ps.setBigDecimal(10, this.overdraftLimit);
			ps.setString(11, this.signatory1);
			ps.setString(12, this.signatory2);
			ps.setString(13, this.signatory3);
			ps.setString(14, this.signatory4);
			ps.setBytes(15, this.signature);
			ps.setInt(16, this.pcCenter.intValue());
			ps.setString(17, this.add1);
			ps.setString(18, this.add2);
			ps.setString(19, this.add3);
			ps.setString(20, this.state);
			ps.setString(21, this.country);
			ps.setString(22, this.phone);
			ps.setString(23, this.contactPerson);
			ps.setString(24, this.fax);
			ps.setInt(25, this.userIdCreate.intValue());
			ps.setInt(26, this.userIdUpdate.intValue());
			ps.setTimestamp(27, this.createTime);
			ps.setTimestamp(28, this.lastUpdate);
			ps.setString(29, this.status);
			ps.setInt(30, this.pkId.intValue());
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
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME;
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class CashAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_cash_account";
	protected final String strObjectName = "CashAccountBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ACC_NUMBER = "acc_number";
	public static final String ACC_TYPE = "acc_type"; // / filled with GL Code
	public static final String CURRENCY = "currency";
	public static final String LEVEL_LOW = "level_low";
	public static final String LEVEL_HIGH = "level_high";
	public static final String FACILITY_AMOUNT = "facility_amount";
	public static final String OVERDRAFT_LIMIT = "overdraft_limit";
	public static final String SIGNATORY1 = "signatory1";
	public static final String SIGNATORY2 = "signatory2";
	public static final String SIGNATORY3 = "signatory3";
	public static final String SIGNATORY4 = "signatory4";
	public static final String SIGNATURE = "signature";
	public static final String PC_CENTER = "pc_center";
	public static final String ADD1 = "add1";
	public static final String ADD2 = "add2";
	public static final String ADD3 = "add3";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String PHONE = "phone";
	public static final String CONTACT_PERSON = "contact_person";
	public static final String FAX = "fax";
	public static final String USERID_CREATE = "userid_create";
	public static final String USERID_UPDATE = "userid_update";
	public static final String CREATE_TIME = "createtime";
	public static final String LAST_UPDATE = "lastupdate";
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String code;
	private String name;
	private String description;
	private String accountNumber;
	private String accountType;
	private String currency;
	private BigDecimal levelLow;
	private BigDecimal levelHigh;
	private BigDecimal facilityAmount;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
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

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public String getCode()
	{
		return this.code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public String getAccountType()
	{
		return this.accountType;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public BigDecimal getLevelLow()
	{
		return this.levelLow;
	}

	public void setLevelLow(BigDecimal levelLow)
	{
		this.levelLow = levelLow;
	}

	public BigDecimal getLevelHigh()
	{
		return this.levelHigh;
	}

	public void setLevelHigh(BigDecimal levelHigh)
	{
		this.levelHigh = levelHigh;
	}

	public BigDecimal getFacilityAmount()
	{
		return this.facilityAmount;
	}

	public void setFacilityAmount(BigDecimal facilityAmount)
	{
		this.facilityAmount = facilityAmount;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public void setOverdraftLimit(BigDecimal overdraftLimit)
	{
		this.overdraftLimit = overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public void setSignature(byte[] signature)
	{
		this.signature = signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
	}

	public void setPCCenter(Integer pcCenter)
	{
		this.pcCenter = pcCenter;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public String getState()
	{
		return this.state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public void setContactPerson(String contactPerson)
	{
		this.contactPerson = contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public void setFax(String fax)
	{
		this.fax = fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public void setUserIdCreate(Integer userIdCreate)
	{
		this.userIdCreate = userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public CashAccountObject getValueObject()
	{
		CashAccountObject bankAccObj = new CashAccountObject();
		bankAccObj.pkId = this.pkId;
		bankAccObj.code = this.code;
		bankAccObj.name = this.name;
		bankAccObj.description = this.description;
		bankAccObj.accountNumber = this.accountNumber;
		bankAccObj.accountType = this.accountType;
		bankAccObj.currency = this.currency;
		bankAccObj.levelLow = this.levelLow;
		bankAccObj.levelHigh = this.levelHigh;
		bankAccObj.facilityAmount = this.facilityAmount;
		bankAccObj.overdraftLimit = this.overdraftLimit;
		bankAccObj.signatory1 = this.signatory1;
		bankAccObj.signatory2 = this.signatory2;
		bankAccObj.signatory3 = this.signatory3;
		bankAccObj.signatory4 = this.signatory4;
		bankAccObj.signature = this.signature;
		bankAccObj.pcCenter = this.pcCenter;
		bankAccObj.add1 = this.add1;
		bankAccObj.add2 = this.add2;
		bankAccObj.add3 = this.add3;
		bankAccObj.state = this.state;
		bankAccObj.country = this.country;
		bankAccObj.phone = this.phone;
		bankAccObj.contactPerson = this.contactPerson;
		bankAccObj.fax = this.fax;
		bankAccObj.userIdCreate = this.userIdCreate;
		bankAccObj.userIdUpdate = this.userIdUpdate;
		bankAccObj.createTime = this.createTime;
		bankAccObj.lastUpdate = this.lastUpdate;
		bankAccObj.status = this.status;
		return bankAccObj;
	}

	public void setValueObject(CashAccountObject cao) throws Exception
	{
		if (cao == null)
		{
			throw new Exception("Object undefined");
		}
		this.code = cao.code;
		this.name = cao.name;
		this.description = cao.description;
		this.accountNumber = cao.accountNumber;
		this.accountType = cao.accountType;
		this.currency = cao.currency;
		this.levelLow = cao.levelLow;
		this.levelHigh = cao.levelHigh;
		this.facilityAmount = cao.facilityAmount;
		this.overdraftLimit = cao.overdraftLimit;
		this.signatory1 = cao.signatory1;
		this.signatory2 = cao.signatory2;
		this.signatory3 = cao.signatory3;
		this.signatory4 = cao.signatory4;
		this.signature = cao.signature;
		this.pcCenter = cao.pcCenter;
		this.add1 = cao.add1;
		this.add2 = cao.add2;
		this.add3 = cao.add3;
		this.state = cao.state;
		this.country = cao.country;
		this.phone = cao.phone;
		this.contactPerson = cao.contactPerson;
		this.fax = cao.fax;
		this.userIdCreate = cao.userIdCreate;
		this.userIdUpdate = cao.userIdUpdate;
		this.createTime = cao.createTime;
		this.lastUpdate = cao.lastUpdate;
		this.status = cao.status;
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

	public Integer ejbCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
				facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature, pcCenter,
				add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate, createTime,
				lastUpdate, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.code = code;
			this.name = name;
			this.description = description;
			this.accountNumber = accountNumber;
			this.accountType = accountType;
			this.currency = currency;
			this.levelLow = levelLow;
			this.levelHigh = levelHigh;
			this.facilityAmount = facilityAmount;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
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

	public void ejbPostCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
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

	private Integer insertNewRow(String code, String name, String description, String accountNumber,
			String accountType, String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", "
					+ LEVEL_HIGH + ", " + FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", "
					+ SIGNATORY2 + ", " + SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", "
					+ ADD1 + ", " + ADD2 + ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", "
					+ CONTACT_PERSON + ", " + FAX + ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME
					+ ", " + LAST_UPDATE + ", " + STATUS + " ) " + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, accountNumber);
			ps.setString(6, accountType);
			ps.setString(7, currency);
			ps.setBigDecimal(8, levelLow);
			ps.setBigDecimal(9, levelHigh);
			ps.setBigDecimal(10, facilityAmount);
			ps.setBigDecimal(11, overdraftLimit);
			ps.setString(12, signatory1);
			ps.setString(13, signatory2);
			ps.setString(14, signatory3);
			ps.setString(15, signatory4);
			ps.setBytes(16, signature);
			ps.setInt(17, pcCenter.intValue());
			ps.setString(18, add1);
			ps.setString(19, add2);
			ps.setString(20, add3);
			ps.setString(21, state);
			ps.setString(22, country);
			ps.setString(23, phone);
			ps.setString(24, contactPerson);
			ps.setString(25, fax);
			ps.setInt(26, userIdCreate.intValue());
			ps.setInt(27, userIdUpdate.intValue());
			ps.setTimestamp(28, createTime);
			ps.setTimestamp(29, lastUpdate);
			ps.setString(30, status);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws SQLException
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?  AND " + STATUS + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement += " AND " + fieldName2 + " = ? ";
			}
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, STATUS_ACTIVE);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(3, value2);
			}
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				CashAccountObject caObj = new CashAccountObject();
				caObj.pkId = new Integer(rs.getInt(PKID));
				caObj.code = rs.getString(CODE);
				caObj.name = rs.getString(NAME);
				caObj.description = rs.getString(DESCRIPTION);
				caObj.accountNumber = rs.getString(ACC_NUMBER);
				caObj.accountType = rs.getString(ACC_TYPE);
				caObj.currency = rs.getString(CURRENCY);
				caObj.levelLow = rs.getBigDecimal(LEVEL_LOW);
				caObj.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				caObj.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				caObj.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				caObj.signatory1 = rs.getString(SIGNATORY1);
				caObj.signatory2 = rs.getString(SIGNATORY2);
				caObj.signatory3 = rs.getString(SIGNATORY3);
				caObj.signatory4 = rs.getString(SIGNATORY4);
				caObj.signature = rs.getBytes(SIGNATURE);
				caObj.pcCenter = new Integer(rs.getString(PC_CENTER));
				caObj.add1 = rs.getString(ADD1);
				caObj.add2 = rs.getString(ADD2);
				caObj.add3 = rs.getString(ADD3);
				caObj.state = rs.getString(STATE);
				caObj.country = rs.getString(COUNTRY);
				caObj.phone = rs.getString(PHONE);
				caObj.contactPerson = rs.getString(CONTACT_PERSON);
				caObj.fax = rs.getString(FAX);
				caObj.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				caObj.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				caObj.createTime = rs.getTimestamp(CREATE_TIME);
				caObj.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				caObj.status = rs.getString(STATUS);
				vecValObj.add(caObj);
			} // / end while
		}// end try
		catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	} // end of selectValueObjectsGiven

	// ////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE pkid =?";
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt(PKID));
				this.code = rs.getString(CODE);
				this.name = rs.getString(NAME);
				this.description = rs.getString(DESCRIPTION);
				this.accountNumber = rs.getString(ACC_NUMBER);
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.levelLow = rs.getBigDecimal(LEVEL_LOW);
				this.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				this.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				this.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				this.signatory1 = rs.getString(SIGNATORY1);
				this.signatory2 = rs.getString(SIGNATORY2);
				this.signatory3 = rs.getString(SIGNATORY3);
				this.signatory4 = rs.getString(SIGNATORY4);
				this.signature = rs.getBytes(SIGNATURE);
				this.pcCenter = new Integer(rs.getString(PC_CENTER));
				this.add1 = rs.getString(ADD1);
				this.add2 = rs.getString(ADD2);
				this.add3 = rs.getString(ADD3);
				this.state = rs.getString(STATE);
				this.country = rs.getString(COUNTRY);
				this.phone = rs.getString(PHONE);
				this.contactPerson = rs.getString(CONTACT_PERSON);
				this.fax = rs.getString(FAX);
				this.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				this.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				this.createTime = rs.getTimestamp(CREATE_TIME);
				this.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				this.status = rs.getString(STATUS);
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET " + CODE + " = ?, " + NAME + " = ?, " + DESCRIPTION
					+ " = ?, " + ACC_NUMBER + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY + " = ?, " + LEVEL_LOW
					+ " = ?, " + LEVEL_HIGH + " = ?, " + FACILITY_AMOUNT + " = ?, " + OVERDRAFT_LIMIT + " = ?, "
					+ SIGNATORY1 + " = ?, " + SIGNATORY2 + " = ?, " + SIGNATORY3 + " = ?, " + SIGNATORY4 + " = ?, "
					+ SIGNATURE + " = ?, " + PC_CENTER + " = ?, " + ADD1 + " = ?, " + ADD2 + " = ?, " + ADD3 + " = ?, "
					+ STATE + " = ?, " + COUNTRY + " = ?, " + PHONE + " = ?, " + CONTACT_PERSON + " = ?, " + FAX
					+ " = ?, " + USERID_CREATE + " = ?, " + USERID_UPDATE + " = ?, " + CREATE_TIME + " = ?, "
					+ LAST_UPDATE + " = ?, " + STATUS + " = ? WHERE " + PKID + " = ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.code);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.accountNumber);
			ps.setString(5, this.accountType);
			ps.setString(6, this.currency);
			ps.setBigDecimal(7, this.levelLow);
			ps.setBigDecimal(8, this.levelHigh);
			ps.setBigDecimal(9, this.facilityAmount);
			ps.setBigDecimal(10, this.overdraftLimit);
			ps.setString(11, this.signatory1);
			ps.setString(12, this.signatory2);
			ps.setString(13, this.signatory3);
			ps.setString(14, this.signatory4);
			ps.setBytes(15, this.signature);
			ps.setInt(16, this.pcCenter.intValue());
			ps.setString(17, this.add1);
			ps.setString(18, this.add2);
			ps.setString(19, this.add3);
			ps.setString(20, this.state);
			ps.setString(21, this.country);
			ps.setString(22, this.phone);
			ps.setString(23, this.contactPerson);
			ps.setString(24, this.fax);
			ps.setInt(25, this.userIdCreate.intValue());
			ps.setInt(26, this.userIdUpdate.intValue());
			ps.setTimestamp(27, this.createTime);
			ps.setTimestamp(28, this.lastUpdate);
			ps.setString(29, this.status);
			ps.setInt(30, this.pkId.intValue());
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
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME;
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class CashAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_cash_account";
	protected final String strObjectName = "CashAccountBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ACC_NUMBER = "acc_number";
	public static final String ACC_TYPE = "acc_type"; // / filled with GL Code
	public static final String CURRENCY = "currency";
	public static final String LEVEL_LOW = "level_low";
	public static final String LEVEL_HIGH = "level_high";
	public static final String FACILITY_AMOUNT = "facility_amount";
	public static final String OVERDRAFT_LIMIT = "overdraft_limit";
	public static final String SIGNATORY1 = "signatory1";
	public static final String SIGNATORY2 = "signatory2";
	public static final String SIGNATORY3 = "signatory3";
	public static final String SIGNATORY4 = "signatory4";
	public static final String SIGNATURE = "signature";
	public static final String PC_CENTER = "pc_center";
	public static final String ADD1 = "add1";
	public static final String ADD2 = "add2";
	public static final String ADD3 = "add3";
	public static final String STATE = "state";
	public static final String COUNTRY = "country";
	public static final String PHONE = "phone";
	public static final String CONTACT_PERSON = "contact_person";
	public static final String FAX = "fax";
	public static final String USERID_CREATE = "userid_create";
	public static final String USERID_UPDATE = "userid_update";
	public static final String CREATE_TIME = "createtime";
	public static final String LAST_UPDATE = "lastupdate";
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String code;
	private String name;
	private String description;
	private String accountNumber;
	private String accountType;
	private String currency;
	private BigDecimal levelLow;
	private BigDecimal levelHigh;
	private BigDecimal facilityAmount;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
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

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public String getCode()
	{
		return this.code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public String getAccountType()
	{
		return this.accountType;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public BigDecimal getLevelLow()
	{
		return this.levelLow;
	}

	public void setLevelLow(BigDecimal levelLow)
	{
		this.levelLow = levelLow;
	}

	public BigDecimal getLevelHigh()
	{
		return this.levelHigh;
	}

	public void setLevelHigh(BigDecimal levelHigh)
	{
		this.levelHigh = levelHigh;
	}

	public BigDecimal getFacilityAmount()
	{
		return this.facilityAmount;
	}

	public void setFacilityAmount(BigDecimal facilityAmount)
	{
		this.facilityAmount = facilityAmount;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public void setOverdraftLimit(BigDecimal overdraftLimit)
	{
		this.overdraftLimit = overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public void setSignature(byte[] signature)
	{
		this.signature = signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
	}

	public void setPCCenter(Integer pcCenter)
	{
		this.pcCenter = pcCenter;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public String getState()
	{
		return this.state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public void setContactPerson(String contactPerson)
	{
		this.contactPerson = contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public void setFax(String fax)
	{
		this.fax = fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public void setUserIdCreate(Integer userIdCreate)
	{
		this.userIdCreate = userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public CashAccountObject getValueObject()
	{
		CashAccountObject bankAccObj = new CashAccountObject();
		bankAccObj.pkId = this.pkId;
		bankAccObj.code = this.code;
		bankAccObj.name = this.name;
		bankAccObj.description = this.description;
		bankAccObj.accountNumber = this.accountNumber;
		bankAccObj.accountType = this.accountType;
		bankAccObj.currency = this.currency;
		bankAccObj.levelLow = this.levelLow;
		bankAccObj.levelHigh = this.levelHigh;
		bankAccObj.facilityAmount = this.facilityAmount;
		bankAccObj.overdraftLimit = this.overdraftLimit;
		bankAccObj.signatory1 = this.signatory1;
		bankAccObj.signatory2 = this.signatory2;
		bankAccObj.signatory3 = this.signatory3;
		bankAccObj.signatory4 = this.signatory4;
		bankAccObj.signature = this.signature;
		bankAccObj.pcCenter = this.pcCenter;
		bankAccObj.add1 = this.add1;
		bankAccObj.add2 = this.add2;
		bankAccObj.add3 = this.add3;
		bankAccObj.state = this.state;
		bankAccObj.country = this.country;
		bankAccObj.phone = this.phone;
		bankAccObj.contactPerson = this.contactPerson;
		bankAccObj.fax = this.fax;
		bankAccObj.userIdCreate = this.userIdCreate;
		bankAccObj.userIdUpdate = this.userIdUpdate;
		bankAccObj.createTime = this.createTime;
		bankAccObj.lastUpdate = this.lastUpdate;
		bankAccObj.status = this.status;
		return bankAccObj;
	}

	public void setValueObject(CashAccountObject cao) throws Exception
	{
		if (cao == null)
		{
			throw new Exception("Object undefined");
		}
		this.code = cao.code;
		this.name = cao.name;
		this.description = cao.description;
		this.accountNumber = cao.accountNumber;
		this.accountType = cao.accountType;
		this.currency = cao.currency;
		this.levelLow = cao.levelLow;
		this.levelHigh = cao.levelHigh;
		this.facilityAmount = cao.facilityAmount;
		this.overdraftLimit = cao.overdraftLimit;
		this.signatory1 = cao.signatory1;
		this.signatory2 = cao.signatory2;
		this.signatory3 = cao.signatory3;
		this.signatory4 = cao.signatory4;
		this.signature = cao.signature;
		this.pcCenter = cao.pcCenter;
		this.add1 = cao.add1;
		this.add2 = cao.add2;
		this.add3 = cao.add3;
		this.state = cao.state;
		this.country = cao.country;
		this.phone = cao.phone;
		this.contactPerson = cao.contactPerson;
		this.fax = cao.fax;
		this.userIdCreate = cao.userIdCreate;
		this.userIdUpdate = cao.userIdUpdate;
		this.createTime = cao.createTime;
		this.lastUpdate = cao.lastUpdate;
		this.status = cao.status;
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

	public Integer ejbCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
				facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature, pcCenter,
				add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate, createTime,
				lastUpdate, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.code = code;
			this.name = name;
			this.description = description;
			this.accountNumber = accountNumber;
			this.accountType = accountType;
			this.currency = currency;
			this.levelLow = levelLow;
			this.levelHigh = levelHigh;
			this.facilityAmount = facilityAmount;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
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

	public void ejbPostCreate(String code, String name, String description, String accountNumber, String accountType,
			String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
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

	private Integer insertNewRow(String code, String name, String description, String accountNumber,
			String accountType, String currency, BigDecimal levelLow, BigDecimal levelHigh, BigDecimal facilityAmount,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state, String country,
			String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", "
					+ LEVEL_HIGH + ", " + FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", "
					+ SIGNATORY2 + ", " + SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", "
					+ ADD1 + ", " + ADD2 + ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", "
					+ CONTACT_PERSON + ", " + FAX + ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME
					+ ", " + LAST_UPDATE + ", " + STATUS + " ) " + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, accountNumber);
			ps.setString(6, accountType);
			ps.setString(7, currency);
			ps.setBigDecimal(8, levelLow);
			ps.setBigDecimal(9, levelHigh);
			ps.setBigDecimal(10, facilityAmount);
			ps.setBigDecimal(11, overdraftLimit);
			ps.setString(12, signatory1);
			ps.setString(13, signatory2);
			ps.setString(14, signatory3);
			ps.setString(15, signatory4);
			ps.setBytes(16, signature);
			ps.setInt(17, pcCenter.intValue());
			ps.setString(18, add1);
			ps.setString(19, add2);
			ps.setString(20, add3);
			ps.setString(21, state);
			ps.setString(22, country);
			ps.setString(23, phone);
			ps.setString(24, contactPerson);
			ps.setString(25, fax);
			ps.setInt(26, userIdCreate.intValue());
			ps.setInt(27, userIdUpdate.intValue());
			ps.setTimestamp(28, createTime);
			ps.setTimestamp(29, lastUpdate);
			ps.setString(30, status);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws SQLException
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?  AND " + STATUS + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement += " AND " + fieldName2 + " = ? ";
			}
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, STATUS_ACTIVE);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(3, value2);
			}
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				CashAccountObject caObj = new CashAccountObject();
				caObj.pkId = new Integer(rs.getInt(PKID));
				caObj.code = rs.getString(CODE);
				caObj.name = rs.getString(NAME);
				caObj.description = rs.getString(DESCRIPTION);
				caObj.accountNumber = rs.getString(ACC_NUMBER);
				caObj.accountType = rs.getString(ACC_TYPE);
				caObj.currency = rs.getString(CURRENCY);
				caObj.levelLow = rs.getBigDecimal(LEVEL_LOW);
				caObj.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				caObj.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				caObj.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				caObj.signatory1 = rs.getString(SIGNATORY1);
				caObj.signatory2 = rs.getString(SIGNATORY2);
				caObj.signatory3 = rs.getString(SIGNATORY3);
				caObj.signatory4 = rs.getString(SIGNATORY4);
				caObj.signature = rs.getBytes(SIGNATURE);
				caObj.pcCenter = new Integer(rs.getString(PC_CENTER));
				caObj.add1 = rs.getString(ADD1);
				caObj.add2 = rs.getString(ADD2);
				caObj.add3 = rs.getString(ADD3);
				caObj.state = rs.getString(STATE);
				caObj.country = rs.getString(COUNTRY);
				caObj.phone = rs.getString(PHONE);
				caObj.contactPerson = rs.getString(CONTACT_PERSON);
				caObj.fax = rs.getString(FAX);
				caObj.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				caObj.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				caObj.createTime = rs.getTimestamp(CREATE_TIME);
				caObj.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				caObj.status = rs.getString(STATUS);
				vecValObj.add(caObj);
			} // / end while
		}// end try
		catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	} // end of selectValueObjectsGiven

	// ////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT  " + PKID + ", " + CODE + ", " + NAME + ", " + DESCRIPTION + ", "
					+ ACC_NUMBER + ", " + ACC_TYPE + ", " + CURRENCY + ", " + LEVEL_LOW + ", " + LEVEL_HIGH + ", "
					+ FACILITY_AMOUNT + ", " + OVERDRAFT_LIMIT + ", " + SIGNATORY1 + ", " + SIGNATORY2 + ", "
					+ SIGNATORY3 + ", " + SIGNATORY4 + ", " + SIGNATURE + ", " + PC_CENTER + ", " + ADD1 + ", " + ADD2
					+ ", " + ADD3 + ", " + STATE + ", " + COUNTRY + ", " + PHONE + ", " + CONTACT_PERSON + ", " + FAX
					+ ", " + USERID_CREATE + ", " + USERID_UPDATE + ", " + CREATE_TIME + ", " + LAST_UPDATE + ", "
					+ STATUS + " FROM " + TABLENAME + " WHERE pkid =?";
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt(PKID));
				this.code = rs.getString(CODE);
				this.name = rs.getString(NAME);
				this.description = rs.getString(DESCRIPTION);
				this.accountNumber = rs.getString(ACC_NUMBER);
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.levelLow = rs.getBigDecimal(LEVEL_LOW);
				this.levelHigh = rs.getBigDecimal(LEVEL_HIGH);
				this.facilityAmount = rs.getBigDecimal(FACILITY_AMOUNT);
				this.overdraftLimit = rs.getBigDecimal(OVERDRAFT_LIMIT);
				this.signatory1 = rs.getString(SIGNATORY1);
				this.signatory2 = rs.getString(SIGNATORY2);
				this.signatory3 = rs.getString(SIGNATORY3);
				this.signatory4 = rs.getString(SIGNATORY4);
				this.signature = rs.getBytes(SIGNATURE);
				this.pcCenter = new Integer(rs.getString(PC_CENTER));
				this.add1 = rs.getString(ADD1);
				this.add2 = rs.getString(ADD2);
				this.add3 = rs.getString(ADD3);
				this.state = rs.getString(STATE);
				this.country = rs.getString(COUNTRY);
				this.phone = rs.getString(PHONE);
				this.contactPerson = rs.getString(CONTACT_PERSON);
				this.fax = rs.getString(FAX);
				this.userIdCreate = new Integer(rs.getString(USERID_CREATE));
				this.userIdUpdate = new Integer(rs.getString(USERID_UPDATE));
				this.createTime = rs.getTimestamp(CREATE_TIME);
				this.lastUpdate = rs.getTimestamp(LAST_UPDATE);
				this.status = rs.getString(STATUS);
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET " + CODE + " = ?, " + NAME + " = ?, " + DESCRIPTION
					+ " = ?, " + ACC_NUMBER + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY + " = ?, " + LEVEL_LOW
					+ " = ?, " + LEVEL_HIGH + " = ?, " + FACILITY_AMOUNT + " = ?, " + OVERDRAFT_LIMIT + " = ?, "
					+ SIGNATORY1 + " = ?, " + SIGNATORY2 + " = ?, " + SIGNATORY3 + " = ?, " + SIGNATORY4 + " = ?, "
					+ SIGNATURE + " = ?, " + PC_CENTER + " = ?, " + ADD1 + " = ?, " + ADD2 + " = ?, " + ADD3 + " = ?, "
					+ STATE + " = ?, " + COUNTRY + " = ?, " + PHONE + " = ?, " + CONTACT_PERSON + " = ?, " + FAX
					+ " = ?, " + USERID_CREATE + " = ?, " + USERID_UPDATE + " = ?, " + CREATE_TIME + " = ?, "
					+ LAST_UPDATE + " = ?, " + STATUS + " = ? WHERE " + PKID + " = ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.code);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.accountNumber);
			ps.setString(5, this.accountType);
			ps.setString(6, this.currency);
			ps.setBigDecimal(7, this.levelLow);
			ps.setBigDecimal(8, this.levelHigh);
			ps.setBigDecimal(9, this.facilityAmount);
			ps.setBigDecimal(10, this.overdraftLimit);
			ps.setString(11, this.signatory1);
			ps.setString(12, this.signatory2);
			ps.setString(13, this.signatory3);
			ps.setString(14, this.signatory4);
			ps.setBytes(15, this.signature);
			ps.setInt(16, this.pcCenter.intValue());
			ps.setString(17, this.add1);
			ps.setString(18, this.add2);
			ps.setString(19, this.add3);
			ps.setString(20, this.state);
			ps.setString(21, this.country);
			ps.setString(22, this.phone);
			ps.setString(23, this.contactPerson);
			ps.setString(24, this.fax);
			ps.setInt(25, this.userIdCreate.intValue());
			ps.setInt(26, this.userIdUpdate.intValue());
			ps.setTimestamp(27, this.createTime);
			ps.setTimestamp(28, this.lastUpdate);
			ps.setString(29, this.status);
			ps.setInt(30, this.pkId.intValue());
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
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME;
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
			sqlStatement += " ORDER BY " + PC_CENTER + "," + NAME + " ";
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
