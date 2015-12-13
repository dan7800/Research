/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/ConceptsTree.java,v 1.4 2004/03/09 15:56:28 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import org.freebxml.omar.client.ui.swing.graph.JBGraphPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.net.URL;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * A JTree that lists
 *
 * @author Jim Glennon
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConceptsTree extends JTree implements PropertyChangeListener {
    private static ConceptsTreeModel s_conceptsTreeModel = null;

    /** DOCUMENT ME! */
    JPopupMenu popup;

    /** DOCUMENT ME! */
    JMenuItem editMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem insertMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem removeMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem saveMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem browseMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem auditTrailMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem exportMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem retrieveMenuItem = null;

    /** DOCUMENT ME! */
    MouseListener popupListener;
    private boolean editable = RegistryBrowser.getInstance().isAuthenticated();

    /**
     * Class Constructor.
     *
     * @see
     */
    public ConceptsTree(boolean updateOnCreate) {
        if (s_conceptsTreeModel == null) {
            s_conceptsTreeModel = new ConceptsTreeModel(updateOnCreate);
        }

        this.setModel(s_conceptsTreeModel);

        setCellRenderer(new ConceptsTreeCellRenderer());
        setRootVisible(false);
        setShowsRootHandles(true);
        addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();

                    if (node == null) {
                        return;
                    }

                    Component c = SwingUtilities.getRoot(ConceptsTree.this);
                    Cursor oldCursor = c.getCursor();
                    RegistryBrowser.setWaitCursor();
                    ((ConceptsTreeModel) getModel()).expandTree(node, 1);
                    RegistryBrowser.setDefaultCursor();
                }
            });

        ((ConceptsTreeModel) getModel()).addTreeModelListener(new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent e) {
                }

                public void treeNodesInserted(TreeModelEvent e) {
                    //                expandPath(e.getTreePath());
                }

                public void treeNodesRemoved(TreeModelEvent e) {
                }

                public void treeNodesStructureChanged(TreeModelEvent e) {
                }

                public void treeStructureChanged(TreeModelEvent e) {
                }
            });

        createPopup();

        //add lister authenticated bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(RegistryBrowser.PROPERTY_AUTHENTICATED,
            this);

        setToolTipText("Classification Schemes");
    }

    /** Create popup menu for List */
    private void createPopup() {
        // Create popup menu for table
        popup = new JPopupMenu();

        if (editable) {
            editMenuItem = new JMenuItem("Edit");
        } else {
            editMenuItem = new JMenuItem("Show Details");
        }

        popup.add(editMenuItem);
        insertMenuItem = new JMenuItem("Insert");
        popup.add(insertMenuItem);
        removeMenuItem = new JMenuItem("Remove");
        popup.add(removeMenuItem);
        saveMenuItem = new JMenuItem("Save");
        popup.add(saveMenuItem);
        browseMenuItem = new JMenuItem("Browse");
        popup.add(browseMenuItem);
        auditTrailMenuItem = new JMenuItem("Show Audit Trail");
        popup.add(auditTrailMenuItem);
        exportMenuItem = new JMenuItem("Export");
        popup.add(exportMenuItem);

        boolean authenticated = RegistryBrowser.getInstance().isAuthenticated();

        insertMenuItem.setVisible(authenticated);
        removeMenuItem.setVisible(authenticated);
        saveMenuItem.setVisible(authenticated);

        editMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    editAction();
                }
            });

        insertMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    insertAction();
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

        exportMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    exportAction();
                }
            });

        // Add listener to self so that I can bring up popup menus on right mouse click
        popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getSelectedConcepts() {
        ArrayList objects = new ArrayList();

        TreePath[] treePaths = getSelectionPaths();

        if (treePaths != null) {
            NodeInfo nodeInfo = null;

            for (int i = 0; i < treePaths.length; ++i) {
                nodeInfo = (NodeInfo) (((DefaultMutableTreeNode) treePaths[i].getLastPathComponent()).getUserObject());

                if (nodeInfo.obj instanceof Concept) {
                    objects.add((Concept) nodeInfo.obj);
                }
            }
        }

        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getSelectedClassificationSchemes() {
        ArrayList objects = new ArrayList();

        TreePath[] treePaths = getSelectionPaths();

        if (treePaths != null) {
            NodeInfo nodeInfo = null;

            for (int i = 0; i < treePaths.length; ++i) {
                nodeInfo = (NodeInfo) (((DefaultMutableTreeNode) treePaths[i].getLastPathComponent()).getUserObject());

                if (nodeInfo.obj instanceof ClassificationScheme) {
                    objects.add(nodeInfo.obj);
                }
            }
        }

        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getSelectedObjects() {
        ArrayList objects = new ArrayList();

        TreePath[] treePaths = getSelectionPaths();

        if (treePaths != null) {
            NodeInfo nodeInfo = null;

            for (int i = 0; i < treePaths.length; ++i) {
                nodeInfo = (NodeInfo) (((DefaultMutableTreeNode) treePaths[i].getLastPathComponent()).getUserObject());
                objects.add(nodeInfo.obj);
            }
        }

        return objects;
    }

    /**
     * DOCUMENT ME!
     */
    protected void editAction() {
        RegistryBrowser.setWaitCursor();

        Object[] selectedObjects = getSelectedObjects().toArray();

        if (selectedObjects.length == 1) {
            RegistryObject ro = (RegistryObject) selectedObjects[0];
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        } else {
            RegistryBrowser.displayError(
                "Exactly one object must be selected in list for Edit action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void insertAction() {
        try {
            RegistryBrowser.setWaitCursor();

            ArrayList selectedObjects = getSelectedObjects();
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();

            int size = selectedObjects.size();

            if (size == 0) {
                //Add a new ClassificationScheme
                ClassificationScheme scheme = lcm.createClassificationScheme("Scheme Name (Change me)",
                        "Scheme description (Change me)");

                JBDialog dialog = JBEditorDialog.showObjectDetails(this,
                        scheme, true, editable);

                if (dialog.getStatus() != JBDialog.OK_STATUS) {
                    return;
                }

                //Now add to tree
                ((ConceptsTreeModel) getModel()).insertClassificationScheme(scheme);
            } else if (size == 1) {
                RegistryObject selectedObject = (RegistryObject) selectedObjects.get(0);

                Concept concept = lcm.createConcept(selectedObject,
                        "Concept Name (Change me)", "Value (Change me)");
                JBDialog dialog = JBEditorDialog.showObjectDetails(this,
                        concept, true, editable);

                if (dialog.getStatus() != JBDialog.OK_STATUS) {
                    return;
                }

                TreePath[] treePaths = getSelectionPaths();
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treePaths[0].getLastPathComponent();

                ((ConceptsTreeModel) getModel()).insertConcept(concept,
                    parentNode);
            } else {
                RegistryBrowser.displayError(
                    "Exactly one object must be selected in list for Insert action.");
            }

            RegistryBrowser.setDefaultCursor();
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void removeAction() {
        RegistryBrowser.setWaitCursor();

        ArrayList selectedObjects = getSelectedObjects();

        int size = selectedObjects.size();

        if (size >= 1) {
            try {
                ArrayList removeKeys = new ArrayList();

                for (int i = size - 1; i >= 0; i--) {
                    RegistryObject obj = (RegistryObject) selectedObjects.get(i);
                    Key key = obj.getKey();
                    removeKeys.add(key);
                }

                JAXRClient client = RegistryBrowser.getInstance().getClient();
                BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
                BulkResponse resp = lcm.deleteObjects(removeKeys);
                client.checkBulkResponse(resp);

                if (resp.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                    TreePath[] currentSelection = getSelectionPaths();

                    if (currentSelection != null) {
                        for (int j = 0; j < currentSelection.length; j++) {
                            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection[j].getLastPathComponent());
                            MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());

                            if (parent != null) {
                                ((ConceptsTreeModel) getModel()).removeNodeFromParent(currentNode);
                            }
                        }
                    }
                }
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(
                "One or more objects must be selected in list for Remove action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void saveAction() {
        RegistryBrowser.setWaitCursor();

        ArrayList selectedObjects = getSelectedObjects();

        int size = selectedObjects.size();

        if (size >= 1) {
            try {
                JAXRClient client = RegistryBrowser.getInstance().getClient();
                client.saveObjects(selectedObjects);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(
                "One or more objects must be selected in list for Save action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void browseAction() {
        RegistryBrowser.setWaitCursor();

        ArrayList selectedObjects = getSelectedObjects();

        int size = selectedObjects.size();

        if (size >= 1) {
            Component parent = SwingUtilities.getRoot(ConceptsTree.this);

            if (parent instanceof JFrame) {
                JBGraphPanel.browseObjects((JFrame) parent, selectedObjects,
                    editable);
            } else if (parent instanceof JDialog) {
                JBGraphPanel.browseObjects((JDialog) parent, selectedObjects,
                    editable);
            }
        } else {
            RegistryBrowser.displayError(
                "One or more objects must be selected in list for Browse action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void auditTrailAction() {
        RegistryBrowser.setWaitCursor();

        ArrayList selectedObjects = getSelectedObjects();

        int size = selectedObjects.size();

        if (size == 1) {
            RegistryObject ro = (RegistryObject) selectedObjects.get(0);
            RegistryBrowser.showAuditTrail(ro);
        } else {
            RegistryBrowser.displayError(
                "Exactly one object must be selected in list for Show Audit Trail action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void exportAction() {
        RegistryBrowser.setWaitCursor();

        ArrayList selectedObjects = getSelectedObjects();

        int size = selectedObjects.size();

        if (size >= 1) {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            client.exportObjects(selectedObjects);
        } else {
            RegistryBrowser.displayError(
                "One or more objects must be selected in list for Export action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    private void showSelectedObjectDetails() {
        ArrayList selectedObjects = getSelectedObjects();

        int size = selectedObjects.size();

        if (size == 1) {
            RegistryObject ro = (RegistryObject) selectedObjects.get(0);
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        }
    }

    public static void clearCache() {
        s_conceptsTreeModel = null;
    }

    /**
     * Listens to property changes in the bound property RegistryBrowser.PROPERTY_AUTHENTICATED.
     * Hides certain menuItems when user is unAuthenticated.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {
            boolean authenticated = ((Boolean) ev.getNewValue()).booleanValue();
            setEditable(editable);
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

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.4 $
     */
    public class ConceptsTreeCellRenderer extends JLabel
        implements TreeCellRenderer {
        /** Is the value currently selected. */
        protected boolean selected;

        // These two ivars will be made protected later.

        /** True if has focus. */
        private boolean hasFocus;

        /** True if draws focus border around icon as well. */
        private boolean drawsFocusBorderAroundIcon;

        // Icons

        /** Icon used to show non-leaf nodes that aren't expanded. */
        protected transient Icon closedIcon;

        /** Icon used to show leaf nodes. */
        protected transient Icon leafIcon;

        /** Icon used to show non-leaf nodes that are expanded. */
        protected transient Icon openIcon;

        // Colors

        /** Color to use for the foreground for selected nodes. */
        protected Color textSelectionColor;

        /** Color to use for the foreground for non-selected nodes. */
        protected Color textNonSelectionColor;

        /** Color to use for the background when a node is selected. */
        protected Color backgroundSelectionColor;

        /**
         * Color to use for the background when the node isn't
         * selected.
         */
        protected Color backgroundNonSelectionColor;

        /**
         * Color to use for the background when the node isn't
         * selected.
         */
        protected Color borderSelectionColor;

        /** DOCUMENT ME! */
        DefaultMutableTreeNode node = null;

        /** DOCUMENT ME! */
        Object obj = null;

        /**
         * Returns a new instance of DefaultTreeCellRenderer.
         * Alignment is set to left aligned. Icons and text color are
         * determined from the UIManager.
         */
        public ConceptsTreeCellRenderer() {
            setHorizontalAlignment(JLabel.TRAILING);
            setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
            setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
            setBackgroundSelectionColor(UIManager.getColor(
                    "Tree.selectionBackground"));
            setBackgroundNonSelectionColor(UIManager.getColor(
                    "Tree.textBackground"));
            setBorderSelectionColor(UIManager.getColor(
                    "Tree.selectionBorderColor"));

            Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");

            drawsFocusBorderAroundIcon = ((value != null) &&
                ((Boolean) value).booleanValue());
        }

        /**
         * Returns the default icon, for the current laf, that is used
         * to represent non-leaf nodes that are expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getDefaultOpenIcon() {
            return getConceptsTreeIcon();
        }

        /**
         * Returns the default icon, for the current laf, that is used
         * to represent non-leaf nodes that are not expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getDefaultClosedIcon() {
            return getConceptsTreeIcon();
        }

        /**
         * Returns the default icon, for the current laf, that is used
         * to represent leaf nodes.
         *
         * @return DOCUMENT ME!
         */
        public Icon getDefaultLeafIcon() {
            return getConceptsTreeIcon();
        }

        /**
         * Returns the icon used to represent non-leaf nodes that are
         * expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getOpenIcon() {
            return getConceptsTreeIcon();
        }

        /**
         * Returns the icon used to represent non-leaf nodes that are
         * not expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getClosedIcon() {
            return getConceptsTreeIcon();
        }

        /**
         * Returns the icon used to represent leaf nodes.
         *
         * @return DOCUMENT ME!
         */
        public Icon getLeafIcon() {
            return getConceptsTreeIcon();
        }

        /**
         * Sets the color the text is drawn with when the node is
         * selected.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setTextSelectionColor(Color newColor) {
            textSelectionColor = newColor;
        }

        /**
         * Returns the color the text is drawn with when the node is
         * selected.
         *
         * @return DOCUMENT ME!
         */
        public Color getTextSelectionColor() {
            return textSelectionColor;
        }

        /**
         * Sets the color the text is drawn with when the node isn't
         * selected.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setTextNonSelectionColor(Color newColor) {
            textNonSelectionColor = newColor;
        }

        /**
         * Returns the color the text is drawn with when the node isn't
         * selected.
         *
         * @return DOCUMENT ME!
         */
        public Color getTextNonSelectionColor() {
            return textNonSelectionColor;
        }

        /**
         * Sets the color to use for the background if node is
         * selected.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setBackgroundSelectionColor(Color newColor) {
            backgroundSelectionColor = newColor;
        }

        /**
         * Returns the color to use for the background if node is
         * selected.
         *
         * @return DOCUMENT ME!
         */
        public Color getBackgroundSelectionColor() {
            return backgroundSelectionColor;
        }

        /**
         * Sets the background color to be used for non selected nodes.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setBackgroundNonSelectionColor(Color newColor) {
            backgroundNonSelectionColor = newColor;
        }

        /**
         * Returns the background color to be used for non selected
         * nodes.
         *
         * @return DOCUMENT ME!
         */
        public Color getBackgroundNonSelectionColor() {
            return backgroundNonSelectionColor;
        }

        /**
         * Sets the color to use for the border.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setBorderSelectionColor(Color newColor) {
            borderSelectionColor = newColor;
        }

        /**
         * Returns the color the border is drawn.
         *
         * @return DOCUMENT ME!
         */
        public Color getBorderSelectionColor() {
            return borderSelectionColor;
        }

        /**
         * Sublcassed to only accept the font if it isn't a
         * FonUIResource
         *
         * @param font DOCUMENT ME!
         */
        public void setFont(Font font) {
            if (font instanceof FontUIResource) {
                font = null;
            }

            super.setFont(font);
        }

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging value with toString(). The
         * foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         *
         * @param tree DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param sel DOCUMENT ME!
         * @param expanded DOCUMENT ME!
         * @param leaf DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param hasFocus DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
            node = (DefaultMutableTreeNode) value;

            ImageIcon icon = null;
            String text = node.toString();

            if (node == getModel().getRoot()) {
                setText(text);

                return this;
            }

            obj = ((NodeInfo) node.getUserObject()).obj;

            String stringValue = tree.convertValueToText(value, sel, expanded,
                    leaf, row, hasFocus);

            this.hasFocus = hasFocus;

            setText(stringValue);

            if (sel) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }

            // There needs to be a way to specify disabled icons.
            if (!tree.isEnabled()) {
                setEnabled(false);

                if (leaf) {
                    setDisabledIcon(getLeafIcon());
                } else if (expanded) {
                    setDisabledIcon(getOpenIcon());
                } else {
                    setDisabledIcon(getClosedIcon());
                }
            } else {
                setEnabled(true);

                if (leaf) {
                    setIcon(getLeafIcon());
                } else if (expanded) {
                    setIcon(getOpenIcon());
                } else {
                    setIcon(getClosedIcon());
                }
            }

            selected = sel;

            return this;
        }

        /**
         * Paints the value.  The background is filled based on
         * selected.
         *
         * @param g DOCUMENT ME!
         */
        public void paint(Graphics g) {
            Color bColor;

            if (selected) {
                bColor = getBackgroundSelectionColor();
            } else {
                bColor = getBackgroundNonSelectionColor();

                if (bColor == null) {
                    bColor = getBackground();
                }
            }

            int imageOffset = -1;

            if (bColor != null) {
                Icon currentI = getIcon();

                imageOffset = getLabelStart();
                g.setColor(bColor);
                g.fillRect(imageOffset, 0, getWidth() - 1 - imageOffset,
                    getHeight());
            }

            if (hasFocus) {
                if (drawsFocusBorderAroundIcon) {
                    imageOffset = 0;
                } else if (imageOffset == -1) {
                    imageOffset = getLabelStart();
                }

                Color bsColor = getBorderSelectionColor();

                if (bsColor != null) {
                    g.setColor(bsColor);
                    g.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset,
                        getHeight() - 1);
                }
            }

            super.paint(g);
        }

        /**
         * Method Declaration.
         *
         * @return
         *
         * @see
         */
        private int getLabelStart() {
            Icon currentI = getIcon();

            if ((currentI != null) && (getText() != null)) {
                return currentI.getIconWidth() +
                Math.max(0, getIconTextGap() - 1);
            }

            return 0;
        }

        /**
         * Overrides <code>JComponent.getPreferredSize</code> to return
         * slightly wider preferred size value.
         *
         * @return DOCUMENT ME!
         */
        public Dimension getPreferredSize() {
            Dimension retDimension = super.getPreferredSize();

            if (retDimension != null) {
                retDimension = new Dimension(retDimension.width + 3,
                        retDimension.height);
            }

            return retDimension;
        }

        /**
         * Method Declaration.
         *
         * @return
         *
         * @see
         */
        Icon getConceptsTreeIcon() {
            Icon icon = null;

            URL url = null;

            if (((NodeInfo) node.getUserObject()).loaded) {
                url = getClass().getClassLoader().getResource("icons/trfolder.gif");
            } else {
                //??Need to find a different icon. ripoff.gif is broken
                url = getClass().getClassLoader().getResource("icons/trfolder.gif");
            }

            icon = new ImageIcon(url);

            return icon;
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
            maybeShowPopup(e);

            if (e.getClickCount() > 1) {
                showSelectedObjectDetails();
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

import com.sun.xml.registry.client.browser.graph.JBGraphPanel;

import java.awt.*;
import java.awt.event.*;

import java.net.URL;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.*;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;

import com.sun.xml.registry.client.browser.RegistryBrowser;


/**
 * A JTree that lists
 *
 * @author Jim Glennon
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConceptsTree extends JTree implements java.beans.PropertyChangeListener {

    /** DOCUMENT ME! */
    JPopupMenu    popup;

    /** DOCUMENT ME! */
    JMenuItem     editMenuItem       = null;

    /** DOCUMENT ME! */
    JMenuItem     insertMenuItem     = null;

    /** DOCUMENT ME! */
    JMenuItem     removeMenuItem     = null;

    /** DOCUMENT ME! */
    JMenuItem     saveMenuItem       = null;

    /** DOCUMENT ME! */
    JMenuItem     browseMenuItem     = null;

    /** DOCUMENT ME! */
    JMenuItem     auditTrailMenuItem = null;

    /** DOCUMENT ME! */
    JMenuItem     exportMenuItem   = null;

    /** DOCUMENT ME! */
    JMenuItem     retrieveMenuItem = null;

    /** DOCUMENT ME! */
    MouseListener popupListener;
    
    private boolean editable = RegistryBrowser.getInstance().isAuthenticated();
    
    private static ConceptsTreeModel s_conceptsTreeModel = null;

    /**
     * Class Constructor.
     *
     * @see
     */
    public ConceptsTree(boolean updateOnCreate) {
        if (s_conceptsTreeModel == null) {
            s_conceptsTreeModel = new ConceptsTreeModel(updateOnCreate);
        }
        this.setModel(s_conceptsTreeModel);
        
        setCellRenderer(new ConceptsTreeCellRenderer());
        setRootVisible(false);
        setShowsRootHandles(true);
        addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {

                    DefaultMutableTreeNode node =
                                 (DefaultMutableTreeNode)getLastSelectedPathComponent();

                    if (node == null) {

                        return;
                    }

                    Component c         =
                                 SwingUtilities.getRoot(ConceptsTree.this);
                    Cursor    oldCursor = c.getCursor();
                    RegistryBrowser.setWaitCursor();
                    ((ConceptsTreeModel)getModel()).expandTree(node, 1);
                    RegistryBrowser.setDefaultCursor();
                }
            });

        ((ConceptsTreeModel)getModel()).addTreeModelListener(new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent e) {
                }

                public void treeNodesInserted(TreeModelEvent e) {

                    //                expandPath(e.getTreePath());
                }

                public void treeNodesRemoved(TreeModelEvent e) {
                }

                public void treeNodesStructureChanged(TreeModelEvent e) {
                }

                public void treeStructureChanged(TreeModelEvent e) {
                }
            });


        createPopup();
            
        //add lister authenticated bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(RegistryBrowser.PROPERTY_AUTHENTICATED, this);
        
        setToolTipText("Classification Schemes");
    }
    
    /** Create popup menu for List */
    private void createPopup() {
        // Create popup menu for table
        popup     = new JPopupMenu();

        if (editable) {
            editMenuItem = new JMenuItem("Edit");
        }
        else {
            editMenuItem = new JMenuItem("Show Details");
        }
        popup.add(editMenuItem);
        insertMenuItem = new JMenuItem("Insert");
        popup.add(insertMenuItem);
        removeMenuItem = new JMenuItem("Remove");
        popup.add(removeMenuItem);
        saveMenuItem = new JMenuItem("Save");
        popup.add(saveMenuItem);
        browseMenuItem = new JMenuItem("Browse");
        popup.add(browseMenuItem);
        auditTrailMenuItem = new JMenuItem("Show Audit Trail");
        popup.add(auditTrailMenuItem);
        exportMenuItem = new JMenuItem("Export");
        popup.add(exportMenuItem);

        boolean authenticated = RegistryBrowser.getInstance().isAuthenticated();

        insertMenuItem.setVisible(authenticated);
        removeMenuItem.setVisible(authenticated);
        saveMenuItem.setVisible(authenticated);
            
        editMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    editAction();
                }
            });

        insertMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    insertAction();
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

        exportMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    exportAction();
                }
            });

        // Add listener to self so that I can bring up popup menus on right mouse click
        popupListener = new PopupListener();
        addMouseListener(popupListener);
        
    }    

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getSelectedConcepts() {

        ArrayList  objects = new ArrayList();

        TreePath[] treePaths = getSelectionPaths();

        if (treePaths != null) {

            NodeInfo nodeInfo = null;

            for (int i = 0; i < treePaths.length; ++i) {
                nodeInfo =
                    (NodeInfo)(((DefaultMutableTreeNode)treePaths[i]
                                .getLastPathComponent()).getUserObject());

                if (nodeInfo.obj instanceof Concept) {
                    objects.add((Concept)nodeInfo.obj);
                }
            }
        }

        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getSelectedClassificationSchemes() {

        ArrayList  objects = new ArrayList();

        TreePath[] treePaths = getSelectionPaths();

        if (treePaths != null) {

            NodeInfo nodeInfo = null;

            for (int i = 0; i < treePaths.length; ++i) {
                nodeInfo =
                    (NodeInfo)(((DefaultMutableTreeNode)treePaths[i]
                                .getLastPathComponent()).getUserObject());

                if (nodeInfo.obj instanceof ClassificationScheme) {
                    objects.add(nodeInfo.obj);
                }
            }
        }

        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getSelectedObjects() {

        ArrayList  objects = new ArrayList();

        TreePath[] treePaths = getSelectionPaths();

        if (treePaths != null) {

            NodeInfo nodeInfo = null;

            for (int i = 0; i < treePaths.length; ++i) {
                nodeInfo =
                    (NodeInfo)(((DefaultMutableTreeNode)treePaths[i]
                                .getLastPathComponent()).getUserObject());
                objects.add(nodeInfo.obj);
            }
        }

        return objects;
    }

    /**
     * DOCUMENT ME!
     */
    protected void editAction() {
        RegistryBrowser.setWaitCursor();

        Object[] selectedObjects = getSelectedObjects().toArray();

        if (selectedObjects.length == 1) {

            RegistryObject ro = (RegistryObject)selectedObjects[0];
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        } else {
            RegistryBrowser.displayError("Exactly one object must be selected in list for Edit action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void insertAction() {

        try {
            RegistryBrowser.setWaitCursor();

            ArrayList                selectedObjects =
                getSelectedObjects();
            JAXRClient               client =
                RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm    =
                client.getBusinessLifeCycleManager();

            int                      size = selectedObjects.size();

            if (size == 0) {

                //Add a new ClassificationScheme
                ClassificationScheme scheme =
                    lcm.createClassificationScheme("Scheme Name (Change me)",
                                                   "Scheme description (Change me)");

                JBDialog             dialog =
                    JBEditorDialog.showObjectDetails(this, scheme, true, editable);

                if (dialog.getStatus() != JBDialog.OK_STATUS) {

                    return;
                }

                //Now add to tree
                ((ConceptsTreeModel)getModel())
                .insertClassificationScheme(scheme);
            } else if (size == 1) {

                RegistryObject selectedObject =
                    (RegistryObject)selectedObjects.get(0);

                Concept        concept =
                    lcm.createConcept(selectedObject,
                                      "Concept Name (Change me)",
                                      "Value (Change me)");
                JBDialog       dialog  =
                    JBEditorDialog.showObjectDetails(this, concept, true, editable);

                if (dialog.getStatus() != JBDialog.OK_STATUS) {

                    return;
                }

                TreePath[]             treePaths  = getSelectionPaths();
                DefaultMutableTreeNode parentNode =
                    (DefaultMutableTreeNode)treePaths[0]
                                        .getLastPathComponent();

                ((ConceptsTreeModel)getModel()).insertConcept(concept,
                                                              parentNode);
            } else {
                RegistryBrowser.displayError("Exactly one object must be selected in list for Insert action.");
            }

            RegistryBrowser.setDefaultCursor();
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void removeAction() {
        RegistryBrowser.setWaitCursor();

        ArrayList selectedObjects = getSelectedObjects();

        int       size = selectedObjects.size();

        if (size >= 1) {

            try {

                ArrayList removeKeys = new ArrayList();

                for (int i = size - 1; i >= 0; i--) {

                    RegistryObject obj =
                        (RegistryObject)selectedObjects.get(i);
                    Key            key = obj.getKey();
                    removeKeys.add(key);
                }

                JAXRClient               client =
                    RegistryBrowser.getInstance().getClient();
                BusinessLifeCycleManager lcm  =
                    client.getBusinessLifeCycleManager();
                BulkResponse             resp =
                    lcm.deleteObjects(removeKeys);
                client.checkBulkResponse(resp);

                if (resp.getStatus() == JAXRResponse.STATUS_SUCCESS) {

                    TreePath[] currentSelection = getSelectionPaths();

                    if (currentSelection != null) {

                        for (int j = 0; j < currentSelection.length;
                             j++) {

                            DefaultMutableTreeNode currentNode =
                                (DefaultMutableTreeNode)(currentSelection[j]
                                 .getLastPathComponent());
                            MutableTreeNode        parent =
                                (MutableTreeNode)(currentNode.getParent());

                            if (parent != null) {
                                ((ConceptsTreeModel)getModel())
                                .removeNodeFromParent(currentNode);
                            }
                        }
                    }
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

        ArrayList selectedObjects = getSelectedObjects();

        int       size = selectedObjects.size();

        if (size >= 1) {

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

        ArrayList selectedObjects = getSelectedObjects();

        int       size = selectedObjects.size();

        if (size >= 1) {

            Component parent =
                SwingUtilities.getRoot(ConceptsTree.this);

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

        ArrayList selectedObjects = getSelectedObjects();

        int       size = selectedObjects.size();

        if (size == 1) {

            RegistryObject ro = (RegistryObject)selectedObjects.get(0);
            RegistryBrowser.showAuditTrail(ro);
        } else {
            RegistryBrowser.displayError("Exactly one object must be selected in list for Show Audit Trail action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    protected void exportAction() {
        RegistryBrowser.setWaitCursor();

        ArrayList selectedObjects = getSelectedObjects();

        int       size = selectedObjects.size();

        if (size >= 1) {

            JAXRClient client =
                RegistryBrowser.getInstance().getClient();
            client.exportObjects(selectedObjects);
        } else {
            RegistryBrowser.displayError("One or more objects must be selected in list for Export action.");
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    private void showSelectedObjectDetails() {

        ArrayList selectedObjects = getSelectedObjects();

        int       size = selectedObjects.size();

        if (size == 1) {

            RegistryObject ro = (RegistryObject)selectedObjects.get(0);
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.21 $
     */
    public class ConceptsTreeCellRenderer extends JLabel
      implements TreeCellRenderer {

        /** Is the value currently selected. */
        protected boolean selected;

        // These two ivars will be made protected later.

        /** True if has focus. */
        private boolean hasFocus;

        /** True if draws focus border around icon as well. */
        private boolean drawsFocusBorderAroundIcon;

        // Icons

        /** Icon used to show non-leaf nodes that aren't expanded. */
        protected transient Icon closedIcon;

        /** Icon used to show leaf nodes. */
        protected transient Icon leafIcon;

        /** Icon used to show non-leaf nodes that are expanded. */
        protected transient Icon openIcon;

        // Colors

        /** Color to use for the foreground for selected nodes. */
        protected Color textSelectionColor;

        /** Color to use for the foreground for non-selected nodes. */
        protected Color textNonSelectionColor;

        /** Color to use for the background when a node is selected. */
        protected Color backgroundSelectionColor;

        /**
         * Color to use for the background when the node isn't
         * selected.
         */
        protected Color backgroundNonSelectionColor;

        /**
         * Color to use for the background when the node isn't
         * selected.
         */
        protected Color        borderSelectionColor;

        /** DOCUMENT ME! */
        DefaultMutableTreeNode node = null;

        /** DOCUMENT ME! */
        Object obj = null;

        /**
         * Returns a new instance of DefaultTreeCellRenderer.
         * Alignment is set to left aligned. Icons and text color are
         * determined from the UIManager.
         */
        public ConceptsTreeCellRenderer() {
            setHorizontalAlignment(JLabel.LEFT);
            setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
            setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
            setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
            setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
            setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));

            Object value =
                UIManager.get("Tree.drawsFocusBorderAroundIcon");

            drawsFocusBorderAroundIcon = ((value != null)
                                         && ((Boolean)value)
                                            .booleanValue());
        }

        /**
         * Returns the default icon, for the current laf, that is used
         * to represent non-leaf nodes that are expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getDefaultOpenIcon() {

            return getConceptsTreeIcon();
        }

        /**
         * Returns the default icon, for the current laf, that is used
         * to represent non-leaf nodes that are not expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getDefaultClosedIcon() {

            return getConceptsTreeIcon();
        }

        /**
         * Returns the default icon, for the current laf, that is used
         * to represent leaf nodes.
         *
         * @return DOCUMENT ME!
         */
        public Icon getDefaultLeafIcon() {

            return getConceptsTreeIcon();
        }

        /**
         * Returns the icon used to represent non-leaf nodes that are
         * expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getOpenIcon() {

            return getConceptsTreeIcon();
        }

        /**
         * Returns the icon used to represent non-leaf nodes that are
         * not expanded.
         *
         * @return DOCUMENT ME!
         */
        public Icon getClosedIcon() {

            return getConceptsTreeIcon();
        }

        /**
         * Returns the icon used to represent leaf nodes.
         *
         * @return DOCUMENT ME!
         */
        public Icon getLeafIcon() {

            return getConceptsTreeIcon();
        }

        /**
         * Sets the color the text is drawn with when the node is
         * selected.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setTextSelectionColor(Color newColor) {
            textSelectionColor = newColor;
        }

        /**
         * Returns the color the text is drawn with when the node is
         * selected.
         *
         * @return DOCUMENT ME!
         */
        public Color getTextSelectionColor() {

            return textSelectionColor;
        }

        /**
         * Sets the color the text is drawn with when the node isn't
         * selected.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setTextNonSelectionColor(Color newColor) {
            textNonSelectionColor = newColor;
        }

        /**
         * Returns the color the text is drawn with when the node isn't
         * selected.
         *
         * @return DOCUMENT ME!
         */
        public Color getTextNonSelectionColor() {

            return textNonSelectionColor;
        }

        /**
         * Sets the color to use for the background if node is
         * selected.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setBackgroundSelectionColor(Color newColor) {
            backgroundSelectionColor = newColor;
        }

        /**
         * Returns the color to use for the background if node is
         * selected.
         *
         * @return DOCUMENT ME!
         */
        public Color getBackgroundSelectionColor() {

            return backgroundSelectionColor;
        }

        /**
         * Sets the background color to be used for non selected nodes.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setBackgroundNonSelectionColor(Color newColor) {
            backgroundNonSelectionColor = newColor;
        }

        /**
         * Returns the background color to be used for non selected
         * nodes.
         *
         * @return DOCUMENT ME!
         */
        public Color getBackgroundNonSelectionColor() {

            return backgroundNonSelectionColor;
        }

        /**
         * Sets the color to use for the border.
         *
         * @param newColor DOCUMENT ME!
         */
        public void setBorderSelectionColor(Color newColor) {
            borderSelectionColor = newColor;
        }

        /**
         * Returns the color the border is drawn.
         *
         * @return DOCUMENT ME!
         */
        public Color getBorderSelectionColor() {

            return borderSelectionColor;
        }

        /**
         * Sublcassed to only accept the font if it isn't a
         * FonUIResource
         *
         * @param font DOCUMENT ME!
         */
        public void setFont(Font font) {

            if (font instanceof FontUIResource) {
                font = null;
            }

            super.setFont(font);
        }

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging value with toString(). The
         * foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         *
         * @param tree DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param sel DOCUMENT ME!
         * @param expanded DOCUMENT ME!
         * @param leaf DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param hasFocus DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            node = (DefaultMutableTreeNode)value;

            ImageIcon icon = null;
            String    text = node.toString();

            if (node == getModel().getRoot()) {
                setText(text);

                return this;
            }

            obj = ((NodeInfo)node.getUserObject()).obj;

            String stringValue =
                tree.convertValueToText(value, sel, expanded, leaf,
                                        row, hasFocus);

            this.hasFocus = hasFocus;

            setText(stringValue);

            if (sel) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }

            // There needs to be a way to specify disabled icons.
            if (!tree.isEnabled()) {
                setEnabled(false);

                if (leaf) {
                    setDisabledIcon(getLeafIcon());
                } else if (expanded) {
                    setDisabledIcon(getOpenIcon());
                } else {
                    setDisabledIcon(getClosedIcon());
                }
            } else {
                setEnabled(true);

                if (leaf) {
                    setIcon(getLeafIcon());
                } else if (expanded) {
                    setIcon(getOpenIcon());
                } else {
                    setIcon(getClosedIcon());
                }
            }

            selected = sel;

            return this;
        }

        /**
         * Paints the value.  The background is filled based on
         * selected.
         *
         * @param g DOCUMENT ME!
         */
        public void paint(Graphics g) {

            Color bColor;

            if (selected) {
                bColor = getBackgroundSelectionColor();
            } else {
                bColor = getBackgroundNonSelectionColor();

                if (bColor == null) {
                    bColor = getBackground();
                }
            }

            int imageOffset = -1;

            if (bColor != null) {

                Icon currentI = getIcon();

                imageOffset = getLabelStart();
                g.setColor(bColor);
                g.fillRect(imageOffset, 0,
                           getWidth() - 1 - imageOffset, getHeight());
            }

            if (hasFocus) {

                if (drawsFocusBorderAroundIcon) {
                    imageOffset = 0;
                } else if (imageOffset == -1) {
                    imageOffset = getLabelStart();
                }

                Color bsColor = getBorderSelectionColor();

                if (bsColor != null) {
                    g.setColor(bsColor);
                    g.drawRect(imageOffset, 0,
                               getWidth() - 1 - imageOffset,
                               getHeight() - 1);
                }
            }

            super.paint(g);
        }

        /**
         * Method Declaration.
         *
         * @return
         *
         * @see
         */
        private int getLabelStart() {

            Icon currentI = getIcon();

            if ((currentI != null) && (getText() != null)) {

                return currentI.getIconWidth()
                       + Math.max(0, getIconTextGap() - 1);
            }

            return 0;
        }

        /**
         * Overrides <code>JComponent.getPreferredSize</code> to return
         * slightly wider preferred size value.
         *
         * @return DOCUMENT ME!
         */
        public Dimension getPreferredSize() {

            Dimension retDimension = super.getPreferredSize();

            if (retDimension != null) {
                retDimension =
                    new Dimension(retDimension.width + 3,
                                  retDimension.height);
            }

            return retDimension;
        }

        /**
         * Method Declaration.
         *
         * @return
         *
         * @see
         */
        Icon getConceptsTreeIcon() {

            Icon icon = null;

            URL  url = null;

            if (((NodeInfo)node.getUserObject()).loaded) {
                url = getClass().getClassLoader().getResource("icons/trfolder.gif");
            } else {

                //??Need to find a different icon. ripoff.gif is broken
                url = getClass().getClassLoader().getResource("icons/trfolder.gif");
            }

            icon = new ImageIcon(url);

            return icon;
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
            maybeShowPopup(e);

            if (e.getClickCount() > 1) {
                showSelectedObjectDetails();
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
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    public static void clearCache() {
        s_conceptsTreeModel = null;
    }

    /**
     * Listens to property changes in the bound property RegistryBrowser.PROPERTY_AUTHENTICATED.
     * Hides certain menuItems when user is unAuthenticated.
     */
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {            
          
            boolean authenticated = ((Boolean)ev.getNewValue()).booleanValue();
            setEditable(editable);

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
