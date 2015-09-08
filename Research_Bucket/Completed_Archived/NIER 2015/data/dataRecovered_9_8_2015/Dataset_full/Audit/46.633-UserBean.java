/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import com.vlee.ejb.customer.InvoiceItemObject;
import com.vlee.local.ServerConfig;
import com.vlee.util.EncryptionEngine;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class UserBean implements EntityBean
{
	private static final long serialVersionUID = 0;
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_index";
	public static final String TABLENAME = "user_index";
	protected final String strObjectName = "UserBean: ";
	// private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String USERID = "userid";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String NAMEFIRST = "namefirst";
	public static final String NAMELAST = "namelast";
	public static final String STATUS = "status";
	public static final Integer USERID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";


	private Integer userId; // primary key!!!
	private String userName = "";
	private byte[] password;
	private String nameFirst = "";
	private String nameLast = "";
	private String status = "";

	public UserObject getObject()
	{
		UserObject uObj = new UserObject();
		uObj.userId = userId;
		uObj.userName = userName;
		uObj.password = password;
		uObj.nameFirst = nameFirst;
		uObj.nameLast = nameLast;
		uObj.status = status;
		return uObj;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer uid)
	{
		this.userId = uid;
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
		return this.userId;
	}

	public void setPrimaryKey(Integer uid)
	{
		this.userId = uid;
	}

	public String getName()
	{
		return nameFirst + " " + nameLast;
	}

	public void setPassword(String passwd)
	{
		EncryptionEngine ee = new EncryptionEngine(passwd);
		ByteArrayOutputStream baos = ee.encrypt(passwd);
		this.password = baos.toByteArray();
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String name)
	{
		this.userName = name;
	}

	public String getNameFirst()
	{
		return nameFirst;
	}

	public void setNameFirst(String name)
	{
		this.nameFirst = name;
	}

	public String getNameLast()
	{
		return nameLast;
	}

	public void setNameLast(String name)
	{
		this.nameLast = name;
	}

	public boolean getValidDomainAccess(String lDomainName)
	{
		boolean bValid = false;
		try
		{
			bValid = validDomainAccess(this.userName, lDomainName);
		} catch (Exception ex)
		{
			throw new EJBException("getValidDomainAccess: " + ex.getMessage());
		}
		return bValid;
	}

	   public Collection ejbHomeGetObjects(QueryObject query)
	   {
	      Collection vecValObj = new Vector();
	      try
	      {
	         vecValObj = selectObjects(query);
	      } catch (Exception ex)
	      {
	         ex.printStackTrace();
	      }
	      return vecValObj;
	   }
	
    private Collection selectObjects(QueryObject query) 
		throws NamingException, SQLException
	{
	   Connection con = null;
		Collection coll = new Vector();
		 Log.printVerbose(strObjectName + " loadObject: ");
		 con = makeConnection();
		 String selectStatement = " SELECT * " + " FROM " + TABLENAME ;
			selectStatement = query.appendQuery(selectStatement);
		 PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		 ResultSet rs = prepStmt.executeQuery();
		 while(rs.next())
		 {
				UserObject usrObj = getObject(rs,"");
				coll.add(usrObj);
		 } 
		 prepStmt.close();
		 releaseConnection(con);
		 Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return coll;
	}	   
	   
	public boolean getValidUser(String uname, String pwd) throws RemoteException
	{
		// filter out inactive users
		if (getStatus().equals(UserBean.INACTIVE)) return false;
		if (pwd.length() < 1) return false;
		EncryptionEngine ee = new EncryptionEngine(pwd);
		ByteArrayInputStream bais = new ByteArrayInputStream(this.password);
		String strDecryptedPwd = ee.decrypt(bais);
		return getUserName().equals(uname) && (strDecryptedPwd.compareTo(pwd) == 0);
	}

	public Integer ejbCreate(String userName, String passwd, String nameFirst, String nameLast) throws CreateException
	{
		Integer uid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			uid = insertUser(userName, passwd, nameFirst, nameLast);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (uid != null)
		{
			this.userName = userName;
			EncryptionEngine ee = new EncryptionEngine(passwd);
			ByteArrayOutputStream baos = ee.encrypt(passwd);
			this.password = baos.toByteArray();
			this.nameFirst = nameFirst;
			this.nameLast = nameLast;
			this.status = UserBean.ACTIVE;
			this.userId = uid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return uid;
	}

	public Integer ejbFindByPrimaryKey(Integer uid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(uid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return uid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + uid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUsername(String uname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUsername");
		Integer bufInt = null;
		try
		{
			bufInt = selectByUsername(uname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUsername: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUsername");
		if (bufInt != null)
		{
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("User " + uname + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteUser(this.userName);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		// makeConnection();
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		// releaseConnection(con)
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.userId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userId = null;
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
			storeUser();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String userName, String passwd, String nameFirst, String nameLast)
	{
	}

	public Collection ejbFindAllUsers()
	{
		try
		{
			Collection bufAL = selectAllUserNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindUsersGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectUsersGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(String field1, String value1, String field2, String value2,
			String field3, String value3)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(field1, value1, field2, value2, field3, value3);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private Connection makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			// Log.printVerbose("Getting connection...");
			// con = ds.getConnection();
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection(Connection con)
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

	private Integer insertUser(String uname, String pwd, String fname, String lname) throws SQLException
	{
		Connection con = null;
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Integer newUid = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select userid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userid"))
				{
					bufInt = rs.getInt("userid");
				}
			}
			newUid = new Integer(bufInt + 1); // new Integer(rs.getInt(1) +
			// 1);
			Log.printVerbose("The new userid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userid, username, password, namefirst, "
					+ " namelast, status ) " + " values ( ?, ? , ? , ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, uname);
			EncryptionEngine ee = new EncryptionEngine(pwd);
			ByteArrayOutputStream baos = ee.encrypt(pwd);
			this.password = baos.toByteArray();
			insertStmt.setBytes(3, this.password);
			insertStmt.setString(4, fname);
			insertStmt.setString(5, lname);
			insertStmt.setString(6, UserBean.ACTIVE);
			insertStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Created user :" + uname + "Name :" + fname + " " + lname);
			Log.printVerbose(strObjectName + "leaving insertUser");
			// releaseConnection(con);
			// return newUid;
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
			releaseConnection(con);
		}
		return newUid;
	}

	private Integer selectByUsername(String uname) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userid " + "from " + strUserTable + " where username = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, uname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("userid"));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
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
			releaseConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer uid) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, uid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private void deleteUser(String userName) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where username = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, userName);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted user: " + userName);
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Vector selectValueObjectsGiven(String field1, String value1, String field2, String value2, String field3,
			String value3) throws SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStmt = " SELECT * " + " FROM " + strUserTable + "  WHERE status!= '*' ";
			if (field1 != null && value1 != null)
			{
				selectStmt += " AND " + field1 + " = '" + value1 + "' ";
			}
			if (field2 != null && value2 != null)
			{
				selectStmt += " AND " + field2 + " = '" + value2 + "' ";
			}
			if (field3 != null && value3 != null)
			{
				selectStmt += " AND " + field3 + " = '" + value3 + "' ";
			}
			selectStmt += " ORDER BY " + USERNAME;
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				UserObject usrObj = new UserObject();
				usrObj.userName = rs.getString(USERNAME);
				usrObj.password = rs.getBytes(PASSWORD);
				usrObj.nameFirst = rs.getString(NAMEFIRST);
				usrObj.nameLast = rs.getString(NAMELAST);
				usrObj.status = rs.getString(STATUS);
				usrObj.userId = new Integer(rs.getInt(USERID));
				vecValObj.add(usrObj);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
		return vecValObj;
	}

	// ////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select username, password, namefirst, namelast, " + " status, userid " + "from "
					+ strUserTable + "  where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, userId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userName = rs.getString(1);
				this.password = rs.getBytes(2);
				this.nameFirst = rs.getString(3);
				this.nameLast = rs.getString(4);
				this.status = rs.getString(5);
				this.userId = new Integer(rs.getInt(6));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for userName " + userName + userId.toString()
						+ " not found in database.");
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Collection selectAllUserNames() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private Collection selectUsersGiven(String fieldName, String value) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private boolean validDomainAccess(String lUsrName, String lDmainCode) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + lUsrName + " " + lDmainCode);
			boolean bValid = false;
			// ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_userrole_link.roleid from user_userrole_link where user_userrole_link.userid in (select user_index.userid from user_index where user_index.username = ?) ) and user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, lUsrName);
			prepStmt.setString(2, lDmainCode);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				bValid = true;
				Log.printVerbose("bvalid=true roleid = " + rs.getString(1));
				// userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			// return userNameSet;
			return bValid;
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
			releaseConnection(con);
		}
	}

	private void storeUser() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String updateStatement = "  UPDATE " + strUserTable + "  set password = ? , " + " namefirst =  ? , "
					+ " namelast = ? , " + " status = ? , " + " username = ?  " + " where userid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setBytes(1, password);
			prepStmt.setString(2, nameFirst);
			prepStmt.setString(3, nameLast);
			prepStmt.setString(4, status);
			prepStmt.setString(5, userName);
			prepStmt.setInt(6, userId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for userName " + userName + " failed." + " username:" + userName
						+ " nameFirst:" + nameFirst + " nameLast:" + nameLast + " status :" + status);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}
	
	private UserObject getObject(ResultSet rs, String prefix)
	{
		UserObject usrObj = null;
		try
		{
			usrObj = new UserObject();
			usrObj.userId = new Integer(rs.getInt(USERID));
			usrObj.userName = rs.getString(USERNAME);
			usrObj.password = rs.getBytes(PASSWORD);
			usrObj.nameFirst = rs.getString(NAMEFIRST);
			usrObj.nameLast = rs.getString(NAMELAST);
			usrObj.status = rs.getString(STATUS);

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return usrObj;
	}	
} // UserBean
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.vlee.local.ServerConfig;
import com.vlee.util.EncryptionEngine;
import com.vlee.util.Log;

