package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoPOSSvcItemEdit implements Action
{
	String strClassName = "DoPOSSvcItemEdit";

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
		return new ActionRouter("cust-pos-edit-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Service) editSvc, given the
		 * serviceCode [2] (POSItem) posSvcItem corresponding to the editSvc [
		 * if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the serviceId from serviceCode
			String lSvcCode = req.getParameter("serviceCode");
			Service lService = ServiceNut.getObjectByCode(lSvcCode);
			if (lService != null)
			{
				req.setAttribute("editSvc", lService);
				// Now try to obtain the posSvcItem corresponding to editSvc
				POSItem posSvcItem = POSItemNut.getPOSSvcItem(lService.getPkid());
				if (posSvcItem != null)
				{
					req.setAttribute("posSvcItem", posSvcItem);
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
			// Try to get the corresponding POSItem
			POSItem lPOSItem = POSItemNut.getPOSSvcItem(new Integer(serviceId));
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the POSItem
				String rtnMsg = "Successfully edited POSItem for Service Code = " + serviceCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_SVC;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(serviceId);
				posObj.itemType = lItemType;
				posObj.currency = lCurrency;
				posObj.unitPriceStd = new BigDecimal(stdPrice);
				posObj.unitPriceDiscounted = new BigDecimal(discPrice);
				posObj.unitPriceMin = new BigDecimal(minPrice);
				posObj.userIdUpdate = usrid;
				POSItem newPOSItem = POSItemNut.fnCreate(posObj);
				// if it reaches here, it has successfully created the POSItem
				if (newPOSItem != null)
				{
					String rtnMsg = "Successfully created POSItem for Service Code = " + serviceCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update POSItem for Service Code = " + serviceCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateService
} // end class DoPOSSvcItemEdit
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoPOSSvcItemEdit implements Action
{
	String strClassName = "DoPOSSvcItemEdit";

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
		return new ActionRouter("cust-pos-edit-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Service) editSvc, given the
		 * serviceCode [2] (POSItem) posSvcItem corresponding to the editSvc [
		 * if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the serviceId from serviceCode
			String lSvcCode = req.getParameter("serviceCode");
			Service lService = ServiceNut.getObjectByCode(lSvcCode);
			if (lService != null)
			{
				req.setAttribute("editSvc", lService);
				// Now try to obtain the posSvcItem corresponding to editSvc
				POSItem posSvcItem = POSItemNut.getPOSSvcItem(lService.getPkid());
				if (posSvcItem != null)
				{
					req.setAttribute("posSvcItem", posSvcItem);
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
			// Try to get the corresponding POSItem
			POSItem lPOSItem = POSItemNut.getPOSSvcItem(new Integer(serviceId));
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the POSItem
				String rtnMsg = "Successfully edited POSItem for Service Code = " + serviceCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_SVC;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(serviceId);
				posObj.itemType = lItemType;
				posObj.currency = lCurrency;
				posObj.unitPriceStd = new BigDecimal(stdPrice);
				posObj.unitPriceDiscounted = new BigDecimal(discPrice);
				posObj.unitPriceMin = new BigDecimal(minPrice);
				posObj.userIdUpdate = usrid;
				POSItem newPOSItem = POSItemNut.fnCreate(posObj);
				// if it reaches here, it has successfully created the POSItem
				if (newPOSItem != null)
				{
					String rtnMsg = "Successfully created POSItem for Service Code = " + serviceCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update POSItem for Service Code = " + serviceCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateService
} // end class DoPOSSvcItemEdit
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoPOSSvcItemEdit implements Action
{
	String strClassName = "DoPOSSvcItemEdit";

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
		return new ActionRouter("cust-pos-edit-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		/*
		 * Need to populate 2 things: [1] (Service) editSvc, given the
		 * serviceCode [2] (POSItem) posSvcItem corresponding to the editSvc [
		 * if exist ]
		 */
		try
		{ // super huge try block
			// Obtain the serviceId from serviceCode
			String lSvcCode = req.getParameter("serviceCode");
			Service lService = ServiceNut.getObjectByCode(lSvcCode);
			if (lService != null)
			{
				req.setAttribute("editSvc", lService);
				// Now try to obtain the posSvcItem corresponding to editSvc
				POSItem posSvcItem = POSItemNut.getPOSSvcItem(lService.getPkid());
				if (posSvcItem != null)
				{
					req.setAttribute("posSvcItem", posSvcItem);
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
			// Try to get the corresponding POSItem
			POSItem lPOSItem = POSItemNut.getPOSSvcItem(new Integer(serviceId));
			// Edit the posItem if exist, else create a new row for it
			if (lPOSItem != null)
			{
				lPOSItem.setCurrency(currency);
				lPOSItem.setUnitPriceStd(new BigDecimal(stdPrice));
				lPOSItem.setUnitPriceDiscounted(new BigDecimal(discPrice));
				lPOSItem.setUnitPriceMin(new BigDecimal(minPrice));
				lPOSItem.setLastUpdate(tsCreate);
				lPOSItem.setUserIdUpdate(usrid);
				// if it reaches here, it has successfully edited the POSItem
				String rtnMsg = "Successfully edited POSItem for Service Code = " + serviceCode;
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			} else
			{
				// Create a new row for it
				String lItemType = POSItemBean.TYPE_SVC;
				String lCurrency = "MYR"; // default
				Integer lCatId = new Integer(0); // default
				POSItemObject posObj = new POSItemObject();
				posObj.itemFKId = new Integer(serviceId);
				posObj.itemType = lItemType;
				posObj.currency = lCurrency;
				posObj.unitPriceStd = new BigDecimal(stdPrice);
				posObj.unitPriceDiscounted = new BigDecimal(discPrice);
				posObj.unitPriceMin = new BigDecimal(minPrice);
				posObj.userIdUpdate = usrid;
				POSItem newPOSItem = POSItemNut.fnCreate(posObj);
				// if it reaches here, it has successfully created the POSItem
				if (newPOSItem != null)
				{
					String rtnMsg = "Successfully created POSItem for Service Code = " + serviceCode;
					Log.printDebug(rtnMsg);
					req.setAttribute("rtnMsg", rtnMsg);
				}
			} // end if(lPOSItem != null)
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Update POSItem for Service Code = " + serviceCode + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnUpdateService
} // end class DoPOSSvcItemEdit
