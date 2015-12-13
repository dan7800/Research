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
import com.vlee.ejb.intranet.*;

public class DoArticles implements Action
{
	private String strClassName = "DoArticles";
	
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String theFwdPage = req.getParameter("theFwdPage");
		if(theFwdPage == null)
		{ theFwdPage = "intranet-articles-ejbtest-page"; }
		String mode = req.getParameter("mode");
		if(mode==null) mode = "";
		
		
		
		
		
		HttpSession session = req.getSession();
 		Integer userId = (Integer) session.getAttribute("userId");
 		
 		IntranetArticleObject artObj = new IntranetArticleObject();

 		artObj.code = "";
 		artObj.permission = "";
 		artObj.title = new String("ohne titel");
 		IntranetArticleNut.fnCreate(artObj);
 		
		
		if(mode.startsWith("lslatest")) // display latest 10 articles
		{
			QueryObject qry = new QueryObject(new String[] {"", "", ""});
			Vector vecArticles = new Vector(IntranetArticleNut.getObjects(qry));
			req.setAttribute("vecArticles", vecArticles);
		
		
			return new ActionRouter("intranet-articles-display-latest-page");
		}
		
		return new ActionRouter(theFwdPage);
	}
	
	
	
// 	protected void fnAuditTrailMarquee1(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
// 	{
// 		HttpSession session = req.getSession();
// 		Integer userId = (Integer) session.getAttribute("userId");
// 		if(userId!=null) {
// 		  AuditTrailObject atObj = new AuditTrailObject();
// 		  atObj.userId = userId;
// 		  atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
// 		  //atObj.remarks = "Bullboard: " + "Article has been edited: current length " + mceInputSanitized.length() + " bytes";
// 		  atObj.remarks = "Marquee1: text=" + marquee1Text.substring(0, 9);
// 		  AuditTrailNut.fnCreate(atObj);
// 		}
// 	}
	
	
}
