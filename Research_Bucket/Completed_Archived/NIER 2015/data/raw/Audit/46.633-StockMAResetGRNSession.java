/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.inventory;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;

//20080430 Jimmy - New
public class StockMAResetGRNSession extends java.lang.Object implements Serializable
{
	public GoodsReceivedNoteObject grnObj;
	public String glCode = "";
	public BigDecimal shippingCharge = new BigDecimal(0);
	public String reason = "";
	public TreeMap treeRptRow = new TreeMap();
	public boolean canSubmit = false;
	public boolean canSave = false;
	
	public void reset()
	{
		this.grnObj = null;
		this.glCode = "";
		this.shippingCharge = new BigDecimal(0);
		this.reason = "";
		this.treeRptRow.clear();
		this.canSubmit = false;
		this.canSave = false;
	}
	
	public String getGRNId()
	{
		if (grnObj!=null)
		{
			return grnObj.mPkid.toString();
		} else {
			return ""; 
		}
	}
	
	public void setGRNObj(Long grnId)
	{
		reset();
		this.grnObj = GoodsReceivedNoteNut.getObject(grnId);
		
		if (this.grnObj != null) populateIntoRptStructure();
	}
	
	public void setGLCode(String glCode)
	{
		this.glCode = glCode;
	}
	
	public void setShippingCharge(BigDecimal shippingCharge)
	{
		this.shippingCharge = shippingCharge;
	}
	
	public void setReason(String reason)
	{
		this.reason = reason;
	}
	
	public void setRowMACost(Long grnItemId, BigDecimal maCost)
	{
		RptRow rptRow = (RptRow) this.treeRptRow.get(grnItemId);
		if (rptRow != null)
		{
			rptRow.newAveCost = maCost;
		}
	}
	
	public BigDecimal getRowMACost(Long grnItemId)
	{
		RptRow rptRow = (RptRow) this.treeRptRow.get(grnItemId);
		return rptRow.newAveCost;
	}
	
