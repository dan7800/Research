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
 
package com.vlee.servlet.ecommerce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.util.*;
import com.vlee.bean.application.AppConfigManager;
import com.vlee.bean.distribution.*;
import com.vlee.bean.ecommerce.*;
import com.vlee.ejb.ecommerce.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.inventory.*;
import com.vlee.util.mail.SendMail;

import org.apache.commons.codec.binary.*;

public class DoMemberCheckout extends HttpServlet
{


	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		String formName = "";
		formName = request.getParameter("formName");
		System.out.println("formName>>>>>>>>>>>>>>>>>>>>>" + formName);
		request.setAttribute("mode", request.getParameter("mode"));


		String nextPage[] = new String[1];
		nextPage[0] = new String("/member_cart.jsp");

		if (formName.equals("express"))
		{
			
			try
			{
				ShoppingCart cart = new ShoppingCart();
				String[] optionId = (String[]) request.getParameterValues("optionId");

				CartItem item = new CartItem();
				item.optionId = new Long(optionId[0]);
				item.productId		= new Long(request.getParameter("productId"));
				item.productCode	= request.getParameter("productCode");
				item.productName	= request.getParameter("productName");
				item.productThumbnail = new Long(request.getParameter("productThumbnail"));
				item.optionName		= request.getParameter(item.optionId+"Name");
				item.optionPrice	= new BigDecimal(request.getParameter(item.optionId+"Price"));
				item.optionType		= request.getParameter(item.optionId+"Type");
				item.invid			= new Long(request.getParameter(item.optionId+"Inv"));
				item.invreserved1	= request.getParameter(item.optionId+"InvReserved1");
				
				//For checking whether is entitle to promotion
				ItemObject itemObj = ItemNut.getObject(new Integer(item.invid.toString()));
				item.invreserved2 = itemObj.reserved2;
				
				System.out.println("item.invreserved2 : "+item.invreserved2);
				
				String[] temp;
				if (request.getParameterValues("addOnId") != null)
				{
					temp = new String[request.getParameterValues("addOnId").length];
					for (int j = 0; j < request.getParameterValues("addOnId").length; j++)
					{
						temp[j] = new String(request
								.getParameterValues("addOnId")[j]);
						item.addOnId += temp[j];
						if (j != request.getParameterValues("addOnId").length - 1)
							item.addOnId += ",";
					}
				} else if (request.getParameter("addOnId") != null)
				{
					item.addOnId = request.getParameter("addOnId");
				}

				cart.addCartItem(item);

				HttpSession session = request.getSession();
				
				//Added by Janet
				session.setAttribute("cart", cart);
				
				session.setAttribute("soCart", cart);

				session.setAttribute("vecSalesOrder", null);

				Vector vecSalesOrder = (Vector) session
						.getAttribute("vecSalesOrder");
				if (vecSalesOrder == null)
					vecSalesOrder = new Vector();

				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
				CreateSalesOrderSession csos = new CreateSalesOrderSession(objStore.ecom_userId);
							
				csos = fnCheckout(request, response, cart, csos);
				vecSalesOrder.add(csos);
				session.setAttribute("vecSalesOrder", vecSalesOrder);

				request.setAttribute("mode", "express");

				nextPage[0] = new String("/member_checkout_single.jsp");

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} else if (formName.equals("single")
				|| formName.equals("multiple_select"))
		{
			try
			{
				HttpSession session = request.getSession();

				ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
				session.setAttribute("soCart", cart);
				session.setAttribute("vecSalesOrder", null);

				Vector vecSalesOrder = (Vector) session
						.getAttribute("vecSalesOrder");
				if (vecSalesOrder == null)
					vecSalesOrder = new Vector();

				EStoreObject objStore = (EStoreObject) session
						.getAttribute("objStore");
				CreateSalesOrderSession csos = new CreateSalesOrderSession(
						objStore.ecom_userId);
				
				csos = fnCheckout(request, response, cart, csos);
				
				vecSalesOrder.add(csos);
				session.setAttribute("vecSalesOrder", vecSalesOrder);

				request.setAttribute("mode", formName);

				nextPage[0] = new String("/member_checkout_" + formName + ".jsp");

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} else if (formName.equals("update") || formName.equals("login")
				|| formName.equals("selectContact") || formName.equals("validatePromoCode") 
			|| formName.equals("switchNonValentine") || formName.equals("switchValentine")
			|| formName.equals("promoPayMode"))
		{
			try
			{
				HttpSession session = request.getSession(true);

				Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");

				String tempStr = "";
				String strSOPkid = "0";
				tempStr = request.getParameter("SOPkid");
				if (tempStr != null)
					strSOPkid = tempStr;

				CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder.elementAt(Integer.parseInt(strSOPkid));
				csos = fnUpdate(request, response, csos, nextPage);
				vecSalesOrder.removeElementAt(Integer.parseInt(strSOPkid));
				vecSalesOrder.insertElementAt(csos, Integer.parseInt(strSOPkid));
				session.setAttribute("vecSalesOrder", vecSalesOrder);

				request.setAttribute("SOPkid", strSOPkid);

				String mode = request.getParameter("mode");
				
				System.out.println("mode : "+mode);
				
				if (mode.equals("express"))
				{
					nextPage[0] = new String("/member_checkout_single.jsp");
				}
				else
				{
					nextPage[0] = new String("/member_checkout_" + mode + ".jsp");
				}
		
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("createSOSingle"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");
				CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder.elementAt(0);
				csos = fnUpdate(request, response, csos, nextPage);

				ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
				
				if (cart == null)
				{
					cart = (ShoppingCart) session.getAttribute("soCart");
				}
					
				csos = fnCreateSOSingle(request, response, cart, csos, nextPage);

				vecSalesOrder.removeAllElements();
				vecSalesOrder.addElement(csos);
				session.setAttribute("vecSalesOrder", vecSalesOrder);

				emptyCart(request, response);
				
                
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("multipleSelect"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");

				String tempStr = "";
				String strSOPkid = "0";
				tempStr = request.getParameter("SOPkid");
				if (tempStr != null)
					strSOPkid = tempStr;

				CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder.elementAt(Integer.parseInt(strSOPkid));
				csos = fnUpdate(request, response, csos, nextPage);
				csos = fnMultipleSelect(request, response, csos, nextPage);
				vecSalesOrder.removeElementAt(Integer.parseInt(strSOPkid));
				vecSalesOrder.insertElementAt(csos, Integer.parseInt(strSOPkid));

				session.setAttribute("vecSalesOrder", vecSalesOrder);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("loginMultiple"))
		{
			try
			{
				fnLoginMultiple(request, response, nextPage);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("createSOMultiple"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				Vector vecSalesOrder = new Vector();
				vecSalesOrder = fnCreateSOMultiple(request, response, nextPage);
				session.setAttribute("vecSalesOrder", vecSalesOrder);

				emptyCart(request, response);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("error"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				
				String tempStr = "";
				String SOGuid = "0";
				tempStr = request.getParameter("SOGuid");
				if (tempStr != null) SOGuid = tempStr;	
				
				request.setAttribute("SOGuid", SOGuid);
		
				String AmtTotal = "";
				tempStr = (String) session.getAttribute("AmtTotal");
				if(tempStr!=null) AmtTotal = tempStr;	

				request.setAttribute("AmtTotal", AmtTotal);

				nextPage[0] = new String("/member_checkout_error.jsp");

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("receipt"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				
				String tempStr = "";
				String SOPkid = "0";
				tempStr = request.getParameter("SOPkid");
				if (tempStr != null) SOPkid = tempStr;

				String SOGuid = "0";
				tempStr = request.getParameter("SOGuid");
				if (tempStr != null) SOGuid = tempStr;		

				String status = "";
				tempStr = request.getParameter("status");	
				if (tempStr != null) status = tempStr;

				if (status.equals("")) 
					status = "pending";

				request.setAttribute("status", status);
		
				EStoreObject objStore = (EStoreObject) session
						.getAttribute("objStore");
				EditSalesOrderSession csos = new EditSalesOrderSession(
						objStore.ecom_userId);
				csos.loadSalesOrder(new Long(SOPkid));
				session.setAttribute("dist-sales-order-session", csos);

				fnSalesOrderAmountTotal(request, response, SOGuid);

				nextPage[0] = new String("/member_checkout_receipt.jsp");

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("payment_creditcard"))
		{
			try
			{
				HttpSession session = request.getSession(true);

				String tempStr = "";
				String paymentMode = "";
				tempStr = request.getParameter("nextPage");
				if (tempStr != null) 
				{
					nextPage[0] = new String(tempStr);
				}

				String SOGuid = "0";
				tempStr = request.getParameter("SOGuid");
				if (tempStr != null) SOGuid = tempStr;			
		
				CustUserObject custUserObj = null;
				String strCustUserPkid = (String) session.getAttribute("userId");
				Integer custUserPkid = new Integer(strCustUserPkid);
				custUserObj = CustUserNut.getObject(custUserPkid);

				String receiptMode = "";
				tempStr = request.getParameter("userDefaultCardType");
				if (tempStr != null) receiptMode = tempStr; 
			
				session.setAttribute("receiptMode", receiptMode);

				EPaymentConfigObject objConfig = EPaymentConfigNut.getObject(new Long(
						receiptMode));
				Integer receiptModeId = new Integer(String
						.valueOf(objConfig.payment_config));
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut
						.getObject(receiptModeId);					

				custUserObj.defaultCardType = objConfig.payment_name; 
				paymentMode = "Credit Card - " + objConfig.payment_name;

				tempStr = request.getParameter("userDefaultCardName");
				if (tempStr != null) custUserObj.defaultCardName = tempStr; 

				tempStr = request.getParameter("userDefaultCardNumber");
				if (tempStr != null) custUserObj.defaultCardNumber = tempStr; 

				tempStr = request.getParameter("userDefaultCardSecurityNumber");
				if (tempStr != null) custUserObj.defaultCardSecurityNum = tempStr; 

				tempStr = request.getParameter("userDefaultCardBank");
				if (tempStr != null) custUserObj.defaultCardBank = tempStr; 

				String ccExpiryYear = request.getParameter("ccExpiryYear");
				String ccExpiryMonth = request.getParameter("ccExpiryMonth");
				Timestamp ccExpiry = TimeFormat.createTimestamp(ccExpiryYear + "-" + ccExpiryMonth);
				custUserObj.defaultCardGoodThru = ccExpiry;

				CustUser custUserEJB = CustUserNut.getHandle(new Integer(strCustUserPkid));
				try
				{
					custUserEJB.setObject(custUserObj);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}

				QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = " + SOGuid + " " });
				Vector vecSalesOrder = new Vector(SalesOrderIndexNut.getObjects(query));
				for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
				{				
					SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);

					soObj.receiptMode		= cpcObj.paymentMode;
					soObj.receiptRemarks	= cpcObj.defaultPaymentRemarks;
					soObj.statusPayment		= cpcObj.defaultPaymentStatus;
					soObj.description		= paymentMode;
					SalesOrderIndex stEJB	= SalesOrderIndexNut.getHandle(soObj.pkid);
					stEJB.setObject(soObj);
				}


			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		}
		else if (formName.equals("promoPayModeMultiple") || formName.equals("validatePromoCodeMultiple"))
		{

			try
			{
				fnPromoMultiple(request, response);
				nextPage[0] = new String("/member_checkout_multiple.jsp");

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} else if (formName.equals("validatePromoCodeReselectSingle") 
			|| formName.equals("promoPayModeReselectSingle"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");

				String tempStr = "";
				String SOPkid = "0";
				tempStr = request.getParameter("SOPkid");
				if (tempStr != null) SOPkid = tempStr;

				EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
				csos.loadSalesOrder(new Long(SOPkid));
				SalesOrderIndexObject soObj  = csos.getSalesOrderIndex();

				String promoPkid = "";
				tempStr = request.getParameter("promoPkid");
				if (tempStr != null)
					promoPkid = tempStr;

				request.setAttribute("promoPkid", promoPkid);

				String promoNumber = "";
				tempStr = request.getParameter("promoNumber");
				if (tempStr != null)
					promoNumber = tempStr;

				BigDecimal discount = new BigDecimal(0);

				if(promoPkid != null && promoPkid.length() != 0)
				{
				
				try
				{
				EPromotionObject objPromo = EPromotionNut.getObject(new Long(promoPkid));
				if (objPromo != null)
				{
					csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));

					if(objPromo.promo_option.equals("payment_mode"))	 
						discount = objPromo.discount_pct;

					if (formName.equals("validatePromoCodeReselectSingle"))
					{
						if(objPromo.promo_option.equals("ticket_public"))	{

							if(objPromo.ticket_code.equals(promoNumber))
								discount = objPromo.discount_pct;

						} else if(objPromo.promo_option.equals("ticket_unique"))	{
							String query = " AND number = '" + promoNumber + "'";
							Vector vecProdTicket = new Vector(EPromotionNut.listProdTicket(new Long(promoPkid), query));
							if(vecProdTicket != null && vecProdTicket.size() > 0)
							{
								EPromoProdTicketObject stObj = (EPromoProdTicketObject) vecProdTicket.get(0);
								discount = stObj.discount_pct;	
							}
						}
					}

					if(discount.intValue() != 0)
					{
						BigDecimal totalDiscount = new BigDecimal(0);
						TreeMap tableRows = csos.getTableRows();
						Vector vecDocRow = new Vector(tableRows.values());
						for(int row1 = 0; row1 < vecDocRow.size(); row1++)
						{
						    DocRow docrow = (DocRow) vecDocRow.get(row1);							
							if(docrow.getItemId() != ItemBean.PKID_DELIVERY.intValue())
							{
								BigDecimal promoDiscountAmount = new BigDecimal(0);
								promoDiscountAmount = docrow.getPrice1();
								promoDiscountAmount = promoDiscountAmount.multiply(discount);
								promoDiscountAmount = promoDiscountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
								docrow.setDiscount(promoDiscountAmount);

								totalDiscount = totalDiscount.add(promoDiscountAmount);
							}
						}

						String promoType = objPromo.promo_option; 
						String promoCode = objPromo.code; 
						String promoName = objPromo.name; 
						BigDecimal promoDiscountPct = discount;

						csos.setPromo(promoType, promoCode, promoNumber, 
							promoName, totalDiscount, promoDiscountPct);
						
					}
					else
					{
						if (formName.equals("validatePromoCodeReselectSingle"))
						{	
							request.setAttribute(
									"errMsgPromo",
									"Sorry, you have provided an incorrect Promotion Code. ");
						}
					}
				}
				} // END TRY
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				} // END IF
				else
				{
					csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));
				} // END ELSE	

				nextPage[0] = new String("/member_checkout_single_reselect_payment.jsp?SOPkid="+SOPkid);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("reSelectSinglePayment"))
		{

			try
			{
				HttpSession session = request.getSession(true);

				String tempStr = "";
				String SOPkid = "0";
				tempStr = request.getParameter("SOPkid");
				if (tempStr != null) SOPkid = tempStr;
	
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
				EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
				csos.loadSalesOrder(new Long(SOPkid));

				fnReSelectPayment(request, response, csos, nextPage);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} else if (formName.equals("validatePromoCodeReselectMultiple") 
			|| formName.equals("promoPayModeReselectMultiple"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");

				String tempStr = "";
				String GUIDPkid = "0";
				tempStr = request.getParameter("GUIDPkid");
				if (tempStr != null) GUIDPkid = tempStr;
	
				QueryObject query2 = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = " + GUIDPkid });

				Vector vecSalesOrder  = new Vector(SalesOrderIndexNut.getObjects(query2));
				if (vecSalesOrder != null && vecSalesOrder.size() != 0)
				{
					String promoPkid = "";
					tempStr = request.getParameter("promoPkid");
					if (tempStr != null)
						promoPkid = tempStr;

					request.setAttribute("promoPkid", promoPkid);

					String promoNumber = "";
					tempStr = request.getParameter("promoNumber");
					if (tempStr != null)
						promoNumber = tempStr;

					EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);

					if(promoPkid!=null && promoPkid.length()>0)
         		{
         		try
         		{
					EPromotionObject objPromo = EPromotionNut.getObject(new Long(promoPkid));

					for(int cnt1=0;cnt1<vecSalesOrder.size();cnt1++)
					{
						SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);
						//EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
						//csos.loadSalesOrder(soObj.pkid);

						BigDecimal discount = new BigDecimal(0);
						if (objPromo != null)
						{
							csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));

							if(objPromo.promo_option.equals("payment_mode"))	 
								discount = objPromo.discount_pct;

							if (formName.equals("validatePromoCodeReselectMultiple"))
							{
								if(objPromo.promo_option.equals("ticket_public"))	{

									if(objPromo.ticket_code.equals(promoNumber))
										discount = objPromo.discount_pct;

								} else if(objPromo.promo_option.equals("ticket_unique"))	{
									String query = " AND number = '" + promoNumber + "'";
									Vector vecProdTicket = new Vector(EPromotionNut.listProdTicket(new Long(promoPkid), query));
									if(vecProdTicket != null && vecProdTicket.size() > 0)
									{
										EPromoProdTicketObject stObj = (EPromoProdTicketObject) vecProdTicket.get(0);
										discount = stObj.discount_pct;	
									}
								}
							}

							if(discount.intValue() != 0)
							{
								BigDecimal totalDiscount = new BigDecimal(0);
								TreeMap tableRows = csos.getTableRows();
								Vector vecDocRow = new Vector(tableRows.values());
								for(int row1 = 0; row1 < vecDocRow.size(); row1++)
								{
									DocRow docrow = (DocRow) vecDocRow.get(row1);							
									if(docrow.getItemId() != ItemBean.PKID_DELIVERY.intValue())
									{
										BigDecimal promoDiscountAmount = new BigDecimal(0);
										promoDiscountAmount = docrow.getPrice1();
										promoDiscountAmount = promoDiscountAmount.multiply(discount);
										promoDiscountAmount = promoDiscountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
										docrow.setDiscount(promoDiscountAmount);

										totalDiscount = totalDiscount.add(promoDiscountAmount);
									}
								}

								String promoType = objPromo.promo_option; 
								String promoCode = objPromo.code; 
								String promoName = objPromo.name; 
								BigDecimal promoDiscountPct = discount;

								csos.setPromo(promoType, promoCode, promoNumber, 
									promoName, totalDiscount, promoDiscountPct);
								
							}
							else
							{
								if (formName.equals("validatePromoCodeReselectMultiple"))
								{	
									request.setAttribute(
											"errMsgPromo",
											"Sorry, you have provided an incorrect Promotion Code. ");
								}
							}


						}
					}
					}	// END Try
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
					} // END IF
					else
					{
						csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));
					}
				}

				nextPage[0] = new String("/member_checkout_multiple_reselect_payment.jsp?GUIDPkid="+GUIDPkid);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("reSelectMultiplePayment"))
		{

			try
			{
				HttpSession session = request.getSession(true);

				String tempStr = "";	
				String GUIDPkid = "0";
				tempStr = request.getParameter("GUIDPkid");
				if (tempStr != null) GUIDPkid = tempStr;
	
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");

				QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = " + GUIDPkid });

				Vector vecSalesOrder  = new Vector(SalesOrderIndexNut.getObjects(query));
				if (vecSalesOrder != null && vecSalesOrder.size() != 0)
				{
					for(int cnt1=0;cnt1<vecSalesOrder.size();cnt1++)
					{
						SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);
						EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
						csos.loadSalesOrder(soObj.pkid);

						fnReSelectPayment(request, response, csos, nextPage);
					}
				}

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		}else if (formName.equals("checkExistEmail"))
		{
			try
			{
				HttpSession session = request.getSession(true);
				String senderEmail = (String) request.getParameter("senderEmail");
				
				Integer exist = new Integer(0);
				exist = CustUserNut.memberExist(CustAccountBean.ACCTYPE_PERSONAL, senderEmail);
				if (exist.intValue() != 0)
				{										
					String errExistEmail = "The email address already exists in our system." +
					 "Please LOGIN with the email address or provide another email address";
					
					session.setAttribute("errExistEmail", errExistEmail);
					
					//request.setAttribute("errExistEmail", "The email address already exists in our system." + "Please LOGIN with the email address or provide another email address");
				}		
				else
				{
					session.setAttribute("errExistEmail", "");					
				}

				Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");

				String tempStr = "";
				String strSOPkid = "0";
				tempStr = request.getParameter("SOPkid");
				if (tempStr != null)
					strSOPkid = tempStr;

				CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder.elementAt(Integer.parseInt(strSOPkid));
				csos = fnUpdate(request, response, csos, nextPage);
				vecSalesOrder.removeElementAt(Integer.parseInt(strSOPkid));
				vecSalesOrder.insertElementAt(csos, Integer.parseInt(strSOPkid));
				session.setAttribute("vecSalesOrder", vecSalesOrder);

				request.setAttribute("SOPkid", strSOPkid);
				
				nextPage[0] = new String("/member_checkout_single.jsp");

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}						
		}
		else if (formName.equals("checkLogin"))
		{
			try
			{
				HttpSession session = request.getSession(true);				
				if (session.getAttribute("userName")!=null)   { 
					String gotoPage = request.getParameter("gotoPage");
					nextPage[0] = new String(gotoPage);
				}
				else
				{
					String errPage = request.getParameter("errPage");
					nextPage[0] = new String(errPage);
				}

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 
		else if (formName.equals("validateLogin"))
		{
			try
			{
				String uname = request.getParameter("email");
				String pwd = request.getParameter("password");
				HttpSession session = request.getSession(true);				
				Vector vecMember = new Vector();
				vecMember = CustUserNut.memberLoginValidate(uname, pwd);
				if (vecMember != null && vecMember.size() > 0)
				{
					if (vecMember.size() == 1)
					{
						session.setAttribute("userName", uname);
						CustUserObject ojb = (CustUserObject) vecMember.get(0);
						String fullname = "";
						if (!ojb.title.equals(""))
							fullname += ojb.title;
						if (!ojb.nameFirst.equals(""))
							fullname += " " + ojb.nameFirst;
						if (!ojb.nameLast.equals(""))
							fullname += " " + ojb.nameLast;
						session.setAttribute("userFullName", fullname);
						session.setAttribute("userId", ojb.pkid.toString());
						
					} 
					else
					{
						request.setAttribute("vecMember", vecMember);
					}

					String gotoPage = request.getParameter("gotoPage");
					nextPage[0] = new String(gotoPage);

				} else
				{
					request.setAttribute("errMsg", "Invalid login information. Please try again.");
					String errPage = request.getParameter("errPage");
					nextPage[0] = new String(errPage);
				}
				

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} 

		System.out.println("NextPage value : "+nextPage[0]);
		
		request.setAttribute("formName", formName);
		if(nextPage[0].indexOf("https")>-1) 
		{
			System.out.println("https exist");
			
			String url = response.encodeURL(nextPage[0]);
			
			System.out.println("URL :"+url);
			
			response.sendRedirect(url);
		}else
			getServletContext().getRequestDispatcher(response.encodeRedirectURL(nextPage[0])).forward(request, response);

	}


	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		processRequest(request, response);
	}

	public void fnLogin(HttpServletRequest req, HttpServletResponse res, String nextPage[])
			throws Exception
	{
		String tempStr = "";
		tempStr = req.getParameter("thisPage");
		if (tempStr != null)
			nextPage[0] = tempStr;

		String uname = req.getParameter("email");
		String pwd = req.getParameter("password");
		HttpSession session = req.getSession(true);
		if (session.getAttribute("userId") == null)
		{

			Vector vecMember = new Vector();
			vecMember = CustUserNut.memberLoginValidate(uname, pwd);
			if (vecMember != null && vecMember.size() > 0)
			{
				if (vecMember.size() == 1)
				{
					session.setAttribute("userName", uname);
					CustUserObject ojb = (CustUserObject) vecMember.get(0);
					String fullname = "";
					if (!ojb.title.equals(""))
						fullname += ojb.title;
					if (!ojb.nameFirst.equals(""))
						fullname += " " + ojb.nameFirst;
					if (!ojb.nameLast.equals(""))
						fullname += " " + ojb.nameLast;
					session.setAttribute("userFullName", fullname);
					session.setAttribute("userId", ojb.pkid.toString());
				}
			} else
			{
				req
						.setAttribute(
								"errMsg",
								"Sorry, you have provided an incorrect Email Address and/or Password. Please try again. ");
			}
		}

	}

	public CreateSalesOrderSession fnUpdate(HttpServletRequest req,
			HttpServletResponse res, CreateSalesOrderSession csos, String nextPage[])
			throws Exception
	{
		System.out.println("Inside DoMemberCheckout.fnUpdate");
		
		HttpSession session = req.getSession(true);
		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
		SalesOrderIndexObject soObj = csos.getSalesOrderIndex();

		String thirdpartyLoyaltyCardCode = "";
		String thirdpartyLoyaltyCardNumber = "";

		boolean bRInvoice = false;
		boolean bRReceipt = false;

		String soType1 = SalesOrderIndexBean.SO_TYPE1_PERSONAL;
		String flagSender = "";
		String flagReceiver = "";
		String managerPassword = "";
		String remarks = "Not shipped";
		String occasion = req.getParameter("occasion");

		BigDecimal interfloraPrice = new BigDecimal(0);
		String interfloraFlowers1 = "";

		csos.setInfo(csos.getBranch().pkid, remarks, bRInvoice, bRReceipt,
				objStore.so_flagInternal, flagSender, flagReceiver,
				managerPassword, soType1, occasion, thirdpartyLoyaltyCardCode,
				thirdpartyLoyaltyCardNumber, interfloraPrice, interfloraFlowers1);

		System.out.println("csos.getBranch : "+csos.getBranch().pkid.toString());

		String location_type = req.getParameter("location_type");
		session.setAttribute("location_type", location_type);

		session.setAttribute("receiptMode", null);

		soObj.receiptBranch = objStore.so_receiptBranch;

		String tempStr = "";
		String receiverTitle = req.getParameter("receiverTitle");
		String receiverName = "";
		tempStr = req.getParameter("receiverFirstName");
		if (tempStr != null)
			receiverName = tempStr;
		tempStr = req.getParameter("receiverLastName");
		if (tempStr != null)
			receiverName += " " + tempStr;
		String receiverIdentityNumber = "";
		String receiverHandphone = "";
		tempStr = req.getParameter("receiverHandphone");
		if (tempStr != null)
			receiverHandphone = tempStr;
		String receiverEmail = "";
		tempStr = req.getParameter("receiverEmail");
		if (tempStr != null)
			receiverEmail = tempStr;
		String receiverFax = "";
		tempStr = req.getParameter("receiverFax");
		if (tempStr != null)
			receiverFax = tempStr;
		String receiverPhone1 = "";
		tempStr = req.getParameter("receiverPhone1");
		if (tempStr != null)
			receiverPhone1 = tempStr;
		String receiverPhone2 = "";
		String receiverCompanyName = "";
		tempStr = req.getParameter("receiverCompanyName");
		if (tempStr != null)
			receiverCompanyName = tempStr;
		String receiverAdd1 = "";
		tempStr = req.getParameter("receiverAdd1");
		if (tempStr != null)
			receiverAdd1 = tempStr;
		String receiverAdd2 = "";
		tempStr = req.getParameter("Address2");
		if (tempStr != null)
			receiverAdd2 = tempStr;
		String receiverAdd3 = "";
		tempStr = req.getParameter("Address3");
		if (tempStr != null)
			receiverAdd3 = tempStr;
		String receiverCity = "";
		tempStr = req.getParameter("City");
		if (tempStr != null)
			receiverCity = tempStr;
		String receiverZip = "";
		tempStr = req.getParameter("PostalCode");
		if (tempStr != null)
			receiverZip = tempStr;
		String receiverState = "";
		tempStr = req.getParameter("State");
		if (tempStr != null)
			receiverState = tempStr;
		String receiverCountry = "";
		tempStr = req.getParameter("receiverCountry");
		if (tempStr != null)
			receiverCountry = tempStr;

		String deliveryPreferences = req.getParameter("deliveryPreferences");

		String senderName = "";
		tempStr = req.getParameter("senderFirstName");
		if (tempStr != null)
			senderName = tempStr;
		tempStr = req.getParameter("senderLastName");
		if (tempStr != null)
			senderName += " " + tempStr;
		String senderIdentityNumber = "";
		String senderHandphone = "";
		tempStr = req.getParameter("senderHandphone");
		if (tempStr != null)
			senderHandphone = tempStr;
		String senderFax = "";
		tempStr = req.getParameter("senderFax");
		if (tempStr != null)
			senderFax = tempStr;
		String senderPhone1 = "";
		tempStr = req.getParameter("senderPhone1");
		if (tempStr != null)
			senderPhone1 = tempStr;
		String senderPhone2 = "";
		String senderCompanyName = "";
		String senderAdd1 = "";
		tempStr = req.getParameter("senderAdd1");
		if (tempStr != null)
			senderAdd1 = tempStr;
		String senderAdd2 = "";
		tempStr = req.getParameter("senderAdd2");
		if (tempStr != null)
			senderAdd2 = tempStr;
		String senderAdd3 = "";
		tempStr = req.getParameter("senderAdd3");
		if (tempStr != null)
			senderAdd3 = tempStr;
		String senderCity = "";
		tempStr = req.getParameter("senderCity");
		if (tempStr != null)
			senderCity = tempStr;
		String senderZip = "";
		tempStr = req.getParameter("senderZip");
		if (tempStr != null)
			senderZip = tempStr;
		String senderState = "";
		tempStr = req.getParameter("senderState");
		if (tempStr != null)
			senderState = tempStr;
		String senderCountry = "";
		tempStr = req.getParameter("senderCountry");
		if (tempStr != null)
			senderCountry = tempStr;
		String senderEmail = "";
		tempStr = req.getParameter("senderEmail");
		if (tempStr != null)
			senderEmail = tempStr;

		String deliveryTo = "";
		String deliveryToName = "";
		String deliveryFrom = "";
		String deliveryFromName = "";
		String deliveryMsg1 = req.getParameter("deliveryMsg1");
		//String expDeliveryTime = req.getParameter("expDeliveryTime");
		String expDeliveryTimeStart = req.getParameter("expDeliveryTimeStart");

		String expDeliveryTime = "";
                tempStr = req.getParameter("expDeliveryTime");
                if (tempStr != null)
                        expDeliveryTime = tempStr;

		String strDeliveryLocationPkid = "0";
		tempStr = req.getParameter("deliveryLocationPkid");
		if (tempStr != null)
			strDeliveryLocationPkid = tempStr;

		String promoPkid = "";
		tempStr = req.getParameter("promoPkid");
		if (tempStr != null)
			promoPkid = tempStr;

		req.setAttribute("promoPkid", promoPkid);

		String promoNumber = "";
		tempStr = req.getParameter("promoNumber");
		if (tempStr != null)
			promoNumber = tempStr;

		System.out.println("Inside DoMemberCheckout.fnUpdate Checkpoint AAA");

		String formName = "";
		formName = req.getParameter("formName");
		
		System.out.println("Inside DoMemberCheckout.fnUpdate formName : "+formName);
		
		if (formName.equals("login"))
		{
			try
			{
				System.out.println("Inside DoMemberCheckout.fnUpdate Login");
				
				fnLogin(req, res, nextPage);
				String strCustUserPkid = (String) session.getAttribute("userId");
				if (strCustUserPkid != null)
				{
					System.out.println("Inside DoMemberCheckout.fnUpdate userId : "+strCustUserPkid);
					
					Integer custUserPkid = new Integer(strCustUserPkid);
					CustUserObject custUserObj = CustUserNut.getObject(custUserPkid);
					csos.setCustUser(custUserObj);

					senderName = custUserObj.nameFirst + " " + custUserObj.nameLast;
					senderHandphone = custUserObj.mobilePhone;
					senderPhone1 = custUserObj.telephone1;
					senderAdd1 = custUserObj.mainAddress1;
					senderAdd2 = custUserObj.mainAddress2;
					senderAdd3 = custUserObj.mainAddress3;
					senderCity = custUserObj.mainCity.toUpperCase();
					senderZip = custUserObj.mainPostcode;
					senderState = custUserObj.mainState.toUpperCase();
					senderCountry = custUserObj.mainCountry.toUpperCase();
					senderEmail = custUserObj.email1;
				}

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} else if (formName.equals("selectContact"))
		{
			try
			{
				System.out.println("Inside DoMemberCheckout.fnUpdate selectContact");
				
				String address_book = (String) req.getParameter("address_book");
				req.setAttribute("address_book", address_book);
				EMemberAddressBkObject contactObj = EMemberAddressBkNut.getObject(new Long(address_book));
				session.setAttribute("contactObj", contactObj);

				receiverTitle = contactObj.title;
				receiverName = contactObj.namefirst + " " + contactObj.namelast;
				receiverHandphone = contactObj.mobilephone;
				receiverPhone1 = contactObj.telephone1;
				receiverAdd1 = contactObj.address1;
				receiverAdd2 = contactObj.address2;
				receiverAdd3 = contactObj.address3;
				receiverCity = contactObj.city.toUpperCase();
				receiverZip = contactObj.postcode;
				receiverState = contactObj.state.toUpperCase();
				receiverCountry = contactObj.country.toUpperCase();

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} else if (formName.equals("switchNonValentine"))
		{

			System.out.println("Inside DoMemberCheckout.fnUpdate switchNonValentine");
			
			ShoppingCart cart = (ShoppingCart) session.getAttribute("soCart");
			if (cart != null && cart.size() > 0)
			{
				System.out.println("Inside DoMemberCheckout.fnUpdate cart size > 0");
				
				for (int cnt1 = 0; cnt1 < cart.size(); cnt1++)
				{
					System.out.println("Inside DoMemberCheckout.fnUpdate switchNonValentine checkpoint BBB");
					
					CartItem items = cart.cartItemAt(cnt1);
					if(items.invreserved1.equals(ItemBean.RESERVED1_VALENTINE))
					{
						System.out.println("Inside DoMemberCheckout.fnUpdate switchNonValentine checkpoint CCC");
						
						QueryObject query = new QueryObject(new String[] { EProOptionBean.PRODUCT_ID + " = " + items.productId + " " });
						Vector vecOptions = new Vector(EProOptionNut.getObjects(query));
						for(int cnt2=0;cnt2<vecOptions.size();cnt2++)
						{
							System.out.println("Inside DoMemberCheckout.fnUpdate switchNonValentine checkpoint DDD");
							
							EProOptionObject stObj = (EProOptionObject) vecOptions.get(cnt2);
							if(stObj.invtype.equals(ItemBean.ITEM_TYPE_INVENTORY))
							{
								System.out.println("Inside DoMemberCheckout.fnUpdate switchNonValentine checkpoint EEE");
								
								ItemObject itemObj = ItemNut.getObject(new Integer(stObj.invid.toString()));
								stObj.invprice = itemObj.priceList;
								stObj.invreserved1 = itemObj.reserved1;
								if(stObj.invreserved1.equals(ItemBean.RESERVED1_NON_VALENTINE))
								{
									System.out.println("Inside DoMemberCheckout.fnUpdate switchNonValentine checkpoint FFF");
									
									CartItem nitem		= new CartItem();
									nitem.optionId		= stObj.pkid;
									nitem.productId		= items.productId;
									nitem.productCode	= items.productCode;
									nitem.productName	= items.productName;
									nitem.productThumbnail = items.productThumbnail;
									nitem.optionName	= stObj.title;
									nitem.optionPrice	= stObj.invprice;
									nitem.optionType	= stObj.invtype;
									nitem.invid			= stObj.invid;
									nitem.invreserved1	= stObj.invreserved1;
									nitem.addOnId		= items.addOnId;
									nitem.qty			= items.qty;

									cart.removeItemAt(cnt1);
									cart.insertItemAt(nitem, cnt1);
									break;
								}
							}
						}
					}
				}
			}

		} 
		else if (formName.equals("switchValentine"))
		{
			System.out.println("Inside DoMemberCheckout.fnUpdate switchValentine");

			ShoppingCart cart = (ShoppingCart) session.getAttribute("soCart");
			if (cart != null && cart.size() > 0)
			{
				System.out.println("Inside DoMemberCheckout.fnUpdate switchValentine checkpoint AAA");
				csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));
				for (int cnt1 = 0; cnt1 < cart.size(); cnt1++)
				{
					System.out.println("Inside DoMemberCheckout.fnUpdate switchValentine checkpoint BBB");
					
					CartItem items = cart.cartItemAt(cnt1);
					if(items.invreserved1.equals(ItemBean.RESERVED1_NON_VALENTINE))
					{
						System.out.println("Inside DoMemberCheckout.fnUpdate switchValentine checkpoint CCC");
						
						QueryObject query = new QueryObject(new String[] { EProOptionBean.PRODUCT_ID + " = " + items.productId + " " });
						Vector vecOptions = new Vector(EProOptionNut.getObjects(query));
						for(int cnt2=0;cnt2<vecOptions.size();cnt2++)
						{
							System.out.println("Inside DoMemberCheckout.fnUpdate switchValentine checkpoint DDD");
							
							EProOptionObject stObj = (EProOptionObject) vecOptions.get(cnt2);
							if(stObj.invtype.equals(ItemBean.ITEM_TYPE_INVENTORY))
							{
								System.out.println("Inside DoMemberCheckout.fnUpdate switchValentine checkpoint EEE");
								
								ItemObject itemObj = ItemNut.getObject(new Integer(stObj.invid.toString()));
								stObj.invprice = itemObj.priceList;
								stObj.invreserved1 = itemObj.reserved1;
								if(stObj.invreserved1.equals(ItemBean.RESERVED1_VALENTINE))
								{
									System.out.println("Inside DoMemberCheckout.fnUpdate switchValentine checkpoint FFF");
									
									cart.removeItemAt(cnt1);
									
									CartItem nitem		= new CartItem();
									nitem.optionId		= stObj.pkid;
									nitem.productId		= items.productId;
									nitem.productCode	= items.productCode;
									nitem.productName	= items.productName;
									nitem.productThumbnail = items.productThumbnail;
									nitem.optionName	= stObj.title;
									nitem.optionPrice	= stObj.invprice;
									nitem.optionType	= stObj.invtype;
									nitem.invid			= stObj.invid;
									nitem.invreserved1	= stObj.invreserved1;
									nitem.addOnId		= items.addOnId;
									cart.insertItemAt(nitem, cnt1);
									break;
								}
							}
						}
					}
				}
			}

		} 
		else if (formName.equals("validatePromoCode") || formName.equals("promoPayMode"))
		{	
			System.out.println("Inside DoMemberCheckout.fnUpdate promo checkpoint AAA");
			
			BigDecimal discount = new BigDecimal(0);
			if(promoPkid!=null && promoPkid.length()>0)
			{	
				try
				{
					System.out.println("Inside DoMemberCheckout.fnUpdate promo checkpoint BBB");
						
					EPromotionObject objPromo = EPromotionNut.getObject(new Long(promoPkid));
					
					if (objPromo != null)
					{
						System.out.println("Inside DoMemberCheckout.fnUpdate promo objPromo not null");
						
						csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));
		
						if(objPromo.promo_option.equals("payment_mode"))	 
							discount = objPromo.discount_pct;
		
						if (formName.equals("validatePromoCode"))
						{
							System.out.println("Inside DoMemberCheckout.fnUpdate validatePromoCode");
							
							if(objPromo.promo_option.equals("ticket_public"))	
							{
								System.out.println("Inside DoMemberCheckout.fnUpdate ticket_public");
								
								if(objPromo.ticket_code.equals(promoNumber))
									discount = objPromo.discount_pct;
		
							} else if(objPromo.promo_option.equals("ticket_unique"))	{
								
								System.out.println("Inside DoMemberCheckout.fnUpdate ticket_unique");
								
								String query = " AND number = '" + promoNumber + "'";
								Vector vecProdTicket = new Vector(EPromotionNut.listProdTicket(new Long(promoPkid), query));
								if(vecProdTicket != null && vecProdTicket.size() > 0)
								{
									System.out.println("Inside DoMemberCheckout.fnUpdate vecProdTicket not null");
									
									EPromoProdTicketObject stObj = (EPromoProdTicketObject) vecProdTicket.get(0);
									discount = stObj.discount_pct;	
								}
							}
						}
		
						if(discount.intValue() != 0)
						{
							System.out.println("Inside DoMemberCheckout.fnUpdate discount > rm0");
							
							BigDecimal totalDiscount = new BigDecimal(0);
							ShoppingCart cart = (ShoppingCart) session.getAttribute("soCart");
							
							String entitledFlag = "";
							String errorFlag = "";
							
							if (cart != null && cart.size() > 0)
							{
								System.out.println("Inside DoMemberCheckout.fnUpdate cart not null");
								
								for (int cnt1 = 0; cnt1 < cart.size(); cnt1++)
								{
									System.out.println("Inside DoMemberCheckout.fnUpdate looping through cart");
									
									CartItem items = cart.cartItemAt(cnt1);
		
									BigDecimal promoDiscountAmount = new BigDecimal(0);
									promoDiscountAmount = items.optionPrice; //Original item price
									
									List listing = null;
									StringTokenizer tokenizer = null;
									tokenizer = new StringTokenizer(items.addOnId, "," );
		
									listing = new ArrayList();
									while ( tokenizer.hasMoreTokens() ) 
									{
									  listing.add( tokenizer.nextToken() );
									}
		
									for (Iterator i = listing.iterator(); i.hasNext();) 
									{
										
										System.out.println("Inside DoMemberCheckout.fnUpdate discount checkpoint DDD");
										
										String strPkid =(String)i.next();
										EProAddOnObject objAddOn = EProAddOnNut.getObject(new Long(strPkid));
		
										ItemObject itemObj = ItemNut.getObject(new Integer(objAddOn.invid.toString()));
										objAddOn.invprice = itemObj.priceList;
		
										if(EProAddOnNut.isDiscount(objAddOn.pkid)) //If addon already got own discount, then not entitle to promo
										{								
											System.out.println("Inside DoMemberCheckout.fnUpdate addon has own discount");
																				
											BigDecimal discPrice = new BigDecimal(0);
											discPrice = itemObj.priceList;
											discPrice = discPrice.subtract(objAddOn.disc_value);
											objAddOn.invprice = discPrice; //Addon price afte own discount
											
											errorFlag += objAddOn.code + ", ";
										}
										else
										{
											if("PROMO-ENTITLED".equals(itemObj.reserved2))
											{
												promoDiscountAmount = promoDiscountAmount.add(objAddOn.invprice);
											}
											else
											{
												System.out.println("AddOn "+itemObj.code+" is not entitle to promotions");
												
												errorFlag += objAddOn.code + ", ";
											}
										}
									}	
		
									ItemObject itemObj2 = ItemNut.getObject(new Integer(items.invid.toString()));
																
									if("PROMO-ENTITLED".equals(itemObj2.reserved2))
									{
										System.out.println("items.invreserved2 is PROMO-ENTITLED");
										
										promoDiscountAmount = promoDiscountAmount.multiply(discount);
										promoDiscountAmount = promoDiscountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
										items.discount = discount;
										
										totalDiscount = totalDiscount.add(promoDiscountAmount.multiply(new BigDecimal(items.qty)));
										
										entitledFlag = "true";
									}
									else
									{
										System.out.println("items.invreserved2 : "+itemObj2.reserved2);
										
										Item itemEJB = ItemNut.getObjectByPkid(items.invid.toString());
										
										ItemObject itemObjOption = itemEJB.getObject();
										
										errorFlag += itemObjOption.code + ", ";
									}
								}
							}
							
							String promoType = ""; 
							String promoCode = "";
							String promoName = "";
							BigDecimal promoDiscountPct = new BigDecimal(0);
							
							if("true".equals(entitledFlag))
							{
								System.out.println("entitledFlag is true");
								
								promoType = objPromo.promo_option; 
								promoCode = objPromo.code; 
								promoName = objPromo.name; 
								promoDiscountPct = discount;
							}
							else
							{
								System.out.println("entitledFlag is false");
								System.out.println("errorFlag : "+errorFlag);
								
								req.setAttribute("errMsgPromo", "Sorry, "+errorFlag+" is not entitle to the promotion");
							}
							
							csos.setPromo(promoType, promoCode, promoNumber, promoName, totalDiscount, promoDiscountPct);
							
						}
						else
						{
							if (formName.equals("validatePromoCode"))
							{	
								req.setAttribute("errMsgPromo", "Sorry, you have provided an incorrect Promotion Code. ");
							}
						}
				}
				}
	            catch (Exception ex)
	            {
	            	ex.printStackTrace();
	            }
			}// end if
			else
			{
				csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));
				
				ShoppingCart cart = (ShoppingCart) session.getAttribute("soCart");
				if (cart != null && cart.size() > 0)
				{
					System.out.println("Inside DoMemberCheckout.fnUpdate reseting the discount");
					
					for (int cnt1 = 0; cnt1 < cart.size(); cnt1++)
					{
						CartItem items = cart.cartItemAt(cnt1);
						items.discount = new BigDecimal(0);
					}
				}
				
				System.out.println("Inside DoMemberCheckout.fnUpdate promoCode is null");
			} // END ELSE

		}		

		String receiverLocationType = SalesOrderIndexBean.INTERNET;
		
		csos.setDeliveryDetails(deliveryTo, deliveryToName, deliveryFrom,
				deliveryFromName, deliveryMsg1, expDeliveryTime,
				expDeliveryTimeStart, deliveryPreferences, senderName,
				senderIdentityNumber, senderEmail, senderHandphone, senderFax, senderPhone1,
				senderPhone2, "", senderCompanyName, senderAdd1, senderAdd2,
				senderAdd3, senderCity.toUpperCase(), senderZip.toUpperCase(),
				senderState.toUpperCase(), senderCountry.toUpperCase(),
				receiverTitle, receiverName, receiverIdentityNumber, "",
				receiverHandphone, receiverFax, receiverPhone1, receiverPhone2,
				receiverCompanyName, receiverAdd1, receiverAdd2, receiverAdd3,
				receiverCity, receiverZip, receiverState, receiverCountry, receiverLocationType);

		System.out.println("Inside DoMemberCheckout.fnUpdate already setDelivery");
		
		req.setAttribute("senderEmail", senderEmail);
		req.setAttribute("deliveryLocationPkid", strDeliveryLocationPkid);

		String[] selectGift = (String[]) req.getParameterValues("selectGift");
		if (selectGift != null)
		{
			System.out.println("Inside DoMemberCheckout.fnUpdate selectGift");
			
			Vector vecSelectedGift = new Vector();
			for (int i = 0; i < selectGift.length; i++)
			{
				System.out.println("Inside DoMemberCheckout.fnUpdate selectGift checkpoint AAA");
				
				vecSelectedGift.add((String) selectGift[i]);
			}
			req.setAttribute("vecSelectedGift", vecSelectedGift);
		}

		fnGetPromoList(req,res);

		return csos;
	}

