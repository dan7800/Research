/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.reports;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class DailySalesReportType02Session extends java.lang.Object implements Serializable
{
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state = null;
	private BranchObject branch = null;
	// private Timestamp date;
	private Timestamp dateFrom;
	private Timestamp dateTo;
	private boolean reportClean = false;
	public Vector vecSalesCollection = null;
	public Vector vecPrevCollection = null;
	public Vector vecSalesReturn = null;
	public Vector vecDeposit = null;
	public String reportBranch = "";
	public String reportDateFrom = "";
	public String reportDateTo = "";
	public Timestamp generateTime = TimeFormat.getTimestamp();

	// / contructor!
	public DailySalesReportType02Session() throws Exception
	{
		this.reportClean = false;
		this.dateFrom = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateFrom, 0, 0, -1);
		this.dateFrom = TimeFormat.add(this.dateFrom, 0, 0, 1);
		this.dateTo = this.dateFrom;
		this.vecSalesCollection = new Vector();
		this.vecPrevCollection = new Vector();
		this.vecDeposit = new Vector();
		this.vecSalesReturn = new Vector();
	}

	public void reset()
	{
		this.branch = null;
	}

	public void generateReport()
	{
		// / check if other parameters are OK
		this.reportClean = true;
		this.vecSalesCollection.clear();
		this.vecPrevCollection.clear();
		this.vecDeposit.clear();
		this.vecSalesReturn.clear();
		// // set the report info
		if (this.branch != null)
		{
			this.reportBranch = this.branch.description + " " + this.branch.name;
		} else
		{
			this.reportBranch = " ALL BRANCHES ";
		}
		this.reportDateFrom = TimeFormat.strDisplayDate(this.dateFrom);
		this.reportDateTo = TimeFormat.strDisplayDate(this.dateTo);
		// /////////////////////////////////////////////////////////////////////////////
		// / step 1:
		// / find all the date's invoices and cashsale
		Timestamp dayAfter = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query1 = null;
		if (this.branch != null)
		{
			query1 = new QueryObject(new String[] {
					InvoiceBean.TIMEISSUED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					InvoiceBean.TIMEISSUED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					InvoiceBean.CUST_SVCCTR_ID + "= '" + this.branch.pkid.toString() + "' " });
		} else
		{
			query1 = new QueryObject(new String[] {
					InvoiceBean.TIMEISSUED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					InvoiceBean.TIMEISSUED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' " });
		}
		query1.setOrder(" ORDER BY " + InvoiceBean.STMT_NO + ", " + InvoiceBean.PKID);
		Vector vecInvoices = new Vector(InvoiceNut.getObjects(query1));
		for (int cnt1 = 0; cnt1 < vecInvoices.size(); cnt1++)
		{
			InvoiceObject invObj = (InvoiceObject) vecInvoices.get(cnt1);
			BranchObject branchObj = BranchNut.getObject(invObj.mCustSvcCtrId);
			ReportRow rptRow = new ReportRow();
			rptRow.position = cnt1 + 1;
			rptRow.description = invObj.mForeignText + " " + invObj.mEntityName + " " + invObj.mEntityContactPerson;
			rptRow.customerId = invObj.mEntityKey.intValue();
			if (rptRow.customerId == CustAccountBean.PKID_CASH.intValue())
			{
				rptRow.description = "CashSale:" + invObj.mEntityName;
			}
			// if(!invObj.mEntityName.equals("Cash"))
			// { rptRow.description = rptRow.description + " " +
			// invObj.mEntityName;}
			rptRow.docDate = invObj.mTimeIssued;
			rptRow.stmtNo = invObj.mStmtNumber.longValue();
			rptRow.documentPkid = invObj.mPkid.longValue();
			rptRow.branchCode = branchObj.code;
			rptRow.paymentInfo = "";
			rptRow.documentAmt = invObj.mTotalAmt;
			rptRow.username = UserNut.getUserName(invObj.mUserIdUpdate);
			Vector vecDocLink = new Vector(DocLinkNut.getByTargetDoc(InvoiceBean.TABLENAME, invObj.mPkid));
			rptRow.vecDocLink = vecDocLink;
			for (int cnt2 = 0; cnt2 < vecDocLink.size(); cnt2++)
			{
				DocLinkObject dlObj = (DocLinkObject) vecDocLink.get(cnt2);
				if (dlObj.srcDocRef.equals(OfficialReceiptBean.TABLENAME))
				{
					OfficialReceiptObject orObj = OfficialReceiptNut.getObject(dlObj.srcDocId);
					// // check for same date
					Timestamp rctNextDay = TimeFormat.add(orObj.paymentTime, 0, 0, 1);
					Timestamp invNextDay = TimeFormat.add(invObj.mTimeIssued, 0, 0, 1);
					// if(rctNextDay.getTime()!=invNextDay.getTime())
					// { continue;}
					//if(rctNextDay.getTime() > dayAfter.getTime() || orObj.paymentTime.getTime() < this.dateFrom.getTime() )
					if(rctNextDay.getTime() > dayAfter.getTime() )
					{
						continue;
					}
					if (this.branch != null && !this.branch.pkid.equals(orObj.branch))
					{
						continue;
					}
					if(orObj.state.equals(OfficialReceiptBean.ST_REVERSED))
					{
						continue;
					}
					rptRow.paymentInfo += " " + orObj.cardType + " " + orObj.paymentMethod + " "
							+ orObj.cardApprovalCode + " " + orObj.chequeNumber + " " + orObj.chequeNumberPD;

					if(orObj.paymentTime.getTime() < this.dateFrom.getTime())
					{
						rptRow.paymentInfo = " DEPOSIT (RCT" +orObj.pkid.toString()+") PAID ON "+TimeFormat.strDisplayDate(orObj.paymentTime)+" "+rptRow.paymentInfo;
						continue;
					}

					BigDecimal amtForThisInvoice = new BigDecimal(0);
					BigDecimal amtOfReceipt = new BigDecimal(0);
					Vector vecDocLinkRct = new Vector(DocLinkNut.getBySourceDoc(OfficialReceiptBean.TABLENAME,
							dlObj.srcDocId));
					for (int cnt3 = 0; cnt3 < vecDocLinkRct.size(); cnt3++)
					{
						DocLinkObject dlObj2 = (DocLinkObject) vecDocLinkRct.get(cnt3);
						// / must check the tgtDoc of this receipt
						// / must check the date of this receipt
						if (!dlObj2.tgtDocRef.equals(SalesOrderIndexBean.TABLENAME))
						{
							amtOfReceipt = amtOfReceipt.add(dlObj2.amount.abs());
						}
						if (dlObj2.tgtDocRef.equals(InvoiceBean.TABLENAME) && dlObj2.tgtDocId.equals(invObj.mPkid))
						{
							amtForThisInvoice = amtForThisInvoice.add(dlObj2.amount.abs());
						}
					}
					if (amtForThisInvoice.signum() > 0)
					{
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.cashAmt = rptRow.cashAmt.add(orObj.amountCash.multiply(amtForThisInvoice.divide(
									amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.cardAmt = rptRow.cardAmt.add(orObj.amountCard.multiply(amtForThisInvoice.divide(
									amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.chequeAmt = rptRow.chequeAmt.add(orObj.amountCheque.multiply(amtForThisInvoice
									.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.pdChequeAmt = rptRow.pdChequeAmt.add(orObj.amountPDCheque.multiply(amtForThisInvoice
									.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.couponAmt = rptRow.couponAmt.add(orObj.amountCoupon.multiply(amtForThisInvoice
									.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.otherAmt = rptRow.otherAmt.add(orObj.amountOther.multiply(amtForThisInvoice.divide(
									amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
					}
				}
			}
			Vector vecInvItem = InvoiceItemNut.getValueObjectsGiven(InvoiceItemBean.INVOICE_ID,
					invObj.mPkid.toString(), InvoiceItemBean.POS_ITEM_ID, POSItemBean.PKID_TRADEDISC.toString(),
					InvoiceItemBean.STATUS, InvoiceItemBean.STATUS_ACTIVE);
			for (int cnt = 0; cnt < vecInvItem.size(); cnt++)
			{
				InvoiceItemObject iiObj = (InvoiceItemObject) vecInvItem.get(cnt);
				rptRow.discountAmt = rptRow.discountAmt.add(iiObj.mUnitPriceQuoted.abs());
			}
			rptRow.termsAmt = rptRow.documentAmt.subtract(rptRow.cashAmt).subtract(rptRow.cardAmt).subtract(
					rptRow.chequeAmt).subtract(rptRow.pdChequeAmt).subtract(rptRow.couponAmt).subtract(rptRow.otherAmt);
			this.vecSalesCollection.add(rptRow);
		}
		// ////////////////////////////////////////////////////////////////////////
		// / step 2:
		// / load the DocLinkBean tgtDoc = Invoice in (1)
		// / and only add the amount if InvoiceID exist above
		// / step 3:
		// / find all the OfficialReceipt on the same day
		QueryObject query2 = null;
		if (this.branch != null)
		{
			query2 = new QueryObject(new String[] {
					OfficialReceiptBean.PAYMENT_TIME + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					OfficialReceiptBean.PAYMENT_TIME + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					OfficialReceiptBean.BRANCH + "= '" + this.branch.pkid.toString() + "' ",
					OfficialReceiptBean.STATE + "= '" + OfficialReceiptBean.ST_CREATED + "' " });
		} else
		{
			query2 = new QueryObject(new String[] {
					OfficialReceiptBean.PAYMENT_TIME + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					OfficialReceiptBean.PAYMENT_TIME + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					OfficialReceiptBean.STATE + "= '" + OfficialReceiptBean.ST_CREATED + "' " });
		}
		query2.setOrder(" ORDER BY " + OfficialReceiptBean.STMT_NO + ", " + OfficialReceiptBean.PKID);
		Vector vecReceipts = new Vector(OfficialReceiptNut.getObjects(query2));
		Log.printVerbose("................checkpoint.........11......");
		Log.printVerbose("................checkpoint.........11......");
		Log.printVerbose("................checkpoint.........11......");
		Log.printVerbose("...... SIZE OF VECTOR = "+vecReceipts.size());

		for (int cnt1 = 0; cnt1 < vecReceipts.size(); cnt1++)
		{
			OfficialReceiptObject orObj = (OfficialReceiptObject) vecReceipts.get(cnt1);
			BranchObject branchObj = BranchNut.getObject(orObj.branch);
			// find all DocLInk with srcDoc = this officialReceipt
			ReportRow rptRow = new ReportRow();
			rptRow.position = cnt1 + 1;
			rptRow.description = orObj.entityName;
			rptRow.branchCode = branchObj.code;
			rptRow.customerId = orObj.entityKey.intValue();
			rptRow.docDate = orObj.paymentTime;
			rptRow.stmtNo = orObj.stmtNumber.longValue();
			rptRow.documentPkid = orObj.pkid.longValue();
			rptRow.documentAmt = new BigDecimal(0);
			rptRow.username = UserNut.getUserName(orObj.userIdUpdate);
			Vector vecDocLink = new Vector(DocLinkNut.getBySourceDoc(OfficialReceiptBean.TABLENAME, orObj.pkid));
			rptRow.vecDocLink = vecDocLink;
			//// FOR DEPOSIT, THERE IS NO DOCLINK
			if(vecDocLink.size()==0)
			{
				rptRow.cashAmt = orObj.amountCash;
				rptRow.cardAmt = orObj.amountCard;
				rptRow.chequeAmt = orObj.amountCheque;
				rptRow.pdChequeAmt = orObj.amountPDCheque;
				rptRow.couponAmt = orObj.amountCoupon;
				rptRow.otherAmt = orObj.amountOther;
				rptRow.paymentInfo += "DEPOSIT";
			}

			//// FOR RECEIPTS LINK to OTHER PAYMENTS
			for (int cnt2 = 0; cnt2 < vecDocLink.size(); cnt2++)
			{
				DocLinkObject dlObj = (DocLinkObject) vecDocLink.get(cnt2);
				if (dlObj.tgtDocRef.equals(InvoiceBean.TABLENAME))
				{
					// // option 1: check for same date
					/*
					 * InvoiceObject invObj =
					 * InvoiceNut.getObject(dlObj.tgtDocId); Timestamp
					 * rctNextDay = TimeFormat.add(orObj.paymentTime,0,0,1);
					 * Timestamp invNextDay =
					 * TimeFormat.add(invObj.mTimeIssued,0,0,1);
					 * if(rctNextDay.getTime()==invNextDay.getTime()) {
					 * rptRow.vecDocLink.remove(cnt2); cnt2--; continue; }
					 */// / option 2: use the vecInvoices above to see if it
						// has be accounted for
					boolean existAbove = false;
					for (int cnt4 = 0; cnt4 < vecInvoices.size(); cnt4++)
					{
						InvoiceObject invObj4 = (InvoiceObject) vecInvoices.get(cnt4);
						if (dlObj.tgtDocId.equals(invObj4.mPkid))
						{
							existAbove = true;
						}
					}
					if (existAbove)
					{
						rptRow.vecDocLink.remove(cnt2);
						cnt2--;
						continue;
					}
				}
				if (dlObj.tgtDocRef.equals(SalesOrderIndexBean.TABLENAME))
				{
					rptRow.vecDocLink.remove(cnt2);
					cnt2--;
					continue;
				}
				BigDecimal amtForThisDocLink = dlObj.amount.abs();
				BigDecimal amtOfReceipt = orObj.amount.abs();
				if (amtForThisDocLink.signum() > 0)
				{
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.cashAmt = rptRow.cashAmt.add(orObj.amountCash.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.cardAmt = rptRow.cardAmt.add(orObj.amountCard.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.chequeAmt = rptRow.chequeAmt.add(orObj.amountCheque.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.pdChequeAmt = rptRow.pdChequeAmt.add(orObj.amountPDCheque.multiply(amtForThisDocLink
								.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.couponAmt = rptRow.couponAmt.add(orObj.amountCoupon.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.otherAmt = rptRow.otherAmt.add(orObj.amountOther.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
				}
			}// / end for doclink

			rptRow.paymentInfo += " " + orObj.cardType + " " + orObj.chequeNumber + " " + orObj.chequeNumberPD;
			// / only add this rptRow if the remaining amount greater than zero
			if(rptRow.cashAmt.signum() > 0 || rptRow.cardAmt.signum() > 0 || rptRow.chequeAmt.signum() > 0
					|| rptRow.pdChequeAmt.signum() > 0 || rptRow.couponAmt.signum() > 0 || rptRow.otherAmt.signum() > 0)
			{
				if(rptRow.vecDocLink.size()==0)
				{
					this.vecDeposit.add(rptRow);
				}
				else
				{	
					this.vecPrevCollection.add(rptRow);
				}
			}



		} // / end for vecReceipt
		// ////////////////////////////////////////////////////////////////////////////
		// / LOOK FOR SALES RETURN
		QueryObject query3 = null;
		if (this.branch != null)
		{
			query3 = new QueryObject(new String[] {
					SalesReturnBean.TIME_CREATED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					SalesReturnBean.TIME_CREATED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					SalesReturnBean.CUST_SVC_CTR_ID + "= '" + this.branch.pkid.toString() + "' " });
		} else
		{
			query3 = new QueryObject(new String[] {
					SalesReturnBean.TIME_CREATED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					SalesReturnBean.TIME_CREATED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' " });
		}
		query3.setOrder(" ORDER BY " + SalesReturnBean.PKID + ", " + SalesReturnBean.STMT_NO);
		Vector vecSRObj = new Vector(SalesReturnNut.getObjects(query3));
		for (int cnt1 = 0; cnt1 < vecSRObj.size(); cnt1++)
		{
			SalesReturnObject srObj = (SalesReturnObject) vecSRObj.get(cnt1);
			BranchObject branchObj = BranchNut.getObject(srObj.mCustSvcCtrId);
			InvoiceObject invObj = InvoiceNut.getObject(srObj.mDocId);
			ReportRow rptRow = new ReportRow();
			rptRow.position = cnt1 + 1;
			rptRow.description = invObj.mEntityName + " " + invObj.mForeignText;
			rptRow.customerId = srObj.mCustAccId.intValue();
			if (rptRow.customerId == CustAccountBean.PKID_CASH.intValue())
			{
				rptRow.description = "CashSale:" + invObj.mEntityName;
			}
			rptRow.description += " Inv" + srObj.mDocId.toString() + " ";
			InvoiceObject invReturn = InvoiceNut.getObject(srObj.mDocId);
			if (invReturn != null)
			{
				rptRow.description += "(" + TimeFormat.strDisplayDate(invObj.mTimeIssued) + ")";
			}
			rptRow.customerId = srObj.mCustAccId.intValue();
			rptRow.docDate = srObj.mTimeCreated;
			rptRow.stmtNo = srObj.mStmtNumber.longValue();
			rptRow.documentPkid = srObj.mPkid.longValue();
			rptRow.branchCode = branchObj.code;
			rptRow.paymentInfo = srObj.mRemarks;
			rptRow.cashAmt = srObj.mTotalAmt.negate();
			if (srObj.mProcessDocTable.equals(CreditMemoIndexBean.TABLENAME))
			{
				rptRow.cashAmt = new BigDecimal(0);
			}
			// rptRow.cashAmt = new BigDecimal(0);
			rptRow.cardAmt = new BigDecimal(0);
			rptRow.chequeAmt = new BigDecimal(0);
			rptRow.pdChequeAmt = new BigDecimal(0);
			rptRow.termsAmt = new BigDecimal(0);
			rptRow.couponAmt = new BigDecimal(0);
			rptRow.otherAmt = new BigDecimal(0);
			rptRow.documentAmt = srObj.mTotalAmt.negate();
			rptRow.discountAmt = new BigDecimal(0);
			rptRow.tradeInAmt = new BigDecimal(0);
			rptRow.username = UserNut.getUserName(srObj.mUserIdUpdate);
			rptRow.vecDocLink = new Vector(DocLinkNut.getByTargetDoc(SalesReturnBean.TABLENAME, srObj.mPkid));
			for (int cnt4 = 0; cnt4 < rptRow.vecDocLink.size(); cnt4++)
			{
				DocLinkObject dlObj = (DocLinkObject) rptRow.vecDocLink.get(cnt4);
				if (dlObj.srcDocRef.equals(PaymentVoucherIndexBean.TABLENAME))
				{
					rptRow.cashAmt = srObj.mTotalAmt.negate();
				}
			}
			this.vecSalesReturn.add(rptRow);
		}
		this.generateTime = TimeFormat.getTimestamp();
	}

	public Timestamp getReportGenerateTime()
	{
		return this.generateTime;
	}

	public boolean getValidBranch()
	{
		return (this.branch != null);
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public void setBranch(Integer branchId) throws Exception
	{
		this.reportClean = false;
		if (branchId == null)
		{
			this.branch = null;
			return;
		}
		this.branch = BranchNut.getObject(branchId);
		if (branch == null)
		{
			throw new Exception(" Failed to locate the branch!!");
		}
	}

	public String getDateFrom(String str)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public Timestamp getDateFrom()
	{
		return this.dateFrom;
	}

	public void setDateFrom(Timestamp newDate)
	{
		this.dateFrom = newDate;
	}

	public String getDateTo(String str)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public Timestamp getDateTo()
	{
		return this.dateTo;
	}

	public void setDateTo(Timestamp newDate)
	{
		this.dateTo = newDate;
	}

	public void setDate(Timestamp dateFrom, Timestamp dateTo)
	{
		this.reportClean = false;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.generateReport();
	}

	public void setDate(Timestamp newDate)
	{
		this.reportClean = false;
		this.dateFrom = newDate;
		this.dateTo = newDate;
		this.generateReport();
	}
	public static class ReportRow extends Object implements Serializable
	{
		public int position;
		public String description;
		public int customerId;
		public Timestamp docDate;
		public long stmtNo;
		public long documentPkid;
		public String branchCode;
		public String paymentInfo;
		public BigDecimal cashAmt;
		public BigDecimal cardAmt;
		public BigDecimal chequeAmt;
		public BigDecimal pdChequeAmt;
		public BigDecimal termsAmt;
		public BigDecimal couponAmt; // not used
		public BigDecimal otherAmt; // not used
		public BigDecimal documentAmt;
		public BigDecimal outstandingAmt; // different from outstanding amt
		public BigDecimal discountAmt;
		public BigDecimal tradeInAmt;
		public String username;
		public Vector vecDocLink;

		public ReportRow()
		{
			this.position = 0;
			this.description = "";
			this.customerId = 0;
			this.docDate = TimeFormat.getTimestamp();
			this.stmtNo = 0;
			this.documentPkid = 0;
			this.branchCode = "";
			this.paymentInfo = "";
			this.cashAmt = new BigDecimal(0);
			this.cardAmt = new BigDecimal(0);
			this.chequeAmt = new BigDecimal(0);
			this.pdChequeAmt = new BigDecimal(0);
			this.termsAmt = new BigDecimal(0);
			this.couponAmt = new BigDecimal(0);
			this.otherAmt = new BigDecimal(0);
			this.documentAmt = new BigDecimal(0);
			this.discountAmt = new BigDecimal(0);
			this.tradeInAmt = new BigDecimal(0);
			this.username = "";
			this.vecDocLink = new Vector();
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.reports;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

public class DailySalesReportType02Session extends java.lang.Object implements Serializable
{
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state = null;
	private BranchObject branch = null;
	// private Timestamp date;
	private Timestamp dateFrom;
	private Timestamp dateTo;
	private boolean reportClean = false;
	public Vector vecSalesCollection = null;
	public Vector vecPrevCollection = null;
	public Vector vecSalesReturn = null;
	public Vector vecDeposit = null;
	public String reportBranch = "";
	public String reportDateFrom = "";
	public String reportDateTo = "";
	public Timestamp generateTime = TimeFormat.getTimestamp();

	// / contructor!
	public DailySalesReportType02Session() throws Exception
	{
		this.reportClean = false;
		this.dateFrom = TimeFormat.getTimestamp();
		this.dateFrom = TimeFormat.add(this.dateFrom, 0, 0, -1);
		this.dateFrom = TimeFormat.add(this.dateFrom, 0, 0, 1);
		this.dateTo = this.dateFrom;
		this.vecSalesCollection = new Vector();
		this.vecPrevCollection = new Vector();
		this.vecDeposit = new Vector();
		this.vecSalesReturn = new Vector();
	}

	public void reset()
	{
		this.branch = null;
	}

	public void generateReport()
	{
		// / check if other parameters are OK
		this.reportClean = true;
		this.vecSalesCollection.clear();
		this.vecPrevCollection.clear();
		this.vecDeposit.clear();
		this.vecSalesReturn.clear();
		// // set the report info
		if (this.branch != null)
		{
			this.reportBranch = this.branch.description + " " + this.branch.name;
		} else
		{
			this.reportBranch = " ALL BRANCHES ";
		}
		this.reportDateFrom = TimeFormat.strDisplayDate(this.dateFrom);
		this.reportDateTo = TimeFormat.strDisplayDate(this.dateTo);
		// /////////////////////////////////////////////////////////////////////////////
		// / step 1:
		// / find all the date's invoices and cashsale
		Timestamp dayAfter = TimeFormat.add(this.dateTo, 0, 0, 1);
		QueryObject query1 = null;
		if (this.branch != null)
		{
			query1 = new QueryObject(new String[] {
					InvoiceBean.TIMEISSUED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					InvoiceBean.TIMEISSUED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					InvoiceBean.CUST_SVCCTR_ID + "= '" + this.branch.pkid.toString() + "' " });
		} else
		{
			query1 = new QueryObject(new String[] {
					InvoiceBean.TIMEISSUED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					InvoiceBean.TIMEISSUED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' " });
		}
		query1.setOrder(" ORDER BY " + InvoiceBean.STMT_NO + ", " + InvoiceBean.PKID);
		Vector vecInvoices = new Vector(InvoiceNut.getObjects(query1));
		for (int cnt1 = 0; cnt1 < vecInvoices.size(); cnt1++)
		{
			InvoiceObject invObj = (InvoiceObject) vecInvoices.get(cnt1);
			BranchObject branchObj = BranchNut.getObject(invObj.mCustSvcCtrId);
			ReportRow rptRow = new ReportRow();
			rptRow.position = cnt1 + 1;
			rptRow.description = invObj.mForeignText + " " + invObj.mEntityName + " " + invObj.mEntityContactPerson;
			rptRow.customerId = invObj.mEntityKey.intValue();
			if (rptRow.customerId == CustAccountBean.PKID_CASH.intValue())
			{
				rptRow.description = "CashSale:" + invObj.mEntityName;
			}
			// if(!invObj.mEntityName.equals("Cash"))
			// { rptRow.description = rptRow.description + " " +
			// invObj.mEntityName;}
			rptRow.docDate = invObj.mTimeIssued;
			rptRow.stmtNo = invObj.mStmtNumber.longValue();
			rptRow.documentPkid = invObj.mPkid.longValue();
			rptRow.branchCode = branchObj.code;
			rptRow.paymentInfo = "";
			rptRow.documentAmt = invObj.mTotalAmt;
			rptRow.username = UserNut.getUserName(invObj.mUserIdUpdate);
			Vector vecDocLink = new Vector(DocLinkNut.getByTargetDoc(InvoiceBean.TABLENAME, invObj.mPkid));
			rptRow.vecDocLink = vecDocLink;
			for (int cnt2 = 0; cnt2 < vecDocLink.size(); cnt2++)
			{
				DocLinkObject dlObj = (DocLinkObject) vecDocLink.get(cnt2);
				if (dlObj.srcDocRef.equals(OfficialReceiptBean.TABLENAME))
				{
					OfficialReceiptObject orObj = OfficialReceiptNut.getObject(dlObj.srcDocId);
					// // check for same date
					Timestamp rctNextDay = TimeFormat.add(orObj.paymentTime, 0, 0, 1);
					Timestamp invNextDay = TimeFormat.add(invObj.mTimeIssued, 0, 0, 1);
					// if(rctNextDay.getTime()!=invNextDay.getTime())
					// { continue;}
					//if(rctNextDay.getTime() > dayAfter.getTime() || orObj.paymentTime.getTime() < this.dateFrom.getTime() )
					if(rctNextDay.getTime() > dayAfter.getTime() )
					{
						continue;
					}
					if (this.branch != null && !this.branch.pkid.equals(orObj.branch))
					{
						continue;
					}
					if(orObj.state.equals(OfficialReceiptBean.ST_REVERSED))
					{
						continue;
					}
					rptRow.paymentInfo += " " + orObj.cardType + " " + orObj.paymentMethod + " "
							+ orObj.cardApprovalCode + " " + orObj.chequeNumber + " " + orObj.chequeNumberPD;

					if(orObj.paymentTime.getTime() < this.dateFrom.getTime())
					{
						rptRow.paymentInfo = " DEPOSIT (RCT" +orObj.pkid.toString()+") PAID ON "+TimeFormat.strDisplayDate(orObj.paymentTime)+" "+rptRow.paymentInfo;
						continue;
					}

					BigDecimal amtForThisInvoice = new BigDecimal(0);
					BigDecimal amtOfReceipt = new BigDecimal(0);
					Vector vecDocLinkRct = new Vector(DocLinkNut.getBySourceDoc(OfficialReceiptBean.TABLENAME,
							dlObj.srcDocId));
					for (int cnt3 = 0; cnt3 < vecDocLinkRct.size(); cnt3++)
					{
						DocLinkObject dlObj2 = (DocLinkObject) vecDocLinkRct.get(cnt3);
						// / must check the tgtDoc of this receipt
						// / must check the date of this receipt
						if (!dlObj2.tgtDocRef.equals(SalesOrderIndexBean.TABLENAME))
						{
							amtOfReceipt = amtOfReceipt.add(dlObj2.amount.abs());
						}
						if (dlObj2.tgtDocRef.equals(InvoiceBean.TABLENAME) && dlObj2.tgtDocId.equals(invObj.mPkid))
						{
							amtForThisInvoice = amtForThisInvoice.add(dlObj2.amount.abs());
						}
					}
					if (amtForThisInvoice.signum() > 0)
					{
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.cashAmt = rptRow.cashAmt.add(orObj.amountCash.multiply(amtForThisInvoice.divide(
									amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.cardAmt = rptRow.cardAmt.add(orObj.amountCard.multiply(amtForThisInvoice.divide(
									amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.chequeAmt = rptRow.chequeAmt.add(orObj.amountCheque.multiply(amtForThisInvoice
									.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.pdChequeAmt = rptRow.pdChequeAmt.add(orObj.amountPDCheque.multiply(amtForThisInvoice
									.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.couponAmt = rptRow.couponAmt.add(orObj.amountCoupon.multiply(amtForThisInvoice
									.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
						if (amtOfReceipt.signum() != 0)
						{
							rptRow.otherAmt = rptRow.otherAmt.add(orObj.amountOther.multiply(amtForThisInvoice.divide(
									amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
						}
					}
				}
			}
			Vector vecInvItem = InvoiceItemNut.getValueObjectsGiven(InvoiceItemBean.INVOICE_ID,
					invObj.mPkid.toString(), InvoiceItemBean.POS_ITEM_ID, POSItemBean.PKID_TRADEDISC.toString(),
					InvoiceItemBean.STATUS, InvoiceItemBean.STATUS_ACTIVE);
			for (int cnt = 0; cnt < vecInvItem.size(); cnt++)
			{
				InvoiceItemObject iiObj = (InvoiceItemObject) vecInvItem.get(cnt);
				rptRow.discountAmt = rptRow.discountAmt.add(iiObj.mUnitPriceQuoted.abs());
			}
			rptRow.termsAmt = rptRow.documentAmt.subtract(rptRow.cashAmt).subtract(rptRow.cardAmt).subtract(
					rptRow.chequeAmt).subtract(rptRow.pdChequeAmt).subtract(rptRow.couponAmt).subtract(rptRow.otherAmt);
			this.vecSalesCollection.add(rptRow);
		}
		// ////////////////////////////////////////////////////////////////////////
		// / step 2:
		// / load the DocLinkBean tgtDoc = Invoice in (1)
		// / and only add the amount if InvoiceID exist above
		// / step 3:
		// / find all the OfficialReceipt on the same day
		QueryObject query2 = null;
		if (this.branch != null)
		{
			query2 = new QueryObject(new String[] {
					OfficialReceiptBean.PAYMENT_TIME + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					OfficialReceiptBean.PAYMENT_TIME + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					OfficialReceiptBean.BRANCH + "= '" + this.branch.pkid.toString() + "' ",
					OfficialReceiptBean.STATE + "= '" + OfficialReceiptBean.ST_CREATED + "' " });
		} else
		{
			query2 = new QueryObject(new String[] {
					OfficialReceiptBean.PAYMENT_TIME + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					OfficialReceiptBean.PAYMENT_TIME + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					OfficialReceiptBean.STATE + "= '" + OfficialReceiptBean.ST_CREATED + "' " });
		}
		query2.setOrder(" ORDER BY " + OfficialReceiptBean.STMT_NO + ", " + OfficialReceiptBean.PKID);
		Vector vecReceipts = new Vector(OfficialReceiptNut.getObjects(query2));
		Log.printVerbose("................checkpoint.........11......");
		Log.printVerbose("................checkpoint.........11......");
		Log.printVerbose("................checkpoint.........11......");
		Log.printVerbose("...... SIZE OF VECTOR = "+vecReceipts.size());

		for (int cnt1 = 0; cnt1 < vecReceipts.size(); cnt1++)
		{
			OfficialReceiptObject orObj = (OfficialReceiptObject) vecReceipts.get(cnt1);
			BranchObject branchObj = BranchNut.getObject(orObj.branch);
			// find all DocLInk with srcDoc = this officialReceipt
			ReportRow rptRow = new ReportRow();
			rptRow.position = cnt1 + 1;
			rptRow.description = orObj.entityName;
			rptRow.branchCode = branchObj.code;
			rptRow.customerId = orObj.entityKey.intValue();
			rptRow.docDate = orObj.paymentTime;
			rptRow.stmtNo = orObj.stmtNumber.longValue();
			rptRow.documentPkid = orObj.pkid.longValue();
			rptRow.documentAmt = new BigDecimal(0);
			rptRow.username = UserNut.getUserName(orObj.userIdUpdate);
			Vector vecDocLink = new Vector(DocLinkNut.getBySourceDoc(OfficialReceiptBean.TABLENAME, orObj.pkid));
			rptRow.vecDocLink = vecDocLink;
			//// FOR DEPOSIT, THERE IS NO DOCLINK
			if(vecDocLink.size()==0)
			{
				rptRow.cashAmt = orObj.amountCash;
				rptRow.cardAmt = orObj.amountCard;
				rptRow.chequeAmt = orObj.amountCheque;
				rptRow.pdChequeAmt = orObj.amountPDCheque;
				rptRow.couponAmt = orObj.amountCoupon;
				rptRow.otherAmt = orObj.amountOther;
				rptRow.paymentInfo += "DEPOSIT";
			}

			//// FOR RECEIPTS LINK to OTHER PAYMENTS
			for (int cnt2 = 0; cnt2 < vecDocLink.size(); cnt2++)
			{
				DocLinkObject dlObj = (DocLinkObject) vecDocLink.get(cnt2);
				if (dlObj.tgtDocRef.equals(InvoiceBean.TABLENAME))
				{
					// // option 1: check for same date
					/*
					 * InvoiceObject invObj =
					 * InvoiceNut.getObject(dlObj.tgtDocId); Timestamp
					 * rctNextDay = TimeFormat.add(orObj.paymentTime,0,0,1);
					 * Timestamp invNextDay =
					 * TimeFormat.add(invObj.mTimeIssued,0,0,1);
					 * if(rctNextDay.getTime()==invNextDay.getTime()) {
					 * rptRow.vecDocLink.remove(cnt2); cnt2--; continue; }
					 */// / option 2: use the vecInvoices above to see if it
						// has be accounted for
					boolean existAbove = false;
					for (int cnt4 = 0; cnt4 < vecInvoices.size(); cnt4++)
					{
						InvoiceObject invObj4 = (InvoiceObject) vecInvoices.get(cnt4);
						if (dlObj.tgtDocId.equals(invObj4.mPkid))
						{
							existAbove = true;
						}
					}
					if (existAbove)
					{
						rptRow.vecDocLink.remove(cnt2);
						cnt2--;
						continue;
					}
				}
				if (dlObj.tgtDocRef.equals(SalesOrderIndexBean.TABLENAME))
				{
					rptRow.vecDocLink.remove(cnt2);
					cnt2--;
					continue;
				}
				BigDecimal amtForThisDocLink = dlObj.amount.abs();
				BigDecimal amtOfReceipt = orObj.amount.abs();
				if (amtForThisDocLink.signum() > 0)
				{
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.cashAmt = rptRow.cashAmt.add(orObj.amountCash.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.cardAmt = rptRow.cardAmt.add(orObj.amountCard.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.chequeAmt = rptRow.chequeAmt.add(orObj.amountCheque.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.pdChequeAmt = rptRow.pdChequeAmt.add(orObj.amountPDCheque.multiply(amtForThisDocLink
								.divide(amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.couponAmt = rptRow.couponAmt.add(orObj.amountCoupon.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
					if (amtOfReceipt.signum() != 0)
					{
						rptRow.otherAmt = rptRow.otherAmt.add(orObj.amountOther.multiply(amtForThisDocLink.divide(
								amtOfReceipt, 12, BigDecimal.ROUND_HALF_EVEN)));
					}
				}
			}// / end for doclink

			rptRow.paymentInfo += " " + orObj.cardType + " " + orObj.chequeNumber + " " + orObj.chequeNumberPD;
			// / only add this rptRow if the remaining amount greater than zero
			if(rptRow.cashAmt.signum() > 0 || rptRow.cardAmt.signum() > 0 || rptRow.chequeAmt.signum() > 0
					|| rptRow.pdChequeAmt.signum() > 0 || rptRow.couponAmt.signum() > 0 || rptRow.otherAmt.signum() > 0)
			{
				if(rptRow.vecDocLink.size()==0)
				{
					this.vecDeposit.add(rptRow);
				}
				else
				{	
					this.vecPrevCollection.add(rptRow);
				}
			}



		} // / end for vecReceipt
		// ////////////////////////////////////////////////////////////////////////////
		// / LOOK FOR SALES RETURN
		QueryObject query3 = null;
		if (this.branch != null)
		{
			query3 = new QueryObject(new String[] {
					SalesReturnBean.TIME_CREATED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					SalesReturnBean.TIME_CREATED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' ",
					SalesReturnBean.CUST_SVC_CTR_ID + "= '" + this.branch.pkid.toString() + "' " });
		} else
		{
			query3 = new QueryObject(new String[] {
					SalesReturnBean.TIME_CREATED + " >= '" + TimeFormat.strDisplayDate(this.dateFrom) + "' ",
					SalesReturnBean.TIME_CREATED + " < '" + TimeFormat.strDisplayDate(dayAfter) + "' " });
		}
		query3.setOrder(" ORDER BY " + SalesReturnBean.PKID + ", " + SalesReturnBean.STMT_NO);
		Vector vecSRObj = new Vector(SalesReturnNut.getObjects(query3));
		for (int cnt1 = 0; cnt1 < vecSRObj.size(); cnt1++)
		{
			SalesReturnObject srObj = (SalesReturnObject) vecSRObj.get(cnt1);
			BranchObject branchObj = BranchNut.getObject(srObj.mCustSvcCtrId);
			InvoiceObject invObj = InvoiceNut.getObject(srObj.mDocId);
			ReportRow rptRow = new ReportRow();
			rptRow.position = cnt1 + 1;
			rptRow.description = invObj.mEntityName + " " + invObj.mForeignText;
			rptRow.customerId = srObj.mCustAccId.intValue();
			if (rptRow.customerId == CustAccountBean.PKID_CASH.intValue())
			{
				rptRow.description = "CashSale:" + invObj.mEntityName;
			}
			rptRow.description += " Inv" + srObj.mDocId.toString() + " ";
			InvoiceObject invReturn = InvoiceNut.getObject(srObj.mDocId);
			if (invReturn != null)
			{
				rptRow.description += "(" + TimeFormat.strDisplayDate(invObj.mTimeIssued) + ")";
			}
			rptRow.customerId = srObj.mCustAccId.intValue();
			rptRow.docDate = srObj.mTimeCreated;
			rptRow.stmtNo = srObj.mStmtNumber.longValue();
			rptRow.documentPkid = srObj.mPkid.longValue();
			rptRow.branchCode = branchObj.code;
			rptRow.paymentInfo = srObj.mRemarks;
			rptRow.cashAmt = srObj.mTotalAmt.negate();
			if (srObj.mProcessDocTable.equals(CreditMemoIndexBean.TABLENAME))
			{
				rptRow.cashAmt = new BigDecimal(0);
			}
			// rptRow.cashAmt = new BigDecimal(0);
			rptRow.cardAmt = new BigDecimal(0);
			rptRow.chequeAmt = new BigDecimal(0);
			rptRow.pdChequeAmt = new BigDecimal(0);
			rptRow.termsAmt = new BigDecimal(0);
			rptRow.couponAmt = new BigDecimal(0);
			rptRow.otherAmt = new BigDecimal(0);
			rptRow.documentAmt = srObj.mTotalAmt.negate();
			rptRow.discountAmt = new BigDecimal(0);
			rptRow.tradeInAmt = new BigDecimal(0);
			rptRow.username = UserNut.getUserName(srObj.mUserIdUpdate);
			rptRow.vecDocLink = new Vector(DocLinkNut.getByTargetDoc(SalesReturnBean.TABLENAME, srObj.mPkid));
			for (int cnt4 = 0; cnt4 < rptRow.vecDocLink.size(); cnt4++)
			{
				DocLinkObject dlObj = (DocLinkObject) rptRow.vecDocLink.get(cnt4);
				if (dlObj.srcDocRef.equals(PaymentVoucherIndexBean.TABLENAME))
				{
					rptRow.cashAmt = srObj.mTotalAmt.negate();
				}
			}
			this.vecSalesReturn.add(rptRow);
		}
		this.generateTime = TimeFormat.getTimestamp();
	}

	public Timestamp getReportGenerateTime()
	{
		return this.generateTime;
	}

	public boolean getValidBranch()
	{
		return (this.branch != null);
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public void setBranch(Integer branchId) throws Exception
	{
		this.reportClean = false;
		if (branchId == null)
		{
			this.branch = null;
			return;
		}
		this.branch = BranchNut.getObject(branchId);
		if (branch == null)
		{
			throw new Exception(" Failed to locate the branch!!");
		}
	}

	public String getDateFrom(String str)
	{
		return TimeFormat.strDisplayDate(this.dateFrom);
	}

	public Timestamp getDateFrom()
	{
		return this.dateFrom;
	}

	public void setDateFrom(Timestamp newDate)
	{
		this.dateFrom = newDate;
	}

	public String getDateTo(String str)
	{
		return TimeFormat.strDisplayDate(this.dateTo);
	}

	public Timestamp getDateTo()
	{
		return this.dateTo;
	}

	public void setDateTo(Timestamp newDate)
	{
		this.dateTo = newDate;
	}

	public void setDate(Timestamp dateFrom, Timestamp dateTo)
	{
		this.reportClean = false;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.generateReport();
	}

	public void setDate(Timestamp newDate)
	{
		this.reportClean = false;
		this.dateFrom = newDate;
		this.dateTo = newDate;
		this.generateReport();
	}
	public static class ReportRow extends Object implements Serializable
	{
		public int position;
		public String description;
		public int customerId;
		public Timestamp docDate;
		public long stmtNo;
		public long documentPkid;
		public String branchCode;
		public String paymentInfo;
		public BigDecimal cashAmt;
		public BigDecimal cardAmt;
		public BigDecimal chequeAmt;
		public BigDecimal pdChequeAmt;
		public BigDecimal termsAmt;
		public BigDecimal couponAmt; // not used
		public BigDecimal otherAmt; // not used
		public BigDecimal documentAmt;
		public BigDecimal outstandingAmt; // different from outstanding amt
		public BigDecimal discountAmt;
		public BigDecimal tradeInAmt;
		public String username;
		public Vector vecDocLink;

		public ReportRow()
		{
			this.position = 0;
			this.description = "";
			this.customerId = 0;
			this.docDate = TimeFormat.getTimestamp();
			this.stmtNo = 0;
			this.documentPkid = 0;
			this.branchCode = "";
			this.paymentInfo = "";
			this.cashAmt = new BigDecimal(0);
			this.cardAmt = new BigDecimal(0);
			this.chequeAmt = new BigDecimal(0);
			this.pdChequeAmt = new BigDecimal(0);
			this.termsAmt = new BigDecimal(0);
			this.couponAmt = new BigDecimal(0);
			this.otherAmt = new BigDecimal(0);
			this.documentAmt = new BigDecimal(0);
			this.discountAmt = new BigDecimal(0);
			this.tradeInAmt = new BigDecimal(0);
			this.username = "";
			this.vecDocLink = new Vector();
		}
	}
}
