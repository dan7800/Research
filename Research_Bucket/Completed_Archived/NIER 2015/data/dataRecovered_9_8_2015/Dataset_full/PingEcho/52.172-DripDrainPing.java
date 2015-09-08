import net.tinyos.message.*;
import net.tinyos.util.*;
import net.tinyos.drip.*;
import net.tinyos.drain.*;

public class DripDrainPing implements MessageListener {
  Drip drip;
  DrainConnector drain;
  Drain drainTree;
  int seqno = 0;
  int identifier;

  public DripDrainPing() {
    System.out.println("Drip-Drain PING");
    drip = new Drip(DripDrainPingConsts.AM_DRIPDRAINPINGMSG);
    drain = new DrainConnector();
    drain.registerListener(DripDrainPingConsts.AM_DRIPDRAINPINGMSG,
			   this);
    drainTree = new Drain();
    drainTree.buildTree();
    identifier = (int)((double)Math.random() * (double)65535);
  }

  public void ping() {
    DripDrainPingMsg msg = new DripDrainPingMsg();

    seqno++;
    System.out.println("--- sending sequence number " + seqno + " ---");

    msg.set_identifier(identifier);
    msg.set_seqno(seqno);
    drip.send(msg, msg.dataGet().length);
  }

  public void messageReceived(int to, Message m) {
    DrainMsg drainMsg = (DrainMsg) m;
    DripDrainPingMsg msg = new DripDrainPingMsg( drainMsg,
						 drainMsg.offset_data(0),
						 drainMsg.dataLength() - 
						 drainMsg.offset_data(0) );

    if (msg.get_identifier() == identifier) {
      System.out.println("response from: " + drainMsg.get_source() + " " +
			 "seq=" + msg.get_seqno());
    }
  }

  public static void main(String args[]) {
    DripDrainPing ping = new DripDrainPing();
    while(true) {
      ping.ping();
      try{ Thread.sleep(1); } catch (InterruptedException e) { }
    }
  }
}
