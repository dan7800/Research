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
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCodeAddRem implements Action
{
	private String strClassName = "DoGLCodeAddRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("acc-add-glcode-page");
		} else if (formName.equals("addGLCode"))
		{
			fnAddGLCode(servlet, req, res);
		} else if (formName.equals("rmGLCode"))
		{
			fnRmGLCode(servlet, req, res);
		} else if (formName.equals("editGLCode"))
		{
			fnPopulateGLCategory(servlet, req, res);
			fnPopulateGLCode(servlet, req, res);
			return new ActionRouter("acc-edit-glcode-page");
		} else if (formName.equals("updateGLCode"))
		{
			fnUpdateGLCode(servlet, req, res);
		} else if (formName.equals("viewGLCode"))
		{
		} else if (formName.equals("popupGLCodeList"))
		{
			Vector vecGLCatTree = GLCategoryNut.getValueObjectsTree((String) null, (String) null, (String) null,
					(String) null, (String) null, (String) null);
			req.setAttribute("vecGLCatTree", vecGLCatTree);
			return new ActionRouter("acc-popup-glcode-list-page");
		}
		// fnGetGLCodeList(servlet, req, res);
		return new ActionRouter("acc-list-glcode-page");
	}
	;
	protected void fnPopulateGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCategory()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCategory = GLCategoryNut.getAllValueObjects();
		req.setAttribute("vecGLCategory", vecGLCategory);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCode()";
		Log.printVerbose("In " + strClassName + funcName);
		Integer pkid = new Integer(req.getParameter("pkid"));
		GLCodeObject glco = GLCodeNut.getObject(pkid);
		req.setAttribute("glco", glco);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnRmGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRmGLCode()";
		String rmGLCodeCode = (String) req.getParameter("removeGLCode");
		if (rmGLCodeCode != null)
		{
			GLCode lGLCode = GLCodeNut.getObjectByCode(rmGLCodeCode);
			if (lGLCode != null)
			{
				try
				{
					lGLCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCode Failed" + ex.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetGLCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetGLCodeList()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCode = GLCodeNut.getAllValueObjects();
		req.setAttribute("vecGLCode", vecGLCode);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddGLCode()";
		// String strPkid = trim(req.getParameter("pkid"));
		// String pkid= trim(req.getParameter("pkid"));
		String code = trim(req.getParameter("code"));
		String name = trim(req.getParameter("name"));
		String description = trim(req.getParameter("description"));
		String ledgerSide = trim(req.getParameter("ledgerSide"));
		String glCategory = trim(req.getParameter("glCategory"));
		String refNumber = trim(req.getParameter("refNumber"));
		// 20080411 Jimmy
		String grouping1 = trim(req.getParameter("grouping1"));
		String grouping2 = trim(req.getParameter("grouping2"));
		String grouping3 = trim(req.getParameter("grouping3"));
		
		code = StringManup.removeSymbols(code);

		// Integer pkid = null;
		// try {
		// pkid = new Integer(strPkid);
		// } catch(Exception ex) {
		// }
		// if (pkid == null) return;
		if (code.equals(""))
			return;
		if (name.equals(""))
			return;
		// if (description.equals("")) return;
		if (ledgerSide.equals(""))
			return;
		if (glCategory.equals(""))
			return;
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		// GLCode glCodeEJB = GLCodeNut.getHandle(pkid);
		GLCode lGLCodeGrp = GLCodeNut.getObjectByCode(code);
		// if (lGLCodeGrp == null && lusr != null && glCodeEJB == null) {
		if (lGLCodeGrp == null && lusr != null)
		{
			GLCodeHome lGLCodeH = GLCodeNut.getHome();
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
				GLCode newGLCode = (GLCode) lGLCodeH.create(code, name, description, ledgerSide,
						new Integer(glCategory), refNumber, grouping1, grouping2, grouping3);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCode " + ex.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateGLCode()";
		String action = req.getParameter("action");
		Integer pkid = new Integer(req.getParameter("pkid"));
		if (action.equals("Save Changes"))
		{
			// String code = trim(req.getParameter("code"));
			String name = trim(req.getParameter("name"));
			String description = trim(req.getParameter("description"));
			String ledgerSide = trim(req.getParameter("ledgerSide"));
			String glCategory = trim(req.getParameter("glCategory"));
			String refNumber = trim(req.getParameter("refNumber"));
			// 2008411 Jimmy
			String grouping1 = trim(req.getParameter("grouping1"));
			String grouping2 = trim(req.getParameter("grouping2"));
			String grouping3 = trim(req.getParameter("grouping3"));
			
			// if (code.equals("")) return;
			if (name.equals(""))
				return;
			if (description.equals(""))
				return;
			if (ledgerSide.equals(""))
				return;
			if (glCategory.equals(""))
				return;

			GLCode glc = GLCodeNut.getHandle(pkid);
			try
			{
				Integer catPkid = new Integer(glCategory);
				GLCodeObject glco = glc.getValueObject();
				glco.name = name;
				glco.description = description;
				glco.ledgerSide = ledgerSide;
				glco.glCategoryId = catPkid;
				glco.codeOld = refNumber;
				// 20080411 Jimmy
				glco.grouping1 = grouping1;
				glco.grouping2 = grouping2;
				glco.grouping3 = grouping3;
				glc.setValueObject(glco);
				
			} catch (Exception ex)
			{
				Log.printDebug("Cannot edit GLCode " + ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			try
			{
				// we do not allow removal of gl code
				// GLCode glc = GLCodeNut.getHandle(pkid);
				// glc.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove gLCode Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
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
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCodeAddRem implements Action
{
	private String strClassName = "DoGLCodeAddRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("acc-add-glcode-page");
		} else if (formName.equals("addGLCode"))
		{
			fnAddGLCode(servlet, req, res);
		} else if (formName.equals("rmGLCode"))
		{
			fnRmGLCode(servlet, req, res);
		} else if (formName.equals("editGLCode"))
		{
			fnPopulateGLCategory(servlet, req, res);
			fnPopulateGLCode(servlet, req, res);
			return new ActionRouter("acc-edit-glcode-page");
		} else if (formName.equals("updateGLCode"))
		{
			fnUpdateGLCode(servlet, req, res);
		} else if (formName.equals("viewGLCode"))
		{
		} else if (formName.equals("popupGLCodeList"))
		{
			Vector vecGLCatTree = GLCategoryNut.getValueObjectsTree((String) null, (String) null, (String) null,
					(String) null, (String) null, (String) null);
			req.setAttribute("vecGLCatTree", vecGLCatTree);
			return new ActionRouter("acc-popup-glcode-list-page");
		}
		// fnGetGLCodeList(servlet, req, res);
		return new ActionRouter("acc-list-glcode-page");
	}

	protected void fnPopulateGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCategory()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCategory = GLCategoryNut.getAllValueObjects();
		req.setAttribute("vecGLCategory", vecGLCategory);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCode()";
		Log.printVerbose("In " + strClassName + funcName);
		Integer pkid = new Integer(req.getParameter("pkid"));
		GLCodeObject glco = GLCodeNut.getObject(pkid);
		req.setAttribute("glco", glco);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnRmGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRmGLCode()";
		String rmGLCodeCode = (String) req.getParameter("removeGLCode");
		if (rmGLCodeCode != null)
		{
			GLCode lGLCode = GLCodeNut.getObjectByCode(rmGLCodeCode);
			if (lGLCode != null)
			{
				try
				{
					lGLCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCode Failed" + ex.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetGLCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetGLCodeList()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCode = GLCodeNut.getAllValueObjects();
		req.setAttribute("vecGLCode", vecGLCode);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddGLCode()";
		// String strPkid = trim(req.getParameter("pkid"));
		// String pkid= trim(req.getParameter("pkid"));
		String code = trim(req.getParameter("code"));
		String name = trim(req.getParameter("name"));
		String description = trim(req.getParameter("description"));
		String ledgerSide = trim(req.getParameter("ledgerSide"));
		String glCategory = trim(req.getParameter("glCategory"));
		
		code = StringManup.removeSymbols(code);

		// Integer pkid = null;
		// try {
		// pkid = new Integer(strPkid);
		// } catch(Exception ex) {
		// }
		// if (pkid == null) return;
		if (code.equals(""))
			return;
		if (name.equals(""))
			return;
		// if (description.equals("")) return;
		if (ledgerSide.equals(""))
			return;
		if (glCategory.equals(""))
			return;
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		// GLCode glCodeEJB = GLCodeNut.getHandle(pkid);
		GLCode lGLCodeGrp = GLCodeNut.getObjectByCode(code);
		// if (lGLCodeGrp == null && lusr != null && glCodeEJB == null) {
		if (lGLCodeGrp == null && lusr != null)
		{
			GLCodeHome lGLCodeH = GLCodeNut.getHome();
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
				GLCode newGLCode = (GLCode) lGLCodeH.create(code, name, description, ledgerSide,
						new Integer(glCategory));
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCode " + ex.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateGLCode()";
		String action = req.getParameter("action");
		Integer pkid = new Integer(req.getParameter("pkid"));
		if (action.equals("Save Changes"))
		{
			// String code = trim(req.getParameter("code"));
			String name = trim(req.getParameter("name"));
			String description = trim(req.getParameter("description"));
			String ledgerSide = trim(req.getParameter("ledgerSide"));
			String glCategory = trim(req.getParameter("glCategory"));
			// if (code.equals("")) return;
			if (name.equals(""))
				return;
			if (description.equals(""))
				return;
			if (ledgerSide.equals(""))
				return;
			if (glCategory.equals(""))
				return;
			GLCode glc = GLCodeNut.getHandle(pkid);
			try
			{
				Integer catPkid = new Integer(glCategory);
				GLCodeObject glco = glc.getValueObject();
				glco.name = name;
				glco.description = description;
				;
				glco.ledgerSide = ledgerSide;
				glco.glCategoryId = catPkid;
				glc.setValueObject(glco);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot edit GLCode " + ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			try
			{
				// we do not allow removal of gl code
				// GLCode glc = GLCodeNut.getHandle(pkid);
				// glc.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove gLCode Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
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
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCodeAddRem implements Action
{
	private String strClassName = "DoGLCodeAddRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("acc-add-glcode-page");
		} else if (formName.equals("addGLCode"))
		{
			fnAddGLCode(servlet, req, res);
		} else if (formName.equals("rmGLCode"))
		{
			fnRmGLCode(servlet, req, res);
		} else if (formName.equals("editGLCode"))
		{
			fnPopulateGLCategory(servlet, req, res);
			fnPopulateGLCode(servlet, req, res);
			return new ActionRouter("acc-edit-glcode-page");
		} else if (formName.equals("updateGLCode"))
		{
			fnUpdateGLCode(servlet, req, res);
		} else if (formName.equals("viewGLCode"))
		{
		} else if (formName.equals("popupGLCodeList"))
		{
			Vector vecGLCatTree = GLCategoryNut.getValueObjectsTree((String) null, (String) null, (String) null,
					(String) null, (String) null, (String) null);
			req.setAttribute("vecGLCatTree", vecGLCatTree);
			return new ActionRouter("acc-popup-glcode-list-page");
		}
		// fnGetGLCodeList(servlet, req, res);
		return new ActionRouter("acc-list-glcode-page");
	}

	protected void fnPopulateGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCategory()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCategory = GLCategoryNut.getAllValueObjects();
		req.setAttribute("vecGLCategory", vecGLCategory);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCode()";
		Log.printVerbose("In " + strClassName + funcName);
		Integer pkid = new Integer(req.getParameter("pkid"));
		GLCodeObject glco = GLCodeNut.getObject(pkid);
		req.setAttribute("glco", glco);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnRmGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRmGLCode()";
		String rmGLCodeCode = (String) req.getParameter("removeGLCode");
		if (rmGLCodeCode != null)
		{
			GLCode lGLCode = GLCodeNut.getObjectByCode(rmGLCodeCode);
			if (lGLCode != null)
			{
				try
				{
					lGLCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCode Failed" + ex.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetGLCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetGLCodeList()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCode = GLCodeNut.getAllValueObjects();
		req.setAttribute("vecGLCode", vecGLCode);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddGLCode()";
		// String strPkid = trim(req.getParameter("pkid"));
		// String pkid= trim(req.getParameter("pkid"));
		String code = trim(req.getParameter("code"));
		String name = trim(req.getParameter("name"));
		String description = trim(req.getParameter("description"));
		String ledgerSide = trim(req.getParameter("ledgerSide"));
		String glCategory = trim(req.getParameter("glCategory"));
		// Integer pkid = null;
		// try {
		// pkid = new Integer(strPkid);
		// } catch(Exception ex) {
		// }
		// if (pkid == null) return;
		if (code.equals(""))
			return;
		if (name.equals(""))
			return;
		// if (description.equals("")) return;
		if (ledgerSide.equals(""))
			return;
		if (glCategory.equals(""))
			return;
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		// GLCode glCodeEJB = GLCodeNut.getHandle(pkid);
		GLCode lGLCodeGrp = GLCodeNut.getObjectByCode(code);
		// if (lGLCodeGrp == null && lusr != null && glCodeEJB == null) {
		if (lGLCodeGrp == null && lusr != null)
		{
			GLCodeHome lGLCodeH = GLCodeNut.getHome();
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
				GLCode newGLCode = (GLCode) lGLCodeH.create(code, name, description, ledgerSide,
						new Integer(glCategory));
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCode " + ex.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateGLCode()";
		String action = req.getParameter("action");
		Integer pkid = new Integer(req.getParameter("pkid"));
		if (action.equals("Save Changes"))
		{
			// String code = trim(req.getParameter("code"));
			String name = trim(req.getParameter("name"));
			String description = trim(req.getParameter("description"));
			String ledgerSide = trim(req.getParameter("ledgerSide"));
			String glCategory = trim(req.getParameter("glCategory"));
			// if (code.equals("")) return;
			if (name.equals(""))
				return;
			if (description.equals(""))
				return;
			if (ledgerSide.equals(""))
				return;
			if (glCategory.equals(""))
				return;
			GLCode glc = GLCodeNut.getHandle(pkid);
			try
			{
				Integer catPkid = new Integer(glCategory);
				GLCodeObject glco = glc.getValueObject();
				glco.name = name;
				glco.description = description;
				;
				glco.ledgerSide = ledgerSide;
				glco.glCategoryId = catPkid;
				glc.setValueObject(glco);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot edit GLCode " + ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			try
			{
				// we do not allow removal of gl code
				// GLCode glc = GLCodeNut.getHandle(pkid);
				// glc.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove gLCode Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
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
package com.vlee.servlet.accounting;

import com.vlee.servlet.main.*;
import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;

public class DoGLCodeAddRem implements Action
{
	private String strClassName = "DoGLCodeAddRem: ";

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		String formName = req.getParameter("formName");
		if (formName == null)
		{
			return new ActionRouter("acc-add-glcode-page");
		} else if (formName.equals("addGLCode"))
		{
			fnAddGLCode(servlet, req, res);
		} else if (formName.equals("rmGLCode"))
		{
			fnRmGLCode(servlet, req, res);
		} else if (formName.equals("editGLCode"))
		{
			fnPopulateGLCategory(servlet, req, res);
			fnPopulateGLCode(servlet, req, res);
			return new ActionRouter("acc-edit-glcode-page");
		} else if (formName.equals("updateGLCode"))
		{
			fnUpdateGLCode(servlet, req, res);
		} else if (formName.equals("viewGLCode"))
		{
		} else if (formName.equals("popupGLCodeList"))
		{
			Vector vecGLCatTree = GLCategoryNut.getValueObjectsTree((String) null, (String) null, (String) null,
					(String) null, (String) null, (String) null);
			req.setAttribute("vecGLCatTree", vecGLCatTree);
			return new ActionRouter("acc-popup-glcode-list-page");
		}
		// fnGetGLCodeList(servlet, req, res);
		return new ActionRouter("acc-list-glcode-page");
	}

	protected void fnPopulateGLCategory(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCategory()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCategory = GLCategoryNut.getAllValueObjects();
		req.setAttribute("vecGLCategory", vecGLCategory);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnPopulateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnPopulateGLCode()";
		Log.printVerbose("In " + strClassName + funcName);
		Integer pkid = new Integer(req.getParameter("pkid"));
		GLCodeObject glco = GLCodeNut.getObject(pkid);
		req.setAttribute("glco", glco);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnRmGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnRmGLCode()";
		String rmGLCodeCode = (String) req.getParameter("removeGLCode");
		if (rmGLCodeCode != null)
		{
			GLCode lGLCode = GLCodeNut.getObjectByCode(rmGLCodeCode);
			if (lGLCode != null)
			{
				try
				{
					lGLCode.remove();
				} catch (Exception ex)
				{
					Log.printDebug("Remove GLCode Failed" + ex.getMessage());
				}
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnGetGLCodeList(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnGetGLCodeList()";
		Log.printVerbose("In " + strClassName + funcName);
		Vector vecGLCode = GLCodeNut.getAllValueObjects();
		req.setAttribute("vecGLCode", vecGLCode);
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnAddGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnAddGLCode()";
		// String strPkid = trim(req.getParameter("pkid"));
		// String pkid= trim(req.getParameter("pkid"));
		String code = trim(req.getParameter("code"));
		String name = trim(req.getParameter("name"));
		String description = trim(req.getParameter("description"));
		String ledgerSide = trim(req.getParameter("ledgerSide"));
		String glCategory = trim(req.getParameter("glCategory"));
		
		code = StringManup.removeSymbols(code);

		// Integer pkid = null;
		// try {
		// pkid = new Integer(strPkid);
		// } catch(Exception ex) {
		// }
		// if (pkid == null) return;
		if (code.equals(""))
			return;
		if (name.equals(""))
			return;
		// if (description.equals("")) return;
		if (ledgerSide.equals(""))
			return;
		if (glCategory.equals(""))
			return;
		HttpSession session = req.getSession();
		User lusr = UserNut.getHandle((String) session.getAttribute("userName"));
		// GLCode glCodeEJB = GLCodeNut.getHandle(pkid);
		GLCode lGLCodeGrp = GLCodeNut.getObjectByCode(code);
		// if (lGLCodeGrp == null && lusr != null && glCodeEJB == null) {
		if (lGLCodeGrp == null && lusr != null)
		{
			GLCodeHome lGLCodeH = GLCodeNut.getHome();
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
				GLCode newGLCode = (GLCode) lGLCodeH.create(code, name, description, ledgerSide,
						new Integer(glCategory));
			} catch (Exception ex)
			{
				Log.printDebug("Cannot create GLCode " + ex.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	protected void fnUpdateGLCode(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
	{
		String funcName = "fnUpdateGLCode()";
		String action = req.getParameter("action");
		Integer pkid = new Integer(req.getParameter("pkid"));
		if (action.equals("Save Changes"))
		{
			// String code = trim(req.getParameter("code"));
			String name = trim(req.getParameter("name"));
			String description = trim(req.getParameter("description"));
			String ledgerSide = trim(req.getParameter("ledgerSide"));
			String glCategory = trim(req.getParameter("glCategory"));
			// if (code.equals("")) return;
			if (name.equals(""))
				return;
			if (description.equals(""))
				return;
			if (ledgerSide.equals(""))
				return;
			if (glCategory.equals(""))
				return;
			GLCode glc = GLCodeNut.getHandle(pkid);
			try
			{
				Integer catPkid = new Integer(glCategory);
				GLCodeObject glco = glc.getValueObject();
				glco.name = name;
				glco.description = description;
				;
				glco.ledgerSide = ledgerSide;
				glco.glCategoryId = catPkid;
				glc.setValueObject(glco);
			} catch (Exception ex)
			{
				Log.printDebug("Cannot edit GLCode " + ex.getMessage());
			}
		} else if (action.equals("Delete Account"))
		{
			try
			{
				// we do not allow removal of gl code
				// GLCode glc = GLCodeNut.getHandle(pkid);
				// glc.remove();
			} catch (Exception e)
			{
				Log.printDebug("Remove gLCode Failed" + e.getMessage());
			}
		}
		Log.printVerbose("Leaving " + strClassName + funcName);
	}

	private String trim(String t)
	{
		if (t == null)
			return "";
		return t.trim();
	}
}
