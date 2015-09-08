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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/MSHServletContextListener.java,v 1.21 2003/04/09 07:47:48 kcyee Exp $
 *
 * Code authored by:
 *
 * kcyee [2002-07-29]
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

import hk.hku.cecid.phoenix.common.util.Property;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 * This class implements the event handler for the life cycle of the
 * MSH servlet. This class should be referenced in web.xml to handle
 * the events.
 *
 * @author kcyee
 * @version $Revision: 1.21 $
 */
public class MSHServletContextListener implements ServletContextListener {

    static Logger logger = Logger.getLogger(MSHServletContextListener.class); 

    /** Release number */
    protected static String release;

    /** Flag indicating if the class has been configured.  */
    protected static boolean isConfigured = false;

    /** 
     * Configure the class if and only if it has not been configured.
     * 
     * @param prop <code>Property</code> object.
     */
    static synchronized void configure(Property prop) 
        throws InitializationException {

        if (isConfigured) return;

        Utility.configureLogger(prop, "hk.hku.cecid.phoenix.message");
        Utility.configureLogger(prop, "hk.hku.cecid.phoenix.pki");
        release = MessageServiceHandler.getMetaData().getRelease();
        isConfigured = true;
    }

    /** Handles event for servlet start up */
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Property prop = Property.load(Constants.MSH_SERVER_PROPERTY_FILE);
            configure(prop);
            logger.info(Constants.PROGRAM_NAME + " v." + release + " started.");
            MessageServer.configure(prop);
        }
        catch (IOException e) {
            System.out.println(ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e));
            return;
        }
        catch (InitializationException e) {
            System.out.println(ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_INIT_ERROR, e));
        }
        catch (Exception e) {
            System.out.println(ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e));
        }

        try {
            MessageServer ms = MessageServer.getInstance();
            Transaction tx = new Transaction(MessageServer.dbConnectionPool);
            try {
                ms.logMSHLifeCycle("start", tx);
                tx.commit();
            }
            catch (Exception e) {
                logger.error(ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_WRITE, e, 
                    "Hermes life cycle (start)"));
                try {
                    tx.rollback();
                }
                catch (Exception e2) {}
            }
        }
        catch (MessageServerException e) {}
    }

    /** Handles event for servlet shut down */
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            MessageServer ms = MessageServer.getInstance();
            Transaction tx = new Transaction(MessageServer.dbConnectionPool);
            try {
                ms.logMSHLifeCycle("stop", tx);
                tx.commit();
            }
            catch (Exception e) {
                logger.error(ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_WRITE, e, 
                    "Hermes life cycle (stop)"));
                try {
                    tx.rollback();
                }
                catch (Exception e2) {}
            }
        }
        catch (MessageServerException e) {}
        logger.info(Constants.PROGRAM_NAME + " v." + release + " stopped.");
    }
}

