package net.sf.jukebox.scheduler;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jukebox.service.ActiveService;
import net.sf.jukebox.util.Interval;

/**
 * A scheduler.
 * <p>
 * Keeps track of the {@link Task scheduled events} and executes them when the
 * time is due. Also, allows the future events to influence the present.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim
 * Tkachenko</a> 2001
 * @version $Id: Scheduler.java,v 1.2 2007-06-14 04:32:16 vtt Exp $
 */
public class Scheduler extends ActiveService {

    /**
     * Set of tasks retrieved from the configuration or {@link #schedule(Task)
     * scheduled}.
     */
    private Set<Task> taskSet = new TreeSet<Task>();

    /**
     * Task queue. The tasks that have been resolved and scheduled are put here.
     */
    private SortedSet<Task> taskQueue = new TreeSet<Task>();

    /**
     * Listener set.
     */
    private Set<TaskListener> listenerSet = new HashSet<TaskListener>();

    /**
     * Default constructor.
     */
    public Scheduler() {

    }

    /**
     * Add the task listener.
     *
     * @param tl Task listener to add.
     */
    public void addListener(TaskListener tl) {

        listenerSet.add(tl);
    }

    /**
     * Remove the task listener.
     *
     * @param tl Task listener to remove.
     */
    public void removeListener(TaskListener tl) {

        listenerSet.remove(tl);
    }

    /**
     * Schedule a task.
     * <p>
     * If the task start time is in the past, it is not executed.
     *
     * @param task Task to schedule.
     * @exception IllegalArgumentException if the task is already present.
     */
    public void schedule(Task task) {

        schedule(task, false);
    }

    /**
     * Schedule a task.
     * <p>
     * The task is executed immediately if the start time is in the past and the
     * <code>runIfLate</code> flag is set.
     *
     * @param task Task to schedule.
     * @param runIfLate Whether to run the task immediately if the start time is
     * in the past.
     * @exception IllegalArgumentException if the task is already present.
     */
    public synchronized void schedule(Task task, boolean runIfLate) {

        if (taskSet.contains(task)) {

            throw new IllegalArgumentException("The task has already been scheduled: " + task);
        }

        if (task.getDescriptor().isLate()) {

            if (runIfLate) {

                logger.debug("Task is late, requested to be run: " + task);
                runTask(task);
            }

            if (!(task.getDescriptor() instanceof RecurringTaskDescriptor)) {

                // It doesn't make sense to schedule the task that is late
                // and is not recurring

                logger.info("Task is late and is not recurring - not scheduled: " + task);
                return;
            }
        }

        taskSet.add(task);
        taskQueue.add(task);

        taskScheduled(task);

        reschedule(false);
    }

    /**
     * Remove the task descriptor from the schedule.
     *
     * @param td Descriptor to remove.
     */
    public void remove(TaskDescriptor td) {

        for (Iterator<Task> i = taskSet.iterator(); i.hasNext();) {

            Task t = i.next();

            if (t.getDescriptor().equals(td)) {

                i.remove();
                reschedule();

                taskRemoved(t);
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startup() throws Throwable {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void execute() throws Throwable {

        while (isEnabled()) {

            try {

                Task current = taskQueue.first();

                long start = current.getDescriptor().getStartTime();
                long now = System.currentTimeMillis();

                if (now > start) {

                    logger.debug("Missed the task start time by "
                            + Interval.toTimeInterval(now - start) + ": " + current);
                }

                // VT: FIXME: If the next tasks' time is in the past as
                // well, we skip this one

                // Now determine how long do we have to wait

                long wait = start - now;

                if (wait <= 0) {

                    logger.debug("Running the task: " + Integer.toHexString(current.hashCode()));
                    taskQueue.remove(current);
                    runTask(current);

                    if (current.getDescriptor() instanceof RecurringTaskDescriptor) {

                        ((RecurringTaskDescriptor) current.getDescriptor()).sync();

                        taskSet.remove(current);
                        schedule(current);

                        logger.info("Recurring task will be run again at "
                                + new Date(current.getDescriptor().getStartTime()));
                    }

                } else {

                    logger.debug("Have to wait " + Interval.toTimeInterval(wait)
                            + " for the next task");

                    // VT: NOTE: Let's cheat a little and make the interval a
                    // little
                    // bit shorter if it is over a minute long. It's not going
                    // to
                    // cause a significant degradation, but will greatly improve
                    // precision.

                    if (wait > 1000 * 60) {

                        wait = (int) (wait * 0.9);
                        logger.debug("Cheating, wait modified to " + Interval.toTimeInterval(wait));
                    }

                    // logger.debug(CH_SCHEDULER, "waiting " + wait +
                    // "ms");
                    wait(wait);
                    // logger.debug(CH_SCHEDULER, "done waiting");

                    // At this point, it's quite possible that we haven't indeed
                    // waited long enough for the interval to expire, but in
                    // fact
                    // someone has scheduled a new task, and this has caused a
                    // notification. Or, the interval was shorter than required
                    // ;)
                }

            } catch (NoSuchElementException ignored) {

                logger.info("Queue exhausted");
                wait();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdown() throws Throwable {

        while (!taskQueue.isEmpty()) {

            Object t = taskQueue.first();

            logger.info("Scheduled task left: " + t);
            taskQueue.remove(t);
        }

        logger.info("shut down");
    }

    /**
     * Run the task.
     *
     * @param task Task to run.
     */
    private void runTask(Task task) {

        TaskRunner tr = new TaskRunner(task);

        Thread t = new Thread(tr);

        t.start();

        taskExecuted(task);
    }

    /**
     * Resynchronize the schedule. Usually, this method would be invoked after
     * some tasks have been changed.
     */
    public synchronized void reschedule() {

        reschedule(true);
    }

    /**
     * Resynchronize the schedule.
     *
     * @param rehash True if tasks have to be reordered. Value of this parameter
     * may be ignored if it is internally determined that tasks need to be
     * reordered.
     */
    private synchronized void reschedule(boolean rehash) {

        if (!rehash) {

            // First of all, let's figure out if we really have to reorder
            // things in the task queue.

            if (taskSet.size() != taskQueue.size()) {

                rehash = true;

            } else {

                // Since the task set is sorted, and the task queue is ordered,
                // ideally the order should be the same.

                Iterator<Task> is = taskSet.iterator();
                Iterator<Task> iq = taskQueue.iterator();

                while (is.hasNext()) {

                    Object s = is.next();
                    Object q = iq.next();

                    if (s != q) {

                        rehash = true;
                        break;
                    }
                }
            }
        }

        if (!rehash) {

            logger.debug("reschedule: no need to reorder tasks");

            notifyAll();
            return;
        }

        logger.debug("Reordering tasks");

        taskQueue.clear();

        for (Iterator<Task> i = taskSet.iterator(); i.hasNext();) {

            Task task = i.next();

            try {

                TaskDescriptor td = task.getDescriptor();

                if (td instanceof RecurringTaskDescriptor) {

                    ((RecurringTaskDescriptor) td).sync();
                }

                if (td.isLate()) {

                    if (td instanceof RecurringTaskDescriptor) {

                        // Do nothing, it's just been sync()ed

                    } else {

                        logger.info("expired: " + task);
                        i.remove();
                    }
                }

                taskQueue.add(task);

                if (false)
                    logger.debug("reschedule: task " + task + " to go at "
                            + new Date(task.getDescriptor().getStartTime()));

            } catch (IllegalStateException ignored) {

                // Filter the task that is in the past by now

                logger.info("reschedule: task expired: " + Integer.toHexString(task.hashCode()));
            }
        }

        if (true) {

            for (Iterator<Task> i = taskQueue.iterator(); i.hasNext();) {

                Task task = i.next();

                logger.debug("reschedule: task " + task + " to go at "
                        + new Date(task.getDescriptor().getStartTime()));
            }

        }

        notifyAll();
    }

    /**
     * Runs the task in a different thread.
     */
    protected class TaskRunner implements Runnable {

        /**
         * Task to run.
         */
        Task task;

        /**
         * Create an instance.
         *
         * @param task to run.
         * @exception IllegalArgumentException if the {@code task} is null.
         */
        TaskRunner(Task task) {

            if (task == null) {
                throw new IllegalArgumentException("task can't be null");
            }

            this.task = task;
        }

        /**
         * Run the {@link #task task}.
         */
        public void run() {

            logger.info("Running task: " + task);

            try {

                task.run();

            } catch (Throwable t) {

                logger.error("Couldn't run task " + Integer.toHexString(task.hashCode())
                        + ", cause:", t);
            }
        }
    }

    /**
     * Tell the listeners that the task had been scheduled.
     *
     * @param t Task to tell the listeners about.
     */
    private void taskScheduled(Task t) {

        for (Iterator<TaskListener> i = listenerSet.iterator(); i.hasNext();) {

            i.next().taskScheduled(t);
        }
    }

    /**
     * Tell the listeners that the task had been executed.
     *
     * @param t Task to tell the listeners about.
     */
    private void taskExecuted(Task t) {

        for (Iterator<TaskListener> i = listenerSet.iterator(); i.hasNext();) {

            i.next().taskExecuted(t);
        }
    }

    /**
     * Tell the listeners that the task had been removed.
     *
     * @param t Task to tell the listeners about.
     */
    private void taskRemoved(Task t) {

        for (Iterator<TaskListener> i = listenerSet.iterator(); i.hasNext();) {

            i.next().taskRemoved(t);
        }
    }
}
