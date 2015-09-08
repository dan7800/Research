package org.archive.monkeys.harness;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * The heartbeat tracker is responsible for making sure that the browser process
 * is not hung or crashed. The tracker will start a browser instance and then check every
 * specified amount of time that the monkey plugin has pinged the harness servlet. If not,
 * the browser will be restarted.
 * @author Eugene Vahlis
 */
public class MonkeyHeartbeatTracker extends Thread {
	private int heartBeatLength = 20000;

	private String browserCommand;

	private boolean pinged;

	private Process browserProc;

	public MonkeyHeartbeatTracker() throws Exception {
		Properties conf = Harness.loadOrCreateProperties();
		this.browserCommand = conf.getProperty("browser.command");
		this.heartBeatLength = Integer.parseInt(conf.getProperty("browser.timeout"));
	}
	
	/**
	 * Notifies the tracker that the browser is still alive.
	 */
	public void ping() {
		synchronized (this) {
			this.pinged = true;
		}
	}

	@Override
	/**
	 * The main method of the thread. Check every HEART_BEAT_LENGTH milliseconds
	 * that the tracker was pinged. Otherwise, restarts the browser.
	 */
	public void run() {
		try {
			startBrowserProc();
			while (!interrupted()) {
				sleep(heartBeatLength);
				validateOrRestart();
			}
		} catch (InterruptedException e) {
			// it's ok
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				killBrowser();
			} catch (InterruptedException e) {
				System.err.println("Why am I interrupted again?");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Makes sure that a browser is running and alive by checking
	 * the ping status and restarting if necessary.
	 * @throws Exception If the restart fails.
	 */
	private void validateOrRestart() throws Exception {
		synchronized (this) {
			if (!pinged) {
				killBrowser();
				startBrowserProc();
			} else {
				this.pinged = false;
			}
		}
	}

	/**
	 * Kills the browser process.
	 * @throws InterruptedException If the tracker thread is interrupted while killing
	 * the browser process.
	 */
	private void killBrowser() throws InterruptedException {
		this.browserProc.destroy();
		this.browserProc.waitFor();
	}

	/**
	 * Starts a browser process.
	 */
	private void startBrowserProc() throws IOException {
		this.browserProc = Runtime.getRuntime().exec(browserCommand);
		this.pinged = false;
	}

}
