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

public class GLCodeBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcode_index";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	protected final String strObjectName = "GLCodeBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LEDGERSIDE = "ledgerside";
	public static final String GLCATEGORYID = "glcategoryid";
	public static final String CODE_OLD = "code_old";
	// 20080411 Jimmy
	public static final String GROUPING1 = "grouping1";
	public static final String GROUPING2 = "grouping2";
	public static final String GROUPING3 = "grouping3";
	
	private GLCodeObject valObj;
	// Constants for GLCode
	public static final String PROFIT_LOSS = "profitLoss";
	public static final String CASH_DISCOUNT = "cashDiscount";
	public static final String PURCHASES = "purchases";
	public static final String PURCHASE_RETURNS = "purchaseReturns";
	public static final String GENERAL_SALES = "generalSales";
	public static final String GENERAL_SALES_DISCOUNT = "generalSalesDiscount";
	public static final String GENERAL_SALES_RETURN = "generalSalesReturn";
	public static final String GST_PAYABLE = "gstPayable";
	public static final String ACC_RECEIVABLE = "accReceivable";
	public static final String ACC_PAYABLE = "accPayable";
	public static final String OTHER_DEBTOR = "otherDebtor";
	public static final String OTHER_CREDITOR = "otherCreditor";
	public static final String BAD_DEBT = "badDebt";
	public static final String INTEREST_REVENUE = "interestRevenue";
	public static final String CREDIT_CARD_CHARGES = "creditCardCharges";
	public static final String INVENTORY_COST = "inventoryCost";
	public static final String INVENTORY = "inventory";
	public static final String INTEREST_BANK_CHARGES = "interestBankCharges";
	public static final String INVENTORY_VARIANCE = "inventoryVariance";
	public static final String FOREX_GAINLOSS = "forexGainLoss";
	public static final String CRV_EXPENSE = "crvExpense";

	// public static final String NOMINAL_ACCOUNT = "nominalAcc";
	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public String getCode()
	{
		return this.valObj.code;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getLedgerSide()
	{
		return this.valObj.ledgerSide;
	}

	public Integer getGLCategoryId()
	{
		return this.valObj.glCategoryId;
	}

	public String getCodeOld()
	{
		return this.valObj.codeOld;
	}
	// 20080411 Jimmy
	public String getGrouping1()
	{
		return this.valObj.grouping1;
	}
	
	public String getGrouping2()
	{
		return this.valObj.grouping2;
	}
	
	public String getGrouping3()
	{
		return this.valObj.grouping3;
	}
	/////////////////
	public void setPkId(Integer pkid)
	{
		
		this.valObj.pkId = pkid;
	}

	public void setCode(String strCode)
	{
		this.valObj.code = strCode;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setLedgerSide(String strLedgerSide)
	{
		this.valObj.ledgerSide = strLedgerSide;
	}

	public void setGLCategoryId(Integer glcat)
	{
		this.valObj.glCategoryId = glcat;
	}

	public void setCodeOld(String strCodeOld)
	{
		this.valObj.codeOld = strCodeOld;
	}
	// 200804011 Jimmy
	public void setGrouping1(String strGrouping1)
	{
		this.valObj.grouping1 = strGrouping1;
	}
	
	public void setGrouping2(String strGrouping2)
	{
		this.valObj.grouping2 = strGrouping2;
	}
	
	public void setGrouping3(String strGrouping3)
	{
		this.valObj.grouping3 = strGrouping3;
	}
	///////////////////
	public GLCodeObject getObject()
	{
		return getValueObject();
	}

	public void setObject(GLCodeObject glco)
	{
		try
		{
			setValueObject(glco);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public GLCodeObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GLCodeObject glco) throws Exception
	{
		if (glco == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.code = glco.code;
		this.valObj.name = glco.name;
		this.valObj.description = glco.description;
		this.valObj.ledgerSide = glco.ledgerSide;
		this.valObj.glCategoryId = glco.glCategoryId;
		this.valObj.codeOld = glco.codeOld;
		this.valObj.grouping1 = glco.grouping1;
		this.valObj.grouping2 = glco.grouping2;
		this.valObj.grouping3 = glco.grouping3;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId, 
			String codeOld, String grouping1, String grouping2, String grouping3 )throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, glCategoryId, codeOld, 
					grouping1, grouping2, grouping3);
		if (newPkId != null)
		{
			this.valObj = new GLCodeObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
			this.valObj.codeOld = codeOld;
			this.valObj.grouping1 = grouping1;
			this.valObj.grouping2 = grouping2;
			this.valObj.grouping3 = grouping3;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public Integer ejbCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId, String codeOld, String grouping1, String grouping2, String grouping3) throws CreateException
	{
		Log.printVerbose(strObjectName + " In ejbCreate");
		boolean success = insertNewRow(newPkId, code, name, description, ledgerSide, glCategoryId, codeOld, 
								grouping1, grouping2, grouping3);
		if (success)
		{
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
			this.valObj.codeOld = codeOld;
			this.valObj.grouping1 = grouping1;
			this.valObj.grouping2 = grouping2;
			this.valObj.grouping3 = grouping3;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new GLCodeObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
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

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId, 
			String codeOld, String grouping1, String gruping2, String grouping3)
	{
	}

	public void ejbPostCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId, String codeOld, String grouping1, String gruping2, String grouping3)
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
		Collection col = null;
		try
		{
			col = selectObjectsGiven(fieldName, value);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	public Vector ejbHomeGetObjects(QueryObject query)
	{
		Vector vecReturn = null;
		try
		{
			vecReturn = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecReturn;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, Integer glCategoryId,
			String codeOld, String grouping1, String grouping2, String grouping3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + ", " + CODE_OLD + ", "
					+ GROUPING1 + ", " + GROUPING2 + ", " + GROUPING3
					+ " ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.setString(7, codeOld);
			ps.setString(8, grouping1); //20080411 Jimmy
			ps.setString(9, grouping2);
			ps.setString(10, grouping3);
			ps.executeUpdate();
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean insertNewRow(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId, String codeOld, String grouping1, String grouping2, String grouping3)
	{
		boolean success = true;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
									+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + ", " + CODE_OLD + ", "
									+ GROUPING1 + ", " + GROUPING2 + ", " + GROUPING3
									+ " ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.setString(7, codeOld);
			ps.setString(8, grouping1); 
			ps.setString(9, grouping2);
			ps.setString(10, grouping3);
			ps.executeUpdate();
		} catch (Exception e)
		{
			success = false;
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return success;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid, code_old, grouping1, grouping2, grouping3 FROM " + TABLENAME
					+ " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
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
					+ " SET code = ?, name = ?, description = ?, ledgerside = ?, glcategoryid = ?, code_old = ?, grouping1 = ?, grouping2 = ?, grouping3 = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setInt(5, this.valObj.glCategoryId.intValue());
			ps.setString(6, this.valObj.codeOld);
			ps.setString(7, this.valObj.grouping1);
			ps.setString(8, this.valObj.grouping2);
			ps.setString(9, this.valObj.grouping3);
			ps.setInt(10, this.valObj.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.valObj.pkId.toString() + " failed.");
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
		ArrayList objectSet = new ArrayList();
		int count = 0;
		try
		{
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			Log.printVerbose(".................. "+sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
//			if (rs.next())
//			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					count++;
					objectSet.add(new Integer(rs.getInt(1)));
				}
//			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		if (count > 0)
		{
			return objectSet;
		} else
		{
			return null;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid, code_old, grouping1, grouping2, grouping3 FROM " + TABLENAME
					+ " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
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
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	private Vector selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			Log.printVerbose("The SQL Statement is " + sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	public static GLCodeObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GLCodeObject theObj = null;
		try
		{
			theObj = new GLCodeObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.glCategoryId = new Integer(rs.getInt("glcategoryid"));
			theObj.codeOld = rs.getString("code_old");
			theObj.grouping1 = rs.getString("grouping1");
			theObj.grouping2 = rs.getString("grouping2");
			theObj.grouping3 = rs.getString("grouping3");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
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

public class GLCodeBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcode_index";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	protected final String strObjectName = "GLCodeBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LEDGERSIDE = "ledgerside";
	public static final String GLCATEGORYID = "glcategoryid";
	private GLCodeObject valObj;
	// Constants for GLCode
	public static final String PROFIT_LOSS = "profitLoss";
	public static final String CASH_DISCOUNT = "cashDiscount";
	public static final String PURCHASES = "purchases";
	public static final String PURCHASE_RETURNS = "purchaseReturns";
	public static final String GENERAL_SALES = "generalSales";
	public static final String GENERAL_SALES_DISCOUNT = "generalSalesDiscount";
	public static final String GENERAL_SALES_RETURN = "generalSalesReturn";
	public static final String GST_PAYABLE = "gstPayable";
	public static final String ACC_RECEIVABLE = "accReceivable";
	public static final String ACC_PAYABLE = "accPayable";
	public static final String OTHER_DEBTOR = "otherDebtor";
	public static final String OTHER_CREDITOR = "otherCreditor";
	public static final String BAD_DEBT = "badDebt";
	public static final String INTEREST_REVENUE = "interestRevenue";
	public static final String CREDIT_CARD_CHARGES = "creditCardCharges";
	public static final String INVENTORY_COST = "inventoryCost";
	public static final String INVENTORY = "inventory";
	public static final String INTEREST_BANK_CHARGES = "interestBankCharges";
	public static final String INVENTORY_VARIANCE = "inventoryVariance";
	public static final String FOREX_GAINLOSS = "forexGainLoss";
	public static final String CRV_EXPENSE = "crvExpense";

	// public static final String NOMINAL_ACCOUNT = "nominalAcc";
	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public String getCode()
	{
		return this.valObj.code;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getLedgerSide()
	{
		return this.valObj.ledgerSide;
	}

	public Integer getGLCategoryId()
	{
		return this.valObj.glCategoryId;
	}

	public void setPkId(Integer pkid)
	{
		this.valObj.pkId = pkid;
	}

	public void setCode(String strCode)
	{
		this.valObj.code = strCode;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setLedgerSide(String strLedgerSide)
	{
		this.valObj.ledgerSide = strLedgerSide;
	}

	public void setGLCategoryId(Integer glcat)
	{
		this.valObj.glCategoryId = glcat;
	}

	public GLCodeObject getObject()
	{
		return getValueObject();
	}

	public void setObject(GLCodeObject glco)
	{
		try
		{
			setValueObject(glco);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public GLCodeObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GLCodeObject glco) throws Exception
	{
		if (glco == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.code = glco.code;
		this.valObj.name = glco.name;
		this.valObj.description = glco.description;
		this.valObj.ledgerSide = glco.ledgerSide;
		this.valObj.glCategoryId = glco.glCategoryId;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId)
			throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, glCategoryId);
		if (newPkId != null)
		{
			this.valObj = new GLCodeObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public Integer ejbCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId) throws CreateException
	{
		Log.printVerbose(strObjectName + " In ejbCreate");
		boolean success = insertNewRow(newPkId, code, name, description, ledgerSide, glCategoryId);
		if (success)
		{
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new GLCodeObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
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

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId)
	{
	}

	public void ejbPostCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId)
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
		Collection col = null;
		try
		{
			col = selectObjectsGiven(fieldName, value);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	public Vector ejbHomeGetObjects(QueryObject query)
	{
		Vector vecReturn = null;
		try
		{
			vecReturn = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecReturn;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, Integer glCategoryId)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + " ) VALUES (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.executeUpdate();
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean insertNewRow(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId)
	{
		boolean success = true;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + " ) VALUES (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.executeUpdate();
		} catch (Exception e)
		{
			success = false;
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return success;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid FROM " + TABLENAME
					+ " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
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
					+ " SET code = ?, name = ?, description = ?, ledgerside = ?, glcategoryid = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setInt(5, this.valObj.glCategoryId.intValue());
			ps.setInt(6, this.valObj.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.valObj.pkId.toString() + " failed.");
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
		ArrayList objectSet = new ArrayList();
		int count = 0;
		try
		{
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			Log.printVerbose(".................. "+sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
//			if (rs.next())
//			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					count++;
					objectSet.add(new Integer(rs.getInt(1)));
				}
//			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		if (count > 0)
		{
			return objectSet;
		} else
		{
			return null;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid FROM " + TABLENAME
					+ " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
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
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	private Vector selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			Log.printVerbose("The SQL Statement is " + sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	public static GLCodeObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GLCodeObject theObj = null;
		try
		{
			theObj = new GLCodeObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.glCategoryId = new Integer(rs.getInt("glcategoryid"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
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

public class GLCodeBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcode_index";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	protected final String strObjectName = "GLCodeBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LEDGERSIDE = "ledgerside";
	public static final String GLCATEGORYID = "glcategoryid";
	private GLCodeObject valObj;
	// Constants for GLCode
	public static final String PROFIT_LOSS = "profitLoss";
	public static final String CASH_DISCOUNT = "cashDiscount";
	public static final String PURCHASES = "purchases";
	public static final String PURCHASE_RETURNS = "purchaseReturns";
	public static final String GENERAL_SALES = "generalSales";
	public static final String GENERAL_SALES_DISCOUNT = "generalSalesDiscount";
	public static final String GENERAL_SALES_RETURN = "generalSalesReturn";
	public static final String GST_PAYABLE = "gstPayable";
	public static final String ACC_RECEIVABLE = "accReceivable";
	public static final String ACC_PAYABLE = "accPayable";
	public static final String OTHER_DEBTOR = "otherDebtor";
	public static final String OTHER_CREDITOR = "otherCreditor";
	public static final String BAD_DEBT = "badDebt";
	public static final String INTEREST_REVENUE = "interestRevenue";
	public static final String CREDIT_CARD_CHARGES = "creditCardCharges";
	public static final String INVENTORY_COST = "inventoryCost";
	public static final String INVENTORY = "inventory";
	public static final String INTEREST_BANK_CHARGES = "interestBankCharges";
	public static final String INVENTORY_VARIANCE = "inventoryVariance";
	public static final String FOREX_GAINLOSS = "forexGainLoss";
	public static final String CRV_EXPENSE = "crvExpense";

	// public static final String NOMINAL_ACCOUNT = "nominalAcc";
	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public String getCode()
	{
		return this.valObj.code;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getLedgerSide()
	{
		return this.valObj.ledgerSide;
	}

	public Integer getGLCategoryId()
	{
		return this.valObj.glCategoryId;
	}

	public void setPkId(Integer pkid)
	{
		this.valObj.pkId = pkid;
	}

	public void setCode(String strCode)
	{
		this.valObj.code = strCode;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setLedgerSide(String strLedgerSide)
	{
		this.valObj.ledgerSide = strLedgerSide;
	}

	public void setGLCategoryId(Integer glcat)
	{
		this.valObj.glCategoryId = glcat;
	}

	public GLCodeObject getObject()
	{
		return getValueObject();
	}

	public void setObject(GLCodeObject glco)
	{
		try
		{
			setValueObject(glco);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public GLCodeObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GLCodeObject glco) throws Exception
	{
		if (glco == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.code = glco.code;
		this.valObj.name = glco.name;
		this.valObj.description = glco.description;
		this.valObj.ledgerSide = glco.ledgerSide;
		this.valObj.glCategoryId = glco.glCategoryId;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId)
			throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, glCategoryId);
		if (newPkId != null)
		{
			this.valObj = new GLCodeObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public Integer ejbCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId) throws CreateException
	{
		Log.printVerbose(strObjectName + " In ejbCreate");
		boolean success = insertNewRow(newPkId, code, name, description, ledgerSide, glCategoryId);
		if (success)
		{
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new GLCodeObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
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

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId)
	{
	}

	public void ejbPostCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId)
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
		Collection col = null;
		try
		{
			col = selectObjectsGiven(fieldName, value);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	public Vector ejbHomeGetObjects(QueryObject query)
	{
		Vector vecReturn = null;
		try
		{
			vecReturn = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecReturn;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, Integer glCategoryId)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + " ) VALUES (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.executeUpdate();
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean insertNewRow(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId)
	{
		boolean success = true;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + " ) VALUES (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.executeUpdate();
		} catch (Exception e)
		{
			success = false;
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return success;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid FROM " + TABLENAME
					+ " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
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
					+ " SET code = ?, name = ?, description = ?, ledgerside = ?, glcategoryid = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setInt(5, this.valObj.glCategoryId.intValue());
			ps.setInt(6, this.valObj.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.valObj.pkId.toString() + " failed.");
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
		ArrayList objectSet = new ArrayList();
		int count = 0;
		try
		{
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
					count++;
					objectSet.add(new Integer(rs.getInt(1)));
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		if (count > 0)
		{
			return objectSet;
		} else
		{
			return null;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid FROM " + TABLENAME
					+ " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
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
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	private Vector selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			Log.printVerbose("The SQL Statement is " + sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	public static GLCodeObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GLCodeObject theObj = null;
		try
		{
			theObj = new GLCodeObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.glCategoryId = new Integer(rs.getInt("glcategoryid"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
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

public class GLCodeBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcode_index";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	protected final String strObjectName = "GLCodeBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LEDGERSIDE = "ledgerside";
	public static final String GLCATEGORYID = "glcategoryid";
	private GLCodeObject valObj;
	// Constants for GLCode
	public static final String PROFIT_LOSS = "profitLoss";
	public static final String CASH_DISCOUNT = "cashDiscount";
	public static final String PURCHASES = "purchases";
	public static final String PURCHASE_RETURNS = "purchaseReturns";
	public static final String GENERAL_SALES = "generalSales";
	public static final String GENERAL_SALES_DISCOUNT = "generalSalesDiscount";
	public static final String GENERAL_SALES_RETURN = "generalSalesReturn";
	public static final String GST_PAYABLE = "gstPayable";
	public static final String ACC_RECEIVABLE = "accReceivable";
	public static final String ACC_PAYABLE = "accPayable";
	public static final String OTHER_DEBTOR = "otherDebtor";
	public static final String OTHER_CREDITOR = "otherCreditor";
	public static final String BAD_DEBT = "badDebt";
	public static final String INTEREST_REVENUE = "interestRevenue";
	public static final String CREDIT_CARD_CHARGES = "creditCardCharges";
	public static final String INVENTORY_COST = "inventoryCost";
	public static final String INVENTORY = "inventory";
	public static final String INTEREST_BANK_CHARGES = "interestBankCharges";
	public static final String INVENTORY_VARIANCE = "inventoryVariance";
	public static final String FOREX_GAINLOSS = "forexGainLoss";
	public static final String CRV_EXPENSE = "crvExpense";

	// public static final String NOMINAL_ACCOUNT = "nominalAcc";
	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public String getCode()
	{
		return this.valObj.code;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getLedgerSide()
	{
		return this.valObj.ledgerSide;
	}

	public Integer getGLCategoryId()
	{
		return this.valObj.glCategoryId;
	}

	public void setPkId(Integer pkid)
	{
		this.valObj.pkId = pkid;
	}

	public void setCode(String strCode)
	{
		this.valObj.code = strCode;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setLedgerSide(String strLedgerSide)
	{
		this.valObj.ledgerSide = strLedgerSide;
	}

	public void setGLCategoryId(Integer glcat)
	{
		this.valObj.glCategoryId = glcat;
	}

	public GLCodeObject getObject()
	{
		return getValueObject();
	}

	public void setObject(GLCodeObject glco)
	{
		try
		{
			setValueObject(glco);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public GLCodeObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GLCodeObject glco) throws Exception
	{
		if (glco == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.code = glco.code;
		this.valObj.name = glco.name;
		this.valObj.description = glco.description;
		this.valObj.ledgerSide = glco.ledgerSide;
		this.valObj.glCategoryId = glco.glCategoryId;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId)
			throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, glCategoryId);
		if (newPkId != null)
		{
			this.valObj = new GLCodeObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public Integer ejbCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId) throws CreateException
	{
		Log.printVerbose(strObjectName + " In ejbCreate");
		boolean success = insertNewRow(newPkId, code, name, description, ledgerSide, glCategoryId);
		if (success)
		{
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.glCategoryId = glCategoryId;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new GLCodeObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
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

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, Integer glCategoryId)
	{
	}

	public void ejbPostCreate(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId)
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
		Collection col = null;
		try
		{
			col = selectObjectsGiven(fieldName, value);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	public Vector ejbHomeGetObjects(QueryObject query)
	{
		Vector vecReturn = null;
		try
		{
			vecReturn = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecReturn;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, Integer glCategoryId)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + " ) VALUES (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.executeUpdate();
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean insertNewRow(Integer newPkId, String code, String name, String description, String ledgerSide,
			Integer glCategoryId)
	{
		boolean success = true;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "INSERT INTO " + TABLENAME + " (" + PKID + ", " + CODE + ", " + NAME + ", "
					+ DESCRIPTION + ", " + LEDGERSIDE + ", " + GLCATEGORYID + " ) VALUES (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setInt(6, glCategoryId.intValue());
			ps.executeUpdate();
		} catch (Exception e)
		{
			success = false;
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return success;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid FROM " + TABLENAME
					+ " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
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
					+ " SET code = ?, name = ?, description = ?, ledgerside = ?, glcategoryid = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setInt(5, this.valObj.glCategoryId.intValue());
			ps.setInt(6, this.valObj.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.valObj.pkId.toString() + " failed.");
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
		ArrayList objectSet = new ArrayList();
		int count = 0;
		try
		{
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			Log.printVerbose(".................. "+sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
//			if (rs.next())
//			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					count++;
					objectSet.add(new Integer(rs.getInt(1)));
				}
//			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		if (count > 0)
		{
			return objectSet;
		} else
		{
			return null;
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, glcategoryid FROM " + TABLENAME
					+ " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
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
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	private Vector selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			Log.printVerbose("The SQL Statement is " + sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCodeObject glco = getObject(rs, "");
				vecValObj.add(glco);
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

	public static GLCodeObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GLCodeObject theObj = null;
		try
		{
			theObj = new GLCodeObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.glCategoryId = new Integer(rs.getInt("glcategoryid"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}
}
