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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import org.enhydra.jdbc.core.CoreDataSource;
import org.enhydra.jdbc.core.JdbcThreadFactory;
import org.enhydra.jdbc.util.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * StandardPoolDataSource class allows to make some operations on
 * PooledConnection. It implements PoolHelper for the 3 methods :<p>
 * create   : create a PooledConnection<p>
 * create(user,password)   : create a PooledConnection with an other user/password<p>
 * testThisObject : check if the object is still valid<p>
 * checkThisObject : check if the object is closed<p>
 * expire   : kill the object<p>
 */
public class StandardPoolDataSource
	extends CoreDataSource
	implements DataSource, PoolHelper, ConnectionEventListener {

	public ConnectionPoolDataSource cpds; // object to build PooledConnection
	public GenericPool pool; // pool to store StandardDataSource object
	public String dataSourceName; // jndi name for DataSource Factory
	public String jdbcTestStmt;
	// JDBC test statement for checkLevelObject=1 or 2
	public boolean onOff; // If the pool is started or not
	public Context ictx; // the initial context
	public Log glog = LogFactory.getLog("org.enhydra.jdbc.xapool");

	/**
	 * Constructor
	 */
	public StandardPoolDataSource() {
		log = new Logger(glog);
		super.setLogWriter(log);

		pool = new GenericPool(this);
		// create the pool with StandardPoolDataSource object
		pool.setLogger(log);
		onOff = false;
		dataSourceName = null;
	}

	/**
	 * Constructor
	 */
	public StandardPoolDataSource(int initSize) { // with an init Max size
		log = new Logger(glog);
		super.setLogWriter(log);

		pool = new GenericPool(this, initSize);
		// create the pool with StandardPoolDataSource object
		pool.setLogger(log);
		onOff = false;
		dataSourceName = null;
	}

	/**
	 * Constructor
	 */
	public StandardPoolDataSource(ConnectionPoolDataSource cc) {
		cpds = cc;
		log = new Logger(glog);

		super.setLogWriter(log);
		pool = new GenericPool(this);
		// create the pool with StandardPoolDataSource object
		pool.setLogger(log);
		try {
			cpds.setLogWriter(log);
		} catch (SQLException sqle) {

		}

		onOff = false;
		dataSourceName = null;
	}

	/**
	 * Constructor
	 */
	public StandardPoolDataSource(
		ConnectionPoolDataSource cc,
		int initSize) { // with an init Max size
		cpds = cc;
		log = new Logger(glog);
		super.setLogWriter(log);

		pool = new GenericPool(this, initSize);
		// create the pool with StandardPoolDataSource object
		pool.setLogger(log);

		onOff = false;
		dataSourceName = null;
	}

	/**
	 * Set up the data source name, get the initial context,
	 * and lookup in JNDI to obtain a reference of the DataSourceName
	 * this method must be called before a getConnection (in this case
	 * an exception is returned
	 */
	public void setDataSourceName(String dataSourceName) {
		log.debug("StandardPoolDataSource:setDataSourceName");
		this.dataSourceName = dataSourceName; // set up the data source name
		/*
		        synchronized(this) {
		            if (onOff) {
		                pool.stop();
		                onOff = false;
		            }
		        }
		        */
	}

	public String getDataSourceName() {
		return dataSourceName; // return the dataSourceName (jndi mechanism)
	}

	/**
	 * getConnection allows to get an object from the pool and returns it
	 * to the user. In this case, we return an PooledConnection
	 */
	public Connection getConnection() throws SQLException {
		return getConnection(getUser(), getPassword());
	}

	/**
	 * getConnection allows to get an object from the pool and returns it
	 * to the user. In this case, we return an PooledConnection
	 */
	public Connection getConnection(String _user, String _password)
		throws SQLException {
		log.debug("StandardPoolDataSource:getConnection");
		Connection ret = null;
		PooledConnection con = null;

		synchronized (this) {
			if (!onOff) {
				log.debug(
					"StandardPoolDataSource:getConnection must configure the pool...");
				pool.start(); // the pool starts now
				onOff = true; // and is initialized
				log.debug(
					"StandardPoolDataSource:getConnection pool config : \n"
						+ pool.toString());
			}
		}

		try {
			try {
				log.debug(
					"StandardPoolDataSource:getConnection Try to give a "
						+ "connection (checkOut)");
				con = (PooledConnection) pool.checkOut(_user, _password);
				// get a connection from the pool
				log.debug(
					"StandardPoolDataSource:getConnection checkOut return"
						+ "a new connection");
			} catch (Exception e) {
                                e.printStackTrace();
				log.debug(
					"StandardPoolDataSource:getConnection SQLException in StandardPoolDataSource:getConnection"
						+ e);
				throw new SQLException(
					"SQLException in StandardPoolDataSource:getConnection no connection available "
						+ e);
			}

			ret = con.getConnection();
		} catch (Exception e) {
			log.debug("StandardPoolDataSource:getConnection exception" + e);
                        e.printStackTrace();
			SQLException sqle =
				new SQLException(
					"SQLException in StandardPoolDataSource:getConnection exception: "
						+ e);
			if (e instanceof SQLException)
				sqle.setNextException((SQLException) e);
			if (con != null) {
				pool.checkIn(con);
			}
			throw sqle;
		}
		log.debug("StandardPoolDataSource:getConnection return a connection");
		return ret;
	}

	/**
	 * connectionErrorOccurred and connectionClosed are methods
	 * from ConnectionEventListener interface
	 *
	 * Invoked when a fatal connection error occurs,
	 * just before an SQLException is thrown to the application
	 */
	public void connectionErrorOccurred(ConnectionEvent event) {
		Object obj = event.getSource();
		PooledConnection pc = (PooledConnection) obj;
		pool.nextGeneration(pc);
		pool.removeLockedObject(pc); // remove the object from the locked pool
		expire(pc); // kill the connection (from super)
		log.debug(
			"StandardXAPoolDataSource:connectionErrorOccurred remove the object from the pool");
	}

	/**
	 * Invoked when the application calls close()
	 * on its representation of the connection
	 */
	public void connectionClosed(ConnectionEvent event) {
		log.debug(
			"StandardPoolDataSource:connectionClosed close the connection");
		Object obj = event.getSource();
		pool.checkIn(obj);
	}

	/**
	 * object specific work to kill the object
	 */
	public void expire(Object o) {
		log.debug(
			"StandardPoolDataSource:expire expire a connection, remove from the pool");
		if (o == null)
			return;
		try {
			PooledConnection pooledCon = (PooledConnection) o;
			pooledCon.close(); // call close() of PooledConnection
			pooledCon.removeConnectionEventListener(this);
			log.debug("StandardPoolDataSource:expire close the connection");
		} catch (java.sql.SQLException e) {
			log.error(
				"StandardPoolDataSource:expire Error java.sql.SQLException in StandardPoolDataSource:expire");
		}
	}

	/**
	 * This method tests if a connection is closed or not
	 */
	public boolean checkThisObject(Object o) {

		PooledConnection con;
		Connection ret;
		log.debug(
			"StandardPoolDataSource:checkThisObject verify the current object");
		try {
			con = (PooledConnection) o;
			ret = con.getConnection(); // get the connection from the pool
			if (ret.isClosed()) {
				return false;
			}
			try {
				ret.close();
			} catch (Exception e) {
				log.error(
					"StandardPoolDataSource:checkThisObject can't closed the connection: "
						+ e);
			}

			return true;
		} catch (java.sql.SQLException e) {
			log.error(
				"StandardPoolDataSource:checkThisObject Error java.sql.SQLException in StandardPoolDataSource:checkThisObject");
			return false;
		}
	}

	/**
	 * This method tests if a connection is valid or not
	 */
	public boolean testThisObject(Object o) {
		Connection ret = null;
		log.debug(
			"StandardPoolDataSource:testThisObject verify the current object");
		try {
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
		} catch (java.sql.SQLException e) {
			log.error(
				"StandardPoolDataSource:checkThisObject Error java.sql.SQLException in StandardPoolDataSource:testThisObject");
			return false;
		}
	}

	public GenerationObject create() throws SQLException {
		return create(getUser(), getPassword());
	}

	public GenerationObject create(String _user, String _password)
		throws SQLException {
		log.debug(
			"StandardPoolDataSource:create create a connection for the pool");
		GenerationObject genObject;
		PooledConnection pooledCon = cpds.getPooledConnection(_user, _password);
		// get the pooled connection

		pooledCon.addConnectionEventListener(this);
		// add it to the event listener
		log.debug("StandardPoolDataSource:create create a object for the pool");
		genObject =
			new GenerationObject(
				pooledCon,
				pool.getGeneration(),
				_user,
				_password);
		return genObject; // return a connection
	}

	/**
	 * stop method to switch off the pool
	 */
	public void stopPool() {
		pool.stop();
		onOff = false;
		log.debug("StandardPoolDataSource:stopPool stop now the pool");
	}

	public void shutdown(boolean force) {
		stopPool();
	}

	/**
	 * set the logwriter for the current object, the logwriter will be use by
	 * the current object and by the generic pool
	 * @param logWriter a PrintWriter object
	 */
	public void setLogWriter(PrintWriter logWriter) {
		pool.setLogger(log);
		super.setLogger(log);
	}

	/**
	 * set the debug flag
	 * @param debug a boolean flag
	 */
	public void setDebug(boolean debug) {
		super.setDebug(debug);
		pool.setDebug(debug);
	}

	/**
	 * set the minimum size of the pool
	 * @param minSize minimum size of the pool
	 * @throws Exception
	 */
	public void setMinSize(int minSize) throws Exception {
		pool.setMinSize(minSize);
	}

	/**     
	 * set the maximum size of the pool
	 * @param maxSize maximum size of the pool
	 * @throws Exception
	 */
	public void setMaxSize(int maxSize) throws Exception {
		pool.setMaxSize(maxSize);
	}

	/**
	 * set the life time of the pooled objects
	 * @param lifeTime life time of the pooled objects (in milliseconds)
	 */
	public void setLifeTime(long lifeTime) {
		pool.setLifeTime(lifeTime);
	}

	/**
	 * set the sleep time of pooled objects
	 * @param sleepTime sleep time of the pooled objects (in milliseconds)
	 */
	public void setSleepTime(long sleepTime) {
		pool.setSleepTime(sleepTime);
	}

	/**
	 * set the garbage collection option
	 * @param gc true: the garbage collector will be launched when clean up of the
	 * pool, else false
	 */
	public void setGC(boolean gc) {
		pool.setGC(gc);
	}

	/**
	 * set the check level of the pooled object before using them
	 * @param checkLevelObject (<br>
	 * 0 = no special checking
	 * 1 = just a check on an object
	 * 2 = test the object
	 * 3 = just a check on an object (for all the objects)
	 * 4 = test the object (for all the objects)
	 */
	public void setCheckLevelObject(int checkLevelObject) {
		pool.setCheckLevelObject(checkLevelObject);
	}

	/**
	 * set the String to test the jdbc connection before using it
	 * @param jdbcTestStmt an sql statement
	 */
	public void setJdbcTestStmt(String jdbcTestStmt) {
		this.jdbcTestStmt = jdbcTestStmt;
	}

	/**
	 * set the generation number for future connection, the generation number
	 * is used to identify a group a created objects
	 * @param generation an integer value which represents a generation
	 */
	public void setGeneration(int generation) {
		pool.setGeneration(generation);
	}

	/**
	 * set the global time the pool can wait for a free object
	 * @param deadLock in milliseconds
	 */
	public void setDeadLockMaxWait(long deadLock) {
		pool.setDeadLockMaxWait(deadLock);
	}

	/**
	 * set the time before 2 tries when trying to obtain an object from the pool
	 * @param loopWait in milliseconds
	 */
	public void setDeadLockRetryWait(long loopWait) {
		pool.setDeadLockRetryWait(loopWait);
	}

	public PrintWriter getLogWriter() {
		return log;
	}

	public int getMinSize() {
		return pool.getMinSize();
	}

	public int getMaxSize() {
		return pool.getMaxSize();
	}

	public long getLifeTime() {
		return pool.getLifeTime();
	}

	public long getSleepTime() {
		return pool.getSleepTime();
	}

	public int getGeneration() {
		return pool.generation;
	}

	public boolean isGC() {
		return pool.isGC();
	}

	public int getLockedObjectCount() {
		return pool.getLockedObjectCount();
	}

	public int getUnlockedObjectCount() {
		return pool.getUnlockedObjectCount();
	}

	public int getCheckLevelObject() {
		return pool.getCheckLevelObject();
	}

	public String getJdbcTestStmt() {
		return jdbcTestStmt;
	}

	public long getDeadLockMaxWait() {
		return pool.getDeadLockMaxWait();
	}

	public long getDeadLockRetryWait() {
		return pool.getDeadLockRetryWait();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardPoolDataSource:\n");
		sb.append("     data source name=<"+this.dataSourceName+">\n");
		sb.append("     jdbc test stmt=<"+this.jdbcTestStmt+">\n");
		sb.append("     user=<"+this.user+">\n");
		if (this.cpds != null)
			sb.append(this.cpds.toString());
		sb.append(pool.toString());
				
		return sb.toString();
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
			"StandardPoolDataSource:getReference return a reference of the object");
		Reference ref = super.getReference();
		ref.add(
			new StringRefAddr(
				"checkLevelObject",
				Integer.toString(getCheckLevelObject())));
		ref.add(new StringRefAddr("lifeTime", Long.toString(getLifeTime())));
		ref.add(new StringRefAddr("jdbcTestStmt", getJdbcTestStmt()));
		ref.add(new StringRefAddr("maxSize", Integer.toString(getMaxSize())));
		ref.add(new StringRefAddr("minSize", Integer.toString(getMinSize())));
		ref.add(new StringRefAddr("dataSourceName", getDataSourceName()));
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
		this.setLifeTime(
			Long.parseLong((String) ref.get("lifeTime").getContent()));
		this.setJdbcTestStmt((String) ref.get("jdbcTestStmt").getContent());
		this.setMaxSize(
			Integer.parseInt((String) ref.get("maxSize").getContent()));
		this.setMinSize(
			Integer.parseInt((String) ref.get("minSize").getContent()));
		this.setDataSourceName((String) ref.get("dataSourceName").getContent());
		InitialContext ictx = new InitialContext(env);
		cpds = (ConnectionPoolDataSource) ictx.lookup(this.dataSourceName);
		return this;
	}

	/**
	 * Override this so that the pool's tf gets set as well
	 */
	public void setThreadFactory(JdbcThreadFactory tf) {
		super.setThreadFactory(tf);
		pool.setThreadFactory(tf);
	}
}