/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.tcp;

import vu.globe.rts.java.*;
import vu.globe.rts.std.StdUtil;
import vu.globe.idlsys.g;
import vu.globe.rts.std.idl.configure.*;
import vu.globe.rts.comm.idl.p2p.*;
import vu.globe.rts.comm.skels.*;
import vu.globe.util.comm.ProtAddress;
import vu.globe.rts.comm.gwrap.P2PDefs;
import vu.globe.rts.std.idl.stdInf.*;

import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.debug.DebugOutput;

import java.io.*;
import java.net.*;

import vu.globe.rts.runtime.ns.idl.*;            // ns.idl
import vu.globe.rts.runtime.cfg.idl.rtconfig.*;  // rtconfig.idl

import javax.net.*;

/**
   The object that implements the listener interface.
*/

/*
   TcpListener state
   -----------------

      a Java server socket
      a callback thread
      a buffer for incoming connections
   
   The Java socket is read exclusively by the callback thread. The callback
   thread places incoming connections and exceptions in a connection buffer.
   The user's invocations to accept() access the connection buffer. The
   callback thread may block if the buffer is full.

   The callback thread and connection buffer are implemented as an Acceptor
   object. This object also provides methods that effect a proper clean-up of
   the callback thread, and implement pauseListenCB().

   Closing
   -------

   To close this listener object, a final listenStopped() callback must be
   made, and the following resources must be released:
   -  the socket must be closed
   -  the callback thread must be cleaned up
   -  the callback interface must be released
   This is implemented inside the Acceptor object as follows.

   The callback thread makes the final listenStopped() callback, releases the
   callback interface, and terminates itself. It does so after being alerted.
   The callback thread is alerted by:
   -  setting a flag (which is read by the thread)
   -  unblocking the thread, and preventing it from blocking again

   The callback thread blocks in two places: while reading the socket and while
   waiting to place an element in the connetion buffer. The callback thread's
   read operation is unblocked by interrupting the thread and closing the
   socket.  The thread's wait() for space in the connetion buffer is unblocked
   using notify().

   Note: the above ignores paused callbacks (see below). Since pausing must not
   only suspend regular callbacks but also the final listenStopped() callback,
   pausing causes all cleanup operations described above to be delayed.

   Pausing
   -------

   The semantics of the IDL pauseListenCB() method are that an invocation of
   pause (true) will suspend callbacks until pause(false) is invoked. In
   addition, pauseListenCB(true) will wait for a currently active callback
   invocation to finish before returning. This is implemented inside the
   Acceptor object as follows.

   pauseListenCB() sets or clears a flag to indicate whether callbacks are to be
   suspended. The flag is read by the callback thread, just before executing a
   callback (whether a regular callback or the final listenStopped() callback).
   If the thread finds the flag set, it will wait until the flag is cleared.

   Another flag is set by the callback thread just before executing a callback
   and cleared just after executing a callback. pauseListenCB(true) reads this
   flag to determine whether a callback invocation is currently active. If so,
   pauseListenCB (true) will wait for the flag to be cleared.

   Also see the note at the end of the section on 'Closing'.

   Thread Safety
   -------------

   Except where noted, this code is thread-safe. Note that Java Sockets are
   assumed to be thread-safe.

   Thread safety has been ignored in some cases where it doesn't matter:
   -  No invocations should follow closeContact(), so invocations concurrent
      with closeContact() are also illegal.

   Globe Objects
   -------------

   A listener object is responsible for creating new connection objects
   and placing them somewhere in the local name space. The objects are placed
   in subcontexts of the listener object context. Unique names are generated
   for them with a standard prefix.
*/

public class TcpListener extends cfgListenerSkel
{
   /** The contact point of this object. Null, if not set.  */
   private String contact_point;

   /** The object that accepts connections and runs the callback thread. */
   private Acceptor acceptor = new Acceptor();

   private CtxNameGenerator ctx_namer = new CtxNameGenerator ();

   private static final int BACK_LOG = 50;

   /** The communication interfaces implemented by this object. */
   private static interfaces COMM_INFS;

   static {
     COMM_INFS = new interfaces (3);

     COMM_INFS.v[0] = comm.infid;
     COMM_INFS.v[1] = listener.infid;
     COMM_INFS.v[2] = contactExporter.infid;
   }


   protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
   {
     super.cleanup();
   }

   // configurable interface

   public void configure (String config_data) throws configureErrors
   {
   }

   // comm interface methods

   public String getProtocolID()
   {
        return ProtAddress.TCP_PROT;
   }

   public String getProtocolStack()
   {
        return TcpAddress.PROT_STACK;
   }


