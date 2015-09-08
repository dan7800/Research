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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import org.enhydra.jdbc.core.JdbcThreadFactory;
import org.enhydra.jdbc.util.Logger;

/**
 * GenericPool is the main class of the Pool. It works with any kind
 * of object that's implement PoolHelper (must provide specific
 * operation on the specific object)
 * Objects stored in hashtables are GenerationObject object. These objects
 * allow to store multiples things in them, in particular, the
 * generation number to identify the generation of an object.
 */
public class GenericPool {
	private long lifeTime; // lifetime of an object in the pool
	private Hashtable locked, unlocked;
	// two hashtables, to stock objects in use and free objects
        private Vector hitList; // holds expired objects until they are killed.
	private JdbcThreadFactory threadFactory; // thread factory
	private int minSize; // minimum size of the pool, set to 0
	private int maxSize; // maximum size of the pool, if set to 0 : unlimited
	private PoolHelper poolHelper; // object type
	private int count; // count the number of object in the pool
	private boolean gc; // gc system call
	private boolean debug; // debug flag
	private long deadLockMaxWait;
	// time to wait before deadlock (return exception)
	private long deadLockRetryWait; // time to wait before 2 try of loop
	private Logger log;

	/**
	 * checking level object
	 * 0 = no special checking
	 * 1 = just a check on an object
	 * 2 = test the object
	 * 3 = just a check on an object (for all the objects)
	 * 4 = test the object (for all the objects)
	 */
	private int checkLevelObject;

	protected Thread keeper; // thread to clean up dead objects
	protected PoolKeeper poolKeeper;
	private long sleepTime; // sleeptime for the pool keeper

	/**
	 * Generation number.  When an error occurs, all objects of the
	 * same generation or earlier are dropped.
	 */
	protected int generation = 1;

	// Default values
	public static final long DEFAULT_EXPIRATION = 600000; // 10 minutes
	//public static final long    DEFAULT_EXPIRATION = 30000;    // 30 secondes
	public static final long DEFAULT_SLEEPTIME = 300000; // 5 minutes
	public static final int DEFAULT_MINSIZE = 2; // 2 objects
	public static final int DEFAULT_MAXSIZE = 50; // 50 objects
	public static final int DEFAULT_DEADLOCKMAXWAIT = 300000; // 5 minutes
	public static final int DEFAULT_DEADLOCKRETRYWAIT = 10000; // 10 seconds

	/**
	 * Creates an GenericPool with the default params.
	 */
	public GenericPool(PoolHelper helper) {
		this(
			helper,
			DEFAULT_MINSIZE,
			DEFAULT_MAXSIZE,
			DEFAULT_EXPIRATION,
			DEFAULT_SLEEPTIME);
	}

	public GenericPool(PoolHelper helper, int initSize) {
		this(
			helper,
			DEFAULT_MINSIZE,
			initSize,
			DEFAULT_EXPIRATION,
			DEFAULT_SLEEPTIME);
	}

	/**
	 * Constructor, set the two hashtables and set by default the other values
	 */
	public GenericPool(
		PoolHelper helper,
		int minSize,
		int maxSize,
		long lifeTime,
		long sleepTime) {
		this.threadFactory = null;
		this.lifeTime = lifeTime;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.poolHelper = helper;
		// helper is the type of the object (interface)
		this.sleepTime = sleepTime;
		this.checkLevelObject = 0;
		this.deadLockMaxWait = DEFAULT_DEADLOCKMAXWAIT;
		this.deadLockRetryWait = DEFAULT_DEADLOCKRETRYWAIT;
	}

