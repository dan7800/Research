package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizGroupAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizGroupList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizgroup-page");
		}
		if (formName.equals("addBizGroup"))
		{
			fnAddBizGroup(servlet, req, res);
		}
		if (formName.equals("rmBizGroup"))
		{
			fnRmBizGroup(servlet, req, res);
		}
		if (formName.equals("activateBizGroup"))
		{
			fnActivateBizGroup(servlet, req, res);
		}
		fnGetBizGroupList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizgroup-page");
	}

	protected void fnRmBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("removeBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("inactive");
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("activateBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("active");
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizGroupList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colBG = BizGroupNut.getAllObjects();
		Iterator itrAllBG = colBG.iterator();
		req.setAttribute("itrAllBG", itrAllBG);
		Collection colActiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.ACTIVE);
		Iterator itrActiveBG = colActiveBG.iterator();
		req.setAttribute("itrActiveBG", itrActiveBG);
		Collection colInactiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.INACTIVE);
		Iterator itrInactiveBG = colInactiveBG.iterator();
		req.setAttribute("itrInactiveBG", itrInactiveBG);
	}

	protected void fnAddBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizGroupCode = (String) req.getParameter("bizGroupCode");
		String bizGroupName = (String) req.getParameter("bizGroupName");
		String bizGroupDesc = (String) req.getParameter("bizGroupDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizGroupCode == null)
			return;
		if (bizGroupName == null)
			return;
		if (bizGroupDesc == null)
			return;
		BizGroup lBizGrp = null;
		if (bizGroupCode != null)
			lBizGrp = BizGroupNut.getObjectByCode(bizGroupCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizGroup");
			BizGroupHome lBGH = BizGroupNut.getHome();
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
				BizGroup newBG = (BizGroup) lBGH.create(bizGroupCode, bizGroupName, bizGroupDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizGroup " + ex.getMessage());
			}
		}
		fnGetBizGroupList(servlet, req, res);
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizGroupAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizGroupList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizgroup-page");
		}
		if (formName.equals("addBizGroup"))
		{
			fnAddBizGroup(servlet, req, res);
		}
		if (formName.equals("rmBizGroup"))
		{
			fnRmBizGroup(servlet, req, res);
		}
		if (formName.equals("activateBizGroup"))
		{
			fnActivateBizGroup(servlet, req, res);
		}
		fnGetBizGroupList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizgroup-page");
	}

	protected void fnRmBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("removeBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("inactive");
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("activateBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("active");
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizGroupList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colBG = BizGroupNut.getAllObjects();
		Iterator itrAllBG = colBG.iterator();
		req.setAttribute("itrAllBG", itrAllBG);
		Collection colActiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.ACTIVE);
		Iterator itrActiveBG = colActiveBG.iterator();
		req.setAttribute("itrActiveBG", itrActiveBG);
		Collection colInactiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.INACTIVE);
		Iterator itrInactiveBG = colInactiveBG.iterator();
		req.setAttribute("itrInactiveBG", itrInactiveBG);
	}

	protected void fnAddBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizGroupCode = (String) req.getParameter("bizGroupCode");
		String bizGroupName = (String) req.getParameter("bizGroupName");
		String bizGroupDesc = (String) req.getParameter("bizGroupDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizGroupCode == null)
			return;
		if (bizGroupName == null)
			return;
		if (bizGroupDesc == null)
			return;
		BizGroup lBizGrp = null;
		if (bizGroupCode != null)
			lBizGrp = BizGroupNut.getObjectByCode(bizGroupCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizGroup");
			BizGroupHome lBGH = BizGroupNut.getHome();
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
				BizGroup newBG = (BizGroup) lBGH.create(bizGroupCode, bizGroupName, bizGroupDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizGroup " + ex.getMessage());
			}
		}
		fnGetBizGroupList(servlet, req, res);
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizGroupAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizGroupList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizgroup-page");
		}
		if (formName.equals("addBizGroup"))
		{
			fnAddBizGroup(servlet, req, res);
		}
		if (formName.equals("rmBizGroup"))
		{
			fnRmBizGroup(servlet, req, res);
		}
		if (formName.equals("activateBizGroup"))
		{
			fnActivateBizGroup(servlet, req, res);
		}
		fnGetBizGroupList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizgroup-page");
	}

	protected void fnRmBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("removeBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("inactive");
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("activateBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("active");
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizGroupList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colBG = BizGroupNut.getAllObjects();
		Iterator itrAllBG = colBG.iterator();
		req.setAttribute("itrAllBG", itrAllBG);
		Collection colActiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.ACTIVE);
		Iterator itrActiveBG = colActiveBG.iterator();
		req.setAttribute("itrActiveBG", itrActiveBG);
		Collection colInactiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.INACTIVE);
		Iterator itrInactiveBG = colInactiveBG.iterator();
		req.setAttribute("itrInactiveBG", itrInactiveBG);
	}

	protected void fnAddBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizGroupCode = (String) req.getParameter("bizGroupCode");
		String bizGroupName = (String) req.getParameter("bizGroupName");
		String bizGroupDesc = (String) req.getParameter("bizGroupDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizGroupCode == null)
			return;
		if (bizGroupName == null)
			return;
		if (bizGroupDesc == null)
			return;
		BizGroup lBizGrp = null;
		if (bizGroupCode != null)
			lBizGrp = BizGroupNut.getObjectByCode(bizGroupCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizGroup");
			BizGroupHome lBGH = BizGroupNut.getHome();
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
				BizGroup newBG = (BizGroup) lBGH.create(bizGroupCode, bizGroupName, bizGroupDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizGroup " + ex.getMessage());
			}
		}
		fnGetBizGroupList(servlet, req, res);
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoBizGroupAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizGroupList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-bizgroup-page");
		}
		if (formName.equals("addBizGroup"))
		{
			fnAddBizGroup(servlet, req, res);
		}
		if (formName.equals("rmBizGroup"))
		{
			fnRmBizGroup(servlet, req, res);
		}
		if (formName.equals("activateBizGroup"))
		{
			fnActivateBizGroup(servlet, req, res);
		}
		fnGetBizGroupList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-bizgroup-page");
	}

	protected void fnRmBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("removeBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("inactive");
				} catch (Exception ex)
				{
					Log.printDebug("Remove BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmBizGroupCode = (String) req.getParameter("activateBizGroup");
		if (rmBizGroupCode != null)
		{
			BizGroup lBG = BizGroupNut.getObjectByCode(rmBizGroupCode);
			if (lBG != null)
			{
				try
				{
					// lBG.remove();
					lBG.setStatus("active");
				} catch (Exception ex)
				{
					Log.printDebug("Activate BizGroup Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetBizGroupList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colBG = BizGroupNut.getAllObjects();
		Iterator itrAllBG = colBG.iterator();
		req.setAttribute("itrAllBG", itrAllBG);
		Collection colActiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.ACTIVE);
		Iterator itrActiveBG = colActiveBG.iterator();
		req.setAttribute("itrActiveBG", itrActiveBG);
		Collection colInactiveBG = BizGroupNut.getCollectionByField(BizGroupBean.STATUS, BizGroupBean.INACTIVE);
		Iterator itrInactiveBG = colInactiveBG.iterator();
		req.setAttribute("itrInactiveBG", itrInactiveBG);
	}

	protected void fnAddBizGroup(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String bizGroupCode = (String) req.getParameter("bizGroupCode");
		String bizGroupName = (String) req.getParameter("bizGroupName");
		String bizGroupDesc = (String) req.getParameter("bizGroupDescription");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (bizGroupCode == null)
			return;
		if (bizGroupName == null)
			return;
		if (bizGroupDesc == null)
			return;
		BizGroup lBizGrp = null;
		if (bizGroupCode != null)
			lBizGrp = BizGroupNut.getObjectByCode(bizGroupCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new BizGroup");
			BizGroupHome lBGH = BizGroupNut.getHome();
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
				BizGroup newBG = (BizGroup) lBGH.create(bizGroupCode, bizGroupName, bizGroupDesc, tsCreate, usrid);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create BizGroup " + ex.getMessage());
			}
		}
		fnGetBizGroupList(servlet, req, res);
	}
}
