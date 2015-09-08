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
package com.vlee.servlet.distribution;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.bean.distribution.DebtCollectionPoolSelectForm;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;

public class DoDebtCollectionPoolSelect extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String parentForm = req.getParameter("parentForm");
		if (parentForm == null)
		{
			parentForm = "dist-debt-collection-pool";
		}
		req.setAttribute("parentForm", parentForm);
		String parentAction = req.getParameter("parentAction");
		if (parentAction == null)
		{
			parentAction = "dist-debt-collection-pool";
		}
		req.setAttribute("parentAction", parentAction);
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-select-page");
		}
		if (formName.equals("setFilter"))
		{
			try
			{
				fnSetFilter(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new ActionRouter("dist-debt-collection-pool-select-page");
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		DebtCollectionPoolSelectForm dcps = (DebtCollectionPoolSelectForm) session
				.getAttribute("debt-collection-pool-select-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setState(state);
	}
}
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
package com.vlee.servlet.distribution;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.bean.distribution.DebtCollectionPoolSelectForm;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;

public class DoDebtCollectionPoolSelect extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String parentForm = req.getParameter("parentForm");
		if (parentForm == null)
		{
			parentForm = "dist-debt-collection-pool";
		}
		req.setAttribute("parentForm", parentForm);
		String parentAction = req.getParameter("parentAction");
		if (parentAction == null)
		{
			parentAction = "dist-debt-collection-pool";
		}
		req.setAttribute("parentAction", parentAction);
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-select-page");
		}
		if (formName.equals("setFilter"))
		{
			try
			{
				fnSetFilter(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new ActionRouter("dist-debt-collection-pool-select-page");
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		DebtCollectionPoolSelectForm dcps = (DebtCollectionPoolSelectForm) session
				.getAttribute("debt-collection-pool-select-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setState(state);
	}
}
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
package com.vlee.servlet.distribution;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.bean.distribution.DebtCollectionPoolSelectForm;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;

public class DoDebtCollectionPoolSelect extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String parentForm = req.getParameter("parentForm");
		if (parentForm == null)
		{
			parentForm = "dist-debt-collection-pool";
		}
		req.setAttribute("parentForm", parentForm);
		String parentAction = req.getParameter("parentAction");
		if (parentAction == null)
		{
			parentAction = "dist-debt-collection-pool";
		}
		req.setAttribute("parentAction", parentAction);
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-select-page");
		}
		if (formName.equals("setFilter"))
		{
			try
			{
				fnSetFilter(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new ActionRouter("dist-debt-collection-pool-select-page");
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		DebtCollectionPoolSelectForm dcps = (DebtCollectionPoolSelectForm) session
				.getAttribute("debt-collection-pool-select-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setState(state);
	}
}
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
package com.vlee.servlet.distribution;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.bean.distribution.DebtCollectionPoolSelectForm;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;

public class DoDebtCollectionPoolSelect extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		String parentForm = req.getParameter("parentForm");
		if (parentForm == null)
		{
			parentForm = "dist-debt-collection-pool";
		}
		req.setAttribute("parentForm", parentForm);
		String parentAction = req.getParameter("parentAction");
		if (parentAction == null)
		{
			parentAction = "dist-debt-collection-pool";
		}
		req.setAttribute("parentAction", parentAction);
		if (formName == null)
		{
			return new ActionRouter("dist-debt-collection-pool-select-page");
		}
		if (formName.equals("setFilter"))
		{
			try
			{
				fnSetFilter(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new ActionRouter("dist-debt-collection-pool-select-page");
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		DebtCollectionPoolSelectForm dcps = (DebtCollectionPoolSelectForm) session
				.getAttribute("debt-collection-pool-select-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setState(state);
	}
}
