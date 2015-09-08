package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class DoVehicleUpdate implements Action
{
	String strClassName = "DoVehicleUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update vehicle
		fnUpdateVehicle(servlet, req, res);
		// propagate custAccId
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-updated-vehicle-page");
	}

	protected void fnUpdateVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateVehicle()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String vehiclePkid = req.getParameter("vehiclePkid");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		String vehicleManufacturer = req.getParameter("vehicleManufacturer");
		String vehicleModel = req.getParameter("vehicleModel");
		String vehicleEngine = req.getParameter("vehicleEngine");
		String vehicleChassisNum = req.getParameter("vehicleChassisNum");
		String vehicleDesc = req.getParameter("vehicleDesc");
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
		if (custAccId == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccId = " + custAccId);
		if (vehiclePkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehiclePkid = " + vehiclePkid);
		if (vehicleRegNum == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleRegNum = " + vehicleRegNum);
		if (vehicleManufacturer == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleManufacturer = " + vehicleManufacturer);
		if (vehicleModel == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleModel = " + vehicleModel);
		if (vehicleEngine == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleEngine = " + vehicleEngine);
		if (vehicleChassisNum == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleChassisNum = " + vehicleChassisNum);
		if (vehicleDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleDesc = " + vehicleDesc);
		try
		{
			// Get the custAccId first
			// CustAccount custAcc =
			// CustAccountNut.getObjectByCode(custAccCode);
			// Integer custAccId = custAcc.getPkid();
			Vehicle lVehicle = VehicleNut.getHandle(new Integer(vehiclePkid));
			if (lVehicle != null)
			{
				lVehicle.setRegNum(vehicleRegNum);
				lVehicle.setManufacturer(vehicleManufacturer);
				lVehicle.setModel(vehicleModel);
				lVehicle.setEngine(vehicleEngine);
				lVehicle.setChassisNum(vehicleChassisNum);
				lVehicle.setDescription(vehicleDesc);
				// and then update the lastModified and userIdUpdate fields
				lVehicle.setLastUpdate(tsCreate);
				lVehicle.setUserIdUpdate(usrid);
				// populate the "editVehicle" attribute so re-display the edited
				// fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("editVehicle", lVehicle);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update Vehicle for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateVehicle
} // end class DoVehicleUpdate
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class DoVehicleUpdate implements Action
{
	String strClassName = "DoVehicleUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update vehicle
		fnUpdateVehicle(servlet, req, res);
		// propagate custAccId
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-updated-vehicle-page");
	}

	protected void fnUpdateVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateVehicle()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String vehiclePkid = req.getParameter("vehiclePkid");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		String vehicleManufacturer = req.getParameter("vehicleManufacturer");
		String vehicleModel = req.getParameter("vehicleModel");
		String vehicleEngine = req.getParameter("vehicleEngine");
		String vehicleChassisNum = req.getParameter("vehicleChassisNum");
		String vehicleDesc = req.getParameter("vehicleDesc");
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
		if (custAccId == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccId = " + custAccId);
		if (vehiclePkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehiclePkid = " + vehiclePkid);
		if (vehicleRegNum == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleRegNum = " + vehicleRegNum);
		if (vehicleManufacturer == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleManufacturer = " + vehicleManufacturer);
		if (vehicleModel == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleModel = " + vehicleModel);
		if (vehicleEngine == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleEngine = " + vehicleEngine);
		if (vehicleChassisNum == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleChassisNum = " + vehicleChassisNum);
		if (vehicleDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleDesc = " + vehicleDesc);
		try
		{
			// Get the custAccId first
			// CustAccount custAcc =
			// CustAccountNut.getObjectByCode(custAccCode);
			// Integer custAccId = custAcc.getPkid();
			Vehicle lVehicle = VehicleNut.getHandle(new Integer(vehiclePkid));
			if (lVehicle != null)
			{
				lVehicle.setRegNum(vehicleRegNum);
				lVehicle.setManufacturer(vehicleManufacturer);
				lVehicle.setModel(vehicleModel);
				lVehicle.setEngine(vehicleEngine);
				lVehicle.setChassisNum(vehicleChassisNum);
				lVehicle.setDescription(vehicleDesc);
				// and then update the lastModified and userIdUpdate fields
				lVehicle.setLastUpdate(tsCreate);
				lVehicle.setUserIdUpdate(usrid);
				// populate the "editVehicle" attribute so re-display the edited
				// fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("editVehicle", lVehicle);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update Vehicle for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateVehicle
} // end class DoVehicleUpdate
package com.vlee.servlet.customer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;

public class DoVehicleUpdate implements Action
{
	String strClassName = "DoVehicleUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update vehicle
		fnUpdateVehicle(servlet, req, res);
		// propagate custAccId
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-updated-vehicle-page");
	}

	protected void fnUpdateVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateVehicle()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String vehiclePkid = req.getParameter("vehiclePkid");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		String vehicleManufacturer = req.getParameter("vehicleManufacturer");
		String vehicleModel = req.getParameter("vehicleModel");
		String vehicleEngine = req.getParameter("vehicleEngine");
		String vehicleChassisNum = req.getParameter("vehicleChassisNum");
		String vehicleDesc = req.getParameter("vehicleDesc");
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
		if (custAccId == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - custAccId = " + custAccId);
		if (vehiclePkid == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehiclePkid = " + vehiclePkid);
		if (vehicleRegNum == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleRegNum = " + vehicleRegNum);
		if (vehicleManufacturer == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleManufacturer = " + vehicleManufacturer);
		if (vehicleModel == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleModel = " + vehicleModel);
		if (vehicleEngine == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleEngine = " + vehicleEngine);
		if (vehicleChassisNum == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleChassisNum = " + vehicleChassisNum);
		if (vehicleDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - vehicleDesc = " + vehicleDesc);
		try
		{
			// Get the custAccId first
			// CustAccount custAcc =
			// CustAccountNut.getObjectByCode(custAccCode);
			// Integer custAccId = custAcc.getPkid();
			Vehicle lVehicle = VehicleNut.getHandle(new Integer(vehiclePkid));
			if (lVehicle != null)
			{
				lVehicle.setRegNum(vehicleRegNum);
				lVehicle.setManufacturer(vehicleManufacturer);
				lVehicle.setModel(vehicleModel);
				lVehicle.setEngine(vehicleEngine);
				lVehicle.setChassisNum(vehicleChassisNum);
				lVehicle.setDescription(vehicleDesc);
				// and then update the lastModified and userIdUpdate fields
				lVehicle.setLastUpdate(tsCreate);
				lVehicle.setUserIdUpdate(usrid);
				// populate the "editVehicle" attribute so re-display the edited
				// fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("editVehicle", lVehicle);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Update Vehicle for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
		} // end try-catch
	} // end fnUpdateVehicle
} // end class DoVehicleUpdate
