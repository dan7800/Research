/*
  $Id: LdapConfig.java 663 2009-09-24 02:06:06Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 663 $
  Updated: $Date: 2009-09-23 19:06:06 -0700 (Wed, 23 Sep 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.LimitExceededException;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import edu.vt.middleware.ldap.handler.FqdnSearchResultHandler;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import edu.vt.middleware.ldap.props.AbstractPropertyConfig;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>LdapConfig</code> contains all the configuration data that the <code>
 * Ldap</code> needs to control connections and searching.
 *
 * @author  Middleware Services
 * @version  $Revision: 663 $ $Date: 2009-09-23 19:06:06 -0700 (Wed, 23 Sep 2009) $
 */
public class LdapConfig extends AbstractPropertyConfig
{


  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.";

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    LdapConfig.class,
    PROPERTIES_DOMAIN);


  /**
   * Enum to define the type of search scope. See {@link
   * javax.naming.directory.SearchControls}.
   */
  public enum SearchScope {

    /** object level search. */
    OBJECT(SearchControls.OBJECT_SCOPE),

    /** one level search. */
    ONELEVEL(SearchControls.ONELEVEL_SCOPE),

    /** subtree search. */
    SUBTREE(SearchControls.SUBTREE_SCOPE);

    /** underlying search scope integer. */
    private int scope;


    /**
     * Creates a new <code>SearchScope</code> with the supplied
     * integer.
     *
     * @param i search scope
     */
    SearchScope(final int i)
    {
      this.scope = i;
    }


    /**
     * Returns the search scope integer.
     *
     * @return  <code>int</code>
     */
    public int scope()
    {
      return this.scope;
    }



    /**
     * Method to convert a JNDI constant value to an enum. Returns null if the
     * supplied constant does not match a known value.
     *
     * @param  i  search scope
     *
     * @return  search scope
     */
    public static SearchScope parseSearchScope(final int i)
    {
      SearchScope ss = null;
      if (OBJECT.scope() == i) {
        ss = OBJECT;
      } else if (ONELEVEL.scope() == i) {
        ss = ONELEVEL;
      } else if (SUBTREE.scope() == i) {
        ss = SUBTREE;
      }
      return ss;
    }
  }

  /** Default context factory. */
  private String contextFactory = LdapConstants.DEFAULT_CONTEXT_FACTORY;

  /** Default ldap socket factory used for SSL and TLS. */
  private SSLSocketFactory sslSocketFactory;

  /** Default hostname verifier for TLS connections. */
  private HostnameVerifier hostnameVerifier;

  /** URL to the LDAP(s). */
  private String ldapUrl;

  /** Hostname of the LDAP server. */
  private String host;

  /** Port the LDAP server is listening on. */
  private String port = LdapConstants.DEFAULT_PORT;

  /** Amount of time in milliseconds that connect operations will block. */
  private Integer timeout;

  /** Username for a service user, this must be a fully qualified DN. */
  private String serviceUser;

  /** Credential for a service user. */
  private Object serviceCredential;

  /** Base dn for LDAP searching. */
  private String base = LdapConstants.DEFAULT_BASE_DN;

  /** Type of search scope to use, default is subtree. */
  private SearchScope searchScope = SearchScope.SUBTREE;

  /** Security level to use when binding to the LDAP. */
  private String authtype = LdapConstants.DEFAULT_AUTHTYPE;

  /** Whether to require the most authoritative source for this service. */
  private boolean authoritative = LdapConstants.DEFAULT_AUTHORITATIVE;

  /** Preferred batch size to use when returning results. */
  private Integer batchSize;

  /** Amount of time in milliseconds that search operations will block. */
  private Integer timeLimit;

  /** Maximum number of entries that search operations will return. */
  private Long countLimit;

  /** Number of times to retry ldap operations on communication exception. */
  private Integer operationRetry;

  /** Whether link dereferencing should be performed during the search. */
  private boolean derefLinkFlag;

  /** Whether objects will be returned in the result. */
  private boolean returningObjFlag;

  /** DNS host to use for JNDI URL context implementation. */
  private String dnsUrl;

  /** Preferred language as defined by RFC 1766. */
  private String language;

  /** How the provider should handle referrals. */
  private String referral;

  /** How the provider should handle aliases. */
  private String derefAliases;

  /** Additional attributes that should be considered binary. */
  private String binaryAttributes;

  /** Handlers to process search results. */
  private SearchResultHandler[] searchResultHandlers =
    new SearchResultHandler[] {new FqdnSearchResultHandler()};

  /** Exception types to ignore when searching. */
  private NamingException[] searchIgnoreExceptions =
    new NamingException[] {new LimitExceededException()};

  /** SASL authorization ID. */
  private String saslAuthorizationId;

  /** SASL realm. */
  private String saslRealm;

  /** Whether only attribute type names should be returned. */
  private boolean typesOnly = LdapConstants.DEFAULT_TYPES_ONLY;

  /** Additional environment properties. */
  private Map<String, Object> additionalEnvironmentProperties =
    new HashMap<String, Object>();

  /** Whether to log authentication credentials. */
  private boolean logCredentials = LdapConstants.DEFAULT_LOG_CREDENTIALS;

  /** Connect to LDAP using SSL protocol. */
  private boolean ssl = LdapConstants.DEFAULT_USE_SSL;

  /** Connect to LDAP using TLS protocol. */
  private boolean tls = LdapConstants.DEFAULT_USE_TLS;

  /** Stream to print LDAP ASN.1 BER packets. */
  private PrintStream tracePackets;


  /** Default constructor. */
  public LdapConfig() {}


  /**
   * This will create a new <code>LdapConfig</code> with the supplied ldap url
   * and base Strings.
   *
   * @param  ldapUrl  <code>String</code> LDAP URL
   * @param  base  <code>String</code> LDAP base DN
   */
  public LdapConfig(final String ldapUrl, final String base)
  {
    this();
    this.setLdapUrl(ldapUrl);
    this.setBase(base);
  }


  /**
   * This returns the Context environment properties that are used to make LDAP
   * connections.
   *
   * @return  <code>Hashtable</code> - context environment
   */
  public Hashtable<String, ?> getEnvironment()
  {
    final Hashtable<String, Object> environment =
      new Hashtable<String, Object>();
    environment.put(LdapConstants.CONTEXT_FACTORY, this.contextFactory);

    if (this.authoritative) {
      environment.put(
        LdapConstants.AUTHORITATIVE,
        Boolean.valueOf(this.authoritative).toString());
    }

    if (this.batchSize != null) {
      environment.put(LdapConstants.BATCH_SIZE, this.batchSize.toString());
    }

    if (this.dnsUrl != null) {
      environment.put(LdapConstants.DNS_URL, this.dnsUrl);
    }

    if (this.language != null) {
      environment.put(LdapConstants.LANGUAGE, this.language);
    }

    if (this.referral != null) {
      environment.put(LdapConstants.REFERRAL, this.referral);
    }

    if (this.derefAliases != null) {
      environment.put(LdapConstants.DEREF_ALIASES, this.derefAliases);
    }

    if (this.binaryAttributes != null) {
      environment.put(LdapConstants.BINARY_ATTRIBUTES, this.binaryAttributes);
    }

    if (this.saslAuthorizationId != null) {
      environment.put(
        LdapConstants.SASL_AUTHORIZATION_ID,
        this.saslAuthorizationId);
    }

    if (this.saslRealm != null) {
      environment.put(LdapConstants.SASL_REALM, this.saslRealm);
    }

    if (this.typesOnly) {
      environment.put(
        LdapConstants.TYPES_ONLY,
        Boolean.valueOf(this.typesOnly).toString());
    }

    if (this.ssl) {
      environment.put(LdapConstants.PROTOCOL, LdapConstants.SSL_PROTOCOL);
      if (this.sslSocketFactory != null) {
        environment.put(
          LdapConstants.SOCKET_FACTORY,
          this.sslSocketFactory.getClass().getName());
      }
    }

    if (this.tracePackets != null) {
      environment.put(LdapConstants.TRACE, this.tracePackets);
    }

    if (this.ldapUrl != null) {
      environment.put(LdapConstants.PROVIDER_URL, this.ldapUrl);
    }

    if (this.timeout != null) {
      environment.put(LdapConstants.TIMEOUT, this.timeout.toString());
    }

    if (!this.additionalEnvironmentProperties.isEmpty()) {
      for (
        Map.Entry<String, Object> entry :
          this.additionalEnvironmentProperties.entrySet()) {
        environment.put(entry.getKey(), entry.getValue());
      }
    }

    return environment;
  }


  /**
   * This returns the context factory of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - context factory
   */
  public String getContextFactory()
  {
    return this.contextFactory;
  }


  /**
   * This returns the SSL socket factory of the <code>LdapConfig</code>.
   *
   * @return  <code>SSLSocketFactory</code> - SSL socket factory
   */
  public SSLSocketFactory getSslSocketFactory()
  {
    return this.sslSocketFactory;
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using a custom SSL
   * socket factory.
   *
   * @return  <code>boolean</code>
   */
  public boolean useSslSocketFactory()
  {
    return this.sslSocketFactory != null;
  }


  /**
   * This returns the hostname verifier of the <code>LdapConfig</code>.
   *
   * @return  <code>HostnameVerifier</code> - hostname verifier
   */
  public HostnameVerifier getHostnameVerifier()
  {
    return this.hostnameVerifier;
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using a custom hostname
   * verifier.
   *
   * @return  <code>boolean</code>
   */
  public boolean useHostnameVerifier()
  {
    return this.hostnameVerifier != null;
  }


  /**
   * This returns the ldap url of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - ldap url
   */
  public String getLdapUrl()
  {
    return this.ldapUrl;
  }


  /**
   * This returns the hostname of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - hostname
   *
   * @deprecated  use {@link #getLdapUrl()} instead
   */
  @Deprecated
  public String getHost()
  {
    return this.host;
  }


  /**
   * This returns the port of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - port
   *
   * @deprecated  use {@link #getLdapUrl()} instead
   */
  @Deprecated
  public String getPort()
  {
    return this.port;
  }


  /**
   * This returns the timeout for the <code>LdapConfig</code>. If this value is
   * 0, then connect operations will wait indefinitely.
   *
   * @return  <code>int</code> - timeout
   */
  public int getTimeout()
  {
    int time = LdapConstants.DEFAULT_TIMEOUT;
    if (this.timeout != null) {
      time = this.timeout.intValue();
    }
    return time;
  }


  /**
   * This returns the username of the service user.
   *
   * @return  <code>String</code> - username
   */
  public String getServiceUser()
  {
    return this.serviceUser;
  }


  /**
   * This returns the credential of the service user.
   *
   * @return  <code>Object</code> - credential
   */
  public Object getServiceCredential()
  {
    return this.serviceCredential;
  }


  /**
   * This returns the base dn for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - base dn
   */
  public String getBase()
  {
    return this.base;
  }


  /**
   * This returns the search scope for the <code>LdapConfig</code>.
   *
   * @return  <code>SearchScope</code> - search scope
   */
  public SearchScope getSearchScope()
  {
    return this.searchScope;
  }


  /**
   * This returns whether the search scope is set to object.
   *
   * @return  <code>boolean</code>
   */
  public boolean isObjectSearchScope()
  {
    return this.searchScope == SearchScope.OBJECT;
  }


  /**
   * This returns whether the search scope is set to one level.
   *
   * @return  <code>boolean</code>
   */
  public boolean isOneLevelSearchScope()
  {
    return this.searchScope == SearchScope.ONELEVEL;
  }


  /**
   * This returns whether the search scope is set to sub tree.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSubTreeSearchScope()
  {
    return this.searchScope == SearchScope.SUBTREE;
  }


  /**
   * This returns the security level for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - security level
   */
  public String getAuthtype()
  {
    return this.authtype;
  }


  /**
   * This returns whether the security authentication context is set to 'none'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isAnonymousAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.NONE_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'simple'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSimpleAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.SIMPLE_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'strong'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isStrongAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.STRONG_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context will perform a
   * SASL bind as defined by the supported SASL mechanisms.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSaslAuth()
  {
    boolean authtypeSasl = false;
    for (String sasl : LdapConstants.SASL_MECHANISMS) {
      if (this.authtype.equalsIgnoreCase(sasl)) {
        authtypeSasl = true;
        break;
      }
    }
    return authtypeSasl;
  }


  /**
   * This returns whether the security authentication context is set to
   * 'EXTERNAL'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isExternalAuth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_EXTERNAL);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'DIGEST-MD5'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isDigestMD5Auth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_DIGEST_MD5);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'CRAM-MD5'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isCramMD5Auth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_CRAM_MD5);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'GSSAPI'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isGSSAPIAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_GSS_API);
  }


  /**
   * See {@link #isAuthoritative()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getAuthoritative()
  {
    return this.isAuthoritative();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to require a
   * authoritative source.
   *
   * @return  <code>boolean</code>
   */
  public boolean isAuthoritative()
  {
    return this.authoritative;
  }


  /**
   * This returns the time limit for the <code>LdapConfig</code>. If this value
   * is 0, then search operations will wait indefinitely for an answer.
   *
   * @return  <code>int</code> - time limit
   */
  public int getTimeLimit()
  {
    int limit = LdapConstants.DEFAULT_TIME_LIMIT;
    if (this.timeLimit != null) {
      limit = this.timeLimit.intValue();
    }
    return limit;
  }


  /**
   * This returns the count limit for the <code>LdapConfig</code>. If this value
   * is 0, then search operations will return all the results it finds.
   *
   * @return  <code>long</code> - count limit
   */
  public long getCountLimit()
  {
    long limit = LdapConstants.DEFAULT_COUNT_LIMIT;
    if (this.countLimit != null) {
      limit = this.countLimit.longValue();
    }
    return limit;
  }


  /**
   * This returns the number of times ldap operations will be retried if a
   * communication exception occurs. If this value is 0, no retries will occur.
   *
   * @return  <code>int</code> - retry count
   */
  public int getOperationRetry()
  {
    int retry = LdapConstants.OPERATION_RETRY;
    if (this.operationRetry != null) {
      retry = this.operationRetry.intValue();
    }
    return retry;
  }


  /**
   * This returns the derefLinkFlag for the <code>LdapConfig</code>.
   *
   * @return  <code>boolean</code>
   */
  public boolean getDerefLinkFlag()
  {
    return this.derefLinkFlag;
  }


  /**
   * This returns the returningObjFlag for the <code>LdapConfig</code>.
   *
   * @return  <code>boolean</code>
   */
  public boolean getReturningObjFlag()
  {
    return this.returningObjFlag;
  }


  /**
   * This returns the batch size for the <code>LdapConfig</code>. If this value
   * is -1, then the default provider setting is being used.
   *
   * @return  <code>int</code> - batch size
   */
  public int getBatchSize()
  {
    int size = LdapConstants.DEFAULT_BATCH_SIZE;
    if (this.batchSize != null) {
      size = this.batchSize.intValue();
    }
    return size;
  }


  /**
   * This returns the dns url for the <code>LdapConfig</code>. If this value is
   * null, then this property is not being used.
   *
   * @return  <code>String</code> - dns url
   */
  public String getDnsUrl()
  {
    return this.dnsUrl;
  }


  /**
   * This returns the preferred language for the <code>LdapConfig</code>. If
   * this value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - language
   */
  public String getLanguage()
  {
    return this.language;
  }


  /**
   * This returns the referral setting for the <code>LdapConfig</code>. If this
   * value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - referral
   */
  public String getReferral()
  {
    return this.referral;
  }


  /**
   * This returns the alias setting for the <code>LdapConfig</code>. If this
   * value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - alias
   */
  public String getDerefAliases()
  {
    return this.derefAliases;
  }


  /**
   * This returns additional binary attributes for the <code>LdapConfig</code>.
   * If this value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - binary attributes
   */
  public String getBinaryAttributes()
  {
    return this.binaryAttributes;
  }


  /**
   * This returns the handlers to use for processing search results.
   *
   * @return  <code>SearchResultHandler[]</code>
   */
  public SearchResultHandler[] getSearchResultHandlers()
  {
    return this.searchResultHandlers;
  }


  /**
   * This returns the exceptions to ignore when searching.
   *
   * @return  <code>NamingException[]</code>
   */
  public NamingException[] getSearchIgnoreExceptions()
  {
    return this.searchIgnoreExceptions;
  }


  /**
   * This returns ths SASL authorization id for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - authorization id
   */
  public String getSaslAuthorizationId()
  {
    return this.saslAuthorizationId;
  }


  /**
   * This returns ths SASL realm for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - realm
   */
  public String getSaslRealm()
  {
    return this.saslRealm;
  }


  /**
   * See {@link #isTypesOnly()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getTypesOnly()
  {
    return this.isTypesOnly();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to only return
   * attribute types.
   *
   * @return  <code>boolean</code>
   */
  public boolean isTypesOnly()
  {
    return this.typesOnly;
  }


  /**
   * This returns any environment properties that may have been set for the
   * <code>LdapConfig</code> using {@link
   * #setEnvironmentProperties(String,String)} that do not represent properties
   * of this config. The collection returned is unmodifiable.
   *
   * @return  <code>Map</code> - additional environment properties
   */
  public Map<String, Object> getEnvironmentProperties()
  {
    return Collections.unmodifiableMap(this.additionalEnvironmentProperties);
  }


  /**
   * This returns whether authentication credentials will be logged.
   *
   * @return  <code>boolean</code> - whether authentication credentials will be
   * logged.
   */
  public boolean getLogCredentials()
  {
    return this.logCredentials;
  }


  /**
   * See {@link #isSslEnabled()}.
   *
   * @return  <code>boolean</code> - whether the SSL protocol is being used
   */
  public boolean getSsl()
  {
    return this.isSslEnabled();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using the SSL protocol
   * for connections.
   *
   * @return  <code>boolean</code> - whether the SSL protocol is being used
   */
  public boolean isSslEnabled()
  {
    return this.ssl;
  }


  /**
   * See {@link #isTlsEnabled()}.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean getTls()
  {
    return this.isTlsEnabled();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using the TLS protocol
   * for connections.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean isTlsEnabled()
  {
    return this.tls;
  }


  /**
   * This sets the context factory of the <code>LdapConfig</code>.
   *
   * @param  contextFactory  <code>String</code> context factory
   */
  public void setContextFactory(final String contextFactory)
  {
    checkImmutable();
    checkStringInput(contextFactory, false);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting contextFactory: " + contextFactory);
    }
    this.contextFactory = contextFactory;
  }


  /**
   * This sets the SSL socket factory of the <code>LdapConfig</code>.
   *
   * @param  sslSocketFactory  <code>SSLSocketFactory</code> SSL socket factory
   */
  public void setSslSocketFactory(final SSLSocketFactory sslSocketFactory)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting sslSocketFactory: " + sslSocketFactory);
    }
    this.sslSocketFactory = sslSocketFactory;
  }


  /**
   * This sets the hostname verifier of the <code>LdapConfig</code>.
   *
   * @param  hostnameVerifier  <code>HostnameVerifier</code> hostname verifier
   */
  public void setHostnameVerifier(final HostnameVerifier hostnameVerifier)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting hostnameVerifier: " + hostnameVerifier);
    }
    this.hostnameVerifier = hostnameVerifier;
  }


  /**
   * This sets the ldap url of the <code>LdapConfig</code>.
   *
   * @param  ldapUrl  <code>String</code> url
   */
  public void setLdapUrl(final String ldapUrl)
  {
    checkImmutable();
    checkStringInput(ldapUrl, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting ldapUrl: " + ldapUrl);
    }
    this.ldapUrl = ldapUrl;
  }


  /**
   * This sets the hostname of the <code>LdapConfig</code>. The host string may
   * be of the form ldap://host.domain.name:389, host.domain.name:389, or
   * host.domain.name. Do not use with {@link #setLdapUrl(String)}.
   *
   * @param  host  <code>String</code> hostname
   *
   * @deprecated  use {@link #setLdapUrl(String)} instead
   */
  @Deprecated
  public void setHost(final String host)
  {
    checkImmutable();
    if (host != null) {
      final int prefixLength = LdapConstants.PROVIDER_URL_PREFIX.length();
      final int separatorLength = LdapConstants.PROVIDER_URL_SEPARATOR.length();
      String h = host;

      // if host contains '://' and there is data after it, remove the scheme
      if (
        h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) != -1 &&
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) + prefixLength <
          h.length()) {
        final String scheme = h.substring(
          0,
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX));
        if (scheme.equalsIgnoreCase(LdapConstants.PROVIDER_URL_SSL_SCHEME)) {
          this.setSsl(true);
          this.setPort(LdapConstants.DEFAULT_SSL_PORT);
        }
        h = h.substring(
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) + prefixLength,
          h.length());
      }

      // if host contains ':' and there is data after it, remove the port
      if (
        h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) != -1 &&
          h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) + separatorLength <
          h.length()) {
        final String p = h.substring(
          h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) + separatorLength,
          h.length());
        this.port = p;
        h = h.substring(0, h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR));
      }

      this.host = h;
      this.setLdapUrl(
        LdapConstants.PROVIDER_URL_SCHEME + LdapConstants.PROVIDER_URL_PREFIX +
        this.host + LdapConstants.PROVIDER_URL_SEPARATOR + this.port);
    }
  }


  /**
   * This sets the port of the <code>LdapConfig</code>. Do not use with {@link
   * #setLdapUrl(String)}.
   *
   * @param  port  <code>String</code> port
   *
   * @deprecated  use {@link #setLdapUrl(String)} instead
   */
  @Deprecated
  public void setPort(final String port)
  {
    checkImmutable();
    this.port = port;
    if (this.host != null) {
      this.setLdapUrl(
        LdapConstants.PROVIDER_URL_SCHEME + LdapConstants.PROVIDER_URL_PREFIX +
        this.host + LdapConstants.PROVIDER_URL_SEPARATOR + this.port);
    }
  }


  /**
   * This sets the maximum amount of time in milliseconds that connect
   * operations will block.
   *
   * @param  timeout  <code>int</code>
   */
  public void setTimeout(final int timeout)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting timeout: " + timeout);
    }
    this.timeout = new Integer(timeout);
  }


  /**
   * This sets the username of the service user. user must be a fully qualified
   * DN.
   *
   * @param  user  <code>String</code> username
   */
  public void setServiceUser(final String user)
  {
    checkImmutable();
    checkStringInput(user, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting serviceUser: " + user);
    }
    this.serviceUser = user;
  }


  /**
   * This sets the credential of the service user.
   *
   * @param  credential  <code>Object</code>
   */
  public void setServiceCredential(final Object credential)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      if (this.getLogCredentials()) {
        this.logger.trace("setting serviceCredential: " + credential);
      } else {
        this.logger.trace("setting serviceCredential: <suppressed>");
      }
    }
    this.serviceCredential = credential;
  }


  /**
   * This sets the username and credential of the service user. user must be a
   * fully qualified DN.
   *
   * @param  user  <code>String</code> service user dn
   * @param  credential  <code>Object</code>
   */
  public void setService(final String user, final Object credential)
  {
    checkImmutable();
    checkStringInput(user, true);
    this.setServiceUser(user);
    this.setServiceCredential(credential);
  }


  /**
   * This sets the base dn for the <code>LdapConfig</code>.
   *
   * @param  base  <code>String</code> base dn
   */
  public void setBase(final String base)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting base: " + base);
    }
    this.base = base;
  }


  /**
   * This sets the search scope for the <code>LdapConfig</code>.
   *
   * @param  searchScope  <code>SearchScope</code>
   */
  public void setSearchScope(final SearchScope searchScope)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting searchScope: " + searchScope);
    }
    this.searchScope = searchScope;
  }


  /**
   * This sets the security level for the <code>LdapConfig</code>.
   *
   * @param  authtype  <code>String</code> security level
   */
  public void setAuthtype(final String authtype)
  {
    checkImmutable();
    checkStringInput(authtype, false);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authtype: " + authtype);
    }
    this.authtype = authtype;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * require an authoritative source.
   *
   * @param  authoritative  <code>boolean</code>
   */
  public void setAuthoritative(final boolean authoritative)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authoritative: " + authoritative);
    }
    this.authoritative = authoritative;
  }


  /**
   * This sets the maximum amount of time in milliseconds that search operations
   * will block.
   *
   * @param  timeLimit  <code>int</code>
   */
  public void setTimeLimit(final int timeLimit)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting timeLimit: " + timeLimit);
    }
    this.timeLimit = new Integer(timeLimit);
  }


  /**
   * This sets the maximum number of entries that search operations will return.
   *
   * @param  countLimit  <code>long</code>
   */
  public void setCountLimit(final long countLimit)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting countLimit: " + countLimit);
    }
    this.countLimit = new Long(countLimit);
  }


  /**
   * This sets the number of times that ldap operations will be retried if a
   * communication exception occurs.
   *
   * @param  operationRetry  <code>int</code>
   */
  public void setOperationRetry(final int operationRetry)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting operationRetry: " + operationRetry);
    }
    this.operationRetry = new Integer(operationRetry);
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to link
   * dereferencing during searches.
   *
   * @param  derefLinkFlag  <code>boolean</code>
   */
  public void setDerefLinkFlag(final boolean derefLinkFlag)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting derefLinkFlag: " + derefLinkFlag);
    }
    this.derefLinkFlag = derefLinkFlag;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * return objects for searches.
   *
   * @param  returningObjFlag  <code>boolean</code>
   */
  public void setReturningObjFlag(final boolean returningObjFlag)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting returningObjFlag: " + returningObjFlag);
    }
    this.returningObjFlag = returningObjFlag;
  }


  /**
   * This sets the batch size for the <code>LdapConfig</code>. A value of -1
   * indicates to use the provider default.
   *
   * @param  batchSize  <code>int</code> batch size to use when returning
   * results
   */
  public void setBatchSize(final int batchSize)
  {
    checkImmutable();
    if (batchSize == -1) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting batchSize: " + null);
      }
      this.batchSize = null;
    } else {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting batchSize: " + batchSize);
      }
      this.batchSize = new Integer(batchSize);
    }
  }


  /**
   * This sets the dns url for the <code>LdapConfig</code>.
   *
   * @param  dnsUrl  <code>String</code>
   */
  public void setDnsUrl(final String dnsUrl)
  {
    checkImmutable();
    checkStringInput(dnsUrl, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting dnsUrl: " + dnsUrl);
    }
    this.dnsUrl = dnsUrl;
  }


  /**
   * This sets the preferred language for the <code>LdapConfig</code>.
   *
   * @param  language  <code>String</code> defined by RFC 1766
   */
  public void setLanguage(final String language)
  {
    checkImmutable();
    checkStringInput(language, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting language: " + language);
    }
    this.language = language;
  }


  /**
   * This specifies how the <code>LdapConfig</code> should handle referrals.
   * referral must be one of: "throw", "ignore", or "follow".
   *
   * @param  referral  <code>String</code> defined by RFC 1766
   */
  public void setReferral(final String referral)
  {
    checkImmutable();
    checkStringInput(referral, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting referral: " + referral);
    }
    this.referral = referral;
  }


  /**
   * This specifies how the <code>LdapConfig</code> should handle aliases.
   * derefAliases must be one of: "always", "never", "finding", or "searching".
   *
   * @param  derefAliases  <code>String</code>
   */
  public void setDerefAliases(final String derefAliases)
  {
    checkImmutable();
    checkStringInput(derefAliases, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting derefAliases: " + derefAliases);
    }
    this.derefAliases = derefAliases;
  }


  /**
   * This specifies additional attributes that should be considered binary.
   * Attributes should be space delimited.
   *
   * @param  binaryAttributes  <code>String</code>
   */
  public void setBinaryAttributes(final String binaryAttributes)
  {
    checkImmutable();
    checkStringInput(binaryAttributes, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting binaryAttributes: " + binaryAttributes);
    }
    this.binaryAttributes = binaryAttributes;
  }


  /**
   * This sets the handlers for processing search results.
   *
   * @param  handlers  <code>SearchResultHandler[]</code>
   */
  public void setSearchResultHandlers(final SearchResultHandler[] handlers)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "setting searchResultsHandlers: " +
        (handlers == null ? "null" : Arrays.asList(handlers)));
    }
    this.searchResultHandlers = handlers;
  }


  /**
   * This sets the exceptions to ignore when searching.
   *
   * @param  exceptions  <code>NamingException[]</code>
   */
  public void setSearchIgnoreExceptions(final NamingException[] exceptions)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "setting searchIgnoreExceptions: " +
        (exceptions == null ? "null" : Arrays.asList(exceptions)));
    }
    this.searchIgnoreExceptions = exceptions;
  }


  /**
   * This specifies a SASL authorization id.
   *
   * @param  saslAuthorizationId  <code>String</code>
   */
  public void setSaslAuthorizationId(final String saslAuthorizationId)
  {
    checkImmutable();
    checkStringInput(saslAuthorizationId, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting saslAuthorizationId: " + saslAuthorizationId);
    }
    this.saslAuthorizationId = saslAuthorizationId;
  }


  /**
   * This specifies a SASL realm.
   *
   * @param  saslRealm  <code>String</code>
   */
  public void setSaslRealm(final String saslRealm)
  {
    checkImmutable();
    checkStringInput(saslRealm, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting saslRealm: " + saslRealm);
    }
    this.saslRealm = saslRealm;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * return only attribute types.
   *
   * @param  typesOnly  <code>boolean</code>
   */
  public void setTypesOnly(final boolean typesOnly)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting typesOnly: " + typesOnly);
    }
    this.typesOnly = typesOnly;
  }


  /** {@inheritDoc}. */
  public String getPropertiesDomain()
  {
    return PROPERTIES_DOMAIN;
  }


  /** {@inheritDoc}. */
  public void setEnvironmentProperties(final String name, final String value)
  {
    checkImmutable();
    if (name != null && value != null) {
      if (PROPERTIES.hasProperty(name)) {
        PROPERTIES.setProperty(this, name, value);
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("setting property " + name + ": " + value);
        }
        this.additionalEnvironmentProperties.put(name, value);
      }
    }
  }


  /** {@inheritDoc}. */
  public boolean hasEnvironmentProperty(final String name)
  {
    return PROPERTIES.hasProperty(name);
  }


  /**
   * Create an instance of this class initialized with properties from the input
   * stream. If the input stream is null, load properties from the default
   * properties file.
   *
   * @param  is  to load properties from
   *
   * @return  <code>LdapPoolConfig</code> initialized ldap pool config
   */
  public static LdapConfig createFromProperties(final InputStream is)
  {
    final LdapConfig ldapConfig = new LdapConfig();
    LdapProperties properties = null;
    if (is != null) {
      properties = new LdapProperties(ldapConfig, is);
    } else {
      properties = new LdapProperties(ldapConfig);
      properties.useDefaultPropertiesFile();
    }
    properties.configure();
    return ldapConfig;
  }


  /**
   * This sets whether authentication credentials will be logged.
   *
   * @param  log  <code>boolean</code>
   */
  public void setLogCredentials(final boolean log)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting logCredentials: " + log);
    }
    this.logCredentials = log;
  }


  /**
   * This sets this <code>LdapConfig</code> to use the SSL protocol for
   * connections. The port is automatically changed to the default of 636. If
   * you need to use a different port then you must call {@link #setPort} after
   * calling this method.
   *
   * @param  ssl  <code>boolean</code>
   */
  public void setSsl(final boolean ssl)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting ssl: " + ssl);
    }
    this.ssl = ssl;
  }


  /**
   * This sets this <code>LdapConfig</code> to use the TLS protocol for
   * connections. For fine control over starting and stopping TLS you must use
   * the {@link Ldap#useTls(boolean)} method.
   *
   * @param  tls  <code>boolean</code>
   */
  public void setTls(final boolean tls)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting tls: " + tls);
    }
    this.tls = tls;
  }


  /**
   * This returns a <code>SearchControls</code> object configured with this
   * <code>LdapConfig</code>.
   *
   * @param  retAttrs  <code>String[]</code> attributes to return from search
   *
   * @return  <code>SearchControls</code>
   */
  public SearchControls getSearchControls(final String[] retAttrs)
  {
    final SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(retAttrs);
    ctls.setSearchScope(this.getSearchScope().ordinal());
    ctls.setTimeLimit(this.getTimeLimit());
    ctls.setCountLimit(this.getCountLimit());
    ctls.setDerefLinkFlag(this.getDerefLinkFlag());
    ctls.setReturningObjFlag(this.getReturningObjFlag());
    return ctls;
  }


  /**
   * This returns a <code>SearchControls</code> object configured to perform a
   * LDAP compare operation.
   *
   * @return  <code>SearchControls</code>
   */
  public static SearchControls getCompareSearchControls()
  {
    final SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(new String[0]);
    ctls.setSearchScope(SearchScope.OBJECT.ordinal());
    return ctls;
  }


  /**
   * This sets this <code>LdapConfig</code> to print ASN.1 BER packets to the
   * supplied <code>PrintStream</code>.
   *
   * @param  stream  <code>PrintStream</code>
   */
  public void setTracePackets(final PrintStream stream)
  {
    checkImmutable();
    this.tracePackets = stream;
  }


  /**
   * Provides a descriptive string representation of this instance.
   *
   * @return  String of the form $Classname@hashCode::config=$config.
   */
  @Override
  public String toString()
  {
    return
      String.format(
        "%s@%d::%s",
        this.getClass().getName(),
        this.hashCode(),
        this.getEnvironment());
  }
}
/*
  $Id: LdapConfig.java 494 2009-08-28 02:31:50Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 494 $
  Updated: $Date: 2009-08-27 19:31:50 -0700 (Thu, 27 Aug 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.directory.SearchControls;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import edu.vt.middleware.ldap.handler.FqdnSearchResultHandler;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import edu.vt.middleware.ldap.props.AbstractPropertyConfig;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>LdapConfig</code> contains all the configuration data that the <code>
 * Ldap</code> needs to control connections and searching.
 *
 * @author  Middleware Services
 * @version  $Revision: 494 $ $Date: 2009-08-27 19:31:50 -0700 (Thu, 27 Aug 2009) $
 */
