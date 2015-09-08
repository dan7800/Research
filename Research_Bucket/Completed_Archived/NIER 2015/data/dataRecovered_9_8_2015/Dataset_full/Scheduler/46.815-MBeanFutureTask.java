/* $Id: MBeanFutureTask.java,v 1.1 2005/12/14 18:08:07 dbernstein Exp $
 *
 * (Created on Dec 12, 2005
 *
 * Copyright (C) 2005 Internet Archive.
 *  
 * This file is part of the Heritrix Cluster Controller (crawler.archive.org).
 *  
 * HCC is free software; you can redistribute it and/or modify
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
package org.archive.hcc.util.jmx;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

public class MBeanFutureTask implements
        NotificationListener,
        NotificationFilter,
        Future,
        Serializable {
    
    private static Logger log = Logger.getLogger(MBeanFutureTask.class.getName());

    private CountDownLatch latch;

    private Notification notification = null;

    private String name;

    private Timer timer;

    private boolean done = false;

    public MBeanFutureTask(String name) {
        this.latch = new CountDownLatch(1);
        this.name = name;
    }

    public boolean isNotificationEnabled(Notification notification) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return done;
    }

    public void handleNotification(Notification notification, Object handback) {
        this.notification = notification;
        this.latch.countDown();
        if (log.isLoggable(Level.FINE)) {
            log.fine("this.name=" + name + "; thread.name="
                    + Thread.currentThread().getName() + "; latch.count="
                    + latch.getCount() + "; notification.type="
                    + notification.getType());
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public Object get() throws InterruptedException, ExecutionException {
        try {
            await(-1, TimeUnit.MILLISECONDS);
            return getData();
        } catch (TimeoutException ex) {
            throw new RuntimeException("This should never happen!");
        }
    }

    private Object getData() throws ExecutionException {
        return (this.notification.getUserData());
    }

    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException,
            ExecutionException,
            TimeoutException {
        await(timeout, unit);
        return getData();
    }

    private boolean await(long timeout, TimeUnit unit)
            throws InterruptedException,
            TimeoutException {
        try {
            if (log.isLoggable(Level.FINE)) {
                log.fine("this.name=" + name + "; thread.name="
                        + Thread.currentThread().getName());
            }

            if (timer == null) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (log.isLoggable(Level.FINE)) {
                            log.fine(name + ": waiting for notification");
                        }
                    }

                }, 60 * 1000, 60 * 1000);
            }

            boolean executed = false;
            if (timeout == -1) {
                this.latch.await();
                executed = true;
            } else {
                executed = this.latch.await(timeout, TimeUnit.MILLISECONDS);
            }

            done = true;
            if (!executed) {
                throw new TimeoutException(
                        "Notification was not received within the timeout period ("
                                + timeout + ")");
            }

            return executed;
        } finally {
            timer.cancel();
        }
    }
}