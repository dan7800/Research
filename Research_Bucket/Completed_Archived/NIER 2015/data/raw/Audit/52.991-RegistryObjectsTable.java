/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryObjectsTable.java,v 1.7 2004/03/16 14:24:16 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.swing.graph.JBGraphPanel;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.MessageFormat;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;


/**
 * A JTable that lists
 *
 * @author Jim Glennon
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectsTable extends JTable
    implements PropertyChangeListener {
    private static final Log log = LogFactory.getLog(RegistryObjectsTableModel.class);

    protected JAXRResourceBundle resourceBundle =
		JAXRResourceBundle.getInstance();

    /** DOCUMENT ME! */
    public static final String SELECTED_ROW_PROP = "selectedRow";

    //Hack to not keep setting columnWidth which prevented user from resing teh column manually
    private static final int maxColWidth = 1000000;

    /** DOCUMENT ME! */
    int selectedRow = -1;

    /** DOCUMENT ME! */
    JPopupMenu popup;

    /** DOCUMENT ME! */
    JMenuItem editMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem removeMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem saveMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem browseMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem auditTrailMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem showRepositoryItemMenuItem = null;

    JMenuItem showRegistryObjectMenuItem = null;
    
    /** DOCUMENT ME! */
    MouseListener popupListener;
    private boolean editable = false;

    /** DOCUMENT ME! */
    final RegistryObjectsTableModel tableModel;
    int stdRowHeight = 0;

    /**
     * Class Constructor.
     *
     * @param model
     *
     * @see
     */
    public RegistryObjectsTable(TableModel model) {
        super(model);

        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        if (model instanceof RegistryObjectsTableModel) {
            tableModel = (RegistryObjectsTableModel) model;
        } else if (model instanceof TableSorter) {
            tableModel = (RegistryObjectsTableModel) (((TableSorter) model).getModel());
        } else {
			Object[] unexpectedTableModelArgs = {model};
			MessageFormat form =
				new MessageFormat(resourceBundle.
								  getString("error.unexpectedTableModel"));
            throw new IllegalArgumentException(form.format(unexpectedTableModelArgs));
        }

        setToolTipText(resourceBundle.getString("tip.registryObjectsTable"));

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        ListSelectionModel rowSM = getSelectionModel();
        stdRowHeight = getRowHeight();
        setRowHeight(stdRowHeight * 3);

        rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                    if (!lsm.isSelectionEmpty()) {
                        setSelectedRow(lsm.getMinSelectionIndex());
                    } else {
                        setSelectedRow(-1);
                    }
                }
            });

        createPopup();

        addRenderers();

        // Add listener to self so that I can bring up popup menus on right mouse click
        popupListener = new PopupListener();
        addMouseListener(popupListener);

        //add listener for 'authenticated' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(RegistryBrowser.PROPERTY_AUTHENTICATED,
            this);

        //add listener for 'locale' bound property
		RegistryBrowser.getInstance().
			addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
									  this);
    }

    private void addRenderers() {
        try {
            setDefaultRenderer(Class.forName("java.lang.Object"),
                new JBDefaultTableCellRenderer());
            setDefaultRenderer(Class.forName("java.util.Collection"),
                new CollectionRenderer());

            final JList list = new JList();
            list.setVisibleRowCount(3);
            list.setOpaque(true);
            list.setCellRenderer(new ListDefaultRenderer());

            list.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(
                        javax.swing.event.ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            Object obj = list.getSelectedValue();
                            obj = RegistryObjectsTable.convertValue(obj);

                            if (obj instanceof URL) {
                                HyperLinker.displayURL(obj.toString());
                            } else if (obj instanceof String) {
                                //Check if URL is valid
                                URL _url = null;

                                try {
                                    _url = new URL(obj.toString());
                                    HyperLinker.displayURL(obj.toString());
                                } catch (java.net.MalformedURLException exc) {
                                    //No need to do anything. It is normal for text to not be a URL
                                }
                            }
                        }
                    }
                });

            JScrollPane listPane = new JScrollPane(list);
            setDefaultEditor(Class.forName("java.util.Collection"),
                new JBDefaultCellEditor(listPane));

            HyperLinkLabel hyperLinkLabel = new HyperLinkLabel();
            hyperLinkLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            setDefaultEditor(Class.forName("java.lang.Object"),
                new JBDefaultCellEditor(hyperLinkLabel));
        } catch (ClassNotFoundException e) {
        }
    }

    public Class getColumnClass(int column) {
        setColumnWidth(column);

        Class clazz = tableModel.getColumnClass(column);

        return clazz;
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        TableCellRenderer renderer = null;
        String columnName = tableModel.getColumnName(column);
        Class clazz = null;

        try {
            clazz = tableModel.getColumnClass(column);
            renderer = super.getCellRenderer(row, column);

            if (renderer == null) {
				Object[] unsupportedColumnClassArgs = {new String(columnName),
													   clazz};
				MessageFormat form =
					new MessageFormat(resourceBundle.
									  getString("error.unsupportedColumnClass"));
				RegistryBrowser.displayError(form.format(unsupportedColumnClassArgs));
            }
        } catch (Exception e) {
			Object[] unsupportedColumnClassArgs = {new String(columnName),
												   clazz};
			MessageFormat form =
				new MessageFormat(resourceBundle.
								  getString("error.unsupportedColumnClass"));
			RegistryBrowser.displayError(form.format(unsupportedColumnClassArgs),
										 e);
        }

        return renderer;
    }

    private void createPopup() {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            ConnectionImpl connection = (ConnectionImpl) client.getConnection();
            boolean authenticated = connection.isAuthenticated();

            // Create popup menu for table
            popup = new JPopupMenu();

            if (editable) {
                editMenuItem = new JMenuItem(resourceBundle.getString("menu.edit"));
            } else {
                editMenuItem = new JMenuItem(resourceBundle.getString("menu.showDetails"));
            }

            popup.add(editMenuItem);
            removeMenuItem = new JMenuItem(resourceBundle.getString("menu.remove"));
            popup.add(removeMenuItem);
            saveMenuItem = new JMenuItem(resourceBundle.getString("menu.save"));
            popup.add(saveMenuItem);
            browseMenuItem = new JMenuItem(resourceBundle.getString("menu.browse"));
            popup.add(browseMenuItem);
            auditTrailMenuItem = new JMenuItem(resourceBundle.getString("menu.showAuditTrail"));
            popup.add(auditTrailMenuItem);
            showRegistryObjectMenuItem =
				new JMenuItem(resourceBundle.getString("menu.showRegistryObject"));
            popup.add(showRegistryObjectMenuItem);
            showRepositoryItemMenuItem =
				new JMenuItem(resourceBundle.getString("menu.showRepositoryItem"));
            showRepositoryItemMenuItem.setVisible(false);
            popup.add(showRepositoryItemMenuItem);

            editMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        editAction();
                    }
                });

            removeMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        removeAction();
                    }
                });

            saveMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        saveAction();
                    }
                });

            browseMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        browseAction();
                    }
                });

            auditTrailMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        auditTrailAction();
                    }
                });

            showRegistryObjectMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        showRegistryObjectAction();
                    }
                });

            showRepositoryItemMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        showRepositoryItemAction();
                    }
                });

            removeMenuItem.setVisible(authenticated);
            saveMenuItem.setVisible(authenticated);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void editAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length == 1) {
            showSelectedObjectDetails();
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.editDetailsAction"));
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    ArrayList getSelectedRegistryObjects() {
        ArrayList selectedObjects = new ArrayList();
        int[] selectedIndices = getSelectedRows();

        for (int i = 0; i < selectedIndices.length; i++) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[i],
                    -1);
            selectedObjects.add(ro);
        }

        return selectedObjects;
    }

    /**
     * DOCUMENT ME!
     */
    protected void removeAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length >= 1) {
            try {
                ArrayList selectedObjects = getSelectedRegistryObjects();
                ArrayList removeKeys = new ArrayList();

                int size = selectedObjects.size();

                for (int i = size - 1; i >= 0; i--) {
                    RegistryObject obj = (RegistryObject) selectedObjects.get(i);
                    Key key = obj.getKey();
                    removeKeys.add(key);
                }

                // Confirm the remove
                boolean confirmRemoves = true;
				// I18N: Do not localize next statement.
                String confirmRemovesStr = ProviderProperties.getInstance()
                                                             .getProperty("jaxr-ebxml.registryBrowser.confirmRemoves",
																		  "true");

                if (confirmRemovesStr.toLowerCase().equals("false") ||
                        confirmRemovesStr.toLowerCase().equals("off")) {
                    confirmRemoves = false;
                }

                if (confirmRemoves) {
                    int option =
						JOptionPane.showConfirmDialog(null,
													  resourceBundle.getString("dialog.confirmRemove.text"),
													  resourceBundle.getString("dialog.confirmRemove.title"),
													  JOptionPane.YES_NO_OPTION);

                    if (option == JOptionPane.NO_OPTION) {
                        RegistryBrowser.setDefaultCursor();

                        return;
                    }
                }

                JAXRClient client = RegistryBrowser.getInstance().getClient();
                BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
                BulkResponse resp = lcm.deleteObjects(removeKeys);
                client.checkBulkResponse(resp);

                if (resp.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                    //Remove from UI model
                    ArrayList objects = (ArrayList) ((tableModel.getRegistryObjects()).clone());
                    size = selectedIndices.length;

                    for (int i = size - 1; i >= 0; i--) {
                        RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[i],
                                -1);
                        objects.remove(ro);
                    }

                    tableModel.setRegistryObjects(objects);
                }
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.removeAction"));
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void saveAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length >= 1) {
            ArrayList selectedObjects = getSelectedRegistryObjects();

            try {
                JAXRClient client = RegistryBrowser.getInstance().getClient();
                client.saveObjects(selectedObjects);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.saveAction"));
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void browseAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length >= 1) {
            ArrayList selectedObjects = getSelectedRegistryObjects();
            Component parent = SwingUtilities.getRoot(RegistryObjectsTable.this);

            if (parent instanceof JFrame) {
                JBGraphPanel.browseObjects((JFrame) parent, selectedObjects,
                    editable);
            } else if (parent instanceof JDialog) {
                JBGraphPanel.browseObjects((JDialog) parent, selectedObjects,
                    editable);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.browseAction"));
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void auditTrailAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedRow,
																	  -1);
            RegistryBrowser.showAuditTrail(ro);
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.auditTrail"));
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void showRegistryObjectAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[0],
                    -1);
            Component parent = SwingUtilities.getRoot(RegistryObjectsTable.this);
            RegistryBrowser.showRegistryObject(ro);
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.showRegistryObjectAction"));
        }

        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void showRepositoryItemAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[0],
                    -1);
            Component parent = SwingUtilities.getRoot(RegistryObjectsTable.this);
            RegistryBrowser.showRepositoryItem(ro);
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.showRepositoryItemAction"));
        }

        RegistryBrowser.setDefaultCursor();
    }

    private void setColumnWidth(int col) {
        TableColumnModel colModel = getColumnModel();
        TableColumn tcol = colModel.getColumn(col);
        Component parent = getParent();

        if ((tcol.getMaxWidth() > maxColWidth) && (parent != null)) {
            int parentWidth = parent.getWidth();

            if (tableModel != null) {
                int width = tableModel.getColumnWidth(col);

                int cols = tableModel.getColumnCount();

                if (width == 0) {
                    width = parentWidth / cols;
                } else {
                    //Width is a % of the viewport width
                    width = (width * parentWidth) / 100;
                }

                tcol.setPreferredWidth(width);

                //Hack to not keep setting columnWidth which prevented user from resing teh column manually
                tcol.setMaxWidth(maxColWidth);
            }
        }
    }

    /**
     * Overrides base class behaviour by setting selection when first
     * row (destination) is added to model
     *
     * @param e DOCUMENT ME!
     */
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);

        // If no selectedRow, set selectedRow to firstRow
        if ((selectedRow == -1) && (e.getType() == TableModelEvent.INSERT)) {
            // Following will result in a software initiated selection
            // of the first row in table
            ListSelectionModel rowSM = getSelectionModel();

            rowSM.setSelectionInterval(0, 0);
        }
    }

    /**
     * Sets the currently selected row in table Also does
     * firePropertyChange on property "selectedRow"
     *
     * @param index DOCUMENT ME!
     */
    private void setSelectedRow(int index) {
        Integer oldIndex = new Integer(selectedRow);

        selectedRow = index;
        firePropertyChange(SELECTED_ROW_PROP, oldIndex, new Integer(index));
    }

    /**
     * Method Declaration.
     *
     * @param makeVisible
     *
     * @see
     */
    public void setVisible(boolean makeVisible) {
        //jimbog        Log.print(Log.TRACE,1,"Destination table visible:" + makeVisible);
        if (makeVisible) {
        }

        super.setVisible(makeVisible);
    }

    /**
     * DOCUMENT ME!
     */
    private void showSelectedObjectDetails() {
        if (selectedRow >= 0) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedRow,
                    -1);
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        }
    }

    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_AUTHENTICATED.  Certain menuItems are
     * hidden when user is unAuthenticated.
	 *
	 * Listens to property changes in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.  Updates locale and UI strings
	 * when the property changes.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {
            boolean authenticated = ((Boolean) ev.getNewValue()).booleanValue();

            setEditable(authenticated);
        } else if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
		}
	}

	/**
	 * Processes a change in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		resourceBundle = JAXRResourceBundle.getInstance();

		setLocale(newLocale);
		setDefaultLocale(newLocale);

		updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
        setToolTipText(resourceBundle.getString("tip.registryObjectsTable"));

		if (editable) {
			editMenuItem.setText(resourceBundle.getString("menu.edit"));
		} else {
			editMenuItem.setText(resourceBundle.getString("menu.showDetails"));
		}

		removeMenuItem.setText(resourceBundle.getString("menu.remove"));
		saveMenuItem.setText(resourceBundle.getString("menu.save"));
		browseMenuItem.setText(resourceBundle.getString("menu.browse"));
		auditTrailMenuItem.setText(resourceBundle.getString("menu.showAuditTrail"));
		showRegistryObjectMenuItem.setText(resourceBundle.getString("menu.showRegistryObject"));
		showRepositoryItemMenuItem.setText(resourceBundle.getString("menu.showRepositoryItem"));
	}

    /**
     * Converts an Object value to a format suitable for display in JTable.
     */
    protected static Object convertValue(Object value) {
        Object finalValue = null;
        Locale selectedLocale = RegistryBrowser.getInstance().getSelectedLocale();

        try {
            if (value instanceof InternationalString) {
                finalValue = ((InternationalStringImpl) value).getClosestValue(selectedLocale);
            } else if (value instanceof ExternalLink) {
                finalValue = ((ExternalLink) value).getExternalURI();

                try {
                    URL url = new URL(((ExternalLink) value).getExternalURI());
                    finalValue = url;
                } catch (java.net.MalformedURLException e) {
                }
            } else if (value instanceof Collection) {
                //Converts elements of Collection
                Collection c1 = (Collection) value;
                Collection c2 = new ArrayList();
                java.util.Iterator iter = c1.iterator();

                while (iter.hasNext()) {
                    c2.add(convertValue(iter.next()));
                }

                finalValue = c2;
            } else if (value instanceof Slot) {
                Collection c = ((Slot) value).getValues();
                finalValue = c;
            } else if (value instanceof Concept) {
                finalValue = ((Concept) value).getValue();
            } else {
                finalValue = value;
            }
        } catch (JAXRException e) {
            log.error(e);
        }

        return finalValue;
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        createPopup();
    }

    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Renderer used to render all Collection types.
     * Uses a JList to display the Collection.
     */
    class CollectionRenderer extends JScrollPane implements TableCellRenderer {
        JList list;

        public CollectionRenderer() {
            list = new JList();

            Font font = RegistryObjectsTable.this.getFont();
            list.setFont(font);
            list.setVisibleRowCount(3);
            list.setOpaque(true);

            ListDefaultRenderer renderer = new ListDefaultRenderer();
            renderer.setHorizontalAlignment(SwingConstants.TRAILING);
            list.setCellRenderer(renderer);
            list.setBorder(BorderFactory.createEmptyBorder());
            this.setBorder(BorderFactory.createEmptyBorder());

            this.setViewportView(list);

            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
        }

        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
            value = RegistryObjectsTable.convertValue(value);

            if (isSelected) {
                list.setBackground(table.getSelectionBackground());
                list.setForeground(table.getSelectionForeground());
            } else {
                list.setBackground(table.getBackground());
                list.setForeground(table.getForeground());
            }

            DefaultListModel model = new DefaultListModel();
            Collection c = (Collection) value;

            if (c != null) {
                java.util.Iterator iter = c.iterator();

                while (iter.hasNext()) {
                    model.addElement(iter.next());
                }
            }

            list.setModel(model);

            return this;
        }
    }

    /**
     * Editor used to edit all types.
     * Adds support for Collection and URL types to DefaultCellEditor
     */
    class JBDefaultCellEditor extends DefaultCellEditor {
        /**
         * Constructs a <code>JBDefaultCellEditor</code> that uses a JList field.
         *
         * @param x  a <code>JList</code> object
         */
        public JBDefaultCellEditor(final JScrollPane scrollPane) {
            super(new JTextField());
            editorComponent = scrollPane;

            this.clickCountToStart = 1;

            final Component comp = scrollPane.getViewport().getView();
            delegate = new EditorDelegate() {
                        public void setValue(Object value) {
                            value = convertValue(value);

                            if (comp instanceof JList) {
                                JList list = (JList) comp;
                                DefaultListModel model = new DefaultListModel();

                                if (value instanceof Collection) {
                                    Collection c = (Collection) value;
                                    java.util.Iterator iter = c.iterator();

                                    while (iter.hasNext()) {
                                        Object obj = iter.next();
                                        model.addElement(obj);
                                    }
                                }

                                list.setModel(model);
                            }
                        }

                        public Object getCellEditorValue() {
                            Object value = null;

                            if (comp instanceof JList) {
                                Collection c = new ArrayList();
                                JList list = (JList) comp;
                                ListModel model = (ListModel) list.getModel();

                                for (int i = 0; i < model.getSize(); i++) {
                                    c.add(model.getElementAt(i));
                                }

                                value = c;
                            }

                            return value;
                        }
                    };

            //list.addActionListener(delegate);
        }

        /**
         * Constructs a <code>JBDefaultCellEditor</code> that uses a JList field.
         *
         * @param x  a <code>JList</code> object
         */
        public JBDefaultCellEditor(final HyperLinkLabel label) {
            super(new JTextField());

            //list.setDefaultRenderer(Class.forName("java.net.URL"), new URLRenderer());
            editorComponent = label;
            this.clickCountToStart = 1;
            delegate = new EditorDelegate() {
                        public void setValue(Object value) {
                            try {
                                label.setURL(null);
                            } catch (java.net.MalformedURLException e) {
                                //Do nothing as this will never be thrown here. 
                            }

                            label.setText(value.toString());
                        }

                        public Object getCellEditorValue() {
                            return label.getText();
                        }
                    };

            //list.addActionListener(delegate);
        }
    }

    class ListDefaultRenderer extends HyperLinkLabel implements ListCellRenderer {
        public ListDefaultRenderer() {
            setOpaque(true);

            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            Font font = RegistryObjectsTable.this.getFont();
            setFont(font);
            list.setFont(font);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            value = RegistryObjectsTable.convertValue(value);
            setText(value.toString());

            return this;
        }
    }

    /**
     * Class Declaration.
     *
     * @author
     * @version 1.9, 03/29/00
     *
     * @see
     */
    class PopupListener extends MouseAdapter {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mousePressed(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int index = rowAtPoint(p);

            Object ro = dataModel.getValueAt(index, -1);

            if (ro instanceof ExtrinsicObject) {
                showRepositoryItemMenuItem.setVisible(true);
            } else {
                showRepositoryItemMenuItem.setVisible(false);
            }

            maybeShowPopup(e);

            if (e.getClickCount() > 1) {
                //showSelectedObjectDetails();
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int[] selectedIndices = getSelectedRows();
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */

package com.sun.xml.registry.client.browser;

import com.sun.xml.registry.client.browser.graph.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import com.sun.xml.registry.client.browser.JAXRClient;
import com.sun.xml.registry.client.browser.RegistryBrowser;
import com.sun.xml.registry.ebxml.ConnectionImpl;
import com.sun.xml.registry.ebxml.util.ProviderProperties;
import com.sun.xml.registry.ebxml.infomodel.InternationalStringImpl;

import java.net.URL;
import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A JTable that lists
 *
 * @author Jim Glennon
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectsTable extends JTable implements java.beans.PropertyChangeListener {

    private static final Log log =
        LogFactory.getLog(RegistryObjectsTableModel.class);
    
    /** DOCUMENT ME! */
    public static final String SELECTED_ROW_PROP = "selectedRow";
    
    //Hack to not keep setting columnWidth which prevented user from resing teh column manually
    private static final int maxColWidth = 1000000;
    

    /** DOCUMENT ME! */
    int selectedRow  = -1;

    /** DOCUMENT ME! */
    JPopupMenu popup;

    /** DOCUMENT ME! */
    JMenuItem editMenuItem                     = null;

    /** DOCUMENT ME! */
    JMenuItem removeMenuItem                   = null;

    /** DOCUMENT ME! */
    JMenuItem saveMenuItem                     = null;

    /** DOCUMENT ME! */
    JMenuItem browseMenuItem                   = null;

    /** DOCUMENT ME! */
    JMenuItem auditTrailMenuItem               = null;

    /** DOCUMENT ME! */
    JMenuItem retrieveMenuItem                 = null;

    /** DOCUMENT ME! */
    MouseListener popupListener;
    
    private boolean editable=false;

    /** DOCUMENT ME! */
    final RegistryObjectsTableModel tableModel;
    
    int stdRowHeight = 0;

    /**
     * Class Constructor.
     *
     * @param model
     *
     * @see
     */
    public RegistryObjectsTable(TableModel model) {
        super(model);
        
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        if (model instanceof RegistryObjectsTableModel) {
            tableModel = (RegistryObjectsTableModel)model;
        }
        else if (model instanceof TableSorter) {
            tableModel = (RegistryObjectsTableModel)(((TableSorter)model).getModel());
        }
        else {
            throw new IllegalArgumentException("Error. Expected RegistryObjectsTableModel or TableSorter, got " + model);
        }
        setToolTipText("Use right mouse button to perform actions (e.g. edit, save, browse, show audit trail)");
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        ListSelectionModel rowSM = getSelectionModel();
        stdRowHeight = getRowHeight();
        setRowHeight(stdRowHeight * 3);

        rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {

                    ListSelectionModel lsm =
                                       (ListSelectionModel)e.getSource();

                    if (!lsm.isSelectionEmpty()) {
                        setSelectedRow(lsm.getMinSelectionIndex());
                    } else {
                        setSelectedRow(-1);
                    }
                }
            });

        createPopup();
        
        addRenderers();
        
        // Add listener to self so that I can bring up popup menus on right mouse click
        popupListener = new PopupListener();
        addMouseListener(popupListener);
        
        //add lister authenticated bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(RegistryBrowser.PROPERTY_AUTHENTICATED, this);
    }
    
    private void addRenderers() {
        try {
            setDefaultRenderer(Class.forName("java.lang.Object"), new JBDefaultTableCellRenderer());
            setDefaultRenderer(Class.forName("java.util.Collection"), new CollectionRenderer());
            
            final JList list = new JList();
            list.setVisibleRowCount(3);
            list.setOpaque(true);
            list.setCellRenderer(new ListDefaultRenderer());

            list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        Object obj = list.getSelectedValue();
                        obj = RegistryObjectsTable.this.convertValue(obj);
                        if (obj instanceof URL) {
                            HyperLinker.displayURL(obj.toString());
                        }
                        else if (obj instanceof String) {
                            //Check if URL is valid
                            URL _url = null;
                            try {
                                _url = new URL(obj.toString());
                                HyperLinker.displayURL(obj.toString());
                            }
                            catch (java.net.MalformedURLException exc) {            
                                //No need to do anything. It is normal for text to not be a URL
                            }
                            
                        }
                    }
                }
            });
            JScrollPane listPane = new JScrollPane(list);            
            setDefaultEditor(Class.forName("java.util.Collection"), new JBDefaultCellEditor(listPane));
            
            HyperLinkLabel hyperLinkLabel = new HyperLinkLabel();
            hyperLinkLabel.setHorizontalAlignment(SwingConstants.LEFT);
            setDefaultEditor(Class.forName("java.lang.Object"), new JBDefaultCellEditor(hyperLinkLabel));            
        }
        catch (ClassNotFoundException e) {
        }
    }
    
    public Class getColumnClass(int column) {
        setColumnWidth(column);
        
        Class clazz = tableModel.getColumnClass(column);
        
        return clazz;
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableCellRenderer renderer = null;
        String columnName = tableModel.getColumnName(column);
        Class clazz = null;
            
        try {
            clazz = tableModel.getColumnClass(column);
            renderer = super.getCellRenderer(row, column);
            
            if (renderer == null) {
                RegistryBrowser.displayError("Configuration error for column: " + columnName + ". Unsupported columnClass:" + clazz);
            }
        }
        catch (Exception e) {
            RegistryBrowser.displayError("Configuration error for column: " + columnName + ". Unsupported columnClass:" + clazz, e);
        }
        
        return renderer;
    }

    private void createPopup() {
        try {

            JAXRClient client = RegistryBrowser.getInstance().getClient();
            ConnectionImpl connection = (ConnectionImpl)client.getConnection();
            boolean authenticated = connection.isAuthenticated();
        
            // Create popup menu for table
            popup     = new JPopupMenu();

            if (editable) {
                editMenuItem = new JMenuItem("Edit");
            }
            else {
                editMenuItem = new JMenuItem("Show Details");
            }
            popup.add(editMenuItem);
            removeMenuItem = new JMenuItem("Remove");
            popup.add(removeMenuItem);
            saveMenuItem = new JMenuItem("Save");
            popup.add(saveMenuItem);
            browseMenuItem = new JMenuItem("Browse");
            popup.add(browseMenuItem);
            auditTrailMenuItem = new JMenuItem("Show Audit Trail");
            popup.add(auditTrailMenuItem);
            retrieveMenuItem = new JMenuItem("Retrieve Item");
            retrieveMenuItem.setVisible(false);
            popup.add(retrieveMenuItem);            
            
            editMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        editAction();
                    }
                });

            removeMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        removeAction();
                    }
                });

            saveMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        saveAction();
                    }
                });

            browseMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        browseAction();
                    }
                });

            auditTrailMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        auditTrailAction();
                    }
                });

            retrieveMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        retrieveAction();
                    }
                });
                
            removeMenuItem.setVisible(authenticated);
            saveMenuItem.setVisible(authenticated);
        
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

    }
    
    /**
     * DOCUMENT ME!
     */
    protected void editAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length == 1) {
            showSelectedObjectDetails();
        } else {
            RegistryBrowser.displayError("Exactly one object must be selected in list for Show Details/Edit action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    ArrayList getSelectedRegistryObjects() {

        ArrayList selectedObjects = new ArrayList();
        int[] selectedIndices     = getSelectedRows();

        for (int i = 0; i < selectedIndices.length; i++) {
            RegistryObject ro = (RegistryObject)dataModel.getValueAt(selectedIndices[i], -1);
            selectedObjects.add(ro);
        }

        return selectedObjects;
    }

    /**
     * DOCUMENT ME!
     */
    protected void removeAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length >= 1) {

            try {

                ArrayList selectedObjects =
                    getSelectedRegistryObjects();
                ArrayList removeKeys      = new ArrayList();

                int size = selectedObjects.size();

                for (int i = size - 1; i >= 0; i--) {

                    RegistryObject obj =
                        (RegistryObject)selectedObjects.get(i);
                    Key key            = obj.getKey();
                    removeKeys.add(key);
                }

				// Confirm the remove
				boolean confirmRemoves = true;
				String confirmRemovesStr = ProviderProperties.getInstance().getProperty
					("jaxr-ebxml.registryBrowser.confirmRemoves", "true");
				if (confirmRemovesStr.toLowerCase().equals("false") ||
					confirmRemovesStr.toLowerCase().equals("off"))
				{
					confirmRemoves = false;
				}
				if (confirmRemoves) {
					int option = JOptionPane.showConfirmDialog(null, 
						"Are you sure you want to remove the selected " +
						"items from the system?", "Confirm Remove", 
						JOptionPane.YES_NO_OPTION);
					if (option == JOptionPane.NO_OPTION) {
				        RegistryBrowser.setDefaultCursor();
						return;
					}
				}
				
                JAXRClient client            =
                    RegistryBrowser.getInstance().getClient();
                BusinessLifeCycleManager lcm =
                    client.getBusinessLifeCycleManager();
                BulkResponse resp            =
                    lcm.deleteObjects(removeKeys);
                client.checkBulkResponse(resp);

                if (resp.getStatus() == JAXRResponse.STATUS_SUCCESS) {

                    //Remove from UI model
                    ArrayList objects =
                        (ArrayList)((tableModel.getRegistryObjects())
                         .clone());
                    size = selectedIndices.length;

                    for (int i = size - 1; i >= 0; i--) {

                        RegistryObject ro = (RegistryObject)dataModel.getValueAt(selectedIndices[i], -1);
                        objects.remove(ro);
                    }

                    tableModel.setRegistryObjects(objects);
                }
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError("One or more objects must be selected in list for Remove action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void saveAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length >= 1) {

            ArrayList selectedObjects = getSelectedRegistryObjects();

            try {

                JAXRClient client =
                    RegistryBrowser.getInstance().getClient();
                client.saveObjects(selectedObjects);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError("One or more objects must be selected in list for Save action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void browseAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length >= 1) {

            ArrayList selectedObjects = getSelectedRegistryObjects();
            Component parent          =
                SwingUtilities.getRoot(RegistryObjectsTable.this);

            if (parent instanceof JFrame) {
                JBGraphPanel.browseObjects((JFrame)parent, selectedObjects, editable);
            } else if (parent instanceof JDialog) {
                JBGraphPanel.browseObjects((JDialog)parent,
                                         selectedObjects, editable);
            }
        } else {
            RegistryBrowser.displayError("One or more objects must be selected in list for Browse action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void auditTrailAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject)dataModel.getValueAt(selectedRow, -1);
            RegistryBrowser.showAuditTrail(ro);
        } else {
            RegistryBrowser.displayError("Exactly one object must be selected in list for Show Audit Trail action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void retrieveAction() {
        RegistryBrowser.setWaitCursor();

        int[] selectedIndices = getSelectedRows();

        if (selectedIndices.length == 1) {

            RegistryObject ro = (RegistryObject)dataModel.getValueAt(selectedIndices[0], -1);
            Component parent =
                SwingUtilities.getRoot(RegistryObjectsTable.this);
            RegistryBrowser.retrieveItem(ro);
        } else {
            RegistryBrowser.displayError("Exactly one object must be selected in list for Retrieve action.");
        }

        RegistryBrowser.setDefaultCursor();
    }
        
    private void setColumnWidth(int col) {
        TableColumnModel colModel = getColumnModel();
        TableColumn tcol = colModel.getColumn(col);
        Component parent = getParent();
        
        if ((tcol.getMaxWidth() > maxColWidth) && (parent != null)) {
            int parentWidth = parent.getWidth();
            
            if (tableModel != null) {
                int width = tableModel.getColumnWidth(col);

	        int cols = tableModel.getColumnCount();
                if (width == 0 ) {
                    width = parentWidth / cols;
                }
                else {
                    //Width is a % of the viewport width
                    width = (width * parentWidth) / 100;
                }
                tcol.setPreferredWidth(width);

                //Hack to not keep setting columnWidth which prevented user from resing teh column manually
                tcol.setMaxWidth(maxColWidth);
            }
        }
        
    }

    /**
     * Overrides base class behaviour by setting selection when first
     * row (destination) is added to model
     *
     * @param e DOCUMENT ME!
     */
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        
        // If no selectedRow, set selectedRow to firstRow
        if ((selectedRow == -1)
            && (e.getType() == TableModelEvent.INSERT)) {

            // Following will result in a software initiated selection
            // of the first row in table
            ListSelectionModel rowSM = getSelectionModel();

            rowSM.setSelectionInterval(0, 0);
        }
    }

    /**
     * Sets the currently selected row in table Also does
     * firePropertyChange on property "selectedRow"
     *
     * @param index DOCUMENT ME!
     */
    private void setSelectedRow(int index) {

        Integer oldIndex = new Integer(selectedRow);

        selectedRow = index;
        firePropertyChange(SELECTED_ROW_PROP, oldIndex,
                           new Integer(index));
    }

    /**
     * Method Declaration.
     *
     * @param makeVisible
     *
     * @see
     */
    public void setVisible(boolean makeVisible) {

        //jimbog        Log.print(Log.TRACE,1,"Destination table visible:" + makeVisible);
        if (makeVisible) {
        }

        super.setVisible(makeVisible);
    }

    /**
     * DOCUMENT ME!
     */
    private void showSelectedObjectDetails() {

        if (selectedRow >= 0) {
            RegistryObject ro = (RegistryObject)dataModel.getValueAt(selectedRow, -1);
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        }
    }
    
    /**
     * Listens to property changes in the bound property RegistryBrowser.PROPERTY_AUTHENTICATED.
     * Hides certain menuItems when user is unAuthenticated.
     */
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {            
          
            boolean authenticated = ((Boolean)ev.getNewValue()).booleanValue();

            setEditable(authenticated);
        }
    }    

    /**
     * Converts an Object value to a format suitable for display in JTable.
     */
    protected static Object convertValue(Object value)
    {
        Object finalValue = null;
        Locale selectedLocale = RegistryBrowser.getInstance().getSelectedLocale();
        
        try {
        
            if (value instanceof InternationalString)
            {
                finalValue = ((InternationalStringImpl)value).getClosestValue(selectedLocale);
            }
            else if (value instanceof ExternalLink)
            {
                finalValue = ((ExternalLink)value).getExternalURI();
                try {
                    URL url = new URL(((ExternalLink)value).getExternalURI());
                    finalValue = url;
                }
                catch (java.net.MalformedURLException e) {
                }
            }
            else if (value instanceof Collection)
            {
                //Converts elements of Collection
                Collection c1 = (Collection)value;
                Collection c2 = new ArrayList();
                java.util.Iterator iter = c1.iterator();
                while (iter.hasNext()) {
                    c2.add(convertValue(iter.next()));
                }
                finalValue = c2;
            }
            else if (value instanceof Slot)
            {
                Collection c = ((Slot)value).getValues();
                finalValue = c;
            }
            else if (value instanceof Concept)
            {
                finalValue = ((Concept)value).getValue();
            }
            else
            {
                finalValue = value;         
            }
        }
        catch (JAXRException e) {
            log.error(e);
        }
        return finalValue;
    }
        	    
    /**
     * Renderer used to render all Collection types.
     * Uses a JList to display the Collection.
     */
    class CollectionRenderer extends JScrollPane implements TableCellRenderer	{
        
        JList list;
    	public CollectionRenderer() {
            list = new JList();
            Font font = RegistryObjectsTable.this.getFont();
            list.setFont(font);
            list.setVisibleRowCount(3);
            list.setOpaque(true);
            ListDefaultRenderer renderer = new ListDefaultRenderer();
            renderer.setHorizontalAlignment(SwingConstants.LEFT);
            list.setCellRenderer(renderer);
            list.setBorder(BorderFactory.createEmptyBorder());
            this.setBorder(BorderFactory.createEmptyBorder());
            
            this.setViewportView(list);
            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
            
    	}
    	        
    	public Component getTableCellRendererComponent(
            JTable table, 
            Object value, 
            boolean isSelected, 
            boolean hasFocus, 
            int row, 
            int column) 
        {
            value = RegistryObjectsTable.this.convertValue(value);
    		            
            if (isSelected) {
                list.setBackground(table.getSelectionBackground());
                list.setForeground(table.getSelectionForeground());
            }
            else {
                list.setBackground(table.getBackground());
                list.setForeground(table.getForeground());
            }

            DefaultListModel model = new DefaultListModel();
            Collection c = (Collection)value;
            if (c != null) {
                java.util.Iterator iter = c.iterator();
                while (iter.hasNext()) {
                    model.addElement(iter.next());
                }
            }
            list.setModel(model);

            return this;
    	}                
    }
    
    /**
     * Editor used to edit all types.
     * Adds support for Collection and URL types to DefaultCellEditor
     */
    class JBDefaultCellEditor extends DefaultCellEditor	{
        
        /**
         * Constructs a <code>JBDefaultCellEditor</code> that uses a JList field.
         *
         * @param x  a <code>JList</code> object
         */
        public JBDefaultCellEditor(final JScrollPane scrollPane) {
            super(new JTextField());
            editorComponent = scrollPane;
            
            this.clickCountToStart = 1;
            final Component comp = scrollPane.getViewport().getView();
            delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    
                    value = convertValue(value);
                    if (comp instanceof JList) {
                        JList list = (JList)comp;
                        DefaultListModel model = new DefaultListModel();
                        
                        if (value instanceof Collection) {
                            Collection c = (Collection)value;
                            java.util.Iterator iter = c.iterator();
                            while (iter.hasNext()) {
                                Object obj = iter.next();
                                model.addElement(obj);
                            }
                        }
                        list.setModel(model);
                    }
                }

                public Object getCellEditorValue() {
                    Object value = null;
                    if (comp instanceof JList) {
                        Collection c = new ArrayList();
                        JList list = (JList)comp;
                        ListModel model = (ListModel)list.getModel();
                        for (int i=0; i<model.getSize(); i++) {
                            c.add(model.getElementAt(i));
                        }
                        value = c;
                    }
                    return value;
                }
            };
            //list.addActionListener(delegate);
        }

        /**
         * Constructs a <code>JBDefaultCellEditor</code> that uses a JList field.
         *
         * @param x  a <code>JList</code> object
         */
        public JBDefaultCellEditor(final HyperLinkLabel label) {
            super(new JTextField());
            //list.setDefaultRenderer(Class.forName("java.net.URL"), new URLRenderer());

            editorComponent = label;
            this.clickCountToStart = 1;
            delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    try {
                        label.setURL(null);
                    }
                    catch (java.net.MalformedURLException e) {
                        //Do nothing as this will never be thrown here. 
                    }
                    label.setText(value.toString());
                }

                public Object getCellEditorValue() {
                    return label.getText();
                }
            };
            //list.addActionListener(delegate);
        }                        
    }

    class ListDefaultRenderer extends HyperLinkLabel implements ListCellRenderer	{
        public ListDefaultRenderer() {
                setOpaque(true);
                //setHorizontalAlignment(CENTER);
                //setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,			
            boolean cellHasFocus) {

            Font font = RegistryObjectsTable.this.getFont();
            setFont(font);
            list.setFont(font);
                
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            value = RegistryObjectsTable.this.convertValue(value);
            setText(value.toString());

            return this;
        }
    }                       
    

    /**
     * Class Declaration.
     *
     * @author
     * @version 1.9, 03/29/00
     *
     * @see
     */
    class PopupListener extends MouseAdapter {

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mousePressed(MouseEvent e) {

            Point p   = new Point(e.getX(), e.getY());
            int index = rowAtPoint(p);

            Object ro = dataModel.getValueAt(index, -1);
            if (ro instanceof ExtrinsicObject) {
                retrieveMenuItem.setVisible(true);
            } else {
                retrieveMenuItem.setVisible(false);
            }

            maybeShowPopup(e);

            if (e.getClickCount() > 1) {
                //showSelectedObjectDetails();
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        private void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {

                int[] selectedIndices = getSelectedRows();
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        createPopup();
    }
    
    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return editable;
    }    
    
}
