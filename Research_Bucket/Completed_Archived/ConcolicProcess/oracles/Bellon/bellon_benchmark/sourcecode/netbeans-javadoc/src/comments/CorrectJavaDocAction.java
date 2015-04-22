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
import org.openide.src.nodes.ElementNode;
import org.openide.src.ClassElement;
import org.openide.src.Element;
import org.openide.cookies.SourceCookie;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
/**
 * Correct Javadoc action.
 *
 * @author   Mauro Botelho
 */
public class CorrectJavaDocAction extends CookieAction {
    static final long serialVersionUID =4989490116568783623L;
    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName () {
        return NbBundle.getBundle( CorrectJavaDocAction.class ).getString("CTL_CORRECTJAVADOC_MenuItem");    }//NOI18N
    /** Cookie classes contains one class returned by cookie () method.
     */
    protected final Class[] cookieClasses () {
        return new Class[] { SourceCookie.Editor.class }; }
    /** All must be DataFolders or JavaDataObjects
     */
    protected int mode () {
        return MODE_ALL; }
    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CorrectJavaDocAction.class); }
    /** Enable this action only if it is really possible to correct the javadoc
     * for this element.
     */
    protected boolean enable( Node[] activatedNodes ) {
        if ((activatedNodes.length != 1) || !(activatedNodes[0] instanceof ElementNode)) {
            return false; }
        Element element = (Element) activatedNodes[0].getCookie(Element.class);
        if( element == null )   //for situation getCookie returns null value
            return false;
        AutoCommenter.Element jdElement = autoCommenterElementFactory(element);
        if( jdElement == null ) //it is for example static initializer
            return false;
        return jdElement.isCorrectable(); }
    private AutoCommenter.Element autoCommenterElementFactory(Element element) {
        AutoCommenter.Element jdElement = null;
        if (element instanceof org.openide.src.ClassElement) {
            jdElement = new AutoCommenter.Element.Class((org.openide.src.ClassElement)element);
        } else if (element instanceof org.openide.src.MethodElement) {
            jdElement = new AutoCommenter.Element.Method((org.openide.src.MethodElement)element);
        } else if (element instanceof org.openide.src.ConstructorElement) {
            jdElement = new AutoCommenter.Element.Constructor((org.openide.src.ConstructorElement)element);
        } else if (element instanceof org.openide.src.FieldElement) {
            jdElement = new AutoCommenter.Element.Field((org.openide.src.FieldElement)element); }
        return jdElement; }
    /** This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     * This default implementation calls the assigned actionPerformer if it
     * is not null otherwise the action is ignored.
     */
    public void performAction ( Node[] nodes ) {
        Element element = (Element) nodes[0].getCookie(Element.class);
        AutoCommenter.Element jdElement = autoCommenterElementFactory(element);
        try {
            if (jdElement.isCorrectable()) {
                jdElement.autoCorrect(); }
        } catch (org.openide.src.SourceException e) {
            TopManager.getDefault().notify(new NotifyDescriptor.Exception(e)); } } }
