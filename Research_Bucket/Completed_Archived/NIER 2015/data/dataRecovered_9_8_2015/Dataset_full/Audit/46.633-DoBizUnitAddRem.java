/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizUnitAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizunit-page");
		}
		if (formName.equals("addBizUnit"))
		{
			fnAddBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("rmBizUnit"))
		{
			fnRmBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("activateBizUnit"))
		{
			fnActivateBizUnit(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		fnGetBizUnitList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizunit-page");
	}

	protected void fnRmBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer rmBizUnitId = (Integer)req.getParameter("removeBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("removeBizUnit"));
		if (rmBizUnitId != null)
		{
			// BizUnit lBU = BizUnitNut.getObjectByCode(rmBizUnitCode);
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					lBU.setStatus(BizUnitBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// String rmBizUnitCode = (String)req.getParameter("activateBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("activateBizUnit"));
		if (rmBizUnitId != null)
		{
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					Log.printVerbose("Activating BizUnit" + lBU.getPkId().toString());
					lBU.setStatus(BizUnitBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizEntityList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.ACTIVE);
		Iterator itrActiveBE = colActiveBE.iterator();
		req.setAttribute("itrActiveBE", itrActiveBE);
		Collection colInactiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.INACTIVE);
		Iterator itrInactiveBE = colInactiveBE.iterator();
		req.setAttribute("itrInactiveBE", itrInactiveBE);
	}

	protected void fnGetBizUnitList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.ACTIVE);
		Iterator itrActiveBU = colActiveBU.iterator();
		req.setAttribute("itrActiveBU", itrActiveBU);
		Collection colInactiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.INACTIVE);
		Iterator itrInactiveBU = colInactiveBU.iterator();
		req.setAttribute("itrInactiveBU", itrInactiveBU);
	}

	protected void fnAddBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizUnitCode = (String) req.getParameter("bizUnitCode");
		String bizUnitName = (String) req.getParameter("bizUnitName");
		String bizUnitDesc = (String) req.getParameter("bizUnitDescription");
		String strBizEntityId = (String) req.getParameter("bizEntityId");
		Integer iBizEntityId = new Integer(strBizEntityId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizUnitCode == null)
			return;
		if (bizUnitName == null)
			return;
		if (bizUnitDesc == null)
			return;
		BizUnit lBizGrp = null;
		if (bizUnitCode != null)
			lBizGrp = BizUnitNut.getObjectByCode(bizUnitCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizUnit");
			BizUnitHome lBUH = BizUnitNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				BizUnit newBU = (BizUnit) lBUH.create(bizUnitCode, bizUnitName, bizUnitDesc, iBizEntityId, tsCreate,
						usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizUnit " + ex.getMessage());
			}
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizUnitAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizunit-page");
		}
		if (formName.equals("addBizUnit"))
		{
			fnAddBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("rmBizUnit"))
		{
			fnRmBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("activateBizUnit"))
		{
			fnActivateBizUnit(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		fnGetBizUnitList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizunit-page");
	}

	protected void fnRmBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer rmBizUnitId = (Integer)req.getParameter("removeBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("removeBizUnit"));
		if (rmBizUnitId != null)
		{
			// BizUnit lBU = BizUnitNut.getObjectByCode(rmBizUnitCode);
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					lBU.setStatus(BizUnitBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// String rmBizUnitCode = (String)req.getParameter("activateBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("activateBizUnit"));
		if (rmBizUnitId != null)
		{
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					Log.printVerbose("Activating BizUnit" + lBU.getPkId().toString());
					lBU.setStatus(BizUnitBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizEntityList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.ACTIVE);
		Iterator itrActiveBE = colActiveBE.iterator();
		req.setAttribute("itrActiveBE", itrActiveBE);
		Collection colInactiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.INACTIVE);
		Iterator itrInactiveBE = colInactiveBE.iterator();
		req.setAttribute("itrInactiveBE", itrInactiveBE);
	}

	protected void fnGetBizUnitList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.ACTIVE);
		Iterator itrActiveBU = colActiveBU.iterator();
		req.setAttribute("itrActiveBU", itrActiveBU);
		Collection colInactiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.INACTIVE);
		Iterator itrInactiveBU = colInactiveBU.iterator();
		req.setAttribute("itrInactiveBU", itrInactiveBU);
	}

	protected void fnAddBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizUnitCode = (String) req.getParameter("bizUnitCode");
		String bizUnitName = (String) req.getParameter("bizUnitName");
		String bizUnitDesc = (String) req.getParameter("bizUnitDescription");
		String strBizEntityId = (String) req.getParameter("bizEntityId");
		Integer iBizEntityId = new Integer(strBizEntityId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizUnitCode == null)
			return;
		if (bizUnitName == null)
			return;
		if (bizUnitDesc == null)
			return;
		BizUnit lBizGrp = null;
		if (bizUnitCode != null)
			lBizGrp = BizUnitNut.getObjectByCode(bizUnitCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizUnit");
			BizUnitHome lBUH = BizUnitNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				BizUnit newBU = (BizUnit) lBUH.create(bizUnitCode, bizUnitName, bizUnitDesc, iBizEntityId, tsCreate,
						usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizUnit " + ex.getMessage());
			}
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizUnitAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizunit-page");
		}
		if (formName.equals("addBizUnit"))
		{
			fnAddBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("rmBizUnit"))
		{
			fnRmBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("activateBizUnit"))
		{
			fnActivateBizUnit(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		fnGetBizUnitList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizunit-page");
	}

	protected void fnRmBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer rmBizUnitId = (Integer)req.getParameter("removeBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("removeBizUnit"));
		if (rmBizUnitId != null)
		{
			// BizUnit lBU = BizUnitNut.getObjectByCode(rmBizUnitCode);
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					lBU.setStatus(BizUnitBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// String rmBizUnitCode = (String)req.getParameter("activateBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("activateBizUnit"));
		if (rmBizUnitId != null)
		{
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					Log.printVerbose("Activating BizUnit" + lBU.getPkId().toString());
					lBU.setStatus(BizUnitBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizEntityList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.ACTIVE);
		Iterator itrActiveBE = colActiveBE.iterator();
		req.setAttribute("itrActiveBE", itrActiveBE);
		Collection colInactiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.INACTIVE);
		Iterator itrInactiveBE = colInactiveBE.iterator();
		req.setAttribute("itrInactiveBE", itrInactiveBE);
	}

	protected void fnGetBizUnitList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.ACTIVE);
		Iterator itrActiveBU = colActiveBU.iterator();
		req.setAttribute("itrActiveBU", itrActiveBU);
		Collection colInactiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.INACTIVE);
		Iterator itrInactiveBU = colInactiveBU.iterator();
		req.setAttribute("itrInactiveBU", itrInactiveBU);
	}

	protected void fnAddBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizUnitCode = (String) req.getParameter("bizUnitCode");
		String bizUnitName = (String) req.getParameter("bizUnitName");
		String bizUnitDesc = (String) req.getParameter("bizUnitDescription");
		String strBizEntityId = (String) req.getParameter("bizEntityId");
		Integer iBizEntityId = new Integer(strBizEntityId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizUnitCode == null)
			return;
		if (bizUnitName == null)
			return;
		if (bizUnitDesc == null)
			return;
		BizUnit lBizGrp = null;
		if (bizUnitCode != null)
			lBizGrp = BizUnitNut.getObjectByCode(bizUnitCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizUnit");
			BizUnitHome lBUH = BizUnitNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				BizUnit newBU = (BizUnit) lBUH.create(bizUnitCode, bizUnitName, bizUnitDesc, iBizEntityId, tsCreate,
						usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizUnit " + ex.getMessage());
			}
		}
	}
}
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizUnitAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizunit-page");
		}
		if (formName.equals("addBizUnit"))
		{
			fnAddBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("rmBizUnit"))
		{
			fnRmBizUnit(servlet, req, res);
			fnGetBizEntityList(servlet, req, res);
			fnGetBizUnitList(servlet, req, res);
		}
		if (formName.equals("activateBizUnit"))
		{
			fnActivateBizUnit(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		fnGetBizUnitList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizunit-page");
	}

	protected void fnRmBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// Integer rmBizUnitId = (Integer)req.getParameter("removeBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("removeBizUnit"));
		if (rmBizUnitId != null)
		{
			// BizUnit lBU = BizUnitNut.getObjectByCode(rmBizUnitCode);
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					lBU.setStatus(BizUnitBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		// String rmBizUnitCode = (String)req.getParameter("activateBizUnit");
		Integer rmBizUnitId = new Integer(req.getParameter("activateBizUnit"));
		if (rmBizUnitId != null)
		{
			BizUnit lBU = BizUnitNut.getHandle(rmBizUnitId);
			if (lBU != null)
			{
				try
				{
					// lBU.remove();
					Log.printVerbose("Activating BizUnit" + lBU.getPkId().toString());
					lBU.setStatus(BizUnitBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizUnit Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizEntityList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.ACTIVE);
		Iterator itrActiveBE = colActiveBE.iterator();
		req.setAttribute("itrActiveBE", itrActiveBE);
		Collection colInactiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.INACTIVE);
		Iterator itrInactiveBE = colInactiveBE.iterator();
		req.setAttribute("itrInactiveBE", itrInactiveBE);
	}

	protected void fnGetBizUnitList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.ACTIVE);
		Iterator itrActiveBU = colActiveBU.iterator();
		req.setAttribute("itrActiveBU", itrActiveBU);
		Collection colInactiveBU = BizUnitNut.getCollectionByField(BizUnitBean.STATUS, BizUnitBean.INACTIVE);
		Iterator itrInactiveBU = colInactiveBU.iterator();
		req.setAttribute("itrInactiveBU", itrInactiveBU);
	}

	protected void fnAddBizUnit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizUnitCode = (String) req.getParameter("bizUnitCode");
		String bizUnitName = (String) req.getParameter("bizUnitName");
		String bizUnitDesc = (String) req.getParameter("bizUnitDescription");
		String strBizEntityId = (String) req.getParameter("bizEntityId");
		Integer iBizEntityId = new Integer(strBizEntityId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizUnitCode == null)
			return;
		if (bizUnitName == null)
			return;
		if (bizUnitDesc == null)
			return;
		BizUnit lBizGrp = null;
		if (bizUnitCode != null)
			lBizGrp = BizUnitNut.getObjectByCode(bizUnitCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizUnit");
			BizUnitHome lBUH = BizUnitNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				BizUnit newBU = (BizUnit) lBUH.create(bizUnitCode, bizUnitName, bizUnitDesc, iBizEntityId, tsCreate,
						usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizUnit " + ex.getMessage());
			}
		}
	}
}
