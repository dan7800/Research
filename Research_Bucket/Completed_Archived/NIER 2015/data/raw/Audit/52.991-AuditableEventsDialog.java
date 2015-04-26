/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/AuditableEventsDialog.java,v 1.4 2004/03/16 14:24:14 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header:
 *
 *
 */
package org.freebxml.omar.client.ui.swing;

import java.awt.BorderLayout;

import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.text.MessageFormat;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;


public class AuditableEventsDialog extends JBDialog {
    private AuditableEventsTableModel auditableEventsTableModel = null;
    private AuditableEventsTable auditableEventsTable = null;

    public AuditableEventsDialog(JFrame parent, boolean modal,
        RegistryObject registryObject) {
        super(parent, modal);

        try {
			Object[] auditableEventsArgs = {RegistryBrowser.getName(registryObject)};
			MessageFormat form =
				new MessageFormat(resourceBundle.getString("title.auditableEvents"));
            setTitle(form.format(auditableEventsArgs));
            initialize(registryObject.getAuditTrail());
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    private void initialize(Collection auditableEvents) {
        JPanel mainPanel = getMainPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel auditableEventsTablePanel = new JPanel();
        auditableEventsTablePanel.setBorder(BorderFactory.createTitledBorder(
                resourceBundle.getString("title.auditableEvents.border")));
        auditableEventsTablePanel.setLayout(new BorderLayout());

        auditableEventsTableModel = new AuditableEventsTableModel();
        auditableEventsTable = new AuditableEventsTable(auditableEventsTableModel);

        JScrollPane auditableEventsTablePane = new JScrollPane(auditableEventsTable);
        auditableEventsTablePanel.add(auditableEventsTablePane,
            BorderLayout.CENTER);

        //setBounds(new Rectangle(300, 300, 600, 300));
        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);        
        setAuditableEvents(auditableEvents);

        auditableEventsTablePanel.setVisible(true);
        mainPanel.add(auditableEventsTablePanel);
        pack();
    }

    public void setAuditableEvents(Collection auditableEvents) {
        auditableEventsTableModel.update(auditableEvents);
    }
}
/**
 * $Header:
 *
 *
 */
package com.sun.xml.registry.client.browser;

import java.util.*;
import javax.swing.*;
import java.beans.*; //Property change stuff
import java.awt.*;
import java.awt.event.*;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;


public class AuditableEventsDialog extends JBDialog {
    
    private AuditableEventsTableModel auditableEventsTableModel = null;
    private AuditableEventsTable auditableEventsTable = null;

    
    public AuditableEventsDialog(JFrame parent, boolean modal, RegistryObject registryObject) {

        super(parent, modal);
        try {
            setTitle("Auditable Events for " + RegistryBrowser.getName(registryObject));
            initialize(registryObject.getAuditTrail());
        }
        catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    
    private void initialize(Collection auditableEvents) {

        JPanel mainPanel = getMainPanel();
        mainPanel.setLayout(new BorderLayout());              

        JPanel auditableEventsTablePanel = new JPanel();
        auditableEventsTablePanel.setBorder(BorderFactory.createTitledBorder("Auditable Events"));
        auditableEventsTablePanel.setLayout(new BorderLayout());
        
        auditableEventsTableModel = new AuditableEventsTableModel();        
        auditableEventsTable = new AuditableEventsTable(auditableEventsTableModel);
        JScrollPane auditableEventsTablePane = new JScrollPane(auditableEventsTable);
        auditableEventsTablePanel.add(auditableEventsTablePane, BorderLayout.CENTER);
                      
        //setBounds(new Rectangle(300, 300, 600, 300));
        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);        
        setAuditableEvents(auditableEvents);
        
        auditableEventsTablePanel.setVisible(true);
        mainPanel.add(auditableEventsTablePanel);
        pack();
    }

    
    public void setAuditableEvents(Collection auditableEvents) {
        
        auditableEventsTableModel.update(auditableEvents);        
    }
        
}