public class LdapConfig extends AbstractPropertyConfig
{


  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.";

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    LdapConfig.class,
    PROPERTIES_DOMAIN);


  /**
   * Enum to define the type of search scope. See {@link
   * javax.naming.directory.SearchControls}.
   */
  public enum SearchScope {

    /** object level search. */
    OBJECT,

    /** one level search. */
    ONELEVEL,

    /** subtree search. */
    SUBTREE;


    /**
     * Method to convert a JNDI constant value to an enum. Returns null if the
     * supplied constant does not match a known value.
     *
     * @param  i  jndi constant
     *
     * @return  search scope
     */
    public static SearchScope parseSearchScope(final int i)
    {
      SearchScope ss = null;
      if (OBJECT.ordinal() == i) {
        ss = OBJECT;
      } else if (ONELEVEL.ordinal() == i) {
        ss = ONELEVEL;
      } else if (SUBTREE.ordinal() == i) {
        ss = SUBTREE;
      }
      return ss;
    }
  }

  /** Default context factory. */
  private String contextFactory = LdapConstants.DEFAULT_CONTEXT_FACTORY;

  /** Default ldap socket factory used for SSL and TLS. */
  private SSLSocketFactory sslSocketFactory;

  /** Default hostname verifier for TLS connections. */
  private HostnameVerifier hostnameVerifier;

  /** URL to the LDAP(s). */
  private String ldapUrl;

  /** Hostname of the LDAP server. */
  private String host;

  /** Port the LDAP server is listening on. */
  private String port = LdapConstants.DEFAULT_PORT;

  /** Amount of time in milliseconds that connect operations will block. */
  private Integer timeout;

  /** Username for a service user, this must be a fully qualified DN. */
  private String serviceUser;

  /** Credential for a service user. */
  private Object serviceCredential;

  /** Base dn for LDAP searching. */
  private String base = LdapConstants.DEFAULT_BASE_DN;

  /** Type of search scope to use, default is subtree. */
  private SearchScope searchScope = SearchScope.SUBTREE;

  /** Security level to use when binding to the LDAP. */
  private String authtype = LdapConstants.DEFAULT_AUTHTYPE;

  /** Whether to require the most authoritative source for this service. */
  private boolean authoritative = LdapConstants.DEFAULT_AUTHORITATIVE;

  /** Whether to ignore case in attribute names. */
  private boolean ignoreCase = LdapConstants.DEFAULT_IGNORE_CASE;

  /** Preferred batch size to use when returning results. */
  private Integer batchSize;

  /** Amount of time in milliseconds that search operations will block. */
  private Integer timeLimit;

  /** Maximum number of entries that search operations will return. */
  private Long countLimit;

  /** Number of times to retry ldap operations on communication exception. */
  private Integer operationRetry;

  /** Whether link dereferencing should be performed during the search. */
  private boolean derefLinkFlag;

  /** Whether objects will be returned in the result. */
  private boolean returningObjFlag;

  /** DNS host to use for JNDI URL context implementation. */
  private String dnsUrl;

  /** Preferred language as defined by RFC 1766. */
  private String language;

  /** How the provider should handle referrals. */
  private String referral;

  /** How the provider should handle aliases. */
  private String derefAliases;

  /** Additional attributes that should be considered binary. */
  private String binaryAttributes;

  /** Handlers to process search results. */
  private SearchResultHandler[] searchResultHandlers =
    new SearchResultHandler[] {new FqdnSearchResultHandler()};

  /** SASL authorization ID. */
  private String saslAuthorizationId;

  /** SASL realm. */
  private String saslRealm;

  /** Whether only attribute type names should be returned. */
  private boolean typesOnly = LdapConstants.DEFAULT_TYPES_ONLY;

  /** Additional environment properties. */
  private Map<String, Object> additionalEnvironmentProperties =
    new HashMap<String, Object>();

  /** Whether to log authentication credentials. */
  private boolean logCredentials = LdapConstants.DEFAULT_LOG_CREDENTIALS;

  /** Connect to LDAP using SSL protocol. */
  private boolean ssl = LdapConstants.DEFAULT_USE_SSL;

  /** Connect to LDAP using TLS protocol. */
  private boolean tls = LdapConstants.DEFAULT_USE_TLS;

  /** Stream to print LDAP ASN.1 BER packets. */
  private PrintStream tracePackets;


  /** Default constructor. */
  public LdapConfig() {}


  /**
   * This will create a new <code>LdapConfig</code> with the supplied ldap url
   * and base Strings.
   *
   * @param  ldapUrl  <code>String</code> LDAP URL
   * @param  base  <code>String</code> LDAP base DN
   */
  public LdapConfig(final String ldapUrl, final String base)
  {
    this();
    this.setLdapUrl(ldapUrl);
    this.setBase(base);
  }


  /**
   * This returns the Context environment properties that are used to make LDAP
   * connections.
   *
   * @return  <code>Hashtable</code> - context environment
   */
  public Hashtable<String, ?> getEnvironment()
  {
    final Hashtable<String, Object> environment =
      new Hashtable<String, Object>();
    environment.put(LdapConstants.CONTEXT_FACTORY, this.contextFactory);

    if (this.authoritative) {
      environment.put(
        LdapConstants.AUTHORITATIVE,
        Boolean.valueOf(this.authoritative).toString());
    }

    if (this.batchSize != null) {
      environment.put(LdapConstants.BATCH_SIZE, this.batchSize.toString());
    }

    if (this.dnsUrl != null) {
      environment.put(LdapConstants.DNS_URL, this.dnsUrl);
    }

    if (this.language != null) {
      environment.put(LdapConstants.LANGUAGE, this.language);
    }

    if (this.referral != null) {
      environment.put(LdapConstants.REFERRAL, this.referral);
    }

    if (this.derefAliases != null) {
      environment.put(LdapConstants.DEREF_ALIASES, this.derefAliases);
    }

    if (this.binaryAttributes != null) {
      environment.put(LdapConstants.BINARY_ATTRIBUTES, this.binaryAttributes);
    }

    if (this.saslAuthorizationId != null) {
      environment.put(
        LdapConstants.SASL_AUTHORIZATION_ID,
        this.saslAuthorizationId);
    }

    if (this.saslRealm != null) {
      environment.put(LdapConstants.SASL_REALM, this.saslRealm);
    }

    if (this.typesOnly) {
      environment.put(
        LdapConstants.TYPES_ONLY,
        Boolean.valueOf(this.typesOnly).toString());
    }

    if (this.ssl) {
      environment.put(LdapConstants.PROTOCOL, LdapConstants.SSL_PROTOCOL);
      if (this.sslSocketFactory != null) {
        environment.put(
          LdapConstants.SOCKET_FACTORY,
          this.sslSocketFactory.getClass().getName());
      }
    }

    if (this.tracePackets != null) {
      environment.put(LdapConstants.TRACE, this.tracePackets);
    }

    if (this.ldapUrl != null) {
      environment.put(LdapConstants.PROVIDER_URL, this.ldapUrl);
    }

    if (this.timeout != null) {
      environment.put(LdapConstants.TIMEOUT, this.timeout.toString());
    }

    if (!this.additionalEnvironmentProperties.isEmpty()) {
      for (
        Map.Entry<String, Object> entry :
          this.additionalEnvironmentProperties.entrySet()) {
        environment.put(entry.getKey(), entry.getValue());
      }
    }

    return environment;
  }


  /**
   * This returns the context factory of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - context factory
   */
  public String getContextFactory()
  {
    return this.contextFactory;
  }


  /**
   * This returns the SSL socket factory of the <code>LdapConfig</code>.
   *
   * @return  <code>SSLSocketFactory</code> - SSL socket factory
   */
  public SSLSocketFactory getSslSocketFactory()
  {
    return this.sslSocketFactory;
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using a custom SSL
   * socket factory.
   *
   * @return  <code>boolean</code>
   */
  public boolean useSslSocketFactory()
  {
    return this.sslSocketFactory != null;
  }


  /**
   * This returns the hostname verifier of the <code>LdapConfig</code>.
   *
   * @return  <code>HostnameVerifier</code> - hostname verifier
   */
  public HostnameVerifier getHostnameVerifier()
  {
    return this.hostnameVerifier;
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using a custom hostname
   * verifier.
   *
   * @return  <code>boolean</code>
   */
  public boolean useHostnameVerifier()
  {
    return this.hostnameVerifier != null;
  }


  /**
   * This returns the ldap url of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - ldap url
   */
  public String getLdapUrl()
  {
    return this.ldapUrl;
  }


  /**
   * This returns the hostname of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - hostname
   *
   * @deprecated  use {@link #getLdapUrl()} instead
   */
  @Deprecated
  public String getHost()
  {
    return this.host;
  }


  /**
   * This returns the port of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - port
   *
   * @deprecated  use {@link #getLdapUrl()} instead
   */
  @Deprecated
  public String getPort()
  {
    return this.port;
  }


  /**
   * This returns the timeout for the <code>LdapConfig</code>. If this value is
   * 0, then connect operations will wait indefinitely.
   *
   * @return  <code>int</code> - timeout
   */
  public int getTimeout()
  {
    int time = LdapConstants.DEFAULT_TIMEOUT;
    if (this.timeout != null) {
      time = this.timeout.intValue();
    }
    return time;
  }


  /**
   * This returns the username of the service user.
   *
   * @return  <code>String</code> - username
   */
  public String getServiceUser()
  {
    return this.serviceUser;
  }


  /**
   * This returns the credential of the service user.
   *
   * @return  <code>Object</code> - credential
   */
  public Object getServiceCredential()
  {
    return this.serviceCredential;
  }


  /**
   * This returns the base dn for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - base dn
   */
  public String getBase()
  {
    return this.base;
  }


  /**
   * This returns the search scope for the <code>LdapConfig</code>.
   *
   * @return  <code>SearchScope</code> - search scope
   */
  public SearchScope getSearchScope()
  {
    return this.searchScope;
  }


  /**
   * This returns whether the search scope is set to object.
   *
   * @return  <code>boolean</code>
   */
  public boolean isObjectSearchScope()
  {
    return this.searchScope == SearchScope.OBJECT;
  }


  /**
   * This returns whether the search scope is set to one level.
   *
   * @return  <code>boolean</code>
   */
  public boolean isOneLevelSearchScope()
  {
    return this.searchScope == SearchScope.ONELEVEL;
  }


  /**
   * This returns whether the search scope is set to sub tree.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSubTreeSearchScope()
  {
    return this.searchScope == SearchScope.SUBTREE;
  }


  /**
   * This returns the security level for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - security level
   */
  public String getAuthtype()
  {
    return this.authtype;
  }


  /**
   * This returns whether the security authentication context is set to 'none'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isAnonymousAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.NONE_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'simple'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSimpleAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.SIMPLE_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'strong'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isStrongAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.STRONG_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context will perform a
   * SASL bind as defined by the supported SASL mechanisms.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSaslAuth()
  {
    boolean authtypeSasl = false;
    for (String sasl : LdapConstants.SASL_MECHANISMS) {
      if (this.authtype.equalsIgnoreCase(sasl)) {
        authtypeSasl = true;
        break;
      }
    }
    return authtypeSasl;
  }


  /**
   * This returns whether the security authentication context is set to
   * 'EXTERNAL'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isExternalAuth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_EXTERNAL);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'DIGEST-MD5'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isDigestMD5Auth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_DIGEST_MD5);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'CRAM-MD5'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isCramMD5Auth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_CRAM_MD5);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'GSSAPI'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isGSSAPIAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_GSS_API);
  }


  /**
   * See {@link #isAuthoritative()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getAuthoritative()
  {
    return this.isAuthoritative();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to require a
   * authoritative source.
   *
   * @return  <code>boolean</code>
   */
  public boolean isAuthoritative()
  {
    return this.authoritative;
  }


  /**
   * See {@link #isIgnoreCase()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getIgnoreCase()
  {
    return this.isIgnoreCase();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to ignore case in
   * attribute names.
   *
   * @return  <code>boolean</code>
   */
  public boolean isIgnoreCase()
  {
    return this.ignoreCase;
  }


  /**
   * This returns the time limit for the <code>LdapConfig</code>. If this value
   * is 0, then search operations will wait indefinitely for an answer.
   *
   * @return  <code>int</code> - time limit
   */
  public int getTimeLimit()
  {
    int limit = LdapConstants.DEFAULT_TIME_LIMIT;
    if (this.timeLimit != null) {
      limit = this.timeLimit.intValue();
    }
    return limit;
  }


  /**
   * This returns the count limit for the <code>LdapConfig</code>. If this value
   * is 0, then search operations will return all the results it finds.
   *
   * @return  <code>long</code> - count limit
   */
  public long getCountLimit()
  {
    long limit = LdapConstants.DEFAULT_COUNT_LIMIT;
    if (this.countLimit != null) {
      limit = this.countLimit.longValue();
    }
    return limit;
  }


  /**
   * This returns the number of times ldap operations will be retried if a
   * communication exception occurs. If this value is 0, no retries will occur.
   *
   * @return  <code>int</code> - retry count
   */
  public int getOperationRetry()
  {
    int retry = LdapConstants.OPERATION_RETRY;
    if (this.operationRetry != null) {
      retry = this.operationRetry.intValue();
    }
    return retry;
  }


  /**
   * This returns the derefLinkFlag for the <code>LdapConfig</code>.
   *
   * @return  <code>boolean</code>
   */
  public boolean getDerefLinkFlag()
  {
    return this.derefLinkFlag;
  }


  /**
   * This returns the returningObjFlag for the <code>LdapConfig</code>.
   *
   * @return  <code>boolean</code>
   */
  public boolean getReturningObjFlag()
  {
    return this.returningObjFlag;
  }


  /**
   * This returns the batch size for the <code>LdapConfig</code>. If this value
   * is -1, then the default provider setting is being used.
   *
   * @return  <code>int</code> - batch size
   */
  public int getBatchSize()
  {
    int size = LdapConstants.DEFAULT_BATCH_SIZE;
    if (this.batchSize != null) {
      size = this.batchSize.intValue();
    }
    return size;
  }


  /**
   * This returns the dns url for the <code>LdapConfig</code>. If this value is
   * null, then this property is not being used.
   *
   * @return  <code>String</code> - dns url
   */
  public String getDnsUrl()
  {
    return this.dnsUrl;
  }


  /**
   * This returns the preferred language for the <code>LdapConfig</code>. If
   * this value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - language
   */
  public String getLanguage()
  {
    return this.language;
  }


  /**
   * This returns the referral setting for the <code>LdapConfig</code>. If this
   * value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - referral
   */
  public String getReferral()
  {
    return this.referral;
  }


  /**
   * This returns the alias setting for the <code>LdapConfig</code>. If this
   * value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - alias
   */
  public String getDerefAliases()
  {
    return this.derefAliases;
  }


  /**
   * This returns additional binary attributes for the <code>LdapConfig</code>.
   * If this value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - binary attributes
   */
  public String getBinaryAttributes()
  {
    return this.binaryAttributes;
  }


  /**
   * This returns the handlers to use for processing search results.
   *
   * @return  <code>SearchResultHandler[]</code>
   */
  public SearchResultHandler[] getSearchResultHandlers()
  {
    return this.searchResultHandlers;
  }


  /**
   * This returns ths SASL authorization id for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - authorization id
   */
  public String getSaslAuthorizationId()
  {
    return this.saslAuthorizationId;
  }


  /**
   * This returns ths SASL realm for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - realm
   */
  public String getSaslRealm()
  {
    return this.saslRealm;
  }


  /**
   * See {@link #isTypesOnly()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getTypesOnly()
  {
    return this.isTypesOnly();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to only return
   * attribute types.
   *
   * @return  <code>boolean</code>
   */
  public boolean isTypesOnly()
  {
    return this.typesOnly;
  }


  /**
   * This returns any environment properties that may have been set for the
   * <code>LdapConfig</code> using {@link
   * #setEnvironmentProperties(String,String)} that do not represent properties
   * of this config. The collection returned is unmodifiable.
   *
   * @return  <code>Map</code> - additional environment properties
   */
  public Map<String, Object> getEnvironmentProperties()
  {
    return Collections.unmodifiableMap(this.additionalEnvironmentProperties);
  }


  /**
   * This returns whether authentication credentials will be logged.
   *
   * @return  <code>boolean</code> - whether authentication credentials will be
   * logged.
   */
  public boolean getLogCredentials()
  {
    return this.logCredentials;
  }


  /**
   * See {@link #isSslEnabled()}.
   *
   * @return  <code>boolean</code> - whether the SSL protocol is being used
   */
  public boolean getSsl()
  {
    return this.isSslEnabled();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using the SSL protocol
   * for connections.
   *
   * @return  <code>boolean</code> - whether the SSL protocol is being used
   */
  public boolean isSslEnabled()
  {
    return this.ssl;
  }


  /**
   * See {@link #isTlsEnabled()}.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean getTls()
  {
    return this.isTlsEnabled();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using the TLS protocol
   * for connections.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean isTlsEnabled()
  {
    return this.tls;
  }


  /**
   * This sets the context factory of the <code>LdapConfig</code>.
   *
   * @param  contextFactory  <code>String</code> context factory
   */
  public void setContextFactory(final String contextFactory)
  {
    checkImmutable();
    checkStringInput(contextFactory, false);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting contextFactory: " + contextFactory);
    }
    this.contextFactory = contextFactory;
  }


  /**
   * This sets the SSL socket factory of the <code>LdapConfig</code>.
   *
   * @param  sslSocketFactory  <code>SSLSocketFactory</code> SSL socket factory
   */
  public void setSslSocketFactory(final SSLSocketFactory sslSocketFactory)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting sslSocketFactory: " + sslSocketFactory);
    }
    this.sslSocketFactory = sslSocketFactory;
  }


  /**
   * This sets the hostname verifier of the <code>LdapConfig</code>.
   *
   * @param  hostnameVerifier  <code>HostnameVerifier</code> hostname verifier
   */
  public void setHostnameVerifier(final HostnameVerifier hostnameVerifier)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting hostnameVerifier: " + hostnameVerifier);
    }
    this.hostnameVerifier = hostnameVerifier;
  }


  /**
   * This sets the ldap url of the <code>LdapConfig</code>.
   *
   * @param  ldapUrl  <code>String</code> url
   */
  public void setLdapUrl(final String ldapUrl)
  {
    checkImmutable();
    checkStringInput(ldapUrl, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting ldapUrl: " + ldapUrl);
    }
    this.ldapUrl = ldapUrl;
  }


  /**
   * This sets the hostname of the <code>LdapConfig</code>. The host string may
   * be of the form ldap://host.domain.name:389, host.domain.name:389, or
   * host.domain.name. Do not use with {@link #setLdapUrl(String)}.
   *
   * @param  host  <code>String</code> hostname
   *
   * @deprecated  use {@link #setLdapUrl(String)} instead
   */
  @Deprecated
  public void setHost(final String host)
  {
    checkImmutable();
    if (host != null) {
      final int prefixLength = LdapConstants.PROVIDER_URL_PREFIX.length();
      final int separatorLength = LdapConstants.PROVIDER_URL_SEPARATOR.length();
      String h = host;

      // if host contains '://' and there is data after it, remove the scheme
      if (
        h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) != -1 &&
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) + prefixLength <
          h.length()) {
        final String scheme = h.substring(
          0,
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX));
        if (scheme.equalsIgnoreCase(LdapConstants.PROVIDER_URL_SSL_SCHEME)) {
          this.setSsl(true);
          this.setPort(LdapConstants.DEFAULT_SSL_PORT);
        }
        h = h.substring(
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) + prefixLength,
          h.length());
      }

      // if host contains ':' and there is data after it, remove the port
      if (
        h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) != -1 &&
          h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) + separatorLength <
          h.length()) {
        final String p = h.substring(
          h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) + separatorLength,
          h.length());
        this.setPort(p);
        h = h.substring(0, h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR));
      }

      this.host = h;
      this.setLdapUrl(
        LdapConstants.PROVIDER_URL_SCHEME + LdapConstants.PROVIDER_URL_PREFIX +
        this.host + LdapConstants.PROVIDER_URL_SEPARATOR + this.port);
    }
  }


  /**
   * This sets the port of the <code>LdapConfig</code>. Do not use with {@link
   * #setLdapUrl(String)}.
   *
   * @param  port  <code>String</code> port
   *
   * @deprecated  use {@link #setLdapUrl(String)} instead
   */
  @Deprecated
  public void setPort(final String port)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting port: " + port);
    }
    this.port = port;
  }


  /**
   * This sets the maximum amount of time in milliseconds that connect
   * operations will block.
   *
   * @param  timeout  <code>int</code>
   */
  public void setTimeout(final int timeout)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting timeout: " + timeout);
    }
    this.timeout = new Integer(timeout);
  }


  /**
   * This sets the username of the service user. user must be a fully qualified
   * DN.
   *
   * @param  user  <code>String</code> username
   */
  public void setServiceUser(final String user)
  {
    checkImmutable();
    checkStringInput(user, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting serviceUser: " + user);
    }
    this.serviceUser = user;
  }


  /**
   * This sets the credential of the service user.
   *
   * @param  credential  <code>Object</code>
   */
  public void setServiceCredential(final Object credential)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      if (this.getLogCredentials()) {
        this.logger.trace("setting serviceCredential: " + credential);
      } else {
        this.logger.trace("setting serviceCredential: <suppressed>");
      }
    }
    this.serviceCredential = credential;
  }


  /**
   * This sets the username and credential of the service user. user must be a
   * fully qualified DN.
   *
   * @param  user  <code>String</code> service user dn
   * @param  credential  <code>Object</code>
   */
  public void setService(final String user, final Object credential)
  {
    checkImmutable();
    checkStringInput(user, true);
    this.setServiceUser(user);
    this.setServiceCredential(credential);
  }


  /**
   * This sets the base dn for the <code>LdapConfig</code>.
   *
   * @param  base  <code>String</code> base dn
   */
  public void setBase(final String base)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting base: " + base);
    }
    this.base = base;
  }


  /**
   * This sets the search scope for the <code>LdapConfig</code>.
   *
   * @param  searchScope  <code>SearchScope</code>
   */
  public void setSearchScope(final SearchScope searchScope)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting searchScope: " + searchScope);
    }
    this.searchScope = searchScope;
  }


  /**
   * This sets the security level for the <code>LdapConfig</code>.
   *
   * @param  authtype  <code>String</code> security level
   */
  public void setAuthtype(final String authtype)
  {
    checkImmutable();
    checkStringInput(authtype, false);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authtype: " + authtype);
    }
    this.authtype = authtype;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * require an authoritative source.
   *
   * @param  authoritative  <code>boolean</code>
   */
  public void setAuthoritative(final boolean authoritative)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting authoritative: " + authoritative);
    }
    this.authoritative = authoritative;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * ignore case for attributes names.
   *
   * @param  ignoreCase  <code>boolean</code>
   */
  public void setIgnoreCase(final boolean ignoreCase)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting ignoreCase: " + ignoreCase);
    }
    this.ignoreCase = ignoreCase;
  }


  /**
   * This sets the maximum amount of time in milliseconds that search operations
   * will block.
   *
   * @param  timeLimit  <code>int</code>
   */
  public void setTimeLimit(final int timeLimit)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting timeLimit: " + timeLimit);
    }
    this.timeLimit = new Integer(timeLimit);
  }


  /**
   * This sets the maximum number of entries that search operations will return.
   *
   * @param  countLimit  <code>long</code>
   */
  public void setCountLimit(final long countLimit)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting countLimit: " + countLimit);
    }
    this.countLimit = new Long(countLimit);
  }


  /**
   * This sets the number of times that ldap operations will be retried if a
   * communication exception occurs.
   *
   * @param  operationRetry  <code>int</code>
   */
  public void setOperationRetry(final int operationRetry)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting operationRetry: " + operationRetry);
    }
    this.operationRetry = new Integer(operationRetry);
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to link
   * dereferencing during searches.
   *
   * @param  derefLinkFlag  <code>boolean</code>
   */
  public void setDerefLinkFlag(final boolean derefLinkFlag)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting derefLinkFlag: " + derefLinkFlag);
    }
    this.derefLinkFlag = derefLinkFlag;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * return objects for searches.
   *
   * @param  returningObjFlag  <code>boolean</code>
   */
  public void setReturningObjFlag(final boolean returningObjFlag)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting returningObjFlag: " + returningObjFlag);
    }
    this.returningObjFlag = returningObjFlag;
  }


  /**
   * This sets the batch size for the <code>LdapConfig</code>. A value of -1
   * indicates to use the provider default.
   *
   * @param  batchSize  <code>int</code> batch size to use when returning
   * results
   */
  public void setBatchSize(final int batchSize)
  {
    checkImmutable();
    if (batchSize == -1) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting batchSize: " + null);
      }
      this.batchSize = null;
    } else {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("setting batchSize: " + batchSize);
      }
      this.batchSize = new Integer(batchSize);
    }
  }


  /**
   * This sets the dns url for the <code>LdapConfig</code>.
   *
   * @param  dnsUrl  <code>String</code>
   */
  public void setDnsUrl(final String dnsUrl)
  {
    checkImmutable();
    checkStringInput(dnsUrl, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting dnsUrl: " + dnsUrl);
    }
    this.dnsUrl = dnsUrl;
  }


  /**
   * This sets the preferred language for the <code>LdapConfig</code>.
   *
   * @param  language  <code>String</code> defined by RFC 1766
   */
  public void setLanguage(final String language)
  {
    checkImmutable();
    checkStringInput(language, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting language: " + language);
    }
    this.language = language;
  }


  /**
   * This specifies how the <code>LdapConfig</code> should handle referrals.
   * referral must be one of: "throw", "ignore", or "follow".
   *
   * @param  referral  <code>String</code> defined by RFC 1766
   */
  public void setReferral(final String referral)
  {
    checkImmutable();
    checkStringInput(referral, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting referral: " + referral);
    }
    this.referral = referral;
  }


  /**
   * This specifies how the <code>LdapConfig</code> should handle aliases.
   * derefAliases must be one of: "always", "never", "finding", or "searching".
   *
   * @param  derefAliases  <code>String</code>
   */
  public void setDerefAliases(final String derefAliases)
  {
    checkImmutable();
    checkStringInput(derefAliases, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting derefAliases: " + derefAliases);
    }
    this.derefAliases = derefAliases;
  }


  /**
   * This specifies additional attributes that should be considered binary.
   * Attributes should be space delimited.
   *
   * @param  binaryAttributes  <code>String</code>
   */
  public void setBinaryAttributes(final String binaryAttributes)
  {
    checkImmutable();
    checkStringInput(binaryAttributes, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting binaryAttributes: " + binaryAttributes);
    }
    this.binaryAttributes = binaryAttributes;
  }


  /**
   * This sets the handlers for processing search results.
   *
   * @param  handlers  <code>SearchResultHandler[]</code>
   */
  public void setSearchResultHandlers(final SearchResultHandler[] handlers)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace(
        "setting searchResultsHandlers: " +
        (handlers == null ? "null" : Arrays.asList(handlers)));
    }
    this.searchResultHandlers = handlers;
  }


  /**
   * This specifies a SASL authorization id.
   *
   * @param  saslAuthorizationId  <code>String</code>
   */
  public void setSaslAuthorizationId(final String saslAuthorizationId)
  {
    checkImmutable();
    checkStringInput(saslAuthorizationId, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting saslAuthorizationId: " + saslAuthorizationId);
    }
    this.saslAuthorizationId = saslAuthorizationId;
  }


  /**
   * This specifies a SASL realm.
   *
   * @param  saslRealm  <code>String</code>
   */
  public void setSaslRealm(final String saslRealm)
  {
    checkImmutable();
    checkStringInput(saslRealm, true);
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting saslRealm: " + saslRealm);
    }
    this.saslRealm = saslRealm;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * return only attribute types.
   *
   * @param  typesOnly  <code>boolean</code>
   */
  public void setTypesOnly(final boolean typesOnly)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting typesOnly: " + typesOnly);
    }
    this.typesOnly = typesOnly;
  }


  /** {@inheritDoc}. */
  public String getPropertiesDomain()
  {
    return PROPERTIES_DOMAIN;
  }


  /** {@inheritDoc}. */
  public void setEnvironmentProperties(final String name, final String value)
  {
    checkImmutable();
    if (name != null && value != null) {
      if (PROPERTIES.hasProperty(name)) {
        PROPERTIES.setProperty(this, name, value);
      } else {
        if (this.logger.isTraceEnabled()) {
          this.logger.trace("setting property " + name + ": " + value);
        }
        this.additionalEnvironmentProperties.put(name, value);
      }
    }
  }


  /** {@inheritDoc}. */
  public boolean hasEnvironmentProperty(final String name)
  {
    return PROPERTIES.hasProperty(name);
  }


  /**
   * Create an instance of this class initialized with properties from the input
   * stream. If the input stream is null, load properties from the default
   * properties file.
   *
   * @param  is  to load properties from
   *
   * @return  <code>LdapPoolConfig</code> initialized ldap pool config
   */
  public static LdapConfig createFromProperties(final InputStream is)
  {
    final LdapConfig ldapConfig = new LdapConfig();
    LdapProperties properties = null;
    if (is != null) {
      properties = new LdapProperties(ldapConfig, is);
    } else {
      properties = new LdapProperties(ldapConfig);
      properties.useDefaultPropertiesFile();
    }
    properties.configure();
    return ldapConfig;
  }


  /**
   * This sets whether authentication credentials will be logged.
   *
   * @param  log  <code>boolean</code>
   */
  public void setLogCredentials(final boolean log)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting logCredentials: " + log);
    }
    this.logCredentials = log;
  }


  /**
   * This sets this <code>LdapConfig</code> to use the SSL protocol for
   * connections. The port is automatically changed to the default of 636. If
   * you need to use a different port then you must call {@link #setPort} after
   * calling this method.
   *
   * @param  ssl  <code>boolean</code>
   */
  public void setSsl(final boolean ssl)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting ssl: " + ssl);
    }
    this.ssl = ssl;
  }


  /**
   * This sets this <code>LdapConfig</code> to use the TLS protocol for
   * connections. For fine control over starting and stopping TLS you must use
   * the {@link Ldap#useTls(boolean)} method.
   *
   * @param  tls  <code>boolean</code>
   */
  public void setTls(final boolean tls)
  {
    checkImmutable();
    if (this.logger.isTraceEnabled()) {
      this.logger.trace("setting tls: " + tls);
    }
    this.tls = tls;
  }


  /**
   * This returns a <code>SearchControls</code> object configured with this
   * <code>LdapConfig</code>.
   *
   * @param  retAttrs  <code>String[]</code> attributres to return from search
   *
   * @return  <code>SearchControls</code>
   */
  public SearchControls getSearchControls(final String[] retAttrs)
  {
    final SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(retAttrs);
    ctls.setSearchScope(this.getSearchScope().ordinal());
    ctls.setTimeLimit(this.getTimeLimit());
    ctls.setCountLimit(this.getCountLimit());
    ctls.setDerefLinkFlag(this.getDerefLinkFlag());
    ctls.setReturningObjFlag(this.getReturningObjFlag());
    return ctls;
  }


  /**
   * This returns a <code>SearchControls</code> object configured to perform a
   * LDAP compare operation.
   *
   * @return  <code>SearchControls</code>
   */
  public static SearchControls getCompareSearchControls()
  {
    final SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(new String[0]);
    ctls.setSearchScope(SearchScope.OBJECT.ordinal());
    return ctls;
  }


  /**
   * This sets this <code>LdapConfig</code> to print ASN.1 BER packets to the
   * supplied <code>PrintStream</code>.
   *
   * @param  stream  <code>PrintStream</code>
   */
  public void setTracePackets(final PrintStream stream)
  {
    checkImmutable();
    this.tracePackets = stream;
  }


  /**
   * Provides a descriptive string representation of this instance.
   *
   * @return  String of the form $Classname@hashCode::config=$config.
   */
  @Override
  public String toString()
  {
    return
      String.format(
        "%s@%d::%s",
        this.getClass().getName(),
        this.hashCode(),
        this.getEnvironment());
  }
}
/*
  $Id: LdapConfig.java 282 2009-05-29 21:57:32Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 282 $
  Updated: $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.directory.SearchControls;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import edu.vt.middleware.ldap.props.AbstractPropertyConfig;
import edu.vt.middleware.ldap.props.LdapProperties;
import edu.vt.middleware.ldap.props.PropertyInvoker;

/**
 * <code>LdapConfig</code> contains all the configuration data that the <code>
 * Ldap</code> needs to control connections and searching.
 *
 * @author  Middleware Services
 * @version  $Revision: 282 $ $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
 */
