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

/**
 * PoolKeeper class allows to clean up automatically dead and
 * expired objects.
 */
public class PoolKeeper implements Runnable {

	private long sleepTime; // timeout between 2 clean up
	private GenericPool pool; // the pool to clean up
	private boolean running = true;

	/**
	 * constructor, called by GenericPool (any kind of object)
	 */
	public PoolKeeper(long sleepTime, GenericPool pool) {
		this.sleepTime = sleepTime;
		this.pool = pool;
	}

	public void stop() {
		synchronized (this ) {
                        running = false;
                }
	}

	/**
	 * run method. allows to clean up the pool
	 */
	public void run() {
		while (running && !Thread.interrupted()) {
			try {
				synchronized (this) {
					wait(this.sleepTime); // wait for timeout ms before attack
				}
			} catch (InterruptedException e) {
                                break;
			}
			this.pool.cleanUp(); // clean up the Pool and reallocate objects
		}
		// release the pool.
		this.pool = null;
	}

}