

package com.vlee.servlet.test;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.vlee.ejb.customer.*;
import com.vlee.ejb.inventory.*;
import com.vlee.servlet.main.*;
import com.vlee.util.*;

public class DoMigrateAllitItemcodes implements Action
{
	private static Task curTask = null;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		Log.printVerbose("... checkpoint 1");
		String formName = req.getParameter("formName");
		Log.printVerbose("... formName "+formName);
		if(formName==null)
		{
			return new ActionRouter("test-migrate-allit-itemcodes-page");
		}
		Log.printVerbose("... checkpoint 2");
	
		if(formName.equals("migrateItemCodes"))
		{
			try
			{ 
		Log.printVerbose("... checkpoint 3");
				fnProcessInputFiles(servlet,req,res);
			}
			catch(Exception ex)
			{ ex.printStackTrace(); }

		Log.printVerbose("... checkpoint 4");
		}	

		Log.printVerbose("... checkpoint 5");
		return new ActionRouter("test-migrate-allit-itemcodes-page");
	}

   private void fnProcessInputFiles(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
               throws Exception
   {
		Log.printVerbose("... A ...");
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
         if (!fi.isFormField() && fi.getFieldName().equals("itemCodeSourceFile"))
         {
            fnProcessItemCode(fi);
         }/// end if categoryfile
		}
	}

   private void fnProcessItemCode(FileItem fileItem)
      throws Exception
   {

		InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		LineNumberReader ln = new LineNumberReader(in);
		String line;
		int count = 0;
		while ((line = ln.readLine()) != null)
		{
			try
			{
				
				String token[] = line.split("[,]");
				if(token.length<=3){continue;}
				String itemCode = token[0].replaceAll("\"","").trim().toUpperCase();
				String itemCat5 = token[1].replaceAll("\"","").trim().toUpperCase();
				String itemName = token[2].replaceAll("\"","").trim();
				String itemDescription = token[3].replaceAll("\"","").trim();
				String itemCat1 = token[4].replaceAll("\"","").trim().toUpperCase();
				String itemCat2 = token[5].replaceAll("\"","").trim().toUpperCase();
				String itemListPrice = token[6].replaceAll("RM","");
				String itemSerialized = token[7].replaceAll("\"","");
				String itemCat4 = token[9].replaceAll("\"","").trim().toUpperCase();
//				String itemCost = token[6].replaceAll("RM","");

				count++;
				Log.printVerbose(" Processing line "+count);
				Log.printVerbose(" Details: "+itemCode+" "+itemName+" "+itemCat1+" "+itemListPrice+" "+itemSerialized);

				if(itemCode.length()<3){continue;}
			
				/// check if the item code exists
				ItemObject itemObj = ItemNut.getValueObjectByCode(itemCode);
				/// check if CATEGORY LEVEL 0 CHECKING
				Category lCategory = CategoryNut.getObjectByCode(itemCat1);
				Timestamp tsNow = TimeFormat.getTimestamp();
				if(lCategory == null )
				{
					try
					{
						Log.printVerbose("Adding new Category");
						CategoryHome lCategoryH = CategoryNut.getHome();
            		lCategory = (Category) lCategoryH.create(itemCat1, itemCat1, itemCat1, tsNow, new Integer(500));
         		} 
					catch(Exception ex)
         		{
						ex.printStackTrace();
						Log.printDebug("Cannot create Category " + ex.getMessage());
					}
				}
				CategoryObject lCategoryObj = lCategory.getObject();

				/// check if CATEGORY LEVEL 1 CHECKING
				QueryObject queryCat1 = new QueryObject(new String[]{
										CategoryTreeBean.CODE + " = '"+itemCat1+"' ",
               					CategoryTreeBean.CAT_LEVEL +" ='1' " });
				queryCat1.setOrder(" ORDER BY "+ CategoryTreeBean.SORT+", "
                        + CategoryTreeBean.CODE+", " + CategoryTreeBean.NAME);
				Vector vecCategoryCat1 = new Vector(CategoryTreeNut.getObjects(queryCat1));
				if(vecCategoryCat1.size()<=0)
				{
					CategoryTreeObject categoryObj = new CategoryTreeObject();
					categoryObj.catLevel = new Integer(1);
					categoryObj.name = itemCat1;
					categoryObj.description = itemCat1;
					categoryObj.code = itemCat1;
					categoryObj.sort = itemCat1;
					CategoryTree categoryTreeEJB = CategoryTreeNut.fnCreate(categoryObj);
				}

				/// check if CATEGORY LEVEL 2 CHECKING
            QueryObject queryCat2 = new QueryObject(new String[]{
										CategoryTreeBean.CODE + " = '"+itemCat2+"' ",
                              CategoryTreeBean.CAT_LEVEL +" ='2' " });
            queryCat2.setOrder(" ORDER BY "+ CategoryTreeBean.SORT+", "
                        + CategoryTreeBean.CODE+", " + CategoryTreeBean.NAME);
            Vector vecCategoryCat2 = new Vector(CategoryTreeNut.getObjects(queryCat2));
				if(vecCategoryCat2.size()<=0)
				{
					CategoryTreeObject categoryObj = new CategoryTreeObject();
					categoryObj.catLevel = new Integer(2);
					categoryObj.name = itemCat2;
					categoryObj.description = itemCat2;
					categoryObj.code = itemCat2;
					categoryObj.sort = itemCat2;
					CategoryTree categoryTreeEJB = CategoryTreeNut.fnCreate(categoryObj);
				}

            /// check if CATEGORY LEVEL 5 CHECKING
            QueryObject queryCat5 = new QueryObject(new String[]{
                              CategoryTreeBean.CODE + " = '"+itemCat5+"' ",
                              CategoryTreeBean.CAT_LEVEL +" ='5' " });
            queryCat5.setOrder(" ORDER BY "+ CategoryTreeBean.SORT+", "
                        + CategoryTreeBean.CODE+", " + CategoryTreeBean.NAME);
            Vector vecCategoryCat5 = new Vector(CategoryTreeNut.getObjects(queryCat5));
            if(vecCategoryCat5.size()<=0)
            {
               CategoryTreeObject categoryObj = new CategoryTreeObject();
               categoryObj.catLevel = new Integer(5);
               categoryObj.name = itemCat5;
               categoryObj.description = itemCat5;
               categoryObj.code = itemCat5;
               categoryObj.sort = itemCat5;
               CategoryTree categoryTreeEJB = CategoryTreeNut.fnCreate(categoryObj);
            }

            /// check if CATEGORY LEVEL 4 CHECKING
            QueryObject queryCat4 = new QueryObject(new String[]{
                              CategoryTreeBean.CODE + " = '"+itemCat4+"' ",
                              CategoryTreeBean.CAT_LEVEL +" ='4' " });
            queryCat4.setOrder(" ORDER BY "+ CategoryTreeBean.SORT+", "
                        + CategoryTreeBean.CODE+", " + CategoryTreeBean.NAME);
            Vector vecCategoryCat4 = new Vector(CategoryTreeNut.getObjects(queryCat4));
            if(vecCategoryCat4.size()<=0)
            {
               CategoryTreeObject categoryObj = new CategoryTreeObject();
               categoryObj.catLevel = new Integer(4);
               categoryObj.name = itemCat4;
               categoryObj.description = itemCat4;
               categoryObj.code = itemCat4;
               categoryObj.sort = itemCat4;
               CategoryTree categoryTreeEJB = CategoryTreeNut.fnCreate(categoryObj);
            }

            System.out.println("SERIAL NUMBER----------------");
				//// PROCESS ITEM SERIALIZATION
				BigDecimal bdSerial = new BigDecimal(itemSerialized);
				boolean boolSerial = (bdSerial.signum()!=0)?true:false;
					
				System.out.println("PRICE----------------");
				BigDecimal bdListPrice = new BigDecimal(itemListPrice);

				
				System.out.println("POPULATING NEW ITEM  -------");
				if(itemObj==null)
				{
					/// create a new item
					ItemObject itmObj = new ItemObject();
					itmObj.code = itemCode;
					itmObj.name = itemName;
					itmObj.categoryId = lCategoryObj.pkid;
					itmObj.category1 = itemCat1;
					itmObj.category2 = itemCat2;
					itmObj.category5 = itemCat5;
					itmObj.category4 = itemCat4;
					itmObj.eanCode = itemCode;
					itmObj.priceList = bdListPrice;
					itmObj.serialized = boolSerial;
					Item itmEJB = ItemNut.fnCreate(itmObj);
				}
				else
									
				{
					System.out.println("POPULATING EXISTING ITEM  -------");
					/// overwrite existing
					Item itmEJB	 = ItemNut.getHandle(itemObj.pkid);
					itemObj.code = itemCode;
					itemObj.name = itemName;
					itemObj.categoryId = lCategoryObj.pkid;
					itemObj.category1 = itemCat1;
					itemObj.category2 = itemCat2;
					itemObj.category4 = itemCat4;
					itemObj.eanCode = itemCode;
					itemObj.priceList = bdListPrice;
					itemObj.serialized = boolSerial;
					itmEJB.setObject(itemObj);	
				}


			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

	}


}



