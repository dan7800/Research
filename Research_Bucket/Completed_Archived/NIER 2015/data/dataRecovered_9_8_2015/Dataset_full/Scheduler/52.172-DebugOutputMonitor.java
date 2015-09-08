/**
 * Copyright (c) 2007, Institute of Parallel and Distributed Systems
 * (IPVS), Universität Stuttgart. 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 *  - Neither the names of the Institute of Parallel and Distributed
 *    Systems and Universität Stuttgart nor the names of its contributors
 *    may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package ncunit.avrora;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;

import avrora.core.Program;
import avrora.core.Register;
import avrora.core.SourceMapping;
import avrora.monitors.Monitor;
import avrora.monitors.MonitorFactory;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.platform.Platform;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;


public class DebugOutputMonitor extends MonitorFactory {

    protected Option.List DEBUG_LEVELS = options.newOptionList("debug-levels", "all", 
    	"This option enables output for given debug levels.");
    
    private enum DebugLevel {
    	  DBG_ALL		("all", 0xff),	/* umm, "verbose"		*/

    	  /*====== Core mote modes =============*/
    	    DBG_BOOT ("boot", 0),	/* the boot sequence		*/
    	    DBG_CLOCK ("clock", 1),	/* clock        		*/
    	    DBG_TASK ("task", 2),	/* task stuff			*/
    	    DBG_SCHED ("sched", 3),	/* switch, scheduling		*/
    	    DBG_SENSOR ("sensor", 4),	/* sensor readings              */
    	    DBG_LED ("led", 5),	/* LEDs         		*/
    	    DBG_CRYPTO ("crypto", 6),	/* Cryptography/security        */

    	  /*====== Networking modes ============*/
    	    DBG_ROUTE ("route", 7),	/* network routing       	*/
    	    DBG_AM ("am", 8),	/* Active Messages		*/
    	    DBG_CRC ("crc", 9),	/* packet CRC stuff		*/
    	    DBG_PACKET ("packet", 10),	/* Packet level stuff 		*/
    	    DBG_ENCODE ("encode", 11),   /* Radio encoding/decoding      */
    	    DBG_RADIO ("radio", 12),	/* radio bits                   */

    	  /*====== Misc. hardware & system =====*/
    	    DBG_LOG ("log", 13),	/* Logger component 		*/
    	    DBG_ADC ("adc", 14),	/* Analog Digital Converter	*/
    	    DBG_I2C ("i2c", 15),	/* I2C bus			*/
    	    DBG_UART ("uart", 16),	/* UART				*/
    	    DBG_PROG ("prog", 17),	/* Remote programming		*/
    	    DBG_SOUNDER ("sounder", 18),   /* SOUNDER component            */
    	    DBG_TIME ("time", 19),   /* Time and Timer components    */
    	    DBG_POWER ("power", 20),   /* Power profiling      */


    	  /*====== Simulator modes =============*/
    	    DBG_SIM ("sim", 21),   /* Simulator                    */
    	    DBG_QUEUE ("queue", 22),   /* Simulator event queue        */
    	    DBG_SIMRADIO ("simradio", 23),   /* Simulator radio model        */
    	    DBG_HARD ("hardware", 24),   /* Hardware emulation           */
    	    DBG_MEM ("mem", 25),   /* malloc/free                  */
