/*
  $Id: AuthenticatorCli.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.dsml.Dsmlv1;
import edu.vt.middleware.ldap.dsml.Dsmlv2;
import edu.vt.middleware.ldap.ldif.Ldif;
import edu.vt.middleware.ldap.props.PropertyInvoker;
import org.apache.commons.cli.CommandLine;

/**
 * Command line interface for authenticator operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $
 */
public class AuthenticatorCli extends AbstractCli
{

  /** Name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapauth";


  /**
   * CLI entry point method.
   *
   * @param  args  Command line arguments.
   */
  public static void main(final String[] args)
  {
    new AuthenticatorCli().performAction(args);
  }


  /** {@inheritDoc}. */
  protected void initOptions()
  {
    super.initOptions(
      new PropertyInvoker(
        AuthenticatorConfig.class,
        AuthenticatorConfig.PROPERTIES_DOMAIN));
  }


  /**
   * Initialize an AuthenticatorConfig with command line options.
   *
   * @param  line  Parsed command line arguments container.
   *
   * @return  <code>AuthenticatorConfig</code> that has been initialized
   *
   * @throws  Exception  On errors thrown by handler.
   */
  protected AuthenticatorConfig initAuthenticatorConfig(final CommandLine line)
    throws Exception
  {
    final AuthenticatorConfig config = new AuthenticatorConfig();
    this.initLdapProperties(config, line);
    if (line.hasOption(OPT_TRACE)) {
      config.setTracePackets(System.out);
    }
    if (
      config.getServiceUser() != null &&
        config.getServiceCredential() == null) {
      // prompt the user to enter a password
      System.out.print(
        "Enter password for service user " + config.getServiceUser() + ": ");

      final String pass = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setServiceCredential(pass);
    }
    if (config.getUser() == null) {
      // prompt for a user name
      System.out.print("Enter user name: ");

      final String user = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setUser(user);
    }
    if (config.getCredential() == null) {
      // prompt the user to enter a password
      System.out.print("Enter password for user " + config.getUser() + ": ");

      final String pass = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setCredential(pass);
    }
    return config;
  }


  /** {@inheritDoc}. */
  protected void dispatch(final CommandLine line)
    throws Exception
  {
    if (line.hasOption(OPT_DSMLV1)) {
      this.outputDsmlv1 = true;
    } else if (line.hasOption(OPT_DSMLV2)) {
      this.outputDsmlv2 = true;
    }
    if (line.hasOption(OPT_HELP)) {
      printHelp();
    } else {
      authenticate(initAuthenticatorConfig(line), line.getArgs());
    }
  }


  /**
   * Executes the authenticate operation.
   *
   * @param  config  Authenticator configuration.
   * @param  attrs  Ldap attributes to return
   *
   * @throws  Exception  On errors.
   */
  protected void authenticate(
    final AuthenticatorConfig config,
    final String[] attrs)
    throws Exception
  {
    final Authenticator auth = new Authenticator();
    auth.setAuthenticatorConfig(config);

    Attributes results = null;
    try {
      if (attrs == null || attrs.length == 0) {
        results = auth.authenticate(null);
      } else {
        results = auth.authenticate(attrs);
      }
      if (results != null && results.size() > 0) {
        final LdapEntry entry = new LdapEntry();
        entry.setDn(auth.getDn(config.getUser()));
        entry.setLdapAttributes(new LdapAttributes(results));
        if (this.outputDsmlv1) {
          (new Dsmlv1()).outputDsml(entry.toSearchResult(), System.out);
        } else if (this.outputDsmlv2) {
          (new Dsmlv2()).outputDsml(entry.toSearchResult(), System.out);
        } else {
          (new Ldif()).outputLdif(entry.toSearchResult(), System.out);
        }
      }
    } finally {
      if (auth != null) {
        auth.close();
      }
    }
  }


