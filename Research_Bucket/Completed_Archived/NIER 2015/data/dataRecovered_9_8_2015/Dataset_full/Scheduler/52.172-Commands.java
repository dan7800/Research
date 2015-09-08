// $Id: Commands.java,v 1.6 2004/06/10 19:26:54 mikedemmer Exp $

/*									tab:2
 *
 * "Copyright (c) 2004 and The Regents of the University 
 * of California.  All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and
 * its documentation for any purpose, without fee, and without written
 * agreement is hereby granted, provided that the above copyright
 * notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY
 * PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Authors:	Philip Levis
 * Date:        January 9, 2004
 * Desc:        
 *
 */

/**
 *
 * The set of functions exported to the Python environment.
 *
 * @author Philip Levis
 */


package net.tinyos.sim.script.reflect;

import net.tinyos.sim.SimDriver;
import net.tinyos.sim.script.ScriptInterpreter;

import java.io.*;
import java.util.*;
import java.net.*;

import net.tinyos.message.*;
import net.tinyos.sim.*;
import net.tinyos.sim.event.*;

/**
 * The Commands class is a reflection of the 'raw' simulator
 * interface. It is expetect that most functionality will be
 * accessible through one of the other reflected classes, however this
 * class is available for a lower-level interface.<p>
 *
 * The class is bound into the simcore module as the <i>comm</i>
 * global instance.
 */
public class Commands extends SimReflect {
  private SimCommands commands;
  
  public static final long DBG_BOOT =   (1 << 0);
  public static final long DBG_CLOCK =  (1 << 1);
  public static final long DBG_TASK =   (1 << 2);
  public static final long DBG_SCHED =  (1 << 3);
  public static final long DBG_SENSOR = (1 << 4);
  public static final long DBG_LED =    (1 << 5);
  public static final long DBG_CRYPTO = (1 << 6);

  public static final long DBG_ROUTE =  (1 << 7);
  public static final long DBG_AM =     (1 << 8);
  public static final long DBG_CRC =    (1 << 9);
  public static final long DBG_PACKET = (1 << 10);
  public static final long DBG_ENCODE = (1 << 11);
  public static final long DBG_RADIO =  (1 << 12);

  public static final long DBG_LOG =    (1 << 13);
  public static final long DBG_ADC =    (1 << 14);
  public static final long DBG_I2C =    (1 << 15);
  public static final long DBG_UART =   (1 << 16);
  public static final long DBG_PROG =   (1 << 17);
  public static final long DBG_SOUNDER =(1 << 18);
  public static final long DBG_TIME =   (1 << 19);

  public static final long DBG_SIM =    (1 << 21);
  public static final long DBG_QUEUE =  (1 << 22);
  public static final long DBG_SIMRADIO =(1 << 23);
  public static final long DBG_HARD =   (1 << 24);
  public static final long DBG_MEM =    (1 << 25);

  public static final long DBG_USR1 =   (1 << 27);
  public static final long DBG_USR2 =   (1 << 28);
  public static final long DBG_USR3 =   (1 << 29);
  public static final long DBG_TEMP =   (1 << 30);
  public static final long DBG_ERROR =  (1 << 31);
  public static final long DBG_NONE =    0;

  public static final long DBG_ALL = ~0;

  /**
   * Constructor for the Commands object. This should not be called
   * explicitly, rather the pre-constructed instance <i>comm</i> should
   * be used.
   */
  public Commands(ScriptInterpreter interp, SimDriver driver) {
    super(interp, driver);
    commands = driver.getSimCommands();
  }

  /**
   * Sends a radio message to the given mote.
   *
   * @param moteID	the id of the target mote
   * @param time	simulator time when to do the operation
   * @param msg		the actual message class
   */
  public void sendRadioMessage(short moteID, long time, Message msg) throws IOException {
    commands.sendRadioMessage(moteID, time, msg);
  }
		
  /**
   * Sends a UART message to the given mote.
   *
   * @param moteID	the id of the target mote
   * @param time	simulator time when to do the operation
   * @param msg		the actual message class
   */
  public void sendUARTMessage(short moteID, long time, Message msg) throws IOException {
    commands.sendUARTMessage(moteID, time, msg);
  }

