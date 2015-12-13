/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/AuditableEventsTable.java,v 1.4 2004/03/16 14:24:14 tonygraham Exp $
 * ====================================================================
 */

/**
 * $Header:
 */
package org.freebxml.omar.client.ui.swing;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;


/**
 *
 * @author <a href="mailto:nikola.stojanovic@acm.org">Nikola Stojanovic</a>
 */
public class AuditableEventsTable extends JTable {
    protected JAXRResourceBundle resourceBundle = JAXRResourceBundle.getInstance();

    final AuditableEventsTableModel tableModel;

    public AuditableEventsTable(AuditableEventsTableModel model) {
        super(model);
        tableModel = model;
        setToolTipText(resourceBundle.getString("tip.auditableEventsTable"));
        setRowHeight(getRowHeight() * 2);
    }

    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
    }

    public void setVisible(boolean makeVisible) {
        if (makeVisible) {
        }

        super.setVisible(makeVisible);
    }
}
/**
 * $Header:
 */
package com.sun.xml.registry.client.browser;
 
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import com.sun.xml.registry.client.browser.graph.*;

/**
 *
 * @author <a href="mailto:nikola.stojanovic@acm.org">Nikola Stojanovic</a>
 */

public class AuditableEventsTable extends JTable {
    
    final AuditableEventsTableModel     tableModel;

    public AuditableEventsTable(AuditableEventsTableModel model) {
        
        super(model);
	tableModel = model;
        setToolTipText("Table of Auditable Events");
        setRowHeight(getRowHeight() * 2);
    }


    public void tableChanged(TableModelEvent e) {

        super.tableChanged(e);       
    }
    
   
    public void setVisible(boolean makeVisible) {

        if (makeVisible) {}
        super.setVisible(makeVisible);
    }
}