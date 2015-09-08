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
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoStockMAReset implements Action
{
	private String strClassName = "DoStockMAReset";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		fnPreserveParam(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter("inv-stock-ma-reset-page");
		}
		if (formName.equals("resetMAPrice"))
		{
			try
			{
				fnResetMAPrice(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-stock-ma-reset-page");
	}

	private void fnPreserveParam(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenter = req.getParameter("pcCenter");
		if (pcCenter != null)
		{
			req.setAttribute("pcCenter", pcCenter);
		}
		String itemCode = req.getParameter("itemCode");
		if (itemCode != null)
		{
			req.setAttribute("itemCode", itemCode);
		}
	}

	private void fnResetMAPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");


		String[] stockId = req.getParameterValues("stockId");
		String strUnitCost = req.getParameter("maUnitCost");
		String itemCode = req.getParameter("itemCode");
		String reason = req.getParameter("reason");
		BigDecimal bdUnitCost = null;
		BigDecimal totalQty = new BigDecimal(0);
		Integer iPCC = new Integer(req.getParameter("pcCenter"));
		ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(iPCC);
		try
		{
			bdUnitCost = new BigDecimal(strUnitCost);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Moving Average Cost!!");
		}

		ItemObject itmObj = ItemNut.getValueObjectByCode(itemCode);

		BigDecimal currentCost = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < stockId.length; cnt1++)
		{
			try
			{
				Integer iStockId = new Integer(stockId[cnt1]);
				if(iStockId.intValue()==0){ continue;}
				Stock stkEJB = (Stock) StockNut.getHandle(iStockId);
				StockObject stkObj = stkEJB.getObject();
				if(stkObj==null)
				{
					stkObj = StockNut.getObject(iStockId);
				}

				/// only reset the stock object if the  balance quantity is ZERO
				if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()==0)
				{
					stkObj.unitCostMA = bdUnitCost;
					stkEJB.setObject(stkObj);
				}

				/// create a stock adjustment entry if the stock has balance quantity
				if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()!=0)
				{
					totalQty = totalQty.add(stkObj.balance);
					currentCost = stkObj.unitCostMA;
					stkObj.unitCostMA = bdUnitCost;
					stkEJB.setObject(stkObj);
  
					StockAdjustmentObject stkAdj = new StockAdjustmentObject(); 
					//stkAdj.tx_code = ""; // varchar(50)
					stkAdj.tx_type = StockAdjustmentBean.TYPE_RESET_MA;
					//stkAdj.tx_module = ""; // varchar(50),
					//stkAdj.tx_option = ""; // varchar(50),
					stkAdj.userid1 = userId;
					stkAdj.userid2 = userId;
					stkAdj.userid3 = userId;
					//stkAdj.entity_table = ""; // varchar(50),
					//stkAdj.entity_key = new Integer(0); // bigint,
					//stkAdj.reference = ""; // varchar(100),
					//stkAdj.description = ""; // varchar(100),
					stkAdj.remarks1 = reason;
					//stkAdj.remarks2 = ""; // varchar(100),
					stkAdj.src_pccenter = stkObj.accPCCenterId;
//					stkAdj.src_branch = new Integer(0); // integer,
					stkAdj.src_location = stkObj.locationId;
					stkAdj.src_currency = pccObj.mCurrency;
					stkAdj.src_price1 = currentCost;
					stkAdj.src_qty1 = stkObj.balance;
					stkAdj.src_serialized = itmObj.serialized;
					//stkAdj.src_remarks = ""; // varchar(500),
					//stkAdj.src_refdoc = ""; // varchar(50),
					//stkAdj.src_refkey = new Long(0); // bigint,
					stkAdj.src_item_id = stkObj.itemId;
					stkAdj.src_item_code = itmObj.code;
					stkAdj.src_item_name = itmObj.name;
					//stkAdj.src_item_remarks = ""; // varchar(500),
					stkAdj.tgt_pccenter = stkObj.accPCCenterId;
//					stkAdj.tgt_branch = new Integer(0); // integer,
					stkAdj.tgt_location = stkObj.locationId;
					stkAdj.tgt_currency = pccObj.mCurrency;
					stkAdj.tgt_price1 = bdUnitCost;
					stkAdj.tgt_qty1 = stkObj.balance;
					stkAdj.tgt_serialized = itmObj.serialized;
					stkAdj.tgt_remarks = ""; // varchar(500),
					//stkAdj.tgt_refdoc = ""; // varchar(50),
					//stkAdj.tgt_refkey = new Long(0); // bigint,
					stkAdj.tgt_item_id = stkObj.itemId;
					stkAdj.tgt_item_code = itmObj.code;
					stkAdj.tgt_item_name = itmObj.name;
					//stkAdj.tgt_item_remarks = ""; // varchar(500),
					//stkAdj.property1 = ""; // varchar(100),
					//stkAdj.property2 = ""; // varchar(100),
					//stkAdj.property3 = ""; // varchar(100),
					//stkAdj.property4 = ""; // varchar(100),
					//stkAdj.property5 = ""; // varchar(100),
					//stkAdj.status = ""; // varchar(20),  -- RowStatus
					stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --

					StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

				}
				else if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()==0)
            {
               stkObj.unitCostMA = bdUnitCost;
               stkEJB.setObject(stkObj);
				}


			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		if(bdUnitCost.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "stock-ma-reset: ITEMCODE:" + itemCode + " ";
			atObj.remarks += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->"
					+ CurrencyFormat.strCcy(bdUnitCost) + " ";
			atObj.remarks += " QTY: " + CurrencyFormat.strInt(totalQty);
			atObj.remarks += " REASON: " + reason;
			atObj.remarks += " PCC:" + ProfitCostCenterNut.getFullName(iPCC);
			AuditTrailNut.fnCreate(atObj);
			String description = " MA-RESET: ITEMCODE:" + itemCode;
			description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdUnitCost)
					+ " ";
			description += " QTY: " + CurrencyFormat.strInt(totalQty);
			BigDecimal variance = bdUnitCost.subtract(currentCost).multiply(totalQty);
			variance = new BigDecimal(CurrencyFormat.strCcy(variance));
			JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, reason, userId);
		}
	}




}


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
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoStockMAReset implements Action
{
	private String strClassName = "DoStockMAReset";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		fnPreserveParam(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter("inv-stock-ma-reset-page");
		}
		if (formName.equals("resetMAPrice"))
		{
			try
			{
				fnResetMAPrice(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-stock-ma-reset-page");
	}

	private void fnPreserveParam(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenter = req.getParameter("pcCenter");
		if (pcCenter != null)
		{
			req.setAttribute("pcCenter", pcCenter);
		}
		String itemCode = req.getParameter("itemCode");
		if (itemCode != null)
		{
			req.setAttribute("itemCode", itemCode);
		}
	}

	private void fnResetMAPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");


		String[] stockId = req.getParameterValues("stockId");
		String strUnitCost = req.getParameter("maUnitCost");
		String itemCode = req.getParameter("itemCode");
		String reason = req.getParameter("reason");
		BigDecimal bdUnitCost = null;
		BigDecimal totalQty = new BigDecimal(0);
		Integer iPCC = new Integer(req.getParameter("pcCenter"));
		ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(iPCC);
		try
		{
			bdUnitCost = new BigDecimal(strUnitCost);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Moving Average Cost!!");
		}

		ItemObject itmObj = ItemNut.getValueObjectByCode(itemCode);

		BigDecimal currentCost = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < stockId.length; cnt1++)
		{
			try
			{
				Integer iStockId = new Integer(stockId[cnt1]);
				if(iStockId.intValue()==0){ continue;}
				Stock stkEJB = (Stock) StockNut.getHandle(iStockId);
				StockObject stkObj = stkEJB.getObject();
				if(stkObj==null)
				{
					stkObj = StockNut.getObject(iStockId);
				}

				/// only reset the stock object if the  balance quantity is ZERO
				if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()==0)
				{
					stkObj.unitCostMA = bdUnitCost;
					stkEJB.setObject(stkObj);
				}

				/// create a stock adjustment entry if the stock has balance quantity
				if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()!=0)
				{
					totalQty = totalQty.add(stkObj.balance);
					currentCost = stkObj.unitCostMA;
					stkObj.unitCostMA = bdUnitCost;
					stkEJB.setObject(stkObj);
  
					StockAdjustmentObject stkAdj = new StockAdjustmentObject(); 
					//stkAdj.tx_code = ""; // varchar(50)
					stkAdj.tx_type = StockAdjustmentBean.TYPE_RESET_MA;
					//stkAdj.tx_module = ""; // varchar(50),
					//stkAdj.tx_option = ""; // varchar(50),
					stkAdj.userid1 = userId;
					stkAdj.userid2 = userId;
					stkAdj.userid3 = userId;
					//stkAdj.entity_table = ""; // varchar(50),
					//stkAdj.entity_key = new Integer(0); // bigint,
					//stkAdj.reference = ""; // varchar(100),
					//stkAdj.description = ""; // varchar(100),
					stkAdj.remarks1 = reason;
					//stkAdj.remarks2 = ""; // varchar(100),
					stkAdj.src_pccenter = stkObj.accPCCenterId;
//					stkAdj.src_branch = new Integer(0); // integer,
					stkAdj.src_location = stkObj.locationId;
					stkAdj.src_currency = pccObj.mCurrency;
					stkAdj.src_price1 = currentCost;
					stkAdj.src_qty1 = stkObj.balance;
					stkAdj.src_serialized = itmObj.serialized;
					//stkAdj.src_remarks = ""; // varchar(500),
					//stkAdj.src_refdoc = ""; // varchar(50),
					//stkAdj.src_refkey = new Long(0); // bigint,
					stkAdj.src_item_id = stkObj.itemId;
					stkAdj.src_item_code = itmObj.code;
					stkAdj.src_item_name = itmObj.name;
					//stkAdj.src_item_remarks = ""; // varchar(500),
					stkAdj.tgt_pccenter = stkObj.accPCCenterId;
//					stkAdj.tgt_branch = new Integer(0); // integer,
					stkAdj.tgt_location = stkObj.locationId;
					stkAdj.tgt_currency = pccObj.mCurrency;
					stkAdj.tgt_price1 = bdUnitCost;
					stkAdj.tgt_qty1 = stkObj.balance;
					stkAdj.tgt_serialized = itmObj.serialized;
					stkAdj.tgt_remarks = ""; // varchar(500),
					//stkAdj.tgt_refdoc = ""; // varchar(50),
					//stkAdj.tgt_refkey = new Long(0); // bigint,
					stkAdj.tgt_item_id = stkObj.itemId;
					stkAdj.tgt_item_code = itmObj.code;
					stkAdj.tgt_item_name = itmObj.name;
					//stkAdj.tgt_item_remarks = ""; // varchar(500),
					//stkAdj.property1 = ""; // varchar(100),
					//stkAdj.property2 = ""; // varchar(100),
					//stkAdj.property3 = ""; // varchar(100),
					//stkAdj.property4 = ""; // varchar(100),
					//stkAdj.property5 = ""; // varchar(100),
					//stkAdj.status = ""; // varchar(20),  -- RowStatus
					stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --

					StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

				}
				else if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()==0)
            {
               stkObj.unitCostMA = bdUnitCost;
               stkEJB.setObject(stkObj);
				}


			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		if(bdUnitCost.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "stock-ma-reset: ITEMCODE:" + itemCode + " ";
			atObj.remarks += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->"
					+ CurrencyFormat.strCcy(bdUnitCost) + " ";
			atObj.remarks += " QTY: " + CurrencyFormat.strInt(totalQty);
			atObj.remarks += " REASON: " + reason;
			atObj.remarks += " PCC:" + ProfitCostCenterNut.getFullName(iPCC);
			AuditTrailNut.fnCreate(atObj);
			String description = " MA-RESET: ITEMCODE:" + itemCode;
			description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdUnitCost)
					+ " ";
			description += " QTY: " + CurrencyFormat.strInt(totalQty);
			BigDecimal variance = bdUnitCost.subtract(currentCost).multiply(totalQty);
			variance = new BigDecimal(CurrencyFormat.strCcy(variance));
			JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, reason, userId);
		}
	}




}


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
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoStockMAReset implements Action
{
	private String strClassName = "DoStockMAReset";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		fnPreserveParam(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter("inv-stock-ma-reset-page");
		}
		if (formName.equals("resetMAPrice"))
		{
			try
			{
				fnResetMAPrice(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-stock-ma-reset-page");
	}

	private void fnPreserveParam(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenter = req.getParameter("pcCenter");
		if (pcCenter != null)
		{
			req.setAttribute("pcCenter", pcCenter);
		}
		String itemCode = req.getParameter("itemCode");
		if (itemCode != null)
		{
			req.setAttribute("itemCode", itemCode);
		}
	}

	private void fnResetMAPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");


		String[] stockId = req.getParameterValues("stockId");
		String strUnitCost = req.getParameter("maUnitCost");
		String itemCode = req.getParameter("itemCode");
		String reason = req.getParameter("reason");
		BigDecimal bdUnitCost = null;
		BigDecimal totalQty = new BigDecimal(0);
		Integer iPCC = new Integer(req.getParameter("pcCenter"));
		ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(iPCC);
		try
		{
			bdUnitCost = new BigDecimal(strUnitCost);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Moving Average Cost!!");
		}

		ItemObject itmObj = ItemNut.getValueObjectByCode(itemCode);

		BigDecimal currentCost = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < stockId.length; cnt1++)
		{
			try
			{
				Integer iStockId = new Integer(stockId[cnt1]);
				if(iStockId.intValue()==0){ continue;}
				Stock stkEJB = (Stock) StockNut.getHandle(iStockId);
				StockObject stkObj = stkEJB.getObject();
				if(stkObj==null)
				{
					stkObj = StockNut.getObject(iStockId);
				}

				/// only reset the stock object if the  balance quantity is ZERO
				if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()==0)
				{
					stkObj.unitCostMA = bdUnitCost;
					stkEJB.setObject(stkObj);
				}

				/// create a stock adjustment entry if the stock has balance quantity
				if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()!=0)
				{
					totalQty = totalQty.add(stkObj.balance);
					currentCost = stkObj.unitCostMA;
					stkObj.unitCostMA = bdUnitCost;
					stkEJB.setObject(stkObj);
  
					StockAdjustmentObject stkAdj = new StockAdjustmentObject(); 
					//stkAdj.tx_code = ""; // varchar(50)
					stkAdj.tx_type = StockAdjustmentBean.TYPE_RESET_MA;
					//stkAdj.tx_module = ""; // varchar(50),
					//stkAdj.tx_option = ""; // varchar(50),
					stkAdj.userid1 = userId;
					stkAdj.userid2 = userId;
					stkAdj.userid3 = userId;
					//stkAdj.entity_table = ""; // varchar(50),
					//stkAdj.entity_key = new Integer(0); // bigint,
					//stkAdj.reference = ""; // varchar(100),
					//stkAdj.description = ""; // varchar(100),
					stkAdj.remarks1 = reason;
					//stkAdj.remarks2 = ""; // varchar(100),
					stkAdj.src_pccenter = stkObj.accPCCenterId;
//					stkAdj.src_branch = new Integer(0); // integer,
					stkAdj.src_location = stkObj.locationId;
					stkAdj.src_currency = pccObj.mCurrency;
					stkAdj.src_price1 = currentCost;
					stkAdj.src_qty1 = stkObj.balance;
					stkAdj.src_serialized = itmObj.serialized;
					//stkAdj.src_remarks = ""; // varchar(500),
					//stkAdj.src_refdoc = ""; // varchar(50),
					//stkAdj.src_refkey = new Long(0); // bigint,
					stkAdj.src_item_id = stkObj.itemId;
					stkAdj.src_item_code = itmObj.code;
					stkAdj.src_item_name = itmObj.name;
					//stkAdj.src_item_remarks = ""; // varchar(500),
					stkAdj.tgt_pccenter = stkObj.accPCCenterId;
//					stkAdj.tgt_branch = new Integer(0); // integer,
					stkAdj.tgt_location = stkObj.locationId;
					stkAdj.tgt_currency = pccObj.mCurrency;
					stkAdj.tgt_price1 = bdUnitCost;
					stkAdj.tgt_qty1 = stkObj.balance;
					stkAdj.tgt_serialized = itmObj.serialized;
					stkAdj.tgt_remarks = ""; // varchar(500),
					//stkAdj.tgt_refdoc = ""; // varchar(50),
					//stkAdj.tgt_refkey = new Long(0); // bigint,
					stkAdj.tgt_item_id = stkObj.itemId;
					stkAdj.tgt_item_code = itmObj.code;
					stkAdj.tgt_item_name = itmObj.name;
					//stkAdj.tgt_item_remarks = ""; // varchar(500),
					//stkAdj.property1 = ""; // varchar(100),
					//stkAdj.property2 = ""; // varchar(100),
					//stkAdj.property3 = ""; // varchar(100),
					//stkAdj.property4 = ""; // varchar(100),
					//stkAdj.property5 = ""; // varchar(100),
					//stkAdj.status = ""; // varchar(20),  -- RowStatus
					stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --

					StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

				}
				else if(stkObj.accPCCenterId.equals(iPCC) && stkObj.balance.signum()==0)
            {
               stkObj.unitCostMA = bdUnitCost;
               stkEJB.setObject(stkObj);
				}


			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		if(bdUnitCost.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "stock-ma-reset: ITEMCODE:" + itemCode + " ";
			atObj.remarks += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->"
					+ CurrencyFormat.strCcy(bdUnitCost) + " ";
			atObj.remarks += " QTY: " + CurrencyFormat.strInt(totalQty);
			atObj.remarks += " REASON: " + reason;
			atObj.remarks += " PCC:" + ProfitCostCenterNut.getFullName(iPCC);
			AuditTrailNut.fnCreate(atObj);
			String description = " MA-RESET: ITEMCODE:" + itemCode;
			description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdUnitCost)
					+ " ";
			description += " QTY: " + CurrencyFormat.strInt(totalQty);
			BigDecimal variance = bdUnitCost.subtract(currentCost).multiply(totalQty);
			variance = new BigDecimal(CurrencyFormat.strCcy(variance));
			JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, reason, userId);
		}
	}




}


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
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;

