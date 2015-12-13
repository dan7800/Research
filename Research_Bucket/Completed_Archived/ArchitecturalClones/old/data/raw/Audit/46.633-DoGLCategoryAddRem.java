package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCategoryAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetGLCategoryList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-glcategory-page");
		}
		if (formName.equals("addGLCategory"))
		{
			fnAddGLCategory(servlet, req, res);
		}
		if (formName.equals("rmGLCategory"))
		{
			fnRmGLCategory(servlet, req, res);
		}
		fnGetGLCategoryList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-glcategory-page");
	}

	protected void fnRmGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmGLCategoryCode = (String) req.getParameter("removeGLCategory");
		if (rmGLCategoryCode != null)
		{
			GLCategory lGLCat = GLCategoryNut.getObjectByCode(rmGLCategoryCode);
			if (lGLCat != null)
			{
				try
				{
					lGLCat.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCategory Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetGLCategoryList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colGLCat = GLCategoryNut.getAllObjects();
		Iterator itrAllGLCat = colGLCat.iterator();
		req.setAttribute("itrAllGLCat", itrAllGLCat);
	}

	protected void fnAddGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String glCategoryCode = (String) req.getParameter("glCategoryCode");
		String glCategoryName = (String) req.getParameter("glCategoryName");
		String glCategoryDesc = (String) req.getParameter("glCategoryDescription");
		String glCategoryLedgerSide = (String) req.getParameter("glCategoryLedgerSide");
		String glCategoryType = (String) req.getParameter("glCategoryType");
		String glCategoryPostToSection = (String) req.getParameter("glCategoryPostToSection");
		String glCategoryOptions = new String("");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (glCategoryCode == null)
			return;
		if (glCategoryName == null)
			return;
		if (glCategoryDesc == null)
			return;
		GLCategory lGLCatGrp = null;
		if (glCategoryCode != null)
			lGLCatGrp = GLCategoryNut.getObjectByCode(glCategoryCode);
		if (lGLCatGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new GLCategory");
			GLCategoryHome lGLCatH = GLCategoryNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				GLCategory newGLCat = (GLCategory) lGLCatH.create(glCategoryCode, glCategoryName, glCategoryDesc,
						glCategoryLedgerSide, glCategoryType, glCategoryPostToSection, glCategoryOptions);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCategory " + ex.getMessage());
			}
		}
		fnGetGLCategoryList(servlet, req, res);
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCategoryAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetGLCategoryList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-glcategory-page");
		}
		if (formName.equals("addGLCategory"))
		{
			fnAddGLCategory(servlet, req, res);
		}
		if (formName.equals("rmGLCategory"))
		{
			fnRmGLCategory(servlet, req, res);
		}
		fnGetGLCategoryList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-glcategory-page");
	}

	protected void fnRmGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmGLCategoryCode = (String) req.getParameter("removeGLCategory");
		if (rmGLCategoryCode != null)
		{
			GLCategory lGLCat = GLCategoryNut.getObjectByCode(rmGLCategoryCode);
			if (lGLCat != null)
			{
				try
				{
					lGLCat.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCategory Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetGLCategoryList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colGLCat = GLCategoryNut.getAllObjects();
		Iterator itrAllGLCat = colGLCat.iterator();
		req.setAttribute("itrAllGLCat", itrAllGLCat);
	}

	protected void fnAddGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String glCategoryCode = (String) req.getParameter("glCategoryCode");
		String glCategoryName = (String) req.getParameter("glCategoryName");
		String glCategoryDesc = (String) req.getParameter("glCategoryDescription");
		String glCategoryLedgerSide = (String) req.getParameter("glCategoryLedgerSide");
		String glCategoryType = (String) req.getParameter("glCategoryType");
		String glCategoryPostToSection = (String) req.getParameter("glCategoryPostToSection");
		String glCategoryOptions = new String("");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (glCategoryCode == null)
			return;
		if (glCategoryName == null)
			return;
		if (glCategoryDesc == null)
			return;
		GLCategory lGLCatGrp = null;
		if (glCategoryCode != null)
			lGLCatGrp = GLCategoryNut.getObjectByCode(glCategoryCode);
		if (lGLCatGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new GLCategory");
			GLCategoryHome lGLCatH = GLCategoryNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				GLCategory newGLCat = (GLCategory) lGLCatH.create(glCategoryCode, glCategoryName, glCategoryDesc,
						glCategoryLedgerSide, glCategoryType, glCategoryPostToSection, glCategoryOptions);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCategory " + ex.getMessage());
			}
		}
		fnGetGLCategoryList(servlet, req, res);
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCategoryAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetGLCategoryList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-glcategory-page");
		}
		if (formName.equals("addGLCategory"))
		{
			fnAddGLCategory(servlet, req, res);
		}
		if (formName.equals("rmGLCategory"))
		{
			fnRmGLCategory(servlet, req, res);
		}
		fnGetGLCategoryList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-glcategory-page");
	}

	protected void fnRmGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmGLCategoryCode = (String) req.getParameter("removeGLCategory");
		if (rmGLCategoryCode != null)
		{
			GLCategory lGLCat = GLCategoryNut.getObjectByCode(rmGLCategoryCode);
			if (lGLCat != null)
			{
				try
				{
					lGLCat.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCategory Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetGLCategoryList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colGLCat = GLCategoryNut.getAllObjects();
		Iterator itrAllGLCat = colGLCat.iterator();
		req.setAttribute("itrAllGLCat", itrAllGLCat);
	}

	protected void fnAddGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String glCategoryCode = (String) req.getParameter("glCategoryCode");
		String glCategoryName = (String) req.getParameter("glCategoryName");
		String glCategoryDesc = (String) req.getParameter("glCategoryDescription");
		String glCategoryLedgerSide = (String) req.getParameter("glCategoryLedgerSide");
		String glCategoryType = (String) req.getParameter("glCategoryType");
		String glCategoryPostToSection = (String) req.getParameter("glCategoryPostToSection");
		String glCategoryOptions = new String("");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (glCategoryCode == null)
			return;
		if (glCategoryName == null)
			return;
		if (glCategoryDesc == null)
			return;
		GLCategory lGLCatGrp = null;
		if (glCategoryCode != null)
			lGLCatGrp = GLCategoryNut.getObjectByCode(glCategoryCode);
		if (lGLCatGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new GLCategory");
			GLCategoryHome lGLCatH = GLCategoryNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				GLCategory newGLCat = (GLCategory) lGLCatH.create(glCategoryCode, glCategoryName, glCategoryDesc,
						glCategoryLedgerSide, glCategoryType, glCategoryPostToSection, glCategoryOptions);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCategory " + ex.getMessage());
			}
		}
		fnGetGLCategoryList(servlet, req, res);
	}
}
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCategoryAddRem implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		// Form Hnalders
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			fnGetGLCategoryList(servlet, req, res);
			return new ActionRouter("acc-setup-addrem-glcategory-page");
		}
		if (formName.equals("addGLCategory"))
		{
			fnAddGLCategory(servlet, req, res);
		}
		if (formName.equals("rmGLCategory"))
		{
			fnRmGLCategory(servlet, req, res);
		}
		fnGetGLCategoryList(servlet, req, res);
		return new ActionRouter("acc-setup-addrem-glcategory-page");
	}

	protected void fnRmGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String rmGLCategoryCode = (String) req.getParameter("removeGLCategory");
		if (rmGLCategoryCode != null)
		{
			GLCategory lGLCat = GLCategoryNut.getObjectByCode(rmGLCategoryCode);
			if (lGLCat != null)
			{
				try
				{
					lGLCat.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCategory Failed" + ex.getMessage());
				}
			}
		}
	}

	protected void fnGetGLCategoryList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		Collection colGLCat = GLCategoryNut.getAllObjects();
		Iterator itrAllGLCat = colGLCat.iterator();
		req.setAttribute("itrAllGLCat", itrAllGLCat);
	}

	protected void fnAddGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String glCategoryCode = (String) req.getParameter("glCategoryCode");
		String glCategoryName = (String) req.getParameter("glCategoryName");
		String glCategoryDesc = (String) req.getParameter("glCategoryDescription");
		String glCategoryLedgerSide = (String) req.getParameter("glCategoryLedgerSide");
		String glCategoryType = (String) req.getParameter("glCategoryType");
		String glCategoryPostToSection = (String) req.getParameter("glCategoryPostToSection");
		String glCategoryOptions = new String("");
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		if (glCategoryCode == null)
			return;
		if (glCategoryName == null)
			return;
		if (glCategoryDesc == null)
			return;
		GLCategory lGLCatGrp = null;
		if (glCategoryCode != null)
			lGLCatGrp = GLCategoryNut.getObjectByCode(glCategoryCode);
		if (lGLCatGrp == null && lusr != null)
		{
			Log.printVerbose("Adding new GLCategory");
			GLCategoryHome lGLCatH = GLCategoryNut.getHome();
			java.util.Date ldt = new java.util.Date();
			Timestamp tsCreate = new Timestamp(ldt.getTime());
			Integer usrid = null;
			try
			{
				usrid = lusr.getUserId();
			} catch (Exception ex)
			{
				Log.printAudit("User does not exist: " + ex.getMessage());
			}
			try
			{
				GLCategory newGLCat = (GLCategory) lGLCatH.create(glCategoryCode, glCategoryName, glCategoryDesc,
						glCategoryLedgerSide, glCategoryType, glCategoryPostToSection, glCategoryOptions);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCategory " + ex.getMessage());
			}
		}
		fnGetGLCategoryList(servlet, req, res);
	}
}
