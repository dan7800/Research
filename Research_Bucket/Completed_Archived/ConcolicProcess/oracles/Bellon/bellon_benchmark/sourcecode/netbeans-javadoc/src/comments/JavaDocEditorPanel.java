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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.TitledBorder;
import org.openide.src.MemberElement;
import org.openide.src.JavaDoc;
import org.openide.src.JavaDocTag;
import org.openide.src.SourceException;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
/**
 * @author phrebejk
 * @version
 */
public class JavaDocEditorPanel extends javax.swing.JPanel
    implements EnhancedCustomPropertyEditor {
    private static final String JAVADOC_PROPERTY_HELP = "javadoc.doc.window";   //NOI18N
    private JavaDoc javaDoc;
    private DefaultListModel listModel;
    private int lastSelection = -1;
    private EmptyTagPanel emptyTagPanel;
    private StandardTagPanel standardTagPanel;
    private SeeTagPanel seeTagPanel;
    private ParamTagPanel paramTagPanel;
    private ThrowsTagPanel throwsTagPanel;
    private SerialFieldTagPanel serialFieldTagPanel;
    private NewTagDialog newTagDialog;
    private MemberElement element;
    private MnemonicsDistributor mnemonicsDistributor;
    static final long serialVersionUID =7005703844831686911L;
    /** Holds value of property dirty. */
    private boolean dirty;
    private boolean updatingTagList;
    /** Creates new form JavaDocEditorPanel
     */
    public JavaDocEditorPanel() {
        initComponents ();
        // Buttons mnemonics
        newButton.setMnemonic( org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.newButton_Mnemonic").charAt(0) );    //NOI18N
        deleteButton.setMnemonic( org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.deleteButton_Mnemonic").charAt(0) );  //NOI18N
        moveUpButton.setMnemonic( org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.moveUpButton_Mnemonic").charAt(0) );  //NOI18N
        moveDownButton.setMnemonic( org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.moveDownButton_Mnemonic").charAt(0) );  //NOI18N
        boldButton.setMnemonic( 'B' );
        italicButton.setMnemonic( 'I' );
        underlineButton.setMnemonic( 'U' );
        codeButton.setMnemonic( 'C' );
        preButton.setMnemonic( 'P' );
        linkButton.setMnemonic( 'L' );
        enableButtons( false );
        mnemonicsDistributor = new MnemonicsDistributor();
        commentTextArea.setContentType( "text/html"); // NOI18N
        commentTextArea.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate( DocumentEvent evt) {
                    dirty = true; }
                public void insertUpdate( DocumentEvent evt) {
                    dirty = true; }
                public void removeUpdate( DocumentEvent evt) {
                    dirty = true; }
            } );
        mnemonicsDistributor.registerComponent( commentTextArea );
        // Make the list to select only one line and listen to selections
        tagList.setVisibleRowCount(4);
        tagList.getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_SELECTION );
        tagList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged( ListSelectionEvent evt ) {
                    tagSelection( evt ); }
            } );
        // i18n
        textPanel.setBorder (new javax.swing.border.TitledBorder(
                                 new javax.swing.border.EtchedBorder(),
                                 org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.textPanel.title")));   //NOI18N
        tagPanel.setBorder (new javax.swing.border.TitledBorder(
                                new javax.swing.border.EtchedBorder(),
                                org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.tagPanel.title"))); //NOI18N
        // Add panels for different tag types
        emptyTagPanel = new EmptyTagPanel( this );
        tagParamPanel.add( emptyTagPanel, emptyTagPanel.getCardName()  );
        standardTagPanel = new StandardTagPanel( this );
        tagParamPanel.add( standardTagPanel, standardTagPanel.getCardName() );
        seeTagPanel = new SeeTagPanel( this );
        tagParamPanel.add( seeTagPanel, seeTagPanel.getCardName() );
        paramTagPanel = new ParamTagPanel( this );
        tagParamPanel.add( paramTagPanel, paramTagPanel.getCardName() );
        throwsTagPanel = new ThrowsTagPanel( this );
        tagParamPanel.add( throwsTagPanel, throwsTagPanel.getCardName() );
        serialFieldTagPanel = new SerialFieldTagPanel( this );
        tagParamPanel.add( serialFieldTagPanel, serialFieldTagPanel.getCardName() );
        HelpCtx.setHelpIDString (this, JAVADOC_PROPERTY_HELP);
        initAccessibility(); }
    /**
     */
    public JavaDocEditorPanel( JavaDoc javaDoc, MemberElement element ) {
        this();
        setElement(element);
        setJavaDoc(javaDoc); }
    /**
     */
    public void clear() {
        commentTextArea.setText("");    //NOI18N
        newButton.setEnabled(false);
        setTagListModel(); }
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        tagList.setEnabled(enable);
        commentTextArea.setEnabled(enable);
        newButton.setEnabled(enable); }
    public void updateForClosing() {
        commitTagChange(); }
    private void setTagListModel() {
        lastSelection = -1;
        listModel = new DefaultListModel();
        tagList.setModel( listModel );
        deleteButton.setEnabled(false);
        moveUpButton.setEnabled(false);
        moveDownButton.setEnabled(false);
        listModel.addListDataListener(
            new ListDataListener() {
                public void intervalAdded(ListDataEvent e) {
                    dirty = true; }
                public void intervalRemoved(ListDataEvent e) {
                    dirty = true; }
                public void contentsChanged(ListDataEvent e) {
                    dirty = true; }
            } ); }
    public void setJavaDoc(JavaDoc javaDoc) {
        this.javaDoc = javaDoc;
        if ( javaDoc != null ) {
            this.javaDoc = javaDoc;
            commentTextArea.setText( javaDoc.getText()  );
            commentTextArea.setCaretPosition(0);
            //commentTextArea.setText( removeWhiteSpaces( javaDoc.getText() ) );
        } else {
            commentTextArea.setText( null ); }
        // Put the tags into listbox
        setTagListModel();
        if ( javaDoc != null ) {
            JavaDocTag tags[] = javaDoc.getTags();
            for( int i = 0; i < tags.length; i++ ) {
                listModel.addElement( tags[i] ); }
            if ( listModel.getSize() < 0 ) {
                tagList.setSelectedIndex( 0 ); } }
        // Add panels for different tag types
        standardTagPanel.setElement( element );
        paramTagPanel.setElement( element );
        throwsTagPanel.setElement ( element );
        setDirty(false);
        revalidate();
        repaint(); }
    public JavaDoc getJavaDoc() {
        return this.javaDoc; }
    public void setElement(MemberElement element) {
        this.element = element; }
    public MemberElement getElement() {
        return this.element; }
    public java.awt.Dimension getPreferredSize() {
        return new Dimension( 600, 520 ); }
    private void initAccessibility() {
        commentTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.commentTextAreaA11yName"));  // NOI18N
        tagList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.tagListA11yName"));   }// NOI18N
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        textPanel = new javax.swing.JPanel();
        commentScrollPane = new javax.swing.JScrollPane();
        commentTextArea = new javax.swing.JEditorPane();
        tagPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        tagScrollPane = new javax.swing.JScrollPane();
        tagList = new javax.swing.JList();
        tagParamPanel = new javax.swing.JPanel();
        htmlToolBar = new javax.swing.JPanel();
        boldButton = new javax.swing.JButton();
        italicButton = new javax.swing.JButton();
        underlineButton = new javax.swing.JButton();
        codeButton = new javax.swing.JButton();
        preButton = new javax.swing.JButton();
        linkButton = new javax.swing.JButton();
        setLayout(new java.awt.GridBagLayout());
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 6, 5, 5)));
        textPanel.setLayout(new java.awt.GridBagLayout());
        commentScrollPane.setMinimumSize(new java.awt.Dimension(22, 60));
        commentTextArea.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.commentTextAreaA11yDesc"));
        commentTextArea.setContentType("text/html");
        commentTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                commentTextAreaFocusGained(evt); }
            public void focusLost(java.awt.event.FocusEvent evt) {
                commentTextAreaFocusLost(evt); }
        });
        commentScrollPane.setViewportView(commentTextArea);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        textPanel.add(commentScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(textPanel, gridBagConstraints);
        tagPanel.setLayout(new java.awt.GridBagLayout());
        newButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.newButton.textA11yDesc"));
        newButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.newButton.text"));
        newButton.setMargin(new java.awt.Insets(2, 12, 2, 12));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTagButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        tagPanel.add(newButton, gridBagConstraints);
        deleteButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.deleteButton.textA11yDesc"));
        deleteButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.deleteButton.text"));
        deleteButton.setMargin(new java.awt.Insets(2, 12, 2, 12));
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delTagButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        tagPanel.add(deleteButton, gridBagConstraints);
        jSeparator2.setMinimumSize(new java.awt.Dimension(1, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 5, 7);
        tagPanel.add(jSeparator2, gridBagConstraints);
        moveUpButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.moveUpButton.textA11yDesc"));
        moveUpButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.moveUpButton.text"));
        moveUpButton.setActionCommand("UP");
        moveUpButton.setMargin(new java.awt.Insets(2, 12, 2, 12));
        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTagButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        tagPanel.add(moveUpButton, gridBagConstraints);
        moveDownButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.moveDownButton.textA11yDesc"));
        moveDownButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.moveDownButton.text"));
        moveDownButton.setActionCommand("DOWN");
        moveDownButton.setMargin(new java.awt.Insets(2, 12, 2, 12));
        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTagButtonActionPerformed(evt); }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 5);
        tagPanel.add(moveDownButton, gridBagConstraints);
        tagList.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.tagListA11yDesc"));
        tagList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                tagListValueChanged(evt); }
        });
        tagScrollPane.setViewportView(tagList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 11, 11);
        tagPanel.add(tagScrollPane, gridBagConstraints);
        tagParamPanel.setLayout(new java.awt.CardLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        tagPanel.add(tagParamPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(tagPanel, gridBagConstraints);
        htmlToolBar.setLayout(new java.awt.GridLayout(1, 6));
        boldButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.boldButton.textA11yDesc"));
        boldButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.boldButton.text"));
        boldButton.setMaximumSize(new java.awt.Dimension(59, 27));
        boldButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        boldButton.setActionCommand("B");
        boldButton.setMinimumSize(new java.awt.Dimension(32, 27));
        boldButton.setRequestFocusEnabled(false);
        boldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt); }
        });
        htmlToolBar.add(boldButton);
        italicButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.italicButton.textA11yDesc"));
        italicButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.italicButton.text"));
        italicButton.setMaximumSize(new java.awt.Dimension(57, 27));
        italicButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        italicButton.setActionCommand("I");
        italicButton.setMinimumSize(new java.awt.Dimension(32, 27));
        italicButton.setRequestFocusEnabled(false);
        italicButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt); }
        });
        htmlToolBar.add(italicButton);
        underlineButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.underlineButton.textA11yDesc"));
        underlineButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.underlineButton.text"));
        underlineButton.setMaximumSize(new java.awt.Dimension(61, 27));
        underlineButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        underlineButton.setActionCommand("U");
        underlineButton.setMinimumSize(new java.awt.Dimension(32, 27));
        underlineButton.setRequestFocusEnabled(false);
        underlineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt); }
        });
        htmlToolBar.add(underlineButton);
        codeButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.codeButton.textA11yDesc"));
        codeButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.codeButton.text"));
        codeButton.setMaximumSize(new java.awt.Dimension(83, 27));
        codeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        codeButton.setActionCommand("CODE");
        codeButton.setMinimumSize(new java.awt.Dimension(32, 27));
        codeButton.setRequestFocusEnabled(false);
        codeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt); }
        });
        htmlToolBar.add(codeButton);
        preButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.preButton.textA11yDesc"));
        preButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.preButton.text"));
        preButton.setMaximumSize(new java.awt.Dimension(73, 27));
        preButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        preButton.setActionCommand("PRE");
        preButton.setMinimumSize(new java.awt.Dimension(32, 27));
        preButton.setRequestFocusEnabled(false);
        preButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt); }
        });
        htmlToolBar.add(preButton);
        linkButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_JavaDocEditorPanel.linkButton.textA11yDesc"));
        linkButton.setText(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.linkButton.text"));
        linkButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        linkButton.setActionCommand("link");
        linkButton.setMinimumSize(new java.awt.Dimension(32, 27));
        linkButton.setRequestFocusEnabled(false);
        linkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt); }
        });
        htmlToolBar.add(linkButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(htmlToolBar, gridBagConstraints);
    }//GEN-END:initComponents
    private void tagListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_tagListValueChanged
        if (updatingTagList) {
            return; }
        deleteButton.setEnabled(true);
        moveUpButton.setEnabled(true);
        moveDownButton.setEnabled(true);
       // JHK 9/29/2000 - cause focus to go to entry fields when new tag created
        JavaDocTag tag = (JavaDocTag)tagList.getSelectedValue();
        getPanelForTag( tag ).grabFirstFocus();
    }//GEN-LAST:event_tagListValueChanged
    private void commentTextAreaFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextAreaFocusLost
        enableButtons( false );
    }//GEN-LAST:event_commentTextAreaFocusLost
    private void commentTextAreaFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextAreaFocusGained
        enableButtons( true );
    }//GEN-LAST:event_commentTextAreaFocusGained
    private void newTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTagButtonActionPerformed
        //if ( newTagDialog == null )   //what is that ? not possible, there can be different types of element
        newTagDialog = new NewTagDialog( new java.awt.Frame (), true, element);
        newTagDialog.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_NewTagDialogA11yName"));  // NOI18N
        newTagDialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("ACS_NewTagDialogA11yDesc"));  // NOI18N
        newTagDialog.show();
        JavaDocTag tag = newTagDialog.getResult();
        if ( tag != null ) {
            listModel.addElement( tag );
            tagList.ensureIndexIsVisible( listModel.getSize() );
            tagList.setSelectedIndex( listModel.getSize() - 1 );
            //tagList.requestFocus();
            /*
            tagScrollPane.revalidate();
            getPanelForTag( tag ).grabFirstFocus();
            */ }
    }//GEN-LAST:event_newTagButtonActionPerformed
    /** Deletes the actual row */
    private void delTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delTagButtonActionPerformed
        int sel = tagList.getMinSelectionIndex();
        if ( sel != -1 )
            listModel.removeElementAt( sel );
        if ( listModel.getSize() > 0 )
            tagList.setSelectedIndex( sel == listModel.getSize() ? sel - 1 : sel );
        else {
            CardLayout layout = (CardLayout)tagParamPanel.getLayout();
            layout.show( tagParamPanel, emptyTagPanel.getCardName() ); }
    }//GEN-LAST:event_delTagButtonActionPerformed
    private void chgTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chgTagButtonActionPerformed
        commitTagChange();
    }//GEN-LAST:event_chgTagButtonActionPerformed
    private void moveTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTagButtonActionPerformed
        // Add your handling code here:
        if ( evt.getActionCommand().equals( "UP" ) ) { // NOI18N
            int selIndex = tagList.getMinSelectionIndex();
            if ( selIndex > 0 ) {
                Object tag = listModel.get( selIndex );
                listModel.removeElementAt( selIndex );
                listModel.insertElementAt( tag, selIndex - 1 );
                tagList.setSelectedIndex( selIndex - 1 ); } }
        else if ( evt.getActionCommand().equals( "DOWN" ) ) { // NOI18N
            int selIndex = tagList.getMinSelectionIndex();
            if ( selIndex < listModel.getSize() - 1 ) {
                Object tag = listModel.get( selIndex );
                listModel.removeElementAt( selIndex );
                listModel.insertElementAt( tag, selIndex + 1 );
                tagList.setSelectedIndex( selIndex + 1 ); } }
    }//GEN-LAST:event_moveTagButtonActionPerformed
    private void formatButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatButtonActionPerformed
        String begTag;
        String endTag;
        String command = evt.getActionCommand();
        if ( command.equals( "link" ) ) { // NOI18N
            begTag = "{@link "; // NOI18N
            endTag = "}";  }// NOI18N
        else {
            begTag = "<" + command + ">"; // NOI18N
            endTag = "</" + command + ">";  }// NOI18N
        if ( commentTextArea.hasFocus() ) {
            int caretPosition = commentTextArea.getCaretPosition();
            /*
            StringBuffer sb = new StringBuffer( commentTextArea.getText() );
            sb.insert( commentTextArea.getSelectionStart(), begTag );
            sb.insert( commentTextArea.getSelectionEnd(), endTag  );
            commentTextArea.setText( sb.toString() ); 
            */
            try {
                commentTextArea.getDocument().insertString( commentTextArea.getSelectionStart(), begTag, null );
                commentTextArea.getDocument().insertString( commentTextArea.getSelectionEnd(), endTag, null );
                commentTextArea.setCaretPosition( caretPosition + 2 + evt.getActionCommand().length() ); }
            catch ( javax.swing.text.BadLocationException e ) {
                 } }//System.out.println(e );
        else {
            JavaDocTag tag = (JavaDocTag)listModel.get( tagList.getMinSelectionIndex() ) ;
            TagPanel tagPanel = getPanelForTag( tag );
            tagPanel.handleFormatButton( begTag, endTag ); }
    }//GEN-LAST:event_formatButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel tagPanel;
    private javax.swing.JButton codeButton;
    private javax.swing.JButton linkButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JEditorPane commentTextArea;
    private javax.swing.JPanel textPanel;
    private javax.swing.JPanel htmlToolBar;
    private javax.swing.JButton italicButton;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton newButton;
    private javax.swing.JScrollPane commentScrollPane;
    private javax.swing.JScrollPane tagScrollPane;
    private javax.swing.JButton deleteButton;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton boldButton;
    private javax.swing.JList tagList;
    private javax.swing.JButton preButton;
    private javax.swing.JPanel tagParamPanel;
    private javax.swing.JButton underlineButton;
    // End of variables declaration//GEN-END:variables
    /** gets the text of comment */
    String getRawText() {
        StringBuffer sb = new StringBuffer( 1000 );
        try {
            sb.append( commentTextArea.getDocument().getText( 0, commentTextArea.getDocument().getLength() ) ); }
        catch ( javax.swing.text.BadLocationException ex ) {
            System.err.println( ex ); }
        sb.append( '\n' );
        for ( int i = 0; i < listModel.getSize(); i++ ) {
            JavaDocTag tag = (( JavaDocTag )listModel.get( i ));
            sb.append( " " + tag.name() + " " + tag.text() ); // NOI18N
            sb.append( '\n' ); }
        return sb.toString(); }
    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException {
        try {
            javaDoc.setRawText( getRawText() ); }
        catch ( SourceException ex ) {
            throw new IllegalStateException(); }
        return javaDoc; }
    /** Called when new tag is selected */
    private void tagSelection( ListSelectionEvent evt ) {
        TagPanel tagPanel;
        int sel = tagList.getMinSelectionIndex();
        if (lastSelection >= 0 && lastSelection != sel && lastSelection<listModel.getSize()) {
            JavaDocTag tag = (JavaDocTag)listModel.get( lastSelection );
            tagPanel = getPanelForTag( tag );
            JavaDocTag newTag = tagPanel.getTag( tag.name() );
            listModel.setElementAt(newTag, lastSelection); }
        lastSelection = sel;
        if ( sel < 0 ) {
            tagPanel = emptyTagPanel;
            enableButtons( false ); }
        else {
            //JavaDocTag tag = (JavaDocTag)listModel.get( tagList.getMinSelectionIndex() ) ;
                  JavaDocTag tag = (JavaDocTag)listModel.get( sel ) ;
            tagPanel = getPanelForTag( tag );
            tagPanel.setData( tag ); }
        CardLayout layout = (CardLayout)tagParamPanel.getLayout();
        layout.show( tagParamPanel, tagPanel.getCardName() );
             // JHK 9/29/2000 - cause focus to go to entry fields when tag selection is changed
        // must check attributes of event since valueChanged is called so often even when the value is not really changed
        if ( tagPanel != emptyTagPanel && evt.getFirstIndex() != evt.getLastIndex() && !evt.getValueIsAdjusting() )
                tagPanel.grabFirstFocus();
         }//System.out.println("In JavaDocEditorPanel.tagSelection: " + evt.toString() );
    TagPanel getPanelForTag( JavaDocTag tag ) {
        if ( tag instanceof JavaDocTag.Param )
            return paramTagPanel;
        else if ( tag instanceof JavaDocTag.Throws )
            return throwsTagPanel;
        else if ( tag instanceof JavaDocTag.SerialField )
            return serialFieldTagPanel;
        else if ( tag instanceof JavaDocTag.See )
            return seeTagPanel;
        else
            return standardTagPanel; }
    /** Removes the whitespaces after new line characters */
    private String removeWhiteSpaces( String text ) {
        StringBuffer sb = new StringBuffer( text );
        StringBuffer newSb = new StringBuffer( text.length() );
        boolean inWhite = false;
        for( int i = 0; i < sb.length(); i++ ) {
            if ( inWhite ) {
                if ( sb.charAt(i) == '\n' || !Character.isWhitespace( sb.charAt( i ) ) ) {
                    //newSb.append( sb.charAt( i ) );
                    inWhite = false; }
                else {
                    continue; } }
            newSb.append( sb.charAt( i ) );
            if ( sb.charAt( i ) == '\n' ) {
                inWhite = true; } }
        return newSb.toString(); }
    /** Changes the tag in the tag list */
    void commitTagChange() {
        updatingTagList = true;
        TagPanel tagPanel;
        int sel = tagList.getMinSelectionIndex();
        if ( sel >= 0 ) {
            JavaDocTag tag = (JavaDocTag)listModel.get( sel );
            tagPanel = getPanelForTag( tag );
            JavaDocTag newTag = tagPanel.getTag( tag.name() );
            listModel.setElementAt( newTag, sel );
            dirty = true; }
        updatingTagList = false; }
    void enableButtons( boolean enable ) {        
        boldButton.setEnabled( enable );
        italicButton.setEnabled( enable );
        underlineButton.setEnabled( enable );
        codeButton.setEnabled( enable );
        preButton.setEnabled( enable );
        linkButton.setEnabled( enable ); }
    void registerComponent( java.awt.Component component) {
        mnemonicsDistributor.registerComponent( component ); }
    /** Getter for property dirty.
     * @return Value of property dirty.
     */
    public boolean isDirty() {
        return dirty; }
    /** Setter for property dirty.
     * @param dirty Value of property dirty.
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty; }
    /** This innerclass serves as workaround for handling alt key mnemonics
     */
    class MnemonicsDistributor extends java.awt.event.KeyAdapter {
        MnemonicsDistributor() { }
        public void keyPressed( java.awt.event.KeyEvent e ) {
            javax.swing.KeyStroke ks = javax.swing.KeyStroke.getKeyStrokeForEvent( e );
            if ( ( ks.getModifiers() & java.awt.event.InputEvent.ALT_MASK ) != 0 ) {
                switch ( ks.getKeyCode() ) {
                case  KeyEvent.VK_B:
                    boldButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_I:
                    italicButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_U:
                    underlineButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_C:
                    codeButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_P:
                    preButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_L:
                    linkButton.doClick();
                    e.consume();
                    break; } } }
        void registerComponent( java.awt.Component component ) {
            component.addKeyListener(this); } } }
