/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.ofbiz.minerva.pool.cache.CachedObjectFactory;

/**
 * Creates PreparedStatements for a PS cache.  Doesn't yet handle
 * different isolation levels, etc.
 *
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
public class PreparedStatementFactory extends CachedObjectFactory {

    private Connection con;
    private Logger logger = Logger.getLogger(PreparedStatementFactory.class);

    public PreparedStatementFactory(Connection con) {
        this.con = con;
    }

    /**
     * Creates a PreparedStatement from a Connection & SQL String.
     */
    public Object createObject(Object sqlString) {
        String sql = (String) sqlString;
        try {
            return con.prepareStatement(sql);
        } catch (SQLException e) {
            logger.warn("Error creating prepared statement.", e);
            return null;
        }
    }

    /**
     * Closes a PreparedStatement.
     */
    public void deleteObject(Object pooledObject) {
        try {
            ((PreparedStatement) pooledObject).close();
        } catch (SQLException e) {
        }
    }
}
