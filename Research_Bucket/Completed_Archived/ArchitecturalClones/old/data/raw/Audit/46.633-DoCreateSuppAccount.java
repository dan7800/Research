/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.supplier;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.supplier.*;

public class DoCreateSuppAccount implements Action
{
	private String strClassName = "DoCreateSuppAccount";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			return new ActionRouter("supp-create-supp-account-page");
		}
		if (formName.equals("createSuppAccount"))
		{
			try
			{
				SuppAccountObject suppObj = fnCreateSuppAccount(servlet, req, res);
				req.setAttribute("suppObj", suppObj);
				req.setAttribute("suppPkid", suppObj.pkid.toString());
				fnAuditTrail(servlet, req, res);
				return new ActionRouter("supp-edit-supp-account-page");
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("supp-create-supp-account-page");
			}
		}
		return new ActionRouter("supp-create-supp-account-page");
	}

	protected SuppAccountObject fnCreateSuppAccount(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		// / 1. check if existing suppCode exists
		String suppCode = req.getParameter("suppCode");
		if (suppCode == null)
		{
			throw new Exception("Supplier Code is null!!");
		}
		if (suppCode.length() == 0)
		{
			GUIDGenerator guid = new GUIDGenerator();
			suppCode = guid.getUUID();
		}
		SuppAccount suppEJB = SuppAccountNut.getObjectByCode(suppCode);
		if (suppEJB != null)
		{
			throw new Exception("The supplier code exists in the database!! ");
		}
		SuppAccountObject suppObj = new SuppAccountObject();
		suppObj.name = req.getParameter("name");
		suppObj.suppAccountCode = suppCode;
		suppObj.description = req.getParameter("description");
		suppObj.accType = new Integer(req.getParameter("accType"));
		suppObj.identityNumber = req.getParameter("identityNumber");
		suppObj.mainAddress1 = req.getParameter("main_address1");
		suppObj.mainAddress2 = req.getParameter("main_address2");
		suppObj.mainAddress3 = req.getParameter("main_address3");
		suppObj.mainPostcode = req.getParameter("main_postcode");
		suppObj.mainState = req.getParameter("main_state");
		suppObj.mainCountry = req.getParameter("main_country");
		suppObj.telephone1 = req.getParameter("usertelephone1_Prefix") + "-" + req.getParameter("usertelephone1")
				+ "Ext" + req.getParameter("userext");
		suppObj.telephone2 = req.getParameter("usertelephone2_Prefix") + "-" + req.getParameter("usertelephone2");
		suppObj.homePhone = req.getParameter("userhome_phonePrefix") + "-" + req.getParameter("userhome_phone");
		suppObj.mobilePhone = req.getParameter("usermobile_phonePrefix") + "-" + req.getParameter("usermobile_phone");
		suppObj.faxNo = req.getParameter("userfax_noPrefix") + "-" + req.getParameter("userfax_no");
		// suppObj.telephone1 = req.getParameter("telephone1");
		// suppObj.telephone2 = req.getParameter("telephone2");
		// suppObj.homePhone = req.getParameter("home_phone");
		// suppObj.mobilePhone = req.getParameter("mobile_phone");
		// suppObj.faxNo = req.getParameter("fax_no");
		suppObj.email1 = req.getParameter("email1");
		suppObj.homepage = req.getParameter("homepage");
		suppObj.creditLimit = new BigDecimal(req.getParameter("creditlimit"));
		suppObj.creditTerms = new Integer(req.getParameter("terms"));
		suppObj.nameFirst = req.getParameter("name_first");
		suppObj.nameLast = req.getParameter("name_last");
		suppObj.designation = req.getParameter("designation");
		suppObj.property1 = req.getParameter("property1");
		suppObj.property2 = req.getParameter("property2");
		suppObj.property3 = req.getParameter("property3");
		suppObj.property4 = req.getParameter("property4");
		suppObj.property5 = req.getParameter("property5");
		// / conditional checking
		suppEJB = SuppAccountNut.fnCreate(suppObj);
		suppObj = suppEJB.getObject();
		if (suppObj == null)
		{
			throw new Exception("Unable to create new supplier account");
		}
		return suppObj;
	}
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		if (iUserId != null)
		{
			SuppAccountObject suppObj = new SuppAccountObject();
			suppObj.name = req.getParameter("name");
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
			
			if(suppObj.name == null || suppObj.name.equals("") )
			{
				atObj.remarks = "Unnamed supplier was created";
			}
			else
			{
				atObj.remarks = "Supplier " + suppObj.name  + " was created";
			}
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
