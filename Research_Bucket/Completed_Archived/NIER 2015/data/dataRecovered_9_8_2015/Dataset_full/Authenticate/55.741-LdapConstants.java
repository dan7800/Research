/*
  $Id: LdapConstants.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap;

/**
 * <code>LdapConstants</code> contains all the constants needed for creating a
 * <code>Ldap</code>. See
 * http://java.sun.com/j2se/1.4.2/docs/guide/jndi/jndi-ldap.html or
 * http://java.sun.com/j2se/1.4.2/docs/guide/jndi/spec/jndi/properties.html for
 * more information on JNDI properties.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $ $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
 */
public final class LdapConstants
{

  /**
   * The value of this property is a fully qualified class name of the factory
   * class which creates the initial context for the LDAP service provider. The
   * value of this constant is {@value}.
   */
  public static final String CONTEXT_FACTORY = "java.naming.factory.initial";

  /**
   * The value of this property is a string identifying the class name of a
   * socket factory. The value of this constant is {@value}.
   */
  public static final String SOCKET_FACTORY = "java.naming.ldap.factory.socket";

  /**
   * The value of this property is a string specifying the authoritativeness of
   * the service requested. The value of this constant is {@value}.
   */
  public static final String AUTHORITATIVE = "java.naming.authoritative";

  /**
   * The value of this property is a java.io.OutputStream object into which a
   * hexadecimal dump of the incoming and outgoing LDAP ASN.1 BER packets is
   * written. The value of this constant is {@value}.
   */
  public static final String TRACE = "com.sun.jndi.ldap.trace.ber";

  /**
   * The value of this property is a string that specifies the authentication
   * mechanism(s) for the provider to use. The value of this constant is {@value
   * }.
   */
  public static final String AUTHENTICATION =
    "java.naming.security.authentication";

  /**
   * The value of this property is a string that specifies the identity of the
   * principal to be authenticated. The value of this constant is {@value}.
   */
  public static final String PRINCIPAL = "java.naming.security.principal";

  /**
   * The value of this property is an object that specifies the credentials of
   * the principal to be authenticated. The value of this constant is {@value}.
   */
  public static final String CREDENTIALS = "java.naming.security.credentials";

  /**
   * The value of this property is a string of decimal digits that specifies the
   * batch size of search results returned by the server. The value of this
   * constant is {@value}.
   */
  public static final String BATCH_SIZE = "java.naming.batchsize";

  /**
   * The value of this property is a string that specifies the DNS host and
   * domain names. The value of this constant is {@value}.
   */
  public static final String DNS_URL = "java.naming.dns.url";

  /**
   * The value of this property is a string language tag according to RFC 1766.
   * The value of this constant is {@value}.
   */
  public static final String LANGUAGE = "java.naming.language";

  /**
   * The value of this property is a string that specifies how referrals shall
   * be handled by the provider. The value of this constant is {@value}.
   */
  public static final String REFERRAL = "java.naming.referral";

  /**
   * The value of this property is a string that specifies how aliases shall be
   * handled by the provider. The value of this constant is {@value}.
   */
  public static final String DEREF_ALIASES = "java.naming.ldap.derefAliases";

  /**
   * The value of this property is a string that specifies additional binary
   * attributes. The value of this constant is {@value}.
   */
  public static final String BINARY_ATTRIBUTES =
    "java.naming.ldap.attributes.binary";

  /**
   * The value of this property is a string that specifies a SASL authorization
   * id. The value of this constant is {@value}.
   */
  public static final String SASL_AUTHORIZATION_ID =
    "java.naming.security.sasl.authorizationId";

  /**
   * The value of this property is a string that specifies a SASL realm. The
   * value of this constant is {@value}.
   */
  public static final String SASL_REALM = "java.naming.security.sasl.realm";

  /**
   * The value of this property is a string that specifies to only return
   * attribute type names, no values. The value of this constant is {@value}.
   */
  public static final String TYPES_ONLY = "java.naming.ldap.typesOnly";

