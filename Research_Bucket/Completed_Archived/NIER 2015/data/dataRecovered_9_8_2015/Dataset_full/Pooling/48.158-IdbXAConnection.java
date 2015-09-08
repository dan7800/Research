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
package org.enhydra.jdbc.instantdb;

import org.enhydra.jdbc.standard.StandardXAConnection;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.enhydra.jdbc.standard.StandardXAStatefulConnection;
import org.enhydra.instantdb.jdbc.ConnectionExtensions;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;


/**
 * Provides and InstantDB specific instance of StandardXAConnection. Almost all of
 * the required functionality is provided curtesy of the generic super class
 * which looks after most of the transaction state management. InstantDB's
 * own Transaction object is informed that it is part of a global transaction
 * and looks after the detail thereafter.
 */
public final class IdbXAConnection extends StandardXAConnection {

    /**
     * Creates the first free connection.
     */
    public IdbXAConnection (StandardXADataSource dataSource, String user, String password) throws SQLException {
        super (dataSource, user, password);					// creates the first Connection object
    }

    /**
     * Associates this XAConnection with a global transaction. This
     * is the only method which can associate the current connection
     * with a global transaction. It acts only on the current
     * connection which must have been previously established using
     * getConnection.
     */
    public void start(Xid xid, int flags) throws XAException {
        doStart (xid, flags);								// do state checks and set state
        curCon.commitOnPrepare = false;						// we will do a REAL prepare
        ConnectionExtensions conExt
                = (ConnectionExtensions)curCon.con;				// get the InstantDB connection
        conExt.startGlobalTransaction (xid);				// associate the transaction with the global TX
        curCon = null;										// no longer owned by this object
        con = null;											// ditto
    }

    // We don't override "end" as all it does is change the state of a connection.

    /**
     * Prepares to perform a commit.
     */
    public int prepare(Xid xid) throws XAException {
        StandardXAStatefulConnection stateCon = checkPreparedState (xid);// do generic state checking etc.
        ConnectionExtensions con
                = (ConnectionExtensions)stateCon.con;			// get the InstantDB connection
        int status = con.prepare();							// prepare to commit
        if (status == XA_RDONLY) {							// if transaction didn't update the database
            xaDataSource.freeConnection (xid, false);			// free the connection
        } // if
        return status;
    }

    // We don't override commit or rollback. Connection.commit and
    // Connection.rollback already know that they're part of a global
    // transaction and will behave accordingly.

    /**
     * Checks to see if two XAResource objects correspond to the
     * same Resource Manager. This can go one better than its
     * super class as it can actually check the InstantDB database
     * objects.
     */
    public boolean isSameRM(XAResource xares) throws XAException {
        if (super.isSameRM(xares)) {						// if super class can figure it out
            return true;									// then accept its conclusion
        } // if
        if (xares instanceof IdbXAConnection) {				// if it's one of our wrappers
            IdbXAConnection xac = (IdbXAConnection)xares;	// cast to something more convenient
            IdbXADataSource cmpds = (IdbXADataSource)xac.dataSource;// get the data source to compare
            IdbXADataSource ds = (IdbXADataSource)dataSource;// get our own data source
            if (ds.databaseId.equals (cmpds.databaseId)) {	// if using the same database
                return true;								// they're the same resource
            } // if
        } // if
        return false;
    }

}