	/**
	 * Start method, to initialize independant values of the pool
	 */
	public synchronized void start() {
		locked = new Hashtable(); // create locked objects pool
		unlocked = new Hashtable(); // create unlocked objects pool
                hitList = new Vector();
		count = 0; // 0 element to start
		gc = false; // do not actions concerning garbage collector
		long now = System.currentTimeMillis(); // current time

		// to obtain to the beginning minSize objects in the pool
		for (int i = 0;
			i < minSize;
			i++) { // count have to be equal to minSize
			try {
				GenerationObject genObject = poolHelper.create();
				unlocked.put(genObject, new Long(now));
				// put it in the unlocked pool
			} catch (Exception e) {
				log.error("Error Exception in GenericPool:start " + e);
			}
			++count; // there is one more element in the pool
		}

		//keeper removes dead or useless objects
		if (threadFactory != null) {
			try {
				this.poolKeeper = new PoolKeeper(sleepTime, this);
				this.keeper = threadFactory.getThread(poolKeeper);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage());
			}
		} else {
                        //keep a handle to the poolkeeper so we can destroy it later
                        this.poolKeeper = new PoolKeeper(sleepTime, this);
                        this.keeper = new Thread(poolKeeper);
			
		}
		keeper.start();
		// start the thread to verify element in the pool(unlocked)
		log.debug("GenericPool:start pool started");
	}

    /*
     * Returns object from the pool or creates a connection
     * outside synchronization to prevent hanging.
     */
    private Object getFromPool(String user, String password)
	throws Exception {

	long now = System.currentTimeMillis(); // current time to compare
	if (getUnlockedObjectCount() > 0) {
	    // now, we have to return an object to the user
	    GenerationObject o = null;
	    Object realObject = null;
	    Long life = null;
		
	    Enumeration e = unlocked.keys(); // then take them
	    while (e.hasMoreElements()) { // for each objects ...
		synchronized (this) {
		    if (!e.hasMoreElements()) break; // recheck for synchronization.
			
		    o = (GenerationObject) e.nextElement();
		    life = (Long) unlocked.get(o);
		    unlocked.remove(o);
		    // In any case the object will be removed.
		    // Prevents others accessing the object while we are
		    // not synchronized.
		    realObject = o.getObj();
		}
		    
		// first, verify if the object is not dead (lifetime)
		if ((now - life.longValue()) > lifeTime) {
		    // object has expired
		    log.debug("GenericPool:getFromPool an object has expired");
		    removeUnlockedObject(o);
		} else {
		    log.debug(
			      "GenericPool:getFromPool check the owner of the connection");
		    if (checkOwner(o, user, password)) {
			log.debug("GenericPool:getFromPool owner is verified");
			// second, verification of the object if needed
			if ((checkLevelObject == 0)
			    || ((checkLevelObject == 1)
				&& poolHelper.checkThisObject(realObject))
			    || ((checkLevelObject == 2)
				&& poolHelper.testThisObject(realObject))) {
				
			    locked.put(o, new Long(now));
			    // put it in the locked pool
			    log.debug(
				      "GenericPool:getFromPool return an object (after verification if needed)");
			    return (o.getObj()); // return this element
			} else { // object failed validation
			    log.debug(
				      "GenericPool:getFromPool kill an object from the pool");
			    removeUnlockedObject(o);
			}
			    
		    } else
			log.debug("GenericPool:getFromPool owner is FALSE");
		}
	    }
	} // if getUnlockedObjectCount() > 0
	    
	    
	// if no objects available, create a new one
	boolean create = false;
	synchronized (this) {
	    if (count < maxSize) {
		create = true;
		count++; // assume we can create a connection.
	    }
	}
	if (create) {
	    // if number of pooled object is < max size of the pool
            log.debug(
                      "GenericPool:getFromPool no objects available, create a new one");
            try {
                GenerationObject genObject = poolHelper.create(user, password);
                
                locked.put(genObject, new Long(now));
                // put it in the locked pool
                return (genObject.getObj()); // and return this element
            } catch (Exception excp) {
                synchronized (this) {
                    count--; // our assumption failed. rollback.
                }

                log.error(
                          "GenericPool:getFromPool Error Exception in GenericPool:getFromPool");
                // cney: rethrow exception thrown by create
                throw excp;
            }
        }

        return null;
    }
        
	public synchronized boolean checkOwner(
		GenerationObject genObject,
		String user,
		String password) {
		return equals(user, genObject.getUser())
			&& equals(password, genObject.getPassword());
	}

	JdbcThreadFactory getThreadFactory() {
		return threadFactory;
	}

	void setThreadFactory(JdbcThreadFactory tf) {
		threadFactory = tf;
	}

	private boolean equals(String a, String b) {
		if (a == null)
			return (b == null);
		if (b == null)
			return (a == null);
		return a.equals(b);
	}

	/**
	 * return pooled object
	 */
	public Object checkOut(String user, String password)
		throws Exception {
		log.debug("GenericPool:checkOut an object");
		long now = System.currentTimeMillis(); // current time to compare
		GenerationObject o;
		Enumeration e;
		Object realObject;
		log.debug(
			"GenericPool:checkOut UnlockedObjectCount="
				+ getUnlockedObjectCount());
		log.debug(
			"GenericPool:checkOut LockedObjectCount=" + getLockedObjectCount());
		log.debug(
			"GenericPool:checkOut count=" + count + " maxSize=" + maxSize);

		if (getUnlockedObjectCount() > 0) {
			// if there are objects in the unlocked pool
			if ((checkLevelObject == 3)
				|| (checkLevelObject == 4)) { //need to verify all the objects
			    
				e = unlocked.keys();
				while (e.hasMoreElements()) {
					o = (GenerationObject) e.nextElement();
					realObject = o.getObj(); // take the current object
					// first, verify if the object is not dead (lifetime)
					if ((now - ((Long) unlocked.get(o)).longValue()) > lifeTime) {
						// object has expired
						log.debug("GenericPool:checkOut an object has expired");
						removeUnlockedObject(o);
						//minimumObject(user, password);
						// build object in the pool if it is lesser than minSize
					} else {
					    log.debug(
					    "GenericPool:checkOut check the owner of the connection");
					    if (checkOwner(o, user, password)) {
					        if (((checkLevelObject == 3)
					                && !poolHelper.checkThisObject(realObject))
					                || ((checkLevelObject == 4)
					                        && !poolHelper.testThisObject(realObject))) {
					            log.debug(
					                    "GenericPool:checkOut remove object checkLevelObject="
					                    + checkLevelObject);
					            removeUnlockedObject(o);
					            //minimumObject(user, password);
					            // build object in the pool if it is lesser than minSize
					        }
					    }
					}
				}
			}



		} 

		int currentWait = 0;

                Object obj = getFromPool(user, password);
		while ((obj == null) && (currentWait < getDeadLockMaxWait())) {
 			log.info("GenericPool:checkOut waiting for an object :"+this.poolHelper.toString());
 			try {
 				synchronized (this) {
					wait(getDeadLockRetryWait());
				}
			} catch (InterruptedException excp) {
				log.error(
					"GenericPool:checkOut ERROR Failed while waiting for an object: "
						+ excp);
			}
			currentWait += getDeadLockRetryWait();
			obj = getFromPool(user, password);
		}

		if (obj == null)
			throw new Exception("GenericPool:checkOut ERROR  impossible to obtain a new object from the pool");

                return obj;
	}

	synchronized public void minimumObject() {
		minimumObject(null, null);
	}

	synchronized public void minimumObject(String user, String password) {
		log.debug(
			"GenericPool:minimumObject create object if there are less than minSize objects in the pool count ="
				+ count);
		if ((count < minSize)
			&& (unlocked != null)) { // if pool has less than minSize elements
			long now = System.currentTimeMillis(); // current time

			for (int i = count;
				i < minSize;
				i++) { //count have to be equal to minSize
				try {
					GenerationObject genObject;
					if ((user != null) && (password != null))
						genObject = poolHelper.create();
					else
						genObject = poolHelper.create(user, password);
					unlocked.put(genObject, new Long(now));
					// put it in the unlocked pool
				} catch (Exception e) {
					log.error(
						"GenericPool:minimumObject Error Exception in GenericPool:minimumObject");
				}
			}
			log.debug(
				"GenericPool:minimumObject count="
					+ count
					+ " Unlocked="
					+ this.getUnlockedObjectCount()
					+ " locked="
					+ this.getLockedObjectCount());

			count = minSize; // pool has now minSize element
		}
	}

	/**
	 * remove object from locked pool
	 */
	public synchronized void checkIn(Object o) {
		log.debug("GenericPool:checkIn return an object to the pool");

		for (Enumeration enum = locked.keys();
			enum.hasMoreElements();
			) { // for each object of
			GenerationObject obj = (GenerationObject) enum.nextElement();
			// the locked pool

			if (obj.getObj().equals(o)) {
				locked.remove(obj); // remove the object from the locked pool
				unlocked.put(obj, new Long(System.currentTimeMillis()));

				// we have to verify if the generation of the object is still valid
				int genObj = obj.getGeneration(); // get the generation number

				// if the generation number of the object is not valid, test the object
				if (generation > genObj) {
					if (!poolHelper.checkThisObject(obj.getObj()))
						// if the object is not valid
						removeUnlockedObject(obj);

				}
				notifyAll();
			}
		}

		if (count > maxSize) {
			// if we have more than maxSize object in the pool
			log.info(
				"GenericPool:checkIn more than maxSize object in the pool");
			Enumeration enum = unlocked.keys(); // get the unlocked pool
			for (int i = maxSize;
				i < count;
				i++) { // try to remove object from this pool
				if (getUnlockedObjectCount() > 0) {
					GenerationObject obj =
						(GenerationObject) enum.nextElement();
					removeUnlockedObject(obj);
				}
			}
			// now, the size of the pool is changed
			// NOTE : count number ca be greater than maxSize here, in case of there is no
			// available object to delete in the unlocked pool
			count = getUnlockedObjectCount() + getLockedObjectCount();
			if (count > maxSize)
				log.warn(
					"GenericPool:checkIn Be careful, the maximum size of the pool does not correspond"
						+ " to your data. When objects will be check in, the pool "
						+ "will decrease");
		}

	}

	synchronized private void removeUnlockedObject(GenerationObject obj) {
		--count;
                notifyAll(); // there is room for new connections.
		unlocked.remove(obj);
                hitList.add(obj); // killing is done by the keeper thread.
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public synchronized void setMinSize(int min) throws Exception {
		if (min < 0)
			throw new Exception("GenericPool:setMinSize Minimum size of the pool can't be lesser than 0");
		else if (min > maxSize)
			throw new Exception(
				"GenericPool:setMinSize Minimum size of the pool can't be greater than the maxSize ("
					+ maxSize
					+ ")");
		else {
			this.minSize = min;
			//minimumObject();            // build object in the pool if it is lesser than minSize
		}

	}

	public synchronized void setMaxSize(int max) throws Exception {
		if (max < 0)
			throw new Exception("GenericPool:setMaxSize Maximum size of the pool can't be lesser than 0");
		else if (max < minSize)
			throw new Exception(
				"GenericPool:setMaxSize Maximum size of the pool can't be lesser than the minSize ("
					+ minSize
					+ ")");
		else {
			this.maxSize = max;
			if (count > max) { // if pool has more than max element
				log.info(
					"GenericPool:setMaxSize pool has more than max element");
				// we try to remove element from the unlocked pool
				Enumeration enum = unlocked.keys();
				for (int i = max; i < count; i++) {
					if (getUnlockedObjectCount() > 0) {
						// if there is element in the unlocked pool
						GenerationObject o =
							(GenerationObject) enum.nextElement();
						removeUnlockedObject(o);
					}
				}

				// new size is :
				count = getUnlockedObjectCount() + getLockedObjectCount();
				// if there is still more than max element, the objects will be removed
				// when the check in operation will be performed.
				if (count > max)
					log.warn(
						"GenericPool:setMaxSize Be careful, the maximum size of "
							+ "the pool does not correspond to your data. When objects "
							+ "will be check in, the pool will decrease");
			}
		}
	}

	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
		log.debug(
			"GenericPool:setGeneration Be careful, it is very dangerous to change "
				+ "the generation number, many objects could be destroyed");
	}

	public void setGC(boolean gc) {
		this.gc = gc;
	}

	/**
	 * level are accepted between 0 and 4
	 */
	public void setCheckLevelObject(int level) {
		if ((level > 0) && (level <= 4))
			this.checkLevelObject = level;
	}

	public void setDeadLockMaxWait(long deadLock) {
		this.deadLockMaxWait = deadLock;
	}

	public void setDeadLockRetryWait(long deadLockRetryWait) {
		this.deadLockRetryWait = deadLockRetryWait;
	}

	public int getMinSize() {
		return minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public long getLifeTime() {
		return lifeTime;
	}

	public boolean isGC() {
		return gc;
	}

	public int getCount() {
		return count;
	}

	public long getSleepTime() {
		return sleepTime;
	}

	public int getGeneration() {
		return generation;
	}

	public int getCheckLevelObject() {
		return checkLevelObject;
	}

	/**
	 * switch off the pool
	 */
	public void stop() {
		log.debug("GenericPool:stop start to stop the pool");
		if ((getLockedObjectCount() != 0) || (getUnlockedObjectCount() != 0)) {
			expireAll(); // try to kill all the objects in the 2 pools
			if (poolKeeper != null)
				poolKeeper.stop(); // release the pool.
			keeper.interrupt(); // and interrupt the pool keeper
			locked.clear(); // clear the locked pool
			unlocked.clear(); // clear the unlocked pool
			locked = null;
			unlocked = null;
			count = 0; // there is no element in the pool
		}
		log.debug("GenericPool:stop pool stopped");
	}

	/**
	 * returns the current number of objects that are locked
	 */
	public int getLockedObjectCount() {
		if (locked != null)
			return locked.size();
		else
			return 0;
	}

	/**
	 * returns the current number of objects that are unlocked
	 */
	public int getUnlockedObjectCount() {
		if (unlocked != null)
			return unlocked.size();
		else
			return 0;
	}

	public long getDeadLockMaxWait() {
		return this.deadLockMaxWait;
	}

	public long getDeadLockRetryWait() {
		return this.deadLockRetryWait;
	}

	/**
	 * returns information from the pool
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("GenericPool:\n");
		sb.append("     num of element =<"+ count+">\n");
		sb.append("     minSize =<"+ minSize+">\n");
		sb.append("     maxSize =<"+ maxSize+">\n");
		sb.append("     lifeTime =<"+ lifeTime+">\n");
		sb.append("     ngeneration =<"+ generation+">\n");
		sb.append("     getLockedObjectCount() =<"+ getLockedObjectCount()+">\n");
		sb.append("     getUnlockedObjectCount() =<"+ getUnlockedObjectCount()+">\n");
		sb.append("     getDeadLockMaxWait() =<"+ getDeadLockMaxWait()+">\n");
		sb.append("     getDeadLockRetryWait() =<"+ getDeadLockRetryWait()+">\n");
			
		if (unlocked != null) {	
			sb.append("Unlocked pool:\n");
			Enumeration e = unlocked.keys();
			while (e.hasMoreElements()) {
				GenerationObject o = (GenerationObject) e.nextElement();
				sb.append(o.getObj().toString());
			}
		}
		
		if (locked != null) {	
			sb.append("Locked pool:\n");
			Enumeration e = unlocked.keys();
			while (e.hasMoreElements()) {
				GenerationObject o = (GenerationObject) e.nextElement();
				sb.append(o.getObj().toString());
			}
		}
		return sb.toString();
	}

	/**
	 * Remove unusable objects from the pool, called by PoolKeeper
	 * Check the unlocked objects for expired members.
	 */
	protected void cleanUp() {
		// During shutdown, unlocked may be null.
                synchronized (this) {
		        if (unlocked == null)
        		    return;
		}
		long now = System.currentTimeMillis(); // current time
		synchronized (this) {
		        for (Enumeration enum = unlocked.keys();
		        	enum.hasMoreElements();
		        	) { // for each object of the
		        	GenerationObject o = (GenerationObject) enum.nextElement();
	        		// unlocked pool
		        	long lasttouch = ((Long) unlocked.get(o)).longValue();
		        	// birth day of the pool

		        	if ((now - lasttouch) > lifeTime) { // if the object has expired
		        		log.debug("GenericPool:cleanUp clean up the pool");
		        		removeUnlockedObject(o);
		        	}
		        }
		}

		// Kill every object in the hit list. We do this outside synchronization
		// in case it takes too long. Note that hitList only grows so this is
		// a safe way to do this.
		while (hitList.size() > 0) { // kill each object.
		        // this might take a while, lets do it outside synchronization.
		        GenerationObject obj = (GenerationObject) hitList.remove(0);
			log.debug("GenericPool:cleanUp killing an object");
			poolHelper.expire(obj.getObj()); // try to "kill" it
			obj.killObject();
                }

		if (isGC()) // if the pool is GCeable
			System.gc(); // launch system call to clean up unused objects

		boolean resize = false;
		// Lets keep the look outside synchronization as object creation
		// may take a while.
		synchronized (this) {
		        resize = count < minSize;
		}

		if (resize) {
			// if there is less than minSize objects in the pool
			log.info(
				"GenericPool:cleanUp less than minSize objects in the pool "
					+ "min="
					+ minSize
					+ " max="
					+ maxSize
					+ " count="
					+ count);
                        while (true) {
				try {
					GenerationObject genObject = poolHelper.create();
					synchronized (this) {
					        unlocked.put(genObject, new Long(now));
						// put it in the unlocked pool
						++count; // there is one more element in the pool
						notifyAll();

						if (count >= minSize) break;
						// we want to make the test inside sync but leave the loop
						// outside sync.
					}

				} catch (Exception e) {
					log.error(
						"GenericPool:cleanUp Error Exception in GenericPool:cleanUp");
				}
                                synchronized (this) {
				    notifyAll();
                                }
			}
			log.info(
				"GenericPool:cleanUp done "
					+ "min="
					+ minSize
					+ " max="
					+ maxSize
					+ " count="
					+ count);

		}
	}

	/**
	 * close all object in the unlocked and locked structures
	 */
	void expireAll() {
		log.debug(
			"GenericPool:expireAll close all object in the unlocked and locked structures");
		for (Enumeration enum = unlocked.keys();
			enum.hasMoreElements();
			) { // for each object of
			GenerationObject o = (GenerationObject) enum.nextElement();
			// the unlocked pool
			poolHelper.expire(o.getObj()); // try to "kill" the object
			o.killObject();
			o = null;
		}
		for (Enumeration enum = locked.keys();
			enum.hasMoreElements();
			) { // for each object of
			GenerationObject o = (GenerationObject) enum.nextElement();
			// the locked pool
			poolHelper.expire(o.getObj()); // try to "kill" the object
			o.killObject();
			o = null;
		}
	}

	/**
	 * Allows to verify if objects from the pool - for the o generation -
	 * are valid or not. (only for the unlocked pool, to avoid to allocate
	 * non-valid object
	 */
	public void nextGeneration(Object obj) {
		log.debug("GenericPool:nextGeneration");
		int genObj = 0;
		for (Enumeration enum = locked.keys();
			enum.hasMoreElements();
			) { // for each object of
			GenerationObject o = (GenerationObject) enum.nextElement();
			// the locked pool

			if (o.getObj().equals(obj))
				genObj = o.getGeneration(); // get the generation number
		}

		for (Enumeration enum = unlocked.keys();
			enum.hasMoreElements();
			) { // for each object of
			GenerationObject o = (GenerationObject) enum.nextElement();
			// the unlocked pool

			if (o.getGeneration() <= genObj) {
				// all objects of the same generation
				// or earlier are dropped
				if (!poolHelper.checkThisObject(o.getObj()))
					// if the object is not valid
					removeUnlockedObject(o);

			}
		}
		++this.generation; // now, we work with the next generation of object

	}

	/**
	 * removes an object for the locked pool, when an error has occurred
	 */
	synchronized public void removeLockedObject(Object obj) {
		log.debug("GenericPool:removeObject remove an object");
		for (Enumeration enum = locked.keys();
			enum.hasMoreElements();
			) { // for each object of
			GenerationObject o = (GenerationObject) enum.nextElement();
			// the locked pool
			if (o.getObj().equals(obj)) {
				locked.remove(o); // remove the object from the locked pool
				--count;
				o.killObject();
				o = null;
			}
		}
	}

	/**
	 * Outputs a log message to the log writer.
	 */
	public void setLogger(Logger alog) {
		log = alog;
	}

	public Hashtable getLockedObject() {
		return locked;

	}
}
