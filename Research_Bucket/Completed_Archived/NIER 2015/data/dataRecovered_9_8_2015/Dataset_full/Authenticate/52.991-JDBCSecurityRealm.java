/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,JDBCSecurityRealm,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001,2002 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     The Initial Developer of the Original Code is Anders W. Tell.
     Portions created by Financial Toolsmiths AB are Copyright (C) 
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File JDBCSecurityRealm.java			*/
package org.openebxml.comp.security.jdk;

/************************************************
	Includes
\************************************************/
import java.util.Vector;	        /* JME CLDC 1.0 */
import java.util.Properties;

import java.sql.*;

import org.openebxml.comp.util.*;
import org.openebxml.comp.security.*;

/**
 *  Class JDBCSecurityRealm
 *
 * @author <a href="mailto:anderst@toolsmiths.se">Anders W. Tell   Financial Toolsmiths AB</a>
 * @version $Id: JDBCSecurityRealm.java,v 1.1 2002/04/14 20:20:06 awtopensource Exp $
 */

public class JDBCSecurityRealm implements SecurityRealmImpl {


	/****/
	protected String fName;

    /****/
    protected   Driver          fDBDriver;
    /****/
    protected   String          fDBDriverName;

	/****/
	protected   String 			fDBUserName;
	/****/
	protected   String 			fDBPassword;
	/****/
	protected	String 			fDBAddress;
	
    /****/
    protected   Connection      fConnection;

	/****/
	protected	PrincipalCacheStrategy	fCache;

	/****/
	protected String		fDB_Credential_Table;
	/****/
	protected String		fDB_Credential_Column;
	/****/
	protected String		fDB_Credential_UserName_Column;
	/****/
	protected int			fDB_Credential_UserName_Index;
	/****/
	protected int			fDB_Credential_Credential_Index;

	/****/
	protected PreparedStatement fDB_Credential_Statement;

	/****/
	protected String		fDB_Roles_Table;
	/****/
	protected String		fDB_Roles_Column;
	/****/
	protected String		fDB_Roles_UserName_Column;
	/****/
	protected int			fDB_Roles_UserName_Index;
	/****/
	protected int			fDB_Roles_RoleName_Index;

	/****/
	protected PreparedStatement fDB_Roles_Statement;

    /****/
    public JDBCSecurityRealm()
    {
		fName				= "";

        fDBDriver   	= null;
		fDBDriverName	= null;

		fDBUserName		= null;
		fDBPassword		= null;
        fConnection 	= null;

		fCache			= new PrincipalLRUCache();

		fDB_Credential_Table			= null;;
		fDB_Credential_Column			= null;
		fDB_Credential_UserName_Column	= null;
		fDB_Credential_UserName_Index	= 1;
		fDB_Credential_Credential_Index	= 1;
		fDB_Credential_Statement		= null;

		fDB_Roles_Table				= null;;
		fDB_Roles_Column			= null;
		fDB_Roles_UserName_Column	= null;
		fDB_Roles_UserName_Index	= 1;
		fDB_Roles_RoleName_Index		= 1;
		fDB_Roles_Statement			= null;
        
    }

	/**
	 * Get the value of Name.
	 * @return value of Name.
	 */
	public String getName() {
		return fName;
	}
	

	/****/
	public void Init(String name, Object config)
//	throws ConfigurationException
    {
        fName               = name;
    }
	
	/**
	 * Get the value of DBDriver.
	 * @return value of DBDriver.
	 */
	public Driver getDBDriver() {
		return fDBDriver;
	}
	
	/**
	 * Set the value of DBDriver.
	 * @param v  Value to assign to DBDriver.
	 */
	public void setDBDriver(Driver  v) {
		this.fDBDriver = v;
	}
	String DBDriverName;
	
	/**
	 * Get the value of DBDriverName.
	 * @r	eturn value of DBDriverName.
	 */
	public String getDBDriverName() {
		return fDBDriverName;
	}
	
	/**
	 * Set the value of DBDriverName.
	 * @param v  Value to assign to DBDriverName.
	 */
	public void setDBDriverName(String  v) {
		this.fDBDriverName = v;
	}
	
	
	/**
	 * Get the value of DBUserName.
	 * @return value of DBUserName.
	 */
	public String getDBUserName() {
		return fDBUserName;
	}
	
	/**
	 * Set the value of DBUserName.
	 * @param v  Value to assign to DBUserName.
	 */
	public void setDBUserName(String  v) {
		this.fDBUserName = v;
	}
	
	
	/**
	 * Get the value of DBPassword.
	 * @return value of DBPassword.
	 */
	public String getDBPassword() {
		return fDBPassword;
	}
	
