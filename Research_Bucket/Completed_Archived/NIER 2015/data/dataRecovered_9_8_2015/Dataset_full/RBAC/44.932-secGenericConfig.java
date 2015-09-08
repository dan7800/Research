/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.security;

import vu.globe.rts.lr.replication.MasterSlave.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import vu.globe.rts.security.protocol.messages.*;
import vu.globe.rts.security.certs.AccessCtrlBitmap;
import vu.globe.util.parse.AttributeString;

public class secGenericConfig {

    /** Some generic things about revocation */
    public static final String CREDS_REVOKED = "ERROR: Credentials revoked";
    public static final int NOT_REVOKED = 0;
    public static final int REVOKED_CLIENT = 1;
    public static final int REVOKED_REPLICA = 2;
    

    /** Implementation handle of the generic security subobject */
    public static final String SEC_GENERIC_IMPL =
                        "JAVA;vu.globe.rts.security.secGenericCO";

    public static final String CONN_ATTR_STR = "connector";
    public static final String LISR_ATTR_STR = "listeners";
    public static final String SEC_OFF_STR = "noSecurity";

    // Credentials manager settings
    // Should we get a CRL
    public static final String GET_CRL_ATTR_STR = "getCRL";
    // Should this be the whole CRL that we must get
    public static final String GET_WHOLE_CRL_ATTR_STR = "getWholeCRL";
    // Should we refresh this CRL regularly ?
    public static final String REFRESH_CRL_ATTR_STR = "refreshCRL";
    // Interval for refresh ?
    public static final String INTERVAL_CRL_ATTR_STR = "intervalCRL";

    public static final long DEFAULT_INTERVAL_TIME = 3600000L; // 1 hour
   
    // Normal SECURE mode init strings
    public static final String SEC_MASTER_INIT;     // listener
    public static final String SEC_SLAVE_INIT;      // listener + connector
    public static final String SEC_CLIENT_INIT;     // connector

    // INSECURE mode init strings
    public static final String SEC_OFF_MASTER_INIT;
    public static final String SEC_OFF_SLAVE_INIT;
    public static final String SEC_OFF_CLIENT_INIT;

    static {
        // Client should only get the CRL when they connect
        AttributeString temp = new AttributeString();
        temp.put(CONN_ATTR_STR, "");
        temp.put(GET_CRL_ATTR_STR, "");
        SEC_CLIENT_INIT = temp.toString();
        temp.put(SEC_OFF_STR, "");
        SEC_OFF_CLIENT_INIT = temp.toString();

        // Slaves must get the WHOLE CRL, regularly, interval determined when
        // they get the CRL from the master.
        temp = new AttributeString();
        temp.put(CONN_ATTR_STR, "");
        temp.put(LISR_ATTR_STR, "");
        temp.put(GET_CRL_ATTR_STR, "");
        temp.put(GET_WHOLE_CRL_ATTR_STR, "");
        temp.put(REFRESH_CRL_ATTR_STR, "");
        SEC_SLAVE_INIT = temp.toString();
        temp.put(SEC_OFF_STR, "");
        SEC_OFF_SLAVE_INIT = temp.toString();
        
        // Master must update the CRL, regularly
        temp = new AttributeString();
        temp.put(LISR_ATTR_STR, "");
        temp.put(REFRESH_CRL_ATTR_STR, "");
        temp.put(INTERVAL_CRL_ATTR_STR, new Long(DEFAULT_INTERVAL_TIME).toString());
        SEC_MASTER_INIT = temp.toString();
        temp.put(SEC_OFF_STR, "");
        SEC_OFF_MASTER_INIT = temp.toString();
    }

    
    /************ SECURITY STUFF *********************************************/
    public static final int MASTER_ROLE = MSConfig.MASTER_ROLE;
    public static final int SLAVE_ROLE = MSConfig.SLAVE_ROLE;
    public static final int CLIENT_ROLE = MSConfig.CLIENT_ROLE;

    public static int NUMBER_OF_SEC_METHODS = 0;    // Automatically set
    
