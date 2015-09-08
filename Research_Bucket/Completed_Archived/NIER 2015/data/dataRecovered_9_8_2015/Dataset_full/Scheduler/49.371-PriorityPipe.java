/**
* Copyright (C) 2002 Lars J. Nilsson, webmaster at larsan.net
*
*   This program is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public License
*   as published by the Free Software Foundation; either version 2.1
*   of the License, or (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU Lesser General Public License for more details.
*
*   You should have received a copy of the GNU Lesser General Public License
*   along with this program; if not, write to the Free Software
*   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
*/

package net.larsan.urd.util.threadpool;

import java.util.*;

/**
 * This is an unbouded (no upper limit) pipe which is sorted by thread
 * priority. I.e: tasks queued by high priority threads willl be executed 
 * before tasks queued by threads with lower priority. It ignores queued 
 * runnabled on closedown and do not  throw any interrupted exceptions.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public class PriorityPipe implements ExecutionPipe, NamedPipe {

    private Object lock;
    private MySet set;
    
    public PriorityPipe() {
        lock = new Object();   
    }

    
    /// --- PIPE --- ///
    
    public void put(String name, Runnable run) throws PipeClosedException { 
        synchronized(lock) {
            if(set == null) throw new PipeClosedException("execution pipe closed");   
            set.put(name, run);
            lock.notify();
        }
    }
    
    public void put(Runnable run) throws PipeClosedException {
        put(null, run);   
    }
    
    
    /// --- EXECUTION QUEUE --- ///
    
    public void init() {
        synchronized(lock) {
            if(set != null) throw new IllegalStateException("already initialized");
            set = new MySet();
        }
    }
    
    public void destroy()  { 
        synchronized(lock) {
            if(set == null) throw new IllegalStateException("not initialized");
            set.clear();
            set = null;
            lock.notify();   
        }
    }
    
    public Runnable poll() throws PipeClosedException { 
        synchronized(lock) {
            while(true) {
                if(set == null) throw new PipeClosedException("execution pipe closed");
                if(set.size() > 0) return set.get();
                else {
                    try {
                        lock.wait();
                    } catch(InterruptedException e) { }
                }
            }   
        }
    }
    
    
    /**
     * Get the name associated with this runnable. Returns null
     * if no name is available.
     */
    
    public String getName(Runnable run) {
        return set.getName(run);   
    }
    
    public Runnable take() throws PipeClosedException { 
        synchronized(lock) {
            if(set == null) throw new PipeClosedException("execution pipe closed");
            else if(set.size() == 0) return null;
            else return set.get();
        }
    }
    
    public int size() { 
        synchronized(lock) {
            return (set == null ? 0 : set.size());
        }
    }
    
    
    /// --- INNER CLASSES --- ///
    
    private static class MySet {
        
        int count;
        private Set set;
        private Map names;
        
        private MySet() {
            set = new TreeSet();   
            names = new WeakHashMap();
            count = 0;
        }   
        
        private void clear() { set.clear(); }
        
        private int size() { return set.size(); }
        
        private void put(String name, Runnable run) {
            if(size() == 0) count = 0;
            set.add(new Wrap(run, count++));
            if(name != null) names.put(run, name);
        }
        
        private String getName(Runnable run) {
            String str = (String)names.remove(run);
            return str;
        }
        
        private Runnable get() {
            Iterator i = set.iterator();
            if(!i.hasNext()) return null;
            else {
                Wrap wrap = (Wrap)i.next();
                i.remove();
                return wrap.run;
            }  
        }
    }

    private static class Wrap implements Comparable {
        
        Runnable run;
        int priority, num;
        
        private Wrap(Runnable run, int num) {
            priority = Thread.currentThread().getPriority();
            this.run = run;   
            this.num = num;
        }
        
        public int compareTo(Object o) {
            Wrap wr = (Wrap)o;
            int checkPriority = wr.priority - this.priority;
            int checkNumber = this.num - wr.num;
            if(checkPriority != 0) return checkPriority;
            else return checkNumber;
        }
    }
}