public class DoStockMAReset implements Action
{
	private String strClassName = "DoStockMAReset";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		fnPreserveParam(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter("inv-stock-ma-reset-page");
		}
		if (formName.equals("resetMAPrice"))
		{
			try
			{
				fnResetMAPrice(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		return new ActionRouter("inv-stock-ma-reset-page");
	}

	private void fnPreserveParam(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenter = req.getParameter("pcCenter");
		if (pcCenter != null)
		{
			req.setAttribute("pcCenter", pcCenter);
		}
		String itemCode = req.getParameter("itemCode");
		if (itemCode != null)
		{
			req.setAttribute("itemCode", itemCode);
		}
	}

	private void fnResetMAPrice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String[] stockId = req.getParameterValues("stockId");
		String strUnitCost = req.getParameter("maUnitCost");
		String itemCode = req.getParameter("itemCode");
		String reason = req.getParameter("reason");
		BigDecimal bdUnitCost = null;
		BigDecimal totalQty = new BigDecimal(0);
		try
		{
			bdUnitCost = new BigDecimal(strUnitCost);
		} catch (Exception ex)
		{
			throw new Exception("Invalid Moving Average Cost!!");
		}
		BigDecimal currentCost = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < stockId.length; cnt1++)
		{
			try
			{
				Stock stkEJB = (Stock) StockNut.getHandle(new Integer(stockId[cnt1]));
				StockObject stkObj = stkEJB.getObject();
				totalQty = totalQty.add(stkObj.balance);
				currentCost = stkObj.unitCostMA;
				stkObj.unitCostMA = bdUnitCost;
				stkEJB.setObject(stkObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		Integer iPCC = new Integer(req.getParameter("pcCenter"));
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		if (bdUnitCost.compareTo(currentCost) != 0)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "stock-ma-reset: ITEMCODE:" + itemCode + " ";
			atObj.remarks += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->"
					+ CurrencyFormat.strCcy(bdUnitCost) + " ";
			atObj.remarks += " QTY: " + CurrencyFormat.strInt(totalQty);
			atObj.remarks += " REASON: " + reason;
			atObj.remarks += " PCC:" + ProfitCostCenterNut.getFullName(iPCC);
			AuditTrailNut.fnCreate(atObj);
			String description = " MA-RESET: ITEMCODE:" + itemCode;
			description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdUnitCost)
					+ " ";
			description += " QTY: " + CurrencyFormat.strInt(totalQty);
			ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(iPCC);
			BigDecimal variance = bdUnitCost.subtract(currentCost).multiply(totalQty);
			JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, reason, userId);
		}
	}
}
