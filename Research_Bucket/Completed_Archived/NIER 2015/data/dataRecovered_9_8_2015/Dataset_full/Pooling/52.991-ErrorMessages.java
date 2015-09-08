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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/ErrorMessages.java,v 1.20 2003/12/11 06:41:29 bobpykoon Exp $
 *
 * Code authored by:
 *
 * kcyee [2003-03-24]
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
 * A class holding error codes and the corresponding error messages.
 *
 * @author kcyee
 * @version $Revision: 1.20 $
 */
public class ErrorMessages {

    public static final int ERR_HERMES_INIT_ERROR = 10001;
    public static final int ERR_HERMES_UNKNOWN_ERROR = 10002;
    public static final int ERR_HERMES_PROPERTY_NOT_SET = 10003;
    public static final int ERR_HERMES_DATA_ERROR = 10004;
    public static final int ERR_HERMES_STATE_ERROR = 10005;
    public static final int ERR_HERMES_FILE_IO_ERROR = 10006;
    public static final int ERR_HERMES_FILE_NOT_FOUND_ERROR = 10007;
    public static final int ERR_HERMES_HTTP_POST_FAILED = 10008;
    public static final int ERR_HERMES_SERVLET_IO_ERROR = 10009;
    public static final int ERR_HERMES_AUTHENTICATION_FAILED = 10010;
    public static final int ERR_HERMES_REGISTRATION_FAILED = 10011;
    public static final int ERR_HERMES_UNKNOWN_APP_CONTEXT = 10012;
    public static final int ERR_HERMES_ZIP_IO_ERROR = 10013;
    public static final int ERR_HERMES_UNKNOWN_DESTINATION = 10014;

    public static final int ERR_SMTP_INVALID_SERVER = 10101;
    public static final int ERR_SMTP_CANNOT_COMPOSE_MESSAGE = 10102;
    public static final int ERR_SMTP_CANNOT_SEND_MESSAGE = 10103;
    public static final int ERR_POP_INVALID_FOLDER = 10104;
    public static final int ERR_POP_INVALID_SERVER = 10105;

    public static final int ERR_PKI_INVALID_KEYSTORE = 10201;
    public static final int ERR_PKI_CANNOT_ENCRYPT = 10202;
    public static final int ERR_PKI_CANNOT_DECRYPT = 10203;
    public static final int ERR_PKI_CANNOT_SIGN = 10204;
    public static final int ERR_PKI_VERIFY_SIGNATURE_FAILED = 10205;

    public static final int ERR_DB_CANNOT_CLOSE_CONN = 10301;
    public static final int ERR_DB_INVALID_PARAM = 10302;
    public static final int ERR_DB_CANNOT_ALLOCATE_CONN = 10303;
    public static final int ERR_DB_POOL_OVERFLOW = 10304;
    public static final int ERR_DB_CANNOT_LOAD_DRIVER = 10305;
    public static final int ERR_DB_CANNOT_CREATE_CONN = 10306;
    public static final int ERR_DB_WRONG_SCHEMA = 10307;
    public static final int ERR_DB_DATA_INCONSISTENT = 10308;
    public static final int ERR_DB_CANNOT_CREATE_TABLE = 10309;
    public static final int ERR_DB_CANNOT_READ = 10310;
    public static final int ERR_DB_CANNOT_WRITE = 10311;
    public static final int ERR_DB_BACKUP_ERROR = 10312;
    public static final int ERR_DB_RESTORE_ERROR = 10313;
    public static final int ERR_DB_CANNOT_COMMIT_CHANGE = 10314;
    public static final int ERR_DB_CANNOT_ROLLBACK_CHANGE = 10315;
    public static final int ERR_DB_CANNOT_FIND_MESSAGE = 10316;

    public static final int ERR_TX_INCONSISTENT_LOCK = 10401;
    public static final int ERR_TX_CANNOT_ROLLBACK = 10402;

    public static final int ERR_SOAP_MESSAGE_FACTORY = 10501;
    public static final int ERR_SOAP_CANNOT_SERIALIZE = 10502;
    public static final int ERR_SOAP_CANNOT_INTERNALIZE = 10503;
    public static final int ERR_SOAP_CANNOT_SAVE_OBJECT = 10504;
    public static final int ERR_SOAP_CANNOT_SEND_MESSAGE = 10505;
    public static final int ERR_SOAP_INIT_CONNECTION = 10506;
    public static final int ERR_SOAP_INVALID_CONNECTION = 10507;
    public static final int ERR_SOAP_CANNOT_CREATE_OBJECT = 10508;
    public static final int ERR_SOAP_GENERAL_ERROR = 10509;


    protected static TreeMap errorMsg;
    protected static boolean isConfigured = false;

