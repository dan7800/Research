/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/*
 * IntegerTest.java
 *
 * Created on November 13, 2002, 2:05 PM
 */

package vu.gaia.apps.integer.progs;

import vu.gaia.rts.RTS;
import vu.gaia.svcs.objsvr.ObjectServerAddress;
import vu.gaia.apps.integer.n.IntegerDeputy;

/**
 *
 * @author  arno
 */
public class IntegerTestSetup
{
    public static void main(String[] Argv)
    throws Exception
    {
System.err.println("- Creates one Master on one objserver");
System.err.println("- Bind to it");
System.err.println("- Set and get 30 times (passivation checkpoint test");
System.err.println("- Unbind");
System.err.println("- Crash objectserver 1, restart objserver1, continue with");System.err.println("  IntegerTestRecover");

        RTS rts = RTS.getInstance();
        rts.init( Argv[0] );
        
        IntegerDeputy i = new IntegerDeputy();
    
        // non-default replication strategy, 2nd level usability,
        // primary interface to blue prints is setSubobject
        i.setReplicationProtocol( i.REPL_MASTER_SLAVE, null );

        i.setInitialReplicaPersistent( true );
        i.setPublicity( i.PUBLICITY_ALL );
        
        ObjectServerAddress osa = new ObjectServerAddress( "wilfred", 23000 );
        i.create( "/nl/vu/cs/globe/wilfred/test/i", osa );
        
        System.out.println( "bindink with master" );
        i.bind("/nl/vu/cs/globe/wilfred/test/i");
        
        System.out.println( "gettink from master (should be 481)");
        System.out.println( "value got = " + i.get_r() );

    
        for (int j=0; j < 10; j++) {
            System.out.println("Setting "+j);
            i.set_w(j);
            System.out.println("Getting ("+j+"): "+i.get_r());
            try {
                Thread.currentThread().sleep(2000);
            } catch (Exception e) {
                System.err.println("Got: "+e.getMessage());
            }
        }


        System.out.println( "settink 123 on master " );
        i.set_w(123);

        
        System.out.println( "gettink2 from master (should be 123)" );
        System.out.println( "value got = " + i.get_r() );
        i.unbind();

/*
        ObjectServerAddress osa2 = new ObjectServerAddress( "wilfred", 26000 );
        i.addReplica( osa2, false );

        System.out.println( "bindink with slave" );
        i.bind();
        
        System.out.println( "gettink from slave" );
        System.out.println( "value got = " + i.get_r() );

//        System.out.println( "UH OH Going to write to slave !!!");
//        i.set_w( 666 );


        i.unbind();
*/
    }
}
