/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.math.*;

import org.compiere.util.*;
import org.compiere.www.*;

/**
 *  Shopping Basket
 *
 *  Items are added to the basket by a get or post request with the following variables:
 *  @param  qty Quantity    (default 1) - format: decimal point no thousand separator
 *  @param  price   Item Price (single qty) - mandatory not added w/o price - format: decimal point no thousand separator
 *  @param  product Product Value/Code - optional internal only
 *  @param  description Description - optional
 *  @param  returnAddr  URL to return to - default sending page
 *  <p>
 *  Example:
 *  <code>
 *  http://localhost/servlet/org.compiere.wstore.Basket?qty=1&price=25.00&poduct=dl&description=Download Compiere&returnAddr=/page.html
 *  </code>
 *  The result is returned via the Basket.jsp
 *
 *  @author Jorg Janke
 *  @version  $Id: Basket.java,v 1.1.1.1 2002/10/12 01:06:54 jjanke Exp $
 */
public class Basket extends HttpServlet
{
	/**
	 *  Initialize global variables
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}
	/**
	 *  Clean up resources
	 */
	public void destroy()
	{
	}

	/** Parameter Delete Line   */
	public static final String  P_DELETE    = "Delete";
	/** Parameter Checkout      */
	public static final String  P_CHECKOUT  = "CheckOut";
	/** Parameter Return Addr   */
	public static final String  P_RETURN    = "returnAddr";

	/*************************************************************************/
	/**
	 *  Process the HTTP Get request
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "Basket.doGet");
		//  Get Session
		HttpSession sess = request.getSession(true);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  User    (from Cookie)
		String user = cProp.getProperty(WLogin.P_USERNAME, "");

		//  Return address
		String returnAddr = request.getParameter(P_RETURN);
		if (returnAddr == null || returnAddr.length() == 0)
			returnAddr = (String)request.getHeader("REFERER");
		request.setAttribute(P_RETURN, returnAddr);

		//  Target URL
		String url = WEnv.getStoreDirectory("basket.jsp");

		/**
		 *  Get Parameter Action Parameter
		 */
		//  Delete a line
		String W_Basket_ID = request.getParameter(P_DELETE);
		if (W_Basket_ID != null && W_Basket_ID.length() > 0)
		{
			String sql = "DELETE FROM W_Basket WHERE W_Basket_ID=" + W_Basket_ID;
			DB.executeUpdate(sql);
		}
		//  Checkout - forward to Customer Login
		else if (request.getParameter(P_CHECKOUT) != null && request.getParameter(P_CHECKOUT).length() > 0)
		{
			url = WEnv.getStoreDirectory("WCustomer.jsp");
		}

		/**
		 *  Get Parameter New Entry
		 */
		else
		{
			//  Quantity
			BigDecimal qty = new BigDecimal(1.0);
			try
			{
				qty = new BigDecimal (request.getParameter("qty"));
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-qty - ", e);
			}
			//  Price
			BigDecimal price = new BigDecimal(0.0);
			try
			{
				price = new BigDecimal (request.getParameter("price"));
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-price - ", e);
			}
			//  Product Value
			String product = "";
			try
			{
				product = request.getParameter("product");
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-product - ", e);
			}
			//  Product Description
			String description = "";
			try
			{
				description = request.getParameter("description");
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-description - ", e);
			}

			/**
			 *  Add to Basket
			 */
			StringBuffer sql = new StringBuffer("INSERT INTO W_Basket ");
			sql.append("(W_Basket_ID, Created, Session_ID, Remote_Addr, User_ID, Qty, Price, Product, Description) ");
			sql.append("VALUES (W_Basket_Seq.nextval, SysDate, ");
			sql.append("'").append(sess.getId()).append("',");              //  Session_ID
			sql.append("'").append(request.getRemoteAddr()).append("',");   //  RemoteAddr
			sql.append("'").append(user).append("',");                      //  User
			sql.append(qty.toString()).append(",");                         //  Qty
			sql.append(price.toString()).append(",");                       //  Price
			sql.append("'").append(product).append("',");                   //  Product
			sql.append("'").append(description).append("')");               //  Description
			//
			DB.executeUpdate(sql.toString());
		}

