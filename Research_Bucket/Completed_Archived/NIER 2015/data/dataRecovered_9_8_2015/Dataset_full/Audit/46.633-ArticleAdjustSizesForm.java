/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.footwear;

import java.io.*;
import java.math.*;
import java.util.*;
import java.sql.*;

import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class ArticleAdjustSizesForm extends java.lang.Object implements Serializable
{
	private static final long serialVersionUID = 0;
	public static final String OBJNAME = "ArticleAdjustSizesForm";
	// MEMBER VARIABLES
	public Integer userId;
	protected LocationObject location = null;
	protected BranchObject branch = null;
	protected String itemPrefix = "";
	protected IsoPrefixRow isoPrefixRow = null;
	public String remarks = "";

	public Timestamp theDate = TimeFormat.getTimestamp();

	// / used for warehouse management module
	public ArticleAdjustSizesForm(Integer userId)
	{
		this.userId = userId;
		this.itemPrefix = "";
		this.isoPrefixRow = new IsoPrefixRow(this.userId);
		this.remarks = "";
		this.theDate = TimeFormat.getTimestamp();
	}

	public void setRemarks(String rmks)
	{ this.remarks = rmks; }
	public String getRemarks()
	{ return this.remarks; }

	public void setDate(String strDate)
	{ this.theDate = TimeFormat.createTimestamp(strDate);}
	public Timestamp getDate()
	{ return this.theDate;}


	public BranchObject getBranch()
	{ return this.branch; }

	public String getBranchId(String buf)
	{ 
		if(this.branch==null){ return buf;}
		return this.branch.pkid.toString();
	}

	public void setBranch(Integer iBranch)
	{
		this.branch = BranchNut.getObject(iBranch);
		if (this.branch != null)
		{
//			this.isoPrefixRow = new IsoPrefixRow(this.userId);
			this.isoPrefixRow.setBranch(iBranch);
			this.isoPrefixRow.setLocation(this.branch.invLocationId);
			setPrefix(this.itemPrefix);
			setLocation(this.branch.invLocationId);
		}
	}

	public void setLocation(Integer iLoc)
	{
		this.location = LocationNut.getValueObject(iLoc);
	}

	public Integer getLocationId()
	{ return this.location.pkid; }

	public void reset()
	{
		this.remarks = "";
	}

	public void setPrefix(String prefix)
	{
		this.itemPrefix  = prefix;
		if(prefix!=null && prefix.length()>3)
		{
			this.isoPrefixRow.setPrefix(prefix);
			this.isoPrefixRow.loadItemCodeByPrefix();
			this.isoPrefixRow.copyBalanceToQty();
		}
	}

	public String getPrefix()
	{ return this.itemPrefix;}

	public IsoPrefixRow getIsoPrefixRow()
	{ return this.isoPrefixRow;}
	public BigDecimal getTotalQty()
	{ return this.isoPrefixRow.getTotalQty(); }

	public BigDecimal getStockBalance()
	{ return this.isoPrefixRow.getStockBalance();}

	public boolean canSave()
	{
		if(! this.isoPrefixRow.hasAdjustment()){ return false;}
		if(! this.isoPrefixRow.qtyEqualsBalance()){ return false;}
		return true;
	}

	public synchronized void confirmSave()
			throws Exception
	{
		if(!canSave()){ return ;}
		TreeMap treeItem = this.isoPrefixRow.getTreeItem();
		Vector vecItem = new Vector(treeItem.values());
		String description = this.isoPrefixRow.getChangeDescription()+ " RMKS:"+this.remarks;
		description = StringManup.truncate(description,150);

		AuditTrailObject atObj = new AuditTrailObject();
      atObj.userId = this.userId;
      atObj.auditType = AuditTrailBean.TYPE_TXN;
      atObj.time = TimeFormat.getTimestamp();
      atObj.remarks = "ARTICLE-ADJUSTMENT:"+this.itemPrefix+" "+description;
      AuditTrailNut.fnCreate(atObj);
	
		BigDecimal unitCost = new BigDecimal(0);	
		for(int cnt1=0;cnt1<vecItem.size();cnt1++)
		{
			IsoPrefixRow.PerItemCode pItmCode = (IsoPrefixRow.PerItemCode) vecItem.get(cnt1);
			if(pItmCode.getStockBalance().compareTo(pItmCode.getQty())!=0)
			{
				BigDecimal deltaQty = pItmCode.getQty().subtract(pItmCode.getStockBalance());
				if(pItmCode.stkObj!=null)
				{ unitCost = pItmCode.stkObj.unitCostMA;}
				StockNut.adjustment(this.userId, pItmCode.itmObj.pkid, getLocationId(),
						this.branch.accPCCenterId, deltaQty, unitCost, this.branch.currency,
						"", new Long(0), 
						description,this.theDate, this.userId, new Vector(),"", "", "", "",this.branch.pkid); 
			}
		}	

		setPrefix(this.itemPrefix);
	}

}
















