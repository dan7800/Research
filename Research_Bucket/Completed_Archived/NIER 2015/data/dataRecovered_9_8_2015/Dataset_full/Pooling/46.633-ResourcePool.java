package com.vlee.beans.util;

import java.util.Iterator;
import java.util.Vector;

public abstract class ResourcePool implements Runnable {
   protected final Vector availableResources = new Vector(),
                               inUseResources = new Vector();
   private final int maxResources;
   private final boolean waitIfMaxedOut;
   private ResourceException error = null; // set by run()

   // Extensions must implement these three methods:
   public abstract Object  createResource () 
                           throws ResourceException;
   public abstract boolean isResourceValid(Object resource);
   public abstract void    closeResource  (Object resource);

   public ResourcePool() {
      this(10,     // by default, a max of 10 resources in pool
           false); // don't wait for resource if maxed out
   }
   public ResourcePool(int max, boolean waitIfMaxedOut) {
      this.maxResources = max;
      this.waitIfMaxedOut = waitIfMaxedOut;
   }
   public Object getResource() throws ResourceException {
      return getResource(0);
   }
   public synchronized Object getResource(long timeout) 
                                        throws ResourceException {
      Object resource = getFirstAvailableResource();

      if(resource == null) { // no available resources
         if(countResources() < maxResources) {
            waitForAvailableResource();
            return getResource();
         }
         else { // maximum resource limit reached
            if(waitIfMaxedOut) { 
               try {
                  wait(timeout);
               }
               catch(InterruptedException ex) {}
               return getResource();
            }
            throw new ResourceException("Maximum number " +
                  "of resources reached. Try again later.");
         }
      }
      inUseResources.addElement(resource);
      return resource;
   }
   public synchronized void recycleResource(Object resource) {
      inUseResources.removeElement(resource);
      availableResources.addElement(resource);
      notifyAll(); // notify waiting threads of available con
   }
   public void shutdown() {
      closeResources(availableResources);
      closeResources(inUseResources);

      availableResources.clear();
      inUseResources.clear();
   }
   public synchronized void run() { // can't throw an exception!
      Object resource;
      error = null;
      try {
         resource = createResource(); // subclasses create
      }
      catch(ResourceException ex) {
         error = ex;  // store the exception
         notifyAll(); // waiting thread will throw an exception
         return;
      }
      availableResources.addElement(resource);
      notifyAll(); // notify waiting threads
   }
   private Object getFirstAvailableResource() {
      Object resource = null;

      if(availableResources.size() > 0) {
         resource = availableResources.firstElement();
         availableResources.removeElementAt(0);
      }
      if(resource != null && !isResourceValid(resource))
         resource = getFirstAvailableResource(); // try again

      return resource;
   }
   private void waitForAvailableResource() 
                                    throws ResourceException {
      Thread thread = new Thread(this); 
      thread.start(); // thread creates a resource: see run()

      try {
         wait(); // wait for new resource to be created
      }          // or for a resource to be recycled
      catch(InterruptedException ex) { }
               
      if(error != null) // exception caught in run()
         throw error;   // rethrow exception caught in run()
   }
   private void closeResources(Vector resources) {
      Iterator it = resources.iterator();
      while(it.hasNext())
         closeResource(it.next());
   }
   private int countResources() {
      return availableResources.size()+inUseResources.size();
   }
}
