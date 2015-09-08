/*
 * Copyright (c) Core Developers Network LLC, All rights reserved
 */
package org.objectweb.howl.journal.spi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import EDU.oswego.cs.dl.util.concurrent.Channel;

import org.objectweb.howl.journal.ReplayListener;

/**
 * This Hardener handles writing Journal events to the 
 * log file that can rollover on a checkpoint.  
 * 
 * This Hardener allways has one online journal (the log file
 * that is currently being appended to) and then zero or more
 * offline journals.  When the online log file exceeds the sizeForRollover
 * length, then when the next start checkpoint event is generated:
 *  1. the online journal is closed and move to the end of the offline 
 * journal list.
 *  2. A new online journal is created and used to harden subsequent events. 
 *  3. If the the clear checkpoint event received and the deleteOldJournals flag
 *     is enabled, then all the offline journals are deleted. 
 * 
 * The log files are always named <file-name>.<n> where n is a positive integer.
 * When the log file rolls over n is allways incremented. 
 *  
 * @version $Revision: 1.3 $ $Date: 2004/01/28 22:17:47 $
 */
public class RollingLogHardener implements Hardener {

    //////////////////////////////////////////////////////////////////////////////
    // Some parameters to tune the Hardener..
    //////////////////////////////////////////////////////////////////////////////
    static final int sizeForRollover = 1024 * 1024 * 8; // 16 Megs
    long bufferSyncDelay = 50;
    // Should old check pointed journals be deleted??
    private boolean deleteOldJournals = false;

    //////////////////////////////////////////////////////////////////////////////
    // What we name our journal log files.
    //////////////////////////////////////////////////////////////////////////////
    private File logDirectory;
    private String logNamePrefix;
    private NumberFormat logNameSuffixFormat;
    private long nextSuffixID = 0;

    //////////////////////////////////////////////////////////////////////////////
    // To track the file names of the journal log files used.
    //////////////////////////////////////////////////////////////////////////////
    private File onlineJournal;
    private ArrayList offlineJournals = new ArrayList(10);

    //////////////////////////////////////////////////////////////////////////////
    // Main processing variables used in the state processing of the online log file.
    //////////////////////////////////////////////////////////////////////////////
    // We get work from this queue
    private Channel eventQueue;
    // Keep track of the listners in this list.
    ArrayList eventsWaitingForHardening = new ArrayList();
    // We use these to write to the log file.
    private FileChannel file;
    private OutputStream bbos;
    private DataOutputStream dos;
    // Used to keep track of time so that we know when to sync the log file.
    private long bufferWriteStartTime;
    private long pollInterval;
    // Holds the id that should be used for the next checkpoint.
    private long nextCheckpointID;
    private long lastClearedCheckPointID;

    /**
     * @return Returns the deleteOldJournals.
     */
    public boolean isDeleteOldJournals() {
        return deleteOldJournals;
    }

    /**
     * @param deleteOldJournals The deleteOldJournals to set.
     */
    public void setDeleteOldJournals(boolean deleteOldJournals) {
        this.deleteOldJournals = deleteOldJournals;
    }

    public RollingLogHardener() {
        logNameSuffixFormat = NumberFormat.getNumberInstance();
        logNameSuffixFormat.setMinimumIntegerDigits(10);
        logNameSuffixFormat.setMaximumIntegerDigits(10);
        logNameSuffixFormat.setGroupingUsed(false);
        logNameSuffixFormat.setParseIntegerOnly(true);
        logNameSuffixFormat.setMaximumFractionDigits(0);
    }

