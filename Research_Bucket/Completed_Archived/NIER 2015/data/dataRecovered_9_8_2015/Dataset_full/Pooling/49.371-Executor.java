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


package net.larsan.urd.util;

/**
* The executor is a simple interface with one method, the execute method. This
* interface might be use to transparently hide thread creation from modules.
*
* @author Lars J. Nilsson
* @version ##URD-VERSION##
*/

public interface Executor {

    /**
    * Execute a runnable target. The target might be executed on a new
    * thread or queued in a pool.
    *
    * @param name Task name
    * @param target Runnable to execute, must not be null
    */
    
    public void execute(String name, Runnable target);
}