  /** {@inheritDoc}. */
  protected String getCommandName()
  {
    return COMMAND_NAME;
  }
}
/*
  $Id: AuthenticatorCli.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.dsml.Dsmlv1;
import edu.vt.middleware.ldap.dsml.Dsmlv2;
import edu.vt.middleware.ldap.ldif.Ldif;
import edu.vt.middleware.ldap.props.PropertyInvoker;
import org.apache.commons.cli.CommandLine;

/**
 * Command line interface for authenticator operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $
 */
public class AuthenticatorCli extends AbstractCli
{

  /** Name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapauth";


  /**
   * CLI entry point method.
   *
   * @param  args  Command line arguments.
   */
  public static void main(final String[] args)
  {
    new AuthenticatorCli().performAction(args);
  }


  /** {@inheritDoc}. */
  protected void initOptions()
  {
    super.initOptions(
      new PropertyInvoker(
        AuthenticatorConfig.class,
        AuthenticatorConfig.PROPERTIES_DOMAIN));
  }


  /**
   * Initialize an AuthenticatorConfig with command line options.
   *
   * @param  line  Parsed command line arguments container.
   *
   * @return  <code>AuthenticatorConfig</code> that has been initialized
   *
   * @throws  Exception  On errors thrown by handler.
   */
  protected AuthenticatorConfig initAuthenticatorConfig(final CommandLine line)
    throws Exception
  {
    final AuthenticatorConfig config = new AuthenticatorConfig();
    this.initLdapProperties(config, line);
    if (line.hasOption(OPT_TRACE)) {
      config.setTracePackets(System.out);
    }
    if (
      config.getServiceUser() != null &&
        config.getServiceCredential() == null) {
      // prompt the user to enter a password
      System.out.print(
        "Enter password for service user " + config.getServiceUser() + ": ");

      final String pass = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setServiceCredential(pass);
    }
    if (config.getUser() == null) {
      // prompt for a user name
      System.out.print("Enter user name: ");

      final String user = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setUser(user);
    }
    if (config.getCredential() == null) {
      // prompt the user to enter a password
      System.out.print("Enter password for user " + config.getUser() + ": ");

      final String pass = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setCredential(pass);
    }
    return config;
  }


  /** {@inheritDoc}. */
  protected void dispatch(final CommandLine line)
    throws Exception
  {
    if (line.hasOption(OPT_DSMLV1)) {
      this.outputDsmlv1 = true;
    } else if (line.hasOption(OPT_DSMLV2)) {
      this.outputDsmlv2 = true;
    }
    if (line.hasOption(OPT_HELP)) {
      printHelp();
    } else {
      authenticate(initAuthenticatorConfig(line), line.getArgs());
    }
  }


  /**
   * Executes the authenticate operation.
   *
   * @param  config  Authenticator configuration.
   * @param  attrs  Ldap attributes to return
   *
   * @throws  Exception  On errors.
   */
  protected void authenticate(
    final AuthenticatorConfig config,
    final String[] attrs)
    throws Exception
  {
    final Authenticator auth = new Authenticator();
    auth.setAuthenticatorConfig(config);

    Attributes results = null;
    try {
      if (attrs == null || attrs.length == 0) {
        results = auth.authenticate(null);
      } else {
        results = auth.authenticate(attrs);
      }
      if (results != null && results.size() > 0) {
        final LdapEntry entry = new LdapEntry();
        entry.setDn(auth.getDn(config.getUser()));
        entry.setLdapAttributes(new LdapAttributes(results));
        if (this.outputDsmlv1) {
          (new Dsmlv1()).outputDsml(entry.toSearchResult(), System.out);
        } else if (this.outputDsmlv2) {
          (new Dsmlv2()).outputDsml(entry.toSearchResult(), System.out);
        } else {
          (new Ldif()).outputLdif(entry.toSearchResult(), System.out);
        }
      }
    } finally {
      if (auth != null) {
        auth.close();
      }
    }
  }


