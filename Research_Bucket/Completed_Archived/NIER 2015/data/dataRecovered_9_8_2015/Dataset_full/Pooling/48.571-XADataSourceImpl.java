/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool.jdbc.xa.wrapper;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Transactional DataSource wrapper for JDBC 1.0 drivers.  This is very
 * lightweight - it just passes requests through to an underlying driver, and
 * wraps the results with an XAConnection.  The XAConnection and corresponding
 * XAResource are responsible for closing the connection when appropriate.
 * Note that the underlying driver may perform pooling, but need not.  This
 * class does not add any pooling capabilities.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XADataSourceImpl implements XADataSource {

    private String url;
    private String user;
    private String password;
    private String driverName;
    private Driver driver;
    private Properties properties;
    private int loginTimeout;
    private PrintWriter logWriter;
    private boolean saveStackTrace;
    private static Logger log = Logger.getLogger(XADataSourceImpl.class);

    /**
     * Empty constructure for beans, reflection, etc.
     */
    public XADataSourceImpl() {
    }

    /**
     * Specifies the URL and properties to connect to the underlying driver.
     * If the properties are null, they will not be used.
     */
    public XADataSourceImpl(String url, Properties properties) {
        this.url = url;
        this.properties = properties;
    }

    /**
     * Gets the JDBC URL used to open an underlying connection.
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the JDBC URL used to open an underlying connection.
     */
    public void setURL(String url) {
        this.url = url;
    }

    /**
     * Gets the JDBC user name used to open an underlying connection.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the JDBC user name used to open an underlying connection.
     * This is optional - use it only if your underlying driver requires it.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Gets the JDBC password used to open an underlying connection.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the JDBC password used to open an underlying connection.
     * This is optional - use it only if your underlying driver requires it.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public void setDriver(String driverName) {
        this.driverName = driverName;
    }

    /**
     * Gets the JDBC properties used to open an underlying connection.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the JDBC properties used to open an underlying connection.
     * This is optional - use it only if your underlying driver requires it.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Have XAClientConnections save a stack trace on creation
     * This is useful for debugging non-closed connections.
     * It must be used with ReleaseOnCommit option
     */
    public boolean getSaveStackTrace() {
        return saveStackTrace;
    }

    public void setSaveStackTrace(boolean save) {
        saveStackTrace = save;
    }

    /**
     * Gets the log writer used to record when XAConnections are opened.
     */
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    /**
     * Sets a log writer used to record when XAConnections are opened.
     */
    public void setLogWriter(PrintWriter writer) throws SQLException {
        if (writer == null) {
            logWriter = null;
        }

    }

    /**
     * This is not used by the current implementation, since the effect would
     * differ depending on the underlying driver.
     */
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    /**
     * This is not used by the current implementation, since the effect would
     * differ depending on the underlying driver.
     */
    public void setLoginTimeout(int timeout) throws SQLException {
        loginTimeout = timeout;
    }

    private void loadDriver() throws SQLException {
        if (driver == null) {
            try {
                driver = (Driver) Class.forName(driverName, true, Thread.currentThread().getContextClassLoader()).newInstance();
                DriverManager.registerDriver(driver);
            } catch (ClassNotFoundException e) {
                log.warn("unable to load driver", e);
            } catch (InstantiationException e) {
                log.warn("unable to instantiate driver", e);
            } catch (IllegalAccessException e) {
                log.warn("illegal access exception", e);
            }
        }
    }

    /**
     * Gets an XAConnection.  This first gets a java.sql.Connection from the
     * underlying driver, and then wraps it in an XAConnection and XAResource.
     * This uses the URL, user, password, and properties (or as many as you
     * have specified) to make the connection.
     */
    public XAConnection getXAConnection() throws SQLException {

        loadDriver();

        Connection con;
        if (user != null && user.length() > 0)
            con = DriverManager.getConnection(url, user, password);
        else if (properties != null)
            con = DriverManager.getConnection(url, properties);
        else
            con = DriverManager.getConnection(url);


        try {
            con.setAutoCommit(false);
        } catch (SQLException e) {
            log.warn("Unable to disable auto-commit on " + con.getClass().getName());
        }

        XAResourceImpl res = new XAResourceImpl(con);
        XAConnectionImpl xacon = new XAConnectionImpl(con, res, saveStackTrace);
        res.setXAConnection(xacon);


        log.debug("created new Connection(" + con.getClass().getName() + ") with XAResource " + res.getClass().getName() + " and XAConnection " + xacon.getClass().getName() + ".");

        return xacon;
    }

    /**
     * Gets an XAConnection.  This first gets a java.sql.Connection from the
     * underlying driver, and then wraps it in an XAConnection and XAResource.
     * Note that we never change the default userid and password, but instead
     * only set the userid and password for this one connection.
     */
    public XAConnection getXAConnection(String user, String password) throws SQLException {

        loadDriver();
        Connection con = DriverManager.getConnection(url, user, password);

        try {
            con.setAutoCommit(false);
        } catch (SQLException e) {
            if (logWriter != null)
                logWriter.println("XADataSource unable to disable auto-commit on " + con.getClass().getName());
        }

        XAResourceImpl res = new XAResourceImpl(con);
        XAConnectionImpl xacon = new XAConnectionImpl(con, res, saveStackTrace);
        res.setXAConnection(xacon);

        xacon.setUser(user);
        xacon.setPassword(password);

        log.debug(" created new Connection (" + con.getClass().getName() + ") with XAResource " + res.getClass().getName() + " and XAConnection with userid and password " + xacon.getClass().getName());


        return xacon;
    }
}
