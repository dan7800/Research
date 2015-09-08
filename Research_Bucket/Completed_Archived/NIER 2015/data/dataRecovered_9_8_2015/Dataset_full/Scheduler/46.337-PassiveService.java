package net.sf.jukebox.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sf.jukebox.jmx.JmxAttribute;
import net.sf.jukebox.jmx.JmxAware;
import net.sf.jukebox.jmx.JmxDescriptor;
import net.sf.jukebox.logger.LogAware;
import net.sf.jukebox.sem.EventSemaphore;
import net.sf.jukebox.util.Interval;

/**
 * Describes the concept of a passive service. The passive service is the one
 * that starts (observing some preconditions at startup), passively serves some
 * requests, and then shuts down (observing some post-conditions at shutdown).
 * <hr>
 * Formerly known as {@code tt.server.GenericService}>. <br>
 * Significant changes:
 * <ul>
 * <li>{@code RunnableService}> interface is gone as it used to be, as too
 * complicated.
 * <li>{@code execute()}> is gone. It turned out that a lot of services are
 * passive (have active {@link #startup startup()} and {@link #shutdown
 * shutdown()} and just sleep in between), or re-active, so the {@link
 * ActiveService ActiveService} now implements the pro-active behavior.
 * <li>Implementation is rewritten from scratch.
 * </ul>
 * <h3>Reusability note</h3>
 * Upon careful examination, it turnes out that this class is actually a good
 * example of 'design by contract', which among other things uses language
 * constructs like <b>require</b> and <b>ensure</b>. In any subclasses, please
 * pay close attention to the {@link #startup startup()} and
 * {@link #shutdown shutdown()} methods, which are actually the pre-condition
 * and post-condition checkers. And, a good reading about this is the article
 * about <a
 * href="http://archive.eiffel.com/doc/manuals/technology/contract/ariane/page.html"
 * target="_top">$500 million mistake</a>.
 * <h3>Refactoring note (Oct 2005)</h3>
 * Turned out that the {@code shutdown()} never needed the concept of the
 * 'shutdown cause' in reality, it was all wishful thinking. It is still
 * possible to get to the shutdown cause should it ever be required, so the
 * framework is left in place, inactive.
 * <p/>
 * Same fate applied to the concept of dependant services - it was only
 * applicable in to a specific task within American Express and was never used
 * beyond that.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2007
 * @version $Id: PassiveService.java,v 1.2 2007-06-14 04:32:20 vtt Exp $
 * @see ActiveService
 */
public abstract class PassiveService extends LogAware implements Service, PassiveServiceMXBean, JmxAware {

  /**
   * The thread factory.
   */
  private ThreadFactory tf;

  /**
   * Time when successful startup was completed. {@code -1} if it never happened.
   */
  private long startedAt = -1L;

  /**
   * Execute the startup sequence.
   *
   * @throws InterruptedException if this thread was interrupted.
   * @throws Throwable to indicate the startup failure. Possibly propagated from implementation.
   */
  protected abstract void startup() throws Throwable;

  /**
   * Execute the shutdown sequence.
   *
   * @throws InterruptedException if this thread was interrupted.
   * @throws Throwable to indicate the shutdown failure. Possibly propagated from implementation.
   */
  protected abstract void shutdown() throws Throwable;

  /**
   * Clean up. It is safe to call {@link #shutdown shutdown()} from the finalizer thread, so this is a default behavior.
   *
   * @throws Throwable JDK 1.2 requirement, and there's not much I can do
   * here anyway.
   */
  @Override
  protected void finalize() throws Throwable {

    super.finalize();

    if (enabled) {

      // This shouldn't have happened, but since it did, the best thing to do
      // would be to report the context.

      logger.fatal("finalizing enabled???", new IllegalStateException("This should not have happened"));

      try {

        shutdown();

      } catch (Throwable t) {

        logger.error("Uncaught exception on shutdown():", t);
      }
    }
  }

  /**
   * ThreadGroup to belong to. Needed because otherwise I'll get a
   * {@code SecurityException} trying to spawn the thread with a different
   * name from a thread with security restrictions on it (for example, AWT
   * threads).
   */
  protected ThreadGroup tGroup = null;

  /**
   * User object, if any.
   *
   * @see #getUserObject
   * @see #setUserObject
   */
  private Object userObject = null;

  /**
   * True if the service is enabled. Becomes true in the {@code start()}>,
   * false again in {@code stop()}> or upon completion.
   *
   * @see #isEnabled
   * @see #ready
   * @see #isReady
   * @see #active
   * @see #isActive
   */
  protected boolean enabled;

  /**
   * True if the service is ready. Becomes true if {@code startup()}> is
   * successfully completed, false again in {@code stop()}> or upon
   * completion.
   *
   * @see #isReady
   * @see #enabled
   * @see #isEnabled
   * @see #active
   * @see #isActive
   */
  protected boolean ready;

  /**
   * True if the service is active. Becomes true in the {@code start()}>,
   * false again in {@code stop()}> or upon completion.
   *
   * @see #isActive
   * @see #enabled
   * @see #isEnabled
   * @see #ready
   * @see #isReady
   */
  protected boolean active;

  /**
   * The core thread.
   */
  protected Thread core = null;

