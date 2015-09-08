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

import java.io.*;
import java.math.*;
import java.util.*;
import java.sql.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoUploadCatItem implements Action // extends HttpServlet
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req,
								HttpServletResponse res) 
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("footwear-upload-cat-item-page");
		} 
		else
		{
			try
			{
				fnProcessInputFiles(req, res);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return new ActionRouter("footwear-upload-cat-item-page");
	}

	private void fnProcessInputFiles(HttpServletRequest req, HttpServletResponse res) 
					throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload fu = new ServletFileUpload(factory);
		fu.setSizeMax(50000000);
		List fileItems = fu.parseRequest(req);
		Iterator itr = fileItems.iterator();
		while (itr.hasNext())
		{
			FileItem fi = (FileItem) itr.next();
			if (!fi.isFormField() && fi.getFieldName().equals("categoryfile"))
			{
				fnProcessCategoryFile(fi);
			}/// end if categoryfile

 
			if (!fi.isFormField() && fi.getFieldName().equals("itemfile"))
			{
				fnProcessItemFile(fi);
			}// end if itemfile

			if (!fi.isFormField() && fi.getFieldName().equals("liquidatingItems"))
			{
				fnProcessLiquidationFile(fi, userId, res);
			}// end if liquidatingItems

			if(!fi.isFormField() && fi.getFieldName().equals("createPurchaseOrder"))
			{
				fnCreatePurchaseOrder(fi,userId);
			}// end if createPurchaseOrder

			if(!fi.isFormField() && fi.getFieldName().equals("markdownPrices"))
			{
				fnMarkdownPrices(fi,userId);
			}// end if createPurchaseOrder

		}/// END WHILE itr.hasNext()
	}// END fnUploadItem


	private void fnProcessCategoryFile(FileItem fileItem)
		throws Exception
	{
					InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
					BufferedReader in = new BufferedReader(inputStreamReader);
					LineNumberReader ln = new LineNumberReader(in);
					String line;
					while ((line = ln.readLine()) != null)
					{
						try
						{
							StringTokenizer sb = new StringTokenizer(line, "|");
							CategoryTreeObject obj = new CategoryTreeObject();
							sb.nextToken();
							obj.code = sb.nextToken().trim();
							obj.name = sb.nextToken().trim();
							obj.sort = "0";
							obj.description = sb.nextToken().trim();
							if (obj.code.length() < 4)
								obj.catLevel = new Integer(2);
							else
								obj.catLevel = new Integer(3);
							CategoryTreeHome cattreehome = CategoryTreeNut.getHome();
							QueryObject query = new QueryObject(new String[] { CategoryTreeBean.CODE + " = '" + obj.code + "' " });
							Collection coll = CategoryTreeNut.getObjects(query);
							if (coll.isEmpty() || coll.size() == 0)
							{
								cattreehome.create(obj);
							} else
							{
								CategoryTreeObject cattree = (CategoryTreeObject) coll.iterator().next();
								CategoryTree categoryEJB = CategoryTreeNut.getHandle(cattree.pkid);
								obj.pkid = cattree.pkid;
								obj.parentId = cattree.parentId;
								obj.sort = cattree.sort;
								categoryEJB.setObject(obj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}


	}

	private void fnProcessItemFile(FileItem fileItem)
		throws Exception
	{
		///item code(0)	| item name	(1) | Item description (2) | 	uom (3)	| cat code (4) 
///			|		w/s price (5) | 	retail price (6) | 	min price (7) |	cost price (8) 



					InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
					BufferedReader in = new BufferedReader(inputStreamReader);
					LineNumberReader ln = new LineNumberReader(in);
					String line;
					while ((line = ln.readLine()) != null)
					{
						try
						{
							ItemObject itmObj = new ItemObject();
							String token[] = line.split("[|]");

							itmObj.code = token[0].trim();
							itmObj.name = token[1].trim();
							itmObj.description = token[2].trim();
							itmObj.uom = token[3].trim();

							String categorycode = token[4].trim();

							BigDecimal wholesalePrice = new BigDecimal(token[5].trim());
							wholesalePrice = wholesalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal retailPrice = new BigDecimal(token[6].trim());
							retailPrice = retailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal minPrice = new BigDecimal(token[7].trim());
							minPrice = minPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal costPrice = new BigDecimal(token[8].trim());
							costPrice = costPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

							itmObj.categoryId = new Integer(0);
							itmObj.category2 = categorycode.substring(0, 2);
							itmObj.category3 = categorycode;
							if(new Integer(categorycode).intValue() >= new Integer("0100").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("0999").intValue())
							{ itmObj.category1 = "MEN"; itmObj.categoryId = new Integer(1001);}
							else if(new Integer(categorycode).intValue() >= new Integer("1000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("1999").intValue())
							{ itmObj.category1 = "LADIES";itmObj.categoryId = new Integer(1002);}
							else if(new Integer(categorycode).intValue() >= new Integer("2000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("2999").intValue())
							{ itmObj.category1 = "CHILDREN"; itmObj.categoryId = new Integer(1003);}
							else if(new Integer(categorycode).intValue() >= new Integer("3000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("3300").intValue())
							{ itmObj.category1 = "CANVAS"; itmObj.categoryId = new Integer(1004);}
							else if(new Integer(categorycode).intValue() >= new Integer("3300").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("3999").intValue())
							{ itmObj.category1 = "ATHLETIC"; itmObj.categoryId = new Integer(1005);}
							else if(new Integer(categorycode).intValue() >= new Integer("4000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("4999").intValue())
							{ itmObj.category1 = "OTHERS"; itmObj.categoryId = new Integer(1006);}
							else if(new Integer(categorycode).intValue() >= new Integer("5000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("6999").intValue())
							{ itmObj.category1 = "NON-FOOTWEAR"; itmObj.categoryId = new Integer(1007);}
						
							if(itmObj.category1.equals("NON-FOOTWEAR"))
							{ itmObj.category4 = "NON-FOOTWEAR"; }
							else { itmObj.category4 = "FOOTWEAR";}
							itmObj.category5 = "";

							itmObj.status = ItemBean.STATUS_ACTIVE;
							itmObj.priceList = retailPrice;
							itmObj.priceSale = wholesalePrice;
							itmObj.priceMin = minPrice;
							itmObj.replacementUnitCost = costPrice;

							Item item = ItemNut.getObjectByCode(itmObj.code);

							if(item==null)
							{
								item = ItemNut.fnCreate(itmObj);
							} 
							else 
							{
								//ItemObject item = (ItemObject) coll.iterator().next();
								ItemObject itmObj2 = item.getObject();
								itmObj2.name = itmObj.name;
								itmObj2.description = itmObj.description;
								itmObj2.uom = itmObj.uom;
								itmObj2.categoryId = itmObj.categoryId;	
								itmObj2.category1 = itmObj.category1;
								itmObj2.category2 = itmObj.category2;
								itmObj2.category3 = itmObj.category3;
								itmObj2.category4 = itmObj.category4;
								itmObj2.category5 = itmObj.category5;
								itmObj2.priceList = itmObj.priceList;
								itmObj2.priceSale = itmObj.priceSale;
								itmObj2.priceMin = itmObj.priceMin;
								itmObj2.replacementUnitCost = itmObj.replacementUnitCost;
								
								// 25/9/2007 : Request by Josim
								// Remarks : When re-using item-code, need to set the item-code to Actice. 
								// So that can create PO, Invoice, SR, Trade-in with this item-code
								itmObj2.status = itmObj.status;

								item.setObject(itmObj2);
							}
						} 
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}

	}





	//////////////////////////////////////////////////////////////////////////////////////////////
	///// PROCESS LIQUIDATION FUNCTION
	//////////////////////////////////////////////////////////////////////////////////////////////
	private void fnProcessLiquidationFile(FileItem fileItem, Integer userId, HttpServletResponse res)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when liquidating stocks..... some of the questions that pop up are:
		//	circular # (0)	| effective date(1)	| week-no (2)|  old item code (3)| 	new item code(4) | new w/s price	(5)
		//					| new retail price (6) | new min price (7) | new cost price (8) | new item name (9)| new cat code(10)

		/// 1. Make sure the source and target item codes share the same cost price
				//// otherwise skip.
		/// 2. Only stocks belong to the location specified are liquidated
		/// 3. If the target item codes does not exist, create one
		/// 4. If the source item code does not exist, throw an exception / or output the error message

		Log.printVerbose("CHECK-1");
		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

	    Vector vecLocations = LocationNut.getValueObjectsGiven(
                  LocationBean.STATUS,
                  LocationBean.STATUS_ACTIVE,
                  (String) null,(String) null);	
	    
	    Vector vecSource = new Vector();
	    Vector vecTarget = new Vector();
	    Vector vecLocPrint = new Vector();

		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{

	for(int cnt2=0;cnt2<vecLocations.size();cnt2++)
	{
		Log.printVerbose("CHECK-2");
				LocationObject locationObj = (LocationObject) vecLocations.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
//		circular # (0)	| effective date(1)	| week-no (2)|  old item code (3)| 	new item code(4) | new w/s price	(5)
//						| new retail price (6) | new min price (7) | new cost price (8) | new item name (9)| new cat code(10)
				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String sourceItem = token[3].trim();
				String targetItem = token[4].trim();
				String targetSalePrice = token[5].trim();
				String targetRetailPrice = token[6].trim();
				String targetMinPrice = token[7].trim();
				String targetCostPrice = token[8].trim();
				String targetItemName = token[9].trim();
				String targetItemCatCode = token[10].trim();
				Log.printVerbose("CHECK-3");
				Log.printVerbose(" ............................ LIQUIDATING ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+sourceItem);
				Log.printVerbose(" TARGET ITEM :"+targetItem);
				Log.printVerbose(" TARGET SALE PRICE:"+targetSalePrice);
				Log.printVerbose(" TARGET RETAIL PRICE:"+targetRetailPrice);
				Log.printVerbose(" TARGET MIN PRICE:"+targetMinPrice);
				Log.printVerbose(" TARGET COST PRICE:"+targetCostPrice);
				Log.printVerbose(" TARGET ITEM NAME :"+targetItemName);
				Log.printVerbose(" TARGET ITEM CAT CODE :"+targetItemCatCode);

				if(targetItem.trim().length()<=0){ continue;}
				if(targetSalePrice.trim().length()<=0){ continue;}

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				BigDecimal bdSourcePrice = new BigDecimal(targetSalePrice);
				bdSourcePrice = bdSourcePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				BigDecimal bdTargetSalePrice = new BigDecimal(targetSalePrice);
				bdTargetSalePrice = bdTargetSalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				BigDecimal bdTargetRetailPrice = new BigDecimal(targetRetailPrice);
				bdTargetRetailPrice = bdTargetRetailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdTargetMinPrice = new BigDecimal(targetMinPrice);
				bdTargetMinPrice = bdTargetMinPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdTargetCostPrice = new BigDecimal(targetCostPrice);
				bdTargetCostPrice = bdTargetCostPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				Log.printVerbose("CHECK-4");
				ItemObject itemObjSource = ItemNut.getValueObjectByCode(sourceItem);
				ItemObject itemObjTarget = ItemNut.getValueObjectByCode(targetItem);

				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}

				/// if(itemObjTarget==null).... CREATE A NEW ONE!!
				if(itemObjTarget==null)
				{
					itemObjTarget = new ItemObject();
					itemObjTarget.code = targetItem;
					itemObjTarget.name = "LIQUIDATED ITEM - "+targetSalePrice;
					itemObjTarget.description = "AUTO CREATED ON "+TimeFormat.strDisplayDate();
					itemObjTarget.priceList = bdTargetRetailPrice;
					itemObjTarget.priceSale = bdTargetSalePrice;
					itemObjTarget.priceDisc1 = bdTargetSalePrice;
					itemObjTarget.priceDisc2 = bdTargetSalePrice;
					itemObjTarget.priceDisc3 = bdTargetSalePrice;
					itemObjTarget.priceMin = bdTargetMinPrice;
//					itemObjTarget.fifoUnitCost = bdTargetPrice;
					itemObjTarget.maUnitCost = bdTargetCostPrice;
//					itemObjTarget.waUnitCost = bdTargetPrice;
//					itemObjTarget.lastUnitCost = bdTargetPrice;
					itemObjTarget.replacementUnitCost = bdTargetCostPrice;

					Log.printVerbose("CHECK-5");
							itemObjTarget.categoryId = new Integer(0);
							itemObjTarget.category2 = targetItemCatCode.substring(0, 2);
							itemObjTarget.category3 = targetItemCatCode;
							if(new Integer(targetItemCatCode).intValue() >= new Integer("0100").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("0999").intValue())
							{ itemObjTarget.category1 = "MEN"; itemObjTarget.categoryId = new Integer(1001);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("1000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("1999").intValue())
							{ itemObjTarget.category1 = "LADIES";itemObjTarget.categoryId = new Integer(1002);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("2000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("2999").intValue())
							{ itemObjTarget.category1 = "CHILDREN"; itemObjTarget.categoryId = new Integer(1003);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("3000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("3300").intValue())
							{ itemObjTarget.category1 = "CANVAS"; itemObjTarget.categoryId = new Integer(1004);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("3300").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("3999").intValue())
							{ itemObjTarget.category1 = "ATHLETIC"; itemObjTarget.categoryId = new Integer(1005);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("4000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("4999").intValue())
							{ itemObjTarget.category1 = "OTHERS"; itemObjTarget.categoryId = new Integer(1006);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("5000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("6999").intValue())
							{ itemObjTarget.category1 = "NON-FOOTWEAR"; itemObjTarget.categoryId = new Integer(1007);}
							if(itemObjTarget.category1.equals("NON-FOOTWEAR"))
							{ itemObjTarget.category4 = "NON-FOOTWEAR"; }
							else { itemObjTarget.category4 = "FOOTWEAR";}
							itemObjTarget.category5 = "";

					ItemNut.fnCreate(itemObjTarget);
				}

				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,locationObj.pkid,new Integer(StockNut.STK_COND_GOOD));
				Stock stkEjbTarget = StockNut.getObjectBy(itemObjTarget.pkid,locationObj.pkid,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				StockObject stkObjTarget = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}

					if(stkEjbTarget!=null)
					{ stkObjTarget = stkEjbTarget.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}

				Log.printVerbose("CHECK-6");


				/// if stkEjbSource ==null... nothing to liquidate... skip this!!
				if(stkObjSource==null)
				{
					Log.printVerbose(" SOURCE LOCATION DOES NOT HAVE ANY STOCK.. for source item "+sourceItem);
										
					// 20071016 : Janet
					// Remarks  : Requested by Josim to set the item's status to inactive during liquidation
					System.out.println("Setting the item's status = inactive");
					
					Item itemEjbSource = ItemNut.getHandle(itemObjSource.pkid);
					itemObjSource.status = ItemBean.STATUS_INACTIVE;
					itemEjbSource.setObject(itemObjSource);
					
					System.out.println("Finished setting the item's status = inactive");
					// End
					
					continue;
				}
				Log.printVerbose("CHECK-7");
				if(stkObjSource.balance.signum()==0)
				{
					Log.printVerbose(" SOURCE LOCATION HAS ZERO STOCK ...  "+sourceItem);
					
					// 20071016 : Janet
					// Remarks  : Requested by Josim to set the item's status to inactive during liquidation
					System.out.println("Setting the item's status = inactive");
					
					Item itemEjbSource = ItemNut.getHandle(itemObjSource.pkid);
					itemObjSource.status = ItemBean.STATUS_INACTIVE;
					itemEjbSource.setObject(itemObjSource);
					
					System.out.println("Finished setting the item's status = inactive");
					// End
					
					continue;
				}


				/// if itemObjSource == itemObjTarget.. skip the liquidation
				if(itemObjSource.pkid.equals(itemObjTarget.pkid))
				{ 
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+" ... SAME ITEM CODE AS TARGET ITEM CODE ..");
					continue;
				}
				Log.printVerbose("CHECK-8");
				/// make sure the sourcePrice and targetPrice are the same
				if(itemObjSource.priceSale.compareTo(itemObjTarget.priceSale)!=0)
				{
					Log.printVerbose(" The Source Item Sale Price is different from target Item Sale Price!");
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... SRC PRICE="+itemObjSource.priceSale.toString()+" ... TGT PRICE="+itemObjTarget.priceSale);
					vecSource.add(itemObjSource);
					vecTarget.add(itemObjTarget);
					vecLocPrint.add(locationObj);
					continue;
				}

				/// check the stkSource and stkTarget should have same average costs
				/*if(stkObjTarget!=null)
				{
					if(stkObjTarget.unitCostMA.compareTo(stkObjSource.unitCostMA)!=0)
					{
						Log.printVerbose(" The current cost of the TARGET ITEM is different from the Target Price!");
						Log.printVerbose(" TGT ITEM CODE = "+targetItem);
						Log.printVerbose(" SKIPPING LINE "+lineCount+" ... SRC PRICE="+bdSourcePrice.toString()+" ... TGT PRICE="+targetSalePrice);
						continue;
					}
				}*/
				Log.printVerbose("CHECK-9");
				///////////////////////////////////////////////////////////////////////
				/// LIQUIDATION IN ACTION!!

				/// create a inv-stock-delta for source and target stock
				/// update the balance of the stock
				BigDecimal qtyDelta = stkObjSource.balance;
				{/// SOURCE ITEM ... OUT
					Vector vecSerialObj = new Vector();
					String remarks ="CircularNo:"+circularNo+" WeekNo:"+weekNo+ 
														" Liquidated to "+targetItem+" at "+targetSalePrice;
					StockNut.out(userId, itemObjSource.pkid, locationObj.pkid, locationObj.accPCCenterId,
         			qtyDelta, stkObjSource.unitCostMA, "MYR", "", new Long(0), remarks,
         			tsDate, userId, StockDeltaBean.NS_INVENTORY, StockDeltaBean.TT_ADJUSTMENT, vecSerialObj,
						"", "", "", "", new Integer(0));
					
					System.out.println("Setting the item's status = inactive");
										
					Item itemEjbSource = ItemNut.getHandle(itemObjSource.pkid);
					itemObjSource.status = ItemBean.STATUS_INACTIVE;
					itemEjbSource.setObject(itemObjSource);
					
					System.out.println("Finished setting the item's status = DEL");
				}

				{
					Vector vecSerialObj = new Vector();
					String remarks = "CircularNo:"+circularNo+" WeekNo:"+weekNo+ 
														" Liquidated from "+sourceItem+" at "+CurrencyFormat.strCcy(bdSourcePrice);
					StockNut.in(userId, itemObjTarget.pkid, locationObj.pkid, locationObj.accPCCenterId,
         			qtyDelta, stkObjSource.unitCostMA, "MYR", "", new Long(0), remarks,
         			tsDate, userId, StockDeltaBean.NS_INVENTORY, StockDeltaBean.TT_ADJUSTMENT, vecSerialObj,
						"", "", "", "", new Integer(0));
				}
				
				stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,locationObj.pkid,new Integer(StockNut.STK_COND_GOOD));
				stkEjbTarget = StockNut.getObjectBy(itemObjTarget.pkid,locationObj.pkid,new Integer(StockNut.STK_COND_GOOD));
				stkObjSource = null;
				stkObjTarget = null;

				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}

					if(stkEjbTarget!=null)
					{ stkObjTarget = stkEjbTarget.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}
				/// no changes at the journal transaction

				/// create audit trail
				Log.printVerbose("CHECK-10");
				/// create inv_stock_adjustment entries
               StockAdjustmentObject stkAdj = new StockAdjustmentObject();
               //stkAdj.tx_code = ""; // varchar(50)
			   stkAdj.tx_type = StockAdjustmentBean.TYPE_LIQUIDATE_CONVERSION;
               //stkAdj.tx_module = ""; // varchar(50),
               //stkAdj.tx_option = ""; // varchar(50),
               stkAdj.userid1 = userId;
               stkAdj.userid2 = userId;
               stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
               stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
               //stkAdj.remarks2 = ""; // varchar(100),
               stkAdj.src_pccenter = locationObj.accPCCenterId;
			   stkAdj.src_branch = new Integer(0);
               stkAdj.src_location = locationObj.pkid;
               stkAdj.src_currency = "MYR";
               stkAdj.src_price1 = bdTargetSalePrice;
               stkAdj.src_qty1 = qtyDelta.negate();
               stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
               stkAdj.src_item_id = stkObjSource.itemId;
               stkAdj.src_item_code = itemObjSource.code;
               stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
               stkAdj.tgt_pccenter = locationObj.accPCCenterId;
			   stkAdj.tgt_branch = new Integer(0);
               stkAdj.tgt_location = locationObj.pkid;
               stkAdj.tgt_currency = "MYR";
               stkAdj.tgt_price1 = bdTargetSalePrice;
               stkAdj.tgt_qty1 = stkAdj.src_qty1.negate();
               stkAdj.tgt_serialized = itemObjTarget.serialized;
               stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
               stkAdj.tgt_item_id = stkObjTarget.itemId;
               stkAdj.tgt_item_code = itemObjTarget.code;
               stkAdj.tgt_item_name = itemObjTarget.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
               stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --
               Log.printVerbose("CHECK-11");
				StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);
				Log.printVerbose(" LIQUIDATED ITEM CREATED AT STOCK ADJ TABLE ");

	}// end of vecBranch loop

	Log.printVerbose("CHECK-13");
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		// TKW20080131: Print list of items that don't match price sale
		Log.printVerbose("CHECK-12");
		FileOutputStream out; // declare a file output object
	    PrintStream p; // declare a print stream object

	    // Create a new file output stream connected to "myfile.txt"
	    out = new FileOutputStream("/usr/java/jboss/server/default/deploy/jbossweb-tomcat50.sar/ROOT.war/backup/failedUploadItems-"+TimeFormat.getTimestamp().toString());
	    Log.printVerbose("file name: " + "failedUploadItems-"+TimeFormat.getTimestamp().toString());
	    // Connect print stream to the output stream
	    p = new PrintStream( out );
	    	Log.printVerbose("vecSource.size(): " + vecSource.size());
		for(int i = 0; i<vecSource.size();i++)
		{
			ItemObject srcObj = (ItemObject) vecSource.get(i);
			ItemObject tarObj = (ItemObject) vecTarget.get(i);
			LocationObject locationObj = (LocationObject) vecLocPrint.get(i);
			String locationMsg = "LOCATION: " + locationObj.locationCode + " (" + locationObj.name + ")";
			String sourceMsg = "SOURCE: " + srcObj.code + " (" + srcObj.name + ")";
			Log.printVerbose(sourceMsg);
			String targetMsg = "TARGET: " + tarObj.code + " (" + tarObj.name + ")";
			Log.printVerbose(targetMsg);
			String separatorMsg = "-------";
			Log.printVerbose(separatorMsg);
			p.println(locationMsg);
			p.println(sourceMsg);
			p.println(targetMsg);
			p.println(separatorMsg);
		}
		p.println("TIME COMPLETED: " + TimeFormat.getTimestamp().toString());
		p.close();
		// End TKW20080131
		
	} // end fnProcessLiquidationFile


	//////////////////////////////////////////////////////////////////////////////////////////
	//// MARKING DOWN PRICES
	//////////////////////////////////////////////////////////////////////////////////////////

	private void fnMarkdownPrices(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when marking down the stocks..... some of the questions that pop up are:

		/// 4. If the source item code does not exist, throw an exception / or output the error message

		// CIRCULAR-NO | DATE       | WEEK-NO | ITEM CODE  | NEW W/S PRICE | NEW RETAIL PRICE| NEW MIN PRICE | NEW COST
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		
		Vector vecLocations = LocationNut.getValueObjectsGiven(
                LocationBean.STATUS,
                LocationBean.STATUS_ACTIVE,
                (String) null,(String) null);	
		
		
		Integer iPCC = new Integer(1); /// default PCC primary key
		ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(new Integer(1));
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{
				BigDecimal totalQty = new BigDecimal(0);

				BigDecimal currentSalePrice = new BigDecimal(0);
				BigDecimal currentCost = new BigDecimal(0);

				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String itemCode = token[3].trim();
				String newSalePrice = token[4].trim();
				String newRetailPrice = token[5].trim();
				String newMinPrice = token[6].trim();
				String newCostPrice = token[7].trim();

				Log.printVerbose(" ............................ MARKING DOWN ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+itemCode);
				Log.printVerbose(" WHOLESALE PRICE:"+newSalePrice);
				Log.printVerbose(" RETAIL PRICE:"+newRetailPrice);
				Log.printVerbose(" MIN PRICE:"+newMinPrice);
				Log.printVerbose(" COST PRICE:"+newCostPrice);

				BigDecimal bdSalePrice = new BigDecimal(newSalePrice);
				bdSalePrice = bdSalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdRetailPrice = new BigDecimal(newRetailPrice);
				bdRetailPrice = bdRetailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdMinPrice = new BigDecimal(newMinPrice);
				bdMinPrice = bdMinPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdCostPrice = new BigDecimal(newCostPrice);
				bdCostPrice = bdCostPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				ItemObject itemObjSource = ItemNut.getValueObjectByCode(itemCode);
				
//				 25/9/2007 : Request by Josim
				// Remarks : Only allow active items to be added into PO. Cause when item is deleted from RIMS,
				// during liquidation, the item codoe will be set to deleted in EMP
				ItemObject itemObjTmp = ItemNut.getValueObjectByCode(itemCode);
				
				if(itemObjTmp != null)
				{
					if(!ItemBean.STATUS_ACTIVE.equals(itemObjTmp.status))
					{
						System.out.println(" Do not markdown Item : "+itemObjTmp.code+" as its status is "+itemObjTmp.status);
						
						continue;
					}
				}
				
				
				Item itemEJBSource = ItemNut.getHandle(itemObjSource.pkid);
				currentSalePrice = itemObjSource.priceSale;
				try
				{
					if(itemObjSource!=null && itemEJBSource!=null)
					{
						itemObjSource.priceSale = bdSalePrice;
						itemObjSource.priceList = bdRetailPrice;
						itemObjSource.priceMin = bdMinPrice;
						itemObjSource.maUnitCost = bdCostPrice;
						itemEJBSource.setObject(itemObjSource);
					}
					else
					{
						/// skip this line!!
						continue;
					}
				}

				catch(Exception ex){ ex.printStackTrace();}
				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+itemCode+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}


	for(int cnt2=0;cnt2<vecLocations.size();cnt2++)
	{
		LocationObject locationObj = (LocationObject) vecLocations.get(cnt2);
			

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,
									locationObj.pkid,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}


         	try
         	{

					if(stkObjSource!=null && stkObjSource.accPCCenterId.equals(iPCC) && stkObjSource.balance.signum()!=0)
					{
						totalQty = totalQty.add(stkObjSource.balance);
						currentCost = stkObjSource.unitCostMA;
						stkObjSource.unitCostMA = bdCostPrice;
						stkObjSource.unitCostReplacement = bdSalePrice;
						stkEjbSource.setObject(stkObjSource);
				{
						StockAdjustmentObject stkAdj = new StockAdjustmentObject();
						//stkAdj.tx_code = ""; // varchar(50)
						stkAdj.tx_type = StockAdjustmentBean.TYPE_MARKDOWN;
						//stkAdj.tx_module = ""; // varchar(50),
						//stkAdj.tx_option = ""; // varchar(50),
						stkAdj.userid1 = userId;
						stkAdj.userid2 = userId;
						stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
						stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
						stkAdj.src_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.src_branch = new Integer(0); // integer,
						stkAdj.src_location = stkObjSource.locationId;
						stkAdj.src_currency = pccObj.mCurrency;
						stkAdj.src_price1 = currentSalePrice;
						stkAdj.src_qty1 = stkObjSource.balance;
						stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
						stkAdj.src_item_id = stkObjSource.itemId;
						stkAdj.src_item_code = itemObjSource.code;
						stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
						stkAdj.tgt_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.tgt_branch = new Integer(0); // integer,
						stkAdj.tgt_location = stkObjSource.locationId;
						stkAdj.tgt_currency = pccObj.mCurrency;
						stkAdj.tgt_price1 = bdSalePrice;
						stkAdj.tgt_qty1 = stkObjSource.balance;
						stkAdj.tgt_serialized = itemObjSource.serialized;
						stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
						stkAdj.tgt_item_id = stkObjSource.itemId;
						stkAdj.tgt_item_code = itemObjSource.code;
						stkAdj.tgt_item_name = itemObjSource.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
						stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --
						StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);
				}
            	}
         	} catch (Exception ex)
         	{
            ex.printStackTrace();
         	}
			}// end of vecBranch loop

      if(bdCostPrice.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
         String description = " MA-RESET: ITEMCODE:" + itemObjSource.code;
         description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdCostPrice)
               + " ";
         description += " QTY: " + CurrencyFormat.strInt(totalQty);
         BigDecimal variance = bdCostPrice.subtract(currentCost).multiply(totalQty);
         variance = new BigDecimal(CurrencyFormat.strCcy(variance));
         JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, 
							" AUTO MARKDOWN BY UPLOADING FILE", userId);
			
      }
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnMarkdownPrices







	private void fnCreatePurchaseOrder(FileItem fileItem, Integer userId)
		throws Exception
	{
// OLD
//Branch-code  |KLANG DEPOT    |reference no |external Invoice No |date  |item code  |quantity |wholesale price|retail price
//62017   		|KLANG DEPOT    |00680  		|00680  	|	02009					|060111|8614203-090| 00120   |00000974			|00001299

// NEW
//Branch-code  |KLANG DEPOT    |reference no |external Invoice No |date  |item code  |quantity |wholesale price|retail price | Min Selling Price | Cost Price | Item Category | Item Name
//62017   		|KLANG DEPOT    |00680  		|02009					|060111|8614203-090| 00120   |00000974			|00001299     | 00000650          |  00000440 | 2001          | T-BAR



		 InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		 BufferedReader in = new BufferedReader(inputStreamReader);
         LineNumberReader ln = new LineNumberReader(in);

         String prevInvoice = "";
         String line = null;
         BigDecimal price = new BigDecimal(0);
         PurchaseOrderObject poObj= new PurchaseOrderObject();
         PurchaseOrderItemObject poitem = new PurchaseOrderItemObject();;

			int count =0;

         while ((line = ln.readLine()) != null) 
		 {		
        	count++;
            try 
				{

					Log.printVerbose("----------------------------------");
					Log.printVerbose("PROCESSING LINE :"+count);

					String token[] = line.split("[|]");
					String branchCode = token[0].trim();
					String branchName = token[1].trim();
					String referenceNo = token[2].trim();
					String externalInvoiceNo = token[3].trim();
					String theRawDate = token[4].trim();
					String itemCode = token[5].trim();
					BigDecimal quantity = new BigDecimal(token[6].trim());

					BigDecimal wholesalePrice = new BigDecimal(token[7].trim());
					wholesalePrice = wholesalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal retailPrice = new BigDecimal(token[8].trim());
					retailPrice = retailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal minPrice = new BigDecimal(token[9].trim());
					minPrice = minPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal costPrice = new BigDecimal(token[10].trim());
					costPrice = costPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

               String categorycode = token[11].trim();
			   String itemName = token[12].trim();

			   Integer itm_categoryid = new Integer(1000);
			   String itm_category1 = "";
               String itm_category2 = categorycode.substring(0, 2);
               String itm_category3 = categorycode;
               String itm_category4 = "";
               String itm_category5 = "";
					
               if(new Integer(categorycode).intValue() >= new Integer("0100").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("0999").intValue())
					{ itm_category1 = "MEN"; itm_categoryid = new Integer(1001); }
               else if(new Integer(categorycode).intValue() >= new Integer("1000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("1999").intValue())
					{ itm_category1 = "LADIES"; itm_categoryid = new Integer(1002);}
               else if(new Integer(categorycode).intValue() >= new Integer("2000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("2999").intValue())
					{ itm_category1 = "CHILDREN"; itm_categoryid = new Integer(1003);}
               else if(new Integer(categorycode).intValue() >= new Integer("3000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("3300").intValue())
					{ itm_category1 = "CANVAS"; itm_categoryid = new Integer(1004);}
               else if(new Integer(categorycode).intValue() >= new Integer("3300").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("3999").intValue())
					{ itm_category1 = "ATHLETIC"; itm_categoryid = new Integer(1005);}
               else if(new Integer(categorycode).intValue() >= new Integer("4000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("4999").intValue())
					{ itm_category1 = "OTHERS"; itm_categoryid = new Integer(1006);}
               else if(new Integer(categorycode).intValue() >= new Integer("5000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("6999").intValue())
					{ itm_category1 = "NON-FOOTWEAR"; itm_categoryid = new Integer(1007);}

               String a1_year = "20" + theRawDate.substring(0, 2);
               String a1_month = theRawDate.substring(2, 4);
               String a1_day = theRawDate.substring(4);
			
					Timestamp theDate = TimeFormat.createTimestamp(a1_year+"-"+a1_month+"-"+a1_day);
					if(itm_category1.equals("NON-FOOTWEAR"))
					{ itm_category4 = "NON-FOOTWEAR"; }
					else { itm_category4 = "FOOTWEAR";}

					if( (!prevInvoice.equals(externalInvoiceNo)) && poObj.vecPurchaseOrderItems.size()>0)
					{

						PurchaseOrder poEJB = PurchaseOrderNut.fnCreate(poObj);
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("CREATED PURCHASE ORDER : "+poObj.mPkid.toString());
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("----------------------------------------------------");
						poObj = new PurchaseOrderObject();
					}


					// 25/9/2007 : Request by Josim
					// Remarks : Only allow active items to be added into PO. Cause when item is deleted from RIMS,
					// during liquidation, the item codoe will be set to deleted in EMP
					ItemObject itemObjTmp = ItemNut.getValueObjectByCode(itemCode);
					
					if(itemObjTmp != null)
					{
						if(!ItemBean.STATUS_ACTIVE.equals(itemObjTmp.status))
						{
							System.out.println(" Do not add Item : "+itemObjTmp.code+" into PO as its status is "+itemObjTmp.status);
							
							continue;
						}
					}
					
               Branch branchEJB= BranchNut.getObjectByCode(branchCode);
               if(branchEJB ==null)
			   {    Log.printVerbose(" NO SUCH BRANCH EXIST!!!"+ branchCode); continue;}
               
					BranchObject branchObj = branchEJB.getObject();

					poObj.mTimeCreated = theDate ;
//					poObj.mTimeComplete;
					poObj.mRequestorId = userId;
					poObj.mApproverId = userId;
					poObj.mCurrency = branchObj.currency;
					poObj.mRemarks = " INVOICE:"+externalInvoiceNo+ " REF:"+referenceNo;
					poObj.mUserIdUpdate = userId;
					poObj.mEntityTable = SuppAccountBean.TABLENAME ;
					poObj.mEntityKey = SuppAccountBean.ONE_TIME_SUPPLIER_PKID;
					poObj.mEntityName = "KLANG HQ";
					poObj.mSuppProcCtrId = branchObj.pkid;
					poObj.mLocationId = branchObj.invLocationId;
					poObj.mPCCenter = branchObj.accPCCenterId;
					poObj.mReferenceNo = "INV"+externalInvoiceNo;
					
					poitem = new PurchaseOrderItemObject();
					//poitem.mPurchaseOrderId = obj.mPkid;
					//poitem.mRemarks = poObj.mRemarks;
					poitem.mTotalQty = quantity;
					poitem.mCurrency = poObj.mCurrency;
					poitem.mUnitPriceRecommended = wholesalePrice;
					poitem.mUnitPriceQuoted = costPrice;
					poitem.mStkCode = itemCode;
					poitem.mOutstandingQty = quantity;
					poitem.mPriceList = retailPrice;
					poitem.mPriceSale = wholesalePrice;
					poitem.mPriceMin = minPrice;

					ItemObject itemObj = ItemNut.getValueObjectByCode(itemCode);

					if(itemObj==null)
					{
						itemObj = new ItemObject();
						itemObj.code = itemCode;
						itemObj.name = itemName;
						itemObj.priceList = retailPrice;
						itemObj.priceSale = wholesalePrice;
						itemObj.priceMin = minPrice;
						itemObj.maUnitCost = costPrice;
						itemObj.lastUnitCost = costPrice;
						itemObj.replacementUnitCost = costPrice;
						itemObj.categoryId = itm_categoryid;
						itemObj.category1 = itm_category1;
						itemObj.category2 = itm_category2;
						itemObj.category3 = itm_category3;
						itemObj.category4 = itm_category4;
						itemObj.category5 = itm_category5;
					
						Item itemEJB = ItemNut.fnCreate(itemObj);
					}
//					 2007-09-25
//					 Remarks: Commented out because Miss Chuah requested the program not to update any item details					
//					else
//					{						
//						Item itemEJB = ItemNut.getHandle(itemObj.pkid);
//		                itemObj.code = itemCode;
//		                itemObj.name = itemName;
//// 2006-06-21
//// Remarks: Commented out because Yvonne requested the program not to update the ItemObject when creating the Purchase Order
////                  itemObj.priceList = retailPrice;
////                  itemObj.priceSale = wholesalePrice;
////                  itemObj.priceMin = minPrice;
////						itemObj.maUnitCost = costPrice;
////						itemObj.lastUnitCost = costPrice;
////						itemObj.replacementUnitCost = costPrice;
//		                itemObj.categoryId = itm_categoryid;
//		                itemObj.category1 = itm_category1;
//		                itemObj.category2 = itm_category2;
//		                itemObj.category3 = itm_category3;
//		                itemObj.category4 = itm_category4;
//		                itemObj.category5 = itm_category5;
//		                
//		                System.out.println("Updating the itemObj.status = act");
//		                
//		                itemObj.status = "act";
//		                
//		                System.out.println("Finish updating the itemObj.status = act");
//		                
//						itemEJB.setObject(itemObj);
//					}

					poitem.mPurchaseItemType = itemObj.itemType1;
					poitem.mPurchaseItemId = itemObj.pkid;
					poitem.mItemId = itemObj.pkid;
					poitem.mName = itemObj.name;
					poitem.mPriceList = retailPrice;
					poitem.mPriceSale = wholesalePrice;
					poitem.mPriceMin = minPrice;
	
					poObj.vecPurchaseOrderItems.add(poitem);

					if(poitem.mUnitPriceRecommended.compareTo(itemObj.priceSale)!=0)
					{
						StockAdjustmentObject stkAdj = new StockAdjustmentObject();
						//stkAdj.tx_code = ""; // varchar(50)
						stkAdj.tx_type = StockAdjustmentBean.TYPE_BATA_IMPORT_PO_ITEM_DIFFEFENCE;
						//stkAdj.tx_module = ""; // varchar(50),
						//stkAdj.tx_option = ""; // varchar(50),
						stkAdj.userid1 = userId;
						stkAdj.userid2 = userId;
						stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
						stkAdj.remarks1 = "IMPORTED PO W/S PRICE DIFFERENT FROM ITEM W/S PRICE";
						stkAdj.src_pccenter = branchObj.accPCCenterId;
						stkAdj.src_branch = branchObj.pkid;
						stkAdj.src_location = branchObj.invLocationId;
						stkAdj.src_currency = branchObj.currency;
						stkAdj.src_price1 = itemObj.priceSale;
						stkAdj.src_qty1 = poitem.mTotalQty;
						stkAdj.src_serialized = itemObj.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               	stkAdj.src_refdoc = PurchaseOrderBean.TABLENAME;
               	stkAdj.src_refkey = poObj.mPkid;
						stkAdj.src_item_id = itemObj.pkid;
						stkAdj.src_item_code = itemObj.code;
						stkAdj.src_item_name = itemObj.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
						stkAdj.tgt_pccenter = branchObj.accPCCenterId;
						stkAdj.tgt_branch = branchObj.pkid;
						stkAdj.tgt_location = branchObj.invLocationId;
						stkAdj.tgt_currency = branchObj.currency;
						stkAdj.tgt_price1 = poitem.mUnitPriceRecommended;
						stkAdj.tgt_qty1 = poitem.mTotalQty;
						stkAdj.tgt_serialized = itemObj.serialized;
						stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
						stkAdj.tgt_item_id = itemObj.pkid;
						stkAdj.tgt_item_code = itemObj.code;
						stkAdj.tgt_item_name = itemObj.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
						stkAdj.lastupdate = theDate;
						StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);
					}


					prevInvoice = externalInvoiceNo;
            } 
				catch (Exception ex) 
				{
               ex.printStackTrace();
            }
         }// end while

			if(poObj.vecPurchaseOrderItems.size()>0)
         {
                  PurchaseOrder poEJB = PurchaseOrderNut.fnCreate(poObj);
                  Log.printVerbose("----------------------------------------------------");
                  Log.printVerbose("LASSSSSSSSSSSSSSSSSSSSSSSSTTTT");
                  Log.printVerbose("CREATED PURCHASE ORDER : "+poObj.mPkid.toString());
                  Log.printVerbose("----------------------------------------------------");
						poObj = new PurchaseOrderObject();
         }



		}


	private void fnAdjustMA(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when marking down the stocks..... some of the questions that pop up are:

		/// 4. If the source item code does not exist, throw an exception / or output the error message

		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		Vector vecBranch = BranchNut.getValueObjectsGiven(
                     BranchBean.STATUS,
                     BranchBean.STATUS_ACTIVE,
                     (String)null, (String)null);
		Integer iPCC = new Integer(1); /// default PCC primary key
      ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(new Integer(1));
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{
				BigDecimal totalQty = new BigDecimal(0);
				BigDecimal currentCost = new BigDecimal(0);
				BigDecimal bdSourcePrice = new BigDecimal(0);
				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String sourceItem = token[3].trim();
				String sourcePrice = token[4].trim();
				String targetItem = token[5].trim();
				String targetSalePrice = token[6].trim();

				Log.printVerbose(" ............................ MARKING DOWN ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+sourceItem);
				Log.printVerbose(" SOURCE PRICE:"+sourcePrice);
				Log.printVerbose(" TARGET ITEM :"+targetItem);
				Log.printVerbose(" TARGET PRICE:"+targetSalePrice);

				bdSourcePrice = new BigDecimal(sourcePrice);
				bdSourcePrice = bdSourcePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				ItemObject itemObjSource = ItemNut.getValueObjectByCode(sourceItem);

				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}


	for(int cnt2=0;cnt2<vecBranch.size();cnt2++)
	{
				BranchObject branchObj = (BranchObject) vecBranch.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,
									branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}


         	try
         	{

//					if(stkObjSource.accPCCenterId.equals(iPCC) && stkObjSource.balance.signum()!=0)
					if(stkObjSource.accPCCenterId.equals(iPCC))
					{
						totalQty = totalQty.add(stkObjSource.balance);
						currentCost = stkObjSource.unitCostMA;
						stkObjSource.unitCostMA = bdSourcePrice;
						stkEjbSource.setObject(stkObjSource);

						StockAdjustmentObject stkAdj = new StockAdjustmentObject();
						//stkAdj.tx_code = ""; // varchar(50)
						stkAdj.tx_type = StockAdjustmentBean.TYPE_RESET_MA;
						//stkAdj.tx_module = ""; // varchar(50),
						//stkAdj.tx_option = ""; // varchar(50),
						stkAdj.userid1 = userId;
						stkAdj.userid2 = userId;
						stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
						stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
						stkAdj.src_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.src_branch = new Integer(0); // integer,
						stkAdj.src_location = stkObjSource.locationId;
						stkAdj.src_currency = pccObj.mCurrency;
						stkAdj.src_price1 = currentCost;
						stkAdj.src_qty1 = stkObjSource.balance;
						stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
						stkAdj.src_item_id = stkObjSource.itemId;
						stkAdj.src_item_code = itemObjSource.code;
						stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
						stkAdj.tgt_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.tgt_branch = new Integer(0); // integer,
						stkAdj.tgt_location = stkObjSource.locationId;
						stkAdj.tgt_currency = pccObj.mCurrency;
						stkAdj.tgt_price1 = bdSourcePrice;
						stkAdj.tgt_qty1 = stkObjSource.balance;
						stkAdj.tgt_serialized = itemObjSource.serialized;
						stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
						stkAdj.tgt_item_id = stkObjSource.itemId;
						stkAdj.tgt_item_code = itemObjSource.code;
						stkAdj.tgt_item_name = itemObjSource.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
						stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --
						StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

            }
         } catch (Exception ex)
         {
            ex.printStackTrace();
         }
	}// end of vecBranch loop

      if(bdSourcePrice.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
         String description = " MA-RESET: ITEMCODE:" + itemObjSource.code;
         description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdSourcePrice)
               + " ";
         description += " QTY: " + CurrencyFormat.strInt(totalQty);
         BigDecimal variance = bdSourcePrice.subtract(currentCost).multiply(totalQty);
         variance = new BigDecimal(CurrencyFormat.strCcy(variance));
         JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, " AUTO MARKDOWN BY UPLOADING FILE", userId);
      }

			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnMarkdownPrices







}

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

import java.io.*;
import java.math.*;
import java.util.*;
import java.sql.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoUploadCatItem implements Action // extends HttpServlet
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, 
								HttpServletResponse res) 
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("footwear-upload-cat-item-page");
		} 
		else
		{
			try
			{
				fnProcessInputFiles(req, res);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return new ActionRouter("footwear-upload-cat-item-page");
	}

	private void fnProcessInputFiles(HttpServletRequest req, HttpServletResponse res) 
					throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload fu = new ServletFileUpload(factory);
		fu.setSizeMax(50000000);
		List fileItems = fu.parseRequest(req);
		Iterator itr = fileItems.iterator();
		while (itr.hasNext())
		{
			FileItem fi = (FileItem) itr.next();
			if (!fi.isFormField() && fi.getFieldName().equals("categoryfile"))
			{
				fnProcessCategoryFile(fi);
			}/// end if categoryfile

 
			if (!fi.isFormField() && fi.getFieldName().equals("itemfile"))
			{
				fnProcessItemFile(fi);
			}// end if itemfile

			if (!fi.isFormField() && fi.getFieldName().equals("liquidatingItems"))
			{
				fnProcessLiquidationFile(fi, userId);
			}// end if liquidatingItems

			if(!fi.isFormField() && fi.getFieldName().equals("createPurchaseOrder"))
			{
				fnCreatePurchaseOrder(fi,userId);
			}// end if createPurchaseOrder

			if(!fi.isFormField() && fi.getFieldName().equals("markdownPrices"))
			{
				fnMarkdownPrices(fi,userId);
			}// end if createPurchaseOrder

		}/// END WHILE itr.hasNext()
	}// END fnUploadItem


	private void fnProcessCategoryFile(FileItem fileItem)
		throws Exception
	{
					InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
					BufferedReader in = new BufferedReader(inputStreamReader);
					LineNumberReader ln = new LineNumberReader(in);
					String line;
					while ((line = ln.readLine()) != null)
					{
						try
						{
							StringTokenizer sb = new StringTokenizer(line, "|");
							CategoryTreeObject obj = new CategoryTreeObject();
							sb.nextToken();
							obj.code = sb.nextToken().trim();
							obj.name = sb.nextToken().trim();
							obj.sort = "0";
							obj.description = sb.nextToken().trim();
							if (obj.code.length() < 4)
								obj.catLevel = new Integer(2);
							else
								obj.catLevel = new Integer(3);
							CategoryTreeHome cattreehome = CategoryTreeNut.getHome();
							QueryObject query = new QueryObject(new String[] { CategoryTreeBean.CODE + " = '" + obj.code + "' " });
							Collection coll = CategoryTreeNut.getObjects(query);
							if (coll.isEmpty() || coll.size() == 0)
							{
								cattreehome.create(obj);
							} else
							{
								CategoryTreeObject cattree = (CategoryTreeObject) coll.iterator().next();
								CategoryTree categoryEJB = CategoryTreeNut.getHandle(cattree.pkid);
								obj.pkid = cattree.pkid;
								obj.parentId = cattree.parentId;
								obj.sort = cattree.sort;
								categoryEJB.setObject(obj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}


	}

	private void fnProcessItemFile(FileItem fileItem)
		throws Exception
	{
		///item code(0)	| item name	(1) | Item description (2) | 	uom (3)	| cat code (4) 
///			|		w/s price (5) | 	retail price (6) | 	min price (7) |	cost price (8) 



					InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
					BufferedReader in = new BufferedReader(inputStreamReader);
					LineNumberReader ln = new LineNumberReader(in);
					String line;
					while ((line = ln.readLine()) != null)
					{
						try
						{
							ItemObject itmObj = new ItemObject();
							String token[] = line.split("[|]");

							itmObj.code = token[0].trim();
							itmObj.name = token[1].trim();
							itmObj.description = token[2].trim();
							itmObj.uom = token[3].trim();

							String categorycode = token[4].trim();

							BigDecimal wholesalePrice = new BigDecimal(token[5].trim());
							wholesalePrice = wholesalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal retailPrice = new BigDecimal(token[6].trim());
							retailPrice = retailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal minPrice = new BigDecimal(token[7].trim());
							minPrice = minPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal costPrice = new BigDecimal(token[8].trim());
							costPrice = costPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

							itmObj.categoryId = new Integer(0);
							itmObj.category2 = categorycode.substring(0, 2);
							itmObj.category3 = categorycode;
							if(new Integer(categorycode).intValue() >= new Integer("0100").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("0999").intValue())
							{ itmObj.category1 = "MEN"; itmObj.categoryId = new Integer(1001);}
							else if(new Integer(categorycode).intValue() >= new Integer("1000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("1999").intValue())
							{ itmObj.category1 = "LADIES";itmObj.categoryId = new Integer(1002);}
							else if(new Integer(categorycode).intValue() >= new Integer("2000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("2999").intValue())
							{ itmObj.category1 = "CHILDREN"; itmObj.categoryId = new Integer(1003);}
							else if(new Integer(categorycode).intValue() >= new Integer("3000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("3300").intValue())
							{ itmObj.category1 = "CANVAS"; itmObj.categoryId = new Integer(1004);}
							else if(new Integer(categorycode).intValue() >= new Integer("3300").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("3999").intValue())
							{ itmObj.category1 = "ATHLETIC"; itmObj.categoryId = new Integer(1005);}
							else if(new Integer(categorycode).intValue() >= new Integer("4000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("4999").intValue())
							{ itmObj.category1 = "OTHERS"; itmObj.categoryId = new Integer(1006);}
							else if(new Integer(categorycode).intValue() >= new Integer("5000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("6999").intValue())
							{ itmObj.category1 = "NON-FOOTWEAR"; itmObj.categoryId = new Integer(1007);}
							itmObj.category4 = "";
							itmObj.category5 = "";

							itmObj.status = ItemBean.STATUS_ACTIVE;
							itmObj.priceList = retailPrice;
							itmObj.priceSale = wholesalePrice;
							itmObj.priceMin = minPrice;
							itmObj.replacementUnitCost = costPrice;

							Item item = ItemNut.getObjectByCode(itmObj.code);

							if(item==null)
							{
								item = ItemNut.fnCreate(itmObj);
							} 
							else 
							{
								//ItemObject item = (ItemObject) coll.iterator().next();
								ItemObject itmObj2 = item.getObject();
								itmObj2.name = itmObj.name;
								itmObj2.description = itmObj.description;
								itmObj2.uom = itmObj.uom;
								itmObj2.categoryId = itmObj.categoryId;	
								itmObj2.category1 = itmObj.category1;
								itmObj2.category2 = itmObj.category2;
								itmObj2.category3 = itmObj.category3;
								itmObj2.category4 = itmObj.category4;
								itmObj2.category5 = itmObj.category5;
								itmObj2.priceList = itmObj.priceList;
								itmObj2.priceSale = itmObj.priceSale;
								itmObj2.priceMin = itmObj.priceMin;
								itmObj2.replacementUnitCost = itmObj.replacementUnitCost;

								item.setObject(itmObj2);
							}
						} 
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}

	}





	//////////////////////////////////////////////////////////////////////////////////////////////
	///// PROCESS LIQUIDATION FUNCTION
	//////////////////////////////////////////////////////////////////////////////////////////////
	private void fnProcessLiquidationFile(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when liquidating stocks..... some of the questions that pop up are:
//		circular # (0)	| effective date(1)	| week-no (2)|  old item code (3)| 	new item code(4) | new w/s price	(5)
//						| new retail price (6) | new min price (7) | new cost price (8) | new item name (9)| new cat code(10)

		/// 1. Make sure the source and target item codes share the same cost price
				//// otherwise skip.
		/// 2. Only stocks belong to the location specified are liquidated
		/// 3. If the target item codes does not exist, create one
		/// 4. If the source item code does not exist, throw an exception / or output the error message


		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		Vector vecBranch = BranchNut.getValueObjectsGiven(
                     BranchBean.STATUS,
                     BranchBean.STATUS_ACTIVE,
                     (String)null, (String)null);
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{

	for(int cnt2=0;cnt2<vecBranch.size();cnt2++)
	{
				BranchObject branchObj = (BranchObject) vecBranch.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
//		circular # (0)	| effective date(1)	| week-no (2)|  old item code (3)| 	new item code(4) | new w/s price	(5)
//						| new retail price (6) | new min price (7) | new cost price (8) | new item name (9)| new cat code(10)
				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String sourceItem = token[3].trim();
				String targetItem = token[4].trim();
				String targetSalePrice = token[5].trim();
				String targetRetailPrice = token[6].trim();
				String targetMinPrice = token[7].trim();
				String targetCostPrice = token[8].trim();
				String targetItemName = token[9].trim();
				String targetItemCatCode = token[10].trim();

				Log.printVerbose(" ............................ LIQUIDATING ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+sourceItem);
				Log.printVerbose(" TARGET ITEM :"+targetItem);
				Log.printVerbose(" TARGET SALE PRICE:"+targetSalePrice);
				Log.printVerbose(" TARGET RETAIL PRICE:"+targetRetailPrice);
				Log.printVerbose(" TARGET MIN PRICE:"+targetMinPrice);
				Log.printVerbose(" TARGET COST PRICE:"+targetCostPrice);
				Log.printVerbose(" TARGET ITEM NAME :"+targetItemName);
				Log.printVerbose(" TARGET ITEM CAT CODE :"+targetItemCatCode);

				if(targetItem.trim().length()<=0){ continue;}
				if(targetSalePrice.trim().length()<=0){ continue;}

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				BigDecimal bdSourcePrice = new BigDecimal(targetSalePrice);
				bdSourcePrice = bdSourcePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				BigDecimal bdTargetSalePrice = new BigDecimal(targetSalePrice);
				bdTargetSalePrice = bdTargetSalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				BigDecimal bdTargetRetailPrice = new BigDecimal(targetRetailPrice);
				bdTargetRetailPrice = bdTargetRetailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdTargetMinPrice = new BigDecimal(targetMinPrice);
				bdTargetMinPrice = bdTargetMinPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdTargetCostPrice = new BigDecimal(targetCostPrice);
				bdTargetCostPrice = bdTargetCostPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);


				ItemObject itemObjSource = ItemNut.getValueObjectByCode(sourceItem);
				ItemObject itemObjTarget = ItemNut.getValueObjectByCode(targetItem);

				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}

				/// if(itemObjTarget==null).... CREATE A NEW ONE!!
				if(itemObjTarget==null)
				{
					itemObjTarget = new ItemObject();
					itemObjTarget.code = targetItem;
					itemObjTarget.name = "LIQUIDATED ITEM - "+targetSalePrice;
					itemObjTarget.description = "AUTO CREATED ON "+TimeFormat.strDisplayDate();
					itemObjTarget.priceList = bdTargetRetailPrice;
					itemObjTarget.priceSale = bdTargetSalePrice;
					itemObjTarget.priceDisc1 = bdTargetSalePrice;
					itemObjTarget.priceDisc2 = bdTargetSalePrice;
					itemObjTarget.priceDisc3 = bdTargetSalePrice;
					itemObjTarget.priceMin = bdTargetMinPrice;
//					itemObjTarget.fifoUnitCost = bdTargetPrice;
					itemObjTarget.maUnitCost = bdTargetCostPrice;
//					itemObjTarget.waUnitCost = bdTargetPrice;
//					itemObjTarget.lastUnitCost = bdTargetPrice;
					itemObjTarget.replacementUnitCost = bdTargetCostPrice;


							itemObjTarget.categoryId = new Integer(0);
							itemObjTarget.category2 = targetItemCatCode.substring(0, 2);
							itemObjTarget.category3 = targetItemCatCode;
							if(new Integer(targetItemCatCode).intValue() >= new Integer("0100").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("0999").intValue())
							{ itemObjTarget.category1 = "MEN"; itemObjTarget.categoryId = new Integer(1001);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("1000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("1999").intValue())
							{ itemObjTarget.category1 = "LADIES";itemObjTarget.categoryId = new Integer(1002);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("2000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("2999").intValue())
							{ itemObjTarget.category1 = "CHILDREN"; itemObjTarget.categoryId = new Integer(1003);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("3000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("3300").intValue())
							{ itemObjTarget.category1 = "CANVAS"; itemObjTarget.categoryId = new Integer(1004);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("3300").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("3999").intValue())
							{ itemObjTarget.category1 = "ATHLETIC"; itemObjTarget.categoryId = new Integer(1005);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("4000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("4999").intValue())
							{ itemObjTarget.category1 = "OTHERS"; itemObjTarget.categoryId = new Integer(1006);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("5000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("6999").intValue())
							{ itemObjTarget.category1 = "NON-FOOTWEAR"; itemObjTarget.categoryId = new Integer(1007);}
							itemObjTarget.category4 = "";
							itemObjTarget.category5 = "";

					ItemNut.fnCreate(itemObjTarget);
				}

				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				Stock stkEjbTarget = StockNut.getObjectBy(itemObjTarget.pkid,branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				StockObject stkObjTarget = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}

					if(stkEjbTarget!=null)
					{ stkObjTarget = stkEjbTarget.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}




				/// if stkEjbSource ==null... nothing to liquidate... skip this!!
				if(stkObjSource==null)
				{
					Log.printVerbose(" SOURCE LOCATION DOES NOT HAVE ANY STOCK.. ");
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"...  AT "+branchObj.description+" !");
					continue;
				}

				if(stkObjSource.balance.signum()==0)
				{
					Log.printVerbose(" SOURCE LOCATION HAS ZERO STOCK ... ");
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"...  AT "+branchObj.description+" !");
					continue;
				}


				/// if itemObjSource == itemObjTarget.. skip the liquidation
				if(itemObjSource.pkid.equals(itemObjTarget.pkid))
				{ 
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+" ... SAME ITEM CODE AS TARGET ITEM CODE ..");
					continue;
				}

				/// make sure the sourcePrice and targetPrice are the same
				if(bdSourcePrice.compareTo(bdTargetSalePrice)!=0)
				{
					Log.printVerbose(" The Source Item Cost is different from target Item Cost!");
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... SRC PRICE="+bdSourcePrice.toString()+" ... TGT PRICE="+targetSalePrice);
					continue;
				}

				/// check the stkSource and stkTarget should have same average costs
				if(stkObjTarget!=null)
				{
					if(stkObjTarget.unitCostMA.compareTo(stkObjSource.unitCostMA)!=0)
					{
						Log.printVerbose(" The current cost of the TARGET ITEM is different from the Target Price!");
						Log.printVerbose(" TGT ITEM CODE = "+targetItem);
						Log.printVerbose(" SKIPPING LINE "+lineCount+" ... SRC PRICE="+bdSourcePrice.toString()+" ... TGT PRICE="+targetSalePrice);
						continue;
					}
				}

				///////////////////////////////////////////////////////////////////////
				/// LIQUIDATION IN ACTION!!

				/// create a inv-stock-delta for source and target stock
				/// update the balance of the stock
				BigDecimal qtyDelta = stkObjSource.balance;
				{/// SOURCE ITEM ... OUT
					Vector vecSerialObj = new Vector();
					String remarks ="CircularNo:"+circularNo+" WeekNo:"+weekNo+ 
														" Liquidated to "+targetItem+" at "+targetSalePrice;
					StockNut.out(userId, itemObjSource.pkid, branchObj.invLocationId, branchObj.accPCCenterId,
         			qtyDelta, stkObjSource.unitCostMA, branchObj.currency, "", new Long(0), remarks,
         			tsDate, userId, StockDeltaBean.NS_INVENTORY, StockDeltaBean.TT_ADJUSTMENT, vecSerialObj);
				}

				{
					Vector vecSerialObj = new Vector();
					String remarks = "CircularNo:"+circularNo+" WeekNo:"+weekNo+ 
														" Liquidated from "+sourceItem+" at "+CurrencyFormat.strCcy(bdSourcePrice);
					StockNut.in(userId, itemObjTarget.pkid, branchObj.invLocationId, branchObj.accPCCenterId,
         			qtyDelta, stkObjSource.unitCostMA, branchObj.currency, "", new Long(0), remarks,
         			tsDate, userId, StockDeltaBean.NS_INVENTORY, StockDeltaBean.TT_ADJUSTMENT, vecSerialObj);
				}


				/// no changes at the journal transaction

				/// create audit trail

				/// create inv_stock_adjustment entries
               StockAdjustmentObject stkAdj = new StockAdjustmentObject();
               //stkAdj.tx_code = ""; // varchar(50)
					stkAdj.tx_type = StockAdjustmentBean.TYPE_LIQUIDATE_CONVERSION;
               //stkAdj.tx_module = ""; // varchar(50),
               //stkAdj.tx_option = ""; // varchar(50),
               stkAdj.userid1 = userId;
               stkAdj.userid2 = userId;
               stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
               stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
               //stkAdj.remarks2 = ""; // varchar(100),
               stkAdj.src_pccenter = branchObj.accPCCenterId;
					stkAdj.src_branch = branchObj.pkid;
               stkAdj.src_location = branchObj.invLocationId;
               stkAdj.src_currency = branchObj.currency;
               stkAdj.src_price1 = stkObjSource.unitCostMA;
               stkAdj.src_qty1 = qtyDelta.negate();
               stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
               stkAdj.src_item_id = stkObjSource.itemId;
               stkAdj.src_item_code = itemObjSource.code;
               stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
               stkAdj.tgt_pccenter = branchObj.accPCCenterId;
					stkAdj.tgt_branch = branchObj.pkid;
               stkAdj.tgt_location = branchObj.invLocationId;
               stkAdj.tgt_currency = branchObj.currency;
               stkAdj.tgt_price1 = bdTargetSalePrice;
               stkAdj.tgt_qty1 = stkAdj.src_qty1.negate();
               stkAdj.tgt_serialized = itemObjTarget.serialized;
               stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
               stkAdj.tgt_item_id = stkObjTarget.itemId;
               stkAdj.tgt_item_code = itemObjTarget.code;
               stkAdj.tgt_item_name = itemObjTarget.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
               stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --

					StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

	}// end of vecBranch loop

			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnProcessLiquidationFile


	//////////////////////////////////////////////////////////////////////////////////////////
	//// MARKING DOWN PRICES
	//////////////////////////////////////////////////////////////////////////////////////////

	private void fnMarkdownPrices(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when marking down the stocks..... some of the questions that pop up are:

		/// 4. If the source item code does not exist, throw an exception / or output the error message

		// CIRCULAR-NO | DATE       | WEEK-NO | ITEM CODE  | NEW W/S PRICE | NEW RETAIL PRICE| NEW MIN PRICE | NEW COST
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		Vector vecBranch = BranchNut.getValueObjectsGiven(
                     BranchBean.STATUS,
                     BranchBean.STATUS_ACTIVE,
                     (String)null, (String)null);
		Integer iPCC = new Integer(1); /// default PCC primary key
      ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(new Integer(1));
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{
				BigDecimal totalQty = new BigDecimal(0);

				BigDecimal currentSalePrice = new BigDecimal(0);
				BigDecimal currentCost = new BigDecimal(0);

				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String itemCode = token[3].trim();
				String newSalePrice = token[4].trim();
				String newRetailPrice = token[5].trim();
				String newMinPrice = token[6].trim();
				String newCostPrice = token[7].trim();

				Log.printVerbose(" ............................ MARKING DOWN ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+itemCode);
				Log.printVerbose(" WHOLESALE PRICE:"+newSalePrice);
				Log.printVerbose(" RETAIL PRICE:"+newRetailPrice);
				Log.printVerbose(" MIN PRICE:"+newMinPrice);
				Log.printVerbose(" COST PRICE:"+newCostPrice);

				BigDecimal bdSalePrice = new BigDecimal(newSalePrice);
				bdSalePrice = bdSalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdRetailPrice = new BigDecimal(newRetailPrice);
				bdRetailPrice = bdRetailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdMinPrice = new BigDecimal(newMinPrice);
				bdMinPrice = bdMinPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdCostPrice = new BigDecimal(newCostPrice);
				bdCostPrice = bdCostPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				ItemObject itemObjSource = ItemNut.getValueObjectByCode(itemCode);
				currentSalePrice = itemObjSource.priceSale;
				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+itemCode+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}


	for(int cnt2=0;cnt2<vecBranch.size();cnt2++)
	{
				BranchObject branchObj = (BranchObject) vecBranch.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,
									branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}


         	try
         	{

					if(stkObjSource.accPCCenterId.equals(iPCC) && stkObjSource.balance.signum()!=0)
					{
						totalQty = totalQty.add(stkObjSource.balance);
						currentCost = stkObjSource.unitCostMA;
						stkObjSource.unitCostMA = bdCostPrice;
						stkObjSource.unitCostReplacement = bdSalePrice;
						stkEjbSource.setObject(stkObjSource);
				{
						StockAdjustmentObject stkAdj = new StockAdjustmentObject();
						//stkAdj.tx_code = ""; // varchar(50)
						stkAdj.tx_type = StockAdjustmentBean.TYPE_MARKDOWN;
						//stkAdj.tx_module = ""; // varchar(50),
						//stkAdj.tx_option = ""; // varchar(50),
						stkAdj.userid1 = userId;
						stkAdj.userid2 = userId;
						stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
						stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
						stkAdj.src_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.src_branch = new Integer(0); // integer,
						stkAdj.src_location = stkObjSource.locationId;
						stkAdj.src_currency = pccObj.mCurrency;
						stkAdj.src_price1 = currentSalePrice;
						stkAdj.src_qty1 = stkObjSource.balance;
						stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
						stkAdj.src_item_id = stkObjSource.itemId;
						stkAdj.src_item_code = itemObjSource.code;
						stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
						stkAdj.tgt_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.tgt_branch = new Integer(0); // integer,
						stkAdj.tgt_location = stkObjSource.locationId;
						stkAdj.tgt_currency = pccObj.mCurrency;
						stkAdj.tgt_price1 = bdSalePrice;
						stkAdj.tgt_qty1 = stkObjSource.balance;
						stkAdj.tgt_serialized = itemObjSource.serialized;
						stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
						stkAdj.tgt_item_id = stkObjSource.itemId;
						stkAdj.tgt_item_code = itemObjSource.code;
						stkAdj.tgt_item_name = itemObjSource.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
						stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --
						StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);
				}
            	}
         	} catch (Exception ex)
         	{
            ex.printStackTrace();
         	}
			}// end of vecBranch loop

      if(bdCostPrice.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
         String description = " MA-RESET: ITEMCODE:" + itemObjSource.code;
         description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdCostPrice)
               + " ";
         description += " QTY: " + CurrencyFormat.strInt(totalQty);
         BigDecimal variance = bdCostPrice.subtract(currentCost).multiply(totalQty);
         variance = new BigDecimal(CurrencyFormat.strCcy(variance));
         JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, 
							" AUTO MARKDOWN BY UPLOADING FILE", userId);
      }
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnMarkdownPrices







	private void fnCreatePurchaseOrder(FileItem fileItem, Integer userId)
		throws Exception
	{
// OLD
//Branch-code  |KLANG DEPOT    |reference no |external Invoice No |date  |item code  |quantity |wholesale price|retail price
//62017   		|KLANG DEPOT    |00680  		|00680  	|	02009					|060111|8614203-090| 00120   |00000974			|00001299

// NEW
//Branch-code  |KLANG DEPOT    |reference no |external Invoice No |date  |item code  |quantity |wholesale price|retail price | Min Selling Price | Cost Price | Item Category | Item Name
//62017   		|KLANG DEPOT    |00680  		|02009					|060111|8614203-090| 00120   |00000974			|00001299     | 00000650          |  00000440 | 2001          | T-BAR



			InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
			BufferedReader in = new BufferedReader(inputStreamReader);
         LineNumberReader ln = new LineNumberReader(in);

         String prevInvoice = "";
         String line = null;
         BigDecimal price = new BigDecimal(0);
         PurchaseOrderObject poObj= new PurchaseOrderObject();
         PurchaseOrderItemObject poitem = new PurchaseOrderItemObject();;

			int count =0;

         while ((line = ln.readLine()) != null) 
			{
				count++;
            try 
				{

					Log.printVerbose("----------------------------------");
					Log.printVerbose("PROCESSING LINE :"+count);

					String token[] = line.split("[|]");
					String branchCode = token[0].trim();
					String branchName = token[1].trim();
					String referenceNo = token[2].trim();
					String externalInvoiceNo = token[3].trim();
					String theRawDate = token[4].trim();
					String itemCode = token[5].trim();
					BigDecimal quantity = new BigDecimal(token[6].trim());


					BigDecimal wholesalePrice = new BigDecimal(token[7].trim());
					wholesalePrice = wholesalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal retailPrice = new BigDecimal(token[8].trim());
					retailPrice = retailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal minPrice = new BigDecimal(token[9].trim());
					minPrice = minPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal costPrice = new BigDecimal(token[10].trim());
					costPrice = costPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

               String categorycode = token[11].trim();
					String itemName = token[12].trim();

					Integer itm_categoryid = new Integer(1000);
					String itm_category1 = "";
               String itm_category2 = categorycode.substring(0, 2);
               String itm_category3 = categorycode;
               String itm_category4 = "";
               String itm_category5 = "";
					
               if(new Integer(categorycode).intValue() >= new Integer("0100").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("0999").intValue())
					{ itm_category1 = "MEN"; itm_categoryid = new Integer(1001); }
               else if(new Integer(categorycode).intValue() >= new Integer("1000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("1999").intValue())
					{ itm_category1 = "LADIES"; itm_categoryid = new Integer(1002);}
               else if(new Integer(categorycode).intValue() >= new Integer("2000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("2999").intValue())
					{ itm_category1 = "CHILDREN"; itm_categoryid = new Integer(1003);}
               else if(new Integer(categorycode).intValue() >= new Integer("3000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("3300").intValue())
					{ itm_category1 = "CANVAS"; itm_categoryid = new Integer(1004);}
               else if(new Integer(categorycode).intValue() >= new Integer("3300").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("3999").intValue())
					{ itm_category1 = "ATHLETIC"; itm_categoryid = new Integer(1005);}
               else if(new Integer(categorycode).intValue() >= new Integer("4000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("4999").intValue())
					{ itm_category1 = "OTHERS"; itm_categoryid = new Integer(1006);}
               else if(new Integer(categorycode).intValue() >= new Integer("5000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("6999").intValue())
					{ itm_category1 = "NON-FOOTWEAR"; itm_categoryid = new Integer(1007);}

               String a1_year = "20" + theRawDate.substring(0, 2);
               String a1_month = theRawDate.substring(2, 4);
               String a1_day = theRawDate.substring(4);
			
					Timestamp theDate = TimeFormat.createTimestamp(a1_year+"-"+a1_month+"-"+a1_day);



					if( (!prevInvoice.equals(externalInvoiceNo)) && poObj.vecPurchaseOrderItems.size()>0)
					{

						PurchaseOrder poEJB = PurchaseOrderNut.fnCreate(poObj);
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("CREATED PURCHASE ORDER : "+poObj.mPkid.toString());
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("----------------------------------------------------");
						poObj = new PurchaseOrderObject();
					}


               Branch branchEJB= BranchNut.getObjectByCode(branchCode);
               if(branchEJB ==null)
					{ Log.printVerbose(" NO SUCH BRANCH EXIST!!!"+ branchCode); continue;}
					BranchObject branchObj = branchEJB.getObject();

					poObj.mTimeCreated = theDate ;
//					poObj.mTimeComplete;
					poObj.mRequestorId = userId;
					poObj.mApproverId = userId;
					poObj.mCurrency = branchObj.currency;
					poObj.mRemarks = " INVOICE:"+externalInvoiceNo+ " REF:"+referenceNo;
   				poObj.mUserIdUpdate = userId;
   				poObj.mEntityTable = SuppAccountBean.TABLENAME ;
					poObj.mEntityKey = SuppAccountBean.ONE_TIME_SUPPLIER_PKID;
					poObj.mEntityName = "KLANG HQ";
   				poObj.mSuppProcCtrId = branchObj.pkid;
					poObj.mLocationId = branchObj.invLocationId;
					poObj.mPCCenter = branchObj.accPCCenterId;
   				poObj.mReferenceNo = "INV"+externalInvoiceNo;


               poitem = new PurchaseOrderItemObject();
               //poitem.mPurchaseOrderId = obj.mPkid;
               //poitem.mRemarks = poObj.mRemarks;
               poitem.mTotalQty = quantity;
               poitem.mCurrency = poObj.mCurrency;
               poitem.mUnitPriceRecommended = wholesalePrice;
               poitem.mUnitPriceQuoted = costPrice;
               poitem.mStkCode = itemCode;
               poitem.mOutstandingQty = quantity;

					ItemObject itemObj = ItemNut.getValueObjectByCode(itemCode);

					if(itemObj==null)
					{
						itemObj = new ItemObject();
						itemObj.code = itemCode;
						itemObj.name = itemName;
						itemObj.priceList = retailPrice;
						itemObj.priceSale = wholesalePrice;
						itemObj.priceMin = minPrice;
						itemObj.maUnitCost = costPrice;
						itemObj.lastUnitCost = costPrice;
						itemObj.replacementUnitCost = costPrice;
						itemObj.categoryId = itm_categoryid;
						itemObj.category1 = itm_category1;
						itemObj.category2 = itm_category2;
						itemObj.category3 = itm_category3;
						itemObj.category4 = itm_category4;
						itemObj.category5 = itm_category5;
					
						Item itemEJB = ItemNut.fnCreate(itemObj);
					}
					else
					{
						Item itemEJB = ItemNut.getHandle(itemObj.pkid);
                  itemObj.code = itemCode;
                  itemObj.name = itemName;
                  itemObj.priceList = retailPrice;
                  itemObj.priceSale = wholesalePrice;
                  itemObj.priceMin = minPrice;
						itemObj.maUnitCost = costPrice;
						itemObj.lastUnitCost = costPrice;
						itemObj.replacementUnitCost = costPrice;
                  itemObj.categoryId = itm_categoryid;
                  itemObj.category1 = itm_category1;
                  itemObj.category2 = itm_category2;
                  itemObj.category3 = itm_category3;
                  itemObj.category4 = itm_category4;
                  itemObj.category5 = itm_category5;
						itemEJB.setObject(itemObj);
					}

               poitem.mPurchaseItemType = itemObj.itemType1;
               poitem.mPurchaseItemId = itemObj.pkid;
					poitem.mItemId = itemObj.pkid;
					poitem.mName = itemObj.name;
					poObj.vecPurchaseOrderItems.add(poitem);

					prevInvoice = externalInvoiceNo;
            } 
				catch (Exception ex) 
				{
               ex.printStackTrace();
            }
         }// end while

			if(poObj.vecPurchaseOrderItems.size()>0)
         {
                  PurchaseOrder poEJB = PurchaseOrderNut.fnCreate(poObj);
                  Log.printVerbose("----------------------------------------------------");
                  Log.printVerbose("LASSSSSSSSSSSSSSSSSSSSSSSSTTTT");
                  Log.printVerbose("CREATED PURCHASE ORDER : "+poObj.mPkid.toString());
                  Log.printVerbose("----------------------------------------------------");
						poObj = new PurchaseOrderObject();
         }



		}


	private void fnAdjustMA(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when marking down the stocks..... some of the questions that pop up are:

		/// 4. If the source item code does not exist, throw an exception / or output the error message

		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		Vector vecBranch = BranchNut.getValueObjectsGiven(
                     BranchBean.STATUS,
                     BranchBean.STATUS_ACTIVE,
                     (String)null, (String)null);
		Integer iPCC = new Integer(1); /// default PCC primary key
      ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(new Integer(1));
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{
				BigDecimal totalQty = new BigDecimal(0);
				BigDecimal currentCost = new BigDecimal(0);
				BigDecimal bdSourcePrice = new BigDecimal(0);
				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String sourceItem = token[3].trim();
				String sourcePrice = token[4].trim();
				String targetItem = token[5].trim();
				String targetSalePrice = token[6].trim();

				Log.printVerbose(" ............................ MARKING DOWN ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+sourceItem);
				Log.printVerbose(" SOURCE PRICE:"+sourcePrice);
				Log.printVerbose(" TARGET ITEM :"+targetItem);
				Log.printVerbose(" TARGET PRICE:"+targetSalePrice);

				bdSourcePrice = new BigDecimal(sourcePrice);
				bdSourcePrice = bdSourcePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				ItemObject itemObjSource = ItemNut.getValueObjectByCode(sourceItem);

				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}


	for(int cnt2=0;cnt2<vecBranch.size();cnt2++)
	{
				BranchObject branchObj = (BranchObject) vecBranch.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,
									branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}


         	try
         	{

					if(stkObjSource.accPCCenterId.equals(iPCC) && stkObjSource.balance.signum()!=0)
					{
						totalQty = totalQty.add(stkObjSource.balance);
						currentCost = stkObjSource.unitCostMA;
						stkObjSource.unitCostMA = bdSourcePrice;
						stkEjbSource.setObject(stkObjSource);

						StockAdjustmentObject stkAdj = new StockAdjustmentObject();
						//stkAdj.tx_code = ""; // varchar(50)
						stkAdj.tx_type = StockAdjustmentBean.TYPE_RESET_MA;
						//stkAdj.tx_module = ""; // varchar(50),
						//stkAdj.tx_option = ""; // varchar(50),
						stkAdj.userid1 = userId;
						stkAdj.userid2 = userId;
						stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
						stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
						stkAdj.src_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.src_branch = new Integer(0); // integer,
						stkAdj.src_location = stkObjSource.locationId;
						stkAdj.src_currency = pccObj.mCurrency;
						stkAdj.src_price1 = currentCost;
						stkAdj.src_qty1 = stkObjSource.balance;
						stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
						stkAdj.src_item_id = stkObjSource.itemId;
						stkAdj.src_item_code = itemObjSource.code;
						stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
						stkAdj.tgt_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.tgt_branch = new Integer(0); // integer,
						stkAdj.tgt_location = stkObjSource.locationId;
						stkAdj.tgt_currency = pccObj.mCurrency;
						stkAdj.tgt_price1 = bdSourcePrice;
						stkAdj.tgt_qty1 = stkObjSource.balance;
						stkAdj.tgt_serialized = itemObjSource.serialized;
						stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
						stkAdj.tgt_item_id = stkObjSource.itemId;
						stkAdj.tgt_item_code = itemObjSource.code;
						stkAdj.tgt_item_name = itemObjSource.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
						stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --
						StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

            }
         } catch (Exception ex)
         {
            ex.printStackTrace();
         }
	}// end of vecBranch loop

      if(bdSourcePrice.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
         String description = " MA-RESET: ITEMCODE:" + itemObjSource.code;
         description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdSourcePrice)
               + " ";
         description += " QTY: " + CurrencyFormat.strInt(totalQty);
         BigDecimal variance = bdSourcePrice.subtract(currentCost).multiply(totalQty);
         variance = new BigDecimal(CurrencyFormat.strCcy(variance));
         JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, " AUTO MARKDOWN BY UPLOADING FILE", userId);
      }

			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnMarkdownPrices







}

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

import java.io.*;
import java.math.*;
import java.util.*;
import java.sql.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.accounting.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoUploadCatItem implements Action // extends HttpServlet
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, 
								HttpServletResponse res) 
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("footwear-upload-cat-item-page");
		} 
		else
		{
			try
			{
				fnProcessInputFiles(req, res);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return new ActionRouter("footwear-upload-cat-item-page");
	}

	private void fnProcessInputFiles(HttpServletRequest req, HttpServletResponse res) 
					throws Exception
	{
		HttpSession session = req.getSession();
		Integer userId = (Integer) session.getAttribute("userId");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload fu = new ServletFileUpload(factory);
		fu.setSizeMax(50000000);
		List fileItems = fu.parseRequest(req);
		Iterator itr = fileItems.iterator();
		while (itr.hasNext())
		{
			FileItem fi = (FileItem) itr.next();
			if (!fi.isFormField() && fi.getFieldName().equals("categoryfile"))
			{
				fnProcessCategoryFile(fi);
			}/// end if categoryfile

 
			if (!fi.isFormField() && fi.getFieldName().equals("itemfile"))
			{
				fnProcessItemFile(fi);
			}// end if itemfile

			if (!fi.isFormField() && fi.getFieldName().equals("liquidatingItems"))
			{
				fnProcessLiquidationFile(fi, userId);
			}// end if liquidatingItems

			if(!fi.isFormField() && fi.getFieldName().equals("createPurchaseOrder"))
			{
				fnCreatePurchaseOrder(fi,userId);
			}// end if createPurchaseOrder

			if(!fi.isFormField() && fi.getFieldName().equals("markdownPrices"))
			{
				fnMarkdownPrices(fi,userId);
			}// end if createPurchaseOrder

		}/// END WHILE itr.hasNext()
	}// END fnUploadItem


	private void fnProcessCategoryFile(FileItem fileItem)
		throws Exception
	{
					InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
					BufferedReader in = new BufferedReader(inputStreamReader);
					LineNumberReader ln = new LineNumberReader(in);
					String line;
					while ((line = ln.readLine()) != null)
					{
						try
						{
							StringTokenizer sb = new StringTokenizer(line, "|");
							CategoryTreeObject obj = new CategoryTreeObject();
							sb.nextToken();
							obj.code = sb.nextToken().trim();
							obj.name = sb.nextToken().trim();
							obj.sort = "0";
							obj.description = sb.nextToken().trim();
							if (obj.code.length() < 4)
								obj.catLevel = new Integer(2);
							else
								obj.catLevel = new Integer(3);
							CategoryTreeHome cattreehome = CategoryTreeNut.getHome();
							QueryObject query = new QueryObject(new String[] { CategoryTreeBean.CODE + " = '" + obj.code + "' " });
							Collection coll = CategoryTreeNut.getObjects(query);
							if (coll.isEmpty() || coll.size() == 0)
							{
								cattreehome.create(obj);
							} else
							{
								CategoryTreeObject cattree = (CategoryTreeObject) coll.iterator().next();
								CategoryTree categoryEJB = CategoryTreeNut.getHandle(cattree.pkid);
								obj.pkid = cattree.pkid;
								obj.parentId = cattree.parentId;
								obj.sort = cattree.sort;
								categoryEJB.setObject(obj);
							}
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}


	}

	private void fnProcessItemFile(FileItem fileItem)
		throws Exception
	{
		///item code(0)	| item name	(1) | Item description (2) | 	uom (3)	| cat code (4) 
///			|		w/s price (5) | 	retail price (6) | 	min price (7) |	cost price (8) 



					InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
					BufferedReader in = new BufferedReader(inputStreamReader);
					LineNumberReader ln = new LineNumberReader(in);
					String line;
					while ((line = ln.readLine()) != null)
					{
						try
						{
							ItemObject itmObj = new ItemObject();
							String token[] = line.split("[|]");

							itmObj.code = token[0].trim();
							itmObj.name = token[1].trim();
							itmObj.description = token[2].trim();
							itmObj.uom = token[3].trim();

							String categorycode = token[4].trim();

							BigDecimal wholesalePrice = new BigDecimal(token[5].trim());
							wholesalePrice = wholesalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal retailPrice = new BigDecimal(token[6].trim());
							retailPrice = retailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal minPrice = new BigDecimal(token[7].trim());
							minPrice = minPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
							BigDecimal costPrice = new BigDecimal(token[8].trim());
							costPrice = costPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

							itmObj.categoryId = new Integer(0);
							itmObj.category2 = categorycode.substring(0, 2);
							itmObj.category3 = categorycode;
							if(new Integer(categorycode).intValue() >= new Integer("0100").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("0999").intValue())
							{ itmObj.category1 = "MEN"; itmObj.categoryId = new Integer(1001);}
							else if(new Integer(categorycode).intValue() >= new Integer("1000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("1999").intValue())
							{ itmObj.category1 = "LADIES";itmObj.categoryId = new Integer(1002);}
							else if(new Integer(categorycode).intValue() >= new Integer("2000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("2999").intValue())
							{ itmObj.category1 = "CHILDREN"; itmObj.categoryId = new Integer(1003);}
							else if(new Integer(categorycode).intValue() >= new Integer("3000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("3300").intValue())
							{ itmObj.category1 = "CANVAS"; itmObj.categoryId = new Integer(1004);}
							else if(new Integer(categorycode).intValue() >= new Integer("3300").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("3999").intValue())
							{ itmObj.category1 = "ATHLETIC"; itmObj.categoryId = new Integer(1005);}
							else if(new Integer(categorycode).intValue() >= new Integer("4000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("4999").intValue())
							{ itmObj.category1 = "OTHERS"; itmObj.categoryId = new Integer(1006);}
							else if(new Integer(categorycode).intValue() >= new Integer("5000").intValue() && 
									new Integer(categorycode).intValue() <= new Integer("6999").intValue())
							{ itmObj.category1 = "NON-FOOTWEAR"; itmObj.categoryId = new Integer(1007);}
							itmObj.category4 = "";
							itmObj.category5 = "";

							itmObj.status = ItemBean.STATUS_ACTIVE;
							itmObj.priceList = retailPrice;
							itmObj.priceSale = wholesalePrice;
							itmObj.priceMin = minPrice;
							itmObj.replacementUnitCost = costPrice;

							Item item = ItemNut.getObjectByCode(itmObj.code);

							if(item==null)
							{
								item = ItemNut.fnCreate(itmObj);
							} 
							else 
							{
								//ItemObject item = (ItemObject) coll.iterator().next();
								ItemObject itmObj2 = item.getObject();
								itmObj2.name = itmObj.name;
								itmObj2.description = itmObj.description;
								itmObj2.uom = itmObj.uom;
								itmObj2.categoryId = itmObj.categoryId;	
								itmObj2.category1 = itmObj.category1;
								itmObj2.category2 = itmObj.category2;
								itmObj2.category3 = itmObj.category3;
								itmObj2.category4 = itmObj.category4;
								itmObj2.category5 = itmObj.category5;
								itmObj2.priceList = itmObj.priceList;
								itmObj2.priceSale = itmObj.priceSale;
								itmObj2.priceMin = itmObj.priceMin;
								itmObj2.replacementUnitCost = itmObj.replacementUnitCost;

								item.setObject(itmObj2);
							}
						} 
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}

	}





	//////////////////////////////////////////////////////////////////////////////////////////////
	///// PROCESS LIQUIDATION FUNCTION
	//////////////////////////////////////////////////////////////////////////////////////////////
	private void fnProcessLiquidationFile(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when liquidating stocks..... some of the questions that pop up are:
//		circular # (0)	| effective date(1)	| week-no (2)|  old item code (3)| 	new item code(4) | new w/s price	(5)
//						| new retail price (6) | new min price (7) | new cost price (8) | new item name (9)| new cat code(10)

		/// 1. Make sure the source and target item codes share the same cost price
				//// otherwise skip.
		/// 2. Only stocks belong to the location specified are liquidated
		/// 3. If the target item codes does not exist, create one
		/// 4. If the source item code does not exist, throw an exception / or output the error message


		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		Vector vecBranch = BranchNut.getValueObjectsGiven(
                     BranchBean.STATUS,
                     BranchBean.STATUS_ACTIVE,
                     (String)null, (String)null);
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{

	for(int cnt2=0;cnt2<vecBranch.size();cnt2++)
	{
				BranchObject branchObj = (BranchObject) vecBranch.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
//		circular # (0)	| effective date(1)	| week-no (2)|  old item code (3)| 	new item code(4) | new w/s price	(5)
//						| new retail price (6) | new min price (7) | new cost price (8) | new item name (9)| new cat code(10)
				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String sourceItem = token[3].trim();
				String targetItem = token[4].trim();
				String targetSalePrice = token[5].trim();
				String targetRetailPrice = token[6].trim();
				String targetMinPrice = token[7].trim();
				String targetCostPrice = token[8].trim();
				String targetItemName = token[9].trim();
				String targetItemCatCode = token[10].trim();

				Log.printVerbose(" ............................ LIQUIDATING ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+sourceItem);
				Log.printVerbose(" TARGET ITEM :"+targetItem);
				Log.printVerbose(" TARGET SALE PRICE:"+targetSalePrice);
				Log.printVerbose(" TARGET RETAIL PRICE:"+targetRetailPrice);
				Log.printVerbose(" TARGET MIN PRICE:"+targetMinPrice);
				Log.printVerbose(" TARGET COST PRICE:"+targetCostPrice);
				Log.printVerbose(" TARGET ITEM NAME :"+targetItemName);
				Log.printVerbose(" TARGET ITEM CAT CODE :"+targetItemCatCode);

				if(targetItem.trim().length()<=0){ continue;}
				if(targetSalePrice.trim().length()<=0){ continue;}

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				BigDecimal bdSourcePrice = new BigDecimal(targetSalePrice);
				bdSourcePrice = bdSourcePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				BigDecimal bdTargetSalePrice = new BigDecimal(targetSalePrice);
				bdTargetSalePrice = bdTargetSalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				BigDecimal bdTargetRetailPrice = new BigDecimal(targetRetailPrice);
				bdTargetRetailPrice = bdTargetRetailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdTargetMinPrice = new BigDecimal(targetMinPrice);
				bdTargetMinPrice = bdTargetMinPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdTargetCostPrice = new BigDecimal(targetCostPrice);
				bdTargetCostPrice = bdTargetCostPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);


				ItemObject itemObjSource = ItemNut.getValueObjectByCode(sourceItem);
				ItemObject itemObjTarget = ItemNut.getValueObjectByCode(targetItem);

				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}

				/// if(itemObjTarget==null).... CREATE A NEW ONE!!
				if(itemObjTarget==null)
				{
					itemObjTarget = new ItemObject();
					itemObjTarget.code = targetItem;
					itemObjTarget.name = "LIQUIDATED ITEM - "+targetSalePrice;
					itemObjTarget.description = "AUTO CREATED ON "+TimeFormat.strDisplayDate();
					itemObjTarget.priceList = bdTargetRetailPrice;
					itemObjTarget.priceSale = bdTargetSalePrice;
					itemObjTarget.priceDisc1 = bdTargetSalePrice;
					itemObjTarget.priceDisc2 = bdTargetSalePrice;
					itemObjTarget.priceDisc3 = bdTargetSalePrice;
					itemObjTarget.priceMin = bdTargetMinPrice;
//					itemObjTarget.fifoUnitCost = bdTargetPrice;
					itemObjTarget.maUnitCost = bdTargetCostPrice;
//					itemObjTarget.waUnitCost = bdTargetPrice;
//					itemObjTarget.lastUnitCost = bdTargetPrice;
					itemObjTarget.replacementUnitCost = bdTargetCostPrice;


							itemObjTarget.categoryId = new Integer(0);
							itemObjTarget.category2 = targetItemCatCode.substring(0, 2);
							itemObjTarget.category3 = targetItemCatCode;
							if(new Integer(targetItemCatCode).intValue() >= new Integer("0100").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("0999").intValue())
							{ itemObjTarget.category1 = "MEN"; itemObjTarget.categoryId = new Integer(1001);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("1000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("1999").intValue())
							{ itemObjTarget.category1 = "LADIES";itemObjTarget.categoryId = new Integer(1002);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("2000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("2999").intValue())
							{ itemObjTarget.category1 = "CHILDREN"; itemObjTarget.categoryId = new Integer(1003);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("3000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("3300").intValue())
							{ itemObjTarget.category1 = "CANVAS"; itemObjTarget.categoryId = new Integer(1004);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("3300").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("3999").intValue())
							{ itemObjTarget.category1 = "ATHLETIC"; itemObjTarget.categoryId = new Integer(1005);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("4000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("4999").intValue())
							{ itemObjTarget.category1 = "OTHERS"; itemObjTarget.categoryId = new Integer(1006);}
							else if(new Integer(targetItemCatCode).intValue() >= new Integer("5000").intValue() && 
									new Integer(targetItemCatCode).intValue() <= new Integer("6999").intValue())
							{ itemObjTarget.category1 = "NON-FOOTWEAR"; itemObjTarget.categoryId = new Integer(1007);}
							itemObjTarget.category4 = "";
							itemObjTarget.category5 = "";

					ItemNut.fnCreate(itemObjTarget);
				}

				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				Stock stkEjbTarget = StockNut.getObjectBy(itemObjTarget.pkid,branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				StockObject stkObjTarget = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}

					if(stkEjbTarget!=null)
					{ stkObjTarget = stkEjbTarget.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}




				/// if stkEjbSource ==null... nothing to liquidate... skip this!!
				if(stkObjSource==null)
				{
					Log.printVerbose(" SOURCE LOCATION DOES NOT HAVE ANY STOCK.. ");
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"...  AT "+branchObj.description+" !");
					continue;
				}

				if(stkObjSource.balance.signum()==0)
				{
					Log.printVerbose(" SOURCE LOCATION HAS ZERO STOCK ... ");
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"...  AT "+branchObj.description+" !");
					continue;
				}


				/// if itemObjSource == itemObjTarget.. skip the liquidation
				if(itemObjSource.pkid.equals(itemObjTarget.pkid))
				{ 
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+" ... SAME ITEM CODE AS TARGET ITEM CODE ..");
					continue;
				}

				/// make sure the sourcePrice and targetPrice are the same
				if(bdSourcePrice.compareTo(bdTargetSalePrice)!=0)
				{
					Log.printVerbose(" The Source Item Cost is different from target Item Cost!");
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... SRC PRICE="+bdSourcePrice.toString()+" ... TGT PRICE="+targetSalePrice);
					continue;
				}

				/// check the stkSource and stkTarget should have same average costs
				if(stkObjTarget!=null)
				{
					if(stkObjTarget.unitCostMA.compareTo(stkObjSource.unitCostMA)!=0)
					{
						Log.printVerbose(" The current cost of the TARGET ITEM is different from the Target Price!");
						Log.printVerbose(" TGT ITEM CODE = "+targetItem);
						Log.printVerbose(" SKIPPING LINE "+lineCount+" ... SRC PRICE="+bdSourcePrice.toString()+" ... TGT PRICE="+targetSalePrice);
						continue;
					}
				}

				///////////////////////////////////////////////////////////////////////
				/// LIQUIDATION IN ACTION!!

				/// create a inv-stock-delta for source and target stock
				/// update the balance of the stock
				BigDecimal qtyDelta = stkObjSource.balance;
				{/// SOURCE ITEM ... OUT
					Vector vecSerialObj = new Vector();
					String remarks ="CircularNo:"+circularNo+" WeekNo:"+weekNo+ 
														" Liquidated to "+targetItem+" at "+targetSalePrice;
					StockNut.out(userId, itemObjSource.pkid, branchObj.invLocationId, branchObj.accPCCenterId,
         			qtyDelta, stkObjSource.unitCostMA, branchObj.currency, "", new Long(0), remarks,
         			tsDate, userId, StockDeltaBean.NS_INVENTORY, StockDeltaBean.TT_ADJUSTMENT, vecSerialObj);
				}

				{
					Vector vecSerialObj = new Vector();
					String remarks = "CircularNo:"+circularNo+" WeekNo:"+weekNo+ 
														" Liquidated from "+sourceItem+" at "+CurrencyFormat.strCcy(bdSourcePrice);
					StockNut.in(userId, itemObjTarget.pkid, branchObj.invLocationId, branchObj.accPCCenterId,
         			qtyDelta, stkObjSource.unitCostMA, branchObj.currency, "", new Long(0), remarks,
         			tsDate, userId, StockDeltaBean.NS_INVENTORY, StockDeltaBean.TT_ADJUSTMENT, vecSerialObj);
				}


				/// no changes at the journal transaction

				/// create audit trail

				/// create inv_stock_adjustment entries
               StockAdjustmentObject stkAdj = new StockAdjustmentObject();
               //stkAdj.tx_code = ""; // varchar(50)
					stkAdj.tx_type = StockAdjustmentBean.TYPE_LIQUIDATE_CONVERSION;
               //stkAdj.tx_module = ""; // varchar(50),
               //stkAdj.tx_option = ""; // varchar(50),
               stkAdj.userid1 = userId;
               stkAdj.userid2 = userId;
               stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
               stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
               //stkAdj.remarks2 = ""; // varchar(100),
               stkAdj.src_pccenter = branchObj.accPCCenterId;
					stkAdj.src_branch = branchObj.pkid;
               stkAdj.src_location = branchObj.invLocationId;
               stkAdj.src_currency = branchObj.currency;
               stkAdj.src_price1 = stkObjSource.unitCostMA;
               stkAdj.src_qty1 = qtyDelta.negate();
               stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
               stkAdj.src_item_id = stkObjSource.itemId;
               stkAdj.src_item_code = itemObjSource.code;
               stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
               stkAdj.tgt_pccenter = branchObj.accPCCenterId;
					stkAdj.tgt_branch = branchObj.pkid;
               stkAdj.tgt_location = branchObj.invLocationId;
               stkAdj.tgt_currency = branchObj.currency;
               stkAdj.tgt_price1 = bdTargetSalePrice;
               stkAdj.tgt_qty1 = stkAdj.src_qty1.negate();
               stkAdj.tgt_serialized = itemObjTarget.serialized;
               stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
               stkAdj.tgt_item_id = stkObjTarget.itemId;
               stkAdj.tgt_item_code = itemObjTarget.code;
               stkAdj.tgt_item_name = itemObjTarget.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
               stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --

					StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

	}// end of vecBranch loop

			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnProcessLiquidationFile


	//////////////////////////////////////////////////////////////////////////////////////////
	//// MARKING DOWN PRICES
	//////////////////////////////////////////////////////////////////////////////////////////

	private void fnMarkdownPrices(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when marking down the stocks..... some of the questions that pop up are:

		/// 4. If the source item code does not exist, throw an exception / or output the error message

		// CIRCULAR-NO | DATE       | WEEK-NO | ITEM CODE  | NEW W/S PRICE | NEW RETAIL PRICE| NEW MIN PRICE | NEW COST
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		Vector vecBranch = BranchNut.getValueObjectsGiven(
                     BranchBean.STATUS,
                     BranchBean.STATUS_ACTIVE,
                     (String)null, (String)null);
		Integer iPCC = new Integer(1); /// default PCC primary key
      ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(new Integer(1));
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{
				BigDecimal totalQty = new BigDecimal(0);

				BigDecimal currentSalePrice = new BigDecimal(0);
				BigDecimal currentCost = new BigDecimal(0);

				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String itemCode = token[3].trim();
				String newSalePrice = token[4].trim();
				String newRetailPrice = token[5].trim();
				String newMinPrice = token[6].trim();
				String newCostPrice = token[7].trim();

				Log.printVerbose(" ............................ MARKING DOWN ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+itemCode);
				Log.printVerbose(" WHOLESALE PRICE:"+newSalePrice);
				Log.printVerbose(" RETAIL PRICE:"+newRetailPrice);
				Log.printVerbose(" MIN PRICE:"+newMinPrice);
				Log.printVerbose(" COST PRICE:"+newCostPrice);

				BigDecimal bdSalePrice = new BigDecimal(newSalePrice);
				bdSalePrice = bdSalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdRetailPrice = new BigDecimal(newRetailPrice);
				bdRetailPrice = bdRetailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdMinPrice = new BigDecimal(newMinPrice);
				bdMinPrice = bdMinPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
				BigDecimal bdCostPrice = new BigDecimal(newCostPrice);
				bdCostPrice = bdCostPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				ItemObject itemObjSource = ItemNut.getValueObjectByCode(itemCode);
				currentSalePrice = itemObjSource.priceSale;
				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+itemCode+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}


	for(int cnt2=0;cnt2<vecBranch.size();cnt2++)
	{
				BranchObject branchObj = (BranchObject) vecBranch.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,
									branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}


         	try
         	{

					if(stkObjSource.accPCCenterId.equals(iPCC) && stkObjSource.balance.signum()!=0)
					{
						totalQty = totalQty.add(stkObjSource.balance);
						currentCost = stkObjSource.unitCostMA;
						stkObjSource.unitCostMA = bdCostPrice;
						stkObjSource.unitCostReplacement = bdSalePrice;
						stkEjbSource.setObject(stkObjSource);
				{
						StockAdjustmentObject stkAdj = new StockAdjustmentObject();
						//stkAdj.tx_code = ""; // varchar(50)
						stkAdj.tx_type = StockAdjustmentBean.TYPE_MARKDOWN;
						//stkAdj.tx_module = ""; // varchar(50),
						//stkAdj.tx_option = ""; // varchar(50),
						stkAdj.userid1 = userId;
						stkAdj.userid2 = userId;
						stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
						stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
						stkAdj.src_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.src_branch = new Integer(0); // integer,
						stkAdj.src_location = stkObjSource.locationId;
						stkAdj.src_currency = pccObj.mCurrency;
						stkAdj.src_price1 = currentSalePrice;
						stkAdj.src_qty1 = stkObjSource.balance;
						stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
						stkAdj.src_item_id = stkObjSource.itemId;
						stkAdj.src_item_code = itemObjSource.code;
						stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
						stkAdj.tgt_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.tgt_branch = new Integer(0); // integer,
						stkAdj.tgt_location = stkObjSource.locationId;
						stkAdj.tgt_currency = pccObj.mCurrency;
						stkAdj.tgt_price1 = bdSalePrice;
						stkAdj.tgt_qty1 = stkObjSource.balance;
						stkAdj.tgt_serialized = itemObjSource.serialized;
						stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
						stkAdj.tgt_item_id = stkObjSource.itemId;
						stkAdj.tgt_item_code = itemObjSource.code;
						stkAdj.tgt_item_name = itemObjSource.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
						stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --
						StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);
				}
            	}
         	} catch (Exception ex)
         	{
            ex.printStackTrace();
         	}
			}// end of vecBranch loop

      if(bdCostPrice.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
         String description = " MA-RESET: ITEMCODE:" + itemObjSource.code;
         description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdCostPrice)
               + " ";
         description += " QTY: " + CurrencyFormat.strInt(totalQty);
         BigDecimal variance = bdCostPrice.subtract(currentCost).multiply(totalQty);
         variance = new BigDecimal(CurrencyFormat.strCcy(variance));
         JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, 
							" AUTO MARKDOWN BY UPLOADING FILE", userId);
      }
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnMarkdownPrices







	private void fnCreatePurchaseOrder(FileItem fileItem, Integer userId)
		throws Exception
	{
// OLD
//Branch-code  |KLANG DEPOT    |reference no |external Invoice No |date  |item code  |quantity |wholesale price|retail price
//62017   		|KLANG DEPOT    |00680  		|00680  	|	02009					|060111|8614203-090| 00120   |00000974			|00001299

// NEW
//Branch-code  |KLANG DEPOT    |reference no |external Invoice No |date  |item code  |quantity |wholesale price|retail price | Min Selling Price | Cost Price | Item Category | Item Name
//62017   		|KLANG DEPOT    |00680  		|02009					|060111|8614203-090| 00120   |00000974			|00001299     | 00000650          |  00000440 | 2001          | T-BAR



			InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
			BufferedReader in = new BufferedReader(inputStreamReader);
         LineNumberReader ln = new LineNumberReader(in);

         String prevInvoice = "";
         String line = null;
         BigDecimal price = new BigDecimal(0);
         PurchaseOrderObject poObj= new PurchaseOrderObject();
         PurchaseOrderItemObject poitem = new PurchaseOrderItemObject();;

			int count =0;

         while ((line = ln.readLine()) != null) 
			{
				count++;
            try 
				{

					Log.printVerbose("----------------------------------");
					Log.printVerbose("PROCESSING LINE :"+count);

					String token[] = line.split("[|]");
					String branchCode = token[0].trim();
					String branchName = token[1].trim();
					String referenceNo = token[2].trim();
					String externalInvoiceNo = token[3].trim();
					String theRawDate = token[4].trim();
					String itemCode = token[5].trim();
					BigDecimal quantity = new BigDecimal(token[6].trim());


					BigDecimal wholesalePrice = new BigDecimal(token[7].trim());
					wholesalePrice = wholesalePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal retailPrice = new BigDecimal(token[8].trim());
					retailPrice = retailPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal minPrice = new BigDecimal(token[9].trim());
					minPrice = minPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
					BigDecimal costPrice = new BigDecimal(token[10].trim());
					costPrice = costPrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

               String categorycode = token[11].trim();
					String itemName = token[12].trim();

					Integer itm_categoryid = new Integer(1000);
					String itm_category1 = "";
               String itm_category2 = categorycode.substring(0, 2);
               String itm_category3 = categorycode;
               String itm_category4 = "";
               String itm_category5 = "";
					
               if(new Integer(categorycode).intValue() >= new Integer("0100").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("0999").intValue())
					{ itm_category1 = "MEN"; itm_categoryid = new Integer(1001); }
               else if(new Integer(categorycode).intValue() >= new Integer("1000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("1999").intValue())
					{ itm_category1 = "LADIES"; itm_categoryid = new Integer(1002);}
               else if(new Integer(categorycode).intValue() >= new Integer("2000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("2999").intValue())
					{ itm_category1 = "CHILDREN"; itm_categoryid = new Integer(1003);}
               else if(new Integer(categorycode).intValue() >= new Integer("3000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("3300").intValue())
					{ itm_category1 = "CANVAS"; itm_categoryid = new Integer(1004);}
               else if(new Integer(categorycode).intValue() >= new Integer("3300").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("3999").intValue())
					{ itm_category1 = "ATHLETIC"; itm_categoryid = new Integer(1005);}
               else if(new Integer(categorycode).intValue() >= new Integer("4000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("4999").intValue())
					{ itm_category1 = "OTHERS"; itm_categoryid = new Integer(1006);}
               else if(new Integer(categorycode).intValue() >= new Integer("5000").intValue() &&
                     new Integer(categorycode).intValue() <= new Integer("6999").intValue())
					{ itm_category1 = "NON-FOOTWEAR"; itm_categoryid = new Integer(1007);}

               String a1_year = "20" + theRawDate.substring(0, 2);
               String a1_month = theRawDate.substring(2, 4);
               String a1_day = theRawDate.substring(4);
			
					Timestamp theDate = TimeFormat.createTimestamp(a1_year+"-"+a1_month+"-"+a1_day);



					if( (!prevInvoice.equals(externalInvoiceNo)) && poObj.vecPurchaseOrderItems.size()>0)
					{

						PurchaseOrder poEJB = PurchaseOrderNut.fnCreate(poObj);
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("CREATED PURCHASE ORDER : "+poObj.mPkid.toString());
						Log.printVerbose("----------------------------------------------------");
						Log.printVerbose("----------------------------------------------------");
						poObj = new PurchaseOrderObject();
					}


               Branch branchEJB= BranchNut.getObjectByCode(branchCode);
               if(branchEJB ==null)
					{ Log.printVerbose(" NO SUCH BRANCH EXIST!!!"+ branchCode); continue;}
					BranchObject branchObj = branchEJB.getObject();

					poObj.mTimeCreated = theDate ;
//					poObj.mTimeComplete;
					poObj.mRequestorId = userId;
					poObj.mApproverId = userId;
					poObj.mCurrency = branchObj.currency;
					poObj.mRemarks = " INVOICE:"+externalInvoiceNo+ " REF:"+referenceNo;
   				poObj.mUserIdUpdate = userId;
   				poObj.mEntityTable = SuppAccountBean.TABLENAME ;
					poObj.mEntityKey = SuppAccountBean.ONE_TIME_SUPPLIER_PKID;
					poObj.mEntityName = "KLANG HQ";
   				poObj.mSuppProcCtrId = branchObj.pkid;
					poObj.mLocationId = branchObj.invLocationId;
					poObj.mPCCenter = branchObj.accPCCenterId;
   				poObj.mReferenceNo = "INV"+externalInvoiceNo;


               poitem = new PurchaseOrderItemObject();
               //poitem.mPurchaseOrderId = obj.mPkid;
               //poitem.mRemarks = poObj.mRemarks;
               poitem.mTotalQty = quantity;
               poitem.mCurrency = poObj.mCurrency;
               poitem.mUnitPriceRecommended = wholesalePrice;
               poitem.mUnitPriceQuoted = costPrice;
               poitem.mStkCode = itemCode;
               poitem.mOutstandingQty = quantity;

					ItemObject itemObj = ItemNut.getValueObjectByCode(itemCode);

					if(itemObj==null)
					{
						itemObj = new ItemObject();
						itemObj.code = itemCode;
						itemObj.name = itemName;
						itemObj.priceList = retailPrice;
						itemObj.priceSale = wholesalePrice;
						itemObj.priceMin = minPrice;
						itemObj.maUnitCost = costPrice;
						itemObj.lastUnitCost = costPrice;
						itemObj.replacementUnitCost = costPrice;
						itemObj.categoryId = itm_categoryid;
						itemObj.category1 = itm_category1;
						itemObj.category2 = itm_category2;
						itemObj.category3 = itm_category3;
						itemObj.category4 = itm_category4;
						itemObj.category5 = itm_category5;
					
						Item itemEJB = ItemNut.fnCreate(itemObj);
					}
					else
					{
						Item itemEJB = ItemNut.getHandle(itemObj.pkid);
                  itemObj.code = itemCode;
                  itemObj.name = itemName;
                  itemObj.priceList = retailPrice;
                  itemObj.priceSale = wholesalePrice;
                  itemObj.priceMin = minPrice;
						itemObj.maUnitCost = costPrice;
						itemObj.lastUnitCost = costPrice;
						itemObj.replacementUnitCost = costPrice;
                  itemObj.categoryId = itm_categoryid;
                  itemObj.category1 = itm_category1;
                  itemObj.category2 = itm_category2;
                  itemObj.category3 = itm_category3;
                  itemObj.category4 = itm_category4;
                  itemObj.category5 = itm_category5;
						itemEJB.setObject(itemObj);
					}

               poitem.mPurchaseItemType = itemObj.itemType1;
               poitem.mPurchaseItemId = itemObj.pkid;
					poitem.mItemId = itemObj.pkid;
					poitem.mName = itemObj.name;
					poObj.vecPurchaseOrderItems.add(poitem);

					prevInvoice = externalInvoiceNo;
            } 
				catch (Exception ex) 
				{
               ex.printStackTrace();
            }
         }// end while

			if(poObj.vecPurchaseOrderItems.size()>0)
         {
                  PurchaseOrder poEJB = PurchaseOrderNut.fnCreate(poObj);
                  Log.printVerbose("----------------------------------------------------");
                  Log.printVerbose("LASSSSSSSSSSSSSSSSSSSSSSSSTTTT");
                  Log.printVerbose("CREATED PURCHASE ORDER : "+poObj.mPkid.toString());
                  Log.printVerbose("----------------------------------------------------");
						poObj = new PurchaseOrderObject();
         }



		}


	private void fnAdjustMA(FileItem fileItem, Integer userId)
		throws Exception
	{
		// FILE FORMAT
		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		/// when marking down the stocks..... some of the questions that pop up are:

		/// 4. If the source item code does not exist, throw an exception / or output the error message

		// CIRCULAR-NO | DATE       | WEEK-NO | SOURCE-ITEM | PRICE1 | TARGET-ITEM | PRICE2
		// 200601      | 2006-01-04 | 23/2005 | 0011312-010 | 39.00  | 1234567-999 | 35.00

		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;

		Vector vecBranch = BranchNut.getValueObjectsGiven(
                     BranchBean.STATUS,
                     BranchBean.STATUS_ACTIVE,
                     (String)null, (String)null);
		Integer iPCC = new Integer(1); /// default PCC primary key
      ProfitCostCenterObject pccObj = ProfitCostCenterNut.getObject(new Integer(1));
		int lineCount =0;
		while((line = ln.readLine()) != null)
		{
			lineCount++;
			try
			{
				BigDecimal totalQty = new BigDecimal(0);
				BigDecimal currentCost = new BigDecimal(0);
				BigDecimal bdSourcePrice = new BigDecimal(0);
				String token[] = line.split("[|]");
				if(token.length < 8){ continue;}
				String circularNo = token[0].trim();
				String strDate = token[1].trim();
				String weekNo = token[2].trim();
				String sourceItem = token[3].trim();
				String sourcePrice = token[4].trim();
				String targetItem = token[5].trim();
				String targetSalePrice = token[6].trim();

				Log.printVerbose(" ............................ MARKING DOWN ITEM!..................");		
				Log.printVerbose(" LINE "+lineCount);
				Log.printVerbose(" CIRCULAR NO:"+circularNo);
				Log.printVerbose(" STR-DATE :"+strDate);
				Log.printVerbose(" WEEK-NO :"+weekNo);
				Log.printVerbose(" SOURCE ITEM :"+sourceItem);
				Log.printVerbose(" SOURCE PRICE:"+sourcePrice);
				Log.printVerbose(" TARGET ITEM :"+targetItem);
				Log.printVerbose(" TARGET PRICE:"+targetSalePrice);

				bdSourcePrice = new BigDecimal(sourcePrice);
				bdSourcePrice = bdSourcePrice.divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);

				Timestamp tsDate = TimeFormat.createTimestamp(strDate);

				ItemObject itemObjSource = ItemNut.getValueObjectByCode(sourceItem);

				/// if(itemObjSource==null).... SKIP!!!
				if(itemObjSource==null)
				{
					Log.printVerbose(" SKIPPING LINE "+lineCount+" ... "+sourceItem+"... SOURCE ITEM CODE DOES NOT EXIST!");
					continue;
				}


	for(int cnt2=0;cnt2<vecBranch.size();cnt2++)
	{
				BranchObject branchObj = (BranchObject) vecBranch.get(cnt2);

//				StringTokenizer sb = new StringTokenizer(line, "|");
//				sb.nextToken();
				Stock stkEjbSource = StockNut.getObjectBy(itemObjSource.pkid,
									branchObj.invLocationId,new Integer(StockNut.STK_COND_GOOD));
				StockObject stkObjSource = null;
				try
				{
					if(stkEjbSource!=null)
					{ stkObjSource = stkEjbSource.getObject();}
				}
				catch(Exception ex)
				{ ex.printStackTrace();}


         	try
         	{

					if(stkObjSource.accPCCenterId.equals(iPCC) && stkObjSource.balance.signum()!=0)
					{
						totalQty = totalQty.add(stkObjSource.balance);
						currentCost = stkObjSource.unitCostMA;
						stkObjSource.unitCostMA = bdSourcePrice;
						stkEjbSource.setObject(stkObjSource);

						StockAdjustmentObject stkAdj = new StockAdjustmentObject();
						//stkAdj.tx_code = ""; // varchar(50)
						stkAdj.tx_type = StockAdjustmentBean.TYPE_RESET_MA;
						//stkAdj.tx_module = ""; // varchar(50),
						//stkAdj.tx_option = ""; // varchar(50),
						stkAdj.userid1 = userId;
						stkAdj.userid2 = userId;
						stkAdj.userid3 = userId;
               //stkAdj.entity_table = ""; // varchar(50),
               //stkAdj.entity_key = new Integer(0); // bigint,
               //stkAdj.reference = ""; // varchar(100),
               //stkAdj.description = ""; // varchar(100),
						stkAdj.remarks1 = "CircularNo:"+circularNo+ " WeekNo:"+weekNo;
						stkAdj.src_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.src_branch = new Integer(0); // integer,
						stkAdj.src_location = stkObjSource.locationId;
						stkAdj.src_currency = pccObj.mCurrency;
						stkAdj.src_price1 = currentCost;
						stkAdj.src_qty1 = stkObjSource.balance;
						stkAdj.src_serialized = itemObjSource.serialized;
               //stkAdj.src_remarks = ""; // varchar(500),
               //stkAdj.src_refdoc = ""; // varchar(50),
               //stkAdj.src_refkey = new Long(0); // bigint,
						stkAdj.src_item_id = stkObjSource.itemId;
						stkAdj.src_item_code = itemObjSource.code;
						stkAdj.src_item_name = itemObjSource.name;
               //stkAdj.src_item_remarks = ""; // varchar(500),
						stkAdj.tgt_pccenter = stkObjSource.accPCCenterId;
//             stkAdj.tgt_branch = new Integer(0); // integer,
						stkAdj.tgt_location = stkObjSource.locationId;
						stkAdj.tgt_currency = pccObj.mCurrency;
						stkAdj.tgt_price1 = bdSourcePrice;
						stkAdj.tgt_qty1 = stkObjSource.balance;
						stkAdj.tgt_serialized = itemObjSource.serialized;
						stkAdj.tgt_remarks = ""; // varchar(500),
               //stkAdj.tgt_refdoc = ""; // varchar(50),
               //stkAdj.tgt_refkey = new Long(0); // bigint,
						stkAdj.tgt_item_id = stkObjSource.itemId;
						stkAdj.tgt_item_code = itemObjSource.code;
						stkAdj.tgt_item_name = itemObjSource.name;
               //stkAdj.tgt_item_remarks = ""; // varchar(500),
               //stkAdj.property1 = ""; // varchar(100),
               //stkAdj.property2 = ""; // varchar(100),
               //stkAdj.property3 = ""; // varchar(100),
               //stkAdj.property4 = ""; // varchar(100),
               //stkAdj.property5 = ""; // varchar(100),
               //stkAdj.status = ""; // varchar(20),  -- RowStatus
						stkAdj.lastupdate = TimeFormat.getTimestamp(); // timestamp,  --
						StockAdjustment stkAdjEJB = StockAdjustmentNut.fnCreate(stkAdj);

            }
         } catch (Exception ex)
         {
            ex.printStackTrace();
         }
	}// end of vecBranch loop

      if(bdSourcePrice.compareTo(currentCost) != 0 && totalQty.signum()>0)
		{
         String description = " MA-RESET: ITEMCODE:" + itemObjSource.code;
         description += " MAPrice:" + CurrencyFormat.strCcy(currentCost) + "->" + CurrencyFormat.strCcy(bdSourcePrice)
               + " ";
         description += " QTY: " + CurrencyFormat.strInt(totalQty);
         BigDecimal variance = bdSourcePrice.subtract(currentCost).multiply(totalQty);
         variance = new BigDecimal(CurrencyFormat.strCcy(variance));
         JournalTxnLogic.fnCreateStockVariance(iPCC, pccObj.mCurrency, variance, description, " AUTO MARKDOWN BY UPLOADING FILE", userId);
      }

			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	} // end fnMarkdownPrices







}

