/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoNewDeliveryOrder implements Action
{
	String strClassName = "DoNewDeliveryOrder";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		// If the form name is null,
		// the users are creating a fresh invoice
		if (formName == null)
		{
			fnGetObjectsForForm(servlet, req, res);
			return new ActionRouter("pos-deliveryorder-new-01-page");
		}
		if (formName.equals("selectSO"))
		{
			try
			{
				String strSoPkid = (String) req.getParameter("soPkid");
				Long soPkid = new Long(strSoPkid);
				// check if the SO exists
				Jobsheet soEJB = JobsheetNut.getHandle(soPkid);
				// check the SO/Sales txn state/status
				String soState = soEJB.getState();
				String soStatus = soEJB.getStatus();
				Integer soType = soEJB.getType();
				// Confirm if js_type = SalesOrder before proceeding
				if (soType.compareTo(JobsheetBean.TYPE_SO) != 0)
				{
					String errMsg = new String(" The selected PKID is NOT a SalesOrder."
							+ " Please enter a valid SalesOrder PKID");
					req.setAttribute("strErrMsg", errMsg);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				}
				// Alex: Temporary do without this STATE_APPROVED check first.
				// if(soState.equals(JobsheetBean.STATE_APPROVED) &&
				// soStatus.equals(JobsheetBean.STATUS_ACTIVE))
				if (soStatus.equals(JobsheetBean.STATUS_ACTIVE))
				{
					fnPopulateCreateDOForm(servlet, req, res);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				} else
				{
					String errMsg = new String("The Sales Transaction is" + " not in a valid state");
					req.setAttribute("strErrMsg", errMsg);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
				String errMsg = new String("The Sales Order PKID does not " + "exist.");
				req.setAttribute("strErrMsg", errMsg);
				return new ActionRouter("pos-deliveryorder-new-01-page");
			}
			// Process existing SO info, and previous DO for
			// this SalesTxn if exist
		}
		// If the form has been filled and user submit to create
		// a new posOrder, do the necessary checking
		if (formName.equals("createDeliveryOrder"))
		{
			boolean bDeliveryOrderVerified = false;
			Log.printVerbose(strClassName + ": before fnCreateNewDeliveryOrder");
			bDeliveryOrderVerified = fnCreateNewDeliveryOrder(servlet, req, res);
			if (bDeliveryOrderVerified == true)
			{
				return new ActionRouter("pos-deliveryorder-new-02-page");
			} else
			{
				return new ActionRouter("pos-deliveryorder-new-01-page");
			}
		}
		// fnGetObjectsForForm(servlet, req, res);
		Log.printVerbose(strClassName + ": returning default ActionRouter");
		return new ActionRouter("pos-deliveryorder-new-01-page");
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnPopulateCreateDOForm(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Get Sales Order Value Object
		String strSoPkid = (String) req.getParameter("soPkid");
		Long soPkid = new Long(strSoPkid);
		// check if the SO exists
		JobsheetObject soObj = JobsheetNut.getObject(soPkid);
		Collection colUsers1 = null;
		Collection colUsers2 = null;
		UserHome lUserHome = UserNut.getHome();
		String fieldName = new String("status");
		// Get users list
		try
		{
			colUsers1 = (Collection) lUserHome.findUsersGiven(fieldName, UserBean.ACTIVE);
			colUsers2 = (Collection) lUserHome.findUsersGiven(fieldName, UserBean.ACTIVE);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("DoUserRoleAddRem: " + ex.getMessage());
		}
		Iterator itrActiveUsers1 = colUsers1.iterator();
		Iterator itrActiveUsers2 = colUsers2.iterator();
		SalesTxnObject sTxnObj = SalesTxnNut.getObject(soObj.mSalesTxnId);
		req.setAttribute("itrActiveUsers1", itrActiveUsers1);
		req.setAttribute("itrActiveUsers2", itrActiveUsers2);
		// req.setAttribute("suppAccObj",saObj);
		req.setAttribute("soObj", soObj);
		// req.setAttribute("colDeliveredQty",colDeliveredQty);
		req.setAttribute("sTxnObj", sTxnObj);
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected boolean fnCreateNewDeliveryOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCreateNewDeliveryOrder()";
		Log.printVerbose(funcName);
		// Alex (27 Oct) - Changed member variables to local variables.
		String mStrErrMsg = new String("");
		String strObjectName = new String("DoNewDeliveryOrder");
		DeliveryOrder doEJB = null;
		DeliveryOrderObject mDeliveryOrderObj = new DeliveryOrderObject();
		String strSoPkid = (String) req.getParameter("soPkid");
		Long soPkid = new Long(strSoPkid);
		JobsheetObject mSOObj = JobsheetNut.getObject(soPkid);
		try
		{ // super huge try-catch
			String errMsg = fnPopulateValueObjects(servlet, req, res, mDeliveryOrderObj, mSOObj);
			if (errMsg != null)
			{
				throw new Exception(errMsg);
			} else
			{
				// Perform precon check
				if (!fnPreconCheck(mDeliveryOrderObj, mSOObj))
				{
					throw new Exception("Failed Precondition check when creating a new DO!"
							+ "Please check that you have entered the Receiving Qty correctly");
				}
				try
				{
					// the following function will automatically rollback
					// if creation is not successful
					// so that servlet only handle higher level logic
					doEJB = DeliveryOrderNut.fnCreateTree(mDeliveryOrderObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
					String strErrMsg = "Failed to create SalesTxnBean: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// Alex: Retrieve the salesTxnObj
			SalesTxnObject salesTxnObj = SalesTxnNut.getObject(mDeliveryOrderObj.mSalesTxnId);
			// salesTxnEjb.setState(SalesTxnBean.ST_JOBSHEET_OK);
			// Populate attributes
			req.setAttribute("doObj", mDeliveryOrderObj);
			req.setAttribute("salesTxnObj", salesTxnObj);
			return true;
		} // end of super huge try
		catch (Exception ex)
		{
			ex.printStackTrace();
			// Handle exceptions in a generic way
			String userErrMsg = "Failed to create new DeliveryOrder. Reason: " + ex.getMessage();
			// Populate the errMsg to be sent to the JSP
			req.setAttribute("strErrMsg", userErrMsg);
			return false;
		}
		// return true;
	}

	// ///////////////////////////////////////////////////////////////////
	protected void fnGetObjectsForForm(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		Collection colActiveSvcC = CustServiceCenterNut.getCollectionByField(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE);
		Iterator itrActiveSvcC = colActiveSvcC.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	// ///////////////////////////////////////////////////////////////////
	private String fnPopulateValueObjects(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			DeliveryOrderObject doObj, JobsheetObject mSOObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			Long soPkid = new Long((String) req.getParameter("soPkid"));
			Long sTxnPkid = new Long((String) req.getParameter("sTxnPkid"));
			String strDeliveryOrderDate = (String) req.getParameter("doDate");
			String strAcknowledgeUname = (String) req.getParameter("ackUname");
			String strApproverUname = (String) req.getParameter("appUname");
			String strUserName = (String) req.getParameter("userName");
			String strDeliveryOrderRmks = (String) req.getParameter("doRemarks");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			String strTimeStampComplete = (String) req.getParameter("tsComplete");
			String[] strSalesOrderItemArray = req.getParameterValues("soiPkid");
			String[] strReceiveQtyArray = req.getParameterValues("deliverQty");
			if (strUserName == null || strAcknowledgeUname == null || strApproverUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer ackId = null;
			Integer appId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User ackUser = UserNut.getHandle(lUserHome, strAcknowledgeUname);
				ackId = (Integer) ackUser.getUserId();
				User appUser = UserNut.getHandle(lUserHome, strApproverUname);
				appId = (Integer) appUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// DeliveryOrderObject doObj = new DeliveryOrderObject();
			doObj.mSalesTxnId = mSOObj.mSalesTxnId;
			doObj.mTimeCreated = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mAcknowledgeId = new Integer(ackId.intValue());
			doObj.mApproverId = new Integer(appId.intValue());
			doObj.mRemarks = strDeliveryOrderRmks;
			doObj.mState = DeliveryOrderBean.STATE_CREATED;
			doObj.mStatus = DeliveryOrderBean.STATUS_ACTIVE;
			doObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strSalesOrderItemArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				DeliveryOrderItemObject doiObj = new DeliveryOrderItemObject();
				// doiObj.mSalesItemId = new Integer("0");
				// ok beware !, need to obtain pkid of posItem given the code
				// For posType=TYPE_NINV and itemCode is not selected,
				// default itemCode to "NON-INV"
				BigDecimal bdThisRowRcvQty = null;
				try
				{
					bdThisRowRcvQty = new BigDecimal(strReceiveQtyArray[nCount]);
					Log.printVerbose(strClassName + funcName + ":: bdThisRowRcvQty = " + bdThisRowRcvQty.toString());
				} catch (Exception ex)
				{
					// ex.printStackTrace();
					Log.printVerbose("Detected empty rows, ignore it");
				}
				if (bdThisRowRcvQty != null)
				{
					Log.printVerbose(strClassName + funcName + " creating DeliveryOrder Items");
					doiObj.mRemarks = new String("");
					doiObj.mSalesOrderItemId = new Long(strSalesOrderItemArray[nCount]);
					doiObj.mTotalQty = bdThisRowRcvQty;
					Log.printVerbose(strClassName + " bdThisRowRcvQty = " + bdThisRowRcvQty.toString());
					doiObj.mStatus = DeliveryOrderItemBean.STATUS_ACTIVE;
					doiObj.mState = DeliveryOrderItemBean.STATE_PARTIAL;
					// Alex: Get the POSItemChildObject
					JobsheetItem soItem = JobsheetItemNut.getHandle(doiObj.mSalesOrderItemId);
					if (soItem != null)
					{
						Log.printVerbose("Obtained SalesOrderItem!");
						POSItemChildObject posICO = POSItemNut.getChildObject(soItem.getPosItemId());
						doiObj.mSalesItem = posICO;
					}
					doObj.vecDOItems.add(doiObj);
					Log.printVerbose(strClassName + funcName + " checkpoint 8");
					Log.printVerbose(strClassName + funcName + " successfully created one DeliveryOrder Items");
				}// end if
			}// end for
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate DeliveryOrder" + " Item Parameters. ";
			errMsg += "Please ensure" + " that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	private boolean fnPreconCheck(DeliveryOrderObject doObj, JobsheetObject soObj)
	{
		try
		{
			// To check if the quantities quoted in doObj does not exceed those
			// quoted in soObj
			// PRECON 1: doObj.vecDOItems.size() > 0
			if (doObj.vecDOItems.isEmpty())
			{
				Log.printVerbose("vecDOItems is empty - doing nothing ... ");
				return false;
			}
			boolean bDOComplete = true; // To track that all DO is done.
			// PRECON 2: Receiving Qty + Delivered Qty <= Total Purchased
			for (int countA = 0; countA < doObj.vecDOItems.size(); countA++)
			{
				DeliveryOrderItemObject doIObj = (DeliveryOrderItemObject) doObj.vecDOItems.get(countA);
				// Get the corresponding JobsheetItemObject
				JobsheetItemObject soiObj = (JobsheetItemObject) soObj.findJSIObj(doIObj.mSalesOrderItemId);
				if (soiObj == null)
				{
					Log.printDebug("NULL JobsheetItemObject");
					return false;
				}
				// POSItemChildObject posICO =
				// POSItemNut.getChildObject(soiObj.mPosItemId) ;
				BigDecimal thisRowQty = soiObj.mTotalQty;
				BigDecimal soiDelivered = DeliveryOrderItemNut.getQtyDelivered(soiObj.mPkid);
				BigDecimal doiReceivingQty = doIObj.mTotalQty;
				/*
				 * if(soiDelivered==null) { soiDelivered = new
				 * BigDecimal("0.0");} if(matchedDOItemObj != null) {
				 * Log.printVerbose("Matched DO Item (PKID = " +
				 * matchedDOItemObj.mPkid.toString() + ") to SO Item (PKID = " +
				 * soiObj.mPkid.toString() + ")"); BigDecimal doiReceivingQty =
				 * matchedDOItemObj.mTotalQty; Log.printVerbose("***
				 * doiReceivingQty = " + doiReceivingQty.toString()); }
				 */
				BigDecimal bdOutstanding = thisRowQty.subtract(soiDelivered.add(doiReceivingQty));
				Log.printVerbose("*** thisRowQty = " + thisRowQty.toString());
				Log.printVerbose("*** soiDelivered = " + soiDelivered.toString());
				Log.printVerbose("*** bdOutstanding = " + bdOutstanding.toString());
				// if bdOutstanding < 0, return error (false)
				if (bdOutstanding.compareTo(new BigDecimal(0)) < 0)
				{
					Log.printDebug("Precon check failed for SOItem =" + countA
							+ "; Receiving Qty exceeded Outstanding Qty");
					return false;
				}
				if (bdOutstanding.compareTo(new BigDecimal(0)) > 0)
				{
					Log.printVerbose("Detected outstanding DOItems, DO NOT OK yet");
					bDOComplete = false;
				}
			}// end of count for loop
			if (bDOComplete)
			{
				// set the Jobsheet State to do_ok
				JobsheetNut.setDOOK(soObj.mPkid);
			}
			// If it reaches up till here without errors, return true (success)
			return true;
		} // end big try
		catch (Exception ex)
		{
			Log.printDebug("Exception caught while performing DO precondition check: " + ex.getMessage());
			return false;
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
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoNewDeliveryOrder implements Action
{
	String strClassName = "DoNewDeliveryOrder";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		// If the form name is null,
		// the users are creating a fresh invoice
		if (formName == null)
		{
			fnGetObjectsForForm(servlet, req, res);
			return new ActionRouter("pos-deliveryorder-new-01-page");
		}
		if (formName.equals("selectSO"))
		{
			try
			{
				String strSoPkid = (String) req.getParameter("soPkid");
				Long soPkid = new Long(strSoPkid);
				// check if the SO exists
				Jobsheet soEJB = JobsheetNut.getHandle(soPkid);
				// check the SO/Sales txn state/status
				String soState = soEJB.getState();
				String soStatus = soEJB.getStatus();
				Integer soType = soEJB.getType();
				// Confirm if js_type = SalesOrder before proceeding
				if (soType.compareTo(JobsheetBean.TYPE_SO) != 0)
				{
					String errMsg = new String(" The selected PKID is NOT a SalesOrder."
							+ " Please enter a valid SalesOrder PKID");
					req.setAttribute("strErrMsg", errMsg);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				}
				// Alex: Temporary do without this STATE_APPROVED check first.
				// if(soState.equals(JobsheetBean.STATE_APPROVED) &&
				// soStatus.equals(JobsheetBean.STATUS_ACTIVE))
				if (soStatus.equals(JobsheetBean.STATUS_ACTIVE))
				{
					fnPopulateCreateDOForm(servlet, req, res);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				} else
				{
					String errMsg = new String("The Sales Transaction is" + " not in a valid state");
					req.setAttribute("strErrMsg", errMsg);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
				String errMsg = new String("The Sales Order PKID does not " + "exist.");
				req.setAttribute("strErrMsg", errMsg);
				return new ActionRouter("pos-deliveryorder-new-01-page");
			}
			// Process existing SO info, and previous DO for
			// this SalesTxn if exist
		}
		// If the form has been filled and user submit to create
		// a new posOrder, do the necessary checking
		if (formName.equals("createDeliveryOrder"))
		{
			boolean bDeliveryOrderVerified = false;
			Log.printVerbose(strClassName + ": before fnCreateNewDeliveryOrder");
			bDeliveryOrderVerified = fnCreateNewDeliveryOrder(servlet, req, res);
			if (bDeliveryOrderVerified == true)
			{
				return new ActionRouter("pos-deliveryorder-new-02-page");
			} else
			{
				return new ActionRouter("pos-deliveryorder-new-01-page");
			}
		}
		// fnGetObjectsForForm(servlet, req, res);
		Log.printVerbose(strClassName + ": returning default ActionRouter");
		return new ActionRouter("pos-deliveryorder-new-01-page");
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnPopulateCreateDOForm(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Get Sales Order Value Object
		String strSoPkid = (String) req.getParameter("soPkid");
		Long soPkid = new Long(strSoPkid);
		// check if the SO exists
		JobsheetObject soObj = JobsheetNut.getObject(soPkid);
		Collection colUsers1 = null;
		Collection colUsers2 = null;
		UserHome lUserHome = UserNut.getHome();
		String fieldName = new String("status");
		// Get users list
		try
		{
			colUsers1 = (Collection) lUserHome.findUsersGiven(fieldName, UserBean.ACTIVE);
			colUsers2 = (Collection) lUserHome.findUsersGiven(fieldName, UserBean.ACTIVE);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("DoUserRoleAddRem: " + ex.getMessage());
		}
		Iterator itrActiveUsers1 = colUsers1.iterator();
		Iterator itrActiveUsers2 = colUsers2.iterator();
		SalesTxnObject sTxnObj = SalesTxnNut.getObject(soObj.mSalesTxnId);
		req.setAttribute("itrActiveUsers1", itrActiveUsers1);
		req.setAttribute("itrActiveUsers2", itrActiveUsers2);
		// req.setAttribute("suppAccObj",saObj);
		req.setAttribute("soObj", soObj);
		// req.setAttribute("colDeliveredQty",colDeliveredQty);
		req.setAttribute("sTxnObj", sTxnObj);
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected boolean fnCreateNewDeliveryOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCreateNewDeliveryOrder()";
		Log.printVerbose(funcName);
		// Alex (27 Oct) - Changed member variables to local variables.
		String mStrErrMsg = new String("");
		String strObjectName = new String("DoNewDeliveryOrder");
		DeliveryOrder doEJB = null;
		DeliveryOrderObject mDeliveryOrderObj = new DeliveryOrderObject();
		String strSoPkid = (String) req.getParameter("soPkid");
		Long soPkid = new Long(strSoPkid);
		JobsheetObject mSOObj = JobsheetNut.getObject(soPkid);
		try
		{ // super huge try-catch
			String errMsg = fnPopulateValueObjects(servlet, req, res, mDeliveryOrderObj, mSOObj);
			if (errMsg != null)
			{
				throw new Exception(errMsg);
			} else
			{
				// Perform precon check
				if (!fnPreconCheck(mDeliveryOrderObj, mSOObj))
				{
					throw new Exception("Failed Precondition check when creating a new DO!"
							+ "Please check that you have entered the Receiving Qty correctly");
				}
				try
				{
					// the following function will automatically rollback
					// if creation is not successful
					// so that servlet only handle higher level logic
					doEJB = DeliveryOrderNut.fnCreateTree(mDeliveryOrderObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
					String strErrMsg = "Failed to create SalesTxnBean: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// Alex: Retrieve the salesTxnObj
			SalesTxnObject salesTxnObj = SalesTxnNut.getObject(mDeliveryOrderObj.mSalesTxnId);
			// salesTxnEjb.setState(SalesTxnBean.ST_JOBSHEET_OK);
			// Populate attributes
			req.setAttribute("doObj", mDeliveryOrderObj);
			req.setAttribute("salesTxnObj", salesTxnObj);
			return true;
		} // end of super huge try
		catch (Exception ex)
		{
			ex.printStackTrace();
			// Handle exceptions in a generic way
			String userErrMsg = "Failed to create new DeliveryOrder. Reason: " + ex.getMessage();
			// Populate the errMsg to be sent to the JSP
			req.setAttribute("strErrMsg", userErrMsg);
			return false;
		}
		// return true;
	}

	// ///////////////////////////////////////////////////////////////////
	protected void fnGetObjectsForForm(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		Collection colActiveSvcC = CustServiceCenterNut.getCollectionByField(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE);
		Iterator itrActiveSvcC = colActiveSvcC.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	// ///////////////////////////////////////////////////////////////////
	private String fnPopulateValueObjects(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			DeliveryOrderObject doObj, JobsheetObject mSOObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			Long soPkid = new Long((String) req.getParameter("soPkid"));
			Long sTxnPkid = new Long((String) req.getParameter("sTxnPkid"));
			String strDeliveryOrderDate = (String) req.getParameter("doDate");
			String strAcknowledgeUname = (String) req.getParameter("ackUname");
			String strApproverUname = (String) req.getParameter("appUname");
			String strUserName = (String) req.getParameter("userName");
			String strDeliveryOrderRmks = (String) req.getParameter("doRemarks");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			String strTimeStampComplete = (String) req.getParameter("tsComplete");
			String[] strSalesOrderItemArray = req.getParameterValues("soiPkid");
			String[] strReceiveQtyArray = req.getParameterValues("deliverQty");
			if (strUserName == null || strAcknowledgeUname == null || strApproverUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer ackId = null;
			Integer appId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User ackUser = UserNut.getHandle(lUserHome, strAcknowledgeUname);
				ackId = (Integer) ackUser.getUserId();
				User appUser = UserNut.getHandle(lUserHome, strApproverUname);
				appId = (Integer) appUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// DeliveryOrderObject doObj = new DeliveryOrderObject();
			doObj.mSalesTxnId = mSOObj.mSalesTxnId;
			doObj.mTimeCreated = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mAcknowledgeId = new Integer(ackId.intValue());
			doObj.mApproverId = new Integer(appId.intValue());
			doObj.mRemarks = strDeliveryOrderRmks;
			doObj.mState = DeliveryOrderBean.STATE_CREATED;
			doObj.mStatus = DeliveryOrderBean.STATUS_ACTIVE;
			doObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strSalesOrderItemArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				DeliveryOrderItemObject doiObj = new DeliveryOrderItemObject();
				// doiObj.mSalesItemId = new Integer("0");
				// ok beware !, need to obtain pkid of posItem given the code
				// For posType=TYPE_NINV and itemCode is not selected,
				// default itemCode to "NON-INV"
				BigDecimal bdThisRowRcvQty = null;
				try
				{
					bdThisRowRcvQty = new BigDecimal(strReceiveQtyArray[nCount]);
					Log.printVerbose(strClassName + funcName + ":: bdThisRowRcvQty = " + bdThisRowRcvQty.toString());
				} catch (Exception ex)
				{
					// ex.printStackTrace();
					Log.printVerbose("Detected empty rows, ignore it");
				}
				if (bdThisRowRcvQty != null)
				{
					Log.printVerbose(strClassName + funcName + " creating DeliveryOrder Items");
					doiObj.mRemarks = new String("");
					doiObj.mSalesOrderItemId = new Long(strSalesOrderItemArray[nCount]);
					doiObj.mTotalQty = bdThisRowRcvQty;
					Log.printVerbose(strClassName + " bdThisRowRcvQty = " + bdThisRowRcvQty.toString());
					doiObj.mStatus = DeliveryOrderItemBean.STATUS_ACTIVE;
					doiObj.mState = DeliveryOrderItemBean.STATE_PARTIAL;
					// Alex: Get the POSItemChildObject
					JobsheetItem soItem = JobsheetItemNut.getHandle(doiObj.mSalesOrderItemId);
					if (soItem != null)
					{
						Log.printVerbose("Obtained SalesOrderItem!");
						POSItemChildObject posICO = POSItemNut.getChildObject(soItem.getPosItemId());
						doiObj.mSalesItem = posICO;
					}
					doObj.vecDOItems.add(doiObj);
					Log.printVerbose(strClassName + funcName + " checkpoint 8");
					Log.printVerbose(strClassName + funcName + " successfully created one DeliveryOrder Items");
				}// end if
			}// end for
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate DeliveryOrder" + " Item Parameters. ";
			errMsg += "Please ensure" + " that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	private boolean fnPreconCheck(DeliveryOrderObject doObj, JobsheetObject soObj)
	{
		try
		{
			// To check if the quantities quoted in doObj does not exceed those
			// quoted in soObj
			// PRECON 1: doObj.vecDOItems.size() > 0
			if (doObj.vecDOItems.isEmpty())
			{
				Log.printVerbose("vecDOItems is empty - doing nothing ... ");
				return false;
			}
			boolean bDOComplete = true; // To track that all DO is done.
			// PRECON 2: Receiving Qty + Delivered Qty <= Total Purchased
			for (int countA = 0; countA < doObj.vecDOItems.size(); countA++)
			{
				DeliveryOrderItemObject doIObj = (DeliveryOrderItemObject) doObj.vecDOItems.get(countA);
				// Get the corresponding JobsheetItemObject
				JobsheetItemObject soiObj = (JobsheetItemObject) soObj.findJSIObj(doIObj.mSalesOrderItemId);
				if (soiObj == null)
				{
					Log.printDebug("NULL JobsheetItemObject");
					return false;
				}
				// POSItemChildObject posICO =
				// POSItemNut.getChildObject(soiObj.mPosItemId) ;
				BigDecimal thisRowQty = soiObj.mTotalQty;
				BigDecimal soiDelivered = DeliveryOrderItemNut.getQtyDelivered(soiObj.mPkid);
				BigDecimal doiReceivingQty = doIObj.mTotalQty;
				/*
				 * if(soiDelivered==null) { soiDelivered = new
				 * BigDecimal("0.0");} if(matchedDOItemObj != null) {
				 * Log.printVerbose("Matched DO Item (PKID = " +
				 * matchedDOItemObj.mPkid.toString() + ") to SO Item (PKID = " +
				 * soiObj.mPkid.toString() + ")"); BigDecimal doiReceivingQty =
				 * matchedDOItemObj.mTotalQty; Log.printVerbose("***
				 * doiReceivingQty = " + doiReceivingQty.toString()); }
				 */
				BigDecimal bdOutstanding = thisRowQty.subtract(soiDelivered.add(doiReceivingQty));
				Log.printVerbose("*** thisRowQty = " + thisRowQty.toString());
				Log.printVerbose("*** soiDelivered = " + soiDelivered.toString());
				Log.printVerbose("*** bdOutstanding = " + bdOutstanding.toString());
				// if bdOutstanding < 0, return error (false)
				if (bdOutstanding.compareTo(new BigDecimal(0)) < 0)
				{
					Log.printDebug("Precon check failed for SOItem =" + countA
							+ "; Receiving Qty exceeded Outstanding Qty");
					return false;
				}
				if (bdOutstanding.compareTo(new BigDecimal(0)) > 0)
				{
					Log.printVerbose("Detected outstanding DOItems, DO NOT OK yet");
					bDOComplete = false;
				}
			}// end of count for loop
			if (bDOComplete)
			{
				// set the Jobsheet State to do_ok
				JobsheetNut.setDOOK(soObj.mPkid);
			}
			// If it reaches up till here without errors, return true (success)
			return true;
		} // end big try
		catch (Exception ex)
		{
			Log.printDebug("Exception caught while performing DO precondition check: " + ex.getMessage());
			return false;
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
package com.vlee.servlet.pos;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoNewDeliveryOrder implements Action
{
	String strClassName = "DoNewDeliveryOrder";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		// If the form name is null,
		// the users are creating a fresh invoice
		if (formName == null)
		{
			fnGetObjectsForForm(servlet, req, res);
			return new ActionRouter("pos-deliveryorder-new-01-page");
		}
		if (formName.equals("selectSO"))
		{
			try
			{
				String strSoPkid = (String) req.getParameter("soPkid");
				Long soPkid = new Long(strSoPkid);
				// check if the SO exists
				Jobsheet soEJB = JobsheetNut.getHandle(soPkid);
				// check the SO/Sales txn state/status
				String soState = soEJB.getState();
				String soStatus = soEJB.getStatus();
				Integer soType = soEJB.getType();
				// Confirm if js_type = SalesOrder before proceeding
				if (soType.compareTo(JobsheetBean.TYPE_SO) != 0)
				{
					String errMsg = new String(" The selected PKID is NOT a SalesOrder."
							+ " Please enter a valid SalesOrder PKID");
					req.setAttribute("strErrMsg", errMsg);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				}
				// Alex: Temporary do without this STATE_APPROVED check first.
				// if(soState.equals(JobsheetBean.STATE_APPROVED) &&
				// soStatus.equals(JobsheetBean.STATUS_ACTIVE))
				if (soStatus.equals(JobsheetBean.STATUS_ACTIVE))
				{
					fnPopulateCreateDOForm(servlet, req, res);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				} else
				{
					String errMsg = new String("The Sales Transaction is" + " not in a valid state");
					req.setAttribute("strErrMsg", errMsg);
					return new ActionRouter("pos-deliveryorder-new-01-page");
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
				String errMsg = new String("The Sales Order PKID does not " + "exist.");
				req.setAttribute("strErrMsg", errMsg);
				return new ActionRouter("pos-deliveryorder-new-01-page");
			}
			// Process existing SO info, and previous DO for
			// this SalesTxn if exist
		}
		// If the form has been filled and user submit to create
		// a new posOrder, do the necessary checking
		if (formName.equals("createDeliveryOrder"))
		{
			boolean bDeliveryOrderVerified = false;
			Log.printVerbose(strClassName + ": before fnCreateNewDeliveryOrder");
			bDeliveryOrderVerified = fnCreateNewDeliveryOrder(servlet, req, res);
			if (bDeliveryOrderVerified == true)
			{
				return new ActionRouter("pos-deliveryorder-new-02-page");
			} else
			{
				return new ActionRouter("pos-deliveryorder-new-01-page");
			}
		}
		// fnGetObjectsForForm(servlet, req, res);
		Log.printVerbose(strClassName + ": returning default ActionRouter");
		return new ActionRouter("pos-deliveryorder-new-01-page");
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected void fnPopulateCreateDOForm(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Get Sales Order Value Object
		String strSoPkid = (String) req.getParameter("soPkid");
		Long soPkid = new Long(strSoPkid);
		// check if the SO exists
		JobsheetObject soObj = JobsheetNut.getObject(soPkid);
		Collection colUsers1 = null;
		Collection colUsers2 = null;
		UserHome lUserHome = UserNut.getHome();
		String fieldName = new String("status");
		// Get users list
		try
		{
			colUsers1 = (Collection) lUserHome.findUsersGiven(fieldName, UserBean.ACTIVE);
			colUsers2 = (Collection) lUserHome.findUsersGiven(fieldName, UserBean.ACTIVE);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("DoUserRoleAddRem: " + ex.getMessage());
		}
		Iterator itrActiveUsers1 = colUsers1.iterator();
		Iterator itrActiveUsers2 = colUsers2.iterator();
		SalesTxnObject sTxnObj = SalesTxnNut.getObject(soObj.mSalesTxnId);
		req.setAttribute("itrActiveUsers1", itrActiveUsers1);
		req.setAttribute("itrActiveUsers2", itrActiveUsers2);
		// req.setAttribute("suppAccObj",saObj);
		req.setAttribute("soObj", soObj);
		// req.setAttribute("colDeliveredQty",colDeliveredQty);
		req.setAttribute("sTxnObj", sTxnObj);
	}

	// /////////////////////////////////////////////////////////////////
	// //
	protected boolean fnCreateNewDeliveryOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnCreateNewDeliveryOrder()";
		Log.printVerbose(funcName);
		// Alex (27 Oct) - Changed member variables to local variables.
		String mStrErrMsg = new String("");
		String strObjectName = new String("DoNewDeliveryOrder");
		DeliveryOrder doEJB = null;
		DeliveryOrderObject mDeliveryOrderObj = new DeliveryOrderObject();
		String strSoPkid = (String) req.getParameter("soPkid");
		Long soPkid = new Long(strSoPkid);
		JobsheetObject mSOObj = JobsheetNut.getObject(soPkid);
		try
		{ // super huge try-catch
			String errMsg = fnPopulateValueObjects(servlet, req, res, mDeliveryOrderObj, mSOObj);
			if (errMsg != null)
			{
				throw new Exception(errMsg);
			} else
			{
				// Perform precon check
				if (!fnPreconCheck(mDeliveryOrderObj, mSOObj))
				{
					throw new Exception("Failed Precondition check when creating a new DO!"
							+ "Please check that you have entered the Receiving Qty correctly");
				}
				try
				{
					// the following function will automatically rollback
					// if creation is not successful
					// so that servlet only handle higher level logic
					doEJB = DeliveryOrderNut.fnCreateTree(mDeliveryOrderObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
					String strErrMsg = "Failed to create SalesTxnBean: " + ex.getMessage();
					throw new Exception(strErrMsg);
				}
			}
			// Alex: Retrieve the salesTxnObj
			SalesTxnObject salesTxnObj = SalesTxnNut.getObject(mDeliveryOrderObj.mSalesTxnId);
			// salesTxnEjb.setState(SalesTxnBean.ST_JOBSHEET_OK);
			// Populate attributes
			req.setAttribute("doObj", mDeliveryOrderObj);
			req.setAttribute("salesTxnObj", salesTxnObj);
			return true;
		} // end of super huge try
		catch (Exception ex)
		{
			ex.printStackTrace();
			// Handle exceptions in a generic way
			String userErrMsg = "Failed to create new DeliveryOrder. Reason: " + ex.getMessage();
			// Populate the errMsg to be sent to the JSP
			req.setAttribute("strErrMsg", userErrMsg);
			return false;
		}
		// return true;
	}

	// ///////////////////////////////////////////////////////////////////
	protected void fnGetObjectsForForm(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		Collection colActiveSvcC = CustServiceCenterNut.getCollectionByField(CustServiceCenterBean.STATUS,
				CustServiceCenterBean.STATUS_ACTIVE);
		Iterator itrActiveSvcC = colActiveSvcC.iterator();
		req.setAttribute("itrActiveSvcC", itrActiveSvcC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	// ///////////////////////////////////////////////////////////////////
	private String fnPopulateValueObjects(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res,
			DeliveryOrderObject doObj, JobsheetObject mSOObj)
	{
		String strErrMsg = null;
		String funcName = "::fnPopulateValueObjects";
		Log.printVerbose(strClassName + funcName + ": before try");
		try
		{
			// get form variables from the jsp page
			Log.printVerbose(strClassName + "Getting form variables 1");
			Long soPkid = new Long((String) req.getParameter("soPkid"));
			Long sTxnPkid = new Long((String) req.getParameter("sTxnPkid"));
			String strDeliveryOrderDate = (String) req.getParameter("doDate");
			String strAcknowledgeUname = (String) req.getParameter("ackUname");
			String strApproverUname = (String) req.getParameter("appUname");
			String strUserName = (String) req.getParameter("userName");
			String strDeliveryOrderRmks = (String) req.getParameter("doRemarks");
			String strTimeStampCreate = (String) req.getParameter("tsCreate");
			String strTimeStampComplete = (String) req.getParameter("tsComplete");
			String[] strSalesOrderItemArray = req.getParameterValues("soiPkid");
			String[] strReceiveQtyArray = req.getParameterValues("deliverQty");
			if (strUserName == null || strAcknowledgeUname == null || strApproverUname == null)
			{
				strErrMsg += "<div class='errMsg'>" + "User does not exist ! </div>";
				req.setAttribute("strErrMsg", strErrMsg);
				Log.printDebug(strClassName + ": strUserName is null ");
				return new String("Null username");
			}
			Integer iUsrId = null;
			Integer ackId = null;
			Integer appId = null;
			try
			{
				UserHome lUserHome = UserNut.getHome();
				User lUser = UserNut.getHandle(lUserHome, strUserName);
				iUsrId = (Integer) lUser.getUserId();
				User ackUser = UserNut.getHandle(lUserHome, strAcknowledgeUname);
				ackId = (Integer) ackUser.getUserId();
				User appUser = UserNut.getHandle(lUserHome, strApproverUname);
				appId = (Integer) appUser.getUserId();
			} catch (Exception ex)
			{
				Log.printDebug(ex.getMessage() + "Invalid userid");
				return new String("Invalid Userid");
			}
			// DeliveryOrderObject doObj = new DeliveryOrderObject();
			doObj.mSalesTxnId = mSOObj.mSalesTxnId;
			doObj.mTimeCreated = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mTimeComplete = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mAcknowledgeId = new Integer(ackId.intValue());
			doObj.mApproverId = new Integer(appId.intValue());
			doObj.mRemarks = strDeliveryOrderRmks;
			doObj.mState = DeliveryOrderBean.STATE_CREATED;
			doObj.mStatus = DeliveryOrderBean.STATUS_ACTIVE;
			doObj.mLastUpdate = TimeFormat.createTimeStamp(strTimeStampCreate);
			doObj.mUserIdUpdate = new Integer(iUsrId.intValue());
			// next, populate these info in value objects
			int nRows = strSalesOrderItemArray.length;
			for (int nCount = 0; nCount < nRows; nCount++)
			{
				DeliveryOrderItemObject doiObj = new DeliveryOrderItemObject();
				// doiObj.mSalesItemId = new Integer("0");
				// ok beware !, need to obtain pkid of posItem given the code
				// For posType=TYPE_NINV and itemCode is not selected,
				// default itemCode to "NON-INV"
				BigDecimal bdThisRowRcvQty = null;
				try
				{
					bdThisRowRcvQty = new BigDecimal(strReceiveQtyArray[nCount]);
					Log.printVerbose(strClassName + funcName + ":: bdThisRowRcvQty = " + bdThisRowRcvQty.toString());
				} catch (Exception ex)
				{
					// ex.printStackTrace();
					Log.printVerbose("Detected empty rows, ignore it");
				}
				if (bdThisRowRcvQty != null)
				{
					Log.printVerbose(strClassName + funcName + " creating DeliveryOrder Items");
					doiObj.mRemarks = new String("");
					doiObj.mSalesOrderItemId = new Long(strSalesOrderItemArray[nCount]);
					doiObj.mTotalQty = bdThisRowRcvQty;
					Log.printVerbose(strClassName + " bdThisRowRcvQty = " + bdThisRowRcvQty.toString());
					doiObj.mStatus = DeliveryOrderItemBean.STATUS_ACTIVE;
					doiObj.mState = DeliveryOrderItemBean.STATE_PARTIAL;
					// Alex: Get the POSItemChildObject
					JobsheetItem soItem = JobsheetItemNut.getHandle(doiObj.mSalesOrderItemId);
					if (soItem != null)
					{
						Log.printVerbose("Obtained SalesOrderItem!");
						POSItemChildObject posICO = POSItemNut.getChildObject(soItem.getPosItemId());
						doiObj.mSalesItem = posICO;
					}
					doObj.vecDOItems.add(doiObj);
					Log.printVerbose(strClassName + funcName + " checkpoint 8");
					Log.printVerbose(strClassName + funcName + " successfully created one DeliveryOrder Items");
				}// end if
			}// end for
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(ex.getMessage());
			String errMsg = "Failed to Populate DeliveryOrder" + " Item Parameters. ";
			errMsg += "Please ensure" + " that you have entered all data in its appropriate format";
			errMsg += ex.getMessage();
			return errMsg;
		}
		return strErrMsg;
	}

	private boolean fnPreconCheck(DeliveryOrderObject doObj, JobsheetObject soObj)
	{
		try
		{
			// To check if the quantities quoted in doObj does not exceed those
			// quoted in soObj
			// PRECON 1: doObj.vecDOItems.size() > 0
			if (doObj.vecDOItems.isEmpty())
			{
				Log.printVerbose("vecDOItems is empty - doing nothing ... ");
				return false;
			}
			boolean bDOComplete = true; // To track that all DO is done.
			// PRECON 2: Receiving Qty + Delivered Qty <= Total Purchased
			for (int countA = 0; countA < doObj.vecDOItems.size(); countA++)
			{
				DeliveryOrderItemObject doIObj = (DeliveryOrderItemObject) doObj.vecDOItems.get(countA);
				// Get the corresponding JobsheetItemObject
				JobsheetItemObject soiObj = (JobsheetItemObject) soObj.findJSIObj(doIObj.mSalesOrderItemId);
				if (soiObj == null)
				{
					Log.printDebug("NULL JobsheetItemObject");
					return false;
				}
				// POSItemChildObject posICO =
				// POSItemNut.getChildObject(soiObj.mPosItemId) ;
				BigDecimal thisRowQty = soiObj.mTotalQty;
				BigDecimal soiDelivered = DeliveryOrderItemNut.getQtyDelivered(soiObj.mPkid);
				BigDecimal doiReceivingQty = doIObj.mTotalQty;
				/*
				 * if(soiDelivered==null) { soiDelivered = new
				 * BigDecimal("0.0");} if(matchedDOItemObj != null) {
				 * Log.printVerbose("Matched DO Item (PKID = " +
				 * matchedDOItemObj.mPkid.toString() + ") to SO Item (PKID = " +
				 * soiObj.mPkid.toString() + ")"); BigDecimal doiReceivingQty =
				 * matchedDOItemObj.mTotalQty; Log.printVerbose("***
				 * doiReceivingQty = " + doiReceivingQty.toString()); }
				 */
				BigDecimal bdOutstanding = thisRowQty.subtract(soiDelivered.add(doiReceivingQty));
				Log.printVerbose("*** thisRowQty = " + thisRowQty.toString());
				Log.printVerbose("*** soiDelivered = " + soiDelivered.toString());
				Log.printVerbose("*** bdOutstanding = " + bdOutstanding.toString());
				// if bdOutstanding < 0, return error (false)
				if (bdOutstanding.compareTo(new BigDecimal(0)) < 0)
				{
					Log.printDebug("Precon check failed for SOItem =" + countA
							+ "; Receiving Qty exceeded Outstanding Qty");
					return false;
				}
				if (bdOutstanding.compareTo(new BigDecimal(0)) > 0)
				{
					Log.printVerbose("Detected outstanding DOItems, DO NOT OK yet");
					bDOComplete = false;
				}
			}// end of count for loop
			if (bDOComplete)
			{
				// set the Jobsheet State to do_ok
				JobsheetNut.setDOOK(soObj.mPkid);
			}
			// If it reaches up till here without errors, return true (success)
			return true;
		} // end big try
		catch (Exception ex)
		{
			Log.printDebug("Exception caught while performing DO precondition check: " + ex.getMessage());
			return false;
		}
	}
}
