package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryUpdate implements Action
{
	String strClassName = "DoInvCategoryUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update category
		fnUpdateCategory(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-category-page");
		return new ActionRouter("inv-redirect-setup-edit-category-page");
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		// Get the request paramaters
		String categoryCode = req.getParameter("categoryCode");
		String categoryName = req.getParameter("categoryName");
		String categoryDesc = req.getParameter("categoryDesc");
		if (categoryCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryCode = " + categoryCode);
		if (categoryName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryName = " + categoryName);
		if (categoryDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryDesc = " + categoryDesc);
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (categoryCode != null)
		{
			Category lCategory = CategoryNut.getObjectByCode(categoryCode);
			if (lCategory != null)
			{
				try
				{
					lCategory.setCategoryCode(categoryCode);
					lCategory.setName(categoryName);
					lCategory.setDescription(categoryDesc);
					// and then update the lastModified and userIdUpdate fields
					lCategory.setLastUpdate(tsCreate);
					lCategory.setUserIdUpdate(usrid);
				} catch (Exception ex)
				{
					Log.printDebug("Update Category: " + categoryCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateCategory
} // end class DoInvCategoryUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryUpdate implements Action
{
	String strClassName = "DoInvCategoryUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update category
		fnUpdateCategory(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-category-page");
		return new ActionRouter("inv-redirect-setup-edit-category-page");
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		// Get the request paramaters
		String categoryCode = req.getParameter("categoryCode");
		String categoryName = req.getParameter("categoryName");
		String categoryDesc = req.getParameter("categoryDesc");
		if (categoryCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryCode = " + categoryCode);
		if (categoryName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryName = " + categoryName);
		if (categoryDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryDesc = " + categoryDesc);
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (categoryCode != null)
		{
			Category lCategory = CategoryNut.getObjectByCode(categoryCode);
			if (lCategory != null)
			{
				try
				{
					lCategory.setCategoryCode(categoryCode);
					lCategory.setName(categoryName);
					lCategory.setDescription(categoryDesc);
					// and then update the lastModified and userIdUpdate fields
					lCategory.setLastUpdate(tsCreate);
					lCategory.setUserIdUpdate(usrid);
				} catch (Exception ex)
				{
					Log.printDebug("Update Category: " + categoryCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateCategory
} // end class DoInvCategoryUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryUpdate implements Action
{
	String strClassName = "DoInvCategoryUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update category
		fnUpdateCategory(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-category-page");
		return new ActionRouter("inv-redirect-setup-edit-category-page");
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		// Get the request paramaters
		String categoryCode = req.getParameter("categoryCode");
		String categoryName = req.getParameter("categoryName");
		String categoryDesc = req.getParameter("categoryDesc");
		if (categoryCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryCode = " + categoryCode);
		if (categoryName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryName = " + categoryName);
		if (categoryDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryDesc = " + categoryDesc);
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (categoryCode != null)
		{
			Category lCategory = CategoryNut.getObjectByCode(categoryCode);
			if (lCategory != null)
			{
				try
				{
					lCategory.setCategoryCode(categoryCode);
					lCategory.setName(categoryName);
					lCategory.setDescription(categoryDesc);
					// and then update the lastModified and userIdUpdate fields
					lCategory.setLastUpdate(tsCreate);
					lCategory.setUserIdUpdate(usrid);
				} catch (Exception ex)
				{
					Log.printDebug("Update Category: " + categoryCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateCategory
} // end class DoInvCategoryUpdate
package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.vlee.servlet.main.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryUpdate implements Action
{
	String strClassName = "DoInvCategoryUpdate";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// update category
		fnUpdateCategory(servlet, req, res);
		// return new ActionRouter("inv-setup-edit-category-page");
		return new ActionRouter("inv-redirect-setup-edit-category-page");
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		// Get the request paramaters
		String categoryCode = req.getParameter("categoryCode");
		String categoryName = req.getParameter("categoryName");
		String categoryDesc = req.getParameter("categoryDesc");
		if (categoryCode == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryCode = " + categoryCode);
		if (categoryName == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryName = " + categoryName);
		if (categoryDesc == null)
			return;
		else
			Log.printVerbose(strClassName + ":" + funcName + " - categoryDesc = " + categoryDesc);
		// Update the last modified and user_edit fields
		HttpSession session = req.getSession();
		User lUsr = UserNut.getHandle((String) session.getAttribute("userName"));
		Integer usrid = null;
		if (lUsr == null)
		{
			Log.printDebug(strClassName + ": " + "WARNING - NULL userName");
			return;
		}
		try
		{
			usrid = lUsr.getUserId();
		} catch (Exception ex)
		{
			Log.printAudit("User does not exist: " + ex.getMessage());
		}
		// get the current timestamp
		java.util.Date ldt = new java.util.Date();
		Timestamp tsCreate = new Timestamp(ldt.getTime());
		if (categoryCode != null)
		{
			Category lCategory = CategoryNut.getObjectByCode(categoryCode);
			if (lCategory != null)
			{
				try
				{
					lCategory.setCategoryCode(categoryCode);
					lCategory.setName(categoryName);
					lCategory.setDescription(categoryDesc);
					// and then update the lastModified and userIdUpdate fields
					lCategory.setLastUpdate(tsCreate);
					lCategory.setUserIdUpdate(usrid);
				} catch (Exception ex)
				{
					Log.printDebug("Update Category: " + categoryCode + " -  Failed" + ex.getMessage());
				} // end try-catch
			} // end if
		} // end if
	} // end fnUpdateCategory
} // end class DoInvCategoryUpdate
