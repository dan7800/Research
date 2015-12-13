/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.user;

import java.math.BigDecimal;

import com.vlee.servlet.main.*;

import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.bean.thickclient.ThickClientProcessingForm;
import com.vlee.ejb.user.*;
import com.vlee.bean.remotecreditservices.ProcessRemoteCreditTransactionForm;
import com.vlee.bean.user.*;
import com.vlee.util.*;

public class DoUserLogin extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		HttpSession session = req.getSession(true);
		User user = null;
		user = (User) UserNut.getHandle(uname);
		// UserHome lUserHome = getUserHome();
		// User user = UserNut.getHandle(lUserHome, uname);
		boolean bLogin;
		if (user != null)
		{
			Log.printDebug("User: " + user);
			bLogin = user.getValidUser(uname, pwd);
		} else
		{
			bLogin = false;
			Log.printDebug(" user is null!! ");
		}
		if (bLogin)
		{ // user is in the login database
			Log.printDebug(" bLogin is true");

			session.setAttribute("loginUser", user);
			session.setAttribute("userName", uname);
			session.setAttribute("nameFirst", user.getNameFirst());
			session.setAttribute("nameLast", user.getNameLast());
			session.setAttribute("userId", user.getUserId());


			//// determine the role of this user
			//// then load permission manager
//			PermissionList permList = UserNut.getPermissionList(user.getUserId());
//			session.setAttribute("user-permission-list", permList);
			try
			{
				Integer roleId = UserRoleNut.getRoleId(user.getUserId());
				PermissionManager permManager = new PermissionManager();
				permManager.setRole(roleId);
				session.setAttribute("permission-manager", permManager);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}

			// Set other session variables like first name,
			// last name and other info
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = user.getUserId();
			atObj.auditType = AuditTrailBean.TYPE_ACCESS;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "Login (from : " + req.getRemoteHost() + " )";
			AuditTrailNut.fnCreate(atObj);
			UserConfigObject ucObj = UserConfigRegistryNut.getUserConfigObject(user.getUserId());
			session.setAttribute("ucObj", ucObj);
			// TKW20080616: This is for thick client.
			String thickClient = req.getParameter("tcAction");
			if(thickClient!=null && thickClient.equals("getItem"))
			{
				String pkid = req.getParameter("pkid");
				req.setAttribute("pkid", pkid);
				ThickClientProcessingForm obj = new ThickClientProcessingForm();
				
				req.setAttribute("responseStatus", obj.getItem(pkid));
				return new ActionRouter("tc-update-respond-page");
			}
			// TKW20080103: This is for Mobile88.
			String creditServices = req.getParameter("creditServices");
			Log.printVerbose("CHECK1");
			if(creditServices!=null)
			{
				Log.printVerbose("CHECK2");
				if(creditServices.equals("Add"))
				{
					Log.printVerbose("CHECK3");
					String merchantCode = req.getParameter("merchantCode");
					String merchantName = req.getParameter("merchantName");
					String paymentId = req.getParameter("paymentId");
					String refNo = req.getParameter("refNo");
					String amount = req.getParameter("amount");
					String txnFee = req.getParameter("txnFee");
					String bankCharge = req.getParameter("bankCharge");
					String currency = req.getParameter("currency");
					String prodDesc = req.getParameter("prodDesc");
					String userName = req.getParameter("userNameAdd");
					String userEmail = req.getParameter("userEmail");
					String userContact = req.getParameter("userContact");
					String remarks = req.getParameter("remarks");
					String lang = req.getParameter("lang");
					String signature = req.getParameter("signature");
					String transId = req.getParameter("transId");
					String branchCode = req.getParameter("branchCode");
					String txnDate = req.getParameter("txnDate");
					String authCode = req.getParameter("authCode");
					String cardPaymentConfigId = req.getParameter("cardPaymentConfigId");
					String control = "";
					Log.printVerbose("userName: "+ userName) ;
					try
					{
						Log.printVerbose("duron6423" + merchantCode + refNo + transId);
						control = SHA1DigiSignControl.getDigitalSignature("duron6423" + merchantCode + refNo + transId);
						Log.printVerbose("control: " + control);
						
						char[] plusArray = new String("+").toCharArray();
						char[] spaceArray = new String(" ").toCharArray();
						char plus = plusArray[0];
						char space = spaceArray[0];
						signature = signature.replace(space, plus);
						Log.printVerbose("signature: " + signature);
					}
					catch(Exception ex)
					{
						
					}
					
					session.setAttribute("IsCreditServices","yes");
					Log.printVerbose("CHECK4");
					ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
					if(prctForm==null)
					{
						Integer userId = user.getUserId();
						prctForm= new ProcessRemoteCreditTransactionForm();
						session.setAttribute("process-remote-credit-services-session", prctForm);
					}	
					Log.printVerbose("CHECK5");
					prctForm.setMerchantCode(merchantCode);
					prctForm.setMerchantName(merchantName);
					Log.printVerbose("CHECK5.1");
					prctForm.setPaymentId(new Integer(paymentId));
					prctForm.setRefNo(refNo);
					Log.printVerbose("CHECK5.2");
					prctForm.setAmount(new BigDecimal(amount));
					Log.printVerbose("CHECK5.3");
					prctForm.setTxnFee(new BigDecimal(txnFee));
					Log.printVerbose("CHECK5.4");
					prctForm.setBankCharge(new BigDecimal(bankCharge));
					prctForm.setCurrency(currency);					
					prctForm.setProdDesc(prodDesc);
					prctForm.setUserName(userName);
					Log.printVerbose("CHECK5.5");
					prctForm.setUserEmail(userEmail);
					prctForm.setUserContact(userContact);
					prctForm.setRemarks(remarks);
					prctForm.setLang(lang);
					prctForm.setSignature(signature);
					Log.printVerbose("CHECK5.6");
					prctForm.setTransId(transId);
					prctForm.setBranchCode(branchCode);
					Log.printVerbose("CHECK5.7");
					prctForm.setTxnDate(TimeFormat.createTimestamp(txnDate));
					Log.printVerbose("CHECK5.8");
					prctForm.setCardPaymentConfigId(cardPaymentConfigId);
					Log.printVerbose("CHECK5.9");
					prctForm.setAuthCode(authCode);
					prctForm.setUserId(user.getUserId());
					prctForm.createTransaction();
					Log.printVerbose("CHECK6");
					req.setAttribute("responseStatus", prctForm.getResponseStatus());
					return new ActionRouter("remote-credit-services-respond-page");
					//return new ActionRouter("remote-credit-services-redirect-page");
				}
				else if(creditServices.equals("Process"))
				{
					session.setAttribute("IsCreditServices","yes");
				}
			}
			
//			com.vlee.servlet.application.DoTopBarLogoConfig.setLogo(servlet, req, res);
			return new ActionRouter("user-login-success-page");
		} else
		{ // store userName request parameter in session
			Log.printDebug(" bLogin is false");
			return new ActionRouter("user-login-failed-page");
		}
	}
	

}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.user;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.bean.user.*;
import com.vlee.util.*;

