/*
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
 * All rights reserved.
 * 
 * Contact: howl@objectweb.org
 * 
 * This software is licensed under the BSD license.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *     
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------------------------------------------------------------------
 * $Id: LogBuffer.java,v 1.10 2005/11/15 22:43:54 girouxm Exp $
 * ------------------------------------------------------------------------------
 */
package org.objectweb.howl.log;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.util.zip.Adler32;


/**
 * Classes used as buffers in LogBufferManager must implement this interface.
 * 
 * <p>This abstract class implements methods common to all LogBuffer sub-classes.
 */
abstract class LogBuffer extends LogObject
{
  /**
   * 
   */
  final ByteBuffer buffer;

  /**
   * buffer number used by owner (LogBufferManager) to workerID into an array of buffers.
   * 
   * <p>Actual use of <i> workerID </i> is determined by the buffer manager implementation.
   */
  int index = -1;

  /**
   * currentTimeMillis that buffer was initialized.
   * 
   * <p>Implementations of LogBuffer should provide some means
   * to persist <i> tod </i> to the file to allow recovery
   * operations to determine when a buffer was written.
   * 
   * <p>Used during replay situations to validate block integrity.
   * The TOD field from the block header is compared with the TOD field
   * of the block footer to verify that an entire block was written.
   */
  long tod = 0;

  /**
   * number of waiting threads.
   * <p>Always synchronized on (waitingThreadsLock).
   * 
   * Design Note:<br/>
   * Originally this field was volatile.  It was changed
   * at the suggestion of David Jencks to protected.
   * <p>We tried using (this) to protect access,
   * but this caused some additional lock contention
   * and performance fell off about 10%.
   * 
   */
  protected int waitingThreads = 0;
  
  /**
   * mutex for synchronizing access to waitingThreads
   */
  final Object waitingThreadsLock = new Object();

  /**
   * results of last write.
   * <p>Value must be one of the constants defined in LogBuffer interface.
   */
  int iostatus = 0;
  
  /**
   * buffer sequence number.
   * <p>LogBufferManager maintains a sequence number
   * of buffers written. The sequence number is stored
   * in the block header of each log block.
   * <p>Initialized to zero.
   * <p>Set to -1 by read() if bytes read is -1 (end of file) 
   */
  int bsn = 0;
  
  /**
   * set true if this LogBuffer should issue a rewind on the FileChannel before
   * writing ByteBuffer.
   */
  boolean rewind = false;
  
  /**
   * set true by LogFileManager if the buffer must be forced
   * due to file switch.
   * 
   * <p>BUG: 300505
   */
  boolean forceNow = false;
  
  /**
   * IOException from last write
   */
  IOException ioexception = null;
  
  /**
   * name of this buffer object.
   */
  String name = null;
  
  /**
   * LogFile associated with the current LogBuffer.
   * 
   * <p>The LogBufferManager will have a pool of LogBuffers.  If the containing
   * Logger is managing a pool of files, then it is possible
   * that some period of time, some buffers will be written to one file, while other 
   * buffers are written to another file.  To allow writes and forces to separate
   * files to be performed in parallel, each LogBuffer must keep track of its own
   * LogFile.
   * 
   * @see org.objectweb.howl.log.LogFileManager#getLogFileForWrite(LogBuffer)
   */
  LogFile lf = null;
  
  /**
   * switch to enable computation of checksum.
   * 
   * <p>Since it takes some CPU to compute checksums over a buffer,
   * it might be useful to disable the checksum, at least for performance
   * measurements.
   * <p>When <i> doChecksum </i> is true then an implementation class
   * should compute a checksum and store the value in the buffer during
   * the write() method.
   * <p>Use of checksums is optional and depends on the actual implementation
   * class. 
   */
  boolean doChecksum = true;
 
  /**
   * Number of used data bytes in the buffer.
   * 
   * <p>This is different than buffer capacity().  The bytes used is the
   * number of bytes of usable data in a buffer.  Bytes between the
   * bytes used count and the buffer footer are undefined, possibly
   * uninitialized or residue.
   * 
   * <p>set by operations that read data from files into the
   * buffer such as read().
   * <p>checked by operations that retrieve logical records
   * from the buffer get().
   */
  int bytesUsed = 0;
  
