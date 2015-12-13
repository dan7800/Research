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

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.accounting.*;

public class DoYTDTrialBalance implements Action
{
	private String strClassName = "DoYTDTrialBalance: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("acc-report-ytd-trialbalance-page");
		} 
		else if (formName.equals("genReport"))
		{
			fnGenerateReport(servlet, req, res);
		}
		
		if (formName.equals("popupPrint"))
		{
			try
			{
				fnGenerateReport(servlet, req, res);
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("acc-report-ytd-trialbalance-printable-page");
		}

		return new ActionRouter("acc-report-ytd-trialbalance-page");

	}

	protected void fnGenerateReport(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		YTDTrialBalanceForm tbs = (YTDTrialBalanceForm) session.getAttribute("acc-report-ytd-trialbalance-session");
		String strPCC = req.getParameter("pcCenter");
//		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		try
		{
			Integer iPCC = new Integer(strPCC);
			Timestamp tsDate = TimeFormat.createTimestamp(dateTo);
			tbs.setPCCenter(iPCC);
			tbs.setDateTo(tsDate);
			tbs.generateReport();
			req.setAttribute("pcCenter",strPCC);
			req.setAttribute("theDate",dateTo);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		System.out.println("Inside fnAuditTrail");
		
		HttpSession session = req.getSession(true);
		YTDTrialBalanceForm tbs = (YTDTrialBalanceForm) session.getAttribute("acc-report-ytd-trialbalance-session");
		
		Integer iUserId = (Integer) session.getAttribute("userId");
		
		String strPCCenter = tbs.getPCCenter("");
		String dateTo = tbs.getDateTo("");
		
		if (iUserId != null)
		{
			System.out.println("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Trial_Balance";
			
			try
			{
				ProfitCostCenterObject pcCenter = ProfitCostCenterNut.getObject(new Integer(strPCCenter));
				
				atObj.remarks += ", PC Center: " + pcCenter.mCode;
			}
			catch(Exception ex)
			{
				System.out.println("Exception : "+ex.toString());
			}
						
			atObj.remarks += ", Date : " + dateTo;
			
			AuditTrailNut.fnCreate(atObj);
		}
		
		System.out.println("Leaving fnAuditTrail");
	}
}
