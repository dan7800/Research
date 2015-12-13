/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.customer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.TreeMap;
import java.util.Vector;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class CustDepositSettlementForm extends java.lang.Object implements Serializable
{
	// public Hashtable outstandingDocuments;
	public TreeMap outstandingDocuments;
	public OfficialReceiptObject receiptObj;
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state;
	private Integer userId;


	public OfficialReceiptObject getReceipt()
	{ return this.receiptObj;}

	public void setReceipt(Long pkid)
	{
		this.receiptObj = OfficialReceiptNut.getObject(pkid);
		if(receiptObj!=null)
		{
			retrieveOutstandingDocuments();
		}
		
	}

	public TreeMap getOutstandingDocuments()
	{ return this.outstandingDocuments;}

	public BigDecimal getSettlementAmt()
	{
		Vector vecDoc = new Vector(this.outstandingDocuments.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
		{
			Document oneDoc = (Document) vecDoc.get(cnt1);
			totalAmt = totalAmt.add(oneDoc.thisSettlement);
		}
		return totalAmt;
	}

	public BigDecimal getTotalDocAmount()
	{
		Vector vecDoc = new Vector(this.outstandingDocuments.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
		{
			Document oneDoc = (Document) vecDoc.get(cnt1);
			totalAmt = totalAmt.add(oneDoc.docAmount);
		}
		return totalAmt;
	}

	public BigDecimal getTotalDocOutstanding()
	{
		Vector vecDoc = new Vector(this.outstandingDocuments.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
		{
			Document oneDoc = (Document) vecDoc.get(cnt1);
			totalAmt = totalAmt.add(oneDoc.docBalance);
		}
		return totalAmt;
	}

	public synchronized void knockOffDocuments()
	{
		if(this.receiptObj==null){ return;}

		this.receiptObj.openBalance = this.receiptObj.openBalance.subtract(getSettlementAmt());
		OfficialReceipt orEJB = OfficialReceiptNut.getHandle(this.receiptObj.pkid);
		try
		{
			orEJB.setObject(this.receiptObj);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		// / reset the outstanding balance of all invoices
		// // create the corresponding doclink beans.
		// create the respective DocLinkBeans
		Vector vecDocLinkEJB = new Vector();
		try
		{
			Vector vecDoc = new Vector(this.outstandingDocuments.values());
			for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
			{
				Document oneDoc = (Document) vecDoc.get(cnt1);
				if (oneDoc.docRef.equals(InvoiceBean.TABLENAME) && oneDoc.thisSettlement.signum() > 0)
				{
					Invoice thisInvEJB = InvoiceNut.getHandle(oneDoc.docId);


         AuditTrailObject atObj = new AuditTrailObject();
         atObj.userId = this.userId;
         atObj.auditType = AuditTrailBean.TYPE_TXN;
         atObj.remarks = "customer: deposit-settlement " + "RCT" + this.receiptObj.pkid.toString()+ " INV" + oneDoc.docId.toString()+" AMOUNT:"+CurrencyFormat.strCcy(oneDoc.thisSettlement);
         AuditTrailNut.fnCreate(atObj);

					
					thisInvEJB.adjustOutstanding(oneDoc.thisSettlement.negate());
					BigDecimal addBackPdc = this.receiptObj.amountPDCheque.multiply(
								oneDoc.thisSettlement.divide(getSettlementAmt(), 12, BigDecimal.ROUND_HALF_EVEN));
					thisInvEJB.adjustOutstandingBfPdc(oneDoc.thisSettlement.negate().add(addBackPdc));
					// Create the DocLink
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", 
								DocLinkBean.RELTYPE_PYMT_INV, OfficialReceiptBean.TABLENAME, 
								this.receiptObj.pkid, InvoiceBean.TABLENAME, oneDoc.docId, 
								this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
								this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
								"", this.receiptObj.paymentTime, this.userId);
					vecDocLinkEJB.add(newDocLink);
					// // reduce the outstanding amount for the sales order too
					// if exists
					SalesOrderIndex soEJB = SalesOrderIndexNut.getHandleByInvoice(oneDoc.docId);
					if (soEJB != null)
					{
						SalesOrderIndexObject soObj = soEJB.getObject();
						soEJB.adjustOutstanding(oneDoc.thisSettlement.negate());
						DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_PYMT_SO, 
								OfficialReceiptBean.TABLENAME, this.receiptObj.pkid, SalesOrderIndexBean.TABLENAME, 
								soObj.pkid, this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
								this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
								"", this.receiptObj.paymentTime, this.userId);
					}
				}// end if invoice settlement > 0
				if (oneDoc.docRef.equals(CreditMemoIndexBean.TABLENAME) && oneDoc.thisSettlement.signum() > 0)
				{
					CreditMemoIndex cmEJB = CreditMemoIndexNut.getHandle(oneDoc.docId);
         AuditTrailObject atObj = new AuditTrailObject();
         atObj.userId = this.userId;
         atObj.auditType = AuditTrailBean.TYPE_TXN;
         atObj.remarks = "customer: deposit-settlement " + "RCT" + this.receiptObj.pkid.toString()+ " CM" + oneDoc.docId.toString()+" AMOUNT:"+CurrencyFormat.strCcy(oneDoc.thisSettlement);
         AuditTrailNut.fnCreate(atObj);
					cmEJB.adjustBalance(oneDoc.thisSettlement.negate());
					BigDecimal addBackPdc = this.receiptObj.amountPDCheque.multiply(oneDoc.thisSettlement.divide(
															getSettlementAmt(), 12, BigDecimal.ROUND_HALF_EVEN));
					cmEJB.adjustBalanceBfPdc(oneDoc.thisSettlement.negate().add(addBackPdc));
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", 
								DocLinkBean.RELTYPE_PYMT_DN, OfficialReceiptBean.TABLENAME, this.receiptObj.pkid, 
								CreditMemoIndexBean.TABLENAME, oneDoc.docId, this.receiptObj.currency, 
								oneDoc.thisSettlement.negate(), // reduces
								this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
								"", this.receiptObj.paymentTime, this.userId);
					vecDocLinkEJB.add(newDocLink);
				}
			} // end for
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	// / contructor!
	public CustDepositSettlementForm(Integer iUser)
	{
		this.receiptObj = null;
		this.state = STATE_DRAFT;
		this.outstandingDocuments = new TreeMap();
		this.userId = iUser;
	}

	public void reset()
	{
		this.state = STATE_DRAFT;
		this.receiptObj = null;
		this.outstandingDocuments.clear();
		retrieveOutstandingDocuments();
	}

	//20080122 Jimmy - clear all Settlement Amount
	public void resetAmount()
	{
		Vector vecDoc = new Vector(this.outstandingDocuments.values());
		for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
		{
			Document oneDoc = (Document) vecDoc.get(cnt1);
			oneDoc.thisSettlement = new BigDecimal(0);
		}
	}
	// End 

	public void confirmAndSave() throws Exception
	{
		if (!canSave())
			throw new Exception("In complete form!");
		// 1) create the PurchaseOrder and GRN
		knockOffDocuments();
		reset();
	}

	public boolean canSave()
	{
		boolean result = true;
		// check state

		if(this.receiptObj ==null){ return false;}

		if (!this.state.equals(STATE_DRAFT))
		{
			result = false;
		}

		if(getSettlementAmt().compareTo(this.receiptObj.openBalance)>0)
		{
			result = false;
		}
		return result;
	}

	public void setRowAmt(String key, BigDecimal amount)
	{
		
		Document oneDoc = (Document) this.outstandingDocuments.get(key);
		if (oneDoc != null)
		{
			oneDoc.thisSettlement = amount;
		}
	}

	public BigDecimal getRowAmt(String key)
	{
		Document oneDoc = (Document) this.outstandingDocuments.get(key);
		return oneDoc.thisSettlement;
	}

	private void retrieveOutstandingDocuments()
	{
		if (this.outstandingDocuments == null)
		{ this.outstandingDocuments = new TreeMap(); } 
		else
		{ this.outstandingDocuments.clear(); }


		if(this.receiptObj == null){ return ;}

		QueryObject query = new QueryObject(new String[] { InvoiceBean.ENTITY_KEY + " = '" + this.receiptObj.entityKey.toString() 
				+ "' " + " AND " + InvoiceBean.PC_CENTER + " = '" + this.receiptObj.pcCenter.toString() + "' " + " AND " 
				+ InvoiceBean.OUTSTANDING_AMT + " > '0' " + " AND " + InvoiceBean.STATUS + " = '" + InvoiceBean.STATUS_ACTIVE 
				+ "' " + " AND " + InvoiceBean.ENTITY_TABLE + " = '" + CustAccountBean.TABLENAME + "' " });
		query.setOrder(" ORDER BY " + InvoiceBean.TIMEISSUED + ", " + InvoiceBean.PKID);
		Vector vecInvoice = new Vector(InvoiceNut.getObjects(query));
		for (int cnt1 = 0; cnt1 < vecInvoice.size(); cnt1++)
		{
			// GUIDGenerator gen = new GUIDGenerator();
			InvoiceObject cinvObj = (InvoiceObject) vecInvoice.get(cnt1);
			Document oneDoc = new Document();
			BranchObject branchObj = BranchNut.getObject(cinvObj.mCustSvcCtrId);
			oneDoc.branchCode = branchObj.code;
			oneDoc.docRef = InvoiceBean.TABLENAME;
			oneDoc.docId = cinvObj.mPkid;
			oneDoc.docDate = cinvObj.mTimeIssued;
			oneDoc.vecDocLink = new Vector();
			oneDoc.docAmount = cinvObj.mTotalAmt;
			oneDoc.docPaid = cinvObj.mTotalAmt.subtract(cinvObj.mOutstandingAmt);
			oneDoc.docBalance = cinvObj.mOutstandingAmt;
			oneDoc.thisSettlement = new BigDecimal(0);
			oneDoc.remarks = cinvObj.mRemarks + " " + cinvObj.mReferenceNo + " " + cinvObj.mDescription;
			this.outstandingDocuments.put(oneDoc.getKey(), oneDoc);
		}

		QueryObject query2 = new QueryObject(new String[] { CreditMemoIndexBean.ENTITY_KEY + " = '" 
			+ this.receiptObj.entityKey.toString() + "' ", CreditMemoIndexBean.PC_CENTER + " = '" 
			+ this.receiptObj.pcCenter.toString() + "' ", CreditMemoIndexBean.BALANCE + " > '0' ", 
				CreditMemoIndexBean.ENTITY_TABLE + " = '" + CustAccountBean.TABLENAME + "' ", 
				CreditMemoIndexBean.STATUS + " = '" + CreditMemoIndexBean.STATUS_ACTIVE + "' " });

		Vector vecCM = new Vector(CreditMemoIndexNut.getObjects(query2));
		for (int cnt1 = 0; cnt1 < vecCM.size(); cnt1++)
		{
			// GUIDGenerator gen = new GUIDGenerator();
			CreditMemoIndexObject cmObj = (CreditMemoIndexObject) vecCM.get(cnt1);
			Document oneDoc = new Document();
			BranchObject branchObj = BranchNut.getObject(cmObj.branch);
			oneDoc.branchCode = branchObj.code;
			oneDoc.docRef = CreditMemoIndexBean.TABLENAME;
			oneDoc.docId = cmObj.pkid;
			oneDoc.docDate = cmObj.timeCreate;
			oneDoc.vecDocLink = new Vector();
			oneDoc.docAmount = cmObj.amount;
			oneDoc.docPaid = cmObj.amount.subtract(cmObj.balance);
			oneDoc.docBalance = cmObj.balance;
			oneDoc.thisSettlement = new BigDecimal(0);
			oneDoc.remarks = cmObj.docReference + " " + cmObj.docDescription + " " + cmObj.memoRemarks + " " + cmObj.memoDescription;
			this.outstandingDocuments.put(oneDoc.getKey(), oneDoc);
		}
	}

	public void settleEarliest(BigDecimal theAmt) throws Exception
	{
		if (theAmt.signum() <= 0)
		{
			throw new Exception("Amount must be more than zero!");
		}
		Vector vecDoc = new Vector(this.outstandingDocuments.values());
		for (int cnt1 = 0; cnt1 < vecDoc.size() && theAmt.signum() > 0; cnt1++)
		{
			Document oneDoc = (Document) vecDoc.get(cnt1);
			if (oneDoc.docBalance.compareTo(theAmt) > 0 && theAmt.signum() > 0)
			{
				oneDoc.thisSettlement = theAmt;
				theAmt = new BigDecimal(0);
			} else if (theAmt.signum() > 0)
			{
				oneDoc.thisSettlement = oneDoc.docBalance;
				theAmt = theAmt.subtract(oneDoc.docBalance);
			} else
			{
				oneDoc.thisSettlement = new BigDecimal(0);
			}
		}
	}
	public static class Document
	{
		public String branchCode;
		public String docRef;
		public Long docId;
		public Timestamp docDate;
		public Vector vecDocLink;
		public BigDecimal docAmount;
		public BigDecimal docPaid;
		public BigDecimal docBalance;
		public BigDecimal thisSettlement;
		public String remarks;

		public Document()
		{
			this.branchCode = "";
			this.docRef = "";
			this.docId = new Long(0);
			this.docDate = TimeFormat.getTimestamp();
			this.vecDocLink = new Vector();
			this.docAmount = new BigDecimal(0);
			this.docPaid = new BigDecimal(0);
			this.docBalance = new BigDecimal(0);
			this.thisSettlement = new BigDecimal(0);
			this.remarks = "";
		}

		public String getKey()
		{
			return TimeFormat.strDisplayDate(this.docDate) + this.docRef + this.docId.toString();
		}
	}
}
