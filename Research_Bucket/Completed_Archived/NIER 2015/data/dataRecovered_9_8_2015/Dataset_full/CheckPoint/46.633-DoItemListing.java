package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.bean.inventory.*;

public class DoItemListing implements Action
{
	private String strClassName = "DoListAllItems";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-item-listing-page");
		}
		
		
		if (formName.equals("popupPrintOut"))
		{
			
			System.out.println("---------------------CHECKPOINT0");
			//fnGetListing(servlet, req, res);
			HttpSession session = req.getSession();
			ItemListingForm ilf = (ItemListingForm) session.getAttribute("inv-item-listing-form");
			Vector vecItem = ilf.getListing();
			if (vecItem != null)
			{
				req.setAttribute("vecItem", vecItem);
			}			
			System.out.println("---------------------CHECKPOINT1");
			return new ActionRouter("inv-item-listing-printable-page");
		
		}
		
		
		if (formName.equals("popupStockTake"))
		{
			
			System.out.println("---------------------CHECKPOINT0");
			//fnGetListing(servlet, req, res);
			HttpSession session = req.getSession();
			ItemListingForm ilf = (ItemListingForm) session.getAttribute("inv-item-listing-form");
			Vector vecItem = ilf.getListing();
			if (vecItem != null)
			{
				req.setAttribute("vecItem", vecItem);
			}			
			System.out.println("---------------------CHECKPOINT1");
			return new ActionRouter("inv-item-listing-printable-stock-take-page");
		
		}
		
		
		
		
		
		if (formName.equals("getListing"))
		{
			fnGetListing(servlet, req, res);
		}
		return new ActionRouter("inv-item-listing-page");
	}

	// //////////////////////
	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		ItemListingForm ilf = (ItemListingForm) session.getAttribute("inv-item-listing-form");
		System.out.println("---------------------CHECKPOINT1" +ilf);
		
		
		if (ilf == null)
		{
			
			System.out.println("---------------------ILF NULL");
			return;
		}
		String filterCodeRange = req.getParameter("filterCodeRange");
		String codeFrom = req.getParameter("codeFrom");
		String codeTo = req.getParameter("codeTo");
		String filterCategory0 = req.getParameter("filterCategory0");
		String idCategory0 = req.getParameter("idCategory0");
		String filterCategory1 = req.getParameter("filterCategory1");
		String category1 = req.getParameter("category1");
		String filterCategory2 = req.getParameter("filterCategory2");
		String category2 = req.getParameter("category2");
		String filterCategory3 = req.getParameter("filterCategory3");
		String category3 = req.getParameter("category3");
		String filterCategory4 = req.getParameter("filterCategory4");
		String category4 = req.getParameter("category4");
		String filterCategory5 = req.getParameter("filterCategory5");
		String category5 = req.getParameter("category5");
		
		// 20080611 Jimmy
		String active = req.getParameter("active");
		ilf.active = active;
		
		ilf.setListingOptions((filterCodeRange != null), codeFrom, codeTo, (filterCategory0 != null), new Integer(
				idCategory0), (filterCategory1 != null), category1, (filterCategory2 != null), category2,
				(filterCategory3 != null), category3, (filterCategory4 != null), category4, (filterCategory5 != null),
				category5);
		Vector vecItem = ilf.getListing();
		if (vecItem != null)
		{
			req.setAttribute("vecItem", vecItem);
		}
	}
}
