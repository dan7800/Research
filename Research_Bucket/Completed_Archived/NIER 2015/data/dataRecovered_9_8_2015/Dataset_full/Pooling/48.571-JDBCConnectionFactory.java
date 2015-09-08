/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ofbiz.minerva.pool.ObjectPool;
import org.ofbiz.minerva.pool.PoolObjectFactory;
import org.ofbiz.minerva.pool.cache.ObjectCache;

/**
 * Object factory that creates java.sql.Connections.  This is meant for use
 * outside a J2EE/JTA environment - servlets alone, client/server, etc.  If
 * you're interested in creating transactional-aware connections, see
 * XAConnectionFactory, which complies with the JDBC 2.0 standard extension.
 * @see org.ofbiz.minerva.pool.jdbc.xa.XAConnectionFactory
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class JDBCConnectionFactory extends PoolObjectFactory {

    private String url;
    private Properties props;
    private String userName;
    private String password;
    private int psCacheSize = 10;
    private ObjectPool pool;

    private static Logger log = Logger.getLogger(JDBCConnectionFactory.class);

    /**
     * Creates a new factory.  You must configure it with JDBC properties
     * before you can use it.
     */
    public JDBCConnectionFactory() {
    }

    /**
     * Sets the JDBC URL used to create new connections.
     */
    public void setConnectURL(String url) {
        this.url = url;
    }

    /**
     * Gets the JDBC URL used to create new connections.
     */
    public String getConnectURL() {
        return url;
    }

    /**
     * Sets the JDBC Propeties used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setConnectProperties(Properties props) {
        this.props = props;
    }

    /**
     * Gets the JDBC Properties used to create new connections.
     */
    public Properties getConnectProperties() {
        return props;
    }

    /**
     * Sets the JDBC user name used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setUser(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the JDBC user name used to create new connections.
     */
    public String getUser() {
        return userName;
    }

    /**
     * Sets the JDBC password used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the JDBC password used to create new connections.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the number of PreparedStatements to be cached for each
     * Connection.  Your DB product may impose a limit on the number
     * of open PreparedStatements.  The default value is 10.
     */
    public void setPSCacheSize(int size) {
        psCacheSize = size;
    }

    /**
     * Gets the number of PreparedStatements to be cached for each
     * Connection.  The default value is 10.
     */
    public int getPSCacheSize() {
        return psCacheSize;
    }

    /**
     * Validates that connection properties were set (at least a URL).
     */
    public void poolStarted(ObjectPool pool) {
        if (log.isDebugEnabled())
            log.debug("Starting");

        super.poolStarted(pool);
        if (url == null) {
            log.error("Must specify JDBC connection URL");
            throw new IllegalStateException("Must specify JDBC connection URL to " + getClass().getName());
        }
        this.pool = pool;
    }

    /**
     * Cleans up.
     */
    public void poolClosing(ObjectPool pool) {
        if (log.isDebugEnabled())
            log.debug("Stopping");

        super.poolClosing(pool);
        this.pool = null;
    }

    /**
     * Creates a new JDBC Connection.
     */
    public Object createObject(Object parameters) throws Exception {

        log.debug("Opening new connection");

        try {
            if (userName != null && userName.length() > 0)
                return DriverManager.getConnection(url, userName, password);
            else if (props != null)
                return DriverManager.getConnection(url, props);
            else
                return DriverManager.getConnection(url);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw e;
        }
    }

    /**
     * Wraps the connection with a ConnectionInPool.
     * @see org.ofbiz.minerva.pool.jdbc.ConnectionInPool
     */
    public Object prepareObject(Object pooledObject) {
        Connection con = (Connection) pooledObject;
        ConnectionInPool wrapper = new ConnectionInPool(con);
        wrapper.setPSCacheSize(psCacheSize);
        return wrapper;
    }

    /**
     * Returns the original connection from a ConnectionInPool.
     * @see org.ofbiz.minerva.pool.jdbc.ConnectionInPool
     */
    public Object translateObject(Object clientObject) {
        return ((ConnectionInPool) clientObject).getUnderlyingConnection();
    }

    /**
     * Closes all outstanding work for the connection, rolls it back, and
     * returns the underlying connection to the pool.
     */
    public Object returnObject(Object clientObject) {
        ConnectionInPool wrapper = (ConnectionInPool) clientObject;
        Connection con = wrapper.getUnderlyingConnection();
        try {
            wrapper.reset();
        } catch (SQLException e) {
            pool.markObjectAsInvalid(clientObject);
        }
        return con;
    }

    /**
     * Closes a connection.
     */
    public void deleteObject(Object pooledObject) {
        Connection con = (Connection) pooledObject;
        try {
            con.rollback();
        } catch (SQLException ignored) {
        }

        // Removed all the cached PreparedStatements for this Connection
        ObjectCache cache = (ObjectCache) ConnectionInPool.psCaches.remove(con);
        if (cache != null)
            cache.close();

        try {
            con.close();
        } catch (SQLException ignored) {
        }
    }
}

/*
vim:tabstop=3:et:shiftwidth=3
*/
