package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;

public class DoInvStoreUpdate implements Action
{
	String strClassName = "DoInvStoreUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update location
		fnUpdateLocation(servlet, req, res);
		return new ActionRouter("inv-redirect-setup-edit-store-page");
	}

	protected void fnUpdateLocation(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateLocation()";
		// Get the request paramaters (for Location)
		String locationCode = req.getParameter("locationCode");
		String locationName = req.getParameter("locationName");
		String locationDesc = req.getParameter("locationDesc");
		// Get the request parameters (for LocAddr)
		String locAddrAddr1 = req.getParameter("locAddrAddr1");
		String locAddrAddr2 = req.getParameter("locAddrAddr2");
		String locAddrAddr3 = req.getParameter("locAddrAddr3");
		String locAddrZip = req.getParameter("locAddrZip");
		String locAddrState = req.getParameter("locAddrState");
		String locAddrCountryCode = req.getParameter("locAddrCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (locationCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationCode = " + locationCode);
		if (locationName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationName = " + locationName);
		if (locationDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationDesc = " + locationDesc);
		if (locationCode != null)
		{
			Location lLocation = LocationNut.getObjectByCode(locationCode);
			if (lLocation != null)
			{
				try
				{
					lLocation.setLocationCode(locationCode);
					lLocation.setName(locationName);
					lLocation.setDescription(locationDesc);
					// and then update the lastModified and userIdUpdate fields
					lLocation.setLastUpdate(tsCreate);
					lLocation.setUserIdUpdate(usrid);
					// update the address portion
					LocAddr lLocAddr = LocAddrNut.getHandle(lLocation.getAddrId());
					if (lLocAddr != null)
					{
						lLocAddr.setAddr1(locAddrAddr1);
						lLocAddr.setAddr2(locAddrAddr2);
						lLocAddr.setAddr3(locAddrAddr3);
						lLocAddr.setZip(locAddrZip);
						lLocAddr.setState(locAddrState);
						lLocAddr.setCountryCode(locAddrCountryCode);
					}
				} catch (Exception ex)
				{
					Log.printDebug("Update Location: " + locationCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateLocation
} // end class DoInvStoreUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;

public class DoInvStoreUpdate implements Action
{
	String strClassName = "DoInvStoreUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update location
		fnUpdateLocation(servlet, req, res);
		return new ActionRouter("inv-redirect-setup-edit-store-page");
	}

	protected void fnUpdateLocation(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateLocation()";
		// Get the request paramaters (for Location)
		String locationCode = req.getParameter("locationCode");
		String locationName = req.getParameter("locationName");
		String locationDesc = req.getParameter("locationDesc");
		// Get the request parameters (for LocAddr)
		String locAddrAddr1 = req.getParameter("locAddrAddr1");
		String locAddrAddr2 = req.getParameter("locAddrAddr2");
		String locAddrAddr3 = req.getParameter("locAddrAddr3");
		String locAddrZip = req.getParameter("locAddrZip");
		String locAddrState = req.getParameter("locAddrState");
		String locAddrCountryCode = req.getParameter("locAddrCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (locationCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationCode = " + locationCode);
		if (locationName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationName = " + locationName);
		if (locationDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationDesc = " + locationDesc);
		if (locationCode != null)
		{
			Location lLocation = LocationNut.getObjectByCode(locationCode);
			if (lLocation != null)
			{
				try
				{
					lLocation.setLocationCode(locationCode);
					lLocation.setName(locationName);
					lLocation.setDescription(locationDesc);
					// and then update the lastModified and userIdUpdate fields
					lLocation.setLastUpdate(tsCreate);
					lLocation.setUserIdUpdate(usrid);
					// update the address portion
					LocAddr lLocAddr = LocAddrNut.getHandle(lLocation.getAddrId());
					if (lLocAddr != null)
					{
						lLocAddr.setAddr1(locAddrAddr1);
						lLocAddr.setAddr2(locAddrAddr2);
						lLocAddr.setAddr3(locAddrAddr3);
						lLocAddr.setZip(locAddrZip);
						lLocAddr.setState(locAddrState);
						lLocAddr.setCountryCode(locAddrCountryCode);
					}
				} catch (Exception ex)
				{
					Log.printDebug("Update Location: " + locationCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateLocation
} // end class DoInvStoreUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;

public class DoInvStoreUpdate implements Action
{
	String strClassName = "DoInvStoreUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update location
		fnUpdateLocation(servlet, req, res);
		return new ActionRouter("inv-redirect-setup-edit-store-page");
	}

	protected void fnUpdateLocation(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateLocation()";
		// Get the request paramaters (for Location)
		String locationCode = req.getParameter("locationCode");
		String locationName = req.getParameter("locationName");
		String locationDesc = req.getParameter("locationDesc");
		// Get the request parameters (for LocAddr)
		String locAddrAddr1 = req.getParameter("locAddrAddr1");
		String locAddrAddr2 = req.getParameter("locAddrAddr2");
		String locAddrAddr3 = req.getParameter("locAddrAddr3");
		String locAddrZip = req.getParameter("locAddrZip");
		String locAddrState = req.getParameter("locAddrState");
		String locAddrCountryCode = req.getParameter("locAddrCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (locationCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationCode = " + locationCode);
		if (locationName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationName = " + locationName);
		if (locationDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationDesc = " + locationDesc);
		if (locationCode != null)
		{
			Location lLocation = LocationNut.getObjectByCode(locationCode);
			if (lLocation != null)
			{
				try
				{
					lLocation.setLocationCode(locationCode);
					lLocation.setName(locationName);
					lLocation.setDescription(locationDesc);
					// and then update the lastModified and userIdUpdate fields
					lLocation.setLastUpdate(tsCreate);
					lLocation.setUserIdUpdate(usrid);
					// update the address portion
					LocAddr lLocAddr = LocAddrNut.getHandle(lLocation.getAddrId());
					if (lLocAddr != null)
					{
						lLocAddr.setAddr1(locAddrAddr1);
						lLocAddr.setAddr2(locAddrAddr2);
						lLocAddr.setAddr3(locAddrAddr3);
						lLocAddr.setZip(locAddrZip);
						lLocAddr.setState(locAddrState);
						lLocAddr.setCountryCode(locAddrCountryCode);
					}
				} catch (Exception ex)
				{
					Log.printDebug("Update Location: " + locationCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateLocation
} // end class DoInvStoreUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;

public class DoInvStoreUpdate implements Action
{
	String strClassName = "DoInvStoreUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update location
		fnUpdateLocation(servlet, req, res);
		return new ActionRouter("inv-redirect-setup-edit-store-page");
	}

	protected void fnUpdateLocation(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateLocation()";
		// Get the request paramaters (for Location)
		String locationCode = req.getParameter("locationCode");
		String locationName = req.getParameter("locationName");
		String locationDesc = req.getParameter("locationDesc");
		// Get the request parameters (for LocAddr)
		String locAddrAddr1 = req.getParameter("locAddrAddr1");
		String locAddrAddr2 = req.getParameter("locAddrAddr2");
		String locAddrAddr3 = req.getParameter("locAddrAddr3");
		String locAddrZip = req.getParameter("locAddrZip");
		String locAddrState = req.getParameter("locAddrState");
		String locAddrCountryCode = req.getParameter("locAddrCountryCode");
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (locationCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationCode = " + locationCode);
		if (locationName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationName = " + locationName);
		if (locationDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - locationDesc = " + locationDesc);
		if (locationCode != null)
		{
			Location lLocation = LocationNut.getObjectByCode(locationCode);
			if (lLocation != null)
			{
				try
				{
					lLocation.setLocationCode(locationCode);
					lLocation.setName(locationName);
					lLocation.setDescription(locationDesc);
					// and then update the lastModified and userIdUpdate fields
					lLocation.setLastUpdate(tsCreate);
					lLocation.setUserIdUpdate(usrid);
					// update the address portion
					LocAddr lLocAddr = LocAddrNut.getHandle(lLocation.getAddrId());
					if (lLocAddr != null)
					{
						lLocAddr.setAddr1(locAddrAddr1);
						lLocAddr.setAddr2(locAddrAddr2);
						lLocAddr.setAddr3(locAddrAddr3);
						lLocAddr.setZip(locAddrZip);
						lLocAddr.setState(locAddrState);
						lLocAddr.setCountryCode(locAddrCountryCode);
					}
				} catch (Exception ex)
				{
					Log.printDebug("Update Location: " + locationCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateLocation
} // end class DoInvStoreUpdate