  /**
   * The value of this property is a string that specifies the security protocol
   * for the provider to use. The value of this constant is {@value}.
   */
  public static final String PROTOCOL = "java.naming.security.protocol";

  /**
   * The value of this property is a string that specifies the protocol version
   * for the provider. The value of this constant is {@value}.
   */
  public static final String VERSION = "java.naming.ldap.version";

  /**
   * The value of this property is a URL string that specifies the hostname and
   * port number of the LDAP server, and the root distinguished name of the
   * naming context to use. The value of this constant is {@value}.
   */
  public static final String PROVIDER_URL = "java.naming.provider.url";

  /**
   * The value of this property is a string that specifies the time in
   * milliseconds that a connection attempt will abort if the connection cannot
   * be made. The value of this constant is {@value}.
   */
  public static final String TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

  /**
   * Value passed to PROTOCOL to use SSL.
   * The value of this constant is {@value}.
   */
  public static final String SSL_PROTOCOL = "ssl";

  /**
   * Value passed to AUTHENTICATION to use simple authentication. The value of
   * this constant is {@value}.
   */
  public static final String SIMPLE_AUTHTYPE = "simple";

  /**
   * Value passed to AUTHENTICATION to use simple authentication. The value of
   * this constant is {@value}.
   */
  public static final String STRONG_AUTHTYPE = "strong";

  /**
   * Value passed to AUTHENTICATION to use none authentication The value of this
   * constant is {@value}.
   */
  public static final String NONE_AUTHTYPE = "none";

  /**
   * Value passed to VERSION to use ldap version 3 controls The value of this
   * constant is {@value}.
   */
  public static final String VERSION_THREE = "3";

  /** Ldap scheme, the value of this constant is {@value}. */
  public static final String PROVIDER_URL_SCHEME = "ldap";

  /** Secure ldap scheme, the value of this constant is {@value}. */
  public static final String PROVIDER_URL_SSL_SCHEME = "ldaps";

  /**
   * URL prefix used for constructing URLs. The value of this constant is
   * {@value}.
   */
  public static final String PROVIDER_URL_PREFIX = "://";

  /**
   * URL separator used for constructing URLs. The value of this constant is
   * {@value}.
   */
  public static final String PROVIDER_URL_SEPARATOR = ":";

  /**
   * Ldap command which returns a list of supported SASL mechanisms. The value
   * of this constant is {@value}.
   */
  public static final String SUPPORTED_SASL_MECHANISMS =
    "supportedSASLMechanisms";

  /**
   * Ldap command which returns a list of supported controls. The value of this
   * constant is {@value}.
   */
  public static final String SUPPORTED_CONTROL = "supportedcontrol";

  /**
   * Value passed to AUTHENTICATION to use SASL authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_EXTERNAL = "EXTERNAL";

  /**
   * Value passed to AUTHENTICATION to use DIGEST-MD5 authentication. The value
   * of this constant is {@value}.
   */
  public static final String SASL_MECHANISM_DIGEST_MD5 = "DIGEST-MD5";

  /**
   * Value passed to AUTHENTICATION to use CRAM-MD5 authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_CRAM_MD5 = "CRAM-MD5";

  /**
   * Value passed to AUTHENTICATION to use GSS-API authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_GSS_API = "GSSAPI";

  /** List of supported SASL Mechanisms. */
  public static final String[] SASL_MECHANISMS = new String[] {
    SASL_MECHANISM_EXTERNAL,
    SASL_MECHANISM_DIGEST_MD5,
    SASL_MECHANISM_CRAM_MD5,
    SASL_MECHANISM_GSS_API,
  };

  /** Default context factory, value of this constant is {@value}. */
  public static final String DEFAULT_CONTEXT_FACTORY =
    "com.sun.jndi.ldap.LdapCtxFactory";

  /** Default base DN, value of this constant is {@value}. */
  public static final String DEFAULT_BASE_DN = "";

  /**
   * Default timeout, -1 means use provider setting. The value of this constant
   * is {@value}.
   */
  public static final int DEFAULT_TIMEOUT = -1;

