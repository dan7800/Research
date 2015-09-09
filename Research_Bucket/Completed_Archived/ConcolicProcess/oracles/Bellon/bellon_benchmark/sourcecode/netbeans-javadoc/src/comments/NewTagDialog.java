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
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ButtonGroup;
import org.openide.src.MemberElement;
import org.openide.src.ConstructorElement;
import org.openide.src.FieldElement;
import org.openide.src.ClassElement;
import org.openide.src.JavaDocTag;
import org.openide.src.JavaDocSupport;
/**
 *
 * @author
 * @version
 */
public class NewTagDialog extends javax.swing.JDialog implements JavaTagNames {
    private ButtonGroup bgroup;
    private JavaDocTag result = null;
    private MemberElement element;
    private String TAG_CUSTOM = "@"; // NOI18N
    static final long serialVersionUID =8648611535888090920L;
    /** Initializes the Form */
    public NewTagDialog(java.awt.Frame parent, boolean modal, MemberElement element ) {
        super (parent, modal);
        initComponents ();
        this.element = element;
        javax.swing.JTextField jTextField2 = new javax.swing.JTextField();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	int a = 10;
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
	jTextField2.setFont(new java.awt.Font("Dialog", a, a));    //NOI18N
	jTextField2.setText("(unknown)");  //NOI18N
	jTextField2.setPreferredSize(new java.awt.Dimension(20 * a, 2 * a));
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints1.insets = new java.awt.Insets(a, 0, a, 12);
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	add(jTextField2, gridBagConstraints1);
	jLabel1.setText("URL: "); //NOI18N
	jLabel1.setToolTipText(ResourceUtils.getBundledString("HINT_Doc_URL"));  //NOI18N
	jLabel1.setFont(new java.awt.Font("Dialog", 0, a));    //NOI18N
	jLabel1.setLabelFor(jTextField2);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.insets = new java.awt.Insets(12, a, 12, a);
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        bgroup = new ButtonGroup();
        bgroup.add( authorRadioButton );
        authorRadioButton.getModel().setActionCommand( TAG_AUTHOR );
        authorRadioButton.setVisible( false );
        authorRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.authorRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( deprecatedRadioButton );
        deprecatedRadioButton.getModel().setActionCommand( TAG_DEPRECATED );
        deprecatedRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.deprecatedRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( paramRadioButton );
        paramRadioButton.getModel().setActionCommand( TAG_PARAM );
        paramRadioButton.setVisible( false );
        paramRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.paramRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( returnRadioButton );
        returnRadioButton.getModel().setActionCommand( TAG_RETURN );
        returnRadioButton.setVisible( false );
        returnRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.returnRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( seeRadioButton );
        seeRadioButton.getModel().setActionCommand( TAG_SEE );
        seeRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.seeRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( serialRadioButton );
        serialRadioButton.getModel().setActionCommand( TAG_SERIAL );
        serialRadioButton.setVisible( false );
        serialRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.serialRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( serialDataRadioButton );
        serialDataRadioButton.getModel().setActionCommand( TAG_SERIALDATA );
        serialDataRadioButton.setVisible( false );
        serialDataRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.serialDataRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( serialFieldRadioButton );
        serialFieldRadioButton.getModel().setActionCommand( TAG_SERIALFIELD );
        serialFieldRadioButton.setVisible( false );
        serialFieldRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.serialFieldRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( sinceRadioButton );
        sinceRadioButton.getModel().setActionCommand( TAG_SINCE );
        sinceRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.sinceRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( throwsRadioButton );
        throwsRadioButton.getModel().setActionCommand( TAG_THROWS );
        throwsRadioButton.setVisible( false );
        throwsRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.throwsRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( versionRadioButton );
        versionRadioButton.getModel().setActionCommand( TAG_VERSION );
        versionRadioButton.setVisible( false );
        versionRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.versionRadioButton_Mnemonic").charAt(0));  // NOI18N
        bgroup.add( customRadioButton );
        customRadioButton.getModel().setActionCommand( TAG_CUSTOM );
        customRadioButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("NewTagDialog.customRadioButton_Mnemonic").charAt(0));  // NOI18N
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = this.getSize();
        this.setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
        if ( element instanceof ConstructorElement ) {
            paramRadioButton.setVisible( true );
            returnRadioButton.setVisible( true );
            serialDataRadioButton.setVisible( true );
            throwsRadioButton.setVisible( true ); }
        else if ( element instanceof FieldElement ) {
            serialRadioButton.setVisible( true );
            serialFieldRadioButton.setVisible( true ); }
        if ( element instanceof ClassElement ) {
            authorRadioButton.setVisible( true );
            versionRadioButton.setVisible( true ); }
        okButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("CTL_NewTagDialog.okButton_Mnemonic").charAt(0));  // NOI18N
        cancelButton.setMnemonic(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("CTL_NewTagDialog.cancelButton_Mnemonic").charAt(0));  // NOI18N
        pack ();
        initAccessibility(); }
    private void initAccessibility() {
        customTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.customTextField.textA11yName"));   }// NOI18N
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel2 = new javax.swing.JPanel();
        authorRadioButton = new javax.swing.JRadioButton();
        deprecatedRadioButton = new javax.swing.JRadioButton();
        paramRadioButton = new javax.swing.JRadioButton();
        returnRadioButton = new javax.swing.JRadioButton();
        seeRadioButton = new javax.swing.JRadioButton();
        serialRadioButton = new javax.swing.JRadioButton();
        serialDataRadioButton = new javax.swing.JRadioButton();
        serialFieldRadioButton = new javax.swing.JRadioButton();
        sinceRadioButton = new javax.swing.JRadioButton();
        throwsRadioButton = new javax.swing.JRadioButton();
        versionRadioButton = new javax.swing.JRadioButton();
        customRadioButton = new javax.swing.JRadioButton();
        customTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        setTitle(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.Form.title"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt); }
        });
        jPanel2.setLayout(new java.awt.GridBagLayout());
        jPanel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        authorRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.authorRadioButton.textA11yDesc"));
        authorRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.authorRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(authorRadioButton, gridBagConstraints);
        deprecatedRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.deprecatedRadioButton.textA11yDesc"));
        deprecatedRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.deprecatedRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(deprecatedRadioButton, gridBagConstraints);
        paramRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.paramRadioButton.textA11yDesc"));
        paramRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.paramRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(paramRadioButton, gridBagConstraints);
        returnRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.returnRadioButton.textA11yDesc"));
        returnRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.returnRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(returnRadioButton, gridBagConstraints);
        seeRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.seeRadioButton.textA11yDesc"));
        seeRadioButton.setSelected(true);
        seeRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.seeRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(seeRadioButton, gridBagConstraints);
        serialRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.serialRadioButton.textA11yDesc"));
        serialRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.serialRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(serialRadioButton, gridBagConstraints);
        serialDataRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.serialDataRadioButton.textA11yDesc"));
        serialDataRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.serialDataRadioButton.text"));
        serialDataRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serialDataRadioButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(serialDataRadioButton, gridBagConstraints);
        serialFieldRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.serialFieldRadioButton.textA11yDesc"));
        serialFieldRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.serialFieldRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(serialFieldRadioButton, gridBagConstraints);
        sinceRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.sinceRadioButton.textA11yDesc"));
        sinceRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.sinceRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(sinceRadioButton, gridBagConstraints);
        throwsRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.throwsRadioButton.textA11yDesc"));
        throwsRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.throwsRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(throwsRadioButton, gridBagConstraints);
        versionRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.versionRadioButton.textA11yDesc"));
        versionRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.versionRadioButton.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(versionRadioButton, gridBagConstraints);
        customRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.customRadioButton.textA11yDesc"));
        customRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("NewTagDialog.customRadioButton.text"));
        customRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customRadioButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(customRadioButton, gridBagConstraints);
        customTextField.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.customTextField.textA11yDesc"));
        customTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customTextFieldActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(customTextField, gridBagConstraints);
        jPanel3.setLayout(new java.awt.GridLayout(1, 2));
        jPanel3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 1, 1, 1)));
        okButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.okButton.textA11yDesc"));
        okButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("CTL_NewTagDialog.okButton.text"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt); }
        });
        jPanel3.add(okButton);
        cancelButton.setToolTipText(org.openide.util.NbBundle.getBundle(NewTagDialog.class).getString("ACS_NewTagDialog.cancelButton.textA11yDesc"));
        cancelButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/javadoc/comments/Bundle").getString("CTL_NewTagDialog.cancelButton.text"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt); }
        });
        jPanel3.add(cancelButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jPanel3, gridBagConstraints);
        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);
    }//GEN-END:initComponents
    private void cancelButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // Add your handling code here:
        result = null;
        closeDialog( null );
    }//GEN-LAST:event_cancelButtonActionPerformed
    private void okButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // Add your handling code here:
        String command = bgroup.getSelection().getActionCommand();
        if ( command.equals( TAG_SEE ) ) {
            result = JavaDocSupport.createSeeTag( command, "" );  }// NOI18N
        else if ( command.equals( TAG_PARAM ) ) {
            result = JavaDocSupport.createParamTag( command, "" );  }// NOI18N
        else if ( command.equals( TAG_THROWS ) ) {
            result = JavaDocSupport.createThrowsTag( command, "" );  }// NOI18N
        else if ( command.equals( TAG_SERIALFIELD ) ) {
            result = JavaDocSupport.createSerialFieldTag( command, " " );  }// NOI18N
        else if ( command.equals( TAG_CUSTOM ) ) {
            result = JavaDocSupport.createTag( command + customTextField.getText(), "" );  }// NOI18N
        else {
            result = JavaDocSupport.createTag( command, "" ) ;  }// NOI18N
        closeDialog( null );
    }//GEN-LAST:event_okButtonActionPerformed
    private void serialDataRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serialDataRadioButtonActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_serialDataRadioButtonActionPerformed
    private void customTextFieldActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customTextFieldActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_customTextFieldActionPerformed
    private void customRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customRadioButtonActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_customRadioButtonActionPerformed
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible (false);
        dispose ();
    }//GEN-LAST:event_closeDialog
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton paramRadioButton;
    private javax.swing.JRadioButton deprecatedRadioButton;
    private javax.swing.JRadioButton serialFieldRadioButton;
    private javax.swing.JRadioButton authorRadioButton;
    private javax.swing.JRadioButton versionRadioButton;
    private javax.swing.JRadioButton serialRadioButton;
    private javax.swing.JRadioButton sinceRadioButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton throwsRadioButton;
    private javax.swing.JRadioButton customRadioButton;
    private javax.swing.JTextField customTextField;
    private javax.swing.JRadioButton returnRadioButton;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton serialDataRadioButton;
    private javax.swing.JRadioButton seeRadioButton;
    // End of variables declaration//GEN-END:variables
    public static void main(java.lang.String[] args) {
        new NewTagDialog (new java.awt.Frame (), false, null).show (); }
    JavaDocTag getResult() {
        return result; } }
