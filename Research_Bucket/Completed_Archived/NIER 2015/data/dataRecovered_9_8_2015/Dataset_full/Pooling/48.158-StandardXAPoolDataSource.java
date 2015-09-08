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
package org.enhydra.jdbc.pool;

import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionEvent;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.enhydra.jdbc.standard.StandardXAConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.PooledConnection;
import javax.transaction.Status;

/**
 * StandardXAPoolDataSource class allows to make some operations on
 * XAConnection. It implements PoolHelper for the 3 methods :<p>
 * create   : create an XAConnection<p>
 * create(user,password)   : create a PooledConnection with an other user/password<p>
 * testThisObject : check if the object is still valid<p>
 * checkThisObject : check if the object is closed<p>
 * expire   : kill the object<p>
 */
public class StandardXAPoolDataSource extends StandardPoolDataSource {

	public XADataSource xads; // object to build XAConnection
	public TransactionManager transactionManager;
	// the current Transaction Manager
	public Log glog = LogFactory.getLog("org.enhydra.jdbc.xapool");

	/**
	 * Constructor
	 */
	public StandardXAPoolDataSource() {
		super();
	}

	/**
	 * Constructor
	 */
	public StandardXAPoolDataSource(int initSize) {
		super(initSize);
	}

	/**
	 * Constructor
	 */
	public StandardXAPoolDataSource(StandardXADataSource ds) {
		super(ds);
                setDataSource(ds);
	}

	/**
	 * Constructor
	 */
	public StandardXAPoolDataSource(StandardXADataSource ds, int initSize) {
		super(ds, initSize);
                setDataSource(ds);
	}

	public void setTransactionManager(TransactionManager tm) {
		log.debug("StandardXAPoolDataSource:setTransactionManager");
		transactionManager = tm;
	}

	/**
	 * Invoked when the application calls close()
	 * on its representation of the connection
	 */
	public void connectionClosed(ConnectionEvent event) {
		Object obj = event.getSource();
		log.debug("StandardXAPoolDataSource:connectionClosed");
		XAConnection xac = (XAConnection) obj; // cast it into an xaConnection

		Transaction tx = null;
		try {
			if (transactionManager == null) {
				TransactionManager tm =
					((StandardXADataSource) xads).getTransactionManager();
				if (tm == null) {
					throw new NullPointerException("TM is null");
				} else
					// here we use tm instead to setup transactionManager = tm
					// if the current transactionManager property is null, it stays
					// there, and we continue to use the TM from the XADataSource
					tx = tm.getTransaction();
			} else {
				tx = transactionManager.getTransaction();
			}
			log.debug(
				"StandardXAPoolDataSource:connectionClosed get a transaction");
		} catch (NullPointerException n) {
			// current is null: we are not in EJBServer.
			log.error(
				"StandardXAPoolDataSource:connectionClosed should not be used outside an EJBServer");
		} catch (SystemException e) {
			log.error(
				"StandardXAPoolDataSource:connectionClosed getTransaction failed:"
					+ e);
		}

		// delist Resource if in transaction
		// We must keep the connection till commit or rollback
		if ((tx != null)
			&& (((StandardXAConnection) xac).connectionHandle.isReallyUsed)) {
			try {
				tx.delistResource(xac.getXAResource(), XAResource.TMSUCCESS);
				// delist the xaResource
				log.debug(
					"StandardXAPoolDataSource:connectionClosed the resourse is delisted");
			} catch (Exception e) {
				log.error(
					"StandardXAPoolDataSource:connectionClosed Exception in connectionClosed:"
						+ e);
			}
		}
		log.debug(
			"StandardXAPoolDataSource:connectionClosed checkIn an object to the pool");
		pool.checkIn(obj); // return the connection to the pool
	}

	public GenerationObject create(String _user, String _password)
		throws SQLException {
		GenerationObject genObject;
		XAConnection xaCon = xads.getXAConnection(_user, _password);
		// get the xa connection
		xaCon.addConnectionEventListener(this); // add it to the event listener
		log.debug(
			"StandardXAPoolDataSource:create create a object for the pool");
		genObject =
			new GenerationObject(xaCon, pool.getGeneration(), _user, _password);

		return genObject;
	}

