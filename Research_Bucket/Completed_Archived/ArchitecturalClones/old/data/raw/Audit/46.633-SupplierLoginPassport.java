/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.portal.supplier;

import java.io.*;
import java.math.*;
import java.util.*;

import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;
import com.vlee.bean.user.*;
import com.vlee.util.*;

import org.apache.commons.lang.StringUtils;

public class SupplierLoginPassport extends java.lang.Object implements Serializable
{
	// MEMBER VARIABLES
	protected String guid;
	protected SuppAccountObject account;
	protected UserObject user;
	protected PermissionManager permissionManager;
	protected UserConfigObject userConfigObj;


	public SupplierLoginPassport(String username, String password, String remoteHost) 
			throws Exception
	{
		/// primary check for username and password
		if(username.trim().length()<5){ throw new Exception("Username must be at least 5 characters!");}
		if(password.trim().length()<5){ throw new Exception("Password must be at least 5 characters!");}

		/// check if there's such valid user
		this.user = UserNut.getObject(username);
		if(this.user==null) { throw new Exception("Invalid User Object!"); }

		// pull out the supplier account
		QueryObject querySupplier = new QueryObject(new String[]{
				SuppAccountBean.USERNAME +" = '"+username+"' "
									});
		Vector vecSupplier = new Vector(SuppAccountNut.getObjects(querySupplier));
		if(vecSupplier.size()==0)
		{ throw new Exception("No supplier account is tagged to this username!"); }
		else if(vecSupplier.size()>1)
		{ throw new Exception("There are more than one supplier account with this username, the username is invalid!");}
		this.account = (SuppAccountObject) vecSupplier.get(0);

		/// veryfying the username and password
		if(!this.account.validLogin(username,password))	
		{ throw new Exception("Invalid Username and Password!");} 

		/// load all permissions
		Integer roleId = UserRoleNut.getRoleId(user.userId);
      this.permissionManager = new PermissionManager();
		this.permissionManager.setRole(roleId);

		/// load other configurations


		/// record in audit trails
		AuditTrailObject atObj = new AuditTrailObject();
		atObj.userId = user.userId;
		atObj.auditType = AuditTrailBean.TYPE_ACCESS;
		atObj.time = TimeFormat.getTimestamp();
		atObj.remarks = "supplier-login: Supplier Portal Login (from : " + remoteHost + " )";
		AuditTrailNut.fnCreate(atObj);
		this.userConfigObj = UserConfigRegistryNut.getUserConfigObject(user.userId);
	}

	public SuppAccountObject getAccount()
	{
		return this.account;
	}

	public UserObject getUser()
	{
		return this.user;
	}

	public PermissionManager getPermissionManager()
	{
		return this.permissionManager;
	}
}












