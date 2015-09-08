/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.bean.autoworkshop;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.sql.*;
import java.util.*;

import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.util.*;
import com.vlee.bean.customer.*;

public class EditJobsheetForm extends java.lang.Object implements Serializable
{
	public JobsheetIndexObject jobsheet = null;
	public JobsheetIndexObject previousJobsheet= null;
	public TreeMap tableRows = null;
	public CustCreditControlChecker creditChecker = null;
	public boolean bSetDate = false;
	public Timestamp tsDate;
	public boolean saving;
	public Vector vecEditedJobsheet;
	public boolean validBranch = false;
	public BranchObject branchObj;
	public CustAccountObject customer;
	public VehicleObject vehicle;
	public Integer userId;
	public InvoiceObject invoice = null;
	public ConvertJobsheetToInvoiceReceiptForm convertForm = null;
	
	// TKW20071113: Checks inventory qty before creating an invoice. Enough said.
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
			Log.printVerbose("Interation: " + i);
			if(objdrow.getBomId().intValue()==0 && !objdrow.getPackageGroup().equals(""))
			{
				Log.printVerbose("package header detected.");
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
	
	public BigDecimal getBillAmount()
	{
		return getNetAmount().add(getTotalTax());
	}
	
	public void addStockWithItemCode(DocRow docr) throws Exception
	{
		docr.setUser1(this.jobsheet.salesId.intValue());
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
			this.tableRows.put(docr.getKey(), docr);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}

		this.convertForm.addStockWithItemCode(docr);
	}

	
	public void setDate(Timestamp txDate)
	{
		if (TimeFormat.strDisplayDate(txDate).equals(TimeFormat.strDisplayDate(this.tsDate)))
		{ return; }
		this.tsDate = txDate;
		this.bSetDate = true;
		this.convertForm.setDate(txDate);
	}