public class LdapConfig extends AbstractPropertyConfig
{


  /** Domain to look for ldap properties in, value is {@value}. */
  public static final String PROPERTIES_DOMAIN = "edu.vt.middleware.ldap.";

  /** Invoker for ldap properties. */
  private static final PropertyInvoker PROPERTIES = new PropertyInvoker(
    LdapConfig.class,
    PROPERTIES_DOMAIN);


  /**
   * Enum to define the type of search scope. See {@link
   * javax.naming.directory.SearchControls}.
   */
  public enum SearchScope {

    /** object level search. */
    OBJECT,

    /** one level search. */
    ONELEVEL,

    /** subtree search. */
    SUBTREE;


    /**
     * Method to convert a JNDI constant value to an enum. Returns null if the
     * supplied constant does not match a known value.
     *
     * @param  i  jndi constant
     *
     * @return  search scope
     */
    public static SearchScope parseSearchScope(final int i)
    {
      SearchScope ss = null;
      if (OBJECT.ordinal() == i) {
        ss = OBJECT;
      } else if (ONELEVEL.ordinal() == i) {
        ss = ONELEVEL;
      } else if (SUBTREE.ordinal() == i) {
        ss = SUBTREE;
      }
      return ss;
    }
  }

  /** Default context factory. */
  private String contextFactory = LdapConstants.DEFAULT_CONTEXT_FACTORY;

