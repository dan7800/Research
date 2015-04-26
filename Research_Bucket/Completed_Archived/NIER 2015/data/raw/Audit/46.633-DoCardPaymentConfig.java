package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.math.*;
import java.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.inventory.*;
import com.vlee.util.*;

public class DoCardPaymentConfig extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if ((formName == null) || (formName.equals("editConfig")))
		{
			fnGetList(servlet, req, res);
			return new ActionRouter("acc-card-payment-config-page");
		}
		if (formName.equals("addConfig"))
		{
			fnAddConfig(servlet, req, res);
		}
		if (formName.equals("removeConfig"))
		{
			fnRemoveConfig(servlet, req, res);
		}
		if (formName.equals("popupPrint"))
		{
			fnGetList(servlet, req, res);
			return new ActionRouter("acc-card-payment-config-printable-page");
		}
		fnGetList(servlet, req, res);
		return new ActionRouter("acc-card-payment-config-page");
	}

	private void fnAddConfig(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		CardPaymentConfigObject cpcObj = new CardPaymentConfigObject();
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
		
		if (cashbookOpt != null)
		{
			cpcObj.cashbookOpt = cashbookOpt;
			try
			{
				cpcObj.cashbook = new Integer(req.getParameter("cashbook"));
			} catch (Exception ex)
			{
			}
		}
		String strCharges = req.getParameter("pctCharges");
		try
		{
			BigDecimal pctCharges = new BigDecimal(strCharges);
			cpcObj.pctCharges = pctCharges.divide(new BigDecimal(100), 12, BigDecimal.ROUND_HALF_EVEN);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		CardPaymentConfig cpcEJB = CardPaymentConfigNut.fnCreate(cpcObj);
		{
			HttpSession session = req.getSession();
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = (Integer) session.getAttribute("userId");
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "create card payment config ";
			atObj.tc_entity_table = CardPaymentConfigBean.TABLENAME;
			
			try
			{
				atObj.tc_entity_id = cpcEJB.getPkid();
			}
			catch(Exception ex)
			{ 
				ex.printStackTrace();
			}
			atObj.tc_action = AuditTrailBean.TC_ACTION_CREATE;
			AuditTrailNut.fnCreate(atObj);
		}
	}

	private synchronized void fnRemoveConfig(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pkid = req.getParameter("cpcPkid");
		try
		{
			CardPaymentConfig cpcEJB = CardPaymentConfigNut.getHandle(new Integer(pkid));
			Log.printVerbose(" BEFOREEEEEEEEEEEEEE");
			if (cpcEJB != null)
			{
				Log.printVerbose(" REEEEEEEEEEEEEEEMOVVVVVVVVVVEEEEEEEEEEEE ");
				cpcEJB.remove();
				{
					HttpSession session = req.getSession();
					AuditTrailObject atObj = new AuditTrailObject();
					atObj.userId = (Integer) session.getAttribute("userId");
					atObj.auditType = AuditTrailBean.TYPE_CONFIG;
					atObj.time = TimeFormat.getTimestamp();
					atObj.remarks = "delete card payment config ";
					atObj.tc_entity_table = CardPaymentConfigBean.TABLENAME;
					atObj.tc_entity_id = cpcEJB.getPkid();
					atObj.tc_action = AuditTrailBean.TC_ACTION_DELETE;
					AuditTrailNut.fnCreate(atObj);
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnGetList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		QueryObject query = new QueryObject(new String[] { CardPaymentConfigBean.NAMESPACE + " = '"
				+ CardPaymentConfigBean.NS_CARD_CHARGES + "' " });
		query.setOrder(" ORDER BY " + CardPaymentConfigBean.SORTCODE);
		Vector vecCardPaymentConfig = new Vector(CardPaymentConfigNut.getObjects(query));
		req.setAttribute("vecCardPaymentConfig", vecCardPaymentConfig);
	}
}
