/*
 * XAPool: Open Source XA JDBC Pool
 * Copyright (C) 2003 Objectweb.org
 * Initial Developer: Lutris Technologies Inc.
 * Contact: xapool-public@lists.debian-sf.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 */
package org.enhydra.jdbc.sybase;

import org.enhydra.jdbc.standard.StandardXADataSource;

import javax.sql.XAConnection;
import javax.sql.PooledConnection;
import java.sql.SQLException;

public class SybaseXADataSource extends StandardXADataSource {

    /**
     * Creates an XA connection using the default username and password.
     */
    public XAConnection getXAConnection () throws SQLException {
        log.debug("SybaseXADataSource:getXAConnection(0) XA connection returned");
        return getXAConnection (user, password);
    }

    /**
     * Creates an XA connection using the supplied username and password.
     */
    public XAConnection getXAConnection (String user, String password) throws SQLException {
        SybaseXAConnection xac = new SybaseXAConnection (this, user, password);
        connectionCount++;
        log.debug("SybaseXADataSource:getXAConnection(2) XA connection returned");
        return xac;
    }

    /**
     * Create a pooled connection using the default username and password.
     */
    public PooledConnection getPooledConnection () throws SQLException {
        log.debug("SybaseConnectionPoolDataSource:getPooledConnection(0) return a pooled connection");
        return getPooledConnection (user, password);
    }

    /**
     * Create a sybase pooled connection using the supplied username and password.
     */
    public PooledConnection getPooledConnection (String user, String password) throws SQLException {
        log.debug("SybaseConnectionPoolDataSource:getPooledConnection(2) return a pooled connection");
        return new SybasePooledConnection (this, user, password);
    }

}