  /** Default ldap socket factory used for SSL and TLS. */
  private SSLSocketFactory sslSocketFactory;

  /** Default hostname verifier for TLS connections. */
  private HostnameVerifier hostnameVerifier;

  /** URL to the LDAP(s). */
  private String ldapUrl;

  /** Hostname of the LDAP server. */
  private String host;

  /** Port the LDAP server is listening on. */
  private String port = LdapConstants.DEFAULT_PORT;

  /** Amount of time in milliseconds that connect operations will block. */
  private Integer timeout;

  /** Username for a service user, this must be a fully qualified DN. */
  private String serviceUser;

  /** Credential for a service user. */
  private Object serviceCredential;

  /** Base dn for LDAP searching. */
  private String base;

  /** Type of search scope to use, default is subtree. */
  private SearchScope searchScope = SearchScope.SUBTREE;

  /** Security level to use when binding to the LDAP. */
  private String authtype = LdapConstants.DEFAULT_AUTHTYPE;

  /** Whether to require the most authoritative source for this service. */
  private boolean authoritative = LdapConstants.DEFAULT_AUTHORITATIVE;

  /** Whether to ignore case in attribute names. */
  private boolean ignoreCase = LdapConstants.DEFAULT_IGNORE_CASE;

  /** Preferred batch size to use when returning results. */
  private Integer batchSize;

