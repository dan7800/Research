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
package org.enhydra.jdbc.informix;

import org.enhydra.jdbc.standard.StandardConnectionHandle;

import java.util.Hashtable;
import java.sql.SQLException;
import java.sql.Statement;

public class InformixConnectionHandle extends StandardConnectionHandle {

    private int lockModeWait = 0;	// The amound time to wait on a query or transaction

    public InformixConnectionHandle (InformixPooledConnection pooledCon, Hashtable preparedStatementCache, int preparedStmtCacheSize) {
        super (pooledCon, preparedStatementCache,preparedStmtCacheSize);
    }

    public synchronized void setLockModeToWait(int seconds) throws SQLException {
        if (lockModeWait != seconds) {
            if (seconds >0) {
                execute("SET LOCK MODE TO WAIT " + seconds);
            } else if (seconds == 0) {
                execute("SET LOCK MODE TO NOT WAIT ");
            } else {
                execute("SET LOCK MODE TO WAIT ");
            }
            lockModeWait = seconds;
        }
    }

    public synchronized void execute (String sql) throws SQLException {
        Statement stat = createStatement();
        stat.execute(sql);
    }

}
