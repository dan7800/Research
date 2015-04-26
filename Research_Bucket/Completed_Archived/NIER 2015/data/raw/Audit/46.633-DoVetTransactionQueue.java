package com.vlee.servlet.thickclient;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.SHA1DigiSignControl;
import com.vlee.util.TimeFormat;
import com.vlee.bean.accounting.ViewGLSession;
import com.vlee.bean.remotecreditservices.*;
import com.vlee.bean.user.PermissionManager;
import com.vlee.ejb.accounting.BranchNut;
import com.vlee.ejb.accounting.BranchObject;
import com.vlee.ejb.accounting.CardPaymentConfigNut;
import com.vlee.ejb.accounting.CardPaymentConfigObject;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.ejb.user.AuditTrailBean;
import com.vlee.ejb.user.AuditTrailNut;
import com.vlee.ejb.user.AuditTrailObject;
import com.vlee.ejb.user.User;
import com.vlee.ejb.user.UserConfigObject;
import com.vlee.ejb.user.UserConfigRegistryNut;
import com.vlee.ejb.user.UserNut;
import com.vlee.ejb.user.UserRoleNut;

import java.math.*;
import java.sql.Timestamp;


public class DoVetTransactionQueue implements Action
{
	
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = (String) req.getParameter("formName");
		
