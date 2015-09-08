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
 * Issue: August, 1998
 * @author: Thomas E. Davis
 */
import java.util.Hashtable;
import java.util.Enumeration;

public abstract class ObjectPool {

  private long expirationTime;
  private long lastCheckOut;

  private Hashtable locked;
  private Hashtable unlocked;

  private CleanUpThread cleaner;



  /**
   * ObjectPool constructor comment.
   */
  public ObjectPool() {

    expirationTime = ( 1000 * 30 ); // 30 seconds

    locked = new Hashtable();
    unlocked = new Hashtable();

    lastCheckOut = System.currentTimeMillis();

    cleaner = new CleanUpThread( this, expirationTime );
    cleaner.start();
  }
  public synchronized void checkIn( Object o ) {
    if( o != null ) {
      locked.remove( o );
      unlocked.put( o, new Long( System.currentTimeMillis() ) );
    }
  }
  public synchronized Object checkOut() throws Exception {
    long now = System.currentTimeMillis();
    lastCheckOut = now;
    Object o;

    if( unlocked.size() > 0 ) {
      Enumeration e = unlocked.keys();

      while( e.hasMoreElements() ) {
        o = e.nextElement();

        if( validate( o ) ) {
          unlocked.remove( o );
          locked.put( o, new Long( now ) );
          return( o );
        } else {
          unlocked.remove( o );
          expire( o );
          o = null;
        }
      }
    }

    o = create();

    locked.put( o, new Long( now ) );
    return( o );
  }
  public synchronized void cleanUp() {
    Object o;

    long now = System.currentTimeMillis();

    Enumeration e = unlocked.keys();

    while( e.hasMoreElements() ) {
      o = e.nextElement();

      if( ( now - ( ( Long ) unlocked.get( o ) ).longValue() ) > expirationTime ) {
        unlocked.remove( o );
        expire( o );
        o = null;
      }
    }

    //System.gc();
  }
  public abstract Object create() throws Exception;
  public abstract void expire( Object o );
  public abstract boolean validate( Object o );
}