   public String getContact()
   {
      return contact_point; // possibly null
   }

   public interfaces getCommInfs()
   {
     return COMM_INFS;
   }


   // contactExporter interface methods
   
   public void exportContact (String contact) throws Exception
   {
      ServerSocket server_sock;

      try
      {
         if (contact == null) 
	 {
            // any address, any port
            // ServerSocket(int, int) picks '0.0.0.0', which is not good enough
            InetAddress local = InetAddress.getLocalHost();

            DebugOutput.println(DebugOutput.DBG_NORMAL, "tcplist: creating "
                                + "ordinary server socket (local)");
            server_sock = new ServerSocket (0, BACK_LOG, local);
         }
         else 
	 {
            // parse the contact point; may throw a commErrors_illegalAddress
            TcpAddress tcp_address = new TcpAddress (contact);

            String hostname = tcp_address.getIpAddress();
            InetAddress host = InetAddress.getByName (hostname);
            int port = tcp_address.getPort();

            DebugOutput.println(DebugOutput.DBG_NORMAL, "tcplist: creating "
                                + "ordinary server socket: "
                                + hostname + ":" + port);

            server_sock = new ServerSocket (port, BACK_LOG, host);
         }
      }
      catch (BindException exc) {
         throw new commErrors_noContactPoint();
      }
      catch (UnknownHostException exc) {
         DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
         throw new commErrors_noContactPoint();
      }
      catch (IOException exc) {
         DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
         throw new commErrors_comm();
      }

      // Install the contact point
      try {
         acceptor.setContactPoint (server_sock);
      }
      catch (IllegalStateException exc) {
         // already invoked
         throw new commErrors_invOp();
      }

      // Save a contact point string. Note that the user-provided contact
      // point may have been null

      String contact_host = server_sock.getInetAddress().getHostAddress();
      int contact_port = server_sock.getLocalPort();
      TcpAddress addr = new TcpAddress(contact_host, contact_port);

      contact_point = addr.toString();
   }

   public void closeContact()
   {
      // No methods can be called concurrent with close(), so declared
      // thread-unsafety of acceptor.registerCallBack(),
      // acceptor.setContactPoint() and acceptor.close() is not a problem.

      acceptor.close();
   }

   // listener interface methods

   public void pauseListenCB(short /* g.bool */ on)
   {
     acceptor.pause(on == g.bool.True);
   }

   public void regListenCB (listenCB cb, g.opaque user) throws commErrors
   {
      try {
         acceptor.registerCallBack (cb, user);
      }
      catch (IllegalStateException exc) {
         // already invoked
         throw new commErrors_invOp();
      }
   }

   public connection accept() throws Exception
   {
      return acceptor.consume(); // may throw a commErrors exception
   }

   /**
      Creates a TcpConnection with the given socket, and returns it as
      a properly initialised Globe object.
   */
   private connection createConnection (Socket sock) throws IOException
   {
      TcpConnection conn = new TcpConnection(sock);

      try {
	 SOInf conn_soi = conn.getSOI();
         StdUtil.initGlobeObject (conn_soi, this.soi.getContext(),
                              ctx_namer.nextName ());
         return (connection) conn_soi.swapInf (connection.infid);
      }
      catch (Exception exc) {
         // We don't expect any Globe exceptions
         exc.printStackTrace();
         throw new AssertionFailedException();
      }
   }

  /**
      An object which runs and interfaces with a callback thread. An
      Acceptor functions as a buffer with a producer (the callback thread)
      and a consumer (the user).
      <p>
      The callback thread is a producer of incoming connections and I/O errors,
      both of which it obtains by listening to the socket. It makes a
      callback to the user for each entry it places in the buffer. It goes to
      sleep if it finds the buffer full.
      <p>
      TcpListener.accept() is a consumer of incoming connections and I/O
      errors. The consumer is non-blocking and never goes to sleep.
      <p>
      The object should be cleaned up using close(). Callbacks can temporarily
      be suspended using pause().
   */
   private class Acceptor implements Runnable
   {
      /** User's callback. Installed by registerCallBack(). */
      private listenCB user_cb;
 
      /** User's callback identifier. Installed by registerCallBack(). */
      private g.opaque user_cookie;
 
      /** Contact point. Installed by setContactPoint(). */
      private ServerSocket server_sock;
 
      /**
       * The callback thread. Installed once both registerCallBack() and
       * setContactPoint() have both been invoked.
       */
      private Thread cb_thread;
 
      /**
       * Flag which is set to true by close(). This flag causes the callback
       * thread to avoid blocking and to shut down. Once set it is never
       * cleared.
       */
      private boolean closing = false;