//    	  DBG_RESERVED ("reserved", 26),   /* reserved for future use      */

    	  /*====== For application use =========*/
    	    DBG_USR1 ("usr1", 27),	/* User component 1		*/
    	    DBG_USR2 ("usr2", 28),	/* User component 2		*/
    	    DBG_USR3 ("usr3", 29),	/* User component 3		*/
    	    DBG_TEMP ("temp", 30),	/* Temorpary testing use	*/

    	    DBG_ERROR ("error", 31),	/* Error condition		*/
    	    DBG_NONE ("none", 0);		/* Nothing                      */

    	
      	private final String name;
    	private final int value;
    	
    	private DebugLevel(String name, int value) {
    		this.name = name;
    		this.value = value;
		}
    	
		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}
		
		public static DebugLevel getLevel(String name) {
			for (DebugLevel level : DebugLevel.values()) {
				if (level.getName().equals(name)) {
					return level;
				}
			}
			return null;
		}
		
		public static DebugLevel getLevel(int number) {
			for (DebugLevel level : DebugLevel.values()) {
				if (level.getValue() == number) {
					return level;
				}
			}
			return null;
		}
    	
    };
    
    private HashSet<DebugLevel> debugLevels = new HashSet<DebugLevel>();

    class DebugCodeProbe extends Simulator.Probe.Empty {

	    final Simulator simulator;
	    
	    /**
		 * 
		 */
		public DebugCodeProbe(Simulator s, String functionName) {
			super();
	        simulator = s;
            Program p = simulator.getProgram();
            SourceMapping smap = p.getSourceMapping();
			if (smap.getLocation(functionName) != null) {
				simulator.insertProbe(this, smap.getLocation(functionName).address);
			}
            else {
            	System.out.println(functionName+" not found");
            }
		}

	    /* (non-Javadoc)
		 * @see avrora.monitors.Monitor#report()
		 */
		public void report() {
		}
		
		/* (non-Javadoc)
		 * @see avrora.sim.Simulator.Probe.Empty#fireAfter(avrora.sim.State, int)
		 */
		public void fireAfter(State state, int pc) {
			StringBuffer buf = new StringBuffer(45);
	        StringUtil.getIDTimeString(buf, simulator);
	        int infoAddr = simulator.getInterpreter().getRegisterWord(Register.R24);

	        String message = "";
        	int debugLevel = getUnsignedRAMData(simulator, infoAddr);
        	DebugLevel level = DebugLevel.getLevel(debugLevel);
        	if (debugLevels.contains(level)) {
    	        int i=0;
    	        int dataByte;
    	        while ((dataByte = getUnsignedRAMData(simulator, infoAddr + 1 + i)) != 0) {
    	        	message += (char) dataByte;
    	        	i++;
    	        }
    	        Terminal.append(Terminal.COLOR_BRIGHT_CYAN, buf, message);
                synchronized ( Terminal.class) {
                    Terminal.println(buf.toString());
                }
        	}
	        
		}
	  }

    class Mon implements Monitor {
        final Simulator simulator;
        final Platform platform;
        private DebugCodeProbe probe;
        
        Mon(Simulator s) {
            simulator = s;
            platform = simulator.getMicrocontroller().getPlatform();
            Iterator iter = DEBUG_LEVELS.get().iterator();
            while (iter.hasNext()) {
				String levelName = (String) iter.next();
				DebugLevel level = DebugLevel.getLevel(levelName);
				if (level != null) {
					if (level.equals(DebugLevel.DBG_ALL)) {
						for (DebugLevel l : DebugLevel.values()) {
							debugLevels.add(l);
						}
					}
					else {
						debugLevels.add(level);
					}
				}
				else {
					synchronized (Terminal.class) {
						Terminal.println("Unknown debug level: " + levelName);
					}
				}
			}
            probe = new DebugCodeProbe(simulator, "_debugOutput");
        }
        
        public void report() {
       		probe.report();
        }

    }

    /**
     * create a new monitor
     */
    public DebugOutputMonitor() {
        super("The \"DebugOutput\" monitor offers TOSSIM-like debug outputs.");
    }


    /**
     * create a new monitor, calls the constructor
     *
     * @see avrora.monitors.MonitorFactory#newMonitor(avrora.sim.Simulator)
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Mon(s);
    }
    
    private int getUnsignedRAMData(Simulator simulator, int address) {
    	byte signedByte = simulator.getInterpreter().getDataByte(address);
    	//make it unsigned
    	ByteBuffer bb = ByteBuffer.allocate(4);
    	bb.put(new byte[] {0, 0, 0, signedByte});
    	bb.rewind();
    	int unsignedByteVal = bb.getInt();
    	return unsignedByteVal;
    }
    
}

