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
package org.netbeans.modules.javadoc.httpfs;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import java.io.IOException;
import org.openide.DialogDescriptor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
/** The action that mount new file system.
*
* @author Petr Hamernik (checked [PENDING HelpCtx])
*/
public class AddFSAction extends CallableSystemAction {
    /** Icon of this action.
    * @return name of the action icon
    */
    public String iconResource() {
        return null; }
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass()); }
    public String getName() {
        return ResourceUtils.getBundledString("AddFS");  }//NOI18N
    /** Gets localized string. */
    private static final String getString(String s) {
        return NbBundle.getBundle(AddFSAction.class).getString(s); }
    /** Adds a directory. */
    public void performAction() {
        HTTPFileSystem httpFs = new HTTPFileSystem();
        UrlChooser chooser = new UrlChooser(httpFs);
        Object closeOptions[] = { DialogDescriptor.CANCEL_OPTION };
        DialogDescriptor dd = new DialogDescriptor(chooser, ResourceUtils.getBundledString("CTL_Choose_URL"), true, chooser);  //NOI18N
        // Prevent the OK button from closing the dialog box
        dd.setClosingOptions( closeOptions );
        TopManager.getDefault().createDialog(dd).setVisible(true);
        if( dd.getValue().equals(DialogDescriptor.OK_OPTION) ) {
            Repository r = TopManager.getDefault().getRepository ();
            if (r.findFileSystem(httpFs.getSystemName()) == null) {
                r.addFileSystem (httpFs); }
            else {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(ResourceUtils.getBundledString("MSG_LocalFSAlreadyMounted"),    //NOI18N
                                                 NotifyDescriptor.ERROR_MESSAGE)
                ); } } }
    /** Class used for the choosing of filesystem (local or jar) */
    private static class UrlChooser extends javax.swing.JPanel implements ActionListener {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 4451076155975278278L;
        private javax.swing.JTextField jTextField2 = new javax.swing.JTextField();
        private javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        private HTTPFileSystem httpFs;
        private UrlChooser(HTTPFileSystem httpFs) {
            this.httpFs = httpFs;
            setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints1;
            jTextField2.setFont(new java.awt.Font("Dialog", 0, 11));    //NOI18N
            jTextField2.setText(this.httpFs.getURL());  //NOI18N
            jTextField2.setPreferredSize(new java.awt.Dimension(200, 20));
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(12, 0, 12, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            add(jTextField2, gridBagConstraints1);
            jLabel1.setText("URL: "); //NOI18N
            jLabel1.setToolTipText(ResourceUtils.getBundledString("HINT_Doc_URL"));  //NOI18N
            jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));    //NOI18N
            jLabel1.setLabelFor(jTextField2);
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.insets = new java.awt.Insets(12, 12, 12, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            add(jLabel1, gridBagConstraints1); }
        public void actionPerformed(ActionEvent actionEvent) {
            // If this is the OK button,
            if( actionEvent.getSource( ) == NotifyDescriptor.OK_OPTION ) {
                try {
                    // Change the URL of the new file system
                    httpFs.setURL( jTextField2.getText( ) );
                    // Close the dialog box
                    ( (Dialog)getTopLevelAncestor( ) ).dispose( );
                // If there was an error with the new URL,
                } catch( IOException e ) {
                    // Display message to user and reset the URL to its previous value
                    TopManager.getDefault( ).notify( new NotifyDescriptor.Message( e.getMessage( ), NotifyDescriptor.ERROR_MESSAGE ) ); } } } } }
