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

import java.math.BigDecimal;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class JournalEntryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private static final String TABLENAME = "acc_journal_entry";
	private final String strObjectName = "JournalEntryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String JTXNID = "journaltxnid";
	public static final String GLACCID = "glaccid";
	public static final String DESCRIPTION = "description";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String FOREXPAIR = "forexpair";
	public static final String FOREXRATE = "forexrate";
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String DEBIT = "debit";
	public static final String CREDIT = "credit";
	public static final BigDecimal FXRATE_DEFAULT = new BigDecimal("1.00");
	public static final String FXPAIR_DEFAULT = "";
	public static final String MODULENAME = "acc";
	public JournalEntryObject valObj;

	/*
	 * private Long pkId; private Long journalTxnId; private Integer glId;
	 * private String description; private String currency; private BigDecimal
	 * amount; private String forexPair; private BigDecimal forexRate; private
	 * String status;
	 */
	public Long getPkId()
	{
		return this.valObj.pkId;
	}

	public void setPkId(Long pkid)
	{
		this.valObj.pkId = pkid;
	}

	public Long getJournalTxnId()
	{
		return this.valObj.journalTxnId;
	}

	public void setJournalTxnId(Long jtxnid)
	{
		this.valObj.journalTxnId = jtxnid;
	}

	public Integer getGLId()
	{
		return this.valObj.glId;
	}

	public void setGLId(Integer glaccid)
	{
		this.valObj.glId = glaccid;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public BigDecimal getAmount()
	{
		return this.valObj.amount;
	}

	public void setAmount(BigDecimal bdAmount)
	{
		this.valObj.amount = bdAmount;
	}

	public String getForexPair()
	{
		return this.valObj.forexPair;
	}

	public void setForexPair(String strPair)
	{
		this.valObj.forexPair = strPair;
	}

	public BigDecimal getForexRate()
	{
		return this.valObj.forexRate;
	}

	public void setForexRate(BigDecimal bdFxRate)
	{
		this.valObj.forexRate = bdFxRate;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public JournalEntryObject getValueObject()
	{
		return this.valObj;
	}

	/*
	 * { JournalEntryObject jeo = new JournalEntryObject(); jeo.pkId =
	 * this.pkId; jeo.journalTxnId = this.journalTxnId; jeo.glId = this.glId;
	 * jeo.description = this.description; jeo.currency = this.currency;
	 * jeo.amount = this.amount; jeo.forexPair = this.forexPair; jeo.forexRate =
	 * this.forexRate; jeo.status = this.status; return jeo; }
	 */
	public void setValueObject(JournalEntryObject jeo) throws Exception
	{
		if (jeo == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.journalTxnId = jeo.journalTxnId;
		this.valObj.glId = jeo.glId;
		this.valObj.description = jeo.description;
		this.valObj.currency = jeo.currency;
		this.valObj.amount = jeo.amount;
		this.valObj.forexPair = jeo.forexPair;
		this.valObj.forexRate = jeo.forexRate;
		this.valObj.status = jeo.status;
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

	public Long ejbCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate) throws CreateException
	{
		Long newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		description = StringManup.truncate(description,1000);
		newPkId = insertNewRow(journalTxnId, glId, description, currency, amount, forexPair, forexRate);
		if (newPkId != null)
		{
			this.valObj = new JournalEntryObject();
			this.valObj.pkId = newPkId;
			this.valObj.journalTxnId = journalTxnId;
			this.valObj.glId = glId;
			this.valObj.description = description;
			this.valObj.currency = currency;
			this.valObj.amount = amount;
			this.valObj.forexPair = forexPair;
			this.valObj.forexRate = forexRate;
			this.valObj.status = JournalEntryBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
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
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new JournalEntryObject();
		this.valObj.pkId = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
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

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
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

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGivenDate");
		Vector vecValObj = selectValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGivenDate");
		return vecValObj;
	}

	public BigDecimal ejbHomeGetAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAmountValueObjectsGivenDate");
		BigDecimal totalAmount = selectAmountValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAmountValueObjectsGivenDate");
		return totalAmount;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ***************** Database Routines ************************ */
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

	private Long insertNewRow(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		
		description = StringManup.truncate(description,1000);
		
		try
		{
			// Long newPkId = getNextPKId();
			String sqlStatement = " INSERT INTO "
					+ TABLENAME
					+ " ("
					+ "pkid, journaltxnid, glaccid, description, currency, amount, forexpair, forexrate, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			Long newPkId = getNextPKId(cn);
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, newPkId.longValue());
			ps.setLong(2, journalTxnId.longValue());
			ps.setInt(3, glId.intValue());
			ps.setString(4, description);
			ps.setString(5, currency);
			ps.setBigDecimal(6, amount);
			ps.setString(7, forexPair);
			ps.setBigDecimal(8, forexRate);
			ps.setString(9, JournalEntryBean.ACTIVE);
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

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}

	/*
	 * private Long getNextPKId() { DataSource ds = getDataSource(); Connection
	 * cn = null; PreparedStatement ps = null; try { String sqlStatement =
	 * "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME; cn =
	 * ds.getConnection(); ps = cn.prepareStatement(sqlStatement); ResultSet rs =
	 * ps.executeQuery();
	 * 
	 * rs.next(); long max = rs.getLong("max_pkid"); if (max == 0) { max = 1000; }
	 * else { max += 1; } return new Long(max); } catch (Exception e) { throw
	 * new EJBException(e); } finally { cleanup(cn, ps); } }
	 */
	private void deleteObject(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, this.valObj.pkId.longValue());
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET journaltxnid = ?, glaccid = ?, description = ?,"
					+ " currency = ?, amount = ?, forexpair = ?, forexrate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			this.valObj.failSafe();
			ps.setLong(1, this.valObj.journalTxnId.longValue());
			ps.setInt(2, this.valObj.glId.intValue());
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.currency);
			ps.setBigDecimal(5, this.valObj.amount);
			ps.setString(6, this.valObj.forexPair);
			ps.setBigDecimal(7, this.valObj.forexRate);
			ps.setString(8, this.valObj.status);
			ps.setLong(9, this.valObj.pkId.longValue());
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

	private boolean selectByPrimaryKey(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
					objectSet.add(new Long(rs.getLong(1)));
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
			sqlStatement += " ORDER BY pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Long(rs.getLong(1)));
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			sqlStatement += " ORDER BY pkid ";
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
				JournalEntryObject jeo = getObject(rs, "");
				vecValObj.add(jeo);
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

	private Vector selectValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT " + TABLENAME + ".pkid, " + TABLENAME + ".journaltxnid, " + TABLENAME
					+ ".glaccid, " + TABLENAME + ".description, " + TABLENAME + ".currency, " + TABLENAME + ".amount, "
					+ TABLENAME + ".forexpair, " + TABLENAME + ".forexrate, " + TABLENAME + ".status FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			sqlStatement += " ORDER BY " + TABLENAME + ".pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				JournalEntryObject jeo = new JournalEntryObject();
				jeo.pkId = new Long(rs.getLong("pkid"));
				jeo.journalTxnId = new Long(rs.getLong("journaltxnid"));
				jeo.glId = new Integer(rs.getInt("glaccid"));
				jeo.description = rs.getString("description");
				jeo.currency = rs.getString("currency");
				jeo.amount = rs.getBigDecimal("amount");
				jeo.forexPair = rs.getString("forexpair");
				jeo.forexRate = rs.getBigDecimal("forexrate");
				jeo.status = rs.getString("status");
				vecValObj.add(jeo);
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

	private BigDecimal selectAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		BigDecimal totalAmount = new BigDecimal("0.00");
		try
		{
			String sqlStatement = "SELECT SUM(" + TABLENAME + ".amount) AS totalamount FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				BigDecimal ta = rs.getBigDecimal("totalamount");
				if (ta != null)
				{
					totalAmount = ta;
				}
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return totalAmount;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		DataSource ds = getDataSource();
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = ds.getConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				JournalEntryObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			// if (prepStmt != null)
			// { prepStmt.close(); }
			// closeConnection(con);
			cleanup(con, prepStmt);
		}
		return result;
	}

	public static JournalEntryObject getObject(ResultSet rs, String prefix) throws Exception
	{
		JournalEntryObject theObj = null;
		try
		{
			theObj = new JournalEntryObject();
			theObj.pkId = new Long(rs.getLong("pkid"));
			theObj.journalTxnId = new Long(rs.getLong(JTXNID));
			theObj.glId = new Integer(rs.getInt(GLACCID));
			theObj.description = rs.getString(DESCRIPTION);
			theObj.currency = rs.getString(CURRENCY);
			theObj.amount = rs.getBigDecimal(AMOUNT);
			theObj.forexPair = rs.getString(FOREXPAIR);
			theObj.forexRate = rs.getBigDecimal(FOREXRATE);
			theObj.status = rs.getString(STATUS);
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
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.accounting;

import java.math.BigDecimal;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class JournalEntryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private static final String TABLENAME = "acc_journal_entry";
	private final String strObjectName = "JournalEntryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String JTXNID = "journaltxnid";
	public static final String GLACCID = "glaccid";
	public static final String DESCRIPTION = "description";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String FOREXPAIR = "forexpair";
	public static final String FOREXRATE = "forexrate";
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String DEBIT = "debit";
	public static final String CREDIT = "credit";
	public static final BigDecimal FXRATE_DEFAULT = new BigDecimal("1.00");
	public static final String FXPAIR_DEFAULT = "";
	public static final String MODULENAME = "acc";
	public JournalEntryObject valObj;

	/*
	 * private Long pkId; private Long journalTxnId; private Integer glId;
	 * private String description; private String currency; private BigDecimal
	 * amount; private String forexPair; private BigDecimal forexRate; private
	 * String status;
	 */
	public Long getPkId()
	{
		return this.valObj.pkId;
	}

	public void setPkId(Long pkid)
	{
		this.valObj.pkId = pkid;
	}

	public Long getJournalTxnId()
	{
		return this.valObj.journalTxnId;
	}

	public void setJournalTxnId(Long jtxnid)
	{
		this.valObj.journalTxnId = jtxnid;
	}

	public Integer getGLId()
	{
		return this.valObj.glId;
	}

	public void setGLId(Integer glaccid)
	{
		this.valObj.glId = glaccid;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public BigDecimal getAmount()
	{
		return this.valObj.amount;
	}

	public void setAmount(BigDecimal bdAmount)
	{
		this.valObj.amount = bdAmount;
	}

	public String getForexPair()
	{
		return this.valObj.forexPair;
	}

	public void setForexPair(String strPair)
	{
		this.valObj.forexPair = strPair;
	}

	public BigDecimal getForexRate()
	{
		return this.valObj.forexRate;
	}

	public void setForexRate(BigDecimal bdFxRate)
	{
		this.valObj.forexRate = bdFxRate;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public JournalEntryObject getValueObject()
	{
		return this.valObj;
	}

	/*
	 * { JournalEntryObject jeo = new JournalEntryObject(); jeo.pkId =
	 * this.pkId; jeo.journalTxnId = this.journalTxnId; jeo.glId = this.glId;
	 * jeo.description = this.description; jeo.currency = this.currency;
	 * jeo.amount = this.amount; jeo.forexPair = this.forexPair; jeo.forexRate =
	 * this.forexRate; jeo.status = this.status; return jeo; }
	 */
	public void setValueObject(JournalEntryObject jeo) throws Exception
	{
		if (jeo == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.journalTxnId = jeo.journalTxnId;
		this.valObj.glId = jeo.glId;
		this.valObj.description = jeo.description;
		this.valObj.currency = jeo.currency;
		this.valObj.amount = jeo.amount;
		this.valObj.forexPair = jeo.forexPair;
		this.valObj.forexRate = jeo.forexRate;
		this.valObj.status = jeo.status;
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

	public Long ejbCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate) throws CreateException
	{
		Long newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(journalTxnId, glId, description, currency, amount, forexPair, forexRate);
		if (newPkId != null)
		{
			this.valObj = new JournalEntryObject();
			this.valObj.pkId = newPkId;
			this.valObj.journalTxnId = journalTxnId;
			this.valObj.glId = glId;
			this.valObj.description = description;
			this.valObj.currency = currency;
			this.valObj.amount = amount;
			this.valObj.forexPair = forexPair;
			this.valObj.forexRate = forexRate;
			this.valObj.status = JournalEntryBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
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
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new JournalEntryObject();
		this.valObj.pkId = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
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

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
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

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGivenDate");
		Vector vecValObj = selectValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGivenDate");
		return vecValObj;
	}

	public BigDecimal ejbHomeGetAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAmountValueObjectsGivenDate");
		BigDecimal totalAmount = selectAmountValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAmountValueObjectsGivenDate");
		return totalAmount;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ***************** Database Routines ************************ */
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

	private Long insertNewRow(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			// Long newPkId = getNextPKId();
			String sqlStatement = " INSERT INTO "
					+ TABLENAME
					+ " ("
					+ "pkid, journaltxnid, glaccid, description, currency, amount, forexpair, forexrate, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			Long newPkId = getNextPKId(cn);
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, newPkId.longValue());
			ps.setLong(2, journalTxnId.longValue());
			ps.setInt(3, glId.intValue());
			ps.setString(4, description);
			ps.setString(5, currency);
			ps.setBigDecimal(6, amount);
			ps.setString(7, forexPair);
			ps.setBigDecimal(8, forexRate);
			ps.setString(9, JournalEntryBean.ACTIVE);
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

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}

	/*
	 * private Long getNextPKId() { DataSource ds = getDataSource(); Connection
	 * cn = null; PreparedStatement ps = null; try { String sqlStatement =
	 * "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME; cn =
	 * ds.getConnection(); ps = cn.prepareStatement(sqlStatement); ResultSet rs =
	 * ps.executeQuery();
	 * 
	 * rs.next(); long max = rs.getLong("max_pkid"); if (max == 0) { max = 1000; }
	 * else { max += 1; } return new Long(max); } catch (Exception e) { throw
	 * new EJBException(e); } finally { cleanup(cn, ps); } }
	 */
	private void deleteObject(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, this.valObj.pkId.longValue());
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET journaltxnid = ?, glaccid = ?, description = ?,"
					+ " currency = ?, amount = ?, forexpair = ?, forexrate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, this.valObj.journalTxnId.longValue());
			ps.setInt(2, this.valObj.glId.intValue());
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.currency);
			ps.setBigDecimal(5, this.valObj.amount);
			ps.setString(6, this.valObj.forexPair);
			ps.setBigDecimal(7, this.valObj.forexRate);
			ps.setString(8, this.valObj.status);
			ps.setLong(9, this.valObj.pkId.longValue());
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

	private boolean selectByPrimaryKey(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
					objectSet.add(new Long(rs.getLong(1)));
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
			sqlStatement += " ORDER BY pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Long(rs.getLong(1)));
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			sqlStatement += " ORDER BY pkid ";
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
				JournalEntryObject jeo = getObject(rs, "");
				vecValObj.add(jeo);
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

	private Vector selectValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT " + TABLENAME + ".pkid, " + TABLENAME + ".journaltxnid, " + TABLENAME
					+ ".glaccid, " + TABLENAME + ".description, " + TABLENAME + ".currency, " + TABLENAME + ".amount, "
					+ TABLENAME + ".forexpair, " + TABLENAME + ".forexrate, " + TABLENAME + ".status FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			sqlStatement += " ORDER BY " + TABLENAME + ".pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				JournalEntryObject jeo = new JournalEntryObject();
				jeo.pkId = new Long(rs.getLong("pkid"));
				jeo.journalTxnId = new Long(rs.getLong("journaltxnid"));
				jeo.glId = new Integer(rs.getInt("glaccid"));
				jeo.description = rs.getString("description");
				jeo.currency = rs.getString("currency");
				jeo.amount = rs.getBigDecimal("amount");
				jeo.forexPair = rs.getString("forexpair");
				jeo.forexRate = rs.getBigDecimal("forexrate");
				jeo.status = rs.getString("status");
				vecValObj.add(jeo);
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

	private BigDecimal selectAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		BigDecimal totalAmount = new BigDecimal("0.00");
		try
		{
			String sqlStatement = "SELECT SUM(" + TABLENAME + ".amount) AS totalamount FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				BigDecimal ta = rs.getBigDecimal("totalamount");
				if (ta != null)
				{
					totalAmount = ta;
				}
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return totalAmount;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		DataSource ds = getDataSource();
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = ds.getConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				JournalEntryObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			// if (prepStmt != null)
			// { prepStmt.close(); }
			// closeConnection(con);
			cleanup(con, prepStmt);
		}
		return result;
	}

	public static JournalEntryObject getObject(ResultSet rs, String prefix) throws Exception
	{
		JournalEntryObject theObj = null;
		try
		{
			theObj = new JournalEntryObject();
			theObj.pkId = new Long(rs.getLong("pkid"));
			theObj.journalTxnId = new Long(rs.getLong(JTXNID));
			theObj.glId = new Integer(rs.getInt(GLACCID));
			theObj.description = rs.getString(DESCRIPTION);
			theObj.currency = rs.getString(CURRENCY);
			theObj.amount = rs.getBigDecimal(AMOUNT);
			theObj.forexPair = rs.getString(FOREXPAIR);
			theObj.forexRate = rs.getBigDecimal(FOREXRATE);
			theObj.status = rs.getString(STATUS);
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
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.accounting;

import java.math.BigDecimal;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class JournalEntryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private static final String TABLENAME = "acc_journal_entry";
	private final String strObjectName = "JournalEntryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String JTXNID = "journaltxnid";
	public static final String GLACCID = "glaccid";
	public static final String DESCRIPTION = "description";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String FOREXPAIR = "forexpair";
	public static final String FOREXRATE = "forexrate";
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String DEBIT = "debit";
	public static final String CREDIT = "credit";
	public static final BigDecimal FXRATE_DEFAULT = new BigDecimal("1.00");
	public static final String FXPAIR_DEFAULT = "";
	public static final String MODULENAME = "acc";
	public JournalEntryObject valObj;

	/*
	 * private Long pkId; private Long journalTxnId; private Integer glId;
	 * private String description; private String currency; private BigDecimal
	 * amount; private String forexPair; private BigDecimal forexRate; private
	 * String status;
	 */
	public Long getPkId()
	{
		return this.valObj.pkId;
	}

	public void setPkId(Long pkid)
	{
		this.valObj.pkId = pkid;
	}

	public Long getJournalTxnId()
	{
		return this.valObj.journalTxnId;
	}

	public void setJournalTxnId(Long jtxnid)
	{
		this.valObj.journalTxnId = jtxnid;
	}

	public Integer getGLId()
	{
		return this.valObj.glId;
	}

	public void setGLId(Integer glaccid)
	{
		this.valObj.glId = glaccid;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public BigDecimal getAmount()
	{
		return this.valObj.amount;
	}

	public void setAmount(BigDecimal bdAmount)
	{
		this.valObj.amount = bdAmount;
	}

	public String getForexPair()
	{
		return this.valObj.forexPair;
	}

	public void setForexPair(String strPair)
	{
		this.valObj.forexPair = strPair;
	}

	public BigDecimal getForexRate()
	{
		return this.valObj.forexRate;
	}

	public void setForexRate(BigDecimal bdFxRate)
	{
		this.valObj.forexRate = bdFxRate;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public JournalEntryObject getValueObject()
	{
		return this.valObj;
	}

	/*
	 * { JournalEntryObject jeo = new JournalEntryObject(); jeo.pkId =
	 * this.pkId; jeo.journalTxnId = this.journalTxnId; jeo.glId = this.glId;
	 * jeo.description = this.description; jeo.currency = this.currency;
	 * jeo.amount = this.amount; jeo.forexPair = this.forexPair; jeo.forexRate =
	 * this.forexRate; jeo.status = this.status; return jeo; }
	 */
	public void setValueObject(JournalEntryObject jeo) throws Exception
	{
		if (jeo == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.journalTxnId = jeo.journalTxnId;
		this.valObj.glId = jeo.glId;
		this.valObj.description = jeo.description;
		this.valObj.currency = jeo.currency;
		this.valObj.amount = jeo.amount;
		this.valObj.forexPair = jeo.forexPair;
		this.valObj.forexRate = jeo.forexRate;
		this.valObj.status = jeo.status;
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

	public Long ejbCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate) throws CreateException
	{
		Long newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(journalTxnId, glId, description, currency, amount, forexPair, forexRate);
		if (newPkId != null)
		{
			this.valObj = new JournalEntryObject();
			this.valObj.pkId = newPkId;
			this.valObj.journalTxnId = journalTxnId;
			this.valObj.glId = glId;
			this.valObj.description = description;
			this.valObj.currency = currency;
			this.valObj.amount = amount;
			this.valObj.forexPair = forexPair;
			this.valObj.forexRate = forexRate;
			this.valObj.status = JournalEntryBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
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
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new JournalEntryObject();
		this.valObj.pkId = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
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

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
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

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGivenDate");
		Vector vecValObj = selectValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGivenDate");
		return vecValObj;
	}

	public BigDecimal ejbHomeGetAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAmountValueObjectsGivenDate");
		BigDecimal totalAmount = selectAmountValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAmountValueObjectsGivenDate");
		return totalAmount;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ***************** Database Routines ************************ */
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

	private Long insertNewRow(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			// Long newPkId = getNextPKId();
			String sqlStatement = " INSERT INTO "
					+ TABLENAME
					+ " ("
					+ "pkid, journaltxnid, glaccid, description, currency, amount, forexpair, forexrate, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			Long newPkId = getNextPKId(cn);
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, newPkId.longValue());
			ps.setLong(2, journalTxnId.longValue());
			ps.setInt(3, glId.intValue());
			ps.setString(4, description);
			ps.setString(5, currency);
			ps.setBigDecimal(6, amount);
			ps.setString(7, forexPair);
			ps.setBigDecimal(8, forexRate);
			ps.setString(9, JournalEntryBean.ACTIVE);
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

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}

	/*
	 * private Long getNextPKId() { DataSource ds = getDataSource(); Connection
	 * cn = null; PreparedStatement ps = null; try { String sqlStatement =
	 * "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME; cn =
	 * ds.getConnection(); ps = cn.prepareStatement(sqlStatement); ResultSet rs =
	 * ps.executeQuery();
	 * 
	 * rs.next(); long max = rs.getLong("max_pkid"); if (max == 0) { max = 1000; }
	 * else { max += 1; } return new Long(max); } catch (Exception e) { throw
	 * new EJBException(e); } finally { cleanup(cn, ps); } }
	 */
	private void deleteObject(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, this.valObj.pkId.longValue());
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET journaltxnid = ?, glaccid = ?, description = ?,"
					+ " currency = ?, amount = ?, forexpair = ?, forexrate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, this.valObj.journalTxnId.longValue());
			ps.setInt(2, this.valObj.glId.intValue());
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.currency);
			ps.setBigDecimal(5, this.valObj.amount);
			ps.setString(6, this.valObj.forexPair);
			ps.setBigDecimal(7, this.valObj.forexRate);
			ps.setString(8, this.valObj.status);
			ps.setLong(9, this.valObj.pkId.longValue());
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

	private boolean selectByPrimaryKey(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
					objectSet.add(new Long(rs.getLong(1)));
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
			sqlStatement += " ORDER BY pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				rs.beforeFirst();
				while (rs.next())
				{
					objectSet.add(new Long(rs.getLong(1)));
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			sqlStatement += " ORDER BY pkid ";
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
				JournalEntryObject jeo = getObject(rs, "");
				vecValObj.add(jeo);
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

	private Vector selectValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT " + TABLENAME + ".pkid, " + TABLENAME + ".journaltxnid, " + TABLENAME
					+ ".glaccid, " + TABLENAME + ".description, " + TABLENAME + ".currency, " + TABLENAME + ".amount, "
					+ TABLENAME + ".forexpair, " + TABLENAME + ".forexrate, " + TABLENAME + ".status FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			sqlStatement += " ORDER BY " + TABLENAME + ".pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				JournalEntryObject jeo = new JournalEntryObject();
				jeo.pkId = new Long(rs.getLong("pkid"));
				jeo.journalTxnId = new Long(rs.getLong("journaltxnid"));
				jeo.glId = new Integer(rs.getInt("glaccid"));
				jeo.description = rs.getString("description");
				jeo.currency = rs.getString("currency");
				jeo.amount = rs.getBigDecimal("amount");
				jeo.forexPair = rs.getString("forexpair");
				jeo.forexRate = rs.getBigDecimal("forexrate");
				jeo.status = rs.getString("status");
				vecValObj.add(jeo);
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

	private BigDecimal selectAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		BigDecimal totalAmount = new BigDecimal("0.00");
		try
		{
			String sqlStatement = "SELECT SUM(" + TABLENAME + ".amount) AS totalamount FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				BigDecimal ta = rs.getBigDecimal("totalamount");
				if (ta != null)
				{
					totalAmount = ta;
				}
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return totalAmount;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		DataSource ds = getDataSource();
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = ds.getConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				JournalEntryObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			// if (prepStmt != null)
			// { prepStmt.close(); }
			// closeConnection(con);
			cleanup(con, prepStmt);
		}
		return result;
	}

	public static JournalEntryObject getObject(ResultSet rs, String prefix) throws Exception
	{
		JournalEntryObject theObj = null;
		try
		{
			theObj = new JournalEntryObject();
			theObj.pkId = new Long(rs.getLong("pkid"));
			theObj.journalTxnId = new Long(rs.getLong(JTXNID));
			theObj.glId = new Integer(rs.getInt(GLACCID));
			theObj.description = rs.getString(DESCRIPTION);
			theObj.currency = rs.getString(CURRENCY);
			theObj.amount = rs.getBigDecimal(AMOUNT);
			theObj.forexPair = rs.getString(FOREXPAIR);
			theObj.forexRate = rs.getBigDecimal(FOREXRATE);
			theObj.status = rs.getString(STATUS);
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
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.accounting;

import java.math.BigDecimal;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class JournalEntryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private static final String TABLENAME = "acc_journal_entry";
	private final String strObjectName = "JournalEntryBean: ";
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String JTXNID = "journaltxnid";
	public static final String GLACCID = "glaccid";
	public static final String DESCRIPTION = "description";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String FOREXPAIR = "forexpair";
	public static final String FOREXRATE = "forexrate";
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String DEBIT = "debit";
	public static final String CREDIT = "credit";
	public static final BigDecimal FXRATE_DEFAULT = new BigDecimal("1.00");
	public static final String FXPAIR_DEFAULT = "";
	public static final String MODULENAME = "acc";
	public JournalEntryObject valObj;

	/*
	 * private Long pkId; private Long journalTxnId; private Integer glId;
	 * private String description; private String currency; private BigDecimal
	 * amount; private String forexPair; private BigDecimal forexRate; private
	 * String status;
	 */
	public Long getPkId()
	{
		return this.valObj.pkId;
	}

	public void setPkId(Long pkid)
	{
		this.valObj.pkId = pkid;
	}

	public Long getJournalTxnId()
	{
		return this.valObj.journalTxnId;
	}

	public void setJournalTxnId(Long jtxnid)
	{
		this.valObj.journalTxnId = jtxnid;
	}

	public Integer getGLId()
	{
		return this.valObj.glId;
	}

	public void setGLId(Integer glaccid)
	{
		this.valObj.glId = glaccid;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public String getCurrency()
	{
		return this.valObj.currency;
	}

	public void setCurrency(String strCurr)
	{
		this.valObj.currency = strCurr;
	}

	public BigDecimal getAmount()
	{
		return this.valObj.amount;
	}

	public void setAmount(BigDecimal bdAmount)
	{
		this.valObj.amount = bdAmount;
	}

	public String getForexPair()
	{
		return this.valObj.forexPair;
	}

	public void setForexPair(String strPair)
	{
		this.valObj.forexPair = strPair;
	}

	public BigDecimal getForexRate()
	{
		return this.valObj.forexRate;
	}

	public void setForexRate(BigDecimal bdFxRate)
	{
		this.valObj.forexRate = bdFxRate;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public JournalEntryObject getValueObject()
	{
		return this.valObj;
	}

	/*
	 * { JournalEntryObject jeo = new JournalEntryObject(); jeo.pkId =
	 * this.pkId; jeo.journalTxnId = this.journalTxnId; jeo.glId = this.glId;
	 * jeo.description = this.description; jeo.currency = this.currency;
	 * jeo.amount = this.amount; jeo.forexPair = this.forexPair; jeo.forexRate =
	 * this.forexRate; jeo.status = this.status; return jeo; }
	 */
	public void setValueObject(JournalEntryObject jeo) throws Exception
	{
		if (jeo == null)
		{
			throw new Exception("Object undefined");
		}
		this.valObj.journalTxnId = jeo.journalTxnId;
		this.valObj.glId = jeo.glId;
		this.valObj.description = jeo.description;
		this.valObj.currency = jeo.currency;
		this.valObj.amount = jeo.amount;
		this.valObj.forexPair = jeo.forexPair;
		this.valObj.forexRate = jeo.forexRate;
		this.valObj.status = jeo.status;
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

	public Long ejbCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate) throws CreateException
	{
		Long newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(journalTxnId, glId, description, currency, amount, forexPair, forexRate);
		if (newPkId != null)
		{
			this.valObj = new JournalEntryObject();
			this.valObj.pkId = newPkId;
			this.valObj.journalTxnId = journalTxnId;
			this.valObj.glId = glId;
			this.valObj.description = description;
			this.valObj.currency = currency;
			this.valObj.amount = amount;
			this.valObj.forexPair = forexPair;
			this.valObj.forexRate = forexRate;
			this.valObj.status = JournalEntryBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbPostCreate(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
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
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new JournalEntryObject();
		this.valObj.pkId = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
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

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
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

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGivenDate");
		Vector vecValObj = selectValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGivenDate");
		return vecValObj;
	}

	public BigDecimal ejbHomeGetAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAmountValueObjectsGivenDate");
		BigDecimal totalAmount = selectAmountValueObjectsGivenDate(fieldName1, value1, fieldName2, value2, ts1, ts2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAmountValueObjectsGivenDate");
		return totalAmount;
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ***************** Database Routines ************************ */
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

	private Long insertNewRow(Long journalTxnId, Integer glId, String description, String currency, BigDecimal amount,
			String forexPair, BigDecimal forexRate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			// Long newPkId = getNextPKId();
			String sqlStatement = " INSERT INTO "
					+ TABLENAME
					+ " ("
					+ "pkid, journaltxnid, glaccid, description, currency, amount, forexpair, forexrate, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			Long newPkId = getNextPKId(cn);
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, newPkId.longValue());
			ps.setLong(2, journalTxnId.longValue());
			ps.setInt(3, glId.intValue());
			ps.setString(4, description);
			ps.setString(5, currency);
			ps.setBigDecimal(6, amount);
			ps.setString(7, forexPair);
			ps.setBigDecimal(8, forexRate);
			ps.setString(9, JournalEntryBean.ACTIVE);
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

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}

	/*
	 * private Long getNextPKId() { DataSource ds = getDataSource(); Connection
	 * cn = null; PreparedStatement ps = null; try { String sqlStatement =
	 * "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME; cn =
	 * ds.getConnection(); ps = cn.prepareStatement(sqlStatement); ResultSet rs =
	 * ps.executeQuery();
	 * 
	 * rs.next(); long max = rs.getLong("max_pkid"); if (max == 0) { max = 1000; }
	 * else { max += 1; } return new Long(max); } catch (Exception e) { throw
	 * new EJBException(e); } finally { cleanup(cn, ps); } }
	 */
	private void deleteObject(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, this.valObj.pkId.longValue());
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
			String sqlStatement = "UPDATE " + TABLENAME + " SET journaltxnid = ?, glaccid = ?, description = ?,"
					+ " currency = ?, amount = ?, forexpair = ?, forexrate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, this.valObj.journalTxnId.longValue());
			ps.setInt(2, this.valObj.glId.intValue());
			ps.setString(3, this.valObj.description);
			ps.setString(4, this.valObj.currency);
			ps.setBigDecimal(5, this.valObj.amount);
			ps.setString(6, this.valObj.forexPair);
			ps.setBigDecimal(7, this.valObj.forexRate);
			ps.setString(8, this.valObj.status);
			ps.setLong(9, this.valObj.pkId.longValue());
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

	private boolean selectByPrimaryKey(Long pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setLong(1, pkid.longValue());
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
					objectSet.add(new Long(rs.getLong(1)));
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
			sqlStatement += " ORDER BY pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Long(rs.getLong(1)));
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
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			sqlStatement += " ORDER BY pkid ";
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
				JournalEntryObject jeo = getObject(rs, "");
				vecValObj.add(jeo);
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

	private Vector selectValueObjectsGivenDate(String fieldName1, String value1, String fieldName2, String value2,
			Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT " + TABLENAME + ".pkid, " + TABLENAME + ".journaltxnid, " + TABLENAME
					+ ".glaccid, " + TABLENAME + ".description, " + TABLENAME + ".currency, " + TABLENAME + ".amount, "
					+ TABLENAME + ".forexpair, " + TABLENAME + ".forexrate, " + TABLENAME + ".status FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			sqlStatement += " ORDER BY " + TABLENAME + ".pkid ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				JournalEntryObject jeo = new JournalEntryObject();
				jeo.pkId = new Long(rs.getLong("pkid"));
				jeo.journalTxnId = new Long(rs.getLong("journaltxnid"));
				jeo.glId = new Integer(rs.getInt("glaccid"));
				jeo.description = rs.getString("description");
				jeo.currency = rs.getString("currency");
				jeo.amount = rs.getBigDecimal("amount");
				jeo.forexPair = rs.getString("forexpair");
				jeo.forexRate = rs.getBigDecimal("forexrate");
				jeo.status = rs.getString("status");
				vecValObj.add(jeo);
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

	private BigDecimal selectAmountValueObjectsGivenDate(String fieldName1, String value1, String fieldName2,
			String value2, Timestamp ts1, Timestamp ts2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		BigDecimal totalAmount = new BigDecimal("0.00");
		try
		{
			String sqlStatement = "SELECT SUM(" + TABLENAME + ".amount) AS totalamount FROM " + TABLENAME
					+ ", acc_journal_transaction WHERE " + TABLENAME
					+ ".journaltxnid = acc_journal_transaction.pkid AND " + TABLENAME + "." + fieldName1 + " = ?";
			String addendum = " acc_journal_transaction.transactiondate >= ? AND acc_journal_transaction.transactiondate < ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ? AND " + addendum;
			} else
			{
				sqlStatement = sqlStatement + " AND " + addendum;
			}
			Log.printVerbose(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
				ps.setTimestamp(3, ts1);
				ps.setTimestamp(4, ts2);
			} else
			{
				ps.setTimestamp(2, ts1);
				ps.setTimestamp(3, ts2);
			}
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				BigDecimal ta = rs.getBigDecimal("totalamount");
				if (ta != null)
				{
					totalAmount = ta;
				}
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return totalAmount;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		DataSource ds = getDataSource();
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = ds.getConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				JournalEntryObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			// if (prepStmt != null)
			// { prepStmt.close(); }
			// closeConnection(con);
			cleanup(con, prepStmt);
		}
		return result;
	}

	public static JournalEntryObject getObject(ResultSet rs, String prefix) throws Exception
	{
		JournalEntryObject theObj = null;
		try
		{
			theObj = new JournalEntryObject();
			theObj.pkId = new Long(rs.getLong("pkid"));
			theObj.journalTxnId = new Long(rs.getLong(JTXNID));
			theObj.glId = new Integer(rs.getInt(GLACCID));
			theObj.description = rs.getString(DESCRIPTION);
			theObj.currency = rs.getString(CURRENCY);
			theObj.amount = rs.getBigDecimal(AMOUNT);
			theObj.forexPair = rs.getString(FOREXPAIR);
			theObj.forexRate = rs.getBigDecimal(FOREXRATE);
			theObj.status = rs.getString(STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}
}
