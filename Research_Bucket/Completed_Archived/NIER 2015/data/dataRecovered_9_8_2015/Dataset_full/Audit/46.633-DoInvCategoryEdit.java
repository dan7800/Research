package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryEdit implements Action
{
	private String strClassName = "DoInvCategoryEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if categoryCode is supplied
		String formName = (String) req.getParameter("formName");
		// String categoryPkid = (String)req.getParameter("categoryPkid");
		if (formName != null)
		{
			if (formName.compareTo("editCategoryDetails") == 0)
			{
				// if (categoryCode != null)
				// {
				// find the category to be edited
				fnGetEditCategory(servlet, req, res);
				// }
			} else if (formName.compareTo("updateCategory") == 0)
			{
				fnUpdateCategory(servlet, req, res);
				fnGetEditCategory(servlet, req, res);
			}
		}
		// list all categoryCodes
		// fnGetCategoryCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-category-page");
	}

	protected void fnGetEditCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetEditCategory";
		String categoryCode = (String) req.getParameter("categoryCode");
		if (categoryCode == null)
		{
			Log.printDebug("Null categoryCode");
			return;
		}
		Log.printVerbose("categoryCode = " + categoryCode);
		Category lCategory = CategoryNut.getObjectByCode(categoryCode);
		if (lCategory != null)
		{
			req.setAttribute("editCategory", lCategory);
		}
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		String categoryPkid = (String) req.getParameter("categoryPkid");
		if (categoryPkid == null)
			return;
		// Validate categoryPkid
		Integer catPkid;
		try
		{
			catPkid = Integer.valueOf(categoryPkid);
		} catch (Exception ex)
		{
			Log.printDebug("Error converting category pkid from string to integer: " + ex.getMessage());
			return;
		}
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
		// if(categoryCode != null)
		// {
		Category lCategory = CategoryNut.getHandle(catPkid);
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
		// } // end if
	} // end fnUpdateCategory

	protected void fnGetCategoryCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector categoryCodes = new Vector();
		Collection colCategories = CategoryNut.getAllObjects();
		try
		{
			for (Iterator itr = colCategories.iterator(); itr.hasNext();)
			{
				Category i = (Category) itr.next();
				Log.printVerbose("Adding: " + i.getCategoryCode());
				categoryCodes.add(i.getCategoryCode());
			}
			req.setAttribute("itrCategoryCode", categoryCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of categoryCodes: " + ex.getMessage());
		}
	}
}
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryEdit implements Action
{
	private String strClassName = "DoInvCategoryEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if categoryCode is supplied
		String formName = (String) req.getParameter("formName");
		// String categoryPkid = (String)req.getParameter("categoryPkid");
		if (formName != null)
		{
			if (formName.compareTo("editCategoryDetails") == 0)
			{
				// if (categoryCode != null)
				// {
				// find the category to be edited
				fnGetEditCategory(servlet, req, res);
				// }
			} else if (formName.compareTo("updateCategory") == 0)
			{
				fnUpdateCategory(servlet, req, res);
				fnGetEditCategory(servlet, req, res);
			}
		}
		// list all categoryCodes
		// fnGetCategoryCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-category-page");
	}

	protected void fnGetEditCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetEditCategory";
		String categoryCode = (String) req.getParameter("categoryCode");
		if (categoryCode == null)
		{
			Log.printDebug("Null categoryCode");
			return;
		}
		Log.printVerbose("categoryCode = " + categoryCode);
		Category lCategory = CategoryNut.getObjectByCode(categoryCode);
		if (lCategory != null)
		{
			req.setAttribute("editCategory", lCategory);
		}
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		String categoryPkid = (String) req.getParameter("categoryPkid");
		if (categoryPkid == null)
			return;
		// Validate categoryPkid
		Integer catPkid;
		try
		{
			catPkid = Integer.valueOf(categoryPkid);
		} catch (Exception ex)
		{
			Log.printDebug("Error converting category pkid from string to integer: " + ex.getMessage());
			return;
		}
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
		// if(categoryCode != null)
		// {
		Category lCategory = CategoryNut.getHandle(catPkid);
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
		// } // end if
	} // end fnUpdateCategory

	protected void fnGetCategoryCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector categoryCodes = new Vector();
		Collection colCategories = CategoryNut.getAllObjects();
		try
		{
			for (Iterator itr = colCategories.iterator(); itr.hasNext();)
			{
				Category i = (Category) itr.next();
				Log.printVerbose("Adding: " + i.getCategoryCode());
				categoryCodes.add(i.getCategoryCode());
			}
			req.setAttribute("itrCategoryCode", categoryCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of categoryCodes: " + ex.getMessage());
		}
	}
}
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryEdit implements Action
{
	private String strClassName = "DoInvCategoryEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if categoryCode is supplied
		String formName = (String) req.getParameter("formName");
		// String categoryPkid = (String)req.getParameter("categoryPkid");
		if (formName != null)
		{
			if (formName.compareTo("editCategoryDetails") == 0)
			{
				// if (categoryCode != null)
				// {
				// find the category to be edited
				fnGetEditCategory(servlet, req, res);
				// }
			} else if (formName.compareTo("updateCategory") == 0)
			{
				fnUpdateCategory(servlet, req, res);
				fnGetEditCategory(servlet, req, res);
			}
		}
		// list all categoryCodes
		// fnGetCategoryCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-category-page");
	}

	protected void fnGetEditCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetEditCategory";
		String categoryCode = (String) req.getParameter("categoryCode");
		if (categoryCode == null)
		{
			Log.printDebug("Null categoryCode");
			return;
		}
		Log.printVerbose("categoryCode = " + categoryCode);
		Category lCategory = CategoryNut.getObjectByCode(categoryCode);
		if (lCategory != null)
		{
			req.setAttribute("editCategory", lCategory);
		}
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		String categoryPkid = (String) req.getParameter("categoryPkid");
		if (categoryPkid == null)
			return;
		// Validate categoryPkid
		Integer catPkid;
		try
		{
			catPkid = Integer.valueOf(categoryPkid);
		} catch (Exception ex)
		{
			Log.printDebug("Error converting category pkid from string to integer: " + ex.getMessage());
			return;
		}
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
		// if(categoryCode != null)
		// {
		Category lCategory = CategoryNut.getHandle(catPkid);
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
		// } // end if
	} // end fnUpdateCategory

	protected void fnGetCategoryCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector categoryCodes = new Vector();
		Collection colCategories = CategoryNut.getAllObjects();
		try
		{
			for (Iterator itr = colCategories.iterator(); itr.hasNext();)
			{
				Category i = (Category) itr.next();
				Log.printVerbose("Adding: " + i.getCategoryCode());
				categoryCodes.add(i.getCategoryCode());
			}
			req.setAttribute("itrCategoryCode", categoryCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of categoryCodes: " + ex.getMessage());
		}
	}
}
package com.vlee.servlet.inventory;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;