		if(formName.trim().equals("getItem"))
		{
			String pkid = (String) req.getParameter("pkid");
			
		}
		
		
		String creditServices = req.getParameter("creditServices");
		if(creditServices!=null)
		{
			if(creditServices.equals("Add"))
			{
				try
				{
					fnExperimentalAdd(servlet, req, res);
					
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}
				
			}
		}
		if(formName!=null){
			if(formName.equals("add"))
			{
				try
				{
					fnAdd(servlet, req, res);
					
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}
			}
			else if(formName.equals("ProcessTxn"))
			{
				try
				{
					fnProcessTxn(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					req.setAttribute("errMsg", ex.getMessage());
					return new ActionRouter("remote-credit-services-process-page");
				}				
			}
			else if(formName.equals("test"))
			{
				try
				{
					return new ActionRouter("remote-credit-services-respond-page");
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}				
			}			
			else if(formName.equals("getTxnList"))
			{
				try
				{
					fnGetList(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}				
			}
			else if(formName.equals("newForm"))
			{
				// DO NOTHING
				return new ActionRouter("remote-credit-services-process-page");
			}				
			else if(formName.equals("editRow"))
			{
				try
				{
					return fnEditRow(servlet, req, res);
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}	
			}
			else if(formName.equals("confirmEditRow"))
			{
				try
				{
					return fnConfirmEditRow(servlet, req, res);
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}	
			}	
			else if(formName.equals("commitEditRow"))
			{
				try
				{
					fnCommitEditRow(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}	
			}				
			else if(formName.equals("deleteRow"))
			{
				try
				{
					fnDeleteRow(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}
				catch(Exception ex)
				{
					Log.printAudit(ex.getLocalizedMessage());
					fnSendError(servlet, req, res);
					return new ActionRouter("remote-credit-services-process-page");
				}	
			}	
			else
			{
				return new ActionRouter("remote-credit-services-process-page");
			}
		}
		else
		{
			return new ActionRouter("remote-credit-services-process-page");
		}
		return new ActionRouter("remote-credit-services-process-page");
		
	}	
	
	private void fnSendError(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res)
	{
		Log.printVerbose("ERRORED OH NOES");
		
	}
	
	
	
	private void fnProcessTxn(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
		String[] strArrPkid = req.getParameterValues("pkid");
		if (strArrPkid != null)
		{
			for (int cnt1 = 0; cnt1 < strArrPkid.length; cnt1++)
			{
				Integer pkid = new Integer(strArrPkid[cnt1]);
				
				RemoteCreditServicesObject rcsObj = RemoteCreditServicesNut.getObject(pkid);
				prctForm.setMerchantCode(rcsObj.merchantCode);
				prctForm.setMerchantName(rcsObj.merchantName);
				prctForm.setPaymentId(rcsObj.paymentId);
				prctForm.setRefNo(rcsObj.refNo);
				prctForm.setAmount(rcsObj.amount);
				prctForm.setTxnFee(rcsObj.txnFee);
				prctForm.setBankCharge(rcsObj.bankCharge);
				prctForm.setCurrency(rcsObj.currency);
				prctForm.setProdDesc(rcsObj.prodDesc);
				prctForm.setUserName(rcsObj.userName);
				prctForm.setUserEmail(rcsObj.userEmail);
				prctForm.setUserContact(rcsObj.userContact);
				prctForm.setRemarks(rcsObj.remarks);
				prctForm.setLang(rcsObj.lang);
				prctForm.setSignature(rcsObj.signature);
				prctForm.setTransId(rcsObj.transId);
				BranchObject bchObj = BranchNut.getObject(rcsObj.branchId);
				prctForm.setBranchCode(bchObj.code);
				prctForm.setTxnDate(rcsObj.txnDate);
				CardPaymentConfigObject cpcObj = CardPaymentConfigNut.getObject(rcsObj.cardPaymentConfigId);
				prctForm.setCardPaymentConfigId(cpcObj.paymentMode);
				prctForm.preProcessingCheck();
				prctForm.processTransaction(pkid);
			}
		}
		
	}
	
	private ActionRouter fnEditRow(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		String pkid = req.getParameter("pkid");
		RemoteCreditServicesObject rcsObj = RemoteCreditServicesNut.getObject(new Integer(pkid));
		if(rcsObj!=null)
		{
			session.setAttribute("editRemoteCreditTransactionObject",rcsObj);
			return new ActionRouter("remote-credit-services-edit-page");
		}
		else
		{
			return new ActionRouter("remote-credit-services-process-page");
		}
	}
	
	private ActionRouter fnConfirmEditRow(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		String branch = req.getParameter("branch");
		String customer = req.getParameter("customer");
		String cardPaymentConfigId = req.getParameter("cardPaymentConfigId");
		String amount = req.getParameter("amount");
		String txnFee = req.getParameter("txnFee");
		String bankCharge = req.getParameter("bankCharge");
		String paymentId = req.getParameter("paymentId");
		String refNo = req.getParameter("refNo");
		String currency = req.getParameter("currency");
		String prodDesc = req.getParameter("prodDesc");
		String userName = req.getParameter("userName");
		String userEmail = req.getParameter("userEmail");
		String userContact = req.getParameter("userContact");
		String remarks = req.getParameter("remarks");
		String lang = req.getParameter("lang");
		String transId = req.getParameter("transId");
		
		RemoteCreditServicesObject rcsObj = (RemoteCreditServicesObject) session.getAttribute("editRemoteCreditTransactionObject");
		ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
		rcsObj.branchId = new Integer(branch);
		rcsObj.merchantCode = customer;
		rcsObj.merchantName = customer;
		rcsObj.cardPaymentConfigId = new Integer(cardPaymentConfigId);
		rcsObj.amount = new BigDecimal(amount);
		rcsObj.txnFee = new BigDecimal(txnFee);
		rcsObj.bankCharge = new BigDecimal(bankCharge);
		rcsObj.paymentId = new Integer(paymentId);
		rcsObj.refNo = refNo;
		rcsObj.currency = currency;
		rcsObj.prodDesc = prodDesc;
		rcsObj.userName = userName;
		rcsObj.userEmail = userEmail;
		rcsObj.userContact = userContact;
		rcsObj.remarks = remarks;
		rcsObj.lang = lang;
		rcsObj.transId = transId;
		
		String result = prctForm.checkRecord(rcsObj);
		if(result.equals(""))
		{
			rcsObj.status = RemoteCreditServicesBean.STATUS_READY;
			session.setAttribute("editRemoteCreditTransactionObject",rcsObj);
			req.setAttribute("editErrorRemoteCreditTransactionObject",result);
			req.setAttribute("confirmOnce","yes");
			return new ActionRouter("remote-credit-services-edit-page");
		}
		else
		{
			session.setAttribute("editRemoteCreditTransactionObject",rcsObj);
			req.setAttribute("editErrorRemoteCreditTransactionObject",result);
			req.setAttribute("confirmOnce","yes");
			return new ActionRouter("remote-credit-services-edit-page");
		}
		
	}

	private void fnCommitEditRow(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		RemoteCreditServicesObject rcsObj = (RemoteCreditServicesObject) session.getAttribute("editRemoteCreditTransactionObject");
		ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
		
		RemoteCreditServicesNut.update(rcsObj);
		prctForm.getTransactionList();
		session.setAttribute("editRemoteCreditTransactionObject",null);
	}
	
	private void fnDeleteRow(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
		String pkid = req.getParameter("pkid");
		prctForm.deleteRow(new Integer(pkid));
	}
	
	private void fnGetList(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
		if(prctForm==null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			prctForm= new ProcessRemoteCreditTransactionForm();
			session.setAttribute("process-remote-credit-services-session", prctForm);
		}	
		String dateFrom = req.getParameter("dateStart");
		String dateTo = req.getParameter("dateEnd");
		String status = req.getParameter("status");
		String sort = req.getParameter("sort");
		Timestamp tsFrom = TimeFormat.createTimestamp(dateFrom);
		Timestamp tsTo = TimeFormat.createTimestamp(dateTo);
		prctForm.setDateTo(tsTo);
		prctForm.setDateFrom(tsFrom);
		prctForm.setStatus(status);
		prctForm.setOrderBy(sort);
		prctForm.getTransactionList();
		
		
	}
	
	private ActionRouter fnAdd(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
		if(prctForm==null)
		{
			Integer userId = (Integer) session.getAttribute("userId");
			prctForm= new ProcessRemoteCreditTransactionForm();
			session.setAttribute("process-remote-credit-services-session", prctForm);
		}	
		String merchantCode = req.getParameter("merchantCode");
		String merchantName = req.getParameter("merchantName");
		String paymentId = req.getParameter("paymentId");
		String refNo = req.getParameter("refNo");
		String amount = req.getParameter("amount");
		String txnFee = req.getParameter("txnFee");
		String bankCharge = req.getParameter("bankCharge");
		String currency = req.getParameter("currency");
		String prodDesc = req.getParameter("prodDesc");
		String userName = req.getParameter("userName");
		String userEmail = req.getParameter("userEmail");
		String userContact = req.getParameter("userContact");
		String remarks = req.getParameter("remarks");
		String lang = req.getParameter("lang");
		String signature = req.getParameter("signature");
		String transId = req.getParameter("transId");
		String branchCode = req.getParameter("branchCode");
		String txnDate = req.getParameter("txnDate");
		String cardPaymentConfigId = req.getParameter("cardPaymentConfigId");
		String authCode = req.getParameter("authCode");
		prctForm.setMerchantCode(merchantCode);
		prctForm.setMerchantName(merchantName);
		prctForm.setPaymentId(new Integer(paymentId));
		prctForm.setRefNo(refNo);
		prctForm.setAmount(new BigDecimal(amount));
		prctForm.setTxnFee(new BigDecimal(txnFee));
		prctForm.setBankCharge(new BigDecimal(bankCharge));
		prctForm.setCurrency(currency);
		prctForm.setProdDesc(prodDesc);
		prctForm.setUserName(userName);
		prctForm.setUserEmail(userEmail);
		prctForm.setUserContact(userContact);
		prctForm.setRemarks(remarks);
		prctForm.setLang(lang);
		prctForm.setSignature(signature);
		prctForm.setTransId(transId);
		prctForm.setBranchCode(branchCode);
		prctForm.setTxnDate(TimeFormat.createTimestamp(txnDate));
		prctForm.setCardPaymentConfigId(cardPaymentConfigId);
		prctForm.setAuthCode(authCode);
		prctForm.createTransaction();
		req.setAttribute("responseStatus", prctForm.getResponseStatus());
		return new ActionRouter("remote-credit-services-respond-page");
	}
	
	private ActionRouter fnExperimentalAdd(HttpServlet servlet, HttpServletRequest req,
			HttpServletResponse res) throws Exception
	{
		String uname = req.getParameter("userName");
		String pwd = req.getParameter("password");
		HttpSession session = req.getSession(true);
		User user = null;
		user = (User) UserNut.getHandle(uname);
		// UserHome lUserHome = getUserHome();
		// User user = UserNut.getHandle(lUserHome, uname);
		boolean bLogin;
		if (user != null)
		{
			Log.printDebug("User: " + user);
			bLogin = user.getValidUser(uname, pwd);
		} else
		{
			bLogin = false;
			Log.printDebug(" user is null!! ");
		}
		if (bLogin)
		{ // user is in the login database
			Log.printDebug(" bLogin is true");

			session.setAttribute("loginUser", user);
			session.setAttribute("userName", uname);
			session.setAttribute("nameFirst", user.getNameFirst());
			session.setAttribute("nameLast", user.getNameLast());
			session.setAttribute("userId", user.getUserId());


			//// determine the role of this user
			//// then load permission manager
//			PermissionList permList = UserNut.getPermissionList(user.getUserId());
//			session.setAttribute("user-permission-list", permList);
			try
			{
				Integer roleId = UserRoleNut.getRoleId(user.getUserId());
				PermissionManager permManager = new PermissionManager();
				permManager.setRole(roleId);
				session.setAttribute("permission-manager", permManager);
			}
			catch(Exception ex)
			{ ex.printStackTrace();}

			// Set other session variables like first name,
			// last name and other info
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = user.getUserId();
			atObj.auditType = AuditTrailBean.TYPE_ACCESS;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "Login (from : " + req.getRemoteHost() + " )";
			AuditTrailNut.fnCreate(atObj);
			UserConfigObject ucObj = UserConfigRegistryNut.getUserConfigObject(user.getUserId());
			session.setAttribute("ucObj", ucObj);
		
			String merchantCode = req.getParameter("MERCHANTCODE");
			String merchantName = req.getParameter("MERCHANTNAME");
			String paymentId = req.getParameter("PAYMENTID");
			String refNo = req.getParameter("REFNO");
			String amount = req.getParameter("AMOUNT");
			String txnFee = req.getParameter("TXNFEE");
			String bankCharge = req.getParameter("BANKCHARGE");
			String currency = req.getParameter("CURRENCY");
			String prodDesc = req.getParameter("PRODDESC");
			String userName = req.getParameter("USERNAME");
			String userEmail = req.getParameter("USEREMAIL");
			String userContact = req.getParameter("USERCONTACT");
			String remarks = req.getParameter("REMARKS");
			String lang = req.getParameter("LANG");
			String signature = req.getParameter("SIGNATURE");
			String transId = req.getParameter("TRANSID");
			String branchCode = req.getParameter("BRANCHCODE");
			String txnDate = req.getParameter("TXNDATE");
			String authCode = req.getParameter("AUTHCODE");
			String cardPaymentConfigId = req.getParameter("CARDPAYMENTCONFIGID");
			
			session.setAttribute("IsCreditServices","yes");
			
			req.setAttribute("MERCHANTCODE",merchantCode);
			req.setAttribute("MERCHANTNAME",merchantName);
			req.setAttribute("PAYMENTID",paymentId);
			req.setAttribute("REFNO",refNo);
			req.setAttribute("AMOUNT",amount);
			req.setAttribute("TXNFEE",txnFee);
			req.setAttribute("BANKCHARGE",bankCharge);
			req.setAttribute("CURRENCY",currency);
			req.setAttribute("PRODDESC",prodDesc);
			req.setAttribute("USERNAME",userName);
			req.setAttribute("USEREMAIL",userEmail);
			req.setAttribute("USERCONTACT",userContact);
			req.setAttribute("REMARKS",remarks);
			req.setAttribute("LANG",lang);
			req.setAttribute("SIGNATURE",signature);
			req.setAttribute("TRANSID",transId);
			req.setAttribute("BRANCHCODE",branchCode);
			req.setAttribute("TXNDATE",txnDate);
			req.setAttribute("AUTHCODE", authCode);
			req.setAttribute("CARDPAYMENTCONFIGID",cardPaymentConfigId);
			
			ProcessRemoteCreditTransactionForm prctForm = (ProcessRemoteCreditTransactionForm) session.getAttribute("process-remote-credit-services-session");
			if(prctForm==null)
			{
				Integer userId = (Integer) session.getAttribute("userId");
				prctForm= new ProcessRemoteCreditTransactionForm();
				session.setAttribute("process-remote-credit-services-session", prctForm);
			}	
			
			prctForm.setMerchantCode(merchantCode);
			prctForm.setMerchantName(merchantName);
			prctForm.setPaymentId(new Integer(paymentId));
			prctForm.setRefNo(refNo);
			prctForm.setAmount(new BigDecimal(amount));
			prctForm.setTxnFee(new BigDecimal(txnFee));
			prctForm.setBankCharge(new BigDecimal(bankCharge));
			prctForm.setCurrency(currency);
			prctForm.setProdDesc(prodDesc);
			prctForm.setUserName(userName);
			prctForm.setUserEmail(userEmail);
			prctForm.setUserContact(userContact);
			prctForm.setRemarks(remarks);
			prctForm.setLang(lang);
			prctForm.setSignature(signature);
			prctForm.setTransId(transId);
			prctForm.setBranchCode(branchCode);
			prctForm.setTxnDate(TimeFormat.createTimestamp(txnDate));
			prctForm.setCardPaymentConfigId(cardPaymentConfigId);
			prctForm.setAuthCode(authCode);
			prctForm.createTransaction();
			req.setAttribute("responseStatus", prctForm.getResponseStatus());
			
		}
		return new ActionRouter("remote-credit-services-respond-page");
	}
	
}
