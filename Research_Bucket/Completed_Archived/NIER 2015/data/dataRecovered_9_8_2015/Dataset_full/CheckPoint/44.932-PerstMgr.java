/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.objsvr.perstm;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;
import vu.globe.rts.std.idl.stdInf.*;         // stdInf.idl
import vu.globe.rts.std.idl.configure.*;      // configure.idl
import vu.globe.rts.runtime.repository.idl.objectRepository.*; // objectRepository.idl
import vu.globe.rts.std.StdUtil;
import vu.globe.util.exc.NotImplementedException;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.storage.*;

import vu.globe.util.comm.ProtStack;
import vu.globe.util.comm.ProtAddress;
import vu.globe.rts.comm.tcp.TcpAddress;
import vu.globe.rts.comm.muxconn.MuxConfig;
import vu.globe.rts.comm.muxconn.MuxAddress;
import vu.globe.rts.comm.gwrap.P2PDefs;
import vu.globe.util.comm.RawOps;
import vu.globe.util.comm.idl.rawData.*;

import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.runtime.ns.idl.context;
import vu.globe.rts.runtime.ns.idl.ns.*;     // ns.idl
import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;

import vu.globe.rts.runtime.cfg.idl.rtconfig.*; // rtconfig.idl
 
import vu.globe.svcs.objsvr.skels.PerstMgrSkel;
import vu.globe.svcs.objsvr.types.ResourceIdent;

import vu.globe.svcs.objsvr.idl.resource;       // resource.idl
import vu.globe.svcs.objsvr.idl.resource.*;     // resource.idl
import vu.globe.svcs.objsvr.idl.persistence.*;  // persistence.idl

import vu.globe.svcs.objsvr.perstm.pmfile.*;
import vu.globe.svcs.objsvr.perstm.chkptfile.*;
import vu.globe.svcs.objsvr.perstm.util.*;

import vu.globe.svcs.objsvr.main.ObjectServerMain;

import java.io.*;
import java.util.*;
import java.net.*;

import vu.globe.rts.security.*;
import vu.globe.rts.security.idl.secconfig.*;
import vu.globe.rts.security.idl.sectypes.*;
  
/**
 * The persistence manager (PM).
 *
 * @author Patrick Verkaik
 */

/*
  Runtime Settings
  ----------------

  The PM is a general resource manager as well as a manager of persistent
  resources. The support for persistence is enabled/disabled by the
  PERST_SETTING runtime setting. If the setting is absent then persistence
  support is disabled. Persistence support must be enabled in order to restart
  a persistent process.

  The directory of the state of the PM is read from a Globe runtime setting.
  The setting's name is FSROOT_SETTING. Its value is a pathname. If the setting
  is absent, then there is no support for storage resources, or for
  persistence. This setting must be defined if persistence was enabled by
  PERST_SETTING.

  The TCP port at which contact points are allocated by the PM is read from a
  Globe runtime setting. The setting's name is TCP_SETTING. Its value is a TCP
  port number. This setting is only used if a process is started for the very
  first time (i.e. it is ignored by a restarting persistent process). A process
  that is started for the very first time and in which this setting is absent
  will have no support for light-weight or persistent contact points.

  The IP address at which contact points are allocated by the PM is read from
  the IP_SETTING Globe runtime setting. This setting is optional. Its value is
  an IP address. This setting is only used if a process is started for the very
  first time (i.e. it is ignored by a restarting persistent process).

  The interval between each passivation checkpoint is read from the
  CHECKPOINT_INTERVAL_SETTING Globe runtime setting. This setting is
  optional. Its value is an interval (in seconds). If it is not defined,
  a default interval is used.

  File System Use
  ===============

  (See implementation notes for furhter FS documentation).

  The PM keeps its storage resources and its passivated state in a directory,
  specified by the runtime setting above.

  The directory is created when the (non)-persistent process is started for the
  first time, and it is an error for the directory to already exist in this
  case. On shutdown of a non-persistent process, the directory is removed.

  Crash Recovery
  --------------

  In order to be able to recover from a crash (improper shutdown), so
  called passivation checkpoints are performed. During a passivation
  checkpoint the object server's essential state is collected and written
  as a single file to disk. The essential state consists of the passivation
  state of the persistent objects and the state of the persistence manager.

  The first step of a passivation checkpoint consists of pausing all
  activities (including network activity). This way, a consistent snapshot
  image of the essential state can be taken. These activities are resumed
  in the final step of a checkpoint. Passivation checkpoints are performed
  at fixed intervals (see the CHECKPOINT_INTERVAL_SETTING described above).

  When an object server boots, it first checks whether the checkpoint
  file is present. If this is the case, it means that the object server
  was not properly shut down (crashed) the previous time. The object
  will then use this file to recover its state. When an object server
  is properly shut down, the checkpoint file (if any) is removed.

  Thread Safety Notes
  -------------------
 
  See implementation notes.
*/

public class PerstMgr extends PerstMgrSkel 
{
  /**************************** Runtime settings ****************************/

  /**
   * The name of the Globe runtime setting that determines whether or not to
   * enable persistence support.
   */ 
  private static final String PERST_SETTING = "persistent";

  /**
   * The name of the Globe runtime setting that determines the directory of the
   * state of the PM.
   */ 
  private static final String FSROOT_SETTING = "perst.fsroot";

  /**
   * The name of the Globe runtime setting that determines the TCP port at
   * which light-weight contact points are allocated.
   */ 
  private static final String TCP_SETTING = "gids.schema.cpPort";

  /**
   * The name of the Globe runtime setting that determines the preferred IP
   * address on a multi-homed host. Used for light-weight contact point
   * allocation.
   */ 
  private static final String IP_SETTING = "gids.schema.preferredIP";

  /**
   * The name of the Globe runtime setting that determines the number of
   * seconds between each passivation checkpoint.
   */
  private static final String CHECKPOINT_INTERVAL_SETTING =
                                "gids.schema.checkpointInterval";

  /**
   * If the following flag is set and the object server is started in
   * regular mode, the first checkpoint is immediately made (rather than
   * waiting until the first checkpoint interval has elapsed).
   */
  private static final boolean CHECKPOINT_ON_START = false;

  /**
   * The name of the Globe runtime setting that determines the estimated
   * number of storage resources the PM should be able to handle (see
   * LargeDir). 
   */ 
  private static final String STORAGE_CAPACITY_SETTING =
  			"perst.storage.capacity";

  /**
   * The name of the Globe runtime setting that determines the estimated
   * number of files per directory that the file system can reasonably support.
   */
  private static final String DIR_CAPACITY_SETTING = "perst.dir.capacity";

  /** The Globe runtime settings object. */
  private rtSettings _settings;                     // counted

  /**
   * Whether or not persistence support is enabled. Determined by the (absence
   * of) the PERST_SETTING runtime setting.
   */
  private boolean _persistence_enabled = false;

  /**
   * The value of the FSROOT_SETTING runtime setting. This value specifies
   * the name of the directory with the files of this process. Null if the
   * runtime setting was undefined.
   */
  private String _fs_root_name = null;

  /**
   * The value of the TCP_*SETTING runtime settings. This value specifies a TCP
   * port at which light-weight contact points are allocated. -1 if the setting
   * was undefined.
   */
  private int _tcp_port = -1;


  /**
   * The value of the IP_SETTING runtime setting. This value specifies an IP
   * address at which light-weight contact points are allocated. null if the
   * setting was undefined.
   */
  private InetAddress _ip_address = null;

  /**
   * The value of the CHECKPOINT_INTERVAL_SETTING runtime setting. This
   * value specifies the time interval (in seconds) between each passivation
   * checkpoint. During a checkpoint, the passivation state of the
   * object server is collected and written to disk. Only if the value
   * is positive, will the checkpoint state be dumped.
   */
  private int _chkpt_interval = 3600;              // 1 hour

  /**
   * The value of the STORAGE_CAPACITY_SETTING runtime setting. This value
   * specifies the estimated number of storage resources the PM should be able
   * to handle (see LargeDir). -1 if the runtime setting was undefined.
   */
  private int _storage_capacity = -1;

  /**
   * The value of the DIR_CAPACITY_SETTING runtime setting. This value
   * specifies the estimated number of files per directory that the file
   * system can reasonably support. 0 for 'any number of files, -1 if the
   * runtime setting was undefined.
   */
  private int _dir_capacity = -1;

  /**************************** Local name space ****************************/

  /** The prefix to name storage objects with in the local name space. */
  private static final String STORAGE_PREFIX = "storage";

  /** The prefix to name multiplexer objects with in the local name space. */
  private static final String MUX_PREFIX = "mux";

  /** The local name space. */
  private nameSpace _lns;

  /** The context of the persistence manager. */
  private int _pm_ctx;


  /*********************************** Table ********************************/

  /** The persistent objects and (persistent and transient) resources. */
  private final PerstTable _persts = new PerstTable();


  /***************************** Persistent objects **************************/

  /** A factory of persistent objects. */
  private PerstObjCreator _perst_obj_creator;

  /**
   * The root persistent object. 'Invalid' if none exists.
   */
  private ResourceIdent _root_po = ResourceIdent.INVALID_RESOURCE_IDENT;

  /******************************* Contact points ****************************/

  /**
   * The light-weight contact point allocators. Currently there is only one
   * such allocator. It allocates contact points at a TCP port.
   */
  private PCPAllocatorTable _pcp_allocs;

  /******************************** Storage ********************************/

  /**
   * The directory with the files of this process. Null if the FSROOT_SETTING
   * runtime setting was undefined.
   */
  private File _fs_root = null;

  /**
   * The file with the PM's persistent state, relative to the root directory
   * of the PM.
   */
  private static final String PM_FILE = "perst_mgr";

  /**
   * The file with the PM's persistent state. Null if the FSROOT_SETTING
   * runtime setting was undefined. 
   */
  private File _pm_state_file = null;

  /**
   * The file with the the storage files of this process, relative to the root
   * directory of the PM.
   */
  private static final String STORAGE_DIR = "storage";

  /**
   * The directory with the storage files of this process. Null if the
   * FSROOT_SETTING runtime setting was undefined.
   */
  private LargeDir _storage_dir = null;

  /** A factory of storage objects. */
  private final StorageObjCreator _storage_obj_creator =
  					new StorageObjCreator();

  /***************************** Checkpointing *****************************/

  /**
   * The name of the passivation checkpoint file, relative to the root
   * directory of the PM. This file, which is periodically created
   * by the passivation checkpoint thread, contains the state of the
   * persistence manager and the passivation state of the persistent
   * objects.
   */
  private static final String CHECKPOINT_FILE = "checkpoint.data";

