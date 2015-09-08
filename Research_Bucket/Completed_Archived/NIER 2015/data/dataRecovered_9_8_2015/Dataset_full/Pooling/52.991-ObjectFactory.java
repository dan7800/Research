/********************************************************************************

    jMule - A java eMule port
    
    Copyright (C) 2002 myon, gos, andyl

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details ( see the LICENSE file ).

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	
********************************************************************************/

package org.jMule;

import java.nio.*;
import java.util.*;
import java.util.logging.Logger;

import org.jMule.packet.*;

/**
* Implements object pooling for any object.
* TODO: We should immplement a ObjectFactoryShrinker becauze the current implementation can hog to much memory: It always holds the maximum of object used.
* This can happen at the application start, at which point we dont throttle the connections and needs lots of the HelloPackets for the ClientVerifier.
* As the Situation stabilized ( we have establish ehuff Connections to saturate the speedDownLine ), we dont need so much HelloPackets. This can change again if we lost *our* internet connection
* ( note: *not* the server connection, but our phisical internet link ( maybe we are on a 24h DSL line, and have been forced to reconnect ) ).
* TODO: The ObjectFactory should have a public method: invalidateObjects( String name ): 
*
* NOTE: Actually this is concedired to be NOT A GOOD DESIGN !
* we should get rid of the whole crap all together.
* The rigth way(tm) to do this sort of thing is
*
* <li>
<item>define a Reusable interface</item>
<item>each object shoud extend the ReusableObject internally</item>
<item>the objects shoud manage the reuse stacks at its own ( actually the implementation shoud be in the ReusableObject )</item>
</li>
**/

public class ObjectFactory {
  
  HashMap stackMap;
  
  static ObjectFactory me = null;
  
  public static ObjectFactory getInstance() {
    if( me == null ) me = new ObjectFactory( );
      
    return me;
  };
  
  public void registerObject( Class clazz ) {
    assert ! stackMap.containsKey( clazz.getName() );
    
    System.out.println( "registerObject "  + clazz.getName() );
    stackMap.put( clazz.getName(), new Stack() );
  };
    
  private ObjectFactory() {
    // we dont need a synchronized HashMap becauze we dont modify the Factory in the threads ( we usually register all wannted object once, and dont change it during the app life time ).
    stackMap = new HashMap(); 
  };
  
  private Stack lookupObjectStack( String name )  {
    // assert stackMap.get( name ) != null;
    System.out.println( "looking " + name + " stack" );
    Stack stk = (Stack)stackMap.get( name );
    return stk;
  };
  
  // NOTE: fixed: becaouz of this, we should use the synchronizes Stack implementations: The Stacks are synchronized already becouze it is a Vector-based class.
  // API 1.4 JavaDoc/Vector  "Unlike the new collection implementations, Vector is synchronized"
  // invalidates all objects in this stack, it will be used for example for the HelloPacket, if the currentServerConnection() changes ( and so the old server ip and port values are not more valid ).
  public void invalidateObjects( String name ) {
    // assert stackMap.get( name ) != null;
    lookupObjectStack( name ).clear();
  };
  
  public void disposeObject( Object obj) {
    // assert lookupObjectStack( obj.getClass().getName() ) != null;
    System.out.println( "disposingObject " + obj.getClass().getName() );
    Stack stack = lookupObjectStack( obj.getClass().getName() );
    stack.push( obj );
  };
  
  private static  final String initObjStr = "initializing new obj ";
  private static  final String reuseObjStr = "reusing obj";
  
  /**
   * creates a packet. the packet have to be registred first ( see registerPacket ).
  **/
  public Object createObject( String name ) {
    Object p=null;
    try {
      System.out.println( "trying to get an " + name+ " object" );
      Stack stack = lookupObjectStack( name );
      p = stack.pop();
      System.out.println( reuseObjStr );
    } catch( EmptyStackException eserr ) {
      try {
        // FIXME: hym and whats about not fixed size packets, how to initialize ?
        p = Class.forName( name ).newInstance();
        System.out.println( initObjStr + name );
      } catch( ClassNotFoundException cnf_err ) {
        cnf_err.printStackTrace();    
      } catch( InstantiationException ie_err ) {
        ie_err.printStackTrace();
      } catch( IllegalAccessException ia_err ) {
        ia_err.printStackTrace();
      };
    } catch(Exception err ) {
      err.printStackTrace();
    };
    
    return p;
  };
  
  
  // for test purposes only 
  public static final void main( String[] args ) {
    int tc = 2;
    long maxTime = Long.MAX_VALUE;
    
    if( args.length > 0 ) tc = Integer.parseInt( args[0] );
    if( args.length > 1 ) maxTime = Long.parseLong( args[1] );  
    ObjectFactory pf = ObjectFactory.getInstance();
    Class[] clazzs = { GetSourcesPacket.class, /*HelloPacket.class */ };
    for( int c = 0; c < clazzs.length; c++ ) {
      pf.registerObject( clazzs[ c ] );
    };
    for( int c = 0; c < tc; c++ ) {
      new T1( pf, "T" + c, maxTime, GetSourcesPacket.class ).start();
    };

  }
}

class T1 extends Thread {
  ObjectFactory pf;
  long workingTime = 0;
  Class clazz;
  long runCount;
  Random rand;
  
  public T1( ObjectFactory pf, String name, long rc, Class clazz ) {
    super( name );
    this.pf = pf;
    runCount = rc;
    rand = new Random();
    this.clazz = clazz;
  }
  
  static long minMemory = Long.MAX_VALUE;
  static long maxMemory = 0;
  
  public void run() {
  
    while( runCount-- > 0 ) {
      long start = System.currentTimeMillis();
      long stop;
      Object p = (GetSourcesPacket)pf.createObject( clazz.getName() );
      stop = System.currentTimeMillis();
      workingTime +=  stop - start;
      try {
        if( runCount % 1000 == 0 ) { 
          long mem = Runtime.getRuntime().freeMemory();
          System.out.println( getName() + ": currentWorkingTime=" + workingTime + " freeMemory=" +  mem + " min/max: " + minMemory + "/" + maxMemory);
          if( minMemory > mem ) minMemory = mem;
          if( mem > maxMemory ) maxMemory = mem;
        };
        wait( rand.nextInt( Integer.MAX_VALUE ) ); // simulate working // random
      } catch( Exception err ) {};
      start = System.currentTimeMillis();
      pf.disposeObject( (GetSourcesPacket)p );
      stop= System.currentTimeMillis();
      workingTime +=  stop - start;
    };
  };
};