public class UserBean implements EntityBean
{
	private static final long serialVersionUID = 0;
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_index";
	protected final String TABLENAME = "user_index";
	protected final String strObjectName = "UserBean: ";
	// private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String USERID = "userid";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String NAMEFIRST = "namefirst";
	public static final String NAMELAST = "namelast";
	public static final String STATUS = "status";
	public static final Integer USERID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";


	private Integer userId; // primary key!!!
	private String userName = "";
	private byte[] password;
	private String nameFirst = "";
	private String nameLast = "";
	private String status = "";

	public UserObject getObject()
	{
		UserObject uObj = new UserObject();
		uObj.userId = userId;
		uObj.userName = userName;
		uObj.password = password;
		uObj.nameFirst = nameFirst;
		uObj.nameLast = nameLast;
		uObj.status = status;
		return uObj;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer uid)
	{
		this.userId = uid;
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
		return this.userId;
	}

	public void setPrimaryKey(Integer uid)
	{
		this.userId = uid;
	}

	public String getName()
	{
		return nameFirst + " " + nameLast;
	}

	public void setPassword(String passwd)
	{
		EncryptionEngine ee = new EncryptionEngine(passwd);
		ByteArrayOutputStream baos = ee.encrypt(passwd);
		this.password = baos.toByteArray();
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String name)
	{
		this.userName = name;
	}

