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

public class DoProfitCostCenterAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizUnitList(servlet, req, res);
			fnGetProfitCostCenterList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-pccenter-page");
		}
		if (formName.equals("addProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnAddProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("rmProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnRmProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("activateProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnActivateProfitCostCenter(servlet, req, res);
		}
		fnGetBizUnitList(servlet, req, res);
		fnGetProfitCostCenterList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-pccenter-page");
	}

	protected void fnRmProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("removeProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("activateProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
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

	protected void fnGetProfitCostCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	protected void fnAddProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenterCode = (String) req.getParameter("pcCenterCode");
		String pcCenterName = (String) req.getParameter("pcCenterName");
		String pcCenterDesc = (String) req.getParameter("pcCenterDescription");
		String pcCenterFullname = (String) req.getParameter("pcCenterFullname");
		String pcCenterCurrency = (String) req.getParameter("pcCenterCurrency");
		String strBizUnitId = (String) req.getParameter("bizUnitId");
		Integer iBizUnitId = new Integer(strBizUnitId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (pcCenterCode == null)
			return;
		if (pcCenterName == null)
			return;
		if (pcCenterDesc == null)
			return;
		ProfitCostCenter lBizGrp = null;
		if (pcCenterCode != null)
			lBizGrp = ProfitCostCenterNut.getObjectByCode(pcCenterCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new ProfitCostCenter");
			ProfitCostCenterHome lPCCH = ProfitCostCenterNut.getHome();
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
				ProfitCostCenterObject pccObj = new ProfitCostCenterObject();
				pccObj.mCode = pcCenterCode;
				pccObj.mName = pcCenterName;
				pccObj.mDescription = pcCenterDesc;
				pccObj.mBizUnitId = iBizUnitId;
				pccObj.mLastUpdateTime = tsCreate;
				pccObj.mUserIdEdit = usrid;
				pccObj.mFullname = pcCenterFullname;
				pccObj.mCurrency = pcCenterCurrency;
				/*
				 * ProfitCostCenter newPCC =
				 * (ProfitCostCenter)lPCCH.create(pcCenterCode, pcCenterName,
				 * pcCenterDesc, iBizUnitId, tsCreate, usrid );
				 */
				ProfitCostCenter newPCC = lPCCH.create(pccObj);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create ProfitCostCenter " + ex.getMessage());
			}
		}
		fnGetProfitCostCenterList(servlet, req, res);
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

public class DoProfitCostCenterAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizUnitList(servlet, req, res);
			fnGetProfitCostCenterList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-pccenter-page");
		}
		if (formName.equals("addProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnAddProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("rmProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnRmProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("activateProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnActivateProfitCostCenter(servlet, req, res);
		}
		fnGetBizUnitList(servlet, req, res);
		fnGetProfitCostCenterList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-pccenter-page");
	}

	protected void fnRmProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("removeProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("activateProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
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

	protected void fnGetProfitCostCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	protected void fnAddProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenterCode = (String) req.getParameter("pcCenterCode");
		String pcCenterName = (String) req.getParameter("pcCenterName");
		String pcCenterDesc = (String) req.getParameter("pcCenterDescription");
		String pcCenterFullname = (String) req.getParameter("pcCenterFullname");
		String pcCenterCurrency = (String) req.getParameter("pcCenterCurrency");
		String strBizUnitId = (String) req.getParameter("bizUnitId");
		Integer iBizUnitId = new Integer(strBizUnitId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (pcCenterCode == null)
			return;
		if (pcCenterName == null)
			return;
		if (pcCenterDesc == null)
			return;
		ProfitCostCenter lBizGrp = null;
		if (pcCenterCode != null)
			lBizGrp = ProfitCostCenterNut.getObjectByCode(pcCenterCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new ProfitCostCenter");
			ProfitCostCenterHome lPCCH = ProfitCostCenterNut.getHome();
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
				ProfitCostCenterObject pccObj = new ProfitCostCenterObject();
				pccObj.mCode = pcCenterCode;
				pccObj.mName = pcCenterName;
				pccObj.mDescription = pcCenterDesc;
				pccObj.mBizUnitId = iBizUnitId;
				pccObj.mLastUpdateTime = tsCreate;
				pccObj.mUserIdEdit = usrid;
				pccObj.mFullname = pcCenterFullname;
				pccObj.mCurrency = pcCenterCurrency;
				/*
				 * ProfitCostCenter newPCC =
				 * (ProfitCostCenter)lPCCH.create(pcCenterCode, pcCenterName,
				 * pcCenterDesc, iBizUnitId, tsCreate, usrid );
				 */
				ProfitCostCenter newPCC = lPCCH.create(pccObj);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create ProfitCostCenter " + ex.getMessage());
			}
		}
		fnGetProfitCostCenterList(servlet, req, res);
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

public class DoProfitCostCenterAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizUnitList(servlet, req, res);
			fnGetProfitCostCenterList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-pccenter-page");
		}
		if (formName.equals("addProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnAddProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("rmProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnRmProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("activateProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnActivateProfitCostCenter(servlet, req, res);
		}
		fnGetBizUnitList(servlet, req, res);
		fnGetProfitCostCenterList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-pccenter-page");
	}

	protected void fnRmProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("removeProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("activateProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
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

	protected void fnGetProfitCostCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	protected void fnAddProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenterCode = (String) req.getParameter("pcCenterCode");
		String pcCenterName = (String) req.getParameter("pcCenterName");
		String pcCenterDesc = (String) req.getParameter("pcCenterDescription");
		String pcCenterFullname = (String) req.getParameter("pcCenterFullname");
		String pcCenterCurrency = (String) req.getParameter("pcCenterCurrency");
		String strBizUnitId = (String) req.getParameter("bizUnitId");
		Integer iBizUnitId = new Integer(strBizUnitId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (pcCenterCode == null)
			return;
		if (pcCenterName == null)
			return;
		if (pcCenterDesc == null)
			return;
		ProfitCostCenter lBizGrp = null;
		if (pcCenterCode != null)
			lBizGrp = ProfitCostCenterNut.getObjectByCode(pcCenterCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new ProfitCostCenter");
			ProfitCostCenterHome lPCCH = ProfitCostCenterNut.getHome();
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
				ProfitCostCenterObject pccObj = new ProfitCostCenterObject();
				pccObj.mCode = pcCenterCode;
				pccObj.mName = pcCenterName;
				pccObj.mDescription = pcCenterDesc;
				pccObj.mBizUnitId = iBizUnitId;
				pccObj.mLastUpdateTime = tsCreate;
				pccObj.mUserIdEdit = usrid;
				pccObj.mFullname = pcCenterFullname;
				pccObj.mCurrency = pcCenterCurrency;
				/*
				 * ProfitCostCenter newPCC =
				 * (ProfitCostCenter)lPCCH.create(pcCenterCode, pcCenterName,
				 * pcCenterDesc, iBizUnitId, tsCreate, usrid );
				 */
				ProfitCostCenter newPCC = lPCCH.create(pccObj);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create ProfitCostCenter " + ex.getMessage());
			}
		}
		fnGetProfitCostCenterList(servlet, req, res);
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

public class DoProfitCostCenterAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetBizUnitList(servlet, req, res);
			fnGetProfitCostCenterList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-pccenter-page");
		}
		if (formName.equals("addProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnAddProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("rmProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnRmProfitCostCenter(servlet, req, res);
		}
		if (formName.equals("activateProfitCostCenter"))
		{
			fnGetBizUnitList(servlet, req, res);
			fnActivateProfitCostCenter(servlet, req, res);
		}
		fnGetBizUnitList(servlet, req, res);
		fnGetProfitCostCenterList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-pccenter-page");
	}

	protected void fnRmProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("removeProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.INACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnActivateProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Integer rmProfitCostCenterId = new Integer(req.getParameter("activateProfitCostCenter"));
		if (rmProfitCostCenterId != null)
		{
			ProfitCostCenter lPCC = ProfitCostCenterNut.getHandle(rmProfitCostCenterId);
			if (lPCC != null)
			{
				try
				{
					// lPCC.remove();
					lPCC.setStatus(ProfitCostCenterBean.ACTIVE);
				} catch (Exception ex)
				{
					Log.printDebug("Remove ProfitCostCenter Failed" + ex.getMessage());
				}
			}
		}
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

	protected void fnGetProfitCostCenterList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colActivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.ACTIVE);
		Iterator itrActivePCC = colActivePCC.iterator();
		req.setAttribute("itrActivePCC", itrActivePCC);
		Collection colInactivePCC = ProfitCostCenterNut.getCollectionByField(ProfitCostCenterBean.STATUS,
				ProfitCostCenterBean.INACTIVE);
		Iterator itrInactivePCC = colInactivePCC.iterator();
		req.setAttribute("itrInactivePCC", itrInactivePCC);
	}

	protected void fnAddProfitCostCenter(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String pcCenterCode = (String) req.getParameter("pcCenterCode");
		String pcCenterName = (String) req.getParameter("pcCenterName");
		String pcCenterDesc = (String) req.getParameter("pcCenterDescription");
		String pcCenterFullname = (String) req.getParameter("pcCenterFullname");
		String pcCenterCurrency = (String) req.getParameter("pcCenterCurrency");
		String strBizUnitId = (String) req.getParameter("bizUnitId");
		Integer iBizUnitId = new Integer(strBizUnitId);
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (pcCenterCode == null)
			return;
		if (pcCenterName == null)
			return;
		if (pcCenterDesc == null)
			return;
		ProfitCostCenter lBizGrp = null;
		if (pcCenterCode != null)
			lBizGrp = ProfitCostCenterNut.getObjectByCode(pcCenterCode);
		if (lBizGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new ProfitCostCenter");
			ProfitCostCenterHome lPCCH = ProfitCostCenterNut.getHome();
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
				ProfitCostCenterObject pccObj = new ProfitCostCenterObject();
				pccObj.mCode = pcCenterCode;
				pccObj.mName = pcCenterName;
				pccObj.mDescription = pcCenterDesc;
				pccObj.mBizUnitId = iBizUnitId;
				pccObj.mLastUpdateTime = tsCreate;
				pccObj.mUserIdEdit = usrid;
				pccObj.mFullname = pcCenterFullname;
				pccObj.mCurrency = pcCenterCurrency;
				/*
				 * ProfitCostCenter newPCC =
				 * (ProfitCostCenter)lPCCH.create(pcCenterCode, pcCenterName,
				 * pcCenterDesc, iBizUnitId, tsCreate, usrid );
				 */
				ProfitCostCenter newPCC = lPCCH.create(pccObj);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create ProfitCostCenter " + ex.getMessage());
			}
		}
		fnGetProfitCostCenterList(servlet, req, res);
	}
}
