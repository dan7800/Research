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
 * The pipe is the public inetrface used to put executables
 * in the thread pool. Specialized instance of the thread pool may 
 * choose to make public extensions of this interface.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public interface PoolPipe { 

    /**
     * Put a runnable in the queue for execution. The semantics
     * of the queue is dependent on the current implementation. If the
     * pipe is closed this method throws an pipe closed exception.
     */
    
    public void put(Runnable run) throws InterruptedException, PipeClosedException;
    
}