  /** Default authentication type, the value of this constant is {@value}. */
  public static final String DEFAULT_AUTHTYPE = SIMPLE_AUTHTYPE;

  /**
   * Default time limit, 0 means wait indefinitely. The value of this constant
   * is {@value}.
   */
  public static final int DEFAULT_TIME_LIMIT = 0;

  /**
   * Default count limit, 0 means return all results. The value of this constant
   * is {@value}.
   */
  public static final long DEFAULT_COUNT_LIMIT = 0;

  /**
   * Default batch size, -1 means use provider setting. The value of this
   * constant is {@value}.
   */
  public static final int DEFAULT_BATCH_SIZE = -1;

  /** Default authoritative value, the value of this constant is {@value}. */
  public static final boolean DEFAULT_AUTHORITATIVE = false;

  /** Default type only value, the value of this constant is {@value}. */
  public static final boolean DEFAULT_TYPES_ONLY = false;

  /** Default ignore case value, value of this constant is {@value}. */
  public static final boolean DEFAULT_IGNORE_CASE = true;

  /** Default ldap port, the value of this constant is {@value}. */
  public static final String DEFAULT_PORT = "389";

  /** Default ldaps port, the value of this constant is {@value}. */
  public static final String DEFAULT_SSL_PORT = "636";

  /** Whether to use SSL by default, the value of this constant is {@value}. */
  public static final boolean DEFAULT_USE_SSL = false;

  /** Whether to use TLS by default, the value of this constant is {@value}. */
  public static final boolean DEFAULT_USE_TLS = false;

  /**
   * Whether to log authentication credentials. The value of this constant is
   * {@value}.
   */
  public static final boolean DEFAULT_LOG_CREDENTIALS = false;

  /**
   * Default userfield field used by Authenticator. The value of this constant
   * is {@value}.
   */
  public static final String DEFAULT_USER_FIELD = "uid";

  /**
   * Whether Authenticator should construct DNs by default. The value of this
   * constant is {@value}.
   */
  public static final boolean DEFAULT_CONSTRUCT_DN = false;

  /**
   * Default character set for creating strings. The value of this constant is
   * {@value}.
   */
  public static final String DEFAULT_CHARSET = "UTF-8";

  /**
   * Number of times to retry an operation on CommunicationException. The value
   * of this constant is {@value}.
   */
  public static final int OPERATION_RETRY = 1;


  /** Default constructor. */
  private LdapConstants() {}
}
/*
  $Id: LdapConstants.java 372 2009-07-27 22:44:35Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 372 $
  Updated: $Date: 2009-07-27 15:44:35 -0700 (Mon, 27 Jul 2009) $
*/
package edu.vt.middleware.ldap;

/**
 * <code>LdapConstants</code> contains all the constants needed for creating a
 * <code>Ldap</code>. See
 * http://java.sun.com/j2se/1.4.2/docs/guide/jndi/jndi-ldap.html or
 * http://java.sun.com/j2se/1.4.2/docs/guide/jndi/spec/jndi/properties.html for
 * more information on JNDI properties.
 *
 * @author  Middleware Services
 * @version  $Revision: 372 $ $Date: 2009-07-27 15:44:35 -0700 (Mon, 27 Jul 2009) $
 */
public final class LdapConstants
{

  /**
   * The value of this property is a fully qualified class name of the factory
   * class which creates the initial context for the LDAP service provider. The
   * value of this constant is {@value}.
   */
  public static final String CONTEXT_FACTORY = "java.naming.factory.initial";

  /**
   * The value of this property is a string identifying the class name of a
   * socket factory. The value of this constant is {@value}.
   */
  public static final String SOCKET_FACTORY = "java.naming.ldap.factory.socket";

  /**
   * The value of this property is a string specifying the authoritativeness of
   * the service requested. The value of this constant is {@value}.
   */
  public static final String AUTHORITATIVE = "java.naming.authoritative";

  /**
   * The value of this property is a java.io.OutputStream object into which a
   * hexadecimal dump of the incoming and outgoing LDAP ASN.1 BER packets is
   * written. The value of this constant is {@value}.
   */
  public static final String TRACE = "com.sun.jndi.ldap.trace.ber";

  /**
   * The value of this property is a string that specifies the authentication
   * mechanism(s) for the provider to use. The value of this constant is {@value
   * }.
   */
  public static final String AUTHENTICATION =
    "java.naming.security.authentication";

  /**
   * The value of this property is a string that specifies the identity of the
   * principal to be authenticated. The value of this constant is {@value}.
   */
  public static final String PRINCIPAL = "java.naming.security.principal";

  /**
   * The value of this property is an object that specifies the credentials of
   * the principal to be authenticated. The value of this constant is {@value}.
   */
  public static final String CREDENTIALS = "java.naming.security.credentials";

  /**
   * The value of this property is a string of decimal digits that specifies the
   * batch size of search results returned by the server. The value of this
   * constant is {@value}.
   */
  public static final String BATCH_SIZE = "java.naming.batchsize";

  /**
   * The value of this property is a string that specifies the DNS host and
   * domain names. The value of this constant is {@value}.
   */
  public static final String DNS_URL = "java.naming.dns.url";

  /**
   * The value of this property is a string language tag according to RFC 1766.
   * The value of this constant is {@value}.
   */
  public static final String LANGUAGE = "java.naming.language";

  /**
   * The value of this property is a string that specifies how referrals shall
   * be handled by the provider. The value of this constant is {@value}.
   */
  public static final String REFERRAL = "java.naming.referral";

  /**
   * The value of this property is a string that specifies how aliases shall be
   * handled by the provider. The value of this constant is {@value}.
   */
  public static final String DEREF_ALIASES = "java.naming.ldap.derefAliases";

  /**
   * The value of this property is a string that specifies additional binary
   * attributes. The value of this constant is {@value}.
   */
  public static final String BINARY_ATTRIBUTES =
    "java.naming.ldap.attributes.binary";

  /**
   * The value of this property is a string that specifies a SASL authorization
   * id. The value of this constant is {@value}.
   */
  public static final String SASL_AUTHORIZATION_ID =
    "java.naming.security.sasl.authorizationId";

  /**
   * The value of this property is a string that specifies a SASL realm. The
   * value of this constant is {@value}.
   */
  public static final String SASL_REALM = "java.naming.security.sasl.realm";

  /**
   * The value of this property is a string that specifies to only return
   * attribute type names, no values. The value of this constant is {@value}.
   */
  public static final String TYPES_ONLY = "java.naming.ldap.typesOnly";

  /**
   * The value of this property is a string that specifies the security protocol
   * for the provider to use. The value of this constant is {@value}.
   */
  public static final String PROTOCOL = "java.naming.security.protocol";

  /**
   * The value of this property is a string that specifies the protocol version
   * for the provider. The value of this constant is {@value}.
   */
  public static final String VERSION = "java.naming.ldap.version";

  /**
   * The value of this property is a URL string that specifies the hostname and
   * port number of the LDAP server, and the root distinguished name of the
   * naming context to use. The value of this constant is {@value}.
   */
  public static final String PROVIDER_URL = "java.naming.provider.url";

  /**
   * The value of this property is a string that specifies the time in
   * milliseconds that a connection attempt will abort if the connection cannot
   * be made. The value of this constant is {@value}.
   */
  public static final String TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

  /**
   * Value passed to PROTOCOL to use SSL.
   * The value of this constant is {@value}.
   */
  public static final String SSL_PROTOCOL = "ssl";

  /**
   * Value passed to AUTHENTICATION to use simple authentication. The value of
   * this constant is {@value}.
   */
  public static final String SIMPLE_AUTHTYPE = "simple";

  /**
   * Value passed to AUTHENTICATION to use simple authentication. The value of
   * this constant is {@value}.
   */
  public static final String STRONG_AUTHTYPE = "strong";