  /** Amount of time in milliseconds that search operations will block. */
  private Integer timeLimit;

  /** Maximum number of entries that search operations will return. */
  private Long countLimit;

  /** Number of times to retry ldap operations on communication exception. */
  private Integer operationRetry;

  /** Whether link dereferencing should be performed during the search. */
  private boolean derefLinkFlag;

  /** Whether objects will be returned in the result. */
  private boolean returningObjFlag;

  /** DNS host to use for JNDI URL context implementation. */
  private String dnsUrl;

  /** Preferred language as defined by RFC 1766. */
  private String language;

  /** How the provider should handle referrals. */
  private String referral;

  /** How the provider should handle aliases. */
  private String derefAliases;

  /** Additional attributes that should be considered binary. */
  private String binaryAttributes;

  /** SASL authorization ID. */
  private String saslAuthorizationId;

  /** SASL realm. */
  private String saslRealm;

  /** Whether only attribute type names should be returned. */
  private boolean typesOnly = LdapConstants.DEFAULT_TYPES_ONLY;

  /** Additional environment properties. */
  private Map<String, Object> additionalEnvironmentProperties =
    new HashMap<String, Object>();

  /** Whether to log authentication credentials. */
  private boolean logCredentials = LdapConstants.DEFAULT_LOG_CREDENTIALS;

