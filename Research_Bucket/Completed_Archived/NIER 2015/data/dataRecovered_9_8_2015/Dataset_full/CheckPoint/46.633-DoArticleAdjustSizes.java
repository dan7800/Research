/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of Wavelet,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.servlet.footwear;

import java.math.*;
import java.util.*;
import javax.servlet.http.*;
import com.vlee.bean.footwear.*;
import com.vlee.ejb.inventory.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;

public class DoArticleAdjustSizes implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("footwear-article-adjust-sizes-page");
		}
	
		if(formName.equals("getStock"))
		{
			try{ fnGetStock(servlet,req,res); }
			catch(Exception ex)
			{
				req.setAttribute("errMsg", ex.getMessage());
			}
		}

		if(formName.equals("setQty"))
		{
			try
			{ fnSetQty(servlet,req,res);}
			catch(Exception ex)
			{ req.setAttribute("errMsg",ex.getMessage());}
		}

		if(formName.equals("confirmSave"))
		{
			try{ fnConfirmSave(servlet,req,res);}
			catch(Exception ex)
			{ req.setAttribute("errMsg",ex.getMessage());}
		}
 
		return new ActionRouter("footwear-article-adjust-sizes-page");
	}

	private void fnGetStock(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
		HttpSession session = req.getSession();
		ArticleAdjustSizesForm aasf = (ArticleAdjustSizesForm) session.getAttribute("footwear-article-adjust-sizes-form");
		if(aasf==null)
		{
      	Integer userId = (Integer) session.getAttribute("userId");
      	UserConfigObject ucObj = (UserConfigObject) session.getAttribute("ucObj");
      	if(ucObj!=null && ucObj.mDefaultCustSvcCtr!=null)
      	{
         	aasf.setBranch(ucObj.mDefaultCustSvcCtr);
      	}
			session.setAttribute("footwear-article-adjust-sizes-form",aasf);
   	}

		String article = req.getParameter("article");
		String branch = req.getParameter("branch");
		Log.printVerbose(" checkpoint 1... " );
		aasf.setPrefix(article);
		Log.printVerbose(" checkpoint 2... " );
		aasf.setBranch(new Integer(branch));
		Log.printVerbose(" checkpoint 3... " );
  		IsoPrefixRow isoPrefixRow = aasf.getIsoPrefixRow();
		ItemObject firstItem = isoPrefixRow.getFirstItemObject();
		if(firstItem==null)
		{
			Log.printVerbose("FIRST ITEM IS NULL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		else
		{
			Log.printVerbose("FIRST ITEM IS NOT NULL.. OK");
		}
	
	

	}

   private void fnSetQty(HttpServlet servlet,
                                    HttpServletRequest req,
                                    HttpServletResponse res)
      throws Exception
   {
      HttpSession session = req.getSession();
      ArticleAdjustSizesForm aasf = (ArticleAdjustSizesForm)
         session.getAttribute("footwear-article-adjust-sizes-form");

      IsoPrefixRow isoPrefix = aasf.getIsoPrefixRow();
      TreeMap treeItem = isoPrefix.getTreeItem();
      Vector vecItem = new Vector(treeItem.values());
      for(int cnt1=0;cnt1<vecItem.size();cnt1++)
      {
         IsoPrefixRow.PerItemCode pItmCode = (IsoPrefixRow.PerItemCode) vecItem.get(cnt1);
         String itmQty = req.getParameter("qty_"+pItmCode.getItemObject().code);
         if(itmQty!=null)
         {
            try
            {
               BigDecimal bdQty = new BigDecimal(itmQty);
               pItmCode.setQty(bdQty);
            }
            catch(Exception ex)
            { ex.printStackTrace(); }
         }
      }

   }

	private synchronized void fnConfirmSave(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
				throws Exception
	{
      HttpSession session = req.getSession();
      ArticleAdjustSizesForm aasf = (ArticleAdjustSizesForm)
         session.getAttribute("footwear-article-adjust-sizes-form");
	
		String theDate = req.getParameter("theDate");
		String remarks = req.getParameter("remarks");
		aasf.setDate(theDate);
		aasf.setRemarks(remarks);
		try
		{
			aasf.confirmSave();
			req.setAttribute("errMsg", "Successfully adjusted the sizes!");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Adjustment was not successful, please consult the system administrator...");
		}
	}

}





