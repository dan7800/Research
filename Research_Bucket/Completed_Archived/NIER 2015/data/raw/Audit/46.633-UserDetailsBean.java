// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.user;

import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class UserDetailsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "userdetails_index";
	protected final String strObjectName = "UserDetailsBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// members ----------------------------------------------
	private Integer mPrimaryKeyId; // primary key!!!
	private Integer mUserId;
	private Calendar mDoB;
	private String mSex;
	private String mEthnic;
	private String mICNumber;
	private String mICType;
	private Calendar mLastVerified;

	public Integer getPrimaryKeyId()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKeyId(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer usrid)
	{
		this.mUserId = usrid;
	}

	public Calendar getDateOfBirth()
	{
		return this.mDoB;
	}

	public void setDateOfBirth(Calendar bufDateOfBirth)
	{
		this.mDoB = bufDateOfBirth;
	}

	public String getSex()
	{
		return this.mSex;
	}

	public void setSex(String sex)
	{
		this.mSex = sex;
	}

	public String getEthnic()
	{
		return this.mEthnic;
	}

	public void setEthnic(String bufEthnic)
	{
		this.mEthnic = bufEthnic;
	}

	public String getICNumber()
	{
		return this.mICNumber;
	}

	public void setICNumber(String bufICNo)
	{
		this.mICNumber = bufICNo;
	}

	public String getICType()
	{
		return this.mICType;
	}

	public void setICType(String bufICType)
	{
		this.mICType = bufICType;
	}

	public Calendar getLastVerified()
	{
		return this.mLastVerified;
	}

	public void setLastVerified(Calendar bufLastVerified)
	{
		this.mLastVerified = bufLastVerified;
	}

	public Integer getPrimaryKey()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer ejbCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(usrid, bufDob, bufSex, bufEthnic, bufICNo, bufICType, bufLastVerified);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPrimaryKeyId = pkid;
			this.mUserId = usrid;
			this.mDoB = bufDob;
			this.mSex = bufSex;
			this.mEthnic = bufEthnic;
			this.mICNumber = bufICNo;
			this.mICType = bufICType;
			this.mLastVerified = bufLastVerified;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("UserDetails " + pkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByPrimaryKeyId(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKeyId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByPrimaryKeyId(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKeyId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKeyId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRow(this.mPrimaryKeyId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "Leaving ejbRemove");
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mPrimaryKeyId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPrimaryKeyId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified)
	{
	}

	public Collection ejbFindAllPrimaryKeyId()
	{
		try
		{
			Collection bufAL = selectAllPrimaryKeyId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllPrimaryKeyId: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/** ***************** Database Routines ************************ */
	private void makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			if (!con.isClosed())
			{
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxUidStmt = " select pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("pkid"))
				{
					bufInt = rs.getInt("pkid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new pkid is :" + newUid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid , userid , dob, sex, "
					+ " ethnic, ic_no, ic_type, lastverified) " + " values ( ?, ? , ? , ?, ?, ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setDate(3, new java.sql.Date(bufDob.getTime().getTime()));
			insertStmt.setString(4, bufSex);
			insertStmt.setString(5, bufEthnic);
			insertStmt.setString(6, bufICNo);
			insertStmt.setString(7, bufICType);
			insertStmt.setDate(8, new java.sql.Date(bufLastVerified.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userdetails : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// releaseConnection();
			return newUid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	private Integer selectByPrimaryKeyId(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select pkid " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("pkid"));
			}
			// prepStmt.close();
			// releaseConnection();
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteRow(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userdetails : " + pkid);
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select pkid, userid, dob, sex, " + " ethnic, ic_no, ic_type , lastverified "
					+ " from " + strObjectTable + "  where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPrimaryKeyId = new Integer(rs.getInt("pkid"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mDoB = Calendar.getInstance();
				this.mDoB.setTime(rs.getDate("dob"));
				this.mSex = rs.getString("sex");
				this.mEthnic = rs.getString("ethnic");
				this.mICNumber = rs.getString("ic_no");
				this.mICType = rs.getString("ic_type");
				this.mLastVerified = Calendar.getInstance();
				this.mLastVerified.setTime(rs.getDate("lastverified"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("pkid " + this.mPrimaryKeyId.toString());
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllPrimaryKeyId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + "  set pkid = ? , " + " userid =  ? , "
					+ " dob = ? , " + " sex = ? , " + " ethnic = ? , " + " ic_no = ? , " + " ic_type = ? , "
					+ " lastverified = ?  " + " where pkid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			prepStmt.setInt(2, this.mUserId.intValue());
			prepStmt.setDate(3, new java.sql.Date(this.mDoB.getTime().getTime()));
			prepStmt.setString(4, this.mSex);
			prepStmt.setString(5, this.mEthnic);
			prepStmt.setString(6, this.mICNumber);
			prepStmt.setString(7, this.mICType);
			prepStmt.setDate(8, new java.sql.Date(this.mLastVerified.getTime().getTime()));
			prepStmt.setInt(9, this.mPrimaryKeyId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row pkid" + this.mPrimaryKeyId.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserDetailsBean
// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.user;

import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class UserDetailsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "userdetails_index";
	protected final String strObjectName = "UserDetailsBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// members ----------------------------------------------
	private Integer mPrimaryKeyId; // primary key!!!
	private Integer mUserId;
	private Calendar mDoB;
	private String mSex;
	private String mEthnic;
	private String mICNumber;
	private String mICType;
	private Calendar mLastVerified;

	public Integer getPrimaryKeyId()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKeyId(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer usrid)
	{
		this.mUserId = usrid;
	}

	public Calendar getDateOfBirth()
	{
		return this.mDoB;
	}

	public void setDateOfBirth(Calendar bufDateOfBirth)
	{
		this.mDoB = bufDateOfBirth;
	}

	public String getSex()
	{
		return this.mSex;
	}

	public void setSex(String sex)
	{
		this.mSex = sex;
	}

	public String getEthnic()
	{
		return this.mEthnic;
	}

	public void setEthnic(String bufEthnic)
	{
		this.mEthnic = bufEthnic;
	}

	public String getICNumber()
	{
		return this.mICNumber;
	}

	public void setICNumber(String bufICNo)
	{
		this.mICNumber = bufICNo;
	}

	public String getICType()
	{
		return this.mICType;
	}

	public void setICType(String bufICType)
	{
		this.mICType = bufICType;
	}

	public Calendar getLastVerified()
	{
		return this.mLastVerified;
	}

	public void setLastVerified(Calendar bufLastVerified)
	{
		this.mLastVerified = bufLastVerified;
	}

	public Integer getPrimaryKey()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer ejbCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(usrid, bufDob, bufSex, bufEthnic, bufICNo, bufICType, bufLastVerified);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPrimaryKeyId = pkid;
			this.mUserId = usrid;
			this.mDoB = bufDob;
			this.mSex = bufSex;
			this.mEthnic = bufEthnic;
			this.mICNumber = bufICNo;
			this.mICType = bufICType;
			this.mLastVerified = bufLastVerified;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("UserDetails " + pkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByPrimaryKeyId(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKeyId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByPrimaryKeyId(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKeyId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKeyId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRow(this.mPrimaryKeyId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "Leaving ejbRemove");
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mPrimaryKeyId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPrimaryKeyId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified)
	{
	}

	public Collection ejbFindAllPrimaryKeyId()
	{
		try
		{
			Collection bufAL = selectAllPrimaryKeyId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllPrimaryKeyId: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/** ***************** Database Routines ************************ */
	private void makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			if (!con.isClosed())
			{
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxUidStmt = " select pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("pkid"))
				{
					bufInt = rs.getInt("pkid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new pkid is :" + newUid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid , userid , dob, sex, "
					+ " ethnic, ic_no, ic_type, lastverified) " + " values ( ?, ? , ? , ?, ?, ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setDate(3, new java.sql.Date(bufDob.getTime().getTime()));
			insertStmt.setString(4, bufSex);
			insertStmt.setString(5, bufEthnic);
			insertStmt.setString(6, bufICNo);
			insertStmt.setString(7, bufICType);
			insertStmt.setDate(8, new java.sql.Date(bufLastVerified.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userdetails : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// releaseConnection();
			return newUid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	private Integer selectByPrimaryKeyId(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select pkid " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("pkid"));
			}
			// prepStmt.close();
			// releaseConnection();
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteRow(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userdetails : " + pkid);
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select pkid, userid, dob, sex, " + " ethnic, ic_no, ic_type , lastverified "
					+ " from " + strObjectTable + "  where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPrimaryKeyId = new Integer(rs.getInt("pkid"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mDoB = Calendar.getInstance();
				this.mDoB.setTime(rs.getDate("dob"));
				this.mSex = rs.getString("sex");
				this.mEthnic = rs.getString("ethnic");
				this.mICNumber = rs.getString("ic_no");
				this.mICType = rs.getString("ic_type");
				this.mLastVerified = Calendar.getInstance();
				this.mLastVerified.setTime(rs.getDate("lastverified"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("pkid " + this.mPrimaryKeyId.toString());
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllPrimaryKeyId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + "  set pkid = ? , " + " userid =  ? , "
					+ " dob = ? , " + " sex = ? , " + " ethnic = ? , " + " ic_no = ? , " + " ic_type = ? , "
					+ " lastverified = ?  " + " where pkid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			prepStmt.setInt(2, this.mUserId.intValue());
			prepStmt.setDate(3, new java.sql.Date(this.mDoB.getTime().getTime()));
			prepStmt.setString(4, this.mSex);
			prepStmt.setString(5, this.mEthnic);
			prepStmt.setString(6, this.mICNumber);
			prepStmt.setString(7, this.mICType);
			prepStmt.setDate(8, new java.sql.Date(this.mLastVerified.getTime().getTime()));
			prepStmt.setInt(9, this.mPrimaryKeyId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row pkid" + this.mPrimaryKeyId.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserDetailsBean
// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.user;

import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class UserDetailsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "userdetails_index";
	protected final String strObjectName = "UserDetailsBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// members ----------------------------------------------
	private Integer mPrimaryKeyId; // primary key!!!
	private Integer mUserId;
	private Calendar mDoB;
	private String mSex;
	private String mEthnic;
	private String mICNumber;
	private String mICType;
	private Calendar mLastVerified;

	public Integer getPrimaryKeyId()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKeyId(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer usrid)
	{
		this.mUserId = usrid;
	}

	public Calendar getDateOfBirth()
	{
		return this.mDoB;
	}

	public void setDateOfBirth(Calendar bufDateOfBirth)
	{
		this.mDoB = bufDateOfBirth;
	}

	public String getSex()
	{
		return this.mSex;
	}

	public void setSex(String sex)
	{
		this.mSex = sex;
	}

	public String getEthnic()
	{
		return this.mEthnic;
	}

	public void setEthnic(String bufEthnic)
	{
		this.mEthnic = bufEthnic;
	}

	public String getICNumber()
	{
		return this.mICNumber;
	}

	public void setICNumber(String bufICNo)
	{
		this.mICNumber = bufICNo;
	}

	public String getICType()
	{
		return this.mICType;
	}

	public void setICType(String bufICType)
	{
		this.mICType = bufICType;
	}

	public Calendar getLastVerified()
	{
		return this.mLastVerified;
	}

	public void setLastVerified(Calendar bufLastVerified)
	{
		this.mLastVerified = bufLastVerified;
	}

	public Integer getPrimaryKey()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer ejbCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(usrid, bufDob, bufSex, bufEthnic, bufICNo, bufICType, bufLastVerified);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPrimaryKeyId = pkid;
			this.mUserId = usrid;
			this.mDoB = bufDob;
			this.mSex = bufSex;
			this.mEthnic = bufEthnic;
			this.mICNumber = bufICNo;
			this.mICType = bufICType;
			this.mLastVerified = bufLastVerified;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("UserDetails " + pkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByPrimaryKeyId(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKeyId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByPrimaryKeyId(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKeyId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKeyId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRow(this.mPrimaryKeyId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "Leaving ejbRemove");
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mPrimaryKeyId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPrimaryKeyId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified)
	{
	}

	public Collection ejbFindAllPrimaryKeyId()
	{
		try
		{
			Collection bufAL = selectAllPrimaryKeyId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllPrimaryKeyId: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/** ***************** Database Routines ************************ */
	private void makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			if (!con.isClosed())
			{
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxUidStmt = " select pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("pkid"))
				{
					bufInt = rs.getInt("pkid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new pkid is :" + newUid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid , userid , dob, sex, "
					+ " ethnic, ic_no, ic_type, lastverified) " + " values ( ?, ? , ? , ?, ?, ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setDate(3, new java.sql.Date(bufDob.getTime().getTime()));
			insertStmt.setString(4, bufSex);
			insertStmt.setString(5, bufEthnic);
			insertStmt.setString(6, bufICNo);
			insertStmt.setString(7, bufICType);
			insertStmt.setDate(8, new java.sql.Date(bufLastVerified.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userdetails : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// releaseConnection();
			return newUid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	private Integer selectByPrimaryKeyId(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select pkid " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("pkid"));
			}
			// prepStmt.close();
			// releaseConnection();
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteRow(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userdetails : " + pkid);
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select pkid, userid, dob, sex, " + " ethnic, ic_no, ic_type , lastverified "
					+ " from " + strObjectTable + "  where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPrimaryKeyId = new Integer(rs.getInt("pkid"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mDoB = Calendar.getInstance();
				this.mDoB.setTime(rs.getDate("dob"));
				this.mSex = rs.getString("sex");
				this.mEthnic = rs.getString("ethnic");
				this.mICNumber = rs.getString("ic_no");
				this.mICType = rs.getString("ic_type");
				this.mLastVerified = Calendar.getInstance();
				this.mLastVerified.setTime(rs.getDate("lastverified"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("pkid " + this.mPrimaryKeyId.toString());
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllPrimaryKeyId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + "  set pkid = ? , " + " userid =  ? , "
					+ " dob = ? , " + " sex = ? , " + " ethnic = ? , " + " ic_no = ? , " + " ic_type = ? , "
					+ " lastverified = ?  " + " where pkid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			prepStmt.setInt(2, this.mUserId.intValue());
			prepStmt.setDate(3, new java.sql.Date(this.mDoB.getTime().getTime()));
			prepStmt.setString(4, this.mSex);
			prepStmt.setString(5, this.mEthnic);
			prepStmt.setString(6, this.mICNumber);
			prepStmt.setString(7, this.mICType);
			prepStmt.setDate(8, new java.sql.Date(this.mLastVerified.getTime().getTime()));
			prepStmt.setInt(9, this.mPrimaryKeyId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row pkid" + this.mPrimaryKeyId.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserDetailsBean
// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.user;

import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class UserDetailsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "userdetails_index";
	protected final String strObjectName = "UserDetailsBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// members ----------------------------------------------
	private Integer mPrimaryKeyId; // primary key!!!
	private Integer mUserId;
	private Calendar mDoB;
	private String mSex;
	private String mEthnic;
	private String mICNumber;
	private String mICType;
	private Calendar mLastVerified;

	public Integer getPrimaryKeyId()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKeyId(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer usrid)
	{
		this.mUserId = usrid;
	}

	public Calendar getDateOfBirth()
	{
		return this.mDoB;
	}

	public void setDateOfBirth(Calendar bufDateOfBirth)
	{
		this.mDoB = bufDateOfBirth;
	}

	public String getSex()
	{
		return this.mSex;
	}

	public void setSex(String sex)
	{
		this.mSex = sex;
	}

	public String getEthnic()
	{
		return this.mEthnic;
	}

	public void setEthnic(String bufEthnic)
	{
		this.mEthnic = bufEthnic;
	}

	public String getICNumber()
	{
		return this.mICNumber;
	}

	public void setICNumber(String bufICNo)
	{
		this.mICNumber = bufICNo;
	}

	public String getICType()
	{
		return this.mICType;
	}

	public void setICType(String bufICType)
	{
		this.mICType = bufICType;
	}

	public Calendar getLastVerified()
	{
		return this.mLastVerified;
	}

	public void setLastVerified(Calendar bufLastVerified)
	{
		this.mLastVerified = bufLastVerified;
	}

	public Integer getPrimaryKey()
	{
		return this.mPrimaryKeyId;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPrimaryKeyId = pkid;
	}

	public Integer ejbCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(usrid, bufDob, bufSex, bufEthnic, bufICNo, bufICType, bufLastVerified);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPrimaryKeyId = pkid;
			this.mUserId = usrid;
			this.mDoB = bufDob;
			this.mSex = bufSex;
			this.mEthnic = bufEthnic;
			this.mICNumber = bufICNo;
			this.mICType = bufICType;
			this.mLastVerified = bufLastVerified;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("UserDetails " + pkid.toString() + " not found.");
		}
	}

	public Integer ejbFindByPrimaryKeyId(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKeyId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByPrimaryKeyId(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKeyId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKeyId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRow(this.mPrimaryKeyId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "Leaving ejbRemove");
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mPrimaryKeyId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPrimaryKeyId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified)
	{
	}

	public Collection ejbFindAllPrimaryKeyId()
	{
		try
		{
			Collection bufAL = selectAllPrimaryKeyId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllPrimaryKeyId: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/** ***************** Database Routines ************************ */
	private void makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			if (!con.isClosed())
			{
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer usrid, Calendar bufDob, String bufSex, String bufEthnic, String bufICNo,
			String bufICType, Calendar bufLastVerified) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxUidStmt = " select pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("pkid"))
				{
					bufInt = rs.getInt("pkid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new pkid is :" + newUid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid , userid , dob, sex, "
					+ " ethnic, ic_no, ic_type, lastverified) " + " values ( ?, ? , ? , ?, ?, ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setDate(3, new java.sql.Date(bufDob.getTime().getTime()));
			insertStmt.setString(4, bufSex);
			insertStmt.setString(5, bufEthnic);
			insertStmt.setString(6, bufICNo);
			insertStmt.setString(7, bufICType);
			insertStmt.setDate(8, new java.sql.Date(bufLastVerified.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userdetails : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// releaseConnection();
			return newUid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	private Integer selectByPrimaryKeyId(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select pkid " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("pkid"));
			}
			// prepStmt.close();
			// releaseConnection();
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteRow(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userdetails : " + pkid);
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select pkid, userid, dob, sex, " + " ethnic, ic_no, ic_type , lastverified "
					+ " from " + strObjectTable + "  where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPrimaryKeyId = new Integer(rs.getInt("pkid"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mDoB = Calendar.getInstance();
				this.mDoB.setTime(rs.getDate("dob"));
				this.mSex = rs.getString("sex");
				this.mEthnic = rs.getString("ethnic");
				this.mICNumber = rs.getString("ic_no");
				this.mICType = rs.getString("ic_type");
				this.mLastVerified = Calendar.getInstance();
				this.mLastVerified.setTime(rs.getDate("lastverified"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("pkid " + this.mPrimaryKeyId.toString());
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllPrimaryKeyId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + "  set pkid = ? , " + " userid =  ? , "
					+ " dob = ? , " + " sex = ? , " + " ethnic = ? , " + " ic_no = ? , " + " ic_type = ? , "
					+ " lastverified = ?  " + " where pkid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPrimaryKeyId.intValue());
			prepStmt.setInt(2, this.mUserId.intValue());
			prepStmt.setDate(3, new java.sql.Date(this.mDoB.getTime().getTime()));
			prepStmt.setString(4, this.mSex);
			prepStmt.setString(5, this.mEthnic);
			prepStmt.setString(6, this.mICNumber);
			prepStmt.setString(7, this.mICType);
			prepStmt.setDate(8, new java.sql.Date(this.mLastVerified.getTime().getTime()));
			prepStmt.setInt(9, this.mPrimaryKeyId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row pkid" + this.mPrimaryKeyId.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserDetailsBean
