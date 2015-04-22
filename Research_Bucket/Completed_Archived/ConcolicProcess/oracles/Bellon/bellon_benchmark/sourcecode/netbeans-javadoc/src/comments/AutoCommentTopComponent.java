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
package org.netbeans.modules.javadoc.comments;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.ResourceBundle;
import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
/** Just a top component which contains AutoCommentPanel
 *
 * @author  phrebejk
 * @version 
 */
public class AutoCommentTopComponent
            extends TopComponent
            implements Externalizable {
    static final String AUTO_COMMENT_HELP_CTX_KEY = "javadoc.auto.comment"; //NOI18N
    /** The only AutoCommentTopComponent allowed in the system */
    private static AutoCommentTopComponent acTopComponent;
    /** The panel contained by acTopComponent */
    private static AutoCommentPanel acPanel;
    /** Resource bundle for this class */
    //private static final ResourceBundle bundle = NbBundle.getBundle( AutoCommentAction.class );
    private static ResourceBundle bundle = null;
    static final long serialVersionUID =3696398508351593122L;
    private static String getBundledString(String key ){
       if( bundle == null ) //if bundle is null, load bundle
             bundle = NbBundle.getBundle( AutoCommentTopComponent.class );
       //return value by key
       return bundle.getString( key ); }
    /** Creates new AutoCommentTopComponent */
    private AutoCommentTopComponent() {
        setLayout( new BorderLayout() );
        add( acPanel = new AutoCommentPanel(), BorderLayout.CENTER ); }
    void setAutoCommenter( AutoCommenter aCommenter ) {
        acPanel.setAutoCommenter( aCommenter );
        // Nodes[] nodes = aCommenter.getNodes();
        Node[] nodes = aCommenter.nodes;
        String className = null;
        if (nodes.length > 1 ) {
            className = getBundledString ("CTL_AUTOCOMMENT_MultipleSources");   //NOI18N
        } else if (nodes.length == 1) {
            className = nodes[0].getDisplayName();
        } else {
            className = "";  }//NOI18N
        acTopComponent.setName( getBundledString ("CTL_AUTOCOMMENT_WindowTitle") + " - " + className);   }//NOI18N
    public static AutoCommentTopComponent getDefault() {
        if ( acTopComponent == null ) {
            acTopComponent = new AutoCommentTopComponent();
            acTopComponent.getAccessibleContext().setAccessibleName(getBundledString("ACS_AutoCommentPanelA11yName"));  // NOI18N
            acTopComponent.getAccessibleContext().setAccessibleDescription(getBundledString("ACS_AutoCommentPanelA11yDesc"));  // NOI18N
            org.netbeans.modules.javadoc.JavadocModule.registerTopComponent(acTopComponent);
            acTopComponent.setName( getBundledString ("CTL_AUTOCOMMENT_WindowTitle") );  }//NOI18N
        Workspace workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        Mode myMode = workspace.findMode(acTopComponent);
        if (myMode == null) {
            myMode = workspace.createMode("AutoComment", getBundledString ("CTL_AUTOCOMMENT_WindowTitle"), null );  //NOI18N
            myMode.setBounds(new Rectangle( 100, 50, 750, 450 ) ); //350
            myMode.dockInto( acTopComponent ); }
        return acTopComponent; }
    public HelpCtx getHelpCtx () {
        return new HelpCtx (AUTO_COMMENT_HELP_CTX_KEY); }
    public boolean canClose(Workspace workspace, boolean last){
        boolean rv = super.canClose(workspace, last);
        if (rv) {
            // This will update the javadoc before closing.
            acPanel.updateForClosing(); }
        acPanel.autoCommenter.removeAutoCommentChangeListener(null);    //remove listener
        return rv; }
    // Implementation of Externalizable -----------------------------------------
    /** Writes a resolvable */
    protected Object writeReplace() {
        return new Resolvable(); }
    static class Resolvable implements java.io.Serializable {
        static final long serialVersionUID =8143238035030034549L;
        private Object readResolve() {            
            acTopComponent = getDefault();
            acTopComponent.setAutoCommenter( new AutoCommenter());
            return acTopComponent; } } }
