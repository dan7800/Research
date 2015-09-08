/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Asset (Delivery) Servlet.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.15 2004/04/18 05:42:55 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "assetServlet";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "/assets.jsp";
		//
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "/login.jsp";
		else
		{
			session.removeAttribute(JSPEnv.HDR_MESSAGE);
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (info != null)
				info.setMessage(msg);
		//	if not returned - results in exception: Cannot forward after response has been committed 
		//	if (msg == null || msg.length() == 0)
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	protected String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}
		byte[] assetInfo = String.valueOf(A_Asset_ID).getBytes();

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownloadStream(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		/**			
		StringBuffer readme = new StringBuffer(asset.getDownloadName())
			.append("\n\rDownload for ")
			.append(wu.getName()).append(" - ").append(wu.getEmail())
			.append("\n\rVersion = ").append(asset.getVersionNo());
		if (asset.getLot() != null && asset.getLot().length() > 0)
			readme.append("\n\rLot = ").append(asset.getLot());
		if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
			readme.append("\n\rSerNo = ").append(asset.getSerNo());
		readme.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
			.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
		**/
		String lot = asset.getLot();
		if (lot == null || lot.length() == 0)
			lot = ".";
		String ser = asset.getSerNo();
		if (ser == null || ser.length() == 0)
			ser = ".";
		Object[] args = new Object[] {
			asset.getDownloadName(), wu.getName() + " - " + wu.getEmail(),
			asset.getVersionNo(), lot, ser, asset.getGuaranteeDate()};
		String readme = Msg.getMsg(ctx, "AssetDeliveryTemplate", args);

		//	Send File
		MAsset_Delivery ad = asset.confirmDelivery(request, wu.getAD_User_ID()); 
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
		//	response.setContentLength(length);
			
			int bufferSize = 2048; //	2k Buffer
			response.setBufferSize(bufferSize);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			
			//	Zip Output Stream
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);		//	Servlet out
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(readme);
							
			//	Readme File
			ZipEntry entry = new ZipEntry("readme.txt");
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			zip.write(readme.getBytes(), 0, readme.length());
			zip.closeEntry();
			
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			byte[] buffer = new byte[bufferSize];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, bufferSize);			//	read delivery
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);				//	write zip
				}
			} while (count != -1);
			zip.closeEntry();
			
			//	Fini
			zip.finish();
			zip.close();
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
			asset.save();
		}
		catch (IOException ex)
		{
			log.warn("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			//	asset.save();	not delivered
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error; Please Retry";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.zip.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Assets.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.6 2003/07/28 03:59:07 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "loginServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "assets.jsp";
		//
		HttpSession session = request.getSession(false);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "login.jsp";
		else
		{
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownload(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		//	Send File
		MAsset_Delivery ad = new MAsset_Delivery (asset, request, wu.getAD_User_ID());
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
			int length = 2048; //	2k Buffer
			response.setBufferSize(length);
		//	response.setContentLength(length);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			//
			StringBuffer content = new StringBuffer(asset.getDownloadName())
				.append("\n\rDownload for ")
				.append(wu.getName()).append(" - ").append(wu.getEmail())
				.append("\n\rVersion = ").append(asset.getVersionNo());
			if (asset.getLot() != null && asset.getLot().length() > 0)
				content.append("\n\rLot = ").append(asset.getLot());
			if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
				content.append("\n\rSerNo = ").append(asset.getSerNo());
			content.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
				.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
			//
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(content.toString());
			//	Readme
			ZipEntry entry = new ZipEntry("readme.txt");
			zip.putNextEntry(entry);
			zip.write(content.toString().getBytes(), 0, content.length());
			zip.closeEntry();
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			zip.putNextEntry(entry);
			byte[] buffer = new byte[length];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, length);
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);
				}
			} while (count != -1);
			zip.closeEntry();
			//
			zip.finish();
			zip.close();
			//
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
		}
		catch (IOException ex)
		{
			log.error("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *  Asset (Delivery) Servlet.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.17 2004/08/30 06:02:38 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "assetServlet";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "/assets.jsp";
		//
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "/login.jsp";
		else
		{
			session.removeAttribute(JSPEnv.HDR_MESSAGE);
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (info != null)
				info.setMessage(msg);
		//	if not returned - results in exception: Cannot forward after response has been committed 
		//	if (msg == null || msg.length() == 0)
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	protected String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}
		byte[] assetInfo = String.valueOf(A_Asset_ID).getBytes();

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable() || wu.isCreditStopHold())
			return "Asset not downloadable";

		InputStream in = asset.getDownloadStream(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		/**			
		StringBuffer readme = new StringBuffer(asset.getDownloadName())
			.append("\n\rDownload for ")
			.append(wu.getName()).append(" - ").append(wu.getEmail())
			.append("\n\rVersion = ").append(asset.getVersionNo());
		if (asset.getLot() != null && asset.getLot().length() > 0)
			readme.append("\n\rLot = ").append(asset.getLot());
		if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
			readme.append("\n\rSerNo = ").append(asset.getSerNo());
		readme.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
			.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
		**/
		String lot = asset.getLot();
		if (lot == null || lot.length() == 0)
			lot = ".";
		String ser = asset.getSerNo();
		if (ser == null || ser.length() == 0)
			ser = ".";
		Object[] args = new Object[] {
			asset.getDownloadName(), wu.getName() + " - " + wu.getEmail(),
			asset.getVersionNo(), lot, ser, asset.getGuaranteeDate()};
		String readme = Msg.getMsg(ctx, "AssetDeliveryTemplate", args);

		//	Send File
		MAsset_Delivery ad = asset.confirmDelivery(request, wu.getAD_User_ID()); 
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
		//	response.setContentLength(length);
			
			int bufferSize = 2048; //	2k Buffer
			response.setBufferSize(bufferSize);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			
			//	Zip Output Stream
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);		//	Servlet out
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(readme);
							
			//	Readme File
			ZipEntry entry = new ZipEntry("readme.txt");
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			zip.write(readme.getBytes(), 0, readme.length());
			zip.closeEntry();
			
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			byte[] buffer = new byte[bufferSize];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, bufferSize);			//	read delivery
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);				//	write zip
				}
			} while (count != -1);
			zip.closeEntry();
			
			//	Fini
			zip.finish();
			zip.close();
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
			asset.save();
		}
		catch (IOException ex)
		{
			log.warn("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			//	asset.save();	not delivered
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error; Please Retry";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.zip.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Assets.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.9 2003/10/04 03:58:26 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "loginServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "assets.jsp";
		//
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "login.jsp";
		else
		{
			session.removeAttribute(JSPEnv.HDR_MESSAGE);
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownloadStream(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		//	Send File
		MAsset_Delivery ad = new MAsset_Delivery (asset, request, wu.getAD_User_ID());
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
			int length = 2048; //	2k Buffer
			response.setBufferSize(length);
		//	response.setContentLength(length);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			//
			StringBuffer content = new StringBuffer(asset.getDownloadName())
				.append("\n\rDownload for ")
				.append(wu.getName()).append(" - ").append(wu.getEmail())
				.append("\n\rVersion = ").append(asset.getVersionNo());
			if (asset.getLot() != null && asset.getLot().length() > 0)
				content.append("\n\rLot = ").append(asset.getLot());
			if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
				content.append("\n\rSerNo = ").append(asset.getSerNo());
			content.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
				.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
			//
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(content.toString());
			//	Readme
			ZipEntry entry = new ZipEntry("readme.txt");
			zip.putNextEntry(entry);
			zip.write(content.toString().getBytes(), 0, content.length());
			zip.closeEntry();
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			zip.putNextEntry(entry);
			byte[] buffer = new byte[length];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, length);
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);
				}
			} while (count != -1);
			zip.closeEntry();
			//
			zip.finish();
			zip.close();
			//
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
		}
		catch (IOException ex)
		{
			log.error("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.zip.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Assets.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.5 2003/07/24 03:36:41 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "loginServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "asset.jsp";
		//
		HttpSession session = request.getSession(false);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "login.jsp";
		else
		{
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Invoice not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownload(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		//	Send File
		MAsset_Delivery ad = new MAsset_Delivery (asset, request, wu.getAD_User_ID());
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
			int length = 2048; //	2k Buffer
			response.setBufferSize(length);
		//	response.setContentLength(length);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			//
			StringBuffer content = new StringBuffer(asset.getDownloadName())
				.append("\n\rDownload for ")
				.append(wu.getName()).append(" - ").append(wu.getEmail())
				.append("\n\rVersion = ").append(asset.getVersionNo());
			if (asset.getLot() != null && asset.getLot().length() > 0)
				content.append("\n\rLot = ").append(asset.getLot());
			if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
				content.append("\n\rSerNo = ").append(asset.getSerNo());
			content.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
				.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
			//
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(content.toString());
			//	Readme
			ZipEntry entry = new ZipEntry("readme.txt");
			zip.putNextEntry(entry);
			zip.write(content.toString().getBytes(), 0, content.length());
			zip.closeEntry();
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			zip.putNextEntry(entry);
			byte[] buffer = new byte[length];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, length);
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);
				}
			} while (count != -1);
			zip.closeEntry();
			//
			zip.finish();
			zip.close();
			//
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
		}
		catch (IOException ex)
		{
			log.error("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.zip.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Assets.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.6 2003/07/28 03:59:07 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "loginServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "assets.jsp";
		//
		HttpSession session = request.getSession(false);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "login.jsp";
		else
		{
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownload(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		//	Send File
		MAsset_Delivery ad = new MAsset_Delivery (asset, request, wu.getAD_User_ID());
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
			int length = 2048; //	2k Buffer
			response.setBufferSize(length);
		//	response.setContentLength(length);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			//
			StringBuffer content = new StringBuffer(asset.getDownloadName())
				.append("\n\rDownload for ")
				.append(wu.getName()).append(" - ").append(wu.getEmail())
				.append("\n\rVersion = ").append(asset.getVersionNo());
			if (asset.getLot() != null && asset.getLot().length() > 0)
				content.append("\n\rLot = ").append(asset.getLot());
			if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
				content.append("\n\rSerNo = ").append(asset.getSerNo());
			content.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
				.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
			//
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(content.toString());
			//	Readme
			ZipEntry entry = new ZipEntry("readme.txt");
			zip.putNextEntry(entry);
			zip.write(content.toString().getBytes(), 0, content.length());
			zip.closeEntry();
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			zip.putNextEntry(entry);
			byte[] buffer = new byte[length];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, length);
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);
				}
			} while (count != -1);
			zip.closeEntry();
			//
			zip.finish();
			zip.close();
			//
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
		}
		catch (IOException ex)
		{
			log.error("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Asset (Delivery) Servlet.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.15 2004/04/18 05:42:55 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "assetServlet";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "/assets.jsp";
		//
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "/login.jsp";
		else
		{
			session.removeAttribute(JSPEnv.HDR_MESSAGE);
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (info != null)
				info.setMessage(msg);
		//	if not returned - results in exception: Cannot forward after response has been committed 
		//	if (msg == null || msg.length() == 0)
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	protected String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}
		byte[] assetInfo = String.valueOf(A_Asset_ID).getBytes();

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownloadStream(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		/**			
		StringBuffer readme = new StringBuffer(asset.getDownloadName())
			.append("\n\rDownload for ")
			.append(wu.getName()).append(" - ").append(wu.getEmail())
			.append("\n\rVersion = ").append(asset.getVersionNo());
		if (asset.getLot() != null && asset.getLot().length() > 0)
			readme.append("\n\rLot = ").append(asset.getLot());
		if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
			readme.append("\n\rSerNo = ").append(asset.getSerNo());
		readme.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
			.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
		**/
		String lot = asset.getLot();
		if (lot == null || lot.length() == 0)
			lot = ".";
		String ser = asset.getSerNo();
		if (ser == null || ser.length() == 0)
			ser = ".";
		Object[] args = new Object[] {
			asset.getDownloadName(), wu.getName() + " - " + wu.getEmail(),
			asset.getVersionNo(), lot, ser, asset.getGuaranteeDate()};
		String readme = Msg.getMsg(ctx, "AssetDeliveryTemplate", args);

		//	Send File
		MAsset_Delivery ad = asset.confirmDelivery(request, wu.getAD_User_ID()); 
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
		//	response.setContentLength(length);
			
			int bufferSize = 2048; //	2k Buffer
			response.setBufferSize(bufferSize);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			
			//	Zip Output Stream
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);		//	Servlet out
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(readme);
							
			//	Readme File
			ZipEntry entry = new ZipEntry("readme.txt");
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			zip.write(readme.getBytes(), 0, readme.length());
			zip.closeEntry();
			
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			byte[] buffer = new byte[bufferSize];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, bufferSize);			//	read delivery
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);				//	write zip
				}
			} while (count != -1);
			zip.closeEntry();
			
			//	Fini
			zip.finish();
			zip.close();
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
			asset.save();
		}
		catch (IOException ex)
		{
			log.warn("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			//	asset.save();	not delivered
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error; Please Retry";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.zip.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Assets.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.9 2003/10/04 03:58:26 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "loginServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "assets.jsp";
		//
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "login.jsp";
		else
		{
			session.removeAttribute(JSPEnv.HDR_MESSAGE);
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownloadStream(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		//	Send File
		MAsset_Delivery ad = new MAsset_Delivery (asset, request, wu.getAD_User_ID());
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
			int length = 2048; //	2k Buffer
			response.setBufferSize(length);
		//	response.setContentLength(length);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			//
			StringBuffer content = new StringBuffer(asset.getDownloadName())
				.append("\n\rDownload for ")
				.append(wu.getName()).append(" - ").append(wu.getEmail())
				.append("\n\rVersion = ").append(asset.getVersionNo());
			if (asset.getLot() != null && asset.getLot().length() > 0)
				content.append("\n\rLot = ").append(asset.getLot());
			if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
				content.append("\n\rSerNo = ").append(asset.getSerNo());
			content.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
				.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
			//
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(content.toString());
			//	Readme
			ZipEntry entry = new ZipEntry("readme.txt");
			zip.putNextEntry(entry);
			zip.write(content.toString().getBytes(), 0, content.length());
			zip.closeEntry();
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			zip.putNextEntry(entry);
			byte[] buffer = new byte[length];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, length);
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);
				}
			} while (count != -1);
			zip.closeEntry();
			//
			zip.finish();
			zip.close();
			//
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
		}
		catch (IOException ex)
		{
			log.error("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *  Asset (Delivery) Servlet.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.17 2004/08/30 06:02:38 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger					log = Logger.getCLogger(getClass());
	/** Name						*/
	static public final String		NAME = "assetServlet";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "/assets.jsp";
		//
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "/login.jsp";
		else
		{
			session.removeAttribute(JSPEnv.HDR_MESSAGE);
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (info != null)
				info.setMessage(msg);
		//	if not returned - results in exception: Cannot forward after response has been committed 
		//	if (msg == null || msg.length() == 0)
			return;
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	protected String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}
		byte[] assetInfo = String.valueOf(A_Asset_ID).getBytes();

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Asset not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable() || wu.isCreditStopHold())
			return "Asset not downloadable";

		InputStream in = asset.getDownloadStream(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		/**			
		StringBuffer readme = new StringBuffer(asset.getDownloadName())
			.append("\n\rDownload for ")
			.append(wu.getName()).append(" - ").append(wu.getEmail())
			.append("\n\rVersion = ").append(asset.getVersionNo());
		if (asset.getLot() != null && asset.getLot().length() > 0)
			readme.append("\n\rLot = ").append(asset.getLot());
		if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
			readme.append("\n\rSerNo = ").append(asset.getSerNo());
		readme.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
			.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
		**/
		String lot = asset.getLot();
		if (lot == null || lot.length() == 0)
			lot = ".";
		String ser = asset.getSerNo();
		if (ser == null || ser.length() == 0)
			ser = ".";
		Object[] args = new Object[] {
			asset.getDownloadName(), wu.getName() + " - " + wu.getEmail(),
			asset.getVersionNo(), lot, ser, asset.getGuaranteeDate()};
		String readme = Msg.getMsg(ctx, "AssetDeliveryTemplate", args);

		//	Send File
		MAsset_Delivery ad = asset.confirmDelivery(request, wu.getAD_User_ID()); 
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
		//	response.setContentLength(length);
			
			int bufferSize = 2048; //	2k Buffer
			response.setBufferSize(bufferSize);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			
			//	Zip Output Stream
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);		//	Servlet out
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(readme);
							
			//	Readme File
			ZipEntry entry = new ZipEntry("readme.txt");
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			zip.write(readme.getBytes(), 0, readme.length());
			zip.closeEntry();
			
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			entry.setExtra(assetInfo);
			zip.putNextEntry(entry);
			byte[] buffer = new byte[bufferSize];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, bufferSize);			//	read delivery
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);				//	write zip
				}
			} while (count != -1);
			zip.closeEntry();
			
			//	Fini
			zip.finish();
			zip.close();
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
			asset.save();
		}
		catch (IOException ex)
		{
			log.warn("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			//	asset.save();	not delivered
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error; Please Retry";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is             Compiere  ERP & CRM Smart Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.wstore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.zip.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.log4j.Logger;

import org.compiere.util.*;
import org.compiere.www.*;
import org.compiere.model.*;

/**
 *  Assets.
 *
 *  @author Jorg Janke
 *  @version $Id: AssetServlet.java,v 1.5 2003/07/24 03:36:41 jjanke Exp $
 */
public class AssetServlet extends HttpServlet
{
	/**	Logging						*/
	private Logger			log = Logger.getLogger(getClass());
	/** Name						*/
	static public final String		NAME = "loginServlet";
	/** Attribute					*/
	static public final String		ATTR_CHECKOUT = "CheckOut";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("AssetServlet.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Assets Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
	}   //  destroy


	/**
	 *  Process the HTTP Get request.
	 * 	(logout, deleteCookie)
	 *  Sends Web Request Page
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.info("doGet from " + request.getRemoteHost() + " - " + request.getRemoteAddr());

		String url = "asset.jsp";
		//
		HttpSession session = request.getSession(false);
		session.removeAttribute(JSPEnv.HDR_MESSAGE);
		if (session == null || session.getAttribute(Info.NAME) == null)
			url = "login.jsp";
		else
		{
			Info info = (Info)session.getAttribute(Info.NAME);
			if (info != null)
				info.setMessage("");

			//	Parameter = Asset_ID - if invoice is valid and belongs to wu then create PDF & stream it
			String msg = streamAsset(request, response);
			if (msg == null || msg.length() == 0)
				return;
			if (info != null)
				info.setMessage(msg);
		}

		log.info ("doGet - Forward to " + url);
		RequestDispatcher dispatcher = getServletContext ().getRequestDispatcher (url);
		dispatcher.forward (request, response);
	}	//	doGet

	/**
	 *  Process the HTTP Post request
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		doGet(request,response);
	}	//	doPost

	/**
	 * 	Stream asset
	 * 	@param request request
	 * 	@param response response
	 * 	@return "" or error message
	 */
	private String streamAsset (HttpServletRequest request, HttpServletResponse response)
	{
		//	Get Asset ID
		String assetString = request.getParameter("Asset_ID");
		if (assetString == null || assetString.length() == 0)
			return "";
		int A_Asset_ID = 0;
		try
		{
			A_Asset_ID = Integer.parseInt (assetString);
		}
		catch (NumberFormatException ex)
		{
			log.debug("streamAsset - " + ex);
		}
		if (A_Asset_ID == 0)
		{
			log.debug("streamAsset - no ID)");
			return "No Asset ID";
		}

		//	Get Asset
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		MAsset asset = new MAsset(ctx, A_Asset_ID);
		if (asset.getA_Asset_ID() != A_Asset_ID)
		{
			log.debug("streamInvoice - Asset not found - ID=" + A_Asset_ID);
			return "Invoice not found";
		}
		//	Get WebUser & Compare with invoice
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		if (wu.getC_BPartner_ID() != asset.getC_BPartner_ID())
		{
			log.warn ("streamInvoice - Asset from BPartner - A_Asset_ID="
				+ A_Asset_ID + " - BP_Invoice=" + asset.getC_BPartner_ID()
				+ " = BP_User=" + wu.getC_BPartner_ID());
			return "Your asset not found";
		}
		if (!asset.isDownloadable())
			return "Asset not downloadable";

		InputStream in = asset.getDownload(ctx.getProperty(JSPEnv.CTX_DOCUMENT_DIR));
		if (in == null)
			return "Asset not found";

		//	Send File
		MAsset_Delivery ad = new MAsset_Delivery (asset, request, wu.getAD_User_ID());
		float speed = 0;
		try
		{
			response.setContentType("application/zip");
			response.setHeader("Content-Location", "asset.zip");
			int length = 2048; //	2k Buffer
			response.setBufferSize(length);
		//	response.setContentLength(length);
			//
			log.debug("streamAsset - " + in + ", available=" + in.available());
			long time = System.currentTimeMillis();
			//
			StringBuffer content = new StringBuffer(asset.getDownloadName())
				.append("\n\rDownload for ")
				.append(wu.getName()).append(" - ").append(wu.getEmail())
				.append("\n\rVersion = ").append(asset.getVersionNo());
			if (asset.getLot() != null && asset.getLot().length() > 0)
				content.append("\n\rLot = ").append(asset.getLot());
			if (asset.getSerNo() != null && asset.getSerNo().length() > 0)
				content.append("\n\rSerNo = ").append(asset.getSerNo());
			content.append("\n\rGuarantee Date = ").append(asset.getGuaranteeDate())
				.append("\n\r\n\rThanks for using Compiere Customer Asset Management");
			//
			ServletOutputStream out = response.getOutputStream ();
			ZipOutputStream zip = new ZipOutputStream(out);
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);
			zip.setComment(content.toString());
			//	Readme
			ZipEntry entry = new ZipEntry("readme.txt");
			zip.putNextEntry(entry);
			zip.write(content.toString().getBytes(), 0, content.length());
			zip.closeEntry();
			//	Payload
			entry = new ZipEntry(asset.getDownloadName());
			zip.putNextEntry(entry);
			byte[] buffer = new byte[length];
			int count = 0;
			int totalSize = 0;
			do
			{
				count = in.read(buffer, 0, length);
				if (count > 0)
				{
					totalSize += count;
					zip.write (buffer, 0, count);
				}
			} while (count != -1);
			zip.closeEntry();
			//
			zip.finish();
			zip.close();
			//
			in.close();
			time = System.currentTimeMillis() - time;
			speed = ((float)totalSize/1024) / ((float)time/1000);
			log.debug("streamAsset - " + totalSize  + " B - " + time + " ms - " + speed + " kB/sec");
			//	Delivery Record
			ad.setDeliveryConfirmation(String.valueOf(speed));
			ad.save();
		}
		catch (IOException ex)
		{
			log.error("streamAsset - " + ex);
			//	Delivery Record
			try
			{
				String msg = ex.getMessage ();
				if (msg == null || msg.length () == 0)
					msg = ex.toString ();
				if (msg.length () > 120)
					msg = msg.substring (0, 119);
				ad.setDeliveryConfirmation (msg);
				ad.save ();
			}
			catch (Exception ex1)
			{
				log.error("streamAsset 2 - " + ex);
			}
			return "Streaming error";
		}
		//
		return null;
	}	//	streamAsset

}	//	AssetServlet
