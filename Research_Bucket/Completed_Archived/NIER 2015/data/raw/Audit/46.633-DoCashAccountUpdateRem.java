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

public class DoCashAccountUpdateRem implements Action
{
	private String strClassName = "DoCashAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateCashAccount");
			try
			{
				fnUpdate(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remCashAccount");
			fnRemove(servlet, request, response);
		}
		else if (action.equals("Activate Account"))
		{
			Log.printVerbose(strClassName + " formName = actCashAccount");
			fnActivate(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-cashaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnUpdate()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = trim(request.getParameter("description"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String accountType = trim(request.getParameter("accountType"));
		GLCode glcodeEJB = GLCodeNut.getObjectByCode(accountType);
		if (glcodeEJB == null)
		{
			throw new Exception("Invalid GLCode!!");
		}
		String currency = request.getParameter("currency");
		String strLevelLow = request.getParameter("levelLow");
		BigDecimal levelLow = new BigDecimal(strLevelLow);
		String strLevelHigh = request.getParameter("levelHigh");
		BigDecimal levelHigh = new BigDecimal(strLevelHigh);
		String strFacilityAmount = request.getParameter("facilityAmount");
		BigDecimal facilityAmount = new BigDecimal(strFacilityAmount);
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String strSignature = new String("not used");
		byte[] signature = strSignature.getBytes();
		Integer pcCenter = new Integer(trim(request.getParameter("pcCenter")));
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			bacc.setCode(code);
			bacc.setName(name);
			bacc.setDescription(description);
			bacc.setAccountNumber(accountNumber);
			bacc.setAccountType(accountType);
			bacc.setCurrency(currency);
			bacc.setLevelLow(levelLow);
			bacc.setLevelHigh(levelHigh);
			bacc.setFacilityAmount(facilityAmount);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setPCCenter(pcCenter);
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
			Log.printDebug("Cannot update Cash Account because " + ex.getMessage());
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(CashAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Cash Account Failed" + e.getMessage());
		}
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}
	
	protected void fnActivate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnActivate()";
		String pkid = request.getParameter("pkid");
		Log.printVerbose(strClassName + " In " + funcName);
		Integer intpkid = new Integer(pkid);
		try
		{
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(CashAccountBean.STATUS_ACTIVE);
		} catch (Exception e)
		{
			Log.printDebug("Remove Cash Account Failed" + e.getMessage());
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

public class DoCashAccountUpdateRem implements Action
{
	private String strClassName = "DoCashAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateCashAccount");
			try
			{
				fnUpdate(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remCashAccount");
			fnRemove(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-cashaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnUpdate()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = trim(request.getParameter("description"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String accountType = trim(request.getParameter("accountType"));
		GLCode glcodeEJB = GLCodeNut.getObjectByCode(accountType);
		if (glcodeEJB == null)
		{
			throw new Exception("Invalid GLCode!!");
		}
		String currency = request.getParameter("currency");
		String strLevelLow = request.getParameter("levelLow");
		BigDecimal levelLow = new BigDecimal(strLevelLow);
		String strLevelHigh = request.getParameter("levelHigh");
		BigDecimal levelHigh = new BigDecimal(strLevelHigh);
		String strFacilityAmount = request.getParameter("facilityAmount");
		BigDecimal facilityAmount = new BigDecimal(strFacilityAmount);
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String strSignature = new String("not used");
		byte[] signature = strSignature.getBytes();
		Integer pcCenter = new Integer(trim(request.getParameter("pcCenter")));
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			bacc.setCode(code);
			bacc.setName(name);
			bacc.setDescription(description);
			bacc.setAccountNumber(accountNumber);
			bacc.setAccountType(accountType);
			bacc.setCurrency(currency);
			bacc.setLevelLow(levelLow);
			bacc.setLevelHigh(levelHigh);
			bacc.setFacilityAmount(facilityAmount);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setPCCenter(pcCenter);
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
			Log.printDebug("Cannot update Cash Account because " + ex.getMessage());
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(CashAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Cash Account Failed" + e.getMessage());
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

public class DoCashAccountUpdateRem implements Action
{
	private String strClassName = "DoCashAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateCashAccount");
			try
			{
				fnUpdate(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remCashAccount");
			fnRemove(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-cashaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnUpdate()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = trim(request.getParameter("description"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String accountType = trim(request.getParameter("accountType"));
		GLCode glcodeEJB = GLCodeNut.getObjectByCode(accountType);
		if (glcodeEJB == null)
		{
			throw new Exception("Invalid GLCode!!");
		}
		String currency = request.getParameter("currency");
		String strLevelLow = request.getParameter("levelLow");
		BigDecimal levelLow = new BigDecimal(strLevelLow);
		String strLevelHigh = request.getParameter("levelHigh");
		BigDecimal levelHigh = new BigDecimal(strLevelHigh);
		String strFacilityAmount = request.getParameter("facilityAmount");
		BigDecimal facilityAmount = new BigDecimal(strFacilityAmount);
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String strSignature = new String("not used");
		byte[] signature = strSignature.getBytes();
		Integer pcCenter = new Integer(trim(request.getParameter("pcCenter")));
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			bacc.setCode(code);
			bacc.setName(name);
			bacc.setDescription(description);
			bacc.setAccountNumber(accountNumber);
			bacc.setAccountType(accountType);
			bacc.setCurrency(currency);
			bacc.setLevelLow(levelLow);
			bacc.setLevelHigh(levelHigh);
			bacc.setFacilityAmount(facilityAmount);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setPCCenter(pcCenter);
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
			Log.printDebug("Cannot update Cash Account because " + ex.getMessage());
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(CashAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Cash Account Failed" + e.getMessage());
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

public class DoCashAccountUpdateRem implements Action
{
	private String strClassName = "DoCashAccountUpdateRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String action = request.getParameter("action");
		if (action.equals("Save Changes"))
		{
			Log.printVerbose(strClassName + " formName = updateCashAccount");
			try
			{
				fnUpdate(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			Log.printVerbose(strClassName + " formName = remCashAccount");
			fnRemove(servlet, request, response);
		}
		fnList(servlet, request, response);
		return new ActionRouter("acc-setup-list-cashaccount-page");
	}

	// //////////////////////////////////////////////////////
	protected void fnList(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		String funcName = "fnList()";
		Log.printVerbose(strClassName + " In " + funcName);
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	// //////////////////////////////////////////////////////
	protected void fnUpdate(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnUpdate()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = trim(request.getParameter("description"));
		String accountNumber = trim(request.getParameter("accountNumber"));
		String accountType = trim(request.getParameter("accountType"));
		GLCode glcodeEJB = GLCodeNut.getObjectByCode(accountType);
		if (glcodeEJB == null)
		{
			throw new Exception("Invalid GLCode!!");
		}
		String currency = request.getParameter("currency");
		String strLevelLow = request.getParameter("levelLow");
		BigDecimal levelLow = new BigDecimal(strLevelLow);
		String strLevelHigh = request.getParameter("levelHigh");
		BigDecimal levelHigh = new BigDecimal(strLevelHigh);
		String strFacilityAmount = request.getParameter("facilityAmount");
		BigDecimal facilityAmount = new BigDecimal(strFacilityAmount);
		String strOverdraftLimit = request.getParameter("overdraftLimit");
		BigDecimal overdraftLimit = new BigDecimal(strOverdraftLimit);
		String signatory1 = trim(request.getParameter("signatory1"));
		String signatory2 = trim(request.getParameter("signatory2"));
		String signatory3 = trim(request.getParameter("signatory3"));
		String signatory4 = trim(request.getParameter("signatory4"));
		String strSignature = new String("not used");
		byte[] signature = strSignature.getBytes();
		Integer pcCenter = new Integer(trim(request.getParameter("pcCenter")));
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			bacc.setCode(code);
			bacc.setName(name);
			bacc.setDescription(description);
			bacc.setAccountNumber(accountNumber);
			bacc.setAccountType(accountType);
			bacc.setCurrency(currency);
			bacc.setLevelLow(levelLow);
			bacc.setLevelHigh(levelHigh);
			bacc.setFacilityAmount(facilityAmount);
			bacc.setOverdraftLimit(overdraftLimit);
			bacc.setSignatory1(signatory1);
			bacc.setSignatory2(signatory2);
			bacc.setSignatory3(signatory3);
			bacc.setSignatory4(signatory4);
			bacc.setPCCenter(pcCenter);
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
			Log.printDebug("Cannot update Cash Account because " + ex.getMessage());
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
			CashAccount bacc = CashAccountNut.getHandle(intpkid);
			// bacc.remove();
			bacc.setStatus(CashAccountBean.STATUS_DELETED);
		} catch (Exception e)
		{
			Log.printDebug("Remove Cash Account Failed" + e.getMessage());
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
