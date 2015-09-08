/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
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

public class DoBankAccountUpdateRem implements Action
{
	private String strClassName = "DoBankAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateBankAccount");
			fnUpdate(servlet, request, response);
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remBankAccount");
			fnRemove(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-bankaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnUpdate()";
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
		Timestamp tsUpdate = TimeFormat.getTimestamp();
		try
		{
			String pkid = request.getParameter("pkid");
			Integer intpkid = new Integer(pkid);
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			bacc.setBankCode(bankCode);
			bacc.setBankName(bankName);
			bacc.setAccountNumber(accountNumber);
			bacc.setCurrency(currency);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setSignatory5(signatory5);
			bacc.setPCCenter(new Integer(pcCenter));
			bacc.setAdd1(add1);
			bacc.setAdd2(add2);
			bacc.setAdd3(add3);
			bacc.setState(state);
			bacc.setCountry(country);
			bacc.setPhone(phone);
			bacc.setContactPerson(contactPerson);
			bacc.setFax(fax);
			bacc.setUserIdUpdate(usrid);
			bacc.setLastUpdate(tsUpdate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot update Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnRemove(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnRemove()";
		String pkid = request.getParameter("pkid");
		Log.printVerbose(strClassName + " In " + funcName);
		Integer intpkid = new Integer(pkid);
		try
		{
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(BankAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Bank Account Failed" + e.getMessage());
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
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
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

public class DoBankAccountUpdateRem implements Action
{
	private String strClassName = "DoBankAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateBankAccount");
			fnUpdate(servlet, request, response);
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remBankAccount");
			fnRemove(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-bankaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnUpdate()";
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
		Timestamp tsUpdate = TimeFormat.getTimestamp();
		try
		{
			String pkid = request.getParameter("pkid");
			Integer intpkid = new Integer(pkid);
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			bacc.setBankCode(bankCode);
			bacc.setBankName(bankName);
			bacc.setAccountNumber(accountNumber);
			bacc.setCurrency(currency);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setSignatory5(signatory5);
			bacc.setPCCenter(new Integer(pcCenter));
			bacc.setAdd1(add1);
			bacc.setAdd2(add2);
			bacc.setAdd3(add3);
			bacc.setState(state);
			bacc.setCountry(country);
			bacc.setPhone(phone);
			bacc.setContactPerson(contactPerson);
			bacc.setFax(fax);
			bacc.setUserIdUpdate(usrid);
			bacc.setLastUpdate(tsUpdate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot update Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnRemove(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnRemove()";
		String pkid = request.getParameter("pkid");
		Log.printVerbose(strClassName + " In " + funcName);
		Integer intpkid = new Integer(pkid);
		try
		{
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(BankAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Bank Account Failed" + e.getMessage());
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
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
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

public class DoBankAccountUpdateRem implements Action
{
	private String strClassName = "DoBankAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateBankAccount");
			fnUpdate(servlet, request, response);
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remBankAccount");
			fnRemove(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-bankaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnUpdate()";
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
		Timestamp tsUpdate = TimeFormat.getTimestamp();
		try
		{
			String pkid = request.getParameter("pkid");
			Integer intpkid = new Integer(pkid);
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			bacc.setBankCode(bankCode);
			bacc.setBankName(bankName);
			bacc.setAccountNumber(accountNumber);
			bacc.setCurrency(currency);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setSignatory5(signatory5);
			bacc.setPCCenter(new Integer(pcCenter));
			bacc.setAdd1(add1);
			bacc.setAdd2(add2);
			bacc.setAdd3(add3);
			bacc.setState(state);
			bacc.setCountry(country);
			bacc.setPhone(phone);
			bacc.setContactPerson(contactPerson);
			bacc.setFax(fax);
			bacc.setUserIdUpdate(usrid);
			bacc.setLastUpdate(tsUpdate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot update Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnRemove(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnRemove()";
		String pkid = request.getParameter("pkid");
		Log.printVerbose(strClassName + " In " + funcName);
		Integer intpkid = new Integer(pkid);
		try
		{
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(BankAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Bank Account Failed" + e.getMessage());
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
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
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

public class DoBankAccountUpdateRem implements Action
{
	private String strClassName = "DoBankAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateBankAccount");
			fnUpdate(servlet, request, response);
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remBankAccount");
			fnRemove(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-bankaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = BankAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnUpdate()";
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
		Timestamp tsUpdate = TimeFormat.getTimestamp();
		try
		{
			String pkid = request.getParameter("pkid");
			Integer intpkid = new Integer(pkid);
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			bacc.setBankCode(bankCode);
			bacc.setBankName(bankName);
			bacc.setAccountNumber(accountNumber);
			bacc.setCurrency(currency);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setSignatory5(signatory5);
			bacc.setPCCenter(new Integer(pcCenter));
			bacc.setAdd1(add1);
			bacc.setAdd2(add2);
			bacc.setAdd3(add3);
			bacc.setState(state);
			bacc.setCountry(country);
			bacc.setPhone(phone);
			bacc.setContactPerson(contactPerson);
			bacc.setFax(fax);
			bacc.setUserIdUpdate(usrid);
			bacc.setLastUpdate(tsUpdate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot update Bank Account because " + ex.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnRemove(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnRemove()";
		String pkid = request.getParameter("pkid");
		Log.printVerbose(strClassName + " In " + funcName);
		Integer intpkid = new Integer(pkid);
		try
		{
			BankAccount bacc = BankAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(BankAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Bank Account Failed" + e.getMessage());
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