  /**
   * Value passed to AUTHENTICATION to use none authentication The value of this
   * constant is {@value}.
   */
  public static final String NONE_AUTHTYPE = "none";

  /**
   * Value passed to VERSION to use ldap version 3 controls The value of this
   * constant is {@value}.
   */
  public static final String VERSION_THREE = "3";

  /** Ldap scheme, the value of this constant is {@value}. */
  public static final String PROVIDER_URL_SCHEME = "ldap";

  /** Secure ldap scheme, the value of this constant is {@value}. */
  public static final String PROVIDER_URL_SSL_SCHEME = "ldaps";

  /**
   * URL prefix used for constructing URLs. The value of this constant is
   * {@value}.
   */
  public static final String PROVIDER_URL_PREFIX = "://";

  /**
   * URL separator used for constructing URLs. The value of this constant is
   * {@value}.
   */
  public static final String PROVIDER_URL_SEPARATOR = ":";

  /**
   * Ldap command which returns a list of supported SASL mechanisms. The value
   * of this constant is {@value}.
   */
  public static final String SUPPORTED_SASL_MECHANISMS =
    "supportedSASLMechanisms";

  /**
   * Ldap command which returns a list of supported controls. The value of this
   * constant is {@value}.
   */
  public static final String SUPPORTED_CONTROL = "supportedcontrol";

  /**
   * Value passed to AUTHENTICATION to use SASL authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_EXTERNAL = "EXTERNAL";

  /**
   * Value passed to AUTHENTICATION to use DIGEST-MD5 authentication. The value
   * of this constant is {@value}.
   */
  public static final String SASL_MECHANISM_DIGEST_MD5 = "DIGEST-MD5";

  /**
   * Value passed to AUTHENTICATION to use CRAM-MD5 authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_CRAM_MD5 = "CRAM-MD5";

  /**
   * Value passed to AUTHENTICATION to use GSS-API authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_GSS_API = "GSSAPI";

  /** List of supported SASL Mechanisms. */
  public static final String[] SASL_MECHANISMS = new String[] {
    SASL_MECHANISM_EXTERNAL,
    SASL_MECHANISM_DIGEST_MD5,
    SASL_MECHANISM_CRAM_MD5,
    SASL_MECHANISM_GSS_API,
  };

  /** Default context factory, value of this constant is {@value}. */
  public static final String DEFAULT_CONTEXT_FACTORY =
    "com.sun.jndi.ldap.LdapCtxFactory";

  /** Default base DN, value of this constant is {@value}. */
  public static final String DEFAULT_BASE_DN = "";

  /**
   * Default timeout, -1 means use provider setting. The value of this constant
   * is {@value}.
   */
  public static final int DEFAULT_TIMEOUT = -1;

  /** Default authentication type, the value of this constant is {@value}. */
  public static final String DEFAULT_AUTHTYPE = SIMPLE_AUTHTYPE;

  /**
   * Default time limit, 0 means wait indefinitely. The value of this constant
   * is {@value}.
   */
  public static final int DEFAULT_TIME_LIMIT = 0;

  /**
   * Default count limit, 0 means return all results. The value of this constant
   * is {@value}.
   */
  public static final long DEFAULT_COUNT_LIMIT = 0;

  /**
   * Default batch size, -1 means use provider setting. The value of this
   * constant is {@value}.
   */
  public static final int DEFAULT_BATCH_SIZE = -1;

  /** Default authoritative value, the value of this constant is {@value}. */
  public static final boolean DEFAULT_AUTHORITATIVE = false;

  /** Default type only value, the value of this constant is {@value}. */
  public static final boolean DEFAULT_TYPES_ONLY = false;

  /** Default ignore case value, value of this constant is {@value}. */
  public static final boolean DEFAULT_IGNORE_CASE = true;

  /** Default ldap port, the value of this constant is {@value}. */
  public static final String DEFAULT_PORT = "389";

  /** Default ldaps port, the value of this constant is {@value}. */
  public static final String DEFAULT_SSL_PORT = "636";

