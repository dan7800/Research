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
import com.vlee.bean.application.AppConfigManager;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class StockAdjustmentForm extends java.lang.Object implements Serializable
{
	public static final String OBJNAME = "StockAdjustmentForm";
	// MEMBER VARIABLES
	
	protected Integer branchId;
	protected BranchObject branchObj;	
	protected Timestamp date;
	protected String remarks;
	protected Integer userId;
	protected Vector vecStkAdj;

	public StockAdjustmentForm(Integer userid) throws Exception
	{
		this.userId = userid;
		this.vecStkAdj = new Vector();
		this.branchObj = new BranchObject();
		this.date = TimeFormat.getTimestamp();//TimeFormat.createTimestamp("0001-01-01");
		this.remarks = "";
	}

	public Vector getStockAdjustmentCollection() throws Exception
	{
		return this.vecStkAdj;
	}

	public void setBranch(Integer iBranch) throws Exception
	{
		this.branchObj = BranchNut.getObject(iBranch);
	}

	public BranchObject getBranch() throws Exception
	{
	 	return this.branchObj;
	}

	public void setDate(Timestamp txDate)
	{
		if (TimeFormat.strDisplayDate(txDate).equals(TimeFormat.strDisplayDate(this.date)))
		{
			return;
		}
		this.date = txDate;
	}

	public Timestamp getDate()
	{
		return this.date;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public String getRemarks()
	{
		return this.remarks;
	}	

	public boolean canSave() throws Exception
	{
		return true;
	}

	public void reset()
	{
		this.vecStkAdj = new Vector();
		this.date = TimeFormat.getTimestamp();
		this.remarks = "";
	}

	public void addNewStockAdjustmentItem(StockAdjustmentItem objStkItem) throws Exception
	{		
		this.vecStkAdj.add(objStkItem);
	}

	public void removeStockAdjustmentItem(String guid) throws Exception
	{
		for(int i=0; i<this.vecStkAdj.size(); i++)
		{
			if(((StockAdjustmentItem) this.vecStkAdj.get(i)).guid.equals(guid))
			{
				this.vecStkAdj.remove(i);
			}			
		}
	}	

	public void performStockAdjustment() throws Exception
	{

		BigDecimal unitCost = new BigDecimal(0);	
		for(int cnt1=0;cnt1<this.vecStkAdj.size();cnt1++)
		{
			StockAdjustmentItem stkAdj = (StockAdjustmentItem) this.vecStkAdj.get(cnt1);
			Item item = ItemNut.getObjectByCode(stkAdj.itemCode);
			ItemObject itmObj;
			if(item!=null)
			{
				itmObj = item.getObject();
				
				BigDecimal deltaQty = new BigDecimal(0);
				if(stkAdj.qtyMode.equals("ADD"))
				{
					deltaQty = new BigDecimal(stkAdj.qty.toString());
				}
				else
				{
					deltaQty = new BigDecimal(stkAdj.qty.toString()).multiply(new BigDecimal(-1));
				}
				
				
				Stock stk = StockNut.getObjectBy(itmObj.pkid,this.branchObj.invLocationId,new Integer(0));
				
				if(stk!=null)
				{ 
					StockObject stkObj = stk.getObject();
					if(stkAdj.qtyMode.equals("REMOVE") && stkObj.balance.compareTo(new BigDecimal(stkAdj.qty.toString()))<0)
					{
						throw new Exception("The stock amount to remove is greater than the current stock count.");
					}
				}
				else
				{
					if(stkAdj.qtyMode.equals("REMOVE"))
					{
						throw new Exception("The stock amount to remove is greater than the current stock count.");
					}
				}
				
				
				unitCost = stkAdj.costPrice;
				StockNut.adjustment(this.userId, itmObj.pkid, this.branchObj.invLocationId,
						this.branchObj.accPCCenterId, deltaQty, unitCost, this.branchObj.currency,
						"", new Long(0), 
						this.remarks,this.date, this.userId, new Vector(),"", "", "", "", this.branchObj.pkid); 
						
						
				AuditTrailObject atObj = new AuditTrailObject();
				atObj.userId = this.userId;
				atObj.auditType = AuditTrailBean.TYPE_CONFIG;
				atObj.time = TimeFormat.getTimestamp();
				atObj.remarks = "manual-stock-adjustment: ITEMCODE:" + stkAdj.itemCode;
				AuditTrailNut.fnCreate(atObj);
				String description = atObj.remarks;
				BigDecimal variance = unitCost.multiply(deltaQty);
				variance = new BigDecimal(CurrencyFormat.strCcy(variance));
				JournalTxnLogic.fnCreateStockVariance(this.branchObj.accPCCenterId, this.branchObj.currency, variance, description, this.remarks, this.userId, this.date);										
			}
			else
			{
				throw new Exception("This item code does not exist!");
			}

		}

	}

	public static class StockAdjustmentItem
	{
		public String itemCode;
		public String qtyMode;
		public Integer qty;
		public BigDecimal costPrice;
		public String guid;
		
		public StockAdjustmentItem(String pItemCode, String pQtyMode, Integer pQty, BigDecimal pCostPrice) throws Exception
		{
			itemCode = pItemCode;
			qtyMode = pQtyMode;
			qty = pQty;
			costPrice = pCostPrice;
			GUIDGenerator gen = null;
			try
			{
				gen = new GUIDGenerator();
				this.guid = gen.getUUID();
			} 		
			catch(Exception ex) { ex.printStackTrace(); }
		}	
	}

}