public class DoInvCategoryEdit implements Action
{
	private String strClassName = "DoInvCategoryEdit";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Check if categoryCode is supplied
		String formName = (String) req.getParameter("formName");
		// String categoryPkid = (String)req.getParameter("categoryPkid");
		if (formName != null)
		{
			if (formName.compareTo("editCategoryDetails") == 0)
			{
				// if (categoryCode != null)
				// {
				// find the category to be edited
				fnGetEditCategory(servlet, req, res);
				// }
			} else if (formName.compareTo("updateCategory") == 0)
			{
				fnUpdateCategory(servlet, req, res);
				fnGetEditCategory(servlet, req, res);
			}
		}
		// list all categoryCodes
		// fnGetCategoryCodeList(servlet, req, res);
		return new ActionRouter("inv-setup-edit-category-page");
	}

	protected void fnGetEditCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetEditCategory";
		String categoryCode = (String) req.getParameter("categoryCode");
		if (categoryCode == null)
		{
			Log.printDebug("Null categoryCode");
			return;
		}
		Log.printVerbose("categoryCode = " + categoryCode);
		Category lCategory = CategoryNut.getObjectByCode(categoryCode);
		if (lCategory != null)
		{
			req.setAttribute("editCategory", lCategory);
		}
	}

	protected void fnUpdateCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateCategory()";
		String categoryPkid = (String) req.getParameter("categoryPkid");
		if (categoryPkid == null)
			return;
		// Validate categoryPkid
		Integer catPkid;
		try
		{
			catPkid = Integer.valueOf(categoryPkid);
		} catch (Exception ex)
		{
			Log.printDebug("Error converting category pkid from string to integer: " + ex.getMessage());
			return;
		}
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
		// if(categoryCode != null)
		// {
		Category lCategory = CategoryNut.getHandle(catPkid);
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
		// } // end if
	} // end fnUpdateCategory

	protected void fnGetCategoryCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Vector categoryCodes = new Vector();
		Collection colCategories = CategoryNut.getAllObjects();
		try
		{
			for (Iterator itr = colCategories.iterator(); itr.hasNext();)
			{
				Category i = (Category) itr.next();
				Log.printVerbose("Adding: " + i.getCategoryCode());
				categoryCodes.add(i.getCategoryCode());
			}
			req.setAttribute("itrCategoryCode", categoryCodes.iterator());
		} catch (Exception ex)
		{
			Log.printDebug("Error in getting list of categoryCodes: " + ex.getMessage());
		}
	}
}
