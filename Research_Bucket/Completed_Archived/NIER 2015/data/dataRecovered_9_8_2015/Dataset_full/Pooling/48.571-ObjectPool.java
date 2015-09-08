/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool;


import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A generic object pool.  You must provide a PoolObjectFactory (or the class
 * of a Java Bean) so the pool knows what kind of objects to create.  It has
 * many configurable parameters, such as the minimum and maximum size of the
 * pool, whether to enable idle timeouts, etc.  If the pooled objects
 * implement PooledObject, they will automatically be returned to the pool at
 * the appropriate times.
 * <P>In general, the appropriate way to use a pool is:</P>
 * <OL>
 *   <LI>Create it</LI>
 *   <LI>Configure it (set factory, name, parameters, etc.)</LI>
 *   <LI>Initialize it (once done, further configuration is not allowed)</LI>
 *   <LI>Use it</LI>
 *   <LI>Shut it down</LI>
 * </OL>
 * @see org.ofbiz.minerva.pool.PooledObject
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 *
 * Revision:
 * 20010701 danch added code for timeout in blocking.
 */
public class ObjectPool implements PoolEventListener {

    private final static String INITIALIZED = "Pool already initialized!";
    private final static PoolGCThread collector = new PoolGCThread();

    static {
        collector.start();
    }

    private Logger log = Logger.getLogger(ObjectPool.class);
    private PoolObjectFactory factory;
    private String poolName;

    private final Map objects = new HashMap();
    private final Set deadObjects = Collections.synchronizedSet(new HashSet());
    private int minSize = 0;
    private int maxSize = 0;
    private boolean idleTimeout = false;
    private boolean runGC = false;
    private float maxIdleShrinkPercent = 1.0f; // don't replace idle connections that timeout
    private long idleTimeoutMillis = 1800000l; // must be idle in pool for 30 minutes
    private long gcMinIdleMillis = 1200000l; // must be unused by client for 20 minutes
    private long gcIntervalMillis = 120000l; // shrink & gc every 2 minutes
    private long lastGC = System.currentTimeMillis();
    private boolean blocking = true;
    private int blockingTimeout = 10000;//Integer.MAX_VALUE;//this is silly
    private boolean trackLastUsed = false;
    private boolean invalidateOnError = false;

    private FIFOSemaphore permits;
    private boolean initialized = false;

    /**
     * Creates a new pool.  It cannot be used until you specify a name and
     * object factory or bean class, and initialize it.
     * @see #setName
     * @see #setObjectFactory
     * @see #initialize
     */
    public ObjectPool() {
    }

    /**
     * Creates a new pool with the specified parameters.  It cannot be used
     * until you initialize it.
     * @param factory The object factory that will create the objects to go in
     *    the pool.
     * @param poolName The name of the pool.  This does not have to be unique
     *    across all pools, but it is strongly recommended (and it may be a
     *    requirement for certain uses of the pool).
     * @see #initialize
     */
    public ObjectPool(PoolObjectFactory factory, String poolName) {
        setObjectFactory(factory);
        setName(poolName);
    }

    /**
     * Creates a new pool with the specified parameters.  It cannot be used
     * until you initialize it.
     * @param javaBeanClass The Class of a Java Bean.  New instances for the
     *    pool will be created with the no-argument constructor, and no
     *    particular initialization or cleanup will be performed on the
     *    instances.  Use a PoolObjectFactory if you want more control over
     *    the instances.
     * @param poolName The name of the pool.  This does not have to be unique
     *    across all pools, but it is strongly recommended (and it may be a
     *    requirement for certain uses of the pool).
     * @see #initialize
     */
    public ObjectPool(Class javaBeanClass, String poolName) {
        setObjectFactory(javaBeanClass);
        setName(poolName);
    }

