/*==========================================================
 *
 * Copyright ? of Vincent Lee (vlee@vlee.net,
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

public class DoSalesOrderFast_joe extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("dist-sales-order-fast-page");
		}
		if (formName.equals("createNew"))
		{
			try
			{
				fnCreateNewSalesOrder(servlet, req, res);
				HttpSession session = req.getSession();
				Integer userId = (Integer) session.getAttribute("userId");
				EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
				SalesOrderIndexObject soObj = esos.getSalesOrderIndex();
				EditOrderLockEngine.updateLock(userId,soObj.pkid);
				esos.setMode(EditSalesOrderSession.MODE_CREATE);	
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("closeEdit"))
		{
			try
			{
				fnCloseEdit(servlet,req,res);
			}	
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		if(formName.equals("recycleOrder"))
		{
			try
			{
				fnRecycleOrder(servlet,req,res);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}


		if(formName.equals("getSalesOrder"))
		{
			try
			{				
				HttpSession session = req.getSession();
				/// check if user is currently editing another order
      		    EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
				if(esos!=null)
				{
					System.out.println("formName getSalesOrder, esos not null");
					
					SalesOrderIndexObject soObj = esos.getSalesOrderIndex();
					if(soObj!=null)
					{
						throw new Exception("You are currently editing ORDER:"+soObj.pkid.toString()+". Please close the order first before you edit another order.");
					}
				}
				/// check if the order is edited by another person,
				/// if edited by another person, throw an exception
				Long soPkid = new Long(req.getParameter("soPkid"));
				Integer userId = (Integer) session.getAttribute("userId");
				EditOrderLockEngine.OrderEditLock orderEditLock = EditOrderLockEngine.getLockByOrderId(soPkid);
				if(orderEditLock!=null)
				{
					throw new Exception(" You cannot edit ORDER:"+soPkid.toString()+" because "+UserNut.getUserName(orderEditLock.userId)+" is currently editing the order.");
				}

				fnGetSalesOrder(servlet, req, res);
				esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
				esos.setMode(EditSalesOrderSession.MODE_EDIT);	
				EditOrderLockEngine.updateLock(userId,soPkid);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if (formName.equals("setCustomer"))
		{
			try
			{
				fnSetCustomer(servlet, req, res);
			} 
			catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if (formName.equals("setInternalFlag"))
		{
			fnSetInternalFlag(servlet, req, res);
		}

		if (formName.equals("setBranch"))
		{
			fnSetBranch(servlet, req, res);
		}

		if (formName.equals("setAllDetails"))
		{
			HttpSession session = req.getSession();
			EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
			
			if(esos==null)
			{
				req.setAttribute("errMsg", "You have closed the edit order in another window, changes has not been saved. Please check your order details again!");
				return new ActionRouter("dist-sales-order-fast-page");
			}
			else
			{
				SalesOrderIndexObject soObj = esos.getSalesOrderIndex();
				if(soObj==null)
				{	
					req.setAttribute("errMsg", "You have closed the edit order in another window, changes has not been saved. Please check your order details again!");
					return new ActionRouter("dist-sales-order-fast-page");
				}
				else
				{
					Long orderId = new Long(req.getParameter("orderId"));
					if(!soObj.pkid.equals(orderId))
					{
						req.setAttribute("errMsg", "You were trying to edit 2 orders at the same time, and system prevent you from saving 2 orders simultaneously.. ("+soObj.pkid.toString()+" and "+orderId.toString()+")");
						return new ActionRouter("dist-sales-order-fast-page");
					}
				}

			}
			
			String errMsg = fnSetAllDetails(servlet, req, res);
			if (errMsg != null && errMsg.length() > 3)
			{ req.setAttribute("errMsg", errMsg); }
		}
		if (formName.equals("rmDocRow"))
		{
			try
			{
				fnRmDocRow(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("trading-pos-cashsale-create-page");
			}
		}
		if (formName.equals("addStockWithESS"))
		{
			try
			{
				fnAddStockWithESS(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				return new ActionRouter("trading-pos-cashsale-edit-page");
			}
		}
		if (formName.equals("toggleProduction"))
		{
			try
			{
				fnToggleProduction(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("toggleDelivery"))
		{
			try
			{
				fnToggleDelivery(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("createReceipt"))
		{
			try
			{
				fnCreateReceipt(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("createInvoice"))
		{
			try
			{
				fnCreateInvoice(servlet, req, res);
				req.setAttribute("nofitySuccess", "Successfully created the invoice/cashsale!");
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("updatedContact"))
		{
			try
			{
				fnUpdatedContact(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("updatedAcc"))
		{
			try
			{
				fnUpdatedAcc(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("repeatOrder"))
		{
			try
			{
				fnRepeatOrder(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		if (formName.equals("setDeliveryLocation"))
		{
			fnSetDeliveryLocation(servlet, req, res);
		}

		if (formName.equals("addPackage"))
		{
			try
			{
				fnAddPackage(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("menuSetShipFrom"))
		{
			try
			{
				fnSetShipFrom(servlet,req,res);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

      if(formName.equals("menuSetOrderType1"))
      {
         try
         {
            fnSetOrderType1(servlet,req,res);
         }
         catch(Exception ex)
         {
            ex.printStackTrace();
            req.setAttribute("errMsg", ex.getMessage());
         }
      }


		return new ActionRouter("dist-sales-order-fast-page");
	}

	private void fnCloseEdit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		//// closing the edit form
		HttpSession session = req.getSession();
		EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		SalesOrderIndexObject soObj = esos.getSalesOrderIndex();
		if(soObj!=null)
		{ EditOrderLockEngine.removeLockByOrderId(soObj.pkid); }
		session.setAttribute("dist-sales-order-fast-form",null);

//		EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
	}

	private void fnRecycleOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		Long orderId = new Long(req.getParameter("orderId"));
		
		// Check whether order is in THE UNSAVE-ORDER-BIN. If YES then can recycle, ELSE can't
        QueryObject queryUnsavedOrder = new QueryObject(new String[]{
           DocumentProcessingItemBean.DOC_ID +" = '"+orderId.toString()+"' ",
           DocumentProcessingItemBean.CATEGORY +" = 'UNSAVED-ORDER-BIN' ",
           DocumentProcessingItemBean.PROCESS_TYPE + " = 'ORDER-CREATION' ",
           DocumentProcessingItemBean.DOC_REF+" ='"+SalesOrderIndexBean.TABLENAME+"' "
                 });
        queryUnsavedOrder.setOrder(" ORDER BY "+DocumentProcessingItemBean.DOC_ID ) ;

        Vector vecUnsavedOrder = new Vector(DocumentProcessingItemNut.getObjects(queryUnsavedOrder));
        
        if(vecUnsavedOrder.size() > 0)
        {        	              
		
			{ EditOrderLockEngine.removeLockByOrderId(orderId); }
			
			// create a row in the recycle bin
			{
	         DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
	         dpiObj.processType = "ORDER-RECYCLE";
	         dpiObj.category = "RECYCLE-BIN";
	         dpiObj.auditLevel = new Integer(0);
	         //dpiObj.processId = new Long(0);
	         dpiObj.userid = userId;
	         dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
	         dpiObj.docId = orderId;
	         //dpiObj.entityRef = "";
	         //dpiObj.entityId = new Integer(0);
	         dpiObj.description1 = "THIS ORDER IS IN RECYCLE BIN";
	         //dpiObj.description2 = "";
	         //dpiObj.remarks = "";
	         dpiObj.time = TimeFormat.getTimestamp();
	         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
	         dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
	         DocumentProcessingItemNut.fnCreate(dpiObj);
			}
	
			//  create a log history for this action
			{
	         DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
	         dpiObj.processType = "ORDER-RECYCLE";
	         dpiObj.category = "PUT-INSIDE-RECYCLE-BIN";
	         dpiObj.auditLevel = new Integer(0);
	         //dpiObj.processId = new Long(0);
	         dpiObj.userid = userId;
	         dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
	         dpiObj.docId = orderId;
	         //dpiObj.entityRef = "";
	         //dpiObj.entityId = new Integer(0);
	         dpiObj.description1 = "Order is placed inside recycle bin for re-use.";
	         //dpiObj.description2 = "";
	         //dpiObj.remarks = "";
	         dpiObj.time = TimeFormat.getTimestamp();
	         dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
	         dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
	         DocumentProcessingItemNut.fnCreate(dpiObj);
			}
			
			// closing the edit form
			session.setAttribute("dist-sales-order-fast-form",null);
			EditSalesOrderSession esos = new EditSalesOrderSession(userId);
			esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
			esos.loadSalesOrder(orderId);
			esos.setMode(EditSalesOrderSession.MODE_CREATE);
			esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
			esos.setReceiptMode("");
			CustAccountObject custObj = CustAccountNut.getObject(CustAccountBean.PKID_CASH, "");
			esos.setCustomer(custObj);
			esos.setDeliveryDetails("", "", "" , "", "" , "", "0001-01-01",//7
	         "", "", "", "", "" ,//12
	         "" , "" , "" , "", //16
	         "" , "" , "" , "", "", //21
	         "", "", "", "", "",//26
	         "", "", "", "", "",//31
	         "", "", "", "",//35
	         "", "", "", "", "", true, "");
			esos.dropAllDocRow();
	
	
			/// To remove the order from the unsaved-order-bin, since it is recycled

//            QueryObject queryUnsavedOrder = new QueryObject(new String[]{
//               DocumentProcessingItemBean.DOC_ID +" = '"+orderId.toString()+"' ",
//               DocumentProcessingItemBean.CATEGORY +" = 'UNSAVED-ORDER-BIN' ",
//               DocumentProcessingItemBean.PROCESS_TYPE + " = 'ORDER-CREATION' ",
//               DocumentProcessingItemBean.DOC_REF+" ='"+SalesOrderIndexBean.TABLENAME+"' "
//                     });
//            queryUnsavedOrder.setOrder(" ORDER BY "+DocumentProcessingItemBean.DOC_ID ) ;
//
//            Vector vecUnsavedOrder = new Vector(DocumentProcessingItemNut.getObjects(queryUnsavedOrder));
            for(int cnt2=0;cnt2<vecUnsavedOrder.size();cnt2++)
            {
               DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecUnsavedOrder.get(cnt2);
               try
               {
                  DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
                  dpiEJB.remove();
               }
               catch(Exception ex)
               { ex.printStackTrace();}

            }
        } // END IF ORDER IN UNSAVED ORDER LIST
        else
        {
        	req.setAttribute("errMsg", "Order "+orderId.toString()+" CANNOT be recycled as it has already been recycled or saved");
        }
	}



   private void fnSetOrderType1(HttpServlet servlet, HttpServletRequest req, 
								HttpServletResponse res) throws Exception
   {
      HttpSession session = req.getSession();
      EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
      String soType1 = req.getParameter("soType1");
      try
      {
         csos.setOrderType1(soType1);
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         throw new Exception(" Failed to set order type!");
      }
   }


	private void fnSetShipFrom(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		String shipFromLocationId = req.getParameter("shipFromLocationId");
		try
		{
			Integer locId = new Integer(shipFromLocationId);
			csos.setProductionLocation(locId);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(" Failed to set ship from location!");
		}
	}

	private void fnAddPackage(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		BranchObject branch = csos.getBranch();
		Integer userId = (Integer) session.getAttribute("userId");
		Integer bomPkid = null;
		try
		{
			bomPkid = new Integer(req.getParameter("bomPkid"));
		} catch (Exception ex)
		{
			new Exception("Invalid BOM ID");
		}
		BOMObject bomObj = BOMNut.getObject(bomPkid);
		for (int cnt1 = 0; cnt1 < bomObj.vecLink.size(); cnt1++)
		{
			BOMLinkObject blObj = (BOMLinkObject) bomObj.vecLink.get(cnt1);
			ItemObject itmObj = ItemNut.getObject(blObj.childItemId);
			try
			{
				DocRow docrow = new DocRow();
				docrow.setTemplateId(0);
				docrow.setItemType(ItemBean.ITEM_TYPE_PACKAGE);
				docrow.setItemId(itmObj.pkid);
				docrow.setItemCode(itmObj.code);
				docrow.setItemName(bomObj.parentItemCode + " - " + itmObj.name);
				docrow.setSerialized(itmObj.serialized);
				docrow.setQty(blObj.getRatio());
				docrow.setCcy1(branch.currency);
				docrow.setPrice1(blObj.priceList);
				docrow.setCommission1(itmObj.commissionPctSales1);
				docrow.user1 = userId.intValue();
				docrow.setRemarks("(PACKAGE) " );
				docrow.setDescription(itmObj.description);
				docrow.setCcy2("");
				docrow.setPrice2(new BigDecimal(0));
				Timestamp tsNow = TimeFormat.getTimestamp();
				Timestamp crvEndNextDay = TimeFormat.add(bomObj.rebate1End, 0, 0, 1);
				// if(tsNow.after(bomObj.rebate1Start) &&
				// tsNow.before(bomObj.rebate1End))
				// { docrow.setCrvGain(blObj.rebate1Price);}
				docrow.setCrvGain(itmObj.rebate1Price);
				docrow.setProductionRequired(itmObj.productionRequired);
				docrow.setDeliveryRequired(itmObj.deliveryRequired);
				docrow.setDiscount(new BigDecimal(0));
				csos.fnAddStockWithItemCode(docrow);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw ex;
			}
		}
	}

	// //////////////////////////////////////////
	private void fnSetDeliveryLocation(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		String dlPkid = req.getParameter("dlPkid");
		String dlOption = req.getParameter("dlOption");
		try
		{
			Long pkid = new Long(dlPkid);
			csos.setDeliveryLocation(pkid, dlOption);
		} catch (Exception ex)
		{ ex.printStackTrace();}
	}

	// //////////////////////////////////////////
	private void fnRepeatOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		String oldSalesOrderId = req.getParameter("oldSalesOrderId");
		SalesOrderIndexObject soObj = null;
		
		try
		{
			Long iOldSO = new Long(oldSalesOrderId);
			soObj = SalesOrderIndexNut.getObjectTree(iOldSO);
			if (soObj == null)
			{
				throw new Exception("Invalid Sales Order!");
			}
		} catch (Exception ex)
		{
			throw ex;
		}
						
		CreateSalesOrderSession csos = new CreateSalesOrderSession(userId);
		UserConfigObject ucObj = (UserConfigObject) session.getAttribute("ucObj");
		if (ucObj != null && ucObj.mDefaultCustSvcCtr != null)
		{
			csos.setBranch(ucObj.mDefaultCustSvcCtr);
		}
		
		// / setting memberCardInfo
		if (soObj.senderLoyaltyCardNo.length() > 5)
		{
			MemberCardObject memCardObj = MemberCardNut.getObjectByCardNo(soObj.senderLoyaltyCardNo);
			if (memCardObj == null)
			{
				throw new Exception("Invalid Card No!");
			}
			csos.setMemberCard(memCardObj);
			
		} else if (soObj.senderTable2.equals(CustUserBean.TABLENAME) && soObj.senderKey2.intValue() > 0)
		{
			CustUserObject custUserObj = CustUserNut.getObject(soObj.senderKey2);
			if (custUserObj != null)
			{
				csos.setCustUser(custUserObj);
			} else
			{
				throw new Exception("Invalid Member");
			}
		} else if (soObj.senderTable1.equals(CustAccountBean.TABLENAME) && soObj.senderKey1.intValue() > 0)
		{
			CustAccountObject custObj = CustAccountNut.getObject(soObj.senderKey1);
			if (custObj != null)
			{
				csos.setCustomer(custObj);
			} else
			{
				throw new Exception("Invalid Account");
			}
		}
		
		csos.setInfo(csos.getBranch().pkid, soObj.remarks1, soObj.requireInvoice, soObj.requireReceipt,
				soObj.flagInternal, soObj.flagSender, soObj.flagReceiver, "", soObj.soType1, soObj.occasion,
				soObj.thirdpartyLoyaltyCardCode, soObj.thirdpartyLoyaltyCardNumber,
				soObj.interfloraPrice, soObj.interfloraFlowers1);
		
		csos.setDeliveryDetails(soObj.deliveryTo, soObj.deliveryToName, soObj.deliveryFrom, soObj.deliveryFromName,
				soObj.deliveryMsg1, soObj.expDeliveryTime, TimeFormat.strDisplayDate(), soObj.deliveryPreferences,
				soObj.senderName, soObj.senderIdentityNumber,soObj.senderEmail, soObj.senderHandphone, soObj.senderFax,
				soObj.senderPhone1, soObj.senderPhone2, soObj.senderInternetNo, soObj.senderCompanyName,
				soObj.senderAdd1, soObj.senderAdd2, soObj.senderAdd3, soObj.senderCity, soObj.senderZip,
				soObj.senderState, soObj.senderCountry, soObj.receiverTitle, soObj.receiverName,
				soObj.receiverIdentityNumber, soObj.receiverEmail, soObj.receiverHandphone, soObj.receiverFax, soObj.receiverPhone1,
				soObj.receiverPhone2, soObj.receiverCompanyName, soObj.receiverAdd1, soObj.receiverAdd2,
				soObj.receiverAdd3, soObj.receiverCity, soObj.receiverZip, soObj.receiverState, soObj.receiverCountry, soObj.receiverLocationType);
		
		csos.setDocRows(soObj.getDocRows());
		
		csos.createSalesOrderAndReceipt();
		
		Vector vecSO = csos.getRecentlyCreatedSO();
		
		if (vecSO != null && vecSO.size() > 0)
		{
			SalesOrderIndexObject soIndexObj = (SalesOrderIndexObject) vecSO.get(vecSO.size() - 1);
			EditSalesOrderSession esos = new EditSalesOrderSession(userId);
			esos.setMode(EditSalesOrderSession.MODE_CREATE);
			esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
			session.setAttribute("dist-sales-order-fast-form", esos);
			try
			{
				esos.loadSalesOrder(soIndexObj.pkid);
				
				{
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "ORDER-CREATION";
					dpiObj.category = "UNSAVED-ORDER-BIN";
					dpiObj.auditLevel = new Integer(0);
					//       dpiObj.processId = new Long(0);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soIndexObj.pkid;
					//       dpiObj.entityRef = "";
					//       dpiObj.entityId = new Integer(0);
					dpiObj.description1 = "THIS ORDER IS BEING CREATED, BUT HAS NOT BEEN SAVED";
					//       dpiObj.description2 = "";
					//       dpiObj.remarks = "";
					dpiObj.time = TimeFormat.getTimestamp();
					dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
					DocumentProcessingItemNut.fnCreate(dpiObj);
				}
				
			} catch (Exception ex)
			{
				throw ex;
			}
		}					
	}

	// //////////////////////////////////////////
	private void fnUpdatedContact(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		System.out.println("Inside fnUpdateContact");
		
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		csos.wait(1000);
		csos.updateContact();
	}

	private void fnUpdatedAcc(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		csos.updateAccount();
	}

	// //////////////////////////////////////////
	private void fnCreateReceipt(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = (HttpSession) req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		csos.createReceipt();
	}

	private void fnCreateInvoice(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		csos.createInvoice();
	}

	private void fnToggleProduction(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		String docrowKey = req.getParameter("rowKey");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		csos.toggleProduction(docrowKey);
	}

	private void fnToggleDelivery(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		String docrowKey = req.getParameter("rowKey");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		csos.toggleDelivery(docrowKey);
	}

	protected void fnAddStockWithESS(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String essGuid = req.getParameter("essGuid");
		OrderStockSession ess = (OrderStockSession) session.getAttribute(essGuid);
		ItemObject itmObj = ess.getItemObject();
		BranchObject branch = ess.getBranch();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		Integer userId = (Integer) session.getAttribute("userId");
		DocRow docrow = null;
		try
		{
			docrow = new DocRow();
			docrow.setTemplateId(itmObj.categoryId.intValue());
			docrow.setItemType(itmObj.itemType1);
			docrow.setItemId(itmObj.pkid);
			docrow.setItemCode(itmObj.code);
			docrow.setItemName(itmObj.name);
			docrow.setSerialized(itmObj.serialized);
			docrow.setQty(ess.getQty());
			docrow.setCcy1(branch.currency);
			docrow.setPrice1(ess.getUnitPrice());
			docrow.setCommission1(itmObj.commissionPctSales1);
			docrow.setDiscount(ess.getDiscount());
			docrow.user1 = ess.getPic1().intValue();
			docrow.setRemarks(ess.getRemarks());
			docrow.setDescription(itmObj.description);
			docrow.setCcy2(ess.getCcy2());
			docrow.setPrice2(ess.getPrice2());
			docrow.setCrvGain(ess.getCrvGain());
			docrow.setProductionRequired(ess.getProductionRequired());
			docrow.setDeliveryRequired(ess.getDeliveryRequired());
			Vector vecSN = ess.getSerialNumbers();
			if (vecSN != null)
			{
				for (int cnt = 0; cnt < vecSN.size(); cnt++)
				{
					String sn = (String) vecSN.get(cnt);
					docrow.addSerial(sn);
				}
			}
			csos.fnAddStockWithItemCode(docrow);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		session.setAttribute(essGuid, null);
	}

	// ///////////////////////////////////////////////////////////
	protected void fnRmDocRow(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = (HttpSession) req.getSession();
		EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		String docRowKey = req.getParameter("docRowKey");
		csos.dropDocRow(docRowKey);
	}

	private void fnSetBranch(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = (HttpSession) req.getSession();
		EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		String branchPkid = req.getParameter("branch");
		try
		{
			Integer iBranch = new Integer(0);
			iBranch = new Integer(branchPkid);
			esos.setBranch(iBranch);
		} catch (Exception ex)
		{
			req.setAttribute("errMsg", "Invalid Branch Setting!");
		}
	}

	private String fnSetAllDetails(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
			String errMsg = "";
			HttpSession session = (HttpSession) req.getSession();
			EditSalesOrderSession csos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
			csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);

			if(csos.getMode().equals(EditSalesOrderSession.MODE_VIEW))
			{ errMsg = "You cannot modify the details or the order in VIEW MODE"; return errMsg; }

//		String flagInternal = req.getParameter("flagInternal");
		
/*      try
      {
      	String branchPkid = req.getParameter("branch");
         Integer iBranch = new Integer(0);
         iBranch = new Integer(branchPkid);
         csos.setBranch(iBranch);
			csos.setFlagInternal(flagInternal);

      } 
		catch (Exception ex)
      {
         req.setAttribute("errMsg", "Invalid Branch Setting!");
      }
*/
		try
		{
			String productionLocationId = req.getParameter("productionLocation");
			if(productionLocationId!=null && productionLocationId.length()>0)
			{
				csos.setProductionLocation(new Integer(productionLocationId));
			}

		}
		catch(Exception ex)
		{
         req.setAttribute("errMsg", "Invalid Branch Setting!");
		}

			SalesOrderIndexObject soObj = csos.getSalesOrderIndex();
			if (!csos.hasInvoice())
			{
				String[] docrowKey = req.getParameterValues("docrowKey");
				String[] docrowRemarks = req.getParameterValues("docrowRemarks");
				String[] docrowDiscount = req.getParameterValues("docrowDiscount");
				String[] docrowQty = req.getParameterValues("docrowQty");
				if (docrowKey != null && docrowRemarks != null && docrowDiscount != null && docrowQty != null)
				{
					for (int cnt1 = 0; cnt1 < docrowKey.length; cnt1++)
					{
						TreeMap tableRows = csos.getTableRows();
						DocRow theDocRow = (DocRow) tableRows.get(docrowKey[cnt1]);
						if (theDocRow != null)
						{
							try
							{
								theDocRow.setRemarks(docrowRemarks[cnt1]);
							} catch (Exception ex)
							{
							}
							try
							{
								theDocRow.setDiscount(new BigDecimal(docrowDiscount[cnt1]));
							} catch (Exception ex)
							{
							}
							try
							{
								theDocRow.setQty(new BigDecimal(docrowQty[cnt1]));
							} catch (Exception ex)
							{
							}
						}
					}
				}// / end if the params are not null
			}
			BranchObject branch = csos.getBranch();
			// String remarks1 = req.getParameter("remarks1");
			
			String remarks1 = "Not Shipped";
			Timestamp tsExpDeliveryTimeStart = TimeFormat.createTimestamp(req.getParameter("expDeliveryTimeStart"));				
			Timestamp tsToday = TimeFormat.getCurrentDate();
			
			if (!soObj.remarks1.equals("Confirmed delivered") || !soObj.remarks1.equals(SalesOrderItemBean.DELIVERY_STATUS_DELIVERY_PROBLEM))
			{
				if(tsExpDeliveryTimeStart.compareTo(tsToday)==0)
				{
					remarks1 = "Being processed";
				}				
			}
						
			String requireInvoice = req.getParameter("requireInvoice");
			String requireReceipt = req.getParameter("requireReceipt");
			String flagInternalBool = req.getParameter("flagInternalBool");
			//String flagInternal = soObj.flagInternal;
			String flagInternal = req.getParameter("flagInternal");
			String flagSender = req.getParameter("flagSender");
			String flagReceiver = soObj.flagReceiver;
			String managerPassword = req.getParameter("managerPassword");
			String soType1 = req.getParameter("soType1");
			String occasion = req.getParameter("occasion");
			String thirdpartyLoyaltyCardCode = req.getParameter("thirdpartyLoyaltyCardCode");
			String thirdpartyLoyaltyCardNumber = req.getParameter("thirdpartyLoyaltyCardNumber");
			String paymentStatus = req.getParameter("paymentStatus");
			BigDecimal interfloraPrice = new BigDecimal(req.getParameter("interfloraPrice"));
			String interfloraFlowers1 = req.getParameter("interfloraFlowers1");
			boolean bRInvoice = false;
			if (requireInvoice != null && requireInvoice.equals("true"))
			{
				bRInvoice = true;
			}
			boolean bRReceipt = false;
			if (requireReceipt != null && requireReceipt.equals("true"))
			{
				bRReceipt = true;
			}

			csos.setInfo(remarks1, bRInvoice, bRReceipt, flagInternal, flagSender, flagReceiver, managerPassword, soType1,
					occasion, thirdpartyLoyaltyCardCode, thirdpartyLoyaltyCardNumber, paymentStatus, interfloraPrice, interfloraFlowers1);
			String deliveryTo = req.getParameter("deliveryTo");
			String deliveryToName = req.getParameter("deliveryToName");
			String deliveryFrom = req.getParameter("deliveryFrom");
			String deliveryFromName = req.getParameter("deliveryFromName");
			String deliveryMsg1 = req.getParameter("deliveryMsg1");
			String strMessageCardRequired = req.getParameter("messageCardRequired");
			
			System.out.println("strMessageCardRequired : "+strMessageCardRequired);
			
			boolean messageCardRequired = true;
			if("FALSE".equals(strMessageCardRequired))
				messageCardRequired = false;
			
			String expDeliveryTime = req.getParameter("expDeliveryTime");
			String expDeliveryTimeStart = req.getParameter("expDeliveryTimeStart");
			String senderName = soObj.senderName;
			String senderIdentityNumber = soObj.senderIdentityNumber;
			String senderHandphone = soObj.senderHandphone;
			String senderFax = soObj.senderFax;
			String senderPhone1 = soObj.senderPhone1;
			String senderPhone2 = soObj.senderPhone2;
			String senderInternetNo = soObj.senderInternetNo;
			String senderCompanyName = soObj.senderCompanyName;
			String senderAdd1 = soObj.senderAdd1;
			String senderAdd2 = soObj.senderAdd2;
			String senderAdd3 = soObj.senderAdd3;
			String senderCity = soObj.senderCity;
			String senderZip = soObj.senderZip;
			String senderState = soObj.senderState;
			String senderCountry = soObj.senderCountry;
			String receiverTitle = req.getParameter("receiverTitle");
			String receiverName = req.getParameter("receiverName");
			String receiverIdentityNumber = req.getParameter("receiverIdentityNumber");
			//[[JOB-JOE
			int fooIdx = -1;
			//String receiverHandphone = req.getParameter("Rmobile_phonePrefix") + "-" + req.getParameter("Rmobile_phone");
			String receiverHandphone = req.getParameter("Rmobile_phone").trim();
			fooIdx = receiverHandphone.indexOf("-");
			String fooRmobile_phonePrefix = req.getParameter("Rmobile_phonePrefix").trim();
			if(fooRmobile_phonePrefix.length()>1 && fooIdx<0) receiverHandphone = fooRmobile_phonePrefix + "-" + receiverHandphone;
			
			//String receiverFax = req.getParameter("Rfax_noPrefix") + "-" + req.getParameter("Rfax_no");
			fooIdx = -1;
			String receiverFax = req.getParameter("Rfax_no").trim();
			fooIdx = receiverFax.indexOf("-");
			String fooRfax_noPrefix = req.getParameter("Rfax_noPrefix").trim();
			if(fooRfax_noPrefix.length()>1 && fooIdx<0) receiverFax = fooRfax_noPrefix + "-" + receiverFax;
			//JOB-JOE]]
			
			//[[JOB-JOE
			//String receiverPhone1 = req.getParameter("Rtelephone1_Prefix") + "-" + req.getParameter("Rtelephone1") + "Ext" + req.getParameter("Rext");
			fooIdx = -1;
			int fooIdx2 = -1;
			String receiverPhone1 = req.getParameter("Rtelephone1").trim();
			fooIdx = receiverPhone1.indexOf("-");
			fooIdx2 = receiverPhone1.indexOf("Ex");
			String fooRtelephone1_Prefix = req.getParameter("Rtelephone1_Prefix").trim(); fooRtelephone1_Prefix.trim();
			String fooRext = req.getParameter("Rext"); fooRext.trim();
			if(fooRtelephone1_Prefix.length()>1 && fooIdx<0) receiverPhone1 = fooRtelephone1_Prefix + "-" + receiverPhone1;
			if(fooRext.length()>0 && fooIdx2<0) receiverPhone1 = receiverPhone1 + "Ext" + fooRext;
			//JOB-JOE]]
			//[[JOB-JOE
			//String receiverPhone2 = req.getParameter("Rtelephone2_Prefix") + "-" + req.getParameter("Rtelephone2");
			fooIdx = -1;
			String receiverPhone2 = req.getParameter("Rtelephone2").trim();
			fooIdx = receiverPhone2.indexOf("-");
			String fooRtelephone2_Prefix = req.getParameter("Rtelephone2_Prefix").trim();
			if(fooRtelephone2_Prefix.length()>1 && fooIdx2<0) receiverPhone2 = fooRtelephone2_Prefix + "-" + receiverPhone2;
			//JOB-JOE]]
			String receiverCompanyName = req.getParameter("receiverCompanyName");
			String receiverAdd1 = req.getParameter("receiverAdd1");
			String receiverAdd2 = req.getParameter("receiverAdd2");
			String receiverAdd3 = req.getParameter("receiverAdd3");
			String receiverCity = req.getParameter("receiverCity");
			String receiverZip = req.getParameter("receiverZip");
			String receiverState = req.getParameter("receiverState");
			String receiverCountry = req.getParameter("receiverCountry");
			String deliveryPreferences = req.getParameter("deliveryPreferences");
			
			String receiverLocationType = req.getParameter("receiverLocationType");

			csos.setDeliveryDetails(deliveryTo, deliveryToName, deliveryFrom,
				deliveryFromName, deliveryMsg1, expDeliveryTime, expDeliveryTimeStart,
				deliveryPreferences, senderName, senderIdentityNumber, "", senderHandphone,
				senderFax, senderPhone1, senderPhone2, senderInternetNo,
				senderCompanyName, senderAdd1, senderAdd2, senderAdd3, senderCity,
				senderZip, senderState, senderCountry, receiverTitle, receiverName,
				receiverIdentityNumber, "", receiverHandphone, receiverFax, receiverPhone1,
				receiverPhone2, receiverCompanyName, receiverAdd1, receiverAdd2,
				receiverAdd3, receiverCity, receiverZip, receiverState, receiverCountry, 
				messageCardRequired, receiverLocationType);
			csos.setDisplayFormat(SalesOrderIndexBean.DF_QUICK_ORDER);
			String receiptMode = req.getParameter("receiptMode");
			String receiptMode_hasChanged = req.getParameter("receiptMode_hasChanged");
			Log.printVerbose(" Receipt Mod Has Changed? " + receiptMode_hasChanged);
			String receiptRemarks = req.getParameter("receiptRemarks");
			String receiptRemarks_hasChanged = req.getParameter("receiptRemarks_hasChanged");
			Log.printVerbose(" Receipt Remarks Has Changed? " + receiptRemarks_hasChanged);
			String receiptApprovalCode = req.getParameter("receiptApprovalCode");
			String receiptBranch = req.getParameter("receiptBranch");
			try
			{
				csos.setReceiptInfo(receiptRemarks, receiptApprovalCode, new Integer(receiptBranch));
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			try
			{
				if (receiptMode_hasChanged != null && receiptMode_hasChanged.equals("true"))
				{
					Integer receiptModeId = new Integer(receiptMode);
					CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(receiptModeId);
					if (cpcObj != null)
					{
						csos.setReceiptMode(cpcObj.paymentMode);
						csos.setPaymentRemarks(cpcObj.defaultPaymentRemarks);
						csos.setPaymentStatus(cpcObj.defaultPaymentStatus);
					} else
					{
						csos.setReceiptMode("");
					}
				}
			} catch (Exception ex)
			{
			}
			try
			{
				if (receiptRemarks_hasChanged != null && receiptRemarks_hasChanged.equals("true"))
				{
					QueryObject queryPayRmks = new QueryObject(new String[] {
							StringTemplateBean.CATEGORY + " ='" + StringTemplateBean.CAT_PAYMENT_REMARKS + "' ",
							StringTemplateBean.CONTENT + " ='" + receiptRemarks + "' " });
					queryPayRmks.setOrder(" ORDER BY " + StringTemplateBean.SORT);
					Vector vecPayRmks = new Vector(StringTemplateNut.getObjects(queryPayRmks));
					if (vecPayRmks != null && vecPayRmks.size() > 0)
					{
						StringTemplateObject stObj = (StringTemplateObject) vecPayRmks.get(0);
						csos.setPaymentStatus(stObj.description);
					}
				}
			} catch (Exception ex)
			{
			}
			/*
			 * ReceiptForm receiptForm = csos.getReceiptForm();
			 * receiptForm.setAmountCoupon(csos.getRedeemingCRVTotal()); String
			 * cashAmt = req.getParameter("amountCash"); String cardAmt =
			 * req.getParameter("amountCard"); // String cardType =
			 * req.getParameter("cardType"); // String cardPct =
			 * req.getParameter("cardPctCharges"); String cbCreditCard =
			 * req.getParameter("creditCardCashbook"); String chequeAmt =
			 * req.getParameter("amountCheque"); String pdChequeAmt =
			 * req.getParameter("amountPDCheque"); String pdChequeDate =
			 * req.getParameter("pdChequeDate"); String chequeNumber =
			 * req.getParameter("chequeNumber"); String chequeNumberPD =
			 * req.getParameter("chequeNumberPD");
			 * 
			 * String strChequeCharges = req.getParameter("chequeCharges"); String
			 * cbCheque = req.getParameter("chequeCashbook"); // String strSurcharge =
			 * req.getParameter("surcharge"); String ccType =
			 * req.getParameter("ccType"); String ccName =
			 * req.getParameter("ccName"); String ccBank =
			 * req.getParameter("ccBank"); String ccNumber =
			 * req.getParameter("ccNumber"); String ccSecurity =
			 * req.getParameter("ccSecurity");
			 * 
			 * String ccExpiryYear = req.getParameter("ccExpiryYear"); String
			 * ccExpiryMonth = req.getParameter("ccExpiryMonth"); Timestamp ccExpiry =
			 * TimeFormat.createTimestamp(ccExpiryYear+"-"+ccExpiryMonth);
			 *  // BigDecimal surcharge = new BigDecimal(0); BigDecimal
			 * chequeCharges = new BigDecimal(0); Integer cashbookCreditCard = new
			 * Integer(0); Integer cashbookCheque = new Integer(0); BigDecimal
			 * amountCash = new BigDecimal(0); BigDecimal amountCard = new
			 * BigDecimal(0); // BigDecimal amountCardPct = new BigDecimal(0);
			 * BigDecimal amountCheque= new BigDecimal(0); BigDecimal
			 * amountPDCheque= new BigDecimal(0); Timestamp pdDate =
			 * receiptForm.getPDChequeDate();
			 * 
			 * String receiptRemarks = req.getParameter("receiptRemarks1"); try{
			 * amountCash = new BigDecimal(cashAmt);} catch(Exception ex){} try{
			 * amountCard = new BigDecimal(cardAmt);} catch(Exception ex){} // try{
			 * amountCardPct = new BigDecimal(cardPct);} // catch(Exception ex){}
			 * try{ amountCheque = new BigDecimal(chequeAmt);} catch(Exception ex){}
			 * try{ cashbookCreditCard = new Integer(cbCreditCard);} catch(Exception
			 * ex){ errMsg += " Invalid Credit Card Cashbook! ";} try{
			 * amountPDCheque = new BigDecimal(pdChequeAmt);} catch(Exception ex){}
			 * try{ pdDate = TimeFormat.createTimestamp(pdChequeDate);}
			 * catch(Exception ex){} try{ cashbookCheque = new Integer(cbCheque);}
			 * catch(Exception ex){ errMsg += " Invalid Cheque Cashbook! ";} try{
			 * chequeCharges = new BigDecimal(strChequeCharges);} catch(Exception
			 * ex){ errMsg += " Invalid Cheque Charges! ";} // try{ surcharge = new
			 * BigDecimal(strSurcharge);} // catch(Exception ex){}
			 * 
			 * receiptForm.setCCDetails(ccNumber,ccType,
			 * ccName,ccBank,ccExpiry,ccSecurity); //
			 * receiptForm.setSurcharge(surcharge); //
			 * receiptForm.setCardType(cardType); //
			 * receiptForm.setCardPctCharges(amountCardPct);
			 * receiptForm.setChequeCharges(chequeCharges);
			 * receiptForm.setCreditCardCashbook(cashbookCreditCard);
			 * receiptForm.setAmountCash(amountCash);
			 * receiptForm.setAmountCard(amountCard);
			 * receiptForm.setAmountCheque(amountCheque);
			 * receiptForm.setAmountPDCheque(amountPDCheque);
			 * receiptForm.setPDChequeDate(pdDate);
			 * receiptForm.setChequeNumber(chequeNumber);
			 * receiptForm.setChequeNumberPD(chequeNumberPD);
			 * receiptForm.setChequeCashbook(cashbookCheque);
			 * receiptForm.setRemarks(receiptRemarks);
			 */
			// //**************************************************************
			// // QUICK ADD ITEM
			// / check if invoice has already been created
			String itemCode = req.getParameter("itemCode");
			while (itemCode != null && itemCode.length() > 2)								
			{
				System.out.println("itemCode not null and length > 2");
				
				itemCode = itemCode.toUpperCase();
				ItemObject itmObj = ItemNut.getValueObjectByCode(itemCode);
				if (itmObj == null)
				{
					if (itemCode.length() > 2)
					{
						itmObj = ItemNut.getObjectByEAN(itemCode);
					}
				}
				if (itmObj == null)
				{
					errMsg += " Invalid Item Code !";
					itemCode = null;
					break;
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
					docrow.setQty(new BigDecimal(req.getParameter("itemQty")));
					docrow.setCcy1(branch.currency);
					docrow.setPrice1(itmObj.priceList);
					docrow.setCommission1(itmObj.commissionPctSales1);
					docrow.setDiscount(new BigDecimal(req.getParameter("itemUnitDiscount")));
					docrow.user1 = userId.intValue();
					docrow.setRemarks(req.getParameter("itemRemarks"));
					docrow.setDescription(itmObj.description);
					docrow.setCcy2("");
					docrow.setPrice2(new BigDecimal(0));
					Timestamp tsNow = TimeFormat.getTimestamp();
					Timestamp crvEndNextDay = TimeFormat.add(itmObj.rebate1End, 0, 0, 1);
					if (tsNow.after(itmObj.rebate1Start) && tsNow.before(itmObj.rebate1End))
					{
						docrow.setCrvGain(itmObj.rebate1Price);
					}
					docrow.setProductionRequired(itmObj.productionRequired);
					docrow.setDeliveryRequired(itmObj.deliveryRequired);
					
					System.out.println("docrow.getItemCode : "+docrow.getItemCode());
					
					csos.fnAddStockWithItemCode(docrow);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
				itemCode = null; // we only want to run this once
			}// / end while
			// //**************************************************************
			// // CREATE RECEIPT ??
			// / check if receipt amount exceeds bill amount / outstanding
			// considering previous receipts
		
			String flag = req.getParameter("flag");
			Log.printVerbose(flag);
			if(errMsg=="" && flag.equals("true"))
			{
				if(csos.getMode().equals(EditSalesOrderSession.MODE_CREATE))
				{
					csos.setOrderCreateTime(TimeFormat.getTimestamp());
					csos.setOrderTaker((Integer)session.getAttribute("userId"));
					csos.updateSalesOrder();
				}
				errMsg="Sales Order "+soObj.pkid.toString()+" has been saved.";

				/// remove the UNSAVED-ORDER-BIN tag to this order
				QueryObject queryCreateButNotSaved = new QueryObject(new String[]{
						DocumentProcessingItemBean.DOC_ID +" = '"+soObj.pkid.toString() +"' ",
						DocumentProcessingItemBean.CATEGORY +" = 'UNSAVED-ORDER-BIN' ",
						DocumentProcessingItemBean.PROCESS_TYPE +" = 'ORDER-CREATION' ",
						DocumentProcessingItemBean.DOC_REF+ " = '"+SalesOrderIndexBean.TABLENAME+"' "});
				Vector vecCreateButNotSaved = new Vector(DocumentProcessingItemNut.getObjects(
																							queryCreateButNotSaved));
				for(int cnt1=0;cnt1<vecCreateButNotSaved.size();cnt1++)
				{
					try	
					{
						DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecCreateButNotSaved.get(cnt1);
						DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
						dpiEJB.remove();
					}	
					catch(Exception ex){ ex.printStackTrace();}
				}
			
				//// since the order has been saved and closed, we remove for the OrderEditLockEngine
				EditOrderLockEngine.removeLockByOrderId(soObj.pkid);

				/// this line is added to make the order disappear from the screen after saving..
				session.setAttribute("dist-sales-order-fast-form",null);
			}
			return errMsg;
	}

	// ///////////////////////////////////////////////////////////
	private void fnSetInternalFlag(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = (HttpSession) req.getSession();
		EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		String flagInternal = req.getParameter("flagInternal");
		esos.setFlagInternal(flagInternal);
	}

	// ///////////////////////////////////////////////////////////
	private void fnSetCustomer(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		String option = req.getParameter("option");
		if (option == null)
		{
			return;
		}
		HttpSession session = (HttpSession) req.getSession();
		EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		if (option.equals("setAcc"))
		{
			String accPkid = req.getParameter("accPkid");
			try
			{
				Integer pkid = new Integer(accPkid);
				CustAccountObject custObj = CustAccountNut.getObject(pkid);
				if (custObj != null)
				{
					esos.setCustomer(custObj);
				} 
				else
				{
					throw new Exception("Invalid Account");
				}
			} catch (Exception ex)
			{
				throw new Exception("Invalid Account Number!");
			}
		}

		if (option.equals("setMember"))
		{
			String memPkid = req.getParameter("memPkid");
			try
			{
				Integer pkid = new Integer(memPkid);
				CustUserObject custUserObj = CustUserNut.getObject(pkid);
				if (custUserObj != null)
				{
					esos.setCustUser(custUserObj);
				} else
				{
					throw new Exception("Invalid Member");
				}
			} catch (Exception ex)
			{
				throw new Exception("Invalid Member");
			}
		}
	}

	private static synchronized void fnCreateNewSalesOrder(HttpServlet servlet, 
				HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		Integer userid = (Integer) session.getAttribute("userId");
		CreateSalesOrderSession csos = new CreateSalesOrderSession(userid);
		// / need to set the branch
		UserConfigObject ucObj = (UserConfigObject) session.getAttribute("ucObj");
		
		if (ucObj != null && ucObj.mDefaultCustSvcCtr != null)
		{ csos.setBranch(ucObj.mDefaultCustSvcCtr); }
		
		SalesOrderIndexObject soObj = csos.getSalesOrderIndex();
		
		if (req.getParameter("orderType2") != null){soObj.soType2 =  SalesOrderIndexBean.SO_TYPE2_FLORIST;}

		/// check if there's any order inside the recycle bin
		QueryObject queryRecycle  = new QueryObject(new String[]{
				DocumentProcessingItemBean.CATEGORY +" = 'RECYCLE-BIN' ",	
				DocumentProcessingItemBean.PROCESS_TYPE+" = 'ORDER-RECYCLE' ",	
				DocumentProcessingItemBean.DOC_REF+" = '"+SalesOrderIndexBean.TABLENAME+"' ",});
		queryRecycle.setOrder(" ORDER BY "+DocumentProcessingItemBean.DOC_ID);
		
		Vector vecRecycleBin = new Vector(DocumentProcessingItemNut.getObjects(queryRecycle));
		
		if(vecRecycleBin.size()==0)
		{
			SalesOrderIndex soEJB = SalesOrderIndexNut.fnCreate(soObj);
			if(soEJB != null)
			{
				EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
				if(esos == null)
				{
					esos = new EditSalesOrderSession(userid);
					esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
					esos.setMode(EditSalesOrderSession.MODE_CREATE);
					session.setAttribute("dist-sales-order-fast-form", esos);
				}
			
				try 
				{ 
					esos.loadSalesOrder(soObj.pkid); 
					esos.setMode(EditSalesOrderSession.MODE_CREATE);
					esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
					if (ucObj != null && ucObj.mDefaultCustSvcCtr != null)
					{ esos.setBranch(ucObj.mDefaultCustSvcCtr); }

					{
						DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
						dpiObj.processType = "ORDER-CREATION";
						dpiObj.category = "UNSAVED-ORDER-BIN";
						dpiObj.auditLevel = new Integer(0);
						//       dpiObj.processId = new Long(0);
						dpiObj.userid = userid;
						dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
						dpiObj.docId = soObj.pkid;
						//       dpiObj.entityRef = "";
						//       dpiObj.entityId = new Integer(0);
						dpiObj.description1 = "THIS ORDER IS BEING CREATED, BUT HAS NOT BEEN SAVED";
						//       dpiObj.description2 = "";
						//       dpiObj.remarks = "";
						dpiObj.time = TimeFormat.getTimestamp();
						dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
						dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
						DocumentProcessingItemNut.fnCreate(dpiObj);
					}
				} 
				catch (Exception ex) { ex.printStackTrace(); throw ex; }
			} 
			else
			{ throw new Exception("Failed to retrieve a new sales order number!"); }
		}
		else
		{

			//// first of all, get the object out for edit
			DocumentProcessingItemObject dpiObj = (DocumentProcessingItemObject) vecRecycleBin.get(0);
			EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
			if(esos == null)
			{
	         	esos = new EditSalesOrderSession(userid);
	         	esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
				esos.setMode(EditSalesOrderSession.MODE_CREATE);
	         	session.setAttribute("dist-sales-order-fast-form", esos);
			}

			try
			{
				esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
				esos.setMode(EditSalesOrderSession.MODE_CREATE);
				esos.loadSalesOrder(dpiObj.docId);

				esos.setReceiptMode("");
				CustAccountObject custObj = CustAccountNut.getObject(CustAccountBean.PKID_CASH, "");
				esos.setCustomer(custObj);
				esos.setDeliveryDetails("To:", "", "From:" , "", "" , "", TimeFormat.strDisplayDate(),//7
							"", "", "", "", "" ,//12
							"" , "" , "" , "", //16
							"" , "" , "" , "", "", //21
							"", "", "", "", "",//26
							"", "", "", "", "",//31
							"", "", "", "",//35
							"", "", "", "", "", true, "");
				esos.dropAllDocRow();
				esos.setOrderCreateTime(TimeFormat.getTimestamp());
				esos.setOrderTaker(userid);
				soObj = esos.getSalesOrderIndex();
				if (ucObj != null && ucObj.mDefaultCustSvcCtr != null)
				{ esos.setBranch(ucObj.mDefaultCustSvcCtr); }
			} 	
			catch (Exception ex)
			{
         	req.setAttribute("errMsg",ex.getMessage());
         	throw ex;
      	}

			/// then remove the orders from recycle bin
			try
			{
				DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj.pkid);
				dpiEJB.remove();
				//// remove all other recyclebin entries for this order also
				QueryObject queryRecycle2  = new QueryObject(new String[]{
            	DocumentProcessingItemBean.DOC_ID+" = '"+dpiObj.docId.toString()+"' ",
            	DocumentProcessingItemBean.CATEGORY +" = 'RECYCLE-BIN' ",
            	DocumentProcessingItemBean.PROCESS_TYPE+" = 'ORDER-RECYCLE' ",
            	DocumentProcessingItemBean.DOC_REF+" = '"+SalesOrderIndexBean.TABLENAME+"' ",});
				queryRecycle2.setOrder(" ORDER BY "+DocumentProcessingItemBean.PKID);
				
				Vector vecRecycleBin2 = new Vector(DocumentProcessingItemNut.getObjects(queryRecycle2));
				
				for(int cnt2=0;cnt2<vecRecycleBin2.size();cnt2++)
				{
					DocumentProcessingItemObject dpiObj2 = (DocumentProcessingItemObject) vecRecycleBin2.get(cnt2);
					try
					{
						DocumentProcessingItem dpiEJB2 = DocumentProcessingItemNut.getHandle(dpiObj2.pkid);
						dpiEJB2.remove();
					}
					catch(Exception ex)
					{ ex.printStackTrace();}
				}

			}
			catch(Exception ex){ ex.printStackTrace();}
			{
				DocumentProcessingItemObject dpiObj2 = new DocumentProcessingItemObject();
	            dpiObj2.processType = "ORDER-CREATION";
	            dpiObj2.category = "UNSAVED-ORDER-BIN";
	            dpiObj2.auditLevel = new Integer(0);
	                  //       dpiObj.processId = new Long(0);
	            dpiObj2.userid = userid;
	            dpiObj2.docRef = SalesOrderIndexBean.TABLENAME;
	            dpiObj2.docId = soObj.pkid;
	                  //       dpiObj.entityRef = "";
	                  //       dpiObj.entityId = new Integer(0);
	            dpiObj2.description1 = "THIS ORDER IS BEING CREATED, BUT HAS NOT BEEN SAVED";
	                  //       dpiObj.description2 = "";
	                  //       dpiObj.remarks = "";
	            dpiObj2.time = TimeFormat.getTimestamp();
	            dpiObj2.state = DocumentProcessingItemBean.STATE_CREATED;
	            dpiObj2.status = DocumentProcessingItemBean.STATUS_ACTIVE;
	            DocumentProcessingItemNut.fnCreate(dpiObj2);
			}

		}

	}

	private static synchronized void fnGetSalesOrder(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) 
			throws Exception
	{
		System.out.println("Inside DoSalesOrderFast.fnGetSalesOrder");
		
		HttpSession session = req.getSession();
		Integer userid = (Integer) session.getAttribute("userId");
		EditSalesOrderSession esos = (EditSalesOrderSession) session.getAttribute("dist-sales-order-fast-form");
		if (esos == null)
		{
			System.out.println("Inside DoSalesOrderFast.fnGetSalesOrder : esos is null");
			esos = new EditSalesOrderSession(userid);
			esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
			esos.setMode(EditSalesOrderSession.MODE_EDIT);
			session.setAttribute("dist-sales-order-fast-form", esos);
		}
		try
		{			
			Long soPkid = new Long(req.getParameter("soPkid"));
			
			System.out.println("Inside DoSalesOrderFast.fnGetSalesOrder, soPkid : "+soPkid.toString());
			
			esos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
			esos.setMode(EditSalesOrderSession.MODE_EDIT);
			esos.loadSalesOrder(soPkid);

      /// check if there's any order inside the recycle bin
		/// remove them
			QueryObject queryRecycle  = new QueryObject(new String[]{
            DocumentProcessingItemBean.DOC_ID +" = '"+soPkid.toString()+"' ",
            DocumentProcessingItemBean.CATEGORY +" = 'RECYCLE-BIN' ",
            DocumentProcessingItemBean.PROCESS_TYPE+" = 'ORDER-RECYCLE' ",
            DocumentProcessingItemBean.DOC_REF+" = '"+SalesOrderIndexBean.TABLENAME+"' ",
                           });
			queryRecycle.setOrder(" ORDER BY "+DocumentProcessingItemBean.PKID);
			Vector vecRecycleBin = new Vector(DocumentProcessingItemNut.getObjects(queryRecycle));
			for(int cnt1=0;cnt1<vecRecycleBin.size();cnt1++)
			{
				DocumentProcessingItemObject dpiObj2 = (DocumentProcessingItemObject) vecRecycleBin.get(cnt1);
				try
				{
					DocumentProcessingItem dpiEJB = DocumentProcessingItemNut.getHandle(dpiObj2.pkid);
					dpiEJB.remove();

					{
						DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
						dpiObj.processType = "ORDER-RECYCLE";
						dpiObj.category = "TAKE-OUT-FROM-RECYCLE-BIN";
						dpiObj.auditLevel = new Integer(0);
			//       dpiObj.processId = new Long(0);
						dpiObj.userid = userid;
						dpiObj.docRef = dpiObj2.docRef;
						dpiObj.docId = dpiObj2.docId;
//       dpiObj.entityRef = "";
//       dpiObj.entityId = new Integer(0);
						dpiObj.description1 = "Order is taken out from recycle bin to re-use.";
//       dpiObj.description2 = "";
//       dpiObj.remarks = "";
						dpiObj.time = TimeFormat.getTimestamp();
						dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
						dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
						DocumentProcessingItemNut.fnCreate(dpiObj);
					}

				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}

		} 
		catch(Exception ex)
		{
			req.setAttribute("errMsg",ex.getMessage());
			throw ex;
		}
	}
}




