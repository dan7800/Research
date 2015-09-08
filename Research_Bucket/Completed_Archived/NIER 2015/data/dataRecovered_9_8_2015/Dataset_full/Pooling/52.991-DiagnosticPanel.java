/*
 * Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the Academic Free License Version 1.0
 *
 * Academic Free License
 * Version 1.0
 *
 * This Academic Free License applies to any software and associated 
 * documentation (the "Software") whose owner (the "Licensor") has placed the 
 * statement "Licensed under the Academic Free License Version 1.0" immediately 
 * after the copyright notice that applies to the Software. 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of the Software (1) to use, copy, modify, merge, publish, perform, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, and (2) under patent 
 * claims owned or controlled by the Licensor that are embodied in the Software 
 * as furnished by the Licensor, to make, use, sell and offer for sale the 
 * Software and derivative works thereof, subject to the following conditions: 
 *
 * - Redistributions of the Software in source code form must retain all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers. 
 * - Redistributions of the Software in executable form must reproduce all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers in the documentation and/or 
 *   other materials provided with the distribution. 
 * - Neither the names of Licensor, nor the names of any contributors to the 
 *   Software, nor any of their trademarks or service marks, may be used to 
 *   endorse or promote products derived from this Software without express 
 *   prior written permission of the Licensor. 
 *
 * DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS 
 * OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER 
 * A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY 
 * PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS 
 * AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE 
 * LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE. 
 *
 * This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved. 
 * Permission is hereby granted to copy and distribute this license without 
 * modification. This license may not be modified without the express written 
 * permission of its copyright owner. 
 */

/* ===== 
 *
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/monitor/DiagnosticPanel.java,v 1.18 2003/12/11 06:41:29 bobpykoon Exp $
 *
 * Code authored by:
 *
 * kcyee [2002-05-09]
 *
 * Code reviewed by:
 *
 * username [YYYY-MM-DD]
 *
 * Remarks:
 *
 * =====
 */

package hk.hku.cecid.phoenix.message.monitor;

import hk.hku.cecid.phoenix.message.handler.ApplicationContext;
import hk.hku.cecid.phoenix.message.handler.Request;
import hk.hku.cecid.phoenix.message.handler.RequestException;
import hk.hku.cecid.phoenix.message.handler.Utility;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
/**
 * A panel for diagnosing MSH
 *
 * @author tslam
 * @version $Revision: 1.18 $
 */
public class DiagnosticPanel extends JPanel {

    /** The MSH stub object */
    protected Request request;

    /** Application context registered to MSH */
    protected ApplicationContext appContext;

    /** Radio button for doing database connection diagnostics */
    protected JRadioButton databaseRadio;
    /** Radio button for doing message persistence diagnostics */
    protected JRadioButton persistenceRadio;
    /** Radio button for checking internal state consistency */
    protected JRadioButton internalRadio;
    /** Radio button for getting environment settings */
    protected JRadioButton envRadio;
    /** Radio button for doing loopback test*/
    protected JRadioButton loopbackRadio;
    /** Radio button for getting pending messages */
    protected JRadioButton pendingRadio;
    /** Radio button for resetting database connection pool*/
    protected JRadioButton resetRadio;
    /** Radio button for suspending MSH*/
    protected JRadioButton suspendRadio;
    /** Radio button for resuming MSH*/
    protected JRadioButton resumeRadio;
    /** Radio button for terminating MSH*/
    protected JRadioButton terminateRadio;
    /** Radio button for backing up MSH data*/
    protected JRadioButton backupRadio;
    /** Radio button for restoring MSH data*/
    protected JRadioButton restoreRadio;
    /** Radio button for getting trusted repository */
    protected JRadioButton trustedRadio;
    /** Radio button for checking if MSH is halted */
    protected JRadioButton haltedRadio;
    /** Radio button for checking number of records in db */
    protected JRadioButton numRecordsRadio;
    /** Radio button for checking db connection pool info */
    protected JRadioButton dbConnInfoRadio;
    /** Input field for entering start time */
    protected JTextField startText;
    /** Input field for entering end time */
    protected JTextField endText;
    /** Input field for entering start time */
    protected JTextField startText2;
    /** Input field for entering end time */
    protected JTextField endText2;
    /** Radio button for archiving MSH data by application context*/
    protected JRadioButton archiveAppContextRadio;
    /** Radio button for archiving MSH data by date*/
    protected JRadioButton archiveDateRadio;
    /** Radio button for archiving MSH data by date and application contexts */
    protected JRadioButton archiveBothRadio;
    /** Push button for sending diagnostic system commands */
    protected JButton goButton;
    /** Text area for showing diagnostic results */
    protected JTextArea resultTextArea;
    /** Search text field */
    protected JTextField searchText;
    /** Push button for searching specified text */
    protected JButton searchButton;