	public String getNameFirst()
	{
		return nameFirst;
	}

	public void setNameFirst(String name)
	{
		this.nameFirst = name;
	}

	public String getNameLast()
	{
		return nameLast;
	}

	public void setNameLast(String name)
	{
		this.nameLast = name;
	}

	public boolean getValidDomainAccess(String lDomainName)
	{
		boolean bValid = false;
		try
		{
			bValid = validDomainAccess(this.userName, lDomainName);
		} catch (Exception ex)
		{
			throw new EJBException("getValidDomainAccess: " + ex.getMessage());
		}
		return bValid;
	}

	public boolean getValidUser(String uname, String pwd) throws RemoteException
	{
		// filter out inactive users
		if (getStatus().equals(UserBean.INACTIVE))
			return false;
		if (pwd.length() < 1)
			return false;
		EncryptionEngine ee = new EncryptionEngine(pwd);
		ByteArrayInputStream bais = new ByteArrayInputStream(this.password);
		String strDecryptedPwd = ee.decrypt(bais);
		return getUserName().equals(uname) && (strDecryptedPwd.compareTo(pwd) == 0);
	}

	public Integer ejbCreate(String userName, String passwd, String nameFirst, String nameLast) throws CreateException
	{
		Integer uid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			uid = insertUser(userName, passwd, nameFirst, nameLast);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (uid != null)
		{
			this.userName = userName;
			EncryptionEngine ee = new EncryptionEngine(passwd);
			ByteArrayOutputStream baos = ee.encrypt(passwd);
			this.password = baos.toByteArray();
			this.nameFirst = nameFirst;
			this.nameLast = nameLast;
			this.status = UserBean.ACTIVE;
			this.userId = uid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return uid;
	}

	public Integer ejbFindByPrimaryKey(Integer uid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(uid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return uid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + uid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUsername(String uname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUsername");
		Integer bufInt = null;
		try
		{
			bufInt = selectByUsername(uname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUsername: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUsername");
		if (bufInt != null)
		{
			Log.printVerbose("see this: " + bufInt.toString());
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("User " + uname + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteUser(this.userName);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		// makeConnection();
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		// releaseConnection(con)
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.userId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userId = null;
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
			storeUser();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String userName, String passwd, String nameFirst, String nameLast)
	{
	}

	public Collection ejbFindAllUsers()
	{
		try
		{
			Collection bufAL = selectAllUserNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindUsersGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectUsersGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(String field1, String value1, String field2, String value2,
			String field3, String value3)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(field1, value1, field2, value2, field3, value3);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private Connection makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			// Log.printVerbose("Getting connection...");
			// con = ds.getConnection();
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection(Connection con)
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

	private Integer insertUser(String uname, String pwd, String fname, String lname) throws SQLException
	{
		Connection con = null;
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Integer newUid = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select userid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userid"))
				{
					bufInt = rs.getInt("userid");
				}
			}
			newUid = new Integer(bufInt + 1); // new Integer(rs.getInt(1) +
			// 1);
			Log.printVerbose("The new userid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userid, username, password, namefirst, "
					+ " namelast, status ) " + " values ( ?, ? , ? , ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, uname);
			EncryptionEngine ee = new EncryptionEngine(pwd);
			ByteArrayOutputStream baos = ee.encrypt(pwd);
			this.password = baos.toByteArray();
			insertStmt.setBytes(3, this.password);
			insertStmt.setString(4, fname);
			insertStmt.setString(5, lname);
			insertStmt.setString(6, UserBean.ACTIVE);
			insertStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Created user :" + uname + "Name :" + fname + " " + lname);
			Log.printVerbose(strObjectName + "leaving insertUser");
			// releaseConnection(con);
			// return newUid;
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
			releaseConnection(con);
		}
		return newUid;
	}

	private Integer selectByUsername(String uname) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userid " + "from " + strUserTable + " where username = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, uname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("userid"));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
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
			releaseConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer uid) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, uid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private void deleteUser(String userName) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where username = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, userName);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted user: " + userName);
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Vector selectValueObjectsGiven(String field1, String value1, String field2, String value2, String field3,
			String value3) throws SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStmt = " SELECT * " + " FROM " + strUserTable + "  WHERE status!= '*' ";
			if (field1 != null && value1 != null)
			{
				selectStmt += " AND " + field1 + " = '" + value1 + "' ";
			}
			if (field2 != null && value2 != null)
			{
				selectStmt += " AND " + field2 + " = '" + value2 + "' ";
			}
			if (field3 != null && value3 != null)
			{
				selectStmt += " AND " + field3 + " = '" + value3 + "' ";
			}
			selectStmt += " ORDER BY " + USERNAME;
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				UserObject usrObj = new UserObject();
				usrObj.userName = rs.getString(USERNAME);
				usrObj.password = rs.getBytes(PASSWORD);
				usrObj.nameFirst = rs.getString(NAMEFIRST);
				usrObj.nameLast = rs.getString(NAMELAST);
				usrObj.status = rs.getString(STATUS);
				usrObj.userId = new Integer(rs.getInt(USERID));
				vecValObj.add(usrObj);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
		return vecValObj;
	}

	// ////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select username, password, namefirst, namelast, " + " status, userid " + "from "
					+ strUserTable + "  where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, userId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userName = rs.getString(1);
				this.password = rs.getBytes(2);
				this.nameFirst = rs.getString(3);
				this.nameLast = rs.getString(4);
				this.status = rs.getString(5);
				this.userId = new Integer(rs.getInt(6));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for userName " + userName + userId.toString()
						+ " not found in database.");
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Collection selectAllUserNames() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private Collection selectUsersGiven(String fieldName, String value) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private boolean validDomainAccess(String lUsrName, String lDmainCode) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + lUsrName + " " + lDmainCode);
			boolean bValid = false;
			// ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_userrole_link.roleid from user_userrole_link where user_userrole_link.userid in (select user_index.userid from user_index where user_index.username = ?) ) and user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, lUsrName);
			prepStmt.setString(2, lDmainCode);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				bValid = true;
				Log.printVerbose("bvalid=true roleid = " + rs.getString(1));
				// userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			// return userNameSet;
			return bValid;
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
			releaseConnection(con);
		}
	}

