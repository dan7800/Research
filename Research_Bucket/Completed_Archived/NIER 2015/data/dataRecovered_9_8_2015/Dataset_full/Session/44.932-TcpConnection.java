/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.tcp;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;
import vu.globe.rts.comm.idl.p2p.*;
import vu.globe.util.comm.idl.rawData.*;
import vu.globe.util.comm.*;
import vu.globe.rts.comm.skels.*;
import vu.globe.rts.comm.gwrap.P2PDefs;
import vu.globe.util.comm.ProtAddress;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.debug.DebugOutput;

import java.io.*;
import java.net.*;

/**
   The object that implements the connection interfaces. This is a Globe
   object without a class object. It is created by TCP connectors and TCP
   listeners.
*/

/*
   No Class Object
   ---------------

   The reason this object does not have a class object is as follows. After
   instantiating an object through a class object, only its Globe interfaces
   are accessible. Therefore there is no way a Java object can be passed to it.
   In this case, the connection object needs to be initialised with a Java
   Socket. Hence, a class object cannot be used. The Java model of Globe
   probably needs to be fixed.

   TCPConnection state
   -------------------

      a Java socket
      a callback thread
      a buffer for incoming messages
   
   Reading and writing the socket are independent activities. The Java socket
   is written exclusively by a user's thread. The Java socket is read
   exclusively by the callback thread. The callback thread places incoming
   messages and exceptions in an input buffer. The user's invocations to
   receive() access the input buffer. The callback thread may block if the
   buffer is full.

   The callback thread and input buffer are implemented as a SocketReader
   object. This object also provides methods that effect a proper clean-up of
   the callback thread, and implement pauseMsgCB().

   Protocol
   --------

   TcpConnection implements a message protocol on top of TCP. It consists
   of a signed 32-bit field preceding the data. The field contains the
   length of the data following it. In other words, the length does not
   include the header itself. The protocol is exclusively read and written
   by the static readMessage() and writeMessage() methods.

   Closing
   -------

   To close this connection object, a final receiveStopped() callback must be
   made, and the following resources must be released:
   -  the socket must be closed
   -  the callback thread must be cleaned up
   -  the callback interface must be released
   This is implemented inside the SocketReader object as follows.

   The callback thread makes the final receiveStopped() callback, releases the
   callback interface, and terminates itself. It does so after being alerted.
   The callback thread is alerted by:
   -  setting a flag (which is read by the thread)
   -  unblocking the thread, and preventing it from blocking again

   The callback thread blocks in two places: while reading the socket and while
   waiting to place an element in the input buffer. The callback thread's read
   operation is unblocked by interrupting the thread and closing the socket.
   The thread's wait() for space in the input buffer is unblocked using
   notify().

   Note: the above ignores paused callbacks (see below). Since pausing must not
   only suspend regular callbacks but also the final receiveStopped() callback,
   pausing causes all cleanup operations described above to be delayed.

   Pausing
   -------

   The semantics of the IDL pauseMsgCB() method are that an invocation of
   pauseMsgCB (true) will suspend callbacks until pauseMsgCB(false) is invoked.
   In addition, pauseMsgCB(true) will wait for a currently active callback
   invocation to finish before returning. This is implemented inside the
   SocketReader object as follows.

   pauseMsgCB() sets or clears a flag to indicate whether callbacks are to be
   suspended. The flag is read by the callback thread, just before executing a
   callback (whether a regular callback or the final receiveStopped() callback).
   If the thread finds the flag set, it will wait until the flag is cleared.
   
   Another flag is set by the callback thread just before executing a callback
   and cleared just after executing a callback. pauseMsgCB(true) reads this
   flag to determine whether a callback invocation is currently active. If so,
   pauseMsgCB (true) will wait for the flag to be cleared.

   Also see the note at the end of the section on 'Closing'.

   Thread Safety
   -------------

   Except where noted, this code is thread-safe. Note that Java Sockets are
   assumed to be thread-safe.

   Thread safety has been ignored in some cases where it doesn't matter:
   -  regMsgCB() should not be invoked twice, so cannot be invoked
      concurrently.
   -  No invocations should follow closeConn(), so invocations concurrent with
      closeConn() are also illegal.
*/

public class TcpConnection extends connectionSkel
{
   /** The Java socket around which this connection is built. */
   private Socket sock;

   /** The InputStream of sock. */
   private InputStream sock_in;

   /** The OutputStream of sock. */
   private OutputStream sock_out;

   /** The object that reads the socket and runs the callback thread. */
   private SocketReader socket_reader = new SocketReader();

   /** The communication interfaces implemented by this object. */
   private static interfaces COMM_INFS;

   static {
     COMM_INFS = new interfaces (3);

     COMM_INFS.v[0] = comm.infid;
     COMM_INFS.v[1] = connection.infid;
     COMM_INFS.v[2] = msg.infid;
   }

   /**
      Constructs a TcpConnection around the given socket.

      @exception IOException
         if an error occurred while getting the socket's in/out streams. This
         may include connection setup errors.
   */
   public TcpConnection (Socket sock) throws IOException
   {
      this.sock = sock;
      sock_in = sock.getInputStream();
      sock_out = sock.getOutputStream();
   }

   protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
   {
     super.cleanup();
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
     return null;
   }

   public interfaces getCommInfs()
   {
     return COMM_INFS;
   }


   // connection interface methods

   public String getLocal()
   {
      String host = sock.getLocalAddress().getHostAddress();
      int port = sock.getLocalPort();
  
      TcpAddress addr = null;
      addr  = new TcpAddress(host, port);
      return addr.toString();
   }

   public String getRemote()
   {
      String host = sock.getInetAddress().getHostAddress();
      int port = sock.getPort();

      TcpAddress addr = null;
      addr  = new TcpAddress(host, port);
      return addr.toString();
   }


   // msg interface methods

   public void pauseMsgCB(short /* g.bool */ on)
   {
     socket_reader.pause(on == g.bool.True);
   }

   public void regMsgCB (msgCB cb, g.opaque user) throws commErrors
   {
      try {
        socket_reader.registerCallBack (cb, user);
      }
      catch (IllegalStateException exc) {
         // already invoked
         throw new commErrors_invOp();
      }
   }

   public void send (String remote, rawDef pkt) throws Exception
   {
      // note: 'remote' must be ignored

      // DebugOutput.println ("[TcpConnection.send] : sending message");
      try {
        /*
          In Sun JDK 1.2.2, various platforms, InputStream.write() for Socket
          appears to be thread-unsafe! When several threads are concurrently
          writing to the same socket and the data buffers that are written
          exceed some internal block size (typically 2048), the data comes out
          corrupted at the other end: apparently a byte gets lost. The problem
          appears to have been fixed in JDK 1.3. I have not found a bug report
          or an entry in the release notes of subsequent versions.
        */
        synchronized (this) {
          writeMessage (sock_out, pkt);
        }
      }
      catch (IOException exc) {
         DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
         throw new commErrors_comm();
      }
      // DebugOutput.println ("[TcpConnection.send] : message sent");
   }

   public msg_receive_Out receive () throws Exception
   {
      msg_receive_Out out = new msg_receive_Out ();

      out.retval = socket_reader.consume(); // may throw a commErrors
      // the 'remote' field can be left null

      return out;
   }

   public void closeConn()
   {
      // No methods can be called concurrent with closeConn(), so declared
      // thread-unsafety of socket_reader.registerCallBack() and
      // socket_reader.close() is not a problem.
      socket_reader.close();
   }

   /**
      Writes a message to an output stream in accordance with the message
      protocol.
   */
   private static void writeMessage (OutputStream out, rawDef pkt)
                                                   throws IOException
   {
      // create 'tcp_pkt': a header followed by 'pkt'
      rawDef tcp_pkt = RawOps.createRaw();
      RawCursor header = new RawCursor (tcp_pkt);
      RawBasic.writeInt32 (header, RawOps.sizeOfRaw (pkt));
      RawOps.appendRaw (tcp_pkt, pkt);

      RawUtil.outputRaw (tcp_pkt, out, 0, RawOps.sizeOfRaw (tcp_pkt));
   }

   /**
      Reads a message from an input stream in accordance with the message
      protocol.
   */
   private static rawDef readMessage (InputStream in) throws IOException
   {
      // Arno: this s*cks, even though we read the size of the message before hand
      // (in an inefficient way) the message gets fragmented into many 1024 byte arrays
      // due to the rawDef data structure, instead of one big block. In particular bad
      // for method invocations with large args, these need to be reconstructed to
      // a single buffer before they can be given to the Semantics subobject.
      //
      // Grrr... OK, inputRawFully is going going gone!

      rawDef pkt = RawOps.createRaw();
       
      // read the header, to find out the length of the remainder
      if (RawUtil.inputRawFully (pkt, in, 0, 4) < 4)
        throw new EOFException();

      RawCursor header = new RawCursor (pkt);
      int length = RawBasic.readInt32 (header);

      byte[] buf = new byte[ length ]; 
      
      int totalnread = 0;
      while( totalnread < length)
      {
            int nread = in.read( buf, totalnread, length-totalnread );
            totalnread += nread;
      }
      rawDef pkt2 = RawOps.createRaw();
      RawOps.setRaw( pkt2, buf, 0, length );
      return pkt2;
   }

   /**
      An object which runs and interfaces with a callback thread. A
      SocketReader functions as a buffer with a producer (the callback thread)
      and a consumer (the user).
      <p>
      The callback thread is a producer of incoming messages and I/O errors,
      both of which it obtains by reading the socket's input stream. It makes a
      callback to the user for each entry it places in the buffer. It goes to
      sleep if it finds the buffer full.
      <p>
      TcpConnection.receive() is a consumer of incoming messages and I/O
      errors. The consumer is non-blocking and never goes to sleep.
      <p>
      The object should be cleaned up using close(). Callbacks can temporarily
      be suspended using pause().
   */
   private class SocketReader implements Runnable
   {
      /** User's callback. Installed by registerCallBack(). */
      private msgCB user_cb;

