/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * DocFSOffsetEditor.java
 *
 * Created on 26. ?nor 2001, 12:00
 */
package org.netbeans.modules.javadoc.search;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.*;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.filesystems.FileSystem;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.TopManager;
/**
 *
 * @author  Petr Suchomel
 * @version 
 */
class DocFSOffsetEditor extends PropertyEditorSupport implements PropertyChangeListener { //ExplorerPanel implements PropertyEditor, PropertyChangeListener { 
    private Node node;
    private PropertyChangeSupport supp = new PropertyChangeSupport (this);
    public DocFSOffsetEditor(Node node){
        this.node = node; }
    public void addPropertyChangeListener (PropertyChangeListener l) {
        super.addPropertyChangeListener(l);
        supp.addPropertyChangeListener (l); }
    public void removePropertyChangeListener (PropertyChangeListener l) {
        super.removePropertyChangeListener(l);
        supp.removePropertyChangeListener (l); }
    public boolean supportsCustomEditor () {
        return true; }
    public Component getCustomEditor () {
	java.awt.GridBagConstraints gridBagConstraints1;
        gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints1.insets = new java.awt.Insets(12, 0, 12, 12);
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	ExplorerPanel panel = new P ();
        panel.setLayout (new BorderLayout ());
        panel.add (new BeanTreeView (), BorderLayout.CENTER);
        panel.getExplorerManager ().addPropertyChangeListener (this);        
        panel.getExplorerManager ().setRootContext (node);        
        String secondRoot = ((JavaDocFSNode)node).getJavaDocFSSettings().getSecondRoot();
        if( secondRoot != null && secondRoot.length() != 0 ) {
            try{      
                Node n = NodeOp.findPath(node, new StringTokenizer(secondRoot,"/"));    //NOI18N
                if( n != null )
                    panel.getExplorerManager().setSelectedNodes(new Node[]{n}); }
            catch(NodeNotFoundException nof){}  //only catch it
            catch(java.beans.PropertyVetoException veto){} }
        return panel; }
    public void propertyChange (PropertyChangeEvent ev) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
            Node[] nodes = (Node[]) ev.getNewValue ();
            if (nodes.length == 1 && nodes[0].getCookie (DataFolder.class) != null) {                
                setValue (nodes[0].getCookie (DataFolder.class));
                supp.firePropertyChange (ExPropertyEditor.PROP_VALUE_VALID, null, Boolean.TRUE);
            } else {
                setValue (null);
                supp.firePropertyChange (ExPropertyEditor.PROP_VALUE_VALID, null, Boolean.FALSE); } } }
    static class P extends ExplorerPanel {
        public HelpCtx getHelpCtx() {
            return new HelpCtx(DocFSOffsetEditor.class); } } }