	private void storeUser() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String updateStatement = "  UPDATE " + strUserTable + "  set password = ? , " + " namefirst =  ? , "
					+ " namelast = ? , " + " status = ? , " + " username = ?  " + " where userid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setBytes(1, password);
			prepStmt.setString(2, nameFirst);
			prepStmt.setString(3, nameLast);
			prepStmt.setString(4, status);
			prepStmt.setString(5, userName);
			prepStmt.setInt(6, userId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for userName " + userName + " failed." + " username:" + userName
						+ " nameFirst:" + nameFirst + " nameLast:" + nameLast + " status :" + status);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}
} // UserBean
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.vlee.local.ServerConfig;
import com.vlee.util.EncryptionEngine;
import com.vlee.util.Log;

public class UserBean implements EntityBean
{
	private static final long serialVersionUID = 0;
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_index";
	protected final String TABLENAME = "user_index";
	protected final String strObjectName = "UserBean: ";
	// private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";
	public static final String USERID = "userid";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String NAMEFIRST = "namefirst";
	public static final String NAMELAST = "namelast";
	public static final String STATUS = "status";
	public static final Integer USERID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	private Integer userId; // primary key!!!
	private String userName = "";
	private byte[] password;
	private String nameFirst = "";
	private String nameLast = "";
	private String status = "";

