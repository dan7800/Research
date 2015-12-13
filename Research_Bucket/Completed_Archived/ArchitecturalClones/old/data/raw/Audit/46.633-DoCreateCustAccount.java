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
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.ItemBean;

public class DoCreateCustAccount implements Action
{
	private String strClassName = "DoCreateCustAccount";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			return new ActionRouter("cust-create-cust-account-page");
		}
		if (formName.equals("createCustAccount"))
		{
			try
			{
				CustAccountObject custObj = fnCreateCustAccount(servlet, req, res);
				req.setAttribute("custObj", custObj);
				req.setAttribute("custPkid", custObj.pkid.toString());
				req.setAttribute("notifySuccess", "The customer account has been successfully created!");
				return new ActionRouter("cust-edit-cust-account-page");
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("cust-create-cust-account-page");
			}
		}
		return new ActionRouter("cust-create-cust-account-page");
	}

	protected CustAccountObject fnCreateCustAccount(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		// / 1. check if existing custCode exists
		String custCode = req.getParameter("custCode");
		if (custCode == null)
		{
			throw new Exception("Customer Code is null!!");
		}
		/*
		 * if(custCode.length()==0) { GUIDGenerator guid = new GUIDGenerator();
		 * custCode = guid.getUUID(); }
		 */
		CustAccountObject custObj = new CustAccountObject();
		custObj.accType = new Integer(req.getParameter("accType"));
		if (custObj.accType.equals(CustAccountBean.ACCTYPE_PERSONAL))
		{
			String strName = req.getParameter("name");
			int flag = 0;
			custObj.name = strName;
			String tokenizeThis = strName;
			StringTokenizer st = new StringTokenizer(tokenizeThis);
			while (st.hasMoreTokens())
			{
				if (flag == 0)
				{
					custObj.nameFirst = st.nextToken();
					flag = 1;
				} else
				{
					custObj.nameLast = custObj.nameLast + " " + st.nextToken();
				}
			}
		} else
		{
			custObj.name = req.getParameter("name");
		}
		Log.printVerbose("custCode: " + custCode);
		Log.printVerbose("custObj.nameFirst: " + custObj.nameFirst);
		Log.printVerbose("custObj.nameLast: " + custObj.nameLast);
		
		custObj.custAccountCode = custCode;
		custObj.description = req.getParameter("description");
		// custObj.accType = new Integer(req.getParameter("accType"));
		custObj.identityNumber = req.getParameter("identityNumber");
		custObj.mainAddress1 = req.getParameter("main_address1");
		custObj.mainAddress2 = req.getParameter("main_address2");
		custObj.mainAddress3 = req.getParameter("main_address3");
		custObj.mainCity = req.getParameter("main_city");
		custObj.mainPostcode = req.getParameter("main_postcode");
		custObj.mainState = req.getParameter("main_state");
		custObj.mainCountry = req.getParameter("main_country");
		custObj.telephone1 = req.getParameter("telephone1_Prefix") + "-" + req.getParameter("telephone1");
		custObj.telephone2 = req.getParameter("telephone2_Prefix") + "-" + req.getParameter("telephone2");
		custObj.homePhone = req.getParameter("home_phonePrefix") + "-" + req.getParameter("home_phone");
		custObj.mobilePhone = req.getParameter("mobile_phonePrefix") + "-" + req.getParameter("mobile_phone");
		custObj.faxNo = req.getParameter("fax_noPrefix") + "-" + req.getParameter("fax_no");
		custObj.email1 = req.getParameter("email1");
		custObj.homepage = req.getParameter("homepage");
		custObj.creditLimit = new BigDecimal(req.getParameter("creditlimit"));
		custObj.creditTerms = new Integer(req.getParameter("terms"));
		custObj.state = req.getParameter("state");
		custObj.dealerCode = req.getParameter("dealerCode");
		custObj.salesman = new Integer(req.getParameter("salesman"));
		custObj.property1 = req.getParameter("property1");
		custObj.property2 = req.getParameter("property2");
		custObj.property3 = req.getParameter("property3");
		custObj.property4 = req.getParameter("property4");
		custObj.property5 = req.getParameter("property5");
		custObj.factorPricing = new BigDecimal(req.getParameter("factorPricing"));
		custObj.factorDiscount = new BigDecimal(req.getParameter("factorDiscount"));
		custObj.monthlyFees = new BigDecimal(req.getParameter("monthlyFees"));
		
		//conditional checking
		CustAccount custEJB = CustAccountNut.getObjectByCode(custCode);
		if (custEJB != null)
		{
			req.setAttribute("custObj", custObj);
			throw new Exception("The customer code exists in the database!! ");
		}
		
		// checking for same company names if this is a corporate account
		String disableNameCheck = req.getParameter("disableNameCheck");
		if (custObj.accType.equals(CustAccountBean.ACCTYPE_CORPORATE) && disableNameCheck == null)
		{
			QueryObject query = new QueryObject(new String[] { CustAccountBean.NAME + " ~* '" + custObj.name + "' " });
			Vector vecSimilarName = new Vector(CustAccountNut.getObjects(query));
			if (vecSimilarName.size() > 0)
			{
				req.setAttribute("vecSimilarName", vecSimilarName);
				req.setAttribute("custObj", custObj);
				throw new Exception(" The Corporate Account May Have been Created, please double check!");
			}
		}
		// create the object!!
		custEJB = CustAccountNut.fnCreate(custObj);
		
		custObj = custEJB.getObject("");
		if (custObj == null)
		{
			throw new Exception("Unable to create new customer account");
		}
		{
			HttpSession session = req.getSession();
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "create customer: " + custObj.name;
			atObj.tc_entity_table = CustAccountBean.TABLENAME;
			atObj.tc_entity_id = custEJB.getPkid();
			atObj.tc_action = AuditTrailBean.TC_ACTION_CREATE;
			AuditTrailNut.fnCreate(atObj);
		}
		return custObj;
	}
}
