/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.StatisticsIF;
import org.logicalcobwebs.proxool.admin.StatisticsListenerIF;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.text.DecimalFormat;

/**
 * Tests how fast Proxool is compared to the "perfect" pool, {@link SimpoolAdapter}.
 *
 * @version $Revision: 1.18 $, $Date: 2006/01/18 14:40:06 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class PerformanceTest extends AbstractProxoolTest  implements StatisticsListenerIF {

    private static final Log LOG = LogFactory.getLog(PerformanceTest.class);

    private static DecimalFormat millisecondsFormat = new DecimalFormat("0.00");

    private Thread waitingThead;

    private StatisticsIF statistics;
    private static final int PERIOD = 5;
    private static final int COUNT = 6;
    private long servedCount;

    public PerformanceTest(String s) {
        super(s);
    }

    /**
     * Test how many connections we can serve if we go as fast as we can!
     * @throws ProxoolException if anything goes wrong
     */
    public void testPerformance() throws ProxoolException, InterruptedException {

        waitingThead = Thread.currentThread();

        String alias = "testPeformance";
        int threadCount = 20;
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, String.valueOf(threadCount));
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, String.valueOf(Boolean.TRUE));
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, String.valueOf(threadCount));
/*
        info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, String.valueOf(PERIOD) + "s");
        info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_INFO);
*/
        ProxoolFacade.registerConnectionPool(url, info);
