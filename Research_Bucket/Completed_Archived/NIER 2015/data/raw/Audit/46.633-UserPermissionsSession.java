package com.vlee.bean.user;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.math.BigDecimal;
import com.vlee.util.*;
import com.vlee.ejb.user.*;

public class UserPermissionsSession extends java.lang.Object implements Serializable
{
	public Integer roleid = null;
	public String roleName = null;
	private String status = "active";
	private Vector vecUserObjectPermission = null;
	private Integer userId;
	private static final String strObjectName = "UserPermissionsSession";

	// / contructor!
	public UserPermissionsSession(Integer roleId) throws Exception
	{
		this.roleid = roleId;
		this.vecUserObjectPermission = permissionList();
	}

	public void setUserId(Integer val)
	{
		this.userId = val;
	}
	
	public void addPermission(String permissionName, String permissionDescription, String objectType, String objectId)
	{
		UserPermissionsObject uperObj = null;
		UserObjectPermissionsObject uobjperObj = null;
		Integer perpkid = null;
		UserPermissionsObject upObj = null;
		Vector vecpkid1 = null;
		QueryObject query = new QueryObject(new String[] { UserPermissionsBean.NAME + "= '" + permissionName + "' " });
		try
		{
			try
			{
				vecpkid1 = new Vector(UserPermissionsNut.getObjects(query));
				if (vecpkid1 != null)
				{
					upObj = (UserPermissionsObject) vecpkid1.get(0);
					perpkid = upObj.pkid;
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			if (perpkid == null)
			{
				uperObj = new UserPermissionsObject();
				uperObj.name = permissionName;
				uperObj.description = permissionDescription;
				uperObj.status = this.status;
				perpkid = UserPermissionsNut.fnCreate(uperObj);
			}
			uobjperObj = new UserObjectPermissionsObject();
			uobjperObj.roleid = this.roleid;
			uobjperObj.objtype = objectType;
			uobjperObj.objid = objectId;
			uobjperObj.permissionid = perpkid;
			UserObjectPermissions ejb = UserObjectPermissionsNut.fnCreate(uobjperObj);
			{
				AuditTrailObject atObj = new AuditTrailObject();
				atObj.userId = this.userId;
				atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
				atObj.remarks = "sa: permission-add ";
				atObj.tc_entity_table = UserObjectPermissionsBean.TABLENAME;
				atObj.tc_entity_id = ejb.getPkid();
				atObj.tc_action = AuditTrailBean.TC_ACTION_CREATE;
				AuditTrailNut.fnCreate(atObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void clearPermission(String permName, String ObjectType, Integer roleId)
	{
		Integer permid = null;
		UserPermissionsObject upObjPkid = null;
		Vector vecpkid = null;
		QueryObject query1 = new QueryObject(new String[] { UserPermissionsBean.NAME + "= '" + permName + "' " });
		try
		{
			vecpkid = new Vector(UserPermissionsNut.getObjects(query1));
			if (vecpkid != null)
			{
				upObjPkid = (UserPermissionsObject) vecpkid.get(0);
				permid = upObjPkid.pkid;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		QueryObject query = new QueryObject(new String[] {
				UserObjectPermissionsBean.PERMISSION_ID + " = '" + getStrValue(permid) + "' ",
				UserObjectPermissionsBean.OBJ_TYPE + " = '" + ObjectType + "' ",
				UserObjectPermissionsBean.ROLE_ID + " = '" + getStrValue(roleId) + "' " });
		UserObjectPermissionsObject uopObject = null;
		try
		{
			Vector vecUOPToDelete = new Vector(UserObjectPermissionsNut.getObjects(query));
			for (int cnt = 0; cnt < vecUOPToDelete.size(); cnt++)
			{
				uopObject = (UserObjectPermissionsObject) vecUOPToDelete.get(cnt);
				UserObjectPermissions uopEJB = UserObjectPermissionsNut.getHandle(uopObject.pkid);
				uopEJB.remove();
				{
					AuditTrailObject atObj = new AuditTrailObject();
					atObj.userId = this.userId;
					atObj.auditType = AuditTrailBean.TYPE_SYSADMIN;
					atObj.remarks = "sa: permission-delete ";
					atObj.tc_entity_table = UserObjectPermissionsBean.TABLENAME;
					atObj.tc_entity_id = uopObject.pkid;
					atObj.tc_action = AuditTrailBean.TC_ACTION_DELETE;
					AuditTrailNut.fnCreate(atObj);
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void clearAuthorityLevel()
	{	
		QueryObject query = new QueryObject(new String[] {
				UserPermissionsBean.NAME + " LIKE 'perm_distribution_level_%' "});	
		UserPermissionsObject uopObject = null;
		try
		{
			Vector vecUOPToDelete = new Vector(UserPermissionsNut.getObjects(query));
			
			for (int cnt = 0; cnt < vecUOPToDelete.size(); cnt++)
			{
				uopObject = (UserPermissionsObject) vecUOPToDelete.get(cnt);
				query = new QueryObject(new String[] {
						UserObjectPermissionsBean.PERMISSION_ID + " = '" + uopObject.pkid + "' "});	
				Vector vecTarget = (Vector) UserObjectPermissionsNut.getObjects(query);
				if(vecTarget.size()>0)
				{
					UserObjectPermissionsObject target = (UserObjectPermissionsObject) vecTarget.get(0);
					UserObjectPermissions uopEJB = UserObjectPermissionsNut.getHandle(target.pkid);
					uopEJB.remove();
				}

			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public boolean hasAuthorityLevel(String permName)
	{
		boolean result = false;
		String permissionId = null;
		UserPermissionsObject upObj = null;
		Vector vecpkid = null;
		QueryObject query = new QueryObject(new String[] { UserPermissionsBean.NAME + " = '" + permName + "' " });
		try
		{
			
			vecpkid = new Vector(UserPermissionsNut.getObjects(query));
			if (vecpkid != null)
			{
				for (int i = 0; i < vecpkid.size(); i++)
				{
					upObj = (UserPermissionsObject) vecpkid.get(i);
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (upObj != null)
		{
			permissionId = upObj.pkid.toString();
			query = new QueryObject(new String[] { UserObjectPermissionsBean.PERMISSION_ID + " = '" + permissionId + "' ",
					UserObjectPermissionsBean.ROLE_ID + " = '" + this.roleid + "'"});
			Vector vecUOP = new Vector();
			
			vecUOP = (Vector) UserObjectPermissionsNut.getObjects(query);
			UserObjectPermissionsObject uopObj = null;
			if(vecUOP.size()>0)
			{
				uopObj = (UserObjectPermissionsObject) vecUOP.get(0);
			}
			if(uopObj!=null)
			{
				result = true;
			}
		}


		return result;
	}
	
	public boolean hasPermission(String permName, String objectType, String objectId)
	{
		boolean result = false;
		String permissionId = null;
		UserObjectPermissionsObject uop = null;
		UserPermissionsObject upObj = null;
		Vector vecpkid = null;
		QueryObject query = new QueryObject(new String[] { UserPermissionsBean.NAME + " = '" + permName + "' " });
		try
		{
			vecpkid = new Vector(UserPermissionsNut.getObjects(query));
			if (vecpkid != null)
			{
				for (int i = 0; i < vecpkid.size(); i++)
				{
					upObj = (UserPermissionsObject) vecpkid.get(i);
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (upObj != null)
		{
			permissionId = upObj.pkid.toString();
		}
		if (this.vecUserObjectPermission != null)
		{
			for (int cnt = 0; cnt < this.vecUserObjectPermission.size(); cnt++)
			{
				uop = (UserObjectPermissionsObject) this.vecUserObjectPermission.get(cnt);
				if ((getStrValue(uop.objtype).equals(objectType) && (getStrValue(uop.permissionid).equals(permissionId)))
						&& (getStrValue(uop.objid).equals(objectId)))
				{
					result = true;
				}
			}
		}
		return result;
	}

	private Vector permissionList()
	{
		Vector vecPermissions = null;
		UserObjectPermissionsObject uopObj = null;
		QueryObject query = new QueryObject(new String[] { UserObjectPermissionsBean.ROLE_ID + " = '"
				+ this.roleid.toString() + "' " });
		try
		{
			vecPermissions = new Vector(UserObjectPermissionsNut.getObjects(query));
		} catch (Exception ex)
		{
		}
		return vecPermissions;
	}

	/*
	 * public void reset() { this.roleid = null; this.objid = ""; this.objtype =
	 * "";
	 *  }
	 */
	public void reloadPermission()
	{
		this.vecUserObjectPermission = permissionList();
	}

	private String getStrValue(Object o)
	{
		if (o == null)
			return "";
		else
			return o.toString();
	}

	// //////////////////////////////////////////////////////
	public Integer getRoleId()
	{
		return this.roleid;
	}

	public void setRoleId(Integer roleId)
	{
		this.roleid = roleId;
	}

	public String getRoleName()
	{
		return this.roleName;
	}

	public void setRoleName(String rolename)
	{
		this.roleName = rolename;
	}
}
