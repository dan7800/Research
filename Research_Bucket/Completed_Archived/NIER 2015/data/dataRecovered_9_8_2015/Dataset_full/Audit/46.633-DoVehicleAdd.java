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

public class DoVehicleAdd implements Action
{
	String strClassName = "DoVehicleAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String custAccId = req.getParameter("custAccId");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		if (custAccId == null)
		{
			Log.printDebug("NULL custAccId !!");
		}
		if (vehicleRegNum != null)
		{
			// add vehicle
			fnAddVehicle(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("cust-setup-added-vehicle-page");
		}
		fnGetParams(servlet, req, res);
		// propagate custAccId
		// String custAccId = req.getParameter("custAccId");
		// req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-add-vehicle-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
	}

	protected void fnAddVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddVehicle()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		String vehicleManufacturer = req.getParameter("vehicleManufacturer");
		String vehicleModel = req.getParameter("vehicleModel");
		String vehicleEngine = req.getParameter("vehicleEngine");
		String vehicleChassisNum = req.getParameter("vehicleChassisNum");
		String vehicleDesc = req.getParameter("vehicleDesc");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL Vehicle Name");
				// return;
				throw new Exception(
						"Invalid Vehiclename. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * if (custAccId == null) { //return; throw new Exception("Invalid
			 * Customer Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - custAccId = " + custAccId); if (vehicleName ==
			 * null) { //return; throw new Exception("Invalid Customer Vehicle
			 * Name"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd = " + vehiclePasswd); if (vehiclePasswd == null) {
			 * //return; throw new Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd = " + vehiclePasswd); if (vehiclePasswd2 == null) {
			 * //return; throw new Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd2 = " + vehiclePasswd2);
			 */
			Log.printVerbose("Adding new Vehicle");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Add the vehicle, ensure it doesn't already exist
			Vehicle lVehicle = VehicleNut.getObjectByRegNum(vehicleRegNum);
			if (lVehicle == null)
			{
				// 1. Create the Vehicle object
				// CustAccount custAcc =
				// CustAccountNut.getObjectByCode(custAccCode);
				// Integer custAccId = custAcc.getPkid();
				VehicleHome vehicleHome = VehicleNut.getHome();
				Vehicle newVehicle = vehicleHome.create(vehicleRegNum, vehicleManufacturer, vehicleDesc, vehicleModel,
						vehicleEngine, vehicleChassisNum, new Integer(0), // TO_DO:
																			// insert
																			// real
																			// category
																			// later
						new Integer(custAccId), tsCreate, usrid);
				// If we reach this stage, we're successful !
				// populate the "addVehicle" attribute so re-display the added
				// fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("addVehicle", newVehicle);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add Vehicle for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
			req.setAttribute("addVehicleErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddVehicle
} // end class DoVehicleAdd
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

public class DoVehicleAdd implements Action
{
	String strClassName = "DoVehicleAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String custAccId = req.getParameter("custAccId");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		if (custAccId == null)
		{
			Log.printDebug("NULL custAccId !!");
		}
		if (vehicleRegNum != null)
		{
			// add vehicle
			fnAddVehicle(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("cust-setup-added-vehicle-page");
		}
		fnGetParams(servlet, req, res);
		// propagate custAccId
		// String custAccId = req.getParameter("custAccId");
		// req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-add-vehicle-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
	}

	protected void fnAddVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddVehicle()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		String vehicleManufacturer = req.getParameter("vehicleManufacturer");
		String vehicleModel = req.getParameter("vehicleModel");
		String vehicleEngine = req.getParameter("vehicleEngine");
		String vehicleChassisNum = req.getParameter("vehicleChassisNum");
		String vehicleDesc = req.getParameter("vehicleDesc");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL Vehicle Name");
				// return;
				throw new Exception(
						"Invalid Vehiclename. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * if (custAccId == null) { //return; throw new Exception("Invalid
			 * Customer Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - custAccId = " + custAccId); if (vehicleName ==
			 * null) { //return; throw new Exception("Invalid Customer Vehicle
			 * Name"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd = " + vehiclePasswd); if (vehiclePasswd == null) {
			 * //return; throw new Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd = " + vehiclePasswd); if (vehiclePasswd2 == null) {
			 * //return; throw new Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd2 = " + vehiclePasswd2);
			 */
			Log.printVerbose("Adding new Vehicle");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Add the vehicle, ensure it doesn't already exist
			Vehicle lVehicle = VehicleNut.getObjectByRegNum(vehicleRegNum);
			if (lVehicle == null)
			{
				// 1. Create the Vehicle object
				// CustAccount custAcc =
				// CustAccountNut.getObjectByCode(custAccCode);
				// Integer custAccId = custAcc.getPkid();
				VehicleHome vehicleHome = VehicleNut.getHome();
				Vehicle newVehicle = vehicleHome.create(vehicleRegNum, vehicleManufacturer, vehicleDesc, vehicleModel,
						vehicleEngine, vehicleChassisNum, new Integer(0), // TO_DO:
																			// insert
																			// real
																			// category
																			// later
						new Integer(custAccId), tsCreate, usrid);
				// If we reach this stage, we're successful !
				// populate the "addVehicle" attribute so re-display the added
				// fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("addVehicle", newVehicle);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add Vehicle for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
			req.setAttribute("addVehicleErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddVehicle
} // end class DoVehicleAdd
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

public class DoVehicleAdd implements Action
{
	String strClassName = "DoVehicleAdd";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String custAccId = req.getParameter("custAccId");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		if (custAccId == null)
		{
			Log.printDebug("NULL custAccId !!");
		}
		if (vehicleRegNum != null)
		{
			// add vehicle
			fnAddVehicle(servlet, req, res);
			fnGetParams(servlet, req, res);
			return new ActionRouter("cust-setup-added-vehicle-page");
		}
		fnGetParams(servlet, req, res);
		// propagate custAccId
		// String custAccId = req.getParameter("custAccId");
		// req.setAttribute("custAccId", custAccId);
		return new ActionRouter("cust-setup-add-vehicle-page");
	}

	protected void fnGetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String custAccId = req.getParameter("custAccId");
		req.setAttribute("custAccId", custAccId);
	}

	protected void fnAddVehicle(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddVehicle()";
		// Get the request paramaters
		String custAccId = req.getParameter("custAccId");
		String vehicleRegNum = req.getParameter("vehicleRegNum");
		String vehicleManufacturer = req.getParameter("vehicleManufacturer");
		String vehicleModel = req.getParameter("vehicleModel");
		String vehicleEngine = req.getParameter("vehicleEngine");
		String vehicleChassisNum = req.getParameter("vehicleChassisNum");
		String vehicleDesc = req.getParameter("vehicleDesc");
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		try
		{
			if (lUsr == null)
			{
				Log.printDebug(strClassName + ": " + "WARNING - NULL Vehicle Name");
				// return;
				throw new Exception(
						"Invalid Vehiclename. Your session may have expired or you may not be authorised to access the system. Please login again");
			}
			/*
			 * if (custAccId == null) { //return; throw new Exception("Invalid
			 * Customer Account"); } else Log.printVerbose(strClassName + ":" +
			 * funcName + " - custAccId = " + custAccId); if (vehicleName ==
			 * null) { //return; throw new Exception("Invalid Customer Vehicle
			 * Name"); } else Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd = " + vehiclePasswd); if (vehiclePasswd == null) {
			 * //return; throw new Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd = " + vehiclePasswd); if (vehiclePasswd2 == null) {
			 * //return; throw new Exception("Invalid Password entered"); } else
			 * Log.printVerbose(strClassName + ":" + funcName + " -
			 * vehiclePasswd2 = " + vehiclePasswd2);
			 */
			Log.printVerbose("Adding new Vehicle");
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			// Add the vehicle, ensure it doesn't already exist
			Vehicle lVehicle = VehicleNut.getObjectByRegNum(vehicleRegNum);
			if (lVehicle == null)
			{
				// 1. Create the Vehicle object
				// CustAccount custAcc =
				// CustAccountNut.getObjectByCode(custAccCode);
				// Integer custAccId = custAcc.getPkid();
				VehicleHome vehicleHome = VehicleNut.getHome();
				Vehicle newVehicle = vehicleHome.create(vehicleRegNum, vehicleManufacturer, vehicleDesc, vehicleModel,
						vehicleEngine, vehicleChassisNum, new Integer(0), // TO_DO:
																			// insert
																			// real
																			// category
																			// later
						new Integer(custAccId), tsCreate, usrid);
				// If we reach this stage, we're successful !
				// populate the "addVehicle" attribute so re-display the added
				// fields
				// req.setAttribute("custAccId", custAccId);
				req.setAttribute("addVehicle", newVehicle);
			} // end if
		} catch (Exception ex)
		{
			Log.printDebug("Add Vehicle for CustAccId = " + custAccId + " -  Failed" + ex.getMessage());
			req.setAttribute("addVehicleErrMsg", ex.getMessage());
		} // end try-catch
	} // end fnAddVehicle
} // end class DoVehicleAdd