  /** Whether to use SSL by default, the value of this constant is {@value}. */
  public static final boolean DEFAULT_USE_SSL = false;

  /** Whether to use TLS by default, the value of this constant is {@value}. */
  public static final boolean DEFAULT_USE_TLS = false;

  /**
   * Whether to log authentication credentials. The value of this constant is
   * {@value}.
   */
  public static final boolean DEFAULT_LOG_CREDENTIALS = false;

  /**
   * Default userfield field used by Authenticator. The value of this constant
   * is {@value}.
   */
  public static final String DEFAULT_USER_FIELD = "uid";

  /**
   * Whether Authenticator should construct DNs by default. The value of this
   * constant is {@value}.
   */
  public static final boolean DEFAULT_CONSTRUCT_DN = false;

  /**
   * Whether Authenticator should perform subtree searches for DNs. The value of
   * this constant is {@value}.
   */
  public static final boolean DEFAULT_SUBTREE_SEARCH = false;

  /**
   * Default character set for creating strings. The value of this constant is
   * {@value}.
   */
  public static final String DEFAULT_CHARSET = "UTF-8";

  /**
   * Number of times to retry an operation on CommunicationException. The value
   * of this constant is {@value}.
   */
  public static final int OPERATION_RETRY = 1;


  /** Default constructor. */
  private LdapConstants() {}
}
/*
  $Id: LdapConstants.java 282 2009-05-29 21:57:32Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 282 $
  Updated: $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap;

/**
 * <code>LdapConstants</code> contains all the constants needed for creating a
 * <code>Ldap</code>. See
 * http://java.sun.com/j2se/1.4.2/docs/guide/jndi/jndi-ldap.html or
 * http://java.sun.com/j2se/1.4.2/docs/guide/jndi/spec/jndi/properties.html for
 * more information on JNDI properties.
 *
 * @author  Middleware Services
 * @version  $Revision: 282 $ $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
 */
public final class LdapConstants
{

  /**
   * The value of this property is a fully qualified class name of the factory
   * class which creates the initial context for the LDAP service provider. The
   * value of this constant is {@value}.
   */
  public static final String CONTEXT_FACTORY = "java.naming.factory.initial";

  /**
   * The value of this property is a string identifying the class name of a
   * socket factory. The value of this constant is {@value}.
   */
  public static final String SOCKET_FACTORY = "java.naming.ldap.factory.socket";

  /**
   * The value of this property is a string specifying the authoritativeness of
   * the service requested. The value of this constant is {@value}.
   */
  public static final String AUTHORITATIVE = "java.naming.authoritative";

  /**
   * The value of this property is a java.io.OutputStream object into which a
   * hexadecimal dump of the incoming and outgoing LDAP ASN.1 BER packets is
   * written. The value of this constant is {@value}.
   */
  public static final String TRACE = "com.sun.jndi.ldap.trace.ber";

  /**
   * The value of this property is a string that specifies the authentication
   * mechanism(s) for the provider to use. The value of this constant is {@value
   * }.
   */
  public static final String AUTHENTICATION =
    "java.naming.security.authentication";

  /**
   * The value of this property is a string that specifies the identity of the
   * principal to be authenticated. The value of this constant is {@value}.
   */
  public static final String PRINCIPAL = "java.naming.security.principal";

  /**
   * The value of this property is an object that specifies the credentials of
   * the principal to be authenticated. The value of this constant is {@value}.
   */
  public static final String CREDENTIALS = "java.naming.security.credentials";

  /**
   * The value of this property is a string of decimal digits that specifies the
   * batch size of search results returned by the server. The value of this
   * constant is {@value}.
   */
  public static final String BATCH_SIZE = "java.naming.batchsize";

  /**
   * The value of this property is a string that specifies the DNS host and
   * domain names. The value of this constant is {@value}.
   */
  public static final String DNS_URL = "java.naming.dns.url";

