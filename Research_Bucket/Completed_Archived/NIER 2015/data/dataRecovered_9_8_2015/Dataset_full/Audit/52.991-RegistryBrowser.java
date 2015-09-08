/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/RegistryBrowser.java,v 1.14 2004/03/18 20:38:43 dhilder Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.text.MessageFormat;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.RegistryObject;

import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.swing.metal.BigContrastMetalTheme;
import org.freebxml.omar.client.ui.swing.metal.ContrastMetalTheme;
import org.freebxml.omar.client.ui.swing.metal.DemoMetalTheme;
import org.freebxml.omar.client.ui.swing.metal.MetalThemeMenu;
import org.freebxml.omar.client.ui.swing.metal.UISwitchListener;

import org.freebxml.omar.common.BindingUtility;

import org.freebxml.omar.client.xml.registry.ConnectionImpl;

/**
 * The ebXML Registry Browser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryBrowser extends JFrame implements PropertyChangeListener {
	public static final String BROWSER_VERSION = "3.0alpha1";

    /** DOCUMENT ME! */
    private static final org.apache.commons.logging.Log log =
		org.apache.commons.logging.LogFactory.getLog(RegistryBrowser.class);
    static JAXRClient client = new JAXRClient();
    protected static JAXRResourceBundle resourceBundle =
		JAXRResourceBundle.getInstance();

    /** Bound Properties. */
    public static String PROPERTY_AUTHENTICATED = "PROPERTY_AUTHENTICATED";
    public static String PROPERTY_LOCALE = "locale";

    /** DOCUMENT ME! */
    static String selectAnItem = resourceBundle.getString("listBox.enterURL");
    static RegistryBrowser instance;
    
    //The baseURL to registry currently connected to.
    static String baseURL = null;

    /** DOCUMENT ME! */
    public ClassLoader classLoader;

    /** DOCUMENT ME! */
    Color buttonBackground;

    /** DOCUMENT ME! */
    ConceptsTreeDialog conceptsTreeDialog = null;

    /** A dialog for selecting a Locale for RegistryBrowser. */
    LocaleSelectorDialog localeSelectorDialog = null;

    /** DOCUMENT ME! */
    JMenuBar menuBar;

	/** File menu */
	JMenu fileMenu;

	/** Edit menu */
	JMenu editMenu;

	/** View menu */
	JMenu viewMenu;

	/** Theme menu */
	JMenu themeMenu;

	/** An array of themes */
	MetalTheme[] themes = {
		new DefaultMetalTheme(),
		new DemoMetalTheme(),
		new ContrastMetalTheme(),
		new BigContrastMetalTheme(),
	};

	/** Help menu */
	JMenu helpMenu;

    /** DOCUMENT ME! */
    JMenuItem newItem;

    /** DOCUMENT ME! */
    JMenuItem openItem;

    /** DOCUMENT ME! */
    JMenuItem saveItem;

    /** DOCUMENT ME! */
    JMenuItem saveAsItem;

    /** DOCUMENT ME! */
    JMenuItem exitItem;

    /** DOCUMENT ME! */
    JMenuItem cutItem;

    /** DOCUMENT ME! */
    JMenuItem copyItem;

    /** DOCUMENT ME! */
    JMenuItem pasteItem;

    /** DOCUMENT ME! */
    JMenuItem aboutItem;

    // move inside constructor later

    /** DOCUMENT ME! */
    FileDialog saveFileDialog = new FileDialog(this);

    /** DOCUMENT ME! */
    JFileChooser openFileDialog = new JFileChooser();

    /** DOCUMENT ME! */
    JPanel tabbedPaneParent = new JPanel();

    /** The tabbed pane */
    JBTabbedPane tabbedPane = null;

    /** DOCUMENT ME! */
    JPanel topPanel = new JPanel();

	/** Button for selecting search function. */
	JButton findButton = null;

	/** Button for selecting scheme. */
	JButton showSchemesButton = null;

	/** Button for logging in. */
	JButton authenticateButton = null;

	/** Button for logging out. */
	JButton logoutButton = null;

	/** Button for registering a user. */
	JButton userRegButton = null;

	/** Button for selecting locale. */
	JButton localeSelButton = null;

	/** Label for registryCombo */
	JLabel locationLabel;

    /** DOCUMENT ME! */
    JComboBox registryCombo = new javax.swing.JComboBox();

    /** TextField to show user name for currently authenticated user */
    JTextField currentUserText = new JTextField();

    /** DOCUMENT ME! */
    JPanel toolbarPanel = new JPanel();

    /** DOCUMENT ME! */
    JToolBar discoveryToolBar = null;

    /** DOCUMENT ME! */
    JPanel registryObjectsPanel = new JPanel();

	private class ItemText {
		private String text;

		ItemText(String text) {
			this.text = text;
		}

		public String toString() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	ItemText selectAnItemText;

    /**
     * Creates a new RegistryBrowser object.
     */
    private RegistryBrowser() {
        instance = this;

        classLoader = getClass().getClassLoader(); //new JAXRBrowserClassLoader(getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);

        try {
            classLoader.loadClass("javax.xml.messaging.Endpoint");
        } catch (ClassNotFoundException e) {
            log.error("Could not find class javax.xml.messaging.Endpoint", e);
        }
        
        UIManager.addPropertyChangeListener(new UISwitchListener((JComponent)getRootPane()));

        //add listener for 'locale' bound property
		addPropertyChangeListener(PROPERTY_LOCALE,
								  this);
        

        menuBar = new javax.swing.JMenuBar();

        fileMenu = new JMenu();
        editMenu = new JMenu();
        viewMenu = new JMenu();
        helpMenu = new JMenu();
        javax.swing.JSeparator JSeparator1 = new javax.swing.JSeparator();

        newItem = new JMenuItem();
        openItem = new JMenuItem();
        saveItem = new JMenuItem();
        saveAsItem = new JMenuItem();
        exitItem = new JMenuItem();
        cutItem = new JMenuItem();
        copyItem = new JMenuItem();
        pasteItem = new JMenuItem();
        aboutItem = new JMenuItem();
        setJMenuBar(menuBar);
        setTitle(resourceBundle.getString("title.registryBrowser"));
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(0, 0));

        // Scale window to be centered using 70% of screen
        java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit()
                                                 .getScreenSize();

        setBounds((int) (dim.getWidth() * .15), (int) (dim.getHeight() * .1),
            (int) (dim.getWidth() * .7), (int) (dim.getHeight() * .75));
        setVisible(false);
        saveFileDialog.setMode(FileDialog.SAVE);
        saveFileDialog.setTitle(resourceBundle.getString("dialog.save.title"));

        java.awt.GridBagLayout gb = new java.awt.GridBagLayout();

        topPanel.setLayout(gb);
        getContentPane().add("North", topPanel);

        GridBagConstraints c = new GridBagConstraints();

        toolbarPanel.setLayout(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEADING, 0, 0));
        toolbarPanel.setBounds(0, 0, 488, 29);

        discoveryToolBar = createDiscoveryToolBar();
        toolbarPanel.add(discoveryToolBar);

        //c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        gb.setConstraints(toolbarPanel, c);
        topPanel.add(toolbarPanel);

        //Panel containing context info like registry location and user context
        JPanel contextPanel = new JPanel();
        java.awt.GridBagLayout gb1 = new java.awt.GridBagLayout();

        contextPanel.setLayout(gb1);

        locationLabel = new javax.swing.JLabel(resourceBundle.getString(
                    "label.registryLocation"));

        // locationLabel.setPreferredSize(new Dimension(80, 23));
        //c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new java.awt.Insets(0, 5, 0, 0);
        gb1.setConstraints(locationLabel, c);

        // contextPanel.setBackground(Color.green);
        contextPanel.add(locationLabel);

        String pathSep = System.getProperty("file.separator");

		selectAnItemText = new ItemText(selectAnItem);
        registryCombo.addItem(selectAnItemText.toString());

        org.freebxml.omar.client.ui.common.conf.bindings.Configuration cfg = UIUtility.getInstance().getConfiguration();
        org.freebxml.omar.client.ui.common.conf.bindings.RegistryURIListType urlList =
            cfg.getRegistryURIList();
        List urls = urlList.getRegistryURI();
        Iterator urlsIter = urls.iterator();
        while (urlsIter.hasNext()) {
            ItemText url = new ItemText((String)urlsIter.next());
            registryCombo.addItem(url.toString());
        }

        registryCombo.setEditable(true);
        registryCombo.setEnabled(true);
        registryCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String url = (String)registryCombo.getSelectedItem();
                    if ((url == null) || (url.equals(selectAnItem))) {
                        return;
                    }

                    RegistryBrowser.setWaitCursor();
                    connectToRegistry(url);

                    Thread.currentThread().setContextClassLoader(RegistryBrowser.getInstance().classLoader);

                    conceptsTreeDialog = null;
                    ConceptsTreeDialog.clearCache();

                    try {
                        tabbedPane = new JBTabbedPane();
                        tabbedPaneParent.removeAll();
                        tabbedPaneParent.add(tabbedPane, BorderLayout.CENTER);
                        
                        // DBH 1/30/04 - Add the submissions panel if the user
                        // is authenticated.
                        ConnectionImpl connection = (ConnectionImpl)RegistryBrowser.client.connection;
                        boolean newValue = connection.isAuthenticated();
                        firePropertyChange(PROPERTY_AUTHENTICATED, false, newValue);

                        getRootPane().updateUI();
                    } catch (JAXRException e1) {
                        displayError(e1);
                    }

                    RegistryBrowser.setDefaultCursor();
                }
            });
		//        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.9;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new java.awt.Insets(0, 0, 5, 0);
        gb1.setConstraints(registryCombo, c);
        contextPanel.add(registryCombo);

        JLabel currentUserLabel = new JLabel(resourceBundle.getString("label.currentUser"),
                SwingConstants.TRAILING);
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new java.awt.Insets(0, 5, 5, 0);
        gb1.setConstraints(currentUserLabel, c);

        //contextPanel.add(currentUserLabel);
        currentUserText.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String text = currentUserText.getText();
                }
            });

        currentUserText.setEditable(false);
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.9;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new java.awt.Insets(0, 0, 5, 5);
        gb1.setConstraints(currentUserText, c);

        //contextPanel.add(currentUserText);        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.9;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        gb.setConstraints(contextPanel, c);
        topPanel.add(contextPanel, c);

        tabbedPaneParent.setBorder(javax.swing.BorderFactory.createBevelBorder(
                javax.swing.border.BevelBorder.LOWERED));
        tabbedPaneParent.setLayout(new BorderLayout());
        tabbedPaneParent.setToolTipText(resourceBundle.getString("tabbedPane.tip"));

        getContentPane().add("Center", tabbedPaneParent);

        fileMenu.setText(resourceBundle.getString("menu.file"));
        fileMenu.setActionCommand("File");
        fileMenu.setMnemonic((int) 'F');
        menuBar.add(fileMenu);

        saveItem.setHorizontalTextPosition(SwingConstants.TRAILING);
        saveItem.setText(resourceBundle.getString("menu.save"));
        saveItem.setActionCommand("Save");
        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
        saveItem.setMnemonic((int) 'S');

        //fileMenu.add(saveItem);
        fileMenu.add(JSeparator1);
        exitItem.setText(resourceBundle.getString("menu.exit"));
        exitItem.setActionCommand("Exit");
        exitItem.setMnemonic((int) 'X');
        fileMenu.add(exitItem);
        editMenu.setText(resourceBundle.getString("menu.edit"));
        editMenu.setActionCommand("Edit");
        editMenu.setMnemonic((int) 'E');

        //menuBar.add(editMenu);
        cutItem.setHorizontalTextPosition(SwingConstants.TRAILING);
        cutItem.setText(resourceBundle.getString("menu.cut"));
        cutItem.setActionCommand("Cut");
        cutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_X, java.awt.Event.CTRL_MASK));
        cutItem.setMnemonic((int) 'T');
        editMenu.add(cutItem);
        copyItem.setHorizontalTextPosition(SwingConstants.TRAILING);
        copyItem.setText(resourceBundle.getString("menu.copy"));
        copyItem.setActionCommand("Copy");
        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C, java.awt.Event.CTRL_MASK));
        copyItem.setMnemonic((int) 'C');
        editMenu.add(copyItem);
        pasteItem.setHorizontalTextPosition(SwingConstants.TRAILING);
        pasteItem.setText(resourceBundle.getString("menu.paste"));
        pasteItem.setActionCommand("Paste");
        pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_V, java.awt.Event.CTRL_MASK));
        pasteItem.setMnemonic((int) 'P');
        editMenu.add(pasteItem);

        viewMenu.setText(resourceBundle.getString("menu.view"));
        viewMenu.setActionCommand("view");
        viewMenu.setMnemonic((int) 'V');

        themeMenu = new MetalThemeMenu(resourceBundle.getString("menu.theme"),
									   themes);
        viewMenu.add(themeMenu);
        menuBar.add(viewMenu);

        helpMenu.setText(resourceBundle.getString("menu.help"));
        helpMenu.setActionCommand("Help");
        helpMenu.setMnemonic((int) 'H');
        menuBar.add(helpMenu);
        aboutItem.setHorizontalTextPosition(SwingConstants.TRAILING);
        aboutItem.setText(resourceBundle.getString("menu.about"));
        aboutItem.setActionCommand("About...");
        aboutItem.setMnemonic((int) 'A');

        aboutItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
					Object[] aboutArgs = {BROWSER_VERSION};
					MessageFormat form =
						new MessageFormat(resourceBundle.getString("dialog.about.text"));
                    JOptionPane.showMessageDialog(RegistryBrowser.this,
												  form.format(aboutArgs),
                        resourceBundle.getString("dialog.about.title"),
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });

        helpMenu.add(aboutItem);

        // REGISTER_LISTENERS
        SymWindow aSymWindow = new SymWindow();

        this.addWindowListener(aSymWindow);

        SymAction lSymAction = new SymAction();

        saveItem.addActionListener(lSymAction);
        exitItem.addActionListener(lSymAction);

        javax.swing.SwingUtilities.updateComponentTreeUI(getContentPane());
        javax.swing.SwingUtilities.updateComponentTreeUI(menuBar);
        javax.swing.SwingUtilities.updateComponentTreeUI(openFileDialog);

        //Auto select the registry that is configured to connect to by default
        String selectedIndexStr =
			org.freebxml.omar.client.xml.registry.util.ProviderProperties.getInstance()
			.getProperty("jaxr-ebxml.registryBrowser.registryLocationCombo.initialSelectionIndex",
						 "0");
        int index = Integer.parseInt(selectedIndexStr);

        try {
            registryCombo.setSelectedIndex(index);
        } catch (IllegalArgumentException e) {
			Object[] invalidIndexArguments = {new Integer(index)};
			MessageFormat form =
				new MessageFormat(resourceBundle.getString("message.error.invalidIndex"));
            displayError(form.format(invalidIndexArguments),
						 e);
        }
    }

    //UserRegistrationWizardAction userRegAction = new UserRegistrationWizardAction();
    public static RegistryBrowser getInstance() {
        if (instance == null) {
            instance = new RegistryBrowser();
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JAXRClient getClient() {
        return client;
    }

    /**
     * Action for the Find tool.
     */
    public void findAction() {
        tabbedPane.findAction();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JToolBar createDiscoveryToolBar() {
        javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
        toolBar.setFloatable(true);

        // Find
        URL findUrl = getClass().getClassLoader().getResource("icons/find.gif");
        ImageIcon findIcon = new ImageIcon(findUrl);
        findButton = toolBar.add(new AbstractAction("", findIcon) {
                    public void actionPerformed(ActionEvent e) {
                        findAction();
                    }
                });

        findButton.setToolTipText(resourceBundle.getString("button.find"));

        // showSchemes
        URL showSchemesUrl = getClass().getClassLoader().getResource("icons/schemeViewer.gif");
        ImageIcon showSchemesIcon = new ImageIcon(showSchemesUrl);
        showSchemesButton = toolBar.add(new AbstractAction("",
                    showSchemesIcon) {
                    public void actionPerformed(ActionEvent e) {
                        if (RegistryBrowser.client.connection == null) {
                            displayUnconnectedError();
                        } else {
                            ConceptsTreeDialog.showSchemes(RegistryBrowser.getInstance(),
                                false, isAuthenticated());
                        }
                    }
                });

        showSchemesButton.setToolTipText(resourceBundle.getString("button.showSchemes"));

        // Re-authenticate
        URL authenticateUrl = getClass().getClassLoader().getResource("icons/authenticate.gif");
        ImageIcon authenticateIcon = new ImageIcon(authenticateUrl);
        authenticateButton = toolBar.add(new AbstractAction("",
                    authenticateIcon) {
                    public void actionPerformed(ActionEvent e) {
                        authenticate();
                    }
                });

        authenticateButton.setToolTipText(resourceBundle.getString("button.authenticate"));

        // Logout
        URL logoutUrl = getClass().getClassLoader().getResource("icons/logoff.gif");
        ImageIcon logoutIcon = new ImageIcon(logoutUrl);
        logoutButton = toolBar.add(new AbstractAction("", logoutIcon) {
                    public void actionPerformed(ActionEvent e) {
                        logout();
                    }
                });

        logoutButton.setToolTipText(resourceBundle.getString("button.logout"));

        // user registration
        URL userRegUrl = getClass().getClassLoader().getResource("icons/userReg.gif");
        ImageIcon userRegIcon = new ImageIcon(userRegUrl);
		userRegButton = toolBar.add(new AbstractAction("", userRegIcon) {
                    public void actionPerformed(ActionEvent e) {
                        RegistryBrowser.setWaitCursor();

                        //showUserRegistrationWizard();
                        if (RegistryBrowser.client.connection == null) {
                            displayUnconnectedError();
                        } else {
                            org.freebxml.omar.client.ui.swing.registration.UserManager userMgr =
                                org.freebxml.omar.client.ui.swing.registration.UserManager.getInstance();

                            try {
                                userMgr.registerNewUser();
                            } catch (Exception er) {
                                RegistryBrowser.displayError(er);
                            }
                        }

                        RegistryBrowser.setDefaultCursor();
                    }
                });

        userRegButton.setToolTipText(resourceBundle.getString("button.userReg"));

        // locale selection
        URL localeSelUrl = getClass().getClassLoader().getResource("icons/localeSel.gif");
        ImageIcon localeSelIcon = new ImageIcon(localeSelUrl);
        localeSelButton = toolBar.add(new AbstractAction("",
                    localeSelIcon) {
                    public void actionPerformed(ActionEvent e) {
                        RegistryBrowser.setWaitCursor();

                        LocaleSelectorDialog dialog = getLocaleSelectorDialog();

						Locale oldSelectedLocale =
							getSelectedLocale();
							
                        dialog.setVisible(true);

						Locale selectedLocale =
							getSelectedLocale();

						System.out.println(getLocale());

						setLocale(selectedLocale);

                        RegistryBrowser.setDefaultCursor();
                    }
                });

        localeSelButton.setToolTipText(resourceBundle.getString("button.localeSel"));

        return toolBar;
    }

    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
		}
	}

	/**
	 * Processes a change in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		setComponentOrientation(ComponentOrientation.
									getOrientation(newLocale));
		updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
		/* Frame */
        setTitle(resourceBundle.getString("title.registryBrowser"));

		/* Dialog boxes */
        saveFileDialog.setTitle(resourceBundle.getString("dialog.save.title"));

		/* Menus and submenus */
        fileMenu.setText(resourceBundle.getString("menu.file"));
        editMenu.setText(resourceBundle.getString("menu.edit"));
        viewMenu.setText(resourceBundle.getString("menu.view"));
        themeMenu.setText(resourceBundle.getString("menu.theme"));
        helpMenu.setText(resourceBundle.getString("menu.help"));

		/* Menu items */
        saveItem.setText(resourceBundle.getString("menu.save"));
        exitItem.setText(resourceBundle.getString("menu.exit"));
        cutItem.setText(resourceBundle.getString("menu.cut"));
        copyItem.setText(resourceBundle.getString("menu.copy"));
        pasteItem.setText(resourceBundle.getString("menu.paste"));
        aboutItem.setText(resourceBundle.getString("menu.about"));

		/* Buttons */
        findButton.setToolTipText(resourceBundle.getString("button.find"));
        showSchemesButton.setToolTipText(resourceBundle.getString("button.showSchemes"));
        authenticateButton.setToolTipText(resourceBundle.getString("button.authenticate"));
        logoutButton.setToolTipText(resourceBundle.getString("button.logout"));
        userRegButton.setToolTipText(resourceBundle.getString("button.userReg"));
        localeSelButton.setToolTipText(resourceBundle.getString("button.localeSel"));

		/* Registry combo */
        locationLabel.setText(resourceBundle.getString("label.registryLocation"));
		selectAnItemText.setText(resourceBundle.getString("listBox.enterURL"));

		/* Tabbed pane parent */
        tabbedPaneParent.setToolTipText(resourceBundle.getString("tabbedPane.tip"));
	}

    /**
     * Getter for property localeSelectorDialog. Instantiates a new
     * LocaleSelectorDialog with Locale.getDefault() if property is null.
     *
     * @return value of property localeSelectorDialog.
     */
    public LocaleSelectorDialog getLocaleSelectorDialog() {
        if (localeSelectorDialog == null) {
            RegistryBrowser.setWaitCursor();
            localeSelectorDialog = new LocaleSelectorDialog(Locale.getDefault(),
                    RegistryBrowser.getInstance(), true);
            RegistryBrowser.setDefaultCursor();
        }

        javax.swing.SwingUtilities.updateComponentTreeUI(localeSelectorDialog);
		localeSelectorDialog.pack();
        return localeSelectorDialog;
    }

    /**
     * Getter for RegistryBrowser's current Locale. Intended to be used when
     * displaying InternationalString values.
     *
     * @return The currently selected Locale.
     */
    public Locale getSelectedLocale() {
        return getLocaleSelectorDialog().getSelectedLocale();
    }

	public void setLocale(Locale locale) {
		super.setLocale(locale);

		Locale oldLocale = Locale.getDefault();

		Locale.setDefault(locale);

		resourceBundle =
			JAXRResourceBundle.getInstance(locale);

		firePropertyChange(PROPERTY_LOCALE, oldLocale, locale);

		applyComponentOrientation(ComponentOrientation.getOrientation(getLocale()));

        javax.swing.SwingUtilities.updateComponentTreeUI(this);

		// Setting the look and feel is seemingly the only way to get
		// the JOptionPane.showConfirmDialog in exitApplication() to
		// use the correct button text for the new locale.
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (Exception ex) {
		}
	}

    /**
     * Determine whether the user has already authenticated and setCredentials
     * on the Connection or not.
     * Add to JAXR 2.0??
     *
     * @param handler DOCUMENT ME!
     */
    public boolean isAuthenticated() {
        boolean authenticated = false;

        if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
        } else {
            try {
                org.freebxml.omar.client.xml.registry.ConnectionImpl connection = (org.freebxml.omar.client.xml.registry.ConnectionImpl) (RegistryBrowser.client.connection);
                authenticated = connection.isAuthenticated();
            } catch (JAXRException e) {
                displayError(e);
            }
        }

        return authenticated;
    }

    /**
     * Forces authentication to occur.
     *
     */
    public void authenticate() {
        RegistryBrowser.setWaitCursor();

        if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
        } else {
            try {
                org.freebxml.omar.client.xml.registry.ConnectionImpl connection = (org.freebxml.omar.client.xml.registry.ConnectionImpl) (RegistryBrowser.client.connection);
                boolean oldValue = connection.isAuthenticated();

                connection.authenticate();

                boolean newValue = connection.isAuthenticated();

                //Notify listeners of this bound property that it has changed.
                firePropertyChange(PROPERTY_AUTHENTICATED, oldValue, newValue);
            } catch (JAXRException e) {
                displayError(e);
            }
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * Handles logout action from toolbar and logs current user out.
     */
    public void logout() {
        RegistryBrowser.setWaitCursor();

        if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
        } else {
            try {
                org.freebxml.omar.client.xml.registry.ConnectionImpl connection = (org.freebxml.omar.client.xml.registry.ConnectionImpl) (RegistryBrowser.client.connection);
                boolean oldValue = connection.isAuthenticated();

                connection.logoff();

                boolean newValue = connection.isAuthenticated();

                //Notify listeners of this bound property that it has changed.
                firePropertyChange(PROPERTY_AUTHENTICATED, oldValue, newValue);
            } catch (JAXRException e) {
                displayError(e);
            }
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     */
    void showUserRegistrationWizard() {
        RegistryBrowser.setWaitCursor();

        if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
        } else {
            //userRegAction.performAction();
        }

        RegistryBrowser.setDefaultCursor();
    }

    /**
     * DOCUMENT ME!
     *
     * @param url DOCUMENT ME!
     */
    public void connectToRegistry(String url) {
        baseURL = url;
        client.createConnection(url);
    }

    /**
     * Helper method to let browser subcomponents set a wait cursor
     * while performing long operations.
     */
    public static void setWaitCursor() {
        instance.setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.WAIT_CURSOR));
    }

    /**
     * Helper method for browser subcomponents to set the cursor back
     * to its default version.
     */
    public static void setDefaultCursor() {
        instance.setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public static void displayInfo(String message) {
        log.info(message);
        JOptionPane.showMessageDialog(RegistryBrowser.getInstance(), message,
            resourceBundle.getString("message.information.label"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Display common error message about not being connected to the server.
     *
     * @param message the message to display
     */
    public static void displayUnconnectedError() {
		displayError(resourceBundle.getString("message.error.noConnection"));
	}

    /**
     * Display an error message.
     *
     * @param message the message to display
     */
    public static void displayError(String message) {
        log.error(message);
        JOptionPane.showMessageDialog(RegistryBrowser.getInstance(), message,
            resourceBundle.getString("message.error.label"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Method Declaration.
     *
     * @param message
     * @param t
     *
     * @see
     */
    public static void displayError(String message, Throwable t) {
        t.printStackTrace();
        log.error(message, t);

        String msg = t.getMessage();

        if (msg.length() > 200) {
            msg = msg.substring(0, 200);
            msg += resourceBundle.getString("message.seeStderr");
        }

        displayError((message + "\n" + msg));
    }

    /**
     * Method Declaration.
     *
     * @param t
     *
     * @see
     */
    public static void displayError(Throwable t) {
        t.printStackTrace();
        log.error(t);

        String msg = t.getMessage();

        if ((msg != null) && (msg.length() > 200)) {
            msg = msg.substring(0, 200);
            msg += resourceBundle.getString("message.seeStderr");
        }

        displayError(msg);
    }

    /**
     * The entry point for this application. Sets the Look and Feel to
     * the System Look and Feel. Creates a new RegistryBrowser and
     * makes it visible.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        try {
            // By default JDialog and JFrame will not follow theme changes.
            //JDialog.setDefaultLookAndFeelDecorated(true);
            //JFrame.setDefaultLookAndFeelDecorated(true);
			// I18N: Do not localize next statement.
            System.setProperty("sun.awt.noerasebackground","true");
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
			// I18N: Do not localize next statement.
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            
            // Create a new instance of our application's frame, and make it visible.
            RegistryBrowser browser = getInstance();

            browser.pack();

            java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit()
                                                     .getScreenSize();
            browser.setBounds(0, 0, (int) (dim.getWidth()),
                (int) (dim.getHeight()));

            browser.setVisible(true);
        } catch (Throwable t) {
            log.fatal(t);
            t.printStackTrace();

            // Ensure the application exits with an error condition.
            System.exit(1);
        }
    }

    /**
     * Method Declaration.
     *
     * @param doConfirm
     * @param exitStatus
     */
    void exitApplication(boolean doConfirm, int exitStatus) {
        boolean doExit = true;

        if (doConfirm) {
            try {
				System.out.println(getLocale());
				System.out.println(Locale.getDefault());
                // Show a confirmation dialog
                int reply = JOptionPane.showConfirmDialog(this,
                        resourceBundle.getString("message.confirmExit"),
                        resourceBundle.getString("title.registryBrowser"),
						JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                // If the confirmation was affirmative, handle exiting.
                if (reply == JOptionPane.YES_OPTION) {
                    this.setVisible(false); // hide the Frame
                    this.dispose(); // free the system resources
                    exitStatus = 0;
                } else {
                    doExit = false;
                }
            } catch (Exception e) {
            }
        }

        if (doExit) {
            System.exit(exitStatus);
        }
    }

    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void RegistryBrowser_windowClosing(WindowEvent event) {
        // to do: code goes here.
        RegistryBrowser_windowClosing_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void RegistryBrowser_windowClosing_Interaction1(WindowEvent event) {
        try {
            this.exitApplication(true, 0);
        } catch (Exception e) {
        }
    }

    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void saveItem_actionPerformed(ActionEvent event) {
        // to do: code goes here.
        if (RegistryBrowser.client.connection != null) {
            try {
                ((org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl) (RegistryBrowser.client.getBusinessLifeCycleManager())).saveAllObjects();
            } catch (JAXRException e) {
                displayError(e);
            }
        }
    }

    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void exitItem_actionPerformed(ActionEvent event) {
        // to do: code goes here.
        exitItem_actionPerformed_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void exitItem_actionPerformed_Interaction1(ActionEvent event) {
        try {
            this.exitApplication(true, 0);
        } catch (Exception e) {
        }
    }

    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void saveButton_actionPerformed(ActionEvent event) {
        // to do: code goes here.
        saveButton_actionPerformed_Interaction1(event);
    }

    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void saveButton_actionPerformed_Interaction1(ActionEvent event) {
        try {
            // saveFileDialog Show the FileDialog
            saveFileDialog.setVisible(true);
        } catch (Exception e) {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public static String getName(javax.xml.registry.infomodel.RegistryObject ro)
        throws JAXRException {
        try {
            return ((InternationalStringImpl) ro.getName()).getClosestValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public static String getDescription(
        javax.xml.registry.infomodel.RegistryObject ro)
        throws JAXRException {
        try {
            return ((InternationalStringImpl) ro.getDescription()).getClosestValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param registryObject DOCUMENT ME!
     */
    public static void showAuditTrail(
        javax.xml.registry.infomodel.RegistryObject registryObject) {
        AuditableEventsDialog dialog = new AuditableEventsDialog((javax.swing.JFrame) RegistryBrowser.getInstance(),
                false, registryObject);
        dialog.setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param user DOCUMENT ME!
     * @param registryLevel DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public static String getUserName(javax.xml.registry.infomodel.User user,
        int registryLevel) throws JAXRException {
        String userName = "";

        if ((user != null) && (user.getPersonName() != null)) {
            javax.xml.registry.infomodel.PersonName personName = user.getPersonName();

            if (registryLevel == 0) {
                userName = personName.getFullName();
            } else {
                String firstName = personName.getFirstName();
                String middleName = personName.getMiddleName();
                String lastName = personName.getLastName();

                if (firstName != null) {
                    userName = firstName;
                }

                if (middleName != null) {
                    userName += (" " + middleName);
                }

                if (lastName != null) {
                    userName += (" " + lastName);
                }
            }
        }

        return userName;
    }

    // util method -> should be implemented as any other predefined enumeration and moved to proper place
    public static String getEventTypeAsString(int eventType)
        throws JAXRException {
        if (eventType == AuditableEvent.EVENT_TYPE_CREATED) {
            return (resourceBundle.getString("eventType.created"));
        } else if (eventType == AuditableEvent.EVENT_TYPE_DELETED) {
            return (resourceBundle.getString("eventType.deleted"));
        } else if (eventType == AuditableEvent.EVENT_TYPE_DEPRECATED) {
            return (resourceBundle.getString("eventType.deprecated"));
        } else if (eventType == AuditableEvent.EVENT_TYPE_UNDEPRECATED) {
            return (resourceBundle.getString("eventType.undeprecated"));
        } else if (eventType == AuditableEvent.EVENT_TYPE_UPDATED) {
            return (resourceBundle.getString("eventType.updated"));
        } else if (eventType == AuditableEvent.EVENT_TYPE_VERSIONED) {
            return (resourceBundle.getString("eventType.versioned"));
        } else {
            return (resourceBundle.getString("message.unknownEventType"));
        }
    }

	/**
	 * Utility method that checks if obj is an instance of targetType.
	 *
	 * @param obj        Object to check
	 * @param targetType Class type for which to check
	 *
	 * @return true if obj is an instance of targetType
	 *
	 * @throws InvalidRequestException if obj is not an instance of targetType.
	 */
	public static boolean isInstanceOf(Object obj, Class targetType)
		throws InvalidRequestException {
		if (targetType.isInstance(obj)) {
			return true;
		} else {
			Object[] notInstanceOfArgs = {targetType.getName(),
										  obj.getClass().getName()};
			MessageFormat form =
				new MessageFormat(resourceBundle.getString("error.notInstanceOf"));
			throw new InvalidRequestException(form.format(notInstanceOfArgs));
		}
	}

    /**
     * Shows the specified RepostoryItem for the RegistryObject in a Web Browser
     *
     * @param registryObject DOCUMENT ME!
     */
    public static void showRepositoryItem(javax.xml.registry.infomodel.RegistryObject registryObject) {

        javax.activation.DataHandler repositoryItem = null;
        File defaultItemFile       = null;

        try {
            repositoryItem =
            ((javax.xml.registry.infomodel.ExtrinsicObject)registryObject).getRepositoryItem();

            /* server.QueryManager.getContent not implemented correctly
            if (repositoryItem == null) {
                displayInfo("There is no repository item for this object");              
                return;
            } else {
                String url = baseURL.substring(0, baseURL.length()-4) + "rest?interface=QueryManager&method=getRepositoryItem&param-id=" + registryObject.getKey().getId();
                HyperLinker.displayURL(url);
            }
             **/
            String url = baseURL.substring(0, baseURL.length()-4) + "rest?interface=QueryManager&method=getRepositoryItem&param-id=" + registryObject.getKey().getId();
            HyperLinker.displayURL(url);
            
        } catch (JAXRException e) {
            displayError(e);
        }
    }

    /**
     * Shows the specified RegistryObject in a Web Browser
     *
     * @param registryObject DOCUMENT ME!
     */
    public static void showRegistryObject(javax.xml.registry.infomodel.RegistryObject registryObject) {

        javax.activation.DataHandler repositoryItem = null;
        File defaultItemFile       = null;

        try {            
            String url = baseURL.substring(0, baseURL.length()-4) + "rest?interface=QueryManager&method=getRegistryObject&param-id=" + registryObject.getKey().getId();
            HyperLinker.displayURL(url);
        } catch (JAXRException e) {
            displayError(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.14 $
     */
    class SymWindow extends WindowAdapter {
        /**
         * DOCUMENT ME!
         *
         * @param event DOCUMENT ME!
         */
        public void windowClosing(WindowEvent event) {
            Object object = event.getSource();

            if (object == RegistryBrowser.this) {
                RegistryBrowser_windowClosing(event);
            }
        }
    }

    /**
     * Class Declaration.
     *
     * @author
     * @version 1.17, 03/29/00
     *
     * @see
     */
    class SymAction implements ActionListener {
        /**
         * Method Declaration.
         *
         * @param event
         *
         * @see
         */
        public void actionPerformed(ActionEvent event) {
            Object object = event.getSource();

            if (object == saveItem) {
                saveItem_actionPerformed(event);
            } else if (object == exitItem) {
                exitItem_actionPerformed(event);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.14 $
     */
    class JAXRBrowserClassLoader extends ClassLoader {
        /**
         * Creates a new JAXRBrowserClassLoader object.
         *
         * @param parent DOCUMENT ME!
         */
        JAXRBrowserClassLoader(ClassLoader parent) {
            log.info("JAXRBrowserClassLoader: Using parent classloader: " +
                parent);
        }

        /**
         * DOCUMENT ME!
         *
         * @param className DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassNotFoundException DOCUMENT ME!
         */
        protected Class findClass(String className)
            throws ClassNotFoundException {
            log.info("findClass: " + className);

            return super.findClass(className);
        }

        /**
         * DOCUMENT ME!
         *
         * @param className DOCUMENT ME!
         * @param resolve DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassNotFoundException DOCUMENT ME!
         */
        protected Class loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
            log.info("loadClass: " + className + " resolve = " + resolve);

            return super.loadClass(className, resolve);
        }

        /**
         * DOCUMENT ME!
         *
         * @param className DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassNotFoundException DOCUMENT ME!
         */
        public Class loadClass(String className) throws ClassNotFoundException {
            log.info("loadClass: " + className);

            Class clazz;

            try {
                clazz = super.loadClass(className);
            } catch (ClassNotFoundException e) {
                log.error(e);
                clazz = getParent().loadClass(className);
            }

            return clazz;
        }
    }
}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/client/browser/RegistryBrowser.java,v 1.75 2003/08/30 23:09:45 farrukh_najmi Exp $
 *
 * ====================================================================
 */

package com.sun.xml.registry.client.browser;

import com.sun.xml.registry.ebxml.infomodel.InternationalStringImpl;
import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;
import javax.xml.registry.JAXRException;

import javax.xml.registry.infomodel.AuditableEvent;

/**
 * The ebXML Registry Browser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryBrowser extends javax.swing.JFrame {
    
    /** DOCUMENT ME! */
    private static final org.apache.commons.logging.Log log =
    org.apache.commons.logging.LogFactory.getLog(RegistryBrowser.class);
    static JAXRClient client = new JAXRClient();
    static JAXRResourceBundle resourceBundle =
    JAXRResourceBundle.getInstance();
    
    /** Bound Properties. */
    public static String PROPERTY_AUTHENTICATED = "PROPERTY_AUTHENTICATED";
    
    /** DOCUMENT ME! */
    static final String selectAnItem =
    resourceBundle.getString("listBox.enterURL");
    static RegistryBrowser instance;
    
    /** DOCUMENT ME! */
    public ClassLoader classLoader;
    
    /** DOCUMENT ME! */
    java.awt.Color buttonBackground;
    
    /** DOCUMENT ME! */
    ConceptsTreeDialog conceptsTreeDialog = null;
    
    /** A dialog for selecting a Locale for RegistryBrowser. */
    LocaleSelectorDialog localeSelectorDialog = null;
    
    /** DOCUMENT ME! */
    javax.swing.JMenuBar menuBar;
    
    /** DOCUMENT ME! */
    JMenuItem newItem;
    
    /** DOCUMENT ME! */
    JMenuItem openItem;
    
    /** DOCUMENT ME! */
    JMenuItem saveItem;
    
    /** DOCUMENT ME! */
    JMenuItem saveAsItem;
    
    /** DOCUMENT ME! */
    JMenuItem exitItem;
    
    /** DOCUMENT ME! */
    JMenuItem cutItem;
    
    /** DOCUMENT ME! */
    JMenuItem copyItem;
    
    /** DOCUMENT ME! */
    JMenuItem pasteItem;
    
    /** DOCUMENT ME! */
    JMenuItem aboutItem;
    
    // move inside constructor later
    
    /** DOCUMENT ME! */
    FileDialog saveFileDialog    = new FileDialog(this);
    
    /** DOCUMENT ME! */
    FileDialog compositionDialog = new FileDialog(this);
    
    /** DOCUMENT ME! */
    JFileChooser openFileDialog = new JFileChooser();
        
    /** DOCUMENT ME! */
    JPanel tabbedPaneParent = new JPanel();
    
    /** The tabbed pane */
    JBTabbedPane tabbedPane=null;
        
    /** DOCUMENT ME! */
    JPanel topPanel             = new JPanel();
    
    /** DOCUMENT ME! */
    javax.swing.JComboBox registryCombo     = new javax.swing.JComboBox();

    /** TextField to show user name for currently authenticated user */
    JTextField currentUserText = new JTextField();
    
    /** DOCUMENT ME! */
    JPanel toolbarPanel         = new JPanel();
    
    /** DOCUMENT ME! */
    javax.swing.JToolBar discoveryToolBar   = null;
    
    /** DOCUMENT ME! */
    JPanel registryObjectsPanel = new JPanel();
    
    /**
     * Creates a new RegistryBrowser object.
     */
    private RegistryBrowser() {
        instance     = this;
        
        classLoader = getClass().getClassLoader(); //new JAXRBrowserClassLoader(getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        
        try {
            classLoader.loadClass("javax.xml.messaging.Endpoint");
        } catch (ClassNotFoundException e) {
            log.error("Could not find class javax.xml.messaging.Endpoint",
            e);
        }
        
        menuBar = new javax.swing.JMenuBar();
        
        JMenu fileMenu         = new JMenu();
        JMenu editMenu         = new JMenu();
        JMenu helpMenu         = new JMenu();
        javax.swing.JSeparator JSeparator1 = new javax.swing.JSeparator();
        
        newItem                = new JMenuItem();
        openItem               = new JMenuItem();
        saveItem               = new JMenuItem();
        saveAsItem             = new JMenuItem();
        exitItem               = new JMenuItem();
        cutItem                = new JMenuItem();
        copyItem               = new JMenuItem();
        pasteItem              = new JMenuItem();
        aboutItem              = new JMenuItem();
        setJMenuBar(menuBar);
        setTitle("Registry Browser");
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(0, 0));
        
        // Scale window to be centered using 70% of screen
        java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        
        setBounds((int)(dim.getWidth() * .15),
        (int)(dim.getHeight() * .1),
        (int)(dim.getWidth() * .7),
        (int)(dim.getHeight() * .75));
        setVisible(false);
        saveFileDialog.setMode(FileDialog.SAVE);
        saveFileDialog.setTitle("Save");
        compositionDialog.setMode(FileDialog.SAVE);
        compositionDialog.setTitle("Save");
        
        java.awt.GridBagLayout gb = new java.awt.GridBagLayout();
        
        topPanel.setLayout(gb);
        getContentPane().add("North", topPanel);
        
        GridBagConstraints c = new GridBagConstraints();
        
        toolbarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        toolbarPanel.setBounds(0, 0, 488, 29);
        
        discoveryToolBar = createDiscoveryToolBar();
        toolbarPanel.add(discoveryToolBar);
        
        c.gridx          = 0;
        c.gridy          = 0;
        c.gridwidth      = 1;
        c.gridheight     = 1;
        c.weightx        = 0.5;
        c.weighty        = 0.5;
        c.fill           = GridBagConstraints.HORIZONTAL;
        c.anchor         = GridBagConstraints.WEST;
        c.insets         = new java.awt.Insets(0, 0, 0, 0);
        gb.setConstraints(toolbarPanel, c);
        topPanel.add(toolbarPanel);
        
        //Panel containing context info like registry location and user context
        JPanel contextPanel = new JPanel();
        java.awt.GridBagLayout gb1    = new java.awt.GridBagLayout();
        
        contextPanel.setLayout(gb1);
        
        javax.swing.JLabel locationLabel =
        new javax.swing.JLabel(resourceBundle.getString("label.registryLocation"));
        
        // locationLabel.setPreferredSize(new Dimension(80, 23));
        c.gridx          = 0;
        c.gridy          = 0;
        c.gridwidth      = 1;
        c.gridheight     = 1;
        c.weightx        = 0.0;
        c.weighty        = 0.5;
        c.fill           = GridBagConstraints.NONE;
        c.anchor         = GridBagConstraints.WEST;
        c.insets         = new java.awt.Insets(0, 5, 0, 0);
        gb1.setConstraints(locationLabel, c);
        
        // contextPanel.setBackground(Color.green);
        contextPanel.add(locationLabel);
        
        String pathSep = System.getProperty("file.separator");
        
        registryCombo.addItem(selectAnItem);
        
        com.sun.xml.registry.client.browser.conf.bindings.Configuration cfg               = getConfiguration();
        com.sun.xml.registry.client.browser.conf.bindings.RegistryURIList urlList         = cfg.getRegistryURIList();
        String[] urls = urlList.getRegistryURI();
        
        for (int i = 0; i < urls.length; i++) {
            registryCombo.addItem(urls[i]);
        }
        
        registryCombo.setEditable(true);
        registryCombo.setEnabled(true);
        registryCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                String url =
                (String)registryCombo
                .getSelectedItem();
                
                if (url.equals(selectAnItem)) {
                    
                    return;
                }
                
                RegistryBrowser.setWaitCursor();
                connectToRegistry(url);
                
                Thread.currentThread().setContextClassLoader(RegistryBrowser
                .getInstance().classLoader);
                
                conceptsTreeDialog = null;
                ConceptsTreeDialog.clearCache();
                
                try {
                    tabbedPane = new JBTabbedPane();
                    tabbedPaneParent.removeAll();
                    tabbedPaneParent.add(tabbedPane, BorderLayout.CENTER);

                    getRootPane().updateUI();
                }
                catch (JAXRException e1) {
                    displayError(e1);
                }

                                
                RegistryBrowser.setDefaultCursor();
            }
        });
        c.gridx          = 1;
        c.gridy          = 0;
        c.gridwidth      = 1;
        c.gridheight     = 1;
        c.weightx        = 0.9;
        c.weighty        = 0.5;
        c.fill           = GridBagConstraints.HORIZONTAL;
        c.anchor         = GridBagConstraints.CENTER;
        c.insets         = new java.awt.Insets(0, 0, 5, 0);
        gb1.setConstraints(registryCombo, c);
        contextPanel.add(registryCombo);
        
        JLabel currentUserLabel = new JLabel("Current User:", SwingConstants.LEFT);
        c.gridx          = 2;
        c.gridy          = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets         = new java.awt.Insets(0, 5, 5, 0);
        gb1.setConstraints(currentUserLabel, c);
        //contextPanel.add(currentUserLabel);

        currentUserText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String text = currentUserText.getText();
            }
        });
        
        currentUserText.setEditable(false);
        c.gridx          = 3;
        c.gridy          = 0;
        c.gridwidth      = 1;
        c.gridheight     = 1;
        c.weightx        = 0.9;
        c.weighty        = 0.5;
        c.fill           = GridBagConstraints.HORIZONTAL;
        c.anchor         = GridBagConstraints.WEST;
        c.insets         = new java.awt.Insets(0, 0, 5, 5);
        gb1.setConstraints(currentUserText, c);
        //contextPanel.add(currentUserText);        
                
        c.gridx          = 0;
        c.gridy          = 1;
        c.gridwidth      = 1;
        c.gridheight     = 1;
        c.weightx        = 0.9;
        c.weighty        = 0.5;
        c.fill           = GridBagConstraints.HORIZONTAL;
        c.anchor         = GridBagConstraints.CENTER;
        c.insets         = new java.awt.Insets(0, 0, 0, 0);
        gb.setConstraints(contextPanel, c);
        topPanel.add(contextPanel, c);
        
        tabbedPaneParent.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        tabbedPaneParent.setLayout(new BorderLayout());
        tabbedPaneParent.setToolTipText("Select registry location");
        
        getContentPane().add("Center", tabbedPaneParent);
        
        fileMenu.setText(resourceBundle.getString("menu.file"));
        fileMenu.setActionCommand("File");
        fileMenu.setMnemonic((int)'F');
        menuBar.add(fileMenu);
        
        saveItem.setHorizontalTextPosition(SwingConstants.RIGHT);
        saveItem.setText(resourceBundle.getString("menu.file.save"));
        saveItem.setActionCommand("Save");
        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
        java.awt.Event.CTRL_MASK));
        saveItem.setMnemonic((int)'S');
        //fileMenu.add(saveItem);
        
        fileMenu.add(JSeparator1);
        exitItem.setText(resourceBundle.getString("menu.file.exit"));
        exitItem.setActionCommand("Exit");
        exitItem.setMnemonic((int)'X');
        fileMenu.add(exitItem);
        editMenu.setText(resourceBundle.getString("menu.edit"));
        editMenu.setActionCommand("Edit");
        editMenu.setMnemonic((int)'E');
        //menuBar.add(editMenu);
        cutItem.setHorizontalTextPosition(SwingConstants.RIGHT);
        cutItem.setText(resourceBundle.getString("menu.edit.cut"));
        cutItem.setActionCommand("Cut");
        cutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X,
        java.awt.Event.CTRL_MASK));
        cutItem.setMnemonic((int)'T');
        editMenu.add(cutItem);
        copyItem.setHorizontalTextPosition(SwingConstants.RIGHT);
        copyItem.setText(resourceBundle.getString("menu.edit.copy"));
        copyItem.setActionCommand("Copy");
        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,
        java.awt.Event.CTRL_MASK));
        copyItem.setMnemonic((int)'C');
        editMenu.add(copyItem);
        pasteItem.setHorizontalTextPosition(SwingConstants.RIGHT);
        pasteItem.setText(resourceBundle.getString("menu.edit.paste"));
        pasteItem.setActionCommand("Paste");
        pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
        java.awt.Event.CTRL_MASK));
        pasteItem.setMnemonic((int)'P');
        editMenu.add(pasteItem);
        helpMenu.setText(resourceBundle.getString("menu.help"));
        helpMenu.setActionCommand("Help");
        helpMenu.setMnemonic((int)'H');
        menuBar.add(helpMenu);
        aboutItem.setHorizontalTextPosition(SwingConstants.RIGHT);
        aboutItem.setText(resourceBundle.getString("menu.help.about"));
        aboutItem.setActionCommand("About...");
        aboutItem.setMnemonic((int)'A');
        
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(RegistryBrowser.this,
                "ebxmlrr Registry Browser version: 2.1final1",
                "About ebXML Registry Browser",
                JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        helpMenu.add(aboutItem);
        
        // REGISTER_LISTENERS
        SymWindow aSymWindow = new SymWindow();
        
        this.addWindowListener(aSymWindow);
        
        SymAction lSymAction = new SymAction();
        
        saveItem.addActionListener(lSymAction);
        exitItem.addActionListener(lSymAction);
        
        javax.swing.SwingUtilities.updateComponentTreeUI(getContentPane());
        javax.swing.SwingUtilities.updateComponentTreeUI(menuBar);
        javax.swing.SwingUtilities.updateComponentTreeUI(openFileDialog);
        
        //Auto select the registry that is configured to connect to by default
        String selectedIndexStr = com.sun.xml.registry.ebxml.util.ProviderProperties.getInstance().
            getProperty("jaxr-ebxml.registryBrowser.registryLocationCombo.initialSelectionIndex", "0");
        int index = Integer.parseInt(selectedIndexStr);
        try {
            registryCombo.setSelectedIndex(index);
        }
        catch (IllegalArgumentException e) {
            displayError("Invalid property value '" + index + "' for jaxr-ebxml.registryBrowser.registryLocationCombo.initialSelectionIndex. Check jaxr-ebxml.properties file.", e );
        }
                
    }
    
    //UserRegistrationWizardAction userRegAction = new UserRegistrationWizardAction();
    public static RegistryBrowser getInstance() {
        
        if (instance == null) {
            instance = new RegistryBrowser();
        }
        
        return instance;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JAXRClient getClient() {
        
        return client;
    }
        
    /**
     * Action for the Find tool.
     */
    public void findAction() {
        tabbedPane.findAction();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public javax.swing.JToolBar createDiscoveryToolBar() {
        
        javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
        toolBar.setFloatable(true);
        
        // Find
        URL findUrl        =
        getClass().getClassLoader().getResource("icons/find.gif");
        ImageIcon findIcon = new ImageIcon(findUrl);
        JButton findButton =
        toolBar.add(new AbstractAction("", findIcon) {
            public void actionPerformed(ActionEvent e) {
                findAction();
            }
        });
        
        findButton.setToolTipText("Search");
        
        // showSchemes
        URL showSchemesUrl        =
        getClass().getClassLoader().getResource("icons/schemeViewer.gif");
        ImageIcon showSchemesIcon = new ImageIcon(showSchemesUrl);
        JButton showSchemesButton =
        toolBar.add(new AbstractAction("", showSchemesIcon) {
            public void actionPerformed(ActionEvent e) {
                
                if (RegistryBrowser.client.connection == null) {
                    RegistryBrowser.displayError("Connect to a registry by specifying Registry Location first.");
                } else {
                    ConceptsTreeDialog.showSchemes(RegistryBrowser.getInstance(), 
                        false, isAuthenticated());
                }
            }
        });
        
        showSchemesButton.setToolTipText("Show ClassificationScheme/Concept Dialog");
        
        // Re-authenticate
        URL authenticateUrl        =
        getClass().getClassLoader().getResource("icons/authenticate.gif");
        ImageIcon authenticateIcon = new ImageIcon(authenticateUrl);
        JButton authenticateButton =
        toolBar.add(new AbstractAction("", authenticateIcon) {
            public void actionPerformed(ActionEvent e) {
                authenticate();
            }
        });
        
        authenticateButton.setToolTipText("Log on");

        // Logout
        URL logoutUrl        =
        getClass().getClassLoader().getResource("icons/logoff.gif");
        ImageIcon logoutIcon = new ImageIcon(logoutUrl);
        JButton logoutButton =
        toolBar.add(new AbstractAction("", logoutIcon) {
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        logoutButton.setToolTipText("Log off");
        
        
        // user registration
        URL userRegUrl        =
        getClass().getClassLoader().getResource("icons/userReg.gif");
        ImageIcon userRegIcon = new ImageIcon(userRegUrl);
        JButton userRegButton =
        toolBar.add(new AbstractAction("", userRegIcon) {
            public void actionPerformed(ActionEvent e) {
                RegistryBrowser.setWaitCursor();
                
                //showUserRegistrationWizard();
                if (RegistryBrowser.client.connection == null) {
                    RegistryBrowser.displayError("Connect to a registry by specifying Registry Location first.");
                } else {
                    
                    com.sun.xml.registry.client.browser.registration.UserManager userMgr = com.sun.xml.registry.client.browser.registration.UserManager.getInstance();
                    
                    try {
                        userMgr.registerNewUser();
                    } catch (Exception er) {
                        RegistryBrowser.displayError(er);
                    }
                }
                
                RegistryBrowser.setDefaultCursor();
            }
        });
        
        userRegButton.setToolTipText("Show User Registration Wizard");
        
        // locale selection
        URL localeSelUrl        =
        getClass().getClassLoader().getResource("icons/localeSel.gif");
        ImageIcon localeSelIcon = new ImageIcon(localeSelUrl);
        JButton localeSelButton =
        toolBar.add(new AbstractAction("", localeSelIcon) {
            public void actionPerformed(ActionEvent e) {
                RegistryBrowser.setWaitCursor();
                
                // Show LocaleSelectorDialog
                if (RegistryBrowser.client.connection == null) {
                    RegistryBrowser.displayError("Connect to a registry by specifying Registry Location first.");
                } else {
                    LocaleSelectorDialog dialog = getLocaleSelectorDialog();
                    dialog.setVisible(true);
                }
                
                RegistryBrowser.setDefaultCursor();
            }
        });
        localeSelButton.setToolTipText("Show Locale Selection Dialog");
        
        return toolBar;
    }
        
    /**
     * Getter for property localeSelectorDialog. Instantiates a new
     * LocaleSelectorDialog with Locale.getDefault() if property is null.
     *
     * @return value of property localeSelectorDialog.
     */
    public LocaleSelectorDialog getLocaleSelectorDialog() {
        if (localeSelectorDialog == null) {
            RegistryBrowser.setWaitCursor();
            localeSelectorDialog = new LocaleSelectorDialog(java.util.Locale.getDefault(),
            RegistryBrowser.getInstance(), true);
            RegistryBrowser.setDefaultCursor();
        }
        return localeSelectorDialog;
    }
    
    /**
     * Getter for RegistryBrowser's current Locale. Intended to be used when
     * displaying InternationalString values.
     *
     * @return The currently selected Locale.
     */
    public java.util.Locale getSelectedLocale() {
        return getLocaleSelectorDialog().getSelectedLocale();
    }
    
    /**
     * Determine whether the user has already authenticated and setCredentials 
     * on the Connection or not.
     * Add to JAXR 2.0??
     *
     * @param handler DOCUMENT ME!
     */
    public boolean isAuthenticated() {
        boolean authenticated = false;
        
        if (RegistryBrowser.client.connection == null) {
            RegistryBrowser.displayError(resourceBundle.getString("message.error.noConnection"));
        } else {
            
            try {
                com.sun.xml.registry.ebxml.ConnectionImpl connection = 
                    (com.sun.xml.registry.ebxml.ConnectionImpl)(RegistryBrowser.client.connection);
                authenticated = connection.isAuthenticated();                                
            } catch (JAXRException e) {
                displayError(e);
            }
        }
        
        return authenticated;
    }
    
    /**
     * Forces authentication to occur.
     *
     */
    public void authenticate() {
        RegistryBrowser.setWaitCursor();
        
        if (RegistryBrowser.client.connection == null) {
            RegistryBrowser.displayError(resourceBundle.getString("message.error.noConnection"));
        } else {
            
            try {
                com.sun.xml.registry.ebxml.ConnectionImpl connection = 
                    (com.sun.xml.registry.ebxml.ConnectionImpl)(RegistryBrowser.client.connection);
                boolean oldValue = connection.isAuthenticated();
                
                connection.authenticate();
                
                boolean newValue = connection.isAuthenticated();                                
                
                //Notify listeners of this bound property that it has changed.
                firePropertyChange(PROPERTY_AUTHENTICATED, oldValue, newValue);
                
            } catch (JAXRException e) {
                displayError(e);
            }
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * Handles logout action from toolbar and logs current user out.
     */
    public void logout() {
        RegistryBrowser.setWaitCursor();
        
        if (RegistryBrowser.client.connection == null) {
            RegistryBrowser.displayError(resourceBundle.getString("message.error.noConnection"));
        } else {
            
            try {
                com.sun.xml.registry.ebxml.ConnectionImpl connection = 
                    (com.sun.xml.registry.ebxml.ConnectionImpl)(RegistryBrowser.client.connection);
                boolean oldValue = connection.isAuthenticated();
                
                connection.logoff();
                
                boolean newValue = connection.isAuthenticated();
                
                //Notify listeners of this bound property that it has changed.
                firePropertyChange(PROPERTY_AUTHENTICATED, oldValue, newValue);
                
            } catch (JAXRException e) {
                displayError(e);
            }
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     */
    void showUserRegistrationWizard() {
        RegistryBrowser.setWaitCursor();
        
        if (RegistryBrowser.client.connection == null) {
            RegistryBrowser.displayError(resourceBundle.getString("message.error.noConnection"));
        } else {
            
            //userRegAction.performAction();
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param url DOCUMENT ME!
     */
    public void connectToRegistry(String url) {
        client.createConnection(url);
    }
    
    /**
     * Helper method to let browser subcomponents set a wait cursor
     * while performing long operations.
     */
    public static void setWaitCursor() {
        instance.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
    }
    
    /**
     * Helper method for browser subcomponents to set the cursor back
     * to its default version.
     */
    public static void setDefaultCursor() {
        instance.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public static void displayInfo(String message) {
        log.info(message);
        JOptionPane.showMessageDialog(RegistryBrowser.getInstance(),
        message, "Information",
        JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Display an error message.
     *
     * @param message the message to display
     */
    public static void displayError(String message) {
        log.error(message);
        JOptionPane.showMessageDialog(RegistryBrowser.getInstance(),
        message, "Error",
        JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Method Declaration.
     *
     * @param message
     * @param t
     *
     * @see
     */
    public static void displayError(String message, Throwable t) {
        log.error(message, t);
        
        String msg = t.getMessage();
        
        if (msg.length() > 200) {
            msg = msg.substring(0, 200);
            msg += "....See stderr for full message.";
        }
        
        displayError((message + "\n" + msg));
    }
    
    /**
     * Method Declaration.
     *
     * @param t
     *
     * @see
     */
    public static void displayError(Throwable t) {
        log.error(t);
        
        String msg = t.getMessage();
        
        if ((msg != null) && (msg.length() > 200)) {
            msg = msg.substring(0, 200);
            msg += "....See stderr for full message.";
        }
        
        displayError(msg);
    }
    
    /**
     * The entry point for this application. Sets the Look and Feel to
     * the System Look and Feel. Creates a new RegistryBrowser and
     * makes it visible.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        
        try {
            
            // Create a new instance of our application's frame, and make it visible.
            RegistryBrowser browser = getInstance();
            
            browser.pack();
            
            java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            browser.setBounds(0, 0, (int)(dim.getWidth()),
            (int)(dim.getHeight()));
            
            browser.setVisible(true);
        } catch (Throwable t) {
            log.fatal(t);
            t.printStackTrace();
            
            // Ensure the application exits with an error condition.
            System.exit(1);
        }
    }
    
    /**
     * Method Declaration.
     *
     * @param doConfirm
     * @param exitStatus
     */
    void exitApplication(boolean doConfirm, int exitStatus) {
        
        boolean doExit = true;
        
        if (doConfirm) {
            
            try {
                
                // Show a confirmation dialog
                int reply =
                JOptionPane.showConfirmDialog(this,
                resourceBundle
                .getString("message.confirmExit"),
                "Registry Browser",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
                
                // If the confirmation was affirmative, handle exiting.
                if (reply == JOptionPane.YES_OPTION) {
                    this.setVisible(false); // hide the Frame
                    this.dispose(); // free the system resources
                    exitStatus = 0;
                } else {
                    doExit = false;
                }
            } catch (Exception e) {
            }
        }
        
        if (doExit) {
            System.exit(exitStatus);
        }
    }
    
    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void RegistryBrowser_windowClosing(java.awt.event.WindowEvent event) {
        
        // to do: code goes here.
        RegistryBrowser_windowClosing_Interaction1(event);
    }
    
    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void RegistryBrowser_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
        
        try {
            this.exitApplication(true, 0);
        } catch (Exception e) {
        }
    }
    
    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void saveItem_actionPerformed(ActionEvent event) {
        
        // to do: code goes here.
        if (RegistryBrowser.client.connection != null) {
            
            try {
                ((com.sun.xml.registry.ebxml.BusinessLifeCycleManagerImpl)(RegistryBrowser.client
                .getBusinessLifeCycleManager())).saveAllObjects();
            } catch (JAXRException e) {
                displayError(e);
            }
        }
    }
    
    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void exitItem_actionPerformed(ActionEvent event) {
        
        // to do: code goes here.
        exitItem_actionPerformed_Interaction1(event);
    }
    
    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void exitItem_actionPerformed_Interaction1(ActionEvent event) {
        
        try {
            this.exitApplication(true, 0);
        } catch (Exception e) {
        }
    }
    
    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void saveButton_actionPerformed(ActionEvent event) {
        
        // to do: code goes here.
        saveButton_actionPerformed_Interaction1(event);
    }
    
    /**
     * Method Declaration.
     *
     * @param event
     *
     * @see
     */
    void saveButton_actionPerformed_Interaction1(ActionEvent event) {
        
        try {
            
            // saveFileDialog Show the FileDialog
            saveFileDialog.setVisible(true);
        } catch (Exception e) {
        }
    }
    
    /**
     * A method to load the Configuration Object for RegistryBrowser
     * from a XML file. The file should be located at
     * "&lt;jaxr-ebxml.home&gt;/registry-browser-config.xml". If not,
     * then it wil be copied there from classpath
     * ("./conf/config.xml") in order to allow the user to customize
     * the RegistryBrowser.
     *
     * @return DOCUMENT ME!
     */
    public com.sun.xml.registry.client.browser.conf.bindings.Configuration getConfiguration() {
        
        com.sun.xml.registry.client.browser.conf.bindings.Configuration cfg       = null;
        java.io.InputStream is          = null;
        File cfgFile            = null;
        boolean readCfgFromHome = true;
        String jaxrHome         = null;
        
        jaxrHome = com.sun.xml.registry.ebxml.util.ProviderProperties.getInstance().getProperty("jaxr-ebxml.home");

        java.io.BufferedInputStream bis  = null;
        java.io.BufferedOutputStream bos = null;

        try {
            File jaxrHomeDir = new File(jaxrHome);
            if (! jaxrHomeDir.exists()) {
                jaxrHomeDir.mkdir();
            }
            cfgFile = new File(jaxrHomeDir, "registry-browser-config.xml");

            if (!cfgFile.canRead()) {


                URL cfgFileUrl =
                    getClass().getResource("conf/config.xml");
                bis     = new java.io.BufferedInputStream(cfgFileUrl.openStream());
                bos     = new java.io.BufferedOutputStream(new java.io.FileOutputStream(cfgFile));

                byte[] buffer = new byte[1024];
                int bytesRead = 0;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException ioe) {
                readCfgFromHome = false;
                log.warn("Unable to store browser configuration in user home");
//                displayError("Unable to store browser configuration in user home",
//                             ioe);
        } finally {

            if (bis != null) {

                try {
                     bis.close();
                } catch (IOException e) {
                }
            }

            if (bos != null) {

                try {
                    bos.close();
                } catch (IOException e) {
                }
            }
        }

        
        // Now get the configuration
        try {
            
            if (readCfgFromHome) {
                is = new java.io.FileInputStream(cfgFile);
            } else {
                
                URL cfgFileUrl =
                getClass().getResource("conf/config.xml");
                is = cfgFileUrl.openStream();
            }
            
            cfg = com.sun.xml.registry.client.browser.conf.bindings.Configuration.unmarshal(new java.io.InputStreamReader(is));
        } catch (IOException e) {
            displayError("Unable to load browser configuration", e);
        } catch (org.exolab.castor.xml.ValidationException e) {
            displayError("Unable to load browser configuration", e);
        } catch (org.exolab.castor.xml.MarshalException e) {
            displayError("Unable to load browser configuration", e);
        } finally {
            
            if (is != null) {
                
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        
        return cfg;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public static String getName(javax.xml.registry.infomodel.RegistryObject ro) throws JAXRException {
        try {
            return ((InternationalStringImpl)ro.getName()).getClosestValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param ro DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public static String getDescription(javax.xml.registry.infomodel.RegistryObject ro) throws JAXRException {
        try {
            return ((InternationalStringImpl)ro.getDescription()).getClosestValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param registryObject DOCUMENT ME!
     */
    public static void retrieveItem(javax.xml.registry.infomodel.RegistryObject registryObject) {
        
        javax.activation.DataHandler repositoryItem = null;
        File defaultItemFile       = null;
        
        try {
            repositoryItem =
            ((javax.xml.registry.infomodel.ExtrinsicObject)registryObject).getRepositoryItem();
            
            if (repositoryItem == null) {
                displayInfo("There is no repository item for this object");
                
                return;
            } else {
                // TO DO: find better modelling for this. Depending on current
                // Locale existing file migt not be found. Use Slot??
                String fileName = RegistryBrowser.getName(registryObject);
                
                //              to use something like javax.activation.MimetypesFileTypeMap?
                //              String fileType = tranaslateMimetypeToFiletype(((ExtrinsicObject)registryObject).getMimeType());
                defaultItemFile = new File(fileName); // + "." + fileType);
            }
        } catch (JAXRException e) {
            displayError(e);
        }
        
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("Save As...");
        saveChooser.setApproveButtonText("Save");
        saveChooser.setSelectedFile(defaultItemFile);
        
        int returnVal = saveChooser.showDialog(null, null);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
            try {
                
                java.io.InputStream item = repositoryItem.getInputStream();
                File itemFile    = saveChooser.getSelectedFile();
                itemFile.createNewFile();
                
                java.io.FileOutputStream itemFileOut =
                new java.io.FileOutputStream(itemFile);
                
                int size    = item.available();
                byte[] buff = new byte[size];
                
                while (item.read(buff) != -1) {
                    itemFileOut.write(buff);
                }
                
                itemFileOut.flush();
                itemFileOut.close();
            } catch (IOException e) {
                displayError(e);
            }
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param registryObject DOCUMENT ME!
     */
    public static void showAuditTrail(javax.xml.registry.infomodel.RegistryObject registryObject) {
        
        AuditableEventsDialog dialog =
        new AuditableEventsDialog((javax.swing.JFrame)RegistryBrowser
        .getInstance(), false,
        registryObject);
        dialog.setVisible(true);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param user DOCUMENT ME!
     * @param registryLevel DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public static String getUserName(javax.xml.registry.infomodel.User user, int registryLevel) throws JAXRException {
        
        String userName = "";
        
        if ((user != null) && (user.getPersonName() != null)) {
            
            javax.xml.registry.infomodel.PersonName personName = user.getPersonName();
            
            if (registryLevel == 0) {
                userName = personName.getFullName();
            } else {
                
                String firstName  = personName.getFirstName();
                String middleName = personName.getMiddleName();
                String lastName   = personName.getLastName();
                
                if (firstName != null) {
                    userName = firstName;
                }
                
                if (middleName != null) {
                    userName += (" " + middleName);
                }
                
                if (lastName != null) {
                    userName += (" " + lastName);
                }
            }
        }
        
        return userName;
    }
    
    // util method -> should be implemented as any other predefined enumeration and moved to proper place
    public static String getEventTypeAsString(int eventType)
    throws JAXRException {
        
        if (eventType == AuditableEvent.EVENT_TYPE_CREATED) {
            
            return ("Created");
        } else if (eventType == AuditableEvent.EVENT_TYPE_DELETED) {
            
            return ("Deleted");
        } else if (eventType == AuditableEvent.EVENT_TYPE_DEPRECATED) {
            
            return ("Deprecated");
        } else if (eventType == AuditableEvent.EVENT_TYPE_UNDEPRECATED) {
            
            return ("Undeprecated");
        } else if (eventType == AuditableEvent.EVENT_TYPE_UPDATED) {
            
            return ("Updated");
        } else if (eventType == AuditableEvent.EVENT_TYPE_VERSIONED) {
            
            return ("Versioned");
        } else {
            
            return ("Unknown Event Type");
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.75 $
     */
    class SymWindow extends java.awt.event.WindowAdapter {
        
        /**
         * DOCUMENT ME!
         *
         * @param event DOCUMENT ME!
         */
        public void windowClosing(java.awt.event.WindowEvent event) {
            
            Object object = event.getSource();
            
            if (object == RegistryBrowser.this) {
                RegistryBrowser_windowClosing(event);
            }
        }
    }
    
    /**
     * Class Declaration.
     *
     * @author
     * @version 1.17, 03/29/00
     *
     * @see
     */
    class SymAction implements ActionListener {
        
        /**
         * Method Declaration.
         *
         * @param event
         *
         * @see
         */
        public void actionPerformed(ActionEvent event) {
            
            Object object = event.getSource();
            
            if (object == saveItem) {
                saveItem_actionPerformed(event);
            } else if (object == exitItem) {
                exitItem_actionPerformed(event);
            }
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.75 $
     */
    class JAXRBrowserClassLoader extends ClassLoader {
        
        /**
         * Creates a new JAXRBrowserClassLoader object.
         *
         * @param parent DOCUMENT ME!
         */
        JAXRBrowserClassLoader(ClassLoader parent) {
            log.info("JAXRBrowserClassLoader: Using parent classloader: "
            + parent);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param className DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassNotFoundException DOCUMENT ME!
         */
        protected Class findClass(String className)
        throws ClassNotFoundException {
            log.info("findClass: " + className);
            
            return super.findClass(className);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param className DOCUMENT ME!
         * @param resolve DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassNotFoundException DOCUMENT ME!
         */
        protected Class loadClass(String className, boolean resolve)
        throws ClassNotFoundException {
            log.info("loadClass: " + className + " resolve = "
            + resolve);
            
            return super.loadClass(className, resolve);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param className DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassNotFoundException DOCUMENT ME!
         */
        public Class loadClass(String className)
        throws ClassNotFoundException {
            log.info("loadClass: " + className);
            
            Class clazz;
            
            try {
                clazz = super.loadClass(className);
            } catch (ClassNotFoundException e) {
                log.error(e);
                clazz = getParent().loadClass(className);
            }
            
            return clazz;
        }
    }
}
