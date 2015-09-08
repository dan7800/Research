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
package org.compiere.util;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.security.*;
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.db.CConnection;


/**
 *  System Environment and static variables
 *
 *  @author     Jorg Janke
 *  @version    $Id: Env.java,v 1.9 2003/08/11 05:56:23 jjanke Exp $
 */
public final class Env
{
	/**	Logging								*/
	private static Logger				s_log = Logger.getLogger(Env.class);

	/**
	 *  Test Init - Set Environment for tests
	 *
	 * @param traceLevel trace level
	 * @param isClient client session
	 * @return Context
	 */
	public static Properties initTest (int traceLevel, boolean isClient)
	{
	//	logger.entering("Env", "initTest");
		org.compiere.Compiere.startupClient();
		Log.setTraceLevel(traceLevel);
		//  Test Context
		Properties ctx = Env.getCtx();
		KeyNamePair[] roles = DB.login(ctx, CConnection.get(),
			"System", "System", true);
		//  load role
		if (roles != null && roles.length > 0)
		{
			KeyNamePair[] clients = DB.loadClients (ctx, roles[0]);
			//  load client
			if (clients != null && clients.length > 0)
			{
				KeyNamePair[] whs = DB.loadWarehouses(ctx, clients[0]);
				//
				KeyNamePair[] orgs = DB.loadOrgs(ctx, clients[0]);
				//  load org
				if (orgs != null && orgs.length > 0)
				{
					DB.loadPreferences(ctx, orgs[0], null, null, null);
				}
			}
		}
		//
		Env.setContext(ctx, "#Date", "2000-01-01");
	//	logger.exiting("Env", "initTest");
		return ctx;
	}   //  testInit

