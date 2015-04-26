package com.vlee.servlet.user;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoUserChangePassword extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String uname = req.getParameter("userName");
		String oldPwd = req.getParameter("oldPassword");
		String newPwd = req.getParameter("newPassword");
		String confNewPwd = req.getParameter("confNewPassword");
		// String errorMsg = "";
		User user = null;
		user = (User) getUserHandle(uname);
		boolean bSuccess = false; //[[JOB-JOE-07052501
		HttpSession session = req.getSession(); //[[JOB-JOE-07052501
		try
		{
			if (user == null)
			{
				String errMsg = "Username <b> " + uname + " </b> does not exist!!";
				Log.printDebug(errMsg);
				throw new Exception(errMsg);
			}
			// First check if the old password was entered correctly
			boolean bLogin = user.getValidUser(uname, oldPwd);
			if (!bLogin)
			{
				String errMsg = "Wrong old Password, please re-enter (Ensure your caps lock is not accidentally turned on)";
				Log.printDebug(errMsg);
				throw new Exception(errMsg);
			}
			// reset the password here....
			user.setPassword(newPwd);
			// return new ActionRouter("user-change-password-page");
			String rtnMsg = "Successfully changed user password. This will take effect the next time you login";
			req.setAttribute("rtnMsg", rtnMsg);
			req.setAttribute("success", new Boolean(true));
			bSuccess = true;
		} catch (Exception ex)
		{
			String errMsg = "Failed to change user password: " + ex.getMessage();
			req.setAttribute("rtnMsg", errMsg);
			req.setAttribute("success", new Boolean(false));
			// return new ActionRouter("error-message-page");
		}
		
		//[[JOB-JOE-07052501
		try
			{
				if (bSuccess)
				{
					user.setPassword(newPwd);
					Log.printDebug("DoUserChangePassword : Password changed");
										
					{
						System.out.println("Recording change of password to audit trail, user : "+user.getName());
						
						AuditTrailObject atObj = new AuditTrailObject();
						atObj.userId = (Integer) session.getAttribute("userId");
						atObj.auditType = AuditTrailBean.TYPE_CONFIG;
						atObj.time = TimeFormat.getTimestamp();
						atObj.remarks = "change-password: User :" + user.getName();
						AuditTrailNut.fnCreate(atObj);
						
						System.out.println("Recorded change of password into audit trail");
					}
					
				} 
			} catch (Exception ex)
			{
				Log.printDebug("DoUserChangePassword : " + ex.getMessage());
			}
		//JOB-JOE-07052501]]
		
		return new ActionRouter("user-change-password-page");
	}
}
