package com.vlee.bean.remotecreditservices;


import java.io.*;
import java.math.*;
import java.sql.*;
import com.vlee.bean.procurement.*;
import com.vlee.bean.pos.*;
import java.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.application.AppRegBean;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;
import com.vlee.bean.application.AppConfigManager;
import com.vlee.bean.customer.*;
import com.vlee.util.CurrencyFormat;
import com.vlee.util.DocRow;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;
import com.vlee.util.SHA1DigiSignControl;
import com.vlee.util.TimeFormat;

public class ProcessRemoteCreditTransactionForm
{
	private String merchantCode;
	private String merchantName;
	private Integer paymentId;
	private String refNo;
	private BigDecimal amount;
	private String currency;
	private String prodDesc;
	private String userName;
	private String userEmail;
	private String userContact;
	private String remarks;
	private String lang;
	private String signature;
	private String transId;
	private String branchCode;
	private BigDecimal txnFee;
	private BigDecimal bankCharge;
	private Integer userid;
	private String status;
	private Timestamp txnDate;
	private Timestamp postDate;
	private String cardPaymentConfigId;
	private Timestamp dateFrom;
	private Timestamp dateTo;
	private String signatureSeed = "duron6423";
	private Vector vecTxn;
	private String fixedItemCode = "CC-CHARGE";
	private String defaultCurrencyCode = "MYR";
	private String orderBy;
	private String responseStatus;
	private String authCode;
	
	public ProcessRemoteCreditTransactionForm()
	{
		this.merchantCode = "";
		this.paymentId = new Integer(0);
		this.refNo = "";
		this.amount = new BigDecimal(0);
		this.txnFee = new BigDecimal(0);
		this.bankCharge = new BigDecimal(0);
		this.currency = "";
		this.prodDesc = "";
		this.userName = "";
		this.userEmail = "";
		this.userContact = "";
		this.remarks = "";
		this.lang = "";
		this.signature = "";
		this.transId = "0";
		this.branchCode = "";
		this.userid = new Integer(0);
		this.status = "";
		this.txnDate = TimeFormat.createTimestamp("0001-01-01");
		this.postDate = TimeFormat.createTimestamp("0001-01-01");
		this.cardPaymentConfigId = "";
		this.vecTxn = new Vector();
		this.dateFrom = TimeFormat.getTimestamp();//TimeFormat.createTimestamp("0001-01-01");
		this.dateTo = TimeFormat.getTimestamp();//TimeFormat.createTimestamp("0001-01-01");
		this.authCode = "";
		this.orderBy = RemoteCreditServicesBean.AUTH_CODE;
	}
	
	public Vector getVecTxn()
	{
		return this.vecTxn;
	}
	
	public void setOrderBy(String orderBy)
	{
		this.orderBy = orderBy;
	}
		
	public String getOrderBy()
	{
		return this.orderBy;
	}
	
	public String getResponseStatus()
	{
		return this.responseStatus;
	}
	
	public void setMerchantCode(String merchantCode)
	{
		this.merchantCode = merchantCode;
	}
	
	public String getMerchantCode()
	{
		return this.merchantCode;
	}

	public void setMerchantName(String merchantName)
	{
		this.merchantName = merchantName;
	}
	
	public String getMerchantName()
	{
		return this.merchantName;
	}
	
	public void setBranchCode(String branchCode)
	{
		this.branchCode = branchCode;
	}
	
	public String getBranchCode()
	{
		return this.branchCode;
	}
	
	public void setPaymentId(Integer paymentId)
	{
		this.paymentId = paymentId;
	}
	
	public Integer getPaymentId()
	{
		return this.paymentId;
	}
	
	public void setRefNo(String refNo)
	{
		this.refNo = refNo;
	}
	
	public String getRefNo()
	{
		return this.refNo;
	}
	
	public void setStatus(String refNo)
	{
		this.status = refNo;
	}
	
	public String getStatus()
	{
		return this.status;
	}

	public void setAuthCode(String authCode)
	{
		this.authCode = authCode;
	}
	
	public String getAuthCode()
	{
		return this.authCode;
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
	
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	
	public void setTxnFee(BigDecimal txnFee)
	{
		this.txnFee = txnFee;
	}
	
	public BigDecimal getTxnFee()
	{
		return this.txnFee;
	}
	
	public void setBankCharge(BigDecimal bankCharge)
	{
		this.bankCharge = bankCharge;
	}
	
	public BigDecimal getBankCharge()
	{
		return this.bankCharge;
	}
	
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}
	
	public String getCurrency()
	{
		return this.currency;
	}
	
