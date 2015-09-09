/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.loyal;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;

import com.vlee.ejb.customer.*;
import com.vlee.bean.loyalty.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class DoMembershipCampaignRewardEdit extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");

		/// initialize the form if it has not been iniitalized
		fnInitForm(servlet,req,res);

		if(formName==null)
		{ return new ActionRouter("loyal-membership-campaign-reward-edit-page"); }

		if(formName.equals("editCampaign"))
		{
			fnEditCampaign(servlet,req,res);
		}
		if (formName.equals("popupPrint"))
		{
			return new ActionRouter("print-loyal-membership-campaign-reward-edit-page");
		}
		if(formName.equals("newCampaign"))
		{
			try
			{
				fnNewCampaign(servlet,req,res);
			}
			catch(Exception ex)
			{
				req.setAttribute("errMsg",ex.getMessage());
			}
		}


		if(formName.equals("setDetails"))
		{
			try
			{
				fnSetDetails(servlet,req,res);
			}
			catch(Exception ex)
			{
				req.setAttribute("errMsg",ex.getMessage());
			}
		}

		if(formName.equals("updateOrAddRules"))
		{
			try
			{
				fnUpdateOrAddRules(servlet,req,res);
			}
			catch(Exception ex)
			{req.setAttribute("errMsg",ex.getMessage());}
		}

		if(formName.equals("removeRules"))
		{
			try
			{
				fnRemoveRules(servlet,req,res);
			}
			catch(Exception ex)
			{
				req.setAttribute("errMsg",ex.getMessage());
			}
		}

		if(formName.equals("setSequence"))
		{
			fnSetSequence(servlet,req,res);
		}

		if(formName.equals("deleteCampaign"))
		{
			fnDeleteCampaign(servlet,req,res);
		}

		return new ActionRouter("loyal-membership-campaign-reward-edit-page");
	}

	private void fnDeleteCampaign(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
      HttpSession session = req.getSession();
      MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm) session.getAttribute("loyal-membership-campaign-reward-edit-form");
		String campaignId = req.getParameter("campaignId");
		try
		{
			Integer iCampaign = new Integer(campaignId);
			CustMembershipCampaignIndex campaignEJB = CustMembershipCampaignIndexNut.getHandle(iCampaign);
         QueryObject queryRules = new QueryObject(new String[]{
               CustMembershipCampaignRulesBean.INDEX_ID+ " = '"+iCampaign.toString()+"' ", });
         queryRules.setOrder(" ORDER BY "+CustMembershipCampaignRulesBean.SEQUENCE+", "+CustMembershipCampaignRulesBean.PKID);
         Vector vecRules = new Vector(CustMembershipCampaignRulesNut.getObjects(queryRules));
			for(int cnt1=0;cnt1<vecRules.size();cnt1++)
			{
				CustMembershipCampaignRulesObject rulesObj = (CustMembershipCampaignRulesObject) vecRules.get(cnt1);	
				CustMembershipCampaignRules rulesEJB = CustMembershipCampaignRulesNut.getHandle(rulesObj.pkid);
				rulesEJB.remove();	
				{
					AuditTrailObject atObj = new AuditTrailObject();
					atObj.userId = (Integer) session.getAttribute("userId");
					atObj.auditType = AuditTrailBean.TYPE_CONFIG;
					atObj.time = TimeFormat.getTimestamp();
					atObj.remarks = "delete campaign rules ";
					atObj.tc_entity_table = CustMembershipCampaignRulesBean.TABLENAME;
					atObj.tc_entity_id = rulesEJB.getPkid();
					atObj.tc_action = AuditTrailBean.TC_ACTION_DELETE;
					AuditTrailNut.fnCreate(atObj);
				}
			}
			campaignEJB.remove();
			{
				AuditTrailObject atObj = new AuditTrailObject();
				atObj.userId = (Integer) session.getAttribute("userId");
				atObj.auditType = AuditTrailBean.TYPE_CONFIG;
				atObj.time = TimeFormat.getTimestamp();
				atObj.remarks = "update campaign ";
				atObj.tc_entity_table = CustMembershipCampaignIndexBean.TABLENAME;
				atObj.tc_entity_id = campaignEJB.getPkid();
				atObj.tc_action = AuditTrailBean.TC_ACTION_DELETE;
				AuditTrailNut.fnCreate(atObj);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		CustMembershipCampaignEngine.reloadCampaign();
		mceForm.reset();	

	}

	private void fnSetSequence(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
      HttpSession session = req.getSession();
      MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm) session.getAttribute("loyal-membership-campaign-reward-edit-form");
		String[] guid = req.getParameterValues("guid");
		String[] sequence = req.getParameterValues("sequence");

		for(int cnt1=0;cnt1<guid.length; cnt1++)
		{
			try
			{
				Integer iSequence = new Integer(sequence[cnt1]);
				mceForm.setSequence(guid[cnt1], iSequence);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}


	private void fnUpdateOrAddRules(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
      HttpSession session = req.getSession();
      MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm) session.getAttribute("loyal-membership-campaign-reward-edit-form");
		String tokenId =  req.getParameter("tokenId");
		MembershipCampaignWidgetRewardRulesForm mcwrrForm = (MembershipCampaignWidgetRewardRulesForm) session.getAttribute(tokenId);
		if(mcwrrForm!=null)
		{
			mceForm.addWidgetRules(mcwrrForm);
		}
		else
		{
			req.setAttribute("errMsg","The Reward Rules is NULL !");
		}

	}

	private void fnEditCampaign(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
      HttpSession session = req.getSession();
      MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm) session.getAttribute("loyal-membership-campaign-reward-edit-form");
		if(mceForm==null)
		{
      	Integer userId = (Integer) session.getAttribute("userId");
      	mceForm = new MembershipCampaignEditForm(userId);
      	session.setAttribute("loyal-membership-campaign-reward-edit-form",mceForm);
		}


		String campaignId = req.getParameter("campaignId");
		try
		{
			Integer iCampaignId = new Integer(campaignId);
			mceForm.loadCampaign(iCampaignId);
			Vector vecWidgetRules = new Vector(mceForm.getTreeWidgetRules().values());
			for(int cnt1=0;cnt1<vecWidgetRules.size();cnt1++)
			{
				MembershipCampaignWidgetRewardRulesForm widgetRulesForm = (MembershipCampaignWidgetRewardRulesForm)
													vecWidgetRules.get(cnt1);
				session.setAttribute(widgetRulesForm.getGuid(),widgetRulesForm);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			req.setAttribute("errMsg", "Cannot load this campaign !");
		}
	}

	private void fnRemoveRules(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{

      HttpSession session = req.getSession();
      MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm) session.getAttribute("loyal-membership-campaign-reward-edit-form");
		String guid = req.getParameter("guid");
		mceForm.removeWidgetRules(guid);
	}

   private void fnSetDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
         throws Exception
   {
      HttpSession session = req.getSession();
      MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm)
                           session.getAttribute("loyal-membership-campaign-reward-edit-form");
		String theSubmit = req.getParameter("theSubmit");

		if(theSubmit!=null && theSubmit.equals("Close"))
		{
			mceForm.reset();	
			return;
		}


		String code = req.getParameter("code");
		String name = req.getParameter("name");
		String description = req.getParameter("description");
		String date_start = req.getParameter("date_start");
		String date_end = req.getParameter("date_end");
		String membership_logic = req.getParameter("membership_logic");
		String branch_logic = req.getParameter("branch_logic");
		String branch_conditions = req.getParameter("branch_conditions");
		String status = req.getParameter("status");

		Timestamp tsStart = TimeFormat.createTimestamp(date_start);
		Timestamp tsEnd = TimeFormat.createTimestamp(date_end);

		mceForm.setDetails(code, name, description, tsStart, tsEnd, 
								membership_logic, branch_logic, branch_conditions,
								status);
		if(theSubmit!=null && theSubmit.equals("Save & Commit"))
		{
			mceForm.saveCampaign();
		}
   }

	private synchronized void fnNewCampaign(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
				throws Exception
	{
		HttpSession session = req.getSession();
		MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm)
									session.getAttribute("loyal-membership-campaign-reward-edit-form");
		mceForm.createNewCampaign();
	}

	private void fnInitForm(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		MembershipCampaignEditForm mceForm = (MembershipCampaignEditForm)
					session.getAttribute("loyal-membership-campaign-reward-edit-form");

		if(mceForm == null)
		{	
			Integer userId = (Integer) session.getAttribute("userId");
			mceForm = new MembershipCampaignEditForm(userId);
			session.setAttribute("loyal-membership-campaign-reward-edit-form",mceForm);
		}

	}

}




