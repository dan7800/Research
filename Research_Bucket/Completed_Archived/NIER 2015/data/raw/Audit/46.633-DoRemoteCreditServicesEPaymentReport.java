package com.vlee.servlet.creditservices;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.SHA1DigiSignControl;
import com.vlee.util.TimeFormat;
import com.vlee.bean.remotecreditservices.*;
import com.vlee.bean.user.PermissionManager;
import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.accounting.CardPaymentConfigNut;
import com.vlee.ejb.accounting.CardPaymentConfigObject;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserConfigObject;
import com.vlee.ejb.user.UserConfigRegistryNut;
import com.vlee.ejb.user.UserNut;
import com.vlee.ejb.user.UserRoleNut;

import java.math.*;
import java.sql.Timestamp;


public class DoRemoteCreditServicesEPaymentReport implements Action
{
	
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		if(formName!=null)
		{
			if (formName.equals("setByCustTree"))
			{
				try
				{
					fnSetCustomer(servlet, req, res);
				} catch (Exception ex)
				{
					req.setAttribute("errMsg", ex.getMessage());
				}
			}			
			else if(formName.equals("getTxnList"))
			{
				try
				{
					fnGetList(servlet, req, res);
					return new ActionRouter("remote-credit-services-epayment-report-page");
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					return new ActionRouter("remote-credit-services-epayment-report-page");
				}				
			}
			else if(formName.equals("popupPrintEpaymentReport"))
			{
				try
				{
					return new ActionRouter("remote-credit-services-print-epayment-report-page");
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					return new ActionRouter("remote-credit-services-epayment-report-page");
				}				
			}
			else
			{
				return new ActionRouter("remote-credit-services-epayment-report-page");
			}
		}
		else
		{
			return new ActionRouter("remote-credit-services-epayment-report-page");
		}
		return new ActionRouter("remote-credit-services-epayment-report-page");
		
	}	
	
	private void fnGetList(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		RemoteCreditServicesEPaymentReportForm prctForm = (RemoteCreditServicesEPaymentReportForm) session.getAttribute("remote-credit-services-epayment-session");
		if(prctForm==null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			prctForm= new RemoteCreditServicesEPaymentReportForm();
			session.setAttribute("remote-credit-services-epayment-session", prctForm);
		}	
		String dateFrom = req.getParameter("dateStart");
		String dateTo = req.getParameter("dateEnd");
		String custPkid = req.getParameter("custPkid");
		String sort = req.getParameter("sort");
		Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
		Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
		prctForm.setDateTo(tsTo);
		prctForm.setDateFrom(tsFrom);
		prctForm.setCustPkid(new Integer(custPkid));
		prctForm.setOrderBy(sort);
		prctForm.getTransactionList();
		
		
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
					HttpSession session = req.getSession();
					RemoteCreditServicesEPaymentReportForm prctForm = (RemoteCreditServicesEPaymentReportForm) session.getAttribute("remote-credit-services-epayment-session");
					prctForm.setCustPkid(custObj.pkid);
				} else
				{
					throw new Exception("Invalid Account");
				}
			} catch (Exception ex)
			{
				throw new Exception("Invalid Account Number!");
			}
		}


	}
	
}
