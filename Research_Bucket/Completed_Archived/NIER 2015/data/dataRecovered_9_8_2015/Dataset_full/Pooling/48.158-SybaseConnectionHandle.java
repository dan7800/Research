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

import org.enhydra.jdbc.standard.StandardConnectionHandle;

import java.util.Hashtable;
import java.sql.SQLException;

public class SybaseConnectionHandle extends StandardConnectionHandle {

    public SybaseConnectionHandle (SybasePooledConnection pooledCon, Hashtable preparedStatementCache, int preparedStmtCacheSize) {
        super (pooledCon, preparedStatementCache,preparedStmtCacheSize);
    }

    synchronized public void setAutoCommit(boolean autoCommit) throws SQLException {
        preInvoke();
        try {
            con.commit();
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            catchInvoke(e);
        }
    }

}
