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

import java.io.BufferedOutputStream;
import java.math.*;
import java.util.Vector;

import javax.servlet.http.*;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.vlee.bean.distribution.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.user.*;
import com.vlee.servlet.main.*;
import com.vlee.util.TimeFormat;

public class DoMsgOrderCheck extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		String selectMessages = (String) req.getParameter("SelectMessages");
		String selectOrders = (String) req.getParameter("selectOrders");
		String messagesCheck = (String) req.getParameter("MessagesCheck");
		String ordersCheck = (String) req.getParameter("OrdersCheck");
		String saveMsg1 = (String) req.getParameter("saveMsg1");
		String saveMsg2 = (String) req.getParameter("saveMsg2");
		//String printSelectedMessages = (String) req.getParameter("printSelectedMessages");
				
		System.out.println("formName : "+formName);
		System.out.println("selectMessages : "+selectMessages);
		System.out.println("selectOrders : "+selectOrders);
		System.out.println("messagesCheck : "+messagesCheck);
		System.out.println("ordersCheck : "+ordersCheck);
		System.out.println("saveMsg1 : "+saveMsg1);
		System.out.println("saveMsg2 : "+saveMsg2);
		
		req.setAttribute("focusElement", req.getParameter("focusElement"));
		req.setAttribute("anchorName", req.getParameter("anchorName"));
		
		if (formName == null)
		{
			HttpSession session = req.getSession();
	        Integer userId = (Integer) session.getAttribute("userId");
	        MsgOrderCheckForm mocf = new MsgOrderCheckForm(userId);
	        session.setAttribute("msg-order-check-form", mocf);
			return new ActionRouter("dist-msg-order-check-page");
		}
		else if (formName.equals("getList"))
		{
			try
			{
				fnGetListing(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else if (formName.equals("msgOdrCheck") && selectMessages!=null)
		{
			System.out.println("Inside selectMessages");
						
			try
			{				
				fnGetSelectedMessages(servlet, req, res);
						
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}
		else if (formName.equals("msgOdrCheck") && selectOrders!=null)
		{
			System.out.println("Inside printSelectedOrders");
			
			String[] odrCheckbox = req.getParameterValues("odrCheckbox");
			if(odrCheckbox!=null)
			{
				fnGetSelectedOrders(servlet,req,res);				
			}						
		}
		else if (formName.equals("printSelectedMessages"))
		{
			System.out.println("Inside printSelectedMessages");
			
			String[] printSelectedMsg = req.getParameterValues("printSelectedMsg");
			if(printSelectedMsg!=null)
			{
				try
				{
					fnExportToRTF(req, res);
					
				} catch (Exception ex)
				{
					req.setAttribute("errMsg", ex.getMessage());
				}				
				
				return null;
			}			
		}
		else if (formName.equals("printSelectedOrders"))
		{
			System.out.println("Inside printSelectedOrders");
			
			String[] printSelectedOdr = req.getParameterValues("printSelectedOdr");
			if(printSelectedOdr!=null)
			{
				try
				{
					fnPrintSelectedOrders(req, res);
					return new ActionRouter("dist-sales-order-print-as-forms-multiple-page");
					
				} catch (Exception ex)
				{
					req.setAttribute("errMsg", ex.getMessage());
				}				
				
				return null;
			}			
		}
		else if (formName.equals("msgOdrCheck") && messagesCheck!=null)
		{
			System.out.println("Inside messagesCheck");
			
			String[] msgCheckbox = req.getParameterValues("msgCheckbox");
			if(msgCheckbox!=null)
			{
				try
				{
					fnMessagesCheck(req, res);
					
				} catch (Exception ex)
				{
					req.setAttribute("errMsg", ex.getMessage());
				}				
			}			
		}
		else if (formName.equals("msgOdrCheck") && ordersCheck!=null)
		{
			System.out.println("Inside ordersCheck");
			
			String[] odrCheckbox = req.getParameterValues("odrCheckbox");
			if(odrCheckbox!=null)
			{
				try
				{
					fnOrdersCheck(req, res);					
					
				} catch (Exception ex)
				{
					req.setAttribute("errMsg", ex.getMessage());
				}				
			}			
		}
		else if (formName.equals("msgOdrCheck") && (saveMsg1!=null || saveMsg2!=null))
		{
			System.out.println("Inside saveMsg");
			
			HttpSession session = req.getSession();
			MsgOrderCheckForm mocf = (MsgOrderCheckForm) session.getAttribute("msg-order-check-form");
			
			try
			{
				fnSaveMessage(req, res);	
				
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
						
			System.out.println("Leaving saveMsg");						
		}
		
		return new ActionRouter("dist-msg-order-check-page");
	}
	
	private void fnGetListing(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{		
		String branch = req.getParameter("branch");
		String dateType = req.getParameter("dateType");
		String dateFrom = req.getParameter("dateFrom");
		String dateTo = req.getParameter("dateTo");
		String logic = (String) req.getParameter("logic");
		
		HttpSession session = req.getSession();
		MsgOrderCheckForm mocf = (MsgOrderCheckForm) session.getAttribute("msg-order-check-form");
		
		if (mocf == null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			mocf = new MsgOrderCheckForm(userId);
			session.setAttribute("msg-order-check-form", mocf);
		}
						
		mocf.setBranchId(branch);
		mocf.setDateType(dateType);
		mocf.setDateRange(dateFrom, dateTo);
		mocf.setLogic(logic);
				
		mocf.searchResult();
	}
	
	private void fnGetSelectedMessages(HttpServlet servlet,HttpServletRequest req,HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		MsgOrderCheckForm mocf = (MsgOrderCheckForm) session.getAttribute("msg-order-check-form");
		
		Vector vecMsg = new Vector();
		String[] msgCheckbox = req.getParameterValues("msgCheckbox");
		
		if(msgCheckbox==null){ return; }
		
		for(int cnt1=0;cnt1<msgCheckbox.length;cnt1++)
		{
			try
			{
				Long lPkid = new Long(msgCheckbox[cnt1]);
				SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);
				soEJB.addPrintCounterMessage();
				
				vecMsg.add(soObj.pkid);
				req.setAttribute("vecMsg", vecMsg);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}	
		
		mocf.searchResult();
	}
	
	private void fnGetSelectedOrders(HttpServlet servlet,HttpServletRequest req,HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		MsgOrderCheckForm mocf = (MsgOrderCheckForm) session.getAttribute("msg-order-check-form");
		
		Vector vecOrder = new Vector();
		String[] odrCheckbox = req.getParameterValues("odrCheckbox");
		if(odrCheckbox==null){ return;}
		
		for(int cnt1=0;cnt1<odrCheckbox.length;cnt1++)
		{
			try
			{
				Long lPkid = new Long(odrCheckbox[cnt1]);
				SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);
				vecOrder.add(soObj.pkid);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}	
		req.setAttribute("vecOrder", vecOrder);
		
		mocf.searchResult();
	}
	
	private void fnPrintSelectedOrders(HttpServletRequest req,HttpServletResponse res) throws Exception
	{
		Vector vecOrder = new Vector();
		String[] printSelectedOdr = req.getParameterValues("printSelectedOdr");
		
		String docTrail = "";
		HttpSession session = (HttpSession) req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		
		for(int cnt1=0;cnt1<printSelectedOdr.length;cnt1++)
		{
			try
			{
				Long lPkid = new Long(printSelectedOdr[cnt1]);
				SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);
				vecOrder.add(soObj);
				
				docTrail = "";
				docTrail = DocumentProcessingItemNut.appendDocTrail(
						"PRINT-OF-DO-WO", "", TimeFormat.strDisplayTime(TimeFormat.getTimestamp()), docTrail);
				fnRecordInDocTrail(userId, soObj.pkid, docTrail);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}	
		req.setAttribute("vecOrder", vecOrder);						
	}
	
	private static synchronized void fnRecordInDocTrail(Integer userId, Long soPkid, String docTrail)
	{		
		if(docTrail.length()>4)
		{
			System.out.println("Inside fnRecordInDocTrail : "+docTrail);
			
			DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
			dpiObj.processType = "ORDER-UPDATE";
			dpiObj.category = "UPDATE-DETAILS";
			dpiObj.auditLevel = new Integer(0);
			dpiObj.userid = userId;
			dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
			dpiObj.docId = soPkid;
			dpiObj.description1 = docTrail;
			dpiObj.time = TimeFormat.getTimestamp();
			dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
			dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
			DocumentProcessingItemNut.fnCreate(dpiObj);
		}
	}
	
	private void fnExportToRTF(HttpServletRequest req, HttpServletResponse res) throws Exception
	{	
			String[] printSelectedMsg = req.getParameterValues("printSelectedMsg");						
		
			if(printSelectedMsg!=null)
			{
				System.out.println("Inside fnExportToRTF : vecMsg not null");
											
				res.setContentType("text/rtf");
				res.setHeader("Content-disposition", "filename=messageCard"+TimeFormat.strDisplayTimeStamp()+".rtf");
				
				BufferedOutputStream outputStream = new BufferedOutputStream(res.getOutputStream());
				Document document = new Document();
				RtfWriter2.getInstance(document, outputStream);
				document.open();
				
				for(int cnt1=0;cnt1<printSelectedMsg.length;cnt1++)
				{
					Long lPkid = new Long(printSelectedMsg[cnt1]);
					SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);			
					
					try
					{									        		         						
				        if(soObj!=null)
				        {	
				        		System.out.println("soObj != null");		        			        	
								
				        		{
									HttpSession session = req.getSession();
									Integer userId = (Integer) session.getAttribute("userId");		
									
									/// record in the audit trail!
									DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
									dpiObj.processType = "UPDATE-ORDER";
									dpiObj.category = "PRINT-MSG";
									dpiObj.auditLevel = new Integer(1);
									dpiObj.userid = userId;
									dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
									dpiObj.docId = soObj.pkid;
									dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("PRINT MSG","","", "");
									dpiObj.time = TimeFormat.getTimestamp();
									dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
									dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
									DocumentProcessingItemNut.fnCreate(dpiObj);
								}	
				        		
								Paragraph headerMsg = new Paragraph(soObj.deliveryTo+" "+soObj.deliveryToName, new Font(Font.HELVETICA,12));
								Paragraph bodyMsg = new Paragraph(soObj.deliveryMsg1, new Font(Font.HELVETICA,12));
								Paragraph footerMsg = new Paragraph(soObj.deliveryFrom+" "+soObj.deliveryFromName, new Font(Font.HELVETICA,12));
								Paragraph orderInfo = new Paragraph("ORDER"+" "+soObj.pkid.toString(), new Font(Font.HELVETICA,8));
							
								headerMsg.setAlignment(Element.ALIGN_CENTER);
								bodyMsg.setAlignment(Element.ALIGN_CENTER);
								footerMsg.setAlignment(Element.ALIGN_CENTER);
								orderInfo.setAlignment(Element.ALIGN_CENTER);
				
								document.add(headerMsg);
								document.add(bodyMsg);
								document.add(footerMsg);
								document.add(new Paragraph(""));
								document.add(orderInfo);	
								document.newPage();
				        }
					} 
					catch (Exception ex){ex.printStackTrace();}
				}
				
				document.close();
				outputStream.flush();		
			}
			else
				System.out.println("Inside fnExportToRTF : vecMsg is null");
	}
	
	private void fnMessagesCheck(HttpServletRequest req,HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		MsgOrderCheckForm mocf = (MsgOrderCheckForm) session.getAttribute("msg-order-check-form");

		String[] msgCheckbox = req.getParameterValues("msgCheckbox");
		
		for(int cnt1=0;cnt1<msgCheckbox.length;cnt1++)
		{
			try
			{							
				Long lPkid = new Long(msgCheckbox[cnt1]);
				SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);
			
				String strTmp = "";
				if("f".equals(soObj.messageCardCheck))				
					strTmp = "NOT_CHECKED";
				else
					strTmp = "CHECKED";
							
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);
				soEJB.checkMessage();	
				
				{
					Integer userId = (Integer) session.getAttribute("userId");		
					
					/// record in the audit trail!
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "CHECK ";
					dpiObj.category = "CHECK-MSG";
					dpiObj.auditLevel = new Integer(1);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soObj.pkid;
					dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("CHECK",strTmp,"CHECKED", "");
					dpiObj.time = TimeFormat.getTimestamp();
					dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
					DocumentProcessingItemNut.fnCreate(dpiObj);
				}					
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}	
		
		mocf.searchResult();
		
		System.out.println("Leaving fnMessagesCheck");
	}
	
	private void fnOrdersCheck(HttpServletRequest req,HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		MsgOrderCheckForm mocf = (MsgOrderCheckForm) session.getAttribute("msg-order-check-form");

		String[] odrCheckbox = req.getParameterValues("odrCheckbox");
		
		for(int cnt1=0;cnt1<odrCheckbox.length;cnt1++)
		{
			try
			{
				Long lPkid = new Long(odrCheckbox[cnt1]);
				SalesOrderIndexObject soObj = SalesOrderIndexNut.getObjectTree(lPkid);
				
				String strTmp = "";
				if("f".equals(soObj.checkOrderDetails))				
					strTmp = "NOT_CHECKED";
				else
					strTmp = "CHECKED";
				
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);
				soEJB.checkOrder();						
				
				{
					Integer userId = (Integer) session.getAttribute("userId");		
					
					/// record in the audit trail!
					DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
					dpiObj.processType = "CHECK ";
					dpiObj.category = "CHECK-ODR";
					dpiObj.auditLevel = new Integer(1);
					dpiObj.userid = userId;
					dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
					dpiObj.docId = soObj.pkid;
					dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("CHECK",strTmp,"CHECKED", "");
					dpiObj.time = TimeFormat.getTimestamp();
					dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
					dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
					DocumentProcessingItemNut.fnCreate(dpiObj);
				}	
			}
			catch(Exception ex)
			{ ex.printStackTrace();}
		}	
		
		mocf.searchResult();
		
		System.out.println("Leaving fnOrdersCheck");
	}
		
	private void fnSaveMessage(HttpServletRequest req,HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		MsgOrderCheckForm mocf = (MsgOrderCheckForm) session.getAttribute("msg-order-check-form");
		
		Vector vecResult = mocf.getResult();
		if(vecResult!=null)
		{
			for(int cnt1=0;cnt1<vecResult.size();cnt1++)
			{
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecResult.get(cnt1);
				
				String toSave = req.getParameter("msg"+soObj.pkid.toString());
				
				if("true".equals(toSave))
				{			
					String oldMsg = "";
					oldMsg = soObj.deliveryTo + " " + soObj.deliveryToName + " ";
					oldMsg += soObj.deliveryMsg1 + " ";
					oldMsg += soObj.deliveryFrom + " " + soObj.deliveryFromName;
						
					soObj.deliveryTo = req.getParameter("deliveryTo"+soObj.pkid.toString());
					soObj.deliveryToName = req.getParameter("deliveryToName"+soObj.pkid.toString());
					soObj.deliveryMsg1 = req.getParameter("deliveryMsg1"+soObj.pkid.toString());
					soObj.deliveryFrom = req.getParameter("deliveryFrom"+soObj.pkid.toString());
					soObj.deliveryFromName = req.getParameter("deliveryFromName"+soObj.pkid.toString());
				
					System.out.println("soObj.deliveryTo : "+soObj.deliveryTo);
					System.out.println("soObj.deliveryToName : "+soObj.deliveryToName);
					System.out.println("soObj.deliveryMsg1 : "+soObj.deliveryMsg1);
					System.out.println("soObj.deliveryFrom : "+soObj.deliveryFrom);
					System.out.println("soObj.deliveryFromName : "+soObj.deliveryFromName);
					
					SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);					
					soEJB.setObject(soObj);
					
					String newMsg = "";
					newMsg = soObj.deliveryTo + " " + soObj.deliveryToName + " ";
					newMsg += soObj.deliveryMsg1 + " ";
					newMsg += soObj.deliveryFrom + " " + soObj.deliveryFromName;
					
					{
						Integer userId = (Integer) session.getAttribute("userId");		
						
						/// record in the audit trail!
						DocumentProcessingItemObject dpiObj = new DocumentProcessingItemObject();
						dpiObj.processType = "UPDATE-ORDER";
						dpiObj.category = "EDIT-MSG";
						dpiObj.auditLevel = new Integer(1);
						dpiObj.userid = userId;
						dpiObj.docRef = SalesOrderIndexBean.TABLENAME;
						dpiObj.docId = soObj.pkid;
						dpiObj.description1 = DocumentProcessingItemNut.appendDocTrail("EDIT",oldMsg,newMsg, "");
						dpiObj.time = TimeFormat.getTimestamp();
						dpiObj.state = DocumentProcessingItemBean.STATE_CREATED;
						dpiObj.status = DocumentProcessingItemBean.STATUS_ACTIVE;
						DocumentProcessingItemNut.fnCreate(dpiObj);
					}
				}
				
				System.out.println("Order "+soObj.pkid.toString()+" : "+toSave);
			}
		}
		
		mocf.searchResult();
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
}
