/*==========================================================
*
* Copyright  of Vincent Lee. All Rights Reserved.
* (http://www.wavelet.biz)
*
* This software is the proprietary information of Wavelet,
* Use is subject to license terms.
*
==========================================================*/
package com.vlee.servlet.sfa;

import java.math.*;
import java.util.*;
import javax.servlet.http.*;
import com.vlee.bean.inventory.*;
import com.vlee.bean.loyalty.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.UserNut;
import com.vlee.ejb.customer.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.bean.application.*;

public class DoWidgetSellSFAStock implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		
		System.out.println("Inside DoWidgetSellStock");
		System.out.println("formName : "+formName);
		
		fnGetSetParams(servlet, req, res);
		if (formName == null)
		{
			return new ActionRouter("trading-widget-sell-sfa-stock-page");
		} 
		if(formName.equals("newForm"))
		{
			fnNewForm(servlet,req,res);
		}
		else if (formName.equals("searchItem"))
		{
			try
			{
				fnGetSearchResults(servlet, req, res);
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("trading-widget-sell-sfa-stock-page");
		} 
		else if (formName.equals("selectCode"))
		{
			fnNewForm(servlet,req,res);
			return fnSelectCode(servlet, req, res);
		} 
		else if (formName.equals("setDetailsSerial"))
		{
			try
			{
				fnSetDetailsSerial(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("trading-widget-sell-sfa-stock-serial-page");
		} 
		else if (formName.equals("setDetailsNonSerial"))
		{
			try
			{
				fnSetDetailsNonSerial(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("trading-widget-sell-sfa-stock-non-serial-page");
		} 
		else if (formName.equals("addSerial"))
		{
			try
			{
				fnAddSerial(servlet, req, res);
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
			return new ActionRouter("trading-widget-sell-sfa-stock-serial-page");
		} 
		else if (formName.equals("selectSerial"))
		{
			try
			{
				fnSelectSerial(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("trading-widget-sell-sfa-stock-serial-page");
		} 
		else if (formName.equals("clearSerial"))
		{
			fnClearSerialNumber(servlet, req, res);
			return new ActionRouter("trading-widget-sell-sfa-stock-serial-page");
		} 
		else if (formName.equals("dropSelectedSerial"))
		{
			fnDropSelectedSerial(servlet, req, res);
			return new ActionRouter("trading-widget-sell-sfa-stock-serial-page");
		} 
		else if (formName.equals("showInfos"))
		{
			try
			{
				showSerial(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("trading-widget-sell-sfa-stock-serial-info-page");
		}
		else if(formName.equals("scanSerialItemCode"))
		{
			try
			{
				return fnScanSerialItemCode(servlet,req,res);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else if(formName.equals("editForm"))
		{
			try
			{
				return fnEditForm(servlet,req,res);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		if(formName.equals("setReward"))
		{
			try
			{ return fnSetReward(servlet,req,res); }
			catch(Exception ex)
			{ ex.printStackTrace();}
		}
	
		if(formName.equals("setRedeem"))
		{
			try
			{ return fnSetRedeem(servlet,req,res); }
			catch(Exception ex)
			{ ex.printStackTrace();}
		}

		return new ActionRouter("trading-widget-sell-sfa-stock-page");
	}

	private ActionRouter fnSetReward(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
     HttpSession session = req.getSession();
	  	String action = req.getParameter("action");
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		ess.setLoyaltyLogic(CustMembershipCampaignRulesBean.RULE_TYPE_REWARD);
     if(ess.getSerialized())
     { return new ActionRouter("trading-widget-sell-stock-serial-page"); } 
		else { return new ActionRouter("trading-widget-sell-sfa-stock-non-serial-page"); }
	}
	
	private ActionRouter fnSetRedeem(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
     HttpSession session = req.getSession();
	  	String action = req.getParameter("action");
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		ess.setLoyaltyLogic(CustMembershipCampaignRulesBean.RULE_TYPE_REDEEM);
     if(ess.getSerialized())
     { return new ActionRouter("trading-widget-sell-stock-serial-page"); } 
		else { return new ActionRouter("trading-widget-sell-sfa-stock-non-serial-page"); }
	}


  private void fnNewForm(HttpServlet servlet,
                             HttpServletRequest req,
                             HttpServletResponse res)
  {
     HttpSession session = req.getSession();
     Log.printVerbose("fnNewForm: Adding userId");
     Integer userId = (Integer) session.getAttribute("userId");
     Integer branchId;
     // TKW20060416: Added this to allow retaining of branch when formName=selectCode.
     if(session.getAttribute("WidgetSellStockBranch")!=null)
     {
   	  Log.printVerbose("fnNewForm: Adding branch method 1");
   	  branchId = new Integer(session.getAttribute("WidgetSellStockBranch").toString());
     }
     else
     {
   	  Log.printVerbose("fnNewForm: Adding branch method 2");
   	  branchId = new Integer(req.getParameter("branch"));
     }
     

     // TKW20070411: Whole section commented out to remove the add link  
     EnterStockSession efif = new EnterStockSession(userId);          
     
		// TKW20070411: Whole section commented out to remove the add link
     	String pricingScheme = req.getParameter("pricingScheme");
		if(pricingScheme!=null) 
		{ efif.setPricingScheme(pricingScheme);	}
		String factorDiscount = req.getParameter("factorDiscount");
		
		String discFactorApplicableItem = req.getParameter("discFactorApplicableItem");
		System.out.println("discFactorApplicableItem : "+discFactorApplicableItem);
		
		if("true".equals(discFactorApplicableItem))
		{
			if(factorDiscount!=null)
			{
			  try
			  {
				  BigDecimal bdFactorDiscount = new BigDecimal(factorDiscount);
				  efif.setFactorDiscount(bdFactorDiscount);
			  }
			  catch(Exception ex){ ex.printStackTrace();}
			}
		}			
		
		// TKW20070718: get salesmanId from main invoice screen and set as default here.
		String salesmanId = req.getParameter("salesmanId").toString();
		if(salesmanId!=null)
		{
			try
			{
				efif.setPic1(UserNut.getUserName(new Integer(salesmanId)));
				req.setAttribute("salesmanId", salesmanId);
			}		
			catch(Exception ex){ ex.printStackTrace();}				
		}		  			
		// End TKW20070718
		
		String action = req.getParameter("action");
		efif.setParentAction(action);		
		
		efif.setBranch(branchId);
		Log.printVerbose(action+EnterStockSession.OBJNAME + " has been populated.");
		
		// Janet : If don't do this, it will overwrite the variable
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		efif.setDisableCheckStockInJobsheet(ess.getDisableCheckStockInJobsheet());
		// End Janet
		
		session.setAttribute(action+EnterStockSession.OBJNAME,efif);
		session.setAttribute(efif.getKey(),efif);

  }



  private ActionRouter fnEditForm(HttpServlet servlet,
                                HttpServletRequest req,
                                HttpServletResponse res)
		throws Exception
  {
     HttpSession session = req.getSession();
	  String action = req.getParameter("action");
	  String discFactorApplicableItem = req.getParameter("discFactorApplicableItem");
	 	  
     String essKey = req.getParameter("essKey");
     
     EnterStockSession efif = (EnterStockSession) session.getAttribute(essKey);
     String parentAction = req.getParameter("action");
     efif.setParentAction(parentAction);
     efif.setDiscFactorApplicableItem(discFactorApplicableItem);
     
     	// Janet : If don't do this, it will overwrite the variable
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		efif.setDisableCheckStockInJobsheet(ess.getDisableCheckStockInJobsheet());
		// End Janet
		
		session.setAttribute(action + EnterStockSession.OBJNAME, efif);
		session.setAttribute(efif.getKey(),efif);
		if(efif.getSerialized())
		{ return new ActionRouter("trading-widget-sell-stock-serial-page"); }
		else 
		{
			/// do not auto close when user is editing
			req.setAttribute("autoClose",req.getParameter("autoClose")); 
			return new ActionRouter("trading-widget-sell-sfa-stock-non-serial-page"); 
		}
  }




	// //////////////////////////////////////////////////////
	protected ActionRouter fnScanSerialItemCode(HttpServlet servlet,
													HttpServletRequest req,
													HttpServletResponse res)
	{
		String itemCode = req.getParameter("itemCode");

     String action = req.getParameter("action");
		String strBranch = req.getParameter("branch");
     HttpSession session = req.getSession();
//     EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		Integer userId = (Integer) session.getAttribute("userId");
		EnterStockSession ess = new EnterStockSession(userId);

		// TKW20070718: get salesmanId from main invoice screen and set as default here.
		String salesmanId = req.getParameter("salesmanId").toString();
		Log.printVerbose("THIS IS THE salesmanId: " + salesmanId);
		  try
		  {
			  ess.setPic1(UserNut.getUserName(new Integer(salesmanId)));
			  Log.printVerbose("WAHAHAHA&&&&& - Pic1 has been set to: " + UserNut.getUserName(new Integer(salesmanId)));
			  req.setAttribute("salesmanId", salesmanId);
		  }
		  catch(Exception ex){ ex.printStackTrace();}				
		// End TKW20070718		
		
		// Janet : If don't do this, it will overwrite the variable
		EnterStockSession efif = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		ess.setDisableCheckStockInJobsheet(efif.getDisableCheckStockInJobsheet());
		// End Janet
		
		String pricingScheme = req.getParameter("pricingScheme");
		if(pricingScheme!=null) 
		{ ess.setPricingScheme(pricingScheme);	}
		String factorDiscount = req.getParameter("factorDiscount");
		if(factorDiscount!=null)
		{
			try
			{
				BigDecimal bdFactorDiscount = new BigDecimal(factorDiscount);
				ess.setFactorDiscount(bdFactorDiscount);
			}
			catch(Exception ex){ ex.printStackTrace();}
		}
		session.setAttribute(action + EnterStockSession.OBJNAME, ess);
     session.setAttribute(ess.getKey(),ess);
     if (itemCode != null && itemCode.length() > 1)
     {
        try
        {
			Integer iBranch = new Integer(strBranch);
			ess.setBranch(iBranch);
           ess.setItemCode(itemCode);

			////// check if there's membership campaign / promotion
			CustMembershipCampaignRulesObject rewardObj = CustMembershipCampaignEngine.getRewardRules(ess.getItemObject());
			if(rewardObj!=null)
			{ ess.computeMembershipReward(); }

           if (ess.getSerialized())
           { return new ActionRouter("trading-widget-sell-stock-serial-page"); } 
				else
           { return new ActionRouter("trading-widget-sell-sfa-stock-non-serial-page"); }
        } catch (Exception ex)
        {
           req.setAttribute("errMsg", ex.getMessage());
           ex.printStackTrace();
        }
     }
     return new ActionRouter("trading-widget-sell-sfa-stock-pagee");
	}

	// //////////////////////////////////////////////////////
	protected void fnDropSelectedSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String action = req.getParameter("action");
		HttpSession session = req.getSession();
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		String[] serialNum = req.getParameterValues("serialNum");
		if (serialNum == null)
			return;
		for (int cnt1 = 0; cnt1 < serialNum.length; cnt1++)
		{
			if (serialNum[cnt1] == null || serialNum[cnt1].length() == 0)
			{
				continue;
			}
			ess.dropSerialNumber(serialNum[cnt1].toUpperCase());
		}
	}

	private void showSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String action = req.getParameter("action");
		req.setAttribute("action",action);
		String numbers = (String) req.getParameter("numbers");
		Vector v = new Vector();
		if (numbers != null)
		{
			StringTokenizer st = new StringTokenizer((String) req.getParameter("numbers"), "|");
			while (st.hasMoreTokens())
			{
				String serialNumber = st.nextToken();
				if (serialNumber != null && !serialNumber.trim().equals(""))
				{
					serialNumber = serialNumber.toUpperCase();
					QueryObject query = new QueryObject(new String[] { SerialNumberIndexBean.SERIAL1 + " = '"
							+ serialNumber + "' " });
					Collection coll = (Collection) SerialNumberIndexNut.getObjects(query);
					if (coll.iterator().hasNext())
					{
						SerialNumberIndexObject obj = (SerialNumberIndexObject) coll.iterator().next();
						v.add(obj);
					}
				}
			}
			req.setAttribute("v", v);
		}
	}

	protected void fnClearSerialNumber(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String action = req.getParameter("action");
		HttpSession session = req.getSession();
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		ess.clearSerialNumber();
	}

	protected void fnSelectSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String action = req.getParameter("action");
		HttpSession session = req.getSession();
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		String[] serial = req.getParameterValues("serialNumber");
		for (int cnt1 = 0; cnt1 < serial.length; cnt1++)
		{
			if (serial[cnt1].length() < 2)
				continue;
			ess.addSerialNumber(serial[cnt1].toUpperCase());
		}
	}

	protected void fnAddSerial(HttpServlet servlet, HttpServletRequest req, 
				HttpServletResponse res) throws Exception
	{
		String action = req.getParameter("action");
		HttpSession session = req.getSession();
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		String serial1 = req.getParameter("serial");
		String first = req.getParameter("first");
		String last = req.getParameter("last");
		// / single serial number
		if (serial1 != null)
		{
			if(serial1.trim().length()==0)
			{
				req.setAttribute("focusElement","price");
			}
			else
			{
				ess.addSerialNumberInStock(serial1.trim().toUpperCase());
			}
		}
		// / multiple serial number
		if (first != null && last != null)
		{
			try
			{
				first = first.trim().toUpperCase();
				last = last.trim().toUpperCase();
				String[] serial = SerialNumberDeltaNut.fnGetSequence(first, last);
				for (int cnt1 = 0; cnt1 < serial.length; cnt1++)
				{
					ess.addSerialNumberInStock(serial[cnt1]);
				}
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", "Error: " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	protected void fnSetDetailsSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String action = req.getParameter("action");
		HttpSession session = req.getSession();
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		String remarks = req.getParameter("remarks");

		String imei = req.getParameter("imei");
		if(imei!=null &&imei.trim().length()>2)
		{ remarks += "IMEI:"+imei.trim().toUpperCase();}

		ess.setRemarks(remarks);
		String pic1 = req.getParameter("pic1");
		String pic2 = "";//req.getParameter("pic2");
		String pic3 = "";//req.getParameter("pic3");
		ess.setPic1(pic1);
		ess.setPic2(pic1);
		ess.setPic3(pic1);
		ess.setWarrantyPeriod(new Integer(req.getParameter("warrantyPeriod")));
		ess.setCodeProject(req.getParameter("codeProject"));
		ess.setCodeDealer(req.getParameter("codeDealer"));
		ess.setCodeDepartment(req.getParameter("codeDepartment"));
		try
		{
			ess.setPrice2(new BigDecimal(req.getParameter("price2")));
			ess.setCcy2(req.getParameter("ccy2"));
		} catch (Exception ex)
		{
		}
		BigDecimal bdPrice = null;
		try
		{
			bdPrice = new BigDecimal(req.getParameter("price"));
		} catch (Exception ex)
		{
			throw new Exception("Invalid Price");
		}

		// / check the price against various parameters
		/*
		 * if(!ess.getValidManager()) { // if not manager, do these checks
		 * ItemObject itmObj = ess.getItemObject(); StockObject stkObj =
		 * ess.getStockObject(); if(stkObj==null) { throw new Exception("The
		 * quantity of this stock is ZERO!!"); }
		 * 
		 * if(bdPrice.compareTo(itmObj.replacementUnitCost)<0) { throw new
		 * Exception("The Price is Lower than Replacement Cost!!!"); }
		 * if(bdPrice.compareTo(stkObj.unitCostMA)<0) { throw new
		 * Exception("The cost is lower than Moving Average Cost!!!");} }
		 */
		ItemObject itmObj = ess.getItemObject();
		if(itmObj!=null && bdPrice!=null)
		{
			if(bdPrice.compareTo(itmObj.priceMin)<0)
			{ throw new Exception("The Price Is Lower Than Minimum Price ("+CurrencyFormat.strCcy(itmObj.priceMin)+"!");}
		}


		ess.setUnitPrice(bdPrice);
		String discount = req.getParameter("discount");
		if(discount!=null)
		{
			BigDecimal bdDiscount = new BigDecimal(0);
			try{ bdDiscount = new BigDecimal(discount); } catch(Exception ex){}
			ess.setUnitDiscount(bdDiscount);
		}
		// TKW20070416: Resetting branch after objective is complete.
		session.setAttribute("WidgetSellStockBranch",null);

	}

	protected void fnSetDetailsNonSerial(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		String action = req.getParameter("action");
		HttpSession session = req.getSession();
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		String remarks = req.getParameter("remarks");
		String imei = req.getParameter("imei");
		if(imei!=null &&imei.trim().length() >2)
		{ remarks += "IMEI:"+imei.trim().toUpperCase();}
		ess.setRemarks(remarks);
		String pic1 = req.getParameter("pic1"); ess.setPic1(UserNut.getUserName(new Integer(pic1)));
		String pic2 = req.getParameter("pic2"); ess.setPic2(UserNut.getUserName(new Integer(pic2)));
		String pic3 = req.getParameter("pic3"); ess.setPic3(UserNut.getUserName(new Integer(pic3)));
		ess.setCodeProject(req.getParameter("codeProject"));
		ess.setCodeDealer(req.getParameter("codeDealer"));
		ess.setCodeDepartment(req.getParameter("codeDepartment"));
		ess.setItemName(req.getParameter("itemName"));
		BigDecimal bdPrice = null;
		BigDecimal bdQty = null;
		// BigDecimal bdPriceDropDown = null; // JOB-JOE
		try
		{ bdPrice = new BigDecimal(req.getParameter("price")); } 
		catch (Exception ex)
		{ throw new Exception("Invalid Price"); }
		
		//[[JOB-JOE Not needed. There's Javascript to autopopulate the price text field
//		try
//		{ bdPriceDropDown = new BigDecimal(req.getParameter("priceDropDown")); } 
//		catch (Exception ex)
//		{ throw new Exception("Invalid Price option"); }
		//JOB-JOE]]

		ItemObject itmObj = ess.getItemObject();
		if(itmObj!=null && bdPrice!=null)
		{
			if(bdPrice.compareTo(itmObj.priceMin)<0)
			{ throw new Exception("The Price Is Lower Than Minimum Price ("+CurrencyFormat.strCcy(itmObj.priceMin)+"!");}
		}
		
		//[[JOB-JOE
//		if(itmObj!=null && bdPrice==null) if(bdPriceDropDown!=null)
//		{
//			if(bdPriceDropDown.compareTo(itmObj.priceMin)<0)
//			{ throw new Exception("The Price Is Lower Than Minimum Price ("+CurrencyFormat.strCcy(itmObj.priceMin)+"!");}
//			bdPrice = bdPriceDropDown;
//		}
		//JOB-JOE]]
		
		ess.setUnitPrice(bdPrice);

		String discount = req.getParameter("discount");
		if(discount!=null)
		{
			BigDecimal bdDiscount = new BigDecimal(0);
			try{ bdDiscount = new BigDecimal(discount); } catch(Exception ex){}
			ess.setUnitDiscount(bdDiscount);
		}


		try
		{
			ess.setPrice2(new BigDecimal(req.getParameter("price2")));
			ess.setCcy2(req.getParameter("ccy2"));
		} catch (Exception ex)
		{
		}
		// / check the price against various parameters
		/*
		 * if(!ess.getValidManager()) { // if not manager, do these checks
		 * ItemObject itmObj = ess.getItemObject(); StockObject stkObj =
		 * ess.getStockObject(); if(stkObj==null) { throw new Exception("The
		 * quantity of this stock is ZERO!!"); }
		 * if(bdPrice.compareTo(itmObj.replacementUnitCost)<0) { throw new
		 * Exception("The Price is Lower than Replacement Cost!!!"); }
		 * 
		 * if(bdPrice.compareTo(stkObj.unitCostMA)<0) { throw new
		 * Exception("The cost is lower than Moving Average Cost!!!");} }
		 */
		try
		{
			bdQty = new BigDecimal(req.getParameter("qty"));
		} catch (Exception ex)
		{
			throw new Exception("Invalid Quantity");
		}		
		
		System.out.println("ess.getDisableCheckStockInJobsheet() : "+ess.getDisableCheckStockInJobsheet());
		if(!"true".equals(ess.getDisableCheckStockInJobsheet()))
		{
			if (!ess.getValidManager())
			{
				// if not manager, do these checks
				ess.getItemObject();
/*				StockObject stkObj = ess.getStockObject();
				if (stkObj == null)
				{
					throw new Exception("The quantity of this stock is ZERO!!");
				}
				if (bdQty.compareTo(stkObj.balance) > 0)
				{
					throw new Exception("Negative Qty!!!");
				}*/
			}											
		}
		
		ess.setQty(bdQty);
		// TKW20070416: Resetting branch after objective is complete.
		session.setAttribute("WidgetSellStockBranch",null);
	}

	protected ActionRouter fnSelectCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{		
		String itemCode = req.getParameter("itemCode");		
		String action = req.getParameter("action");
		
		HttpSession session = req.getSession();
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		if(ess!=null)
		{
			Log.printVerbose("@@-ess is not null.");
		}
		if (itemCode != null && itemCode.length() > 1)
		{
			try
			{
				Log.printVerbose("@@-Attempting to set itemCode in ess.");
				ess.setItemCode(itemCode);
				Log.printVerbose("@@-Attempt succeeded.");
				//// the following lines is to retrieve the Pseudo Code object, and populate the fake name and pseudo price
				String bomLinkId = req.getParameter("bomLinkId");
				if(bomLinkId!=null)
				{
					try
					{
						Integer blLink = new Integer(bomLinkId);
						BOMLinkObject bomLinkObj = BOMLinkNut.getObject(blLink);
						if(bomLinkObj!=null && bomLinkObj.displayLogic.equals(DocRow.PSEUDO_LOGIC_REPLACE))
						{
							ess.setPseudoProperties(bomLinkObj.displayLogic, bomLinkObj.displayCode, bomLinkObj.displayName,
												bomLinkObj.displayDescription);
							ess.setUnitPrice(bomLinkObj.priceList);
						}
					}
					catch(Exception ex)
					{ }

				}

				////// check if there's membership campaign / promotion
				CustMembershipCampaignRulesObject rewardObj = CustMembershipCampaignEngine.getRewardRules(ess.getItemObject());
				if(rewardObj!=null)
				{ 
					ess.computeMembershipReward(); 
					Log.printVerbose(" checkpoint... rewardObj!=null!!");
				}
				else
				{
					Log.printVerbose(" checkpoint... rewardObj==null!!");
				}	

				
/*				Log.printVerbose("@@-Deciding whether to go serial or non-serial.-@@");
				if (ess.getSerialized())
				{ 
					Log.printVerbose("@@-I went serial.-@@");
					return new ActionRouter("trading-widget-sell-stock-serial-page"); } 
				else
				{ 
					Log.printVerbose("@@-I went non-serial.-@@");*/
					Log.printVerbose("autoClose value: " + (String) req.getParameter("autoClose"));
					return new ActionRouter("trading-widget-sell-sfa-stock-non-serial-page"); 
				//}
			} catch (Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
				ex.printStackTrace();
			}
		}
		Log.printVerbose("@@-I didn't go anywhere.-@@");
		return new ActionRouter("trading-widget-sell-sfa-stock-pagee");
	}

	protected void fnGetSetParams(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		
		Log.printVerbose("@@-Setting parameters.");
		Log.printVerbose("@@-Setting action.");
		String action = req.getParameter("action");
		req.setAttribute("action", action);
		
		Log.printVerbose("@@-Setting discFactorApplicableItem.");
		String discFactorApplicableItem = req.getParameter("discFactorApplicableItem");
		req.setAttribute("discFactorApplicableItem", discFactorApplicableItem);
		System.out.println("Inside fnGetSetParams - discFactorApplicableItem : "+discFactorApplicableItem);
		
		HttpSession session = req.getSession();
		Log.printVerbose("@@-Setting ess.");
		EnterStockSession ess = (EnterStockSession) session.getAttribute(action + EnterStockSession.OBJNAME);
		if (ess == null)
		{
			Log.printVerbose("@@-Setting userId.");
			Integer userId = (Integer) session.getAttribute("userId");
			ess = new EnterStockSession(userId);
			session.setAttribute(action + EnterStockSession.OBJNAME, ess);
			Log.printVerbose("@@-Setting essObject.");
			session.setAttribute(ess.getKey(),ess);
		}
		Log.printVerbose("@@-Setting pricingScheme.");
		String pricingScheme = req.getParameter("pricingScheme");
		if(pricingScheme!=null) 
		{ ess.setPricingScheme(pricingScheme);	}
		Log.printVerbose("@@-Setting factorDiscount.");
		String factorDiscount = req.getParameter("factorDiscount");
		if(factorDiscount!=null)
		{
			try
			{
				BigDecimal bdFactorDiscount = new BigDecimal(factorDiscount);
				ess.setFactorDiscount(bdFactorDiscount);
			}
			catch(Exception ex){ ex.printStackTrace();}
		}
		Log.printVerbose("@@-Setting branch.");
		String branch = req.getParameter("branch");
		if (branch != null)
		{
			// TKW20070416: Added this to maintain the branch parameter
			// when formName=selectCode.
			Log.printVerbose("Populating WidgetSellStockBranch.");
			session.setAttribute("WidgetSellStockBranch",branch);
			// End TKW20070416
			ess.setBranch(new Integer(branch));
		}
		else
		{
			Log.printVerbose("Branch from parameter was null.");
		}
		Log.printVerbose("@@-Setting criteria.");
		String criteria = req.getParameter("criteria");
		if (criteria != null)
		{
			req.setAttribute("criteria", criteria);
		}

		String salesmanId = req.getParameter("salesmanId");
		Log.printVerbose("@@-Setting salesmanId. " + salesmanId);
		if (salesmanId != null)
		{
			req.setAttribute("salesmanId", salesmanId);
			Log.printVerbose("@@-Set salesmanId. " + salesmanId);
		}		
		
		
		Log.printVerbose("@@-Setting nextFocusElement.");
		String focusElement = req.getParameter("nextFocusElement");
		if(focusElement!=null)
		{ req.setAttribute("focusElement",focusElement );}
		
		String disableCheckStockQtyInJobSheet = req.getParameter("disableCheckStockQtyInJobSheet");
		if(disableCheckStockQtyInJobSheet!=null)
		{
			try
			{
				String strTmp = ess.getDisableCheckStockInJobsheet();
				System.out.println("strTmp : "+strTmp);
				
				if("".equals(strTmp))
					ess.setDisableCheckStockInJobsheet(disableCheckStockQtyInJobSheet);
			}
			catch(Exception ex){ ex.printStackTrace();}
		}
		
		String strTmp = ess.getDisableCheckStockInJobsheet();
	}

	protected void fnGetSearchResults(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws Exception
	{
		Vector vecItemObj = null;
		String criteria = (String) req.getParameter("criteria");
		criteria = criteria.trim();
		QueryObject query = null;
		if (criteria != null && criteria.length() >= 1)
		{
			criteria = "%" + criteria + "%";
			query = new QueryObject(new String[] { ItemBean.ITEM_CODE + " ILIKE '" + criteria + "' " + " OR "
					+ ItemBean.NAME + " ILIKE '" + criteria + "' " + " OR " + ItemBean.DESCRIPTION + " ILIKE '"
					+ criteria + "' " + " OR " + ItemBean.EANCODE + " ILIKE '" + criteria + "' " + " OR "
					+ ItemBean.UPCCODE + " ILIKE '" + criteria + "' " + " OR " + ItemBean.ISBNCODE + " ILIKE '"
					+ criteria + "' " });
			query.setOrder(" ORDER BY " + ItemBean.ITEM_CODE);
			vecItemObj = new Vector(ItemNut.getObjects(query));
		} else
		{
			vecItemObj = new Vector();
		}
		if (vecItemObj == null)
		{
			throw new Exception("Unable to retrieve the supplier objects");
		}
		req.setAttribute("vecItemObj", vecItemObj);
		return;
	}
}
