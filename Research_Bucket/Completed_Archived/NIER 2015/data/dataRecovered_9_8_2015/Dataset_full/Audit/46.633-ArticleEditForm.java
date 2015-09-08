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

public class ArticleEditForm extends java.lang.Object implements Serializable
{
	private static final long serialVersionUID = 0;
	public static final String OBJNAME = "ArticleEditForm";
	// MEMBER VARIABLES
	public Integer userId;
	protected String guid;
	protected Integer pic1;
	protected Integer pic2;
	protected Integer pic3;
	protected String remarks; // maxlength 200 chars
	protected String codeProject;
	protected String codeDepartment;
	protected String codeDealer;
	protected String itemPrefix = "";
	protected String parentAction = "";
	protected IsoPrefixRow isoPrefixRow = null;
	public Vector vecPromotion;

	public Vector vecDiscountElement;


	// / used for warehouse management module
	public ArticleEditForm(Integer userId)
	{
		GUIDGenerator gen = null;
		try
		{
			gen = new GUIDGenerator();
			this.guid = gen.getUUID();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		this.pic1 = userId;
		this.pic2 = userId;
		this.pic3 = userId;
		this.remarks = "";
		this.codeProject = "";
		this.codeDepartment = "";
		this.codeDealer = "";
		this.itemPrefix = "";
		this.isoPrefixRow = new IsoPrefixRow(this.userId);
		this.vecPromotion = new Vector();
		this.vecDiscountElement = new Vector();
	}

	public void setParentAction(String buf)
	{ this.parentAction = buf;}
	public String getParentAction()
	{ return this.parentAction;}

	public String getKey()
	{ return getGuid();}

	public void setUser1(int buf)
	{
		this.pic1 = new Integer(buf);
	}

	public String getGuid()
	{ return this.guid; }
	public void setRemarks(String rmks)
	{ this.remarks = rmks; }
	public String getRemarks()
	{ return this.remarks; }

	public void setPic1(String username1) throws Exception
	{
		Integer userId = UserNut.getUserId(username1);
		if (userId == null)
		{ throw new Exception(" Username1 is invalid!"); }
		this.pic1 = userId;
	}
	public Integer getPic1()
	{ return this.pic1; }

	public void setPic2(String username2) throws Exception
	{
		Integer userId = UserNut.getUserId(username2);
		if (userId == null)
		{ throw new Exception(" Username2 is invalid!"); }
		this.pic2 = userId;
	}
	public Integer getPic2()	
	{ return this.pic2;}

	public void setCodeProject(String buf)
	{ this.codeProject = buf; }
	public String getCodeProject()
	{ return this.codeProject; }

	public void setCodeDepartment(String buf)
	{ this.codeDepartment = buf; }
	public String getCodeDepartment()
	{ return this.codeDepartment; }

	public void setCodeDealer(String buf)
	{ this.codeDealer = buf; }
	public String getCodeDealer()
	{ return this.codeDealer; }


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
		}
	}

	public String getPrefix()
	{ return this.itemPrefix;}

	public IsoPrefixRow getIsoPrefixRow()
	{ return this.isoPrefixRow;}


	public void setDetails(String itemName, Integer iCategoryId, String category1, String category2, String category3,
									String category4, String category5, String itemDescription, BigDecimal bdPriceList,
									BigDecimal bdPriceSale, BigDecimal bdPriceDisc1, BigDecimal bdPriceDisc2, BigDecimal bdPriceDisc3,
									BigDecimal bdLastUnitCost, BigDecimal bdReplacementCost, BigDecimal bdPriceMin, String status)
	{
		if(this.isoPrefixRow==null){ return ;}
		TreeMap treeItem = this.isoPrefixRow.getTreeItem();
		Vector vecItem  = new Vector(treeItem.values());
		for(int cnt1=0;cnt1<vecItem.size();cnt1++)
		{
			if(this.pic1!=null) System.out.println("TUPP: userId=" + this.pic1);
			else System.out.println("TUPP: userId is null");
			IsoPrefixRow.PerItemCode pItmCode = (IsoPrefixRow.PerItemCode) vecItem.get(cnt1);
			try
			{
				//[[JOB-JOE
			BigDecimal oldPriceList = pItmCode.itmObj.priceList;
			BigDecimal oldPriceSale = pItmCode.itmObj.priceSale;
			// String strUserId = UserNut.getUserName(this.userId);
			{
				AuditTrailObject atObj = new AuditTrailObject();
				atObj.userId = this.pic1;
				atObj.auditType = AuditTrailBean.TYPE_CONFIG;
				atObj.time = TimeFormat.getTimestamp();
				atObj.remarks = "FootWear Edit Article: Item " + itemName + "(" +  pItmCode.itmObj.code + ")" + " price edited: " + "List price "  + oldPriceList.toString() + "->" + bdPriceList.toString() + ": Sale price " + oldPriceSale.toString() + "->" + bdPriceSale.toString();
				//atObj.remarks = "FootWear Edit Article: Item ";
				
				AuditTrailNut.fnCreate(atObj);
					System.out.println("TUPP: ArticleEditForm: audit trail block: userId=" + this.pic1.toString() + "|" + oldPriceList.toString() + "|" + oldPriceSale.toString());
			}
			//JOB-JOE]]
				
				Item itemEJB = ItemNut.getHandle(pItmCode.itmObj.pkid);
				pItmCode.itmObj.name = itemName;
				pItmCode.itmObj.categoryId = iCategoryId;
				pItmCode.itmObj.category1 = category1;
				pItmCode.itmObj.category2 = category2;
				pItmCode.itmObj.category3 = category3;
				pItmCode.itmObj.category4 = category4;
				pItmCode.itmObj.category5 = category5;
				pItmCode.itmObj.description = itemDescription;
				pItmCode.itmObj.priceList = bdPriceList;
				pItmCode.itmObj.priceSale = bdPriceSale;
				pItmCode.itmObj.priceDisc1 = bdPriceDisc1;
				pItmCode.itmObj.priceDisc2 = bdPriceDisc2;
				pItmCode.itmObj.priceDisc3 = bdPriceDisc3;
				pItmCode.itmObj.lastUnitCost = bdLastUnitCost;
				pItmCode.itmObj.replacementUnitCost = bdReplacementCost;
				pItmCode.itmObj.priceMin = bdPriceMin;
	            pItmCode.itmObj.status = status;
				itemEJB.setObject(pItmCode.itmObj);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}

		}
	}



}
















