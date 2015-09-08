/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// PassivationCheckpointThread.java

package vu.globe.svcs.objsvr.perstm;


import java.io.*;
import java.util.*;

import vu.globe.util.debug.DebugOutput;
import vu.globe.util.log.*;

import vu.globe.svcs.objsvr.perstm.chkptfile.*;


/**
 * This class represents a thread which periodically dumps the passivation
 * state of the object server to a so-called checkpoint file. The passivation
 * state consists of the state of the persistence manager and the
 * passivation state of the persistent objects. In order to collect a
 * consistent passivation state, the object server is temporarily frozen.
 * <p>
 * During a dump (passivation checkpoint), the following actions are
 * taken (in the order given): 1) collect the passivation state; 2) write
 * the passivation state to a temporary file; 3) rename the existing
 * checkpoint file (if any) to the backup checkpoint file; 4) rename the
 * temporary file to the checkpoint file; 5) remove the backup file.
 * <p>
 * During step 1, which is performed by the persistence manager,
 * the object server is `frozen' (i.e., all activity is
 * disabled). When step 1 is finished, the object server resumes its
 * normal operation. Meanwhile, the remaining steps are carried out by
 * this thread. A temporary backup file (step 3) is used as a safety
 * measure in the case step 4 fails. If the backup file exists, but
 * not the checkpoint file, the backup file should be used to recover
 * the state.
 * <p>
 * By writing the state to a temporary checkpoint file and then renaming
 * this file, an atomic write of a checkpoint file is achieved.
 */