    /** Default button width */
    protected static final int BUTTON_WIDTH = 100;
    /** Default button height */
    protected static final int BUTTON_HEIGHT = 25;

    /** Default number of rows for text area */
    protected static final int TEXTAREA_ROWS = 6;
    /** Default number of columns for text area */
    protected static final int TEXTAREA_COLUMNS = 40;

    /** Default constructor. This function draws the user interface */
    public DiagnosticPanel() {
        createUI();
    }

    /** Draws user interface */
    protected void createUI() {
        JPanel diagPanel = new JPanel();
        JPanel resultPanel = new JPanel();

        diagPanel.setBorder(new TitledBorder("Diagnostics"));
        resultPanel.setBorder(new TitledBorder("Diagnostics Results"));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        add(diagPanel, c);
        c.gridx = 0;
        c.gridy = 1;
        add(resultPanel, c);

        Color textColor = (new JLabel()).getForeground();

        ButtonGroup buttonGroup;
        diagPanel.setLayout(new GridBagLayout());
        databaseRadio = new JRadioButton("Database connection");
        persistenceRadio = new JRadioButton("Message persistence");
        internalRadio = new JRadioButton("Internal state consistency");
        envRadio = new JRadioButton("Environment settings");
        loopbackRadio = new JRadioButton("Loopback test");
        pendingRadio = new JRadioButton("Get pending messages");
        resetRadio = new JRadioButton("Reset database connection pool");
        suspendRadio = new JRadioButton("Suspend MSH");
        resumeRadio = new JRadioButton("Resume MSH");
        terminateRadio = new JRadioButton("Terminate MSH");
        backupRadio = new JRadioButton("Backup MSH data to file");
        restoreRadio = new JRadioButton("Restore MSH data from file");
        trustedRadio = new JRadioButton("Get trusted repositories");
        haltedRadio = new JRadioButton("Check if MSH is halted");
        numRecordsRadio = new JRadioButton("Get number of records in DB");
        dbConnInfoRadio = new JRadioButton("Get DB connection pool info");
        archiveAppContextRadio = 
            new JRadioButton("Archive MSH data by AppContext");
        archiveDateRadio = new JRadioButton("Archive MSH data by Date");
        archiveBothRadio = new JRadioButton("Archive MSH Data by Both");
        startText = new JTextField(10);
        endText = new JTextField(10);
        startText2 = new JTextField(10);
        endText2 = new JTextField(10);

        goButton = new JButton("Go");
        goButton.setPreferredSize(
            new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                diagnose();
            }
        });

        buttonGroup = new ButtonGroup();
        buttonGroup.add(databaseRadio);
        buttonGroup.add(persistenceRadio);
        buttonGroup.add(internalRadio);
        buttonGroup.add(envRadio);
        buttonGroup.add(loopbackRadio);
        buttonGroup.add(pendingRadio);
        buttonGroup.add(resetRadio);
        buttonGroup.add(suspendRadio);
        buttonGroup.add(resumeRadio);
        buttonGroup.add(terminateRadio);
        buttonGroup.add(backupRadio);
        buttonGroup.add(restoreRadio);
        buttonGroup.add(trustedRadio);
        buttonGroup.add(haltedRadio);
        buttonGroup.add(numRecordsRadio);
        buttonGroup.add(dbConnInfoRadio);
        buttonGroup.add(archiveAppContextRadio);
        buttonGroup.add(archiveDateRadio);
        buttonGroup.add(archiveBothRadio);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(databaseRadio, c);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(persistenceRadio, c);
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(internalRadio, c);
        c.gridx = 0;
        c.gridy = 3;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(envRadio, c);
        c.gridx = 0;
        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(loopbackRadio, c);
        c.gridx = 0;
        c.gridy = 5;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(resetRadio, c);
        c.gridx = 0;
        c.gridy = 6;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(haltedRadio, c);
        c.gridx = 0;
        c.gridy = 7;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(dbConnInfoRadio, c);
        c.gridx = 0;
        c.gridy = 8;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(archiveAppContextRadio, c);
        c.gridx = 0;
        c.gridy = 9;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(archiveDateRadio, c);
        c.gridx = 0;
        c.gridy = 10;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(archiveBothRadio, c);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(pendingRadio, c);
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(suspendRadio, c);
        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(resumeRadio, c);
        c.gridx = 1;
        c.gridy = 3;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(terminateRadio, c);
        c.gridx = 1;
        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(backupRadio, c);
        c.gridx = 1;
        c.gridy = 5;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(restoreRadio, c);
        c.gridx = 1;
        c.gridy = 6;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(trustedRadio, c);
        c.gridx = 1;
        c.gridy = 7;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(numRecordsRadio, c);
        c.gridx = 1;
        c.gridy = 8;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(new JLabel(" "), c);
        c.gridx = 1;
        c.gridy = 9;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(startText, c);
        c.gridx = 2;
        c.gridy = 9;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(new JLabel(" to "), c);
        c.gridx = 3;
        c.gridy = 9;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(endText, c);
        c.gridx = 1;
        c.gridy = 10;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(startText2, c);
        c.gridx = 2;
        c.gridy = 10;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(new JLabel(" to "), c);
        c.gridx = 3;
        c.gridy = 10;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(endText2, c);
        c.gridx = 0;
        c.gridy = 11;
        c.gridwidth = 4;
        c.fill = GridBagConstraints.BOTH;
        diagPanel.add(goButton, c);

        resultTextArea = new JTextArea(TEXTAREA_ROWS, TEXTAREA_COLUMNS);
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultPanel.setLayout(new GridBagLayout());

        searchButton = new JButton("Search");
        searchButton.setPreferredSize(
            new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });

        searchText = new JTextField(20);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        resultPanel.add(new JScrollPane(resultTextArea, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), c);

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        resultPanel.add(new JLabel("Search for: "), c);

        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        resultPanel.add(searchText, c);

        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        resultPanel.add(searchButton, c);
    }

    /** Reset MSH's database connection pool */
    protected void resetConnectionPool() {
        try {
            String result = request.resetConnectionPool();

            if (result != null) {
                resultTextArea.setText(result);
            }
        }
        catch (RequestException e) {
            JOptionPane.showMessageDialog(this, "Exception occurred:\n"
                + e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
    }
    /** Send diagnostic command to MSH */
    protected void diagnose() {
        try {
            String result = null;
            if (databaseRadio.isSelected()) {
                result = request.checkDatabase();
            }
            else if (persistenceRadio.isSelected()) {
                result = request.checkPersistence();
            }
            else if (internalRadio.isSelected()) {
                result = request.checkInternalStates();
            }
            else if (envRadio.isSelected()) {
                result = request.reportEnvironment();
            }
            else if (loopbackRadio.isSelected()) {
                result = request.testLoopback();
            }
            else if (resetRadio.isSelected()) {
                result = request.resetConnectionPool();
            }
            else if (pendingRadio.isSelected()) {
                StringBuffer buf = new StringBuffer();
                String [] results = request.getPendingMessages();
                if (results.length == 0) {
                    result = "No pending message.";
                }
                else {
                    for (int i = 0; i < results.length; i++) {
                        buf.append(results[i] + "\n");
                    }
                    result = buf.toString();
                }
            }
            else if (suspendRadio.isSelected()) {
                result = request.haltMSH(Request.HALT_SUSPEND);
            }
            else if (resumeRadio.isSelected()) {
                result = request.resumeMSH();
            }
            else if (terminateRadio.isSelected()) {
                result = request.haltMSH(Request.HALT_TERMINATE);
            }
            else if (backupRadio.isSelected()) {
                result = request.backupMSH();
            }
            else if (restoreRadio.isSelected()) {
                result = request.restoreMSH();
            }
            else if (trustedRadio.isSelected()) {
                String [] results = request.getTrustedRepository();
                result = new String();
                for (int i = 0; i < results.length; i++) {
                    result += results[i] + "\n";
                }
            }
            else if (haltedRadio.isSelected()) {
                result = new Boolean(request.getIsHalted()).toString();
            }
            else if (dbConnInfoRadio.isSelected()) {
                result = request.getDBConnectionPoolInfo();
            }
            else if (numRecordsRadio.isSelected()) {
                result = request.getNumRecordsInDB();
            }
            else if (archiveAppContextRadio.isSelected()) {
                result = request.archiveByAppContext(
                    new ApplicationContext [] { appContext });
            }
            else if (archiveDateRadio.isSelected()) {
                Date startDate = null;
                Date endDate = null;
                String errMsg = "";
                if (startText.getText().length() == 0) {
                    startDate = null;
                }
                else {
                    startDate = Utility.fromUTCString(startText.getText());
                    if (startDate == null) {
                        errMsg += "Cannot parse starting date\n";
                    }
                }
                if (endText.getText().length() == 0) {
                    endDate = null;
                }
                else {
                    endDate = Utility.fromUTCString(endText.getText());
                    if (endDate == null) {
                        errMsg += "Cannot parse ending date\n";
                    }
                }
                if (errMsg.length() > 0) {
                    JOptionPane.showMessageDialog(this, errMsg, "Error", 
                                                  JOptionPane.OK_OPTION);
                }
                else {
                    result = request.archiveByDate(startDate, endDate);
                }
            }
            else if (archiveBothRadio.isSelected()) {
                Date startDate = null;
                Date endDate = null;
                String errMsg = "";
                if (startText2.getText().length() == 0) {
                    startDate = null;
                }
                else {
                    startDate = Utility.fromUTCString(startText2.getText());
                    if (startDate == null) {
                        errMsg += "Cannot parse starting date\n";
                    }
                }
                if (endText2.getText().length() == 0) {
                    endDate = null;
                }
                else {
                    endDate = Utility.fromUTCString(endText2.getText());
                    if (endDate == null) {
                        errMsg += "Cannot parse ending date\n";
                    }
                }
                if (errMsg.length() > 0) {
                    JOptionPane.showMessageDialog(this, errMsg, "Error", 
                                                  JOptionPane.OK_OPTION);
                }
                else {
                    result = request.archive(startDate, endDate, 
                        new ApplicationContext [] { appContext });
                }
            }
            else {
                JOptionPane.showMessageDialog(this, "Please select one of the "
                    + "options available.", "Error", JOptionPane.OK_OPTION);
            }

            if (result != null) {
                resultTextArea.setText(result);
            }
        }
        catch (RequestException e) {
            JOptionPane.showMessageDialog(this, "Exception occurred:\n"
                + e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
    }

    /** 
     * Search for a specified string in diagnostic result
     */
    protected void search() {
        String text = searchText.getText();
        int index = resultTextArea.getText().indexOf(text, 
            resultTextArea.getCaretPosition());
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Cannot find specified string");
        }
        else {
            resultTextArea.select(index, index + text.length());
        }
    }

    /** Sets the Application Context registered to MSH */
    public void setApplicationContext(ApplicationContext ac) {
        appContext = ac;
    }

    /** Sets the MSH stub object */
    public void setSendingContext(Request req) {
        request = req;
    }
}

