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

import java.sql.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.bean.application.AppConfigManager;
import com.vlee.bean.customer.CustCreditControlChecker;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
// import com.vlee.bean.util.*;
import com.vlee.util.*;

/*---------------------------------------------------------
 BASIC RULES
 If Receipt Has Already Been Created
 1) CANNOT Change/edit the memberCard/User/CustAccount
 2) CANNOT remove Sales Order Item
 3) CAN add Sales Order Item
 4) Cannot Change Receipt Details
 5) Recheck the CRV to see if it is still valid
 
 If Receipt Has Not Been Created
 1) Can add/remove Sales Order Items
 2) Can set receipt/payment info
 3) Able to create receipt, and respective CRV if amount 
 matches the SalesOrder bill amount

 SalesOrder Cancellation
 1) Cancel the CRV if it has already been created
 2) Lead user to SalesReturn use-case if invoice has already been created
 3) Reset the state of the sales order items

 -----------------------------------------------------------*/
public class EditSalesOrderSession extends java.lang.Object implements Serializable
{
	public Vector prevInvoices = new Vector();
	boolean popupPrintCRV = false;
	SalesOrderIndexObject soObj = null;
	BranchObject branch = null;
	LocationObject productionLocation = null;
	Integer userId = null;
	CustAccountObject custAccObj = null;
	CustUserObject custUserObj = null;
	MemberCardObject memCardObj = null;
	ReceiptForm receiptForm = null;
	CashRebateVoucherObject crvObj = null;
	Vector vecReceipt = null;
	Vector vecCreditMemoLink = null;
	public String soType2 = "";
	public String salesmanCode = "";
	protected TreeMap tableRows = null; // // this is used for SalesOrderItems
	protected TreeMap redeemableCRV = null;
	protected TreeMap redeemingList = null;
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
	public String docTrail = "";

	public String orderMode = MODE_VIEW;
	public static String MODE_VIEW = "VIEW-MODE";
	public static String MODE_EDIT = "EDIT-MODE";
	public static String MODE_CREATE = "CREATE-MODE";
	
	public String etxnStatus = "";
	public static final String STATUS_BREACHED_CREDIT_LIMIT = "This customer's credit limit has been breached.";
	public static final String STATUS_CREDIT_TERMS_OUTSTANDING = "This customer has invoice(s) with exceeded credit terms.";
	public static final String STATUS_INVENTORY_QTY_INSUFFICIENT = "Current stocks cannot fulfill this sales order's requirements.";
	public static final String STATUS_GENERIC_APPROVAL_REQUIRED = "This sales order requires approval.";
	public static final String STATUS_ITEM_PRICE_APPROVAL_REQUIRED = "All item prices in this sales order require approval.";

	// // constructor
	public EditSalesOrderSession(Integer userId)
	{
		this.userId = userId;
		this.redeemableCRV = new TreeMap();
		this.redeemingList = new TreeMap();
		this.vecReceipt = new Vector();
		this.vecCreditMemoLink = new Vector();
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		this.popupPrintCRV = false;
		UserObject userObj = UserNut.getObject(this.userId);
		this.salesmanCode = userObj.userName;
		this.docTrail = "";
		this.etxnStatus = "";
	}
	
	public Vector getPreviousInvoices()
	{
		return this.prevInvoices;
	}
	
	public void setEtxnStatus(String buf)
	{
		this.soObj.etxnStatus = buf;
	}
	
	public String getMode()
	{ return this.orderMode;}
	public void setMode(String buf)
	{ this.orderMode = buf;}

	public void setOrderTaker(Integer bufUser)
	{
		if(this.soObj!=null)
		{ 
			this.soObj.useridCreate = bufUser; 
   		this.soObj.ordertakerUserid = bufUser;
			this.soObj.ordertakerName = UserNut.getUserName(this.soObj.ordertakerUserid);
			this.soObj.ordertakerTime = TimeFormat.getTimestamp();
		}
	}

	public void appendDocTrail(String paramName, String oldValue, String newValue)
	{
		this.docTrail = DocumentProcessingItemNut.appendDocTrail(paramName,oldValue,newValue, this.docTrail);
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
		// // reload the original sales order and auxiliary objects
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		this.popupPrintCRV = false;
		this.docTrail = "";
		this.soObj=null;
		this.branch = null;
		this.productionLocation = null;

	}

	public void setPopupPrintCRV(boolean buf)
	{
		this.popupPrintCRV = buf;
	}

	public boolean getPopupPrintCRV()
	{
		return this.popupPrintCRV;
	}