  /**
   * Local buffer used to compute checksums.
   * 
   * The initial implementation of HOWL used the ByteBuffer.hashCode()
   * method to compute a checksum.  Since each vendor's implementation
   * of the JVM is not guaranteed to generate the same value for hashCode,
   * this approach is not portable, and could result in journal files
   * that appear to be invalid if restarted using a JVM by a vendor
   * that is different than the JVM that was used to write the journal
   * initially.
   * 
   * The problem is solved by implementing our own checksum method.
   * The checksumBuffer member is used to obtain a local copy of
   * the ByteBuffer contents for purposses of computing the checksum.
   * Instead of obtaining each byte individually, the entire buffer
   * is retrieved into checksumBuffer for computing the checksum.
   * 
   */
  byte[] checksumBuffer = null; // BUG 304291
  
  final Adler32 checksum; // BUG 304291
  
  /**
   * default constructor.
   * <p>after creating a new instance of LogBuffer the caller must
   * invoke config().
   */
  LogBuffer(Configuration config)
  {
    super(config);  // LogObject 
    name = this.getClass().getName();
    doChecksum = config.isChecksumEnabled();
    buffer = ByteBuffer.allocateDirect(config.getBufferSize() * 1024); // BUG 300957
    checksum = (doChecksum && config.isAdler32ChecksumEnabled()) ? new Adler32(): null;
  }

  /**
   * decrements count of waiting threads and returns updated value.
   *
   * @return number of threads still waiting after the release.
   */
  final int release()
  {
    synchronized(waitingThreadsLock)
    {
      return --waitingThreads;
    }
  }
  
  /**
   * returns the number of threads currently waiting for
   * the buffer to be forced to disk.
   * 
   * @return current value of waitingThreads 
   */
  final int getWaitingThreads()
  {
    synchronized (waitingThreadsLock)
    {
      return waitingThreads;
    }
  }

  /**
   * park threads that are waiting for the ByteBuffer to
   * be forced to disk.
   * <p>The count of waiting threads (<i> waitingThreads </i>)
   * has been incremented in <i> put() </i>.
   */
  final void sync() throws IOException, InterruptedException
  {
    if (Thread.interrupted()) throw new InterruptedException();

    synchronized(this)
    {
      while (iostatus != LogBufferStatus.COMPLETE)
      {
        if (iostatus == LogBufferStatus.ERROR) {
          // BUG 303907 add a message to the IOException
          throw new IOException("LogBuffer.sync(): LogBufferStatus.ERROR");
        }
        wait();
      }
    }
  }

  /**
   * Computes a checksum over the the entire byte buffer
   * backing this LogBuffer object.
   * 
   * @return the computed checksum.
   */
  int checksum()
  {
    int result = 0;
    buffer.clear();
    
    if (checksum == null)
    {
      result = buffer.hashCode();
    }
    else
    {
      byte[] checksumBuffer;
      if (buffer.hasArray())
        checksumBuffer = buffer.array();
      else {
        // allocate a local buffer once to avoid excessive garbage collection
        if (this.checksumBuffer == null)
          this.checksumBuffer = new byte[buffer.capacity()];
        checksumBuffer = this.checksumBuffer;
        buffer.get(checksumBuffer);
      }
      
      checksum.reset();
      checksum.update(checksumBuffer, 0, checksumBuffer.length);
      result = (int) (checksum.getValue() & 0xFFFFFFFF);
    }

    return result;
  }
  
  /**
   * May be used in traces or other purposes for debugging.
   * 
   * @return name of LogBuffer object.
   */
  String getName()
  {
    return "(" + name + ")";
  }
  
  /**
   * initialize members for LogBuffer implementation class for reuse.
   * <p>LogBufferManager maintains a pool of LogBuffer objects. Each
   * time a LogBuffer is allocated from the pool for use as the current
   * collection buffer, the init() routine is called.  After performing necessary
   * initialization, the LogBuffer invokes the LogFileManager to obtain
   * a LogFile for use when writing and forcing the buffer.  If a file
   * switch occurrs, the LogFileManager will store a file header record
   * into this newly initialized buffer.
   * 
   * @param bsn Logical Block Sequence Number of the buffer.
   * LogBufferManager maintains a block sequence number
   * to ensure correct order of writes to disk.  Some implementations
   * of LogBuffer may include the BSN as part of a record or
   * block header.
   * 
   * @param lfm LogFileMaager to call to obtain a LogFile.
   * 
   * @return this LogBuffer
   * 
   */ 
  abstract LogBuffer init(int bsn, LogFileManager lfm) throws LogFileOverflowException;

