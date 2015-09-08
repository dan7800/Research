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
package com.vlee.ejb.accounting;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.apache.tomcat.util.buf.TimeStamp;

import com.vlee.bean.application.AppConfigManager;
import com.vlee.ejb.customer.CashRebateVoucherBean;
import com.vlee.ejb.customer.CashRebateVoucherObject;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.customer.CustAccountNut;
import com.vlee.ejb.customer.CustAccountObject;
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.customer.SalesReturnBean;
import com.vlee.ejb.customer.SalesReturnNut;
import com.vlee.ejb.customer.SalesReturnObject;
import com.vlee.ejb.inventory.RMATicketNut;
import com.vlee.ejb.inventory.RMATicketObject;
import com.vlee.ejb.supplier.GoodsReceivedNoteBean;
import com.vlee.ejb.supplier.GoodsReceivedNoteObject;
import com.vlee.ejb.supplier.SuppAccountBean;
import com.vlee.ejb.supplier.SuppInvoiceObject;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.TimeFormat;

public class JournalTxnLogic
{
	public static Integer DEFAULT_BATCH = BatchBean.PKID_DEFAULT;

	// // Customer Invoice Object to Journal
	// // Debit Credit
	// // accReceivable 
	// // generalSales 
	// // COGS 
	// // inventoryStock 
	public static void fnCreate(InvoiceObject invObj) throws Exception
	{
		NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME, invObj.mPCCenter,
				invObj.mEntityKey, invObj.mCurrency);
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// code = "not_used";
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = invObj.mEntityKey;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			naObj.currency = invObj.mCurrency;
			naObj.amount = new BigDecimal(0);
			naObj.remarks = invObj.mRemarks;
			naObj.accPCCenterId = invObj.mPCCenter;
			naObj.userIdUpdate = invObj.mUserIdUpdate;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			naObj = naEJB.getObject();
		}
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = InvoiceBean.TABLENAME;
		natObj.foreignKey = invObj.mPkid;
		natObj.code = "not_used";
		natObj.info1 = " ";
		// natObj.description = " ";
		natObj.description = "Sales. INV:" + invObj.mPkid.toString() + " REF:" + invObj.mReferenceNo + " RMKS:"
				+ invObj.mRemarks;
		natObj.txnType = " ";
		natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
		natObj.currency = invObj.mCurrency;
		natObj.amount = invObj.mTotalAmt;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = invObj.mTimeIssued;
		natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
		natObj.timeParam2 = invObj.mTimeIssued;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = invObj.mUserIdUpdate;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		// /////////////////////////////////////////////////////////////////////
		BigDecimal cogs = InvoiceNut.getCostOfGoodsSold(invObj.mPkid);
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 2 ");
		if (cogs.signum() == 0 && invObj.mTotalAmt.signum() == 0)
		{
			return;
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 3 ");
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject genSalesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		// 20080312 Jimmy
		GeneralLedgerObject glCodeLdgObj = new GeneralLedgerObject();
		String defaultGLCodeTax = AppConfigManager.getProperty("TAX-DEFAULT-GL-CODE");
		GLCode glObj = GLCodeNut.getObjectByCode(defaultGLCodeTax);
		if (glObj != null) {
			glCodeLdgObj = GeneralLedgerNut.getObjectFailSafe(defaultGLCodeTax, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		}
		// --------------
		
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 4 ");
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = invObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Invoice To Customer";
		jttObj.description = "Inv - Cust:" + invObj.mForeignText + " " + invObj.mEntityName + " ("
				+ invObj.mEntityKey.toString() + ") REF:" + invObj.mReferenceNo;
		jttObj.amount = invObj.mTotalAmt;
		jttObj.transactionDate = invObj.mTimeIssued;
		jttObj.docRef = InvoiceBean.TABLENAME;
		jttObj.docKey = invObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = invObj.mUserIdUpdate;
		jttObj.userIdEdit = invObj.mUserIdUpdate;
		jttObj.userIdCancel = invObj.mUserIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 5 ");
		if (invObj.mTotalAmt.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.ACC_RECEIVABLE;
			jeObj.currency = invObj.mCurrency;
			
			//jeObj.amount = invObj.getAmountStd();
			jeObj.amount = invObj.mTotalAmt;;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 6 ");
		if (invObj.getAmountNet().signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genSalesLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.GENERAL_SALES;
			jeObj.currency = invObj.mCurrency;
			
			//jeObj.amount = invObj.mTotalAmt.negate();
			jeObj.amount = invObj.mTotalAmt.subtract(invObj.getAmountTax()).negate();
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 7 ");
		// / use inventory cost instead of invoice amount for the following:
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY_COST;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 8 ");
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs.negate();
			jttObj.vecEntry.add(jeObj);
		}
		// 20080311 Jimmy - add GLCode for Tax
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 9 ");
			
		if (glObj != null && invObj.getAmountTax().signum() !=0){
				Log.printVerbose("glObj : " + glObj.toString());
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = glCodeLdgObj.pkId;
				jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
						+ ") " + defaultGLCodeTax;
				jeObj.currency = invObj.mCurrency;
				jeObj.amount = invObj.getAmountTax().negate();
				jttObj.vecEntry.add(jeObj);		
				
		}
		
		// ---------------------------------
		
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 10 ");
		jttObj = JournalTransactionNut.fnCreate(jttObj);
		
		
	
	}

	// / Customer Receipt Object
	// // Debit Credit
	// // cash 
	// // accReceivable 
	// /
	public static void fnCreate(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
			
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Collection from Customer ";
		jttObj.description = "Rct - Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.paymentTime;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		if (rctObj.amount.signum() != 0)
		{
			// 20080606 Jimmy
			if (rctObj.vecEntry.size() == 0)
			{
				GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
						rctObj.pcCenter, rctObj.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = creditLdgObj.pkId;
				jeObj.description = jttObj.description;
				if (rctObj.amountCash.signum() != 0)
				{
					jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
				}
				if (rctObj.amountCard.signum() != 0)
				{
					jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
				}
				if (rctObj.amountCheque.signum() != 0)
				{
					jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
				}
				if (rctObj.amountPDCheque.signum() != 0)
				{
					jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
				}
				jeObj.currency = rctObj.currency;
				jeObj.amount = rctObj.amount.negate();
				jttObj.vecEntry.add(jeObj);
			} else {
				// add ReceiptItem to JournalEntry
				for (int cnt1 = 0; cnt1 < rctObj.vecEntry.size(); cnt1++)
				{
					OfficialReceiptItemObject receiptItemObj = (OfficialReceiptItemObject) rctObj.vecEntry.get(cnt1);
					GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(receiptItemObj.glCodeCredit, DEFAULT_BATCH,
							rctObj.pcCenter, rctObj.currency);
					
					JournalEntryObject jeObj = new JournalEntryObject();
					jeObj.glId = creditLdgObj.pkId;
					jeObj.description = jttObj.description;
					jeObj.currency = receiptItemObj.currency;
					jeObj.amount = receiptItemObj.amount.negate();
					jttObj.vecEntry.add(jeObj);					
				}
			}
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null && rctObj.amountCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther;
			jttObj.vecEntry.add(jeObj);
	}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnReverse(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
				rctObj.pcCenter, rctObj.currency);
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			if(rctObj.chequeBankInCb.intValue() > 0)
			{ cbChequeObj = CashAccountNut.getObject(rctObj.chequeBankInCb); }
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			if(rctObj.pdChequeBankInCb.intValue() > 0)
			{ cbPDChequeObj = CashAccountNut.getObject(rctObj.pdChequeBankInCb); }

			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "REVERSE RECEIPT";
		jttObj.description = "Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.paymentTime;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = rctObj.lastUpdate;
		jttObj.timestampEdit = rctObj.lastUpdate;
		jttObj.timestampCancel = rctObj.lastUpdate;
		if (rctObj.amount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			if (rctObj.amountCash.signum() != 0)
			{
				jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
			}
			if (rctObj.amountCard.signum() != 0)
			{
				jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
			}
			if (rctObj.amountCheque.signum() != 0)
			{
				jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
			}
			if (rctObj.amountPDCheque.signum() != 0)
			{
				jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
			}
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null
				&& rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Credit/Debit Note
	public static void fnCreate(GenericStmtObject gsObj) throws Exception
	{
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeDebit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeCredit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = gsObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "DN / CN to Customer ";
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE))
		{
			jttObj.name = " Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE))
		{
			jttObj.name = " Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_DEBIT_NOTE))
		{
			jttObj.name = " Received Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_CREDIT_NOTE))
		{
			jttObj.name = " Received Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT))
		{
			jttObj.name = " Reverse Payment ";
		}
		jttObj.description = "Cust:" + gsObj.entityName + " (" + gsObj.foreignEntityKey.toString() + ") ";
		jttObj.amount = gsObj.amount;
		jttObj.transactionDate = gsObj.dateStmt;
		jttObj.docRef = GenericStmtBean.TABLENAME;
		jttObj.docKey = gsObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = gsObj.userIdUpdate;
		jttObj.userIdEdit = gsObj.userIdUpdate;
		jttObj.userIdCancel = gsObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = creditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Sales Return
	// // Debit Credit
	// // generalSalesReturn 
	// // acc receivable 
	// // inventory 
	// // COGS 
	public static void fnCreate(SalesReturnObject srObj) throws Exception
	{
		BigDecimal cogr = SalesReturnNut.getCostOfGoodsReturned(srObj.mPkid);
		// SELECT SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta
		// WHERE doc_table='cust_sales_return_item' AND doc_key IN (SELECT pkid
		// FROM cust_sales_return_item WHERE sales_return_id='1005');
		BranchObject branch = BranchNut.getObject(srObj.mCustSvcCtrId);
		CustAccountObject custObj = CustAccountNut.getObject(srObj.mCustAccId);
		GeneralLedgerObject srLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES_RETURN,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = branch.accPCCenterId;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Sales Return ";
		jttObj.description = "Customer:" + custObj.name + " (" + custObj.pkid.toString() + ") ";
		jttObj.amount = srObj.mTotalAmt;
		jttObj.transactionDate = srObj.mTimeCreated;
		jttObj.docRef = SalesReturnBean.TABLENAME;
		jttObj.docKey = srObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = srObj.mApproverId;
		jttObj.userIdEdit = srObj.mApproverId;
		jttObj.userIdCancel = srObj.mApproverId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = srLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt.negate();
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnCreate(PaymentVoucherIndexObject pvObj) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Payment Voucher";
		jttObj.description = "Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal.negate();
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount;
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnReverse(PaymentVoucherIndexObject pvObj, Integer userId) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " REVERSE PAYMENT VOUCHER ";
		jttObj.description = "REVERSE Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount.negate();
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Supplier Invoice / Goods Received Note
	// // Debit Credit
	// // inventoryStock 
	// // accPayable 
	public static void fnCreate(SuppInvoiceObject sinvObj, GoodsReceivedNoteObject grnObj) throws Exception
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		GeneralLedgerObject accPayLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = sinvObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " GRN (Purchase) ";
		jttObj.description = "Supplier:" + grnObj.mEntityName + "(" + grnObj.mEntityKey + ")" + " REF:"
				+ grnObj.mReferenceNo;
		jttObj.amount = grnObj.mAmount;
		jttObj.transactionDate = grnObj.mTimeComplete;
		jttObj.docRef = GoodsReceivedNoteBean.TABLENAME;
		jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = grnObj.mAcknowledgeId;
		jttObj.userIdEdit = grnObj.mAcknowledgeId;
		jttObj.userIdCancel = grnObj.mAcknowledgeId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accPayLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnCreate(CashRebateVoucherObject crvObj) throws Exception
	{
		BranchObject branch = BranchNut.getObject(crvObj.branch);
		CashAccountObject caObj = CashAccountNut.getObject(branch.cashbookCoupon);
		GeneralLedgerObject crvExpenseLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CRV_EXPENSE,
				DEFAULT_BATCH, crvObj.pcCenter, branch.currency);
		GeneralLedgerObject crvBookLdgObj = GeneralLedgerNut.getObjectFailSafe(caObj.accountType, DEFAULT_BATCH,
				crvObj.pcCenter, branch.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = crvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CRV For " + crvObj.nameDisplay + " (" + crvObj.cardNo + ")";
		jttObj.description = " CREATED CRV";
		jttObj.amount = crvObj.voucherValue;
		jttObj.transactionDate = crvObj.dateCreate;
		jttObj.docRef = CashRebateVoucherBean.TABLENAME;
		jttObj.docKey = crvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = crvObj.userCreate;
		jttObj.userIdEdit = crvObj.userCreate;
		// jttObj.userIdCancel= new
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvExpenseLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvBookLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	/*
	 * ///////////////////////////////////////////////////////////////// ///
	 * Purchase Return and Corresponding Credit Memo /// debit accPayable xxxxx1
	 * /// credit purchaseReturn xxxxx2 /// credit inventory xxxxx3 (by cogr)
	 * 
	 * //// xxxx2 = xxxxx3 - xxxx1
	 * 
	 * public static void fnCreate(PurchaseReturnIndexObject prObj) throws
	 * Exception { BigDecimal cogr =
	 * PurchaseReturnIndexNut.getCostOfGoodsReturned(prObj.pkid); //SELECT
	 * SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta WHERE
	 * doc_table='supp_purchase_return_item' AND doc_key IN (SELECT pkid FROM
	 * supp_purchase_return_item WHERE index_id='1003'); // BranchObject branch =
	 * BranchNut.getObject(prObj.branch); SuppAccountObject suppObj =
	 * SuppAccountNut.getObject(prObj.entityId);
	 * 
	 * GeneralLedgerObject accPayLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * cogsLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * inventoryLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency);
	 * 
	 * BigDecimal cogs = cogr.negate().subtract(prObj.amount);
	 * 
	 * JournalTransactionObject jttObj = new JournalTransactionObject();
	 * jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO; jttObj.pcCenterId =
	 * prObj.pcCenter; jttObj.batchId = DEFAULT_BATCH; // jttObj.typeId = //
	 * leave it as default jttObj.status = JournalTransactionBean.ACTIVE;
	 * jttObj.name = "Purchase Return "; jttObj.description = "
	 * Supplier:"+suppObj.name+" ("+suppObj.pkid.toString()+") "; jttObj.amount =
	 * prObj.amount; jttObj.transactionDate = prObj.timeComplete; jttObj.docRef =
	 * PurchaseReturnIndexBean.TABLENAME; jttObj.docKey = prObj.pkid;
	 * jttObj.state = JournalTransactionBean.STATE_CREATED; jttObj.userIdCreate =
	 * prObj.userIdUpdate; jttObj.userIdEdit = prObj.userIdUpdate;
	 * jttObj.userIdCancel= prObj.userIdUpdate; jttObj.timestampCreate =
	 * TimeFormat.getTimestamp(); jttObj.timestampEdit =
	 * TimeFormat.getTimestamp(); jttObj.timestampCancel =
	 * TimeFormat.getTimestamp();
	 *  { JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * accPayLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = prObj.amount; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * cogsLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = cogs; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * inventoryLdgObj.pkId; jeObj.description = jttObj.description;
	 * jeObj.currency = prObj.currency; jeObj.amount = cogr;
	 * jttObj.vecEntry.add(jeObj); } jttObj =
	 * JournalTransactionNut.fnCreate(jttObj); }
	 * 
	 */
	// / CreditMemo
	// / debit acc-payable
	// / credit purchase-return
	// / Cash Transfer
	public static void fnCreate(CashTransferItemObject ctiObj) throws Exception
	{
		CashAccountObject cashbookFrom = CashAccountNut.getObject(ctiObj.cashbookFrom);
		CashAccountObject cashbookTo = CashAccountNut.getObject(ctiObj.cashbookTo);
		GeneralLedgerObject fromCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookFrom.accountType,
				DEFAULT_BATCH, ctiObj.pcCenter, ctiObj.currency);
		GeneralLedgerObject toCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				ctiObj.pcCenter, ctiObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = ctiObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Internal Cash Transfer ";
		jttObj.description = " CashTransfer: From " + cashbookFrom.name + " " + cashbookFrom.code + " "
				+ cashbookFrom.accountNumber + " to " + cashbookTo.name + " " + cashbookTo.code + " "
				+ cashbookTo.accountNumber;
		jttObj.amount = ctiObj.amount;
		jttObj.transactionDate = ctiObj.dateStmt;
		jttObj.docRef = CashTransferItemBean.TABLENAME;
		jttObj.docKey = ctiObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = ctiObj.userPic;
		jttObj.userIdEdit = ctiObj.userPic;
		jttObj.userIdCancel = ctiObj.userPic;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = toCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = fromCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Bank In Slip
	public static void fnCreate(BankInSlipObject bisObj) throws Exception
	{
		CashAccountObject cashbookTo = CashAccountNut.getObject(bisObj.cashbook);
		CashAccountObject cashbookCash = null;
		CashAccountObject cashbookCheque1 = null;
		CashAccountObject cashbookCheque2 = null;
		CashAccountObject cashbookCheque3 = null;
		CashAccountObject cashbookCheque4 = null;
		CashAccountObject cashbookCheque5 = null;
		if (bisObj.cashCashbook.intValue() > 0)
		{
			cashbookCash = CashAccountNut.getObject(bisObj.cashCashbook);
		}
		if (bisObj.row1Cashbook.intValue() > 0)
		{
			cashbookCheque1 = CashAccountNut.getObject(bisObj.row1Cashbook);
		}
		if (bisObj.row2Cashbook.intValue() > 0)
		{
			cashbookCheque2 = CashAccountNut.getObject(bisObj.row2Cashbook);
		}
		if (bisObj.row3Cashbook.intValue() > 0)
		{
			cashbookCheque3 = CashAccountNut.getObject(bisObj.row3Cashbook);
		}
		if (bisObj.row4Cashbook.intValue() > 0)
		{
			cashbookCheque4 = CashAccountNut.getObject(bisObj.row4Cashbook);
		}
		if (bisObj.row5Cashbook.intValue() > 0)
		{
			cashbookCheque5 = CashAccountNut.getObject(bisObj.row5Cashbook);
		}
		GeneralLedgerObject cashToLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				bisObj.pcCenter, bisObj.currency);
		GeneralLedgerObject cashLdgObj = null;
		if (cashbookCash != null)
		{
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCash.accountType, DEFAULT_BATCH, bisObj.pcCenter,
					bisObj.currency);
		}
		GeneralLedgerObject cheque1LdgObj = null;
		if (cashbookCheque1 != null)
		{
			cheque1LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque1.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque2LdgObj = null;
		if (cashbookCheque2 != null)
		{
			cheque2LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque2.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque3LdgObj = null;
		if (cashbookCheque3 != null)
		{
			cheque3LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque3.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque4LdgObj = null;
		if (cashbookCheque4 != null)
		{
			cheque4LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque4.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque5LdgObj = null;
		if (cashbookCheque5 != null)
		{
			cheque5LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque5.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = bisObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Bank In Slip ";
		jttObj.description = " BankInSlip: " + bisObj.txDesc + " " + bisObj.bankCode + " " + bisObj.bankName + " "
				+ bisObj.accNumber;
		jttObj.amount = bisObj.amountTotal;
		jttObj.transactionDate = bisObj.txDate;
		jttObj.docRef = BankInSlipBean.TABLENAME;
		jttObj.docKey = bisObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = bisObj.userIdCreate;
		jttObj.userIdEdit = bisObj.userIdCreate;
		jttObj.userIdCancel = bisObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashToLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.cashAmount.signum() != 0 && cashLdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.cashAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row1Amount.signum() != 0 && cheque1LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque1LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row1Amount.subtract(bisObj.row1Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row2Amount.signum() != 0 && cheque2LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque2LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row2Amount.subtract(bisObj.row2Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row3Amount.signum() != 0 && cheque3LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque3LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row3Amount.subtract(bisObj.row3Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row4Amount.signum() != 0 && cheque4LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque4LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row4Amount.subtract(bisObj.row4Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row5Amount.signum() != 0 && cheque5LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque5LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row5Amount.subtract(bisObj.row5Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Credit Memo
	public static void fnCreate(CreditMemoIndexObject valObj) throws Exception
	{
		GeneralLedgerObject nominalLdgObj = null;
		if (valObj.entityTable.equals(SuppAccountBean.TABLENAME))
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH, valObj.pcCenter,
					valObj.currency);
		} else
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
					valObj.pcCenter, valObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = valObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CreditMemo ";
		if(valObj.memoType.equals(CreditMemoIndexBean.MEMO_TYPE_CUSTOMER_DEBIT_NOTE) || valObj.memoType.equals(CreditMemoIndexBean.MEMO_TYPE_SUPPLIER_DEBIT_NOTE))
		{
			jttObj.description = "DN - To:" + valObj.entityName + "(" + valObj.entityKey + ")" + valObj.docReference+" "+ valObj.docRemarks+ " "+valObj.memoRemarks + "  " + valObj.docRemarks;
		}
		else if(valObj.memoType.equals(CreditMemoIndexBean.MEMO_TYPE_CUSTOMER_CREDIT_NOTE) || valObj.memoType.equals(CreditMemoIndexBean.MEMO_TYPE_SUPPLIER_CREDIT_NOTE))
		{
			jttObj.description = "CN - To:" + valObj.entityName + "(" + valObj.entityKey + ")" + valObj.docReference+" "+ valObj.docRemarks+ " "+valObj.memoRemarks + "  " + valObj.docRemarks;
		}
		
		jttObj.amount = valObj.amount.abs();
		jttObj.transactionDate = valObj.timeCreate;
		jttObj.docRef = CreditMemoIndexBean.TABLENAME;
		jttObj.docKey = valObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = valObj.userIdCreate;
		jttObj.userIdEdit = valObj.userIdCreate;
		jttObj.userIdCancel = valObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = nominalLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = valObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt = 0; cnt < valObj.vecItems.size(); cnt++)
		{
			String theGLCode = "";
			CreditMemoItemObject cmiObj = (CreditMemoItemObject) valObj.vecItems.get(cnt);
			GeneralLedgerObject genLdgObj = null;
			if (cmiObj.amount.signum() > 0)
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeDebit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			} else
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeCredit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			}
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = cmiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj.failSafe();
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	//20080505 Jimmy
	public static void fnCreateStockVariance(String glCode, Integer pcCenter, String currency, BigDecimal variance,
			String description, String reason, Integer userId)
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				pcCenter, currency);
		GeneralLedgerObject inventoryVarLdgObj = GeneralLedgerNut.getObjectFailSafe(glCode,
				DEFAULT_BATCH, pcCenter, currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Reset Moving Average Stock Value ";
		jttObj.description = "DESC:" + description + " REASON:" + reason;
		jttObj.amount = variance.abs();
		jttObj.transactionDate = TimeFormat.getTimestamp();
		// jttObj.docRef =
		// jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = userId;
		jttObj.userIdEdit = userId;
		jttObj.userIdCancel = userId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryVarLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}
	// / Receipt Voucher
	// / Marketing Fund (thunderMatch) for Stock Gain/ Stock Loss
	public static void fnCreateStockVariance(Integer pcCenter, String currency, BigDecimal variance,
			String description, String reason, Integer userId)
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				pcCenter, currency);
		GeneralLedgerObject inventoryVarLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_VARIANCE,
				DEFAULT_BATCH, pcCenter, currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Reset Moving Average Stock Value ";
		jttObj.description = "DESC:" + description + " REASON:" + reason;
		jttObj.amount = variance.abs();
		jttObj.transactionDate = TimeFormat.getTimestamp();
		// jttObj.docRef =
		// jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = userId;
		jttObj.userIdEdit = userId;
		jttObj.userIdCancel = userId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryVarLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}
	
	public static void fnCreateStockVariance(Integer pcCenter, String currency, BigDecimal variance,
			String description, String reason, Integer userId, Timestamp transactionDate)
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				pcCenter, currency);
		GeneralLedgerObject inventoryVarLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_VARIANCE,
				DEFAULT_BATCH, pcCenter, currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Reset Moving Average Stock Value ";
		jttObj.description = "DESC:" + description + " REASON:" + reason;
		jttObj.amount = variance.abs();
		jttObj.transactionDate = transactionDate;
		// jttObj.docRef =
		// jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = userId;
		jttObj.userIdEdit = userId;
		jttObj.userIdCancel = userId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryVarLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
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
package com.vlee.ejb.accounting;

import java.math.BigDecimal;

import com.vlee.ejb.customer.CashRebateVoucherBean;
import com.vlee.ejb.customer.CashRebateVoucherObject;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.customer.CustAccountNut;
import com.vlee.ejb.customer.CustAccountObject;
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.customer.SalesReturnBean;
import com.vlee.ejb.customer.SalesReturnNut;
import com.vlee.ejb.customer.SalesReturnObject;
import com.vlee.ejb.supplier.GoodsReceivedNoteBean;
import com.vlee.ejb.supplier.GoodsReceivedNoteObject;
import com.vlee.ejb.supplier.SuppAccountBean;
import com.vlee.ejb.supplier.SuppInvoiceObject;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.TimeFormat;

public class JournalTxnLogic
{
	public static Integer DEFAULT_BATCH = BatchBean.PKID_DEFAULT;

	// // Customer Invoice Object to Journal
	// // Debit Credit
	// // accReceivable 
	// // generalSales 
	// // COGS 
	// // inventoryStock 
	public static void fnCreate(InvoiceObject invObj) throws Exception
	{
		NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME, invObj.mPCCenter,
				invObj.mEntityKey, invObj.mCurrency);
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// code = "not_used";
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = invObj.mEntityKey;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			naObj.currency = invObj.mCurrency;
			naObj.amount = new BigDecimal(0);
			naObj.remarks = invObj.mRemarks;
			naObj.accPCCenterId = invObj.mPCCenter;
			naObj.userIdUpdate = invObj.mUserIdUpdate;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			naObj = naEJB.getObject();
		}
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = InvoiceBean.TABLENAME;
		natObj.foreignKey = invObj.mPkid;
		natObj.code = "not_used";
		natObj.info1 = " ";
		// natObj.description = " ";
		natObj.description = "Sales. INV:" + invObj.mPkid.toString() + " REF:" + invObj.mReferenceNo + " RMKS:"
				+ invObj.mRemarks;
		natObj.txnType = " ";
		natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
		natObj.currency = invObj.mCurrency;
		natObj.amount = invObj.mTotalAmt;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = invObj.mTimeIssued;
		natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
		natObj.timeParam2 = invObj.mTimeIssued;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = invObj.mUserIdUpdate;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		// /////////////////////////////////////////////////////////////////////
		BigDecimal cogs = InvoiceNut.getCostOfGoodsSold(invObj.mPkid);
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 2 ");
		if (cogs.signum() == 0 && invObj.mTotalAmt.signum() == 0)
		{
			return;
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 3 ");
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject genSalesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 4 ");
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = invObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Invoice To Customer";
		jttObj.description = "Cust:" + invObj.mForeignText + " " + invObj.mEntityName + " ("
				+ invObj.mEntityKey.toString() + ") REF:" + invObj.mReferenceNo;
		jttObj.amount = invObj.mTotalAmt;
		jttObj.transactionDate = invObj.mTimeIssued;
		jttObj.docRef = InvoiceBean.TABLENAME;
		jttObj.docKey = invObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = invObj.mUserIdUpdate;
		jttObj.userIdEdit = invObj.mUserIdUpdate;
		jttObj.userIdCancel = invObj.mUserIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 5 ");
		if (invObj.mTotalAmt.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.GENERAL_SALES;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = invObj.mTotalAmt;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 6 ");
		if (invObj.mTotalAmt.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genSalesLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.ACC_RECEIVABLE;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = invObj.mTotalAmt.negate();
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 7 ");
		// / use inventory cost instead of invoice amount for the following:
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY_COST;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 8 ");
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs.negate();
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 9 ");
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Receipt Object
	// // Debit Credit
	// // cash 
	// // accReceivable 
	// /
	public static void fnCreate(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
		GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
				rctObj.pcCenter, rctObj.currency);
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Collection from Customer ";
		jttObj.description = "Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.paymentTime;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		if (rctObj.amount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = creditLdgObj.pkId;
			jeObj.description = jttObj.description;
			if (rctObj.amountCash.signum() != 0)
			{
				jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
			}
			if (rctObj.amountCard.signum() != 0)
			{
				jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
			}
			if (rctObj.amountCheque.signum() != 0)
			{
				jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
			}
			if (rctObj.amountPDCheque.signum() != 0)
			{
				jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
			}
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null && rctObj.amountCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnReverse(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
				rctObj.pcCenter, rctObj.currency);
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			if(rctObj.chequeBankInCb.intValue() > 0)
			{ cbChequeObj = CashAccountNut.getObject(rctObj.chequeBankInCb); }
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			if(rctObj.pdChequeBankInCb.intValue() > 0)
			{ cbPDChequeObj = CashAccountNut.getObject(rctObj.pdChequeBankInCb); }

			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "REVERSE RECEIPT";
		jttObj.description = "Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.lastUpdate;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = rctObj.lastUpdate;
		jttObj.timestampEdit = rctObj.lastUpdate;
		jttObj.timestampCancel = rctObj.lastUpdate;
		if (rctObj.amount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			if (rctObj.amountCash.signum() != 0)
			{
				jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
			}
			if (rctObj.amountCard.signum() != 0)
			{
				jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
			}
			if (rctObj.amountCheque.signum() != 0)
			{
				jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
			}
			if (rctObj.amountPDCheque.signum() != 0)
			{
				jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
			}
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null
				&& rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Credit/Debit Note
	public static void fnCreate(GenericStmtObject gsObj) throws Exception
	{
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeDebit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeCredit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = gsObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "DN / CN to Customer ";
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE))
		{
			jttObj.name = " Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE))
		{
			jttObj.name = " Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_DEBIT_NOTE))
		{
			jttObj.name = " Received Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_CREDIT_NOTE))
		{
			jttObj.name = " Received Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT))
		{
			jttObj.name = " Reverse Payment ";
		}
		jttObj.description = "Cust:" + gsObj.entityName + " (" + gsObj.foreignEntityKey.toString() + ") ";
		jttObj.amount = gsObj.amount;
		jttObj.transactionDate = gsObj.dateStmt;
		jttObj.docRef = GenericStmtBean.TABLENAME;
		jttObj.docKey = gsObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = gsObj.userIdUpdate;
		jttObj.userIdEdit = gsObj.userIdUpdate;
		jttObj.userIdCancel = gsObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = creditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Sales Return
	// // Debit Credit
	// // generalSalesReturn 
	// // acc receivable 
	// // inventory 
	// // COGS 
	public static void fnCreate(SalesReturnObject srObj) throws Exception
	{
		BigDecimal cogr = SalesReturnNut.getCostOfGoodsReturned(srObj.mPkid);
		// SELECT SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta
		// WHERE doc_table='cust_sales_return_item' AND doc_key IN (SELECT pkid
		// FROM cust_sales_return_item WHERE sales_return_id='1005');
		BranchObject branch = BranchNut.getObject(srObj.mCustSvcCtrId);
		CustAccountObject custObj = CustAccountNut.getObject(srObj.mCustAccId);
		GeneralLedgerObject srLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES_RETURN,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = branch.accPCCenterId;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Sales Return ";
		jttObj.description = "Customer:" + custObj.name + " (" + custObj.pkid.toString() + ") ";
		jttObj.amount = srObj.mTotalAmt;
		jttObj.transactionDate = srObj.mTimeCreated;
		jttObj.docRef = SalesReturnBean.TABLENAME;
		jttObj.docKey = srObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = srObj.mApproverId;
		jttObj.userIdEdit = srObj.mApproverId;
		jttObj.userIdCancel = srObj.mApproverId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = srLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt.negate();
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnCreate(PaymentVoucherIndexObject pvObj) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Payment Voucher";
		jttObj.description = "Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal.negate();
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount;
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnReverse(PaymentVoucherIndexObject pvObj, Integer userId) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " REVERSE PAYMENT VOUCHER ";
		jttObj.description = "REVERSE Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount.negate();
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Supplier Invoice / Goods Received Note
	// // Debit Credit
	// // inventoryStock 
	// // accPayable 
	public static void fnCreate(SuppInvoiceObject sinvObj, GoodsReceivedNoteObject grnObj) throws Exception
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		GeneralLedgerObject accPayLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = sinvObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " GRN (Purchase) ";
		jttObj.description = "Supplier:" + grnObj.mEntityName + "(" + grnObj.mEntityKey + ")" + " REF:"
				+ grnObj.mReferenceNo;
		jttObj.amount = grnObj.mAmount;
		jttObj.transactionDate = grnObj.mTimeComplete;
		jttObj.docRef = GoodsReceivedNoteBean.TABLENAME;
		jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = grnObj.mAcknowledgeId;
		jttObj.userIdEdit = grnObj.mAcknowledgeId;
		jttObj.userIdCancel = grnObj.mAcknowledgeId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accPayLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnCreate(CashRebateVoucherObject crvObj) throws Exception
	{
		BranchObject branch = BranchNut.getObject(crvObj.branch);
		CashAccountObject caObj = CashAccountNut.getObject(branch.cashbookCoupon);
		GeneralLedgerObject crvExpenseLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CRV_EXPENSE,
				DEFAULT_BATCH, crvObj.pcCenter, branch.currency);
		GeneralLedgerObject crvBookLdgObj = GeneralLedgerNut.getObjectFailSafe(caObj.accountType, DEFAULT_BATCH,
				crvObj.pcCenter, branch.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = crvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CRV For " + crvObj.nameDisplay + " (" + crvObj.cardNo + ")";
		jttObj.description = " CREATED CRV";
		jttObj.amount = crvObj.voucherValue;
		jttObj.transactionDate = crvObj.dateCreate;
		jttObj.docRef = CashRebateVoucherBean.TABLENAME;
		jttObj.docKey = crvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = crvObj.userCreate;
		jttObj.userIdEdit = crvObj.userCreate;
		// jttObj.userIdCancel= new
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvExpenseLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvBookLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	/*
	 * ///////////////////////////////////////////////////////////////// ///
	 * Purchase Return and Corresponding Credit Memo /// debit accPayable xxxxx1
	 * /// credit purchaseReturn xxxxx2 /// credit inventory xxxxx3 (by cogr)
	 * 
	 * //// xxxx2 = xxxxx3 - xxxx1
	 * 
	 * public static void fnCreate(PurchaseReturnIndexObject prObj) throws
	 * Exception { BigDecimal cogr =
	 * PurchaseReturnIndexNut.getCostOfGoodsReturned(prObj.pkid); //SELECT
	 * SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta WHERE
	 * doc_table='supp_purchase_return_item' AND doc_key IN (SELECT pkid FROM
	 * supp_purchase_return_item WHERE index_id='1003'); // BranchObject branch =
	 * BranchNut.getObject(prObj.branch); SuppAccountObject suppObj =
	 * SuppAccountNut.getObject(prObj.entityId);
	 * 
	 * GeneralLedgerObject accPayLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * cogsLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * inventoryLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency);
	 * 
	 * BigDecimal cogs = cogr.negate().subtract(prObj.amount);
	 * 
	 * JournalTransactionObject jttObj = new JournalTransactionObject();
	 * jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO; jttObj.pcCenterId =
	 * prObj.pcCenter; jttObj.batchId = DEFAULT_BATCH; // jttObj.typeId = //
	 * leave it as default jttObj.status = JournalTransactionBean.ACTIVE;
	 * jttObj.name = "Purchase Return "; jttObj.description = "
	 * Supplier:"+suppObj.name+" ("+suppObj.pkid.toString()+") "; jttObj.amount =
	 * prObj.amount; jttObj.transactionDate = prObj.timeComplete; jttObj.docRef =
	 * PurchaseReturnIndexBean.TABLENAME; jttObj.docKey = prObj.pkid;
	 * jttObj.state = JournalTransactionBean.STATE_CREATED; jttObj.userIdCreate =
	 * prObj.userIdUpdate; jttObj.userIdEdit = prObj.userIdUpdate;
	 * jttObj.userIdCancel= prObj.userIdUpdate; jttObj.timestampCreate =
	 * TimeFormat.getTimestamp(); jttObj.timestampEdit =
	 * TimeFormat.getTimestamp(); jttObj.timestampCancel =
	 * TimeFormat.getTimestamp();
	 *  { JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * accPayLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = prObj.amount; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * cogsLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = cogs; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * inventoryLdgObj.pkId; jeObj.description = jttObj.description;
	 * jeObj.currency = prObj.currency; jeObj.amount = cogr;
	 * jttObj.vecEntry.add(jeObj); } jttObj =
	 * JournalTransactionNut.fnCreate(jttObj); }
	 * 
	 */
	// / CreditMemo
	// / debit acc-payable
	// / credit purchase-return
	// / Cash Transfer
	public static void fnCreate(CashTransferItemObject ctiObj) throws Exception
	{
		CashAccountObject cashbookFrom = CashAccountNut.getObject(ctiObj.cashbookFrom);
		CashAccountObject cashbookTo = CashAccountNut.getObject(ctiObj.cashbookTo);
		GeneralLedgerObject fromCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookFrom.accountType,
				DEFAULT_BATCH, ctiObj.pcCenter, ctiObj.currency);
		GeneralLedgerObject toCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				ctiObj.pcCenter, ctiObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = ctiObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Internal Cash Transfer ";
		jttObj.description = " CashTransfer: From " + cashbookFrom.name + " " + cashbookFrom.code + " "
				+ cashbookFrom.accountNumber + " to " + cashbookTo.name + " " + cashbookTo.code + " "
				+ cashbookTo.accountNumber;
		jttObj.amount = ctiObj.amount;
		jttObj.transactionDate = ctiObj.dateStmt;
		jttObj.docRef = CashTransferItemBean.TABLENAME;
		jttObj.docKey = ctiObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = ctiObj.userPic;
		jttObj.userIdEdit = ctiObj.userPic;
		jttObj.userIdCancel = ctiObj.userPic;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = toCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = fromCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Bank In Slip
	public static void fnCreate(BankInSlipObject bisObj) throws Exception
	{
		CashAccountObject cashbookTo = CashAccountNut.getObject(bisObj.cashbook);
		CashAccountObject cashbookCash = null;
		CashAccountObject cashbookCheque1 = null;
		CashAccountObject cashbookCheque2 = null;
		CashAccountObject cashbookCheque3 = null;
		CashAccountObject cashbookCheque4 = null;
		CashAccountObject cashbookCheque5 = null;
		if (bisObj.cashCashbook.intValue() > 0)
		{
			cashbookCash = CashAccountNut.getObject(bisObj.cashCashbook);
		}
		if (bisObj.row1Cashbook.intValue() > 0)
		{
			cashbookCheque1 = CashAccountNut.getObject(bisObj.row1Cashbook);
		}
		if (bisObj.row2Cashbook.intValue() > 0)
		{
			cashbookCheque2 = CashAccountNut.getObject(bisObj.row2Cashbook);
		}
		if (bisObj.row3Cashbook.intValue() > 0)
		{
			cashbookCheque3 = CashAccountNut.getObject(bisObj.row3Cashbook);
		}
		if (bisObj.row4Cashbook.intValue() > 0)
		{
			cashbookCheque4 = CashAccountNut.getObject(bisObj.row4Cashbook);
		}
		if (bisObj.row5Cashbook.intValue() > 0)
		{
			cashbookCheque5 = CashAccountNut.getObject(bisObj.row5Cashbook);
		}
		GeneralLedgerObject cashToLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				bisObj.pcCenter, bisObj.currency);
		GeneralLedgerObject cashLdgObj = null;
		if (cashbookCash != null)
		{
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCash.accountType, DEFAULT_BATCH, bisObj.pcCenter,
					bisObj.currency);
		}
		GeneralLedgerObject cheque1LdgObj = null;
		if (cashbookCheque1 != null)
		{
			cheque1LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque1.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque2LdgObj = null;
		if (cashbookCheque2 != null)
		{
			cheque2LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque2.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque3LdgObj = null;
		if (cashbookCheque3 != null)
		{
			cheque3LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque3.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque4LdgObj = null;
		if (cashbookCheque4 != null)
		{
			cheque4LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque4.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque5LdgObj = null;
		if (cashbookCheque5 != null)
		{
			cheque5LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque5.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = bisObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Bank In Slip ";
		jttObj.description = " BankInSlip: " + bisObj.txDesc + " " + bisObj.bankCode + " " + bisObj.bankName + " "
				+ bisObj.accNumber;
		jttObj.amount = bisObj.amountTotal;
		jttObj.transactionDate = bisObj.txDate;
		jttObj.docRef = BankInSlipBean.TABLENAME;
		jttObj.docKey = bisObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = bisObj.userIdCreate;
		jttObj.userIdEdit = bisObj.userIdCreate;
		jttObj.userIdCancel = bisObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashToLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.cashAmount.signum() != 0 && cashLdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.cashAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row1Amount.signum() != 0 && cheque1LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque1LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row1Amount.subtract(bisObj.row1Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row2Amount.signum() != 0 && cheque2LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque2LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row2Amount.subtract(bisObj.row2Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row3Amount.signum() != 0 && cheque3LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque3LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row3Amount.subtract(bisObj.row3Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row4Amount.signum() != 0 && cheque4LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque4LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row4Amount.subtract(bisObj.row4Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row5Amount.signum() != 0 && cheque5LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque5LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row5Amount.subtract(bisObj.row5Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Credit Memo
	public static void fnCreate(CreditMemoIndexObject valObj) throws Exception
	{
		GeneralLedgerObject nominalLdgObj = null;
		if (valObj.entityTable.equals(SuppAccountBean.TABLENAME))
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH, valObj.pcCenter,
					valObj.currency);
		} else
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
					valObj.pcCenter, valObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = valObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CreditMemo ";
		jttObj.description = "To:" + valObj.entityName + "(" + valObj.entityKey + ")" + valObj.docReference;
		jttObj.amount = valObj.amount.abs();
		jttObj.transactionDate = valObj.timeCreate;
		jttObj.docRef = CreditMemoIndexBean.TABLENAME;
		jttObj.docKey = valObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = valObj.userIdCreate;
		jttObj.userIdEdit = valObj.userIdCreate;
		jttObj.userIdCancel = valObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = nominalLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = valObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt = 0; cnt < valObj.vecItems.size(); cnt++)
		{
			String theGLCode = "";
			CreditMemoItemObject cmiObj = (CreditMemoItemObject) valObj.vecItems.get(cnt);
			GeneralLedgerObject genLdgObj = null;
			if (cmiObj.amount.signum() > 0)
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeDebit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			} else
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeCredit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			}
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = cmiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Receipt Voucher
	// / Marketing Fund (thunderMatch) for Stock Gain/ Stock Loss
	public static void fnCreateStockVariance(Integer pcCenter, String currency, BigDecimal variance,
			String description, String reason, Integer userId)
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				pcCenter, currency);
		GeneralLedgerObject inventoryVarLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_VARIANCE,
				DEFAULT_BATCH, pcCenter, currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Reset Moving Average Stock Value ";
		jttObj.description = "DESC:" + description + " REASON:" + reason;
		jttObj.amount = variance.abs();
		jttObj.transactionDate = TimeFormat.getTimestamp();
		// jttObj.docRef =
		// jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = userId;
		jttObj.userIdEdit = userId;
		jttObj.userIdCancel = userId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryVarLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
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
package com.vlee.ejb.accounting;

import javax.servlet.ServletContext;
import java.sql.Timestamp;
import javax.rmi.*;
import java.util.*;
import javax.naming.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class JournalTxnLogic
{
	public static Integer DEFAULT_BATCH = BatchBean.PKID_DEFAULT;

	// // Customer Invoice Object to Journal
	// // Debit Credit
	// // accReceivable XXX
	// // generalSales XXX
	// // COGS XXX
	// // inventoryStock XXX
	public static void fnCreate(InvoiceObject invObj) throws Exception
	{
		NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME, invObj.mPCCenter,
				invObj.mEntityKey, invObj.mCurrency);
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// code = "not_used";
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = invObj.mEntityKey;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			naObj.currency = invObj.mCurrency;
			naObj.amount = new BigDecimal(0);
			naObj.remarks = invObj.mRemarks;
			naObj.accPCCenterId = invObj.mPCCenter;
			naObj.userIdUpdate = invObj.mUserIdUpdate;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			naObj = naEJB.getObject();
		}
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = InvoiceBean.TABLENAME;
		natObj.foreignKey = invObj.mPkid;
		natObj.code = "not_used";
		natObj.info1 = " ";
		// natObj.description = " ";
		natObj.description = "Sales. INV:" + invObj.mPkid.toString() + " REF:" + invObj.mReferenceNo + " RMKS:"
				+ invObj.mRemarks;
		natObj.txnType = " ";
		natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
		natObj.currency = invObj.mCurrency;
		natObj.amount = invObj.mTotalAmt;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = invObj.mTimeIssued;
		natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
		natObj.timeParam2 = invObj.mTimeIssued;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = invObj.mUserIdUpdate;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		// /////////////////////////////////////////////////////////////////////
		BigDecimal cogs = InvoiceNut.getCostOfGoodsSold(invObj.mPkid);
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 2 ");
		if (cogs.signum() == 0 && invObj.mTotalAmt.signum() == 0)
		{
			return;
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 3 ");
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject genSalesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 4 ");
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = invObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Invoice To Customer";
		jttObj.description = "Cust:" + invObj.mForeignText + " " + invObj.mEntityName + " ("
				+ invObj.mEntityKey.toString() + ") REF:" + invObj.mReferenceNo;
		jttObj.amount = invObj.mTotalAmt;
		jttObj.transactionDate = invObj.mTimeIssued;
		jttObj.docRef = InvoiceBean.TABLENAME;
		jttObj.docKey = invObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = invObj.mUserIdUpdate;
		jttObj.userIdEdit = invObj.mUserIdUpdate;
		jttObj.userIdCancel = invObj.mUserIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 5 ");
		if (invObj.mTotalAmt.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.GENERAL_SALES;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = invObj.mTotalAmt;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 6 ");
		if (invObj.mTotalAmt.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genSalesLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.ACC_RECEIVABLE;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = invObj.mTotalAmt.negate();
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 7 ");
		// / use inventory cost instead of invoice amount for the following:
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY_COST;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 8 ");
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs.negate();
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 9 ");
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Receipt Object
	// // Debit Credit
	// // cash XXX
	// // accReceivable XXX
	// /
	public static void fnCreate(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
		GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
				rctObj.pcCenter, rctObj.currency);
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Collection from Customer ";
		jttObj.description = "Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.paymentTime;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		if (rctObj.amount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = creditLdgObj.pkId;
			jeObj.description = jttObj.description;
			if (rctObj.amountCash.signum() != 0)
			{
				jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
			}
			if (rctObj.amountCard.signum() != 0)
			{
				jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
			}
			if (rctObj.amountCheque.signum() != 0)
			{
				jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
			}
			if (rctObj.amountPDCheque.signum() != 0)
			{
				jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
			}
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null && rctObj.amountCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnReverse(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
				rctObj.pcCenter, rctObj.currency);
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			if(rctObj.chequeBankInCb.intValue() > 0)
			{ cbChequeObj = CashAccountNut.getObject(rctObj.chequeBankInCb); }
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			if(rctObj.pdChequeBankInCb.intValue() > 0)
			{ cbPDChequeObj = CashAccountNut.getObject(rctObj.pdChequeBankInCb); }

			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "REVERSE RECEIPT";
		jttObj.description = "Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.lastUpdate;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = rctObj.lastUpdate;
		jttObj.timestampEdit = rctObj.lastUpdate;
		jttObj.timestampCancel = rctObj.lastUpdate;
		if (rctObj.amount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			if (rctObj.amountCash.signum() != 0)
			{
				jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
			}
			if (rctObj.amountCard.signum() != 0)
			{
				jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
			}
			if (rctObj.amountCheque.signum() != 0)
			{
				jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
			}
			if (rctObj.amountPDCheque.signum() != 0)
			{
				jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
			}
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null
				&& rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Credit/Debit Note
	public static void fnCreate(GenericStmtObject gsObj) throws Exception
	{
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeDebit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeCredit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = gsObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "DN / CN to Customer ";
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE))
		{
			jttObj.name = " Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE))
		{
			jttObj.name = " Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_DEBIT_NOTE))
		{
			jttObj.name = " Received Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_CREDIT_NOTE))
		{
			jttObj.name = " Received Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT))
		{
			jttObj.name = " Reverse Payment ";
		}
		jttObj.description = "Cust:" + gsObj.entityName + " (" + gsObj.foreignEntityKey.toString() + ") ";
		jttObj.amount = gsObj.amount;
		jttObj.transactionDate = gsObj.dateStmt;
		jttObj.docRef = GenericStmtBean.TABLENAME;
		jttObj.docKey = gsObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = gsObj.userIdUpdate;
		jttObj.userIdEdit = gsObj.userIdUpdate;
		jttObj.userIdCancel = gsObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = creditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Sales Return
	// // Debit Credit
	// // generalSalesReturn XXX
	// // acc receivable XXX
	// // inventory XXX
	// // COGS XXX
	public static void fnCreate(SalesReturnObject srObj) throws Exception
	{
		BigDecimal cogr = SalesReturnNut.getCostOfGoodsReturned(srObj.mPkid);
		// SELECT SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta
		// WHERE doc_table='cust_sales_return_item' AND doc_key IN (SELECT pkid
		// FROM cust_sales_return_item WHERE sales_return_id='1005');
		BranchObject branch = BranchNut.getObject(srObj.mCustSvcCtrId);
		CustAccountObject custObj = CustAccountNut.getObject(srObj.mCustAccId);
		GeneralLedgerObject srLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES_RETURN,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = branch.accPCCenterId;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Sales Return ";
		jttObj.description = "Customer:" + custObj.name + " (" + custObj.pkid.toString() + ") ";
		jttObj.amount = srObj.mTotalAmt;
		jttObj.transactionDate = srObj.mTimeCreated;
		jttObj.docRef = SalesReturnBean.TABLENAME;
		jttObj.docKey = srObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = srObj.mApproverId;
		jttObj.userIdEdit = srObj.mApproverId;
		jttObj.userIdCancel = srObj.mApproverId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = srLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt.negate();
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnCreate(PaymentVoucherIndexObject pvObj) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Payment Voucher";
		jttObj.description = "Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal.negate();
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount;
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnReverse(PaymentVoucherIndexObject pvObj, Integer userId) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " REVERSE PAYMENT VOUCHER ";
		jttObj.description = "REVERSE Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount.negate();
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Supplier Invoice / Goods Received Note
	// // Debit Credit
	// // inventoryStock XXX
	// // accPayable XXX
	public static void fnCreate(SuppInvoiceObject sinvObj, GoodsReceivedNoteObject grnObj) throws Exception
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		GeneralLedgerObject accPayLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = sinvObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " GRN (Purchase) ";
		jttObj.description = "Supplier:" + grnObj.mEntityName + "(" + grnObj.mEntityKey + ")" + " REF:"
				+ grnObj.mReferenceNo;
		jttObj.amount = grnObj.mAmount;
		jttObj.transactionDate = grnObj.mTimeComplete;
		jttObj.docRef = GoodsReceivedNoteBean.TABLENAME;
		jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = grnObj.mAcknowledgeId;
		jttObj.userIdEdit = grnObj.mAcknowledgeId;
		jttObj.userIdCancel = grnObj.mAcknowledgeId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accPayLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnCreate(CashRebateVoucherObject crvObj) throws Exception
	{
		BranchObject branch = BranchNut.getObject(crvObj.branch);
		CashAccountObject caObj = CashAccountNut.getObject(branch.cashbookCoupon);
		GeneralLedgerObject crvExpenseLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CRV_EXPENSE,
				DEFAULT_BATCH, crvObj.pcCenter, branch.currency);
		GeneralLedgerObject crvBookLdgObj = GeneralLedgerNut.getObjectFailSafe(caObj.accountType, DEFAULT_BATCH,
				crvObj.pcCenter, branch.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = crvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CRV For " + crvObj.nameDisplay + " (" + crvObj.cardNo + ")";
		jttObj.description = " CREATED CRV";
		jttObj.amount = crvObj.voucherValue;
		jttObj.transactionDate = crvObj.dateCreate;
		jttObj.docRef = CashRebateVoucherBean.TABLENAME;
		jttObj.docKey = crvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = crvObj.userCreate;
		jttObj.userIdEdit = crvObj.userCreate;
		// jttObj.userIdCancel= new
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvExpenseLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvBookLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	/*
	 * ///////////////////////////////////////////////////////////////// ///
	 * Purchase Return and Corresponding Credit Memo /// debit accPayable xxxxx1
	 * /// credit purchaseReturn xxxxx2 /// credit inventory xxxxx3 (by cogr)
	 * 
	 * //// xxxx2 = xxxxx3 - xxxx1
	 * 
	 * public static void fnCreate(PurchaseReturnIndexObject prObj) throws
	 * Exception { BigDecimal cogr =
	 * PurchaseReturnIndexNut.getCostOfGoodsReturned(prObj.pkid); //SELECT
	 * SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta WHERE
	 * doc_table='supp_purchase_return_item' AND doc_key IN (SELECT pkid FROM
	 * supp_purchase_return_item WHERE index_id='1003'); // BranchObject branch =
	 * BranchNut.getObject(prObj.branch); SuppAccountObject suppObj =
	 * SuppAccountNut.getObject(prObj.entityId);
	 * 
	 * GeneralLedgerObject accPayLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * cogsLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * inventoryLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency);
	 * 
	 * BigDecimal cogs = cogr.negate().subtract(prObj.amount);
	 * 
	 * JournalTransactionObject jttObj = new JournalTransactionObject();
	 * jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO; jttObj.pcCenterId =
	 * prObj.pcCenter; jttObj.batchId = DEFAULT_BATCH; // jttObj.typeId = //
	 * leave it as default jttObj.status = JournalTransactionBean.ACTIVE;
	 * jttObj.name = "Purchase Return "; jttObj.description = "
	 * Supplier:"+suppObj.name+" ("+suppObj.pkid.toString()+") "; jttObj.amount =
	 * prObj.amount; jttObj.transactionDate = prObj.timeComplete; jttObj.docRef =
	 * PurchaseReturnIndexBean.TABLENAME; jttObj.docKey = prObj.pkid;
	 * jttObj.state = JournalTransactionBean.STATE_CREATED; jttObj.userIdCreate =
	 * prObj.userIdUpdate; jttObj.userIdEdit = prObj.userIdUpdate;
	 * jttObj.userIdCancel= prObj.userIdUpdate; jttObj.timestampCreate =
	 * TimeFormat.getTimestamp(); jttObj.timestampEdit =
	 * TimeFormat.getTimestamp(); jttObj.timestampCancel =
	 * TimeFormat.getTimestamp();
	 *  { JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * accPayLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = prObj.amount; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * cogsLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = cogs; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * inventoryLdgObj.pkId; jeObj.description = jttObj.description;
	 * jeObj.currency = prObj.currency; jeObj.amount = cogr;
	 * jttObj.vecEntry.add(jeObj); } jttObj =
	 * JournalTransactionNut.fnCreate(jttObj); }
	 * 
	 */
	// / CreditMemo
	// / debit acc-payable
	// / credit purchase-return
	// / Cash Transfer
	public static void fnCreate(CashTransferItemObject ctiObj) throws Exception
	{
		CashAccountObject cashbookFrom = CashAccountNut.getObject(ctiObj.cashbookFrom);
		CashAccountObject cashbookTo = CashAccountNut.getObject(ctiObj.cashbookTo);
		GeneralLedgerObject fromCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookFrom.accountType,
				DEFAULT_BATCH, ctiObj.pcCenter, ctiObj.currency);
		GeneralLedgerObject toCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				ctiObj.pcCenter, ctiObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = ctiObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Internal Cash Transfer ";
		jttObj.description = " CashTransfer: From " + cashbookFrom.name + " " + cashbookFrom.code + " "
				+ cashbookFrom.accountNumber + " to " + cashbookTo.name + " " + cashbookTo.code + " "
				+ cashbookTo.accountNumber;
		jttObj.amount = ctiObj.amount;
		jttObj.transactionDate = ctiObj.dateStmt;
		jttObj.docRef = CashTransferItemBean.TABLENAME;
		jttObj.docKey = ctiObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = ctiObj.userPic;
		jttObj.userIdEdit = ctiObj.userPic;
		jttObj.userIdCancel = ctiObj.userPic;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = toCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = fromCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Bank In Slip
	public static void fnCreate(BankInSlipObject bisObj) throws Exception
	{
		CashAccountObject cashbookTo = CashAccountNut.getObject(bisObj.cashbook);
		CashAccountObject cashbookCash = null;
		CashAccountObject cashbookCheque1 = null;
		CashAccountObject cashbookCheque2 = null;
		CashAccountObject cashbookCheque3 = null;
		CashAccountObject cashbookCheque4 = null;
		CashAccountObject cashbookCheque5 = null;
		if (bisObj.cashCashbook.intValue() > 0)
		{
			cashbookCash = CashAccountNut.getObject(bisObj.cashCashbook);
		}
		if (bisObj.row1Cashbook.intValue() > 0)
		{
			cashbookCheque1 = CashAccountNut.getObject(bisObj.row1Cashbook);
		}
		if (bisObj.row2Cashbook.intValue() > 0)
		{
			cashbookCheque2 = CashAccountNut.getObject(bisObj.row2Cashbook);
		}
		if (bisObj.row3Cashbook.intValue() > 0)
		{
			cashbookCheque3 = CashAccountNut.getObject(bisObj.row3Cashbook);
		}
		if (bisObj.row4Cashbook.intValue() > 0)
		{
			cashbookCheque4 = CashAccountNut.getObject(bisObj.row4Cashbook);
		}
		if (bisObj.row5Cashbook.intValue() > 0)
		{
			cashbookCheque5 = CashAccountNut.getObject(bisObj.row5Cashbook);
		}
		GeneralLedgerObject cashToLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				bisObj.pcCenter, bisObj.currency);
		GeneralLedgerObject cashLdgObj = null;
		if (cashbookCash != null)
		{
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCash.accountType, DEFAULT_BATCH, bisObj.pcCenter,
					bisObj.currency);
		}
		GeneralLedgerObject cheque1LdgObj = null;
		if (cashbookCheque1 != null)
		{
			cheque1LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque1.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque2LdgObj = null;
		if (cashbookCheque2 != null)
		{
			cheque2LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque2.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque3LdgObj = null;
		if (cashbookCheque3 != null)
		{
			cheque3LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque3.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque4LdgObj = null;
		if (cashbookCheque4 != null)
		{
			cheque4LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque4.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque5LdgObj = null;
		if (cashbookCheque5 != null)
		{
			cheque5LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque5.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = bisObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Bank In Slip ";
		jttObj.description = " BankInSlip: " + bisObj.txDesc + " " + bisObj.bankCode + " " + bisObj.bankName + " "
				+ bisObj.accNumber;
		jttObj.amount = bisObj.amountTotal;
		jttObj.transactionDate = bisObj.txDate;
		jttObj.docRef = BankInSlipBean.TABLENAME;
		jttObj.docKey = bisObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = bisObj.userIdCreate;
		jttObj.userIdEdit = bisObj.userIdCreate;
		jttObj.userIdCancel = bisObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashToLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.cashAmount.signum() != 0 && cashLdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.cashAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row1Amount.signum() != 0 && cheque1LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque1LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row1Amount.subtract(bisObj.row1Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row2Amount.signum() != 0 && cheque2LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque2LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row2Amount.subtract(bisObj.row2Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row3Amount.signum() != 0 && cheque3LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque3LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row3Amount.subtract(bisObj.row3Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row4Amount.signum() != 0 && cheque4LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque4LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row4Amount.subtract(bisObj.row4Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row5Amount.signum() != 0 && cheque5LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque5LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row5Amount.subtract(bisObj.row5Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Credit Memo
	public static void fnCreate(CreditMemoIndexObject valObj) throws Exception
	{
		GeneralLedgerObject nominalLdgObj = null;
		if (valObj.entityTable.equals(SuppAccountBean.TABLENAME))
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH, valObj.pcCenter,
					valObj.currency);
		} else
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
					valObj.pcCenter, valObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = valObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CreditMemo ";
		jttObj.description = "To:" + valObj.entityName + "(" + valObj.entityKey + ")" + valObj.docReference;
		jttObj.amount = valObj.amount.abs();
		jttObj.transactionDate = valObj.timeCreate;
		jttObj.docRef = CreditMemoIndexBean.TABLENAME;
		jttObj.docKey = valObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = valObj.userIdCreate;
		jttObj.userIdEdit = valObj.userIdCreate;
		jttObj.userIdCancel = valObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = nominalLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = valObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt = 0; cnt < valObj.vecItems.size(); cnt++)
		{
			String theGLCode = "";
			CreditMemoItemObject cmiObj = (CreditMemoItemObject) valObj.vecItems.get(cnt);
			GeneralLedgerObject genLdgObj = null;
			if (cmiObj.amount.signum() > 0)
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeDebit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			} else
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeCredit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			}
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = cmiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Receipt Voucher
	// / Marketing Fund (thunderMatch) for Stock Gain/ Stock Loss
	public static void fnCreateStockVariance(Integer pcCenter, String currency, BigDecimal variance,
			String description, String reason, Integer userId)
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				pcCenter, currency);
		GeneralLedgerObject inventoryVarLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_VARIANCE,
				DEFAULT_BATCH, pcCenter, currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Reset Moving Average Stock Value ";
		jttObj.description = "DESC:" + description + " REASON:" + reason;
		jttObj.amount = variance.abs();
		jttObj.transactionDate = TimeFormat.getTimestamp();
		// jttObj.docRef =
		// jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = userId;
		jttObj.userIdEdit = userId;
		jttObj.userIdCancel = userId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryVarLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
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
package com.vlee.ejb.accounting;

import java.math.BigDecimal;

import com.vlee.ejb.customer.CashRebateVoucherBean;
import com.vlee.ejb.customer.CashRebateVoucherObject;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.customer.CustAccountNut;
import com.vlee.ejb.customer.CustAccountObject;
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.customer.SalesReturnBean;
import com.vlee.ejb.customer.SalesReturnNut;
import com.vlee.ejb.customer.SalesReturnObject;
import com.vlee.ejb.supplier.GoodsReceivedNoteBean;
import com.vlee.ejb.supplier.GoodsReceivedNoteObject;
import com.vlee.ejb.supplier.SuppAccountBean;
import com.vlee.ejb.supplier.SuppInvoiceObject;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.Log;
import com.vlee.util.TimeFormat;

public class JournalTxnLogic
{
	public static Integer DEFAULT_BATCH = BatchBean.PKID_DEFAULT;

	// // Customer Invoice Object to Journal
	// // Debit Credit
	// // accReceivable 
	// // generalSales 
	// // COGS 
	// // inventoryStock 
	public static void fnCreate(InvoiceObject invObj) throws Exception
	{
		NominalAccountObject naObj = NominalAccountNut.getObject(CustAccountBean.TABLENAME, invObj.mPCCenter,
				invObj.mEntityKey, invObj.mCurrency);
		if (naObj == null)
		{
			naObj = new NominalAccountObject();
			// code = "not_used";
			naObj.namespace = NominalAccountBean.NS_CUSTOMER;
			naObj.foreignTable = NominalAccountBean.FT_CUSTOMER;
			naObj.foreignKey = invObj.mEntityKey;
			naObj.accountType = NominalAccountBean.ACC_TYPE_RECEIVABLE;
			naObj.currency = invObj.mCurrency;
			naObj.amount = new BigDecimal(0);
			naObj.remarks = invObj.mRemarks;
			naObj.accPCCenterId = invObj.mPCCenter;
			naObj.userIdUpdate = invObj.mUserIdUpdate;
			NominalAccount naEJB = NominalAccountNut.fnCreate(naObj);
			naObj = naEJB.getObject();
		}
		NominalAccountTxnObject natObj = new NominalAccountTxnObject();
		natObj.nominalAccount = naObj.pkid;
		natObj.foreignTable = InvoiceBean.TABLENAME;
		natObj.foreignKey = invObj.mPkid;
		natObj.code = "not_used";
		natObj.info1 = " ";
		// natObj.description = " ";
		natObj.description = "Sales. INV:" + invObj.mPkid.toString() + " REF:" + invObj.mReferenceNo + " RMKS:"
				+ invObj.mRemarks;
		natObj.txnType = " ";
		natObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
		natObj.glCodeCredit = GLCodeBean.GENERAL_SALES;
		natObj.currency = invObj.mCurrency;
		natObj.amount = invObj.mTotalAmt;
		natObj.timeOption1 = NominalAccountTxnBean.TIME_STMT;
		natObj.timeParam1 = invObj.mTimeIssued;
		natObj.timeOption2 = NominalAccountTxnBean.TIME_DUE;
		natObj.timeParam2 = invObj.mTimeIssued;
		natObj.state = NominalAccountTxnBean.ST_ACTUAL;
		natObj.status = NominalAccountTxnBean.STATUS_ACTIVE;
		natObj.lastUpdate = TimeFormat.getTimestamp();
		natObj.userIdUpdate = invObj.mUserIdUpdate;
		NominalAccountTxn natEJB = NominalAccountTxnNut.fnCreate(natObj);
		// /////////////////////////////////////////////////////////////////////
		BigDecimal cogs = InvoiceNut.getCostOfGoodsSold(invObj.mPkid);
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 2 ");
		if (cogs.signum() == 0 && invObj.mTotalAmt.signum() == 0)
		{
			return;
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 3 ");
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject genSalesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, invObj.mPCCenter, invObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				invObj.mPCCenter, invObj.mCurrency);
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 4 ");
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = invObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Invoice To Customer";
		jttObj.description = "Cust:" + invObj.mForeignText + " " + invObj.mEntityName + " ("
				+ invObj.mEntityKey.toString() + ") REF:" + invObj.mReferenceNo;
		jttObj.amount = invObj.mTotalAmt;
		jttObj.transactionDate = invObj.mTimeIssued;
		jttObj.docRef = InvoiceBean.TABLENAME;
		jttObj.docKey = invObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = invObj.mUserIdUpdate;
		jttObj.userIdEdit = invObj.mUserIdUpdate;
		jttObj.userIdCancel = invObj.mUserIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 5 ");
		if (invObj.mTotalAmt.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.GENERAL_SALES;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = invObj.mTotalAmt;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 6 ");
		if (invObj.mTotalAmt.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genSalesLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.ACC_RECEIVABLE;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = invObj.mTotalAmt.negate();
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 7 ");
		// / use inventory cost instead of invoice amount for the following:
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY_COST;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs;
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 8 ");
		if (cogs.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = "Customer Invoice: " + invObj.mStmtNumber.toString() + "(" + invObj.mPkid.toString()
					+ ") " + GLCodeBean.INVENTORY;
			jeObj.currency = invObj.mCurrency;
			jeObj.amount = cogs.negate();
			jttObj.vecEntry.add(jeObj);
		}
		Log.printVerbose(" CREATING invoice jtxn... checkpoint 9 ");
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Receipt Object
	// // Debit Credit
	// // cash 
	// // accReceivable 
	// /
	public static void fnCreate(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
		GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
				rctObj.pcCenter, rctObj.currency);
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Collection from Customer ";
		jttObj.description = "Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.paymentTime;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		if (rctObj.amount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = creditLdgObj.pkId;
			jeObj.description = jttObj.description;
			if (rctObj.amountCash.signum() != 0)
			{
				jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
			}
			if (rctObj.amountCard.signum() != 0)
			{
				jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
			}
			if (rctObj.amountCheque.signum() != 0)
			{
				jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
			}
			if (rctObj.amountPDCheque.signum() != 0)
			{
				jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
			}
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null && rctObj.amountCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount);
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnReverse(OfficialReceiptObject rctObj) throws Exception
	{
		if (rctObj.amount.signum() == 0)
		{
			return;
		}
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(rctObj.glCodeCredit, DEFAULT_BATCH,
				rctObj.pcCenter, rctObj.currency);
		CashAccountObject cbCashObj = null;
		GeneralLedgerObject cashLdgObj = null;
		if (rctObj.cbCash.intValue() > 0)
		{
			cbCashObj = CashAccountNut.getObject(rctObj.cbCash);
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCashObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbCardObj = null;
		GeneralLedgerObject cardLdgObj = null;
		if (rctObj.cbCard.intValue() > 0)
		{
			cbCardObj = CashAccountNut.getObject(rctObj.cbCard);
			cardLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCardObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbChequeObj = null;
		GeneralLedgerObject chequeLdgObj = null;
		if (rctObj.cbCheque.intValue() > 0)
		{
			cbChequeObj = CashAccountNut.getObject(rctObj.cbCheque);
			if(rctObj.chequeBankInCb.intValue() > 0)
			{ cbChequeObj = CashAccountNut.getObject(rctObj.chequeBankInCb); }
			chequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbChequeObj.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		CashAccountObject cbPDChequeObj = null;
		GeneralLedgerObject pdChequeLdgObj = null;
		if (rctObj.cbPDCheque.intValue() > 0)
		{
			cbPDChequeObj = CashAccountNut.getObject(rctObj.cbPDCheque);
			if(rctObj.pdChequeBankInCb.intValue() > 0)
			{ cbPDChequeObj = CashAccountNut.getObject(rctObj.pdChequeBankInCb); }

			pdChequeLdgObj = GeneralLedgerNut.getObjectFailSafe(cbPDChequeObj.accountType, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		CashAccountObject cbOther = null;
		GeneralLedgerObject otherLdgObj = null;
		if (rctObj.cbOther.intValue() > 0)
		{
			cbOther = CashAccountNut.getObject(rctObj.cbOther);
			otherLdgObj = GeneralLedgerNut.getObjectFailSafe(cbOther.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		Log.printVerbose(" In JTxnLogic : checkpoint 3....................");
		CashAccountObject cbCoupon = null;
		GeneralLedgerObject couponLdgObj = null;
		if (rctObj.cbCoupon.intValue() > 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			couponLdgObj = GeneralLedgerNut.getObjectFailSafe(cbCoupon.accountType, DEFAULT_BATCH, rctObj.pcCenter,
					rctObj.currency);
		}
		GeneralLedgerObject cardChargesLdgObj = null;
		if (rctObj.cardCharges.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			cardChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CREDIT_CARD_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		GeneralLedgerObject chequeChargesLdgObj = null;
		if (rctObj.chequeChargesAmount.signum() != 0)
		{
			cbCoupon = CashAccountNut.getObject(rctObj.cbCoupon);
			chequeChargesLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INTEREST_BANK_CHARGES, DEFAULT_BATCH,
					rctObj.pcCenter, rctObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = rctObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "REVERSE RECEIPT";
		jttObj.description = "Cust:" + rctObj.entityName + " (" + rctObj.entityKey.toString() + ") ";
		jttObj.amount = rctObj.amount;
		jttObj.transactionDate = rctObj.paymentTime;
		jttObj.docRef = OfficialReceiptBean.TABLENAME;
		jttObj.docKey = rctObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = rctObj.userIdUpdate;
		jttObj.userIdEdit = rctObj.userIdUpdate;
		jttObj.userIdCancel = rctObj.userIdUpdate;
		jttObj.timestampCreate = rctObj.lastUpdate;
		jttObj.timestampEdit = rctObj.lastUpdate;
		jttObj.timestampCancel = rctObj.lastUpdate;
		if (rctObj.amount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			if (rctObj.amountCash.signum() != 0)
			{
				jeObj.description += " Cash:" + CurrencyFormat.strCcy(rctObj.amountCash);
			}
			if (rctObj.amountCard.signum() != 0)
			{
				jeObj.description += " Card:" + CurrencyFormat.strCcy(rctObj.amountCard);
			}
			if (rctObj.amountCheque.signum() != 0)
			{
				jeObj.description += " Cheque:" + CurrencyFormat.strCcy(rctObj.amountCheque);
			}
			if (rctObj.amountPDCheque.signum() != 0)
			{
				jeObj.description += " PDCheque:" + CurrencyFormat.strCcy(rctObj.amountPDCheque);
			}
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCashObj != null && cashLdgObj != null && rctObj.amountCash.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCash.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardLdgObj != null && rctObj.amountCard.subtract(rctObj.cardCharges).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCard.subtract(rctObj.cardCharges).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && cardChargesLdgObj != null && rctObj.cardCharges.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cardChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.cardCharges.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbChequeObj != null && chequeLdgObj != null
				&& rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCheque.subtract(rctObj.chequeChargesAmount).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCardObj != null && chequeChargesLdgObj != null && rctObj.chequeChargesAmount.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = chequeChargesLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.chequeChargesAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbPDChequeObj != null && pdChequeLdgObj != null && rctObj.amountPDCheque.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = pdChequeLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountPDCheque.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbOther != null && otherLdgObj != null && rctObj.amountOther.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = otherLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountOther.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (cbCoupon != null && couponLdgObj != null && rctObj.amountCoupon.signum() != 0)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = couponLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = rctObj.currency;
			jeObj.amount = rctObj.amountCoupon.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Credit/Debit Note
	public static void fnCreate(GenericStmtObject gsObj) throws Exception
	{
		// // Create a GeneralLedger (T-Account) if it does not exist
		GeneralLedgerObject debitLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeDebit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		GeneralLedgerObject creditLdgObj = GeneralLedgerNut.getObjectFailSafe(gsObj.glCodeCredit, DEFAULT_BATCH,
				gsObj.pcCenter, gsObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = gsObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "DN / CN to Customer ";
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_CREDIT_NOTE))
		{
			jttObj.name = " Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_ISS_DEBIT_NOTE))
		{
			jttObj.name = " Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_DEBIT_NOTE))
		{
			jttObj.name = " Received Debit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REC_CREDIT_NOTE))
		{
			jttObj.name = " Received Credit Note ";
		}
		if (gsObj.stmtType.equals(GenericStmtBean.STMT_TYPE_REVERSE_PAYMENT))
		{
			jttObj.name = " Reverse Payment ";
		}
		jttObj.description = "Cust:" + gsObj.entityName + " (" + gsObj.foreignEntityKey.toString() + ") ";
		jttObj.amount = gsObj.amount;
		jttObj.transactionDate = gsObj.dateStmt;
		jttObj.docRef = GenericStmtBean.TABLENAME;
		jttObj.docKey = gsObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = gsObj.userIdUpdate;
		jttObj.userIdEdit = gsObj.userIdUpdate;
		jttObj.userIdCancel = gsObj.userIdUpdate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = debitLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = creditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = gsObj.currency;
			jeObj.amount = gsObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Customer Sales Return
	// // Debit Credit
	// // generalSalesReturn 
	// // acc receivable 
	// // inventory 
	// // COGS 
	public static void fnCreate(SalesReturnObject srObj) throws Exception
	{
		BigDecimal cogr = SalesReturnNut.getCostOfGoodsReturned(srObj.mPkid);
		// SELECT SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta
		// WHERE doc_table='cust_sales_return_item' AND doc_key IN (SELECT pkid
		// FROM cust_sales_return_item WHERE sales_return_id='1005');
		BranchObject branch = BranchNut.getObject(srObj.mCustSvcCtrId);
		CustAccountObject custObj = CustAccountNut.getObject(srObj.mCustAccId);
		GeneralLedgerObject srLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.GENERAL_SALES_RETURN,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject accRecLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject invCostLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
				DEFAULT_BATCH, branch.accPCCenterId, srObj.mCurrency);
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				branch.accPCCenterId, srObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = branch.accPCCenterId;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Sales Return ";
		jttObj.description = "Customer:" + custObj.name + " (" + custObj.pkid.toString() + ") ";
		jttObj.amount = srObj.mTotalAmt;
		jttObj.transactionDate = srObj.mTimeCreated;
		jttObj.docRef = SalesReturnBean.TABLENAME;
		jttObj.docKey = srObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = srObj.mApproverId;
		jttObj.userIdEdit = srObj.mApproverId;
		jttObj.userIdCancel = srObj.mApproverId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = srLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accRecLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = srObj.mTotalAmt.negate();
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = invCostLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = srObj.mCurrency;
			jeObj.amount = cogr.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnCreate(PaymentVoucherIndexObject pvObj) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = "Payment Voucher";
		jttObj.description = "Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal.negate();
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount;
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// // PaymentVoucher
	// // Debit Credit
	// // yyyyyyyyy XXXXX
	// // cash/chequingBank XXXXX
	public static void fnReverse(PaymentVoucherIndexObject pvObj, Integer userId) throws Exception
	{
		CashAccountObject cashbook = CashAccountNut.getObject(pvObj.cashbookOther);
		GeneralLedgerObject cbCreditLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbook.accountType, DEFAULT_BATCH,
				pvObj.pcCenter, cashbook.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " REVERSE PAYMENT VOUCHER ";
		jttObj.description = "REVERSE Payee:" + pvObj.payTo + " " + pvObj.description + " " + pvObj.remarks + " "
				+ pvObj.chequeNo;
		jttObj.amount = pvObj.amountTotal;
		jttObj.transactionDate = pvObj.dateStmt;
		jttObj.docRef = PaymentVoucherIndexBean.TABLENAME;
		jttObj.docKey = pvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = pvObj.userIdCreate;
		jttObj.userIdEdit = pvObj.userIdPIC;
		jttObj.userIdCancel = pvObj.userIdPIC;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cbCreditLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = pvObj.currency;
			jeObj.amount = pvObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt1 = 0; cnt1 < pvObj.vecItems.size(); cnt1++)
		{
			PaymentVoucherItemObject pviObj = (PaymentVoucherItemObject) pvObj.vecItems.get(cnt1);
			{
				GeneralLedgerObject accDebitLdgObj = GeneralLedgerNut.getObjectFailSafe(pviObj.glCodeDebit,
						DEFAULT_BATCH, pvObj.pcCenter, cashbook.currency);
				JournalEntryObject jeObj = new JournalEntryObject();
				jeObj.glId = accDebitLdgObj.pkId;
				jeObj.description = jttObj.description;
				jeObj.currency = pvObj.currency;
				jeObj.amount = pviObj.amount.negate();
				jttObj.vecEntry.add(jeObj);
			}
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Supplier Invoice / Goods Received Note
	// // Debit Credit
	// // inventoryStock 
	// // accPayable 
	public static void fnCreate(SuppInvoiceObject sinvObj, GoodsReceivedNoteObject grnObj) throws Exception
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		GeneralLedgerObject accPayLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH,
				sinvObj.mPCCenter, sinvObj.mCurrency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = sinvObj.mPCCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " GRN (Purchase) ";
		jttObj.description = "Supplier:" + grnObj.mEntityName + "(" + grnObj.mEntityKey + ")" + " REF:"
				+ grnObj.mReferenceNo;
		jttObj.amount = grnObj.mAmount;
		jttObj.transactionDate = grnObj.mTimeComplete;
		jttObj.docRef = GoodsReceivedNoteBean.TABLENAME;
		jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = grnObj.mAcknowledgeId;
		jttObj.userIdEdit = grnObj.mAcknowledgeId;
		jttObj.userIdCancel = grnObj.mAcknowledgeId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = accPayLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = sinvObj.mCurrency;
			jeObj.amount = grnObj.mAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	public static void fnCreate(CashRebateVoucherObject crvObj) throws Exception
	{
		BranchObject branch = BranchNut.getObject(crvObj.branch);
		CashAccountObject caObj = CashAccountNut.getObject(branch.cashbookCoupon);
		GeneralLedgerObject crvExpenseLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.CRV_EXPENSE,
				DEFAULT_BATCH, crvObj.pcCenter, branch.currency);
		GeneralLedgerObject crvBookLdgObj = GeneralLedgerNut.getObjectFailSafe(caObj.accountType, DEFAULT_BATCH,
				crvObj.pcCenter, branch.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = crvObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CRV For " + crvObj.nameDisplay + " (" + crvObj.cardNo + ")";
		jttObj.description = " CREATED CRV";
		jttObj.amount = crvObj.voucherValue;
		jttObj.transactionDate = crvObj.dateCreate;
		jttObj.docRef = CashRebateVoucherBean.TABLENAME;
		jttObj.docKey = crvObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = crvObj.userCreate;
		jttObj.userIdEdit = crvObj.userCreate;
		// jttObj.userIdCancel= new
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvExpenseLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = crvBookLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = branch.currency;
			jeObj.amount = crvObj.voucherValue.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	/*
	 * ///////////////////////////////////////////////////////////////// ///
	 * Purchase Return and Corresponding Credit Memo /// debit accPayable xxxxx1
	 * /// credit purchaseReturn xxxxx2 /// credit inventory xxxxx3 (by cogr)
	 * 
	 * //// xxxx2 = xxxxx3 - xxxx1
	 * 
	 * public static void fnCreate(PurchaseReturnIndexObject prObj) throws
	 * Exception { BigDecimal cogr =
	 * PurchaseReturnIndexNut.getCostOfGoodsReturned(prObj.pkid); //SELECT
	 * SUM(qty),SUM(qty*unit_cost_ma) AS cogr FROM inv_stock_delta WHERE
	 * doc_table='supp_purchase_return_item' AND doc_key IN (SELECT pkid FROM
	 * supp_purchase_return_item WHERE index_id='1003'); // BranchObject branch =
	 * BranchNut.getObject(prObj.branch); SuppAccountObject suppObj =
	 * SuppAccountNut.getObject(prObj.entityId);
	 * 
	 * GeneralLedgerObject accPayLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * cogsLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_COST,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency); GeneralLedgerObject
	 * inventoryLdgObj =
	 * GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY,
	 * DEFAULT_BATCH,prObj.pcCenter, prObj.currency);
	 * 
	 * BigDecimal cogs = cogr.negate().subtract(prObj.amount);
	 * 
	 * JournalTransactionObject jttObj = new JournalTransactionObject();
	 * jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO; jttObj.pcCenterId =
	 * prObj.pcCenter; jttObj.batchId = DEFAULT_BATCH; // jttObj.typeId = //
	 * leave it as default jttObj.status = JournalTransactionBean.ACTIVE;
	 * jttObj.name = "Purchase Return "; jttObj.description = "
	 * Supplier:"+suppObj.name+" ("+suppObj.pkid.toString()+") "; jttObj.amount =
	 * prObj.amount; jttObj.transactionDate = prObj.timeComplete; jttObj.docRef =
	 * PurchaseReturnIndexBean.TABLENAME; jttObj.docKey = prObj.pkid;
	 * jttObj.state = JournalTransactionBean.STATE_CREATED; jttObj.userIdCreate =
	 * prObj.userIdUpdate; jttObj.userIdEdit = prObj.userIdUpdate;
	 * jttObj.userIdCancel= prObj.userIdUpdate; jttObj.timestampCreate =
	 * TimeFormat.getTimestamp(); jttObj.timestampEdit =
	 * TimeFormat.getTimestamp(); jttObj.timestampCancel =
	 * TimeFormat.getTimestamp();
	 *  { JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * accPayLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = prObj.amount; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * cogsLdgObj.pkId; jeObj.description = jttObj.description; jeObj.currency =
	 * prObj.currency; jeObj.amount = cogs; jttObj.vecEntry.add(jeObj); } {
	 * JournalEntryObject jeObj = new JournalEntryObject(); jeObj.glId =
	 * inventoryLdgObj.pkId; jeObj.description = jttObj.description;
	 * jeObj.currency = prObj.currency; jeObj.amount = cogr;
	 * jttObj.vecEntry.add(jeObj); } jttObj =
	 * JournalTransactionNut.fnCreate(jttObj); }
	 * 
	 */
	// / CreditMemo
	// / debit acc-payable
	// / credit purchase-return
	// / Cash Transfer
	public static void fnCreate(CashTransferItemObject ctiObj) throws Exception
	{
		CashAccountObject cashbookFrom = CashAccountNut.getObject(ctiObj.cashbookFrom);
		CashAccountObject cashbookTo = CashAccountNut.getObject(ctiObj.cashbookTo);
		GeneralLedgerObject fromCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookFrom.accountType,
				DEFAULT_BATCH, ctiObj.pcCenter, ctiObj.currency);
		GeneralLedgerObject toCashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				ctiObj.pcCenter, ctiObj.currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = ctiObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Internal Cash Transfer ";
		jttObj.description = " CashTransfer: From " + cashbookFrom.name + " " + cashbookFrom.code + " "
				+ cashbookFrom.accountNumber + " to " + cashbookTo.name + " " + cashbookTo.code + " "
				+ cashbookTo.accountNumber;
		jttObj.amount = ctiObj.amount;
		jttObj.transactionDate = ctiObj.dateStmt;
		jttObj.docRef = CashTransferItemBean.TABLENAME;
		jttObj.docKey = ctiObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = ctiObj.userPic;
		jttObj.userIdEdit = ctiObj.userPic;
		jttObj.userIdCancel = ctiObj.userPic;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = toCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = fromCashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = ctiObj.currency;
			jeObj.amount = ctiObj.amount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Bank In Slip
	public static void fnCreate(BankInSlipObject bisObj) throws Exception
	{
		CashAccountObject cashbookTo = CashAccountNut.getObject(bisObj.cashbook);
		CashAccountObject cashbookCash = null;
		CashAccountObject cashbookCheque1 = null;
		CashAccountObject cashbookCheque2 = null;
		CashAccountObject cashbookCheque3 = null;
		CashAccountObject cashbookCheque4 = null;
		CashAccountObject cashbookCheque5 = null;
		if (bisObj.cashCashbook.intValue() > 0)
		{
			cashbookCash = CashAccountNut.getObject(bisObj.cashCashbook);
		}
		if (bisObj.row1Cashbook.intValue() > 0)
		{
			cashbookCheque1 = CashAccountNut.getObject(bisObj.row1Cashbook);
		}
		if (bisObj.row2Cashbook.intValue() > 0)
		{
			cashbookCheque2 = CashAccountNut.getObject(bisObj.row2Cashbook);
		}
		if (bisObj.row3Cashbook.intValue() > 0)
		{
			cashbookCheque3 = CashAccountNut.getObject(bisObj.row3Cashbook);
		}
		if (bisObj.row4Cashbook.intValue() > 0)
		{
			cashbookCheque4 = CashAccountNut.getObject(bisObj.row4Cashbook);
		}
		if (bisObj.row5Cashbook.intValue() > 0)
		{
			cashbookCheque5 = CashAccountNut.getObject(bisObj.row5Cashbook);
		}
		GeneralLedgerObject cashToLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookTo.accountType, DEFAULT_BATCH,
				bisObj.pcCenter, bisObj.currency);
		GeneralLedgerObject cashLdgObj = null;
		if (cashbookCash != null)
		{
			cashLdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCash.accountType, DEFAULT_BATCH, bisObj.pcCenter,
					bisObj.currency);
		}
		GeneralLedgerObject cheque1LdgObj = null;
		if (cashbookCheque1 != null)
		{
			cheque1LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque1.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque2LdgObj = null;
		if (cashbookCheque2 != null)
		{
			cheque2LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque2.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque3LdgObj = null;
		if (cashbookCheque3 != null)
		{
			cheque3LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque3.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque4LdgObj = null;
		if (cashbookCheque4 != null)
		{
			cheque4LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque4.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		GeneralLedgerObject cheque5LdgObj = null;
		if (cashbookCheque5 != null)
		{
			cheque5LdgObj = GeneralLedgerNut.getObjectFailSafe(cashbookCheque5.accountType, DEFAULT_BATCH,
					bisObj.pcCenter, bisObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = bisObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Bank In Slip ";
		jttObj.description = " BankInSlip: " + bisObj.txDesc + " " + bisObj.bankCode + " " + bisObj.bankName + " "
				+ bisObj.accNumber;
		jttObj.amount = bisObj.amountTotal;
		jttObj.transactionDate = bisObj.txDate;
		jttObj.docRef = BankInSlipBean.TABLENAME;
		jttObj.docKey = bisObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = bisObj.userIdCreate;
		jttObj.userIdEdit = bisObj.userIdCreate;
		jttObj.userIdCancel = bisObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashToLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.amountTotal;
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.cashAmount.signum() != 0 && cashLdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cashLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.cashAmount.negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row1Amount.signum() != 0 && cheque1LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque1LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row1Amount.subtract(bisObj.row1Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row2Amount.signum() != 0 && cheque2LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque2LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row2Amount.subtract(bisObj.row2Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row3Amount.signum() != 0 && cheque3LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque3LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row3Amount.subtract(bisObj.row3Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row4Amount.signum() != 0 && cheque4LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque4LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row4Amount.subtract(bisObj.row4Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		if (bisObj.row5Amount.signum() != 0 && cheque5LdgObj != null)
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = cheque5LdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = bisObj.currency;
			jeObj.amount = bisObj.row5Amount.subtract(bisObj.row5Comission).negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Credit Memo
	public static void fnCreate(CreditMemoIndexObject valObj) throws Exception
	{
		GeneralLedgerObject nominalLdgObj = null;
		if (valObj.entityTable.equals(SuppAccountBean.TABLENAME))
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_PAYABLE, DEFAULT_BATCH, valObj.pcCenter,
					valObj.currency);
		} else
		{
			nominalLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.ACC_RECEIVABLE, DEFAULT_BATCH,
					valObj.pcCenter, valObj.currency);
		}
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = valObj.pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " CreditMemo ";
		jttObj.description = "To:" + valObj.entityName + "(" + valObj.entityKey + ")" + valObj.docReference;
		jttObj.amount = valObj.amount.abs();
		jttObj.transactionDate = valObj.timeCreate;
		jttObj.docRef = CreditMemoIndexBean.TABLENAME;
		jttObj.docKey = valObj.pkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = valObj.userIdCreate;
		jttObj.userIdEdit = valObj.userIdCreate;
		jttObj.userIdCancel = valObj.userIdCreate;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = nominalLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = valObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		for (int cnt = 0; cnt < valObj.vecItems.size(); cnt++)
		{
			String theGLCode = "";
			CreditMemoItemObject cmiObj = (CreditMemoItemObject) valObj.vecItems.get(cnt);
			GeneralLedgerObject genLdgObj = null;
			if (cmiObj.amount.signum() > 0)
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeDebit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			} else
			{
				genLdgObj = GeneralLedgerNut.getObjectFailSafe(cmiObj.glCodeCredit, DEFAULT_BATCH, valObj.pcCenter,
						valObj.currency);
			}
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = genLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = valObj.currency;
			jeObj.amount = cmiObj.amount;
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}

	// / Receipt Voucher
	// / Marketing Fund (thunderMatch) for Stock Gain/ Stock Loss
	public static void fnCreateStockVariance(Integer pcCenter, String currency, BigDecimal variance,
			String description, String reason, Integer userId)
	{
		GeneralLedgerObject inventoryLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY, DEFAULT_BATCH,
				pcCenter, currency);
		GeneralLedgerObject inventoryVarLdgObj = GeneralLedgerNut.getObjectFailSafe(GLCodeBean.INVENTORY_VARIANCE,
				DEFAULT_BATCH, pcCenter, currency);
		JournalTransactionObject jttObj = new JournalTransactionObject();
		jttObj.txnCode = JournalTransactionBean.TXNCODE_AUTO;
		jttObj.pcCenterId = pcCenter;
		jttObj.batchId = DEFAULT_BATCH;
		// jttObj.typeId = // leave it as default
		jttObj.status = JournalTransactionBean.ACTIVE;
		jttObj.name = " Reset Moving Average Stock Value ";
		jttObj.description = "DESC:" + description + " REASON:" + reason;
		jttObj.amount = variance.abs();
		jttObj.transactionDate = TimeFormat.getTimestamp();
		// jttObj.docRef =
		// jttObj.docKey = grnObj.mPkid;
		jttObj.state = JournalTransactionBean.STATE_CREATED;
		jttObj.userIdCreate = userId;
		jttObj.userIdEdit = userId;
		jttObj.userIdCancel = userId;
		jttObj.timestampCreate = TimeFormat.getTimestamp();
		jttObj.timestampEdit = TimeFormat.getTimestamp();
		jttObj.timestampCancel = TimeFormat.getTimestamp();
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance;
			jttObj.vecEntry.add(jeObj);
		}
		{
			JournalEntryObject jeObj = new JournalEntryObject();
			jeObj.glId = inventoryVarLdgObj.pkId;
			jeObj.description = jttObj.description;
			jeObj.currency = currency;
			jeObj.amount = variance.negate();
			jttObj.vecEntry.add(jeObj);
		}
		jttObj = JournalTransactionNut.fnCreate(jttObj);
	}
}
