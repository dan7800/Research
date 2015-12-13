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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vlee.util.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.ecommerce.EHomepageNut;
import com.vlee.ejb.ecommerce.EHomepageObject;
import com.vlee.ejb.ecommerce.EProAddOn;
import com.vlee.ejb.ecommerce.EProAddOnBean;
import com.vlee.ejb.ecommerce.EProAddOnNut;
import com.vlee.ejb.ecommerce.EProAddOnObject;
import com.vlee.ejb.ecommerce.EProOption;
import com.vlee.ejb.ecommerce.EProOptionBean;
import com.vlee.ejb.ecommerce.EProOptionNut;
import com.vlee.ejb.ecommerce.EProOptionObject;
import com.vlee.ejb.ecommerce.EProduct;
import com.vlee.ejb.ecommerce.EProductBean;
import com.vlee.ejb.ecommerce.EProductNut;
import com.vlee.ejb.ecommerce.EProductObject;
import com.vlee.ejb.inventory.BOMNut;
import com.vlee.ejb.inventory.BOMObject;
import com.vlee.ejb.inventory.ItemNut;
import com.vlee.ejb.inventory.ItemObject;
import com.vlee.servlet.main.Action;
import com.vlee.servlet.main.ActionDo;
import com.vlee.servlet.main.ActionRouter;
import com.vlee.util.Log;
import com.vlee.util.QueryObject;
import com.vlee.util.TimeFormat;

public class DoEProductMaintenance extends ActionDo implements Action
{
	String theCategory = "0";
	String tempStr = "";
	String searchOption = "";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		System.out.println("formName>>>>>>>>>>>>>>>>>>>>>" + formName);
		if (formName == null)
		{
			return new ActionRouter("ecommerce-product-listing-page");
		}

