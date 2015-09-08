/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.objectweb.howl.journal;

import java.io.File;
import java.io.FileFilter;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.1 $ $Date: 2004/01/26 20:59:30 $
 */
public class JournalTestCase extends TestCase {

    private static final byte TEST_DATA[][] = { "Entry1".getBytes(), "Entry2".getBytes(), "Entry3".getBytes(), "Entry4".getBytes(), };
    static final File file = new File("journal.log");

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        File[] files = file.getCanonicalFile().getParentFile().listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().startsWith(file.getName());
            }
        });
        for (int i = 0; i < files.length; i++)
            files[i].delete();

    }

    public void testCheckpointRestart() throws Throwable {
        Journal journal = new Journal(file);
        journal.start();

        journal.add(TEST_DATA[0]);
        long cp = journal.startCheckPoint();
        journal.add(TEST_DATA[1]);
        journal.clearCheckPoint();
        for (int i = 0; i < TEST_DATA.length; i++) {
            journal.add(TEST_DATA[i]);
        }
        journal.stop();
        journal.close();

        try {
            // Restart the journal..  See if the check points match up..
            journal = new Journal(file);
            assertEquals(cp, journal.getLastClearedCheckPointID());
            journal.close();
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void testAddEntry() throws Exception {
        Journal journal = new Journal(file);
        journal.start();

        for (int i = 0; i < TEST_DATA.length; i++) {
            journal.add(TEST_DATA[i]);
        }
        journal.stop();
        journal.close();
        
    }

    int messageCounter = 0;
    public void testJournalReplay() throws Exception {
        Journal journal = new Journal(file);
        journal.start();

        journal.add(TEST_DATA[0]);
        long cp = journal.startCheckPoint();
        journal.add(TEST_DATA[1]);
        journal.clearCheckPoint();
        for (int i = 0; i < TEST_DATA.length; i++) {
            journal.add(TEST_DATA[i]);
        }
        journal.stop();
        journal.close();

        // Restart the journal..  
        journal = new Journal(file);

        // See if the number of replay message matches up right. 
        messageCounter = 0;
        journal.replayFromCheckpoint(cp, new ReplayListener() {
            public void add(byte[] data) {
                messageCounter++;
            }
            public void startCheckPoint(long checkpointID) {
            }
            public void clearCheckPoint(long checkpointID) {
            }
        });
        journal.close();

        // We should get the messages that we added after the start checkpoint.
        assertEquals(TEST_DATA.length + 1, messageCounter);
    }

    public void testRolling() throws Exception {

        Journal journal = new Journal(file);
        journal.start();

        final int MESSAGE_COUNT = 30000;
        final int MESSAGE_SIZE = 1024;
        final int MESSAGE_SYNC_COUNT = 2000;
        final int MESSAGE_CHECK_POINT = 5000;

        byte[] data = new byte[MESSAGE_SIZE];
        for (int i = 0; i < MESSAGE_SIZE; i++)
            data[i] = (byte) i;

        int clearCount = 12;
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            if ((i % MESSAGE_SYNC_COUNT) == 0)
                journal.addAndSync(data);
            else
                journal.add(data);

            if ((i % MESSAGE_CHECK_POINT) == 0) {
                journal.startCheckPoint();
                clearCount = 0;
            }

            if (clearCount == 10)
                journal.clearCheckPoint();
            clearCount++;
        }

        journal.stop();
        journal.close();
    }

}