	/**
	 *  Java Version Test
	 *  @return true if Java Version is OK
	 */
	public static boolean isJavaOK()
	{
		//	Java System version check
		String jVersion = System.getProperty("java.version");
		String targetVersion = "1.4.1";				//	specific release
		if (jVersion.startsWith(targetVersion)) 	//  this release
			return true;
		//  Warning
		boolean ok = false;
		if (jVersion.startsWith("1.4"))  			//  later/earlier release
			ok = true;

		//  Error Message
		StringBuffer msg = new StringBuffer();
		msg.append(System.getProperty("java.vm.name")).append(" - ").append(jVersion);
		if (ok)
			msg.append("(untested)");
		msg.append("  <>  ").append(targetVersion);
		//
		JOptionPane.showMessageDialog(null, msg.toString(),
			org.compiere.Compiere.getName() + " - Java Version Check",
			ok ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
		return ok;
	}   //  isJavaOK

	/**
	 *	Exit System
	 *  @param status System exit status (usually 0 for no error)
	 */
	public static void exitEnv (int status)
	{
		reset(true);
		s_log.info("exit");
		LogManager.shutdown();
		if (Ini.isClient())
			System.exit (status);
	}	//	close

	/**
	 * 	Reset Cache
	 * 	@param all everything otherwise login data remains
	 */
	public static void reset (boolean all)
	{
		s_log.info("reset - all=" + all);

		//	Dismantle windows
		/**
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container win = (Container)s_windows.get(i);
			if (win.getClass().getName().endsWith("AMenu")) // Null pointer
				;
			else if (win instanceof Window)
				((Window)win).dispose();
			else
				win.removeAll();
		}
		**/
		s_windows.clear();

		//	Context
		if (all)
			s_ctx.clear();
		else
		{
			Object[] keys = s_ctx.keySet().toArray();
			for (int i = 0; i < keys.length; i++)
			{
				String tag = keys[i].toString();
				if (Character.isDigit(tag.charAt(0)))
					s_ctx.remove(keys[i]);
			}
		}

		//	Cache
		CacheMgt.get().reset();
		DB.closeTarget();
	}	//	resetAll


	/*************************************************************************/

	/**
	 *  Application Context
	 */
	private static Properties   s_ctx = new Properties();
	/** WindowNo for Find           */
	public static final int     WINDOW_FIND = 1110;
	/** WinowNo for MLookup         */
	public static final int	    WINDOW_MLOOKUP = 1111;
	/** WindowNo for PrintCustomize */
	public static final int     WINDOW_CUSTOMIZE = 1112;

	/** Tab for Info                */
	public static final int     TAB_INFO = 77;

	/**
	 *  Get Context
	 *  @return Properties
	 */
	public static final Properties getCtx()
	{
		return s_ctx;
	}   //  getCtx

	/**
	 * Set Context
	 * @param ctx context
	 */
	public static void setCtx (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.setCtx - require Context");
		s_ctx.clear();
		s_ctx = ctx;
	}   //  setCtx

	/**
	 *	Set Global Context to Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		if (value == null || value.length() == 0)
			ctx.remove(context);
		else
			ctx.setProperty(context, value);
	}	//	setContext

	/**
	 *	Set Global Context to (int) Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		ctx.setProperty(context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Global Context to Y/N Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, boolean value)
	{
		setContext (ctx, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set Context for Window to int Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		ctx.setProperty(WindowNo+"|"+context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Context for Window to Y/N Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, boolean value)
	{
		setContext (ctx, WindowNo, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window & Tab to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 *  @param TabNo tab no
	 */
	public static void setContext (Properties ctx, int WindowNo, int TabNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(9, "Context("+WindowNo+","+TabNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+TabNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+TabNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set AutoCommit
	 *  @param ctx context
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty("AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Set AutoCommit for Window
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, int WindowNo, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty(WindowNo+"|AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Get global Value of Context
	 *  @param ctx context
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		return ctx.getProperty(context, "");
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available and enabled
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context, boolean onlyWindow)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+context);
		if (s == null)
		{
			if (onlyWindow)
				return "";
			if (context.startsWith("#") || context.startsWith("$"))
				return getContext(ctx, context);
			return getContext(ctx, "#" + context);
		}
		return s;
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context)
	{
		return getContext(ctx, WindowNo, context, false);
	}	//	getContext

	/**
	 *	Get Value of Context for Window & Tab,
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, int TabNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+TabNo+"|"+context);
		if (s == null)
			return getContext(ctx, WindowNo, context, false);
		return s;
	}	//	getContext

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param context context key
	 *  @return value
	 */
	public static int getContextAsInt(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		if (s.length() == 0)
			s = getContext(ctx, 0, context, false);		//	search 0 and defaults
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return value or 0
	 */
	public static int getContextAsInt(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Is AutoCommit
	 *  @param ctx context
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, "AutoCommit");
		if (s != null && s.equals("Y"))
			return true;
		return false;
	}	//	isAutoCommit

	/**
	 *	Is Window AutoCommit (if not set use default)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, "AutoCommit", false);
		if (s != null)
		{
			if (s.equals("Y"))
				return true;
			else
				return false;
		}
		return isAutoCommit(ctx);
	}	//	isAutoCommit

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, String context)
	{
		return getContextAsDate(ctx, 0, context);
	}	//	getContextAsDate

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/*************************************************************************/

	/**
	 *	Get Preference.
	 *  <pre>
	 *		0)	Current Setting
	 *		1) 	Window Preference
	 *		2) 	Global Preference
	 *		3)	Login settings
	 *		4)	Accounting settings
	 *  </pre>
	 *  @param  ctx context
	 *	@param	AD_Window_ID window no
	 *	@param	context		Entity to search
	 *	@param	system		System level preferences (vs. user defined)
	 *  @return preference value
	 */
	public static String getPreference (Properties ctx, int AD_Window_ID, String context, boolean system)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getPreference - require Context");
		String retValue = null;
		//
		if (!system)	//	User Preferences
		{
			retValue = ctx.getProperty("P"+AD_Window_ID+"|"+context);//	Window Pref
			if (retValue == null)
				retValue = ctx.getProperty("P|"+context);  			//	Global Pref
		}
		else			//	System Preferences
		{
			retValue = ctx.getProperty("#"+context);   				//	Login setting
			if (retValue == null)
				retValue = ctx.getProperty("$"+context);   			//	Accounting setting
		}
		//
		return (retValue == null ? "" : retValue);
	}	//	getPreference

	/****************************************************************************
	 *  Language issues
	 */

	/** Context Language identifier */
	static public final String      LANG = "#AD_Language";

	/**
	 *  Check Base Language
	 *  @param ctx context
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Properties ctx, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (getAD_Language(ctx));
		else	//	No AD Table
			if (!isMultiLingualDocument(ctx))
				return true;		//	access base table
		return Language.isBaseLanguage (getAD_Language(ctx));
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param AD_Language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (String AD_Language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (AD_Language);
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return Language.isBaseLanguage (AD_Language);
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Language language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			language.isBaseLanguage();
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return language.isBaseLanguage();
	}	//	isBaseLanguage

	/**
	 * 	Do we have Multi-Lingual Documents.
	 *  Set in DB.loadOrgs
	 * 	@param ctx context
	 * 	@return true if multi lingual documents
	 */
	public static boolean isMultiLingualDocument (Properties ctx)
	{
		return "Y".equals(Env.getContext(ctx, "#IsMultiLingualDocument"));
	}	//	isMultiLingualDocument

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return AD_Language eg. en_US
	 */
	public static String getAD_Language (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return lang;
		}
		return Language.getBaseAD_Language();
	}	//	getAD_Language

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return Language
	 */
	public static Language getLanguage (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return Language.getLanguage(lang);
		}
		return Language.getLanguage();
	}	//	getLanguage

	/**
	 *  Verify Language.
	 *  Check that language is supported by the system
	 *  @param ctx might be updated with new AD_Language
	 *  @param language language
	 */
	public static void verifyLanguage (Properties ctx, Language language)
	{
		ArrayList sysLang = new ArrayList();
		String sql = "SELECT AD_Language FROM AD_Language ORDER BY IsBaseLanguage DESC";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				sysLang.add(rs.getString(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("ALogin.verifyLanguage", e);
		}

		String selectedLanguage = language.getAD_Language();
		for (int i = 0; i < sysLang.size(); i++)
		{
			if (selectedLanguage.equals(sysLang.get(i)))
				return;
		}
		//  Language/Country not found - try finding similar language (i.e. first e chars)
		selectedLanguage = selectedLanguage.substring(0,2);
		for (int i = 0; i < sysLang.size(); i++)
		{
			String comp = sysLang.get(i).toString().substring(0,2);
			if (selectedLanguage.equals(comp))
			{
				language.setAD_Language(sysLang.get(i).toString());
				Env.setContext(ctx, Env.LANG, language.getAD_Language());
				return;
			}
		}
		//  none found - use Database Base Language
		language.setAD_Language(sysLang.get(0).toString());
		Env.setContext(ctx, Env.LANG, language.getAD_Language());
	}   //  verifyLanguage

	/*************************************************************************/

	/**
	 *	Get Context as String array with format: key == value
	 *  @param ctx context
	 *  @return context string
	 */
	public static String[] getEntireContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getEntireContext - require Context");
		Iterator keyIterator = ctx.keySet().iterator();
		String[] sList = new String[ctx.size()];
		int i = 0;
		while (keyIterator.hasNext())
		{
			Object key = keyIterator.next();
			sList[i++] = key.toString() + " == " + ctx.get(key).toString();
		}

		return sList;
	}	//	getEntireContext

	/**
	 *	Get Header info (connection, org, user)
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @return Header String
	 */
	public static String getHeader(Properties ctx, int WindowNo)
	{
		StringBuffer sb = new StringBuffer();
		if (WindowNo > 0)
			sb.append(getContext(ctx, WindowNo, "WindowName", false)).append("  ");
		sb.append(getContext(ctx, "#AD_User_Name")).append("@")
			.append(getContext(ctx, "#AD_Client_Name")).append(".")
			.append(getContext(ctx, "#AD_Org_Name"))
			.append(" [").append(CConnection.get().toString()).append("]");
		return sb.toString();
	}	//	getHeader

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public static void clearWinContext(Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearWinContext - require Context");
		//
		Object[] keys = ctx.keySet().toArray();
		for (int i = 0; i < keys.length; i++)
		{
			String tag = keys[i].toString();
			if (tag.startsWith(WindowNo+"|"))
				ctx.remove(keys[i]);
		}
		//  Clear Lookup Cache
		MLookupCache.cacheReset(WindowNo);
	//	MLocator.cacheReset(WindowNo);
		//
		removeWindow(WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 *  @param ctx context
	 */
	public static void clearContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearContext - require Context");
		ctx.clear();
	}	//	clearContext


	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 * 	@param	ignoreUnparsable if true, unsuccessful @tag@ are ignored otherwise "" is returned
	 *  @return parsed String or "" if not successful and ignoreUnparsable
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow, boolean ignoreUnparsable)
	{
		if (value == null)
			return "";

		String token;
		String inStr = new String(value);
		StringBuffer outStr = new StringBuffer();

		int i = inStr.indexOf("@");
		while (i != -1)
		{
			outStr.append(inStr.substring(0, i));			// up to @
			inStr = inStr.substring(i+1, inStr.length());	// from first @

			int j = inStr.indexOf("@");						// next @
			if (j < 0)
			{
				Log.error("Env.parseContext - no second tag: " + inStr);
				return "";						//	no second tag
			}

			token = inStr.substring(0, j);

			String ctxInfo = getContext(ctx, WindowNo, token, onlyWindow);	// get context
			if (ctxInfo.length() == 0 && (token.startsWith("#") || token.startsWith("$")) )
				ctxInfo = getContext(ctx, token);	// get global context
			if (ctxInfo.length() == 0)
			{
				Log.trace(Log.l5_DData, "Env.parseContext - no context (" + WindowNo + ") for: " + token);
				if (!ignoreUnparsable)
					return "";						//	token not found
			}
			else
				outStr.append(ctxInfo);				// replace context with Context

			inStr = inStr.substring(j+1, inStr.length());	// from second @
			i = inStr.indexOf("@");
		}
		outStr.append(inStr);						// add the rest of the string

		return outStr.toString();
	}	//	parseContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return parsed String or "" if not successful
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow)
	{
		return parseContext(ctx, WindowNo, value, onlyWindow, false);
	}	//	parseContext

	/*************************************************************************/

	private static ArrayList	s_windows = new ArrayList(20);

	/**
	 *	Add Container and return WindowNo.
	 *  The container is a APanel, AWindow or JFrame/JDialog
	 *  @param win window
	 *  @return WindowNo used for context
	 */
	public static int createWindowNo(Container win)
	{
		int retValue = s_windows.size();
		s_windows.add(win);
		return retValue;
	}	//	createWindowNo

	/**
	 *	Search Window by comparing the Frames
	 *  @param container container
	 *  @return WindowNo of container or 0
	 */
	public static int getWindowNo (Container container)
	{
		if (container == null)
			return 0;
		JFrame winFrame = getFrame(container);
		if (winFrame == null)
			return 0;

		//  loop through windows
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container cmp = (Container)s_windows.get(i);
			if (cmp != null)
			{
				JFrame cmpFrame = getFrame(cmp);
				if (winFrame.equals(cmpFrame))
					return i;
			}
		}
		return 0;
	}	//	getWindowNo

	/**
	 *	Return the JFrame pointer of WindowNo - or null
	 *  @param WindowNo window
	 *  @return JFrame of WindowNo
	 */
	public static JFrame getWindow (int WindowNo)
	{
		JFrame retValue = null;
		try
		{
			retValue = getFrame ((Container)s_windows.get(WindowNo));
		}
		catch (Exception e)
		{
			System.err.println("Env.getWindow - " + e);
		}
		return retValue;
	}	//	getWindow

	/**
	 *	Remove window from active list
	 *  @param WindowNo window
	 */
	private static void removeWindow (int WindowNo)
	{
		if (WindowNo <= s_windows.size())
			s_windows.set(WindowNo, null);
	}	//	removeWindow

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param WindowNo window
	 */
	public static void clearWinContext(int WindowNo)
	{
		clearWinContext (s_ctx, WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 */
	public static void clearContext()
	{
		s_ctx.clear();
	}	//	clearContext


	/*************************************************************************/

	/**
	 *	Get Frame of Window
	 *  @param container Container
	 *  @return JFrame of container or null
	 */
	public static JFrame getFrame (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JFrame)
				return (JFrame)element;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *	Get Graphics of container or its parent.
	 *  The element may not have a Graphic if not displayed yet,
	 * 	but the parent might have.
	 *  @param container Container
	 *  @return Graphics of container or null
	 */
	public static Graphics getGraphics (Container container)
	{
		Container element = container;
		while (element != null)
		{
			Graphics g = element.getGraphics();
			if (g != null)
				return g;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *  Return JDialog or JFrame Parent
	 *  @param container Container
	 *  @return JDialog or JFrame of container
	 */
	public static Window getParent (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JDialog || element instanceof JFrame)
				return (Window)element;
			element = element.getParent();
		}
		return null;
	}   //  getParent

	/*************************************************************************/

	/**
	 *  Get Image with File name
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static Image getImage (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.getImage(url);
	}   //  getImage

	/**
	 *  Get ImageIcon
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static ImageIcon getImageIcon (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		return new ImageIcon(url);
	}   //  getImageIcon


	/**************************************************************************/

	/**
	 *  Start Browser
	 *  @param url url
	 */
	public static void startBrowser (String url)
	{
		Log.trace(Log.l1_User, "Env.startBrowser", url);
		//  OS command
		String cmd = "explorer ";
		if (!System.getProperty("os.name").startsWith("Win"))
			cmd = "netscape ";
		//
		String execute = cmd + url;
		try
		{
			Runtime.getRuntime().exec(execute);
		}
		catch (Exception e)
		{
			System.err.println("Env.startBrowser - " + execute + " - " + e);
		}
	}   //  startBrowser

	/**************************************************************************
	 *  Static Variables
	 */

	/**
	 *  Big Decimal Zero
	 */
	static final public java.math.BigDecimal ZERO = new java.math.BigDecimal(0.0);
	static final public java.math.BigDecimal ONE = new java.math.BigDecimal(1.0);

	/**
	 * 	New Line
	 */
	public static final String	NL = System.getProperty("line.separator");


	/**
	 *  Static initializer
	 */
	static
	{
		//  Set English as default Language
		s_ctx.put(LANG, Language.getBaseAD_Language());
	}   //  static

}   //  Env
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
package org.compiere.util;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.security.*;
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.db.CConnection;


/**
 *  System Environment and static variables
 *
 *  @author     Jorg Janke
 *  @version    $Id: Env.java,v 1.3 2003/02/15 06:32:50 jjanke Exp $
 */
public final class Env
{
	/**	Logging								*/
	private static Logger				s_log = Logger.getLogger("org.compiere.util.Env");

	/**
	 *  Test Init - Set Environment for tests
	 *
	 * @param traceLevel trace level
	 * @param isClient client session
	 * @return Context
	 */
	public static Properties initTest (int traceLevel, boolean isClient)
	{
	//	logger.entering("Env", "initTest");
		org.compiere.Compiere.startupClient();
		Log.setTraceLevel(traceLevel);
		//  Test Context
		Properties ctx = Env.getCtx();
		KeyNamePair[] roles = DB.login(ctx, CConnection.get(),
			"System", "System", true);
		//  load role
		if (roles != null && roles.length > 0)
		{
			KeyNamePair[] clients = DB.loadClients (ctx, roles[0]);
			//  load client
			if (clients != null && clients.length > 0)
			{
				KeyNamePair[] whs = DB.loadWarehouses(ctx, clients[0]);
				//
				KeyNamePair[] orgs = DB.loadOrgs(ctx, clients[0]);
				//  load org
				if (orgs != null && orgs.length > 0)
				{
					DB.loadPreferences(ctx, orgs[0], null, null, null);
				}
			}
		}
		//
		Env.setContext(ctx, "#Date", "2000-05-01");
	//	logger.exiting("Env", "initTest");
		return ctx;
	}   //  testInit

	/**
	 *  Java Version Test
	 *  @return true if Java Version is OK
	 */
	public static boolean isJavaOK()
	{
		//	Java System version check
		String jVersion = System.getProperty("java.version");
		String targetVersion = "1.4.";
		if (jVersion.startsWith(targetVersion))		//  same version
			return true;
		//  Warning
		boolean ok = false;
		if (jVersion.startsWith("1.4.2"))  			//  later release
			ok = true;

		//  Error Message
		StringBuffer msg = new StringBuffer();
		msg.append(System.getProperty("java.vm.name")).append(" - ").append(jVersion);
		if (ok)
			msg.append("(untested)");
		msg.append("  <>  ").append(targetVersion);
		//
		JOptionPane.showMessageDialog(null, msg.toString(),
			org.compiere.Compiere.getName() + " - Java Version Check",
			ok ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
		return ok;
	}   //  isJavaOK

	/**
	 *	Exit System
	 *  @param status System exit status (usually 0 for no error)
	 */
	public static void exitEnv (int status)
	{
		s_log.info("exit");
		DB.closeTarget();
		s_windows.clear();
		s_ctx.clear();
		Msg.reset();
		LogManager.shutdown();
		if (Ini.isClient())
			System.exit (status);
	}	//	close

	/*************************************************************************/

	/**
	 *  Application Context
	 */
	private static Properties   s_ctx = new Properties();
	/** WindowNo for Find           */
	public static final int     WINDOW_FIND = 1110;
	/** WinowNo for MLookup         */
	public static final int	    WINDOW_MLOOKUP = 1111;
	/** WindowNo for PrintCustomize */
	public static final int     WINDOW_CUSTOMIZE = 1112;

	/** Tab for Info                */
	public static final int     TAB_INFO = 77;

	/**
	 *  Get Context
	 *  @return Properties
	 */
	public static final Properties getCtx()
	{
		return s_ctx;
	}   //  getCtx

	/**
	 * Set Context
	 * @param ctx context
	 */
	public static void setCtx (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.setCtx - require Context");
		s_ctx.clear();
		s_ctx = ctx;
	}   //  setCtx

	/**
	 *	Set Global Context to Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		if (value == null || value.length() == 0)
			ctx.remove(context);
		else
			ctx.setProperty(context, value);
	}	//	setContext

	/**
	 *	Set Global Context to (int) Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		ctx.setProperty(context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Global Context to Y/N Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, boolean value)
	{
		setContext (ctx, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set Context for Window to int Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		ctx.setProperty(WindowNo+"|"+context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Context for Window to Y/N Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, boolean value)
	{
		setContext (ctx, WindowNo, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window & Tab to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 *  @param TabNo tab no
	 */
	public static void setContext (Properties ctx, int WindowNo, int TabNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(9, "Context("+WindowNo+","+TabNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+TabNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+TabNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set AutoCommit
	 *  @param ctx context
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty("AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Set AutoCommit for Window
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, int WindowNo, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty(WindowNo+"|AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Get global Value of Context
	 *  @param ctx context
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		return ctx.getProperty(context, "");
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available and enabled
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context, boolean onlyWindow)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+context);
		if (s == null)
		{
			if (onlyWindow)
				return "";
			if (context.startsWith("#") || context.startsWith("$"))
				return getContext(ctx, context);
			return getContext(ctx, "#" + context);
		}
		return s;
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context)
	{
		return getContext(ctx, WindowNo, context, false);
	}	//	getContext

	/**
	 *	Get Value of Context for Window & Tab,
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, int TabNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+TabNo+"|"+context);
		if (s == null)
			return getContext(ctx, WindowNo, context, false);
		return s;
	}	//	getContext

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param context context key
	 *  @return value
	 */
	public static int getContextAsInt(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return value or 0
	 */
	public static int getContextAsInt(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Is AutoCommit
	 *  @param ctx context
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, "AutoCommit");
		if (s != null && s.equals("Y"))
			return true;
		return false;
	}	//	isAutoCommit

	/**
	 *	Is Window AutoCommit (if not set use default)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, "AutoCommit", false);
		if (s != null)
		{
			if (s.equals("Y"))
				return true;
			else
				return false;
		}
		return isAutoCommit(ctx);
	}	//	isAutoCommit

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/*************************************************************************/

	/**
	 *	Get Preference.
	 *  <pre>
	 *		0)	Current Setting
	 *		1) 	Window Preference
	 *		2) 	Global Preference
	 *		3)	Login settings
	 *		4)	Accounting settings
	 *  </pre>
	 *  @param  ctx context
	 *	@param	AD_Window_ID window no
	 *	@param	context		Entity to search
	 *	@param	system		System level preferences (vs. user defined)
	 *  @return preference value
	 */
	public static String getPreference (Properties ctx, int AD_Window_ID, String context, boolean system)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getPreference - require Context");
		String retValue = null;
		//
		if (!system)	//	User Preferences
		{
			retValue = ctx.getProperty("P"+AD_Window_ID+"|"+context);//	Window Pref
			if (retValue == null)
				retValue = ctx.getProperty("P|"+context);  			//	Global Pref
		}
		else			//	System Preferences
		{
			retValue = ctx.getProperty("#"+context);   				//	Login setting
			if (retValue == null)
				retValue = ctx.getProperty("$"+context);   			//	Accounting setting
		}
		//
		return (retValue == null ? "" : retValue);
	}	//	getPreference

	/****************************************************************************
	 *  Language issues
	 */

	/** Context Language identifier */
	static public final String      LANG = "#AD_Language";

	/**
	 *  Check Base Language
	 *  @param ctx context
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Properties ctx, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (getAD_Language(ctx));
		else	//	No AD Table
			if (!isMultiLingualDocument(ctx))
				return true;		//	access base table
		return Language.isBaseLanguage (getAD_Language(ctx));
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param AD_Language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (String AD_Language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (AD_Language);
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return Language.isBaseLanguage (AD_Language);
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Language language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			language.isBaseLanguage();
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return language.isBaseLanguage();
	}	//	isBaseLanguage

	/**
	 * 	Do we have Multi-Lingual Documents.
	 *  Set in DB.loadOrgs
	 * 	@param ctx context
	 * 	@return true if multi lingual documents
	 */
	public static boolean isMultiLingualDocument (Properties ctx)
	{
		return "Y".equals(Env.getContext(ctx, "#IsMultiLingualDocument"));
	}	//	isMultiLingualDocument

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return AD_Language eg. en_US
	 */
	public static String getAD_Language (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return lang;
		}
		return Language.getBaseAD_Language();
	}	//	getAD_Language

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return Language
	 */
	public static Language getLanguage (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return Language.getLanguage(lang);
		}
		return Language.getLanguage();
	}	//	getLanguage

	/**
	 *  Verify Language.
	 *  Check that language is supported by the system
	 *  @param ctx might be updated with new AD_Language
	 *  @param language language
	 */
	public static void verifyLanguage (Properties ctx, Language language)
	{
		ArrayList sysLang = new ArrayList();
		String sql = "SELECT AD_Language FROM AD_Language ORDER BY IsBaseLanguage DESC";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				sysLang.add(rs.getString(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("ALogin.verifyLanguage", e);
		}

		String selectedLanguage = language.getAD_Language();
		for (int i = 0; i < sysLang.size(); i++)
		{
			if (selectedLanguage.equals(sysLang.get(i)))
				return;
		}
		//  Language/Country not found - try finding similar language (i.e. first e chars)
		selectedLanguage = selectedLanguage.substring(0,2);
		for (int i = 0; i < sysLang.size(); i++)
		{
			String comp = sysLang.get(i).toString().substring(0,2);
			if (selectedLanguage.equals(comp))
			{
				language.setAD_Language(sysLang.get(i).toString());
				Env.setContext(ctx, Env.LANG, language.getAD_Language());
				return;
			}
		}
		//  none found - use Database Base Language
		language.setAD_Language(sysLang.get(0).toString());
		Env.setContext(ctx, Env.LANG, language.getAD_Language());
	}   //  verifyLanguage

	/*************************************************************************/

	/**
	 *	Get Context as String array with format: key == value
	 *  @param ctx context
	 *  @return context string
	 */
	public static String[] getEntireContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getEntireContext - require Context");
		Iterator keyIterator = ctx.keySet().iterator();
		String[] sList = new String[ctx.size()];
		int i = 0;
		while (keyIterator.hasNext())
		{
			Object key = keyIterator.next();
			sList[i++] = key.toString() + " == " + ctx.get(key).toString();
		}

		return sList;
	}	//	getEntireContext

	/**
	 *	Get Header info (connection, org, user)
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @return Header String
	 */
	public static String getHeader(Properties ctx, int WindowNo)
	{
		StringBuffer sb = new StringBuffer();
		if (WindowNo > 0)
			sb.append(getContext(ctx, WindowNo, "WindowName", false)).append("  ");
		sb.append(getContext(ctx, "#AD_User_Name")).append("@")
			.append(getContext(ctx, "#AD_Client_Name")).append(".")
			.append(getContext(ctx, "#AD_Org_Name"))
			.append(" [").append(CConnection.get().toString()).append("]");
		return sb.toString();
	}	//	getHeader

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public static void clearWinContext(Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearWinContext - require Context");
		//
		Object[] keys = ctx.keySet().toArray();
		for (int i = 0; i < keys.length; i++)
		{
			String tag = keys[i].toString();
			if (tag.startsWith(WindowNo+"|"))
				ctx.remove(keys[i]);
		}
		//  Clear Lookup Cache
		MLookupCache.cacheReset(WindowNo);
	//	MLocator.cacheReset(WindowNo);
		//
		removeWindow(WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 *  @param ctx context
	 */
	public static void clearContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearContext - require Context");
		ctx.clear();
	}	//	clearContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 * 	@param	ignoreUnparsable if true, unsuccessful @tag@ are ignored otherwise "" is returned
	 *  @return parsed String or "" if not successful and ignoreUnparsable
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow, boolean ignoreUnparsable)
	{
		if (value == null)
			return "";

		String token;
		String inStr = new String(value);
		StringBuffer outStr = new StringBuffer();

		int i = inStr.indexOf("@");
		while (i != -1)
		{
			outStr.append(inStr.substring(0, i));			// up to @
			inStr = inStr.substring(i+1, inStr.length());	// from first @

			int j = inStr.indexOf("@");						// next @
			if (j < 0)
			{
				Log.error("Env.parseContext - no second tag: " + inStr);
				return "";						//	no second tag
			}

			token = inStr.substring(0, j);

			String ctxInfo = getContext(ctx, WindowNo, token, onlyWindow);	// get context
			if (ctxInfo.length() == 0 && (token.startsWith("#") || token.startsWith("$")) )
				ctxInfo = getContext(ctx, token);	// get global context
			if (ctxInfo.length() == 0)
			{
				Log.trace(Log.l5_DData, "Env.parseContext - no context (" + WindowNo + ") for: " + token);
				if (!ignoreUnparsable)
					return "";						//	token not found
			}
			else
				outStr.append(ctxInfo);				// replace context with Context

			inStr = inStr.substring(j+1, inStr.length());	// from second @
			i = inStr.indexOf("@");
		}
		outStr.append(inStr);						// add the rest of the string

		return outStr.toString();
	}	//	parseContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return parsed String or "" if not successful
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow)
	{
		return parseContext(ctx, WindowNo, value, onlyWindow, false);
	}	//	parseContext

	/*************************************************************************/

	private static ArrayList	s_windows = new ArrayList(20);

	/**
	 *	Add Container and return WindowNo.
	 *  The container is a APanel, AWindow or JFrame/JDialog
	 *  @param win window
	 *  @return WindowNo used for context
	 */
	public static int createWindowNo(Container win)
	{
		int retValue = s_windows.size();
		s_windows.add(win);
		return retValue;
	}	//	createWindowNo

	/**
	 *	Search Window by comparing the Frames
	 *  @param container container
	 *  @return WindowNo of container or 0
	 */
	public static int getWindowNo (Container container)
	{
		if (container == null)
			return 0;
		JFrame winFrame = getFrame(container);
		if (winFrame == null)
			return 0;

		//  loop through windows
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container cmp = (Container)s_windows.get(i);
			if (cmp != null)
			{
				JFrame cmpFrame = getFrame(cmp);
				if (winFrame.equals(cmpFrame))
					return i;
			}
		}
		return 0;
	}	//	getWindowNo

	/**
	 *	Return the JFrame pointer of WindowNo - or null
	 *  @param WindowNo window
	 *  @return JFrame of WindowNo
	 */
	public static JFrame getWindow (int WindowNo)
	{
		JFrame retValue = null;
		try
		{
			retValue = getFrame ((Container)s_windows.get(WindowNo));
		}
		catch (Exception e)
		{
			System.err.println("Env.getWindow - " + e);
		}
		return retValue;
	}	//	getWindow

	/**
	 *	Remove window from active list
	 *  @param WindowNo window
	 */
	private static void removeWindow (int WindowNo)
	{
		if (WindowNo <= s_windows.size())
			s_windows.set(WindowNo, null);
	}	//	removeWindow

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param WindowNo window
	 */
	public static void clearWinContext(int WindowNo)
	{
		clearWinContext (s_ctx, WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 */
	public static void clearContext()
	{
		s_ctx.clear();
	}	//	clearContext


	/*************************************************************************/

	/**
	 *	Get Frame of Window
	 *  @param container Container
	 *  @return JFrame of container or null
	 */
	public static JFrame getFrame (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JFrame)
				return (JFrame)element;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *	Get Graphics of container or its parent.
	 *  The element may not have a Graphic if not displayed yet,
	 * 	but the parent might have.
	 *  @param container Container
	 *  @return Graphics of container or null
	 */
	public static Graphics getGraphics (Container container)
	{
		Container element = container;
		while (element != null)
		{
			Graphics g = element.getGraphics();
			if (g != null)
				return g;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *  Return JDialog or JFrame Parent
	 *  @param container Container
	 *  @return JDialog or JFrame of container
	 */
	public static Window getParent (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JDialog || element instanceof JFrame)
				return (Window)element;
			element = element.getParent();
		}
		return null;
	}   //  getParent

	/*************************************************************************/

	/**
	 *  Get Image with File name
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static Image getImage (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.getImage(url);
	}   //  getImage

	/**
	 *  Get ImageIcon
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static ImageIcon getImageIcon (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		return new ImageIcon(url);
	}   //  getImageIcon


	/**************************************************************************/

	/**
	 *  Start Browser
	 *  @param url url
	 */
	public static void startBrowser (String url)
	{
		Log.trace(Log.l1_User, "Env.startBrowser", url);
		//  OS command
		String cmd = "explorer ";
		if (!System.getProperty("os.name").startsWith("Win"))
			cmd = "netscape ";
		//
		String execute = cmd + url;
		try
		{
			Runtime.getRuntime().exec(execute);
		}
		catch (Exception e)
		{
			System.err.println("Env.startBrowser - " + execute + " - " + e);
		}
	}   //  startBrowser

	/**************************************************************************
	 *  Static Variables
	 */

	/**
	 *  Big Decimal Zero
	 */
	static final public java.math.BigDecimal ZERO = new java.math.BigDecimal(0.0);

	/**
	 * 	New Line
	 */
	public static final String	NL = System.getProperty("line.separator");


	/**
	 *  Static initializer
	 */
	static
	{
		//  Set English as default Language
		s_ctx.put(LANG, Language.getBaseAD_Language());
	}   //  static

}   //  Env
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
package org.compiere.util;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.security.*;
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.db.CConnection;


/**
 *  System Environment and static variables
 *
 *  @author     Jorg Janke
 *  @version    $Id: Env.java,v 1.8 2003/06/16 14:40:16 jjanke Exp $
 */
public final class Env
{
	/**	Logging								*/
	private static Logger				s_log = Logger.getLogger(Env.class);

	/**
	 *  Test Init - Set Environment for tests
	 *
	 * @param traceLevel trace level
	 * @param isClient client session
	 * @return Context
	 */
	public static Properties initTest (int traceLevel, boolean isClient)
	{
	//	logger.entering("Env", "initTest");
		org.compiere.Compiere.startupClient();
		Log.setTraceLevel(traceLevel);
		//  Test Context
		Properties ctx = Env.getCtx();
		KeyNamePair[] roles = DB.login(ctx, CConnection.get(),
			"System", "System", true);
		//  load role
		if (roles != null && roles.length > 0)
		{
			KeyNamePair[] clients = DB.loadClients (ctx, roles[0]);
			//  load client
			if (clients != null && clients.length > 0)
			{
				KeyNamePair[] whs = DB.loadWarehouses(ctx, clients[0]);
				//
				KeyNamePair[] orgs = DB.loadOrgs(ctx, clients[0]);
				//  load org
				if (orgs != null && orgs.length > 0)
				{
					DB.loadPreferences(ctx, orgs[0], null, null, null);
				}
			}
		}
		//
		Env.setContext(ctx, "#Date", "2000-01-01");
	//	logger.exiting("Env", "initTest");
		return ctx;
	}   //  testInit

	/**
	 *  Java Version Test
	 *  @return true if Java Version is OK
	 */
	public static boolean isJavaOK()
	{
		//	Java System version check
		String jVersion = System.getProperty("java.version");
		String targetVersion = "1.4.1";				//	specific release
		if (jVersion.startsWith(targetVersion)) 	//  this release
			return true;
		//  Warning
		boolean ok = false;
		if (jVersion.startsWith("1.4"))  			//  later/earlier release
			ok = true;

		//  Error Message
		StringBuffer msg = new StringBuffer();
		msg.append(System.getProperty("java.vm.name")).append(" - ").append(jVersion);
		if (ok)
			msg.append("(untested)");
		msg.append("  <>  ").append(targetVersion);
		//
		JOptionPane.showMessageDialog(null, msg.toString(),
			org.compiere.Compiere.getName() + " - Java Version Check",
			ok ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
		return ok;
	}   //  isJavaOK

	/**
	 *	Exit System
	 *  @param status System exit status (usually 0 for no error)
	 */
	public static void exitEnv (int status)
	{
		reset(true);
		s_log.info("exit");
		LogManager.shutdown();
		if (Ini.isClient())
			System.exit (status);
	}	//	close

	/**
	 * 	Reset Cache
	 * 	@param all everything otherwise login data remains
	 */
	public static void reset (boolean all)
	{
		s_log.info("reset - all=" + all);

		//	Dismantle windows
		/**
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container win = (Container)s_windows.get(i);
			if (win.getClass().getName().endsWith("AMenu")) // Null pointer
				;
			else if (win instanceof Window)
				((Window)win).dispose();
			else
				win.removeAll();
		}
		**/
		s_windows.clear();

		//	Context
		if (all)
			s_ctx.clear();
		else
		{
			Object[] keys = s_ctx.keySet().toArray();
			for (int i = 0; i < keys.length; i++)
			{
				String tag = keys[i].toString();
				if (Character.isDigit(tag.charAt(0)))
					s_ctx.remove(keys[i]);
			}
		}

		//	Cache
		CacheMgt.get().reset();
		DB.closeTarget();
	}	//	resetAll


	/*************************************************************************/

	/**
	 *  Application Context
	 */
	private static Properties   s_ctx = new Properties();
	/** WindowNo for Find           */
	public static final int     WINDOW_FIND = 1110;
	/** WinowNo for MLookup         */
	public static final int	    WINDOW_MLOOKUP = 1111;
	/** WindowNo for PrintCustomize */
	public static final int     WINDOW_CUSTOMIZE = 1112;

	/** Tab for Info                */
	public static final int     TAB_INFO = 77;

	/**
	 *  Get Context
	 *  @return Properties
	 */
	public static final Properties getCtx()
	{
		return s_ctx;
	}   //  getCtx

	/**
	 * Set Context
	 * @param ctx context
	 */
	public static void setCtx (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.setCtx - require Context");
		s_ctx.clear();
		s_ctx = ctx;
	}   //  setCtx

	/**
	 *	Set Global Context to Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		if (value == null || value.length() == 0)
			ctx.remove(context);
		else
			ctx.setProperty(context, value);
	}	//	setContext

	/**
	 *	Set Global Context to (int) Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		ctx.setProperty(context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Global Context to Y/N Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, boolean value)
	{
		setContext (ctx, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set Context for Window to int Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		ctx.setProperty(WindowNo+"|"+context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Context for Window to Y/N Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, boolean value)
	{
		setContext (ctx, WindowNo, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window & Tab to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 *  @param TabNo tab no
	 */
	public static void setContext (Properties ctx, int WindowNo, int TabNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(9, "Context("+WindowNo+","+TabNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+TabNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+TabNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set AutoCommit
	 *  @param ctx context
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty("AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Set AutoCommit for Window
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, int WindowNo, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty(WindowNo+"|AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Get global Value of Context
	 *  @param ctx context
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		return ctx.getProperty(context, "");
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available and enabled
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context, boolean onlyWindow)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+context);
		if (s == null)
		{
			if (onlyWindow)
				return "";
			if (context.startsWith("#") || context.startsWith("$"))
				return getContext(ctx, context);
			return getContext(ctx, "#" + context);
		}
		return s;
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context)
	{
		return getContext(ctx, WindowNo, context, false);
	}	//	getContext

	/**
	 *	Get Value of Context for Window & Tab,
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, int TabNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+TabNo+"|"+context);
		if (s == null)
			return getContext(ctx, WindowNo, context, false);
		return s;
	}	//	getContext

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param context context key
	 *  @return value
	 */
	public static int getContextAsInt(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		if (s.length() == 0)
			s = getContext(ctx, 0, context, false);		//	search 0 and defaults
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return value or 0
	 */
	public static int getContextAsInt(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Is AutoCommit
	 *  @param ctx context
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, "AutoCommit");
		if (s != null && s.equals("Y"))
			return true;
		return false;
	}	//	isAutoCommit

	/**
	 *	Is Window AutoCommit (if not set use default)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, "AutoCommit", false);
		if (s != null)
		{
			if (s.equals("Y"))
				return true;
			else
				return false;
		}
		return isAutoCommit(ctx);
	}	//	isAutoCommit

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, String context)
	{
		return getContextAsDate(ctx, 0, context);
	}	//	getContextAsDate

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/*************************************************************************/

	/**
	 *	Get Preference.
	 *  <pre>
	 *		0)	Current Setting
	 *		1) 	Window Preference
	 *		2) 	Global Preference
	 *		3)	Login settings
	 *		4)	Accounting settings
	 *  </pre>
	 *  @param  ctx context
	 *	@param	AD_Window_ID window no
	 *	@param	context		Entity to search
	 *	@param	system		System level preferences (vs. user defined)
	 *  @return preference value
	 */
	public static String getPreference (Properties ctx, int AD_Window_ID, String context, boolean system)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getPreference - require Context");
		String retValue = null;
		//
		if (!system)	//	User Preferences
		{
			retValue = ctx.getProperty("P"+AD_Window_ID+"|"+context);//	Window Pref
			if (retValue == null)
				retValue = ctx.getProperty("P|"+context);  			//	Global Pref
		}
		else			//	System Preferences
		{
			retValue = ctx.getProperty("#"+context);   				//	Login setting
			if (retValue == null)
				retValue = ctx.getProperty("$"+context);   			//	Accounting setting
		}
		//
		return (retValue == null ? "" : retValue);
	}	//	getPreference

	/****************************************************************************
	 *  Language issues
	 */

	/** Context Language identifier */
	static public final String      LANG = "#AD_Language";

	/**
	 *  Check Base Language
	 *  @param ctx context
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Properties ctx, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (getAD_Language(ctx));
		else	//	No AD Table
			if (!isMultiLingualDocument(ctx))
				return true;		//	access base table
		return Language.isBaseLanguage (getAD_Language(ctx));
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param AD_Language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (String AD_Language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (AD_Language);
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return Language.isBaseLanguage (AD_Language);
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Language language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			language.isBaseLanguage();
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return language.isBaseLanguage();
	}	//	isBaseLanguage

	/**
	 * 	Do we have Multi-Lingual Documents.
	 *  Set in DB.loadOrgs
	 * 	@param ctx context
	 * 	@return true if multi lingual documents
	 */
	public static boolean isMultiLingualDocument (Properties ctx)
	{
		return "Y".equals(Env.getContext(ctx, "#IsMultiLingualDocument"));
	}	//	isMultiLingualDocument

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return AD_Language eg. en_US
	 */
	public static String getAD_Language (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return lang;
		}
		return Language.getBaseAD_Language();
	}	//	getAD_Language

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return Language
	 */
	public static Language getLanguage (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return Language.getLanguage(lang);
		}
		return Language.getLanguage();
	}	//	getLanguage

	/**
	 *  Verify Language.
	 *  Check that language is supported by the system
	 *  @param ctx might be updated with new AD_Language
	 *  @param language language
	 */
	public static void verifyLanguage (Properties ctx, Language language)
	{
		ArrayList sysLang = new ArrayList();
		String sql = "SELECT AD_Language FROM AD_Language ORDER BY IsBaseLanguage DESC";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				sysLang.add(rs.getString(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("ALogin.verifyLanguage", e);
		}

		String selectedLanguage = language.getAD_Language();
		for (int i = 0; i < sysLang.size(); i++)
		{
			if (selectedLanguage.equals(sysLang.get(i)))
				return;
		}
		//  Language/Country not found - try finding similar language (i.e. first e chars)
		selectedLanguage = selectedLanguage.substring(0,2);
		for (int i = 0; i < sysLang.size(); i++)
		{
			String comp = sysLang.get(i).toString().substring(0,2);
			if (selectedLanguage.equals(comp))
			{
				language.setAD_Language(sysLang.get(i).toString());
				Env.setContext(ctx, Env.LANG, language.getAD_Language());
				return;
			}
		}
		//  none found - use Database Base Language
		language.setAD_Language(sysLang.get(0).toString());
		Env.setContext(ctx, Env.LANG, language.getAD_Language());
	}   //  verifyLanguage

	/*************************************************************************/

	/**
	 *	Get Context as String array with format: key == value
	 *  @param ctx context
	 *  @return context string
	 */
	public static String[] getEntireContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getEntireContext - require Context");
		Iterator keyIterator = ctx.keySet().iterator();
		String[] sList = new String[ctx.size()];
		int i = 0;
		while (keyIterator.hasNext())
		{
			Object key = keyIterator.next();
			sList[i++] = key.toString() + " == " + ctx.get(key).toString();
		}

		return sList;
	}	//	getEntireContext

	/**
	 *	Get Header info (connection, org, user)
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @return Header String
	 */
	public static String getHeader(Properties ctx, int WindowNo)
	{
		StringBuffer sb = new StringBuffer();
		if (WindowNo > 0)
			sb.append(getContext(ctx, WindowNo, "WindowName", false)).append("  ");
		sb.append(getContext(ctx, "#AD_User_Name")).append("@")
			.append(getContext(ctx, "#AD_Client_Name")).append(".")
			.append(getContext(ctx, "#AD_Org_Name"))
			.append(" [").append(CConnection.get().toString()).append("]");
		return sb.toString();
	}	//	getHeader

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public static void clearWinContext(Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearWinContext - require Context");
		//
		Object[] keys = ctx.keySet().toArray();
		for (int i = 0; i < keys.length; i++)
		{
			String tag = keys[i].toString();
			if (tag.startsWith(WindowNo+"|"))
				ctx.remove(keys[i]);
		}
		//  Clear Lookup Cache
		MLookupCache.cacheReset(WindowNo);
	//	MLocator.cacheReset(WindowNo);
		//
		removeWindow(WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 *  @param ctx context
	 */
	public static void clearContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearContext - require Context");
		ctx.clear();
	}	//	clearContext


	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 * 	@param	ignoreUnparsable if true, unsuccessful @tag@ are ignored otherwise "" is returned
	 *  @return parsed String or "" if not successful and ignoreUnparsable
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow, boolean ignoreUnparsable)
	{
		if (value == null)
			return "";

		String token;
		String inStr = new String(value);
		StringBuffer outStr = new StringBuffer();

		int i = inStr.indexOf("@");
		while (i != -1)
		{
			outStr.append(inStr.substring(0, i));			// up to @
			inStr = inStr.substring(i+1, inStr.length());	// from first @

			int j = inStr.indexOf("@");						// next @
			if (j < 0)
			{
				Log.error("Env.parseContext - no second tag: " + inStr);
				return "";						//	no second tag
			}

			token = inStr.substring(0, j);

			String ctxInfo = getContext(ctx, WindowNo, token, onlyWindow);	// get context
			if (ctxInfo.length() == 0 && (token.startsWith("#") || token.startsWith("$")) )
				ctxInfo = getContext(ctx, token);	// get global context
			if (ctxInfo.length() == 0)
			{
				Log.trace(Log.l5_DData, "Env.parseContext - no context (" + WindowNo + ") for: " + token);
				if (!ignoreUnparsable)
					return "";						//	token not found
			}
			else
				outStr.append(ctxInfo);				// replace context with Context

			inStr = inStr.substring(j+1, inStr.length());	// from second @
			i = inStr.indexOf("@");
		}
		outStr.append(inStr);						// add the rest of the string

		return outStr.toString();
	}	//	parseContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return parsed String or "" if not successful
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow)
	{
		return parseContext(ctx, WindowNo, value, onlyWindow, false);
	}	//	parseContext

	/*************************************************************************/

	private static ArrayList	s_windows = new ArrayList(20);

	/**
	 *	Add Container and return WindowNo.
	 *  The container is a APanel, AWindow or JFrame/JDialog
	 *  @param win window
	 *  @return WindowNo used for context
	 */
	public static int createWindowNo(Container win)
	{
		int retValue = s_windows.size();
		s_windows.add(win);
		return retValue;
	}	//	createWindowNo

	/**
	 *	Search Window by comparing the Frames
	 *  @param container container
	 *  @return WindowNo of container or 0
	 */
	public static int getWindowNo (Container container)
	{
		if (container == null)
			return 0;
		JFrame winFrame = getFrame(container);
		if (winFrame == null)
			return 0;

		//  loop through windows
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container cmp = (Container)s_windows.get(i);
			if (cmp != null)
			{
				JFrame cmpFrame = getFrame(cmp);
				if (winFrame.equals(cmpFrame))
					return i;
			}
		}
		return 0;
	}	//	getWindowNo

	/**
	 *	Return the JFrame pointer of WindowNo - or null
	 *  @param WindowNo window
	 *  @return JFrame of WindowNo
	 */
	public static JFrame getWindow (int WindowNo)
	{
		JFrame retValue = null;
		try
		{
			retValue = getFrame ((Container)s_windows.get(WindowNo));
		}
		catch (Exception e)
		{
			System.err.println("Env.getWindow - " + e);
		}
		return retValue;
	}	//	getWindow

	/**
	 *	Remove window from active list
	 *  @param WindowNo window
	 */
	private static void removeWindow (int WindowNo)
	{
		if (WindowNo <= s_windows.size())
			s_windows.set(WindowNo, null);
	}	//	removeWindow

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param WindowNo window
	 */
	public static void clearWinContext(int WindowNo)
	{
		clearWinContext (s_ctx, WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 */
	public static void clearContext()
	{
		s_ctx.clear();
	}	//	clearContext


	/*************************************************************************/

	/**
	 *	Get Frame of Window
	 *  @param container Container
	 *  @return JFrame of container or null
	 */
	public static JFrame getFrame (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JFrame)
				return (JFrame)element;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *	Get Graphics of container or its parent.
	 *  The element may not have a Graphic if not displayed yet,
	 * 	but the parent might have.
	 *  @param container Container
	 *  @return Graphics of container or null
	 */
	public static Graphics getGraphics (Container container)
	{
		Container element = container;
		while (element != null)
		{
			Graphics g = element.getGraphics();
			if (g != null)
				return g;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *  Return JDialog or JFrame Parent
	 *  @param container Container
	 *  @return JDialog or JFrame of container
	 */
	public static Window getParent (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JDialog || element instanceof JFrame)
				return (Window)element;
			element = element.getParent();
		}
		return null;
	}   //  getParent

	/*************************************************************************/

	/**
	 *  Get Image with File name
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static Image getImage (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.getImage(url);
	}   //  getImage

	/**
	 *  Get ImageIcon
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static ImageIcon getImageIcon (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		return new ImageIcon(url);
	}   //  getImageIcon


	/**************************************************************************/

	/**
	 *  Start Browser
	 *  @param url url
	 */
	public static void startBrowser (String url)
	{
		Log.trace(Log.l1_User, "Env.startBrowser", url);
		//  OS command
		String cmd = "explorer ";
		if (!System.getProperty("os.name").startsWith("Win"))
			cmd = "netscape ";
		//
		String execute = cmd + url;
		try
		{
			Runtime.getRuntime().exec(execute);
		}
		catch (Exception e)
		{
			System.err.println("Env.startBrowser - " + execute + " - " + e);
		}
	}   //  startBrowser

	/**************************************************************************
	 *  Static Variables
	 */

	/**
	 *  Big Decimal Zero
	 */
	static final public java.math.BigDecimal ZERO = new java.math.BigDecimal(0.0);

	/**
	 * 	New Line
	 */
	public static final String	NL = System.getProperty("line.separator");


	/**
	 *  Static initializer
	 */
	static
	{
		//  Set English as default Language
		s_ctx.put(LANG, Language.getBaseAD_Language());
	}   //  static

}   //  Env
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
package org.compiere.util;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.security.*;
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.db.CConnection;


/**
 *  System Environment and static variables
 *
 *  @author     Jorg Janke
 *  @version    $Id: Env.java,v 1.9 2003/08/11 05:56:23 jjanke Exp $
 */
public final class Env
{
	/**	Logging								*/
	private static Logger				s_log = Logger.getLogger(Env.class);

	/**
	 *  Test Init - Set Environment for tests
	 *
	 * @param traceLevel trace level
	 * @param isClient client session
	 * @return Context
	 */
	public static Properties initTest (int traceLevel, boolean isClient)
	{
	//	logger.entering("Env", "initTest");
		org.compiere.Compiere.startupClient();
		Log.setTraceLevel(traceLevel);
		//  Test Context
		Properties ctx = Env.getCtx();
		KeyNamePair[] roles = DB.login(ctx, CConnection.get(),
			"System", "System", true);
		//  load role
		if (roles != null && roles.length > 0)
		{
			KeyNamePair[] clients = DB.loadClients (ctx, roles[0]);
			//  load client
			if (clients != null && clients.length > 0)
			{
				KeyNamePair[] whs = DB.loadWarehouses(ctx, clients[0]);
				//
				KeyNamePair[] orgs = DB.loadOrgs(ctx, clients[0]);
				//  load org
				if (orgs != null && orgs.length > 0)
				{
					DB.loadPreferences(ctx, orgs[0], null, null, null);
				}
			}
		}
		//
		Env.setContext(ctx, "#Date", "2000-01-01");
	//	logger.exiting("Env", "initTest");
		return ctx;
	}   //  testInit

	/**
	 *  Java Version Test
	 *  @return true if Java Version is OK
	 */
	public static boolean isJavaOK()
	{
		//	Java System version check
		String jVersion = System.getProperty("java.version");
		String targetVersion = "1.4.1";				//	specific release
		if (jVersion.startsWith(targetVersion)) 	//  this release
			return true;
		//  Warning
		boolean ok = false;
		if (jVersion.startsWith("1.4"))  			//  later/earlier release
			ok = true;

		//  Error Message
		StringBuffer msg = new StringBuffer();
		msg.append(System.getProperty("java.vm.name")).append(" - ").append(jVersion);
		if (ok)
			msg.append("(untested)");
		msg.append("  <>  ").append(targetVersion);
		//
		JOptionPane.showMessageDialog(null, msg.toString(),
			org.compiere.Compiere.getName() + " - Java Version Check",
			ok ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
		return ok;
	}   //  isJavaOK

	/**
	 *	Exit System
	 *  @param status System exit status (usually 0 for no error)
	 */
	public static void exitEnv (int status)
	{
		reset(true);
		s_log.info("exit");
		LogManager.shutdown();
		if (Ini.isClient())
			System.exit (status);
	}	//	close

	/**
	 * 	Reset Cache
	 * 	@param all everything otherwise login data remains
	 */
	public static void reset (boolean all)
	{
		s_log.info("reset - all=" + all);

		//	Dismantle windows
		/**
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container win = (Container)s_windows.get(i);
			if (win.getClass().getName().endsWith("AMenu")) // Null pointer
				;
			else if (win instanceof Window)
				((Window)win).dispose();
			else
				win.removeAll();
		}
		**/
		s_windows.clear();

		//	Context
		if (all)
			s_ctx.clear();
		else
		{
			Object[] keys = s_ctx.keySet().toArray();
			for (int i = 0; i < keys.length; i++)
			{
				String tag = keys[i].toString();
				if (Character.isDigit(tag.charAt(0)))
					s_ctx.remove(keys[i]);
			}
		}

		//	Cache
		CacheMgt.get().reset();
		DB.closeTarget();
	}	//	resetAll


	/*************************************************************************/

	/**
	 *  Application Context
	 */
	private static Properties   s_ctx = new Properties();
	/** WindowNo for Find           */
	public static final int     WINDOW_FIND = 1110;
	/** WinowNo for MLookup         */
	public static final int	    WINDOW_MLOOKUP = 1111;
	/** WindowNo for PrintCustomize */
	public static final int     WINDOW_CUSTOMIZE = 1112;

	/** Tab for Info                */
	public static final int     TAB_INFO = 77;

	/**
	 *  Get Context
	 *  @return Properties
	 */
	public static final Properties getCtx()
	{
		return s_ctx;
	}   //  getCtx

	/**
	 * Set Context
	 * @param ctx context
	 */
	public static void setCtx (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.setCtx - require Context");
		s_ctx.clear();
		s_ctx = ctx;
	}   //  setCtx

	/**
	 *	Set Global Context to Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		if (value == null || value.length() == 0)
			ctx.remove(context);
		else
			ctx.setProperty(context, value);
	}	//	setContext

	/**
	 *	Set Global Context to (int) Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		ctx.setProperty(context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Global Context to Y/N Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, boolean value)
	{
		setContext (ctx, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set Context for Window to int Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		ctx.setProperty(WindowNo+"|"+context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Context for Window to Y/N Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, boolean value)
	{
		setContext (ctx, WindowNo, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window & Tab to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 *  @param TabNo tab no
	 */
	public static void setContext (Properties ctx, int WindowNo, int TabNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(9, "Context("+WindowNo+","+TabNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+TabNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+TabNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set AutoCommit
	 *  @param ctx context
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty("AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Set AutoCommit for Window
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, int WindowNo, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty(WindowNo+"|AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Get global Value of Context
	 *  @param ctx context
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		return ctx.getProperty(context, "");
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available and enabled
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context, boolean onlyWindow)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+context);
		if (s == null)
		{
			if (onlyWindow)
				return "";
			if (context.startsWith("#") || context.startsWith("$"))
				return getContext(ctx, context);
			return getContext(ctx, "#" + context);
		}
		return s;
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context)
	{
		return getContext(ctx, WindowNo, context, false);
	}	//	getContext

	/**
	 *	Get Value of Context for Window & Tab,
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, int TabNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+TabNo+"|"+context);
		if (s == null)
			return getContext(ctx, WindowNo, context, false);
		return s;
	}	//	getContext

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param context context key
	 *  @return value
	 */
	public static int getContextAsInt(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		if (s.length() == 0)
			s = getContext(ctx, 0, context, false);		//	search 0 and defaults
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return value or 0
	 */
	public static int getContextAsInt(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Is AutoCommit
	 *  @param ctx context
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, "AutoCommit");
		if (s != null && s.equals("Y"))
			return true;
		return false;
	}	//	isAutoCommit

	/**
	 *	Is Window AutoCommit (if not set use default)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, "AutoCommit", false);
		if (s != null)
		{
			if (s.equals("Y"))
				return true;
			else
				return false;
		}
		return isAutoCommit(ctx);
	}	//	isAutoCommit

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, String context)
	{
		return getContextAsDate(ctx, 0, context);
	}	//	getContextAsDate

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/*************************************************************************/

	/**
	 *	Get Preference.
	 *  <pre>
	 *		0)	Current Setting
	 *		1) 	Window Preference
	 *		2) 	Global Preference
	 *		3)	Login settings
	 *		4)	Accounting settings
	 *  </pre>
	 *  @param  ctx context
	 *	@param	AD_Window_ID window no
	 *	@param	context		Entity to search
	 *	@param	system		System level preferences (vs. user defined)
	 *  @return preference value
	 */
	public static String getPreference (Properties ctx, int AD_Window_ID, String context, boolean system)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getPreference - require Context");
		String retValue = null;
		//
		if (!system)	//	User Preferences
		{
			retValue = ctx.getProperty("P"+AD_Window_ID+"|"+context);//	Window Pref
			if (retValue == null)
				retValue = ctx.getProperty("P|"+context);  			//	Global Pref
		}
		else			//	System Preferences
		{
			retValue = ctx.getProperty("#"+context);   				//	Login setting
			if (retValue == null)
				retValue = ctx.getProperty("$"+context);   			//	Accounting setting
		}
		//
		return (retValue == null ? "" : retValue);
	}	//	getPreference

	/****************************************************************************
	 *  Language issues
	 */

	/** Context Language identifier */
	static public final String      LANG = "#AD_Language";

	/**
	 *  Check Base Language
	 *  @param ctx context
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Properties ctx, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (getAD_Language(ctx));
		else	//	No AD Table
			if (!isMultiLingualDocument(ctx))
				return true;		//	access base table
		return Language.isBaseLanguage (getAD_Language(ctx));
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param AD_Language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (String AD_Language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (AD_Language);
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return Language.isBaseLanguage (AD_Language);
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Language language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			language.isBaseLanguage();
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return language.isBaseLanguage();
	}	//	isBaseLanguage

	/**
	 * 	Do we have Multi-Lingual Documents.
	 *  Set in DB.loadOrgs
	 * 	@param ctx context
	 * 	@return true if multi lingual documents
	 */
	public static boolean isMultiLingualDocument (Properties ctx)
	{
		return "Y".equals(Env.getContext(ctx, "#IsMultiLingualDocument"));
	}	//	isMultiLingualDocument

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return AD_Language eg. en_US
	 */
	public static String getAD_Language (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return lang;
		}
		return Language.getBaseAD_Language();
	}	//	getAD_Language

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return Language
	 */
	public static Language getLanguage (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return Language.getLanguage(lang);
		}
		return Language.getLanguage();
	}	//	getLanguage

	/**
	 *  Verify Language.
	 *  Check that language is supported by the system
	 *  @param ctx might be updated with new AD_Language
	 *  @param language language
	 */
	public static void verifyLanguage (Properties ctx, Language language)
	{
		ArrayList sysLang = new ArrayList();
		String sql = "SELECT AD_Language FROM AD_Language ORDER BY IsBaseLanguage DESC";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				sysLang.add(rs.getString(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("ALogin.verifyLanguage", e);
		}

		String selectedLanguage = language.getAD_Language();
		for (int i = 0; i < sysLang.size(); i++)
		{
			if (selectedLanguage.equals(sysLang.get(i)))
				return;
		}
		//  Language/Country not found - try finding similar language (i.e. first e chars)
		selectedLanguage = selectedLanguage.substring(0,2);
		for (int i = 0; i < sysLang.size(); i++)
		{
			String comp = sysLang.get(i).toString().substring(0,2);
			if (selectedLanguage.equals(comp))
			{
				language.setAD_Language(sysLang.get(i).toString());
				Env.setContext(ctx, Env.LANG, language.getAD_Language());
				return;
			}
		}
		//  none found - use Database Base Language
		language.setAD_Language(sysLang.get(0).toString());
		Env.setContext(ctx, Env.LANG, language.getAD_Language());
	}   //  verifyLanguage

	/*************************************************************************/

	/**
	 *	Get Context as String array with format: key == value
	 *  @param ctx context
	 *  @return context string
	 */
	public static String[] getEntireContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getEntireContext - require Context");
		Iterator keyIterator = ctx.keySet().iterator();
		String[] sList = new String[ctx.size()];
		int i = 0;
		while (keyIterator.hasNext())
		{
			Object key = keyIterator.next();
			sList[i++] = key.toString() + " == " + ctx.get(key).toString();
		}

		return sList;
	}	//	getEntireContext

	/**
	 *	Get Header info (connection, org, user)
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @return Header String
	 */
	public static String getHeader(Properties ctx, int WindowNo)
	{
		StringBuffer sb = new StringBuffer();
		if (WindowNo > 0)
			sb.append(getContext(ctx, WindowNo, "WindowName", false)).append("  ");
		sb.append(getContext(ctx, "#AD_User_Name")).append("@")
			.append(getContext(ctx, "#AD_Client_Name")).append(".")
			.append(getContext(ctx, "#AD_Org_Name"))
			.append(" [").append(CConnection.get().toString()).append("]");
		return sb.toString();
	}	//	getHeader

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public static void clearWinContext(Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearWinContext - require Context");
		//
		Object[] keys = ctx.keySet().toArray();
		for (int i = 0; i < keys.length; i++)
		{
			String tag = keys[i].toString();
			if (tag.startsWith(WindowNo+"|"))
				ctx.remove(keys[i]);
		}
		//  Clear Lookup Cache
		MLookupCache.cacheReset(WindowNo);
	//	MLocator.cacheReset(WindowNo);
		//
		removeWindow(WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 *  @param ctx context
	 */
	public static void clearContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearContext - require Context");
		ctx.clear();
	}	//	clearContext


	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 * 	@param	ignoreUnparsable if true, unsuccessful @tag@ are ignored otherwise "" is returned
	 *  @return parsed String or "" if not successful and ignoreUnparsable
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow, boolean ignoreUnparsable)
	{
		if (value == null)
			return "";

		String token;
		String inStr = new String(value);
		StringBuffer outStr = new StringBuffer();

		int i = inStr.indexOf("@");
		while (i != -1)
		{
			outStr.append(inStr.substring(0, i));			// up to @
			inStr = inStr.substring(i+1, inStr.length());	// from first @

			int j = inStr.indexOf("@");						// next @
			if (j < 0)
			{
				Log.error("Env.parseContext - no second tag: " + inStr);
				return "";						//	no second tag
			}

			token = inStr.substring(0, j);

			String ctxInfo = getContext(ctx, WindowNo, token, onlyWindow);	// get context
			if (ctxInfo.length() == 0 && (token.startsWith("#") || token.startsWith("$")) )
				ctxInfo = getContext(ctx, token);	// get global context
			if (ctxInfo.length() == 0)
			{
				Log.trace(Log.l5_DData, "Env.parseContext - no context (" + WindowNo + ") for: " + token);
				if (!ignoreUnparsable)
					return "";						//	token not found
			}
			else
				outStr.append(ctxInfo);				// replace context with Context

			inStr = inStr.substring(j+1, inStr.length());	// from second @
			i = inStr.indexOf("@");
		}
		outStr.append(inStr);						// add the rest of the string

		return outStr.toString();
	}	//	parseContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return parsed String or "" if not successful
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow)
	{
		return parseContext(ctx, WindowNo, value, onlyWindow, false);
	}	//	parseContext

	/*************************************************************************/

	private static ArrayList	s_windows = new ArrayList(20);

	/**
	 *	Add Container and return WindowNo.
	 *  The container is a APanel, AWindow or JFrame/JDialog
	 *  @param win window
	 *  @return WindowNo used for context
	 */
	public static int createWindowNo(Container win)
	{
		int retValue = s_windows.size();
		s_windows.add(win);
		return retValue;
	}	//	createWindowNo

	/**
	 *	Search Window by comparing the Frames
	 *  @param container container
	 *  @return WindowNo of container or 0
	 */
	public static int getWindowNo (Container container)
	{
		if (container == null)
			return 0;
		JFrame winFrame = getFrame(container);
		if (winFrame == null)
			return 0;

		//  loop through windows
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container cmp = (Container)s_windows.get(i);
			if (cmp != null)
			{
				JFrame cmpFrame = getFrame(cmp);
				if (winFrame.equals(cmpFrame))
					return i;
			}
		}
		return 0;
	}	//	getWindowNo

	/**
	 *	Return the JFrame pointer of WindowNo - or null
	 *  @param WindowNo window
	 *  @return JFrame of WindowNo
	 */
	public static JFrame getWindow (int WindowNo)
	{
		JFrame retValue = null;
		try
		{
			retValue = getFrame ((Container)s_windows.get(WindowNo));
		}
		catch (Exception e)
		{
			System.err.println("Env.getWindow - " + e);
		}
		return retValue;
	}	//	getWindow

	/**
	 *	Remove window from active list
	 *  @param WindowNo window
	 */
	private static void removeWindow (int WindowNo)
	{
		if (WindowNo <= s_windows.size())
			s_windows.set(WindowNo, null);
	}	//	removeWindow

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param WindowNo window
	 */
	public static void clearWinContext(int WindowNo)
	{
		clearWinContext (s_ctx, WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 */
	public static void clearContext()
	{
		s_ctx.clear();
	}	//	clearContext


	/*************************************************************************/

	/**
	 *	Get Frame of Window
	 *  @param container Container
	 *  @return JFrame of container or null
	 */
	public static JFrame getFrame (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JFrame)
				return (JFrame)element;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *	Get Graphics of container or its parent.
	 *  The element may not have a Graphic if not displayed yet,
	 * 	but the parent might have.
	 *  @param container Container
	 *  @return Graphics of container or null
	 */
	public static Graphics getGraphics (Container container)
	{
		Container element = container;
		while (element != null)
		{
			Graphics g = element.getGraphics();
			if (g != null)
				return g;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *  Return JDialog or JFrame Parent
	 *  @param container Container
	 *  @return JDialog or JFrame of container
	 */
	public static Window getParent (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JDialog || element instanceof JFrame)
				return (Window)element;
			element = element.getParent();
		}
		return null;
	}   //  getParent

	/*************************************************************************/

	/**
	 *  Get Image with File name
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static Image getImage (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.getImage(url);
	}   //  getImage

	/**
	 *  Get ImageIcon
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static ImageIcon getImageIcon (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		return new ImageIcon(url);
	}   //  getImageIcon


	/**************************************************************************/

	/**
	 *  Start Browser
	 *  @param url url
	 */
	public static void startBrowser (String url)
	{
		Log.trace(Log.l1_User, "Env.startBrowser", url);
		//  OS command
		String cmd = "explorer ";
		if (!System.getProperty("os.name").startsWith("Win"))
			cmd = "netscape ";
		//
		String execute = cmd + url;
		try
		{
			Runtime.getRuntime().exec(execute);
		}
		catch (Exception e)
		{
			System.err.println("Env.startBrowser - " + execute + " - " + e);
		}
	}   //  startBrowser

	/**************************************************************************
	 *  Static Variables
	 */

	/**
	 *  Big Decimal Zero
	 */
	static final public java.math.BigDecimal ZERO = new java.math.BigDecimal(0.0);
	static final public java.math.BigDecimal ONE = new java.math.BigDecimal(1.0);

	/**
	 * 	New Line
	 */
	public static final String	NL = System.getProperty("line.separator");


	/**
	 *  Static initializer
	 */
	static
	{
		//  Set English as default Language
		s_ctx.put(LANG, Language.getBaseAD_Language());
	}   //  static

}   //  Env
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
package org.compiere.util;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.security.*;
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.db.CConnection;


/**
 *  System Environment and static variables
 *
 *  @author     Jorg Janke
 *  @version    $Id: Env.java,v 1.3 2003/02/15 06:32:50 jjanke Exp $
 */
public final class Env
{
	/**	Logging								*/
	private static Logger				s_log = Logger.getLogger("org.compiere.util.Env");

	/**
	 *  Test Init - Set Environment for tests
	 *
	 * @param traceLevel trace level
	 * @param isClient client session
	 * @return Context
	 */
	public static Properties initTest (int traceLevel, boolean isClient)
	{
	//	logger.entering("Env", "initTest");
		org.compiere.Compiere.startupClient();
		Log.setTraceLevel(traceLevel);
		//  Test Context
		Properties ctx = Env.getCtx();
		KeyNamePair[] roles = DB.login(ctx, CConnection.get(),
			"System", "System", true);
		//  load role
		if (roles != null && roles.length > 0)
		{
			KeyNamePair[] clients = DB.loadClients (ctx, roles[0]);
			//  load client
			if (clients != null && clients.length > 0)
			{
				KeyNamePair[] whs = DB.loadWarehouses(ctx, clients[0]);
				//
				KeyNamePair[] orgs = DB.loadOrgs(ctx, clients[0]);
				//  load org
				if (orgs != null && orgs.length > 0)
				{
					DB.loadPreferences(ctx, orgs[0], null, null, null);
				}
			}
		}
		//
		Env.setContext(ctx, "#Date", "2000-05-01");
	//	logger.exiting("Env", "initTest");
		return ctx;
	}   //  testInit

	/**
	 *  Java Version Test
	 *  @return true if Java Version is OK
	 */
	public static boolean isJavaOK()
	{
		//	Java System version check
		String jVersion = System.getProperty("java.version");
		String targetVersion = "1.4.";
		if (jVersion.startsWith(targetVersion))		//  same version
			return true;
		//  Warning
		boolean ok = false;
		if (jVersion.startsWith("1.4.2"))  			//  later release
			ok = true;

		//  Error Message
		StringBuffer msg = new StringBuffer();
		msg.append(System.getProperty("java.vm.name")).append(" - ").append(jVersion);
		if (ok)
			msg.append("(untested)");
		msg.append("  <>  ").append(targetVersion);
		//
		JOptionPane.showMessageDialog(null, msg.toString(),
			org.compiere.Compiere.getName() + " - Java Version Check",
			ok ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
		return ok;
	}   //  isJavaOK

	/**
	 *	Exit System
	 *  @param status System exit status (usually 0 for no error)
	 */
	public static void exitEnv (int status)
	{
		s_log.info("exit");
		DB.closeTarget();
		s_windows.clear();
		s_ctx.clear();
		Msg.reset();
		LogManager.shutdown();
		if (Ini.isClient())
			System.exit (status);
	}	//	close

	/*************************************************************************/

	/**
	 *  Application Context
	 */
	private static Properties   s_ctx = new Properties();
	/** WindowNo for Find           */
	public static final int     WINDOW_FIND = 1110;
	/** WinowNo for MLookup         */
	public static final int	    WINDOW_MLOOKUP = 1111;
	/** WindowNo for PrintCustomize */
	public static final int     WINDOW_CUSTOMIZE = 1112;

	/** Tab for Info                */
	public static final int     TAB_INFO = 77;

	/**
	 *  Get Context
	 *  @return Properties
	 */
	public static final Properties getCtx()
	{
		return s_ctx;
	}   //  getCtx

	/**
	 * Set Context
	 * @param ctx context
	 */
	public static void setCtx (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.setCtx - require Context");
		s_ctx.clear();
		s_ctx = ctx;
	}   //  setCtx

	/**
	 *	Set Global Context to Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		if (value == null || value.length() == 0)
			ctx.remove(context);
		else
			ctx.setProperty(context, value);
	}	//	setContext

	/**
	 *	Set Global Context to (int) Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		ctx.setProperty(context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Global Context to Y/N Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, boolean value)
	{
		setContext (ctx, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set Context for Window to int Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		ctx.setProperty(WindowNo+"|"+context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Context for Window to Y/N Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, boolean value)
	{
		setContext (ctx, WindowNo, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window & Tab to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 *  @param TabNo tab no
	 */
	public static void setContext (Properties ctx, int WindowNo, int TabNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(9, "Context("+WindowNo+","+TabNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+TabNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+TabNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set AutoCommit
	 *  @param ctx context
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty("AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Set AutoCommit for Window
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, int WindowNo, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty(WindowNo+"|AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Get global Value of Context
	 *  @param ctx context
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		return ctx.getProperty(context, "");
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available and enabled
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context, boolean onlyWindow)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+context);
		if (s == null)
		{
			if (onlyWindow)
				return "";
			if (context.startsWith("#") || context.startsWith("$"))
				return getContext(ctx, context);
			return getContext(ctx, "#" + context);
		}
		return s;
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context)
	{
		return getContext(ctx, WindowNo, context, false);
	}	//	getContext

	/**
	 *	Get Value of Context for Window & Tab,
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, int TabNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+TabNo+"|"+context);
		if (s == null)
			return getContext(ctx, WindowNo, context, false);
		return s;
	}	//	getContext

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param context context key
	 *  @return value
	 */
	public static int getContextAsInt(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return value or 0
	 */
	public static int getContextAsInt(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Is AutoCommit
	 *  @param ctx context
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, "AutoCommit");
		if (s != null && s.equals("Y"))
			return true;
		return false;
	}	//	isAutoCommit

	/**
	 *	Is Window AutoCommit (if not set use default)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, "AutoCommit", false);
		if (s != null)
		{
			if (s.equals("Y"))
				return true;
			else
				return false;
		}
		return isAutoCommit(ctx);
	}	//	isAutoCommit

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/*************************************************************************/

	/**
	 *	Get Preference.
	 *  <pre>
	 *		0)	Current Setting
	 *		1) 	Window Preference
	 *		2) 	Global Preference
	 *		3)	Login settings
	 *		4)	Accounting settings
	 *  </pre>
	 *  @param  ctx context
	 *	@param	AD_Window_ID window no
	 *	@param	context		Entity to search
	 *	@param	system		System level preferences (vs. user defined)
	 *  @return preference value
	 */
	public static String getPreference (Properties ctx, int AD_Window_ID, String context, boolean system)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getPreference - require Context");
		String retValue = null;
		//
		if (!system)	//	User Preferences
		{
			retValue = ctx.getProperty("P"+AD_Window_ID+"|"+context);//	Window Pref
			if (retValue == null)
				retValue = ctx.getProperty("P|"+context);  			//	Global Pref
		}
		else			//	System Preferences
		{
			retValue = ctx.getProperty("#"+context);   				//	Login setting
			if (retValue == null)
				retValue = ctx.getProperty("$"+context);   			//	Accounting setting
		}
		//
		return (retValue == null ? "" : retValue);
	}	//	getPreference

	/****************************************************************************
	 *  Language issues
	 */

	/** Context Language identifier */
	static public final String      LANG = "#AD_Language";

	/**
	 *  Check Base Language
	 *  @param ctx context
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Properties ctx, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (getAD_Language(ctx));
		else	//	No AD Table
			if (!isMultiLingualDocument(ctx))
				return true;		//	access base table
		return Language.isBaseLanguage (getAD_Language(ctx));
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param AD_Language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (String AD_Language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (AD_Language);
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return Language.isBaseLanguage (AD_Language);
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Language language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			language.isBaseLanguage();
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return language.isBaseLanguage();
	}	//	isBaseLanguage

	/**
	 * 	Do we have Multi-Lingual Documents.
	 *  Set in DB.loadOrgs
	 * 	@param ctx context
	 * 	@return true if multi lingual documents
	 */
	public static boolean isMultiLingualDocument (Properties ctx)
	{
		return "Y".equals(Env.getContext(ctx, "#IsMultiLingualDocument"));
	}	//	isMultiLingualDocument

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return AD_Language eg. en_US
	 */
	public static String getAD_Language (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return lang;
		}
		return Language.getBaseAD_Language();
	}	//	getAD_Language

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return Language
	 */
	public static Language getLanguage (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return Language.getLanguage(lang);
		}
		return Language.getLanguage();
	}	//	getLanguage

	/**
	 *  Verify Language.
	 *  Check that language is supported by the system
	 *  @param ctx might be updated with new AD_Language
	 *  @param language language
	 */
	public static void verifyLanguage (Properties ctx, Language language)
	{
		ArrayList sysLang = new ArrayList();
		String sql = "SELECT AD_Language FROM AD_Language ORDER BY IsBaseLanguage DESC";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				sysLang.add(rs.getString(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("ALogin.verifyLanguage", e);
		}

		String selectedLanguage = language.getAD_Language();
		for (int i = 0; i < sysLang.size(); i++)
		{
			if (selectedLanguage.equals(sysLang.get(i)))
				return;
		}
		//  Language/Country not found - try finding similar language (i.e. first e chars)
		selectedLanguage = selectedLanguage.substring(0,2);
		for (int i = 0; i < sysLang.size(); i++)
		{
			String comp = sysLang.get(i).toString().substring(0,2);
			if (selectedLanguage.equals(comp))
			{
				language.setAD_Language(sysLang.get(i).toString());
				Env.setContext(ctx, Env.LANG, language.getAD_Language());
				return;
			}
		}
		//  none found - use Database Base Language
		language.setAD_Language(sysLang.get(0).toString());
		Env.setContext(ctx, Env.LANG, language.getAD_Language());
	}   //  verifyLanguage

	/*************************************************************************/

	/**
	 *	Get Context as String array with format: key == value
	 *  @param ctx context
	 *  @return context string
	 */
	public static String[] getEntireContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getEntireContext - require Context");
		Iterator keyIterator = ctx.keySet().iterator();
		String[] sList = new String[ctx.size()];
		int i = 0;
		while (keyIterator.hasNext())
		{
			Object key = keyIterator.next();
			sList[i++] = key.toString() + " == " + ctx.get(key).toString();
		}

		return sList;
	}	//	getEntireContext

	/**
	 *	Get Header info (connection, org, user)
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @return Header String
	 */
	public static String getHeader(Properties ctx, int WindowNo)
	{
		StringBuffer sb = new StringBuffer();
		if (WindowNo > 0)
			sb.append(getContext(ctx, WindowNo, "WindowName", false)).append("  ");
		sb.append(getContext(ctx, "#AD_User_Name")).append("@")
			.append(getContext(ctx, "#AD_Client_Name")).append(".")
			.append(getContext(ctx, "#AD_Org_Name"))
			.append(" [").append(CConnection.get().toString()).append("]");
		return sb.toString();
	}	//	getHeader

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public static void clearWinContext(Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearWinContext - require Context");
		//
		Object[] keys = ctx.keySet().toArray();
		for (int i = 0; i < keys.length; i++)
		{
			String tag = keys[i].toString();
			if (tag.startsWith(WindowNo+"|"))
				ctx.remove(keys[i]);
		}
		//  Clear Lookup Cache
		MLookupCache.cacheReset(WindowNo);
	//	MLocator.cacheReset(WindowNo);
		//
		removeWindow(WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 *  @param ctx context
	 */
	public static void clearContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearContext - require Context");
		ctx.clear();
	}	//	clearContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 * 	@param	ignoreUnparsable if true, unsuccessful @tag@ are ignored otherwise "" is returned
	 *  @return parsed String or "" if not successful and ignoreUnparsable
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow, boolean ignoreUnparsable)
	{
		if (value == null)
			return "";

		String token;
		String inStr = new String(value);
		StringBuffer outStr = new StringBuffer();

		int i = inStr.indexOf("@");
		while (i != -1)
		{
			outStr.append(inStr.substring(0, i));			// up to @
			inStr = inStr.substring(i+1, inStr.length());	// from first @

			int j = inStr.indexOf("@");						// next @
			if (j < 0)
			{
				Log.error("Env.parseContext - no second tag: " + inStr);
				return "";						//	no second tag
			}

			token = inStr.substring(0, j);

			String ctxInfo = getContext(ctx, WindowNo, token, onlyWindow);	// get context
			if (ctxInfo.length() == 0 && (token.startsWith("#") || token.startsWith("$")) )
				ctxInfo = getContext(ctx, token);	// get global context
			if (ctxInfo.length() == 0)
			{
				Log.trace(Log.l5_DData, "Env.parseContext - no context (" + WindowNo + ") for: " + token);
				if (!ignoreUnparsable)
					return "";						//	token not found
			}
			else
				outStr.append(ctxInfo);				// replace context with Context

			inStr = inStr.substring(j+1, inStr.length());	// from second @
			i = inStr.indexOf("@");
		}
		outStr.append(inStr);						// add the rest of the string

		return outStr.toString();
	}	//	parseContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return parsed String or "" if not successful
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow)
	{
		return parseContext(ctx, WindowNo, value, onlyWindow, false);
	}	//	parseContext

	/*************************************************************************/

	private static ArrayList	s_windows = new ArrayList(20);

	/**
	 *	Add Container and return WindowNo.
	 *  The container is a APanel, AWindow or JFrame/JDialog
	 *  @param win window
	 *  @return WindowNo used for context
	 */
	public static int createWindowNo(Container win)
	{
		int retValue = s_windows.size();
		s_windows.add(win);
		return retValue;
	}	//	createWindowNo

	/**
	 *	Search Window by comparing the Frames
	 *  @param container container
	 *  @return WindowNo of container or 0
	 */
	public static int getWindowNo (Container container)
	{
		if (container == null)
			return 0;
		JFrame winFrame = getFrame(container);
		if (winFrame == null)
			return 0;

		//  loop through windows
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container cmp = (Container)s_windows.get(i);
			if (cmp != null)
			{
				JFrame cmpFrame = getFrame(cmp);
				if (winFrame.equals(cmpFrame))
					return i;
			}
		}
		return 0;
	}	//	getWindowNo

	/**
	 *	Return the JFrame pointer of WindowNo - or null
	 *  @param WindowNo window
	 *  @return JFrame of WindowNo
	 */
	public static JFrame getWindow (int WindowNo)
	{
		JFrame retValue = null;
		try
		{
			retValue = getFrame ((Container)s_windows.get(WindowNo));
		}
		catch (Exception e)
		{
			System.err.println("Env.getWindow - " + e);
		}
		return retValue;
	}	//	getWindow

	/**
	 *	Remove window from active list
	 *  @param WindowNo window
	 */
	private static void removeWindow (int WindowNo)
	{
		if (WindowNo <= s_windows.size())
			s_windows.set(WindowNo, null);
	}	//	removeWindow

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param WindowNo window
	 */
	public static void clearWinContext(int WindowNo)
	{
		clearWinContext (s_ctx, WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 */
	public static void clearContext()
	{
		s_ctx.clear();
	}	//	clearContext


	/*************************************************************************/

	/**
	 *	Get Frame of Window
	 *  @param container Container
	 *  @return JFrame of container or null
	 */
	public static JFrame getFrame (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JFrame)
				return (JFrame)element;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *	Get Graphics of container or its parent.
	 *  The element may not have a Graphic if not displayed yet,
	 * 	but the parent might have.
	 *  @param container Container
	 *  @return Graphics of container or null
	 */
	public static Graphics getGraphics (Container container)
	{
		Container element = container;
		while (element != null)
		{
			Graphics g = element.getGraphics();
			if (g != null)
				return g;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *  Return JDialog or JFrame Parent
	 *  @param container Container
	 *  @return JDialog or JFrame of container
	 */
	public static Window getParent (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JDialog || element instanceof JFrame)
				return (Window)element;
			element = element.getParent();
		}
		return null;
	}   //  getParent

	/*************************************************************************/

	/**
	 *  Get Image with File name
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static Image getImage (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.getImage(url);
	}   //  getImage

	/**
	 *  Get ImageIcon
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static ImageIcon getImageIcon (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		return new ImageIcon(url);
	}   //  getImageIcon


	/**************************************************************************/

	/**
	 *  Start Browser
	 *  @param url url
	 */
	public static void startBrowser (String url)
	{
		Log.trace(Log.l1_User, "Env.startBrowser", url);
		//  OS command
		String cmd = "explorer ";
		if (!System.getProperty("os.name").startsWith("Win"))
			cmd = "netscape ";
		//
		String execute = cmd + url;
		try
		{
			Runtime.getRuntime().exec(execute);
		}
		catch (Exception e)
		{
			System.err.println("Env.startBrowser - " + execute + " - " + e);
		}
	}   //  startBrowser

	/**************************************************************************
	 *  Static Variables
	 */

	/**
	 *  Big Decimal Zero
	 */
	static final public java.math.BigDecimal ZERO = new java.math.BigDecimal(0.0);

	/**
	 * 	New Line
	 */
	public static final String	NL = System.getProperty("line.separator");


	/**
	 *  Static initializer
	 */
	static
	{
		//  Set English as default Language
		s_ctx.put(LANG, Language.getBaseAD_Language());
	}   //  static

}   //  Env
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
package org.compiere.util;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.security.*;
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import org.compiere.Compiere;
import org.compiere.model.*;
import org.compiere.db.CConnection;


/**
 *  System Environment and static variables
 *
 *  @author     Jorg Janke
 *  @version    $Id: Env.java,v 1.8 2003/06/16 14:40:16 jjanke Exp $
 */
public final class Env
{
	/**	Logging								*/
	private static Logger				s_log = Logger.getLogger(Env.class);

	/**
	 *  Test Init - Set Environment for tests
	 *
	 * @param traceLevel trace level
	 * @param isClient client session
	 * @return Context
	 */
	public static Properties initTest (int traceLevel, boolean isClient)
	{
	//	logger.entering("Env", "initTest");
		org.compiere.Compiere.startupClient();
		Log.setTraceLevel(traceLevel);
		//  Test Context
		Properties ctx = Env.getCtx();
		KeyNamePair[] roles = DB.login(ctx, CConnection.get(),
			"System", "System", true);
		//  load role
		if (roles != null && roles.length > 0)
		{
			KeyNamePair[] clients = DB.loadClients (ctx, roles[0]);
			//  load client
			if (clients != null && clients.length > 0)
			{
				KeyNamePair[] whs = DB.loadWarehouses(ctx, clients[0]);
				//
				KeyNamePair[] orgs = DB.loadOrgs(ctx, clients[0]);
				//  load org
				if (orgs != null && orgs.length > 0)
				{
					DB.loadPreferences(ctx, orgs[0], null, null, null);
				}
			}
		}
		//
		Env.setContext(ctx, "#Date", "2000-01-01");
	//	logger.exiting("Env", "initTest");
		return ctx;
	}   //  testInit

	/**
	 *  Java Version Test
	 *  @return true if Java Version is OK
	 */
	public static boolean isJavaOK()
	{
		//	Java System version check
		String jVersion = System.getProperty("java.version");
		String targetVersion = "1.4.1";				//	specific release
		if (jVersion.startsWith(targetVersion)) 	//  this release
			return true;
		//  Warning
		boolean ok = false;
		if (jVersion.startsWith("1.4"))  			//  later/earlier release
			ok = true;

		//  Error Message
		StringBuffer msg = new StringBuffer();
		msg.append(System.getProperty("java.vm.name")).append(" - ").append(jVersion);
		if (ok)
			msg.append("(untested)");
		msg.append("  <>  ").append(targetVersion);
		//
		JOptionPane.showMessageDialog(null, msg.toString(),
			org.compiere.Compiere.getName() + " - Java Version Check",
			ok ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
		return ok;
	}   //  isJavaOK

	/**
	 *	Exit System
	 *  @param status System exit status (usually 0 for no error)
	 */
	public static void exitEnv (int status)
	{
		reset(true);
		s_log.info("exit");
		LogManager.shutdown();
		if (Ini.isClient())
			System.exit (status);
	}	//	close

	/**
	 * 	Reset Cache
	 * 	@param all everything otherwise login data remains
	 */
	public static void reset (boolean all)
	{
		s_log.info("reset - all=" + all);

		//	Dismantle windows
		/**
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container win = (Container)s_windows.get(i);
			if (win.getClass().getName().endsWith("AMenu")) // Null pointer
				;
			else if (win instanceof Window)
				((Window)win).dispose();
			else
				win.removeAll();
		}
		**/
		s_windows.clear();

		//	Context
		if (all)
			s_ctx.clear();
		else
		{
			Object[] keys = s_ctx.keySet().toArray();
			for (int i = 0; i < keys.length; i++)
			{
				String tag = keys[i].toString();
				if (Character.isDigit(tag.charAt(0)))
					s_ctx.remove(keys[i]);
			}
		}

		//	Cache
		CacheMgt.get().reset();
		DB.closeTarget();
	}	//	resetAll


	/*************************************************************************/

	/**
	 *  Application Context
	 */
	private static Properties   s_ctx = new Properties();
	/** WindowNo for Find           */
	public static final int     WINDOW_FIND = 1110;
	/** WinowNo for MLookup         */
	public static final int	    WINDOW_MLOOKUP = 1111;
	/** WindowNo for PrintCustomize */
	public static final int     WINDOW_CUSTOMIZE = 1112;

	/** Tab for Info                */
	public static final int     TAB_INFO = 77;

	/**
	 *  Get Context
	 *  @return Properties
	 */
	public static final Properties getCtx()
	{
		return s_ctx;
	}   //  getCtx

	/**
	 * Set Context
	 * @param ctx context
	 */
	public static void setCtx (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.setCtx - require Context");
		s_ctx.clear();
		s_ctx = ctx;
	}   //  setCtx

	/**
	 *	Set Global Context to Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		if (value == null || value.length() == 0)
			ctx.remove(context);
		else
			ctx.setProperty(context, value);
	}	//	setContext

	/**
	 *	Set Global Context to (int) Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		Log.trace(7, "Context " + context + "==" + value);
		//
		ctx.setProperty(context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Global Context to Y/N Value
	 *  @param ctx context
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, String context, boolean value)
	{
		setContext (ctx, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set Context for Window to int Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, int value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(8, "Context("+WindowNo+") " + context + "==" + value);
		//
		ctx.setProperty(WindowNo+"|"+context, String.valueOf(value));
	}	//	setContext

	/**
	 *	Set Context for Window to Y/N Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 */
	public static void setContext (Properties ctx, int WindowNo, String context, boolean value)
	{
		setContext (ctx, WindowNo, context, value ? "Y" : "N");
	}	//	setContext

	/**
	 *	Set Context for Window & Tab to Value
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @param value context value
	 *  @param TabNo tab no
	 */
	public static void setContext (Properties ctx, int WindowNo, int TabNo, String context, String value)
	{
		if (ctx == null || context == null)
			return;
		if (WindowNo != WINDOW_FIND && WindowNo != WINDOW_MLOOKUP)
			Log.trace(9, "Context("+WindowNo+","+TabNo+") " + context + "==" + value);
		//
		if (value == null || value.equals(""))
			ctx.remove(WindowNo+"|"+TabNo+"|"+context);
		else
			ctx.setProperty(WindowNo+"|"+TabNo+"|"+context, value);
	}	//	setContext

	/**
	 *	Set AutoCommit
	 *  @param ctx context
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty("AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Set AutoCommit for Window
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param autoCommit auto commit (save)
	 */
	public static void setAutoCommit (Properties ctx, int WindowNo, boolean autoCommit)
	{
		if (ctx == null)
			return;
		ctx.setProperty(WindowNo+"|AutoCommit", autoCommit ? "Y" : "N");
	}	//	setAutoCommit

	/**
	 *	Get global Value of Context
	 *  @param ctx context
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		return ctx.getProperty(context, "");
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available and enabled
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context, boolean onlyWindow)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+context);
		if (s == null)
		{
			if (onlyWindow)
				return "";
			if (context.startsWith("#") || context.startsWith("$"))
				return getContext(ctx, context);
			return getContext(ctx, "#" + context);
		}
		return s;
	}	//	getContext

	/**
	 *	Get Value of Context for Window.
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, String context)
	{
		return getContext(ctx, WindowNo, context, false);
	}	//	getContext

	/**
	 *	Get Value of Context for Window & Tab,
	 *	if not found global context if available
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param context context key
	 *  @return value or ""
	 */
	public static String getContext (Properties ctx, int WindowNo, int TabNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = ctx.getProperty(WindowNo+"|"+TabNo+"|"+context);
		if (s == null)
			return getContext(ctx, WindowNo, context, false);
		return s;
	}	//	getContext

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param context context key
	 *  @return value
	 */
	public static int getContextAsInt(Properties ctx, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, context);
		if (s.length() == 0)
			s = getContext(ctx, 0, context, false);		//	search 0 and defaults
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Get Context and convert it to an integer (0 if error)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return value or 0
	 */
	public static int getContextAsInt(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		if (s.length() == 0)
			return 0;
		//
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Log.error("Env.getContextAsInt (" + context + ") = " + s, e);
		}
		return 0;
	}	//	getContextAsInt

	/**
	 *	Is AutoCommit
	 *  @param ctx context
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, "AutoCommit");
		if (s != null && s.equals("Y"))
			return true;
		return false;
	}	//	isAutoCommit

	/**
	 *	Is Window AutoCommit (if not set use default)
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @return true if auto commit
	 */
	public static boolean isAutoCommit (Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, "AutoCommit", false);
		if (s != null)
		{
			if (s.equals("Y"))
				return true;
			else
				return false;
		}
		return isAutoCommit(ctx);
	}	//	isAutoCommit

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, String context)
	{
		return getContextAsDate(ctx, 0, context);
	}	//	getContextAsDate

	/**
	 *	Get Context and convert it to a Timestamp
	 *	if error return today's date
	 *  @param ctx context
	 *  @param WindowNo window no
	 *  @param context context key
	 *  @return Timestamp
	 */
	public static Timestamp getContextAsDate(Properties ctx, int WindowNo, String context)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getContext - require Context");
		String s = getContext(ctx, WindowNo, context, false);
		//	JDBC Format YYYY-MM-DD	example 2000-09-11 00:00:00.0
		if (s == null || s.equals(""))
		{
			Log.error("Env.getContextAsDate - No value for: " + context);
			return new Timestamp(System.currentTimeMillis());
		}

		//  timestamp requires time
		if (s.trim().length() == 10)
			s = s.trim() + " 00:00:00.0";

		return Timestamp.valueOf(s);
	}	//	getContextAsDate

	/*************************************************************************/

	/**
	 *	Get Preference.
	 *  <pre>
	 *		0)	Current Setting
	 *		1) 	Window Preference
	 *		2) 	Global Preference
	 *		3)	Login settings
	 *		4)	Accounting settings
	 *  </pre>
	 *  @param  ctx context
	 *	@param	AD_Window_ID window no
	 *	@param	context		Entity to search
	 *	@param	system		System level preferences (vs. user defined)
	 *  @return preference value
	 */
	public static String getPreference (Properties ctx, int AD_Window_ID, String context, boolean system)
	{
		if (ctx == null || context == null)
			throw new IllegalArgumentException ("Env.getPreference - require Context");
		String retValue = null;
		//
		if (!system)	//	User Preferences
		{
			retValue = ctx.getProperty("P"+AD_Window_ID+"|"+context);//	Window Pref
			if (retValue == null)
				retValue = ctx.getProperty("P|"+context);  			//	Global Pref
		}
		else			//	System Preferences
		{
			retValue = ctx.getProperty("#"+context);   				//	Login setting
			if (retValue == null)
				retValue = ctx.getProperty("$"+context);   			//	Accounting setting
		}
		//
		return (retValue == null ? "" : retValue);
	}	//	getPreference

	/****************************************************************************
	 *  Language issues
	 */

	/** Context Language identifier */
	static public final String      LANG = "#AD_Language";

	/**
	 *  Check Base Language
	 *  @param ctx context
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Properties ctx, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (getAD_Language(ctx));
		else	//	No AD Table
			if (!isMultiLingualDocument(ctx))
				return true;		//	access base table
		return Language.isBaseLanguage (getAD_Language(ctx));
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param AD_Language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (String AD_Language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			Language.isBaseLanguage (AD_Language);
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return Language.isBaseLanguage (AD_Language);
	}	//	isBaseLanguage

	/**
	 *	Check Base Language
	 * 	@param language language
	 * 	@param TableName table to be translated
	 * 	@return true if base language and table not translated
	 */
	public static boolean isBaseLanguage (Language language, String TableName)
	{
		if (TableName.startsWith("AD") || TableName.equals("C_UOM"))
			language.isBaseLanguage();
		else	//	No AD Table
			if (!isMultiLingualDocument(s_ctx))				//	Base Context
				return true;		//	access base table
		return language.isBaseLanguage();
	}	//	isBaseLanguage

	/**
	 * 	Do we have Multi-Lingual Documents.
	 *  Set in DB.loadOrgs
	 * 	@param ctx context
	 * 	@return true if multi lingual documents
	 */
	public static boolean isMultiLingualDocument (Properties ctx)
	{
		return "Y".equals(Env.getContext(ctx, "#IsMultiLingualDocument"));
	}	//	isMultiLingualDocument

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return AD_Language eg. en_US
	 */
	public static String getAD_Language (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return lang;
		}
		return Language.getBaseAD_Language();
	}	//	getAD_Language

	/**
	 *  Get AD_Language
	 *  @param ctx context
	 *	@return Language
	 */
	public static Language getLanguage (Properties ctx)
	{
		if (ctx != null)
		{
			String lang = getContext(ctx, LANG);
			if (lang != null || lang.length() > 0)
				return Language.getLanguage(lang);
		}
		return Language.getLanguage();
	}	//	getLanguage

	/**
	 *  Verify Language.
	 *  Check that language is supported by the system
	 *  @param ctx might be updated with new AD_Language
	 *  @param language language
	 */
	public static void verifyLanguage (Properties ctx, Language language)
	{
		ArrayList sysLang = new ArrayList();
		String sql = "SELECT AD_Language FROM AD_Language ORDER BY IsBaseLanguage DESC";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				sysLang.add(rs.getString(1));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("ALogin.verifyLanguage", e);
		}

		String selectedLanguage = language.getAD_Language();
		for (int i = 0; i < sysLang.size(); i++)
		{
			if (selectedLanguage.equals(sysLang.get(i)))
				return;
		}
		//  Language/Country not found - try finding similar language (i.e. first e chars)
		selectedLanguage = selectedLanguage.substring(0,2);
		for (int i = 0; i < sysLang.size(); i++)
		{
			String comp = sysLang.get(i).toString().substring(0,2);
			if (selectedLanguage.equals(comp))
			{
				language.setAD_Language(sysLang.get(i).toString());
				Env.setContext(ctx, Env.LANG, language.getAD_Language());
				return;
			}
		}
		//  none found - use Database Base Language
		language.setAD_Language(sysLang.get(0).toString());
		Env.setContext(ctx, Env.LANG, language.getAD_Language());
	}   //  verifyLanguage

	/*************************************************************************/

	/**
	 *	Get Context as String array with format: key == value
	 *  @param ctx context
	 *  @return context string
	 */
	public static String[] getEntireContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.getEntireContext - require Context");
		Iterator keyIterator = ctx.keySet().iterator();
		String[] sList = new String[ctx.size()];
		int i = 0;
		while (keyIterator.hasNext())
		{
			Object key = keyIterator.next();
			sList[i++] = key.toString() + " == " + ctx.get(key).toString();
		}

		return sList;
	}	//	getEntireContext

	/**
	 *	Get Header info (connection, org, user)
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @return Header String
	 */
	public static String getHeader(Properties ctx, int WindowNo)
	{
		StringBuffer sb = new StringBuffer();
		if (WindowNo > 0)
			sb.append(getContext(ctx, WindowNo, "WindowName", false)).append("  ");
		sb.append(getContext(ctx, "#AD_User_Name")).append("@")
			.append(getContext(ctx, "#AD_Client_Name")).append(".")
			.append(getContext(ctx, "#AD_Org_Name"))
			.append(" [").append(CConnection.get().toString()).append("]");
		return sb.toString();
	}	//	getHeader

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public static void clearWinContext(Properties ctx, int WindowNo)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearWinContext - require Context");
		//
		Object[] keys = ctx.keySet().toArray();
		for (int i = 0; i < keys.length; i++)
		{
			String tag = keys[i].toString();
			if (tag.startsWith(WindowNo+"|"))
				ctx.remove(keys[i]);
		}
		//  Clear Lookup Cache
		MLookupCache.cacheReset(WindowNo);
	//	MLocator.cacheReset(WindowNo);
		//
		removeWindow(WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 *  @param ctx context
	 */
	public static void clearContext(Properties ctx)
	{
		if (ctx == null)
			throw new IllegalArgumentException ("Env.clearContext - require Context");
		ctx.clear();
	}	//	clearContext


	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 * 	@param	ignoreUnparsable if true, unsuccessful @tag@ are ignored otherwise "" is returned
	 *  @return parsed String or "" if not successful and ignoreUnparsable
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow, boolean ignoreUnparsable)
	{
		if (value == null)
			return "";

		String token;
		String inStr = new String(value);
		StringBuffer outStr = new StringBuffer();

		int i = inStr.indexOf("@");
		while (i != -1)
		{
			outStr.append(inStr.substring(0, i));			// up to @
			inStr = inStr.substring(i+1, inStr.length());	// from first @

			int j = inStr.indexOf("@");						// next @
			if (j < 0)
			{
				Log.error("Env.parseContext - no second tag: " + inStr);
				return "";						//	no second tag
			}

			token = inStr.substring(0, j);

			String ctxInfo = getContext(ctx, WindowNo, token, onlyWindow);	// get context
			if (ctxInfo.length() == 0 && (token.startsWith("#") || token.startsWith("$")) )
				ctxInfo = getContext(ctx, token);	// get global context
			if (ctxInfo.length() == 0)
			{
				Log.trace(Log.l5_DData, "Env.parseContext - no context (" + WindowNo + ") for: " + token);
				if (!ignoreUnparsable)
					return "";						//	token not found
			}
			else
				outStr.append(ctxInfo);				// replace context with Context

			inStr = inStr.substring(j+1, inStr.length());	// from second @
			i = inStr.indexOf("@");
		}
		outStr.append(inStr);						// add the rest of the string

		return outStr.toString();
	}	//	parseContext

	/**
	 *	Parse Context replaces global or Window context @tag@ with actual value.
	 *
	 *  @param ctx context
	 *	@param	WindowNo	Number of Window
	 *	@param	value		Message to be parsed
	 *  @param  onlyWindow  if true, no defaults are used
	 *  @return parsed String or "" if not successful
	 */
	public static String parseContext (Properties ctx, int WindowNo, String value,
		boolean onlyWindow)
	{
		return parseContext(ctx, WindowNo, value, onlyWindow, false);
	}	//	parseContext

	/*************************************************************************/

	private static ArrayList	s_windows = new ArrayList(20);

	/**
	 *	Add Container and return WindowNo.
	 *  The container is a APanel, AWindow or JFrame/JDialog
	 *  @param win window
	 *  @return WindowNo used for context
	 */
	public static int createWindowNo(Container win)
	{
		int retValue = s_windows.size();
		s_windows.add(win);
		return retValue;
	}	//	createWindowNo

	/**
	 *	Search Window by comparing the Frames
	 *  @param container container
	 *  @return WindowNo of container or 0
	 */
	public static int getWindowNo (Container container)
	{
		if (container == null)
			return 0;
		JFrame winFrame = getFrame(container);
		if (winFrame == null)
			return 0;

		//  loop through windows
		for (int i = 0; i < s_windows.size(); i++)
		{
			Container cmp = (Container)s_windows.get(i);
			if (cmp != null)
			{
				JFrame cmpFrame = getFrame(cmp);
				if (winFrame.equals(cmpFrame))
					return i;
			}
		}
		return 0;
	}	//	getWindowNo

	/**
	 *	Return the JFrame pointer of WindowNo - or null
	 *  @param WindowNo window
	 *  @return JFrame of WindowNo
	 */
	public static JFrame getWindow (int WindowNo)
	{
		JFrame retValue = null;
		try
		{
			retValue = getFrame ((Container)s_windows.get(WindowNo));
		}
		catch (Exception e)
		{
			System.err.println("Env.getWindow - " + e);
		}
		return retValue;
	}	//	getWindow

	/**
	 *	Remove window from active list
	 *  @param WindowNo window
	 */
	private static void removeWindow (int WindowNo)
	{
		if (WindowNo <= s_windows.size())
			s_windows.set(WindowNo, null);
	}	//	removeWindow

	/**
	 *	Clean up context for Window (i.e. delete it)
	 *  @param WindowNo window
	 */
	public static void clearWinContext(int WindowNo)
	{
		clearWinContext (s_ctx, WindowNo);
	}	//	clearWinContext

	/**
	 *	Clean up all context (i.e. delete it)
	 */
	public static void clearContext()
	{
		s_ctx.clear();
	}	//	clearContext


	/*************************************************************************/

	/**
	 *	Get Frame of Window
	 *  @param container Container
	 *  @return JFrame of container or null
	 */
	public static JFrame getFrame (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JFrame)
				return (JFrame)element;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *	Get Graphics of container or its parent.
	 *  The element may not have a Graphic if not displayed yet,
	 * 	but the parent might have.
	 *  @param container Container
	 *  @return Graphics of container or null
	 */
	public static Graphics getGraphics (Container container)
	{
		Container element = container;
		while (element != null)
		{
			Graphics g = element.getGraphics();
			if (g != null)
				return g;
			element = element.getParent();
		}
		return null;
	}	//	getFrame

	/**
	 *  Return JDialog or JFrame Parent
	 *  @param container Container
	 *  @return JDialog or JFrame of container
	 */
	public static Window getParent (Container container)
	{
		Container element = container;
		while (element != null)
		{
			if (element instanceof JDialog || element instanceof JFrame)
				return (Window)element;
			element = element.getParent();
		}
		return null;
	}   //  getParent

	/*************************************************************************/

	/**
	 *  Get Image with File name
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static Image getImage (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		Toolkit tk = Toolkit.getDefaultToolkit();
		return tk.getImage(url);
	}   //  getImage

	/**
	 *  Get ImageIcon
	 *
	 *  @param fileNameInImageDir full file name in imgaes folder (e.g. Bean16.gif)
	 *  @return image
	 */
	public static ImageIcon getImageIcon (String fileNameInImageDir)
	{
		URL url = Compiere.class.getResource("images/" + fileNameInImageDir);
		if (url == null)
			return null;
		return new ImageIcon(url);
	}   //  getImageIcon


	/**************************************************************************/

	/**
	 *  Start Browser
	 *  @param url url
	 */
	public static void startBrowser (String url)
	{
		Log.trace(Log.l1_User, "Env.startBrowser", url);
		//  OS command
		String cmd = "explorer ";
		if (!System.getProperty("os.name").startsWith("Win"))
			cmd = "netscape ";
		//
		String execute = cmd + url;
		try
		{
			Runtime.getRuntime().exec(execute);
		}
		catch (Exception e)
		{
			System.err.println("Env.startBrowser - " + execute + " - " + e);
		}
	}   //  startBrowser

	/**************************************************************************
	 *  Static Variables
	 */

	/**
	 *  Big Decimal Zero
	 */
	static final public java.math.BigDecimal ZERO = new java.math.BigDecimal(0.0);

	/**
	 * 	New Line
	 */
	public static final String	NL = System.getProperty("line.separator");


	/**
	 *  Static initializer
	 */
	static
	{
		//  Set English as default Language
		s_ctx.put(LANG, Language.getBaseAD_Language());
	}   //  static

}   //  Env
