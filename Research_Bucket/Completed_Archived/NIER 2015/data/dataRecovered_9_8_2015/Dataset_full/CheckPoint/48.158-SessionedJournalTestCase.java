/*
 * Copyright (c) Core Developers Network LLC, All rights reserved
 */
package org.objectweb.howl.journal;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import org.objectweb.howl.journal.spi.RollingLogHardener;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.1 $ $Date: 2004/01/28 22:17:47 $
 */
public class SessionedJournalTestCase extends TestCase {

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


    public void testAddEntry() throws Exception {
        SessionedJournal journal = new SessionedJournal(new Journal(file));
        journal.start();

        for (int i = 0; i < TEST_DATA.length; i++) {
            journal.add(TEST_DATA[i],"Key:"+i,false);
        }
        journal.stop();
        journal.close();
        
    }

    public void testAddAndSyncEntry() throws Exception {
        SessionedJournal journal = new SessionedJournal(new Journal(file));
        journal.start();

        for (int i = 0; i < TEST_DATA.length; i++) {
            journal.addAndSync(TEST_DATA[i],"Key:"+i,false);
        }
        journal.stop();
        journal.close();
        
    }

    int messageCounter = 0;
    public void testRecover() throws Exception {
        SessionedJournal journal = new SessionedJournal(new Journal(file));
        journal.start();

        for (int i = 0; i < TEST_DATA.length; i++) {
            journal.add(TEST_DATA[i],"Key:"+i,false);
        }
        journal.add(null,"Key:"+0,true);
        journal.stop();
        journal.close();

        // Restart the journal..  
        journal = new SessionedJournal(new Journal(file));
        journal.recover();
        Collection sessions = journal.getOpenSessions();
        
        // Did we recover the right number of sessions??
        assertEquals(TEST_DATA.length-1, sessions.size());
    }

    public void testCheckPointing() throws Exception {
        RollingLogHardener hardener = new RollingLogHardener();
        hardener.setDeleteOldJournals(true);
        SessionedJournal journal = new SessionedJournal(new Journal(file, hardener));
        journal.start();

        for (int i = 0; i < 20000; i++) {
            journal.add(TEST_DATA[0],"Key:A",false);
        }
        journal.add(null,"Key:A",true);
        // A checkpoint will happen midway in this loop
        for (int i = 0; i < 90000; i++) {
            journal.add(TEST_DATA[0],"Key:B",false);
        }
        for (int i = 0; i < 30000; i++) {
            journal.add(TEST_DATA[0],"Key:C",false);
        }
        journal.add(null,"Key:C",true);

        journal.stop();
        journal.close();

        // Restart the journal..  
        journal = new SessionedJournal(new Journal(file));
        journal.recover();
        Collection sessions = journal.getOpenSessions();
        
        // Did we recover the right number of sessions??
        assertEquals(1, sessions.size());
        assertEquals(90000, journal.getSessionData("Key:B").size());
    }

}
