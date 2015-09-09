/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;

import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoRecordOwnGift extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-popup-record-own-gift-page");
		}

		if(formName.equals("recordOwnCardGift"))
		{
			fnRecordOwnCardGift(servlet,req,res);
		}

		if(formName.equals("trackOwnCardGift"))
		{
			fnTrackOwnCardGift(servlet,req,res);
		}

		return new ActionRouter("dist-popup-record-own-gift-page");
	}

	private void fnRecordOwnCardGift(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();

		String hasOwnCardGift = req.getParameter("hasOwnCardGift");
		String receivedAtBranch = req.getParameter("receivedAtBranch");
		String cardGiftReceiveBy = req.getParameter("cardGiftReceiveBy");
		String cardGiftKeptAt = req.getParameter("cardGiftKeptAt");
		String cardGiftDesc = req.getParameter("cardGiftDesc");
		String orderNo = req.getParameter("orderNo");
		
		cardGiftKeptAt = cardGiftKeptAt + " DESC: " + cardGiftDesc;
		
		Integer iBranch = new Integer(receivedAtBranch);
		BranchObject branch = BranchNut.getObject(iBranch);
		Integer iUser = new Integer(cardGiftReceiveBy);
		UserObject user = UserNut.getObject(iUser);
		Long lOrder = new Long(orderNo);
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lOrder);

		if(branch==null || user==null || soObj==null)
		{ return;}

		String documentTrail = "";
		documentTrail += "<br>GIFT-TYPE: <font color='blue'>"+hasOwnCardGift+"</font> ";
		documentTrail += "<br>RECEIVED-AT-BRANCH: <font color='blue'>"+branch.description+"</font> ";
		documentTrail += "<br>RECEIVED-BY:<font color='blue'>"+user.userName+"</font> ";
		documentTrail += "<br>KEPT-AT:<font color='blue'>"+cardGiftKeptAt+"</font>";

		DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
      dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
      dpiObj.processType = "UPDATE-ORDER";
      dpiObj.category = "CARD-GIFT-RECORD";
      dpiObj.auditLevel = new Integer(0);
      dpiObj.processId = new Long(0);
      dpiObj.userid = (Integer) session.getAttribute("userId");
      dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
      dpiObj.docId = soObj.pkid;
      dpiObj.entityRef = CustAccountBean.TABLENAME;
      dpiObj.entityId = soObj.senderKey1;
      dpiObj.description1 = documentTrail;
      dpiObj.description2 = "";
      dpiObj.remarks = "";
      dpiObj.time = TimeFormat.getTimestamp();
      DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
	
		//// check if the sales order
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
         {
            ItemObject itmObj = ItemNut.getValueObjectByCode("OWN-GIFT");
            if (itmObj == null)
            {
					itmObj = new ItemObject();
					itmObj.code = "OWN-GIFT";
					itmObj.name = "OWN CARD / GIFT ";
					ItemNut.fnCreate(itmObj);
            }

            Integer userId = (Integer) session.getAttribute("userId");
            // / check if template id exists, if it doesn't,create a new ones..
            try
            {
               DocRow docrow = new DocRow();
               docrow.setTemplateId(itmObj.categoryId.intValue());
               docrow.setItemType(itmObj.itemType1);
               docrow.setItemId(itmObj.pkid);
               docrow.setItemCode(itmObj.code);
               docrow.setItemName(itmObj.name);
               docrow.setSerialized(itmObj.serialized);
               docrow.setQty(new BigDecimal(1));
               docrow.setCcy1(branch.currency);
               docrow.setPrice1(itmObj.priceList);
               docrow.setCommission1(itmObj.commissionPctSales1);
               docrow.setDiscount(new BigDecimal(0));
               docrow.user1 = userId.intValue();
               docrow.setRemarks(cardGiftKeptAt);
               docrow.setDescription(itmObj.description);
               docrow.setCcy2("");
               docrow.setPrice2(new BigDecimal(0));
               Timestamp tsNow = TimeFormat.getTimestamp();
               docrow.setProductionRequired(itmObj.productionRequired);
               docrow.setDeliveryRequired(itmObj.deliveryRequired);
               csos.fnAddStockWithItemCode(docrow);
            } catch (Exception ex)
            {
               ex.printStackTrace();
            }
         }// / end while
		
		req.setAttribute("closeAndRefreshParent","true");
	}

	private void fnTrackOwnCardGift(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();

		String orderNo = req.getParameter("orderNo");
		String giftHandedToDeliveryMan = req.getParameter("giftHandedToDeliveryMan");
		String giftHandedTime = req.getParameter("giftHandedTime");
		String giftReceivedBy = req.getParameter("giftReceivedBy");
		String giftReceivedTime = req.getParameter("giftReceivedTime");

		Long lOrder = new Long(orderNo);
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lOrder);

		String documentTrail = " ";
		documentTrail += " DELIVERY-MAN:<font color='blue'>"+giftHandedToDeliveryMan+"</font>";
		documentTrail += " DELIVERY-MAN-TIME:<font color='blue'>"+giftHandedTime+"</font>";
		documentTrail += " GIFT-RECEIVED-BY:<font color='blue'>"+giftReceivedBy+"</font>";
		documentTrail += " GIFT-RECEIVED-TIME:<font color='blue'>"+giftReceivedTime+"</font>";

      DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
      dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
      dpiObj.processType = "UPDATE-ORDER";
      dpiObj.category = "CARD-GIFT-TRACE";
      dpiObj.auditLevel = new Integer(0);
      dpiObj.processId = new Long(0);
      dpiObj.userid = (Integer) session.getAttribute("userId");
      dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
      dpiObj.docId = soObj.pkid;
      dpiObj.entityRef = CustAccountBean.TABLENAME;
      dpiObj.entityId = soObj.senderKey1;
      dpiObj.description1 = documentTrail;
      dpiObj.description2 = "";
      dpiObj.remarks = "";
      dpiObj.time = TimeFormat.getTimestamp();
      DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);

		req.setAttribute("closeOnly","true");

	}

}



