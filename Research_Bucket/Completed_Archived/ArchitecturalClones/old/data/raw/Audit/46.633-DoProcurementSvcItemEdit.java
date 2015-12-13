/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of Wavelet Technology,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoProcurementSvcItemEdit implements Action
{
	String strClassName = "DoProcurementSvcItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateService") == 0)
			{
				// update the POS Service Item
				fnUpdateService(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-edit-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Service) editSvc, given the
		 * serviceCode [2] (PurchaseItem) purchaseSvcItem corresponding to the
		 * editSvc [ if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the serviceId from serviceCode
			String lSvcCode = req.getParameter("serviceCode");
			Service lService = ServiceNut.getObjectByCode(lSvcCode);
			if (lService != null)
			{
				req.setAttribute("editSvc", lService);
				// Now try to obtain the purchaseSvcItem corresponding to
				// editSvc
				PurchaseItem purchaseSvcItem = PurchaseItemNut.getPurchaseSvcItem(lService.getPkid());
				if (purchaseSvcItem != null)
				{
					req.setAttribute("purchaseSvcItem", purchaseSvcItem);
				}
			} // end if (lService != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnUpdateService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateService()";
		// Get the request paramaters
		String serviceId = (String) req.getParameter("serviceId");
		String serviceCode = (String) req.getParameter("serviceCode");
		String serviceName = (String) req.getParameter("serviceName");
		String serviceDesc = (String) req.getParameter("serviceDesc");
		String serviceUOM = req.getParameter("serviceUOM");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				// return;
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			if (serviceCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceCode = " + serviceCode);
			if (serviceName == null)
			{
				// return;
				throw new Exception("Invalid serviceName");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceName = " + serviceName);
			if (serviceDesc == null)
			{
				// return;
				throw new Exception("Invalid serviceDesc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceDesc = " + serviceDesc);
			if (serviceUOM == null)
			{
				// return;
				throw new Exception("Invalid serviceUOM");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceUOM = " + serviceUOM);
			if (currency == null)
			{
				// return;
				throw new Exception("Invalid currency");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
			if (stdPrice == null)
			{
				// return;
				throw new Exception("Invalid stdPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - stdPrice = " + stdPrice);
			if (discPrice == null)
			{
				// return;
				throw new Exception("Invalid discPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - discPrice = " + discPrice);
			if (minPrice == null)
			{
				// return;
				throw new Exception("Invalid minPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - minPrice = " + minPrice);
			Log.printVerbose("Editing Service Inventory Item ... ");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			// Get the Service
			Service lSvc = ServiceNut.getHandle(new Integer(serviceId));
			if (lSvc == null)
			{
				throw new Exception("Cannot Edit Null Service");
			}
			// Edit the Service Details here
			lSvc.setCode(serviceCode);
			lSvc.setName(serviceName);
			lSvc.setDescription(serviceDesc);
			lSvc.setUnitMeasure(serviceUOM);
			lSvc.setLastUpdate(tsCreate);
			lSvc.setUserIdUpdate(usrid);
			// Try to get the corresponding PurchaseItem
			PurchaseItem lPurchaseItem = PurchaseItemNut.getPurchaseSvcItem(new Integer(serviceId));
			// Edit the purchaseItem if exist, else create a new row for it
			if (lPurchaseItem != null)
			{
				lPurchaseItem.setCurrency(currency);
				lPurchaseItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPurchaseItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPurchaseItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPurchaseItem.setLastUpdate(tsCreate);
				lPurchaseItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the
				// PurchaseItem
				String rtnMsg = "Successfully edited PurchaseItem for Service Code = " + serviceCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = PurchaseItemBean.TYPE_SVC;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				PurchaseItemObject purObj = new PurchaseItemObject();
				purObj.itemFKId = new Integer(serviceId);
				purObj.itemType = lItemType;
				purObj.currency = lCurrency;
				purObj.unitPriceStd = new BigDecimal(stdPrice);
				purObj.unitPriceDiscounted = new BigDecimal(discPrice);
				purObj.unitPriceMin = new BigDecimal(minPrice);
				purObj.userIdUpdate = usrid;
				PurchaseItem newPurchaseItem = PurchaseItemNut.fnCreate(purObj);
				if (newPurchaseItem != null)
				{
					String rtnMsg = "Successfully created PurchaseItem for Service Code = " + serviceCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPurchaseItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update PurchaseItem for Service Code = " + serviceCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateService
} // end class DoPOSSvcItemEdit
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of Wavelet Technology,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoProcurementSvcItemEdit implements Action
{
	String strClassName = "DoProcurementSvcItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateService") == 0)
			{
				// update the POS Service Item
				fnUpdateService(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-edit-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Service) editSvc, given the
		 * serviceCode [2] (PurchaseItem) purchaseSvcItem corresponding to the
		 * editSvc [ if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the serviceId from serviceCode
			String lSvcCode = req.getParameter("serviceCode");
			Service lService = ServiceNut.getObjectByCode(lSvcCode);
			if (lService != null)
			{
				req.setAttribute("editSvc", lService);
				// Now try to obtain the purchaseSvcItem corresponding to
				// editSvc
				PurchaseItem purchaseSvcItem = PurchaseItemNut.getPurchaseSvcItem(lService.getPkid());
				if (purchaseSvcItem != null)
				{
					req.setAttribute("purchaseSvcItem", purchaseSvcItem);
				}
			} // end if (lService != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnUpdateService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateService()";
		// Get the request paramaters
		String serviceId = (String) req.getParameter("serviceId");
		String serviceCode = (String) req.getParameter("serviceCode");
		String serviceName = (String) req.getParameter("serviceName");
		String serviceDesc = (String) req.getParameter("serviceDesc");
		String serviceUOM = req.getParameter("serviceUOM");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				// return;
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			if (serviceCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceCode = " + serviceCode);
			if (serviceName == null)
			{
				// return;
				throw new Exception("Invalid serviceName");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceName = " + serviceName);
			if (serviceDesc == null)
			{
				// return;
				throw new Exception("Invalid serviceDesc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceDesc = " + serviceDesc);
			if (serviceUOM == null)
			{
				// return;
				throw new Exception("Invalid serviceUOM");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceUOM = " + serviceUOM);
			if (currency == null)
			{
				// return;
				throw new Exception("Invalid currency");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
			if (stdPrice == null)
			{
				// return;
				throw new Exception("Invalid stdPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - stdPrice = " + stdPrice);
			if (discPrice == null)
			{
				// return;
				throw new Exception("Invalid discPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - discPrice = " + discPrice);
			if (minPrice == null)
			{
				// return;
				throw new Exception("Invalid minPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - minPrice = " + minPrice);
			Log.printVerbose("Editing Service Inventory Item ... ");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			// Get the Service
			Service lSvc = ServiceNut.getHandle(new Integer(serviceId));
			if (lSvc == null)
			{
				throw new Exception("Cannot Edit Null Service");
			}
			// Edit the Service Details here
			lSvc.setCode(serviceCode);
			lSvc.setName(serviceName);
			lSvc.setDescription(serviceDesc);
			lSvc.setUnitMeasure(serviceUOM);
			lSvc.setLastUpdate(tsCreate);
			lSvc.setUserIdUpdate(usrid);
			// Try to get the corresponding PurchaseItem
			PurchaseItem lPurchaseItem = PurchaseItemNut.getPurchaseSvcItem(new Integer(serviceId));
			// Edit the purchaseItem if exist, else create a new row for it
			if (lPurchaseItem != null)
			{
				lPurchaseItem.setCurrency(currency);
				lPurchaseItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPurchaseItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPurchaseItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPurchaseItem.setLastUpdate(tsCreate);
				lPurchaseItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the
				// PurchaseItem
				String rtnMsg = "Successfully edited PurchaseItem for Service Code = " + serviceCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = PurchaseItemBean.TYPE_SVC;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				PurchaseItemObject purObj = new PurchaseItemObject();
				purObj.itemFKId = new Integer(serviceId);
				purObj.itemType = lItemType;
				purObj.currency = lCurrency;
				purObj.unitPriceStd = new BigDecimal(stdPrice);
				purObj.unitPriceDiscounted = new BigDecimal(discPrice);
				purObj.unitPriceMin = new BigDecimal(minPrice);
				purObj.userIdUpdate = usrid;
				PurchaseItem newPurchaseItem = PurchaseItemNut.fnCreate(purObj);
				if (newPurchaseItem != null)
				{
					String rtnMsg = "Successfully created PurchaseItem for Service Code = " + serviceCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPurchaseItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update PurchaseItem for Service Code = " + serviceCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateService
} // end class DoPOSSvcItemEdit
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of Wavelet Technology,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.procurement;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoProcurementSvcItemEdit implements Action
{
	String strClassName = "DoProcurementSvcItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("updateService") == 0)
			{
				// update the POS Service Item
				fnUpdateService(servlet, req, res);
				// fnGetParams(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-edit-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Service) editSvc, given the
		 * serviceCode [2] (PurchaseItem) purchaseSvcItem corresponding to the
		 * editSvc [ if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the serviceId from serviceCode
			String lSvcCode = req.getParameter("serviceCode");
			Service lService = ServiceNut.getObjectByCode(lSvcCode);
			if (lService != null)
			{
				req.setAttribute("editSvc", lService);
				// Now try to obtain the purchaseSvcItem corresponding to
				// editSvc
				PurchaseItem purchaseSvcItem = PurchaseItemNut.getPurchaseSvcItem(lService.getPkid());
				if (purchaseSvcItem != null)
				{
					req.setAttribute("purchaseSvcItem", purchaseSvcItem);
				}
			} // end if (lService != null)
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
		}
	} // end fnGetParams

	protected void fnUpdateService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateService()";
		// Get the request paramaters
		String serviceId = (String) req.getParameter("serviceId");
		String serviceCode = (String) req.getParameter("serviceCode");
		String serviceName = (String) req.getParameter("serviceName");
		String serviceDesc = (String) req.getParameter("serviceDesc");
		String serviceUOM = req.getParameter("serviceUOM");
		String currency = req.getParameter("currency");
		String stdPrice = req.getParameter("stdPrice");
		String discPrice = req.getParameter("discPrice");
		String minPrice = req.getParameter("minPrice");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				// return;
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			if (serviceCode == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceCode = " + serviceCode);
			if (serviceName == null)
			{
				// return;
				throw new Exception("Invalid serviceName");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceName = " + serviceName);
			if (serviceDesc == null)
			{
				// return;
				throw new Exception("Invalid serviceDesc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceDesc = " + serviceDesc);
			if (serviceUOM == null)
			{
				// return;
				throw new Exception("Invalid serviceUOM");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - serviceUOM = " + serviceUOM);
			if (currency == null)
			{
				// return;
				throw new Exception("Invalid currency");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - currency = " + currency);
			if (stdPrice == null)
			{
				// return;
				throw new Exception("Invalid stdPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - stdPrice = " + stdPrice);
			if (discPrice == null)
			{
				// return;
				throw new Exception("Invalid discPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - discPrice = " + discPrice);
			if (minPrice == null)
			{
				// return;
				throw new Exception("Invalid minPrice");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - minPrice = " + minPrice);
			Log.printVerbose("Editing Service Inventory Item ... ");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			// Get the Service
			Service lSvc = ServiceNut.getHandle(new Integer(serviceId));
			if (lSvc == null)
			{
				throw new Exception("Cannot Edit Null Service");
			}
			// Edit the Service Details here
			lSvc.setCode(serviceCode);
			lSvc.setName(serviceName);
			lSvc.setDescription(serviceDesc);
			lSvc.setUnitMeasure(serviceUOM);
			lSvc.setLastUpdate(tsCreate);
			lSvc.setUserIdUpdate(usrid);
			// Try to get the corresponding PurchaseItem
			PurchaseItem lPurchaseItem = PurchaseItemNut.getPurchaseSvcItem(new Integer(serviceId));
			// Edit the purchaseItem if exist, else create a new row for it
			if (lPurchaseItem != null)
			{
				lPurchaseItem.setCurrency(currency);
				lPurchaseItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPurchaseItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPurchaseItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPurchaseItem.setLastUpdate(tsCreate);
				lPurchaseItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the
				// PurchaseItem
				String rtnMsg = "Successfully edited PurchaseItem for Service Code = " + serviceCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = PurchaseItemBean.TYPE_SVC;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				PurchaseItemObject purObj = new PurchaseItemObject();
				purObj.itemFKId = new Integer(serviceId);
				purObj.itemType = lItemType;
				purObj.currency = lCurrency;
				purObj.unitPriceStd = new BigDecimal(stdPrice);
				purObj.unitPriceDiscounted = new BigDecimal(discPrice);
				purObj.unitPriceMin = new BigDecimal(minPrice);
				purObj.userIdUpdate = usrid;
				PurchaseItem newPurchaseItem = PurchaseItemNut.fnCreate(purObj);
				if (newPurchaseItem != null)
				{
					String rtnMsg = "Successfully created PurchaseItem for Service Code = " + serviceCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPurchaseItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update PurchaseItem for Service Code = " + serviceCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateService
} // end class DoPOSSvcItemEdit
