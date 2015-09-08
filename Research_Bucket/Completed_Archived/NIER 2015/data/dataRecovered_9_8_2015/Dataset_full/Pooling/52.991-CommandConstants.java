/*
 * Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the Academic Free License Version 1.0
 *
 * Academic Free License
 * Version 1.0
 *
 * This Academic Free License applies to any software and associated 
 * documentation (the "Software") whose owner (the "Licensor") has placed the 
 * statement "Licensed under the Academic Free License Version 1.0" immediately 
 * after the copyright notice that applies to the Software. 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of the Software (1) to use, copy, modify, merge, publish, perform, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, and (2) under patent 
 * claims owned or controlled by the Licensor that are embodied in the Software 
 * as furnished by the Licensor, to make, use, sell and offer for sale the 
 * Software and derivative works thereof, subject to the following conditions: 
 *
 * - Redistributions of the Software in source code form must retain all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers. 
 * - Redistributions of the Software in executable form must reproduce all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers in the documentation and/or 
 *   other materials provided with the distribution. 
 * - Neither the names of Licensor, nor the names of any contributors to the 
 *   Software, nor any of their trademarks or service marks, may be used to 
 *   endorse or promote products derived from this Software without express 
 *   prior written permission of the Licensor. 
 *
 * DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS 
 * OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER 
 * A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY 
 * PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS 
 * AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE 
 * LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE. 
 *
 * This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved. 
 * Permission is hereby granted to copy and distribute this license without 
 * modification. This license may not be modified without the express written 
 * permission of its copyright owner. 
 */

/* ===== 
 *
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/CommandConstants.java,v 1.11 2004/02/10 03:12:51 bobpykoon Exp $
 *
 * Code authored by:
 *
 * kcyee [2002-11-19]
 *
 * Code reviewed by:
 *
 * username [YYYY-MM-DD]
 *
 * Remarks:
 *
 * =====
 */

package hk.hku.cecid.phoenix.message.handler;

import java.util.Iterator;
import java.util.TreeMap;
/**
 * This class defines the constant values used by identifying the type of
 * the Command object.
 *
 * @author kcyee
 * @version $Revision: 1.11 $
 */
class CommandConstants {
    /*
     * All commands are classified into four categories:
     * - System commands: Functions that alter the state of MSH but not used by
     *                    applications in daily operations.
     * - System queries:  Report the internal state of MSH but not used by 
     *                    applications in daily operations.
     * - Commands:        Functions that alter the state of MSH and used by
     *                    applications in daily operations.
     * - Queries:         Query the MSH without altering any state.
     */

    // System commands
    
    /** Register MSH Configuration. */
    static final int REGISTER_MSH_CONFIG = 1;

    /** Disable sending acknowledgement request */
    static final int DISABLE_ACKNOWLEDGMENT = 2;

    /** Enable sending acknowledgement request */
    static final int ENABLE_ACKNOWLEDGMENT = 3;

    /** Ignore all incoming acknowledgements */
    static final int IGNORE_ACKNOWLEDGMENT = 4;
    
    /** Process all incoming acknowledgements */
    static final int ACCEPT_ACKNOWLEDGMENT = 5;

    /** Carry out loopback test */
    static final int TEST_LOOPBACK = 6;

    /** Reset database connection pool */
    static final int RESET_DB_CONNECTION_POOL = 7;

    /** Halt <code>MessageServiceHandler</code> to stop receiving incoming
        and sending outgoing messages */
    static final int HALT_SUSPEND = 8;

    /** Terminate the MSH. */
    static final int HALT_TERMINATE = 9;

    /** Resume <code>MessageServiceHandler</code> normal operation */
    static final int RESUME = 10;

    /** Backup database, message repository and <code>MessageListener</code>
        object store used by <code>MessageServiceHandler</code> */
    static final int MSH_BACKUP = 11;

    /** Restore database, message repository and <code>MessageListener</code>
        object store used by <code>MessageServiceHandler</code> */
    static final int MSH_RESTORE = 12;

    /** Archive the MSH data by application context. */
    static final int MSH_ARCHIVE_BY_APPCONTEXT = 13;

    /** Archive the MSH data by date. */
    static final int MSH_ARCHIVE_BY_DATE = 14;

    /** Archive the MSH data by date and application context. */
    static final int MSH_ARCHIVE_BY_DATE_AND_APPCONTEXT = 15;

    // System queries

    /** Report environment settings */
    static final int REPORT_ENVIRONMENT = 21;

    /** Check database connections */
    static final int CHECK_DATABASE = 22;

    /** Check message persistence */
    static final int CHECK_PERSISTENCE = 23;

    /** Check consistency of internal states */
    static final int CHECK_INTERNAL_STATES = 24;