      /** User's callback identifier. Installed by registerCallBack(). */
      private g.opaque user_cookie;

      /** The callback thread. Installed by registerCallBack(). */
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

      // A buffer of size 1, containing a message or an exception, but not both:

      /** Buffered message. Null if none buffered. */
      private rawDef buffered_pkt;

      /** Buffered exception. Null if none buffered. */
      private commErrors buffered_exc;

      protected void finalize() throws Throwable
      {
        super.finalize();
        if (paused) {
          DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
           "[TcpConnection]:  unpausing during SocketReader finalize");
          pause (false);
        }
	if (! closing)
          close();
      }

      /**
       * Installs the user's callback and starts running the callback thread.
       * This method is thread-unsafe with respect to close().
       *
       * @exception IllegalStateException
       *   if this operation has already been invoked
       */
      public void registerCallBack (msgCB user_cb, g.opaque user_cookie)
      {
        if (this.user_cb != null)
          throw new IllegalStateException();

        this.user_cb = user_cb;
        this.user_cookie = user_cookie;

	runThread();
      }

      /**
       * Creates and runs the callback thread.
       */
      private void runThread ()
      {
        cb_thread = new Thread (this, "TcpConnection");
        cb_thread.setDaemon (true);
        cb_thread.start();
      }


      /**
       * Consumes the next buffer element without blocking. Message elements
       * are simply returned. I/O error elements are thrown as exceptions.
       *
       * @return 
       *    a message if the next element was a message, null if neither a
       *    message nor an I/O error was waiting (i.e. empty buffer)
       *
       * @exception commErrors
       *    if the next element was an I/O error
      */
      public synchronized rawDef consume() throws commErrors
      {
         if (buffered_pkt == null && buffered_exc == null)
            return null;

         // about to consume so wake up a sleeping producer
         notify();

         if (buffered_exc != null) {
            commErrors save_exc = buffered_exc;
            buffered_exc = null;
            throw save_exc;
         }
         else {
            rawDef save_pkt = buffered_pkt;
            buffered_pkt = null;
            return save_pkt;
         }
      }

      /**
       * Suspends or resumes callbacks, depending on the value of 'on'. On
       * return of pause (on==true), all callbacks (including the final
       * receiveStopped()) are guaranteed to have suspended and to no longer be
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
	      		"[TcpConnection.pause] : ignoring interruption", exc);
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
       * receiveStopped(), and finally the user's callback interface is
       * released. Note that some of these actions may be delayed if callbacks
       * are suspended by pause().
       *
       * This method is thread-unsafe with respect to registerCallBack().
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

	if (cb_thread != null) { // this test is not thread-safe
          cb_thread.interrupt();
	}

        try {
	  // avoid holding a lock (e.g. being in a synchronized block) here,
	  // just in case this call blocks
          sock.close();
        }
        catch (IOException exc) {
          DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
        }
      }


      // from here on: methods that are invoked by the callback thread



      /**
       * Run by the callback thread until terminated by close(). On termination,
       * makes the final callback to the user (receiveStopped()) and releases
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
       * (receiveStopped()) and user's interface released.
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

        rawDef message = null;     // message element for the buffer
	commErrors comm_exc = null; // I/O error element for the buffer

        try {
	  // DebugOutput.println ("[TcpConnection.produce] : recvd message");
          message = readMessage (sock_in);
        }
        catch (IOException exc) {

          // I/O exceptions occur while reading the socket. They can be
          // caused by proper I/O errors or by the socket being closed.

	  if (! closing) {
	    comm_exc = new commErrors_comm();
            if (exc instanceof EOFException)
              DebugOutput.println(DebugOutput.DBG_NORMAL,
                                  "[TcpConnection.produce] : eof exception");
            else 
              DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
	  }
        }

	// assert (closing || (message != null || comm_exc != null))

	/*
	  2. Place the element in the buffer.
	*/
	synchronized (this) {
	  try {
	    if (! closing) {
	      if (message != null) {
	        produce (message);		// may wait()
              }
	      else if (comm_exc != null) {
	        produce (comm_exc);		// may wait()
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
	    user_cb.msgReceived (user_cookie);
	  }
	  else {
            // make final callback
	    try {
              user_cb.receiveStopped (user_cookie);
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

	  synchronized (this) {
	    cb_active = false;
	    notify();    // wake up a call to pause(true)
	  }
	  return ! my_closing;
	}
      }

      /**
       * Invoked by the callback thread to add a message to the buffer. Blocks
       * if the buffer is full.
       *
       * @exception InterruptedException
       *    if the producer could not go to sleep or was woken up, due to a
       *    call to close()
       */
      private synchronized void produce (rawDef pkt)
                              throws InterruptedException
      {
         producerWait();
         buffered_pkt = pkt;
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
	 while (! closing && (buffered_pkt != null || buffered_exc != null)) {
           wait();
	 }

         if (closing) {
           throw new InterruptedException();
	 }
      }

   }
}