    public static ArrayList _singletonReverseMethodIDMap;
    public static final String METHOD_GET_BLUEPRINTS =
            Integer.toString(SecMessageFact.MSG_GET_BLUEPRINTS_REQ);
    public static final String METHOD_GET_CREDENTIALS =
            Integer.toString(SecMessageFact.MSG_GET_CREDENTIALS_REQ);
    public static final String METHOD_UPDATE_CREDENTIALS =
            Integer.toString(SecMessageFact.MSG_UPDATE_CREDENTIALS_REQ);
    public static final String METHOD_GET_WHOLE_CRL =
            Integer.toString(SecMessageFact.MSG_GET_WHOLE_CRL_REQ);
    public static final String METHOD_GET_CRL =
            Integer.toString(SecMessageFact.MSG_GET_CRL_REQ);
    public static final String METHOD_REVOKE_CERTIFICATE =
            Integer.toString(SecMessageFact.MSG_REVOKE_CERTIFICATE_REQ);
    public static final String METHOD_SELF_DESTRUCT = 
            Integer.toString(SecMessageFact.MSG_SELF_DESTRUCT_REQ);
    public static final String METHOD_GET_CLIENTS_LIST =
            Integer.toString(SecMessageFact.MSG_GET_CLIENTS_REQ);

    static {
        
        _singletonReverseMethodIDMap = new ArrayList();

        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_GET_BLUEPRINTS);
        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_GET_CREDENTIALS);
        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_UPDATE_CREDENTIALS);
        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_GET_WHOLE_CRL);
        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_GET_CRL);
        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_REVOKE_CERTIFICATE);
        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_SELF_DESTRUCT);
        _singletonReverseMethodIDMap.add(NUMBER_OF_SEC_METHODS++,
                                         METHOD_GET_CLIENTS_LIST);
    }

    public static List getReverseSecMethodIDMap() {
        return _singletonReverseMethodIDMap;
    }

    public static AccessCtrlBitmap[] getSecurityRequirements(int role)
    {
        AccessCtrlBitmap facb = new AccessCtrlBitmap( NUMBER_OF_SEC_METHODS );
        AccessCtrlBitmap racb = new AccessCtrlBitmap( NUMBER_OF_SEC_METHODS );

        // Construct (forward) MethodIDMap
        Hashtable h = new Hashtable();
        for (int i=0; i<_singletonReverseMethodIDMap.size(); i++)
        {
            h.put( _singletonReverseMethodIDMap.get( i ), new Integer( i ) );
        }

        if (role == MASTER_ROLE) {
            racb.set(((Integer)h.get(METHOD_GET_BLUEPRINTS)).intValue());
            racb.set(((Integer)h.get(METHOD_GET_CREDENTIALS)).intValue());
            racb.set(((Integer)h.get(METHOD_UPDATE_CREDENTIALS)).intValue());
            racb.set(((Integer)h.get(METHOD_GET_WHOLE_CRL)).intValue());
            racb.set(((Integer)h.get(METHOD_GET_CRL)).intValue());
            racb.set(((Integer)h.get(METHOD_REVOKE_CERTIFICATE)).intValue());
            racb.set(((Integer)h.get(METHOD_GET_CLIENTS_LIST)).intValue());

            facb.set(((Integer)h.get(METHOD_SELF_DESTRUCT)).intValue());
        } else if (role == SLAVE_ROLE) {
            racb.set(((Integer)h.get(METHOD_GET_BLUEPRINTS)).intValue());
            racb.set(((Integer)h.get(METHOD_GET_CRL)).intValue());
            racb.set(((Integer)h.get(METHOD_SELF_DESTRUCT)).intValue());

            facb.set(((Integer)h.get(METHOD_GET_WHOLE_CRL)).intValue());
        } else if (role == CLIENT_ROLE) {
            facb.set(((Integer)h.get(METHOD_GET_BLUEPRINTS)).intValue());
            facb.set(((Integer)h.get(METHOD_GET_CREDENTIALS)).intValue());
            facb.set(((Integer)h.get(METHOD_GET_CRL)).intValue());
        }
        return new AccessCtrlBitmap[] { facb, racb };
    }

    /** This will add special privileges to the OWNER's security subobject's
     * FACB. Only owner is allowed to do Update Credentials and revoke
     * certificate out of the box.
     */
    public static void setOwnerPrivileges(AccessCtrlBitmap facb)
    {
        // Construct (forward) MethodIDMap
        Hashtable h = new Hashtable();
        for (int i=0; i<_singletonReverseMethodIDMap.size(); i++)
        {
            h.put( _singletonReverseMethodIDMap.get( i ), new Integer( i ) );
        }

        facb.set(((Integer)h.get(METHOD_GET_CLIENTS_LIST)).intValue());
        facb.set(((Integer)h.get(METHOD_UPDATE_CREDENTIALS)).intValue());
        facb.set(((Integer)h.get(METHOD_REVOKE_CERTIFICATE)).intValue());
    }
}
