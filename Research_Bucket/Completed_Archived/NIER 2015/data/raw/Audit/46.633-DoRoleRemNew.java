package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.vlee.util.*;

import java.util.*;

import com.vlee.ejb.accounting.CardPaymentConfigBean;
import com.vlee.ejb.user.*;

public class DoRoleRemNew extends ActionDo implements Action
{
	Role usr;

	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		UserRole bufUR = null;
		// HttpSession session = req.getSession();
		String rname[] = req.getParameterValues("removeRole");
		if (rname == null)
		{
			return new ActionRouter("sa-new-redirect-addrem-role-page");
		}
		if (rname.equals("noname"))
		{
			return new ActionRouter("sa-new-redirect-addrem-role-page");
		}
		// Role role = getRoleHandle(rname);
		RoleHome lRoleHome = getRoleHome();
		Role role = null;
		Role nonameRole = null;
		Calendar cal = Calendar.getInstance();
		Collection colUserRoles = null;
		UserRoleHome lUserRoleHome = null;
		Iterator itr = null;
		String remrname = null;
		for (int cnt = 0; cnt < rname.length; cnt++)
		{
			remrname = rname[cnt];
			role = getRoleHandle(lRoleHome, remrname);
			nonameRole = getRoleHandle(lRoleHome, new String("noname"));
			lUserRoleHome = getUserRoleHome();
			try
			{
				colUserRoles = (Collection) lUserRoleHome.findUserRolesGiven(new String("roleid"), role.getRoleId()
						.toString());
			} catch (Exception ex)
			{
				Log.printDebug("DoUserRoleAddRem:" + ex.getMessage());
			}
			itr = colUserRoles.iterator();
			if (itr.hasNext())
			{
				bufUR = (UserRole) itr.next();
				bufUR.setRoleId(nonameRole.getRoleId());
				bufUR.setTheDate(cal);
			}
			// todo: should be passivating roles instead of
			// deleting from database to maintain
			// database integrity.
			try
			{
				role.remove();
				{
					HttpSession session = req.getSession();
					AuditTrailObject atObj = new AuditTrailObject();
					atObj.userId = (Integer) session.getAttribute("userId");
					atObj.auditType = AuditTrailBean.TYPE_CONFIG;
					atObj.time = TimeFormat.getTimestamp();
					atObj.remarks = "delete role ";
					atObj.tc_entity_table = RoleBean.TABLENAME;
					atObj.tc_entity_id = role.getRoleId();
					atObj.tc_action = AuditTrailBean.TC_ACTION_DELETE;
					AuditTrailNut.fnCreate(atObj);
				}
			} catch (Exception ex)
			{
				Log.printDebug("DoRoleRem : " + ex.getMessage());
			}
		}// for loop closed
		return new ActionRouter("sa-new-redirect-addrem-role-page");
	}
}