  /** Connect to LDAP using SSL protocol. */
  private boolean ssl = LdapConstants.DEFAULT_USE_SSL;

  /** Connect to LDAP using TLS protocol. */
  private boolean tls = LdapConstants.DEFAULT_USE_TLS;

  /** Stream to print LDAP ASN.1 BER packets. */
  private PrintStream tracePackets;


  /** Default constructor. */
  public LdapConfig() {}


  /**
   * This will create a new <code>LdapConfig</code> with the supplied ldap url
   * and base Strings.
   *
   * @param  ldapUrl  <code>String</code> LDAP URL
   * @param  base  <code>String</code> LDAP base DN
   */
  public LdapConfig(final String ldapUrl, final String base)
  {
    this();
    this.setLdapUrl(ldapUrl);
    this.setBase(base);
  }


  /**
   * This returns the Context environment properties that are used to make LDAP
   * connections.
   *
   * @return  <code>Hashtable</code> - context environment
   */
  public Hashtable<String, ?> getEnvironment()
  {
    final Hashtable<String, Object> environment =
      new Hashtable<String, Object>();
    environment.put(LdapConstants.CONTEXT_FACTORY, this.contextFactory);

    if (this.authoritative) {
      environment.put(
        LdapConstants.AUTHORITATIVE,
        Boolean.valueOf(this.authoritative).toString());
    }

    if (this.batchSize != null) {
      environment.put(LdapConstants.BATCH_SIZE, this.batchSize.toString());
    }

    if (this.dnsUrl != null) {
      environment.put(LdapConstants.DNS_URL, this.dnsUrl);
    }

    if (this.language != null) {
      environment.put(LdapConstants.LANGUAGE, this.language);
    }

    if (this.referral != null) {
      environment.put(LdapConstants.REFERRAL, this.referral);
    }

    if (this.derefAliases != null) {
      environment.put(LdapConstants.DEREF_ALIASES, this.derefAliases);
    }

    if (this.binaryAttributes != null) {
      environment.put(LdapConstants.BINARY_ATTRIBUTES, this.binaryAttributes);
    }

    if (this.saslAuthorizationId != null) {
      environment.put(
        LdapConstants.SASL_AUTHORIZATION_ID,
        this.saslAuthorizationId);
    }

    if (this.saslRealm != null) {
      environment.put(LdapConstants.SASL_REALM, this.saslRealm);
    }

    if (this.typesOnly) {
      environment.put(
        LdapConstants.TYPES_ONLY,
        Boolean.valueOf(this.typesOnly).toString());
    }

    if (this.ssl) {
      environment.put(LdapConstants.PROTOCOL, LdapConstants.SSL_PROTOCOL);
      if (this.sslSocketFactory != null) {
        environment.put(
          LdapConstants.SOCKET_FACTORY,
          this.sslSocketFactory.getClass().getName());
      }
    }

    if (this.tracePackets != null) {
      environment.put(LdapConstants.TRACE, this.tracePackets);
    }

    if (this.ldapUrl != null) {
      environment.put(LdapConstants.PROVIDER_URL, this.ldapUrl);
    }

    if (this.timeout != null) {
      environment.put(LdapConstants.TIMEOUT, this.timeout.toString());
    }

    if (!this.additionalEnvironmentProperties.isEmpty()) {
      for (
        Map.Entry<String, Object> entry :
          this.additionalEnvironmentProperties.entrySet()) {
        environment.put(entry.getKey(), entry.getValue());
      }
    }

    return environment;
  }


