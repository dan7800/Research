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
import com.vlee.bean.user.*;

public class UserRoleBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String strUserTable = "user_userrole_link";
	protected final String strObjectName = "UserRoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	public static final String USERROLEID = "userroleid";
	public static final String USERID = "userid";
	public static final String ROLEID = "roleid";
	public static final String STATUS = "status";
	public static final String ADATE = "adate";
	// members ----------------------------------------------
	private Integer userRoleId; // primary key!!!
	private Integer userId;
	private Integer roleId;
	private String status;
	private Calendar aDate;
	private UserRoleObject valObj;

	public UserRoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(UserRoleObject newVal)
	{
		Integer userRoleId = this.valObj.userroleid;
		this.valObj = newVal;
		this.valObj.userroleid = userRoleId;
	}

	public Integer getUserRoleId()
	{
		return this.userRoleId;
	}

	public void setUserRoleId(Integer urid)
	{
		this.userRoleId = urid;
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
		return this.userRoleId;
	}

	public void setPrimaryKey(Integer urid)
	{
		this.userRoleId = urid;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer usrid)
	{
		this.userId = usrid;
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

	public Integer ejbCreate(Integer rlid, Integer usrid, Calendar bufADate) throws CreateException
	{
		Integer urid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			urid = insertUserRole(rlid, usrid, bufADate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (urid != null)
		{
			this.userRoleId = urid;
			this.userId = usrid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = UserRoleBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return urid;
	}

	public Integer ejbFindByPrimaryKey(Integer urid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(urid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return urid;
		} else
		{
			throw new ObjectNotFoundException("UserRole " + urid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUserRoleId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUserRoleId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByUserRoleId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUserRoleId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUserRoleId");
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
			deleteUserRole(this.userRoleId);
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
		this.userRoleId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userRoleId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadUserRole();
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
			storeUserRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer usrid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllUserRoleId()
	{
		try
		{
			Collection bufAL = selectAllUserRoleId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUserRoleId: " + ex);
			return null;
		}
	}

	public Collection ejbFindUserRolesGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectUserRolesGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * getObjects()
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

	public Collection ejbHomeGetUserList(String status, String sortby)
	{
		Collection colList = null;
		try
		{
			colList = getList(status, sortby);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return colList;
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

	private Integer insertUserRole(Integer rlid, Integer usrid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUserRole: ");
			String findMaxUidStmt = " select userroleid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userroleid"))
				{
					bufInt = rs.getInt("userroleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new userroleid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userroleid , userid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, UserRoleBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userrole : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertUserRole");
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

	private Integer selectByUserRoleId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userroleid " + "from " + strUserTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("userroleid"));
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

	private boolean selectByPrimaryKey(Integer urid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, urid.intValue());
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

	private void deleteUserRole(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where userroleid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userrole : " + rlid);
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

	private void loadUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select userroleid, userid, roleid, status, " + " adate " + " from "
					+ strUserTable + "  where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userRoleId = new Integer(rs.getInt("userroleid"));
				this.userId = new Integer(rs.getInt("userid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("userroleid " + this.userRoleId.toString());
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

	private Collection selectAllUserRoleId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private Collection selectUserRolesGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private void storeUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set userroleid = ? , " + " userid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where userroleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			prepStmt.setInt(2, this.userId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.userRoleId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row userroleid" + this.userRoleId.toString() + " failed.");
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

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		// Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + " selectObjects: ");
			// con= makeConnection();
			String selectStmt = " SELECT * FROM " + strUserTable;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				UserRoleObject roleObj = getObject(rs, "");
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
			releaseConnection();
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static UserRoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		UserRoleObject roleObj = null;
		try
		{
			roleObj = new UserRoleObject();
			roleObj.userroleid = new Integer(rs.getInt("userroleid")); // primary
																		// key
			roleObj.userid = new Integer(rs.getInt("userid"));
			roleObj.roleid = new Integer(rs.getInt("roleid"));
			roleObj.status = rs.getString("status");
			// roleObj.adate = new Date(rs.getDate("adate"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}

	private Collection getList(String status, String sortby) throws NamingException, SQLException, Exception
	{
		makeConnection();
		PreparedStatement prepStmt = null;
		Vector vecUserList = new Vector();
		try
		{
			String sltStmt = "select A.userid as userid,username,namefirst,namelast,A.status as status ,";
			sltStmt += "C.userroleid,rolename,B.roleid as roleid ";
			sltStmt += " from user_index as A ,user_role_index as B,user_userrole_link as C ";
			sltStmt += " where A.userid=C.userid and B.roleid=C.roleid  ";
			sltStmt += " and A.status=? order by " + sortby;
			prepStmt = con.prepareStatement(sltStmt);
			prepStmt.setString(1, status);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				UserRoleListing.Row usrsession = new UserRoleListing.Row();
				usrsession.userId = new Integer(rs.getInt("userid"));
				usrsession.userName = rs.getString("username");
				usrsession.firstName = rs.getString("namefirst");
				usrsession.lastName = rs.getString("namelast");
				usrsession.status = rs.getString("status");
				usrsession.userRoleId = new Integer(rs.getInt("userroleid"));
				usrsession.roleName = rs.getString("rolename");
				usrsession.roleId = new Integer(rs.getInt("roleid"));
				vecUserList.add(usrsession);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		if (prepStmt != null)
		{
			prepStmt.close();
		}
		releaseConnection();
		return vecUserList;
	}
} // UserRoleBean
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
import com.vlee.bean.user.*;

public class UserRoleBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_userrole_link";
	protected final String strObjectName = "UserRoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	public static final String USERROLEID = "userroleid";
	public static final String USERID = "userid";
	public static final String ROLEID = "roleid";
	public static final String STATUS = "status";
	public static final String ADATE = "adate";
	// members ----------------------------------------------
	private Integer userRoleId; // primary key!!!
	private Integer userId;
	private Integer roleId;
	private String status;
	private Calendar aDate;
	private UserRoleObject valObj;

	public UserRoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(UserRoleObject newVal)
	{
		Integer userRoleId = this.valObj.userroleid;
		this.valObj = newVal;
		this.valObj.userroleid = userRoleId;
	}

	public Integer getUserRoleId()
	{
		return this.userRoleId;
	}

	public void setUserRoleId(Integer urid)
	{
		this.userRoleId = urid;
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
		return this.userRoleId;
	}

	public void setPrimaryKey(Integer urid)
	{
		this.userRoleId = urid;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer usrid)
	{
		this.userId = usrid;
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

	public Integer ejbCreate(Integer rlid, Integer usrid, Calendar bufADate) throws CreateException
	{
		Integer urid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			urid = insertUserRole(rlid, usrid, bufADate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (urid != null)
		{
			this.userRoleId = urid;
			this.userId = usrid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = UserRoleBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return urid;
	}

	public Integer ejbFindByPrimaryKey(Integer urid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(urid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return urid;
		} else
		{
			throw new ObjectNotFoundException("UserRole " + urid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUserRoleId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUserRoleId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByUserRoleId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUserRoleId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUserRoleId");
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
			deleteUserRole(this.userRoleId);
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
		this.userRoleId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userRoleId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadUserRole();
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
			storeUserRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer usrid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllUserRoleId()
	{
		try
		{
			Collection bufAL = selectAllUserRoleId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUserRoleId: " + ex);
			return null;
		}
	}

	public Collection ejbFindUserRolesGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectUserRolesGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * getObjects()
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

	public Collection ejbHomeGetUserList(String status, String sortby)
	{
		Collection colList = null;
		try
		{
			colList = getList(status, sortby);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return colList;
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

	private Integer insertUserRole(Integer rlid, Integer usrid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUserRole: ");
			String findMaxUidStmt = " select userroleid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userroleid"))
				{
					bufInt = rs.getInt("userroleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new userroleid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userroleid , userid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, UserRoleBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userrole : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertUserRole");
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

	private Integer selectByUserRoleId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userroleid " + "from " + strUserTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("userroleid"));
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

	private boolean selectByPrimaryKey(Integer urid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, urid.intValue());
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

	private void deleteUserRole(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where userroleid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userrole : " + rlid);
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

	private void loadUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select userroleid, userid, roleid, status, " + " adate " + " from "
					+ strUserTable + "  where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userRoleId = new Integer(rs.getInt("userroleid"));
				this.userId = new Integer(rs.getInt("userid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("userroleid " + this.userRoleId.toString());
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

	private Collection selectAllUserRoleId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private Collection selectUserRolesGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private void storeUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set userroleid = ? , " + " userid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where userroleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			prepStmt.setInt(2, this.userId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.userRoleId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row userroleid" + this.userRoleId.toString() + " failed.");
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

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		// Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + " selectObjects: ");
			// con= makeConnection();
			String selectStmt = " SELECT * FROM " + strUserTable;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				Log.printVerbose("found one cheque!!!");
				UserRoleObject roleObj = getObject(rs, "");
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
			releaseConnection();
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static UserRoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		UserRoleObject roleObj = null;
		try
		{
			roleObj = new UserRoleObject();
			roleObj.userroleid = new Integer(rs.getInt("userroleid")); // primary
																		// key
			roleObj.userid = new Integer(rs.getInt("userid"));
			roleObj.roleid = new Integer(rs.getInt("roleid"));
			roleObj.status = rs.getString("status");
			// roleObj.adate = new Date(rs.getDate("adate"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}

	private Collection getList(String status, String sortby) throws NamingException, SQLException, Exception
	{
		makeConnection();
		PreparedStatement prepStmt = null;
		Vector vecUserList = new Vector();
		try
		{
			String sltStmt = "select A.userid as userid,username,namefirst,namelast,A.status as status ,";
			sltStmt += "C.userroleid,rolename,B.roleid as roleid ";
			sltStmt += " from user_index as A ,user_role_index as B,user_userrole_link as C ";
			sltStmt += " where A.userid=C.userid and B.roleid=C.roleid  ";
			sltStmt += " and A.status=? order by " + sortby;
			prepStmt = con.prepareStatement(sltStmt);
			prepStmt.setString(1, status);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				UserRoleListing.Row usrsession = new UserRoleListing.Row();
				usrsession.userId = new Integer(rs.getInt("userid"));
				usrsession.userName = rs.getString("username");
				usrsession.firstName = rs.getString("namefirst");
				usrsession.lastName = rs.getString("namelast");
				usrsession.status = rs.getString("status");
				usrsession.userRoleId = new Integer(rs.getInt("userroleid"));
				usrsession.roleName = rs.getString("rolename");
				usrsession.roleId = new Integer(rs.getInt("roleid"));
				vecUserList.add(usrsession);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		if (prepStmt != null)
		{
			prepStmt.close();
		}
		releaseConnection();
		return vecUserList;
	}
} // UserRoleBean
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
import com.vlee.bean.user.*;

public class UserRoleBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_userrole_link";
	protected final String strObjectName = "UserRoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	public static final String USERROLEID = "userroleid";
	public static final String USERID = "userid";
	public static final String ROLEID = "roleid";
	public static final String STATUS = "status";
	public static final String ADATE = "adate";
	// members ----------------------------------------------
	private Integer userRoleId; // primary key!!!
	private Integer userId;
	private Integer roleId;
	private String status;
	private Calendar aDate;
	private UserRoleObject valObj;

	public UserRoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(UserRoleObject newVal)
	{
		Integer userRoleId = this.valObj.userroleid;
		this.valObj = newVal;
		this.valObj.userroleid = userRoleId;
	}

	public Integer getUserRoleId()
	{
		return this.userRoleId;
	}

	public void setUserRoleId(Integer urid)
	{
		this.userRoleId = urid;
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
		return this.userRoleId;
	}

	public void setPrimaryKey(Integer urid)
	{
		this.userRoleId = urid;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer usrid)
	{
		this.userId = usrid;
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

	public Integer ejbCreate(Integer rlid, Integer usrid, Calendar bufADate) throws CreateException
	{
		Integer urid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			urid = insertUserRole(rlid, usrid, bufADate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (urid != null)
		{
			this.userRoleId = urid;
			this.userId = usrid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = UserRoleBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return urid;
	}

	public Integer ejbFindByPrimaryKey(Integer urid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(urid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return urid;
		} else
		{
			throw new ObjectNotFoundException("UserRole " + urid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUserRoleId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUserRoleId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByUserRoleId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUserRoleId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUserRoleId");
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
			deleteUserRole(this.userRoleId);
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
		this.userRoleId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userRoleId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadUserRole();
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
			storeUserRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer usrid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllUserRoleId()
	{
		try
		{
			Collection bufAL = selectAllUserRoleId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUserRoleId: " + ex);
			return null;
		}
	}

	public Collection ejbFindUserRolesGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectUserRolesGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * getObjects()
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

	public Collection ejbHomeGetUserList(String status, String sortby)
	{
		Collection colList = null;
		try
		{
			colList = getList(status, sortby);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return colList;
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

	private Integer insertUserRole(Integer rlid, Integer usrid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUserRole: ");
			String findMaxUidStmt = " select userroleid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userroleid"))
				{
					bufInt = rs.getInt("userroleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new userroleid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userroleid , userid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, UserRoleBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userrole : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertUserRole");
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

	private Integer selectByUserRoleId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userroleid " + "from " + strUserTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("userroleid"));
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

	private boolean selectByPrimaryKey(Integer urid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, urid.intValue());
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

	private void deleteUserRole(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where userroleid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userrole : " + rlid);
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

	private void loadUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select userroleid, userid, roleid, status, " + " adate " + " from "
					+ strUserTable + "  where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userRoleId = new Integer(rs.getInt("userroleid"));
				this.userId = new Integer(rs.getInt("userid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("userroleid " + this.userRoleId.toString());
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

	private Collection selectAllUserRoleId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private Collection selectUserRolesGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private void storeUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set userroleid = ? , " + " userid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where userroleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			prepStmt.setInt(2, this.userId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.userRoleId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row userroleid" + this.userRoleId.toString() + " failed.");
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

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		// Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + " selectObjects: ");
			// con= makeConnection();
			String selectStmt = " SELECT * FROM " + strUserTable;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				Log.printVerbose("found one cheque!!!");
				UserRoleObject roleObj = getObject(rs, "");
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
			releaseConnection();
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static UserRoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		UserRoleObject roleObj = null;
		try
		{
			roleObj = new UserRoleObject();
			roleObj.userroleid = new Integer(rs.getInt("userroleid")); // primary
																		// key
			roleObj.userid = new Integer(rs.getInt("userid"));
			roleObj.roleid = new Integer(rs.getInt("roleid"));
			roleObj.status = rs.getString("status");
			// roleObj.adate = new Date(rs.getDate("adate"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}

	private Collection getList(String status, String sortby) throws NamingException, SQLException, Exception
	{
		makeConnection();
		PreparedStatement prepStmt = null;
		Vector vecUserList = new Vector();
		try
		{
			String sltStmt = "select A.userid as userid,username,namefirst,namelast,A.status as status ,";
			sltStmt += "C.userroleid,rolename,B.roleid as roleid ";
			sltStmt += " from user_index as A ,user_role_index as B,user_userrole_link as C ";
			sltStmt += " where A.userid=C.userid and B.roleid=C.roleid  ";
			sltStmt += " and A.status=? order by " + sortby;
			prepStmt = con.prepareStatement(sltStmt);
			prepStmt.setString(1, status);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				UserRoleListing.Row usrsession = new UserRoleListing.Row();
				usrsession.userId = new Integer(rs.getInt("userid"));
				usrsession.userName = rs.getString("username");
				usrsession.firstName = rs.getString("namefirst");
				usrsession.lastName = rs.getString("namelast");
				usrsession.status = rs.getString("status");
				usrsession.userRoleId = new Integer(rs.getInt("userroleid"));
				usrsession.roleName = rs.getString("rolename");
				usrsession.roleId = new Integer(rs.getInt("roleid"));
				vecUserList.add(usrsession);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		if (prepStmt != null)
		{
			prepStmt.close();
		}
		releaseConnection();
		return vecUserList;
	}
} // UserRoleBean
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
import com.vlee.bean.user.*;

public class UserRoleBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_userrole_link";
	protected final String strObjectName = "UserRoleBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	public static final String USERROLEID = "userroleid";
	public static final String USERID = "userid";
	public static final String ROLEID = "roleid";
	public static final String STATUS = "status";
	public static final String ADATE = "adate";
	// members ----------------------------------------------
	private Integer userRoleId; // primary key!!!
	private Integer userId;
	private Integer roleId;
	private String status;
	private Calendar aDate;
	private UserRoleObject valObj;

	public UserRoleObject getObject()
	{
		return this.valObj;
	}

	public void setObject(UserRoleObject newVal)
	{
		Integer userRoleId = this.valObj.userroleid;
		this.valObj = newVal;
		this.valObj.userroleid = userRoleId;
	}

	public Integer getUserRoleId()
	{
		return this.userRoleId;
	}

	public void setUserRoleId(Integer urid)
	{
		this.userRoleId = urid;
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
		return this.userRoleId;
	}

	public void setPrimaryKey(Integer urid)
	{
		this.userRoleId = urid;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer usrid)
	{
		this.userId = usrid;
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

	public Integer ejbCreate(Integer rlid, Integer usrid, Calendar bufADate) throws CreateException
	{
		Integer urid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			urid = insertUserRole(rlid, usrid, bufADate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (urid != null)
		{
			this.userRoleId = urid;
			this.userId = usrid;
			this.roleId = rlid;
			this.aDate = bufADate;
			this.status = UserRoleBean.ACTIVE;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return urid;
	}

	public Integer ejbFindByPrimaryKey(Integer urid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(urid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return urid;
		} else
		{
			throw new ObjectNotFoundException("UserRole " + urid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUserRoleId(Integer rlid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUserRoleId");
		boolean result;
		Integer bufInt = null;
		try
		{
			bufInt = selectByUserRoleId(rlid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUserRoleId: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUserRoleId");
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
			deleteUserRole(this.userRoleId);
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
		this.userRoleId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userRoleId = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadUserRole();
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
			storeUserRole();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer rlid, Integer usrid, Calendar bufADate)
	{
	}

	public Collection ejbFindAllUserRoleId()
	{
		try
		{
			Collection bufAL = selectAllUserRoleId();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUserRoleId: " + ex);
			return null;
		}
	}

	public Collection ejbFindUserRolesGiven(String fieldName, String strCriteria)
	{
		try
		{
			Collection bufAL = selectUserRolesGiven(fieldName, strCriteria);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	/***************************************************************************
	 * getObjects()
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

	public Collection ejbHomeGetUserList(String status, String sortby)
	{
		Collection colList = null;
		try
		{
			colList = getList(status, sortby);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return colList;
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

	private Integer insertUserRole(Integer rlid, Integer usrid, Calendar bufADate) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertUserRole: ");
			String findMaxUidStmt = " select userroleid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userroleid"))
				{
					bufInt = rs.getInt("userroleid");
				}
			}
			Integer newUid = new Integer(bufInt + 1); // new
														// Integer(rs.getInt(1)
														// + 1);
			Log.printVerbose("The new userroleid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userroleid , userid , roleid , status, "
					+ " adate) " + " values ( ?, ? , ? , ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setInt(2, usrid.intValue());
			insertStmt.setInt(3, rlid.intValue());
			insertStmt.setString(4, UserRoleBean.ACTIVE);
			insertStmt.setDate(5, new java.sql.Date(bufADate.getTime().getTime()));
			insertStmt.executeUpdate();
			// insertStmt.close();
			Log.printAudit(strObjectName + "Created userrole : " + newUid.toString());
			Log.printVerbose(strObjectName + "leaving insertUserRole");
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

	private Integer selectByUserRoleId(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userroleid " + "from " + strUserTable + " where roleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, rlid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				bufInt = new Integer(rs.getInt("userroleid"));
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

	private boolean selectByPrimaryKey(Integer urid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, urid.intValue());
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

	private void deleteUserRole(Integer rlid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where userroleid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, rlid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted userrole : " + rlid);
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

	private void loadUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = " select userroleid, userid, roleid, status, " + " adate " + " from "
					+ strUserTable + "  where userroleid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userRoleId = new Integer(rs.getInt("userroleid"));
				this.userId = new Integer(rs.getInt("userid"));
				this.roleId = new Integer(rs.getInt("roleid"));
				this.status = rs.getString("status");
				this.aDate = Calendar.getInstance();
				this.aDate.setTime(rs.getDate("adate"));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("userroleid " + this.userRoleId.toString());
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

	private Collection selectAllUserRoleId() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private Collection selectUserRolesGiven(String fieldName, String strCriteria) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + strCriteria);
			ArrayList userroleSet = new ArrayList();
			makeConnection();
			String selectStatement = "select userroleid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, strCriteria);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userroleSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection();
			return userroleSet;
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

	private void storeUserRole() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strUserTable + "  set userroleid = ? , " + " userid =  ? , "
					+ " roleid = ? , " + " status = ? , " + " adate = ?  " + " where userroleid = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.userRoleId.intValue());
			prepStmt.setInt(2, this.userId.intValue());
			prepStmt.setInt(3, this.roleId.intValue());
			prepStmt.setString(4, this.status);
			prepStmt.setDate(5, new java.sql.Date(this.aDate.getTime().getTime()));
			prepStmt.setInt(6, this.userRoleId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row userroleid" + this.userRoleId.toString() + " failed.");
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

	/***************************************************************************
	 * selectObjects
	 **************************************************************************/
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		// Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + " selectObjects: ");
			// con= makeConnection();
			String selectStmt = " SELECT * FROM " + strUserTable;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				Log.printVerbose("found one cheque!!!");
				UserRoleObject roleObj = getObject(rs, "");
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
			releaseConnection();
		}
		return result;
	}

	/***************************************************************************
	 * getObject
	 **************************************************************************/
	public static UserRoleObject getObject(ResultSet rs, String prefix) throws Exception
	{
		UserRoleObject roleObj = null;
		try
		{
			roleObj = new UserRoleObject();
			roleObj.userroleid = new Integer(rs.getInt("userroleid")); // primary
																		// key
			roleObj.userid = new Integer(rs.getInt("userid"));
			roleObj.roleid = new Integer(rs.getInt("roleid"));
			roleObj.status = rs.getString("status");
			// roleObj.adate = new Date(rs.getDate("adate"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return roleObj;
	}

	private Collection getList(String status, String sortby) throws NamingException, SQLException, Exception
	{
		makeConnection();
		PreparedStatement prepStmt = null;
		Vector vecUserList = new Vector();
		try
		{
			String sltStmt = "select A.userid as userid,username,namefirst,namelast,A.status as status ,";
			sltStmt += "C.userroleid,rolename,B.roleid as roleid ";
			sltStmt += " from user_index as A ,user_role_index as B,user_userrole_link as C ";
			sltStmt += " where A.userid=C.userid and B.roleid=C.roleid  ";
			sltStmt += " and A.status=? order by " + sortby;
			prepStmt = con.prepareStatement(sltStmt);
			prepStmt.setString(1, status);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				UserRoleListing.Row usrsession = new UserRoleListing.Row();
				usrsession.userId = new Integer(rs.getInt("userid"));
				usrsession.userName = rs.getString("username");
				usrsession.firstName = rs.getString("namefirst");
				usrsession.lastName = rs.getString("namelast");
				usrsession.status = rs.getString("status");
				usrsession.userRoleId = new Integer(rs.getInt("userroleid"));
				usrsession.roleName = rs.getString("rolename");
				usrsession.roleId = new Integer(rs.getInt("roleid"));
				vecUserList.add(usrsession);
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		if (prepStmt != null)
		{
			prepStmt.close();
		}
		releaseConnection();
		return vecUserList;
	}
} // UserRoleBean
