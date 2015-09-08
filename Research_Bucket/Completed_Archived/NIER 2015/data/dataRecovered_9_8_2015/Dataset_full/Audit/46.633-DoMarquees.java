package com.vlee.servlet.intranet;

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


public class DoMarquees implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String theFwdPage = req.getParameter("theFwdPage");
		if(theFwdPage == null)
		{ theFwdPage = "user-bullboard-display-page"; }
		String mode = req.getParameter("mode");
		if(mode==null) mode = "";
		String m = req.getParameter("m"); // 1,2, 3
		if(m==null) m = "";
		
		marquee1Text = req.getParameter("marquee-1-text");
		marquee1Width = req.getParameter("marquee-1-width");
		marquee1Height = req.getParameter("marquee-1-height");
		marquee1Fgcolor = req.getParameter("marquee-1-fgcolor");
		marquee1Bgcolor = req.getParameter("marquee-1-bgcolor");
		
		marquee2Text = req.getParameter("marquee-2-text");
		marquee2Width = req.getParameter("marquee-2-width");
		marquee2Height = req.getParameter("marquee-2-height");
		marquee2Fgcolor = req.getParameter("marquee-2-fgcolor");
		marquee2Bgcolor = req.getParameter("marquee-2-bgcolor");
		
		marquee3Text = req.getParameter("marquee-3-text");
		marquee3Width = req.getParameter("marquee-3-width");
		marquee3Height = req.getParameter("marquee-3-height");
		marquee3Fgcolor = req.getParameter("marquee-3-fgcolor");
		marquee3Bgcolor = req.getParameter("marquee-3-bgcolor");
		
		if(mode.equalsIgnoreCase("demo")) {
		  return new ActionRouter("intranet-marquees-stub-page");
		}
		
		if(m.equals("1")) {
			HttpSession session = req.getSession();
			PermissionManager permMgr = (PermissionManager) session.getAttribute("permission-manager"); // this line is idencitalto that in the src/web/intranet/inc_leftmenu.jsp
			if(permMgr.hasPermission("intra-marquees-create"))
			{
				try
				{ fnSetMarquee1Content(servlet, req, res);
				} catch(Exception ex) 
				{ ex.printStackTrace(); }
				return new ActionRouter("intranet-marquees-edit-page");
			}
			return new ActionRouter("intranet-marquees-noedit-page");
		}
		
		if(m.equals("2")) {
			HttpSession session = req.getSession();
			PermissionManager permMgr = (PermissionManager) session.getAttribute("permission-manager"); // this line is idencitalto that in the src/web/intranet/inc_leftmenu.jsp
			if(permMgr.hasPermission("intra-marquees-create"))
			{
				try
				{ fnSetMarquee2Content(servlet, req, res);
				} catch(Exception ex) 
				{ ex.printStackTrace(); }
				return new ActionRouter("intranet-marquees-edit-page");
			}
			return new ActionRouter("intranet-marquees-noedit-page");
		}
		
		if(m.equals("3")) {
			HttpSession session = req.getSession();
			PermissionManager permMgr = (PermissionManager) session.getAttribute("permission-manager"); // this line is idencitalto that in the src/web/intranet/inc_leftmenu.jsp
			if(permMgr.hasPermission("intra-marquees-create"))
			{
				try
				{ fnSetMarquee3Content(servlet, req, res);
				} catch(Exception ex) 
				{ ex.printStackTrace(); }
				return new ActionRouter("intranet-marquees-edit-page");
			}
			return new ActionRouter("intranet-marquees-noedit-page");
		}
		
		if(mode.equals("edit")) {
			HttpSession session = req.getSession();
			PermissionManager permMgr = (PermissionManager) session.getAttribute("permission-manager"); // this line is idencitalto that in the src/web/intranet/inc_leftmenu.jsp
			if(permMgr.hasPermission("intra-marquees-create"))
			{
				return new ActionRouter("intranet-marquees-edit-page");
			}
			return new ActionRouter("intranet-marquees-noedit-page");
		}
		
		
		return new ActionRouter(theFwdPage);
	
	}
	
	private String
	marquee1Text, marquee1Width, marquee1Height, marquee1Fgcolor, marquee1Bgcolor,
	marquee2Text, marquee2Width, marquee2Height, marquee2Fgcolor, marquee2Bgcolor,
	marquee3Text, marquee3Width, marquee3Height, marquee3Fgcolor, marquee3Bgcolor;
	
	protected void fnSetMarquee1Content(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	throws Exception 
	{
		// Get the userIdfrom the http session
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		
		AppConfigForm appConfigForm1 = new AppConfigForm(userId);
		AppRegObject appRegObj1 = new AppRegObject();
		appRegObj1.mNamespace = "marquee-1-text";
		appRegObj1.mUseridEdit = userId;
		appRegObj1.mContent = marquee1Text;
		Vector vecRegObj1 = new Vector();
		vecRegObj1.add(appRegObj1);
		appConfigForm1.setConfig("marquee-1-text", vecRegObj1);
		
		AppConfigForm appConfigForm2 = new AppConfigForm(userId);
		AppRegObject appRegObj2 = new AppRegObject();
		appRegObj2.mNamespace = "marquee-1-width";
		appRegObj2.mUseridEdit = userId;
		appRegObj2.mContent = marquee1Width;
		Vector vecRegObj2 = new Vector();
		vecRegObj2.add(appRegObj2);
		appConfigForm2.setConfig("marquee-1-width", vecRegObj2);
		
		AppConfigForm appConfigForm3 = new AppConfigForm(userId);
		AppRegObject appRegObj3 = new AppRegObject();
		appRegObj3.mNamespace = "marquee-1-height";
		appRegObj3.mUseridEdit = userId;
		appRegObj3.mContent = marquee1Height;
		Vector vecRegObj3 = new Vector();
		vecRegObj3.add(appRegObj3);
		appConfigForm3.setConfig("marquee-1-height", vecRegObj3);
		
		AppConfigForm appConfigForm4 = new AppConfigForm(userId);
		AppRegObject appRegObj4 = new AppRegObject();
		appRegObj4.mNamespace = "marquee-1-fgcolor";
		appRegObj4.mUseridEdit = userId;
		appRegObj4.mContent = marquee1Fgcolor;
		Vector vecRegObj4 = new Vector();
		vecRegObj4.add(appRegObj4);
		appConfigForm4.setConfig("marquee-1-fgcolor", vecRegObj4);
		
		AppConfigForm appConfigForm5 = new AppConfigForm(userId);
		AppRegObject appRegObj5 = new AppRegObject();
		appRegObj5.mNamespace = "marquee-1-bgcolor";
		appRegObj5.mUseridEdit = userId;
		appRegObj5.mContent = marquee1Bgcolor;
		Vector vecRegObj5 = new Vector();
		vecRegObj5.add(appRegObj5);
		appConfigForm5.setConfig("marquee-1-bgcolor", vecRegObj5);
		
		
		AppConfigManager.reloadRegistry(); // need to have this line or the AppConfigManager.getPorperty("WHAT-EVER") calls in the JSP will not return any value
		
		fnAuditTrailMarquee1(servlet, req, res);

	}
	
	protected void fnAuditTrailMarquee1(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		if(userId!=null) {
		  AuditTrailObject atObj = new AuditTrailObject();
		  atObj.userId = userId;
		  atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
		  //atObj.remarks = "Bullboard: " + "Article has been edited: current length " + mceInputSanitized.length() + " bytes";
		  atObj.remarks = "Marquee1: text=" + marquee1Text.substring(0, 9);
		  AuditTrailNut.fnCreate(atObj);
		}
	}
	
	protected void fnSetMarquee2Content(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	throws Exception 
	{
		// Get the userIdfrom the http session
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		
		AppConfigForm appConfigForm1 = new AppConfigForm(userId);
		AppRegObject appRegObj1 = new AppRegObject();
		appRegObj1.mNamespace = "marquee-2-text";
		appRegObj1.mUseridEdit = userId;
		appRegObj1.mContent = marquee2Text;
		Vector vecRegObj1 = new Vector();
		vecRegObj1.add(appRegObj1);
		appConfigForm1.setConfig("marquee-2-text", vecRegObj1);
		
		AppConfigForm appConfigForm2 = new AppConfigForm(userId);
		AppRegObject appRegObj2 = new AppRegObject();
		appRegObj2.mNamespace = "marquee-2-width";
		appRegObj2.mUseridEdit = userId;
		appRegObj2.mContent = marquee2Width;
		Vector vecRegObj2 = new Vector();
		vecRegObj2.add(appRegObj2);
		appConfigForm2.setConfig("marquee-2-width", vecRegObj2);
		
		AppConfigForm appConfigForm3 = new AppConfigForm(userId);
		AppRegObject appRegObj3 = new AppRegObject();
		appRegObj3.mNamespace = "marquee-2-height";
		appRegObj3.mUseridEdit = userId;
		appRegObj3.mContent = marquee2Height;
		Vector vecRegObj3 = new Vector();
		vecRegObj3.add(appRegObj3);
		appConfigForm3.setConfig("marquee-2-height", vecRegObj3);
		
		AppConfigForm appConfigForm4 = new AppConfigForm(userId);
		AppRegObject appRegObj4 = new AppRegObject();
		appRegObj4.mNamespace = "marquee-2-fgcolor";
		appRegObj4.mUseridEdit = userId;
		appRegObj4.mContent = marquee2Fgcolor;
		Vector vecRegObj4 = new Vector();
		vecRegObj4.add(appRegObj4);
		appConfigForm4.setConfig("marquee-2-fgcolor", vecRegObj4);
		
		AppConfigForm appConfigForm5 = new AppConfigForm(userId);
		AppRegObject appRegObj5 = new AppRegObject();
		appRegObj5.mNamespace = "marquee-2-bgcolor";
		appRegObj5.mUseridEdit = userId;
		appRegObj5.mContent = marquee2Bgcolor;
		Vector vecRegObj5 = new Vector();
		vecRegObj5.add(appRegObj5);
		appConfigForm5.setConfig("marquee-2-bgcolor", vecRegObj5);
		
		
		AppConfigManager.reloadRegistry(); // need to have this line or the AppConfigManager.getPorperty("WHAT-EVER") calls in the JSP will not return any value
		
		fnAuditTrailMarquee2(servlet, req, res);

	}
	
	protected void fnAuditTrailMarquee2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		if(userId!=null) {
		  AuditTrailObject atObj = new AuditTrailObject();
		  atObj.userId = userId;
		  atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
		  //atObj.remarks = "Bullboard: " + "Article has been edited: current length " + mceInputSanitized.length() + " bytes";
		  atObj.remarks = "Marquee2: text=" + marquee2Text.substring(0, 9);
		  AuditTrailNut.fnCreate(atObj);
		}
	}
	
	protected void fnSetMarquee3Content(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	throws Exception 
	{
		// Get the userIdfrom the http session
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		
		AppConfigForm appConfigForm1 = new AppConfigForm(userId);
		AppRegObject appRegObj1 = new AppRegObject();
		appRegObj1.mNamespace = "marquee-3-text";
		appRegObj1.mUseridEdit = userId;
		appRegObj1.mContent = marquee3Text;
		Vector vecRegObj1 = new Vector();
		vecRegObj1.add(appRegObj1);
		appConfigForm1.setConfig("marquee-3-text", vecRegObj1);
		
		AppConfigForm appConfigForm2 = new AppConfigForm(userId);
		AppRegObject appRegObj2 = new AppRegObject();
		appRegObj2.mNamespace = "marquee-3-width";
		appRegObj2.mUseridEdit = userId;
		appRegObj2.mContent = marquee3Width;
		Vector vecRegObj2 = new Vector();
		vecRegObj2.add(appRegObj2);
		appConfigForm2.setConfig("marquee-3-width", vecRegObj2);
		
		AppConfigForm appConfigForm3 = new AppConfigForm(userId);
		AppRegObject appRegObj3 = new AppRegObject();
		appRegObj3.mNamespace = "marquee-3-height";
		appRegObj3.mUseridEdit = userId;
		appRegObj3.mContent = marquee3Height;
		Vector vecRegObj3 = new Vector();
		vecRegObj3.add(appRegObj3);
		appConfigForm3.setConfig("marquee-3-height", vecRegObj3);
		
		AppConfigForm appConfigForm4 = new AppConfigForm(userId);
		AppRegObject appRegObj4 = new AppRegObject();
		appRegObj4.mNamespace = "marquee-3-fgcolor";
		appRegObj4.mUseridEdit = userId;
		appRegObj4.mContent = marquee3Fgcolor;
		Vector vecRegObj4 = new Vector();
		vecRegObj4.add(appRegObj4);
		appConfigForm4.setConfig("marquee-3-fgcolor", vecRegObj4);
		
		AppConfigForm appConfigForm5 = new AppConfigForm(userId);
		AppRegObject appRegObj5 = new AppRegObject();
		appRegObj5.mNamespace = "marquee-3-bgcolor";
		appRegObj5.mUseridEdit = userId;
		appRegObj5.mContent = marquee3Bgcolor;
		Vector vecRegObj5 = new Vector();
		vecRegObj5.add(appRegObj5);
		appConfigForm5.setConfig("marquee-3-bgcolor", vecRegObj5);
		
		
		AppConfigManager.reloadRegistry(); // need to have this line or the AppConfigManager.getPorperty("WHAT-EVER") calls in the JSP will not return any value
		
		fnAuditTrailMarquee3(servlet, req, res);

	}
	
	protected void fnAuditTrailMarquee3(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		if(userId!=null) {
		  AuditTrailObject atObj = new AuditTrailObject();
		  atObj.userId = userId;
		  atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
		  //atObj.remarks = "Bullboard: " + "Article has been edited: current length " + mceInputSanitized.length() + " bytes";
		  atObj.remarks = "Marquee3: text=" + marquee3Text.substring(0, 9);
		  AuditTrailNut.fnCreate(atObj);
		}
	}
	
	
	
	
}