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
import org.openide.ErrorManager;
import org.openide.TopManager;
import org.openide.src.MemberElement;
import org.openide.src.ConstructorElement;
import org.openide.src.Identifier;
import org.openide.src.JavaDocTag;
import org.openide.src.JavaDocSupport;
import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.JEditorPane;
/**
 *
 * @author
 * @version
 */
abstract class TagPanelExtended extends javax.swing.JPanel {
    protected ArrayList htmlComponents = new ArrayList();
    private static JavaDocEditorPanel editorPanel;
    TagPanelExtended( JavaDocEditorPanel editorPanel ) {
        this.editorPanel = editorPanel; }
    abstract void setData( JavaDocTag tag );
    abstract String getCardName();
    abstract JavaDocTag getTag( String tagName );
    void addHTMLComponent( JEditorPane component ) {
        htmlComponents.add( component ); }
    void handleFormatButton( String begTag, String endTag ) {
        for ( int i = htmlComponents.size(); i > 0; --i ) {
            JEditorPane c = (JEditorPane)htmlComponents.get( i );
            if ( c.hasFocus() ) {
                StringBuffer sb = new StringBuffer( c.getText());
                int caretPosition = c.getCaretPosition();
                /*
                sb.insert( c.getSelectionStart(), begTag );
                sb.insert( c.getSelectionEnd() + begTag.length(), endTag );
                c.setText( sb.toString() );
                c.setCaretPosition( caretPosition + begTag.length() );
                */
                try {
                    c.getDocument().insertString( c.getSelectionStart(), begTag, null );
                    c.getDocument().insertString( c.getSelectionEnd(), endTag, null );
                    c.setCaretPosition( caretPosition + begTag.length() ); }
                catch ( javax.swing.text.BadLocationException e ) {
                     }//System.out.println(e );
                break; } } }
    void commitTagChange() {
        editorPanel.commitTagChange(); }
    void enableHTMLButtons( boolean disable ) {
        editorPanel.enableButtons( disable ); }
    abstract void grabFirstFocus(); }
public class ThrowsTagPanel extends TagPanel {
    private static final String cardName = "CRD_THROWS"; // NOI18N
    static final long serialVersionUID =2048140086381465545L;
    /** Initializes the Form */
    public ThrowsTagPanel( JavaDocEditorPanel editorPanel ) {
        super( editorPanel );
        initComponents ();
        jLabel2.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(StandardTagPanel.class).getString("CTL_ThrowsTagPanel.jLabel2.text_Mnemonic").charAt(0));  // NOI18N
        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(StandardTagPanel.class).getString("CTL_ThrowsTagPanel.jLabel1.text_Mnemonic").charAt(0));  // NOI18N
        editorPanel.registerComponent( descriptionTextArea );
        addHTMLComponent( descriptionTextArea );
        exceptionComboBox.getEditor().getEditorComponent().addFocusListener(
        new java.awt.event.FocusAdapter () {
            public void focusLost (java.awt.event.FocusEvent evt) {
                commitTagChange(); }
        });
        initAccessibility(); }
    public ThrowsTagPanel( MemberElement element, JavaDocEditorPanel editorPanel ) {
        this( editorPanel );
        setElement(element); }
    public void setElement( MemberElement element ) {
        exceptionComboBox.removeAllItems();
        if ( element instanceof ConstructorElement ) {
            Identifier exceptions[] = ((ConstructorElement)element).getExceptions();
            for( int i = 0; i < exceptions.length; i++ ) {
                exceptionComboBox.addItem( exceptions[i].getName() ); }
            exceptionComboBox.setSelectedItem( "" );  } }// NOI18N
    private void initAccessibility() {
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("ACS_ThrowsTagPanel.jLabel2.textA11yDesc"));  // NOI18N
        exceptionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("ACS_ThrowsTagPanel.exceptionComboBox.textA11yName"));  // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("ACS_ThrowsTagPanel.jLabel1.textA11yDesc"));  // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("ACS_ThrowsTagPanel.descriptionTextArea.textA11yName"));   }// NOI18N
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        jLabel2 = new javax.swing.JLabel();
        exceptionComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JEditorPane();
        setLayout(new java.awt.GridBagLayout());
        jLabel2.setText(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("CTL_ThrowsTagPanel.jLabel2.text"));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setLabelFor(exceptionComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 1);
        add(jLabel2, gridBagConstraints);
        exceptionComboBox.setMaximumRowCount(4);
        exceptionComboBox.setToolTipText(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("ACS_ThrowsTagPanel.exceptionComboBox.textA11yDesc"));
        exceptionComboBox.setEditable(true);
        exceptionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exceptionComboBoxActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 1, 2, 2);
        add(exceptionComboBox, gridBagConstraints);
        jLabel1.setText(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("CTL_ThrowsTagPanel.jLabel1.text"));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setLabelFor(descriptionTextArea);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 1);
        add(jLabel1, gridBagConstraints);
        descriptionTextArea.setToolTipText(org.openide.util.NbBundle.getBundle(ThrowsTagPanel.class).getString("ACS_ThrowsTagPanel.descriptionTextArea.textA11yDesc"));
        descriptionTextArea.setContentType("text/html");
        descriptionTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                descriptionTextAreaFocusGained(evt); }
            public void focusLost(java.awt.event.FocusEvent evt) {
                descriptionTextAreaFocusLost(evt); }
        });
        descriptionScrollPane.setViewportView(descriptionTextArea);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 1, 2, 2);
        add(descriptionScrollPane, gridBagConstraints);
    }//GEN-END:initComponents
    private void descriptionTextAreaFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_descriptionTextAreaFocusGained
        enableHTMLButtons( true );
    }//GEN-LAST:event_descriptionTextAreaFocusGained
    private void descriptionTextAreaFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_descriptionTextAreaFocusLost
        enableHTMLButtons( false );
        commitTagChange();
    }//GEN-LAST:event_descriptionTextAreaFocusLost
    private void exceptionComboBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exceptionComboBoxActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_exceptionComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JComboBox exceptionComboBox;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JEditorPane descriptionTextArea;
    // End of variables declaration//GEN-END:variables
    void setData( JavaDocTag tag ) {
        exceptionComboBox.setSelectedItem(((JavaDocTag.Throws)tag).exceptionName());
        String exceptionComment = ((JavaDocTag.Throws)tag).exceptionComment().trim();
        if ((exceptionComment != null) && (! "".equals(exceptionComment))) {    //NOI18N
            descriptionTextArea.setText( exceptionComment );
        } else {
            try {
                descriptionTextArea.getDocument().remove(0, descriptionTextArea.getDocument().getLength());
            } catch (BadLocationException e) {
                TopManager.getDefault().getErrorManager().annotate(e, "Failed to remove the text in the descriptionTextArea.");  } } }//NOI18N
    JavaDocTag getTag( String tagName ) {
        return JavaDocSupport.createThrowsTag( tagName,
        exceptionComboBox.getEditor().getItem().toString() + " " + // NOI18N
        descriptionTextArea.getText() ); }
    String getCardName() {
        return cardName; }
    void grabFirstFocus() {
        // JHK 9/29/2000 - cause focus to default to description if the name combo box is filled
        if (( exceptionComboBox.getSelectedIndex() == -1 && exceptionComboBox.getSelectedItem() != null && exceptionComboBox.getSelectedItem().equals( "" ) ) || ("".equals((String)exceptionComboBox.getEditor().getItem()))) {    //NOI18N
            exceptionComboBox.requestFocus();
        } else {
            descriptionTextArea.requestFocus(); } } }
