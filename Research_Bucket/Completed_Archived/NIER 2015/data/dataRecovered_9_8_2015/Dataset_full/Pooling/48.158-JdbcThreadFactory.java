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

import java.sql.SQLException;

/**
 * Generic interface for getting threads, modeled after Enhydra 
 * ThreadClientService interface.  This allows the JdbcLib code 
 * to be portable across app servers.
 */
public interface JdbcThreadFactory {

	/**
	 * Get a thread for the client. This thread will belong to the
	 * client's thread group.
	 *
	 * @param target
	 *  the Runnable object that will use this thread.
	 * @return 
	 *  the Thread that the client can now use.
	 */
	public Thread getThread(Runnable target) throws SQLException;

	/**
	 * Get a thread for the client. This thread will belong to the
	 * client's thread group.
	 *
	 * @param target
	 *  the Runnable object that will use this thread.
	 * @param name
	 *  the name of the thread. If a <code>null<code> value is given
	 *  an arbitrary name will be provided
	 * @return 
	 *  the Thread that the client can now use.
	 */
	public Thread getThread(Runnable target, String name) throws SQLException;

	/**
	 * Get a thread for the client. This thread will belong to the
	 * the specified thread group, or the client's thread group if none
	 * is specified.
	 *
	 * @param group
	 *  the ThreadGroup to which the new thread will be added. If
	 *  <code>null</code> the new thread is added to the same thread
	 *  group as the currently executing thread.
	 * @param target
	 *  the Runnable object that will use this thread.
	 * @return 
	 *  the Thread that the client can now use.
	 */
	public Thread getThread(ThreadGroup group, Runnable target)
		throws SQLException;

	/**
	 * Get a thread for the client. This thread will belong to the
	 * the specified thread group, or the client's thread group if none
	 * is specified.
	 *
	 * @param group
	 *  the ThreadGroup to which the new thread will be added. If
	 *  <code>null</code> the new thread is added to the same thread
	 *  group as the currently executing thread.
	 * @param target
	 *  the Runnable object that will use this thread.
	 * @param name
	 *  the String name ofthe new thread. If a <code>null</code> value
	 *  is given an arbitrary name will be provided.
	 * @return 
	 *  the Thread that the client can now use.
	 */
	public Thread getThread(ThreadGroup group, Runnable target, String name)
		throws SQLException;
}