/*
        ProxoolFacade.addStatisticsListener(alias, this);
*/

        doWait();

        AnnoyingConnector[] annoyingConnectors = new AnnoyingConnector[threadCount];
        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i] = new AnnoyingConnector(alias);
            Thread t = new Thread(annoyingConnectors[i]);
            t.start();
        }

        for (int i = 0; i < COUNT; i++) {
            doWait();
        }

        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i].cancel();
        }

        for (int i = 0; i < 5; i++) {
            int activeConnectionCount = ProxoolFacade.getSnapshot(alias).getActiveConnectionCount();
            if (activeConnectionCount > 0) {
                LOG.info("Waiting for 10 seconds for connections to become inactive (" + activeConnectionCount + ")");
                Thread.sleep(10000);
            } else {
                break;
            }
        }

        final SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias, true);
        LOG.info("Active count: " + snapshot.getActiveConnectionCount());
        LOG.info("Available count: " + snapshot.getAvailableConnectionCount());
        ConnectionInfoIF[] cis = snapshot.getConnectionInfos();
        LOG.info("Found " + cis.length + " connetions with a detailed snapshot" + "");
        for (int i = 0; i < cis.length; i++) {
            ConnectionInfoIF ci = cis[i];
            LOG.info("#" + ci.getId() + ": " + ci.getStatus() + ", lap=" + (ci.getTimeLastStopActive() - ci.getTimeLastStartActive()));
        }
        LOG.info("Served a total of " + ProxoolFacade.getSnapshot(alias).getServedCount());

    }

    private void doWait() {
        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait(60000);
            } catch (InterruptedException e) {
                fail("Statistics didn't arrive as expected");
            }
        }
    }

    public void statistics(String alias, StatisticsIF statistics) {
        this.servedCount += statistics.getServedCount();
        this.statistics = statistics;
        synchronized (waitingThead) {
            waitingThead.notify();
        }
    }

    public void testSnapshotImpact() throws ProxoolException {

        waitingThead = Thread.currentThread();

        String alias = "testPeformance";
        int threadCount = 10;
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, String.valueOf(threadCount));
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, String.valueOf(threadCount));
        info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, String.valueOf(PERIOD) + "s");
        info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_INFO);
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, String.valueOf(Boolean.TRUE));
        ProxoolFacade.registerConnectionPool(url, info);
        ProxoolFacade.addStatisticsListener(alias, this);
        DisagreeableSnapshotter disagreeableSnapshotter = new DisagreeableSnapshotter(alias);
        new Thread(disagreeableSnapshotter).start();

        AnnoyingConnector[] annoyingConnectors = new AnnoyingConnector[threadCount];
        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i] = new AnnoyingConnector(alias);
            Thread t = new Thread(annoyingConnectors[i]);
            t.start();
        }

        doWait();

        int servedCount = 0;
        for (int i = 0; i < COUNT; i++) {
            doWait();
            servedCount += statistics.getServedCount();
            assertTrue("disparityNoticed", !disagreeableSnapshotter.isDisparityNoticed());
        }

        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i].cancel();
        }
        disagreeableSnapshotter.cancel();

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 30000) {
            int threadsRunning = 0;
            for (int i = 0; i < annoyingConnectors.length; i++) {
                if (annoyingConnectors[i].isRunning()) {
                    threadsRunning++;
                }
            }
            if (disagreeableSnapshotter.isRunning()) {
                threadsRunning++;
            }
            if (threadsRunning == 0) {
                break;
            }
        }


        assertTrue("disparityNoticed", !disagreeableSnapshotter.isDisparityNoticed());

        LOG.info("Served " + servedCount
            + " at " + millisecondsFormat.format((double) (1000 * PERIOD * COUNT) / (double) servedCount) + " ms per connection");

    }

    class DisagreeableSnapshotter implements Runnable {

        private String alias;

        private boolean cancelled;

        private boolean disparityNoticed;

        private boolean running;

        public DisagreeableSnapshotter(String alias) {
            this.alias = alias;
        }

        public void run() {

            running = true;
            int snapshotCount = 0;
            while (!cancelled) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOG.error("Awoken", e);
                }
                try {
                    SnapshotIF s = ProxoolFacade.getSnapshot(alias, true);
                    int c1 = s.getActiveConnectionCount();
                    int c2 = getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_ACTIVE);
                    int v1 = s.getAvailableConnectionCount();
                    int v2 = getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_AVAILABLE);
                    int o1 = s.getOfflineConnectionCount();
                    int o2 = getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_OFFLINE);
                    if (c1 != c2 || v1 != v2 || o1 != o2) {
                        LOG.error("Disparity noticed. Active: " + c1 + (c1 == c2 ? " == " : " != ") + c2
                            + ", available: " + v1 + (v1 == v2 ? " == " : " != ") + v2
                            + ", offline: " + o1 + (o1 == o2 ? " == " : " != ") + o2);
                        disparityNoticed = true;
                    }
                    snapshotCount++;
                } catch (ProxoolException e) {
                    LOG.error("Couldn't get snapshot", e);
                }
            }
            LOG.info(snapshotCount + " snapshots taken");
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

        private int getCount(ConnectionInfoIF[] connectionInfos, int status) {
            int count = 0;
            for (int i = 0; i < connectionInfos.length; i++) {
                if (connectionInfos[i].getStatus() == status) {
                    count++;
                }
            }
            return count;
        }

        public boolean isDisparityNoticed() {
            return disparityNoticed;
        }

        public void cancel() {
            cancelled = true;
        }

    }

    class AnnoyingConnector implements Runnable {

        private String alias;

        private boolean cancelled;

        private int exceptionCount;

        private boolean running = false;

        public AnnoyingConnector(String alias) {
            this.alias = alias;
        }

        public void run() {
            running = true;
                while (!cancelled) {
                    try {
                        Connection connection = null;
                        Statement s = null;
                        try {
                            connection = DriverManager.getConnection(TestHelper.buildProxoolUrl(alias));
                            s = connection.createStatement();
                            Thread.yield();
                        } finally {
                            if (s != null) {
                                s.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                        }
                    } catch (SQLException e) {
                        LOG.error(Thread.currentThread().getName(), e);
                        exceptionCount++;
                    }
                }
            running = false;
        }

        public boolean isRunning() {
            return running;
        }

        public void cancel() {
            cancelled = true;
        }

        public int getExceptionCount() {
            return exceptionCount;
        }

    }

}

/*
 Revision history:
 $Log: PerformanceTest.java,v $
 Revision 1.18  2006/01/18 14:40:06  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.17  2003/11/04 13:54:02  billhorsman
 checkstyle

 Revision 1.16  2003/03/11 14:58:32  billhorsman
 put PerformanceTest back in the global test

 Revision 1.15  2003/03/11 14:51:43  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.14  2003/03/10 23:49:04  billhorsman
 new test to measure the impact of taking snapshots

 Revision 1.13  2003/03/10 15:31:26  billhorsman
 fixes

 Revision 1.12  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.11  2003/03/03 17:08:57  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.10  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.9  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.8  2003/02/19 15:14:23  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.7  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.6  2002/12/16 17:05:05  billhorsman
 new test structure

 Revision 1.5  2002/11/09 16:01:53  billhorsman
 fix doc

 Revision 1.4  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.3  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.2  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.1  2002/10/30 21:17:51  billhorsman
 new performance tests

*/
/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.pool.performance;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Multi-thread performance test
 * 
 * @author Dirk Verbeeck
 * @version $Revision: 155430 $ $Date: 2005-02-26 08:13:28 -0500 (Sat, 26 Feb 2005) $ 
 */
