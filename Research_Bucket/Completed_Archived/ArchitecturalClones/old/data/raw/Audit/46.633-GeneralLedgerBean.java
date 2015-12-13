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
import com.vlee.bean.accounting.*;

public class GeneralLedgerBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_index";
	protected final String strObjectName = "GeneralLedgerBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private GeneralLedgerObject valObj;
	public static final String PKID = "pkid";
	public static final String GLCODEID = "glcodeid";
	public static final String PCCENTERID = "pccenterid";
	public static final String BATCHID = "batchid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	public static final String OPTIONS = "options";
	public static final String CURRENCY = "currency";

	/*
	 * private Integer pkId; private Integer glCodeId; private Integer
	 * pcCenterId; private Integer batchId; private String name; private String
	 * description; private String status; private String options; private
	 * String currency;
	 */
	public GeneralLedgerObject getObject()
	{
		return this.valObj;
	}

	public void setObject(GeneralLedgerObject newVal)
	{
		Integer pkid = this.valObj.pkId;
		this.valObj = newVal;
		this.valObj.pkId = pkid;
	}

	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public Integer getGLCodeId()
	{
		return this.valObj.glCodeId;
	}

	public Integer getPCCenterId()
	{
		return this.valObj.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.valObj.batchId;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public String getOptions()
	{
		return this.valObj.options;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setPkId(Integer pkid)
	{
		this.valObj.pkId = pkid;
	}

	public void setGLCodeId(Integer iGLCodeId)
	{
		this.valObj.glCodeId = iGLCodeId;
	}

	public void setPCCenterId(Integer iPCCenterId)
	{
		this.valObj.pcCenterId = iPCCenterId;
	}

	public void setBatchId(Integer batchid)
	{
		this.valObj.batchId = batchid;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public GeneralLedgerObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GeneralLedgerObject glo) throws Exception
	{
		setObject(glo);
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

	// public Integer ejbCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId,
	// String name, String description, String options, String currency)
	// public void ejbPostCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId, String name, String description, String options, String
	// currency) {
	// }
	public Integer ejbCreate(GeneralLedgerObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = newObj;
			this.valObj.pkId = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkId;
	}

	public void ejbPostCreate(GeneralLedgerObject newObj)
	{
	}

	public void ejbRemove()
	{
		deleteObject(this.valObj.pkId);
	}

	public void ejbActivate()
	{
		this.valObj = new GeneralLedgerObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		this.valObj.pkId = null;
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

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
			throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId, currency);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
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

	public Vector ejbHomeGetDistinctPCCenterAndBatch()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetDistinctPCCenterAndBatch");
		Vector vecValObj = selectDistinctPCCenterAndBatch();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetDistinctPCCenterAndBatch");
		return vecValObj;
	}

	public boolean ejbHomeRetainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeRetainedEarningsExist");
		boolean exist = retainedEarningsExist(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeRetainedEarningsExist");
		return exist;
	}

	public Vector ejbHomeGetGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
	{
		Vector result = new Vector();
		try
		{
			result = selectGLTree(batchId, pcCenter, glCategoryId, glCodeId);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public Vector ejbHomeGetGLView(Vector arrPkid, Timestamp dateStartFY, Timestamp dateStart, Timestamp dateEnd)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectGLView(arrPkid, dateStartFY, dateStart, dateEnd);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public Vector ejbHomeGetTrialBalance(Integer pccenter, Integer batch, Timestamp theDate)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectTrialBalance(pccenter, batch, theDate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}


   public Vector ejbHomeGetYTDTrialBalance(Integer pccenter, Integer batch, Timestamp dateFrom, Timestamp dateTo)
   {
	   Log.printVerbose(strObjectName + " In ejbHomeGetYTDTrialBalance");
      Vector vecResult = new Vector();
      try
      {
         vecResult = selectYTDTrialBalance(pccenter, batch, dateFrom, dateTo);
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }
      Log.printVerbose(strObjectName + " Leaving ejbHomeGetYTDTrialBalance");
      return vecResult;
      
   }



	public GLSummaryObject ejbHomeGetSummary(GLSummaryObject glSumObj)
	{
		try
		{
			glSumObj = selectSummary(glSumObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return glSumObj;
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

	// private Integer insertNewRow(Integer glCodeId, Integer pcCenterId,
	// Integer batchId, String name, String description, String options, String
	// currency)
	private Integer insertObject(GeneralLedgerObject newObj)
	{
		Log.printVerbose(strObjectName + " In insertObject");
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			newObj.pkId = newPkId;
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, newObj.glCodeId.intValue());
			ps.setInt(3, newObj.pcCenterId.intValue());
			ps.setInt(4, newObj.batchId.intValue());
			ps.setString(5, newObj.name);
			ps.setString(6, newObj.description);
			ps.setString(7, GeneralLedgerBean.ACTIVE);
			ps.setString(8, newObj.options);
			ps.setString(9, newObj.currency);
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Created New Row:" + newPkId.toString());
			Log.printVerbose(strObjectName + " Leaving insertObject");
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
		Log.printVerbose(strObjectName + " In getNextPKId");
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
			Log.printVerbose(strObjectName + " Leaving getNextPKId");
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
		Log.printVerbose(strObjectName + " In loadObject");
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
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
		Log.printVerbose(strObjectName + " Leaving loadObject");
	}

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE " + TABLENAME + " SET glcodeid = ?, pccenterid = ?, batchid = ?, "
					+ " name = ?, description = ?, status = ?, options = ?, currency = ?" + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.glCodeId.intValue());
			ps.setInt(2, this.valObj.pcCenterId.intValue());
			ps.setInt(3, this.valObj.batchId.intValue());
			ps.setString(4, this.valObj.name);
			ps.setString(5, this.valObj.description);
			ps.setString(6, this.valObj.status);
			ps.setString(7, this.valObj.options);
			ps.setString(8, this.valObj.currency);
			ps.setInt(9, this.valObj.pkId.intValue());
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

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId)
	{
		return selectUnique(batchId, pcCenter, glCodeId, (String) null);
	}

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
	{
		Integer pkid = null;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE batchid = '" + batchId.toString()
					+ "' AND pccenterid = '" + pcCenter.toString() + "' AND glcodeid = '" + glCodeId.toString() + "' ";
			if (currency != null)
			{
				sqlStatement += " AND currency = '" + currency + "' ";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				pkid = new Integer(rs.getInt("pkid"));
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
		return pkid;
	}
	
	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY name";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ? ORDER BY name";
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
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? ORDER BY name";
			} else
			{
				sqlStatement = sqlStatement + " ORDER BY name";
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
				GeneralLedgerObject glo = getObject(rs, "");
				vecValObj.add(glo);
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

	private Vector selectTrialBalance(Integer pcCenter, Integer batch, Timestamp theDate)
	{
		Log.printVerbose(strObjectName + " In selectTrialBalance");
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		Timestamp dayAfter = TimeFormat.add(theDate, 0, 0, 1);
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS balance FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE jtxn.pccenterid='"
					+ pcCenter.toString()
					+ "' "
					+ " AND jtxn.batchid='"
					+ batch.toString()
					+ "' AND "
					+ "jtxn.transactiondate<'"
					+ TimeFormat.strDisplayDate(dayAfter)
					+ "' "
					+ "AND jtxn.typeid='"
					+ JournalTransactionBean.TYPEID_POSTED.toString()
					+ "' "
					+ " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON (tbl2.glcodeid = code.pkid)) AS tbl3 INNER JOIN acc_glcategory_index AS cat ON (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				TrialBalanceSession.Row theRow = new TrialBalanceSession.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				vecValObj.add(theRow);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		Log.printVerbose(strObjectName + " Leaving selectTrialBalance");
		return vecValObj;
	}


	private Vector selectYTDTrialBalance(Integer pcCenter, Integer batch, Timestamp dateFrom, Timestamp dateTo)
	{
		Log.printVerbose(strObjectName + " In selectYTDTrialBalance");
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psTo = null;
		Vector vecValObj = new Vector();
		Timestamp dateStart = TimeFormat.createTimestamp(TimeFormat.strDisplayDate(dateFrom));
		Timestamp dayAfter = TimeFormat.add(dateTo, 0, 0, 1);
		try
		{
			String sqlStatementTo = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS balance FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE jtxn.pccenterid='" + pcCenter.toString() + "' "
					+ " AND jtxn.batchid='" + batch.toString() + "' "
					+ " AND jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dayAfter) + "' "
					+ " AND jtxn.typeid='" + JournalTransactionBean.TYPEID_POSTED.toString() + "' "
					+ " AND jtxn.txncode !='" + JournalTransactionBean.TXNCODE_PROFIT + "' "
					+ " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON (tbl2.glcodeid = code.pkid)) AS tbl3 INNER JOIN acc_glcategory_index AS cat ON (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			cn = ds.getConnection();
			psTo = cn.prepareStatement(sqlStatementTo);
			ResultSet rsTo = psTo.executeQuery();
			while (rsTo.next())
			{
				YTDTrialBalanceForm.Row theRow = new YTDTrialBalanceForm.Row();
				theRow.glCategoryId = new Integer(rsTo.getInt("glcategoryid"));
				theRow.postSection = rsTo.getString("postto_section");
				theRow.realTemp = rsTo.getString("real_temp");
				theRow.glCodeId = new Integer(rsTo.getInt("glcodeid"));
				theRow.glCode = rsTo.getString("code");
				theRow.glCodeName = rsTo.getString("gl_name");
				theRow.balance = rsTo.getBigDecimal("balance");
				theRow.genLedgerId = new Integer(rsTo.getInt("glaccid"));
				vecValObj.add(theRow);
			}

         String sqlStatementStart = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
               + " code.code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
               + " (SELECT jent.glaccid, sum(jent.amount) AS balance FROM acc_journal_transaction AS jtxn INNER JOIN "
               + " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE jtxn.pccenterid='" + pcCenter.toString() + "' "
               + " AND jtxn.batchid='" + batch.toString() + "' "
               + " AND jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dateFrom) + "' "
               + " AND jtxn.typeid='" + JournalTransactionBean.TYPEID_POSTED.toString() + "' "
               + " AND jtxn.txncode !='" + JournalTransactionBean.TXNCODE_PROFIT + "' "
               + " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON (tbl2.glcodeid = code.pkid)) AS tbl3 INNER JOIN acc_glcategory_index AS cat ON (tbl3.glcategoryid = cat.pkid) WHERE cat.real_temp = '"+GLCategoryBean.RT_TEMP+"' ORDER BY cat.postto_section, tbl3.code;";
         //cn = ds.getConnection();
         psTo = cn.prepareStatement(sqlStatementStart);
         ResultSet rsStart= psTo.executeQuery();
         while (rsStart.next())
         {
            YTDTrialBalanceForm.Row theRow = new YTDTrialBalanceForm.Row();
            theRow.glCategoryId = new Integer(rsStart.getInt("glcategoryid"));
            theRow.postSection = rsStart.getString("postto_section");
            theRow.realTemp = rsStart.getString("real_temp");
            theRow.glCodeId = new Integer(rsStart.getInt("glcodeid"));
            theRow.glCode = rsStart.getString("code");
            theRow.glCodeName = rsStart.getString("gl_name");
            theRow.balance = rsStart.getBigDecimal("balance");
            theRow.genLedgerId = new Integer(rsStart.getInt("glaccid"));

				//// if balance !=null and realTemp = temp
				if(theRow.balance!=null && theRow.genLedgerId!=null)
				{
					for(int cnt1=0; cnt1 < vecValObj.size(); cnt1++)
					{
						YTDTrialBalanceForm.Row oneRow = (YTDTrialBalanceForm.Row) vecValObj.get(cnt1);
						if(theRow.genLedgerId.equals(oneRow.genLedgerId))
						{
							oneRow.balance = oneRow.balance.subtract(theRow.balance);
						}
					}
				}

         }

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psTo);
		}
		Log.printVerbose(strObjectName + " Leaving selectYTDTrialBalance");
		return vecValObj;
	}




	private GLSummaryObject selectSummary(GLSummaryObject glSumObj)
	{
		Log.printVerbose(strObjectName + " In selectSummary");
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Timestamp dayAfter = TimeFormat.add(glSumObj.dateTo, 0, 0, 1);
		glSumObj.vecRow.clear();
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code AS code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS sum_amt FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE " + " jtxn.pccenterid='"
					+ glSumObj.pcCenter.toString() + "' " + " AND jtxn.batchid='" + glSumObj.batch.toString() + "' "
					+ " AND jtxn.transactiondate>='" + TimeFormat.strDisplayDate(glSumObj.dateFrom) + "' "
					+ " AND jtxn.transactiondate<'" + TimeFormat.strDisplayDate(dayAfter) + "' "
					+ " AND jtxn.typeid ='" + glSumObj.jTypeId.toString() + "' ";
			if (glSumObj.jTxnCode != null)
			{
				sqlStatement += " AND jtxn.txncode = '" + glSumObj.jTxnCode + "' ";
			}
			sqlStatement += " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON "
					+ " (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON "
					+ " (tbl2.glcodeid = code.pkid)";
			if (glSumObj.glCode != null)
			{
				sqlStatement += " WHERE code.code = '" + glSumObj.glCode + "' ";
			}
			sqlStatement += ") AS tbl3 INNER JOIN acc_glcategory_index AS cat ON "
					+ " (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLSummaryObject.Row theRow = new GLSummaryObject.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.amount = rs.getBigDecimal("sum_amt");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				glSumObj.vecRow.add(theRow);
				// vecValObj.add(theRow);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		Log.printVerbose(strObjectName + " Leaving selectSummary");
		return glSumObj;
	}

	private boolean retainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		boolean exist = false;
		try
		{
			String sqlStatement = "SELECT "
					+ TABLENAME
					+ ".pkid FROM "
					+ TABLENAME
					+ ", acc_glcode_index, acc_glcategory_index WHERE "
					+ TABLENAME
					+ ".glcodeid = acc_glcode_index.pkid AND acc_glcode_index.glcategoryid = acc_glcategory_index.pkid AND "
					+ "acc_glcategory_index.postto_section = 'RetainedEarnings' AND " + TABLENAME + "." + fieldName1
					+ " = ? AND " + TABLENAME + "." + fieldName2 + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, value2);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				exist = true;
			} else
			{
				exist = false;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return exist;
	}

	private Vector selectDistinctPCCenterAndBatch()
	{
		Log.printVerbose(strObjectName + " In selectDistinctPCCenterAndBatch");
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT DISTINCT pccenterid, batchid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerObject glo = new GeneralLedgerObject();
				glo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				glo.batchId = new Integer(rs.getInt("batchid"));
				vecValObj.add(glo);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		Log.printVerbose(strObjectName + " Leaving selectDistinctPCCenterAndBatch");
		return vecValObj;
	}

	public Vector selectGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
			throws SQLException, Exception
	{
		Log.printVerbose(strObjectName + " In selectGLTree");
		Vector vecGLTree = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLCode = null;
		PreparedStatement psGLCat = null;
		try
		{
			// String sqlGL = " SELECT * FROM " + TABLENAME ;
			String sqlGL = " SELECT gl2.*, cat.postto_section FROM "
					+ " (SELECT gl.*,code.glcategoryid AS catid, code.code AS glcode "
					+ "	 FROM acc_general_ledger_index "
					+ " AS gl INNER JOIN acc_glcode_index AS code ON (gl.glcodeid = code.pkid)  "
					+ " WHERE gl.pccenterid='" + pcCenter.toString() + "' " + " ) "
					+ " AS gl2 INNER JOIN acc_glcategory_index AS cat ON (gl2.catid = cat.pkid) "
					+ " ORDER BY cat.postto_section, gl2.glcode ";
			cn = ds.getConnection();
			psGL = cn.prepareStatement(sqlGL);
			ResultSet rsGL = psGL.executeQuery();
			while (rsGL.next())
			{
				// / processing glcode
				Integer ledgerGLCode = new Integer(rsGL.getInt(GLCODEID));
				if (glCodeId != null && !ledgerGLCode.equals(glCodeId))
				{
					continue;
				}
				String sqlGLCode = " SELECT * FROM " + GLCodeBean.TABLENAME + " WHERE " + GLCodeBean.PKID + " ='"
						+ ledgerGLCode.toString() + "' ";
				psGLCode = cn.prepareStatement(sqlGLCode);
				ResultSet rsGLCode = psGLCode.executeQuery();
				if (!rsGLCode.next())
				{
					continue;
				}
				GLCodeObject glCodeObj = GLCodeBean.getObject(rsGLCode, "");
				// / processing gl-category
				if (glCategoryId != null && !glCodeObj.glCategoryId.equals(glCategoryId))
				{
					continue;
				}
				String sqlGLCat = " SELECT * FROM " + GLCategoryBean.TABLENAME + " WHERE " + GLCategoryBean.PKID
						+ " ='" + glCodeObj.glCategoryId.toString() + "' ";
				psGLCat = cn.prepareStatement(sqlGLCat);
				ResultSet rsGLCat = psGLCat.executeQuery();
				if (!rsGLCat.next())
				{
					continue;
				}
				GLCategoryObject glCategoryObj = GLCategoryBean.getObject(rsGLCat, "");
				GeneralLedgerObject glObj = getObject(rsGL, "");
				GeneralLedgerTreeObject glTreeObj = new GeneralLedgerTreeObject(glObj);
				glTreeObj.glCodeObj = glCodeObj;
				glTreeObj.glCategoryObj = glCategoryObj;
				vecGLTree.add(glTreeObj);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLCode);
			cleanup(cn, psGLCat);
		}
		Log.printVerbose(strObjectName + " Leaving selectGLTree");
		return vecGLTree;
	}

	public static GeneralLedgerObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GeneralLedgerObject theObj = null;
		try
		{
			theObj = new GeneralLedgerObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.glCodeId = new Integer(rs.getInt("glcodeid"));
			theObj.pcCenterId = new Integer(rs.getInt("pccenterid"));
			theObj.batchId = new Integer(rs.getInt("batchid"));
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.status = rs.getString("status");
			theObj.options = rs.getString("options");
			theObj.currency = rs.getString("currency");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	public Vector selectGLView(Vector arrPkid, Timestamp dateStartFY, Timestamp dateStart, Timestamp dateEnd) throws SQLException, Exception
	{
		Log.printVerbose(strObjectName + " In selectGLView");
		Timestamp dateAfterEnd = TimeFormat.add(dateEnd, 0, 0, 1);
		Vector vecGLView = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLOpenBal = null;
		PreparedStatement psGLTransferredAmt  = null;
		PreparedStatement psGLEntry = null;
		try
		{
			cn = ds.getConnection();
			for (int cnt1 = 0; cnt1 < arrPkid.size(); cnt1++)
			{
				Integer thePkid = (Integer) arrPkid.get(cnt1);
				String sqlGL = " SELECT cat.pkid AS cat_pkid, cat.real_temp AS cat_rt, cat.postto_section AS cat_pts , "
						+ " codegl.* FROM (SELECT code.pkid AS code_pkid, code.code AS code_code, code.glcategoryid "
						+ " AS code_glcatid, gl.* FROM (SELECT * FROM acc_general_ledger_index WHERE pkid  = '"
						+ thePkid.toString()
						+ "' ) "
						+ " AS gl INNER JOIN acc_glcode_index AS code ON (code.pkid=gl.glcodeid)) AS codegl INNER JOIN "
						+ " acc_glcategory_index AS cat ON (cat.pkid = codegl.code_glcatid); ";
				psGL = cn.prepareStatement(sqlGL);

				String sqlGLOpenBal = " SELECT sum(jent.amount) AS balance FROM (SELECT * FROM "
						+ " acc_journal_entry WHERE glaccid='" + thePkid.toString() + "') AS jent INNER JOIN "
						+ " acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) WHERE "
						+ " jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dateStart) + "';";
				psGLOpenBal = cn.prepareStatement(sqlGLOpenBal);

				String sqlGLTransferredAmt = " SELECT sum(jent.amount) AS balance FROM (SELECT * FROM "
						+ " acc_journal_entry WHERE glaccid='" + thePkid.toString() + "') AS jent INNER JOIN "
						+ " acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) WHERE "
						+ " jtxn.txncode = '" + JournalTransactionBean.TXNCODE_PROFIT + "'"
						+ " AND jtxn.transactiondate >= '" + TimeFormat.strDisplayDate(dateStartFY) + "'"
						+ " AND jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dateStart) + "'";
				psGLTransferredAmt = cn.prepareStatement(sqlGLTransferredAmt);

				String sqlGLEntry = "SELECT jtxn.*, jent.description AS entry_description, jent.amount AS entry_amount "
						+ " FROM (SELECT * FROM acc_journal_entry WHERE glaccid='"
						+ thePkid.toString()
						+ "') AS "
						+ " jent INNER JOIN acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) "
						+ " WHERE jtxn.transactiondate >= '" + TimeFormat.strDisplayDate(dateStart) + "' "
						+ " AND jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dateAfterEnd) + "' "
						+ " AND jtxn.typeid='" + JournalTransactionBean.TYPEID_POSTED.toString() + "' "
						+ " AND jtxn.txncode !='" + JournalTransactionBean.TXNCODE_PROFIT+ "' "
						+ " ORDER BY jtxn.transactiondate, jtxn.pkid;";
				psGLEntry = cn.prepareStatement(sqlGLEntry);
				// / Create general ledger view object
				ResultSet rsGL = psGL.executeQuery();
				ResultSet rsGLOpenBal = psGLOpenBal.executeQuery();
				ResultSet rsGLTransferredAmt = psGLTransferredAmt.executeQuery();
				ResultSet rsGLEntry = psGLEntry.executeQuery();
				// / calculate the opening balance for this period
				if (rsGL.next() && rsGLOpenBal.next() && rsGLTransferredAmt.next())
				{
					GeneralLedgerView glView = new GeneralLedgerView();
					GeneralLedgerObject glObj = getObject(rsGL, "");
					glView.setGL(glObj);
					glView.glCode = rsGL.getString("code_code");
					glView.glCategoryPkid = new Integer(rsGL.getInt("cat_pkid"));
					glView.glCategoryRealTemp = rsGL.getString("cat_rt");
					glView.glCategoryPostSection = rsGL.getString("cat_pts");
					glView.dateStart = dateStart;
					glView.dateEnd = dateEnd;
					glView.balanceOpen = rsGLOpenBal.getBigDecimal("balance");
					if (glView.balanceOpen == null)
					{ glView.balanceOpen = new BigDecimal(0); }
					BigDecimal transferredAmt = rsGLTransferredAmt.getBigDecimal("balance");
					if(transferredAmt!=null)
					{ glView.balanceOpen = glView.balanceOpen.add(transferredAmt.negate());}

					// glView.balanceClose = new BigDecimal(0);
					while (rsGLEntry.next())
					{
						GeneralLedgerView.Entry entry = new GeneralLedgerView.Entry();
						JournalTransactionObject jtxnObj = JournalTransactionBean.getObject(rsGLEntry, "");
						entry.setJTxn(jtxnObj);
						entry.entryDescription = rsGLEntry.getString("entry_description");
						entry.entryAmount = rsGLEntry.getBigDecimal("entry_amount");
						glView.vecTxn.add(entry);
					}
					vecGLView.add(glView);
				} else
				{
					/* do nothing */
				}
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLOpenBal);
			cleanup(cn, psGLEntry);
		}
		Log.printVerbose(strObjectName + " Leaving selectGLView");
		return vecGLView;
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
import com.vlee.bean.accounting.*;

public class GeneralLedgerBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_index";
	protected final String strObjectName = "GeneralLedgerBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private GeneralLedgerObject valObj;
	public static final String PKID = "pkid";
	public static final String GLCODEID = "glcodeid";
	public static final String PCCENTERID = "pccenterid";
	public static final String BATCHID = "batchid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	public static final String OPTIONS = "options";
	public static final String CURRENCY = "currency";

	/*
	 * private Integer pkId; private Integer glCodeId; private Integer
	 * pcCenterId; private Integer batchId; private String name; private String
	 * description; private String status; private String options; private
	 * String currency;
	 */
	public GeneralLedgerObject getObject()
	{
		return this.valObj;
	}

	public void setObject(GeneralLedgerObject newVal)
	{
		Integer pkid = this.valObj.pkId;
		this.valObj = newVal;
		this.valObj.pkId = pkid;
	}

	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public Integer getGLCodeId()
	{
		return this.valObj.glCodeId;
	}

	public Integer getPCCenterId()
	{
		return this.valObj.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.valObj.batchId;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public String getOptions()
	{
		return this.valObj.options;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setPkId(Integer pkid)
	{
		this.valObj.pkId = pkid;
	}

	public void setGLCodeId(Integer iGLCodeId)
	{
		this.valObj.glCodeId = iGLCodeId;
	}

	public void setPCCenterId(Integer iPCCenterId)
	{
		this.valObj.pcCenterId = iPCCenterId;
	}

	public void setBatchId(Integer batchid)
	{
		this.valObj.batchId = batchid;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public GeneralLedgerObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GeneralLedgerObject glo) throws Exception
	{
		setObject(glo);
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

	// public Integer ejbCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId,
	// String name, String description, String options, String currency)
	// public void ejbPostCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId, String name, String description, String options, String
	// currency) {
	// }
	public Integer ejbCreate(GeneralLedgerObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = newObj;
			this.valObj.pkId = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkId;
	}

	public void ejbPostCreate(GeneralLedgerObject newObj)
	{
	}

	public void ejbRemove()
	{
		deleteObject(this.valObj.pkId);
	}

	public void ejbActivate()
	{
		this.valObj = new GeneralLedgerObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		this.valObj.pkId = null;
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

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
			throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId, currency);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
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

	public Vector ejbHomeGetDistinctPCCenterAndBatch()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetDistinctPCCenterAndBatch");
		Vector vecValObj = selectDistinctPCCenterAndBatch();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetDistinctPCCenterAndBatch");
		return vecValObj;
	}

	public boolean ejbHomeRetainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeRetainedEarningsExist");
		boolean exist = retainedEarningsExist(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeRetainedEarningsExist");
		return exist;
	}

	public Vector ejbHomeGetGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
	{
		Vector result = new Vector();
		try
		{
			result = selectGLTree(batchId, pcCenter, glCategoryId, glCodeId);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public Vector ejbHomeGetGLView(Vector arrPkid, Timestamp dateStart, Timestamp dateEnd)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectGLView(arrPkid, dateStart, dateEnd);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public Vector ejbHomeGetTrialBalance(Integer pccenter, Integer batch, Timestamp theDate)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectTrialBalance(pccenter, batch, theDate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public GLSummaryObject ejbHomeGetSummary(GLSummaryObject glSumObj)
	{
		try
		{
			glSumObj = selectSummary(glSumObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return glSumObj;
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

	// private Integer insertNewRow(Integer glCodeId, Integer pcCenterId,
	// Integer batchId, String name, String description, String options, String
	// currency)
	private Integer insertObject(GeneralLedgerObject newObj)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			newObj.pkId = newPkId;
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, newObj.glCodeId.intValue());
			ps.setInt(3, newObj.pcCenterId.intValue());
			ps.setInt(4, newObj.batchId.intValue());
			ps.setString(5, newObj.name);
			ps.setString(6, newObj.description);
			ps.setString(7, GeneralLedgerBean.ACTIVE);
			ps.setString(8, newObj.options);
			ps.setString(9, newObj.currency);
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
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
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

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE " + TABLENAME + " SET glcodeid = ?, pccenterid = ?, batchid = ?, "
					+ " name = ?, description = ?, status = ?, options = ?, currency = ?" + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.glCodeId.intValue());
			ps.setInt(2, this.valObj.pcCenterId.intValue());
			ps.setInt(3, this.valObj.batchId.intValue());
			ps.setString(4, this.valObj.name);
			ps.setString(5, this.valObj.description);
			ps.setString(6, this.valObj.status);
			ps.setString(7, this.valObj.options);
			ps.setString(8, this.valObj.currency);
			ps.setInt(9, this.valObj.pkId.intValue());
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

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId)
	{
		return selectUnique(batchId, pcCenter, glCodeId, (String) null);
	}

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
	{
		Integer pkid = null;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE batchid = '" + batchId.toString()
					+ "' AND pccenterid = '" + pcCenter.toString() + "' AND glcodeid = '" + glCodeId.toString() + "' ";
			if (currency != null)
			{
				sqlStatement += " AND currency = '" + currency + "' ";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				pkid = new Integer(rs.getInt("pkid"));
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
		return pkid;
	}

	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY name";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ? ORDER BY name";
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
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? ORDER BY name";
			} else
			{
				sqlStatement = sqlStatement + " ORDER BY name";
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
				GeneralLedgerObject glo = getObject(rs, "");
				vecValObj.add(glo);
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

	private Vector selectTrialBalance(Integer pcCenter, Integer batch, Timestamp theDate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		Timestamp dayAfter = TimeFormat.add(theDate, 0, 0, 1);
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS balance FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE jtxn.pccenterid='"
					+ pcCenter.toString()
					+ "' "
					+ " AND jtxn.batchid='"
					+ batch.toString()
					+ "' AND "
					+ "jtxn.transactiondate<'"
					+ TimeFormat.strDisplayDate(dayAfter)
					+ "' "
					+ "AND jtxn.typeid='"
					+ JournalTransactionBean.TYPEID_POSTED.toString()
					+ "' "
					+ " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON (tbl2.glcodeid = code.pkid)) AS tbl3 INNER JOIN acc_glcategory_index AS cat ON (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				TrialBalanceSession.Row theRow = new TrialBalanceSession.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				vecValObj.add(theRow);
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

	private GLSummaryObject selectSummary(GLSummaryObject glSumObj)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Timestamp dayAfter = TimeFormat.add(glSumObj.dateTo, 0, 0, 1);
		glSumObj.vecRow.clear();
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code AS code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS sum_amt FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE " + " jtxn.pccenterid='"
					+ glSumObj.pcCenter.toString() + "' " + " AND jtxn.batchid='" + glSumObj.batch.toString() + "' "
					+ " AND jtxn.transactiondate>='" + TimeFormat.strDisplayDate(glSumObj.dateFrom) + "' "
					+ " AND jtxn.transactiondate<'" + TimeFormat.strDisplayDate(dayAfter) + "' "
					+ " AND jtxn.typeid ='" + glSumObj.jTypeId.toString() + "' ";
			if (glSumObj.jTxnCode != null)
			{
				sqlStatement += " AND jtxn.txncode = '" + glSumObj.jTxnCode + "' ";
			}
			sqlStatement += " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON "
					+ " (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON "
					+ " (tbl2.glcodeid = code.pkid)";
			if (glSumObj.glCode != null)
			{
				sqlStatement += " WHERE code.code = '" + glSumObj.glCode + "' ";
			}
			sqlStatement += ") AS tbl3 INNER JOIN acc_glcategory_index AS cat ON "
					+ " (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			Log.printVerbose(sqlStatement);
			Log.printVerbose(sqlStatement);
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLSummaryObject.Row theRow = new GLSummaryObject.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.amount = rs.getBigDecimal("sum_amt");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				glSumObj.vecRow.add(theRow);
				// vecValObj.add(theRow);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return glSumObj;
	}

	private boolean retainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		boolean exist = false;
		try
		{
			String sqlStatement = "SELECT "
					+ TABLENAME
					+ ".pkid FROM "
					+ TABLENAME
					+ ", acc_glcode_index, acc_glcategory_index WHERE "
					+ TABLENAME
					+ ".glcodeid = acc_glcode_index.pkid AND acc_glcode_index.glcategoryid = acc_glcategory_index.pkid AND "
					+ "acc_glcategory_index.postto_section = 'RetainedEarnings' AND " + TABLENAME + "." + fieldName1
					+ " = ? AND " + TABLENAME + "." + fieldName2 + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, value2);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				exist = true;
			} else
			{
				exist = false;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return exist;
	}

	private Vector selectDistinctPCCenterAndBatch()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT DISTINCT pccenterid, batchid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerObject glo = new GeneralLedgerObject();
				glo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				glo.batchId = new Integer(rs.getInt("batchid"));
				vecValObj.add(glo);
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

	public Vector selectGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
			throws SQLException, Exception
	{
		Vector vecGLTree = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLCode = null;
		PreparedStatement psGLCat = null;
		try
		{
			// String sqlGL = " SELECT * FROM " + TABLENAME ;
			String sqlGL = " SELECT gl2.*, cat.postto_section FROM "
					+ " (SELECT gl.*,code.glcategoryid AS catid, code.code AS glcode "
					+ "	 FROM acc_general_ledger_index "
					+ " AS gl INNER JOIN acc_glcode_index AS code ON (gl.glcodeid = code.pkid)  "
					+ " WHERE gl.pccenterid='" + pcCenter.toString() + "' " + " ) "
					+ " AS gl2 INNER JOIN acc_glcategory_index AS cat ON (gl2.catid = cat.pkid) "
					+ " ORDER BY cat.postto_section, gl2.glcode ";
			cn = ds.getConnection();
			psGL = cn.prepareStatement(sqlGL);
			ResultSet rsGL = psGL.executeQuery();
			while (rsGL.next())
			{
				// / processing glcode
				Integer ledgerGLCode = new Integer(rsGL.getInt(GLCODEID));
				if (glCodeId != null && !ledgerGLCode.equals(glCodeId))
				{
					continue;
				}
				String sqlGLCode = " SELECT * FROM " + GLCodeBean.TABLENAME + " WHERE " + GLCodeBean.PKID + " ='"
						+ ledgerGLCode.toString() + "' ";
				psGLCode = cn.prepareStatement(sqlGLCode);
				ResultSet rsGLCode = psGLCode.executeQuery();
				if (!rsGLCode.next())
				{
					continue;
				}
				GLCodeObject glCodeObj = GLCodeBean.getObject(rsGLCode, "");
				// / processing gl-category
				if (glCategoryId != null && !glCodeObj.glCategoryId.equals(glCategoryId))
				{
					continue;
				}
				String sqlGLCat = " SELECT * FROM " + GLCategoryBean.TABLENAME + " WHERE " + GLCategoryBean.PKID
						+ " ='" + glCodeObj.glCategoryId.toString() + "' ";
				psGLCat = cn.prepareStatement(sqlGLCat);
				ResultSet rsGLCat = psGLCat.executeQuery();
				if (!rsGLCat.next())
				{
					continue;
				}
				GLCategoryObject glCategoryObj = GLCategoryBean.getObject(rsGLCat, "");
				GeneralLedgerObject glObj = getObject(rsGL, "");
				GeneralLedgerTreeObject glTreeObj = new GeneralLedgerTreeObject(glObj);
				glTreeObj.glCodeObj = glCodeObj;
				glTreeObj.glCategoryObj = glCategoryObj;
				vecGLTree.add(glTreeObj);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLCode);
			cleanup(cn, psGLCat);
		}
		return vecGLTree;
	}

	public static GeneralLedgerObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GeneralLedgerObject theObj = null;
		try
		{
			theObj = new GeneralLedgerObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.glCodeId = new Integer(rs.getInt("glcodeid"));
			theObj.pcCenterId = new Integer(rs.getInt("pccenterid"));
			theObj.batchId = new Integer(rs.getInt("batchid"));
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.status = rs.getString("status");
			theObj.options = rs.getString("options");
			theObj.currency = rs.getString("currency");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	public Vector selectGLView(Vector arrPkid, Timestamp dateStart, Timestamp dateEnd) throws SQLException, Exception
	{
		Timestamp dateAfterEnd = TimeFormat.add(dateEnd, 0, 0, 1);
		Vector vecGLView = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLOpenBal = null;
		PreparedStatement psGLEntry = null;
		try
		{
			cn = ds.getConnection();
			for (int cnt1 = 0; cnt1 < arrPkid.size(); cnt1++)
			{
				Integer thePkid = (Integer) arrPkid.get(cnt1);
				String sqlGL = " SELECT cat.pkid AS cat_pkid, cat.real_temp AS cat_rt, cat.postto_section AS cat_pts , "
						+ " codegl.* FROM (SELECT code.pkid AS code_pkid, code.code AS code_code, code.glcategoryid "
						+ " AS code_glcatid, gl.* FROM (SELECT * FROM acc_general_ledger_index WHERE pkid  = '"
						+ thePkid.toString()
						+ "' ) "
						+ " AS gl INNER JOIN acc_glcode_index AS code ON (code.pkid=gl.glcodeid)) AS codegl INNER JOIN "
						+ " acc_glcategory_index AS cat ON (cat.pkid = codegl.code_glcatid); ";
				psGL = cn.prepareStatement(sqlGL);
				String sqlGLOpenBal = " SELECT sum(jent.amount) AS balance FROM (SELECT * FROM "
						+ " acc_journal_entry WHERE glaccid='" + thePkid.toString() + "') AS jent INNER JOIN "
						+ " acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) WHERE "
						+ " jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dateStart) + "';";
				psGLOpenBal = cn.prepareStatement(sqlGLOpenBal);
				String sqlGLEntry = "SELECT jtxn.*, jent.description AS entry_description, jent.amount AS entry_amount "
						+ " FROM (SELECT * FROM acc_journal_entry WHERE glaccid='"
						+ thePkid.toString()
						+ "') AS "
						+ " jent INNER JOIN acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) "
						+ " WHERE jtxn.transactiondate >= '"
						+ TimeFormat.strDisplayDate(dateStart)
						+ "' AND jtxn.transactiondate < '"
						+ TimeFormat.strDisplayDate(dateAfterEnd)
						+ "' "
						+ " AND jtxn.typeid='"
						+ JournalTransactionBean.TYPEID_POSTED.toString()
						+ "' "
						+ " ORDER BY jtxn.transactiondate, jtxn.pkid;";
				psGLEntry = cn.prepareStatement(sqlGLEntry);
				// / Create general ledger view object
				ResultSet rsGL = psGL.executeQuery();
				ResultSet rsGLOpenBal = psGLOpenBal.executeQuery();
				ResultSet rsGLEntry = psGLEntry.executeQuery();
				// / calculate the opening balance for this period
				if (rsGL.next() && rsGLOpenBal.next())
				{
					GeneralLedgerView glView = new GeneralLedgerView();
					GeneralLedgerObject glObj = getObject(rsGL, "");
					glView.setGL(glObj);
					glView.glCode = rsGL.getString("code_code");
					glView.glCategoryPkid = new Integer(rsGL.getInt("cat_pkid"));
					glView.glCategoryRealTemp = rsGL.getString("cat_rt");
					glView.glCategoryPostSection = rsGL.getString("cat_pts");
					glView.dateStart = dateStart;
					glView.dateEnd = dateEnd;
					glView.balanceOpen = rsGLOpenBal.getBigDecimal("balance");
					if (glView.balanceOpen == null)
					{
						glView.balanceOpen = new BigDecimal(0);
					}
					// glView.balanceClose = new BigDecimal(0);
					while (rsGLEntry.next())
					{
						GeneralLedgerView.Entry entry = new GeneralLedgerView.Entry();
						JournalTransactionObject jtxnObj = JournalTransactionBean.getObject(rsGLEntry, "");
						entry.setJTxn(jtxnObj);
						entry.entryDescription = rsGLEntry.getString("entry_description");
						entry.entryAmount = rsGLEntry.getBigDecimal("entry_amount");
						glView.vecTxn.add(entry);
					}
					vecGLView.add(glView);
				} else
				{
					/* do nothing */
				}
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLOpenBal);
			cleanup(cn, psGLEntry);
		}
		return vecGLView;
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
import com.vlee.bean.accounting.*;

public class GeneralLedgerBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_index";
	protected final String strObjectName = "GeneralLedgerBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private GeneralLedgerObject valObj;
	public static final String PKID = "pkid";
	public static final String GLCODEID = "glcodeid";
	public static final String PCCENTERID = "pccenterid";
	public static final String BATCHID = "batchid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	public static final String OPTIONS = "options";
	public static final String CURRENCY = "currency";

	/*
	 * private Integer pkId; private Integer glCodeId; private Integer
	 * pcCenterId; private Integer batchId; private String name; private String
	 * description; private String status; private String options; private
	 * String currency;
	 */
	public GeneralLedgerObject getObject()
	{
		return this.valObj;
	}

	public void setObject(GeneralLedgerObject newVal)
	{
		Integer pkid = this.valObj.pkId;
		this.valObj = newVal;
		this.valObj.pkId = pkid;
	}

	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public Integer getGLCodeId()
	{
		return this.valObj.glCodeId;
	}

	public Integer getPCCenterId()
	{
		return this.valObj.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.valObj.batchId;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public String getOptions()
	{
		return this.valObj.options;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setPkId(Integer pkid)
	{
		this.valObj.pkId = pkid;
	}

	public void setGLCodeId(Integer iGLCodeId)
	{
		this.valObj.glCodeId = iGLCodeId;
	}

	public void setPCCenterId(Integer iPCCenterId)
	{
		this.valObj.pcCenterId = iPCCenterId;
	}

	public void setBatchId(Integer batchid)
	{
		this.valObj.batchId = batchid;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public GeneralLedgerObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GeneralLedgerObject glo) throws Exception
	{
		setObject(glo);
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

	// public Integer ejbCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId,
	// String name, String description, String options, String currency)
	// public void ejbPostCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId, String name, String description, String options, String
	// currency) {
	// }
	public Integer ejbCreate(GeneralLedgerObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = newObj;
			this.valObj.pkId = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkId;
	}

	public void ejbPostCreate(GeneralLedgerObject newObj)
	{
	}

	public void ejbRemove()
	{
		deleteObject(this.valObj.pkId);
	}

	public void ejbActivate()
	{
		this.valObj = new GeneralLedgerObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		this.valObj.pkId = null;
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

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
			throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId, currency);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
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

	public Vector ejbHomeGetDistinctPCCenterAndBatch()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetDistinctPCCenterAndBatch");
		Vector vecValObj = selectDistinctPCCenterAndBatch();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetDistinctPCCenterAndBatch");
		return vecValObj;
	}

	public boolean ejbHomeRetainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeRetainedEarningsExist");
		boolean exist = retainedEarningsExist(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeRetainedEarningsExist");
		return exist;
	}

	public Vector ejbHomeGetGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
	{
		Vector result = new Vector();
		try
		{
			result = selectGLTree(batchId, pcCenter, glCategoryId, glCodeId);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public Vector ejbHomeGetGLView(Vector arrPkid, Timestamp dateStart, Timestamp dateEnd)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectGLView(arrPkid, dateStart, dateEnd);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public Vector ejbHomeGetTrialBalance(Integer pccenter, Integer batch, Timestamp theDate)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectTrialBalance(pccenter, batch, theDate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public GLSummaryObject ejbHomeGetSummary(GLSummaryObject glSumObj)
	{
		try
		{
			glSumObj = selectSummary(glSumObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return glSumObj;
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

	// private Integer insertNewRow(Integer glCodeId, Integer pcCenterId,
	// Integer batchId, String name, String description, String options, String
	// currency)
	private Integer insertObject(GeneralLedgerObject newObj)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			newObj.pkId = newPkId;
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, newObj.glCodeId.intValue());
			ps.setInt(3, newObj.pcCenterId.intValue());
			ps.setInt(4, newObj.batchId.intValue());
			ps.setString(5, newObj.name);
			ps.setString(6, newObj.description);
			ps.setString(7, GeneralLedgerBean.ACTIVE);
			ps.setString(8, newObj.options);
			ps.setString(9, newObj.currency);
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
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
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

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE " + TABLENAME + " SET glcodeid = ?, pccenterid = ?, batchid = ?, "
					+ " name = ?, description = ?, status = ?, options = ?, currency = ?" + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.glCodeId.intValue());
			ps.setInt(2, this.valObj.pcCenterId.intValue());
			ps.setInt(3, this.valObj.batchId.intValue());
			ps.setString(4, this.valObj.name);
			ps.setString(5, this.valObj.description);
			ps.setString(6, this.valObj.status);
			ps.setString(7, this.valObj.options);
			ps.setString(8, this.valObj.currency);
			ps.setInt(9, this.valObj.pkId.intValue());
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

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId)
	{
		return selectUnique(batchId, pcCenter, glCodeId, (String) null);
	}

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
	{
		Integer pkid = null;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE batchid = '" + batchId.toString()
					+ "' AND pccenterid = '" + pcCenter.toString() + "' AND glcodeid = '" + glCodeId.toString() + "' ";
			if (currency != null)
			{
				sqlStatement += " AND currency = '" + currency + "' ";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				pkid = new Integer(rs.getInt("pkid"));
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
		return pkid;
	}

	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY name";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ? ORDER BY name";
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
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? ORDER BY name";
			} else
			{
				sqlStatement = sqlStatement + " ORDER BY name";
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
				GeneralLedgerObject glo = getObject(rs, "");
				vecValObj.add(glo);
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

	private Vector selectTrialBalance(Integer pcCenter, Integer batch, Timestamp theDate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		Timestamp dayAfter = TimeFormat.add(theDate, 0, 0, 1);
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS balance FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE jtxn.pccenterid='"
					+ pcCenter.toString()
					+ "' "
					+ " AND jtxn.batchid='"
					+ batch.toString()
					+ "' AND "
					+ "jtxn.transactiondate<'"
					+ TimeFormat.strDisplayDate(dayAfter)
					+ "' "
					+ "AND jtxn.typeid='"
					+ JournalTransactionBean.TYPEID_POSTED.toString()
					+ "' "
					+ " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON (tbl2.glcodeid = code.pkid)) AS tbl3 INNER JOIN acc_glcategory_index AS cat ON (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				TrialBalanceSession.Row theRow = new TrialBalanceSession.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				vecValObj.add(theRow);
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

	private GLSummaryObject selectSummary(GLSummaryObject glSumObj)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Timestamp dayAfter = TimeFormat.add(glSumObj.dateTo, 0, 0, 1);
		glSumObj.vecRow.clear();
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code AS code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS sum_amt FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE " + " jtxn.pccenterid='"
					+ glSumObj.pcCenter.toString() + "' " + " AND jtxn.batchid='" + glSumObj.batch.toString() + "' "
					+ " AND jtxn.transactiondate>='" + TimeFormat.strDisplayDate(glSumObj.dateFrom) + "' "
					+ " AND jtxn.transactiondate<'" + TimeFormat.strDisplayDate(dayAfter) + "' "
					+ " AND jtxn.typeid ='" + glSumObj.jTypeId.toString() + "' ";
			if (glSumObj.jTxnCode != null)
			{
				sqlStatement += " AND jtxn.txncode = '" + glSumObj.jTxnCode + "' ";
			}
			sqlStatement += " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON "
					+ " (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON "
					+ " (tbl2.glcodeid = code.pkid)";
			if (glSumObj.glCode != null)
			{
				sqlStatement += " WHERE code.code = '" + glSumObj.glCode + "' ";
			}
			sqlStatement += ") AS tbl3 INNER JOIN acc_glcategory_index AS cat ON "
					+ " (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			Log.printVerbose(sqlStatement);
			Log.printVerbose(sqlStatement);
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLSummaryObject.Row theRow = new GLSummaryObject.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.amount = rs.getBigDecimal("sum_amt");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				glSumObj.vecRow.add(theRow);
				// vecValObj.add(theRow);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return glSumObj;
	}

	private boolean retainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		boolean exist = false;
		try
		{
			String sqlStatement = "SELECT "
					+ TABLENAME
					+ ".pkid FROM "
					+ TABLENAME
					+ ", acc_glcode_index, acc_glcategory_index WHERE "
					+ TABLENAME
					+ ".glcodeid = acc_glcode_index.pkid AND acc_glcode_index.glcategoryid = acc_glcategory_index.pkid AND "
					+ "acc_glcategory_index.postto_section = 'RetainedEarnings' AND " + TABLENAME + "." + fieldName1
					+ " = ? AND " + TABLENAME + "." + fieldName2 + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, value2);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				exist = true;
			} else
			{
				exist = false;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return exist;
	}

	private Vector selectDistinctPCCenterAndBatch()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT DISTINCT pccenterid, batchid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerObject glo = new GeneralLedgerObject();
				glo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				glo.batchId = new Integer(rs.getInt("batchid"));
				vecValObj.add(glo);
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

	public Vector selectGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
			throws SQLException, Exception
	{
		Vector vecGLTree = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLCode = null;
		PreparedStatement psGLCat = null;
		try
		{
			// String sqlGL = " SELECT * FROM " + TABLENAME ;
			String sqlGL = " SELECT gl2.*, cat.postto_section FROM "
					+ " (SELECT gl.*,code.glcategoryid AS catid, code.code AS glcode "
					+ "	 FROM acc_general_ledger_index "
					+ " AS gl INNER JOIN acc_glcode_index AS code ON (gl.glcodeid = code.pkid)  "
					+ " WHERE gl.pccenterid='" + pcCenter.toString() + "' " + " ) "
					+ " AS gl2 INNER JOIN acc_glcategory_index AS cat ON (gl2.catid = cat.pkid) "
					+ " ORDER BY cat.postto_section, gl2.glcode ";
			cn = ds.getConnection();
			psGL = cn.prepareStatement(sqlGL);
			ResultSet rsGL = psGL.executeQuery();
			while (rsGL.next())
			{
				// / processing glcode
				Integer ledgerGLCode = new Integer(rsGL.getInt(GLCODEID));
				if (glCodeId != null && !ledgerGLCode.equals(glCodeId))
				{
					continue;
				}
				String sqlGLCode = " SELECT * FROM " + GLCodeBean.TABLENAME + " WHERE " + GLCodeBean.PKID + " ='"
						+ ledgerGLCode.toString() + "' ";
				psGLCode = cn.prepareStatement(sqlGLCode);
				ResultSet rsGLCode = psGLCode.executeQuery();
				if (!rsGLCode.next())
				{
					continue;
				}
				GLCodeObject glCodeObj = GLCodeBean.getObject(rsGLCode, "");
				// / processing gl-category
				if (glCategoryId != null && !glCodeObj.glCategoryId.equals(glCategoryId))
				{
					continue;
				}
				String sqlGLCat = " SELECT * FROM " + GLCategoryBean.TABLENAME + " WHERE " + GLCategoryBean.PKID
						+ " ='" + glCodeObj.glCategoryId.toString() + "' ";
				psGLCat = cn.prepareStatement(sqlGLCat);
				ResultSet rsGLCat = psGLCat.executeQuery();
				if (!rsGLCat.next())
				{
					continue;
				}
				GLCategoryObject glCategoryObj = GLCategoryBean.getObject(rsGLCat, "");
				GeneralLedgerObject glObj = getObject(rsGL, "");
				GeneralLedgerTreeObject glTreeObj = new GeneralLedgerTreeObject(glObj);
				glTreeObj.glCodeObj = glCodeObj;
				glTreeObj.glCategoryObj = glCategoryObj;
				vecGLTree.add(glTreeObj);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLCode);
			cleanup(cn, psGLCat);
		}
		return vecGLTree;
	}

	public static GeneralLedgerObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GeneralLedgerObject theObj = null;
		try
		{
			theObj = new GeneralLedgerObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.glCodeId = new Integer(rs.getInt("glcodeid"));
			theObj.pcCenterId = new Integer(rs.getInt("pccenterid"));
			theObj.batchId = new Integer(rs.getInt("batchid"));
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.status = rs.getString("status");
			theObj.options = rs.getString("options");
			theObj.currency = rs.getString("currency");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	public Vector selectGLView(Vector arrPkid, Timestamp dateStart, Timestamp dateEnd) throws SQLException, Exception
	{
		Timestamp dateAfterEnd = TimeFormat.add(dateEnd, 0, 0, 1);
		Vector vecGLView = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLOpenBal = null;
		PreparedStatement psGLEntry = null;
		try
		{
			cn = ds.getConnection();
			for (int cnt1 = 0; cnt1 < arrPkid.size(); cnt1++)
			{
				Integer thePkid = (Integer) arrPkid.get(cnt1);
				String sqlGL = " SELECT cat.pkid AS cat_pkid, cat.real_temp AS cat_rt, cat.postto_section AS cat_pts , "
						+ " codegl.* FROM (SELECT code.pkid AS code_pkid, code.code AS code_code, code.glcategoryid "
						+ " AS code_glcatid, gl.* FROM (SELECT * FROM acc_general_ledger_index WHERE pkid  = '"
						+ thePkid.toString()
						+ "' ) "
						+ " AS gl INNER JOIN acc_glcode_index AS code ON (code.pkid=gl.glcodeid)) AS codegl INNER JOIN "
						+ " acc_glcategory_index AS cat ON (cat.pkid = codegl.code_glcatid); ";
				psGL = cn.prepareStatement(sqlGL);
				String sqlGLOpenBal = " SELECT sum(jent.amount) AS balance FROM (SELECT * FROM "
						+ " acc_journal_entry WHERE glaccid='" + thePkid.toString() + "') AS jent INNER JOIN "
						+ " acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) WHERE "
						+ " jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dateStart) + "';";
				psGLOpenBal = cn.prepareStatement(sqlGLOpenBal);
				String sqlGLEntry = "SELECT jtxn.*, jent.description AS entry_description, jent.amount AS entry_amount "
						+ " FROM (SELECT * FROM acc_journal_entry WHERE glaccid='"
						+ thePkid.toString()
						+ "') AS "
						+ " jent INNER JOIN acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) "
						+ " WHERE jtxn.transactiondate >= '"
						+ TimeFormat.strDisplayDate(dateStart)
						+ "' AND jtxn.transactiondate < '"
						+ TimeFormat.strDisplayDate(dateAfterEnd)
						+ "' "
						+ " AND jtxn.typeid='"
						+ JournalTransactionBean.TYPEID_POSTED.toString()
						+ "' "
						+ " ORDER BY jtxn.transactiondate, jtxn.pkid;";
				psGLEntry = cn.prepareStatement(sqlGLEntry);
				// / Create general ledger view object
				ResultSet rsGL = psGL.executeQuery();
				ResultSet rsGLOpenBal = psGLOpenBal.executeQuery();
				ResultSet rsGLEntry = psGLEntry.executeQuery();
				// / calculate the opening balance for this period
				if (rsGL.next() && rsGLOpenBal.next())
				{
					GeneralLedgerView glView = new GeneralLedgerView();
					GeneralLedgerObject glObj = getObject(rsGL, "");
					glView.setGL(glObj);
					glView.glCode = rsGL.getString("code_code");
					glView.glCategoryPkid = new Integer(rsGL.getInt("cat_pkid"));
					glView.glCategoryRealTemp = rsGL.getString("cat_rt");
					glView.glCategoryPostSection = rsGL.getString("cat_pts");
					glView.dateStart = dateStart;
					glView.dateEnd = dateEnd;
					glView.balanceOpen = rsGLOpenBal.getBigDecimal("balance");
					if (glView.balanceOpen == null)
					{
						glView.balanceOpen = new BigDecimal(0);
					}
					// glView.balanceClose = new BigDecimal(0);
					while (rsGLEntry.next())
					{
						GeneralLedgerView.Entry entry = new GeneralLedgerView.Entry();
						JournalTransactionObject jtxnObj = JournalTransactionBean.getObject(rsGLEntry, "");
						entry.setJTxn(jtxnObj);
						entry.entryDescription = rsGLEntry.getString("entry_description");
						entry.entryAmount = rsGLEntry.getBigDecimal("entry_amount");
						glView.vecTxn.add(entry);
					}
					vecGLView.add(glView);
				} else
				{
					/* do nothing */
				}
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLOpenBal);
			cleanup(cn, psGLEntry);
		}
		return vecGLView;
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
import com.vlee.bean.accounting.*;

public class GeneralLedgerBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_index";
	protected final String strObjectName = "GeneralLedgerBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private GeneralLedgerObject valObj;
	public static final String PKID = "pkid";
	public static final String GLCODEID = "glcodeid";
	public static final String PCCENTERID = "pccenterid";
	public static final String BATCHID = "batchid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	public static final String OPTIONS = "options";
	public static final String CURRENCY = "currency";

	/*
	 * private Integer pkId; private Integer glCodeId; private Integer
	 * pcCenterId; private Integer batchId; private String name; private String
	 * description; private String status; private String options; private
	 * String currency;
	 */
	public GeneralLedgerObject getObject()
	{
		return this.valObj;
	}

	public void setObject(GeneralLedgerObject newVal)
	{
		Integer pkid = this.valObj.pkId;
		this.valObj = newVal;
		this.valObj.pkId = pkid;
	}

	public Integer getPkId()
	{
		return this.valObj.pkId;
	}

	public Integer getGLCodeId()
	{
		return this.valObj.glCodeId;
	}

	public Integer getPCCenterId()
	{
		return this.valObj.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.valObj.batchId;
	}

	public String getName()
	{
		return this.valObj.name;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public String getOptions()
	{
		return this.valObj.options;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setPkId(Integer pkid)
	{
		this.valObj.pkId = pkid;
	}

	public void setGLCodeId(Integer iGLCodeId)
	{
		this.valObj.glCodeId = iGLCodeId;
	}

	public void setPCCenterId(Integer iPCCenterId)
	{
		this.valObj.pcCenterId = iPCCenterId;
	}

	public void setBatchId(Integer batchid)
	{
		this.valObj.batchId = batchid;
	}

	public void setName(String strName)
	{
		this.valObj.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.valObj.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public void setOptions(String strOptions)
	{
		this.valObj.options = strOptions;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public GeneralLedgerObject getValueObject()
	{
		return this.valObj;
	}

	public void setValueObject(GeneralLedgerObject glo) throws Exception
	{
		setObject(glo);
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

	// public Integer ejbCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId,
	// String name, String description, String options, String currency)
	// public void ejbPostCreate(Integer glCodeId, Integer pcCenterId, Integer
	// batchId, String name, String description, String options, String
	// currency) {
	// }
	public Integer ejbCreate(GeneralLedgerObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = newObj;
			this.valObj.pkId = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkId;
	}

	public void ejbPostCreate(GeneralLedgerObject newObj)
	{
	}

	public void ejbRemove()
	{
		deleteObject(this.valObj.pkId);
	}

	public void ejbActivate()
	{
		this.valObj = new GeneralLedgerObject();
		this.valObj.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		this.valObj.pkId = null;
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

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
			throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		Integer thePkid = selectUnique(batchId, pcCenter, glCodeId, currency);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (thePkid != null)
		{
			return thePkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + thePkid.toString() + " not found.");
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

	public Vector ejbHomeGetDistinctPCCenterAndBatch()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetDistinctPCCenterAndBatch");
		Vector vecValObj = selectDistinctPCCenterAndBatch();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetDistinctPCCenterAndBatch");
		return vecValObj;
	}

	public boolean ejbHomeRetainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeRetainedEarningsExist");
		boolean exist = retainedEarningsExist(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeRetainedEarningsExist");
		return exist;
	}

	public Vector ejbHomeGetGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
	{
		Vector result = new Vector();
		try
		{
			result = selectGLTree(batchId, pcCenter, glCategoryId, glCodeId);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public Vector ejbHomeGetGLView(Vector arrPkid, Timestamp dateStart, Timestamp dateEnd)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectGLView(arrPkid, dateStart, dateEnd);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public Vector ejbHomeGetTrialBalance(Integer pccenter, Integer batch, Timestamp theDate)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectTrialBalance(pccenter, batch, theDate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public GLSummaryObject ejbHomeGetSummary(GLSummaryObject glSumObj)
	{
		try
		{
			glSumObj = selectSummary(glSumObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return glSumObj;
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

	// private Integer insertNewRow(Integer glCodeId, Integer pcCenterId,
	// Integer batchId, String name, String description, String options, String
	// currency)
	private Integer insertObject(GeneralLedgerObject newObj)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			newObj.pkId = newPkId;
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, newObj.glCodeId.intValue());
			ps.setInt(3, newObj.pcCenterId.intValue());
			ps.setInt(4, newObj.batchId.intValue());
			ps.setString(5, newObj.name);
			ps.setString(6, newObj.description);
			ps.setString(7, GeneralLedgerBean.ACTIVE);
			ps.setString(8, newObj.options);
			ps.setString(9, newObj.currency);
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
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
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

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE " + TABLENAME + " SET glcodeid = ?, pccenterid = ?, batchid = ?, "
					+ " name = ?, description = ?, status = ?, options = ?, currency = ?" + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.valObj.glCodeId.intValue());
			ps.setInt(2, this.valObj.pcCenterId.intValue());
			ps.setInt(3, this.valObj.batchId.intValue());
			ps.setString(4, this.valObj.name);
			ps.setString(5, this.valObj.description);
			ps.setString(6, this.valObj.status);
			ps.setString(7, this.valObj.options);
			ps.setString(8, this.valObj.currency);
			ps.setInt(9, this.valObj.pkId.intValue());
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

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId)
	{
		return selectUnique(batchId, pcCenter, glCodeId, (String) null);
	}

	private Integer selectUnique(Integer batchId, Integer pcCenter, Integer glCodeId, String currency)
	{
		Integer pkid = null;
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE batchid = '" + batchId.toString()
					+ "' AND pccenterid = '" + pcCenter.toString() + "' AND glcodeid = '" + glCodeId.toString() + "' ";
			if (currency != null)
			{
				sqlStatement += " AND currency = '" + currency + "' ";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				pkid = new Integer(rs.getInt("pkid"));
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
		return pkid;
	}

	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY name";
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
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ? ORDER BY name";
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
			String sqlStatement = "SELECT pkid, glcodeid, pccenterid, batchid, name, description, status, options, currency FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? ORDER BY name";
			} else
			{
				sqlStatement = sqlStatement + " ORDER BY name";
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
				GeneralLedgerObject glo = getObject(rs, "");
				vecValObj.add(glo);
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

	private Vector selectTrialBalance(Integer pcCenter, Integer batch, Timestamp theDate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		Timestamp dayAfter = TimeFormat.add(theDate, 0, 0, 1);
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS balance FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE jtxn.pccenterid='"
					+ pcCenter.toString()
					+ "' "
					+ " AND jtxn.batchid='"
					+ batch.toString()
					+ "' AND "
					+ "jtxn.transactiondate<'"
					+ TimeFormat.strDisplayDate(dayAfter)
					+ "' "
					+ "AND jtxn.typeid='"
					+ JournalTransactionBean.TYPEID_POSTED.toString()
					+ "' "
					+ " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON (tbl2.glcodeid = code.pkid)) AS tbl3 INNER JOIN acc_glcategory_index AS cat ON (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				TrialBalanceSession.Row theRow = new TrialBalanceSession.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				vecValObj.add(theRow);
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

	private GLSummaryObject selectSummary(GLSummaryObject glSumObj)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Timestamp dayAfter = TimeFormat.add(glSumObj.dateTo, 0, 0, 1);
		glSumObj.vecRow.clear();
		try
		{
			String sqlStatement = "SELECT tbl3.*, cat.real_temp, cat.postto_section FROM (SELECT tbl2.* , "
					+ " code.code AS code, code.glcategoryid FROM (SELECT sumary.*, gl.glcodeid, gl.name  AS gl_name FROM "
					+ " (SELECT jent.glaccid, sum(jent.amount) AS sum_amt FROM acc_journal_transaction AS jtxn INNER JOIN "
					+ " acc_journal_entry AS jent ON (jent.journaltxnid=jtxn.pkid) WHERE " + " jtxn.pccenterid='"
					+ glSumObj.pcCenter.toString() + "' " + " AND jtxn.batchid='" + glSumObj.batch.toString() + "' "
					+ " AND jtxn.transactiondate>='" + TimeFormat.strDisplayDate(glSumObj.dateFrom) + "' "
					+ " AND jtxn.transactiondate<'" + TimeFormat.strDisplayDate(dayAfter) + "' "
					+ " AND jtxn.typeid ='" + glSumObj.jTypeId.toString() + "' ";
			if (glSumObj.jTxnCode != null)
			{
				sqlStatement += " AND jtxn.txncode = '" + glSumObj.jTxnCode + "' ";
			}
			sqlStatement += " GROUP BY jent.glaccid) as sumary INNER JOIN acc_general_ledger_index AS gl ON "
					+ " (sumary.glaccid = gl.pkid)) AS tbl2 INNER JOIN acc_glcode_index AS code ON "
					+ " (tbl2.glcodeid = code.pkid)";
			if (glSumObj.glCode != null)
			{
				sqlStatement += " WHERE code.code = '" + glSumObj.glCode + "' ";
			}
			sqlStatement += ") AS tbl3 INNER JOIN acc_glcategory_index AS cat ON "
					+ " (tbl3.glcategoryid = cat.pkid) ORDER BY cat.postto_section, tbl3.code;";
			Log.printVerbose(sqlStatement);
			Log.printVerbose(sqlStatement);
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GLSummaryObject.Row theRow = new GLSummaryObject.Row();
				theRow.glCategoryId = new Integer(rs.getInt("glcategoryid"));
				theRow.postSection = rs.getString("postto_section");
				theRow.realTemp = rs.getString("real_temp");
				theRow.glCodeId = new Integer(rs.getInt("glcodeid"));
				theRow.glCode = rs.getString("code");
				theRow.glCodeName = rs.getString("gl_name");
				theRow.amount = rs.getBigDecimal("sum_amt");
				theRow.genLedgerId = new Integer(rs.getInt("glaccid"));
				glSumObj.vecRow.add(theRow);
				// vecValObj.add(theRow);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return glSumObj;
	}

	private boolean retainedEarningsExist(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		boolean exist = false;
		try
		{
			String sqlStatement = "SELECT "
					+ TABLENAME
					+ ".pkid FROM "
					+ TABLENAME
					+ ", acc_glcode_index, acc_glcategory_index WHERE "
					+ TABLENAME
					+ ".glcodeid = acc_glcode_index.pkid AND acc_glcode_index.glcategoryid = acc_glcategory_index.pkid AND "
					+ "acc_glcategory_index.postto_section = 'RetainedEarnings' AND " + TABLENAME + "." + fieldName1
					+ " = ? AND " + TABLENAME + "." + fieldName2 + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			ps.setString(2, value2);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				exist = true;
			} else
			{
				exist = false;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return exist;
	}

	private Vector selectDistinctPCCenterAndBatch()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT DISTINCT pccenterid, batchid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerObject glo = new GeneralLedgerObject();
				glo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				glo.batchId = new Integer(rs.getInt("batchid"));
				vecValObj.add(glo);
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

	public Vector selectGLTree(Integer batchId, Integer pcCenter, Integer glCategoryId, Integer glCodeId)
			throws SQLException, Exception
	{
		Vector vecGLTree = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLCode = null;
		PreparedStatement psGLCat = null;
		try
		{
			// String sqlGL = " SELECT * FROM " + TABLENAME ;
			String sqlGL = " SELECT gl2.*, cat.postto_section FROM "
					+ " (SELECT gl.*,code.glcategoryid AS catid, code.code AS glcode "
					+ "	 FROM acc_general_ledger_index "
					+ " AS gl INNER JOIN acc_glcode_index AS code ON (gl.glcodeid = code.pkid)  "
					+ " WHERE gl.pccenterid='" + pcCenter.toString() + "' " + " ) "
					+ " AS gl2 INNER JOIN acc_glcategory_index AS cat ON (gl2.catid = cat.pkid) "
					+ " ORDER BY cat.postto_section, gl2.glcode ";
			cn = ds.getConnection();
			psGL = cn.prepareStatement(sqlGL);
			ResultSet rsGL = psGL.executeQuery();
			while (rsGL.next())
			{
				// / processing glcode
				Integer ledgerGLCode = new Integer(rsGL.getInt(GLCODEID));
				if (glCodeId != null && !ledgerGLCode.equals(glCodeId))
				{
					continue;
				}
				String sqlGLCode = " SELECT * FROM " + GLCodeBean.TABLENAME + " WHERE " + GLCodeBean.PKID + " ='"
						+ ledgerGLCode.toString() + "' ";
				psGLCode = cn.prepareStatement(sqlGLCode);
				ResultSet rsGLCode = psGLCode.executeQuery();
				if (!rsGLCode.next())
				{
					continue;
				}
				GLCodeObject glCodeObj = GLCodeBean.getObject(rsGLCode, "");
				// / processing gl-category
				if (glCategoryId != null && !glCodeObj.glCategoryId.equals(glCategoryId))
				{
					continue;
				}
				String sqlGLCat = " SELECT * FROM " + GLCategoryBean.TABLENAME + " WHERE " + GLCategoryBean.PKID
						+ " ='" + glCodeObj.glCategoryId.toString() + "' ";
				psGLCat = cn.prepareStatement(sqlGLCat);
				ResultSet rsGLCat = psGLCat.executeQuery();
				if (!rsGLCat.next())
				{
					continue;
				}
				GLCategoryObject glCategoryObj = GLCategoryBean.getObject(rsGLCat, "");
				GeneralLedgerObject glObj = getObject(rsGL, "");
				GeneralLedgerTreeObject glTreeObj = new GeneralLedgerTreeObject(glObj);
				glTreeObj.glCodeObj = glCodeObj;
				glTreeObj.glCategoryObj = glCategoryObj;
				vecGLTree.add(glTreeObj);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLCode);
			cleanup(cn, psGLCat);
		}
		return vecGLTree;
	}

	public static GeneralLedgerObject getObject(ResultSet rs, String prefix) throws Exception
	{
		GeneralLedgerObject theObj = null;
		try
		{
			theObj = new GeneralLedgerObject();
			theObj.pkId = new Integer(rs.getInt("pkid"));
			theObj.glCodeId = new Integer(rs.getInt("glcodeid"));
			theObj.pcCenterId = new Integer(rs.getInt("pccenterid"));
			theObj.batchId = new Integer(rs.getInt("batchid"));
			theObj.name = rs.getString("name");
			theObj.description = rs.getString("description");
			theObj.status = rs.getString("status");
			theObj.options = rs.getString("options");
			theObj.currency = rs.getString("currency");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	public Vector selectGLView(Vector arrPkid, Timestamp dateStart, Timestamp dateEnd) throws SQLException, Exception
	{
		Timestamp dateAfterEnd = TimeFormat.add(dateEnd, 0, 0, 1);
		Vector vecGLView = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement psGL = null;
		PreparedStatement psGLOpenBal = null;
		PreparedStatement psGLEntry = null;
		try
		{
			cn = ds.getConnection();
			for (int cnt1 = 0; cnt1 < arrPkid.size(); cnt1++)
			{
				Integer thePkid = (Integer) arrPkid.get(cnt1);
				String sqlGL = " SELECT cat.pkid AS cat_pkid, cat.real_temp AS cat_rt, cat.postto_section AS cat_pts , "
						+ " codegl.* FROM (SELECT code.pkid AS code_pkid, code.code AS code_code, code.glcategoryid "
						+ " AS code_glcatid, gl.* FROM (SELECT * FROM acc_general_ledger_index WHERE pkid  = '"
						+ thePkid.toString()
						+ "' ) "
						+ " AS gl INNER JOIN acc_glcode_index AS code ON (code.pkid=gl.glcodeid)) AS codegl INNER JOIN "
						+ " acc_glcategory_index AS cat ON (cat.pkid = codegl.code_glcatid); ";
				psGL = cn.prepareStatement(sqlGL);
				String sqlGLOpenBal = " SELECT sum(jent.amount) AS balance FROM (SELECT * FROM "
						+ " acc_journal_entry WHERE glaccid='" + thePkid.toString() + "') AS jent INNER JOIN "
						+ " acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) WHERE "
						+ " jtxn.transactiondate < '" + TimeFormat.strDisplayDate(dateStart) + "';";
				psGLOpenBal = cn.prepareStatement(sqlGLOpenBal);
				String sqlGLEntry = "SELECT jtxn.*, jent.description AS entry_description, jent.amount AS entry_amount "
						+ " FROM (SELECT * FROM acc_journal_entry WHERE glaccid='"
						+ thePkid.toString()
						+ "') AS "
						+ " jent INNER JOIN acc_journal_transaction AS jtxn ON (jent.journaltxnid = jtxn.pkid) "
						+ " WHERE jtxn.transactiondate >= '"
						+ TimeFormat.strDisplayDate(dateStart)
						+ "' AND jtxn.transactiondate < '"
						+ TimeFormat.strDisplayDate(dateAfterEnd)
						+ "' "
						+ " AND jtxn.typeid='"
						+ JournalTransactionBean.TYPEID_POSTED.toString()
						+ "' "
						+ " ORDER BY jtxn.transactiondate, jtxn.pkid;";
				psGLEntry = cn.prepareStatement(sqlGLEntry);
				// / Create general ledger view object
				ResultSet rsGL = psGL.executeQuery();
				ResultSet rsGLOpenBal = psGLOpenBal.executeQuery();
				ResultSet rsGLEntry = psGLEntry.executeQuery();
				// / calculate the opening balance for this period
				if (rsGL.next() && rsGLOpenBal.next())
				{
					GeneralLedgerView glView = new GeneralLedgerView();
					GeneralLedgerObject glObj = getObject(rsGL, "");
					glView.setGL(glObj);
					glView.glCode = rsGL.getString("code_code");
					glView.glCategoryPkid = new Integer(rsGL.getInt("cat_pkid"));
					glView.glCategoryRealTemp = rsGL.getString("cat_rt");
					glView.glCategoryPostSection = rsGL.getString("cat_pts");
					glView.dateStart = dateStart;
					glView.dateEnd = dateEnd;
					glView.balanceOpen = rsGLOpenBal.getBigDecimal("balance");
					if (glView.balanceOpen == null)
					{
						glView.balanceOpen = new BigDecimal(0);
					}
					// glView.balanceClose = new BigDecimal(0);
					while (rsGLEntry.next())
					{
						GeneralLedgerView.Entry entry = new GeneralLedgerView.Entry();
						JournalTransactionObject jtxnObj = JournalTransactionBean.getObject(rsGLEntry, "");
						entry.setJTxn(jtxnObj);
						entry.entryDescription = rsGLEntry.getString("entry_description");
						entry.entryAmount = rsGLEntry.getBigDecimal("entry_amount");
						glView.vecTxn.add(entry);
					}
					vecGLView.add(glView);
				} else
				{
					/* do nothing */
				}
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, psGL);
			cleanup(cn, psGLOpenBal);
			cleanup(cn, psGLEntry);
		}
		return vecGLView;
	}
}