    /**
     * When the journal is opened,
     *  1. All the offline journal files are located, 
     *  2. the last offline journal is selected as the online journal
     *  3. The journal is scaned (in reverse order) to find the last started and cleared checkpoints.
     *
     * @see com.chirino.journal.spi.Hardener#open(java.io.File, EDU.oswego.cs.dl.util.concurrent.Channel)
     */
    public void open(final File journal, Channel eventQueue) throws IOException {
        this.eventQueue = eventQueue;

        logDirectory = journal.getCanonicalFile().getParentFile();
        if (!logDirectory.isDirectory())
            throw new IOException("Invalid journal file: [" + journal + "], the path [" + logDirectory + "] is not a directory.");
        logNamePrefix = journal.getName() + ".";

        // Get a list of all the files 
        File[] files = logDirectory.listFiles(new FileFilter() {
            public boolean accept(File f) {
                try {
                    String name = f.getName();
                    int i = name.indexOf(logNamePrefix);
                    if (i != 0 || name.length() == logNamePrefix.length())
                        return false;

                    // The rest of the name must be an int.
                    name = f.getName().substring(logNamePrefix.length());
                    Number number = logNameSuffixFormat.parse(name);
                    name = logNamePrefix + logNameSuffixFormat.format(number);
                    if (f.getName().equals(name))
                        return true;

                } catch (Throwable e) {
                }
                return false;
            }
        });

        // Is this the first time we use the journal...
        if (files.length == 0) {

            offlineJournals.clear();
            nextSuffixID = 0;
            String fileName = logNamePrefix + logNameSuffixFormat.format(nextSuffixID++);
            onlineJournal = new File(logDirectory, fileName);
            nextCheckpointID = 0;

        } else {

            // Create an array list of the files sorted by name.
            offlineJournals.clear();
            for (int i = 0; i < files.length; i++)
                offlineJournals.add(files[i]);
            Collections.sort(offlineJournals, new Comparator() {
                public int compare(Object f1, Object f2) {
                    File file1 = (File) f1;
                    File file2 = (File) f2;
                    return file1.getName().compareTo(file2.getName());
                }
            });

            // Get the last log file.
            File lastJournal = (File) offlineJournals.remove(offlineJournals.size() - 1);
            try {
                nextSuffixID = logNameSuffixFormat.parse(lastJournal.getName().substring(logNamePrefix.length())).longValue() + 1;
            } catch (ParseException willNotHappen) {
                throw new RuntimeException("Internal Error 1");
            }
            onlineJournal = lastJournal;
            reopenOnlineJournal();
        }

    }

    /**
     * @see com.chirino.journal.spi.Hardener#close()
     */
    public void close() throws IOException {
        eventQueue = null;
        logDirectory = null;
        logNamePrefix = null;
        onlineJournal = null;
        offlineJournals.clear();
    }

    /**
     * We are going to append to an exisiting journal we need to 
     * find out what the next checkpoint id will be and make sure that the 
     * last record was store correctly.
     */
    private void reopenOnlineJournal() throws IOException {
        validate();

        // We should usually find a check point in the online journal file...
        nextCheckpointID = -1;
        lastClearedCheckPointID = -1;
        findPreviousCheckpoints(onlineJournal);
        if (nextCheckpointID != -1 && lastClearedCheckPointID != -1)
            return;

        // If not start looking the offline journals.  Go in reverse order...        
        int s = offlineJournals.size();
        for (int i = s - 1; i >= 0; i--) {
            File f = (File) offlineJournals.get(i);
            findPreviousCheckpoints(onlineJournal);
            if (nextCheckpointID != -1 && lastClearedCheckPointID != -1)
                return;
        }

        // No checkpoint id found..  assume we are at 0
        if (nextCheckpointID == -1)
            nextCheckpointID = 0;
        if (lastClearedCheckPointID == -1)
            lastClearedCheckPointID = 0;

    }

    /**
     * @see com.chirino.journal.spi.Hardener#replayFromCheckpoint(long, com.chirino.journal.ReplayListener)
     */
    public void replayFromCheckpoint(long checkpointID, ReplayListener listener) throws IOException {

        ArrayList journals = new ArrayList(offlineJournals);
        journals.add(onlineJournal);
        long[] pos = seekToPreviousCheckpoint(journals, checkpointID);
        JournalEvent event = new JournalEvent();

        int journalIndex = (int) pos[0];
        long recPos = pos[1];
        while (journalIndex < journals.size()) {

            File f = (File) journals.get(journalIndex);
            RandomAccessFile file = new RandomAccessFile(f, "r");
            try {

                if (journalIndex == pos[0]) {
                    file.seek(pos[1]);
                } else {
                    file.seek(0);
                }

                long fileSize = file.length();
                while (file.getFilePointer() < fileSize) {
                    readExternal(event, file);

                    if (event.eventType == JournalEvent.ADD_EVENT_TYPE) {
                        listener.add(event.data);
                    } else if (event.eventType == JournalEvent.START_CHECKPOINT_EVENT_TYPE) {
                        listener.startCheckPoint(event.checkpointID);
                    } else if (event.eventType == JournalEvent.CLEAR_CHECKPOINT_EVENT_TYPE) {
                        listener.clearCheckPoint(event.checkpointID);
                    }
                }

            } finally {
                file.close();
            }
            journalIndex++;
        }

    }

