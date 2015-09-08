/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/AuditableEventsTableModel.java,v 1.6 2004/03/16 14:24:14 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header:
 */
package org.freebxml.omar.client.ui.swing;

import java.util.ArrayList;
import java.util.Collection;

import java.sql.Timestamp;


import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.User;


/**
 * @author <a href="mailto:nikola.stojanovic@acm.org">Nikola Stojanovic</a>
 */
public class AuditableEventsTableModel extends AbstractTableModel {
    protected JAXRResourceBundle resourceBundle = JAXRResourceBundle.getInstance();

    String[] columnNames = { resourceBundle.getString("columnName.eventType"),
							 resourceBundle.getString("columnName.timestamp"),
							 resourceBundle.getString("columnName.user") };
    ArrayList auditableEvents = new ArrayList();
    RegistryBrowser registryBrowser;

    public AuditableEventsTableModel() {
        registryBrowser = RegistryBrowser.getInstance();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return auditableEvents.size();
    }

    public AuditableEvent getObjectAt(int row) {
        AuditableEvent ae = (AuditableEvent) auditableEvents.get(row);

        return ae;
    }

    public Object getValueAt(int row, int col) {
        AuditableEvent auditableEvent = (AuditableEvent) auditableEvents.get(row);
        Object value = null;
        InternationalString iString = null;

        try {
            switch (col) {
            case 0:
                value = RegistryBrowser.getEventTypeAsString(auditableEvent.getEventType());

                break;

            case 1:
                Timestamp timestamp = auditableEvent.getTimestamp();
                if (timestamp!= null) {
                    value = timestamp.toString();
                }
                else {
                    value = resourceBundle.getString("text.unknownTime");
                }
                break;

            case 2:

                User user = null;

                try {
                    user = auditableEvent.getUser();
                } catch (JAXRException e) {
                    //User may have been deleted. Handle gracefully
                }

                if (user != null) {
                    value = RegistryBrowser.getUserName(auditableEvent.getUser(),
                            1);
                } else {
                    org.freebxml.omar.client.xml.registry.infomodel.RegistryObjectRef userRef =
                        ((org.freebxml.omar.client.xml.registry.infomodel.AuditableEventImpl) auditableEvent).getUserRef();
                    value = userRef.getId();
                }

                break;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return value;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    void update(Collection auditableEvents) {
        if (auditableEvents.isEmpty()) {
            JOptionPane.showMessageDialog(null,
										  resourceBundle.getString("message.noAuditTrail"),
										  resourceBundle.getString("title.registryBrowser"),
										  JOptionPane.INFORMATION_MESSAGE);
            auditableEvents = new ArrayList();
        }

        setAuditableEvents(auditableEvents);
    }

    ArrayList getAuditableEvents() {
        return auditableEvents;
    }

    void setAuditableEvents(Collection objects) {
        auditableEvents.clear();
        auditableEvents.addAll(objects);
        fireTableDataChanged();
    }
}
/**
 * $Header:
 */
package com.sun.xml.registry.client.browser;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * @author <a href="mailto:nikola.stojanovic@acm.org">Nikola Stojanovic</a>
 */

public class AuditableEventsTableModel extends AbstractTableModel {
    
    String[] columnNames = {"Event Type", "Timestamp", "User"};
    ArrayList                   auditableEvents = new ArrayList();
    RegistryBrowser             registryBrowser;  
       

    public AuditableEventsTableModel() {
        
        registryBrowser = RegistryBrowser.getInstance();
    }
    

    public int getColumnCount() {
        
        return columnNames.length;
    }
    

    public int getRowCount() {
        
        return auditableEvents.size();
    }
    

    public AuditableEvent getObjectAt(int row) {
        
        AuditableEvent  ae = (AuditableEvent) auditableEvents.get(row);
        return ae;
    }
    

    public Object getValueAt(int row, int col) {
        
        AuditableEvent      auditableEvent = (AuditableEvent) auditableEvents.get(row);
        Object              value = null;
        InternationalString iString = null;
        
        try {
            switch (col) {
                case 0:
                    value = RegistryBrowser.getEventTypeAsString(auditableEvent.getEventType());
                    break;
                case 1:
                    value = (auditableEvent.getTimestamp()).toString();
                    break;
                case 2:
                    User user = null;
                    try {
                        user = auditableEvent.getUser();
                    }
                    catch (JAXRException e) {
                        //User may have been deleted. Handle gracefully                        
                    }
                    
                    if (user != null) {
                        value = RegistryBrowser.getUserName(auditableEvent.getUser(), 1);
                    }
                    else {
                        com.sun.xml.registry.ebxml.infomodel.RegistryObjectRef userRef = 
                            ((com.sun.xml.registry.ebxml.infomodel.AuditableEventImpl)auditableEvent).getUserRef();
                        value = userRef.getId();
                    }
                    break;
            }
        }
        catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        return value;
    }
    

    public String getColumnName(int col) {
        
        return columnNames[col].toString();
    }
    
    
    void update(Collection auditableEvents) {
              
        if (auditableEvents.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Audit Trail found.",
            "Registry Browser",
            JOptionPane.INFORMATION_MESSAGE);
            auditableEvents = new ArrayList();
        }
        setAuditableEvents(auditableEvents);
    }

       
    ArrayList getAuditableEvents() {
        
        return auditableEvents;
    }
    

    void setAuditableEvents(Collection objects) {
        auditableEvents.clear();
        auditableEvents.addAll(objects);
        fireTableDataChanged();
    }
}