	/**
	 * Set the value of DBPassword.
	 * @param v  Value to assign to DBPassword.
	 */
	public void setDBPassword(String  v) {
		this.fDBPassword = v;
	}

	
	/**
	 * Get the value of DBAddress.
	 * @return value of DBAddress.
	 */
	public String getDBAddress() {
		return fDBAddress;
	}
	
	/**
	 * Set the value of DBAddress.
	 * @param v  Value to assign to DBAddress.
	 */
	public void setDBAddress(String  v) {
		this.fDBAddress = v;
	}

	
	/**
	 * Get the value of DB_Credential_Table.
	 * @return value of DB_Credential_Table.
	 */
	public String getDB_Credential_Table() {
		return fDB_Credential_Table;
	}
	
	/**
	 * Set the value of DB_Credential_Table.
	 * @param v  Value to assign to DB_Credential_Table.
	 */
	public void setDB_Credential_Table(String  v) {
		this.fDB_Credential_Table = v;
		/* invalidate prepared statement */
		fDB_Credential_Statement	= null;
	}
	
	
	/**
	 * Get the value of DB_Credential_Column.
	 * @return value of DB_Credential_Column.
	 */
	public String getDB_Credential_Column() {
		return fDB_Credential_Column;
	}
	
	/**
	 * Set the value of DB_Credential_Column.
	 * @param v  Value to assign to DB_Credential_Column.
	 */
	public void setDB_Credential_Column(String  v) {
		this.fDB_Credential_Column = v;

		/* invalidate prepared statement */
		fDB_Credential_Statement	= null;
	}
	
	
	/**
	 * Get the value of DB_Credential_UserName_Column.
	 * @return value of DB_Credential_UserName_Column.
	 */
	public String getDB_Credential_UserName_Column() {
		return fDB_Credential_UserName_Column;
	}
	
	/**
	 * Set the value of DB_Credential_UserName_Column.
	 * @param v  Value to assign to DB_Credential_UserName_Column.
	 */
	public void setDB_Credential_UserName_Column(String  v) {
		this.fDB_Credential_UserName_Column = v;

		/* invalidate prepared statement */
		fDB_Credential_Statement	= null;
	}
	
	
	/**
	 * Get the value of DB_Credential_UserName_Index.
	 * @return value of DB_Credential_UserName_Index.
	 */
	public int getDB_Credential_UserName_Index() {
		return fDB_Credential_UserName_Index;
	}
	
	/**
	 * Set the value of DB_Credential_UserName_Index.
	 * @param v  Value to assign to DB_Credential_UserName_Index.
	 */
	public void setDB_Credential_UserName_Index(int  v) {
		this.fDB_Credential_UserName_Index = v;

		/* invalidate prepared statement */
		fDB_Credential_Statement	= null;
	}
	
	/**
	 * Get the value of DB_Credential_Credential_Index.
	 * @return value of DB_Credential_Credential_Index.
	 */
	public int getDB_Credential_Credential_Index() {
		return fDB_Credential_Credential_Index;
	}
	
	/**
	 * Set the value of DB_Credential_Credential_Index.
	 * @param v  Value to assign to DB_Credential_Credential_Index.
	 */
	public void setDB_Credential_Credential_Index(int  v) {
		this.fDB_Credential_Credential_Index = v;

		/* invalidate prepared statement */
		fDB_Credential_Statement	= null;
	}
	
	/**
	 * Get the value of DB_Roles_Table.
	 * @return value of DB_Roles_Table.
	 */
	public String getDB_Roles_Table() {
		return fDB_Roles_Table;
	}
	
	/**
	 * Set the value of DB_Roles_Table.
	 * @param v  Value to assign to DB_Roles_Table.
	 */
	public void setDB_Roles_Table(String  v) {
		this.fDB_Roles_Table = v;
		/* invalidate prepared statement */
		fDB_Roles_Statement	= null;
	}
	
	
	/**
	 * Get the value of DB_Roles_Column.
	 * @return value of DB_Roles_Column.
	 */
	public String getDB_Roles_Column() {
		return fDB_Roles_Column;
	}
	
	/**
	 * Set the value of DB_Roles_Column.
	 * @param v  Value to assign to DB_Roles_Column.
	 */
	public void setDB_Roles_Column(String  v) {
		this.fDB_Roles_Column = v;

		/* invalidate prepared statement */
		fDB_Roles_Statement	= null;
	}
	
	
	/**
	 * Get the value of DB_Roles_UserName_Column.
	 * @return value of DB_Roles_UserName_Column.
	 */
	public String getDB_Roles_UserName_Column() {
		return fDB_Roles_UserName_Column;
	}
	
