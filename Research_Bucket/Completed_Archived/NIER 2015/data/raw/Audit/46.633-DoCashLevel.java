/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.finance;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.bean.finance.*;

public class DoCashLevel implements Action
{
	// Member variables
	String strObjectName = new String("DoCashLevel");

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("finance-cash-level-page");
		}
		if (formName.equals("setPCCenter"))
		{
			try
			{
				fnSetPCCenter(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDateTo"))
		{
			fnSetDateTo(servlet, req, res);
		}
		return new ActionRouter("finance-cash-level-page");
	}

	private void fnSetPCCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		CashLevelSession clvl = (CashLevelSession) session.getAttribute("finance-cash-level-session");
		try
		{
			Integer pcCenter = new Integer(req.getParameter("pcCenter"));
			clvl.setPCCenter(pcCenter);
		} catch (Exception ex)
		{
			throw new Exception(" An error occured, invalid PC Center ");
		}
	}

	private void fnSetDateTo(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String dateTo = req.getParameter("dateTo");
		HttpSession session = req.getSession();
		CashLevelSession clvl = (CashLevelSession) session.getAttribute("finance-cash-level-session");
		clvl.setDateTo(TimeFormat.createTimestamp(dateTo));
		
		fnAuditTrail(servlet, req, res);
	}
	
	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		System.out.println("Inside fnAuditTrail");
		
		HttpSession session = req.getSession(true);		
		CashLevelSession clvl = (CashLevelSession) session.getAttribute("finance-cash-level-session");
		
		Integer iUserId = (Integer) session.getAttribute("userId");
		
		String strPCCenter = clvl.getPCCenter().toString();
		String strDate = TimeFormat.strDisplayDate(clvl.getDateTo());
				
		if (iUserId != null)
		{
			System.out.println("userid : "+iUserId.toString());
			
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_USAGE;
			atObj.remarks = "generate-rpt: Cash Level";
			
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