	public String getDate(String buffer)
	{
		if (this.bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		return TimeFormat.strDisplayDate(this.tsDate);
	}

	public Timestamp getDate()
	{
		if (this.bSetDate == false)
		{
			this.tsDate = TimeFormat.getTimestamp();
		}
		return this.tsDate;
	}

	public void setTermsDay(Integer terms)
	{
		this.jobsheet.paymentTermsId = terms;
	}

	public Integer getTermsDay()
	{
		return this.jobsheet.paymentTermsId;
	}

	public DocRow dropDocRow(String key)
	{
		this.convertForm.dropDocRow(key);
		return (DocRow) this.tableRows.remove(key);
	}

	public boolean getValidBranch()
	{
		if(this.branchObj==null)
		{ 	
			this.validBranch = false;
			return false;
		}
		else
		{
			this.validBranch = true;
			return true;
		}
	}

	public boolean setBranch(Integer iBranch)
	{
		this.branchObj = BranchNut.getObject(iBranch);
		if (this.branchObj != null)
		{
			this.validBranch = true;
			this.convertForm.setBranch(this.branchObj.pkid);
			return true;
		} else
		{
			this.validBranch = false;
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

	public JobsheetIndexObject getJobsheet()
	{ return this.jobsheet; }


	public void setVehicle(VehicleObject vehObj)
	{
		setCustomer(vehObj.accountId);
		this.vehicle = vehObj;
		this.convertForm.setVehicle(vehObj);
	}


	public TreeMap getTableRows()
	{
		return this.tableRows;
	}

	public void setVehicle(Integer vehicleId)
	{
		VehicleObject vehObj = VehicleNut.getObject(vehicleId);
		if(vehObj!=null)
		{
			setCustomer(vehObj.accountId);
			this.vehicle = vehObj;
			this.convertForm.setVehicle(vehicleId);
		}
		else
		{
		}
	}

	public VehicleObject getVehicle()
	{ return this.vehicle;}

	public CustAccountObject getCustomer()
	{ return this.customer;}

	public void updateAccount()
	{
      this.customer = CustAccountNut.getObject(this.customer.pkid);
      if (this.customer != null)
      {
         this.jobsheet.paymentTermsId = this.customer.creditTerms;
         if(this.customer.salesman.intValue()>0)
         {
            try {setSalesUserId(customer.salesman);}
            catch(Exception ex){};
         }
         this.jobsheet.entityTable = CustAccountBean.TABLENAME;
         this.jobsheet.entityKey = this.customer.pkid;
         this.jobsheet.entityName  = this.customer.getName();
         this.jobsheet.identityNumber = this.customer.identityNumber;
         this.jobsheet.billingHandphone = this.customer.mobilePhone;
         this.jobsheet.billingPhone1 = this.customer.telephone1;
         this.jobsheet.billingPhone2 = this.customer.telephone2;
         this.jobsheet.billingFax = this.customer.faxNo;
         this.jobsheet.billingEmail = this.customer.email1;
         this.jobsheet.billingCompanyName = this.customer.name;
         this.jobsheet.billingAdd1 = this.customer.mainAddress1;
         this.jobsheet.billingAdd2 = this.customer.mainAddress2;
         this.jobsheet.billingAdd3 = this.customer.mainAddress3;
         this.jobsheet.billingCity = this.customer.mainCity;
         this.jobsheet.billingZip = this.customer.mainPostcode;
         this.jobsheet.billingState =  this.customer.mainState;
         this.jobsheet.billingCountry = this.customer.mainCountry;
         this.jobsheet.jsType = this.customer.dealerCode;

         this.creditChecker = new CustCreditControlChecker();
         this.creditChecker.setAccount(this.customer);
         this.creditChecker.generateReport();
        

      }
   }


   public void updateVehicle()
   {
      setVehicle(this.vehicle.pkid);
   }

   public boolean setCustomer(Integer iCustomer)
   {
		this.vehicle = null;
      this.customer = CustAccountNut.getObject(iCustomer);
      if (this.customer != null)
      {
         //this.jobsheet.paymentTermsId = this.customer.creditTerms;
/*         if(this.customer.salesman.intValue()>0)
         {
            try {setSalesUserId(customer.salesman);}
            catch(Exception ex){};
         }*/
			this.jobsheet.entityTable = CustAccountBean.TABLENAME;
			this.jobsheet.entityKey = this.customer.pkid;
			this.jobsheet.entityName  = this.customer.getName();
			this.jobsheet.identityNumber = this.customer.identityNumber;
			this.jobsheet.billingHandphone = this.customer.mobilePhone;
			this.jobsheet.billingPhone1 = this.customer.telephone1;
			this.jobsheet.billingPhone2 = this.customer.telephone2;
			this.jobsheet.billingFax = this.customer.faxNo;
			this.jobsheet.billingEmail = this.customer.email1;
			this.jobsheet.billingCompanyName = this.customer.name;
			this.jobsheet.billingAdd1 = this.customer.mainAddress1;
			this.jobsheet.billingAdd2 = this.customer.mainAddress2;
			this.jobsheet.billingAdd3 = this.customer.mainAddress3;
			this.jobsheet.billingCity = this.customer.mainCity;
			this.jobsheet.billingZip = this.customer.mainPostcode;
			this.jobsheet.billingState = 	this.customer.mainState;
			this.jobsheet.billingCountry = this.customer.mainCountry;
			this.jobsheet.jsType = this.customer.dealerCode;

         this.creditChecker = new CustCreditControlChecker();
         this.creditChecker.setAccount(this.customer);
         this.creditChecker.generateReport();
       //sim  this.convertForm.setCustomer(iCustomer);

         return true;
      } 
		else
      {
         return false;
      }
   }






	public void setSalesUsername(String username) throws Exception
	{
		Integer buf = UserNut.getUserId(username);
		if (buf == null)
		{
			throw new Exception("Invalid salesman username!!");
		}
		this.jobsheet.salesId = buf;
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			docrow.setUser1(this.jobsheet.salesId.intValue());
		}
		this.convertForm.setSalesUsername(username);
	}

	public void setSalesUserId(Integer userId) throws Exception
	{
		UserObject userObj = UserNut.getObject(userId);
		this.jobsheet.salesId = userId;
		Vector vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow docrow = (DocRow) vecDocRow.get(cnt);
			docrow.setUser1(this.jobsheet.salesId.intValue());
		}
		this.convertForm.setSalesUserId(userId);
	}

	public synchronized JobsheetIndexObject saveJobsheet()
			throws Exception
	{
		/// declaring a temporary variables to store the jobsheet
		JobsheetIndexObject jsObj = this.jobsheet;
		if(!canSave()){ throw new Exception("Cannot Save Jobsheet!");}

		// Populate Defaults

		/// first of all, populate various fields in jobsheet
		Log.printVerbose("....................checkpoint 2222a");
		jsObj.branch = branchObj.pkid;
		jsObj.location = branchObj.invLocationId;
		jsObj.pcCenter = branchObj.accPCCenterId;
		jsObj.currency = branchObj.currency;
		jsObj.lastUpdate = TimeFormat.getTimestamp();
		jsObj.useridEdit = this.userId;
		
		/// then populate the vehicle information		
		if(this.vehicle!=null)
		{
				Log.printVerbose("....................checkpoint 2222a-1");
				/// need to update the Vehicle Object mileage too
				this.vehicle =  VehicleNut.getObject(this.vehicle.pkid);
				Vehicle vehicleEJB = VehicleNut.getHandle(this.vehicle.pkid);
				this.vehicle.mileageLatest = jsObj.mileageLatest;
				this.vehicle.mileageNext = this.vehicle.mileageLatest.add(new BigDecimal(3000));
				this.vehicle.repairLast = TimeFormat.getTimestamp();
				this.vehicle.repairNext = TimeFormat.add(this.vehicle.repairLast,0,3,0);
				vehicleEJB.setObject(this.vehicle);

				jsObj.foreignTable = VehicleBean.TABLENAME;
				jsObj.foreignKey = this.vehicle.pkid;
				jsObj.foreignText = this.vehicle.regnum;
		}
		else
		{
				jsObj.foreignTable = "";
				jsObj.foreignKey = new Integer(0);
				jsObj.foreignText = "";
		}

		/// then populate the DocRow into jobsheet item
		Log.printVerbose("....................checkpoint 2222a-2");
		Vector vecTableRows = new Vector(this.tableRows.values());
		jsObj.vecItem.clear();
		for(int cnt1=0;cnt1<vecTableRows.size();cnt1++)
		{
				Log.printVerbose("....................checkpoint 2222a-3-"+cnt1);
				DocRow docrow = (DocRow) vecTableRows.get(cnt1);
				JobsheetItemObject jsItemObj = new JobsheetItemObject(docrow, jsObj);
				jsObj.vecItem.add(jsItemObj);
		}

		/// then create the jobsheet tree
		JobsheetIndexNut.update(jsObj);

		/// store a copy of the jobsheet inside the vector for easy retrieval of this jobsheet for the user later
		this.vecEditedJobsheet.add(jsObj);

		return jsObj;
	}

	// / contructor!
	public EditJobsheetForm(Integer iUser)
	{
		this.jobsheet = null;
		this.tableRows = new TreeMap();
		this.bSetDate = false;
		this.tsDate = TimeFormat.getTimestamp();
		this.saving = false;
		this.previousJobsheet = null;
		this.userId = iUser;
//		this.jobsheet.salesId = iUser;
		this.customer = null;
		this.vehicle = null;
		this.vecEditedJobsheet = new Vector();
		this.convertForm = new ConvertJobsheetToInvoiceReceiptForm(this.userId);
	}

	public ConvertJobsheetToInvoiceReceiptForm getConvertForm()
	{
		System.out.println("convertForm customer pkid : "+this.convertForm.getCustomer().pkid.toString());
			
		return this.convertForm;
	}

	public void loadJobsheet(Long jsId) throws Exception
	{
		reset();
		
		this.jobsheet = JobsheetIndexNut.getObjectTree(jsId);
		
		if(this.jobsheet==null){ throw new Exception("The Jobsheet "+jsId.toString()+" Does not exist!");}

		QueryObject queryInvoice = new QueryObject(new String[]{
				InvoiceBean.WORK_ORDER +" = '"+this.jobsheet.pkid.toString()+"' "});
		
		Vector vecInvoice = new Vector(InvoiceNut.getObjects(queryInvoice));
		
		for(int cnt1=0;cnt1<vecInvoice.size();cnt1++)
		{ this.invoice  = (InvoiceObject) vecInvoice.get(cnt1); }

		/// must check the status of the jobsheet, if it has been converted into invoice, 
		/// do not allow editing!
		if(this.jobsheet.state.equals(JobsheetIndexBean.STATE_INVOICE))
		{
			this.jobsheet = null;
			throw new Exception("The JOBSHEET "+jsId.toString()+" has been converted into invoice, it cannot be edited anymore!");
		}

		if(this.jobsheet.foreignTable.equals(VehicleBean.TABLENAME))
		{ 
			setVehicle(this.jobsheet.foreignKey); 
			this.convertForm.setVehicle(this.jobsheet.foreignKey);
		}
		else if(this.jobsheet.entityKey.intValue()>0)
		{ 
			setCustomer(this.jobsheet.entityKey); 
			this.convertForm.setCustomer(this.jobsheet.entityKey);
		}
		
		System.out.println("Customer : "+this.convertForm.getCustomer().name);
		
		this.branchObj = BranchNut.getObject(this.jobsheet.branch);
		
		this.convertForm.setBranch(this.jobsheet.branch);
		
		this.tsDate = this.jobsheet.timeCreated;
		this.convertForm.setDate(this.tsDate);
		this.convertForm.setRemarks(this.jobsheet.remarks);
		this.tableRows = new TreeMap();
		
		for(int cnt1=0;cnt1<this.jobsheet.vecItem.size();cnt1++)
		{
			JobsheetItemObject jsItemObj = (JobsheetItemObject) this.jobsheet.vecItem.get(cnt1);
			Log.printVerbose("from jsItemObj unitPriceNet: " + jsItemObj.unitPriceNet);
			Log.printVerbose("from jsItemObj unitPriceList: " + jsItemObj.unitPriceList);
			DocRow docrow = jsItemObj.getDocRow();
			Log.printVerbose("Item " + docrow.itemName + " in loadJobsheet getNetPrice:" + docrow.getNetPrice());
			Log.printVerbose("Item " + docrow.itemName + " in loadJobsheet getPrice1:" + docrow.getPrice1());
			docrow.setIncludeCodeInKey(false);
			//docrow.setKey2(docrow.getKey()); // 20080328 Jimmy
			this.tableRows.put(docrow.getKey(), docrow);
			this.convertForm.addStockWithItemCode(docrow);
		}
		
		System.out.println("Customer : "+this.convertForm.getCustomer().name);
		
		this.convertForm.setJobsheet(this.jobsheet);
		this.convertForm.setAccidentDate(this.jobsheet.accidentDate);
		this.convertForm.setPolicyNumber(this.jobsheet.policyNumber);
		this.convertForm.setClaimAmount(this.jobsheet.claimAmount);
		
		System.out.println("Customer : "+this.convertForm.getCustomer().name);
	}

	public JobsheetIndexObject getPreviousJobsheet()	
	{ return this.previousJobsheet;}

	public void reset()
	{
		this.jobsheet = null;
		this.invoice = null;
		this.customer = null;
		this.vehicle = null;
		this.tableRows = null;
		this.bSetDate = false;
		this.tsDate = TimeFormat.getTimestamp();
		this.saving = false;
		this.convertForm = new ConvertJobsheetToInvoiceReceiptForm(this.userId);
	}

	public synchronized void confirmAndSave() throws Exception
	{
		/// this is to prevent double click
		if (saving == true) return;
		this.previousJobsheet = null;
		this.saving = true;
		if (!canSave()) throw new Exception("Saving Not Allowed!");
		// 1) create the PurchaseOrder and GRN
		this.previousJobsheet = saveJobsheet();
		saving = false;
		reset();
	}

	public boolean canSave()
	{
		if(this.jobsheet==null) { return false; }
		/// if invoice has been created, do not allow saving
		if(this.jobsheet.state.equals(JobsheetIndexBean.STATE_INVOICE)) { return false; }
		/// also need to check if the vehicle / account has been selected
		if(this.customer==null) { return false;}
		return true;
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
			totalAmt = totalAmt.add(docrow.getQty().multiply(docrow.getPrice1()));
		}
		return totalAmt;
	}

