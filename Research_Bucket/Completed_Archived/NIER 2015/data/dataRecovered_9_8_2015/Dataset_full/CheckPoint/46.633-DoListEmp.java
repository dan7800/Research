package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoListEmp implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		EmpDetailsHome lEmpDetailsHome = EmpDetailsNut.getHome();
		Collection colRI = null;
		try
		{
			colRI = lEmpDetailsHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
		}
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-employee-page");
	}
}
package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoListEmp implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		EmpDetailsHome lEmpDetailsHome = EmpDetailsNut.getHome();
		Collection colRI = null;
		try
		{
			colRI = lEmpDetailsHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
		}
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-employee-page");
	}
}
package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoListEmp implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		EmpDetailsHome lEmpDetailsHome = EmpDetailsNut.getHome();
		Collection colRI = null;
		try
		{
			colRI = lEmpDetailsHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
		}
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-employee-page");
	}
}
package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoListEmp implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		EmpDetailsHome lEmpDetailsHome = EmpDetailsNut.getHome();
		Collection colRI = null;
		try
		{
			colRI = lEmpDetailsHome.findAllObjects();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
		}
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-employee-page");
	}
}
