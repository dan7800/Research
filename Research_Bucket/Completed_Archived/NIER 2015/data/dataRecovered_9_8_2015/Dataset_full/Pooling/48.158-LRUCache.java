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
package org.enhydra.jdbc.util;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Enumeration;

/**
 * Simple implementation of a cache, using Least Recently Used algorithm
 * for discarding members when the cache fills up
 */
public class LRUCache {
    /** The cache */
    private Hashtable cache = new Hashtable();
    /** The linked list to keep track of least/most recently used */
    private LinkedList lru = new LinkedList();
    /** The maximum size of the cache */
    private int maxSize;

    public Logger log;

    /**
     * Constructor
     */
    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
    }

    public int LRUSize() {
        return lru.size();

    }
    public int cacheSize() {
        return cache.size();

    }

    /**
     * Puts a new object in the cache.  If the cache is full, it removes
     * the least recently used object.  The new object becomes the most
     * recently used object.
     */
    public void put(Object key, Object value) {
        List removed = new ArrayList();
        synchronized (this) {
            // make room if needed
            while (cache.size() + 1 > maxSize) {
                removed.add(removeLRU());
            }
            // remove the key from the list if it's in the cache already
            if (cache.containsKey(key)) {
                lru.remove(key);
            }
            // the last item in the list is the most recently used
            lru.addLast(key);
            // put it in the actual cache
            cache.put(key, value);
        }
        cleanupAll(removed);
    }

    /**
     * Gets an object from the cache.  This object is set to be the
     * most recenty used
     */
    public synchronized Object get(Object key) {
        // check for existence in cache
        if (!cache.containsKey(key)) {
            return null;
        }
        // set to most recently used
        lru.remove(key);
        lru.addLast(key);
        // return the object
        return cache.get(key);
    }

    /**
     * Removes the object from the cache and the lru list
     */
    public synchronized Object remove(Object key) {
        // check for existence in cache
        if (!cache.containsKey(key)) {
            return null;
        }
        // remove from lru list
        lru.remove(key);
        // remove from cache
        Object obj = cache.remove(key);
        return obj;
    }

    private synchronized Object removeLRU() {
        Object obj = cache.remove(lru.getFirst());
        lru.removeFirst();
        return obj;
    }

    /**
     * Resize the cache
     */
    public void resize(int newSize) {
        if (newSize <= 0) {
            return;
        }
        List removed = new ArrayList();
        synchronized (this) {
            maxSize = newSize;
            while (cache.size() > maxSize) {
                removed.add(removeLRU());
            }
        }
        cleanupAll(removed);
    }

    private void cleanupAll(List removed) {
        Iterator it = removed.iterator();
        while (it.hasNext()) {
            cleanupObject(it.next());
        }
    }
    
    /**
     * Override this method to do special cleanup on an object,
     * such as closing a statement or a connection
     */
    protected void cleanupObject(Object o) {}

    public void cleanupAll() {
        for (Enumeration enum = cache.keys();enum.hasMoreElements();) {

            Object o = remove(enum.nextElement());
            cleanupObject(o);

        }
    }

    public void setLogger(Logger alog) {
        log = alog;
    }
}