	public UserObject getObject()
	{
		UserObject uObj = new UserObject();
		uObj.userId = userId;
		uObj.userName = userName;
		uObj.password = password;
		uObj.nameFirst = nameFirst;
		uObj.nameLast = nameLast;
		uObj.status = status;
		return uObj;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer uid)
	{
		this.userId = uid;
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
		return this.userId;
	}

	public void setPrimaryKey(Integer uid)
	{
		this.userId = uid;
	}

	public String getName()
	{
		return nameFirst + " " + nameLast;
	}

	public void setPassword(String passwd)
	{
		EncryptionEngine ee = new EncryptionEngine(passwd);
		ByteArrayOutputStream baos = ee.encrypt(passwd);
		this.password = baos.toByteArray();
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String name)
	{
		this.userName = name;
	}

	public String getNameFirst()
	{
		return nameFirst;
	}

	public void setNameFirst(String name)
	{
		this.nameFirst = name;
	}

	public String getNameLast()
	{
		return nameLast;
	}

	public void setNameLast(String name)
	{
		this.nameLast = name;
	}

	public boolean getValidDomainAccess(String lDomainName)
	{
		boolean bValid = false;
		try
		{
			bValid = validDomainAccess(this.userName, lDomainName);
		} catch (Exception ex)
		{
			throw new EJBException("getValidDomainAccess: " + ex.getMessage());
		}
		return bValid;
	}

	public boolean getValidUser(String uname, String pwd) throws RemoteException
	{
		// filter out inactive users
		if (getStatus().equals(UserBean.INACTIVE))
			return false;
		if (pwd.length() < 1)
			return false;
		EncryptionEngine ee = new EncryptionEngine(pwd);
		ByteArrayInputStream bais = new ByteArrayInputStream(this.password);
		String strDecryptedPwd = ee.decrypt(bais);
		return getUserName().equals(uname) && (strDecryptedPwd.compareTo(pwd) == 0);
	}