  /**
   * The value of this property is a string language tag according to RFC 1766.
   * The value of this constant is {@value}.
   */
  public static final String LANGUAGE = "java.naming.language";

  /**
   * The value of this property is a string that specifies how referrals shall
   * be handled by the provider. The value of this constant is {@value}.
   */
  public static final String REFERRAL = "java.naming.referral";

  /**
   * The value of this property is a string that specifies how aliases shall be
   * handled by the provider. The value of this constant is {@value}.
   */
  public static final String DEREF_ALIASES = "java.naming.ldap.derefAliases";

  /**
   * The value of this property is a string that specifies additional binary
   * attributes. The value of this constant is {@value}.
   */
  public static final String BINARY_ATTRIBUTES =
    "java.naming.ldap.attributes.binary";

  /**
   * The value of this property is a string that specifies a SASL authorization
   * id. The value of this constant is {@value}.
   */
  public static final String SASL_AUTHORIZATION_ID =
    "java.naming.security.sasl.authorizationId";

  /**
   * The value of this property is a string that specifies a SASL realm. The
   * value of this constant is {@value}.
   */
  public static final String SASL_REALM = "java.naming.security.sasl.realm";

  /**
   * The value of this property is a string that specifies to only return
   * attribute type names, no values. The value of this constant is {@value}.
   */
  public static final String TYPES_ONLY = "java.naming.ldap.typesOnly";

  /**
   * The value of this property is a string that specifies the security protocol
   * for the provider to use. The value of this constant is {@value}.
   */
  public static final String PROTOCOL = "java.naming.security.protocol";

  /**
   * The value of this property is a string that specifies the protocol version
   * for the provider. The value of this constant is {@value}.
   */
  public static final String VERSION = "java.naming.ldap.version";

  /**
   * The value of this property is a URL string that specifies the hostname and
   * port number of the LDAP server, and the root distinguished name of the
   * naming context to use. The value of this constant is {@value}.
   */
  public static final String PROVIDER_URL = "java.naming.provider.url";

  /**
   * The value of this property is a string that specifies the time in
   * milliseconds that a connection attempt will abort if the connection cannot
   * be made. The value of this constant is {@value}.
   */
  public static final String TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

  /**
   * Value passed to PROTOCOL to use SSL. The value of this constant is
   * {@value}.
   */
  public static final String SSL_PROTOCOL = "ssl";

  /**
   * Value passed to AUTHENTICATION to use simple authentication. The value of
   * this constant is {@value}.
   */
  public static final String SIMPLE_AUTHTYPE = "simple";

  /**
   * Value passed to AUTHENTICATION to use simple authentication. The value of
   * this constant is {@value}.
   */
  public static final String STRONG_AUTHTYPE = "strong";

  /**
   * Value passed to AUTHENTICATION to use none authentication The value of this
   * constant is {@value}.
   */
  public static final String NONE_AUTHTYPE = "none";

  /**
   * Value passed to VERSION to use ldap version 3 controls The value of this
   * constant is {@value}.
   */
  public static final String VERSION_THREE = "3";

  /** Ldap scheme, the value of this constant is {@value}. */
  public static final String PROVIDER_URL_SCHEME = "ldap";

  /** Secure ldap scheme, the value of this constant is {@value}. */
  public static final String PROVIDER_URL_SSL_SCHEME = "ldaps";

  /**
   * URL prefix used for constructing URLs. The value of this constant is
   * {@value}.
   */
  public static final String PROVIDER_URL_PREFIX = "://";

  /**
   * URL separator used for constructing URLs. The value of this constant is
   * {@value}.
   */
  public static final String PROVIDER_URL_SEPARATOR = ":";

  /**
   * Ldap command which returns a list of supported SASL mechanisms. The value
   * of this constant is {@value}.
   */
  public static final String SUPPORTED_SASL_MECHANISMS =
    "supportedSASLMechanisms";

  /**
   * Ldap command which returns a list of supported controls. The value of this
   * constant is {@value}.
   */
  public static final String SUPPORTED_CONTROL = "supportedcontrol";

