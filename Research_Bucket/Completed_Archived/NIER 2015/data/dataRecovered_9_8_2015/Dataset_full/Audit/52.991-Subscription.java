/*
 * $Header: /cvsroot/sino/ebxmlrr-spec/src/share/org/oasis/ebxml/registry/infomodel/Subscription.java,v 1.7 2003/05/14 21:00:22 farrukh_najmi Exp $
 * 
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 */
package org.oasis.ebxml.registry.infomodel;

import java.util.*;

/**
 * A Subscription is submitted by a client (subscriber) to register interest in specific
 * types of AuditableEvents. When these events occur, the registry notifies
 * the subscriber of the event via a Notification.
 * 
 * 
 * @see AuditableEvent
 * @see Notification
 *
 * @author Farrukh S. Najmi
 * 
 */
public abstract class Subscription extends RegistryObject {
    
    /**
     * The date starting with which the Subscription becomes active.
     */
    public Date startDate;

    /**
     * The date ending with which the Subscription expires.
     */
    public Date endDate;
    
    /**
     * 
     **/
    long notificationInterval;
    
    /**
     * The selector for this object.
     */
    Selector selector;
    
    /**
     * The action for this object.
     * @supplierRole action
     * @supplierCardinality 1..*
     * @undirected
     */
   Action action;

   /**
    * @directed
    * @supplierRole selector 
    */
   private AdhocQuery lnkAdhocQuery;    
}