  /**
   * This returns the context factory of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - context factory
   */
  public String getContextFactory()
  {
    return this.contextFactory;
  }


  /**
   * This returns the SSL socket factory of the <code>LdapConfig</code>.
   *
   * @return  <code>SSLSocketFactory</code> - SSL socket factory
   */
  public SSLSocketFactory getSslSocketFactory()
  {
    return this.sslSocketFactory;
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using a custom SSL
   * socket factory.
   *
   * @return  <code>boolean</code>
   */
  public boolean useSslSocketFactory()
  {
    return this.sslSocketFactory != null;
  }


  /**
   * This returns the hostname verifier of the <code>LdapConfig</code>.
   *
   * @return  <code>HostnameVerifier</code> - hostname verifier
   */
  public HostnameVerifier getHostnameVerifier()
  {
    return this.hostnameVerifier;
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using a custom hostname
   * verifier.
   *
   * @return  <code>boolean</code>
   */
  public boolean useHostnameVerifier()
  {
    return this.hostnameVerifier != null;
  }


  /**
   * This returns the ldap url of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - ldap url
   */
  public String getLdapUrl()
  {
    return this.ldapUrl;
  }


  /**
   * This returns the hostname of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - hostname
   *
   * @deprecated  use {@link #getLdapUrl()} instead
   */
  @Deprecated
  public String getHost()
  {
    return this.host;
  }


  /**
   * This returns the port of the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - port
   *
   * @deprecated  use {@link #getLdapUrl()} instead
   */
  @Deprecated
  public String getPort()
  {
    return this.port;
  }


  /**
   * This returns the timeout for the <code>LdapConfig</code>. If this value is
   * 0, then connect operations will wait indefinitely.
   *
   * @return  <code>int</code> - timeout
   */
  public int getTimeout()
  {
    int time = LdapConstants.DEFAULT_TIMEOUT;
    if (this.timeout != null) {
      time = this.timeout.intValue();
    }
    return time;
  }


  /**
   * This returns the username of the service user.
   *
   * @return  <code>String</code> - username
   */
  public String getServiceUser()
  {
    return this.serviceUser;
  }


  /**
   * This returns the credential of the service user.
   *
   * @return  <code>Object</code> - credential
   */
  public Object getServiceCredential()
  {
    return this.serviceCredential;
  }


  /**
   * This returns the base dn for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - base dn
   */
  public String getBase()
  {
    return this.base;
  }


  /**
   * This returns the search scope for the <code>LdapConfig</code>.
   *
   * @return  <code>SearchScope</code> - search scope
   */
  public SearchScope getSearchScope()
  {
    return this.searchScope;
  }


  /**
   * This returns whether the search scope is set to object.
   *
   * @return  <code>boolean</code>
   */
  public boolean isObjectSearchScope()
  {
    return this.searchScope == SearchScope.OBJECT;
  }


  /**
   * This returns whether the search scope is set to one level.
   *
   * @return  <code>boolean</code>
   */
  public boolean isOneLevelSearchScope()
  {
    return this.searchScope == SearchScope.ONELEVEL;
  }


  /**
   * This returns whether the search scope is set to sub tree.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSubTreeSearchScope()
  {
    return this.searchScope == SearchScope.SUBTREE;
  }


  /**
   * This returns the security level for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - security level
   */
  public String getAuthtype()
  {
    return this.authtype;
  }


  /**
   * This returns whether the security authentication context is set to 'none'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isAnonymousAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.NONE_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'simple'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSimpleAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.SIMPLE_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'strong'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isStrongAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.STRONG_AUTHTYPE);
  }


  /**
   * This returns whether the security authentication context will perform a
   * SASL bind as defined by the supported SASL mechanisms.
   *
   * @return  <code>boolean</code>
   */
  public boolean isSaslAuth()
  {
    boolean authtypeSasl = false;
    for (String sasl : LdapConstants.SASL_MECHANISMS) {
      if (this.authtype.equalsIgnoreCase(sasl)) {
        authtypeSasl = true;
        break;
      }
    }
    return authtypeSasl;
  }


  /**
   * This returns whether the security authentication context is set to
   * 'EXTERNAL'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isExternalAuth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_EXTERNAL);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'DIGEST-MD5'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isDigestMD5Auth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_DIGEST_MD5);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'CRAM-MD5'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isCramMD5Auth()
  {
    return
      this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_CRAM_MD5);
  }


  /**
   * This returns whether the security authentication context is set to
   * 'GSSAPI'.
   *
   * @return  <code>boolean</code>
   */
  public boolean isGSSAPIAuth()
  {
    return this.authtype.equalsIgnoreCase(LdapConstants.SASL_MECHANISM_GSS_API);
  }


  /**
   * See {@link #isAuthoritative()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getAuthoritative()
  {
    return this.isAuthoritative();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to require a
   * authoritative source.
   *
   * @return  <code>boolean</code>
   */
  public boolean isAuthoritative()
  {
    return this.authoritative;
  }


  /**
   * See {@link #isIgnoreCase()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getIgnoreCase()
  {
    return this.isIgnoreCase();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to ignore case in
   * attribute names.
   *
   * @return  <code>boolean</code>
   */
  public boolean isIgnoreCase()
  {
    return this.ignoreCase;
  }


  /**
   * This returns the time limit for the <code>LdapConfig</code>. If this value
   * is 0, then search operations will wait indefinitely for an answer.
   *
   * @return  <code>int</code> - time limit
   */
  public int getTimeLimit()
  {
    int limit = LdapConstants.DEFAULT_TIME_LIMIT;
    if (this.timeLimit != null) {
      limit = this.timeLimit.intValue();
    }
    return limit;
  }


  /**
   * This returns the count limit for the <code>LdapConfig</code>. If this value
   * is 0, then search operations will return all the results it finds.
   *
   * @return  <code>long</code> - count limit
   */
  public long getCountLimit()
  {
    long limit = LdapConstants.DEFAULT_COUNT_LIMIT;
    if (this.countLimit != null) {
      limit = this.countLimit.longValue();
    }
    return limit;
  }


  /**
   * This returns the number of times ldap operations will be retried if a
   * communication exception occurs. If this value is 0, no retries will occur.
   *
   * @return  <code>int</code> - retry count
   */
  public int getOperationRetry()
  {
    int retry = LdapConstants.OPERATION_RETRY;
    if (this.operationRetry != null) {
      retry = this.operationRetry.intValue();
    }
    return retry;
  }


  /**
   * This returns the derefLinkFlag for the <code>LdapConfig</code>.
   *
   * @return  <code>boolean</code>
   */
  public boolean getDerefLinkFlag()
  {
    return this.derefLinkFlag;
  }


  /**
   * This returns the returningObjFlag for the <code>LdapConfig</code>.
   *
   * @return  <code>boolean</code>
   */
  public boolean getReturningObjFlag()
  {
    return this.returningObjFlag;
  }


  /**
   * This returns the batch size for the <code>LdapConfig</code>. If this value
   * is -1, then the default provider setting is being used.
   *
   * @return  <code>int</code> - batch size
   */
  public int getBatchSize()
  {
    int size = LdapConstants.DEFAULT_BATCH_SIZE;
    if (this.batchSize != null) {
      size = this.batchSize.intValue();
    }
    return size;
  }


  /**
   * This returns the dns url for the <code>LdapConfig</code>. If this value is
   * null, then this property is not being used.
   *
   * @return  <code>String</code> - dns url
   */
  public String getDnsUrl()
  {
    return this.dnsUrl;
  }


  /**
   * This returns the preferred language for the <code>LdapConfig</code>. If
   * this value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - language
   */
  public String getLanguage()
  {
    return this.language;
  }


  /**
   * This returns the referral setting for the <code>LdapConfig</code>. If this
   * value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - referral
   */
  public String getReferral()
  {
    return this.referral;
  }


  /**
   * This returns the alias setting for the <code>LdapConfig</code>. If this
   * value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - alias
   */
  public String getDerefAliases()
  {
    return this.derefAliases;
  }


  /**
   * This returns additional binary attributes for the <code>LdapConfig</code>.
   * If this value is null, then the default provider setting is being used.
   *
   * @return  <code>String</code> - binary attributes
   */
  public String getBinaryAttributes()
  {
    return this.binaryAttributes;
  }


  /**
   * This returns ths SASL authorization id for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - authorization id
   */
  public String getSaslAuthorizationId()
  {
    return this.saslAuthorizationId;
  }


  /**
   * This returns ths SASL realm for the <code>LdapConfig</code>.
   *
   * @return  <code>String</code> - realm
   */
  public String getSaslRealm()
  {
    return this.saslRealm;
  }


  /**
   * See {@link #isTypesOnly()}.
   *
   * @return  <code>boolean</code>
   */
  public boolean getTypesOnly()
  {
    return this.isTypesOnly();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is set to only return
   * attribute types.
   *
   * @return  <code>boolean</code>
   */
  public boolean isTypesOnly()
  {
    return this.typesOnly;
  }


  /**
   * This returns any environment properties that may have been set for the
   * <code>LdapConfig</code> using {@link
   * #setEnvironmentProperties(String,String)} that do not represent properties
   * of this config. The collection returned is unmodifiable.
   *
   * @return  <code>Map</code> - additional environment properties
   */
  public Map<String, Object> getEnvironmentProperties()
  {
    return Collections.unmodifiableMap(this.additionalEnvironmentProperties);
  }


  /**
   * This returns whether authentication credentials will be logged.
   *
   * @return  <code>boolean</code> - whether authentication credentials will be
   * logged.
   */
  public boolean getLogCredentials()
  {
    return this.logCredentials;
  }


  /**
   * See {@link #isSslEnabled()}.
   *
   * @return  <code>boolean</code> - whether the SSL protocol is being used
   */
  public boolean getSsl()
  {
    return this.isSslEnabled();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using the SSL protocol
   * for connections.
   *
   * @return  <code>boolean</code> - whether the SSL protocol is being used
   */
  public boolean isSslEnabled()
  {
    return this.ssl;
  }


  /**
   * See {@link #isTlsEnabled()}.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean getTls()
  {
    return this.isTlsEnabled();
  }


  /**
   * This returns whether the <code>LdapConfig</code> is using the TLS protocol
   * for connections.
   *
   * @return  <code>boolean</code> - whether the TLS protocol is being used
   */
  public boolean isTlsEnabled()
  {
    return this.tls;
  }


  /**
   * This sets the context factory of the <code>LdapConfig</code>.
   *
   * @param  contextFactory  <code>String</code> context factory
   */
  public void setContextFactory(final String contextFactory)
  {
    checkImmutable();
    checkStringInput(contextFactory, false);
    this.contextFactory = contextFactory;
  }


  /**
   * This sets the SSL socket factory of the <code>LdapConfig</code>.
   *
   * @param  sslSocketFactory  <code>SSLSocketFactory</code> SSL socket factory
   */
  public void setSslSocketFactory(final SSLSocketFactory sslSocketFactory)
  {
    checkImmutable();
    this.sslSocketFactory = sslSocketFactory;
  }


  /**
   * This sets the hostname verifier of the <code>LdapConfig</code>.
   *
   * @param  hostnameVerifier  <code>HostnameVerifier</code> hostname verifier
   */
  public void setHostnameVerifier(final HostnameVerifier hostnameVerifier)
  {
    checkImmutable();
    this.hostnameVerifier = hostnameVerifier;
  }


  /**
   * This sets the ldap url of the <code>LdapConfig</code>.
   *
   * @param  ldapUrl  <code>String</code> url
   */
  public void setLdapUrl(final String ldapUrl)
  {
    checkImmutable();
    checkStringInput(ldapUrl, true);
    this.ldapUrl = ldapUrl;
  }


  /**
   * This sets the hostname of the <code>LdapConfig</code>. The host string may
   * be of the form ldap://host.domain.name:389, host.domain.name:389, or
   * host.domain.name. Do not use with {@link #setLdapUrl(String)}.
   *
   * @param  host  <code>String</code> hostname
   *
   * @deprecated  use {@link #setLdapUrl(String)} instead
   */
  @Deprecated
  public void setHost(final String host)
  {
    checkImmutable();
    if (host != null) {
      final int prefixLength = LdapConstants.PROVIDER_URL_PREFIX.length();
      final int separatorLength = LdapConstants.PROVIDER_URL_SEPARATOR.length();
      String h = host;

      // if host contains '://' and there is data after it, remove the scheme
      if (
        h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) != -1 &&
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) + prefixLength <
          h.length()) {
        final String scheme = h.substring(
          0,
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX));
        if (scheme.equalsIgnoreCase(LdapConstants.PROVIDER_URL_SSL_SCHEME)) {
          this.setSsl(true);
          this.setPort(LdapConstants.DEFAULT_SSL_PORT);
        }
        h = h.substring(
          h.indexOf(LdapConstants.PROVIDER_URL_PREFIX) + prefixLength,
          h.length());
      }

      // if host contains ':' and there is data after it, remove the port
      if (
        h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) != -1 &&
          h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) + separatorLength <
          h.length()) {
        final String p = h.substring(
          h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR) + separatorLength,
          h.length());
        this.setPort(p);
        h = h.substring(0, h.indexOf(LdapConstants.PROVIDER_URL_SEPARATOR));
      }

