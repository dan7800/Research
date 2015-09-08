package com.vlee.servlet.user;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;

import com.vlee.ejb.user.*;
import com.vlee.ejb.application.*;
import com.vlee.bean.user.*;
import com.vlee.bean.application.*;

/* Usage
user-bullboard.do?mode=edit|test
*/

public class DoBullBoard implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String theFwdPage = req.getParameter("theFwdPage");
		if(theFwdPage == null)
		{ theFwdPage = "user-bullboard-display-page"; }
		
		
		String mceInput = req.getParameter("BULLBOARD-CONTENT");
		String mceInputSanitized = mceInput; // do sanitization later
		String modefoo = req.getParameter("mode");
		String saved = "";
		
		if(req.getParameter("save")!=null) saved = req.getParameter("save");
		
		
		req.setAttribute("foo1", "Form name is " + formName + " ; theFwdPage is " + theFwdPage );
		req.setAttribute("foo2", "mode is " + modefoo);
		req.setAttribute("mceInput", mceInputSanitized); // need to have this line or the request.getAttribute("mceInput") call in the JSP will not return any value
		
		if(saved.equals("Save")) { // this usecase needs to precede that of if(modefoo.equals("edit")){}
			req.setAttribute("txn", "save bullboard");
			
			try
			{ fnSetBBContent(servlet, req, res, mceInputSanitized); }
			catch(Exception ex)
			{ ex.printStackTrace(); }
			req.setAttribute("mode", "sdfsdf");
			return new ActionRouter(theFwdPage);
		}
			
		if(modefoo.equals("edit"))
		{
			req.setAttribute("txn", "edit bullboard");
			
/*			try
			{ fnSetBBContent(servlet, req, res, mceInputSanitized); }
			catch(Exception ex)
			{ ex.printStackTrace(); }*/
			
			return new ActionRouter("user-popup-bullboard-edit-page");
		}
		
		
		
		return new ActionRouter(theFwdPage);
	
	}
	
	protected void fnSetBBContent(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String mceInputSanitized)
	throws Exception 
	{
		// Get the userIdfrom the http session
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		
		AppConfigForm appConfigForm1 = new AppConfigForm(userId);
		AppRegObject appRegObj1 = new AppRegObject();
	
		appRegObj1.mNamespace = "BULLBOARD-CONTENT";
		appRegObj1.mUseridEdit = userId;
		appRegObj1.mContent = mceInputSanitized;
		Vector vecRegObj1 = new Vector();
		vecRegObj1.add(appRegObj1);
		appConfigForm1.setConfig("BULLBOARD-CONTENT", vecRegObj1);
		
		AppConfigManager.reloadRegistry(); // need to have this line or the AppConfigManager.getPorperty("WHAT-EVER") calls in the JSP will not return any value
		fnAuditTrail(servlet, req, res, mceInputSanitized);

	}
	
	protected void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String mceInputSanitized)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		if(userId!=null) {
		  AuditTrailObject atObj = new AuditTrailObject();
		  atObj.userId = userId;
		  atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
		  atObj.remarks = "Bullboard: " + "Article has been edited: current length " + mceInputSanitized.length() + " bytes";
		  AuditTrailNut.fnCreate(atObj);
		}
	}
	
	
}