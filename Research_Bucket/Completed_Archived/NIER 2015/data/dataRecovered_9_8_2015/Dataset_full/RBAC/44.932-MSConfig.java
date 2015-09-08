/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.lr.replication.MasterSlave;

import vu.globe.util.parse.AttributeString;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import vu.globe.rts.lr.replication.std.protocol.*;
import vu.globe.rts.security.certs.AccessCtrlBitmap;


/**
 * Defines the configuration string expected by this particular replication
 * object implementation. Attributes are encoded as defined by AttributeString.
 *
 * @author Patrick Verkaik
 * @author Egon Amade (added security stuff)
 * @author Arno Bakker (upgraded security stuff)
 */
 
public class MSConfig
{

    /** The master/slave replication object implementation handles. */
    public static final String IMPLHANDLE_MASTER_SLAVE_REPL_CLIENT = "JAVA;vu.globe.rts.lr.replication.MasterSlave.msClientCO";
    public static final String IMPLHANDLE_MASTER_SLAVE_REPL_SLAVE = "JAVA;vu.globe.rts.lr.replication.MasterSlave.msSlaveCO";
    public static final String IMPLHANDLE_MASTER_SLAVE_REPL_MASTER = "JAVA;vu.globe.rts.lr.replication.MasterSlave.msMasterCO";

  /**
   * The name of the attribute which specifies whether a master should offer
   * a slave contact address. If set, the master will only offer a client
   * contact address, resulting Client/Server replication. The value of the
   * attribute should be NO_SLAVE_TRUE or NO_SLAVE_FALSE. The attribute is
   * only required by the master replication object.
   */
  public static final String NO_SLAVE = "no-slave";

  /**
   * If the NO_SLAVE attribute has this value, a master replication object
   * will only offer a client contact address.
   */
  public static final String NO_SLAVE_TRUE = "true";

  /**
   * If the NO_SLAVE attribute has this value, a master replication object
   * will offer a client contact address as well as a slave contact address.
   */
  public static final String NO_SLAVE_FALSE = "false";


  /**
   * The offset to use to determine the methodID number for our replication
   * messages.
   */
  public static final String REPL_OFFSET = "repl-offset";

  /**
   * Message when credentials are revoked.
   */
  public static final String CREDS_REVOKED = "ERROR: Credentials revoked";

  
  // AUTOREPL
  /**
   * The name of the attribute which specifies whether automatic replication
   * should be used. The value is a stringified boolean.
   */
  public static final String AUTOREPL_ENABLED = "autorepl-enabled";
  public static final String AUTOREPL_ENABLED_TRUE =  "true";
  public static final String AUTOREPL_ENABLED_FALSE = "false";

  /**
   * The name of the attribute which specifies the string-encoded replication
   * policy.
   */
  public static final String AUTOREPL_POLICY = "autorepl-policy";

  /**
   * Utility method which creates the configuration string expected by a
   *  client replication object.
   */
  public static String createClientConfig(int replOffset, 
                                          boolean AutoReplicationEnabled)
  {
    AttributeString cfg = new AttributeString();
    cfg.put(REPL_OFFSET, new Integer(replOffset).toString());

    // AUTOREPL
    cfg.put(AUTOREPL_ENABLED, AutoReplicationEnabled ? AUTOREPL_ENABLED_TRUE 
                                                     : AUTOREPL_ENABLED_FALSE);
    
    return cfg.encode();
  }

  public static String createSlaveConfig(int replOffset)
  {
    AttributeString cfg = new AttributeString();
    cfg.put(REPL_OFFSET, new Integer(replOffset).toString());
    cfg.put(AUTOREPL_ENABLED, AUTOREPL_ENABLED_FALSE );
    return cfg.encode();
  }

 
  // AUTOREPL
  // Used by master in contact addresses to be used by slaves
  public static String createSlaveConfig(int replOffset,
                                         String AutoReplicationPolicy)
  {
    AttributeString cfg = new AttributeString();
    cfg.put (REPL_OFFSET, new Integer(replOffset).toString());
    cfg.put(AUTOREPL_ENABLED, AUTOREPL_ENABLED_TRUE );
    cfg.put (AUTOREPL_POLICY, AutoReplicationPolicy );
    return cfg.encode();
  }


  /**
   * Utility method which creates the configuration string expected by a
   * master replication object.
   * <p>
   *
   * @param no_slave	whether the master replication object should offer a
   *			slave contact address
   * 
   * Used by object-creating clients for C/S and non-automatic M/S replicated
   */
  public static String createMasterConfig(int replOffset, boolean no_slave )
  {
    AttributeString cfg = new AttributeString();
    cfg.put (REPL_OFFSET, new Integer(replOffset).toString());
    cfg.put (NO_SLAVE, no_slave ? NO_SLAVE_TRUE : NO_SLAVE_FALSE);

    return cfg.encode();
  }


  /** //AUTOREPL
   * Used for auto M/S objects.
   */ 
  public static String createMasterConfig(int replOffset, boolean no_slave, 
                                          String ReplicationPolicy )
  {
    AttributeString cfg = new AttributeString();
    cfg.put (REPL_OFFSET, new Integer(replOffset).toString());
    cfg.put (NO_SLAVE, no_slave ? NO_SLAVE_TRUE : NO_SLAVE_FALSE);

    if (!no_slave)
    {
        cfg.put (AUTOREPL_ENABLED, AUTOREPL_ENABLED_TRUE );
	cfg.put (AUTOREPL_POLICY, ReplicationPolicy );
    }

    return cfg.encode();
  }