    /** Get the current status of MSH */
    static final int QUERY_MSH_STATUS = 25;

    // Commands

    /** Send message to the other MSH */
    static final int SEND_MESSAGE = 31;

    /** Read a message from the MSH */
    static final int GET_MESSAGE = 32;

    /** Read undelivered message id's from the MSH */
    static final int GET_MESSAGE_ID = 33;

    /** Read undelivered message by id from the MSH */
    static final int GET_MESSAGE_BY_ID = 34;

    /** Query the current message order sequence number */
    static final int QUERY_SEQUENCE_NUMBER = 35;

    /** Reset the current message order sequence number; the next sequence 
     *  number returned by QUERY_SEQUENCE_NUMBER will be 1 instead of 0 */
    static final int QUERY_RESET_SEQUENCE_NUMBER = 36;

    /** Delete messages if they have not been sent. */
    static final int DELETE_PENDING_MESSAGE = 37;

    // Queries
    
    /** Get the message status */
    static final int QUERY_MESSAGE_STATUS = 41;

    /** Get the location of trusted repository */
    static final int QUERY_TRUSTED_REPOSITORY = 42;

    /** Get the list of pending messages */
    static final int QUERY_PENDING_MESSAGE = 43;

    /** Get the database connection pool information */
    static final int QUERY_DB_CONN_POOL = 44;

    /** Get number of records in database */
    static final int QUERY_NUM_RECORDS_IN_DB = 45;
    
    static final int SEND_MESSAGE_AUTOMATIC_REGISTER = 46;
    
    private static final TreeMap commandStrings = new TreeMap();

    private static void addCommand(int command, String value) {
        commandStrings.put(new Integer(command), value); 
    }

    public static String getCommandString(int command) {
        return (String) commandStrings.get(new Integer(command));
    }

    static {
        addCommand(REGISTER_MSH_CONFIG, "Register MSH Configuration");
        addCommand(DISABLE_ACKNOWLEDGMENT, "Disable sending acknowledgement request");
        addCommand(ENABLE_ACKNOWLEDGMENT, "Enable sending acknowledgement request");
        addCommand(IGNORE_ACKNOWLEDGMENT, "Ignore all incoming acknowledgements");
        addCommand(ACCEPT_ACKNOWLEDGMENT, "Process all incoming acknowledgements");
        addCommand(TEST_LOOPBACK, "Carry out loopback test");
        addCommand(RESET_DB_CONNECTION_POOL, "Reset database connection pool");
        addCommand(HALT_SUSPEND, "Halt MSH");
        addCommand(HALT_TERMINATE, "Terminate MSH");
        addCommand(RESUME, "Resume MSH");
        addCommand(MSH_BACKUP, "Backup MSH");
        addCommand(MSH_RESTORE, "Restore MSH");
        addCommand(MSH_ARCHIVE_BY_APPCONTEXT, "Archive by application context");
        addCommand(MSH_ARCHIVE_BY_DATE , "Archive by date");
        addCommand(MSH_ARCHIVE_BY_DATE_AND_APPCONTEXT , "Archive by date and application context");
        addCommand(REPORT_ENVIRONMENT, "Report environment settings");
        addCommand(CHECK_DATABASE, "Check database connections");
        addCommand(CHECK_PERSISTENCE, "Check message persistence");
        addCommand(CHECK_INTERNAL_STATES, "Check consistency of internal states");
        addCommand(QUERY_MSH_STATUS, "Query MSH status");
        addCommand(SEND_MESSAGE, "Send message");
        addCommand(GET_MESSAGE, "Get message");
        addCommand(GET_MESSAGE_ID, "Get undelivered message id");
        addCommand(GET_MESSAGE_BY_ID, "Get message by id");
        addCommand(QUERY_SEQUENCE_NUMBER, "Query message order sequence number");
        addCommand(QUERY_RESET_SEQUENCE_NUMBER, "Reset message order sequence number");
        addCommand(DELETE_PENDING_MESSAGE, "Delete messages");
        addCommand(QUERY_MESSAGE_STATUS, "Get message status");
        addCommand(QUERY_TRUSTED_REPOSITORY, "Get the location of trusted repository");
        addCommand(QUERY_PENDING_MESSAGE, "Get the list of pending messages");
        addCommand(QUERY_DB_CONN_POOL, "Get the database connection pool information");
        addCommand(QUERY_NUM_RECORDS_IN_DB, "Get number of records in database");
        addCommand(SEND_MESSAGE_AUTOMATIC_REGISTER, "Send message with automatic registration");
    }
    
    public static void main(String[] args) {
        Iterator it = commandStrings.keySet().iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            System.out.println(key.intValue() + "," 
                + (String) commandStrings.get(key));
        }
    }
}
