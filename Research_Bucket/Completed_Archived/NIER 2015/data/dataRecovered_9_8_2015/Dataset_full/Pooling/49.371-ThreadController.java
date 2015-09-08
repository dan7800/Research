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
 * The thread controller creates, keeps track of and destroys
 * threads fro the thread pool. 
 * 
 * @author Lars J. Nilsson
 * @version ##URD-VERSION##
 */

public interface ThreadController {
    
    
    /**
     * Initiate this controller with an execution queue
     * to use. This method is guarantied to be called only
     * once, and when {@link #destroy() destroy} is called the
     * controller will not be used again.
     * 
     * @param queue Execution queue to use
     */

    public void init(ExecutionQueue queue);
    
    
    /**
     * Destroy this controller. This is the last method call this object
     * is going to receive. After this point the controller is retired
     * and the tread pool will null it's references to it.
     */
    
    public void destroy();
    
}
