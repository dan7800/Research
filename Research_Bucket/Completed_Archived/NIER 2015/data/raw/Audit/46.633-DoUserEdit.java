package com.vlee.servlet.sysadmin;

import com.vlee.servlet.main.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.vlee.util.*;

import java.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.accounting.*;


public class DoUserEdit extends ActionDo implements Action
{
	public ActionRouter perform(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res)
			throws java.io.IOException, javax.servlet.ServletException
	{
		
		String formName = req.getParameter("formName");
		String fieldName = new String("status");
		Collection colRoles = null;
		RoleHome lRoleHome = getRoleHome();
		Iterator itrActiveRoles = null;
		UserObject uob = null;
		if (formName == null)
		{
			// [[ joe JOB07042501
			HttpSession session = req.getSession();
			// Integer userIdEdit = (Integer) session.getAttribute("userId");
			Integer userIdEdit = new Integer(req.getParameter("userId"));
			UserConfigObject ucObj;
			// joe JOB07042501]]
			String userId = req.getParameter("userId");
			String roleId = req.getParameter("roleId");
			String userroleId = req.getParameter("userRoleId");
			Log.printDebug("DoUserEdit:formname=null: roleId=" + roleId);
			Integer userId1 = new Integer(userId);
			try
			{
				colRoles = (Collection) lRoleHome.findRolesGiven(fieldName, UserRoleBean.ACTIVE);
				itrActiveRoles = colRoles.iterator();
				uob = UserNut.getObject(userId1);
				// [[ joe JOB07042501
				ucObj = UserConfigRegistryNut.getUserConfigObject(userIdEdit);
				req.setAttribute("ucObj", ucObj);
				System.out.println("TUPPENY: inside DoUserEdit::perform(): formName=null");
				String userBranch = req.getParameter("userBranch");
				Integer iUserBranch = new Integer(userBranch);
				BranchObject userBranchObj = BranchNut.getObject(iUserBranch);
				req.setAttribute("userBranch", userBranch);
				// joe JOB07042501]]
			} catch (Exception e)
			{
			}
			req.setAttribute("roleId", roleId);
			req.setAttribute("userRoleId", userroleId);
			req.setAttribute("itrActiveRoles", itrActiveRoles);
			req.setAttribute("userObject", uob);
			
			// [[joe JOB07042501
			//req.setAttribute("ucObj", ucObj);
			
			// joe JOB07042501]]
			return new ActionRouter("sa-edit-user-page");
		}
		if (formName.equals("editUser"))
		{
			Integer userId2 = new Integer(getStrValue(req.getParameter("userId")));
			Integer userRoleId = new Integer(getStrValue(req.getParameter("userRoleId")));
			String pwd = req.getParameter("password");
			String nameFirst = req.getParameter("nameFirst");
			String nameLast = req.getParameter("nameLast");
			String userStatus = req.getParameter("userStatus");
			Integer roleId = new Integer(getStrValue(req.getParameter("roleId")));
			
			
			String checkPwd = getStrValue(req.getParameter("checkpwd"));
			
			HttpSession session = req.getSession();
			//Integer userId = (Integer) req.getParameter("userId");
			// [[joe JOB07042501
			System.out.println("TUPPENY: inside DoUserEdit::perform(): formName=editUser");
			String userBranch = req.getParameter("userBranch");
			Integer iUserBranch = new Integer(userBranch);
			BranchObject userBranchObj = BranchNut.getObject(iUserBranch);
			
			//UserConfigRegistryNut.fnCreate(userId2, UserConfigRegistryBean.CAT_DEFAULT,
			//		UserConfigRegistryBean.NS_CUSTSVCCTR, iUserBranch.toString(), (String) null, (String) null, (String) null,
			//		(String) null, userId2, TimeFormat.getTimestamp(), TimeFormat.getTimestamp());
			UserConfigObject ucObj = UserConfigRegistryNut.getUserConfigObject(userId2);
			System.out.println("TUPPENY: inside DoUserEdit::perform(): formName=editUser, ucObj not null. Yeah");
			if(ucObj!=null) // set it
			{
				System.out.println("TUPPENY FUK: inside DoUserEdit::perform(): formName=editUser, ucObj=null?? this shouldn't happen");
				ucObj.mDefaultCustSvcCtr = iUserBranch;
				UserConfigRegistryNut.setUserConfigObject(ucObj);
				
			}
			req.setAttribute("ucObj", ucObj);
			req.setAttribute("userBranch", userBranch);
			
			// joe JOB07042501]]
			
			try
			{
				UserHome lUserHome = getUserHome();
				UserRoleHome lUserRoleHome = getUserRoleHome();
				User user = UserNut.getHandle(lUserHome, userId2);
				UserRole userrole = UserRoleNut.getHandle(lUserRoleHome, userRoleId);
				
				if ((checkPwd.equals("checked")) && (pwd.length() > 4))
				{
					user.setPassword(pwd);
					Log.printDebug("DoUserEdit : Password changed");
										
					{
						System.out.println("Recording change of password to audit trail, user : "+user.getName());
						
						AuditTrailObject atObj = new AuditTrailObject();
						atObj.userId = (Integer) session.getAttribute("userId");
						atObj.auditType = AuditTrailBean.TYPE_CONFIG;
						atObj.time = TimeFormat.getTimestamp();
						atObj.remarks = "change-password: User :" + user.getName();
						AuditTrailNut.fnCreate(atObj);
						
						System.out.println("Recorded change of password into audit trail");
					}
					
				} else
				{
					Log.printDebug("DoUserEdit : Password not Changed");
				}
				
				Integer originalRoleId = userrole.getRoleId();
				RoleObject originalRoleObj = RoleNut.getObject(originalRoleId);
				
				Integer newRoleId = roleId;
				RoleObject newRoleObj = RoleNut.getObject(roleId);
				
				
				user.setNameFirst(nameFirst);
				user.setNameLast(nameLast);
				user.setStatus(userStatus);
				userrole.setRoleId(newRoleId);				
				{					
					AuditTrailObject atObj = new AuditTrailObject();
					atObj.userId = (Integer) session.getAttribute("userId");
					atObj.auditType = AuditTrailBean.TYPE_CONFIG;
					atObj.time = TimeFormat.getTimestamp();
					atObj.remarks = "update user :" + user.getName();
					atObj.tc_entity_table = UserBean.TABLENAME;
					atObj.tc_entity_id = user.getUserId();
					atObj.tc_action = AuditTrailBean.TC_ACTION_UPDATE;
					AuditTrailNut.fnCreate(atObj);
					
					System.out.println("Recorded change of password into audit trail");
				}				
				if(! originalRoleId.toString().equals(userrole.getRoleId().toString()))
				{
					{
						System.out.println("Recording change of role to audit trail, user : "+user.getName());
						
						AuditTrailObject atObj = new AuditTrailObject();
						atObj.userId = (Integer) session.getAttribute("userId");
						atObj.auditType = AuditTrailBean.TYPE_CONFIG;
						atObj.time = TimeFormat.getTimestamp();
						atObj.remarks = "change-role : User " + user.getName() + " (" + originalRoleObj.rolename + " --> " + newRoleObj.rolename + ")";
						AuditTrailNut.fnCreate(atObj);
						
						System.out.println("Recorded change of role into audit trail");
					}
				}

			} catch (Exception ex)
			{
				Log.printDebug("DoUserRem : " + ex.getMessage());
			}
			return new ActionRouter("sa-new-redirect-addrem-user-page");
		}
		return new ActionRouter("sa-edit-user-page");
	}

	private String getStrValue(Object o)
	{
		if (o == null)
			return "";
		else
			return o.toString();
	}
}