  /**
   * "Service has just been started" semaphore. This allows other services to
   * monitor this service and to take appropriate actions.
   * <p/>
   * Always posted.
   *
   * @see #semUp
   * @see #semStopped
   * @see #semDown
   */
  private EventSemaphore semStarted = new EventSemaphore(null, "started");

  /**
   * @return The semaphore that is triggered when the service has started.
   */
  public EventSemaphore getSemStarted() {
    return semStarted;
  }

  /**
   * "Service startup has completed" semaphore. This allows other services to
   * monitor this service and to take appropriate actions.
   * <ul>
   * <li>Posted if the service starts successfully;
   * <li>Cleared if the startup has failed.
   * </ul>
   *
   * @see #semStarted
   * @see #semStopped
   * @see #semDown
   */
  protected EventSemaphore semUp = new EventSemaphore(null, "up");

  /**
   * Get the {@link #semUp semaphore} that is triggered upon the {@link
   * #startup startup()} completion.
   * <p/>
   * Ideally, the semaphore returned by this method should be an immutable
   * clone of the actual semaphore.
   *
   * @return The {@link #semUp semUp} semaphore.
   */
  public EventSemaphore getSemUp() {
    return semUp;
  }

  /**
   * "Service has just been stopped" semaphore. This allows other services to
   * monitor this service and to take appropriate actions.
   * <ul>
   * <li>Posted if the service just completed the execution;
   * <li>Cleared if it had been stopped.
   * </ul>
   *
   * @see #semStarted
   * @see #semUp
   * @see #semDown
   */
  protected EventSemaphore semStopped = new EventSemaphore(null, "stopped");

  /**
   * @return The semaphore that gets triggered when the service is being stopped.
   * <p/>
   * Ideally, the semaphore returned by this method should be an immutable clone of the actual semaphore.
   *
   * @see #semStopped
   */
  public EventSemaphore getSemStopped() {
    return semStopped;
  }

  /**
   * "Service has shut down" semaphore. This allows other services to monitor
   * this service and to take appropriate actions.
   * <ul>
   * <li>Posted if the shutdown is considered successful;
   * <li>Cleared otherwise.
   * </ul>
   *
   * @see #semStarted
   * @see #semUp
   * @see #semStopped
   */
  protected EventSemaphore semDown = new EventSemaphore(null, "down");

  /**
   * Get the semaphore that gets triggered upon {@link #shutdown shutdown()}* completion.
   * <p/>
   * Ideally, the semaphore returned by this method should be an immutable clone of the actual semaphore.
   *
   * @return The semaphore that gets triggered when the service has shut down.
   *
   * @see #semStopped
   */
  public EventSemaphore getSemDown() {
    return semDown;
  }

  /**
   * A default constructor.
   * <p/>
   * This will create an object with no logger attached, default thread group
   * and default thread factory.
   */
  protected PassiveService() {
    this(Thread.currentThread().getThreadGroup(), null);
  }

  /**
   * Create a new instance, running in a designated thread group.
   *
   * @param tGroup Thread group to belong to.
   * @param tf Thread factory to use.
   */
  protected PassiveService(ThreadGroup tGroup, ThreadFactory tf) {

    this.tGroup = tGroup;
    this.tf = tf;

    if (this.tf == null) {

      // Well, we can't possibly exist without a way to get a thread
      // to execute the startup and shutdown

      //this.tf = Executors.privilegedThreadFactory();
      this.tf = Executors.defaultThreadFactory();
    }
  }

  /**
   * Create a new instance of a wrapper, running in a default thread group.
   *
   * @param tf Thread factory to use.
   */
  protected PassiveService(ThreadFactory tf) {

    // This is done to avoid the possible complications when running in
    // the alien environment, like AWT. Used to give a lot of trouble.
    this(Thread.currentThread().getThreadGroup(), tf);
  }

  /**
   * @return The thread factory being used.
   */
  public final ThreadFactory getThreadFactory() {
    return tf;
  }

  /**
   * Set the user object. This may be used to store the startup arguments
   * and/or execution results.
   *
   * @param userObject Arguments to set.
   */
  public void setUserObject(Object userObject) {

    // FIXME: maybe broadcast the change notification?
    this.userObject = userObject;
  }

  /**
   * Get the user object.
   *
   * @return The user object.
   */
  public Object getUserObject() {
    return userObject;
  }

  /**
   * Tells if the service is enabled. Service is enabled after
   * {@link #start start()} method is called and until {@link #stop stop()}
   * method is called.
   *
   * @return true if the service is enabled, false otherwise
   *
   * @see #enabled
   */
  @JmxAttribute(description = "True from the moment the service is started until the moment the service is stopped")
  public final boolean isEnabled() {
    return enabled;
  }

  /**
   * Tells if the service is ready. Service is ready when all startup actions
   * have been completed and until {@link #stop stop()} method is called.
   *
   * @return true if the service is ready, false otherwise
   *
   * @see #ready
   */
  @JmxAttribute(description = "True from the moment the service is up until the moment the service is stopped")
  public final boolean isReady() {

    return ready;
  }

  /**
   * Tells if the service is active. Service is active since
   * {@link #startup startup()} invocation and until
   * {@link #shutdown shutdown()} (after {@link #stop stop()} invocation) is
   * completed.
   *
   * @return true if the service is active, false otherwise
   *
   * @see #active
   */
  @JmxAttribute(description = "True from the moment the service is started until the moment the service is down")
  public final boolean isActive() {
    return active;
  }

