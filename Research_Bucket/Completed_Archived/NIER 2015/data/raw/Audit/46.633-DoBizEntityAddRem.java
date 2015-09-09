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

public class DoBizEntityAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizentity-page");
		}
		if (formName.equals("addBizEntity"))
		{
			fnAddBizEntity(servlet, req, res);
		}
		if (formName.equals("rmBizEntity"))
		{
			fnRmBizEntity(servlet, req, res);
		}
		if (formName.equals("activateBizEntity"))
		{
			fnActivateBizEntity(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizentity-page");
	}

	protected void fnRmBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("removeBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{
					// lBE.remove();
					lBE.setStatus(BizEntityBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizEntity Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("activateBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{ // lBE.remove();
					lBE.setStatus(BizEntityBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizEntity Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizEntityList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Log.printVerbose(".... inside servlet... getting biz entity listing... ");
		Collection colActiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.ACTIVE);
		Iterator itrActiveBE = colActiveBE.iterator();
		req.setAttribute("itrActiveBE", itrActiveBE);
		Collection colInactiveBE = BizEntityNut.getCollectionByField(BizEntityBean.STATUS, BizEntityBean.INACTIVE);
		Iterator itrInactiveBE = colInactiveBE.iterator();
		req.setAttribute("itrInactiveBE", itrInactiveBE);
	}

	protected void fnAddBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizEntityCode = (String) req.getParameter("bizEntityCode");
		String bizEntityName = (String) req.getParameter("bizEntityName");
		String bizEntityRegistrarId = (String) req.getParameter("bizEntityRegistrarId");
		String bizEntityDesc = (String) req.getParameter("bizEntityDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizEntityCode == null)
			return;
		if (bizEntityName == null)
			return;
		if (bizEntityDesc == null)
			return;
		BizEntity lBizGrp = null;
		if (bizEntityCode != null)
			lBizGrp = BizEntityNut.getObjectByCode(bizEntityCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizEntity");
			BizEntityHome lBEH = BizEntityNut.getHome();
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
				BizEntity newBE = (BizEntity) lBEH.create(bizEntityCode, bizEntityName, bizEntityRegistrarId,
						bizEntityDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizEntity " + ex.getMessage());
			}
		}
		fnGetBizEntityList(servlet, req, res);
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

public class DoBizEntityAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizentity-page");
		}
		if (formName.equals("addBizEntity"))
		{
			fnAddBizEntity(servlet, req, res);
		}
		if (formName.equals("rmBizEntity"))
		{
			fnRmBizEntity(servlet, req, res);
		}
		if (formName.equals("activateBizEntity"))
		{
			fnActivateBizEntity(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizentity-page");
	}

	protected void fnRmBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("removeBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{
					// lBE.remove();
					lBE.setStatus(BizEntityBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizEntity Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("activateBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{ // lBE.remove();
					lBE.setStatus(BizEntityBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizEntity Failed" + ex.getMessage());
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

	protected void fnAddBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizEntityCode = (String) req.getParameter("bizEntityCode");
		String bizEntityName = (String) req.getParameter("bizEntityName");
		String bizEntityRegistrarId = (String) req.getParameter("bizEntityRegistrarId");
		String bizEntityDesc = (String) req.getParameter("bizEntityDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizEntityCode == null)
			return;
		if (bizEntityName == null)
			return;
		if (bizEntityDesc == null)
			return;
		BizEntity lBizGrp = null;
		if (bizEntityCode != null)
			lBizGrp = BizEntityNut.getObjectByCode(bizEntityCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizEntity");
			BizEntityHome lBEH = BizEntityNut.getHome();
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
				BizEntity newBE = (BizEntity) lBEH.create(bizEntityCode, bizEntityName, bizEntityRegistrarId,
						bizEntityDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizEntity " + ex.getMessage());
			}
		}
		fnGetBizEntityList(servlet, req, res);
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

public class DoBizEntityAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizentity-page");
		}
		if (formName.equals("addBizEntity"))
		{
			fnAddBizEntity(servlet, req, res);
		}
		if (formName.equals("rmBizEntity"))
		{
			fnRmBizEntity(servlet, req, res);
		}
		if (formName.equals("activateBizEntity"))
		{
			fnActivateBizEntity(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizentity-page");
	}

	protected void fnRmBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("removeBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{
					// lBE.remove();
					lBE.setStatus(BizEntityBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizEntity Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("activateBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{ // lBE.remove();
					lBE.setStatus(BizEntityBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizEntity Failed" + ex.getMessage());
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

	protected void fnAddBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizEntityCode = (String) req.getParameter("bizEntityCode");
		String bizEntityName = (String) req.getParameter("bizEntityName");
		String bizEntityRegistrarId = (String) req.getParameter("bizEntityRegistrarId");
		String bizEntityDesc = (String) req.getParameter("bizEntityDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizEntityCode == null)
			return;
		if (bizEntityName == null)
			return;
		if (bizEntityDesc == null)
			return;
		BizEntity lBizGrp = null;
		if (bizEntityCode != null)
			lBizGrp = BizEntityNut.getObjectByCode(bizEntityCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizEntity");
			BizEntityHome lBEH = BizEntityNut.getHome();
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
				BizEntity newBE = (BizEntity) lBEH.create(bizEntityCode, bizEntityName, bizEntityRegistrarId,
						bizEntityDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizEntity " + ex.getMessage());
			}
		}
		fnGetBizEntityList(servlet, req, res);
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

public class DoBizEntityAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizEntityList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizentity-page");
		}
		if (formName.equals("addBizEntity"))
		{
			fnAddBizEntity(servlet, req, res);
		}
		if (formName.equals("rmBizEntity"))
		{
			fnRmBizEntity(servlet, req, res);
		}
		if (formName.equals("activateBizEntity"))
		{
			fnActivateBizEntity(servlet, req, res);
		}
		fnGetBizEntityList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizentity-page");
	}

	protected void fnRmBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("removeBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{
					// lBE.remove();
					lBE.setStatus(BizEntityBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizEntity Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizEntityCode = (String) req.getParameter("activateBizEntity");
		if (rmBizEntityCode != null)
		{
			BizEntity lBE = BizEntityNut.getObjectByCode(rmBizEntityCode);
			if (lBE != null)
			{
				try
				{ // lBE.remove();
					lBE.setStatus(BizEntityBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizEntity Failed" + ex.getMessage());
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

	protected void fnAddBizEntity(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizEntityCode = (String) req.getParameter("bizEntityCode");
		String bizEntityName = (String) req.getParameter("bizEntityName");
		String bizEntityRegistrarId = (String) req.getParameter("bizEntityRegistrarId");
		String bizEntityDesc = (String) req.getParameter("bizEntityDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizEntityCode == null)
			return;
		if (bizEntityName == null)
			return;
		if (bizEntityDesc == null)
			return;
		BizEntity lBizGrp = null;
		if (bizEntityCode != null)
			lBizGrp = BizEntityNut.getObjectByCode(bizEntityCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizEntity");
			BizEntityHome lBEH = BizEntityNut.getHome();
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
				BizEntity newBE = (BizEntity) lBEH.create(bizEntityCode, bizEntityName, bizEntityRegistrarId,
						bizEntityDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizEntity " + ex.getMessage());
			}
		}
		fnGetBizEntityList(servlet, req, res);
	}
}
