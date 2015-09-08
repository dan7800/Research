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
package com.vlee.servlet.procurement;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;

import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;
import com.vlee.bean.procurement.*;
import com.vlee.bean.distribution.EditDeliveryTripForm;
import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.bean.inventory.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class DoEditPO implements Action
{
	String strClassName = "DoEditPO";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{		
		String formName = req.getParameter("formName");
		
		if (formName == null)
		{
			return new ActionRouter("procurement-po-edit-page");
		}
		if (formName.equals("selectPO"))
		{
			try
			{
				fnSelectPO(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setBranch"))
		{
			setBranch(servlet, req, res);
			return new ActionRouter("procurement-po-edit-page");
		}
		if (formName.equals("setSupplier"))
		{
			try
			{
				setSupplier(servlet, req, res);
				return new ActionRouter("procurement-po-edit-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-edit-page");
			}
		}
		if (formName.equals("setRefNum"))
		{
			setRefNum(servlet, req, res);
		}
		if (formName.equals("setRemarks"))
		{
			setRemarks(servlet, req, res);
		}
		if (formName.equals("setShipTo"))
		{
			setShipTo(servlet, req, res);
		}
		if (formName.equals("setTermsDate1"))
		{
			
			setTermsDate(servlet, req, res);
			
		}
		if (formName.equals("addStockWithItemCode"))
		{
			try
			{
				fnAddStockWithItemCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-edit-page");
			}
		}
		if (formName.equals("addStockWithESS"))
		{
			try
			{
				fnAddStockWithESS(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-edit-page");
			}
		}
		if (formName.equals("rmDocRow"))
		{
			try
			{
				fnRmDocRow(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-edit-page");
			}
		}
		if (formName.equals("processRow"))
		{
			try
			{
				fnProcessRow(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-edit-page");
			}
		}		
		if (formName.equals("reset"))
		{
			fnReset(servlet, req, res);
			return new ActionRouter("procurement-po-edit-page");
		}
		if (formName.equals("confirmAndSave"))
		{
			try
			{
				fnConfirmAndSave(servlet, req, res);
				req.setAttribute("successNotify","Successfully Saved the PO");
				req.setAttribute("popupPrintableGRN", "true");
				
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("procurement-po-edit-page");
		}
		if (formName.equals("setForeignCurrency"))
		{
			try
			{
				fnSetForeignCurrency(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("procurement-po-edit-page");
	}

	private void fnSelectPO(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{		
		HttpSession session = req.getSession();
		Integer userid = (Integer) session.getAttribute("userId");
		String strPOPkid = req.getParameter("poPkid");
		Long poId = new Long(strPOPkid);
		session.setAttribute("procurement-po-edit-form", null);		
			
		EditPOForm epof = new EditPOForm(poId, userid);		
		epof.loadPO(poId);
		session.setAttribute("procurement-po-edit-form", epof);
		
			// Janet
			// create the PurchaseOrderStockSession so that the po items could be edited
			System.out.println("-------- Putting the items into PurchaseOrderStockSession to allow edit");
			TreeMap tableRows = epof.getTableRows();
			Vector vecRows = new Vector(tableRows.values());
			Integer userId = (Integer) session.getAttribute("userId");
			
			for(int cnt1=0;cnt1<vecRows.size();cnt1++)
			{
				DocRow docRow = (DocRow) vecRows.get(cnt1);
				System.out.println("ItemCode : "+docRow.getItemCode());
				System.out.println("epof.poObj.mCcyPair"+epof.poObj.mCcyPair);
				System.out.println("epof.poObj.mXRate"+epof.poObj.mXRate);
				PurchaseOrderStockSession efif = new PurchaseOrderStockSession(docRow,epof.branch.pkid);
				efif.setGuid(docRow.getGuid());
				efif.setForex(epof.poObj.mCcyPair, epof.poObj.mXRate);
				session.setAttribute("procurement-po-edit" + PurchaseOrderStockSession.OBJNAME, efif);
				session.setAttribute(efif.getKey(),efif);			
				
				System.out.println("docRow.getGuid() : "+docRow.getGuid());
			}	
			// End Janet
								
	}
	
	private void fnSetForeignCurrency(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String foreignCcy = req.getParameter("foreignCcy");
		String xChangeRate = req.getParameter("xrate");
	
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		String docRowKey = req.getParameter("docRowKey");		

		try
		{
			BigDecimal xrate = new BigDecimal(xChangeRate);
			if (xrate.signum() > 0)
			{
				// BigDecimal xrate2 = xrate.divide(new
				// BigDecimal(1),12,BigDecimal.ROUND_HALF_EVEN);
				epof.setForeignCurrencyReverse(foreignCcy, xrate);
				resetItemPrice2(servlet, req, res);
			} else
			{
				throw new Exception("Invalid Exchange Rate!");
			}	
		} catch (Exception ex)
		{
			throw new Exception("Invalid Exchange Rate!");
		}		
		
	}
	
	private void resetItemPrice2(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		
		String foreignCcy = req.getParameter("foreignCcy");
		String xChangeRate = req.getParameter("xrate");
		String docRowKey = req.getParameter("docRowKey");		
		BigDecimal xrate = new BigDecimal(xChangeRate);
		
		TreeMap tableRows = epof.getTableRows();
		Vector vecDocRow = new Vector(tableRows.values());
		for(int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt1);
			PurchaseOrderItemObject poiObj = new PurchaseOrderItemObject(docrow, epof.poObj);
			if(foreignCcy.equals(epof.poObj.mCurrency) && xrate.compareTo(new BigDecimal(0))!=0)
	        {
				System.out.println("Inside resetItemPrice2........");
				 BigDecimal priceLocal = docrow.getPrice2().multiply(xrate);
									
				 System.out.println("setPrice1"+priceLocal.toString());
				    
				docrow.setPrice1(priceLocal);
	        }
			else
			{
				System.out.println("ddddd");
				docrow.setCcy1(epof.branch.currency);
				docrow.setPrice1(poiObj.mUnitPriceQuoted);
				docrow.setCcy2(epof.poObj.mCcyPair);
				docrow.setPrice2(new BigDecimal(CurrencyFormat.strCcy(poiObj.mUnitPriceQuoted.multiply(epof.poObj.mXRate))));
			}
		}
	}
	

	private void setShipTo(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String shipTo = req.getParameter("shipTo");
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		epof.setShipTo(shipTo);
	}
	
	private void setTermsDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		
			Timestamp terms = TimeFormat.createTimestamp("tDate");
			epof.setTermsDate(terms);
	
	}
	
	// /////////////////////////////////////////////////////////////////
	private void setRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String remarks = req.getParameter("remarks");
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		epof.setRemarks(remarks);
	}

	// /////////////////////////////////////////////////////////////////
	private void setBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		epof.setBranch(new Integer(req.getParameter("branchId")));
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnReset(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		epof.reset();
		session.setAttribute("procurement-po-edit-form", null);
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnConfirmAndSave(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		try
		{
			epof.confirmAndSave();			
						
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
		session.setAttribute("procurement-po-edit-form",null);
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnRmDocRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		String altNumbering = req.getParameter("altNumbering");	
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		
		System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		System.out.println("docRowKey : "+docRowKey);
		
		epof.dropDocRow(docRowKey,altNumbering);
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnProcessRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");	
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
				
		epof.dropDocRow(docRowKey);
	}
	
	// /////////////////////////////////////////////////////////////////
	protected void fnAddStockWithItemCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer itemId = new Integer(req.getParameter("itemId"));
		BigDecimal unitPrice = new BigDecimal(req.getParameter("unitPrice"));
		BigDecimal qty = null;
		try{ qty = new BigDecimal(req.getParameter("qty")); } catch (Exception ex) { ex.printStackTrace();}

		String[] serial = req.getParameterValues("sn");
		ItemObject itmObj = ItemNut.getObject(itemId);
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		BigDecimal priceLocal = new BigDecimal(0);
		BigDecimal priceForeign = new BigDecimal(0);
		
		
		String xChangeRate = req.getParameter("xrate");
		BigDecimal xrate = new BigDecimal(xChangeRate);
		
				
		
		if (epof.usingForeignCurrency())
		{
			//priceForeign = unitPrice;
			priceForeign = new BigDecimal(CurrencyFormat.strCcy(unitPrice.multiply(xrate)));
			
			priceLocal = priceForeign.divide(priceForeign, 12, BigDecimal.ROUND_HALF_EVEN);
		} else
		{
			priceLocal = unitPrice;
			priceForeign = new BigDecimal(0);
		}
		
		BranchObject branch = epof.branch;
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
	
		try
		{
			docrow = new DocRow();
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(qty);			
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(priceLocal);
			if (epof.usingForeignCurrency())
			{
				docrow.setCcy2(epof.getForeignCurrency());
				docrow.setPrice2(priceForeign);
			}
			if (serial != null)
			{
				for (int cnt = 0; cnt < serial.length; cnt++)
				{
					docrow.addSerial(serial[cnt]);
				}
			}
			
			epof.fnAddStockWithItemCode(docrow);
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void setRefNum(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String refNum = req.getParameter("refNum");
		HttpSession session = req.getSession();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		epof.setReferenceNo(refNum);
	}

	public void setSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			HttpSession session = req.getSession();
			EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
			String strSuppAcc = (String) req.getParameter("suppPkid");
			Integer iSuppAcc = new Integer(strSuppAcc);
			SuppAccountObject suppAccObj = SuppAccountNut.getObject(iSuppAcc);
			
			if (suppAccObj == null)
			{
				throw new Exception(" No such supplier!! ");
			}
			if (!epof.setSupplier(iSuppAcc))
			{
				throw new Exception("Invalid Supplier Account");
			}
			
		} catch (Exception ex)
		{
			throw new Exception("Invalid Supplier PKID: " + ex.getMessage());
		}
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnAddStockWithESS(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String essGuid = req.getParameter("essGuid");
		
		Log.printVerbose("essGuid = " + essGuid);
		
		PurchaseOrderStockSession ess = (PurchaseOrderStockSession) session.getAttribute(essGuid);
		ItemObject itmObj = ess.getItemObject();
		BranchObject branch = ess.getBranch();
		EditPOForm epof = (EditPOForm) session.getAttribute("procurement-po-edit-form");
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		
		Log.printVerbose("checkpoint 1..........................");
		// / check if template id exists, if it doesn't,create a new ones..
		try
		{
			Log.printVerbose("checkpoint 5..........................");
			docrow = new DocRow();
			docrow.setGuid(ess.getGuid()); 
//			docrow.setTemplateId(piObj.pkid.intValue());
//			docrow.setItemType(itemType);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(ess.getQty());
			docrow.setCcy1(branch.currency);
			docrow.setPrice1Forex(ess.getPrice1());
			docrow.user1 = ess.getPic1().intValue();
			docrow.setRemarks(ess.getRemarks());
			docrow.setCcy2(ess.getCcy2());
			docrow.setPrice2(ess.getPrice2());
			docrow.setReceivingRemarks(ess.getReceivingRemarks());
			docrow.setReceivingPrice(ess.getReceivingPrice());
			docrow.setReceivingQty(ess.getReceivingQty());
			docrow.setReceivingStatus(ess.getReceivingStatus());
			Vector vecSN = ess.getSerialNumbers();
			if (vecSN != null)
			{
				for (int cnt = 0; cnt < vecSN.size(); cnt++)
				{
					String sn = (String) vecSN.get(cnt);
					docrow.addSerial(sn);
					Log.printVerbose("checkpoint 6..........................");
				}
			}
			Log.printVerbose("checkp,loint 7..........................");
			epof.fnAddStockWithItemCode(docrow);
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//session.setAttribute(essGuid, null);
	}
}
