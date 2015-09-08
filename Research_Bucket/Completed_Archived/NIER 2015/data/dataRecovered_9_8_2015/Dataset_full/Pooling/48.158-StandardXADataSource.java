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
package org.enhydra.jdbc.standard;

import org.enhydra.jdbc.util.Logger;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import javax.transaction.TransactionManager;

/**
 * Data source for creating StandardXAConnections.
 */
public class StandardXADataSource
	extends StandardConnectionPoolDataSource
	implements XADataSource {

	public int minCon; // minimum number of connections
	public int maxCon; // maximum number of connections
	public long deadLockMaxWait;
	// time (in ms) to wait before return an exception
	Vector freeConnections; // connections not currently associated with an XID
	Hashtable xidConnections; // connections currently associated with an XID
	Hashtable deadConnections;
	// connections which should be discarded when the transaction finishes
	public int connectionCount = 0; // total number of connections created
	public long deadLockRetryWait; // time to wait before 2 try of loop
	transient public TransactionManager transactionManager;

	public static final int DEFAULT_MIN_CON = 50;
	// minimum number of connections
	public static final int DEFAULT_MAX_CON = 0;
	// maximum number of connections
	public static final long DEFAULT_DEADLOCKMAXWAIT = 300000; // 5 minutes
	public static final int DEFAULT_DEADLOCKRETRYWAIT = 10000; // 10 seconds

	/**
	 * Constructor
	 */
	public StandardXADataSource() {
		super();
		minCon = DEFAULT_MIN_CON;
		maxCon = DEFAULT_MAX_CON;
		deadLockMaxWait = DEFAULT_DEADLOCKMAXWAIT;
		deadLockRetryWait = DEFAULT_DEADLOCKRETRYWAIT;
		freeConnections = new Vector(minCon, 1);
		// allow a reasonable size for free connections
		xidConnections = new Hashtable(minCon * 2, 0.5f);
		// ...and same for used connections

		log = new Logger(LogFactory.getLog("org.enhydra.jdbc.xapool"));
		log.debug("StandardXADataSource is created");
	}

	public int getConnectionCount() {
		return connectionCount;
	}

	public Hashtable getXidConnections() {
		return xidConnections;
	}

	/**
	 * Creates an XA connection using the default username and password.
	 */
	public XAConnection getXAConnection() throws SQLException {
		log.debug(
			"StandardXADataSource:getXAConnection(0) XA connection returned");
		return getXAConnection(user, password);
	}

	/**
	 * Creates an XA connection using the supplied username and password.
	 */
	public synchronized XAConnection getXAConnection(
		String user,
		String password)
		throws SQLException {
		log.debug("StandardXADataSource:getXAConnection(user, password)");
		StandardXAConnection xac =
			new StandardXAConnection(this, user, password);
		xac.setTransactionManager(transactionManager);
		xac.setLogger(log);
		connectionCount++;
		return xac;
	}

	public void setTransactionManager(TransactionManager tm) {
		log.debug("StandardXADataSource:setTransactionManager");
		this.transactionManager = tm;
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setUser(String user) {
		log.debug("StandardXADataSource:setUser");
		if (((user == null) || (getUser() == null))
			? user != getUser()
			: !user.equals(getUser())) {
			super.setUser(user);
			resetCache();
		}
	}

	public void setPassword(String password) {
		log.debug("StandardXADataSource:setPassword");
		if (((password == null) || (getPassword() == null))
			? password != getPassword()
			: !password.equals(getPassword())) {
			super.setPassword(password);
			resetCache();
		}
	}

	public void setUrl(String url) {
		if (((url == null) || (getUrl() == null))
			? url != getUrl()
			: !url.equals(getUrl())) {
			super.setUrl(url);
			resetCache();
		}
	}

	public void setDriverName(String driverName) throws SQLException {
		if ((driverName == null && getDriverName() != null)
			|| (!driverName.equals(getDriverName()))) {
			super.setDriverName(driverName);
			resetCache();
		}
	}

	private synchronized void resetCache() {
		log.debug("StandardXADataSource:resetCache");
		// deadConnections will temporarily hold pointers to the
		// current ongoing transactions.  These will be discarded when
		// freed
		deadConnections = (Hashtable) xidConnections.clone();
		deadConnections.putAll(xidConnections);

		// now we'll just clear out the freeConnections
		Enumeration enum = freeConnections.elements();
		while (enum.hasMoreElements()) {
			StandardXAStatefulConnection xasc =
				(StandardXAStatefulConnection) enum.nextElement();
			try {
				log.debug(
					"StandardXADataSource:resetCache closing Connection:"
						+ xasc.con);
				xasc.con.close();
			} catch (SQLException e) {
				log.error(
					"StandardXADataSource:resetCache Error closing connection:"
						+ xasc.con);
			}
			freeConnections.removeElement(xasc);
		}
	}



	/**
	 * Called when an XA connection gets closed. When they have all
	 * been closed then any remaining physical connections are also
	 * closed.
	 */
	synchronized void connectionClosed() throws SQLException {
		log.debug("StandardXADataSource:connectionClosed");
		connectionCount--; // one more connection closed
		if (connectionCount == 0) { // if no connections left

			// Close any connections still associated with XIDs.
			Enumeration cons = xidConnections.keys();
			// used to iterate through the used connections
			while (cons.hasMoreElements()) {
				// while there are more connections
				Object key = cons.nextElement(); // get the next connection
                                StandardXAStatefulConnection cur =
					(StandardXAStatefulConnection)xidConnections.remove(key);
                                if(cur != null)
                                {

                                    cur.con.close(); // close the physical connection
                                }
				// cast to something more convenient

				log.debug(
					"StandardXADataSource:connectionClosed close physical connection");
			}
                 
                        
                        Iterator connIterator = freeConnections.iterator();
                        while (connIterator.hasNext())
                        {
                            StandardXAStatefulConnection cur =(StandardXAStatefulConnection)connIterator.next();
                            cur.con.close();
                            connIterator.remove();
                            log.debug(
                                      "StandardXADataSource:connectionClosed close any free connections");
                        }
		}
	}



	/**
	 * Returns the number of connections that are either
	 * prepared or heuristically completed.
	 */
	public int getXidCount() {
		int count = 0; // the return value
		Enumeration cons = xidConnections.elements();
		// used to iterate through the used connections
		while (cons.hasMoreElements()) { // while there are more connections
			Object o = cons.nextElement(); // get the next connection
			StandardXAStatefulConnection cur = (StandardXAStatefulConnection) o;
			// cast to something more convenient
			if ((cur.getState() == Status.STATUS_PREPARED)
				|| // if prepared
			 (
					cur.getState() == Status.STATUS_PREPARING)) {
				// ...or heuristically committed
				count++; // one more connection with a valid xid
			}
		}
		log.debug(
			"StandardXADataSource:getXidCount return XidCount=<" + count + ">");
		return count;
	}

	/**
	 * Constructs a list of all prepared connections' xids.
	 */
	Xid[] recover() {
		int nodeCount = getXidCount();
		// get number of connections in transactions
		Xid[] xids = new Xid[nodeCount]; // create the return array
		int i = 0; // used as xids index
		Enumeration cons = xidConnections.elements();
		// used to iterate through the used connections
		while (cons.hasMoreElements()) { // while there are more connections
			Object o = cons.nextElement(); // get the next connection
			StandardXAStatefulConnection cur = (StandardXAStatefulConnection) o;
			// cast to something more convenient
			if ((cur.getState() == Status.STATUS_PREPARED)
				|| // if prepared
			 (
					cur.getState() == Status.STATUS_PREPARING)) {
				// ...or heuristically committed
				xids[i++] = cur.xid; // save in list
			}
		}
		return xids;
	}

	/**
	 * Frees a connection to make it eligible for reuse. The free list
	 * is normally a last in, first out list (LIFO). This is efficient.
	 * However, timed out connections are nice to hang onto for error
	 * reporting, so they can be placed at the start. This is less
	 * efficient, but hopefully is a rare occurence.
	 *
	 * Here, no need to verify the number of connections, we remove an
	 * object from the xidConnections to put it in th freeConnections
	 *
	 */
	public synchronized void freeConnection(Xid id, boolean placeAtStart) {
		log.debug("StandardXADataSource:freeConnection");
		Object o = xidConnections.get(id); // lookup the connection by XID
		StandardXAStatefulConnection cur = (StandardXAStatefulConnection) o;
		// cast to something more convenient
		xidConnections.remove(id); // remove connection from in use list
		log.debug(
			"StandardXADataSource:freeConnection remove id from xidConnections");

		if (!deadConnections.containsKey(id)) {
			// if this isn't to be discarded
			/*
			 try {
			     log.debug("StandardXADataSource:freeConnection setAutoCommit(true):" + cur.id);
			     log.debug("con='"+cur.con.toString()+"'");
			     cur.con.setAutoCommit(acommit);
			 } catch(SQLException e) {
			     log.error("ERROR: Failed while autocommiting a connection: "+e);
			 }
			 */
			cur.setState(Status.STATUS_NO_TRANSACTION);
			// set its new internal state
			if (!freeConnections.contains(cur)) {
				if (placeAtStart) { // if we want to keep for as long as possible
					freeConnections.insertElementAt(cur, 0);
					// then place it at the start of the list
				} else {
					freeConnections.addElement(cur);
					// otherwise it's a LIFO list
				}
			}
		} else {
			deadConnections.remove(id);
			try {
				cur.con.close();
			} catch (SQLException e) {
				//ignore
			}
		}
		notify();

	}

	/**
	 * Invoked by the timer thread to check all transactions
	 * for timeouts. Returns the time of the next timeout event
	 * after current timeouts have expired.
	 */
	synchronized long checkTimeouts(long curTime) throws SQLException {
		//log.debug("StandardXADataSource:checkTimeouts");
		long nextTimeout = 0; // the earliest non-expired timeout in the list
		Enumeration cons = xidConnections.elements();
		// used to iterate through the used connections
		while (cons.hasMoreElements()) { // while there are more connections
			Object o = cons.nextElement(); // get the next connection
			StandardXAStatefulConnection cur = (StandardXAStatefulConnection) o;
			// cast to something more convenient
			if ((cur.timeout != 0)
				&& // if connection has a timeout
			 (
					curTime > cur.timeout)) {
				// ...and transaction has timed out
				//log.debug("StandardXADataSource:checkTimeouts connection timeout");
				cur.con.rollback();
				// undo everything to do with this transaction
				cur.timedOut = true; // flag that it has timed out
				//log.debug(cur.toString()+" timed out");
				freeConnection(cur.xid, true);
				// make the connection eligible for reuse
				// The timed out connection is eligible for reuse. The Xid and timedOut
				// flag will nevertheless remain valid until it is reallocated to another
				// global transaction. This gives the TM a *chance* to get a timeout
				// exception, but we won't hang on to it forever.
			} else { // transaction has not timed out
				if (cur.timeout != 0) { // but it has a timeout scheduled
					if ((cur.timeout < nextTimeout)
						|| // and it's the next timeout to expire
					 (
							nextTimeout == 0)) {
						// ...or first timeout we've found
						nextTimeout = cur.timeout; // set up next timeout
					}
				}
			}
		}
		return nextTimeout;
	}

	/**
	 * Checks the start of the free list to see if the connection
	 * previously associated with the supplied Xid has timed out.
	 * <P>
	 * Note that this can be an expensive operation as it has to
	 * scan all free connections. so it should only be called in
	 * the event of an error.
	 */
	synchronized private void checkTimeouts(Xid xid) throws XAException {
		log.debug("StandardXADataSource:checkTimeouts");
		for (int i = 0;
			i < freeConnections.size();
			i++) { // check each free connection
			Object o = freeConnections.elementAt(i); // get next connection
			StandardXAStatefulConnection cur = (StandardXAStatefulConnection) o;
			// cast to something more convenient
			if (!cur.timedOut) { // if it hasn't timed out
				continue; // skip it
			}
			log.debug(
				"StandardXADataSource:checkTimeouts ("
					+ i
					+ "/"
					+ freeConnections.size()
					+ ") xid     = "
					+ xid);
			log.debug(
				"StandardXADataSource:checkTimeouts cur.xid = " + cur.xid);
			if (xid.equals(cur.xid)) { // if we've found our xid
				cur.timedOut = false; // cancel time out
				throw new XAException(XAException.XA_RBTIMEOUT);
			}
		}
	}

	/**
	 * Returns the connection associated with a given XID.
	 * is reached, the Xid is found or an exception is thrown.
	 */
	synchronized StandardXAStatefulConnection getConnection(
		Xid xid,
		boolean mustFind)
		throws XAException {
		log.debug(
			"StandardXADataSource:getConnection (xid="
				+ xid
				+ ", mustFind="
				+ mustFind
				+ ")");
		Object o = xidConnections.get(xid); // lookup the connection by XID
		log.debug("XID: " + o);
		StandardXAStatefulConnection cur = (StandardXAStatefulConnection) o;
		// cast to something more convenient
		if (mustFind) { // if we expected to find the connection
			if (cur == null) { // and we didn't
				log.debug(
					"StandardXADataSource:getConnection (StatefulConnection is null)");
				checkTimeouts(xid); // see if it's been freed during a timeout
				throw new XAException(XAException.XAER_NOTA);
				// not a valid XID
			}
		} else { // didn't expect to find the connection
			if (cur != null) { // but we found it anyway
				throw new XAException(XAException.XAER_DUPID); // duplicate XID
			}
		}
		log.debug(
			"StandardXADataSource:getConnection return connection associated with a given XID");
		return cur;
	}

	/**
	 * Returns a connection from the free list, removing it
	 * in the process. If none area available then a new
	 * connection is created.
	 */
	synchronized StandardXAStatefulConnection getFreeConnection()
		throws SQLException {
		log.debug("StandardXADataSource:getFreeConnection");
		StandardXAStatefulConnection cur = null;
		// this will be the return value
		int freeCount = freeConnections.size();
		// get number of free connections
		if (freeCount == 0) { // if there are no free connections
			log.debug(
				"StandardXADataSource:getFreeConnection  there are no free connections, get a new database connection");
			Connection con = super.getConnection(user, password);
			// get a new database connection
			cur = new StandardXAStatefulConnection(this, con);
			// make the connection stateful
		} else {
			Object o = freeConnections.lastElement(); // get the last element
			cur = (StandardXAStatefulConnection) o;
			// cast to something more convenient
			freeConnections.removeElementAt(freeCount - 1);
			// remove from free list
			cur.timeout = 0; // no timeout until start() called
			cur.timedOut = false; // cancel any time old out
		}
		log.debug(
			"StandardXADataSource:getFreeConnection return a connection from the free list");

		try {
			log.debug(
				"StandardXADataSource:getFreeConnection setAutoCommit(true)");
			// changed by cney - was false
                        cur.con.setAutoCommit(true);
		} catch (SQLException e) {
                  log.error(
                          "StandardXADataSource:getFreeConnection ERROR: Failed while autocommiting a connection: "
					+ e);
		}

		return cur;
	}

	public void closeFreeConnection() {
		log.debug("StandardXADataSource:closeFreeConnection empty method TBD");
	}

	public void setMinCon(int min) {
		this.minCon = min;
	}

	public void setMaxCon(int max) {
		this.maxCon = max;
	}

	public void setDeadLockMaxWait(long deadLock) {
		this.deadLockMaxWait = deadLock;
	}

	public int getMinCon() {
		return this.minCon;
	}

	public int getMaxCon() {
		return this.maxCon;
	}

	public long getDeadLockMaxWait() {
		return this.deadLockMaxWait;
	}

	public int getAllConnections() {
		return xidConnections.size() + freeConnections.size();
	}

	public synchronized void processToWait() throws Exception {
		log.debug("StandardXADataSource:processToWait");
		int currentWait = 0;

		if (maxCon != 0) {
			while ((getAllConnections() >= maxCon)
				&& (currentWait < getDeadLockMaxWait())) {
				dump();
				try {
					synchronized (this) {
						wait(getDeadLockRetryWait());
					}
				} catch (InterruptedException e) {
					log.error(
						"StandardXADataSource:processToWait ERROR: Failed while waiting for an object: "
							+ e);
				}
				currentWait += getDeadLockRetryWait();
			}
			if (getAllConnections() >= getMaxCon())
				throw new Exception("StandardXADataSource:processToWait ERROR : impossible to obtain a new xa connection");
		}
	}

	public void dump() {
		for (int i = 0; i < freeConnections.size(); i++) {
			log.debug(
				"freeConnection:<"
					+ freeConnections.elementAt(i).toString()
					+ ">");
		}
		for (Enumeration enum = xidConnections.elements();
			enum.hasMoreElements();
			) {
			log.debug("xidConnection:<" + enum.nextElement().toString() + ">");
		}

	}

	public void setDeadLockRetryWait(long deadLockRetryWait) {
		this.deadLockRetryWait = deadLockRetryWait;
	}

	public long getDeadLockRetryWait() {
		return this.deadLockRetryWait;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardXADataSource:\n");
		sb.append("     connection count=<"+this.connectionCount+">\n");
		if (deadConnections != null)
		    sb.append("     number of dead connection=<"+this.deadConnections.size()+">\n");
		sb.append("     dead lock max wait=<"+this.deadLockMaxWait+">\n");
		sb.append("     dead lock retry wait=<"+this.deadLockRetryWait+">\n");
		if (driver != null)
			sb.append("     driver=<"+this.driver.toString()+">\n");
		sb.append("     driver name=<"+this.driverName+">\n");
		if (freeConnections != null) 
		    sb.append("     number of *free* connections=<"+this.freeConnections.size()+">\n");
		sb.append("     max con=<"+this.maxCon+">\n");
		sb.append("     min con=<"+this.minCon+">\n");
		sb.append("     prepared stmt cache size=<"+this.preparedStmtCacheSize+">\n");
		sb.append("     transaction manager=<"+this.transactionManager+">\n");
		sb.append("     xid connection size=<"+this.xidConnections.size()+">\n");
		sb.append(super.toString());
		return sb.toString();
	}
}
