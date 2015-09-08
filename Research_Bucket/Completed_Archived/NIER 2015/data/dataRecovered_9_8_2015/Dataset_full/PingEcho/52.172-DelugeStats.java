
import java.text.*;
import java.util.*;

import net.tinyos.deluge.*;
import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;

public class DelugeStats implements MessageListener {

  MoteIF moteif;

  Timer displayTimer;
  TimerTask displayTask;

  Hashtable motes = new Hashtable();
  Vector moteIDs = new Vector();
  int curImage = 0;
  int numImages = 7;
  boolean passive = false;
  boolean incomplete = false;
  boolean running = false;

  public DelugeStats( String[] args ) {

    try {
      moteif = new MoteIF( (Messenger)null );
      moteif.registerListener( new DelugeAdvMsg(), this );
      moteif.registerListener( new NetProgMsg(), this );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    for ( int i = 0; i < args.length; i++ ) {
      if ( args[i].equals("passive") )
	passive = true;
      else if ( args[i].equals("incomplete") )
	incomplete = true;
      else if ( args[i].equals("running") )
	running = true;
    }

    moteif.start();
    
    displayTimer = new Timer();
    displayTask = new DisplayTask();
    displayTimer.schedule( displayTask, 0, 250 );

  }

  public void messageReceived( int to, Message m ) {

    switch ( m.amType() ) {

    case DelugeAdvMsg.AM_TYPE:

      if ( running )
        return;

      DelugeAdvMsg advMsg = (DelugeAdvMsg)m;

      //System.out.print( advMsg );

      if ( advMsg.get_sourceAddr() == MoteIF.TOS_BCAST_ADDR )
	return;

      if ( advMsg.get_numImages() > numImages )
	numImages = advMsg.get_numImages();

      if ( advMsg.get_imgDesc_imgNum() >= numImages )
	return;

      Integer key = new Integer( advMsg.get_sourceAddr() );
      if ( !motes.containsKey( key ) ) {
	DelugeAdvMsg msgs[] = new DelugeAdvMsg[ numImages ];
	motes.put( key, msgs );
	moteIDs.add( key );
      }
      
      DelugeAdvMsg msgs[] = (DelugeAdvMsg[])motes.get( key );
      msgs[ advMsg.get_imgDesc_imgNum() ] = advMsg;

      break;

    case NetProgMsg.AM_TYPE:

      if ( !running )
        return;

      NetProgMsg netProgMsg = (NetProgMsg)m;
      if ( netProgMsg.get_sourceAddr() == MoteIF.TOS_BCAST_ADDR )
	return;

      Integer key2 = new Integer( netProgMsg.get_sourceAddr() );
      if ( !motes.containsKey( key2 ) ) {
	motes.put( key2, netProgMsg );
	moteIDs.add( key2 );
      }
      
      break;

    }

  }

  private void ping() {
    try {
      if ( !running ) {
	DelugeAdvMsg advMsg = new DelugeAdvMsg();
	advMsg.set_sourceAddr( MoteIF.TOS_BCAST_ADDR /*0x7e*/ );
	advMsg.set_version( DelugeConsts.DELUGE_VERSION );
	advMsg.set_type( (short)DelugeConsts.DELUGE_ADV_PING );
	advMsg.set_nodeDesc_vNum( (short)DelugeConsts.DELUGE_INVALID_VNUM );
	advMsg.set_imgDesc_vNum( (short)DelugeConsts.DELUGE_INVALID_VNUM );
	advMsg.set_imgDesc_imgNum( (short)curImage );
	curImage = ( curImage + 1 ) % numImages;
	moteif.send( MoteIF.TOS_BCAST_ADDR, advMsg );
      }
      else {
	NetProgMsg netProgMsg = new NetProgMsg();
	netProgMsg.set_sourceAddr( MoteIF.TOS_BCAST_ADDR );
	moteif.send( MoteIF.TOS_BCAST_ADDR, netProgMsg );
      }
      //System.out.print( advMsg );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public static void main( String[] args ) {
    DelugeStats delugeStats = new DelugeStats( args );
  }

  class DisplayTask extends TimerTask {

    DecimalFormat format = new DecimalFormat( "00" );

    public void run() {

      Object tmpMotes[] = moteIDs.toArray();
      Arrays.sort( tmpMotes );

      System.out.println("--------------------------------------------------");
      System.out.println("Num nodes: " + tmpMotes.length );
      System.out.print("ID\t");

      if ( !running ) {
	  for ( int j = 0; j < numImages; j++ )
	    System.out.print( j + "\t\t" );
	  System.out.println();

      }
      else {
	  System.out.println( "Unix Time\tUser Hash" );
      }

      for ( int i = 0; i < tmpMotes.length; i++ ) {

	if ( !running ) {

	  DelugeAdvMsg msgs[] = (DelugeAdvMsg[])motes.get( (Integer)tmpMotes[i] );
	  System.out.print( tmpMotes[i] + "\t" );
	  for ( int j = 0; j < msgs.length; j++ ) {
	    if ( msgs[j] == null ) {
	      System.out.print("\t\t");
	      continue;
	    }
	    if ( incomplete && msgs[j].get_imgDesc_numPgs() == msgs[j].get_imgDesc_numPgsComplete() ) {
	      System.out.print("\t\t");
	      continue;
	    }
	    System.out.print( "(" + format.format(msgs[j].get_imgDesc_vNum()) +
			      "," + format.format(msgs[j].get_imgDesc_numPgsComplete()) +
			      "," + format.format(msgs[j].get_imgDesc_numPgs()) +
			      ")\t" );
	  }
	  System.out.println();
	  
	}
	else if ( running ) {

	  NetProgMsg msg = (NetProgMsg)motes.get( (Integer)tmpMotes[ i ] );
	  System.out.print( tmpMotes[ i ] + "\t" );
	  System.out.print( msg.get_ident_unix_time() + "\t" + msg.get_ident_user_hash() );
	}
	System.out.println();
      }
      if ( !passive )
	ping();
    }

  }

}