    protected static synchronized void configure() {

        if (isConfigured) return;

        errorMsg = new TreeMap();
        load(ERR_HERMES_INIT_ERROR, "Initialization error");
        load(ERR_HERMES_UNKNOWN_ERROR, "Unknown error");
        load(ERR_HERMES_PROPERTY_NOT_SET, "Property not properly set");
        load(ERR_HERMES_DATA_ERROR, "Data error");
        load(ERR_HERMES_STATE_ERROR, "State error");
        load(ERR_HERMES_FILE_IO_ERROR, "File IO error");
        load(ERR_HERMES_FILE_NOT_FOUND_ERROR, "File not found");
        load(ERR_HERMES_HTTP_POST_FAILED, "HTTP POST request failed");
        load(ERR_HERMES_SERVLET_IO_ERROR, "Servlet IO error");
        load(ERR_HERMES_AUTHENTICATION_FAILED, "Authentication failed");
        load(ERR_HERMES_REGISTRATION_FAILED, "Registration failed");
        load(ERR_HERMES_UNKNOWN_APP_CONTEXT, "Unknown application context");
        load(ERR_HERMES_ZIP_IO_ERROR, "Error reading/writing ZIP stream");
        load(ERR_HERMES_UNKNOWN_DESTINATION, "Unknown destination");

        load(ERR_SMTP_INVALID_SERVER, "Invalid SMTP server");
        load(ERR_SMTP_CANNOT_COMPOSE_MESSAGE, "Cannot compose mail message");
        load(ERR_SMTP_CANNOT_SEND_MESSAGE, "Cannot send mail message");
        load(ERR_POP_INVALID_FOLDER, "Invalid POP/IMAP folder");
        load(ERR_POP_INVALID_SERVER, "Invalid POP/IMAP server");

        load(ERR_PKI_INVALID_KEYSTORE, "Invalid keystore");
        load(ERR_PKI_CANNOT_ENCRYPT, "Cannot encrypt message");
        load(ERR_PKI_CANNOT_DECRYPT, "Cannot decrypt message");
        load(ERR_PKI_CANNOT_SIGN, "Cannot sign message");
        load(ERR_PKI_VERIFY_SIGNATURE_FAILED, 
            "Verification of signature failed");

        load(ERR_DB_CANNOT_CLOSE_CONN, "Cannot close DB connection");
        load(ERR_DB_INVALID_PARAM, "Invalid parameter");
        load(ERR_DB_CANNOT_ALLOCATE_CONN, "Cannot get DB connection");
        load(ERR_DB_POOL_OVERFLOW, 
            "DB connection are freed more than allocated");
        load(ERR_DB_CANNOT_LOAD_DRIVER, "Cannot load JDBC driver");
        load(ERR_DB_CANNOT_CREATE_CONN, "Cannot create DB connection");
        load(ERR_DB_WRONG_SCHEMA, "Existing DB schema incorrect");
        load(ERR_DB_DATA_INCONSISTENT, "Data inconsistency");
        load(ERR_DB_CANNOT_CREATE_TABLE, "Cannot create DB table");
        load(ERR_DB_CANNOT_READ, "Cannot query record from DB");
        load(ERR_DB_CANNOT_WRITE, "Cannot write record to DB");
        load(ERR_DB_BACKUP_ERROR, "Database backup error");
        load(ERR_DB_RESTORE_ERROR, "Database restore error");
        load(ERR_DB_CANNOT_COMMIT_CHANGE, "Cannot commit DB changes");
        load(ERR_DB_CANNOT_ROLLBACK_CHANGE, "Cannot rollback DB changes");
        load(ERR_DB_CANNOT_FIND_MESSAGE, "Cannot find message in DB");

        load(ERR_TX_INCONSISTENT_LOCK, "Cannot unlock a non-existent object");
        load(ERR_TX_CANNOT_ROLLBACK, "Cannot rollback");

        load(ERR_SOAP_MESSAGE_FACTORY, 
            "Default MessageFactory cannot be instantiated");
        load(ERR_SOAP_CANNOT_SERIALIZE, "Cannot serialize SOAP message");
        load(ERR_SOAP_CANNOT_INTERNALIZE, "Cannot internalize SOAP object");
        load(ERR_SOAP_CANNOT_SAVE_OBJECT, "Cannot save change to SOAP message");
        load(ERR_SOAP_CANNOT_SEND_MESSAGE, "Cannot send SOAP message");
        load(ERR_SOAP_INIT_CONNECTION, "Cannot initialize SOAP connection");
        load(ERR_SOAP_INVALID_CONNECTION, "Invalid HTTP SOAP connection");
        load(ERR_SOAP_GENERAL_ERROR, "Cannot process SOAP message");
        load(ERR_SOAP_CANNOT_CREATE_OBJECT, "Cannot create SOAP object");
    }

    protected static void load(int code, String msg) {
        errorMsg.put(new Integer(code), msg);
    }

    public static String getMessage(int code) {
        return getMessage(code, null, "");
    }

    public static String getMessage(int code, String extraMsg) {
        return getMessage(code, null, extraMsg);
    }

    public static String getMessage(int code, Throwable e) {
        return getMessage(code, e, "");
    }

    public static String getMessage(int code, Throwable e, String extraMsg) {
        configure();
    
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(code).append("] ");
        String err = (String) errorMsg.get(new Integer(code));
        if (err == null) {
            sb.append("Unknown error");
        }
        else {
            sb.append(err);
        }
        if (!extraMsg.equals("")) {
            sb.append(" - ").append(extraMsg);
        }
        if (e != null) {
            sb.append("\nException: ").append(e.getClass().getName());
            sb.append("\nMessage: ").append(e.getMessage());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        configure();
        Iterator keys = errorMsg.keySet().iterator();
        while (keys.hasNext()) {
            Integer err = (Integer) keys.next();
            String msg = (String) errorMsg.get(err);
            System.out.println(err.intValue() + "\t" + msg);
        }

    }
}