	public CreateSalesOrderSession fnCheckout(HttpServletRequest req,
			HttpServletResponse res, ShoppingCart cart,
			CreateSalesOrderSession csos) throws Exception
	{
		
		HttpSession session = req.getSession(true);
		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
		csos.setBranch(objStore.so_branchId);
		BranchObject branchObj = BranchNut.getObject(objStore.so_branchId);
		csos.setProductionLocation(branchObj.invLocationId);

		String senderName = "";
		String senderHandphone = "";
		String senderPhone1 = "";
		String senderAdd1 = "";
		String senderAdd2 = "";
		String senderAdd3 = "";
		String senderCity = "";
		String senderZip = "";
		String senderState = "";
		String senderCountry = "";
		String senderEmail = "";

		String strCustUserPkid = (String) session.getAttribute("userId");
		if (strCustUserPkid != null)
		{
			Integer custUserPkid = new Integer(strCustUserPkid);
			CustUserObject custUserObj = CustUserNut.getObject(custUserPkid);
			csos.setCustUser(custUserObj);

			senderName = custUserObj.nameFirst + " " + custUserObj.nameLast;
			senderHandphone = custUserObj.mobilePhone;
			senderPhone1 = custUserObj.telephone1;
			senderAdd1 = custUserObj.mainAddress1;
			senderAdd2 = custUserObj.mainAddress2;
			senderAdd3 = custUserObj.mainAddress3;
			senderCity = custUserObj.mainCity.toUpperCase();
			senderZip = custUserObj.mainPostcode;
			senderState = custUserObj.mainState.toUpperCase();
			senderCountry = custUserObj.mainCountry.toUpperCase();
			senderEmail = custUserObj.email1;

		}

		csos.setDisplayFormat(SalesOrderIndexBean.DF_QUICK_ORDER);
		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);

