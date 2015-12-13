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

public class GLCategoryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcategory_index";
	protected final String strObjectName = "GLCategoryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String POST_TO_SECTION = "postto_section";
	public static final String REAL_TEMP = "real_temp";

	public static final String LS_DEBIT = "dr";
	public static final String LS_CREDIT = "cr";
	public static final String RT_REAL = "real";
	public static final String RT_TEMP = "temp";
	// / GL Type / PostToSection
	public static final String COGS = "CostOfGoodsSold";
	public static final String COGS_EXPENSES = "CostOfGoodsSold-Expenses";
	public static final String COGS_GAINS = "CostOfGoodsSold-Gains";
	public static final String CURRENT_ASSETS = "CurrentAssets";
	public static final String CURRENT_LIABILITIES = "CurrentLiabilities";
	public static final String FIXED_ASSETS = "FixedAssets";
	public static final String FIXED_ASSETS_ACCUMULATED_DEPRECIATION = "FixedAssets-AccumulatedDepreciation";
	public static final String GENERAL_EXPENSES = "GeneralExpenses";
	public static final String LONG_TERM_LIABILITIES = "LongTermLiabilities";
	public static final String OTHER_ASSETS = "OtherAssets";
	public static final String OTHER_REVENUES = "OtherRevenues";
	public static final String SALES_EXPENSES = "SalesExpenses";
	public static final String SALES_REVENUE = "SalesRevenue";
	public static final String SHARE_CAPITAL = "ShareCapital";
	public static final String TAX_EXPENSES = "TaxExpenses";
	public static final String RETAINED_EARNINGS = "RetainedEarnings";
	public static final String PROFIT_LOSS = "ProfitLoss";
	private GLCategoryObject valObj;

	/*
	 * private Integer pkId; private String code; private String name; private
	 * String description; private String ledgerSide; private String realTemp;
	 * private String postToSection; private String options;
	 */
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

	public String getRealTemp()
	{
		return this.valObj.realTemp;
	}

	public String getPostToSection()
	{
		return this.valObj.postToSection;
	}

	public String getOptions()
	{
		return this.valObj.options;
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

	public void setRealTemp(String strRealTemp)
	{
		this.valObj.realTemp = strRealTemp;
	}

	public void setPostToSection(String strPostToSection)
	{
		this.valObj.ledgerSide = strPostToSection;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public GLCategoryObject getValueObject()
	{
		/*
		 * GLCategoryObject glco = new GLCategoryObject(); glco.pkId =
		 * this.pkId; glco.code = this.code; glco.name = this.name;
		 * glco.description = this.description; glco.ledgerSide =
		 * this.ledgerSide; glco.realTemp = this.realTemp; glco.postToSection =
		 * this.postToSection; glco.options = this.options; return glco;
		 */
		return this.valObj;
	}

	public void setValueObject(GLCategoryObject glco) throws Exception
	{
		/*
		 * if (glco == null) { throw new Exception("Object undefined"); }
		 * this.code = glco.code; this.name = glco.name; this.description =
		 * glco.description; this.ledgerSide = glco.ledgerSide; this.realTemp =
		 * glco.realTemp; this.postToSection = glco.postToSection; this.options =
		 * glco.options;
		 */
		this.valObj = glco;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, realTemp, postToSection, options);
		if (newPkId != null)
		{
			this.valObj = new GLCategoryObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.realTemp = realTemp;
			this.valObj.postToSection = postToSection;
			this.valObj.options = options;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		this.valObj = new GLCategoryObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
	}

	public void ejbPassivate()
	{
		this.valObj = null;
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

	/*
	 * public Vector ejbHomeGetValueObjectsTree(String field1, String value1,
	 * String field2, String value2, String field3, String value3) {
	 * Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
	 * Vector vecValObj = null; try { vecValObj = selectValueObjectsTree(field1,
	 * value1, field2, value2, field3, value3); } catch(Exception ex) {
	 * ex.printStackTrace(); } Log.printVerbose(strObjectName + " Leaving
	 * ejbHomeGetValueObjectsTree"); return vecValObj; }
	 */
	public Vector ejbHomeGetValueObjectsTree(QueryObject query)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsTree(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsTree");
		return vecValObj;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStmt = "INSERT INTO " + TABLENAME
					+ " (pkid, code, name, description, ledgerside, real_temp, postto_section , "
					+ " options) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setString(6, realTemp);
			ps.setString(7, postToSection);
			ps.setString(8, options);
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
			String sqlStmt = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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

	// ///////////////////////////////////////////////////////////
	// private Vector selectValueObjectsTree(String field1,String value1,
	// String field2,String value2,
	// String field3,String value3)
	private Vector selectValueObjectsTree(QueryObject query)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			// String sqlStmt = "SELECT pkid, code, name, description,
			// ledgerside, real_temp, postto_section, options FROM " + TABLENAME
			// + " ORDER BY postto_section, name ";
			String sqlStmt = "SELECT * FROM " + TABLENAME;
			sqlStmt = query.appendQuery(sqlStmt);
			String sqlStmt2 = "SELECT pkid, code, name, description, ledgerside, glcategoryid, code_old  FROM "
					+ GLCodeBean.TABLENAME + " WHERE glcategoryid = ?  ORDER BY code, name ";
			/*
			 * if(field1!=null && value1!=null) { sqlStmt += " AND " + field1 +
			 * "='"+value1+"' "; } if(field2!=null && value2!=null) { sqlStmt += "
			 * AND " + field2 + "='"+value2+"' "; } if(field3!=null &&
			 * value3!=null) { sqlStmt += " AND " + field3 + "='"+value3+"' "; }
			 */
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				GLCategoryObject glCatObj = new GLCategoryObject();
				glCatObj.pkId = new Integer(rs.getInt("pkid"));
				glCatObj.code = rs.getString("code");
				glCatObj.name = rs.getString("name");
				glCatObj.description = rs.getString("description");
				glCatObj.ledgerSide = rs.getString("ledgerside");
				glCatObj.realTemp = rs.getString("real_temp");
				glCatObj.postToSection = rs.getString("postto_section");
				glCatObj.options = rs.getString("options");
				PreparedStatement ps2 = null;
				ps2 = cn.prepareStatement(sqlStmt2);
				ps2.setInt(1, glCatObj.pkId.intValue());
				ResultSet rs2 = ps2.executeQuery();
//				rs2.beforeFirst();
				while (rs2.next())
				{
					GLCodeObject glCodeObj = new GLCodeObject();
					glCodeObj.pkId = new Integer(rs2.getInt("pkid"));
					glCodeObj.code = rs2.getString("code");
					glCodeObj.name = rs2.getString("name");
					glCodeObj.description = rs2.getString("description");
					glCodeObj.ledgerSide = rs2.getString("ledgerside");
					glCodeObj.glCategoryId = new Integer(rs2.getString("glcategoryid"));
					glCodeObj.codeOld = rs2.getString("code_old");
					glCatObj.vecGLCodes.add(glCodeObj);
				}
				ps2.close();
				vecValObj.add(glCatObj);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
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

	// //////////////////////////////////////////////////////////
	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "UPDATE " + TABLENAME + " SET code = ?, name = ?, description = ?, ledgerside = ?, "
					+ " real_temp = ?, postto_section = ?, options = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setString(5, this.valObj.realTemp);
			ps.setString(6, this.valObj.postToSection);
			ps.setString(7, this.valObj.options);
			ps.setInt(8, this.valObj.pkId.intValue());
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
			String sqlStmt = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " ORDER BY name";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
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
				GLCategoryObject glco = getObject(rs, "");
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

	private Collection selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Collection result = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
				result.add(glco);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return result;
	}

	public static GLCategoryObject getObject(ResultSet rs, String prefix)
	{
		GLCategoryObject theObj = null;
		try
		{
			theObj = new GLCategoryObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.realTemp = rs.getString("real_temp");
			theObj.postToSection = rs.getString("postto_section");
			theObj.options = rs.getString("options");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception ex)
		{
			ex.printStackTrace();
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

public class GLCategoryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcategory_index";
	protected final String strObjectName = "GLCategoryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String POST_TO_SECTION = "postto_section";
	public static final String LS_DEBIT = "dr";
	public static final String LS_CREDIT = "cr";
	public static final String RT_REAL = "real";
	public static final String RT_TEMP = "temp";
	// / GL Type / PostToSection
	public static final String COGS = "CostOfGoodsSold";
	public static final String COGS_EXPENSES = "CostOfGoodsSold-Expenses";
	public static final String COGS_GAINS = "CostOfGoodsSold-Gains";
	public static final String CURRENT_ASSETS = "CurrentAssets";
	public static final String CURRENT_LIABILITIES = "CurrentLiabilities";
	public static final String FIXED_ASSETS = "FixedAssets";
	public static final String FIXED_ASSETS_ACCUMULATED_DEPRECIATION = "FixedAssets-AccumulatedDepreciation";
	public static final String GENERAL_EXPENSES = "GeneralExpenses";
	public static final String LONG_TERM_LIABILITIES = "LongTermLiabilities";
	public static final String OTHER_ASSETS = "OtherAssets";
	public static final String OTHER_REVENUES = "OtherRevenues";
	public static final String SALES_EXPENSES = "SalesExpenses";
	public static final String SALES_REVENUE = "SalesRevenue";
	public static final String SHARE_CAPITAL = "ShareCapital";
	public static final String TAX_EXPENSES = "TaxExpenses";
	public static final String RETAINED_EARNINGS = "RetainedEarnings";
	public static final String PROFIT_LOSS = "ProfitLoss";
	private GLCategoryObject valObj;

	/*
	 * private Integer pkId; private String code; private String name; private
	 * String description; private String ledgerSide; private String realTemp;
	 * private String postToSection; private String options;
	 */
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

	public String getRealTemp()
	{
		return this.valObj.realTemp;
	}

	public String getPostToSection()
	{
		return this.valObj.postToSection;
	}

	public String getOptions()
	{
		return this.valObj.options;
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

	public void setRealTemp(String strRealTemp)
	{
		this.valObj.realTemp = strRealTemp;
	}

	public void setPostToSection(String strPostToSection)
	{
		this.valObj.ledgerSide = strPostToSection;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public GLCategoryObject getValueObject()
	{
		/*
		 * GLCategoryObject glco = new GLCategoryObject(); glco.pkId =
		 * this.pkId; glco.code = this.code; glco.name = this.name;
		 * glco.description = this.description; glco.ledgerSide =
		 * this.ledgerSide; glco.realTemp = this.realTemp; glco.postToSection =
		 * this.postToSection; glco.options = this.options; return glco;
		 */
		return this.valObj;
	}

	public void setValueObject(GLCategoryObject glco) throws Exception
	{
		/*
		 * if (glco == null) { throw new Exception("Object undefined"); }
		 * this.code = glco.code; this.name = glco.name; this.description =
		 * glco.description; this.ledgerSide = glco.ledgerSide; this.realTemp =
		 * glco.realTemp; this.postToSection = glco.postToSection; this.options =
		 * glco.options;
		 */
		this.valObj = glco;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, realTemp, postToSection, options);
		if (newPkId != null)
		{
			this.valObj = new GLCategoryObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.realTemp = realTemp;
			this.valObj.postToSection = postToSection;
			this.valObj.options = options;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		this.valObj = new GLCategoryObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
	}

	public void ejbPassivate()
	{
		this.valObj = null;
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

	/*
	 * public Vector ejbHomeGetValueObjectsTree(String field1, String value1,
	 * String field2, String value2, String field3, String value3) {
	 * Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
	 * Vector vecValObj = null; try { vecValObj = selectValueObjectsTree(field1,
	 * value1, field2, value2, field3, value3); } catch(Exception ex) {
	 * ex.printStackTrace(); } Log.printVerbose(strObjectName + " Leaving
	 * ejbHomeGetValueObjectsTree"); return vecValObj; }
	 */
	public Vector ejbHomeGetValueObjectsTree(QueryObject query)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsTree(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsTree");
		return vecValObj;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStmt = "INSERT INTO " + TABLENAME
					+ " (pkid, code, name, description, ledgerside, real_temp, postto_section , "
					+ " options) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setString(6, realTemp);
			ps.setString(7, postToSection);
			ps.setString(8, options);
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
			String sqlStmt = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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

	// ///////////////////////////////////////////////////////////
	// private Vector selectValueObjectsTree(String field1,String value1,
	// String field2,String value2,
	// String field3,String value3)
	private Vector selectValueObjectsTree(QueryObject query)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			// String sqlStmt = "SELECT pkid, code, name, description,
			// ledgerside, real_temp, postto_section, options FROM " + TABLENAME
			// + " ORDER BY postto_section, name ";
			String sqlStmt = "SELECT * FROM " + TABLENAME;
			sqlStmt = query.appendQuery(sqlStmt);
			String sqlStmt2 = "SELECT pkid, code, name, description, ledgerside, glcategoryid  FROM "
					+ GLCodeBean.TABLENAME + " WHERE glcategoryid = ?  ORDER BY code, name ";
			/*
			 * if(field1!=null && value1!=null) { sqlStmt += " AND " + field1 +
			 * "='"+value1+"' "; } if(field2!=null && value2!=null) { sqlStmt += "
			 * AND " + field2 + "='"+value2+"' "; } if(field3!=null &&
			 * value3!=null) { sqlStmt += " AND " + field3 + "='"+value3+"' "; }
			 */
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				GLCategoryObject glCatObj = new GLCategoryObject();
				glCatObj.pkId = new Integer(rs.getInt("pkid"));
				glCatObj.code = rs.getString("code");
				glCatObj.name = rs.getString("name");
				glCatObj.description = rs.getString("description");
				glCatObj.ledgerSide = rs.getString("ledgerside");
				glCatObj.realTemp = rs.getString("real_temp");
				glCatObj.postToSection = rs.getString("postto_section");
				glCatObj.options = rs.getString("options");
				PreparedStatement ps2 = null;
				ps2 = cn.prepareStatement(sqlStmt2);
				ps2.setInt(1, glCatObj.pkId.intValue());
				ResultSet rs2 = ps2.executeQuery();
//				rs2.beforeFirst();
				while (rs2.next())
				{
					GLCodeObject glCodeObj = new GLCodeObject();
					glCodeObj.pkId = new Integer(rs2.getInt("pkid"));
					glCodeObj.code = rs2.getString("code");
					glCodeObj.name = rs2.getString("name");
					glCodeObj.description = rs2.getString("description");
					glCodeObj.ledgerSide = rs2.getString("ledgerside");
					glCodeObj.glCategoryId = new Integer(rs2.getString("glcategoryid"));
					glCatObj.vecGLCodes.add(glCodeObj);
				}
				ps2.close();
				vecValObj.add(glCatObj);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
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

	// //////////////////////////////////////////////////////////
	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "UPDATE " + TABLENAME + " SET code = ?, name = ?, description = ?, ledgerside = ?, "
					+ " real_temp = ?, postto_section = ?, options = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setString(5, this.valObj.realTemp);
			ps.setString(6, this.valObj.postToSection);
			ps.setString(7, this.valObj.options);
			ps.setInt(8, this.valObj.pkId.intValue());
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
			String sqlStmt = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " ORDER BY name";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
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
				GLCategoryObject glco = getObject(rs, "");
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

	private Collection selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Collection result = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
				result.add(glco);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return result;
	}

	public static GLCategoryObject getObject(ResultSet rs, String prefix)
	{
		GLCategoryObject theObj = null;
		try
		{
			theObj = new GLCategoryObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.realTemp = rs.getString("real_temp");
			theObj.postToSection = rs.getString("postto_section");
			theObj.options = rs.getString("options");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception ex)
		{
			ex.printStackTrace();
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

public class GLCategoryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcategory_index";
	protected final String strObjectName = "GLCategoryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String POST_TO_SECTION = "postto_section";
	public static final String LS_DEBIT = "dr";
	public static final String LS_CREDIT = "cr";
	public static final String RT_REAL = "real";
	public static final String RT_TEMP = "temp";
	// / GL Type / PostToSection
	public static final String COGS = "CostOfGoodsSold";
	public static final String COGS_EXPENSES = "CostOfGoodsSold-Expenses";
	public static final String COGS_GAINS = "CostOfGoodsSold-Gains";
	public static final String CURRENT_ASSETS = "CurrentAssets";
	public static final String CURRENT_LIABILITIES = "CurrentLiabilities";
	public static final String FIXED_ASSETS = "FixedAssets";
	public static final String FIXED_ASSETS_ACCUMULATED_DEPRECIATION = "FixedAssets-AccumulatedDepreciation";
	public static final String GENERAL_EXPENSES = "GeneralExpenses";
	public static final String LONG_TERM_LIABILITIES = "LongTermLiabilities";
	public static final String OTHER_ASSETS = "OtherAssets";
	public static final String OTHER_REVENUES = "OtherRevenues";
	public static final String SALES_EXPENSES = "SalesExpenses";
	public static final String SALES_REVENUE = "SalesRevenue";
	public static final String SHARE_CAPITAL = "ShareCapital";
	public static final String TAX_EXPENSES = "TaxExpenses";
	public static final String RETAINED_EARNINGS = "RetainedEarnings";
	public static final String PROFIT_LOSS = "ProfitLoss";
	private GLCategoryObject valObj;

	/*
	 * private Integer pkId; private String code; private String name; private
	 * String description; private String ledgerSide; private String realTemp;
	 * private String postToSection; private String options;
	 */
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

	public String getRealTemp()
	{
		return this.valObj.realTemp;
	}

	public String getPostToSection()
	{
		return this.valObj.postToSection;
	}

	public String getOptions()
	{
		return this.valObj.options;
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

	public void setRealTemp(String strRealTemp)
	{
		this.valObj.realTemp = strRealTemp;
	}

	public void setPostToSection(String strPostToSection)
	{
		this.valObj.ledgerSide = strPostToSection;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public GLCategoryObject getValueObject()
	{
		/*
		 * GLCategoryObject glco = new GLCategoryObject(); glco.pkId =
		 * this.pkId; glco.code = this.code; glco.name = this.name;
		 * glco.description = this.description; glco.ledgerSide =
		 * this.ledgerSide; glco.realTemp = this.realTemp; glco.postToSection =
		 * this.postToSection; glco.options = this.options; return glco;
		 */
		return this.valObj;
	}

	public void setValueObject(GLCategoryObject glco) throws Exception
	{
		/*
		 * if (glco == null) { throw new Exception("Object undefined"); }
		 * this.code = glco.code; this.name = glco.name; this.description =
		 * glco.description; this.ledgerSide = glco.ledgerSide; this.realTemp =
		 * glco.realTemp; this.postToSection = glco.postToSection; this.options =
		 * glco.options;
		 */
		this.valObj = glco;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, realTemp, postToSection, options);
		if (newPkId != null)
		{
			this.valObj = new GLCategoryObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.realTemp = realTemp;
			this.valObj.postToSection = postToSection;
			this.valObj.options = options;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		this.valObj = new GLCategoryObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
	}

	public void ejbPassivate()
	{
		this.valObj = null;
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

	/*
	 * public Vector ejbHomeGetValueObjectsTree(String field1, String value1,
	 * String field2, String value2, String field3, String value3) {
	 * Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
	 * Vector vecValObj = null; try { vecValObj = selectValueObjectsTree(field1,
	 * value1, field2, value2, field3, value3); } catch(Exception ex) {
	 * ex.printStackTrace(); } Log.printVerbose(strObjectName + " Leaving
	 * ejbHomeGetValueObjectsTree"); return vecValObj; }
	 */
	public Vector ejbHomeGetValueObjectsTree(QueryObject query)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsTree(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsTree");
		return vecValObj;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStmt = "INSERT INTO " + TABLENAME
					+ " (pkid, code, name, description, ledgerside, real_temp, postto_section , "
					+ " options) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setString(6, realTemp);
			ps.setString(7, postToSection);
			ps.setString(8, options);
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
			String sqlStmt = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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

	// ///////////////////////////////////////////////////////////
	// private Vector selectValueObjectsTree(String field1,String value1,
	// String field2,String value2,
	// String field3,String value3)
	private Vector selectValueObjectsTree(QueryObject query)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			// String sqlStmt = "SELECT pkid, code, name, description,
			// ledgerside, real_temp, postto_section, options FROM " + TABLENAME
			// + " ORDER BY postto_section, name ";
			String sqlStmt = "SELECT * FROM " + TABLENAME;
			sqlStmt = query.appendQuery(sqlStmt);
			String sqlStmt2 = "SELECT pkid, code, name, description, ledgerside, glcategoryid  FROM "
					+ GLCodeBean.TABLENAME + " WHERE glcategoryid = ?  ORDER BY code, name ";
			/*
			 * if(field1!=null && value1!=null) { sqlStmt += " AND " + field1 +
			 * "='"+value1+"' "; } if(field2!=null && value2!=null) { sqlStmt += "
			 * AND " + field2 + "='"+value2+"' "; } if(field3!=null &&
			 * value3!=null) { sqlStmt += " AND " + field3 + "='"+value3+"' "; }
			 */
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				GLCategoryObject glCatObj = new GLCategoryObject();
				glCatObj.pkId = new Integer(rs.getInt("pkid"));
				glCatObj.code = rs.getString("code");
				glCatObj.name = rs.getString("name");
				glCatObj.description = rs.getString("description");
				glCatObj.ledgerSide = rs.getString("ledgerside");
				glCatObj.realTemp = rs.getString("real_temp");
				glCatObj.postToSection = rs.getString("postto_section");
				glCatObj.options = rs.getString("options");
				PreparedStatement ps2 = null;
				ps2 = cn.prepareStatement(sqlStmt2);
				ps2.setInt(1, glCatObj.pkId.intValue());
				ResultSet rs2 = ps2.executeQuery();
				rs2.beforeFirst();
				while (rs2.next())
				{
					GLCodeObject glCodeObj = new GLCodeObject();
					glCodeObj.pkId = new Integer(rs2.getInt("pkid"));
					glCodeObj.code = rs2.getString("code");
					glCodeObj.name = rs2.getString("name");
					glCodeObj.description = rs2.getString("description");
					glCodeObj.ledgerSide = rs2.getString("ledgerside");
					glCodeObj.glCategoryId = new Integer(rs2.getString("glcategoryid"));
					glCatObj.vecGLCodes.add(glCodeObj);
				}
				ps2.close();
				vecValObj.add(glCatObj);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
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

	// //////////////////////////////////////////////////////////
	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "UPDATE " + TABLENAME + " SET code = ?, name = ?, description = ?, ledgerside = ?, "
					+ " real_temp = ?, postto_section = ?, options = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setString(5, this.valObj.realTemp);
			ps.setString(6, this.valObj.postToSection);
			ps.setString(7, this.valObj.options);
			ps.setInt(8, this.valObj.pkId.intValue());
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
			String sqlStmt = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " ORDER BY name";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
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
				GLCategoryObject glco = getObject(rs, "");
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

	private Collection selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Collection result = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
				result.add(glco);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return result;
	}

	public static GLCategoryObject getObject(ResultSet rs, String prefix)
	{
		GLCategoryObject theObj = null;
		try
		{
			theObj = new GLCategoryObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.realTemp = rs.getString("real_temp");
			theObj.postToSection = rs.getString("postto_section");
			theObj.options = rs.getString("options");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception ex)
		{
			ex.printStackTrace();
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

public class GLCategoryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_glcategory_index";
	protected final String strObjectName = "GLCategoryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String POST_TO_SECTION = "postto_section";
	public static final String LS_DEBIT = "dr";
	public static final String LS_CREDIT = "cr";
	public static final String RT_REAL = "real";
	public static final String RT_TEMP = "temp";
	// / GL Type / PostToSection
	public static final String COGS = "CostOfGoodsSold";
	public static final String COGS_EXPENSES = "CostOfGoodsSold-Expenses";
	public static final String COGS_GAINS = "CostOfGoodsSold-Gains";
	public static final String CURRENT_ASSETS = "CurrentAssets";
	public static final String CURRENT_LIABILITIES = "CurrentLiabilities";
	public static final String FIXED_ASSETS = "FixedAssets";
	public static final String FIXED_ASSETS_ACCUMULATED_DEPRECIATION = "FixedAssets-AccumulatedDepreciation";
	public static final String GENERAL_EXPENSES = "GeneralExpenses";
	public static final String LONG_TERM_LIABILITIES = "LongTermLiabilities";
	public static final String OTHER_ASSETS = "OtherAssets";
	public static final String OTHER_REVENUES = "OtherRevenues";
	public static final String SALES_EXPENSES = "SalesExpenses";
	public static final String SALES_REVENUE = "SalesRevenue";
	public static final String SHARE_CAPITAL = "ShareCapital";
	public static final String TAX_EXPENSES = "TaxExpenses";
	public static final String RETAINED_EARNINGS = "RetainedEarnings";
	public static final String PROFIT_LOSS = "ProfitLoss";
	private GLCategoryObject valObj;

	/*
	 * private Integer pkId; private String code; private String name; private
	 * String description; private String ledgerSide; private String realTemp;
	 * private String postToSection; private String options;
	 */
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

	public String getRealTemp()
	{
		return this.valObj.realTemp;
	}

	public String getPostToSection()
	{
		return this.valObj.postToSection;
	}

	public String getOptions()
	{
		return this.valObj.options;
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

	public void setRealTemp(String strRealTemp)
	{
		this.valObj.realTemp = strRealTemp;
	}

	public void setPostToSection(String strPostToSection)
	{
		this.valObj.ledgerSide = strPostToSection;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public GLCategoryObject getValueObject()
	{
		/*
		 * GLCategoryObject glco = new GLCategoryObject(); glco.pkId =
		 * this.pkId; glco.code = this.code; glco.name = this.name;
		 * glco.description = this.description; glco.ledgerSide =
		 * this.ledgerSide; glco.realTemp = this.realTemp; glco.postToSection =
		 * this.postToSection; glco.options = this.options; return glco;
		 */
		return this.valObj;
	}

	public void setValueObject(GLCategoryObject glco) throws Exception
	{
		/*
		 * if (glco == null) { throw new Exception("Object undefined"); }
		 * this.code = glco.code; this.name = glco.name; this.description =
		 * glco.description; this.ledgerSide = glco.ledgerSide; this.realTemp =
		 * glco.realTemp; this.postToSection = glco.postToSection; this.options =
		 * glco.options;
		 */
		this.valObj = glco;
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

	public Integer ejbCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(code, name, description, ledgerSide, realTemp, postToSection, options);
		if (newPkId != null)
		{
			this.valObj = new GLCategoryObject();
			this.valObj.pkId = newPkId;
			this.valObj.code = code;
			this.valObj.name = name;
			this.valObj.description = description;
			this.valObj.ledgerSide = ledgerSide;
			this.valObj.realTemp = realTemp;
			this.valObj.postToSection = postToSection;
			this.valObj.options = options;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.valObj.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		this.valObj = new GLCategoryObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
	}

	public void ejbPassivate()
	{
		this.valObj = null;
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

	/*
	 * public Vector ejbHomeGetValueObjectsTree(String field1, String value1,
	 * String field2, String value2, String field3, String value3) {
	 * Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
	 * Vector vecValObj = null; try { vecValObj = selectValueObjectsTree(field1,
	 * value1, field2, value2, field3, value3); } catch(Exception ex) {
	 * ex.printStackTrace(); } Log.printVerbose(strObjectName + " Leaving
	 * ejbHomeGetValueObjectsTree"); return vecValObj; }
	 */
	public Vector ejbHomeGetValueObjectsTree(QueryObject query)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsTree");
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsTree(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsTree");
		return vecValObj;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
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

	private Integer insertNewRow(String code, String name, String description, String ledgerSide, String realTemp,
			String postToSection, String options)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStmt = "INSERT INTO " + TABLENAME
					+ " (pkid, code, name, description, ledgerside, real_temp, postto_section , "
					+ " options) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, code);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, ledgerSide);
			ps.setString(6, realTemp);
			ps.setString(7, postToSection);
			ps.setString(8, options);
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
			String sqlStmt = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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

	// ///////////////////////////////////////////////////////////
	// private Vector selectValueObjectsTree(String field1,String value1,
	// String field2,String value2,
	// String field3,String value3)
	private Vector selectValueObjectsTree(QueryObject query)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			// String sqlStmt = "SELECT pkid, code, name, description,
			// ledgerside, real_temp, postto_section, options FROM " + TABLENAME
			// + " ORDER BY postto_section, name ";
			String sqlStmt = "SELECT * FROM " + TABLENAME;
			sqlStmt = query.appendQuery(sqlStmt);
			String sqlStmt2 = "SELECT pkid, code, name, description, ledgerside, glcategoryid  FROM "
					+ GLCodeBean.TABLENAME + " WHERE glcategoryid = ?  ORDER BY code, name ";
			/*
			 * if(field1!=null && value1!=null) { sqlStmt += " AND " + field1 +
			 * "='"+value1+"' "; } if(field2!=null && value2!=null) { sqlStmt += "
			 * AND " + field2 + "='"+value2+"' "; } if(field3!=null &&
			 * value3!=null) { sqlStmt += " AND " + field3 + "='"+value3+"' "; }
			 */
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				GLCategoryObject glCatObj = new GLCategoryObject();
				glCatObj.pkId = new Integer(rs.getInt("pkid"));
				glCatObj.code = rs.getString("code");
				glCatObj.name = rs.getString("name");
				glCatObj.description = rs.getString("description");
				glCatObj.ledgerSide = rs.getString("ledgerside");
				glCatObj.realTemp = rs.getString("real_temp");
				glCatObj.postToSection = rs.getString("postto_section");
				glCatObj.options = rs.getString("options");
				PreparedStatement ps2 = null;
				ps2 = cn.prepareStatement(sqlStmt2);
				ps2.setInt(1, glCatObj.pkId.intValue());
				ResultSet rs2 = ps2.executeQuery();
//				rs2.beforeFirst();
				while (rs2.next())
				{
					GLCodeObject glCodeObj = new GLCodeObject();
					glCodeObj.pkId = new Integer(rs2.getInt("pkid"));
					glCodeObj.code = rs2.getString("code");
					glCodeObj.name = rs2.getString("name");
					glCodeObj.description = rs2.getString("description");
					glCodeObj.ledgerSide = rs2.getString("ledgerside");
					glCodeObj.glCategoryId = new Integer(rs2.getString("glcategoryid"));
					glCatObj.vecGLCodes.add(glCodeObj);
				}
				ps2.close();
				vecValObj.add(glCatObj);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setInt(1, this.valObj.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
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

	// //////////////////////////////////////////////////////////
	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStmt = "UPDATE " + TABLENAME + " SET code = ?, name = ?, description = ?, ledgerside = ?, "
					+ " real_temp = ?, postto_section = ?, options = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
			ps.setString(1, this.valObj.code);
			ps.setString(2, this.valObj.name);
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.ledgerSide);
			ps.setString(5, this.valObj.realTemp);
			ps.setString(6, this.valObj.postToSection);
			ps.setString(7, this.valObj.options);
			ps.setInt(8, this.valObj.pkId.intValue());
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
			String sqlStmt = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStmt = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStmt);
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
					+ TABLENAME + " ORDER BY name";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
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
			String sqlStatement = "SELECT pkid, code, name, description, ledgerside, real_temp, postto_section, options FROM "
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
				GLCategoryObject glco = getObject(rs, "");
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

	private Collection selectObjects(QueryObject query)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Collection result = new Vector();
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLCategoryObject glco = getObject(rs, "");
				result.add(glco);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return result;
	}

	public static GLCategoryObject getObject(ResultSet rs, String prefix)
	{
		GLCategoryObject theObj = null;
		try
		{
			theObj = new GLCategoryObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.code = rs.getString("code");
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.ledgerSide = rs.getString("ledgerside");
			theObj.realTemp = rs.getString("real_temp");
			theObj.postToSection = rs.getString("postto_section");
			theObj.options = rs.getString("options");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return theObj;
	}
}
