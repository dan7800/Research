package net.sf.jukebox.datastream.logger.impl.rrd;

import net.sf.jukebox.datastream.signal.model.DataSample;
import net.sf.jukebox.conf.Configuration;
import net.sf.jukebox.jmx.JmxDescriptor;
import org.apache.log4j.NDC;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/">RRDTool</a> data logger.
 *
 * @param <E> Data type to log.
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2008
 */
public class RrdLogger<E extends Number> extends AbstractRrdLogger<E, File> {

  /**
   * RRDTool location.
   */
  private String rrdtool;

  /**
   * {@inheritDoc}
   */
  @Override
  protected final void configure2(Configuration cf, String cfroot) throws Throwable {

    // There's no default

    rrdtool = cf.getString(cfroot + ".rrdtool");

    // Let's check if it is there

    File tool = new File(rrdtool);

    // Unfortunately, there's no way to find out

    if (!tool.exists() || !tool.canRead() || !tool.isFile()) {
      throw new IllegalArgumentException(tool.toString() + ": doesn't exist, unreadable or not a regular file");
    }

    // We won't accept relative names to avoid ambiguity

    if (!tool.isAbsolute()) {
      throw new IllegalArgumentException(tool.toString() + ": only absolute locations are acceptable");
    }

    // Good, we can go now
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected final synchronized void createChannel(String name, String signature, long timestamp) throws IOException {

    checkStatus();
    checkSignature(signature);

    // RRD may or may not exist.

    try {

      File rrdFile = new File(getRrdBase(), signature + ".rrd");

      if (!rrdFile.exists()) {

        // Have to create it. Let's put together a command line for
        // this

        String command = rrdtool + " create " + rrdFile;

        // If the span between now and the timestamp is quite large,
        // it'll take
        // quite a long time to complete the first rrdupdate after the
        // creation.
        // Should be fine afterwards, though

        command += " --start " + Long.toString(timestamp / 1000 - 600);

        // Minimum step is 1 second

        command += " --step 1 ";

        // Heartbeat is 90 seconds
        // No minimum cutoff
        // No maximum cutoff

        command += "DS:" + signature + ":GAUGE:90:U:U ";

        // 3600 samples of 1 second: 1 hour

        command += "RRA:LAST:0.5:1:3600 ";
        command += "RRA:MAX:0.5:1:3600 ";
        command += "RRA:MIN:0.5:1:3600 ";

        // 5760 samples of 30 seconds: 3 hours

        command += "RRA:LAST:0.5:30:5760 ";
        command += "RRA:MAX:0.5:30:5760 ";
        command += "RRA:MIN:0.5:30:5760 ";

        // 13824 samples of 150 seconds: 48 hours

        command += "RRA:AVERAGE:0.5:5:13824 ";
        command += "RRA:MAX:0.5:5:13824 ";
        command += "RRA:MIN:0.5:5:13824 ";

        // 16704 samples of 9000 seconds: 4 years

        command += "RRA:AVERAGE:0.5:60:16704 ";
        command += "RRA:MAX:0.5:60:16704 ";
        command += "RRA:MIN:0.5:60:16704 ";

        command += "RRA:AVERAGE:0.5:1440:50000 ";
        command += "RRA:MAX:0.5:1440:50000 ";
        command += "RRA:MIN:0.5:1440:50000";

        run(command);

        logger.warn("Created RRD database. First update may take a few minutes, be patient");
      }

      // After all is done: remember where the file is

      consume(signature, name, rrdFile);

    } catch (Throwable t) {

      throw (IOException) new IOException("Unable to create RRD").initCause(t);
    }
  }

  /**
   * Check the channel signature for {@code rrdtool} compliance.
   *
   * @param ignored Signature to check.
   * @exception IllegalArgumentException if the signature doesn't conform to
   * {@code rrdtool} constraints.
   */
  private void checkSignature(String ignored) {

    // VT: FIXME: What are the restrictions? I thought it was 16 characters - no, doesn't seem to be...
    // But I know for sure that it doesn't take 32
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected final synchronized void consume(String signature, DataSample<E> value) {

    checkStatus();

    // Get the RRD responsible for this signal

    File rrd = (File) getRrd(signature);

    if (rrd == null) {

      logger.error("RRD for '" + signature + "' supposed to exist, but doesn't - sample skipped");
      return;
    }

    // VT: NOTE: This assumes that the signal came in with a good
    // timestamp.

    // Time is defined in seconds
    String timestamp = Long.toString(value.timestamp / 1000);

    // Let's doublecheck: even though the sample may be present, its
    // signal value may be NaN

    double signalValue = value.sample == null ? Double.NaN : value.sample.doubleValue();

    String command = rrdtool + " update " + rrd + " " + timestamp + ":"
        + (Double.isNaN(signalValue) ? "U" : Double.toString(signalValue));

    run(command);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected final void shutdown() throws Throwable {

  }

  /**
   * Run a command. The output, if any, will be logged. If the command
   * completes successfully, it will be logged with {@code LOG_INFO},
   * otherwise {@code LOG_ERR}.
   *
   * @param command Command to run.
   */
  private synchronized void run(final String command) {

    NDC.push("run");

    BufferedReader br = null;

    try {

      logger.debug("Executing: " + command);

      Process p = Runtime.getRuntime().exec(command);

      int rc = p.waitFor();

      if (rc != 0) {
        // We're screwed
        logger.error("Command returned error code " + rc + ": " + command);
      }

      br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      dump(rc != 0, br);

      // Unless this is executed, repeated invocations of exec() will
      // eventually cause the system to run out of file handles

      p.destroy();

    } catch (Throwable t) {
      logger.error("Unable to execute command: " + command, t);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          logger.info("Can't close() the process stream, ignored:", e);
        }
      }
      NDC.pop();
    }
  }

  /**
   * Dump the reader content into the log.
   *
   * @param error {@code true} if the information should be logged as error.
   * @param br Reader to dump.
   */
  private void dump(boolean error, BufferedReader br) {

    long size = 0;

    try {

      while (true) {

        String line = br.readLine();

        if (line == null) {
          break;
        }

        size += line.length();

        if (error) {
          logger.error(line);
        } else {
          logger.info(line);
        }
      }

      br.close();

    } catch (IOException ex) {

      logger.error("Failed to dump rrdtool output", ex);

    } finally {

      if (size == 0 && error) {
        logger.error("No output collected");
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return "RRDTool based RRD logger";
  }

  @Override
  public JmxDescriptor getJmxDescriptor() {

    JmxDescriptor d = super.getJmxDescriptor();
    return new JmxDescriptor("DZ", d.name, d.instance, "RRD logger using rrdtool");
  }
}
