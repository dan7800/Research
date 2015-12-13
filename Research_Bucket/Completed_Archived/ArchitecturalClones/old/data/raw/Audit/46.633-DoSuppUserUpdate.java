/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserUpdate implements Action
{
	String strClassName = "DoSuppUserUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppUser
		fnUpdateSuppUser(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		// return new ActionRouter("supp-setup-updated-user-page");
		return new ActionRouter("supp-setup-edit-user-page");
	}

	protected void fnUpdateSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppUser()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserPkid = req.getParameter("suppUserPkid");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserNewPasswd = req.getParameter("suppUserNewPasswd");
		String suppUserNewPasswd2 = req.getParameter("suppUserNewPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		String suppUserPhoneCountryCode = req.getParameter("suppUserPhoneCountryCode");
		String suppUserPhoneAreaCode = req.getParameter("suppUserPhoneAreaCode");
		String suppUserPhonePhoneNo = req.getParameter("suppUserPhonePhoneNo");
		String suppUserFaxCountryCode = req.getParameter("suppUserFaxCountryCode");
		String suppUserFaxAreaCode = req.getParameter("suppUserFaxAreaCode");
		String suppUserFaxPhoneNo = req.getParameter("suppUserFaxPhoneNo");
		String suppUserAddr1 = req.getParameter("suppUserAddr1");
		String suppUserAddr2 = req.getParameter("suppUserAddr2");
		String suppUserAddr3 = req.getParameter("suppUserAddr3");
		String suppUserZip = req.getParameter("suppUserZip");
		String suppUserState = req.getParameter("suppUserState");
		String suppUserCountryCode = req.getParameter("suppUserCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (suppAccPKID == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppAccPKID = " + suppAccPKID);
		if (suppUserPkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserPkid = " + suppUserPkid);
		if (suppUserName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd2 == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd2 = " + suppUserNewPasswd2);
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppUser lSuppUser = SuppUserNut.getHandle(new Integer(suppUserPkid));
			if (lSuppUser != null)
			{
				lSuppUser.setUserName(suppUserName);
				if ((suppUserNewPasswd.compareTo("") != 0) && (suppUserNewPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserNewPasswd, suppUserNewPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
					lSuppUser.setPassword(suppUserNewPasswd);
					req.setAttribute("newPassword", suppUserNewPasswd);
				}
				lSuppUser.setNameFirst(suppUserNameFirst);
				lSuppUser.setNameLast(suppUserNameLast);
				// and then update the lastModified and userIdUpdate fields
				lSuppUser.setLastUpdate(tsCreate);
				lSuppUser.setUserIdUpdate(usrid);
				// Update the phone and fax numbers
				SuppUserPhone lSuppUserPhone = null;
				SuppUserPhone lSuppUserFax = null;
				lSuppUserPhone = SuppUserPhoneNut.getPhoneByUserId(lSuppUser.getPkid());
				lSuppUserFax = SuppUserPhoneNut.getFaxByUserId(lSuppUser.getPkid());
				if (lSuppUserPhone == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserPhoneHome = SuppUserPhoneNut.getHome();
					lSuppUserPhone = lSuppUserPhoneHome.create(lSuppUser.getPkid(), "phone", suppUserPhoneCountryCode,
							suppUserPhoneAreaCode, suppUserPhonePhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserPhone.setCountryCode(suppUserPhoneCountryCode);
					lSuppUserPhone.setAreaCode(suppUserPhoneAreaCode);
					lSuppUserPhone.setPhoneNo(suppUserPhonePhoneNo);
					lSuppUserPhone.setLastUpdate(tsCreate);
					lSuppUserPhone.setUserIdUpdate(usrid);
				}
				if (lSuppUserFax == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserFaxHome = SuppUserPhoneNut.getHome();
					lSuppUserFax = lSuppUserFaxHome.create(lSuppUser.getPkid(), "fax", suppUserFaxCountryCode,
							suppUserFaxAreaCode, suppUserFaxPhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserFax.setCountryCode(suppUserFaxCountryCode);
					lSuppUserFax.setAreaCode(suppUserFaxAreaCode);
					lSuppUserFax.setPhoneNo(suppUserFaxPhoneNo);
					lSuppUserFax.setLastUpdate(tsCreate);
					lSuppUserFax.setUserIdUpdate(usrid);
				}
				// Update the user details
				SuppUserDetails lSuppUserDetails = null;
				lSuppUserDetails = SuppUserDetailsNut.getObjectByUserId(lSuppUser.getPkid());
				if (lSuppUserDetails != null)
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
					lSuppUserDetails.setLastUpdate(tsCreate);
					lSuppUserDetails.setUserIdUpdate(usrid);
				} else
				{
					// new entry, create new
					SuppUserDetailsHome lSuppUserDetailH = SuppUserDetailsNut.getHome();
					// create the bare details first,
					// then start setting one by one
					lSuppUserDetails = lSuppUserDetailH.create(lSuppUser.getPkid(), tsCreate, usrid);
					// TO_DO: Add to this set list if we want to add
					// the rest of the fields
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
				}
				// populate the "editSuppUser" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppUser", lSuppUser);
				req.setAttribute("editSuppUserPhone", lSuppUserPhone);
				req.setAttribute("editSuppUserFax", lSuppUserFax);
				req.setAttribute("editSuppUserDetails", lSuppUserDetails);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Update SuppUser for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserUpdate
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserUpdate implements Action
{
	String strClassName = "DoSuppUserUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppUser
		fnUpdateSuppUser(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		// return new ActionRouter("supp-setup-updated-user-page");
		return new ActionRouter("supp-setup-edit-user-page");
	}

	protected void fnUpdateSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppUser()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserPkid = req.getParameter("suppUserPkid");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserNewPasswd = req.getParameter("suppUserNewPasswd");
		String suppUserNewPasswd2 = req.getParameter("suppUserNewPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		String suppUserPhoneCountryCode = req.getParameter("suppUserPhoneCountryCode");
		String suppUserPhoneAreaCode = req.getParameter("suppUserPhoneAreaCode");
		String suppUserPhonePhoneNo = req.getParameter("suppUserPhonePhoneNo");
		String suppUserFaxCountryCode = req.getParameter("suppUserFaxCountryCode");
		String suppUserFaxAreaCode = req.getParameter("suppUserFaxAreaCode");
		String suppUserFaxPhoneNo = req.getParameter("suppUserFaxPhoneNo");
		String suppUserAddr1 = req.getParameter("suppUserAddr1");
		String suppUserAddr2 = req.getParameter("suppUserAddr2");
		String suppUserAddr3 = req.getParameter("suppUserAddr3");
		String suppUserZip = req.getParameter("suppUserZip");
		String suppUserState = req.getParameter("suppUserState");
		String suppUserCountryCode = req.getParameter("suppUserCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (suppAccPKID == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppAccPKID = " + suppAccPKID);
		if (suppUserPkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserPkid = " + suppUserPkid);
		if (suppUserName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd2 == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd2 = " + suppUserNewPasswd2);
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppUser lSuppUser = SuppUserNut.getHandle(new Integer(suppUserPkid));
			if (lSuppUser != null)
			{
				lSuppUser.setUserName(suppUserName);
				if ((suppUserNewPasswd.compareTo("") != 0) && (suppUserNewPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserNewPasswd, suppUserNewPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
					lSuppUser.setPassword(suppUserNewPasswd);
					req.setAttribute("newPassword", suppUserNewPasswd);
				}
				lSuppUser.setNameFirst(suppUserNameFirst);
				lSuppUser.setNameLast(suppUserNameLast);
				// and then update the lastModified and userIdUpdate fields
				lSuppUser.setLastUpdate(tsCreate);
				lSuppUser.setUserIdUpdate(usrid);
				// Update the phone and fax numbers
				SuppUserPhone lSuppUserPhone = null;
				SuppUserPhone lSuppUserFax = null;
				lSuppUserPhone = SuppUserPhoneNut.getPhoneByUserId(lSuppUser.getPkid());
				lSuppUserFax = SuppUserPhoneNut.getFaxByUserId(lSuppUser.getPkid());
				if (lSuppUserPhone == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserPhoneHome = SuppUserPhoneNut.getHome();
					lSuppUserPhone = lSuppUserPhoneHome.create(lSuppUser.getPkid(), "phone", suppUserPhoneCountryCode,
							suppUserPhoneAreaCode, suppUserPhonePhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserPhone.setCountryCode(suppUserPhoneCountryCode);
					lSuppUserPhone.setAreaCode(suppUserPhoneAreaCode);
					lSuppUserPhone.setPhoneNo(suppUserPhonePhoneNo);
					lSuppUserPhone.setLastUpdate(tsCreate);
					lSuppUserPhone.setUserIdUpdate(usrid);
				}
				if (lSuppUserFax == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserFaxHome = SuppUserPhoneNut.getHome();
					lSuppUserFax = lSuppUserFaxHome.create(lSuppUser.getPkid(), "fax", suppUserFaxCountryCode,
							suppUserFaxAreaCode, suppUserFaxPhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserFax.setCountryCode(suppUserFaxCountryCode);
					lSuppUserFax.setAreaCode(suppUserFaxAreaCode);
					lSuppUserFax.setPhoneNo(suppUserFaxPhoneNo);
					lSuppUserFax.setLastUpdate(tsCreate);
					lSuppUserFax.setUserIdUpdate(usrid);
				}
				// Update the user details
				SuppUserDetails lSuppUserDetails = null;
				lSuppUserDetails = SuppUserDetailsNut.getObjectByUserId(lSuppUser.getPkid());
				if (lSuppUserDetails != null)
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
					lSuppUserDetails.setLastUpdate(tsCreate);
					lSuppUserDetails.setUserIdUpdate(usrid);
				} else
				{
					// new entry, create new
					SuppUserDetailsHome lSuppUserDetailH = SuppUserDetailsNut.getHome();
					// create the bare details first,
					// then start setting one by one
					lSuppUserDetails = lSuppUserDetailH.create(lSuppUser.getPkid(), tsCreate, usrid);
					// TO_DO: Add to this set list if we want to add
					// the rest of the fields
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
				}
				// populate the "editSuppUser" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppUser", lSuppUser);
				req.setAttribute("editSuppUserPhone", lSuppUserPhone);
				req.setAttribute("editSuppUserFax", lSuppUserFax);
				req.setAttribute("editSuppUserDetails", lSuppUserDetails);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Update SuppUser for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserUpdate
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserUpdate implements Action
{
	String strClassName = "DoSuppUserUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppUser
		fnUpdateSuppUser(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		// return new ActionRouter("supp-setup-updated-user-page");
		return new ActionRouter("supp-setup-edit-user-page");
	}

	protected void fnUpdateSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppUser()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserPkid = req.getParameter("suppUserPkid");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserNewPasswd = req.getParameter("suppUserNewPasswd");
		String suppUserNewPasswd2 = req.getParameter("suppUserNewPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		String suppUserPhoneCountryCode = req.getParameter("suppUserPhoneCountryCode");
		String suppUserPhoneAreaCode = req.getParameter("suppUserPhoneAreaCode");
		String suppUserPhonePhoneNo = req.getParameter("suppUserPhonePhoneNo");
		String suppUserFaxCountryCode = req.getParameter("suppUserFaxCountryCode");
		String suppUserFaxAreaCode = req.getParameter("suppUserFaxAreaCode");
		String suppUserFaxPhoneNo = req.getParameter("suppUserFaxPhoneNo");
		String suppUserAddr1 = req.getParameter("suppUserAddr1");
		String suppUserAddr2 = req.getParameter("suppUserAddr2");
		String suppUserAddr3 = req.getParameter("suppUserAddr3");
		String suppUserZip = req.getParameter("suppUserZip");
		String suppUserState = req.getParameter("suppUserState");
		String suppUserCountryCode = req.getParameter("suppUserCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (suppAccPKID == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppAccPKID = " + suppAccPKID);
		if (suppUserPkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserPkid = " + suppUserPkid);
		if (suppUserName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd2 == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd2 = " + suppUserNewPasswd2);
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppUser lSuppUser = SuppUserNut.getHandle(new Integer(suppUserPkid));
			if (lSuppUser != null)
			{
				lSuppUser.setUserName(suppUserName);
				if ((suppUserNewPasswd.compareTo("") != 0) && (suppUserNewPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserNewPasswd, suppUserNewPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
					lSuppUser.setPassword(suppUserNewPasswd);
					req.setAttribute("newPassword", suppUserNewPasswd);
				}
				lSuppUser.setNameFirst(suppUserNameFirst);
				lSuppUser.setNameLast(suppUserNameLast);
				// and then update the lastModified and userIdUpdate fields
				lSuppUser.setLastUpdate(tsCreate);
				lSuppUser.setUserIdUpdate(usrid);
				// Update the phone and fax numbers
				SuppUserPhone lSuppUserPhone = null;
				SuppUserPhone lSuppUserFax = null;
				lSuppUserPhone = SuppUserPhoneNut.getPhoneByUserId(lSuppUser.getPkid());
				lSuppUserFax = SuppUserPhoneNut.getFaxByUserId(lSuppUser.getPkid());
				if (lSuppUserPhone == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserPhoneHome = SuppUserPhoneNut.getHome();
					lSuppUserPhone = lSuppUserPhoneHome.create(lSuppUser.getPkid(), "phone", suppUserPhoneCountryCode,
							suppUserPhoneAreaCode, suppUserPhonePhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserPhone.setCountryCode(suppUserPhoneCountryCode);
					lSuppUserPhone.setAreaCode(suppUserPhoneAreaCode);
					lSuppUserPhone.setPhoneNo(suppUserPhonePhoneNo);
					lSuppUserPhone.setLastUpdate(tsCreate);
					lSuppUserPhone.setUserIdUpdate(usrid);
				}
				if (lSuppUserFax == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserFaxHome = SuppUserPhoneNut.getHome();
					lSuppUserFax = lSuppUserFaxHome.create(lSuppUser.getPkid(), "fax", suppUserFaxCountryCode,
							suppUserFaxAreaCode, suppUserFaxPhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserFax.setCountryCode(suppUserFaxCountryCode);
					lSuppUserFax.setAreaCode(suppUserFaxAreaCode);
					lSuppUserFax.setPhoneNo(suppUserFaxPhoneNo);
					lSuppUserFax.setLastUpdate(tsCreate);
					lSuppUserFax.setUserIdUpdate(usrid);
				}
				// Update the user details
				SuppUserDetails lSuppUserDetails = null;
				lSuppUserDetails = SuppUserDetailsNut.getObjectByUserId(lSuppUser.getPkid());
				if (lSuppUserDetails != null)
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
					lSuppUserDetails.setLastUpdate(tsCreate);
					lSuppUserDetails.setUserIdUpdate(usrid);
				} else
				{
					// new entry, create new
					SuppUserDetailsHome lSuppUserDetailH = SuppUserDetailsNut.getHome();
					// create the bare details first,
					// then start setting one by one
					lSuppUserDetails = lSuppUserDetailH.create(lSuppUser.getPkid(), tsCreate, usrid);
					// TO_DO: Add to this set list if we want to add
					// the rest of the fields
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
				}
				// populate the "editSuppUser" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppUser", lSuppUser);
				req.setAttribute("editSuppUserPhone", lSuppUserPhone);
				req.setAttribute("editSuppUserFax", lSuppUserFax);
				req.setAttribute("editSuppUserDetails", lSuppUserDetails);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Update SuppUser for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserUpdate
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.*;

public class DoSuppUserUpdate implements Action
{
	String strClassName = "DoSuppUserUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update suppUser
		fnUpdateSuppUser(servlet, req, res);
		// propagate suppAccPKID
		String suppAccPKID = req.getParameter("suppAccPKID");
		req.setAttribute("suppAccPKID", suppAccPKID);
		// return new ActionRouter("supp-setup-updated-user-page");
		return new ActionRouter("supp-setup-edit-user-page");
	}

	protected void fnUpdateSuppUser(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateSuppUser()";
		// Get the request paramaters
		String suppAccPKID = req.getParameter("suppAccPKID");
		String suppUserPkid = req.getParameter("suppUserPkid");
		String suppUserName = req.getParameter("suppUserName");
		String suppUserNewPasswd = req.getParameter("suppUserNewPasswd");
		String suppUserNewPasswd2 = req.getParameter("suppUserNewPasswd2");
		String suppUserNameFirst = req.getParameter("suppUserNameFirst");
		String suppUserNameLast = req.getParameter("suppUserNameLast");
		String suppUserPhoneCountryCode = req.getParameter("suppUserPhoneCountryCode");
		String suppUserPhoneAreaCode = req.getParameter("suppUserPhoneAreaCode");
		String suppUserPhonePhoneNo = req.getParameter("suppUserPhonePhoneNo");
		String suppUserFaxCountryCode = req.getParameter("suppUserFaxCountryCode");
		String suppUserFaxAreaCode = req.getParameter("suppUserFaxAreaCode");
		String suppUserFaxPhoneNo = req.getParameter("suppUserFaxPhoneNo");
		String suppUserAddr1 = req.getParameter("suppUserAddr1");
		String suppUserAddr2 = req.getParameter("suppUserAddr2");
		String suppUserAddr3 = req.getParameter("suppUserAddr3");
		String suppUserZip = req.getParameter("suppUserZip");
		String suppUserState = req.getParameter("suppUserState");
		String suppUserCountryCode = req.getParameter("suppUserCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (suppAccPKID == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppAccPKID = " + suppAccPKID);
		if (suppUserPkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserPkid = " + suppUserPkid);
		if (suppUserName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd = " + suppUserNewPasswd);
		if (suppUserNewPasswd2 == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - suppUserNewPasswd2 = " + suppUserNewPasswd2);
		try
		{
			// Get the suppAccId first
			Integer suppAccId = new Integer(suppAccPKID);
			SuppAccount suppAcc = SuppAccountNut.getHandle(suppAccId);
			// Integer suppAccId = suppAcc.getPkid();
			SuppUser lSuppUser = SuppUserNut.getHandle(new Integer(suppUserPkid));
			if (lSuppUser != null)
			{
				lSuppUser.setUserName(suppUserName);
				if ((suppUserNewPasswd.compareTo("") != 0) && (suppUserNewPasswd2.compareTo("") != 0))
				{
					// Check password ... TO_DO: more needs to be done here
					if (fnCheckPassword(suppUserNewPasswd, suppUserNewPasswd2) == 0)
					{
						Log.printDebug(strClassName + ":" + funcName + " - failed password check.");
						throw new Exception("failed password check");
					}
					lSuppUser.setPassword(suppUserNewPasswd);
					req.setAttribute("newPassword", suppUserNewPasswd);
				}
				lSuppUser.setNameFirst(suppUserNameFirst);
				lSuppUser.setNameLast(suppUserNameLast);
				// and then update the lastModified and userIdUpdate fields
				lSuppUser.setLastUpdate(tsCreate);
				lSuppUser.setUserIdUpdate(usrid);
				// Update the phone and fax numbers
				SuppUserPhone lSuppUserPhone = null;
				SuppUserPhone lSuppUserFax = null;
				lSuppUserPhone = SuppUserPhoneNut.getPhoneByUserId(lSuppUser.getPkid());
				lSuppUserFax = SuppUserPhoneNut.getFaxByUserId(lSuppUser.getPkid());
				if (lSuppUserPhone == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserPhoneHome = SuppUserPhoneNut.getHome();
					lSuppUserPhone = lSuppUserPhoneHome.create(lSuppUser.getPkid(), "phone", suppUserPhoneCountryCode,
							suppUserPhoneAreaCode, suppUserPhonePhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserPhone.setCountryCode(suppUserPhoneCountryCode);
					lSuppUserPhone.setAreaCode(suppUserPhoneAreaCode);
					lSuppUserPhone.setPhoneNo(suppUserPhonePhoneNo);
					lSuppUserPhone.setLastUpdate(tsCreate);
					lSuppUserPhone.setUserIdUpdate(usrid);
				}
				if (lSuppUserFax == null)
				{
					// add the entry
					SuppUserPhoneHome lSuppUserFaxHome = SuppUserPhoneNut.getHome();
					lSuppUserFax = lSuppUserFaxHome.create(lSuppUser.getPkid(), "fax", suppUserFaxCountryCode,
							suppUserFaxAreaCode, suppUserFaxPhoneNo, tsCreate, usrid);
				} else
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserFax.setCountryCode(suppUserFaxCountryCode);
					lSuppUserFax.setAreaCode(suppUserFaxAreaCode);
					lSuppUserFax.setPhoneNo(suppUserFaxPhoneNo);
					lSuppUserFax.setLastUpdate(tsCreate);
					lSuppUserFax.setUserIdUpdate(usrid);
				}
				// Update the user details
				SuppUserDetails lSuppUserDetails = null;
				lSuppUserDetails = SuppUserDetailsNut.getObjectByUserId(lSuppUser.getPkid());
				if (lSuppUserDetails != null)
				{
					// edit the entry
					// TO_DO: Here we simply edit all the field,
					// even if the user hasn't changed anything
					// need to add a check here to only edit
					// fields that have been changed / modified
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
					lSuppUserDetails.setLastUpdate(tsCreate);
					lSuppUserDetails.setUserIdUpdate(usrid);
				} else
				{
					// new entry, create new
					SuppUserDetailsHome lSuppUserDetailH = SuppUserDetailsNut.getHome();
					// create the bare details first,
					// then start setting one by one
					lSuppUserDetails = lSuppUserDetailH.create(lSuppUser.getPkid(), tsCreate, usrid);
					// TO_DO: Add to this set list if we want to add
					// the rest of the fields
					lSuppUserDetails.setAddr1(suppUserAddr1);
					lSuppUserDetails.setAddr2(suppUserAddr2);
					lSuppUserDetails.setAddr3(suppUserAddr3);
					lSuppUserDetails.setZip(suppUserZip);
					lSuppUserDetails.setState(suppUserState);
					lSuppUserDetails.setCountryCode(suppUserCountryCode);
				}
				// populate the "editSuppUser" attribute so re-display the
				// edited fields
				// req.setAttribute("suppAccPKID", suppAccPKID);
				req.setAttribute("editSuppUser", lSuppUser);
				req.setAttribute("editSuppUserPhone", lSuppUserPhone);
				req.setAttribute("editSuppUserFax", lSuppUserFax);
				req.setAttribute("editSuppUserDetails", lSuppUserDetails);
			} // end if
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Update SuppUser for SuppAccCode = " + suppAccPKID + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateSuppUser

	protected int fnCheckPassword(String passwd1, String passwd2)
	{
		// Assume password has been validated by javascript first
		// Actually even the routine below should be validated by javascript
		// But this routine is to perform whatever password check that
		// javascript cannot possibly do.
		if (passwd1.compareTo(passwd2) == 0)
			return 1;
		else
			return 0;
	}
} // end class DoSuppUserUpdate
