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

public class DomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_domain_index";
	protected final String strObjectName = "DomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer domainId; // primary key!!!
	private String domainName;
	private String title;
	private String description;
	private String status;

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer uid)
	{
		this.domainId = uid;
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
		return this.domainId;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.domainId = rid;
	}

	public String getDomainName()
	{
		return this.domainName;
	}

	public void setDomainName(String dmnname)
	{
		this.domainName = dmnname;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String ttl)
	{
		this.title = ttl;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String desc)
	{
		this.description = desc;
	}

	public Integer ejbCreate(String dmnname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertDomain(dmnname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.domainName = dmnname;
			this.title = lTitle;
			this.description = desc;
			this.status = "active";
			this.domainId = rid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rid;
	}

	public Integer ejbFindByPrimaryKey(Integer rid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByDomainName(String dmnname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByDomainName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByDomainName(dmnname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByDomainName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByDomainName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + dmnname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteDomain(this.domainName);
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
		this.domainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.domainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadDomain();
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
			storeDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String domainName, String title, String description)
	{
	}

	public Collection ejbFindAllDomainNames()
	{
		try
		{
			Collection bufAL = selectAllDomainNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectDomainsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsByRoleName(String roleName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectDomainsByRoleName(roleName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
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

	private Integer insertDomain(String dmnname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select domainid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("domainid"))
				{
					bufInt = rs.getInt("domainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new domainid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (domainid , domainname , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, dmnname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, DomainBean.ACTIVE);
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created domain : " + dmnname + "First title : " + lTitle);
			Log.printVerbose(strObjectName + "leaving insertDomain");
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

	private Integer selectByDomainName(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select domainid " + "from " + strUserTable + " where domainname = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnname);
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("domainid"));
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

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rid.intValue());
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

	private void deleteDomain(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where domainname = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, dmnname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted domain : " + dmnname);
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

	private void loadDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select domainname, title , description, status, " + " domainid " + "from "
					+ strUserTable + "  where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, domainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.domainName = rs.getString(1);
				this.title = rs.getString(2);
				this.description = rs.getString(3);
				this.status = rs.getString(4);
				this.domainId = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("domainName " + domainName + " domainid: " + domainId.toString()
						+ " not found in database.");
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

	private Collection selectAllDomainNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsByRoleName(String roleName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			ArrayList domainSet = new ArrayList();
			String selectStatement = " select user_domain_index.domainid from user_domain_index where user_domain_index.domainid in ( select user_roledomain_link.domainid from user_roledomain_link where user_roledomain_link.roleid in ( select user_role_index.roleid from user_role_index where user_role_index.rolename = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, roleName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private void storeDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set domainname = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where domainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.domainName);
			prepStmt.setString(2, this.title);
			prepStmt.setString(3, this.description);
			prepStmt.setString(4, this.status);
			prepStmt.setInt(5, domainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row domainname " + this.domainName + " failed." + " domainId :"
						+ this.domainId);
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
} // DomainBean
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

public class DomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_domain_index";
	protected final String strObjectName = "DomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer domainId; // primary key!!!
	private String domainName;
	private String title;
	private String description;
	private String status;

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer uid)
	{
		this.domainId = uid;
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
		return this.domainId;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.domainId = rid;
	}

	public String getDomainName()
	{
		return this.domainName;
	}

	public void setDomainName(String dmnname)
	{
		this.domainName = dmnname;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String ttl)
	{
		this.title = ttl;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String desc)
	{
		this.description = desc;
	}

	public Integer ejbCreate(String dmnname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertDomain(dmnname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.domainName = dmnname;
			this.title = lTitle;
			this.description = desc;
			this.status = "active";
			this.domainId = rid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rid;
	}

	public Integer ejbFindByPrimaryKey(Integer rid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByDomainName(String dmnname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByDomainName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByDomainName(dmnname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByDomainName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByDomainName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + dmnname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteDomain(this.domainName);
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
		this.domainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.domainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadDomain();
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
			storeDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String domainName, String title, String description)
	{
	}

	public Collection ejbFindAllDomainNames()
	{
		try
		{
			Collection bufAL = selectAllDomainNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectDomainsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsByRoleName(String roleName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectDomainsByRoleName(roleName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
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

	private Integer insertDomain(String dmnname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select domainid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("domainid"))
				{
					bufInt = rs.getInt("domainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new domainid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (domainid , domainname , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, dmnname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, DomainBean.ACTIVE);
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created domain : " + dmnname + "First title : " + lTitle);
			Log.printVerbose(strObjectName + "leaving insertDomain");
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

	private Integer selectByDomainName(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select domainid " + "from " + strUserTable + " where domainname = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnname);
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("domainid"));
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

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rid.intValue());
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

	private void deleteDomain(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where domainname = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, dmnname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted domain : " + dmnname);
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

	private void loadDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select domainname, title , description, status, " + " domainid " + "from "
					+ strUserTable + "  where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, domainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.domainName = rs.getString(1);
				this.title = rs.getString(2);
				this.description = rs.getString(3);
				this.status = rs.getString(4);
				this.domainId = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("domainName " + domainName + " domainid: " + domainId.toString()
						+ " not found in database.");
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

	private Collection selectAllDomainNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsByRoleName(String roleName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			ArrayList domainSet = new ArrayList();
			String selectStatement = " select user_domain_index.domainid from user_domain_index where user_domain_index.domainid in ( select user_roledomain_link.domainid from user_roledomain_link where user_roledomain_link.roleid in ( select user_role_index.roleid from user_role_index where user_role_index.rolename = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, roleName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private void storeDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set domainname = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where domainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.domainName);
			prepStmt.setString(2, this.title);
			prepStmt.setString(3, this.description);
			prepStmt.setString(4, this.status);
			prepStmt.setInt(5, domainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row domainname " + this.domainName + " failed." + " domainId :"
						+ this.domainId);
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
} // DomainBean
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

public class DomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_domain_index";
	protected final String strObjectName = "DomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer domainId; // primary key!!!
	private String domainName;
	private String title;
	private String description;
	private String status;

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer uid)
	{
		this.domainId = uid;
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
		return this.domainId;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.domainId = rid;
	}

	public String getDomainName()
	{
		return this.domainName;
	}

	public void setDomainName(String dmnname)
	{
		this.domainName = dmnname;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String ttl)
	{
		this.title = ttl;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String desc)
	{
		this.description = desc;
	}

	public Integer ejbCreate(String dmnname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertDomain(dmnname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.domainName = dmnname;
			this.title = lTitle;
			this.description = desc;
			this.status = "active";
			this.domainId = rid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rid;
	}

	public Integer ejbFindByPrimaryKey(Integer rid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByDomainName(String dmnname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByDomainName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByDomainName(dmnname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByDomainName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByDomainName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + dmnname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteDomain(this.domainName);
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
		this.domainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.domainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadDomain();
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
			storeDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String domainName, String title, String description)
	{
	}

	public Collection ejbFindAllDomainNames()
	{
		try
		{
			Collection bufAL = selectAllDomainNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectDomainsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsByRoleName(String roleName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectDomainsByRoleName(roleName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
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

	private Integer insertDomain(String dmnname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select domainid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("domainid"))
				{
					bufInt = rs.getInt("domainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new domainid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (domainid , domainname , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, dmnname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, DomainBean.ACTIVE);
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created domain : " + dmnname + "First title : " + lTitle);
			Log.printVerbose(strObjectName + "leaving insertDomain");
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

	private Integer selectByDomainName(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select domainid " + "from " + strUserTable + " where domainname = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnname);
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("domainid"));
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

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rid.intValue());
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

	private void deleteDomain(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where domainname = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, dmnname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted domain : " + dmnname);
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

	private void loadDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select domainname, title , description, status, " + " domainid " + "from "
					+ strUserTable + "  where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, domainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.domainName = rs.getString(1);
				this.title = rs.getString(2);
				this.description = rs.getString(3);
				this.status = rs.getString(4);
				this.domainId = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("domainName " + domainName + " domainid: " + domainId.toString()
						+ " not found in database.");
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

	private Collection selectAllDomainNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsByRoleName(String roleName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			ArrayList domainSet = new ArrayList();
			String selectStatement = " select user_domain_index.domainid from user_domain_index where user_domain_index.domainid in ( select user_roledomain_link.domainid from user_roledomain_link where user_roledomain_link.roleid in ( select user_role_index.roleid from user_role_index where user_role_index.rolename = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, roleName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private void storeDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set domainname = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where domainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.domainName);
			prepStmt.setString(2, this.title);
			prepStmt.setString(3, this.description);
			prepStmt.setString(4, this.status);
			prepStmt.setInt(5, domainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row domainname " + this.domainName + " failed." + " domainId :"
						+ this.domainId);
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
} // DomainBean
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

public class DomainBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_domain_index";
	protected final String strObjectName = "DomainBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// members ----------------------------------------------
	private Integer domainId; // primary key!!!
	private String domainName;
	private String title;
	private String description;
	private String status;

	public Integer getDomainId()
	{
		return this.domainId;
	}

	public void setDomainId(Integer uid)
	{
		this.domainId = uid;
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
		return this.domainId;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.domainId = rid;
	}

	public String getDomainName()
	{
		return this.domainName;
	}

	public void setDomainName(String dmnname)
	{
		this.domainName = dmnname;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String ttl)
	{
		this.title = ttl;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String desc)
	{
		this.description = desc;
	}

	public Integer ejbCreate(String dmnname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertDomain(dmnname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.domainName = dmnname;
			this.title = lTitle;
			this.description = desc;
			this.status = "active";
			this.domainId = rid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return rid;
	}

	public Integer ejbFindByPrimaryKey(Integer rid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(rid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return rid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByDomainName(String dmnname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByDomainName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByDomainName(dmnname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByDomainName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByDomainName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + dmnname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteDomain(this.domainName);
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
		this.domainId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.domainId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadDomain();
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
			storeDomain();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String domainName, String title, String description)
	{
	}

	public Collection ejbFindAllDomainNames()
	{
		try
		{
			Collection bufAL = selectAllDomainNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectDomainsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindDomainsByRoleName(String roleName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectDomainsByRoleName(roleName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
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

	private Integer insertDomain(String dmnname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select domainid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("domainid"))
				{
					bufInt = rs.getInt("domainid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new domainid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (domainid , domainname , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, dmnname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, DomainBean.ACTIVE);
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created domain : " + dmnname + "First title : " + lTitle);
			Log.printVerbose(strObjectName + "leaving insertDomain");
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

	private Integer selectByDomainName(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select domainid " + "from " + strUserTable + " where domainname = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnname);
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("domainid"));
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

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rid.intValue());
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

	private void deleteDomain(String dmnname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where domainname = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, dmnname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted domain : " + dmnname);
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

	private void loadDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select domainname, title , description, status, " + " domainid " + "from "
					+ strUserTable + "  where domainid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, domainId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.domainName = rs.getString(1);
				this.title = rs.getString(2);
				this.description = rs.getString(3);
				this.status = rs.getString(4);
				this.domainId = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("domainName " + domainName + " domainid: " + domainId.toString()
						+ " not found in database.");
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

	private Collection selectAllDomainNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList domainSet = new ArrayList();
			makeConnection();
			String selectStatement = "select domainid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private Collection selectDomainsByRoleName(String roleName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			ArrayList domainSet = new ArrayList();
			String selectStatement = " select user_domain_index.domainid from user_domain_index where user_domain_index.domainid in ( select user_roledomain_link.domainid from user_roledomain_link where user_roledomain_link.roleid in ( select user_role_index.roleid from user_role_index where user_role_index.rolename = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, roleName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				domainSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return domainSet;
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

	private void storeDomain() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set domainname = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where domainid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.domainName);
			prepStmt.setString(2, this.title);
			prepStmt.setString(3, this.description);
			prepStmt.setString(4, this.status);
			prepStmt.setInt(5, domainId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row domainname " + this.domainName + " failed." + " domainId :"
						+ this.domainId);
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
} // DomainBean
