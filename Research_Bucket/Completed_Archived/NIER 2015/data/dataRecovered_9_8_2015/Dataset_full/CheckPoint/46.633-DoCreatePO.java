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
import com.vlee.bean.pos.CreateInvoiceSession;
import com.vlee.bean.procurement.*;
import com.vlee.bean.inventory.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class DoCreatePO implements Action
{
	String strClassName = "DoCreatePO";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		HttpSession session = req.getSession();
		CreatePOForm recStkSes = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		String formName = req.getParameter("formName");
		Log.printVerbose("@@@%%%PROCUREMENT FORM NAME:" + formName);
		if (formName == null)
		{
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("mapPInvToPO"))
		{
			try
			{
				fnMapPInvToPO(servlet, req, res);
				return new ActionRouter("procurement-po-create-page");
				
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
			}
		}
		if (formName.equals("mapSReqToStkTransfer"))
		{
			try
			{
				fnMapSReqToPO(servlet, req, res);
				return new ActionRouter("procurement-po-create-page");
				
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
			}
		}
		if (formName.equals("setBranch"))
		{
			setBranch(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("setSupplier"))
		{
			try
			{
				setSupplier(servlet, req, res);
				return new ActionRouter("procurement-po-create-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
		if (formName.equals("setDate"))
		{
			setDate(servlet, req, res);
		}
		if (formName.equals("setShipTo"))
		{
			setShipTo(servlet, req, res);
		}
		if (formName.equals("setTermsDate"))
		{
			try
			{
				setTermsDate(servlet, req, res);
			}catch(Exception ex)
			{}
		
		}
		if (formName.equals("addStockWithItemCode"))
		{
			try
			{
				fnAddStockWithItemCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
			}
		}
		if (formName.equals("reset"))
		{
			fnReset(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("confirmAndSave"))
		{
			try
			{
				fnConfirmAndSave(servlet, req, res);
				req.setAttribute("popupPrintableGRN", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("procurement-po-create-page");
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
		return new ActionRouter("procurement-po-create-page");
	}

	private void fnSetForeignCurrency(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String foreignCcy = req.getParameter("foreignCcy");
		String xChangeRate = req.getParameter("xrate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			BigDecimal xrate = new BigDecimal(xChangeRate);
			if (xrate.signum() > 0)
			{
				// BigDecimal xrate2 = xrate.divide(new
				// BigDecimal(1),12,BigDecimal.ROUND_HALF_EVEN);
				trss.setForeignCurrencyReverse(foreignCcy, xrate);
			} else
			{
				throw new Exception("Invalid Exchange Rate!");
			}
		} catch (Exception ex)
		{
			throw new Exception("Invalid Exchange Rate!");
		}
	}

	// /////////////////////////////////////////////////////////////////
	private void setRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String remarks = req.getParameter("remarks");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setRemarks(remarks);
	}

	// /////////////////////////////////////////////////////////////////
	private void setDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String txDate = req.getParameter("txDate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		if (txDate == null)
		{
			return;
		}
		Timestamp tsDate = TimeFormat.createTimestamp(txDate);
		trss.setDate(tsDate);
	}
	   
	private void setShipTo(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String shipTo = req.getParameter("shipTo");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setShipTo(shipTo);
	}
	
	private void setTermsDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)throws Exception
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			Integer terms = new Integer(req.getParameter("tDate"));
			trss.setTermsDate(terms);
		} catch (Exception ex)
		{
			throw new Exception("Error setting terms!");
		}
	
	
	
	}

	// /////////////////////////////////////////////////////////////////
	private void setBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setBranch(new Integer(req.getParameter("branchId")));
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnReset(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.reset();
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnConfirmAndSave(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{			
			String linkFromSReq = trss.getLinkFromSReq();			
			System.out.println("linkFromSReq : "+linkFromSReq);
			
			String linkFromProInv = trss.getLinkFromProInv();			
			System.out.println("linkFromProInv : "+linkFromProInv);
			
			if("true".equals(linkFromSReq) && !"true".equals(linkFromProInv))
			{
				System.out.println("Link from Stock Requisition");
								
				Long sReqId = trss.getSReqId();
				StockRequisitionObject stockRequisition = StockRequisitionNut.getObject(sReqId);
				
				System.out.println("Ordered Qty : "+trss.getTotalQty().toString());
				System.out.println("OutStanding Qty : "+stockRequisition.qty_outstanding.toString());
				
				if(trss.getTotalQty().compareTo(stockRequisition.qty_outstanding) == 1)
				{
					throw new Exception("The transfer quantity is more than the out standing quantity of the stock requisition!");
				}
				else
				{
					trss.confirmAndSave();		
				}
			}
			else
			{
				trss.confirmAndSave();
			}
			
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnRmDocRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.dropDocRow(docRowKey);
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
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		BigDecimal priceLocal = new BigDecimal(0);
		BigDecimal priceForeign = new BigDecimal(0);
		if (trss.usingForeignCurrency())
		{
			priceForeign = unitPrice;
			priceLocal = priceForeign.divide(priceForeign, 12, BigDecimal.ROUND_HALF_EVEN);
		} else
		{
			priceLocal = unitPrice;
			priceForeign = new BigDecimal(0);
		}
		BranchObject branch = trss.getBranch();
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		// / check if template id exists, if it doesn't,create a new ones..
		try
		{
			docrow = new DocRow();
//			docrow.setTemplateId(piObj.pkid.intValue());
//			docrow.setItemType(PurchaseItemBean.TYPE_INV);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(qty);
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(priceLocal);
			if (trss.usingForeignCurrency())
			{
				docrow.setCcy2(trss.getForeignCurrency());
				docrow.setPrice2(priceForeign);
			}
			if (serial != null)
			{
				for (int cnt = 0; cnt < serial.length; cnt++)
				{
					docrow.addSerial(serial[cnt]);
				}
			}
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void setRefNum(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String refNum = req.getParameter("refNum");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setReferenceNo(refNum);
	}

	public void setSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			HttpSession session = req.getSession();
			CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
			String strSuppAcc = (String) req.getParameter("suppPkid");
			Integer iSuppAcc = new Integer(strSuppAcc);
			SuppAccountObject suppAccObj = SuppAccountNut.getObject(iSuppAcc);
			if (suppAccObj == null)
			{
				throw new Exception(" No such supplier!! ");
			}
			if (!trss.setSupplier(iSuppAcc))
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
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		Log.printVerbose("TUPP: DoCreatePO::fnAddStockESS() checkpoint 1..........................");
		// / check if template id exists, if it doesn't,create a new ones..
		try
		{
			Log.printVerbose("TUPP: DoCreatePO::fnAddStockESS()checkpoint 5..........................");
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
			docrow.setPrice1(ess.getPrice1());
			docrow.user1 = ess.getPic1().intValue();
			docrow.setRemarks(ess.getRemarks());
			docrow.setCcy2(ess.getCcy2());
			docrow.setPrice2(ess.getPrice2());
			Vector vecSN = ess.getSerialNumbers();
			if (vecSN != null)
			{
				for (int cnt = 0; cnt < vecSN.size(); cnt++)
				{
					String sn = (String) vecSN.get(cnt);
					docrow.addSerial(sn);
					Log.printVerbose("TUPP: DoCreatePO::fnAddStockESS() checkpoint 6..........................");
				}
			}
			Log.printVerbose("TUPP: DoCreatePO::fnAddStockESS() checkpoint 7..........................");
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//joe session.setAttribute(essGuid, null);
	}
	
	 /////////////////////////////////////////////////////////////////
	protected void fnMapPInvToPO(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		
		if(trss == null)
		{
			Integer iUser = (Integer) session.getAttribute("userId");
			trss = new CreatePOForm(iUser);
			session.setAttribute("procurement-po-create-form",trss);
		}
		
		String cpiiPkid = req.getParameter("cpiiPkid");
		
		System.out.println("cpiiPkid : "+cpiiPkid);			
		
		try
		{
			CustProformaInvoiceIndexObject cpiiObj = CustProformaInvoiceIndexNut.getObject(new Long(cpiiPkid));

			trss.setBranch(cpiiObj.branch);
			trss.setReferenceNo(cpiiPkid);
			trss.setRemarks("AUTO-CREATED FROM PROFO INV"+cpiiPkid);
			trss.setCpiiPkid(cpiiObj.pkid);
			trss.setLinkFromProInv("true");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	 /////////////////////////////////////////////////////////////////
	protected void fnMapSReqToPO(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		
		if(trss == null)
		{
			Integer iUser = (Integer) session.getAttribute("userId");
			trss = new CreatePOForm(iUser);
			session.setAttribute("procurement-po-create-form",trss);
		}
		
		String sReqId = req.getParameter("sReqId");
		String branchFromId = req.getParameter("branchFromId");
		
		System.out.println("Stock Requisition ID : "+sReqId);			
		
		try
		{
			StockRequisitionObject stockRequisition = StockRequisitionNut.getObject(new Long(sReqId));

			trss.setBranch(stockRequisition.branch_id);
			trss.setReferenceNo(sReqId);
			trss.setRemarks("AUTO-CREATED FROM SREQ"+sReqId);
			trss.setSReqId(stockRequisition.pkid);
			trss.setLinkFromSReq("true");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
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
import com.vlee.bean.inventory.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class DoCreatePO implements Action
{
	String strClassName = "DoCreatePO";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		HttpSession session = req.getSession();
		CreatePOForm recStkSes = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("setBranch"))
		{
			setBranch(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("setSupplier"))
		{
			try
			{
				setSupplier(servlet, req, res);
				return new ActionRouter("procurement-po-create-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
		if (formName.equals("setDate"))
		{
			setDate(servlet, req, res);
		}
		if (formName.equals("addStockWithItemCode"))
		{
			try
			{
				fnAddStockWithItemCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
			}
		}
		if (formName.equals("reset"))
		{
			fnReset(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("confirmAndSave"))
		{
			try
			{
				fnConfirmAndSave(servlet, req, res);
				req.setAttribute("popupPrintableGRN", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("procurement-po-create-page");
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
		return new ActionRouter("procurement-po-create-page");
	}

	private void fnSetForeignCurrency(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String foreignCcy = req.getParameter("foreignCcy");
		String xChangeRate = req.getParameter("xrate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			BigDecimal xrate = new BigDecimal(xChangeRate);
			if (xrate.signum() > 0)
			{
				// BigDecimal xrate2 = xrate.divide(new
				// BigDecimal(1),12,BigDecimal.ROUND_HALF_EVEN);
				trss.setForeignCurrencyReverse(foreignCcy, xrate);
			} else
			{
				throw new Exception("Invalid Exchange Rate!");
			}
		} catch (Exception ex)
		{
			throw new Exception("Invalid Exchange Rate!");
		}
	}

	// /////////////////////////////////////////////////////////////////
	private void setRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String remarks = req.getParameter("remarks");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setRemarks(remarks);
	}

	// /////////////////////////////////////////////////////////////////
	private void setDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String txDate = req.getParameter("txDate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		if (txDate == null)
		{
			return;
		}
		Timestamp tsDate = TimeFormat.createTimestamp(txDate);
		trss.setDate(tsDate);
	}

	// /////////////////////////////////////////////////////////////////
	private void setBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setBranch(new Integer(req.getParameter("branchId")));
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnReset(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.reset();
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnConfirmAndSave(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			trss.confirmAndSave();
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnRmDocRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.dropDocRow(docRowKey);
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
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		BigDecimal priceLocal = new BigDecimal(0);
		BigDecimal priceForeign = new BigDecimal(0);
		if (trss.usingForeignCurrency())
		{
			priceForeign = unitPrice;
			priceLocal = priceForeign.divide(priceForeign, 12, BigDecimal.ROUND_HALF_EVEN);
		} else
		{
			priceLocal = unitPrice;
			priceForeign = new BigDecimal(0);
		}
		BranchObject branch = trss.getBranch();
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		// / check if template id exists, if it doesn't,create a new ones..
		PurchaseItem piEJB = PurchaseItemNut.getPurchaseInvItem(itmObj.pkid, PurchaseItemBean.TYPE_INV);
		PurchaseItemObject piObj = null;
		if (piEJB == null)
		{
			piObj = new PurchaseItemObject();
			piObj.itemFKId = itmObj.pkid;
			piObj.itemType = PurchaseItemBean.TYPE_INV;
			piObj.currency = branch.currency;
			piObj.unitPriceStd = unitPrice;
			piObj.unitPriceDiscounted = unitPrice;
			piObj.unitPriceMin = unitPrice;
			piObj.userIdUpdate = userId;
			piEJB = PurchaseItemNut.fnCreate(piObj);
		} else
		{
			try
			{
				piObj = piEJB.getObject();
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		try
		{
			docrow = new DocRow();
			docrow.setTemplateId(piObj.pkid.intValue());
			docrow.setItemType(PurchaseItemBean.TYPE_INV);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(qty);
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(priceLocal);
			if (trss.usingForeignCurrency())
			{
				docrow.setCcy2(trss.getForeignCurrency());
				docrow.setPrice2(priceForeign);
			}
			if (serial != null)
			{
				for (int cnt = 0; cnt < serial.length; cnt++)
				{
					docrow.addSerial(serial[cnt]);
				}
			}
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void setRefNum(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String refNum = req.getParameter("refNum");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setReferenceNo(refNum);
	}

	public void setSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			HttpSession session = req.getSession();
			CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
			String strSuppAcc = (String) req.getParameter("suppPkid");
			Integer iSuppAcc = new Integer(strSuppAcc);
			SuppAccountObject suppAccObj = SuppAccountNut.getObject(iSuppAcc);
			if (suppAccObj == null)
			{
				throw new Exception(" No such supplier!! ");
			}
			if (!trss.setSupplier(iSuppAcc))
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
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		Log.printVerbose("checkpoint 1..........................");
		// / check if template id exists, if it doesn't,create a new ones..
		String itemType = POSItemBean.TYPE_INV;
		POSItemObject piObj = null;
		if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_INVENTORY)))
		{
			Log.printVerbose("checkpoint 2..........................");
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_INV);
			itemType = POSItemBean.TYPE_INV;
		} else if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_NONSTK)))
		{
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_NSTK);
			itemType = POSItemBean.TYPE_NSTK;
		} else if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_TRADEIN)))
		{
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_TRADEIN_SELL);
			itemType = POSItemBean.TYPE_TRADEIN_SELL;
		}
		Log.printVerbose("checkpoint 3..........................");
		if (piObj == null)
		{
			Log.printVerbose("checkpoint 4..........................");
			piObj = new POSItemObject();
			piObj.itemFKId = itmObj.pkid;
			piObj.itemType = itemType;
			piObj.currency = branch.currency;
			piObj.unitPriceStd = ess.getUnitPrice();
			piObj.unitPriceDiscounted = ess.getUnitPrice();
			piObj.unitPriceMin = ess.getUnitPrice();
			piObj.userIdUpdate = userId;
			POSItem piEJB = POSItemNut.fnCreate(piObj);
		}
		try
		{
			Log.printVerbose("checkpoint 5..........................");
			docrow = new DocRow();
			docrow.setTemplateId(piObj.pkid.intValue());
			docrow.setItemType(itemType);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(ess.getQty());
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(ess.getPrice1());
			docrow.user1 = ess.getPic1().intValue();
			docrow.setRemarks(ess.getRemarks());
			docrow.setCcy2(ess.getCcy2());
			docrow.setPrice2(ess.getPrice2());
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
			Log.printVerbose("checkpoint 7..........................");
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		session.setAttribute(essGuid, null);
	}
}
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
import com.vlee.bean.inventory.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class DoCreatePO implements Action
{
	String strClassName = "DoCreatePO";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		HttpSession session = req.getSession();
		CreatePOForm recStkSes = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("setBranch"))
		{
			setBranch(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("setSupplier"))
		{
			try
			{
				setSupplier(servlet, req, res);
				return new ActionRouter("procurement-po-create-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
		if (formName.equals("setDate"))
		{
			setDate(servlet, req, res);
		}
		if (formName.equals("addStockWithItemCode"))
		{
			try
			{
				fnAddStockWithItemCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
			}
		}
		if (formName.equals("reset"))
		{
			fnReset(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("confirmAndSave"))
		{
			try
			{
				fnConfirmAndSave(servlet, req, res);
				req.setAttribute("popupPrintableGRN", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("procurement-po-create-page");
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
		return new ActionRouter("procurement-po-create-page");
	}

	private void fnSetForeignCurrency(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String foreignCcy = req.getParameter("foreignCcy");
		String xChangeRate = req.getParameter("xrate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			BigDecimal xrate = new BigDecimal(xChangeRate);
			if (xrate.signum() > 0)
			{
				// BigDecimal xrate2 = xrate.divide(new
				// BigDecimal(1),12,BigDecimal.ROUND_HALF_EVEN);
				trss.setForeignCurrencyReverse(foreignCcy, xrate);
			} else
			{
				throw new Exception("Invalid Exchange Rate!");
			}
		} catch (Exception ex)
		{
			throw new Exception("Invalid Exchange Rate!");
		}
	}

	// /////////////////////////////////////////////////////////////////
	private void setRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String remarks = req.getParameter("remarks");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setRemarks(remarks);
	}

	// /////////////////////////////////////////////////////////////////
	private void setDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String txDate = req.getParameter("txDate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		if (txDate == null)
		{
			return;
		}
		Timestamp tsDate = TimeFormat.createTimestamp(txDate);
		trss.setDate(tsDate);
	}

	// /////////////////////////////////////////////////////////////////
	private void setBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setBranch(new Integer(req.getParameter("branchId")));
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnReset(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.reset();
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnConfirmAndSave(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			trss.confirmAndSave();
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnRmDocRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.dropDocRow(docRowKey);
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
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		BigDecimal priceLocal = new BigDecimal(0);
		BigDecimal priceForeign = new BigDecimal(0);
		if (trss.usingForeignCurrency())
		{
			priceForeign = unitPrice;
			priceLocal = priceForeign.divide(priceForeign, 12, BigDecimal.ROUND_HALF_EVEN);
		} else
		{
			priceLocal = unitPrice;
			priceForeign = new BigDecimal(0);
		}
		BranchObject branch = trss.getBranch();
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		// / check if template id exists, if it doesn't,create a new ones..
		PurchaseItem piEJB = PurchaseItemNut.getPurchaseInvItem(itmObj.pkid, PurchaseItemBean.TYPE_INV);
		PurchaseItemObject piObj = null;
		if (piEJB == null)
		{
			piObj = new PurchaseItemObject();
			piObj.itemFKId = itmObj.pkid;
			piObj.itemType = PurchaseItemBean.TYPE_INV;
			piObj.currency = branch.currency;
			piObj.unitPriceStd = unitPrice;
			piObj.unitPriceDiscounted = unitPrice;
			piObj.unitPriceMin = unitPrice;
			piObj.userIdUpdate = userId;
			piEJB = PurchaseItemNut.fnCreate(piObj);
		} else
		{
			try
			{
				piObj = piEJB.getObject();
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		try
		{
			docrow = new DocRow();
			docrow.setTemplateId(piObj.pkid.intValue());
			docrow.setItemType(PurchaseItemBean.TYPE_INV);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(qty);
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(priceLocal);
			if (trss.usingForeignCurrency())
			{
				docrow.setCcy2(trss.getForeignCurrency());
				docrow.setPrice2(priceForeign);
			}
			if (serial != null)
			{
				for (int cnt = 0; cnt < serial.length; cnt++)
				{
					docrow.addSerial(serial[cnt]);
				}
			}
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void setRefNum(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String refNum = req.getParameter("refNum");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setReferenceNo(refNum);
	}

	public void setSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			HttpSession session = req.getSession();
			CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
			String strSuppAcc = (String) req.getParameter("suppPkid");
			Integer iSuppAcc = new Integer(strSuppAcc);
			SuppAccountObject suppAccObj = SuppAccountNut.getObject(iSuppAcc);
			if (suppAccObj == null)
			{
				throw new Exception(" No such supplier!! ");
			}
			if (!trss.setSupplier(iSuppAcc))
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
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		Log.printVerbose("checkpoint 1..........................");
		// / check if template id exists, if it doesn't,create a new ones..
		String itemType = POSItemBean.TYPE_INV;
		POSItemObject piObj = null;
		if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_INVENTORY)))
		{
			Log.printVerbose("checkpoint 2..........................");
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_INV);
			itemType = POSItemBean.TYPE_INV;
		} else if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_NONSTK)))
		{
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_NSTK);
			itemType = POSItemBean.TYPE_NSTK;
		} else if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_TRADEIN)))
		{
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_TRADEIN_SELL);
			itemType = POSItemBean.TYPE_TRADEIN_SELL;
		}
		Log.printVerbose("checkpoint 3..........................");
		if (piObj == null)
		{
			Log.printVerbose("checkpoint 4..........................");
			piObj = new POSItemObject();
			piObj.itemFKId = itmObj.pkid;
			piObj.itemType = itemType;
			piObj.currency = branch.currency;
			piObj.unitPriceStd = ess.getUnitPrice();
			piObj.unitPriceDiscounted = ess.getUnitPrice();
			piObj.unitPriceMin = ess.getUnitPrice();
			piObj.userIdUpdate = userId;
			POSItem piEJB = POSItemNut.fnCreate(piObj);
		}
		try
		{
			Log.printVerbose("checkpoint 5..........................");
			docrow = new DocRow();
			docrow.setTemplateId(piObj.pkid.intValue());
			docrow.setItemType(itemType);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(ess.getQty());
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(ess.getPrice1());
			docrow.user1 = ess.getPic1().intValue();
			docrow.setRemarks(ess.getRemarks());
			docrow.setCcy2(ess.getCcy2());
			docrow.setPrice2(ess.getPrice2());
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
			Log.printVerbose("checkpoint 7..........................");
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		session.setAttribute(essGuid, null);
	}
}
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
import com.vlee.bean.inventory.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class DoCreatePO implements Action
{
	String strClassName = "DoCreatePO";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		HttpSession session = req.getSession();
		CreatePOForm recStkSes = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("setBranch"))
		{
			setBranch(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("setSupplier"))
		{
			try
			{
				setSupplier(servlet, req, res);
				return new ActionRouter("procurement-po-create-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
		if (formName.equals("setDate"))
		{
			setDate(servlet, req, res);
		}
		if (formName.equals("addStockWithItemCode"))
		{
			try
			{
				fnAddStockWithItemCode(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
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
				return new ActionRouter("procurement-po-create-page");
			}
		}
		if (formName.equals("reset"))
		{
			fnReset(servlet, req, res);
			return new ActionRouter("procurement-po-create-page");
		}
		if (formName.equals("confirmAndSave"))
		{
			try
			{
				fnConfirmAndSave(servlet, req, res);
				req.setAttribute("popupPrintableGRN", "true");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("procurement-po-create-page");
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
		return new ActionRouter("procurement-po-create-page");
	}

	private void fnSetForeignCurrency(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String foreignCcy = req.getParameter("foreignCcy");
		String xChangeRate = req.getParameter("xrate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			BigDecimal xrate = new BigDecimal(xChangeRate);
			if (xrate.signum() > 0)
			{
				// BigDecimal xrate2 = xrate.divide(new
				// BigDecimal(1),12,BigDecimal.ROUND_HALF_EVEN);
				trss.setForeignCurrencyReverse(foreignCcy, xrate);
			} else
			{
				throw new Exception("Invalid Exchange Rate!");
			}
		} catch (Exception ex)
		{
			throw new Exception("Invalid Exchange Rate!");
		}
	}

	// /////////////////////////////////////////////////////////////////
	private void setRemarks(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String remarks = req.getParameter("remarks");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setRemarks(remarks);
	}

	// /////////////////////////////////////////////////////////////////
	private void setDate(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String txDate = req.getParameter("txDate");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		if (txDate == null)
		{
			return;
		}
		Timestamp tsDate = TimeFormat.createTimestamp(txDate);
		trss.setDate(tsDate);
	}

	// /////////////////////////////////////////////////////////////////
	private void setBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setBranch(new Integer(req.getParameter("branchId")));
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnReset(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.reset();
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnConfirmAndSave(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		try
		{
			trss.confirmAndSave();
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnRmDocRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String docRowKey = req.getParameter("docRowKey");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.dropDocRow(docRowKey);
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnAddStockWithItemCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer itemId = new Integer(req.getParameter("itemId"));
		BigDecimal unitPrice = new BigDecimal(req.getParameter("unitPrice"));
		BigDecimal qty = null;
		try
		{
			qty = new BigDecimal(req.getParameter("qty"));
		} catch (Exception ex)
		{
		}
		String[] serial = req.getParameterValues("sn");
		ItemObject itmObj = ItemNut.getObject(itemId);
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		BigDecimal priceLocal = new BigDecimal(0);
		BigDecimal priceForeign = new BigDecimal(0);
		if (trss.usingForeignCurrency())
		{
			priceForeign = unitPrice;
			priceLocal = priceForeign.divide(priceForeign, 12, BigDecimal.ROUND_HALF_EVEN);
		} else
		{
			priceLocal = unitPrice;
			priceForeign = new BigDecimal(0);
		}
		BranchObject branch = trss.getBranch();
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		// / check if template id exists, if it doesn't,create a new ones..
		PurchaseItem piEJB = PurchaseItemNut.getPurchaseInvItem(itmObj.pkid, PurchaseItemBean.TYPE_INV);
		PurchaseItemObject piObj = null;
		if (piEJB == null)
		{
			piObj = new PurchaseItemObject();
			piObj.itemFKId = itmObj.pkid;
			piObj.itemType = PurchaseItemBean.TYPE_INV;
			piObj.currency = branch.currency;
			piObj.unitPriceStd = unitPrice;
			piObj.unitPriceDiscounted = unitPrice;
			piObj.unitPriceMin = unitPrice;
			piObj.userIdUpdate = userId;
			piEJB = PurchaseItemNut.fnCreate(piObj);
		} else
		{
			try
			{
				piObj = piEJB.getObject();
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		try
		{
			docrow = new DocRow();
			docrow.setTemplateId(piObj.pkid.intValue());
			docrow.setItemType(PurchaseItemBean.TYPE_INV);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(qty);
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(priceLocal);
			if (trss.usingForeignCurrency())
			{
				docrow.setCcy2(trss.getForeignCurrency());
				docrow.setPrice2(priceForeign);
			}
			if (serial != null)
			{
				for (int cnt = 0; cnt < serial.length; cnt++)
				{
					docrow.addSerial(serial[cnt]);
				}
			}
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void setRefNum(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String refNum = req.getParameter("refNum");
		HttpSession session = req.getSession();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		trss.setReferenceNo(refNum);
	}

	public void setSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			HttpSession session = req.getSession();
			CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
			String strSuppAcc = (String) req.getParameter("suppPkid");
			Integer iSuppAcc = new Integer(strSuppAcc);
			SuppAccountObject suppAccObj = SuppAccountNut.getObject(iSuppAcc);
			if (suppAccObj == null)
			{
				throw new Exception(" No such supplier!! ");
			}
			if (!trss.setSupplier(iSuppAcc))
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
		EnterStockSession ess = (EnterStockSession) session.getAttribute(essGuid);
		ItemObject itmObj = ess.getItemObject();
		BranchObject branch = ess.getBranch();
		CreatePOForm trss = (CreatePOForm) session.getAttribute("procurement-po-create-form");
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		Log.printVerbose("checkpoint 1..........................");
		// / check if template id exists, if it doesn't,create a new ones..
		String itemType = POSItemBean.TYPE_INV;
		POSItemObject piObj = null;
		if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_INVENTORY)))
		{
			Log.printVerbose("checkpoint 2..........................");
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_INV);
			itemType = POSItemBean.TYPE_INV;
		} else if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_NONSTK)))
		{
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_NSTK);
			itemType = POSItemBean.TYPE_NSTK;
		} else if (itmObj.enumInvType.equals(new Integer(ItemBean.INV_TYPE_TRADEIN)))
		{
			piObj = POSItemNut.getObject(itmObj.pkid, POSItemBean.TYPE_TRADEIN_SELL);
			itemType = POSItemBean.TYPE_TRADEIN_SELL;
		}
		Log.printVerbose("checkpoint 3..........................");
		if (piObj == null)
		{
			Log.printVerbose("checkpoint 4..........................");
			piObj = new POSItemObject();
			piObj.itemFKId = itmObj.pkid;
			piObj.itemType = itemType;
			piObj.currency = branch.currency;
			piObj.unitPriceStd = ess.getUnitPrice();
			piObj.unitPriceDiscounted = ess.getUnitPrice();
			piObj.unitPriceMin = ess.getUnitPrice();
			piObj.userIdUpdate = userId;
			POSItem piEJB = POSItemNut.fnCreate(piObj);
		}
		try
		{
			Log.printVerbose("checkpoint 5..........................");
			docrow = new DocRow();
			docrow.setTemplateId(piObj.pkid.intValue());
			docrow.setItemType(itemType);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(ess.getQty());
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(ess.getPrice1());
			docrow.user1 = ess.getPic1().intValue();
			docrow.setRemarks(ess.getRemarks());
			docrow.setCcy2(ess.getCcy2());
			docrow.setPrice2(ess.getPrice2());
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
			Log.printVerbose("checkpoint 7..........................");
			trss.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		session.setAttribute(essGuid, null);
	}
}
