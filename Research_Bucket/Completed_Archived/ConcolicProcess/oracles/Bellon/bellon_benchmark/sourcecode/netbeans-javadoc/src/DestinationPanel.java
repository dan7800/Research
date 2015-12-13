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
package org.netbeans.modules.javadoc;
import java.io.File;
import java.util.ResourceBundle;
import java.awt.event.KeyEvent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
/** Lets user select the destination for generating documentation
 * @author  phrebejk
 */
class DestinationPanel extends javax.swing.JPanel
    implements java.awt.event.ActionListener {
    /** Options of the dialog */
    private static javax.swing.JButton OK_BUTTON = null;
    private static javax.swing.JButton CANCEL_BUTTON = null;
    /** The dialog containing this panel */
    java.awt.Dialog dialog;
    /** The default destination directory */
    private File defaultDir = null;
    static final long serialVersionUID =1905540018208272852L;
    /** Creates new form DestinationPanel */
    public DestinationPanel() {
        initComponents ();
        initAccessibility();
        // i18n
        destinationLabel.setText( ResourceUtils.getBundledString( "CTL_Destination_label" ) );     //NOI18N
        destinationLabel.setDisplayedMnemonic(ResourceUtils.getBundledString("CTL_Destination_label_Mnemonic").charAt(0));  // NOI18N
        browseButton.setText( ResourceUtils.getBundledString( "CTL_Destination_browseButton" ) );   //NOI18N
        browseButton.setMnemonic(ResourceUtils.getBundledString("CTL_Destination_browseButton_Mnemonic").charAt(0));  // NOI18N
        /*
        destinationField.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER));
        System.out.println( destinationField.getRegisteredKeyStrokes()[0] ); 
        System.out.println( KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        Keymap defaultKM = destinationField.getKeymap( javax.swing.text.JTextComponent.DEFAULT_KEYMAP );
        destinationField.removeKeymap( javax.swing.text.JTextComponent.DEFAULT_KEYMAP );
        destinationField.addKeymap( null, null );
        //f = new JTextField();
        // KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        // Keymap map = f.getKeymap();
        // map.removeKeyStrokeBinding(enter);
        */
        HelpCtx.setHelpIDString (this, DestinationPanel.class.getName ()); }
    private void initAccessibility() {
            destinationField.getAccessibleContext().setAccessibleName(ResourceUtils.getBundledString("ACS_Destination_textFieldA11yName"));   }// NOI18N
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        destinationLabel = new javax.swing.JLabel();
        destinationField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        setLayout(new java.awt.GridBagLayout());
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(16, 16, 16, 16)));
        setPreferredSize(new java.awt.Dimension(405, 71));
        destinationLabel.setText("jLabel1");
        destinationLabel.setLabelFor(destinationField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(destinationLabel, gridBagConstraints);
        destinationField.setToolTipText(org.openide.util.NbBundle.getBundle(DestinationPanel.class).getString("ACS_Destination_textFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(destinationField, gridBagConstraints);
        browseButton.setToolTipText(org.openide.util.NbBundle.getBundle(DestinationPanel.class).getString("ACS_Destination_browseButtonA11yDesc"));
        browseButton.setText("jButton1");
        browseButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(browseButton, gridBagConstraints);
    }//GEN-END:initComponents
    private void browseButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(ResourceUtils.getBundledString("CTL_DestChooser_Title"));   //NOI18N
        if ( defaultDir != null) {
            chooser.setSelectedFile( defaultDir ); }
        HelpCtx.setHelpIDString (chooser, DestinationPanel.class.getName ());
        if (chooser.showDialog(TopManager.getDefault ().getWindowManager ().getMainWindow (),
                               ResourceUtils.getBundledString("CTL_Destination_Approve_Button"))   //NOI18N
                == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if ( f != null && f.isDirectory() ) {
                destinationField.setText( f.getAbsolutePath() ); } }
    }//GEN-LAST:event_browseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JTextField destinationField;
    private javax.swing.JButton browseButton;
    // End of variables declaration//GEN-END:variables
    private void setDefaultDir( File defaultDir ) {
        this.defaultDir = defaultDir;
        destinationField.setText( defaultDir.getAbsolutePath() ); }
    private File getDestination() {
        File dest = new File( destinationField.getText() );
        if ( dest != null && dest.isDirectory() ) {
            return dest; }
        else {
            return null; } }
    static File showDialog( File defaultDir ) {
        DestinationPanel panel = new DestinationPanel();
        panel.setDefaultDir( defaultDir );
        panel.getAccessibleContext().setAccessibleName(ResourceUtils.getBundledString("ACS_Destination_panelA11yName"));  // NOI18N
        panel.getAccessibleContext().setAccessibleDescription(ResourceUtils.getBundledString("ACS_Destination_panelA11yDesc"));  // NOI18N
        OK_BUTTON = new javax.swing.JButton( ResourceUtils.getBundledString( "CTL_Destination_OkButton" ) );   //NOI18N
        OK_BUTTON.setToolTipText(ResourceUtils.getBundledString("ACS_Destination_OkButtonA11yDesc"));  // NOI18N
        OK_BUTTON.setMnemonic(ResourceUtils.getBundledString("CTL_Destination_OkButton_Mnemonic").charAt(0));  // NOI18N
        CANCEL_BUTTON = new javax.swing.JButton( ResourceUtils.getBundledString( "CTL_Destination_CancelButton" ) );   //NOI18N
        CANCEL_BUTTON.setToolTipText(ResourceUtils.getBundledString("ACS_Destination_CancelButtonA11yDesc"));  // NOI18N
        CANCEL_BUTTON.setMnemonic(ResourceUtils.getBundledString("CTL_Destination_CancelButton_Mnemonic").charAt(0));  // NOI18N
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                                                panel,
                                                ResourceUtils.getBundledString( "CTL_Destination_Title" ),   //NOI18N
                                                true,
                                                new Object[] { OK_BUTTON, CANCEL_BUTTON },
                                                OK_BUTTON,
                                                DialogDescriptor.BOTTOM_ALIGN,
                                                new HelpCtx ( DestinationPanel.class ),
                                                panel );
        panel.dialog = TopManager.getDefault().createDialog( dialogDescriptor );
        panel.dialog.show();
        if ( dialogDescriptor.getValue() == OK_BUTTON ) {
            return panel.getDestination(); }
        else {
            return null; } }
    public void actionPerformed(final java.awt.event.ActionEvent evt) {
        if ( dialog == null )
            return;
        if ( evt.getSource() != OK_BUTTON && evt.getSource() != CANCEL_BUTTON )
            return;
        if ( evt.getSource() == OK_BUTTON ) {
            File f = getDestination();
            if ( f == null ) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                                          ResourceUtils.getBundledString( "MSG_NonExistingDirectory" ),   //NOI18N
                                          NotifyDescriptor.OK_CANCEL_OPTION );
                TopManager.getDefault().notify( nd );
                if ( nd.getValue() == NotifyDescriptor.OK_OPTION ) { // Create the directory
                    File newDir = new File( destinationField.getText() );
                    newDir.mkdirs();
                    if ( !newDir.isDirectory() ) { // Can't create directory
                        NotifyDescriptor ndm = new NotifyDescriptor.Message(
                                                   ResourceUtils.getBundledString( "MSG_CantCreateDirectory" ) );   //NOI18N
                        TopManager.getDefault().notify( ndm );
                        return; }
                    else { // Directory created
                         } }// Do nothing
                else { // Don't create directory
                    return; } } }
        // Javadoc runs or cancel was pressed
        dialog.setVisible( false );
        dialog.dispose();
        dialog = null; } }