/* ARCWriterTest
 *
 * $Id: ARCWriterTest.java,v 1.37 2006/08/25 17:34:38 stack-sf Exp $
 *
 * Created on Dec 31, 2003.
 *
 * Copyright (C) 2003 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.io.arc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.archive.io.ArchiveRecord;
import org.archive.io.ReplayInputStream;
import org.archive.io.WriterPoolMember;
import org.archive.util.ArchiveUtils;
import org.archive.util.FileUtils;
import org.archive.util.TmpDirTestCase;


/**
 * Test ARCWriter class.
 *
 * This code exercises ARCWriter AND ARCReader.  First it writes ARCs w/
 * ARCWriter.  Then it validates what was written w/ ARCReader.
 *
 * @author stack
 */
public class ARCWriterTest
extends TmpDirTestCase implements ARCConstants {
    /**
     * Prefix to use for ARC files made by JUNIT.
     */
    private static final String PREFIX =
        /* TODO DEFAULT_ARC_FILE_PREFIX*/ "IAH";
    
    private static final String SOME_URL = "http://www.archive.org/test/";

    
    private static final AtomicInteger SERIAL_NO = new AtomicInteger();

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected static String getContent() {
        return getContent(null);
    }
    
    protected static String getContent(String indexStr) {
        String page = (indexStr != null)? "Page #" + indexStr: "Some Page";
        return "HTTP/1.1 200 OK\r\n" +
        "Content-Type: text/html\r\n\r\n" +
        "<html><head><title>" + page +
        "</title></head>" +
        "<body>" + page +
        "</body></html>";
    }

    protected int writeRandomHTTPRecord(ARCWriter arcWriter, int index)
    throws IOException {
        String indexStr = Integer.toString(index);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Start the record with an arbitrary 14-digit date per RFC2540
        String now = ArchiveUtils.get14DigitDate();
        int recordLength = 0;
        byte[] record = (getContent(indexStr)).getBytes();
        recordLength += record.length;
        baos.write(record);
        // Add the newline between records back in
        baos.write("\n".getBytes());
        recordLength += 1;
        arcWriter.write("http://www.one.net/id=" + indexStr, "text/html",
            "0.1.2.3", Long.parseLong(now), recordLength, baos);
        return recordLength;
    }

    private File writeRecords(String baseName, boolean compress,
        int maxSize, int recordCount)
    throws IOException {
        cleanUpOldFiles(baseName);
        File [] files = {getTmpDir()};
        ARCWriter arcWriter = new ARCWriter(SERIAL_NO, Arrays.asList(files),
            baseName + '-' + PREFIX, compress, maxSize);
        assertNotNull(arcWriter);
        for (int i = 0; i < recordCount; i++) {
            writeRandomHTTPRecord(arcWriter, i);
        }
        arcWriter.close();
        assertTrue("Doesn't exist: " +
                arcWriter.getFile().getAbsolutePath(), 
            arcWriter.getFile().exists());
        return arcWriter.getFile();
    }

    private void validate(File arcFile, int recordCount)
    throws FileNotFoundException, IOException {
        ARCReader reader = ARCReaderFactory.get(arcFile);
        assertNotNull(reader);
        List metaDatas = null;
        if (recordCount == -1) {
            metaDatas = reader.validate();
        } else {
            metaDatas = reader.validate(recordCount);
        }
        reader.close();
        // Now, run through each of the records doing absolute get going from
        // the end to start.  Reopen the arc so no context between this test
        // and the previous.
        reader = ARCReaderFactory.get(arcFile);
        for (int i = metaDatas.size() - 1; i >= 0; i--) {
            ARCRecordMetaData meta = (ARCRecordMetaData)metaDatas.get(i);
            ArchiveRecord r = reader.get(meta.getOffset());
            String mimeType = r.getHeader().getMimetype();
            assertTrue("Record is bogus",
                mimeType != null && mimeType.length() > 0);
        }
        reader.close();
        assertTrue("Metadatas not equal", metaDatas.size() == recordCount);
        for (Iterator i = metaDatas.iterator(); i.hasNext();) {
                ARCRecordMetaData r = (ARCRecordMetaData)i.next();
                assertTrue("Record is empty", r.getLength() > 0);
        }
    }

    public void testCheckARCFileSize()
    throws IOException {
        runCheckARCFileSizeTest("checkARCFileSize", false);
    }

    public void testCheckARCFileSizeCompressed()
    throws IOException {
        runCheckARCFileSizeTest("checkARCFileSize", true);
    }

    public void testWriteRecord() throws IOException {
        final int recordCount = 2;
        File arcFile = writeRecords("writeRecord", false,
                DEFAULT_MAX_ARC_FILE_SIZE, recordCount);
        validate(arcFile, recordCount  + 1); // Header record.
    }
    
    public void testRandomAccess() throws IOException {
        final int recordCount = 3;
        File arcFile = writeRecords("writeRecord", true,
            DEFAULT_MAX_ARC_FILE_SIZE, recordCount);
        ARCReader reader = ARCReaderFactory.get(arcFile);
        // Get to second record.  Get its offset for later use.
        boolean readFirst = false;
        String url = null;
        long offset = -1;
        long totalRecords = 0;
        boolean readSecond = false;
        for (final Iterator i = reader.iterator(); i.hasNext(); totalRecords++) {
            ARCRecord ar = (ARCRecord)i.next();
            if (!readFirst) {
                readFirst = true;
                continue;
            }
            if (!readSecond) {
                url = ar.getMetaData().getUrl();
                offset = ar.getMetaData().getOffset();
                readSecond = true;
            }
        }
        
        reader = ARCReaderFactory.get(arcFile, offset);
        ArchiveRecord ar = reader.get();
        assertEquals(ar.getHeader().getUrl(), url);
        ar.close();
        
        // Get reader again.  See how iterator works with offset
        reader = ARCReaderFactory.get(arcFile, offset);
        int count = 0;
        for (final Iterator i = reader.iterator(); i.hasNext(); i.next()) {
            count++;
        }
        reader.close();
        assertEquals(totalRecords - 1, count);
    }

    public void testWriteRecordCompressed() throws IOException {
        final int recordCount = 2;
        File arcFile = writeRecords("writeRecordCompressed", true,
                DEFAULT_MAX_ARC_FILE_SIZE, recordCount);
        validate(arcFile, recordCount + 1 /*Header record*/);
    }
    
    private void runCheckARCFileSizeTest(String baseName, boolean compress)
    throws FileNotFoundException, IOException  {
        writeRecords(baseName, compress, 1024, 15);
        // Now validate all files just created.
        File [] files = FileUtils.getFilesWithPrefix(getTmpDir(), PREFIX);
        for (int i = 0; i < files.length; i++) {
            validate(files[i], -1);
        }
    }
    
    protected ARCWriter createARCWriter(String NAME, boolean compress) {
        File [] files = {getTmpDir()};
        return new ARCWriter(SERIAL_NO, Arrays.asList(files), NAME,
            compress, DEFAULT_MAX_ARC_FILE_SIZE);
    }
    
    protected static ByteArrayOutputStream getBaos(String str)
    throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(str.getBytes());
        return baos;
    }
    
    protected static void writeRecord(ARCWriter writer, String url,
        String type, int len, ByteArrayOutputStream baos)
    throws IOException {
        writer.write(url, type, "192.168.1.1", (new Date()).getTime(), len,
            baos);
    }
    
    protected int iterateRecords(ARCReader r)
    throws IOException {
        int count = 0;
        for (Iterator i = r.iterator(); i.hasNext();) {
            ARCRecord rec = (ARCRecord)i.next();
            rec.close();
            if (count != 0) {
                assertTrue("Unexpected URL " + rec.getMetaData().getUrl(),
                    rec.getMetaData().getUrl().equals(SOME_URL));
            }
            count++;
        }
        return count;
    }
    
    protected ARCWriter createArcWithOneRecord(String name,
        boolean compressed)
    throws IOException {
    	ARCWriter writer = createARCWriter(name, compressed);
        String content = getContent();
        writeRecord(writer, SOME_URL, "text/html",
            content.length(), getBaos(content));
        return writer;
    }
    
    public void testSpaceInURL() {
        String eMessage = null;
        try {
            holeyUrl("testSpaceInURL-" + PREFIX, false, " ");
        } catch (IOException e) {
            eMessage = e.getMessage();
        }
        assertTrue("Didn't get expected exception: " + eMessage,
            eMessage.startsWith("Metadata line doesn't match"));
    }

    public void testTabInURL() {        
        String eMessage = null;
        try {
            holeyUrl("testTabInURL-" + PREFIX, false, "\t");
        } catch (IOException e) {
            eMessage = e.getMessage();
        }
        assertTrue("Didn't get expected exception: " + eMessage,
            eMessage.startsWith("Metadata line doesn't match"));
    }
    
    protected void holeyUrl(String name, boolean compress, String urlInsert)
    throws IOException {
    	ARCWriter writer = createArcWithOneRecord(name, compress);
        // Add some bytes on the end to mess up the record.
        String content = getContent();
        ByteArrayOutputStream baos = getBaos(content);
        writeRecord(writer, SOME_URL + urlInsert + "/index.html", "text/html",
            content.length(), baos);
        writer.close();
    }
    