	/**
	 * Set the value of DB_Roles_UserName_Column.
	 * @param v  Value to assign to DB_Roles_UserName_Column.
	 */
	public void setDB_Roles_UserName_Column(String  v) {
		this.fDB_Roles_UserName_Column = v;

		/* invalidate prepared statement */
		fDB_Roles_Statement	= null;
	}
	
	
	/**
	 * Get the value of DB_Roles_UserName_Index.
	 * @return value of DB_Roles_UserName_Index.
	 */
	public int getDB_Roles_UserName_Index() {
		return fDB_Roles_UserName_Index;
	}
	
	/**
	 * Set the value of DB_Roles_UserName_Index.
	 * @param v  Value to assign to DB_Roles_UserName_Index.
	 */
	public void setDB_Roles_UserName_Index(int  v) {
		this.fDB_Roles_UserName_Index = v;

		/* invalidate prepared statement */
		fDB_Roles_Statement	= null;
	}
	
	/**
	 * Get the value of DB_Roles_RoleName_Index.
	 * @return value of DB_Roles_RoleName_Index.
	 */
	public int getDB_Roles_RoleName_Index() {
		return fDB_Roles_RoleName_Index;
	}
	
	/**
	 * Set the value of DB_Roles_RoleName_Index.
	 * @param v  Value to assign to DB_Roles_RoleName_Index.
	 */
	public void setDB_Roles_RoleName_Index(int  v) {
		this.fDB_Roles_RoleName_Index = v;

		/* invalidate prepared statement */
		fDB_Roles_Statement	= null;
	}
	
	/*----------------------------------------------------------------*/
    /****/
    protected synchronized  Connection   DB_Connect()
    throws SQLException
    {
		/* check if we already got connection*/
        if( fConnection != null )
            {return fConnection;}

		/* if not then we need driver */
        if( fDBDriver == null )
			{
			try
				{
				Class lDriver = Class.forName(fDBDriverName);
				fDBDriver	= (Driver)lDriver.newInstance();
				}
			catch(Throwable th)
				{
				throw new SQLException(th.toString());
				}
			}
		/* prepare connect */
		try
			{
			Properties	lP = new Properties();
			if( fDBUserName != null )
				{
				lP.put("user",fDBUserName);
				}
			if( fDBPassword != null )
				{
				lP.put("password",fDBPassword);
				}
			fConnection = fDBDriver.connect(fDBAddress, lP);
			fConnection.setAutoCommit(false);
			}
		catch(Throwable th)
			{
			fConnection	= null;
			throw new SQLException(th.toString());
			}

        return fConnection;
    }

	/****/
	protected synchronized void DB_Close(Connection connection)
    {
		if( fDB_Credential_Statement != null )
			{
			try 
				{
				fDB_Credential_Statement.close();
				}
			catch (Throwable th)
				{/*.TODO log failure*/}
			fDB_Credential_Statement = null;
			}

		if( fDB_Roles_Statement != null )
			{
			try 
				{
				fDB_Roles_Statement.close();
				}
			catch (Throwable th)
				{/*.TODO log failure*/}
			fDB_Roles_Statement = null;
			}

		if( connection != null )
			{
			try 
				{
				connection.close();
				}
			catch (Throwable th)
				{/*.TODO log failure*/}
			}

		if( fConnection != null && fConnection != connection)
			{
			try 
				{
				fConnection.close();
				}
			catch (Throwable th)
				{/*.TODO log failure*/}
			fConnection = null;
			}
	}

	/****/
	protected PreparedStatement  DBPrepare_CredentialQuery(Connection connection, String userName)
	throws SQLException 
    {
		PreparedStatement lPS = fDB_Credential_Statement;
		if( lPS == null )
			{
			String	lsStatement = 
			"   select "+ fDB_Credential_Column
			+ " from   "+ fDB_Credential_Table
			+ " where  "+ fDB_Credential_UserName_Column + " = ?";

			lPS = connection.prepareStatement(lsStatement);
			fDB_Credential_Statement	= lPS;
			}

		lPS.setString(fDB_Credential_UserName_Index, userName);
		return lPS;
	}

	/****/
	protected PreparedStatement  DBPrepare_RolesQuery(Connection connection, String userName, String credential)
	throws SQLException 
    {
		PreparedStatement lPS = fDB_Roles_Statement;
		if( lPS == null )
			{
			String	lsStatement = 
			"   select "+ fDB_Roles_Column
			+ " from   "+ fDB_Roles_Table
			+ " where  "+ fDB_Roles_UserName_Column + " = ?";

			lPS = connection.prepareStatement(lsStatement);
			fDB_Roles_Statement	= lPS;
			}

		lPS.setString(fDB_Roles_UserName_Index, userName);
		return lPS;
	}

