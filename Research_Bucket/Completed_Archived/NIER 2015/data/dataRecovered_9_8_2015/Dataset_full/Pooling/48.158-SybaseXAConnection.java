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

import org.enhydra.jdbc.standard.StandardXAConnection;

import java.sql.SQLException;

public class SybaseXAConnection extends StandardXAConnection {

    /**
     * Creates the first free connection.
     */
    public SybaseXAConnection (SybaseXADataSource dataSource, String user, String password) throws SQLException {
        super (dataSource, user, password);	// creates the first Connection object

        // Save the constructor parameters.
        this.dataSource = dataSource;
        curCon = new SybaseXAStatefulConnection (dataSource, con);// wrap connection as a stateful connection

        // NOTE - the current connection is not made known to the data source
        // so it is not eligible for re-use. It only goes on the data source list
        // if it ever becomes associated with a global transaction.

        /*
        // get the timer thread
        if (dataSource.getThreadFactory() != null) {
        dataSource.log("StandardXAConnection: Getting thread from factory");
        timerThread = dataSource.getThreadFactory().getThread(this);
        dataSource.log("StandardXAConnection: Got thread from factory");
        } else {
        dataSource.log("StandardXAConnection: Getting thread from new Thread()");
        timerThread = new Thread (this);	// create the backgroup thread to check for timeouts
        }

        timerThread.start();			// start the timer thread
        //timerThread.suspend();			// and suspend until some timeouts get set up
        */
        dataSource.log.debug("SybaseXAConnection created");
    }

}