  /** {@inheritDoc}. */
  protected String getCommandName()
  {
    return COMMAND_NAME;
  }
}
/*
  $Id: AuthenticatorCli.java 183 2009-05-06 02:45:57Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 183 $
  Updated: $Date: 2009-05-05 19:45:57 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.dsml.Dsmlv1;
import edu.vt.middleware.ldap.dsml.Dsmlv2;
import edu.vt.middleware.ldap.ldif.Ldif;
import edu.vt.middleware.ldap.props.PropertyInvoker;
import org.apache.commons.cli.CommandLine;

/**
 * Command line interface for authenticator operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 183 $
 */
public class AuthenticatorCli extends AbstractCli
{

  /** Name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapauth";


  /**
   * CLI entry point method.
   *
   * @param  args  Command line arguments.
   */
  public static void main(final String[] args)
  {
    new AuthenticatorCli().performAction(args);
  }


  /** {@inheritDoc}. */
  protected void initOptions()
  {
    super.initOptions(
      new PropertyInvoker(
        AuthenticatorConfig.class,
        AuthenticatorConfig.PROPERTIES_DOMAIN));
  }


  /**
   * Initialize an AuthenticatorConfig with command line options.
   *
   * @param  line  Parsed command line arguments container.
   *
   * @return  <code>AuthenticatorConfig</code> that has been initialized
   *
   * @throws  Exception  On errors thrown by handler.
   */
  protected AuthenticatorConfig initAuthenticatorConfig(final CommandLine line)
    throws Exception
  {
    final AuthenticatorConfig config = new AuthenticatorConfig();
    this.initLdapProperties(config, line);
    if (line.hasOption(OPT_TRACE)) {
      config.setTracePackets(System.out);
    }
    if (
      config.getServiceUser() != null &&
        config.getServiceCredential() == null) {
      // prompt the user to enter a password
      System.out.print(
        "Enter password for service user " + config.getServiceUser() + ": ");

      final String pass = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setServiceCredential(pass);
    }
    if (config.getUser() == null) {
      // prompt for a user name
      System.out.print("Enter user name: ");

      final String user = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setUser(user);
    }
    if (config.getCredential() == null) {
      // prompt the user to enter a password
      System.out.print("Enter password for user " + config.getUser() + ": ");

      final String pass = (new BufferedReader(new InputStreamReader(System.in)))
          .readLine();
      config.setCredential(pass);
    }
    return config;
  }


  /** {@inheritDoc}. */
  protected void dispatch(final CommandLine line)
    throws Exception
  {
    if (line.hasOption(OPT_DSMLV1)) {
      this.outputDsmlv1 = true;
    } else if (line.hasOption(OPT_DSMLV2)) {
      this.outputDsmlv2 = true;
    }
    if (line.hasOption(OPT_HELP)) {
      printHelp();
    } else {
      authenticate(initAuthenticatorConfig(line), line.getArgs());
    }
  }


  /**
   * Executes the authenticate operation.
   *
   * @param  config  Authenticator configuration.
   * @param  attrs  Ldap attributes to return
   *
   * @throws  Exception  On errors.
   */
  protected void authenticate(
    final AuthenticatorConfig config,
    final String[] attrs)
    throws Exception
  {
    final Authenticator auth = new Authenticator();
    auth.setAuthenticatorConfig(config);

    Attributes results = null;
    try {
      if (attrs == null || attrs.length == 0) {
        results = auth.authenticate(null);
      } else {
        results = auth.authenticate(attrs);
      }
      if (results != null && results.size() > 0) {
        final LdapEntry entry = new LdapEntry();
        entry.setDn(auth.getDn(config.getUser()));
        entry.setLdapAttributes(new LdapAttributes(results));
        if (this.outputDsmlv1) {
          (new Dsmlv1()).outputDsml(entry.toSearchResult(), System.out);
        } else if (this.outputDsmlv2) {
          (new Dsmlv2()).outputDsml(entry.toSearchResult(), System.out);
        } else {
          (new Ldif()).outputLdif(entry.toSearchResult(), System.out);
        }
      }
    } finally {
      if (auth != null) {
        auth.close();
      }
    }
  }


  /** {@inheritDoc}. */
  protected String getCommandName()
  {
    return COMMAND_NAME;
  }
}