      /**
       * Flag which is set by pause() to suspend callbacks, and cleared by
       * pause() to resume them. This flag is effective even if 'closing' is
       * set.
       */
      private boolean paused = false;

      /**
       * Flag which is set by the callback thread while performing a callback
       * invocation. This flag is used by pause() to wait for a concurrent
       * callback invocation to finish.
       */
      private boolean cb_active = false;

      // A buffer of size 1, containing a connection or an exception, but not
      // both:

      /** Buffered connection. Null if none buffered. */
      private connection buffered_conn;

      /** Buffered exception. Null if none buffered. */
      private commErrors buffered_exc;

      protected void finalize() throws Throwable
      {
        super.finalize();
        if (paused) {
          DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	   "[TcpListener]:  unpausing during Acceptor finalize");
          pause (false);
	}
        if (! closing)
           close();
      }

      /**
       * Installs the user's callback. If setContactPoint() has also been
       * called, then creates and runs the callback thread. This method is
       * thread-safe with respect to setContactPoint(), but thread-unsafe with
       * respect to close().
       *
       * @exception IllegalStateException
       *    if this operation has already been invoked 
       */
      public synchronized void registerCallBack (listenCB user_cb,
                     g.opaque user_cookie) throws IllegalStateException
      {
        if (this.user_cb != null)
          throw new IllegalStateException();

	this.user_cb = user_cb;
        this.user_cookie = user_cookie;
        if (server_sock != null)
          runThread();
      }

      /**
       * Installs the contact point. If registerCallBack() has also been called,
       * then creates and runs the callback thread. This method is thread-safe
       * with respect to registerCallBack(), but thread-unsafe with respect to
       * close().
       *
       * @exception IllegalStateException
       *    if this operation has already been invoked 
       */
      public synchronized void setContactPoint (ServerSocket server_sock)
                     throws IllegalStateException
      {
        if (this.server_sock != null)
          throw new IllegalStateException();

        this.server_sock = server_sock;
        if (user_cb != null)
          runThread();
      }

      /**
       * Creates and runs the callback thread.
       */
      private void runThread ()
      {
        cb_thread = new Thread (this, "TcpListener");
        cb_thread.setDaemon (true);
        cb_thread.start();
      }

      /**
       * Consumes the next buffer element without blocking. Connection elements
       * are simply returned. I/O error elements are thrown as exceptions.
       *
       * @return 
       *    a connection if the next element was a connection, null if neither a
       *    connection nor an I/O error was waiting (i.e. empty buffer)
       *
       * @exception commErrors
       *    if the next element was an I/O error
       */
      public synchronized connection consume() throws commErrors
      {
         if (buffered_conn == null && buffered_exc == null)
            return null;

         // about to consume so wake up a sleeping producer
         notify();

         if (buffered_exc != null) {
            commErrors save_exc = buffered_exc;
            buffered_exc = null;
            throw save_exc;
         }
         else {
            connection save_conn = buffered_conn;
            buffered_conn = null;
            return save_conn;
         }
      }

      /**
       * Suspends or resumes callbacks, depending on the value of 'on'. On
       * return of pause (on==true), all callbacks (including the final
       * listenStopped()) are guaranteed to have suspended and to no longer be
       * active, if necessary by waiting for a currently active callback to
       * finish.
       */
      public synchronized void pause(boolean on)
      {
        if (paused == on) {
          // no change
          return;
        }

        paused = on;

        if (paused) { // suspending

          // wait for callback thread to finish a concurrent callback
          while (cb_active) {
            try {
              wait();
            }
            catch (InterruptedException exc) {
              DebugOutput.println (
		      "[TcpListener.pause] : ignoring interruption", exc);
            }
          }
        }
        else {        // resuming
          // wake up a suspended callback thread
          notify();
        }
      }

      /**
       * Asynchronously cleans up the callback thread and the socket. On
       * shutdown, the callback thread will perform the following actions:
       * callbacks to the user are discontinued, a final callback is made to
       * listenStopped(), and finally the user's callback interface is
       * released. Note that some of these actions may be delayed if callbacks
       * are suspended by pause().
       *
       * This method is thread-unsafe with respect to registerCallBack() and 
       * setContactPoint()
       */
      public void close()
      {
        /*
          Alert the callback thread by setting a flag, and interrupting it
          in case it's blocked producing and/or reading the socket. First set
          the flag to let it know what's going on.
        */

        synchronized (this) {
          closing = true;
          notify();
        }

	if (cb_thread != null) {          // this test is not thread-safe
	  cb_thread.interrupt();
        }

        try {
          // avoid holding a lock (e.g. being in a synchronized block) here,
          // just in case this call blocks
          if (server_sock != null) {      // this test is not thread-safe
            server_sock.close();
          }
        }
        catch (IOException exc) {
          DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
        }
      }


