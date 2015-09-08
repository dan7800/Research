package net.sf.jukebox.service;

import net.sf.jukebox.sem.EventSemaphore;

/**
 * Supports the concept of an observable service. Quite often it is required
 * that the checkpoints in the service lifecycle have to be tracked. The
 * checkpoints are:
 * <ul>
 * <li> The moment the service is started;
 * <li> Service startup sequence completion, either successful or unsuccessful;
 * <li> The moment the service is stopped or has completed execution, either
 * successfully or unsuccessfully;
 * <li> Service shutdown sequence completion.
 * <ul>
 * Such notification is supported with the semaphores.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-1999
 * @since J5
 * @see net.sf.jukebox.sem.EventSemaphore
 */
public interface Service extends StoppableService {

    /**
     * Get the semaphore that is triggered when the service is
     * {@link RunnableService#start() started}. This semaphore is always
     * posted.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemStarted();

    /**
     * Get the semaphore that is triggered when the startup sequence is
     * completed. This semaphore is posted if the service has successfully
     * started up and cleared if it failed to start up. In case of a failure
     * this semaphore's user object will refer to the exception that was thrown.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemUp();

    /**
     * Get the semaphore that is triggered when the service is
     * {@link StoppableService#stop() stopped}, either because it was stopped
     * by a third party or has finished execution, either successfully or
     * unsuccessfully. If the service has abnormally terminated, the semaphore
     * user object will refer to the exception that caused the termination.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemStopped();

    /**
     * Get the semaphore that is triggered when the service shutdown sequence
     * has completed. This semaphore is posted if the service has successfully
     * shut down and cleared if it failed to shut down without throwing an
     * exception. In case of a failure this semaphore's user object will refer
     * to the exception that was thrown.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemDown();

}package net.sf.jukebox.service;

import net.sf.jukebox.sem.EventSemaphore;

/**
 * Supports the concept of an observable service. Quite often it is required
 * that the checkpoints in the service lifecycle have to be tracked. The
 * checkpoints are:
 * <ul>
 * <li> The moment the service is started;
 * <li> Service startup sequence completion, either successful or unsuccessful;
 * <li> The moment the service is stopped or has completed execution, either
 * successfully or unsuccessfully;
 * <li> Service shutdown sequence completion.
 * <ul>
 * Such notification is supported with the semaphores.
 *
 * @version $Id: Service.java,v 1.2 2007-06-14 04:32:20 vtt Exp $
 * @since J5
 * @see net.sf.jukebox.sem.EventSemaphore
 */
public interface Service extends StoppableService {

    /**
     * Get the semaphore that is triggered when the service is
     * {@link RunnableService#start() started}. This semaphore is always
     * posted.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemStarted();

    /**
     * Get the semaphore that is triggered when the startup sequence is
     * completed. This semaphore is posted if the service has successfully
     * started up and cleared if it failed to start up. In case of a failure
     * this semaphore's user object will refer to the exception that was thrown.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemUp();

    /**
     * Get the semaphore that is triggered when the service is
     * {@link StoppableService#stop() stopped}, either because it was stopped
     * by a third party or has finished execution, either successfully or
     * unsuccessfully. If the service has abnormally terminated, the semaphore
     * user object will refer to the exception that caused the termination.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemStopped();

    /**
     * Get the semaphore that is triggered when the service shutdown sequence
     * has completed. This semaphore is posted if the service has successfully
     * shut down and cleared if it failed to shut down without throwing an
     * exception. In case of a failure this semaphore's user object will refer
     * to the exception that was thrown.
     *
     * @return The semaphore.
     */
    public EventSemaphore getSemDown();

}
