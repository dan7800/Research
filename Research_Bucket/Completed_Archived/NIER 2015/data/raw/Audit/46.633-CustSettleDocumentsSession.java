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
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.util.*;

public class CustSettleDocumentsSession extends java.lang.Object implements Serializable
{
	// public Hashtable outstandingDocuments;
	public TreeMap outstandingDocuments;
	public OfficialReceiptObject receiptObj;
	public Vector prevReceipts = new Vector();
	//public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private String state;
	private BranchObject branch;
	private CustAccountObject customer = null;
	private String referenceNo = "";
	private String remarks = "";
	private Integer userId;
	private Timestamp pdChequeDate;
	private String chequeNumber;
	private String chequeNumberPD;
	private BigDecimal cardPctCharges;
	private String cardType;
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
	private BigDecimal openBalance = new BigDecimal(0);
	private BigDecimal chequeCharges;
	private BigDecimal surcharge;
	private String ccNumber;
	private String ccType;
	private String ccName;
	private String ccBank;
	private String ccApprovalCode;
	private Timestamp ccExpiry;
	private String ccSecurity;
	private boolean bSetDate = false;
	private Integer cardPaymentConfig = new Integer(0);
	private boolean isRemoteCreditTransaction = false;

	private boolean branchFilter = true;
	public boolean creditAlarm = false;
	
	private Timestamp tsDate = null;
	
	public void setIsRemoteCreditTransaction(boolean val)
	{
		this.isRemoteCreditTransaction = val;
	}
	
	public void setBranchFilter(boolean buf)
	{ this.branchFilter = buf;}
	
	public boolean getBranchFilter()
	{ return this.branchFilter;}


	public void setSurcharge(BigDecimal sc)
	{
		this.surcharge = sc;
	}

	public BigDecimal getSurcharge()
	{
		return this.surcharge;
	}

