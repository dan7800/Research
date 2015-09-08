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
import com.vlee.bean.procurement.*;
import java.util.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;
import com.vlee.bean.customer.*;
import com.vlee.bean.customer.CustDepositSettlementForm.Document;

public class CreateInvoiceSession extends java.lang.Object implements Serializable
{
	private static final long serialVersionUID = 0;
	public InvoiceObject invoiceObj;
	public Vector prevInvoices = new Vector();
	public static final String STATE_DRAFT = "draft";
	public static final String SAVED = "saved";
	private boolean saving = false;
	private String state;
	private BranchObject branchObj;
	private CustAccountObject customer = null;
	private CustUserObject contact = null;
	private boolean bCustSvcCtr = false;
	private String referenceNo = "";
	private String remarks = "";
	private Integer userId;
	private String salesUsername;
	private Integer salesUserId;
	private Integer termsDay;
	private Long srvNo;
	protected TreeMap tableRows;
	private Timestamp tsDate = null;
	private boolean bSetDate = false;
	public CustCreditControlChecker creditChecker = null;
	
	private Long cpiiPkid = new Long(0);
	private String linkFromProInv = "";

	private Invoice mInvoiceEJBTemp;
	private String name = "";
	public String policyNumber="";
	public Timestamp accidentDate;
	public BigDecimal claimAmount;
	
	//[[JOB-JOE
	public Long jsPkid = new Long(0);
	public String linkFromJobsheet = "";
	private TreeMap treeJobsheet;
	private String isMakeGRN;
	public String foreignText;
	public String invoiceFormat;
	
	//20080121 Jimmy
	public OfficialReceiptObject receiptObj;
	public BigDecimal receiptPayment;
	
	
	// JANET
	public void setCpiiPkid(Long cpiiPkid)
	{
		System.out.println("Inside Create Invoice Form :  cpiiPkid = "+cpiiPkid.toString());
		
		this.cpiiPkid = cpiiPkid;
		
		System.out.println("this.cpiiPkid = "+this.cpiiPkid.toString());
	}	
	
	public String getInvoiceFormat()
	{
		return this.invoiceFormat;
	}
	
	public void setInvoiceFormat(String invoiceFormat)
	{
		this.invoiceFormat = invoiceFormat;
	}
	
	public void setIsMakeGRN(String res)
	{
		isMakeGRN = res;
	}
	public Long getCpiiPkid()
	{
		return this.cpiiPkid;
	}	
	public void setLinkFromProInv(String linkFromProInv)
	{
		this.linkFromProInv = linkFromProInv;
	}
	public String getLinkFromProInv()
	{
		return this.linkFromProInv;
	}
	// END JANET
	
	// TKW20070509: Checks inventory qty before creating an invoice. Enough said.
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