   public BigDecimal getTotalDiscount()
   {
      Vector vecDocRow = new Vector(this.tableRows.values());
      BigDecimal totalAmt = new BigDecimal(0);
      for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
      {
         DocRow docrow = (DocRow) vecDocRow.get(cnt);
         totalAmt = totalAmt.add(docrow.getQty().multiply(docrow.getDiscount()));
      }
      return totalAmt;
   }

	public BigDecimal getNetAmount()
	{
		BigDecimal netAmount = getTotalAmt().subtract(getTotalDiscount());
		return netAmount;
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

	public BigDecimal getTotalAmountAfterTax()
	{
		return getTotalTax().add(getNetAmount());
	}

	public void fnAddStockWithItemCode(DocRow docr) throws Exception
	{
		//docr.setUser1(this.jobsheet.salesId.intValue());
		docr.setIncludeCodeInKey(false);

//		Log.printVerbose(" Checkpoint 1: docr.getPrice1(): "+docr.getPrice1());
//		Log.printVerbose(" Checkpoint 1: docr.getNetPrice(): "+docr.getNetPrice());
//
//		ItemObject itmObj = ItemNut.getObject(new Integer(docr.itemId));
//		if(itmObj.tax_option.equals(ItemBean.TAX_ENABLED))
//		{
//			BigDecimal taxPercentage = itmObj.tax_rate;
//			docr.setTaxPct(taxPercentage);			
//		}		
//		Log.printVerbose("EJF-CHECK-1");
      // / check if this row exists in the list already
      Vector vecDocRow = new Vector(this.tableRows.values());
      for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
      {
         DocRow dcrw = (DocRow) vecDocRow.get(cnt);
         if(dcrw.getKey2().equals(docr.getKey2()))
         {
				Log.printVerbose(" The old docrow is being removed... "+ dcrw.getItemName()+" Price:"+dcrw.getPrice1());
				Log.printVerbose(" The new docrow to be added ... "+ docr.getItemName()+" Price:"+docr.getPrice1());
            this.tableRows.remove(dcrw.getKey());
         }
      }
      Log.printVerbose("EJF-CHECK-2");
		// / check if this row exists in the list already
		vecDocRow = new Vector(this.tableRows.values());
		for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
		{
			DocRow oldrow = (DocRow) vecDocRow.get(cnt);
			if(!oldrow.getKey().equals(docr.getKey()))
			{
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
		}
		// / add it
		try
		{
			
/*			DocRow oldDocRow = (DocRow) this.tableRows.get(docr.getKey());*/
/*			if(oldDocRow==null)
			{
				Log.printVerbose("This item is null");
			}	*/		
			Log.printVerbose("EJF-CHECK-3");
/*			Log.printVerbose(" The old docrow getPrice1... "+ oldDocRow.getItemName()+" Price1:"+oldDocRow.getPrice1());
			Log.printVerbose(" The old docrow getNetPrice... "+ oldDocRow.getItemName()+" NetPrice:"+oldDocRow.getNetPrice());*/
			this.convertForm.fnAddStockWithItemCode(docr);
			Log.printVerbose("EJF-CHECK-4");
			Log.printVerbose(" The new docrow getPrice1... "+ docr.getItemName()+" Price1:"+docr.getPrice1());
			Log.printVerbose(" The new docrow getNetPrice... "+ docr.getItemName()+" NetPrice:"+docr.getNetPrice());			
			this.tableRows.put(docr.getKey(), docr);
			DocRow testDocRow = (DocRow) this.tableRows.get(docr.getKey());
			Log.printVerbose(" The test docrow getPrice1... "+ testDocRow.getItemName()+" Price1:"+testDocRow.getPrice1());
			Log.printVerbose(" The test docrow getNetPrice... "+ testDocRow.getItemName()+" NetPrice:"+testDocRow.getNetPrice());			
		
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	public void setDetails(Timestamp jobsheetDate, Timestamp completeDate, BigDecimal mileageLatest, Integer paymentTermsId,
									String remarks, String referenceNo, String policyNumber, BigDecimal claimAmount, Timestamp accidentDate)
	{

		setDate(jobsheetDate);
		
		// TKW20071101: Hello, this is where I cheated. The problem here is that whenever
		// the user tries to change this set of info, the timeCreated field gets changed along
		// with it. It would be better to separate the date modification function from this, but
		// I did not have the luxury of time to do so. Therefore, I am going to cheat and simply 
		// take the current time and merge it with the modified date. I am doing this to prevent 
		// a time of 12:00 AM from showing in TopSpeed's jobsheet printables. I am fine with how
		// this affects other customers because 1), this module is currently only used by TopSpeed, 
		// and 2), no other customer has complained about a time of 12:00 AM showing up in their 
		// jobsheets. I also feel completely justified in frakking up TopSpeed's original
		// jobsheet creation time this way because they are always frakking me too.
		// this.jobsheet.timeCreated = jobsheetDate;
		int hour = 0;
		int minute = 0;
		Timestamp fakeTime = TimeFormat.getTimestamp();
		if(jobsheet.timeCreated!=null)
		{
			fakeTime = jobsheet.timeCreated;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("hh");
		long b = fakeTime.getTime();
		java.util.Date bufDate = new java.util.Date(b);

		hour = new Integer(formatter.format(bufDate)).intValue();
		
		formatter = new SimpleDateFormat("hh");
		bufDate = new java.util.Date(b);

		minute = new Integer(formatter.format(bufDate)).intValue();
		jobsheetDate = TimeFormat.addTime(jobsheetDate,hour,minute,0);
		this.jobsheet.timeCreated = jobsheetDate;
		// End TKW20071101
		this.jobsheet.timeComplete = completeDate;
		this.jobsheet.mileageLatest = mileageLatest;
		this.jobsheet.paymentTermsId = paymentTermsId;
		this.jobsheet.remarks = remarks;
		this.jobsheet.referenceNo = referenceNo;
		this.jobsheet.claimAmount =claimAmount;
		this.jobsheet.accidentDate = accidentDate;
		this.jobsheet.policyNumber = policyNumber;

		//// UPDATING the CONVERTION FORM
		this.convertForm.setRemarks(remarks);
		this.convertForm.setReferenceNo(referenceNo);
		this.convertForm.setDate(jobsheetDate);
		this.convertForm.setAccidentDate(accidentDate);
		this.convertForm.setPolicyNumber(policyNumber);
		this.convertForm.setClaimAmount(claimAmount);
	}

	// 20080319 Jimmy
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
}



