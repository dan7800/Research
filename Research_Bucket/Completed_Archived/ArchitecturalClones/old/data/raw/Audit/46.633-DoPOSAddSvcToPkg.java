/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoPOSAddSvcToPkg implements Action
{
	private String strClassName = "DoPOSAddSvcToPkg";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = (String) req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addSvcToPkg") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "addSvcToPkg");
				fnAddSvcToPkg(servlet, req, res);
				return new ActionRouter("cust-pos-added-svc-to-pkg-page");
			}
			if (formName.compareTo("showAddServices") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "showAddServices");
				fnGetAllInvSvcs(servlet, req, res);
			}
		}
		return new ActionRouter("cust-pos-add-svc-to-pkg-page");
	}

	protected void fnGetAllInvSvcs(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetAllInvSvcs()";
		// propagate the packageId to the receiving JSP
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		req.setAttribute("packageId", packageId);
		req.setAttribute("packageCode", packageCode);
		try
		{
			ServiceHome lSvcHome = (ServiceHome) ServiceNut.getHome();
			if (lSvcHome != null)
			{
				String lSortBy = ServiceBean.CODE;
				Vector vecSvcCodes = new Vector();
				Vector vecSvcNames = new Vector();
				Collection colSvcObj = (Collection) lSvcHome.getAllCodeName(lSortBy);
				/*
				 * if (colPkg != null) { for (Iterator itr = colPkg.iterator();
				 * itr.hasNext();) { com.vlee.ejb.customer.Service lCustAcc =
				 * (com.vlee.ejb.customer.Service) itr.next();
				 * vecSvcCodes.add(lCustAcc.getCode());
				 * vecSvcNames.add(lCustAcc.getName()); }
				 *  }
				 */
				// proof of concept, split colSvcObj into vecSvcCode and
				// vecSvcName
				for (Iterator itemObjItr = colSvcObj.iterator(); itemObjItr.hasNext();)
				{
					ServiceObject lSvcObj = (ServiceObject) itemObjItr.next();
					vecSvcCodes.add(lSvcObj.getCode());
					vecSvcNames.add(lSvcObj.getName());
				}
				if (vecSvcCodes != null && vecSvcNames != null)
				{
					Log.printVerbose(strClassName + ":" + funcName + " - vecSvcCodes = " + debugVector(vecSvcCodes));
					Log.printVerbose(strClassName + ":" + funcName + " - vecSvcNames = " + debugVector(vecSvcNames));
					req.setAttribute("vecSvcCodes", vecSvcCodes);
					req.setAttribute("vecSvcNames", vecSvcNames);
				}
			}
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
			return;
		}
	}

	protected void fnAddSvcToPkg(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSvcToPkg()";
		// Get the required parameters
		String[] svcsToAdd = (String[]) req.getParameterValues("svcsToAdd");
		// String[] svcQuantities = (String[]) req.getParameterValues("svcQty");
		String packageId = (String) req.getParameter("packageId");
		String lPkgCode = (String) req.getParameter("packageCode");
		Log.printVerbose("*** Size of svcsToAdd = " + svcsToAdd.length + "***");
		// Log.printVerbose("*** Size of svcQuantities = " +
		// svcQuantities.length + "***");
		try
		{
			if (svcsToAdd == null || packageId == null || lPkgCode == null)
			{
				String rtnMsg = "Null svcsToAdd or packageId";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			// Now start the adding ....
			Vector vecSvcsAdded = new Vector();
			String strQty;
			BigDecimal qtyToAdd;
			for (int i = 0; i < svcsToAdd.length; i++)
			{
				// Obtain the itemId to add
				Service lSvc = ServiceNut.getObjectByCode(svcsToAdd[i]);
				Integer lSvcId = lSvc.getPkid();
				// Obtain the quantity to add
				if ((strQty = req.getParameter("svcQty_" + svcsToAdd[i])) == null || strQty.equals(""))
				{
					Log.printVerbose("*** strQty = null ***");
					qtyToAdd = new BigDecimal(1);
				} else
				{
					Log.printVerbose("*** strQty = " + strQty + "***");
					qtyToAdd = new BigDecimal(strQty);
				}
				Log.printDebug("*** qtyToAdd = " + qtyToAdd);
				// create the package-item link
				PackageServiceHome lPkgSvcHome = PackageServiceNut.getHome();
				PackageService lPkgSvc = lPkgSvcHome.create(new Integer(packageId), lSvcId, qtyToAdd, "", tsCreate,
						usrid);
				// populate all successful additions
				if (lPkgSvc != null)
				{
					vecSvcsAdded.add(svcsToAdd[i]);
				}
			}
			// if it reached here, it's probably added the svcs successfully
			String rtnMsg = "Successfully added the following services to package " + lPkgCode;
			// req.setAttribute("packageId", packageId);
			req.setAttribute("packageCode", lPkgCode);
			req.setAttribute("vecSvcsAdded", vecSvcsAdded);
			req.setAttribute("rtnMsg", rtnMsg);
		} catch (Exception ex)
		{
			String rtnMsg = "Failed to Add Svcs to Package " + lPkgCode + ". Reason = " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
			Boolean error = new Boolean(true);
			req.setAttribute("error", error);
		}
	} // fnAddSvcToPkg

	private String debugVector(Vector vec)
	{
		String debugStr = "";
		for (Iterator itr = vec.iterator(); itr.hasNext();)
		{
			debugStr += itr.next().toString() + "\n";
		}
		return debugStr;
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
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoPOSAddSvcToPkg implements Action
{
	private String strClassName = "DoPOSAddSvcToPkg";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = (String) req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addSvcToPkg") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "addSvcToPkg");
				fnAddSvcToPkg(servlet, req, res);
				return new ActionRouter("cust-pos-added-svc-to-pkg-page");
			}
			if (formName.compareTo("showAddServices") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "showAddServices");
				fnGetAllInvSvcs(servlet, req, res);
			}
		}
		return new ActionRouter("cust-pos-add-svc-to-pkg-page");
	}

	protected void fnGetAllInvSvcs(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetAllInvSvcs()";
		// propagate the packageId to the receiving JSP
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		req.setAttribute("packageId", packageId);
		req.setAttribute("packageCode", packageCode);
		try
		{
			ServiceHome lSvcHome = (ServiceHome) ServiceNut.getHome();
			if (lSvcHome != null)
			{
				String lSortBy = ServiceBean.CODE;
				Vector vecSvcCodes = new Vector();
				Vector vecSvcNames = new Vector();
				Collection colSvcObj = (Collection) lSvcHome.getAllCodeName(lSortBy);
				/*
				 * if (colPkg != null) { for (Iterator itr = colPkg.iterator();
				 * itr.hasNext();) { com.vlee.ejb.customer.Service lCustAcc =
				 * (com.vlee.ejb.customer.Service) itr.next();
				 * vecSvcCodes.add(lCustAcc.getCode());
				 * vecSvcNames.add(lCustAcc.getName()); }
				 *  }
				 */
				// proof of concept, split colSvcObj into vecSvcCode and
				// vecSvcName
				for (Iterator itemObjItr = colSvcObj.iterator(); itemObjItr.hasNext();)
				{
					ServiceObject lSvcObj = (ServiceObject) itemObjItr.next();
					vecSvcCodes.add(lSvcObj.getCode());
					vecSvcNames.add(lSvcObj.getName());
				}
				if (vecSvcCodes != null && vecSvcNames != null)
				{
					Log.printVerbose(strClassName + ":" + funcName + " - vecSvcCodes = " + debugVector(vecSvcCodes));
					Log.printVerbose(strClassName + ":" + funcName + " - vecSvcNames = " + debugVector(vecSvcNames));
					req.setAttribute("vecSvcCodes", vecSvcCodes);
					req.setAttribute("vecSvcNames", vecSvcNames);
				}
			}
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
			return;
		}
	}

	protected void fnAddSvcToPkg(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSvcToPkg()";
		// Get the required parameters
		String[] svcsToAdd = (String[]) req.getParameterValues("svcsToAdd");
		// String[] svcQuantities = (String[]) req.getParameterValues("svcQty");
		String packageId = (String) req.getParameter("packageId");
		String lPkgCode = (String) req.getParameter("packageCode");
		Log.printVerbose("*** Size of svcsToAdd = " + svcsToAdd.length + "***");
		// Log.printVerbose("*** Size of svcQuantities = " +
		// svcQuantities.length + "***");
		try
		{
			if (svcsToAdd == null || packageId == null || lPkgCode == null)
			{
				String rtnMsg = "Null svcsToAdd or packageId";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			// Now start the adding ....
			Vector vecSvcsAdded = new Vector();
			String strQty;
			BigDecimal qtyToAdd;
			for (int i = 0; i < svcsToAdd.length; i++)
			{
				// Obtain the itemId to add
				Service lSvc = ServiceNut.getObjectByCode(svcsToAdd[i]);
				Integer lSvcId = lSvc.getPkid();
				// Obtain the quantity to add
				if ((strQty = req.getParameter("svcQty_" + svcsToAdd[i])) == null || strQty.equals(""))
				{
					Log.printVerbose("*** strQty = null ***");
					qtyToAdd = new BigDecimal(1);
				} else
				{
					Log.printVerbose("*** strQty = " + strQty + "***");
					qtyToAdd = new BigDecimal(strQty);
				}
				Log.printDebug("*** qtyToAdd = " + qtyToAdd);
				// create the package-item link
				PackageServiceHome lPkgSvcHome = PackageServiceNut.getHome();
				PackageService lPkgSvc = lPkgSvcHome.create(new Integer(packageId), lSvcId, qtyToAdd, "", tsCreate,
						usrid);
				// populate all successful additions
				if (lPkgSvc != null)
				{
					vecSvcsAdded.add(svcsToAdd[i]);
				}
			}
			// if it reached here, it's probably added the svcs successfully
			String rtnMsg = "Successfully added the following services to package " + lPkgCode;
			// req.setAttribute("packageId", packageId);
			req.setAttribute("packageCode", lPkgCode);
			req.setAttribute("vecSvcsAdded", vecSvcsAdded);
			req.setAttribute("rtnMsg", rtnMsg);
		} catch (Exception ex)
		{
			String rtnMsg = "Failed to Add Svcs to Package " + lPkgCode + ". Reason = " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
			Boolean error = new Boolean(true);
			req.setAttribute("error", error);
		}
	} // fnAddSvcToPkg

	private String debugVector(Vector vec)
	{
		String debugStr = "";
		for (Iterator itr = vec.iterator(); itr.hasNext();)
		{
			debugStr += itr.next().toString() + "\n";
		}
		return debugStr;
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
package com.vlee.servlet.customer;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;

public class DoPOSAddSvcToPkg implements Action
{
	private String strClassName = "DoPOSAddSvcToPkg";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Get the formName
		String formName = (String) req.getParameter("formName");
		if (formName != null)
		{
			if (formName.compareTo("addSvcToPkg") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "addSvcToPkg");
				fnAddSvcToPkg(servlet, req, res);
				return new ActionRouter("cust-pos-added-svc-to-pkg-page");
			}
			if (formName.compareTo("showAddServices") == 0)
			{
				// Propagate the formName back
				// req.setAttribute("formName", "showAddServices");
				fnGetAllInvSvcs(servlet, req, res);
			}
		}
		return new ActionRouter("cust-pos-add-svc-to-pkg-page");
	}

	protected void fnGetAllInvSvcs(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetAllInvSvcs()";
		// propagate the packageId to the receiving JSP
		String packageId = req.getParameter("packageId");
		String packageCode = req.getParameter("packageCode");
		req.setAttribute("packageId", packageId);
		req.setAttribute("packageCode", packageCode);
		try
		{
			ServiceHome lSvcHome = (ServiceHome) ServiceNut.getHome();
			if (lSvcHome != null)
			{
				String lSortBy = ServiceBean.CODE;
				Vector vecSvcCodes = new Vector();
				Vector vecSvcNames = new Vector();
				Collection colSvcObj = (Collection) lSvcHome.getAllCodeName(lSortBy);
				/*
				 * if (colPkg != null) { for (Iterator itr = colPkg.iterator();
				 * itr.hasNext();) { com.vlee.ejb.customer.Service lCustAcc =
				 * (com.vlee.ejb.customer.Service) itr.next();
				 * vecSvcCodes.add(lCustAcc.getCode());
				 * vecSvcNames.add(lCustAcc.getName()); }
				 *  }
				 */
				// proof of concept, split colSvcObj into vecSvcCode and
				// vecSvcName
				for (Iterator itemObjItr = colSvcObj.iterator(); itemObjItr.hasNext();)
				{
					ServiceObject lSvcObj = (ServiceObject) itemObjItr.next();
					vecSvcCodes.add(lSvcObj.getCode());
					vecSvcNames.add(lSvcObj.getName());
				}
				if (vecSvcCodes != null && vecSvcNames != null)
				{
					Log.printVerbose(strClassName + ":" + funcName + " - vecSvcCodes = " + debugVector(vecSvcCodes));
					Log.printVerbose(strClassName + ":" + funcName + " - vecSvcNames = " + debugVector(vecSvcNames));
					req.setAttribute("vecSvcCodes", vecSvcCodes);
					req.setAttribute("vecSvcNames", vecSvcNames);
				}
			}
		} catch (Exception ex)
		{
			Log.printDebug(strClassName + ":" + funcName + " - " + ex.getMessage());
			return;
		}
	}

	protected void fnAddSvcToPkg(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddSvcToPkg()";
		// Get the required parameters
		String[] svcsToAdd = (String[]) req.getParameterValues("svcsToAdd");
		// String[] svcQuantities = (String[]) req.getParameterValues("svcQty");
		String packageId = (String) req.getParameter("packageId");
		String lPkgCode = (String) req.getParameter("packageCode");
		Log.printVerbose("*** Size of svcsToAdd = " + svcsToAdd.length + "***");
		// Log.printVerbose("*** Size of svcQuantities = " +
		// svcQuantities.length + "***");
		try
		{
			if (svcsToAdd == null || packageId == null || lPkgCode == null)
			{
				String rtnMsg = "Null svcsToAdd or packageId";
				throw new Exception(rtnMsg);
			}
			HttpSession session = req.getSession();
			User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
				throw new Exception(
						"Invalid Username. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
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
			// Now start the adding ....
			Vector vecSvcsAdded = new Vector();
			String strQty;
			BigDecimal qtyToAdd;
			for (int i = 0; i < svcsToAdd.length; i++)
			{
				// Obtain the itemId to add
				Service lSvc = ServiceNut.getObjectByCode(svcsToAdd[i]);
				Integer lSvcId = lSvc.getPkid();
				// Obtain the quantity to add
				if ((strQty = req.getParameter("svcQty_" + svcsToAdd[i])) == null || strQty.equals(""))
				{
					Log.printVerbose("*** strQty = null ***");
					qtyToAdd = new BigDecimal(1);
				} else
				{
					Log.printVerbose("*** strQty = " + strQty + "***");
					qtyToAdd = new BigDecimal(strQty);
				}
				Log.printDebug("*** qtyToAdd = " + qtyToAdd);
				// create the package-item link
				PackageServiceHome lPkgSvcHome = PackageServiceNut.getHome();
				PackageService lPkgSvc = lPkgSvcHome.create(new Integer(packageId), lSvcId, qtyToAdd, "", tsCreate,
						usrid);
				// populate all successful additions
				if (lPkgSvc != null)
				{
					vecSvcsAdded.add(svcsToAdd[i]);
				}
			}
			// if it reached here, it's probably added the svcs successfully
			String rtnMsg = "Successfully added the following services to package " + lPkgCode;
			// req.setAttribute("packageId", packageId);
			req.setAttribute("packageCode", lPkgCode);
			req.setAttribute("vecSvcsAdded", vecSvcsAdded);
			req.setAttribute("rtnMsg", rtnMsg);
		} catch (Exception ex)
		{
			String rtnMsg = "Failed to Add Svcs to Package " + lPkgCode + ". Reason = " + ex.getMessage();
			Log.printDebug(rtnMsg);
			req.setAttribute("rtnMsg", rtnMsg);
			Boolean error = new Boolean(true);
			req.setAttribute("error", error);
		}
	} // fnAddSvcToPkg

	private String debugVector(Vector vec)
	{
		String debugStr = "";
		for (Iterator itr = vec.iterator(); itr.hasNext();)
		{
			debugStr += itr.next().toString() + "\n";
		}
		return debugStr;
	}
}
