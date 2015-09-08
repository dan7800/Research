/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gids.rsd.util;

/*
The JavaLDAP Server
Copyright (C) 2000  Clayton Donley (donley@linc-dev.com) - All Rights Reserved

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

/**
 * ObjectPool cleanup thread based on code from Javaworld
 * Issue: August, 1988
 * @author: Thomas E. Davis
 */
class CleanUpThread extends Thread {

  private ObjectPool pool;
  private long sleepTime;

  CleanUpThread( ObjectPool pool, long sleepTime ) {
    this.pool = pool;
    this.sleepTime = sleepTime;
  }
  public void run() {
    while( true ) {
      try {
        sleep( sleepTime );
      } catch( InterruptedException e ) {
        // ignore it
      }

      pool.cleanUp();
    }
  }
}
