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
 package org.enhydra.jdbc.core;

import org.enhydra.jdbc.util.Logger;
import org.enhydra.jdbc.util.JdbcUtil;

import java.io.Serializable;
import java.io.PrintWriter;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

/**
 * Provides a Data Source which can be used to generate JDBC connections.
 * <P>
 * This class is generic in the sense that it does not rely upon anything other
 * than standard Java APIs. It uses java.sql.DriverManager and preconfigured
 * properties to construct a JDBC connection.
 */
public class CoreDataSource
	extends JdbcUtil
	implements Referenceable, ObjectFactory, Serializable {

	// Standard Data Source properties
	private int loginTimeout; // timeout for database logins
	transient public PrintWriter logWriter; // the log writer
	public String user; // user for the database
	public String password; // password for the database
	private String description; // description of the datasource
	private boolean debug; // debug flag
	private boolean verbose; // verbose flag
	private JdbcThreadFactory threadFactory; // thread factory

	/**
	 * Constructor
	 */
	public CoreDataSource() {
		loginTimeout = 60; // Default value for loginTimeout
		logWriter = null;
		user = null;
		password = null;
		description = null;
		debug = false;
		verbose = false;
		threadFactory = null;
	}

	/**
	 * Setter/Getter defined for standard properties
	 */
	public String getDescription() {
		return description;
	}
	public String getPassword() {
		return password;
	}
	public String getUser() {
		return user;
	}
	public JdbcThreadFactory getThreadFactory() {
		return threadFactory;
	}
	public boolean isDebug() {
		return debug;
	}
	public boolean isVerbose() {
		return verbose;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	public void setThreadFactory(JdbcThreadFactory f) {
		this.threadFactory = f;
	}

	public PrintWriter getLogWriter() {
		return log;
	}

	public void setLogWriter(PrintWriter out) {
		log = (Logger) out;
	}

	/**
	 * shutdown is a placeholder for datasources which should shut down
	 * any pools which they maintain.
	 */
	public void shutdown(boolean force) {
	}

	public void setLoginTimeout(int seconds) {
		loginTimeout = seconds;
	}

	public int getLoginTimeout() {
		return loginTimeout;
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
		ref.add(new StringRefAddr("user", getUser()));
		ref.add(new StringRefAddr("password", getPassword()));
		ref.add(new StringRefAddr("description", getDescription()));
		ref.add(
			new StringRefAddr(
				"loginTimeout",
				Integer.toString(getLoginTimeout())));
		log.debug("CoreDataSource:getReference object returned");
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

		this.setUser((String) ref.get("user").getContent());
		this.setPassword((String) ref.get("password").getContent());
		this.setDescription((String) ref.get("description").getContent());
		this.setLoginTimeout(
			Integer.parseInt((String) ref.get("loginTimeout").getContent()));
		log.debug("CoreDataSource:getObjectInstance instance created");
		return this;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CoreDataSource :\n");
		sb.append("     debug =<"+this.debug+">\n");
		sb.append("     description =<"+this.description+">\n");
		sb.append("     login time out =<"+this.loginTimeout+">\n");
		sb.append("     user =<"+this.user+">\n");
		sb.append("     verbose =<"+this.verbose+">\n");
		
		return sb.toString();
	}
}
