/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.distribution;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.vlee.bean.application.AppConfigManager;
import com.vlee.bean.customer.CustCreditControlChecker;
import com.vlee.bean.user.PermissionManager;
import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.accounting.CreditTermsRulesetNut;
import com.vlee.ejb.accounting.DocLinkBean;
import com.vlee.ejb.accounting.DocLinkNut;
import com.vlee.ejb.accounting.DocLinkObject;
import com.vlee.ejb.accounting.JournalTxnLogic;
import com.vlee.ejb.accounting.OfficialReceiptBean;
import com.vlee.ejb.accounting.OfficialReceiptObject;
import com.vlee.ejb.customer.CashRebateVoucher;
import com.vlee.ejb.customer.CashRebateVoucherBean;
import com.vlee.ejb.customer.CashRebateVoucherNut;
import com.vlee.ejb.customer.CashRebateVoucherObject;
import com.vlee.ejb.customer.CustAccountBean;
import com.vlee.ejb.customer.CustAccountNut;
import com.vlee.ejb.customer.CustAccountObject;
import com.vlee.ejb.customer.CustUserBean;
import com.vlee.ejb.customer.CustUserNut;
import com.vlee.ejb.customer.CustUserObject;
import com.vlee.ejb.customer.Invoice;
import com.vlee.ejb.customer.InvoiceBean;
import com.vlee.ejb.customer.InvoiceItem;
import com.vlee.ejb.customer.InvoiceItemBean;
import com.vlee.ejb.customer.InvoiceItemNut;
import com.vlee.ejb.customer.InvoiceItemObject;
import com.vlee.ejb.customer.InvoiceNut;
import com.vlee.ejb.customer.InvoiceObject;
import com.vlee.ejb.customer.MemberCard;
import com.vlee.ejb.customer.MemberCardBean;
import com.vlee.ejb.customer.MemberCardNut;
import com.vlee.ejb.customer.MemberCardObject;
import com.vlee.ejb.customer.SalesOrderIndex;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.customer.SalesOrderItemObject;
import com.vlee.ejb.inventory.Stock;
import com.vlee.ejb.inventory.StockNut;
import com.vlee.ejb.inventory.StockObject;
import com.vlee.ejb.user.UserNut;
import com.vlee.ejb.user.UserObject;
import com.vlee.ejb.user.UserObjectPermissionsBean;
import com.vlee.ejb.user.UserObjectPermissionsNut;
import com.vlee.ejb.user.UserObjectPermissionsObject;
import com.vlee.ejb.user.UserPermissionsBean;
import com.vlee.ejb.user.UserPermissionsNut;
import com.vlee.ejb.user.UserPermissionsObject;
import com.vlee.ejb.user.UserRoleNut;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.DocRow;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

/*--------------------------------------------------------------
 BASIC LOGIC
 1. CRV is only created when Cashsale is confirmed because
 a) Full payment must be made
 b) No more additional sales order item to be added after
 the invoice is issued, so CRV gain could be determined.
 ----------------------------------------------------------------*/
public class CreateSalesOrderSession extends java.lang.Object implements Serializable
{
	private Vector vecRecentlyCreatedSO = new Vector();
	private Vector vecRecentlyCreatedCRV = new Vector();
	public Vector prevInvoices = new Vector();
	boolean popupPrintSO = false;
	boolean popupPrintCRV = false;
	boolean managerMode = false;
	boolean discountAllow = false;
	Integer discountApprover = new Integer(0);
	String discountLog = "";
	String discountReason = "";
	SalesOrderIndexObject soObj = null;
	BranchObject branch = null;
	Integer userId = null;
	CustAccountObject custAccObj = null;
	CustUserObject custUserObj = null;
	MemberCardObject memCardObj = null;
	ReceiptForm receiptForm = null;
	CashRebateVoucherObject crvObj = null;
	protected TreeMap tableRows = null; // // this is used for SalesOrderItems
	protected TreeMap redeemableCRV = null;
	protected TreeMap redeemingList = null;
	private Vector vecReceipts = null;
	public String salesmanCode = "";
	public boolean bChangedDate = false;
	public Timestamp txnDate = TimeFormat.getTimestamp();
	public static final int ERROR = 0;
	public static final int READY = 1; // / ready to be used
	public static final int REQUIRED = 2; // / must be used
	public static final int OPTIONAL = 3;
	public static final int COMPLETED = 4; // / completed
	public boolean bTabOrderForm = true;
	public boolean bTabDeliveryForm = false;
	public boolean bTabPaymentForm = false;
	public int stateOrderDetails = READY;
	public int stateDeliveryDetails = READY;
	public int statePaymentDetails = READY;
	public boolean resetting = false;
	public BigDecimal dollarNoteReceived = new BigDecimal(0);
	public String etxnStatus = "";
	
	//public static final String STATUS_CANCEL_COLLECTION = "CANCEL_COLLECTION";
	public static final String STATUS_BREACHED_CREDIT_LIMIT = "This customer's credit limit has been breached.";
	public static final String STATUS_CREDIT_TERMS_OUTSTANDING = "This customer has invoice(s) with broken credit terms.";
	public static final String STATUS_INVENTORY_QTY_INSUFFICIENT = "Current stocks cannot fulfill this sales order's requirements.";
	public static final String STATUS_GENERIC_APPROVAL_REQUIRED = "This sales order requires approval.";
	
	// // constructor
	public CreateSalesOrderSession(Integer userId)
	{
		this.userId = userId;
		this.soObj = new SalesOrderIndexObject();
		this.soObj.useridCreate = this.userId;
		this.soObj.useridEdit = this.userId;
		this.tableRows = new TreeMap();
		this.redeemableCRV = new TreeMap();
		this.redeemingList = new TreeMap();
		this.vecReceipts = new Vector();
		this.receiptForm = new ReceiptForm(userId);
		this.receiptForm.setCustomer(CustAccountBean.PKID_CASH);
		this.custAccObj = CustAccountNut.getObject(CustAccountBean.PKID_CASH);
		setCustomer(this.custAccObj);
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		UserObject userObj = UserNut.getObject(this.userId);
		this.salesmanCode = userObj.userName;
		this.dollarNoteReceived = new BigDecimal(0);
		this.etxnStatus = "";
	}

	public Vector getPreviousInvoices()
	{
		return this.prevInvoices;
	}
	
	public void setSORemarks(String val)
	{
		this.soObj.remarks1 = val;
		this.soObj.timeUpdate = TimeFormat.getTimestamp();
	}

	public void setSOTxnDate(Timestamp val)
	{
		this.soObj.timeCreate = val;
		this.soObj.timeUpdate = TimeFormat.getTimestamp();
	}
	
	public void setEtxnStatus(String buf)
	{
		this.etxnStatus = buf;
	}
	
	public void setSalesmanCode(String buf)
	{
		Integer userId = UserNut.getUserId(buf);
		UserObject userObj = UserNut.getObject(userId);
		if (userObj != null)
		{
			this.salesmanCode = userObj.userName;
		}
	}

	public String getSalesmanCode()
	{
		return this.salesmanCode;
	}

	public synchronized void reset()
	{
		resetting = true;
		this.soObj = new SalesOrderIndexObject();
		// / reset certain values to be remembered across transactions
		this.tableRows.clear();
		this.redeemableCRV.clear();
		this.redeemingList.clear();
		this.custAccObj = CustAccountNut.getObject(CustAccountBean.PKID_CASH);
		setCustomer(this.custAccObj);
		this.custUserObj = null;
		this.memCardObj = null;
		this.receiptForm = new ReceiptForm(this.userId);
		this.receiptForm.reset();
		this.receiptForm.setCustomer(CustAccountBean.PKID_CASH);
		if (this.branch != null)
		{
			this.receiptForm.setBranch(this.branch.pkid);
		}
		this.crvObj = null;
		this.vecReceipts.clear();
		resetting = false;
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		this.managerMode = false;
		this.discountAllow = false;
		this.discountApprover = new Integer(0);
		this.discountLog = "";
		this.discountReason = "";
		this.dollarNoteReceived = new BigDecimal(0);
		this.etxnStatus = "";
	}

	public void setDollarNoteReceived(BigDecimal buf)
	{
		this.dollarNoteReceived = buf;
		BigDecimal newCashAmt = new BigDecimal(0);
		this.receiptForm.setAmountCash(newCashAmt);
		if (getMoneyChange().signum() >= 0)
		{
			newCashAmt = getBillAmount().subtract(this.receiptForm.getReceiptAmt());
			this.receiptForm.setAmountCash(newCashAmt);
		}
	}

	public BigDecimal getDollarNoteReceived()
	{
		return this.dollarNoteReceived;
	}

	public BigDecimal getMoneyChange()
	{
		BigDecimal theChange = getAmountGross();
		theChange = theChange.subtract(getBillAmount());
		return theChange;
	}

