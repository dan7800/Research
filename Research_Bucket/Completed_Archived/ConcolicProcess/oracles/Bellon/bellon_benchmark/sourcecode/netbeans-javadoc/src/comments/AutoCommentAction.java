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
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.util.ResourceBundle;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.src.ClassElement;
import org.openide.src.SourceElement;
import org.openide.cookies.SourceCookie;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileStateInvalidException;
/**
 * Auto comment action.
 *
 * @author   Petr Hrebejk
 */
public class AutoCommentAction extends CookieAction {
    static final long serialVersionUID =4989490116568783623L;
    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getBundle( AutoCommentAction.class ).getString("CTL_AUTOCOMMENT_MenuItem");     }//NOI18N
    /** Cookie classes contains one class returned by cookie () method.
     */
    protected final Class[] cookieClasses() {
        return new Class[] { DataFolder.class, SourceCookie.Editor.class }; }
    /** All must be DataFolders or JavaDataObjects
     */
    protected int mode() {
        return MODE_ALL; }
    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AutoCommentAction.class); }
    protected boolean enable( Node[] activatedNodes ) {
        if( activatedNodes.length == 0 )
            return false;
        int jdoCount = 0;
        for( int i = 0; i < activatedNodes.length; i++ ){
            SourceCookie sc = (SourceCookie)activatedNodes[i].getCookie(SourceCookie.Editor.class);
            if ( sc == null )
                continue;
            SourceElement se = sc.getSource();
            if ( se != null ) {
                DataObject doj = (DataObject)se.getCookie(DataObject.class);
                if( doj != null && doj.getPrimaryFile().isReadOnly() )
                    continue; }
            jdoCount++; }
        if( jdoCount != 0)
            return true;
        else {
            return findInFolder( activatedNodes ); } }
    /**
     * @param activatedNodes activated nodes on which look up
     * @return true if there is any commentable source
     */
    private boolean findInFolder( Node[] activatedNodes ){
        for( int i = 0; i < activatedNodes.length; i++ ){
            DataFolder df = (DataFolder)activatedNodes[i].getCookie(DataFolder.class);
            if( df == null )
                continue;
            try {
                if(df.getPrimaryFile().getFileSystem().isDefault())
                    continue; }
            catch(FileStateInvalidException fsie) {
                continue; }
            return true;
            /*
            DataObject[] children = df.getChildren();
            for ( int n = 0; n < children.length; n++ ){
                SourceCookie sc = (SourceCookie)children[n].getCookie(SourceCookie.class);
                if( sc != null && !children[n].getPrimaryFile().isReadOnly())
                    return true; }
             */ }
        return false; }
    /** This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     * This default implementation calls the assigned actionPerformer if it
     * is not null otherwise the action is ignored.
     */
    public void performAction( Node[] nodes ) {
        AutoCommentTopComponent acTopComponent = AutoCommentTopComponent.getDefault();
        acTopComponent.open();
        acTopComponent.requestFocus();
        acTopComponent.setAutoCommenter( new AutoCommenter( nodes )); }
    protected String iconResource(){
        return "/org/netbeans/modules/javadoc/resources/autocomment.gif";  } }//NOI18N