	public void setProdDesc(String prodDesc)
	{
		this.prodDesc = prodDesc;
	}
	
	public String getProdDesc()
	{
		return this.prodDesc;
	}
	
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	
	public String getUserName()
	{
		return this.userName;
	}
	
	public void setUserEmail(String userEmail)
	{
		this.userEmail = userEmail;
	}
	
	public String getUserEmail()
	{
		return this.userEmail;
	}
	
	public void setUserContact(String userContact)
	{
		this.userContact = userContact;
	}
	
	public String getUserContact()
	{
		return this.userContact;
	}
	
	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}
	
	public String getRemarks()
	{
		return this.remarks;
	}
	
	public void setLang(String lang)
	{
		this.lang = lang;
	}
	
	public String getLang()
	{
		return this.lang;
	}
	
	public void setTxnDate(Timestamp txnDate)
	{
		this.txnDate = txnDate;
	}
	
	public Timestamp getTxnDate()
	{
		return this.txnDate;
	}

	public void setDateFrom(Timestamp txnDate)
	{
		this.dateFrom = txnDate;
	}
	
	public Timestamp getDateFrom()
	{
		return this.dateFrom;
	}
	
	public void setDateTo(Timestamp txnDate)
	{
		this.dateTo = txnDate;
	}
	
	public Timestamp getDateTo()
	{
		return this.dateTo;
	}
	public void setPostDate(Timestamp postDate)
	{
		this.postDate = postDate;
	}
	
	public Timestamp getPostDate()
	{
		return this.postDate;
	}
	
	public void setSignature(String signature)
	{
		this.signature = signature;
	}
	
	public String getSignature()
	{
		return this.signature;
	}
	
	public void setTransId(String transId)
	{
		this.transId = transId;
	}
	
	public String getTransId()
	{
		return this.transId;
	}
	
	public void setCardPaymentConfigId(String cardPaymentConfigId)
	{
		this.cardPaymentConfigId = cardPaymentConfigId;
	}
	
	public String getCardPaymentConfigId()
	{
		return this.cardPaymentConfigId;
	}
	
	public void setUserId(Integer userid)
	{
		this.userid = userid;
	}
	
	public Integer getUserId()
	{
		return this.userid;
	}
	
	public Vector getTransactionList()
	{
		Vector vecTxn = null;
		QueryObject qry = null;
		Timestamp dateToNextDay = TimeFormat.add(this.dateTo,0,0,1);
		if(this.status.equals(RemoteCreditServicesBean.STATUS_ERROR))
		{
			qry = new QueryObject(new String[]{RemoteCreditServicesBean.STATUS + " <> '" + RemoteCreditServicesBean.STATUS_READY + "'",
					RemoteCreditServicesBean.STATUS + " <> '" + RemoteCreditServicesBean.STATUS_COMPLETED + "'",
					RemoteCreditServicesBean.STATUS + " <> '" + RemoteCreditServicesBean.STATUS_FAIL + "'",
					RemoteCreditServicesBean.TIME_CREATED + " >= '"+TimeFormat.strDisplayDate(this.dateFrom)+"' ",
					RemoteCreditServicesBean.TIME_CREATED + " < '"+TimeFormat.strDisplayDate(dateToNextDay)+"' "});			
		}
		else
		{
			qry = new QueryObject(new String[]{RemoteCreditServicesBean.STATUS + " = '" + this.status + "'",RemoteCreditServicesBean.TIME_CREATED + " >= '"+TimeFormat.strDisplayDate(this.dateFrom)+"' ",
					RemoteCreditServicesBean.TIME_CREATED + " < '"+TimeFormat.strDisplayDate(dateToNextDay)+"' "});			
		}
		qry.setOrder(" ORDER BY " + this.orderBy);
		vecTxn = (Vector) RemoteCreditServicesNut.getObjects(qry);	
		this.vecTxn = vecTxn;
		return vecTxn;
	}
	
	public void preProcessingCheck() throws Exception
	{
		String result = "";
		Item itm = ItemNut.getObjectByCode(fixedItemCode);
		ItemObject itmObj = itm.getObject();
		if(itmObj==null)
		{
			throw new Exception("Preset CC-Charge item does not exist! Please create it first according to the given specifications.");
		}
		String strGLCode = "";	
		if(AppConfigManager.getProperty("CUSTOMER-DEBIT-NOTE-GL-CODE-OPTION")!=null)
		{
			strGLCode = AppConfigManager.getProperty("CUSTOMER-DEBIT-NOTE-GL-CODE-OPTION");
		}
		else
		{
			throw new Exception("Customer Debit Note GL Code has not been set.");
		}
		if(strGLCode.equals(""))
		{
			throw new Exception("Customer Debit Note GL Code has not been set.");
		}
		QueryObject query = null;
   		query= new QueryObject(new String[]{GLCodeBean.CODE+" = '" + strGLCode + "'"});
   		Vector vecGLCode = GLCodeNut.getObjects(query);
   		GLCodeObject glObj = null;
   		if(vecGLCode.size()>0)
   		{
   			glObj = (GLCodeObject) vecGLCode.get(0);
   		}
   		if(glObj==null)
   		{
   			throw new Exception("Specified GL Code does not exist.");
   		}
	}
	
	public String checkRecord(RemoteCreditServicesObject rcsObj)
	{
		String result = "";
		
		CustAccountObject custObj = null;
		Vector vecCust = null;
		QueryObject qry = null;

		qry = new QueryObject(new String[]{CustAccountBean.NAME + " = '" + rcsObj.merchantCode + "'"});
		vecCust = (Vector) CustAccountNut.getObjects(qry);
		if(vecCust!=null && vecCust.size()>0)
		{
			custObj = (CustAccountObject) vecCust.get(0);
		}

		if(custObj!=null)
		{
			// DO NOTHING
		}
		else
		{
			Log.printVerbose("Customer Object is null.");
			result = "Customer does not exist.";
		}		
		
/*		if(!rcsObj.merchantCode.trim().equals(rcsObj.merchantName.trim()))
		{
			Log.printVerbose("Merchant Code does not match Merchant Name.");
			result = "Merchant code and merchant name mismatch.";
		}*/
		
		if(!rcsObj.currency.equals(this.defaultCurrencyCode))
		{
			Log.printVerbose("Currency does not match designated currency code: " + this.defaultCurrencyCode);
			result = "Currency code mismatch.";
		}

		if(rcsObj.paymentId.toString().equals(RemoteCreditServicesBean.PID_ALLIANCE_ONLINE_TRANSFER)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_CREDIT_CARD)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_MAYBANK2U)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_AM_BANK)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_RHB)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_HLB_TRANSFER)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_FPX)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_MOBILE_MONEY)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_POSPAY)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_WEBCASH)
				)
		{
			// DO NOTHING
		}
		else
		{
			Log.printVerbose("Payment Id does not match any authorised Ids.");
			result = "Payment Id mismatch.";
		}
		
		BranchObject bchObj = BranchNut.getObject(rcsObj.branchId);
		if(bchObj!=null)
		{
//			 DO NOTHING
		}
		else
		{
			Log.printVerbose("Branch Object is null.");
			result = "Branch does not exist.";
		}

		CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(rcsObj.cardPaymentConfigId);
		if(cpcObj!=null)
		{
			// DO NOTHING
		}
		else
		{
			Log.printVerbose("CardPaymentConfig Object is null.");
			result = "Payment Mode does not exist.";
		}
	
		// Check if TransId previously existed and not posted. If so, edit the record rather than creating a new one.
		qry = new QueryObject(new String[]{RemoteCreditServicesBean.STATUS + " != '" + RemoteCreditServicesBean.STATUS_FAIL+ "'",RemoteCreditServicesBean.STATUS + " != '" + RemoteCreditServicesBean.STATUS_COMPLETED+ "'",RemoteCreditServicesBean.TRANS_ID + " = '"+rcsObj.transId.toString()+"' "});
		Vector vecTransId = (Vector) RemoteCreditServicesNut.getObjects(qry);
		if(vecTransId.size()>0)
		{
			if(vecTransId.size()>1)
			{
				status = RemoteCreditServicesBean.STATUS_SIGNATURE_TOO_MANY_IDENTICAL_TRANSID;
				Log.printVerbose("TOO MANY IDENTICAL TRANSID UNPOSTED");
			}
		}

		
		if(rcsObj.amount.compareTo(new BigDecimal(0))<0)
		{
			Log.printVerbose("Amount is negative.");
			result = "Amount is negative.";		
		}
		
		if(rcsObj.txnFee.compareTo(new BigDecimal(0))<0)
		{
			Log.printVerbose("Txn Fee is negative.");
			result = "Txn Fee is negative.";				
		}
		
		if(rcsObj.bankCharge.compareTo(new BigDecimal(0))<0)
		{
			Log.printVerbose("Bank Charge is negative.");
			result = "Bank Charge is negative.";			
		}
		