	public BigDecimal getAmountGross()
	{
		BigDecimal theAmt = this.dollarNoteReceived.add(this.receiptForm.getReceiptAmt());
		theAmt = theAmt.subtract(this.receiptForm.getAmountCash());
		return theAmt;
	}

	public void calculateActualCashReceived()
	{
	}

	public boolean getDiscountAllow()
	{
		return this.discountAllow;
	}

	public Integer getDiscountApprover()
	{
		return this.discountApprover;
	}

	public String getDiscountLog()
	{
		return this.discountLog;
	}

	public String getDiscountReason()
	{
		return this.discountReason;
	}

	public void setDiscountInfo(boolean bufAllow, Integer bufApprover, String log, String reason)
	{
		this.discountAllow = bufAllow;
		this.discountApprover = bufApprover;
		this.discountLog = log;
		this.discountReason = reason;
	}

	public void setDocRows(TreeMap newDocRow)
	{
		System.out.println("Inside setDocRows");
		
		this.tableRows = newDocRow;
	}

	public void setManagerPassword(String buf) throws Exception
	{
		if (this.branch != null)
		{
			if (this.branch.managerPassword01.equals(buf))
			{
				this.managerMode = true;
			} else
			{
				this.managerMode = false;
				throw new Exception(" Invalid Manager Password !");
			}
		}
	}

	public void setPopupPrintSO(boolean buf)
	{
		this.popupPrintSO = buf;
	}

	public boolean getPopupPrintSO()
	{
		return this.popupPrintSO;
	}

	public void setPopupPrintCRV(boolean buf)
	{
		this.popupPrintCRV = buf;
	}

	public boolean getPopupPrintCRV()
	{
		return this.popupPrintCRV;
	}

	public ReceiptForm getReceiptForm()
	{
		return this.receiptForm;
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
	}

