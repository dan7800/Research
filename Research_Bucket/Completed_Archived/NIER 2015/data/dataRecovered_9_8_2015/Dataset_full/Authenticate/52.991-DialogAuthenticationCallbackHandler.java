/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/jaas/DialogAuthenticationCallbackHandler.java,v 1.3 2003/10/26 13:19:30 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry.jaas;

import org.apache.commons.logging.Log;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Raghu V
 * @version 1.0
 *
 * This class is a CallbackHandler implementation that is suitable for logging into a keystore.
 * The handle(Callback[]) method has been customized such that it can only handle the login method
 * of LoginContext.
 * Although the class has been designed such that it can interactively retrieve the keystore password
 * from the user, these lines have been commented. The current implementation is such that this
 * password is read from the property file(s).
 */
public class DialogAuthenticationCallbackHandler implements CallbackHandler {
    /* The ResourceBundle for Sun's Auth package. */
    private static final ResourceBundle authResBundle = ResourceBundle.getBundle(
            "sun.security.util.AuthResources");

    /* The size of the Dialog to be displayed. This may have to be increased
    later on when we decide to get the keystore password interactively rather
    than from the properties file */
    private static final Dimension DIALOG_SIZE = new Dimension(400, 150);
    private static final Dimension PANEL_SIZE = new Dimension(375, 25);
    private static final int OK = 1;
    private static final int CANCEL = 0;
    private final Log log;

    /* The dialog box shown to the user prompting for security details */
    private JDialog confirmationDialog;

    /* The frame to which this Dialog is bound to */
    private Frame ownerFrame;

    /* Panel and components to prompt the user to enter keystore alias  */
    private JPanel pnlAlias = new JPanel(null);
    private JLabel lblAlias = new JLabel("Keystore alias:");
    private JTextField txtAlias = new JTextField();

    /* Panel and components to prompt the user for keystore password */
    private JPanel pnlStorepass = new JPanel(null);
    private JLabel lblStorepass = new JLabel("Keystore password:");
    private JPasswordField txtStorepass = new JPasswordField(8);

    /*Panel and components to prompt for private key password */
    private JPanel pnlKeypass = new JPanel(null);
    private JLabel lblKeypass = new JLabel("Private key password(optional):");
    private JPasswordField txtKeypass = new JPasswordField(8);

    /*Panel and components for the buttons */
    private JPanel pnlButtons = new JPanel(null);
    private JButton btnOk = new JButton("OK");
    private JButton btnCancel = new JButton("Cancel");

    /* boolean flag that indicates whether or not the OK button was pressed */
    private boolean bOkPressed = false;

    /**
     * One arg constructor that accepts a <code>java.awt.Frame</code>
     * object. The login dialog shown to the user will be modal to this
     * <code>java.awt.Frame</code> object.
     * @param frame Instance of <code>java.awt.Frame</code> object to
     * which the login dialog is modal
     * @param log Logger to be used
     */
    public DialogAuthenticationCallbackHandler(Frame frame, Log l) {
        ownerFrame = frame;
        this.log = l;
        initComponents();
    }

