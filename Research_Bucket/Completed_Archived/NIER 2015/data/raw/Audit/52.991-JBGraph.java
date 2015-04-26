/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/graph/JBGraph.java,v 1.6 2004/03/28 20:50:01 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.graph;

import com.jgraph.JGraph;

import com.jgraph.event.GraphSelectionEvent;
import com.jgraph.event.GraphSelectionListener;

import com.jgraph.graph.CellMapper;
import com.jgraph.graph.CellView;
import com.jgraph.graph.ConnectionSet;
import com.jgraph.graph.DefaultEdge;
import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.Edge;
import com.jgraph.graph.EdgeView;
import com.jgraph.graph.GraphCell;
import com.jgraph.graph.GraphConstants;
import com.jgraph.graph.GraphUndoManager;
import com.jgraph.graph.GraphView;
import com.jgraph.graph.ParentMap;
import com.jgraph.graph.Port;
import com.jgraph.graph.VertexView;

import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.swing.JAXRClient;
import org.freebxml.omar.client.ui.swing.JBDialog;
import org.freebxml.omar.client.ui.swing.JBEditorDialog;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;
import org.freebxml.omar.client.ui.swing.TreeCombo;

import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.UndoableEditEvent;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.User;


/**
 * Specialized JGraph for JAXR Browser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBGraph extends JGraph implements GraphSelectionListener,
    KeyListener {
    static int LINK_TYPE_UNSPECIFIED = 0;
    static int LINK_TYPE_ASSOCIATION = 1;
    static int LINK_TYPE_COMPOSITION = 2;
    static int LINK_TYPE_AGGREGATION = 3;

    // Undo Manager

    /** DOCUMENT ME! */
    protected GraphUndoManager undoManager;
    private JToolBar toolBar = null;
    private HashMap registryObjectToCellMap = new HashMap();
    private TreeCombo objectTypeCombo = null;

    // Actions which Change State
    protected Action undo;

    // Actions which Change State
    protected Action redo;

    // Actions which Change State
    protected Action remove;

    // Actions which Change State
    //protected Action group;
    // Actions which Change State
    //protected Action ungroup;
    // Actions which Change State
    protected Action tofront;

    // Actions which Change State
    protected Action toback;

    // Actions which Change State
    //protected Action cut;
    // Actions which Change State
    //protected Action copy;
    // Actions which Change State
    //protected Action paste;
    private boolean editable = true;

    /**
     * Creates a new JBGraph object.
     */
    public JBGraph() {
        this(new JBGraphModel());
    }

    /**
     * Creates a new JBGraph object.
     *
     * @param model DOCUMENT ME!
     */
    public JBGraph(JBGraphModel model) {
        super(model);

        // Use a Custom Marquee Handler
        setMarqueeHandler(new JBMarqueeHandler(this));

        // Tell the Graph to Select new Cells upon Insertion
        setSelectNewCells(true);

        // Make Ports Visible by Default
        setPortsVisible(true);

        // Use the Grid (but don't make it Visible)
        setGridEnabled(true);

        // Set the Grid Size to 10 Pixel
        setGridSize(6);

        // Set the Snap Size to 2 Pixel
        setSnapSize(1);

        // Construct Command History
        //
        // Create a GraphUndoManager which also Updates the ToolBar
        undoManager = new GraphUndoManager() {
                    // Override Superclass
                    public void undoableEditHappened(UndoableEditEvent e) {
                        // First Invoke Superclass
                        super.undoableEditHappened(e);

                        // Then Update Undo/Redo Buttons
                        updateHistoryButtons();
                    }
                };

        // Add Listeners to Graph
        //
        // Register UndoManager with the Model
        getModel().addUndoableEditListener(undoManager);

        // Update ToolBar based on Selection Changes
        getSelectionModel().addGraphSelectionListener(this);

        // Listen for Delete Keystroke when the Graph has Focus
        addKeyListener(this);

        toolBar = createToolBar();
    }

    // Override Superclass Method to Return Custom EdgeView
    protected EdgeView createEdgeView(Edge e, CellMapper cm) {
        // Return Custom EdgeView
        return new EdgeView(e, this, cm) {
                // Override Superclass Method
                public boolean isAddPointEvent(MouseEvent event) {
                    // Points are Added using Shift-Click
                    return event.isShiftDown();
                }

                // Override Superclass Method
                public boolean isRemovePointEvent(MouseEvent event) {
                    // Points are Removed using Shift-Click
                    return event.isShiftDown();
                }
            };
    }

    // Insert a new Vertex at point
    public void insert(Point point) {
        RegistryBrowser.setWaitCursor();

        try {
            if (RegistryBrowser.getInstance().getClient().getConnection() == null) {
                RegistryBrowser.displayError(
                    "Connect to a registry by specifying Registry Location first.");

                return;
            }

            RegistryObject ro = createRegistryObject();

            // Construct Vertex with no Label
            JBGraphCell vertex = new JBGraphCell(ro, true);

            registryObjectToCellMap.put(ro, vertex);

            // Add one Floating Port
            vertex.add(new DefaultPort());

            // Snap the Point to the Grid
            point = snap(new Point(point));

            // Default Size for the new Vertex
            Dimension size = new Dimension(25, 25);

            // Create a Map that holds the attributes for the Vertex
            Map map = GraphConstants.createMap();

            // Add a Bounds Attribute to the Map
            GraphConstants.setBounds(map, new Rectangle(point, size));

            // Add a Border Color Attribute to the Map
            GraphConstants.setBorderColor(map, Color.black);

            // Add a White Background
            GraphConstants.setBackground(map, Color.white);

            // Make Vertex Opaque
            GraphConstants.setOpaque(map, true);

            // Construct a Map from cells to Maps (for insert)
            Hashtable attributes = new Hashtable();

            // Associate the Vertex with its Attributes
            attributes.put(vertex, map);

            // Insert the Vertex and its Attributes
            getModel().insert(new Object[] { vertex }, null, null, attributes);
        } catch (JAXRException e) {
            e.printStackTrace();
            RegistryBrowser.displayError(e);
        }

        RegistryBrowser.setDefaultCursor();
    }

    // save changes to graph
    public void save() {
        RegistryBrowser.setWaitCursor();

        Object[] selectedCells = getSelectionCells();
        HashSet objectsToSave = new HashSet();

        for (int i = 0; i < selectedCells.length; i++) {
            if (selectedCells[i] instanceof JBGraphCell) {
                RegistryObject ro = ((JBGraphCell) selectedCells[i]).getRegistryObject();
                objectsToSave.add(ro);
            } else if (selectedCells[i] instanceof DefaultGraphCell) {
                //This is the case when save done of a Collection element
                //For now all elements will be processed as it is not obvious
                //how to figure out which element was selected
                DefaultGraphCell cell = (DefaultGraphCell) selectedCells[i];
                java.util.List children = cell.getChildren();

                if (children != null) {
                    Iterator iter = children.iterator();

                    while (iter.hasNext()) {
                        Object obj = iter.next();

                        if (obj instanceof JBGraphCell) {
                            RegistryObject ro = ((JBGraphCell) obj).getRegistryObject();
                            objectsToSave.add(ro);
                        }
                    }
                }
            }
        }

        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            client.saveObjects(objectsToSave);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        RegistryBrowser.setDefaultCursor();
    }

    // export changes to graph
    public void export() {
        RegistryBrowser.setWaitCursor();

        Object[] selectedCells = getSelectionCells();
        HashSet objectsToExport = new HashSet();

        for (int i = 0; i < selectedCells.length; i++) {
            if (selectedCells[i] instanceof JBGraphCell) {
                RegistryObject ro = ((JBGraphCell) selectedCells[i]).getRegistryObject();
                objectsToExport.add(ro);
            } else if (selectedCells[i] instanceof DefaultGraphCell) {
                //This is the case when save done of a Collection element
                //For now all elements will be processed as it is not obvious
                //how to figure out which element was selected
                DefaultGraphCell cell = (DefaultGraphCell) selectedCells[i];
                java.util.List children = cell.getChildren();
                Iterator iter = children.iterator();

                if (children != null) {
                    while (iter.hasNext()) {
                        Object obj = iter.next();

                        if (obj instanceof JBGraphCell) {
                            RegistryObject ro = ((JBGraphCell) obj).getRegistryObject();
                            objectsToExport.add(ro);
                        }
                    }
                }
            }
        }

        JAXRClient client = RegistryBrowser.getInstance().getClient();
        client.exportObjects(objectsToExport);
        RegistryBrowser.setDefaultCursor();
    }

    // Insert a new Edge between source and target
    public void connect(Port source, Port target) {
        try {
            if (RegistryBrowser.getInstance().getClient().getConnection() == null) {
                RegistryBrowser.displayError(
                    "Connect to a registry by specifying Registry Location first.");

                return;
            }

            Object srcObj = (((DefaultPort) source).getParent());
            Object targetObj = (((DefaultPort) target).getParent());

            if (!((srcObj instanceof JBGraphCell) &&
                    (srcObj instanceof JBGraphCell))) {
                return;
            }

            RegistryObject srcRO = ((JBGraphCell) srcObj).getRegistryObject();
            RegistryObject targetRO = ((JBGraphCell) targetObj).getRegistryObject();

            RelationshipPanel relPanel = new RelationshipPanel(srcRO, targetRO);
            JBDialog dialog = new JBDialog(RegistryBrowser.getInstance(), true,
                    relPanel);
            dialog.setTitle("Relationship");
            dialog.setEditable(editable);
            dialog.setVisible(true);

            if (dialog.getStatus() != JBDialog.OK_STATUS) {
                return;
            }

            String relationshipType = relPanel.getRelationshipType();

            boolean reverse = false;
            boolean directed = true;
            int linkType = LINK_TYPE_ASSOCIATION;

            String relationshipName = relPanel.getRelationshipName();
            boolean collection = true;

            if (relationshipType == RelationshipPanel.RELATIONSHIP_TYPE_ASSOCIATION) {
                directed = true;
                collection = false;
                linkType = LINK_TYPE_ASSOCIATION;
            } else if (relationshipType == RelationshipPanel.RELATIONSHIP_TYPE_REFERENCE) {
                directed = true;
                relPanel.setReferenceAttributeOnSourceObject();
            }

            /*
               GraphConstants.setLineBegin(aggregateStyle, GraphConstants.DIAMOND);
               GraphConstants.setBeginFill(aggregateStyle, true);
               GraphConstants.setBeginSize(aggregateStyle, 6);
               GraphConstants.setLineEnd(aggregateStyle, GraphConstants.SIMPLE);
               GraphConstants.setEndSize(aggregateStyle, 8);
               GraphConstants.setFontSize(aggregateStyle, 10);
               boolean reverse = false;
               String relationshipName = "";
               boolean collection=true;
             */

            // Connections that will be inserted into the Model
            ConnectionSet cs = new ConnectionSet();

            // Construct Edge with no label
            DefaultEdge edge = new DefaultEdge(relationshipName);

            // Create Connection between source and target using edge
            if (!reverse) {
                cs.connect(edge, source, target);
            } else {
                cs.connect(edge, target, source);
            }

            // Create a Map thath holds the attributes for the edge            
            Map map = GraphConstants.createMap();

            if (directed == true) {
                GraphConstants.setLineEnd(map, GraphConstants.SIMPLE);
            } else {
            }

            if (linkType == LINK_TYPE_COMPOSITION) {
                GraphConstants.setBeginFill(map, true);
                GraphConstants.setLineBegin(map, GraphConstants.DIAMOND);
            } else if (linkType == LINK_TYPE_AGGREGATION) {
                GraphConstants.setBeginFill(map, false);
                GraphConstants.setLineBegin(map, GraphConstants.DIAMOND);
            }

            // Construct a Map from cells to Maps (for insert)
            Hashtable attributes = new Hashtable();

            // Associate the Edge with its Attributes
            attributes.put(edge, map);

            // Insert the Edge and its Attributes
            getModel().insert(new Object[] { edge }, cs, null, attributes);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    // Create a Group that Contains the Cells
    public DefaultGraphCell group(Object[] cells) {
        DefaultGraphCell group = null;

        // Order Cells by View Layering
        cells = getView().order(cells);

        // If Any Cells in View
        if ((cells != null) && (cells.length > 0)) {
            // Create Group Cell
            group = new DefaultGraphCell();

            DefaultPort port = new DefaultPort("Center");
            group.add(port);

            // Create Change Information
            ParentMap map = new ParentMap();

            // Insert Child Parent Entries
            for (int i = 0; i < cells.length; i++) {
                map.addEntry(cells[i], group);
            }

            // Insert into model
            getModel().insert(new Object[] { group }, null, map, null);
        }

        return group;
    }

    // Ungroup the Groups in Cells and Select the Children
    public void ungroup(Object[] cells) {
        // If any Cells
        if ((cells != null) && (cells.length > 0)) {
            // List that Holds the Groups
            ArrayList groups = new ArrayList();

            // List that Holds the Children
            ArrayList children = new ArrayList();

            // Loop Cells
            for (int i = 0; i < cells.length; i++) {
                // If Cell is a Group
                if (isGroup(cells[i])) {
                    // Add to List of Groups
                    groups.add(cells[i]);

                    // Loop Children of Cell
                    for (int j = 0; j < getModel().getChildCount(cells[i]);
                            j++) {
                        // Get Child from Model
                        Object child = getModel().getChild(cells[i], j);

                        // If Not Port
                        if (!(child instanceof Port)) {
                            // Add to Children List
                            children.add(child);
                        }
                    }
                }
            }

            // Remove Groups from Model (Without Children)
            getModel().remove(groups.toArray());

            // Select Children
            setSelectionCells(children.toArray());
        }
    }

    // Determines if a Cell is a Group
    public boolean isGroup(Object cell) {
        // Map the Cell to its View
        CellView view = getView().getMapping(cell, false);

        if (view != null) {
            return !view.isLeaf();
        }

        return false;
    }

    // Brings the Specified Cells to Front
    public void toFront(Object[] c) {
        if ((c != null) && (c.length > 0)) {
            getView().toFront(getView().getMapping(c));
        }
    }

    // Sends the Specified Cells to Back
    public void toBack(Object[] c) {
        if ((c != null) && (c.length > 0)) {
            getView().toBack(getView().getMapping(c));
        }
    }

    // Undo the last Change to the Model or the View
    public void undo() {
        try {
            undoManager.undo(getView());
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            updateHistoryButtons();
        }
    }

    // Redo the last Change to the Model or the View
    public void redo() {
        try {
            undoManager.redo(getView());
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            updateHistoryButtons();
        }
    }

    // Update Undo/Redo Button State based on Undo Manager
    protected void updateHistoryButtons() {
        // The View Argument Defines the Context
        undo.setEnabled(undoManager.canUndo(getView()));
        redo.setEnabled(undoManager.canRedo(getView()));
    }

    //
    // Listeners
    //
    // From GraphSelectionListener Interface
    public void valueChanged(GraphSelectionEvent e) {
        // Group Button only Enabled if more than One Cell Selected
        //group.setEnabled(getSelectionCount() > 1);
        // Update Button States based on Current Selection
        boolean enabled = !isSelectionEmpty();
        remove.setEnabled(enabled);

        //ungroup.setEnabled(enabled);
        tofront.setEnabled(enabled);
        toback.setEnabled(enabled);

        //copy.setEnabled(enabled);
        //cut.setEnabled(enabled);

        /*
           //Show selected cell's details if RegistryObjectDialog isVisible
           RegistryObjectDialog dialog = RegistryBrowser.getInstance().getRegistryObjectDialog();

           if (dialog.isVisible()) {
               Object[] selectedCells = getSelectionCells();
               if ((selectedCells.length == 1) && (selectedCells[0] instanceof JBGraphCell)) {
                   dialog.setRegistryObject(((JBGraphCell)selectedCells[0]).getRegistryObject());
               }
               else {
                   dialog.setRegistryObject(null);
               }
           }
         */
    }

    //
    // KeyListener for Delete KeyStroke
    //
    public void keyReleased(KeyEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void keyPressed(KeyEvent e) {
        // Listen for Delete Key Press
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            // Execute Remove Action on Delete Key Press
            remove.actionPerformed(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     * @param bounds DOCUMENT ME!
     * @param createIcon DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JBGraphCell addRegistryObject(RegistryObject ro, Rectangle bounds,
        boolean createIcon) {
        JBGraphCell cell = null;

        //Only add if not already present
        if (!(registryObjectToCellMap.containsKey(ro))) {
            // Create Vertex
            cell = new JBGraphCell(ro, createIcon);

            Map viewMap = new Hashtable();
            Map map;

            map = GraphConstants.createMap();
            viewMap.put(cell, map);

            GraphConstants.setBounds(map, bounds);

            Object[] insert = new Object[] { cell };
            getModel().insert(insert, null, null, viewMap);

            registryObjectToCellMap.put(ro, cell);

            Component c = getCellRenderer(cell);

            if (c instanceof JComponent) {
                ((JComponent) c).setToolTipText(getToolTipText(ro));
            }
        }

        return cell;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String getToolTipText(RegistryObject ro) {
        String toolTipText = null;

        try {
            String name = RegistryBrowser.getName(ro);

            if ((name != null) && (name.length() != 0)) {
                toolTipText += ("Name: " + name);
            }

            String desc = RegistryBrowser.getDescription(ro);

            if (desc != null) {
                toolTipText += ("\nDescription: " + desc);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return toolTipText;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceCell DOCUMENT ME!
     * @param ro DOCUMENT ME!
     * @param bounds DOCUMENT ME!
     * @param relationshipName DOCUMENT ME!
     * @param reverseDirection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JBGraphCell addRelatedObject(JBGraphCell sourceCell,
        RegistryObject ro, Rectangle bounds, String relationshipName,
        boolean reverseDirection) {
        JBGraphCell targetCell = null;

        //Only add if not already present
        if (!(registryObjectToCellMap.containsKey(ro))) {
            targetCell = addRegistryObject(ro, bounds, true);

            if (!reverseDirection) {
                connectCells(sourceCell, targetCell, relationshipName, false);
            } else {
                connectCells(targetCell, sourceCell, relationshipName, false);
            }
        }

        return targetCell;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceCell DOCUMENT ME!
     * @param targetCell DOCUMENT ME!
     * @param relationshipName DOCUMENT ME!
     * @param reverseDirection DOCUMENT ME!
     */
    public void connectCells(JBGraphCell sourceCell,
        DefaultGraphCell targetCell, String relationshipName,
        boolean reverseDirection) {
        //Create the edge between sourceCell and targetCell
        Map aggregateStyle = GraphConstants.createMap();
        GraphConstants.setLineBegin(aggregateStyle, GraphConstants.DIAMOND);
        GraphConstants.setBeginFill(aggregateStyle, true);
        GraphConstants.setBeginSize(aggregateStyle, 6);
        GraphConstants.setLineEnd(aggregateStyle, GraphConstants.SIMPLE);
        GraphConstants.setEndSize(aggregateStyle, 8);

        //GraphConstants.setLabelPosition(aggregateStyle, new Point(500, 1200));
        GraphConstants.setFontSize(aggregateStyle, 10);

        ConnectionSet cs = new ConnectionSet();
        Map viewAttributes = new Hashtable();

        DefaultEdge edge = new DefaultEdge(relationshipName);

        if (!reverseDirection) {
            cs.connect(edge, sourceCell.getChildAt(0), targetCell.getChildAt(0));
        } else {
            cs.connect(edge, targetCell.getChildAt(0), sourceCell.getChildAt(0));
        }

        viewAttributes.put(edge, aggregateStyle);

        Object[] insert = new Object[] { edge };
        getModel().insert(insert, cs, null, viewAttributes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Component getCellRenderer(GraphCell cell) {
        GraphView graphView = getView();
        CellView cellView = graphView.getMapping(cell, true);
        Component renderer = cellView.getRendererComponent(this, false, false,
                false);

        return renderer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    void showRelatedObjects(JBGraphCell cell) {
        RegistryBrowser.setWaitCursor();

        RegistryObject ro = cell.getRegistryObject();

        ArrayList relatedCells = showRelatedObjects(cell, ro);

        ArrayList moreRelatedCells = null;

        if (ro instanceof javax.xml.registry.infomodel.Organization) {
            moreRelatedCells = showRelatedObjects(cell, (Organization) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.Service) {
            moreRelatedCells = showRelatedObjects(cell, (Service) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.ServiceBinding) {
            moreRelatedCells = showRelatedObjects(cell, (ServiceBinding) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.SpecificationLink) {
            moreRelatedCells = showRelatedObjects(cell, (SpecificationLink) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.User) {
            moreRelatedCells = showRelatedObjects(cell, (User) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.Classification) {
            moreRelatedCells = showRelatedObjects(cell, (Classification) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.ExternalIdentifier) {
            moreRelatedCells = showRelatedObjects(cell, (ExternalIdentifier) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.ClassificationScheme) {
            moreRelatedCells = showRelatedObjects(cell,
                    (ClassificationScheme) ro);
        } else if (ro instanceof javax.xml.registry.infomodel.Concept) {
            moreRelatedCells = showRelatedObjects(cell, (Concept) ro);
        }

        if (moreRelatedCells != null) {
            relatedCells.addAll(moreRelatedCells);
        }

        System.err.println("relatedCells.size() = " + relatedCells.size());
        circleLayout(this, cell, relatedCells);
        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    void browseObject(JBGraphCell cell) {
        RegistryObject ro = cell.getRegistryObject();

        JBGraphPanel.browseObject((Window) (SwingUtilities.getRoot(this)), ro,
            editable);
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    public void showRegistryObject(JBGraphCell cell) {
        RegistryBrowser.showRegistryObject(cell.getRegistryObject());
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    public void showRepositoryItem(JBGraphCell cell) {
        RegistryBrowser.showRepositoryItem(cell.getRegistryObject());
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    public void showAuditTrail(JBGraphCell cell) {
        RegistryBrowser.showAuditTrail(cell.getRegistryObject());
    }

    /**
     * DOCUMENT ME!
     *
     * @param objs DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private DefaultGraphCell createGroupFromObjectCollection(Collection objs) {
        DefaultGraphCell groupCell = null;

        if (objs != null) {
            ArrayList groupedCells = new ArrayList();

            boolean createIcon = true;
            int x = 50;
            Iterator iter = objs.iterator();

            while (iter.hasNext()) {
                RegistryObject ro = (RegistryObject) iter.next();

                //System.err.println("ro = " + ro.getName().getValue());
                //Use any existing cell for the ro or create new cell
                JBGraphCell newCell = (JBGraphCell) registryObjectToCellMap.get(ro);

                if (newCell == null) {
                    newCell = addRegistryObject(ro,
                            new Rectangle(0, x, 50, 50), createIcon);
                    createIcon = false;
                }

                CellView newCellView = getView().getMapping(newCell, false);

                Rectangle bounds = newCellView.getBounds();
                x += (1.25 * bounds.height);
                groupedCells.add(newCell);
            }

            if (groupedCells.size() > 0) {
                groupCell = group(groupedCells.toArray());
            }
        }

        return groupCell;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param ro DOCUMENT ME!
     *
     * @return ArrayList of GraphCell for the related objects.
     */
    private ArrayList showRelatedObjects(JBGraphCell cell, RegistryObject ro) {
        ArrayList relatedCells = new ArrayList();

        if (ro == null) {
            return relatedCells;
        }

        try {
            CellView cellView = getView().getMapping(cell, false);
            Rectangle bounds = cellView.getBounds();

            //Classifications
            Collection classifications = ro.getClassifications();
            DefaultGraphCell groupCell = createGroupFromObjectCollection(classifications);

            if (groupCell != null) {
                connectCells(cell, groupCell, "classifications", false);
                relatedCells.add(groupCell);
            }

            //ExternalIdentifiers
            Collection extIds = ro.getExternalIdentifiers();
            groupCell = createGroupFromObjectCollection(extIds);

            if (groupCell != null) {
                connectCells(cell, groupCell, "externalIdentifiers", false);
                relatedCells.add(groupCell);
            }

            //ExternalLinks
            Collection extLinks = ro.getExternalLinks();
            groupCell = createGroupFromObjectCollection(extLinks);

            if (groupCell != null) {
                connectCells(cell, groupCell, "externalLinks", false);
                relatedCells.add(groupCell);
            }

            /*
               //RegistryPackages
               try {
                   Collection pkgs = ro.getRegistryPackages();
                   Iterator iter = pkgs.iterator();
                   while (iter.hasNext()) {
                       RegistryPackage pkg = (RegistryPackage)iter.next();
                       if (pkg != null) {
                           JBGraphCell newCell = addRelatedObject(cell, pkg, new Rectangle(0, 0, 50, 50), "HasMember", true);
                           relatedCells.add(newCell);
                       }
                   }
               } catch (UnsupportedCapabilityException e) {
               }
             **/

            //Associations
            try {
                Collection assocs = ro.getAssociations();
                Iterator iter = assocs.iterator();

                while (iter.hasNext()) {
                    Association assoc = (Association) iter.next();
                    RegistryObject srcObj = assoc.getSourceObject();
                    RegistryObject targetObj = assoc.getTargetObject();
                    Concept concept = assoc.getAssociationType();

                    String label = "associatedWith";

                    if (concept != null) {
                        label = concept.getValue();
                    }

                    if ((srcObj != null) && (targetObj != null)) {
                        JBGraphCell newCell = null;

                        if (srcObj.getKey().getId().equalsIgnoreCase(ro.getKey()
                                                                           .getId())) {
                            //ro is the source, newCell is the target
                            newCell = addRelatedObject(cell, targetObj,
                                    new Rectangle(bounds.x + 100, bounds.y, 50,
                                        50), label, false);
                        } else {
                            //ro is the target, newCell is the source
                            newCell = addRelatedObject(cell, srcObj,
                                    new Rectangle(bounds.x + 100, bounds.y, 50,
                                        50), label, true);
                        }

                        relatedCells.add(newCell);
                    } else {
                        System.err.println(
                            "Invalid association. Source or traget is null: " +
                            assoc.getKey().getId());
                    }
                }
            } catch (UnsupportedCapabilityException e) {
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param org DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell, Organization org) {
        ArrayList relatedCells = new ArrayList();

        if (org == null) {
            return relatedCells;
        }

        try {
            //services
            Collection services = org.getServices();
            DefaultGraphCell groupCell = createGroupFromObjectCollection(services);

            if (groupCell != null) {
                connectCells(cell, groupCell, "services", false);
                relatedCells.add(groupCell);
            }

            //parent Organization
            try {
                Organization parentOrg = org.getParentOrganization();

                if (parentOrg != null) {
                    JBGraphCell newCell = addRelatedObject(cell, parentOrg,
                            new Rectangle(0, 0, 50, 50), "parent Oragnization",
                            false);
                    relatedCells.add(newCell);
                }
            } catch (UnsupportedCapabilityException e) {
            }

            //children Organizations
            try {
                Collection children = org.getChildOrganizations();
                groupCell = createGroupFromObjectCollection(children);

                if (groupCell != null) {
                    connectCells(cell, groupCell, "parent Organization", true);
                    relatedCells.add(groupCell);
                }
            } catch (UnsupportedCapabilityException e) {
            }

            //users
            Collection users = org.getUsers();
            groupCell = createGroupFromObjectCollection(users);

            if (groupCell != null) {
                connectCells(cell, groupCell, "users", false);
                relatedCells.add(groupCell);
            }

            //Primary contact

            /*
               User primContact = org.getPrimaryContact();
               JBGraphCell primContactCell = (JBGraphCell)registryObjectToCellMap.get(primContact);
               if (primContactCell != null) {
                       connectCells(cell, primContactCell, "primary contact", false);
                       relatedCells.add(primContactCell);
               }
             */
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param service DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell, Service service) {
        ArrayList relatedCells = new ArrayList();

        if (service == null) {
            return relatedCells;
        }

        try {
            //bindings
            Collection bindings = service.getServiceBindings();
            DefaultGraphCell groupCell = createGroupFromObjectCollection(bindings);

            if (groupCell != null) {
                connectCells(cell, groupCell, "service bindings", false);
                relatedCells.add(groupCell);
            }

            //parent Organization
            Organization parentOrg = service.getProvidingOrganization();

            if (parentOrg != null) {
                JBGraphCell newCell = addRelatedObject(cell, parentOrg,
                        new Rectangle(0, 0, 50, 50), "parent Oragnization",
                        false);
                relatedCells.add(newCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param binding DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
        ServiceBinding binding) {
        ArrayList relatedCells = new ArrayList();

        if (binding == null) {
            return relatedCells;
        }

        try {
            //specLinks
            Collection specLinks = binding.getSpecificationLinks();
            System.err.println("Binding has " + specLinks.size() +
                " specLinks");

            DefaultGraphCell groupCell = createGroupFromObjectCollection(specLinks);

            if (groupCell != null) {
                connectCells(cell, groupCell, "specification links", false);
                relatedCells.add(groupCell);
            }

            //parent Service
            //Service parentService = binding.getService();
            //JBGraphCell newCell = addRelatedObject(cell, parentService, new Rectangle(0, 0, 50, 50), "parent Service", false);
            //relatedCells.add(newCell);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param specLink DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
        SpecificationLink specLink) {
        ArrayList relatedCells = new ArrayList();

        if (specLink == null) {
            return relatedCells;
        }

        try {
            //specificationObject
            RegistryObject specObject = specLink.getSpecificationObject();

            if (specObject != null) {
                JBGraphCell newCell = addRelatedObject(cell, specObject,
                        new Rectangle(0, 0, 50, 50), "specification object",
                        false);
                relatedCells.add(newCell);

                //parent Service
                //Service parentBinding = specLink.getServiceBinding();
                //JBGraphCell newCell = addRelatedObject(cell, parentServiceBinding, new Rectangle(0, 0, 50, 50), "parent ServiceBinding", false);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell, User user) {
        ArrayList relatedCells = new ArrayList();

        if (user == null) {
            return relatedCells;
        }

        try {
            //parent Organization
            Organization org = user.getOrganization();

            if (org != null) {
                JBGraphCell newCell = addRelatedObject(cell, org,
                        new Rectangle(0, 0, 50, 50), "affiliated with", false);
                relatedCells.add(newCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param classification DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
        Classification classification) {
        ArrayList relatedCells = new ArrayList();

        if (classification == null) {
            return relatedCells;
        }

        try {
            //scheme
            ClassificationScheme scheme = classification.getClassificationScheme();
            JBGraphCell newCell = addRelatedObject(cell, scheme,
                    new Rectangle(0, 0, 50, 50), "classification scheme", false);
            relatedCells.add(newCell);

            //Concept
            Concept concept = classification.getConcept();

            if (concept != null) {
                newCell = addRelatedObject(cell, concept,
                        new Rectangle(0, 0, 50, 50), "value", false);
                relatedCells.add(newCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param externalIdentifier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
        ExternalIdentifier externalIdentifier) {
        ArrayList relatedCells = new ArrayList();

        if (externalIdentifier == null) {
            return relatedCells;
        }

        try {
            //scheme
            ClassificationScheme scheme = externalIdentifier.getIdentificationScheme();
            JBGraphCell newCell = addRelatedObject(cell, scheme,
                    new Rectangle(0, 0, 50, 50), "identification scheme", false);
            relatedCells.add(newCell);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param scheme DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
        ClassificationScheme scheme) {
        ArrayList relatedCells = new ArrayList();

        if (scheme == null) {
            return relatedCells;
        }

        try {
            //bindings
            Collection childConcepts = scheme.getChildrenConcepts();
            DefaultGraphCell groupCell = createGroupFromObjectCollection(childConcepts);

            if (groupCell != null) {
                connectCells(cell, groupCell, "child Concepts", false);
                relatedCells.add(groupCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param concept DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell, Concept concept) {
        ArrayList relatedCells = new ArrayList();

        if (concept == null) {
            return relatedCells;
        }

        try {
            //parent Organization
            Concept parentConcept = concept.getParentConcept();

            if (parentConcept != null) {
                JBGraphCell newCell = addRelatedObject(cell, parentConcept,
                        new Rectangle(0, 0, 50, 50), "parent", false);
                relatedCells.add(newCell);
            } else {
                ClassificationScheme scheme = concept.getClassificationScheme();

                if (scheme != null) {
                    JBGraphCell newCell = addRelatedObject(cell, scheme,
                            new Rectangle(0, 0, 50, 50),
                            "classification scheme", false);
                    relatedCells.add(newCell);
                }
            }

            //child Concepts
            Collection childConcepts = concept.getChildrenConcepts();
            DefaultGraphCell groupCell = createGroupFromObjectCollection(childConcepts);

            if (groupCell != null) {
                connectCells(cell, groupCell, "child Concepts", false);
                relatedCells.add(groupCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * Create a RegistryObject based on teh current setting of
     * objectTypeCombo
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    private RegistryObject createRegistryObject() throws JAXRException {
        RegistryObject ro = null;

        JAXRClient client = RegistryBrowser.getInstance().getClient();
        BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
        Concept objectTypeConcept = null;
        
        Object conceptsTreeNode = objectTypeCombo.getSelectedItemsObject();
        Object nodeInfo = ((javax.swing.tree.DefaultMutableTreeNode) conceptsTreeNode).getUserObject();

        if (nodeInfo instanceof org.freebxml.omar.client.ui.swing.NodeInfo) {
            Object obj = ((org.freebxml.omar.client.ui.swing.NodeInfo) nodeInfo).obj;

            if (obj instanceof javax.xml.registry.infomodel.Concept) {
                objectTypeConcept = (Concept)obj;
            } else {
                throw new JAXRException("Expected Concept but found a " + obj.getClass().getName());
            }            
        }
        
        
        org.freebxml.omar.client.xml.registry.util.QueryUtil qu = org.freebxml.omar.client.xml.registry.util.QueryUtil.getInstance();
        
        ro = (RegistryObject) (((LifeCycleManagerImpl)lcm).createObject(objectTypeConcept));

        return ro;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JToolBar createToolBar() {
        JButton button = null;
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        org.freebxml.omar.client.ui.swing.ConceptsTreeModel objectTypesTreeModel =
            org.freebxml.omar.client.ui.swing.BusinessQueryPanel.getObjectTypesTreeModel();
        objectTypeCombo = new org.freebxml.omar.client.ui.swing.TreeCombo(objectTypesTreeModel);
        toolbar.add(objectTypeCombo);

        // Insert
        URL insertUrl = getClass().getClassLoader().getResource("icons/insert.gif");
        ImageIcon insertIcon = new ImageIcon(insertUrl);
        button = toolbar.add(new AbstractAction("", insertIcon) {
                    public void actionPerformed(ActionEvent e) {
                        insert(new Point(10, 10));
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Insert");

        // Toggle Connect Mode
        URL connectUrl = getClass().getClassLoader().getResource("icons/connecton.gif");
        ImageIcon connectIcon = new ImageIcon(connectUrl);
        button = toolbar.add(new AbstractAction("", connectIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setPortsVisible(!isPortsVisible());

                        URL connectUrl;

                        if (isPortsVisible()) {
                            connectUrl = getClass().getClassLoader()
                                             .getResource("icons/connecton.gif");
                        } else {
                            connectUrl = getClass().getClassLoader()
                                             .getResource("icons/connectoff.gif");
                        }

                        ImageIcon connectIcon = new ImageIcon(connectUrl);
                        putValue(SMALL_ICON, connectIcon);
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Toggle Connect Mode");

        // Undo
        toolbar.addSeparator();

        URL undoUrl = getClass().getClassLoader().getResource("icons/undo.gif");
        ImageIcon undoIcon = new ImageIcon(undoUrl);
        undo = new AbstractAction("", undoIcon) {
                    public void actionPerformed(ActionEvent e) {
                        undo();
                    }
                };
        undo.setEnabled(false);
        button = toolbar.add(undo);
        button.setText(""); //an icon-only button
        button.setToolTipText("Undo");

        // Redo
        URL redoUrl = getClass().getClassLoader().getResource("icons/redo.gif");
        ImageIcon redoIcon = new ImageIcon(redoUrl);
        redo = new AbstractAction("", redoIcon) {
                    public void actionPerformed(ActionEvent e) {
                        redo();
                    }
                };
        redo.setEnabled(false);
        button = toolbar.add(redo);
        button.setText(""); //an icon-only button
        button.setToolTipText("Redo");

        //
        // Edit Block
        //
        toolbar.addSeparator();

        Action action;
        URL url;

        // Copy
        action = TransferHandler.getCopyAction();
        url = getClass().getClassLoader().getResource("icons/copy.gif");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));

        //Commented out until we can figure out how to assign new id to copied objects
        //button = toolbar.add(copy = new EventRedirector(action));
        button.setText(""); //an icon-only button
        button.setToolTipText("Copy");

        // Paste
        action = TransferHandler.getPasteAction();
        url = getClass().getClassLoader().getResource("icons/paste.gif");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));

        //Commented out until we can figure out how to assign new id to copied objects
        //button = toolbar.add(paste = new EventRedirector(action));
        button.setText(""); //an icon-only button
        button.setToolTipText("Paste");

        // Cut
        action = TransferHandler.getCutAction();
        url = getClass().getClassLoader().getResource("icons/cut.gif");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));

        //Commented out until we can figure out how to assign new id to copied objects
        //button = toolbar.add(cut = new EventRedirector(action));
        button.setText(""); //an icon-only button
        button.setToolTipText("Cut");

        // Remove
        URL removeUrl = getClass().getClassLoader().getResource("icons/delete.gif");
        ImageIcon removeIcon = new ImageIcon(removeUrl);
        remove = new AbstractAction("", removeIcon) {
                    public void actionPerformed(ActionEvent e) {
                        if (!isSelectionEmpty()) {
                            Object[] cells = getSelectionCells();
                            cells = getDescendants(cells);
                            getModel().remove(cells);

                            //Remove entry from map of cells on the graph
                            for (int i = 0; i < cells.length; i++) {
                                Object cell = cells[i];

                                if (cell instanceof JBGraphCell) {
                                    RegistryObject ro = ((JBGraphCell) cell).getRegistryObject();
                                    registryObjectToCellMap.remove(ro);
                                }
                            }
                        }
                    }
                };
        remove.setEnabled(false);
        button = toolbar.add(remove);
        button.setText(""); //an icon-only button
        button.setToolTipText("Remove");

        // Zoom Std
        toolbar.addSeparator();

        URL zoomUrl = getClass().getClassLoader().getResource("icons/zoom.gif");
        ImageIcon zoomIcon = new ImageIcon(zoomUrl);
        button = toolbar.add(new AbstractAction("", zoomIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setScale(1.0);
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Zoom");

        // Zoom In
        URL zoomInUrl = getClass().getClassLoader().getResource("icons/zoomin.gif");
        ImageIcon zoomInIcon = new ImageIcon(zoomInUrl);
        button = toolbar.add(new AbstractAction("", zoomInIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setScale(2 * getScale());
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Zoom In");

        // Zoom Out
        URL zoomOutUrl = getClass().getClassLoader().getResource("icons/zoomout.gif");
        ImageIcon zoomOutIcon = new ImageIcon(zoomOutUrl);
        button = toolbar.add(new AbstractAction("", zoomOutIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setScale(getScale() / 2);
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Zoom Out");

        // Group
        /*
        toolbar.addSeparator();

        URL groupUrl          =
            getClass().getClassLoader().getResource("icons/group.gif");
        ImageIcon groupIcon   = new ImageIcon(groupUrl);
        group =
            new AbstractAction("", groupIcon) {
                    public void actionPerformed(ActionEvent e) {
                        group(getSelectionCells());
                    }
                };
        group.setEnabled(false);
        //button                = toolbar.add(group);
        button.setText(""); //an icon-only button
        button.setToolTipText("Group");

        // Ungroup
        URL ungroupUrl        =
            getClass().getClassLoader().getResource("icons/ungroup.gif");
        ImageIcon ungroupIcon = new ImageIcon(ungroupUrl);
        ungroup =
            new AbstractAction("", ungroupIcon) {
                    public void actionPerformed(ActionEvent e) {
                        ungroup(getSelectionCells());
                    }
                };
        ungroup.setEnabled(false);
        //button                = toolbar.add(ungroup);
        button.setText(""); //an icon-only button
        button.setToolTipText("Ungroup");
         */
        // To Front
        toolbar.addSeparator();

        URL toFrontUrl = getClass().getClassLoader().getResource("icons/tofront.gif");
        ImageIcon toFrontIcon = new ImageIcon(toFrontUrl);
        tofront = new AbstractAction("", toFrontIcon) {
                    public void actionPerformed(ActionEvent e) {
                        if (!isSelectionEmpty()) {
                            toFront(getSelectionCells());
                        }
                    }
                };
        tofront.setEnabled(false);
        button = toolbar.add(tofront);
        button.setText(""); //an icon-only button
        button.setToolTipText("To Front");

        // To Back
        URL toBackUrl = getClass().getClassLoader().getResource("icons/toback.gif");
        ImageIcon toBackIcon = new ImageIcon(toBackUrl);
        toback = new AbstractAction("", toBackIcon) {
                    public void actionPerformed(ActionEvent e) {
                        if (!isSelectionEmpty()) {
                            toBack(getSelectionCells());
                        }
                    }
                };
        toback.setEnabled(false);
        button = toolbar.add(toback);
        button.setText(""); //an icon-only button
        button.setToolTipText("To Back");

        return toolbar;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JToolBar getToolBar() {
        return toolBar;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    void editCell(JBGraphCell cell) {
        RegistryObject ro = cell.getRegistryObject();

        if (ro != null) {
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param graph DOCUMENT ME!
     */
    public static void circleLayout(JGraph graph) {
        Object[] selectedCells = graph.getSelectionCells();

        if (selectedCells.length > 1) {
            GraphCell hubCell = (GraphCell) selectedCells[0];

            ArrayList spokeCells = new ArrayList();

            for (int i = 1; i < selectedCells.length; i++) {
                spokeCells.add(selectedCells[i]);
            }

            circleLayout(graph, hubCell, spokeCells);
        }
    }

    /**
     * Given a graph, a hub vertex in a graph and a list of spoke
     * vertices in teh graph this will modify the location of the
     * spokes so that they are laid out in a circle around the hub
     * started with first spoke at an angle of 135 degrees (left of
     * V).
     *
     * @param graph DOCUMENT ME!
     * @param hubCell DOCUMENT ME!
     * @param spokeCells DOCUMENT ME!
     */
    public static void circleLayout(JGraph graph, GraphCell hubCell,
        ArrayList spokeCells) {
        if (spokeCells.size() == 0) {
            return;
        }

        GraphView graphView = graph.getView();
        CellView hubCellView = graphView.getMapping(hubCell, true);

        // Maximum width or height
        int max = 0;
        Rectangle bounds = hubCellView.getBounds();

        // Update Maximum
        if (bounds != null) {
            max = Math.max(Math.max(bounds.width, bounds.height), max);
        }

        //Now get the spokeCellViews
        ArrayList spokeCellViews = new ArrayList();

        Iterator iter = spokeCells.iterator();

        while (iter.hasNext()) {
            GraphCell spokeCell = (GraphCell) iter.next();
            CellView spokeCellView = graphView.getMapping(spokeCell, true);

            if (spokeCellView != null) {
                spokeCellViews.add(spokeCellView);
            } else {
                System.err.println("Null spokeCellView for spokeCell: " +
                    spokeCell);
            }

            // Fetch Bounds
            //bounds = spokeCellView.getBounds();
            // Update Maximum
            //if (bounds != null)
            //	max = Math.max(Math.max(bounds.width, bounds.height), max);
        }

        Rectangle hubBounds = hubCellView.getBounds();

        // Compute Radius
        int r = (int) Math.max(((spokeCellViews.size()) * max) / Math.PI, 100);

        //System.err.println("Origin=" + hubBounds.getLocation() + " radius = " + r);
        //Set max radius to 250 pixels.
        if (r > 250) {
            r = 250;
        }

        // Compute step angle in radians
        double stepAngle = Math.toRadians(360.0 / ((double) (spokeCellViews.size())));

        //System.err.println("cellCount=" + spokeCellViews.size() + " stepAngle= " + stepAngle);
        //angle from hub to a spoke.
        double theta = Math.toRadians(90.0);

        if ((spokeCells.size() % 2) == 0) {
            theta = Math.toRadians(135.0);
        }

        // Arrange spokes in a circle around hub.
        iter = spokeCellViews.iterator();

        while (iter.hasNext()) {
            VertexView spokeCellView = (VertexView) iter.next();
            DefaultGraphCell spokeCell = (DefaultGraphCell) spokeCellView.getCell();
            Rectangle spokeBounds = spokeCellView.getBounds();

            //System.err.println("Cell=" + spokeCell.getUserObject() + " theta= " + theta);
            // Update Location
            if (spokeBounds != null) {
                int x = (hubBounds.x + (int) (r * Math.cos(theta))) -
                    (int) ((spokeBounds.width) / 2.0);
                int y = hubBounds.y - (int) (r * Math.sin(theta)) -
                    (int) ((spokeBounds.height) / 2.0);

                translate(spokeCellView, x - spokeBounds.x, y - spokeBounds.y);

                //spokeBounds.setLocation(x, y);
                //System.err.println("X=" + x + " Y=" + y);
            }

            theta -= stepAngle;
        }
    }

    //Workaround for bug in JGraph where a group does not move
    //by setLocation
    private static void translate(CellView view, int dx, int dy) {
        if (view.isLeaf()) {
            GraphConstants.translate(view.getAttributes(), dx, dy);
        } else {
            CellView[] childViews = view.getChildViews();

            for (int i = 0; i < childViews.length; i++) {
                translate(childViews[i], dx, dy);
            }
        }
    }

    public JBGraphCell getJBGraphCellAt(DefaultGraphCell parent, int x, int y) {
        JBGraphCell jbCell = null;

        java.util.List children = parent.getChildren();

        if (children != null) {
            Iterator iter = children.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();

                if (obj instanceof JBGraphCell) {
                    JBGraphCell _jbCell = (JBGraphCell) obj;
                    Rectangle bounds = getCellBounds(_jbCell);

                    if (bounds.contains(x, y)) {
                        jbCell = _jbCell;

                        break;
                    }
                }
            }
        }

        return jbCell;
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return editable;
    }

    // This will change the source of the actionevent to graph.
    protected class EventRedirector extends AbstractAction {
        /** DOCUMENT ME! */
        protected Action action;

        // Construct the "Wrapper" Action
        public EventRedirector(Action a) {
            super("", (ImageIcon) a.getValue(Action.SMALL_ICON));
            this.action = a;
        }

        // Redirect the Actionevent
        public void actionPerformed(ActionEvent e) {
            e = new ActionEvent(JBGraph.this, e.getID(), e.getActionCommand(),
                    e.getModifiers());
            action.actionPerformed(e);
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

package com.sun.xml.registry.client.browser.graph;

import com.jgraph.JGraph;

import com.jgraph.event.GraphSelectionEvent;
import com.jgraph.event.GraphSelectionListener;

import com.jgraph.graph.*;

import com.sun.xml.registry.client.browser.FindParamsPanel;
import com.sun.xml.registry.client.browser.JAXRClient;
import com.sun.xml.registry.client.browser.JBDialog;
import com.sun.xml.registry.client.browser.JBEditorDialog;
import com.sun.xml.registry.client.browser.RegistryBrowser;

import java.awt.*;
import java.awt.event.*;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.User;


/**
 * Specialized JGraph for JAXR Browser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBGraph extends JGraph implements GraphSelectionListener,
                                               KeyListener {

    static int LINK_TYPE_UNSPECIFIED = 0;
    static int LINK_TYPE_ASSOCIATION = 1;
    static int LINK_TYPE_COMPOSITION = 2;
    static int LINK_TYPE_AGGREGATION = 3;

    // Undo Manager

    /** DOCUMENT ME! */
    protected GraphUndoManager undoManager;
    private JToolBar toolBar = null;
    private HashMap registryObjectToCellMap = new HashMap();
    private com.sun.xml.registry.client.browser.TreeCombo objectTypeCombo=null;    

    // Actions which Change State
    protected Action undo;

    // Actions which Change State
    protected Action redo;

    // Actions which Change State
    protected Action remove;

    // Actions which Change State
    //protected Action group;

    // Actions which Change State
    //protected Action ungroup;

    // Actions which Change State
    protected Action tofront;

    // Actions which Change State
    protected Action toback;

    // Actions which Change State
    //protected Action cut;

    // Actions which Change State
    //protected Action copy;

    // Actions which Change State
    //protected Action paste;
    
    private boolean editable = true;

    /**
     * Creates a new JBGraph object.
     */
    public JBGraph() {
        this(new JBGraphModel());
    }

    /**
     * Creates a new JBGraph object.
     *
     * @param model DOCUMENT ME!
     */
    public JBGraph(JBGraphModel model) {
        super(model);

        // Use a Custom Marquee Handler
        setMarqueeHandler(new JBMarqueeHandler(this));

        // Tell the Graph to Select new Cells upon Insertion
        setSelectNewCells(true);

        // Make Ports Visible by Default
        setPortsVisible(true);

        // Use the Grid (but don't make it Visible)
        setGridEnabled(true);

        // Set the Grid Size to 10 Pixel
        setGridSize(6);

        // Set the Snap Size to 2 Pixel
        setSnapSize(1);

        // Construct Command History
        //
        // Create a GraphUndoManager which also Updates the ToolBar
        undoManager =
            new GraphUndoManager() {

                    // Override Superclass
                    public void undoableEditHappened(UndoableEditEvent e) {

                        // First Invoke Superclass
                        super.undoableEditHappened(e);

                        // Then Update Undo/Redo Buttons
                        updateHistoryButtons();
                    }
                };

        // Add Listeners to Graph
        //
        // Register UndoManager with the Model
        getModel().addUndoableEditListener(undoManager);

        // Update ToolBar based on Selection Changes
        getSelectionModel().addGraphSelectionListener(this);

        // Listen for Delete Keystroke when the Graph has Focus
        addKeyListener(this);

        toolBar = createToolBar();
    }

    // Override Superclass Method to Return Custom EdgeView
    protected EdgeView createEdgeView(Edge e, CellMapper cm) {

        // Return Custom EdgeView
        return new EdgeView(e, this, cm) {

                // Override Superclass Method
                public boolean isAddPointEvent(MouseEvent event) {

                    // Points are Added using Shift-Click
                    return event.isShiftDown();
                }

                // Override Superclass Method
                public boolean isRemovePointEvent(MouseEvent event) {

                    // Points are Removed using Shift-Click
                    return event.isShiftDown();
                }
            };
    }

    // Insert a new Vertex at point
    public void insert(Point point) {
        RegistryBrowser.setWaitCursor();

        try {

            if (RegistryBrowser.getInstance().getClient().getConnection() == null) {
                RegistryBrowser.displayError("Connect to a registry by specifying Registry Location first.");

                return;
            }

            RegistryObject ro = createRegistryObject();

            // Construct Vertex with no Label
            JBGraphCell vertex = new JBGraphCell(ro, true);

            registryObjectToCellMap.put(ro, vertex);

            // Add one Floating Port
            vertex.add(new DefaultPort());

            // Snap the Point to the Grid
            point = snap(new Point(point));

            // Default Size for the new Vertex
            Dimension size = new Dimension(25, 25);

            // Create a Map that holds the attributes for the Vertex
            Map map = GraphConstants.createMap();

            // Add a Bounds Attribute to the Map
            GraphConstants.setBounds(map, new Rectangle(point, size));

            // Add a Border Color Attribute to the Map
            GraphConstants.setBorderColor(map, Color.black);

            // Add a White Background
            GraphConstants.setBackground(map, Color.white);

            // Make Vertex Opaque
            GraphConstants.setOpaque(map, true);

            // Construct a Map from cells to Maps (for insert)
            Hashtable attributes = new Hashtable();

            // Associate the Vertex with its Attributes
            attributes.put(vertex, map);

            // Insert the Vertex and its Attributes
            getModel().insert(new Object[] { vertex }, null, null,
                              attributes);
        } catch (JAXRException e) {
            e.printStackTrace();
            RegistryBrowser.displayError(e);
        }

        RegistryBrowser.setDefaultCursor();
    }

    // save changes to graph
    public void save() {
        RegistryBrowser.setWaitCursor();

        Object[] selectedCells = getSelectionCells();
        HashSet objectsToSave  = new HashSet();

        for (int i = 0; i < selectedCells.length; i++) {

            if (selectedCells[i] instanceof JBGraphCell) {

                RegistryObject ro =
                    ((JBGraphCell)selectedCells[i]).getRegistryObject();
                objectsToSave.add(ro);
            }
            else if (selectedCells[i] instanceof DefaultGraphCell) {
                //This is the case when save done of a Collection element
                //For now all elements will be processed as it is not obvious
                //how to figure out which element was selected
                DefaultGraphCell cell = (DefaultGraphCell)selectedCells[i];
                java.util.List children = cell.getChildren();
                if (children != null)
                {
                    Iterator iter = children.iterator();
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        if (obj instanceof JBGraphCell) {
                        RegistryObject ro = ((JBGraphCell)obj).getRegistryObject();
                            objectsToSave.add(ro);
                        }                    
                    }
                }
            }
        }

        try {

            JAXRClient client =
                RegistryBrowser.getInstance().getClient();
            client.saveObjects(objectsToSave);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        RegistryBrowser.setDefaultCursor();
    }

    // export changes to graph
    public void export() {
        RegistryBrowser.setWaitCursor();

        Object[] selectedCells  = getSelectionCells();
        HashSet objectsToExport = new HashSet();

        for (int i = 0; i < selectedCells.length; i++) {

            if (selectedCells[i] instanceof JBGraphCell) {

                RegistryObject ro =
                    ((JBGraphCell)selectedCells[i]).getRegistryObject();
                objectsToExport.add(ro);
            }
            else if (selectedCells[i] instanceof DefaultGraphCell) {
                //This is the case when save done of a Collection element
                //For now all elements will be processed as it is not obvious
                //how to figure out which element was selected
                DefaultGraphCell cell = (DefaultGraphCell)selectedCells[i];
                java.util.List children = cell.getChildren();
                Iterator iter = children.iterator();
                if (children != null)
                {
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        if (obj instanceof JBGraphCell) {
                            RegistryObject ro = ((JBGraphCell)obj).getRegistryObject();
                            objectsToExport.add(ro);
                        }                    
                    }
                }
            }
        }

        JAXRClient client = RegistryBrowser.getInstance().getClient();
        client.exportObjects(objectsToExport);
        RegistryBrowser.setDefaultCursor();
    }

    // Insert a new Edge between source and target
    public void connect(Port source, Port target) {

        try {

            if (RegistryBrowser.getInstance().getClient().getConnection() == null) {
                RegistryBrowser.displayError("Connect to a registry by specifying Registry Location first.");

                return;
            }

            Object srcObj    = (((DefaultPort)source).getParent());
            Object targetObj = (((DefaultPort)target).getParent());

            if (!((srcObj instanceof JBGraphCell)
                && (srcObj instanceof JBGraphCell))) {

                return;
            }

            RegistryObject srcRO    =
                ((JBGraphCell)srcObj).getRegistryObject();
            RegistryObject targetRO =
                ((JBGraphCell)targetObj).getRegistryObject();

            RelationshipPanel relPanel =
                new RelationshipPanel(srcRO, targetRO);
            JBDialog dialog            =
                new JBDialog(RegistryBrowser.getInstance(), true,
                             relPanel);
            dialog.setTitle("Relationship");
            dialog.setEditable(editable);
            dialog.setVisible(true);

            if (dialog.getStatus() != JBDialog.OK_STATUS) {

                return;
            }

            String relationshipType = relPanel.getRelationshipType();

            boolean reverse  = false;
            boolean directed = true;
            int linkType     = LINK_TYPE_ASSOCIATION;

            String relationshipName = relPanel.getRelationshipName();
            boolean collection      = true;

            if (relationshipType == RelationshipPanel.RELATIONSHIP_TYPE_ASSOCIATION) {
                directed       = true;
                collection     = false;
                linkType       = LINK_TYPE_ASSOCIATION;
            } else if (relationshipType == RelationshipPanel.RELATIONSHIP_TYPE_REFERENCE) {
                directed = true;
                relPanel.setReferenceAttributeOnSourceObject();
            }

            /*
               GraphConstants.setLineBegin(aggregateStyle, GraphConstants.DIAMOND);
               GraphConstants.setBeginFill(aggregateStyle, true);
               GraphConstants.setBeginSize(aggregateStyle, 6);
               GraphConstants.setLineEnd(aggregateStyle, GraphConstants.SIMPLE);
               GraphConstants.setEndSize(aggregateStyle, 8);
               GraphConstants.setFontSize(aggregateStyle, 10);
               boolean reverse = false;
               String relationshipName = "";
               boolean collection=true;
             */

            // Connections that will be inserted into the Model
            ConnectionSet cs = new ConnectionSet();

            // Construct Edge with no label
            DefaultEdge edge = new DefaultEdge(relationshipName);

            // Create Connection between source and target using edge
            if (!reverse) {
                cs.connect(edge, source, target);
            } else {
                cs.connect(edge, target, source);
            }

            // Create a Map thath holds the attributes for the edge            
            Map map = GraphConstants.createMap();

            if (directed == true) {
                GraphConstants.setLineEnd(map, GraphConstants.SIMPLE);
            } else {
            }

            if (linkType == LINK_TYPE_COMPOSITION) {
                GraphConstants.setBeginFill(map, true);
                GraphConstants.setLineBegin(map, GraphConstants.DIAMOND);
            } else if (linkType == LINK_TYPE_AGGREGATION) {
                GraphConstants.setBeginFill(map, false);
                GraphConstants.setLineBegin(map, GraphConstants.DIAMOND);
            }

            // Construct a Map from cells to Maps (for insert)
            Hashtable attributes = new Hashtable();

            // Associate the Edge with its Attributes
            attributes.put(edge, map);

            // Insert the Edge and its Attributes
            getModel().insert(new Object[] { edge }, cs, null,
                              attributes);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    // Create a Group that Contains the Cells
    public DefaultGraphCell group(Object[] cells) {

        DefaultGraphCell group = null;

        // Order Cells by View Layering
        cells = getView().order(cells);

        // If Any Cells in View
        if ((cells != null) && (cells.length > 0)) {

            // Create Group Cell
            group = new DefaultGraphCell();

            DefaultPort port = new DefaultPort("Center");
            group.add(port);

            // Create Change Information
            ParentMap map = new ParentMap();

            // Insert Child Parent Entries
            for (int i = 0; i < cells.length; i++) {
                map.addEntry(cells[i], group);
            }

            // Insert into model
            getModel().insert(new Object[] { group }, null, map, null);
        }

        return group;
    }

    // Ungroup the Groups in Cells and Select the Children
    public void ungroup(Object[] cells) {

        // If any Cells
        if ((cells != null) && (cells.length > 0)) {

            // List that Holds the Groups
            ArrayList groups = new ArrayList();

            // List that Holds the Children
            ArrayList children = new ArrayList();

            // Loop Cells
            for (int i = 0; i < cells.length; i++) {

                // If Cell is a Group
                if (isGroup(cells[i])) {

                    // Add to List of Groups
                    groups.add(cells[i]);

                    // Loop Children of Cell
                    for (int j = 0;
                         j < getModel().getChildCount(cells[i]); j++) {

                        // Get Child from Model
                        Object child = getModel().getChild(cells[i], j);

                        // If Not Port
                        if (!(child instanceof Port)) {

                            // Add to Children List
                            children.add(child);
                        }
                    }
                }
            }

            // Remove Groups from Model (Without Children)
            getModel().remove(groups.toArray());

            // Select Children
            setSelectionCells(children.toArray());
        }
    }

    // Determines if a Cell is a Group
    public boolean isGroup(Object cell) {

        // Map the Cell to its View
        CellView view = getView().getMapping(cell, false);

        if (view != null) {

            return !view.isLeaf();
        }

        return false;
    }

    // Brings the Specified Cells to Front
    public void toFront(Object[] c) {

        if ((c != null) && (c.length > 0)) {
            getView().toFront(getView().getMapping(c));
        }
    }

    // Sends the Specified Cells to Back
    public void toBack(Object[] c) {

        if ((c != null) && (c.length > 0)) {
            getView().toBack(getView().getMapping(c));
        }
    }

    // Undo the last Change to the Model or the View
    public void undo() {

        try {
            undoManager.undo(getView());
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            updateHistoryButtons();
        }
    }

    // Redo the last Change to the Model or the View
    public void redo() {

        try {
            undoManager.redo(getView());
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            updateHistoryButtons();
        }
    }

    // Update Undo/Redo Button State based on Undo Manager
    protected void updateHistoryButtons() {

        // The View Argument Defines the Context
        undo.setEnabled(undoManager.canUndo(getView()));
        redo.setEnabled(undoManager.canRedo(getView()));
    }

    //
    // Listeners
    //
    // From GraphSelectionListener Interface
    public void valueChanged(GraphSelectionEvent e) {

        // Group Button only Enabled if more than One Cell Selected
        //group.setEnabled(getSelectionCount() > 1);

        // Update Button States based on Current Selection
        boolean enabled = !isSelectionEmpty();
        remove.setEnabled(enabled);
        //ungroup.setEnabled(enabled);
        tofront.setEnabled(enabled);
        toback.setEnabled(enabled);
        //copy.setEnabled(enabled);
        //cut.setEnabled(enabled);

        /*
           //Show selected cell's details if RegistryObjectDialog isVisible
           RegistryObjectDialog dialog = RegistryBrowser.getInstance().getRegistryObjectDialog();
        
           if (dialog.isVisible()) {
               Object[] selectedCells = getSelectionCells();
               if ((selectedCells.length == 1) && (selectedCells[0] instanceof JBGraphCell)) {
                   dialog.setRegistryObject(((JBGraphCell)selectedCells[0]).getRegistryObject());
               }
               else {
                   dialog.setRegistryObject(null);
               }
           }
         */
    }

    //
    // KeyListener for Delete KeyStroke
    //
    public void keyReleased(KeyEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void keyPressed(KeyEvent e) {

        // Listen for Delete Key Press
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {

            // Execute Remove Action on Delete Key Press
            remove.actionPerformed(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     * @param bounds DOCUMENT ME!
     * @param createIcon DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JBGraphCell addRegistryObject(RegistryObject ro,
                                         Rectangle bounds,
                                         boolean createIcon) {

        JBGraphCell cell = null;

        //Only add if not already present
        if (!(registryObjectToCellMap.containsKey(ro))) {

            // Create Vertex
            cell = new JBGraphCell(ro, createIcon);

            Map viewMap = new Hashtable();
            Map map;

            map = GraphConstants.createMap();
            viewMap.put(cell, map);

            GraphConstants.setBounds(map, bounds);

            Object[] insert = new Object[] { cell };
            getModel().insert(insert, null, null, viewMap);

            registryObjectToCellMap.put(ro, cell);

            Component c = getCellRenderer(cell);

            if (c instanceof JComponent) {
                ((JComponent)c).setToolTipText(getToolTipText(ro));
            }
        }

        return cell;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String getToolTipText(RegistryObject ro) {

        String toolTipText = null;

        try {

            String name = RegistryBrowser.getName(ro);

            if (name != null && name.length() != 0) {
                toolTipText += ("Name: " + name);
            }

            String desc = RegistryBrowser.getDescription(ro);

            if (desc != null) {
                toolTipText += ("\nDescription: " + desc);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return toolTipText;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceCell DOCUMENT ME!
     * @param ro DOCUMENT ME!
     * @param bounds DOCUMENT ME!
     * @param relationshipName DOCUMENT ME!
     * @param reverseDirection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JBGraphCell addRelatedObject(JBGraphCell sourceCell,
                                        RegistryObject ro,
                                        Rectangle bounds,
                                        String relationshipName,
                                        boolean reverseDirection) {

        JBGraphCell targetCell = null;

        //Only add if not already present
        if (!(registryObjectToCellMap.containsKey(ro))) {
            targetCell = addRegistryObject(ro, bounds, true);

            if (!reverseDirection) {
                connectCells(sourceCell, targetCell, relationshipName,
                             false);
            } else {
                connectCells(targetCell, sourceCell, relationshipName,
                             false);
            }
        }

        return targetCell;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceCell DOCUMENT ME!
     * @param targetCell DOCUMENT ME!
     * @param relationshipName DOCUMENT ME!
     * @param reverseDirection DOCUMENT ME!
     */
    public void connectCells(JBGraphCell sourceCell,
                             DefaultGraphCell targetCell,
                             String relationshipName,
                             boolean reverseDirection) {

        //Create the edge between sourceCell and targetCell
        Map aggregateStyle = GraphConstants.createMap();
        GraphConstants.setLineBegin(aggregateStyle,
                                    GraphConstants.DIAMOND);
        GraphConstants.setBeginFill(aggregateStyle, true);
        GraphConstants.setBeginSize(aggregateStyle, 6);
        GraphConstants.setLineEnd(aggregateStyle, GraphConstants.SIMPLE);
        GraphConstants.setEndSize(aggregateStyle, 8);

        //GraphConstants.setLabelPosition(aggregateStyle, new Point(500, 1200));
        GraphConstants.setFontSize(aggregateStyle, 10);

        ConnectionSet cs   = new ConnectionSet();
        Map viewAttributes = new Hashtable();

        DefaultEdge edge = new DefaultEdge(relationshipName);

        if (!reverseDirection) {
            cs.connect(edge, sourceCell.getChildAt(0),
                       targetCell.getChildAt(0));
        } else {
            cs.connect(edge, targetCell.getChildAt(0),
                       sourceCell.getChildAt(0));
        }

        viewAttributes.put(edge, aggregateStyle);

        Object[] insert = new Object[] { edge };
        getModel().insert(insert, cs, null, viewAttributes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Component getCellRenderer(GraphCell cell) {

        GraphView graphView = getView();
        CellView cellView   = graphView.getMapping(cell, true);
        Component renderer  =
            cellView.getRendererComponent(this, false, false, false);

        return renderer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    void showRelatedObjects(JBGraphCell cell) {
        RegistryBrowser.setWaitCursor();

        RegistryObject ro = cell.getRegistryObject();

        ArrayList relatedCells = showRelatedObjects(cell, ro);

        ArrayList moreRelatedCells = null;

        if (ro instanceof javax.xml.registry.infomodel.Organization) {
            moreRelatedCells =
                showRelatedObjects(cell, (Organization)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.Service) {
            moreRelatedCells = showRelatedObjects(cell, (Service)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.ServiceBinding) {
            moreRelatedCells =
                showRelatedObjects(cell, (ServiceBinding)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.SpecificationLink) {
            moreRelatedCells =
                showRelatedObjects(cell, (SpecificationLink)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.User) {
            moreRelatedCells = showRelatedObjects(cell, (User)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.Classification) {
            moreRelatedCells =
                showRelatedObjects(cell, (Classification)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.ExternalIdentifier) {
            moreRelatedCells =
                showRelatedObjects(cell, (ExternalIdentifier)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.ClassificationScheme) {
            moreRelatedCells =
                showRelatedObjects(cell, (ClassificationScheme)ro);
        } else if (ro instanceof javax.xml.registry.infomodel.Concept) {
            moreRelatedCells = showRelatedObjects(cell, (Concept)ro);
        }

        if (moreRelatedCells != null) {
            relatedCells.addAll(moreRelatedCells);
        }

        System.err.println("relatedCells.size() = "
                           + relatedCells.size());
        circleLayout(this, cell, relatedCells);
        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    void browseObject(JBGraphCell cell) {

        RegistryObject ro = cell.getRegistryObject();

        JBGraphPanel.browseObject((Window)(SwingUtilities.getRoot(this)),
                                ro, editable);
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    public void retrieveItem(JBGraphCell cell) {
        RegistryBrowser.retrieveItem(cell.getRegistryObject());
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    public void showAuditTrail(JBGraphCell cell) {
        RegistryBrowser.showAuditTrail(cell.getRegistryObject());
    }

    /**
     * DOCUMENT ME!
     *
     * @param objs DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private DefaultGraphCell createGroupFromObjectCollection(Collection objs) {

        DefaultGraphCell groupCell = null;

        if (objs != null) {

            ArrayList groupedCells = new ArrayList();

            boolean createIcon = true;
            int x              = 50;
            Iterator iter      = objs.iterator();

            while (iter.hasNext()) {

                RegistryObject ro = (RegistryObject)iter.next();

                //System.err.println("ro = " + ro.getName().getValue());
                //Use any existing cell for the ro or create new cell
                JBGraphCell newCell =
                    (JBGraphCell)registryObjectToCellMap.get(ro);

                if (newCell == null) {
                    newCell =
                        addRegistryObject(ro,
                                          new Rectangle(0, x, 50, 50),
                                          createIcon);
                    createIcon = false;
                }

                CellView newCellView =
                    getView().getMapping(newCell, false);

                Rectangle bounds = newCellView.getBounds();
                x += (1.25 * bounds.height);
                groupedCells.add(newCell);
            }

            if (groupedCells.size() > 0) {
                groupCell = group(groupedCells.toArray());
            }
        }

        return groupCell;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param ro DOCUMENT ME!
     *
     * @return ArrayList of GraphCell for the related objects.
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         RegistryObject ro) {

        ArrayList relatedCells = new ArrayList();
        if (ro == null) {
            return relatedCells;
        }
        
        try {

            CellView cellView = getView().getMapping(cell, false);
            Rectangle bounds  = cellView.getBounds();

            //Classifications
            Collection classifications = ro.getClassifications();
            DefaultGraphCell groupCell =
                createGroupFromObjectCollection(classifications);

            if (groupCell != null) {
                connectCells(cell, groupCell, "classifications", false);
                relatedCells.add(groupCell);
            }

            //ExternalIdentifiers
            Collection extIds = ro.getExternalIdentifiers();
            groupCell = createGroupFromObjectCollection(extIds);

            if (groupCell != null) {
                connectCells(cell, groupCell, "externalIdentifiers",
                             false);
                relatedCells.add(groupCell);
            }

            //ExternalLinks
            Collection extLinks = ro.getExternalLinks();
            groupCell = createGroupFromObjectCollection(extLinks);

            if (groupCell != null) {
                connectCells(cell, groupCell, "externalLinks", false);
                relatedCells.add(groupCell);
            }

            /*
               //RegistryPackages
               try {
                   Collection pkgs = ro.getRegistryPackages();
                   Iterator iter = pkgs.iterator();
                   while (iter.hasNext()) {
                       RegistryPackage pkg = (RegistryPackage)iter.next();
                       if (pkg != null) {
                           JBGraphCell newCell = addRelatedObject(cell, pkg, new Rectangle(0, 0, 50, 50), "HasMember", true);
                           relatedCells.add(newCell);
                       }
                   }
               } catch (UnsupportedCapabilityException e) {
               }
             **/

            //Associations
            try {

                Collection assocs = ro.getAssociations();
                Iterator iter     = assocs.iterator();

                while (iter.hasNext()) {

                    Association assoc        = (Association)iter.next();
                    RegistryObject srcObj    = assoc.getSourceObject();
                    RegistryObject targetObj = assoc.getTargetObject();
                    Concept concept          =
                        assoc.getAssociationType();

                    String label = "associatedWith";

                    if (concept != null) {
                        label = concept.getValue();
                    }

                    if ((srcObj != null) && (targetObj != null)) {

                        JBGraphCell newCell = null;

                        if (srcObj.getKey().getId().equalsIgnoreCase(ro.getKey()
                                                                     .getId())) {

                            //ro is the source, newCell is the target
                            newCell =
                                addRelatedObject(cell, targetObj,
                                                 new Rectangle(bounds.x
                                                               + 100,
                                                               bounds.y,
                                                               50, 50),
                                                 label, false);
                        } else {

                            //ro is the target, newCell is the source
                            newCell =
                                addRelatedObject(cell, srcObj,
                                                 new Rectangle(bounds.x
                                                               + 100,
                                                               bounds.y,
                                                               50, 50),
                                                 label, true);
                        }

                        relatedCells.add(newCell);
                    } else {
                        System.err.println("Invalid association. Source or traget is null: "
                                           + assoc.getKey().getId());
                    }
                }
            } catch (UnsupportedCapabilityException e) {
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param org DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         Organization org) {

        ArrayList relatedCells = new ArrayList();
        if (org == null) {
            return relatedCells;
        }

        try {

            //services
            Collection services        = org.getServices();
            DefaultGraphCell groupCell =
                createGroupFromObjectCollection(services);

            if (groupCell != null) {
                connectCells(cell, groupCell, "services", false);
                relatedCells.add(groupCell);
            }

            //parent Organization
            try {

                Organization parentOrg = org.getParentOrganization();

                if (parentOrg != null) {

                    JBGraphCell newCell =
                        addRelatedObject(cell, parentOrg,
                                         new Rectangle(0, 0, 50, 50),
                                         "parent Oragnization", false);
                    relatedCells.add(newCell);
                }
            } catch (UnsupportedCapabilityException e) {
            }

            //children Organizations
            try {

                Collection children = org.getChildOrganizations();
                groupCell = createGroupFromObjectCollection(children);

                if (groupCell != null) {
                    connectCells(cell, groupCell,
                                 "parent Organization", true);
                    relatedCells.add(groupCell);
                }
            } catch (UnsupportedCapabilityException e) {
            }

            //users
            Collection users = org.getUsers();
            groupCell = createGroupFromObjectCollection(users);

            if (groupCell != null) {
                connectCells(cell, groupCell, "users", false);
                relatedCells.add(groupCell);
            }

            //Primary contact

            /*
               User primContact = org.getPrimaryContact();
               JBGraphCell primContactCell = (JBGraphCell)registryObjectToCellMap.get(primContact);
               if (primContactCell != null) {
                       connectCells(cell, primContactCell, "primary contact", false);
                       relatedCells.add(primContactCell);
               }
             */
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param service DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         Service service) {

        ArrayList relatedCells = new ArrayList();
        if (service == null) {
            return relatedCells;
        }

        try {

            //bindings
            Collection bindings        = service.getServiceBindings();
            DefaultGraphCell groupCell =
                createGroupFromObjectCollection(bindings);

            if (groupCell != null) {
                connectCells(cell, groupCell, "service bindings", false);
                relatedCells.add(groupCell);
            }

            //parent Organization
            Organization parentOrg = service.getProvidingOrganization();

            if (parentOrg != null) {

                JBGraphCell newCell =
                    addRelatedObject(cell, parentOrg,
                                     new Rectangle(0, 0, 50, 50),
                                     "parent Oragnization", false);
                relatedCells.add(newCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param binding DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         ServiceBinding binding) {

        ArrayList relatedCells = new ArrayList();
        if (binding == null) {
            return relatedCells;
        }

        try {

            //specLinks
            Collection specLinks = binding.getSpecificationLinks();
            System.err.println("Binding has " + specLinks.size()
                               + " specLinks");

            DefaultGraphCell groupCell =
                createGroupFromObjectCollection(specLinks);

            if (groupCell != null) {
                connectCells(cell, groupCell, "specification links",
                             false);
                relatedCells.add(groupCell);
            }

            //parent Service
            //Service parentService = binding.getService();
            //JBGraphCell newCell = addRelatedObject(cell, parentService, new Rectangle(0, 0, 50, 50), "parent Service", false);
            //relatedCells.add(newCell);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param specLink DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         SpecificationLink specLink) {

        ArrayList relatedCells = new ArrayList();
        if (specLink == null) {
            return relatedCells;
        }

        try {

            //specificationObject
            RegistryObject specObject =
                specLink.getSpecificationObject();
            
            if (specObject != null) {
                JBGraphCell newCell       =
                    addRelatedObject(cell, specObject,
                                     new Rectangle(0, 0, 50, 50),
                                     "specification object", false);
                relatedCells.add(newCell);

                //parent Service
                //Service parentBinding = specLink.getServiceBinding();
                //JBGraphCell newCell = addRelatedObject(cell, parentServiceBinding, new Rectangle(0, 0, 50, 50), "parent ServiceBinding", false);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell, User user) {

        ArrayList relatedCells = new ArrayList();
        if (user == null) {
            return relatedCells;
        }

        try {

            //parent Organization
            Organization org    = user.getOrganization();
            if (org != null) {
                JBGraphCell newCell =
                    addRelatedObject(cell, org,
                                     new Rectangle(0, 0, 50, 50),
                                     "affiliated with", false);
                relatedCells.add(newCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param classification DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         Classification classification) {

        ArrayList relatedCells = new ArrayList();
        if (classification == null) {
            return relatedCells;
        }

        try {

            //scheme
            ClassificationScheme scheme =
                classification.getClassificationScheme();
            JBGraphCell newCell         =
                addRelatedObject(cell, scheme,
                                 new Rectangle(0, 0, 50, 50),
                                 "classification scheme", false);
            relatedCells.add(newCell);

            //Concept
            Concept concept = classification.getConcept();

            if (concept != null) {
                newCell =
                    addRelatedObject(cell, concept,
                                     new Rectangle(0, 0, 50, 50),
                                     "value", false);
                relatedCells.add(newCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param externalIdentifier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         ExternalIdentifier externalIdentifier) {

        ArrayList relatedCells = new ArrayList();
        if (externalIdentifier == null) {
            return relatedCells;
        }

        try {

            //scheme
            ClassificationScheme scheme =
                externalIdentifier.getIdentificationScheme();
            JBGraphCell newCell         =
                addRelatedObject(cell, scheme,
                                 new Rectangle(0, 0, 50, 50),
                                 "identification scheme", false);
            relatedCells.add(newCell);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param scheme DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         ClassificationScheme scheme) {

        ArrayList relatedCells = new ArrayList();
        if (scheme == null) {
            return relatedCells;
        }

        try {

            //bindings
            Collection childConcepts   = scheme.getChildrenConcepts();
            DefaultGraphCell groupCell =
                createGroupFromObjectCollection(childConcepts);

            if (groupCell != null) {
                connectCells(cell, groupCell, "child Concepts", false);
                relatedCells.add(groupCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     * @param concept DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private ArrayList showRelatedObjects(JBGraphCell cell,
                                         Concept concept) {

        ArrayList relatedCells = new ArrayList();
        if (concept == null) {
            return relatedCells;
        }

        try {

            //parent Organization
            Concept parentConcept = concept.getParentConcept();

            if (parentConcept != null) {

                JBGraphCell newCell =
                    addRelatedObject(cell, parentConcept,
                                     new Rectangle(0, 0, 50, 50),
                                     "parent", false);
                relatedCells.add(newCell);
            } else {

                ClassificationScheme scheme =
                    concept.getClassificationScheme();

                if (scheme != null) {

                    JBGraphCell newCell =
                        addRelatedObject(cell, scheme,
                                         new Rectangle(0, 0, 50, 50),
                                         "classification scheme", false);
                    relatedCells.add(newCell);
                }
            }

            //child Concepts
            Collection childConcepts   = concept.getChildrenConcepts();
            DefaultGraphCell groupCell =
                createGroupFromObjectCollection(childConcepts);

            if (groupCell != null) {
                connectCells(cell, groupCell, "child Concepts", false);
                relatedCells.add(groupCell);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relatedCells;
    }

    /**
     * Create a RegistryObject based on teh current setting of
     * objectTypeCombo
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    private RegistryObject createRegistryObject() throws JAXRException {

        RegistryObject ro = null;

        JAXRClient client            =
            RegistryBrowser.getInstance().getClient();
        BusinessLifeCycleManager lcm =
            client.getBusinessLifeCycleManager();

        String objectType = (objectTypeCombo.getSelectedItem()).toString();

        com.sun.xml.registry.ebxml.util.QueryUtil qu = com.sun.xml.registry.ebxml.util.QueryUtil.getInstance();
        if (qu.getUnsupportedObjectTypes().contains(objectType)) {
            throw new JAXRException("Search not supported for: " + objectType);
        }
        
        boolean isIntrinsic = com.sun.xml.registry.client.browser.BusinessQueryPanel.isIntrinsicObjectType(objectType);
        String newObjectType = objectType;
        
        if (!isIntrinsic) {
            newObjectType = "ExtrinsicObject";
        }
        
        ro = (RegistryObject)(lcm.createObject(newObjectType));
        
        if (ro instanceof com.sun.xml.registry.ebxml.infomodel.ExtrinsicObjectImpl) {
            com.sun.xml.registry.ebxml.infomodel.ExtrinsicObjectImpl eo = 
                (com.sun.xml.registry.ebxml.infomodel.ExtrinsicObjectImpl)ro;
            
            //Need to convert to urn:uuid later
            eo.setObjectType(objectType);
        }
        

        return ro;
    }
        

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JToolBar createToolBar() {

        JButton button   = null;
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        com.sun.xml.registry.client.browser.ConceptsTreeModel objectTypesTreeModel = com.sun.xml.registry.client.browser.BusinessQueryPanel.getObjectTypesTreeModel();
        objectTypeCombo = new com.sun.xml.registry.client.browser.TreeCombo(objectTypesTreeModel);
        toolbar.add(objectTypeCombo);

        // Insert
        URL insertUrl        =
            getClass().getClassLoader().getResource("icons/insert.gif");
        ImageIcon insertIcon = new ImageIcon(insertUrl);
        button =
            toolbar.add(new AbstractAction("", insertIcon) {
                    public void actionPerformed(ActionEvent e) {
                        insert(new Point(10, 10));
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Insert");

        // Toggle Connect Mode
        URL connectUrl        =
            getClass().getClassLoader().getResource("icons/connecton.gif");
        ImageIcon connectIcon = new ImageIcon(connectUrl);
        button =
            toolbar.add(new AbstractAction("", connectIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setPortsVisible(!isPortsVisible());

                        URL connectUrl;

                        if (isPortsVisible()) {
                            connectUrl =
                                                        getClass()
                                                        .getClassLoader()
                                                        .getResource("icons/connecton.gif");
                        } else {
                            connectUrl =
                                                        getClass()
                                                        .getClassLoader()
                                                        .getResource("icons/connectoff.gif");
                        }

                        ImageIcon connectIcon =
                                                    new ImageIcon(connectUrl);
                        putValue(SMALL_ICON, connectIcon);
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Toggle Connect Mode");

        // Undo
        toolbar.addSeparator();

        URL undoUrl        =
            getClass().getClassLoader().getResource("icons/undo.gif");
        ImageIcon undoIcon = new ImageIcon(undoUrl);
        undo =
            new AbstractAction("", undoIcon) {
                    public void actionPerformed(ActionEvent e) {
                        undo();
                    }
                };
        undo.setEnabled(false);
        button             = toolbar.add(undo);
        button.setText(""); //an icon-only button
        button.setToolTipText("Undo");

        // Redo
        URL redoUrl        =
            getClass().getClassLoader().getResource("icons/redo.gif");
        ImageIcon redoIcon = new ImageIcon(redoUrl);
        redo =
            new AbstractAction("", redoIcon) {
                    public void actionPerformed(ActionEvent e) {
                        redo();
                    }
                };
        redo.setEnabled(false);
        button             = toolbar.add(redo);
        button.setText(""); //an icon-only button
        button.setToolTipText("Redo");

        //
        // Edit Block
        //
        toolbar.addSeparator();

        Action action;
        URL url;

        // Copy
        action     = TransferHandler.getCopyAction();
        url        = getClass().getClassLoader().getResource("icons/copy.gif");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));
        //Commented out until we can figure out how to assign new id to copied objects
        //button = toolbar.add(copy = new EventRedirector(action));
        button.setText(""); //an icon-only button
        button.setToolTipText("Copy");

        // Paste
        action     = TransferHandler.getPasteAction();
        url        = getClass().getClassLoader().getResource("icons/paste.gif");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));
        //Commented out until we can figure out how to assign new id to copied objects
        //button = toolbar.add(paste = new EventRedirector(action));
        button.setText(""); //an icon-only button
        button.setToolTipText("Paste");

        // Cut
        action     = TransferHandler.getCutAction();
        url        = getClass().getClassLoader().getResource("icons/cut.gif");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));
        //Commented out until we can figure out how to assign new id to copied objects
        //button = toolbar.add(cut = new EventRedirector(action));
        button.setText(""); //an icon-only button
        button.setToolTipText("Cut");

        // Remove
        URL removeUrl        =
            getClass().getClassLoader().getResource("icons/delete.gif");
        ImageIcon removeIcon = new ImageIcon(removeUrl);
        remove =
            new AbstractAction("", removeIcon) {
                    public void actionPerformed(ActionEvent e) {

                        if (!isSelectionEmpty()) {

                            Object[] cells = getSelectionCells();
                            cells = getDescendants(cells);
                            getModel().remove(cells);

                            //Remove entry from map of cells on the graph
                            for (int i = 0; i < cells.length; i++) {

                                Object cell = cells[i];

                                if (cell instanceof JBGraphCell) {

                                    RegistryObject ro =
                                        ((JBGraphCell)cell)
                                                                                .getRegistryObject();
                                    registryObjectToCellMap.remove(ro);
                                }
                            }
                        }
                    }
                };
        remove.setEnabled(false);
        button = toolbar.add(remove);
        button.setText(""); //an icon-only button
        button.setToolTipText("Remove");

        // Zoom Std
        toolbar.addSeparator();

        URL zoomUrl        =
            getClass().getClassLoader().getResource("icons/zoom.gif");
        ImageIcon zoomIcon = new ImageIcon(zoomUrl);
        button =
            toolbar.add(new AbstractAction("", zoomIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setScale(1.0);
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Zoom");

        // Zoom In
        URL zoomInUrl        =
            getClass().getClassLoader().getResource("icons/zoomin.gif");
        ImageIcon zoomInIcon = new ImageIcon(zoomInUrl);
        button =
            toolbar.add(new AbstractAction("", zoomInIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setScale(2 * getScale());
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Zoom In");

        // Zoom Out
        URL zoomOutUrl        =
            getClass().getClassLoader().getResource("icons/zoomout.gif");
        ImageIcon zoomOutIcon = new ImageIcon(zoomOutUrl);
        button =
            toolbar.add(new AbstractAction("", zoomOutIcon) {
                    public void actionPerformed(ActionEvent e) {
                        setScale(getScale() / 2);
                    }
                });
        button.setText(""); //an icon-only button
        button.setToolTipText("Zoom Out");

        // Group
        /*
        toolbar.addSeparator();

        URL groupUrl          =
            getClass().getClassLoader().getResource("icons/group.gif");
        ImageIcon groupIcon   = new ImageIcon(groupUrl);
        group =
            new AbstractAction("", groupIcon) {
                    public void actionPerformed(ActionEvent e) {
                        group(getSelectionCells());
                    }
                };
        group.setEnabled(false);
        //button                = toolbar.add(group);
        button.setText(""); //an icon-only button
        button.setToolTipText("Group");

        // Ungroup
        URL ungroupUrl        =
            getClass().getClassLoader().getResource("icons/ungroup.gif");
        ImageIcon ungroupIcon = new ImageIcon(ungroupUrl);
        ungroup =
            new AbstractAction("", ungroupIcon) {
                    public void actionPerformed(ActionEvent e) {
                        ungroup(getSelectionCells());
                    }
                };
        ungroup.setEnabled(false);
        //button                = toolbar.add(ungroup);
        button.setText(""); //an icon-only button
        button.setToolTipText("Ungroup");
         */

        // To Front
        toolbar.addSeparator();

        URL toFrontUrl        =
            getClass().getClassLoader().getResource("icons/tofront.gif");
        ImageIcon toFrontIcon = new ImageIcon(toFrontUrl);
        tofront =
            new AbstractAction("", toFrontIcon) {
                    public void actionPerformed(ActionEvent e) {

                        if (!isSelectionEmpty()) {
                            toFront(getSelectionCells());
                        }
                    }
                };
        tofront.setEnabled(false);
        button                = toolbar.add(tofront);
        button.setText(""); //an icon-only button
        button.setToolTipText("To Front");

        // To Back
        URL toBackUrl        =
            getClass().getClassLoader().getResource("icons/toback.gif");
        ImageIcon toBackIcon = new ImageIcon(toBackUrl);
        toback =
            new AbstractAction("", toBackIcon) {
                    public void actionPerformed(ActionEvent e) {

                        if (!isSelectionEmpty()) {
                            toBack(getSelectionCells());
                        }
                    }
                };
        toback.setEnabled(false);
        button               = toolbar.add(toback);
        button.setText(""); //an icon-only button
        button.setToolTipText("To Back");

        return toolbar;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JToolBar getToolBar() {

        return toolBar;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cell DOCUMENT ME!
     */
    void editCell(JBGraphCell cell) {

        RegistryObject ro = cell.getRegistryObject();

        if (ro != null) {
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param graph DOCUMENT ME!
     */
    public static void circleLayout(JGraph graph) {

        Object[] selectedCells = graph.getSelectionCells();

        if (selectedCells.length > 1) {

            GraphCell hubCell = (GraphCell)selectedCells[0];

            ArrayList spokeCells = new ArrayList();

            for (int i = 1; i < selectedCells.length; i++) {
                spokeCells.add(selectedCells[i]);
            }

            circleLayout(graph, hubCell, spokeCells);
        }
    }

    /**
     * Given a graph, a hub vertex in a graph and a list of spoke
     * vertices in teh graph this will modify the location of the
     * spokes so that they are laid out in a circle around the hub
     * started with first spoke at an angle of 135 degrees (left of
     * V).
     *
     * @param graph DOCUMENT ME!
     * @param hubCell DOCUMENT ME!
     * @param spokeCells DOCUMENT ME!
     */
    public static void circleLayout(JGraph graph, GraphCell hubCell,
                                    ArrayList spokeCells) {

        if (spokeCells.size() == 0) {

            return;
        }

        GraphView graphView  = graph.getView();
        CellView hubCellView = graphView.getMapping(hubCell, true);

        // Maximum width or height
        int max          = 0;
        Rectangle bounds = hubCellView.getBounds();

        // Update Maximum
        if (bounds != null) {
            max = Math.max(Math.max(bounds.width, bounds.height), max);
        }

        //Now get the spokeCellViews
        ArrayList spokeCellViews = new ArrayList();

        Iterator iter = spokeCells.iterator();

        while (iter.hasNext()) {

            GraphCell spokeCell    = (GraphCell)iter.next();
            CellView spokeCellView =
                graphView.getMapping(spokeCell, true);

            if (spokeCellView != null) {
                spokeCellViews.add(spokeCellView);
            } else {
                System.err.println("Null spokeCellView for spokeCell: "
                                   + spokeCell);
            }

            // Fetch Bounds
            //bounds = spokeCellView.getBounds();
            // Update Maximum
            //if (bounds != null)
            //	max = Math.max(Math.max(bounds.width, bounds.height), max);
        }

        Rectangle hubBounds = hubCellView.getBounds();

        // Compute Radius
        int r =
            (int)Math.max(((spokeCellViews.size()) * max) / Math.PI, 100);

        //System.err.println("Origin=" + hubBounds.getLocation() + " radius = " + r);
        //Set max radius to 250 pixels.
        if (r > 250) {
            r = 250;
        }

        // Compute step angle in radians
        double stepAngle =
            Math.toRadians(360.0 / ((double)(spokeCellViews.size())));

        //System.err.println("cellCount=" + spokeCellViews.size() + " stepAngle= " + stepAngle);
        //angle from hub to a spoke.
        double theta = Math.toRadians(90.0);

        if ((spokeCells.size() % 2) == 0) {
            theta = Math.toRadians(135.0);
        }

        // Arrange spokes in a circle around hub.
        iter = spokeCellViews.iterator();

        while (iter.hasNext()) {

            VertexView spokeCellView   = (VertexView)iter.next();
            DefaultGraphCell spokeCell =
                (DefaultGraphCell)spokeCellView.getCell();
            Rectangle spokeBounds      = spokeCellView.getBounds();

            //System.err.println("Cell=" + spokeCell.getUserObject() + " theta= " + theta);
            // Update Location
            if (spokeBounds != null) {

                int x =
                    (hubBounds.x + (int)(r * Math.cos(theta)))
                    - (int)((spokeBounds.width) / 2.0);
                int y =
                    hubBounds.y - (int)(r * Math.sin(theta))
                    - (int)((spokeBounds.height) / 2.0);

                translate(spokeCellView, x - spokeBounds.x,
                          y - spokeBounds.y);

                //spokeBounds.setLocation(x, y);
                //System.err.println("X=" + x + " Y=" + y);
            }

            theta -= stepAngle;
        }
    }

    //Workaround for bug in JGraph where a group does not move
    //by setLocation
    private static void translate(CellView view, int dx, int dy) {

        if (view.isLeaf()) {
            GraphConstants.translate(view.getAttributes(), dx, dy);
        } else {

            CellView[] childViews = view.getChildViews();

            for (int i = 0; i < childViews.length; i++) {
                translate(childViews[i], dx, dy);
            }
        }
    }

    public JBGraphCell getJBGraphCellAt(DefaultGraphCell parent, int x, int y) {
        JBGraphCell jbCell = null;
        
        java.util.List children = parent.getChildren();
        if (children != null) {
            Iterator iter = children.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof JBGraphCell) {
                    JBGraphCell _jbCell = (JBGraphCell)obj;
                    Rectangle bounds = getCellBounds(_jbCell);
                    if (bounds.contains(x, y)) {
                        jbCell = _jbCell;
                        break;
                    }
                }                    
            }
        }
                        
        return jbCell;
    }
    
    // This will change the source of the actionevent to graph.
    protected class EventRedirector extends AbstractAction {

        /** DOCUMENT ME! */
        protected Action action;

        // Construct the "Wrapper" Action
        public EventRedirector(Action a) {
            super("", (ImageIcon)a.getValue(Action.SMALL_ICON));
            this.action = a;
        }

        // Redirect the Actionevent
        public void actionPerformed(ActionEvent e) {
            e = new ActionEvent(JBGraph.this, e.getID(),
                                e.getActionCommand(), e.getModifiers());
            action.actionPerformed(e);
        }
    }
    
    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return editable;
    }    
    
}
