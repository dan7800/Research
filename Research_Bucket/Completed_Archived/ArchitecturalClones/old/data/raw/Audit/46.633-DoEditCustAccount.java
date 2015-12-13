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

import java.math.BigDecimal;
import java.util.Vector;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.Item;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.*;

public class DoEditCustAccount implements Action
{
	private String strClassName = "DoEditCustAccount";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String custPkid = req.getParameter("custPkid");
		req.setAttribute("custPkid", custPkid);
		if (formName == null)
		{
			Log.printVerbose(strClassName + ": formName = null");
			if (custPkid != null)
			{
				Log.printVerbose(custPkid);
				CustAccountObject custObj = fnFetchAccount(custPkid);
				if (custObj != null)
				{
					fnLoadAuxiliaryObjects(servlet, req, res, custObj);
				}
				req.setAttribute("custObj", custObj);
			}
			return new ActionRouter("cust-edit-cust-account-page");
		} else if (formName.equals("editCustAccount"))
		{
			try
			{
				CustAccountObject custObj = fnEditCustAccount(servlet, req, res);
				fnLoadAuxiliaryObjects(servlet, req, res, custObj);
				req.setAttribute("custObj", custObj);
				req.setAttribute("custPkid", custObj.pkid.toString());
				return new ActionRouter("cust-edit-cust-account-page");
			} catch (Exception ex)
			{
				return new ActionRouter("cust-edit-cust-account-page");
			}
		} else if (formName.equals("setByCustTree"))
		{
			try
			{
				fnSetCustomer(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("cust-edit-cust-account-page");
	}

	protected void fnLoadAuxiliaryObjects(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			CustAccountObject custObj)
	{
		Vector vecVehicles = new Vector(VehicleNut.getObjectsForCustAccount(custObj.pkid));
		req.setAttribute("vecVehicles", vecVehicles);
		Vector vecUsers = CustUserNut.getObjectsByAccount(custObj.pkid);
		req.setAttribute("vecUsers", vecUsers);
	}

	protected CustAccountObject fnFetchAccount(String custPkid)
	{
		Integer pkid = null;
		try
		{
			pkid = new Integer(custPkid);
			return CustAccountNut.getObject(pkid, "");
		} catch (Exception ex)
		{
		}
		return (CustAccountObject) null;
	}

	protected CustAccountObject fnEditCustAccount(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		String custPkid = req.getParameter("custPkid");
		Integer pkid = new Integer(custPkid);
		CustAccount custEJB = CustAccountNut.getHandle(pkid);
		CustAccountObject custObj = custEJB.getObject("");
		//sim
		custObj.userIdUpdate = (Integer) session.getAttribute("userId");
		if (custObj == null)
		{
			throw new Exception("Invalid Customer Account PKID ");
		}
		// / 1. check if existing custCode exists
		String custCode = req.getParameter("cust_code");
		if (!custCode.equals(custObj.custAccountCode))
		{
			CustAccount custEJB2 = CustAccountNut.getObjectByCode(custCode);
			if (custEJB2 != null)	
			{
				throw new Exception("The customer code exists in the database!! ");
			}
		}
		// / 2. if string is empty, generate a GUID
		if (custCode.trim().length() == 0)
		{
			GUIDGenerator gen = new GUIDGenerator();
			custCode = gen.getUUID();
		}
		Integer creditTerms = custObj.creditTerms;
		BigDecimal creditLimit = custObj.creditLimit;
		custObj.custAccountCode = custCode;
		custObj.name = req.getParameter("name");
		custObj.description = req.getParameter("description");
		custObj.accType = new Integer(req.getParameter("accType"));
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
		// /custObj.telephone1 = req.getParameter("telephone1");
		// /custObj.telephone2 = req.getParameter("telephone2");
		// /custObj.homePhone = req.getParameter("home_phone");
		// /custObj.mobilePhone = req.getParameter("mobile_phone");
		// /custObj.faxNo = req.getParameter("fax_no");
		custObj.email1 = req.getParameter("email1");
		custObj.homepage = req.getParameter("homepage");
		custObj.creditLimit = new BigDecimal(req.getParameter("creditlimit"));
		custObj.creditTerms = new Integer(req.getParameter("terms"));
		custObj.state = req.getParameter("status");
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
		
	//	custEJB.setObject(custObj);
		
		try
		{
			custEJB.setObject(custObj);
			// // audit trail!!
			if(custObj.state.equals(CustAccountBean.STATE_BL))
			{
				AuditTrailObject atObj = new AuditTrailObject();
				atObj.userId = custObj.userIdUpdate;
				atObj.auditType = AuditTrailBean.TYPE_CONFIG;
				atObj.time = TimeFormat.getTimestamp();
				atObj.remarks = custObj.name + " has been blacklisted. ";
				atObj.tc_entity_table = CustAccountBean.TABLENAME;
				atObj.tc_entity_id = custEJB.getPkid();
				atObj.tc_action = AuditTrailBean.TC_ACTION_UPDATE;	
				AuditTrailNut.fnCreate(atObj);
			}
			boolean criticalChange = false;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = custObj.userIdUpdate;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: " + custObj.pkid + " " + custObj.name + " ";
			atObj.tc_entity_table = CustAccountBean.TABLENAME;
			atObj.tc_entity_id = custEJB.getPkid();
			atObj.tc_action = AuditTrailBean.TC_ACTION_UPDATE;
			if (creditTerms.compareTo(custObj.creditTerms) != 0)
			{
				criticalChange = true;
				atObj.remarks += " Credit Terms:" + creditTerms + "->"
						+ custObj.creditTerms + " ";
			}
			if (creditLimit.compareTo(custObj.creditLimit) != 0)
			{
				criticalChange = true;
				atObj.remarks += " Credit Limit:" + creditLimit + "->"
						+ CurrencyFormat.strCcy(custObj.creditLimit) + " ";
				
			}
			if (criticalChange)
			{
				AuditTrailNut.fnCreate(atObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		return custObj;
	}

	private void fnSetCustomer(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String option = req.getParameter("option");
		if (option == null)
		{
			return;
		}
		if (option.equals("setAcc"))
		{
			String accPkid = req.getParameter("accPkid");
			try
			{
				Integer pkid = new Integer(accPkid);
				CustAccountObject custObj = CustAccountNut.getObject(pkid);
				if (custObj != null)
				{
					fnLoadAuxiliaryObjects(servlet, req, res, custObj);
					req.setAttribute("custObj", custObj);
					req.setAttribute("custPkid", custObj.pkid.toString());
				} else
				{
					throw new Exception("Invalid Account");
				}
			} catch (Exception ex)
			{
				throw new Exception("Invalid Account Number!");
			}
		}
		if (option.equals("setMember"))
		{
			String memPkid = req.getParameter("memPkid");
			try
			{
				Integer pkid = new Integer(memPkid);
				CustUserObject custUserObj = CustUserNut.getObject(pkid);
				if (custUserObj != null)
				{
					CustAccountObject custObj = CustAccountNut.getObject(custUserObj.accId);
					fnLoadAuxiliaryObjects(servlet, req, res, custObj);
					req.setAttribute("custObj", custObj);
					req.setAttribute("custPkid", custObj.pkid.toString());
				} else
				{
					throw new Exception("Invalid Member");
				}
			} catch (Exception ex)
			{
				throw new Exception("Invalid Member");
			}
		}
		if (option.equals("setCard"))
		{
			String cardPkid = req.getParameter("cardPkid");
			try
			{
				Long pkid = new Long(cardPkid);
				MemberCardObject memCardObj = MemberCardNut.getObject(pkid);
				if (memCardObj != null)
				{
					CustAccountObject custObj = CustAccountNut.getObject(memCardObj.entityKey1);
					fnLoadAuxiliaryObjects(servlet, req, res, custObj);
					req.setAttribute("custObj", custObj);
					req.setAttribute("custPkid", custObj.pkid.toString());
				} else
				{
					throw new Exception("Invalid MemberCard");
				}
			} catch (Exception ex)
			{
				throw new Exception("Invalid MemberCard");
			}
		}
	}
}
