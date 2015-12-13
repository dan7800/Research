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
import com.vlee.ejb.accounting.ProfitCostCenterNut;
import com.vlee.ejb.accounting.ProfitCostCenterObject;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.TimeFormat;

public class DoBalSheet implements Action
{
	private String strClassName = "DoBalSheet: ";
	
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		fnPreserveParams(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter("acc-balsheet-page");
		}
		/*else if (formName.equals("balanceSheet"))
		{
			fnGetBalSheet(servlet, req, res);
		}*/
		
		if (formName.equals("popupPrint"))
                {
                        try
                        {
                                fnGetBalSheet(servlet, req, res);
                        } catch (Exception ex)
                        {
                                req.setAttribute("errMsg", ex.getMessage());
                        }
                        return new ActionRouter("acc-balsheet-printable-page");
                }
		
		if (formName.equals("balanceSheet"))
		{
			try
			{
				fnGetBalSheet(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("acc-balsheet-page");
	}

	protected void fnGetBalSheet(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String strDate = req.getParameter("theDate");
		String strPCC = req.getParameter("pcCenter");
		GLSummaryObject glSumObj = new GLSummaryObject();
		try
		{
			glSumObj.pcCenter = new Integer(strPCC);
		} catch (Exception ex)
		{
			throw new Exception("Invalid PC Center!");
		}
		// glSumObj.jTxnCode = JournalTransactionBean.TXNCODE_PROFIT;
		glSumObj.dateFrom = TimeFormat.createTimestamp("0001-01-01");
		glSumObj.dateTo = TimeFormat.createTimestamp(strDate);
		glSumObj = GeneralLedgerNut.getSummary(glSumObj);
		req.setAttribute("balsheet-glSumObj", glSumObj);
		
		fnAuditTrail(servlet, req, res);
	}

	protected void fnPreserveParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strDate = req.getParameter("theDate");
		if (strDate == null)
		{
			strDate = TimeFormat.strDisplayDate();
		}
		req.setAttribute("theDate", strDate);
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
		
		String strPCCenter = req.getParameter("pcCenter");
		String strDate = req.getParameter("theDate");
				
		if (iUserId != null)
		{
			System.out.println("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Balance Sheet";
			
			try
			{
				ProfitCostCenterObject pcCenter = ProfitCostCenterNut.getObject(new Integer(strPCCenter));
				
				atObj.remarks += ", PC Center: " + pcCenter.mCode;
			}
			catch(Exception ex)
			{
				System.out.println("Exception : "+ex.toString());
			}
						
			atObj.remarks += ", Date : " + strDate;
			
			AuditTrailNut.fnCreate(atObj);
		}
		
		System.out.println("Leaving fnAuditTrail");
	}
}