	public Integer ejbCreate(String userName, String passwd, String nameFirst, String nameLast) throws CreateException
	{
		Integer uid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			uid = insertUser(userName, passwd, nameFirst, nameLast);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (uid != null)
		{
			this.userName = userName;
			EncryptionEngine ee = new EncryptionEngine(passwd);
			ByteArrayOutputStream baos = ee.encrypt(passwd);
			this.password = baos.toByteArray();
			this.nameFirst = nameFirst;
			this.nameLast = nameLast;
			this.status = UserBean.ACTIVE;
			this.userId = uid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return uid;
	}

	public Integer ejbFindByPrimaryKey(Integer uid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(uid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return uid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + uid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUsername(String uname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUsername");
		Integer bufInt = null;
		try
		{
			bufInt = selectByUsername(uname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUsername: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUsername");
		if (bufInt != null)
		{
			Log.printVerbose("see this: " + bufInt.toString());
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("User " + uname + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteUser(this.userName);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		// makeConnection();
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		// releaseConnection(con)
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.userId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userId = null;
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
			storeUser();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String userName, String passwd, String nameFirst, String nameLast)
	{
	}

	public Collection ejbFindAllUsers()
	{
		try
		{
			Collection bufAL = selectAllUserNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindUsersGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectUsersGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(String field1, String value1, String field2, String value2,
			String field3, String value3)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(field1, value1, field2, value2, field3, value3);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private Connection makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			// Log.printVerbose("Getting connection...");
			// con = ds.getConnection();
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection(Connection con)
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

	private Integer insertUser(String uname, String pwd, String fname, String lname) throws SQLException
	{
		Connection con = null;
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Integer newUid = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select userid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userid"))
				{
					bufInt = rs.getInt("userid");
				}
			}
			newUid = new Integer(bufInt + 1); // new Integer(rs.getInt(1) +
			// 1);
			Log.printVerbose("The new userid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userid, username, password, namefirst, "
					+ " namelast, status ) " + " values ( ?, ? , ? , ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, uname);
			EncryptionEngine ee = new EncryptionEngine(pwd);
			ByteArrayOutputStream baos = ee.encrypt(pwd);
			this.password = baos.toByteArray();
			insertStmt.setBytes(3, this.password);
			insertStmt.setString(4, fname);
			insertStmt.setString(5, lname);
			insertStmt.setString(6, UserBean.ACTIVE);
			insertStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Created user :" + uname + "Name :" + fname + " " + lname);
			Log.printVerbose(strObjectName + "leaving insertUser");
			// releaseConnection(con);
			// return newUid;
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
			releaseConnection(con);
		}
		return newUid;
	}

	private Integer selectByUsername(String uname) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userid " + "from " + strUserTable + " where username = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, uname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("userid"));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
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
			releaseConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer uid) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, uid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private void deleteUser(String userName) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where username = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, userName);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted user: " + userName);
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Vector selectValueObjectsGiven(String field1, String value1, String field2, String value2, String field3,
			String value3) throws SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStmt = " SELECT * " + " FROM " + strUserTable + "  WHERE status!= '*' ";
			if (field1 != null && value1 != null)
			{
				selectStmt += " AND " + field1 + " = '" + value1 + "' ";
			}
			if (field2 != null && value2 != null)
			{
				selectStmt += " AND " + field2 + " = '" + value2 + "' ";
			}
			if (field3 != null && value3 != null)
			{
				selectStmt += " AND " + field3 + " = '" + value3 + "' ";
			}
			selectStmt += " ORDER BY " + USERNAME;
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				UserObject usrObj = new UserObject();
				usrObj.userName = rs.getString(USERNAME);
				usrObj.password = rs.getBytes(PASSWORD);
				usrObj.nameFirst = rs.getString(NAMEFIRST);
				usrObj.nameLast = rs.getString(NAMELAST);
				usrObj.status = rs.getString(STATUS);
				usrObj.userId = new Integer(rs.getInt(USERID));
				vecValObj.add(usrObj);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
		return vecValObj;
	}

	// ////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select username, password, namefirst, namelast, " + " status, userid " + "from "
					+ strUserTable + "  where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, userId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userName = rs.getString(1);
				this.password = rs.getBytes(2);
				this.nameFirst = rs.getString(3);
				this.nameLast = rs.getString(4);
				this.status = rs.getString(5);
				this.userId = new Integer(rs.getInt(6));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for userName " + userName + userId.toString()
						+ " not found in database.");
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Collection selectAllUserNames() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private Collection selectUsersGiven(String fieldName, String value) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private boolean validDomainAccess(String lUsrName, String lDmainCode) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + lUsrName + " " + lDmainCode);
			boolean bValid = false;
			// ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_userrole_link.roleid from user_userrole_link where user_userrole_link.userid in (select user_index.userid from user_index where user_index.username = ?) ) and user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, lUsrName);
			prepStmt.setString(2, lDmainCode);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				bValid = true;
				Log.printVerbose("bvalid=true roleid = " + rs.getString(1));
				// userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			// return userNameSet;
			return bValid;
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
			releaseConnection(con);
		}
	}

	private void storeUser() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String updateStatement = "  UPDATE " + strUserTable + "  set password = ? , " + " namefirst =  ? , "
					+ " namelast = ? , " + " status = ? , " + " username = ?  " + " where userid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setBytes(1, password);
			prepStmt.setString(2, nameFirst);
			prepStmt.setString(3, nameLast);
			prepStmt.setString(4, status);
			prepStmt.setString(5, userName);
			prepStmt.setInt(6, userId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for userName " + userName + " failed." + " username:" + userName
						+ " nameFirst:" + nameFirst + " nameLast:" + nameLast + " status :" + status);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}
} // UserBean
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.vlee.local.ServerConfig;
import com.vlee.util.EncryptionEngine;
import com.vlee.util.Log;

public class UserBean implements EntityBean
{
	private static final long serialVersionUID = 0;
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strUserTable = "user_index";
	protected final String TABLENAME = "user_index";
	protected final String strObjectName = "UserBean: ";
	// private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String USERID = "userid";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String NAMEFIRST = "namefirst";
	public static final String NAMELAST = "namelast";
	public static final String STATUS = "status";
	public static final Integer USERID_DEVELOPER = new Integer("500");
	// members ----------------------------------------------
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String SYSADMIN = "sysadmin";


	private Integer userId; // primary key!!!
	private String userName = "";
	private byte[] password;
	private String nameFirst = "";
	private String nameLast = "";
	private String status = "";

