/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoNewJobsheet implements Action
{
	String strClassName = "DoNewJobsheet";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		try
		{
			// Form Handlers
			String formName = req.getParameter("formName");
			// Regardless of the usage of the form..
			// we will populate the following
			// fnGetCustSalesCenterList(servlet, req, res);
			// If the form name is null,
			// the users are creating a fresh jobsheet
			if (formName == null)
			{
				Log.printVerbose(strClassName + "formName: null");
				// return new ActionRouter("pos-create-new-jobsheet-01-page");
				return new ActionRouter("pos-check-customer-by-vehicle-page");
			}
			if (formName.equals("retrieveVehicleNo"))
			{
				// define the default number of rows for the jobsheet
				// or the invoices
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// check vehicle number
				String mVehicleNo = req.getParameter("vehicleNo");
				Integer mCustAccId = null;
				Vehicle vehicleEJB = VehicleNut.getObjectByRegNum(mVehicleNo);
				if (vehicleEJB != null)
				{
					try
					{
						// repopulate the real vehicle no as retrieved from the
						// DB
						Integer mVehicleId = vehicleEJB.getPkid();
						mVehicleNo = vehicleEJB.getRegNum();
						mCustAccId = vehicleEJB.getCustAccountId();
						fnGetUserList(servlet, req, res);
						fnGetCustSalesCenterList(servlet, req, res);
						// req.setAttribute("vehicleNo", mVehicleNo);
						req.setAttribute("vehicleId", mVehicleId);
						req.setAttribute("custAccId", mCustAccId);
						return new ActionRouter("pos-create-new-jobsheet-01-page");
					} catch (Exception ex)
					{
						String strErrMsg = "Failed to retrieve regNum from vehicle EJB!!";
						Log.printDebug(strErrMsg);
						req.setAttribute("strErrMsg", strErrMsg);
						return new ActionRouter("pos-check-customer-by-vehicle-page");
					}
				} else
				{
					Log.printVerbose("*** Vehicle No not found in the DB ***");
					// New Customer
					Log.printVerbose(strClassName + "Vehicle No does not match any customer. Creating new ...");
					req.setAttribute("vehicleNo", mVehicleNo);
					// req.setAttribute("strErrMsg", mStrErrMsg);
					return new ActionRouter("pos-create-new-customer-page");
				}
			}
			if (formName.equals("createNewCustomer"))
			{
				// Add a new customer
				if (!fnCreateNewCustomer(servlet, req, res))
				{
					String strErrMsg = "Error in creating new customer. " + "Please try again";
					Log.printDebug(strClassName + ": " + strErrMsg);
					// req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-new-customer-page");
				}
				// Successfully created New Customer
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// Set the rest of the attributes
				/*
				 * SET THIS IN fnCreateNewCustomer req.setAttribute("strErrMsg",
				 * this.mStrErrMsg); req.setAttribute("vehicleNo",
				 * this.mVehicleNo); req.setAttribute("custAccId",
				 * this.mCustAccId);
				 */
				return new ActionRouter("pos-create-new-jobsheet-01-page");
			}
			if (formName.equals("retrieveItnlCust"))
			{
				// Obtain the custAccountId
				String strCustAccId = req.getParameter("iItnlCustAccId");
				if (strCustAccId == null)
				{
					String strErrMsg = "Could not obtain internal CustAccId";
					Log.printDebug(strErrMsg);
					req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-new-itnl-sales-page");
				}
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// req.setAttribute("vehicleNo", this.mVehicleNo);
				req.setAttribute("custAccId", new Integer(strCustAccId));
				return new ActionRouter("pos-create-new-jobsheet-01-page");
			}
			if (formName.equals("retrieveCustAccId"))
			{
				// Obtain the custAccountId
				String strCustAccId = req.getParameter("custAccId");
				if (strCustAccId == null)
				{
					String strErrMsg = "Could not obtain CustAccId";
					Log.printDebug(strErrMsg);
					req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-salesorder-01-page");
				}
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// req.setAttribute("vehicleNo", this.mVehicleNo);
				req.setAttribute("custAccId", new Integer(strCustAccId));
				return new ActionRouter("pos-create-salesorder-02-page");
			}
			// If the form has been filled and user submit to create
			// a new jobsheet, do the necessary checking
			if (formName.equals("createNewJobsheet"))
			{
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// boolean bTradeInOK = false;
				boolean bJobsheetVerified = false;
				Log.printVerbose(strClassName + ": before fnCreateNewJobsheet");
				bJobsheetVerified = fnCreateNewJobsheet(servlet, req, res, false);
				// bTradeInOK = fnCreateNewTradeIn(servlet, req, res);
				// Log.printVerbose(this.mStrErrMsg);
				// tentatively.. force it to true first..
				// this is to test the display
				// bJobsheetVerified = true;
				if (bJobsheetVerified == true)
				{
					return new ActionRouter("pos-create-new-jobsheet-02-page");
				} else
				{
					// Some errors must have occurred in fnCreateNewJobsheet
					// Rollback transactions
					// rollBackTxn();
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					// propagate iRow
					String strRows = req.getParameter("iRows");
					Integer iRows = null;
					if (strRows != null)
					{
						iRows = new Integer(strRows);
					} else
					{
						iRows = new Integer("15");
					}
					req.setAttribute("iRows", iRows);
					return new ActionRouter("pos-create-new-jobsheet-01-page");
				}
			}
			if (formName.equals("createSalesOrder"))
			{
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				boolean bJobsheetVerified = false;
				Log.printVerbose(strClassName + ": before fnCreateNewJobsheet");
				bJobsheetVerified = fnCreateNewJobsheet(servlet, req, res, true);
				// Log.printVerbose(this.mStrErrMsg);
				// tentatively.. force it to true first..
				// this is to test the display
				// bJobsheetVerified = true;
				if (bJobsheetVerified == true)
				{
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					// req.setAttribute("salesTxnObj", this.mSalesTxnObj);
					// req.setAttribute("jobsheetObj", this.mJobsheetObj);
					return new ActionRouter("pos-create-salesorder-03-page");
				} else
				{
					// Some errors must have occurred in fnCreateNewJobsheet
					// Rollback transactions
					// rollBackTxn();
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					return new ActionRouter("pos-create-salesorder-02-page");
				}
			}
			// fnGetCustSalesCenterList(servlet, req, res);
			Log.printVerbose(strClassName + ": returning default ActionRouter");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return new ActionRouter("pos-create-new-jobsheet-01-page");
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnGetUserList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector vecUserObj = UserNut.getValueObjectsGiven(UserBean.STATUS, UserBean.ACTIVE, (String) null,
				(String) null, (String) null, (String) null);
		req.setAttribute("vecUserObj", vecUserObj);
	}

	// /////////////////////////////////////////////////////////////////
	protected boolean fnCreateNewTradeIn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
	// HashMap mapGood, HashMap mapError,
			JobsheetObject jobsheetObj)
	{
		// Get all the relevent parameters
		String custSvcCtrId = req.getParameter("CustSvcCtrId");
		String[] tradeInNameArray = req.getParameterValues("tradeInName");
		String[] tradeInDescArray = req.getParameterValues("tradeInDesc");
		String[] tradeInQtyArray = req.getParameterValues("tradeInQty");
		// String[] tradeInSalesPriceArray =
		// req.getParameterValues("tradeInSalesPrice");
		String[] tradeInCostPriceArray = req.getParameterValues("tradeInCostPrice");
		String tradeInCustId = req.getParameter("custAccountId");
		// Item newItem = null;
		POSItem newPOSItemBuy = null;
		// POSItem newPOSItemSell = null;
		Vector vecNewItems = new Vector();
		Vector vecNewPOSItemsBuy = new Vector();
		// Vector vecNewPOSItemsSell = new Vector();
		try
		{ // Super huge try-catch block
			// Get current date/time and username
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				return false;
			}
			/*******************************************************************
			 * Verify Customer PKID
			 ******************************************************************/
			if (CustAccountNut.getHandle(new Integer(tradeInCustId)) == null)
			{
				throw new Exception("Customer (PKID: " + tradeInCustId + ") does not exist!");
			}
			// map ServiceCenter to Location
			Integer thisLocPkid = CustServiceCenterNut.mapSvcCtrToInvLoc(new Integer(custSvcCtrId));
			int nRows = tradeInNameArray.length;
			Log.printVerbose("Number of tradeIn rows = " + nRows);
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				String thisTradeInName = tradeInNameArray[nCount];
				String thisTradeInDesc = tradeInDescArray[nCount];
				String thisTradeInQty = tradeInQtyArray[nCount];
				String thisTradeInCostPrice = tradeInCostPriceArray[nCount];
				// String thisTradeInSalesPrice =
				// tradeInSalesPriceArray[nCount];
				// Check that the TradeIn Name is not empty
				if (thisTradeInName.equals(""))
					continue;
				BigDecimal costPrice = new BigDecimal(thisTradeInCostPrice);
				BigDecimal buyQty = null;
				try
				{
					buyQty = new BigDecimal(thisTradeInQty);
				} catch (Exception ex)
				{
					buyQty = new BigDecimal(1);
				}
				// Here we assume everytime this function is called it is a new
				// trade-in item
				/***************************************************************
				 * CREATE THE TRADE-IN ITEM
				 **************************************************************/
				ItemObject itemObj = new ItemObject();
				// populate the properties here!!
				itemObj.name = thisTradeInName;
				itemObj.description = thisTradeInDesc;
				itemObj.userIdUpdate = usrid;
				itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_TRADEIN);
				itemObj.priceList = costPrice;
				itemObj.priceSale = costPrice;
				itemObj.priceDisc1 = costPrice;
				itemObj.priceDisc2 = costPrice;
				itemObj.priceDisc3 = costPrice;
				itemObj.priceMin = costPrice;
				itemObj.fifoUnitCost = costPrice;
				itemObj.maUnitCost = costPrice;
				itemObj.waUnitCost = costPrice;
				itemObj.lastUnitCost = costPrice;
				itemObj.replacementUnitCost = costPrice;
				// itemObj.preferredSupplier = suppAccObj.pkid;
				Item itemEJB = ItemNut.fnCreateWithCodePrefix(itemObj, ItemBean.CODE_PREFIX_TI);
				if (itemEJB == null)
				{
					throw new Exception("Null Item");
				} else
				{
					Log.printVerbose("Successfully created Item");
				}
				vecNewItems.add(itemEJB);
				/***************************************************************
				 * ADD POS ITEM
				 **************************************************************/
				POSItemHome lPOSItemHome = POSItemNut.getHome();
				// Create one entry for purchase and one more for sales
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = itemEJB.getPkid();
				posObj.itemType = POSItemBean.TYPE_TRADEIN_BUY;
				posObj.currency = "MYR";
				posObj.unitPriceStd = new BigDecimal(thisTradeInCostPrice);
				posObj.unitPriceDiscounted = new BigDecimal(thisTradeInCostPrice);
				posObj.unitPriceMin = new BigDecimal(thisTradeInCostPrice);
				posObj.userIdUpdate = usrid;
				newPOSItemBuy = POSItemNut.fnCreate(posObj);
				if (newPOSItemBuy == null)
					throw new Exception("Null POSItem (Trade-In)!!");
				else
					Log.printVerbose("Successfully created POSItem");
				vecNewPOSItemsBuy.add(newPOSItemBuy);
				/***************************************************************
				 * Populate JobsheetItemObject
				 **************************************************************/
				JobsheetItemObject jsio = new JobsheetItemObject();
				jsio.mPosItemId = newPOSItemBuy.getPkid();
				jsio.mRemarks = "Traded in by Customer PKID " + tradeInCustId;
				jsio.mTotalQty = new BigDecimal(thisTradeInQty);
				jsio.mCurrency = "MYR";
				jsio.mUnitPriceQuoted = new BigDecimal(thisTradeInCostPrice).negate(); // negate
																						// because
																						// stockIn
				jsio.mUnitPriceRecommended = jsio.mUnitPriceQuoted; // temp. put
																	// the same
																	// as quoted
				jsio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
				jobsheetObj.vecJobsheetItems.add(jsio);
				/***************************************************************
				 * ADD TRADE-IN STOCK
				 **************************************************************/
				// At this point, populate the newly created trade-in item
				// code+name
				// req.setAttribute("strTradeInCode", tradeInCode);
				// req.setAttribute("strTradeInName", tradeInName);
			} // end for
		} // end huge-try
		catch (Exception ex)
		{
			// end of super huge try-catch block
			// Rollback all transactions
			// in the following order:
			// 1. POSItem
			// 2. Item
			try
			{
				// Loop through all populated items and start deleting each one
				for (int itemIdx = 0; itemIdx < vecNewItems.size(); itemIdx++)
				{
					Item rollbackItem = (Item) vecNewItems.get(itemIdx);
					if (rollbackItem != null)
						rollbackItem.remove();
				}
				for (int posItemIdx = 0; posItemIdx < vecNewPOSItemsBuy.size(); posItemIdx++)
				{
					POSItem rollbackPOSItem = (POSItem) vecNewPOSItemsBuy.get(posItemIdx);
					if (rollbackPOSItem != null)
						rollbackPOSItem.remove();
				}
			} catch (Exception e)
			{
				Log.printDebug("Failure to rollback transactions !!! ");
			}
			String strErrMsg;
			strErrMsg = "Error while processing a new trade-in item: " + ex.getMessage();
			Log.printDebug(strErrMsg);
			req.setAttribute("strErrMsg", strErrMsg);
			return false;
		}
		return true;
	} // end fnCreateNewTradeIn

	protected boolean fnCreateNewJobsheet(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			boolean isSalesOrder)
	{
		String funcName = "fnCreateNewJobsheet()";
		// Alex (27 Oct) - Changed member variables to local variables.
		String mStrErrMsg = new String("");
		// String strObjectName = new String("DoNewJobsheet");
		JobsheetObject mJobsheetObj = new JobsheetObject();
		SalesTxnObject mSalesTxnObj = new SalesTxnObject();
		SalesTxn mSalesTxn = null;
		Jobsheet mJobsheet = null;
		// String mVehicleNo = null;
		// Integer mCustAccId = null;
		Vector mVecJobsheetItems = new Vector();
		try
		{ // super huge try-catch
			// Populate the value objects including
			// SalesTxn, Jobsheet and jobsheet items
			Log.printVerbose(strClassName + "::fnCreateNewJobsheet" + " before entering fnPopulateValueObject");
			// Alex (04/13) - Added support for thorough field validation
			HashMap mapGood = new HashMap();
			HashMap mapError = new HashMap();
			String errMsg;
			if (isSalesOrder)
			{
				errMsg = fnPopulateValObjForSO(servlet, req, res, mapGood, mapError, mJobsheetObj, mSalesTxnObj);
			} else
			{
				errMsg = fnPopulateValObjForJS(servlet, req, res, mapGood, mapError, mJobsheetObj, mSalesTxnObj);
			}
			Log.printVerbose(strClassName + "::fnCreateNewJobsheet" + " after exiting fnPopulateValueObject");
			if (errMsg != null)
			{
				req.setAttribute("mapGood", mapGood);
				req.setAttribute("mapError", mapError);
				throw new Exception(errMsg);
			}
			// server side form validation
			// 1) create the SalesTxn Object
			// SalesTxn salesTxn = null;
			if (mSalesTxnObj != null)
			{
				mSalesTxn = SalesTxnNut.fnCreate(mSalesTxnObj);
			}
			if (mSalesTxn == null)
			{
				String strErrMsg = "SalesTxnNut.fnCreate failed to create object";
				throw new Exception(strErrMsg);
			} else
			{
				try
				{
					mSalesTxnObj.pkid = (Long) mSalesTxn.getPkid();
					for (int vecIndex = 0; vecIndex < mSalesTxnObj.vecJobsheets.size(); vecIndex++)
					{
						JobsheetObject tmpInvObj = (JobsheetObject) mSalesTxnObj.vecJobsheets.get(vecIndex);
						tmpInvObj.mSalesTxnId = mSalesTxnObj.pkid;
						mSalesTxnObj.vecJobsheets.set(vecIndex, tmpInvObj);
					}
				} catch (Exception ex)
				{
					String strErrMsg = "Failed to create SalesTxnBean: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// We are not creating a loop here, because
			// there's only one jobsheet attached to this sales transaction
			// because it is a new jobsheet.
			// for(int countA=0; countA< this.mSalesTxnObj.vecJobsheets.size();
			// countA++) { }
			// 2.0) create the Jobsheet Object
			// JobsheetObject mJobsheetObj = (JobsheetObject)
			// this.mSalesTxnObj.vecJobsheets.get(0);
			// Jobsheet jobsheetRemote = null;
			/*
			 * Already done in fnPopulateValueObjects() // Alex: 27 Dec 03 - to
			 * cater for SalesOrder, need to add the state field
			 * if(isSalesOrder) mJobsheetObj.mType = JobsheetBean.TYPE_SO;
			 */
			if (mJobsheetObj != null)
			{
				mJobsheet = JobsheetNut.fnCreate(mJobsheetObj);
			}
			if (mJobsheet == null)
			{
				String strErrMsg = "Failed to create new Jobsheet Bean";
				throw new Exception(strErrMsg);
			} else
			{
				try
				{
					mJobsheetObj.mPkid = (Long) mJobsheet.getPkid();
					mJobsheetObj.mStmtNumber = (Long) mJobsheet.getStmtNumber();
				} catch (Exception ex)
				{
					String strErrMsg = "Invalid Jobsheet Primary Key or StmtNumber: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// Check for any trade-in items
			if (!fnCreateNewTradeIn(servlet, req, res, mJobsheetObj))
			{
				throw new Exception("Error while creating new trade-in items");
			}
			// 2.1) create the Jobsheet Items Object selectively
			for (int countA = 0; countA < mJobsheetObj.vecJobsheetItems.size(); countA++)
			{
				JobsheetItemObject jsItemObj = (JobsheetItemObject) mJobsheetObj.vecJobsheetItems.get(countA);
				// Alex added this ..
				// Need to assign the newly created jobsheet
				// pkid into the jsItemObj
				jsItemObj.mJobsheetId = mJobsheetObj.mPkid;
				JobsheetItem invItem = JobsheetItemNut.fnCreate(jsItemObj);
				// if successful, fill the pkid of the valueObject
				if (invItem != null)
				{
					// First thing is to append to the JobsheetItem Vector
					mVecJobsheetItems.add(invItem);
					try
					{
						jsItemObj.mPkid = invItem.getPkid();
					} catch (Exception ex)
					{
						String strErrMsg = "Invalid Inventory Item Primary Key: " + ex.getMessage();
						throw new Exception(strErrMsg);
					}
				} else
				{
					String strErrMsg = "Failure in creating jobsheet item object.";
					throw new Exception(strErrMsg);
				}
			} // end for
			// Convert SalesTxn State to JOBSHEET_OK
			SalesTxn salesTxnEjb = SalesTxnNut.getHandle(mSalesTxnObj.pkid);
			salesTxnEjb.setState(SalesTxnBean.ST_JOBSHEET_OK);
			// Populate attributes
			// req.setAttribute("strErrMsg", "");
			req.setAttribute("salesTxnObj", mSalesTxnObj);
			req.setAttribute("jobsheetObj", mJobsheetObj);
			// If not, return to the original form for correction
			/*
			 * if(errMsg != null) { Log.printDebug(strClassName+" Cannot create
			 * salesTransaction "); mStrErrMsg += errMsg; return false; }
			 */
			// To create a new jobsheet
			// 0. Create a ReceiptBean
			// for now, we assume all transactions are on cash basis.
			// so, a receipt is automatically generated.
			// Also, cash sales are automatically treated as
			// customer with pkid = 1
			// Clean up the database if new jobsheet is not
			// created due to some reasons.
			return true;
		} // end of super huge try
		catch (Exception ex)
		{
			// Handle exceptions in a generic way
			String userErrMsg = "Failed to create new Jobsheet. Reason: " + ex.getMessage();
			// Rollback
			try
			{
				if (!mVecJobsheetItems.isEmpty())
				{
					Log.printDebug("Rolling back the creation of JobsheetItems");
					for (int i = 0; i < mVecJobsheetItems.size(); i++)
					{
						JobsheetItem lInvItem = (JobsheetItem) mVecJobsheetItems.get(i);
						lInvItem.remove();
					}
				}
				if (mJobsheet != null)
				{
					Log.printDebug("Rolling back the creation of Jobsheet");
					mJobsheet.remove();
				}
				if (mSalesTxn != null)
				{
					Log.printDebug("Rolling back the creation of SalesTxn");
					mSalesTxn.remove();
				}
			} catch (Exception rollbackEx)
			{
				userErrMsg += ". WARNING: Failed to RollBack Jobsheet Creation.";
				userErrMsg += " Some objects need to be manually deleted from the Database";
				// Log.printDebug(mStrErrMsg);
				// result=false;
			}
			Log.printDebug(strClassName + "::" + funcName + " - " + userErrMsg);
			req.setAttribute("strErrMsg", userErrMsg);
			return false;
		}
		// return true;
	}

	protected void fnGetCustSalesCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		/*
		 * Collection colActiveSvcC = CustServiceCenterNut.getCollectionByField(
		 * CustServiceCenterBean.STATUS, CustServiceCenterBean.STATUS_ACTIVE);
		 * Iterator itrActiveSvcC = colActiveSvcC.iterator();
		 */
		// Alex: Optimizing retrieval of CustSvcCtr list
		Collection colActiveSvcCtrObj = CustServiceCenterNut.getActiveValObj();
		Iterator itrActiveSvcC = colActiveSvcCtrObj.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	private void validateHeaderFields(String iCustSvcCtrId, String tsJobsheetDate, String strSupervisorUname,
			String strTechnicianUname, String strUserName, String strJobsheetRemarks, String strCurrency,
			String tsCreate, String tsCompleteDate, String iVehicleMileage, Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateHeaderFields()");
		// Put all fields back into HashMap
		mapGood.put("CustSvcCtrId", iCustSvcCtrId);
		mapGood.put("jobsheetDate", tsJobsheetDate);
		mapGood.put("supervisorUname", strSupervisorUname);
		mapGood.put("technicianUname", strTechnicianUname);
		mapGood.put("userName", strUserName);
		mapGood.put("jobsheetRemarks", strJobsheetRemarks);
		mapGood.put("currency", strCurrency);
		mapGood.put("tsCreate", tsCreate);
		mapGood.put("completeDate", tsCompleteDate);
		mapGood.put("vehicleMileage", iVehicleMileage);
		// iCustSvcCtrId: requires no validation(chosen by select options)
		// tsjobsheetDate: client-side validation
		/*
		 * try { new Timestamp(tsJobsheetDate); } catch (Exception ex) {
		 * mapError.put("jobsheetDate", "Invalid Date Format"); }
		 */
		// strSupervisorUname, strTechnicianUname, strUserName:
		// [1] validate valid username
		// Integer iUsrId = null;
		/*
		 * Integer supId = null; Integer tecId = null; UserHome lUserHome =
		 * UserNut.getHome(); try { User lUser = UserNut.getHandle(lUserHome,
		 * strUserName); iUsrId = (Integer) lUser.getUserId(); } catch
		 * (Exception ex) { mapError.put("strUserName", "Invalid User"); } try {
		 * User supUser = UserNut.getHandle( lUserHome,strSupervisorUname);
		 * supId = (Integer) supUser.getUserId(); } catch (Exception ex) {
		 * mapError.put("strSupervisorUname", "Invalid User"); }
		 * 
		 * try { User tecUser = UserNut.getHandle(
		 * lUserHome,strTechnicianUname); tecId = (Integer) tecUser.getUserId(); }
		 * catch(Exception ex) { mapError.put("strTechnicianUname", "Invalid
		 * User"); }
		 */
		// jobsheetRemarks: requires no validation
		// currency: requires no validation
		// tsCreate: requires no validation
		// completeDate: client-side validation
		/*
		 * try { new Timestamp(tsCompleteDate); } catch (Exception ex) {
		 * mapError.put("completeDate", "Invalid Date Format"); }
		 */
		// vehicleMileage: client-side validation
	}

	private void validateRowFields(String[] strPosTypeArr, String[] strDeptCodeArr, String[] strItemRmksArr,
			String[] strItemCodeArr, String[] bdItemQtyArr, String[] bdItemStdPriceArr, String[] bdItemQuotedPriceArr,
			Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateRowFields()");
		// Put ALL arrays (whether good or bad) back to mapGood
		mapGood.put("pos_type_arr", strPosTypeArr);
		mapGood.put("pos_item_rmks_arr", strItemRmksArr);
		mapGood.put("pos_item_id_arr", strItemCodeArr);
		mapGood.put("pos_serv_code_arr", strDeptCodeArr);
		mapGood.put("posJobsheetItemQty_arr", bdItemQtyArr);
		mapGood.put("posJobsheetItemStdPrice_arr", bdItemStdPriceArr);
		mapGood.put("posJobsheetItemQuotedPrice_arr", bdItemQuotedPriceArr);
		// Validation ...
		for (int i = 0; i < strPosTypeArr.length; i++)
		{
			if (!strPosTypeArr[i].equals(POSItemBean.TYPE_NONE))
			{
				// Do not apply validation for Non-Inventory
				// which has empty item code (defaults to NON-INV)
				if (strPosTypeArr[i].equals(POSItemBean.TYPE_NINV) && strItemCodeArr[i].trim().equals(""))
				{
					continue;
				}
				// strPosType: no validation needed
				// strDeptCode: no validation needed
				// strItemRmks: no validation needed
				// strItemCode: validate code
				if (POSItemNut.getPosItemIdByCode(strPosTypeArr[i], strItemCodeArr[i].trim()) == null)
				{
					String key = "pos_item_id" + i;
					mapError.put(key, "Invalid Code");
				}
				/*
				 * if (strPosTypeArr[i].equals(POSItemBean.TYPE_SVC)) { if
				 * (ServiceNut.getObjectByCode(strItemCodeArr[i].trim()) ==
				 * null) { String key = "pos_item_id" + i; mapError.put(key,
				 * "Invalid service code"); } } else if
				 * (strPosTypeArr[i].equals(POSItemBean.TYPE_PKG)) { if
				 * (PackageNut.getObjectByCode(strItemCodeArr[i].trim()) ==
				 * null) { String key = "pos_item_id" + i; mapError.put(key,
				 * "Invalid package code"); } } else { if
				 * (ItemNut.getObjectByCode(strItemCodeArr[i].trim()) == null) {
				 * String key = "pos_item_id" + i; mapError.put(key, "Invalid
				 * item code"); } }
				 */
				// bdItemQty: client-side validation
				// bdItemStdPrice: client-side validation
				// bdItemQuotedPrice: client-side validation
			} // end if !(TYPE_NONE)
		} // end for
	}

	private void validateTradeInFields(HttpServletRequest req, Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateTradeInFields()");
		String[] tradeInNameArray = req.getParameterValues("tradeInName");
		String[] tradeInDescArray = req.getParameterValues("tradeInDesc");
		String[] tradeInQtyArray = req.getParameterValues("tradeInQty");
		String[] tradeInCostPriceArray = req.getParameterValues("tradeInCostPrice");
		// Put ALL arrays (whether good or bad) back to mapGood
		mapGood.put("tradeInName_arr", tradeInNameArray);
		mapGood.put("tradeInDesc_arr", tradeInDescArray);
		mapGood.put("tradeInQty_arr", tradeInQtyArray);
		mapGood.put("tradeInCostPrice_arr", tradeInCostPriceArray);
		// Validation ...
		/*
		 * for (int i=0; i<tradeInNameArray.length; i++) { // So far no
		 * validation required } // end for
		 */
		// Propagate the iTradeInRow
		String strTradeInRows = req.getParameter("iTradeInRows");
		req.setAttribute("iTradeInRows", new Integer(strTradeInRows));
	}

	private String fnPopulateValObjForJS(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			HashMap mapGood, HashMap mapError, JobsheetObject jobsheetObj, SalesTxnObject salesTxnObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{ // super huge try block. TO_DO: Make this more granular later (if
			// need be)
		// String[] theArray = req.getParameterValues("desc");
		// req.setAttribute("theArray",theArray);
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			String strCustSvcCtrId = req.getParameter("CustSvcCtrId");
			// Integer salesTxnId = new Integer((String)
			// req.getParameter("salesTxnId"));
			String strJobsheetDate = (String) req.getParameter("jobsheetDate");
			Log.printVerbose(strClassName + "Getting form variables 2");
			String strSupervisorUname = (String) req.getParameter("supervisorUname");
			String strTechnicianUname = (String) req.getParameter("technicianUname");
			String strUserName = (String) req.getParameter("userName");
			Log.printVerbose(strClassName + "Getting form variables 3");
			String strJobsheetRmks = (String) req.getParameter("jobsheetRemarks");
			String strCcy = (String) req.getParameter("currency");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			Log.printVerbose(strClassName + "Getting form variables 4");
			String strTimeStampComplete = (String) req.getParameter("completeDate");
			String strCustAccId = (String) req.getParameter("custAccountId");
			Log.printDebug("custAccId = " + strCustAccId);
			/*
			 * String strVehicleNo = (String)req.getParameter("vehicleNo");
			 * Log.printDebug("vehicleNo = " + strVehicleNo);
			 */
			String strVehicleId = (String) req.getParameter("vehicleId");
			String strVehicleMileage = (String) req.getParameter("vehicleMileage");
			if (strVehicleId == null)
				strVehicleId = "0";
			if (strVehicleMileage == null)
				strVehicleMileage = "n/a";
			Log.printDebug("vehicleId = " + strVehicleId);
			Log.printDebug("vehicleMileage = " + strVehicleMileage);
			Log.printVerbose(strClassName + ": tsCreate=" + strTimeStampCreate);
			String[] strPosTypeArray = req.getParameterValues("pos_type");
			String[] strPosServCodeArray = req.getParameterValues("pos_serv_code");
			String[] strPosRemarksArray = req.getParameterValues("pos_item_rmks");
			String[] strPosItemIdArray = req.getParameterValues("pos_item_id");
			for (int i = 0; i < strPosItemIdArray.length; i++)
				Log.printVerbose("posItemId[" + i + "] = " + strPosItemIdArray[i]);
			String[] strJobsheetItemQty = req.getParameterValues("posJobsheetItemQty");
			String[] strJobsheetItemRecommendedPrice = req.getParameterValues("posJobsheetItemStdPrice");
			String[] strJobsheetItemQuotedPrice = req.getParameterValues("posJobsheetItemQuotedPrice");
			/*******************************************************************
			 * BEGIN: VALIDATION
			 ******************************************************************/
			// HashMap mapError = new HashMap();
			// HashMap mapGood = new HashMap();
			validateHeaderFields(strCustSvcCtrId, strJobsheetDate, strSupervisorUname, strTechnicianUname, strUserName,
					strJobsheetRmks, strCcy, strTimeStampCreate, strTimeStampComplete, strVehicleMileage, mapError,
					mapGood);
			validateRowFields(strPosTypeArray, strPosServCodeArray, strPosRemarksArray, strPosItemIdArray,
					strJobsheetItemQty, strJobsheetItemRecommendedPrice, strJobsheetItemQuotedPrice, mapError, mapGood);
			validateTradeInFields(req, mapError, mapGood);
			if (mapError.size() > 0)
			{
				Log.printVerbose("*** mapError contains: " + Arrays.asList(mapError.keySet().toArray()).toString());
				// contains error, do not proceed, but propagate the hidden
				// fields!!
				req.setAttribute("custAccId", new Integer(strCustAccId));
				req.setAttribute("vehicleId", new Integer(strVehicleId));
				return "Failed Jobsheet Validation";
			}
			/*******************************************************************
			 * END: VALIDATION
			 ******************************************************************/
			// the line below is not implemented yet because
			// at this stage, we assume all transactions are cash based. //
			// todo: once the Customer Vehicle/Customer Identifier
			// is determined. We will populate this field accordingly
			Integer customerAccountId = null;
			// customerAccountId =
			// VehicleNut.getCustIdGivenRegNum(strVehicleNo); // Alex commented
			// out: 9 Nov
			// TO_DO: Double check the strCustAccId.compareTo("NONE") logic
			// below !!
			if (strCustAccId == null || strCustAccId.compareTo("NONE") == 0)
			{
				Log.printDebug("Customer Account Id is NULL! " + "defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			} else
			{
				customerAccountId = new Integer(strCustAccId);
			}
			if (customerAccountId == null)
			{
				// Log.printDebug("Failure in retrieving customer for vehicle no
				// = " + strVehicleNo
				Log.printDebug(" Null customerAccId ... defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			}
			if (strUserName == null || strSupervisorUname == null || strTechnicianUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer supId = null;
			Integer tecId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User supUser = UserNut.getHandle(lUserHome, strSupervisorUname);
				supId = (Integer) supUser.getUserId();
				User tecUser = UserNut.getHandle(lUserHome, strTechnicianUname);
				tecId = (Integer) tecUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// JobsheetObject jobsheetObj = new JobsheetObject();
			// jobsheetObj.mTimeCreated= TimeFormat.createTimeStamp(
			// strTimeStampCreate);
			jobsheetObj.mTimeCreated = TimeFormat.createTimeStamp(strJobsheetDate);
			jobsheetObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampComplete);
			jobsheetObj.mSupervisorId = new Integer(supId.intValue());
			jobsheetObj.mTechnicianId = new Integer(tecId.intValue());
			jobsheetObj.mCurrency = strCcy;
			jobsheetObj.mRemarks = strJobsheetRmks;
			jobsheetObj.mType = JobsheetBean.TYPE_JS;
			jobsheetObj.mState = JobsheetBean.STATE_CREATED;
			jobsheetObj.mStatus = JobsheetBean.STATUS_ACTIVE;
			jobsheetObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			jobsheetObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strPosTypeArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				JobsheetItemObject iio = new JobsheetItemObject();
				// This is the tricky part because we will have to
				// traverse the database to determine the POS Item Id
				// Based on POS Item Type, and the corresponding Item Code
				// (either pkg, svc or inv). strPosTypeArray[nCount] and
				// strPOSItemIdArray[nCount]
				// if(!strPosTypeArray[nCount].equals(JobsheetItemBean.TYPE_NONE))
				if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NONE) != 0)
				{
					// iio.mPosItemId = new Integer("0");
					// ok beware !, need to obtain pkid of posItem given the
					// code
					// For posType=TYPE_NINV and itemCode is not selected,
					// default itemCode to "NON-INV"
					if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NINV) == 0
							&& strPosItemIdArray[nCount].trim().compareTo("") == 0)
					{
						// strPosItemIdArray[nCount] = ItemBean.CODE_NON_INV;
					}
					Integer lPosItemId = POSItemNut.getPosItemIdByCode(strPosTypeArray[nCount],
							strPosItemIdArray[nCount].trim());
					Log.printVerbose(strClassName + funcName + " creating Jobsheet Items");
					iio.mPosItemId = lPosItemId;
					// iio.mRemarks = new String("none");
					iio.mRemarks = strPosRemarksArray[nCount];
					iio.mTotalQty = new BigDecimal(strJobsheetItemQty[nCount].trim());
					iio.mCurrency = new String(strCcy);
					iio.mUnitPriceRecommended = new BigDecimal(strJobsheetItemRecommendedPrice[nCount].trim());
					iio.mUnitPriceQuoted = new BigDecimal(strJobsheetItemQuotedPrice[nCount].trim());
					iio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
					if (!strPosServCodeArray[nCount].equals(""))
					{
						iio.mStrName1 = JobsheetItemBean.STRNAME1_DEPTCODE;
						iio.mStrValue1 = strPosServCodeArray[nCount];
					}
					jobsheetObj.vecJobsheetItems.add(iio);
					Log.printVerbose(strClassName + funcName + " successfully created one Jobsheet Items");
					// vince:todo- Alex, please help to add a loop/sum/variable
					// to add up all the service items
					// and then when the for/loop is done.. we need to execute
					// the following line
					// jobsheetObj.vecJobsheetItems.add(iioSvcTax);
					// where iioSvcTax is the object that you create to account
					// for all the service items in the bill.
					// of course.. inside iioSvcTax we need to have.....
					// iioSvcTax.mPosItemId = POSItemBean.PKID_GOV_SVC_TAX;
					// Please ask me when in doubt.
				}
			}
			// Alex: Although we have custAccId, we should store the
			// CustAccountObject
			// under SalesTxnObject so that it becomes easier to retrieve
			// all info later.
			// Given the customerAccountId, load the CustAccountObject,
			CustAccountObject lCustAccObj = CustAccountNut.getObject(customerAccountId, "");
			Log.printVerbose(lCustAccObj.toString());
			// SalesTxnObject salesTxnObj = new SalesTxnObject();
			salesTxnObj.custAccId = customerAccountId;
			salesTxnObj.custSvcCtrId = new Integer(strCustSvcCtrId);
			salesTxnObj.txnTime = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.remarks = strJobsheetRmks;
			// salesTxnObj.state = SalesTxnBean.ST_CREATED;
			salesTxnObj.state = SalesTxnBean.ST_JOBSHEET_OK;
			salesTxnObj.refForeignTable = VehicleBean.TABLENAME;
			salesTxnObj.refForeignKey = new Integer(strVehicleId);
			salesTxnObj.status = SalesTxnBean.STATUS_ACTIVE;
			salesTxnObj.lastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.userIdUpdate = new Integer(iUsrId.intValue());
			salesTxnObj.strName1 = "mileage";
			salesTxnObj.strValue1 = strVehicleMileage;
			// this.mJobsheetObj = jobsheetObj;
			salesTxnObj.vecJobsheets.add(jobsheetObj);
			// Alex added this ...
			salesTxnObj.custAccObj = lCustAccObj;
			// this.mSalesTxnObj = salesTxnObj;
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate Jobsheet Item Parameters. ";
			errMsg += "Please ensure that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	private String fnPopulateValObjForSO(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			HashMap mapGood, HashMap mapError, JobsheetObject jobsheetObj, SalesTxnObject salesTxnObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{ // super huge try block. TO_DO: Make this more granular later (if
			// need be)
		// String[] theArray = req.getParameterValues("desc");
		// req.setAttribute("theArray",theArray);
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			String strCustSvcCtrId = req.getParameter("CustSvcCtrId");
			// Integer salesTxnId = new Integer((String)
			// req.getParameter("salesTxnId"));
			String strJobsheetDate = (String) req.getParameter("jobsheetDate");
			Log.printVerbose(strClassName + "Getting form variables 2");
			String strSupervisorUname = (String) req.getParameter("supervisorUname");
			String strTechnicianUname = (String) req.getParameter("technicianUname");
			String strUserName = (String) req.getParameter("userName");
			Log.printVerbose(strClassName + "Getting form variables 3");
			String strJobsheetRmks = (String) req.getParameter("jobsheetRemarks");
			String strCcy = (String) req.getParameter("currency");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			Log.printVerbose(strClassName + "Getting form variables 4");
			String strTimeStampComplete = (String) req.getParameter("completeDate");
			String strCustAccId = (String) req.getParameter("custAccountId");
			Log.printDebug("custAccId = " + strCustAccId);
			Log.printVerbose(strClassName + ": tsCreate=" + strTimeStampCreate);
			String[] strPosTypeArray = req.getParameterValues("pos_type");
			String[] strPosServCodeArray = req.getParameterValues("pos_serv_code");
			String[] strPosRemarksArray = req.getParameterValues("pos_item_rmks");
			String[] strPosItemIdArray = req.getParameterValues("pos_item_id");
			for (int i = 0; i < strPosItemIdArray.length; i++)
				Log.printVerbose("posItemId[" + i + "] = " + strPosItemIdArray[i]);
			String[] strJobsheetItemQty = req.getParameterValues("posJobsheetItemQty");
			String[] strJobsheetItemRecommendedPrice = req.getParameterValues("posJobsheetItemStdPrice");
			String[] strJobsheetItemQuotedPrice = req.getParameterValues("posJobsheetItemQuotedPrice");
			/*******************************************************************
			 * BEGIN: VALIDATION
			 ******************************************************************/
			// HashMap mapError = new HashMap();
			// HashMap mapGood = new HashMap();
			validateHeaderFields(strCustSvcCtrId, strJobsheetDate, strSupervisorUname, strTechnicianUname, strUserName,
					strJobsheetRmks, strCcy, strTimeStampCreate, strTimeStampComplete, "", mapError, mapGood);
			validateRowFields(strPosTypeArray, strPosServCodeArray, strPosRemarksArray, strPosItemIdArray,
					strJobsheetItemQty, strJobsheetItemRecommendedPrice, strJobsheetItemQuotedPrice, mapError, mapGood);
			validateTradeInFields(req, mapError, mapGood);
			if (mapError.size() > 0)
			{
				Log.printVerbose("*** mapError contains: " + Arrays.asList(mapError.keySet().toArray()).toString());
				// contains error, do not proceed, but propagate the hidden
				// fields!!
				req.setAttribute("custAccId", new Integer(strCustAccId));
				// req.setAttribute("vehicleId", new Integer(strVehicleId));
				return "Failed Jobsheet Validation";
			}
			/*******************************************************************
			 * END: VALIDATION
			 ******************************************************************/
			// the line below is not implemented yet because
			// at this stage, we assume all transactions are cash based. //
			// todo: once the Customer Vehicle/Customer Identifier
			// is determined. We will populate this field accordingly
			Integer customerAccountId = null;
			// customerAccountId =
			// VehicleNut.getCustIdGivenRegNum(strVehicleNo); // Alex commented
			// out: 9 Nov
			// TO_DO: Double check the strCustAccId.compareTo("NONE") logic
			// below !!
			if (strCustAccId == null || strCustAccId.compareTo("NONE") == 0)
			{
				Log.printDebug("Customer Account Id is NULL! " + "defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			} else
			{
				customerAccountId = new Integer(strCustAccId);
			}
			if (customerAccountId == null)
			{
				// Log.printDebug("Failure in retrieving customer for vehicle no
				// = " + strVehicleNo
				Log.printDebug(" Null customerAccId ... defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			}
			if (strUserName == null || strSupervisorUname == null || strTechnicianUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer supId = null;
			Integer tecId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User supUser = UserNut.getHandle(lUserHome, strSupervisorUname);
				supId = (Integer) supUser.getUserId();
				User tecUser = UserNut.getHandle(lUserHome, strTechnicianUname);
				tecId = (Integer) tecUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// JobsheetObject jobsheetObj = new JobsheetObject();
			// jobsheetObj.mTimeCreated= TimeFormat.createTimeStamp(
			// strTimeStampCreate);
			jobsheetObj.mTimeCreated = TimeFormat.createTimeStamp(strJobsheetDate);
			jobsheetObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampComplete);
			jobsheetObj.mSupervisorId = new Integer(supId.intValue());
			jobsheetObj.mTechnicianId = new Integer(tecId.intValue());
			jobsheetObj.mCurrency = strCcy;
			jobsheetObj.mRemarks = strJobsheetRmks;
			jobsheetObj.mType = JobsheetBean.TYPE_SO;
			jobsheetObj.mState = JobsheetBean.STATE_CREATED;
			jobsheetObj.mStatus = JobsheetBean.STATUS_ACTIVE;
			jobsheetObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			jobsheetObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strPosTypeArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				JobsheetItemObject iio = new JobsheetItemObject();
				// This is the tricky part because we will have to
				// traverse the database to determine the POS Item Id
				// Based on POS Item Type, and the corresponding Item Code
				// (either pkg, svc or inv). strPosTypeArray[nCount] and
				// strPOSItemIdArray[nCount]
				// if(!strPosTypeArray[nCount].equals(JobsheetItemBean.TYPE_NONE))
				if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NONE) != 0)
				{
					// iio.mPosItemId = new Integer("0");
					// ok beware !, need to obtain pkid of posItem given the
					// code
					// For posType=TYPE_NINV and itemCode is not selected,
					// default itemCode to "NON-INV"
					if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NINV) == 0
							&& strPosItemIdArray[nCount].trim().compareTo("") == 0)
					{
						// strPosItemIdArray[nCount] = ItemBean.CODE_NON_INV;
					}
					Integer lPosItemId = POSItemNut.getPosItemIdByCode(strPosTypeArray[nCount],
							strPosItemIdArray[nCount].trim());
					Log.printVerbose(strClassName + funcName + " creating Jobsheet Items");
					iio.mPosItemId = lPosItemId;
					// iio.mRemarks = new String("none");
					iio.mRemarks = strPosRemarksArray[nCount];
					iio.mTotalQty = new BigDecimal(strJobsheetItemQty[nCount].trim());
					iio.mCurrency = new String(strCcy);
					iio.mUnitPriceRecommended = new BigDecimal(strJobsheetItemRecommendedPrice[nCount].trim());
					iio.mUnitPriceQuoted = new BigDecimal(strJobsheetItemQuotedPrice[nCount].trim());
					iio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
					if (!strPosServCodeArray[nCount].equals(""))
					{
						iio.mStrName1 = JobsheetItemBean.STRNAME1_DEPTCODE;
						iio.mStrValue1 = strPosServCodeArray[nCount];
					}
					jobsheetObj.vecJobsheetItems.add(iio);
					Log.printVerbose(strClassName + funcName + " successfully created one Jobsheet Items");
					// vince:todo- Alex, please help to add a loop/sum/variable
					// to add up all the service items
					// and then when the for/loop is done.. we need to execute
					// the following line
					// jobsheetObj.vecJobsheetItems.add(iioSvcTax);
					// where iioSvcTax is the object that you create to account
					// for all the service items in the bill.
					// of course.. inside iioSvcTax we need to have.....
					// iioSvcTax.mPosItemId = POSItemBean.PKID_GOV_SVC_TAX;
					// Please ask me when in doubt.
				}
			}
			// Alex: Although we have custAccId, we should store the
			// CustAccountObject
			// under SalesTxnObject so that it becomes easier to retrieve
			// all info later.
			// Given the customerAccountId, load the CustAccountObject,
			CustAccountObject lCustAccObj = CustAccountNut.getObject(customerAccountId, "");
			Log.printVerbose(lCustAccObj.toString());
			// SalesTxnObject salesTxnObj = new SalesTxnObject();
			salesTxnObj.custAccId = customerAccountId;
			salesTxnObj.custSvcCtrId = new Integer(strCustSvcCtrId);
			salesTxnObj.txnTime = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.remarks = strJobsheetRmks;
			// salesTxnObj.state = SalesTxnBean.ST_CREATED;
			salesTxnObj.state = SalesTxnBean.ST_JOBSHEET_OK;
			// salesTxnObj.refForeignTable = VehicleBean.TABLENAME;
			// salesTxnObj.refForeignKey = new Integer(strVehicleId);
			salesTxnObj.status = SalesTxnBean.STATUS_ACTIVE;
			salesTxnObj.lastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.userIdUpdate = new Integer(iUsrId.intValue());
			// salesTxnObj.strName1 = "mileage";
			// salesTxnObj.strValue1 = strVehicleMileage;
			// this.mJobsheetObj = jobsheetObj;
			salesTxnObj.vecJobsheets.add(jobsheetObj);
			// Alex added this ...
			salesTxnObj.custAccObj = lCustAccObj;
			// this.mSalesTxnObj = salesTxnObj;
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate Jobsheet Item Parameters. ";
			errMsg += "Please ensure that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	/*
	 * protected boolean fnCheckVehicleNo(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { String funcName =
	 * "fnCheckVehicleNo()"; Log.printVerbose("In " + funcName);
	 * 
	 * mVehicleNo = req.getParameter("vehicleNo");
	 * 
	 * Vehicle vehicleEJB = VehicleNut.getObjectByRegNum(mVehicleNo);
	 * if(vehicleEJB!=null) { try { // repopulate the real vehicle no as
	 * retrieved from the DB mVehicleNo = vehicleEJB.getRegNum(); mCustAccId =
	 * vehicleEJB.getCustAccountId(); } catch(Exception ex) {
	 * Log.printDebug("Failed to retrieve regNum from vehicle EJB!!"); } return
	 * true; }
	 * 
	 * Log.printVerbose("*** Vehicle No not found in the DB ***");
	 * 
	 * return false;
	 *  }
	 */
	// protected synchronized boolean fnCreateNewCustomer(HttpServlet servlet,
	protected boolean fnCreateNewCustomer(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCreateNewCustomer()";
		/**
		 * STEP 1: Create the main CustAccount EJB
		 */
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String custCode = (String) req.getParameter("custCode");
		// String custNewPkid = CustAccountNut.getNextPkid();
		String custName = (String) req.getParameter("custName");
		String custDesc = (String) req.getParameter("custDesc");
		HttpSession session = req.getSession();
		Integer userid = (Integer) session.getAttribute("userId");
		GUIDGenerator guid = null;
		try
		{
			guid = new GUIDGenerator();
		} catch (Exception ex)
		{
			return false;
		}
		CustAccountObject custObj = new CustAccountObject();
		custObj.name = custName;
		custObj.description = custDesc;
		custObj.custAccountCode = guid.getUUID();
		custObj.userIdUpdate = userid;
		custObj.telephone1 = req.getParameter("telephone1");
		custObj.mainAddress1 = req.getParameter("mainAddress1");
		custObj.mainAddress2 = req.getParameter("mainAddress2");
		custObj.mainAddress3 = req.getParameter("mainAddress3");
		custObj.mainPostcode = req.getParameter("mainPostcode");
		custObj.mainState = req.getParameter("mainState");
		custObj.mainCountry = req.getParameter("mainCountry");
		// Ensure CustAccount does not exist
		CustAccount newCustAccount = CustAccountNut.fnCreate(custObj);
		try
		{
			custObj = newCustAccount.getObject("");
		} catch (Exception ex)
		{
			if (newCustAccount != null)
			{
				try
				{
					newCustAccount.remove();
				} catch (Exception ex2)
				{
				}
				;
			}
			return false;
		}
		// For Vehicle
		VehicleObject newVehObj = new VehicleObject();
		newVehObj.regNum = req.getParameter("vehicleNo");
		newVehObj.model = req.getParameter("vehicleModel");
		newVehObj.userIdUpdate = userid;
		newVehObj.custAccountId = custObj.pkid;
		Vehicle vehEJB = VehicleNut.fnCreate(newVehObj);
		try
		{
			newVehObj = vehEJB.getObject();
		} catch (Exception ex)
		{
			if (vehEJB != null)
			{
				try
				{
					newCustAccount.remove();
					vehEJB.remove();
				} catch (Exception ex2)
				{
				}
			}
			return false;
		}
		req.setAttribute("vehicleId", newVehObj.pkid);
		req.setAttribute("custAccId", custObj.pkid);
		return true;
	} // end fnCreateNewCustomer
}
/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoNewJobsheet implements Action
{
	String strClassName = "DoNewJobsheet";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		try
		{
			// Form Handlers
			String formName = req.getParameter("formName");
			// Regardless of the usage of the form..
			// we will populate the following
			// fnGetCustSalesCenterList(servlet, req, res);
			// If the form name is null,
			// the users are creating a fresh jobsheet
			if (formName == null)
			{
				Log.printVerbose(strClassName + "formName: null");
				// return new ActionRouter("pos-create-new-jobsheet-01-page");
				return new ActionRouter("pos-check-customer-by-vehicle-page");
			}
			if (formName.equals("retrieveVehicleNo"))
			{
				// define the default number of rows for the jobsheet
				// or the invoices
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// check vehicle number
				String mVehicleNo = req.getParameter("vehicleNo");
				Integer mCustAccId = null;
				Vehicle vehicleEJB = VehicleNut.getObjectByRegNum(mVehicleNo);
				if (vehicleEJB != null)
				{
					try
					{
						// repopulate the real vehicle no as retrieved from the
						// DB
						Integer mVehicleId = vehicleEJB.getPkid();
						mVehicleNo = vehicleEJB.getRegNum();
						mCustAccId = vehicleEJB.getCustAccountId();
						fnGetUserList(servlet, req, res);
						fnGetCustSalesCenterList(servlet, req, res);
						// req.setAttribute("vehicleNo", mVehicleNo);
						req.setAttribute("vehicleId", mVehicleId);
						req.setAttribute("custAccId", mCustAccId);
						return new ActionRouter("pos-create-new-jobsheet-01-page");
					} catch (Exception ex)
					{
						String strErrMsg = "Failed to retrieve regNum from vehicle EJB!!";
						Log.printDebug(strErrMsg);
						req.setAttribute("strErrMsg", strErrMsg);
						return new ActionRouter("pos-check-customer-by-vehicle-page");
					}
				} else
				{
					Log.printVerbose("*** Vehicle No not found in the DB ***");
					// New Customer
					Log.printVerbose(strClassName + "Vehicle No does not match any customer. Creating new ...");
					req.setAttribute("vehicleNo", mVehicleNo);
					// req.setAttribute("strErrMsg", mStrErrMsg);
					return new ActionRouter("pos-create-new-customer-page");
				}
			}
			if (formName.equals("createNewCustomer"))
			{
				// Add a new customer
				if (!fnCreateNewCustomer(servlet, req, res))
				{
					String strErrMsg = "Error in creating new customer. " + "Please try again";
					Log.printDebug(strClassName + ": " + strErrMsg);
					// req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-new-customer-page");
				}
				// Successfully created New Customer
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// Set the rest of the attributes
				/*
				 * SET THIS IN fnCreateNewCustomer req.setAttribute("strErrMsg",
				 * this.mStrErrMsg); req.setAttribute("vehicleNo",
				 * this.mVehicleNo); req.setAttribute("custAccId",
				 * this.mCustAccId);
				 */
				return new ActionRouter("pos-create-new-jobsheet-01-page");
			}
			if (formName.equals("retrieveItnlCust"))
			{
				// Obtain the custAccountId
				String strCustAccId = req.getParameter("iItnlCustAccId");
				if (strCustAccId == null)
				{
					String strErrMsg = "Could not obtain internal CustAccId";
					Log.printDebug(strErrMsg);
					req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-new-itnl-sales-page");
				}
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// req.setAttribute("vehicleNo", this.mVehicleNo);
				req.setAttribute("custAccId", new Integer(strCustAccId));
				return new ActionRouter("pos-create-new-jobsheet-01-page");
			}
			if (formName.equals("retrieveCustAccId"))
			{
				// Obtain the custAccountId
				String strCustAccId = req.getParameter("custAccId");
				if (strCustAccId == null)
				{
					String strErrMsg = "Could not obtain CustAccId";
					Log.printDebug(strErrMsg);
					req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-salesorder-01-page");
				}
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// req.setAttribute("vehicleNo", this.mVehicleNo);
				req.setAttribute("custAccId", new Integer(strCustAccId));
				return new ActionRouter("pos-create-salesorder-02-page");
			}
			// If the form has been filled and user submit to create
			// a new jobsheet, do the necessary checking
			if (formName.equals("createNewJobsheet"))
			{
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// boolean bTradeInOK = false;
				boolean bJobsheetVerified = false;
				Log.printVerbose(strClassName + ": before fnCreateNewJobsheet");
				bJobsheetVerified = fnCreateNewJobsheet(servlet, req, res, false);
				// bTradeInOK = fnCreateNewTradeIn(servlet, req, res);
				// Log.printVerbose(this.mStrErrMsg);
				// tentatively.. force it to true first..
				// this is to test the display
				// bJobsheetVerified = true;
				if (bJobsheetVerified == true)
				{
					return new ActionRouter("pos-create-new-jobsheet-02-page");
				} else
				{
					// Some errors must have occurred in fnCreateNewJobsheet
					// Rollback transactions
					// rollBackTxn();
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					// propagate iRow
					String strRows = req.getParameter("iRows");
					Integer iRows = null;
					if (strRows != null)
					{
						iRows = new Integer(strRows);
					} else
					{
						iRows = new Integer("15");
					}
					req.setAttribute("iRows", iRows);
					return new ActionRouter("pos-create-new-jobsheet-01-page");
				}
			}
			if (formName.equals("createSalesOrder"))
			{
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				boolean bJobsheetVerified = false;
				Log.printVerbose(strClassName + ": before fnCreateNewJobsheet");
				bJobsheetVerified = fnCreateNewJobsheet(servlet, req, res, true);
				// Log.printVerbose(this.mStrErrMsg);
				// tentatively.. force it to true first..
				// this is to test the display
				// bJobsheetVerified = true;
				if (bJobsheetVerified == true)
				{
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					// req.setAttribute("salesTxnObj", this.mSalesTxnObj);
					// req.setAttribute("jobsheetObj", this.mJobsheetObj);
					return new ActionRouter("pos-create-salesorder-03-page");
				} else
				{
					// Some errors must have occurred in fnCreateNewJobsheet
					// Rollback transactions
					// rollBackTxn();
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					return new ActionRouter("pos-create-salesorder-02-page");
				}
			}
			// fnGetCustSalesCenterList(servlet, req, res);
			Log.printVerbose(strClassName + ": returning default ActionRouter");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return new ActionRouter("pos-create-new-jobsheet-01-page");
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnGetUserList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector vecUserObj = UserNut.getValueObjectsGiven(UserBean.STATUS, UserBean.ACTIVE, (String) null,
				(String) null, (String) null, (String) null);
		req.setAttribute("vecUserObj", vecUserObj);
	}

	// /////////////////////////////////////////////////////////////////
	protected boolean fnCreateNewTradeIn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
	// HashMap mapGood, HashMap mapError,
			JobsheetObject jobsheetObj)
	{
		// Get all the relevent parameters
		String custSvcCtrId = req.getParameter("CustSvcCtrId");
		String[] tradeInNameArray = req.getParameterValues("tradeInName");
		String[] tradeInDescArray = req.getParameterValues("tradeInDesc");
		String[] tradeInQtyArray = req.getParameterValues("tradeInQty");
		// String[] tradeInSalesPriceArray =
		// req.getParameterValues("tradeInSalesPrice");
		String[] tradeInCostPriceArray = req.getParameterValues("tradeInCostPrice");
		String tradeInCustId = req.getParameter("custAccountId");
		// Item newItem = null;
		POSItem newPOSItemBuy = null;
		// POSItem newPOSItemSell = null;
		Vector vecNewItems = new Vector();
		Vector vecNewPOSItemsBuy = new Vector();
		// Vector vecNewPOSItemsSell = new Vector();
		try
		{ // Super huge try-catch block
			// Get current date/time and username
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				return false;
			}
			/*******************************************************************
			 * Verify Customer PKID
			 ******************************************************************/
			if (CustAccountNut.getHandle(new Integer(tradeInCustId)) == null)
			{
				throw new Exception("Customer (PKID: " + tradeInCustId + ") does not exist!");
			}
			// map ServiceCenter to Location
			Integer thisLocPkid = CustServiceCenterNut.mapSvcCtrToInvLoc(new Integer(custSvcCtrId));
			int nRows = tradeInNameArray.length;
			Log.printVerbose("Number of tradeIn rows = " + nRows);
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				String thisTradeInName = tradeInNameArray[nCount];
				String thisTradeInDesc = tradeInDescArray[nCount];
				String thisTradeInQty = tradeInQtyArray[nCount];
				String thisTradeInCostPrice = tradeInCostPriceArray[nCount];
				// String thisTradeInSalesPrice =
				// tradeInSalesPriceArray[nCount];
				// Check that the TradeIn Name is not empty
				if (thisTradeInName.equals(""))
					continue;
				BigDecimal costPrice = new BigDecimal(thisTradeInCostPrice);
				BigDecimal buyQty = null;
				try
				{
					buyQty = new BigDecimal(thisTradeInQty);
				} catch (Exception ex)
				{
					buyQty = new BigDecimal(1);
				}
				// Here we assume everytime this function is called it is a new
				// trade-in item
				/***************************************************************
				 * CREATE THE TRADE-IN ITEM
				 **************************************************************/
				ItemObject itemObj = new ItemObject();
				// populate the properties here!!
				itemObj.name = thisTradeInName;
				itemObj.description = thisTradeInDesc;
				itemObj.userIdUpdate = usrid;
				itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_TRADEIN);
				itemObj.priceList = costPrice;
				itemObj.priceSale = costPrice;
				itemObj.priceDisc1 = costPrice;
				itemObj.priceDisc2 = costPrice;
				itemObj.priceDisc3 = costPrice;
				itemObj.priceMin = costPrice;
				itemObj.fifoUnitCost = costPrice;
				itemObj.maUnitCost = costPrice;
				itemObj.waUnitCost = costPrice;
				itemObj.lastUnitCost = costPrice;
				itemObj.replacementUnitCost = costPrice;
				// itemObj.preferredSupplier = suppAccObj.pkid;
				Item itemEJB = ItemNut.fnCreateWithCodePrefix(itemObj, ItemBean.CODE_PREFIX_TI);
				if (itemEJB == null)
				{
					throw new Exception("Null Item");
				} else
				{
					Log.printVerbose("Successfully created Item");
				}
				vecNewItems.add(itemEJB);
				/***************************************************************
				 * ADD POS ITEM
				 **************************************************************/
				POSItemHome lPOSItemHome = POSItemNut.getHome();
				// Create one entry for purchase and one more for sales
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = itemEJB.getPkid();
				posObj.itemType = POSItemBean.TYPE_TRADEIN_BUY;
				posObj.currency = "MYR";
				posObj.unitPriceStd = new BigDecimal(thisTradeInCostPrice);
				posObj.unitPriceDiscounted = new BigDecimal(thisTradeInCostPrice);
				posObj.unitPriceMin = new BigDecimal(thisTradeInCostPrice);
				posObj.userIdUpdate = usrid;
				newPOSItemBuy = POSItemNut.fnCreate(posObj);
				if (newPOSItemBuy == null)
					throw new Exception("Null POSItem (Trade-In)!!");
				else
					Log.printVerbose("Successfully created POSItem");
				vecNewPOSItemsBuy.add(newPOSItemBuy);
				/***************************************************************
				 * Populate JobsheetItemObject
				 **************************************************************/
				JobsheetItemObject jsio = new JobsheetItemObject();
				jsio.mPosItemId = newPOSItemBuy.getPkid();
				jsio.mRemarks = "Traded in by Customer PKID " + tradeInCustId;
				jsio.mTotalQty = new BigDecimal(thisTradeInQty);
				jsio.mCurrency = "MYR";
				jsio.mUnitPriceQuoted = new BigDecimal(thisTradeInCostPrice).negate(); // negate
																						// because
																						// stockIn
				jsio.mUnitPriceRecommended = jsio.mUnitPriceQuoted; // temp. put
																	// the same
																	// as quoted
				jsio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
				jobsheetObj.vecJobsheetItems.add(jsio);
				/***************************************************************
				 * ADD TRADE-IN STOCK
				 **************************************************************/
				// At this point, populate the newly created trade-in item
				// code+name
				// req.setAttribute("strTradeInCode", tradeInCode);
				// req.setAttribute("strTradeInName", tradeInName);
			} // end for
		} // end huge-try
		catch (Exception ex)
		{
			// end of super huge try-catch block
			// Rollback all transactions
			// in the following order:
			// 1. POSItem
			// 2. Item
			try
			{
				// Loop through all populated items and start deleting each one
				for (int itemIdx = 0; itemIdx < vecNewItems.size(); itemIdx++)
				{
					Item rollbackItem = (Item) vecNewItems.get(itemIdx);
					if (rollbackItem != null)
						rollbackItem.remove();
				}
				for (int posItemIdx = 0; posItemIdx < vecNewPOSItemsBuy.size(); posItemIdx++)
				{
					POSItem rollbackPOSItem = (POSItem) vecNewPOSItemsBuy.get(posItemIdx);
					if (rollbackPOSItem != null)
						rollbackPOSItem.remove();
				}
			} catch (Exception e)
			{
				Log.printDebug("Failure to rollback transactions !!! ");
			}
			String strErrMsg;
			strErrMsg = "Error while processing a new trade-in item: " + ex.getMessage();
			Log.printDebug(strErrMsg);
			req.setAttribute("strErrMsg", strErrMsg);
			return false;
		}
		return true;
	} // end fnCreateNewTradeIn

	protected boolean fnCreateNewJobsheet(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			boolean isSalesOrder)
	{
		String funcName = "fnCreateNewJobsheet()";
		// Alex (27 Oct) - Changed member variables to local variables.
		String mStrErrMsg = new String("");
		// String strObjectName = new String("DoNewJobsheet");
		JobsheetObject mJobsheetObj = new JobsheetObject();
		SalesTxnObject mSalesTxnObj = new SalesTxnObject();
		SalesTxn mSalesTxn = null;
		Jobsheet mJobsheet = null;
		// String mVehicleNo = null;
		// Integer mCustAccId = null;
		Vector mVecJobsheetItems = new Vector();
		try
		{ // super huge try-catch
			// Populate the value objects including
			// SalesTxn, Jobsheet and jobsheet items
			Log.printVerbose(strClassName + "::fnCreateNewJobsheet" + " before entering fnPopulateValueObject");
			// Alex (04/13) - Added support for thorough field validation
			HashMap mapGood = new HashMap();
			HashMap mapError = new HashMap();
			String errMsg;
			if (isSalesOrder)
			{
				errMsg = fnPopulateValObjForSO(servlet, req, res, mapGood, mapError, mJobsheetObj, mSalesTxnObj);
			} else
			{
				errMsg = fnPopulateValObjForJS(servlet, req, res, mapGood, mapError, mJobsheetObj, mSalesTxnObj);
			}
			Log.printVerbose(strClassName + "::fnCreateNewJobsheet" + " after exiting fnPopulateValueObject");
			if (errMsg != null)
			{
				req.setAttribute("mapGood", mapGood);
				req.setAttribute("mapError", mapError);
				throw new Exception(errMsg);
			}
			// server side form validation
			// 1) create the SalesTxn Object
			// SalesTxn salesTxn = null;
			if (mSalesTxnObj != null)
			{
				mSalesTxn = SalesTxnNut.fnCreate(mSalesTxnObj);
			}
			if (mSalesTxn == null)
			{
				String strErrMsg = "SalesTxnNut.fnCreate failed to create object";
				throw new Exception(strErrMsg);
			} else
			{
				try
				{
					mSalesTxnObj.pkid = (Long) mSalesTxn.getPkid();
					for (int vecIndex = 0; vecIndex < mSalesTxnObj.vecJobsheets.size(); vecIndex++)
					{
						JobsheetObject tmpInvObj = (JobsheetObject) mSalesTxnObj.vecJobsheets.get(vecIndex);
						tmpInvObj.mSalesTxnId = mSalesTxnObj.pkid;
						mSalesTxnObj.vecJobsheets.set(vecIndex, tmpInvObj);
					}
				} catch (Exception ex)
				{
					String strErrMsg = "Failed to create SalesTxnBean: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// We are not creating a loop here, because
			// there's only one jobsheet attached to this sales transaction
			// because it is a new jobsheet.
			// for(int countA=0; countA< this.mSalesTxnObj.vecJobsheets.size();
			// countA++) { }
			// 2.0) create the Jobsheet Object
			// JobsheetObject mJobsheetObj = (JobsheetObject)
			// this.mSalesTxnObj.vecJobsheets.get(0);
			// Jobsheet jobsheetRemote = null;
			/*
			 * Already done in fnPopulateValueObjects() // Alex: 27 Dec 03 - to
			 * cater for SalesOrder, need to add the state field
			 * if(isSalesOrder) mJobsheetObj.mType = JobsheetBean.TYPE_SO;
			 */
			if (mJobsheetObj != null)
			{
				mJobsheet = JobsheetNut.fnCreate(mJobsheetObj);
			}
			if (mJobsheet == null)
			{
				String strErrMsg = "Failed to create new Jobsheet Bean";
				throw new Exception(strErrMsg);
			} else
			{
				try
				{
					mJobsheetObj.mPkid = (Long) mJobsheet.getPkid();
					mJobsheetObj.mStmtNumber = (Long) mJobsheet.getStmtNumber();
				} catch (Exception ex)
				{
					String strErrMsg = "Invalid Jobsheet Primary Key or StmtNumber: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// Check for any trade-in items
			if (!fnCreateNewTradeIn(servlet, req, res, mJobsheetObj))
			{
				throw new Exception("Error while creating new trade-in items");
			}
			// 2.1) create the Jobsheet Items Object selectively
			for (int countA = 0; countA < mJobsheetObj.vecJobsheetItems.size(); countA++)
			{
				JobsheetItemObject jsItemObj = (JobsheetItemObject) mJobsheetObj.vecJobsheetItems.get(countA);
				// Alex added this ..
				// Need to assign the newly created jobsheet
				// pkid into the jsItemObj
				jsItemObj.mJobsheetId = mJobsheetObj.mPkid;
				JobsheetItem invItem = JobsheetItemNut.fnCreate(jsItemObj);
				// if successful, fill the pkid of the valueObject
				if (invItem != null)
				{
					// First thing is to append to the JobsheetItem Vector
					mVecJobsheetItems.add(invItem);
					try
					{
						jsItemObj.mPkid = invItem.getPkid();
					} catch (Exception ex)
					{
						String strErrMsg = "Invalid Inventory Item Primary Key: " + ex.getMessage();
						throw new Exception(strErrMsg);
					}
				} else
				{
					String strErrMsg = "Failure in creating jobsheet item object.";
					throw new Exception(strErrMsg);
				}
			} // end for
			// Convert SalesTxn State to JOBSHEET_OK
			SalesTxn salesTxnEjb = SalesTxnNut.getHandle(mSalesTxnObj.pkid);
			salesTxnEjb.setState(SalesTxnBean.ST_JOBSHEET_OK);
			// Populate attributes
			// req.setAttribute("strErrMsg", "");
			req.setAttribute("salesTxnObj", mSalesTxnObj);
			req.setAttribute("jobsheetObj", mJobsheetObj);
			// If not, return to the original form for correction
			/*
			 * if(errMsg != null) { Log.printDebug(strClassName+" Cannot create
			 * salesTransaction "); mStrErrMsg += errMsg; return false; }
			 */
			// To create a new jobsheet
			// 0. Create a ReceiptBean
			// for now, we assume all transactions are on cash basis.
			// so, a receipt is automatically generated.
			// Also, cash sales are automatically treated as
			// customer with pkid = 1
			// Clean up the database if new jobsheet is not
			// created due to some reasons.
			return true;
		} // end of super huge try
		catch (Exception ex)
		{
			// Handle exceptions in a generic way
			String userErrMsg = "Failed to create new Jobsheet. Reason: " + ex.getMessage();
			// Rollback
			try
			{
				if (!mVecJobsheetItems.isEmpty())
				{
					Log.printDebug("Rolling back the creation of JobsheetItems");
					for (int i = 0; i < mVecJobsheetItems.size(); i++)
					{
						JobsheetItem lInvItem = (JobsheetItem) mVecJobsheetItems.get(i);
						lInvItem.remove();
					}
				}
				if (mJobsheet != null)
				{
					Log.printDebug("Rolling back the creation of Jobsheet");
					mJobsheet.remove();
				}
				if (mSalesTxn != null)
				{
					Log.printDebug("Rolling back the creation of SalesTxn");
					mSalesTxn.remove();
				}
			} catch (Exception rollbackEx)
			{
				userErrMsg += ". WARNING: Failed to RollBack Jobsheet Creation.";
				userErrMsg += " Some objects need to be manually deleted from the Database";
				// Log.printDebug(mStrErrMsg);
				// result=false;
			}
			Log.printDebug(strClassName + "::" + funcName + " - " + userErrMsg);
			req.setAttribute("strErrMsg", userErrMsg);
			return false;
		}
		// return true;
	}

	protected void fnGetCustSalesCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		/*
		 * Collection colActiveSvcC = CustServiceCenterNut.getCollectionByField(
		 * CustServiceCenterBean.STATUS, CustServiceCenterBean.STATUS_ACTIVE);
		 * Iterator itrActiveSvcC = colActiveSvcC.iterator();
		 */
		// Alex: Optimizing retrieval of CustSvcCtr list
		Collection colActiveSvcCtrObj = CustServiceCenterNut.getActiveValObj();
		Iterator itrActiveSvcC = colActiveSvcCtrObj.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	private void validateHeaderFields(String iCustSvcCtrId, String tsJobsheetDate, String strSupervisorUname,
			String strTechnicianUname, String strUserName, String strJobsheetRemarks, String strCurrency,
			String tsCreate, String tsCompleteDate, String iVehicleMileage, Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateHeaderFields()");
		// Put all fields back into HashMap
		mapGood.put("CustSvcCtrId", iCustSvcCtrId);
		mapGood.put("jobsheetDate", tsJobsheetDate);
		mapGood.put("supervisorUname", strSupervisorUname);
		mapGood.put("technicianUname", strTechnicianUname);
		mapGood.put("userName", strUserName);
		mapGood.put("jobsheetRemarks", strJobsheetRemarks);
		mapGood.put("currency", strCurrency);
		mapGood.put("tsCreate", tsCreate);
		mapGood.put("completeDate", tsCompleteDate);
		mapGood.put("vehicleMileage", iVehicleMileage);
		// iCustSvcCtrId: requires no validation(chosen by select options)
		// tsjobsheetDate: client-side validation
		/*
		 * try { new Timestamp(tsJobsheetDate); } catch (Exception ex) {
		 * mapError.put("jobsheetDate", "Invalid Date Format"); }
		 */
		// strSupervisorUname, strTechnicianUname, strUserName:
		// [1] validate valid username
		// Integer iUsrId = null;
		/*
		 * Integer supId = null; Integer tecId = null; UserHome lUserHome =
		 * UserNut.getHome(); try { User lUser = UserNut.getHandle(lUserHome,
		 * strUserName); iUsrId = (Integer) lUser.getUserId(); } catch
		 * (Exception ex) { mapError.put("strUserName", "Invalid User"); } try {
		 * User supUser = UserNut.getHandle( lUserHome,strSupervisorUname);
		 * supId = (Integer) supUser.getUserId(); } catch (Exception ex) {
		 * mapError.put("strSupervisorUname", "Invalid User"); }
		 * 
		 * try { User tecUser = UserNut.getHandle(
		 * lUserHome,strTechnicianUname); tecId = (Integer) tecUser.getUserId(); }
		 * catch(Exception ex) { mapError.put("strTechnicianUname", "Invalid
		 * User"); }
		 */
		// jobsheetRemarks: requires no validation
		// currency: requires no validation
		// tsCreate: requires no validation
		// completeDate: client-side validation
		/*
		 * try { new Timestamp(tsCompleteDate); } catch (Exception ex) {
		 * mapError.put("completeDate", "Invalid Date Format"); }
		 */
		// vehicleMileage: client-side validation
	}

	private void validateRowFields(String[] strPosTypeArr, String[] strDeptCodeArr, String[] strItemRmksArr,
			String[] strItemCodeArr, String[] bdItemQtyArr, String[] bdItemStdPriceArr, String[] bdItemQuotedPriceArr,
			Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateRowFields()");
		// Put ALL arrays (whether good or bad) back to mapGood
		mapGood.put("pos_type_arr", strPosTypeArr);
		mapGood.put("pos_item_rmks_arr", strItemRmksArr);
		mapGood.put("pos_item_id_arr", strItemCodeArr);
		mapGood.put("pos_serv_code_arr", strDeptCodeArr);
		mapGood.put("posJobsheetItemQty_arr", bdItemQtyArr);
		mapGood.put("posJobsheetItemStdPrice_arr", bdItemStdPriceArr);
		mapGood.put("posJobsheetItemQuotedPrice_arr", bdItemQuotedPriceArr);
		// Validation ...
		for (int i = 0; i < strPosTypeArr.length; i++)
		{
			if (!strPosTypeArr[i].equals(POSItemBean.TYPE_NONE))
			{
				// Do not apply validation for Non-Inventory
				// which has empty item code (defaults to NON-INV)
				if (strPosTypeArr[i].equals(POSItemBean.TYPE_NINV) && strItemCodeArr[i].trim().equals(""))
				{
					continue;
				}
				// strPosType: no validation needed
				// strDeptCode: no validation needed
				// strItemRmks: no validation needed
				// strItemCode: validate code
				if (POSItemNut.getPosItemIdByCode(strPosTypeArr[i], strItemCodeArr[i].trim()) == null)
				{
					String key = "pos_item_id" + i;
					mapError.put(key, "Invalid Code");
				}
				/*
				 * if (strPosTypeArr[i].equals(POSItemBean.TYPE_SVC)) { if
				 * (ServiceNut.getObjectByCode(strItemCodeArr[i].trim()) ==
				 * null) { String key = "pos_item_id" + i; mapError.put(key,
				 * "Invalid service code"); } } else if
				 * (strPosTypeArr[i].equals(POSItemBean.TYPE_PKG)) { if
				 * (PackageNut.getObjectByCode(strItemCodeArr[i].trim()) ==
				 * null) { String key = "pos_item_id" + i; mapError.put(key,
				 * "Invalid package code"); } } else { if
				 * (ItemNut.getObjectByCode(strItemCodeArr[i].trim()) == null) {
				 * String key = "pos_item_id" + i; mapError.put(key, "Invalid
				 * item code"); } }
				 */
				// bdItemQty: client-side validation
				// bdItemStdPrice: client-side validation
				// bdItemQuotedPrice: client-side validation
			} // end if !(TYPE_NONE)
		} // end for
	}

	private void validateTradeInFields(HttpServletRequest req, Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateTradeInFields()");
		String[] tradeInNameArray = req.getParameterValues("tradeInName");
		String[] tradeInDescArray = req.getParameterValues("tradeInDesc");
		String[] tradeInQtyArray = req.getParameterValues("tradeInQty");
		String[] tradeInCostPriceArray = req.getParameterValues("tradeInCostPrice");
		// Put ALL arrays (whether good or bad) back to mapGood
		mapGood.put("tradeInName_arr", tradeInNameArray);
		mapGood.put("tradeInDesc_arr", tradeInDescArray);
		mapGood.put("tradeInQty_arr", tradeInQtyArray);
		mapGood.put("tradeInCostPrice_arr", tradeInCostPriceArray);
		// Validation ...
		/*
		 * for (int i=0; i<tradeInNameArray.length; i++) { // So far no
		 * validation required } // end for
		 */
		// Propagate the iTradeInRow
		String strTradeInRows = req.getParameter("iTradeInRows");
		req.setAttribute("iTradeInRows", new Integer(strTradeInRows));
	}

	private String fnPopulateValObjForJS(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			HashMap mapGood, HashMap mapError, JobsheetObject jobsheetObj, SalesTxnObject salesTxnObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{ // super huge try block. TO_DO: Make this more granular later (if
			// need be)
		// String[] theArray = req.getParameterValues("desc");
		// req.setAttribute("theArray",theArray);
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			String strCustSvcCtrId = req.getParameter("CustSvcCtrId");
			// Integer salesTxnId = new Integer((String)
			// req.getParameter("salesTxnId"));
			String strJobsheetDate = (String) req.getParameter("jobsheetDate");
			Log.printVerbose(strClassName + "Getting form variables 2");
			String strSupervisorUname = (String) req.getParameter("supervisorUname");
			String strTechnicianUname = (String) req.getParameter("technicianUname");
			String strUserName = (String) req.getParameter("userName");
			Log.printVerbose(strClassName + "Getting form variables 3");
			String strJobsheetRmks = (String) req.getParameter("jobsheetRemarks");
			String strCcy = (String) req.getParameter("currency");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			Log.printVerbose(strClassName + "Getting form variables 4");
			String strTimeStampComplete = (String) req.getParameter("completeDate");
			String strCustAccId = (String) req.getParameter("custAccountId");
			Log.printDebug("custAccId = " + strCustAccId);
			/*
			 * String strVehicleNo = (String)req.getParameter("vehicleNo");
			 * Log.printDebug("vehicleNo = " + strVehicleNo);
			 */
			String strVehicleId = (String) req.getParameter("vehicleId");
			String strVehicleMileage = (String) req.getParameter("vehicleMileage");
			if (strVehicleId == null)
				strVehicleId = "0";
			if (strVehicleMileage == null)
				strVehicleMileage = "n/a";
			Log.printDebug("vehicleId = " + strVehicleId);
			Log.printDebug("vehicleMileage = " + strVehicleMileage);
			Log.printVerbose(strClassName + ": tsCreate=" + strTimeStampCreate);
			String[] strPosTypeArray = req.getParameterValues("pos_type");
			String[] strPosServCodeArray = req.getParameterValues("pos_serv_code");
			String[] strPosRemarksArray = req.getParameterValues("pos_item_rmks");
			String[] strPosItemIdArray = req.getParameterValues("pos_item_id");
			for (int i = 0; i < strPosItemIdArray.length; i++)
				Log.printVerbose("posItemId[" + i + "] = " + strPosItemIdArray[i]);
			String[] strJobsheetItemQty = req.getParameterValues("posJobsheetItemQty");
			String[] strJobsheetItemRecommendedPrice = req.getParameterValues("posJobsheetItemStdPrice");
			String[] strJobsheetItemQuotedPrice = req.getParameterValues("posJobsheetItemQuotedPrice");
			/*******************************************************************
			 * BEGIN: VALIDATION
			 ******************************************************************/
			// HashMap mapError = new HashMap();
			// HashMap mapGood = new HashMap();
			validateHeaderFields(strCustSvcCtrId, strJobsheetDate, strSupervisorUname, strTechnicianUname, strUserName,
					strJobsheetRmks, strCcy, strTimeStampCreate, strTimeStampComplete, strVehicleMileage, mapError,
					mapGood);
			validateRowFields(strPosTypeArray, strPosServCodeArray, strPosRemarksArray, strPosItemIdArray,
					strJobsheetItemQty, strJobsheetItemRecommendedPrice, strJobsheetItemQuotedPrice, mapError, mapGood);
			validateTradeInFields(req, mapError, mapGood);
			if (mapError.size() > 0)
			{
				Log.printVerbose("*** mapError contains: " + Arrays.asList(mapError.keySet().toArray()).toString());
				// contains error, do not proceed, but propagate the hidden
				// fields!!
				req.setAttribute("custAccId", new Integer(strCustAccId));
				req.setAttribute("vehicleId", new Integer(strVehicleId));
				return "Failed Jobsheet Validation";
			}
			/*******************************************************************
			 * END: VALIDATION
			 ******************************************************************/
			// the line below is not implemented yet because
			// at this stage, we assume all transactions are cash based. //
			// todo: once the Customer Vehicle/Customer Identifier
			// is determined. We will populate this field accordingly
			Integer customerAccountId = null;
			// customerAccountId =
			// VehicleNut.getCustIdGivenRegNum(strVehicleNo); // Alex commented
			// out: 9 Nov
			// TO_DO: Double check the strCustAccId.compareTo("NONE") logic
			// below !!
			if (strCustAccId == null || strCustAccId.compareTo("NONE") == 0)
			{
				Log.printDebug("Customer Account Id is NULL! " + "defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			} else
			{
				customerAccountId = new Integer(strCustAccId);
			}
			if (customerAccountId == null)
			{
				// Log.printDebug("Failure in retrieving customer for vehicle no
				// = " + strVehicleNo
				Log.printDebug(" Null customerAccId ... defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			}
			if (strUserName == null || strSupervisorUname == null || strTechnicianUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer supId = null;
			Integer tecId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User supUser = UserNut.getHandle(lUserHome, strSupervisorUname);
				supId = (Integer) supUser.getUserId();
				User tecUser = UserNut.getHandle(lUserHome, strTechnicianUname);
				tecId = (Integer) tecUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// JobsheetObject jobsheetObj = new JobsheetObject();
			// jobsheetObj.mTimeCreated= TimeFormat.createTimeStamp(
			// strTimeStampCreate);
			jobsheetObj.mTimeCreated = TimeFormat.createTimeStamp(strJobsheetDate);
			jobsheetObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampComplete);
			jobsheetObj.mSupervisorId = new Integer(supId.intValue());
			jobsheetObj.mTechnicianId = new Integer(tecId.intValue());
			jobsheetObj.mCurrency = strCcy;
			jobsheetObj.mRemarks = strJobsheetRmks;
			jobsheetObj.mType = JobsheetBean.TYPE_JS;
			jobsheetObj.mState = JobsheetBean.STATE_CREATED;
			jobsheetObj.mStatus = JobsheetBean.STATUS_ACTIVE;
			jobsheetObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			jobsheetObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strPosTypeArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				JobsheetItemObject iio = new JobsheetItemObject();
				// This is the tricky part because we will have to
				// traverse the database to determine the POS Item Id
				// Based on POS Item Type, and the corresponding Item Code
				// (either pkg, svc or inv). strPosTypeArray[nCount] and
				// strPOSItemIdArray[nCount]
				// if(!strPosTypeArray[nCount].equals(JobsheetItemBean.TYPE_NONE))
				if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NONE) != 0)
				{
					// iio.mPosItemId = new Integer("0");
					// ok beware !, need to obtain pkid of posItem given the
					// code
					// For posType=TYPE_NINV and itemCode is not selected,
					// default itemCode to "NON-INV"
					if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NINV) == 0
							&& strPosItemIdArray[nCount].trim().compareTo("") == 0)
					{
						// strPosItemIdArray[nCount] = ItemBean.CODE_NON_INV;
					}
					Integer lPosItemId = POSItemNut.getPosItemIdByCode(strPosTypeArray[nCount],
							strPosItemIdArray[nCount].trim());
					Log.printVerbose(strClassName + funcName + " creating Jobsheet Items");
					iio.mPosItemId = lPosItemId;
					// iio.mRemarks = new String("none");
					iio.mRemarks = strPosRemarksArray[nCount];
					iio.mTotalQty = new BigDecimal(strJobsheetItemQty[nCount].trim());
					iio.mCurrency = new String(strCcy);
					iio.mUnitPriceRecommended = new BigDecimal(strJobsheetItemRecommendedPrice[nCount].trim());
					iio.mUnitPriceQuoted = new BigDecimal(strJobsheetItemQuotedPrice[nCount].trim());
					iio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
					if (!strPosServCodeArray[nCount].equals(""))
					{
						iio.mStrName1 = JobsheetItemBean.STRNAME1_DEPTCODE;
						iio.mStrValue1 = strPosServCodeArray[nCount];
					}
					jobsheetObj.vecJobsheetItems.add(iio);
					Log.printVerbose(strClassName + funcName + " successfully created one Jobsheet Items");
					// vince:todo- Alex, please help to add a loop/sum/variable
					// to add up all the service items
					// and then when the for/loop is done.. we need to execute
					// the following line
					// jobsheetObj.vecJobsheetItems.add(iioSvcTax);
					// where iioSvcTax is the object that you create to account
					// for all the service items in the bill.
					// of course.. inside iioSvcTax we need to have.....
					// iioSvcTax.mPosItemId = POSItemBean.PKID_GOV_SVC_TAX;
					// Please ask me when in doubt.
				}
			}
			// Alex: Although we have custAccId, we should store the
			// CustAccountObject
			// under SalesTxnObject so that it becomes easier to retrieve
			// all info later.
			// Given the customerAccountId, load the CustAccountObject,
			CustAccountObject lCustAccObj = CustAccountNut.getObject(customerAccountId, "");
			Log.printVerbose(lCustAccObj.toString());
			// SalesTxnObject salesTxnObj = new SalesTxnObject();
			salesTxnObj.custAccId = customerAccountId;
			salesTxnObj.custSvcCtrId = new Integer(strCustSvcCtrId);
			salesTxnObj.txnTime = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.remarks = strJobsheetRmks;
			// salesTxnObj.state = SalesTxnBean.ST_CREATED;
			salesTxnObj.state = SalesTxnBean.ST_JOBSHEET_OK;
			salesTxnObj.refForeignTable = VehicleBean.TABLENAME;
			salesTxnObj.refForeignKey = new Integer(strVehicleId);
			salesTxnObj.status = SalesTxnBean.STATUS_ACTIVE;
			salesTxnObj.lastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.userIdUpdate = new Integer(iUsrId.intValue());
			salesTxnObj.strName1 = "mileage";
			salesTxnObj.strValue1 = strVehicleMileage;
			// this.mJobsheetObj = jobsheetObj;
			salesTxnObj.vecJobsheets.add(jobsheetObj);
			// Alex added this ...
			salesTxnObj.custAccObj = lCustAccObj;
			// this.mSalesTxnObj = salesTxnObj;
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate Jobsheet Item Parameters. ";
			errMsg += "Please ensure that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	private String fnPopulateValObjForSO(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			HashMap mapGood, HashMap mapError, JobsheetObject jobsheetObj, SalesTxnObject salesTxnObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{ // super huge try block. TO_DO: Make this more granular later (if
			// need be)
		// String[] theArray = req.getParameterValues("desc");
		// req.setAttribute("theArray",theArray);
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			String strCustSvcCtrId = req.getParameter("CustSvcCtrId");
			// Integer salesTxnId = new Integer((String)
			// req.getParameter("salesTxnId"));
			String strJobsheetDate = (String) req.getParameter("jobsheetDate");
			Log.printVerbose(strClassName + "Getting form variables 2");
			String strSupervisorUname = (String) req.getParameter("supervisorUname");
			String strTechnicianUname = (String) req.getParameter("technicianUname");
			String strUserName = (String) req.getParameter("userName");
			Log.printVerbose(strClassName + "Getting form variables 3");
			String strJobsheetRmks = (String) req.getParameter("jobsheetRemarks");
			String strCcy = (String) req.getParameter("currency");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			Log.printVerbose(strClassName + "Getting form variables 4");
			String strTimeStampComplete = (String) req.getParameter("completeDate");
			String strCustAccId = (String) req.getParameter("custAccountId");
			Log.printDebug("custAccId = " + strCustAccId);
			Log.printVerbose(strClassName + ": tsCreate=" + strTimeStampCreate);
			String[] strPosTypeArray = req.getParameterValues("pos_type");
			String[] strPosServCodeArray = req.getParameterValues("pos_serv_code");
			String[] strPosRemarksArray = req.getParameterValues("pos_item_rmks");
			String[] strPosItemIdArray = req.getParameterValues("pos_item_id");
			for (int i = 0; i < strPosItemIdArray.length; i++)
				Log.printVerbose("posItemId[" + i + "] = " + strPosItemIdArray[i]);
			String[] strJobsheetItemQty = req.getParameterValues("posJobsheetItemQty");
			String[] strJobsheetItemRecommendedPrice = req.getParameterValues("posJobsheetItemStdPrice");
			String[] strJobsheetItemQuotedPrice = req.getParameterValues("posJobsheetItemQuotedPrice");
			/*******************************************************************
			 * BEGIN: VALIDATION
			 ******************************************************************/
			// HashMap mapError = new HashMap();
			// HashMap mapGood = new HashMap();
			validateHeaderFields(strCustSvcCtrId, strJobsheetDate, strSupervisorUname, strTechnicianUname, strUserName,
					strJobsheetRmks, strCcy, strTimeStampCreate, strTimeStampComplete, "", mapError, mapGood);
			validateRowFields(strPosTypeArray, strPosServCodeArray, strPosRemarksArray, strPosItemIdArray,
					strJobsheetItemQty, strJobsheetItemRecommendedPrice, strJobsheetItemQuotedPrice, mapError, mapGood);
			validateTradeInFields(req, mapError, mapGood);
			if (mapError.size() > 0)
			{
				Log.printVerbose("*** mapError contains: " + Arrays.asList(mapError.keySet().toArray()).toString());
				// contains error, do not proceed, but propagate the hidden
				// fields!!
				req.setAttribute("custAccId", new Integer(strCustAccId));
				// req.setAttribute("vehicleId", new Integer(strVehicleId));
				return "Failed Jobsheet Validation";
			}
			/*******************************************************************
			 * END: VALIDATION
			 ******************************************************************/
			// the line below is not implemented yet because
			// at this stage, we assume all transactions are cash based. //
			// todo: once the Customer Vehicle/Customer Identifier
			// is determined. We will populate this field accordingly
			Integer customerAccountId = null;
			// customerAccountId =
			// VehicleNut.getCustIdGivenRegNum(strVehicleNo); // Alex commented
			// out: 9 Nov
			// TO_DO: Double check the strCustAccId.compareTo("NONE") logic
			// below !!
			if (strCustAccId == null || strCustAccId.compareTo("NONE") == 0)
			{
				Log.printDebug("Customer Account Id is NULL! " + "defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			} else
			{
				customerAccountId = new Integer(strCustAccId);
			}
			if (customerAccountId == null)
			{
				// Log.printDebug("Failure in retrieving customer for vehicle no
				// = " + strVehicleNo
				Log.printDebug(" Null customerAccId ... defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			}
			if (strUserName == null || strSupervisorUname == null || strTechnicianUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer supId = null;
			Integer tecId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User supUser = UserNut.getHandle(lUserHome, strSupervisorUname);
				supId = (Integer) supUser.getUserId();
				User tecUser = UserNut.getHandle(lUserHome, strTechnicianUname);
				tecId = (Integer) tecUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// JobsheetObject jobsheetObj = new JobsheetObject();
			// jobsheetObj.mTimeCreated= TimeFormat.createTimeStamp(
			// strTimeStampCreate);
			jobsheetObj.mTimeCreated = TimeFormat.createTimeStamp(strJobsheetDate);
			jobsheetObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampComplete);
			jobsheetObj.mSupervisorId = new Integer(supId.intValue());
			jobsheetObj.mTechnicianId = new Integer(tecId.intValue());
			jobsheetObj.mCurrency = strCcy;
			jobsheetObj.mRemarks = strJobsheetRmks;
			jobsheetObj.mType = JobsheetBean.TYPE_SO;
			jobsheetObj.mState = JobsheetBean.STATE_CREATED;
			jobsheetObj.mStatus = JobsheetBean.STATUS_ACTIVE;
			jobsheetObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			jobsheetObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strPosTypeArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				JobsheetItemObject iio = new JobsheetItemObject();
				// This is the tricky part because we will have to
				// traverse the database to determine the POS Item Id
				// Based on POS Item Type, and the corresponding Item Code
				// (either pkg, svc or inv). strPosTypeArray[nCount] and
				// strPOSItemIdArray[nCount]
				// if(!strPosTypeArray[nCount].equals(JobsheetItemBean.TYPE_NONE))
				if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NONE) != 0)
				{
					// iio.mPosItemId = new Integer("0");
					// ok beware !, need to obtain pkid of posItem given the
					// code
					// For posType=TYPE_NINV and itemCode is not selected,
					// default itemCode to "NON-INV"
					if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NINV) == 0
							&& strPosItemIdArray[nCount].trim().compareTo("") == 0)
					{
						// strPosItemIdArray[nCount] = ItemBean.CODE_NON_INV;
					}
					Integer lPosItemId = POSItemNut.getPosItemIdByCode(strPosTypeArray[nCount],
							strPosItemIdArray[nCount].trim());
					Log.printVerbose(strClassName + funcName + " creating Jobsheet Items");
					iio.mPosItemId = lPosItemId;
					// iio.mRemarks = new String("none");
					iio.mRemarks = strPosRemarksArray[nCount];
					iio.mTotalQty = new BigDecimal(strJobsheetItemQty[nCount].trim());
					iio.mCurrency = new String(strCcy);
					iio.mUnitPriceRecommended = new BigDecimal(strJobsheetItemRecommendedPrice[nCount].trim());
					iio.mUnitPriceQuoted = new BigDecimal(strJobsheetItemQuotedPrice[nCount].trim());
					iio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
					if (!strPosServCodeArray[nCount].equals(""))
					{
						iio.mStrName1 = JobsheetItemBean.STRNAME1_DEPTCODE;
						iio.mStrValue1 = strPosServCodeArray[nCount];
					}
					jobsheetObj.vecJobsheetItems.add(iio);
					Log.printVerbose(strClassName + funcName + " successfully created one Jobsheet Items");
					// vince:todo- Alex, please help to add a loop/sum/variable
					// to add up all the service items
					// and then when the for/loop is done.. we need to execute
					// the following line
					// jobsheetObj.vecJobsheetItems.add(iioSvcTax);
					// where iioSvcTax is the object that you create to account
					// for all the service items in the bill.
					// of course.. inside iioSvcTax we need to have.....
					// iioSvcTax.mPosItemId = POSItemBean.PKID_GOV_SVC_TAX;
					// Please ask me when in doubt.
				}
			}
			// Alex: Although we have custAccId, we should store the
			// CustAccountObject
			// under SalesTxnObject so that it becomes easier to retrieve
			// all info later.
			// Given the customerAccountId, load the CustAccountObject,
			CustAccountObject lCustAccObj = CustAccountNut.getObject(customerAccountId, "");
			Log.printVerbose(lCustAccObj.toString());
			// SalesTxnObject salesTxnObj = new SalesTxnObject();
			salesTxnObj.custAccId = customerAccountId;
			salesTxnObj.custSvcCtrId = new Integer(strCustSvcCtrId);
			salesTxnObj.txnTime = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.remarks = strJobsheetRmks;
			// salesTxnObj.state = SalesTxnBean.ST_CREATED;
			salesTxnObj.state = SalesTxnBean.ST_JOBSHEET_OK;
			// salesTxnObj.refForeignTable = VehicleBean.TABLENAME;
			// salesTxnObj.refForeignKey = new Integer(strVehicleId);
			salesTxnObj.status = SalesTxnBean.STATUS_ACTIVE;
			salesTxnObj.lastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.userIdUpdate = new Integer(iUsrId.intValue());
			// salesTxnObj.strName1 = "mileage";
			// salesTxnObj.strValue1 = strVehicleMileage;
			// this.mJobsheetObj = jobsheetObj;
			salesTxnObj.vecJobsheets.add(jobsheetObj);
			// Alex added this ...
			salesTxnObj.custAccObj = lCustAccObj;
			// this.mSalesTxnObj = salesTxnObj;
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate Jobsheet Item Parameters. ";
			errMsg += "Please ensure that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	/*
	 * protected boolean fnCheckVehicleNo(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { String funcName =
	 * "fnCheckVehicleNo()"; Log.printVerbose("In " + funcName);
	 * 
	 * mVehicleNo = req.getParameter("vehicleNo");
	 * 
	 * Vehicle vehicleEJB = VehicleNut.getObjectByRegNum(mVehicleNo);
	 * if(vehicleEJB!=null) { try { // repopulate the real vehicle no as
	 * retrieved from the DB mVehicleNo = vehicleEJB.getRegNum(); mCustAccId =
	 * vehicleEJB.getCustAccountId(); } catch(Exception ex) {
	 * Log.printDebug("Failed to retrieve regNum from vehicle EJB!!"); } return
	 * true; }
	 * 
	 * Log.printVerbose("*** Vehicle No not found in the DB ***");
	 * 
	 * return false;
	 *  }
	 */
	// protected synchronized boolean fnCreateNewCustomer(HttpServlet servlet,
	protected boolean fnCreateNewCustomer(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCreateNewCustomer()";
		/**
		 * STEP 1: Create the main CustAccount EJB
		 */
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String custCode = (String) req.getParameter("custCode");
		// String custNewPkid = CustAccountNut.getNextPkid();
		String custName = (String) req.getParameter("custName");
		String custDesc = (String) req.getParameter("custDesc");
		HttpSession session = req.getSession();
		Integer userid = (Integer) session.getAttribute("userId");
		GUIDGenerator guid = null;
		try
		{
			guid = new GUIDGenerator();
		} catch (Exception ex)
		{
			return false;
		}
		CustAccountObject custObj = new CustAccountObject();
		custObj.name = custName;
		custObj.description = custDesc;
		custObj.custAccountCode = guid.getUUID();
		custObj.userIdUpdate = userid;
		custObj.telephone1 = req.getParameter("telephone1");
		custObj.mainAddress1 = req.getParameter("mainAddress1");
		custObj.mainAddress2 = req.getParameter("mainAddress2");
		custObj.mainAddress3 = req.getParameter("mainAddress3");
		custObj.mainPostcode = req.getParameter("mainPostcode");
		custObj.mainState = req.getParameter("mainState");
		custObj.mainCountry = req.getParameter("mainCountry");
		// Ensure CustAccount does not exist
		CustAccount newCustAccount = CustAccountNut.fnCreate(custObj);
		try
		{
			custObj = newCustAccount.getObject("");
		} catch (Exception ex)
		{
			if (newCustAccount != null)
			{
				try
				{
					newCustAccount.remove();
				} catch (Exception ex2)
				{
				}
				;
			}
			return false;
		}
		// For Vehicle
		VehicleObject newVehObj = new VehicleObject();
		newVehObj.regNum = req.getParameter("vehicleNo");
		newVehObj.model = req.getParameter("vehicleModel");
		newVehObj.userIdUpdate = userid;
		newVehObj.custAccountId = custObj.pkid;
		Vehicle vehEJB = VehicleNut.fnCreate(newVehObj);
		try
		{
			newVehObj = vehEJB.getObject();
		} catch (Exception ex)
		{
			if (vehEJB != null)
			{
				try
				{
					newCustAccount.remove();
					vehEJB.remove();
				} catch (Exception ex2)
				{
				}
			}
			return false;
		}
		req.setAttribute("vehicleId", newVehObj.pkid);
		req.setAttribute("custAccId", custObj.pkid);
		return true;
	} // end fnCreateNewCustomer
}
/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoNewJobsheet implements Action
{
	String strClassName = "DoNewJobsheet";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		try
		{
			// Form Handlers
			String formName = req.getParameter("formName");
			// Regardless of the usage of the form..
			// we will populate the following
			// fnGetCustSalesCenterList(servlet, req, res);
			// If the form name is null,
			// the users are creating a fresh jobsheet
			if (formName == null)
			{
				Log.printVerbose(strClassName + "formName: null");
				// return new ActionRouter("pos-create-new-jobsheet-01-page");
				return new ActionRouter("pos-check-customer-by-vehicle-page");
			}
			if (formName.equals("retrieveVehicleNo"))
			{
				// define the default number of rows for the jobsheet
				// or the invoices
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// check vehicle number
				String mVehicleNo = req.getParameter("vehicleNo");
				Integer mCustAccId = null;
				Vehicle vehicleEJB = VehicleNut.getObjectByRegNum(mVehicleNo);
				if (vehicleEJB != null)
				{
					try
					{
						// repopulate the real vehicle no as retrieved from the
						// DB
						Integer mVehicleId = vehicleEJB.getPkid();
						mVehicleNo = vehicleEJB.getRegNum();
						mCustAccId = vehicleEJB.getCustAccountId();
						fnGetUserList(servlet, req, res);
						fnGetCustSalesCenterList(servlet, req, res);
						// req.setAttribute("vehicleNo", mVehicleNo);
						req.setAttribute("vehicleId", mVehicleId);
						req.setAttribute("custAccId", mCustAccId);
						return new ActionRouter("pos-create-new-jobsheet-01-page");
					} catch (Exception ex)
					{
						String strErrMsg = "Failed to retrieve regNum from vehicle EJB!!";
						Log.printDebug(strErrMsg);
						req.setAttribute("strErrMsg", strErrMsg);
						return new ActionRouter("pos-check-customer-by-vehicle-page");
					}
				} else
				{
					Log.printVerbose("*** Vehicle No not found in the DB ***");
					// New Customer
					Log.printVerbose(strClassName + "Vehicle No does not match any customer. Creating new ...");
					req.setAttribute("vehicleNo", mVehicleNo);
					// req.setAttribute("strErrMsg", mStrErrMsg);
					return new ActionRouter("pos-create-new-customer-page");
				}
			}
			if (formName.equals("createNewCustomer"))
			{
				// Add a new customer
				if (!fnCreateNewCustomer(servlet, req, res))
				{
					String strErrMsg = "Error in creating new customer. " + "Please try again";
					Log.printDebug(strClassName + ": " + strErrMsg);
					// req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-new-customer-page");
				}
				// Successfully created New Customer
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// Set the rest of the attributes
				/*
				 * SET THIS IN fnCreateNewCustomer req.setAttribute("strErrMsg",
				 * this.mStrErrMsg); req.setAttribute("vehicleNo",
				 * this.mVehicleNo); req.setAttribute("custAccId",
				 * this.mCustAccId);
				 */
				return new ActionRouter("pos-create-new-jobsheet-01-page");
			}
			if (formName.equals("retrieveItnlCust"))
			{
				// Obtain the custAccountId
				String strCustAccId = req.getParameter("iItnlCustAccId");
				if (strCustAccId == null)
				{
					String strErrMsg = "Could not obtain internal CustAccId";
					Log.printDebug(strErrMsg);
					req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-new-itnl-sales-page");
				}
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// req.setAttribute("vehicleNo", this.mVehicleNo);
				req.setAttribute("custAccId", new Integer(strCustAccId));
				return new ActionRouter("pos-create-new-jobsheet-01-page");
			}
			if (formName.equals("retrieveCustAccId"))
			{
				// Obtain the custAccountId
				String strCustAccId = req.getParameter("custAccId");
				if (strCustAccId == null)
				{
					String strErrMsg = "Could not obtain CustAccId";
					Log.printDebug(strErrMsg);
					req.setAttribute("strErrMsg", strErrMsg);
					return new ActionRouter("pos-create-salesorder-01-page");
				}
				// Carry on with the jobsheet
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// propagate iRow
				String strRows = req.getParameter("iRows");
				Integer iRows = null;
				if (strRows != null)
				{
					iRows = new Integer(strRows);
				} else
				{
					iRows = new Integer("15");
				}
				req.setAttribute("iRows", iRows);
				// req.setAttribute("vehicleNo", this.mVehicleNo);
				req.setAttribute("custAccId", new Integer(strCustAccId));
				return new ActionRouter("pos-create-salesorder-02-page");
			}
			// If the form has been filled and user submit to create
			// a new jobsheet, do the necessary checking
			if (formName.equals("createNewJobsheet"))
			{
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				// boolean bTradeInOK = false;
				boolean bJobsheetVerified = false;
				Log.printVerbose(strClassName + ": before fnCreateNewJobsheet");
				bJobsheetVerified = fnCreateNewJobsheet(servlet, req, res, false);
				// bTradeInOK = fnCreateNewTradeIn(servlet, req, res);
				// Log.printVerbose(this.mStrErrMsg);
				// tentatively.. force it to true first..
				// this is to test the display
				// bJobsheetVerified = true;
				if (bJobsheetVerified == true)
				{
					return new ActionRouter("pos-create-new-jobsheet-02-page");
				} else
				{
					// Some errors must have occurred in fnCreateNewJobsheet
					// Rollback transactions
					// rollBackTxn();
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					// propagate iRow
					String strRows = req.getParameter("iRows");
					Integer iRows = null;
					if (strRows != null)
					{
						iRows = new Integer(strRows);
					} else
					{
						iRows = new Integer("15");
					}
					req.setAttribute("iRows", iRows);
					return new ActionRouter("pos-create-new-jobsheet-01-page");
				}
			}
			if (formName.equals("createSalesOrder"))
			{
				fnGetUserList(servlet, req, res);
				fnGetCustSalesCenterList(servlet, req, res);
				boolean bJobsheetVerified = false;
				Log.printVerbose(strClassName + ": before fnCreateNewJobsheet");
				bJobsheetVerified = fnCreateNewJobsheet(servlet, req, res, true);
				// Log.printVerbose(this.mStrErrMsg);
				// tentatively.. force it to true first..
				// this is to test the display
				// bJobsheetVerified = true;
				if (bJobsheetVerified == true)
				{
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					// req.setAttribute("salesTxnObj", this.mSalesTxnObj);
					// req.setAttribute("jobsheetObj", this.mJobsheetObj);
					return new ActionRouter("pos-create-salesorder-03-page");
				} else
				{
					// Some errors must have occurred in fnCreateNewJobsheet
					// Rollback transactions
					// rollBackTxn();
					// req.setAttribute("strErrMsg", this.mStrErrMsg);
					return new ActionRouter("pos-create-salesorder-02-page");
				}
			}
			// fnGetCustSalesCenterList(servlet, req, res);
			Log.printVerbose(strClassName + ": returning default ActionRouter");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return new ActionRouter("pos-create-new-jobsheet-01-page");
	}

	// /////////////////////////////////////////////////////////////////
	protected void fnGetUserList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector vecUserObj = UserNut.getValueObjectsGiven(UserBean.STATUS, UserBean.ACTIVE, (String) null,
				(String) null, (String) null, (String) null);
		req.setAttribute("vecUserObj", vecUserObj);
	}

	// /////////////////////////////////////////////////////////////////
	protected boolean fnCreateNewTradeIn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
	// HashMap mapGood, HashMap mapError,
			JobsheetObject jobsheetObj)
	{
		// Get all the relevent parameters
		String custSvcCtrId = req.getParameter("CustSvcCtrId");
		String[] tradeInNameArray = req.getParameterValues("tradeInName");
		String[] tradeInDescArray = req.getParameterValues("tradeInDesc");
		String[] tradeInQtyArray = req.getParameterValues("tradeInQty");
		// String[] tradeInSalesPriceArray =
		// req.getParameterValues("tradeInSalesPrice");
		String[] tradeInCostPriceArray = req.getParameterValues("tradeInCostPrice");
		String tradeInCustId = req.getParameter("custAccountId");
		// Item newItem = null;
		POSItem newPOSItemBuy = null;
		// POSItem newPOSItemSell = null;
		Vector vecNewItems = new Vector();
		Vector vecNewPOSItemsBuy = new Vector();
		// Vector vecNewPOSItemsSell = new Vector();
		try
		{ // Super huge try-catch block
			// Get current date/time and username
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				return false;
			}
			/*******************************************************************
			 * Verify Customer PKID
			 ******************************************************************/
			if (CustAccountNut.getHandle(new Integer(tradeInCustId)) == null)
			{
				throw new Exception("Customer (PKID: " + tradeInCustId + ") does not exist!");
			}
			// map ServiceCenter to Location
			Integer thisLocPkid = CustServiceCenterNut.mapSvcCtrToInvLoc(new Integer(custSvcCtrId));
			int nRows = tradeInNameArray.length;
			Log.printVerbose("Number of tradeIn rows = " + nRows);
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				String thisTradeInName = tradeInNameArray[nCount];
				String thisTradeInDesc = tradeInDescArray[nCount];
				String thisTradeInQty = tradeInQtyArray[nCount];
				String thisTradeInCostPrice = tradeInCostPriceArray[nCount];
				// String thisTradeInSalesPrice =
				// tradeInSalesPriceArray[nCount];
				// Check that the TradeIn Name is not empty
				if (thisTradeInName.equals(""))
					continue;
				BigDecimal costPrice = new BigDecimal(thisTradeInCostPrice);
				BigDecimal buyQty = null;
				try
				{
					buyQty = new BigDecimal(thisTradeInQty);
				} catch (Exception ex)
				{
					buyQty = new BigDecimal(1);
				}
				// Here we assume everytime this function is called it is a new
				// trade-in item
				/***************************************************************
				 * CREATE THE TRADE-IN ITEM
				 **************************************************************/
				ItemObject itemObj = new ItemObject();
				// populate the properties here!!
				itemObj.name = thisTradeInName;
				itemObj.description = thisTradeInDesc;
				itemObj.userIdUpdate = usrid;
				itemObj.enumInvType = new Integer(ItemBean.INV_TYPE_TRADEIN);
				itemObj.priceList = costPrice;
				itemObj.priceSale = costPrice;
				itemObj.priceDisc1 = costPrice;
				itemObj.priceDisc2 = costPrice;
				itemObj.priceDisc3 = costPrice;
				itemObj.priceMin = costPrice;
				itemObj.fifoUnitCost = costPrice;
				itemObj.maUnitCost = costPrice;
				itemObj.waUnitCost = costPrice;
				itemObj.lastUnitCost = costPrice;
				itemObj.replacementUnitCost = costPrice;
				// itemObj.preferredSupplier = suppAccObj.pkid;
				Item itemEJB = ItemNut.fnCreateWithCodePrefix(itemObj, ItemBean.CODE_PREFIX_TI);
				if (itemEJB == null)
				{
					throw new Exception("Null Item");
				} else
				{
					Log.printVerbose("Successfully created Item");
				}
				vecNewItems.add(itemEJB);
				/***************************************************************
				 * ADD POS ITEM
				 **************************************************************/
				POSItemHome lPOSItemHome = POSItemNut.getHome();
				// Create one entry for purchase and one more for sales
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = itemEJB.getPkid();
				posObj.itemType = POSItemBean.TYPE_TRADEIN_BUY;
				posObj.currency = "MYR";
				posObj.unitPriceStd = new BigDecimal(thisTradeInCostPrice);
				posObj.unitPriceDiscounted = new BigDecimal(thisTradeInCostPrice);
				posObj.unitPriceMin = new BigDecimal(thisTradeInCostPrice);
				posObj.userIdUpdate = usrid;
				newPOSItemBuy = POSItemNut.fnCreate(posObj);
				if (newPOSItemBuy == null)
					throw new Exception("Null POSItem (Trade-In)!!");
				else
					Log.printVerbose("Successfully created POSItem");
				vecNewPOSItemsBuy.add(newPOSItemBuy);
				/***************************************************************
				 * Populate JobsheetItemObject
				 **************************************************************/
				JobsheetItemObject jsio = new JobsheetItemObject();
				jsio.mPosItemId = newPOSItemBuy.getPkid();
				jsio.mRemarks = "Traded in by Customer PKID " + tradeInCustId;
				jsio.mTotalQty = new BigDecimal(thisTradeInQty);
				jsio.mCurrency = "MYR";
				jsio.mUnitPriceQuoted = new BigDecimal(thisTradeInCostPrice).negate(); // negate
																						// because
																						// stockIn
				jsio.mUnitPriceRecommended = jsio.mUnitPriceQuoted; // temp. put
																	// the same
																	// as quoted
				jsio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
				jobsheetObj.vecJobsheetItems.add(jsio);
				/***************************************************************
				 * ADD TRADE-IN STOCK
				 **************************************************************/
				// At this point, populate the newly created trade-in item
				// code+name
				// req.setAttribute("strTradeInCode", tradeInCode);
				// req.setAttribute("strTradeInName", tradeInName);
			} // end for
		} // end huge-try
		catch (Exception ex)
		{
			// end of super huge try-catch block
			// Rollback all transactions
			// in the following order:
			// 1. POSItem
			// 2. Item
			try
			{
				// Loop through all populated items and start deleting each one
				for (int itemIdx = 0; itemIdx < vecNewItems.size(); itemIdx++)
				{
					Item rollbackItem = (Item) vecNewItems.get(itemIdx);
					if (rollbackItem != null)
						rollbackItem.remove();
				}
				for (int posItemIdx = 0; posItemIdx < vecNewPOSItemsBuy.size(); posItemIdx++)
				{
					POSItem rollbackPOSItem = (POSItem) vecNewPOSItemsBuy.get(posItemIdx);
					if (rollbackPOSItem != null)
						rollbackPOSItem.remove();
				}
			} catch (Exception e)
			{
				Log.printDebug("Failure to rollback transactions !!! ");
			}
			String strErrMsg;
			strErrMsg = "Error while processing a new trade-in item: " + ex.getMessage();
			Log.printDebug(strErrMsg);
			req.setAttribute("strErrMsg", strErrMsg);
			return false;
		}
		return true;
	} // end fnCreateNewTradeIn

	protected boolean fnCreateNewJobsheet(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			boolean isSalesOrder)
	{
		String funcName = "fnCreateNewJobsheet()";
		// Alex (27 Oct) - Changed member variables to local variables.
		String mStrErrMsg = new String("");
		// String strObjectName = new String("DoNewJobsheet");
		JobsheetObject mJobsheetObj = new JobsheetObject();
		SalesTxnObject mSalesTxnObj = new SalesTxnObject();
		SalesTxn mSalesTxn = null;
		Jobsheet mJobsheet = null;
		// String mVehicleNo = null;
		// Integer mCustAccId = null;
		Vector mVecJobsheetItems = new Vector();
		try
		{ // super huge try-catch
			// Populate the value objects including
			// SalesTxn, Jobsheet and jobsheet items
			Log.printVerbose(strClassName + "::fnCreateNewJobsheet" + " before entering fnPopulateValueObject");
			// Alex (04/13) - Added support for thorough field validation
			HashMap mapGood = new HashMap();
			HashMap mapError = new HashMap();
			String errMsg;
			if (isSalesOrder)
			{
				errMsg = fnPopulateValObjForSO(servlet, req, res, mapGood, mapError, mJobsheetObj, mSalesTxnObj);
			} else
			{
				errMsg = fnPopulateValObjForJS(servlet, req, res, mapGood, mapError, mJobsheetObj, mSalesTxnObj);
			}
			Log.printVerbose(strClassName + "::fnCreateNewJobsheet" + " after exiting fnPopulateValueObject");
			if (errMsg != null)
			{
				req.setAttribute("mapGood", mapGood);
				req.setAttribute("mapError", mapError);
				throw new Exception(errMsg);
			}
			// server side form validation
			// 1) create the SalesTxn Object
			// SalesTxn salesTxn = null;
			if (mSalesTxnObj != null)
			{
				mSalesTxn = SalesTxnNut.fnCreate(mSalesTxnObj);
			}
			if (mSalesTxn == null)
			{
				String strErrMsg = "SalesTxnNut.fnCreate failed to create object";
				throw new Exception(strErrMsg);
			} else
			{
				try
				{
					mSalesTxnObj.pkid = (Long) mSalesTxn.getPkid();
					for (int vecIndex = 0; vecIndex < mSalesTxnObj.vecJobsheets.size(); vecIndex++)
					{
						JobsheetObject tmpInvObj = (JobsheetObject) mSalesTxnObj.vecJobsheets.get(vecIndex);
						tmpInvObj.mSalesTxnId = mSalesTxnObj.pkid;
						mSalesTxnObj.vecJobsheets.set(vecIndex, tmpInvObj);
					}
				} catch (Exception ex)
				{
					String strErrMsg = "Failed to create SalesTxnBean: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// We are not creating a loop here, because
			// there's only one jobsheet attached to this sales transaction
			// because it is a new jobsheet.
			// for(int countA=0; countA< this.mSalesTxnObj.vecJobsheets.size();
			// countA++) { }
			// 2.0) create the Jobsheet Object
			// JobsheetObject mJobsheetObj = (JobsheetObject)
			// this.mSalesTxnObj.vecJobsheets.get(0);
			// Jobsheet jobsheetRemote = null;
			/*
			 * Already done in fnPopulateValueObjects() // Alex: 27 Dec 03 - to
			 * cater for SalesOrder, need to add the state field
			 * if(isSalesOrder) mJobsheetObj.mType = JobsheetBean.TYPE_SO;
			 */
			if (mJobsheetObj != null)
			{
				mJobsheet = JobsheetNut.fnCreate(mJobsheetObj);
			}
			if (mJobsheet == null)
			{
				String strErrMsg = "Failed to create new Jobsheet Bean";
				throw new Exception(strErrMsg);
			} else
			{
				try
				{
					mJobsheetObj.mPkid = (Long) mJobsheet.getPkid();
					mJobsheetObj.mStmtNumber = (Long) mJobsheet.getStmtNumber();
				} catch (Exception ex)
				{
					String strErrMsg = "Invalid Jobsheet Primary Key or StmtNumber: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// Check for any trade-in items
			if (!fnCreateNewTradeIn(servlet, req, res, mJobsheetObj))
			{
				throw new Exception("Error while creating new trade-in items");
			}
			// 2.1) create the Jobsheet Items Object selectively
			for (int countA = 0; countA < mJobsheetObj.vecJobsheetItems.size(); countA++)
			{
				JobsheetItemObject jsItemObj = (JobsheetItemObject) mJobsheetObj.vecJobsheetItems.get(countA);
				// Alex added this ..
				// Need to assign the newly created jobsheet
				// pkid into the jsItemObj
				jsItemObj.mJobsheetId = mJobsheetObj.mPkid;
				JobsheetItem invItem = JobsheetItemNut.fnCreate(jsItemObj);
				// if successful, fill the pkid of the valueObject
				if (invItem != null)
				{
					// First thing is to append to the JobsheetItem Vector
					mVecJobsheetItems.add(invItem);
					try
					{
						jsItemObj.mPkid = invItem.getPkid();
					} catch (Exception ex)
					{
						String strErrMsg = "Invalid Inventory Item Primary Key: " + ex.getMessage();
						throw new Exception(strErrMsg);
					}
				} else
				{
					String strErrMsg = "Failure in creating jobsheet item object.";
					throw new Exception(strErrMsg);
				}
			} // end for
			// Convert SalesTxn State to JOBSHEET_OK
			SalesTxn salesTxnEjb = SalesTxnNut.getHandle(mSalesTxnObj.pkid);
			salesTxnEjb.setState(SalesTxnBean.ST_JOBSHEET_OK);
			// Populate attributes
			// req.setAttribute("strErrMsg", "");
			req.setAttribute("salesTxnObj", mSalesTxnObj);
			req.setAttribute("jobsheetObj", mJobsheetObj);
			// If not, return to the original form for correction
			/*
			 * if(errMsg != null) { Log.printDebug(strClassName+" Cannot create
			 * salesTransaction "); mStrErrMsg += errMsg; return false; }
			 */
			// To create a new jobsheet
			// 0. Create a ReceiptBean
			// for now, we assume all transactions are on cash basis.
			// so, a receipt is automatically generated.
			// Also, cash sales are automatically treated as
			// customer with pkid = 1
			// Clean up the database if new jobsheet is not
			// created due to some reasons.
			return true;
		} // end of super huge try
		catch (Exception ex)
		{
			// Handle exceptions in a generic way
			String userErrMsg = "Failed to create new Jobsheet. Reason: " + ex.getMessage();
			// Rollback
			try
			{
				if (!mVecJobsheetItems.isEmpty())
				{
					Log.printDebug("Rolling back the creation of JobsheetItems");
					for (int i = 0; i < mVecJobsheetItems.size(); i++)
					{
						JobsheetItem lInvItem = (JobsheetItem) mVecJobsheetItems.get(i);
						lInvItem.remove();
					}
				}
				if (mJobsheet != null)
				{
					Log.printDebug("Rolling back the creation of Jobsheet");
					mJobsheet.remove();
				}
				if (mSalesTxn != null)
				{
					Log.printDebug("Rolling back the creation of SalesTxn");
					mSalesTxn.remove();
				}
			} catch (Exception rollbackEx)
			{
				userErrMsg += ". WARNING: Failed to RollBack Jobsheet Creation.";
				userErrMsg += " Some objects need to be manually deleted from the Database";
				// Log.printDebug(mStrErrMsg);
				// result=false;
			}
			Log.printDebug(strClassName + "::" + funcName + " - " + userErrMsg);
			req.setAttribute("strErrMsg", userErrMsg);
			return false;
		}
		// return true;
	}

	protected void fnGetCustSalesCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		/*
		 * Collection colActiveSvcC = CustServiceCenterNut.getCollectionByField(
		 * CustServiceCenterBean.STATUS, CustServiceCenterBean.STATUS_ACTIVE);
		 * Iterator itrActiveSvcC = colActiveSvcC.iterator();
		 */
		// Alex: Optimizing retrieval of CustSvcCtr list
		Collection colActiveSvcCtrObj = CustServiceCenterNut.getActiveValObj();
		Iterator itrActiveSvcC = colActiveSvcCtrObj.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	private void validateHeaderFields(String iCustSvcCtrId, String tsJobsheetDate, String strSupervisorUname,
			String strTechnicianUname, String strUserName, String strJobsheetRemarks, String strCurrency,
			String tsCreate, String tsCompleteDate, String iVehicleMileage, Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateHeaderFields()");
		// Put all fields back into HashMap
		mapGood.put("CustSvcCtrId", iCustSvcCtrId);
		mapGood.put("jobsheetDate", tsJobsheetDate);
		mapGood.put("supervisorUname", strSupervisorUname);
		mapGood.put("technicianUname", strTechnicianUname);
		mapGood.put("userName", strUserName);
		mapGood.put("jobsheetRemarks", strJobsheetRemarks);
		mapGood.put("currency", strCurrency);
		mapGood.put("tsCreate", tsCreate);
		mapGood.put("completeDate", tsCompleteDate);
		mapGood.put("vehicleMileage", iVehicleMileage);
		// iCustSvcCtrId: requires no validation(chosen by select options)
		// tsjobsheetDate: client-side validation
		/*
		 * try { new Timestamp(tsJobsheetDate); } catch (Exception ex) {
		 * mapError.put("jobsheetDate", "Invalid Date Format"); }
		 */
		// strSupervisorUname, strTechnicianUname, strUserName:
		// [1] validate valid username
		// Integer iUsrId = null;
		/*
		 * Integer supId = null; Integer tecId = null; UserHome lUserHome =
		 * UserNut.getHome(); try { User lUser = UserNut.getHandle(lUserHome,
		 * strUserName); iUsrId = (Integer) lUser.getUserId(); } catch
		 * (Exception ex) { mapError.put("strUserName", "Invalid User"); } try {
		 * User supUser = UserNut.getHandle( lUserHome,strSupervisorUname);
		 * supId = (Integer) supUser.getUserId(); } catch (Exception ex) {
		 * mapError.put("strSupervisorUname", "Invalid User"); }
		 * 
		 * try { User tecUser = UserNut.getHandle(
		 * lUserHome,strTechnicianUname); tecId = (Integer) tecUser.getUserId(); }
		 * catch(Exception ex) { mapError.put("strTechnicianUname", "Invalid
		 * User"); }
		 */
		// jobsheetRemarks: requires no validation
		// currency: requires no validation
		// tsCreate: requires no validation
		// completeDate: client-side validation
		/*
		 * try { new Timestamp(tsCompleteDate); } catch (Exception ex) {
		 * mapError.put("completeDate", "Invalid Date Format"); }
		 */
		// vehicleMileage: client-side validation
	}

	private void validateRowFields(String[] strPosTypeArr, String[] strDeptCodeArr, String[] strItemRmksArr,
			String[] strItemCodeArr, String[] bdItemQtyArr, String[] bdItemStdPriceArr, String[] bdItemQuotedPriceArr,
			Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateRowFields()");
		// Put ALL arrays (whether good or bad) back to mapGood
		mapGood.put("pos_type_arr", strPosTypeArr);
		mapGood.put("pos_item_rmks_arr", strItemRmksArr);
		mapGood.put("pos_item_id_arr", strItemCodeArr);
		mapGood.put("pos_serv_code_arr", strDeptCodeArr);
		mapGood.put("posJobsheetItemQty_arr", bdItemQtyArr);
		mapGood.put("posJobsheetItemStdPrice_arr", bdItemStdPriceArr);
		mapGood.put("posJobsheetItemQuotedPrice_arr", bdItemQuotedPriceArr);
		// Validation ...
		for (int i = 0; i < strPosTypeArr.length; i++)
		{
			if (!strPosTypeArr[i].equals(POSItemBean.TYPE_NONE))
			{
				// Do not apply validation for Non-Inventory
				// which has empty item code (defaults to NON-INV)
				if (strPosTypeArr[i].equals(POSItemBean.TYPE_NINV) && strItemCodeArr[i].trim().equals(""))
				{
					continue;
				}
				// strPosType: no validation needed
				// strDeptCode: no validation needed
				// strItemRmks: no validation needed
				// strItemCode: validate code
				if (POSItemNut.getPosItemIdByCode(strPosTypeArr[i], strItemCodeArr[i].trim()) == null)
				{
					String key = "pos_item_id" + i;
					mapError.put(key, "Invalid Code");
				}
				/*
				 * if (strPosTypeArr[i].equals(POSItemBean.TYPE_SVC)) { if
				 * (ServiceNut.getObjectByCode(strItemCodeArr[i].trim()) ==
				 * null) { String key = "pos_item_id" + i; mapError.put(key,
				 * "Invalid service code"); } } else if
				 * (strPosTypeArr[i].equals(POSItemBean.TYPE_PKG)) { if
				 * (PackageNut.getObjectByCode(strItemCodeArr[i].trim()) ==
				 * null) { String key = "pos_item_id" + i; mapError.put(key,
				 * "Invalid package code"); } } else { if
				 * (ItemNut.getObjectByCode(strItemCodeArr[i].trim()) == null) {
				 * String key = "pos_item_id" + i; mapError.put(key, "Invalid
				 * item code"); } }
				 */
				// bdItemQty: client-side validation
				// bdItemStdPrice: client-side validation
				// bdItemQuotedPrice: client-side validation
			} // end if !(TYPE_NONE)
		} // end for
	}

	private void validateTradeInFields(HttpServletRequest req, Map mapError, Map mapGood)
	{
		Log.printVerbose("In validateTradeInFields()");
		String[] tradeInNameArray = req.getParameterValues("tradeInName");
		String[] tradeInDescArray = req.getParameterValues("tradeInDesc");
		String[] tradeInQtyArray = req.getParameterValues("tradeInQty");
		String[] tradeInCostPriceArray = req.getParameterValues("tradeInCostPrice");
		// Put ALL arrays (whether good or bad) back to mapGood
		mapGood.put("tradeInName_arr", tradeInNameArray);
		mapGood.put("tradeInDesc_arr", tradeInDescArray);
		mapGood.put("tradeInQty_arr", tradeInQtyArray);
		mapGood.put("tradeInCostPrice_arr", tradeInCostPriceArray);
		// Validation ...
		/*
		 * for (int i=0; i<tradeInNameArray.length; i++) { // So far no
		 * validation required } // end for
		 */
		// Propagate the iTradeInRow
		String strTradeInRows = req.getParameter("iTradeInRows");
		req.setAttribute("iTradeInRows", new Integer(strTradeInRows));
	}

	private String fnPopulateValObjForJS(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			HashMap mapGood, HashMap mapError, JobsheetObject jobsheetObj, SalesTxnObject salesTxnObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{ // super huge try block. TO_DO: Make this more granular later (if
			// need be)
		// String[] theArray = req.getParameterValues("desc");
		// req.setAttribute("theArray",theArray);
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			String strCustSvcCtrId = req.getParameter("CustSvcCtrId");
			// Integer salesTxnId = new Integer((String)
			// req.getParameter("salesTxnId"));
			String strJobsheetDate = (String) req.getParameter("jobsheetDate");
			Log.printVerbose(strClassName + "Getting form variables 2");
			String strSupervisorUname = (String) req.getParameter("supervisorUname");
			String strTechnicianUname = (String) req.getParameter("technicianUname");
			String strUserName = (String) req.getParameter("userName");
			Log.printVerbose(strClassName + "Getting form variables 3");
			String strJobsheetRmks = (String) req.getParameter("jobsheetRemarks");
			String strCcy = (String) req.getParameter("currency");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			Log.printVerbose(strClassName + "Getting form variables 4");
			String strTimeStampComplete = (String) req.getParameter("completeDate");
			String strCustAccId = (String) req.getParameter("custAccountId");
			Log.printDebug("custAccId = " + strCustAccId);
			/*
			 * String strVehicleNo = (String)req.getParameter("vehicleNo");
			 * Log.printDebug("vehicleNo = " + strVehicleNo);
			 */
			String strVehicleId = (String) req.getParameter("vehicleId");
			String strVehicleMileage = (String) req.getParameter("vehicleMileage");
			if (strVehicleId == null)
				strVehicleId = "0";
			if (strVehicleMileage == null)
				strVehicleMileage = "n/a";
			Log.printDebug("vehicleId = " + strVehicleId);
			Log.printDebug("vehicleMileage = " + strVehicleMileage);
			Log.printVerbose(strClassName + ": tsCreate=" + strTimeStampCreate);
			String[] strPosTypeArray = req.getParameterValues("pos_type");
			String[] strPosServCodeArray = req.getParameterValues("pos_serv_code");
			String[] strPosRemarksArray = req.getParameterValues("pos_item_rmks");
			String[] strPosItemIdArray = req.getParameterValues("pos_item_id");
			for (int i = 0; i < strPosItemIdArray.length; i++)
				Log.printVerbose("posItemId[" + i + "] = " + strPosItemIdArray[i]);
			String[] strJobsheetItemQty = req.getParameterValues("posJobsheetItemQty");
			String[] strJobsheetItemRecommendedPrice = req.getParameterValues("posJobsheetItemStdPrice");
			String[] strJobsheetItemQuotedPrice = req.getParameterValues("posJobsheetItemQuotedPrice");
			/*******************************************************************
			 * BEGIN: VALIDATION
			 ******************************************************************/
			// HashMap mapError = new HashMap();
			// HashMap mapGood = new HashMap();
			validateHeaderFields(strCustSvcCtrId, strJobsheetDate, strSupervisorUname, strTechnicianUname, strUserName,
					strJobsheetRmks, strCcy, strTimeStampCreate, strTimeStampComplete, strVehicleMileage, mapError,
					mapGood);
			validateRowFields(strPosTypeArray, strPosServCodeArray, strPosRemarksArray, strPosItemIdArray,
					strJobsheetItemQty, strJobsheetItemRecommendedPrice, strJobsheetItemQuotedPrice, mapError, mapGood);
			validateTradeInFields(req, mapError, mapGood);
			if (mapError.size() > 0)
			{
				Log.printVerbose("*** mapError contains: " + Arrays.asList(mapError.keySet().toArray()).toString());
				// contains error, do not proceed, but propagate the hidden
				// fields!!
				req.setAttribute("custAccId", new Integer(strCustAccId));
				req.setAttribute("vehicleId", new Integer(strVehicleId));
				return "Failed Jobsheet Validation";
			}
			/*******************************************************************
			 * END: VALIDATION
			 ******************************************************************/
			// the line below is not implemented yet because
			// at this stage, we assume all transactions are cash based. //
			// todo: once the Customer Vehicle/Customer Identifier
			// is determined. We will populate this field accordingly
			Integer customerAccountId = null;
			// customerAccountId =
			// VehicleNut.getCustIdGivenRegNum(strVehicleNo); // Alex commented
			// out: 9 Nov
			// TO_DO: Double check the strCustAccId.compareTo("NONE") logic
			// below !!
			if (strCustAccId == null || strCustAccId.compareTo("NONE") == 0)
			{
				Log.printDebug("Customer Account Id is NULL! " + "defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			} else
			{
				customerAccountId = new Integer(strCustAccId);
			}
			if (customerAccountId == null)
			{
				// Log.printDebug("Failure in retrieving customer for vehicle no
				// = " + strVehicleNo
				Log.printDebug(" Null customerAccId ... defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			}
			if (strUserName == null || strSupervisorUname == null || strTechnicianUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer supId = null;
			Integer tecId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User supUser = UserNut.getHandle(lUserHome, strSupervisorUname);
				supId = (Integer) supUser.getUserId();
				User tecUser = UserNut.getHandle(lUserHome, strTechnicianUname);
				tecId = (Integer) tecUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// JobsheetObject jobsheetObj = new JobsheetObject();
			// jobsheetObj.mTimeCreated= TimeFormat.createTimeStamp(
			// strTimeStampCreate);
			jobsheetObj.mTimeCreated = TimeFormat.createTimeStamp(strJobsheetDate);
			jobsheetObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampComplete);
			jobsheetObj.mSupervisorId = new Integer(supId.intValue());
			jobsheetObj.mTechnicianId = new Integer(tecId.intValue());
			jobsheetObj.mCurrency = strCcy;
			jobsheetObj.mRemarks = strJobsheetRmks;
			jobsheetObj.mType = JobsheetBean.TYPE_JS;
			jobsheetObj.mState = JobsheetBean.STATE_CREATED;
			jobsheetObj.mStatus = JobsheetBean.STATUS_ACTIVE;
			jobsheetObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			jobsheetObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strPosTypeArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				JobsheetItemObject iio = new JobsheetItemObject();
				// This is the tricky part because we will have to
				// traverse the database to determine the POS Item Id
				// Based on POS Item Type, and the corresponding Item Code
				// (either pkg, svc or inv). strPosTypeArray[nCount] and
				// strPOSItemIdArray[nCount]
				// if(!strPosTypeArray[nCount].equals(JobsheetItemBean.TYPE_NONE))
				if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NONE) != 0)
				{
					// iio.mPosItemId = new Integer("0");
					// ok beware !, need to obtain pkid of posItem given the
					// code
					// For posType=TYPE_NINV and itemCode is not selected,
					// default itemCode to "NON-INV"
					if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NINV) == 0
							&& strPosItemIdArray[nCount].trim().compareTo("") == 0)
					{
						// strPosItemIdArray[nCount] = ItemBean.CODE_NON_INV;
					}
					Integer lPosItemId = POSItemNut.getPosItemIdByCode(strPosTypeArray[nCount],
							strPosItemIdArray[nCount].trim());
					Log.printVerbose(strClassName + funcName + " creating Jobsheet Items");
					iio.mPosItemId = lPosItemId;
					// iio.mRemarks = new String("none");
					iio.mRemarks = strPosRemarksArray[nCount];
					iio.mTotalQty = new BigDecimal(strJobsheetItemQty[nCount].trim());
					iio.mCurrency = new String(strCcy);
					iio.mUnitPriceRecommended = new BigDecimal(strJobsheetItemRecommendedPrice[nCount].trim());
					iio.mUnitPriceQuoted = new BigDecimal(strJobsheetItemQuotedPrice[nCount].trim());
					iio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
					if (!strPosServCodeArray[nCount].equals(""))
					{
						iio.mStrName1 = JobsheetItemBean.STRNAME1_DEPTCODE;
						iio.mStrValue1 = strPosServCodeArray[nCount];
					}
					jobsheetObj.vecJobsheetItems.add(iio);
					Log.printVerbose(strClassName + funcName + " successfully created one Jobsheet Items");
					// vince:todo- Alex, please help to add a loop/sum/variable
					// to add up all the service items
					// and then when the for/loop is done.. we need to execute
					// the following line
					// jobsheetObj.vecJobsheetItems.add(iioSvcTax);
					// where iioSvcTax is the object that you create to account
					// for all the service items in the bill.
					// of course.. inside iioSvcTax we need to have.....
					// iioSvcTax.mPosItemId = POSItemBean.PKID_GOV_SVC_TAX;
					// Please ask me when in doubt.
				}
			}
			// Alex: Although we have custAccId, we should store the
			// CustAccountObject
			// under SalesTxnObject so that it becomes easier to retrieve
			// all info later.
			// Given the customerAccountId, load the CustAccountObject,
			CustAccountObject lCustAccObj = CustAccountNut.getObject(customerAccountId, "");
			Log.printVerbose(lCustAccObj.toString());
			// SalesTxnObject salesTxnObj = new SalesTxnObject();
			salesTxnObj.custAccId = customerAccountId;
			salesTxnObj.custSvcCtrId = new Integer(strCustSvcCtrId);
			salesTxnObj.txnTime = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.remarks = strJobsheetRmks;
			// salesTxnObj.state = SalesTxnBean.ST_CREATED;
			salesTxnObj.state = SalesTxnBean.ST_JOBSHEET_OK;
			salesTxnObj.refForeignTable = VehicleBean.TABLENAME;
			salesTxnObj.refForeignKey = new Integer(strVehicleId);
			salesTxnObj.status = SalesTxnBean.STATUS_ACTIVE;
			salesTxnObj.lastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.userIdUpdate = new Integer(iUsrId.intValue());
			salesTxnObj.strName1 = "mileage";
			salesTxnObj.strValue1 = strVehicleMileage;
			// this.mJobsheetObj = jobsheetObj;
			salesTxnObj.vecJobsheets.add(jobsheetObj);
			// Alex added this ...
			salesTxnObj.custAccObj = lCustAccObj;
			// this.mSalesTxnObj = salesTxnObj;
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate Jobsheet Item Parameters. ";
			errMsg += "Please ensure that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	private String fnPopulateValObjForSO(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			HashMap mapGood, HashMap mapError, JobsheetObject jobsheetObj, SalesTxnObject salesTxnObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{ // super huge try block. TO_DO: Make this more granular later (if
			// need be)
		// String[] theArray = req.getParameterValues("desc");
		// req.setAttribute("theArray",theArray);
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			String strCustSvcCtrId = req.getParameter("CustSvcCtrId");
			// Integer salesTxnId = new Integer((String)
			// req.getParameter("salesTxnId"));
			String strJobsheetDate = (String) req.getParameter("jobsheetDate");
			Log.printVerbose(strClassName + "Getting form variables 2");
			String strSupervisorUname = (String) req.getParameter("supervisorUname");
			String strTechnicianUname = (String) req.getParameter("technicianUname");
			String strUserName = (String) req.getParameter("userName");
			Log.printVerbose(strClassName + "Getting form variables 3");
			String strJobsheetRmks = (String) req.getParameter("jobsheetRemarks");
			String strCcy = (String) req.getParameter("currency");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			Log.printVerbose(strClassName + "Getting form variables 4");
			String strTimeStampComplete = (String) req.getParameter("completeDate");
			String strCustAccId = (String) req.getParameter("custAccountId");
			Log.printDebug("custAccId = " + strCustAccId);
			Log.printVerbose(strClassName + ": tsCreate=" + strTimeStampCreate);
			String[] strPosTypeArray = req.getParameterValues("pos_type");
			String[] strPosServCodeArray = req.getParameterValues("pos_serv_code");
			String[] strPosRemarksArray = req.getParameterValues("pos_item_rmks");
			String[] strPosItemIdArray = req.getParameterValues("pos_item_id");
			for (int i = 0; i < strPosItemIdArray.length; i++)
				Log.printVerbose("posItemId[" + i + "] = " + strPosItemIdArray[i]);
			String[] strJobsheetItemQty = req.getParameterValues("posJobsheetItemQty");
			String[] strJobsheetItemRecommendedPrice = req.getParameterValues("posJobsheetItemStdPrice");
			String[] strJobsheetItemQuotedPrice = req.getParameterValues("posJobsheetItemQuotedPrice");
			/*******************************************************************
			 * BEGIN: VALIDATION
			 ******************************************************************/
			// HashMap mapError = new HashMap();
			// HashMap mapGood = new HashMap();
			validateHeaderFields(strCustSvcCtrId, strJobsheetDate, strSupervisorUname, strTechnicianUname, strUserName,
					strJobsheetRmks, strCcy, strTimeStampCreate, strTimeStampComplete, "", mapError, mapGood);
			validateRowFields(strPosTypeArray, strPosServCodeArray, strPosRemarksArray, strPosItemIdArray,
					strJobsheetItemQty, strJobsheetItemRecommendedPrice, strJobsheetItemQuotedPrice, mapError, mapGood);
			validateTradeInFields(req, mapError, mapGood);
			if (mapError.size() > 0)
			{
				Log.printVerbose("*** mapError contains: " + Arrays.asList(mapError.keySet().toArray()).toString());
				// contains error, do not proceed, but propagate the hidden
				// fields!!
				req.setAttribute("custAccId", new Integer(strCustAccId));
				// req.setAttribute("vehicleId", new Integer(strVehicleId));
				return "Failed Jobsheet Validation";
			}
			/*******************************************************************
			 * END: VALIDATION
			 ******************************************************************/
			// the line below is not implemented yet because
			// at this stage, we assume all transactions are cash based. //
			// todo: once the Customer Vehicle/Customer Identifier
			// is determined. We will populate this field accordingly
			Integer customerAccountId = null;
			// customerAccountId =
			// VehicleNut.getCustIdGivenRegNum(strVehicleNo); // Alex commented
			// out: 9 Nov
			// TO_DO: Double check the strCustAccId.compareTo("NONE") logic
			// below !!
			if (strCustAccId == null || strCustAccId.compareTo("NONE") == 0)
			{
				Log.printDebug("Customer Account Id is NULL! " + "defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			} else
			{
				customerAccountId = new Integer(strCustAccId);
			}
			if (customerAccountId == null)
			{
				// Log.printDebug("Failure in retrieving customer for vehicle no
				// = " + strVehicleNo
				Log.printDebug(" Null customerAccId ... defaulting to One Time Customer");
				customerAccountId = CustAccountBean.PKID_CASH;
			}
			if (strUserName == null || strSupervisorUname == null || strTechnicianUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer supId = null;
			Integer tecId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User supUser = UserNut.getHandle(lUserHome, strSupervisorUname);
				supId = (Integer) supUser.getUserId();
				User tecUser = UserNut.getHandle(lUserHome, strTechnicianUname);
				tecId = (Integer) tecUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// JobsheetObject jobsheetObj = new JobsheetObject();
			// jobsheetObj.mTimeCreated= TimeFormat.createTimeStamp(
			// strTimeStampCreate);
			jobsheetObj.mTimeCreated = TimeFormat.createTimeStamp(strJobsheetDate);
			jobsheetObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampComplete);
			jobsheetObj.mSupervisorId = new Integer(supId.intValue());
			jobsheetObj.mTechnicianId = new Integer(tecId.intValue());
			jobsheetObj.mCurrency = strCcy;
			jobsheetObj.mRemarks = strJobsheetRmks;
			jobsheetObj.mType = JobsheetBean.TYPE_SO;
			jobsheetObj.mState = JobsheetBean.STATE_CREATED;
			jobsheetObj.mStatus = JobsheetBean.STATUS_ACTIVE;
			jobsheetObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			jobsheetObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strPosTypeArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				JobsheetItemObject iio = new JobsheetItemObject();
				// This is the tricky part because we will have to
				// traverse the database to determine the POS Item Id
				// Based on POS Item Type, and the corresponding Item Code
				// (either pkg, svc or inv). strPosTypeArray[nCount] and
				// strPOSItemIdArray[nCount]
				// if(!strPosTypeArray[nCount].equals(JobsheetItemBean.TYPE_NONE))
				if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NONE) != 0)
				{
					// iio.mPosItemId = new Integer("0");
					// ok beware !, need to obtain pkid of posItem given the
					// code
					// For posType=TYPE_NINV and itemCode is not selected,
					// default itemCode to "NON-INV"
					if (strPosTypeArray[nCount].compareTo(POSItemBean.TYPE_NINV) == 0
							&& strPosItemIdArray[nCount].trim().compareTo("") == 0)
					{
						// strPosItemIdArray[nCount] = ItemBean.CODE_NON_INV;
					}
					Integer lPosItemId = POSItemNut.getPosItemIdByCode(strPosTypeArray[nCount],
							strPosItemIdArray[nCount].trim());
					Log.printVerbose(strClassName + funcName + " creating Jobsheet Items");
					iio.mPosItemId = lPosItemId;
					// iio.mRemarks = new String("none");
					iio.mRemarks = strPosRemarksArray[nCount];
					iio.mTotalQty = new BigDecimal(strJobsheetItemQty[nCount].trim());
					iio.mCurrency = new String(strCcy);
					iio.mUnitPriceRecommended = new BigDecimal(strJobsheetItemRecommendedPrice[nCount].trim());
					iio.mUnitPriceQuoted = new BigDecimal(strJobsheetItemQuotedPrice[nCount].trim());
					iio.mStatus = JobsheetItemBean.STATUS_ACTIVE;
					if (!strPosServCodeArray[nCount].equals(""))
					{
						iio.mStrName1 = JobsheetItemBean.STRNAME1_DEPTCODE;
						iio.mStrValue1 = strPosServCodeArray[nCount];
					}
					jobsheetObj.vecJobsheetItems.add(iio);
					Log.printVerbose(strClassName + funcName + " successfully created one Jobsheet Items");
					// vince:todo- Alex, please help to add a loop/sum/variable
					// to add up all the service items
					// and then when the for/loop is done.. we need to execute
					// the following line
					// jobsheetObj.vecJobsheetItems.add(iioSvcTax);
					// where iioSvcTax is the object that you create to account
					// for all the service items in the bill.
					// of course.. inside iioSvcTax we need to have.....
					// iioSvcTax.mPosItemId = POSItemBean.PKID_GOV_SVC_TAX;
					// Please ask me when in doubt.
				}
			}
			// Alex: Although we have custAccId, we should store the
			// CustAccountObject
			// under SalesTxnObject so that it becomes easier to retrieve
			// all info later.
			// Given the customerAccountId, load the CustAccountObject,
			CustAccountObject lCustAccObj = CustAccountNut.getObject(customerAccountId, "");
			Log.printVerbose(lCustAccObj.toString());
			// SalesTxnObject salesTxnObj = new SalesTxnObject();
			salesTxnObj.custAccId = customerAccountId;
			salesTxnObj.custSvcCtrId = new Integer(strCustSvcCtrId);
			salesTxnObj.txnTime = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.remarks = strJobsheetRmks;
			// salesTxnObj.state = SalesTxnBean.ST_CREATED;
			salesTxnObj.state = SalesTxnBean.ST_JOBSHEET_OK;
			// salesTxnObj.refForeignTable = VehicleBean.TABLENAME;
			// salesTxnObj.refForeignKey = new Integer(strVehicleId);
			salesTxnObj.status = SalesTxnBean.STATUS_ACTIVE;
			salesTxnObj.lastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			salesTxnObj.userIdUpdate = new Integer(iUsrId.intValue());
			// salesTxnObj.strName1 = "mileage";
			// salesTxnObj.strValue1 = strVehicleMileage;
			// this.mJobsheetObj = jobsheetObj;
			salesTxnObj.vecJobsheets.add(jobsheetObj);
			// Alex added this ...
			salesTxnObj.custAccObj = lCustAccObj;
			// this.mSalesTxnObj = salesTxnObj;
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate Jobsheet Item Parameters. ";
			errMsg += "Please ensure that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	/*
	 * protected boolean fnCheckVehicleNo(HttpServlet servlet,
	 * HttpServletRequest req, HttpServletResponse res) { String funcName =
	 * "fnCheckVehicleNo()"; Log.printVerbose("In " + funcName);
	 * 
	 * mVehicleNo = req.getParameter("vehicleNo");
	 * 
	 * Vehicle vehicleEJB = VehicleNut.getObjectByRegNum(mVehicleNo);
	 * if(vehicleEJB!=null) { try { // repopulate the real vehicle no as
	 * retrieved from the DB mVehicleNo = vehicleEJB.getRegNum(); mCustAccId =
	 * vehicleEJB.getCustAccountId(); } catch(Exception ex) {
	 * Log.printDebug("Failed to retrieve regNum from vehicle EJB!!"); } return
	 * true; }
	 * 
	 * Log.printVerbose("*** Vehicle No not found in the DB ***");
	 * 
	 * return false;
	 *  }
	 */
	// protected synchronized boolean fnCreateNewCustomer(HttpServlet servlet,
	protected boolean fnCreateNewCustomer(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCreateNewCustomer()";
		/**
		 * STEP 1: Create the main CustAccount EJB
		 */
		Log.printVerbose(strClassName + ": " + "In " + funcName);
		// String custCode = (String) req.getParameter("custCode");
		// String custNewPkid = CustAccountNut.getNextPkid();
		String custName = (String) req.getParameter("custName");
		String custDesc = (String) req.getParameter("custDesc");
		HttpSession session = req.getSession();
		Integer userid = (Integer) session.getAttribute("userId");
		GUIDGenerator guid = null;
		try
		{
			guid = new GUIDGenerator();
		} catch (Exception ex)
		{
			return false;
		}
		CustAccountObject custObj = new CustAccountObject();
		custObj.name = custName;
		custObj.description = custDesc;
		custObj.custAccountCode = guid.getUUID();
		custObj.userIdUpdate = userid;
		custObj.telephone1 = req.getParameter("telephone1");
		custObj.mainAddress1 = req.getParameter("mainAddress1");
		custObj.mainAddress2 = req.getParameter("mainAddress2");
		custObj.mainAddress3 = req.getParameter("mainAddress3");
		custObj.mainPostcode = req.getParameter("mainPostcode");
		custObj.mainState = req.getParameter("mainState");
		custObj.mainCountry = req.getParameter("mainCountry");
		// Ensure CustAccount does not exist
		CustAccount newCustAccount = CustAccountNut.fnCreate(custObj);
		try
		{
			custObj = newCustAccount.getObject("");
		} catch (Exception ex)
		{
			if (newCustAccount != null)
			{
				try
				{
					newCustAccount.remove();
				} catch (Exception ex2)
				{
				}
				;
			}
			return false;
		}
		// For Vehicle
		VehicleObject newVehObj = new VehicleObject();
		newVehObj.regNum = req.getParameter("vehicleNo");
		newVehObj.model = req.getParameter("vehicleModel");
		newVehObj.userIdUpdate = userid;
		newVehObj.custAccountId = custObj.pkid;
		Vehicle vehEJB = VehicleNut.fnCreate(newVehObj);
		try
		{
			newVehObj = vehEJB.getObject();
		} catch (Exception ex)
		{
			if (vehEJB != null)
			{
				try
				{
					newCustAccount.remove();
					vehEJB.remove();
				} catch (Exception ex2)
				{
				}
			}
			return false;
		}
		req.setAttribute("vehicleId", newVehObj.pkid);
		req.setAttribute("custAccId", custObj.pkid);
		return true;
	} // end fnCreateNewCustomer
}
