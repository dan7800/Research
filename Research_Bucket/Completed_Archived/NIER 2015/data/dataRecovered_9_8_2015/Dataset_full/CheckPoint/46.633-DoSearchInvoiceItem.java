/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.trading;

import java.math.*;
import java.sql.*;
import javax.servlet.http.*;
import com.vlee.bean.inventory.*;
import com.vlee.bean.pos.*;
import com.vlee.ejb.inventory.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoSearchInvoiceItem implements Action
{

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		Log.printVerbose(" formName = "+formName);
		if (formName == null)
		{
			return new ActionRouter("trading-pos-search-invoice-item-page");
		}

		if (formName.equals("searchByKeyword"))
		{
			try
			{
				fnSearchInvoiceItem(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		return new ActionRouter("trading-pos-search-invoice-item-page");
	}

	private void fnSearchInvoiceItem(HttpServlet servlet, HttpServletRequest req, 
							HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		SearchInvoiceItemForm siiForm = (SearchInvoiceItemForm) session.getAttribute(
										"trading-pos-search-invoice-item-form");
		Timestamp dateStart = TimeFormat.createTimestamp(req.getParameter("dateStart"));
		Timestamp dateEnd = TimeFormat.createTimestamp(req.getParameter("dateEnd"));
		Log.printVerbose("checkpoint 1...");	
		String keyword = req.getParameter("keyword");
		keyword = keyword.trim();
		siiForm.setDate(dateStart,dateEnd);
		siiForm.setKeyword(keyword);
		siiForm.searchRecords();
		siiForm.searchInvoice();
		Log.printVerbose("checkpoint 2...");	
	}

}



