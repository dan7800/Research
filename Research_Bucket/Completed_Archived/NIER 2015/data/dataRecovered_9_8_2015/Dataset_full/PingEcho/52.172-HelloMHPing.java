package net.tinyos.hello;

import net.tinyos.message.*;
import net.tinyos.util.*;
import net.tinyos.drip.*;

import java.io.*; 
import java.util.*;

public class HelloMHPing implements MessageListener, Runnable {

  MoteIF moteIF;
  private static int PING_LOCAL = 1;
  private static int PING_TREE = 2;

  private boolean light = false;
  private boolean sound = false;
  private int ping = 0;
  private boolean repeat = false;
  private int ttl = 0xff;
  private int group = 0xff;
  private int nodeID = 0xffff;

  private void usage() {
    System.err.println("Usage: java net.tinyos.hello.HelloMHPing [OPTION]...");
    System.err.println("Command applies to all nodes by default");
    System.err.println("  -l : light, -s: sound");
    System.err.println("  -pl: local response, -pt: tree response");
    System.err.println("  -r: repeat response, -rc: cancel repeat response");
    System.err.println("  --id <node ID> : send command to a specific node");
    System.err.println("  --group <group> : send command to a specific group");
    System.err.println("  --ttl <hop count> : limit number of hops");
    System.exit(1);
  }

  private void parseArgs(String args[]) {

    ArrayList cleanedArgs = new ArrayList();

    for(int i = 0; i < args.length; i++) {
      if (args[i].startsWith("--")) {
	// Parse Long Options
	String longopt = args[i].substring(2);

	if (longopt.equals("help")) {
	  usage();
	} else if (longopt.equals("id")) {
	  nodeID = Integer.parseInt(args[i+1]);
	  i++;
	} else if (longopt.equals("group")) {
	  group = Integer.parseInt(args[i+1]);
	  i++;
	} else if (longopt.equals("ttl")) {
	  ttl = Integer.parseInt(args[i+1]);
	  i++;
	}

      } else if (args[i].startsWith("-")) {
	// Parse Short Options
	String opt = args[i].substring(1);

	if (opt.equals("h")) {
	  usage();
	} else if (opt.equals("l")) {
	  light = true;
	} else if (opt.equals("s")) {
	  sound = true;
	} else if (opt.equals("pl")) {
	  ping = PING_LOCAL;
	} else if (opt.equals("pt")) {
	  ping = PING_TREE;
	} else if (opt.equals("r")) {
	  repeat = true;
	} else if (opt.equals("rc")) {
	  repeat = false;
	}

      } else {
	// Place into args string
	cleanedArgs.add(args[i]);
      }
    }
  }

  HelloMHPing(String args[]) {

    if (args.length == 0) {
      light = true; sound = true; ping = PING_LOCAL;
    } else {
      parseArgs(args);
    }

    try {
      moteIF = new MoteIF(PrintStreamMessenger.err);
      moteIF.registerListener(new HelloMsg(), this);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    TOSBaseCmdMsg cmdMsg = new TOSBaseCmdMsg();
    cmdMsg.set_groupChanged((byte)1);
    cmdMsg.set_group((short)0xff);
    
    send(cmdMsg);

    DripMsg dripMsg = new DripMsg();

    dripMsg.set_metadata_id((short)HelloCmdMsg.AM_TYPE);
    dripMsg.set_metadata_seqno((byte)0);

    NamingMsg namingMsg = new NamingMsg(dripMsg, dripMsg.offset_data(0),
					NamingMsg.DEFAULT_MESSAGE_SIZE +
					HelloCmdMsg.DEFAULT_MESSAGE_SIZE);
    namingMsg.set_ttl((short)ttl);
    namingMsg.set_group((short)group);
    namingMsg.set_addr((short)nodeID);

    HelloCmdMsg hcMsg = 
      new HelloCmdMsg(namingMsg,
		      namingMsg.offset_data(0),
		      HelloCmdMsg.DEFAULT_MESSAGE_SIZE);
    
    if (light) 
      hcMsg.set_light((byte)1);

    if (sound)
      hcMsg.set_sound((byte)1);

    if (ping == PING_LOCAL) {
      hcMsg.set_local((byte)1);
    } else if (ping == PING_TREE) {
      hcMsg.set_tree((byte)1);
    }

    if (repeat)
      hcMsg.set_sticky((byte)1);

    send(dripMsg);

    Thread thread = new Thread(this);
    thread.setDaemon(true);
    thread.start();
  }

  public void run() {
    while(true) {
      try {
	Thread.currentThread().sleep(4096+1024);
	System.exit(0);
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  }

  public synchronized void send(Message m) {
    try {
      moteIF.send(MoteIF.TOS_BCAST_ADDR, m);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("ERROR: Can't send message");
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  synchronized public void messageReceived(int to, Message m) {
    HelloMsg helloMsg = (HelloMsg) m;

    System.out.print(""+ helloMsg.get_sourceAddr() + ":\t" + 
		     "\t" + 
		     new String(helloMsg.get_programName()) + "\t\t0x" +
		     Long.toHexString(helloMsg.get_userHash()) + "\t0x" +
		     Long.toHexString(helloMsg.get_unixTime()) + "\t");

    for (int i = 0; i < 8; i++) {
      if (helloMsg.getElement_hardwareId(i) < 0x10) {
	System.out.print("0");
      }
      System.out.print(Long.toHexString(helloMsg.getElement_hardwareId(i) & 0xff));
    }
    System.out.println();
	
  }
  
  public static void main(String args[]) {
    new HelloMHPing(args);
  }

}