    /**
     * @param checkpointID
     * @return
     */
    private long[] seekToPreviousCheckpoint(ArrayList journals, long checkpointID) throws IOException {

        for (int i = journals.size() - 1; i >= 0; i--) {
            File f = (File) journals.get(i);

            RandomAccessFile file = new RandomAccessFile(f, "r");
            try {

                long endofRecField = file.length() - 4;
                while (endofRecField > 0) {

                    // Seek to the last field of the record..
                    // It tells us how big the record is.
                    file.seek(endofRecField);
                    int recSize = file.readInt();

                    // Move to the start of rec+4 this field tells us the type of the record.
                    long recPos = file.getFilePointer() - recSize;
                    file.seek(recPos + 4);
                    int type = file.readByte();

                    // We found a start checkpint
                    if (type == JournalEvent.START_CHECKPOINT_EVENT_TYPE) {
                        file.seek(recPos);
                        JournalEvent event = new JournalEvent();
                        readExternal(event, file);
                        if (checkpointID == event.checkpointID) {
                            // Move to next record..
                            file.seek(recPos + recSize);
                            return new long[] { i, recPos };
                        }
                    }
                    // That record was not it..  lets go on to the previous record.
                    endofRecField = recPos - 4;
                }

            } finally {
                file.close();
            }
        }

        // Checkpoint not found.. start with the first log found...
        return new long[] { 0, 0 };

    }

    /**
     * @param journal
     */
    private void findPreviousCheckpoints(File journal) throws IOException {
        RandomAccessFile file = new RandomAccessFile(journal, "r");
        try {

            long endofRecField = file.length() - 4;
            while (endofRecField > 0) {

                // Seek to the last field of the record..
                // It tells us how big the record is.
                file.seek(endofRecField);
                int recSize = file.readInt();

                // Move to the start of rec+4 this field tells us the type of the record.
                long recPos = file.getFilePointer() - recSize;
                file.seek(recPos + 4);
                int type = file.readByte();

                // We found the nextCheckpointID
                if (type == JournalEvent.START_CHECKPOINT_EVENT_TYPE && nextCheckpointID == -1) {
                    file.seek(recPos);
                    JournalEvent event = new JournalEvent();
                    readExternal(event, file);
                    nextCheckpointID = event.checkpointID + 1;
                }

                // We found the lastClearedCheckPointID
                if (type == JournalEvent.CLEAR_CHECKPOINT_EVENT_TYPE && lastClearedCheckPointID == -1) {
                    file.seek(recPos);
                    JournalEvent event = new JournalEvent();
                    readExternal(event, file);
                    lastClearedCheckPointID = event.checkpointID;
                }

                // If we have them both we can stop searching.
                if (nextCheckpointID != -1 && lastClearedCheckPointID != -1)
                    return;

                // That record was not it..  lets go on to the previous record.
                endofRecField = recPos - 4;
            }
        } finally {
            file.close();
        }
    }

