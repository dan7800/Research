/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org. All rights reserved.
 *
 * $Header:
 * /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/persistence/rdb/NotifyActionDAO.java,v
 * 1.3 2003/11/03 01:10:09 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.event;

import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;

/**
 * Listens to AuditableEvents.
 *
 * @author Farrukh S. Najmi
 * @author Nikola Stojanovic
 */
public interface AuditableEventListener {
    
    public void onEvent(AuditableEventType ae);
}
