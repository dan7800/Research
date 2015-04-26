/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.pos;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import com.vlee.bean.footwear.EnterFootwearItemForm;
import com.vlee.bean.footwear.IsoPrefixRow;
import com.vlee.bean.inventory.EnterStockSession;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;

public class CreateCashsaleSession extends java.lang.Object implements Serializable
{
	private static final long serialVersionUID = 0;
	public InvoiceObject invoiceObj;
	public OfficialReceiptObject receiptObj;
	public Vector prevCashsales = new Vector();
	public Vector prevReceipts = new Vector();
	private boolean saving = false;
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state;
	private BranchObject branch;
	private CustAccountObject customer = null;
	private CustUserObject custUser = null;
	private boolean bCustSvcCtr = false;
	private String referenceNo = "";
	private String name = "";
	private String remarks = "";
	private Integer userId;
	private Timestamp pdChequeDate;
	private String chequeNumber;
	private String chequeNumberPD;
	private BigDecimal cardPctCharges;
	private String cardType;
	private Timestamp tsDate;
	private boolean bSetDate = false;
	public CashAccountObject cashbookCash;
	public CashAccountObject cashbookCard;
	public CashAccountObject cashbookCheque;
	public CashAccountObject cashbookPDCheque;
	public CashAccountObject cashbookCoupon;
	public CashAccountObject cashbookOther;
	private BigDecimal amountCash = new BigDecimal(0);
	private BigDecimal amountCard = new BigDecimal(0);
	private BigDecimal amountCheque = new BigDecimal(0);
	private BigDecimal amountPDCheque = new BigDecimal(0);
	private BigDecimal amountCoupon = new BigDecimal(0);
	private BigDecimal amountOther = new BigDecimal(0);
	protected TreeMap tableRows;
	private String salesUsername = "";
	private Integer salesUserId = new Integer(0);
	private BigDecimal chequeCharges;
	private BigDecimal surcharge;
	private String ccNumber;
	private String ccName;
	private String ccType;
	private String ccBank;
	private String ccApprovalCode;
	private String ccSecurity;
	private Timestamp ccExpiry;
	private Integer cardPaymentConfig = new Integer(0);
	private Long srvNo;

	// 20080423 Jimmy
	public OfficialReceiptObject receiptObjDeposit;
	public BigDecimal receiptPayment;
	
	public void setSrvNo(Long srvNo)
	{
		this.srvNo = srvNo;
	}

	public Long getSrvNo()
	{
		return this.srvNo;
	}

	public void setSurcharge(BigDecimal sc)
	{
		this.surcharge = sc;
	}

	public BigDecimal getSurcharge()
	{
		return this.surcharge;
	}

	public void setCCDetails(String ccNumber, String ccType, String ccName, String ccBank, Timestamp ccExpiry,
			String ccApprovalCode, String ccSecurity)
	{
		this.ccNumber = ccNumber;
		this.ccType = ccType;
		this.ccName = ccName;
		this.ccBank = ccBank;
		this.ccExpiry = ccExpiry;
		this.ccApprovalCode = ccApprovalCode;
		this.ccSecurity = ccSecurity;
	}

	public String getCCNumber()
	{
		return this.ccNumber;
	}

	public String getCCName()
	{
		return this.ccName;
	}

	public String getCCType()
	{
		return this.ccType;
	}

	public String getCCBank()
	{
		return this.ccBank;
	}

	public Timestamp getCCExpiry()
	{
		return this.ccExpiry;
	}

	public String getCCApprovalCode()
	{
		return this.ccApprovalCode;
	}

	public String getCCSecurity()
	{
		return this.ccSecurity;
	}

	public String getChequeCharges()
	{
		if (this.chequeCharges != null)
		{
			return CurrencyFormat.strCcy(this.chequeCharges);
		}
		return "0.00";
	}

	public void setChequeCharges(BigDecimal chequeCharges)
	{
		this.chequeCharges = chequeCharges;
	}

	public String getPCCenter()
	{
		if (this.branch == null)
		{
			return "x";
		}
		return this.branch.accPCCenterId.toString();
	}

	public String getChequeNumber()
	{
		return this.chequeNumber;
	}

	public void setChequeNumber(String chequeNumber)
	{
		this.chequeNumber = chequeNumber;
	}

	public String getChequeNumberPD()
	{
		return this.chequeNumberPD;
	}

	public void setChequeNumberPD(String chequeNumberPD)
	{
		this.chequeNumberPD = chequeNumberPD;
	}

	public void setDate(Timestamp theDate)
	{
		if (TimeFormat.strDisplayDate(theDate).equals(TimeFormat.strDisplayDate(this.tsDate)))
		{
			return;
		}
		this.tsDate = theDate;
		bSetDate = true;
	}

