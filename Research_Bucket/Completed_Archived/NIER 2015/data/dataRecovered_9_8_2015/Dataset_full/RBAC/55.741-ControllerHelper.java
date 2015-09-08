/*
  $Id: ControllerHelper.java 78 2009-03-24 20:33:52Z marvin.addison $

  Copyright (C) 2008 Virginia Tech, Marvin S. Addison.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Marvin S. Addison
  Email:   serac@vt.edu
  Version: $Revision: 78 $
  Updated: $Date: 2009-03-24 13:33:52 -0700 (Tue, 24 Mar 2009) $
 */
package edu.vt.middleware.gator.web;

import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.BasePermission;

import edu.vt.middleware.gator.AppenderConfig;
import edu.vt.middleware.gator.AppenderParamConfig;
import edu.vt.middleware.gator.CategoryConfig;
import edu.vt.middleware.gator.ClientConfig;
import edu.vt.middleware.gator.LayoutParamConfig;
import edu.vt.middleware.gator.PermissionConfig;
import edu.vt.middleware.gator.ProjectConfig;

/**
 * Utility class provides common controller operations.
 *
 * @author Marvin S. Addison
 *
 */
public class ControllerHelper
{
  /** Creates a new instance */
  protected ControllerHelper() {}


  /**
   * Creates a deep clone of the given appender.
   * @param source Appender to clone.
   * @return Cloned appender.
   */
  public static AppenderConfig cloneAppender(final AppenderConfig source)
  {
    final AppenderConfig clone = new AppenderConfig();
    clone.setName(source.getName());
    clone.setAppenderClassName(source.getAppenderClassName());
    clone.setErrorHandlerClassName(source.getErrorHandlerClassName());
    clone.setLayoutClassName(source.getLayoutClassName());
    for (AppenderParamConfig param : source.getAppenderParams()) {
      final AppenderParamConfig newParam = new AppenderParamConfig();
      newParam.setName(param.getName());
      newParam.setValue(param.getValue());
      clone.addAppenderParam(newParam);
    }
    for (LayoutParamConfig param : source.getLayoutParams()) {
      final LayoutParamConfig newParam = new LayoutParamConfig();
      newParam.setName(param.getName());
      newParam.setValue(param.getValue());
      clone.addLayoutParam(newParam);
    }
    return clone;
  }


  /**
   * Creates a deep clone of the given category.
   * @param parent Project to which cloned category will eventually belong.
   * Only appenders in the source category that also belong to the parent
   * will be associated with the cloned category.
   * @param source Category to clone.
   * @return Cloned category.
   */
  public static CategoryConfig cloneCategory(
    final ProjectConfig parent,
    final CategoryConfig source)
  {
    final CategoryConfig clone = new CategoryConfig();
    clone.setName(source.getName());
    clone.setLevel(source.getLevel());
    for (AppenderConfig appender : source.getAppenders()) {
      final AppenderConfig appenderRef = parent.getAppender(appender.getName());
      if (appenderRef != null) {
        clone.getAppenders().add(appenderRef);
      }
    }
    return clone;
  }


  /**
   * Creates a deep clone of the given client.
   * @param source Client to clone.
   * @return Cloned client.
   */
  public static ClientConfig cloneClient(final ClientConfig source)
  {
    final ClientConfig clone = new ClientConfig();
    clone.setName(source.getName());
    return clone;
  }


  /**
   * Creates a deep clone of the given project.  All fields except
   * modifiedDate are cloned; the modified date is set to the current
   * system date/time.
   * @param source Project to clone.
   * @return Cloned project.
   */
  public static ProjectConfig cloneProject(final ProjectConfig source)
  {
    final ProjectConfig clone = new ProjectConfig();
    clone.setName(source.getName());
    clone.setClientLogDir(source.getClientLogDir());
    for (AppenderConfig appender : source.getAppenders()) {
      clone.addAppender(cloneAppender(appender));
    }
    for (CategoryConfig category : source.getCategories()) {
      clone.addCategory(cloneCategory(clone, category));
    }
    return clone;
  }


  /**
   * Filter a view name containing placeholders for values that
   * need to be replaced by data from a project.
   * @param viewName View name to filter.
   * @param project Project configuration.
   * @return New view name with placeholders replaced.
   */
  public static String filterViewName(final String viewName,
      final ProjectConfig project)
  {
    return viewName.replace("@PROJECT_NAME@", project.getName());
  }


  /**
   * Create a permission configuration containing all permissions for the
   * given security identifier.
   * @param sid Security identifier; either a username or role name.
   * @return Permission config with all permissions set for given SID.
   */
  public static PermissionConfig createAllPermissions(final String sid)
  {
    final PermissionConfig perm = new PermissionConfig();
    perm.setName(sid);
    int permBits = 0;
    for (Permission p : PermissionConfig.ALL_PERMISSIONS) {
      permBits |= p.getMask();
    }
    perm.setPermissionBits(permBits);
    return perm;
  }
  

  /**
   * Determines whether the given permission is the last full permission in
   * the given project.
   * @param project Project to test.
   * @param permissionId ID of permission to check.
   * @return True if given permission is last full permission in the given
   * project.
   */
  public static boolean isLastFullPermissions(
    final ProjectConfig project,
    final int permissionId)
  {
    int count = 0;
    int fullPermissionId = 0;
    for (PermissionConfig perm : project.getPermissions()) {
      if (perm.hasPermission(BasePermission.READ) &&
          perm.hasPermission(BasePermission.WRITE) &&
          perm.hasPermission(BasePermission.DELETE))
      {
        count++;
        fullPermissionId = perm.getId();
      }
    }
    return count <= 1 && fullPermissionId == permissionId;
  }
}
/*
  $Id: ControllerHelper.java 71 2009-03-23 18:23:55Z marvin.addison $

  Copyright (C) 2008 Virginia Tech, Marvin S. Addison.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Marvin S. Addison
  Email:   serac@vt.edu
  Version: $Revision: 71 $
  Updated: $Date: 2009-03-23 11:23:55 -0700 (Mon, 23 Mar 2009) $
 */
