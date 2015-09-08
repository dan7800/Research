/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool.jdbc.xa.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.ofbiz.minerva.pool.PoolEvent;
import org.ofbiz.minerva.pool.cache.LeastRecentlyUsedCache;
import org.ofbiz.minerva.pool.cache.ObjectCache;
import org.ofbiz.minerva.pool.jdbc.ConnectionInPool;
import org.ofbiz.minerva.pool.jdbc.ConnectionWrapper;
import org.ofbiz.minerva.pool.jdbc.PreparedStatementFactory;
import org.ofbiz.minerva.pool.jdbc.PreparedStatementInPool;
import org.ofbiz.minerva.pool.jdbc.StatementInPool;

import org.apache.log4j.Logger;

/**
 * Wrapper for database connections used by an XAConnection.  When close is
 * called, it does not close the underlying connection, just informs the
 * XAConnection that close was called.  The connection will not be closed (or
 * returned to the pool) until the transactional details are taken care of.
 * This instance only lives as long as one client is using it - though we
 * probably want to consider reusing it to save object allocations.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAClientConnection implements ConnectionWrapper {

    private final static String CLOSED = "Connection has been closed!";

    private Connection con;
    private HashSet statements;
    private Vector listeners;
    private XAConnectionImpl xaCon;
    private int preparedStatementCacheSize = 0;
    private ObjectCache preparedStatementCache;
    private String stackTrace = null;
    private static Logger log = Logger.getLogger(XADataSourceImpl.class);

    /**
     * Creates a new connection wrapper.
     * @param xaCon The handler for all the transactional details.
     * @param con The "real" database connection to wrap.
     */
    public XAClientConnection(XAConnectionImpl xaCon, Connection con, boolean saveStackTrace) {
        this.con = con;
        this.xaCon = xaCon;
        preparedStatementCache = (ObjectCache) ConnectionInPool.psCaches.get(con);
        if (preparedStatementCache == null) {
            PreparedStatementFactory factory = new PreparedStatementFactory(con);
            preparedStatementCache = new LeastRecentlyUsedCache(factory, preparedStatementCacheSize);
            ConnectionInPool.psCaches.put(con, preparedStatementCache);
        }
        statements = new HashSet();
        listeners = new Vector();
        if (saveStackTrace) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream stream = new PrintStream(baos);
                new Throwable().printStackTrace(stream);
                baos.close();
                stackTrace = baos.toString();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Sets the number of PreparedStatements to be cached for each
     * Connection.  Your DB product may impose a limit on the number
     * of open PreparedStatements.
     */
    public void setPSCacheSize(int maxSize) {
        preparedStatementCacheSize = maxSize;
        if (preparedStatementCache != null) {
            preparedStatementCache.setSize(maxSize);
        }
    }

    /**
     * Gets the number of PreparedStatements to be cached for each
     * Connection.
     */
    public int getPSCacheSize() {
        return preparedStatementCacheSize;
    }

    /**
     * Gets a reference to the "real" connection.  This should only be used if
     * you need to cast that to a specific type to call a proprietary method -
     * you will defeat all the pooling if you use the underlying connection
     * directly.
     */
    public Connection getUnderlyingConnection() {
        return con;
    }

    /**
     * Closes this connection wrapper permanently.  All further calls with throw
     * a SQLException.
     */
    public void shutdown() {
        con = null;
        statements = null;
        listeners = null;
        xaCon = null;
    }

    /**
     * Updates the last used time for this connection to the current time.
     * This is not used by the current implementation.
     */
    public void setLastUsed() {
        xaCon.firePoolEvent(new PoolEvent(xaCon, PoolEvent.OBJECT_USED));
    }

    /**
     * Indicates that an error occured on this connection.
     */
    public void setError(SQLException e) {
        xaCon.setConnectionError(e);
    }

    /**
     * Indicates that a statement has been closed and no longer needs to be
     * tracked.  Outstanding statements are closed when the connection is
     * returned to the pool.
     */
    public void statementClosed(Statement st) {
        statements.remove(st);
        if ((con != null) && (st instanceof PreparedStatementInPool) && preparedStatementCacheSize != 0) {

            // Now return the "real" statement to the pool
            PreparedStatementInPool ps = (PreparedStatementInPool) st;
            PreparedStatement ups = ps.getUnderlyingPreparedStatement();
            preparedStatementCache.returnObject(ps.getSql(), ups);
/*
            int rsType = ResultSet.TYPE_FORWARD_ONLY;
            int rsConcur = ResultSet.CONCUR_READ_ONLY;

            // We may have JDBC 1.0 driver
            try {
                rsType = ups.getResultSetType();
                rsConcur = ups.getResultSetConcurrency();
            } catch (Throwable th) {
            }
            PreparedStatementInPool.preparedStatementCache.put(
                    new PSCacheKey(con, ps.getSql(), rsType, rsConcur), ups);
*/
        }
    }

    // ---- Implementation of java.sql.Connection ----
    public Statement createStatement() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            StatementInPool st = new StatementInPool(con.createStatement(), this);
            statements.add(st);
            return st;
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            PreparedStatement ps;
            if (preparedStatementCacheSize == 0) {
                // cache disabled
                ps = con.prepareStatement(sql);
            } else {
                ps = (PreparedStatement) preparedStatementCache.useObject(sql);
            }
            if (ps == null)
                throw new SQLException("Unable to create PreparedStatement!");
            PreparedStatementInPool wrapper = new PreparedStatementInPool(ps, this, sql);
            statements.add(wrapper);
            return wrapper;
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.prepareCall(sql);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public String nativeSQL(String sql) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.nativeSQL(sql);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        if (((XAResourceImpl) xaCon.getXAResource()).isTransaction() && autoCommit)
            throw new SQLException("Cannot set AutoCommit for a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");

        try {
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }

    }

    public boolean getAutoCommit() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.getAutoCommit();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void commit() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        if (((XAResourceImpl) xaCon.getXAResource()).isTransaction())
            throw new SQLException("Cannot commit a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");
        try {
            con.commit();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void rollback() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        if (((XAResourceImpl) xaCon.getXAResource()).isTransaction())
            throw new SQLException("Cannot rollback a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");
    }

    public void forcedClose() throws SQLException {
        if (stackTrace != null)
            System.err.println("A forced close because a non-closed connection:\n" + stackTrace);
        if (con == null) throw new SQLException(CLOSED);
        Collection copy = (Collection) statements.clone();
        Iterator it = copy.iterator();
        while (it.hasNext())
            try {
                ((Statement) it.next()).close();
            } catch (SQLException e) {
            }
        shutdown();
    }

    public void close() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        Collection copy = (Collection) statements.clone();
        Iterator it = copy.iterator();
        while (it.hasNext())
            try {
                ((Statement) it.next()).close();
            } catch (SQLException e) {
                log.warn("SQLException : ", e);
            }

        xaCon.clientConnectionClosed(this);
        shutdown();
    }

    public boolean isClosed() throws SQLException {
        if (con == null) return true;
        try {
            return con.isClosed();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.getMetaData();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            con.setReadOnly(readOnly);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean isReadOnly() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.isReadOnly();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setCatalog(String catalog) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            con.setCatalog(catalog);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public String getCatalog() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.getCatalog();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTransactionIsolation(int level) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            con.setTransactionIsolation(level);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getTransactionIsolation() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.getTransactionIsolation();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.getWarnings();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void clearWarnings() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            con.clearWarnings();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            StatementInPool st = new StatementInPool(con.createStatement(resultSetType, resultSetConcurrency), this);
            statements.add(st);
            return st;
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Map getTypeMap() throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            return con.getTypeMap();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTypeMap(Map map) throws SQLException {
        if (con == null) throw new SQLException(CLOSED);
        try {
            con.setTypeMap(map);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    // JDK 1.4 methods

    /* (non-Javadoc)
     * @see java.sql.Connection#setHoldability(int)
     */
    public void setHoldability(int arg0) throws SQLException {
        throw new SQLException("Method not implemented!");

    }

    /* (non-Javadoc)
     * @see java.sql.Connection#getHoldability()
     */
    public int getHoldability() throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setSavepoint()
     */
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#setSavepoint(java.lang.String)
     */
    public Savepoint setSavepoint(String arg0) throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#rollback(java.sql.Savepoint)
     */
    public void rollback(Savepoint arg0) throws SQLException {
        throw new SQLException("Method not implemented!");

    }

    /* (non-Javadoc)
     * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
     */
    public void releaseSavepoint(Savepoint arg0) throws SQLException {
        throw new SQLException("Method not implemented!");

    }

    /* (non-Javadoc)
     * @see java.sql.Connection#createStatement(int, int, int)
     */
    public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
     */
    public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
     */
    public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, int)
     */
    public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
     */
    public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
        throw new SQLException("Method not implemented!");
    }

    /* (non-Javadoc)
     * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
     */
    public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
        throw new SQLException("Method not implemented!");
    }
}