	public UserObject getObject()
	{
		UserObject uObj = new UserObject();
		uObj.userId = userId;
		uObj.userName = userName;
		uObj.password = password;
		uObj.nameFirst = nameFirst;
		uObj.nameLast = nameLast;
		uObj.status = status;
		return uObj;
	}

	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer uid)
	{
		this.userId = uid;
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
		return this.userId;
	}

	public void setPrimaryKey(Integer uid)
	{
		this.userId = uid;
	}

	public String getName()
	{
		return nameFirst + " " + nameLast;
	}

	public void setPassword(String passwd)
	{
		EncryptionEngine ee = new EncryptionEngine(passwd);
		ByteArrayOutputStream baos = ee.encrypt(passwd);
		this.password = baos.toByteArray();
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String name)
	{
		this.userName = name;
	}

	public String getNameFirst()
	{
		return nameFirst;
	}

	public void setNameFirst(String name)
	{
		this.nameFirst = name;
	}

	public String getNameLast()
	{
		return nameLast;
	}

	public void setNameLast(String name)
	{
		this.nameLast = name;
	}

	public boolean getValidDomainAccess(String lDomainName)
	{
		boolean bValid = false;
		try
		{
			bValid = validDomainAccess(this.userName, lDomainName);
		} catch (Exception ex)
		{
			throw new EJBException("getValidDomainAccess: " + ex.getMessage());
		}
		return bValid;
	}

	public boolean getValidUser(String uname, String pwd) throws RemoteException
	{
		// filter out inactive users
		if (getStatus().equals(UserBean.INACTIVE))
			return false;
		if (pwd.length() < 1)
			return false;
		EncryptionEngine ee = new EncryptionEngine(pwd);
		ByteArrayInputStream bais = new ByteArrayInputStream(this.password);
		String strDecryptedPwd = ee.decrypt(bais);
		return getUserName().equals(uname) && (strDecryptedPwd.compareTo(pwd) == 0);
	}

	public Integer ejbCreate(String userName, String passwd, String nameFirst, String nameLast) throws CreateException
	{
		Integer uid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			uid = insertUser(userName, passwd, nameFirst, nameLast);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (uid != null)
		{
			this.userName = userName;
			EncryptionEngine ee = new EncryptionEngine(passwd);
			ByteArrayOutputStream baos = ee.encrypt(passwd);
			this.password = baos.toByteArray();
			this.nameFirst = nameFirst;
			this.nameLast = nameLast;
			this.status = UserBean.ACTIVE;
			this.userId = uid;
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return uid;
	}

	public Integer ejbFindByPrimaryKey(Integer uid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(uid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return uid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + uid.toString() + " not found.");
		}
	}

	public Integer ejbFindByUsername(String uname) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByUsername");
		Integer bufInt = null;
		try
		{
			bufInt = selectByUsername(uname);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByUsername: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbFindByUsername");
		if (bufInt != null)
		{
			Log.printVerbose("see this: " + bufInt.toString());
			return bufInt;
		} else
		{
			throw new ObjectNotFoundException("User " + uname + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteUser(this.userName);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
		// makeConnection();
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		// releaseConnection(con)
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.userId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.userId = null;
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
			storeUser();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(String userName, String passwd, String nameFirst, String nameLast)
	{
	}

	public Collection ejbFindAllUsers()
	{
		try
		{
			Collection bufAL = selectAllUserNames();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllUsers: " + ex);
			return null;
		}
	}

	public Collection ejbFindUsersGiven(String fieldName, String value)
	{
		try
		{
			Collection bufAL = selectUsersGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindUsersGiven: " + ex);
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(String field1, String value1, String field2, String value2,
			String field3, String value3)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(field1, value1, field2, value2, field3, value3);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private Connection makeConnection()
	// throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			// Log.printVerbose("Getting connection...");
			// con = ds.getConnection();
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection(Connection con)
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

	private Integer insertUser(String uname, String pwd, String fname, String lname) throws SQLException
	{
		Connection con = null;
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		Integer newUid = null;
		try
		{
			con = makeConnection();
			Log.printVerbose(strObjectName + "insertUser: ");
			String findMaxUidStmt = " select userid from " + strUserTable + " ";
			selectStmt = con.prepareStatement(findMaxUidStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("userid"))
				{
					bufInt = rs.getInt("userid");
				}
			}
			newUid = new Integer(bufInt + 1); // new Integer(rs.getInt(1) +
			// 1);
			Log.printVerbose("The new userid is :" + newUid.toString());
			String insertStatement = "insert into " + strUserTable + " (userid, username, password, namefirst, "
					+ " namelast, status ) " + " values ( ?, ? , ? , ?, ?, ?)";
			insertStmt = con.prepareStatement(insertStatement);
			insertStmt.setInt(1, newUid.intValue());
			insertStmt.setString(2, uname);
			EncryptionEngine ee = new EncryptionEngine(pwd);
			ByteArrayOutputStream baos = ee.encrypt(pwd);
			this.password = baos.toByteArray();
			insertStmt.setBytes(3, this.password);
			insertStmt.setString(4, fname);
			insertStmt.setString(5, lname);
			insertStmt.setString(6, UserBean.ACTIVE);
			insertStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Created user :" + uname + "Name :" + fname + " " + lname);
			Log.printVerbose(strObjectName + "leaving insertUser");
			// releaseConnection(con);
			// return newUid;
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
			releaseConnection(con);
		}
		return newUid;
	}

	private Integer selectByUsername(String uname) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			Integer bufInt = null;
			String selectStatement = "select userid " + "from " + strUserTable + " where username = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, uname);
			ResultSet rs = prepStmt.executeQuery();
			boolean lbool = rs.next();
			if (lbool)
			{
				bufInt = new Integer(rs.getInt("userid"));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return bufInt;
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
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
			releaseConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer uid) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select * " + "from " + strUserTable + " where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, uid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private void deleteUser(String userName) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String deleteStatement = "delete from " + strUserTable + "  " + "where username = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setString(1, userName);
			prepStmt.executeUpdate();
			// prepStmt.close();
			Log.printAudit(strObjectName + "Deleted user: " + userName);
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Vector selectValueObjectsGiven(String field1, String value1, String field2, String value2, String field3,
			String value3) throws SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStmt = " SELECT * " + " FROM " + strUserTable + "  WHERE status!= '*' ";
			if (field1 != null && value1 != null)
			{
				selectStmt += " AND " + field1 + " = '" + value1 + "' ";
			}
			if (field2 != null && value2 != null)
			{
				selectStmt += " AND " + field2 + " = '" + value2 + "' ";
			}
			if (field3 != null && value3 != null)
			{
				selectStmt += " AND " + field3 + " = '" + value3 + "' ";
			}
			selectStmt += " ORDER BY " + USERNAME;
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				UserObject usrObj = new UserObject();
				usrObj.userName = rs.getString(USERNAME);
				usrObj.password = rs.getBytes(PASSWORD);
				usrObj.nameFirst = rs.getString(NAMEFIRST);
				usrObj.nameLast = rs.getString(NAMELAST);
				usrObj.status = rs.getString(STATUS);
				usrObj.userId = new Integer(rs.getInt(USERID));
				vecValObj.add(usrObj);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
		return vecValObj;
	}

	// ////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = "select username, password, namefirst, namelast, " + " status, userid " + "from "
					+ strUserTable + "  where userid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, userId.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.userName = rs.getString(1);
				this.password = rs.getBytes(2);
				this.nameFirst = rs.getString(3);
				this.nameLast = rs.getString(4);
				this.status = rs.getString(5);
				this.userId = new Integer(rs.getInt(6));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for userName " + userName + userId.toString()
						+ " not found in database.");
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}

	private Collection selectAllUserNames() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private Collection selectUsersGiven(String fieldName, String value) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = "select userid from " + strUserTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			return userNameSet;
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
			releaseConnection(con);
		}
	}

	private boolean validDomainAccess(String lUsrName, String lDmainCode) throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + lUsrName + " " + lDmainCode);
			boolean bValid = false;
			// ArrayList userNameSet = new ArrayList();
			con = makeConnection();
			String selectStatement = " select user_role_index.roleid from user_role_index where user_role_index.roleid in ( select user_userrole_link.roleid from user_userrole_link where user_userrole_link.userid in (select user_index.userid from user_index where user_index.username = ?) ) and user_role_index.roleid in ( select user_roledomain_link.roleid from user_roledomain_link where user_roledomain_link.domainid in ( select user_domain_index.domainid from user_domain_index where user_domain_index.domainname = ? ) ) ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, lUsrName);
			prepStmt.setString(2, lDmainCode);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				bValid = true;
				Log.printVerbose("bvalid=true roleid = " + rs.getString(1));
				// userNameSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// releaseConnection(con);
			// return userNameSet;
			return bValid;
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
			releaseConnection(con);
		}
	}

	private void storeUser() throws SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String updateStatement = "  UPDATE " + strUserTable + "  set password = ? , " + " namefirst =  ? , "
					+ " namelast = ? , " + " status = ? , " + " username = ?  " + " where userid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setBytes(1, password);
			prepStmt.setString(2, nameFirst);
			prepStmt.setString(3, nameLast);
			prepStmt.setString(4, status);
			prepStmt.setString(5, userName);
			prepStmt.setInt(6, userId.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for userName " + userName + " failed." + " username:" + userName
						+ " nameFirst:" + nameFirst + " nameLast:" + nameLast + " status :" + status);
			}
			// releaseConnection(con);
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
			releaseConnection(con);
		}
	}
} // UserBean