  @JmxAttribute(description = "Time in milliseconds since the service was started")
  public long getUptimeMillis() {
    return startedAt == -1 ? startedAt : System.currentTimeMillis() - startedAt;
  }

  @JmxAttribute(description = "Time since the service was started, as a human readable string")
  public String getUptime() {
    return startedAt == -1 ? "down" : Interval.toTimeInterval(getUptimeMillis());
  }

  /**
   * {@inheritDoc}
   */
  public JmxDescriptor getJmxDescriptor() {
    return new JmxDescriptor("jukebox", getClass().getSimpleName(), Integer.toHexString(hashCode()), "FIXME");
  }

  /**
   * Class that supports the method redirection for the wrappers.
   *
   * @see PassiveService.StartupWrapper
   * @see PassiveService.ShutdownWrapper
   * @see PassiveService.PassiveWrapper
   */
  protected abstract class MethodWrapper {

    /**
     * Call target.
     */
    Object target;

    /**
     * Create the method wrapper based on the target object.
     *
     * @param target Object to perform the operation on.
     */
    protected MethodWrapper(Object target) {
      this.target = target;
    }

    /**
     * Call the proper method. The exact method to call is determined by
     * derived (usually anonymous) classes.
     *
     * @throws InterruptedException if the thread was interrupted.
     * @throws Throwable propagates from the called method.
     */
    abstract void call() throws Throwable;

    /**
     * Set the proper values for the host's {@link PassiveService#enabled enabled},
     * {@link PassiveService#active active} and {@link PassiveService#ready ready} flags.
     *
     * @param status {@code call()}> result is passed here.
     */
    abstract void setFlags(boolean status);
  }

  /**
   * Method wrapper for {@code startup()}> call. It will be reused in
   * {@code ActiveService}> class.
   */
  protected class StartupWrapper extends MethodWrapper {

    /**
     * Create an instance.
     *
     * @param target Object to control.
     */
    protected StartupWrapper(Object target) {

      super(target);
    }

    /**
     * Execute the startup sequence for the target service.
     * <ol>
     * <li>Call the target's {@link PassiveService#startup() startup()} method.
     * <li>If it returns {@code true}> and the target service implements
     * the {@link IdleClient IdleClient} interface, {@link Idle#register
     * register it}. <br>
     * Thoughts to myself: not every service needs to be declared idle-able,
     * and this is an overhead, so it might be a good idea to pass the
     * instances to the background thread and let it handle the idle
     * behavior. The priority of that thread may be minimal, if the
     * requirements to the strict idle timeouts could be relaxed.
     * </ol>
     *
     * @throws InterruptedException if the wait is interrupted.
     * @throws Throwable if anything goes wrong.
     */
    @Override
    protected void call() throws Throwable {

      // logger.debug("target: "+target.getClass().getName());

      ((PassiveService) target).startup();

      // If the startup() has thrown an exception, it doesn't make
      // sense to register the client as idle anyway, because it
      // hasn't started up properly.

      if (target instanceof IdleClient) {

        Idle.register((IdleClient) target);
      }
    }

    /**
     * Set the {@link PassiveService#ready ready}, {@link PassiveService#enabled enabled} and
     * {@link PassiveService#active active} flags.
     *
     * @param status Status to set the flags based upon.
     */
    @Override
    protected void setFlags(boolean status) {

      if (status) {

        ready = true;
        startedAt = System.currentTimeMillis();

      } else {

        enabled = false;
        ready = false;
        active = false;
        startedAt = -1;
      }
    }
  }

  /**
   * Method wrapper for {@code shutdown()}> call. It will be reused in
   * {@code ActiveService}> class.
   */
  protected class ShutdownWrapper extends MethodWrapper {

    /*
    * private Throwable failureCause; protected ShutdownWrapper(Object
    * target, Throwable failureCause) { super(target); // this.failureCause =
    * failureCause; }
    */
    /**
     * Create an instance.
     *
     * @param target Object to perform {@link PassiveService#shutdown() shutdown()} on.
     */
    protected ShutdownWrapper(Object target) {

      super(target);
    }