  /**
   * Value passed to AUTHENTICATION to use SASL authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_EXTERNAL = "EXTERNAL";

  /**
   * Value passed to AUTHENTICATION to use DIGEST-MD5 authentication. The value
   * of this constant is {@value}.
   */
  public static final String SASL_MECHANISM_DIGEST_MD5 = "DIGEST-MD5";

  /**
   * Value passed to AUTHENTICATION to use CRAM-MD5 authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_CRAM_MD5 = "CRAM-MD5";

  /**
   * Value passed to AUTHENTICATION to use GSS-API authentication. The value of
   * this constant is {@value}.
   */
  public static final String SASL_MECHANISM_GSS_API = "GSSAPI";

  /** List of supported SASL Mechanisms. */
  public static final String[] SASL_MECHANISMS = new String[] {
    SASL_MECHANISM_EXTERNAL,
    SASL_MECHANISM_DIGEST_MD5,
    SASL_MECHANISM_CRAM_MD5,
    SASL_MECHANISM_GSS_API,
  };

  /** Default context factory, value of this constant is {@value}. */
  public static final String DEFAULT_CONTEXT_FACTORY =
    "com.sun.jndi.ldap.LdapCtxFactory";

  /**
   * Default timeout, -1 means use provider setting. The value of this constant
   * is {@value}.
   */
  public static final int DEFAULT_TIMEOUT = -1;

  /** Default authentication type, the value of this constant is {@value}. */
  public static final String DEFAULT_AUTHTYPE = SIMPLE_AUTHTYPE;

  /**
   * Default time limit, 0 means wait indefinitely. The value of this constant
   * is {@value}.
   */
  public static final int DEFAULT_TIME_LIMIT = 0;

  /**
   * Default count limit, 0 means return all results. The value of this constant
   * is {@value}.
   */
  public static final long DEFAULT_COUNT_LIMIT = 0;

  /**
   * Default batch size, -1 means use provider setting. The value of this
   * constant is {@value}.
   */
  public static final int DEFAULT_BATCH_SIZE = -1;

  /** Default authoritative value, the value of this constant is {@value}. */
  public static final boolean DEFAULT_AUTHORITATIVE = false;

  /** Default type only value, the value of this constant is {@value}. */
  public static final boolean DEFAULT_TYPES_ONLY = false;

  /** Default ignore case value, value of this constant is {@value}. */
  public static final boolean DEFAULT_IGNORE_CASE = true;

  /** Default ldap port, the value of this constant is {@value}. */
  public static final String DEFAULT_PORT = "389";

  /** Default ldaps port, the value of this constant is {@value}. */
  public static final String DEFAULT_SSL_PORT = "636";

  /** Whether to use SSL by default, the value of this constant is {@value}. */
  public static final boolean DEFAULT_USE_SSL = false;

  /** Whether to use TLS by default, the value of this constant is {@value}. */
  public static final boolean DEFAULT_USE_TLS = false;

  /**
   * Whether to log authentication credentials. The value of this constant is
   * {@value}.
   */
  public static final boolean DEFAULT_LOG_CREDENTIALS = false;

  /**
   * Default userfield field used by Authenticator. The value of this constant
   * is {@value}.
   */
  public static final String DEFAULT_USER_FIELD = "uid";

  /**
   * Whether Authenticator should construct DNs by default. The value of this
   * constant is {@value}.
   */
  public static final boolean DEFAULT_CONSTRUCT_DN = false;

  /**
   * Whether Authenticator should perform subtree searches for DNs. The value of
   * this constant is {@value}.
   */
  public static final boolean DEFAULT_SUBTREE_SEARCH = false;

  /**
   * Default character set for creating strings. The value of this constant is
   * {@value}.
   */
  public static final String DEFAULT_CHARSET = "UTF-8";

  /**
   * Number of times to retry an operation on CommunicationException. The value
   * of this constant is {@value}.
   */
  public static final int OPERATION_RETRY = 1;


  /** Default constructor. */
  private LdapConstants() {}
}