	private void populateIntoRptStructure() 
	{
		this.treeRptRow.clear();
		for (int cnt0 = 0; cnt0 < this.grnObj.vecGRNItems.size(); cnt0++)
		{
			try {
				GoodsReceivedNoteItemObject grniObj = (GoodsReceivedNoteItemObject) this.grnObj.vecGRNItems.get(cnt0);
				Stock stkEJB = StockNut.getObjectBy(grniObj.mItemId, this.grnObj.mLocationId, new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObj = stkEJB.getObject();
				
				RptRow rptRow = new RptRow();
				rptRow.grnItemId = grniObj.mPkid;
				rptRow.itemId = grniObj.mItemId;
				rptRow.itemCode = grniObj.mStkCode;
				rptRow.unitPrice = grniObj.mPrice1;
				rptRow.qty = grniObj.mTotalQty;
				rptRow.aveCost = stkObj.unitCostMA;
				rptRow.lastCost = stkObj.unitCostLast;
				rptRow.replacementCost = stkObj.unitCostReplacement;
				rptRow.stockQty = stkObj.balance;
				rptRow.stockAmount = stkObj.balance.multiply(stkObj.unitCostMA);
				rptRow.newAveCost = new BigDecimal(0);
				
				this.treeRptRow.put(rptRow.grnItemId, rptRow);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
	}
	
	public void calculateMAPrice()
	{
		BigDecimal totalAmount = new BigDecimal(0);
		BigDecimal totalQty = new BigDecimal(0);
		Vector vecRow = new Vector(this.treeRptRow.values());
				
		//1. Get total amount of GRN, not include zero balance of item and zero unit price 
		for (int cnt1 = 0; cnt1 < vecRow.size(); cnt1++)
		{
			RptRow rptRow = (RptRow) vecRow.get(cnt1);
			if (rptRow.stockQty.signum() > 0 && rptRow.unitPrice.signum() > 0) {
				totalAmount = totalAmount.add(rptRow.qty.multiply(rptRow.unitPrice));
				totalQty = totalQty.add(rptRow.qty);
			}
		}
		Log.printVerbose("Amount of GRN: " + totalAmount);
		
		//2. calculate
		for (int cnt1 = 0; cnt1 < vecRow.size(); cnt1++)
		{
			RptRow rptRow = (RptRow) vecRow.get(cnt1);
			BigDecimal shipping = new BigDecimal(0);
			if (rptRow.stockQty.signum() > 0 && rptRow.unitPrice.signum() > 0) {
				shipping = (rptRow.qty.multiply(rptRow.unitPrice)).multiply(this.shippingCharge).divide(totalAmount,2,BigDecimal.ROUND_HALF_EVEN);
				rptRow.newAveCost = rptRow.stockAmount.add(shipping).divide(rptRow.stockQty,2, BigDecimal.ROUND_HALF_EVEN);		
			}
		}
	}
	
	public void resetMAPrice(Integer userId)
	{
		ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(this.grnObj.mPCCenter);
		Integer iPCC = this.grnObj.mPCCenter;
		BigDecimal currentCost = new BigDecimal(0);
		BigDecimal totalQty = new BigDecimal(0);
		Vector vecRow = new Vector(this.treeRptRow.values());
		for (int cnt1 = 0; cnt1 < vecRow.size(); cnt1++)
		{
			try {
				RptRow rptRow = (RptRow) vecRow.get(cnt1);
				ItemObject itmObj = ItemNut.getObject(rptRow.itemId);
				Stock stkEJB = StockNut.getObjectBy(rptRow.itemId, this.grnObj.mLocationId, new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObj = stkEJB.getObject();
			
				if (rptRow.stockQty.signum() > 0)
				{
					currentCost = stkObj.unitCostMA;
					totalQty =rptRow.stockQty; 
						
					stkObj.unitCostMA = rptRow.newAveCost;
					stkEJB.setObject(stkObj);
					
					// Insert into inv_stock_adjustment
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
					stkAdj.remarks1 = this.reason;
					//stkAdj.remarks2 = ""; // varchar(100),
					stkAdj.src_pccenter = stkObj.accPCCenterId;
					//stkAdj.src_branch = new Integer(0); // integer,
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
					stkAdj.tgt_price1 = rptRow.newAveCost;
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
					
					if(rptRow.newAveCost.compareTo(currentCost) != 0)
					{
						// Insert Audit Trail
						AuditTrailObject atObj = new AuditTrailObject();
						atObj.userId = userId;
						atObj.auditType = AuditTrailBean.TYPE_CONFIG;
						atObj.time = TimeFormat.getTimestamp();
						atObj.remarks = "stock-ma-reset-grn: ITEMCODE:" + itmObj.code + " ";
						atObj.remarks += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->"
								+ CurrencyFormat.strCcy(rptRow.newAveCost) + " ";
						atObj.remarks += " QTY: " + CurrencyFormat.strInt(totalQty);
						atObj.remarks += " REASON: " + this.reason;
						atObj.remarks += " PCC:" + ProfitCostCenterNut.getFullName(iPCC);
						AuditTrailNut.fnCreate(atObj);
						
						// Insert Journal Tranx
						String description = " MA-RESET: ITEMCODE:" + itmObj.code;
						description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(rptRow.newAveCost)
								+ " ";
						description += " QTY: " + CurrencyFormat.strInt(totalQty);
						BigDecimal variance = rptRow.newAveCost.subtract(currentCost).multiply(totalQty);
						variance = new BigDecimal(CurrencyFormat.strCcy(variance));
						JournalTxnLogic.fnCreateStockVariance(this.glCode, iPCC, pccObj.mCurrency, variance, description, reason, userId);
					}
				} // end if rptRow > 0
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}// end for cnt1
	}
	
	public static class RptRow extends Object implements Serializable
	{
		public Long grnItemId;
		public Integer itemId;
		public String itemCode;
		public BigDecimal unitPrice;
		public BigDecimal qty;
		public BigDecimal aveCost;
		public BigDecimal lastCost;
		public BigDecimal replacementCost;
		public BigDecimal stockQty;
		public BigDecimal stockAmount;
		public BigDecimal newAveCost;
	}
}
