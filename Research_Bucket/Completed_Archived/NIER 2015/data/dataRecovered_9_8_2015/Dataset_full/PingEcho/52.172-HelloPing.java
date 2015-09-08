package net.tinyos.hello;

import net.tinyos.message.*;
import net.tinyos.util.*;

import java.io.*; 

public class HelloPing implements MessageListener, Runnable {

  MoteIF moteIF;

  HelloPing() {

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

    HelloReqMsg hrm = new HelloReqMsg();
    hrm.set_reqAddr(MoteIF.TOS_BCAST_ADDR);
    hrm.set_reqId((short)0xfffe);

    System.out.println("Addr\tGroup\tProgram \tUserHash\tUnixTime\tHardwareID");
    send(hrm);

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
    new HelloPing();
  }

}
