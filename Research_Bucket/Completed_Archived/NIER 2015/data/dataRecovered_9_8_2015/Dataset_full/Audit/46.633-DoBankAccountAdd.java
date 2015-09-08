package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBankAccountAdd implements Action
{
	private String strClassName = "DoBankAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-bankaccount-page");
		} else
		{
			fnAdd(servlet, request, response);
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-bankaccount-page");
		}
	}

	protected void fnPopulate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnPopulate()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = ProfitCostCenterNut.getAllObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String bankCode = trim(request.getParameter("bankCode"));
		String bankName = trim(request.getParameter("bankName"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String currency = request.getParameter("currency");
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String signatory5 = trim(request.getParameter("signatory5"));
		String pcCenter = trim(request.getParameter("pcCenter"));
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		HttpSession session = request.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + " WARNING - NULL userName");
		} else
		{
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception e)
			{
				Log.printAudit("User does not exist: " + e.getMessage());
			}
		}
		Timestamp tsCreate = TimeFormat.getTimestamp();
		byte[] signature = new String("not used").getBytes();
		try
		{
			BankAccountHome home = BankAccountNut.getHome();
			home.create(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
					signatory3, signatory4, signatory5, signature, new Integer(pcCenter), add1, add2, add3, state,
					country, phone, contactPerson, fax, usrid, usrid, tsCreate, tsCreate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBankAccountAdd implements Action
{
	private String strClassName = "DoBankAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-bankaccount-page");
		} else
		{
			fnAdd(servlet, request, response);
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-bankaccount-page");
		}
	}

	protected void fnPopulate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnPopulate()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = ProfitCostCenterNut.getAllObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String bankCode = trim(request.getParameter("bankCode"));
		String bankName = trim(request.getParameter("bankName"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String currency = request.getParameter("currency");
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String signatory5 = trim(request.getParameter("signatory5"));
		String pcCenter = trim(request.getParameter("pcCenter"));
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		HttpSession session = request.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + " WARNING - NULL userName");
		} else
		{
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception e)
			{
				Log.printAudit("User does not exist: " + e.getMessage());
			}
		}
		Timestamp tsCreate = TimeFormat.getTimestamp();
		byte[] signature = new String("not used").getBytes();
		try
		{
			BankAccountHome home = BankAccountNut.getHome();
			home.create(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
					signatory3, signatory4, signatory5, signature, new Integer(pcCenter), add1, add2, add3, state,
					country, phone, contactPerson, fax, usrid, usrid, tsCreate, tsCreate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBankAccountAdd implements Action
{
	private String strClassName = "DoBankAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-bankaccount-page");
		} else
		{
			fnAdd(servlet, request, response);
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-bankaccount-page");
		}
	}

	protected void fnPopulate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnPopulate()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = ProfitCostCenterNut.getAllObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String bankCode = trim(request.getParameter("bankCode"));
		String bankName = trim(request.getParameter("bankName"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String currency = request.getParameter("currency");
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String signatory5 = trim(request.getParameter("signatory5"));
		String pcCenter = trim(request.getParameter("pcCenter"));
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		HttpSession session = request.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + " WARNING - NULL userName");
		} else
		{
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception e)
			{
				Log.printAudit("User does not exist: " + e.getMessage());
			}
		}
		Timestamp tsCreate = TimeFormat.getTimestamp();
		byte[] signature = new String("not used").getBytes();
		try
		{
			BankAccountHome home = BankAccountNut.getHome();
			home.create(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
					signatory3, signatory4, signatory5, signature, new Integer(pcCenter), add1, add2, add3, state,
					country, phone, contactPerson, fax, usrid, usrid, tsCreate, tsCreate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBankAccountAdd implements Action
{
	private String strClassName = "DoBankAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-bankaccount-page");
		} else
		{
			fnAdd(servlet, request, response);
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-bankaccount-page");
		}
	}

	protected void fnPopulate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnPopulate()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = ProfitCostCenterNut.getAllObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String bankCode = trim(request.getParameter("bankCode"));
		String bankName = trim(request.getParameter("bankName"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String currency = request.getParameter("currency");
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String signatory5 = trim(request.getParameter("signatory5"));
		String pcCenter = trim(request.getParameter("pcCenter"));
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		HttpSession session = request.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + " WARNING - NULL userName");
		} else
		{
			try
			{
				usrid = lUsr.getUserId();
			} catch (Exception e)
			{
				Log.printAudit("User does not exist: " + e.getMessage());
			}
		}
		Timestamp tsCreate = TimeFormat.getTimestamp();
		byte[] signature = new String("not used").getBytes();
		try
		{
			BankAccountHome home = BankAccountNut.getHome();
			home.create(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
					signatory3, signatory4, signatory5, signature, new Integer(pcCenter), add1, add2, add3, state,
					country, phone, contactPerson, fax, usrid, usrid, tsCreate, tsCreate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