public class PassivationCheckpointThread
  extends Thread
{
  private long _interval;             // dump interval (msec)
  private PerstMgr _pmMgr;            // the persistence manager
  private File _checkpointFile;       // checkpoint file
  private String _checkpointFname;    // name of the checkpoint file
  private File _tmpFile;              // temporary checkpoint file
  private String _tmpFname;           // name of the temporary file
  private File _bakFile;              // backup checkpoint file
  private File _logFile;              // log file (if null: do not create log)
  private boolean _shouldRun;         // if cleared, stop check pointing
  private boolean _isBusy;            // set when busy with performing chkpoint
  private boolean _checkpointOnStart; // if set, perform checkpoint on start

  // Factory to create a checkpoint file writer.
  private ChkptFileReaderWriterFactory _readerWriterFactory;

  // `Factory' for dates in the common log file format.
  private static CommonLogFileDateFormat _clfDateFormatter =
                                            new CommonLogFileDateFormat();

  private String _dateCheckpoint;    // creation date of the current checkpoint
  private int _numCheckpoint;        // current number of checkpoints made

  // Entries in in an array which holds timing statistics
  private static final int TIME_TOTAL      = 0;
  private static final int TIME_CHECKPOINT = 1;
  private static final int TIME_WRITESTATE = 2;
  private static final int TIME_MISC       = 3;
  private static final int NUM_TIME        = 4;


  /**
   * Instance creation.
   *
   * @param  pmMgr              persistence manager
   * @param  interval           dump interval (seconds)
   * @param  checkpointOnStart  if set, perform the first checkpoint
   *                            immediately, without waiting for the dump
   *                            interval to pass by
   * @param  checkpointFile     checkpoint file to be written
   * @param  bakFile            backup file to be written
   * @param  logFile            log file to be written; if <code>null</code>,
   *                            no log file will be written
   * @param  fact               factory to create a checkpoint file writer
   */
  public PassivationCheckpointThread(PerstMgr pmMgr, int interval,
                         boolean checkpointOnStart,
                         File checkpointFile, File bakFile, File logFile,
                         ChkptFileReaderWriterFactory fact)
  {
    _pmMgr = pmMgr;
    _interval = 1000 * interval;
    _checkpointOnStart = checkpointOnStart;
    _checkpointFile = checkpointFile;
    _checkpointFname = checkpointFile.getPath();
    _readerWriterFactory = fact;
    _bakFile = bakFile;
    _tmpFname = _checkpointFname + ".tmp";
    _tmpFile = new File(_tmpFname);
    _logFile = logFile;                               // possibly null
    _shouldRun = true;
    _isBusy = false;
    _numCheckpoint = 1;

    DebugOutput.println(DebugOutput.DBG_DEBUG,
      ";; [PassivationCheckpointThread] checkpoint interval = "
      + interval + "s");
  }


  /**
   * Run the thread. This method exits when <code>_shouldRun</code>
   * is cleared (see <code>halt()</code>).
   */
  public void run()
  {
    long timeStart, interval;
    Date date;

    /*
     * If requested, perform the first checkpoint now.
     */ 
    if (_checkpointOnStart) {
      try {
        _isBusy = true;
        performCheckpoint();
      }
      catch(IOException e) {
        System.err.println("Passivation checkpoint " + _numCheckpoint
                           + " failed: " + e.getMessage());
      }
      finally {
        synchronized(this) {
          _isBusy = false;
          this.notify();
        }
        _numCheckpoint++;
      }
    }

    while (_shouldRun) {
      interval = _interval;
      timeStart = System.currentTimeMillis();

      /*
       * Sleep.
       */
      for ( ; ; ) {
        try {
          Thread.sleep(interval); 
        }
        catch(InterruptedException e) {
          if (!_shouldRun) {
            return;
          }

          // Continue sleeping if we woke up too early.
          long timeEnd = System.currentTimeMillis();
          if (timeEnd < timeStart + interval) {
            interval = timeStart + interval - timeEnd;
            continue;
          }
        }
        break;
      }

      if (!_shouldRun) {
        return;
      }

      /*
       * Dump checkpoint.
       */
      try {
        _isBusy = true;
        performCheckpoint();
      }
      catch(IOException e) {
        System.err.println("Passivation checkpoint " + _numCheckpoint
                           + " failed: " + e.getMessage());
      }
      finally {
        synchronized(this) {
          _isBusy = false;
          this.notify();
        }
        _numCheckpoint++;
      }
    }
  }


  /**
   * Signal this thread to stop. Note that this may not stop the
   * thread immediately.
   *
   * @see <code>waitUntilHalted</code>
   */
  public synchronized void halt()
  {
    _shouldRun = false;
    notify();

    if (!_isBusy) {
      interrupt();         // wake up (in the case the thread is sleeping)
    }
  }


  /**
   * Wait until this thread is no longer busy with dumping state.
   *
   * @see <code>halt</code>
   */
  public synchronized void waitUntilHalted()
  {
    for ( ; ; ) {
      while (_isBusy && !_shouldRun) {
        try {
          wait();
        }
        catch(InterruptedException e) {
          System.err.println("checkpoint thread: "
                             + "ignoring interupted exception");
        } 
      }

      if ((_isBusy && !_shouldRun) == false) {
        break;
      }
    }
  }


  /**
   * Perform the next checkpoint by performing the following steps:
   * 1) collect the passivation state; 2) write the passivation state to
   * a temporary file; 3) rename the existing checkpoint file (if any) to
   * the backup checkpoint file; 4) rename the temporary file to the
   * checkpoint file; 5) remove the backup file; 6) write a log.
   * Step 6 is optional.
   *
   * @exception  IOException  if an error occurs
   */
  private void performCheckpoint()
    throws IOException
  {
    Date date = new Date();
    long start, end;
    long tmElapsed[] = new long[NUM_TIME];

    _dateCheckpoint = _clfDateFormatter.format(date);

    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Starting passivation checkpoint " + _numCheckpoint
                        + " - " + _dateCheckpoint);

    start = System.currentTimeMillis();

    PassivationCheckpointData checkPointData = null;

    /*
     * Step 1: Get the object server's passivation state.
     */
    try {
      checkPointData = _pmMgr.performPassivationCheckpoint();
    }
    catch(PassivationCheckpointException e) {
      throw new IOException("cannot get passivation data: " + e.getMessage());
    }

    ObjSvrPassivationState gosPstate = checkPointData.gosPassivationState;

    tmElapsed[TIME_CHECKPOINT] = System.currentTimeMillis();

    /*
     * Step 2: Write the state to the temporary checkpoint file.
     */
    try {
      writeCheckpointFile(_tmpFile, gosPstate);
    }
    catch(IOException e) {
      throw new IOException("cannot write temporary checkpoint file "
                            + _tmpFile.getPath() + ": " + e.getMessage());
    }
    finally {
      gosPstate.clear();
    }

    tmElapsed[TIME_WRITESTATE] = System.currentTimeMillis();

    /*
     * Step 3: Rename the checkpoint file to the backup file.
     */
    if (_checkpointFile.exists()) {
      if (_checkpointFile.renameTo(_bakFile)) {
        _checkpointFile = new File(_checkpointFname);             // reset
      }
      else {
        throw new IOException("cannot rename checkpoint file "
                              + _tmpFile.getPath());
      }
    }

    /*
     * Step 4: Rename the temporary to the regular checkpoint file.
     */
    if (_tmpFile.renameTo(_checkpointFile)) {
      _tmpFile = new File(_tmpFname);                           // reset
    }
    else {
      throw new IOException("cannot rename temporary checkpoint file "
                            + _tmpFile.getPath());
    }

    /*
     * Step 5: Remove the backup file.
     */
    if (_bakFile.exists() && !_bakFile.delete()) {
      System.err.println("Cannot remove backup file: " + _bakFile.getPath());
    }
 
    end = System.currentTimeMillis();

    // Make times relative.
    tmElapsed[TIME_MISC] = end - tmElapsed[TIME_WRITESTATE];
    tmElapsed[TIME_WRITESTATE] -= tmElapsed[TIME_CHECKPOINT];
    tmElapsed[TIME_CHECKPOINT] -= start;
    tmElapsed[TIME_TOTAL] = end - start;

    /*
     * Write the dump log.
     */
    if (_logFile != null) {
      try {
        writeLogFile(_logFile, checkPointData.posStatus, tmElapsed);
      }
      catch(IOException e) {
        throw new IOException("cannot create dump log: " + e.getMessage());
      }
    }

    /*
     * Successful dump.
     */

    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Passivation checkpoint " + _numCheckpoint
                        + " completed");
    DebugOutput.println(DebugOutput.DBG_DEBUG, "Checkpoint statistics:");
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Checkpoint file         : "
                        + _checkpointFile.getPath());
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Checkpoint file size    : " + _checkpointFile.length()
                        + " bytes");
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Total time              : "
                        + tmElapsed[TIME_TOTAL] + " ms");
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Collecting state        : "
                        + tmElapsed[TIME_CHECKPOINT] + " ms");
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Writing state to disk   : "
                        + tmElapsed[TIME_WRITESTATE] + " ms");
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "Miscellaneous operations: " +
                        + tmElapsed[TIME_MISC] + " ms");
  }


  /**
   * Create a new checkpoint file and write the object server's passivation
   * state to it.
   *
   * @param  file       output file
   * @param  gosPstate  object server passivation state
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writeCheckpointFile(File file, ObjSvrPassivationState gosPstate)
    throws IOException
  {
    ChkptFileWriter writer = null;

    try {
      writer = _readerWriterFactory.createWriter(file);

      writer.write(gosPstate);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch(IOException e) {
          System.err.println("Cannot close checkpoint file: " + e.getMessage());
        }
      }
    }
  }


  /**
   * Create a log and write it to disk.
   *
   * @param  file       file to write the log to (created if necessary)
   * @param  posStatus  array holding the checkpoint status of the persistent
   *                    objects (optional)
   * @param  tmElapsed  timing statistics (optional)
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writeLogFile(File file, PassivationCheckpointStatus posStatus[],
                            long tmElapsed[])
    throws IOException
  {
    RandomAccessFile f = null;

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Creating dump log " + file.getPath()
                        + " for passivation checkpoint " + _numCheckpoint);

    try {
      f = new RandomAccessFile(file.getPath(), "rw");
      f.writeBytes("Checkpoint log -- " + _dateCheckpoint + "\n");

      f.writeBytes("\n");
      f.writeBytes("Checkpoint file     : " + _checkpointFile.getPath() + "\n");
      f.writeBytes("Checkpoint file size: " + _checkpointFile.length()
                   + " bytes\n");

      if (posStatus != null) {
        int i, numFailedToPrepare = 0, numFailedToPerform = 0,
            numFailedToComplete = 0, numOk = 0;
        PassivationCheckpointStatus stat;

        for (i = 0; i < posStatus.length; i++) {
          stat = posStatus[i];
          if (stat.hasPrepared() == false) {
            numFailedToPrepare++;
          }
          else if (stat.hasPerformed() == false) {
            numFailedToPerform++;
          }
          else if (stat.hasCompleted() == false) {
            numFailedToComplete++;
          }
          else {
            numOk++;
          }
        }

        f.writeBytes("\n");
        f.writeBytes("Total number of persistent objects: " + posStatus.length
                     + "\n");
        f.writeBytes("Successfully dumped               : " + numOk + "\n");
        f.writeBytes("Failed to prepare checkpoint      : "
                     + numFailedToPrepare + "\n");
        f.writeBytes("Failed to perform checkpoint      : "
                     + numFailedToPerform + "\n");
        f.writeBytes("Failed to complete checkpoint     : "
                     + numFailedToComplete + "\n");
      }

      if (tmElapsed != null) {
        f.writeBytes("\n");
        f.writeBytes("Total time               : "
                     + tmElapsed[TIME_TOTAL] + "ms" + "\n");
        f.writeBytes("Collecting state         : "
                     + tmElapsed[TIME_CHECKPOINT] + "ms" + "\n");
        f.writeBytes("Writing state to disk    : "
                     + tmElapsed[TIME_WRITESTATE] + "ms" + "\n");
        f.writeBytes("Miscellaneous operations : " +
                     + tmElapsed[TIME_MISC] + "ms" + "\n");
      }
    }
    catch (IOException e) {
      throw new IOException("cannot create dump log "
                            + file.getPath() + ": " + e.getMessage());
    }
    finally {
      if (f != null) {
        try {
          f.close();
        }
        catch(IOException e) {
          System.err.println("Cannot close dump log: " + e.getMessage());
        }
        f = null;
      }
    }
  }
}
