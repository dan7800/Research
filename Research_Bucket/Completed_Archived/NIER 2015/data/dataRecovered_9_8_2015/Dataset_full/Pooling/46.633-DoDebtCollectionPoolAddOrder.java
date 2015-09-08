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

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPoolAddOrder extends ActionDo implements Action
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
			return new ActionRouter("dist-debt-collection-pool-add-order-page");
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
		if (formName.equals("addOrder"))
		{
			try
			{
				fnAddOrder(servlet, req, res);
				req.setAttribute("success-add-order", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-add-order-page");
	}

	private void fnAddOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String[] soPkid = req.getParameterValues("soPkid");
		for (int cnt = 0; cnt < soPkid.length; cnt++)
		{
			try
			{
				Long lPkid = new Long(soPkid[cnt]);
				edcf.fnAddOrder(lPkid);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String payStatus = req.getParameter("payStatus");
		DebtCollectionPoolAddOrderForm dcps = (DebtCollectionPoolAddOrderForm) session
				.getAttribute("dist-debt-collection-pool-add-order-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setPayStatus(payStatus);
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

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPoolAddOrder extends ActionDo implements Action
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
			return new ActionRouter("dist-debt-collection-pool-add-order-page");
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
		if (formName.equals("addOrder"))
		{
			try
			{
				fnAddOrder(servlet, req, res);
				req.setAttribute("success-add-order", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-add-order-page");
	}

	private void fnAddOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String[] soPkid = req.getParameterValues("soPkid");
		for (int cnt = 0; cnt < soPkid.length; cnt++)
		{
			try
			{
				Long lPkid = new Long(soPkid[cnt]);
				edcf.fnAddOrder(lPkid);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String payStatus = req.getParameter("payStatus");
		DebtCollectionPoolAddOrderForm dcps = (DebtCollectionPoolAddOrderForm) session
				.getAttribute("dist-debt-collection-pool-add-order-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setPayStatus(payStatus);
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

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPoolAddOrder extends ActionDo implements Action
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
			return new ActionRouter("dist-debt-collection-pool-add-order-page");
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
		if (formName.equals("addOrder"))
		{
			try
			{
				fnAddOrder(servlet, req, res);
				req.setAttribute("success-add-order", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-add-order-page");
	}

	private void fnAddOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String[] soPkid = req.getParameterValues("soPkid");
		for (int cnt = 0; cnt < soPkid.length; cnt++)
		{
			try
			{
				Long lPkid = new Long(soPkid[cnt]);
				edcf.fnAddOrder(lPkid);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String payStatus = req.getParameter("payStatus");
		DebtCollectionPoolAddOrderForm dcps = (DebtCollectionPoolAddOrderForm) session
				.getAttribute("dist-debt-collection-pool-add-order-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setPayStatus(payStatus);
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

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.distribution.*;
import com.vlee.util.*;

public class DoDebtCollectionPoolAddOrder extends ActionDo implements Action
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
			return new ActionRouter("dist-debt-collection-pool-add-order-page");
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
		if (formName.equals("addOrder"))
		{
			try
			{
				fnAddOrder(servlet, req, res);
				req.setAttribute("success-add-order", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("dist-debt-collection-pool-add-order-page");
	}

	private void fnAddOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditDebtCollectionForm edcf = (EditDebtCollectionForm) session.getAttribute("dist-debt-collection-pool-form");
		String[] soPkid = req.getParameterValues("soPkid");
		for (int cnt = 0; cnt < soPkid.length; cnt++)
		{
			try
			{
				Long lPkid = new Long(soPkid[cnt]);
				edcf.fnAddOrder(lPkid);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private void fnSetFilter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String payStatus = req.getParameter("payStatus");
		DebtCollectionPoolAddOrderForm dcps = (DebtCollectionPoolAddOrderForm) session
				.getAttribute("dist-debt-collection-pool-add-order-form");
		dcps.setDateRange(dateType, dateFrom, dateTo);
		dcps.setPayStatus(payStatus);
	}
}