public class DoUserLogin extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		HttpSession session = req.getSession(true);
		User user = null;
		user = (User) UserNut.getHandle(uname);
		// UserHome lUserHome = getUserHome();
		// User user = UserNut.getHandle(lUserHome, uname);
		boolean bLogin;
		if (user != null)
		{
			Log.printDebug("User: " + user);
			bLogin = user.getValidUser(uname, pwd);
		} else
		{
			bLogin = false;
			Log.printDebug(" user is null!! ");
		}
		if (bLogin)
		{ // user is in the login database
			Log.printDebug(" bLogin is true");

			session.setAttribute("loginUser", user);
			session.setAttribute("userName", uname);
			session.setAttribute("nameFirst", user.getNameFirst());
			session.setAttribute("nameLast", user.getNameLast());
			session.setAttribute("userId", user.getUserId());


			//// determine the role of this user
			//// then load permission manager
//			PermissionList permList = UserNut.getPermissionList(user.getUserId());
//			session.setAttribute("user-permission-list", permList);
			try
			{
				Integer roleId = UserRoleNut.getRoleId(user.getUserId());
				PermissionManager permManager = new PermissionManager();
				permManager.setRole(roleId);
				session.setAttribute("permission-manager", permManager);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}

			// Set other session variables like first name,
			// last name and other info
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = user.getUserId();
			atObj.auditType = AuditTrailBean.TYPE_ACCESS;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "Login (from : " + req.getRemoteHost() + " )";
			AuditTrailNut.fnCreate(atObj);
			UserConfigObject ucObj = UserConfigRegistryNut.getUserConfigObject(user.getUserId());
			session.setAttribute("ucObj", ucObj);

			com.vlee.servlet.application.DoTopBarLogoConfig.setLogo(servlet, req, res);
			return new ActionRouter("user-login-success-page");
		} else
		{ // store userName request parameter in session
			Log.printDebug(" bLogin is false");
			return new ActionRouter("user-login-failed-page");
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.user;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.bean.user.*;
import com.vlee.util.*;

public class DoUserLogin extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		HttpSession session = req.getSession(true);
		User user = null;
		user = (User) UserNut.getHandle(uname);
		// UserHome lUserHome = getUserHome();
		// User user = UserNut.getHandle(lUserHome, uname);
		boolean bLogin;
		if (user != null)
		{
			Log.printDebug("User: " + user);
			bLogin = user.getValidUser(uname, pwd);
		} else
		{
			bLogin = false;
			Log.printDebug(" user is null!! ");
		}
		if (bLogin)
		{ // user is in the login database
			Log.printDebug(" bLogin is true");

			session.setAttribute("loginUser", user);
			session.setAttribute("userName", uname);
			session.setAttribute("nameFirst", user.getNameFirst());
			session.setAttribute("nameLast", user.getNameLast());
			session.setAttribute("userId", user.getUserId());


			//// determine the role of this user
			//// then load permission manager
//			PermissionList permList = UserNut.getPermissionList(user.getUserId());
//			session.setAttribute("user-permission-list", permList);
			try
			{
				Integer roleId = UserRoleNut.getRoleId(user.getUserId());
				PermissionManager permManager = new PermissionManager();
				permManager.setRole(roleId);
				session.setAttribute("permission-manager", permManager);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}

			// Set other session variables like first name,
			// last name and other info
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = user.getUserId();
			atObj.auditType = AuditTrailBean.TYPE_ACCESS;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "Login (from : " + req.getRemoteHost() + " )";
			AuditTrailNut.fnCreate(atObj);
			UserConfigObject ucObj = UserConfigRegistryNut.getUserConfigObject(user.getUserId());
			session.setAttribute("ucObj", ucObj);

			com.vlee.servlet.application.DoTopBarLogoConfig.setLogo(servlet, req, res);
			return new ActionRouter("user-login-success-page");
		} else
		{ // store userName request parameter in session
			Log.printDebug(" bLogin is false");
			return new ActionRouter("user-login-failed-page");
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.user;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.bean.user.*;
import com.vlee.util.*;

public class DoUserLogin extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		HttpSession session = req.getSession(true);
		User user = null;
		user = (User) UserNut.getHandle(uname);
		// UserHome lUserHome = getUserHome();
		// User user = UserNut.getHandle(lUserHome, uname);
		boolean bLogin;
		if (user != null)
		{
			Log.printDebug("User: " + user);
			bLogin = user.getValidUser(uname, pwd);
		} else
		{
			bLogin = false;
			Log.printDebug(" user is null!! ");
		}
		if (bLogin)
		{ // user is in the login database
			Log.printDebug(" bLogin is true");

			session.setAttribute("loginUser", user);
			session.setAttribute("userName", uname);
			session.setAttribute("nameFirst", user.getNameFirst());
			session.setAttribute("nameLast", user.getNameLast());
			session.setAttribute("userId", user.getUserId());


			//// determine the role of this user
			//// then load permission manager
//			PermissionList permList = UserNut.getPermissionList(user.getUserId());
//			session.setAttribute("user-permission-list", permList);
			try
			{
				Integer roleId = UserRoleNut.getRoleId(user.getUserId());
				PermissionManager permManager = new PermissionManager();
				permManager.setRole(roleId);
				session.setAttribute("permission-manager", permManager);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}

			// Set other session variables like first name,
			// last name and other info
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = user.getUserId();
			atObj.auditType = AuditTrailBean.TYPE_ACCESS;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "Login (from : " + req.getRemoteHost() + " )";
			AuditTrailNut.fnCreate(atObj);
			UserConfigObject ucObj = UserConfigRegistryNut.getUserConfigObject(user.getUserId());
			session.setAttribute("ucObj", ucObj);

			com.vlee.servlet.application.DoTopBarLogoConfig.setLogo(servlet, req, res);
			return new ActionRouter("user-login-success-page");
		} else
		{ // store userName request parameter in session
			Log.printDebug(" bLogin is false");
			return new ActionRouter("user-login-failed-page");
		}
	}
}