	public boolean getValidBranch()
	{
		if (this.branch == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}

	public String getBranch(String buf)
	{
		if (this.branch == null)
		{
			return buf;
		} else
		{
			return this.branch.pkid.toString();
		}
	}

	public void setBranch(Integer ibranch)
	{
		if (ibranch != null)
		{
			this.branch = BranchNut.getObject(ibranch);
			this.receiptForm.setBranch(ibranch);
			if (this.branch != null && this.soObj != null)
			{
				this.soObj.branch = this.branch.pkid;
				this.soObj.location = this.branch.invLocationId;	
				this.soObj.receiptBranch = this.branch.pkid;
				this.soObj.productionBranch = this.branch.pkid;
				this.soObj.productionLocation = this.branch.invLocationId;	
				this.soObj.pccenter = this.branch.accPCCenterId;
				this.soObj.currency = this.branch.currency;
			}
		}
	}
	
	public void setProductionLocation(Integer invLocationId)
	{
		if (invLocationId != null)
		{
			this.soObj.productionLocation = invLocationId;	
		}
	}

	public String getDescription()
	{
		return this.soObj.description;
	}

	public void setDescription(String description)
	{
	 	Log.printVerbose(description);	
		if (description != null)
			this.soObj.description = description;
	}

	public String getGuid()
	{
		return this.soObj.guid;
	}

	public void setGuid(String guid)
	{
		if (guid != null)
			this.soObj.guid = guid;
	}

	public void setInfo(Integer iBranch, String remarks, boolean bRInvoice, boolean bRReceipt, String flagInternal,
			String flagSender, String flagReceiver, String managerPassword, String soType1, String occasion,
			String thirdpartyLoyaltyCardCode, String thirdpartyLoyaltyCardNumber, BigDecimal interfloraPrice, String interfloraFlowers1)
	{
		Log.printVerbose(" SAVING DATA ... ");
		this.branch = BranchNut.getObject(iBranch);
		this.soObj.remarks1 = remarks;
		this.soObj.requireInvoice = bRInvoice;
		this.soObj.requireReceipt = bRReceipt;
		// this.soObj.flagInternalBool = bFlagInternal;
		this.soObj.flagInternal = flagInternal;
		this.soObj.flagSender = flagSender;
		this.soObj.flagReceiver = flagReceiver;
		Log.printVerbose(" branch: " + this.branch.toString());
		if (this.soObj.requireInvoice)
		{
			Log.printVerbose(" requireInvoice: true");
		} else
		{
			Log.printVerbose(" requireInvoice: false");
		}
		this.soObj.soType1 = soType1;
		this.soObj.occasion = occasion;
		this.soObj.thirdpartyLoyaltyCardCode = thirdpartyLoyaltyCardCode;
		this.soObj.thirdpartyLoyaltyCardNumber = thirdpartyLoyaltyCardNumber;
		this.soObj.interfloraPrice = interfloraPrice;
		this.soObj.interfloraFlowers1 = interfloraFlowers1;
	}

	public void setCustomer(CustAccountObject custObj)
	{
		this.custUserObj = null;
		this.memCardObj = null;
		this.custAccObj = custObj;
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderName = custObj.name;
		this.soObj.senderIdentityNumber = custObj.identityNumber;
		this.soObj.senderHandphone = custObj.mobilePhone;
		this.soObj.senderFax = custObj.faxNo;
		this.soObj.senderPhone1 = custObj.telephone1;
		this.soObj.senderPhone2 = custObj.telephone2;
		// this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderCompanyName = custObj.name;
		this.soObj.senderAdd1 = custObj.mainAddress1;
		this.soObj.senderAdd2 = custObj.mainAddress2;
		this.soObj.senderAdd3 = custObj.mainAddress3;
		// this.soObj.senderCity = senderCity;
		this.soObj.senderZip = custObj.mainPostcode;
		this.soObj.senderState = custObj.mainState;
		this.soObj.senderCountry = custObj.mainCountry;
		if (this.custAccObj.state.equals(CustAccountBean.STATE_BL))
		{
			this.soObj.flagInternalBool = true;
		} else
		{
			this.soObj.flagInternalBool = false;
		}
		this.receiptForm.setCustomer(custAccObj.pkid);
		retrieveRedeemableCRV();
		this.managerMode = false;
	}

	public void setCustUser(CustUserObject custUser)
	{
		this.memCardObj = null;
		this.custUserObj = custUser;
		this.custAccObj = CustAccountNut.getObject(this.custUserObj.accId);
		this.soObj.thirdpartyLoyaltyCardCode = this.custUserObj.loyaltyCardName1;
		this.soObj.thirdpartyLoyaltyCardNumber = this.custUserObj.loyaltyCardNumber1;
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderTable2 = CustUserBean.TABLENAME;
		this.soObj.senderKey2 = this.custUserObj.pkid;
		this.soObj.senderName = custUser.nameFirst + " " + custUser.nameLast;
		this.soObj.senderIdentityNumber = custUser.referenceNo;
		this.soObj.senderHandphone = custUser.mobilePhone;
		this.soObj.senderFax = custUser.faxNo;
		this.soObj.senderPhone1 = custUser.telephone1;
		this.soObj.senderPhone2 = custUser.telephone2;
		// this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderCompanyName = this.custAccObj.name;
		this.soObj.senderAdd1 = custUser.mainAddress1;
		this.soObj.senderAdd2 = custUser.mainAddress2;
		this.soObj.senderAdd3 = custUser.mainAddress3;
		// this.soObj.senderCity = senderCity;
		this.soObj.senderZip = custUser.mainPostcode;
		this.soObj.senderState = custUser.mainState;
		this.soObj.senderCountry = custUser.mainCountry;
		if (this.custAccObj.state.equals(CustAccountBean.STATE_BL))
		{
			this.soObj.flagInternalBool = true;
		} else
		{
			this.soObj.flagInternalBool = false;
		}
		this.receiptForm.setCustomer(custAccObj.pkid);
		this.receiptForm.setCCDetails(this.custUserObj.defaultCardNumber, this.custUserObj.defaultCardType,
				this.custUserObj.defaultCardName, this.custUserObj.defaultCardBank, "",
				this.custUserObj.defaultCardGoodThru, this.custUserObj.defaultCardSecurityNum);
		// this.receiptForm.setCardType(custUser.defaultCardType);
		retrieveRedeemableCRV();
		this.managerMode = false;
	}

	public void updateContact()
	{
		if (this.custUserObj == null)
		{
			return;
		}
		this.custUserObj = CustUserNut.getObject(this.custUserObj.pkid);
		this.soObj.thirdpartyLoyaltyCardCode = this.custUserObj.loyaltyCardName1;
		this.soObj.thirdpartyLoyaltyCardNumber = this.custUserObj.loyaltyCardNumber1;
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderTable2 = CustUserBean.TABLENAME;
		this.soObj.senderKey2 = this.custUserObj.pkid;
		this.soObj.senderName = this.custUserObj.nameFirst + " " + this.custUserObj.nameLast;
		this.soObj.senderIdentityNumber = this.custUserObj.referenceNo;
		this.soObj.senderHandphone = this.custUserObj.mobilePhone;
		this.soObj.senderFax = this.custUserObj.faxNo;
		this.soObj.senderPhone1 = this.custUserObj.telephone1;
		this.soObj.senderPhone2 = this.custUserObj.telephone2;
		// this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderAdd1 = this.custUserObj.mainAddress1;
		this.soObj.senderAdd2 = this.custUserObj.mainAddress2;
		this.soObj.senderAdd3 = this.custUserObj.mainAddress3;
		// this.soObj.senderCity = senderCity;
		this.soObj.senderZip = this.custUserObj.mainPostcode;
		this.soObj.senderState = this.custUserObj.mainState;
		this.soObj.senderCountry = this.custUserObj.mainCountry;
		// this.soObj.receiverCity = receiverCity;
		this.soObj.receiverZip = this.soObj.senderZip;
		this.soObj.receiverState = this.soObj.senderState;
		this.soObj.receiverCountry = this.soObj.senderCountry;
		if (this.custAccObj.state.equals(CustAccountBean.STATE_BL))
		{
			this.soObj.flagInternalBool = true;
		} else
		{
			this.soObj.flagInternalBool = false;
		}
		this.receiptForm.setCCDetails(this.custUserObj.defaultCardNumber, this.custUserObj.defaultCardType,
				this.custUserObj.defaultCardName, this.custUserObj.defaultCardBank, "",
				this.custUserObj.defaultCardGoodThru, this.custUserObj.defaultCardSecurityNum);
		this.managerMode = false;
	}

	public void setMemberCard(MemberCardObject memcardObj)
	{
		this.memCardObj = memcardObj;
		this.custUserObj = CustUserNut.getObject(this.memCardObj.entityKey2);
		this.custAccObj = CustAccountNut.getObject(this.memCardObj.entityKey1);
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderTable2 = CustUserBean.TABLENAME;
		this.soObj.senderKey2 = this.custUserObj.pkid;
		this.soObj.senderName = this.custUserObj.nameFirst + " " + this.custUserObj.nameLast;
		this.soObj.senderIdentityNumber = this.custUserObj.referenceNo;
		this.soObj.senderHandphone = this.custUserObj.mobilePhone;
		this.soObj.senderFax = this.custUserObj.faxNo;
		this.soObj.senderPhone1 = this.custUserObj.telephone1;
		this.soObj.senderPhone2 = this.custUserObj.telephone2;
		// this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderCompanyName = this.custAccObj.name;
		this.soObj.senderAdd1 = this.custUserObj.mainAddress1;
		this.soObj.senderAdd2 = this.custUserObj.mainAddress2;
		this.soObj.senderAdd3 = this.custUserObj.mainAddress3;
		// this.soObj.senderCity = senderCity;
		this.soObj.senderZip = this.custUserObj.mainPostcode;
		this.soObj.senderState = this.custUserObj.mainState;
		this.soObj.senderCountry = this.custUserObj.mainCountry;
		this.soObj.receiverName = this.soObj.senderName;
		this.soObj.receiverIdentityNumber = this.soObj.senderIdentityNumber;
		this.soObj.receiverHandphone = this.soObj.senderHandphone;
		this.soObj.receiverFax = this.soObj.senderFax;
		this.soObj.receiverPhone1 = this.soObj.senderPhone1;
		this.soObj.receiverPhone2 = this.soObj.senderPhone2;
		// this.soObj.receiverInternetNo = receiverInternetNo;
		this.soObj.receiverAdd1 = this.soObj.senderAdd1;
		this.soObj.receiverAdd2 = this.soObj.senderAdd2;
		this.soObj.receiverAdd3 = this.soObj.senderAdd3;
		// this.soObj.receiverCity = receiverCity;
		this.soObj.receiverZip = this.soObj.senderZip;
		this.soObj.receiverState = this.soObj.senderState;
		this.soObj.receiverCountry = this.soObj.senderCountry;
		this.soObj.senderLoyaltyCardName = this.memCardObj.nameDisplay;
		this.soObj.senderLoyaltyCardNo = this.memCardObj.cardNo;
		// this.soObj.senderLoyaltyCardPtsGain = this.
		// this.soObj.senderLoyaltyCardPtsConsume =
		this.soObj.senderForeignTable = MemberCardBean.TABLENAME;
		this.soObj.senderForeignKey = new Integer(this.memCardObj.pkid.intValue());
		this.soObj.senderForeignText = this.memCardObj.cardType;
		if (this.custAccObj.state.equals(CustAccountBean.STATE_BL))
		{
			this.soObj.flagInternalBool = true;
		} else
		{
			this.soObj.flagInternalBool = false;
		}
		this.receiptForm.setCustomer(custAccObj.pkid);
		this.receiptForm.setCCDetails(this.custUserObj.defaultCardNumber, this.custUserObj.defaultCardType,
				this.custUserObj.defaultCardName, this.custUserObj.defaultCardBank, "",
				this.custUserObj.defaultCardGoodThru, this.custUserObj.defaultCardSecurityNum);
		// this.receiptForm.setCardType(this.custUserObj.defaultCardType);
		retrieveRedeemableCRV();
	}

	// // INTEGRATED SETTERS
	public void setDeliveryDetails(String deliveryTo, String deliveryToName, String deliveryFrom,
			String deliveryFromName, String deliveryMsg1, String expDeliveryTime, String expDeliveryTimeStart,
			String deliveryPreferences, String senderName, String senderIdentityNumber, String senderEmail, String senderHandphone,
			String senderFax, String senderPhone1, String senderPhone2, String senderInternetNo,
			String senderCompanyName, String senderAdd1, String senderAdd2, String senderAdd3, String senderCity,
			String senderZip, String senderState, String senderCountry, String receiverTitle, String receiverName,
			String receiverIdentityNumber, String receiverEmail, String receiverHandphone, String receiverFax, String receiverPhone1,
			String receiverPhone2, String receiverCompanyName, String receiverAdd1, String receiverAdd2,
			String receiverAdd3, String receiverCity, String receiverZip, String receiverState, String receiverCountry, String receiverLocationType)
	{
		this.soObj.deliveryTo = deliveryTo;
		this.soObj.deliveryToName = deliveryToName;
		this.soObj.deliveryFrom = deliveryFrom;
		this.soObj.deliveryFromName = deliveryFromName;
		this.soObj.deliveryMsg1 = deliveryMsg1;
		this.soObj.expDeliveryTime = expDeliveryTime;
		this.soObj.expDeliveryTimeStart = TimeFormat.createTimestamp(expDeliveryTimeStart);
		this.soObj.deliveryPreferences = deliveryPreferences;
		this.soObj.senderName = senderName;
		this.soObj.senderIdentityNumber = senderIdentityNumber;
		this.soObj.senderEmail = senderEmail;
		this.soObj.senderHandphone = senderHandphone;
		this.soObj.senderFax = senderFax;
		this.soObj.senderPhone1 = senderPhone1;
		this.soObj.senderPhone2 = senderPhone2;
		this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderCompanyName = senderCompanyName;
		this.soObj.senderAdd1 = senderAdd1;
		this.soObj.senderAdd2 = senderAdd2;
		this.soObj.senderAdd3 = senderAdd3;
		this.soObj.senderCity = senderCity;
		this.soObj.senderZip = senderZip;
		this.soObj.senderState = senderState;
		this.soObj.senderCountry = senderCountry;
		this.soObj.receiverTitle = receiverTitle;
		this.soObj.receiverName = receiverName;
		this.soObj.receiverEmail = receiverEmail;
		this.soObj.receiverHandphone = receiverHandphone;
		this.soObj.receiverFax = receiverFax;
		this.soObj.receiverPhone1 = receiverPhone1;
		this.soObj.receiverPhone2 = receiverPhone2;
		this.soObj.receiverCompanyName = receiverCompanyName;
		this.soObj.receiverAdd1 = receiverAdd1;
		this.soObj.receiverAdd2 = receiverAdd2;
		this.soObj.receiverAdd3 = receiverAdd3;
		this.soObj.receiverCity = receiverCity;
		this.soObj.receiverZip = receiverZip;
		this.soObj.receiverState = receiverState;
		this.soObj.receiverCountry = receiverCountry;
		this.soObj.receiverLocationType = receiverLocationType;
				
		System.out.println("this.soObj.senderEmail :"+this.soObj.senderEmail);
		
		Timestamp tsToday = TimeFormat.getCurrentDate();
		if(this.soObj.expDeliveryTimeStart.compareTo(tsToday)==0)
		{
			this.soObj.remarks1 = "Being processed";
		}
	}

	public boolean canCreate()
	{
		System.out.println("Inside CreateSalesOrderSession.canCreate");								
		
		boolean rtn = true;
		if (this.soObj.pkid.longValue() > 0)
		{
			System.out.println("this.soObj.pkid : "+this.soObj.pkid.toString());
			return false;
		}
		if (branch == null)
		{
			System.out.println("branch is null ");
			return false;
		}
		if (userId == null)
		{
			System.out.println("userId is null ");
			return false;
		}
		if (this.custAccObj == null)
		{
			System.out.println("this.custAccObj is null ");
			return false;
		}
		if (!getValidBranch())
		{
			System.out.println(" not null ");
			return false;
		}
		Vector vecDocRow = new Vector(this.tableRows.values());
		if (vecDocRow.size() == 0)
		{
			System.out.println("vecDocRow size is 0");
			return false;
		}
		return rtn;
	}

	public boolean canCreateCashsale()
	{
		if (!canCreate())
		{
			return false;
		}
		if (getBillAmount().compareTo(this.receiptForm.getReceiptAmt()) != 0)
		{
			return false;
		}
		return true;
	}

	public boolean canCreateInvoice()
	{
		if (!canCreate())
		{
			return false;
		}
		if (getBillAmount().compareTo(this.receiptForm.getReceiptAmt()) <= 0)
		{
			return false;
		}
		return true;
	}

	public void fnAddStockWithItemCode(DocRow docr) throws Exception
	{
		System.out.println("Inside fnAddStockWithItemCode");
		
		// / check if this row exists in the list already
		Vector vecDocRow = new Vector(this.tableRows.values());		
		
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			System.out.println("Inside for loop vecDocRow");
			vecDocRow.get(cnt);
		}
		// / add it
		try
		{
			docr.setIncludeCodeInKey(false);
			this.tableRows.put(docr.getKey(), docr);
			
			System.out.println("Added row into tableRows");
			
		} catch (Exception ex)
		{ 
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	// /// PURE GETTERS
	public SalesOrderIndexObject getSalesOrderIndex()
	{
		return this.soObj;
	}

	public CustAccountObject getCustAccount()
	{
		return this.custAccObj;
	}

	public CustUserObject getCustUser()
	{
		return this.custUserObj;
	}

	public MemberCardObject getMemberCard()
	{
		return this.memCardObj;
	}

	public Vector getRecentlyCreatedSO()
	{
		return this.vecRecentlyCreatedSO;
	}

	public Vector getRecentlyCreatedCRV()
	{
		return this.vecRecentlyCreatedCRV;
	}

	public boolean getValidMemberCard()
	{
		if (this.memCardObj == null)
		{
			return false;
		}
		return true;
	}

	public BigDecimal getBillAmount()
	{
		BigDecimal totalBill = new BigDecimal(0);
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalBill = totalBill.add(docrow.getNetAmount());
		}
		return totalBill;
	}

	public void toggleProduction(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow != null)
		{
			docrow.productionRequired = !docrow.productionRequired;
		}
	}