    /**
     * Checks the online journal to see if it is intact.
     * @throws IOException
     */
    private void validate() throws IOException {
        RandomAccessFile file = new RandomAccessFile(onlineJournal, "rw");
        try {

            long fileSize = file.length();
            long recPos = 0;
            int records = 0;
            while (recPos < fileSize) {

                // Seek to last record.  
                file.seek(recPos);
                int recSize = file.readInt();

                // Does the record extend past the file???
                if (recPos + recSize > fileSize) {
                    // This may happen is the last log entry was not fully recorded due
                    // to failure.
                    System.out.println("Truncating partial log record.");
                    file.setLength(recPos);
                    fileSize = file.length();

                } else {
                    file.seek(recPos + recSize - 4);
                    int recSize2 = file.readInt();

                    // The only thing I can think of that would cause a corrupted record to be stored
                    // is if the disk is failing..  we can't really try to recover from that.
                    if (recSize != recSize2)
                        throw new IOException("Corrupted record detected.");

                    records++;
                    recPos += recSize;
                }
            }
        } finally {
            file.close();
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        long elapsed, remainingTimeBeforeFlush, now;
        JournalEvent event = null;
        try {

            // Reopen the last log file.
            file = new RandomAccessFile(onlineJournal, "rw").getChannel();
            bbos = Channels.newOutputStream(file);
            dos = new DataOutputStream(bbos);

            // Start writing at the end of the file.
            file.position(file.size());

            pollInterval = Long.MAX_VALUE;
            bufferWriteStartTime = 0;
            while (true) {

                event = (JournalEvent) eventQueue.poll(pollInterval);
                if (event == null) {
                    // We did not get another event for the buffer..  flush now.
                    sync();
                    continue;
                }

                if (event.eventType == JournalEvent.STOP_EVENT_TYPE) {
                    sync();
                    return;
                }

                writeExternal(event, dos);
                if (event.isWaitignForHardening()) {
                    eventsWaitingForHardening.add(event);
                }

                if (event.eventType == JournalEvent.START_CHECKPOINT_EVENT_TYPE) {
                    nextCheckpointID++;
                    if (file.position() > sizeForRollover) {
                        sync();
                        // Roll it over!
                        file.close();
                        offlineJournals.add(onlineJournal);

                        String fileName = logNamePrefix + logNameSuffixFormat.format(nextSuffixID++);
                        onlineJournal = new File(logDirectory, fileName);
                        this.file = new RandomAccessFile(onlineJournal, "rw").getChannel();
                        this.bbos = Channels.newOutputStream(file);
                        this.dos = new DataOutputStream(bbos);

                        continue;
                    }
                }

                if (event.eventType == JournalEvent.CLEAR_CHECKPOINT_EVENT_TYPE) {
                    lastClearedCheckPointID = event.checkpointID;
                    // can we clear out old journals now??
                    if (deleteOldJournals && offlineJournals.size() > 0) {
                        sync();
                        deleteOldJournalLogs();
                        continue;
                    }
                }
                
                now = System.currentTimeMillis();
                if (bufferWriteStartTime == 0)
                    bufferWriteStartTime = now;

                // Force a sync if we have been writting too long without a sync.
                if ( (now-bufferWriteStartTime) > bufferSyncDelay ) {                    
                    sync();
                    continue;
                }
                
                pollInterval = 0;
                
            }
        } catch (InterruptedException e) {
        } catch (IOException e) {
        } finally {
            // Safe close the file if it was opened..
            try { file.close(); } catch (Throwable ignore) {}
            file = null;
        }
    }

    private void sync() throws IOException {
        // flush now.
        file.force(false);
        notifyWaitingEvents();
        // Restart our processing loop.
        pollInterval = Long.MAX_VALUE;
        bufferWriteStartTime = 0;
    }

    /**
     * Delete all the log files except the last file.  This gets called
     * when we clear a checkpoint.
     */
    private void deleteOldJournalLogs() {
        Iterator i = offlineJournals.iterator();
        while (i.hasNext()) {
            File f = (File) i.next();
            f.delete();
            i.remove();
        }
    }

    // Notify the events what were in the buffer that they
    // were hardened.
    void notifyWaitingEvents() {
        int s = eventsWaitingForHardening.size();
        for (int i = 0; i < s; i++) {
            JournalEvent je = (JournalEvent) eventsWaitingForHardening.get(i);
            je.notifyOfHardening();
        }
        eventsWaitingForHardening.clear();
    }

    /**
     * @see com.chirino.journal.spi.Hardener#getNextCheckPointID()
     */
    public long getNextCheckPointID() {
        return nextCheckpointID;
    }

    /**
     * @see com.chirino.journal.spi.Hardener#getLastClearedCheckPointID()
     */
    public long getLastClearedCheckPointID() {
        return lastClearedCheckPointID;
    }

        private static final int SERIALIZED_MIN_SIZE = 4 // The size of the record
        +1 // The type of record.
        +4 // Repeate the size of the record
    ;

    /**
     * Serializes an event to a record.  The size of the record is the first 
     * and last field of the record.  This allows us to navigate the journal
     * file forward and backwards easily even though we are using variable
     * sized records.  
     */
    public void writeExternal(JournalEvent e, DataOutput out) throws IOException {

        int datasize = 0;
        if (e.eventType == JournalEvent.ADD_EVENT_TYPE) {
            datasize = e.data.length;
        } else if (e.eventType == JournalEvent.START_CHECKPOINT_EVENT_TYPE) {
            datasize = 8;
        } else if (e.eventType == JournalEvent.CLEAR_CHECKPOINT_EVENT_TYPE) {
            datasize = 8;
        }

        // Write out how many bytes this record is.
        out.writeInt(SERIALIZED_MIN_SIZE + datasize);
        out.writeByte(e.eventType);
        if (e.eventType == JournalEvent.ADD_EVENT_TYPE) {
            out.write(e.data);
        } else if (e.eventType == JournalEvent.START_CHECKPOINT_EVENT_TYPE) {
            out.writeLong(e.checkpointID);
        } else if (e.eventType == JournalEvent.CLEAR_CHECKPOINT_EVENT_TYPE) {
            out.writeLong(e.checkpointID);
        }
        out.writeInt(SERIALIZED_MIN_SIZE + datasize);
    }

    /**
     * Deserialized a record into an event.
     */
    public void readExternal(JournalEvent e, DataInput in) throws IOException {
        int recSize = in.readInt();
        e.eventType = in.readByte();
        if (e.eventType == JournalEvent.ADD_EVENT_TYPE) {
            e.data = new byte[recSize - SERIALIZED_MIN_SIZE];
            in.readFully(e.data);
        } else if (e.eventType == JournalEvent.START_CHECKPOINT_EVENT_TYPE) {
            e.checkpointID = in.readLong();
        } else if (e.eventType == JournalEvent.CLEAR_CHECKPOINT_EVENT_TYPE) {
            e.checkpointID = in.readLong();
        }
        int recSizeCheck = in.readInt();
        if (recSize != recSizeCheck)
            throw new IOException("Error in serialization protocol detected.");
    }

}