    private void initComponents() {
        int curX = 5;
        int curY = 15;
        int lblWidth = 200;
        int txtWidth = 165;
        int buttonWidth = 75;
        int compHeight = 25;
        int padding = 5;

        // Initialize the confirmation dialog
        confirmationDialog = new JDialog(ownerFrame, "Keystore login", true);
        confirmationDialog.getContentPane().setLayout(null);
        confirmationDialog.setLocation(20, 20);
        confirmationDialog.setSize(DIALOG_SIZE);

        //Initialize the components required to get the alias from the user
        pnlAlias.setSize(PANEL_SIZE);
        pnlAlias.setLocation(curX, curY);
        lblAlias.setBounds(0, 0, lblWidth, compHeight);
        txtAlias.setBounds(lblWidth + padding, 0, txtWidth, compHeight);
        pnlAlias.add(lblAlias);
        pnlAlias.add(txtAlias);
        confirmationDialog.getContentPane().add(pnlAlias);

        /* This Entire block needs to be commented for now. THe current plan is to
        hide the Keystore password implementation from the user and read it directly from
        the properties file


        //Initialize the components required to get the keystore password from the user
        curY = curY + PANEL_SIZE.height + padding ;
        txtStorepass.setEchoChar('*');
        pnlStorepass.setSize(PANEL_SIZE) ;
        pnlStorepass.setLocation(curX, curY) ;
        lblStorepass.setBounds(0,0, lblWidth, compHeight);
        txtStorepass.setBounds(lblWidth + padding, 0, txtWidth, compHeight) ;
        pnlStorepass.add(lblStorepass) ;
        pnlStorepass.add(txtStorepass) ;
        confirmationDialog.getContentPane().add(pnlStorepass) ;
        */
        //Initialize the components required to get the key password from the user
        curY = curY + PANEL_SIZE.height + (padding * 2);
        txtKeypass.setEchoChar('*');
        pnlKeypass.setSize(PANEL_SIZE);
        pnlKeypass.setLocation(curX, curY);
        lblKeypass.setBounds(0, 0, lblWidth, compHeight);
        txtKeypass.setBounds(lblWidth + padding, 0, txtWidth, compHeight);
        pnlKeypass.add(lblKeypass);
        pnlKeypass.add(txtKeypass);
        confirmationDialog.getContentPane().add(pnlKeypass);

        //Initialize the components required to get the bottom buttons.
        btnOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bOkPressed = true;
                    confirmationDialog.setVisible(false);
                }
            });
        btnCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bOkPressed = false;
                    confirmationDialog.setVisible(false);
                }
            });
        curY = curY + PANEL_SIZE.height + (padding * 2);
        pnlButtons.setSize(PANEL_SIZE);
        pnlButtons.setLocation(curX, curY);

        int buttonsBoundingWidth = (buttonWidth * 2) + (padding * 2);
        curX = (((PANEL_SIZE.width - buttonsBoundingWidth) / 2) > 0)
            ? ((PANEL_SIZE.width - buttonsBoundingWidth) / 2) : curX;
        btnOk.setBounds(curX, 0, buttonWidth, compHeight);
        curX = curX + buttonWidth + (padding * 2);
        btnCancel.setBounds(curX, 0, buttonWidth, compHeight);
        curX = pnlButtons.getLocation().x;
        pnlButtons.add(btnOk);
        pnlButtons.add(btnCancel);
        confirmationDialog.getContentPane().add(pnlButtons);
        confirmationDialog.getRootPane().setDefaultButton(btnOk);
    }

    // end of of initComponents
    private int showDialog() {
        int returnValue = CANCEL;
        confirmationDialog.setVisible(true);

        if (bOkPressed) {
            returnValue = OK;
            bOkPressed = false;
        } else {
            returnValue = CANCEL;
        }

        return returnValue;
    }

    /** Implementation of the handle method specified by
     * <code> javax.security.auth.callback.CallbackHandler </code>
     * @param callbacks <code>Array of javax.security.auth.callback.CallbackHandler</code>
     *
     */
    public void handle(Callback[] callbacks)
        throws UnsupportedCallbackException {
        int result = showDialog();

        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {
                // Ignore this section for now. This will be used when a generic callback handler
                // is being implemented. In our current implementation, we are only expecting the
                //login type callback handler.
            } else if (callbacks[i] instanceof NameCallback) {
                // prompt the user for a username
                NameCallback nc = (NameCallback) callbacks[i];
                String strPrompt = nc.getPrompt();
                String strName = "";

                if (strPrompt.equals(authResBundle.getString("Keystore alias: "))) {
                    strName = txtAlias.getText();
                }

                nc.setName(strName);
            } else if (callbacks[i] instanceof PasswordCallback) {
                // prompt the user for sensitive information
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                String strPrompt = pc.getPrompt();
                char[] chrPass = new char[0];

                if (strPrompt.equals(authResBundle.getString(
                                "Keystore password: "))) {
                    /* As of now hide the Keystore password part from the user and
                       read directly from the properties file
                       chrPass = txtStorepass.getPassword() ; */
                    chrPass = ProviderProperties.getInstance()
                                                .getProperty("jaxr-ebxml.security.storepass")
                                                .toCharArray();

                    if ((chrPass == null) || (chrPass.length == 0)) {
                        log.error(
                            "Property jaxr-ebxml.security.storepass is undefined");
                    }
                } else if (strPrompt.equals(authResBundle.getString(
                                "Private key password (optional): "))) {
                    chrPass = txtKeypass.getPassword();
                }

                pc.setPassword(chrPass);
            } else if (callbacks[i] instanceof ConfirmationCallback) {
                ConfirmationCallback cc = (ConfirmationCallback) callbacks[i];

                if (result == OK) {
                    cc.setSelectedIndex(ConfirmationCallback.OK);
                } else {
                    cc.setSelectedIndex(ConfirmationCallback.CANCEL);
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                    "Unrecognized Callback");
            }
        }
    }
}
/*
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/ebxml/jaas/DialogAuthenticationCallbackHandler.java,v 1.4 2003/09/11 02:36:14 farrukh_najmi Exp $
 *
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * ====================================================================
 */