  /**
   * Turn off the given mote.
   *
   * @param moteID	the id of the mote
   * @param time	simulator time when to do the operation
   */
  public void turnMoteOff(short moteID, long time) throws IOException {
    commands.turnMoteOff(moteID, time);
  }
  
  /**
   * Turn on the given mote.
   *
   * @param moteID	the id of the mote
   * @param time	simulator time when to do the operation
   */
  public void turnMoteOn(short moteID, long time) throws IOException {
    commands.turnMoteOn(moteID, time);
  }
  
  /**
   * Set the ADC value at the given mote to the specified value.
   *
   * @param moteID	the id of the mote
   * @param time	simulator time when to do the operation
   * @param port	the ADC port to set
   * @param value	the new value
   */
  public void setADCValue(short moteID, long time, byte port, short value) throws IOException {
    commands.setADCValue(moteID, time, port, value);
  }

  /**
   * Sets the simulator rate
   *
   * @param rate	the new simulator rate (relative to real time)
   */
  public void setSimRate(double rate) throws IOException {
    commands.setSimRate(rate);
  }

  /**
   * Set the ADC value at the given mote to the specified value.
   *
   * @param src		the source mote id
   * @param dest	the destination mote id
   * @param time	simulator time when to do the operation
   * @param loss	the bit error loss value
   */
  public void setLinkBitErrorProbability(short src, long time, short dest, double loss) throws IOException {
    commands.setLinkBitErrorProbability(src, time, dest, loss);
  }

  /**
   * Get a unique interrupt ID.
   */
  public int getInterruptID() {
    return driver.getSimComm().getInterruptID();
  }

  /**
   * Schedule an interrupt event.
   *
   * @param time	simulator time when to do the operation
   * @param interruptID	id code for the interrupt event
   */
  public void interruptInFuture(long time, int interruptID) throws IOException {
    commands.interruptInFuture(time, interruptID);
  }

  /**
   * Send a variable resolve command and return the result.
   *
   * @param moteID	mote identifier
   * @param name	mote frame variable name
   */
  public VariableResolveResponse resolveVariable(short moteID, String name) throws IOException {
    return commands.resolveVariable(moteID, name);
  }

  /**
   * Send a variable request command and return the result value.
   *
   * @param addr	variable address
   * @param length	variable length
   */
  public VariableRequestResponse requestVariable(long addr, short length) throws IOException {
    return commands.requestVariable(addr, length);
  }

  /**
   * Enable the given debug flag.
   *
   * @param dbg		debug flag
   */
  public void setDBG(long dbg) throws IOException {
    commands.setDBG(dbg);
  }

  /**
   * Set the mask for which events are transmitted.
   *
   * @param mask	event mask
   */
  public void setEventMask(short mask) throws IOException {
    commands.setEventMask(mask);
  }
  
  /**
   * Block execution until the given simulator time. This should be
   * used with care; specifically should not be called from within an
   * event handler.
   *
   * @param time	simulator time at which to unblock
   */
  public void waitUntil(long time) throws IOException {
    int id = getInterruptID();
    interruptInFuture(time, id);
    SimEventBus bus = driver.getEventBus();
    String notifier = new String();
    
    Plugin plugin = new WaitUntilPlugin(id, notifier, driver);

    bus.register(plugin);
    try {
      synchronized(notifier) {
	notifier.wait();
      }
    }
    catch (Exception e) {
      System.err.println(e);
    }
    bus.deregister(plugin);
  }

  /**
   * Block execution for a specified amount of time. This should be
   * used with care; specifically should not be called from within an
   * event handler.
   *
   * @param time	simulator time amount to wait for
   */
  public void waitFor(long time) throws IOException {
    waitUntil(driver.getTossimTime() + time);
  }

  private class WaitUntilPlugin extends Plugin {
    private int id;
    private String notifier;
    private SimDriver driver;
    
    public WaitUntilPlugin(int id, String notifier, SimDriver driver) {
      this.id = id;
      this.notifier = notifier;
      this.driver = driver;
    }
    
    public void handleEvent(SimEvent e) {
      if (e instanceof InterruptEvent) {
	InterruptEvent spe = (InterruptEvent)e;
	if ((int)spe.get_id() == id) {
	  synchronized(notifier) {
	    notifier.notifyAll();
	  }
	  //driver.pause();
	}
      }
    }
  }
}


