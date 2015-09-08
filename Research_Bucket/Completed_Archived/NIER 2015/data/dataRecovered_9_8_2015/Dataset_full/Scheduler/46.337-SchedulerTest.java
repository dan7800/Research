package net.sf.test.unit;

import net.sf.jukebox.service.ActiveService;
import net.sf.jukebox.scheduler.RecurringTaskDescriptor;
import net.sf.jukebox.scheduler.Scheduler;
import net.sf.jukebox.scheduler.Task;
import net.sf.jukebox.scheduler.TaskDescriptor;

/**
 * Test case for classes in {@code net.sf.jukebox.scheduler} package.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2005
 * @version $Id: SchedulerTest.java,v 1.2 2007-06-14 04:32:23 vtt Exp $
 */
public class SchedulerTest extends ActiveService {

    /**
     * Scheduler to test.
     */
    private Scheduler scheduler = new Scheduler();

    /**
     * Task to test the {@link #scheduler scheduler} with.
     */
    private Task variable;

    /**
     * Default constructor.
     */
    public SchedulerTest() {

        super(null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startup() throws Throwable {

        scheduler.start().waitFor();

        long startAt = System.currentTimeMillis();

        TaskDescriptor td = new RecurringTaskDescriptor(startAt + 1000, 0xFF);
        variable = new Command(td, "NOW");
        scheduler.schedule(variable);

        startAt += 30 * 1000;

        td = new TaskDescriptor(startAt);
        Task t = new Command(td, "+30 seconds/A");
        scheduler.schedule(t);

        t = new Command(td, "+30 seconds/B");
        scheduler.schedule(t);

        startAt += 5 * 1000;
        td = new TaskDescriptor(startAt);
        t = new Command(td, "+35 seconds");
        scheduler.schedule(t);

        startAt += 70 * 1000;
        td = new TaskDescriptor(startAt);
        t = new Command(td, "+140 seconds");
        scheduler.schedule(t);

        td = new TaskDescriptor(System.currentTimeMillis() - 5000);
        t = new Command(td, "LATE");
        scheduler.schedule(t, true);

        td = new RecurringTaskDescriptor(System.currentTimeMillis() - 5000, 0xFF);
        t = new Command(td, "LATE RECURRING");
        scheduler.schedule(t, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Throwable {

        Thread.sleep(1000 * 5);

        variable.getDescriptor().setStartTime(System.currentTimeMillis() + 1000);
        scheduler.reschedule();

        Thread.sleep(1000 * 200);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdown() throws Throwable {

        scheduler.stop().waitFor();

        logger.info("Finished");
    }

    /**
     * VT: FIXME: Need to remember what did I create this for.
     */
    protected class Command extends Task {

        /**
         * Create an instance.
         *
         * @param td Task descriptor.
         * @param message Command message.
         */
        Command(TaskDescriptor td, String message) {

            super(td, message);
        }

        /**
         * Complain about the command.
         */
        public void run() {

            logger.info("Command: " + getTarget());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {

            return getDescriptor().toString() + "(" + getTarget() + ")";
        }
    }
}