	/**
	 * Retrieves the Reference of this object. Used at binding time by JNDI
	 * to build a reference on this object.
	 *
	 * @return  The non-null Reference of this object.
	 * @exception  NamingException  If a naming exception was encountered while
	 * retrieving the reference.
	 */
	public Reference getReference() throws NamingException {
		log.debug(
			"StandardXAPoolDataSource:getReference return a reference of the object");
		Reference ref = super.getReference();
		ref.add(
			new StringRefAddr("transactionManagerName", "TransactionManager"));
		return ref;
	}

	/* (non-Javadoc)
	 * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	public Object getObjectInstance(
		Object refObj,
		Name name,
		Context nameCtx,
		Hashtable env)
		throws Exception {

		super.getObjectInstance(refObj, name, nameCtx, env);
		Reference ref = (Reference) refObj;
		InitialContext ictx = new InitialContext(env);
		this.setTransactionManager(
			(TransactionManager) ictx.lookup(
				"javax.transaction.TransactionManager"));
		this.setDataSource((XADataSource) ictx.lookup(this.dataSourceName));
		log.debug("StandardPoolDataSource:getObjectInstance: instance created");
		return this;
	}

	/** Getter for property dataSource.
	 * @return Value of property dataSource.
	 */
	public XADataSource getDataSource() {
		return xads;
	}

	/** Setter for property dataSource.
	 * @param dataSource New value of property dataSource.
	 */
	public void setDataSource(XADataSource dataSource) {
		this.xads = dataSource;
		if (transactionManager != null)
			((StandardXADataSource) dataSource).setTransactionManager(
				transactionManager);
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardXAPoolDataSource:\n");
                if (this.transactionManager != null)
		        sb.append("     transaction manager=<"+this.transactionManager.toString()+">\n");
                if (this.xads != null)
		        sb.append(this.xads.toString());
		sb.append(super.toString());		
		return sb.toString();
	}

    /**
     * This method tests if a connection is valid or not. It overrides the
     * method in StandardPoolDataSource to take into account global transactions:
     * if global transaction is in progress - suspend it so that
     * connection testing happens ouside of transaction.
     * If connection testing fails - it will not affect transaction
     * and next good connection can join the transaction
     */
     public boolean testThisObject(Object o) {
        Connection ret = null;
        log.debug(
            "StandardPoolDataSource:testThisObject verify the current object");
        Transaction suspended = null;
        try {
            Transaction tx = transactionManager == null
                                ? null
                                : transactionManager.getTransaction();
            boolean isActive = tx == null
                                ? false
                                : tx.getStatus() == Status.STATUS_ACTIVE;
            if (isActive) {
                suspended = transactionManager.suspend();
            }


            PooledConnection con = (PooledConnection) o;
            ret = con.getConnection();



            Statement s = ret.createStatement();
            s.execute(jdbcTestStmt);
            s.close();
            try {
                ret.close();
            } catch (Exception e) {
                log.error(
                    "StandardPoolDataSource:checkThisObject can't closed the connection: "
                        + e);
            }
            return true;
        } catch (SQLException e) {
            log.error(
                "StandardXAPoolDataSource:checkThisObject Error java.sql.SQLException in StandardXAPoolDataSource:testThisObject");
            return false;
        } catch (SystemException e) {
            log.error(
                "StandardXAPoolDataSource:checkThisObject Error java.sql.SystemException in StandardXAPoolDataSource:testThisObject");
            return false;
        } finally {
            if (suspended != null) {
                try {
                    transactionManager.resume(suspended);
                } catch (Exception ex) {
                    log.error(
                        "StandardXAPoolDataSource:checkThisObject Error Exception in StandardXAPoolDataSource:testThisObject");
                    return false;
                }
            }
        }
    }

}