	public Timestamp getDate()
	{
		if (bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		return this.tsDate;
	}

	public String getDate(String buffer)
	{
		if (bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		return TimeFormat.strDisplayDate(this.tsDate);
	}

	public Timestamp getPDChequeDate()
	{
		return this.pdChequeDate;
	}

	public void setPDChequeDate(Timestamp pdDate)
	{
		this.pdChequeDate = pdDate;
	}

	public BigDecimal getAmountCash()
	{
		return this.amountCash;
	}

	public void setAmountCash(BigDecimal amt)
	{
		this.amountCash = amt;
	}

	public BigDecimal getAmountCard()
	{
		return this.amountCard;
	}

	public void setAmountCard(BigDecimal amt)
	{
		this.amountCard = amt;
	}

	public BigDecimal getCardPctCharges()
	{
		return this.cardPctCharges;
	}

	public void setCardPctCharges(BigDecimal amt)
	{
		this.cardPctCharges = amt;
	}

	public BigDecimal getCardCharges()
	{
		return this.cardPctCharges.multiply(getAmountCard()).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN);
	}

	public String getCardType()
	{
		return this.cardType;
	}

	public void setCardType(String type)
	{
		this.cardType = type;
	}

	public BigDecimal getAmountCheque()
	{
		return this.amountCheque;
	}

	public void setAmountCheque(BigDecimal amt)
	{
		this.amountCheque = amt;
	}

	public BigDecimal getAmountPDCheque()
	{
		return this.amountPDCheque;
	}

	public void setAmountPDCheque(BigDecimal amt)
	{
		this.amountPDCheque = amt;
	}

	public BigDecimal getAmountOther()
	{
		return this.amountOther;
	}

	public void setAmountOther(BigDecimal amt)
	{
		this.amountOther = amt;
	}

	public BigDecimal getAmountCoupon()
	{
		return this.amountCoupon;
	}

	public void setAmountCoupon(BigDecimal amt)
	{
		this.amountCoupon = amt;
	}

	public String getCardCashbook()
	{
		if (this.cashbookCard == null)
		{
			return "0";
		}
		return this.cashbookCard.pkId.toString();
	}

	public void setCardCashbook(Integer cardBook)
	{
		this.cashbookCard = CashAccountNut.getObject(cardBook);
	}

	public String getChequeCashbook()
	{
		if (this.cashbookCheque == null)
		{
			return "0";
		}
		return this.cashbookCheque.pkId.toString();
	}

	public void setChequeCashbook(Integer chequeBook)
	{
		this.cashbookCheque = CashAccountNut.getObject(chequeBook);
	}

	public BigDecimal getBillAmount()
	{
		BigDecimal totalBill = new BigDecimal(0);
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalBill = totalBill.add(docrow.getQty().multiply(docrow.getNetPrice()));
			// 20080314 Jimmy
			totalBill = totalBill.add(docrow.getTaxAmt());
		}
		return totalBill;
	}