    /**
     * Execute the shutdown sequence for the target service.
     * <ol>
     * <li>Call the target's {@link PassiveService#shutdown()} method.
     * <li>If the target service implements the {@link IdleClient
     * IdleClient} interface, {@link Idle#unregister unregister it}.
     * </ol>
     *
     * @throws InterruptedException if this thread was interrupted.
     * @throws Throwable to indicate the failure. Possibly propagated
     * from implementation.
     */
    @Override
    protected void call() throws Throwable {

      Throwable cause = null;

      try {

        ((PassiveService) target).shutdown();

      } catch (Throwable t) {

        cause = t;
      }

      if (target instanceof IdleClient) {

        Idle.unregister((IdleClient) target);
      }

      if (cause != null) {

        // Rethrow it so the upper levels know what's going on

        throw cause;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setFlags(boolean status) {

      active = false;
      startedAt = -1;
    }
  }

  /**
   * Start the service. Service is started asynchronously, posting
   * {@code semStarted}> before calling the target service's
   * {@link #startup startup()}, and triggering the {@link #semUp semUp}
   * semaphore when the startup is complete.
   *
   * @return The semaphore which is triggered upon the target service startup
   *         completion. Posting that semaphore means success, clearing means failure.
   */
  public synchronized EventSemaphore start() {

    if (enabled) {

      throw new IllegalStateException("Already started");
    }

    // logger.debug(CH_SERVICE, "posting semStarted");
    semStarted.post();

    enabled = true;
    active = true;
    startCore();

    notifyAll();
    return semUp;
  }

  /**
   * Start the service. Actually, for this class it means spawn a thread which
   * executes the {@link #startup startup()}.
   */
  protected void startCore() {

    PassiveWrapper pw = new PassiveWrapper(new StartupWrapper(this), semUp);

    core = tf.newThread(pw);
    core.start();
  }

  /**
   * Stop the service. Service is stopped asynchronously, <b>clearing</b>
   * {@link #semStopped semStopped} before calling the target service
   * {@link #shutdown shutdown()}, and triggering the
   * {@link #semDown semDown} semaphore when the startup is complete.
   *
   * @return The semaphore which is triggered upon the target service shutdown
   *         completion. Posting that semaphore means success (clean shutdown),
   *         clearing means failure (unclean one).
   */
  public synchronized EventSemaphore stop() {

    if (!enabled) {
      throw new IllegalStateException("Already stopped");
    }

    semStopped.post();

    enabled = false;
    ready = false;
    stopCore();

    notifyAll();
    return semDown;
  }

  /**
   * Stop the service. Actually, for this class it means spawn a thread which
   * executes the {@link #shutdown shutdown()}.
   */
  protected void stopCore() {

    // VT: NOTE: Formerly, the ShutdownWrapper took a second argument - the
    // cause
    PassiveWrapper pw = new PassiveWrapper(new ShutdownWrapper(this), semDown);

    core = tf.newThread(pw);
    core.start();
  }

  /**
   * Wrapper for the startup, execution and shutdown threads.
   * <p/>
   * All of above share the common features:
   * <ol>
   * <li>Specific method on the target object should be called.
   * <li>Exceptions thrown by that method call should be caught and handled.
   * <li>Semaphores should be triggered.
   * </ol>
   * Thus, it's possible to have the common behavior here and implement the
   * differencies in behavior using other class, namely
   * {@link PassiveService.MethodWrapper MethodWrapper}.
   *
   * @see PassiveService.MethodWrapper
   */
  protected class PassiveWrapper implements Runnable {

    /**
     * Call target.
     */
    MethodWrapper target;

    /**
     * Semaphore to trigger with the method call result.
     */
    EventSemaphore sem;

    /**
     * Create a new wrapper.
     *
     * @param target Method wrapper providing the invocation target and the
     * proper method.
     * @param sem Event semaphore to trigger with the result of the method
     * call.
     *
     * @see PassiveService#tGroup
     */
    PassiveWrapper(MethodWrapper target, EventSemaphore sem) {

      this.target = target;
      this.sem = sem;
    }

    /**
     * Call the proper method, using the {@link PassiveService.MethodWrapper
     * MethodWrapper}, handle the exceptions and trigger the semaphore with
     * the proper value.
     */
    public// synchronized // BAD THING
    void run() {

      // logger.debug("RUNNING");
      wrap(target, sem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {

      logger.debug("finalize." + Integer.toHexString(hashCode()));
      super.finalize();
    }
  }

  /**
   * Wrap the method.
   *
   * @param target MethodWrapper to wrap.
   * @param sem Semaphore to trigger
   */
  protected void wrap(MethodWrapper target, EventSemaphore sem) {

    try {

      target.call();

      // logger.debug("call(): successful");

    } catch (InterruptedException iex) {

      logger.info("startup interrupted: ", iex);

      target.setFlags(false);
      sem.clear();
      core = null;
      // notifyAll();
      return;

    } catch (Throwable t) {

      logger.error("Uncaught exception: ", t);
      core = null;
      target.setFlags(false);
      sem.clear();
      // notifyAll();
      return;
    }

    core = null;
    target.setFlags(true);
    // logger.debug(CH_SERVICE, "trigger: " + sem.toString() + " " +
    // result);
    sem.post();
    // notifyAll(); // ???
  }

  /**
   * Check if the service is ready. This method is to be used in the methods
   * that have to be called only when the service has successfully started up
   * and before it is shut down.
   *
   * @throws IllegalStateException if the service {@link #isReady is not
   * ready}.
   */
  protected final void checkStatus() {

    if (!isReady()) {
      throw new IllegalStateException("The service has to be started before calling any methods");
    }
  }

  public ServiceStatus getStatus() {
    return new Status();
  }

  private class Status implements ServiceStatus {

    public boolean isActive() {
      return PassiveService.this.isActive();
    }

    public boolean isEnabled() {
      return PassiveService.this.isEnabled();
    }

    public boolean isReady() {
      return PassiveService.this.isReady();
    }

    public long getUptimeMillis() {
      return startedAt == -1 ? startedAt : System.currentTimeMillis() - startedAt;
    }

    public String getUptime() {
      return startedAt == -1 ? "down" : Interval.toTimeInterval(getUptimeMillis());
    }
  }
}package net.sf.jukebox.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sf.jukebox.jmx.JmxAttribute;
import net.sf.jukebox.jmx.JmxAware;
import net.sf.jukebox.jmx.JmxDescriptor;
import net.sf.jukebox.logger.LogAware;
import net.sf.jukebox.sem.EventSemaphore;
import net.sf.jukebox.util.Interval;

/**
 * Describes the concept of a passive service. The passive service is the one
 * that starts (observing some preconditions at startup), passively serves some
 * requests, and then shuts down (observing some post-conditions at shutdown).
 * <hr>
 * Formerly known as {@code tt.server.GenericService}>. <br>
 * Significant changes:
 * <ul>
 * <li>{@code RunnableService}> interface is gone as it used to be, as too
 * complicated.
 * <li>{@code execute()}> is gone. It turned out that a lot of services are
 * passive (have active {@link #startup startup()} and {@link #shutdown
 * shutdown()} and just sleep in between), or re-active, so the {@link
 * ActiveService ActiveService} now implements the pro-active behavior.
 * <li>Implementation is rewritten from scratch.
 * </ul>
 * <h3>Reusability note</h3>
 * Upon careful examination, it turnes out that this class is actually a good
 * example of 'design by contract', which among other things uses language
 * constructs like <b>require</b> and <b>ensure</b>. In any subclasses, please
 * pay close attention to the {@link #startup startup()} and
 * {@link #shutdown shutdown()} methods, which are actually the pre-condition
 * and post-condition checkers. And, a good reading about this is the article
 * about <a
 * href="http://archive.eiffel.com/doc/manuals/technology/contract/ariane/page.html"
 * target="_top">$500 million mistake</a>.
 * <h3>Refactoring note (Oct 2005)</h3>
 * Turned out that the {@code shutdown()} never needed the concept of the
 * 'shutdown cause' in reality, it was all wishful thinking. It is still
 * possible to get to the shutdown cause should it ever be required, so the
 * framework is left in place, inactive.
 * <p/>
 * Same fate applied to the concept of dependant services - it was only
 * applicable in to a specific task within American Express and was never used
 * beyond that.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2007
 * @version $Id: PassiveService.java,v 1.2 2007-06-14 04:32:20 vtt Exp $
 * @see ActiveService
 */
public abstract class PassiveService extends LogAware implements Service, PassiveServiceMXBean, JmxAware {

  /**
   * The thread factory.
   */
  private ThreadFactory tf;

  /**
   * Time when successful startup was completed. {@code -1} if it never happened.
   */
  private long startedAt = -1L;

  /**
   * Execute the startup sequence.
   *
   * @throws InterruptedException if this thread was interrupted.
   * @throws Throwable to indicate the startup failure. Possibly propagated from implementation.
   */
  protected abstract void startup() throws Throwable;

  /**
   * Execute the shutdown sequence.
   *
   * @throws InterruptedException if this thread was interrupted.
   * @throws Throwable to indicate the shutdown failure. Possibly propagated from implementation.
   */
  protected abstract void shutdown() throws Throwable;

  /**
   * Clean up. It is safe to call {@link #shutdown shutdown()} from the finalizer thread, so this is a default behavior.
   *
   * @throws Throwable JDK 1.2 requirement, and there's not much I can do
   * here anyway.
   */
  @Override
  protected void finalize() throws Throwable {

    super.finalize();

    if (enabled) {

      // VT: FIXME: What the hell? Throw IllegalStateException? This
      // shouldn't have happened

      logger.fatal("finalizing enabled???");

      try {

        // Of course there was no exception if the service is being
        // finalized and it is still enabled...

        // VT: FIXME: set the shutdownCause here
        shutdown();

      } catch (Throwable t) {

        logger.error("Uncaught exception on shutdown():", t);
      }
    }
  }

  /**
   * ThreadGroup to belong to. Needed because otherwise I'll get a
   * {@code SecurityException} trying to spawn the thread with a different
   * name from a thread with security restrictions on it (for example, AWT
   * threads).
   */
  protected ThreadGroup tGroup = null;

  /**
   * User object, if any.
   *
   * @see #getUserObject
   * @see #setUserObject
   */
  private Object userObject = null;

  /**
   * True if the service is enabled. Becomes true in the {@code start()}>,
   * false again in {@code stop()}> or upon completion.
   *
   * @see #isEnabled
   * @see #ready
   * @see #isReady
   * @see #active
   * @see #isActive
   */
  protected boolean enabled;

  /**
   * True if the service is ready. Becomes true if {@code startup()}> is
   * successfully completed, false again in {@code stop()}> or upon
   * completion.
   *
   * @see #isReady
   * @see #enabled
   * @see #isEnabled
   * @see #active
   * @see #isActive
   */
  protected boolean ready;

  /**
   * True if the service is active. Becomes true in the {@code start()}>,
   * false again in {@code stop()}> or upon completion.
   *
   * @see #isActive
   * @see #enabled
   * @see #isEnabled
   * @see #ready
   * @see #isReady
   */
  protected boolean active;

  /**
   * The core thread.
   */
  protected Thread core = null;

  /**
   * "Service has just been started" semaphore. This allows other services to
   * monitor this service and to take appropriate actions.
   * <p/>
   * Always posted.
   *
   * @see #semUp
   * @see #semStopped
   * @see #semDown
   */
  private EventSemaphore semStarted = new EventSemaphore(null, "started");

  /**
   * @return The semaphore that is triggered when the service has started.
   */
  public EventSemaphore getSemStarted() {
    return semStarted;
  }

  /**
   * "Service startup has completed" semaphore. This allows other services to
   * monitor this service and to take appropriate actions.
   * <ul>
   * <li>Posted if the service starts successfully;
   * <li>Cleared if the startup has failed.
   * </ul>
   *
   * @see #semStarted
   * @see #semStopped
   * @see #semDown
   */
  protected EventSemaphore semUp = new EventSemaphore(null, "up");

  /**
   * Get the {@link #semUp semaphore} that is triggered upon the {@link
   * #startup startup()} completion.
   * <p/>
   * Ideally, the semaphore returned by this method should be an immutable
   * clone of the actual semaphore.
   *
   * @return The {@link #semUp semUp} semaphore.
   */
  public EventSemaphore getSemUp() {
    return semUp;
  }

  /**
   * "Service has just been stopped" semaphore. This allows other services to
   * monitor this service and to take appropriate actions.
   * <ul>
   * <li>Posted if the service just completed the execution;
   * <li>Cleared if it had been stopped.
   * </ul>
   *
   * @see #semStarted
   * @see #semUp
   * @see #semDown
   */
  protected EventSemaphore semStopped = new EventSemaphore(null, "stopped");

  /**
   * @return The semaphore that gets triggered when the service is being stopped.
   * <p/>
   * Ideally, the semaphore returned by this method should be an immutable clone of the actual semaphore.
   *
   * @see #semStopped
   */
  public EventSemaphore getSemStopped() {
    return semStopped;
  }

  /**
   * "Service has shut down" semaphore. This allows other services to monitor
   * this service and to take appropriate actions.
   * <ul>
   * <li>Posted if the shutdown is considered successful;
   * <li>Cleared otherwise.
   * </ul>
   *
   * @see #semStarted
   * @see #semUp
   * @see #semStopped
   */
  protected EventSemaphore semDown = new EventSemaphore(null, "down");

  /**
   * Get the semaphore that gets triggered upon {@link #shutdown shutdown()}* completion.
   * <p/>
   * Ideally, the semaphore returned by this method should be an immutable clone of the actual semaphore.
   *
   * @return The semaphore that gets triggered when the service has shut down.
   *
   * @see #semStopped
   */
  public EventSemaphore getSemDown() {
    return semDown;
  }

  /**
   * A default constructor.
   * <p/>
   * This will create an object with no logger attached, default thread group
   * and default thread factory.
   */
  protected PassiveService() {
    this(Thread.currentThread().getThreadGroup(), null);
  }

  /**
   * Create a new instance, running in a designated thread group.
   *
   * @param tGroup Thread group to belong to.
   * @param tf Thread factory to use.
   */
  protected PassiveService(ThreadGroup tGroup, ThreadFactory tf) {

    this.tGroup = tGroup;
    this.tf = tf;

    if (this.tf == null) {

      // Well, we can't possibly exist without a way to get a thread
      // to execute the startup and shutdown

      //this.tf = Executors.privilegedThreadFactory();
      this.tf = Executors.defaultThreadFactory();
    }
  }

  /**
   * Create a new instance of a wrapper, running in a default thread group.
   *
   * @param tf Thread factory to use.
   */
  protected PassiveService(ThreadFactory tf) {

    // This is done to avoid the possible complications when running in
    // the alien environment, like AWT. Used to give a lot of trouble.
    this(Thread.currentThread().getThreadGroup(), tf);
  }

  /**
   * @return The thread factory being used.
   */
  public final ThreadFactory getThreadFactory() {
    return tf;
  }

  /**
   * Set the user object. This may be used to store the startup arguments
   * and/or execution results.
   *
   * @param userObject Arguments to set.
   */
  public void setUserObject(Object userObject) {

    // FIXME: maybe broadcast the change notification?
    this.userObject = userObject;
  }

  /**
   * Get the user object.
   *
   * @return The user object.
   */
  public Object getUserObject() {
    return userObject;
  }

  /**
   * Tells if the service is enabled. Service is enabled after
   * {@link #start start()} method is called and until {@link #stop stop()}
   * method is called.
   *
   * @return true if the service is enabled, false otherwise
   *
   * @see #enabled
   */
  @JmxAttribute(description = "True from the moment the service is started until the moment the service is stopped")
  public final boolean isEnabled() {
    return enabled;
  }

  /**
   * Tells if the service is ready. Service is ready when all startup actions
   * have been completed and until {@link #stop stop()} method is called.
   *
   * @return true if the service is ready, false otherwise
   *
   * @see #ready
   */
  @JmxAttribute(description = "True from the moment the service is up until the moment the service is stopped")
  public final boolean isReady() {

    return ready;
  }

  /**
   * Tells if the service is active. Service is active since
   * {@link #startup startup()} invocation and until
   * {@link #shutdown shutdown()} (after {@link #stop stop()} invocation) is
   * completed.
   *
   * @return true if the service is active, false otherwise
   *
   * @see #active
   */
  @JmxAttribute(description = "True from the moment the service is started until the moment the service is down")
  public final boolean isActive() {
    return active;
  }

  @JmxAttribute(description = "Time in milliseconds since the service was started")
  public long getUptimeMillis() {
    return startedAt == -1 ? startedAt : System.currentTimeMillis() - startedAt;
  }

  @JmxAttribute(description = "Time since the service was started, as a human readable string")
  public String getUptime() {
    return startedAt == -1 ? "down" : Interval.toTimeInterval(getUptimeMillis());
  }

  /**
   * {@inheritDoc}
   */
  public JmxDescriptor getJmxDescriptor() {
    return new JmxDescriptor("jukebox", getClass().getSimpleName(), Integer.toHexString(hashCode()), "FIXME");
  }

  /**
   * Class that supports the method redirection for the wrappers.
   *
   * @see PassiveService.StartupWrapper
   * @see PassiveService.ShutdownWrapper
   * @see PassiveService.PassiveWrapper
   */
  protected abstract class MethodWrapper {

    /**
     * Call target.
     */
    Object target;

    /**
     * Create the method wrapper based on the target object.
     *
     * @param target Object to perform the operation on.
     */
    protected MethodWrapper(Object target) {
      this.target = target;
    }

    /**
     * Call the proper method. The exact method to call is determined by
     * derived (usually anonymous) classes.
     *
     * @throws InterruptedException if the thread was interrupted.
     * @throws Throwable propagates from the called method.
     */
    abstract void call() throws Throwable;

    /**
     * Set the proper values for the host's {@link PassiveService#enabled enabled},
     * {@link PassiveService#active active} and {@link PassiveService#ready ready} flags.
     *
     * @param status {@code call()}> result is passed here.
     */
    abstract void setFlags(boolean status);
  }

  /**
   * Method wrapper for {@code startup()}> call. It will be reused in
   * {@code ActiveService}> class.
   */
  protected class StartupWrapper extends MethodWrapper {

    /**
     * Create an instance.
     *
     * @param target Object to control.
     */
    protected StartupWrapper(Object target) {

      super(target);
    }

    /**
     * Execute the startup sequence for the target service.
     * <ol>
     * <li>Call the target's {@link PassiveService#startup() startup()} method.
     * <li>If it returns {@code true}> and the target service implements
     * the {@link IdleClient IdleClient} interface, {@link Idle#register
     * register it}. <br>
     * Thoughts to myself: not every service needs to be declared idle-able,
     * and this is an overhead, so it might be a good idea to pass the
     * instances to the background thread and let it handle the idle
     * behavior. The priority of that thread may be minimal, if the
     * requirements to the strict idle timeouts could be relaxed.
     * </ol>
     *
     * @throws InterruptedException if the wait is interrupted.
     * @throws Throwable if anything goes wrong.
     */
    @Override
    protected void call() throws Throwable {

      // logger.debug("target: "+target.getClass().getName());

      ((PassiveService) target).startup();

      // If the startup() has thrown an exception, it doesn't make
      // sense to register the client as idle anyway, because it
      // hasn't started up properly.

      if (target instanceof IdleClient) {

        Idle.register((IdleClient) target);
      }
    }

    /**
     * Set the {@link PassiveService#ready ready}, {@link PassiveService#enabled enabled} and
     * {@link PassiveService#active active} flags.
     *
     * @param status Status to set the flags based upon.
     */
    @Override
    protected void setFlags(boolean status) {

      if (status) {

        ready = true;
        startedAt = System.currentTimeMillis();

      } else {

        enabled = false;
        ready = false;
        active = false;
        startedAt = -1;
      }
    }
  }

  /**
   * Method wrapper for {@code shutdown()}> call. It will be reused in
   * {@code ActiveService}> class.
   */
  protected class ShutdownWrapper extends MethodWrapper {

    /*
    * private Throwable failureCause; protected ShutdownWrapper(Object
    * target, Throwable failureCause) { super(target); // this.failureCause =
    * failureCause; }
    */
    /**
     * Create an instance.
     *
     * @param target Object to perform {@link PassiveService#shutdown() shutdown()} on.
     */
    protected ShutdownWrapper(Object target) {

      super(target);
    }

    /**
     * Execute the shutdown sequence for the target service.
     * <ol>
     * <li>Call the target's {@link PassiveService#shutdown()} method.
     * <li>If the target service implements the {@link IdleClient
     * IdleClient} interface, {@link Idle#unregister unregister it}.
     * </ol>
     *
     * @throws InterruptedException if this thread was interrupted.
     * @throws Throwable to indicate the failure. Possibly propagated
     * from implementation.
     */
    @Override
    protected void call() throws Throwable {

      Throwable cause = null;

      try {

        // VT: FIXME: provide shutdownCause here

        ((PassiveService) target).shutdown();

      } catch (Throwable t) {

        cause = t;
      }

      if (target instanceof IdleClient) {

        Idle.unregister((IdleClient) target);
      }

      if (cause != null) {

        // Rethrow it so the upper levels know what's going on

        throw cause;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setFlags(boolean status) {

      active = false;
      startedAt = -1;
    }
  }

  /**
   * Start the service. Service is started asynchronously, posting
   * {@code semStarted}> before calling the target service's
   * {@link #startup startup()}, and triggering the {@link #semUp semUp}
   * semaphore when the startup is complete.
   *
   * @return The semaphore which is triggered upon the target service startup
   *         completion. Posting that semaphore means success, clearing means failure.
   */
  public synchronized EventSemaphore start() {

    if (enabled) {

      throw new IllegalStateException("Already started");
    }

    // logger.debug(CH_SERVICE, "posting semStarted");
    semStarted.post();

    enabled = true;
    active = true;
    startCore();

    notifyAll();
    return semUp;
  }

  /**
   * Start the service. Actually, for this class it means spawn a thread which
   * executes the {@link #startup startup()}.
   */
  protected void startCore() {

    PassiveWrapper pw = new PassiveWrapper(new StartupWrapper(this), semUp);

    core = tf.newThread(pw);
    core.start();
  }

  /**
   * Stop the service. Service is stopped asynchronously, <b>clearing</b>
   * {@link #semStopped semStopped} before calling the target service
   * {@link #shutdown shutdown()}, and triggering the
   * {@link #semDown semDown} semaphore when the startup is complete.
   *
   * @return The semaphore which is triggered upon the target service shutdown
   *         completion. Posting that semaphore means success (clean shutdown),
   *         clearing means failure (unclean one).
   */
  public synchronized EventSemaphore stop() {

    if (!enabled) {
      throw new IllegalStateException("Already stopped");
    }

    semStopped.post();

    enabled = false;
    ready = false;
    stopCore();

    notifyAll();
    return semDown;
  }

  /**
   * Stop the service. Actually, for this class it means spawn a thread which
   * executes the {@link #shutdown shutdown()}.
   */
  protected void stopCore() {

    // VT: NOTE: Formerly, the ShutdownWrapper took a second argument - the
    // cause
    PassiveWrapper pw = new PassiveWrapper(new ShutdownWrapper(this), semDown);

    core = tf.newThread(pw);
    core.start();
  }

  /**
   * Wrapper for the startup, execution and shutdown threads.
   * <p/>
   * All of above share the common features:
   * <ol>
   * <li>Specific method on the target object should be called.
   * <li>Exceptions thrown by that method call should be caught and handled.
   * <li>Semaphores should be triggered.
   * </ol>
   * Thus, it's possible to have the common behavior here and implement the
   * differencies in behavior using other class, namely
   * {@link PassiveService.MethodWrapper MethodWrapper}.
   *
   * @see PassiveService.MethodWrapper
   */
  protected class PassiveWrapper implements Runnable {

    /**
     * Call target.
     */
    MethodWrapper target;

    /**
     * Semaphore to trigger with the method call result.
     */
    EventSemaphore sem;

    /**
     * Create a new wrapper.
     *
     * @param target Method wrapper providing the invocation target and the
     * proper method.
     * @param sem Event semaphore to trigger with the result of the method
     * call.
     *
     * @see PassiveService#tGroup
     */
    PassiveWrapper(MethodWrapper target, EventSemaphore sem) {

      this.target = target;
      this.sem = sem;
    }

    /**
     * Call the proper method, using the {@link PassiveService.MethodWrapper
     * MethodWrapper}, handle the exceptions and trigger the semaphore with
     * the proper value.
     */
    public// synchronized // BAD THING
    void run() {

      // logger.debug("RUNNING");
      wrap(target, sem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {

      logger.debug("finalize." + Integer.toHexString(hashCode()));
      super.finalize();
    }
  }

  /**
   * Wrap the method.
   *
   * @param target MethodWrapper to wrap.
   * @param sem Semaphore to trigger
   */
  protected void wrap(MethodWrapper target, EventSemaphore sem) {

    try {

      target.call();

      // logger.debug("call(): successful");

    } catch (InterruptedException iex) {

      logger.info("startup interrupted: ", iex);

      target.setFlags(false);
      sem.clear();
      core = null;
      // notifyAll();
      return;

    } catch (Throwable t) {

      logger.error("Uncaught exception: ", t);
      core = null;
      target.setFlags(false);
      sem.clear();
      // notifyAll();
      return;
    }

    core = null;
    target.setFlags(true);
    // logger.debug(CH_SERVICE, "trigger: " + sem.toString() + " " +
    // result);
    sem.post();
    // notifyAll(); // ???
  }

  /**
   * Check if the service is ready. This method is to be used in the methods
   * that have to be called only when the service has successfully started up
   * and before it is shut down.
   *
   * @throws IllegalStateException if the service {@link #isReady is not
   * ready}.
   */
  protected final void checkStatus() {

    if (!isReady()) {
      throw new IllegalStateException("The service has to be started before calling any methods");
    }
  }

  public ServiceStatus getStatus() {
    return new Status();
  }

  private class Status implements ServiceStatus {

    public boolean isActive() {
      return PassiveService.this.isActive();
    }

    public boolean isEnabled() {
      return PassiveService.this.isEnabled();
    }

    public boolean isReady() {
      return PassiveService.this.isReady();
    }

    public long getUptimeMillis() {
      return startedAt == -1 ? startedAt : System.currentTimeMillis() - startedAt;
    }

    public String getUptime() {
      return startedAt == -1 ? "down" : Interval.toTimeInterval(getUptimeMillis());
    }
  }
}
