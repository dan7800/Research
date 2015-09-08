// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class GLLinkBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "acc_gl_link";
	protected final String strObjectName = "GLLinkBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public final String SUPPLIER = "supplier";
	public final String CUSTOMER = "customer";
	public final String EMPLOYEE = "employee";
	public final String INVENTORY = "inventory";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private String mCode;
	private String mModule;
	private String mModContext;
	private Integer mGLid; // Refers to General Ledger ID
	// PC Center is already embedded in acc_general_ledger.sql
	// Hence, PC Center should not be duplicated elsewhere.
	private Integer mAccountId; // Refers to External Module Account Reference
								// ID

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public String getCode()
	{
		return this.mCode;
	}

	public void setCode(String strCode)
	{
		this.mCode = strCode;
	}

	public String getModule()
	{
		return this.mModule;
	}

	public void setModule(String strModule)
	{
		this.mModule = strModule;
	}

	public String getContext()
	{
		return this.mModContext;
	}

	public void setContext(String strContext)
	{
		this.mModContext = strContext;
	}

	public Integer getGLid()
	{
		return this.mGLid;
	}

	public void setGLid(Integer glid)
	{
		this.mGLid = glid;
	}

	public Integer getAccountId()
	{
		return this.mAccountId;
	}

	public void setAccountId(Integer accid)
	{
		this.mAccountId = accid;
	}

	public Integer getPrimaryKey()
	{
		return this.mPKid;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer ejbCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(strCode, strModule, strContext, iGLid, iAccId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue());
			// primary key!!!
			this.mCode = strCode;
			this.mModule = strModule;
			this.mModContext = strContext;
			this.mGLid = iGLid;
			this.mAccountId = iAccId;
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
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
	{
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		try
		{
			Collection bufAL = selectAllObjects();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllObjects: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
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
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws SQLException
	{
		makeConnection();
		Log.printVerbose(strObjectName + "insertNewRow: ");
		String findMaxPkidStmt = " select pkid from " + strObjectTable + " ";
		PreparedStatement prepStmt = con.prepareStatement(findMaxPkidStmt);
		ResultSet rs = prepStmt.executeQuery();
		int bufInt = 0;
		while (rs.next())
		{
			if (bufInt < rs.getInt("pkid"))
			{
				bufInt = rs.getInt("pkid");
			}
		}
		Integer newPkid = new Integer(bufInt + 1); // new Integer(rs.getInt(1)
													// + 1);
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "insert into " + strObjectTable
				+ " (pkid ,  code, module, context, glid, accountid ) " + " values ( ?, ?,?,? ,? , ? )";
		prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setInt(1, newPkid.intValue());
		prepStmt.setString(2, strCode);
		prepStmt.setString(3, strModule);
		prepStmt.setString(4, strContext);
		prepStmt.setInt(5, iGLid.intValue());
		prepStmt.setInt(6, iAccId.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, pkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setInt(1, pkid.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "select pkid, code, module, context, glid, accountid  " + " from " + strObjectTable
				+ " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mPKid = new Integer(rs.getInt("pkid"));
			this.mCode = rs.getString("code");
			this.mModule = rs.getString("module");
			this.mModContext = rs.getString("context");
			this.mGLid = new Integer(rs.getInt("glid"));
			this.mAccountId = new Integer(rs.getInt("accountid"));
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private Collection selectAllObjects() throws SQLException
	{
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		// prepStmt.setString(2, value);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " code = ? , " + " module = ? , "
				+ " context = ? , " + " glid = ? " + " accountid = ? " + " where pkid = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		prepStmt.setString(2, this.mCode);
		prepStmt.setString(3, this.mModule);
		prepStmt.setString(4, this.mModContext);
		prepStmt.setInt(3, this.mGLid.intValue());
		prepStmt.setInt(4, this.mAccountId.intValue());
		prepStmt.setInt(5, this.mPKid.intValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
		}
		releaseConnection();
	}
} // UserBean
// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class GLLinkBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "acc_gl_link";
	protected final String strObjectName = "GLLinkBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public final String SUPPLIER = "supplier";
	public final String CUSTOMER = "customer";
	public final String EMPLOYEE = "employee";
	public final String INVENTORY = "inventory";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private String mCode;
	private String mModule;
	private String mModContext;
	private Integer mGLid; // Refers to General Ledger ID
	// PC Center is already embedded in acc_general_ledger.sql
	// Hence, PC Center should not be duplicated elsewhere.
	private Integer mAccountId; // Refers to External Module Account Reference
								// ID

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public String getCode()
	{
		return this.mCode;
	}

	public void setCode(String strCode)
	{
		this.mCode = strCode;
	}

	public String getModule()
	{
		return this.mModule;
	}

	public void setModule(String strModule)
	{
		this.mModule = strModule;
	}

	public String getContext()
	{
		return this.mModContext;
	}

	public void setContext(String strContext)
	{
		this.mModContext = strContext;
	}

	public Integer getGLid()
	{
		return this.mGLid;
	}

	public void setGLid(Integer glid)
	{
		this.mGLid = glid;
	}

	public Integer getAccountId()
	{
		return this.mAccountId;
	}

	public void setAccountId(Integer accid)
	{
		this.mAccountId = accid;
	}

	public Integer getPrimaryKey()
	{
		return this.mPKid;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer ejbCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(strCode, strModule, strContext, iGLid, iAccId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue());
			// primary key!!!
			this.mCode = strCode;
			this.mModule = strModule;
			this.mModContext = strContext;
			this.mGLid = iGLid;
			this.mAccountId = iAccId;
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
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
	{
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		try
		{
			Collection bufAL = selectAllObjects();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllObjects: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
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
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws SQLException
	{
		makeConnection();
		Log.printVerbose(strObjectName + "insertNewRow: ");
		String findMaxPkidStmt = " select pkid from " + strObjectTable + " ";
		PreparedStatement prepStmt = con.prepareStatement(findMaxPkidStmt);
		ResultSet rs = prepStmt.executeQuery();
		int bufInt = 0;
		while (rs.next())
		{
			if (bufInt < rs.getInt("pkid"))
			{
				bufInt = rs.getInt("pkid");
			}
		}
		Integer newPkid = new Integer(bufInt + 1); // new Integer(rs.getInt(1)
													// + 1);
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "insert into " + strObjectTable
				+ " (pkid ,  code, module, context, glid, accountid ) " + " values ( ?, ?,?,? ,? , ? )";
		prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setInt(1, newPkid.intValue());
		prepStmt.setString(2, strCode);
		prepStmt.setString(3, strModule);
		prepStmt.setString(4, strContext);
		prepStmt.setInt(5, iGLid.intValue());
		prepStmt.setInt(6, iAccId.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, pkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setInt(1, pkid.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "select pkid, code, module, context, glid, accountid  " + " from " + strObjectTable
				+ " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mPKid = new Integer(rs.getInt("pkid"));
			this.mCode = rs.getString("code");
			this.mModule = rs.getString("module");
			this.mModContext = rs.getString("context");
			this.mGLid = new Integer(rs.getInt("glid"));
			this.mAccountId = new Integer(rs.getInt("accountid"));
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private Collection selectAllObjects() throws SQLException
	{
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		// prepStmt.setString(2, value);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " code = ? , " + " module = ? , "
				+ " context = ? , " + " glid = ? " + " accountid = ? " + " where pkid = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		prepStmt.setString(2, this.mCode);
		prepStmt.setString(3, this.mModule);
		prepStmt.setString(4, this.mModContext);
		prepStmt.setInt(3, this.mGLid.intValue());
		prepStmt.setInt(4, this.mAccountId.intValue());
		prepStmt.setInt(5, this.mPKid.intValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
		}
		releaseConnection();
	}
} // UserBean
// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class GLLinkBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "acc_gl_link";
	protected final String strObjectName = "GLLinkBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public final String SUPPLIER = "supplier";
	public final String CUSTOMER = "customer";
	public final String EMPLOYEE = "employee";
	public final String INVENTORY = "inventory";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private String mCode;
	private String mModule;
	private String mModContext;
	private Integer mGLid; // Refers to General Ledger ID
	// PC Center is already embedded in acc_general_ledger.sql
	// Hence, PC Center should not be duplicated elsewhere.
	private Integer mAccountId; // Refers to External Module Account Reference
								// ID

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public String getCode()
	{
		return this.mCode;
	}

	public void setCode(String strCode)
	{
		this.mCode = strCode;
	}

	public String getModule()
	{
		return this.mModule;
	}

	public void setModule(String strModule)
	{
		this.mModule = strModule;
	}

	public String getContext()
	{
		return this.mModContext;
	}

	public void setContext(String strContext)
	{
		this.mModContext = strContext;
	}

	public Integer getGLid()
	{
		return this.mGLid;
	}

	public void setGLid(Integer glid)
	{
		this.mGLid = glid;
	}

	public Integer getAccountId()
	{
		return this.mAccountId;
	}

	public void setAccountId(Integer accid)
	{
		this.mAccountId = accid;
	}

	public Integer getPrimaryKey()
	{
		return this.mPKid;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer ejbCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(strCode, strModule, strContext, iGLid, iAccId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue());
			// primary key!!!
			this.mCode = strCode;
			this.mModule = strModule;
			this.mModContext = strContext;
			this.mGLid = iGLid;
			this.mAccountId = iAccId;
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
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
	{
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		try
		{
			Collection bufAL = selectAllObjects();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllObjects: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
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
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws SQLException
	{
		makeConnection();
		Log.printVerbose(strObjectName + "insertNewRow: ");
		String findMaxPkidStmt = " select pkid from " + strObjectTable + " ";
		PreparedStatement prepStmt = con.prepareStatement(findMaxPkidStmt);
		ResultSet rs = prepStmt.executeQuery();
		int bufInt = 0;
		while (rs.next())
		{
			if (bufInt < rs.getInt("pkid"))
			{
				bufInt = rs.getInt("pkid");
			}
		}
		Integer newPkid = new Integer(bufInt + 1); // new Integer(rs.getInt(1)
													// + 1);
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "insert into " + strObjectTable
				+ " (pkid ,  code, module, context, glid, accountid ) " + " values ( ?, ?,?,? ,? , ? )";
		prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setInt(1, newPkid.intValue());
		prepStmt.setString(2, strCode);
		prepStmt.setString(3, strModule);
		prepStmt.setString(4, strContext);
		prepStmt.setInt(5, iGLid.intValue());
		prepStmt.setInt(6, iAccId.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, pkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setInt(1, pkid.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "select pkid, code, module, context, glid, accountid  " + " from " + strObjectTable
				+ " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mPKid = new Integer(rs.getInt("pkid"));
			this.mCode = rs.getString("code");
			this.mModule = rs.getString("module");
			this.mModContext = rs.getString("context");
			this.mGLid = new Integer(rs.getInt("glid"));
			this.mAccountId = new Integer(rs.getInt("accountid"));
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private Collection selectAllObjects() throws SQLException
	{
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		rs.beforeFirst();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		// prepStmt.setString(2, value);
		ResultSet rs = prepStmt.executeQuery();
		rs.beforeFirst();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " code = ? , " + " module = ? , "
				+ " context = ? , " + " glid = ? " + " accountid = ? " + " where pkid = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		prepStmt.setString(2, this.mCode);
		prepStmt.setString(3, this.mModule);
		prepStmt.setString(4, this.mModContext);
		prepStmt.setInt(3, this.mGLid.intValue());
		prepStmt.setInt(4, this.mAccountId.intValue());
		prepStmt.setInt(5, this.mPKid.intValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
		}
		releaseConnection();
	}
} // UserBean
// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class GLLinkBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "acc_gl_link";
	protected final String strObjectName = "GLLinkBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public final String SUPPLIER = "supplier";
	public final String CUSTOMER = "customer";
	public final String EMPLOYEE = "employee";
	public final String INVENTORY = "inventory";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private String mCode;
	private String mModule;
	private String mModContext;
	private Integer mGLid; // Refers to General Ledger ID
	// PC Center is already embedded in acc_general_ledger.sql
	// Hence, PC Center should not be duplicated elsewhere.
	private Integer mAccountId; // Refers to External Module Account Reference
								// ID

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public String getCode()
	{
		return this.mCode;
	}

	public void setCode(String strCode)
	{
		this.mCode = strCode;
	}

	public String getModule()
	{
		return this.mModule;
	}

	public void setModule(String strModule)
	{
		this.mModule = strModule;
	}

	public String getContext()
	{
		return this.mModContext;
	}

	public void setContext(String strContext)
	{
		this.mModContext = strContext;
	}

	public Integer getGLid()
	{
		return this.mGLid;
	}

	public void setGLid(Integer glid)
	{
		this.mGLid = glid;
	}

	public Integer getAccountId()
	{
		return this.mAccountId;
	}

	public void setAccountId(Integer accid)
	{
		this.mAccountId = accid;
	}

	public Integer getPrimaryKey()
	{
		return this.mPKid;
	}

	public void setPrimaryKey(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer ejbCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(strCode, strModule, strContext, iGLid, iAccId);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue());
			// primary key!!!
			this.mCode = strCode;
			this.mModule = strModule;
			this.mModContext = strContext;
			this.mGLid = iGLid;
			this.mAccountId = iAccId;
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
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
	{
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		try
		{
			Collection bufAL = selectAllObjects();
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindAllObjects: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
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
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(String strCode, String strModule, String strContext, Integer iGLid, Integer iAccId)
			throws SQLException
	{
		makeConnection();
		Log.printVerbose(strObjectName + "insertNewRow: ");
		String findMaxPkidStmt = " select pkid from " + strObjectTable + " ";
		PreparedStatement prepStmt = con.prepareStatement(findMaxPkidStmt);
		ResultSet rs = prepStmt.executeQuery();
		int bufInt = 0;
		while (rs.next())
		{
			if (bufInt < rs.getInt("pkid"))
			{
				bufInt = rs.getInt("pkid");
			}
		}
		Integer newPkid = new Integer(bufInt + 1); // new Integer(rs.getInt(1)
													// + 1);
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "insert into " + strObjectTable
				+ " (pkid ,  code, module, context, glid, accountid ) " + " values ( ?, ?,?,? ,? , ? )";
		prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setInt(1, newPkid.intValue());
		prepStmt.setString(2, strCode);
		prepStmt.setString(3, strModule);
		prepStmt.setString(4, strContext);
		prepStmt.setInt(5, iGLid.intValue());
		prepStmt.setInt(6, iAccId.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, pkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setInt(1, pkid.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "select pkid, code, module, context, glid, accountid  " + " from " + strObjectTable
				+ " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mPKid = new Integer(rs.getInt("pkid"));
			this.mCode = rs.getString("code");
			this.mModule = rs.getString("module");
			this.mModContext = rs.getString("context");
			this.mGLid = new Integer(rs.getInt("glid"));
			this.mAccountId = new Integer(rs.getInt("accountid"));
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private Collection selectAllObjects() throws SQLException
	{
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = "select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		// prepStmt.setString(2, value);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " code = ? , " + " module = ? , "
				+ " context = ? , " + " glid = ? " + " accountid = ? " + " where pkid = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setInt(1, this.mPKid.intValue());
		prepStmt.setString(2, this.mCode);
		prepStmt.setString(3, this.mModule);
		prepStmt.setString(4, this.mModContext);
		prepStmt.setInt(3, this.mGLid.intValue());
		prepStmt.setInt(4, this.mAccountId.intValue());
		prepStmt.setInt(5, this.mPKid.intValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
		}
		releaseConnection();
	}
} // UserBean
