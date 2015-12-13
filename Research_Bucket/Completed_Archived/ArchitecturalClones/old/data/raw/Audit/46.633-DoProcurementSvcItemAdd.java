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

public class DoProcurementSvcItemAdd implements Action
{
	String strClassName = "DoProcurementSvcItemAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addPOSSvcItem") == 0)
			{
				// add the POS Service Item
				Log.printVerbose(strClassName + ": formName = addPOSSvcItem");
				fnAddService(servlet, req, res);
			}
			if (formName.compareTo("remPOSSvcItem") == 0)
			{
				// remove the POS Service Item
				Log.printVerbose(strClassName + ": formName = remPOSSvcItem");
				fnRemService(servlet, req, res);
			}
			if (formName.compareTo("deactPOSSvcItem") == 0)
			{
				// Deactivate the POS Service Item
				Log.printVerbose(strClassName + ": formName = deactPOSSvcItem");
				fnDeactService(servlet, req, res);
			}
			if (formName.compareTo("actPOSSvcItem") == 0)
			{
				// Activate the POS Service Item
				Log.printVerbose(strClassName + ": formName = actPOSSvcItem");
				fnActivateService(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-add-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		// 1. itrActiveService
		Collection colActiveService = ServiceNut.getActiveObjects();
		Iterator itrActiveService = colActiveService.iterator();
		Log.printVerbose("Setting attribute itrActiveService now");
		req.setAttribute("itrActiveService", itrActiveService);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 2. itrPassiveService
		Collection colInactiveService = ServiceNut.getInactiveObjects();
		Iterator itrInactiveService = colInactiveService.iterator();
		Log.printVerbose("Setting attribute itrPassiveService now");
		req.setAttribute("itrPassiveService", itrInactiveService);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAddService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddService()";
		// Get the request paramaters
		String pos_svc_code = req.getParameter("pos_svc_code");
		String pos_svc_name = req.getParameter("pos_svc_name");
		String pos_svc_desc = req.getParameter("pos_svc_desc");
		String pos_uom = req.getParameter("pos_uom");
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
			if (pos_svc_code == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_code = " + pos_svc_code);
			if (pos_svc_name == null)
			{
				// return;
				throw new Exception("Invalid pos_svc_name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_name = " + pos_svc_name);
			if (pos_svc_desc == null)
			{
				// return;
				throw new Exception("Invalid pos_svc_desc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_desc = " + pos_svc_desc);
			if (pos_uom == null)
			{
				// return;
				throw new Exception("Invalid pos_uom");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_uom = " + pos_uom);
			Log.printVerbose("Adding new POS Service Item");
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
			Service lService = ServiceNut.getObjectByCode(pos_svc_code);
			if (lService != null)
			{
				String rtnMsg = "ERROR: Service aleady exists for the given code, please enter a new code";
				Log.printDebug(rtnMsg);
				throw new Exception(rtnMsg);
			}
			Integer lCatId = new Integer(0); // default categoryId=0
			ServiceHome lSvcHome = ServiceNut.getHome();
			Service newService = lSvcHome.create(pos_svc_code, pos_svc_name, pos_svc_desc, pos_uom, lCatId, tsCreate,
					usrid);
			// if it reaches here, it has successfully created the PurchaseItem
			if (newService != null)
			{
				String rtnMsg = "Successfully created Service '" + pos_svc_code + "'";
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			}
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Add Service with code = " + pos_svc_code + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnAddService

	protected void fnRemService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmSvcCode = (String) req.getParameter("serviceCode");
		if (rmSvcCode != null)
		{
			Service lSvcCode = ServiceNut.getObjectByCode(rmSvcCode);
			if (lSvcCode != null)
			{
				try
				{
					lSvcCode.remove();
				} catch (Exception ex)
				{
					String rtnMsg = "ERROR: Failed to remove Service '"
							+ rmSvcCode
							+ "'"
							+ " because it's still been referenced by other objects."
							+ " Please ensure that all objects currently refering to this service be unlinked before removing this service.";
					req.setAttribute("rtnMsg", rtnMsg);
					Log.printDebug("Remove Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if (lSvcCode !=null)
		} // end if (rmSvcCode != null)
	} // end fnRemService

	protected void fnDeactService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String serviceCode = (String) req.getParameter("serviceCode");
		if (serviceCode != null)
		{
			Service lService = ServiceNut.getObjectByCode(serviceCode);
			if (lService != null)
			{
				try
				{
					lService.setStatus(ServiceBean.STATUS_INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Deactivate Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactService

	protected void fnActivateService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String serviceCode = (String) req.getParameter("serviceCode");
		if (serviceCode != null)
		{
			Service lService = ServiceNut.getObjectByCode(serviceCode);
			if (lService != null)
			{
				try
				{
					lService.setStatus(ServiceBean.STATUS_ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactService
} // end class DoPOSSvcItemAdd
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

public class DoProcurementSvcItemAdd implements Action
{
	String strClassName = "DoProcurementSvcItemAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addPOSSvcItem") == 0)
			{
				// add the POS Service Item
				Log.printVerbose(strClassName + ": formName = addPOSSvcItem");
				fnAddService(servlet, req, res);
			}
			if (formName.compareTo("remPOSSvcItem") == 0)
			{
				// remove the POS Service Item
				Log.printVerbose(strClassName + ": formName = remPOSSvcItem");
				fnRemService(servlet, req, res);
			}
			if (formName.compareTo("deactPOSSvcItem") == 0)
			{
				// Deactivate the POS Service Item
				Log.printVerbose(strClassName + ": formName = deactPOSSvcItem");
				fnDeactService(servlet, req, res);
			}
			if (formName.compareTo("actPOSSvcItem") == 0)
			{
				// Activate the POS Service Item
				Log.printVerbose(strClassName + ": formName = actPOSSvcItem");
				fnActivateService(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-add-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		// 1. itrActiveService
		Collection colActiveService = ServiceNut.getActiveObjects();
		Iterator itrActiveService = colActiveService.iterator();
		Log.printVerbose("Setting attribute itrActiveService now");
		req.setAttribute("itrActiveService", itrActiveService);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 2. itrPassiveService
		Collection colInactiveService = ServiceNut.getInactiveObjects();
		Iterator itrInactiveService = colInactiveService.iterator();
		Log.printVerbose("Setting attribute itrPassiveService now");
		req.setAttribute("itrPassiveService", itrInactiveService);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAddService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddService()";
		// Get the request paramaters
		String pos_svc_code = req.getParameter("pos_svc_code");
		String pos_svc_name = req.getParameter("pos_svc_name");
		String pos_svc_desc = req.getParameter("pos_svc_desc");
		String pos_uom = req.getParameter("pos_uom");
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
			if (pos_svc_code == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_code = " + pos_svc_code);
			if (pos_svc_name == null)
			{
				// return;
				throw new Exception("Invalid pos_svc_name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_name = " + pos_svc_name);
			if (pos_svc_desc == null)
			{
				// return;
				throw new Exception("Invalid pos_svc_desc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_desc = " + pos_svc_desc);
			if (pos_uom == null)
			{
				// return;
				throw new Exception("Invalid pos_uom");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_uom = " + pos_uom);
			Log.printVerbose("Adding new POS Service Item");
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
			Service lService = ServiceNut.getObjectByCode(pos_svc_code);
			if (lService != null)
			{
				String rtnMsg = "ERROR: Service aleady exists for the given code, please enter a new code";
				Log.printDebug(rtnMsg);
				throw new Exception(rtnMsg);
			}
			Integer lCatId = new Integer(0); // default categoryId=0
			ServiceHome lSvcHome = ServiceNut.getHome();
			Service newService = lSvcHome.create(pos_svc_code, pos_svc_name, pos_svc_desc, pos_uom, lCatId, tsCreate,
					usrid);
			// if it reaches here, it has successfully created the PurchaseItem
			if (newService != null)
			{
				String rtnMsg = "Successfully created Service '" + pos_svc_code + "'";
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			}
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Add Service with code = " + pos_svc_code + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnAddService

	protected void fnRemService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmSvcCode = (String) req.getParameter("serviceCode");
		if (rmSvcCode != null)
		{
			Service lSvcCode = ServiceNut.getObjectByCode(rmSvcCode);
			if (lSvcCode != null)
			{
				try
				{
					lSvcCode.remove();
				} catch (Exception ex)
				{
					String rtnMsg = "ERROR: Failed to remove Service '"
							+ rmSvcCode
							+ "'"
							+ " because it's still been referenced by other objects."
							+ " Please ensure that all objects currently refering to this service be unlinked before removing this service.";
					req.setAttribute("rtnMsg", rtnMsg);
					Log.printDebug("Remove Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if (lSvcCode !=null)
		} // end if (rmSvcCode != null)
	} // end fnRemService

	protected void fnDeactService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String serviceCode = (String) req.getParameter("serviceCode");
		if (serviceCode != null)
		{
			Service lService = ServiceNut.getObjectByCode(serviceCode);
			if (lService != null)
			{
				try
				{
					lService.setStatus(ServiceBean.STATUS_INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Deactivate Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactService

	protected void fnActivateService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String serviceCode = (String) req.getParameter("serviceCode");
		if (serviceCode != null)
		{
			Service lService = ServiceNut.getObjectByCode(serviceCode);
			if (lService != null)
			{
				try
				{
					lService.setStatus(ServiceBean.STATUS_ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactService
} // end class DoPOSSvcItemAdd
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

public class DoProcurementSvcItemAdd implements Action
{
	String strClassName = "DoProcurementSvcItemAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addPOSSvcItem") == 0)
			{
				// add the POS Service Item
				Log.printVerbose(strClassName + ": formName = addPOSSvcItem");
				fnAddService(servlet, req, res);
			}
			if (formName.compareTo("remPOSSvcItem") == 0)
			{
				// remove the POS Service Item
				Log.printVerbose(strClassName + ": formName = remPOSSvcItem");
				fnRemService(servlet, req, res);
			}
			if (formName.compareTo("deactPOSSvcItem") == 0)
			{
				// Deactivate the POS Service Item
				Log.printVerbose(strClassName + ": formName = deactPOSSvcItem");
				fnDeactService(servlet, req, res);
			}
			if (formName.compareTo("actPOSSvcItem") == 0)
			{
				// Activate the POS Service Item
				Log.printVerbose(strClassName + ": formName = actPOSSvcItem");
				fnActivateService(servlet, req, res);
			}
		}
		fnGetParams(servlet, req, res);
		return new ActionRouter("supp-procurement-add-svc-items-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetParams()";
		// 1. itrActiveService
		Collection colActiveService = ServiceNut.getActiveObjects();
		Iterator itrActiveService = colActiveService.iterator();
		Log.printVerbose("Setting attribute itrActiveService now");
		req.setAttribute("itrActiveService", itrActiveService);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
		// 2. itrPassiveService
		Collection colInactiveService = ServiceNut.getInactiveObjects();
		Iterator itrInactiveService = colInactiveService.iterator();
		Log.printVerbose("Setting attribute itrPassiveService now");
		req.setAttribute("itrPassiveService", itrInactiveService);
		Log.printVerbose("Leaving " + strClassName + "::" + funcName);
	}

	protected void fnAddService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddService()";
		// Get the request paramaters
		String pos_svc_code = req.getParameter("pos_svc_code");
		String pos_svc_name = req.getParameter("pos_svc_name");
		String pos_svc_desc = req.getParameter("pos_svc_desc");
		String pos_uom = req.getParameter("pos_uom");
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
			if (pos_svc_code == null)
			{
				// return;
				throw new Exception("Invalid Item Code");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_code = " + pos_svc_code);
			if (pos_svc_name == null)
			{
				// return;
				throw new Exception("Invalid pos_svc_name");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_name = " + pos_svc_name);
			if (pos_svc_desc == null)
			{
				// return;
				throw new Exception("Invalid pos_svc_desc");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_svc_desc = " + pos_svc_desc);
			if (pos_uom == null)
			{
				// return;
				throw new Exception("Invalid pos_uom");
			} else
				Log.printVerbose(strClassName + ":" + funcName + " - pos_uom = " + pos_uom);
			Log.printVerbose("Adding new POS Service Item");
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
			Service lService = ServiceNut.getObjectByCode(pos_svc_code);
			if (lService != null)
			{
				String rtnMsg = "ERROR: Service aleady exists for the given code, please enter a new code";
				Log.printDebug(rtnMsg);
				throw new Exception(rtnMsg);
			}
			Integer lCatId = new Integer(0); // default categoryId=0
			ServiceHome lSvcHome = ServiceNut.getHome();
			Service newService = lSvcHome.create(pos_svc_code, pos_svc_name, pos_svc_desc, pos_uom, lCatId, tsCreate,
					usrid);
			// if it reaches here, it has successfully created the PurchaseItem
			if (newService != null)
			{
				String rtnMsg = "Successfully created Service '" + pos_svc_code + "'";
				Log.printDebug(rtnMsg);
				req.setAttribute("rtnMsg", rtnMsg);
			}
		} // end try
		catch (Exception ex)
		{
			String rtnMsg = "Add Service with code = " + pos_svc_code + " -  Failed: " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
		} // end try-catch
	} // end fnAddService

	protected void fnRemService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmSvcCode = (String) req.getParameter("serviceCode");
		if (rmSvcCode != null)
		{
			Service lSvcCode = ServiceNut.getObjectByCode(rmSvcCode);
			if (lSvcCode != null)
			{
				try
				{
					lSvcCode.remove();
				} catch (Exception ex)
				{
					String rtnMsg = "ERROR: Failed to remove Service '"
							+ rmSvcCode
							+ "'"
							+ " because it's still been referenced by other objects."
							+ " Please ensure that all objects currently refering to this service be unlinked before removing this service.";
					req.setAttribute("rtnMsg", rtnMsg);
					Log.printDebug("Remove Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if (lSvcCode !=null)
		} // end if (rmSvcCode != null)
	} // end fnRemService

	protected void fnDeactService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String serviceCode = (String) req.getParameter("serviceCode");
		if (serviceCode != null)
		{
			Service lService = ServiceNut.getObjectByCode(serviceCode);
			if (lService != null)
			{
				try
				{
					lService.setStatus(ServiceBean.STATUS_INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Deactivate Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactService

	protected void fnActivateService(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String serviceCode = (String) req.getParameter("serviceCode");
		if (serviceCode != null)
		{
			Service lService = ServiceNut.getObjectByCode(serviceCode);
			if (lService != null)
			{
				try
				{
					lService.setStatus(ServiceBean.STATUS_ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate Service Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnDeactService
} // end class DoPOSSvcItemAdd