      this.host = h;
      this.setLdapUrl(
        LdapConstants.PROVIDER_URL_SCHEME + LdapConstants.PROVIDER_URL_PREFIX +
        this.host + LdapConstants.PROVIDER_URL_SEPARATOR + this.port);
    }
  }


  /**
   * This sets the port of the <code>LdapConfig</code>. Do not use with {@link
   * #setLdapUrl(String)}.
   *
   * @param  port  <code>String</code> port
   *
   * @deprecated  use {@link #setLdapUrl(String)} instead
   */
  @Deprecated
  public void setPort(final String port)
  {
    checkImmutable();
    this.port = port;
  }


  /**
   * This sets the maximum amount of time in milliseconds that connect
   * operations will block.
   *
   * @param  timeout  <code>int</code>
   */
  public void setTimeout(final int timeout)
  {
    checkImmutable();
    this.timeout = new Integer(timeout);
  }


  /**
   * This sets the username of the service user. user must be a fully qualified
   * DN.
   *
   * @param  user  <code>String</code> username
   */
  public void setServiceUser(final String user)
  {
    checkImmutable();
    checkStringInput(user, true);
    this.serviceUser = user;
  }


  /**
   * This sets the credential of the service user.
   *
   * @param  credential  <code>Object</code>
   */
  public void setServiceCredential(final Object credential)
  {
    checkImmutable();
    this.serviceCredential = credential;
  }


  /**
   * This sets the username and credential of the service user. user must be a
   * fully qualified DN.
   *
   * @param  user  <code>String</code> service user dn
   * @param  credential  <code>Object</code>
   */
  public void setService(final String user, final Object credential)
  {
    checkImmutable();
    checkStringInput(user, true);
    this.setServiceUser(user);
    this.setServiceCredential(credential);
  }


  /**
   * This sets the base dn for the <code>LdapConfig</code>.
   *
   * @param  base  <code>String</code> base dn
   */
  public void setBase(final String base)
  {
    checkImmutable();
    this.base = base;
  }


  /**
   * This sets the search scope for the <code>LdapConfig</code>.
   *
   * @param  searchScope  <code>SearchScope</code>
   */
  public void setSearchScope(final SearchScope searchScope)
  {
    checkImmutable();
    this.searchScope = searchScope;
  }


  /**
   * This sets the security level for the <code>LdapConfig</code>.
   *
   * @param  authtype  <code>String</code> security level
   */
  public void setAuthtype(final String authtype)
  {
    checkImmutable();
    checkStringInput(authtype, false);
    this.authtype = authtype;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * require an authoritative source.
   *
   * @param  authoritative  <code>boolean</code>
   */
  public void setAuthoritative(final boolean authoritative)
  {
    checkImmutable();
    this.authoritative = authoritative;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * ignore case for attributes names.
   *
   * @param  ignoreCase  <code>boolean</code>
   */
  public void setIgnoreCase(final boolean ignoreCase)
  {
    checkImmutable();
    this.ignoreCase = ignoreCase;
  }


  /**
   * This sets the maximum amount of time in milliseconds that search operations
   * will block.
   *
   * @param  timeLimit  <code>int</code>
   */
  public void setTimeLimit(final int timeLimit)
  {
    checkImmutable();
    this.timeLimit = new Integer(timeLimit);
  }


  /**
   * This sets the maximum number of entries that search operations will return.
   *
   * @param  countLimit  <code>long</code>
   */
  public void setCountLimit(final long countLimit)
  {
    checkImmutable();
    this.countLimit = new Long(countLimit);
  }


  /**
   * This sets the number of times that ldap operations will be retried if a
   * communication exception occurs.
   *
   * @param  operationRetry  <code>int</code>
   */
  public void setOperationRetry(final int operationRetry)
  {
    checkImmutable();
    this.operationRetry = new Integer(operationRetry);
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to link
   * dereferencing during searches.
   *
   * @param  derefLinkFlag  <code>boolean</code>
   */
  public void setDerefLinkFlag(final boolean derefLinkFlag)
  {
    checkImmutable();
    this.derefLinkFlag = derefLinkFlag;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * return objects for searches.
   *
   * @param  returningObjFlag  <code>boolean</code>
   */
  public void setReturningObjFlag(final boolean returningObjFlag)
  {
    checkImmutable();
    this.returningObjFlag = returningObjFlag;
  }


  /**
   * This sets the batch size for the <code>LdapConfig</code>. A value of -1
   * indicates to use the provider default.
   *
   * @param  batchSize  <code>int</code> batch size to use when returning
   * results
   */
  public void setBatchSize(final int batchSize)
  {
    checkImmutable();
    if (batchSize == -1) {
      this.batchSize = null;
    } else {
      this.batchSize = new Integer(batchSize);
    }
  }


  /**
   * This sets the dns url for the <code>LdapConfig</code>.
   *
   * @param  dnsUrl  <code>String</code>
   */
  public void setDnsUrl(final String dnsUrl)
  {
    checkImmutable();
    checkStringInput(dnsUrl, true);
    this.dnsUrl = dnsUrl;
  }


  /**
   * This sets the preferred language for the <code>LdapConfig</code>.
   *
   * @param  language  <code>String</code> defined by RFC 1766
   */
  public void setLanguage(final String language)
  {
    checkImmutable();
    checkStringInput(language, true);
    this.language = language;
  }


  /**
   * This specifies how the <code>LdapConfig</code> should handle referrals.
   * referral must be one of: "throw", "ignore", or "follow".
   *
   * @param  referral  <code>String</code> defined by RFC 1766
   */
  public void setReferral(final String referral)
  {
    checkImmutable();
    checkStringInput(referral, true);
    this.referral = referral;
  }


  /**
   * This specifies how the <code>LdapConfig</code> should handle aliases.
   * derefAliases must be one of: "always", "never", "finding", or "searching".
   *
   * @param  derefAliases  <code>String</code>
   */
  public void setDerefAliases(final String derefAliases)
  {
    checkImmutable();
    checkStringInput(derefAliases, true);
    this.derefAliases = derefAliases;
  }


  /**
   * This specifies additional attributes that should be considered binary.
   * Attributes should be space delimited.
   *
   * @param  binaryAttributes  <code>String</code>
   */
  public void setBinaryAttributes(final String binaryAttributes)
  {
    checkImmutable();
    checkStringInput(binaryAttributes, true);
    this.binaryAttributes = binaryAttributes;
  }


  /**
   * This specifies a SASL authorization id.
   *
   * @param  saslAuthorizationId  <code>String</code>
   */
  public void setSaslAuthorizationId(final String saslAuthorizationId)
  {
    checkImmutable();
    checkStringInput(saslAuthorizationId, true);
    this.saslAuthorizationId = saslAuthorizationId;
  }


  /**
   * This specifies a SASL realm.
   *
   * @param  saslRealm  <code>String</code>
   */
  public void setSaslRealm(final String saslRealm)
  {
    checkImmutable();
    checkStringInput(saslRealm, true);
    this.saslRealm = saslRealm;
  }


  /**
   * This specifies whether or not to force this <code>LdapConfig</code> to
   * return only attribute types.
   *
   * @param  typesOnly  <code>boolean</code>
   */
  public void setTypesOnly(final boolean typesOnly)
  {
    checkImmutable();
    this.typesOnly = typesOnly;
  }


  /** {@inheritDoc}. */
  public String getPropertiesDomain()
  {
    return PROPERTIES_DOMAIN;
  }


  /** {@inheritDoc}. */
  public void setEnvironmentProperties(final String name, final String value)
  {
    checkImmutable();
    if (name != null && value != null) {
      if (PROPERTIES.hasProperty(name)) {
        PROPERTIES.setProperty(this, name, value);
      } else {
        this.additionalEnvironmentProperties.put(name, value);
      }
    }
  }


  /** {@inheritDoc}. */
  public boolean hasEnvironmentProperty(final String name)
  {
    return PROPERTIES.hasProperty(name);
  }


  /**
   * Create an instance of this class initialized with properties from the
   * properties file. If propertiesFile is null, load properties from the
   * default properties file.
   *
   * @param  propertiesFile  to load properties from
   *
   * @return  <code>LdapPoolConfig</code> initialized ldap pool config
   */
  public static LdapConfig createFromProperties(final String propertiesFile)
  {
    final LdapConfig ldapConfig = new LdapConfig();
    LdapProperties properties = null;
    if (propertiesFile != null) {
      properties = new LdapProperties(ldapConfig, propertiesFile);
    } else {
      properties = new LdapProperties(ldapConfig);
    }
    properties.configure();
    return ldapConfig;
  }


  /**
   * This sets whether authentication credentials will be logged.
   *
   * @param  log  <code>boolean</code>
   */
  public void setLogCredentials(final boolean log)
  {
    checkImmutable();
    this.logCredentials = log;
  }


  /**
   * This sets this <code>LdapConfig</code> to use the SSL protocol for
   * connections. The port is automatically changed to the default of 636. If
   * you need to use a different port then you must call {@link #setPort} after
   * calling this method.
   *
   * @param  ssl  <code>boolean</code>
   */
  public void setSsl(final boolean ssl)
  {
    checkImmutable();
    this.ssl = ssl;
  }


  /**
   * This sets this <code>LdapConfig</code> to use the TLS protocol for
   * connections. For fine control over starting and stopping TLS you must use
   * the {@link Ldap#useTls(boolean)} method.
   *
   * @param  tls  <code>boolean</code>
   */
  public void setTls(final boolean tls)
  {
    checkImmutable();
    this.tls = tls;
  }


  /**
   * This returns a <code>SearchControls</code> object configured with this
   * <code>LdapConfig</code>.
   *
   * @param  retAttrs  <code>String[]</code> attributres to return from search
   *
   * @return  <code>SearchControls</code>
   */
  public SearchControls getSearchControls(final String[] retAttrs)
  {
    final SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(retAttrs);
    ctls.setSearchScope(this.getSearchScope().ordinal());
    ctls.setTimeLimit(this.getTimeLimit());
    ctls.setCountLimit(this.getCountLimit());
    ctls.setDerefLinkFlag(this.getDerefLinkFlag());
    ctls.setReturningObjFlag(this.getReturningObjFlag());
    return ctls;
  }


  /**
   * This returns a <code>SearchControls</code> object configured to perform a
   * LDAP compare operation.
   *
   * @return  <code>SearchControls</code>
   */
  public static SearchControls getCompareSearchControls()
  {
    final SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(new String[0]);
    ctls.setSearchScope(SearchScope.OBJECT.ordinal());
    return ctls;
  }


  /**
   * This sets this <code>LdapConfig</code> to print ASN.1 BER packets to the
   * supplied <code>PrintStream</code>.
   *
   * @param  stream  <code>PrintStream</code>
   */
  public void setTracePackets(final PrintStream stream)
  {
    checkImmutable();
    this.tracePackets = stream;
  }


  /**
   * Provides a descriptive string representation of this instance.
   *
   * @return  String of the form $Classname@hashCode::config=$config.
   */
  @Override
  public String toString()
  {
    return
      String.format(
        "%s@%d::%s",
        this.getClass().getName(),
        this.hashCode(),
        this.getEnvironment());
  }
}
