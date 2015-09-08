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

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import com.vlee.bean.application.AppConfigManager;
import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndex;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentConfigNut;
import com.vlee.ejb.ecommerce.EPaymentConfigObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPayment extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		System.out.println("TUPPENY: inside DoEPayment::perform()... formName=" + formName);
		
		{
			// Check whether the bank returns the sales order guid
			String guid = (String) req.getParameter("OrderNo");
			
			if("".equals(guid))
			{
				req.setAttribute("noGuidError", "noGuidError");
				
				return new ActionRouter("checkout-receipt-page");
			}
		}
		
		if (formName.equals("epaymentRHB"))
		{
			String guid = (String) req.getParameter("OrderNo");
			String status = (String) req.getParameter("ReturnCode");
			String paymentConfigPkid = "10023"; 
			guid = guid.replaceAll("PG","");				
			if (status.equals("0"))
			{
				status = "success";
				req.setAttribute("status", "success");

				rhb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);
				
				try{ fnSalesOrderAmountTotal(req,res,guid.replaceAll("PG",""));}
				catch(Exception ex){ ex.printStackTrace();}
			} else
			{
				status = "fail";
				req.setAttribute("status", "fail");

				rhb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid.replaceAll("PG",""));}
                                catch(Exception ex){ ex.printStackTrace();}
			}
			
		} 
		else if (formName.equals("epayment3DSSL"))
		{
                        String status = (String) req.getParameter("responseCode");
                        String paymentConfigPkid = "10012";
                        String guid = (String) req.getParameter("merchantOrderId");
                        
                        if (status.equals("1"))
                        {
                        		String Password = AppConfigManager.getProperty("ECOM-3DSSL-PASS"); //Have it in DoMemberCheckout.java also
                        		String MerID = (String) req.getParameter("merchantId");
                        		String AcqID = (String) req.getParameter("acquirerId");
								String signature3DSSL = (String) req.getParameter("signature");
								String responseCode = (String) req.getParameter("responseCode");
								String reasonCode = (String) req.getParameter("reasonCode");
								
								System.out.println("Password : "+Password);
				
								//Verify sender
								String Hash = Password + MerID + AcqID + guid + responseCode + reasonCode;
								String signature = encryption(Hash);
								
								System.out.println("signature3DSSL: "+signature3DSSL);

								signature3DSSL = signature3DSSL.replace(' ', '+');

								System.out.println("signature3DSSL after replacement: "+signature3DSSL);
								System.out.println("signature : "+signature);
							
                        	
								if(signature.equals(signature3DSSL))
								{
									status = "success";
								}else
								{
									status = "signature error";
								}								
								//End Verify sender

								req.setAttribute("status", status);
								
                                threeDSSL(req, status);
                                updatePayment(req, guid, paymentConfigPkid, status, formName);

                                Log.printVerbose("Checkpoint Z22");

                                try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

                                Log.printVerbose("Checkpoint Z11");

                        } else if (status.equals("2"))
                        {
                                Log.printVerbose("Checkpoint Y11");

								status = "fail";
								req.setAttribute("status", "fail");

                                threeDSSL(req, status);             
                                updatePayment(req, guid, paymentConfigPkid, status, formName);

                                Log.printVerbose("Checkpoint Z22");

                                try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

                                Log.printVerbose("Checkpoint Z11");
                        }else
						{
								//String guid = (String) req.getParameter("merchantOrderId");
							
								Log.printVerbose("Checkpoint Y11");
						
								status = "unknown";
								req.setAttribute("status", "unknown");
				
				                threeDSSL(req, status);
								updatePayment(req, guid, paymentConfigPkid, status, formName);

                                try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

                                Log.printVerbose("Checkpoint Y22");
						}		
		} else if (formName.equals("epaymentHLB"))
		{
			String guid = (String) req.getParameter("OrderNo");
			String status = (String) req.getParameter("respCode");
			String paymentConfigPkid = "10018"; 

			if (status.equals("000000"))
			{
				status = "success";
				req.setAttribute("status", "success");

				hlb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				Log.printVerbose("Checkpoint Z22");

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

				Log.printVerbose("Checkpoint Z11");
			} else
			{
				Log.printVerbose("Checkpoint Y11");

				status = "fail";
				req.setAttribute("status", "fail");

				hlb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                catch(Exception ex){ ex.printStackTrace();}

				Log.printVerbose("Checkpoint Y22");
			} // ambankfollows
		//[[JOB-JOE-070510
		} else if (formName.equals("epaymentAmBank"))
		{
			System.out.println("TUPPENY:DoEPaymentservlet, formName==epaymentAmBank");
			String guid = (String) req.getParameter("InvoiceNo");
			String status = (String) req.getParameter("success");
			
			String paymentConfigPkid = "10027"; // to change to 10027 

			if (status.equals("1"))
			{
				status = "success";
				req.setAttribute("status", "success");

				AmBank(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

			} else
			{
				Log.printVerbose("Checkpoint Y11");

				status = "fail";
				req.setAttribute("status", "fail");

				AmBank(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                catch(Exception ex){ ex.printStackTrace();}

				Log.printVerbose("Checkpoint Y22");
			}
		//[[JOB-JOE-070510
		} else if (formName.equals("epaymentFPX"))
        {
            String guid = (String) req.getParameter("sellerOrderNumber");
            String txnAmt = (String) req.getParameter("txnAmt");
			String debitAuthNo = (String) req.getParameter("debitAuthNo");
			String debitAuthCode = (String) req.getParameter("debitAuthCode");
			String creditAuthNo = (String) req.getParameter("creditAuthNo");
			String creditAuthCode = (String) req.getParameter("creditAuthCode");
            String paymentConfigPkid = "10020";
			String status = "";

			System.out.println("guid : "+guid);
			System.out.println("txnAmt : "+txnAmt);
			System.out.println("debitAuthNo : "+debitAuthNo);
			System.out.println("creditAuthNo :"+creditAuthNo);

			if(! (debitAuthCode.equals("") && creditAuthCode.equals("")))
			{
			if(debitAuthCode.equals("00") && creditAuthCode.equals("00"))
                	{
				System.out.println("Checkpoint 111");

				status = "success";
                        	req.setAttribute("status", "success");

				fpx(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                        	try{ fnSalesOrderAmountTotal(req,res,guid);}
                        	catch(Exception ex){ ex.printStackTrace();}
                	}
                	else
                	{
				System.out.println("Checkpoint 222");

				status = "failed";
                       	 	req.setAttribute("status", "fail");

				fpx(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                        	try{ fnSalesOrderAmountTotal(req,res,guid);}
                        	catch(Exception ex){ ex.printStackTrace();}
                	}
			}
		}

		return new ActionRouter("checkout-receipt-page");
	}

	public String encryption(String Hash)
	{	
		String signature64 = "";
		
		try
		{
			System.out.println("========== SHA1=========");
			
			byte[] buffer = Hash.getBytes();

	        MessageDigest algorithm = MessageDigest.getInstance("SHA1");
	        		
	        algorithm.reset();
	        algorithm.update(buffer);
	        
	        Base64 base64_signature = new Base64();
	        signature64 = new String(base64_signature.encode(algorithm.digest()));
	        
	        System.out.println(" 64 base="+signature64);

			  //signature64 = signature64.replace('+', ' ');

           //System.out.println(" 64 base afer replacement ="+signature64);
        
		}catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return signature64;
	}
	
	private void rhb(HttpServletRequest req, String status)
	{
		HttpSession session = req.getSession();
		try
		{
			String merchantID = (String) req.getParameter("MerchantID");
			String orderNo = (String) req.getParameter("OrderNo");
			String amount = (String) req.getParameter("Amount");
			String referenceNo = (String) req.getParameter("ReferenceNo");
			String returnMsg = (String) req.getParameter("ReturnMsg");
			String hashCount = (String) req.getParameter("HashCount");

			//orderNo = orderNo.substring(2);
			orderNo = orderNo.replaceAll("PG","");

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_RHBBANK;
			inboxObj.merchant_id = merchantID;
			inboxObj.merchant_tranx_id = new Integer(orderNo);
			inboxObj.tranx_amt = new BigDecimal(amount);
			inboxObj.tranx_status = status;

			if(returnMsg != null)
			{
				inboxObj.tranx_err_code = returnMsg;
				
				req.setAttribute("transError", returnMsg);
			}
	
			if(referenceNo != null)
			{
				inboxObj.bank_ref_no = referenceNo;
			
				req.setAttribute("transBankRefNo", referenceNo);
			}
	
			inboxObj.property2 = hashCount;

			inboxHome.create(inboxObj);

//                        EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
//                        EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
//                        csos.loadSalesOrder(new Long(orderNo));
//                        session.setAttribute("dist-sales-order-session", csos);



		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void hlb(HttpServletRequest req, String status)
	{
		try
		{
			String orderNo = (String) req.getParameter("OrderNo");
			String paymentAmount = (String) req.getParameter("paymentAmount");
			String respCode = (String) req.getParameter("respCode");
			String trxref = (String) req.getParameter("trxref");
			String paymentMode = (String) req.getParameter("paymentMode");
			String errCode = (String) req.getParameter("respCode");
		
			Log.printVerbose("orderNo " + orderNo);
			Log.printVerbose("paymentAmount " + paymentAmount);
			Log.printVerbose("trxref " + trxref);
			Log.printVerbose("paymentMode " + paymentMode);
			Log.printVerbose("respCode "+ respCode);

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_HLB;
			inboxObj.merchant_tranx_id = new Integer(orderNo);
			inboxObj.tranx_amt = new BigDecimal(paymentAmount);
			inboxObj.tranx_status = status;

			if(trxref != null)
			{
				inboxObj.bank_ref_no = trxref;
			
				req.setAttribute("transBankRefNo", trxref);
			}

			if(status.equals("fail"))
			{
				inboxObj.tranx_err_code = errCode;
				
				req.setAttribute("transError", errCode);
			}
				
			inboxObj.bank_ref_no = trxref;
			inboxObj.property1 = paymentMode;

			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//[[JOB-JOE-070510
	private void AmBank(HttpServletRequest req, String status)
	{
		try
		{
			String orderNo = (String) req.getParameter("InvoiceNo");
			String paymentAmount = (String) req.getParameter("TransAmount");
			String field1 = (String) req.getParameter("field1");
			//String respCode = (String) req.getParameter("respCode");
			//String trxref = (String) req.getParameter("trxref");
			String trxref = orderNo;
			
			System.out.println("TUPPENY: inside DoEPayment::AmBank()... orderNo=" + orderNo + ",paymentAmount=" + paymentAmount + ",trxref=" + trxref);
			
		
			Log.printVerbose("AmBank:orderNo " + orderNo);
			Log.printVerbose("AmBank:paymentAmount " + paymentAmount);
			Log.printVerbose("AmBank:trxref " + trxref);
			
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			
			//inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_HLB;
			//inboxObj.merchant_tranx_id = new Integer(orderNo);
			//inboxObj.tranx_amt = new BigDecimal(paymentAmount);
			//inboxObj.tranx_status = status;
			
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_AMBANK; // Modify the EJB and Change to PAYMENT_AMBANK 
			inboxObj.merchant_tranx_id = new Integer(orderNo);
			inboxObj.tranx_amt = new BigDecimal(paymentAmount);
			inboxObj.tranx_status = status;

			if(trxref != null)
			{
				inboxObj.bank_ref_no = trxref;
			
				req.setAttribute("transBankRefNo", trxref);
			}

			if(status.equals("fail"))
			{
				inboxObj.tranx_err_code = "AmBank:errCode";
				
				req.setAttribute("transError", "AmBank:errCode");
			}
				
			inboxObj.bank_ref_no = trxref;
			inboxObj.property1 = field1;

			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	//JOB-JOE-070510]]

	private void threeDSSL(HttpServletRequest req, String status)    
	{
		
		try
		{
			String responseCode = (String) req.getParameter("responseCode");
			String reasonCode = (String) req.getParameter("reasonCode");
			String reasonDescription = (String) req.getParameter("reasonDescription");
			String merchantId = (String) req.getParameter("merchantId");
			String acquirerId = (String) req.getParameter("acquirerId");
			String merchantOrderId = (String) req.getParameter("merchantOrderId");
			String signature = (String) req.getParameter("signature");
			String referenceNumber = (String) req.getParameter("referenceNumber");
			String cardNumber = (String) req.getParameter("cardNumber");
			String authorizationCode = (String) req.getParameter("authorizationCode");
			String billingAddress = (String) req.getParameter("billingAddress");
			String shippingAddress =(String) req.getParameter("shippingAddress");
		
			String cardType = "MASTER / VISA";
			req.setAttribute("cardType", cardType);
								
			Log.printVerbose("responseCode " + responseCode);
			Log.printVerbose("reasonCode " + reasonCode);
			Log.printVerbose("reasonDescription " + reasonDescription);
			Log.printVerbose("merchantId " + merchantId);
			Log.printVerbose("acquirerId " + acquirerId);
			Log.printVerbose("merchantOrderId " + merchantOrderId);
			Log.printVerbose("signature " + signature);
			Log.printVerbose("referenceNumber " + referenceNumber);
			Log.printVerbose("cardNumber " + cardNumber);
			Log.printVerbose("authorizationCode " + authorizationCode);
			Log.printVerbose("billingAddress " + billingAddress);
			Log.printVerbose("shippingAddress " + shippingAddress);
			Log.printVerbose("cardType " + cardType);
				
			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			if(status.equals("success"))
			{
				inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MASTER;
				inboxObj.merchant_id = merchantId;
                inboxObj.merchant_tranx_id = new Integer(merchantOrderId);
                inboxObj.tranx_status = status;

				if(authorizationCode != null)
				{
					inboxObj.tranx_appr_code = authorizationCode;
				
					req.setAttribute("authorizationCode", authorizationCode);
				}
				
				if(referenceNumber != null)
				{
					inboxObj.bank_ref_no = referenceNumber;
				
					req.setAttribute("transBankRefNo", referenceNumber);
				}

				inboxObj.property1 = acquirerId;
				inboxObj.property2 = signature;

				if(cardNumber != null)
				{
					inboxObj.property3 = cardNumber;
		
					req.setAttribute("cardNo", cardNumber);
				}
				
				inboxObj.property4 = billingAddress;
				inboxObj.property5 = shippingAddress;
			}else 
			{
				if(merchantId != null)
				{
					inboxObj.merchant_tranx_id = new Integer(merchantOrderId);
				}else
				{
					inboxObj.merchant_tranx_id = new Integer(0);
				}
				
				if(reasonDescription != null)
				{
					inboxObj.tranx_err_code = reasonDescription;
					req.setAttribute("transError", reasonDescription);
				}				
			}
			
					inboxObj.tranx_status = status;	
						
                    inboxHome.create(inboxObj);
                    
                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

		private void fpx(HttpServletRequest req, String status)
        {
                HttpSession session = req.getSession();
                try
                {
                        String fpxSellerId = "0000564";

                        EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
                        EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			System.out.println("Checkpoint 333");

                        inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_FPX;
                        inboxObj.merchant_id = fpxSellerId;
                        inboxObj.merchant_tranx_id = new Integer(req.getParameter("sellerOrderNumber"));
                        inboxObj.tranx_status = status;
			inboxObj.tranx_amt = new BigDecimal(req.getParameter("txnAmt"));
			
			System.out.println("Checkpoint 444");

			String transBankRefNo = "";

			if(req.getParameter("debitAuthNo") != null)
			{
				inboxObj.tranx_appr_code = req.getParameter("debitAuthNo");

				transBankRefNo = "Debit Auth No: " + transBankRefNo + inboxObj.tranx_appr_code;
			}
			
			if(req.getParameter("creditAuthNo") != null)
			{
				inboxObj.bank_ref_no = req.getParameter("creditAuthNo");

				transBankRefNo = "Credit Auth No: " + transBankRefNo + " | " +inboxObj.bank_ref_no;
			}

			req.setAttribute("transBankRefNo", transBankRefNo);

			if(req.getParameter("errorMessage") != null)
			{
				req.setAttribute("transError", req.getParameter("errorMessage"));
					
				inboxObj.tranx_err_code = req.getParameter("errorMessage");
			}
		
            inboxHome.create(inboxObj);

			System.out.println("Checkpoint 555");

                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

	private void updatePayment(HttpServletRequest req, String pkid, String paymentConfigPkid, String status, String formName)
	{
		try
		{
			System.out.println("Checkpoint yyyy");

			Integer so_pkid = new Integer(pkid);
			QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + so_pkid.toString() + "' " });

			Vector vecOrder = new Vector(SalesOrderIndexNut.getObjects(query));
			for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
			{
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecOrder.get(cnt1);
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);

				if(status.equals("success"))
				{	
					if (formName.equals("epayment3DSSL"))
					{
						String cardNumber = (String) req.getParameter("cardNumber");						
						
						if(cardNumber != null)
						{
							paymentConfigPkid = "10013";
						}
						
						EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(paymentConfigPkid));
							
						soObj.statusPayment = obj.payment_status;
						soObj.receiptRemarks = obj.payment_remarks;
						
					}else
					{
						EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(paymentConfigPkid));

						soObj.statusPayment = obj.payment_status;
						soObj.receiptRemarks = obj.payment_remarks;
					}
				}

				if(formName.equals("epaymentRHB"))
				{
					if(req.getParameter("ReturnMsg") != null)
						soObj.etxnErrorCode = req.getParameter("ReturnMsg");
					
					if (req.getParameter("ReferenceNo") != null)
						soObj.etxnCode = req.getParameter("ReferenceNo");

				} else if (formName.equals("epaymentHLB"))
				{
					if(req.getParameter("respCode") != null)
                        soObj.etxnErrorCode = req.getParameter("respCode");
					
					if (req.getParameter("trxref") != null)
						soObj.etxnCode = req.getParameter("trxref");
					
				} else if (formName.equals("epayment3DSSL"))
				{
					if(req.getParameter("reasonDescription") != null)
						soObj.etxnErrorCode = req.getParameter("reasonDescription");
					
					if (req.getParameter("referenceNumber") != null)
						soObj.etxnCode = req.getParameter("referenceNumber");
					
					if (req.getParameter("authorizationCode") != null)
						soObj.receiptApprovalCode = req.getParameter("authorizationCode");

				} else if (formName.equals("epaymentFPX"))
				{	
					if(req.getParameter("errorMessage") != null)
                        soObj.etxnErrorCode = req.getParameter("errorMessage");
					
					if (req.getParameter("debitAuthNo") != null)
						soObj.etxnCode = req.getParameter("debitAuthNo");
						
					if (req.getParameter("creditAuthNo") != null)
						soObj.etxnRemarks = req.getParameter("creditAuthNo");
				}
				

				soEJB.setObject(soObj);
			}

			System.out.println("Checkpoint zzzz");

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void fnSalesOrderAmountTotal(HttpServletRequest req, HttpServletResponse res, String SOGuid)
			throws Exception
	{

		Log.printVerbose("Checkpoint ZAAAAAA");

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


			AmtTotal = AmtTotal.add(csos.getBillAmount());

		}

		Log.printVerbose("vecSalesOrder "+ vecSalesOrder.size());

		session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

		session.setAttribute("vecSalesOrder", null);
		session.setAttribute("vecSalesOrder", vecSalesOrder);

		Log.printVerbose("Assigned to verSalesOrder session");
	}
}
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

import java.math.BigDecimal;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndex;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentConfigNut;
import com.vlee.ejb.ecommerce.EPaymentConfigObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPayment extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");

		if (formName.equals("epaymentRHB"))
		{
			String guid = (String) req.getParameter("OrderNo");
			String status = (String) req.getParameter("ReturnCode");
			String paymentConfigPkid = "10023"; 
			guid = guid.replaceAll("PG","");				
			if (status.equals("0"))
			{
				status = "success";
				req.setAttribute("status", "success");

				rhb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);
				
				try{ fnSalesOrderAmountTotal(req,res,guid.replaceAll("PG",""));}
				catch(Exception ex){ ex.printStackTrace();}
			} else
			{
				status = "fail";
				req.setAttribute("status", "fail");

				rhb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid.replaceAll("PG",""));}
                                catch(Exception ex){ ex.printStackTrace();}
			}
			
		} 
/*
		else if (formName.equals("paydirectApprove"))
		{
			String guid = (String) req.getParameter("ORDERID");
			String status = (String) req.getParameter("PDSTATUS");
			String paymentConfigPkid = "10012"; 
			
			if (status.equals("1"))
			{
				paydirect(req, status);	
				req.setAttribute("status", "success");
				updatePayment(req, guid, paymentConfigPkid, status, formName);
			} else
			{
				paydirect(req, status);
				req.setAttribute("status", "fail");
			}
			return new ActionRouter("member-checkout-payment-paydirect-page");

		} else if (formName.equals("paydirectUnapprove"))
		{
			String guid = (String) req.getParameter("ORDERID");
			String status = (String) req.getParameter("PDSTATUS");
			String paymentConfigPkid = "10012"; 
			
			paydirect(req, status);
			req.setAttribute("status", "fail");
			updatePayment(req, guid, paymentConfigPkid, status, formName);

			return new ActionRouter("member-checkout-payment-paydirect-page");
			
		} 
*/
		else if (formName.equals("epayment3DSSL"))
		{
                        String status = (String) req.getParameter("responseCode");
                        String paymentConfigPkid = "10012";

                        if (status.equals("1"))
                        {
				String guid = (String) req.getParameter("merchantOrderId");

				status = "success";
				req.setAttribute("status", "success");

                                threeDSSL(req, status);
                                updatePayment(req, guid, paymentConfigPkid, status, formName);

                                Log.printVerbose("Checkpoint Z22");

                                try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

                                Log.printVerbose("Checkpoint Z11");

                        } else if (status.equals("2"))
                        {
                                Log.printVerbose("Checkpoint Y11");

				status = "fail";
				req.setAttribute("status", "fail");

                                threeDSSL(req, status);

                		HttpSession session = req.getSession(true);

                		session.setAttribute("AmtTotal", "0");
                        }else
			{
				String guid = (String) req.getParameter("merchantOrderId");
			
				Log.printVerbose("Checkpoint Y11");
		
				status = "unknown";
				req.setAttribute("status", "unknown");

                                threeDSSL(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                                try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

                                Log.printVerbose("Checkpoint Y22");
			}		
	
		} else if (formName.equals("epaymentHLB"))
		{
			String guid = (String) req.getParameter("OrderNo");
			String status = (String) req.getParameter("respCode");
			String paymentConfigPkid = "10018"; 

			if (status.equals("000000"))
			{
				status = "success";
				req.setAttribute("status", "success");

				hlb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				Log.printVerbose("Checkpoint Z22");

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

				Log.printVerbose("Checkpoint Z11");
			} else
			{
				Log.printVerbose("Checkpoint Y11");

				status = "fail";
				req.setAttribute("status", "fail");

				hlb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

				Log.printVerbose("Checkpoint Y22");
			}
		} else if (formName.equals("epaymentFPX"))
                {
                        String guid = (String) req.getParameter("sellerOrderNumber");
                        String txnAmt = (String) req.getParameter("txnAmt");
			String debitAuthNo = (String) req.getParameter("debitAuthNo");
			String debitAuthCode = (String) req.getParameter("debitAuthCode");
			String creditAuthNo = (String) req.getParameter("creditAuthNo");
			String creditAuthCode = (String) req.getParameter("creditAuthCode");
                        String paymentConfigPkid = "10020";
			String status = "";

			System.out.println("guid : "+guid);
			System.out.println("txnAmt : "+txnAmt);
			System.out.println("debitAuthNo : "+debitAuthNo);
			System.out.println("creditAuthNo :"+creditAuthNo);

			if(! (debitAuthCode.equals("") && creditAuthCode.equals("")))
			{
			if(debitAuthCode.equals("00") && creditAuthCode.equals("00"))
                	{
				System.out.println("Checkpoint 111");

				status = "success";
                        	req.setAttribute("status", "success");

				fpx(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                        	try{ fnSalesOrderAmountTotal(req,res,guid);}
                        	catch(Exception ex){ ex.printStackTrace();}
                	}
                	else
                	{
				System.out.println("Checkpoint 222");

				status = "failed";
                       	 	req.setAttribute("status", "fail");

				fpx(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                        	try{ fnSalesOrderAmountTotal(req,res,guid);}
                        	catch(Exception ex){ ex.printStackTrace();}
                	}
			}
		}

		return new ActionRouter("checkout-receipt-page");
	}

	private void rhb(HttpServletRequest req, String status)
	{
		HttpSession session = req.getSession();
		try
		{
			String merchantID = (String) req.getParameter("MerchantID");
			String orderNo = (String) req.getParameter("OrderNo");
			String amount = (String) req.getParameter("Amount");
			String referenceNo = (String) req.getParameter("ReferenceNo");
			String returnMsg = (String) req.getParameter("ReturnMsg");
			String hashCount = (String) req.getParameter("HashCount");

			//orderNo = orderNo.substring(2);
			orderNo = orderNo.replaceAll("PG","");

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_RHBBANK;
			inboxObj.merchant_id = new Integer(merchantID);
			inboxObj.merchant_tranx_id = new Integer(orderNo);
			inboxObj.tranx_amt = new BigDecimal(amount);
			inboxObj.tranx_status = status;

			if(returnMsg != null)
				inboxObj.tranx_err_code = returnMsg;
	
			if(referenceNo != null)
				inboxObj.bank_ref_no = referenceNo;
	
			inboxObj.property2 = hashCount;

			inboxHome.create(inboxObj);

//                        EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
//                        EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
//                        csos.loadSalesOrder(new Long(orderNo));
//                        session.setAttribute("dist-sales-order-session", csos);



		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void hlb(HttpServletRequest req, String status)
	{
		try
		{
			//String sellerID = "EPY0000002";
			String orderNo = (String) req.getParameter("OrderNo");
			String paymentAmount = (String) req.getParameter("paymentAmount");
			String respCode = (String) req.getParameter("respCode");
			String trxref = (String) req.getParameter("trxref");
			String paymentMode = (String) req.getParameter("paymentMode");
			String errCode = (String) req.getParameter("respCode");
		
			// Log.printVerbose("sellerID "+sellerID);
			Log.printVerbose("orderNo " + orderNo);
			Log.printVerbose("paymentAmount " + paymentAmount);
			Log.printVerbose("trxref " + trxref);
			Log.printVerbose("paymentMode " + paymentMode);
			Log.printVerbose("respCode "+ respCode);

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_HLB;
			//inboxObj.merchant_id = new Integer(sellerID);
			inboxObj.merchant_tranx_id = new Integer(orderNo);
			inboxObj.tranx_amt = new BigDecimal(paymentAmount);
			inboxObj.tranx_status = status;

			if(trxref != null)
				inboxObj.bank_ref_no = trxref;

			if(status.equals("fail"))
			{
				inboxObj.tranx_err_code = errCode;
			}
				
			inboxObj.bank_ref_no = trxref;
			inboxObj.property1 = paymentMode;

			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void threeDSSL(HttpServletRequest req, String status)
        {
                try
                {
			String responseCode = (String) req.getParameter("responseCode");
        		String reasonCode = (String) req.getParameter("reasonCode");
        		String reasonDescription = (String) req.getParameter("reasonDescription");
        		String merchantId = (String) req.getParameter("merchantId");
        		String acquirerId = (String) req.getParameter("acquirerId");
        		String merchantOrderId = (String) req.getParameter("merchantOrderId");
        		String signature = (String) req.getParameter("signature");
        		String referenceNumber = (String) req.getParameter("referenceNumber");
        		String cardNumber = (String) req.getParameter("cardNumber");
        		String authorizationCode = (String) req.getParameter("authorizationCode");
        		String billingAddress = (String) req.getParameter("billingAddress");
        		String shippingAddress =(String) req.getParameter("shippingAddress");
		
			String cardType = "";
			
			if(cardNumber.charAt(0) == '4')
				cardType = "VISA";
			else
				cardType = "MASTER";
	
                        Log.printVerbose("responseCode " + responseCode);
                        Log.printVerbose("reasonCode " + reasonCode);
                        Log.printVerbose("reasonDescription " + reasonDescription);
                        Log.printVerbose("merchantId " + merchantId);
                        Log.printVerbose("acquirerId " + acquirerId);
			Log.printVerbose("merchantOrderId " + merchantOrderId);
                        Log.printVerbose("signature " + signature);
                        Log.printVerbose("referenceNumber " + referenceNumber);
                        Log.printVerbose("cardNumber " + cardNumber);
                        Log.printVerbose("authorizationCode " + authorizationCode);
			Log.printVerbose("billingAddress " + billingAddress);
                        Log.printVerbose("shippingAddress " + shippingAddress);
			Log.printVerbose("cardType " + cardType);
			
                        EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
                        EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			if(status.equals("success"))
			{
				if(cardType.equals("VISA"))
	                        	inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_VISA;
				else
					inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MASTER;

				inboxObj.merchant_id = new Integer(merchantId);
                        	inboxObj.merchant_tranx_id = new Integer(merchantOrderId);
                        	inboxObj.tranx_status = status;
				inboxObj.tranx_appr_code = authorizationCode;
				inboxObj.bank_ref_no = referenceNumber;
				inboxObj.property1 = acquirerId;
				inboxObj.property2 = signature;
				inboxObj.property3 = cardNumber;
				inboxObj.property4 = billingAddress;
				inboxObj.property5 = shippingAddress;
			}else 
			{
				if(merchantId != null)
				{
					inboxObj.merchant_tranx_id = new Integer(merchantOrderId);
				}else
				{
					inboxObj.merchant_tranx_id = new Integer(0);
				}
				
			}
			inboxObj.tranx_status = status;	
			inboxObj.tranx_err_code = reasonDescription;
			
                        inboxHome.create(inboxObj);
                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

	private void fpx(HttpServletRequest req, String status)
        {
                HttpSession session = req.getSession();
                try
                {
                        String fpxSellerId = "0000564";

                        EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
                        EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			System.out.println("Checkpoint 333");

                        inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_FPX;
                        inboxObj.merchant_id = new Integer(fpxSellerId);
                        inboxObj.merchant_tranx_id = new Integer(req.getParameter("sellerOrderNumber"));
                        inboxObj.tranx_status = status;
			inboxObj.tranx_amt = new BigDecimal(req.getParameter("txnAmt"));
			
			System.out.println("Checkpoint 444");

			if(req.getParameter("debutAuthNo") != null)
				inboxObj.tranx_appr_code = req.getParameter("debutAuthNo");
			
			if(req.getParameter("credutAuthNo") != null)
				inboxObj.bank_ref_no = req.getParameter("credutAuthNo");

			if(req.getParameter("errorMessage") != null)
				inboxObj.tranx_err_code = req.getParameter("errorMessage");
		
                        inboxHome.create(inboxObj);

			System.out.println("Checkpoint 555");

                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

/*
	private void paydirect(HttpServletRequest req, String status)
	{
		try
		{
			String orderid = (String) req.getParameter("ORDERID");
			String pdrespcode = null;
			if (req.getParameter("PDRESPCODE") != null)
				pdrespcode = (String) req.getParameter("PDRESPCODE");
			String pdauthcode = (String) req.getParameter("PDAUTHCODE");
			String pdtransid = (String) req.getParameter("PDTRANSID");
			String xid = (String) req.getParameter("XID");

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			inboxObj.merchant_id = new Integer(37);
			inboxObj.merchant_tranx_id = new Integer(orderid);
			inboxObj.tranx_status = status;
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_PAYDIRECT;

			if (pdrespcode != null)
				inboxObj.tranx_err_code = pdrespcode;

			if (pdauthcode != null)
				inboxObj.tranx_appr_code = pdauthcode;
	
			if (pdtransid != null)
				inboxObj.bank_ref_no = pdtransid;
	
			inboxObj.property3 = xid;
			
			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/
	// private void updatePayment(HttpServletRequest req, int pkid)
	// {
	// try
	// {
	// HttpSession session = req.getSession();
	// Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");
	// for (int x = 0; x < vecSalesOrder.size(); x++)
	// {
	// CreateSalesOrderSession csos = (CreateSalesOrderSession)
	// vecSalesOrder.get(x);
	// SalesOrderIndexObject salesObj = csos.getSalesOrderIndex();
	// EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(pkid));
	// salesObj.statusPayment = obj.payment_status;
	// salesObj.receiptRemarks = obj.payment_remarks;
	// SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(salesObj.pkid);
	// soEJB.setObject(salesObj);
	// }
	// session.removeAttribute("vecSalesOrder");
	// } catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	private void updatePayment(HttpServletRequest req, String pkid, String paymentConfigPkid, String status, String formName)
	{
		try
		{
			System.out.println("Checkpoint yyyy");

			Integer so_pkid = new Integer(pkid);
			QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + so_pkid.toString() + "' " });

			Vector vecOrder = new Vector(SalesOrderIndexNut.getObjects(query));
			for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
			{
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecOrder.get(cnt1);
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);

				if(status.equals("success"))
				{
					EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(paymentConfigPkid));
					soObj.statusPayment = obj.payment_status;
					soObj.receiptRemarks = obj.payment_remarks;
				}

				if(formName.equals("epaymentRHB"))
				{
					if(req.getParameter("ReturnMsg") != null)
						soObj.etxnErrorCode = req.getParameter("ReturnMsg");

				} else if (formName.equals("epaymentHLB"))
				{
					if(req.getParameter("respCode") != null)
                                                soObj.etxnErrorCode = req.getParameter("respCode");

				} else if (formName.equals("epayment3DSSL"))
				{
					if(req.getParameter("reasonDescription") != null)
						soObj.etxnErrorCode = req.getParameter("reasonDescription");

				} else if (formName.equals("epaymentFPX"))
				{	
					if(req.getParameter("errorMessage") != null)
                                                soObj.etxnErrorCode = req.getParameter("errorMessage");
				}
				

				soEJB.setObject(soObj);
			}

			System.out.println("Checkpoint zzzz");

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

/*
	private void fnNextPage(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			HttpSession session = request.getSession(true);

			String SOPkid = "0";
			String tempStr = (String) request.getParameter("mmm_tran_id");
			if (tempStr != null)
				SOPkid = tempStr;

			EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
			EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
			csos.loadSalesOrder(new Long(SOPkid));
			session.setAttribute("dist-sales-order-session", csos);

			fnSalesOrderAmountTotal(request, response, SOPkid);

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
*/


	public void fnSalesOrderAmountTotal(HttpServletRequest req, HttpServletResponse res, String SOGuid)
			throws Exception
	{

		Log.printVerbose("Checkpoint ZAAAAAA");

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


			AmtTotal = AmtTotal.add(csos.getBillAmount());

		}

		Log.printVerbose("vecSalesOrder "+ vecSalesOrder.size());

		session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

		session.setAttribute("vecSalesOrder", null);
		session.setAttribute("vecSalesOrder", vecSalesOrder);

		Log.printVerbose("Assigned to verSalesOrder session");
	}
}
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

import java.math.BigDecimal;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.bean.distribution.EditSalesOrderSession;
import com.vlee.ejb.customer.SalesOrderIndex;
import com.vlee.ejb.customer.SalesOrderIndexBean;
import com.vlee.ejb.customer.SalesOrderIndexNut;
import com.vlee.ejb.customer.SalesOrderIndexObject;
import com.vlee.ejb.ecommerce.EPaymentConfigNut;
import com.vlee.ejb.ecommerce.EPaymentConfigObject;
import com.vlee.ejb.ecommerce.EPaymentInboxBean;
import com.vlee.ejb.ecommerce.EPaymentInboxHome;
import com.vlee.ejb.ecommerce.EPaymentInboxNut;
import com.vlee.ejb.ecommerce.EPaymentInboxObject;
import com.vlee.ejb.ecommerce.EStoreObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;

public class DoEPayment extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");

		if (formName.equals("epaymentRHB"))
		{
			String guid = (String) req.getParameter("OrderNo");
			String status = (String) req.getParameter("ReturnCode");
			String paymentConfigPkid = "10023"; 
			guid = guid.replaceAll("PG","");				
			if (status.equals("0"))
			{
				status = "success";
				req.setAttribute("status", "success");

				rhb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);
				
				try{ fnSalesOrderAmountTotal(req,res,guid.replaceAll("PG",""));}
				catch(Exception ex){ ex.printStackTrace();}
			} else
			{
				status = "fail";
				req.setAttribute("status", "fail");

				rhb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid.replaceAll("PG",""));}
                                catch(Exception ex){ ex.printStackTrace();}
			}
			
		} 
/*
		else if (formName.equals("paydirectApprove"))
		{
			String guid = (String) req.getParameter("ORDERID");
			String status = (String) req.getParameter("PDSTATUS");
			String paymentConfigPkid = "10012"; 
			
			if (status.equals("1"))
			{
				paydirect(req, status);	
				req.setAttribute("status", "success");
				updatePayment(req, guid, paymentConfigPkid, status, formName);
			} else
			{
				paydirect(req, status);
				req.setAttribute("status", "fail");
			}
			return new ActionRouter("member-checkout-payment-paydirect-page");

		} else if (formName.equals("paydirectUnapprove"))
		{
			String guid = (String) req.getParameter("ORDERID");
			String status = (String) req.getParameter("PDSTATUS");
			String paymentConfigPkid = "10012"; 
			
			paydirect(req, status);
			req.setAttribute("status", "fail");
			updatePayment(req, guid, paymentConfigPkid, status, formName);

			return new ActionRouter("member-checkout-payment-paydirect-page");
			
		} 
*/
		else if (formName.equals("epayment3DSSL"))
		{
                        String status = (String) req.getParameter("responseCode");
                        String paymentConfigPkid = "10012";

                        if (status.equals("1"))
                        {
				String guid = (String) req.getParameter("merchantOrderId");

				status = "success";
				req.setAttribute("status", "success");

                                threeDSSL(req, status);
                                updatePayment(req, guid, paymentConfigPkid, status, formName);

                                Log.printVerbose("Checkpoint Z22");

                                try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

                                Log.printVerbose("Checkpoint Z11");

                        } else if (status.equals("2"))
                        {
                                Log.printVerbose("Checkpoint Y11");

				status = "fail";
				req.setAttribute("status", "fail");

                                threeDSSL(req, status);

                		HttpSession session = req.getSession(true);

                		session.setAttribute("AmtTotal", "0");
                        }else
			{
				String guid = (String) req.getParameter("merchantOrderId");
			
				Log.printVerbose("Checkpoint Y11");
		
				status = "unknown";
				req.setAttribute("status", "unknown");

                                threeDSSL(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                                try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

                                Log.printVerbose("Checkpoint Y22");
			}		
	
		} else if (formName.equals("epaymentHLB"))
		{
			String guid = (String) req.getParameter("OrderNo");
			String status = (String) req.getParameter("respCode");
			String paymentConfigPkid = "10018"; 

			if (status.equals("000000"))
			{
				status = "success";
				req.setAttribute("status", "success");

				hlb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				Log.printVerbose("Checkpoint Z22");

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

				Log.printVerbose("Checkpoint Z11");
			} else
			{
				Log.printVerbose("Checkpoint Y11");

				status = "fail";
				req.setAttribute("status", "fail");

				hlb(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

				try{ fnSalesOrderAmountTotal(req,res,guid);}
                                catch(Exception ex){ ex.printStackTrace();}

				Log.printVerbose("Checkpoint Y22");
			}
		} else if (formName.equals("epaymentFPX"))
                {
                        String guid = (String) req.getParameter("sellerOrderNumber");
                        String txnAmt = (String) req.getParameter("txnAmt");
			String debitAuthNo = (String) req.getParameter("debitAuthNo");
			String debitAuthCode = (String) req.getParameter("debitAuthCode");
			String creditAuthNo = (String) req.getParameter("creditAuthNo");
			String creditAuthCode = (String) req.getParameter("creditAuthCode");
                        String paymentConfigPkid = "10020";
			String status = "";

			System.out.println("guid : "+guid);
			System.out.println("txnAmt : "+txnAmt);
			System.out.println("debitAuthNo : "+debitAuthNo);
			System.out.println("creditAuthNo :"+creditAuthNo);

			if(! (debitAuthCode.equals("") && creditAuthCode.equals("")))
			{
			if(debitAuthCode.equals("00") && creditAuthCode.equals("00"))
                	{
				System.out.println("Checkpoint 111");

				status = "success";
                        	req.setAttribute("status", "success");

				fpx(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                        	try{ fnSalesOrderAmountTotal(req,res,guid);}
                        	catch(Exception ex){ ex.printStackTrace();}
                	}
                	else
                	{
				System.out.println("Checkpoint 222");

				status = "failed";
                       	 	req.setAttribute("status", "fail");

				fpx(req, status);
				updatePayment(req, guid, paymentConfigPkid, status, formName);

                        	try{ fnSalesOrderAmountTotal(req,res,guid);}
                        	catch(Exception ex){ ex.printStackTrace();}
                	}
			}
		}

		return new ActionRouter("checkout-receipt-page");
	}

	private void rhb(HttpServletRequest req, String status)
	{
		HttpSession session = req.getSession();
		try
		{
			String merchantID = (String) req.getParameter("MerchantID");
			String orderNo = (String) req.getParameter("OrderNo");
			String amount = (String) req.getParameter("Amount");
			String referenceNo = (String) req.getParameter("ReferenceNo");
			String returnMsg = (String) req.getParameter("ReturnMsg");
			String hashCount = (String) req.getParameter("HashCount");

			//orderNo = orderNo.substring(2);
			orderNo = orderNo.replaceAll("PG","");

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_RHBBANK;
			inboxObj.merchant_id = new Integer(merchantID);
			inboxObj.merchant_tranx_id = new Integer(orderNo);
			inboxObj.tranx_amt = new BigDecimal(amount);
			inboxObj.tranx_status = status;

			if(returnMsg != null)
				inboxObj.tranx_err_code = returnMsg;
	
			if(referenceNo != null)
				inboxObj.bank_ref_no = referenceNo;
	
			inboxObj.property2 = hashCount;

			inboxHome.create(inboxObj);

//                        EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
//                        EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
//                        csos.loadSalesOrder(new Long(orderNo));
//                        session.setAttribute("dist-sales-order-session", csos);



		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void hlb(HttpServletRequest req, String status)
	{
		try
		{
			//String sellerID = "EPY0000002";
			String orderNo = (String) req.getParameter("OrderNo");
			String paymentAmount = (String) req.getParameter("paymentAmount");
			String respCode = (String) req.getParameter("respCode");
			String trxref = (String) req.getParameter("trxref");
			String paymentMode = (String) req.getParameter("paymentMode");
			String errCode = (String) req.getParameter("respCode");
		
			// Log.printVerbose("sellerID "+sellerID);
			Log.printVerbose("orderNo " + orderNo);
			Log.printVerbose("paymentAmount " + paymentAmount);
			Log.printVerbose("trxref " + trxref);
			Log.printVerbose("paymentMode " + paymentMode);
			Log.printVerbose("respCode "+ respCode);

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();
			
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_HLB;
			//inboxObj.merchant_id = new Integer(sellerID);
			inboxObj.merchant_tranx_id = new Integer(orderNo);
			inboxObj.tranx_amt = new BigDecimal(paymentAmount);
			inboxObj.tranx_status = status;

			if(trxref != null)
				inboxObj.bank_ref_no = trxref;

			if(status.equals("fail"))
			{
				inboxObj.tranx_err_code = errCode;
			}
				
			inboxObj.bank_ref_no = trxref;
			inboxObj.property1 = paymentMode;

			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void threeDSSL(HttpServletRequest req, String status)
        {
                try
                {
			String responseCode = (String) req.getParameter("responseCode");
        		String reasonCode = (String) req.getParameter("reasonCode");
        		String reasonDescription = (String) req.getParameter("reasonDescription");
        		String merchantId = (String) req.getParameter("merchantId");
        		String acquirerId = (String) req.getParameter("acquirerId");
        		String merchantOrderId = (String) req.getParameter("merchantOrderId");
        		String signature = (String) req.getParameter("signature");
        		String referenceNumber = (String) req.getParameter("referenceNumber");
        		String cardNumber = (String) req.getParameter("cardNumber");
        		String authorizationCode = (String) req.getParameter("authorizationCode");
        		String billingAddress = (String) req.getParameter("billingAddress");
        		String shippingAddress =(String) req.getParameter("shippingAddress");
		
			String cardType = "";
			
			if(cardNumber.charAt(0) == '4')
				cardType = "VISA";
			else
				cardType = "MASTER";
	
                        Log.printVerbose("responseCode " + responseCode);
                        Log.printVerbose("reasonCode " + reasonCode);
                        Log.printVerbose("reasonDescription " + reasonDescription);
                        Log.printVerbose("merchantId " + merchantId);
                        Log.printVerbose("acquirerId " + acquirerId);
			Log.printVerbose("merchantOrderId " + merchantOrderId);
                        Log.printVerbose("signature " + signature);
                        Log.printVerbose("referenceNumber " + referenceNumber);
                        Log.printVerbose("cardNumber " + cardNumber);
                        Log.printVerbose("authorizationCode " + authorizationCode);
			Log.printVerbose("billingAddress " + billingAddress);
                        Log.printVerbose("shippingAddress " + shippingAddress);
			Log.printVerbose("cardType " + cardType);
			
                        EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
                        EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			if(status.equals("success"))
			{
				if(cardType.equals("VISA"))
	                        	inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_VISA;
				else
					inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_MASTER;

				inboxObj.merchant_id = new Integer(merchantId);
                        	inboxObj.merchant_tranx_id = new Integer(merchantOrderId);
                        	inboxObj.tranx_status = status;
				inboxObj.tranx_appr_code = authorizationCode;
				inboxObj.bank_ref_no = referenceNumber;
				inboxObj.property1 = acquirerId;
				inboxObj.property2 = signature;
				inboxObj.property3 = cardNumber;
				inboxObj.property4 = billingAddress;
				inboxObj.property5 = shippingAddress;
			}else 
			{
				if(merchantId != null)
				{
					inboxObj.merchant_tranx_id = new Integer(merchantOrderId);
				}else
				{
					inboxObj.merchant_tranx_id = new Integer(0);
				}
				
			}
			inboxObj.tranx_status = status;	
			inboxObj.tranx_err_code = reasonDescription;
			
                        inboxHome.create(inboxObj);
                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

	private void fpx(HttpServletRequest req, String status)
        {
                HttpSession session = req.getSession();
                try
                {
                        String fpxSellerId = "0000564";

                        EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
                        EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			System.out.println("Checkpoint 333");

                        inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_FPX;
                        inboxObj.merchant_id = new Integer(fpxSellerId);
                        inboxObj.merchant_tranx_id = new Integer(req.getParameter("sellerOrderNumber"));
                        inboxObj.tranx_status = status;
			inboxObj.tranx_amt = new BigDecimal(req.getParameter("txnAmt"));
			
			System.out.println("Checkpoint 444");

			if(req.getParameter("debutAuthNo") != null)
				inboxObj.tranx_appr_code = req.getParameter("debutAuthNo");
			
			if(req.getParameter("credutAuthNo") != null)
				inboxObj.bank_ref_no = req.getParameter("credutAuthNo");

			if(req.getParameter("errorMessage") != null)
				inboxObj.tranx_err_code = req.getParameter("errorMessage");
		
                        inboxHome.create(inboxObj);

			System.out.println("Checkpoint 555");

                } catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

/*
	private void paydirect(HttpServletRequest req, String status)
	{
		try
		{
			String orderid = (String) req.getParameter("ORDERID");
			String pdrespcode = null;
			if (req.getParameter("PDRESPCODE") != null)
				pdrespcode = (String) req.getParameter("PDRESPCODE");
			String pdauthcode = (String) req.getParameter("PDAUTHCODE");
			String pdtransid = (String) req.getParameter("PDTRANSID");
			String xid = (String) req.getParameter("XID");

			EPaymentInboxHome inboxHome = EPaymentInboxNut.getHome();
			EPaymentInboxObject inboxObj = new EPaymentInboxObject();

			inboxObj.merchant_id = new Integer(37);
			inboxObj.merchant_tranx_id = new Integer(orderid);
			inboxObj.tranx_status = status;
			inboxObj.payment_mode = EPaymentInboxBean.PAYMENT_PAYDIRECT;

			if (pdrespcode != null)
				inboxObj.tranx_err_code = pdrespcode;

			if (pdauthcode != null)
				inboxObj.tranx_appr_code = pdauthcode;
	
			if (pdtransid != null)
				inboxObj.bank_ref_no = pdtransid;
	
			inboxObj.property3 = xid;
			
			inboxHome.create(inboxObj);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/
	// private void updatePayment(HttpServletRequest req, int pkid)
	// {
	// try
	// {
	// HttpSession session = req.getSession();
	// Vector vecSalesOrder = (Vector) session.getAttribute("vecSalesOrder");
	// for (int x = 0; x < vecSalesOrder.size(); x++)
	// {
	// CreateSalesOrderSession csos = (CreateSalesOrderSession)
	// vecSalesOrder.get(x);
	// SalesOrderIndexObject salesObj = csos.getSalesOrderIndex();
	// EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(pkid));
	// salesObj.statusPayment = obj.payment_status;
	// salesObj.receiptRemarks = obj.payment_remarks;
	// SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(salesObj.pkid);
	// soEJB.setObject(salesObj);
	// }
	// session.removeAttribute("vecSalesOrder");
	// } catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	private void updatePayment(HttpServletRequest req, String pkid, String paymentConfigPkid, String status, String formName)
	{
		try
		{
			System.out.println("Checkpoint yyyy");

			Integer so_pkid = new Integer(pkid);
			QueryObject query = new QueryObject(new String[] { SalesOrderIndexBean.GUID + " = '" + so_pkid.toString() + "' " });

			Vector vecOrder = new Vector(SalesOrderIndexNut.getObjects(query));
			for (int cnt1 = 0; cnt1 < vecOrder.size(); cnt1++)
			{
				SalesOrderIndexObject soObj = (SalesOrderIndexObject) vecOrder.get(cnt1);
				SalesOrderIndex soEJB = SalesOrderIndexNut.getHandle(soObj.pkid);

				if(status.equals("success"))
				{
					EPaymentConfigObject obj = EPaymentConfigNut.getObject(new Long(paymentConfigPkid));
					soObj.statusPayment = obj.payment_status;
					soObj.receiptRemarks = obj.payment_remarks;
				}

				if(formName.equals("epaymentRHB"))
				{
					if(req.getParameter("ReturnMsg") != null)
						soObj.etxnErrorCode = req.getParameter("ReturnMsg");

				} else if (formName.equals("epaymentHLB"))
				{
					if(req.getParameter("respCode") != null)
                                                soObj.etxnErrorCode = req.getParameter("respCode");

				} else if (formName.equals("epayment3DSSL"))
				{
					if(req.getParameter("reasonDescription") != null)
						soObj.etxnErrorCode = req.getParameter("reasonDescription");

				} else if (formName.equals("epaymentFPX"))
				{	
					if(req.getParameter("errorMessage") != null)
                                                soObj.etxnErrorCode = req.getParameter("errorMessage");
				}
				

				soEJB.setObject(soObj);
			}

			System.out.println("Checkpoint zzzz");

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

/*
	private void fnNextPage(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			HttpSession session = request.getSession(true);

			String SOPkid = "0";
			String tempStr = (String) request.getParameter("mmm_tran_id");
			if (tempStr != null)
				SOPkid = tempStr;

			EStoreObject objStore = (EStoreObject) session.getAttribute("objStore");
			EditSalesOrderSession csos = new EditSalesOrderSession(objStore.ecom_userId);
			csos.loadSalesOrder(new Long(SOPkid));
			session.setAttribute("dist-sales-order-session", csos);

			fnSalesOrderAmountTotal(request, response, SOPkid);

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
*/


	public void fnSalesOrderAmountTotal(HttpServletRequest req, HttpServletResponse res, String SOGuid)
			throws Exception
	{

		Log.printVerbose("Checkpoint ZAAAAAA");

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


			AmtTotal = AmtTotal.add(csos.getBillAmount());

		}

		Log.printVerbose("vecSalesOrder "+ vecSalesOrder.size());

		session.setAttribute("AmtTotal", String.valueOf(AmtTotal));

		session.setAttribute("vecSalesOrder", null);
		session.setAttribute("vecSalesOrder", vecSalesOrder);

		Log.printVerbose("Assigned to verSalesOrder session");
	}
}
