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

public class RoleBean implements EntityBean
{
	// Member Variables
	public static final String ROLEID = "roleid";
	public static final String ROLENAME = "rolename";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";

	public static final String ROLENAME_SUPPLIER = "supplier";

	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "user_role_index";
	protected final String strObjectName = "RoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// CONSTANT for ROLEID
	public static final Integer ROLEID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	private RoleObject valObj;

	public RoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(RoleObject newVal)
	{
		Integer roleid = this.valObj.roleid;
		this.valObj = newVal;
		this.valObj.roleid = roleid;
	}

	public Integer getRoleId()
	{
		return this.valObj.roleid;
	}

	public void setRoleId(Integer uid)
	{
		this.valObj.roleid = uid;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.valObj.roleid;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.valObj.roleid = rid;
	}

	public String getRoleName()
	{
		return this.valObj.rolename;
	}

	public void setRoleName(String rname)
	{
		this.valObj.rolename = rname;
	}

	public String getTitle()
	{
		return this.valObj.title;
	}

	public void setTitle(String ttl)
	{
		this.valObj.title = ttl;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public Integer ejbCreate(String rname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertRole(rname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.valObj = new RoleObject();
			this.valObj.rolename = rname;
			this.valObj.title = lTitle;
			this.valObj.description = desc;
			this.valObj.status = "active";
			this.valObj.roleid = rid;
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
			throw new ObjectNotFoundException("Role id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleName(String rname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleName(rname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Role " + rname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRole(this.valObj.rolename);
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
		this.valObj = new RoleObject();
		this.valObj.roleid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRole();
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
			storeRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String roleName, String title, String description)
	{
	}

	public Collection ejbFindAllRoleNames()
	{
		try
		{
			Collection bufAL = selectAllRoleNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectRolesGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesByDomainName(String dmnName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectRolesByDomainName(dmnName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * get Objects
	 **************************************************************************/
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
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * closeConnection()
	 **************************************************************************/
	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Integer insertRole(String rname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select roleid from " + TABLENAME + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roleid"))
				{
					bufInt = rs.getInt("roleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roleid is :" + newUid.toString());
			String insertStatement = "insert into " + TABLENAME + " (roleid , rolename , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, rname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, RoleBean.ACTIVE);
			insertStmt.executeUpdate();
			Log.printVerbose(strObjectName + "leaving insertRole");
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
			try
			{
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
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Integer selectByRoleName(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roleid " + "from " + TABLENAME + " where rolename = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, rname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("roleid"));
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + TABLENAME + " where roleid = ? ";
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void deleteRole(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + "  " + "where rolename = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, rname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted role : " + rname);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void loadRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select rolename, title , description, status, " + " roleid " + "from "
					+ TABLENAME + "  where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.valObj.roleid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = new RoleObject();
				this.valObj.rolename = rs.getString(1);
				this.valObj.title = rs.getString(2);
				this.valObj.description = rs.getString(3);
				this.valObj.status = rs.getString(4);
				this.valObj.roleid = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("roleName " + this.valObj.rolename + " roleid: "
						+ this.valObj.roleid.toString() + " not found in database.");
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectAllRoleNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			ArrayList roleSet = new ArrayList();
			RoleObject roleObj = null;
			con = makeConnection();
			String selectStatement = "select roleid  from " + TABLENAME + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleObj = new RoleObject();
				roleObj.roleid = new Integer(rs.getInt(1));
				roleSet.add(roleObj);
				Log.printDebug("RoleBean: roleid=" + roleObj.roleid);
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			
			ArrayList roleSet = new ArrayList();
			con = makeConnection();
			
			String selectStatement = "select roleid, rolename from " + TABLENAME + "  where " + fieldName + " = ? ";
			selectStatement += " order by rolename";

			System.out.println("selectStatement : "+selectStatement);
			
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);

			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
			
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesByDomainName(String dmnName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			ArrayList roleSet = new ArrayList();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void storeRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + "  set rolename = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where roleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.valObj.rolename);
			prepStmt.setString(2, this.valObj.title);
			prepStmt.setString(3, this.valObj.description);
			prepStmt.setString(4, this.valObj.status);
			prepStmt.setInt(5, this.valObj.roleid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row rolename " + this.valObj.rolename + " failed." + " roleId :"
						+ this.valObj.roleid);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			
			Log.printVerbose(selectStmt);
			
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			
			while (rs.next())
			{
				RoleObject roleObj = getObject(rs, "");
				if (roleObj != null)
				{
					result.add(roleObj);
				}
			}
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			if (con != null)
			{
				closeConnection(con);
			}
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static RoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		RoleObject roleObj = null;
		try
		{
			roleObj = new RoleObject();
			roleObj.roleid = new Integer(rs.getInt(ROLEID)); // primary key
			roleObj.rolename = rs.getString(ROLENAME);
			roleObj.title = rs.getString(TITLE);
			roleObj.description = rs.getString(DESCRIPTION);
			roleObj.status = rs.getString(STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}
}
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

public class RoleBean implements EntityBean
{
	// Member Variables
	public static final String ROLEID = "roleid";
	public static final String ROLENAME = "rolename";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "user_role_index";
	protected final String strObjectName = "RoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// CONSTANT for ROLEID
	public static final Integer ROLEID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	private RoleObject valObj;

	public RoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(RoleObject newVal)
	{
		Integer roleid = this.valObj.roleid;
		this.valObj = newVal;
		this.valObj.roleid = roleid;
	}

	public Integer getRoleId()
	{
		return this.valObj.roleid;
	}

	public void setRoleId(Integer uid)
	{
		this.valObj.roleid = uid;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.valObj.roleid;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.valObj.roleid = rid;
	}

	public String getRoleName()
	{
		return this.valObj.rolename;
	}

	public void setRoleName(String rname)
	{
		this.valObj.rolename = rname;
	}

	public String getTitle()
	{
		return this.valObj.title;
	}

	public void setTitle(String ttl)
	{
		this.valObj.title = ttl;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public Integer ejbCreate(String rname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertRole(rname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.valObj = new RoleObject();
			this.valObj.rolename = rname;
			this.valObj.title = lTitle;
			this.valObj.description = desc;
			this.valObj.status = "active";
			this.valObj.roleid = rid;
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
			throw new ObjectNotFoundException("Role id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleName(String rname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleName(rname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Role " + rname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRole(this.valObj.rolename);
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
		this.valObj = new RoleObject();
		this.valObj.roleid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRole();
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
			storeRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String roleName, String title, String description)
	{
	}

	public Collection ejbFindAllRoleNames()
	{
		try
		{
			Collection bufAL = selectAllRoleNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectRolesGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesByDomainName(String dmnName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectRolesByDomainName(dmnName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * get Objects
	 **************************************************************************/
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
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * closeConnection()
	 **************************************************************************/
	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Integer insertRole(String rname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select roleid from " + TABLENAME + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roleid"))
				{
					bufInt = rs.getInt("roleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roleid is :" + newUid.toString());
			String insertStatement = "insert into " + TABLENAME + " (roleid , rolename , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, rname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, RoleBean.ACTIVE);
			insertStmt.executeUpdate();
			Log.printVerbose(strObjectName + "leaving insertRole");
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
			try
			{
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
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Integer selectByRoleName(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roleid " + "from " + TABLENAME + " where rolename = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, rname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("roleid"));
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + TABLENAME + " where roleid = ? ";
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void deleteRole(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + "  " + "where rolename = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, rname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted role : " + rname);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void loadRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select rolename, title , description, status, " + " roleid " + "from "
					+ TABLENAME + "  where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.valObj.roleid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = new RoleObject();
				this.valObj.rolename = rs.getString(1);
				this.valObj.title = rs.getString(2);
				this.valObj.description = rs.getString(3);
				this.valObj.status = rs.getString(4);
				this.valObj.roleid = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("roleName " + this.valObj.rolename + " roleid: "
						+ this.valObj.roleid.toString() + " not found in database.");
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectAllRoleNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			ArrayList roleSet = new ArrayList();
			RoleObject roleObj = null;
			con = makeConnection();
			String selectStatement = "select roleid  from " + TABLENAME + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleObj = new RoleObject();
				roleObj.roleid = new Integer(rs.getInt(1));
				roleSet.add(roleObj);
				Log.printDebug("RoleBean: roleid=" + roleObj.roleid);
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList roleSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select roleid from " + TABLENAME + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesByDomainName(String dmnName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			ArrayList roleSet = new ArrayList();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void storeRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + "  set rolename = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where roleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.valObj.rolename);
			prepStmt.setString(2, this.valObj.title);
			prepStmt.setString(3, this.valObj.description);
			prepStmt.setString(4, this.valObj.status);
			prepStmt.setInt(5, this.valObj.roleid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row rolename " + this.valObj.rolename + " failed." + " roleId :"
						+ this.valObj.roleid);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				Log.printVerbose("found one cheque!!!");
				RoleObject roleObj = getObject(rs, "");
				if (roleObj != null)
				{
					result.add(roleObj);
				}
			}
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			if (con != null)
			{
				closeConnection(con);
			}
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static RoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		RoleObject roleObj = null;
		try
		{
			roleObj = new RoleObject();
			roleObj.roleid = new Integer(rs.getInt(ROLEID)); // primary key
			roleObj.rolename = rs.getString(ROLENAME);
			roleObj.title = rs.getString(TITLE);
			roleObj.description = rs.getString(DESCRIPTION);
			roleObj.status = rs.getString(STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}
}
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

public class RoleBean implements EntityBean
{
	// Member Variables
	public static final String ROLEID = "roleid";
	public static final String ROLENAME = "rolename";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "user_role_index";
	protected final String strObjectName = "RoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// CONSTANT for ROLEID
	public static final Integer ROLEID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	private RoleObject valObj;

	public RoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(RoleObject newVal)
	{
		Integer roleid = this.valObj.roleid;
		this.valObj = newVal;
		this.valObj.roleid = roleid;
	}

	public Integer getRoleId()
	{
		return this.valObj.roleid;
	}

	public void setRoleId(Integer uid)
	{
		this.valObj.roleid = uid;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.valObj.roleid;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.valObj.roleid = rid;
	}

	public String getRoleName()
	{
		return this.valObj.rolename;
	}

	public void setRoleName(String rname)
	{
		this.valObj.rolename = rname;
	}

	public String getTitle()
	{
		return this.valObj.title;
	}

	public void setTitle(String ttl)
	{
		this.valObj.title = ttl;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public Integer ejbCreate(String rname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertRole(rname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.valObj = new RoleObject();
			this.valObj.rolename = rname;
			this.valObj.title = lTitle;
			this.valObj.description = desc;
			this.valObj.status = "active";
			this.valObj.roleid = rid;
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
			throw new ObjectNotFoundException("Role id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleName(String rname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleName(rname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Role " + rname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRole(this.valObj.rolename);
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
		this.valObj = new RoleObject();
		this.valObj.roleid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRole();
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
			storeRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String roleName, String title, String description)
	{
	}

	public Collection ejbFindAllRoleNames()
	{
		try
		{
			Collection bufAL = selectAllRoleNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectRolesGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesByDomainName(String dmnName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectRolesByDomainName(dmnName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * get Objects
	 **************************************************************************/
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
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * closeConnection()
	 **************************************************************************/
	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Integer insertRole(String rname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select roleid from " + TABLENAME + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roleid"))
				{
					bufInt = rs.getInt("roleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roleid is :" + newUid.toString());
			String insertStatement = "insert into " + TABLENAME + " (roleid , rolename , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, rname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, RoleBean.ACTIVE);
			insertStmt.executeUpdate();
			Log.printVerbose(strObjectName + "leaving insertRole");
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
			try
			{
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
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Integer selectByRoleName(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roleid " + "from " + TABLENAME + " where rolename = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, rname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("roleid"));
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + TABLENAME + " where roleid = ? ";
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void deleteRole(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + "  " + "where rolename = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, rname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted role : " + rname);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void loadRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select rolename, title , description, status, " + " roleid " + "from "
					+ TABLENAME + "  where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.valObj.roleid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = new RoleObject();
				this.valObj.rolename = rs.getString(1);
				this.valObj.title = rs.getString(2);
				this.valObj.description = rs.getString(3);
				this.valObj.status = rs.getString(4);
				this.valObj.roleid = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("roleName " + this.valObj.rolename + " roleid: "
						+ this.valObj.roleid.toString() + " not found in database.");
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectAllRoleNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			ArrayList roleSet = new ArrayList();
			RoleObject roleObj = null;
			con = makeConnection();
			String selectStatement = "select roleid  from " + TABLENAME + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				roleObj = new RoleObject();
				roleObj.roleid = new Integer(rs.getInt(1));
				roleSet.add(roleObj);
				Log.printDebug("RoleBean: roleid=" + roleObj.roleid);
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList roleSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select roleid from " + TABLENAME + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesByDomainName(String dmnName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			ArrayList roleSet = new ArrayList();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void storeRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + "  set rolename = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where roleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.valObj.rolename);
			prepStmt.setString(2, this.valObj.title);
			prepStmt.setString(3, this.valObj.description);
			prepStmt.setString(4, this.valObj.status);
			prepStmt.setInt(5, this.valObj.roleid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row rolename " + this.valObj.rolename + " failed." + " roleId :"
						+ this.valObj.roleid);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				Log.printVerbose("found one cheque!!!");
				RoleObject roleObj = getObject(rs, "");
				if (roleObj != null)
				{
					result.add(roleObj);
				}
			}
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			if (con != null)
			{
				closeConnection(con);
			}
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static RoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		RoleObject roleObj = null;
		try
		{
			roleObj = new RoleObject();
			roleObj.roleid = new Integer(rs.getInt(ROLEID)); // primary key
			roleObj.rolename = rs.getString(ROLENAME);
			roleObj.title = rs.getString(TITLE);
			roleObj.description = rs.getString(DESCRIPTION);
			roleObj.status = rs.getString(STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}
}
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

public class RoleBean implements EntityBean
{
	// Member Variables
	public static final String ROLEID = "roleid";
	public static final String ROLENAME = "rolename";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "user_role_index";
	protected final String strObjectName = "RoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	// CONSTANT for ROLEID
	public static final Integer ROLEID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	private RoleObject valObj;

	public RoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(RoleObject newVal)
	{
		Integer roleid = this.valObj.roleid;
		this.valObj = newVal;
		this.valObj.roleid = roleid;
	}

	public Integer getRoleId()
	{
		return this.valObj.roleid;
	}

	public void setRoleId(Integer uid)
	{
		this.valObj.roleid = uid;
	}

	public String getStatus()
	{
		return this.valObj.status;
	}

	public void setStatus(String stts)
	{
		this.valObj.status = stts;
	}

	public Integer getPrimaryKey()
	{
		return this.valObj.roleid;
	}

	public void setPrimaryKey(Integer rid)
	{
		this.valObj.roleid = rid;
	}

	public String getRoleName()
	{
		return this.valObj.rolename;
	}

	public void setRoleName(String rname)
	{
		this.valObj.rolename = rname;
	}

	public String getTitle()
	{
		return this.valObj.title;
	}

	public void setTitle(String ttl)
	{
		this.valObj.title = ttl;
	}

	public String getDescription()
	{
		return this.valObj.description;
	}

	public void setDescription(String desc)
	{
		this.valObj.description = desc;
	}

	public Integer ejbCreate(String rname, String lTitle, String desc) throws CreateException
	{
		Integer rid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			rid = insertRole(rname, lTitle, desc);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (rid != null)
		{
			this.valObj = new RoleObject();
			this.valObj.rolename = rname;
			this.valObj.title = lTitle;
			this.valObj.description = desc;
			this.valObj.status = "active";
			this.valObj.roleid = rid;
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
			throw new ObjectNotFoundException("Role id " + rid.toString() + " not found.");
		}
	}

	public Integer ejbFindByRoleName(String rname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByRoleName");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByRoleName(rname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByRoleName: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByRoleName");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("Role " + rname + " not found.");
		}
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + "in ejbRemove");
		try
		{
			deleteRole(this.valObj.rolename);
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
		this.valObj = new RoleObject();
		this.valObj.roleid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadRole();
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
			storeRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String roleName, String title, String description)
	{
	}

	public Collection ejbFindAllRoleNames()
	{
		try
		{
			Collection bufAL = selectAllRoleNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectRolesGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Collection ejbFindRolesByDomainName(String dmnName)
	{
		Log.printVerbose(" entering findRolesByDomainName ");
		try
		{
			Collection bufAL = selectRolesByDomainName(dmnName);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindRolesGivenDomainName: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * get Objects
	 **************************************************************************/
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
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * closeConnection()
	 **************************************************************************/
	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Integer insertRole(String rname, String lTitle, String desc) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select roleid from " + TABLENAME + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("roleid"))
				{
					bufInt = rs.getInt("roleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new roleid is :" + newUid.toString());
			String insertStatement = "insert into " + TABLENAME + " (roleid , rolename , title , description , "
					+ " status ) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, rname);
			insertStmt.setString(3, lTitle);
			insertStmt.setString(4, desc);
			insertStmt.setString(5, RoleBean.ACTIVE);
			insertStmt.executeUpdate();
			Log.printVerbose(strObjectName + "leaving insertRole");
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
			try
			{
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
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Integer selectByRoleName(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select roleid " + "from " + TABLENAME + " where rolename = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, rname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("roleid"));
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private boolean selectByPrimaryKey(Integer rid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + TABLENAME + " where roleid = ? ";
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void deleteRole(String rname) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + "  " + "where rolename = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, rname);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted role : " + rname);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void loadRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select rolename, title , description, status, " + " roleid " + "from "
					+ TABLENAME + "  where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.valObj.roleid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = new RoleObject();
				this.valObj.rolename = rs.getString(1);
				this.valObj.title = rs.getString(2);
				this.valObj.description = rs.getString(3);
				this.valObj.status = rs.getString(4);
				this.valObj.roleid = new Integer(rs.getInt(5));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("roleName " + this.valObj.rolename + " roleid: "
						+ this.valObj.roleid.toString() + " not found in database.");
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectAllRoleNames() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			ArrayList roleSet = new ArrayList();
			RoleObject roleObj = null;
			con = makeConnection();
			String selectStatement = "select roleid  from " + TABLENAME + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleObj = new RoleObject();
				roleObj.roleid = new Integer(rs.getInt(1));
				roleSet.add(roleObj);
				Log.printDebug("RoleBean: roleid=" + roleObj.roleid);
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList roleSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select roleid from " + TABLENAME + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private Collection selectRolesByDomainName(String dmnName) throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			ArrayList roleSet = new ArrayList();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, dmnName);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				roleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return roleSet;
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	private void storeRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		Connection con = null;
		try
		{
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + "  set rolename = ? , " + " title =  ? , "
					+ " description = ? , " + " status = ?  " + " where roleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setString(1, this.valObj.rolename);
			prepStmt.setString(2, this.valObj.title);
			prepStmt.setString(3, this.valObj.description);
			prepStmt.setString(4, this.valObj.status);
			prepStmt.setInt(5, this.valObj.roleid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row rolename " + this.valObj.rolename + " failed." + " roleId :"
						+ this.valObj.roleid);
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
			try
			{
				if (prepStmt != null)
				{
					// Log.printVerbose("Closing prepStmt ...");
					prepStmt.close();
				}
				if (con != null)
				{
					closeConnection(con);
				}
			} catch (Exception ex)
			{
			}
		}
	}

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				Log.printVerbose("found one cheque!!!");
				RoleObject roleObj = getObject(rs, "");
				if (roleObj != null)
				{
					result.add(roleObj);
				}
			}
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			if (con != null)
			{
				closeConnection(con);
			}
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static RoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		RoleObject roleObj = null;
		try
		{
			roleObj = new RoleObject();
			roleObj.roleid = new Integer(rs.getInt(ROLEID)); // primary key
			roleObj.rolename = rs.getString(ROLENAME);
			roleObj.title = rs.getString(TITLE);
			roleObj.description = rs.getString(DESCRIPTION);
			roleObj.status = rs.getString(STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}
}