		/**
		 *  Forward to jsp
		 */
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}   //  doGet

	/**
	 *  Process the HTTP Post request
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}   //  doPost

}   //  Basket
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.math.*;

import org.compiere.util.*;
import org.compiere.www.*;

/**
 *  Shopping Basket
 *
 *  Items are added to the basket by a get or post request with the following variables:
 *  @param  qty Quantity    (default 1) - format: decimal point no thousand separator
 *  @param  price   Item Price (single qty) - mandatory not added w/o price - format: decimal point no thousand separator
 *  @param  product Product Value/Code - optional internal only
 *  @param  description Description - optional
 *  @param  returnAddr  URL to return to - default sending page
 *  <p>
 *  Example:
 *  <code>
 *  http://localhost/servlet/org.compiere.wstore.Basket?qty=1&price=25.00&poduct=dl&description=Download Compiere&returnAddr=/page.html
 *  </code>
 *  The result is returned via the Basket.jsp
 *
 *  @author Jorg Janke
 *  @version  $Id: Basket.java,v 1.1.1.1 2002/10/12 01:06:54 jjanke Exp $
 */
public class Basket extends HttpServlet
{
	/**
	 *  Initialize global variables
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}
	/**
	 *  Clean up resources
	 */
	public void destroy()
	{
	}

	/** Parameter Delete Line   */
	public static final String  P_DELETE    = "Delete";
	/** Parameter Checkout      */
	public static final String  P_CHECKOUT  = "CheckOut";
	/** Parameter Return Addr   */
	public static final String  P_RETURN    = "returnAddr";

	/*************************************************************************/
	/**
	 *  Process the HTTP Get request
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "Basket.doGet");
		//  Get Session
		HttpSession sess = request.getSession(true);
		//  Get Cookie
		Properties cProp = WUtil.getCookieProprties(request);
		//  User    (from Cookie)
		String user = cProp.getProperty(WLogin.P_USERNAME, "");

		//  Return address
		String returnAddr = request.getParameter(P_RETURN);
		if (returnAddr == null || returnAddr.length() == 0)
			returnAddr = (String)request.getHeader("REFERER");
		request.setAttribute(P_RETURN, returnAddr);

		//  Target URL
		String url = WEnv.getStoreDirectory("basket.jsp");

		/**
		 *  Get Parameter Action Parameter
		 */
		//  Delete a line
		String W_Basket_ID = request.getParameter(P_DELETE);
		if (W_Basket_ID != null && W_Basket_ID.length() > 0)
		{
			String sql = "DELETE FROM W_Basket WHERE W_Basket_ID=" + W_Basket_ID;
			DB.executeUpdate(sql);
		}
		//  Checkout - forward to Customer Login
		else if (request.getParameter(P_CHECKOUT) != null && request.getParameter(P_CHECKOUT).length() > 0)
		{
			url = WEnv.getStoreDirectory("WCustomer.jsp");
		}

		/**
		 *  Get Parameter New Entry
		 */
		else
		{
			//  Quantity
			BigDecimal qty = new BigDecimal(1.0);
			try
			{
				qty = new BigDecimal (request.getParameter("qty"));
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-qty - ", e);
			}
			//  Price
			BigDecimal price = new BigDecimal(0.0);
			try
			{
				price = new BigDecimal (request.getParameter("price"));
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-price - ", e);
			}
			//  Product Value
			String product = "";
			try
			{
				product = request.getParameter("product");
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-product - ", e);
			}
			//  Product Description
			String description = "";
			try
			{
				description = request.getParameter("description");
			}
			catch(Exception e)
			{
				Log.error("Basket.doGet Parameter-description - ", e);
			}

			/**
			 *  Add to Basket
			 */
			StringBuffer sql = new StringBuffer("INSERT INTO W_Basket ");
			sql.append("(W_Basket_ID, Created, Session_ID, Remote_Addr, User_ID, Qty, Price, Product, Description) ");
			sql.append("VALUES (W_Basket_Seq.nextval, SysDate, ");
			sql.append("'").append(sess.getId()).append("',");              //  Session_ID
			sql.append("'").append(request.getRemoteAddr()).append("',");   //  RemoteAddr
			sql.append("'").append(user).append("',");                      //  User
			sql.append(qty.toString()).append(",");                         //  Qty
			sql.append(price.toString()).append(",");                       //  Price
			sql.append("'").append(product).append("',");                   //  Product
			sql.append("'").append(description).append("')");               //  Description
			//
			DB.executeUpdate(sql.toString());
		}

		/**
		 *  Forward to jsp
		 */
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}   //  doGet

	/**
	 *  Process the HTTP Post request
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}   //  doPost

}   //  Basket