  /**
   * The file with the object server's checkpoint data. Null if the
   * FSROOT_SETTING runtime setting was undefined. 
   */
  private File _chkpt_file;

  /**
   * The name of the backup file of the passivation checkpoint file,
   * relative to the root directory of the PM.
   */
  private static final String CHECKPOINT_BACKUP_FILE = "checkpoint.data.bak";

  /**
   * The backup file of the object server's checkpoint data. Null if the
   * FSROOT_SETTING runtime setting was undefined. 
   */
  private File _chkpt_backup_file;


  /**
   * The name of the passivation checkpoint log file, relative to the root
   * directory of the PM. This file, which is periodically created
   * by the passivation checkpoint thread, contains status information
   * about the checkpoint. If set to <code>null</code>, no log file will
   * be created.
   */
  private static final String CHECKPOINT_LOG_FILE = "checkpoint.log";

  /**
   * The log file associated with the checkpoint file. Null if the
   * FSROOT_SETTING runtime setting was undefined, or if the STATE_LOG_FILE was
   * undefined.
   */
  private File _chkpt_log_file;

  /**
   * A thread which periodically performs a checkpoint. During a
   * checkpoint, it retrieves the checkpoint state by invoking
   * <code>performPassivationCheckpoint()</code>, and it then
   * writes this state to disk in the background.
   */
  private PassivationCheckpointThread _checkpointThread = null;

  /**
   * A factory to create checkpoint file readers and writers.
   */
  private ChkptFileReaderWriterFactory _chkptFileReaderWriterFactory = null;


  /***************************** Miscellaneous *****************************/

  /** A PM-specific synchronisation object. */
  private final Coordinator _coord = new Coordinator();

  /**
   * A table which holds important resource identifiers. The mapping is
   * as follows: <code>ResourceIdent</code> -> <code>ResourceIdent</code>.
   */
  private HashMap _essential_rids = new HashMap();


  /** An object which alerts object who register their callback there at regular
   * intervals.
   */
  private TimerResourceManager _timerRscMgr = null;


  /*************************************************************************/

  // GObject method overrides

  public PerstMgr()
  {
    super();
  }

  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    GInterface.RelInf (_settings);
    if (_perst_obj_creator != null)
      _perst_obj_creator.removeClient();
    if (_pcp_allocs != null)
      _pcp_allocs.removeClient();

