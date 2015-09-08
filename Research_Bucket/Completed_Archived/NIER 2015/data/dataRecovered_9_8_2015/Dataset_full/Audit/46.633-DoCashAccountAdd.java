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

public class DoCashAccountAdd implements Action
{
	private String strClassName = "DoCashAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-cashaccount-page");
		} else
		{
			try
			{
				fnAdd(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-cashaccount-page");
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
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = request.getParameter("description");
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
		String StrPcCenter = trim(request.getParameter("pcCenter"));
		Integer pcCenter = new Integer(StrPcCenter);
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		String status = CashAccountBean.STATUS_ACTIVE;
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
		Integer userIdCreate = usrid;
		Integer userIdUpdate = usrid;
		Timestamp tsCreate = TimeFormat.getTimestamp();
		Timestamp createTime = tsCreate;
		Timestamp lastUpdate = tsCreate;
		byte[] signature = new String("not used").getBytes();
		try
		{
			CashAccountHome cashHome = CashAccountNut.getHome();
			cashHome.create(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
					facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature,
					pcCenter, add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate,
					createTime, lastUpdate, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Cash Account because " + ex.getMessage());
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

public class DoCashAccountAdd implements Action
{
	private String strClassName = "DoCashAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-cashaccount-page");
		} else
		{
			try
			{
				fnAdd(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-cashaccount-page");
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
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = request.getParameter("description");
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
		String StrPcCenter = trim(request.getParameter("pcCenter"));
		Integer pcCenter = new Integer(StrPcCenter);
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		String status = CashAccountBean.STATUS_ACTIVE;
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
		Integer userIdCreate = usrid;
		Integer userIdUpdate = usrid;
		Timestamp tsCreate = TimeFormat.getTimestamp();
		Timestamp createTime = tsCreate;
		Timestamp lastUpdate = tsCreate;
		byte[] signature = new String("not used").getBytes();
		try
		{
			CashAccountHome cashHome = CashAccountNut.getHome();
			cashHome.create(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
					facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature,
					pcCenter, add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate,
					createTime, lastUpdate, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Cash Account because " + ex.getMessage());
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

public class DoCashAccountAdd implements Action
{
	private String strClassName = "DoCashAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-cashaccount-page");
		} else
		{
			try
			{
				fnAdd(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-cashaccount-page");
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
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = request.getParameter("description");
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
		String StrPcCenter = trim(request.getParameter("pcCenter"));
		Integer pcCenter = new Integer(StrPcCenter);
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		String status = CashAccountBean.STATUS_ACTIVE;
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
		Integer userIdCreate = usrid;
		Integer userIdUpdate = usrid;
		Timestamp tsCreate = TimeFormat.getTimestamp();
		Timestamp createTime = tsCreate;
		Timestamp lastUpdate = tsCreate;
		byte[] signature = new String("not used").getBytes();
		try
		{
			CashAccountHome cashHome = CashAccountNut.getHome();
			cashHome.create(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
					facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature,
					pcCenter, add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate,
					createTime, lastUpdate, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Cash Account because " + ex.getMessage());
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

public class DoCashAccountAdd implements Action
{
	private String strClassName = "DoCashAccountAdd: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String formName = request.getParameter("formName");
		if (formName == null || formName.equals(""))
		{
			fnPopulate(servlet, request, response);
			return new ActionRouter("acc-setup-add-cashaccount-page");
		} else
		{
			try
			{
				fnAdd(servlet, request, response);
			} catch (Exception ex)
			{
				request.setAttribute("errMsg", ex.getMessage());
			}
			fnList(servlet, request, response);
			return new ActionRouter("acc-setup-list-cashaccount-page");
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
		Collection col = CashAccountNut.getActiveObjects();
		Iterator iter = col.iterator();
		request.setAttribute("iter", iter);
		Log.printVerbose("Leaving " + strClassName + " " + funcName);
	}

	protected void fnAdd(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String funcName = "fnAdd()";
		Log.printVerbose(strClassName + " In " + funcName);
		String code = trim(request.getParameter("code"));
		String name = trim(request.getParameter("name"));
		String description = request.getParameter("description");
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
		String StrPcCenter = trim(request.getParameter("pcCenter"));
		Integer pcCenter = new Integer(StrPcCenter);
		String add1 = trim(request.getParameter("add1"));
		String add2 = trim(request.getParameter("add2"));
		String add3 = trim(request.getParameter("add3"));
		String state = trim(request.getParameter("state"));
		String country = trim(request.getParameter("country"));
		String phone = trim(request.getParameter("phone"));
		String contactPerson = trim(request.getParameter("contactPerson"));
		String fax = trim(request.getParameter("fax"));
		String status = CashAccountBean.STATUS_ACTIVE;
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
		Integer userIdCreate = usrid;
		Integer userIdUpdate = usrid;
		Timestamp tsCreate = TimeFormat.getTimestamp();
		Timestamp createTime = tsCreate;
		Timestamp lastUpdate = tsCreate;
		byte[] signature = new String("not used").getBytes();
		try
		{
			CashAccountHome cashHome = CashAccountNut.getHome();
			cashHome.create(code, name, description, accountNumber, accountType, currency, levelLow, levelHigh,
					facilityAmount, overdraftLimit, signatory1, signatory2, signatory3, signatory4, signature,
					pcCenter, add1, add2, add3, state, country, phone, contactPerson, fax, userIdCreate, userIdUpdate,
					createTime, lastUpdate, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("Cannot create Cash Account because " + ex.getMessage());
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
