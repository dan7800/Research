/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/

package com.vlee.servlet.footwear;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.ejb.inventory.*;
import com.vlee.util.*;

public class DoReportStockAdjustmentByMarkdown implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		fnPreserveParam(servlet,req,res);
		if(formName==null)
		{
			return new ActionRouter("footwear-report-stock-adjustment-by-markdown-page");
		}

		if (formName.equals("popupPrint"))
		{
			fnGetReport(servlet,req,res);
			return new ActionRouter("footwear-report-stock-adjustment-by-markdown-printable-page");
		}
		
		if(formName.equals("popupPrintClosingReport"))
		{
			fnGetReport(servlet,req,res);
			return new ActionRouter("footwear-report-stock-adjustment-by-markdown-printable-closing-report-page");
		}
		
		if(formName.equals("getReport"))
		{
			fnGetReport(servlet,req,res);
		}
		System.out.println("---------------------CHECKPOINT0");
		
		
		return new ActionRouter("footwear-report-stock-adjustment-by-markdown-page");
		
		
		
		
		
	}

	private void fnPreserveParam(HttpServlet servlet,
											HttpServletRequest req, HttpServletResponse res)
	{
		req.setAttribute("locationId",req.getParameter("locationId"));
		req.setAttribute("itemCodeFrom",req.getParameter("itemCodeFrom"));
		req.setAttribute("itemCodeTo",req.getParameter("itemCodeTo"));
		req.setAttribute("dateFrom",req.getParameter("dateFrom"));
		req.setAttribute("dateTo",req.getParameter("dateTo"));
		req.setAttribute("sortBy",req.getParameter("sortBy"));
		req.setAttribute("mergeLocation",req.getParameter("mergeLocation"));

	}


	private void fnGetReport(HttpServlet servlet, HttpServletRequest req,
										HttpServletResponse res)
	{
		String locationId = req.getParameter("locationId");
		String itemCodeFrom = req.getParameter("itemCodeFrom");
		String itemCodeTo = req.getParameter("itemCodeTo");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String sortBy = req.getParameter("sortBy");
		String mergeLocation = req.getParameter("mergeLocation");
		
		System.out.println("---------------------------CHECKPOINT1" +locationId);

		Integer iLocationId = null;
		try{ iLocationId = new Integer(locationId);}catch(Exception ex){}
		
		System.out.println("---------------------------CHECKPOINT2" +locationId);
		
		Timestamp tsToNext = TimeFormat.createTimestamp(dateTo);
		tsToNext = TimeFormat.add(tsToNext,0,0,1);
		String dateToNext = TimeFormat.strDisplayDate(tsToNext);

		System.out.println("---------------------------CHECKPOINT3" +tsToNext);
		
		String condition = StockAdjustmentBean.LASTUPDATE +" < '"+dateToNext+"' ";
		condition += " AND "+StockAdjustmentBean.LASTUPDATE +" >= '"+dateFrom+"' ";

		condition += " AND "+StockAdjustmentBean.TX_TYPE +" = '"+StockAdjustmentBean.TYPE_MARKDOWN+"' ";

		if(itemCodeFrom!=null && itemCodeFrom.trim().length()>0)
		{ condition += " AND "+StockAdjustmentBean.SRC_ITEM_CODE+" >= '"+itemCodeFrom+"' "; }	
		if(itemCodeTo !=null && itemCodeTo.trim().length()>0)
		{ condition += " AND "+StockAdjustmentBean.SRC_ITEM_CODE+" <= '"+itemCodeTo+"' "; }	
		if(iLocationId!=null && iLocationId.intValue()>0)
		{ condition += " AND "+StockAdjustmentBean.SRC_LOCATION+" = '"+iLocationId.toString()+"' "; }

		QueryObject query = new QueryObject(new String[]{ condition });
		query.setOrder(" ORDER BY "+sortBy+" , "+StockAdjustmentBean.PKID);

		System.out.println("---------------------------CHECKPOINT4" +StockAdjustmentBean.PKID);
		System.out.println("---------------------------CHECKPOINT4" +query);
		
		Vector vecResult = new Vector(StockAdjustmentNut.getObjects(query));

		if(mergeLocation!=null && mergeLocation.equals("true"))
		{
			TreeMap treeResult = new TreeMap();
			for(int cnt1=0;cnt1<vecResult.size();cnt1++)
			{
				StockAdjustmentObject stkAdjObj = (StockAdjustmentObject) vecResult.get(cnt1);
				String key = stkAdjObj.src_item_code+stkAdjObj.userid1.toString()+stkAdjObj.src_price1.toString()+
										stkAdjObj.tgt_price1.toString();
				StockAdjustmentObject bufObj = (StockAdjustmentObject) treeResult.get(key);

				if(bufObj==null)
				{
					treeResult.put(key,stkAdjObj);
				}
				else
				{
					bufObj.src_qty1 = bufObj.src_qty1.add(stkAdjObj.src_qty1);
					bufObj.tgt_qty1 = bufObj.tgt_qty1.add(stkAdjObj.tgt_qty1);
					bufObj.src_location = new Integer(0);
					bufObj.tgt_location = new Integer(0);
				}
			}
			vecResult = new Vector(treeResult.values());
		}
		System.out.println("---------------------------CHECKPOINT4" +vecResult);
		req.setAttribute("vecResult", vecResult);

	}

}