package com.sun.xml.registry.ebxml.jaas;

import com.sun.xml.registry.ebxml.util.ProviderProperties;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Raghu V
 * @version 1.0
 *
 * This class is a CallbackHandler implementation that is suitable for logging into a keystore.
 * The handle(Callback[]) method has been customized such that it can only handle the login method
 * of LoginContext.
 * Although the class has been designed such that it can interactively retrieve the keystore password
 * from the user, these lines have been commented. The current implementation is such that this
 * password is read from the property file(s).
 */

public class DialogAuthenticationCallbackHandler implements CallbackHandler {

    private final Log log;

    /* The ResourceBundle for Sun's Auth package. */
    private static final ResourceBundle authResBundle =
            ResourceBundle.getBundle("sun.security.util.AuthResources");
    
    /* The size of the Dialog to be displayed. This may have to be increased
    later on when we decide to get the keystore password interactively rather
    than from the properties file */
    private static final Dimension DIALOG_SIZE = new Dimension(400, 150) ;
    private static final Dimension PANEL_SIZE = new Dimension(375, 25) ;

    private static final int OK = 1;
    private static final int CANCEL =0 ;

    /* The dialog box shown to the user prompting for security details */
    private JDialog confirmationDialog ;

    /* The frame to which this Dialog is bound to */
    private Frame ownerFrame ;

    /* Panel and components to prompt the user to enter keystore alias  */
    private JPanel pnlAlias = new JPanel(null) ;
    private JLabel lblAlias = new JLabel("Keystore alias:") ;
    private JTextField txtAlias = new JTextField() ;

    /* Panel and components to prompt the user for keystore password */
    private JPanel pnlStorepass = new JPanel(null) ;
    private JLabel lblStorepass = new JLabel("Keystore password:") ;
    private JPasswordField txtStorepass = new JPasswordField(8) ;

    /*Panel and components to prompt for private key password */
    private JPanel pnlKeypass = new JPanel(null) ;
    private JLabel lblKeypass = new JLabel("Private key password(optional):") ;
    private JPasswordField txtKeypass = new JPasswordField(8) ;

    /*Panel and components for the buttons */
    private JPanel pnlButtons = new JPanel(null) ;
    private JButton btnOk = new JButton("OK") ;
    private JButton btnCancel = new JButton("Cancel") ;

    /* boolean flag that indicates whether or not the OK button was pressed */
    private boolean bOkPressed = false ;

    /** 
     * One arg constructor that accepts a <code>java.awt.Frame</code>
     * object. The login dialog shown to the user will be modal to this
     * <code>java.awt.Frame</code> object.
     * @param frame Instance of <code>java.awt.Frame</code> object to
     * which the login dialog is modal
     * @param log Logger to be used
     */
    public DialogAuthenticationCallbackHandler(Frame frame, Log l) {
        ownerFrame = frame ;
        this.log = l;
        initComponents() ;
    }