	/*----------------------------------------------------------------*/
	/****/
	protected  synchronized Principal  DB_Authenticate(Connection connection,
													   String userName, 
													   String credentials)
	throws SQLException
    {
		boolean	lbAuthenticated = false;
		String	lsDBCredential	= null;
		Vector	lRoles			= null;
	
		/* do query to find CREDENTIALS info in the DB */
		PreparedStatement lCredentialQuery;
		lCredentialQuery = DBPrepare_CredentialQuery(connection,userName);

		ResultSet  lResult = lCredentialQuery.executeQuery();
		while( !lbAuthenticated && lResult.next() )
			{
			lsDBCredential =lResult.getString(fDB_Credential_Credential_Index);
			lsDBCredential.trim();

			/*.TODO check credentials*/
			lbAuthenticated	= true;

			/* get ROLES from DB */
			lRoles	= new Vector();
			PreparedStatement lRolesQuery;
			lRolesQuery = DBPrepare_RolesQuery(connection,
											   userName, lsDBCredential);
			ResultSet  lRolesResult = lRolesQuery.executeQuery();
			while( ! lRolesResult.next() )
				{
				String	lsRole = lRolesResult.getString(fDB_Roles_RoleName_Index);
				lRoles.add(lsRole);
				}/*while*/
			lRolesResult.close();
			}/*while results */

		lResult.close();
		connection.commit();

		if( lbAuthenticated )
			{
			/* Return snapshot of credentials and roles */
			JDBCPrincipal lNew = new JDBCPrincipal(this, 
											   userName, 
                                               null/*.TODO lsDBCredential*/,
											   /*.TODO */
                                               null, null, null);
			return lNew;
			}
		else
			{return null;}

	}

	/*----------------------------------------------------------------*/
	/*----------------------------------------------------------------*/
	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String credentials)
    {
		/*.TODO Check credentials*/

		return null;
    }

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, byte[] credentials)
    {
		/*.TODO Check credentials*/

		return null;
    }

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, Object[] certificates)
    {
		/*.TODO Check credentials*/

		return null;
    }

	/****/
	public  AccessControlPolicy  AuthenticateResource(String resourceID, String digest,
                                              String uniqueToken,
                                              String secondMD5)
    {
		/*.TODO Check credentials*/

		return null;
    }
	/*----------------------------------------------------------------*/
	/*----------------------------------------------------------------*/
	/****/
	public  Principal  Authenticate(String userName, String credentials)
    {
		/* try cache first */
		Principal	lFound = fCache.Find(userName, credentials);
		if( lFound != null )
			{
			return lFound;
			}

		Connection      lConn = null;
        try
            {
            lConn   = DB_Connect();

			Principal lP = DB_Authenticate(lConn,userName, credentials);

			/* add to cache */
			fCache.Add(userName, credentials, lP);

			return lP;
            }
        catch(SQLException ex)
            {
            if( lConn != null )
                {
                DB_Close(lConn);
                }
			/*.TODO log */
            return null;
            }
	}
	
	/****/
	public  Principal  Authenticate(String userName, byte[] credentials)
    {
		if( credentials != null )
			{
			return Authenticate(userName, credentials.toString());
			}
		else
			{
			return Authenticate(userName, (String)null);
			}
	}
	/****/
	public  Principal  Authenticate(String userName, Object[] certificates)
    {
		/*.TODO impl this method */
		if( certificates != null )
			{
			return Authenticate(userName, certificates.toString());
			}
		else
			{
			return Authenticate(userName, (String)null);
			}
	}
	/****/
	public  Principal  Authenticate(String userName, String digest,
									String uniqueToken,
									String secondMD5)
	{
		/*.TODO Check credentials*/

		return null;
	}

	/****/
	public boolean havePermission(Principal principal, AccessControlPolicy policy, String methodName)
    {
		if( principal== null || policy == null  || methodName == null )
			{return false;}

/*.TODO */
        /* Type check*/
//		if( !( principal instanceof DefaultPrincipal) )
//			{return false;}
//		DefaultPrincipal	lP = (DefaultPrincipal)principal;

        /* Need to be from identical realm */
//		if( lP.getSecurityRealm() != this )
//			{return false;}

        /* Type check*/
//		if( !( policy instanceof DefaultAccessControlPolicy) )
//			{return false;}
//        DefaultAccessControlPolicy lAP = (DefaultAccessControlPolicy)policy;

        /* get permissions */
//        Permission lPerm = lAP.getPermission(methodName);
//        if( lPerm == null )
//            {return false;}

        /*.TODO get Privileges */
        

		/*.TODO return lP.hasRole(methodName);*/
        return false;
	}

}


/*.IEnd,JDBCSecurityRealm,====================================*/