// If uncompressed, length has to be right or parse will fail.
//
//    public void testLengthTooShort() throws IOException {
//        lengthTooShort("testLengthTooShort-" + PREFIX, false);
//    }
    
    public void testLengthTooShortCompressed() throws IOException {
        lengthTooShort("testLengthTooShortCompressed-" + PREFIX, true, false);
    }
    
    public void testLengthTooShortCompressedStrict()
    throws IOException {      
        String eMessage = null;
        try {
            lengthTooShort("testLengthTooShortCompressedStrict-" + PREFIX,
                true, true);
        } catch (RuntimeException e) {
            eMessage = e.getMessage();
        }
        assertTrue("Didn't get expected exception: " + eMessage,
            eMessage.startsWith("java.io.IOException: Record ENDING at"));
    }
     
    protected void lengthTooShort(String name, boolean compress, boolean strict)
    throws IOException {
    	ARCWriter writer = createArcWithOneRecord(name, compress);
        // Add some bytes on the end to mess up the record.
        String content = getContent();
        ByteArrayOutputStream baos = getBaos(content);
        baos.write("SOME TRAILING BYTES".getBytes());
        writeRecord(writer, SOME_URL, "text/html",
            content.length(), baos);
        writeRecord(writer, SOME_URL, "text/html",
            content.length(), getBaos(content));
        writer.close();
        
        // Catch System.err into a byte stream.
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        System.setErr(new PrintStream(os));
        
        ARCReader r = ARCReaderFactory.get(writer.getFile());
        r.setStrict(strict);
        int count = iterateRecords(r);
        assertTrue("Count wrong " + count, count == 4);

        // Make sure we get the warning string which complains about the
        // trailing bytes.
        String err = os.toString();
        assertTrue("No message " + err, err.startsWith("WARNING") &&
            (err.indexOf("Record ENDING at") > 0));
    }
    