		if (formName.equals("New"))
		{
			EProductObject objProduct = new EProductObject();
			tempStr = req.getParameter("productCode");
			if (tempStr != null)
				objProduct.code = tempStr;
			tempStr = req.getParameter("productName");
			if (tempStr != null)
				objProduct.name = tempStr;
			tempStr = req.getParameter("productShortDescription");
			if (tempStr != null)
				objProduct.short_description = tempStr;
			tempStr = req.getParameter("productLongDescription");
			if (tempStr != null)
				objProduct.description = tempStr;
			tempStr = req.getParameter("productKeywords");
			if (tempStr != null)
				objProduct.keywords = tempStr;
			tempStr = req.getParameter("imageLarge");
			if (tempStr != null)
				objProduct.img_large = new Long(tempStr);
			else
				objProduct.img_large = new Long(0);
			tempStr = req.getParameter("imageMed");
			if (tempStr != null)
				objProduct.img_medium = new Long(tempStr);
			else
				objProduct.img_medium = new Long(0);
			tempStr = req.getParameter("imageThumb");
			if (tempStr != null)
				objProduct.img_thumbnail = new Long(tempStr);
			else
				objProduct.img_thumbnail = new Long(0);
			objProduct.local = (req.getParameter("local") != null);
			objProduct.visible = (req.getParameter("productVisible") != null);

			String imageIcon = "";
			tempStr = req.getParameter("imageIcon");
			if (tempStr != null) imageIcon = tempStr;

			if(!"".equals(imageIcon))
				imageIcon = "," + imageIcon;

			String icon = "";
			tempStr = req.getParameter("icon");
			if (tempStr != null) icon = tempStr;

			objProduct.icon = icon + imageIcon;

			req.setAttribute("objProduct", objProduct);

			String[] categories = (String[]) req.getParameterValues("categories");
			if (categories != null)
			{
				Vector vecSelectedCat = new Vector();
				for (int i = 0; i < categories.length; i++)
				{
					vecSelectedCat.add((String) categories[i]);
				}
				req.setAttribute("vecSelectedCat", vecSelectedCat);
			}
			String[] countries = (String[]) req.getParameterValues("countries");
			if (countries != null)
			{
				Vector vecSelectedCtry = new Vector();
				for (int i = 0; i < countries.length; i++)
				{
					vecSelectedCtry.add((String) countries[i]);
				}
				req.setAttribute("vecSelectedCtry", vecSelectedCtry);
			}
			req.setAttribute("imageParam", (String) req.getParameter("imageParam"));
			req.setAttribute("imageName", (String) req.getParameter("imageName"));
			req.setAttribute("imageHeight", (String) req.getParameter("imageHeight"));
			req.setAttribute("imageWeight", (String) req.getParameter("imageWeight"));
			req.setAttribute("formName", formName);
			return new ActionRouter("ecommerce-product-detail-page");
		} 
		else if (formName.equals("NewProduct"))
		{
			try
			{
				fnAdd(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		} 
		else if (formName.equals("Edit"))
		{
			String strPkid = (String) req.getParameter("productPkid");
			if (strPkid == null)
			{
				Log.printDebug("Null Product Pkid");
				return new ActionRouter("ecommerce-product-detail-page");
			}
			String imageParam = "";
			tempStr = (String) req.getParameter("imageParam");
			if (tempStr != null)imageParam = tempStr;

			if (!imageParam.equals(""))
			{
				EProductObject objProduct = new EProductObject();
				tempStr = req.getParameter("productPkid");
				if (tempStr != null)
					objProduct.pkid = new Long(tempStr);
				tempStr = req.getParameter("productCode");
				if (tempStr != null)
					objProduct.code = tempStr;
				tempStr = req.getParameter("productName");
				if (tempStr != null)
					objProduct.name = tempStr;
				tempStr = req.getParameter("productShortDescription");
				if (tempStr != null)
					objProduct.short_description = tempStr;
				tempStr = req.getParameter("productLongDescription");
				if (tempStr != null)
					objProduct.description = tempStr;
				tempStr = req.getParameter("productKeywords");
				if (tempStr != null)
					objProduct.keywords = tempStr;
				tempStr = req.getParameter("imageLarge");
				if (tempStr != null)
					objProduct.img_large = new Long(tempStr);
				else
					objProduct.img_large = new Long(0);
				tempStr = req.getParameter("imageMed");
				if (tempStr != null)
					objProduct.img_medium = new Long(tempStr);
				else
					objProduct.img_medium = new Long(0);
				tempStr = req.getParameter("imageThumb");
				if (tempStr != null)
					objProduct.img_thumbnail = new Long(tempStr);
				else
					objProduct.img_thumbnail = new Long(0);
				
				objProduct.local = (req.getParameter("local") != null);
				objProduct.visible = (req.getParameter("productVisible") != null);
		
				String imageIcon = "";
				tempStr = req.getParameter("imageIcon");
				if (tempStr != null) imageIcon = tempStr;

				if(!"".equals(imageIcon))
					imageIcon = "," + imageIcon;

				String icon = "";
				tempStr = req.getParameter("icon");
				if (tempStr != null) icon = tempStr;

				objProduct.icon = icon + imageIcon;				

				req.setAttribute("objProduct", objProduct);
				
				String[] categories = (String[]) req.getParameterValues("categories");
				Vector vecSelectedCat = new Vector();
				if(categories != null)
				{
					for (int i = 0; i < categories.length; i++)
						vecSelectedCat.add((String) categories[i]);
				}				
				req.setAttribute("vecSelectedCat", vecSelectedCat);
				
				String[] countries = (String[]) req.getParameterValues("countries");
				Vector vecSelectedCtry = new Vector();
				if(countries!=null)
				{
					for (int i = 0; i < countries.length; i++)
						vecSelectedCtry.add((String) countries[i]);
				}
				req.setAttribute("vecSelectedCtry", vecSelectedCtry);
			} 
			else
			{
				EProductObject objProduct = EProductNut.getObject(new Long(strPkid));
				if (objProduct != null)
				{
					req.setAttribute("objProduct", objProduct);
				}
				
				Vector vecSelectedCat = (Vector) EProductNut.selectedCategoryList(objProduct.pkid);
				req.setAttribute("vecSelectedCat", vecSelectedCat);
				Vector vecSelectedCtry = (Vector) EProductNut.selectedCountryList(objProduct.pkid);
				req.setAttribute("vecSelectedCtry", vecSelectedCtry);
			}

			req.setAttribute("imageName", (String) req.getParameter("imageName"));
			req.setAttribute("imageHeight", (String) req.getParameter("imageHeight"));
			req.setAttribute("imageWeight", (String) req.getParameter("imageWeight"));
			req.setAttribute("formName", formName);
			return new ActionRouter("ecommerce-product-detail-page");

		}
		else if (formName.equals("EditProduct"))
		{
			try
			{
				fnEdit(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		} else if (formName.equals("deleteProduct"))
		{
			try
			{
				fnDelete(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}

		} else if (formName.equals("move_up"))
		{
			try
			{
				fnMoveUp(servlet, req, res);
				fnGetList(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		} else if (formName.equals("move_down"))
		{
			try
			{
				fnMoveDown(servlet, req, res);
				fnGetList(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		} 
		else if (formName.equals("getList"))
		{
			fnGetList(servlet, req, res);
		} 
		else if (formName.equals("assignOptions"))
		{
			try
			{
				EProOptionObject objProOption = new EProOptionObject();
				req.setAttribute("objProOption", objProOption);
				fnAssignOption(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-options-page");
		} 
		else if (formName.equals("editOption"))
		{
			try
			{
				String strPkid = (String) req.getParameter("optionPkid");
				if (strPkid == null)
				{
					Log.printDebug("Null Product Option Pkid");
					return new ActionRouter("ecommerce-product-listing-page");
				}

				EProOptionObject objProOption = EProOptionNut.getObject(new Long(strPkid));
				ItemObject itemObj = ItemNut.getObject(new Integer(objProOption.invid.toString()));
				objProOption.invcode = itemObj.code;
				//objProOption.invprice = itemObj.priceList;
				objProOption.invname = itemObj.name;
				objProOption.invprice = itemObj.priceEcom;

				req.setAttribute("objProOption", objProOption);
				fnAssignOption(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-options-page");
		} 
		else if (formName.equals("saveOptions"))
		{
			try
			{
				fnSaveOption(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-options-page");
		} 
		else if (formName.equals("deleteOption"))
		{
			try
			{
				String strPkid = (String) req.getParameter("optionPkid");
				if (strPkid == null)
				{
					Log.printDebug("Null Product Option Pkid");
					return new ActionRouter("ecommerce-product-listing-page");
				}
				Long pkid = new Long(strPkid);
				EProOption stEJB = EProOptionNut.getHandle(pkid);
				stEJB.remove();
				EProOptionObject objProOption = new EProOptionObject();
				req.setAttribute("objProOption", objProOption);
				fnAssignOption(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-options-page");
		} 
		else if (formName.equals("moveOptions_up"))
		{
			try
			{
				fnMoveOptionUp(servlet, req, res);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-options-page");
		} 
		else if (formName.equals("moveOptions_down"))
		{
			try
			{
				fnMoveOptionDown(servlet, req, res);

			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-options-page");
		}
		else if (formName.equals("assignAddOns"))
		{
			try
			{
				EProAddOnObject objAddOn = new EProAddOnObject();

				String imageParam = "";
				tempStr = (String) req.getParameter("imageParam");
				if (tempStr != null)imageParam = tempStr;

				if (!imageParam.equals(""))
				{
					objAddOn.code = req.getParameter("addOnCode");
					objAddOn.title = req.getParameter("invName");
					objAddOn.productId = new Long(req.getParameter("thePkid"));
					objAddOn.visible = (req.getParameter("addOnVisible") != null);
					objAddOn.invid = new Long(req.getParameter("invPkid"));
					objAddOn.invcode = req.getParameter("invCode");
					objAddOn.pkid = new Long(req.getParameter("addOnPkid"));
					
					objAddOn.disc_description = req.getParameter("disc_description");

					tempStr = req.getParameter("disc_value");
					if (tempStr != null) 
						objAddOn.disc_value = new BigDecimal(tempStr);

					String dateStart = req.getParameter("disc_date_start");
					if(dateStart != null)
						objAddOn.disc_date_start = TimeFormat.createTimestamp(dateStart);

					String dateEnd = req.getParameter("disc_date_end");
					if(dateEnd != null)
						objAddOn.disc_date_end = TimeFormat.createTimestamp(dateEnd);


					tempStr = req.getParameter("imageThumb");
					if (tempStr != null)
						objAddOn.img_thumbnail = new Long(tempStr);
					else
						objAddOn.img_thumbnail = new Long(0);

					req.setAttribute("imageParam", (String) req.getParameter("imageParam"));
					req.setAttribute("imageName", (String) req.getParameter("imageName"));
					req.setAttribute("imageHeight", (String) req.getParameter("imageHeight"));
					req.setAttribute("imageWeight", (String) req.getParameter("imageWeight"));
					req.setAttribute("formName", formName);
				}

				req.setAttribute("objAddOn", objAddOn);

				fnAssignAddOn(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-addons-page");
		} 
		else if (formName.equals("editAddOn"))
		{
			try
			{
				String strPkid = (String) req.getParameter("addOnPkid");
				if (strPkid == null)
				{
					Log.printDebug("Null Product Add-On Pkid");
					return new ActionRouter("ecommerce-product-listing-page");
				}
				EProAddOnObject objAddOn = EProAddOnNut.getObject(new Long(strPkid));
				ItemObject itemObj = ItemNut.getObject(new Integer(objAddOn.invid.toString()));
				objAddOn.invcode = itemObj.code;
				//objAddOn.invprice = itemObj.priceList;
				objAddOn.invprice = itemObj.priceEcom;
				objAddOn.invname = itemObj.name;
				req.setAttribute("objAddOn", objAddOn);
				fnAssignAddOn(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-addons-page");
		} 
		else if (formName.equals("saveAddOn"))
		{
			try
			{
				fnSaveAddOn(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-addons-page");
		} 
		else if (formName.equals("deleteAddOn"))
		{
			try
			{
				String strPkid = (String) req.getParameter("addOnPkid");
				if (strPkid == null)
				{
					Log.printDebug("Null Product Add-On Pkid");
					return new ActionRouter("ecommerce-product-listing-page");
				}
				Long pkid = new Long(strPkid);
				EProAddOn stEJB = EProAddOnNut.getHandle(pkid);
				stEJB.remove();
				EProAddOnObject objAddOn = new EProAddOnObject();
				req.setAttribute("objAddOn", objAddOn);
				fnAssignAddOn(servlet, req, res);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return new ActionRouter("ecommerce-product-addons-page");
		}


		return new ActionRouter("ecommerce-product-listing-page");
	}

	private void fnSaveOption(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		EProOptionObject optionObj = new EProOptionObject();
		optionObj.title = req.getParameter("invName");
		if (optionObj.title == null)
		{
			Log.printDebug("Null Product Option Title");
			return;
		}
		optionObj.productid = new Long(req.getParameter("thePkid"));
		optionObj.visible = (req.getParameter("optionVisible") != null);
		optionObj.invid = new Long(req.getParameter("invPkid"));
		optionObj.invtype = req.getParameter("invType");
		optionObj.pkid = new Long(req.getParameter("optionPkid"));
		if (optionObj.pkid != null && optionObj.pkid.intValue() != 0)
		{
			try
			{
				EProOption stEJB = EProOptionNut.getHandle(optionObj.pkid);
				stEJB.setObject(optionObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		} else
		{
			EProOptionNut.fnCreate(optionObj);
		}

		ItemObject itemObj = ItemNut.getObject(new Integer(optionObj.invid.toString()));
		BigDecimal priceEcom = itemObj.priceEcom;
		itemObj.priceEcom = new BigDecimal(req.getParameter("priceEcom"));
		try
		{
			HttpSession session = req.getSession(true);
			itemObj.userIdUpdate = (Integer) session.getAttribute("userId");
			Item itemEJB = ItemNut.getHandle(new Integer(optionObj.invid.toString()));
			itemEJB.setObject(itemObj);

			// // audit trail!!
			boolean criticalChange = false;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = itemObj.userIdUpdate;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: change-item-code (" + itemObj.code + ") ";
			if (priceEcom.compareTo(itemObj.priceEcom) != 0)
			{
				criticalChange = true;
				atObj.remarks += " Ecommerce Price:" + CurrencyFormat.strCcy(priceEcom) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceEcom) + " ";
			}
			if (criticalChange)
			{
				AuditTrailNut.fnCreate(atObj);
			}

		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		EProOptionObject objProOption = new EProOptionObject();
		req.setAttribute("objProOption", objProOption);
		fnAssignOption(servlet, req, res);
	}

	private void fnAssignOption(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strPkid = req.getParameter("thePkid");
		req.setAttribute("thePkid", strPkid);
		String strName = req.getParameter("theName");
		req.setAttribute("theName", strName);
		QueryObject query = new QueryObject(new String[] { EProOptionBean.PRODUCT_ID + " = " + strPkid + " " });
		query.setOrder(" ORDER BY " + EProOptionBean.SORT + ", " + EProOptionBean.TITLE);
		Vector vecOptions = new Vector(EProOptionNut.getObjects(query));
		req.setAttribute("vecOptions", vecOptions);
	}

	private void fnMoveOptionUp(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strPkid = req.getParameter("optionPkid");
		String strSort = req.getParameter("sort");
		String thePkid = req.getParameter("thePkid");
		req.setAttribute("thePkid", thePkid);
		try
		{
			EProOptionNut.fnMoveUp(new Long(strPkid), Integer.parseInt(strSort), new Long(thePkid));
			EProOptionObject objProOption = new EProOptionObject();
			req.setAttribute("objProOption", objProOption);
			fnAssignOption(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnMoveOptionDown(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strPkid = req.getParameter("optionPkid");
		String strSort = req.getParameter("sort");
		String thePkid = req.getParameter("thePkid");
		req.setAttribute("thePkid", thePkid);
		try
		{
			EProOptionNut.fnMoveDown(new Long(strPkid), Integer.parseInt(strSort), new Long(thePkid));
			EProOptionObject objProOption = new EProOptionObject();
			req.setAttribute("objProOption", objProOption);
			fnAssignOption(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnMoveUp(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strPkid = req.getParameter("pkid");
		String strSort = req.getParameter("sort");
		HttpSession session = req.getSession();
		String strCategory = (String) session.getAttribute("searchKeyword");
		try
		{
			EProductNut.fnMoveUp(new Long(strPkid), Integer.parseInt(strSort), new Long(strCategory));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void fnMoveDown(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strPkid = req.getParameter("pkid");
		String strSort = req.getParameter("sort");
		HttpSession session = req.getSession();
		String strCategory = (String) session.getAttribute("searchKeyword");
		try
		{
			EProductNut.fnMoveDown(new Long(strPkid), Integer.parseInt(strSort), new Long(strCategory));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void fnGetList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		tempStr = req.getParameter("searchOption");
		if (tempStr != null)
			searchOption = tempStr;
		else
		{
			tempStr = (String) session.getAttribute("searchOption");
			if (tempStr != null)
				searchOption = tempStr;
		}
		String searchKeyword = "";
		if (searchOption.equals("category"))
		{
			tempStr = req.getParameter("theCategory");
			if (tempStr != null)
				searchKeyword = tempStr;
			else
				searchKeyword = (String) session.getAttribute("searchKeyword");

			if(searchKeyword != null && !searchKeyword.equals(""))
			{
				Vector vecProduct = new Vector(EProductNut.ProductsCategoryList(new Long(searchKeyword), ""));
				req.setAttribute("vecProduct", vecProduct);
			}

		} else if (searchOption.equals("featured"))
		{
			Vector vecProduct = new Vector();
			List listing = null;
			StringTokenizer tokenizer = null;
			EHomepageObject objList = new EHomepageObject();
			listing = new ArrayList();
			objList = EHomepageNut.getListSection("1");
			objList = EHomepageNut.getListSection("2");
			String tmp = objList.list1_itemid + objList.list2_itemid;
			tokenizer = new StringTokenizer(tmp, ",");
			while (tokenizer.hasMoreTokens())
			{
				listing.add(tokenizer.nextToken());
			}
			for (Iterator i = listing.iterator(); i.hasNext();)
			{
				String strPkid = (String) i.next();
				EProductObject objProduct = EProductNut.getObject(new Long(strPkid));
				vecProduct.add(objProduct);
			}
			req.setAttribute("vecProduct", vecProduct);
		} 
		else if (searchOption.equals("product_name"))
		{
			tempStr = req.getParameter("keyword");
			if (tempStr != null)
				searchKeyword = tempStr;
			else
				searchKeyword = (String) session.getAttribute("searchKeyword");

			QueryObject query = new QueryObject(new String[] { " lower(" + EProductBean.NAME + ") LIKE lower('%"
					+ searchKeyword + "%') " });
			query.setOrder(" ORDER BY " + EProductBean.NAME);
			Vector vecProduct = new Vector(EProductNut.getObjects(query));
			req.setAttribute("vecProduct", vecProduct);
		} 
		else if (searchOption.equals("product_code"))
		{
			tempStr = req.getParameter("product_code");
			if (tempStr != null)
				searchKeyword = tempStr;
			else
				searchKeyword = (String) session.getAttribute("searchKeyword");
			QueryObject query = new QueryObject(new String[] { EProductBean.CODE + " = '" + searchKeyword + "' " });
			query.setOrder(" ORDER BY " + EProductBean.NAME);
			Vector vecProduct = new Vector(EProductNut.getObjects(query));
			req.setAttribute("vecProduct", vecProduct);
		} 
		else if (searchOption.equals("visibility"))
		{
			tempStr = req.getParameter("visibility");
			if (tempStr != null)
				searchKeyword = tempStr;
			else
				searchKeyword = (String) session.getAttribute("searchKeyword");
			QueryObject query = new QueryObject(new String[] { EProductBean.VISIBLE + " = " + searchKeyword + " " });
			query.setOrder(" ORDER BY " + EProductBean.NAME);
			Vector vecProduct = new Vector(EProductNut.getObjects(query));
			req.setAttribute("vecProduct", vecProduct);
		} 
		else if (searchOption.equals("all"))
		{
			QueryObject query = new QueryObject(new String[] { "" });
			query.setOrder(" ORDER BY " + EProductBean.NAME);
			Vector vecProduct = new Vector(EProductNut.getObjects(query));
			req.setAttribute("vecProduct", vecProduct);
		}
		session.setAttribute("searchOption", searchOption);
		session.setAttribute("searchKeyword", searchKeyword);
	}

	public void fnAdd(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		HttpSession session = req.getSession();
		EProductObject productObj = new EProductObject();
		productObj.name = req.getParameter("productName");
		if (productObj.name == null)
		{
			Log.printDebug("Null Product Name");
			return;
		}
		productObj.code = req.getParameter("productCode");
		productObj.short_description = req.getParameter("productShortDescription");
		productObj.description = req.getParameter("productLongDescription");
		productObj.keywords = req.getParameter("productKeywords");
		productObj.local = (req.getParameter("local") != null);
		productObj.visible = (req.getParameter("productVisible") != null);
		productObj.img_thumbnail = new Long(req.getParameter("imageThumb"));
		productObj.img_medium = new Long(req.getParameter("imageMed"));
		productObj.img_large = new Long(req.getParameter("imageLarge"));
		productObj.icon = req.getParameter("icon");

		EProductNut.fnCreate(productObj);

		String[] categories = (String[]) req.getParameterValues("categories");
		if (categories != null && categories.length > 0)
			EProductNut.insertProductCategory(categories, productObj.pkid, 0);

		String[] countries = (String[]) req.getParameterValues("countries");
		if (countries != null && countries.length > 0)
			EProductNut.insertProductCountry(countries, productObj.pkid);

		session.setAttribute("searchOption", "product_name");
		session.setAttribute("keyword", productObj.name);

		fnGetList(servlet, req, res);
	}

	private void fnEdit(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			EProductObject productObj = new EProductObject();
			productObj.name = req.getParameter("productName");
			if (productObj.name == null)
			{
				Log.printDebug("Null Product Name");
				return;
			}
			productObj.pkid = new Long(req.getParameter("productPkid"));
			productObj.code = req.getParameter("productCode");
			productObj.short_description = req.getParameter("productShortDescription");
			productObj.description = req.getParameter("productLongDescription");
			productObj.keywords = req.getParameter("productKeywords");
			productObj.local = (req.getParameter("local") != null);
			productObj.visible = (req.getParameter("productVisible") != null);
			productObj.img_thumbnail = new Long(req.getParameter("imageThumb"));
			productObj.img_medium = new Long(req.getParameter("imageMed"));
			productObj.img_large = new Long(req.getParameter("imageLarge"));

			String icon = "";
			tempStr = req.getParameter("icon");
			if (tempStr != null) icon = tempStr;

			productObj.icon = icon;

			EProduct stEJB = EProductNut.getHandle(productObj.pkid);
			stEJB.setObject(productObj);

			String[] categories = (String[]) req.getParameterValues("categories");
			if(categories != null && categories.length > 0)
				EProductNut.insertProductCategory(categories, productObj.pkid, productObj.sort);

			String[] countries = (String[]) req.getParameterValues("countries");
			if(countries != null && countries.length > 0)
				EProductNut.insertProductCountry(countries, productObj.pkid);

			fnGetList(servlet, req, res);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}


	private void fnAssignAddOn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strPkid = req.getParameter("thePkid");
		req.setAttribute("thePkid", strPkid);
		String strName = req.getParameter("theName");
		req.setAttribute("theName", strName);
		QueryObject query = new QueryObject(new String[] { EProAddOnBean.PRODUCT_ID + " = " + strPkid + " " });
		Vector vecAddOn = new Vector(EProAddOnNut.getObjects(query));
		req.setAttribute("vecAddOn", vecAddOn);
	}

	private void fnSaveAddOn(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		EProAddOnObject addOnObj = new EProAddOnObject();
		addOnObj.title = req.getParameter("invName");
		if (addOnObj.title == null)
		{
			Log.printDebug("Null Product Add-On Title");
			return;
		}
		addOnObj.code = req.getParameter("addOnCode");
		addOnObj.productId = new Long(req.getParameter("thePkid"));
		addOnObj.visible = (req.getParameter("addOnVisible") != null);
		addOnObj.invid = new Long(req.getParameter("invPkid"));
		addOnObj.pkid = new Long(req.getParameter("addOnPkid"));
		addOnObj.disc_description = req.getParameter("disc_description");

		tempStr = req.getParameter("disc_value");
		if (tempStr != null && tempStr.length() > 0) 
			addOnObj.disc_value = new BigDecimal(tempStr);

		String dateStart = req.getParameter("disc_date_start");		
		System.out.println("dateStart"+dateStart);
		
		if(dateStart != null && dateStart.length() > 0)
			addOnObj.disc_date_start = TimeFormat.createTimestamp(dateStart);

		String dateEnd = req.getParameter("disc_date_end");
		System.out.println("dateEnd"+dateEnd);
		
		if(dateEnd != null && dateEnd.length() > 0)
			addOnObj.disc_date_end = TimeFormat.createTimestamp(dateEnd);

		tempStr = req.getParameter("imageThumb");
		if (tempStr != null)
			addOnObj.img_thumbnail = new Long(tempStr);
		else
			addOnObj.img_thumbnail = new Long(0);

		if (addOnObj.pkid != null && addOnObj.pkid.intValue() != 0)
		{
			try
			{
				EProAddOn stEJB = EProAddOnNut.getHandle(addOnObj.pkid);
				stEJB.setObject(addOnObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		} else
		{
			EProAddOnNut.fnCreate(addOnObj);
		}

		ItemObject itemObj = ItemNut.getObject(new Integer(addOnObj.invid.toString()));
		BigDecimal priceEcom = itemObj.priceEcom;
		itemObj.priceEcom = new BigDecimal(req.getParameter("priceEcom"));
		try
		{
			Item itemEJB = ItemNut.getHandle(new Integer(addOnObj.invid.toString()));
			itemEJB.setObject(itemObj);

			// // audit trail!!
			boolean criticalChange = false;
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.userId = itemObj.userIdUpdate;
			atObj.auditType = AuditTrailBean.TYPE_CONFIG;
			atObj.time = TimeFormat.getTimestamp();
			atObj.remarks = "config: " + itemObj.code + " ";
			if (priceEcom.compareTo(itemObj.priceEcom) != 0)
			{
				criticalChange = true;
				atObj.remarks += " EcommercePrice:" + CurrencyFormat.strCcy(priceEcom) + "->"
						+ CurrencyFormat.strCcy(itemObj.priceEcom) + " ";
			}
			if (criticalChange)
			{
				AuditTrailNut.fnCreate(atObj);
			}

		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		EProAddOnObject objAddOn = new EProAddOnObject();
		req.setAttribute("objAddOn", objAddOn);
		fnAssignAddOn(servlet, req, res);
	}

	private void fnDelete(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String strPkid = req.getParameter("pkid");
		try
		{
			QueryObject query = new QueryObject(new String[] { EProOptionBean.PRODUCT_ID + " = " + strPkid + " " });
			Vector vecOptions = new Vector(EProOptionNut.getObjects(query));
			for(int cnt1=0;cnt1<vecOptions.size();cnt1++)
			{
				EProOptionObject stObj = (EProOptionObject) vecOptions.get(cnt1);
				EProOption stEJBOption = EProOptionNut.getHandle(stObj.pkid);
				stEJBOption.remove();
			}
			
			query = new QueryObject(new String[] { EProAddOnBean.PRODUCT_ID + " = " + strPkid + " " });
			Vector vecAddOn = new Vector(EProAddOnNut.getObjects(query));
			for(int cnt2=0;cnt2<vecAddOn.size();cnt2++)
			{
				EProAddOnObject stObj = (EProAddOnObject) vecAddOn.get(cnt2);
				EProAddOn stEJBAddOn = EProAddOnNut.getHandle(stObj.pkid);
				stEJBAddOn.remove();
			}


		
			Long pkid = new Long(strPkid);
			EProduct stEJB = EProductNut.getHandle(pkid);
			stEJB.remove();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}


}
