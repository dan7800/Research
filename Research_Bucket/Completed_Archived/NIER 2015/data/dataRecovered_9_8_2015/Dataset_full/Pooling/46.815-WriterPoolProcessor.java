/* WriterPoolProcessor
 *
 * $Id: WriterPoolProcessor.java,v 1.6 2006/09/25 20:19:53 paul_jack Exp $
 *
 * Created on July 19th, 2006
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
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
package org.archive.crawler.framework;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlHost;
import org.archive.crawler.datamodel.CrawlOrder;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.event.CrawlStatusListener;
import org.archive.crawler.settings.SimpleType;
import org.archive.crawler.settings.StringList;
import org.archive.crawler.settings.Type;
import org.archive.io.ObjectPlusFilesInputStream;
import org.archive.io.WriterPool;
import org.archive.io.WriterPoolMember;

/**
 * Abstract implementation of a file pool processor.
 * Subclass to implement for a particular {@link WriterPoolMember} instance.
 * @author Parker Thompson
 * @author stack
 */
public abstract class WriterPoolProcessor extends Processor
implements CoreAttributeConstants, CrawlStatusListener {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Key to use asking settings for file compression value.
     */
    public static final String ATTR_COMPRESS = "compress";

    /**
     * Default as to whether we do compression of files.
     */
    public static final boolean DEFAULT_COMPRESS = true;

    /**
     * Key to use asking settings for file prefix value.
     */
    public static final String ATTR_PREFIX = "prefix";    

    /**
     * Key to use asking settings for arc path value.
     */
    public static final String ATTR_PATH ="path";

    /**
     * Key to use asking settings for file suffix value.
     */
    public static final String ATTR_SUFFIX = "suffix";

    /**
     * Key to use asking settings for file max size value.
     */
    public static final String ATTR_MAX_SIZE_BYTES = "max-size-bytes";
    
    /**
     * Key to get maximum pool size.
     *
     * This key is for maximum files active in the pool.
     */
    public static final String ATTR_POOL_MAX_ACTIVE = "pool-max-active";

    /**
     * Key to get maximum wait on pool object before we give up and
     * throw IOException.
     */
    public static final String ATTR_POOL_MAX_WAIT = "pool-max-wait";

    /***
     * Key for the maximum bytes to write attribute.
     */
    public static final String ATTR_MAX_BYTES_WRITTEN =
    	"total-bytes-to-write";
    
    /**
     * Default maximum file size.
     * TODO: Check that subclasses can set a different MAX_FILE_SIZE and
     * it will be used in the constructor as default.
     */
    private static final int DEFAULT_MAX_FILE_SIZE = 100000000;
    
    /**
     * Default path list.
     * 
     * TODO: Confirm this one gets picked up.
     */
    private static final String [] DEFAULT_PATH = {"crawl-store"};

    /**
     * Reference to pool.
     */
    transient private WriterPool pool = null;
    
    /**
     * Total number of bytes written to disc.
     */
    private long totalBytesWritten = 0;


    /**
     * @param name Name of this processor.
     */
    public WriterPoolProcessor(String name) {
    	this(name, "Pool of files processor");
    }
    	
    /**
     * @param name Name of this processor.
     * @param description Description for this processor.
     */
    public WriterPoolProcessor(final String name,
        		final String description) {
        super(name, description);
        Type e = addElementToDefinition(
            new SimpleType(ATTR_COMPRESS, "Compress files when " +
            	"writing to disk.", new Boolean(DEFAULT_COMPRESS)));
        e.setOverrideable(false);
        e = addElementToDefinition(
            new SimpleType(ATTR_PREFIX, 
                "File prefix. " +
                "The text supplied here will be used as a prefix naming " +
                "writer files.  For example if the prefix is 'IAH', " +
                "then file names will look like " +
                "IAH-20040808101010-0001-HOSTNAME.arc.gz " +
                "...if writing ARCs (The prefix will be " +
                "separated from the date by a hyphen).",
                WriterPoolMember.DEFAULT_PREFIX));
        e = addElementToDefinition(
            new SimpleType(ATTR_SUFFIX, "Suffix to tag onto " +
                "files. If value is '${HOSTNAME}', will use hostname for " +
                "suffix. If empty, no suffix will be added.",
                WriterPoolMember.DEFAULT_SUFFIX));
        e.setOverrideable(false);
        e = addElementToDefinition(
            new SimpleType(ATTR_MAX_SIZE_BYTES, "Max size of each file",
                new Integer(DEFAULT_MAX_FILE_SIZE)));
        e.setOverrideable(false);
        e = addElementToDefinition(
            new StringList(ATTR_PATH, "Where to files. " +
                "Supply absolute or relative path.  If relative, files " +
                "will be written relative to " +
                "the " + CrawlOrder.ATTR_DISK_PATH + "setting." +
                " If more than one path specified, we'll round-robin" +
                " dropping files to each.  This setting is safe" +
                " to change midcrawl (You can remove and add new dirs" +
                " as the crawler progresses).", getDefaultPath()));
        e.setOverrideable(false);
        e = addElementToDefinition(new SimpleType(ATTR_POOL_MAX_ACTIVE,
            "Maximum active files in pool. " +
            "This setting cannot be varied over the life of a crawl.",
            new Integer(WriterPool.DEFAULT_MAX_ACTIVE)));
        e.setOverrideable(false);
        e = addElementToDefinition(new SimpleType(ATTR_POOL_MAX_WAIT,
            "Maximum time to wait on pool element" +
            " (milliseconds). This setting cannot be varied over the life" +
            " of a crawl.",
            new Integer(WriterPool.DEFAULT_MAXIMUM_WAIT)));
        e.setOverrideable(false);
        e = addElementToDefinition(new SimpleType(ATTR_MAX_BYTES_WRITTEN,
            "Total file bytes to write to disk." +
            " Once the size of all files on disk has exceeded this " +
            "limit, this processor will stop the crawler. " +
            "A value of zero means no upper limit.", new Long(0)));
        e.setOverrideable(false);
        e.setExpertSetting(true);
    }
    
    protected String [] getDefaultPath() {
    	return DEFAULT_PATH;
	}

    public synchronized void initialTasks() {
        // Add this class to crawl state listeners and setup pool.
        getSettingsHandler().getOrder().getController().
            addCrawlStatusListener(this);
        setupPool(new AtomicInteger());
        // Run checkpoint recovery code.
        if (getSettingsHandler().getOrder().getController().
        		isCheckpointRecover()) {
        	checkpointRecover();
        }
    }
    
    protected AtomicInteger getSerialNo() {
        return ((WriterPool)getPool()).getSerialNo();
    }

    /**
     * Set up pool of files.
     */
    protected abstract void setupPool(final AtomicInteger serialNo);

    /**
     * Writes a CrawlURI and its associated data to store file.
     *
     * Currently this method understands the following uri types: dns, http, 
     * and https.
     *
     * @param curi CrawlURI to process.
     */
    protected abstract void innerProcess(CrawlURI curi);
    
    protected void checkBytesWritten() {
        long max = getMaxToWrite();
        if (max <= 0) {
            return;
        }
        if (max <= this.totalBytesWritten) {
            getController().requestCrawlStop("Finished - Maximum bytes (" +
                Long.toString(max) + ") written");
        }
    }
    
    protected String getHostAddress(CrawlURI curi) {
        CrawlHost h = getController().getServerCache().getHostFor(curi);
        if (h == null) {
            throw new NullPointerException("Crawlhost is null for " +
                curi + " " + curi.getVia());
        }
        InetAddress a = h.getIP();
        if (a == null) {
            throw new NullPointerException("Address is null for " +
                curi + " " + curi.getVia() + ". Address " +
                ((h.getIpFetched() == CrawlHost.IP_NEVER_LOOKED_UP)?
                     "was never looked up.":
                     (System.currentTimeMillis() - h.getIpFetched()) +
                         " ms ago."));
        }
        return h.getIP().getHostAddress();
    }
    
    /**
     * Version of getAttributes that catches and logs exceptions
     * and returns null if failure to fetch the attribute.
     * @param name Attribute name.
     * @return Attribute or null.
     */
    public Object getAttributeUnchecked(String name) {
        Object result = null;
        try {
            result = super.getAttribute(name);
        } catch (AttributeNotFoundException e) {
            logger.warning(e.getLocalizedMessage());
        } catch (MBeanException e) {
            logger.warning(e.getLocalizedMessage());
        } catch (ReflectionException e) {
            logger.warning(e.getLocalizedMessage());
        }
        return result;
    }

   /**
    * Max size we want files to be (bytes).
    *
    * Default is ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE.  Note that ARC
    * files will usually be bigger than maxSize; they'll be maxSize + length
    * to next boundary.
    * @return ARC maximum size.
    */
    public int getMaxSize() {
        Object obj = getAttributeUnchecked(ATTR_MAX_SIZE_BYTES);
        return (obj == null)? DEFAULT_MAX_FILE_SIZE: ((Integer)obj).intValue();
    }

    public String getPrefix() {
        Object obj = getAttributeUnchecked(ATTR_PREFIX);
        return (obj == null)? WriterPoolMember.DEFAULT_PREFIX: (String)obj;
    }

    public List<File> getOutputDirs() {
        Object obj = getAttributeUnchecked(ATTR_PATH);
        List list = (obj == null)? Arrays.asList(DEFAULT_PATH): (StringList)obj;
        ArrayList<File> results = new ArrayList<File>();
        for (Iterator i = list.iterator(); i.hasNext();) {
            String path = (String)i.next();
            File f = new File(path);
            if (!f.isAbsolute()) {
                f = new File(getController().getDisk(), path);
            }
            if (!f.exists()) {
                try {
                    f.mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            results.add(f);
        }
        return results;
    }
    
    public boolean isCompressed() {
        Object obj = getAttributeUnchecked(ATTR_COMPRESS);
        return (obj == null)? DEFAULT_COMPRESS:
            ((Boolean)obj).booleanValue();
    }

    /**
     * @return Returns the poolMaximumActive.
     */
    public int getPoolMaximumActive() {
        Object obj = getAttributeUnchecked(ATTR_POOL_MAX_ACTIVE);
        return (obj == null)? WriterPool.DEFAULT_MAX_ACTIVE:
            ((Integer)obj).intValue();
    }

    /**
     * @return Returns the poolMaximumWait.
     */
    public int getPoolMaximumWait() {
        Object obj = getAttributeUnchecked(ATTR_POOL_MAX_WAIT);
        return (obj == null)? WriterPool.DEFAULT_MAXIMUM_WAIT:
            ((Integer)obj).intValue();
    }

    public String getSuffix() {
        Object obj = getAttributeUnchecked(ATTR_SUFFIX);
        String sfx = (obj == null)?
            WriterPoolMember.DEFAULT_SUFFIX: (String)obj;
        if (sfx != null && sfx.trim().
                equals(WriterPoolMember.HOSTNAME_VARIABLE)) {
            String str = "localhost.localdomain";
            try {
                str = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ue) {
                logger.severe("Failed getHostAddress for this host: " + ue);
            }
            sfx = str;
        }
        return sfx;
    }
    
    public long getMaxToWrite() {
        Object obj = getAttributeUnchecked(ATTR_MAX_BYTES_WRITTEN);
        return (obj == null)? 0: ((Long)obj).longValue();
    }

	public void crawlEnding(String sExitMessage) {
		this.pool.close();
	}

	public void crawlEnded(String sExitMessage) {
        // sExitMessage is unused.
	}

    /* (non-Javadoc)
     * @see org.archive.crawler.event.CrawlStatusListener#crawlStarted(java.lang.String)
     */
    public void crawlStarted(String message) {
        // TODO Auto-generated method stub
    }
    
    protected String getCheckpointStateFile() {
    	return this.getClass().getName() + ".state";
    }
    
    public void crawlCheckpoint(File checkpointDir) throws IOException {
        int serial = getSerialNo().get();
        if (this.pool.getNumActive() > 0) {
            // If we have open active Archive files, up the serial number
            // so after checkpoint, we start at one past current number and
            // so the number we serialize, is one past current serialNo.
            // All this serial number manipulation should be fine in here since
            // we're paused checkpointing (Revisit if this assumption changes).
            serial = getSerialNo().incrementAndGet();
        }
        saveCheckpointSerialNumber(checkpointDir, serial);
        // Close all ARCs on checkpoint.
        try {
            this.pool.close();
        } finally {
            // Reopen on checkpoint.
            setupPool(new AtomicInteger(serial));
        }
    }
    
	public void crawlPausing(String statusMessage) {
        // sExitMessage is unused.
	}

	public void crawlPaused(String statusMessage) {
        // sExitMessage is unused.
	}

	public void crawlResuming(String statusMessage) {
        // sExitMessage is unused.
	}
	
    private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        ObjectPlusFilesInputStream coistream =
            (ObjectPlusFilesInputStream)stream;
        coistream.registerFinishTask( new Runnable() {
            public void run() {
            	setupPool(new AtomicInteger());
            }
        });
    }

	protected WriterPool getPool() {
		return pool;
	}

	protected void setPool(WriterPool pool) {
		this.pool = pool;
	}

	protected long getTotalBytesWritten() {
		return totalBytesWritten;
	}

	protected void setTotalBytesWritten(long totalBytesWritten) {
        this.totalBytesWritten = totalBytesWritten;
    }
	
    /**
     * Called out of {@link #initialTasks()} when recovering a checkpoint.
     * Restore state.
     */
    protected void checkpointRecover() {
        int serialNo = loadCheckpointSerialNumber();
        if (serialNo != -1) {
            getSerialNo().set(serialNo);
        }
    }

    /**
     * @return Serial number from checkpoint state file or if unreadable, -1
     * (Client should check for -1).
     */
    protected int loadCheckpointSerialNumber() {
        int result = -1;
        
        // If in recover mode, read in the Writer serial number saved
        // off when we checkpointed.
        File stateFile = new File(getSettingsHandler().getOrder()
                .getController().getCheckpointRecover().getDirectory(),
                getCheckpointStateFile());
        if (!stateFile.exists()) {
            logger.info(stateFile.getAbsolutePath()
                    + " doesn't exist so cannot restore Writer serial number.");
        } else {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new FileInputStream(stateFile));
                result = dis.readShort();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (dis != null) {
                        dis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    
    protected void saveCheckpointSerialNumber(final File checkpointDir,
            final int serialNo)
    throws IOException {
        // Write out the current state of the ARCWriter serial number.
        File f = new File(checkpointDir, getCheckpointStateFile());
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
        try {
            dos.writeShort(serialNo);
        } finally {
            dos.close();
        }
    }
}