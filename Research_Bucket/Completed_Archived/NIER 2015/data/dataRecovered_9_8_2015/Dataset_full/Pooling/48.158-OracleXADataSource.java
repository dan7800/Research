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
package org.enhydra.jdbc.oracle;

import org.enhydra.jdbc.standard.StandardXADataSource;

import javax.sql.XADataSource;
import javax.sql.XAConnection;
import javax.naming.Reference;
import javax.naming.Context;
import javax.naming.Name;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Data source for creating IdbXAConnections.
 */
public class OracleXADataSource extends StandardXADataSource implements XADataSource {

    /**
     * Constructor.
     */
    public OracleXADataSource () {
        // Required by JNDI
        super();
    }

    /**
     * Creates an XA connection using the default username and password.
     */
    public XAConnection getXAConnection () throws SQLException {
        return getXAConnection (user, password);
    }

    /**
     * Creates an XA connection using the supplied username and password.
     */
    public XAConnection getXAConnection (String user, String password) throws SQLException {
        OracleXAConnection xac = new OracleXAConnection (this, user, password);
        xac.setTransactionManager(transactionManager);
        connectionCount++;
        return xac;
    }

    /**
     * The factory interface.
     */
    public Object getObjectInstance(Object refObj, Name name, Context nameCtx, Hashtable env) throws Exception {
        Reference ref = (Reference)refObj;
        if (ref.getClassName().equals(getClass().getName())) {
            OracleXADataSource dataSource = new OracleXADataSource ();
            dataSource.setDriverName((String)ref.get("driverName").getContent());
            dataSource.setUrl((String)ref.get("url").getContent());
            return dataSource;
        } else {
            return null;
        }
    }

}