/*==========================================================
 *
 * Copyright © of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.distribution;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.*;
import java.sql.*;

import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.util.*;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;

public class DoRecordOwnGift extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		req.setAttribute("orderNo",req.getParameter("orderNo"));

		if(formName==null)
		{
			return new ActionRouter("dist-popup-record-own-gift-page");
		}

		if(formName.equals("recordOwnCardGift"))
		{
			fnRecordOwnCardGift(servlet,req,res);
		}

		if(formName.equals("trackOwnCardGift"))
		{
			fnTrackOwnCardGift(servlet,req,res);
		}

		return new ActionRouter("dist-popup-record-own-gift-page");
	}

	private void fnRecordOwnCardGift(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();

		String hasOwnCardGift = req.getParameter("hasOwnCardGift");
		String receivedAtBranch = req.getParameter("receivedAtBranch");
		String cardGiftReceiveBy = req.getParameter("cardGiftReceiveBy");
		String cardGiftKeptAt = req.getParameter("cardGiftKeptAt");
		String orderNo = req.getParameter("orderNo");

		Integer iBranch = new Integer(receivedAtBranch);
		BranchObject branch = BranchNut.getObject(iBranch);
		Integer iUser = new Integer(cardGiftReceiveBy);
		UserObject user = UserNut.getObject(iUser);
		Long lOrder = new Long(orderNo);
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lOrder);

		if(branch==null || user==null || soObj==null)
		{ return;}

		String documentTrail = "";
		documentTrail += "<br>GIFT-TYPE: <font color='blue'>"+hasOwnCardGift+"</font> ";
		documentTrail += "<br>RECEIVED-AT-BRANCH: <font color='blue'>"+branch.description+"</font> ";
		documentTrail += "<br>RECEIVED-BY:<font color='blue'>"+user.userName+"</font> ";
		documentTrail += "<br>KEPT-AT:<font color='blue'>"+cardGiftKeptAt+"</font>";

		DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
      dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
      dpiObj.processType = "UPDATE-ORDER";
      dpiObj.category = "CARD-GIFT-RECORD";
      dpiObj.auditLevel = new Integer(0);
      dpiObj.processId = new Long(0);
      dpiObj.userid = (Integer) session.getAttribute("userId");
      dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
      dpiObj.docId = soObj.pkid;
      dpiObj.entityRef = CustAccountBean.TABLENAME;
      dpiObj.entityId = soObj.senderKey1;
      dpiObj.description1 = documentTrail;
      dpiObj.description2 = "";
      dpiObj.remarks = "";
      dpiObj.time = TimeFormat.getTimestamp();
      DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);
	
		//// check if the sales order
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
         {
            ItemObject itmObj = ItemNut.getValueObjectByCode("OWN-GIFT");
            if (itmObj == null)
            {
					itmObj = new ItemObject();
					itmObj.code = "OWN-GIFT";
					itmObj.name = "OWN CARD / GIFT ";
					ItemNut.fnCreate(itmObj);
            }

            Integer userId = (Integer) session.getAttribute("userId");
            // / check if template id exists, if it doesn't,create a new ones..
            try
            {
               DocRow docrow = new DocRow();
               docrow.setTemplateId(itmObj.categoryId.intValue());
               docrow.setItemType(itmObj.itemType1);
               docrow.setItemId(itmObj.pkid);
               docrow.setItemCode(itmObj.code);
               docrow.setItemName(itmObj.name);
               docrow.setSerialized(itmObj.serialized);
               docrow.setQty(new BigDecimal(1));
               docrow.setCcy1(branch.currency);
               docrow.setPrice1(itmObj.priceList);
               docrow.setCommission1(itmObj.commissionPctSales1);
               docrow.setDiscount(new BigDecimal(0));
               docrow.user1 = userId.intValue();
               docrow.setRemarks(cardGiftKeptAt);
               docrow.setDescription(itmObj.description);
               docrow.setCcy2("");
               docrow.setPrice2(new BigDecimal(0));
               Timestamp tsNow = TimeFormat.getTimestamp();
               docrow.setProductionRequired(itmObj.productionRequired);
               docrow.setDeliveryRequired(itmObj.deliveryRequired);
               csos.fnAddStockWithItemCode(docrow);
            } catch (Exception ex)
            {
               ex.printStackTrace();
            }
         }// / end while
		
		req.setAttribute("closeAndRefreshParent","true");
	}

	private void fnTrackOwnCardGift(HttpServlet servlet, HttpServletRequest req,
											HttpServletResponse res)
	{
		HttpSession session = req.getSession();

		String orderNo = req.getParameter("orderNo");
		String giftHandedToDeliveryMan = req.getParameter("giftHandedToDeliveryMan");
		String giftHandedTime = req.getParameter("giftHandedTime");
		String giftReceivedBy = req.getParameter("giftReceivedBy");
		String giftReceivedTime = req.getParameter("giftReceivedTime");

		Long lOrder = new Long(orderNo);
		SalesOrderIndexObject soObj = SalesOrderIndexNut.getObject(lOrder);

		String documentTrail = " ";
		documentTrail += " DELIVERY-MAN:<font color='blue'>"+giftHandedToDeliveryMan+"</font>";
		documentTrail += " DELIVERY-MAN-TIME:<font color='blue'>"+giftHandedTime+"</font>";
		documentTrail += " GIFT-RECEIVED-BY:<font color='blue'>"+giftReceivedBy+"</font>";
		documentTrail += " GIFT-RECEIVED-TIME:<font color='blue'>"+giftReceivedTime+"</font>";

      DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
      dpiObj.module = DocumentProcessingItemBean.MODULE_DISTRIBUTION;
      dpiObj.processType = "UPDATE-ORDER";
      dpiObj.category = "CARD-GIFT-TRACE";
      dpiObj.auditLevel = new Integer(0);
      dpiObj.processId = new Long(0);
      dpiObj.userid = (Integer) session.getAttribute("userId");
      dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
      dpiObj.docId = soObj.pkid;
      dpiObj.entityRef = CustAccountBean.TABLENAME;
      dpiObj.entityId = soObj.senderKey1;
      dpiObj.description1 = documentTrail;
      dpiObj.description2 = "";
      dpiObj.remarks = "";
      dpiObj.time = TimeFormat.getTimestamp();
      DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.fnCreate(dpiObj);

		req.setAttribute("closeOnly","true");

	}

}



