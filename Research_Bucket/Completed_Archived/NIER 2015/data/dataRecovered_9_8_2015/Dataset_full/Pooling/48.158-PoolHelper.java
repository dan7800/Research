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

/**
 * PoolHelper defines methods allowing to make some specific
 * operation on object. These operations are required to work
 * the Generic Pool
 */
public interface PoolHelper {

	public void expire(Object o); // object specific work to kill the object
	public boolean checkThisObject(Object o);
	// check if the object is still valid
	public boolean testThisObject(Object o); // check if the object is closed
	public GenerationObject create() throws SQLException;
	public GenerationObject create(String _user, String _password)
		throws SQLException;
	public String toString();
}