	public void toggleDelivery(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow != null)
		{
			docrow.deliveryRequired = !docrow.deliveryRequired;
		}
	}

	public BigDecimal getDocRowCRVGainAmount()
	{
		BigDecimal totalCRVGain = new BigDecimal(0);
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalCRVGain = totalCRVGain.add(docrow.getCrvGain().multiply(docrow.getQty()));
		}
		return totalCRVGain;
	}

	public CashRebateVoucherObject getCashRebateVoucher() throws Exception
	{
		// / check if the CRV Gain is bigger than zero
		BigDecimal newCrvValue = getDocRowCRVGainAmount();
		if (newCrvValue.signum() <= 0)
		{
			this.crvObj = null;
			throw new Exception("ZERO CRV VALUE, CRV will not be created");
		}
		// / check if there's valid memberCard
		if (this.memCardObj == null)
		{
			this.crvObj = null;
			throw new Exception("NO membercard selected. CRV will not be created");
		}
		// / check if the payment is done fully (receipt)
		if (this.receiptForm.getReceiptAmt().compareTo(getBillAmount()) != 0)
		{
			this.crvObj = null;
			throw new Exception("CRV is only issued for fully settled sales order!");
		}
		// // create a new CRV Object if it has not been created
		if (this.crvObj == null)
		{
			this.crvObj = new CashRebateVoucherObject();
		}
		// /// populate/update the CRV Object
		this.crvObj.branch = this.branch.pkid;
		this.crvObj.pcCenter = this.branch.accPCCenterId;
		this.crvObj.cardId = this.memCardObj.pkid;
		this.crvObj.cardNo = this.memCardObj.cardNo;
		this.crvObj.entityTable1 = CustAccountBean.TABLENAME;
		this.crvObj.entityKey1 = this.custAccObj.pkid;
		this.crvObj.nameDisplay = this.memCardObj.nameDisplay;
		this.crvObj.identityNumber = this.memCardObj.identityNumber;
		Timestamp tsToday = TimeFormat.getTimestamp();
		this.crvObj.dateValidFrom = TimeFormat.add(tsToday, 0, 0, this.branch.crvDayFrom.intValue());
		this.crvObj.dateGoodThru = TimeFormat.add(this.crvObj.dateValidFrom, 0, 0, this.branch.crvDayTo.intValue());
		this.crvObj.cardType = this.memCardObj.cardType;
		this.crvObj.remarks = "";
		this.crvObj.info1 = "";
		this.crvObj.info2 = "";
		this.crvObj.voucherValue = getDocRowCRVGainAmount();
		this.crvObj.voucherBalance = this.crvObj.voucherValue;
		this.crvObj.pointBonus = new BigDecimal(0);
		this.crvObj.srcTable1 = SalesOrderIndexBean.TABLENAME;
		this.crvObj.srcKey1 = this.soObj.pkid;
		if (this.receiptForm.getReceiptAmt().signum() != 0)
		{
			this.crvObj.srcTable2 = OfficialReceiptBean.TABLENAME;
			OfficialReceiptObject rctObj = this.receiptForm.getReceipt();
			this.crvObj.srcKey2 = rctObj.pkid;
		}
		// this.crvObj.tgtTable1 = "";
		// this.crvObj.tgtKey1 = new Long(0);
		// this.crvObj.tgtTable2 = "";
		// this.crvObj.tgtKey2 = new Long(0);
		Log.printVerbose(" CCCCCCCCCCCCCCCCCCCC 14====================");
		this.crvObj.dateCreate = TimeFormat.getTimestamp();
		this.crvObj.dateEdit = TimeFormat.getTimestamp();
		this.crvObj.dateUse = TimeFormat.getTimestamp();
		this.crvObj.userCreate = this.userId;
		this.crvObj.userEdit = this.userId;
		this.crvObj.userUse = this.userId;
		this.crvObj.state = CashRebateVoucherBean.STATE_CREATED;
		this.crvObj.status = CashRebateVoucherBean.STATUS_ACTIVE;
		Log.printVerbose(" CCCCCCCCCCCCCCCCCCCC 15====================");
		return this.crvObj;
	}

	public BigDecimal getCRVConsumeAmount()
	{
		BigDecimal totalCRVConsume = new BigDecimal(0);
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			totalCRVConsume = totalCRVConsume.add(docrow.getCrvConsume());
		}
		return totalCRVConsume;
	}

	public DocRow dropDocRow(String key)
	{
		return (DocRow) this.tableRows.remove(key);
	}

	public synchronized String createSalesOrderAndReceipt() throws Exception
	{
		String result = createSoRctVchr();
		if (!resetting)
		{
			reset();
		}
		return result;
	}

	public CashRebateVoucherObject getCRVGivenSO(Long soPkid)
	{
		CashRebateVoucherObject crvObj = null;
		for (int cnt1 = 0; cnt1 < vecRecentlyCreatedCRV.size(); cnt1++)
		{
			CashRebateVoucherObject cashRebateObj = (CashRebateVoucherObject) vecRecentlyCreatedCRV.get(cnt1);
			if (cashRebateObj.srcKey1.equals(soPkid) && cashRebateObj.srcTable1.equals(SalesOrderIndexBean.TABLENAME))
			{
				return cashRebateObj;
			}
		}
		return crvObj;
	}

	public synchronized String createInvoice() throws Exception
	{
		String result = "";
		if (!canCreateInvoice() && !canCreateCashsale())
		{
			return result;
		}
		if(!checkInventoryQty(this.branch.invLocationId).equals("#GOOD#"))
		{
			throw new Exception("Item code " + checkInventoryQty(this.branch.invLocationId) + " does not have sufficient quantity.");
		}
		result = createSoRctVchr();
		createInvDocLink();
		// / 4) create new CRV if valid
		// / populate the CRV with SalesOrder PKID and Receipt PKID
		if (getBillAmount().compareTo(this.receiptForm.getReceiptAmt()) == 0)
		{
			try
			{
				CashRebateVoucherObject crvObj = getCashRebateVoucher();
				if (crvObj != null && this.memCardObj != null)
				{
					BigDecimal bdTotalCRV = new BigDecimal(0);
					bdTotalCRV = this.memCardObj.pointBalance.add(crvObj.voucherValue);
					// / before creating the CRV, add the value to the
					// MemberCardObject.pointBalance
					// / if the value of the point Balance + this CRV exceeds
					// minimum amount RM1, then
					// / proceed to create the CRV
					if (bdTotalCRV.compareTo(new BigDecimal("1.00")) < 0)
					{
						try
						{
							MemberCard memCardEJB = MemberCardNut.getHandle(memCardObj.pkid);
							this.memCardObj.pointBalance = this.memCardObj.pointBalance.add(crvObj.voucherValue);
							memCardEJB.setObject(this.memCardObj);
						} catch (Exception ex)
						{
						}
					} else
					{
						// // if not, just don't create the CRV, add existing
						// CRV as accumulated values
						// /// inside CRV Balance
						CashRebateVoucher crvEJB = CashRebateVoucherNut.fnCreate(crvObj);
						BigDecimal pointBalance = this.memCardObj.pointBalance;
						if (pointBalance.signum() > 0)
						{
							try
							{
								MemberCard memCardEJB = MemberCardNut.getHandle(memCardObj.pkid);
								this.memCardObj.pointBalance = new BigDecimal(0);
								memCardEJB.setObject(this.memCardObj);
								crvObj.voucherValue = crvObj.voucherValue.add(pointBalance);
								crvObj.voucherBalance = crvObj.voucherBalance.add(pointBalance);
								crvObj.info2 += " Added " + CurrencyFormat.strCcy(pointBalance)
										+ " from previously accumulated points. ";
								crvEJB.setObject(crvObj);
							} catch (Exception ex)
							{
							}
						}
						// / check if there's membershipFee Due, if yes, deduct
						// from the CRV to be created.
						if (this.memCardObj.membershipFeeDue.signum() > 0)
						{
							BigDecimal reduceAmt = new BigDecimal(0);
							if (crvObj.voucherValue.compareTo(this.memCardObj.membershipFeeDue) > 0)
							{
								reduceAmt = this.memCardObj.membershipFeeDue;
							} else
							{
								reduceAmt = crvObj.voucherValue;
							}
							if (reduceAmt.signum() > 0)
							{
								try
								{
									MemberCard memCardEJB = MemberCardNut.getHandle(memCardObj.pkid);
									this.memCardObj.membershipFeeDue = this.memCardObj.membershipFeeDue
											.subtract(reduceAmt);
									this.memCardObj.membershipFeeLog = " Deducted from CRV No:"
											+ crvObj.pkid.toString() + " " + this.memCardObj.membershipFeeLog;
									memCardEJB.setObject(this.memCardObj);
									crvObj.voucherValue = crvObj.voucherValue.subtract(reduceAmt);
									crvObj.voucherBalance = crvObj.voucherBalance.subtract(reduceAmt);
									crvObj.info2 += " Deducted " + CurrencyFormat.strCcy(reduceAmt)
											+ " for Membership Fee ";
									crvEJB.setObject(crvObj);
								} catch (Exception ex)
								{
								}
							}
						}
						this.vecRecentlyCreatedCRV.add(crvObj);
						this.popupPrintCRV = true;
					}// / end of crvValue < 1
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (!resetting)
		{
			reset();
		}
		return result;
	}

	private synchronized void createInvDocLink() throws Exception
	{
		// / first, create the invoice...
		// // set the outstanding balance = sales order amount - receipt amount
		// Populate Defaults
		// this.invoiceObj.mSalesTxnId = // automatically created when default
		// is zero
		// this.invoiceObj.mPaymentTermsId = pmtTerm;
		// this.invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		if(!checkInventoryQty(this.branch.invLocationId).equals("#GOOD#"))
		{
			throw new Exception("Item code " + checkInventoryQty(this.branch.invLocationId) + " does not have sufficient quantity.");
		}
		InvoiceObject invoiceObj = new InvoiceObject();
		invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		invoiceObj.mCurrency = this.branch.currency;
		invoiceObj.mTotalAmt = getBillAmount();
		invoiceObj.mOutstandingAmt = getBillAmount().subtract(this.receiptForm.getReceiptAmt());
		invoiceObj.mOutstandingAmt = getBillAmount().subtract(this.receiptForm.getReceiptAmt().subtract(this.receiptForm.getAmountPDCheque()));
		invoiceObj.mRemarks = "";
		// this.invoiceObj.mState = InvoiceBean.ST_CREATED; // 10
		// this.invoiceObj.mStatus = InvoiceBean.STATUS_ACTIVE;
		invoiceObj.mLastUpdate = TimeFormat.getTimestamp();
		invoiceObj.mUserIdUpdate = this.userId;
		invoiceObj.mEntityTable = CustAccountBean.TABLENAME;
		invoiceObj.mEntityKey = this.custAccObj.pkid;
		invoiceObj.mEntityName = this.custAccObj.name;
		// this.invoiceObj.mEntityType = "";
		invoiceObj.mIdentityNumber = this.custAccObj.identityNumber;
		invoiceObj.mEntityContactPerson = this.custAccObj.getName();
		// invoiceObj.mForeignTable = ""; // 20
		// invoiceObj.mForeignKey = new Integer(0);
		// this.invoiceObj.mForeignText = "";
		invoiceObj.mCustSvcCtrId = this.branch.pkid;
		invoiceObj.mLocationId = this.branch.invLocationId;
		invoiceObj.mPCCenter = this.branch.accPCCenterId;
		// this.invoiceObj.mTxnType = "";
		// this.invoiceObj.mStmtType = "";
		// invoiceObj.mReferenceNo = this.referenceNo;
		invoiceObj.mDescription = " Auto Created From SalesOrder: " + this.soObj.pkid.toString();
		// invoiceObj.mWorkOrder = new Long(0); // 30
		// invoiceObj.mDeliveryOrder = new Long(0);
		invoiceObj.mReceiptId = this.receiptForm.getReceipt().pkid;
		if (canCreateCashsale())
		{
			//invoiceObj.mDisplayFormat = InvoiceBean.CASHBILL_TRADING_1;
			if(!"".equals(this.branch.formatInvoiceType))
			{	
				invoiceObj.mDisplayFormat = this.branch.formatInvoiceType;
			}
			else
			{
				invoiceObj.mDisplayFormat = InvoiceBean.CASHBILL_TRADING_1;
			}
			invoiceObj.mDocType = InvoiceBean.CASHBILL;
		} else if (canCreateInvoice())
		{
			//invoiceObj.mDisplayFormat = InvoiceBean.INVOICE_TRADING_1;
			if(!"".equals(this.branch.formatInvoiceType))
			{	
				invoiceObj.mDisplayFormat = this.branch.formatInvoiceType;
			}
			else
			{
				invoiceObj.mDisplayFormat = InvoiceBean.INVOICE_TRADING_1;
			}
			invoiceObj.mDocType = InvoiceBean.INVOICE;
		}
		Invoice invoiceEJB = InvoiceNut.fnCreate(invoiceObj);
		this.prevInvoices.add(invoiceObj);
		// / create invoice items
		Vector vecDocRow = new Vector(this.tableRows.values());
		Vector vecCashsaleItemEJB = new Vector();
		try
		{
			for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt1);
				InvoiceItemObject iiObj = new InvoiceItemObject(invoiceObj, docrow);
				InvoiceItem iiEJB = InvoiceItemNut.fnCreate(iiObj);
				vecCashsaleItemEJB.add(iiEJB);
				invoiceObj.vecInvoiceItems.add(iiObj);
				// / effect the Stock Delta, Stock balance etc.
				// / we are not doing this at the InvoiceItemNut level
				// / because when DeliveryOrder is used, creation of
				// / invoice does not affect the stock
				// / however, from accounting perspective
				// / once stock is delivered, asset reduced, by right
				// / there should be a corresponding increase in
				// / Account receivable... to be investigated later..
				// StockNut.sell(invoiceObj.mUserIdUpdate, //Integer
				// personInCharge,
				StockNut.sell(iiObj.mPic2, // Integer personInCharge,
						iiObj.mItemId,// Integer itemId,
						invoiceObj.mLocationId, invoiceObj.mPCCenter, iiObj.mTotalQty, iiObj.mUnitPriceQuoted,
						iiObj.mCurrency, InvoiceItemBean.TABLENAME, iiObj.mPkid, iiObj.mRemarks, // remarks
						invoiceObj.mTimeIssued, invoiceObj.mUserIdUpdate, new Vector(iiObj.colSerialObj),
						"", "", "", "", invoiceObj.mCustSvcCtrId);
			}
			JournalTxnLogic.fnCreate(invoiceObj);
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
		// / update sales order object and EJB to populate invoice PKID
		try
		{
			SalesOrderIndexObject soObjTemp = this.soObj;			
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObjTemp.pkid);
			this.soObj.idInvoice = invoiceObj.mPkid;
			soObjTemp.idInvoice = invoiceObj.mPkid;
			
			soEJB.setObject(soObjTemp);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		// / check if receipt amount > 0
		// / create doc link
		// /////////// MUST CREATE THE DOC LINK BEAN HERE!!!!
		if (this.receiptForm.getReceiptAmt().signum() > 0)
		{
			OfficialReceiptObject receiptObj = this.receiptForm.getReceipt();
			DocLinkObject dlObj = new DocLinkObject();
			dlObj.namespace = DocLinkBean.NS_CUSTOMER;
			// dlObj.reference = "";
			dlObj.relType = DocLinkBean.RELTYPE_PYMT_INV;
			dlObj.srcDocRef = OfficialReceiptBean.TABLENAME;
			dlObj.srcDocId = receiptObj.pkid;
			dlObj.tgtDocRef = InvoiceBean.TABLENAME;
			dlObj.tgtDocId = invoiceObj.mPkid;
			dlObj.currency = invoiceObj.mCurrency;
			dlObj.amount = receiptObj.amount.negate();
			// dlObj.currency2 = "";
			// dlObj.amount2 = new BigDecimal("0.00");
			// dlObj.remarks = "";
			dlObj.status = DocLinkBean.STATUS_ACTIVE;
			dlObj.lastUpdate = TimeFormat.getTimestamp();
			dlObj.userIdUpdate = this.userId;
			DocLinkNut.fnCreate(dlObj);
		}
	}

	private Integer hasAuthorityLevel(Integer roleId)
	{
		Integer result = new Integer(0);
		String permissionId = null;
		UserPermissionsObject upObj = null;
		Vector vecpkid = null;
		
		ArrayList permName = new ArrayList(0);
		for(int i = 0;i<10;i++)
		{
			permName.add("perm_distribution_level_" + (i + 1));
		}
		
		for(int j = 0; j<10; j++)
		{
			QueryObject query = new QueryObject(new String[] { UserPermissionsBean.NAME + " = '" + permName.get(j) + "' " });
			try
			{
				
				vecpkid = new Vector(UserPermissionsNut.getObjects(query));
				if (vecpkid != null)
				{
					for (int i = 0; i < vecpkid.size(); i++)
					{
						upObj = (UserPermissionsObject) vecpkid.get(i);
					}
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			if (upObj != null)
			{
				permissionId = upObj.pkid.toString();
				query = new QueryObject(new String[] { UserObjectPermissionsBean.PERMISSION_ID + " = '" + permissionId + "' ",
						UserObjectPermissionsBean.ROLE_ID + " = '" + roleId + "'"});
				Vector vecUOP = new Vector();
				
				vecUOP = (Vector) UserObjectPermissionsNut.getObjects(query);
				UserObjectPermissionsObject uopObj = null;
				if(vecUOP.size()>0)
				{
					uopObj = (UserObjectPermissionsObject) vecUOP.get(0);
				}
				if(uopObj!=null)
				{
					result = new Integer(j + 1);
				}
			}			
		}



		return result;
	}
	
	public String checkInventoryQtyItem(Integer locationid, Integer itemid) throws Exception
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		TreeMap objSort = new TreeMap();
		//LocationObject lObj = LocationNut.getObject(locationid);
		DocRow objdrow = new DocRow();
		Stock stkEJB;
		StockObject stkObj;
		Log.printVerbose("Entered checkInventoryQty, size: " + this.tableRows.size());
		for(int i=0;i<this.tableRows.size();i++)
		{
			objdrow = (DocRow) vecDocRow.get(i);
			Log.printVerbose("Iteration: " + i);
			if(objdrow.getBomId().intValue()==0 && !objdrow.getPackageGroup().equals(""))
			{
				Log.printVerbose("package header detected.");
				Log.printVerbose("objdrow.getBomId().intValue(): " + objdrow.getBomId().intValue());
				Log.printVerbose("objdrow.getPackageGroup(): " + objdrow.getPackageGroup());
				// Don't check; this is a package header.
			}
			else
			{
				// Add into TreeMap. This is because users may enter in identical items,
				// but different entries. The total qty of such items must be checked against
				// the stock values in the database, instead of individually comparing them,
				// which is inaccurate.
				
				// If the item doesn't exist, put it in the TreeMap
				if(objSort.get(new Integer(objdrow.itemId))==null)
				{
					DocRow objdrowTemp = new DocRow();
					objdrowTemp.itemId = objdrow.itemId;
					objdrowTemp.itemCode = objdrow.itemCode;
					objdrowTemp.qty = objdrow.getQty();
					objSort.put(new Integer(objdrow.itemId),objdrowTemp);
				}
				else
				// If the item exists, just add the current qty to that, to get the total qty
				// for that particular item Id.
				{
					DocRow objdrowTemp2 = (DocRow) objSort.get(new Integer(objdrow.itemId));
					objdrowTemp2.itemId = objdrow.itemId;
					objdrowTemp2.itemCode = objdrow.itemCode;
					objdrowTemp2.qty = objdrowTemp2.qty.add(objdrow.getQty());
					objSort.remove(new Integer(objdrow.itemId));
					objSort.put(new Integer(objdrow.itemId),objdrowTemp2);
				}
			}
		}
		
		// Now to actually check if those quantities can be supported by existing stock.
		Vector vecQty = new Vector(objSort.values());
		for(int j=0;j<vecQty.size();j++)
		{
			DocRow objdrowQty = (DocRow)vecQty.get(j);
			if(new Integer(objdrowQty.itemId).equals(itemid))
			{
				stkEJB = StockNut.getObjectBy(new Integer(objdrowQty.itemId),locationid,new Integer(0));
				if(stkEJB==null)
				{
					Log.printVerbose("Never had any stock.");
					return objdrowQty.itemCode;
				}
				stkObj = stkEJB.getObject();
				Log.printVerbose("stkObj created.");
				if(stkObj.balance.compareTo(objdrowQty.getQty())<0)
				{
					Log.printVerbose("Stock("+ stkObj.balance +") less than required("+objdrowQty.getQty()+").");
					return objdrowQty.itemCode;
				}	
				Log.printVerbose(objdrowQty.itemCode + " has enough qty in stock: " + stkObj.balance.toString() + "compared to the invoice's requirements: " + objdrowQty.getQty());				
			}	
		}
		
		return "#GOOD#";
	}
	
	public String checkInventoryQty(Integer locationid) throws Exception
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		TreeMap objSort = new TreeMap();
		//LocationObject lObj = LocationNut.getObject(locationid);
		DocRow objdrow = new DocRow();
		Stock stkEJB;
		StockObject stkObj;
		Log.printVerbose("Entered checkInventoryQty, size: " + this.tableRows.size());
		for(int i=0;i<this.tableRows.size();i++)
		{
			objdrow = (DocRow) vecDocRow.get(i);
			Log.printVerbose("Iteration: " + i);
			if(objdrow.getBomId().intValue()==0 && !objdrow.getPackageGroup().equals(""))
			{
				Log.printVerbose("package header detected.");
				Log.printVerbose("objdrow.getBomId().intValue(): " + objdrow.getBomId().intValue());
				Log.printVerbose("objdrow.getPackageGroup(): " + objdrow.getPackageGroup());
				// Don't check; this is a package header.
			}
			else
			{
				// Add into TreeMap. This is because users may enter in identical items,
				// but different entries. The total qty of such items must be checked against
				// the stock values in the database, instead of individually comparing them,
				// which is inaccurate.
				
				// If the item doesn't exist, put it in the TreeMap
				if(objSort.get(new Integer(objdrow.itemId))==null)
				{
					DocRow objdrowTemp = new DocRow();
					objdrowTemp.itemId = objdrow.itemId;
					objdrowTemp.itemCode = objdrow.itemCode;
					objdrowTemp.qty = objdrow.getQty();
					objSort.put(new Integer(objdrow.itemId),objdrowTemp);
				}
				else
				// If the item exists, just add the current qty to that, to get the total qty
				// for that particular item Id.
				{
					DocRow objdrowTemp2 = (DocRow) objSort.get(new Integer(objdrow.itemId));
					objdrowTemp2.itemId = objdrow.itemId;
					objdrowTemp2.itemCode = objdrow.itemCode;
					objdrowTemp2.qty = objdrowTemp2.qty.add(objdrow.getQty());
					objSort.remove(new Integer(objdrow.itemId));
					objSort.put(new Integer(objdrow.itemId),objdrowTemp2);
				}
			}
		}
		
		// Now to actually check if those quantities can be supported by existing stock.
		Vector vecQty = new Vector(objSort.values());
		for(int j=0;j<vecQty.size();j++)
		{
			DocRow objdrowQty = (DocRow)vecQty.get(j);
			stkEJB = StockNut.getObjectBy(new Integer(objdrowQty.itemId),locationid,new Integer(0));
			if(stkEJB==null)
			{
				Log.printVerbose("Never had any stock.");
				return objdrowQty.itemCode;
			}
			stkObj = stkEJB.getObject();
			Log.printVerbose("stkObj created.");
			if(stkObj.balance.compareTo(objdrowQty.getQty())<0)
			{
				Log.printVerbose("Stock("+ stkObj.balance +") less than required("+objdrowQty.getQty()+").");
				return objdrowQty.itemCode;
			}	
			Log.printVerbose(objdrowQty.itemCode + " has enough qty in stock: " + stkObj.balance.toString() + "compared to the invoice's requirements: " + objdrowQty.getQty());			
		}
		
		return "#GOOD#";
	}
	
	public boolean checkCreditTermsOutstanding(CustAccountObject customer, BigDecimal SOAmount)
	{
		boolean result = false;
		CustCreditControlChecker creditChecker = new CustCreditControlChecker();
     	creditChecker.setAccount(custAccObj);
     	creditChecker.generateReport();
        CustCreditControlChecker cccc = creditChecker;
        if(cccc!=null)
        {
        	CustCreditControlChecker.PerCustomer perCust = cccc.getSingleCustomerReport();
	        if(perCust!=null)
	        {
	           for(int cnt2=0;cnt2< perCust.vecPerBranch.size();cnt2++)
	           {
	              CustCreditControlChecker.PerCustomer.PerBranch perBranch =
	                 (CustCreditControlChecker.PerCustomer.PerBranch) perCust.vecPerBranch.get(cnt2);
	              for(int cnt3=0;cnt3<perBranch.vecDocument.size();cnt3++)
	              {
	                 CustCreditControlChecker.PerCustomer.PerBranch.PerDocument document
	                          = (CustCreditControlChecker.PerCustomer.PerBranch.PerDocument) perBranch.vecDocument.get(cnt3);
	                 if(perCust.termsDay.intValue()<document.nDays.intValue())
	                 { 
	                	 result = true;
	                 }
	              }/// end cnt3
	           } /// end cnt2
	        }/// end of perCust!=null
        }/// end cccc
        return result;
	}
	
	private boolean checkCreditLimitBreached(CustAccountObject customer, BigDecimal SOAmount)
	{
		boolean result = false;
		CustCreditControlChecker cccc = new CustCreditControlChecker();
		cccc.setAccount(customer);
		cccc.generateReport();
		if (cccc != null)
		{
			CustCreditControlChecker.PerCustomer perCust = cccc.getSingleCustomerReport();
			// boolean credit_alarm = false;
			if (perCust != null)
			{
				if(perCust.totalOutstanding.compareTo(perCust.creditLimit)>0)
				{
					result = true;
				}
/*				BigDecimal total = perCust.totalOutstanding.add(SOAmount);
				if (total.compareTo(perCust.creditLimit) > 0)
				{
					result = true;
				}*/
			}
		}
		return result;
	}
	
	private synchronized String createSoRctVchr() throws Exception
	{
		String result = "";
		if (!canCreate())
		{
			return result;
		}
		// / create the SalesOrderIndex and SalesOrderItemObject
		// / first, auto populate certain fields based on input
		// / populate the soIndex fulfillment stage -> production, delivery,
		// completed
		populateSalesOrder();
		// TKW20080401: I know it's April Fool's. But I am SOOO not kidding.
		// This code checks for credit terms and inventory integrity. If the sales order fails, it requires approval.
		// If collection was included in the creation of this sales order, it must be rejected and no receipt can be created.
		boolean creditTermsStandIn = false; // remove this and replace them with the actual integrity functions later
		String option = AppConfigManager.getProperty("SALES-ORDER-APPROVAL-OPTION");
		if(option==null || option.equals("Standard"))
		{
/*			if(!CreditTermsRulesetNut.checkCreditTermsOk(custAccObj.pkid))
			{
				result = STATUS_CREDIT_TERMS_OUTSTANDING;
			}	*/
			if(checkCreditTermsOutstanding(custAccObj,this.soObj.amount))
			{
				result = STATUS_CREDIT_TERMS_OUTSTANDING;
			}
			else if(!checkInventoryQty(this.branch.invLocationId).equals("#GOOD#"))
			{
				result = STATUS_INVENTORY_QTY_INSUFFICIENT;
			}
			else if(checkCreditLimitBreached(custAccObj,this.soObj.amount))
			{
				result = STATUS_BREACHED_CREDIT_LIMIT;
			}
		}
		else if(option.equals("Never"))
		{
			this.soObj.lastApproval = TimeFormat.getTimestamp();
			this.soObj.approverId = this.soObj.useridCreate;
			try
			{
				Integer roleId = UserRoleNut.getRoleId(this.soObj.useridCreate);
				this.soObj.approverLevel = hasAuthorityLevel(roleId);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
			this.soObj.approvalStatus = SalesOrderIndexBean.STATUS_APPROVED;
			
		}
		else if(option.equals("Always"))
		{
			result = STATUS_GENERIC_APPROVAL_REQUIRED;
		}

		// End TKW20080401
		
		// / CHECKING BUSINESS LOGIC
		// / 1) If delivery is required, check if the addresses etc are properly
		// filled in
		// / 2) Check the status of the CRV being consumed to make sure they are
		// still in valid state
		// / 3) Re adjust the new CRV Gained values accordingly
		// / CREATE BASIC OBJECTS
		// / 1) do not create temporary invoice! invoice is to be created at a
		// later stage
		// / 2) create Receipt
		try
		{
			Log.printVerbose("Inside createSoRctVchr");
			
			SalesOrderIndexNut.fnCreate(this.soObj);
			
			Log.printVerbose("Created SalesOrderIndexNut");
			
			if (this.receiptForm.canSave() )
			{
				this.receiptForm.confirmAndSave();
				OfficialReceiptObject rctObj = this.receiptForm.getReceipt();
				this.soObj.idReceipt = rctObj.pkid;
				// / update the CRV consumed upon successful savings of Receipt
				// Object
				// Vector vecCRVRedeemed = new
				// Vector(this.redeemingList.values());
				Set keySet = this.redeemingList.keySet();
				Iterator keyItr = keySet.iterator();

				while (keyItr.hasNext())
				{
					Long crvPkid = (Long) keyItr.next();
					BigDecimal redeemAmt = (BigDecimal) this.redeemingList.get(crvPkid);
					try
					{
						CashRebateVoucher bufEJB = CashRebateVoucherNut.getHandle(crvPkid);
						CashRebateVoucherObject bufObj = bufEJB.getObject();
						bufObj.voucherBalance = bufObj.voucherBalance.subtract(redeemAmt);
						bufObj.usedAtBranch = branch.pkid;
						bufObj.usedAtPCCenter = branch.accPCCenterId;
						bufObj.usedTime = TimeFormat.getTimestamp();
						bufObj.tgtTable1 = OfficialReceiptBean.TABLENAME;
						bufObj.tgtKey1 = rctObj.pkid;
						bufObj.tgtTable2 = SalesOrderIndexBean.TABLENAME;
						bufObj.tgtKey2 = this.soObj.pkid;
						// bufObj.tgtKey2 = rctObj.pkid;
						bufEJB.setObject(bufObj);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		} catch (Exception ex)
		{
			throw new Exception(ex);
		}
		// / 3) create the salesOrderObject and SalesOrderItem
		// SalesOrderIndexNut.fnCreate(this.soObj);
		this.vecRecentlyCreatedSO.add(0, this.soObj);
		this.popupPrintSO = true;
		// // CROSS POPULATE THE PKIDs into different objects
		// // create respective DOCLinks
		if (this.receiptForm.canSave())
		{
			DocLinkObject dlObj = new DocLinkObject();
			dlObj.namespace = DocLinkBean.NS_CUSTOMER;
			// dlObj.reference = "";
			dlObj.relType = DocLinkBean.RELTYPE_PYMT_SO;
			dlObj.srcDocRef = OfficialReceiptBean.TABLENAME;
			dlObj.srcDocId = this.receiptForm.getReceipt().pkid;
			dlObj.tgtDocRef = SalesOrderIndexBean.TABLENAME;
			dlObj.tgtDocId = this.soObj.pkid;
			dlObj.currency = this.soObj.currency;
			dlObj.amount = this.receiptForm.getReceipt().amount.negate();
			// dlObj.currency2 = "";
			// dlObj.amount2 = new BigDecimal("0.00");
			// dlObj.remarks = "";
			dlObj.status = DocLinkBean.STATUS_ACTIVE;
			dlObj.lastUpdate = TimeFormat.getTimestamp();
			dlObj.userIdUpdate = this.userId;
			DocLinkNut.fnCreate(dlObj);
		}

		return result;
	}

	private boolean requireProduction()
	{
		boolean rtn = false;
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			if (docrow.productionRequired)
			{
				rtn = true;
			}
		}
		return rtn;
	}

	private boolean requireDelivery()
	{
		boolean rtn = false;
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			if (docrow.deliveryRequired)
			{
				rtn = true;
			}
		}
		return rtn;
	}

	private void populateSalesOrder()
	{
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;

		Log.printVerbose("Check point AAA");
		if (this.custUserObj != null)
		{
			this.soObj.senderTable2 = CustUserBean.TABLENAME;
			this.soObj.senderKey2 = this.custUserObj.pkid;
		}

		Log.printVerbose("Check point BBB");
		this.soObj.branch = this.branch.pkid;
		this.soObj.pccenter = this.branch.accPCCenterId;
		this.soObj.location = this.branch.invLocationId;
		this.soObj.description = this.soObj.description;
		this.soObj.productionLocation = this.branch.invLocationId;
		this.soObj.currency = this.branch.currency;

		Log.printVerbose("Check point CCC");
		this.soObj.amount = getBillAmount();
		this.soObj.amountOutstanding = this.soObj.amount.subtract(this.receiptForm.getReceiptAmt());
		this.soObj.timeCreate = TimeFormat.getTimestamp();
		this.soObj.timeUpdate = TimeFormat.getTimestamp();
		this.soObj.useridCreate = this.userId;
		this.soObj.useridEdit = this.userId;
		UserObject userObj = UserNut.getObject(this.userId);
		this.soObj.ordertakerUserid = this.userId;
		this.soObj.ordertakerName = userObj.userName;
		this.soObj.ordertakerTime = TimeFormat.getTimestamp();
		this.soObj.processProduction = requireProduction();
		this.soObj.processDelivery = requireDelivery();
		Vector vecDocRow = new Vector(this.tableRows.values());
		this.soObj.processProduction = false;
		this.soObj.processDelivery = false;
		this.soObj.etxnStatus = this.etxnStatus;
		
		Log.printVerbose("Check point DDD");
		if (this.soObj.thirdpartyLoyaltyCardCode.length() > 3)
		{
			this.soObj.thirdpartyLoyaltyCardPtsGain = this.soObj.amount;
		}

		Log.printVerbose("Check point EEE");
		for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt1);
			SalesOrderItemObject soItmObj = new SalesOrderItemObject(this.soObj, docrow);
			if (this.memCardObj == null)
			{
				soItmObj.crvGain = new BigDecimal(0);
			}
			this.soObj.vecItem.add(soItmObj);
			if (soItmObj.productionRequired)
			{
				this.soObj.processProduction = true;
			}
			if (soItmObj.deliveryRequired)
			{
				this.soObj.processDelivery = true;
			}
		}

		Log.printVerbose("Check point FFF");
	}

	public TreeMap getRedeemableCRV()
	{
		return this.redeemableCRV;
	}

	/*
	 * public void addRedeemingCRV(String pkid) { Long lPkid = null; String
	 * theKey = ""; try { lPkid = new Long(pkid); CashRebateVoucherObject
	 * cashRVObj = CashRebateVoucherNut.getObject(lPkid); theKey =
	 * TimeFormat.strDisplayDate(cashRVObj.dateGoodThru)+pkid; /// if the pkid
	 * is ok /// remove previous entry this.redeemingList.remove(lPkid);
	 * 
	 * if(cashRVObj.voucherBalance.signum()<=0){ return;}
	 * 
	 * this.redeemingList.put(lPkid,cashRVObj); } catch(Exception ex) { return;} }
	 */
	public void addRedeemingCRV(String pkid, String amount)
	{
		Long lPkid = null;

		try
		{
			BigDecimal redeemAmt = new BigDecimal(amount);
			if (redeemAmt.signum() == 0)
			{
				return;
			}
			lPkid = new Long(pkid);
			CashRebateVoucherObject cashRVObj = CashRebateVoucherNut.getObject(lPkid);
			TimeFormat.strDisplayDate(cashRVObj.dateGoodThru);
			// / if the pkid is ok
			// / remove previous entry
			this.redeemingList.remove(lPkid);
			if (cashRVObj.voucherBalance.signum() <= 0)
			{
				return;
			}
			if (redeemAmt.compareTo(cashRVObj.voucherBalance) > 0)
			{
				return;
			}
			this.redeemingList.put(lPkid, redeemAmt);
			recalculateRedeemingCRV();
		} catch (Exception ex)
		{
			return;
		}
	}

	public void recalculateRedeemingCRV()
	{
		ReceiptForm rctForm = getReceiptForm();
		if (getBillAmount().compareTo(getRedeemingCRVTotal()) < 0)
		{
			rctForm.setAmountCoupon(getBillAmount());
		} else
		{
			rctForm.setAmountCoupon(getRedeemingCRVTotal());
		}
	}

	public BigDecimal dropRedeemingCRV(String key)
	{
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.remove(lPkid);
	}

	public BigDecimal getRedeemingCRVAmount(String key)
	{
		/*
		 * CashRebateVoucherObject cashRV = (CashRebateVoucherObject)
		 * this.redeemingList.get(key); if(cashRV!=null){ return
		 * cashRV.voucherBalance;} else{ return (BigDecimal) null;}
		 */
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.get(lPkid);
	}

	public void clearRedeemingList()
	{
		this.redeemingList.clear();
	}

	public void retrieveRedeemableCRV()
	{
		this.redeemingList.clear();
		this.redeemableCRV.clear();
		if (!getValidMemberCard())
		{
			return;
		}
		// if(!getValidBranch()){return ;}
		Timestamp tsToday = TimeFormat.getTimestamp();
		Timestamp afterToday = TimeFormat.add(tsToday, 0, 0, 1);
		Timestamp beforeToday = TimeFormat.add(tsToday, 0, 0, -1);
		QueryObject query = new QueryObject(new String[] {
				CashRebateVoucherBean.CARD_ID + " = '" + this.memCardObj.pkid.toString() + "' ",
				CashRebateVoucherBean.DATE_VALID_FROM + " < '" + TimeFormat.strDisplayDate(afterToday) + "' ",
				CashRebateVoucherBean.DATE_GOOD_THRU + " >= '" + TimeFormat.strDisplayDate(beforeToday) + "' ", });
		query.setOrder(" ORDER BY " + CashRebateVoucherBean.DATE_GOOD_THRU + ", " + CashRebateVoucherBean.PKID);
		Vector vecRedeemableCRV = new Vector(CashRebateVoucherNut.getObjects(query));
		if (this.redeemableCRV == null)
		{
			this.redeemableCRV = new TreeMap();
		}
		for (int cnt1 = 0; cnt1 < vecRedeemableCRV.size(); cnt1++)
		{
			CashRebateVoucherObject bufObj = (CashRebateVoucherObject) vecRedeemableCRV.get(cnt1);
			this.redeemableCRV.put(TimeFormat.strDisplayDate(bufObj.dateGoodThru) + bufObj.pkid.toString(), bufObj);
		}
	}

	public BigDecimal getRedeemingCRVTotal()
	{
		Vector vecDocRow = new Vector(this.redeemingList.values());
		BigDecimal totalAmt = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
		{
			// CashRebateVoucherObject cashRV = (CashRebateVoucherObject)
			// vecDocRow.get(cnt1);
			// totalAmt = totalAmt.add(cashRV.voucherBalance);
			BigDecimal redeemAmt = (BigDecimal) vecDocRow.get(cnt1);
			if (redeemAmt != null)
			{
				totalAmt = totalAmt.add(redeemAmt);
			}
		}
		return totalAmt;
	}
	/*
	 * public BigDecimal getTotalReceiptAmount() { BigDecimal total = new
	 * BigDecimal(0); Vector vecDocLink = new
	 * Vector(DocLinkNut.getByTargetDoc(SalesOrderIndexBean.TABLENAME,
	 * this.soObj.pkid)); for(int cnt1=0;cnt1<vecDocLink.size();cnt1++) { }
	 * return total; }
	 */

	public void setReceiptMode(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.receiptMode = buf;
		}
	}

	public void setPaymentRemarks(String buf)
	{
		this.soObj.receiptRemarks = buf;
	}

	public void setPaymentStatus(String buf)
	{
		this.soObj.statusPayment = buf;
	}

	public void setDisplayFormat(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.displayFormat = buf;
		}
	}

	public void setOrderType2(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.soType2 = SalesOrderIndexBean.SO_TYPE2_FLORIST;
		}
	}

	public void setEtxnType(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.etxnType = buf;
		}
	}
	
	public void setPromo(String promoType, String promoCode, String promoNumber, 
		String promoName, BigDecimal promoDiscountAmount, BigDecimal promoDiscountPct)
	{
		this.soObj.promoType = promoType;
		this.soObj.promoCode = promoCode;
		this.soObj.promoNumber = promoNumber;
		this.soObj.promoName = promoName;
		this.soObj.promoDiscountAmount = promoDiscountAmount;
		this.soObj.promoDiscountPct = promoDiscountPct;
	}


}