      // from here on: methods that are invoked by the callback thread



      /**
       * Run by the callback thread until terminated by close(). On termination,
       * makes the final callback to the user (listenStopped()) and releases
       * the user's interface.
       */
      public void run ()
      {
         while ( runOnce() ) ;
      }

      /**
       * Invoked by the callback thread to produce a single element for the
       * buffer, and make a single callback to the user. After close() has
       * been invoked, the final callback to the user is made
       * (listenStopped()) and user's interface released.
       *
       * @return false once the final callback has been made, and the callback
       *               thread may terminate
       */
      private boolean runOnce()
      {
        /*
          1. Obtain an element for the buffer, and save in a local variable.
             Avoid holding a lock (e.g. being in a synchronized block) while
             reading the socket. We may be interrupted if close() is invoked
             concurrently.
        */

        connection conn = null;     // connection element for the buffer
        commErrors comm_exc = null; // I/O error element for the buffer

        try {
          // wrap a connection inside a TcpConnection Globe object
          conn = createConnection (server_sock.accept());
	  DebugOutput.println (DebugOutput.DBG_DEBUGPLUS, "tcplist: accepted connection" );
        }
        catch (IOException exc) {

          // I/O exceptions occur while reading the socket. They can be
          // caused by proper I/O errors or by the socket being closed.

          if (! closing) {
            comm_exc = new commErrors_comm();
            DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
          }
        }

        // assert (closing || (conn != null || comm_exc != null))

        /*
          2. Place the element in the buffer.
        */
        synchronized (this) {
          try {
            if (! closing) {
              if (conn != null) {
		 produce (conn);                 // may wait()
              }
              else if (comm_exc != null) {
		 produce (comm_exc);             // may wait()
              }
              else {
                DebugOutput.dassert (false);
              }
            }
          }
          catch (InterruptedException exc) {
            // caused by close().
            DebugOutput.dassert (closing);
          }

          /*
            3. Prepare the callback:
               o If callbacks are paused, wait for a concurrent call to
                 pause(false).
               o Ensure that pause(true) is suspended for the duration of the
                 callback by setting cb_active.

               The decision to proceed (i.e. finding paused == false) must be
               atomic with setting cb_active.
          */

	  while (paused) {
            try {
              wait();
            }
            catch (InterruptedException exc) {
              // caused by close().
              DebugOutput.dassert (closing);
            }
          }

          cb_active = true;
        } // end synchronized

        /*
          4. Make the callback. It is good practice not to hold a lock (e.g. be
             in a synchronized block) while making a callback. The decision to
             make either a normal callback or a final callback must be atomic
             with deciding the return value. Therefore, save the current value
             of 'closing' in a local variable.
        */

        boolean my_closing = closing;
        try {
          if (! my_closing) {
            user_cb.connArrived (user_cookie);
          }
          else {
            // make final callback
            try {
              user_cb.listenStopped (user_cookie);
	    }
            finally {
              // release the interface
              user_cb = null;
            }
          }
        }
        catch (Exception exc) { // exception thrown by callback invocation
          DebugOutput.printException(exc);
        }
        finally {

          /*
            5. Release a suspended concurrent call to pause(true) by clearing
               cb_active.
          */

          synchronized (this) 
	  {
   	            cb_active = false;
        	    notify();    // wake up a call to pause(true)
          }
          return ! my_closing;
        }
      }

      /**
       * Invoked by the callback thread to add a connection to the buffer.
       * Blocks if the buffer is full.
       *
       * @exception InterruptedException
       *    if the producer could not go to sleep or was woken up, due to a
       *    call to close()
       */
      private synchronized void produce (connection conn)
                              throws InterruptedException
      {
         producerWait();
         buffered_conn = conn;
      }

      /**
       * Invoked by the callback thread to add an I/O error to the buffer.
       * Blocks if the buffer is full.
       *
       * @exception InterruptedException
       *    if the producer could not go to sleep or was woken up, due to a
       *    call to close()
       */
      private synchronized void produce (commErrors exc)
                                          throws InterruptedException
      {
         producerWait();
         buffered_exc = exc;
      }

      /**
       * Invoked by the callback thread to block until there is room to add
       * an element to the buffer.
       *
       * @exception InterruptedException
       *    if the producer could not go to sleep or was woken up, due to a
       *    call to close()
       */
      private synchronized void producerWait() throws InterruptedException
      {
         while (! closing && (buffered_conn != null || buffered_exc != null)) {
           wait();
         }

         if (closing) {
           throw new InterruptedException();
         }
      }
   }
}
