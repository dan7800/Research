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
 * This class allows to store multiple things in the Generic 
 * Pool hashtable. In our first implementation, we store the
 * generation number. It is used to close down all pooled objects
 * that were open when a error occured, allowing new pooled objects
 * to be allocated
 */
public class GenerationObject {
    Object 	obj;		// object to store
    // generation number of the object
    // generation property is managed by GenericPool object
    int 	generation;	
    String	user;
    String	password;

    /**
     * constructor
     */
    public GenerationObject(Object o, int generation) {
        this.obj = o;
        this.generation = generation;
    }

    /**
     * constructor
     */
    public GenerationObject(Object o, int generation, String user, String password) {
        this.obj = o;
        this.generation = generation;
        this.user = user;
        this.password = password;
    }

    public int getGeneration() {
        return this.generation;
    }

    public Object getObj() {
        return this.obj;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public void killObject() {
        obj = null;
    }

}