    if (_timerRscMgr != null) {
        try {
            _timerRscMgr.stop();
        } catch (Exception e) {
            // Well guess not then...
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "PerstMgr: ERROR: Could not stop Timer Manager!");
        }
    }
    
    super.cleanup();
  }

  protected void initState() throws SOIerrors
  {
    /*
       Performs initialisation, but leaves the following to restart():
       -  creating the directory for persistent state
       -  creating the LargeDir with storage object data
       -  loading persistent state
       -  installing a PCP allocator
       -  activating persistent objects
       -  setting the file object members (_fs_root, etc.)
       -  running the passivation checkpoint thread.

	Setting the file object members is deliberately left to restart(). This 
	ensures that allocateStorage will fail if the the process neglects to
	invoke restart(). The same applies to installing a PCP allocator. If
	restart() is not invoked, allocateContact() will fail.
    */

    _lns = nameSpaceImp.getLNS();
    _pm_ctx = getContext(); // GObject method;
    try {

      // bind to the settings object
      SOInf sett_soi = _lns.bind (getContext(), nsConst.SETTINGS_NAME);
      _settings = (rtSettings) sett_soi.swapInf (rtSettings.infid);

      readPersistenceSetting();
      readRootFSSetting();
      _tcp_port = readTcpPortSetting( TCP_SETTING );
      readIpAdressSetting();
      readStorageCapacitySetting();
      readDirCapacitySetting();
      readCheckpointIntervalSetting();

      _perst_obj_creator = new PerstObjCreator();
      _pcp_allocs = new PCPAllocatorTable (_lns, _pm_ctx, MUX_PREFIX);


      // The TimerResourceManager
      _timerRscMgr = new TimerResourceManager();
    }
    catch (SOIerrors exc) {
      throw exc;
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new SOIerrors_misc();
    }
  }

  /**
   * Reads the Globe runtime setting which defines whether persistence support
   * is enabled, and sets _persistence_enabled accordingly.
   */
  private void readPersistenceSetting() throws Exception
  {
    String perst_str = _settings.getValue (PERST_SETTING);

    _persistence_enabled = perst_str != null &&
		perst_str.equalsIgnoreCase ("true");
  }

  /**
   * Reads the Globe runtime setting which defines the root directory of the
   * PM's files, and sets _fs_root_name accordingly.
   */
  private void readRootFSSetting() throws Exception
  {
    _fs_root_name = _settings.getValue (FSROOT_SETTING); // null if undefined
  }

  /**
   * Reads the Globe runtime setting which defines the TCP port at which
   * light-weight contact points are allocated.
   */
  private int readTcpPortSetting( String SettingName ) throws Exception
  {
    String port_str = _settings.getValue( SettingName );
    if (port_str != null) 
    {
      try {
        return Integer.parseInt (port_str);
      }
      catch (NumberFormatException exc) {
        System.err.println( SettingName + " setting must have numeric value!");
        throw new RuntimeException( SettingName + " has illegal value");
      }
    }
    return -1;
  }

  /**
   * Reads the Globe runtime setting which defines the preferred IP address
   * on a multi-homed host, and sets _ip_address accordingly.
   */
  private void readIpAdressSetting() throws Exception
  {
    String ip_str = _settings.getValue (IP_SETTING);
    if (ip_str != null) {
      try {
        _ip_address = InetAddress.getByName (ip_str);
      }
      catch (UnknownHostException exc) {
        System.err.println (IP_SETTING + " refers to unknown IP address!");
        throw new RuntimeException (IP_SETTING + " has illegal value");
      }
    }
  }

  /**
   * Reads the Globe runtime setting which defines the passivation
   * checkpoint interval, and sets _chkpt_interval accordingly.
   */
  private void readCheckpointIntervalSetting() throws Exception
  {
    String s = _settings.getValue (CHECKPOINT_INTERVAL_SETTING);
    if (s != null) {
      try {
        _chkpt_interval = Integer.parseInt(s);
      }
      catch(NumberFormatException exc) {
        System.err.println (CHECKPOINT_INTERVAL_SETTING
                            + " setting must have numeric value: " + s);
        throw new RuntimeException (CHECKPOINT_INTERVAL_SETTING
                                    + " has illegal value: " + s);
      }
    }
  }


  /** Reads _storage_capacity from a Globe runtime setting. */
  private void readStorageCapacitySetting() throws Exception
  {
    String str = _settings.getValue (STORAGE_CAPACITY_SETTING);
    if (str != null) {
      try {
        _storage_capacity = Integer.parseInt (str);
      }
      catch (NumberFormatException exc) {
        System.err.println (STORAGE_CAPACITY_SETTING +
		" setting must have numeric value!");
        throw new RuntimeException (STORAGE_CAPACITY_SETTING +
		" has illegal value");
      }
    }
  }

  /** Reads _dir_capacity from a Globe runtime setting. */
  private void readDirCapacitySetting() throws Exception
  {
    String str = _settings.getValue (DIR_CAPACITY_SETTING);
    if (str != null) {
      try {
        _dir_capacity = Integer.parseInt (str);
      }
      catch (NumberFormatException exc) {
        System.err.println (DIR_CAPACITY_SETTING +
		" setting must have numeric value!");
        throw new RuntimeException (DIR_CAPACITY_SETTING +
		" has illegal value");
      }
    }
  }

  // timerResourceManagerInterface
  public long schedule(timerResourceManagerCB cb, g.opaque user,
                       long interval, long firstInterval) throws Exception {
    DebugOutput.println(DebugOutput.DBG_DEBUG,
            "PerstMgr: about to schedule a timer task at interval: "+interval);
    return _timerRscMgr.schedule(cb, user, interval, firstInterval);
  }

  public void cancel(long timer) throws Exception {
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "PerstMgr: cancelling timer: "+timer);
    _timerRscMgr.cancel(timer);
  }
 
  public void reschedule(long timer, long interval, long firstInterval) 
            throws Exception {
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "PerstMgr: rescheduling timer: "+timer);
    _timerRscMgr.reschedule(timer, interval, firstInterval);
  }
  

  // storageManager interface
  public long allocateStorage() throws resourceError
  {
    if (_fs_root_name == null) {
      // no support for storage resources
      throw new resourceError_invOp();
    }

    DebugOutput.dassert (_fs_root != null,
    	"!!!!!!!!!!!!PM: restart() was not invoked");

    ResourceIdent rid = _persts.allocateIdentifier();

    // allocate, but do not create the file

    String filename = rid.toString();
    File file = _storage_dir.mapToFile (filename);
    if (file.exists()) { // safety check 
      System.err.println ("pm: curious: " + file.getPath() + " exists");
      throw new resourceError_io();
    }
    StorageEntry entry = new StorageEntry (rid, filename); 
    _persts.storeEntry (rid, entry);

    return rid.getIdl();
  }

  public void deallocateStorage (long rid)  throws resourceError_invArg
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    StorageEntry entry = _persts.removeStorageEntry (res_id);
    if (entry == null) {
      // id non-existent or refers to something other than storage
      throw new resourceError_invArg();
    }

    // deallocate id *after* deleting the file!
    String filename = entry.getStorage();
    File file = _storage_dir.mapToFile (filename);
    file.delete();
    _persts.deallocateIdentifier (res_id);
  }

  public storage openStorage (long rid) throws resourceError
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    PerstEntry entry = _persts.lookupEntry (res_id);
    if (! (entry instanceof StorageEntry)) {
      // id non-existent or refers to something other than storage
      throw new resourceError_invArg();
    }
    StorageEntry sentry = (StorageEntry) entry;
    File file = _storage_dir.mapToFile (sentry.getStorage());
    try {
      return _storage_obj_creator.createStorageObj (res_id, file);
    }
    catch (IOException exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new resourceError_io();
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new resourceError_misc();
    }
  }

  // contactManager interface
  public long allocateContact (String protocol) throws resourceError
  {
    if (_tcp_port == -1 ) {
      // no support for contact point resources
      throw new resourceError_invOp();
    }

    DebugOutput.dassert (_pcp_allocs.size() != 0, 
    	"!!!!!!!!!!!!PM: restart() was not invoked");

    ResourceIdent rid = _persts.allocateIdentifier();

    // allocate a light-weight contact point
    PCPAllocator palloc = _pcp_allocs.getByStack (protocol);
    if (palloc == null) {
	// mux protocol stack not supported or restart() not invoked
    	throw new resourceError_invArg();
    }
    String opsys_contact = palloc.getOpsysContact();
    String lw_contact = palloc.allocate();

    ContactEntry entry = new ContactEntry (rid, lw_contact, opsys_contact);
    _persts.storeEntry (rid, entry);

    return rid.getIdl();
  }

  public void deallocateContact (long rid) throws resourceError_invArg
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    ContactEntry entry = _persts.removeContactEntry (res_id);
    if (entry == null) {
      // id non-existent or refers to something other than a light-weight cp
      throw new resourceError_invArg();
    }

    // deallocate id *after* deallocating the light-weight contact point
    String opsys_contact = entry.getOpsysContact();
    String lw_contact = entry.getLWContact();
    PCPAllocator palloc = _pcp_allocs.getByOpsys (opsys_contact);
    DebugOutput.dassert (palloc != null);
    palloc.deallocate (lw_contact);
    _persts.deallocateIdentifier (res_id);
  }

  public String getAddress (long rid) throws resourceError_invArg
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    PerstEntry entry = _persts.lookupEntry (res_id);
    if (! (entry instanceof ContactEntry)) {
      // id non-existent or refers to something other than a light-weight cp
      throw new resourceError_invArg();
    }
    ContactEntry centry = (ContactEntry) entry;
    return centry.getLWContact();
  }

  // perstResourceManager interface

  public void makePersistent (long allocator, long rid) throws perstError
  {
    // 'allocator' not needed until GC implemented

    if (! _persistence_enabled)         // no support for persistent resources
      throw new perstError_invOp();

    ResourceIdent res_id = new ResourceIdent (rid);
    PerstEntry entry = _persts.lookupEntry (res_id);
    if (! (entry instanceof ResourceEntry)) {
      // id non-existent or refers to something other than a resource
      throw new perstError_invArg();
    }
    ResourceEntry rentry = (ResourceEntry) entry;
    rentry.makePersistent();
  }


  // activationRecord interface
  public void setRecreateInfo (long rid, String impl, String init, rawDef jar)
                   throws perstError_invArg
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    PerstEntry entry = _persts.lookupEntry (res_id);
    if (! (entry instanceof ActivationEntry)) {
      // id non-existent or refers to something other than an activation rec
      throw new perstError_invArg();
    }
    ActivationEntry aentry = (ActivationEntry) entry;
    aentry.setRecreateInfo (impl, init, jar);
  }

  public long getState (long rid) throws perstError_invArg
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    PerstEntry entry = _persts.lookupEntry (res_id);
    if (! (entry instanceof ActivationEntry)) {
      // id non-existent or refers to something other than an activation rec
      throw new perstError_invArg();
    }
    ActivationEntry aentry = (ActivationEntry) entry;
    return aentry.getState().getIdl();
  }

  public void informPassivated (long rid) throws perstError_invArg
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    PerstEntry entry = _persts.lookupEntry (res_id);
    if (! (entry instanceof ActivationEntry)) {
      // id non-existent or refers to something other than an activation rec
      throw new perstError_invArg();
    }
    _coord.informObjectHasPassivated();
  }
 
  // perstObjectManager interface
  public long registerPersistent (long allocator, persistentObject inf)
                    throws Exception
  {
    if (! _persistence_enabled)         // no support for persistent objects
      throw new perstError_invOp();

    _coord.beginRegister();

    try {

      // transient allocator <=> root does not yet exist
      if (allocator == resource.invalidResourceID ^ _root_po.equals (
                                   ResourceIdent.INVALID_RESOURCE_IDENT))
          throw new perstError_invArg();

      managedPerstObject po = (managedPerstObject) inf.soi.getInf (
                                                  managedPerstObject.infid);

      ResourceIdent rid = _persts.allocateIdentifier();
      ActivationEntry entry = new ActivationEntry (rid);
      entry.setRuntime (po);

      // create persistent storage on behalf of the po.
      long state_rid = allocateStorage();
      makePersistent (rid.getIdl(), state_rid);
      entry.setState (new ResourceIdent (state_rid));

      _persts.storeEntry (rid, entry);

      if (_root_po.equals (ResourceIdent.INVALID_RESOURCE_IDENT)) {
        _root_po = rid;

        registerEssentialResourceID(_root_po.getIdl());
      }

      // Tell the object to become persistent.
      po.bePersistent (rid.getIdl());

      return rid.getIdl();
    }
    finally {
      _coord.endRegister();
    }
  }

  public void unregisterPersistent (long rid) throws Exception
  {
    _coord.beginUnregister();

    try {

      ResourceIdent res_id = new ResourceIdent (rid);
      if (res_id.equals (_root_po)) {
        _root_po = ResourceIdent.INVALID_RESOURCE_IDENT;

        unregisterEssentialResourceID(_root_po.getIdl());
      }

      PerstEntry entry = _persts.lookupEntry (res_id);
      if (! (entry instanceof ActivationEntry)) {
        // id non-existent or refers to something other than an activation rec
        throw new perstError_invArg();
      }
      ActivationEntry aentry = (ActivationEntry) entry;
      managedPerstObject po = aentry.getRuntime();

      // Tell the object to become transient.
      po.beTransient();


      // Release my runtime infs to the object.
      po.relInf();
      aentry.releaseRuntime();

      // remove table entry, persistent state, and id allocation
      // Note: This call must be done after releasing the runtime
      //       interfaces (see previous two statements), as the object
      //       whose interfaces are being released may still assume that
      //       it has an entry in the table. Consistency is ensured by
      //       the coordinator.
      _persts.removeEntry (res_id);

      deallocateStorage (aentry.getState().getIdl());
      _persts.deallocateIdentifier (res_id);

    }
    finally {
      _coord.endUnregister();
    }
  }

  public short isValidEntry(long pid) {
    
    ResourceIdent res_id = new ResourceIdent(pid);
    PerstEntry entry = _persts.lookupEntry(res_id);
    if (entry == null) {
        // id non-existent
        return g.bool.False;
    }
    return g.bool.True;
  }
  
  public long getRootPersistent() throws perstError_invOp
  {
    if (! _persistence_enabled)         // no support for persistent objects
      throw new perstError_invOp();

    return _root_po.getIdl();
  }

  public persistentObject getRuntime (long rid) throws Exception
  {
    ResourceIdent res_id = new ResourceIdent (rid);
    PerstEntry entry = _persts.lookupEntry (res_id);
    if (! (entry instanceof ActivationEntry)) {
      // id non-existent or refers to something other than an activation rec
      throw new perstError_invArg();
    }
    ActivationEntry aentry = (ActivationEntry) entry;
    
    // see the implementation notes about the following

    try {
      activateAnObject (aentry, null);
    }
    catch (ActivationEntry.ActivationException exc) {
      exc.printStackTrace();

      /*
       * An error occurred. If it is a non-essential object, remove its
       * activation record from the rid table.
       */
      if (isEssentialResourceID(res_id)) {
        System.err.println ("pm: cannot activate essential object with rid "
                            + rid);
      }
      else {
        System.err.println ("pm: cannot activate object with rid " + rid
                            + " -- removing it");
        _persts.removeEntry (res_id);
      }

      throw new perstError_invArg();
    }

    managedPerstObject po;
    try { po = aentry.getActivatedRuntime(); }
    catch (ActivationEntry.ActivationException exc) {
      throw new perstError_invArg();
    }

    return (persistentObject) po.swapInf (persistentObject.infid);
  }

  /**
   * Mark the given resource identifier as essential by adding it to the
   * ``important resource IDs'' table. This is a no-op if the identifier
   * is already in the table.
   */
  public void registerEssentialResourceID(long rid)
  {
    ResourceIdent res_id = new ResourceIdent (rid);

    synchronized(this) {
      _essential_rids.put(res_id, res_id);
    }
  }

  /**
   * Remove the given resource identifier from the ``important resource IDs''
   * table. This is a no-op if the identifier is not in the table.
   *
   * @param  rid  the resource identifier
   */
  public void unregisterEssentialResourceID(long rid)
  {
    ResourceIdent res_id = new ResourceIdent (rid);

    synchronized(this) {
      _essential_rids.remove(res_id);
    }
  }

  /**
   * Return <code>true</code> if the given resource identifier is marked
   * as essential; <code>false</code> otherwise.
   */
  private boolean isEssentialResourceID(ResourceIdent rid)
  {
    synchronized(this) {
      return (_essential_rids.get(rid) != null);
    }
  }


  /*=====================================================================*\
   *                            shutdown                                 *
  \*=====================================================================*/

  // persistenceManager interface
  public void shutdown (short /* g.bool */ immediately) throws perstError
  {
    // Cleans up a (non-)persistent process. Performs the following tasks, each
    // as necessary:
    // - stops the passivation checkpoint thread
    // - stops the timer manager thread
    // - passivates persistent objects
    // - saves persistent state
    // - removes checkpoint file 
    // - removes the directory

    System.out.println ("pm: process is shutting down");
    
    if (! _persistence_enabled) {
  
      // remove the directory together with all of its contents
  
      if (_fs_root != null) {
  
        DebugOutput.println ("removing directory tree " + _fs_root);
        try { StorageUtil.removeDirFiles (_fs_root, true); }
        catch (IOException exc) {
          DebugOutput.printException (exc);
          DebugOutput.println ("pm: please remove directory " + _fs_root +
          	" by hand");
          throw new perstError_io();
        }
      }
    }
    else {
      /*
       * Stop the passivation checkpoint thread, as its operation (i.e.,
       * saving state) may interfere with the shut down procedure.
       */
      System.out.print("pm: stopping checkpoint thread... ");
      System.out.flush();
      _checkpointThread.halt();
      _checkpointThread.waitUntilHalted();                    // wait
      System.out.println("[ok]");

      // Stop the timer resource manager thread.
      if (_timerRscMgr != null) {
        try {
            System.out.print("pm: shutting down timer resource manager ");
            System.out.flush();
            _timerRscMgr.stop();
            System.out.println("[ok]");
        } catch (Exception e) {
            // Well guess not then...
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "PerstMgr: ERROR: Could not stop Timer Manager!");
        }
    }

      // save state
      passivateObjects (immediately);

      try { writePMState(_pm_state_file); }
      catch (IOException exc) {
        exc.printStackTrace();
        System.err.println ("pm: could not save persistence manager state");
        throw new perstError_io();
      }
    }

    if (_persistence_enabled) {
      /*
       * Remove checkpoint files. The absence of the checkpoint file
       * indicates a proper shutdown.
       */
      removeCheckpointFiles();
    }
  }

  /**
   * Releases all persistent objects. The objects will be passivated as soon
   * as other objects in the system have also released them. Waits until
   * passivation of all persistent objects has completed.
   *
   * @param immediately    see the IDL spec for shutdown. If true, then the
   *			   objects will be told to passivate quickly
   */
  private void passivateObjects (short /* g.bool */ immediately)
  {
    // Make a list containing all persistent objects. The list also includes
    // resources. Although object (un)registrations are impossible,
    // resource (de)allocations are not. So take care when using this list.

    ResourceIdent [] rid_list = _persts.getIdentsArray();

    /*
     * The root persistent object (the object server manager) must be
     * passivated first, then the regular objects. Otherwise,
     * (un)registrations may occur while these regular objects are
     * passivated. 
     */

    /*
     * Move the resource identifier of the root po to the front of the
     * rid list. This way, the root po will be passivated first.
     */
    if (_root_po != ResourceIdent.INVALID_RESOURCE_IDENT) {
      if (moveRidToFront(rid_list, _root_po) == false) {
        System.err.println("pm: error - root po cannot be passivated: "
                           + "rid list does not contain root po");
        throw new AssertionFailedException();
      }
    }
    else {
      System.err.println("pm: error - root po cannot be passivated: "
                         + "root po rid not set");
      throw new AssertionFailedException();
    }

    // go through the list releasing (and counting) the objects
    // if 'immediately' is true then give an advance warning of the passivation
    int n_objects = 0;
    for (int i = 0; i < rid_list.length; i++) {

      ResourceIdent rid = rid_list [i];
      PerstEntry entry = _persts.lookupEntry (rid);
      if (! (entry instanceof ActivationEntry))  // or entry==null
        continue;
      ActivationEntry aentry = (ActivationEntry) entry;

      DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                           "pm: releasing persistent object with rid " + rid);

      aentry.informPassivating();

      // Warn (optional) and release the object.
      managedPerstObject po = aentry.getRuntime();

      try {
	if (immediately == g.bool.True) {
      	  po.prepareImmediatePassivation();
        }
      } catch (Exception exc) {
	exc.printStackTrace();
      }

      // release the interface reference allowing the passivation of the po
      aentry.releaseRuntime();
      po.relInf();
      n_objects++;

      /*
       * If this is the root po, wait until all invocations of
       * (un)registerPersistent() have finished.
       */
      if (rid == _root_po) {
        _coord.waitUntilRegisterPersistentsFinished();
      }
    }
    _coord.waitUntilObjectsPassivated (n_objects);
  }


  /*=====================================================================*\
   *                            (re)start                                *
  \*=====================================================================*/

  public void restart() throws perstError
  {
    // Either starts a new (non-)persistent process, or restart a persistent
    // process. Performs the following tasks, each as necessary:
    // - sets the file object members (_fs_root, etc.)
    // - creates the directory with persistent state
    // - creates the LargeDir with storage object data
    // - installs a PCP allocator
    // - loads persistent state
    // - activates persistent objects
    // - runs the passivation checkpoint thread.

    boolean recoverMode = false;

    if (_fs_root_name != null) {
      _fs_root = new File (_fs_root_name);
      _pm_state_file = new File (_fs_root, PM_FILE);

      _chkpt_file = new File (_fs_root, CHECKPOINT_FILE);
      _chkpt_backup_file = new File (_fs_root, CHECKPOINT_BACKUP_FILE);

      if (CHECKPOINT_LOG_FILE == null) {
        _chkpt_log_file = null;
      }
      else {
        _chkpt_log_file = new File (_fs_root, CHECKPOINT_LOG_FILE);
      }
    }
  
    if (_persts.size() != 0)
      throw new AssertionFailedException ("already (re)started"); 
  
    if (! _persistence_enabled) {
      DebugOutput.println ("persistence support disabled");

      if (_fs_root != null && _fs_root.exists()) {
  
        System.err.println("pm: cannot run non-persistent process in " + 
                           "existing directory " + _fs_root);
        throw new perstError_io();
      }
      createStartedProcessState();
    }
    else {
      DebugOutput.println ("persistence support enabled");
  
      // _persistence_enabled, ergo _fs_root is defined

      if (_fs_root == null) {
        System.err.println ("if '" + PERST_SETTING + "' is defined, then so" +
                            " must '" + FSROOT_SETTING + "'");
        throw new perstError_misc();
      }

      /*
       * Create a factory for checkpoint file readers and writers.
       */
      _chkptFileReaderWriterFactory =
         new PlainChkptFileReaderWriterFactory();      // use regular files
  
      if (! _fs_root.exists()) {
        System.out.println("pm: first run of persistent process");
        createStartedProcessState();
      }
      else {
  
        // Create the LargeDir, load the state, and activate persistent
        // objects
  
        System.out.println ("pm: persistent process restarting");
        if (_tcp_port != -1)
          System.out.println ("pm: ignoring " + TCP_SETTING + " setting");
        if (_ip_address != null)
          System.out.println ("pm: ignoring " + IP_SETTING + " setting");

        try {
          _storage_dir = new LargeDir (new File (_fs_root, STORAGE_DIR),
            "pm", _storage_capacity, _dir_capacity);
        }
        catch (IOException exc) {
          DebugOutput.println ("pm: error initialising LargeDir: ", exc);
          throw new perstError_io();
        }

        /*
         * If the checkpoint file or its backup exists, the server
         * probably crashed the previous time. Therefore, the server
         * will now start in recover mode.
         */
        if (_chkpt_file.exists() || _chkpt_backup_file.exists()) {

          recoverMode = true;

          File f = (_chkpt_file.exists()) ? _chkpt_file : _chkpt_backup_file;
          finishRestartFromCrash(f);
        }
        else {
          recoverMode = false;

          /*
           * If the persistence manager state file does not exist, the
           * object server was not properly shut down during its first run.
           * If this is the case, if no object server state exists, the
           * object server is started as if it is the first time. If there
           * is state, the user is asked to remove it and then to restart
           * the object server. Note that the state cannot be recovered as
           * the persistence-manager's state is lost.
           */
          if (! _pm_state_file.exists()) {
            System.out.println();
            System.out.println("*******************************************");
            System.out.println("* PM state file is absent.");
            System.out.println("*");
            System.out.println("* Checking if any state is present...");
            System.out.println("*******************************************");
            System.out.println();

            if (! haveStateOnDisk()) {                 // no state present
              createStartedProcessState();
            }
            else {                                     // state present
              System.out.println();
              System.out.println("*******************************************");
              System.out.println("* Cannot recover from improper shutdown...");
              System.out.println("*");
              System.out.println("* Cannot repair object server state. Please");
              System.out.println("* remove directory " + _fs_root
                                 + " and restart the server");
              System.out.println("*******************************************");
              System.out.println();

              throw new perstError_io();
            }
          }
          else {
            finishRestart();
          }
        }
      }
    }

    /*
     * Create and start the passivation checkpoint thread.
     */
    if (_persistence_enabled && _chkpt_interval >= 0) {
      _checkpointThread = new PassivationCheckpointThread(this,
                                _chkpt_interval,
                                (!recoverMode && CHECKPOINT_ON_START),
                                _chkpt_file,
                                _chkpt_backup_file,
                                _chkpt_log_file,
                                _chkptFileReaderWriterFactory);
      _checkpointThread.start();
    }
  }


  /**
   * Finish the restart operation. To be invoked when restarting in
   * regular mode.
   */
  private void finishRestart()
    throws perstError_io
  {
    try {
      readPMState(_pm_state_file);
    }
    catch (FileNotFoundException exc) {
      System.err.println("pm: error - persistence manager state file "
                         + "does not exist: " + _pm_state_file.getPath());
      throw new perstError_io();
    }
    catch (IOException exc) {
      exc.printStackTrace();
      System.err.println("pm: error - cannot load persistence manager state");
      throw new perstError_io();
    }

    /*
     * Check the consistency of the pm's state.
     */
    if (! pmStateConsistent(false)) {
      System.err.println("pm: error - pm state is not consistent");
      throw new perstError_io();
    }

    try {
      activateObjects(null);
    }
    catch(Exception e) {
      System.err.println("pm: error - cannot activate objects");
      throw new perstError_io();
    }
  }


  /**
   * Finish the restart operation. To be invoked when restarting in
   * crash recovery mode.
   *
   * @param  checkpointFile  the checkpoint file to be used
   */
  private void finishRestartFromCrash(File checkpointFile)
    throws perstError_io
  {
    ObjSvrPassivationState gosState = null;
    PMFileByteArrayReader pmfReader = null;

    System.out.println();
    System.out.println("*****************************************");
    System.out.println("* Restarting from improper shutdown...");
    System.out.println("*");
    System.out.println("* Using checkpoint file " + checkpointFile.getPath());
    System.out.println("*****************************************");
    System.out.println();

    /*
     * Read the checkpoint file.
     */
    try {
      gosState = readChkptFile(checkpointFile);
    }
    catch(ChkptFileException e) {
      System.err.println("pm: error - checkpoint file "
                         + checkpointFile.getPath()
                         + " is corrupted: " + e.getMessage());
      throw new perstError_io();
    }
    catch(IOException e) {
      System.err.println("pm: error - cannot read checkpoint file "
                         + checkpointFile.getPath() + ": " + e.getMessage());
      throw new perstError_io();
    }

    try {
      /*
       * Use the dumped checkpoint state to initialize the persistence manager.
       */
      System.out.println();
      System.out.println("*****************************************");
      System.out.println("* Recovering persistence manager...");
      System.out.println("*****************************************");
      System.out.println();

      pmfReader = new PMFileByteArrayReader(gosState.pmState.buf,
                                            0, gosState.pmState.length);
      readPMState(pmfReader); 
    }
    catch (IOException exc) {
      exc.printStackTrace();
      System.err.println("pm: error - cannot load persistence manager state");
      throw new perstError_io();
    }
    finally {
      if (pmfReader != null) {
        try {
          pmfReader.close();
        }
        catch(IOException e) {
          System.err.println("pm: error - cannot close pmf reader: "
                             + e.getMessage());
        }
      }
      gosState.pmState.clear();                // no longer needed
    }

    /*
     * Check the consistency of the pm's state.
     */
    if (! pmStateConsistent(true)) {
      System.err.println("pm: error - pm state is not consistent");
      throw new perstError_io();
    }

    /*
     * Use the dumped checkpoint state to initialize and activate the
     * persistent objects.
     */
    System.out.println();
    System.out.println("*****************************************");
    System.out.println("* Recovering persistent objects...");
    System.out.println("*****************************************");
    System.out.println();

    try {
      activateObjects(gosState.poStates);
    }
    catch(Exception e) {
      System.err.println("pm: error - cannot activate objects");
      throw new perstError_io();
    }

    gosState.poStates.clear();                   // no longer needed
  }


  /**
   * Read the checkpoint file, and return its contents (i.e. the state of
   * the persistence manager and the passivation state of the persistent
   * objects).
   *
   * @param  file  the checkpoint file
   * @return the contents of the checkpoint file
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  private ObjSvrPassivationState readChkptFile(File file)
    throws ChkptFileException, IOException
  {
    ChkptFileReader reader = null;

    try {
      reader = _chkptFileReaderWriterFactory.createReader(file);

      System.out.println("pm: reading checkpoint file: creation date: "
                         + reader.getCreationDate());

      return reader.read();
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch(Exception e) {
          System.err.println("pm: error - cannot close checkpoint file: "
                             + e.getMessage());
        }
      }
    }
  }


  /**
   * Activates all persistent objects. If an error occurs during the
   * activation of a non-essential object, the object's activation entry
   * is removed from the rid table. If it is an essential object, an
   * exception is thrown.
   * <p>
   * If the objects' passivation state is given, the objects are activated
   * in recovery mode, regular mode otherwise. The passivation state of an
   * object is the state that was saved during a passivation checkpoint.
   *
   * @param  poStates  array holding passivationState objects (optional)
   *
   * @exception ActivationException  if an error occurred during the
   *                                 activation of an essential object
   */
  private void activateObjects(ArrayList poStates)
    throws ActivationEntry.ActivationException
  {
    ResourceIdent rid;
    PerstEntry entry;
    ActivationEntry aentry;
    passivationState pState;

    // Make a list containing all persistent objects. The list also includes
    // persistent resources. Although object (un)registrations are locked out,
    // resource (de)allocations are not. So take care when using this list.

    ResourceIdent [] rid_list = _persts.getIdentsArray();

    if (rid_list.length == 0) {
      return;
    }

    /*
     * The regular persistent objects must be activated first, then the
     * root persistent object (the object server manager). Otherwise,
     * (un)registrations may occur while the regular objects are being
     * activated.
     */

    /*
     * Move the resource identifier of the root po to the tail of the
     * rid list. This way, the root po will be activated last.
     */
    if (_root_po != ResourceIdent.INVALID_RESOURCE_IDENT) {
      if (moveRidToTail(rid_list, _root_po) == false) {
        System.err.println("pm: error - root po cannot be activated: "
                           + "rid list does not contain root po");
        throw new AssertionFailedException();
      }
    }
    else {
      System.err.println("pm: error - root po cannot be activated: "
                         + "root po rid not set");
      throw new AssertionFailedException();
    }

    // go through the copied list for activation
    for (int i = 0; i < rid_list.length; i++) {

      rid = rid_list [i];

      entry = _persts.lookupEntry (rid);
      if (! (entry instanceof ActivationEntry)) // or null
        continue;
      aentry = (ActivationEntry) entry;

      try {
        if (poStates == null) {                            // regular mode
          activateAnObject (aentry, null);
        }
        else {                                             // recovery mode
          /*
           * Get the passivation (checkpoint) state of the persistent object.
           */
          pState = getPassivationState(poStates, rid);

          if (pState != null) {
            activateAnObject (aentry, pState.state);
          }
          else { 
            if (isEssentialResourceID(rid)) {
              System.err.println("pm: error - cannot activate object with rid "
                                 + rid + ": no passivation state found -- "
                                 + "aborting");
              throw new ActivationEntry.ActivationException();
            }
            else {
              System.err.println("pm: error - cannot activate object with rid "
                                 + rid + ": no passivation state found -- "
                                 + "removing object");
              _persts.removeEntry (rid);
            }
          }
        }
      }
      catch (Exception exc) {
//        exc.printStackTrace();

        if (isEssentialResourceID(rid)) {
          System.err.println ("pm: cannot activate essential object with rid "
                            + rid + " -- aborting");
          throw new ActivationEntry.ActivationException();
        }
        else {
          System.err.println ("pm: cannot activate object with rid " + rid
                              + " -- removing object");

          _persts.removeEntry (rid);
        }
      }
    }
  }


  /**
   * Checks if a persistent object should be activated. If so, does so. If not,
   * the object has already been activated or is still being activated, and
   * this method will wait for activation to complete.
   * <p>
   * If the object's passivation state is given, the object is activated
   * in recovery mode, regular mode otherwise.
   *
   * @param  entry  persistent object's activation entry
   * @param  state  the passivation state of the persistent object; if
   *                set the object is activated in recovery mode
   *
   * @exception ActivationException  if an error occurred during activation
   */
  private void activateAnObject (ActivationEntry entry, rawDef state)
    throws ActivationEntry.ActivationException
  {
    // see the implementation notes about the following

    ResourceIdent rid = entry.getResourceIdent();
    if (! entry.shouldActivate()) { // may throw ActivationException

      DebugOutput.println ("pm: persistent object rid " + String.valueOf (rid) +
      			  " already activated");
      return;
    }

    managedPerstObject po = null;
    try {
      DebugOutput.println ("\npm: Recreating persistent object with rid " +
                        String.valueOf (rid));
      String impl = entry.getImpl();
      String init = entry.getInit();
     
      if (impl.equals("")) {
        po = _perst_obj_creator.createPerstObj(entry.getJAR());
      } else {
        po = _perst_obj_creator.createPerstObj(impl, init);
      }
      DebugOutput.println ("pm: activating persistent object with rid " +
                          rid);

      if (state == null) {                           // regular mode
        po.activate (rid.getIdl());
      }
      else {                                         // recovery mode
        po.activateFromCrash(rid.getIdl(), state);
      }

      entry.setRuntime (po);
      entry.informActivated();
    }
    catch (Exception exc) {
      exc.printStackTrace();

      // cleanup the mess of this partially created object entry.
      if (po != null) {
          po.relInf();
      }
      
      entry.informActivationError();
      throw new ActivationEntry.ActivationException();
    }
  }

  /*
   * Find the passivation state of a persistent object.
   *
   * @param  poStates  array of passivationState objects to look in
   * @param  rid       the persistent object's resource identifier
   * @return the passivation state;
   *         <code>null</code> if the state is not found
   */
  private passivationState getPassivationState(ArrayList poStates,
                                               ResourceIdent rid)
  {
    passivationState pState = null;
    long id = rid.getIdl();

    for (int i = 0; i < poStates.size(); i++) {
      pState = (passivationState)poStates.get(i);

      if (pState.perst_id == id) {
        return pState;
      }
    }
    return null;
  }


  /**
   * Performs the initialisation of a persistent process started for the first
   * time, or a non-persistent process. Specifically, it creates the PCP
   * allocators (provided _tcp_*port is set), and (provided _fs_root is set)
   * creates the persistent state directory (if it does not exist) and
   * a LargeDir object.
   */
  private void createStartedProcessState() throws perstError_io
  {
    // add a light-weight contact point allocator
    if (_tcp_port == -1)
    {
      DebugOutput.println(TCP_SETTING + " not defined. There is no support " +
       "for normal light-weight or persistent contact points.");  
    }
    else
    {
	createPCPAllocators();
    }

    if (_fs_root == null)
      DebugOutput.println (FSROOT_SETTING + " is not defined. There is no " +
              "support for storage resources.");
    else {

      if (_fs_root.exists()) {
        System.out.println ("pm: persistent state directory exists: "
                            + _fs_root);
      }
      else {
        System.out.println ("pm: creating persistent state directory: "
                            + _fs_root);

        if (! _fs_root.mkdirs()) {
          System.err.println ("pm: error creating directory: " + _fs_root);
          throw new perstError_io();
        }
      }

      try {
        _storage_dir = new LargeDir (new File (_fs_root, STORAGE_DIR), "pm",
                                       _storage_capacity, _dir_capacity);
      }
      catch (IOException exc) {
        DebugOutput.println ("pm: error initialising LargeDir: ", exc);
        throw new perstError_io();
      }
    }
  }


  /*=====================================================================*\
   *                       passivation checkpoint                        *
  \*=====================================================================*/

  /**
   * Perform a passivation checkpoint: collect the passivation state of
   * the object server and return it. The passivation state consists of
   * the state of the persistence manager and the passivation state of
   * the persistent objects. Prior to collecting state, the persistent
   * objects are told to pause any activity. When the state has been
   * collected, the persistent objects are told to resume their
   * activities.
   *
   * @return the passivation state
   *
   * @exception  PassivationCheckpointException  if an error occurs
   */
  PassivationCheckpointData performPassivationCheckpoint()
    throws PassivationCheckpointException
  {
    System.out.println ("pm : performing passivation checkpoint...");

    // ASSERT: _persistence_enabled == true

    ObjSvrPassivationState gosPstate = new ObjSvrPassivationState();
    ArrayList posStatus = getPersistentObjectList();

    if (posStatus.size() == 0) {
      throw new PassivationCheckpointException("there are no "
                                               + "persistent objects");
    }

    // Pause the timer resource manager
    _timerRscMgr.pause();

    /*
     * A checkpoint goes through the following steps:
     *
     *  1.    Tell persistent objects to prepare for the passivation
     *        checkpoint.
     *  2(a). Get the passivation state of each persistent object.
     *  2(b). Get the state of the persistence manager.
     *  3.    Tell persistent objects to complete the passivation
     *        checkpoint.
     *
     * Step 3 is always for those persistent objects that have
     * performed step 1.
     */
    try {
      /*
       * Step 1. Tell the persistent objects to prepare for a passivation
       * checkpoint. Any activity should be disabled.
       */
      try {
        preparePassivationCheckpointObjects(posStatus);
      }
      catch(Exception e) {
        System.err.println("pm : error - persistent objects failed to "
                           + "prepare for passivation checkpoint");
        e.printStackTrace();
        throw new PassivationCheckpointException("persistent objects "
                    + "failed to prepare for passivation checkpoint");
      }

      /*
       * Step 2(a). Get the passivation state of each persistent object.
       */
      try {
        gosPstate.poStates = passivationCheckpointObjects(posStatus);
      }
      catch(Exception e) {
        System.err.println("pm : error - persistent objects failed to"
                           + "perform passivation checkpoint");
        e.printStackTrace();
        throw new PassivationCheckpointException("persistent objects "
                    + "failed to perform passivation checkpoint");
      }

      try {
        /*
         * Step 2(b). Get the state of the persistence manager.
         */
        DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                            "pm : collecting state of persistence manager...");

        gosPstate.pmState = getPMState();
      }
      catch(IOException e) {
        System.err.println("pm : error - cannot collect persistence "
                           + "manager state: " + e.getMessage());
        e.printStackTrace();
        throw new PassivationCheckpointException("persistence manager "
                    + "failed to collect state: " + e.getMessage());
      }
      return new PassivationCheckpointData(gosPstate, posStatus);
    }
    finally {

      /*
       * Step 3. Tell the persistent objects that the passivation
       * checkpoint is finished. Any activity that has been paused should
       * be resumed.
       */
      try {
        completePassivationCheckpointObjects(posStatus);
      }
      catch(Exception e) {
        System.err.println("pm : error - cannot complete passivation "
                           + "checkpoint");
        e.printStackTrace();
        throw new PassivationCheckpointException("cannot complete "
                                                 + "passivation checkpoint");
      }

      // Unpause the timer resource manager
      _timerRscMgr.unpause();

      System.out.println ("pm : passivation checkpoint completed");
      System.out.println();
    }
  }


  /**
   * Step 1 of 3 of a passivation checkpoint. Signal all persistent objects
   * specified by the given array to prepare for a passivation checkpoint.
   * The status object (in the given array) associated with a persistent
   * object will indicate whether the object has performed the preparation,
   * even in the case that this method throws an exception.
   * <p>
   * When an exception is thrown, the caller should tell the persistent
   * objects that have prepared for the passivation checkpoint to
   * complete the checkpoint. This way, an object will re-enable its
   * network communication, which it has disabled as part of the
   * preparation step.
   *
   * @param  posStatus  an array holding a
   *                    <code>PassivationCheckpointStatus</code> object for
   *                    each persistent object. When this method returns,
   *                    the status object indicates whether the persistent
   *                    object has successfully prepared the passivation
   *                    checkpoint.
   */  
  private void preparePassivationCheckpointObjects(ArrayList posStatus)
    throws Exception
  {
    PassivationCheckpointStatus stat;
    int i;
    ResourceIdent rid;
    PerstEntry entry;

    System.out.println("pm : passivation checkpoint - persistent objects "
                       + "preparing for checkpoint...");

    /*
     * First, tell the root persistent object (the object server manager)
     * to prepare for the passivation checkpoint.
     */

    // The first entry of posStatus should contain the root po.
    stat = (PassivationCheckpointStatus)posStatus.get(0);
    rid = stat.getResourceIdent();

    if (rid.equals(_root_po) == false) {
      System.err.println("pm : error - assertion failed: root po expected");
      throw new AssertionFailedException();
    }

    entry = _persts.lookupEntry (rid);
    if (! (entry instanceof ActivationEntry)) {
      System.err.println("pm : root po has no activation entry");
    }
    else {
      try {
	// for extra robustness, perform the setPrepared() call even if
	// preparation failed: the object may have partially prepared. 
        stat.setPrepared(true);
        preparePassivationCheckpointObject(rid, (ActivationEntry) entry);
      }
      catch(Exception e) {
        if (isEssentialResourceID(rid)) {
          System.err.println("pm : error - root po failed to prepare for "
                             + "passivation checkpoint");
          throw e;
        }
      }

      /*
       * Wait until all invocations of (un)registerPersistent() have finished.
       */
      _coord.waitUntilRegisterPersistentsFinished();
    }

    /*
     * Next, tell the remaining persistent objects to prepare for the
     * passivation checkpoint.
     */

    int n = posStatus.size() - 1;
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "pm : examining " + n
                        + " regular po's to prepare for passivation...");

    for (i = 1; i < posStatus.size(); i++) {

      stat = (PassivationCheckpointStatus)posStatus.get(i);
      rid = stat.getResourceIdent();

      entry = _persts.lookupEntry (rid);
      if (! (entry instanceof ActivationEntry))
        continue;

      try {
	// for extra robustness, perform the setPrepared() call even if
	// preparation failed: the object may have partially prepared. 
        stat.setPrepared(true);
        preparePassivationCheckpointObject(rid, (ActivationEntry) entry);

      }
      catch(Exception e) {
        throw e;
      }
    }
  }


  /**
   * Tell an object to prepare for a passivation checkpoint.
   *
   * @param  rid     object's resource identifier
   * @param  aentry  object's activation entry
   */
  private void preparePassivationCheckpointObject(ResourceIdent rid,
                                                  ActivationEntry aentry)
    throws Exception
  {
    DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                         "pm : telling object with rid " + rid
                         + " to prepare for passivation checkpoint");

    managedPerstObject po = null;

    try {
      po = aentry.getRuntime();

      po.preparePassivationCheckpoint();
    }
    catch (Exception e) {
      System.err.println("pm : error - object with rid " + rid
                         + " failed to prepare for passivation checkpoint");
      e.printStackTrace();
      throw e;
    }
    finally {
      // Release my runtime infs to the object.
      if (po != null) {
        po.relInf();
      }
    }
  }


  /**
   * Step 2 of 3 of a passivation checkpoint. Signal the persistent
   * objects specified by the given array to perform a passivation
   * checkpoint. The status object (in the given array) associated with
   * a persistent object indicates whether the object has successfully
   * performed the previous step: preparing for a passivation checkpoint.
   * If an object has not performed this step, it will be skipped. Upon
   * return of this method, even when an exception is thrown, the status
   * object will indicate whether the object has successfully performed
   * the checkpoint.
   * <p>
   * When an exception is thrown, the caller should tell the persistent
   * objects which have performed the passivation checkpoint, to complete
   * the checkpoint. This way, an object will re-enable its network
   * communication, which it has disabled as part of the preparation
   * step (step 1).
   * <p>
   * An exception is thrown if an essential persistent object (e.g.,
   * the object server manager) fails to perform the passivation
   * checkpoint.
   *
   * @param  posStatus  an array holding a
   *                    <code>PassivationCheckpointStatus</code> object for
   *                    each persistent object. When this method returns,
   *                    the status object indicates whether the persistent
   *                    object has successfully performed the passivation
   *                    checkpoint.
   * @return an array holding the passivation state (as a
   *         <code>passivationState</code> object) of each persistent object
   */
  private ArrayList passivationCheckpointObjects(ArrayList posStatus)
    throws Exception
  {
    ArrayList ar = new ArrayList();
    managedPerstObject po;

    System.out.println("pm : passivation checkpoint - persistent objects "
                       + "collecting state...");

    for (int i = 0; i < posStatus.size(); i++) {
      PassivationCheckpointStatus stat =
        (PassivationCheckpointStatus)posStatus.get(i);

      // Skip object if it has not prepared for the checkpoint.
      if (stat.hasPrepared() == false) {
        continue;
      }

      ResourceIdent rid = stat.getResourceIdent();
      PerstEntry entry = _persts.lookupEntry (rid);
      if (! (entry instanceof ActivationEntry))
        continue;
      ActivationEntry aentry = (ActivationEntry) entry;
      
      DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                           "pm : collecting passivation state of persistent "
                           + "object with rid " + rid);

      po = null;

      try {
        po = aentry.getRuntime();

        passivationState pState = po.passivationCheckpoint();

        // ASSERT: pState.perst_id == rid.getIdl();

      	ar.add(pState);
        stat.setPerformed(true);                        // ok
      }
      catch(Exception e) {
        /*
         * Abort checkpoint if the object that failed is the object
         * server manager, otherwise continue with next object.
         */

        if (isEssentialResourceID(rid)) {
          System.err.println("pm : error - cannot get passivation state "
                             + "of essential object with rid " + rid
                             + " -- aborting checkpoint");
          e.printStackTrace();
          throw e;
        }
        else {
          System.err.println("pm : error - cannot get passivation state "
                             + "of object with rid " + rid
                             + " -- skipping object");
          e.printStackTrace();
        }
      }
      finally {
        // Release my runtime infs to the object.
        if (po != null) {
          po.relInf();
        }
      }
    }
    return ar;
  }


  /**
   * Step 3 of 3 of a passivation checkpoint. Signal the persistent
   * objects specified by the given array to complete a passivation
   * checkpoint. This step is the complement of step 1 (preparing for
   * a passivation checkpoint). The status object (in the given array)
   * associated with a persistent object indicates whether the object
   * has successfully performed step 1. If an object has not performed
   * this step, it will be skipped. Upon return of this method, even
   * when an exception is thrown, the status object will indicate whether
   * the object has successfully completed the passivation checkpoint.
   * <p>
   * An exception is thrown if an essential persistent object (e.g.,
   * the object server manager) fails to complete the passivation
   * checkpoint.
   *
   * @param  posStatus  an array holding a
   *                    <code>PassivationCheckpointStatus</code> object for
   *                    each persistent object. When this method returns,
   *                    the status object indicates whether the persistent
   *                    object has successfully completed the passivation
   *                    checkpoint.
   */  
  private void completePassivationCheckpointObjects(ArrayList posStatus)
    throws Exception
  {
    managedPerstObject po;

    System.out.println("pm : passivation checkpoint - persistent objects "
                       + "finishing checkpoint...");

    /*
     * First tell the regular persistent objects to complete their
     * passivation checkpoint, then the root persistent object. The
     * root persistent object is stored in entry 0 of posStatus.
     */

    for (int i = posStatus.size() - 1; i >= 0; i--) {
      PassivationCheckpointStatus stat =
        (PassivationCheckpointStatus)posStatus.get(i);

      // Skip object if it has not prepared for the checkpoint.
      if (stat.hasPrepared() == false) {
        continue;
      }

      ResourceIdent rid = stat.getResourceIdent();
      PerstEntry entry = _persts.lookupEntry (rid);
      if (! (entry instanceof ActivationEntry))
        continue;
      ActivationEntry aentry = (ActivationEntry) entry;
      
      DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                           "pm : telling object with rid " + rid
                           + " to complete passivation checkpoint");

      po = null;

      try {
        po = aentry.getRuntime();

        po.completePassivationCheckpoint();

        stat.setCompleted(true);                         // ok
      }
      catch(Exception e) {

        /*
         * Abort if the object that failed is the object
         * server manager, otherwise continue with next object.
         */

        if (isEssentialResourceID(rid)) {
          System.err.println("pm : error - essential object with rid " + rid
                             + " failed to complete passivation checkpoint"
                             + " -- aborting checkpoint");
          e.printStackTrace();
          throw e;
        }
        else {
          System.err.println("pm : error - object with rid " + rid
                             + " failed to complete passivation checkpoint"
                             + " -- skipping object");
          e.printStackTrace();
        }
      }
      finally {
        // Release my runtime infs to the object.
        if (po != null) {
          po.relInf();
        }
      }
    }
  }


  /**
   * Return an array with a <code>PassivationCheckpointStatus</code>
   * object for each persistent object. If the root persistent object
   * is defined, it is stored in entry 0 of the array.
   */
  private ArrayList getPersistentObjectList()
  {
    ResourceIdent rid_list[], rid;
    ArrayList posStatus;
    PerstEntry entry;
    PassivationCheckpointStatus poStatus, poRootStatus = null;

    rid_list = _persts.getIdentsArray();
    posStatus = new ArrayList();

    for (int i = 0; i < rid_list.length; i++) {

      rid = rid_list [i];
      entry = _persts.lookupEntry (rid);
      if (! (entry instanceof ActivationEntry))  // or entry==null
        continue;

      poStatus = new PassivationCheckpointStatus(rid);

      if (rid.equals(_root_po) == false) {
        posStatus.add(poStatus);
      }
      else {
        poRootStatus = poStatus;
      }
    }

    if (poRootStatus != null) {
      posStatus.add(0, poRootStatus);
    }

    return posStatus;
  }


  /**
   * Remove the checkpoint file and the files related to it. The absence
   * of the checkpoint file indicates that the object server has
   * gracefully shut down. If the checkpoint file is successfully removed,
   * the checkpoint log file is removed (if any).
   */
  private void removeCheckpointFiles()
  {
    /*
     * Remove backup file.
     */
    try {
      removeFile(_chkpt_backup_file, "checkpoint backup file");
    }
    catch(IOException e) {
      System.err.println("pm: error - cannot remove checkpoint backup file: "
                         + e.getMessage());
    }

    /*
     * Remove checkpoint file and checkpoint log.
     */
    try {
      removeFile(_chkpt_file, "checkpoint file");

      if (_chkpt_log_file != null) {
        try {
          removeFile(_chkpt_log_file, "checkpoint log file");
        }
        catch(IOException e) {
          System.err.println("pm: error - cannot remove checkpoint "
                             + "log file: " + e.getMessage());
        }
      }
    }
    catch(IOException e) {
      System.err.println("pm: error - cannot remove checkpoint file: "
                         + e.getMessage());
    }
  }


  /*=====================================================================*\
   *                            PM state                                 *
  \*=====================================================================*/

  /**
   * Reads the PM's persistent state: the root po, the allocation state of
   * resource ids, and the contents of the rid table.
   *
   * @param  file  PM file to be read
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void readPMState(File file)
    throws IOException
  {
    PMFileReader pmf = null;

    try {
      pmf = new PMFileReader (file);
      readPMState(pmf);
    }
    finally {
      if (pmf != null) {
        pmf.close();
        pmf = null;
      }
    }
  }

  /**
   * Reads the PM's persistent state: the root po, the allocation state of
   * resource ids, and the contents of the rid table.
   *
   * @param  pmf  reader for the PM file to be read (to be closed by
   *              the caller)
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void readPMState(PMFileReader pmf)
    throws IOException
  {
    DebugOutput.println ("pm: reading pm's persistent state");
     
    try {

      // loop over the entries in the pmf
      while (true) {
        PMFile.PMFEntry pmf_entry = pmf.read();
	if (pmf_entry == null) {
	  break; // eof
        }

	switch (pmf_entry.entry_type) {
	  case PMFile.PMF_ROOT_PO:

	    DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                                 "pm: reading root po's rid");

            // Read root's rid. An 'invalid rid' means there is no root.

	    if (pmf_entry.entry_fields.length != 1)
              throw new IOException ("incorrect number of root po fields");
            try {
              _root_po = new ResourceIdent (pmf_entry.entry_fields [0]);

              // Mark the root po as essential.
              registerEssentialResourceID(_root_po.getIdl());
            }
            catch (IllegalArgumentException exc) {
              throw new IOException ("illegal root po");
            }
	    break;
	  case PMFile.PMF_OPSYS_CONTACT:
	    DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                                 "pm: reading an opsys contact point");
	    _pcp_allocs.unmarshallOpsysContact (pmf_entry.entry_fields);
	    break;
	  case PMFile.PMF_RID_ALLOCATION:
	    DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                                 "pm: reading the rid allocation state");
	    _persts.unmarshallRidAllocation (pmf_entry.entry_fields);
	    break;
	  case PMFile.PMF_STORAGE:
	    DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                                 "pm: reading a storage resource");
	    _persts.unmarshallStorage (pmf_entry.entry_fields);
	    break;
	  case PMFile.PMF_ACTIVATION:
	    DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                                 "pm: reading an activation record");
	    _persts.unmarshallActivation (pmf_entry.entry_fields);
	    break;
	  case PMFile.PMF_LW_CONTACT:
	    DebugOutput.println (DebugOutput.DBG_DEBUGPLUS,
                                 "pm: reading a light-weight contact point");
	    _persts.unmarshallContact (pmf_entry.entry_fields);
	    break;
	  default:
	    throw new IOException ("unknown entry type");
	}
      }
    }
    catch (IOException exc) {
      System.err.println (pmf.makeReadError (exc.getMessage()));
      throw exc;
    }
    catch (IllegalArgumentException exc) { // one of the unmarshall calls
      System.err.println (pmf.makeReadError (exc.getMessage()));
      exc.printStackTrace();
      throw new IOException (exc.getMessage());
    }
  }

  /**
   * Verifies that the PM's state is consistent.
   *
   * @param  recoverMode  set if invoked during recover mode
   * @return true         iff consistent
   */
  private boolean pmStateConsistent(boolean recoverMode)
  {
    // check that the root po (if it exists) refers to a valid object entry
    if (! _root_po.equals (ResourceIdent.INVALID_RESOURCE_IDENT)) {

      PerstEntry entry = _persts.lookupEntry (_root_po);
      if (! (entry instanceof ActivationEntry)) { // or null
        System.err.println ("pm: root persistent object invalid");
        return false;
      }
    }

    /*
      Traverse persistent objects, checking that each po's state refers to
      a valid storage entry, and that, in regular mode, each storage
      entry refers to an existing file. We take care to traverse
      safely (probably not even necessary).
    */

    synchronized (_persts) {

      Iterator rids = _persts.getIdents();
      for (int i = 0; rids.hasNext();) {

        ResourceIdent rid = (ResourceIdent) rids.next();
        PerstEntry entry = _persts.lookupEntry (rid);

        if (entry instanceof ActivationEntry) {

          ResourceIdent state_rid = ((ActivationEntry) entry).getState();
          PerstEntry state_entry = _persts.lookupEntry (state_rid);

          if (! (state_entry instanceof StorageEntry)) { // or null
            System.err.println ("pm: persistent object (rid "
                                + rid.toString()
                                + ") does not have a valid state field");
            return false;
          }
        }
        else if (entry instanceof StorageEntry) {
          /*
           * In regular mode, check if the storage resource refers to a
           * valid file. This check cannot be performed in crash recovery
           * mode because not all storage files may have been created
           * (the creation of certain storage files is delayed until
           * the object server is shut down gracefully).
           */
          if (! recoverMode) {
	    String filename = ((StorageEntry) entry).getStorage();
            File file = _storage_dir.mapToFile (filename);
            // is it a regular existing file with the correct permissions?
            if (! ( file.isFile() && file.canRead() && file.canWrite())) {
              System.err.println ("pm: storage resource (rid "
                                  + rid.toString()
                                  + ") does not refer to a valid file");
              return false;
            }
          }
        }
	else if (! (entry instanceof ContactEntry)) {
	  System.err.println ("pm: rid " + rid.toString() + " refers to " +
	  	"unknown persistent resource or object");
	  return false;
	  
	}
      }
    }
    return true;
  }


  /**
   * Writes the PM's persistent state: the root po, the contents of the rid
   * table (except transient resources), and the contents of the
   * PCP allocator table.
   * <p>
   * Assumes all persistent objects have passivated or have disabled
   * activity such as network communication.
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writePMState(File file) throws IOException
  {
    DebugOutput.println ("pm: saving pm's state to file " + file.getName());

    PMFileWriter pmf = null;

    // open a pmf for writing
    try {
      pmf = new PMFileWriter (file);
      writePMState(pmf);
    }
    finally {
      if (pmf != null) {
        pmf.close();
      }
      DebugOutput.println ("pm: saved pm's state");
    }
  }

  /**
   * Writes the PM's persistent state by using the given PM file writer.
   * <p>
   * Assumes all persistent objects have passivated or have disabled
   * activity such as network communication.
   *
   * @param pmf  PMFile writer (to be closed by the caller)
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writePMState(PMFileWriter pmf)
    throws IOException
  {
    // Since all persistent objects have passivated or have been disabled,
    // and (un)registration of the root persistent object is now impossible,
    // it is safe to assume that the persistent objects and resources in
    // the rid table are stable. We don't really care whether transient
    // resources are stable, as long as they don't mess up the saved state.

    // Write root's rid, even if invalid.
    pmf.write (PMFileWriter.PMF_ROOT_PO, _root_po.toString());

    // Write the rid table, excluding transient resources
    _persts.marshall (pmf);

    // Write the pcp allocator table
    _pcp_allocs.marshall (pmf);
  }


  /**
   * Returns the PM's persistent state: the root po, the contents of the
   * rid table (except transient resources), and the contents of the PCP
   * allocator table.
   * <p>
   * Assumes all persistent objects have passivated or have disabled
   * activity such as network communication.
   *
   * @return  an <code>ByteArray</code> object holding the state 
   *
   * @exception  IOException  if an I/O error occurs
   */
  private ByteArray getPMState()
    throws IOException
  {
    DebugOutput.println ("pm : collecting pm's state...");

    PMFileByteArrayWriter pmf = new PMFileByteArrayWriter();

    try {
      writePMState(pmf);
      return pmf.getByteArray();
    }
    finally {
      pmf.close();
      pmf = null;

      DebugOutput.println ("pm : finished collecting pm's state");
    }
  }


  /*=====================================================================*\
   *                          miscellaneous                              *
  \*=====================================================================*/

  /**
   * Look for the specified resource identifier in an array, and move it
   * to the front entry of the array. Any subsequent rids are shifted to the
   * right (adds one from their indices).
   *
   * @param  rid_list  array of resource identifiers
   * @param  tgt       the target resource identifier to be moved
   * @return <code>true</code>;
   *         <code>false</code> if the resource identifier is not in the array
   */
  private boolean moveRidToFront(ResourceIdent rid_list[], ResourceIdent tgt)
  {
    ResourceIdent rid;

    for (int i = 0; i < rid_list.length; i++) {
      rid = rid_list[i];

      if (rid.equals(tgt)) {
        if (i != 0) {
          // shift elements
          for (int j = i; j > 0; j--) {
            rid_list[j] = rid_list[j - 1];
          }

          // insert element
          rid_list[0] = rid;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Look for the specified resource identifier in an array, and move it
   * to the tail entry of the array. Any subsequent rids are shifted to the
   * left (subtracts one from their indices).
   *
   * @param  rid_list  array of resource identifiers
   * @param  tgt       the target resource identifier to be moved
   * @return <code>true</code>;
   *         <code>false</code> if the resource identifier is not in the array
   */
  private boolean moveRidToTail(ResourceIdent rid_list[], ResourceIdent tgt)
  {
    ResourceIdent rid;

    for (int i = 0; i < rid_list.length; i++) {
      rid = rid_list[i];

      if (rid.equals(tgt)) {
        if (i != rid_list.length - 1) {
          // shift elements
          for (int j = i; j < rid_list.length - 1; j++) {
            rid_list[j] = rid_list[j + 1];
          }

          // insert element
          rid_list[rid_list.length - 1] = rid;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Remove a file and display an output message. This is a no-op if
   * the file does not exist.
   *
   * @param  file  file to be removed
   * @param  s     string to be displayed in the output message
   *
   * @exception  IOException  if an error occurs
   */
  private void removeFile(File file, String s)
    throws IOException
  {
    System.out.println("pm: removing " + s + " " + file.getPath());

    if (! file.delete()) {
      if (file.exists()) {
        throw new IOException(file.getPath());
      }
    }
  }


  /**
   * Returns true if the object server has persistent state on disk.
   */
  private boolean haveStateOnDisk()
  {
    File files[] = _fs_root.listFiles();

    /*
     * If the persistent state directory has an entry, it must be the
     * entry for the storage directory (which must be empty).
     */
    switch(files.length) {
      case 0:
        return false;
      case 1:
        return !(files[0].equals(_storage_dir.getFile())
                 && isEmptyDirectoryTree(_storage_dir.getFile()));
      default:
        return true;
    }
  }


  /**
   * Returns true if the named directory (including its subdirectories)
   * does not contain a file.
   */
  private boolean isEmptyDirectoryTree(File dir)
  {
    File files[] = dir.listFiles();

    if (files.length == 0) {
      return true;
    }

    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {             // check subdirectory
        if (! isEmptyDirectoryTree(files[i])) {
          return false;
        }
      }
      else {                                    // a file
        return false;
      }
    }
    return true;
  }


  /**
   * Adds to _pcp_allocs light-weight TCP/IP based contact point allocators,
   * allocating at the three _tcp*_ports and, if set, _ip_address. Note that
   * _pcp_allocs will save and restore the allocators. Therefore this call
   * should only be made when PM is started for the very first time.
   */
  private void createPCPAllocators()
  {
    // TCP/IP address
    String opsys_contact;
    try 
    {
      InetAddress ip = (_ip_address != null) ? _ip_address : 
                                               InetAddress.getLocalHost();

      ProtAddress pa = new ProtAddress(ProtAddress.IP_PROT,ip.getHostAddress());
      pa.add( ProtAddress.TCP_PROT, String.valueOf(_tcp_port) );
      opsys_contact = pa.toString();
    }
    catch (UnknownHostException exc) {
      throw new AssertionFailedException();
    }
    catch (IllegalArgumentException exc) { // ProtAddress
      throw new AssertionFailedException();
    }

    _pcp_allocs.create( P2PDefs.TCP_MUX_SEC_STACK, opsys_contact,
                        P2PDefs.TCP_MUX_LISR_IMPL, 
                        P2PDefs.TCP_MUX_LISR_INIT);
  }
  

  /**
   * Coordinates the various PM methods for thread-safety. See the
   * implementation notes.
   */
  static class Coordinator
  {
    /** The number of current (un)registerPersistent method executions. */
    private int _nregs = 0;

    /** The number of persistent objects that have passivated. */
    private int _n_passivated = 0;

    /**
     * Must be called at the start of a registerPersistent method execution.
     *
     * @exception perstError_shuttingDown   if the PM has started shutting down
     */
    public void beginRegister() throws perstError_shuttingDown
    {
      synchronized (this) {
	_nregs++;
      }
    }

    /**
     * Must be called at the end of a registerPersistent method execution.
     */
    public void endRegister()
    {
      synchronized (this) {
        if (--_nregs == 0)
	  this.notifyAll();              // waitUntilRegisterPersistents
      }
    }

    /**
     * Must be called at the start of an unregisterPersistent method execution.
     *
     * @exception perstError_shuttingDown   if the PM has started shutting down
     */
    public void beginUnregister() throws perstError_shuttingDown
    {
      synchronized (this) {
	_nregs++;
      }
    }


    /**
     * Must be called at the end of an unregisterPersistent method execution.
     */
    public void endUnregister()
    {
      synchronized (this) {
        if (--_nregs == 0)
	  this.notifyAll();             // waitUntilRegisterPersistentsFinished
      }
    }


    /**
     * Called by shutdown and performPassivationCheckpoint to wait until
     * all (un)registerPesistents have finished.
     */
    public void waitUntilRegisterPersistentsFinished()
    {
      synchronized (this) {

        int n = 0;

        while (_nregs != 0) {
          if (n != _nregs) {
            n = _nregs;
            DebugOutput.println("pm: waiting until all (un)registrations "
                                + "have finished: now " + n);
          }

	  try { this.wait(); } // for endUnregister/endRegister
	  catch (InterruptedException exc) {
	    System.err.println ("Ignoring interruption.");
	  }
	}
	
	DebugOutput.dassert (_nregs >= 0);
      }
    }


    /**
     * Called by shutdown to wait until all persistent objects have passivated.
     *
     * @param n_objects    the number of persistent objects passivating
     */
    public void waitUntilObjectsPassivated (int n_objects)
    {
      // prod any Globe objects which have not been properly released
      System.gc();

      synchronized (this) {

        int n = _n_passivated;

	while (_n_passivated != n_objects) {
          if (n != _n_passivated) {
            n = _n_passivated;
            int m = n_objects - n;
            System.err.println("pm: waiting until all persistent objects "
                                + "have passivated: now " + n);
          }

	  try { this.wait(); }    // for informObjectHasPassivated
	  catch (InterruptedException exc) {
	    System.err.println ("Ignoring interruption.");
	  }
	}
      }
    }

    /**
     * Called by informPassivated to indicate that a persistent object has
     * finished passivating.
     */
    public void informObjectHasPassivated()
    {
      synchronized (this) {
        _n_passivated++;
        this.notifyAll();   // waitUntilObjectsPassivated
      }
    }
  }

  /**
   * Creates storage objects under the _pm_ctx context. Thread-safe.
   */
  class StorageObjCreator
  {
    /** Used to construct a new local name for a storage object. */
    private int _counter = 0;

    /*
     * Constructs a new storage object for a given resource id.
     * @see StorageObject.StorageObject(ResourceIdent, File)
     * 
     * @exception IOException
     *    @see StorageObject.StorageObject(ResourceIdent, File)
     * @exception Exception
     *   a Globe exception during instance creation or name space access.
     */
    public storage createStorageObj (ResourceIdent rid, File file)
                     throws Exception
    {
      Storage storage_obj = new Storage (rid, file);

      // return the storage object as a proper Globe object
      SOInf storage_soi = storage_obj.getSOI();
      StdUtil.initGlobeObject (storage_soi, _pm_ctx, nextName ());
      return (storage) storage_soi.swapInf (storage.infid);
    }

    /**
     * Generates a new local name for a storage object.
     */
    private synchronized String nextName()
    {
      return STORAGE_PREFIX + String.valueOf (_counter++);
    }
  }

  /**
   * Creates persistent objects under the _pm_ctx context. The objects are
   * created by their implementation handle and initialisation string.
   * Thread-safe.
   */
  class PerstObjCreator
  {
    // implementation repository
    private repository _repos;       // counted

    /** Used to construct a new local name for a persistent object. */
    private int _counter = 0;

    /** The prefix to name persistent objects with in the local name space. */
    private static final String PERST_PREFIX = "perst";

 
    /**
     * Constructor. When the creator of this object is done, removeClient()
     * should be called.
     *
     * @exception Exception
     *   a Globe exception during instance creation or name space access.
     */
    public PerstObjCreator() throws Exception
    {
      SOInf soi = (SOInf) _lns.bind(context.NsRootCtx, nsConst.IMPL_REPOS_NAME);
      _repos = (repository) soi.swapInf (repository.infid);
    }

 
    /** Releases any Globe references held. */
    public void removeClient()
    {
      _repos.relInf();
    }

    /*
     * Constructs a new persistent object by its implementation handle and
     * initialisation string.
     * 
     * @exception IllegalArgumentException
     *   if the implementation handle was not recognised by the repository
     * @exception Exception
     *   an unexpected Globe exception during creation.
     */
    public managedPerstObject createPerstObj (String impl, String init)
    			throws Exception
    {
      try {
        // convert the implementation handle to a class object
        SOInf class_soi = _repos.map (impl);
        SCInf sci = (SCInf) class_soi.swapInf (SCInf.infid);

        // create an instance and configure it
	SOInf soi = StdUtil.createGlobeObject (sci, _pm_ctx, nextName());
	configurable cfg = (configurable) soi.getUncountedInf (configurable.infid);
        cfg.configure (init);

        return (managedPerstObject) soi.swapInf (managedPerstObject.infid);
      }
      catch (repositoryErrors exc) {
        System.err.println ("pm: repository unable to find an implementation");
	exc.printStackTrace();
	throw new IllegalArgumentException ("impl");
      }
    }

    public managedPerstObject createPerstObj (rawDef jar)
                        throws Exception
    {
      try {
        // Create the LRMgrConfig from the jar
        IDLLRMgrConfig cfg = null;
        cfg = LRSecUtil.fromJar(jar, null, null, false, 
                                ObjectServerMain.securityEnabled);

        // convert the implementation handle to a class object
        SOInf class_soi = _repos.map (cfg.tocreatebp.lrmgr_impl);
        SCInf sci = (SCInf) class_soi.swapInf (SCInf.infid);

        // create an instance and configure it
        SOInf soi = StdUtil.createGlobeObject (sci, _pm_ctx, nextName());

        constructorLRMgr lrConstr;
        lrConstr = (constructorLRMgr) 
                                soi.getUncountedInf(constructorLRMgr.infid);
        lrConstr.constructor(jar, cfg);

        return (managedPerstObject) soi.swapInf (managedPerstObject.infid);
      }
      catch (repositoryErrors exc) {
        System.err.println ("pm: repository unable to find an implementation");
        exc.printStackTrace();
        throw new IllegalArgumentException ("impl");
      }
    }


    
    /**
     * Generates a new local name for a persistent object.
     */
    private synchronized String nextName()
    {
      return PERST_PREFIX + String.valueOf (_counter++);
    }
  }
}