    /**
     * Sets the object factory for the pool.  The object factory controls the
     * instances created for the pool, and can initialize instances given out
     * by the pool and cleanup instances returned to the pool.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the object factory after the pool has been
     *    initialized.
     */
    public void setObjectFactory(PoolObjectFactory factory) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        this.factory = factory;
    }

    /**
     * Sets the object factory as a new factory for Java Beans.  New instances
     *    for the pool will be created with the no-argument constructor, and no
     *    particular initialization or cleanup will be performed on the
     *    instances.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the object factory after the pool has been
     *    initialized.
     */
    public void setObjectFactory(Class javaBeanClass) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        factory = new BeanFactory(javaBeanClass);
    }

    /**
     * Sets the name of the pool.  This is not required to be unique across all
     * pools, but is strongly recommended.  Certain uses of the pool (such as
     * a JNDI object factory) may require it.  This must be set exactly once
     * for each pool (it may be set in the constructor).
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the name of the pool more than once.
     */
    public void setName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Cannot set pool name to null or empty!");
        if (poolName != null && !poolName.equals(name))
            throw new IllegalStateException("Cannot change pool name once set!");
        poolName = name;
        log = Logger.getLogger(ObjectPool.class.getName() + "." + name);
    }

    /**
     * Gets the name of the pool.
     */
    public String getName() {
        return poolName;
    }

    /**
     * Sets the minimum size of the pool.  The pool will create this many
     * instances at startup, and once running, it will never shrink below this
     * size.  The default is zero.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the minimum size after the pool has been
     *    initialized.
     */
    public void setMinSize(int size) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        minSize = size;
        if (maxSize != 0 && minSize > maxSize) {
            maxSize = minSize;
            log.warn("pool max size set to " + maxSize + " to stay >= min size");
        }
    }

    /**
     * Gets the minimum size of the pool.
     * @see #setMinSize
     */
    public int getMinSize() {
        return minSize;
    }

    /**
     * Sets the maximum size of the pool.  Once the pool has grown to hold this
     * number of instances, it will not add any more instances.  If one of the
     * pooled instances is available when a request comes in, it will be
     * returned.  If none of the pooled instances are available, the pool will
     * either block until an instance is available, or return null.  The default
     * is no maximum size.
     * @see #setBlocking
     * @param size The maximum size of the pool, or 0 if the pool should grow
     *    indefinitely (not recommended).
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the maximum size after the pool has been
     *    initialized.
     */
    public void setMaxSize(int size) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        maxSize = size;
        if (maxSize != 0 && minSize > maxSize) {
            minSize = maxSize;
            log.warn("pool min size set to " + minSize + " to stay <= max size");
        }
    }

    /**
     * Gets the maximum size of the pool.
     * @see #setMaxSize
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets whether the pool should release instances that have not been used
     * recently.  This is intended to reclaim resources (memory, database
     * connections, file handles, etc) during periods of inactivity.  This runs
     * as often as garbage collection (even if garbage collection is disabled,
     * this uses the same timing parameter), but the required period of
     * inactivity is different.  All objects that have been unused for more
     * than the idle timeout are closed, but if you set the MaxIdleShrinkPercent
     * parameter, the pool may recreate some objects so the total number of
     * pooled instances doesn't shrink as rapidly. Also, under no circumstances
     * will the number of pooled instances fall below the minimum size.</p>
     * <P>The default is disabled.</P>
     * @see #setGCInterval
     * @see #setIdleTimeout
     * @see #setMaxIdleTimeoutPercent
     * @see #setMinSize
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the idle timeout state after the pool has
     *    been initialized.
     */
    public void setIdleTimeoutEnabled(boolean enableTimeout) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        idleTimeout = enableTimeout;
    }

    /**
     * Gets whether the pool releases instances that have not been used
     * recently.  This is different than garbage collection, which returns
     * instances to the pool if a client checked an instance out but has not
     * used it and not returned it to the pool.
     * @see #setIdleTimeoutEnabled
     */
    public boolean isIdleTimeoutEnabled() {
        return idleTimeout;
    }

    /**
     * Sets whether garbage collection is enabled.  This is the process of
     * returning objects to the pool if they have been checked out of the pool
     * but have not been used in a long periond of time.  This is meant to
     * reclaim resources, generally caused by unexpected failures on the part
     * of the pool client (which forestalled returning an object to the pool).
     * This runs on the same schedule as the idle timeout (if enabled), but
     * objects that were just garbage collected will not be eligible for the
     * idle timeout immediately (after all, they presumably represented "active"
     * clients).  Objects that are garbage collected will be checked out again
     * immediately if a client is blocking waiting for an object.  The default
     * value is disabled.
     * @see #setGCMinIdleTime
     * @see #setGCInterval
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection state after the pool
     *    has been initialized.
     */
    public void setGCEnabled(boolean enabled) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        runGC = enabled;
    }

    /**
     * Gets whether garbage collection is enabled.
     * @see #setGCEnabled
     */
    public boolean isGCEnabled() {
        return runGC;
    }

    /**
     * Sets the idle timeout percent as a fraction between 0 and 1.  If a number
     * of objects are determined to be idle, they will all be closed and
     * removed from the pool.  However, if the ratio of objects released to
     * objects in the pool is greater than this fraction, some new objects
     * will be created to replace the closed objects.  This prevents the pool
     * size from decreasing too rapidly.  Set to 0 to decrease the pool size by
     * a maximum of 1 object per test, or 1 to never replace objects that have
     * exceeded the idle timeout.  The pool will always replace enough closed
     * connections to stay at the minimum size.
     * @see #setIdleTimeoutEnabled
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the idle timeout percent after the pool
     *    has been initialized.
     * @throws java.lang.IllegalArgumentException
     *    Occurs when the percent parameter is not between 0 and 1.
     */
    public void setMaxIdleTimeoutPercent(float percent) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        if (percent < 0f || percent > 1f)
            throw new IllegalArgumentException("Percent must be between 0 and 1!");
        maxIdleShrinkPercent = percent;
    }

    /**
     * Gets the idle timeout percent as a fraction between 0 and 1.
     * @see #setMaxIdleTimeoutPercent
     */
    public float getMaxIdleTimeoutPercent() {
        return maxIdleShrinkPercent;
    }

    /**
     * Sets the minimum idle time to release an unused object from the pool.  If
     * the object is not in use and has not been used for this amount of time,
     * it will be released from the pool.  If timestamps are enabled, the client
     * may update the last used time.  Otherwise, the last used time is only
     * updated when an object is acquired or released.  The default value is
     * 30 minutes.
     * @see #setIdleTimeoutEnabled
     * @param millis The idle time, in milliseconds.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the idle timeout after the pool
     *    has been initialized.
     */
    public void setIdleTimeout(long millis) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        idleTimeoutMillis = millis;

        if (log.isDebugEnabled())
            log.debug("setIdleTimeout(" + millis + ")");
    }

    /**
     * Gets the minimum idle time to release an unused object from the pool.
     * @see #setIdleTimeout
     */
    public long getIdleTimeout() {
        return idleTimeoutMillis;
    }

    /**
     * Sets the minimum idle time to make an object eligible for garbage
     * collection.  If the object is in use and has not been used for this
     * amount of time, it may be returned to the pool.  If timestamps are
     * enabled, the client may update the last used time (this is generally
     * recommended if garbage collection is enabled).  Otherwise, the last used
     * time is only updated when an object is acquired or released.  The default
     * value is 20 minutes.
     * @see #setGCEnabled
     * @param millis The idle time, in milliseconds.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection idle time after the
     *    pool has been initialized.
     */
    public void setGCMinIdleTime(long millis) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        gcMinIdleMillis = millis;

        if (log.isDebugEnabled())
            log.debug("setGCMinIdleTime(" + millis + ")");
    }

    /**
     * Gets the minimum idle time to make an object eligible for garbage
     * collection.
     * @see #setGCMinIdleTime
     */
    public long getGCMinIdleTime() {
        return gcMinIdleMillis;
    }

    /**
     * Sets the length of time between garbage collection and idle timeout runs.
     * This is inexact - if there are many pools with garbage collection and/or
     * the idle timeout enabled, there will not be a thread for each one, and
     * several nearby actions may be combined.  Likewise if the collection
     * process is lengthy for certain types of pooled objects (not recommended),
     * other actions may be delayed.  This is to prevend an unnecessary
     * proliferation of threads.  Note that this parameter controls
     * both garbage collection and the idle timeout - and they will be performed
     * together if both are enabled.  The deafult value is 2 minutes.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection interval after the
     *    pool has been initialized.
     */
    public void setGCInterval(long millis) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);

        gcIntervalMillis = millis;

        if (log.isDebugEnabled())
            log.debug("setGCInterval(" + gcIntervalMillis + ")");
    }

    /**
     * Gets the length of time between garbage collection and idle timeout runs.
     * @see #setGCInterval
     */
    public long getGCInterval() {
        return gcIntervalMillis;
    }

    /**
     * Sets whether a request for an object will block if the pool size is
     * maxed out and no objects are available.  If set to block, the request
     * will not return until an object is available.  Otherwise, the request
     * will return null immediately (and may be retried).  If multiple
     * requests block, there is no guarantee which will return first.  The
     * default is to block.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the blocking parameter after the
     *    pool has been initialized.
     */
    public void setBlocking(boolean blocking) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        this.blocking = blocking;
    }

    /**
     * Gets whether a request for an object will block if the pool size is
     * maxed out and no objects are available.
     * @see #setBlocking
     */
    public boolean isBlocking() {
        return blocking;
    }

    /** sets how long to wait for a free object when blocking, -1 indicating
     *  forever.
     */
    public void setBlockingTimeout(int blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

    /** get how long this pool will wait for a free object while blocking */
    public int getBlockingTimeout() {
        return this.blockingTimeout;
    }

    /**
     * Sets whether object clients can update the last used time.  If not, the
     * last used time will only be updated when the object is given to a client
     * and returned to the pool.  This time is important if the idle timeout or
     * garbage collection is enabled (particularly the latter).  The default
     * is false.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the timestamp parameter after the
     *    pool has been initialized.
     */
    public void setTimestampUsed(boolean timestamp) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);

        trackLastUsed = timestamp;

        if (log.isDebugEnabled())
            log.debug("setTimestampUsed(" + timestamp + ")");
    }

    /**
     * Gets whether object clients can update the last used time.
     */
    public boolean isTimestampUsed() {
        return trackLastUsed;
    }

    /**
     * Sets the response for object errors.  If this flag is set and an error
     * event occurs, the object is removed from the pool entirely.  Otherwise,
     * the object is returned to the pool of available objects.  For example, a
     * SQL error may not indicate a bad database connection (flag not set),
     * while a TCP/IP error probably indicates a bad network connection (flag
     * set).  If this flag is not set, you can still manually invalidate
     * objects using markObjectAsInvalid.
     * @see #markObjectAsInvalid
     * @see #objectError
     */
    public void setInvalidateOnError(boolean invalidate) {
        if (initialized)
            throw new IllegalStateException(INITIALIZED);
        invalidateOnError = invalidate;
    }

    /**
     * Gets whether objects are removed from the pool in case of errors.
     */
    public boolean isInvalidateOnError() {
        return invalidateOnError;
    }

    /**
     * Prepares the pool for use.  This must be called exactly once before
     * getObject is even called.  The pool name and object factory must be set
     * before this call will succeed.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to initialize the pool without setting the object
     *    factory or name, or you initialize the pool more than once.
     */
    public void initialize() {
        if (factory == null || poolName == null)
            throw new IllegalStateException("Factory and Name must be set before pool initialization!");
        if (initialized)
            throw new IllegalStateException("Cannot initialize more than once!");
        initialized = true;
        permits = new FIFOSemaphore(maxSize);
        factory.poolStarted(this);
        lastGC = System.currentTimeMillis();
        //fill pool to min size
        fillToMin();
        /*
        int max = maxSize <= 0 ? minSize : Math.min(minSize, maxSize);
        Collection cs = new LinkedList();
        for(int i=0; i<max; i++)
        {
           cs.add(getObject(null));
        }
        while (Iterator i = cs.iterator(); i.hasNext();)
        {
           releaseObject(i.next());
        } // end of while ()
        */
        collector.addPool(this);
    }

    /**
     * Shuts down the pool.  All outstanding objects are closed and all objects
     * are released from the pool.  No getObject or releaseObject calls will
     * succeed after this method is called - and they will probably fail during
     * this method call.
     */
    public void shutDown() {
        collector.removePool(this);
        factory.poolClosing(this);

        // close all objects
        synchronized (objects) {
            for (Iterator it = objects.values().iterator(); it.hasNext();) {
                ObjectRecord rec = (ObjectRecord) it.next();
                if (null != rec) {
                    if (rec.isInUse())
                        factory.returnObject(rec.getClientObject());
                    factory.deleteObject(rec.getObject());
                    rec.close();
                }
            }
            objects.clear();
            deadObjects.clear();
        }//end of synch

        factory = null;
        poolName = null;
        initialized = false;
    }

    /**
     * Gets an object from the pool.  If all the objects in the pool are in use,
     * creates a new object, adds it to the pool, and returns it.  If all
     * objects are in use and the pool is at maximum size, will block or
     * return null.
     * @see #setBlocking
     */
    public Object getObject() {
        return getObject(null);
    }

    /**
     * Gets an object that fits the specified parameters from the pool.
     * If all the objects in the pool are in use or don't fit, creates
     * a new object, adds it to the pool, and returns it.  If all
     * objects are in use or don't fit and the pool is at maximum size,
     * will block or return null.
     * @see #setBlocking
     */
    public Object getObject(Object parameters) {
        if (objects == null)
            throw new IllegalStateException("Tried to use pool before it was Initialized or after it was ShutDown!");

        Object result = factory.isUniqueRequest();
        if (result != null) // If this is identical to a previous request,
            return result;  // return the same result.  This is the 2.4 total hack to
                            // share local connections within a managed tx.

        try {
            if (permits.attempt(blockingTimeout)) {
                ObjectRecord rec = null;
                synchronized (objects) {
                    for (Iterator it = objects.values().iterator(); it.hasNext();) {
                        rec = (ObjectRecord) it.next();
                        if (null != rec && !rec.isInUse() && factory.checkValidObject(rec.getObject(), parameters)) {
                            log.info("Handing out from pool object: " + rec.getObject());
                            try {
                                rec.setInUse(true);
                            } catch (ConcurrentModificationException e) {
                                log.info("Conflict trying to set rec. in use flag:" + rec.getObject());
                                // That's OK, just go on and try another object
                                continue;//shouldn't happen now.
                            }
                            break;
                        }
                        rec = null;//not found
                    }
                }//synch on objects

                if (rec == null) {
                    try {
                        rec = createNewObject(parameters);
                    } catch (Exception e) {
                        log.error("Exception in creating new object for pool", e);
                        permits.release();
                        throw e;
                    }
                } // end of if ()
                if (rec == null) {
                    permits.release();
                    String message = "Pool is broken, did not find or create an object";
                    log.error(message);
                    throw new RuntimeException(message);
                } // end of if ()
                Object ob = rec.getObject();

                result = factory.prepareObject(ob);
                if (result != ob) rec.setClientObject(result);
                if (result instanceof PooledObject)
                    ((PooledObject) result).addPoolEventListener(this);

                log.debug("Pool " + this + " gave out object: " + result);
                return result;
            }//end of permits
            else {
                //we timed out
                throw new RuntimeException("No ManagedConnections Available!");
            } // end of else
        }//try
        catch (RuntimeException e) {
            throw e;
        } // end of catch
        catch (InterruptedException ie) {
            log.info("Interrupted while requesting permit!", new Exception("stacktrace"));
            throw new RuntimeException("Interrupted while requesting permit!");
        } // end of try-catch
        catch (Exception e) {
            log.info("problem getting connection from pool", e);
            throw new RuntimeException("problem getting connection from pool " + e.getMessage());
        } // end of catch
    }

    /**
     * Sets the last used time for an object in the pool that is currently
     * in use.  If the timestamp parameter is not set, this call does nothing.
     * Otherwise, the object is marked as last used at the current time.
     * @throws java.lang.IllegalArgumentException
     *         Occurs when the object is not recognized by the factory or not
     *         in the pool.
     * @throws java.lang.IllegalStateException
     *         Occurs when the object is not currently in use.
     * @see #setTimestampUsed
     */
    public void setLastUsed(Object object) {
        if (!trackLastUsed) return;
        Object ob = null;
        try {
            ob = factory.translateObject(object);
        } catch (Exception e) {
            throw new IllegalArgumentException("Pool " + getName() + " does not recognize object for last used time: " + object);
        }
        ObjectRecord rec = ob == null ? null : (ObjectRecord) objects.get(ob);
        if (rec == null)
            throw new IllegalArgumentException("Pool " + getName() + " does not recognize object for last used time: " + object);
        if (rec.isInUse())
            rec.setLastUsed();
        else
            throw new IllegalStateException("Cannot set last updated time for an object that's not in use!");
    }

    /**
     * Indicates that an object is no longer valid, and should be removed from
     * the pool entirely.  This should be called before the object is returned
     * to the pool (specifically, before factory.returnObject returns), or else
     * the object may be given out again by the time this is called!  Also, you
     * still need to actually return the object to the pool by calling
     * releaseObject, if you aren't calling this during that process already.
     * @param object The object to invalidate, which must be the exact object
     *               returned by getObject
     */
    public void markObjectAsInvalid(Object object) {
        if (deadObjects == null)
            throw new IllegalStateException("Tried to use pool before it was Initialized or after it was ShutDown!");
        deadObjects.add(object); //a synchronized set

    }

    /**
     * Returns an object to the pool.  This must be the exact object that was
     * given out by getObject, and it must be returned to the same pool that
     * generated it.  If other clients are blocked waiting on an object, the
     * object may be re-released immediately.
     * @throws java.lang.IllegalArgumentException
     *    Occurs when the object is not in this pool.
     */
    public void releaseObject(Object object) {

        log.debug("Pool " + this + " object released: " + object);

        Object pooled = null;
        try {
            factory.returnObject(object);//do this first
            pooled = factory.translateObject(object);
        } catch (Exception e) {
            return;        // We can't release it if the factory can't recognize it
        }
        if (pooled == null) // We can't release it if the factory can't recognize it
            return;
        boolean removed = false;
        synchronized (objects) {
            ObjectRecord rec = (ObjectRecord) objects.get(pooled);
            if (rec == null) // Factory understands it, but we don't
                throw new IllegalArgumentException("Object " + object + " is not in pool " + poolName + "!");
            if (!rec.isInUse()) return; // Must have been released by GC?
            if (object instanceof PooledObject)
                ((PooledObject) object).removePoolEventListener(this);
            removed = deadObjects.remove(object);
            rec.setInUse(false);
            if (removed) {
                log.debug("Object was dead: " + object);
                objects.remove(pooled);
                rec.close();
            } // end of if ()

        }//end of synch on objects
        if (removed) {
            try {
                factory.deleteObject(pooled);
            } catch (Exception e) {
                log.error("Pool " + this + " factory (" + factory.getClass().getName() + " delete error: ", e);
            }
            fillToMin();
            /*FIXME --MINSIZE
              if(objects.size() < minSize)
              createNewObject(null);
            */
        }

        if (removed)
            log.debug("Pool " + this + " destroyed object " + object + ".");
        else
            log.debug("Pool " + this + " returned object " + object + " to the pool.");

        permits.release();
        /*
        if(blocking)
        {
           synchronized(this)
           {
              notify();
           }
        }
        */
    }

    private int getUsedCount() {
        int total = 0;
        synchronized (objects) {
            for (Iterator it = new HashSet(objects.values()).iterator(); it.hasNext();) {
                ObjectRecord or = (ObjectRecord) it.next();
                if (or != null && or.isInUse())
                    ++total;
            }
        }
        return total;
    }

    /**
     * Returns the pool name and status.
     */
    public String toString() {
        return poolName + " [" + getUsedCount() + "/" + (objects == null ? 0 : objects.size()) + "/" + (maxSize == 0 ? "Unlimited" : Integer.toString(maxSize)) + "]";
    }


    // ---- PoolEventListener Implementation ----

    /**
     * If the object has been closed, release it.
     */
    public void objectClosed(PoolEvent evt) {
        releaseObject(evt.getSource());
    }

    /**
     * If the invalidateOnError flag is set, the object will be removed from
     * the pool entirely when the client has finished with it.
     */
    public void objectError(PoolEvent evt) {
        if (invalidateOnError || evt.isCatastrophic())
            markObjectAsInvalid(evt.getSource());
    }

    /**
     * If we're tracking the last used times, update the last used time for the
     * specified object.
     */
    public void objectUsed(PoolEvent evt) {
        if (!trackLastUsed) return;
        setLastUsed(evt.getSource());
    }

    long getNextGCMillis(long now) {
        long t = lastGC + gcIntervalMillis - now;

        log.debug("getNextGCMillis(): returning " + t);

        if (!runGC)
            return Long.MAX_VALUE;

        return t;
    }

    // Allow GC if we're within 10% of the desired interval
    boolean isTimeToGC() {
        long now;
        now = System.currentTimeMillis();

        log.debug("isTimeToGC(): " + (now >= lastGC + Math.round((float) gcIntervalMillis * 0.9f)));

        return now >= lastGC + Math.round((float) gcIntervalMillis * 0.9f);

    }

    void runGCandShrink() {

        log.debug("runGCandShrink(): runGC = " + runGC + "; idleTimeout = " + idleTimeout);

        if (runGC || idleTimeout) {
            HashSet objsCopy;
            synchronized (objects) {
                objsCopy = new HashSet(objects.values());
            }

            if (runGC) { // Garbage collection - return any object that's been out too long with no use
                Iterator it = objsCopy.iterator();
                while (it.hasNext()) {
                    ObjectRecord rec = (ObjectRecord) it.next();
                    if (rec != null && rec.isInUse() && rec.getMillisSinceLastUse() >= gcMinIdleMillis) {
                        releaseObject(rec.getClientObject());
                    }
                }
            }
            if (idleTimeout) { // Shrinking the pool - remove objects from the pool if they have not been used in a long time
                // Find object eligible for removal
                HashSet eligible = new HashSet();
                Iterator it = objsCopy.iterator();
                while (it.hasNext()) {
                    ObjectRecord rec = (ObjectRecord) it.next();
                    if (rec != null && !rec.isInUse() && rec.getMillisSinceLastUse() > idleTimeoutMillis)
                        eligible.add(rec);
                }
                // Calculate max number of objects to remove without replacing
                int max = Math.round(eligible.size() * maxIdleShrinkPercent);
                if (max == 0 && eligible.size() > 0) max = 1;
                int count = 0;
                // Attempt to remove that many objects
                it = eligible.iterator();
                while (it.hasNext()) {
                    try {
                        // Delete the object
                        ObjectRecord rec = (ObjectRecord) it.next();
                        if (rec != null) {
                            rec.setInUse(true);  // Don't let someone use it while we destroy it
                            Object pooled = rec.getObject();
                            synchronized (objects) {
                                objects.remove(pooled);
                            }
                            //removeObject(pooled);
                            try {
                                factory.deleteObject(pooled);
                            } catch (Exception e) {
                                log.error("Pool " + this + " factory (" + factory.getClass().getName() + " delete error: ", e);
                            }
                            rec.close();
                            ++count;
                        }
                        fillToMin();
                        /*FIXME --MINSIZE
                                    if(count > max || objects.size() < minSize)
                                       createNewObject(null);

                        */
                    } catch (ConcurrentModificationException e) {
                    }
                }
            }
        }
        lastGC = System.currentTimeMillis();
    }

    /**
     * Removes an object from the pool.  Only one thread can add or remove
     * an object at a time.
     */
    /*   private void removeObject(Object pooled)
    {
       synchronized(objects)
       {
          objects.remove(pooled);
       }
    }
    */
    /**
     * Creates a new Object.
     * @param parameters If <b>true</b>, then the object is locked and
     *          translated by the factory, and the resulting object
     *          returned.  If <b>false</b>, then the object is left in the
     *          pool unlocked.
     */
    private ObjectRecord createNewObject(Object parameters) {
        Object ob = null;
        try {
            ob = factory.createObject(parameters);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create connection");
        }
        if (ob != null) { // if factory can create object
            ObjectRecord rec = new ObjectRecord(ob);
            synchronized (objects) {
                objects.put(ob, rec);
            }
            return rec;
        } else {
            throw new RuntimeException("could not create new object!");
        }
    }

    public void fillToMin() {
        Collection newMCs = new ArrayList();
        try {
            while (objects.size() < minSize) {
                newMCs.add(getObject(null));
            } // end of while ()
        } catch (Exception re) {
            //Whatever the reason, stop trying to add more!
        } // end of try-catch
        for (Iterator i = newMCs.iterator(); i.hasNext();) {
            releaseObject(i.next());
        } // end of for ()

    }

}

class BeanFactory extends PoolObjectFactory {

    private Class beanClass;

    private Logger log = Logger.getLogger(BeanFactory.class);

    public BeanFactory(Class beanClass) {
        try {
            beanClass.getConstructor(new Class[0]);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean class doesn't have no-arg constructor!");
        }
        this.beanClass = beanClass;
    }

    public void poolStarted(ObjectPool pool) {
        super.poolStarted(pool);
    }

    public Object createObject(Object parameters) {
        try {
            return beanClass.newInstance();
        } catch (Exception e) {
            log.error("Unable to create instance of " + beanClass.getName(), e);
        }
        return null;
    }
}
