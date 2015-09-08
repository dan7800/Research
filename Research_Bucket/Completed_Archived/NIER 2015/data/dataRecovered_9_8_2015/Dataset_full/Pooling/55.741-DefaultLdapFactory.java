/*
  $Id: DefaultLdapFactory.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;

/**
 * <code>DefaultLdapFactory</code> provides a simple implementation of a ldap
 * factory. Uses {@link ConnectLdapValidator} by default.
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $ $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
 */
public class DefaultLdapFactory extends AbstractLdapFactory<Ldap>
{

  /** Ldap config to create ldap objects with. */
  private LdapConfig config;

  /** Whether to connect to the ldap on object creation. */
  private boolean connectOnCreate = true;


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the default
   * properties file, which must be located in your classpath.
   */
  public DefaultLdapFactory()
  {
    this.config = LdapConfig.createFromProperties(null);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied
   * properties file, which must be located in your classpath.
   *
   * @param  propertiesFile  <code>String</code>
   */
  public DefaultLdapFactory(final String propertiesFile)
  {
    this.config = LdapConfig.createFromProperties(propertiesFile);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied ldap
   * configuration. The ldap configuration will be marked as immutable by this
   * factory.
   *
   * @param  lc  ldap config
   */
  public DefaultLdapFactory(final LdapConfig lc)
  {
    this.config = lc;
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * Returns whether ldap objects will attempt to connect after creation.
   * Default is true.
   *
   * @return  <code>boolean</code>
   */
  public boolean getConnectOnCreate()
  {
    return this.connectOnCreate;
  }


  /**
   * This sets whether newly created ldap objects will attempt to connect.
   * Default is true.
   *
   * @param  b  connect on create
   */
  public void setConnectOnCreate(final boolean b)
  {
    this.connectOnCreate = b;
  }


  /** {@inheritDoc}. */
  public Ldap create()
  {
    Ldap l = new Ldap(this.config);
    if (this.connectOnCreate) {
      try {
        l.connect();
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("unabled to connect to the ldap", e);
        }
        l = null;
      }
    }
    return l;
  }


  /** {@inheritDoc}. */
  public void destroy(final Ldap l)
  {
    l.close();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("destroyed ldap object: " + l);
    }
  }
}
/*
  $Id: DefaultLdapFactory.java 358 2009-07-24 16:11:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 358 $
  Updated: $Date: 2009-07-24 09:11:31 -0700 (Fri, 24 Jul 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.io.InputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;

/**
 * <code>DefaultLdapFactory</code> provides a simple implementation of a ldap
 * factory. Uses {@link ConnectLdapValidator} by default.
 *
 * @author  Middleware Services
 * @version  $Revision: 358 $ $Date: 2009-07-24 09:11:31 -0700 (Fri, 24 Jul 2009) $
 */
public class DefaultLdapFactory extends AbstractLdapFactory<Ldap>
{

  /** Ldap config to create ldap objects with. */
  private LdapConfig config;

  /** Whether to connect to the ldap on object creation. */
  private boolean connectOnCreate = true;


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the default
   * properties file, which must be located in your classpath.
   */
  public DefaultLdapFactory()
  {
    this.config = LdapConfig.createFromProperties(null);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied input
   * stream.
   *
   * @param  is  <code>InputStream</code>
   */
  public DefaultLdapFactory(final InputStream is)
  {
    this.config = LdapConfig.createFromProperties(is);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied ldap
   * configuration. The ldap configuration will be marked as immutable by this
   * factory.
   *
   * @param  lc  ldap config
   */
  public DefaultLdapFactory(final LdapConfig lc)
  {
    this.config = lc;
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * Returns whether ldap objects will attempt to connect after creation.
   * Default is true.
   *
   * @return  <code>boolean</code>
   */
  public boolean getConnectOnCreate()
  {
    return this.connectOnCreate;
  }


  /**
   * This sets whether newly created ldap objects will attempt to connect.
   * Default is true.
   *
   * @param  b  connect on create
   */
  public void setConnectOnCreate(final boolean b)
  {
    this.connectOnCreate = b;
  }


  /** {@inheritDoc}. */
  public Ldap create()
  {
    Ldap l = new Ldap(this.config);
    if (this.connectOnCreate) {
      try {
        l.connect();
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("unabled to connect to the ldap", e);
        }
        l = null;
      }
    }
    return l;
  }


  /** {@inheritDoc}. */
  public void destroy(final Ldap l)
  {
    l.close();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("destroyed ldap object: " + l);
    }
  }
}
/*
  $Id: DefaultLdapFactory.java 358 2009-07-24 16:11:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 358 $
  Updated: $Date: 2009-07-24 09:11:31 -0700 (Fri, 24 Jul 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.io.InputStream;
import javax.naming.NamingException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;

/**
 * <code>DefaultLdapFactory</code> provides a simple implementation of a ldap
 * factory. Uses {@link ConnectLdapValidator} by default.
 *
 * @author  Middleware Services
 * @version  $Revision: 358 $ $Date: 2009-07-24 09:11:31 -0700 (Fri, 24 Jul 2009) $
 */
public class DefaultLdapFactory extends AbstractLdapFactory<Ldap>
{

  /** Ldap config to create ldap objects with. */
  private LdapConfig config;

  /** Whether to connect to the ldap on object creation. */
  private boolean connectOnCreate = true;


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the default
   * properties file, which must be located in your classpath.
   */
  public DefaultLdapFactory()
  {
    this.config = LdapConfig.createFromProperties(null);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied input
   * stream.
   *
   * @param  is  <code>InputStream</code>
   */
  public DefaultLdapFactory(final InputStream is)
  {
    this.config = LdapConfig.createFromProperties(is);
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * This creates a new <code>DefaultLdapFactory</code> with the supplied ldap
   * configuration. The ldap configuration will be marked as immutable by this
   * factory.
   *
   * @param  lc  ldap config
   */
  public DefaultLdapFactory(final LdapConfig lc)
  {
    this.config = lc;
    this.config.makeImmutable();
    this.validator = new ConnectLdapValidator();
  }


  /**
   * Returns whether ldap objects will attempt to connect after creation.
   * Default is true.
   *
   * @return  <code>boolean</code>
   */
  public boolean getConnectOnCreate()
  {
    return this.connectOnCreate;
  }


  /**
   * This sets whether newly created ldap objects will attempt to connect.
   * Default is true.
   *
   * @param  b  connect on create
   */
  public void setConnectOnCreate(final boolean b)
  {
    this.connectOnCreate = b;
  }


  /** {@inheritDoc}. */
  public Ldap create()
  {
    Ldap l = new Ldap(this.config);
    if (this.connectOnCreate) {
      try {
        l.connect();
      } catch (NamingException e) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("unabled to connect to the ldap", e);
        }
        l = null;
      }
    }
    return l;
  }


  /** {@inheritDoc}. */
  public void destroy(final Ldap l)
  {
    l.close();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("destroyed ldap object: " + l);
    }
  }
}
