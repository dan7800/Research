/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.supplier;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;

import com.vlee.ejb.customer.JobsheetItemObject;
import com.vlee.ejb.customer.SalesOrderItemObject;
import com.vlee.util.*;

// import com.vlee.bean.util.*;
public class PurchaseOrderObject extends java.lang.Object implements Serializable
{
	// Original member variables
	// Attributes of Object
	public Long mPkid; // Primary Key
	public Long mStmtNumber;
	public Long mPurchaseTxnId;
	public Timestamp mTimeCreated;
	public Timestamp mTimeComplete;
	public Integer mRequestorId;
	public Integer mApproverId;
	public String mCurrency;
	public String mRemarks;
	public String mState;
	public String mStatus;
	public Timestamp mLastUpdate;
	public Integer mUserIdUpdate;
	public String mCcyPair;
	public BigDecimal mXRate;
	public String mEntityTable;
	public Integer mEntityKey;
	public String mEntityName;
	public String mEntityType;
	public String mIdentityNumber;
	public String mEntityContactPerson;
	public String mEntityAdd1;
	public String mEntityAdd2;
	public String mEntityAdd3;
	public String mEntityPostcode;
	public String mEntityState;
	public String mEntityCountry;
	public String mEntityTelephone;
	public String mEntityFax;
	public String mEntityEmail;
	public Integer mSuppProcCtrId;
	public Integer mLocationId;
	public Integer mPCCenter;
	public String mTxnType;
	public BigDecimal mAmount;
	public String mStmtType;
	public String mReferenceNo;
	public String mDescription;
	public Timestamp mTermsDate;
	public String mShipTo;
	public String mProperty1;
	public String mProperty2;
	
	// Added for Supplier portal > Purchase order listing enhancement
	public String SuppProcessStatus;
	
	// Janet : Added for Blooming enhancement
	public String mForeignTable;
	public Long mForeignKey;
	// End Janet
	
	// Derived Member Variables
	public Vector vecPurchaseOrderItems = new Vector();
	// Presentation layer params
	public Integer mDefaultRowsNumber = new Integer(15);

	// default number of rows
	// Constructor
	// Create a new / empty PurchaseOrderObject
	public PurchaseOrderObject()
	{
		// Populate Defaults
		this.mPkid = new Long("0");
		this.mStmtNumber = new Long("0");
		this.mPurchaseTxnId = new Long("0");
		this.mTimeCreated = TimeFormat.getTimestamp();
		this.mTimeComplete = this.mTimeCreated;
		this.mRequestorId = new Integer(0);
		this.mApproverId = new Integer(0);
		this.mCurrency = "";
		this.mRemarks = "";
		this.mState = PurchaseOrderBean.STATE_CREATED;
		this.mStatus = PurchaseOrderBean.STATUS_ACTIVE;
		this.mLastUpdate = TimeFormat.getTimestamp();
		this.mUserIdUpdate = new Integer(0);
		this.mCcyPair = "";
		this.mXRate = new BigDecimal("0.00");
		this.mEntityTable = "";
		this.mEntityKey = new Integer(0);
		this.mEntityName = "";
		this.mEntityType = "";
		this.mIdentityNumber = "";
		this.mEntityContactPerson = "";
		this.mEntityAdd1 = "";
		this.mEntityAdd2 = "";
		this.mEntityAdd3 = "";
		this.mEntityPostcode = "";
		this.mEntityState = "";
		this.mEntityCountry = "";
		this.mEntityTelephone = "";
		this.mEntityFax = "";
		this.mEntityEmail = "";
		this.mSuppProcCtrId = new Integer(0);
		this.mLocationId = new Integer(0);
		this.mPCCenter = new Integer(0);
		this.mTxnType = "";
		this.mAmount = new BigDecimal("0.00");
		this.mStmtType = "";
		this.mReferenceNo = "";
		this.mDescription = "";
		this.mTermsDate = TimeFormat.createTimestamp("0001-01-01");
		this.mShipTo = "";
		this.mProperty1 = "";
		this.mProperty2 = "";
		
		// Added for Supplier portal > Purchase order listing enhancement
		this.SuppProcessStatus = "";
		
		// Janet 
		this.mForeignTable = "";
		this.mForeignKey = new Long("0");
		// End Janet
		
		this.vecPurchaseOrderItems = new Vector();
		this.vecPurchaseOrderItems.clear();
	}

	// Write a mthod to verify each of the PurchaseOrder Items
	public String fnVerifyPurchaseOrderItems()
	{
		return null;
	}

