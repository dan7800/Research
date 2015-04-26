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

public class RoleDomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strBeanTable = "user_roledomain_link";
	protected final String strObjectName = "RoleDomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer roleDomainId; // primary key!!!
	private Integer domainId;
	private Integer roleId;
	private String status;
	private Calendar aDate;

	public Integer getRoleDomainId()
	{
		return this.roleDomainId;
	}

	public void setRoleDomainId(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.roleDomainId;
	}

	public void setPrimaryKey(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer domainid)
	{
		this.domainId = domainid;
	}

	public Integer getRoleId()
	{
		return this.roleId;
	}

	public void setRoleId(Integer roleid)
	{
		this.roleId = roleid;
	}

	public Calendar getTheDate()
	{
		return this.aDate;
	}

	public void setTheDate(Calendar bufADate)
	{
		this.aDate = bufADate;
	}

	public Integer ejbCreate(Integer rlid, Integer dmnid, Calendar bufADate) throws CreateException
	{
		Integer rdid = null;
		Integer bufint = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			bufint = selectRDbyRoleIdAndDomainId(rlid, dmnid);
			if (bufint == null)
			{
				rdid = insertRoleDomain(rlid, dmnid, bufADate);
			}
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rdid != null)
		{
			this.roleDomainId = rdid;
			this.domainId = dmnid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = RoleDomainBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rdid;
	}

	public Integer ejbFindByRoleIdAndDomainId(Integer rlid, Integer dmnid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByRoleIdAndDomainId");
		Integer result;
		try
		{
			result = selectRDbyRoleIdAndDomainId(rlid, dmnid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleIdAndDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByRoleIdAndDomainId");
		if (result != null)
		{
			return result;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain : Role=" + rlid.toString() + " Domain=" + dmnid.toString()
					+ " not found.");
		}
	}

	public Integer ejbFindByPrimaryKey(Integer rdid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rdid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rdid;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain " + rdid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleDomainId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleDomainId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleDomainId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleDomainId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rlid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRoleDomain(this.roleDomainId);
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
		// releaseConnection()
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.roleDomainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.roleDomainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRoleDomain();
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
			storeRoleDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer dmnid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllRoleDomainId()
	{
		try
		{
			Collection bufAL = selectAllRoleDomainId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllRoleDomainId: " + ex);
			return null;
		}
	}

	public Collection ejbFindRoleDomainsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectRoleDomainsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRoleDomainsGiven: " + ex);
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
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertRoleDomain(Integer rlid, Integer dmnid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertRoleDomain: ");
			String findMaxUidStmt = " select roledomainid from " + strBeanTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roledomainid"))
				{
					bufInt = rs.getInt("roledomainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roledomainid is :" + newUid.toString());
			String insertStatement = "insert into " + strBeanTable + " (roledomainid , domainid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, dmnid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, RoleDomainBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created roledomain : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertRoleDomain");
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

	private Integer selectByRoleDomainId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
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

	private Integer selectRDbyRoleIdAndDomainId(Integer roleid, Integer dmnid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable
					+ " where roleid = ? and domainid = ?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, roleid.intValue());
			prepStmt.setInt(2, dmnid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
			}
			// prepStmt.close();;
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

	private boolean selectByPrimaryKey(Integer rdid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strBeanTable + " where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rdid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();;
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

	private void deleteRoleDomain(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strBeanTable + "  " + "where roledomainid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted roledomain : " + rlid);
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

	private void loadRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select roledomainid, domainid, roleid, status, " + " adate " + " from "
					+ strBeanTable + "  where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.roleDomainId = new Integer(rs.getInt("roledomainid"));
				this.domainId = new Integer(rs.getInt("domainid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("roledomainid " + this.roleDomainId.toString());
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

	private Collection selectAllRoleDomainId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private Collection selectRoleDomainsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				Log.printVerbose(" something found ");
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			Log.printVerbose(" nothing found ");
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private void storeRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strBeanTable + "  set roledomainid = ? , " + " domainid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where roledomainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			prepStmt.setInt(2, this.domainId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.roleDomainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing row roledomainid" + this.roleDomainId.toString() + " failed.");
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
} // RoleDomainBean
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

public class RoleDomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strBeanTable = "user_roledomain_link";
	protected final String strObjectName = "RoleDomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer roleDomainId; // primary key!!!
	private Integer domainId;
	private Integer roleId;
	private String status;
	private Calendar aDate;

	public Integer getRoleDomainId()
	{
		return this.roleDomainId;
	}

	public void setRoleDomainId(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.roleDomainId;
	}

	public void setPrimaryKey(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer domainid)
	{
		this.domainId = domainid;
	}

	public Integer getRoleId()
	{
		return this.roleId;
	}

	public void setRoleId(Integer roleid)
	{
		this.roleId = roleid;
	}

	public Calendar getTheDate()
	{
		return this.aDate;
	}

	public void setTheDate(Calendar bufADate)
	{
		this.aDate = bufADate;
	}

	public Integer ejbCreate(Integer rlid, Integer dmnid, Calendar bufADate) throws CreateException
	{
		Integer rdid = null;
		Integer bufint = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			bufint = selectRDbyRoleIdAndDomainId(rlid, dmnid);
			if (bufint == null)
			{
				rdid = insertRoleDomain(rlid, dmnid, bufADate);
			}
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rdid != null)
		{
			this.roleDomainId = rdid;
			this.domainId = dmnid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = RoleDomainBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rdid;
	}

	public Integer ejbFindByRoleIdAndDomainId(Integer rlid, Integer dmnid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByRoleIdAndDomainId");
		Integer result;
		try
		{
			result = selectRDbyRoleIdAndDomainId(rlid, dmnid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleIdAndDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByRoleIdAndDomainId");
		if (result != null)
		{
			return result;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain : Role=" + rlid.toString() + " Domain=" + dmnid.toString()
					+ " not found.");
		}
	}

	public Integer ejbFindByPrimaryKey(Integer rdid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rdid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rdid;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain " + rdid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleDomainId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleDomainId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleDomainId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleDomainId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rlid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRoleDomain(this.roleDomainId);
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
		// releaseConnection()
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.roleDomainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.roleDomainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRoleDomain();
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
			storeRoleDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer dmnid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllRoleDomainId()
	{
		try
		{
			Collection bufAL = selectAllRoleDomainId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllRoleDomainId: " + ex);
			return null;
		}
	}

	public Collection ejbFindRoleDomainsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectRoleDomainsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRoleDomainsGiven: " + ex);
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
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertRoleDomain(Integer rlid, Integer dmnid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertRoleDomain: ");
			String findMaxUidStmt = " select roledomainid from " + strBeanTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roledomainid"))
				{
					bufInt = rs.getInt("roledomainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roledomainid is :" + newUid.toString());
			String insertStatement = "insert into " + strBeanTable + " (roledomainid , domainid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, dmnid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, RoleDomainBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created roledomain : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertRoleDomain");
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

	private Integer selectByRoleDomainId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
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

	private Integer selectRDbyRoleIdAndDomainId(Integer roleid, Integer dmnid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable
					+ " where roleid = ? and domainid = ?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, roleid.intValue());
			prepStmt.setInt(2, dmnid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
			}
			// prepStmt.close();;
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

	private boolean selectByPrimaryKey(Integer rdid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strBeanTable + " where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rdid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();;
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

	private void deleteRoleDomain(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strBeanTable + "  " + "where roledomainid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted roledomain : " + rlid);
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

	private void loadRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select roledomainid, domainid, roleid, status, " + " adate " + " from "
					+ strBeanTable + "  where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.roleDomainId = new Integer(rs.getInt("roledomainid"));
				this.domainId = new Integer(rs.getInt("domainid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("roledomainid " + this.roleDomainId.toString());
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

	private Collection selectAllRoleDomainId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private Collection selectRoleDomainsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				Log.printVerbose(" something found ");
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			Log.printVerbose(" nothing found ");
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private void storeRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strBeanTable + "  set roledomainid = ? , " + " domainid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where roledomainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			prepStmt.setInt(2, this.domainId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.roleDomainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing row roledomainid" + this.roleDomainId.toString() + " failed.");
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
} // RoleDomainBean
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

public class RoleDomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strBeanTable = "user_roledomain_link";
	protected final String strObjectName = "RoleDomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer roleDomainId; // primary key!!!
	private Integer domainId;
	private Integer roleId;
	private String status;
	private Calendar aDate;

	public Integer getRoleDomainId()
	{
		return this.roleDomainId;
	}

	public void setRoleDomainId(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.roleDomainId;
	}

	public void setPrimaryKey(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer domainid)
	{
		this.domainId = domainid;
	}

	public Integer getRoleId()
	{
		return this.roleId;
	}

	public void setRoleId(Integer roleid)
	{
		this.roleId = roleid;
	}

	public Calendar getTheDate()
	{
		return this.aDate;
	}

	public void setTheDate(Calendar bufADate)
	{
		this.aDate = bufADate;
	}

	public Integer ejbCreate(Integer rlid, Integer dmnid, Calendar bufADate) throws CreateException
	{
		Integer rdid = null;
		Integer bufint = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			bufint = selectRDbyRoleIdAndDomainId(rlid, dmnid);
			if (bufint == null)
			{
				rdid = insertRoleDomain(rlid, dmnid, bufADate);
			}
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rdid != null)
		{
			this.roleDomainId = rdid;
			this.domainId = dmnid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = RoleDomainBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rdid;
	}

	public Integer ejbFindByRoleIdAndDomainId(Integer rlid, Integer dmnid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByRoleIdAndDomainId");
		Integer result;
		try
		{
			result = selectRDbyRoleIdAndDomainId(rlid, dmnid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleIdAndDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByRoleIdAndDomainId");
		if (result != null)
		{
			return result;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain : Role=" + rlid.toString() + " Domain=" + dmnid.toString()
					+ " not found.");
		}
	}

	public Integer ejbFindByPrimaryKey(Integer rdid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rdid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rdid;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain " + rdid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleDomainId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleDomainId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleDomainId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleDomainId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rlid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRoleDomain(this.roleDomainId);
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
		// releaseConnection()
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.roleDomainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.roleDomainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRoleDomain();
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
			storeRoleDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer dmnid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllRoleDomainId()
	{
		try
		{
			Collection bufAL = selectAllRoleDomainId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllRoleDomainId: " + ex);
			return null;
		}
	}

	public Collection ejbFindRoleDomainsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectRoleDomainsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRoleDomainsGiven: " + ex);
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
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertRoleDomain(Integer rlid, Integer dmnid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertRoleDomain: ");
			String findMaxUidStmt = " select roledomainid from " + strBeanTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roledomainid"))
				{
					bufInt = rs.getInt("roledomainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roledomainid is :" + newUid.toString());
			String insertStatement = "insert into " + strBeanTable + " (roledomainid , domainid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, dmnid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, RoleDomainBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created roledomain : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertRoleDomain");
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

	private Integer selectByRoleDomainId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
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

	private Integer selectRDbyRoleIdAndDomainId(Integer roleid, Integer dmnid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable
					+ " where roleid = ? and domainid = ?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, roleid.intValue());
			prepStmt.setInt(2, dmnid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
			}
			// prepStmt.close();;
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

	private boolean selectByPrimaryKey(Integer rdid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strBeanTable + " where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rdid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();;
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

	private void deleteRoleDomain(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strBeanTable + "  " + "where roledomainid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted roledomain : " + rlid);
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

	private void loadRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select roledomainid, domainid, roleid, status, " + " adate " + " from "
					+ strBeanTable + "  where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.roleDomainId = new Integer(rs.getInt("roledomainid"));
				this.domainId = new Integer(rs.getInt("domainid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("roledomainid " + this.roleDomainId.toString());
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

	private Collection selectAllRoleDomainId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private Collection selectRoleDomainsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				Log.printVerbose(" something found ");
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			Log.printVerbose(" nothing found ");
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private void storeRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strBeanTable + "  set roledomainid = ? , " + " domainid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where roledomainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			prepStmt.setInt(2, this.domainId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.roleDomainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing row roledomainid" + this.roleDomainId.toString() + " failed.");
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
} // RoleDomainBean
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

public class RoleDomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strBeanTable = "user_roledomain_link";
	protected final String strObjectName = "RoleDomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer roleDomainId; // primary key!!!
	private Integer domainId;
	private Integer roleId;
	private String status;
	private Calendar aDate;

	public Integer getRoleDomainId()
	{
		return this.roleDomainId;
	}

	public void setRoleDomainId(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.roleDomainId;
	}

	public void setPrimaryKey(Integer rdid)
	{
		this.roleDomainId = rdid;
	}

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer domainid)
	{
		this.domainId = domainid;
	}

	public Integer getRoleId()
	{
		return this.roleId;
	}

	public void setRoleId(Integer roleid)
	{
		this.roleId = roleid;
	}

	public Calendar getTheDate()
	{
		return this.aDate;
	}

	public void setTheDate(Calendar bufADate)
	{
		this.aDate = bufADate;
	}

	public Integer ejbCreate(Integer rlid, Integer dmnid, Calendar bufADate) throws CreateException
	{
		Integer rdid = null;
		Integer bufint = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			bufint = selectRDbyRoleIdAndDomainId(rlid, dmnid);
			if (bufint == null)
			{
				rdid = insertRoleDomain(rlid, dmnid, bufADate);
			}
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rdid != null)
		{
			this.roleDomainId = rdid;
			this.domainId = dmnid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = RoleDomainBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rdid;
	}

	public Integer ejbFindByRoleIdAndDomainId(Integer rlid, Integer dmnid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByRoleIdAndDomainId");
		Integer result;
		try
		{
			result = selectRDbyRoleIdAndDomainId(rlid, dmnid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleIdAndDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByRoleIdAndDomainId");
		if (result != null)
		{
			return result;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain : Role=" + rlid.toString() + " Domain=" + dmnid.toString()
					+ " not found.");
		}
	}

	public Integer ejbFindByPrimaryKey(Integer rdid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rdid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rdid;
		} else
		{
			throw new ObjectNotFoundException("RoleDomain " + rdid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleDomainId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleDomainId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleDomainId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleDomainId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleDomainId");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rlid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRoleDomain(this.roleDomainId);
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
		// releaseConnection()
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.roleDomainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.roleDomainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRoleDomain();
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
			storeRoleDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer dmnid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllRoleDomainId()
	{
		try
		{
			Collection bufAL = selectAllRoleDomainId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllRoleDomainId: " + ex);
			return null;
		}
	}

	public Collection ejbFindRoleDomainsGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectRoleDomainsGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRoleDomainsGiven: " + ex);
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
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertRoleDomain(Integer rlid, Integer dmnid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertRoleDomain: ");
			String findMaxUidStmt = " select roledomainid from " + strBeanTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roledomainid"))
				{
					bufInt = rs.getInt("roledomainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roledomainid is :" + newUid.toString());
			String insertStatement = "insert into " + strBeanTable + " (roledomainid , domainid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, dmnid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, RoleDomainBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created roledomain : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertRoleDomain");
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

	private Integer selectByRoleDomainId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
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

	private Integer selectRDbyRoleIdAndDomainId(Integer roleid, Integer dmnid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roledomainid " + "from " + strBeanTable
					+ " where roleid = ? and domainid = ?";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, roleid.intValue());
			prepStmt.setInt(2, dmnid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("roledomainid"));
			}
			// prepStmt.close();;
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

	private boolean selectByPrimaryKey(Integer rdid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strBeanTable + " where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rdid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();;
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

	private void deleteRoleDomain(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strBeanTable + "  " + "where roledomainid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted roledomain : " + rlid);
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

	private void loadRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select roledomainid, domainid, roleid, status, " + " adate " + " from "
					+ strBeanTable + "  where roledomainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.roleDomainId = new Integer(rs.getInt("roledomainid"));
				this.domainId = new Integer(rs.getInt("domainid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("roledomainid " + this.roleDomainId.toString());
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

	private Collection selectAllRoleDomainId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private Collection selectRoleDomainsGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList roledomainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select roledomainid from " + strBeanTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				Log.printVerbose(" something found ");
				roledomainSet.add(new Integer(rs.getInt(1)));
			}
			Log.printVerbose(" nothing found ");
			// prepStmt.close();;
			// releaseConnection();
			return roledomainSet;
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

	private void storeRoleDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strBeanTable + "  set roledomainid = ? , " + " domainid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where roledomainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.roleDomainId.intValue());
			prepStmt.setInt(2, this.domainId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.roleDomainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing row roledomainid" + this.roleDomainId.toString() + " failed.");
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
} // RoleDomainBean