public class PerformanceTest {
    private int logLevel = 0;
    private int nrIterations = 5;
    private int nrThreads = 100;

    private GenericObjectPool pool;
    private boolean start = false;
    private volatile int waiting = 0;
    private volatile int complete = 0;
    private volatile long totalBorrowTime = 0;
    private volatile long totalReturnTime = 0;
    private volatile int nrSamples = 0; 

    public void setLogLevel(int i) {
        logLevel = i;
    }
    
    private void init() {
        start = false;
        waiting = 0;
        complete = 0;
        totalBorrowTime = 0;
        totalReturnTime = 0;
        nrSamples = 0;     
    }

    class MyThread implements Runnable {
        long borrowTime;
        long returnTime;

        public void runOnce() {
            try {
                waiting++;
                if (logLevel >= 5) {
                    String name = "thread" + Thread.currentThread().getName();
                    System.out.println(name + "   waiting: " + waiting + "   complete: " + complete);
                }
                long bbegin = System.currentTimeMillis();
                Object o = pool.borrowObject();
                long bend = System.currentTimeMillis();
                waiting--;
                do {
                    Thread.yield();
                }
                while (!start);

                if (logLevel >= 3) {
                    String name = "thread" + Thread.currentThread().getName();
                    System.out.println(name + "    waiting: " + waiting + "   complete: " + complete);
                }
                                 
                long rbegin = System.currentTimeMillis();
                pool.returnObject(o);
                long rend = System.currentTimeMillis();
                Thread.yield();
                complete++;
                borrowTime = (bend-bbegin);
                returnTime = (rend-rbegin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            runOnce(); // warmup
            for (int i = 0; i<nrIterations; i++) {
                runOnce();
                totalBorrowTime += borrowTime;
                totalReturnTime += returnTime;
                nrSamples++;
                if (logLevel >= 2) {
                    String name = "thread" + Thread.currentThread().getName();
                    System.out.println(
                        "result " + nrSamples + "\t" + name 
                        + "\t" + "borrow time: " + borrowTime + "\t" + "return time: " + returnTime
                        + "\t" + "waiting: " + waiting + "\t" + "complete: " + complete);
                }
            }
        }
    }

    private void run(int nrIterations, int nrThreads, int maxActive, int maxIdle) {
        this.nrIterations = nrIterations;
        this.nrThreads = nrThreads;
        init();
        
        SleepingObjectFactory factory = new SleepingObjectFactory();
        if (logLevel >= 4) { factory.setDebug(true); } 
        pool = new GenericObjectPool(factory);
        pool.setMaxActive(maxActive);
        pool.setMaxIdle(maxIdle);
        pool.setTestOnBorrow(true);

        Thread[] threads = new Thread[nrThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i]= new Thread(new MyThread(), Integer.toString(i));
            Thread.yield();
        }
        if (logLevel >= 1) { System.out.println("created"); } 
        Thread.yield();

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
            Thread.yield();
        }
        if (logLevel >= 1) { System.out.println("started"); }
        Thread.yield();

        start = true;
        if (logLevel >= 1) { System.out.println("go"); }
        Thread.yield();

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (logLevel >= 1) { System.out.println("finish"); }
        System.out.println("-----------------------------------------");
        System.out.println("nrIterations: " + nrIterations);
        System.out.println("nrThreads: " + nrThreads);
        System.out.println("maxActive: " + maxActive);
        System.out.println("maxIdle: " + maxIdle);
        System.out.println("nrSamples: " + nrSamples);
        System.out.println("totalBorrowTime: " + totalBorrowTime);
        System.out.println("totalReturnTime: " + totalReturnTime);
        System.out.println("avg BorrowTime: " + totalBorrowTime/nrSamples);
        System.out.println("avg ReturnTime: " + totalReturnTime/nrSamples);
    }

    public static void main(String[] args) {
        PerformanceTest test = new PerformanceTest();
        test.setLogLevel(0);
        System.out.println("Increase threads");
        test.run(1,  50,  5,  5);
        test.run(1, 100,  5,  5);
        test.run(1, 200,  5,  5);
        test.run(1, 400,  5,  5);

        System.out.println("Increase threads & poolsize");
        test.run(1,  50,  5,  5);
        test.run(1, 100, 10, 10);
        test.run(1, 200, 20, 20);
        test.run(1, 400, 40, 40);

        System.out.println("Increase maxIdle");
        test.run(1, 400, 40,  5);
        test.run(1, 400, 40, 40);


//      System.out.println("Show creation/destruction of objects");
//      test.setLogLevel(4);
//      test.run(1, 400, 40,  5);
    }

}