package edu.vt.middleware.gator.web;

import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.BasePermission;

import edu.vt.middleware.gator.AppenderConfig;
import edu.vt.middleware.gator.AppenderParamConfig;
import edu.vt.middleware.gator.CategoryConfig;
import edu.vt.middleware.gator.ClientConfig;
import edu.vt.middleware.gator.LayoutParamConfig;
import edu.vt.middleware.gator.PermissionConfig;
import edu.vt.middleware.gator.ProjectConfig;

/**
 * Utility class provides common controller operations.
 *
 * @author Marvin S. Addison
 *
 */
public class ControllerHelper
{
  /** Creates a new instance */
  protected ControllerHelper() {}


  /**
   * Creates a deep clone of the given appender.
   * @param source Appender to clone.
   * @return Cloned appender.
   */
  public static AppenderConfig cloneAppender(final AppenderConfig source)
  {
    final AppenderConfig clone = new AppenderConfig();
    clone.setName(source.getName());
    clone.setAppenderClassName(source.getAppenderClassName());
    clone.setErrorHandlerClassName(source.getErrorHandlerClassName());
    clone.setLayoutClassName(source.getLayoutClassName());
    for (AppenderParamConfig param : source.getAppenderParams()) {
      final AppenderParamConfig newParam = new AppenderParamConfig();
      newParam.setName(param.getName());
      newParam.setValue(param.getValue());
      clone.addAppenderParam(newParam);
    }
    for (LayoutParamConfig param : source.getLayoutParams()) {
      final LayoutParamConfig newParam = new LayoutParamConfig();
      newParam.setName(param.getName());
      newParam.setValue(param.getValue());
      clone.addLayoutParam(newParam);
    }
    return clone;
  }


  /**
   * Creates a deep clone of the given category.
   * @param parent Project to which cloned category will eventually belong.
   * Only appenders in the source category that also belong to the parent
   * will be associated with the cloned category.
   * @param source Category to clone.
   * @return Cloned category.
   */
  public static CategoryConfig cloneCategory(
    final ProjectConfig parent,
    final CategoryConfig source)
  {
    final CategoryConfig clone = new CategoryConfig();
    clone.setName(source.getName());
    clone.setLevel(source.getLevel());
    for (AppenderConfig appender : source.getAppenders()) {
      final AppenderConfig appenderRef = parent.getAppender(appender.getName());
      if (appenderRef != null) {
        clone.getAppenders().add(appenderRef);
      }
    }
    return clone;
  }


  /**
   * Creates a deep clone of the given client.
   * @param source Client to clone.
   * @return Cloned client.
   */
  public static ClientConfig cloneClient(final ClientConfig source)
  {
    final ClientConfig clone = new ClientConfig();
    clone.setName(source.getName());
    return clone;
  }


  /**
   * Creates a deep clone of the given project.  All fields except
   * modifiedDate are cloned; the modified date is set to the current
   * system date/time.
   * @param source Project to clone.
   * @return Cloned project.
   */
  public static ProjectConfig cloneProject(final ProjectConfig source)
  {
    final ProjectConfig clone = new ProjectConfig();
    clone.setName(source.getName());
    clone.setClientLogDir(source.getClientLogDir());
    for (AppenderConfig appender : source.getAppenders()) {
      clone.addAppender(cloneAppender(appender));
    }
    for (CategoryConfig category : source.getCategories()) {
      clone.addCategory(cloneCategory(clone, category));
    }
    return clone;
  }


  /**
   * Filter a view name containing placeholders for values that
   * need to be replaced by data from a project.
   * @param viewName View name to filter.
   * @param project Project configuration.
   * @return New view name with placeholders replaced.
   */
  public static String filterViewName(final String viewName,
      final ProjectConfig project)
  {
    return viewName.replace("@PROJECT_NAME@", project.getName());
  }


  /**
   * Create a permission configuration containing all permissions for the
   * given security identifier.
   * @param sid Security identifier; either a username or role name.
   * @return Permission config with all permissions set for given SID.
   */
  public static PermissionConfig createAllPermissions(final String sid)
  {
    final PermissionConfig perm = new PermissionConfig();
    perm.setName(sid);
    int permBits = 0;
    for (Permission p : PermissionConfig.ALL_PERMISSIONS) {
      permBits |= p.getMask();
    }
    perm.setPermissionBits(permBits);
    return perm;
  }
  

  /**
   * Determines whether the given permission is the last full permission in
   * the given project.
   * @param project Project to test.
   * @param permissionId ID of permission to check.
   * @return True if given permission is last full permission in the given
   * project.
   */
  public static boolean isLastFullPermissions(
    final ProjectConfig project,
    final int permissionId)
  {
    int count = 0;
    int fullPermissionId = 0;
    for (PermissionConfig perm : project.getPermissions()) {
      if (perm.hasPermission(BasePermission.READ) &&
          perm.hasPermission(BasePermission.WRITE) &&
          perm.hasPermission(BasePermission.DELETE))
      {
        count++;
        fullPermissionId = perm.getId();
      }
    }
    return count <= 1 && fullPermissionId == permissionId;
  }
}
