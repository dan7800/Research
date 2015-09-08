/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxcp;
 
import vu.globe.rts.comm.idl.mux.*;    // mux.idl
import vu.globe.rts.comm.idl.mux;      // mux.idl
import vu.globe.rts.comm.idl.p2p.*;    // p2p.idl
import vu.globe.util.thread.EventScheduler;

import java.util.*;

 
/**
 * The object that manages a multiplexing listener's ports. Thread safe.
 */

/*
  For port number allocation, see the README.
*/
 
public class ListenerPortTable
{
  /**
   * A thread-unsafe table with the listener ports currently in use. Maps a
   * port number (Integer) to a ListenerPort. Does not store null keys or
   * values.
   */
  private HashMap _active_ports = new HashMap();

  /**
   * The next free port number to pick. As explained in the README, the
   * multiplexer picks port numbers from the positive numbers only.
   */
  private int _next_number = 1;

  /**
   * Used to construct ListenerPorts with.
   * See CallbackPort(cb_class,EventScheduler,boolean).
   */
  private final EventScheduler _sched;

  /**
   * Used to construct ListenerPorts with.
   * See CallbackPort(cb_class,EventScheduler,boolean).
   */
  private final boolean _sched_only;

  /** Constructor. Equivalent to ListenerPortTable(null, false). */
  public ListenerPortTable()
  {
    this (null, false);
  }

  /**
   * Constructor. The supplied values of 'sched' and 'sched_only' are used
   * to construct ListenerPorts.
   *
   * @param sched       see CallbackPort(cb_class,EventScheduler,boolean)
   * @param sched_only  see CallbackPort(cb_class,EventScheduler,boolean)
   */
  public ListenerPortTable(EventScheduler sched, boolean sched_only)
  {
    _sched = sched;
    _sched_only = sched_only;
  }

  /**
   * Looks up an existing listener port.
   *
   * @param port_number		the port number
   * @return			the listener port, or null if not found
   */
  public synchronized ListenerPort lookup (int port_number)
  {
    return (ListenerPort) _active_ports.get (new Integer (port_number));
  }

  /**
   * Looks up an existing listener port, or creates a new listener port if
   * it did not exist.
   *
   * @param port_number		the port number
   * @return			the listener port found or created
   *
   * @exception muxErrors_illegalPort
   * @exception muxErrors_noMem
   *				if a new port could not be created due to lack
   *				of space
   */
  public synchronized ListenerPort lookupOrCreate (int port_number)
  			throws muxErrors_illegalPort, muxErrors_noMem
  {
    Integer pnum = new Integer (port_number);
    ListenerPort port = (ListenerPort) _active_ports.get (pnum);
    if (port != null)
      return port;

    if (port_number == mux.NoPort)
      throw new muxErrors_illegalPort();
    port = new ListenerPort (port_number, _sched, _sched_only);
    _active_ports.put (pnum, port);

    return port;
  }

  /**
   * Creates a new listener port, and allocates an available port number.
   *
   * @return                 the listener port created
   *
   * @exception muxErrors_noMem
   *				if a new port could not be created due to lack
   *				of space
   */
  public synchronized ListenerPort create() throws muxErrors_noMem
  {
    // Try to allocate the next positive port number (_next_number). It is
    // possible that _next_number has already been occupied by a prior explicit
    // allocation. In that case try the next number, etc.
    // However, with the expected use of the multiplexer, the range of
    // explicitly allocated port numbers is negative, and clashes will not
    // occur.

    // loop until we have allocated a free port number 
    while (true) {
      int port_number = _next_number++;
      Integer pnum = new Integer (port_number);
      ListenerPort port = new ListenerPort (port_number, _sched, _sched_only);
      ListenerPort old_port = (ListenerPort) _active_ports.put (pnum, port);
      if (old_port == null)
        return port; // success

      // failure: undo
      _active_ports.put (pnum, old_port);
    }
  }

  /**
   * Removes a listener port from the table, and deallocates the port number.
   *
   * @return	the removed listener port, or null if not found
   */
  public synchronized ListenerPort remove (int port_number)
  {
    return (ListenerPort) _active_ports.remove (new Integer (port_number));
  }

  /**
   * Returns an iterator over the ListenerPorts in the table. The iterator
   * supports the remove method. The iterator
   * is not thread-safe with respect to the update operations in the table.
   * However, you can lock out all other operations by synchronising on the
   * table object.
   */
  public synchronized Iterator iterator()
  {
    return _active_ports.values().iterator();
  }
}

