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
package com.vlee.servlet.warehouse;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoCancelDelta implements Action
{
	private String strClassName = "DoCancelDelta";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-cancel-delta-1-page";
		}
		// //----------------------------------------
		String lotNo = req.getParameter("lotNo");
		if (lotNo != null)
		{
			// trim it, remove white spaces and capitalize it
			lotNo = StringManup.stripAndUpper(lotNo);
		}
		req.setAttribute("lotNo", lotNo);
		// //----------------------------------------
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		// //----------------------------------------
		if (formName.equals("selectLot"))
		{
			// check if the lot exist
			Vector vecLots = WarehouseStockNut.getObjectsGiven(WarehouseStockBean.LOT_NO, lotNo, lotNo, (String) null,
					(String) null, (String) null, (String) null, (String) null, (String) null, (String) null,
					(String) null, (String) null);
			WarehouseStockObject whsObj = null;
			if (vecLots.size() > 0)
			{
				whsObj = (WarehouseStockObject) vecLots.get(0);
				req.setAttribute("whsObj", whsObj);
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("cancelDelta"))
		{
			// first, create the lot,
			String strErrMsg = fnCancelStockDelta(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to cancel the stock movement.... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-cancel-delta-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-cancel-delta-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnCancelStockDelta(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Long deltaId = new Long(req.getParameter("deltaId"));
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			StockDelta deltaEJB = StockDeltaNut.getHandle(deltaId);
			StockDeltaObject deltaObj = deltaEJB.getObject();
			// / update Stock Balance
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			BigDecimal bal = stkEJB.getBalance();
			bal = bal.add(deltaObj.quantity.negate());
			stkEJB.setBalance(bal);
			// / update WarehouseStock Balance
			whsEJB.setBalFull(whsObj.computeFullQty(bal));
			whsEJB.setBalLoose(whsObj.computeRemainder(bal));
			WarehouseStockObject whsObjNew = whsEJB.getObject();
			deltaObj.status = StockDeltaBean.STATUS_CANCELLED;
			deltaEJB.setObject(deltaObj);
			deltaEJB.remove();

			req.setAttribute("deltaObj", deltaObj);
			req.setAttribute("whsObj", whsObj);
			req.setAttribute("whsObjNew", whsObjNew);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			strErrMsg = ex.getMessage();
			return strErrMsg;
		}
		return strErrMsg;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		Long deltaID = new Long(req.getParameter("deltaId"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = WarehouseStockBean.FE_TABLE;
			atObj.foreignKey1 = deltaID;
			atObj.remarks = "warehouse_mgt: delete-delta";
			AuditTrailNut.fnCreate(atObj);
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
package com.vlee.servlet.warehouse;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoCancelDelta implements Action
{
	private String strClassName = "DoCancelDelta";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-cancel-delta-1-page";
		}
		// //----------------------------------------
		String lotNo = req.getParameter("lotNo");
		if (lotNo != null)
		{
			// trim it, remove white spaces and capitalize it
			lotNo = StringManup.stripAndUpper(lotNo);
		}
		req.setAttribute("lotNo", lotNo);
		// //----------------------------------------
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		// //----------------------------------------
		if (formName.equals("selectLot"))
		{
			// check if the lot exist
			Vector vecLots = WarehouseStockNut.getObjectsGiven(WarehouseStockBean.LOT_NO, lotNo, lotNo, (String) null,
					(String) null, (String) null, (String) null, (String) null, (String) null, (String) null,
					(String) null, (String) null);
			WarehouseStockObject whsObj = null;
			if (vecLots.size() > 0)
			{
				whsObj = (WarehouseStockObject) vecLots.get(0);
				req.setAttribute("whsObj", whsObj);
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("cancelDelta"))
		{
			// first, create the lot,
			String strErrMsg = fnCancelStockDelta(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to cancel the stock movement.... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-cancel-delta-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-cancel-delta-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnCancelStockDelta(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Long deltaId = new Long(req.getParameter("deltaId"));
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			StockDelta deltaEJB = StockDeltaNut.getHandle(deltaId);
			StockDeltaObject deltaObj = deltaEJB.getObject();
			// / update Stock Balance
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			BigDecimal bal = stkEJB.getBalance();
			bal = bal.add(deltaObj.quantity.negate());
			stkEJB.setBalance(bal);
			// / update WarehouseStock Balance
			whsEJB.setBalFull(whsObj.computeFullQty(bal));
			whsEJB.setBalLoose(whsObj.computeRemainder(bal));
			WarehouseStockObject whsObjNew = whsEJB.getObject();
			deltaObj.status = StockDeltaBean.STATUS_CANCELLED;
			deltaEJB.setObject(deltaObj);
			deltaEJB.remove();

			req.setAttribute("deltaObj", deltaObj);
			req.setAttribute("whsObj", whsObj);
			req.setAttribute("whsObjNew", whsObjNew);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			strErrMsg = ex.getMessage();
			return strErrMsg;
		}
		return strErrMsg;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		Long deltaID = new Long(req.getParameter("deltaId"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = WarehouseStockBean.FE_TABLE;
			atObj.foreignKey1 = deltaID;
			atObj.remarks = "warehouse_mgt: delete-delta";
			AuditTrailNut.fnCreate(atObj);
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
package com.vlee.servlet.warehouse;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoCancelDelta implements Action
{
	private String strClassName = "DoCancelDelta";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-cancel-delta-1-page";
		}
		// //----------------------------------------
		String lotNo = req.getParameter("lotNo");
		if (lotNo != null)
		{
			// trim it, remove white spaces and capitalize it
			lotNo = StringManup.stripAndUpper(lotNo);
		}
		req.setAttribute("lotNo", lotNo);
		// //----------------------------------------
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		// //----------------------------------------
		if (formName.equals("selectLot"))
		{
			// check if the lot exist
			Vector vecLots = WarehouseStockNut.getObjectsGiven(WarehouseStockBean.LOT_NO, lotNo, lotNo, (String) null,
					(String) null, (String) null, (String) null, (String) null, (String) null, (String) null,
					(String) null, (String) null);
			WarehouseStockObject whsObj = null;
			if (vecLots.size() > 0)
			{
				whsObj = (WarehouseStockObject) vecLots.get(0);
				req.setAttribute("whsObj", whsObj);
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("cancelDelta"))
		{
			// first, create the lot,
			String strErrMsg = fnCancelStockDelta(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to cancel the stock movement.... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-cancel-delta-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-cancel-delta-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnCancelStockDelta(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Long deltaId = new Long(req.getParameter("deltaId"));
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			StockDelta deltaEJB = StockDeltaNut.getHandle(deltaId);
			StockDeltaObject deltaObj = deltaEJB.getObject();
			// / update Stock Balance
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			BigDecimal bal = stkEJB.getBalance();
			bal = bal.add(deltaObj.quantity.negate());
			stkEJB.setBalance(bal);
			// / update WarehouseStock Balance
			whsEJB.setBalFull(whsObj.computeFullQty(bal));
			whsEJB.setBalLoose(whsObj.computeRemainder(bal));
			WarehouseStockObject whsObjNew = whsEJB.getObject();
			deltaObj.status = StockDeltaBean.STATUS_CANCELLED;
			deltaEJB.setObject(deltaObj);
			req.setAttribute("deltaObj", deltaObj);
			req.setAttribute("whsObj", whsObj);
			req.setAttribute("whsObjNew", whsObjNew);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			strErrMsg = ex.getMessage();
			return strErrMsg;
		}
		return strErrMsg;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		Long deltaID = new Long(req.getParameter("deltaId"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = WarehouseStockBean.FE_TABLE;
			atObj.foreignKey1 = deltaID;
			atObj.remarks = "warehouse_mgt: delete-delta";
			AuditTrailNut.fnCreate(atObj);
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
package com.vlee.servlet.warehouse;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;

public class DoCancelDelta implements Action
{
	private String strClassName = "DoCancelDelta";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-cancel-delta-1-page";
		}
		// //----------------------------------------
		String lotNo = req.getParameter("lotNo");
		if (lotNo != null)
		{
			// trim it, remove white spaces and capitalize it
			lotNo = StringManup.stripAndUpper(lotNo);
		}
		req.setAttribute("lotNo", lotNo);
		// //----------------------------------------
		if (formName == null)
		{
			return new ActionRouter(fwdPage);
		}
		// //----------------------------------------
		if (formName.equals("selectLot"))
		{
			// check if the lot exist
			Vector vecLots = WarehouseStockNut.getObjectsGiven(WarehouseStockBean.LOT_NO, lotNo, lotNo, (String) null,
					(String) null, (String) null, (String) null, (String) null, (String) null, (String) null,
					(String) null, (String) null);
			WarehouseStockObject whsObj = null;
			if (vecLots.size() > 0)
			{
				whsObj = (WarehouseStockObject) vecLots.get(0);
				req.setAttribute("whsObj", whsObj);
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-cancel-delta-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("cancelDelta"))
		{
			// first, create the lot,
			String strErrMsg = fnCancelStockDelta(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to cancel the stock movement.... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-cancel-delta-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-cancel-delta-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnCancelStockDelta(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Long deltaId = new Long(req.getParameter("deltaId"));
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			StockDelta deltaEJB = StockDeltaNut.getHandle(deltaId);
			StockDeltaObject deltaObj = deltaEJB.getObject();
			// / update Stock Balance
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			BigDecimal bal = stkEJB.getBalance();
			bal = bal.add(deltaObj.quantity.negate());
			stkEJB.setBalance(bal);
			// / update WarehouseStock Balance
			whsEJB.setBalFull(whsObj.computeFullQty(bal));
			whsEJB.setBalLoose(whsObj.computeRemainder(bal));
			WarehouseStockObject whsObjNew = whsEJB.getObject();
			deltaObj.status = StockDeltaBean.STATUS_CANCELLED;
			deltaEJB.setObject(deltaObj);
			deltaEJB.remove();

			req.setAttribute("deltaObj", deltaObj);
			req.setAttribute("whsObj", whsObj);
			req.setAttribute("whsObjNew", whsObjNew);
			fnAuditTrail(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			strErrMsg = ex.getMessage();
			return strErrMsg;
		}
		return strErrMsg;
	}

	private void fnAuditTrail(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession(true);
		Integer iUserId = (Integer) session.getAttribute("userId");
		Long deltaID = new Long(req.getParameter("deltaId"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.foreignTable1 = WarehouseStockBean.FE_TABLE;
			atObj.foreignKey1 = deltaID;
			atObj.remarks = "warehouse_mgt: delete-delta";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