/*		String control = "";
		try
		{
			control = SHA1DigiSignControl.getDigitalSignature(signatureSeed + rcsObj.merchantCode + rcsObj.refNo + rcsObj.transId.toString());
		}
		catch(Exception ex)
		{
			
		}
		Log.printVerbose(control);
		Log.printVerbose(signature);
		if(rcsObj.signature.equals(control))
		{
			// DO NOTHING
		}
		else
		{
			Log.printVerbose("The digital signature does not match.");
			result = "The digital signature does not match. This record may have been tampered with during the sending process.";		
		}*/
		return result;
	}
	
	public void deleteRow(Integer pkid)
	{
		RemoteCreditServicesNut.remove(pkid);
		getTransactionList();
	}
	
	public void createTransaction()
	{
		RemoteCreditServicesObject newObj = new RemoteCreditServicesObject();
		String status = RemoteCreditServicesBean.STATUS_READY;
		CustAccountObject custObj = null;
		Vector vecCust = null;
		QueryObject qry = null;

		qry = new QueryObject(new String[]{CustAccountBean.CUSTCODE + " = '" + this.merchantCode + "'"});
		vecCust = (Vector) CustAccountNut.getObjects(qry);
		if(vecCust!=null && vecCust.size()>0)
		{
			custObj = (CustAccountObject) vecCust.get(0);
		}
/*		if(!this.merchantCode.equals(this.merchantName))
		{
			status = RemoteCreditServicesBean.STATUS_CUSTOMER_NAME_CODE_MISMATCH;
		}*/
		if(custObj!=null)
		{
			newObj.merchantCode = this.merchantCode;
			newObj.merchantName = this.merchantName;
		}
		else
		{
			newObj.merchantCode = this.merchantCode;
			newObj.merchantName = this.merchantName;
			Log.printVerbose("CUSTOMER PROBLEM");
			status = RemoteCreditServicesBean.STATUS_CUSTOMER_NULL;
		}
	
/*		if(this.paymentId.toString().equals(RemoteCreditServicesBean.PID_ALLIANCE_ONLINE_TRANSFER)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_CREDIT_CARD)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_MAYBANK2U)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_AM_BANK)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_RHB)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_HLB_TRANSFER)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_FPX)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_MOBILE_MONEY)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_POSPAY)
				|| this.paymentId.toString().equals(RemoteCreditServicesBean.PID_WEBCASH)
				)
		{
			newObj.paymentId = this.paymentId;
		}
		else
		{
			newObj.paymentId = this.paymentId;
			status = RemoteCreditServicesBean.STATUS_PAYMENT_ID_MISMATCH;
		}*/
		if(this.paymentId!=null && !this.paymentId.toString().trim().equals(""))
		{
			newObj.paymentId = this.paymentId;
		}
		else
		{
			newObj.paymentId = this.paymentId;
			status = RemoteCreditServicesBean.STATUS_PAYMENT_ID_MISMATCH;
		}
		
		
		
		newObj.refNo = this.refNo;
		newObj.amount = this.amount;
		newObj.txnFee = this.txnFee;
		newObj.bankCharge = this.bankCharge;
		if(!this.currency.equals(this.defaultCurrencyCode))
		{
			newObj.currency = this.currency;
			status = RemoteCreditServicesBean.STATUS_CURRENCY_MISMATCH;
		}
		else
		{
			newObj.currency = this.currency;
		}
		
		newObj.prodDesc = this.prodDesc;
		newObj.userName = this.userName;
		newObj.userEmail = this.userEmail;
		newObj.userContact = this.userContact;
		newObj.remarks = this.remarks;
		newObj.lang = this.lang;
		//newObj.status = this.status;
		newObj.txnDate = this.txnDate;
		newObj.postDate = this.postDate;
		newObj.authCode = this.authCode;
		// Below is the proposed signature string.
		String control = "";
		try
		{
			control = SHA1DigiSignControl.getDigitalSignature(signatureSeed + this.merchantCode + this.refNo + this.transId);
		}
		catch(Exception ex)
		{
			
		}
		
		Log.printVerbose(control);
		Log.printVerbose(signature);
		if(this.signature.equals(control))
		{
			newObj.signature = this.signature;
		}
		else
		{
			newObj.signature = this.signature;
			status = RemoteCreditServicesBean.STATUS_SIGNATURE_MISMATCH;
		}
		
		newObj.transId = this.transId;	
		BranchObject bchObj = null;
		Vector vecBch = null;
		qry = new QueryObject(new String[]{BranchBean.CODE + " = '" + this.branchCode + "'"});
		vecBch = (Vector) BranchNut.getObjects(qry);
		if(vecBch!=null && vecBch.size()>0)
		{
			bchObj = (BranchObject) vecBch.get(0);
		}
		if(bchObj!=null)
		{
			newObj.branchId = bchObj.pkid;	
		}
		else
		{
			newObj.branchId = new Integer(0);
			status = RemoteCreditServicesBean.STATUS_BRANCH_NULL;
		}
		
		CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObjectByPaymentMode(this.cardPaymentConfigId);
		if(cpcObj!=null)
		{
			newObj.cardPaymentConfigId = cpcObj.pkid;
		}
		else
		{
			newObj.cardPaymentConfigId = new Integer(0);	
			status = RemoteCreditServicesBean.STATUS_CARD_PAYMENT_CONFIG_NULL;
		}
		
		newObj.userid = this.userid;

		if(newObj.amount.compareTo(new BigDecimal(0))<0)
		{
			status = RemoteCreditServicesBean.STATUS_AMOUNT_NEGATIVE;
		}
		
		if(newObj.txnFee.compareTo(new BigDecimal(0))<0)
		{
			status = RemoteCreditServicesBean.STATUS_TXN_FEE_NEGATIVE;			
		}
		
		if(newObj.bankCharge.compareTo(new BigDecimal(0))<0)
		{
			status = RemoteCreditServicesBean.STATUS_BANK_CHARGE_NEGATIVE;			
		}
		
		// Check if TransId previously existed and not posted. If so, edit the record rather than creating a new one.
		qry = new QueryObject(new String[]{RemoteCreditServicesBean.STATUS + " != '" + RemoteCreditServicesBean.STATUS_FAIL+ "'",RemoteCreditServicesBean.STATUS + " != '" + RemoteCreditServicesBean.STATUS_COMPLETED+ "'",RemoteCreditServicesBean.TRANS_ID + " = '"+this.transId+"' "});
		Vector vecTransId = (Vector) RemoteCreditServicesNut.getObjects(qry);
		if(vecTransId.size()>0)
		{
			if(vecTransId.size()>1)
			{
				status = RemoteCreditServicesBean.STATUS_SIGNATURE_TOO_MANY_IDENTICAL_TRANSID;
				Log.printVerbose("TOO MANY IDENTICAL TRANSID UNPOSTED");
			}
			else
			{
				RemoteCreditServicesObject rcsObj = (RemoteCreditServicesObject) vecTransId.get(0);
				newObj.pkid = rcsObj.pkid;
				newObj.status = status;
				this.responseStatus = status;
				try
				{
					RemoteCreditServicesNut.update(newObj);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				
			}
		}
		else
		{
			newObj.status = status;
			RemoteCreditServicesNut.fnCreate(newObj);
		}
		newObj.status = status;
		this.responseStatus = status;
	}
		
	public void processTransaction(Integer pkid) throws Exception
	{

		try
		{
			QueryObject query = null;
			CustSettleDocumentsSession csSes = new CustSettleDocumentsSession(this.userid);
			CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObjectByPaymentMode(this.cardPaymentConfigId);

			
			CreateInvoiceSession ciSes = new CreateInvoiceSession(this.userid);
			Branch bch = BranchNut.getObjectByCode(this.branchCode);
			BranchObject bchObj = bch.getObject();
			if(bchObj!=null)
			{
				ciSes.setBranch(bchObj.pkid);
			}
			CustAccount cust = CustAccountNut.getObjectByCode(this.merchantCode);
			CustAccountObject custObj = cust.getObject();
			if(custObj!=null)
			{
				ciSes.setCustomer(custObj.pkid);
				ciSes.setTermsDay(custObj.creditTerms);
			}
			else
			{
				throw new Exception("This customer code doesn't exist.");
			}
			
			if(cpcObj!=null)
			{
				if(cpcObj!=null)
				{
					if(!cpcObj.cashbookOpt.equals(CardPaymentConfigBean.CB_AUTO_SELECT) && cpcObj.cashbookOpt.equals(CardPaymentConfigBean.CB_NONE))
					{
						cpcObj.cashbook = bchObj.cashbookCard;
					}					
				}

				csSes.setCardPaymentConfig(cpcObj.pkid.toString(),this.bankCharge);
			}
			else
			{
				throw new Exception("This card payment config ID doesn't exist.");
			}
			
			String strGLCode = "";	
			if(AppConfigManager.getProperty("CUSTOMER-DEBIT-NOTE-GL-CODE-OPTION")!=null)
			{
				strGLCode = AppConfigManager.getProperty("CUSTOMER-DEBIT-NOTE-GL-CODE-OPTION");
			}
			else
			{
				throw new Exception("Customer Debit Note GL Code has not been set.");
			}
			if(strGLCode.equals(""))
			{
				throw new Exception("Customer Debit Note GL Code has not been set.");
			}
	   		query= new QueryObject(new String[]{GLCodeBean.CODE+" = '" + strGLCode + "'"});
	   		Vector vecGLCode = GLCodeNut.getObjects(query);
	   		GLCodeObject glObj = null;
	   		if(vecGLCode.size()>0)
	   		{
	   			glObj = (GLCodeObject) vecGLCode.get(0);
	   		}
	   		if(glObj==null)
	   		{
	   			throw new Exception("Specified GL Code does not exist.");
	   		}
			// Phase 1: Create Invoice
			// Add invoice master data
	   		Log.printVerbose("CHECK1.1");
			ciSes.setDate(this.txnDate);
			ciSes.setInvoiceFormat(ciSes.getBranch().formatInvoiceType);
			ciSes.setReferenceNo(this.refNo);
			ciSes.setRemarks(this.remarks);
			UserObject userObj = UserNut.getObject(this.userid);
			// Add preset CC-Charge item
			Item itm = ItemNut.getObjectByCode(fixedItemCode);
			ItemObject itmObj = itm.getObject();
			DocRow docrow = null;
			docrow = new DocRow();
			docrow.setKey2(docrow.getGuid());
			docrow.setItemType(itmObj.itemType1);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(new BigDecimal(1));
			docrow.setAverageCost(new BigDecimal(0));
/*			QueryObject query = new QueryObject(new String[] 
			      { CurrencyBean.CODE + "= '" + this.currency + "' " });
			Vector vecCurr =(Vector) CurrencyNut.getObjects(query);
			if(vecCurr.size()>0)
			{
				CurrencyObject currObj = (CurrencyObject) vecCurr.get(0);
				docrow.setCcy1(currObj.code);
				docrow.setCcy2(currObj.code);
			}*/
			docrow.setCcy1(this.currency);
			docrow.setCcy2(this.currency);
			// TKW20080512 - CHANGED
/*			docrow.setPrice1(this.txnFee.add(this.bankCharge));			
			docrow.setNetPrice(this.txnFee.add(this.bankCharge));	
			docrow.setPrice2(this.txnFee.add(this.bankCharge));*/
			docrow.setPrice1(this.txnFee);			
			docrow.setNetPrice(this.txnFee);	
			docrow.setPrice2(this.txnFee);
			// END TKW20080512
			docrow.user1 = this.userid.intValue();
			docrow.user2 = this.userid.intValue();
			docrow.user3 = this.userid.intValue();
			docrow.setRemarks(this.remarks);			
			
			docrow.setCodeProject(itmObj.codeProject);
			docrow.setCodeDepartment(itmObj.codeDepartment);
			docrow.setCodeDealer(itmObj.codeDealer);
			ciSes.fnAddStockWithItemCode(docrow);
						
			ciSes.setSalesUserId(userObj.userId);
			ciSes.setSalesUsername(userObj.userName);
			
			Long invPkid = ciSes.saveInvoice();		
			Log.printVerbose("CHECK1.2");
			// End Phase 1	
			// Phase 2: CREATE DEBIT NOTE TO COMPANY B USING REFUND-AMT (RM970)
			
			Log.printVerbose("CHECK2.1");
			CreditMemoIndexObject cmObj = new CreditMemoIndexObject();
			cmObj.branch = bchObj.pkid;
			cmObj.pcCenter = bchObj.accPCCenterId;
			cmObj.currency = bchObj.currency;
			cmObj.amount = this.amount.subtract(this.txnFee).negate();
			cmObj.balance = this.amount.subtract(this.txnFee);
			cmObj.balanceBfPdc = this.amount.subtract(this.txnFee);

			cmObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;
			cmObj.memoType = CreditMemoIndexBean.MEMO_TYPE_CUSTOMER_DEBIT_NOTE;

			cmObj.memoRemarks = this.remarks;
			cmObj.docReference = this.refNo;
			cmObj.entityTable = CustAccountBean.TABLENAME;
			cmObj.entityKey = custObj.pkid;
			cmObj.entityName = custObj.name;
			cmObj.entityPic = custObj.getName();
			cmObj.userIdCreate = this.userid;
			cmObj.userIdApprove = this.userid;
			cmObj.userIdUpdate = this.userid;
			cmObj.timeCreate = this.txnDate; // / this is the statement date
			cmObj.timeApprove = this.txnDate;
			cmObj.timeUpdate = this.txnDate;

			CreditMemoItemObject cmiObj = new CreditMemoItemObject();

			cmiObj.glCodeCredit = strGLCode;
			cmiObj.glCodeDebit = GLCodeBean.ACC_RECEIVABLE;

			cmiObj.remarks = this.remarks;
			cmiObj.amount = this.amount.subtract(this.txnFee).negate();

			cmiObj.pcCenter = cmObj.pcCenter;
			cmiObj.branch = cmObj.branch;

			cmiObj.currency = bchObj.currency;

			cmiObj.entityTable = cmObj.entityTable;
			cmiObj.entityKey = cmObj.entityKey;
			cmiObj.entityName = cmObj.entityName;
			cmiObj.dateStmt = cmObj.timeCreate;
			cmiObj.dateItem = cmObj.timeCreate;
			cmiObj.userIdPIC = this.userid;
			cmObj.vecItems.add(cmiObj);

			CreditMemoIndex cmEJB1 = CreditMemoIndexNut.fnCreate(cmObj);
			Long cmPkid = cmEJB1.getPkid();
			Log.printVerbose("CHECK2.2");
			// End Phase 2
//			 Phase 3: CREATE OFFICIAL OPEN CREDIT RECEIPT TO COMPANY B USING SALES-AMT (RM1000) TO KNOCK OFF DEBIT NOTE CREATED IN PREVIOUS STEP
//			(IF FINANCE CHARGE IS RM0, THEN “BANK” GL ACCOUNT SHOULD BE DEBITED WITH RM1000)
			Log.printVerbose("CHECK3.1");
			csSes.setBranch(bchObj.pkid);
			csSes.setBranchFilter(true);
			csSes.setCustomer(custObj.pkid);
			csSes.setDate(this.txnDate);
			csSes.setRemarks(this.remarks);
			



			csSes.setCCDetails("", cpcObj.defaultCardType, "", "", "", TimeFormat.getTimestamp(), "");
			csSes.setAmountCard(this.amount);
			csSes.setCardCashbook(cpcObj.cashbook);
			csSes.setIsRemoteCreditTransaction(true);
			csSes.setOpenBalance(this.amount);
			csSes.getCheckReceipt();
			Long rctPkid = csSes.confirmAndSave();
			Log.printVerbose("CHECK3.2");
			// End Phase 3
			// Phase 4: CREATE CREDIT NOTE TO COMPANY B USING REFUND-AMT (RM970 / CHOOSING CREDIT CARD AS METHOD OF PAYMENT)
			Log.printVerbose("CHECK4.1");
			cmObj = new CreditMemoIndexObject();
			cmObj.branch = bchObj.pkid;
			cmObj.pcCenter = bchObj.accPCCenterId;
			cmObj.currency = bchObj.currency;
			cmObj.amount = this.amount.subtract(this.txnFee);
			cmObj.balance = this.amount.subtract(this.txnFee).negate();
			cmObj.balanceBfPdc = this.amount.subtract(this.txnFee).negate();

			cmObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE;
			cmObj.memoType = CreditMemoIndexBean.MEMO_TYPE_CUSTOMER_CREDIT_NOTE;

			cmObj.memoRemarks = this.remarks;
			cmObj.docReference = this.refNo;
			cmObj.entityTable = CustAccountBean.TABLENAME;
			cmObj.entityKey = custObj.pkid;
			cmObj.entityName = custObj.name;
			cmObj.entityPic = custObj.getName();
			cmObj.userIdCreate = this.userid;
			cmObj.userIdApprove = this.userid;
			cmObj.userIdUpdate = this.userid;
			cmObj.timeCreate = this.txnDate; // / this is the statement date
			cmObj.timeApprove = this.txnDate;
			cmObj.timeUpdate = this.txnDate;

			cmiObj = new CreditMemoItemObject();

			cmiObj.glCodeDebit = strGLCode;
			cmiObj.glCodeCredit = GLCodeBean.ACC_RECEIVABLE;

			cmiObj.remarks = this.remarks;
			cmiObj.amount = this.amount.subtract(this.txnFee);

			cmiObj.pcCenter = cmObj.pcCenter;
			cmiObj.branch = cmObj.branch;

			cmiObj.currency = bchObj.currency;

			cmiObj.entityTable = cmObj.entityTable;
			cmiObj.entityKey = cmObj.entityKey;
			cmiObj.entityName = cmObj.entityName;
			cmiObj.dateStmt = cmObj.timeCreate;
			cmiObj.dateItem = cmObj.timeCreate;
			cmiObj.userIdPIC = this.userid;
			cmObj.vecItems.add(cmiObj);

			CreditMemoIndex cmEJB2 = CreditMemoIndexNut.fnCreate(cmObj);		
			Log.printVerbose("CHECK4.2");
			// End Phase 4
			// Phase 5: SETTLE THE INVOICE AND CREDIT MEMO WITH OPEN RECEIPT
			Log.printVerbose("CHECK5.1");
			OfficialReceiptObject rctObj = OfficialReceiptNut.getObject(rctPkid);
			InvoiceObject invObj = InvoiceNut.getObject(invPkid);
			CreditMemoIndexObject cmkObj = CreditMemoIndexNut.getObject(cmPkid);
			rctObj.openBalance = rctObj.openBalance.subtract(cmkObj.amount.add(invObj.mTotalAmt.subtract(this.bankCharge)));// had to remove bank charge or total CM + inv amount would be higher than open receipt. may change.
			OfficialReceipt orEJB = OfficialReceiptNut.getHandle(rctObj.pkid);
			try
			{
				orEJB.setObject(rctObj);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			// Invoice knockoff
			Invoice thisInvEJB = InvoiceNut.getHandle(invPkid);

	         AuditTrailObject atObj = new AuditTrailObject();
	         atObj.userId = this.userid;
	         atObj.auditType = AuditTrailBean.TYPE_TXN;
	         atObj.remarks = "customer: deposit-settlement " + "RCT" + rctObj.pkid.toString()+ " INV" + invPkid.toString()+" AMOUNT:"+CurrencyFormat.strCcy(invObj.mTotalAmt.subtract(this.bankCharge));
	         AuditTrailNut.fnCreate(atObj);

						
						thisInvEJB.adjustOutstanding(invObj.mTotalAmt.subtract(this.bankCharge).negate());
						BigDecimal addBackPdc = rctObj.amountPDCheque.multiply(
								invObj.mTotalAmt.subtract(this.bankCharge).divide(cmkObj.amount.add(invObj.mTotalAmt.subtract(this.bankCharge)), 12, BigDecimal.ROUND_HALF_EVEN));
						thisInvEJB.adjustOutstandingBfPdc(invObj.mTotalAmt.subtract(this.bankCharge).add(addBackPdc));
						// Create the DocLink
						DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", 
									DocLinkBean.RELTYPE_PYMT_INV, OfficialReceiptBean.TABLENAME, 
									rctObj.pkid, InvoiceBean.TABLENAME, invPkid, 
									rctObj.currency, invObj.mTotalAmt.subtract(this.bankCharge).negate(), // reduces
									rctObj.currency, invObj.mTotalAmt.subtract(this.bankCharge).negate(), // reduces
									"", rctObj.paymentTime, this.userid);

						// CM knockoff
						CreditMemoIndex cmEJB = CreditMemoIndexNut.getHandle(cmPkid);
				         atObj = new AuditTrailObject();
				         atObj.userId = this.userid;
				         atObj.auditType = AuditTrailBean.TYPE_TXN;
				         atObj.remarks = "customer: deposit-settlement " + "RCT" + rctObj.pkid.toString()+ " CM" + cmPkid.toString()+" AMOUNT:"+CurrencyFormat.strCcy(cmkObj.amount);
				         AuditTrailNut.fnCreate(atObj);
									cmEJB.adjustBalance(cmkObj.amount.negate());
									addBackPdc = rctObj.amountPDCheque.multiply(cmkObj.amount.divide(
											cmkObj.amount.add(invObj.mTotalAmt.subtract(this.bankCharge)), 12, BigDecimal.ROUND_HALF_EVEN));
									cmEJB.adjustBalanceBfPdc(cmkObj.amount.negate().add(addBackPdc));
									newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", 
												DocLinkBean.RELTYPE_PYMT_DN, OfficialReceiptBean.TABLENAME, rctObj.pkid, 
												CreditMemoIndexBean.TABLENAME, cmPkid, rctObj.currency, 
												cmkObj.amount.negate(), // reduces
												rctObj.currency, cmkObj.amount.negate(), // reduces
												"", rctObj.paymentTime, this.userid);
			Log.printVerbose("CHECK5.2");
			// End Phase 5
			RemoteCreditServicesObject rcsObj = RemoteCreditServicesNut.getObject(pkid);
			rcsObj.status = RemoteCreditServicesBean.STATUS_COMPLETED;
			rcsObj.postDate = TimeFormat.getTimestamp();
			RemoteCreditServicesNut.update(rcsObj);
			Log.printVerbose("CHECK6");
		}
		catch(Exception ex)
		{
			Log.printVerbose("CHECK7");
			ex.printStackTrace();
			RemoteCreditServicesObject rcsObj = RemoteCreditServicesNut.getObject(pkid);
			rcsObj.status = RemoteCreditServicesBean.STATUS_FAIL;
			rcsObj.postDate = TimeFormat.getTimestamp();
			RemoteCreditServicesNut.update(rcsObj);			
			throw ex;
		}

	}
	
}
