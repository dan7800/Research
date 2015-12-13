package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.math.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoCardPaymentConfigEdit implements Action
{
	private String strClassName = "DoCardPaymentConfigEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if (formName != null)
		{
			if (formName.equals("selectCard"))
			{
				try
				{
					fnGetSelectCardConfig(servlet, req, res);
				} catch (Exception ex)
				{
					req.setAttribute("errMsg", ex.getMessage());
				}
			}
			if (formName.equals("editConfig"))
			{
				try
				{
					fnGetEditConfig(servlet, req, res);
					fnGetSelectCardConfig(servlet, req, res);
					req.setAttribute("notifySuccess", "Successfully edited the card payment");
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		return new ActionRouter("acc-card-payment-config-edit-page");
	}

	private void fnGetSelectCardConfig(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("cpcPkid");
		Integer cpcPkid = new Integer(strPkid);
		CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(cpcPkid);
		req.setAttribute("cpcObj", cpcObj);
	}

	private void fnGetEditConfig(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String strPkid = req.getParameter("cpcPkid");
		Integer cpcPkid = new Integer(strPkid);
		CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(cpcPkid);
		cpcObj.namespace = CardPaymentConfigBean.NS_CARD_CHARGES;
		cpcObj.sortcode = req.getParameter("sortcode");
		cpcObj.bankCode = req.getParameter("bankCode");
		cpcObj.bankName = req.getParameter("bankName");
		cpcObj.paymentMode = req.getParameter("paymentMode");
		cpcObj.policyCharges = req.getParameter("policyCharges");
		cpcObj.pctCharges = new BigDecimal(0);
		cpcObj.paymentType = req.getParameter("paymentType");
		cpcObj.defaultCardType = req.getParameter("defaultCardType");
		cpcObj.defaultPaymentStatus = req.getParameter("paymentStatus");
		cpcObj.defaultPaymentRemarks = req.getParameter("paymentRemarks");
		cpcObj.property1 = req.getParameter("property1");
		cpcObj.property2 = req.getParameter("property2");
		String cashbookOpt = req.getParameter("cashbookOpt");
		if (cashbookOpt != null)
		{
			cpcObj.cashbookOpt = cashbookOpt;
			try
			{
				cpcObj.cashbook = new Integer(req.getParameter("cashbook"));
			} catch (Exception ex)
			{
			}
		} else
		{
			cpcObj.cashbookOpt = "";
			cpcObj.cashbook = new Integer(0);
		}

      cpcObj.custAdminFeeOption = req.getParameter("custAdminFeeOption");
      try{ cpcObj.custAdminFeeAbs = new BigDecimal(req.getParameter("custAdminFeeAbs"));}
      catch(Exception ex){ex.printStackTrace();}
      try{ cpcObj.custAdminFeeMinAmount = new BigDecimal(req.getParameter("custAdminFeeMinAmount"));}
      catch(Exception ex){ex.printStackTrace();}
      try{ cpcObj.custAdminFeeMaxAmount = new BigDecimal(req.getParameter("custAdminFeeMaxAmount"));}
      catch(Exception ex){ex.printStackTrace();}
      try{ cpcObj.custAdminFeeRatio = new BigDecimal(req.getParameter("custAdminFeeRatio"));}
      catch(Exception ex){ex.printStackTrace();}

      try
      {
         String custAdminFeeItemCode = req.getParameter("custAdminFeeItemCode");
         if(custAdminFeeItemCode!=null && custAdminFeeItemCode.trim().length()>0)
         {
            custAdminFeeItemCode = custAdminFeeItemCode.trim();
            ItemObject itmObj = ItemNut.getValueObjectByCode(custAdminFeeItemCode);
            if(itmObj!=null)
            {
               cpcObj.custAdminFeeItemid = itmObj.pkid;
            }
            else
            {
               cpcObj.custAdminFeeItemid = new Integer(0);
            }
         }
      }
      catch(Exception ex)
      { }

      try{ cpcObj.custAdminFeeRoundingScale = new Integer(req.getParameter("custAdminFeeRoundingScale"));}
      catch(Exception ex){}
      try{ cpcObj.custAdminFeeRoundingMode = new Integer(req.getParameter("custAdminFeeRoundingMode"));}
      catch(Exception ex){}



		//[[JOB-JOE
		String branchStr = req.getParameter("branch");
		if(branchStr!=null) cpcObj.branch = new Integer(branchStr);
		String branchOpt = req.getParameter("branchOpt");
		if(branchOpt!=null) cpcObj.branchOpt = branchOpt;
		//JOB-JOE]]
		
		String strCharges = req.getParameter("pctCharges");
		CardPaymentConfig cpcEJB = CardPaymentConfigNut.getHandle(cpcPkid);
		try
		{
			BigDecimal pctCharges = new BigDecimal(strCharges);
			cpcObj.pctCharges = pctCharges.divide(new BigDecimal(100), 12, BigDecimal.ROUND_HALF_EVEN);
			cpcEJB.setObject(cpcObj);
			{
				HttpSession session = req.getSession();
				AuditTrailObject atObj = new AuditTrailObject();
				atObj.userId = (Integer) session.getAttribute("userId");
				atObj.auditType = AuditTrailBean.TYPE_CONFIG;
				atObj.time = TimeFormat.getTimestamp();
				atObj.remarks = "delete card payment config ";
				atObj.tc_entity_table = CardPaymentConfigBean.TABLENAME;
				atObj.tc_entity_id = cpcEJB.getPkid();
				atObj.tc_action = AuditTrailBean.TC_ACTION_UPDATE;
				AuditTrailNut.fnCreate(atObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}



