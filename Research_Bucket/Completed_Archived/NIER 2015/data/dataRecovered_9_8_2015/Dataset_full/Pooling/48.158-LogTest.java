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
 * $Id: LogTest.java,v 1.30 2005/12/01 18:16:33 girouxm Exp $
 * ------------------------------------------------------------------------------
 */
package org.objectweb.howl.log;

import java.io.File;
import java.io.FileNotFoundException;

import junit.extensions.RepeatedTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class LogTest extends TestDriver
{

  /**
   * Constructor for LogTest.
   * 
   * @param name
   */
  public LogTest(String name) {
    super(name);
  }
  
  protected void setUp() throws Exception {
    super.setUp();

    log = new Logger(cfg);

  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(LogTest.class);
    return new RepeatedTest(suite, Integer.getInteger("LogTest.repeatcount", 1).intValue());
  }
  
  public void testGetHighMark() throws Exception {
    try {
      log.lfmgr.getHighMark();
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      ; // expected this
    }
    this.deleteLogFiles();
    log.open();
    log.lfmgr.getHighMark();
    log.close();
  }
  
  public void testGetHighMark_NewFiles() throws Exception {
    deleteLogFiles();
    try {
      log.lfmgr.getHighMark();
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      ; // expected this
    }
    
    log.open();
    log.lfmgr.getHighMark();
    log.close();
  }
  
  public void testLoggerSingleThread()
    throws LogException, Exception
  {
    log.open();
    log.setAutoMark(true);
    
    prop.setProperty("msg.count", "10");
    workers = 1;
    runWorkers(LogTestWorker.class);
    // log.close(); called by runWorkers()

  }
  
  public void testLoggerAutomarkTrue()
    throws LogException, Exception
  {
    log.open();
    log.setAutoMark(true);

    runWorkers(LogTestWorker.class);
    // log.close(); called by runWorkers()
  }
  
  public void testLoggerReplay() throws Exception {
    // populate log with some records
    log.open();
    log.setAutoMark(true);
    runWorkers(LogTestWorker.class);
    log.close();
    
    // verify that all records are marked processed
    log.open();
    TestLogReader reader = new TestLogReader();
    reader.run(log);
    log.close();
    assertEquals(getName(), 0L, reader.recordCount);
  }
  
  /**
   * Verifies that replay can begin with a log key that
   * has <b>NOT</b> been forced to the journal.
   * <p>puts a record to the journal with sync == false, then
   * trys to replay from the log key for that record.
   * @throws Exception
   */
  public void testLoggerReplay_unforcedRecord() throws Exception {
    log.open();
    long key = log.put("".getBytes(), false);
    TestLogReader reader = new TestLogReader();
    log.replay(reader, key);
    log.close();
    if (reader.exception != null)
      throw reader.exception;
  }
  
  /**
   * Verifies that replay can begin with a log key that
   * <b>HAS</b> been forced to the journal.
   * <p>puts a record to the journal with sync == false, then
   * trys to replay from the log key for that record.
   * @throws Exception
   */
  public void testLoggerReplay_forcedRecord() throws Exception {
    log.open();
    long key = log.put("".getBytes(), true);
    TestLogReader reader = new TestLogReader();
    log.replay(reader, key);
    log.close();
    if (reader.exception != null)
      throw reader.exception;
  }
  
  public void testMultipleClose() throws Exception {
    log.open();
    log.close();
    log.close();
  }
  
  /**
   * Verify that replay works with newly created files. 
   * @throws Exception
   */
  public void testLoggerReplay_NewFiles() throws Exception {
    deleteLogFiles();
    log.open();
    TestLogReader reader = new TestLogReader();
    reader.run(log, 0L);
    log.close();
    assertEquals("unexpected records found in new log files", 0L, reader.recordCount);    
  }
  
  /**
   * Verifies that replay does not return records that have
   * been marked.
   * 
   * @throws Exception
   */
  public void testLoggerReplay_MarkedRecords() throws Exception {
    long key = 0L;
    int  count = 0;
    
    deleteLogFiles(); // so we know exactly how many records to expect
    log.open();
    // 1. write two records.
    for (int i=1; i < 10; ++i)
    {
      key = log.put(("Record_" + i).getBytes(), false);
      ++count;
    }
    
    // 1a. replay the records we have so far.
    //     there is no mark yet, so we should ge all of the records
    TestLogReader reader = new TestLogReader();
    reader.printRecord = false;
    reader.activeMark = log.getActiveMark();  // NOTE - there is no mark yet
    reader.run(log);
    assertEquals(getName() + ": unexpected record count:", count, reader.recordCount);
    
    
    key = log.put("Mark; replay should start here".getBytes(), false);
    
    // 2. mark
    log.mark(key, true);
    
    // 3. write another record
    key = log.put("Record_X".getBytes(), true);

    // 4. replay should get 2 records, the record we just wrote, and the record at the mark.
    reader = new TestLogReader();
    reader.printRecord = false; // true causes reader to print records
    reader.activeMark = 0L; // replay all records given to onRecord event
    reader.run(log);
    assertEquals(getName() + ": unexpected record count:", 2L, reader.recordCount);
    
    // 5. verify we get same two records if log is closed and reopened
    log.close();
    log.open();
    reader = new TestLogReader();
    reader.printRecord = false; // true causes reader to print records
    reader.run(log);
    assertEquals(getName() + ": unexpected record count:", 2L, reader.recordCount);
    
    // 6. verify we get only 1 record if we skip the marked record
    reader = new TestLogReader();
    reader.printRecord = false; // true causes reader to print records
    reader.activeMark = log.getActiveMark();
    reader.run(log);
    assertEquals(getName() + ": unexpected record count:", 1L, reader.recordCount);
    
    log.close();
  }
  
  public void testLogClosedException() throws Exception, LogException {
    log.open();
    log.close();
    try {
      log.setAutoMark(false);
      fail("expected LogClosedException");
    } catch (LogClosedException e) {
      // this is what we expected so ignore it
    }
  }
  
  /**
   * FEATURE 300922
   * Verify that LogConfigurationException is thrown if multiple
   * openings on the log are attempted.
   * 
   * @throws Exception
   */
  public void testLogConfigurationException_Lock() throws Exception
  {
    log.open();
    Logger log2 = new Logger(cfg);

    try {
      log2.open();
    } catch (LogConfigurationException e) {
      // this is what we expected so ignore it
      log2.close();
    }
    
    log.close();
    
  }
  /**
   * Verify that an invalid buffer class name throws LogConfigurationException.
   * <p>The LogConfigurationException occurs after the log files have been
   * opened and locked.  As a result, it is necessary to call close to 
   * unlock the files.
   * @throws Exception
   */
  public void testLogConfigurationException_ClassNotFound() throws Exception
  {
    cfg.setBufferClassName("org.objectweb.howl.log.noSuchBufferClass");
    
    try {
      log.open();
      log.close();
      fail("expected LogConfigurationException");
    } catch (LogConfigurationException e) {
      if (!(e.getCause() instanceof ClassNotFoundException))
        throw e;
      // otherwise this is what we expected so ignore it
    }
    
    // close and unlock the log files
    log.close();
  }
  
  /**
   * Verify that log.open() will throw an exception if the configuration
   * is changed after a set of log files is created.
   * <p>In this test, we change the number of log files.
   * <p>PRECONDITION: log files exist from prior test.
   * @throws Exception
   */
  public void testLogConfigurationException_maxLogFiles() throws Exception
  {
    // increase number of log files for current set
    cfg.setMaxLogFiles(cfg.getMaxLogFiles() + 1);

    // try to open the log -- we should get an error
    try {
      log.open();
      log.close();
      fail("expected LogConfigurationException");
    } catch (LogConfigurationException e) {
      // this is what we expected so ignore it
    }
    log.close();
  }
  
  /**
   * Verify that FileNotFoundException is processed
   * correctly.
   * 
   * <p>In order to test FileNotFoundException
   * it is necessary to devise a pathname that will fail
   * on all platforms.  The technique used here is to
   * create a file, then use the file as a directory
   * name in cfg.setLogFileDir.  So far, all file
   * systems reject the attempt to create a file
   * subordinate to another file, so this technique
   * seems to be platform neutral.
   * 
   * <p>The test fails if FileNotFoundException is
   * not thrown by the logger.
   * 
   * @throws Exception
   */
  public void testFileNotFoundException() throws Exception
  {
    // create a file (not directory) that will be used as LogFileDir for test
    File invalidDir = new File(outDir, "invalid");
    if (!invalidDir.exists() && !invalidDir.createNewFile())
        fail("unable to create 'invalid' directory");
    
    String invalid = invalidDir.getPath();
    
    // set log dir to some invalid value
    cfg.setLogFileDir(invalid);
    try {
      log.open();
      fail("expected FileNotFoundException");
    } catch (FileNotFoundException e) {
      // this is what we expected
    } finally {
      log.close();
    }
    
    // one more time to make sure the file locks set via system properties are cleared
    // FEATURE 300922
    try {
      log.open();
      fail("expected FileNotFoundException");
    } catch (FileNotFoundException e) {
      // this is what we expected
    } finally {
      log.close();
    }
  }

  public void testLogConfigurationException_1File() throws Exception
  {
    // a single log file is not allowed
    cfg.setMaxLogFiles(1);

    try {
      // open should catch this and get an error
      log.open();
      log.close();
      fail("expected LogConfigurationException");
    } catch (LogConfigurationException e) {
      // this is what we expected so ignore it
    }
  }
  
  public void testLogRecordSizeException() throws Exception {
    log.open();
    // record size == block size is guaranteed to fail
    byte[] data = new byte[cfg.getBufferSize() * 1024]; // BUG 300957
    
    try {
      log.put(data,false);
      fail("expected LogRecordSizeException");
    } catch (LogRecordSizeException e) {
      // this is what we expected so ignore it
    }
    log.close();
  }
  
  public void testInvalidLogKeyException_NegativeKey() throws Exception {
    TestLogReader tlr = new TestLogReader();
    log.open();
    
    // try log key == -1
    try {
      log.replay(tlr, -1L);
      log.close();
      fail("expected InvalidLogKeyException (-1)");
    } catch (InvalidLogKeyException e) {
      // this is what we expected
    }

    log.close();
  }

  public void testInvalidLogKeyException_InvalidKey() throws Exception {
    TestLogReader tlr = new TestLogReader();
    log.open();
    
    // try a key that is invalid
    try {
      log.replay(tlr, (log.getActiveMark() + 1L));
      log.close();
      fail("expected InvalidLogKeyException");
    } catch (InvalidLogKeyException e) {
      // this is what we expected
    } finally {
      saveStats();
    }

    log.close();
  }

  public void testBlockLogBufferSink() throws Exception {
    cfg.setBufferClassName("org.objectweb.howl.log.BlockLogBufferSink");
    log.open();
    log.setAutoMark(true);
    runWorkers(LogTestWorker.class);
    // log.close(); called by runWorkers()
  }
  
  final class DataRecords {  // make class final to clear findbugs warnings
    final int count;
    final String[] sVal;
    final byte[][] r1;
    final long[] key;
    final LogRecord lr;
    
    DataRecords(int count) {
      this.count = count;
      sVal = new String[count];
      r1   = new byte[count][];
      key  = new long[count];

      // initialize test records
      for (int i=0; i< count; ++i)
      {
        sVal[i] = "Record_" + (i+1);
        r1[i] = sVal[i].getBytes();
      }
      int last = count - 1;
      lr = new LogRecord(sVal[last].length()+6);
    }
    
    void putAll(boolean forceLastRecord) throws Exception {
      // populate journal with test records
      for (int i=0; i< count; ++i) {
        boolean force = (i == sVal.length - 1) ? forceLastRecord : false ;
        key[i] = log.put(r1[i], force); 
      }
    }
    
    LogRecord verify(int index) throws Exception {
      log.get(lr, key[index]);
      verifyLogRecord(lr, sVal[index], key[index]);
      return lr;
    }
    
    LogRecord verify(int index, LogRecord lr) throws Exception {
      verifyLogRecord(lr, sVal[index], key[index]);
      return lr;
    }
    
  }
  
  /**
   * Verify that Logger.get() method returns requested records.
   * <p>We write records to the journal than go through
   * a series of Logger.get() requests to verify that Logger.get()
   * works as expected.
   * 
   * <p>We also verify that channel position is not affected
   * by the Logger.get() methods.
   * 
   * @throws Exception
   */
  public void testGetMethods() throws Exception {
    DataRecords dr = new DataRecords(5);
    LogRecord lr = dr.lr;
    
    // make sure we are working from the beginning of a new file.
    deleteLogFiles(); 
    
    log.open();
    
    // populate journal with test records
    dr.putAll(true);
    
    // remember file position for subsequent validations
    long pos = log.lfmgr.currentLogFile.channel.position();
    
    lr.setFilterCtrlRecords(true);
    for (int i=0; i < dr.sVal.length; ++i) {
      dr.verify(i);
    }
    
    // read backwards
    for (int i = dr.sVal.length-1; i >= 0; --i) {
      dr.verify(i);
    }
    long posNow = log.lfmgr.currentLogFile.channel.position();
    assertEquals("File Position", pos, posNow);
    
    // check the Logger.getNext method
    lr = dr.verify(0);
    
    for (int i=1; i < dr.count; ++i) {
      do {
        lr = log.getNext(lr);
      } while (lr.isCTRL()); // skip control records
      dr.verify(i, lr);
    }
    posNow = log.lfmgr.currentLogFile.channel.position();
    assertEquals("File Position", pos, posNow);
    
    // now read to end of journal
    int recordCount = 0;
    while (true) {
      lr = log.getNext(lr);
      if (lr.type == LogRecordType.END_OF_LOG) break;
      ++recordCount;
    }
    posNow = log.lfmgr.currentLogFile.channel.position();
    assertEquals("File Position", pos, posNow);
    
    // read backwards, and write a new record after each read
    for (int j = 0, i = dr.count-1; i >= 0; --i, ++j)
    {
      lr = dr.verify(i);
      log.put(dr.r1[j], true);
    }
    
    // verify that we now have two sets of records
    lr = dr.verify(0);

    for (int i=1; i < dr.count; ++i) {
      do {
        lr = log.getNext(lr);
      } while (lr.isCTRL()); // skip control records
      dr.verify(i, lr);
    }
    
    // make sure have a second set of records.
    for (int i=0; i < dr.count; ++i) {
      do {
        lr = log.getNext(lr);
      } while (lr.isCTRL()); // skip control records
      verifyLogRecord(lr, dr.sVal[i], lr.key);
    }
    
    log.close();
    
  }
  
  /**
   * Verify that Logger.get() method returns a record
   * that was written with force = false.
   * 
   * @throws Exception
   */
  public void testGetMethods_UnforcedRecord() throws Exception {
    DataRecords dr = new DataRecords(1);
    log.open();
    dr.putAll(false);
    dr.verify(0);
    log.close();
  }
  
  /**
   * BUG 303659 - Verify that single instance of log can be
   * opened and closed multiple times.
   * @throws Exception
   */
  public void testMultipleOpenClose() throws Exception
  {
    log.open();
    log.setAutoMark(true);
    
    prop.setProperty("msg.count", "1");
    workers = 1;
    runWorkers(LogTestWorker.class);
    // log.close(); called by runWorkers()
    
    // now see what happens when we open the log a second time
    log.open();
    runWorkers(LogTestWorker.class);
    // log.close(); called by runWorkers()

    // now see what happens when we open the log a third time
    log.open();
    runWorkers(LogTestWorker.class);
    // log.close(); called by runWorkers()
  }
  
  /**
   * Verify that a second attempt to open a log causes
   * a LogConfigurationException.
   * <p>BUG 303907 reported by JOTM is a result of
   * multiple instances of Logger opening same
   * set of files.  The bug resulted from JOTM
   * running with HOWL_0_1_7 version.
   * 
   * Multiple openings are also possible
   * on non-Windows platforms such as
   * Linux and OS/X.
   * 
   * @throws Exception
   */
  public void testMultipleOpen() throws Exception
  {
    log.open();
    
    Logger l2 = new Logger(cfg);
    try {
      l2.open();
      fail("expected LogConfigurationException");
    } catch (LogConfigurationException e) {
      ; // expected result
    }
  }
  
  /**
   * BUG: 303659 - Verify that log can shut down even if FlushManager is not
   * running.
   * <p>If there are unforced records waiting to be written
   * to disk at the time the application closes the log,
   * and if the FlushManager thread has failed for some
   * reason, then the application will hang in LogBufferManager.flushAll()
   * waiting for buffers to be returned to the free pool.
   * <p>This test simulates failure of the FlushManager by
   * seting FlushManager.isClosed = true to prevent it from running.
   * @throws Exception
   */
  public void simulateFlushManagerFailure(boolean flushPartialBuffers) throws Exception
  {
    DataRecords dr = new DataRecords(10);

    cfg.setFlushPartialBuffers(flushPartialBuffers);
    log = new Logger(cfg);
    log.open();
    log.setAutoMark(true);
    
    // simulate FlushManager interrupt
    log.bmgr.flushManager.isClosed = true;
    
    dr.putAll(false);
    log.close();
  }
  
  /**
   * BUG: 303659 - Make sure log can be closed if FlushManager thread has
   * stopped running.
   * @throws Exception
   */
  public void testFlushManagerFailure_FPB_TRUE() throws Exception
  {
    simulateFlushManagerFailure(true);
  }

  /**
   * BUG: 303659 - Make sure log can be closed if FlushManager thread has
   * stopped running.
   * @throws Exception
   */
  public void testFlushManagerFailure_FPB_FALSE() throws Exception
  {
    simulateFlushManagerFailure(false);
  }
  
  /**
   * Verifies the content of the LogRecord is correct.
   * @param lr LogRecord to be verified
   * @param eVal expected value
   * @param eKey expected record key
   */
  void verifyLogRecord(LogRecord lr, String eVal, long eKey) {
    byte[][] r2 = lr.getFields();
    String rVal = new String(r2[0]);
    assertEquals("Record Type: " + Long.toHexString(lr.type), 0, lr.type);
    assertEquals("Record Key: " + Long.toHexString(eKey), eKey, lr.key);
    assertEquals("Record Data", eVal, rVal);
    assertEquals("Field Count != 1", 1, r2.length);
  }
  
  public void testLogFileOverflow() throws Exception
  {
    this.deleteLogFiles();

    int recCount = 1;
    
    cfg.setMaxLogFiles(2);
    cfg.setMaxBlocksPerFile(5);
    log.open();
    
    // display highmark for each log file
    byte[][] record = new byte[][] {"record".getBytes(), "1".getBytes()};
    
    // keep reference to first log file
    long initialKey = log.put(record, false);
    
    // write records until switch to second log file
    LogFile firstLF = log.lfmgr.currentLogFile;
    do {
      record[1] = Integer.toString(++recCount).getBytes();
      log.put(record, false);
    } while (firstLF.equals(log.lfmgr.currentLogFile));

    // write records until switch to first log file -- should get LogFileOverflowException
    try {
      LogFile currentLF = log.lfmgr.currentLogFile;
      do {
        record[1] = Integer.toString(++recCount).getBytes();
        log.put(record, false);
      } while(currentLF.equals(log.lfmgr.currentLogFile));
      
      fail("LogFileOverflowException expected");
    } catch (LogFileOverflowException e) {
      // expected
    }
    
    // verify that first record in file has not been overwritten.
    LogRecord lr = log.get(null, 0L);
    while (lr.type != LogRecordType.USER) log.getNext(lr);
    assertEquals("Unexpected log key: ", initialKey, lr.key);
    byte[][] fields = lr.getFields();
    assertTrue("record".equals(new String(fields[0])));
    assertTrue("1".equals(new String(fields[1])));
    
    // close the log, reopen and write a record -- should get overflow
    log.close();
    
    log = new Logger(cfg);
    log.open();
    System.out.println("activeMark: " + Long.toHexString(log.lfmgr.activeMark));
    try {
      record[1] = Integer.toString(++recCount).getBytes();
      log.put(record, false);
      fail("Expected LogFileOverflowException");
    } catch (LogFileOverflowException e) {
      // expected
    }
    
    log.close(); 
  }

}