    private void initComponents() {
        int curX = 5 ;
        int curY = 15 ;
        int lblWidth = 200 ;
        int txtWidth = 165 ;
        int buttonWidth = 75 ;
        int compHeight = 25 ;
        int padding = 5 ;

        // Initialize the confirmation dialog
        confirmationDialog = new JDialog(ownerFrame, "Keystore login", true) ;
        confirmationDialog.getContentPane().setLayout(null);
        confirmationDialog.setLocation(20, 20);
        confirmationDialog.setSize(DIALOG_SIZE);

        //Initialize the components required to get the alias from the user
        pnlAlias.setSize(PANEL_SIZE);
        pnlAlias.setLocation(curX, curY);
        lblAlias.setBounds(0, 0, lblWidth, compHeight);
        txtAlias.setBounds(lblWidth + padding, 0, txtWidth, compHeight) ;
        pnlAlias.add(lblAlias);
        pnlAlias.add(txtAlias) ;
        confirmationDialog.getContentPane().add(pnlAlias) ;

        /* This Entire block needs to be commented for now. THe current plan is to
        hide the Keystore password implementation from the user and read it directly from
        the properties file


        //Initialize the components required to get the keystore password from the user
        curY = curY + PANEL_SIZE.height + padding ;
        txtStorepass.setEchoChar('*');
        pnlStorepass.setSize(PANEL_SIZE) ;
        pnlStorepass.setLocation(curX, curY) ;
        lblStorepass.setBounds(0,0, lblWidth, compHeight);
        txtStorepass.setBounds(lblWidth + padding, 0, txtWidth, compHeight) ;
        pnlStorepass.add(lblStorepass) ;
        pnlStorepass.add(txtStorepass) ;
        confirmationDialog.getContentPane().add(pnlStorepass) ;
        */
        //Initialize the components required to get the key password from the user
        curY = curY + PANEL_SIZE.height + padding * 2 ;
        txtKeypass.setEchoChar('*') ;
        pnlKeypass.setSize(PANEL_SIZE);
        pnlKeypass.setLocation(curX, curY) ;
        lblKeypass.setBounds(0,0, lblWidth, compHeight) ;
        txtKeypass.setBounds(lblWidth + padding, 0, txtWidth, compHeight) ;
        pnlKeypass.add(lblKeypass) ;
        pnlKeypass.add(txtKeypass) ;
        confirmationDialog.getContentPane().add(pnlKeypass) ;

        //Initialize the components required to get the bottom buttons.
        btnOk.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bOkPressed = true ;
                    confirmationDialog.setVisible(false) ;
                }
            });
        btnCancel.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bOkPressed = false ;
                    confirmationDialog.setVisible(false) ;
                }
            });
        curY = curY + PANEL_SIZE.height + padding * 2 ;
        pnlButtons.setSize(PANEL_SIZE) ;
        pnlButtons.setLocation(curX, curY) ;
        int buttonsBoundingWidth = buttonWidth * 2 + padding * 2 ;
        curX = (PANEL_SIZE.width - buttonsBoundingWidth)/2 > 0 ? (PANEL_SIZE.width - buttonsBoundingWidth)/2 : curX ;
        btnOk.setBounds(curX, 0, buttonWidth, compHeight) ;
        curX = curX + buttonWidth + padding * 2 ;
        btnCancel.setBounds(curX, 0, buttonWidth, compHeight) ;
        curX = pnlButtons.getLocation().x ;
        pnlButtons.add(btnOk) ;
        pnlButtons.add(btnCancel) ;
        confirmationDialog.getContentPane().add(pnlButtons) ;
        confirmationDialog.getRootPane().setDefaultButton( btnOk );
    } // end of of initComponents


    private int showDialog() {
        int returnValue = CANCEL ;
        confirmationDialog.setVisible(true);
        if(bOkPressed) {
            returnValue = OK ;
            bOkPressed = false ;
        } else {
            returnValue = CANCEL ;
        }
        return returnValue ;
    }

    /** Implementation of the handle method specified by
     * <code> javax.security.auth.callback.CallbackHandler </code>
     * @param callbacks <code>Array of javax.security.auth.callback.CallbackHandler</code>
     *
     */
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {

        int result = showDialog() ;
        for (int i = 0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof TextOutputCallback) {
               // Ignore this section for now. This will be used when a generic callback handler
               // is being implemented. In our current implementation, we are only expecting the
               //login type callback handler.
            }
            else if (callbacks[i] instanceof NameCallback) {
                // prompt the user for a username
                NameCallback nc = (NameCallback)callbacks[i];
                String strPrompt = nc.getPrompt() ;
                String strName = "" ;
                if (strPrompt.equals(authResBundle.getString("Keystore alias: "))) {
                    strName = txtAlias.getText() ;
                }
                nc.setName(strName);
            }
            else if (callbacks[i] instanceof PasswordCallback) {
                // prompt the user for sensitive information
                PasswordCallback pc = (PasswordCallback)callbacks[i];
                String strPrompt = pc.getPrompt() ;
                char[] chrPass = new char[0] ;
                if (strPrompt.equals(authResBundle.getString("Keystore password: "))) {
                    /* As of now hide the Keystore password part from the user and
                       read directly from the properties file
                       chrPass = txtStorepass.getPassword() ; */
                    chrPass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storepass").toCharArray();
                    if ((chrPass == null) || (chrPass.length == 0))
                        log.error("Property jaxr-ebxml.security.storepass is undefined");
                }
                else if (strPrompt.equals(authResBundle.getString("Private key password (optional): "))) {
                    chrPass = txtKeypass.getPassword() ;
                }
                pc.setPassword(chrPass);
            }
            else if (callbacks[i] instanceof ConfirmationCallback) {
                ConfirmationCallback cc = (ConfirmationCallback)callbacks[i];
                if (result == OK ) {
                    cc.setSelectedIndex(ConfirmationCallback.OK);
                }
                else {
                    cc.setSelectedIndex(ConfirmationCallback.CANCEL);
                }
            }
            else {
               throw new UnsupportedCallbackException
                   (callbacks[i], "Unrecognized Callback");
            }
        }
    }

}
