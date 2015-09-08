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

public class DoDeleteLot implements Action
{
	private String strClassName = "DoDeleteLot";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-lot-delete-1-page";
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
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("deleteLot"))
		{
			// first, create the lot,
			String strErrMsg = fnDeleteWarehouseStock(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to delete the LOT .... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-lot-delete-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-lot-delete-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnDeleteWarehouseStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			Vector vecDeltas = StockDeltaNut.getValueObjectsGiven(whsObj.stock_id, StockDeltaBean.STATUS_ACTIVE);
			// / delete all stock movements
			for (int cnt1 = 0; cnt1 < vecDeltas.size(); cnt1++)
			{
				StockDeltaObject sdObj = (StockDeltaObject) vecDeltas.get(cnt1);
				StockDelta sdEJB = StockDeltaNut.getHandle(sdObj.pkid);
				sdEJB.remove();
			}
			// / delete the stock opening
			Collection colOpen = StockOpeningNut.getCollectionByField(StockOpeningBean.STOCKID, whsObj.stock_id
					.toString());
			Iterator itrOpen = colOpen.iterator();
			while (itrOpen.hasNext())
			{
				StockOpening stkOpen = (StockOpening) itrOpen.next();
				stkOpen.remove();
			}
			// / delete the warehouse stock
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			try
			{
				whsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				return ex.getMessage();
			}
			// / delete the stock ejb
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			stkEJB.remove();
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
		Integer lotID = new Integer(req.getParameter("whsPkid"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.remarks = "warehouse_mgt: delete-stock";
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

public class DoDeleteLot implements Action
{
	private String strClassName = "DoDeleteLot";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-lot-delete-1-page";
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
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("deleteLot"))
		{
			// first, create the lot,
			String strErrMsg = fnDeleteWarehouseStock(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to delete the LOT .... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-lot-delete-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-lot-delete-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnDeleteWarehouseStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			Vector vecDeltas = StockDeltaNut.getValueObjectsGiven(whsObj.stock_id, StockDeltaBean.STATUS_ACTIVE);
			// / delete all stock movements
			for (int cnt1 = 0; cnt1 < vecDeltas.size(); cnt1++)
			{
				StockDeltaObject sdObj = (StockDeltaObject) vecDeltas.get(cnt1);
				StockDelta sdEJB = StockDeltaNut.getHandle(sdObj.pkid);
				sdEJB.remove();
			}
			// / delete the stock opening
			Collection colOpen = StockOpeningNut.getCollectionByField(StockOpeningBean.STOCKID, whsObj.stock_id
					.toString());
			Iterator itrOpen = colOpen.iterator();
			while (itrOpen.hasNext())
			{
				StockOpening stkOpen = (StockOpening) itrOpen.next();
				stkOpen.remove();
			}
			// / delete the warehouse stock
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			try
			{
				whsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				return ex.getMessage();
			}
			// / delete the stock ejb
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			stkEJB.remove();
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
		Integer lotID = new Integer(req.getParameter("whsPkid"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.remarks = "warehouse_mgt: delete-stock";
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

public class DoDeleteLot implements Action
{
	private String strClassName = "DoDeleteLot";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-lot-delete-1-page";
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
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("deleteLot"))
		{
			// first, create the lot,
			String strErrMsg = fnDeleteWarehouseStock(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to delete the LOT .... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-lot-delete-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-lot-delete-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnDeleteWarehouseStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			Vector vecDeltas = StockDeltaNut.getValueObjectsGiven(whsObj.stock_id, StockDeltaBean.STATUS_ACTIVE);
			// / delete all stock movements
			for (int cnt1 = 0; cnt1 < vecDeltas.size(); cnt1++)
			{
				StockDeltaObject sdObj = (StockDeltaObject) vecDeltas.get(cnt1);
				StockDelta sdEJB = StockDeltaNut.getHandle(sdObj.pkid);
				sdEJB.remove();
			}
			// / delete the stock opening
			Collection colOpen = StockOpeningNut.getCollectionByField(StockOpeningBean.STOCKID, whsObj.stock_id
					.toString());
			Iterator itrOpen = colOpen.iterator();
			while (itrOpen.hasNext())
			{
				StockOpening stkOpen = (StockOpening) itrOpen.next();
				stkOpen.remove();
			}
			// / delete the warehouse stock
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			try
			{
				whsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				return ex.getMessage();
			}
			// / delete the stock ejb
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			stkEJB.remove();
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
		Integer lotID = new Integer(req.getParameter("whsPkid"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.remarks = "warehouse_mgt: delete-stock";
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

public class DoDeleteLot implements Action
{
	private String strClassName = "DoDeleteLot";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		String fwdPage = req.getParameter("fwdPage");
		if (fwdPage == null)
		{
			fwdPage = "whm-lot-delete-1-page";
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
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			} else
			{
				fwdPage = "whm-lot-delete-1-page";
				return new ActionRouter(fwdPage);
			}
		} // end of selectLot
		// //----------------------------------------
		if (formName.equals("deleteLot"))
		{
			// first, create the lot,
			String strErrMsg = fnDeleteWarehouseStock(servlet, req, res);
			if (strErrMsg != null)
			{
				// if not successful, return to the previous page
				// however, remember the values filled in previously
				strErrMsg += " Unable to delete the LOT .... ";
				req.setAttribute("strErrMsg", strErrMsg);
				return new ActionRouter("whm-lot-delete-2-page");
			} else
			{
				// if successfull create the stock movement
				return new ActionRouter("whm-lot-delete-2-page");
			}
		}
		// //----------------------------------------
		return new ActionRouter(fwdPage);
	}

	// //////////////////////////////////////////////////////////
	public String fnDeleteWarehouseStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strErrMsg = null;
		try
		{
			// / first, get the whsObject
			Integer whsPkid = new Integer(req.getParameter("whsPkid"));
			WarehouseStockObject whsObj = WarehouseStockNut.getObject(whsPkid);
			req.setAttribute("whsObj", whsObj);
			Vector vecDeltas = StockDeltaNut.getValueObjectsGiven(whsObj.stock_id, StockDeltaBean.STATUS_ACTIVE);
			// / delete all stock movements
			for (int cnt1 = 0; cnt1 < vecDeltas.size(); cnt1++)
			{
				StockDeltaObject sdObj = (StockDeltaObject) vecDeltas.get(cnt1);
				StockDelta sdEJB = StockDeltaNut.getHandle(sdObj.pkid);
				sdEJB.remove();
			}
			// / delete the stock opening
			Collection colOpen = StockOpeningNut.getCollectionByField(StockOpeningBean.STOCKID, whsObj.stock_id
					.toString());
			Iterator itrOpen = colOpen.iterator();
			while (itrOpen.hasNext())
			{
				StockOpening stkOpen = (StockOpening) itrOpen.next();
				stkOpen.remove();
			}
			// / delete the warehouse stock
			WarehouseStock whsEJB = WarehouseStockNut.getHandle(whsPkid);
			try
			{
				whsEJB.remove();
			} catch (Exception ex)
			{
				ex.printStackTrace();
				return ex.getMessage();
			}
			// / delete the stock ejb
			Stock stkEJB = StockNut.getHandle(whsObj.stock_id);
			stkEJB.remove();
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
		Integer lotID = new Integer(req.getParameter("whsPkid"));
		if (iUserId != null)
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = iUserId;
			atObj.auditType = AuditTrailBean.TYPE_TXN;
			atObj.remarks = "warehouse_mgt: delete-stock";
			AuditTrailNut.fnCreate(atObj);
		}
	}
}