//  If uncompressed, length has to be right or parse will fail.
//
//    public void testLengthTooLong()
//    throws IOException {
//        lengthTooLong("testLengthTooLongCompressed-" + PREFIX,
//            false, false);
//    }
    
    public void testLengthTooLongCompressed()
    throws IOException {
        lengthTooLong("testLengthTooLongCompressed-" + PREFIX,
            true, false);
    }
    
    public void testLengthTooLongCompressedStrict() {
        String eMessage = null;
        try {
            lengthTooLong("testLengthTooLongCompressed-" + PREFIX,
                true, true);
        } catch (IOException e) {
            eMessage = e.getMessage();
        }
        assertTrue("Didn't get expected exception: " + eMessage,
            eMessage.startsWith("Premature EOF before end-of-record"));
    }
    
    protected void lengthTooLong(String name, boolean compress,
            boolean strict)
    throws IOException {
    	ARCWriter writer = createArcWithOneRecord(name, compress);
        // Add a record with a length that is too long.
        String content = getContent();
        writeRecord(writer, SOME_URL, "text/html",
            content.length() + 10, getBaos(content));
        writeRecord(writer, SOME_URL, "text/html",
            content.length(), getBaos(content));
        writer.close();
        
        // Catch System.err.
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        System.setErr(new PrintStream(os));
        
        ARCReader r = ARCReaderFactory.get(writer.getFile());
        r.setStrict(strict);
        int count = iterateRecords(r);
        assertTrue("Count wrong " + count, count == 4);
        
        // Make sure we get the warning string which complains about the
        // trailing bytes.
        String err = os.toString();
        assertTrue("No message " + err, 
            err.startsWith("WARNING Premature EOF before end-of-record"));
    }
    
    public void testGapError() throws IOException {
    	ARCWriter writer = createArcWithOneRecord("testGapError", true);
        String content = getContent();
        // Make a 'weird' RIS that returns bad 'remaining' length
        // after the call to readFullyTo.
        ReplayInputStream ris = new ReplayInputStream(content.getBytes(),
                content.length(), null) {
            private boolean readFullyToCalled = false;
            public void readFullyTo(OutputStream os)
            throws IOException {
                super.readFullyTo(os);
                this.readFullyToCalled = true;
            }
            
            public long remaining() {
                return (this.readFullyToCalled)? -1: super.remaining();
            }
        };
        String message = null;
        try {
        writer.write(SOME_URL, "text/html", "192.168.1.1",
            (new Date()).getTime(), content.length(), ris);
        } catch (IOException e) {
            message = e.getMessage();
        }
        writer.close();
        assertTrue("No gap when should be",
            message != null &&
            message.indexOf("Gap between expected and actual") >= 0);
    }
    
    /**
     * Write an arc file for other tests to use.
     * @param arcdir Directory to write to.
     * @param compress True if file should be compressed.
     * @return ARC written.
     * @throws IOException 
     */
    public static File createARCFile(File arcdir, boolean compress)
    throws IOException {
        File [] files = {arcdir};
        ARCWriter writer = new ARCWriter(SERIAL_NO, Arrays.asList(files),
            "test", compress, DEFAULT_MAX_ARC_FILE_SIZE);
        String content = getContent();
        writeRecord(writer, SOME_URL, "text/html", content.length(),
            getBaos(content));
        writer.close();
        return writer.getFile();
    }
    
