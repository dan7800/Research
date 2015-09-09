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
import java.util.Vector;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import org.openide.src.JavaDoc;
import org.openide.src.JavaDocTag;
/** Panel with standard tags
 *
 * @author Petr Hrebejk
 * @version
 */
abstract class TagPanel extends javax.swing.JPanel {
    protected ArrayList htmlComponents = new ArrayList();
    private static JavaDocEditorPanel editorPanel;
    TagPanel( JavaDocEditorPanel editorPanel ) {
        this.editorPanel = editorPanel; }
    abstract void setData( JavaDocTag tag );
    abstract String getCardName();
    abstract JavaDocTag getTag( String tagName );
    void addHTMLComponent( JEditorPane component ) {
        htmlComponents.add( component ); }
    void handleFormatButton( String begTag, String endTag ) {
        for ( int i = 0; i < htmlComponents.size(); i++ ) {
            JEditorPane component = (JEditorPane)htmlComponents.get( i );
            if ( component.hasFocus() ) {
                StringBuffer sb = new StringBuffer( component.getText());
                int caretPosition = component.getCaretPosition();
                /*
                sb.insert( component.getSelectionStart(), begTag );
                sb.insert( component.getSelectionEnd() + begTag.length(), endTag );
                component.setText( sb.toString() );
                component.setCaretPosition( caretPosition + begTag.length() );
                */
                try {
                    component.getDocument().insertString( component.getSelectionStart(), begTag, null );
                    component.getDocument().insertString( component.getSelectionEnd(), endTag, null );
                    component.setCaretPosition( caretPosition + begTag.length() ); }
                catch ( javax.swing.text.BadLocationException e ) {
                     }//System.out.println(e );
                break; } } }
    void commitTagChange() {
        editorPanel.commitTagChange(); }
    void enableHTMLButtons( boolean enable ) {
        editorPanel.enableButtons( enable ); }
    abstract void grabFirstFocus(); }
abstract class TaggedPanel extends javax.swing.JPanel {
    protected ArrayList htmlStuff = new ArrayList();
    private static JavaDocEditorPanel editor;
    TaggedPanel( JavaDocEditorPanel editor ) {
        this.editor = editor; }
    abstract void setData( JavaDocTag tag );
    abstract String getCardName();
    abstract JavaDocTag getTag( String tagName );
    void addHTMLStuff( JEditorPane component ) {
        htmlStuff.add( component ); }
    void handleFormatButton( String begTag, String endTag ) {
        for ( int i = 0; i < htmlStuff.size(); i++ ) {
            JEditorPane component = (JEditorPane)htmlStuff.get( i );
            if ( component.hasFocus() ) {
                StringBuffer sb = new StringBuffer( component.getText());
                int pos = component.getCaretPosition();
                /*
                sb.insert( component.getSelectionStart(), begTag );
                sb.insert( component.getSelectionEnd() + begTag.length(), endTag );
                component.setText( sb.toString() );
                component.setCaretPosition( pos + begTag.length() );
                */
                try {
                    component.getDocument().insertString( component.getSelectionStart(), begTag, null );
                    component.getDocument().insertString( component.getSelectionEnd(), endTag, null );
                    component.setCaretPosition( pos + begTag.length() ); }
                catch ( javax.swing.text.BadLocationException e ) {
                     }//System.out.println(e );
                break; } } }
    void commitTagChange() {
        editor.commitTagChange(); }
    void enableHTMLButtons( boolean flag ) {
        editor.enableButtons( flag ); }
    abstract void grabFirstFocus(); }
