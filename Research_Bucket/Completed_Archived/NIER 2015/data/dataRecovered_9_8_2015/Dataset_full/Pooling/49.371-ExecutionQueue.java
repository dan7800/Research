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
 * This is the private part of an {@link ExecutionPipe}. It is used by the
 * thread pool internals to controll the pipe. IT will not be disclosed
 * to the thread pool users.
 * 
 * <p>An instance of this class must be possible to {@link #init() initiate}
 * and {@link #destroy() destroy} multiple times. But is is gaurantitied
 * never to be instantiated before it is destroyed and never destroyed unless
 * initiated first.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public interface ExecutionQueue { 
    
    /**
     * Initiate this queue. This method may only be called immediately
     * on queue creation and after destroy.
     */
    
    public void init();
    
    
    /**
     * Close this queue. This method is called by the
     * thread pool on destruction. The implementation is free
     * to block the calling thread until all objects in the
     * queue is executed or to return immediately.
     */
    
    public void destroy() throws InterruptedException;
    
    
    /**
     * Get the number of waiting runnables to execute. This 
     * method return 0 if the queue is empty.
     */
   
    public int size();
    
    
     /**
     * Poll for a runnable from the queue. This method will wait
     * if the queue is empty. If the queue is closed it this method
     * will throw an pipe closed exception.
     */

    public Runnable poll() throws InterruptedException, PipeClosedException;
    

    /**
     * Take a runnable from the queue is one is available. If the
     * queue is empty this method returns null and if it is closed it
     * throw a {@link PipeClosedException}.
     */

    public Runnable take() throws PipeClosedException;

}