	public void setOrderType2(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.soType2 = SalesOrderIndexBean.SO_TYPE2_FLORIST;
		}
	}

	public void setOrderType1(String buf)
	{
		if(this.soObj !=null)
		{
			appendDocTrail("ORDER TYPE", this.soObj.soType1, buf);
			this.soObj.soType1 = buf;
		}
	}

	public void setDeliveryLocation(Long dlPkid, String option) throws Exception
	{
		DeliveryLocationObject dlObj = DeliveryLocationNut.getObject(dlPkid);

		if (dlObj == null || option == null)
		{
			return;
		}
		if (option.equals("country"))
		{
			appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);
			this.soObj.receiverCountry = dlObj.country;
		}
		if (option.equals("state"))
		{
			appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);
			appendDocTrail("RECIPIENT STATE",this.soObj.receiverState,dlObj.state);

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
		}
		if(option.equals("zip"))
		{
			appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);
			appendDocTrail("RECIPIENT STATE",this.soObj.receiverState,dlObj.state);
			{ appendDocTrail("RECIPIENT POSCODE",this.soObj.receiverZip,dlObj.zip);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
		}
		if(option.equals("city"))
		{

			{ appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RECIPIENT STATE",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RECIPIENT POSCODE",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RECIPIENT CITY",this.soObj.receiverCity,dlObj.city);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
		}
		if (option.equals("area"))
		{
			{ appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RECIPIENT STATE",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RECIPIENT POSCODE",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RECIPIENT CITY",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RECIPIENT AREA",this.soObj.receiverAdd3,dlObj.area);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
		}
		if (option.equals("street"))
		{

			{ appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RECIPIENT STATE",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RECIPIENT POSCODE",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RECIPIENT CITY",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RECIPIENT AREA",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RECIPIENT STREET",this.soObj.receiverAdd2,dlObj.street);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
		}
		if (option.equals("building"))
		{
			{ appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RECIPIENT STATE",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RECIPIENT POSCODE",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RECIPIENT CITY",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RECIPIENT AREA",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RECIPIENT STREET",this.soObj.receiverAdd2,dlObj.street);}
			{ appendDocTrail("RECIPIENT BUILDING",this.soObj.receiverAdd1,dlObj.building);}


			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
			this.soObj.receiverAdd1 = dlObj.building;
		}
		if (option.equals("company"))
		{
			{ appendDocTrail("RECIPIENT COUNTRY",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RECIPIENT STATE",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RECIPIENT POSCODE",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RECIPIENT CITY",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RECIPIENT AREA",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RECIPIENT STREET",this.soObj.receiverAdd2,dlObj.street);}
			{ appendDocTrail("RECIPIENT BUILDING",this.soObj.receiverAdd1,dlObj.building);}
			{ appendDocTrail("RECIPIENT COMPANY",this.soObj.receiverCompanyName,dlObj.name);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
			this.soObj.receiverAdd1 = dlObj.building;
			this.soObj.receiverCompanyName = dlObj.name;
		}
		

		if(dlObj.deliveryRate1.signum()>0)
		{
			ItemObject itmObj = ItemNut.getObject(ItemBean.PKID_DELIVERY);
			DocRow docrow = new DocRow();
			// docrow.setTemplateId(0)
			docrow.setItemType(ItemBean.CODE_DELIVERY);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(new BigDecimal("1"));
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(dlObj.deliveryRate1);
			// docrow.setDiscount(new
			// BigDecimal(req.getParameter("itemUnitDiscount")));
			docrow.user1 = userId.intValue();
			docrow.setRemarks("");
			docrow.setCcy2("");
			docrow.setPrice2(new BigDecimal(0));
			docrow.setProductionRequired(itmObj.productionRequired);
			docrow.setDeliveryRequired(itmObj.deliveryRequired);
			fnAddStockWithItemCode(docrow);
		} else
		{
			// check for this row in the list
			Vector vecDocRow = new Vector(this.tableRows.values());
			for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt);
				if (docrow.getItemId() == ItemBean.PKID_DELIVERY.intValue())
				{
					// / remove from this.tableRows
					DocRow dcrowRemoved = (DocRow) this.tableRows.remove(docrow.getKey());
					// / remove from this.soObj.vecItem
					for (int cnt3 = 0; cnt3 < this.soObj.vecItem.size(); cnt3++)
					{
						SalesOrderItemObject soItemObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt3);
						if (soItemObj.pkid.longValue() == dcrowRemoved.getDocId())
						{
							this.soObj.vecItem.remove(cnt3);
							cnt3--;
						}
					}
					// / remove from EJB
					try
					{
						SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(dcrowRemoved.getDocId()));
						if (soItemEJB != null)
						{
							soItemEJB.remove();
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
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
	
	public boolean checkCreditLimitBreached(CustAccountObject customer, BigDecimal SOAmount)
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
	
	public void loadSalesOrder(Long soPkid) throws Exception
	{
		System.out.println("Inside EditSalesOrderSession.loadSalesOrder");
		
		reset();

		this.soObj = SalesOrderIndexNut.getObjectTree(soPkid);	
		if(this.soObj == null || this.soObj.pkid.longValue()==0)
		{
			throw new Exception("Invalid Sales Order Number!");
		}
		// / populate this into the DocRows
		this.tableRows = this.soObj.getDocRows();

		// / populate other member objects of this Form/Session
		this.branch = BranchNut.getObject(this.soObj.branch);

		Log.printVerbose("Branch: "+this.branch);

		this.productionLocation = LocationNut.getObject(this.soObj.productionLocation);

		Log.printVerbose("ProductionLocation: "+this.productionLocation);

		this.custAccObj = CustAccountNut.getObject(this.soObj.senderKey1);

		if (this.soObj.senderKey2.intValue() > 0)
		{
			this.custUserObj = CustUserNut.getObject(this.soObj.senderKey2);
		} else
		{
			this.custUserObj = null;
		}
		if (this.soObj.senderLoyaltyCardNo.length() > 0)
		{
			this.memCardObj = MemberCardNut.getObjectByCardNo(this.soObj.senderLoyaltyCardNo);
		} else
		{
			this.memCardObj = null;
		}
		initializeReceiptForm();
		this.loadReceipts();
		retrieveRedeemableCRV();
		this.loadCRV();

		Log.printVerbose("End of Load SO");
	}

	private void loadCRV()
	{
		QueryObject query = new QueryObject(new String[] { CashRebateVoucherBean.SRC_KEY1 + " = '"
				+ this.soObj.pkid.toString() + "' " });
		query.setOrder(" ORDER BY " + CashRebateVoucherBean.PKID);
		Vector vecCRV = new Vector(CashRebateVoucherNut.getObjects(query));
		if (vecCRV.size() > 0)
		{
			this.crvObj = (CashRebateVoucherObject) vecCRV.get(vecCRV.size() - 1);
		} else
		{
			this.crvObj = null;
		}
	}

	public ReceiptForm getReceiptForm()
	{
		return this.receiptForm;
	}

	public TreeMap getTableRows()
	{
		System.out.println("Inside EditSalesOrderSession.getTableRows");
						
		return this.tableRows;
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

	public void setBranch(Integer iBranch)
	{
		if (hasInvoice())
		{
			return;
		}
		try
		{
			BranchObject brhObj = BranchNut.getObject(iBranch);
			if (brhObj != null)
			{
				if(this.branch!=null && !brhObj.pkid.equals(this.branch.pkid))
				{ appendDocTrail("BRANCH", this.branch.code, brhObj.code);}
				this.branch = brhObj;
				if (!hasInvoice())
				{
					this.soObj.branch = iBranch;
					this.soObj.receiptBranch = iBranch;
					this.soObj.productionBranch = iBranch;
					this.soObj.productionLocation = this.branch.invLocationId;
				}
				this.receiptForm.setBranch(this.soObj.branch);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public LocationObject getProductionLocation()
	{ return this.productionLocation;}

	public String getProductionLocation(String buf)
	{
		if(this.productionLocation ==null)
		{ return buf; }
		else
		{ return this.productionLocation.pkid.toString(); }
	}

	public void setProductionLocation(Integer iLocation)
	{
		if(hasInvoice())
		{ return ;}
		try
		{
			LocationObject locObj = LocationNut.getObject(iLocation);
			if(locObj!=null)
			{
				if(!locObj.pkid.equals(this.productionLocation.pkid))
				{	
					appendDocTrail("SHIP FROM", this.productionLocation.locationCode,locObj.locationCode);
				}
				this.productionLocation = locObj;
				this.soObj.productionLocation = this.productionLocation.pkid;
			}
		}
		catch(Exception ex)
		{ ex.printStackTrace();}
	}

	public boolean canChangeAccount()
	{
		boolean rtnValue = !hasInvoice() && !hasReceipt();
		return rtnValue;
	}

	public void setReceiptInfo(String receiptRemarks, String receiptApprovalCode, Integer receiptBranch)
	{
		if (this.soObj != null)
		{
			{ appendDocTrail("RCT Remarks", this.soObj.receiptRemarks,receiptRemarks);}
			{ appendDocTrail("RCT APPROVAL CODE",this.soObj.receiptApprovalCode,receiptApprovalCode);}
			if(!this.soObj.receiptBranch.equals(receiptBranch))
			{ 
				BranchObject brh1 = BranchNut.getObject(this.soObj.receiptBranch);
				BranchObject brh2 = BranchNut.getObject(receiptBranch);
				if(brh1!=null && brh2!=null)
				{
					appendDocTrail("PAY AT", brh1.code, brh2.code);
				}
			}

			// this.soObj.receiptMode = receiptMode;
			this.soObj.receiptRemarks = receiptRemarks;
			this.soObj.receiptApprovalCode = receiptApprovalCode;
			this.soObj.receiptBranch = receiptBranch;
		}



	}

	public void setReceiptMode(String buf)
	{
		if (this.soObj != null)
		{
			{ appendDocTrail("PAYMENT MODE", this.soObj.receiptMode, buf);}
			this.soObj.receiptMode = buf;
		}
	}

	public void setDisplayFormat(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.displayFormat = buf;
		}
	}

	public void setCustomer(CustAccountObject custObj)
	{
		if (!canChangeAccount())
		{
			return;
		}
		
		System.out.println("--------------------> this.soObj.senderName : "+this.soObj.senderName);
		System.out.println("--------------------> custObj.name : "+custObj.name);
		
		{ appendDocTrail("SENDER ACCOUNT NAME", this.soObj.senderName, custObj.name);}
		{ appendDocTrail("SENDER GEN LINE", this.soObj.senderPhone1, custObj.telephone1);}
		{ appendDocTrail("SENDER DIR LINE", this.soObj.senderPhone2, custObj.telephone2);}
		{ appendDocTrail("SENDER MOBILE", this.soObj.senderHandphone, custObj.mobilePhone);}
		{ appendDocTrail("SENDER FAX", this.soObj.senderFax, custObj.faxNo);}
		{ appendDocTrail("SENDER COMPANY", this.soObj.senderCompanyName, custObj.name);}
		{ appendDocTrail("SENDER BUILDING", this.soObj.senderAdd1, custObj.mainAddress1);}
		{ appendDocTrail("SENDER UNIT/ST", this.soObj.senderAdd2, custObj.mainAddress2);}
		{ appendDocTrail("SENDER AREA/TMN", this.soObj.senderAdd3, custObj.mainAddress3);}
		{ appendDocTrail("SENDER CITY", this.soObj.senderCity, custObj.mainCity);}
		{ appendDocTrail("SENDER POSCODE", this.soObj.senderZip, custObj.mainPostcode);}
		{ appendDocTrail("SENDER STATE", this.soObj.senderState, custObj.mainState);}
		{ appendDocTrail("SENDER COUNTRY", this.soObj.senderCountry, custObj.mainCountry);}
		
		if(this.docTrail.length()>4 && this.soObj!=null)
		{
			System.out.println("Inside EditSalesOrderSession.updateSalesOrder docTrail : "+this.docTrail);
			
			DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
			dpiObj.processType = "ORDER-UPDATE";
			dpiObj.category = "UPDATE-DETAILS";			
			dpiObj.auditLevel = new Integer(0);
			dpiObj.userid = this.userId;
			dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
			dpiObj.docId = this.soObj.pkid;
			dpiObj.description1 = this.docTrail;
			dpiObj.time = TimeFormat.getTimestamp();
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
			DocumentProcessingItemNut.fnCreate(dpiObj);
			this.docTrail = "";
		}
		
		this.custUserObj = null;
		this.memCardObj = null;
		this.custAccObj = custObj;
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderName = custObj.name;
		this.soObj.senderTable2 = "";
		this.soObj.senderKey2 = new Integer(0);
		this.soObj.senderIdentityNumber = custObj.identityNumber;
		this.soObj.senderHandphone = custObj.mobilePhone;
		this.soObj.senderFax = custObj.faxNo;
		this.soObj.senderPhone1 = custObj.telephone1;
		this.soObj.senderPhone2 = custObj.telephone2;
		this.soObj.senderCompanyName = custObj.name;
		this.soObj.senderAdd1 = custObj.mainAddress1;
		this.soObj.senderAdd2 = custObj.mainAddress2;
		this.soObj.senderAdd3 = custObj.mainAddress3;
		this.soObj.senderCity = custObj.mainCity;
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
	}

	public void setCustUser(CustUserObject custUser)
	{
		this.memCardObj = null;
		this.custUserObj = custUser;
		if (!canChangeAccount())
		{
			if (!this.custUserObj.accId.equals(this.custAccObj.pkid))
			{
				return;
			}
		}
		this.custAccObj = CustAccountNut.getObject(this.custUserObj.accId);
		
		System.out.println("--------------------> this.soObj.senderName : "+this.soObj.senderName);
		System.out.println("--------------------> custObj.name : "+this.custAccObj.name);
		System.out.println("--------------------> custUser : "+custUser.nameFirst + " "+custUser.nameLast);
		
		{ appendDocTrail("SENDER ACCOUNT NAME", this.soObj.senderName, this.custAccObj.name);}
		{ appendDocTrail("SENDER CONTACT NAME", this.soObj.senderName, custUser.nameFirst + " "+custUser.nameLast);}
		{ appendDocTrail("SENDER GEN LINE", this.soObj.senderPhone1, this.custAccObj.telephone1);}
		{ appendDocTrail("SENDER DIR LINE", this.soObj.senderPhone2, this.custAccObj.telephone2);}
		{ appendDocTrail("SENDER MOBILE", this.soObj.senderHandphone, this.custAccObj.mobilePhone);}
		{ appendDocTrail("SENDER FAX", this.soObj.senderFax, this.custAccObj.faxNo);}
		{ appendDocTrail("SENDER COMPANY", this.soObj.senderCompanyName, this.custAccObj.name);}
		{ appendDocTrail("SENDER BUILDING", this.soObj.senderAdd1, this.custAccObj.mainAddress1);}
		{ appendDocTrail("SENDER UNIT/ST", this.soObj.senderAdd2, this.custAccObj.mainAddress2);}
		{ appendDocTrail("SENDER AREA/TMN", this.soObj.senderAdd3, this.custAccObj.mainAddress3);}
		{ appendDocTrail("SENDER CITY", this.soObj.senderCity, this.custAccObj.mainCity);}
		{ appendDocTrail("SENDER POSCODE", this.soObj.senderZip, this.custAccObj.mainPostcode);}
		{ appendDocTrail("SENDER STATE", this.soObj.senderState, this.custAccObj.mainState);}
		{ appendDocTrail("SENDER COUNTRY", this.soObj.senderCountry, this.custAccObj.mainCountry);}
		
		if(this.docTrail.length()>4 && this.soObj!=null)
		{
			System.out.println("Inside EditSalesOrderSession.updateSalesOrder docTrail : "+this.docTrail);
			
			DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
			dpiObj.processType = "ORDER-UPDATE";
			dpiObj.category = "UPDATE-DETAILS";			
			dpiObj.auditLevel = new Integer(0);
			dpiObj.userid = this.userId;
			dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
			dpiObj.docId = this.soObj.pkid;
			dpiObj.description1 = this.docTrail;
			dpiObj.time = TimeFormat.getTimestamp();
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
			DocumentProcessingItemNut.fnCreate(dpiObj);
			this.docTrail = "";
		}
		
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;

		this.soObj.senderTable2 = CustUserBean.TABLENAME;
		this.soObj.senderKey2 = this.custUserObj.pkid;

		this.soObj.senderName = custUser.nameFirst + " "+custUser.nameLast;
		this.soObj.senderIdentityNumber = custUser.referenceNo;
		this.soObj.senderHandphone = custUser.mobilePhone;
		this.soObj.senderFax = custUser.faxNo;
		this.soObj.senderPhone1 = custUser.telephone1;
		this.soObj.senderPhone2 = custUser.telephone2;
		this.soObj.senderCompanyName = this.custAccObj.name;
		this.soObj.senderAdd1 = custUser.mainAddress1;
		this.soObj.senderAdd2 = custUser.mainAddress2;
		this.soObj.senderAdd3 = custUser.mainAddress3;
		this.soObj.senderZip = custUser.mainPostcode;
		this.soObj.senderState = custUser.mainState;
		this.soObj.senderCountry = custUser.mainCountry;
		this.soObj.etxnAccount = custUser.defaultCardNumber;

		if(this.custAccObj.state.equals(CustAccountBean.STATE_BL))
		{ this.soObj.flagInternalBool = true; }
		else { this.soObj.flagInternalBool = false;}

		this.receiptForm.setCustomer(custAccObj.pkid);
		this.receiptForm.setCCDetails(this.custUserObj.defaultCardNumber,
                                    this.custUserObj.defaultCardType,
                                    this.custUserObj.defaultCardName,
                                    this.custUserObj.defaultCardBank,
									"",
                                    this.custUserObj.defaultCardGoodThru,		
									this.custUserObj.defaultCardSecurityNum);
		retrieveRedeemableCRV();
   }


	public void updateAccount()
	{
		this.custAccObj = CustAccountNut.getObject(this.custAccObj.pkid);
	}

	public void setFlagInternal(String buf)
	{
		{ appendDocTrail("INTERNAL REMARKS", this.soObj.flagInternal, buf);}
		this.soObj.flagInternal = buf;
	}

	public void setPaymentStatus(String buf)
	{
		{ appendDocTrail("PAY STATUS", this.soObj.statusPayment, buf);}
		this.soObj.statusPayment = buf;
	}

	public void setPaymentRemarks(String buf)
	{
		{ appendDocTrail("PAYMENT REMARKS", this.soObj.receiptRemarks, buf);}
		this.soObj.receiptRemarks = buf;
	}

	public void setInfo(String remarks, boolean bRInvoice, boolean bRReceipt, String flagInternal, String flagSender,
			String flagReceiver, String managerPassword, String soType1, String occasion,
			String thirdpartyLoyaltyCardCode, String thirdpartyLoyaltyCardNumber, String paymentStatus, 
			BigDecimal interfloraPrice, String interfloraFlowers1)
	{
		Log.printVerbose(" SAVING DATA ... ");
		{ appendDocTrail( "RMKS", this.soObj.remarks1, remarks);}
		if(!this.soObj.requireInvoice==bRInvoice)
		{ appendDocTrail( "REQUIRE INVOICE:", ((this.soObj.requireInvoice)?"true":"false"), ((bRInvoice)?"true":"false"));}
		appendDocTrail("INTERNAL RMKS", this.soObj.flagInternal, flagInternal);
		{ appendDocTrail("SENDER RMKS", this.soObj.flagSender, flagSender);}
		{ appendDocTrail("RECIPIENT RMKS", this.soObj.flagReceiver, flagReceiver);}
		{ appendDocTrail("ORDER TYPE",this.soObj.soType1, soType1);}
		{ appendDocTrail("OCCASION", this.soObj.occasion,occasion);}
		{ appendDocTrail("LOYALTY CARD", this.soObj.thirdpartyLoyaltyCardCode, thirdpartyLoyaltyCardCode);}
		{ appendDocTrail("LOYALTY CARD NO", this.soObj.thirdpartyLoyaltyCardNumber, thirdpartyLoyaltyCardNumber);}
		{ appendDocTrail("PAY STATUS", this.soObj.statusPayment, paymentStatus);}
		{ appendDocTrail("INTERFLORA PRICE", CurrencyFormat.strCcy(this.soObj.interfloraPrice),CurrencyFormat.strCcy(interfloraPrice));}
		{ appendDocTrail("INTERFLORA FLOWERS", this.soObj.interfloraFlowers1, interfloraFlowers1);}

		this.soObj.remarks1 = remarks;
		this.soObj.requireInvoice = bRInvoice;
		this.soObj.requireReceipt = bRReceipt;
		this.soObj.flagInternal = flagInternal;
		this.soObj.flagSender = flagSender;
		this.soObj.flagReceiver = flagReceiver;
		this.soObj.soType1 = soType1;
		this.soObj.occasion = occasion;
		this.soObj.thirdpartyLoyaltyCardCode = thirdpartyLoyaltyCardCode;
		this.soObj.thirdpartyLoyaltyCardNumber = thirdpartyLoyaltyCardNumber;
		this.soObj.statusPayment = paymentStatus;
		this.soObj.interfloraPrice = interfloraPrice;
		this.soObj.interfloraFlowers1 = interfloraFlowers1;
		//updateSalesOrder();
	}

	// // INTEGRATED SETTERS
	public void setDeliveryDetails(String deliveryTo, String deliveryToName, String deliveryFrom, //3
			String deliveryFromName, String deliveryMsg1, String expDeliveryTime, String expDeliveryTimeStart,//7
			String deliveryPreferences, String senderName, String senderIdentityNumber, String senderEmail, String senderHandphone,//12
			String senderFax, String senderPhone1, String senderPhone2, String senderInternetNo, //16
			String senderCompanyName, String senderAdd1, String senderAdd2, String senderAdd3, String senderCity, //21
			String senderZip, String senderState, String senderCountry, String receiverTitle, String receiverName,//26
			String receiverIdentityNumber, String receiverEmail, String receiverHandphone, String receiverFax, String receiverPhone1,//31
			String receiverPhone2, String receiverCompanyName, String receiverAdd1, String receiverAdd2,//35
			String receiverAdd3, String receiverCity, String receiverZip, String receiverState, String receiverCountry, 
			boolean messageCardRequired, String receiverLocationType)//40
	{
		
		{ appendDocTrail("CARD TO", this.soObj.deliveryTo, deliveryTo);}
		{ appendDocTrail("CARD TO NAME", this.soObj.deliveryToName, deliveryToName);}
		{ appendDocTrail("CARD FROM", this.soObj.deliveryFrom, deliveryFrom);}
		{ appendDocTrail("CARD FROM NAME", this.soObj.deliveryFromName, deliveryFromName);}
		{ appendDocTrail("CARD MESSAGE", this.soObj.deliveryMsg1, deliveryMsg1);}
		{ appendDocTrail("DEL PREF", this.soObj.deliveryPreferences, deliveryPreferences);}		

		{ appendDocTrail("RECIPIENT NAME", this.soObj.receiverName, receiverName);}
		{ appendDocTrail("RECIPIENT ID", this.soObj.receiverIdentityNumber, receiverIdentityNumber);}
		{ appendDocTrail("RECIPIENT EMAIL", this.soObj.receiverEmail, receiverEmail);}
		
		{ if(!this.soObj.receiverPhone1.equals("") && !receiverPhone1.equals(" -Ext"))
				{
					if(this.soObj.receiverPhone1.equals(" -Ext"))
					{
						appendDocTrail("RECIPIENT GEN LINE", " ", receiverPhone1);
					}
					else
						appendDocTrail("RECIPIENT GEN LINE", this.soObj.receiverPhone1, receiverPhone1);
				}
		}
		{ if(!this.soObj.receiverPhone2.equals("") && !receiverPhone2.equals(" -"))
				{
					if(this.soObj.receiverPhone2.equals(" -"))
						appendDocTrail("RECIPIENT DIR LINE", " ", receiverPhone2);
					else
						appendDocTrail("RECIPIENT DIR LINE", this.soObj.receiverPhone2, receiverPhone2);
				}
		}
		{ if(!this.soObj.receiverHandphone.equals("") && !receiverHandphone.equals(" -"))
				{
					if(this.soObj.receiverHandphone.equals(" -"))
						appendDocTrail("RECIPIENT MOBILE", " ", receiverHandphone);
					else
						appendDocTrail("RECIPIENT MOBILE", this.soObj.receiverHandphone, receiverHandphone);
				}
		}
		{ if(!this.soObj.receiverFax.equals("") && !receiverFax.equals(" -"))
				{
					if(this.soObj.receiverFax.equals(" -"))
						appendDocTrail("RECIPIENT FAX", " ", receiverFax);
					else
						appendDocTrail("RECIPIENT FAX", this.soObj.receiverFax, receiverFax);
				}
		}
		{ appendDocTrail("RECIPIENT COMPANY", this.soObj.receiverCompanyName, receiverCompanyName);}
		{ appendDocTrail("RECIPIENT BUILDING", this.soObj.receiverAdd1, receiverAdd1);}
		{ appendDocTrail("RECIPIENT UNIT/ST", this.soObj.receiverAdd2, receiverAdd2);}
		{ appendDocTrail("RECIPIENT AREA/TMN", this.soObj.receiverAdd3, receiverAdd3);}
		{ appendDocTrail("RECIPIENT CITY/TOWN", this.soObj.receiverCity, receiverCity);}
		{ appendDocTrail("RECIPIENT POSCODE", this.soObj.receiverZip, receiverZip);}
		{ appendDocTrail("RECIPIENT STATE", this.soObj.receiverState, receiverState);}
		{ appendDocTrail("RECIPIENT COUNTRY", this.soObj.receiverCountry, receiverCountry);}
		{ appendDocTrail("RECIPIENT LOCATION TYPE", this.soObj.receiverLocationType, receiverLocationType);}
		
		if(messageCardRequired)
			System.out.println("messageCardRequired is TRUE");
		else
			System.out.println("messageCardRequired is FALSE");

		{ appendDocTrail("DELIVERY-DATE-TIME", 
							TimeFormat.strDisplayDate(this.soObj.expDeliveryTimeStart)+" "+this.soObj.expDeliveryTime, 
							expDeliveryTimeStart + " " + expDeliveryTime);}
		
		this.soObj.deliveryTo = deliveryTo;
		this.soObj.deliveryToName = deliveryToName;
		this.soObj.deliveryFrom = deliveryFrom;
		this.soObj.deliveryFromName = deliveryFromName;
		this.soObj.deliveryMsg1 = deliveryMsg1;
		this.soObj.messageCardRequired = messageCardRequired; 
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
		this.soObj.receiverIdentityNumber = receiverIdentityNumber;
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
		//updateSalesOrder();
	}

	public boolean hasItems()
	{
		boolean rtn = true;
		Vector vecDocRow = new Vector(this.tableRows.values());
		if (vecDocRow.size() == 0)
		{
			return false;
		}
		return rtn;
	}

	public boolean hasInvoice()
	{
		if(this.soObj==null){ return false;}
		return (this.soObj.idInvoice.longValue() > 0);
	}

	public boolean hasReceipt()
	{
		if (this.vecReceipt.size() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public Vector getPreviousReceipts()
	{
		return this.vecReceipt;
	}

	public Vector getPreviousCreditMemoLink()
	{
		return this.vecCreditMemoLink;
	}

	public BigDecimal getPreviousReceiptAmt()
	{
		BigDecimal total = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < this.vecReceipt.size(); cnt1++)
		{
			OfficialReceiptObject orObj = (OfficialReceiptObject) this.vecReceipt.get(cnt1);
			total = total.add(orObj.getReceiptAmount());
		}
		return total;
	}

	public BigDecimal getPreviousCreditMemoLinkAmt()
	{
		BigDecimal total = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < this.vecCreditMemoLink.size(); cnt1++)
		{
			DocLinkObject dlObj = (DocLinkObject) vecCreditMemoLink.get(cnt1);
			total = dlObj.amount.negate();
		}
		return total;
	}

	public BigDecimal getPreviousOutstandingAmt()
	{
		return this.soObj.amount.subtract(getPreviousReceiptAmt()).subtract(getPreviousCreditMemoLinkAmt());
	}

	public BigDecimal getTempOutstandingAmt()
	{
		return getPreviousOutstandingAmt().subtract(this.receiptForm.getReceiptAmt());
	}

	public void loadReceipts()
	{
		this.vecReceipt.clear();
		this.vecCreditMemoLink.clear();
		Vector vecDoc = new Vector(DocLinkNut.getByTargetDoc(SalesOrderIndexBean.TABLENAME, this.soObj.pkid));
		for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
		{
			DocLinkObject dlObj = (DocLinkObject) vecDoc.get(cnt1);
			if (dlObj.srcDocRef.equals(OfficialReceiptBean.TABLENAME))
			{
				OfficialReceiptObject orObj = OfficialReceiptNut.getObject(dlObj.srcDocId);
				if (orObj != null)
				{
					this.vecReceipt.add(orObj);
				}
			}
			if (dlObj.srcDocRef.equals(CreditMemoIndexBean.TABLENAME))
			{
				this.vecCreditMemoLink.add(dlObj);
			}
		}
	}

	public boolean canCreateReceipt()
	{
		boolean rtn = true;
		// // CREATING RECEIPTS
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			this.receiptForm.setDisabled(true);
		} else
		{
			this.receiptForm.setDisabled(false);
		}
		if (this.receiptForm.canSave())
		{
			Log.printVerbose(" ReceiptForm can save : true");
		} else
		{
			Log.printVerbose(" ReceiptForm can save : false");
		}
		if (getTempOutstandingAmt().signum() < 0)
		{
			return false;
		}
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			return false;
		}
		if (this.receiptForm.getReceiptAmt().signum() <= 0)
		{
			return false;
		}
		if (!this.receiptForm.canSave())
		{
			return false;
		}
		return rtn;
	}

	public boolean canCreateCashsale()
	{
		if (!hasItems())
		{
			return false;
		}
		if (hasInvoice())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() != 0)
		{
			return false;
		}
		return true;
	}

	public boolean canCreateInvoice()
	{
		if (!hasItems())
		{
			return false;
		}
		if (hasInvoice())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() <= 0)
		{
			return false;
		}
		if(!this.soObj.approvalStatus.equals(SalesOrderIndexBean.STATUS_APPROVED))
		{
			return false;
		}
		for(int i = 0; i<this.soObj.vecItem.size();i++)
		{
			SalesOrderItemObject soiObj = (SalesOrderItemObject) soObj.vecItem.get(i);
			
			if(!soiObj.approvalStatus.equals(SalesOrderItemBean.STATUS_APPROVED))
			{
				return false;
			}
		}
		return true;
	}

	public boolean canCreateBill()
	{
		return canCreateInvoice() || canCreateCashsale();
	}

	public void fnAddStockWithItemCode(DocRow docr) throws Exception
	{
		System.out.println("Inside EditSalesOrderSession fnAddStockWithItemCode");
		
		// if invoice exists, do nothing
		if (hasInvoice())
		{
			return;
		}
		
		// check if this row exists in the list already
		Vector vecDocRow = new Vector(this.tableRows.values());
		if (docr.getItemId() == ItemBean.PKID_DELIVERY.intValue())
		{
			System.out.println("Inside EditSalesOrderSession AAAAAAAAAAAAAAAAA");
				
			for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt);
				if (docrow.getItemId() == ItemBean.PKID_DELIVERY.intValue())
				{
					// / remove from this.tableRows
					DocRow dcrowRemoved = (DocRow) this.tableRows.remove(docrow.getKey());
					// / remove from this.soObj.vecItem
					for (int cnt3 = 0; cnt3 < this.soObj.vecItem.size(); cnt3++)
					{
						SalesOrderItemObject soItemObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt3);
						if (soItemObj.pkid.longValue() == dcrowRemoved.getDocId())
						{
							this.soObj.vecItem.remove(cnt3);
							cnt3--;
						}
					}
					// / remove from EJB
					try
					{
						SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(dcrowRemoved.getDocId()));
						if (soItemEJB != null)
						{
							soItemEJB.remove();
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
		
		// add it
		try
		{
			SalesOrderItemObject soItemObj = new SalesOrderItemObject(this.soObj, docr);
			if (this.memCardObj == null)
			{
				soItemObj.crvGain = new BigDecimal(0);
			}
			SalesOrderItem soItemEJB = SalesOrderItemNut.fnCreate(soItemObj);
			docr.setDocId(soItemObj.pkid.longValue());
			this.soObj.vecItem.add(soItemObj);
			this.tableRows.put(docr.getKey(), docr);
			
			System.out.println("Inside EditSalesOrderSession.fnAddStockWithItemCode : docr.getKey()"+docr.getKey());
			
			{ appendDocTrail("ADD-ITEM", "", soItemObj.itemCode); }
			
			//updateSalesOrder();
			
		} catch (Exception ex)
		{
			dropDocRow(docr.getKey());
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	public void toggleProduction(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow != null)
		{
			docrow.productionRequired = !docrow.productionRequired;
		}
		try
		{
			Long soItemPkid = new Long(docrow.getDocId());
			// / update EJB
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			soItemObj.productionRequired = docrow.productionRequired;
			soItemEJB.setObject(soItemObj);
			// / update valueObjects
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.pkid.longValue() == docrow.getDocId())
				{
					soiObj.productionRequired = docrow.productionRequired;
				}
			}
			// // update the parent valueObject and the EJB
			this.soObj.processDelivery = false;
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.productionRequired)
				{
					this.soObj.processProduction = true;
				}
			}
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setProcessProduction(this.soObj.processProduction);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void toggleDelivery(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow == null)
		{
			return;
		}
		try
		{
			Long soItemPkid = new Long(docrow.getDocId());
			// / update EJB
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			// // 1) check if it has been delivered, if delivered,
			// / cannot change status to false
			if (soItemObj.deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_CONFIRMED_DELIVERED))
			{
				return;
			}
			docrow.deliveryRequired = !docrow.deliveryRequired;
			soItemObj.deliveryRequired = docrow.deliveryRequired;
			soItemEJB.setObject(soItemObj);
			// / update valueObjects
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.pkid.longValue() == docrow.getDocId())
				{
					soiObj.deliveryRequired = docrow.deliveryRequired;
				}
			}
			// // update the parent valueObject and the EJB
			this.soObj.processDelivery = false;
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.deliveryRequired)
				{
					this.soObj.processDelivery = true;
				}
			}
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setProcessDelivery(this.soObj.processDelivery);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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

	public boolean hasCRV()
	{
		if (this.crvObj != null)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public boolean canCreateCRV()
	{
		if (this.memCardObj == null)
		{
			return false;
		}
		if (!hasInvoice())
		{
			return false;
		}
		if (hasCRV())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() != 0)
		{
			return false;
		}
		BigDecimal newCrvValue = getDocRowCRVGainAmount();
		if (newCrvValue.signum() <= 0)
		{
			return false;
		}
		return true;
	}

	public CashRebateVoucherObject getCashRebateVoucher()
	{
		return this.crvObj;
	}

	private synchronized CashRebateVoucherObject createCashRebateVoucher()
	{
		Log.printVerbose(".............. trying to create CashRebateVoucher................");
		if (!canCreateCRV())
		{
			return null;
		}
		Log.printVerbose("............. condition fulfilled ............................ ");
		CashRebateVoucherObject bufObj = populateCashRebateVoucher();
		try
		{
			if (this.memCardObj == null)
			{
				return null;
			} else
			{
				Log.printVerbose("............. membercard not null ............................ ");
				if (this.memCardObj.pointBalance == null)
				{
					Log.printVerbose(" POINT BALANCE == NULLLLLLLLLLLLLLL");
				}
				if (bufObj.voucherValue == null)
				{
					Log.printVerbose(" CRV VOUCHER VALUE == NULLLLLLLLLLLLLLL");
				}
				BigDecimal bdTotalCRV = new BigDecimal(0);
				bdTotalCRV = this.memCardObj.pointBalance.add(bufObj.voucherValue);
				// / before creating the CRV, add the value to the
				// MemberCardObject.pointBalance
				// / if the value of the point Balance + this CRV exceeds
				// minimum amount RM1, then
				// / proceed to create the CRV
				Log.printVerbose("............. total CRV plus balance " + bdTotalCRV.toString());
				if (bdTotalCRV.compareTo(new BigDecimal("1.00")) < 0)
				{
					try
					{
						Log.printVerbose(".............  addding balance to balance .... "
								+ bufObj.voucherValue.toString());
						MemberCard memCardEJB = MemberCardNut.getHandle(memCardObj.pkid);
						this.memCardObj.pointBalance = this.memCardObj.pointBalance.add(bufObj.voucherValue);
						Log
								.printVerbose(".............  balance after .... "
										+ this.memCardObj.pointBalance.toString());
						memCardEJB.setObject(this.memCardObj);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				} else
				{
					// // if not, just don't create the CRV, add existing CRV as
					// accumulated values
					// /// inside CRV Balance
					CashRebateVoucher crvEJB = CashRebateVoucherNut.fnCreate(bufObj);
					BigDecimal pointBalance = this.memCardObj.pointBalance;
					this.crvObj = bufObj;
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
					// / check if there's membershipFee Due, if yes, deduct from
					// the CRV to be created.
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
								this.memCardObj.membershipFeeDue = this.memCardObj.membershipFeeDue.subtract(reduceAmt);
								this.memCardObj.membershipFeeLog = " Deducted from CRV No:" + crvObj.pkid.toString()
										+ " " + this.memCardObj.membershipFeeLog;
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
					this.popupPrintCRV = true;
				}
				return bufObj;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return bufObj;
	}

	public CashRebateVoucherObject populateCashRebateVoucher()
	{
		// // create a new CRV Object if it has not been created
		CashRebateVoucherObject tmpCrvObj = new CashRebateVoucherObject();
		// /// populate/update the CRV Object
		tmpCrvObj.branch = this.branch.pkid;
		tmpCrvObj.pcCenter = this.branch.accPCCenterId;
		tmpCrvObj.cardId = this.memCardObj.pkid;
		tmpCrvObj.cardNo = this.memCardObj.cardNo;
		tmpCrvObj.entityTable1 = CustAccountBean.TABLENAME;
		tmpCrvObj.entityKey1 = this.custAccObj.pkid;
		tmpCrvObj.nameDisplay = this.memCardObj.nameDisplay;
		tmpCrvObj.identityNumber = this.memCardObj.identityNumber;
		Timestamp tsToday = TimeFormat.getTimestamp();
		tmpCrvObj.dateValidFrom = TimeFormat.add(tsToday, 0, 0, this.branch.crvDayFrom.intValue());
		tmpCrvObj.dateGoodThru = TimeFormat.add(tmpCrvObj.dateValidFrom, 0, 0, this.branch.crvDayTo.intValue());
		tmpCrvObj.cardType = this.memCardObj.cardType;
		tmpCrvObj.remarks = "";
		tmpCrvObj.info1 = "";
		tmpCrvObj.info2 = "";
		tmpCrvObj.voucherValue = getDocRowCRVGainAmount();
		tmpCrvObj.voucherBalance = tmpCrvObj.voucherValue;
		tmpCrvObj.pointBonus = new BigDecimal(0);
		tmpCrvObj.srcTable1 = SalesOrderIndexBean.TABLENAME;
		tmpCrvObj.srcKey1 = this.soObj.pkid;
		if (hasReceipt())
		{
			tmpCrvObj.srcTable2 = OfficialReceiptBean.TABLENAME;
			OfficialReceiptObject tmpORObj = (OfficialReceiptObject) this.vecReceipt.get(this.vecReceipt.size() - 1);
			tmpCrvObj.srcKey2 = tmpORObj.pkid;
		}
		// tmpCrvObj.tgtTable1 = "";
		// tmpCrvObj.tgtKey1 = new Long(0);
		// tmpCrvObj.tgtTable2 = "";
		// tmpCrvObj.tgtKey2 = new Long(0);
		tmpCrvObj.dateCreate = TimeFormat.getTimestamp();
		tmpCrvObj.dateEdit = TimeFormat.getTimestamp();
		// tmpCrvObj.dateUse = TimeFormat.getTimestamp();
		tmpCrvObj.userCreate = this.userId;
		tmpCrvObj.userEdit = this.userId;
		tmpCrvObj.userUse = this.userId;
		tmpCrvObj.state = CashRebateVoucherBean.STATE_CREATED;
		tmpCrvObj.status = CashRebateVoucherBean.STATUS_ACTIVE;
		return tmpCrvObj;
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
		if (hasInvoice())
		{
			return null;
		}
		DocRow drow = (DocRow) this.tableRows.remove(key);
		try
		{
			{ appendDocTrail("DELETE-ITEM", "", drow.itemCode); }
			
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(drow.getDocId()));
			soItemEJB.remove();
			this.soObj.removeItem(new Long(drow.getDocId()));
			updateSalesOrder();
		} catch (Exception ex)
		{
		}
		return drow;
	}

	public void dropAllDocRow()
	{
		Vector vecDocRow = new Vector(this.tableRows.values());
		for(int cnt1=0;cnt1<vecDocRow.size();cnt1++)
		{
			try
			{
				DocRow drow = (DocRow) vecDocRow.get(cnt1);
				SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(drow.getDocId()));
				soItemEJB.remove();
				this.soObj.removeItem(new Long(drow.getDocId()));
				updateSalesOrder();
			}
			catch(Exception ex)
			{ ex.printStackTrace();}	
		}
	}

	public synchronized void createInvoice() throws Exception
	{
		String result = "";
		boolean creditTermsStandIn = false; // remove this and replace them with the actual integrity functions later

/*		if(!CreditTermsRulesetNut.checkCreditTermsOk(custAccObj.pkid))
		{
			result = STATUS_CREDIT_TERMS_OUTSTANDING;
		}*/
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
		else if(!this.soObj.approvalStatus.equals(SalesOrderIndexBean.STATUS_APPROVED))
		{
			result = STATUS_GENERIC_APPROVAL_REQUIRED;
		}else 
		{
			Vector vecItem = this.soObj.vecItem;
			Log.printVerbose("vecItem.size(): " + vecItem.size());
			for(int i=0;i<vecItem.size();i++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) vecItem.get(i);
				if(!soiObj.approvalStatus.equals(SalesOrderItemBean.STATUS_APPROVED))
				{
					result = STATUS_ITEM_PRICE_APPROVAL_REQUIRED;
				}
			}			
		}

		if(!result.equals(""))
		{
			throw new Exception(result);
		}
		
		if (!canCreateInvoice() && !canCreateCashsale())
		{
			return;
		}
		if (canCreateReceipt())
		{
			createReceipt();
		}
		if(!checkInventoryQty(this.branch.invLocationId).equals("#GOOD#"))
		{
			throw new Exception("Item code " + checkInventoryQty(this.branch.invLocationId) + " does not have sufficient quantity.");
		}
		createInvDocLink();
		// / 4) create new CRV if valid
		// / populate the CRV with SalesOrder PKID and Receipt PKID
		createCashRebateVoucher();
	}

	public void updateContact()
	{
		if (this.custUserObj == null)
		{
			return;
		}
		
		this.custUserObj = CustUserNut.getObject(this.custUserObj.pkid);
		this.soObj.thirdpartyLoyaltyCardCode = this.custUserObj.loyaltyCardName1;
		//this.soObj.thirdpartyLoyaltyCardNumber = this.custUserObj.loyaltyCardNumber1;
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderTable2 = CustUserBean.TABLENAME;
		this.soObj.senderKey2 = this.custUserObj.pkid;
		this.soObj.senderName = custUserObj.nameFirst + " " + custUserObj.nameLast;
		this.soObj.senderIdentityNumber = custUserObj.referenceNo;
		this.soObj.senderHandphone = custUserObj.mobilePhone;
		this.soObj.senderFax = custUserObj.faxNo;
		this.soObj.senderPhone1 = custUserObj.telephone1;
		this.soObj.senderPhone2 = custUserObj.telephone2;
		// this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderCompanyName = this.custAccObj.name;
		this.soObj.senderAdd1 = custUserObj.mainAddress1;
		this.soObj.senderAdd2 = custUserObj.mainAddress2;
		this.soObj.senderAdd3 = custUserObj.mainAddress3;
		// this.soObj.senderCity = senderCity;
		this.soObj.senderZip = custUserObj.mainPostcode;
		this.soObj.senderState = custUserObj.mainState;
		this.soObj.senderCountry = custUserObj.mainCountry;
		// this.soObj.receiverCity = receiverCity;
		this.soObj.receiverZip = this.soObj.senderZip;
		this.soObj.receiverState = this.soObj.senderState;
		this.soObj.receiverCountry = this.soObj.senderCountry;
		this.soObj.etxnAccount = custUserObj.defaultCardNumber;
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
		
		System.out.println("inside update contact");
		
	}

	public Vector getReceiptList()
	{
		Vector result = new Vector();
				
		QueryObject qry = new QueryObject(new String[] {DocLinkBean.RELTYPE + " = '" + DocLinkBean.RELTYPE_PYMT_SO + "'",
		                                              DocLinkBean.TGT_DOCREF + " = '" + SalesOrderIndexBean.TABLENAME + "'",
		                                              DocLinkBean.SRC_DOCREF + " = '" + OfficialReceiptBean.TABLENAME + "'",
		                                              DocLinkBean.TGT_DOCID + " = '" + this.soObj.pkid + "'"});
		result = ((Vector) DocLinkNut.getObjects(qry));
		
		return result;
	}
	
	public BigDecimal getReceiptAmount(Vector vecVal)
	{
		BigDecimal result = new BigDecimal(0);
		for(int i = 0; i < vecVal.size(); i++)
		{
			DocLinkObject obj = (DocLinkObject) vecVal.get(i);
			result = result.add(obj.amount);
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
		// TKW20080523: Done so the previous receipts get their outstanding amounts removed from invoice outstanding amount.
		invoiceObj.mOutstandingAmt = invoiceObj.mOutstandingAmt.add(getReceiptAmount(getReceiptList()));
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
		//invoiceObj.mLocationId = this.branch.invLocationId;
		invoiceObj.mLocationId = this.productionLocation.pkid;
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
			invoiceObj.mDisplayFormat = this.branch.formatInvoiceType;
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
				StockNut.sell(iiObj.mPic2, iiObj.mItemId,// Integer itemId,
						invoiceObj.mLocationId, invoiceObj.mPCCenter, iiObj.mTotalQty, iiObj.mUnitPriceQuoted,
						iiObj.mCurrency, InvoiceItemBean.TABLENAME, iiObj.mPkid, iiObj.mRemarks, // remarks
						invoiceObj.mTimeIssued, invoiceObj.mUserIdUpdate, new Vector(iiObj.colSerialObj), "", "", "", "", invoiceObj.mCustSvcCtrId);
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
			this.soObj.idInvoice = invoiceObj.mPkid;
			updateSalesOrder();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		// / check if receipt amount > 0
		// / create doc link
		if (hasReceipt())
		{
			for (int cnt1 = 0; cnt1 < this.vecReceipt.size(); cnt1++)
			{
				OfficialReceiptObject receiptObj = (OfficialReceiptObject) this.vecReceipt.get(cnt1);
				// /////////// MUST CREATE THE DOC LINK BEAN HERE!!!!
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
				DocLink dlEJB = DocLinkNut.fnCreate(dlObj);
			}
		}
		updateSalesOrder();
	}

	private void initializeReceiptForm()
	{
		this.receiptForm = new ReceiptForm(this.userId);
		this.receiptForm.setBranch(this.soObj.branch);
		this.receiptForm.setCustomer(this.custAccObj.pkid);
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			this.receiptForm.setDisabled(true);
		}
	}

	public synchronized OfficialReceiptObject createReceipt() throws Exception
	{
		BigDecimal bdOutstandingBefore = getPreviousOutstandingAmt();
		OfficialReceiptObject orObj = null;
		if (!canCreateReceipt())
		{
			return orObj;
		}
		// / CHECKING BUSINESS LOGIC
		// / 1) If delivery is required, check if the addresses etc are properly
		// filled in
		// / 2) Check the status of the CRV being consumed to make sure they are
		// still in valid state
		// / CREATE BASIC OBJECTS
		// / 1) do not create temporary invoice! invoice is to be created at a
		// later stage
		// / 2) create Receipt
		try
		{
			if (this.receiptForm != null && this.receiptForm.canSave())
			{
				this.receiptForm.confirmAndSave();
				// this.receiptObj = this.receiptForm.getReceipt();
				orObj = this.receiptForm.getReceipt();
				// this.soObj.idReceipt = this.receiptObj.pkid;
				// /todo: create the DocLink for Receipt -> Invoice and Receipt
				// -> Sales Order
				// / update the CRV consumed upon successful savings of Receipt
				// Object
				/*
				 * Vector vecCRVRedeemed = new
				 * Vector(this.redeemingList.values()); for(int cnt1=0;cnt1<vecCRVRedeemed.size();cnt1++) {
				 * CashRebateVoucherObject bufObj = (CashRebateVoucherObject)
				 * vecCRVRedeemed.get(cnt1); try { CashRebateVoucher bufEJB =
				 * CashRebateVoucherNut.getHandle(bufObj.pkid);
				 * bufObj.voucherBalance = new BigDecimal(0);
				 * bufEJB.setObject(bufObj); } catch(Exception ex) {
				 * ex.printStackTrace(); } }
				 */
				Set keySet = this.redeemingList.keySet();
				Iterator keyItr = keySet.iterator();
				int cnt1 = 0;
				while (keyItr.hasNext())
				{
					Long crvPkid = (Long) keyItr.next();
					BigDecimal redeemAmt = (BigDecimal) this.redeemingList.get(crvPkid);
					try
					{
						CashRebateVoucher bufEJB = CashRebateVoucherNut.getHandle(crvPkid);
						CashRebateVoucherObject bufObj = bufEJB.getObject();
						OfficialReceiptObject rctObj = this.receiptForm.getReceipt();
						bufObj.voucherBalance = bufObj.voucherBalance.subtract(redeemAmt);
						bufObj.usedAtBranch = branch.pkid;
						bufObj.usedAtPCCenter = branch.accPCCenterId;
						bufObj.usedTime = TimeFormat.getTimestamp();
						bufObj.tgtTable1 = OfficialReceiptBean.TABLENAME;
						bufObj.tgtKey1 = rctObj.pkid;
						bufObj.tgtTable2 = SalesOrderIndexBean.TABLENAME;
						bufObj.tgtKey2 = this.soObj.pkid;
						bufEJB.setObject(bufObj);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				clearRedeemingList();
			}
		} catch (Exception ex)
		{
			throw new Exception(ex);
		}
		initializeReceiptForm();
		// // CROSS POPULATE THE PKIDs into different objects
		// // create respective DOCLinks
		if (orObj != null)
		{
			DocLinkObject dlObj = new DocLinkObject();
			dlObj.namespace = DocLinkBean.NS_CUSTOMER;
			// dlObj.reference = "";
			dlObj.relType = DocLinkBean.RELTYPE_PYMT_SO;
			dlObj.srcDocRef = OfficialReceiptBean.TABLENAME;
			dlObj.srcDocId = orObj.pkid;
			dlObj.tgtDocRef = SalesOrderIndexBean.TABLENAME;
			dlObj.tgtDocId = this.soObj.pkid;
			dlObj.currency = this.soObj.currency;
			dlObj.amount = orObj.amount.negate();
			dlObj.currency2 = "";
			dlObj.amount2 = new BigDecimal("0.00");
			dlObj.remarks = "";
			dlObj.status = DocLinkBean.STATUS_ACTIVE;
			dlObj.lastUpdate = TimeFormat.getTimestamp();
			dlObj.userIdUpdate = this.userId;
			DocLink dlEJB = DocLinkNut.fnCreate(dlObj);
			if (hasInvoice())
			{
				DocLinkObject dlObj2 = new DocLinkObject();
				dlObj2.namespace = DocLinkBean.NS_CUSTOMER;
				// dlObj.reference = "";
				dlObj2.relType = DocLinkBean.RELTYPE_PYMT_INV;
				dlObj2.srcDocRef = OfficialReceiptBean.TABLENAME;
				dlObj2.srcDocId = orObj.pkid;
				dlObj2.tgtDocRef = InvoiceBean.TABLENAME;
				dlObj2.tgtDocId = this.soObj.idInvoice;
				dlObj2.currency = this.soObj.currency;
				dlObj2.amount = orObj.amount.negate();
				dlObj2.currency2 = "";
				dlObj2.amount2 = new BigDecimal("0.00");
				dlObj2.remarks = "";
				dlObj2.status = DocLinkBean.STATUS_ACTIVE;
				dlObj2.lastUpdate = TimeFormat.getTimestamp();
				dlObj2.userIdUpdate = this.userId;
				DocLink dlEJB2 = DocLinkNut.fnCreate(dlObj2);
			}
		}
		loadReceipts();
		// / 3) update the sales order object
		try
		{
			SalesOrderIndexObject soObjTmp = this.soObj;
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObjTmp.pkid);
			soObjTmp.amountOutstanding = getPreviousOutstandingAmt();
			this.soObj.amountOutstanding = getPreviousOutstandingAmt();
			
			soEJB.setObject(soObjTmp);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (hasInvoice())
		{
			try
			{
				Invoice invoiceEJB = InvoiceNut.getHandle(this.soObj.idInvoice);
				InvoiceObject invoiceObj = invoiceEJB.getObject();
				invoiceObj.mOutstandingAmt = getPreviousOutstandingAmt();
				invoiceEJB.setObject(invoiceObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		BigDecimal bdOutstandingAfter = getPreviousOutstandingAmt();
		if (bdOutstandingAfter.signum() == 0 && bdOutstandingBefore.signum() > 0)
		{
			createCashRebateVoucher();
		}
		return orObj;
	}

	public String getAlertMessage()
	{
		String alertMsg = "";
		alertMsg+= "Stock Taken From:"+this.branch.description+"\\n ";

		if(requireProduction())
		{ alertMsg+= " The order REQUIRE Workshop!\\n"; }
		else
		{ alertMsg+= " The order DOES NOT need Workshop!\\n";}

		if(requireDelivery())
		{ alertMsg+= " The order REQUIRE Delivery!\\n"; }
		else
		{ alertMsg+= " The order DOES NOT need Delivery!\\n";}

		alertMsg+=" Payment Mode:"+this.soObj.receiptMode +" \\n";

//		alertMsg+= " Delivery Date:"+ TimeFormat.strDisplayDate(this.soObj.expDeliveryTimeStart)+" "+this.soObj.expDeliveryTime;

		return alertMsg;
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

	public void setOrderCreateTime(Timestamp tsBuffer)
	{
		if(this.soObj!=null)
		{
			this.soObj.timeCreate = tsBuffer;
		}
	}


	public void updateSalesOrder()
	{

		if(this.docTrail.length()>4 && this.soObj!=null)
		{
			System.out.println("Inside EditSalesOrderSession.updateSalesOrder docTrail : "+this.docTrail);
			
			DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
			dpiObj.processType = "ORDER-UPDATE";
			dpiObj.category = "UPDATE-DETAILS";
			if(this.soObj.printCountInvoice.intValue()>0 || this.soObj.printCountReceipt.intValue()>0	||
				this.soObj.printCountWorkshop.intValue()>0 || this.soObj.printCountDeliveryOrder.intValue()>0 ||
				this.soObj.printCountSalesOrder.intValue()>0)
			{
				dpiObj.processType = "ORDER-WARNING";
				dpiObj.category = "EDIT-PRINTED-ORDER";
			}
			dpiObj.auditLevel = new Integer(0);
			dpiObj.userid = this.userId;
			dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
			dpiObj.docId = this.soObj.pkid;
			dpiObj.description1 = this.docTrail;
			dpiObj.time = TimeFormat.getTimestamp();
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
			DocumentProcessingItemNut.fnCreate(dpiObj);
			this.docTrail = "";
		}


		UserObject userObj = UserNut.getObject(this.userId);
		this.soObj.amount = getBillAmount();
		this.soObj.amountOutstanding = getPreviousOutstandingAmt();
		this.soObj.timeUpdate = TimeFormat.getTimestamp();
		this.soObj.useridEdit = this.userId;
		this.soObj.processProduction = requireProduction();
		this.soObj.processDelivery = requireDelivery();
		if (this.soObj.thirdpartyLoyaltyCardCode.length() > 3)
		{
			this.soObj.thirdpartyLoyaltyCardPtsGain = this.soObj.amount;
		}
		
		System.out.println("this.soObj.pkid : "+this.soObj.pkid.toString());
		System.out.println("this.soObj.amount : "+this.soObj.amount.toString());
		
		try
		{
			SalesOrderIndexObject soObjTmp = this.soObj;
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObjTmp.pkid);
			soEJB.setObject(soObjTmp);
			
			Vector newVecItem = new Vector();
			for (int cnt2 = 0; cnt2 < this.soObj.vecItem.size(); cnt2++)
			{
				SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt2);
				Vector vecDocRow = new Vector(this.tableRows.values());
				for (int cnt3 = 0; cnt3 < vecDocRow.size(); cnt3++)
				{
					DocRow docrow = (DocRow) vecDocRow.get(cnt3);
					if (soItmObj.pkid.equals(new Long(docrow.getDocId())))
					{
						SalesOrderItemObject soItmObjBuf = new SalesOrderItemObject(this.soObj, docrow);
						soItmObjBuf.pkid = soItmObj.pkid;
						SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
						soItmEJB.setObject(soItmObjBuf);
						newVecItem.add(soItmObjBuf);
					}
				}
			}
			this.soObj.vecItem = newVecItem;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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
		String theKey = "";
		try
		{
			BigDecimal redeemAmt = new BigDecimal(amount);
			if (redeemAmt.signum() == 0)
			{
				return;
			}
			lPkid = new Long(pkid);
			CashRebateVoucherObject cashRVObj = CashRebateVoucherNut.getObject(lPkid);
			theKey = TimeFormat.strDisplayDate(cashRVObj.dateGoodThru) + pkid;
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
		} catch (Exception ex)
		{
			return;
		}
	}

	public BigDecimal dropRedeemingCRV(String key)
	{
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.remove(lPkid);
	}

	public BigDecimal getRedeemingCRVAmount(String key)
	{
		// CashRebateVoucherObject cashRV = (CashRebateVoucherObject)
		// this.redeemingList.get(key);
		// if(cashRV!=null){ return cashRV.voucherBalance;}
		// else{ return (BigDecimal) null;}
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.get(lPkid);
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

	public void clearRedeemingList()
	{
		this.redeemingList.clear();
	}

	public void retrieveRedeemableCRV()
	{
		if (!getValidMemberCard())
		{
			return;
		}
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
		} else
		{
			this.redeemableCRV.clear();
		}
		for (int cnt1 = 0; cnt1 < vecRedeemableCRV.size(); cnt1++)
		{
			CashRebateVoucherObject bufObj = (CashRebateVoucherObject) vecRedeemableCRV.get(cnt1);
			this.redeemableCRV.put(TimeFormat.strDisplayDate(bufObj.dateGoodThru) + bufObj.pkid.toString(), bufObj);
		}
		this.redeemingList.clear();
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

	public String getDescription()
	{
		return this.soObj.description;
	}

	public void setDescription(String description)
	{
		if (description != null)
		{
			{ appendDocTrail("DESCRIPTION", this.soObj.description, description);}
			this.soObj.description = description;
		}
	}

	public void setPromo(String promoType, String promoCode, String promoNumber, 
		String promoName, BigDecimal promoDiscountAmount, BigDecimal promoDiscountPct)
	{
		{ appendDocTrail("PROMO TYPE", this.soObj.promoType, promoType);}
		{ appendDocTrail("PROMO CODE", this.soObj.promoCode, promoCode);}
		{ appendDocTrail("PROMO NUMBER", this.soObj.promoNumber, promoNumber);}
		{ appendDocTrail("PROMO DISCOUNT", CurrencyFormat.strCcy(this.soObj.promoDiscountAmount),CurrencyFormat.strCcy(promoDiscountAmount));}
		{ appendDocTrail("PROMO DISCOUNT %", CurrencyFormat.strCcy(this.soObj.promoDiscountPct),CurrencyFormat.strCcy(promoDiscountPct));}
		this.soObj.promoType = promoType;
		this.soObj.promoCode = promoCode;
		this.soObj.promoNumber = promoNumber;
		this.soObj.promoName = promoName;
		this.soObj.promoDiscountAmount = promoDiscountAmount;
		this.soObj.promoDiscountPct = promoDiscountPct;
	}

	public void fnRecordInDocTrail(String category, Integer userId, Long docId, String paramName, String oldValue, String newValue)
	{		
		DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
		dpiObj.processType = "ORDER-UPDATE";
		dpiObj.category = category;
		dpiObj.auditLevel = new Integer(1);
		dpiObj.userid = userId;
		dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
		dpiObj.docId = docId;
		dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail(paramName,oldValue,newValue,"");
		dpiObj.time = TimeFormat.getTimestamp();
		dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
		dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
		DocumentProcessingItemNut.fnCreate(dpiObj);
	}
	
	public Integer getAuthorityLevel(Integer roleId)
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
	
	public void setIndexApproval(Integer pkid, String status, Integer userId) throws Exception
	{
		SalesOrderIndexObject val = SalesOrderIndexNut.getObject(new Long(pkid.intValue()));
		// TKW20080407: Stop approval change if current user does not have permission to override last approver. 
		Integer roleId = UserRoleNut.getRoleId(userId);
		if(getAuthorityLevel(roleId).compareTo(val.approverLevel)<0)
		{
			throw new Exception("You do not have the necessary authority level to override the last approver.");
		}
		val.approvalStatus = status;
		val.lastApproval = TimeFormat.getTimestamp();
		val.approverId = userId;		
		val.approverLevel = getAuthorityLevel(roleId);
		SalesOrderIndexNut.update(val);
	}
	
	public void setApproval(Integer pkid, String status, Integer userId) throws Exception
	{
		SalesOrderItemObject val = SalesOrderItemNut.getObject(new Long(pkid.intValue()));
		// TKW20080407: Stop approval change if current user does not have permission to override last approver. 
		Integer roleId = UserRoleNut.getRoleId(userId);
		if(getAuthorityLevel(roleId).compareTo(val.approverLevel)<0)
		{
			throw new Exception("You do not have the necessary authority level to override the last approver.");
		}
		val.approvalStatus = status;
		val.lastApproval = TimeFormat.getTimestamp();
		val.approverId = userId;
		val.approverLevel = getAuthorityLevel(roleId);
		SalesOrderItemNut.update(val);
		loadSalesOrder(this.soObj.pkid);
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
package com.vlee.bean.distribution;

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

/*---------------------------------------------------------
 BASIC RULES
 If Receipt Has Already Been Created
 1) CANNOT Change/edit the memberCard/User/CustAccount
 2) CANNOT remove Sales Order Item
 3) CAN add Sales Order Item
 4) Cannot Change Receipt Details
 5) Recheck the CRV to see if it is still valid
 
 If Receipt Has Not Been Created
 1) Can add/remove Sales Order Items
 2) Can set receipt/payment info
 3) Able to create receipt, and respective CRV if amount 
 matches the SalesOrder bill amount

 SalesOrder Cancellation
 1) Cancel the CRV if it has already been created
 2) Lead user to SalesReturn use-case if invoice has already been created
 3) Reset the state of the sales order items

 -----------------------------------------------------------*/
public class EditSalesOrderSession extends java.lang.Object implements Serializable
{
	boolean popupPrintCRV = false;
	SalesOrderIndexObject soObj = null;
	BranchObject branch = null;
	LocationObject productionLocation = null;
	Integer userId = null;
	CustAccountObject custAccObj = null;
	CustUserObject custUserObj = null;
	MemberCardObject memCardObj = null;
	ReceiptForm receiptForm = null;
	CashRebateVoucherObject crvObj = null;
	Vector vecReceipt = null;
	Vector vecCreditMemoLink = null;
	public String soType2 = "";
	public String salesmanCode = "";
	protected TreeMap tableRows = null; // // this is used for SalesOrderItems
	protected TreeMap redeemableCRV = null;
	protected TreeMap redeemingList = null;
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
	public String docTrail = "";


	// // constructor
	public EditSalesOrderSession(Integer userId)
	{
		this.userId = userId;
		this.redeemableCRV = new TreeMap();
		this.redeemingList = new TreeMap();
		this.vecReceipt = new Vector();
		this.vecCreditMemoLink = new Vector();
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		this.popupPrintCRV = false;
		UserObject userObj = UserNut.getObject(this.userId);
		this.salesmanCode = userObj.userName;
		this.docTrail = "";
	}

	public void appendDocTrail(String paramName, String oldValue, String newValue)
	{
		this.docTrail = DocumentProcessingItemNut.appendDocTrail(paramName,oldValue,newValue, this.docTrail);
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
		// // reload the original sales order and auxiliary objects
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		this.popupPrintCRV = false;
		this.docTrail = "";
	}

	public void setPopupPrintCRV(boolean buf)
	{
		this.popupPrintCRV = buf;
	}

	public boolean getPopupPrintCRV()
	{
		return this.popupPrintCRV;
	}

	public void setOrderType2(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.soType2 = SalesOrderIndexBean.SO_TYPE2_FLORIST;
		}
	}

	public void setOrderType1(String buf)
	{
		if(this.soObj !=null)
		{
			appendDocTrail(" ORDER TYPE ", this.soObj.soType1, buf);
			this.soObj.soType1 = buf;
		}
	}

	public void setDeliveryLocation(Long dlPkid, String option) throws Exception
	{
		DeliveryLocationObject dlObj = DeliveryLocationNut.getObject(dlPkid);

		if (dlObj == null || option == null)
		{
			return;
		}
		if (option.equals("country"))
		{
			appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);
			this.soObj.receiverCountry = dlObj.country;
		}
		if (option.equals("state"))
		{
			appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);
			appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
		}
		if(option.equals("zip"))
		{
			appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);
			appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
		}
		if(option.equals("city"))
		{

			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
		}
		if (option.equals("area"))
		{
			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
		}
		if (option.equals("street"))
		{

			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RecipientStreet",this.soObj.receiverAdd2,dlObj.street);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
		}
		if (option.equals("building"))
		{
			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RecipientStreet",this.soObj.receiverAdd2,dlObj.street);}
			{ appendDocTrail("RecipientBuilding",this.soObj.receiverAdd1,dlObj.building);}


			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
			this.soObj.receiverAdd1 = dlObj.building;
		}
		if (option.equals("company"))
		{
			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RecipientStreet",this.soObj.receiverAdd2,dlObj.street);}
			{ appendDocTrail("RecipientBuilding",this.soObj.receiverAdd1,dlObj.building);}
			{ appendDocTrail("RecipientCompany",this.soObj.receiverCompanyName,dlObj.name);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
			this.soObj.receiverAdd1 = dlObj.building;
			this.soObj.receiverCompanyName = dlObj.name;
		}
		

		if(dlObj.deliveryRate1.signum()>0)
		{
			ItemObject itmObj = ItemNut.getObject(ItemBean.PKID_DELIVERY);
			DocRow docrow = new DocRow();
			// docrow.setTemplateId(0)
			docrow.setItemType(ItemBean.CODE_DELIVERY);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(new BigDecimal("1"));
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(dlObj.deliveryRate1);
			// docrow.setDiscount(new
			// BigDecimal(req.getParameter("itemUnitDiscount")));
			docrow.user1 = userId.intValue();
			docrow.setRemarks("");
			docrow.setCcy2("");
			docrow.setPrice2(new BigDecimal(0));
			docrow.setProductionRequired(itmObj.productionRequired);
			docrow.setDeliveryRequired(itmObj.deliveryRequired);
			fnAddStockWithItemCode(docrow);
		} else
		{
			// check for this row in the list
			Vector vecDocRow = new Vector(this.tableRows.values());
			for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt);
				if (docrow.getItemId() == ItemBean.PKID_DELIVERY.intValue())
				{
					// / remove from this.tableRows
					DocRow dcrowRemoved = (DocRow) this.tableRows.remove(docrow.getKey());
					// / remove from this.soObj.vecItem
					for (int cnt3 = 0; cnt3 < this.soObj.vecItem.size(); cnt3++)
					{
						SalesOrderItemObject soItemObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt3);
						if (soItemObj.pkid.longValue() == dcrowRemoved.getDocId())
						{
							this.soObj.vecItem.remove(cnt3);
							cnt3--;
						}
					}
					// / remove from EJB
					try
					{
						SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(dcrowRemoved.getDocId()));
						if (soItemEJB != null)
						{
							soItemEJB.remove();
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
	}

	public void loadSalesOrder(Long soPkid) throws Exception
	{
		reset();
		this.soObj = SalesOrderIndexNut.getObjectTree(soPkid);
		if (this.soObj == null)
		{
			throw new Exception("Invalid Sales Order Number!");
		}
		// / populate this into the DocRows
		this.tableRows = this.soObj.getDocRows();

		// / populate other member objects of this Form/Session
		this.branch = BranchNut.getObject(this.soObj.branch);

		Log.printVerbose("Branch: "+this.branch);

		this.productionLocation = LocationNut.getObject(this.soObj.productionLocation);

		Log.printVerbose("ProductionLocation: "+this.productionLocation);

		this.custAccObj = CustAccountNut.getObject(this.soObj.senderKey1);

		if (this.soObj.senderKey2.intValue() > 0)
		{
			this.custUserObj = CustUserNut.getObject(this.soObj.senderKey2);
		} else
		{
			this.custUserObj = null;
		}
		if (this.soObj.senderLoyaltyCardNo.length() > 0)
		{
			this.memCardObj = MemberCardNut.getObjectByCardNo(this.soObj.senderLoyaltyCardNo);
		} else
		{
			this.memCardObj = null;
		}
		initializeReceiptForm();
		this.loadReceipts();
		retrieveRedeemableCRV();
		this.loadCRV();

		Log.printVerbose("End of Load SO");
	}

	private void loadCRV()
	{
		QueryObject query = new QueryObject(new String[] { CashRebateVoucherBean.SRC_KEY1 + " = '"
				+ this.soObj.pkid.toString() + "' " });
		query.setOrder(" ORDER BY " + CashRebateVoucherBean.PKID);
		Vector vecCRV = new Vector(CashRebateVoucherNut.getObjects(query));
		if (vecCRV.size() > 0)
		{
			this.crvObj = (CashRebateVoucherObject) vecCRV.get(vecCRV.size() - 1);
		} else
		{
			this.crvObj = null;
		}
	}

	public ReceiptForm getReceiptForm()
	{
		return this.receiptForm;
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
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

	public void setBranch(Integer iBranch)
	{
		if (hasInvoice())
		{
			return;
		}
		try
		{
			BranchObject brhObj = BranchNut.getObject(iBranch);
			if (brhObj != null)
			{
				if(!brhObj.pkid.equals(this.branch.pkid))
				{ appendDocTrail("BRANCH", this.branch.code, brhObj.code);}
				this.branch = brhObj;
				if (!hasInvoice())
				{
					this.soObj.branch = iBranch;
					this.soObj.receiptBranch = iBranch;
					this.soObj.productionBranch = iBranch;
					this.soObj.productionLocation = this.branch.invLocationId;
				}
				this.receiptForm.setBranch(this.soObj.branch);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public LocationObject getProductionLocation()
	{ return this.productionLocation;}

	public String getProductionLocation(String buf)
	{
		if(this.productionLocation ==null)
		{ return buf; }
		else
		{ return this.productionLocation.pkid.toString(); }
	}

	public void setProductionLocation(Integer iLocation)
	{
		if(hasInvoice())
		{ return ;}
		try
		{
			LocationObject locObj = LocationNut.getObject(iLocation);
			if(locObj!=null)
			{
				if(!locObj.pkid.equals(this.productionLocation.pkid))
				{	
					appendDocTrail("SHIP FROM", this.productionLocation.locationCode,locObj.locationCode);
				}
				this.productionLocation = locObj;
				this.soObj.productionLocation = this.productionLocation.pkid;
			}
		}
		catch(Exception ex)
		{ ex.printStackTrace();}
	}

	public boolean canChangeAccount()
	{
		boolean rtnValue = !hasInvoice() && !hasReceipt();
		return rtnValue;
	}

	public void setReceiptInfo(String receiptRemarks, String receiptApprovalCode, Integer receiptBranch)
	{
		if (this.soObj != null)
		{
			{ appendDocTrail("RCT Remarks", this.soObj.receiptRemarks,receiptRemarks);}
			{ appendDocTrail("RCT APPROVAL CODE",this.soObj.receiptApprovalCode,receiptApprovalCode);}
			if(!this.soObj.receiptBranch.equals(receiptBranch))
			{ 
				BranchObject brh1 = BranchNut.getObject(this.soObj.receiptBranch);
				BranchObject brh2 = BranchNut.getObject(receiptBranch);
				if(brh1!=null && brh2!=null)
				{
					appendDocTrail("PAY AT", brh1.code, brh2.code);
				}
			}

			// this.soObj.receiptMode = receiptMode;
			this.soObj.receiptRemarks = receiptRemarks;
			this.soObj.receiptApprovalCode = receiptApprovalCode;
			this.soObj.receiptBranch = receiptBranch;
		}



	}

	public void setReceiptMode(String buf)
	{
		if (this.soObj != null)
		{
			{ appendDocTrail("PAY MODE", this.soObj.receiptMode, buf);}
			this.soObj.receiptMode = buf;
		}
	}

	public void setDisplayFormat(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.displayFormat = buf;
		}
	}

	public void setCustomer(CustAccountObject custObj)
	{
		if (!canChangeAccount())
		{
			return;
		}
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
	}

	public void setCustUser(CustUserObject custUser)
	{
		this.memCardObj = null;
		this.custUserObj = custUser;
		if (!canChangeAccount())
		{
			if (!this.custUserObj.accId.equals(this.custAccObj.pkid))
			{
				return;
			}
		}
		this.custAccObj = CustAccountNut.getObject(this.custUserObj.accId);

      //this.soObj.thirdpartyLoyaltyCardCode = this.custUserObj.loyaltyCardName1;
      //this.soObj.thirdpartyLoyaltyCardNumber = this.custUserObj.loyaltyCardNumber1;
		
      this.soObj.senderTable1 = CustAccountBean.TABLENAME;
      this.soObj.senderKey1 = this.custAccObj.pkid;

      this.soObj.senderTable2 = CustUserBean.TABLENAME;
      this.soObj.senderKey2 = this.custUserObj.pkid;

      this.soObj.senderName = custUser.nameFirst + " "+custUser.nameLast;
      this.soObj.senderIdentityNumber = custUser.referenceNo;
      this.soObj.senderHandphone = custUser.mobilePhone;
      this.soObj.senderFax = custUser.faxNo;
      this.soObj.senderPhone1 = custUser.telephone1;
      this.soObj.senderPhone2 = custUser.telephone2;
//      this.soObj.senderInternetNo = senderInternetNo;
      this.soObj.senderCompanyName = this.custAccObj.name;
      this.soObj.senderAdd1 = custUser.mainAddress1;
      this.soObj.senderAdd2 = custUser.mainAddress2;
      this.soObj.senderAdd3 = custUser.mainAddress3;
//      this.soObj.senderCity = senderCity;
      this.soObj.senderZip = custUser.mainPostcode;
      this.soObj.senderState = custUser.mainState;
      this.soObj.senderCountry = custUser.mainCountry;

		if(this.custAccObj.state.equals(CustAccountBean.STATE_BL))
		{ this.soObj.flagInternalBool = true; }
		else { this.soObj.flagInternalBool = false;}

      this.receiptForm.setCustomer(custAccObj.pkid);
      this.receiptForm.setCCDetails(this.custUserObj.defaultCardNumber,
                                    this.custUserObj.defaultCardType,
                                    this.custUserObj.defaultCardName,
                                    this.custUserObj.defaultCardBank,
												"",
                                    this.custUserObj.defaultCardGoodThru,		
												this.custUserObj.defaultCardSecurityNum);
//    this.receiptForm.setCardType(custUser.defaultCardType);
      retrieveRedeemableCRV();

   }


	public void updateAccount()
	{
		this.custAccObj = CustAccountNut.getObject(this.custAccObj.pkid);
	}

	public void setFlagInternal(String buf)
	{
		{ appendDocTrail("INTERNAL REMARKS", this.soObj.flagInternal, buf);}
		this.soObj.flagInternal = buf;
	}

	public void setPaymentStatus(String buf)
	{
		{ appendDocTrail("PAY STATUS", this.soObj.statusPayment, buf);}
		this.soObj.statusPayment = buf;
	}

	public void setPaymentRemarks(String buf)
	{
		{ appendDocTrail("PAY REMARKS", this.soObj.receiptRemarks, buf);}
		this.soObj.receiptRemarks = buf;
	}

	public void setInfo(String remarks, boolean bRInvoice, boolean bRReceipt, String flagInternal, String flagSender,
			String flagReceiver, String managerPassword, String soType1, String occasion,
			String thirdpartyLoyaltyCardCode, String thirdpartyLoyaltyCardNumber, String paymentStatus, 
			BigDecimal interfloraPrice, String interfloraFlowers1)
	{
		Log.printVerbose(" SAVING DATA ... ");
		{ appendDocTrail( "RMKS", this.soObj.remarks1, remarks);}
		if(!this.soObj.requireInvoice==bRInvoice)
		{ appendDocTrail( "REQUIRE INVOICE:", ((this.soObj.requireInvoice)?"true":"false"), ((bRInvoice)?"true":"false"));}
		appendDocTrail("INTERNAL REMARKS", this.soObj.flagInternal, flagInternal);
		{ appendDocTrail("SENDER REMARKS", this.soObj.flagSender, flagSender);}
		{ appendDocTrail("RECIPIENT RMKS", this.soObj.flagReceiver, flagReceiver);}
		{ appendDocTrail("ORDER TYPE",this.soObj.soType1, soType1);}
		{ appendDocTrail("OCCASION", this.soObj.occasion,occasion);}
		{ appendDocTrail("LOYALTY CARD", this.soObj.thirdpartyLoyaltyCardCode, thirdpartyLoyaltyCardCode);}
		{ appendDocTrail("LOYALTY CARD NO", this.soObj.thirdpartyLoyaltyCardNumber, thirdpartyLoyaltyCardNumber);}
		{ appendDocTrail("PAY STATUS", this.soObj.statusPayment, paymentStatus);}
		{ appendDocTrail("INTERFLORA PRICE", CurrencyFormat.strCcy(this.soObj.interfloraPrice),CurrencyFormat.strCcy(interfloraPrice));}
		{ appendDocTrail("INTERFLORA FLOWERS", this.soObj.interfloraFlowers1, interfloraFlowers1);}

		this.soObj.remarks1 = remarks;
		this.soObj.requireInvoice = bRInvoice;
		this.soObj.requireReceipt = bRReceipt;
		this.soObj.flagInternal = flagInternal;
		this.soObj.flagSender = flagSender;
		this.soObj.flagReceiver = flagReceiver;
		this.soObj.soType1 = soType1;
		this.soObj.occasion = occasion;
		this.soObj.thirdpartyLoyaltyCardCode = thirdpartyLoyaltyCardCode;
		this.soObj.thirdpartyLoyaltyCardNumber = thirdpartyLoyaltyCardNumber;
		this.soObj.statusPayment = paymentStatus;
		this.soObj.interfloraPrice = interfloraPrice;
		this.soObj.interfloraFlowers1 = interfloraFlowers1;
		updateSalesOrder();
	}

	// // INTEGRATED SETTERS
	public void setDeliveryDetails(String deliveryTo, String deliveryToName, String deliveryFrom, //3
			String deliveryFromName, String deliveryMsg1, String expDeliveryTime, String expDeliveryTimeStart,//7
			String deliveryPreferences, String senderName, String senderIdentityNumber, String senderEmail, String senderHandphone,//12
			String senderFax, String senderPhone1, String senderPhone2, String senderInternetNo, //16
			String senderCompanyName, String senderAdd1, String senderAdd2, String senderAdd3, String senderCity, //21
			String senderZip, String senderState, String senderCountry, String receiverTitle, String receiverName,//26
			String receiverIdentityNumber, String receiverEmail, String receiverHandphone, String receiverFax, String receiverPhone1,//31
			String receiverPhone2, String receiverCompanyName, String receiverAdd1, String receiverAdd2,//35
			String receiverAdd3, String receiverCity, String receiverZip, String receiverState, String receiverCountry)//40
	{
		{ appendDocTrail("CARD TO", this.soObj.deliveryTo, deliveryTo);}
		{ appendDocTrail("CARD TO NAME", this.soObj.deliveryToName, deliveryToName);}
		{ appendDocTrail("CARD FROM", this.soObj.deliveryFrom, deliveryFrom);}
		{ appendDocTrail("CARD FROM NAME", this.soObj.deliveryFromName, deliveryFromName);}
		{ appendDocTrail("CARD MESSAGE", this.soObj.deliveryMsg1, deliveryMsg1);}
		{ appendDocTrail("DEL TIME", this.soObj.expDeliveryTime, expDeliveryTime);}
		{ appendDocTrail("DEL PREF", this.soObj.deliveryPreferences, deliveryPreferences);}

		{ appendDocTrail("SENDER NAME", this.soObj.senderName, senderName);}
		{ appendDocTrail("SENDER ID", this.soObj.senderIdentityNumber, senderIdentityNumber);}
		{ appendDocTrail("SENDER EMAIL", this.soObj.senderEmail, senderEmail);}
		{ appendDocTrail("SENDER H/P", this.soObj.senderHandphone, senderHandphone);}
		{ appendDocTrail("SENDER FAX", this.soObj.senderFax, senderFax);}
		{ appendDocTrail("SENDER PHONE1", this.soObj.senderPhone1, senderPhone1);}
		{ appendDocTrail("SENDER PHONE2", this.soObj.senderPhone2, senderPhone2);}
		{ appendDocTrail("SENDER INTERNET", this.soObj.senderInternetNo, senderInternetNo);}
		{ appendDocTrail("SENDER COMPANY", this.soObj.senderCompanyName, senderCompanyName);}
		{ appendDocTrail("SENDER ADD1", this.soObj.senderAdd1, senderAdd1);}
		{ appendDocTrail("SENDER ADD2", this.soObj.senderAdd2, senderAdd2);}
		{ appendDocTrail("SENDER ADD3", this.soObj.senderAdd3, senderAdd3);}
		{ appendDocTrail("SENDER CITY", this.soObj.senderCity, senderCity);}
		{ appendDocTrail("SENDER ZIP", this.soObj.senderZip, senderZip);}
		{ appendDocTrail("SENDER STATE", this.soObj.senderState, senderState);}
		{ appendDocTrail("SENDER COUNTRY", this.soObj.senderCountry, senderCountry);}

		{ appendDocTrail("RECIPIENT NAME", this.soObj.receiverName, receiverName);}
		{ appendDocTrail("RECIPIENT ID", this.soObj.receiverIdentityNumber, receiverIdentityNumber);}
		{ appendDocTrail("RECIPIENT EMAIL", this.soObj.receiverEmail, receiverEmail);}
		{ appendDocTrail("RECIPIENT H/P", this.soObj.receiverHandphone, receiverHandphone);}
		{ appendDocTrail("RECIPIENT FAX", this.soObj.receiverFax, receiverFax);}
		{ appendDocTrail("RECIPIENT PHONE1", this.soObj.receiverPhone1, receiverPhone1);}
		{ appendDocTrail("RECIPIENT PHONE2", this.soObj.receiverPhone2, receiverPhone2);}
		{ appendDocTrail("RECIPIENT COMPANY", this.soObj.receiverCompanyName, receiverCompanyName);}
		{ appendDocTrail("RECIPIENT ADD1", this.soObj.receiverAdd1, receiverAdd1);}
		{ appendDocTrail("RECIPIENT ADD2", this.soObj.receiverAdd2, receiverAdd2);}
		{ appendDocTrail("RECIPIENT ADD3", this.soObj.receiverAdd3, receiverAdd3);}
		{ appendDocTrail("RECIPIENT CITY", this.soObj.receiverCity, receiverCity);}
		{ appendDocTrail("RECIPIENT ZIP", this.soObj.receiverZip, receiverZip);}
		{ appendDocTrail("RECIPIENT STATE", this.soObj.receiverState, receiverState);}
		{ appendDocTrail("RECIPIENT COUNTRY", this.soObj.receiverCountry, receiverCountry);}



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
		this.soObj.receiverIdentityNumber = receiverIdentityNumber;
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
		updateSalesOrder();
	}

	public boolean hasItems()
	{
		boolean rtn = true;
		Vector vecDocRow = new Vector(this.tableRows.values());
		if (vecDocRow.size() == 0)
		{
			return false;
		}
		return rtn;
	}

	public boolean hasInvoice()
	{
		return (this.soObj.idInvoice.longValue() > 0);
	}

	public boolean hasReceipt()
	{
		if (this.vecReceipt.size() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public Vector getPreviousReceipts()
	{
		return this.vecReceipt;
	}

	public Vector getPreviousCreditMemoLink()
	{
		return this.vecCreditMemoLink;
	}

	public BigDecimal getPreviousReceiptAmt()
	{
		BigDecimal total = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < this.vecReceipt.size(); cnt1++)
		{
			OfficialReceiptObject orObj = (OfficialReceiptObject) this.vecReceipt.get(cnt1);
			total = total.add(orObj.getReceiptAmount());
		}
		return total;
	}

	public BigDecimal getPreviousCreditMemoLinkAmt()
	{
		BigDecimal total = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < this.vecCreditMemoLink.size(); cnt1++)
		{
			DocLinkObject dlObj = (DocLinkObject) vecCreditMemoLink.get(cnt1);
			total = dlObj.amount.negate();
		}
		return total;
	}

	public BigDecimal getPreviousOutstandingAmt()
	{
		return this.soObj.amount.subtract(getPreviousReceiptAmt()).subtract(getPreviousCreditMemoLinkAmt());
	}

	public BigDecimal getTempOutstandingAmt()
	{
		return getPreviousOutstandingAmt().subtract(this.receiptForm.getReceiptAmt());
	}

	public void loadReceipts()
	{
		this.vecReceipt.clear();
		this.vecCreditMemoLink.clear();
		Vector vecDoc = new Vector(DocLinkNut.getByTargetDoc(SalesOrderIndexBean.TABLENAME, this.soObj.pkid));
		for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
		{
			DocLinkObject dlObj = (DocLinkObject) vecDoc.get(cnt1);
			if (dlObj.srcDocRef.equals(OfficialReceiptBean.TABLENAME))
			{
				OfficialReceiptObject orObj = OfficialReceiptNut.getObject(dlObj.srcDocId);
				if (orObj != null)
				{
					this.vecReceipt.add(orObj);
				}
			}
			if (dlObj.srcDocRef.equals(CreditMemoIndexBean.TABLENAME))
			{
				this.vecCreditMemoLink.add(dlObj);
			}
		}
	}

	public boolean canCreateReceipt()
	{
		boolean rtn = true;
		// // CREATING RECEIPTS
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			this.receiptForm.setDisabled(true);
		} else
		{
			this.receiptForm.setDisabled(false);
		}
		if (this.receiptForm.canSave())
		{
			Log.printVerbose(" ReceiptForm can save : true");
		} else
		{
			Log.printVerbose(" ReceiptForm can save : false");
		}
		if (getTempOutstandingAmt().signum() < 0)
		{
			return false;
		}
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			return false;
		}
		if (this.receiptForm.getReceiptAmt().signum() <= 0)
		{
			return false;
		}
		if (!this.receiptForm.canSave())
		{
			return false;
		}
		return rtn;
	}

	public boolean canCreateCashsale()
	{
		if (!hasItems())
		{
			return false;
		}
		if (hasInvoice())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() != 0)
		{
			return false;
		}
		return true;
	}

	public boolean canCreateInvoice()
	{
		if (!hasItems())
		{
			return false;
		}
		if (hasInvoice())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() <= 0)
		{
			return false;
		}
		return true;
	}

	public boolean canCreateBill()
	{
		return canCreateInvoice() || canCreateCashsale();
	}

	public void fnAddStockWithItemCode(DocRow docr) throws Exception
	{
		// / if invoice exists, do nothing
		if (hasInvoice())
		{
			return;
		}
		// / check if this row exists in the list already
		Vector vecDocRow = new Vector(this.tableRows.values());
		if (docr.getItemId() == ItemBean.PKID_DELIVERY.intValue())
		{
			for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt);
				if (docrow.getItemId() == ItemBean.PKID_DELIVERY.intValue())
				{
					// / remove from this.tableRows
					DocRow dcrowRemoved = (DocRow) this.tableRows.remove(docrow.getKey());
					// / remove from this.soObj.vecItem
					for (int cnt3 = 0; cnt3 < this.soObj.vecItem.size(); cnt3++)
					{
						SalesOrderItemObject soItemObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt3);
						if (soItemObj.pkid.longValue() == dcrowRemoved.getDocId())
						{
							this.soObj.vecItem.remove(cnt3);
							cnt3--;
						}
					}
					// / remove from EJB
					try
					{
						SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(dcrowRemoved.getDocId()));
						if (soItemEJB != null)
						{
							soItemEJB.remove();
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
		// / add it
		try
		{
			SalesOrderItemObject soItemObj = new SalesOrderItemObject(this.soObj, docr);
			if (this.memCardObj == null)
			{
				soItemObj.crvGain = new BigDecimal(0);
			}
			SalesOrderItem soItemEJB = SalesOrderItemNut.fnCreate(soItemObj);
			docr.setDocId(soItemObj.pkid.longValue());
			this.soObj.vecItem.add(soItemObj);
			this.tableRows.put(docr.getKey(), docr);
			updateSalesOrder();
		} catch (Exception ex)
		{
			dropDocRow(docr.getKey());
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	public void toggleProduction(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow != null)
		{
			docrow.productionRequired = !docrow.productionRequired;
		}
		try
		{
			Long soItemPkid = new Long(docrow.getDocId());
			// / update EJB
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			soItemObj.productionRequired = docrow.productionRequired;
			soItemEJB.setObject(soItemObj);
			// / update valueObjects
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.pkid.longValue() == docrow.getDocId())
				{
					soiObj.productionRequired = docrow.productionRequired;
				}
			}
			// // update the parent valueObject and the EJB
			this.soObj.processDelivery = false;
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.productionRequired)
				{
					this.soObj.processProduction = true;
				}
			}
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setProcessProduction(this.soObj.processProduction);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void toggleDelivery(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow == null)
		{
			return;
		}
		try
		{
			Long soItemPkid = new Long(docrow.getDocId());
			// / update EJB
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			// // 1) check if it has been delivered, if delivered,
			// / cannot change status to false
			if (soItemObj.deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_DELIVERED))
			{
				return;
			}
			docrow.deliveryRequired = !docrow.deliveryRequired;
			soItemObj.deliveryRequired = docrow.deliveryRequired;
			soItemEJB.setObject(soItemObj);
			// / update valueObjects
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.pkid.longValue() == docrow.getDocId())
				{
					soiObj.deliveryRequired = docrow.deliveryRequired;
				}
			}
			// // update the parent valueObject and the EJB
			this.soObj.processDelivery = false;
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.deliveryRequired)
				{
					this.soObj.processDelivery = true;
				}
			}
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setProcessDelivery(this.soObj.processDelivery);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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

	public boolean hasCRV()
	{
		if (this.crvObj != null)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public boolean canCreateCRV()
	{
		if (this.memCardObj == null)
		{
			return false;
		}
		if (!hasInvoice())
		{
			return false;
		}
		if (hasCRV())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() != 0)
		{
			return false;
		}
		BigDecimal newCrvValue = getDocRowCRVGainAmount();
		if (newCrvValue.signum() <= 0)
		{
			return false;
		}
		return true;
	}

	public CashRebateVoucherObject getCashRebateVoucher()
	{
		return this.crvObj;
	}

	private synchronized CashRebateVoucherObject createCashRebateVoucher()
	{
		Log.printVerbose(".............. trying to create CashRebateVoucher................");
		if (!canCreateCRV())
		{
			return null;
		}
		Log.printVerbose("............. condition fulfilled ............................ ");
		CashRebateVoucherObject bufObj = populateCashRebateVoucher();
		try
		{
			if (this.memCardObj == null)
			{
				return null;
			} else
			{
				Log.printVerbose("............. membercard not null ............................ ");
				if (this.memCardObj.pointBalance == null)
				{
					Log.printVerbose(" POINT BALANCE == NULLLLLLLLLLLLLLL");
				}
				if (bufObj.voucherValue == null)
				{
					Log.printVerbose(" CRV VOUCHER VALUE == NULLLLLLLLLLLLLLL");
				}
				BigDecimal bdTotalCRV = new BigDecimal(0);
				bdTotalCRV = this.memCardObj.pointBalance.add(bufObj.voucherValue);
				// / before creating the CRV, add the value to the
				// MemberCardObject.pointBalance
				// / if the value of the point Balance + this CRV exceeds
				// minimum amount RM1, then
				// / proceed to create the CRV
				Log.printVerbose("............. total CRV plus balance " + bdTotalCRV.toString());
				if (bdTotalCRV.compareTo(new BigDecimal("1.00")) < 0)
				{
					try
					{
						Log.printVerbose(".............  addding balance to balance .... "
								+ bufObj.voucherValue.toString());
						MemberCard memCardEJB = MemberCardNut.getHandle(memCardObj.pkid);
						this.memCardObj.pointBalance = this.memCardObj.pointBalance.add(bufObj.voucherValue);
						Log
								.printVerbose(".............  balance after .... "
										+ this.memCardObj.pointBalance.toString());
						memCardEJB.setObject(this.memCardObj);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				} else
				{
					// // if not, just don't create the CRV, add existing CRV as
					// accumulated values
					// /// inside CRV Balance
					CashRebateVoucher crvEJB = CashRebateVoucherNut.fnCreate(bufObj);
					BigDecimal pointBalance = this.memCardObj.pointBalance;
					this.crvObj = bufObj;
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
					// / check if there's membershipFee Due, if yes, deduct from
					// the CRV to be created.
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
								this.memCardObj.membershipFeeDue = this.memCardObj.membershipFeeDue.subtract(reduceAmt);
								this.memCardObj.membershipFeeLog = " Deducted from CRV No:" + crvObj.pkid.toString()
										+ " " + this.memCardObj.membershipFeeLog;
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
					this.popupPrintCRV = true;
				}
				return bufObj;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return bufObj;
	}

	public CashRebateVoucherObject populateCashRebateVoucher()
	{
		// // create a new CRV Object if it has not been created
		CashRebateVoucherObject tmpCrvObj = new CashRebateVoucherObject();
		// /// populate/update the CRV Object
		tmpCrvObj.branch = this.branch.pkid;
		tmpCrvObj.pcCenter = this.branch.accPCCenterId;
		tmpCrvObj.cardId = this.memCardObj.pkid;
		tmpCrvObj.cardNo = this.memCardObj.cardNo;
		tmpCrvObj.entityTable1 = CustAccountBean.TABLENAME;
		tmpCrvObj.entityKey1 = this.custAccObj.pkid;
		tmpCrvObj.nameDisplay = this.memCardObj.nameDisplay;
		tmpCrvObj.identityNumber = this.memCardObj.identityNumber;
		Timestamp tsToday = TimeFormat.getTimestamp();
		tmpCrvObj.dateValidFrom = TimeFormat.add(tsToday, 0, 0, this.branch.crvDayFrom.intValue());
		tmpCrvObj.dateGoodThru = TimeFormat.add(tmpCrvObj.dateValidFrom, 0, 0, this.branch.crvDayTo.intValue());
		tmpCrvObj.cardType = this.memCardObj.cardType;
		tmpCrvObj.remarks = "";
		tmpCrvObj.info1 = "";
		tmpCrvObj.info2 = "";
		tmpCrvObj.voucherValue = getDocRowCRVGainAmount();
		tmpCrvObj.voucherBalance = tmpCrvObj.voucherValue;
		tmpCrvObj.pointBonus = new BigDecimal(0);
		tmpCrvObj.srcTable1 = SalesOrderIndexBean.TABLENAME;
		tmpCrvObj.srcKey1 = this.soObj.pkid;
		if (hasReceipt())
		{
			tmpCrvObj.srcTable2 = OfficialReceiptBean.TABLENAME;
			OfficialReceiptObject tmpORObj = (OfficialReceiptObject) this.vecReceipt.get(this.vecReceipt.size() - 1);
			tmpCrvObj.srcKey2 = tmpORObj.pkid;
		}
		// tmpCrvObj.tgtTable1 = "";
		// tmpCrvObj.tgtKey1 = new Long(0);
		// tmpCrvObj.tgtTable2 = "";
		// tmpCrvObj.tgtKey2 = new Long(0);
		tmpCrvObj.dateCreate = TimeFormat.getTimestamp();
		tmpCrvObj.dateEdit = TimeFormat.getTimestamp();
		// tmpCrvObj.dateUse = TimeFormat.getTimestamp();
		tmpCrvObj.userCreate = this.userId;
		tmpCrvObj.userEdit = this.userId;
		tmpCrvObj.userUse = this.userId;
		tmpCrvObj.state = CashRebateVoucherBean.STATE_CREATED;
		tmpCrvObj.status = CashRebateVoucherBean.STATUS_ACTIVE;
		return tmpCrvObj;
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
		if (hasInvoice())
		{
			return null;
		}
		DocRow drow = (DocRow) this.tableRows.remove(key);
		try
		{
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(drow.getDocId()));
			soItemEJB.remove();
			this.soObj.removeItem(new Long(drow.getDocId()));
			updateSalesOrder();
		} catch (Exception ex)
		{
		}
		return drow;
	}

	public synchronized void createInvoice() throws Exception
	{
		if (!canCreateInvoice() && !canCreateCashsale())
		{
			return;
		}
		if (canCreateReceipt())
		{
			createReceipt();
		}
		createInvDocLink();
		// / 4) create new CRV if valid
		// / populate the CRV with SalesOrder PKID and Receipt PKID
		createCashRebateVoucher();
	}

	public void updateContact()
	{
		if (this.custUserObj == null)
		{
			return;
		}
		this.custUserObj = CustUserNut.getObject(this.custUserObj.pkid);
		this.soObj.thirdpartyLoyaltyCardCode = this.custUserObj.loyaltyCardName1;
		//this.soObj.thirdpartyLoyaltyCardNumber = this.custUserObj.loyaltyCardNumber1;
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderTable2 = CustUserBean.TABLENAME;
		this.soObj.senderKey2 = this.custUserObj.pkid;
		this.soObj.senderName = custUserObj.nameFirst + " " + custUserObj.nameLast;
		this.soObj.senderIdentityNumber = custUserObj.referenceNo;
		this.soObj.senderHandphone = custUserObj.mobilePhone;
		this.soObj.senderFax = custUserObj.faxNo;
		this.soObj.senderPhone1 = custUserObj.telephone1;
		this.soObj.senderPhone2 = custUserObj.telephone2;
		// this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderCompanyName = this.custAccObj.name;
		this.soObj.senderAdd1 = custUserObj.mainAddress1;
		this.soObj.senderAdd2 = custUserObj.mainAddress2;
		this.soObj.senderAdd3 = custUserObj.mainAddress3;
		// this.soObj.senderCity = senderCity;
		this.soObj.senderZip = custUserObj.mainPostcode;
		this.soObj.senderState = custUserObj.mainState;
		this.soObj.senderCountry = custUserObj.mainCountry;
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
	}

	private synchronized void createInvDocLink()
	{
		// / first, create the invoice...
		// // set the outstanding balance = sales order amount - receipt amount
		// Populate Defaults
		// this.invoiceObj.mSalesTxnId = // automatically created when default
		// is zero
		// this.invoiceObj.mPaymentTermsId = pmtTerm;
		// this.invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		InvoiceObject invoiceObj = new InvoiceObject();
		invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		invoiceObj.mCurrency = this.branch.currency;
		invoiceObj.mTotalAmt = getBillAmount();
		invoiceObj.mOutstandingAmt = getBillAmount().subtract(this.receiptForm.getReceiptAmt());
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
		//invoiceObj.mLocationId = this.branch.invLocationId;
		invoiceObj.mLocationId = this.productionLocation.pkid;
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
			invoiceObj.mDisplayFormat = InvoiceBean.CASHBILL_TRADING_1;
			invoiceObj.mDocType = InvoiceBean.CASHBILL;
		} else if (canCreateInvoice())
		{
			invoiceObj.mDisplayFormat = InvoiceBean.INVOICE_TRADING_1;
			invoiceObj.mDocType = InvoiceBean.INVOICE;
		}
		Invoice invoiceEJB = InvoiceNut.fnCreate(invoiceObj);
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
				StockNut.sell(iiObj.mPic2, iiObj.mItemId,// Integer itemId,
						invoiceObj.mLocationId, invoiceObj.mPCCenter, iiObj.mTotalQty, iiObj.mUnitPriceQuoted,
						iiObj.mCurrency, InvoiceItemBean.TABLENAME, iiObj.mPkid, iiObj.mRemarks, // remarks
						invoiceObj.mTimeIssued, invoiceObj.mUserIdUpdate, new Vector(iiObj.colSerialObj));
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
			this.soObj.idInvoice = invoiceObj.mPkid;
			updateSalesOrder();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		// / check if receipt amount > 0
		// / create doc link
		if (hasReceipt())
		{
			for (int cnt1 = 0; cnt1 < this.vecReceipt.size(); cnt1++)
			{
				OfficialReceiptObject receiptObj = (OfficialReceiptObject) this.vecReceipt.get(cnt1);
				// /////////// MUST CREATE THE DOC LINK BEAN HERE!!!!
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
				DocLink dlEJB = DocLinkNut.fnCreate(dlObj);
			}
		}
		updateSalesOrder();
	}

	private void initializeReceiptForm()
	{
		this.receiptForm = new ReceiptForm(this.userId);
		this.receiptForm.setBranch(this.soObj.branch);
		this.receiptForm.setCustomer(this.custAccObj.pkid);
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			this.receiptForm.setDisabled(true);
		}
	}

	public synchronized OfficialReceiptObject createReceipt() throws Exception
	{
		BigDecimal bdOutstandingBefore = getPreviousOutstandingAmt();
		OfficialReceiptObject orObj = null;
		if (!canCreateReceipt())
		{
			return orObj;
		}
		// / CHECKING BUSINESS LOGIC
		// / 1) If delivery is required, check if the addresses etc are properly
		// filled in
		// / 2) Check the status of the CRV being consumed to make sure they are
		// still in valid state
		// / CREATE BASIC OBJECTS
		// / 1) do not create temporary invoice! invoice is to be created at a
		// later stage
		// / 2) create Receipt
		try
		{
			if (this.receiptForm != null && this.receiptForm.canSave())
			{
				this.receiptForm.confirmAndSave();
				// this.receiptObj = this.receiptForm.getReceipt();
				orObj = this.receiptForm.getReceipt();
				// this.soObj.idReceipt = this.receiptObj.pkid;
				// /todo: create the DocLink for Receipt -> Invoice and Receipt
				// -> Sales Order
				// / update the CRV consumed upon successful savings of Receipt
				// Object
				/*
				 * Vector vecCRVRedeemed = new
				 * Vector(this.redeemingList.values()); for(int cnt1=0;cnt1<vecCRVRedeemed.size();cnt1++) {
				 * CashRebateVoucherObject bufObj = (CashRebateVoucherObject)
				 * vecCRVRedeemed.get(cnt1); try { CashRebateVoucher bufEJB =
				 * CashRebateVoucherNut.getHandle(bufObj.pkid);
				 * bufObj.voucherBalance = new BigDecimal(0);
				 * bufEJB.setObject(bufObj); } catch(Exception ex) {
				 * ex.printStackTrace(); } }
				 */
				Set keySet = this.redeemingList.keySet();
				Iterator keyItr = keySet.iterator();
				int cnt1 = 0;
				while (keyItr.hasNext())
				{
					Long crvPkid = (Long) keyItr.next();
					BigDecimal redeemAmt = (BigDecimal) this.redeemingList.get(crvPkid);
					try
					{
						CashRebateVoucher bufEJB = CashRebateVoucherNut.getHandle(crvPkid);
						CashRebateVoucherObject bufObj = bufEJB.getObject();
						OfficialReceiptObject rctObj = this.receiptForm.getReceipt();
						bufObj.voucherBalance = bufObj.voucherBalance.subtract(redeemAmt);
						bufObj.usedAtBranch = branch.pkid;
						bufObj.usedAtPCCenter = branch.accPCCenterId;
						bufObj.usedTime = TimeFormat.getTimestamp();
						bufObj.tgtTable1 = OfficialReceiptBean.TABLENAME;
						bufObj.tgtKey1 = rctObj.pkid;
						bufObj.tgtTable2 = SalesOrderIndexBean.TABLENAME;
						bufObj.tgtKey2 = this.soObj.pkid;
						bufEJB.setObject(bufObj);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				clearRedeemingList();
			}
		} catch (Exception ex)
		{
			throw new Exception(ex);
		}
		initializeReceiptForm();
		// // CROSS POPULATE THE PKIDs into different objects
		// // create respective DOCLinks
		if (orObj != null)
		{
			DocLinkObject dlObj = new DocLinkObject();
			dlObj.namespace = DocLinkBean.NS_CUSTOMER;
			// dlObj.reference = "";
			dlObj.relType = DocLinkBean.RELTYPE_PYMT_SO;
			dlObj.srcDocRef = OfficialReceiptBean.TABLENAME;
			dlObj.srcDocId = orObj.pkid;
			dlObj.tgtDocRef = SalesOrderIndexBean.TABLENAME;
			dlObj.tgtDocId = this.soObj.pkid;
			dlObj.currency = this.soObj.currency;
			dlObj.amount = orObj.amount.negate();
			dlObj.currency2 = "";
			dlObj.amount2 = new BigDecimal("0.00");
			dlObj.remarks = "";
			dlObj.status = DocLinkBean.STATUS_ACTIVE;
			dlObj.lastUpdate = TimeFormat.getTimestamp();
			dlObj.userIdUpdate = this.userId;
			DocLink dlEJB = DocLinkNut.fnCreate(dlObj);
			if (hasInvoice())
			{
				DocLinkObject dlObj2 = new DocLinkObject();
				dlObj2.namespace = DocLinkBean.NS_CUSTOMER;
				// dlObj.reference = "";
				dlObj2.relType = DocLinkBean.RELTYPE_PYMT_INV;
				dlObj2.srcDocRef = OfficialReceiptBean.TABLENAME;
				dlObj2.srcDocId = orObj.pkid;
				dlObj2.tgtDocRef = InvoiceBean.TABLENAME;
				dlObj2.tgtDocId = this.soObj.idInvoice;
				dlObj2.currency = this.soObj.currency;
				dlObj2.amount = orObj.amount.negate();
				dlObj2.currency2 = "";
				dlObj2.amount2 = new BigDecimal("0.00");
				dlObj2.remarks = "";
				dlObj2.status = DocLinkBean.STATUS_ACTIVE;
				dlObj2.lastUpdate = TimeFormat.getTimestamp();
				dlObj2.userIdUpdate = this.userId;
				DocLink dlEJB2 = DocLinkNut.fnCreate(dlObj2);
			}
		}
		loadReceipts();
		// / 3) update the sales order object
		try
		{
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			this.soObj.amountOutstanding = getPreviousOutstandingAmt();
			soEJB.setObject(this.soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (hasInvoice())
		{
			try
			{
				Invoice invoiceEJB = InvoiceNut.getHandle(this.soObj.idInvoice);
				InvoiceObject invoiceObj = invoiceEJB.getObject();
				invoiceObj.mOutstandingAmt = getPreviousOutstandingAmt();
				invoiceEJB.setObject(invoiceObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		BigDecimal bdOutstandingAfter = getPreviousOutstandingAmt();
		if (bdOutstandingAfter.signum() == 0 && bdOutstandingBefore.signum() > 0)
		{
			createCashRebateVoucher();
		}
		return orObj;
	}

	public String getAlertMessage()
	{
		String alertMsg = "";
		alertMsg+= "Stock Taken From:"+this.branch.description+"\\n ";

		if(requireProduction())
		{ alertMsg+= " The order REQUIRE Workshop!\\n"; }
		else
		{ alertMsg+= " The order DOES NOT need Workshop!\\n";}

		if(requireDelivery())
		{ alertMsg+= " The order REQUIRE Delivery!\\n"; }
		else
		{ alertMsg+= " The order DOES NOT need Delivery!\\n";}

		alertMsg+=" Payment Mode:"+this.soObj.receiptMode;
		return alertMsg;
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

	public void updateSalesOrder()
	{

		if(this.docTrail.length()>4 && this.soObj!=null)
		{
			DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
			dpiObj.processType = "ORDER-UPDATE";
      	dpiObj.category = "UPDATE-DETAILS";
			dpiObj.auditLevel = new Integer(0);
//			dpiObj.processId = new Long(0);
			dpiObj.userid = this.userId;
			dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
			dpiObj.docId = this.soObj.pkid;
//			dpiObj.entityRef = "";
//      	dpiObj.entityId = new Integer(0);
			dpiObj.description1 = this.docTrail;
//			dpiObj.description2 = "";
//	      dpiObj.remarks = "";
			dpiObj.time = TimeFormat.getTimestamp();
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
			DocumentProcessingItemNut.fnCreate(dpiObj);
			this.docTrail = "";
		}

		this.soObj.amount = getBillAmount();
		// // todo
		// this.soObj.amountOutstanding =
		// this.soObj.amount.subtract(this.receiptForm.getReceiptAmt());
		this.soObj.amountOutstanding = getPreviousOutstandingAmt();
		// this.soObj.amountOutstanding =
		// this.soObj.amount.subtract(this.receiptForm.getReceiptAmt());
		// this.soObj.timeCreate = TimeFormat.getTimestamp();
		this.soObj.timeUpdate = TimeFormat.getTimestamp();
		this.soObj.useridEdit = this.userId;
		UserObject userObj = UserNut.getObject(this.userId);
		this.soObj.ordertakerUserid = this.userId;
		this.soObj.ordertakerName = userObj.userName;
		this.soObj.ordertakerTime = TimeFormat.getTimestamp();
		this.soObj.processProduction = requireProduction();
		this.soObj.processDelivery = requireDelivery();
		if (this.soObj.thirdpartyLoyaltyCardCode.length() > 3)
		{
			this.soObj.thirdpartyLoyaltyCardPtsGain = this.soObj.amount;
		}
		try
		{
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setObject(this.soObj);
			Vector newVecItem = new Vector();
			for (int cnt2 = 0; cnt2 < this.soObj.vecItem.size(); cnt2++)
			{
				SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt2);
				Vector vecDocRow = new Vector(this.tableRows.values());
				for (int cnt3 = 0; cnt3 < vecDocRow.size(); cnt3++)
				{
					DocRow docrow = (DocRow) vecDocRow.get(cnt3);
					if (soItmObj.pkid.equals(new Long(docrow.getDocId())))
					{
						SalesOrderItemObject soItmObjBuf = new SalesOrderItemObject(this.soObj, docrow);
						soItmObjBuf.pkid = soItmObj.pkid;
						SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
						soItmEJB.setObject(soItmObjBuf);
						newVecItem.add(soItmObjBuf);
					}
				}
			}
			this.soObj.vecItem = newVecItem;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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
		String theKey = "";
		try
		{
			BigDecimal redeemAmt = new BigDecimal(amount);
			if (redeemAmt.signum() == 0)
			{
				return;
			}
			lPkid = new Long(pkid);
			CashRebateVoucherObject cashRVObj = CashRebateVoucherNut.getObject(lPkid);
			theKey = TimeFormat.strDisplayDate(cashRVObj.dateGoodThru) + pkid;
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
		} catch (Exception ex)
		{
			return;
		}
	}

	public BigDecimal dropRedeemingCRV(String key)
	{
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.remove(lPkid);
	}

	public BigDecimal getRedeemingCRVAmount(String key)
	{
		// CashRebateVoucherObject cashRV = (CashRebateVoucherObject)
		// this.redeemingList.get(key);
		// if(cashRV!=null){ return cashRV.voucherBalance;}
		// else{ return (BigDecimal) null;}
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.get(lPkid);
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

	public void clearRedeemingList()
	{
		this.redeemingList.clear();
	}

	public void retrieveRedeemableCRV()
	{
		if (!getValidMemberCard())
		{
			return;
		}
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
		} else
		{
			this.redeemableCRV.clear();
		}
		for (int cnt1 = 0; cnt1 < vecRedeemableCRV.size(); cnt1++)
		{
			CashRebateVoucherObject bufObj = (CashRebateVoucherObject) vecRedeemableCRV.get(cnt1);
			this.redeemableCRV.put(TimeFormat.strDisplayDate(bufObj.dateGoodThru) + bufObj.pkid.toString(), bufObj);
		}
		this.redeemingList.clear();
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

	public String getDescription()
	{
		return this.soObj.description;
	}

	public void setDescription(String description)
	{
		if (description != null)
		{
			{ appendDocTrail("DESCRIPTION", this.soObj.description, description);}
			this.soObj.description = description;
		}
	}

	public void setPromo(String promoType, String promoCode, String promoNumber, 
		String promoName, BigDecimal promoDiscountAmount, BigDecimal promoDiscountPct)
	{
		{ appendDocTrail("PROMO TYPE", this.soObj.promoType, promoType);}
		{ appendDocTrail("PROMO CODE", this.soObj.promoCode, promoCode);}
		{ appendDocTrail("PROMO NUMBER", this.soObj.promoNumber, promoNumber);}
		{ appendDocTrail("PROMO DISCOUNT", CurrencyFormat.strCcy(this.soObj.promoDiscountAmount),CurrencyFormat.strCcy(promoDiscountAmount));}
		{ appendDocTrail("PROMO DISCOUNT %", CurrencyFormat.strCcy(this.soObj.promoDiscountPct),CurrencyFormat.strCcy(promoDiscountPct));}
		this.soObj.promoType = promoType;
		this.soObj.promoCode = promoCode;
		this.soObj.promoNumber = promoNumber;
		this.soObj.promoName = promoName;
		this.soObj.promoDiscountAmount = promoDiscountAmount;
		this.soObj.promoDiscountPct = promoDiscountPct;
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
package com.vlee.bean.distribution;

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

/*---------------------------------------------------------
 BASIC RULES
 If Receipt Has Already Been Created
 1) CANNOT Change/edit the memberCard/User/CustAccount
 2) CANNOT remove Sales Order Item
 3) CAN add Sales Order Item
 4) Cannot Change Receipt Details
 5) Recheck the CRV to see if it is still valid
 
 If Receipt Has Not Been Created
 1) Can add/remove Sales Order Items
 2) Can set receipt/payment info
 3) Able to create receipt, and respective CRV if amount 
 matches the SalesOrder bill amount

 SalesOrder Cancellation
 1) Cancel the CRV if it has already been created
 2) Lead user to SalesReturn use-case if invoice has already been created
 3) Reset the state of the sales order items

 -----------------------------------------------------------*/
public class EditSalesOrderSession extends java.lang.Object implements Serializable
{
	boolean popupPrintCRV = false;
	SalesOrderIndexObject soObj = null;
	BranchObject branch = null;
	LocationObject productionLocation = null;
	Integer userId = null;
	CustAccountObject custAccObj = null;
	CustUserObject custUserObj = null;
	MemberCardObject memCardObj = null;
	ReceiptForm receiptForm = null;
	CashRebateVoucherObject crvObj = null;
	Vector vecReceipt = null;
	Vector vecCreditMemoLink = null;
	public String soType2 = "";
	public String salesmanCode = "";
	protected TreeMap tableRows = null; // // this is used for SalesOrderItems
	protected TreeMap redeemableCRV = null;
	protected TreeMap redeemingList = null;
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
	public String docTrail = "";


	// // constructor
	public EditSalesOrderSession(Integer userId)
	{
		this.userId = userId;
		this.redeemableCRV = new TreeMap();
		this.redeemingList = new TreeMap();
		this.vecReceipt = new Vector();
		this.vecCreditMemoLink = new Vector();
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		this.popupPrintCRV = false;
		UserObject userObj = UserNut.getObject(this.userId);
		this.salesmanCode = userObj.userName;
		this.docTrail = "";
	}

	public void appendDocTrail(String paramName, String oldValue, String newValue)
	{
		this.docTrail = DocumentProcessingItemNut.appendDocTrail(paramName,oldValue,newValue, this.docTrail);
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
		// // reload the original sales order and auxiliary objects
		this.bTabOrderForm = true;
		this.bTabDeliveryForm = false;
		this.bTabPaymentForm = true;
		this.popupPrintCRV = false;
		this.docTrail = "";
	}

	public void setPopupPrintCRV(boolean buf)
	{
		this.popupPrintCRV = buf;
	}

	public boolean getPopupPrintCRV()
	{
		return this.popupPrintCRV;
	}

	public void setOrderType2(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.soType2 = SalesOrderIndexBean.SO_TYPE2_FLORIST;
		}
	}

	public void setOrderType1(String buf)
	{
		if(this.soObj !=null)
		{
			appendDocTrail(" ORDER TYPE ", this.soObj.soType1, buf);
			this.soObj.soType1 = buf;
		}
	}

	public void setDeliveryLocation(Long dlPkid, String option) throws Exception
	{
		DeliveryLocationObject dlObj = DeliveryLocationNut.getObject(dlPkid);

		if (dlObj == null || option == null)
		{
			return;
		}
		if (option.equals("country"))
		{
			appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);
			this.soObj.receiverCountry = dlObj.country;
		}
		if (option.equals("state"))
		{
			appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);
			appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
		}
		if(option.equals("zip"))
		{
			appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);
			appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
		}
		if(option.equals("city"))
		{

			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
		}
		if (option.equals("area"))
		{
			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
		}
		if (option.equals("street"))
		{

			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RecipientStreet",this.soObj.receiverAdd2,dlObj.street);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
		}
		if (option.equals("building"))
		{
			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RecipientStreet",this.soObj.receiverAdd2,dlObj.street);}
			{ appendDocTrail("RecipientBuilding",this.soObj.receiverAdd1,dlObj.building);}


			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
			this.soObj.receiverAdd1 = dlObj.building;
		}
		if (option.equals("company"))
		{
			{ appendDocTrail("RecipientCountry",this.soObj.receiverCountry,dlObj.country);}
			{ appendDocTrail("RecipientState",this.soObj.receiverState,dlObj.state);}
			{ appendDocTrail("RecipientZip",this.soObj.receiverZip,dlObj.zip);}
			{ appendDocTrail("RecipientCity",this.soObj.receiverCity,dlObj.city);}
			{ appendDocTrail("RecipientArea",this.soObj.receiverAdd3,dlObj.area);}
			{ appendDocTrail("RecipientStreet",this.soObj.receiverAdd2,dlObj.street);}
			{ appendDocTrail("RecipientBuilding",this.soObj.receiverAdd1,dlObj.building);}
			{ appendDocTrail("RecipientCompany",this.soObj.receiverCompanyName,dlObj.name);}

			this.soObj.receiverCountry = dlObj.country;
			this.soObj.receiverState = dlObj.state;
			this.soObj.receiverZip = dlObj.zip;
			this.soObj.receiverCity = dlObj.city;
			this.soObj.receiverAdd3 = dlObj.area;
			this.soObj.receiverAdd2 = dlObj.street;
			this.soObj.receiverAdd1 = dlObj.building;
			this.soObj.receiverCompanyName = dlObj.name;
		}
		

		if(dlObj.deliveryRate1.signum()>0)
		{
			ItemObject itmObj = ItemNut.getObject(ItemBean.PKID_DELIVERY);
			DocRow docrow = new DocRow();
			// docrow.setTemplateId(0)
			docrow.setItemType(ItemBean.CODE_DELIVERY);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(new BigDecimal("1"));
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(dlObj.deliveryRate1);
			// docrow.setDiscount(new
			// BigDecimal(req.getParameter("itemUnitDiscount")));
			docrow.user1 = userId.intValue();
			docrow.setRemarks("");
			docrow.setCcy2("");
			docrow.setPrice2(new BigDecimal(0));
			docrow.setProductionRequired(itmObj.productionRequired);
			docrow.setDeliveryRequired(itmObj.deliveryRequired);
			fnAddStockWithItemCode(docrow);
		} else
		{
			// check for this row in the list
			Vector vecDocRow = new Vector(this.tableRows.values());
			for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt);
				if (docrow.getItemId() == ItemBean.PKID_DELIVERY.intValue())
				{
					// / remove from this.tableRows
					DocRow dcrowRemoved = (DocRow) this.tableRows.remove(docrow.getKey());
					// / remove from this.soObj.vecItem
					for (int cnt3 = 0; cnt3 < this.soObj.vecItem.size(); cnt3++)
					{
						SalesOrderItemObject soItemObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt3);
						if (soItemObj.pkid.longValue() == dcrowRemoved.getDocId())
						{
							this.soObj.vecItem.remove(cnt3);
							cnt3--;
						}
					}
					// / remove from EJB
					try
					{
						SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(dcrowRemoved.getDocId()));
						if (soItemEJB != null)
						{
							soItemEJB.remove();
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
	}

	public void loadSalesOrder(Long soPkid) throws Exception
	{
		reset();

		this.soObj = SalesOrderIndexNut.getObjectTree(soPkid);
		if (this.soObj == null)
		{
			throw new Exception("Invalid Sales Order Number!");
		}
		// / populate this into the DocRows
		this.tableRows = this.soObj.getDocRows();

		// / populate other member objects of this Form/Session
		this.branch = BranchNut.getObject(this.soObj.branch);

		Log.printVerbose("Branch: "+this.branch);

		this.productionLocation = LocationNut.getObject(this.soObj.productionLocation);

		Log.printVerbose("ProductionLocation: "+this.productionLocation);

		this.custAccObj = CustAccountNut.getObject(this.soObj.senderKey1);

		if (this.soObj.senderKey2.intValue() > 0)
		{
			this.custUserObj = CustUserNut.getObject(this.soObj.senderKey2);
		} else
		{
			this.custUserObj = null;
		}
		if (this.soObj.senderLoyaltyCardNo.length() > 0)
		{
			this.memCardObj = MemberCardNut.getObjectByCardNo(this.soObj.senderLoyaltyCardNo);
		} else
		{
			this.memCardObj = null;
		}
		initializeReceiptForm();
		this.loadReceipts();
		retrieveRedeemableCRV();
		this.loadCRV();

		Log.printVerbose("End of Load SO");
	}

	private void loadCRV()
	{
		QueryObject query = new QueryObject(new String[] { CashRebateVoucherBean.SRC_KEY1 + " = '"
				+ this.soObj.pkid.toString() + "' " });
		query.setOrder(" ORDER BY " + CashRebateVoucherBean.PKID);
		Vector vecCRV = new Vector(CashRebateVoucherNut.getObjects(query));
		if (vecCRV.size() > 0)
		{
			this.crvObj = (CashRebateVoucherObject) vecCRV.get(vecCRV.size() - 1);
		} else
		{
			this.crvObj = null;
		}
	}

	public ReceiptForm getReceiptForm()
	{
		return this.receiptForm;
	}

	public TreeMap getTableRows()
	{
		return this.tableRows;
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

	public void setBranch(Integer iBranch)
	{
		if (hasInvoice())
		{
			return;
		}
		try
		{
			BranchObject brhObj = BranchNut.getObject(iBranch);
			if (brhObj != null)
			{
				if(this.branch!=null && !brhObj.pkid.equals(this.branch.pkid))
				{ appendDocTrail("BRANCH", this.branch.code, brhObj.code);}
				this.branch = brhObj;
				if (!hasInvoice())
				{
					this.soObj.branch = iBranch;
					this.soObj.receiptBranch = iBranch;
					this.soObj.productionBranch = iBranch;
					this.soObj.productionLocation = this.branch.invLocationId;
				}
				this.receiptForm.setBranch(this.soObj.branch);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public LocationObject getProductionLocation()
	{ return this.productionLocation;}

	public String getProductionLocation(String buf)
	{
		if(this.productionLocation ==null)
		{ return buf; }
		else
		{ return this.productionLocation.pkid.toString(); }
	}

	public void setProductionLocation(Integer iLocation)
	{
		if(hasInvoice())
		{ return ;}
		try
		{
			LocationObject locObj = LocationNut.getObject(iLocation);
			if(locObj!=null)
			{
				if(!locObj.pkid.equals(this.productionLocation.pkid))
				{	
					appendDocTrail("SHIP FROM", this.productionLocation.locationCode,locObj.locationCode);
				}
				this.productionLocation = locObj;
				this.soObj.productionLocation = this.productionLocation.pkid;
			}
		}
		catch(Exception ex)
		{ ex.printStackTrace();}
	}

	public boolean canChangeAccount()
	{
		boolean rtnValue = !hasInvoice() && !hasReceipt();
		return rtnValue;
	}

	public void setReceiptInfo(String receiptRemarks, String receiptApprovalCode, Integer receiptBranch)
	{
		if (this.soObj != null)
		{
			{ appendDocTrail("RCT Remarks", this.soObj.receiptRemarks,receiptRemarks);}
			{ appendDocTrail("RCT APPROVAL CODE",this.soObj.receiptApprovalCode,receiptApprovalCode);}
			if(!this.soObj.receiptBranch.equals(receiptBranch))
			{ 
				BranchObject brh1 = BranchNut.getObject(this.soObj.receiptBranch);
				BranchObject brh2 = BranchNut.getObject(receiptBranch);
				if(brh1!=null && brh2!=null)
				{
					appendDocTrail("PAY AT", brh1.code, brh2.code);
				}
			}

			// this.soObj.receiptMode = receiptMode;
			this.soObj.receiptRemarks = receiptRemarks;
			this.soObj.receiptApprovalCode = receiptApprovalCode;
			this.soObj.receiptBranch = receiptBranch;
		}



	}

	public void setReceiptMode(String buf)
	{
		if (this.soObj != null)
		{
			{ appendDocTrail("PAY MODE", this.soObj.receiptMode, buf);}
			this.soObj.receiptMode = buf;
		}
	}

	public void setDisplayFormat(String buf)
	{
		if (this.soObj != null)
		{
			this.soObj.displayFormat = buf;
		}
	}

	public void setCustomer(CustAccountObject custObj)
	{
		if (!canChangeAccount())
		{
			return;
		}
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
	}

	public void setCustUser(CustUserObject custUser)
	{
		this.memCardObj = null;
		this.custUserObj = custUser;
		if (!canChangeAccount())
		{
			if (!this.custUserObj.accId.equals(this.custAccObj.pkid))
			{
				return;
			}
		}
		this.custAccObj = CustAccountNut.getObject(this.custUserObj.accId);

      //this.soObj.thirdpartyLoyaltyCardCode = this.custUserObj.loyaltyCardName1;
      //this.soObj.thirdpartyLoyaltyCardNumber = this.custUserObj.loyaltyCardNumber1;
		
      this.soObj.senderTable1 = CustAccountBean.TABLENAME;
      this.soObj.senderKey1 = this.custAccObj.pkid;

      this.soObj.senderTable2 = CustUserBean.TABLENAME;
      this.soObj.senderKey2 = this.custUserObj.pkid;

      this.soObj.senderName = custUser.nameFirst + " "+custUser.nameLast;
      this.soObj.senderIdentityNumber = custUser.referenceNo;
      this.soObj.senderHandphone = custUser.mobilePhone;
      this.soObj.senderFax = custUser.faxNo;
      this.soObj.senderPhone1 = custUser.telephone1;
      this.soObj.senderPhone2 = custUser.telephone2;
//      this.soObj.senderInternetNo = senderInternetNo;
      this.soObj.senderCompanyName = this.custAccObj.name;
      this.soObj.senderAdd1 = custUser.mainAddress1;
      this.soObj.senderAdd2 = custUser.mainAddress2;
      this.soObj.senderAdd3 = custUser.mainAddress3;
//      this.soObj.senderCity = senderCity;
      this.soObj.senderZip = custUser.mainPostcode;
      this.soObj.senderState = custUser.mainState;
      this.soObj.senderCountry = custUser.mainCountry;

		if(this.custAccObj.state.equals(CustAccountBean.STATE_BL))
		{ this.soObj.flagInternalBool = true; }
		else { this.soObj.flagInternalBool = false;}

      this.receiptForm.setCustomer(custAccObj.pkid);
      this.receiptForm.setCCDetails(this.custUserObj.defaultCardNumber,
                                    this.custUserObj.defaultCardType,
                                    this.custUserObj.defaultCardName,
                                    this.custUserObj.defaultCardBank,
												"",
                                    this.custUserObj.defaultCardGoodThru,		
												this.custUserObj.defaultCardSecurityNum);
//    this.receiptForm.setCardType(custUser.defaultCardType);
      retrieveRedeemableCRV();

   }


	public void updateAccount()
	{
		this.custAccObj = CustAccountNut.getObject(this.custAccObj.pkid);
	}

	public void setFlagInternal(String buf)
	{
		{ appendDocTrail("INTERNAL REMARKS", this.soObj.flagInternal, buf);}
		this.soObj.flagInternal = buf;
	}

	public void setPaymentStatus(String buf)
	{
		{ appendDocTrail("PAY STATUS", this.soObj.statusPayment, buf);}
		this.soObj.statusPayment = buf;
	}

	public void setPaymentRemarks(String buf)
	{
		{ appendDocTrail("PAY REMARKS", this.soObj.receiptRemarks, buf);}
		this.soObj.receiptRemarks = buf;
	}

	public void setInfo(String remarks, boolean bRInvoice, boolean bRReceipt, String flagInternal, String flagSender,
			String flagReceiver, String managerPassword, String soType1, String occasion,
			String thirdpartyLoyaltyCardCode, String thirdpartyLoyaltyCardNumber, String paymentStatus, 
			BigDecimal interfloraPrice, String interfloraFlowers1)
	{
		Log.printVerbose(" SAVING DATA ... ");
		{ appendDocTrail( "RMKS", this.soObj.remarks1, remarks);}
		if(!this.soObj.requireInvoice==bRInvoice)
		{ appendDocTrail( "REQUIRE INVOICE:", ((this.soObj.requireInvoice)?"true":"false"), ((bRInvoice)?"true":"false"));}
		appendDocTrail("INTERNAL REMARKS", this.soObj.flagInternal, flagInternal);
		{ appendDocTrail("SENDER REMARKS", this.soObj.flagSender, flagSender);}
		{ appendDocTrail("RECIPIENT RMKS", this.soObj.flagReceiver, flagReceiver);}
		{ appendDocTrail("ORDER TYPE",this.soObj.soType1, soType1);}
		{ appendDocTrail("OCCASION", this.soObj.occasion,occasion);}
		{ appendDocTrail("LOYALTY CARD", this.soObj.thirdpartyLoyaltyCardCode, thirdpartyLoyaltyCardCode);}
		{ appendDocTrail("LOYALTY CARD NO", this.soObj.thirdpartyLoyaltyCardNumber, thirdpartyLoyaltyCardNumber);}
		{ appendDocTrail("PAY STATUS", this.soObj.statusPayment, paymentStatus);}
		{ appendDocTrail("INTERFLORA PRICE", CurrencyFormat.strCcy(this.soObj.interfloraPrice),CurrencyFormat.strCcy(interfloraPrice));}
		{ appendDocTrail("INTERFLORA FLOWERS", this.soObj.interfloraFlowers1, interfloraFlowers1);}

		this.soObj.remarks1 = remarks;
		this.soObj.requireInvoice = bRInvoice;
		this.soObj.requireReceipt = bRReceipt;
		this.soObj.flagInternal = flagInternal;
		this.soObj.flagSender = flagSender;
		this.soObj.flagReceiver = flagReceiver;
		this.soObj.soType1 = soType1;
		this.soObj.occasion = occasion;
		this.soObj.thirdpartyLoyaltyCardCode = thirdpartyLoyaltyCardCode;
		this.soObj.thirdpartyLoyaltyCardNumber = thirdpartyLoyaltyCardNumber;
		this.soObj.statusPayment = paymentStatus;
		this.soObj.interfloraPrice = interfloraPrice;
		this.soObj.interfloraFlowers1 = interfloraFlowers1;
		updateSalesOrder();
	}

	// // INTEGRATED SETTERS
	public void setDeliveryDetails(String deliveryTo, String deliveryToName, String deliveryFrom, //3
			String deliveryFromName, String deliveryMsg1, String expDeliveryTime, String expDeliveryTimeStart,//7
			String deliveryPreferences, String senderName, String senderIdentityNumber, String senderEmail, String senderHandphone,//12
			String senderFax, String senderPhone1, String senderPhone2, String senderInternetNo, //16
			String senderCompanyName, String senderAdd1, String senderAdd2, String senderAdd3, String senderCity, //21
			String senderZip, String senderState, String senderCountry, String receiverTitle, String receiverName,//26
			String receiverIdentityNumber, String receiverEmail, String receiverHandphone, String receiverFax, String receiverPhone1,//31
			String receiverPhone2, String receiverCompanyName, String receiverAdd1, String receiverAdd2,//35
			String receiverAdd3, String receiverCity, String receiverZip, String receiverState, String receiverCountry)//40
	{
		{ appendDocTrail("CARD TO", this.soObj.deliveryTo, deliveryTo);}
		{ appendDocTrail("CARD TO NAME", this.soObj.deliveryToName, deliveryToName);}
		{ appendDocTrail("CARD FROM", this.soObj.deliveryFrom, deliveryFrom);}
		{ appendDocTrail("CARD FROM NAME", this.soObj.deliveryFromName, deliveryFromName);}
		{ appendDocTrail("CARD MESSAGE", this.soObj.deliveryMsg1, deliveryMsg1);}
		{ appendDocTrail("DEL TIME", this.soObj.expDeliveryTime, expDeliveryTime);}
		{ appendDocTrail("DEL PREF", this.soObj.deliveryPreferences, deliveryPreferences);}

		{ appendDocTrail("SENDER NAME", this.soObj.senderName, senderName);}
		{ appendDocTrail("SENDER ID", this.soObj.senderIdentityNumber, senderIdentityNumber);}
		{ appendDocTrail("SENDER EMAIL", this.soObj.senderEmail, senderEmail);}
		{ appendDocTrail("SENDER H/P", this.soObj.senderHandphone, senderHandphone);}
		{ appendDocTrail("SENDER FAX", this.soObj.senderFax, senderFax);}
		{ appendDocTrail("SENDER PHONE1", this.soObj.senderPhone1, senderPhone1);}
		{ appendDocTrail("SENDER PHONE2", this.soObj.senderPhone2, senderPhone2);}
		{ appendDocTrail("SENDER INTERNET", this.soObj.senderInternetNo, senderInternetNo);}
		{ appendDocTrail("SENDER COMPANY", this.soObj.senderCompanyName, senderCompanyName);}
		{ appendDocTrail("SENDER ADD1", this.soObj.senderAdd1, senderAdd1);}
		{ appendDocTrail("SENDER ADD2", this.soObj.senderAdd2, senderAdd2);}
		{ appendDocTrail("SENDER ADD3", this.soObj.senderAdd3, senderAdd3);}
		{ appendDocTrail("SENDER CITY", this.soObj.senderCity, senderCity);}
		{ appendDocTrail("SENDER ZIP", this.soObj.senderZip, senderZip);}
		{ appendDocTrail("SENDER STATE", this.soObj.senderState, senderState);}
		{ appendDocTrail("SENDER COUNTRY", this.soObj.senderCountry, senderCountry);}

		{ appendDocTrail("RECIPIENT NAME", this.soObj.receiverName, receiverName);}
		{ appendDocTrail("RECIPIENT ID", this.soObj.receiverIdentityNumber, receiverIdentityNumber);}
		{ appendDocTrail("RECIPIENT EMAIL", this.soObj.receiverEmail, receiverEmail);}
		{ appendDocTrail("RECIPIENT H/P", this.soObj.receiverHandphone, receiverHandphone);}
		{ appendDocTrail("RECIPIENT FAX", this.soObj.receiverFax, receiverFax);}
		{ appendDocTrail("RECIPIENT PHONE1", this.soObj.receiverPhone1, receiverPhone1);}
		{ appendDocTrail("RECIPIENT PHONE2", this.soObj.receiverPhone2, receiverPhone2);}
		{ appendDocTrail("RECIPIENT COMPANY", this.soObj.receiverCompanyName, receiverCompanyName);}
		{ appendDocTrail("RECIPIENT ADD1", this.soObj.receiverAdd1, receiverAdd1);}
		{ appendDocTrail("RECIPIENT ADD2", this.soObj.receiverAdd2, receiverAdd2);}
		{ appendDocTrail("RECIPIENT ADD3", this.soObj.receiverAdd3, receiverAdd3);}
		{ appendDocTrail("RECIPIENT CITY", this.soObj.receiverCity, receiverCity);}
		{ appendDocTrail("RECIPIENT ZIP", this.soObj.receiverZip, receiverZip);}
		{ appendDocTrail("RECIPIENT STATE", this.soObj.receiverState, receiverState);}
		{ appendDocTrail("RECIPIENT COUNTRY", this.soObj.receiverCountry, receiverCountry);}



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
		this.soObj.receiverIdentityNumber = receiverIdentityNumber;
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
		updateSalesOrder();
	}

	public boolean hasItems()
	{
		boolean rtn = true;
		Vector vecDocRow = new Vector(this.tableRows.values());
		if (vecDocRow.size() == 0)
		{
			return false;
		}
		return rtn;
	}

	public boolean hasInvoice()
	{
		return (this.soObj.idInvoice.longValue() > 0);
	}

	public boolean hasReceipt()
	{
		if (this.vecReceipt.size() > 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public Vector getPreviousReceipts()
	{
		return this.vecReceipt;
	}

	public Vector getPreviousCreditMemoLink()
	{
		return this.vecCreditMemoLink;
	}

	public BigDecimal getPreviousReceiptAmt()
	{
		BigDecimal total = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < this.vecReceipt.size(); cnt1++)
		{
			OfficialReceiptObject orObj = (OfficialReceiptObject) this.vecReceipt.get(cnt1);
			total = total.add(orObj.getReceiptAmount());
		}
		return total;
	}

	public BigDecimal getPreviousCreditMemoLinkAmt()
	{
		BigDecimal total = new BigDecimal(0);
		for (int cnt1 = 0; cnt1 < this.vecCreditMemoLink.size(); cnt1++)
		{
			DocLinkObject dlObj = (DocLinkObject) vecCreditMemoLink.get(cnt1);
			total = dlObj.amount.negate();
		}
		return total;
	}

	public BigDecimal getPreviousOutstandingAmt()
	{
		return this.soObj.amount.subtract(getPreviousReceiptAmt()).subtract(getPreviousCreditMemoLinkAmt());
	}

	public BigDecimal getTempOutstandingAmt()
	{
		return getPreviousOutstandingAmt().subtract(this.receiptForm.getReceiptAmt());
	}

	public void loadReceipts()
	{
		this.vecReceipt.clear();
		this.vecCreditMemoLink.clear();
		Vector vecDoc = new Vector(DocLinkNut.getByTargetDoc(SalesOrderIndexBean.TABLENAME, this.soObj.pkid));
		for (int cnt1 = 0; cnt1 < vecDoc.size(); cnt1++)
		{
			DocLinkObject dlObj = (DocLinkObject) vecDoc.get(cnt1);
			if (dlObj.srcDocRef.equals(OfficialReceiptBean.TABLENAME))
			{
				OfficialReceiptObject orObj = OfficialReceiptNut.getObject(dlObj.srcDocId);
				if (orObj != null)
				{
					this.vecReceipt.add(orObj);
				}
			}
			if (dlObj.srcDocRef.equals(CreditMemoIndexBean.TABLENAME))
			{
				this.vecCreditMemoLink.add(dlObj);
			}
		}
	}

	public boolean canCreateReceipt()
	{
		boolean rtn = true;
		// // CREATING RECEIPTS
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			this.receiptForm.setDisabled(true);
		} else
		{
			this.receiptForm.setDisabled(false);
		}
		if (this.receiptForm.canSave())
		{
			Log.printVerbose(" ReceiptForm can save : true");
		} else
		{
			Log.printVerbose(" ReceiptForm can save : false");
		}
		if (getTempOutstandingAmt().signum() < 0)
		{
			return false;
		}
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			return false;
		}
		if (this.receiptForm.getReceiptAmt().signum() <= 0)
		{
			return false;
		}
		if (!this.receiptForm.canSave())
		{
			return false;
		}
		return rtn;
	}

	public boolean canCreateCashsale()
	{
		if (!hasItems())
		{
			return false;
		}
		if (hasInvoice())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() != 0)
		{
			return false;
		}
		return true;
	}

	public boolean canCreateInvoice()
	{
		if (!hasItems())
		{
			return false;
		}
		if (hasInvoice())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() <= 0)
		{
			return false;
		}
		return true;
	}

	public boolean canCreateBill()
	{
		return canCreateInvoice() || canCreateCashsale();
	}

	public void fnAddStockWithItemCode(DocRow docr) throws Exception
	{
		// / if invoice exists, do nothing
		if (hasInvoice())
		{
			return;
		}
		// / check if this row exists in the list already
		Vector vecDocRow = new Vector(this.tableRows.values());
		if (docr.getItemId() == ItemBean.PKID_DELIVERY.intValue())
		{
			for (int cnt = 0; cnt < vecDocRow.size(); cnt++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(cnt);
				if (docrow.getItemId() == ItemBean.PKID_DELIVERY.intValue())
				{
					// / remove from this.tableRows
					DocRow dcrowRemoved = (DocRow) this.tableRows.remove(docrow.getKey());
					// / remove from this.soObj.vecItem
					for (int cnt3 = 0; cnt3 < this.soObj.vecItem.size(); cnt3++)
					{
						SalesOrderItemObject soItemObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt3);
						if (soItemObj.pkid.longValue() == dcrowRemoved.getDocId())
						{
							this.soObj.vecItem.remove(cnt3);
							cnt3--;
						}
					}
					// / remove from EJB
					try
					{
						SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(dcrowRemoved.getDocId()));
						if (soItemEJB != null)
						{
							soItemEJB.remove();
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
		// / add it
		try
		{
			SalesOrderItemObject soItemObj = new SalesOrderItemObject(this.soObj, docr);
			if (this.memCardObj == null)
			{
				soItemObj.crvGain = new BigDecimal(0);
			}
			SalesOrderItem soItemEJB = SalesOrderItemNut.fnCreate(soItemObj);
			docr.setDocId(soItemObj.pkid.longValue());
			this.soObj.vecItem.add(soItemObj);
			this.tableRows.put(docr.getKey(), docr);
			updateSalesOrder();
		} catch (Exception ex)
		{
			dropDocRow(docr.getKey());
			ex.printStackTrace();
			throw new Exception("Error adding this row! " + ex.getMessage());
		}
	}

	public void toggleProduction(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow != null)
		{
			docrow.productionRequired = !docrow.productionRequired;
		}
		try
		{
			Long soItemPkid = new Long(docrow.getDocId());
			// / update EJB
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			soItemObj.productionRequired = docrow.productionRequired;
			soItemEJB.setObject(soItemObj);
			// / update valueObjects
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.pkid.longValue() == docrow.getDocId())
				{
					soiObj.productionRequired = docrow.productionRequired;
				}
			}
			// // update the parent valueObject and the EJB
			this.soObj.processDelivery = false;
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.productionRequired)
				{
					this.soObj.processProduction = true;
				}
			}
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setProcessProduction(this.soObj.processProduction);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void toggleDelivery(String key)
	{
		DocRow docrow = (DocRow) this.tableRows.get(key);
		if (docrow == null)
		{
			return;
		}
		try
		{
			Long soItemPkid = new Long(docrow.getDocId());
			// / update EJB
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(soItemPkid);
			SalesOrderItemObject soItemObj = soItemEJB.getObject();
			// // 1) check if it has been delivered, if delivered,
			// / cannot change status to false
			if (soItemObj.deliveryStatus.equals(SalesOrderItemBean.DELIVERY_STATUS_DELIVERED))
			{
				return;
			}
			docrow.deliveryRequired = !docrow.deliveryRequired;
			soItemObj.deliveryRequired = docrow.deliveryRequired;
			soItemEJB.setObject(soItemObj);
			// / update valueObjects
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.pkid.longValue() == docrow.getDocId())
				{
					soiObj.deliveryRequired = docrow.deliveryRequired;
				}
			}
			// // update the parent valueObject and the EJB
			this.soObj.processDelivery = false;
			for (int cnt1 = 0; cnt1 < this.soObj.vecItem.size(); cnt1++)
			{
				SalesOrderItemObject soiObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt1);
				if (soiObj.deliveryRequired)
				{
					this.soObj.processDelivery = true;
				}
			}
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setProcessDelivery(this.soObj.processDelivery);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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

	public boolean hasCRV()
	{
		if (this.crvObj != null)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public boolean canCreateCRV()
	{
		if (this.memCardObj == null)
		{
			return false;
		}
		if (!hasInvoice())
		{
			return false;
		}
		if (hasCRV())
		{
			return false;
		}
		if (getTempOutstandingAmt().signum() != 0)
		{
			return false;
		}
		BigDecimal newCrvValue = getDocRowCRVGainAmount();
		if (newCrvValue.signum() <= 0)
		{
			return false;
		}
		return true;
	}

	public CashRebateVoucherObject getCashRebateVoucher()
	{
		return this.crvObj;
	}

	private synchronized CashRebateVoucherObject createCashRebateVoucher()
	{
		Log.printVerbose(".............. trying to create CashRebateVoucher................");
		if (!canCreateCRV())
		{
			return null;
		}
		Log.printVerbose("............. condition fulfilled ............................ ");
		CashRebateVoucherObject bufObj = populateCashRebateVoucher();
		try
		{
			if (this.memCardObj == null)
			{
				return null;
			} else
			{
				Log.printVerbose("............. membercard not null ............................ ");
				if (this.memCardObj.pointBalance == null)
				{
					Log.printVerbose(" POINT BALANCE == NULLLLLLLLLLLLLLL");
				}
				if (bufObj.voucherValue == null)
				{
					Log.printVerbose(" CRV VOUCHER VALUE == NULLLLLLLLLLLLLLL");
				}
				BigDecimal bdTotalCRV = new BigDecimal(0);
				bdTotalCRV = this.memCardObj.pointBalance.add(bufObj.voucherValue);
				// / before creating the CRV, add the value to the
				// MemberCardObject.pointBalance
				// / if the value of the point Balance + this CRV exceeds
				// minimum amount RM1, then
				// / proceed to create the CRV
				Log.printVerbose("............. total CRV plus balance " + bdTotalCRV.toString());
				if (bdTotalCRV.compareTo(new BigDecimal("1.00")) < 0)
				{
					try
					{
						Log.printVerbose(".............  addding balance to balance .... "
								+ bufObj.voucherValue.toString());
						MemberCard memCardEJB = MemberCardNut.getHandle(memCardObj.pkid);
						this.memCardObj.pointBalance = this.memCardObj.pointBalance.add(bufObj.voucherValue);
						Log
								.printVerbose(".............  balance after .... "
										+ this.memCardObj.pointBalance.toString());
						memCardEJB.setObject(this.memCardObj);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				} else
				{
					// // if not, just don't create the CRV, add existing CRV as
					// accumulated values
					// /// inside CRV Balance
					CashRebateVoucher crvEJB = CashRebateVoucherNut.fnCreate(bufObj);
					BigDecimal pointBalance = this.memCardObj.pointBalance;
					this.crvObj = bufObj;
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
					// / check if there's membershipFee Due, if yes, deduct from
					// the CRV to be created.
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
								this.memCardObj.membershipFeeDue = this.memCardObj.membershipFeeDue.subtract(reduceAmt);
								this.memCardObj.membershipFeeLog = " Deducted from CRV No:" + crvObj.pkid.toString()
										+ " " + this.memCardObj.membershipFeeLog;
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
					this.popupPrintCRV = true;
				}
				return bufObj;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return bufObj;
	}

	public CashRebateVoucherObject populateCashRebateVoucher()
	{
		// // create a new CRV Object if it has not been created
		CashRebateVoucherObject tmpCrvObj = new CashRebateVoucherObject();
		// /// populate/update the CRV Object
		tmpCrvObj.branch = this.branch.pkid;
		tmpCrvObj.pcCenter = this.branch.accPCCenterId;
		tmpCrvObj.cardId = this.memCardObj.pkid;
		tmpCrvObj.cardNo = this.memCardObj.cardNo;
		tmpCrvObj.entityTable1 = CustAccountBean.TABLENAME;
		tmpCrvObj.entityKey1 = this.custAccObj.pkid;
		tmpCrvObj.nameDisplay = this.memCardObj.nameDisplay;
		tmpCrvObj.identityNumber = this.memCardObj.identityNumber;
		Timestamp tsToday = TimeFormat.getTimestamp();
		tmpCrvObj.dateValidFrom = TimeFormat.add(tsToday, 0, 0, this.branch.crvDayFrom.intValue());
		tmpCrvObj.dateGoodThru = TimeFormat.add(tmpCrvObj.dateValidFrom, 0, 0, this.branch.crvDayTo.intValue());
		tmpCrvObj.cardType = this.memCardObj.cardType;
		tmpCrvObj.remarks = "";
		tmpCrvObj.info1 = "";
		tmpCrvObj.info2 = "";
		tmpCrvObj.voucherValue = getDocRowCRVGainAmount();
		tmpCrvObj.voucherBalance = tmpCrvObj.voucherValue;
		tmpCrvObj.pointBonus = new BigDecimal(0);
		tmpCrvObj.srcTable1 = SalesOrderIndexBean.TABLENAME;
		tmpCrvObj.srcKey1 = this.soObj.pkid;
		if (hasReceipt())
		{
			tmpCrvObj.srcTable2 = OfficialReceiptBean.TABLENAME;
			OfficialReceiptObject tmpORObj = (OfficialReceiptObject) this.vecReceipt.get(this.vecReceipt.size() - 1);
			tmpCrvObj.srcKey2 = tmpORObj.pkid;
		}
		// tmpCrvObj.tgtTable1 = "";
		// tmpCrvObj.tgtKey1 = new Long(0);
		// tmpCrvObj.tgtTable2 = "";
		// tmpCrvObj.tgtKey2 = new Long(0);
		tmpCrvObj.dateCreate = TimeFormat.getTimestamp();
		tmpCrvObj.dateEdit = TimeFormat.getTimestamp();
		// tmpCrvObj.dateUse = TimeFormat.getTimestamp();
		tmpCrvObj.userCreate = this.userId;
		tmpCrvObj.userEdit = this.userId;
		tmpCrvObj.userUse = this.userId;
		tmpCrvObj.state = CashRebateVoucherBean.STATE_CREATED;
		tmpCrvObj.status = CashRebateVoucherBean.STATUS_ACTIVE;
		return tmpCrvObj;
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
		if (hasInvoice())
		{
			return null;
		}
		DocRow drow = (DocRow) this.tableRows.remove(key);
		try
		{
			SalesOrderItem soItemEJB = SalesOrderItemNut.getHandle(new Long(drow.getDocId()));
			soItemEJB.remove();
			this.soObj.removeItem(new Long(drow.getDocId()));
			updateSalesOrder();
		} catch (Exception ex)
		{
		}
		return drow;
	}

	public synchronized void createInvoice() throws Exception
	{
		if (!canCreateInvoice() && !canCreateCashsale())
		{
			return;
		}
		if (canCreateReceipt())
		{
			createReceipt();
		}
		createInvDocLink();
		// / 4) create new CRV if valid
		// / populate the CRV with SalesOrder PKID and Receipt PKID
		createCashRebateVoucher();
	}

	public void updateContact()
	{
		if (this.custUserObj == null)
		{
			return;
		}
		this.custUserObj = CustUserNut.getObject(this.custUserObj.pkid);
		this.soObj.thirdpartyLoyaltyCardCode = this.custUserObj.loyaltyCardName1;
		//this.soObj.thirdpartyLoyaltyCardNumber = this.custUserObj.loyaltyCardNumber1;
		this.soObj.senderTable1 = CustAccountBean.TABLENAME;
		this.soObj.senderKey1 = this.custAccObj.pkid;
		this.soObj.senderTable2 = CustUserBean.TABLENAME;
		this.soObj.senderKey2 = this.custUserObj.pkid;
		this.soObj.senderName = custUserObj.nameFirst + " " + custUserObj.nameLast;
		this.soObj.senderIdentityNumber = custUserObj.referenceNo;
		this.soObj.senderHandphone = custUserObj.mobilePhone;
		this.soObj.senderFax = custUserObj.faxNo;
		this.soObj.senderPhone1 = custUserObj.telephone1;
		this.soObj.senderPhone2 = custUserObj.telephone2;
		// this.soObj.senderInternetNo = senderInternetNo;
		this.soObj.senderCompanyName = this.custAccObj.name;
		this.soObj.senderAdd1 = custUserObj.mainAddress1;
		this.soObj.senderAdd2 = custUserObj.mainAddress2;
		this.soObj.senderAdd3 = custUserObj.mainAddress3;
		// this.soObj.senderCity = senderCity;
		this.soObj.senderZip = custUserObj.mainPostcode;
		this.soObj.senderState = custUserObj.mainState;
		this.soObj.senderCountry = custUserObj.mainCountry;
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
	}

	private synchronized void createInvDocLink()
	{
		// / first, create the invoice...
		// // set the outstanding balance = sales order amount - receipt amount
		// Populate Defaults
		// this.invoiceObj.mSalesTxnId = // automatically created when default
		// is zero
		// this.invoiceObj.mPaymentTermsId = pmtTerm;
		// this.invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		InvoiceObject invoiceObj = new InvoiceObject();
		invoiceObj.mTimeIssued = TimeFormat.getTimestamp();
		invoiceObj.mCurrency = this.branch.currency;
		invoiceObj.mTotalAmt = getBillAmount();
		invoiceObj.mOutstandingAmt = getBillAmount().subtract(this.receiptForm.getReceiptAmt());
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
		//invoiceObj.mLocationId = this.branch.invLocationId;
		invoiceObj.mLocationId = this.productionLocation.pkid;
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
			invoiceObj.mDisplayFormat = InvoiceBean.CASHBILL_TRADING_1;
			invoiceObj.mDocType = InvoiceBean.CASHBILL;
		} else if (canCreateInvoice())
		{
			invoiceObj.mDisplayFormat = InvoiceBean.INVOICE_TRADING_1;
			invoiceObj.mDocType = InvoiceBean.INVOICE;
		}
		Invoice invoiceEJB = InvoiceNut.fnCreate(invoiceObj);
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
				StockNut.sell(iiObj.mPic2, iiObj.mItemId,// Integer itemId,
						invoiceObj.mLocationId, invoiceObj.mPCCenter, iiObj.mTotalQty, iiObj.mUnitPriceQuoted,
						iiObj.mCurrency, InvoiceItemBean.TABLENAME, iiObj.mPkid, iiObj.mRemarks, // remarks
						invoiceObj.mTimeIssued, invoiceObj.mUserIdUpdate, new Vector(iiObj.colSerialObj));
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
			this.soObj.idInvoice = invoiceObj.mPkid;
			updateSalesOrder();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		// / check if receipt amount > 0
		// / create doc link
		if (hasReceipt())
		{
			for (int cnt1 = 0; cnt1 < this.vecReceipt.size(); cnt1++)
			{
				OfficialReceiptObject receiptObj = (OfficialReceiptObject) this.vecReceipt.get(cnt1);
				// /////////// MUST CREATE THE DOC LINK BEAN HERE!!!!
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
				DocLink dlEJB = DocLinkNut.fnCreate(dlObj);
			}
		}
		updateSalesOrder();
	}

	private void initializeReceiptForm()
	{
		this.receiptForm = new ReceiptForm(this.userId);
		this.receiptForm.setBranch(this.soObj.branch);
		this.receiptForm.setCustomer(this.custAccObj.pkid);
		if (getPreviousOutstandingAmt().signum() <= 0)
		{
			this.receiptForm.setDisabled(true);
		}
	}

	public synchronized OfficialReceiptObject createReceipt() throws Exception
	{
		BigDecimal bdOutstandingBefore = getPreviousOutstandingAmt();
		OfficialReceiptObject orObj = null;
		if (!canCreateReceipt())
		{
			return orObj;
		}
		// / CHECKING BUSINESS LOGIC
		// / 1) If delivery is required, check if the addresses etc are properly
		// filled in
		// / 2) Check the status of the CRV being consumed to make sure they are
		// still in valid state
		// / CREATE BASIC OBJECTS
		// / 1) do not create temporary invoice! invoice is to be created at a
		// later stage
		// / 2) create Receipt
		try
		{
			if (this.receiptForm != null && this.receiptForm.canSave())
			{
				this.receiptForm.confirmAndSave();
				// this.receiptObj = this.receiptForm.getReceipt();
				orObj = this.receiptForm.getReceipt();
				// this.soObj.idReceipt = this.receiptObj.pkid;
				// /todo: create the DocLink for Receipt -> Invoice and Receipt
				// -> Sales Order
				// / update the CRV consumed upon successful savings of Receipt
				// Object
				/*
				 * Vector vecCRVRedeemed = new
				 * Vector(this.redeemingList.values()); for(int cnt1=0;cnt1<vecCRVRedeemed.size();cnt1++) {
				 * CashRebateVoucherObject bufObj = (CashRebateVoucherObject)
				 * vecCRVRedeemed.get(cnt1); try { CashRebateVoucher bufEJB =
				 * CashRebateVoucherNut.getHandle(bufObj.pkid);
				 * bufObj.voucherBalance = new BigDecimal(0);
				 * bufEJB.setObject(bufObj); } catch(Exception ex) {
				 * ex.printStackTrace(); } }
				 */
				Set keySet = this.redeemingList.keySet();
				Iterator keyItr = keySet.iterator();
				int cnt1 = 0;
				while (keyItr.hasNext())
				{
					Long crvPkid = (Long) keyItr.next();
					BigDecimal redeemAmt = (BigDecimal) this.redeemingList.get(crvPkid);
					try
					{
						CashRebateVoucher bufEJB = CashRebateVoucherNut.getHandle(crvPkid);
						CashRebateVoucherObject bufObj = bufEJB.getObject();
						OfficialReceiptObject rctObj = this.receiptForm.getReceipt();
						bufObj.voucherBalance = bufObj.voucherBalance.subtract(redeemAmt);
						bufObj.usedAtBranch = branch.pkid;
						bufObj.usedAtPCCenter = branch.accPCCenterId;
						bufObj.usedTime = TimeFormat.getTimestamp();
						bufObj.tgtTable1 = OfficialReceiptBean.TABLENAME;
						bufObj.tgtKey1 = rctObj.pkid;
						bufObj.tgtTable2 = SalesOrderIndexBean.TABLENAME;
						bufObj.tgtKey2 = this.soObj.pkid;
						bufEJB.setObject(bufObj);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				clearRedeemingList();
			}
		} catch (Exception ex)
		{
			throw new Exception(ex);
		}
		initializeReceiptForm();
		// // CROSS POPULATE THE PKIDs into different objects
		// // create respective DOCLinks
		if (orObj != null)
		{
			DocLinkObject dlObj = new DocLinkObject();
			dlObj.namespace = DocLinkBean.NS_CUSTOMER;
			// dlObj.reference = "";
			dlObj.relType = DocLinkBean.RELTYPE_PYMT_SO;
			dlObj.srcDocRef = OfficialReceiptBean.TABLENAME;
			dlObj.srcDocId = orObj.pkid;
			dlObj.tgtDocRef = SalesOrderIndexBean.TABLENAME;
			dlObj.tgtDocId = this.soObj.pkid;
			dlObj.currency = this.soObj.currency;
			dlObj.amount = orObj.amount.negate();
			dlObj.currency2 = "";
			dlObj.amount2 = new BigDecimal("0.00");
			dlObj.remarks = "";
			dlObj.status = DocLinkBean.STATUS_ACTIVE;
			dlObj.lastUpdate = TimeFormat.getTimestamp();
			dlObj.userIdUpdate = this.userId;
			DocLink dlEJB = DocLinkNut.fnCreate(dlObj);
			if (hasInvoice())
			{
				DocLinkObject dlObj2 = new DocLinkObject();
				dlObj2.namespace = DocLinkBean.NS_CUSTOMER;
				// dlObj.reference = "";
				dlObj2.relType = DocLinkBean.RELTYPE_PYMT_INV;
				dlObj2.srcDocRef = OfficialReceiptBean.TABLENAME;
				dlObj2.srcDocId = orObj.pkid;
				dlObj2.tgtDocRef = InvoiceBean.TABLENAME;
				dlObj2.tgtDocId = this.soObj.idInvoice;
				dlObj2.currency = this.soObj.currency;
				dlObj2.amount = orObj.amount.negate();
				dlObj2.currency2 = "";
				dlObj2.amount2 = new BigDecimal("0.00");
				dlObj2.remarks = "";
				dlObj2.status = DocLinkBean.STATUS_ACTIVE;
				dlObj2.lastUpdate = TimeFormat.getTimestamp();
				dlObj2.userIdUpdate = this.userId;
				DocLink dlEJB2 = DocLinkNut.fnCreate(dlObj2);
			}
		}
		loadReceipts();
		// / 3) update the sales order object
		try
		{
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			this.soObj.amountOutstanding = getPreviousOutstandingAmt();
			soEJB.setObject(this.soObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (hasInvoice())
		{
			try
			{
				Invoice invoiceEJB = InvoiceNut.getHandle(this.soObj.idInvoice);
				InvoiceObject invoiceObj = invoiceEJB.getObject();
				invoiceObj.mOutstandingAmt = getPreviousOutstandingAmt();
				invoiceEJB.setObject(invoiceObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		BigDecimal bdOutstandingAfter = getPreviousOutstandingAmt();
		if (bdOutstandingAfter.signum() == 0 && bdOutstandingBefore.signum() > 0)
		{
			createCashRebateVoucher();
		}
		return orObj;
	}

	public String getAlertMessage()
	{
		String alertMsg = "";
		alertMsg+= "Stock Taken From:"+this.branch.description+"\\n ";

		if(requireProduction())
		{ alertMsg+= " The order REQUIRE Workshop!\\n"; }
		else
		{ alertMsg+= " The order DOES NOT need Workshop!\\n";}

		if(requireDelivery())
		{ alertMsg+= " The order REQUIRE Delivery!\\n"; }
		else
		{ alertMsg+= " The order DOES NOT need Delivery!\\n";}

		alertMsg+=" Payment Mode:"+this.soObj.receiptMode;
		return alertMsg;
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

	public void updateSalesOrder()
	{

		if(this.docTrail.length()>4 && this.soObj!=null)
		{
			DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
			dpiObj.processType = "ORDER-UPDATE";
      	dpiObj.category = "UPDATE-DETAILS";
			if(this.soObj.printCountInvoice.intValue()>0 || this.soObj.printCountReceipt.intValue()>0	||
				this.soObj.printCountWorkshop.intValue()>0 || this.soObj.printCountDeliveryOrder.intValue()>0 ||
				this.soObj.printCountSalesOrder.intValue()>0)
			{
				dpiObj.processType = "ORDER-WARNING";
				dpiObj.category = "EDIT-PRINTED-ORDER";
			}
			dpiObj.auditLevel = new Integer(0);
//			dpiObj.processId = new Long(0);
			dpiObj.userid = this.userId;
			dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
			dpiObj.docId = this.soObj.pkid;
//			dpiObj.entityRef = "";
//      	dpiObj.entityId = new Integer(0);
			dpiObj.description1 = this.docTrail;
//			dpiObj.description2 = "";
//	      dpiObj.remarks = "";
			dpiObj.time = TimeFormat.getTimestamp();
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
			DocumentProcessingItemNut.fnCreate(dpiObj);
			this.docTrail = "";
		}

		this.soObj.amount = getBillAmount();
		// // todo
		// this.soObj.amountOutstanding =
		// this.soObj.amount.subtract(this.receiptForm.getReceiptAmt());
		this.soObj.amountOutstanding = getPreviousOutstandingAmt();
		// this.soObj.amountOutstanding =
		// this.soObj.amount.subtract(this.receiptForm.getReceiptAmt());
		// this.soObj.timeCreate = TimeFormat.getTimestamp();
		this.soObj.timeUpdate = TimeFormat.getTimestamp();
		this.soObj.useridEdit = this.userId;
		UserObject userObj = UserNut.getObject(this.userId);
		this.soObj.ordertakerUserid = this.userId;
		this.soObj.ordertakerName = userObj.userName;
		this.soObj.ordertakerTime = TimeFormat.getTimestamp();
		this.soObj.processProduction = requireProduction();
		this.soObj.processDelivery = requireDelivery();
		if (this.soObj.thirdpartyLoyaltyCardCode.length() > 3)
		{
			this.soObj.thirdpartyLoyaltyCardPtsGain = this.soObj.amount;
		}
		try
		{
			SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(this.soObj.pkid);
			soEJB.setObject(this.soObj);
			Vector newVecItem = new Vector();
			for (int cnt2 = 0; cnt2 < this.soObj.vecItem.size(); cnt2++)
			{
				SalesOrderItemObject soItmObj = (SalesOrderItemObject) this.soObj.vecItem.get(cnt2);
				Vector vecDocRow = new Vector(this.tableRows.values());
				for (int cnt3 = 0; cnt3 < vecDocRow.size(); cnt3++)
				{
					DocRow docrow = (DocRow) vecDocRow.get(cnt3);
					if (soItmObj.pkid.equals(new Long(docrow.getDocId())))
					{
						SalesOrderItemObject soItmObjBuf = new SalesOrderItemObject(this.soObj, docrow);
						soItmObjBuf.pkid = soItmObj.pkid;
						SalesOrderItem soItmEJB = SalesOrderItemNut.getHandle(soItmObj.pkid);
						soItmEJB.setObject(soItmObjBuf);
						newVecItem.add(soItmObjBuf);
					}
				}
			}
			this.soObj.vecItem = newVecItem;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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
		String theKey = "";
		try
		{
			BigDecimal redeemAmt = new BigDecimal(amount);
			if (redeemAmt.signum() == 0)
			{
				return;
			}
			lPkid = new Long(pkid);
			CashRebateVoucherObject cashRVObj = CashRebateVoucherNut.getObject(lPkid);
			theKey = TimeFormat.strDisplayDate(cashRVObj.dateGoodThru) + pkid;
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
		} catch (Exception ex)
		{
			return;
		}
	}

	public BigDecimal dropRedeemingCRV(String key)
	{
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.remove(lPkid);
	}

	public BigDecimal getRedeemingCRVAmount(String key)
	{
		// CashRebateVoucherObject cashRV = (CashRebateVoucherObject)
		// this.redeemingList.get(key);
		// if(cashRV!=null){ return cashRV.voucherBalance;}
		// else{ return (BigDecimal) null;}
		Long lPkid = new Long(key);
		return (BigDecimal) this.redeemingList.get(lPkid);
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

	public void clearRedeemingList()
	{
		this.redeemingList.clear();
	}

	public void retrieveRedeemableCRV()
	{
		if (!getValidMemberCard())
		{
			return;
		}
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
		} else
		{
			this.redeemableCRV.clear();
		}
		for (int cnt1 = 0; cnt1 < vecRedeemableCRV.size(); cnt1++)
		{
			CashRebateVoucherObject bufObj = (CashRebateVoucherObject) vecRedeemableCRV.get(cnt1);
			this.redeemableCRV.put(TimeFormat.strDisplayDate(bufObj.dateGoodThru) + bufObj.pkid.toString(), bufObj);
		}
		this.redeemingList.clear();
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

	public String getDescription()
	{
		return this.soObj.description;
	}

	public void setDescription(String description)
	{
		if (description != null)
		{
			{ appendDocTrail("DESCRIPTION", this.soObj.description, description);}
			this.soObj.description = description;
		}
	}

	public void setPromo(String promoType, String promoCode, String promoNumber, 
		String promoName, BigDecimal promoDiscountAmount, BigDecimal promoDiscountPct)
	{
		{ appendDocTrail("PROMO TYPE", this.soObj.promoType, promoType);}
		{ appendDocTrail("PROMO CODE", this.soObj.promoCode, promoCode);}
		{ appendDocTrail("PROMO NUMBER", this.soObj.promoNumber, promoNumber);}
		{ appendDocTrail("PROMO DISCOUNT", CurrencyFormat.strCcy(this.soObj.promoDiscountAmount),CurrencyFormat.strCcy(promoDiscountAmount));}
		{ appendDocTrail("PROMO DISCOUNT %", CurrencyFormat.strCcy(this.soObj.promoDiscountPct),CurrencyFormat.strCcy(promoDiscountPct));}
		this.soObj.promoType = promoType;
		this.soObj.promoCode = promoCode;
		this.soObj.promoNumber = promoNumber;
		this.soObj.promoName = promoName;
		this.soObj.promoDiscountAmount = promoDiscountAmount;
		this.soObj.promoDiscountPct = promoDiscountPct;
	}

}
