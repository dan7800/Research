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

/**
 * The default contructor of this class creates a thread pool using 10
 * worker threads and an unbounded default pipe.
 * 
 * <p>To provide extended thread controll, or change the number of default
 * threads in the pool: override the method {@link #createController()}.
 * 
 * <p>The thread pool consists of three different parts: the {@link ExecutionPipe 
 * execution pipe}, the {@link ThreadController} and the main thread pool. The pipe controls
 * task queueing and schedueling, and the controller thread creation and execution.
 * 
 * <p>A thread pool can be initiated and destroyed multiple times.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public class ThreadPool {

    protected ThreadController contr;    
    protected ExecutionPipe pipe;
    protected PoolPipe pipeWrap;
    protected Object lock;    
    
    
    /**
     * Create a thread pool with a non-default execution pipe. This pipe
     * will be wrapped in an object from the {@link #createRealPipe(ExecutionPipe)}
     * method before it is returned to the users of this pool.
     * 
     * @param pipe Execution pipe instance to use for the pool.
     */

	public ThreadPool(ExecutionPipe pipe) {
        pipeWrap = createRealPipe(pipe);
        this.lock = new Object();
        this.pipe = pipe;
	}
    
    
    /**
     * Create a thread pool with a default execution pipe. The pipe
     * created by this constructor has no upper limit and disposes of
     * waiting tasks on shutdown.
     */
    
    public ThreadPool() {
        this(new DefaultPipe());   
    }


    /// --- PUBLIC METHODS --- ///


    /**
     * Initiate the thread pool. This method will throw an 
     * illegal state exception if {@link #isOpen()} returns true. This
     * method will create the controller to use and initiate the execution
     * pipe.
     */

    public void init() { 
        synchronized(lock) {
            if(contr != null) throw new IllegalStateException("already initialized");
            this.pipe.init();
            this.contr = createController();
            this.contr.init(pipe);        
        }
    }
    
    
    /**
     * Get the incoming pipe for this thread pool.
     * 
     * @return The public pipe used for queueing tasks in this thread pool
     */

    public PoolPipe getPipe() {
        return pipeWrap;   
    }
    
    
    /**
     * Check if the pool is open for incoming tasks or not. This method
     * should be used if the pool is closed and re-opened since {@link #init()}
     * and {@link #destroy()} throws illegal state exceptions if the pool is
     * in thre wrong state.
     * 
     * @return true if the pool is open
     */
    
    public boolean isOpen() {
        synchronized(lock) {
            return (contr != null);
        }      
    }
    
    
    /**
     * Destroy this pool. This will throw an illegal state exception if the
     * pool is not {@link #isOpen() open}. It will close the current pipe
     * and destroy the controller. 
     */

    public void destroy() { 
        synchronized(lock) {
            if(contr == null) throw new IllegalStateException("not initialized");
            try { pipe.destroy(); } catch(InterruptedException e) { }
            contr.destroy();
            contr = null;
        }        
    }


    /// --- PROTECTED METHODS --- ///
    
    /**
     * Create the thread controller to use. This method is only ever called
     * during {@link #init() initation} and should return the controller to
     * use for the pool. The default controller uses tem eagerly instantiated 
     * worker threads.
     * 
     * @return The {@link ThreadController} instalnce to use
     */
    
    protected ThreadController createController() {
        return new MyController();
    }
    
    
    /**
     * Create a wrapper for the current execution pipe. This wrapper will be
     * cached and should only expose the methods in the {@link PoolPipe interface}. 
     * However, for specialized subclasses of the thread pool more methods of
     * it's pipe might be exposed.
     * 
     * @param pipe Execution pipe uised by the pool
     * @return The public pipe to use for task queueing
     */
    
    protected PoolPipe createRealPipe(final ExecutionPipe pipe) {
        if(pipe instanceof NamedPipe) {
            return new NamedPipe() {
                public void put(Runnable run) throws PipeClosedException, InterruptedException {
                    pipe.put(run);   
                } 
                public void put(String name, Runnable run) throws PipeClosedException, InterruptedException {
                    ((NamedPipe)pipe).put(name, run);   
                }   
            };
        } else {
            return new PoolPipe() {
                public void put(Runnable run) throws PipeClosedException, InterruptedException {
                    pipe.put(run);   
                }  
            };  
        } 
    }
    
    
    /// --- INNER CLASSES --- ///
    
    
    /** Simple controller class. */
    
    private static class MyController implements ThreadController {
        
        private Object lock;
        private boolean isOpen;
        private ExecutionQueue queue;
        
        private MyController() {
            this.lock = new Object();
            isOpen = false;   
        }
        
        public void init(ExecutionQueue queue) {
            this.isOpen = true;
            this.queue = queue;   
            for(int i = 0; i < 10; i++) {
				new MyThread("ThreadPool queue thread # " + i);
			}
        }
    
        public void destroy() {
            synchronized(lock) {
                isOpen = false;
                queue = null;   
            }   
        }
    
        private class MyThread implements Runnable {
         
            private MyThread(String name) {
                new Thread(this, name).start();   
            }
            
            public void run() { 
                while(true) {
                    try {
                        Runnable run = getNew();
                        if(run != null) run.run();
                        else break;
                    } catch(InterruptedException e) {
                        // DO SOMETHING ?!
                    } catch(PipeClosedException e) {
                        break;
                    } catch(Throwable e) {
                        // DO SOMETHING !
                        e.printStackTrace();
                    }  
                }
            }
            
            private Runnable getNew() throws InterruptedException {
                synchronized(lock) {
                    if(MyController.this.isOpen) return MyController.this.queue.poll();
                    else return null;
                }
            }
        }
    }
}