	public void setCCDetails(String ccNumber, String ccType, String ccName, String ccBank, String ccApprovalCode, Timestamp ccExpiry, String ccSecurity)
	{
		this.ccNumber = ccNumber;
		this.ccType = ccType;
		this.ccName = ccName;
		this.ccBank = ccBank;
		this.ccApprovalCode = ccApprovalCode;
		this.ccExpiry = ccExpiry;
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

	public String getCCApprovalCode()
	{
		return this.ccApprovalCode;
	}

	public Timestamp getCCExpiry()
	{
		return this.ccExpiry;
	}

	public String getCCSecurity()
	{
		return this.ccSecurity;
	}

	public TreeMap getOutstandingDocuments()
	{
		return this.outstandingDocuments;
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

	public BigDecimal getOpenBalance()
	{
		return this.openBalance;
	}

	public void setOpenBalance(BigDecimal openBalance)
	{
		this.openBalance = openBalance;
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
		System.out.println("this.amountCash"+ this.amountCash);
	}

	public BigDecimal getAmountCard()
	{
		return this.amountCard;
	}

	public void setAmountCard(BigDecimal amt)
	{
		this.amountCard = amt;
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

	public BigDecimal getAmountCoupon()
	{
		return this.amountCoupon;
	}

	public void setAmountCoupon(BigDecimal amt)
	{
		this.amountCoupon = amt;
	}

	public BigDecimal getAmountOther()
	{
		return this.amountOther;
	}

	public void setAmountOther(BigDecimal amt)
	{
		this.amountOther = amt;
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
		// TKW20080512: This if statement is for mobile88
		if(this.isRemoteCreditTransaction)
		{
			return this.surcharge;
		}
		else
		{
			//return this.cardPctCharges.multiply(getAmountCard()).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN);
			//20080422 Jimmy -> rounding up 2 decimal (eg. 270 * 3.85 = 10.40, not 10.395)		
			return this.cardPctCharges.multiply(getAmountCard()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_EVEN);
		}
		
	} 

	public String getCardType()
	{
		return this.cardType;
	}

	public void setCardType(String type)
	{
		this.cardType = type;
	}

	public void setDate(Timestamp txDate)
	{
		if (TimeFormat.strDisplayDate(txDate).equals(TimeFormat.strDisplayDate(this.tsDate)))
		{
			return;
		}
		this.tsDate = txDate;
		bSetDate = true;
	}

	public String getDate(String buffer)
	{
		if (bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		return TimeFormat.strDisplayDate(this.tsDate);
	}

	public Timestamp getDate()
	{
		if (bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		return this.tsDate;
	}

	public String getState()
	{
		return this.state;
	}
	
	public void setState(String buffer)
	{
		this.state = buffer;
	}
	
	public BigDecimal getReceiptAmt()
	{
		BigDecimal totalReceipt = this.amountCash.add(this.amountCard).add(this.amountCheque).add(this.amountPDCheque).add(this.amountCoupon).add(this.amountOther);
		return totalReceipt;
	}

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

	public void getCheckReceipt() throws Exception
	{
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


	public boolean getValidBranch()
	{
		return (this.branch != null);
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


	
	public boolean setBranch(Integer iCustSvcCtr)
	{
		this.branch = BranchNut.getObject(iCustSvcCtr);
		System.out.println("Branch="+this.branch);
		if (this.branch != null)
		{
			// / need to load all cash account objects
			this.cashbookCash = CashAccountNut.getObject(this.branch.cashbookCash);
			this.cashbookCard = CashAccountNut.getObject(this.branch.cashbookCard);
			this.cashbookCheque = CashAccountNut.getObject(this.branch.cashbookCheque);
			this.cashbookPDCheque = CashAccountNut.getObject(this.branch.cashbookPDCheque);
			this.cashbookCoupon = CashAccountNut.getObject(this.branch.cashbookCoupon);
			this.cashbookOther = CashAccountNut.getObject(this.branch.cashbookOther);
			retrieveOutstandingDocuments();
			return true;
		} else
		{
			return false;
		}
	}

	public BranchObject getBranch()
	{
		return this.branch;
	}
	


	public String getPCCenter()
	{
		if (this.branch == null)
		{
			return "x";
		}
		return this.branch.accPCCenterId.toString();
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
	
	public String getOtherCashbook()
	{
		if(this.cashbookOther == null)
		{
			return "0";
		}
		return this.cashbookOther.pkId.toString();
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

	public void setChequeCashbook(Integer chequeBook)
	{
		this.cashbookCheque = CashAccountNut.getObject(chequeBook);
	}

	public void setOtherCashbook(Integer otherBook)
	{
		this.cashbookOther = CashAccountNut.getObject(otherBook);
	}


	public boolean setCustomer(Integer iCustomer)
	{
		this.customer = CustAccountNut.getObject(iCustomer);
		this.state = this.customer.state;
		this.creditAlarm = false;
		retrieveOutstandingDocuments();
		return (this.customer != null);
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
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void setCardPaymentConfig(String cpcPkid, BigDecimal bankCharge)
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
				if(bankCharge.compareTo(new BigDecimal(0))>0)
				{
					setSurcharge(bankCharge);
				}
				
				// make sure the cashbook exists
				CashAccountObject cashbookCardOpt = CashAccountNut.getObject(cpcObj.cashbook);
				if (cashbookCardOpt != null)
				{
					setCardCashbook(cpcObj.cashbook);
				}

			}
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public Long saveReceipt()
	{
		// // create the receipt
		Log.printVerbose("%%%%%%%%%%%CHECK1");
		this.receiptObj = new OfficialReceiptObject();
		this.receiptObj.entityTable = CustAccountBean.TABLENAME;
		this.receiptObj.entityKey = this.customer.pkid;
		this.receiptObj.entityName = this.customer.name;
		this.receiptObj.currency = this.cashbookCash.currency;
		this.receiptObj.amount = this.getReceiptAmt();
		// this.receiptObj.paymentTime = TimeFormat.getTimestamp();
		this.receiptObj.paymentTime = this.tsDate;
		// this.receiptObj.paymentMethod =
		this.receiptObj.paymentRemarks = this.remarks;
		if (this.amountCheque.signum() > 0)
		{
			this.receiptObj.chequeNumber = this.chequeNumber;
		}
		Log.printVerbose("%%%%%%%%%%%CHECK2");
		this.receiptObj.lastUpdate = TimeFormat.getTimestamp();
		this.receiptObj.userIdUpdate = this.userId;
		Log.printVerbose("%%%%%%%%%%%CHECK21");
		this.receiptObj.cbCash = this.cashbookCash.pkId;
		Log.printVerbose("%%%%%%%%%%%CHECK22");
		this.receiptObj.cbCard = this.cashbookCard.pkId;
		Log.printVerbose("%%%%%%%%%%%CHECK23");
		this.receiptObj.cbCheque = this.cashbookCheque.pkId;
		Log.printVerbose("%%%%%%%%%%%CHECK24");
		this.receiptObj.cbPDCheque = this.cashbookPDCheque.pkId;
		Log.printVerbose("%%%%%%%%%%%CHECK25");
		this.receiptObj.cbCoupon = this.cashbookCoupon.pkId;
		Log.printVerbose("%%%%%%%%%%%CHECK26");
		this.receiptObj.cbOther = this.cashbookOther.pkId;
		Log.printVerbose("%%%%%%%%%%%CHECK27");
		this.receiptObj.amountCash = this.amountCash;
		Log.printVerbose("%%%%%%%%%%%CHECK28");
		this.receiptObj.amountCard = this.amountCard;
		Log.printVerbose("%%%%%%%%%%%CHECK29");
		this.receiptObj.amountCheque = this.amountCheque;
		Log.printVerbose("%%%%%%%%%%%CHECK30");
		this.receiptObj.amountPDCheque = this.amountPDCheque;
		Log.printVerbose("%%%%%%%%%%%CHECK31");
		this.receiptObj.amountCoupon = this.amountCoupon;
		Log.printVerbose("%%%%%%%%%%%CHECK32");
		this.receiptObj.amountOther = this.amountOther;
		Log.printVerbose("%%%%%%%%%%%CHECK33");
		if (this.amountPDCheque.signum() > 0)
		{
			Log.printVerbose("%%%%%%%%%%%CHECK34");
			this.receiptObj.chequeNumberPD = this.chequeNumberPD;
			Log.printVerbose("%%%%%%%%%%%CHECK35");
			this.receiptObj.pdcExist = true;
			Log.printVerbose("%%%%%%%%%%%CHECK36");
		}
		Log.printVerbose("%%%%%%%%%%%CHECK3");
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
		this.receiptObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE;
		this.receiptObj.openBalance = this.openBalance;
		this.receiptObj.chequeChargesAmount = this.chequeCharges;
		this.receiptObj.cardSurcharge = this.surcharge;
		this.receiptObj.cardName = this.ccName;
		// this.receiptObj.cardType = this.ccType;
		this.receiptObj.cardBank = this.ccBank;
		this.receiptObj.cardNumber = this.ccNumber;
		this.receiptObj.cardApprovalCode = this.ccApprovalCode;
		this.receiptObj.cardSecurityNum = this.ccSecurity;
		this.receiptObj.cardValidThru = this.ccExpiry;
		
		this.receiptObj.cardId = this.cardPaymentConfig; // 20080528 Jimmy
		OfficialReceiptNut.fnCreateFromSettlement(this.receiptObj);
		Log.printVerbose("%%%%%%%%%%%CHECK4");
		// / reset the outstanding balance of all invoices
		// // create the corresponding doclink beans.
		// create the respective DocLinkBeans
		Vector vecDocLinkEJB = new Vector();
		try
		{
			Vector vecDoc = new Vector(this.outstandingDocuments.values());
			for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
			{
				Log.printVerbose("%%%%%%%%%%%CHECK5." + cnt1);
				Document oneDoc = (Document) vecDoc.get(cnt1);
				if (oneDoc.docRef.equals(InvoiceBean.TABLENAME) && oneDoc.thisSettlement.signum() > 0)
				{
					Log.printVerbose("%%%%%%%%%%%CHECK5." + cnt1 + ".1");
					Invoice thisInvEJB = InvoiceNut.getHandle(oneDoc.docId);
					thisInvEJB.adjustOutstanding(oneDoc.thisSettlement.negate());
					BigDecimal addBackPdc = this.receiptObj.amountPDCheque.multiply(oneDoc.thisSettlement.divide(getSettlementAmt(), 12, BigDecimal.ROUND_HALF_EVEN));
					thisInvEJB.adjustOutstandingBfPdc(oneDoc.thisSettlement.negate().add(addBackPdc));
					// Create the DocLink
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER,this.receiptObj.chequeNumberPD+" "
					+this.receiptObj.chequeNumber , DocLinkBean.RELTYPE_PYMT_INV, OfficialReceiptBean.TABLENAME, this.receiptObj.pkid, InvoiceBean.TABLENAME, oneDoc.docId, this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
																																																																					// tgt
							this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
							// tgt
							"", this.receiptObj.paymentTime, this.userId);
					vecDocLinkEJB.add(newDocLink);
					// // reduce the outstanding amount for the sales order too
					// if exists
					SalesOrderIndex soEJB = SalesOrderIndexNut.getHandleByInvoice(oneDoc.docId);
					Log.printVerbose("%%%%%%%%%%%CHECK5." + cnt1 + ".2");
					if (soEJB != null)
					{
						Log.printVerbose("%%%%%%%%%%%CHECK5." + cnt1 + ".1.1@");
						SalesOrderIndexObject soObj = soEJB.getObject();
						soEJB.adjustOutstanding(oneDoc.thisSettlement.negate());
						DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, this.receiptObj.chequeNumberPD+" "
						+this.receiptObj.chequeNumber, DocLinkBean.RELTYPE_PYMT_SO, OfficialReceiptBean.TABLENAME, this.receiptObj.pkid, SalesOrderIndexBean.TABLENAME, soObj.pkid, this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
																																																																		// tgt
								this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
								// tgt
								// "", TimeFormat.getTimestamp(), this.userId);
								"", this.receiptObj.paymentTime, this.userId);
						Log.printVerbose("%%%%%%%%%%%CHECK5." + cnt1 + ".1.2@");
					}
				}// end if invoice settlement > 0
				if (oneDoc.docRef.equals(CreditMemoIndexBean.TABLENAME) && oneDoc.thisSettlement.signum() > 0)
				{
					Log.printVerbose("%%%%%%%%%%%CHECK5." + cnt1 + ".3");
					CreditMemoIndex cmEJB = CreditMemoIndexNut.getHandle(oneDoc.docId);
					cmEJB.adjustBalance(oneDoc.thisSettlement.negate());
					BigDecimal addBackPdc = this.receiptObj.amountPDCheque.multiply(oneDoc.thisSettlement.divide(getSettlementAmt(), 12, BigDecimal.ROUND_HALF_EVEN));
					cmEJB.adjustBalanceBfPdc(oneDoc.thisSettlement.negate().add(addBackPdc));
					DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, this.receiptObj.chequeNumberPD+" "
					+this.receiptObj.chequeNumber, DocLinkBean.RELTYPE_PYMT_DN, OfficialReceiptBean.TABLENAME, this.receiptObj.pkid, CreditMemoIndexBean.TABLENAME, oneDoc.docId, this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
																																																																							// tgt
							this.receiptObj.currency, oneDoc.thisSettlement.negate(), // reduces
							// tgt
							"", this.receiptObj.paymentTime, this.userId);
					vecDocLinkEJB.add(newDocLink);
					Log.printVerbose("%%%%%%%%%%%CHECK5." + cnt1 + ".4");
				}
			} // end for
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return this.receiptObj.pkid;
	}

	//20050516 Jimmy
	public void checkTheBlackListCondition()
	{
		try 
		{
			// get total outstanding
			CustOutstandingDocumentReport codReport  = new CustOutstandingDocumentReport();
			codReport.setCustomer(this.customer.pkid.toString());
			codReport.generateReport();
			
			for(int cnt1=0;cnt1<codReport.vecCustomer.size();cnt1++)
			{
				
				CustOutstandingDocumentReport.PerCustomer customer =
	        		(CustOutstandingDocumentReport.PerCustomer) codReport.vecCustomer.get(cnt1);
				
				BigDecimal total = customer.totalOutstanding.subtract(this.getSettlementAmt());
				if (total.compareTo(customer.creditLimit)<=0 && this.customer.state.equals("BL"))   
				{ 
					//CustAccount custEJB = CustAccountNut.getHandle(new Integer(customer.custPkid));
					//custEJB.setState(CustAccountBean.STATE_OK);
					creditAlarm = true;
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

	public void UpdateAccountCustomerState()
	{
		try
		{
			CustAccount custEJB = CustAccountNut.getHandle(this.customer.pkid);
			custEJB.setState(this.state);
			if(this.state.equals(CustAccountBean.STATE_BL))
			{
				AuditTrailObject atObj = new AuditTrailObject();
				atObj.userId = this.userId;
				atObj.auditType = AuditTrailBean.TYPE_CONFIG;
				atObj.time = TimeFormat.getTimestamp();
				atObj.remarks = this.customer.name + " has been blacklisted. ";
				atObj.tc_entity_table = CustAccountBean.TABLENAME;
				atObj.tc_entity_id = custEJB.getPkid();
				atObj.tc_action = AuditTrailBean.TC_ACTION_UPDATE;	
				AuditTrailNut.fnCreate(atObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	// / contructor!
	public CustSettleDocumentsSession(Integer iUser)
	{
		this.receiptObj = new OfficialReceiptObject();
		this.state = "";
		this.customer = null;
		this.outstandingDocuments = new TreeMap();
		this.userId = iUser;
		this.pdChequeDate = TimeFormat.getTimestamp();
		this.pdChequeDate = TimeFormat.add(this.pdChequeDate, 0, 1, 0);
		this.chequeNumber = "";
		this.chequeNumberPD = "";
		this.cardType = "";
		this.cardPctCharges = new BigDecimal("0.0");
		this.tsDate = TimeFormat.getTimestamp();
		this.openBalance = new BigDecimal(0);
		this.chequeCharges = new BigDecimal(0);
		this.surcharge = new BigDecimal(0);
		this.ccNumber = "";
		this.ccName = "";
		this.ccType = "";
		this.ccBank = "";
		this.ccApprovalCode = "";
		this.ccExpiry = TimeFormat.getTimestamp();
		this.ccSecurity = "";
	}

	public void reset()
	{
		this.state = "";
		this.customer = null;
		this.receiptObj = new OfficialReceiptObject();
		this.outstandingDocuments.clear();
		this.amountCash = new BigDecimal(0);
		this.amountCard = new BigDecimal(0);
		this.amountCheque = new BigDecimal(0);
		this.amountPDCheque = new BigDecimal(0);
		this.amountCoupon = new BigDecimal(0);
		this.amountOther = new BigDecimal(0);
		this.chequeNumber = "";
		this.chequeNumberPD = "";
		this.cardType = "";
		this.cardPctCharges = new BigDecimal("0.0");
		this.openBalance = new BigDecimal(0);
		retrieveOutstandingDocuments();
		this.chequeCharges = new BigDecimal(0);
		this.surcharge = new BigDecimal(0);
		this.ccNumber = "";
		this.ccName = "";
		this.ccBank = "";
		this.ccApprovalCode = "";
		this.ccSecurity = "";
		this.ccExpiry = TimeFormat.getTimestamp();
		this.tsDate = TimeFormat.getTimestamp();
		this.bSetDate = false;
		this.cardPaymentConfig = new Integer(0);
		this.creditAlarm = false;
		if (this.branch != null)
		{
			this.cashbookCash = CashAccountNut.getObject(this.branch.cashbookCash);
			this.cashbookCard = CashAccountNut.getObject(this.branch.cashbookCard);
			this.cashbookCheque = CashAccountNut.getObject(this.branch.cashbookCheque);
			this.cashbookPDCheque = CashAccountNut.getObject(this.branch.cashbookPDCheque);
			this.cashbookCoupon = CashAccountNut.getObject(this.branch.cashbookCoupon);
			this.cashbookOther = CashAccountNut.getObject(this.branch.cashbookOther);
		}
		this.remarks = "";
	}

	public Long confirmAndSave() throws Exception
	{
		if (!canSave())
			throw new Exception("In complete form!");
		// 1) create the PurchaseOrder and GRN
		Long result = saveReceipt();
		UpdateAccountCustomerState();
		transferExistingDocToPrevBuffer();
		reset();
		return result;
	}

	private void transferExistingDocToPrevBuffer()
	{
		this.prevReceipts.add(this.receiptObj);
	}

	public boolean canSave()
	{
		boolean result = true;
		// check state
		//if (!this.state.equals(STATE_DRAFT))
		//{
		//	result = false;
		//}
		// check valid procurement center
		//if (getSettlementAmt().signum() == 0)
		//{
		//	result = false;
		//}
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
		// if(this.getTotalQty().compareTo(new BigDecimal(0))<=0) { result =
		// false;}
		// check valid amount
		try
		{
			getCheckReceipt();
		} catch (Exception ex)
		{
			result = false;
		}
		// / no check at this time
		if (!(getSettlementAmt().add(getOpenBalance()).compareTo(getReceiptAmt()) == 0))
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
			if(amount.compareTo(oneDoc.docBalance)>0)
			{ oneDoc.thisSettlement = new BigDecimal(CurrencyFormat.strCcy(oneDoc.docBalance)); }
			else
			{ oneDoc.thisSettlement = amount; }
		}
	}

	public BigDecimal getRowAmt(String key)
	{
		Document oneDoc = (Document) this.outstandingDocuments.get(key);
		return oneDoc.thisSettlement;
	}

	public Vector getPreviousReceipts()
	{
		return this.prevReceipts;
	}

	public boolean getValidCustomer()
	{
		return (this.customer != null);
	}

	private void retrieveOutstandingDocuments()
	{
		// check valid supplier account
		if (!getValidCustomer())
		{
			return;
		}
		// / check valid procurement center
		if (!getValidBranch())
		{
			return;
		}
		if (this.outstandingDocuments == null)
		{
			this.outstandingDocuments = new TreeMap();
		} else
		{
			this.outstandingDocuments.clear();
		}
		QueryObject query = new QueryObject(new String[] { InvoiceBean.ENTITY_KEY + " = '" + this.customer.pkid.toString() + "' " + " AND " + InvoiceBean.PC_CENTER + " = '" + this.branch.accPCCenterId.toString() + "' " + " AND " + InvoiceBean.OUTSTANDING_AMT + " > '0' " + " AND " + InvoiceBean.STATUS + " = '" + InvoiceBean.STATUS_ACTIVE + "' " + " AND " + InvoiceBean.ENTITY_TABLE + " = '" + CustAccountBean.TABLENAME + "' " });
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

			if(this.branchFilter==true || this.branch.pkid.equals(cinvObj.mCustSvcCtrId))
			{
				this.outstandingDocuments.put(oneDoc.getKey(), oneDoc);
			}
		}
		QueryObject query2 = new QueryObject(new String[] { CreditMemoIndexBean.ENTITY_KEY + " = '" + this.customer.pkid.toString() + "' ", CreditMemoIndexBean.PC_CENTER + " = '" + this.branch.accPCCenterId.toString() + "' ", CreditMemoIndexBean.BALANCE + " > '0' ", CreditMemoIndexBean.ENTITY_TABLE + " = '" + CustAccountBean.TABLENAME + "' ", CreditMemoIndexBean.STATUS + " = '" + CreditMemoIndexBean.STATUS_ACTIVE + "' " });
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
			if(this.branchFilter== true || this.branch.pkid.equals(cmObj.branch))
			{
				this.outstandingDocuments.put(oneDoc.getKey(), oneDoc);
			}
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
