/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.bean.procurement.ReceiveStockSession;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemEdit implements Action
{
	private String strClassName = "DoInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		
		
		Log.printVerbose("formName: " + formName);
		if (formName == null)
		{
			return new ActionRouter("inv-setup-edit-item-page");
		}
		if (formName.equals("selectItem"))
		{
			try
			{
				fnSelectItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		// TKW20070402: Allow user to select supplier(s) for an item.
		if (formName.equals("setSupplier"))
		{
			try
			{
				setSupplier(servlet, req, res);
				fnSelectItem(servlet,req,res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}	
		if (formName.equals("removeSupplier"))
		{
			try
			{
				removeSupplier(servlet, req, res);
				fnSelectItem(servlet,req,res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}	
		if (formName.equals("choosePreferredSupplier"))
		{
			try
			{
				setPreferredSupplier(servlet, req, res);
				fnSelectItem(servlet,req,res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}			
		// End TKW20070402
		if (formName.equals("editItem"))
		{
			try
			{
				fnEditItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("editOtherDetails"))
		{
			try
			{
				fnEditOtherDetails(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("resetPricing"))
		{
			try
			{
				fnResetPricing(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
			
						
		}

		if (formName.equals("removePseudo"))
		{
			try
			{
				fnRemovePseudo(servlet,req,res);
				fnSelectItem(servlet,req,res);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}

		}

		return new ActionRouter("inv-setup-edit-item-page");
	}

	protected void fnRemovePseudo(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String bomLinkPkid = req.getParameter("bomLinkPkid");
		try
		{
			Integer iBomLink = new Integer(bomLinkPkid);
			BOMLink bomLinkEJB = BOMLinkNut.getHandle(iBomLink);
			bomLinkEJB.remove();
		}
		catch(Exception ex)
		{
			throw new Exception("Unable to remove Pseudo Code");
		}
	}

	// TKW20070402: Allow user to select supplier(s) for an item.
	public void setSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			HttpSession session = req.getSession();
			String strSuppAcc = (String) req.getParameter("suppPkid");
			Integer iItemId = new Integer((String) session.getAttribute("suppItemPkid"));;
			Log.printVerbose("First, setSupplier: " + iItemId.toString());
			Long lSuppAcc = new Long(strSuppAcc);
			Integer iSuppAcc = new Integer(strSuppAcc);
			SuppAccountObject suppAccObj = SuppAccountNut.getObject(iSuppAcc);
			
			if (suppAccObj == null)
			{
				throw new Exception(" No such supplier!! ");
			}
		
			SuppAccountItemLinkObject sailObj = new SuppAccountItemLinkObject();
			sailObj.account = iSuppAcc;
			sailObj.itemid = iItemId;
			sailObj.priority = new Integer(0);
			SuppAccountItemLinkNut.fnCreate(sailObj);
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Invalid Supplier PKID: " + ex.getMessage());
		}
	}	
	
	public void removeSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			String strPkid = req.getParameter("targetSuppPkid");
			Long pkid = new Long(strPkid);
			SuppAccountItemLink itemSuppEJB = SuppAccountItemLinkNut.getHandle(pkid);
			if (itemSuppEJB != null)
			{
				itemSuppEJB.remove();
			}				
		} catch (Exception ex)
		{
			throw new Exception("Invalid Supplier PKID: " + ex.getMessage());
		}
	}	
	
	public void setPreferredSupplier(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		try
		{
			HttpSession session = req.getSession();
			String itemCode = req.getParameter("itemCode");
			if(itemCode!=null){
				session.setAttribute("editItemCode",itemCode.trim());
			}
			Item itemEJB = ItemNut.getObjectByCode((String) session.getAttribute("editItemCode"));
			if (itemEJB == null)
			{
				throw new Exception("Item code does not exist in the database!");
			}

			ItemObject itemObj = itemEJB.getObject();

			
			
			String strPkid = req.getParameter("targetSuppPkid");
			Long pkid = new Long(strPkid);
			SuppAccountItemLinkObject sailObj = SuppAccountItemLinkNut.getObject(pkid);
			SuppAccountItemLinkObject otherSailObj;
			SuppAccountItemLink itemSuppEJB = SuppAccountItemLinkNut.getHandle(pkid);

			// TKW 20070404: Set preferred supplier field in item object.
			Log.printVerbose("itemid: " + strPkid + "-supplier: " + itemObj.preferredSupplier);
			itemObj.preferredSupplier = new Integer(strPkid);
			itemEJB.setObject(itemObj);
			
			QueryObject query = new QueryObject(new String[]{"itemid = '" + itemObj.pkid.toString() +"'"}) ;
			Vector vecSupplierList = new Vector();
			if(pkid.toString()!=null)
			{
				vecSupplierList = new Vector(SuppAccountItemLinkNut.getObjects(query));
			}
			if (sailObj != null)
			{
				if(itemSuppEJB != null)
				{
					if(vecSupplierList.size()>0)
					{
						// TKW20070404: First, reset the priority back to 0 for all suppliers belonging to the item.
						for(int i=0;i<vecSupplierList.size();i++)
						{
							
							otherSailObj = (SuppAccountItemLinkObject) vecSupplierList.get(i);
							Log.printVerbose("Resetting: " + otherSailObj.pkid);
							otherSailObj.priority = new Integer(0);
							SuppAccountItemLink itemSuppEJB2 = SuppAccountItemLinkNut.getHandle(otherSailObj.pkid);
							itemSuppEJB2.setObject(otherSailObj);
							
						}
						// TKW 20070404: Then set the preferred supplier's priority to 1.
						Log.printVerbose("Set preferred supplier to: " + sailObj.pkid);
						sailObj.priority = new Integer(1);
						itemSuppEJB.setObject(sailObj);
		
					}
				}
			}					
						
		} catch (Exception ex)
		{
			throw new Exception("Invalid Supplier PKID: " + ex.getMessage());
		}
	}		
	// End TKW20070402
	
	protected void fnSelectItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String itemCode = req.getParameter("itemCode");
		System.out.println("TUPPENY:DoInvItemEdit::fnSelectItem: itemCode is " + itemCode);
		if(itemCode!=null){
			session.setAttribute("editItemCode",itemCode.trim());
		}
		Item itemEJB = ItemNut.getObjectByCode((String) session.getAttribute("editItemCode"));
		if (itemEJB == null)
		{
			throw new Exception("Item code does not exist in the database!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			req.setAttribute("itemObj", itemObj);
			
			
			/// retrieve the pseudo codes
			if(itemObj!=null)
			{
				session.setAttribute("suppItemPkid",itemObj.pkid.toString());
				
				QueryObject query = new QueryObject(new String[]{"itemid = '" + itemObj.pkid.toString() +"'"}) ;
				query.setOrder(" ORDER BY priority DESC");
				Vector vecSupplierList = new Vector();
				if(itemObj.pkid.toString()!=null)
				{
					vecSupplierList = new Vector(SuppAccountItemLinkNut.getObjects(query));
				}
				session.setAttribute("vecSupplierList",vecSupplierList);
				
				QueryObject queryPseudo = new QueryObject(new String[]{
						BOMLinkBean.CHILD_ITEM_ID +" = '"+itemObj.pkid.toString()+"' "
											});
				queryPseudo.setOrder(" ORDER BY "+BOMLinkBean.SORT + " DESC , "+BOMLinkBean.DISPLAY_CODE + " ASC");
				Vector vecPseudoItem = new Vector(BOMLinkNut.getObjects(queryPseudo));
				req.setAttribute("vecPseudoItem", vecPseudoItem);
			}

		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnEditItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String strPkid = req.getParameter("pkid");
		String strIsOverwritePrices = req.getParameter("chkOverwritePrices"); // TKW20070410: Added to allow overwriting of pseudocode prices if main prices are modified.
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String itemCode = (String) req.getParameter("itemCode");
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
		if (!itemObj.code.equals(itemCode))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-code" + itemObj.code + " => " + itemCode;
			AuditTrailNut.fnCreate(atObj);
		}
/*		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);*/
		// TKW20070228: Commented the lines above and replaced with the line below to stop spaces being stripped.
		itemCode = StringManup.convertUpper(itemCode);
		
		itemCode = StringManup.removeSymbolsExceptSpace(itemCode);
		// End TKW20070228
		itemObj.code = itemCode.trim();
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		if (itemEJB2 != null && !itemEJB2.getPkid().equals(pkid))
		{
			throw new Exception("The item code exists in the database!! ");
		}
		
		String itemName = (String) req.getParameter("itemName");
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 999)
		{
			throw new Exception("Item Name must not have more than 1000 characters!! ");
		}
		if (!itemObj.name.equals(itemName))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-name " + itemObj.name + " => " + itemName;
			AuditTrailNut.fnCreate(atObj);
		}
		
		String serialized = req.getParameter("serialized");
		
		// 20080129 Jimmy
		String itemSerialized = (itemObj.serialized)?"true":"false";
		String remark = (serialized.equals("true"))?"Serial Number Support":"No Serial Number";
		if (!serialized.equals(itemSerialized))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "change-item-serialized: " + itemObj.name + " => " + remark;
			AuditTrailNut.fnCreate(atObj);
		}
		
		// End
			
		if (serialized.equals("true"))
		{
			itemObj.serialized = true;
		} else
		{
			itemObj.serialized = false;
		}
		
			
		itemObj.name = itemName;
		String catId = req.getParameter("itemCategoryId");
		itemObj.categoryId = new Integer(catId);
		itemObj.category1 = req.getParameter("itemCategory1");
		itemObj.category2 = req.getParameter("itemCategory2");
		itemObj.category3 = req.getParameter("itemCategory3");
		itemObj.category4 = req.getParameter("itemCategory4");
		itemObj.category5 = req.getParameter("itemCategory5");
		itemObj.itemType1 = req.getParameter("itemType1");
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		
		try
		{
			itemEJB.setObject(itemObj);
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = " item modified: " + itemName;
			atObj.tc_action = AuditTrailBean.TC_ACTION_UPDATE;
			atObj.tc_entity_id = itemEJB.getPkid();
			atObj.tc_entity_table = ItemBean.TABLENAME;
			AuditTrailNut.fnCreate(atObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);

         if(itemObj!=null)
         {
            QueryObject queryPseudo = new QueryObject(new String[]{
                  BOMLinkBean.CHILD_ITEM_ID +" = '"+itemObj.pkid.toString()+"' "
                                 });
            queryPseudo.setOrder(" ORDER BY "+BOMLinkBean.DISPLAY_CODE+ " , "+BOMLinkBean.DISPLAY_NAME);
            Vector vecPseudoItem = new Vector(BOMLinkNut.getObjects(queryPseudo));
            req.setAttribute("vecPseudoItem", vecPseudoItem);
         }
     	

	}

	protected void fnEditOtherDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		itemObj.eanCode = eanCode.trim();
		itemObj.upcCode = upcCode.trim();
		itemObj.isbnCode = isbnCode.trim();
		itemObj.reserved1 = req.getParameter("reserved1");
		itemObj.reserved2 = req.getParameter("reserved2");
		
		//itemObj.status = req.getParameter("status");
		
		// TKW20070705: Saving department code info.
		itemObj.codeDepartment = req.getParameter("codeDepartment");		
		
		
		String status = req.getParameter("status");
		

		if (!status.equals(itemObj.status))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "change-status: " + itemObj.code + " from " + itemObj.status + " => " + status;
			AuditTrailNut.fnCreate(atObj);
		}
		
		itemObj.status = req.getParameter("status");

		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null && !eanItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null && !upcItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null && isbnItmObj.equals(itemObj.pkid))
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		itemObj.description = (String) req.getParameter("itemDescription");
		itemObj.remarks1 = req.getParameter("remarks1");
		itemObj.remarks2 = req.getParameter("remarks2");
		itemObj.keywords = req.getParameter("keywords");
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{ throw new Exception("Unit of measure must have at least 2 characters!! "); } 
		else
		{ itemObj.uom = itemUOM.toUpperCase(); }


		itemObj.weight = new BigDecimal(req.getParameter("weight"));
		itemObj.length = new BigDecimal(req.getParameter("length"));
		itemObj.width = new BigDecimal(req.getParameter("width"));
		itemObj.depth = new BigDecimal(req.getParameter("depth"));
		itemObj.thresholdQtyReorder = new BigDecimal(req.getParameter("qtyReorder"));
		itemObj.thresholdQtyMaxQty = new BigDecimal(req.getParameter("qtyMaxQty"));
		itemObj.leadTime = new Long(req.getParameter("qtyLeadTimeQty"));
		itemObj.minOrderQty = new BigDecimal(req.getParameter("qtyMinQty"));		
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			itemObj.productionRequired = true;
		} else
		{
			itemObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			itemObj.deliveryRequired = true;
		} else
		{
			itemObj.deliveryRequired = false;
		}
		
		// 20080522 Jimmy
		itemObj.outQty = new BigDecimal(req.getParameter("outQty"));
		itemObj.outUnit = req.getParameter("outUnit");
		itemObj.inQty = new BigDecimal(req.getParameter("inQty"));
		itemObj.inUnit = req.getParameter("inUnit");
		itemObj.innQty = new BigDecimal(req.getParameter("innQty"));
		itemObj.innUnit = req.getParameter("innUnit");
		itemObj.inmQty = new BigDecimal(req.getParameter("inmQty"));
		itemObj.inmUnit = req.getParameter("inmUnit");
		
		if (itemObj.inmQty.intValue() > 0) {
			itemObj.uom = itemObj.inmUnit;
		}else if (itemObj.innQty.intValue() > 0) {
			itemObj.uom = itemObj.innUnit;
		}else if (itemObj.inQty.intValue() > 0) {
			itemObj.uom = itemObj.inUnit;
		}
		
		//HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);

         if(itemObj!=null)
         {
            QueryObject queryPseudo = new QueryObject(new String[]{
                  BOMLinkBean.CHILD_ITEM_ID +" = '"+itemObj.pkid.toString()+"' "
                                 });
            queryPseudo.setOrder(" ORDER BY "+BOMLinkBean.DISPLAY_CODE+ " , "+BOMLinkBean.DISPLAY_NAME);
            Vector vecPseudoItem = new Vector(BOMLinkNut.getObjects(queryPseudo));
            req.setAttribute("vecPseudoItem", vecPseudoItem);
         }


	}

	protected void fnResetPricing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		String pricelist = (String)req.getParameter("priceList");
		String priceMin = (String)req.getParameter("priceMin");
		
		
		// TKW20070504: Find out if user selected chkOverwritePrices.
		String strIsOverwritePrices = "";
		strIsOverwritePrices = req.getParameter("chkOverwritePrices");
		
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		
		//if (priceMin.compareTo(pricelist)> 0)
		//{
//			throw new Exception("The List Price must be greater than the minimum price!! ");
		//}
		
		// // storing old values
		BigDecimal priceList = itemObj.priceList;
		BigDecimal priceSale = itemObj.priceSale;
		BigDecimal priceDisc1 = itemObj.priceDisc1;
		// BigDecimal maUnitCost = itemObj.maUnitCost;
		BigDecimal lastUnitCost = itemObj.lastUnitCost;
		BigDecimal rebate1Price = itemObj.rebate1Price;
		BigDecimal rebate1Pct = itemObj.rebate1Pct;
		BigDecimal deltaPriceRetailAmt = itemObj.deltaPriceRetailAmt;
		BigDecimal deltaPriceDealerAmt = itemObj.deltaPriceDealerAmt;
		BigDecimal deltaPriceOutletAmt = itemObj.deltaPriceOutletAmt;
		itemObj.priceList = new BigDecimal(req.getParameter("priceList"));
		itemObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		itemObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		itemObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		itemObj.priceDisc3 = new BigDecimal(req.getParameter("priceDisc3"));
		// itemObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		itemObj.lastUnitCost = new BigDecimal(req.getParameter("lastUnitCost"));
		itemObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		itemObj.priceMin = new BigDecimal(req.getParameter("priceMin"));
		//itemObj.reserved1 = req.getParameter("reserved1");
		itemObj.deltaPriceRetailAmt = new BigDecimal(req.getParameter("deltaPriceRetailAmt"));
		itemObj.deltaPriceDealerAmt = new BigDecimal(req.getParameter("deltaPriceDealerAmt"));
		itemObj.deltaPriceOutletAmt = new BigDecimal(req.getParameter("deltaPriceOutletAmt"));
		itemObj.tax_rate = new BigDecimal(req.getParameter("taxRate"));
		itemObj.tax_option = req.getParameter("taxOption").toString();
		String rebateMethod = req.getParameter("rebateMethod");
		
		
		
		
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			try
			{
				itemObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
				itemObj.rebate1Pct = itemObj.rebate1Pct.divide(new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN);
				itemObj.rebate1Price = itemObj.priceList.multiply(itemObj.rebate1Pct);
				itemObj.rebate1Price = new BigDecimal(CurrencyFormat.strCcy(itemObj.rebate1Price));
			} catch (Exception ex)
			{
			}
		} else
		{
			itemObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (itemObj.priceList.signum() > 0)
			{
				itemObj.rebate1Pct = itemObj.rebate1Price.divide(itemObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		itemObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		itemObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		Log.printVerbose(" REBATE PRICE = " + itemObj.rebate1Price.toString());
		Log.printVerbose(" REBATE START = " + itemObj.rebate1Start.toString());
		Log.printVerbose(" REBATE END  = " + itemObj.rebate1End.toString());
		HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
			// // audit trail!!
			boolean criticalChange = false;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = itemObj.userIdUpdate;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: " + itemObj.code + " ";
			if (priceList.compareTo(itemObj.priceList) != 0)
			{
				criticalChange = true;
				atObj.remarks += " ListPrice:" + CurrencyFormat.strCcy(priceList) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceList) + " ";
			}
			Log.printVerbose(" OLD / NEW SALE PRICE = " + priceSale.toString() + " / " + itemObj.priceSale.toString());
			if (priceSale.compareTo(itemObj.priceSale) != 0)
			{
				criticalChange = true;
				atObj.remarks += " SalePrice:" + CurrencyFormat.strCcy(priceSale) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceSale) + " ";
			}
			// Log.printVerbose(" OLD / NEW MA PRICE = "+maUnitCost.toString()
			// + " / "+ itemObj.maUnitCost.toString());
			// if(maUnitCost.compareTo(itemObj.maUnitCost)!=0)
			// { criticalChange = true;
			// atObj.remarks += "
			// MovingAverage:"+CurrencyFormat.strCcy(maUnitCost)+"->"
			// +CurrencyFormat.strCcy(itemObj.maUnitCost)+" ";}
			if (lastUnitCost.compareTo(itemObj.lastUnitCost) != 0)
			{
				criticalChange = true;
				atObj.remarks += " LastCost:" + CurrencyFormat.strCcy(lastUnitCost) + "->"
						+ CurrencyFormat.strCcy(itemObj.lastUnitCost) + " ";
			}
			if (criticalChange)
			{
				AuditTrailNut.fnCreate(atObj);
			}
			Vector vecStock = new Vector(StockNut.getCollectionByField(StockBean.ITEMID, pkid.toString()));
			for (int cnt1 = 0; cnt1 < vecStock.size(); cnt1++)
			{
				Stock stkEJB = (Stock) vecStock.get(cnt1);
				StockObject stkObj = stkEJB.getObject();
				// stkObj.unitCostMA = itemObj.maUnitCost;
				stkObj.unitCostReplacement = itemObj.replacementUnitCost;
				stkObj.unitCostLast = itemObj.lastUnitCost;
				stkEJB.setObject(stkObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);

         if(itemObj!=null)
         {
            QueryObject queryPseudo = new QueryObject(new String[]{
                  BOMLinkBean.CHILD_ITEM_ID +" = '"+itemObj.pkid.toString()+"' "
                                 });
            queryPseudo.setOrder(" ORDER BY "+BOMLinkBean.DISPLAY_CODE+ " , "+BOMLinkBean.DISPLAY_NAME);
            Vector vecPseudoItem = new Vector(BOMLinkNut.getObjects(queryPseudo));
            req.setAttribute("vecPseudoItem", vecPseudoItem);
         }
         // TKW20070504: Replace list prices in pseudo items with main list price.
         Log.printVerbose("strIsOverwritePrices is " + strIsOverwritePrices);
         if(strIsOverwritePrices.equals("1"))
         {
        	 BigDecimal asd = itemObj.priceList;
				QueryObject query = new QueryObject(new String[]{
						BOMLinkBean.CHILD_ITEM_ID +" = '"+itemObj.pkid.toString()+"' "
											});
        	 Vector vecBOMLink = (Vector) BOMLinkNut.getObjects(query);
        	 Log.printVerbose("!!!OVERWRITE CONFIRMED. OBJECT SIZE: " + vecBOMLink.size());
        	 for(int i=0;i<vecBOMLink.size();i++)
        	 {
        		 BOMLinkObject blObj = (BOMLinkObject) vecBOMLink.get(i);
        		 if(blObj.parentItemCode.equals(""))
        		 {
            		 blObj.priceList = itemObj.priceList;
            		 blObj.lastupdate = TimeFormat.getTimestamp();
            		 BOMLink bomLinkEJB = BOMLinkNut.getHandle(blObj.pkid);
        			 Log.printVerbose("!!!This BOMLinkObject is " + blObj.displayCode);
            		 if (bomLinkEJB != null)
        			 {
        				 
        				 bomLinkEJB.setObject(blObj);    
        				 Log.printVerbose(blObj.displayCode + " is set.");
        			 }        			 
        		 }

        	 }  
        	 query.setOrder(" ORDER BY "+BOMLinkBean.DISPLAY_CODE+ " , "+BOMLinkBean.DISPLAY_NAME);
             Vector vecPseudoItem = new Vector(BOMLinkNut.getObjects(query));
             req.setAttribute("vecPseudoItem", vecPseudoItem);
         }

	}
}



/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemEdit implements Action
{
	private String strClassName = "DoInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-setup-edit-item-page");
		}
		if (formName.equals("selectItem"))
		{
			try
			{
				fnSelectItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("editItem"))
		{
			try
			{
				fnEditItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("editOtherDetails"))
		{
			try
			{
				fnEditOtherDetails(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("resetPricing"))
		{
			try
			{
				fnResetPricing(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		return new ActionRouter("inv-setup-edit-item-page");
	}

	protected void fnSelectItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String itemCode = req.getParameter("itemCode");
		Item itemEJB = ItemNut.getObjectByCode(itemCode.trim());
		Log.printVerbose("111111111111111111111111111111111111111111111111");
		if (itemEJB == null)
		{
			throw new Exception("Item code does not exist in the database!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			req.setAttribute("itemObj", itemObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnEditItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String itemCode = (String) req.getParameter("itemCode");
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
		if (!itemObj.code.equals(itemCode))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-code" + itemObj.code + " => " + itemCode;
			AuditTrailNut.fnCreate(atObj);
		}
		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);
		itemObj.code = itemCode.trim();
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		if (itemEJB2 != null && !itemEJB2.getPkid().equals(pkid))
		{
			throw new Exception("The item code exists in the database!! ");
		}
		String serialized = req.getParameter("serialized");
		if (serialized.equals("true"))
		{
			itemObj.serialized = true;
		} else
		{
			itemObj.serialized = false;
		}
		String itemName = (String) req.getParameter("itemName");
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 99)
		{
			throw new Exception("Item Name must not have more than 100 characters!! ");
		}
		if (!itemObj.name.equals(itemName))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-name " + itemObj.name + " => " + itemName;
			AuditTrailNut.fnCreate(atObj);
		}
		itemObj.name = itemName;
		String catId = req.getParameter("itemCategoryId");
		itemObj.categoryId = new Integer(catId);
		itemObj.category1 = req.getParameter("itemCategory1");
		itemObj.category2 = req.getParameter("itemCategory2");
		itemObj.category3 = req.getParameter("itemCategory3");
		itemObj.category4 = req.getParameter("itemCategory4");
		itemObj.category5 = req.getParameter("itemCategory5");
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}

	protected void fnEditOtherDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		itemObj.eanCode = eanCode.trim();
		itemObj.upcCode = upcCode.trim();
		itemObj.isbnCode = isbnCode.trim();
		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null && !eanItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null && !upcItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null && isbnItmObj.equals(itemObj.pkid))
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		itemObj.description = (String) req.getParameter("itemDescription");
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{ throw new Exception("Unit of measure must have at least 2 characters!! "); } 
		else
		{ itemObj.uom = itemUOM.toUpperCase(); }


		itemObj.weight = new BigDecimal(req.getParameter("weight"));
		itemObj.length = new BigDecimal(req.getParameter("length"));
		itemObj.width = new BigDecimal(req.getParameter("width"));
		itemObj.depth = new BigDecimal(req.getParameter("depth"));
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			itemObj.productionRequired = true;
		} else
		{
			itemObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			itemObj.deliveryRequired = true;
		} else
		{
			itemObj.deliveryRequired = false;
		}
		HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}

	protected void fnResetPricing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		// // storing old values
		BigDecimal priceList = itemObj.priceList;
		BigDecimal priceSale = itemObj.priceSale;
		BigDecimal priceDisc1 = itemObj.priceDisc1;
		// BigDecimal maUnitCost = itemObj.maUnitCost;
		BigDecimal lastUnitCost = itemObj.lastUnitCost;
		BigDecimal rebate1Price = itemObj.rebate1Price;
		BigDecimal rebate1Pct = itemObj.rebate1Pct;
		BigDecimal deltaPriceRetailAmt = itemObj.deltaPriceRetailAmt;
		BigDecimal deltaPriceDealerAmt = itemObj.deltaPriceDealerAmt;
		BigDecimal deltaPriceOutletAmt = itemObj.deltaPriceOutletAmt;
		itemObj.priceList = new BigDecimal(req.getParameter("priceList"));
		itemObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		itemObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		itemObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		itemObj.priceDisc3 = new BigDecimal(req.getParameter("priceDisc3"));
		// itemObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		itemObj.lastUnitCost = new BigDecimal(req.getParameter("lastUnitCost"));
		itemObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		itemObj.priceMin = new BigDecimal(req.getParameter("priceMin"));
		itemObj.reserved1 = req.getParameter("reserved1");
		itemObj.deltaPriceRetailAmt = new BigDecimal(req.getParameter("deltaPriceRetailAmt"));
		itemObj.deltaPriceDealerAmt = new BigDecimal(req.getParameter("deltaPriceDealerAmt"));
		itemObj.deltaPriceOutletAmt = new BigDecimal(req.getParameter("deltaPriceOutletAmt"));
		String rebateMethod = req.getParameter("rebateMethod");
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			try
			{
				itemObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
				itemObj.rebate1Pct = itemObj.rebate1Pct.divide(new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN);
				itemObj.rebate1Price = itemObj.priceList.multiply(itemObj.rebate1Pct);
				itemObj.rebate1Price = new BigDecimal(CurrencyFormat.strCcy(itemObj.rebate1Price));
			} catch (Exception ex)
			{
			}
		} else
		{
			itemObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (itemObj.priceList.signum() > 0)
			{
				itemObj.rebate1Pct = itemObj.rebate1Price.divide(itemObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		itemObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		itemObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		Log.printVerbose(" REBATE PRICE = " + itemObj.rebate1Price.toString());
		Log.printVerbose(" REBATE START = " + itemObj.rebate1Start.toString());
		Log.printVerbose(" REBATE END  = " + itemObj.rebate1End.toString());
		HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
			// // audit trail!!
			boolean criticalChange = false;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = itemObj.userIdUpdate;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: " + itemObj.code + " ";
			if (priceList.compareTo(itemObj.priceList) != 0)
			{
				criticalChange = true;
				atObj.remarks += " ListPrice:" + CurrencyFormat.strCcy(priceList) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceList) + " ";
			}
			Log.printVerbose(" OLD / NEW SALE PRICE = " + priceSale.toString() + " / " + itemObj.priceSale.toString());
			if (priceSale.compareTo(itemObj.priceSale) != 0)
			{
				criticalChange = true;
				atObj.remarks += " SalePrice:" + CurrencyFormat.strCcy(priceSale) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceSale) + " ";
			}
			// Log.printVerbose(" OLD / NEW MA PRICE = "+maUnitCost.toString()
			// + " / "+ itemObj.maUnitCost.toString());
			// if(maUnitCost.compareTo(itemObj.maUnitCost)!=0)
			// { criticalChange = true;
			// atObj.remarks += "
			// MovingAverage:"+CurrencyFormat.strCcy(maUnitCost)+"->"
			// +CurrencyFormat.strCcy(itemObj.maUnitCost)+" ";}
			if (lastUnitCost.compareTo(itemObj.lastUnitCost) != 0)
			{
				criticalChange = true;
				atObj.remarks += " LastCost:" + CurrencyFormat.strCcy(lastUnitCost) + "->"
						+ CurrencyFormat.strCcy(itemObj.lastUnitCost) + " ";
			}
			if (criticalChange)
			{
				AuditTrailNut.fnCreate(atObj);
			}
			Vector vecStock = new Vector(StockNut.getCollectionByField(StockBean.ITEMID, pkid.toString()));
			for (int cnt1 = 0; cnt1 < vecStock.size(); cnt1++)
			{
				Stock stkEJB = (Stock) vecStock.get(cnt1);
				StockObject stkObj = stkEJB.getObject();
				// stkObj.unitCostMA = itemObj.maUnitCost;
				stkObj.unitCostReplacement = itemObj.replacementUnitCost;
				stkObj.unitCostLast = itemObj.lastUnitCost;
				stkEJB.setObject(stkObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}
}
/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemEdit implements Action
{
	private String strClassName = "DoInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-setup-edit-item-page");
		}
		if (formName.equals("selectItem"))
		{
			try
			{
				fnSelectItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("editItem"))
		{
			try
			{
				fnEditItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("editOtherDetails"))
		{
			try
			{
				fnEditOtherDetails(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("resetPricing"))
		{
			try
			{
				fnResetPricing(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		return new ActionRouter("inv-setup-edit-item-page");
	}

	protected void fnSelectItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String itemCode = req.getParameter("itemCode");
		Item itemEJB = ItemNut.getObjectByCode(itemCode.trim());
		Log.printVerbose("111111111111111111111111111111111111111111111111");
		if (itemEJB == null)
		{
			throw new Exception("Item code does not exist in the database!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			req.setAttribute("itemObj", itemObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnEditItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String itemCode = (String) req.getParameter("itemCode");
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
		if (!itemObj.code.equals(itemCode))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-code" + itemObj.code + " => " + itemCode;
			AuditTrailNut.fnCreate(atObj);
		}
		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);
		itemObj.code = itemCode.trim();
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		if (itemEJB2 != null && !itemEJB2.getPkid().equals(pkid))
		{
			throw new Exception("The item code exists in the database!! ");
		}
		String serialized = req.getParameter("serialized");
		if (serialized.equals("true"))
		{
			itemObj.serialized = true;
		} else
		{
			itemObj.serialized = false;
		}
		String itemName = (String) req.getParameter("itemName");
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 99)
		{
			throw new Exception("Item Name must not have more than 100 characters!! ");
		}
		if (!itemObj.name.equals(itemName))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-name " + itemObj.name + " => " + itemName;
			AuditTrailNut.fnCreate(atObj);
		}
		itemObj.name = itemName;
		String catId = req.getParameter("itemCategoryId");
		itemObj.categoryId = new Integer(catId);
		itemObj.category1 = req.getParameter("itemCategory1");
		itemObj.category2 = req.getParameter("itemCategory2");
		itemObj.category3 = req.getParameter("itemCategory3");
		itemObj.category4 = req.getParameter("itemCategory4");
		itemObj.category5 = req.getParameter("itemCategory5");
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}

	protected void fnEditOtherDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		itemObj.eanCode = eanCode.trim();
		itemObj.upcCode = upcCode.trim();
		itemObj.isbnCode = isbnCode.trim();
		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null && !eanItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null && !upcItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null && isbnItmObj.equals(itemObj.pkid))
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		itemObj.description = (String) req.getParameter("itemDescription");
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{
			throw new Exception("Item Name must have at least 2 characters!! ");
		} else
		{
			itemObj.uom = itemUOM.toUpperCase();
		}
		itemObj.weight = new BigDecimal(req.getParameter("weight"));
		itemObj.length = new BigDecimal(req.getParameter("length"));
		itemObj.width = new BigDecimal(req.getParameter("width"));
		itemObj.depth = new BigDecimal(req.getParameter("depth"));
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			itemObj.productionRequired = true;
		} else
		{
			itemObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			itemObj.deliveryRequired = true;
		} else
		{
			itemObj.deliveryRequired = false;
		}
		HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}

	protected void fnResetPricing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		// // storing old values
		BigDecimal priceList = itemObj.priceList;
		BigDecimal priceSale = itemObj.priceSale;
		BigDecimal priceDisc1 = itemObj.priceDisc1;
		// BigDecimal maUnitCost = itemObj.maUnitCost;
		BigDecimal lastUnitCost = itemObj.lastUnitCost;
		BigDecimal rebate1Price = itemObj.rebate1Price;
		BigDecimal rebate1Pct = itemObj.rebate1Pct;
		BigDecimal priceMin = itemObj.priceMin;
		BigDecimal deltaPriceRetailAmt = itemObj.deltaPriceRetailAmt;
		BigDecimal deltaPriceDealerAmt = itemObj.deltaPriceDealerAmt;
		BigDecimal deltaPriceOutletAmt = itemObj.deltaPriceOutletAmt;
		itemObj.priceList = new BigDecimal(req.getParameter("priceList"));
		itemObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		itemObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		itemObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		// itemObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		itemObj.lastUnitCost = new BigDecimal(req.getParameter("lastUnitCost"));
		itemObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		itemObj.priceMin = new BigDecimal(req.getParameter("priceMin"));
		itemObj.deltaPriceRetailAmt = new BigDecimal(req.getParameter("deltaPriceRetailAmt"));
		itemObj.deltaPriceDealerAmt = new BigDecimal(req.getParameter("deltaPriceDealerAmt"));
		itemObj.deltaPriceOutletAmt = new BigDecimal(req.getParameter("deltaPriceOutletAmt"));
		String rebateMethod = req.getParameter("rebateMethod");
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			try
			{
				itemObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
				itemObj.rebate1Pct = itemObj.rebate1Pct.divide(new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN);
				itemObj.rebate1Price = itemObj.priceList.multiply(itemObj.rebate1Pct);
				itemObj.rebate1Price = new BigDecimal(CurrencyFormat.strCcy(itemObj.rebate1Price));
			} catch (Exception ex)
			{
			}
		} else
		{
			itemObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (itemObj.priceList.signum() > 0)
			{
				itemObj.rebate1Pct = itemObj.rebate1Price.divide(itemObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		itemObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		itemObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		Log.printVerbose(" REBATE PRICE = " + itemObj.rebate1Price.toString());
		Log.printVerbose(" REBATE START = " + itemObj.rebate1Start.toString());
		Log.printVerbose(" REBATE END  = " + itemObj.rebate1End.toString());
		HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
			// // audit trail!!
			boolean criticalChange = false;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = itemObj.userIdUpdate;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: " + itemObj.code + " ";
			if (priceList.compareTo(itemObj.priceList) != 0)
			{
				criticalChange = true;
				atObj.remarks += " ListPrice:" + CurrencyFormat.strCcy(priceList) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceList) + " ";
			}
			Log.printVerbose(" OLD / NEW SALE PRICE = " + priceSale.toString() + " / " + itemObj.priceSale.toString());
			if (priceSale.compareTo(itemObj.priceSale) != 0)
			{
				criticalChange = true;
				atObj.remarks += " SalePrice:" + CurrencyFormat.strCcy(priceSale) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceSale) + " ";
			}
			// Log.printVerbose(" OLD / NEW MA PRICE = "+maUnitCost.toString()
			// + " / "+ itemObj.maUnitCost.toString());
			// if(maUnitCost.compareTo(itemObj.maUnitCost)!=0)
			// { criticalChange = true;
			// atObj.remarks += "
			// MovingAverage:"+CurrencyFormat.strCcy(maUnitCost)+"->"
			// +CurrencyFormat.strCcy(itemObj.maUnitCost)+" ";}
			if (lastUnitCost.compareTo(itemObj.lastUnitCost) != 0)
			{
				criticalChange = true;
				atObj.remarks += " LastCost:" + CurrencyFormat.strCcy(lastUnitCost) + "->"
						+ CurrencyFormat.strCcy(itemObj.lastUnitCost) + " ";
			}
			if (criticalChange)
			{
				AuditTrailNut.fnCreate(atObj);
			}
			Vector vecStock = new Vector(StockNut.getCollectionByField(StockBean.ITEMID, pkid.toString()));
			for (int cnt1 = 0; cnt1 < vecStock.size(); cnt1++)
			{
				Stock stkEJB = (Stock) vecStock.get(cnt1);
				StockObject stkObj = stkEJB.getObject();
				// stkObj.unitCostMA = itemObj.maUnitCost;
				stkObj.unitCostReplacement = itemObj.replacementUnitCost;
				stkObj.unitCostLast = itemObj.lastUnitCost;
				stkEJB.setObject(stkObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}
}
/*==========================================================
 *
 * Copyright of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;

public class DoInvItemEdit implements Action
{
	private String strClassName = "DoInvItemEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Handlers
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("inv-setup-edit-item-page");
		}
		if (formName.equals("selectItem"))
		{
			try
			{
				fnSelectItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("editItem"))
		{
			try
			{
				fnEditItem(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("editOtherDetails"))
		{
			try
			{
				fnEditOtherDetails(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		if (formName.equals("resetPricing"))
		{
			try
			{
				fnResetPricing(servlet, req, res);
				return new ActionRouter("inv-setup-edit-item-page");
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("inv-setup-edit-item-page");
			}
		}
		return new ActionRouter("inv-setup-edit-item-page");
	}

	protected void fnSelectItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String itemCode = req.getParameter("itemCode");
		Item itemEJB = ItemNut.getObjectByCode(itemCode.trim());
		Log.printVerbose("111111111111111111111111111111111111111111111111");
		if (itemEJB == null)
		{
			throw new Exception("Item code does not exist in the database!");
		}
		try
		{
			ItemObject itemObj = itemEJB.getObject();
			req.setAttribute("itemObj", itemObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void fnEditItem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String itemCode = (String) req.getParameter("itemCode");
		if (itemCode == null || itemCode.length() < 3)
		{
			throw new Exception("Item Code must have at least 3 characters!! ");
		}
		if (!itemObj.code.equals(itemCode))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-code" + itemObj.code + " => " + itemCode;
			AuditTrailNut.fnCreate(atObj);
		}
		itemCode = StringManup.stripAndUpper(itemCode);
		itemCode = StringManup.removeSymbols(itemCode);
		itemObj.code = itemCode.trim();
		Item itemEJB2 = ItemNut.getObjectByCode(itemCode);
		if (itemEJB2 != null && !itemEJB2.getPkid().equals(pkid))
		{
			throw new Exception("The item code exists in the database!! ");
		}
		String serialized = req.getParameter("serialized");
		if (serialized.equals("true"))
		{
			itemObj.serialized = true;
		} else
		{
			itemObj.serialized = false;
		}
		String itemName = (String) req.getParameter("itemName");
		if (itemName == null || itemName.length() < 3)
		{
			throw new Exception("Item Name must have at least 3 characters!! ");
		}
		if (itemName.length() > 99)
		{
			throw new Exception("Item Name must not have more than 100 characters!! ");
		}
		if (!itemObj.name.equals(itemName))
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = userId;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-name " + itemObj.name + " => " + itemName;
			AuditTrailNut.fnCreate(atObj);
		}
		itemObj.name = itemName;
		String catId = req.getParameter("itemCategoryId");
		itemObj.categoryId = new Integer(catId);
		itemObj.category1 = req.getParameter("itemCategory1");
		itemObj.category2 = req.getParameter("itemCategory2");
		itemObj.category3 = req.getParameter("itemCategory3");
		itemObj.category4 = req.getParameter("itemCategory4");
		itemObj.category5 = req.getParameter("itemCategory5");
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}

	protected void fnEditOtherDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		String eanCode = (String) req.getParameter("eanCode");
		String upcCode = (String) req.getParameter("upcCode");
		String isbnCode = (String) req.getParameter("isbnCode");
		itemObj.eanCode = eanCode.trim();
		itemObj.upcCode = upcCode.trim();
		itemObj.isbnCode = isbnCode.trim();
		if (eanCode.length() > 4)
		{
			ItemObject eanItmObj = ItemNut.getObjectByEAN(eanCode);
			if (eanItmObj != null && !eanItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The EAN Code exists in the database!!");
			}
		}
		if (upcCode.length() > 4)
		{
			ItemObject upcItmObj = ItemNut.getObjectByUPC(upcCode);
			if (upcItmObj != null && !upcItmObj.pkid.equals(itemObj.pkid))
			{
				throw new Exception("The UPC Code exists in the database!!");
			}
		}
		if (isbnCode.length() > 4)
		{
			ItemObject isbnItmObj = ItemNut.getObjectByISBN(isbnCode);
			if (isbnItmObj != null && isbnItmObj.equals(itemObj.pkid))
			{
				throw new Exception("The ISBN Code exists in the database!!");
			}
		}
		itemObj.description = (String) req.getParameter("itemDescription");
		String itemUOM = req.getParameter("itemUOM");
		if (itemUOM == null || itemUOM.length() < 2)
		{ throw new Exception("Unit of measure must have at least 2 characters!! "); } 
		else
		{ itemObj.uom = itemUOM.toUpperCase(); }


		itemObj.weight = new BigDecimal(req.getParameter("weight"));
		itemObj.length = new BigDecimal(req.getParameter("length"));
		itemObj.width = new BigDecimal(req.getParameter("width"));
		itemObj.depth = new BigDecimal(req.getParameter("depth"));
		String prodReq = req.getParameter("productionRequired");
		if (prodReq != null && prodReq.equals("true"))
		{
			itemObj.productionRequired = true;
		} else
		{
			itemObj.productionRequired = false;
		}
		String delReq = req.getParameter("deliveryRequired");
		if (delReq != null && delReq.equals("true"))
		{
			itemObj.deliveryRequired = true;
		} else
		{
			itemObj.deliveryRequired = false;
		}
		HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}

	protected void fnResetPricing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String strPkid = req.getParameter("pkid");
		Integer pkid = null;
		try
		{
			pkid = new Integer(strPkid);
		} catch (Exception ex)
		{
			throw new Exception("Invalid pkid");
		}
		ItemObject itemObj = ItemNut.getObject(pkid);
		if (itemObj == null)
		{
			throw new Exception("Invalid Item Object");
		}
		// // storing old values
		BigDecimal priceList = itemObj.priceList;
		BigDecimal priceSale = itemObj.priceSale;
		BigDecimal priceDisc1 = itemObj.priceDisc1;
		// BigDecimal maUnitCost = itemObj.maUnitCost;
		BigDecimal lastUnitCost = itemObj.lastUnitCost;
		BigDecimal rebate1Price = itemObj.rebate1Price;
		BigDecimal rebate1Pct = itemObj.rebate1Pct;
		BigDecimal deltaPriceRetailAmt = itemObj.deltaPriceRetailAmt;
		BigDecimal deltaPriceDealerAmt = itemObj.deltaPriceDealerAmt;
		BigDecimal deltaPriceOutletAmt = itemObj.deltaPriceOutletAmt;
		itemObj.priceList = new BigDecimal(req.getParameter("priceList"));
		itemObj.priceSale = new BigDecimal(req.getParameter("priceSale"));
		itemObj.priceDisc1 = new BigDecimal(req.getParameter("priceDisc1"));
		itemObj.priceDisc2 = new BigDecimal(req.getParameter("priceDisc2"));
		itemObj.priceDisc3 = new BigDecimal(req.getParameter("priceDisc3"));
		// itemObj.maUnitCost = new BigDecimal(req.getParameter("maUnitCost"));
		itemObj.lastUnitCost = new BigDecimal(req.getParameter("lastUnitCost"));
		itemObj.replacementUnitCost = new BigDecimal(req.getParameter("replacementCost"));
		itemObj.priceMin = new BigDecimal(req.getParameter("priceMin"));
		itemObj.reserved1 = req.getParameter("reserved1");
		itemObj.deltaPriceRetailAmt = new BigDecimal(req.getParameter("deltaPriceRetailAmt"));
		itemObj.deltaPriceDealerAmt = new BigDecimal(req.getParameter("deltaPriceDealerAmt"));
		itemObj.deltaPriceOutletAmt = new BigDecimal(req.getParameter("deltaPriceOutletAmt"));
		String rebateMethod = req.getParameter("rebateMethod");
		if (rebateMethod != null && rebateMethod.equals("percent"))
		{
			try
			{
				itemObj.rebate1Pct = new BigDecimal(req.getParameter("rebate1Pct"));
				itemObj.rebate1Pct = itemObj.rebate1Pct.divide(new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN);
				itemObj.rebate1Price = itemObj.priceList.multiply(itemObj.rebate1Pct);
				itemObj.rebate1Price = new BigDecimal(CurrencyFormat.strCcy(itemObj.rebate1Price));
			} catch (Exception ex)
			{
			}
		} else
		{
			itemObj.rebate1Price = new BigDecimal(req.getParameter("rebate1Price"));
			if (itemObj.priceList.signum() > 0)
			{
				itemObj.rebate1Pct = itemObj.rebate1Price.divide(itemObj.priceList, 12, BigDecimal.ROUND_HALF_EVEN);
			}
		}
		itemObj.rebate1Start = TimeFormat.createTimestamp(req.getParameter("rebate1Start"));
		itemObj.rebate1End = TimeFormat.createTimestamp(req.getParameter("rebate1End"));
		Log.printVerbose(" REBATE PRICE = " + itemObj.rebate1Price.toString());
		Log.printVerbose(" REBATE START = " + itemObj.rebate1Start.toString());
		Log.printVerbose(" REBATE END  = " + itemObj.rebate1End.toString());
		HttpSession session = req.getSession();
		itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
		Item itemEJB = ItemNut.getHandle(pkid);
		try
		{
			itemEJB.setObject(itemObj);
			// // audit trail!!
			boolean criticalChange = false;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = itemObj.userIdUpdate;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: " + itemObj.code + " ";
			if (priceList.compareTo(itemObj.priceList) != 0)
			{
				criticalChange = true;
				atObj.remarks += " ListPrice:" + CurrencyFormat.strCcy(priceList) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceList) + " ";
			}
			Log.printVerbose(" OLD / NEW SALE PRICE = " + priceSale.toString() + " / " + itemObj.priceSale.toString());
			if (priceSale.compareTo(itemObj.priceSale) != 0)
			{
				criticalChange = true;
				atObj.remarks += " SalePrice:" + CurrencyFormat.strCcy(priceSale) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceSale) + " ";
			}
			// Log.printVerbose(" OLD / NEW MA PRICE = "+maUnitCost.toString()
			// + " / "+ itemObj.maUnitCost.toString());
			// if(maUnitCost.compareTo(itemObj.maUnitCost)!=0)
			// { criticalChange = true;
			// atObj.remarks += "
			// MovingAverage:"+CurrencyFormat.strCcy(maUnitCost)+"->"
			// +CurrencyFormat.strCcy(itemObj.maUnitCost)+" ";}
			if (lastUnitCost.compareTo(itemObj.lastUnitCost) != 0)
			{
				criticalChange = true;
				atObj.remarks += " LastCost:" + CurrencyFormat.strCcy(lastUnitCost) + "->"
						+ CurrencyFormat.strCcy(itemObj.lastUnitCost) + " ";
			}
			if (criticalChange)
			{
				AuditTrailNut.fnCreate(atObj);
			}
			Vector vecStock = new Vector(StockNut.getCollectionByField(StockBean.ITEMID, pkid.toString()));
			for (int cnt1 = 0; cnt1 < vecStock.size(); cnt1++)
			{
				Stock stkEJB = (Stock) vecStock.get(cnt1);
				StockObject stkObj = stkEJB.getObject();
				// stkObj.unitCostMA = itemObj.maUnitCost;
				stkObj.unitCostReplacement = itemObj.replacementUnitCost;
				stkObj.unitCostLast = itemObj.lastUnitCost;
				stkEJB.setObject(stkObj);
			}
		} catch (Exception ex)
		{
			throw new Exception(" Could not update the item! " + ex.getMessage());
		}
		req.setAttribute("itemObj", itemObj);
	}
}
