/*
  $Id: ProjectAcl.java 78 2009-03-24 20:33:52Z marvin.addison $

  Copyright (C) 2008-2009 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware
  Email:   middleware@vt.edu
  Version: $Revision: 78 $
  Updated: $Date: 2009-03-24 13:33:52 -0700 (Tue, 24 Mar 2009) $
*/
package edu.vt.middleware.gator.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.acls.AccessControlEntry;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.UnloadedSidException;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;

import edu.vt.middleware.gator.PermissionConfig;
import edu.vt.middleware.gator.ProjectConfig;

/**
 * Simple ACL implementation that does not support ACL inheritance,
 * auditing, or logging.
 *
 * @author Middleware
 * @version $Revision: 78 $
 *
 */
public class ProjectAcl implements Acl
{
  /** ProjectAcl.java */
  private static final long serialVersionUID = 7764134389962328091L;


  private final AccessControlEntry[] entries;
  
  private final ObjectIdentity objectIdentity;

  /**
   * Creates a simple ACL containing the given access control entries that
   * apply to the given project configuration object.
   * @param object Project configuration object.
   */
  public ProjectAcl(final ProjectConfig object)
  {
    objectIdentity = new ObjectIdentityImpl(ProjectConfig.class, object);
    final List<AccessControlEntry> entryList = 
      new ArrayList<AccessControlEntry>();
    for (PermissionConfig perm : object.getPermissions()) {
      entryList.addAll(getAces(perm));
    }
    entries = new AccessControlEntry[entryList.size()];
    entryList.toArray(entries);
  }

  /** {@inheritDoc} */
  public AccessControlEntry[] getEntries()
  {
    return entries;
  }

  /** {@inheritDoc} */
  public ObjectIdentity getObjectIdentity()
  {
    return objectIdentity;
  }

  /**
   * Ownership is not supported.
   * @return null to indicate ownership not supported.
   */
  public Sid getOwner()
  {
    return null;
  }

  /**
   * ACL inheritance is not supported
   * @return null
   */
  public Acl getParentAcl()
  {
    return null;
  }

  /**
   * ACL inheritance is not supported
   * @return False in all cases.
   */
  public boolean isEntriesInheriting()
  {
    return false;
  }

  /** {@inheritDoc} */
  public boolean isGranted(
    final Permission[] permission,
    final Sid[] sids,
    final boolean administrativeMode)
    throws NotFoundException, UnloadedSidException
  {
    for (AccessControlEntry ace : entries) {
      for (Sid sid : sids) {
        if (ace.getSid().equals(sid)) {
          // Exit on first matching permission
          for (Permission perm : permission) {
            if (ace.getPermission().equals(perm)) {
              return ace.isGranting();
            }
          }
        }
      }
    }
    // Implicit deny
    return false;
  }

  /**
   * All SIDs are loaded by default, so this method always returns true.
   * @return true
   */
  public boolean isSidLoaded(final Sid[] sids)
  {
    return true;
  }
 
  /**
   * Gets the access control entries for the given permission
   * configuration object.
   * @param perm Permission configuration object.
   * @return List of access control entries.
   */
  private List<AccessControlEntry> getAces(final PermissionConfig perm)
  {
    final List<AccessControlEntry> aceList =
      new ArrayList<AccessControlEntry>();
    for (Permission p : PermissionConfig.ALL_PERMISSIONS) {
      int result = p.getMask() & perm.getPermissionBits();
      if (result > 0) {
        Sid sid = null;
        if (perm.getName().startsWith("ROLE_")) {
          sid = new GrantedAuthoritySid(perm.getName());
        } else {
          sid = new PrincipalSid(perm.getName());
        }
        aceList.add(
          new AccessControlEntryImpl(
            perm.getProject(),
            this,
            sid,
            p,
            true,
            false,
            false));
      }
    }
    return aceList;
  }
}
/*
  $Id: ProjectAcl.java 66 2009-03-23 15:57:08Z marvin.addison $

  Copyright (C) 2008-2009 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware
  Email:   middleware@vt.edu
  Version: $Revision: 66 $
  Updated: $Date: 2009-03-23 08:57:08 -0700 (Mon, 23 Mar 2009) $
*/
package edu.vt.middleware.gator.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.acls.AccessControlEntry;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.UnloadedSidException;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;

import edu.vt.middleware.gator.PermissionConfig;
import edu.vt.middleware.gator.ProjectConfig;

/**
 * Simple ACL implementation that does not support ACL inheritance,
 * auditing, or logging.
 *
 * @author Middleware
 * @version $Revision: 66 $
 *
 */
public class ProjectAcl implements Acl
{
  /** ProjectAcl.java */
  private static final long serialVersionUID = 7764134389962328091L;


  private final AccessControlEntry[] entries;
  
  private final ObjectIdentity objectIdentity;

  /**
   * Creates a simple ACL containing the given access control entries that
   * apply to the given project configuration object.
   * @param object Project configuration object.
   */
  public ProjectAcl(final ProjectConfig object)
  {
    objectIdentity = new ObjectIdentityImpl(ProjectConfig.class, object);
    final List<AccessControlEntry> entryList = 
      new ArrayList<AccessControlEntry>();
    for (PermissionConfig perm : object.getPermissions()) {
      entryList.addAll(getAces(perm));
    }
    entries = new AccessControlEntry[entryList.size()];
    entryList.toArray(entries);
  }

  /** {@inheritDoc} */
  public AccessControlEntry[] getEntries()
  {
    return entries;
  }

  /** {@inheritDoc} */
  public ObjectIdentity getObjectIdentity()
  {
    return objectIdentity;
  }

  /**
   * Ownership is not supported.
   * @return null to indicate ownership not supported.
   */
  public Sid getOwner()
  {
    return null;
  }

  /**
   * ACL inheritance is not supported
   * @return null
   */
  public Acl getParentAcl()
  {
    return null;
  }

  /**
   * ACL inheritance is not supported
   * @return False in all cases.
   */
  public boolean isEntriesInheriting()
  {
    return false;
  }

  /** {@inheritDoc} */
  public boolean isGranted(
    final Permission[] permission,
    final Sid[] sids,
    final boolean administrativeMode)
    throws NotFoundException, UnloadedSidException
  {
    for (AccessControlEntry ace : entries) {
      for (Sid sid : sids) {
        if (ace.getSid().equals(sid)) {
          // Exit on first matching permission
          for (Permission perm : permission) {
            if (ace.getPermission().equals(perm)) {
              return ace.isGranting();
            }
          }
        }
      }
    }
    // Implicit deny
    return false;
  }

  /**
   * All SIDs are loaded by default, so this method always returns true.
   * @return true
   */
  public boolean isSidLoaded(final Sid[] sids)
  {
    return true;
  }
 
  /**
   * Gets the access control entries for the given permission
   * configuration object.
   * @param perm Permission configuration object.
   * @return List of access control entries.
   */
  private List<AccessControlEntry> getAces(final PermissionConfig perm)
  {
    final List<AccessControlEntry> aceList =
      new ArrayList<AccessControlEntry>();
    for (Permission p : PermissionConfig.ALL_PERMISSIONS) {
      int result = p.getMask() & perm.getPermissionBits();
      if (result > 0) {
        Sid sid = null;
        if (perm.getName().startsWith("ROLE_")) {
          sid = new GrantedAuthoritySid(perm.getName());
        } else {
          sid = new PrincipalSid(perm.getName());
        }
        aceList.add(
          new AccessControlEntryImpl(
            perm.getProject(),
            this,
            sid,
            p,
            true,
            false,
            false));
      }
    }
    return aceList;
  }
}