	public BigDecimal getCostAmount()
	{
		BigDecimal totalCost = new BigDecimal(0);
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalCost = totalCost.add(docrow.getQty().multiply(docrow.getAverageCost()));
		}
		return totalCost;
	}

	public BigDecimal getReceiptAmt()
	{
		BigDecimal totalReceipt = this.amountCash.add(this.amountCard).add(this.amountCheque).add(this.amountPDCheque)
				.add(this.amountCoupon).add(this.amountOther);
		return totalReceipt;
	}

	public void getCheckReceipt() throws Exception
	{
		//if (this.getReceiptAmt().compareTo(getBillAmount()) != 0)
		//{
		//	throw new Exception("Receipt Amt does not tally with Bill Amt");
		//}
		if (this.getReceiptAmt().add(this.getReceiptPayment()).compareTo(getBillAmount()) != 0)
		{
			throw new Exception("Receipt Amt does not tally with Bill Amt");
		}
		if (this.amountCheque.signum() > 0 && this.chequeNumber.length() < 6)
		{
			throw new Exception("Please enter the cheque number");
		}
		if (this.amountPDCheque.signum() > 0 && this.chequeNumberPD.length() < 6)
		{
			throw new Exception("Please enter the Post Dated Cheque Number");
		}
	}

	public boolean getValidReceipt()
	{
		try
		{
			getCheckReceipt();
		} catch (Exception ex)
		{
			return false;
		}
		return true;
	}

	// TKW20071113: Checks inventory qty before creating an invoice. Enough
	// said.
	public String checkInventoryQty(Integer locationid) throws Exception
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		TreeMap objSort = new TreeMap();
		// LocationObject lObj = LocationNut.getObject(locationid);
		DocRow objdrow = new DocRow();
		Stock stkEJB;
		StockObject stkObj;
		Log.printVerbose("Entered checkInventoryQty, size: " + this.tableRows.size());
		for (int i = 0; i < this.tableRows.size(); i++)
		{
			objdrow = (DocRow) vecDocRow.get(i);
			Log.printVerbose("Interation: " + i);
			if (objdrow.getBomId().intValue() == 0 && !objdrow.getPackageGroup().equals(""))
			{
				Log.printVerbose("package header detected.");
				// Don't check; this is a package header.
			} else
			{
				// Add into TreeMap. This is because users may enter in
				// identical items,
				// but different entries. The total qty of such items must be
				// checked against
				// the stock values in the database, instead of individually
				// comparing them,
				// which is inaccurate.
				// If the item doesn't exist, put it in the TreeMap
				if (objSort.get(new Integer(objdrow.itemId)) == null)
				{
					DocRow objdrowTemp = new DocRow();
					objdrowTemp.itemId = objdrow.itemId;
					objdrowTemp.itemCode = objdrow.itemCode;
					objdrowTemp.qty = objdrow.getQty();
					objSort.put(new Integer(objdrow.itemId), objdrowTemp);
				} else
				// If the item exists, just add the current qty to that, to get
				// the total qty
				// for that particular item Id.
				{
					DocRow objdrowTemp2 = (DocRow) objSort.get(new Integer(objdrow.itemId));
					objdrowTemp2.itemId = objdrow.itemId;
					objdrowTemp2.itemCode = objdrow.itemCode;
					objdrowTemp2.qty = objdrowTemp2.qty.add(objdrow.getQty());
					objSort.remove(new Integer(objdrow.itemId));
					objSort.put(new Integer(objdrow.itemId), objdrowTemp2);
				}
			}
		}
		// Now to actually check if those quantities can be supported by
		// existing stock.
		Vector vecQty = new Vector(objSort.values());
		for (int j = 0; j < vecQty.size(); j++)
		{
			DocRow objdrowQty = (DocRow) vecQty.get(j);
			stkEJB = StockNut.getObjectBy(new Integer(objdrowQty.itemId), locationid, new Integer(0));
			if (stkEJB == null)
			{
				Log.printVerbose("Never had any stock.");
				return objdrowQty.itemCode;
			}
			stkObj = stkEJB.getObject();
			Log.printVerbose("stkObj created.");
			if (stkObj.balance.compareTo(objdrowQty.getQty()) < 0)
			{
				Log.printVerbose("Stock(" + stkObj.balance + ") less than required(" + objdrowQty.getQty() + ").");
				return objdrowQty.itemCode;
			}
			Log.printVerbose(objdrowQty.itemCode + " has enough qty in stock: " + stkObj.balance.toString()
					+ "compared to the invoice's requirements: " + objdrowQty.getQty());
		}
		return "#GOOD#";
	}

	public void addStockWithItemCode(DocRow docr) throws Exception
	{
		docr.setUser1(this.salesUserId.intValue());
		// / check if this row exists in the list already
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			if (docrow.getKey().equals(docr.getKey()))
			{
				throw new Exception(" The item exist in the list already!");
			}
		}
		// / add it
		try
		{
			this.tableRows.put(docr.getKey(), docr);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	public DocRow dropDocRow(String key)
	{
		return (DocRow) this.tableRows.remove(key);
	}

	public String getState()
	{
		return this.state;
	}

	public boolean getValidBranch()
	{
		return bCustSvcCtr;
	}

	public void setReferenceNo(String refNum)
	{
		this.referenceNo = refNum;
	}

	public String getReferenceNo()
	{
		return this.referenceNo;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public String getRemarks()
	{
		return this.remarks;
	}

	public void setCustUser(CustUserObject custUserObj)
	{
		this.customer = CustAccountNut.getObject(custUserObj.accId);
		// 20080423 Jimmy - change another customer, clear previous Receipt Session.
	      resetReceipt();
	    // End
		if (this.customer == null)
		{
			return;
		}
		setCustomer(this.customer.pkid);
		this.custUser = custUserObj;
		this.name = custUserObj.getName();
	}

	public void resetCustUser()
	{
		this.custUser = null;
	}

	public CustUserObject getCustUser()
	{
		return this.custUser;
	}

	public boolean setBranch(Integer iCustSvcCtr)
	{
		reset();
		this.branch = BranchNut.getObject(iCustSvcCtr);
		if (this.branch != null)
		{
			this.bCustSvcCtr = true;
			// / need to load all cash account objects
			this.cashbookCash = CashAccountNut.getObject(this.branch.cashbookCash);
			this.cashbookCard = CashAccountNut.getObject(this.branch.cashbookCard);
			this.cashbookCheque = CashAccountNut.getObject(this.branch.cashbookCheque);
			this.cashbookPDCheque = CashAccountNut.getObject(this.branch.cashbookPDCheque);
			this.cashbookCoupon = CashAccountNut.getObject(this.branch.cashbookCoupon);
			this.cashbookOther = CashAccountNut.getObject(this.branch.cashbookOther);
			return true;
		} else
		{
			this.bCustSvcCtr = false;
			return false;
		}
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public Integer getBranchId()
	{
		return this.branch.pkid;
	}

	public boolean setCustomer(Integer iCustomer)
	{
		this.customer = CustAccountNut.getObject(iCustomer);
		// 20080423 Jimmy - change another customer, clear previous Receipt Session.
	     resetReceipt();
	    // End
		if (this.customer != null)
		{
			this.name = this.customer.name;
			return true;
		} else
		{
			return false;
		}
	}

	public String getCustomerId()
	{
		if (this.customer == null)
		{
			return "";
		} else
		{
			return this.customer.pkid.toString();
		}
	}

	public CustAccountObject getCustomer()
	{
		return this.customer;
	}

	public String getCardPaymentConfig(String buf)
	{
		if (this.cardPaymentConfig != null)
		{
			return this.cardPaymentConfig.toString();
		}
		return "0";
	}

	public void setCardPaymentConfig(String cpcPkid)
	{
		try
		{
			Integer cpcId = new Integer(cpcPkid);
			this.cardPaymentConfig = cpcId;
			CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(cpcId);
			setCardType("");
			setSurcharge(new BigDecimal(0));
			setCardPctCharges(new BigDecimal(0));
			if (cpcObj != null)
			{
				setCardType(cpcObj.paymentMode);
				if (cpcObj.policyCharges.equals(CardPaymentConfigBean.POLICY_ABSORB_CHARGES))
				{
					setCardPctCharges(cpcObj.pctCharges.multiply(new BigDecimal(100)));
				}
				if (cpcObj.policyCharges.equals(CardPaymentConfigBean.POLICY_SURCHARGE))
				{
					BigDecimal surchargeApplied = cpcObj.pctCharges.multiply(getAmountCard());
					surchargeApplied = new BigDecimal(CurrencyFormat.strCcy(surchargeApplied));
					setSurcharge(surchargeApplied);
				}
				if (cpcObj.cashbookOpt.equals(CardPaymentConfigBean.CB_AUTO_SELECT))
				{
					// make sure the cashbook exists
					CashAccountObject cashbookCardOpt = CashAccountNut.getObject(cpcObj.cashbook);
					if (cashbookCardOpt != null)
					{
						setCardCashbook(cpcObj.cashbook);
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void saveCashsale()
	{
		// Populate Defaults
		// this.invoiceObj.mSalesTxnId = // automatically created when default
		// is zero
		// this.invoiceObj.mPaymentTermsId = pmtTerm;
		// this.invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		if (bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		// If the user did not specify the Invoice Format, then use the default
		// one. Else use the one specified
		System.out.println("this.branch.formatInvoiceType : " + this.branch.formatInvoiceType);
		if (!"".equals(this.branch.formatInvoiceType))
		{
			this.invoiceObj.mDisplayFormat = this.branch.formatInvoiceType;
			System.out.println("this.invoiceObj.mDisplayFormat : " + this.invoiceObj.mDisplayFormat);
		} else
		{
			this.invoiceObj.mDisplayFormat = InvoiceBean.CASHBILL_TRADING_1;
			System.out.println("this.invoiceObj.mDisplayFormat : " + this.invoiceObj.mDisplayFormat);
		}
		this.invoiceObj.mTimeIssued = this.tsDate;
		this.invoiceObj.mCurrency = this.branch.currency;
		// 20080312 Jimmy -> include Tax
		this.invoiceObj.mTotalAmt = getTotalAmt();
		this.invoiceObj.mOutstandingAmt = getTotalAmt();
		this.invoiceObj.mOutstandingBfPdc = getTotalAmt();
		this.invoiceObj.taxAmount = getTotalTax();
		// --------------
		// this.invoiceObj.mTotalAmt = getTotalAmt();
		// this.invoiceObj.mOutstandingAmt = getTotalAmt();
		this.invoiceObj.mRemarks = this.remarks;
		// this.invoiceObj.mState = InvoiceBean.ST_CREATED; // 10
		// this.invoiceObj.mStatus = InvoiceBean.STATUS_ACTIVE;
		this.invoiceObj.mLastUpdate = TimeFormat.getTimestamp();
		this.invoiceObj.mUserIdUpdate = this.userId;
		this.invoiceObj.mEntityTable = CustAccountBean.TABLENAME;
		this.invoiceObj.mEntityKey = this.customer.pkid;
		this.invoiceObj.mEntityName = this.customer.name;
		if (this.name.length() > 3)
		{
			this.invoiceObj.mEntityName = this.name;
		}
		// this.invoiceObj.mEntityType = "";
		this.invoiceObj.mIdentityNumber = this.customer.identityNumber;
		this.invoiceObj.mEntityContactPerson = this.customer.getName();
		this.invoiceObj.mForeignTable = ""; // 20
		this.invoiceObj.mForeignKey = new Integer(0);
		this.invoiceObj.mForeignText = "";
		this.invoiceObj.mCustSvcCtrId = this.branch.pkid;
		this.invoiceObj.mLocationId = this.branch.invLocationId;
		this.invoiceObj.mPCCenter = this.branch.accPCCenterId;
		// this.invoiceObj.mTxnType = "";
		// this.invoiceObj.mStmtType = "";
		this.invoiceObj.mReferenceNo = this.referenceNo;
		this.invoiceObj.mDescription = "";
		this.invoiceObj.mWorkOrder = new Long(0); // 30
		this.invoiceObj.mDeliveryOrder = new Long(0);
		this.invoiceObj.mReceiptId = new Long(0);
		this.invoiceObj.mDocType = InvoiceBean.CASHBILL;
		this.invoiceObj.salesId = this.salesUserId;
		this.invoiceObj.policyNumber = "";
		this.invoiceObj.accidentDate = TimeFormat.getTimestamp();
		this.invoiceObj.claimAmount = new BigDecimal(0);
		this.invoiceObj.billingHandphone = this.customer.mobilePhone;
		this.invoiceObj.billingPhone1 = this.customer.telephone1;
		this.invoiceObj.billingPhone2 = this.customer.telephone2;
		this.invoiceObj.billingFax = this.customer.faxNo;
		this.invoiceObj.billingEmail = this.customer.email1;
		this.invoiceObj.billingCompanyName = "";
		this.invoiceObj.billingAdd1 = this.customer.mainAddress1;
		this.invoiceObj.billingAdd2 = this.customer.mainAddress2;
		this.invoiceObj.billingAdd3 = this.customer.mainAddress3;
		this.invoiceObj.billingCity = this.customer.mainCity;
		this.invoiceObj.billingZip = this.customer.mainPostcode;
		this.invoiceObj.billingState = this.customer.mainState;
		this.invoiceObj.billingCountry = this.customer.mainCountry;
		this.invoiceObj.receiverName = this.customer.name;
		this.invoiceObj.receiverHandphone = this.customer.mobilePhone;
		this.invoiceObj.receiverPhone1 = this.customer.telephone1;
		this.invoiceObj.receiverPhone2 = this.customer.telephone2;
		this.invoiceObj.receiverFax = this.customer.faxNo;
		this.invoiceObj.receiverEmail = this.customer.email1;
		this.invoiceObj.receiverCompanyName = "";
		this.invoiceObj.receiverAdd1 = this.customer.mainAddress1;
		this.invoiceObj.receiverAdd2 = this.customer.mainAddress2;
		this.invoiceObj.receiverAdd3 = this.customer.mainAddress3;
		this.invoiceObj.receiverCity = this.customer.mainCity;
		this.invoiceObj.receiverZip = this.customer.mainPostcode;
		this.invoiceObj.receiverState = this.customer.mainState;
		this.invoiceObj.receiverCountry = this.customer.mainCountry;
		if (this.custUser != null)
		{
			this.invoiceObj.mForeignTable = CustUserBean.TABLENAME;
			this.invoiceObj.mForeignKey = this.custUser.pkid;
			this.invoiceObj.receiverName = this.custUser.getName();
			this.invoiceObj.receiverHandphone = this.custUser.mobilePhone;
			this.invoiceObj.receiverPhone1 = this.custUser.telephone1;
			this.invoiceObj.receiverPhone2 = this.custUser.telephone2;
			this.invoiceObj.receiverFax = this.custUser.faxNo;
			this.invoiceObj.receiverEmail = this.custUser.email1;
			this.invoiceObj.receiverCompanyName = "";
			this.invoiceObj.receiverAdd1 = this.custUser.mainAddress1;
			this.invoiceObj.receiverAdd2 = this.custUser.mainAddress2;
			this.invoiceObj.receiverAdd3 = this.custUser.mainAddress3;
			this.invoiceObj.receiverCity = this.custUser.mainCity;
			this.invoiceObj.receiverZip = this.custUser.mainPostcode;
			this.invoiceObj.receiverState = this.custUser.mainState;
			this.invoiceObj.receiverCountry = this.custUser.mainCountry;
		}
		Invoice invoiceEJB = InvoiceNut.fnCreate(this.invoiceObj);
		// / create invoice items
		Vector vecDocRow = new Vector(this.tableRows.values());
		Vector vecCashsaleItemEJB = new Vector();
		try
		{
			for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt1);
				InvoiceItemObject iiObj = new InvoiceItemObject(this.invoiceObj, docrow);
				InvoiceItem iiEJB = InvoiceItemNut.fnCreate(iiObj);
				vecCashsaleItemEJB.add(iiEJB);
				this.invoiceObj.vecInvoiceItems.add(iiObj);
				// / effect the Stock Delta, Stock balance etc.
				// / we are not doing this at the InvoiceItemNut level
				// / because when DeliveryOrder is used, creation of
				// / invoice does not affect the stock
				// / however, from accounting perspective
				// / once stock is delivered, asset reduced, by right
				// / there should be a corresponding increase in
				// / Account receivable... to be investigated later..
				// StockNut.sell(this.invoiceObj.mUserIdUpdate, //Integer
				// personInCharge,
				StockNut.sell(
						iiObj.mPic2, // Integer personInCharge,
						iiObj.mItemId,// Integer itemId,
						this.invoiceObj.mLocationId, this.invoiceObj.mPCCenter, iiObj.mTotalQty,
						iiObj.mUnitPriceQuoted, iiObj.mCurrency, InvoiceItemBean.TABLENAME,
						iiObj.mPkid,
						"", // remarks
						this.invoiceObj.mTimeIssued, this.invoiceObj.mUserIdUpdate, new Vector(iiObj.colSerialObj),
						iiObj.codeProject, iiObj.codeDepartment, iiObj.codeDealer, iiObj.codeSalesman,
						this.invoiceObj.mCustSvcCtrId, iiObj.mTaxAmt);
			}
			JournalTxnLogic.fnCreate(this.invoiceObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			// roll back the transactions
			try
			{
				for (int cnt2 = 0; cnt2 < vecCashsaleItemEJB.size(); cnt2++)
				{
					InvoiceItem iiEJB = (InvoiceItem) vecCashsaleItemEJB.get(cnt2);
					iiEJB.remove();
				}
				invoiceEJB.remove();
			} catch (Exception ex2)
			{
				ex.printStackTrace();
			}
		}
		// / recalculate Cashsale Amount
		// try { InvoiceNut.recalcInvAmt(this.invoiceObj); }
		// catch(Exception ex) { ex.printStackTrace(); }
		try
		{
			invoiceEJB.setOutstandingAmt(new BigDecimal(0));
			invoiceEJB.setOutstandingBfPdc(this.amountPDCheque);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		// // IMPORTANT!!
		// // create the receipt
		if (checkAmount())
		{
			this.receiptObj = new OfficialReceiptObject();
			this.receiptObj.entityTable = CustAccountBean.TABLENAME;
			this.receiptObj.entityKey = this.customer.pkid;
			this.receiptObj.entityName = this.customer.name;
			if (this.name.length() > 3)
			{
				this.receiptObj.entityName = this.name;
			}
			this.receiptObj.currency = this.invoiceObj.mCurrency;
			this.receiptObj.amount = this.getReceiptAmt();
			// this.receiptObj.paymentTime = TimeFormat.getTimestamp();
			this.receiptObj.paymentTime = this.tsDate;
			// this.receiptObj.paymentMethod =
			this.receiptObj.paymentRemarks = this.remarks;
			if (this.amountCheque.signum() > 0)
			{
				this.receiptObj.chequeNumber = this.chequeNumber;
			}
			this.receiptObj.lastUpdate = TimeFormat.getTimestamp();
			this.receiptObj.userIdUpdate = this.userId;
			this.receiptObj.cbCash = this.cashbookCash.pkId;
			this.receiptObj.cbCard = this.cashbookCard.pkId;
			this.receiptObj.cbCheque = this.cashbookCheque.pkId;
			this.receiptObj.cbPDCheque = this.cashbookPDCheque.pkId;
			this.receiptObj.cbCoupon = this.cashbookCoupon.pkId;
			this.receiptObj.cbOther = this.cashbookOther.pkId;
			this.receiptObj.amountCash = this.amountCash;
			this.receiptObj.amountCard = this.amountCard;
			this.receiptObj.amountCheque = this.amountCheque;
			this.receiptObj.amountPDCheque = this.amountPDCheque;
			if (this.amountPDCheque.signum() > 0)
			{
				this.receiptObj.chequeNumberPD = this.chequeNumberPD;
			}
			this.receiptObj.amountCoupon = this.amountCoupon;
			this.receiptObj.amountOther = this.amountOther;
			this.receiptObj.datePDCheque = this.pdChequeDate;
			this.receiptObj.branch = this.branch.pkid;
			this.receiptObj.pcCenter = this.branch.accPCCenterId;
			this.receiptObj.cardPctCharges = this.cardPctCharges;
			this.receiptObj.cardCharges = getCardCharges();
			this.receiptObj.cardType = StringManup.truncate(this.ccType + "-" + this.cardType, 19);
			if (this.receiptObj.amountCard.signum() == 0)
			{
				this.receiptObj.cardType = "";
			}
			this.receiptObj.chequeChargesAmount = this.chequeCharges;
			this.receiptObj.cardSurcharge = this.surcharge;
			this.receiptObj.cardName = this.ccName;
			// this.receiptObj.cardType = this.ccType ;
			this.receiptObj.cardBank = this.ccBank;
			this.receiptObj.cardNumber = this.ccNumber;
			this.receiptObj.cardValidThru = this.ccExpiry;
			this.receiptObj.cardApprovalCode = this.ccApprovalCode;
			this.receiptObj.cardSecurityNum = this.ccSecurity;
			this.receiptObj.cardId = this.cardPaymentConfig; // 20080528 Jimmy
			OfficialReceiptNut.fnCreate(this.receiptObj);
			this.invoiceObj.mReceiptId = this.receiptObj.pkid;
			try
			{
				invoiceEJB.setObject(this.invoiceObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		//// end receipt created
		
		// /////////// MUST CREATE THE DOC LINK BEAN HERE!!!!
		DocLinkObject dlObj = new DocLinkObject();
		dlObj.namespace = DocLinkBean.NS_CUSTOMER;
		// dlObj.reference = "";
		dlObj.relType = DocLinkBean.RELTYPE_PYMT_INV;
		dlObj.srcDocRef = OfficialReceiptBean.TABLENAME;
		dlObj.srcDocId = this.receiptObj.pkid;
		dlObj.tgtDocRef = InvoiceBean.TABLENAME;
		dlObj.tgtDocId = this.invoiceObj.mPkid;
		dlObj.currency = this.invoiceObj.mCurrency;
		dlObj.amount = this.receiptObj.amount.negate();
		// dlObj.currency2 = "";
		// dlObj.amount2 = new BigDecimal("0.00");
		// dlObj.remarks = "";
		dlObj.status = DocLinkBean.STATUS_ACTIVE;
		dlObj.lastUpdate = TimeFormat.getTimestamp();
		dlObj.userIdUpdate = this.userId;
		DocLinkNut.fnCreate(dlObj);
	}

	// / contructor!
	public CreateCashsaleSession(Integer iUser)
	{
		this.invoiceObj = new InvoiceObject();
		this.receiptObj = new OfficialReceiptObject();
		this.state = STATE_DRAFT;
		this.bCustSvcCtr = false;
		this.customer = CustAccountNut.getObject(CustAccountBean.PKID_CASH);
		this.tableRows = new TreeMap();
		this.userId = iUser;
		this.pdChequeDate = TimeFormat.getTimestamp();
		this.pdChequeDate = TimeFormat.add(this.pdChequeDate, 0, 1, 0);
		this.chequeNumber = "";
		this.chequeNumberPD = "";
		this.cardType = "";
		this.cardPctCharges = new BigDecimal("0.0");
		this.tsDate = TimeFormat.getTimestamp();
		this.salesUsername = UserNut.getUserName(iUser);
		this.salesUserId = iUser;
		UserObject userObj = UserNut.getObject(this.userId);
		this.salesUsername = userObj.userName;
		this.chequeCharges = new BigDecimal(0);
		this.surcharge = new BigDecimal(0);
		this.ccNumber = "";
		this.ccName = "";
		this.ccType = "";
		this.ccBank = "";
		this.ccApprovalCode = "";
		this.ccSecurity = "";
		this.ccExpiry = TimeFormat.getTimestamp();
		this.srvNo = new Long(0);
	}

	public void setSalesUsername(String username) throws Exception
	{
		Integer buf = UserNut.getUserId(username);
		if (buf == null)
		{
			throw new Exception("Invalid salesman username!!");
		}
		this.salesUsername = username;
		this.salesUserId = buf;
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			docrow.setUser1(this.salesUserId.intValue());
		}
	}

	public void setSalesUserId(Integer userId) throws Exception
	{
		UserObject userObj = UserNut.getObject(userId);
		this.salesUsername = userObj.userName;
		this.salesUserId = userId;
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			docrow.setUser1(this.salesUserId.intValue());
		}
	}

	public Integer getSalesUserId()
	{
		return this.salesUserId;
	}

	public String getSalesUsername()
	{
		return this.salesUsername;
	}

	public void reset()
	{
		this.state = STATE_DRAFT;
		// bCustSvcCtr = false;
		// bCustomer = false;
		this.tableRows.clear();
		this.invoiceObj = new InvoiceObject();
		this.receiptObj = new OfficialReceiptObject();
		this.amountCash = new BigDecimal(0);
		this.amountCard = new BigDecimal(0);
		this.amountCheque = new BigDecimal(0);
		this.amountPDCheque = new BigDecimal(0);
		this.amountCoupon = new BigDecimal(0);
		this.amountOther = new BigDecimal(0);
		if (this.branch != null)
		{
			this.bCustSvcCtr = true;
			// / need to load all cash account objects
			this.cashbookCash = CashAccountNut.getObject(this.branch.cashbookCash);
			this.cashbookCard = CashAccountNut.getObject(this.branch.cashbookCard);
			this.cashbookCheque = CashAccountNut.getObject(this.branch.cashbookCheque);
			this.cashbookPDCheque = CashAccountNut.getObject(this.branch.cashbookPDCheque);
			this.cashbookCoupon = CashAccountNut.getObject(this.branch.cashbookCoupon);
			this.cashbookOther = CashAccountNut.getObject(this.branch.cashbookOther);
		}
		this.chequeCharges = new BigDecimal(0);
		this.surcharge = new BigDecimal(0);
		this.ccNumber = "";
		this.ccName = "";
		this.ccType = "";
		this.ccBank = "";
		this.ccApprovalCode = "";
		this.ccSecurity = "";
		this.ccExpiry = TimeFormat.getTimestamp();
		this.tsDate = TimeFormat.getTimestamp();
		this.bSetDate = false;
		this.remarks = "";
		this.cardPaymentConfig = new Integer(0);
		
		// 20080423 Jimmy
		this.resetReceipt();
	}

	public synchronized void confirmAndSave() throws Exception
	{
		if (saving == true)
			return;
		// Check stock balance
		try
		{
			System.out.println("Inside confirmAndSave");
			checkNegativeQuantity();
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
		System.out.println("finish checking stkObj qty");
		if (!canSave())
			throw new Exception("In complete form!");
		saving = true;
		// 1) create the PurchaseOrder and GRN
		saveCashsale();
		transferExistingDocToPrevBuffer();
		updateSerialNumberIndex();
		
		if (this.getSrvNo().intValue() != 0)
		{
			System.out.println("Going to update RMA Ticket");
			updateRMATicket();
		}
		
		// 20080423 Jimmy - for deposit settlement
		knockOffDocuments();
		saving = false;
		reset();
	}

	private boolean checkNegativeQuantity() throws Exception
	{
		System.out.println("Inside checkNegativeQuantity");
		// int STK_COND_GOOD = 0;
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			if (this.branch.invLocationId != null && docrow.getItemId() != 0 && docrow.getItemCode() != null)
			{
				Stock stkEJB = StockNut.getObjectBy(new Integer(docrow.getItemId()), this.branch.invLocationId,
						StockBean.COND_DEF);
				StockObject stkObj = null;
				if (stkEJB != null)
				{
					try
					{
						stkObj = stkEJB.getObject();
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				} else
				{
					stkObj = null;
				}
				if (stkObj == null)
				{
					System.out.println("stkObj is null");
					throw new Exception("Invalid Stock!");
				}
				if (docrow.getQty().compareTo(stkObj.balance) > 0)
				{
					System.out.println("stkObj qty will be negative");
					throw new Exception("Negative Stock!");
				}
			} else
				throw new Exception("Incomplete Form!");
		}
		System.out.println("stkObj qty is OK");
		return true;
	}

	private void updateSerialNumberIndex()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		try
		{
			for (int x = 0; x < vecDocRow.size(); x++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(x);
				for (int y = 0; y < docrow.vecSerial.size(); y++)
				{
					String sN = (String) docrow.vecSerial.get(y);
					try
					{
						Long pkid = new Long(sN);
						SerialNumberIndex serialnumber = SerialNumberIndexNut.getHandle(pkid);
						SerialNumberIndexObject obj = serialnumber.getObject();
						obj.serial1 = StringManup.lPad(obj.pkid.toString(), 6, '0');
						obj.status = SerialNumberIndexBean.CASHSALES;
						obj.useridEdit = this.userId;
						obj.timeEdit = TimeFormat.getTimestamp();
						serialnumber.setObject(obj);
					} catch (Exception ex)
					{
						Log.printDebug("No SerialNumberIndex Found");
					}
				}
			}
		} catch (Exception e)
		{
		}
	}

	private void transferExistingDocToPrevBuffer()
	{
		this.prevCashsales.add(this.invoiceObj);
		this.prevReceipts.add(this.receiptObj);
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
	}

	public boolean canSave()
	{
		boolean result = true;
		// check state
		if (!this.state.equals(STATE_DRAFT))
		{
			result = false;
		}
		// check valid procurement center
		if (this.branch == null)
		{
			result = false;
		}
		// check valid customer
		if (this.customer == null)
		{
			result = false;
		}
		// check valid qty
		if (this.getTotalQty().compareTo(new BigDecimal(0)) <= 0)
		{
			result = false;
		}
		// check valid amount
		try
		{
			getCheckReceipt();
		} catch (Exception ex)
		{
			result = false;
		}
		// check whether there are any serialized items but without serial numbers due to jobsheet
		if(hasSerializedItemNoSerialNumber())
		{
			result = false;
		}
		
		// 20080326 Jimmy - check whether there are any qty of serialized items not same qty of BOM header.
		if (qtySerializedItemNotSame())
		{
			result = false;
		}
		
		// / no check at this time
		return result;
	}

	public BigDecimal getTotalQty()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		BigDecimal totalQty = new BigDecimal(0);
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalQty = totalQty.add(docrow.getQty());
		}
		return totalQty;
	}

	public BigDecimal getTotalAmt()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalAmt = totalAmt.add(docrow.getQty().multiply(docrow.getNetPrice()));
			totalAmt = totalAmt.add(docrow.getTaxAmt());
		}
		return totalAmt;
	}


	public BigDecimal getTotalTax()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalAmt = totalAmt.add(docrow.getTaxAmt());
		}
		return totalAmt;
	}

	// --------------------
	public void fnAddStockWithItemCode(DocRow docr) throws Exception
	{
		docr.setUser1(this.salesUserId.intValue());
		docr.setIncludeCodeInKey(false);
		// / check if this row exists in the list already
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow dcrw = (DocRow) vecDocRow.get(cnt);
			if (dcrw.getKey2().equals(docr.getKey2()))
			{
				this.tableRows.remove(dcrw.getKey());
			}
		}
		// / check if this row exists in the list already
		vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow oldrow = (DocRow) vecDocRow.get(cnt);
			for (int cnt1 = 0; cnt1 < docr.vecSerial.size(); cnt1++)
			{
				String theSN = (String) docr.vecSerial.get(cnt1);
				for (int cnt2 = 0; cnt2 < oldrow.vecSerial.size(); cnt2++)
				{
					String theSN2 = (String) oldrow.vecSerial.get(cnt2);
					if (theSN2.equals(theSN))
					{
						throw new Exception("The Serial Number Has Already Been Added To This Invoice!");
					}
				}
			}
		}
		// / add it
		try
		{
			this.tableRows.put(docr.getKey(), docr);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	public Vector getPreviousCashsales()
	{
		return this.prevCashsales;
	}

	public Vector getPreviousReceipts()
	{
		return this.prevReceipts;
	}

	public void setName(String newName)
	{
		this.name = newName;
	}

	public String getName()
	{
		return this.name;
	}

	public void updateAccount()
	{
		this.customer = CustAccountNut.getObject(this.customer.pkid);
		if (this.custUser == null)
		{
			System.out.println("this.custUser is null");
			this.name = this.customer.name;
		}
	}

	public void updateContact()
	{
		this.custUser = CustUserNut.getObject(this.custUser.pkid);
		this.name = this.custUser.getName();
	}

	private void updateRMATicket()
	{
		if (!"0".equals(this.srvNo.toString()))
		{
			RMATicketObject rmaObj = RMATicketNut.getObject(this.srvNo);
			RMATicket rmaEJB = RMATicketNut.getHandle(this.srvNo);
			// Update Invoice
			if (!"0".equals(this.invoiceObj.mPkid.toString()))
			{
				InvoiceObject invObj = InvoiceNut.getObject(this.invoiceObj.mPkid);
				Invoice invEJB = InvoiceNut.getHandle(this.invoiceObj.mPkid);
				String invRemarks = invObj.mRemarks;
				invRemarks += " (Itm:" + rmaObj.itemCode + " Problem:" + rmaObj.problemStmt + " Rmk:" + rmaObj.remarks
						+ ")";
				invObj.mRemarks = StringManup.truncate(invRemarks, 800);
				System.out.println("INV remarks : " + invObj.mRemarks);
				System.out.println("remarks : " + invRemarks);
				try
				{
					invEJB.setObject(invObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			// Update RMA
			String rmaReference = "";
			if (!"0".equals(this.invoiceObj.mPkid.toString()))
			{
				rmaReference = this.invoiceObj.mPkid.toString();
				// rmaObj.status = RMATicketBean.STATUS_BILLED;
			}
			rmaObj.supplierReference = StringManup.truncate(rmaReference, 50);
			System.out.println("RMA Inv : " + rmaObj.supplierReference);
			System.out.println("Reference : " + rmaReference);
			try
			{
				rmaEJB.setObject(rmaObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			this.srvNo = new Long(0);
		}
	}
	
	public void dropPackageDocRow(String key)
	{
		// Parameter key represents the key to the package header.
		DocRow objdrow = (DocRow) this.tableRows.get(key);
		// The package header and its children all share the same package_group.
		String packageGuid = objdrow.package_group;
		
		// Remove the package header and its children.
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow dcrw = (DocRow) vecDocRow.get(cnt);
			if(dcrw.package_group.equals(packageGuid))
			{
				this.tableRows.remove(dcrw.getKey());
			}
		}		
	}
	
	public boolean hasSerializedItemNoSerialNumber()
	{
		   TreeMap tableRows = this.getTableRows();
		   Vector vecDocRow = new Vector(tableRows.values());		   
		   boolean alertSerialized = false;
		   
		   
		   for(int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		   {
		      DocRow docrow = (DocRow) vecDocRow.get(cnt1);			  
			  Integer itemPkid = new Integer(docrow.getItemId());
			  
			  if(itemPkid != null)
			  {
				  ItemObject itmObj = ItemNut.getObject(itemPkid);
				  
				  if(itmObj!=null)
				  {
						if(itmObj.serialized==true && (docrow.vecSerial==null || docrow.vecSerial.size()<=0))
						{
							alertSerialized = true;
							return alertSerialized;
						}
				  }
			  }	  	  
		   }
		   
		   return alertSerialized;		   
	}
	
	// 20080326 Jimmy - check whether there are any qty of serialized items not same qty of BOM header.
	public boolean qtySerializedItemNotSame()
	{
		
		TreeMap tableRows = this.getTableRows();
		Vector vecDocRow = new Vector(tableRows.values());		   
		for(int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
		      DocRow docrow = (DocRow) vecDocRow.get(cnt1);
		      Integer itemPkid = new Integer(docrow.getItemId());
			  
			  if(itemPkid != null)
			  {
				  ItemObject itmObj = ItemNut.getObject(itemPkid);
				  
				  if(itmObj!=null)
				  {
						if(itmObj.serialized==true)
						{
							BigDecimal sizeSerial = new BigDecimal(docrow.vecSerial.size());
						    if (docrow.getQty().compareTo(sizeSerial) != 0)
						    {
						    	  return true;
						    }
						}
				  }
			  }	  	  
		}
		return false;
	}
	
	// 20080423 Jimmy
	public OfficialReceiptObject getReceipt()
	{ return this.receiptObjDeposit;}

	public void setReceipt(Long pkid)
	{
		this.receiptObjDeposit = OfficialReceiptNut.getObject(pkid);
	}
	
	public BigDecimal getReceiptPayment()
	{ return this.receiptPayment; }
	
	public void setReceiptPayemnt(BigDecimal receiptPayment)
	{
		this.receiptPayment = receiptPayment;
	}
	
	public void resetReceipt()
	{
		this.receiptObjDeposit = null;
		this.receiptPayment = new BigDecimal(0);
	}
	
	public synchronized void knockOffDocuments()
	{
		if(this.receiptObjDeposit==null){ return;}

		//Update open balance of receipt
		Log.printVerbose("Update open balance of receipt");
		this.receiptObjDeposit.openBalance = this.receiptObjDeposit.openBalance.subtract(this.receiptPayment);
		OfficialReceipt orEJB = OfficialReceiptNut.getHandle(this.receiptObjDeposit.pkid);
		try
		{
			orEJB.setObject(this.receiptObjDeposit);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		// find the latest invoice
	    InvoiceObject invoiceObj = (InvoiceObject) this.prevCashsales.get(this.prevCashsales.size()-1);
		
	    Long invoiceId = new Long(invoiceObj.mPkid.longValue());
	    // reset the outstanding balance of invoice
		// create the corresponding doclink beans.
		// create the respective DocLinkBeans
		try
		{
			Invoice thisInvEJB = InvoiceNut.getHandle(invoiceId);

			 // Create AuditTrail Report
			 Log.printVerbose("Create Audit Trail");
	         AuditTrailObject atObj = new AuditTrailObject();
	         atObj.userId = this.userId;
	         atObj.auditType = AuditTrailBean.TYPE_TXN;
	         atObj.remarks = "customer: deposit-settlement " + "RCT" + this.receiptObjDeposit.pkid.toString()+ " INV" + invoiceId.toString()+" AMOUNT:"+CurrencyFormat.strCcy(this.getReceiptPayment());
	         AuditTrailNut.fnCreate(atObj);

	         //Adjust outstanding amount of Invoice
	         Log.printVerbose("Adjust outstanding amount of Invocie: " + invoiceId);
	         thisInvEJB.adjustOutstanding(this.getReceiptPayment().negate());
	         BigDecimal addBackPdc = this.receiptObjDeposit.amountPDCheque.multiply(
	        		 this.getReceiptPayment().divide(this.receiptPayment, 12, BigDecimal.ROUND_HALF_EVEN));
	         thisInvEJB.adjustOutstandingBfPdc(this.getReceiptPayment().negate().add(addBackPdc));
	         
	         // Create the DocLink
	         Log.printVerbose("Create the DocLink");
	         DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", 
						DocLinkBean.RELTYPE_PYMT_INV, OfficialReceiptBean.TABLENAME, 
						this.receiptObjDeposit.pkid, InvoiceBean.TABLENAME, invoiceId, 
						this.receiptObjDeposit.currency, this.getReceiptPayment().negate(), // reduces
						this.receiptObjDeposit.currency, this.getReceiptPayment().negate(), // reduces
						"", this.receiptObjDeposit.paymentTime, this.userId);
	        
			// reduce the outstanding amount for the sales order too
			// if exists 
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandleByInvoice(invoiceId);
			if (soEJB != null)
			{
				Log.printVerbose("Reduce the outstanding amount for the sales order");
				SalesOrderIndexObject soObj = soEJB.getObject();
				soEJB.adjustOutstanding(this.getReceiptPayment().negate());
				DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_PYMT_SO, 
					OfficialReceiptBean.TABLENAME, this.receiptObjDeposit.pkid, SalesOrderIndexBean.TABLENAME, 
					soObj.pkid, this.receiptObjDeposit.currency, this.getReceiptPayment().negate(), // reduces
					this.receiptObjDeposit.currency, this.getReceiptPayment().negate(), // reduces
					"", this.receiptObjDeposit.paymentTime, this.userId);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	// 20080530 Jimmy
	private boolean checkAmount()
	{	
		if (this.amountCash.signum() > 0) return true;
		if (this.amountCard.signum() > 0) return true;
		if (this.amountCheque.signum() > 0) return true;
		if (this.amountPDCheque.signum() > 0) return true;
		if (this.amountCoupon.signum() > 0) return true;
		if (this.amountOther.signum() > 0) return true;
		
		return false;
	}
	
	// End
	
}