//    public void testSpeed() throws IOException {
//        ARCWriter writer = createArcWithOneRecord("speed", true);
//        // Add a record with a length that is too long.
//        String content = getContent();
//        final int count = 100000;
//        logger.info("Starting speed write of " + count + " records.");
//        for (int i = 0; i < count; i++) {
//            writeRecord(writer, SOME_URL, "text/html", content.length(),
//                    getBaos(content));
//        }
//        writer.close();
//        logger.info("Finished speed write test.");
//    }
    
    
    public void testValidateMetaLine() throws Exception {
        final String line = "http://www.aandw.net/images/walden2.png " +
            "128.197.34.86 20060111174224 image/png 2160";
        ARCWriter w = createARCWriter("testValidateMetaLine", true);
        try {
            w.validateMetaLine(line);
            w.validateMetaLine(line + LINE_SEPARATOR);
            w.validateMetaLine(line + "\\r\\n");
        } finally {
            w.close();
        }
    }
    
    public void testArcRecordOffsetReads() throws Exception {
    	// Get an ARC with one record.
		WriterPoolMember w =
			createArcWithOneRecord("testArcRecordInBufferStream", true);
		w.close();
		// Get reader on said ARC.
		ARCReader r = ARCReaderFactory.get(w.getFile());
		final Iterator i = r.iterator();
		// Skip first ARC meta record.
		ARCRecord ar = (ARCRecord) i.next();
		i.hasNext();
		// Now we're at first and only record in ARC.
		ar = (ARCRecord) i.next();
		// Now try getting some random set of bytes out of it 
		// at an odd offset (used to fail because we were
		// doing bad math to find where in buffer to read).
		final byte[] buffer = new byte[17];
		final int maxRead = 4;
		int totalRead = 0;
		while (totalRead < maxRead) {
			totalRead = totalRead
			    + ar.read(buffer, 13 + totalRead, maxRead - totalRead);
			assertTrue(totalRead > 0);
		}
	}
}