  /**
   * read a block of data from the LogFile object provided
   *  in the <i> lf </i> parameter starting at the position
   * specified in the <i> postiion </i> parameter.
   * 
   * <p>Used by LogFileManager implementations to read
   * blocks of data that are formatted by a specific LogBuffer
   * implementation.
   * 
   * <p>The LogFileManager uses LogBufferManager.getLogBuffer() method to obtain
   * a buffer that is used for reading log files.
   *   
   * @param lf LogFile to read.
   * @param position within the LogFile to be read.
   * 
   * @return this LogBuffer reference.
   * @throws IOException
   * @throws InvalidLogBufferException
   */
  abstract LogBuffer read(LogFile lf, long position) throws IOException, InvalidLogBufferException;
  
  /**
   * returns <b>true</b> if the buffer should be forced to disk.
   * <p>The criteria for determining if a buffer should be
   * forced is implementation dependent.  Typically, this 
   * method will return true if there are one or more threads
   * waiting for the buffer to be forced and the amount
   * of time the threads has been waiting has been longer
   * than some implementation defined value.
   *
   * @return true if buffer should be forced immediately.
   */ 
  abstract boolean shouldForce();
  
  /**
   * puts a data record into the buffer and returns a token for record.
   * 
   * <p>PRECONDITION: caller holds a bufferManager monitor.
   * 
   * <p>The caller must set the sync parameter true if the thread
   * will ultimately call sync() after a successful put().
   * This strategy allows the waitingThreads counter to be
   * incremented while the current thread holds the bufferManager
   * monitor.
   * 
   * <p>Implementations should return a token that can be used
   * later for replay, and for debugging purposes.
   * 
   * <p>The data record is passed as a byte[][] allowing
   * callers to construct data records from individual bits
   * of information. 
   * The arrays are concatenated into a single log record whose size
   * is the sum of the individual array sizes.
   * Each array is preceded by the size of the individual array.
   * The entire record is preceded by the size of all arrays
   * including the individual array size fields.
   * <p>The record format is as follows:
   * <pre>
   * +------+------------+----------------+---------+       +----------------+---------+
   * | type | total size | data[0].length | data[0] | . . . | data[n].length | data[n] |
   * +------+------------+----------------+---------+       +----------------+---------+
   * </pre>
   * <p>During replay the entire record is returned as a single
   * byte[].  The ReplayListener is responsible for
   * decomposing the record into the original array of byte[].

   * @param type short containing implementation defined record
   * type information.  The <var> type </var> is stored as the first
   * field of the log record.
   * @param data byte[][] to be written to log.
   * @param sync true if thread will call sync following the put.
   * Causes count of waitingThreads to be incremented.
   * 
   * @throws LogRecordSizeException
   * if the sum of all <i> data[] </i> array sizes is larger than
   * the maximum allowed record size for the configured buffer size.
   *
   * @return  a long that contains the physical position of the record is returned.
   * The value returned by put() is an encoding of the physical 
   * position.  The format of the returned value is implementation
   * specific, and should be treated as opaque by the caller.
   * Returns 0 if there is no room for the record in the current buffer.
   */
  abstract long put(short type, byte[][] data, boolean sync) throws LogRecordSizeException;
  
  /**
   * write ByteBuffer to the LogFile.
   *
   * <p>updates the buffer header with the number
   * of bytes used. Based on configuration, some implementations
   * may compute a hash code or other integrety value and
   * include the value in some implementation defined header
   * information. 
   * <p>The buffer is written using the LogFile.write() method
   * to allow the LogFile to manage file position for circular
   * journals.
   * <p>forcing and notification of waiting threads is
   * the responsibility of the LogBufferManager that owns this LogBUffer.
   * 
   * @throws IOException
   *          rethrows any IOExceptions thrown by FileChannel methods.
   *
   * <p>QUESTION: should waiters be interupted if IO Error occurs?
   * @see #init(int, LogFileManager)
   * @see org.objectweb.howl.log.LogFile#write(LogBuffer)
   */
  abstract void write() throws IOException;
  
  /**
   * returns statistics for this LogBuffer object.
   * 
   * <p>information is returned in the form of an XML node that
   * can be included as a nested element in a larger document
   * containing stats for multiple LogBuffer objects, and any
   * containing objects.
   * 
   * @return statistics for this buffer as XML
   */
  abstract String getStats();

}