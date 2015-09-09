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
import java.util.ResourceBundle;
import java.util.Date;
import java.lang.reflect.Modifier;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.text.MessageFormat;
import org.openide.awt.SplittedPanel;
import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.SharedClassObject;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
import org.openide.util.WeakListener;
/**
 */
public class AutoCommentPanel
extends javax.swing.JPanel
implements ListSelectionListener {
    AutoCommenter autoCommenter;
    private JavaDocEditorPanel javaDocEditor = new JavaDocEditorPanel();
    private static final boolean newMode = true;
    /** Holds the value of the last selected index in badList to be able to
     * call modifyJavaDoc method for that method. */
    private int badListLastSelectedIndex = -1;
    private boolean updatingBadList = false;
    private static final DefaultListModel EMPTY_MODEL = new DefaultListModel();
    static final String WAIT_STRING = "WAIT.MODEL"; // NOI18N
    private static final DefaultListModel WAIT_MODEL = new DefaultListModel();
    static {
        WAIT_MODEL.addElement( WAIT_STRING ); }
    private static final ImageIcon publicIcon = new ImageIcon (AutoCommentPanel.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/public.gif")); // NOI18N
    private static final ImageIcon protectedIcon = new ImageIcon (AutoCommentPanel.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/protected.gif")); // NOI18N
    private static final ImageIcon packageIcon = new ImageIcon (AutoCommentPanel.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/package.gif")); // NOI18N
    private static final ImageIcon privateIcon = new ImageIcon (AutoCommentPanel.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/private.gif")); // NOI18N
    private static final ImageIcon okIcon = new ImageIcon (AutoCommentPanel.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/ok.gif")); // NOI18N
    private static final ImageIcon errorIcon = new ImageIcon (AutoCommentPanel.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/error.gif")); // NOI18N
    private static final ImageIcon missIcon = new ImageIcon (AutoCommentPanel.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/missing.gif")); // NOI18N
    private int modifierMask;
    private int errorMask;
    /** The state of the window is stored in hidden options of DocumentationSettings */
    DocumentationSettings dss = ((DocumentationSettings)SharedClassObject.findObject(DocumentationSettings.class, true));
    static final long serialVersionUID =1845033305150331568L;
    /** Creates new form AutoCommentPanel
     */
    public AutoCommentPanel() {
        initComponents ();
        javaDocEditor.setEnabled(false);
        if (newMode) {
            customizeButton.setVisible(false); }
        customizeButton.setMnemonic( org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.customizeButton.text_Mnemonic").charAt(0) );
        defaultButton.setMnemonic( org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.defaultButton.text_Mnemonic").charAt(0) );
        sourceButton.setMnemonic( org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.sourceButton.text_Mnemonic").charAt(0) );
        refreshButton.setMnemonic( org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.refreshButton.text_Mnemonic").charAt(0) );
        okButton.setMnemonic( '1' );
        errButton.setMnemonic( '2' );
        missButton.setMnemonic( '3' );
        publicButton.setMnemonic( '4' );
        packageButton.setMnemonic( '5' );
        protectedButton.setMnemonic( '6' );
        privateButton.setMnemonic( '7' );
        splittedPanel = new SplittedPanel();
        splittedPanel.setSplitDragable(true);
        splittedPanel.setSplitTypeChangeEnabled(true);
        splittedPanel.setSwapPanesEnabled(true);
        splittedPanel.setKeepFirstSame(true);
        //splittedPanel.setContinuousLayout(true);
        splittedPanel.setSplitPosition(SplittedPanel.FIRST_PREFERRED);
        splittedPanel.setSplitType(SplittedPanel.HORIZONTAL);
        splittedPanel.add(classInfoPanel, SplittedPanel.ADD_TOP);
        splittedPanel.add(javaDocEditor, SplittedPanel.ADD_BOTTOM);
        splittedPanel.setSplitPosition(dss.getAutocommentSplit());
        add(splittedPanel);
        splittedPanel.addSplitChangeListener( new SplittedPanel.SplitChangeListener() {
            public void splitChanged (SplittedPanel.SplitChangeEvent evt) {
                int value = evt.getNewValue();
                dss.setAutocommentSplit( value ); }
        } );
        okButton.setIcon( okIcon );
        errButton.setIcon( errorIcon );
        missButton.setIcon( missIcon );
        publicButton.setIcon( publicIcon );
        packageButton.setIcon( packageIcon );
        protectedButton.setIcon( protectedIcon );
        privateButton.setIcon( privateIcon );
        resolveButtonState();
        badList.setCellRenderer( new AutoCommentListCellRenderer() );
        badList.getSelectionModel().addListSelectionListener(
        new javax.swing.event.ListSelectionListener() {
            public void valueChanged( javax.swing.event.ListSelectionEvent evt ) {
                elementSelection( evt ); } }
        );
        detailsPanel.setBorder (new javax.swing.border.TitledBorder(
        new javax.swing.border.EtchedBorder(),
        org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.detailsPanel.border") ) );  //NOI18N
        elementSelection( new ListSelectionEvent( badList, -1, -1, false ) );
        initAccessibility(); }
    /**
     */
    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension sup = super.getPreferredSize ();
        //return new java.awt.Dimension ( Math.max (sup.width, 300), Math.max (sup.height, 400 ));
        return new java.awt.Dimension ( 350, Math.max (sup.height, 400 )); }
    private void initAccessibility() {
        badList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.methodListA11yName"));  // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.nameLabel.textA11yDesc"));  // NOI18N
        classTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.nameTextField.textA11yName"));  // NOI18N
        errorListBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.commentList.textA11yName"));   }// NOI18N
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        classInfoPanel = new javax.swing.JPanel();
        filterPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JToggleButton();
        errButton = new javax.swing.JToggleButton();
        missButton = new javax.swing.JToggleButton();
        publicButton = new javax.swing.JToggleButton();
        packageButton = new javax.swing.JToggleButton();
        protectedButton = new javax.swing.JToggleButton();
        privateButton = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        badList = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        customizeButton = new javax.swing.JButton();
        defaultButton = new javax.swing.JButton();
        sourceButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        detailsPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        errorListBox = new javax.swing.JList();
	SplittedPanel myPanel;
        myPanel = new SplittedPanel();
        myPanel.setSplitDragable(true);
        myPanel.setSplitTypeChangeEnabled(true);
        myPanel.setSwapPanesEnabled(true);
        myPanel.setKeepFirstSame(true);
        //myPanel.setContinuousLayout(true);
        myPanel.setSplitPosition(myPanel.FIRST_PREFERRED);
        myPanel.setSplitType(myPanel.HORIZONTAL);
        myPanel.add(classInfoPanel, myPanel.ADD_TOP);
        myPanel.add(javaDocEditor, myPanel.ADD_BOTTOM);
        myPanel.setSplitPosition(dss.getAutocommentSplit());
        add(myPanel);
        setLayout(new java.awt.BorderLayout());
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        setPreferredSize(new java.awt.Dimension(250, 350));
        classInfoPanel.setLayout(new java.awt.GridBagLayout());
        classInfoPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        classInfoPanel.setPreferredSize(new java.awt.Dimension(250, 350));
        classInfoPanel.setName("classInfoPanel");
        filterPanel.setLayout(new java.awt.GridBagLayout());
        okButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.okButton.toolTipText"));
        okButton.setSelected(true);
        okButton.setActionCommand("ALL");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt); }
        });
        filterPanel.add(okButton, new java.awt.GridBagConstraints());
        errButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.errButton.toolTipText"));
        errButton.setSelected(true);
        errButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        errButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errButtonActionPerformed(evt); }
        });
        filterPanel.add(errButton, new java.awt.GridBagConstraints());
        missButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.missButton.toolTipText"));
        missButton.setSelected(true);
        missButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        missButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                missButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        filterPanel.add(missButton, gridBagConstraints);
        publicButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.publicButton.toolTipText"));
        publicButton.setSelected(true);
        publicButton.setActionCommand("PUBLIC");
        publicButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        publicButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicButtonActionPerformed(evt); }
        });
        filterPanel.add(publicButton, new java.awt.GridBagConstraints());
        packageButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.packageButton.toolTipText"));
        packageButton.setActionCommand("PACKAGE");
        packageButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        packageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageButtonActionPerformed(evt); }
        });
        filterPanel.add(packageButton, new java.awt.GridBagConstraints());
        protectedButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.protectedButton.toolTipText"));
        protectedButton.setSelected(true);
        protectedButton.setActionCommand("PROTECTED");
        protectedButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        protectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protectedButtonActionPerformed(evt); }
        });
        filterPanel.add(protectedButton, new java.awt.GridBagConstraints());
        privateButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.privateButton.toolTipText"));
        privateButton.setActionCommand("PRIVATE");
        privateButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        privateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateButtonActionPerformed(evt); }
        });
        filterPanel.add(privateButton, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        filterPanel.add(jPanel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        classInfoPanel.add(filterPanel, gridBagConstraints);
        badList.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.methodListA11yDesc"));
        badList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                badListValueChanged(evt); }
        });
        jScrollPane1.setViewportView(badList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 1.0;
        classInfoPanel.add(jScrollPane1, gridBagConstraints);
        buttonPanel.setLayout(new java.awt.GridBagLayout());
        buttonPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 0, 0)));
        customizeButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.customizeButton.textA11yDesc"));
        customizeButton.setText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.customizeButton.text"));
        customizeButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        customizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customizeButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(customizeButton, gridBagConstraints);
        defaultButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.defaultButton.textA11yDesc"));
        defaultButton.setText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.defaultButton.text"));
        defaultButton.setActionCommand("Default Comment");
        defaultButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        defaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        buttonPanel.add(defaultButton, gridBagConstraints);
        sourceButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.sourceButton.textA11yDesc"));
        sourceButton.setText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.sourceButton.text"));
        sourceButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        sourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        buttonPanel.add(sourceButton, gridBagConstraints);
        refreshButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.refreshButton.textA11yDesc"));
        refreshButton.setText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutoCommentPanel.refreshButton.text"));
        refreshButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        buttonPanel.add(refreshButton, gridBagConstraints);
        helpButton.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutocommentPanel.helpButton.tooltipText"));
        helpButton.setText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("CTL_AutocommentPanel.helpButton.text"));
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        buttonPanel.add(helpButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        buttonPanel.add(jPanel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 0.6;
        classInfoPanel.add(buttonPanel, gridBagConstraints);
        detailsPanel.setLayout(new java.awt.GridBagLayout());
        nameLabel.setText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("AutoCommentPanel.nameLabel.text"));
        nameLabel.setLabelFor(classTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 0);
        detailsPanel.add(nameLabel, gridBagConstraints);
        classTextField.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.nameTextField.textA11yDesc"));
        classTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 6);
        detailsPanel.add(classTextField, gridBagConstraints);
        errorListBox.setToolTipText(org.openide.util.NbBundle.getBundle(AutoCommentPanel.class).getString("ACS_AutoCommentPanel.commentList.textA11yDesc"));
        jScrollPane2.setViewportView(errorListBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        detailsPanel.add(jScrollPane2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.4;
        classInfoPanel.add(detailsPanel, gridBagConstraints);
        add(classInfoPanel, java.awt.BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
	int i = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(i, 0, 0, 0);
        buttonPanel.add(helpButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        buttonPanel.add(jPanel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    }//GEN-END:initComponents
    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        org.openide.TopManager.getDefault().showHelp(
            new org.openide.util.HelpCtx(AutoCommentTopComponent.AUTO_COMMENT_HELP_CTX_KEY));
    }//GEN-LAST:event_helpButtonActionPerformed
  private void badListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_badListValueChanged
      // Check if this module is running in the new mode (1 window) or old mode
      // (2 separate windows).
      if (newMode) {
          int sel = badList.getMinSelectionIndex();
          // This is to prevente an infinite loop when performing an update to
          // the list
          if (updatingBadList) {
              return; }
          // If there was an item selected before changing, check to see if we
          // have to update the current javadoc.
          if (badListLastSelectedIndex != -1) {
              // System.out.println("run modifyJavaDoc for element " + badListLastSelectedIndex);
              //check for no resolved
              if( !(badList.getModel().getElementAt( 0 ) instanceof AutoCommenter.Element) )
                  return;
              modifyJavaDoc((AutoCommenter.Element)badList.getModel().getElementAt( badListLastSelectedIndex ) );
              if (! updatingBadList) {
                  updatingBadList = true;
                  // This is to check if there's any error in the javadoc just entered.
                  DefaultListModel badModel = (DefaultListModel)badList.getModel();
                  AutoCommenter.Element element = (AutoCommenter.Element)badModel.getElementAt( badListLastSelectedIndex );
                  badModel.removeElementAt( badListLastSelectedIndex );
                  element.checkError();
                  if ( AutoCommenter.acceptElement( element, modifierMask, packageButton.isSelected(), errorMask ) ) {
                      badModel.add( badListLastSelectedIndex, element );
                      badList.getSelectionModel().setSelectionInterval( sel, sel );
                       }//                    badList.getSelectionModel().setSelectionInterval( badListLastSelectedIndex, badListLastSelectedIndex );
                  updatingBadList = false; } }
          int size = badList.getModel().getSize();
          if (sel == -1 || size == 0) {
              //showCommentEditor(null);
          } else {
              sel = (sel >= size )?size - 1:sel;
              //check for no resolved
              if( !(badList.getModel().getElementAt( 0 ) instanceof AutoCommenter.Element) )
                  return;
              showCommentEditor( (AutoCommenter.Element)badList.getModel().getElementAt( sel ) ); }
          badListLastSelectedIndex = sel; }
    }//GEN-LAST:event_badListValueChanged
    private void missButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_missButtonActionPerformed
        refreshState();
    }//GEN-LAST:event_missButtonActionPerformed
    private void errButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errButtonActionPerformed
        refreshState();
    }//GEN-LAST:event_errButtonActionPerformed
    private void okButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        refreshState();
    }//GEN-LAST:event_okButtonActionPerformed
    private void refreshButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        updateForClosing();
        autoCommenter.refreshFromSource();
        prepareModel();        
    }//GEN-LAST:event_refreshButtonActionPerformed
    private void privateButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateButtonActionPerformed
        refreshState();
    }//GEN-LAST:event_privateButtonActionPerformed
    private void protectedButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protectedButtonActionPerformed
        refreshState();
    }//GEN-LAST:event_protectedButtonActionPerformed
    private void packageButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageButtonActionPerformed
        refreshState();
    }//GEN-LAST:event_packageButtonActionPerformed
    private void publicButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicButtonActionPerformed
        refreshState();
    }//GEN-LAST:event_publicButtonActionPerformed
    private void customizeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customizeButtonActionPerformed
        int sel = badList.getMinSelectionIndex();
        if ( sel == -1 )
            return;
        showCommentEditor( (AutoCommenter.Element)badList.getModel().getElementAt( sel ) );
        DefaultListModel badModel = (DefaultListModel)badList.getModel();
        AutoCommenter.Element element = (AutoCommenter.Element)badModel.getElementAt( sel );
        element.checkError();
        if (AutoCommenter.acceptElement( element, modifierMask, packageButton.isSelected(), errorMask ) ) {
            badModel.setElementAt(element, badListLastSelectedIndex);
        } else {
            // do not update anything
            badListLastSelectedIndex = -1; 
            badModel.removeElementAt( sel );
            int size = badModel.getSize();
            if (sel < size)
                badList.setSelectedIndex(sel);
            else 
                badList.setSelectedIndex(size - 1); }
        //errorListBox.setModel( element.getErrorList() );
        //badList.repaint();
        //errorListBox.repaint();
    }//GEN-LAST:event_customizeButtonActionPerformed
    private void defaultButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultButtonActionPerformed
        int sel = badList.getMinSelectionIndex();
        if ( sel == -1 )
            return;
        //((AutoCommenter.Element)badList.getModel().getElementAt( sel )).generateDefaultComment();
        AutoCommenter.Element element = (AutoCommenter.Element)badList.getModel().getElementAt( sel );
        updateForClosing(); //update before correct
        try {
            element.autoCorrect();
            customizeButtonActionPerformed(evt);
        } catch (org.openide.src.SourceException e) {
            TopManager.getDefault().notify(new NotifyDescriptor.Exception(e)); }
    }//GEN-LAST:event_defaultButtonActionPerformed
    private void sourceButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceButtonActionPerformed
        int sel = badList.getMinSelectionIndex();
        if ( sel == -1 )
            return;
        ((AutoCommenter.Element)badList.getModel().getElementAt( sel )).viewSource();
    }//GEN-LAST:event_sourceButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton defaultButton;
    private javax.swing.JToggleButton packageButton;
    private javax.swing.JPanel classInfoPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton sourceButton;
    private javax.swing.JButton customizeButton;
    private javax.swing.JToggleButton privateButton;
    private javax.swing.JToggleButton okButton;
    private javax.swing.JToggleButton errButton;
    private javax.swing.JToggleButton missButton;
    private javax.swing.JList badList;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToggleButton protectedButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField classTextField;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton helpButton;
    private javax.swing.JList errorListBox;
    private javax.swing.JToggleButton publicButton;
    // End of variables declaration//GEN-END:variables
    private org.openide.awt.SplittedPanel splittedPanel;
    public void updateForClosing() {
        javaDocEditor.updateForClosing();
        int sel = badList.getMinSelectionIndex();
        if (sel != -1) {
            if( !(badList.getModel().getElementAt( 0 ) instanceof AutoCommenter.Element) )
                  return;
            modifyJavaDoc((AutoCommenter.Element)badList.getModel().getElementAt( sel ) ); } }
    private void showCommentEditor( final AutoCommenter.Element el ) {
        if (newMode) {
            javaDocEditor.setElement(el.getSrcElement());
            javaDocEditor.setJavaDoc(el.getJavaDoc());
        } else {
            final JavaDocEditorPanel editorPanel = new JavaDocEditorPanel( el.getJavaDoc(), el.getSrcElement());
            DialogDescriptor dd = new DialogDescriptor( editorPanel,
            MessageFormat.format( NbBundle.getBundle(AutoCommentPanel.class).getString( "CTL_TITLE_JavaDocComment" ),   //NOI18N
            new Object[] {  el.getName()  } ), //Title
            true,                                                 // Modal
            NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
            NotifyDescriptor.OK_OPTION,                           // Default
            DialogDescriptor.BOTTOM_ALIGN,                        // Align
            null,                                                 // Help
            null );
            java.awt.Dialog dialog = TopManager.getDefault().createDialog( dd );
            dialog.show ();
            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
                modifyJavaDoc(el); } } }
    private void modifyJavaDoc(final AutoCommenter.Element el) {
        if (! javaDocEditor.isDirty()) {
            return; }
        final org.openide.src.SourceException[] exc = { null };
        try {
            el.modifyJavaDoc(new Runnable() {
                public void run() {
                    try {
                        el.getJavaDoc().setRawText( javaDocEditor.getRawText() );
                        javaDocEditor.setDirty(false);
                    } catch (org.openide.src.SourceException e) {
                        exc[0] = e; } }
            }); }
        catch (org.openide.src.SourceException ex ) {
            exc[0] = ex; }
        if (exc[0] != null) {
            org.openide.TopManager.getDefault().notify(new NotifyDescriptor.Exception(exc[0])); } }
    private void elementSelection( ListSelectionEvent evt ) {
        int sel = badList.getMinSelectionIndex();
        ListModel badListModel = badList.getModel();
        if ( sel < 0 ) {
            errorListBox.setModel(EMPTY_MODEL);
            defaultButton.setEnabled( false );
            customizeButton.setEnabled( false );
            sourceButton.setEnabled( false );
            classTextField.setText( "" ); // NOI18N
            javaDocEditor.clear();
            javaDocEditor.setEnabled(false); }
        else {
            //check for no resolved
            if( !(badList.getModel().getElementAt( 0 ) instanceof AutoCommenter.Element) )
                  return;
            AutoCommenter.Element element = (AutoCommenter.Element)badListModel.getElementAt( sel );            
            try {
                classTextField.setText( element.getSrcElement().getDeclaringClass().getName().getFullName() ); }
            catch ( NullPointerException e ) {
                classTextField.setText( element.getSrcElement().getName().getFullName() ); }
            customizeButton.setEnabled( true );
            defaultButton.setEnabled( element.isCorrectable() );
            sourceButton.setEnabled( true );
            javaDocEditor.setEnabled( true );
            errorListBox.setModel( element.getErrorList() ); } }
    void setAutoCommenter( AutoCommenter autoCommenter ) {
        badListLastSelectedIndex = -1;
        badList.setModel( WAIT_MODEL );
        this.autoCommenter = autoCommenter;
        try{
            AutoCommenter.AutoCommentChangeListener acl = new AutoCommenter.AutoCommentChangeListener(){
                public void listChanged(){
                    refreshAfterChange();             }
            };
            autoCommenter.addAutoCommentChangeListener(
                            (AutoCommenter.AutoCommentChangeListener)WeakListener.create(AutoCommenter.AutoCommentChangeListener.class, acl, autoCommenter)); }
        catch(java.util.TooManyListenersException tooEx){
            tooEx.printStackTrace(); }
        RequestProcessor.postRequest( new Refresher() ); }
    private void resolveMask() {
        modifierMask = 0;
        errorMask = 0;
        if ( publicButton.isSelected() )
            modifierMask |= Modifier.PUBLIC;
        if ( protectedButton.isSelected() )
            modifierMask |= Modifier.PROTECTED;
        if ( privateButton.isSelected() )
            modifierMask |= Modifier.PRIVATE;
        if ( okButton.isSelected() )
            errorMask |= AutoCommenter.JDC_OK;
        if ( errButton.isSelected() )
            errorMask |= AutoCommenter.JDC_ERROR;
        if ( missButton.isSelected() )
            errorMask |= AutoCommenter.JDC_MISSING;
        DocumentationSettings ds = ((DocumentationSettings)SharedClassObject.findObject(DocumentationSettings.class, true));
        ds.setAutocommentModifierMask( modifierMask );
        ds.setAutocommentPackage( packageButton.isSelected() );
        ds.setAutocommentErrorMask( errorMask ); }
    private void refreshState(){
        updateForClosing();
        autoCommenter.refreshFromSource();
        resolveMask();
        prepareModel(); }
    private void refreshAfterChange(){
        autoCommenter.refreshFromSource();
        prepareModel(); }
    private void prepareModel() {
        updatingBadList = true;
        badList.getSelectionModel().removeListSelectionListener( this );
        badList.setModel( autoCommenter.prepareListModel( modifierMask, packageButton.isSelected(), errorMask ) );
        badList.getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_SELECTION );
        badList.getSelectionModel().addListSelectionListener( this );
        errorListBox.setModel( EMPTY_MODEL );
        badList.setSelectedIndex(-1);
        badListLastSelectedIndex = -1;
        updatingBadList = false;
        javaDocEditor.enableButtons( false ); }
    // Impelmentation of externalizable
    /**
     */
    public void resolveButtonState() {
        DocumentationSettings ds = ((DocumentationSettings)SharedClassObject.findObject(DocumentationSettings.class, true));
        final int modifierMask = ds.getAutocommentModifierMask();
        final boolean pckg = ds.getAutocommentPackage();
        final int errorMask = ds.getAutocommentErrorMask();
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                publicButton.setSelected( (modifierMask & Modifier.PUBLIC) != 0 );
                protectedButton.setSelected( (modifierMask & Modifier.PROTECTED) != 0 );
                privateButton.setSelected( (modifierMask & Modifier.PRIVATE) != 0 );
                packageButton.setSelected( pckg );
                okButton.setSelected( ( errorMask & AutoCommenter.JDC_OK ) != 0 );
                errButton.setSelected( ( errorMask & AutoCommenter.JDC_ERROR ) != 0 );
                missButton.setSelected( ( errorMask & AutoCommenter.JDC_MISSING) != 0 ); }
        } ); }
    // Implementation of ListSelectionListener
    /**
     */
    public void valueChanged(final ListSelectionEvent evt) {
        elementSelection( evt ); }
    // InnerClass refreshes from source
    class Refresher implements Runnable {
        /**
         */
        public void run () {
            autoCommenter.refreshFromSource();
            resolveMask();
            javax.swing.SwingUtilities.invokeLater( new Runnable() {
                public void run () {
                    prepareModel(); }
            } ); } } }
