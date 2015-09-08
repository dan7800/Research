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
import com.vlee.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.bean.distribution.*;

public class DoDebtCollectionPoolListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");

		if(formName==null)
		{
			return new ActionRouter("dist-debt-collection-pool-listing-page");
		}

		if(formName.equals("getListing"))
		{
			fnGetListing(servlet,req,res);
		}

		return new ActionRouter("dist-debt-collection-pool-listing-page");
	}


	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		DebtCollectionPoolListingForm dcpl = (DebtCollectionPoolListingForm) 
					session.getAttribute("dist-debt-collection-pool-listing-form");

		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		
		try
		{
			dcpl.setDateRange(dateFrom, dateTo);
			dcpl.setState(state);
			dcpl.searchRecords();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
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
import com.vlee.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.bean.distribution.*;

public class DoDebtCollectionPoolListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");

		if(formName==null)
		{
			return new ActionRouter("dist-debt-collection-pool-listing-page");
		}

		if(formName.equals("getListing"))
		{
			fnGetListing(servlet,req,res);
		}

		return new ActionRouter("dist-debt-collection-pool-listing-page");
	}


	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		DebtCollectionPoolListingForm dcpl = (DebtCollectionPoolListingForm) 
					session.getAttribute("dist-debt-collection-pool-listing-form");

		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		
		try
		{
			dcpl.setDateRange(dateFrom, dateTo);
			dcpl.setState(state);
			dcpl.searchRecords();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
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
import com.vlee.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.bean.distribution.*;

public class DoDebtCollectionPoolListing extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");

		if(formName==null)
		{
			return new ActionRouter("dist-debt-collection-pool-listing-page");
		}

		if(formName.equals("getListing"))
		{
			fnGetListing(servlet,req,res);
		}

		return new ActionRouter("dist-debt-collection-pool-listing-page");
	}


	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		DebtCollectionPoolListingForm dcpl = (DebtCollectionPoolListingForm) 
					session.getAttribute("dist-debt-collection-pool-listing-form");

		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String state = req.getParameter("state");
		
		try
		{
			dcpl.setDateRange(dateFrom, dateTo);
			dcpl.setState(state);
			dcpl.searchRecords();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}



}



