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
 * A named pipe extends pooled pipe with a name for the
 * task to run. This name will be added to the thread that
 * is executing the target.
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public interface NamedPipe extends PoolPipe {

    /**
     * Put a runnable in the queue for execution. The semantics
     * of the queue is dependent on the current implementation. If the
     * pipe is closed this method throws an pipe closed exception. The name
     * fill be used by the executing thread name.
     */
    
    public void put(String name, Runnable run) throws InterruptedException, PipeClosedException;
    
}