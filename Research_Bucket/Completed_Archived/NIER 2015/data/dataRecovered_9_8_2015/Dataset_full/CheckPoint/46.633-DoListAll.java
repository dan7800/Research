package com.vlee.servlet.employee;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.employee.*;

public class DoListAll implements Action
{
	private String strClassName = "DoListAll";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		RemunerationItemHome lRIHome = RemunerationItemNut.getHome();
		Log.printVerbose("Pass Here next");
		Collection colRI = null;
		try
		{
			Log.printVerbose("checkpoint 1");
			colRI = lRIHome.findAllObjects();
			Log.printVerbose("checkpoint 2");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
			Log.printDebug("hhhhhhhherrrrrrrrrrrr");
		}
		Log.printVerbose("checkpoint 4");
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-all-item-page");
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

public class DoListAll implements Action
{
	private String strClassName = "DoListAll";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		RemunerationItemHome lRIHome = RemunerationItemNut.getHome();
		Log.printVerbose("Pass Here next");
		Collection colRI = null;
		try
		{
			Log.printVerbose("checkpoint 1");
			colRI = lRIHome.findAllObjects();
			Log.printVerbose("checkpoint 2");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
			Log.printDebug("hhhhhhhherrrrrrrrrrrr");
		}
		Log.printVerbose("checkpoint 4");
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-all-item-page");
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

public class DoListAll implements Action
{
	private String strClassName = "DoListAll";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		RemunerationItemHome lRIHome = RemunerationItemNut.getHome();
		Log.printVerbose("Pass Here next");
		Collection colRI = null;
		try
		{
			Log.printVerbose("checkpoint 1");
			colRI = lRIHome.findAllObjects();
			Log.printVerbose("checkpoint 2");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
			Log.printDebug("hhhhhhhherrrrrrrrrrrr");
		}
		Log.printVerbose("checkpoint 4");
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-all-item-page");
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

public class DoListAll implements Action
{
	private String strClassName = "DoListAll";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// list all
		RemunerationItemHome lRIHome = RemunerationItemNut.getHome();
		Log.printVerbose("Pass Here next");
		Collection colRI = null;
		try
		{
			Log.printVerbose("checkpoint 1");
			colRI = lRIHome.findAllObjects();
			Log.printVerbose("checkpoint 2");
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose("checkpoint 3");
			Log.printDebug("err msg " + ex.getMessage());
			Log.printDebug("hhhhhhhherrrrrrrrrrrr");
		}
		Log.printVerbose("checkpoint 4");
		Iterator itrRI = colRI.iterator();
		req.setAttribute("itrRI", itrRI);
		return new ActionRouter("erm-list-all-item-page");
	}
}
