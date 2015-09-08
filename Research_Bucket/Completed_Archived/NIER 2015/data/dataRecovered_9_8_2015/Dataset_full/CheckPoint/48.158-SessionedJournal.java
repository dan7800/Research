/*
 * Copyright (c) Core Developers Network LLC, All rights reserved
 */
package org.objectweb.howl.journal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @version $Revision: 1.1 $ $Date: 2004/01/28 22:17:47 $
 */
public class SessionedJournal {

    Journal journal;
    HashMap openSessions = new HashMap();
    int addCounter=0;

    /**
     * @param journal
     */
    public SessionedJournal(Journal journal) {
        this.journal = journal;
    }

    static final class SessionJournalEvent implements Serializable {
        static final int ADD_EVENT = 0;
        static final int RELOG_EVENT = 1;
        static final int FORGET_EVENT = 3;
        int type;
        Object sessionKey;
        Object data;

        /**
         * @param type
         * @param sessionKey
         * @param data
         */
        public SessionJournalEvent(int type, Object sessionKey, Object data) {
            this.type = type;
            this.sessionKey = sessionKey;
            this.data = data;
        }
    }

    static byte[] toBytes(SessionJournalEvent event) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(event);
        os.close();
        return baos.toByteArray();
    }

    static SessionJournalEvent toSessionJournalEvent(byte data[]) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(bais);
        Object rc = is.readObject();
        is.close();
        return (SessionJournalEvent) rc;
    }

    /**
     * @param data
     * @throws InterruptedException
     */
    public void add(Serializable data, Serializable sessionKey, boolean closeSession) throws InterruptedException, IOException {
        SessionJournalEvent journalEvent = createAddEvent(data, sessionKey, closeSession);
        journal.add(toBytes(journalEvent));
        checkPointCheck();
    }

    public synchronized Collection getOpenSessions() {
        return new HashSet(openSessions.keySet());
    }

    public synchronized Collection getSessionData(Serializable sessionKey) {
        return (Collection)openSessions.get(sessionKey);
    }

    /**
     * @param data
     * @throws InterruptedException
     */
    public void addAndSync(Serializable data, Serializable sessionKey, boolean closeSession) throws InterruptedException, IOException {
        SessionJournalEvent journalEvent = createAddEvent(data, sessionKey, closeSession);
        journal.addAndSync(toBytes(journalEvent));
        checkPointCheck();
    }

    /**
     * Right now just checkpoint after 50,000 adds to the journal..  In the future
     * we can make this smarter.
     */
    synchronized private void checkPointCheck() throws InterruptedException, IOException {
        addCounter++;
        if( addCounter < 100000 ) 
            return;
        addCounter=0;
        
        journal.startCheckPoint();
        Iterator it = openSessions.keySet().iterator();
        while (it.hasNext()) {
            Serializable key = (Serializable) it.next();
            ArrayList session = (ArrayList) openSessions.get(key);
            SessionJournalEvent event = new SessionJournalEvent(SessionJournalEvent.RELOG_EVENT, key, session);
            journal.add(toBytes(event));
        }
        journal.clearCheckPoint();
    }

    private SessionJournalEvent createAddEvent(Object data, Object key, boolean closeSession) {
        SessionJournalEvent journalEvent;
        ArrayList session = getOrCreateSession(key);
        if (!closeSession) {
            session.add(data);
            journalEvent = new SessionJournalEvent(SessionJournalEvent.ADD_EVENT, key, data);
        } else {
            removeSession(key);
            journalEvent = new SessionJournalEvent(SessionJournalEvent.FORGET_EVENT, key, data);
        }
        return journalEvent;
    }

    /**
     * @param key
     */
    synchronized private void removeSession(Object key) {
        openSessions.remove(key);
    }

    /**
     * @param key
     * @return
     */
    synchronized private ArrayList getOrCreateSession(Object key) {
        ArrayList dataList = (ArrayList) openSessions.get(key);
        if (dataList == null) {
            dataList = new ArrayList();
            openSessions.put(key, dataList);
        }
        return dataList;
    }

    /**
     * @throws IOException
     */
    void close() throws IOException {
        journal.close();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return journal.equals(arg0);
    }

    synchronized public void recover() throws IOException {
        openSessions.clear();
        journal.replayFromCheckpoint(journal.getLastClearedCheckPointID(), new ReplayListener() {

            public void add(byte[] data) {
                SessionJournalEvent event;
                try {
                    event = toSessionJournalEvent(data);
                    if (event.type == SessionJournalEvent.ADD_EVENT) {
                        ArrayList session = getOrCreateSession(event.sessionKey );
                        session.add(event.data);
                    } else if (event.type == SessionJournalEvent.FORGET_EVENT ) {
                        removeSession(event.sessionKey);
                    } else if (event.type == SessionJournalEvent.RELOG_EVENT ) {
                        ArrayList session = (ArrayList) event.data;
                        openSessions.put(event.sessionKey, session);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
            public void startCheckPoint(long checkpointID) {
            }
            public void clearCheckPoint(long checkpointID) {
            }
        });

    }

    /**
     * 
     */
    public void start() {
        journal.start();
    }

    /**
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        journal.stop();
    }

}
