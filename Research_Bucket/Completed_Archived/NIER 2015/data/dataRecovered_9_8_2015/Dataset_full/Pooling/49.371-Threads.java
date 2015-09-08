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

package net.larsan.urd.impl;

import net.larsan.norna.util.*;
import net.larsan.urd.util.Executor;
import net.larsan.urd.util.threadpool.*;

public class Threads extends ThreadPool implements Executor {

    private ClassLoader contextLoader;
    private PriorityPipe myPipe;
    
    public Threads(ClassLoader contextLoader) {
        super(new PriorityPipe());   
        this.contextLoader = contextLoader;
        myPipe = (PriorityPipe)super.pipe;
        super.init();
    }
        
	public void execute(String name, Runnable target) {
		try {
			myPipe.put(name, target);
		} catch(PipeClosedException e) {
            //shutdown error: do something !
        }   
	}
        

    /// --- PROTECTED METHODS --- ///

    protected ThreadController createController() {
        return new MyController();
    }
    
    
    /// --- PRIVATE CLASSES --- ///
    
    private class MyController implements ThreadController {
    
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
                new MyThread("Urd Pooled Thread # " + i);
            }
        }
    
        public void destroy() {
            synchronized(lock) {
                isOpen = false;
                queue = null;   
            }   
        }

        private class MyThread implements Runnable {
            
            private String name;
            private Runnable current;
         
            private MyThread(String name) {
                createThread(this, name).start();   
                this.name = name;
            }
            
            public void run() { 
                Thread.currentThread().setName(name + " (waiting)");
                while(true) {
                    try {
                        current = getNew();
                        if(current != null) {
                            beforeExecution();
                            try {
                                current.run();
                            } finally {
                                afterExecution();  
                                current = null;
                            }
                        } else break;
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
            
            protected Thread createThread(Runnable handle, String name) {
                Thread th = new Thread(handle, name);
                th.setContextClassLoader(contextLoader);
                return th;   
            }
        
            protected void beforeExecution() {
                String tmpName = myPipe.getName(current);
                if(tmpName != null) {
                    Thread.currentThread().setName(name + " (executing: " + tmpName + ")");
                }
            }
            
            protected void afterExecution() {
                Thread.currentThread().setName(name + " (waiting)");
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