		SalesOrderIndexObject soObj = csos.getSalesOrderIndex();

		String userCountry = "MALAYSIA";

		String tempStr = "";
		int ctryId = 0;
		tempStr = req.getParameter("ctryId");
		if (tempStr != null)
			ctryId = Integer.parseInt(tempStr);

		if (ctryId != 0)
		{
			ECountryObject objCtry = ECountryNut.getObject(new Long(ctryId));
			userCountry = objCtry.country_name;
		}

		String receiverLocationType = SalesOrderIndexBean.INTERNET;
		
		csos.setDeliveryDetails("", "", "", senderName, "", "", "", "",
				senderName, "", senderEmail, senderHandphone, "", senderPhone1, "", "", "",
				senderAdd1, senderAdd2, senderAdd3, senderCity.toUpperCase(),
				senderZip, senderState.toUpperCase(), senderCountry
						.toUpperCase(), "", "", "", "", "", "", "", "", "", "", "",
				"", "", "", "", userCountry, receiverLocationType);

		req.setAttribute("senderEmail", senderEmail);
	
		fnGetPromoList(req,res);

		return csos;
	}

	public CreateSalesOrderSession fnCreateSOSingle(HttpServletRequest req,
			HttpServletResponse res, ShoppingCart cart,
			CreateSalesOrderSession csos, String nextPage[]) throws Exception
	{
		try{		
		
		HttpSession session = req.getSession(true);
		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");

		String tempStr = "";
		String senderEmail = "";
		tempStr = req.getParameter("senderEmail");
		if (tempStr != null)
			senderEmail = tempStr;

		String strCustUserPkid = (String) session.getAttribute("userId");
		if (strCustUserPkid == null)
		{
			System.out.println("--------------------- Creating a new cust user account !! ");
			
			fnCreatePersonalAccount(req, res, objStore);
			strCustUserPkid = (String) session.getAttribute("userId");
		}
		else
		{
			System.out.println("---------------------- Updating the cust user account !! ");
			 
			SalesOrderIndexObject soObj = csos.getSalesOrderIndex();
			Integer custUserPkid = new Integer(strCustUserPkid);
			CustUserObject custUser = CustUserNut.getObject(custUserPkid);		
			CustUser custUserEJB = CustUserNut.getHandle(custUser.pkid);
			
			custUser.mobilePhone = soObj.senderHandphone; 
			custUser.faxNo = soObj.senderFax;
			custUser.telephone1 = soObj.senderPhone1;
			custUser.telephone2 = soObj.senderPhone2;
			custUser.mainAddress1 = soObj.senderAdd1;
			custUser.mainAddress2 = soObj.senderAdd2;
			custUser.mainAddress3 = soObj.senderAdd3;
			custUser.mainPostcode = soObj.senderZip;
			custUser.mainState = soObj.senderState;
			custUser.mainCountry = soObj.senderCountry;
			
			custUserEJB.setObject(custUser);			
		}
		
		Integer custUserPkid = new Integer(strCustUserPkid);
		CustUserObject custUserObj = CustUserNut.getObject(custUserPkid);
		csos.setCustUser(custUserObj);

		String addBk = "";
		tempStr = req.getParameter("addBk");
		if (tempStr != null)
			addBk = tempStr;

		if (addBk.equals("true"))
		{

			EMemberAddressBkObject obj = new EMemberAddressBkObject();
			obj.memberId = new Long(strCustUserPkid);

			tempStr = req.getParameter("receiverTitle");
			if (tempStr != null)
				obj.title = tempStr;
			tempStr = req.getParameter("receiverFirstName");
			if (tempStr != null)
				obj.namefirst = tempStr;
			tempStr = req.getParameter("receiverLastName");
			if (tempStr != null)
				obj.namelast = tempStr;

			obj.nickname = obj.namefirst + " " + obj.namelast;

			tempStr = req.getParameter("receiverCompanyName");
			if (tempStr != null)
				obj.company = tempStr;
			tempStr = req.getParameter("receiverAdd1");
			if (tempStr != null)
				obj.address1 = tempStr;
			tempStr = req.getParameter("Address2");
			if (tempStr != null)
				obj.address2 = tempStr;
			tempStr = req.getParameter("Address3");
			if (tempStr != null)
				obj.address3 = tempStr;
			tempStr = req.getParameter("City");
			if (tempStr != null)
				obj.city = tempStr.toUpperCase();
			tempStr = req.getParameter("PostalCode");
			if (tempStr != null)
				obj.postcode = tempStr;
			tempStr = req.getParameter("State");
			if (tempStr != null)
				obj.state = tempStr.toUpperCase();
			tempStr = req.getParameter("receiverCountry");
			if (tempStr != null)
				obj.country = tempStr.toUpperCase();
			tempStr = req.getParameter("receiverPhone1");
			if (tempStr != null)
				obj.telephone1 = tempStr;
			tempStr = req.getParameter("receiverHandphone");
			if (tempStr != null)
				obj.mobilephone = tempStr;

			EMemberAddressBkNut.fnCreate(obj);
		}

		SalesOrderIndexObject soObj = csos.getSalesOrderIndex();
	
		BranchObject branch = csos.getBranch();
		
		Integer cartSize = new Integer(cart.size());
		for (int i = 0; i < cart.size(); i++)
		{
			CartItem item = cart.cartItemAt(i);
			if (item.optionType.equals(ItemBean.ITEM_TYPE_INVENTORY))
			{				
				csos = fnAddSOItemTypeTypeInventory(csos, item, new BigDecimal(item.qty), objStore);

			} else if (item.optionType.equals(ItemBean.ITEM_TYPE_PACKAGE))
			{
				csos = fnAddSOItemTypeTypePackage(csos, item, new BigDecimal(item.qty), objStore);
			}			
			
			csos = fnAddSOItemTypeTypeAddOn(csos, item, objStore);
		}

		String strDeliveryLocationPkid = "0";

		Log.printVerbose("strDeliveryLocationPkid "+strDeliveryLocationPkid);

		tempStr = req.getParameter("deliveryLocationPkid");
		if (tempStr != null)
		{
			strDeliveryLocationPkid = tempStr;
			csos = fnAddSOItemTypeDelivery(csos, new Long(strDeliveryLocationPkid), objStore, soObj);
		}

		String receiptMode = req.getParameter("receiptMode");
		
//		String etxnStatus = "";
//		tempStr = (String) req.getParameter("OthersNo");
//		
//		System.out.println("Checking othersNo");
//		
//		if (tempStr != null) 
//			etxnStatus = tempStr;
//				
//		if(!etxnStatus.equals(""))
//			csos.setEtxnStatus(etxnStatus);
//		
//		System.out.println("othersNo "+etxnStatus);
		
		EPaymentConfigObject objConfig = EPaymentConfigNut.getObject(new Long(receiptMode));
		Integer receiptModeId = new Integer(String.valueOf(objConfig.payment_config));
		CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(receiptModeId);
		
		if (cpcObj != null)
		{
			csos.setReceiptMode(cpcObj.paymentMode);
			csos.setPaymentRemarks(cpcObj.defaultPaymentRemarks);
			csos.setPaymentStatus(cpcObj.defaultPaymentStatus);
		}

		tempStr = req.getParameter("receiptApprovalCode" + receiptMode);
		if (tempStr != null) soObj.receiptApprovalCode = tempStr;

		System.out.println("etxnType: "+req.getParameter("etxnType"));
		
		tempStr = req.getParameter("etxnType");
		if (tempStr != null) soObj.etxnType = tempStr;

		csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
		csos.resetting = true;
		soObj.guid = "";
		soObj.receiptBranch = objStore.so_receiptBranch;

		Log.printVerbose("Going to create Sales Order and Receipt");

		csos.createSalesOrderAndReceipt();

		Log.printVerbose("CSOS created Sales Order and Receipt");

		System.out.println("soObj.guid : "+soObj.guid);
		
		fnSalesOrderAmountTotal(req, res, soObj.guid);

		csos.setDescription(fnNextPageAfterCheckout(req, res, objConfig, String.valueOf(soObj.pkid), soObj.guid, nextPage));

		//fnSendEmailSalesOrder(req, res, soObj, csos.getTableRows(), csos.getBillAmount(), csos.getDescription());

		req.setAttribute("SOGuid", soObj.guid);
		req.setAttribute("senderEmail", senderEmail);
		req.setAttribute("receiptMode", receiptMode);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return csos;
	}

	public CreateSalesOrderSession fnMultipleSelect(HttpServletRequest req,
			HttpServletResponse res, CreateSalesOrderSession csos, String nextPage[])
			throws Exception
	{
		try{		
		
		HttpSession session = req.getSession(true);
		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");

		String tempStr = "";
		boolean empty = false;
		ShoppingCart cart = (ShoppingCart) session.getAttribute("soCart");
		if (cart != null && cart.size() > 0)
		{

			BranchObject branch = csos.getBranch();
			for (int cnt1 = 0; cnt1 < cart.size(); cnt1++)
			{
				CartItem item = cart.cartItemAt(cnt1);
				int qty = 0;
				tempStr = req.getParameter("item" + cnt1);
				if (tempStr != null)
					qty = Integer.parseInt(tempStr);

				if (qty != 0)
				{
					if (item.optionType
							.equals(ItemBean.ITEM_TYPE_INVENTORY))
					{
						csos = fnAddSOItemTypeTypeInventory(csos, item, new BigDecimal(qty), objStore);
					} else if (item.optionType
							.equals(ItemBean.ITEM_TYPE_PACKAGE))
					{

						csos = fnAddSOItemTypeTypePackage(csos, item, new BigDecimal(qty), objStore);
					}

					csos = fnAddSOItemTypeTypeAddOn(csos, item, objStore);

					cart.removeItemAt(cnt1);
					item.qty = item.qty - qty;
					cart.insertItemAt(item, cnt1);

				}

			}

			for (int cnt2 = 0; cnt2 < cart.size(); cnt2++)
			{
				CartItem item2 = cart.cartItemAt(cnt2);
				if (item2.qty == 0)
					empty = true;
				else
				{
					empty = false;
					break;
				}
		}

		String addBk = "";
		tempStr = req.getParameter("addBk");
		if (tempStr != null)
			addBk = tempStr;

		if (addBk.equals("true"))
		{

			String strCustUserPkid = (String) session.getAttribute("userId");
			EMemberAddressBkObject obj = new EMemberAddressBkObject();
			obj.memberId = new Long(strCustUserPkid);

			tempStr = req.getParameter("receiverTitle");
			if (tempStr != null)
				obj.title = tempStr;
			tempStr = req.getParameter("receiverFirstName");
			if (tempStr != null)
				obj.namefirst = tempStr;
			tempStr = req.getParameter("receiverLastName");
			if (tempStr != null)
				obj.namelast = tempStr;

			obj.nickname = obj.namefirst + " " + obj.namelast;

			tempStr = req.getParameter("receiverCompanyName");
			if (tempStr != null)
				obj.company = tempStr;
			tempStr = req.getParameter("receiverAdd1");
			if (tempStr != null)
				obj.address1 = tempStr;
			tempStr = req.getParameter("Address2");
			if (tempStr != null)
				obj.address2 = tempStr;
			tempStr = req.getParameter("Address3");
			if (tempStr != null)
				obj.address3 = tempStr;
			tempStr = req.getParameter("City");
			if (tempStr != null)
				obj.city = tempStr.toUpperCase();
			tempStr = req.getParameter("PostalCode");
			if (tempStr != null)
				obj.postcode = tempStr;
			tempStr = req.getParameter("State");
			if (tempStr != null)
				obj.state = tempStr.toUpperCase();
			tempStr = req.getParameter("receiverCountry");
			if (tempStr != null)
				obj.country = tempStr.toUpperCase();
			tempStr = req.getParameter("receiverPhone1");
			if (tempStr != null)
				obj.telephone1 = tempStr;
			tempStr = req.getParameter("receiverHandphone");
			if (tempStr != null)
				obj.mobilephone = tempStr;

			EMemberAddressBkNut.fnCreate(obj);

		}


			if (empty)
				nextPage[0] = new String("/member_checkout_multiple.jsp");
			else
			{
				String strSOPkid = "0";
				tempStr = req.getParameter("SOPkid");
				if (tempStr != null)
					strSOPkid = tempStr;

				Vector vecSalesOrder = (Vector) session
						.getAttribute("vecSalesOrder");

				CreateSalesOrderSession csos2 = new CreateSalesOrderSession(
						objStore.ecom_userId);
				csos2 = fnCheckout(req, res, cart, csos2);
				vecSalesOrder.add(csos2);
				strSOPkid = String.valueOf(vecSalesOrder.size() - 1);

				req.setAttribute("SOPkid", strSOPkid);

				session.setAttribute("soCart", cart);
				req.setAttribute("vecSelectedGift", null);

				nextPage[0] = new String("/member_checkout_multiple_select.jsp");
			
			}
		}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return csos;
	}

	public Vector fnCreateSOMultiple(HttpServletRequest req,
			HttpServletResponse res, String nextPage[]) throws Exception
	{							
		HttpSession session = req.getSession(true);
		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
		Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");

		try{
			
		String tempStr = "";
		String senderEmail = "";
		tempStr = req.getParameter("senderEmail");
		if (tempStr != null)
			senderEmail = tempStr;

		String strCustUserPkid = (String) session.getAttribute("userId");
		if (strCustUserPkid == null)
		{
			fnCreatePersonalAccount(req, res, objStore);
			strCustUserPkid = (String) session.getAttribute("userId");
		}
		else
		{
			System.out.println("---------------------- Updating the cust user account !! ");
			
			for (int cnt1 = 0; cnt1 < 1; cnt1++)
			{
				CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder.get(cnt1);
				
				SalesOrderIndexObject soObj = csos.getSalesOrderIndex();
				Integer custUserPkid = new Integer(strCustUserPkid);
				CustUserObject custUser = CustUserNut.getObject(custUserPkid);		
				CustUser custUserEJB = CustUserNut.getHandle(custUser.pkid);
				
				custUser.mobilePhone = soObj.senderHandphone; 
				custUser.faxNo = soObj.senderFax;
				custUser.telephone1 = soObj.senderPhone1;
				custUser.telephone2 = soObj.senderPhone2;
				custUser.mainAddress1 = soObj.senderAdd1;
				custUser.mainAddress2 = soObj.senderAdd2;
				custUser.mainAddress3 = soObj.senderAdd3;
				custUser.mainPostcode = soObj.senderZip;
				custUser.mainState = soObj.senderState;
				custUser.mainCountry = soObj.senderCountry;
				
				custUserEJB.setObject(custUser);			
			}
		}

		Integer custUserPkid = new Integer(strCustUserPkid);
		CustUserObject custUserObj = CustUserNut.getObject(custUserPkid);

//		String etxnStatus = "";
//		tempStr = (String) req.getParameter("OthersNo");
//		
//		System.out.println("Checking othersNo");
//		
//		if (tempStr != null) 
//			etxnStatus = tempStr;
		
		String receiptMode = req.getParameter("receiptMode");
		session.setAttribute("receiptMode", receiptMode);

		EPaymentConfigObject objConfig = EPaymentConfigNut.getObject(new Long(receiptMode));

		String extraCode = "";
		String soPkid = "";

		for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
		{
			CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder.get(cnt1);
			SalesOrderIndexObject soObj = csos.getSalesOrderIndex();

			csos.setCustUser(custUserObj);

			TreeMap tableRows = csos.getTableRows();
			Vector vecDocRow = new Vector(tableRows.values());

			if (soObj.getDeliveryLocationPkid() != null)
			{
				csos = fnAddSOItemTypeDelivery(csos, soObj.getDeliveryLocationPkid(), objStore, soObj);
			}

			String thirdpartyLoyaltyCardCode = "";
			String thirdpartyLoyaltyCardNumber = "";

			boolean bRInvoice = false;
			boolean bRReceipt = false;

			String soType1 = SalesOrderIndexBean.SO_TYPE1_PERSONAL;
			String flagSender = "";
			String flagReceiver = "";
			String managerPassword = "";
			String remarks = "Not shipped";
			String occasion = req.getParameter("occasion");

			BigDecimal interfloraPrice = new BigDecimal(0);
			String interfloraFlowers1 = "";

			csos.setInfo(csos.getBranch().pkid, remarks, bRInvoice, bRReceipt,
					objStore.so_flagInternal, flagSender, flagReceiver,
					managerPassword, soType1, soObj.occasion,
					thirdpartyLoyaltyCardCode, thirdpartyLoyaltyCardNumber, interfloraPrice, interfloraFlowers1);

//			csos.setEtxnStatus(etxnStatus);

			Integer receiptModeId = new Integer(String.valueOf(objConfig.payment_config));
			CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(receiptModeId);
			if (cpcObj != null)
			{
				csos.setReceiptMode(cpcObj.paymentMode);
				csos.setPaymentRemarks(cpcObj.defaultPaymentRemarks);
				csos.setPaymentStatus(cpcObj.defaultPaymentStatus);
			}

			tempStr = req.getParameter("receiptApprovalCode" + receiptMode);
			if (tempStr != null) soObj.receiptApprovalCode = tempStr;

			tempStr = req.getParameter("etxn_account");
			if (tempStr != null) soObj.etxnAccount = tempStr;

			soObj.receiptBranch = objStore.so_receiptBranch;

			String senderName = "";
			tempStr = req.getParameter("senderFirstName");
			if (tempStr != null)
				senderName = tempStr;
			tempStr = req.getParameter("senderLastName");
			if (tempStr != null)
				senderName += " " + tempStr;
			String senderIdentityNumber = "";
			String senderHandphone = "";
			tempStr = req.getParameter("senderHandphone");
			if (tempStr != null)
				senderHandphone = tempStr;
			String senderFax = "";
			String senderPhone1 = "";
			tempStr = req.getParameter("senderPhone1");
			if (tempStr != null)
				senderPhone1 = tempStr;
			String senderPhone2 = "";
			String senderCompanyName = "";
			String senderAdd1 = "";
			tempStr = req.getParameter("senderAdd1");
			if (tempStr != null)
				senderAdd1 = tempStr;
			String senderAdd2 = "";
			tempStr = req.getParameter("senderAdd2");
			if (tempStr != null)
				senderAdd2 = tempStr;
			String senderAdd3 = "";
			tempStr = req.getParameter("senderAdd3");
			if (tempStr != null)
				senderAdd3 = tempStr;
			String senderCity = "";
			tempStr = req.getParameter("senderCity");
			if (tempStr != null)
				senderCity = tempStr;
			String senderZip = "";
			tempStr = req.getParameter("senderZip");
			if (tempStr != null)
				senderZip = tempStr;
			String senderState = "";
			tempStr = req.getParameter("senderState");
			if (tempStr != null)
				senderState = tempStr;
			String senderCountry = "";
			tempStr = req.getParameter("senderCountry");
			if (tempStr != null)
				senderCountry = tempStr;

			String receiverLocationType = SalesOrderIndexBean.INTERNET;
			
			csos.setDeliveryDetails(soObj.deliveryTo, soObj.deliveryToName,
					soObj.deliveryFrom, soObj.deliveryFromName,
					soObj.deliveryMsg1, soObj.expDeliveryTime,
					soObj.expDeliveryTimeStart.toString(),
					soObj.deliveryPreferences, senderName,
					senderIdentityNumber, senderEmail, senderHandphone, senderFax,
					senderPhone1, senderPhone2, "", senderCompanyName,
					senderAdd1, senderAdd2, senderAdd3, senderCity, senderZip,
					senderState, senderCountry, soObj.receiverTitle,
					soObj.receiverName, soObj.receiverIdentityNumber, "",
					soObj.receiverHandphone, soObj.receiverFax,
					soObj.receiverPhone1, soObj.receiverPhone2,
					soObj.receiverCompanyName, soObj.receiverAdd1,
					soObj.receiverAdd2, soObj.receiverAdd3, soObj.receiverCity,
					soObj.receiverZip, soObj.receiverState,
					soObj.receiverCountry, receiverLocationType);

			tempStr = req.getParameter("etxnType");
			if (tempStr != null) 
				csos.setEtxnType(tempStr);

			TreeMap tableRows2 = csos.getTableRows();
			Vector vecDocRow2 = new Vector(tableRows2.values());

			csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);
			csos.resetting = true;
			soObj.guid = soPkid;
			csos.createSalesOrderAndReceipt();
			
			if(cnt1 == 0)	
				soPkid = String.valueOf(soObj.pkid);

			fnSalesOrderAmountTotal(req, res, soObj.guid);

			csos.setDescription(fnNextPageAfterCheckout(req, res, objConfig, soPkid, soObj.guid, nextPage));
			
			//fnSendEmailSalesOrder(req, res, soObj, csos.getTableRows(), csos.getBillAmount(), csos.getDescription());
		}		

		req.setAttribute("senderEmail", senderEmail);
		req.setAttribute("receiptMode", receiptMode);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return vecSalesOrder;

	}

	public void fnLoginMultiple(HttpServletRequest req, HttpServletResponse res, String nextPage[])
			throws Exception
	{
		HttpSession session = req.getSession(true);
		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
		Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");

		String tempStr = "";
		String senderName = "";
		String senderHandphone = "";
		String senderPhone1 = "";
		String senderAdd1 = "";
		String senderAdd2 = "";
		String senderAdd3 = "";
		String senderCity = "";
		String senderZip = "";
		String senderState = "";
		String senderCountry = "";
		String senderEmail = "";
		String senderCompanyName = "";
		String senderFax = "";
		String senderPhone2 = "";
		String senderIdentityNumber = "";

		String receiptMode = req.getParameter("receiptMode");
		session.setAttribute("receiptMode", receiptMode);

		EPaymentConfigObject objConfig = EPaymentConfigNut.getObject(new Long(
				receiptMode));

		for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
		{
			CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder
					.get(cnt1);
			SalesOrderIndexObject soObj = csos.getSalesOrderIndex();

			String thirdpartyLoyaltyCardCode = "";
			String thirdpartyLoyaltyCardNumber = "";

			boolean bRInvoice = false;
			boolean bRReceipt = false;

			String soType1 = SalesOrderIndexBean.SO_TYPE1_PERSONAL;
			String flagSender = "";
			String flagReceiver = "";
			String managerPassword = "";
			String remarks = "Not shipped";
			String occasion = req.getParameter("occasion");

			BigDecimal interfloraPrice = new BigDecimal(0);
			String interfloraFlowers1 = "";
			
			csos.setInfo(csos.getBranch().pkid, remarks, bRInvoice, bRReceipt,
					objStore.so_flagInternal, flagSender, flagReceiver,
					managerPassword, soType1, soObj.occasion,
					thirdpartyLoyaltyCardCode, thirdpartyLoyaltyCardNumber, interfloraPrice, interfloraFlowers1);


			Integer receiptModeId = new Integer(String
					.valueOf(objConfig.payment_config));
			CardPaymentConfigObject cpcObj = CardPaymentConfigNut
					.getObject(receiptModeId);
			if (cpcObj != null)
			{
				csos.setReceiptMode(cpcObj.paymentMode);
				csos.setPaymentRemarks(cpcObj.defaultPaymentRemarks);
				csos.setPaymentStatus(cpcObj.defaultPaymentStatus);
			}

			tempStr = req.getParameter("receiptApprovalCode" + receiptMode);
			if (tempStr != null) soObj.receiptApprovalCode = tempStr;

			tempStr = req.getParameter("etxn_account");
			if (tempStr != null) soObj.etxnAccount = tempStr;

			soObj.receiptBranch = objStore.so_receiptBranch;

			try
			{
				fnLogin(req, res, nextPage);
				String strCustUserPkid = (String) session
						.getAttribute("userId");
				if (strCustUserPkid != null)
				{
					Integer custUserPkid = new Integer(strCustUserPkid);
					CustUserObject custUserObj = CustUserNut
							.getObject(custUserPkid);
					csos.setCustUser(custUserObj);

					senderName = custUserObj.nameFirst + " "
							+ custUserObj.nameLast;
					senderHandphone = custUserObj.telephone1;
					senderPhone1 = custUserObj.mobilePhone;
					senderAdd1 = custUserObj.mainAddress1;
					senderAdd2 = custUserObj.mainAddress2;
					senderAdd3 = custUserObj.mainAddress3;
					senderCity = custUserObj.mainCity.toUpperCase();
					senderZip = custUserObj.mainPostcode;
					senderState = custUserObj.mainState.toUpperCase();
					senderCountry = custUserObj.mainCountry.toUpperCase();
					senderEmail = custUserObj.email1;
				}

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

			String receiverLocationType = SalesOrderIndexBean.INTERNET;
			
			csos.setDeliveryDetails(soObj.deliveryTo, soObj.deliveryToName,
					soObj.deliveryFrom, soObj.deliveryFromName,
					soObj.deliveryMsg1, soObj.expDeliveryTime,
					soObj.expDeliveryTimeStart.toString(),
					soObj.deliveryPreferences, senderName,
					senderIdentityNumber, senderEmail, senderHandphone, senderFax,
					senderPhone1, senderPhone2, "", senderCompanyName,
					senderAdd1, senderAdd2, senderAdd3, senderCity, senderZip,
					senderState, senderCountry, soObj.receiverTitle,
					soObj.receiverName, soObj.receiverIdentityNumber, "",
					soObj.receiverHandphone, soObj.receiverFax,
					soObj.receiverPhone1, soObj.receiverPhone2,
					soObj.receiverCompanyName, soObj.receiverAdd1,
					soObj.receiverAdd2, soObj.receiverAdd3, soObj.receiverCity,
					soObj.receiverZip, soObj.receiverState,
					soObj.receiverCountry, receiverLocationType);

			csos.setOrderType2(SalesOrderIndexBean.SO_TYPE2_FLORIST);

			vecSalesOrder.removeElementAt(cnt1);
			vecSalesOrder.insertElementAt(csos, cnt1);

		}

		req.setAttribute("senderEmail", senderEmail);
		session.setAttribute("vecSalesOrder", vecSalesOrder);

	}

	public void emptyCart(HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession();
		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
		
		if (cart == null)
		{
			cart = new ShoppingCart();
			session.setAttribute("cart", cart);
		}

		cart.removeAllItems();
	}


	public CreateSalesOrderSession fnAddSOItemTypeTypeInventory(CreateSalesOrderSession csos, 
		CartItem item, BigDecimal qty, EStoreObject objStore)
			throws Exception
	{
		BranchObject branch = csos.getBranch();
		
		try
		{
			
		System.out.println("Inside DoMemberCheckout.fnAddSOItemTypeTypeInventory");
							
		ItemObject itmObj = ItemNut.getObject(new Integer(item.invid.toString()));
		
		DocRow docrow = new DocRow();
		docrow.setItemId(itmObj.pkid);
		docrow.setExternalId(item.optionId);
		docrow.setItemName(itmObj.name);
		docrow.setItemType(itmObj.itemType1);
		docrow.setTemplateId(itmObj.categoryId.intValue());
		
		String itemRemarks = item.productName + ", " + csos.getSalesOrderIndex().deliveryPreferences;
		docrow.setRemarks(itemRemarks);
		
		docrow.setPrice1(itmObj.priceList);
		docrow.setItemCode(itmObj.code);
		docrow.setQty(qty);
		docrow.user1 = objStore.ecom_userId.intValue();
		docrow.setCrvGain(itmObj.rebate1Price);
		docrow.setProductionRequired(itmObj.productionRequired);
		docrow.setDeliveryRequired(itmObj.deliveryRequired);

		if("PROMO-ENTITLED".equals(itmObj.reserved2))
		{
			BigDecimal discountAmount = new BigDecimal(0);
			discountAmount = itmObj.priceList;
			discountAmount = discountAmount.multiply(item.discount);
			discountAmount = discountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
			docrow.setDiscount(discountAmount);
		}
		
		docrow.setCcy1(branch.currency);
		docrow.setCcy2("");
		docrow.setPrice2(new BigDecimal(0));
		docrow.setSerialized(itmObj.serialized);
		docrow.setCommission1(itmObj.commissionPctSales1);
		docrow.setDescription(itmObj.description);
		csos.fnAddStockWithItemCode(docrow);
		
		System.out.println("Leaving DoMemberCheckout.fnAddSOItemTypeTypeInventory");

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return csos;
	}

	public CreateSalesOrderSession fnAddSOItemTypeTypePackage(CreateSalesOrderSession csos, 
		CartItem item, BigDecimal qty, EStoreObject objStore)
			throws Exception
	{
		BranchObject branch = csos.getBranch();

		BOMObject bomObj = BOMNut.getObject(new Integer(
				item.invid.toString()));
		for (int cnt1 = 0; cnt1 < bomObj.vecLink.size(); cnt1++)
		{
			BOMLinkObject blObj = (BOMLinkObject) bomObj.vecLink
					.get(cnt1);
			ItemObject itmObj = ItemNut
					.getObject(blObj.childItemId);
			try
			{
				DocRow docrow = new DocRow();
				docrow.setTemplateId(0);
				docrow.setItemType(ItemBean.ITEM_TYPE_PACKAGE);
				docrow.setItemId(itmObj.pkid);
				docrow.setExternalId(item.optionId);
				docrow.setItemCode(itmObj.code);
				docrow.setItemName(bomObj.parentItemCode + " - " + itmObj.name);
				docrow.setSerialized(itmObj.serialized);
				docrow.setQty(qty);
				docrow.setCcy1(branch.currency);
				docrow.setPrice1(blObj.priceList);
				docrow.setCommission1(itmObj.commissionPctSales1);
				docrow.user1 = objStore.ecom_userId.intValue();
				
				String itemRemarks = item.productName + " (PACKAGE), " + csos.getSalesOrderIndex().deliveryPreferences;
				docrow.setRemarks(itemRemarks);
				
				docrow.setDescription(itmObj.description);
				docrow.setCcy2("");
				docrow.setPrice2(new BigDecimal(0));
				docrow.setCrvGain(itmObj.rebate1Price);
				docrow.setProductionRequired(itmObj.productionRequired);
				docrow.setDeliveryRequired(itmObj.deliveryRequired);

				if("PROMO-ENTITLED".equals(itmObj.reserved2))
				{
					BigDecimal discountAmount = new BigDecimal(0);
					discountAmount = blObj.priceList;
					discountAmount = discountAmount.multiply(item.discount);
					discountAmount = discountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
					docrow.setDiscount(discountAmount);
				}
				
				csos.fnAddStockWithItemCode(docrow);
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
				throw ex;
			}
		}


		return csos;
	}

	public CreateSalesOrderSession fnAddSOItemTypeTypeAddOn(CreateSalesOrderSession csos, 
		CartItem item, EStoreObject objStore)
			throws Exception
	{
		System.out.println("Inside DoMemberCheckout.fnAddSOItemTypeTypeAddOn");
		
		BranchObject branch = csos.getBranch();

		List listAddOn = null;
		StringTokenizer tokenizer = null;
		tokenizer = new StringTokenizer(item.addOnId, ",");

		listAddOn = new ArrayList();
		while (tokenizer.hasMoreTokens())
		{
			listAddOn.add(tokenizer.nextToken());
		}

		for (Iterator j = listAddOn.iterator(); j.hasNext();)
		{
			String strPkid = (String) j.next();
			EProAddOnObject objAddOn = EProAddOnNut
					.getObject(new Long(strPkid));
			ItemObject itmObj = ItemNut.getObject(new Integer(
					objAddOn.invid.toString()));

			if(EProAddOnNut.isDiscount(objAddOn.pkid))	{
				BigDecimal discPrice = new BigDecimal(0);
				discPrice = itmObj.priceList;
				discPrice = discPrice.subtract(objAddOn.disc_value);
				itmObj.priceList = discPrice;
			}

			DocRow docrow = new DocRow();
			docrow.setItemId(itmObj.pkid);
			docrow.setExternalId(new Long(strPkid));
			docrow.setItemName(itmObj.name);
			docrow.setItemType(itmObj.itemType1);
			docrow.setTemplateId(itmObj.categoryId.intValue());
			
			String itemRemarks = item.productName + " (ADD-ON), " + csos.getSalesOrderIndex().deliveryPreferences;
			docrow.setRemarks(itemRemarks);
			
			docrow.setPrice1(itmObj.priceList);
			docrow.setItemCode(itmObj.code);
			docrow.setQty(new BigDecimal(1));
			docrow.user1 = objStore.ecom_userId.intValue();
			docrow.setCrvGain(itmObj.rebate1Price);
			docrow.setProductionRequired(itmObj.productionRequired);
			docrow.setDeliveryRequired(itmObj.deliveryRequired);

			if("PROMO-ENTITLED".equals(itmObj.reserved2))
			{
				BigDecimal discountAmount = new BigDecimal(0);
				discountAmount = itmObj.priceList;
				discountAmount = discountAmount.multiply(item.discount);
				discountAmount = discountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
				docrow.setDiscount(discountAmount);
			}
						
			docrow.setCcy1(branch.currency);
			docrow.setCcy2("");
			docrow.setPrice2(new BigDecimal(0));
			docrow.setSerialized(itmObj.serialized);
			docrow.setCommission1(itmObj.commissionPctSales1);
			docrow.setDescription(itmObj.description);

			csos.fnAddStockWithItemCode(docrow);
			
			System.out.println("Leaving DoMemberCheckout.fnAddSOItemTypeTypeAddOn");
		}

		return csos;
	}

	public CreateSalesOrderSession fnAddSOItemTypeDelivery(CreateSalesOrderSession csos, 
		Long DeliveryLocationPkid, EStoreObject objStore, SalesOrderIndexObject soObj)
			throws Exception
	{
		BranchObject branch = csos.getBranch();

		try
		{
			System.out.println("Inside DoMemberCheckout.fnAddSOItemTypeDelivery");
			
			DeliveryLocationObject dlObj = DeliveryLocationNut
					.getObject(DeliveryLocationPkid);

			if (dlObj != null)
			{
				ItemObject itmObj = ItemNut.getValueObjectByCode("DELIVERY");
				DocRow docrow = new DocRow();
				docrow.setTemplateId(itmObj.categoryId.intValue());
				docrow.setItemType(itmObj.itemType1);
				docrow.setItemId(itmObj.pkid);
				docrow.setItemCode(itmObj.code);
				docrow.setItemName(itmObj.name);
				docrow.setSerialized(itmObj.serialized);
				docrow.setQty(new BigDecimal(1));
				docrow.setCcy1(branch.currency);
				docrow.setPrice1(dlObj.deliveryRate1);
				docrow.setCommission1(itmObj.commissionPctSales1);
				docrow.user1 = objStore.ecom_userId.intValue();
				docrow.setRemarks(soObj.receiverCity + ", " + soObj.receiverZip
						+ ", " + soObj.receiverState + ", "
						+ soObj.receiverCountry);
				docrow.setDescription(itmObj.description);
				docrow.setCcy2("");
				docrow.setPrice2(new BigDecimal(0));
				Timestamp tsNow = TimeFormat.getTimestamp();
				Timestamp crvEndNextDay = TimeFormat.add(itmObj.rebate1End, 0,
						0, 1);
				if (tsNow.after(itmObj.rebate1Start)
						&& tsNow.before(itmObj.rebate1End))
				{
					docrow.setCrvGain(itmObj.rebate1Price);
				}
				docrow.setProductionRequired(itmObj.productionRequired);
				docrow.setDeliveryRequired(itmObj.deliveryRequired);
				csos.fnAddStockWithItemCode(docrow);
				
				System.out.println("Leaving DoMemberCheckout.fnAddSOItemTypeDelivery");
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}

		return csos;
	}

	public void fnCreatePersonalAccount(HttpServletRequest req,
			HttpServletResponse res, EStoreObject objStore) throws Exception
	{
		HttpSession session = req.getSession();

		CustUserObject custUserObj = new CustUserObject();
		custUserObj.email1 = req.getParameter("senderEmail");
		custUserObj.username = req.getParameter("senderEmail");

		String tempStr = "";
		tempStr = req.getParameter("Password");
		if (tempStr != null)
		{
			EncryptionEngine ee = new EncryptionEngine("ecommerce");
			ByteArrayOutputStream baos = ee.encrypt(tempStr);
			custUserObj.password = baos.toByteArray();
		}

		tempStr = req.getParameter("senderFirstName");
		if (tempStr != null)
			custUserObj.nameFirst = tempStr;
		tempStr = req.getParameter("senderLastName");
		if (tempStr != null)
			custUserObj.nameLast = tempStr;
		tempStr = req.getParameter("DOB");
		if (tempStr != null)
			custUserObj.dob = TimeFormat.createTimestamp(tempStr);
		tempStr = req.getParameter("senderAdd1");
		if (tempStr != null)
			custUserObj.mainAddress1 = tempStr;
		tempStr = req.getParameter("senderAdd2");
		if (tempStr != null)
			custUserObj.mainAddress2 = tempStr;
		tempStr = req.getParameter("senderAdd3");
		if (tempStr != null)
			custUserObj.mainAddress3 = tempStr;
		tempStr = req.getParameter("senderCity");
		if (tempStr != null)
			custUserObj.mainCity = tempStr;
		tempStr = req.getParameter("senderZip");
		if (tempStr != null)
			custUserObj.mainPostcode = tempStr;
		tempStr = req.getParameter("senderState");
		if (tempStr != null)
			custUserObj.mainState = tempStr;
		tempStr = req.getParameter("senderCountry");
		if (tempStr != null)
			custUserObj.mainCountry = tempStr;
		custUserObj.telephone1 = req.getParameter("senderPhone1");
		custUserObj.mobilePhone = req.getParameter("senderHandphone");

		custUserObj.useridEdit = objStore.ecom_userId;

		CustAccountObject custAccObj = new CustAccountObject();
		custAccObj.name = custUserObj.nameFirst + " " + custUserObj.nameLast;
		custAccObj.nameFirst = custUserObj.nameFirst;
		custAccObj.nameLast = custUserObj.nameLast;
		custAccObj.title = custUserObj.title;
		custAccObj.designation = custUserObj.designation;
		custAccObj.custAccountCode = "PER";
		custAccObj.accType = CustAccountBean.ACCTYPE_PERSONAL;
		custAccObj.mainAddress1 = custUserObj.mainAddress1;
		custAccObj.mainAddress2 = custUserObj.mainAddress2;
		custAccObj.mainAddress3 = custUserObj.mainAddress3;
		custAccObj.mainCity = custUserObj.mainCity.toUpperCase();
		custAccObj.mainPostcode = custUserObj.mainPostcode;
		custAccObj.mainState = custUserObj.mainState.toUpperCase();
		custAccObj.mainCountry = custUserObj.mainCountry.toUpperCase();
		custAccObj.telephone1 = custUserObj.telephone1;
		custAccObj.telephone2 = custUserObj.telephone2;
		custAccObj.homePhone = custUserObj.homePhone;
		custAccObj.mobilePhone = custUserObj.mobilePhone;
		custAccObj.faxNo = custUserObj.faxNo;
		custAccObj.email1 = custUserObj.email1;
		custAccObj.creditLimit = new BigDecimal(1000);
		custAccObj.creditTerms = new Integer(30);
		custAccObj.state = CustAccountBean.STATE_OK;
		custAccObj.dealerCode = "";
		CustAccount custAccEJB = CustAccountNut.fnCreate(custAccObj);
		custUserObj.accId = custAccObj.pkid;
		CustUser custUserEJB = CustUserNut.fnCreate(custUserObj);

		session.setAttribute("userName", custUserObj.username);
		String fullname = "";
		if (!custUserObj.title.equals(""))
			fullname += custUserObj.title;
		if (!custUserObj.nameFirst.equals(""))
			fullname += " " + custUserObj.nameFirst;
		if (!custUserObj.nameLast.equals(""))
			fullname += " " + custUserObj.nameLast;
		session.setAttribute("userFullName", fullname);
		session.setAttribute("userId", custUserObj.pkid.toString());

		String storeName = "";
		if (objStore != null && !objStore.store.equals(""))
			storeName = objStore.store;
		String emailFrom = "";
		if (objStore != null && !objStore.email1.equals(""))
			emailFrom = storeName + " Customer Service <" + objStore.email1 + ">";

		String smtpAddress = AppConfigManager.getProperty("EMAIL-SMTP-ADDRESS");
		String username = AppConfigManager.getProperty("EMAIL-SMTP-USERNAME");
		String password = AppConfigManager.getProperty("EMAIL-SMTP-PASSWORD");
		
		String emailTo = "";
		emailTo = fullname + " <" + custUserObj.email1 + ">";
		String body = "";
		body += "Dear " + fullname;
		body += "<br>";
		body += "<br>";
		body += "Thank you for registering with " + storeName + " on : "
				+ TimeFormat.strDisplayTimeStamp() + ".";
		body += "<br>";
		body += "Your User ID and Password are:<br>";
		body += "<br>";
		body += "---------------------------<br>";
		body += "User ID : <b>" + custUserObj.email1 + "</b><br>";
		body += "Password : <b>" + req.getParameter("Password")
				+ "</b><br>";
		body += "---------------------------<br>";
		body += "<br>";
		body += "<br>";
		body += "You can log onto your account at" + objStore.homepage
				+ ".";
		body += "<br>";
		body += "<br>";
		body += "*For security reasons, please remember to change your password regularly.";
		body += "<br>";
		body += "<br>";
		body += "Have a nice day!<br>";
		body += "<br>";
		body += "Customer Service<br>";
		body += objStore.store;
		body += "<br>";
		body += objStore.homepage;
		body += "<br>";
		//SendMail.sendEmail(emailFrom, emailTo, storeName+ " Members Registration", body);
		SendMail.sendEmail(emailFrom, emailTo, storeName+ " Members Registration", body, username, password, smtpAddress);

	}

	public String fnNextPageAfterCheckout(HttpServletRequest req, HttpServletResponse res, 
		EPaymentConfigObject objConfig, String SOPkid, String SOGuid, String nextPage[])
			throws Exception
	{
		System.out.println("soObj.guid : "+SOGuid);
		
		HttpSession session = req.getSession(true);

		String strTemp = "";
		String paymentMode = "";
		
		try
		{
					
		if (objConfig.payment_mode.equals("creditcard"))
		{
			paymentMode = "Credit Card - " + objConfig.payment_name;

			Integer intServerPort = new Integer(req.getServerPort());

			if(req.getServerPort()==8080){
		
				Log.printVerbose("Server Port = "+intServerPort.toString());
				nextPage[0] = new String("https://"+req.getServerName()+req.getContextPath()+"/member_checkout_payment_creditcard.jsp?SOGuid=" + SOGuid);
			}
			else 
			{
				Log.printVerbose("Server Port = "+intServerPort.toString());	
				nextPage[0] = new String("https://"+req.getServerName()+":443"+req.getContextPath()+"/member_checkout_payment_creditcard.jsp?SOGuid=" + SOGuid);
			}
		
		}
		else if (objConfig.payment_mode.equals("3DSSL"))
		{
			//paymentMode = "Real Time - " + objConfig.payment_name;
			paymentMode = "Real Time - Credit Card";
			
			String AmtTotal = "0";
			strTemp = (String) session.getAttribute("AmtTotal");
			if(strTemp!=null) AmtTotal = strTemp; //12.0000

			BigDecimal buf = new BigDecimal(AmtTotal);
	        buf = buf.multiply(new BigDecimal(100)); //12.0000 ---> 1200.00
	        
		    // The 0 symbol shows a digit or 0 if no digit present
		    NumberFormat formatter = new DecimalFormat("000000000000");
		    String ccyAmount = formatter.format(buf);  // 000000001200

		    System.out.println("Purchase Amt: "+ccyAmount);

		    String MerID = AppConfigManager.getProperty("ECOM-3DSSL-MERID"); 
			String AcqID = AppConfigManager.getProperty("ECOM-3DSSL-ACCID"); 	
			String Password = AppConfigManager.getProperty("ECOM-3DSSL-PASS"); //Have it in DoEPayment.java also
		    String PurchaseCurrency = "458";		    
			String Hash = Password + MerID + AcqID + SOGuid + ccyAmount + PurchaseCurrency;
			//String Hash = "orange45877687459999SENTRYORD01154321000000001200840";			
			String signature = encryption(Hash);
			
			System.out.println("MerID : "+MerID);
			System.out.println("AcqID : "+AcqID);
			System.out.println("Password : "+Password);
		
			nextPage[0] = new String("/member_checkout_payment_post.jsp?formName=3DSSL&AmtTotal="+ccyAmount+"&SOGuid="+SOGuid+"&Signature="+signature);	
			//nextPage = "/member_checkout_payment_post.jsp?formName=3DSSL&AmtTotal="+AmtTotal+"&SOGuid="+SOGuid;
		}
		else if (objConfig.payment_mode.equals("inetbank"))
		{
			paymentMode = "Online Banking - " + objConfig.payment_name;
			nextPage[0] = new String("/member_checkout_payment_inetbank.jsp?SOGuid="+SOGuid);
		
		}
		else if (objConfig.payment_mode.equals("fpx"))
        {
			String AmtTotal = "0";
            strTemp = (String) session.getAttribute("AmtTotal");
            if(strTemp!=null) AmtTotal = strTemp;

            paymentMode = "Online Banking - " + objConfig.payment_name;
            
            //nextPage = "/member_checkout_payment_inetbank.jsp?SOGuid="+SOGuid;
            nextPage[0] = new String("/member_checkout_payment_fpxPassValue.jsp?SOGuid="+SOGuid+"&AmtTotal="+AmtTotal);                	     
        } 
		else if (objConfig.payment_mode.equals("mobile"))
		{
			String mobileNumber = "";
			String icNumber = "";

			mobileNumber = (String) req.getParameter("receiptApprovalCode10021");
			icNumber = (String) req.getParameter("receiptICNumber10021");
		
			Log.printVerbose("mobileNumber " + mobileNumber);
			Log.printVerbose("icNumber " + icNumber);
	
			paymentMode = "Mobile Payment - " + objConfig.payment_name;
			nextPage[0] = new String("/member_checkout_payment_mobile.jsp?SOGuid="+SOGuid+"&mobileNumber="+mobileNumber+"&icNumber="+icNumber);
		
		} 
		else
		{
			String tempStr = "";
			String othersNo = "";
			tempStr = (String) req.getParameter("OthersNo");
			if (tempStr != null) 
				othersNo = tempStr;
					
			if(othersNo.equals(""))
				paymentMode = "Will Pay Later - " + objConfig.payment_name;
			else
				paymentMode = "Will Pay Later - " + objConfig.payment_name + " (" + othersNo + ")";
			
			nextPage[0] = new String("/DoMemberCheckout?formName=receipt&SOPkid="+SOPkid+"&SOGuid="+SOGuid);
		}


		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return paymentMode;
	}

	public String encryption(String Hash)
	{	
		String signature64 = "";
		
		try
		{
			System.out.println("========== MD5 ==========");
			
			byte[] buffer = Hash.getBytes();

	        MessageDigest algorithm = MessageDigest.getInstance("MD5");
	        		
	        algorithm.reset();
	        algorithm.update(buffer);
	        
	        Base64 base64_signature = new Base64();
	        signature64 = new String(base64_signature.encode(algorithm.digest()));
	        
	        System.out.println(" 64 base="+signature64);
        
		}catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return signature64;
	}
	
	public void fnSalesOrderAmountTotal(HttpServletRequest req,
			HttpServletResponse res, String SOGuid) throws Exception
	{
		try{
		
			System.out.println("Inside DoMemberCheckout.fnSalesOrderAmountTotal");
			
			HttpSession session = req.getSession(true);
			
			QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + SOGuid + "' " });
			Vector vecSalesOrder = new Vector(SalesOrderIndexNut.getObjects(query));
			BigDecimal AmtTotal = new BigDecimal(0);
			
			for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
			{				
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);
				EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
				EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
				csos.loadSalesOrder(soObj.pkid);
				session.setAttribute("dist-sales-order-session", csos);

				AmtTotal = AmtTotal.add(csos.getBillAmount());

				System.out.println("soObj.pkid : "+soObj.pkid.toString());
				System.out.println("sub-amtTotal : "+AmtTotal.toString());
			}

			session.setAttribute("AmtTotal", String.valueOf(AmtTotal));


			Log.printVerbose("AmtTotal : " + AmtTotal.toString());

			Log.printVerbose("AmtTotal from session : " + session.getAttribute("AmtTotal").toString());

			session.setAttribute("vecSalesOrder", null);
			session.setAttribute("vecSalesOrder", vecSalesOrder);
			
			System.out.println("Leaving DoMemberCheckout.fnSalesOrderAmountTotal");
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void fnGetPromoList(HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		try{
					
			QueryObject query = new QueryObject(new String[] { EPromotionBean.DATE_END + " >= '"
					+ TimeFormat.strDisplayDate(TimeFormat.getTimestamp()) + "'  " + " AND " + EPromotionBean.DATE_START + " <= '"
					+ TimeFormat.strDisplayDate(TimeFormat.getTimestamp()) + "' AND " + EPromotionBean.VISIBLE + " = true" });
			query.setOrder(" ORDER BY " + EPromotionBean.SORT);
			Vector vecPromo = new Vector(EPromotionNut.getObjects(query));
			for (int cnt0 = 0; cnt0 < vecPromo.size(); cnt0++)
			{
				EPromotionObject promoObj = (EPromotionObject) vecPromo.get(cnt0);
				if(promoObj.promo_option.equals("ticket_unique"))	
				{
					String queryStr = " AND date_valid_to >= '"
					+ TimeFormat.strDisplayDate(TimeFormat.getTimestamp()) + "' AND date_valid_from <= '"
					+ TimeFormat.strDisplayDate(TimeFormat.getTimestamp()) + "' AND state != 'used'";
					Vector vecProdTicket = new Vector(EPromotionNut.listProdTicket(promoObj.pkid, queryStr));
					if(vecProdTicket == null)
					{
						vecPromo.removeElementAt(cnt0);
					}
				}
			}
			req.setAttribute("vecPromo", vecPromo);

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void fnSendEmailSalesOrder(HttpServletRequest req,
			HttpServletResponse res, SalesOrderIndexObject soObj, TreeMap tableRows, BigDecimal getBillAmount, String getDescription) throws Exception
	{

		HttpSession session = req.getSession(true);

		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");

		String storeName = "";
		if (objStore != null && !objStore.store.equals(""))
			storeName = objStore.store;
		String emailFrom = "";
		if (objStore != null && !objStore.email1.equals(""))
			emailFrom = storeName + " Customer Service <" + objStore.email1
					+ ">";

		String smtpAddress = AppConfigManager.getProperty("EMAIL-SMTP-ADDRESS");
		String username = AppConfigManager.getProperty("EMAIL-SMTP-USERNAME");
		String password = AppConfigManager.getProperty("EMAIL-SMTP-PASSWORD");
		
		String emailTo = "";
		emailTo = soObj.senderName + " <" + soObj.senderEmail + ">";
		String body = "";
		body += "Dear " + soObj.senderName;
		body += "<br>";
		body += "<br>";
//		body += "Thank you for your order with " + storeName + " on : "+ TimeFormat.strDisplayTimeStamp() + ".";
		body += "Thank you for shopping with us.";
//		body += "<br>";
		body += "Your order will be processed once we have received your payment";
		body += "<br>";
		body += "<br>";
		body += "If you wish to contact us, please send an email to general@blooming.com.my. Please quote your order number in your email.";
			
		body += "<br><br>";

		body += "<table border=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"500\">";

		Vector vecSalesOrder  = (Vector) session.getAttribute("vecSalesOrder");

                for (int cnt1 = 0; cnt1 < vecSalesOrder.size(); cnt1++)
                {
                        SalesOrderIndexObject soObj3 = (SalesOrderIndexObject) vecSalesOrder.get(cnt1);

                        if(soObj3.pkid.equals(soObj.pkid))
                        {
                                  body += "<font size=\"2\">Order Number: <b>" + soObj3.pkid + "</b></font>";
                        }
                        else
                        {
                                  body += "<font size=\"2\">&nbsp;&nbsp; <b>" + soObj3.pkid + "</b></font>";
                        }
                }
				

		body += "<br><font size=\"2\" color=\"#990000\">You will be billed today in the amount of: </font><font size=\"2\"><b>"	+ CurrencyFormat.strCcy3ToSymbol("MYR") + " " + CurrencyFormat.strCcy(getBillAmount) + "</font></b></p></td></tr>";

		body += "<tr><td><hr></td></tr><tr><td></td></tr>";
		body += "<tr><td valign=\"top\"><b>";
		body += "========= ORDER INFORMATION =========</b><br>";
		body += "Order Number : " +soObj.pkid + "<br>";
		body += "Order Date/Time : " +TimeFormat.strDisplayDate1(soObj.timeCreate) + " " + TimeFormat.strDisplayTime1(soObj.timeCreate) + "<br>";
		body += "Delivery Date/Time : " + TimeFormat.strDisplayDate1(soObj.expDeliveryTimeStart) + " " + soObj.expDeliveryTime +  "<br><br>";

		BigDecimal shipTotal = new BigDecimal(0);
		BigDecimal subTotal = new BigDecimal(0);
		Vector vecDocRow = new Vector(tableRows.values());
		for(int cnt2 = 0; cnt2 < vecDocRow.size(); cnt2++)
		{
		  DocRow docrow = (DocRow) vecDocRow.get(cnt2);
		  if(!docrow.getItemCode().equals("DELIVERY"))
		  {
			  subTotal = subTotal.add(docrow.getPrice1());
			
			body += "<B>Item (" + (cnt2 + 1) + ")</B> : ";
			//body += docrow.getRemarks();
			body += "(" + docrow.getItemCode() + ")" + " " + docrow.getItemName(); 
			body += "<br>Qty: " +CurrencyFormat.strInt(docrow.getQty());
			body += "<br>Price: " + CurrencyFormat.strCcy3ToSymbol("MYR")+ " "+CurrencyFormat.strCcy(docrow.getPrice1());
			body += "<br>";
			body += "<br>";
		   }
		   else
		   {
				shipTotal = shipTotal.add(docrow.getPrice1());
		   }	
		}	

		subTotal = subTotal.subtract(soObj.promoDiscountAmount);	
		body += "Discount: " +CurrencyFormat.strCcy3ToSymbol("MYR")+ " " +CurrencyFormat.strCcy(soObj.promoDiscountAmount);
		body += "<br>Sub total: " +CurrencyFormat.strCcy3ToSymbol("MYR")+ " " +CurrencyFormat.strCcy(subTotal)+ "<br>";
		body += "<br>";
		body += "Shipping Charge: " +CurrencyFormat.strCcy3ToSymbol("MYR")+ " " +CurrencyFormat.strCcy(shipTotal)+ "<br>";
		body += "Sales Tax (0%): " +CurrencyFormat.strCcy3ToSymbol("MYR")+ " 0.00<br>";
		body += "<br>";
		body += "Order Total: " +CurrencyFormat.strCcy3ToSymbol("MYR")+ " " +CurrencyFormat.strCcy(getBillAmount)+ "<br>";
		body += "<b>Total Amount : " +CurrencyFormat.strCcy3ToSymbol("MYR")+ " " +CurrencyFormat.strCcy(getBillAmount)+ "</b><br><br>";
//		body += "Payment Method : " +getDescription;
//		body += "<br>";
//		body += "<br>";
		body += "<b>========= RECIPIENT INFORMATION =========</b><br>";
		body += "Name : " +soObj.receiverTitle+ " " +soObj.receiverName+ "<br>";
		body += "Company Name: " +soObj.receiverCompanyName+ " <br>";
		body += "Address: " + "<br>" + soObj.receiverAdd1+ "<br>";
		body += "" +soObj.receiverAdd2+ "<br>";
		body += "" +soObj.receiverAdd3+ "<br>";
		body += "City: " +soObj.receiverCity+ " <br>";
		body += "State: " +soObj.receiverState+ " <br>";
		body += "Postcode: " +soObj.receiverZip+ " <br>";
		body += "Country: " +soObj.receiverCountry+ " <br>";
		body += "Phone Number: " +soObj.receiverPhone1+ " <br>";
		body += "Mobile Number: " +soObj.receiverHandphone+ " <br>";
		body += "Email: " +soObj.senderEmail+ " <br>";

		body += "<br>";
		body += "Card Message:<br>";
		body += "   <table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#808080\" width=\"380\">";
		body += "	<tr>";
		body += "	  <td align=\"center\">" +soObj.deliveryTo+ " "+soObj.deliveryToName+ ", <br>";
		body += "	  <br>";
		body += "	  " +soObj.deliveryMsg1+ "<br>";
		body += "	  <br>";
		body += "	  " +soObj.deliveryFrom+ ", " +soObj.deliveryFromName+ "</td>";
		body += "	</tr>";
		body += "   </table>";
		body += "<p><br>";			

		body += "<b>========= BILLING INFORMATION =========</b><br>";
		body += "Name : " +soObj.senderName+ "<br>";
		body += "Address: " + "<br>"  + soObj.senderAdd1+ "<br>";
		body += "" +soObj.senderAdd2+ "<br>";
		body += "" +soObj.senderAdd3+ "<br>";
		body += "City: " +soObj.senderCity+ " <br>";
		body += "State: " +soObj.senderState+ " <br>";
		body += "Postcode: " +soObj.senderZip+ " <br>";
		body += "Country: " +soObj.senderCountry+ " <br>";
		body += "Phone Number: " +soObj.senderPhone1+ " <br>";
		body += "Mobile Number: " +soObj.senderHandphone+ " <br>";
		body += "Email: " +soObj.senderEmail+ " <br>	";
		body += "<br>";
		body += "<br>";
//		body += "If you have any questions,	comments, or feedback,please do not hesitate to contact us at ";
//		body += "<a href=\"mailto:general@blooming.com.my\">support@blooming.om.my</a>.<br><br>Assuring you our committed service and best regards.<br>";
		body += "Assuring you our committed service and best regards.";
		body += "<br>";
		body += "<b>Terms &amp; Conditions<br>";
		body += "<br>";
		body += "</b><u><font size=\"1\">Refund Policy</font></u><font size=\"1\"><br>";
		body += "All our products(except for orders overseas) are backed up by an unconditonal money-back guarantee or free replacement. We should be notified of any problem immediately upon receipt of the flowers. We reserve the right to refuse a request for refund if such request is not received by us on a timely basis. All prices shown and all transactions are in Ringgit Malaysia (RM).<br>";
		body += "<br>";
		body += "As our products are mostly perishable, please revert as soon as possible if there is a problem. All refund request will be dealt on a case by case basis.<br>&nbsp;</font>";

		body += "<br>";
		body += "Have a nice day!<br>";
		body += "<br>";
		body += "Customer Service<br>";
		body += objStore.store;
		body += "<br>";
		body += "blooming.com.my";
		body += "<br>";
		//SendMail.sendEmail(emailFrom, emailTo, storeName+ " Order Confirmation", body);
		SendMail.sendEmail(emailFrom, emailTo, storeName+ " Order Confirmation", body, username, password, smtpAddress);
			
	}


	public void fnPromoMultiple(HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		HttpSession session = req.getSession(true);
		EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
		Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");

		try{
					
		String tempStr = "";
		String senderName = "";
		tempStr = req.getParameter("senderFirstName");
		if (tempStr != null)
			senderName = tempStr;
		tempStr = req.getParameter("senderLastName");
		if (tempStr != null)
			senderName += " " + tempStr;
		String senderIdentityNumber = "";
		String senderHandphone = "";
		tempStr = req.getParameter("senderHandphone");
		if (tempStr != null)
			senderHandphone = tempStr;
		String senderFax = "";
		String senderPhone1 = "";
		tempStr = req.getParameter("senderPhone1");
		if (tempStr != null)
			senderPhone1 = tempStr;
		String senderPhone2 = "";
		String senderCompanyName = "";
		String senderAdd1 = "";
		tempStr = req.getParameter("senderAdd1");
		if (tempStr != null)
			senderAdd1 = tempStr;
		String senderAdd2 = "";
		tempStr = req.getParameter("senderAdd2");
		if (tempStr != null)
			senderAdd2 = tempStr;
		String senderAdd3 = "";
		tempStr = req.getParameter("senderAdd3");
		if (tempStr != null)
			senderAdd3 = tempStr;
		String senderCity = "";
		tempStr = req.getParameter("senderCity");
		if (tempStr != null)
			senderCity = tempStr;
		String senderZip = "";
		tempStr = req.getParameter("senderZip");
		if (tempStr != null)
			senderZip = tempStr;
		String senderState = "";
		tempStr = req.getParameter("senderState");
		if (tempStr != null)
			senderState = tempStr;
		String senderCountry = "";
		tempStr = req.getParameter("senderCountry");
		if (tempStr != null)
			senderCountry = tempStr;
		String senderEmail = "";
		tempStr = req.getParameter("senderEmail");
		if (tempStr != null)
			senderEmail = tempStr;

		System.out.println("senderEmail : "+senderEmail);
		req.setAttribute("senderEmail", senderEmail);
		
		String promoPkid = "";
		tempStr = req.getParameter("promoPkid");
		if (tempStr != null)
			promoPkid = tempStr;

		req.setAttribute("promoPkid", promoPkid);

		String promoNumber = "";
		tempStr = req.getParameter("promoNumber");
		if (tempStr != null)
			promoNumber = tempStr;


		String formName = "";
		formName = req.getParameter("formName");
		for (int cnt2 = 0; cnt2 < vecSalesOrder.size(); cnt2++)
		{
			CreateSalesOrderSession csos = (CreateSalesOrderSession) vecSalesOrder.get(cnt2);
			SalesOrderIndexObject soObj = csos.getSalesOrderIndex();
			
			String entitledFlag = "false";
			String errorFlag = "";

			BigDecimal discount = new BigDecimal(0);
			EPromotionObject objPromo = EPromotionNut.getObject(new Long(promoPkid));
			if (objPromo != null)
			{				
				csos.setPromo("", "", "", "", new BigDecimal(0), new BigDecimal(0));

				if(objPromo.promo_option.equals("payment_mode"))	 
					discount = objPromo.discount_pct;

				if (formName.equals("validatePromoCodeMultiple"))
				{
					if(objPromo.promo_option.equals("ticket_public"))	{

						if(objPromo.ticket_code.equals(promoNumber))
							discount = objPromo.discount_pct;

					} else if(objPromo.promo_option.equals("ticket_unique"))	{
						String query = " AND number = '" + promoNumber + "'";
						Vector vecProdTicket = new Vector(EPromotionNut.listProdTicket(new Long(promoPkid), query));
						if(vecProdTicket != null)
						{
							EPromoProdTicketObject stObj = (EPromoProdTicketObject) vecProdTicket.get(0);
							discount = stObj.discount_pct;	
						}
					}
				}

				if(discount.intValue() != 0)
				{
					BigDecimal totalDiscount = new BigDecimal(0);
					TreeMap tableRows = csos.getTableRows();
					Vector vecDocRow = new Vector(tableRows.values());
					
					for(int row1 = 0; row1 < vecDocRow.size(); row1++)
					{
						System.out.println("Inside DoMemberCheckout.fnPromoMultiple discount > rm0");
						
					    DocRow docrow = (DocRow) vecDocRow.get(row1);
						if(docrow.getItemId() != ItemBean.PKID_DELIVERY.intValue())
						{
							Long itemPkid = new Long(docrow.getItemId());							
							System.out.println("itemPkid : "+itemPkid.toString());
							
							Item itemEJB = ItemNut.getObjectByPkid(itemPkid.toString());							
							ItemObject itemObjOption = itemEJB.getObject();
											
							BigDecimal promoDiscountAmount = new BigDecimal(0);
							
							if("PROMO-ENTITLED".equals(itemObjOption.reserved2))
							{
								System.out.println("Item is PROMO-ENTITLED");
								
								promoDiscountAmount = docrow.getPrice1();
								promoDiscountAmount = promoDiscountAmount.multiply(discount);
								promoDiscountAmount = promoDiscountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
								docrow.setDiscount(promoDiscountAmount);
								
								totalDiscount = totalDiscount.add(promoDiscountAmount);
								
								entitledFlag = "true";
							}
							else
							{
								System.out.println("Item is Not PROMO-ENTITLED");
								
								errorFlag += itemObjOption.code;
							}
						}
					}

					String promoType = ""; 
					String promoCode = "";
					String promoName = "";
					BigDecimal promoDiscountPct = new BigDecimal(0);
					
					if("true".equals(entitledFlag))
					{
						System.out.println("entitledFlag is true");
						
						promoType = objPromo.promo_option; 
						promoCode = objPromo.code; 
						promoName = objPromo.name; 
						promoDiscountPct = discount;
					}
					else
					{
						System.out.println("entitledFlag is false");
						
						req.setAttribute("errMsgPromo", "Sorry, "+errorFlag+" is not entitled to promotions");
					}
					
					csos.setPromo(promoType, promoCode, promoNumber, 
						promoName, totalDiscount, promoDiscountPct);
					
				}
				else
				{
					if (formName.equals("validatePromoCodeMultiple"))
					{	
						req.setAttribute(
								"errMsgPromo",
								"Sorry, you have provided an incorrect Promotion Code. ");
					}
				}
			}

			session.setAttribute("receiptMode", null);

			String receiverLocationType = SalesOrderIndexBean.INTERNET;
			
			csos.setDeliveryDetails(soObj.deliveryTo, soObj.deliveryToName,
					soObj.deliveryFrom, soObj.deliveryFromName,
					soObj.deliveryMsg1, soObj.expDeliveryTime,
					soObj.expDeliveryTimeStart.toString(),
					soObj.deliveryPreferences, senderName,
					senderIdentityNumber, senderEmail, senderHandphone, senderFax,
					senderPhone1, senderPhone2, "", senderCompanyName,
					senderAdd1, senderAdd2, senderAdd3, senderCity, senderZip,
					senderState, senderCountry, soObj.receiverTitle,
					soObj.receiverName, soObj.receiverIdentityNumber, "",
					soObj.receiverHandphone, soObj.receiverFax,
					soObj.receiverPhone1, soObj.receiverPhone2,
					soObj.receiverCompanyName, soObj.receiverAdd1,
					soObj.receiverAdd2, soObj.receiverAdd3, soObj.receiverCity,
					soObj.receiverZip, soObj.receiverState,
					soObj.receiverCountry, receiverLocationType);

			vecSalesOrder.removeElementAt(cnt2);
			vecSalesOrder.insertElementAt(csos, cnt2);

		}

		fnGetPromoList(req,res);
		session.setAttribute("vecSalesOrder", vecSalesOrder);

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public void fnReSelectPayment(HttpServletRequest req, HttpServletResponse res, EditSalesOrderSession csos, String nextPage[])
			throws Exception
	{
		try{
					
		HttpSession session = req.getSession(true);
		SalesOrderIndexObject soObj  = csos.getSalesOrderIndex();
		if(soObj.promoDiscountPct.intValue() != 0)
		{
			TreeMap tableRows = csos.getTableRows();
			Vector vecDocRow = new Vector(tableRows.values());
			for(int row1 = 0; row1 < vecDocRow.size(); row1++)
			{
				DocRow docrow = (DocRow) vecDocRow.get(row1);							
				if(docrow.getItemId() != ItemBean.PKID_DELIVERY.intValue())
				{
					BigDecimal promoDiscountAmount = new BigDecimal(0);
					promoDiscountAmount = docrow.getPrice1();
					promoDiscountAmount = promoDiscountAmount.multiply(soObj.promoDiscountPct);
					promoDiscountAmount = promoDiscountAmount.divide(new BigDecimal(100), 6, BigDecimal.ROUND_DOWN);
					docrow.setDiscount(promoDiscountAmount);
					csos.updateSalesOrder();
				}
			}
		}
	   
		String tempStr = "";
		String etxnStatus = "";
		tempStr = (String) req.getParameter("OthersNo");
		
		System.out.println("Checking othersNo");
		
		if (tempStr != null) 
			etxnStatus = tempStr;
				
		csos.setEtxnStatus(etxnStatus);

		String receiptMode = req.getParameter("receiptMode");

		EPaymentConfigObject objConfig = EPaymentConfigNut.getObject(new Long(receiptMode));
		Integer receiptModeId = new Integer(String.valueOf(objConfig.payment_config));
		CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(receiptModeId);
		if (cpcObj != null)
		{
			csos.setReceiptMode(cpcObj.paymentMode);
			csos.setPaymentRemarks(cpcObj.defaultPaymentRemarks);
			csos.setPaymentStatus(cpcObj.defaultPaymentStatus);
		}

		tempStr = req.getParameter("receiptApprovalCode" + receiptMode);
		if (tempStr != null) soObj.receiptApprovalCode = tempStr;

		tempStr = req.getParameter("etxn_account");
		if (tempStr != null) soObj.etxnAccount = tempStr;

		fnSalesOrderAmountTotal(req, res, soObj.guid);

		csos.setDescription(fnNextPageAfterCheckout(req, res, objConfig, String.valueOf(soObj.pkid), soObj.guid, nextPage));

		//fnSendEmailSalesOrder(req, res, soObj, csos.getTableRows(), csos.getBillAmount(), csos.getDescription());

		req.setAttribute("receiptMode", receiptMode);	

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
