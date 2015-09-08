/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.bean.accounting.YTDTrialBalanceForm;
import com.vlee.ejb.accounting.GLSummaryObject;
import com.vlee.ejb.accounting.GeneralLedgerNut;
import com.vlee.ejb.accounting.JournalTransactionBean;
import com.vlee.ejb.accounting.ProfitCostCenterNut;
import com.vlee.ejb.accounting.ProfitCostCenterObject;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.TimeFormat;

public class DoProfitLossStmt implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws java.io.IOException,
			javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		fnPreserveParams(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter("acc-profitloss-stmt-page");
		}

		if (formName.equals("popupPrint"))
		{
			try
			{
				fnGetProfitLossStmt(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("acc-profitloss-stmt-popup-page");
		}

		if (formName.equals("getProfitLossStmt"))
		{
			try
			{
				fnGetProfitLossStmt(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new ActionRouter("acc-profitloss-stmt-page");
	}

	protected void fnGetProfitLossStmt(HttpServlet servlet,
			HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String strFrom = req.getParameter("dateFrom");
		String strTo = req.getParameter("dateTo");
		String strPCC = req.getParameter("pcCenter");
		GLSummaryObject glSumObj = new GLSummaryObject();
		try
		{
			glSumObj.pcCenter = new Integer(strPCC);
		} catch (Exception ex)
		{
			throw new Exception("Invalid PC Center!");
		}
		glSumObj.jTxnCode = JournalTransactionBean.TXNCODE_PROFIT;
		glSumObj.dateFrom = TimeFormat.createTimestamp(strFrom);
		glSumObj.dateTo = TimeFormat.createTimestamp(strTo);
		glSumObj = GeneralLedgerNut.getSummary(glSumObj);
		req.setAttribute("profitLoss-glSumObj", glSumObj);
		
		fnAuditTrail(servlet, req, res);
	}

	/*
     * protected void fnPopUpPrint(HttpServlet servlet, HttpServletRequest req,
     * HttpServletResponse res) throws Exception { String strFrom =
     * req.getParameter("dateFrom"); String strTo = req.getParameter("dateTo");
     * String strPCC = req.getParameter("pcCenter"); GLSummaryObject glSumObj =
     * new GLSummaryObject(); try { glSumObj.pcCenter = new Integer(strPCC); }
     * catch (Exception ex) { throw new Exception("Invalid PC Center!"); }
     * glSumObj.jTxnCode = JournalTransactionBean.TXNCODE_PROFIT;
     * glSumObj.dateFrom = TimeFormat.createTimestamp(strFrom); glSumObj.dateTo =
     * TimeFormat.createTimestamp(strTo); glSumObj =
     * GeneralLedgerNut.getSummary(glSumObj);
     * req.setAttribute("profitLoss-glSumObj", glSumObj); }
     */

	protected void fnPreserveParams(HttpServlet servlet,
			HttpServletRequest req, HttpServletResponse res)
	{
		String strFrom = req.getParameter("dateFrom");
		if (strFrom == null)
		{
			strFrom = TimeFormat.strDisplayDate();
		}
		req.setAttribute("dateFrom", strFrom);
		String strTo = req.getParameter("dateTo");
		if (strTo == null)
		{
			strTo = TimeFormat.strDisplayDate();
		}
		req.setAttribute("dateTo", strTo);
		String strPCC = req.getParameter("pcCenter");
		if (strPCC == null)
		{
			strPCC = "0";
		}
		req.setAttribute("pcCenter", strPCC);
	}
	
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		System.out.println("Inside fnAuditTrail");
		
		HttpSession session = req.getSession(true);				
		Integer iUserId = (Integer) session.getAttribute("userId");
		
		String strFrom = req.getParameter("dateFrom");
		String strTo = req.getParameter("dateTo");
		String strPCC = req.getParameter("pcCenter");
		
		String strPCCenter = strPCC;
		String dateTo = strTo;
		String dateFrom = strFrom;
		
		if (iUserId != null)
		{
			System.out.println("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Profit and Loss";
			
			try
			{
				ProfitCostCenterObject pcCenter = ProfitCostCenterNut.getObject(new Integer(strPCCenter));
				
				atObj.remarks += ", PC Center: " + pcCenter.mCode;
			}
			catch(Exception ex)
			{
				System.out.println("Exception : "+ex.toString());
			}
						
			atObj.remarks += ", Date : " + dateFrom + " to " + dateTo;
			
			AuditTrailNut.fnCreate(atObj);
		}
		
		System.out.println("Leaving fnAuditTrail");
	}
}
