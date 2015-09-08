/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxconn;
 
import vu.globe.rts.comm.idl.mux;     // mux.idl
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.thread.EventScheduler;

import java.net.ProtocolException;
import java.util.HashMap;

/**
 * A table of connection ports. Maps connection port number to a connection
 * port.
 * <p>
 * Connection ports must remain in this table for as long as a client or a
   remote multiplexer has a 'reference' to (a port number of) the connection
   port, which has not been closed. Therefore, connection ports are removed:
   <ul>
   <le>when the client closes the connection port and the remote multiplexer has
       previously closed the connection port (ConnectionPort.eventUserCloses()).
   <le>when the remote multiplexer closes the connection port and the client
       has previously closed the connection port (ConnectionPort.
       eventRemoteCloses()).
   <le>
   </ul>
   There are two special circumstances:
   <ul>
   <le>When the base connection on which this connection port is dependent
       closes (e.g. due to an error), this is treated as though the remote
       multiplexer closes the connection port (ConnectionPort.
       eventBaseCloses()).
   <le>When the client has paused the connection port, and subsequently closes
       it, removal is delayed until the client unpauses. (Otherwise the client
       will have no way of unpausing the port.) See XXXX
   </ul>
   In addition, connection ports are removed to undo changes on error recovery
   (e.g. MuxConnectionOriented.connectOnPort()). This includes receiving an
   'open port refused' message while establishing a light-weight connection.
 * <p>
 * Thread safe.
 */

class ConnectionPortTable
{
  /**
   * A thread-unsafe table with the connection ports currently in use. Maps a
   * port number (Integer) to a ConnectionPort.
   */
  private final HashMap _connection_ports = new HashMap();

  /**
   * The next free port number to pick. Note that negative numbers are allowed,
   * but mux.NoPort is not.
   */
  private int _next_cport_number = Integer.MIN_VALUE;

  /**
   * Used to construct ConnectionPorts with.
   * See CallbackPort(cb_class,EventScheduler,boolean).
   */
  private final EventScheduler _sched;

  public ConnectionPortTable (EventScheduler sched)
  {
    _sched = sched;
  }

  /**
   * Looks up an existing connection port.
   *
   * @param local_cport_number	the port number
   * @return                    the connection port, or null if not found
   */
  public synchronized ConnectionPort lookup (int local_cport_number)
  {
    return (ConnectionPort) _connection_ports.get (
                                new Integer (local_cport_number));
  }

  /**
   * Creates a new connection port, and allocates an available port number.
   * This method is used when a light-weight connection is initiated locally.
   *
   * @param bconn	the base connection carrying this port's communication
   * @return            the connection port created
   *
   * @see ConnectionPort()
   */
  public ConnectionPort create (BaseConnection bconn)
  {
    return createImpl (bconn, mux.NoPort);
  }

  /**
   * As create(BaseConnection) but supplies a remote port number as well. This
   * method is used when a light-weight connection is initiated by the remote
   * party.
   *
   * @param remote_cport_number		the remote port number
   * @exception ProtocolException	if remote_cport_number was invalid
   *
   * @see ConnectionPort()
   */
  public ConnectionPort create (BaseConnection bconn, int remote_cport_number)
    throws ProtocolException
  {
    if (remote_cport_number == mux.NoPort) {
      throw new ProtocolException (
        "received 'open port' message with invalid remote port " +
        remote_cport_number);
    }
    return createImpl (bconn, remote_cport_number);
  }

  /**
   * Implementation of the create() methods.
   *
   * @param remote_cport_number	mux.NoPort for a locally iniated connection 
   */
  private synchronized ConnectionPort createImpl (BaseConnection bconn,
                int remote_cport_number)
  {
    // allocate a port number. Make sure that mux.NoPort is skipped.
    int local_cport_number;
    do {
      local_cport_number = _next_cport_number++;
    } while (local_cport_number == mux.NoPort);

    Integer pnum = new Integer (local_cport_number);
    ConnectionPort cport = new ConnectionPort (local_cport_number,
      remote_cport_number, bconn, this, _sched);
    ConnectionPort old_cport =
      (ConnectionPort) _connection_ports.put (pnum, cport);
    DebugOutput.dassert (old_cport == null);
    return cport;
  }

  /**
   * Removes a connection port from the table, and deallocates the port number.
   *
   * @return    the removed connection port, or null if not found
   */
  public synchronized ConnectionPort remove (int local_cport_number)
  {

    ConnectionPort cport = (ConnectionPort) _connection_ports.remove (
                                new Integer (local_cport_number));
    return cport;
  }
}