  /**************** NEW SECURITY STUFF STARTS HERE **************************/
    public static final int CS = 0; // client/server
    public static final int MS = 1; // master/slave (auto and nonauto)
  
    public static int NUMBER_OF_REPLICATION_METHODS = 0; // automatically set
    public static final int MASTER_ROLE=0;
    public static final int SLAVE_ROLE=1;
    public static final int CLIENT_ROLE=2;

    public static ArrayList _singletonReverseMethodIDMap;
    public static final String METHOD_REGISTER_CLIENT =
        Integer.toString(ReplMessageFact.MSG_REGISTERCLIENT_REQ);
    public static final String METHOD_REGISTER_SLAVE =
        Integer.toString(ReplMessageFact.MSG_REGISTERSLAVE_REQ);
    public static final String METHOD_GET_STATE_VERSION =
        Integer.toString(ReplMessageFact.MSG_GETSTATEVERSION_REQ);
    public static final String METHOD_SEND_ME_STATE =
        Integer.toString(ReplMessageFact.MSG_SEND_ME_STATE);
    public static final String METHOD_INVALIDATE =
        Integer.toString(ReplMessageFact.MSG_INVALIDATE);
    public static final String METHOD_SEND_STATE =
        Integer.toString(ReplMessageFact.MSG_SEND_STATE);
    public static final String METHOD_REPORT_STATS =
        Integer.toString(ReplMessageFact.MSG_AUTOREPL_SIMPLEACCESSPATT);

    static
    {
        _singletonReverseMethodIDMap = new ArrayList();
        // Client methods
        _singletonReverseMethodIDMap.add(NUMBER_OF_REPLICATION_METHODS++,
                                         METHOD_REGISTER_CLIENT);

        // Slave methods
        _singletonReverseMethodIDMap.add(NUMBER_OF_REPLICATION_METHODS++,
                                         METHOD_REGISTER_SLAVE);
        _singletonReverseMethodIDMap.add(NUMBER_OF_REPLICATION_METHODS++,
                                         METHOD_GET_STATE_VERSION);
        _singletonReverseMethodIDMap.add(NUMBER_OF_REPLICATION_METHODS++,
                                         METHOD_SEND_ME_STATE);
        _singletonReverseMethodIDMap.add(NUMBER_OF_REPLICATION_METHODS++,
                                         METHOD_REPORT_STATS);
        

        // Master methods
        _singletonReverseMethodIDMap.add(NUMBER_OF_REPLICATION_METHODS++,
                                         METHOD_SEND_STATE);
        _singletonReverseMethodIDMap.add(NUMBER_OF_REPLICATION_METHODS++,
                                         METHOD_INVALIDATE);
    }


    public static List getReverseMethodIDMap()
    {
        return _singletonReverseMethodIDMap;
    }

    public static AccessCtrlBitmap[] getSecurityRequirements(int mode,int role)
    {
        AccessCtrlBitmap facb = new AccessCtrlBitmap( NUMBER_OF_REPLICATION_METHODS );
        AccessCtrlBitmap racb = new AccessCtrlBitmap( NUMBER_OF_REPLICATION_METHODS );
        // Construct (forward) MethodIDMap
        Hashtable h = new Hashtable();
        for (int i=0; i<_singletonReverseMethodIDMap.size(); i++)
        {
            h.put( _singletonReverseMethodIDMap.get( i ), new Integer( i ) );
        }

        if (mode == MS) {
            if (role == MASTER_ROLE)
            {
                // master can only invalidate and send state
                facb.set(((Integer)h.get(METHOD_INVALIDATE)).intValue());
                facb.set(((Integer)h.get(METHOD_SEND_STATE)).intValue());

                racb.setAllPermissions();
                racb.clear(((Integer)h.get(METHOD_INVALIDATE)).intValue());
                racb.clear(((Integer)h.get(METHOD_SEND_STATE)).intValue());
            }
            else if (role == SLAVE_ROLE)
            {
                facb.set(((Integer)h.get(METHOD_REGISTER_SLAVE)).intValue());
                facb.set(((Integer)h.get(METHOD_GET_STATE_VERSION)).intValue());
                facb.set(((Integer)h.get(METHOD_SEND_ME_STATE)).intValue());
                facb.set(((Integer)h.get(METHOD_REPORT_STATS)).intValue());

                racb.set(((Integer)h.get(METHOD_REGISTER_CLIENT)).intValue());
                racb.set(((Integer)h.get(METHOD_SEND_STATE)).intValue());
                racb.set(((Integer)h.get(METHOD_INVALIDATE)).intValue());
            }
            else if (role == CLIENT_ROLE)
            {
                facb.set(((Integer)h.get(METHOD_REGISTER_CLIENT)).intValue());
            }
        } 
        else // client/server mode
          {
            // The replication subobject ALWAYS expects a REGISTER_CLIENT
            // and the client (could) search(es) for this bit in the Location
            // service, and ALWAYS send register client for each new connection.
            if (role == MASTER_ROLE) {
                racb.set(((Integer)h.get(METHOD_REGISTER_CLIENT)).intValue());
            }
            else if (role == CLIENT_ROLE) {
                facb.set(((Integer)h.get(METHOD_REGISTER_CLIENT)).intValue());            }
        }
        return new AccessCtrlBitmap[] { facb, racb };
    }
}
