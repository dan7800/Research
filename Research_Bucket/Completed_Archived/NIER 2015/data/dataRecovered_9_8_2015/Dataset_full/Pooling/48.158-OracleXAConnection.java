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


import oracle.jdbc.xa.client.OracleXAResource;
import oracle.jdbc.xa.OracleXid;
import org.enhydra.jdbc.standard.StandardXAConnection;
import org.enhydra.jdbc.standard.StandardXADataSource;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.Hashtable;
import java.sql.SQLException;


/**
 * Provides an Oracle specific instance of StandardXAConnection. Almost all of
 * the required functionality is provided curtesy of the generic super class
 * which looks after most of the transaction state management. Oracle's
 * own resource manager is informed that it is part of a global transaction
 * and looks after the detail thereafter.
 */
public final class OracleXAConnection extends StandardXAConnection {

    private XAResource xarsrc = null;
    private static Hashtable txctxs = new Hashtable();

    /**
     * Creates the first free connection.
     */
    public OracleXAConnection (StandardXADataSource dataSource, String user, String password) throws SQLException {
        super (dataSource, user, password);					// creates the first Connection object
    }

    private OracleXid getOracleXid(Xid xid) throws XAException {
        if (!(xid instanceof OracleXid)) {
            byte[] txctx = (byte[])txctxs.get(xid);
            dataSource.log.debug("txctx is " + txctx);
            OracleXid newXid = new OracleXid(xid.getFormatId(), xid.getGlobalTransactionId(), xid.getBranchQualifier(), txctx);
            return newXid;
        } else {
            return (OracleXid)xid;
        }
    }

    public void commit(Xid xid, boolean flag) throws XAException {
        dataSource.log.debug("commit:" + xid.getGlobalTransactionId());
        xarsrc.commit(getOracleXid(xid), flag);
        xaDataSource.freeConnection(xid, false);
        txctxs.remove(xid);
    }

    public void end(Xid xid, int flags) throws XAException {
        dataSource.log.debug("end" + ":" + xid.getFormatId() + ":" + xid.getGlobalTransactionId() + ":" + xid.getBranchQualifier() + ":" + flags);
        xarsrc.end(getOracleXid(xid), flags);
    }

    public void forget(Xid xid) throws XAException {
        dataSource.log.debug("forget" + ":" + xid.getGlobalTransactionId());
        xarsrc.forget(getOracleXid(xid));
        xaDataSource.freeConnection(xid, false);
        txctxs.remove(xid);
    }

    public int prepare(Xid xid) throws XAException {
        dataSource.log.debug("prepare" + ":" + xid.getGlobalTransactionId());
        int res = xarsrc.prepare(getOracleXid(xid));
        if (res == XA_RDONLY) {
            xaDataSource.freeConnection(xid, false);
            txctxs.remove(xid);
        }
        return res;
    }

    public void rollback(Xid xid) throws XAException {
        dataSource.log.debug("rollback" + ":" + xid.getGlobalTransactionId());
        xarsrc.rollback(getOracleXid(xid));
        xaDataSource.freeConnection(xid, false);
        txctxs.remove(xid);
    }

    public void start(Xid xid, int flags) throws XAException {
        dataSource.log.debug("start" + ":" + xid.getFormatId() + ":" + xid.getGlobalTransactionId() + ":" + xid.getBranchQualifier() + ":" + flags);
        doStart(xid, flags);
        xarsrc = new OracleXAResource(curCon.con);
        OracleXid oXid = getOracleXid(xid);
        xarsrc.start(oXid, flags);
        txctxs.put(xid, oXid.getTxContext());
        curCon = null;
        con = null;
    }

    public boolean isSameRM(XAResource res) throws XAException {
        if (!(res instanceof OracleXAConnection)) {
            dataSource.log.debug("isSameRM returning false");
            return false;
        }
        OracleXAConnection ores = (OracleXAConnection)res;
        if (ores.xarsrc.isSameRM(xarsrc)) {
            dataSource.log.debug("isSameRM returning true");
            return true;
        }
        dataSource.log.debug("isSameRM returning false");
        return false;
    }

}
