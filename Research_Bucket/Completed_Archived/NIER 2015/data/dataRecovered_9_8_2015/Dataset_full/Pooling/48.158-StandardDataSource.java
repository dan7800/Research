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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import org.enhydra.jdbc.core.CoreDataSource;
import java.sql.Driver;
import java.util.Properties;
import org.enhydra.jdbc.util.Logger;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a Data Source which can be used to generate JDBC connections.
 * <P>
 * This class is generic in the sense that it does not rely upon anything other
 * than standard Java APIs. It uses java.sql.DriverManager and preconfigured
 * properties to construct a JDBC connection.
 * Important : networkProtocol, portNumber, serverName are not used. Please use
 * instead the url property.
 */
public class StandardDataSource extends CoreDataSource implements DataSource {

	// Standard Data Source properties
	transient Driver driver;
	String driverName; // name of the Standard JDBC driver
	String url; // an explicit JDBC URL used to access this data source
	private int transIsolation; // transaction isolation level
	private boolean loadedFromCCL = false;

	/**
	 * Constructors
	 */
	public StandardDataSource() {
		// This constructor is needed by the object factory
		super();
		driver = null;
		driverName = "";
		url = "";
		transIsolation = -1; //use default
                setLogger(new Logger(LogFactory.getLog("org.enhydra.jdbc.xapool")));
	}

	protected StandardDataSource(Driver drv) throws SQLException {
		this();
		driver = drv;
		driverName = drv.getClass().getName();
                setLogger(new Logger(LogFactory.getLog("org.enhydra.jdbc.xapool")));
	}

	/**
	 * return the name of the driver
	 * @return the string representation of the driver name
	 */
	public String getDriverName() {
		return driverName;
	}

	/**
	 * return the url of the database
	 * @return the string representation of the database url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * set the name of the jdbc driver
	 * @param driverName the string representation of the jdbc driver name
	 * @throws SQLException
	 */
	public void setDriverName(String driverName) throws SQLException {
		if (!this.driverName.equals(driverName)) {
			this.driverName = driverName;
			driver = null;
		}
		/*
		try {
		driver= (Driver)Class.forName (driverName).newInstance();
		log("StandardDataSource:setDriverName a new driver instance is created");
		} catch (Exception e) {
		throw new SQLException ("Error trying to load driver: "+driverName+"\n"+e.getMessage());
		} // try-catch
		*/
	}

	/**
	 * set the database url
	 * @param url the string representation of the database url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * set the level of the transaction isolation for the current database
	 * @param level the integer level
	 */
	public void setTransactionIsolation(int level) {
		transIsolation = level;
	}

	/**
	 * return the transaction isolation level defined for the current database
	 * @return the transaction isolation level
	 */
	public int getTransactionIsolation() {
		return transIsolation;
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 */
	synchronized public Connection getConnection() throws SQLException {
		return getConnection(user, password);
	}

	/**
	 *
	 * @param u
	 * @param p
	 * @return
	 * @throws SQLException
	 */
	synchronized public Connection getConnection(String u, String p)
		throws SQLException {
		Connection ret = null; // the connection that gets returned
		Properties prop = new Properties();
		if (u != null)
			prop.put("user", u);
		if (p != null)
			prop.put("password", p);
		if (url == null) { // if no explicit url provided
			// Build URL from serverName, NetworkProtocol etc.
		} else { // explicit URL provided
			if (driver == null) {
				try {
					driver = (Driver) Class.forName(driverName).newInstance();
					loadedFromCCL = false;
					log.debug(
						"StandardDataSource:getConnection a new driver instance is created");
				} catch (Exception e) {
					try {
						driver =
							(Driver) Class
								.forName(
									driverName,
									true,
									Thread
										.currentThread()
										.getContextClassLoader())
								.newInstance();
						loadedFromCCL = true;
					} catch (Exception e2) {
						throw new SQLException(
							"Error trying to load driver: "
								+ driverName
								+ " : "
								+ e2.getMessage());
					}
				}
			}
			// commenting out since at least one driver will complain if you
			// instantiate the driver outside the Driver Manager
			// (ie. Cloudscape RMI)
			/*
			if (!driver.acceptsURL(url)) {
			log("Driver does not accept url "+url);
			throw new SQLException("Driver does not accept url "+url);
			}
			*/
			try {
				if (loadedFromCCL) {
					ret = driver.connect(url, prop);
					// Driver creates the connection
				} else {
					ret = DriverManager.getConnection(url, prop);
					// DriverManager creates the connection
				}
				int transIsolation = getTransactionIsolation();
				if (transIsolation >= 0) {
					ret.setTransactionIsolation(transIsolation);
				}
				log.debug(
					"StandardDataSource:getConnection Connection from DriverManager is returned");
			} catch (SQLException e) {
				throw new SQLException(
					"Cannot get connection for URL "
						+ url
						+ " : "
						+ e.getMessage());
			}
		}
		return ret;
	}

	/**
	 * Methods inherited from referenceable
	 */
	public Reference getReference() throws NamingException {
		// Note that we use getClass().getName() to provide the factory
		// class name. It is assumed that this class, and all of its
		// descendants are their own factories.

		Reference ref =
			new Reference(getClass().getName(), getClass().getName(), null);
		ref.add(new StringRefAddr("driverName", getDriverName()));
		ref.add(new StringRefAddr("url", getUrl()));
		ref.add(new StringRefAddr("user", getUser()));
		ref.add(new StringRefAddr("password", getPassword()));
		ref.add(new StringRefAddr("description", getDescription()));
		ref.add(
			new StringRefAddr(
				"loginTimeout",
				Integer.toString(getLoginTimeout())));
		ref.add(
			new StringRefAddr(
				"transIsolation",
				Integer.toString(getTransactionIsolation())));
		log.debug("StandardDataSource:getReference object returned");
		return ref;
	}

	/**
	 * Methods inherited from ObjectFactory
	 */
	public Object getObjectInstance(
		Object refObj,
		Name name,
		Context nameCtx,
		Hashtable env)
		throws Exception {
		Reference ref = (Reference) refObj;
		this.setDriverName((String) ref.get("driverName").getContent());
		this.setUrl((String) ref.get("url").getContent());
		this.setUser((String) ref.get("user").getContent());
		this.setPassword((String) ref.get("password").getContent());
		this.setDescription((String) ref.get("description").getContent());
		this.setLoginTimeout(
			Integer.parseInt((String) ref.get("loginTimeout").getContent()));
		this.setTransactionIsolation(
			Integer.parseInt((String) ref.get("transIsolation").getContent()));
		return this;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		return false;
		/*
		        if (obj instanceof StandardDataSource) {
		            StandardDataSource other = (StandardDataSource)obj;
		            if (!(this.url == null || this.user == null)) {
		                return (this.url.equals(other.url) && this.user.equals(other.user));
		            } else if (this.url != null) {
		                return (this.url.equals(other.url) && this.user == other.user);
		            } else if (this.user != null) {
		                return (this.url == other.url && this.user.equals(other.user));
		            } else {
		                return (this.url == other.url && this.user == other.user);
		            }
		        } else {
		            return false;
		        }
		        */
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardDataSource:\n");
		sb.append("     driver=<" + driver +">\n");
		sb.append("     url=<" + url + ">\n");
		sb.append("     user=<" + user + ">\n");
		sb.append(super.toString());
		
		return sb.toString();
	}

	public int hashCode() {
		return toString().hashCode();
	}
}