	// retrieve the POItemObject corresponding to the POItem Id
	public PurchaseOrderItemObject findPOIObj(Long poiId)
	{
		Log.printVerbose("*** poiId = " + poiId);
		PurchaseOrderItemObject matchingPOItemObj = null;
		for (int i = 0; i < vecPurchaseOrderItems.size(); i++)
		{
			matchingPOItemObj = (PurchaseOrderItemObject) vecPurchaseOrderItems.get(i);
			if (matchingPOItemObj.mPkid.compareTo(poiId) == 0)
			{
				Log.printVerbose("*** matchingPOItemObj.mPkid = " + matchingPOItemObj.mPkid);
				return matchingPOItemObj;
			}
		}
		// if we reach here, it's either vecPurchaseOrderItems is empty or that
		// we couldn't find anything
		return null;
	}

	public void setItems(Collection colDocRow)
	{
		Vector vecDocRow = new Vector(colDocRow);
		this.vecPurchaseOrderItems.clear();
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			PurchaseOrderItemObject poiObj = new PurchaseOrderItemObject(docrow);
			this.vecPurchaseOrderItems.add(poiObj);
		}
		this.mAmount = getQuotedAmount();
	}

	public BigDecimal getQuotedAmount()
	{
		BigDecimal bdTotal = new BigDecimal(0);
		// Loop through vecPurchaseOrderItems to sum up the quoted prices for
		// each subtending item
		for (int i = 0; i < vecPurchaseOrderItems.size(); i++)
		{
			PurchaseOrderItemObject jsIO = (PurchaseOrderItemObject) vecPurchaseOrderItems.get(i);
			bdTotal = bdTotal.add(jsIO.mUnitPriceQuoted.multiply(jsIO.mTotalQty));
		}
		return bdTotal;
	}

	public String toString()
	{
		// For printing contents of this object
		String dbgStr = "PurchaseOrderObject:\n";
		dbgStr += "mPkid = " + this.mPkid.toString() + "\n";
		dbgStr += "mPurchaseTxnId = " + this.mPurchaseTxnId.toString() + "\n";
		dbgStr += "mTimeCreated = " + this.mTimeCreated.toString() + "\n";
		dbgStr += "mTimeComplete = " + this.mTimeComplete.toString() + "\n";
		dbgStr += "mRequestorId = " + this.mRequestorId.toString() + "\n";
		dbgStr += "mApproverId = " + this.mApproverId.toString() + "\n";
		dbgStr += "mCurrency = " + this.mCurrency.toString() + "\n";
		dbgStr += "mRemarks = " + this.mRemarks + "\n";
		dbgStr += "mState = " + this.mState + "\n";
		dbgStr += "mStatus = " + this.mStatus + "\n";
		dbgStr += "mLastUpdate = " + this.mLastUpdate.toString() + "\n";
		dbgStr += "mUserIdUpdate = " + this.mUserIdUpdate.toString() + "\n";
		dbgStr += "mShipTo = " + this.mShipTo.toString() + "\n";
		// print the vector of items
		for (Iterator itemItr = vecPurchaseOrderItems.iterator(); itemItr.hasNext();)
		{
			PurchaseOrderItemObject lInvItemObj = (PurchaseOrderItemObject) itemItr.next();
			dbgStr += lInvItemObj.toString();
		}
		return dbgStr;
	}

   public boolean usingForeignCurrency()
   {
      if (this.mXRate.signum() <= 0)
      {
         return false;
      }
      if (this.mCcyPair.length() < 1)
      {
         return false;
      }
      return true;
   }     
   
   public void calculateAmount()
	{
	   this.mAmount = new BigDecimal(0); //20
	   
	   for(int cnt1=0;cnt1<this.vecPurchaseOrderItems.size();cnt1++)
	   {
		   PurchaseOrderItemObject poiObj = (PurchaseOrderItemObject) this.vecPurchaseOrderItems.get(cnt1);
		   this.mAmount = this.mAmount.add(poiObj.mUnitPriceQuoted.multiply(poiObj.mTotalQty));		   			
	   }	
	}
   
   public boolean hasPOItem(Long poItemPkid)
	{
		boolean result = false;
		
		String vecNewItem = (new Integer(this.vecPurchaseOrderItems.size())).toString(); 
		System.out.println("1111. vecNewItem : "+vecNewItem);
		
		for(int cnt1=0;cnt1<this.vecPurchaseOrderItems.size();cnt1++)
		{
			PurchaseOrderItemObject poiObj = (PurchaseOrderItemObject) this.vecPurchaseOrderItems.get(cnt1);
			
			System.out.println("1111. old poiObj.pkid : "+poItemPkid.toString());
			System.out.println("1111. new poiObj.pkid : "+poiObj.mPkid.toString());
						
			if(poiObj.mPkid.equals(poItemPkid))
			{ return true;}
		}
		
		System.out.println("Checkpoint PurchaseOrderObject.hasPOItem");
		
		return result;			
	}
}