	public void addStockWithItemCode(DocRow docr) throws Exception
	{
		//docr.setUser1(this.salesUserId.intValue());
		// / check if this row exists in the list already
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow dcrw = (DocRow) vecDocRow.get(cnt);
			if(dcrw.getKey2().equals(docr.getKey2()))
			{
				this.tableRows.remove(dcrw.getKey());
			}
		}
		// / add it
		try
		{
			/// append it to the TreeMap
			this.tableRows.put(docr.getKey(), docr);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	// TKW20070416: Added to delete package header and its children.
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

	public void setTermsDay(Integer terms)
	{
		this.termsDay = terms;
	}
	
	public void setSrvNo(Long srvNo)
	{
		this.srvNo = srvNo;
	}
	
	public Long getSrvNo()
	{
		return this.srvNo;
	}

	public Integer getTermsDay()
	{
		return this.termsDay;
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

	public boolean setBranch(Integer iBranch)
	{
		reset();
		this.branchObj = BranchNut.getObject(iBranch);
		if (this.branchObj != null)
		{
			this.bCustSvcCtr = true;
			return true;
		} else
		{
			this.bCustSvcCtr = false;
			return false;
		}
	}

	public BranchObject getBranch()
	{
		return this.branchObj;
	}

	public Integer getBranchId()
	{ return this.branchObj.pkid;}

	public CustCreditControlChecker getCreditChecker()
	{
		return this.creditChecker;
	}

   public String getCustomerId()
   {
      if(this.customer==null)
      { return "";}
      else
      { return this.customer.pkid.toString();}
   }

	public CustAccountObject getCustomer()
	{
		return this.customer;
	}

   public boolean setCustomer(Integer iCustomer)
   {
      this.customer = CustAccountNut.getObject(iCustomer);
      // 20080122 Jimmy - change another customer, clear previous Receipt Session.
      resetReceipt();
      // End
      if (this.customer != null)
      {
         this.termsDay = this.customer.creditTerms;
                  
         if(this.customer.salesman.intValue()>0)
         {        	 
            try
            {setSalesUserId(this.customer.salesman);}
            catch(Exception ex){};
         }

         this.creditChecker = new CustCreditControlChecker();
         this.creditChecker.setAccount(this.customer);
         this.creditChecker.generateReport();

         return true;
      } else
      {
         return false;
      }
   }

   public void setCustomer(CustAccountObject custObj)
   {
      this.contact = null;
      this.customer = custObj;
      this.termsDay = this.customer.creditTerms;

      // 20080122 Jimmy - change another customer, clear previous Receipt Session.
      resetReceipt();
      // End

      if(this.customer.salesman.intValue()>0)
      {
         try{setSalesUserId(this.customer.salesman);}
         catch(Exception ex){}
         this.creditChecker = new CustCreditControlChecker();
         this.creditChecker.setAccount(this.customer);
         this.creditChecker.generateReport();
      }
      else
     {
    	  try{setSalesUserId(this.userId);}
    	  catch(Exception ex){}
     }
      
      this.name = this.customer.name;
   }

   public void setCustUser(CustUserObject custUserObj)
   {
      this.customer = CustAccountNut.getObject(custUserObj.accId);
      if(this.customer==null){ return ;}
      setCustomer(this.customer);
      this.termsDay = this.customer.creditTerms;
      if(custUserObj.salesman.intValue()>0)
      {
         try{setSalesUserId(custUserObj.salesman);}
         catch(Exception ex){}
      }
      this.contact = custUserObj;
      this.name = custUserObj.getName();	
   }

   public CustUserObject getContact()
   {
      return this.contact;
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

	public Integer getSalesUserId()
	{
		return this.salesUserId;
	}

	public String getSalesUsername()
	{
		return this.salesUsername;
	}

	public void setSalesUserId(Integer userId) throws Exception
	{
		UserObject userObj = UserNut.getObject(userId);
		this.salesUsername = userObj.userName;
		this.salesUserId = userId;
		System.out.println("this.salesUserId : "+this.salesUserId.toString());
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			docrow.setUser1(this.salesUserId.intValue());
		}
	}

	public Long saveInvoice()
	{
		// Populate Defaults
		if (bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		// this.invoiceObj.mSalesTxnId = // automatically created when default
		// is zero
		// this.invoiceObj.mPaymentTermsId = pmtTerm;
		// this.invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		
		if(!"".equals(this.branchObj.formatInvoiceType))
		{			
			this.invoiceObj.mDisplayFormat = this.branchObj.formatInvoiceType;
			
			System.out.println("this.invoiceObj.mDisplayFormat : "+this.invoiceObj.mDisplayFormat);
		}
		else
		{
			this.invoiceObj.mDisplayFormat = InvoiceBean.INVOICE_TRADING_1;
			
			System.out.println("this.invoiceObj.mDisplayFormat : "+this.invoiceObj.mDisplayFormat);
		}	
		this.invoiceObj.mTimeIssued = this.tsDate;
		this.invoiceObj.mCurrency = this.branchObj.currency;
		this.invoiceObj.mTotalAmt = getTotalAmt();
		this.invoiceObj.mOutstandingAmt = getTotalAmt();
		this.invoiceObj.mOutstandingBfPdc = getTotalAmt();
		this.invoiceObj.taxAmount = getTotalTax(); // 20080314 Jimmy
		this.invoiceObj.mRemarks = this.remarks;
		// this.invoiceObj.mState = InvoiceBean.ST_CREATED; // 10
		// this.invoiceObj.mStatus = InvoiceBean.STATUS_ACTIVE;
		this.invoiceObj.mLastUpdate = TimeFormat.getTimestamp();
		this.invoiceObj.mUserIdUpdate = this.userId;
		this.invoiceObj.mEntityTable = CustAccountBean.TABLENAME;
		this.invoiceObj.mEntityKey = this.customer.pkid;
		this.invoiceObj.mEntityName = this.customer.name;
		this.invoiceObj.policyNumber = this.policyNumber;
		this.invoiceObj.claimAmount = this.claimAmount;
		this.invoiceObj.accidentDate = this.accidentDate;
		if (this.name.length() > 3)
		{
			this.invoiceObj.mEntityName = this.name;
		}
		if(isMakeGRN.equals("yes"))
		{
			this.invoiceObj.subtype1 = InvoiceBean.SUBTYPE_INTERCOMPANY_STOCK_TRANSFER;
		}
		// this.invoiceObj.mEntityType = "";
		this.invoiceObj.mIdentityNumber = this.customer.identityNumber;
		this.invoiceObj.mEntityContactPerson = this.customer.getName();
		this.invoiceObj.mForeignTable = ""; // 20
		this.invoiceObj.mForeignKey = new Integer(0);
		this.invoiceObj.mForeignText = this.foreignText;
		this.invoiceObj.mCustSvcCtrId = this.branchObj.pkid;
		this.invoiceObj.mLocationId = this.branchObj.invLocationId;
		this.invoiceObj.mPCCenter = this.branchObj.accPCCenterId;
		// this.invoiceObj.mTxnType = "";
		// this.invoiceObj.mStmtType = "";
		this.invoiceObj.mReferenceNo = this.referenceNo;
		this.invoiceObj.mDescription = "";
		this.invoiceObj.mWorkOrder = this.jsPkid; // 30
		this.invoiceObj.mDeliveryOrder = new Long(0);
		this.invoiceObj.mReceiptId = new Long(0);		
		this.invoiceObj.mDocType = InvoiceBean.INVOICE;
		this.invoiceObj.terms1Date = TimeFormat.add(tsDate, 0, 0, this.termsDay.intValue());
		this.invoiceObj.salesId= this.salesUserId;
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
      if(this.contact!=null)
      {
         this.invoiceObj.mForeignTable = CustUserBean.TABLENAME;
         this.invoiceObj.mForeignKey = this.contact.pkid;
         this.invoiceObj.receiverName = this.contact.getName();
         this.invoiceObj.receiverHandphone = this.contact.mobilePhone;
         this.invoiceObj.receiverPhone1 = this.contact.telephone1;
         this.invoiceObj.receiverPhone2 = this.contact.telephone2;
         this.invoiceObj.receiverFax = this.contact.faxNo;
         this.invoiceObj.receiverEmail = this.contact.email1;
         this.invoiceObj.receiverCompanyName = "";
         this.invoiceObj.receiverAdd1 = this.contact.mainAddress1;
         this.invoiceObj.receiverAdd2 = this.contact.mainAddress2;
         this.invoiceObj.receiverAdd3 = this.contact.mainAddress3;
         this.invoiceObj.receiverCity = this.contact.mainCity;
         this.invoiceObj.receiverZip = this.contact.mainPostcode;
         this.invoiceObj.receiverState = this.contact.mainState;
         this.invoiceObj.receiverCountry = this.contact.mainCountry;
      }
		Invoice invoiceEJB = InvoiceNut.fnCreate(this.invoiceObj);
		mInvoiceEJBTemp = invoiceEJB;
		// / create invoice items
		Vector vecDocRow = new Vector(this.tableRows.values());
		Vector vecInvoiceItemEJB = new Vector();
		try
		{
			for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt1);
				InvoiceItemObject iiObj = new InvoiceItemObject(this.invoiceObj, docrow);
				InvoiceItem iiEJB = InvoiceItemNut.fnCreate(iiObj);
				vecInvoiceItemEJB.add(iiEJB);
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
				
				
				// TKW20070912: Originally personInCharge was set to iiObj.mPic2, but
				// Janet wanted it changed to this.invoiceObj.salesId
				StockNut.sell(this.invoiceObj.salesId, // Integer personInCharge,
						iiObj.mItemId,// Integer itemId,
						this.invoiceObj.mLocationId, this.invoiceObj.mPCCenter, iiObj.mTotalQty,
						iiObj.mUnitPriceQuoted, iiObj.mCurrency, InvoiceItemBean.TABLENAME, iiObj.mPkid, "", // remarks
						// this.invoiceObj.mLastUpdate,
						this.invoiceObj.mTimeIssued, this.invoiceObj.mUserIdUpdate, new Vector(iiObj.colSerialObj),
						iiObj.codeProject,iiObj.codeDepartment, iiObj.codeDealer, iiObj.codeSalesman, 
						this.invoiceObj.mCustSvcCtrId, iiObj.mTaxAmt);
			}
			JournalTxnLogic.fnCreate(this.invoiceObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			// roll back the transactions
			try
			{
				for (int cnt2 = 0; cnt2 < vecInvoiceItemEJB.size(); cnt2++)
				{
					InvoiceItem iiEJB = (InvoiceItem) vecInvoiceItemEJB.get(cnt2);
					iiEJB.remove();
				}
				invoiceEJB.remove();
			} catch (Exception ex2)
			{
				ex.printStackTrace();
			}
		}
		// / recalculate Invoice Amount
		/*
		 * try { InvoiceNut.recalcInvAmt(this.invoiceObj); } catch(Exception ex) {
		 * ex.printStackTrace(); }
		 */
		 
		 //[[JOB-JOE
		 	this.treeJobsheet.clear();
			for (int cnt1 = 0; cnt1 < vecDocRow.size(); cnt1++)
			{
				DocRow docRow = (DocRow) vecDocRow.get(cnt1);
				if(docRow.getJobsheetItem()!=null)
				{ this.treeJobsheet.put(docRow.getJobsheetItem().indexId, docRow.getJobsheetItem().indexId); }
			}
			Vector vecJobsheetId = new Vector(this.treeJobsheet.values());
			for(int cnt1=0;cnt1< vecJobsheetId.size();cnt1++)
			{
				try
				{
					Long jsId = (Long) vecJobsheetId.get(cnt1);
					JobsheetIndex jsEJB = JobsheetIndexNut.getHandle(jsId);
					jsEJB.setState(JobsheetIndexBean.STATE_INVOICE);
	
					DocLinkObject dlObj = new DocLinkObject();
					dlObj.namespace = DocLinkBean.NS_CUSTOMER;
					// dlObj.reference = "";
					dlObj.relType = DocLinkBean.RELTYPE_JS_TO_INVOICE;
					dlObj.srcDocRef = InvoiceBean.TABLENAME;
					dlObj.srcDocId = this.invoiceObj.mPkid;
					dlObj.tgtDocRef = JobsheetIndexBean.TABLENAME;
					dlObj.tgtDocId = jsId;
					dlObj.currency = this.invoiceObj.mCurrency;
					dlObj.amount = new BigDecimal(0);
					// dlObj.currency2 = "";
					// dlObj.amount2 = new BigDecimal("0.00");
					// dlObj.remarks = "";
					dlObj.status = DocLinkBean.STATUS_ACTIVE;
					dlObj.lastUpdate = TimeFormat.getTimestamp();
					dlObj.userIdUpdate = this.userId;
					DocLinkNut.fnCreate(dlObj);
				}
				catch(Exception ex)
				{
					System.out.println("Exception : "+ex.toString());
					ex.printStackTrace();
				}
			}
		 	
		 
		 //JOB-JOE]]
		 return this.invoiceObj.mPkid;
	}

	// / contructor!
	public CreateInvoiceSession(Integer iUser)
	{
		this.invoiceObj = new InvoiceObject();
		this.state = STATE_DRAFT;
		this.bCustSvcCtr = false;
		this.customer = null;
		this.tableRows = new TreeMap();
		this.userId = iUser;
		this.tsDate = TimeFormat.getTimestamp();
		this.salesUsername = UserNut.getUserName(iUser);
		this.salesUserId = iUser;
		UserObject userObj = UserNut.getObject(this.userId);
		this.salesUsername = userObj.userName;
		this.termsDay = new Integer(30);
		this.srvNo = new Long(0);
		this.treeJobsheet = new TreeMap(); //JOB-JOE
		this.isMakeGRN = "no";
		this.policyNumber="";
		this.claimAmount = new BigDecimal(0);
		this.accidentDate = TimeFormat.getTimestamp();
		this.foreignText = "";
	}

	public void reset()
	{
		this.state = STATE_DRAFT;
		bCustSvcCtr = false;
		this.tableRows.clear();
		this.invoiceObj = new InvoiceObject();
		this.remarks = "";
		this.termsDay = new Integer(30);
		//[[JOB-JOE
		this.treeJobsheet.clear(); 
		this.jsPkid = new Long(0);
		this.linkFromJobsheet = "";
		this.treeJobsheet = new TreeMap();
		this.referenceNo = "";	
		this.customer = null;
		this.name="";
		this.salesUsername = UserNut.getUserName(userId);
		this.salesUserId = userId;
		UserObject userObj = UserNut.getObject(this.userId);
		this.salesUsername = userObj.userName;
		this.policyNumber="";
		this.claimAmount = new BigDecimal(0);
		this.accidentDate = TimeFormat.getTimestamp();
		this.foreignText = "";
		//JOB-JOE]]
		
		//20080122 Jimmy
		this.resetReceipt();
		this.creditChecker = null;
	}

	public synchronized void confirmAndSave() throws Exception
	{
		if (saving == true)
			return;
		saving = true;
		if (!canSave())
			throw new Exception("In complete form!");
		// 1) create the PurchaseOrder and GRN
		saveInvoice();
		if("true".equals(this.linkFromProInv))
		{
			updateProformaInvoice();
		}	
		transferExistingDocToPrevBuffer();
		
		
		updateSerialNumberIndex();
		if(this.getSrvNo().intValue() != 0)
		{
			System.out.println("Going to update RMA Ticket");
			updateRMATicket();
		}
		saving = false;
		// TKW20070806: If isMakeGRN = 'yes', auto-generated a GRN.
		if(isMakeGRN.equals("yes"))
		{
			ReceiveStockSession rsObj = new ReceiveStockSession(userId);
			InterCompanyLinkObject obj = InterCompanyLinkNut.getObjectByCustomerId(this.customer.pkid);
			rsObj.setBranch(obj.branchId);
			InvoiceObject invObj = mInvoiceEJBTemp.getObject();
			rsObj.setReferenceNo(invObj.mPkid.toString());
			rsObj.setSupplier(obj.supplierId);		
		    Vector vecDocRow = new Vector(this.tableRows.values());
			
			for(int i = 0;i < this.tableRows.size(); i++)
			{
				DocRow dcrw = (DocRow) vecDocRow.get(i);	
				dcrw.setPrice1(dcrw.getNetPrice());
				rsObj.fnAddStockWithItemCode(dcrw);
			}
			rsObj.confirmAndSave();			
		}
		

		//20080122 Jimmy - for deposit settlement
		knockOffDocuments();
		
		System.out.println("about to reset the invoice page");		
		reset();
	}

	private void updateRMATicket()
	{
		if(!"0".equals(this.srvNo.toString()))
		{								
			RMATicketObject rmaObj = RMATicketNut.getObject(this.srvNo);
			RMATicket rmaEJB = RMATicketNut.getHandle(this.srvNo);
		
			// Update Invoice			
			if(!"0".equals(this.invoiceObj.mPkid.toString()))
			{
				InvoiceObject invObj = InvoiceNut.getObject(this.invoiceObj.mPkid);
				Invoice invEJB = InvoiceNut.getHandle(this.invoiceObj.mPkid);
				
				String invRemarks = invObj.mRemarks;
				invRemarks += " (Itm:"+rmaObj.itemCode+" Problem:"+rmaObj.problemStmt+" Rmk:"+rmaObj.remarks+")";
				
				invObj.mRemarks = StringManup.truncate(invRemarks, 800);
				
				System.out.println("INV remarks : "+invObj.mRemarks);
				System.out.println("remarks : "+invRemarks);				
				
				try
				{
					invEJB.setObject(invObj);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}				
			}
			
			// Update RMA
			String rmaReference = "";
			
			if(!"0".equals(this.invoiceObj.mPkid.toString()))
			{
				rmaReference = this.invoiceObj.mPkid.toString();
				//rmaObj.status = RMATicketBean.STATUS_BILLED;
			}			
			
			rmaObj.supplierReference = StringManup.truncate(rmaReference, 50);
			
			System.out.println("RMA Inv : "+rmaObj.supplierReference);
			System.out.println("Reference : "+rmaReference);				
			
			try
			{
				rmaEJB.setObject(rmaObj);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}		
			
			this.srvNo = new Long(0);
		}				
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
						obj.status = SerialNumberIndexBean.INVOICE;
						obj.useridEdit = this.userId;
						obj.timeEdit = TimeFormat.getTimestamp();
						serialnumber.setObject(obj);
					} catch (Exception ex)
					{
						Log.printDebug("No SerialNumberIndex Found");
					}
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void transferExistingDocToPrevBuffer()
	{
		this.prevInvoices.add(this.invoiceObj);
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
		if (this.branchObj == null)
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
		
		/// check if customer is black listed
		if(this.customer!=null && CustAccountBean.STATE_BL.equals(this.customer.state))
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
			// TKW20070913: TO ensure taxes are taken into account when creating invoice.
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

	//[[JOB-JOE
	public void clearTableRows()
	{
		this.tableRows = new TreeMap();
	}
	//JOB-JOE]]
	
	public void fnAddStockWithItemCode(DocRow docr) throws Exception
	{
		//docr.setUser1(this.salesUserId.intValue());
		docr.setIncludeCodeInKey(false);

      // / check if this row exists in the list already
      Vector vecDocRow = new Vector(this.tableRows.values());
      for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
      {
         DocRow dcrw = (DocRow) vecDocRow.get(cnt);
         if(dcrw.getKey2().equals(docr.getKey2()))
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

	public Vector getPreviousInvoices()
	{
		return this.prevInvoices;
	}
	
	public void setName(String newName)
	{
		this.name = newName;
	}

	public String getName()
	{
		return this.name;
	}
	
	//20080121 Jimmy
	public OfficialReceiptObject getReceipt()
	{ return this.receiptObj;}

	public void setReceipt(Long pkid)
	{
		this.receiptObj = OfficialReceiptNut.getObject(pkid);
	}
	
	public BigDecimal getReceiptPayment()
	{ return this.receiptPayment; }
	
	public void setReceiptPayemnt(BigDecimal receiptPayment)
	{
		this.receiptPayment = receiptPayment;
	}
	
	public void resetReceipt()
	{
		this.receiptObj = null;
		this.receiptPayment = new BigDecimal(0);
	}
	
	
	public synchronized void knockOffDocuments()
	{
		if(this.receiptObj==null){ return;}

		//Update open balance of receipt
		Log.printVerbose("Update open balance of receipt");
		this.receiptObj.openBalance = this.receiptObj.openBalance.subtract(this.receiptPayment);
		OfficialReceipt orEJB = OfficialReceiptNut.getHandle(this.receiptObj.pkid);
		try
		{
			orEJB.setObject(this.receiptObj);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		// find the latest invoice
	    InvoiceObject invoiceObj = (InvoiceObject) this.prevInvoices.get(this.prevInvoices.size()-1);
		
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
	         atObj.remarks = "customer: deposit-settlement " + "RCT" + this.receiptObj.pkid.toString()+ " INV" + invoiceId.toString()+" AMOUNT:"+CurrencyFormat.strCcy(this.getReceiptPayment());
	         AuditTrailNut.fnCreate(atObj);

	         //Adjust outstanding amount of Invoice
	         Log.printVerbose("Adjust outstanding amount of Invocie: " + invoiceId);
	         thisInvEJB.adjustOutstanding(this.getReceiptPayment().negate());
	         BigDecimal addBackPdc = this.receiptObj.amountPDCheque.multiply(
	        		 this.getReceiptPayment().divide(this.receiptPayment, 12, BigDecimal.ROUND_HALF_EVEN));
	         thisInvEJB.adjustOutstandingBfPdc(this.getReceiptPayment().negate().add(addBackPdc));
	         
	         // Create the DocLink
	         Log.printVerbose("Create the DocLink");
	         DocLink newDocLink = DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", 
						DocLinkBean.RELTYPE_PYMT_INV, OfficialReceiptBean.TABLENAME, 
						this.receiptObj.pkid, InvoiceBean.TABLENAME, invoiceId, 
						this.receiptObj.currency, this.getReceiptPayment().negate(), // reduces
						this.receiptObj.currency, this.getReceiptPayment().negate(), // reduces
						"", this.receiptObj.paymentTime, this.userId);
	        
			// reduce the outstanding amount for the sales order too
			// if exists
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandleByInvoice(invoiceId);
			if (soEJB != null)
			{
				Log.printVerbose("Reduce the outstanding amount for the sales order");
				SalesOrderIndexObject soObj = soEJB.getObject();
				soEJB.adjustOutstanding(this.getReceiptPayment().negate());
				DocLinkNut.getHome().create(DocLinkBean.NS_CUSTOMER, "", DocLinkBean.RELTYPE_PYMT_SO, 
					OfficialReceiptBean.TABLENAME, this.receiptObj.pkid, SalesOrderIndexBean.TABLENAME, 
					soObj.pkid, this.receiptObj.currency, this.getReceiptPayment().negate(), // reduces
					this.receiptObj.currency, this.getReceiptPayment().negate(), // reduces
					"", this.receiptObj.paymentTime, this.userId);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	// End
	
	private void updateProformaInvoice()
	{
		try
		{
			System.out.println("Updating Proforma Invoice");
			
			CustProformaInvoiceIndexObject cpiiObj = CustProformaInvoiceIndexNut.getObject(this.cpiiPkid);
			CustProformaInvoiceIndex cpiiEJB = CustProformaInvoiceIndexNut.getHandle(this.cpiiPkid);
									
			cpiiObj.statusInvoice = CustProformaInvoiceIndexBean.STATUS_INVOICED;
			cpiiObj.idInvoice = this.invoiceObj.mPkid;
			
			System.out.println("Newly created Invoice Id = "+this.invoiceObj.mPkid);
			
			cpiiEJB.setObject(cpiiObj);
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}				
	}
}
