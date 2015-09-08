/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool.jdbc.xa;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Log4jLoggerWriter;
import org.ofbiz.minerva.pool.ObjectPool;

/**
 * DataSource for transactional JDBC pools.  This handles configuration
 * parameters for both the pool and the JDBC connection.  It is important that
 * you set all the configuration parameters before you initialize the DataSource,
 * and you must initialize it before you use it.  All the configuration
 * parameters are not documented here; you are instead referred to ObjectPool
 * and XAConnectionFactory.
 * @see org.ofbiz.minerva.pool.ObjectPool
 * @see org.ofbiz.minerva.pool.jdbc.xa.XAConnectionFactory
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 * Revision:
 * 20010701 danch added code for timeout in blocking.
 * 20010703 bill added code for transaction isolation and setting the ps cache size
 */
public class XAPoolDataSource implements DataSource, Referenceable, ObjectFactory, Serializable
        //just including Serializable to strongly indicate serialization support
{

    private transient static Logger log = Logger.getLogger(XAPoolDataSource.class);
    private transient static HashMap sources = new HashMap();

    /**
     * Gets all the current JDBC pool data sources.
     */
    public static Collection getDataSources() {
        return new HashSet(sources.values());
    }

    /**
     * Gets a specific JDBC pool data source by pool name.
     */
    public static XAPoolDataSource getDataSource(String poolName) {
        return (XAPoolDataSource) sources.get(poolName);
    }

    private transient ObjectPool pool;
    private transient XAConnectionFactory factory;
    private transient PrintWriter logWriter;
    private transient int timeout;
    private transient boolean initialized = false;
    //Unused cruft.
    private transient String jndiName;
    //what is actually used to bind in jndi by XADataSourceLoader.
    private String name;

    /**
     * Creates a new XA pool data source.  Be sure to configure it and then
     * call initialize before you try to use it.
     */
    public XAPoolDataSource() {
        log.debug("Creating XA Pool");
        pool = new ObjectPool();
        factory = new XAConnectionFactory();
        log.debug("Created factory");

        XAPoolDriver.instance();
        log.debug("got driver instance");
    }

    // Unique properties
    /**
     * If you use this to set a JNDI name, this pool will be bound to that name
     * using the default InitialContext.  You can also do this manually if you
     * have additional requirements.
     */
    public void setJNDIName(String name) throws NamingException {
        if (log.isDebugEnabled())
            log.debug("Binding to JNDI name " + name);

        InitialContext ctx = new InitialContext();
        if (jndiName != null && !jndiName.equals(name))
            ctx.unbind(jndiName);
        if (name != null)
            ctx.bind(name, this);
        jndiName = name;
    }

    /**
     * Gets the JNDI name this pool is bound to.  Only valid if you used
     * setJNDIName to bind it.
     * @see #setJNDIName
     */
    public String getJNDIName() {
        return jndiName;
    }

    // XA properties
    public void setDataSource(XADataSource ds) {
        factory.setDataSource(ds);
    }

    public XADataSource getDataSource() {
        return factory.getDataSource();
    }

    public void setTransactionManager(TransactionManager tm) {
        factory.setTransactionManager(tm);
    }

    //public String getTransactionManagerJNDIName() {return factory.getTransactionManagerJNDIName();}
    public void setJDBCUser(String user) {
        factory.setUser(user);
    }

    public String getJDBCUser() {
        return factory.getUser();
    }

    public void setJDBCPassword(String password) {
        factory.setPassword(password);
    }

    public String getJDBCPassword() {
        return factory.getPassword();
    }

    public int getTransactionIsolation() {
        return factory.getTransactionIsolation();
    }

    public void setTransactionIsolation(int iso) {
        factory.setTransactionIsolation(iso);
    }

    public void setTransactionIsolation(String iso) {
        factory.setTransactionIsolation(iso);
    }

    public int getPSCacheSize() {
        return factory.getPSCacheSize();
    }

    public void setPSCacheSize(int size) {
        factory.setPSCacheSize(size);
    }

    public boolean getReleaseOnCommit() {
        return factory.getReleaseOnCommit();
    }

    public void setReleaseOnCommit(boolean rel) {
        factory.setReleaseOnCommit(rel);
    }

    /**
     * Have XAClientConnections save a stack trace on creation
     * This is useful for debugging non-closed connections.
     * It must be used with ReleaseOnCommit option
     */
    public boolean getSaveStackTrace() {
        return factory.getSaveStackTrace();
    }

    public void setSaveStackTrace(boolean save) {
        factory.setSaveStackTrace(save);
    }


    // Pool properties
    public void setPoolName(String name) {
        //remember the name so we can look ourselves up on deserialization.
        this.name = name;
        pool.setName(name);
        sources.put(pool.getName(), this);
    }

    public String getPoolName() {
        return name;
    }

    public void setMinSize(int size) {
        pool.setMinSize(size);
    }

    public int getMinSize() {
        return pool.getMinSize();
    }

    public void setMaxSize(int size) {
        pool.setMaxSize(size);
    }

    public int getMaxSize() {
        return pool.getMaxSize();
    }

    public void setBlocking(boolean blocking) {
        pool.setBlocking(blocking);
    }

    public boolean isBlocking() {
        return pool.isBlocking();
    }

    public void setBlockingTimeout(int blockingTimeout) {
        pool.setBlockingTimeout(blockingTimeout);
    }

    public int getBlockingTimeout() {
        return pool.getBlockingTimeout();
    }

    public void setIdleTimeoutEnabled(boolean allowShrinking) {
        pool.setIdleTimeoutEnabled(allowShrinking);
    }

    public boolean isIdleTimeoutEnabled() {
        return pool.isIdleTimeoutEnabled();
    }

    public void setGCEnabled(boolean allowGC) {
        pool.setGCEnabled(allowGC);
    }

    public boolean isGCEnabled() {
        return pool.isGCEnabled();
    }

    public void setMaxIdleTimeoutPercent(float percent) {
        pool.setMaxIdleTimeoutPercent(percent);
    }

    public float getMaxIdleTimeoutPercent() {
        return pool.getMaxIdleTimeoutPercent();
    }

    public void setIdleTimeout(long millis) {
        pool.setIdleTimeout(millis);
    }

    public long getIdleTimeout() {
        return pool.getIdleTimeout();
    }

    public void setGCMinIdleTime(long millis) {
        pool.setGCMinIdleTime(millis);
    }

    public long getGCMinIdleTime() {
        return pool.getGCMinIdleTime();
    }

    public void setGCInterval(long millis) {
        pool.setGCInterval(millis);
    }

    public long getGCInterval() {
        return pool.getGCInterval();
    }

    public void setInvalidateOnError(boolean invalidate) {
        pool.setInvalidateOnError(invalidate);
    }

    public boolean isInvalidateOnError() {
        return pool.isInvalidateOnError();
    }

    public void setTimestampUsed(boolean timestamp) {
        pool.setTimestampUsed(timestamp);
    }

    public boolean isTimestampUsed() {
        return pool.isTimestampUsed();
    }

    // Other methods

    /**
     * Initializes the pool.  You need to have configured all the pool and
     * XA properties first.
     */
    public void initialize() {
        initialized = true;
        pool.setObjectFactory(factory);
        pool.initialize();
    }

    /**
     * Returns a string describing the pool status (number of connections
     * created, used, and maximum).
     */
    public String getPoolStatus() {
        return pool.toString();
    }

    /**
     * Shuts down this data source and the underlying pool.  If you used
     * setJNDI name to bind it in JNDI, it is unbound.
     */
    public void close() {
        if (log.isDebugEnabled())
            log.debug("Closing DataSource");

        try {
            setJNDIName(null);
        } catch (NamingException e) {
            log.warn("Can't unbind from JNDI", e);
        }
        sources.remove(pool.getName());
        pool.shutDown();
        pool = null;
        factory = null;
    }

    /**
     * Gets a connection from the pool.
     * Since no userid or password is specified, we get a connection using the default
     * userid and password, specified when the factory was created.
     */
    public Connection getConnection() throws java.sql.SQLException {
        if (!initialized) initialize();

        log.debug("Getting a Connection");
        String user = factory.getUser();
        String password = factory.getPassword();
        String[] params = {user, password};
        XAConnection xaConn = (XAConnection) pool.getObject(params);
        return xaConn.getConnection();
    }

    /**
     * Gets a new connection from the pool.  If a new connection must be
     * created, it will use the specified user name and password.  If there is
     * a connection available in the pool, it will be used, regardless of the
     * user name and password use to created it initially.
     */
    public Connection getConnection(String user, String password) throws java.sql.SQLException {
        if (!initialized) initialize();

        log.debug("Getting a connection for user " + user + " with password " + password);
        String[] params = {user, password};
        XAConnection xaConn = (XAConnection) pool.getObject(params);
        return xaConn.getConnection();
    }

    /**
     * Gets a log writer used to record pool events.
     */
    public PrintWriter getLogWriter() throws java.sql.SQLException {
        return logWriter;
    }

    /**
     * Sets a log writer used to record pool events.
     */
    public void setLogWriter(PrintWriter writer) throws java.sql.SQLException {
        if (writer == null) {
            logWriter = null;
        } else {
            if (logWriter == null) {
                logWriter = new Log4jLoggerWriter(log);
            }
        }
    }

    /**
     * This property is not used by this implementation.
     */
    public int getLoginTimeout() throws java.sql.SQLException {
        return timeout;
    }

    /**
     * This property is not used by this implementation.
     */
    public void setLoginTimeout(int timeout) throws java.sql.SQLException {
        this.timeout = timeout;
    }

    // Referenceable implementation ----------------------------------
    /**
     * Gets a reference to this data source.
     */
    public Reference getReference() {
        return new Reference(getClass().getName(), new StringRefAddr("XAPool", pool.getName()), getClass().getName(), null);
    }

    // ObjectFactory implementation ----------------------------------
    /**
     * Decodes a reference to a specific pool data source.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) {
        if (obj instanceof Reference) {
            Reference ref = (Reference) obj;
            if (ref.getClassName().equals(getClass().getName())) {
                RefAddr addr = ref.get("XAPool");
                return sources.get(addr.getContent());
            }
        }
        return null;
    }

    //deserialization to correct instance support

    private Object readResolve() throws ObjectStreamException {
        try {
            InitialContext ctx = new InitialContext();
            return ctx.lookup("java:/" + name);
        } catch (NamingException e) {
            throw new InvalidObjectException("problem finding correct datasource instance" + e);
        } // end of try-catch

    }
}

/*
vim:tabstop=3:et:shiftwidth=3
*/
