/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/jaas/LoginModuleManager.java,v 1.6 2004/03/29 03:53:54 psterk Exp $
 * ====================================================================
 */
/*
 * LoginModuleManager.java
 *
 * Created on May 20, 2003, 10:15 PM
 */
package org.freebxml.omar.client.xml.registry.jaas;

import com.sun.security.auth.login.ConfigFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.xml.registry.util.KeystoreUtil;

import java.awt.Frame;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Constructor;

import java.net.URL;

import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import javax.xml.registry.JAXRException;


/**
 *
 * @author  psterk
 */
public class LoginModuleManager {
    private static final int MAX_FIXED_ATTRIBUTES = 25;
    private static final int MAX_BACKUP_LOGIN_FILES = 10;
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator");
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");

    // Default application name. This is used if a bundled authentication
    // application config file does not exist, or if the 
    // LoginModuleManager(String applicationName) was not called
    private static final String DEFAULT_APPLICATION_NAME = "jaxr-ebxml-provider";
    private static final String DEFAULT_LOGIN_MODULE_CLASSNAME = "com.sun.security.auth.module.KeyStoreLoginModule";
    private static final String  DEFAULT_KEYSTORE_FILENAME = "${user.home}${/}jaxr-ebxml{/}security${/}keystore.jks";
    private static final String ROOT_PROPERTY_NAME ="omar";
        
    private static boolean _createLoginFile = false;
    private static boolean _createDefaultLoginFile = false;
    private static boolean _getCallbackHandler = false;
    private static final Log log = LogFactory.getLog(LoginModuleManager.class);

    // cached objects
    private String _applicationName;
    private CallbackHandler _callbackHandler;
    private CallbackHandler _defaultCallbackHandler;
    
    private Properties _bundledProperties;
    private String _bundledCfgFileContents;
    private Properties _securityProps;
    private Frame _parentFrame;
    private Log _parentLog = log;

    /**
     * Default constructor<br>
     * Uses jaxr-ebxml-provider as the default login config application name
     */
    public LoginModuleManager() {       
    }

    /**
     * Alternative constructor<br>
     * The application name is configurable
     *
     * @param applicationName
     *  A String that contains the application name for the login config file
     */
    public LoginModuleManager(String applicationName) {
        if (_applicationName == null) {
            _applicationName = DEFAULT_APPLICATION_NAME;
        } else {
            _applicationName = applicationName;
        }
    }

    /**
     * This method is used to set the parent frame of this class. The
     * reference will be passed to the CallbackHandler implementation to
     * improve the GUI behavior.
     *
     * @param frame
     *   The parent frame used by the CallbackHandler implementation
     */
    public void setParentFrame(Frame frame) {
        _parentFrame = frame;
    }

    /**
     * This method is used to get the parent frame of this class.
     *
     * @retrun
     *   The parent frame used by the CallbackHandler implementation
     */
    public Frame getParentFrame() {
        return _parentFrame;
    }

    /**
     * This method is used to set the parent Log of this class. This reference
     * will be passed to the CallbackHandler implementation to provide more
     * consistent logging.
     *
     * @param log
     *   The parent log used by the CallbackHandler implementation
     */
    public void setParentLog(Log log) {
        _parentLog = log;
    }

    /**
     * This method is used to write the login configuration file required
     * by the LoginContext.  It searches for the java.login.config file in
     * the classpath and writes it to the filesystem.  The LoginContext class
     * will read this file, and instantiate and configure the correct
     * JAAS LoginModules. If the java.login.config file cannot be found, it
     * defaults to the current KeystoreLoginModule.
     *
     * @throws JAXRException
     *  This exception is thrown if the bundled config file is different from
     *  the user config file, and cannot be written to the filesystem. If there
     *  is no bundled config file, this exception is thrown if there is a
     *  problem writing the default config file to the filesystem
     */
    public void createLoginConfigFile() throws JAXRException {
        log.debug("start creating login config file");

        // first look for java.login.config in the classpath
        String bundledCfgFileContents = getBundledCfgFileContents();

        // if java.login.config does not exist, 
        // call createDefaultLoginConfigFile()
        if (bundledCfgFileContents == null) {
            createDefaultLoginConfigFile();
        } else {
            createLoginConfigFileInternal(bundledCfgFileContents);
        }

        log.debug("finish creating login config file");
    }

    /**
     * This method is used to get the application name from the bundled config
     * file.  If this file does not exist, it defaults to 'jaxr-ebxml-provider'
     *
     * @return
     *  A String containing the application name
     */
    public String getApplicationName() {
        log.debug("start getting application name");

        if (_applicationName == null) {
            try { // try to get application from bundled config file             

                String bundledCfgFileContents = getBundledCfgFileContents();

                if (bundledCfgFileContents != null) {
                    _applicationName = getLoginName(bundledCfgFileContents);
                }

                log.info("authentication application name: " +
                    _applicationName);
            } catch (Throwable t) {
                log.warn("problem reading bundled login config file. " +
                    "Use default " + DEFAULT_APPLICATION_NAME, t);
            }

            // if bundled config does not exist or there is a problem loading it
            // use default
            if (_applicationName == null) {
                log.warn("problem reading bundled login config file. " +
                    "Use default " + DEFAULT_APPLICATION_NAME);
                _applicationName = DEFAULT_APPLICATION_NAME;
            }
        }

        log.debug("finish getting application name");

        return _applicationName;
    }

    /**
     * This method is used to set the default CallbackHandler. If the
     * jaxr-ebxml.security.jaas.callbackHandlerClassName property is not set,
     * this default CallbackHandler will be used.
     *
     * @param handler
     *  A javax.security.auth.callback.CallbackHandler implementation
     *  provided by the user
     */
    public void setDefaultCallbackHandler(CallbackHandler handler) {
        _defaultCallbackHandler = handler;
    }

    /**
     * This method is used to get the CallbackHandler from the bundled
     * properties file.  It reads the
     * jaxr-ebxml.security.jaas.callbackHandlerClassName property.
     * If this file or property does not exist, it defaults to
     * com.sun.xml.registry.client.jaas.DialogAuthenticationCallbackHandler.
     *
     * @return
     *  An instance of the CallbackHandler interface
     */
    public CallbackHandler getCallbackHandler() throws JAXRException {
        log.debug("start getting CallbackHandler name");

        if (_callbackHandler == null) {
            Properties properties = getBundledProperties();
            String callbackHandlerClassName = properties.getProperty(ROOT_PROPERTY_NAME +
                    ".security.jaas.callbackHandlerClassName");

            if (callbackHandlerClassName != null) {
                Class clazz = null;

                try {
                    clazz = Class.forName(callbackHandlerClassName);
                } catch (ClassNotFoundException ex) {
                    throw new JAXRException("Could not instantiate " +
                        "CallbackHandler " + callbackHandlerClassName +
                        " Is this class in the classpath?", ex);
                }

                Class[] clazzes = new Class[2];
                clazzes[0] = java.awt.Frame.class;
                clazzes[1] = org.apache.commons.logging.Log.class;

                Constructor constructor = null;

                try {
                    constructor = clazz.getDeclaredConstructor(clazzes);

                    Object[] objs = new Object[2];
                        Frame frame = getParentFrame();
                    if (frame != null) {
                        objs[0] = frame;
                        objs[1] = _parentLog;
                    }
                    _callbackHandler = (CallbackHandler) constructor.newInstance(objs);
                } catch (NoSuchMethodException ex) {
                    log.debug("Could not find constructor that takes a Frame " +
                        "and Log as parameters. Trying default constructor");

                    // use default constructor instead
                    try {
                        _callbackHandler = (CallbackHandler) Class.forName(callbackHandlerClassName)
                                                                  .newInstance();
                    } catch (Throwable t) {
                        throw new JAXRException("Could not instantiate " +
                            "CallbackHandler " + callbackHandlerClassName +
                            " reason", t);
                    }
                } catch (Throwable t) {
                    throw new JAXRException("Could not instantiate " +
                        "CallbackHandler " + callbackHandlerClassName +
                        " Details:" + t.getMessage());
                }
            }

            if (_callbackHandler == null) {
                log.info("Using default CallbackHandler");

                // If set, user default CallbackHandler provided by user
                if (_defaultCallbackHandler != null) {
                    _callbackHandler = _defaultCallbackHandler;
                } else // user our internal default CallbackHandler
                 {
                    _callbackHandler = new DialogAuthenticationCallbackHandler(getParentFrame(),
                            log);
                }
            }

            log.info("CallbackHandler name: " +
                _callbackHandler.getClass().getName());
        }

        log.debug("finish getting CallbackHandler name");

        return _callbackHandler;
    }

    /**
     * This method is used to create the default login configuration file.
     * Currently, the default file is for the
     * com.sun.security.auth.module.KeystoreLoginModule
     *
     * @throws JAXRException
     *  This is thrown if there is a problem writing the default login config
     *  file to the filesystem
     */
    public void createDefaultLoginConfigFile() throws JAXRException {
        log.debug("start creation of default login config file");

        File keystoreFile = KeystoreUtil.getKeystoreFile();
        KeystoreUtil.canReadKeystoreFile(keystoreFile);

        // This property should always be set by java
        String userHomeFileName = System.getProperty("user.home");

        if ((userHomeFileName == null) || (userHomeFileName.length() == 0)) {
            throw new JAXRException("Could not get system property user.home");
        }

        File configFile = new File(userHomeFileName, ".java.login.config");

        if (configFile.exists()) {
            if (configFile.canRead()) {
                Configuration config = ConfigFile.getConfiguration();
                String appName = getApplicationName();
                AppConfigurationEntry[] defaultAppConfigEntries = getReloadedAppConfigurationEntries(ConfigFile.getConfiguration(),
                        configFile.getPath() + ".tmp",
                        getDefaultConfigFileContents(DEFAULT_APPLICATION_NAME +
                            ".tmp"), appName + ".tmp");
                AppConfigurationEntry[] userAppConfigEntries = config.getAppConfigurationEntry(appName);
                boolean isCorrect = checkLoginModules(userAppConfigEntries,
                        defaultAppConfigEntries);

                // if the user has a login config file with the same app name
                // as the default, but the login modules are different, rename
                // the existing user login config file and write the default
                // config file in place of the existing
                if (isCorrect == false) {
                    String userCfgFileName = configFile.getName();
                    log.warn("User login config file does not have the same " +
                        "login modules as the default");
                    renameCfgFile(userCfgFileName, userCfgFileName + ".bak");
                    writeCfgFile(configFile, getDefaultConfigFileContents());
                    config.refresh();
                    log.info("created new login config file: " +
                        configFile.getName());
                } else {
                    log.info("using existing config file: " +
                        configFile.getName());

                    return;
                }
            } else {
                throw new JAXRException("File " + configFile.getAbsolutePath() +
                    " exists but is not readable.");
            }
        } else {
            writeCfgFile(configFile, getDefaultConfigFileContents());
            log.info("created new login config file: " + configFile.getName());
        }

        log.debug("finish creation of default login config file");
    }

    /*************************************************************************
     * private methods
     *************************************************************************/
    private boolean getDebugSetting() {
        boolean debug = false;
        Configuration config = ConfigFile.getConfiguration();
        AppConfigurationEntry[] userAppConfigEntries = config.getAppConfigurationEntry(getApplicationName());

        for (int i = 0; i < userAppConfigEntries.length; i++) {
            Map options = userAppConfigEntries[i].getOptions();
            String debugStr = (String) options.get("debug");

            if (debugStr != null) {
                if (debugStr.equalsIgnoreCase("true")) {
                    debug = true;
                }

                break;
            }
        }

        return debug;
    }

    private String getBundledCfgFilename() {
        URL url = this.getClass().getClassLoader().getResource("java.login.config");
        String fileName = url.getFile();

        return fileName;
    }

    private Properties getBundledProperties() {
        if (_bundledProperties == null) {
            _bundledProperties = new Properties();

            BufferedReader in = null;

            try {
                InputStream propInputStream = this.getClass().getClassLoader()
                                                  .getResourceAsStream("org/freebxml/omar/server/common/omar.properties");

                if (propInputStream != null) {
                    _bundledProperties.load(propInputStream);
                }
            } catch (Throwable t) {
                log.warn("Problem reading bundled omar.properties file");
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        log.warn("could not close input stream", ex);
                    }
                }
            }
        }

        return _bundledProperties;
    }

    private String getBundledCfgFileContents() {
        if (_bundledCfgFileContents == null) {
            BufferedReader in = null;

            try {
                InputStream cfgFileInputStream = this.getClass().getClassLoader()
                                                     .getResourceAsStream("org/freebxml/omar/client/xml/registry/util/jaxr.java.login.config");

                if (cfgFileInputStream != null) {
                    log.info(
                        "found org/freebxml/omar/client/xml/registry/util/jaxr.java.login.config file");

                    StringBuffer sb = new StringBuffer();
                    String line = null;
                    in = new BufferedReader(new InputStreamReader(
                                cfgFileInputStream));

                    while ((line = in.readLine()) != null) {
                        sb.append(line).append(LINE_SEPARATOR);
                    }

                    _bundledCfgFileContents = sb.toString();
                    checkKeystoreOption();
                }
            } catch (IOException ex) {
                log.warn("problem reading bundled login config file. " +
                    "Use default", ex);
                _bundledCfgFileContents = null;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore
                }
            }
        }

        return _bundledCfgFileContents;
    }

    private void createLoginConfigFileInternal(String bundledFileContents)
        throws JAXRException {
        log.debug("start creating login config file - internal");

        try {
            String userCfgFileName = getUserCfgFileName();
            log.info("User's config file name: " + userCfgFileName);

            String userCfgContents = getUserCfgFileContents(userCfgFileName);

            // if the user doesn't have an existing login cfg file, write the
            // bundled one
            if (userCfgContents == null) {
                writeCfgFile(userCfgFileName, bundledFileContents);
            } else {
                // the user has an existing login cfg file. Use this method
                // to compare both files and take appropriate actions
                checkUserCfgFile(userCfgFileName, userCfgContents,
                    bundledFileContents);
            }
        } catch (Throwable t) {
            log.warn("problem reading config file. using default", t);

            // problem reading config file.  Use default
            createDefaultLoginConfigFile();
        }

        log.debug("finish creating login config file - internal");
    }

    private void checkUserCfgFile(String userCfgFileName,
        String userCfgContents, String bundledFileContents)
        throws JAXRException {
        String userLoginName = getLoginName(userCfgContents);
        String bundledLoginName = getLoginName(bundledFileContents);

        // if the login names are the same, check attributes
        if (userLoginName.equalsIgnoreCase(bundledLoginName)) {
            // this method checks that any required attributes are present and
            // that fixed attributes are set according to the settings in the
            // bundled jaxr-ebxml.properties file.            
            Configuration config = ConfigFile.getConfiguration();
            String appName = getApplicationName();
            String tmpAppName = appName + ".tmp";
            AppConfigurationEntry[] bundledAppConfigEntries = getReloadedAppConfigurationEntries(config,
                    userCfgFileName + ".tmp", bundledFileContents, appName);
            AppConfigurationEntry[] userAppConfigEntries = config.getAppConfigurationEntry(appName);
            boolean isCorrect = areUserCfgFileAttributesCorrect(userAppConfigEntries,
                    bundledAppConfigEntries);

            // if the user cfg content has changed, write it to the user cfg
            // file
            if (isCorrect == false) {
                log.warn("User login config file is not correct. Using " +
                    "bundled config file instead");
                renameCfgFile(userCfgFileName, userCfgFileName + ".bak");
                log.info("Renamed " + userCfgFileName + " to .bak file");
                writeCfgFile(userCfgFileName, bundledFileContents);
                ConfigFile.getConfiguration().refresh();
                log.info("created new login file: " + userCfgFileName);
            } else {
                // if the user has a different keystore file in the 
                // jaxr-ebxml.properties file, update the user's config file
                // automatically
                updateUserCfgContents(userAppConfigEntries, userCfgContents,
                    userCfgFileName);
            }
        } else {
            // the existing login name in different than the bundled. So, move 
            // the existing user cfg file to a backup file
            renameCfgFile(userCfgFileName, userCfgFileName + ".bak");
            writeCfgFile(userCfgFileName, bundledFileContents);
        }
    }

    private void renameCfgFile(String fileName, String renamedFileName)
        throws JAXRException {
        try {
            File file = new File(fileName);
            File renamedFile = new File(renamedFileName);

            if (renamedFile.exists()) {
                for (int i = 2; i <= MAX_BACKUP_LOGIN_FILES; i++) {
                    String tempFileName = renamedFileName + i;
                    File tempFile = new File(tempFileName);

                    if (!tempFile.exists()) {
                        file.renameTo(tempFile);
                        log.debug("renaming config file " + fileName + " to " +
                            renamedFileName);

                        break;
                    }
                }
            } else {
                file.renameTo(renamedFile);
            }
        } catch (SecurityException ex) {
            throw new JAXRException(ex);
        }
    }

    private boolean areUserCfgFileAttributesCorrect(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries)
        throws JAXRException {
        boolean isCorrect = false;
        isCorrect = checkLoginModules(userAppConfigEntries,
                bundledAppConfigEntries);

        if (isCorrect == false) {
            return isCorrect;
        }

        isCorrect = checkControlFlag(userAppConfigEntries,
                bundledAppConfigEntries);

        if (isCorrect == false) {
            return isCorrect;
        }

        isCorrect = checkLoginModuleOptions(userAppConfigEntries,
                bundledAppConfigEntries);

        return isCorrect;
    }

    private AppConfigurationEntry[] getReloadedAppConfigurationEntries(
        Configuration config, String cfgFileName, String cfgFileContents,
        String appConfigName) throws JAXRException {
        AppConfigurationEntry[] appConfigEntries = null;

        // if there is an IOException, we do not have permission to write
        // to the local filesystem.  Without this permission, we cannot
        // control the authentication.  In this case, throw new 
        // JAXRException to notify the user to give us permission
        try {
            File file = new File(cfgFileName);
            writeCfgFile(file, cfgFileContents);
        } catch (Throwable t) {
            throw new JAXRException("This application does not have " +
                "permission to write to the local filesystem. Please " +
                "provide this permission in your java.policy file. Go to " +
                "http://java.sun.com and search for 'java.policy' for " +
                "details on setting permissions", t);
        }

        String javaSecLoginCfg = System.getProperty(
                "java.security.auth.login.config");
        String userCfgFileName = getUserCfgFileName();
        System.setProperty("java.security.auth.login.config", cfgFileName);
        config.refresh();
        appConfigEntries = config.getAppConfigurationEntry(appConfigName);

        try {
            deleteCfgFile(cfgFileName);
        } catch (Throwable t) {
            log.warn("problem deleting config file: ", t);
        } finally {
            if (javaSecLoginCfg != null) {
                System.setProperty("java.security.auth.login.config",
                    javaSecLoginCfg);
            } else {
                System.setProperty("java.security.auth.login.config",
                    userCfgFileName);
            }

            config.refresh();
        }

        return appConfigEntries;
    }

    /*
     * The login module names in the user's config file must appear in the
     * same order as the bundled config file
     */
    private boolean checkLoginModules(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries) {
        boolean isCorrect = false;

        for (int i = 0; i < bundledAppConfigEntries.length; i++) {
            isCorrect = true;

            try { // user login modules must appear in the same order as the
                  // bundled ones

                String bundledLoginModuleName = bundledAppConfigEntries[i].getLoginModuleName();
                String userLoginModuleName = userAppConfigEntries[i].getLoginModuleName();

                if (!bundledLoginModuleName.equals(userLoginModuleName)) {
                    isCorrect = false;

                    break;
                }
            } catch (Throwable t) {
                // If the user config file has missing login module, it will
                // be caught here
                isCorrect = false;

                break;
            }
        }

        if (isCorrect) {
            log.debug(
                "login module(s) in the existing login config file are ok");
        } else {
            log.warn("The login module(s) contained in the existing " +
                "login config file do not appear in the " +
                "same order as the modules in the bundled config file");
        }

        return isCorrect;
    }

    private boolean checkControlFlag(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries) {
        boolean isCorrect = true;

        for (int i = 0; i < bundledAppConfigEntries.length; i++) {
            try {
                AppConfigurationEntry.LoginModuleControlFlag bundledFlag = bundledAppConfigEntries[i].getControlFlag();
                String bundledFlagStr = bundledFlag.toString();
                AppConfigurationEntry.LoginModuleControlFlag userFlag = userAppConfigEntries[i].getControlFlag();
                String userFlagStr = userFlag.toString();

                if (!bundledFlagStr.equals(userFlagStr)) {
                    isCorrect = false;

                    break;
                }
            } catch (Throwable t) {
                isCorrect = false;

                break;
            }
        }

        if (isCorrect) {
            log.debug(
                "control flag(s) in the existing login config file are ok");
        } else {
            log.warn("The control flags contained in the existing " +
                "login config file do not match those in the " +
                "bundled config file");
        }

        return isCorrect;
    }

    private boolean checkLoginModuleOptions(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries) {
        boolean isCorrect = true;

        for (int i = 0; i < bundledAppConfigEntries.length; i++) {
            Map userOptions = userAppConfigEntries[i].getOptions();
            Map bundledOptions = bundledAppConfigEntries[i].getOptions();
            isCorrect = doAllUserOptionExist(userOptions, bundledOptions);

            if (isCorrect == false) {
                break; // problem with options; stop checking
            }

            String loginModuleName = bundledAppConfigEntries[i].getLoginModuleName();
            isCorrect = areAllUserOptionsSetToCorrectValues(userOptions,
                    loginModuleName);

            if (isCorrect == false) {
                break; // problem with options; stop checking
            }
        }

        return isCorrect;
    }

    private boolean doAllUserOptionExist(Map userOptions, Map bundledOptions) {
        boolean isCorrect = true;
        Iterator bundledOptionsIter = bundledOptions.keySet().iterator();

        while (bundledOptionsIter.hasNext()) {
            String bundledOption = (String) bundledOptionsIter.next();
            String userOption = (String) userOptions.get(bundledOption);

            if (userOption == null) {
                log.warn("The following option is missing " +
                    "in the existing login config file: " + userOption);
                isCorrect = false;

                break;
            }
        }

        if (isCorrect) {
            log.debug("All options exist in the existing login config file");
        }

        return isCorrect;
    }

    private boolean areAllUserOptionsSetToCorrectValues(Map userOptions,
        String loginModuleFullName) {
        boolean isCorrect = true;
        int lastPeriodIndex = loginModuleFullName.lastIndexOf('.');
        String loginModuleName = loginModuleFullName.substring(lastPeriodIndex +
                1, loginModuleFullName.length());
        Properties properties = getBundledProperties();

        if (properties == null) {
            return isCorrect;
        }

        String partialAttributeKey = ROOT_PROPERTY_NAME + ".security.jaas." +
            loginModuleName + ".attribute.";
        String partialFixedKey = ROOT_PROPERTY_NAME + ".security.jaas." +
            loginModuleName + ".fixedValue.";

        for (int j = 1; j <= MAX_FIXED_ATTRIBUTES; j++) {
            String attributeKey = partialAttributeKey + j;
            String attributeValue = null;

            try {
                attributeValue = properties.getProperty(attributeKey);
            } catch (MissingResourceException ex) {
                // ignore - try to load attribute.1 through
                // attribute.MAX_FIXED_ATTRIBUTES
            }

            if (attributeValue != null) {
                String fixedKey = partialFixedKey + j;
                String fixedValue = null;

                try {
                    fixedValue = properties.getProperty(fixedKey);
                } catch (MissingResourceException ex) {
                    // ignore - try to load fixedValue.1 through
                    // fixedValue.MAX_FIXED_ATTRIBUTES
                }

                if (fixedValue != null) {
                    String optionValue = (String) userOptions.get(attributeValue);

                    if ((optionValue == null) ||
                            !optionValue.equalsIgnoreCase(fixedValue)) {
                        // integrity check has failed
                        // break and return 'false'
                        log.warn("The following option is not set properly: " +
                            attributeValue);
                        log.warn("It is set to: " + optionValue);
                        log.warn("It should be set to: " + fixedValue);
                        isCorrect = false;

                        break;
                    }
                }
            }
        }

        if (isCorrect == true) {
            log.debug("all user config file options are set properly");
        }

        return isCorrect;
    }

    private Properties getSecurityProperties() {
        if (_securityProps == null) {
            _securityProps = new Properties();

            BufferedInputStream bis = null;
            String fileName = null;

            try {
                String javaHome = System.getProperty("java.home");
                String dirSep = System.getProperty("file.separator");
                StringBuffer sb = new StringBuffer(javaHome);
                sb.append(dirSep).append("lib").append(dirSep);
                sb.append("security").append(dirSep);
                sb.append("java.security");
                fileName = sb.toString();
                log.info("Found java.security properties: " + fileName);

                File file = new File(fileName);
                bis = new BufferedInputStream(new FileInputStream(file));
                _securityProps.load(bis);
            } catch (IOException ex) {
                log.warn("could not open java.security properties file. Ignore.",
                    ex);
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException ex) {
                    // ignore
                }
            }
        }

        return _securityProps;
    }

    private String getLoginName(String cfgFileContents) {
        int firstSpaceIndex = cfgFileContents.indexOf(' ');

        return (cfgFileContents.substring(0, firstSpaceIndex));
    }

    private void deleteCfgFile(String cfgFile) throws JAXRException {
        try {
            File file = new File(cfgFile);
            boolean isDeleted = file.delete();

            if (isDeleted == false) {
                System.out.println("warning: could not delete tmp file");
            }
        } catch (Throwable t) {
            throw new JAXRException(t);
        }
    }

    private void writeCfgFile(File configFile, String cfgFileContents)
        throws JAXRException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(cfgFileContents, 0, cfgFileContents.length());
            writer.flush();
        } catch (IOException ex) {
            throw new JAXRException(ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    private void writeCfgFile(String userCfgFileName, String fileContents)
        throws JAXRException {
        BufferedWriter writer = null;

        try {
            File file = new File(userCfgFileName);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(fileContents, 0, fileContents.length());
            writer.flush();
        } catch (IOException ex) {
            throw new JAXRException(ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    private String getUserCfgFileName() {
        String userCfgFileName = null;

        try {
            Properties securityProps = getSecurityProperties();

            // Ignore the login.configuration.provider property in this
            // release. Alternative provider implementations can be supported
            // in subsequent releases.
            // We do not support login.config.url.1 in this release
            userCfgFileName = System.getProperty(
                    "java.security.auth.login.config");

            if ((userCfgFileName == null) || userCfgFileName.equals("")) {
                userCfgFileName = getDefaultUserCfgFileName();
            }
        } catch (Throwable t) {
            log.warn("problem getting user config file: ", t);
            userCfgFileName = getDefaultUserCfgFileName();
        }

        return userCfgFileName;
    }

    private String getDefaultUserCfgFileName() {
        StringBuffer sb = new StringBuffer(System.getProperty("user.home"));
        sb.append(System.getProperty("file.separator"));
        sb.append(".java.login.config");

        return (sb.toString());
    }

    private String getUserCfgFileContents(String userHomeFileName)
        throws JAXRException {
        String fileContents = null;
        BufferedReader in = null;

        try {
            File configFile = new File(userHomeFileName);

            if (configFile.exists() == true) {
                StringBuffer sb2 = new StringBuffer();
                String line = null;
                in = new BufferedReader(new FileReader(configFile));

                while ((line = in.readLine()) != null) {
                    sb2.append(line).append(LINE_SEPARATOR);
                }

                fileContents = sb2.toString();
            }

            log.debug("user config file contents: " + fileContents);

            return fileContents;
        } catch (IOException ex) {
            throw new JAXRException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    private String getDefaultConfigFileContents() {
        return getDefaultConfigFileContents(DEFAULT_APPLICATION_NAME);
    }

    private String getDefaultConfigFileContents(String appName) {
        String defaultConfigFileContents = null;

        if (appName == null) {
            appName = DEFAULT_APPLICATION_NAME;
        }

        String keystoreFileName = DEFAULT_KEYSTORE_FILENAME;

        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.canReadKeystoreFile(keystoreFile);
            keystoreFileName = keystoreFile.toURL().getFile();
        } catch (Throwable ex) {
            // Since keystoreFileName is already set to default, ignore
        }

        StringBuffer sb = new StringBuffer(appName);
        sb.append(" { " + LINE_SEPARATOR);
        sb.append("    com.sun.security.auth.module.KeyStoreLoginModule ");
        sb.append("required ").append(LINE_SEPARATOR);
        sb.append("    debug=true keyStoreURL=\"file:");
        sb.append(keystoreFileName).append("\";");
        sb.append(LINE_SEPARATOR).append("};").append(LINE_SEPARATOR);
        defaultConfigFileContents = sb.toString();
        log.debug("Default config file contents: " + defaultConfigFileContents);

        return defaultConfigFileContents;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                String argFlag = args[i];

                if (argFlag.equals("-d")) {
                    _createDefaultLoginFile = true;
                } else if (argFlag.equals("-l")) {
                    _createLoginFile = true;
                } else if (argFlag.equals("-c")) {
                    _getCallbackHandler = true;
                } else { // if no flags execute all methods
                    _createDefaultLoginFile = true;
                    _createLoginFile = true;
                    _getCallbackHandler = true;
                }
            } catch (RuntimeException ex) {
                // use defaults
            }
        }

        if (args.length == 0) {
            _createLoginFile = true;
        }

        LoginModuleManager loginModuleMgr = new LoginModuleManager();

        try {
            if (_createDefaultLoginFile) {
                log.info("starting createDefaultLoginConfigFile");
                loginModuleMgr.createDefaultLoginConfigFile();
            }

            if (_createLoginFile) {
                log.info("starting createLoginConfigFile");
                loginModuleMgr.createLoginConfigFile();
            }

            if (_getCallbackHandler) {
                log.info("starting getCallbackHandler");
                loginModuleMgr.getCallbackHandler();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void updateUserCfgContents(
        AppConfigurationEntry[] userAppConfigEntries, String userCfgContents,
        String cfgFileName) {
        for (int i = 0; i < userAppConfigEntries.length; i++) {
            Map userOptions = userAppConfigEntries[i].getOptions();
            String userKeystoreFile = (String) userOptions.get("keyStoreURL");

            if (userKeystoreFile != null) {
                try {
                    File keystoreFile = KeystoreUtil.getKeystoreFile();
                    KeystoreUtil.canReadKeystoreFile(keystoreFile);

                    String keystoreFileInPropFile = keystoreFile.toURL()
                                                                .getFile();

                    if (keystoreFileInPropFile != null) {
                        if (!userKeystoreFile.equals(keystoreFileInPropFile)) {
                            String keyStoreURL = new String(
                                    "keyStoreURL=\"file:");
                            int keyStartIndex = userCfgContents.indexOf(keyStoreURL);
                            keyStartIndex += keyStoreURL.length();

                            int keyEndIndex = userCfgContents.indexOf("\";",
                                    keyStartIndex);

                            String firstPart = userCfgContents.substring(0,
                                    keyStartIndex);
                            String lastPart = userCfgContents.substring(keyEndIndex);

                            // combine all parts
                            StringBuffer sb = new StringBuffer(firstPart);
                            sb.append(keystoreFileInPropFile).append(lastPart);
                            userCfgContents = sb.toString();
                            writeCfgFile(cfgFileName, userCfgContents);
                        }
                    }
                } catch (Throwable t) {
                    // ignore
                }
            }
        }
    }

    private void checkKeystoreOption() {
        String keyStoreURL = new String("keyStoreURL=\"file:");
        int keyStartIndex = _bundledCfgFileContents.indexOf(keyStoreURL);
        keyStartIndex += keyStoreURL.length();

        int keyEndIndex = _bundledCfgFileContents.indexOf("\";", keyStartIndex);

        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.canReadKeystoreFile(keystoreFile);

            String firstPart = _bundledCfgFileContents.substring(0,
                    keyStartIndex);
            String keystoreFileName = keystoreFile.toURL().getFile();
            String lastPart = _bundledCfgFileContents.substring(keyEndIndex);

            // combine all parts
            StringBuffer sb = new StringBuffer(firstPart);
            sb.append(keystoreFileName).append(lastPart);
            _bundledCfgFileContents = sb.toString();
        } catch (Throwable ex) {
            // ignore - use existing _bundledCfgFileContents
        }
    }
}
/*
 * LoginModuleManager.java
 *
 * Created on May 20, 2003, 10:15 PM
 */

package com.sun.xml.registry.ebxml.jaas;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.*;
import java.awt.Frame;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.registry.JAXRException;

import com.sun.xml.registry.ebxml.util.KeystoreUtil;

import com.sun.security.auth.callback.DialogCallbackHandler;
import com.sun.security.auth.login.ConfigFile;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author  psterk
 */
public class LoginModuleManager
{
    private static final int MAX_FIXED_ATTRIBUTES = 25;
    private static final int MAX_BACKUP_LOGIN_FILES = 10;
    private static final String LINE_SEPARATOR = 
        System.getProperty("line.separator");
    private static final String FILE_SEPARATOR = 
        System.getProperty("file.separator");
    // Default application name. This is used if a bundled authentication
    // application config file does not exist, or if the 
    // LoginModuleManager(String applicationName) was not called
    private static final String DEFAULT_APPLICATION_NAME = 
        "jaxr-ebxml-provider";                     
    private static final String DEFAULT_LOGIN_MODULE_CLASSNAME =
        "com.sun.security.auth.module.KeyStoreLoginModule";
    private static final String DEFAULT_KEYSTORE_FILENAME =
        "${user.home}${/}jaxr-ebxml${/}security${/}keystore.jks";
    private static boolean _createLoginFile = false;
    private static boolean _createDefaultLoginFile = false;
    private static boolean _getCallbackHandler = false;
    

    // cached objects
    private String _applicationName;
    private CallbackHandler _callbackHandler;
    private CallbackHandler _defaultCallbackHandler;
    private String _propertiesName = "jaxr-ebxml";
    private Properties _bundledProperties;
    private String _bundledCfgFileContents;
    private Properties _securityProps;
    
    private static final Log log = LogFactory.getLog(LoginModuleManager.class);
    
    private Frame _parentFrame;
    private Log _parentLog = log;

    /** 
     * Default constructor<br>
     * Uses jaxr-ebxml-provider as the default login config application name
     */
    public LoginModuleManager()
    {
        
    }
    
    /**
     * Alternative constructor<br>
     * The application name is configurable
     *
     * @param applicationName
     *  A String that contains the application name for the login config file
     */
    public LoginModuleManager(String applicationName)
    {
        if (_applicationName == null)
        {
            _applicationName = DEFAULT_APPLICATION_NAME;
        }
        else
        {
            _applicationName = applicationName;
        }
    }
    
   /**
    * This method is used to set the parent frame of this class. The 
    * reference will be passed to the CallbackHandler implementation to 
    * improve the GUI behavior.
    *
    * @param frame
    *   The parent frame used by the CallbackHandler implementation
    */ 
    public void setParentFrame(Frame frame)
    {
        _parentFrame = frame;
    }

   /**
    * This method is used to get the parent frame of this class. 
    *
    * @retrun 
    *   The parent frame used by the CallbackHandler implementation
    */
    public Frame getParentFrame()
    {
        if (_parentFrame == null)
        {
            _parentFrame = new Frame();
        }
        return _parentFrame;
    }

   /**
    * This method is used to set the parent Log of this class. This reference
    * will be passed to the CallbackHandler implementation to provide more 
    * consistent logging.
    *
    * @param log
    *   The parent log used by the CallbackHandler implementation
    */
    public void setParentLog(Log log)
    {
        _parentLog = log;
    }

    /**
     * This method is used to write the login configuration file required 
     * by the LoginContext.  It searches for the java.login.config file in
     * the classpath and writes it to the filesystem.  The LoginContext class
     * will read this file, and instantiate and configure the correct 
     * JAAS LoginModules. If the java.login.config file cannot be found, it 
     * defaults to the current KeystoreLoginModule.
     *
     * @throws JAXRException
     *  This exception is thrown if the bundled config file is different from
     *  the user config file, and cannot be written to the filesystem. If there
     *  is no bundled config file, this exception is thrown if there is a 
     *  problem writing the default config file to the filesystem
     */
    public void createLoginConfigFile() throws JAXRException
    {
        log.debug("start creating login config file");
        // first look for java.login.config in the classpath
        String bundledCfgFileContents = getBundledCfgFileContents();

        // if java.login.config does not exist, 
        // call createDefaultLoginConfigFile()
        if (bundledCfgFileContents == null)
        {
            createDefaultLoginConfigFile();
        }
        else
        {
            createLoginConfigFileInternal(bundledCfgFileContents);
        }
        log.debug("finish creating login config file");
    }
    
    /**
     * This method is used to get the application name from the bundled config
     * file.  If this file does not exist, it defaults to 'jaxr-ebxml-provider'
     *
     * @return
     *  A String containing the application name
     */
    public String getApplicationName()
    {
        log.debug("start getting application name");
        if (_applicationName == null)
        {
            try 
            {   // try to get application from bundled config file             
                String bundledCfgFileContents = getBundledCfgFileContents();
                if (bundledCfgFileContents != null)
                {
                    _applicationName = getLoginName(bundledCfgFileContents);
                }
                log.info("authentication application name: "+_applicationName);
            }
            catch (Throwable t)
            {
                log.warn("problem reading bundled login config file. "+
                    "Use default "+DEFAULT_APPLICATION_NAME, t);
            }
            // if bundled config does not exist or there is a problem loading it
            // use default
            if (_applicationName == null)
            {
                log.warn("problem reading bundled login config file. "+
                    "Use default "+DEFAULT_APPLICATION_NAME);
                _applicationName = DEFAULT_APPLICATION_NAME; 
            }
        }
        log.debug("finish getting application name");
        return _applicationName;
    }
    
   /**
    * This method is used to set the default CallbackHandler. If the 
    * jaxr-ebxml.security.jaas.callbackHandlerClassName property is not set,
    * this default CallbackHandler will be used.
    *
    * @param handler
    *  A javax.security.auth.callback.CallbackHandler implementation
    *  provided by the user
    */
    public void setDefaultCallbackHandler(CallbackHandler handler)
    {
        _defaultCallbackHandler = handler;
    }

    /**
     * This method is used to get the CallbackHandler from the bundled
     * properties file.  It reads the 
     * jaxr-ebxml.security.jaas.callbackHandlerClassName property.
     * If this file or property does not exist, it defaults to
     * com.sun.xml.registry.client.jaas.DialogAuthenticationCallbackHandler.
     * 
     * @return
     *  An instance of the CallbackHandler interface
     */
    public CallbackHandler getCallbackHandler() throws JAXRException
    {
        log.debug("start getting CallbackHandler name");
        if (_callbackHandler == null)
        {
            
            Properties properties = getBundledProperties();
            String callbackHandlerClassName = properties.getProperty(
                _propertiesName+".security.jaas.callbackHandlerClassName");
            if (callbackHandlerClassName != null)
            {
                Class clazz = null;
                try
                {
                    clazz = Class.forName(callbackHandlerClassName);
                }
                catch (ClassNotFoundException ex)
                {
                    throw new JAXRException("Could not instantiate "+
                        "CallbackHandler "+callbackHandlerClassName+
                        " Is this class in the classpath?", ex);
                }
                Class[] clazzes = new Class[2];
                clazzes[0] = java.awt.Frame.class;
                clazzes[1] = org.apache.commons.logging.Log.class;
                Constructor constructor = null;
                try
                {
                    constructor = clazz.getDeclaredConstructor(clazzes); 
                    Object[] objs = new Object[2];
                    objs[0] = getParentFrame();
                    objs[1] = _parentLog;
                    _callbackHandler = 
                        (CallbackHandler)constructor.newInstance(objs);
                }
                catch (NoSuchMethodException ex)
                {
                    log.debug("Could not find constructor that takes a Frame "+
                        "and Log as parameters. Trying default constructor");
                    // use default constructor instead
                    try
                    {
                        _callbackHandler = (CallbackHandler)Class.
                            forName(callbackHandlerClassName).newInstance();
                    }
                    catch (Throwable t)
                    {
                        throw new JAXRException("Could not instantiate "+
                            "CallbackHandler "+callbackHandlerClassName+
                            " reason", t);
                    }
                }
                catch (Throwable t)
                {
                    throw new JAXRException("Could not instantiate "+
                        "CallbackHandler "+callbackHandlerClassName+" Details:"+
                        t.getMessage());
                }
            }
            if (_callbackHandler == null)
            {
                log.info("Using default CallbackHandler");
                // If set, user default CallbackHandler provided by user
                if (_defaultCallbackHandler != null)
                {
                    _callbackHandler = _defaultCallbackHandler;
                }
                else // user our internal default CallbackHandler
                {
                    _callbackHandler = new DialogAuthenticationCallbackHandler(
                        getParentFrame(), log);
                }
            }
            log.info("CallbackHandler name: "+ _callbackHandler.getClass().getName());
        }
        log.debug("finish getting CallbackHandler name");
        return _callbackHandler;
    }
    
    /**
     * This method is used to create the default login configuration file.
     * Currently, the default file is for the 
     * com.sun.security.auth.module.KeystoreLoginModule
     * 
     * @throws JAXRException
     *  This is thrown if there is a problem writing the default login config
     *  file to the filesystem
     */
    public void createDefaultLoginConfigFile() throws JAXRException 
    {
        log.debug("start creation of default login config file");
        File keystoreFile = KeystoreUtil.getKeystoreFile();
        KeystoreUtil.canReadKeystoreFile(keystoreFile);
        // This property should always be set by java
        String userHomeFileName = System.getProperty("user.home");
        if ((userHomeFileName == null)
            || (userHomeFileName.length() == 0)) 
        {
            throw new JAXRException("Could not get system property user.home");
        }
        File configFile = new File(userHomeFileName, ".java.login.config");
        if (configFile.exists()) 
        {
            if (configFile.canRead()) 
            {  
                Configuration config = ConfigFile.getConfiguration();
                String appName = getApplicationName();
                AppConfigurationEntry[] defaultAppConfigEntries = 
                    getReloadedAppConfigurationEntries(
                        ConfigFile.getConfiguration(), 
                        configFile.getPath()+".tmp",
                        getDefaultConfigFileContents(DEFAULT_APPLICATION_NAME+
                            ".tmp"),
                        appName+".tmp");
                AppConfigurationEntry[] userAppConfigEntries = 
                    config.getAppConfigurationEntry(appName);
                boolean isCorrect = checkLoginModules(userAppConfigEntries, 
                    defaultAppConfigEntries);
                
                // if the user has a login config file with the same app name
                // as the default, but the login modules are different, rename
                // the existing user login config file and write the default
                // config file in place of the existing
                if (isCorrect == false)
                {
                    String userCfgFileName = configFile.getName();
                    log.warn("User login config file does not have the same "+
                        "login modules as the default");
                    renameCfgFile(userCfgFileName, userCfgFileName+".bak");
                    writeCfgFile(configFile, getDefaultConfigFileContents());
                    config.refresh();
                    log.info("created new login config file: "+configFile.getName());
                }
                else
                {
                    log.info("using existing config file: "+
                        configFile.getName());
                    return;
                }
            } 
            else 
            {
                throw new JAXRException("File "
                                        + configFile.getAbsolutePath()
                                        + " exists but is not readable.");
            }
        }  
        else
        {
            writeCfgFile(configFile, getDefaultConfigFileContents());
            log.info("created new login config file: "+configFile.getName());
        }
        log.debug("finish creation of default login config file");
    }
    
    /*************************************************************************
     * private methods
     *************************************************************************/
    private boolean getDebugSetting()
    {
        boolean debug = false;
        Configuration config = ConfigFile.getConfiguration();
        AppConfigurationEntry[] userAppConfigEntries = 
            config.getAppConfigurationEntry(getApplicationName());
        for (int i = 0; i < userAppConfigEntries.length; i++)
        {
            Map options = userAppConfigEntries[i].getOptions();
            String debugStr = (String)options.get("debug");
            if (debugStr != null)
            {
                if (debugStr.equalsIgnoreCase("true"))
                {
                    debug = true;
                }
                break;
            }
        }
        return debug;
    }
    
    private String getBundledCfgFilename()
    {
        URL url = this.getClass().getClassLoader().
            getResource("java.login.config");
        String fileName = url.getFile();
        return fileName;
    }
  
    private Properties getBundledProperties()
    {
        if (_bundledProperties == null)
        {
            _bundledProperties = new Properties();
            BufferedReader in = null;
            try 
            {
                InputStream propInputStream = this.getClass().
                        getClassLoader().getResourceAsStream(
                        "com/sun/xml/registry/ebxml/util/jaxr-ebxml-defaults.properties");
                if (propInputStream != null)
                {
                    _bundledProperties.load(propInputStream);
                }
            }
            catch (Throwable t)
            {
                log.warn("Problem reading bundled jaxr-ebxml.properties file");
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException ex)
                    {
                        log.warn("could not close input stream", ex);
                    }
                }
            }
        }
        return _bundledProperties;
    }
    
    private String getBundledCfgFileContents()
    {
        if (_bundledCfgFileContents == null)
        {
            BufferedReader in = null;
            try 
            {
                InputStream cfgFileInputStream = this.getClass().
                    getClassLoader().getResourceAsStream(
                    "com/sun/xml/registry/ebxml/util/jaxr.java.login.config");
                if (cfgFileInputStream != null)
                {
                    log.info("found com/sun/xml/registry/ebxml/util/jaxr.java.login.config file");
                    StringBuffer sb = new StringBuffer();
                    String line = null;
                    in = new BufferedReader(new InputStreamReader(
                        cfgFileInputStream));
                    while ((line = in.readLine()) != null)
                    {
                        sb.append(line).append(LINE_SEPARATOR);
                    }
                    _bundledCfgFileContents = sb.toString();
                    checkKeystoreOption();
                }
            
            }
            catch (IOException ex)
            {
                log.warn("problem reading bundled login config file. "+
                    "Use default", ex);
                _bundledCfgFileContents = null;
            }
            finally
            {
                try
                {
                    if (in != null)
                        in.close();
                }
                catch (IOException ex)
                {
                    // ignore
                }
            }
        }
        return _bundledCfgFileContents;
    }
    
    private void createLoginConfigFileInternal(String bundledFileContents)
    throws JAXRException
    {
        log.debug("start creating login config file - internal");
        try
        {
            String userCfgFileName = getUserCfgFileName();   
            log.info("User's config file name: "+ userCfgFileName);
            String userCfgContents = getUserCfgFileContents(userCfgFileName);
            // if the user doesn't have an existing login cfg file, write the
            // bundled one
            if (userCfgContents == null)
            {
                writeCfgFile(userCfgFileName, bundledFileContents);
            }
            else
            {
                // the user has an existing login cfg file. Use this method
                // to compare both files and take appropriate actions
                checkUserCfgFile(userCfgFileName, userCfgContents, 
                    bundledFileContents);
            }
        }
        catch (Throwable t)
        {
            log.warn("problem reading config file. using default", t);
            // problem reading config file.  Use default
            createDefaultLoginConfigFile();
        }
        log.debug("finish creating login config file - internal");
    }
        
    private void checkUserCfgFile(String userCfgFileName, 
                                  String userCfgContents, 
                                  String bundledFileContents)
    throws JAXRException
    {
        String userLoginName = getLoginName(userCfgContents);
        String bundledLoginName = getLoginName(bundledFileContents);
        // if the login names are the same, check attributes
        if (userLoginName.equalsIgnoreCase(bundledLoginName))
        {
            // this method checks that any required attributes are present and
            // that fixed attributes are set according to the settings in the
            // bundled jaxr-ebxml.properties file.            
            Configuration config = ConfigFile.getConfiguration();
            String appName = getApplicationName();
            String tmpAppName = appName+".tmp";
            AppConfigurationEntry[] bundledAppConfigEntries = 
                getReloadedAppConfigurationEntries(config, userCfgFileName+".tmp", 
                    bundledFileContents, appName); 
            AppConfigurationEntry[] userAppConfigEntries = 
                config.getAppConfigurationEntry(appName);
            boolean isCorrect = 
                areUserCfgFileAttributesCorrect(userAppConfigEntries, 
                    bundledAppConfigEntries);
            // if the user cfg content has changed, write it to the user cfg
            // file
            if (isCorrect == false)
            {
                log.warn("User login config file is not correct. Using "+
                    "bundled config file instead");
                renameCfgFile(userCfgFileName, userCfgFileName+".bak");
                log.info("Renamed "+userCfgFileName+" to .bak file");
                writeCfgFile(userCfgFileName, bundledFileContents);
                ConfigFile.getConfiguration().refresh();
                log.info("created new login file: "+userCfgFileName);
            }
            else 
            {
                // if the user has a different keystore file in the 
                // jaxr-ebxml.properties file, update the user's config file
                // automatically
                updateUserCfgContents(userAppConfigEntries, userCfgContents,
                    userCfgFileName);
            }
        }
        else 
        {
            // the existing login name in different than the bundled. So, move 
            // the existing user cfg file to a backup file
            renameCfgFile(userCfgFileName, userCfgFileName+".bak");
            writeCfgFile(userCfgFileName, bundledFileContents);
        }
    }

    private void renameCfgFile(String fileName, String renamedFileName)
    throws JAXRException
    {
        try
        {
            File file = new File(fileName);
            File renamedFile = new File(renamedFileName);
            if (renamedFile.exists())
            {
                for (int i = 2; i <= MAX_BACKUP_LOGIN_FILES; i++)
                {
                    String tempFileName = renamedFileName + i;
                    File tempFile = new File(tempFileName);
                    if (! tempFile.exists())
                    {
                        file.renameTo(tempFile);
                        log.debug("renaming config file "+fileName+" to "+
                            renamedFileName);
                        break;
                    }
                }
            }
            else
            {
                file.renameTo(renamedFile);
            }
        }
        catch (SecurityException ex)
        {
            throw new JAXRException(ex);
        }
    }
    
    private boolean areUserCfgFileAttributesCorrect(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries)
        throws JAXRException
    {
        boolean isCorrect = false;
        isCorrect = checkLoginModules(userAppConfigEntries, 
            bundledAppConfigEntries);
        if (isCorrect == false)
            return isCorrect;
        isCorrect = checkControlFlag(userAppConfigEntries, 
            bundledAppConfigEntries);
        if (isCorrect == false)
            return isCorrect;
        isCorrect = checkLoginModuleOptions(userAppConfigEntries, 
            bundledAppConfigEntries);
        return isCorrect;
    }
    
    private AppConfigurationEntry[] getReloadedAppConfigurationEntries(
        Configuration config, String cfgFileName, String cfgFileContents,
        String appConfigName)
    throws JAXRException
    {
        AppConfigurationEntry[] appConfigEntries = null;
        // if there is an IOException, we do not have permission to write
        // to the local filesystem.  Without this permission, we cannot
        // control the authentication.  In this case, throw new 
        // JAXRException to notify the user to give us permission
        try
        {
            File file = new File(cfgFileName);
            writeCfgFile(file, cfgFileContents); 
        }
        catch (Throwable t)
        {
            throw new JAXRException("This application does not have "+
                "permission to write to the local filesystem. Please "+
                "provide this permission in your java.policy file. Go to "+
                "http://java.sun.com and search for 'java.policy' for "+
                "details on setting permissions", t);
        }   
        String javaSecLoginCfg = 
            System.getProperty("java.security.auth.login.config");
        String userCfgFileName = getUserCfgFileName();
        System.setProperty("java.security.auth.login.config", cfgFileName);
        config.refresh();
        appConfigEntries = config.getAppConfigurationEntry(appConfigName);
        try
        { 
            deleteCfgFile(cfgFileName);
        }
        catch (Throwable t)
        {
            log.warn("problem deleting config file: ", t);
        }
        finally
        {
            if (javaSecLoginCfg != null)
            {
                System.setProperty("java.security.auth.login.config", javaSecLoginCfg);
            }
            else
            {
                System.setProperty("java.security.auth.login.config", userCfgFileName);
            }
            config.refresh();
        }
        return appConfigEntries;
    }

    /*
     * The login module names in the user's config file must appear in the
     * same order as the bundled config file
     */
    private boolean checkLoginModules(
        AppConfigurationEntry[] userAppConfigEntries, 
        AppConfigurationEntry[] bundledAppConfigEntries)
    {
        boolean isCorrect = false;
        for (int i = 0; i < bundledAppConfigEntries.length; i++)
        {
            isCorrect = true;
            try
            {   // user login modules must appear in the same order as the
                // bundled ones
                String bundledLoginModuleName = 
                    bundledAppConfigEntries[i].getLoginModuleName();
                String userLoginModuleName = 
                    userAppConfigEntries[i].getLoginModuleName();
                if (! bundledLoginModuleName.equals(userLoginModuleName))
                {
                    
                    isCorrect = false;
                    break;
                }
            }
            catch (Throwable t)
            {
                // If the user config file has missing login module, it will
                // be caught here
                isCorrect = false;
                break;
            }            
        }
        if (isCorrect)
        {
            log.debug("login module(s) in the existing login config file are ok");
        }
        else
        {
            log.warn("The login module(s) contained in the existing "+
                "login config file do not appear in the "+
                "same order as the modules in the bundled config file");
        }
        return isCorrect;
    }
    
    private boolean checkControlFlag(
        AppConfigurationEntry[] userAppConfigEntries, 
        AppConfigurationEntry[] bundledAppConfigEntries)
    {
        boolean isCorrect = true;
        for (int i = 0; i < bundledAppConfigEntries.length; i++)
        {
            try
            {
                AppConfigurationEntry.LoginModuleControlFlag bundledFlag = 
                    bundledAppConfigEntries[i].getControlFlag();
                String bundledFlagStr = bundledFlag.toString();
                AppConfigurationEntry.LoginModuleControlFlag userFlag = 
                    userAppConfigEntries[i].getControlFlag();
                String userFlagStr = userFlag.toString();
                if (! bundledFlagStr.equals(userFlagStr))
                {
                    isCorrect = false;
                    break;
                }
                
            }
            catch (Throwable t)
            {
                isCorrect = false;
                break;
            }
        }
        if (isCorrect)
        {
            log.debug("control flag(s) in the existing login config file are ok");
        }
        else
        {
            log.warn("The control flags contained in the existing "+
                "login config file do not match those in the "+
                "bundled config file");
        }
        return isCorrect;
    }
    
    private boolean checkLoginModuleOptions(
        AppConfigurationEntry[] userAppConfigEntries, 
        AppConfigurationEntry[] bundledAppConfigEntries)
    {
        boolean isCorrect = true;
        for (int i = 0; i < bundledAppConfigEntries.length; i++)
        {         
            Map userOptions = userAppConfigEntries[i].getOptions();
            Map bundledOptions = bundledAppConfigEntries[i].getOptions();
            isCorrect = doAllUserOptionExist(userOptions, bundledOptions);
            if (isCorrect == false)
            {
                break; // problem with options; stop checking
            }
            String loginModuleName = 
                bundledAppConfigEntries[i].getLoginModuleName();
            isCorrect = areAllUserOptionsSetToCorrectValues(userOptions, 
                loginModuleName);
            if (isCorrect == false) 
            { 
                break; // problem with options; stop checking
            }
        }
        return isCorrect;
    }
    
    private boolean doAllUserOptionExist(Map userOptions, Map bundledOptions)
    {
        boolean isCorrect = true;
        Iterator bundledOptionsIter = bundledOptions.keySet().iterator();
        while (bundledOptionsIter.hasNext())
        {
            String bundledOption = (String)bundledOptionsIter.next();
            String userOption = (String)userOptions.get(bundledOption);
            if (userOption == null)
            {
                log.warn("The following option is missing "+
                    "in the existing login config file: "+userOption);
                isCorrect = false;
                break;
            }
        }
        if (isCorrect)
        {
            log.debug("All options exist in the existing login config file");
        }
        return isCorrect;
    }
    
    private boolean areAllUserOptionsSetToCorrectValues(Map userOptions, 
        String loginModuleFullName)
    {
        boolean isCorrect = true;
        int lastPeriodIndex = loginModuleFullName.lastIndexOf('.');
        String loginModuleName = loginModuleFullName.
            substring(lastPeriodIndex+1, loginModuleFullName.length());
        Properties properties = getBundledProperties();
        if (properties == null)
            return isCorrect;
        String partialAttributeKey = _propertiesName+".security.jaas."+
            loginModuleName+".attribute.";
        String partialFixedKey = _propertiesName+".security.jaas."+
            loginModuleName+".fixedValue.";
        for (int j = 1; j <= MAX_FIXED_ATTRIBUTES; j++)
        {
            String attributeKey = partialAttributeKey + j;
            String attributeValue = null;
            try
            {                
                attributeValue = properties.getProperty(attributeKey);
            }
            catch (MissingResourceException ex)
            {
                // ignore - try to load attribute.1 through
                // attribute.MAX_FIXED_ATTRIBUTES
            }
            if (attributeValue != null)
            {
                String fixedKey = partialFixedKey + j;
                String fixedValue = null;
                try
                {
                    fixedValue = properties.getProperty(fixedKey);
                }
                catch (MissingResourceException ex)
                {
                    // ignore - try to load fixedValue.1 through
                    // fixedValue.MAX_FIXED_ATTRIBUTES
                }
                if (fixedValue != null)
                {   
                    String optionValue = 
                        (String)userOptions.get(attributeValue);
                    if (optionValue == null || 
                        ! optionValue.equalsIgnoreCase(fixedValue))
                    {
                        // integrity check has failed
                        // break and return 'false'
                        log.warn("The following option is not set properly: "+
                            attributeValue);
                        log.warn("It is set to: "+optionValue);
                        log.warn("It should be set to: "+fixedValue);
                        isCorrect = false;
                        break;
                    }
                }
            }
        }
        if (isCorrect == true)
        {
            log.debug("all user config file options are set properly");
        }
        return isCorrect;
    }

    private Properties getSecurityProperties()
    {
        if (_securityProps == null)
        {
            _securityProps = new Properties();
            BufferedInputStream bis = null;
            String fileName = null;
            try
            {
                String javaHome = System.getProperty("java.home");
                String dirSep = System.getProperty("file.separator");
                StringBuffer sb = new StringBuffer(javaHome);
                sb.append(dirSep).append("lib").append(dirSep);
                sb.append("security").append(dirSep);
                sb.append("java.security");
                fileName = sb.toString();
                log.info("Found java.security properties: "+fileName);
                File file = new File(fileName);
                bis = new BufferedInputStream(new FileInputStream(file));
                _securityProps.load(bis);
            }
            catch (IOException ex)
            {
                log.warn("could not open java.security properties file. Ignore.", 
                    ex);
            }
            finally
            {
                try
                {
                    if (bis != null)
                    {
                        bis.close();
                    }
                }
                catch (IOException ex)
                {
                    // ignore
                }
            }
        }
        return _securityProps;
    }
    
    private String getLoginName(String cfgFileContents)
    {
        int firstSpaceIndex = cfgFileContents.indexOf(' ');
        return (cfgFileContents.substring(0, firstSpaceIndex));
    }
    
    private void deleteCfgFile(String cfgFile) throws JAXRException
    {
        try
        {
            File file = new File(cfgFile);
            boolean isDeleted = file.delete();
            if (isDeleted == false)
            {
                System.out.println("warning: could not delete tmp file");
            }
        }
        catch (Throwable t)
        {
            throw new JAXRException(t);   
        }
    }
    
    private void writeCfgFile(File configFile, String cfgFileContents)
    throws JAXRException
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(cfgFileContents, 0, cfgFileContents.length());
            writer.flush();
        }
        catch (IOException ex)
        {
            throw new JAXRException(ex);
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (IOException ex)
                {
                    // ignore
                }
            }
        }
    }
    
    private void writeCfgFile(String userCfgFileName, String fileContents) 
    throws JAXRException
    {
        BufferedWriter writer = null;
        try
        {
            File file = new File(userCfgFileName);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(fileContents, 0, fileContents.length());
            writer.flush();
        }
        catch (IOException ex)
        {
            throw new JAXRException(ex);
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (IOException ex)
                {
                    // ignore
                }
            }
        }
    }
    
    private String getUserCfgFileName()
    {
        String userCfgFileName = null;
        try 
        {
            Properties securityProps = getSecurityProperties();
            // Ignore the login.configuration.provider property in this
            // release. Alternative provider implementations can be supported
            // in subsequent releases.
            // We do not support login.config.url.1 in this release
            userCfgFileName = 
                    System.getProperty("java.security.auth.login.config");
            if (userCfgFileName == null || userCfgFileName.equals(""))
            {
                userCfgFileName = getDefaultUserCfgFileName();
            }
        }
        catch (Throwable t)
        {
            log.warn("problem getting user config file: ", t);
            userCfgFileName = getDefaultUserCfgFileName();
        }
        return userCfgFileName;
    }
    
    private String getDefaultUserCfgFileName()
    {
        StringBuffer sb = new StringBuffer(System.getProperty("user.home"));
        sb.append(System.getProperty("file.separator"));
        sb.append(".java.login.config");
        return (sb.toString());
    }
    
    private String getUserCfgFileContents(String userHomeFileName) 
    throws JAXRException
    {
        String fileContents = null;
        BufferedReader in = null;
        try 
        {
            File configFile = new File(userHomeFileName);
            if (configFile.exists() == true)
            {
                StringBuffer sb2 = new StringBuffer();
                String line = null;
                in = new BufferedReader(new FileReader(configFile));
                while ((line = in.readLine()) != null)
                {
                    sb2.append(line).append(LINE_SEPARATOR);
                }
                fileContents = sb2.toString();
            }
            log.debug("user config file contents: "+fileContents);
            return fileContents;
        }
        catch (IOException ex)
        {
            throw new JAXRException(ex);
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException ex)
            {
                // ignore
            }
        }
    }
    
    private String getDefaultConfigFileContents()
    {
        return getDefaultConfigFileContents(DEFAULT_APPLICATION_NAME);
    }
    
    private String getDefaultConfigFileContents(String appName)
    {
        String defaultConfigFileContents = null;
       
        if (appName == null)
        {
            appName = DEFAULT_APPLICATION_NAME;
        }
        String keystoreFileName = DEFAULT_KEYSTORE_FILENAME;
        try
        {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.canReadKeystoreFile(keystoreFile);
            keystoreFileName = keystoreFile.toURL().getFile();
        }
        catch (Throwable ex)
        {
            // Since keystoreFileName is already set to default, ignore
        }

        StringBuffer sb = new StringBuffer(appName);
        sb.append(" { "+LINE_SEPARATOR);
        sb.append("    com.sun.security.auth.module.KeyStoreLoginModule ");
        sb.append("required ").append(LINE_SEPARATOR);
        sb.append("    debug=true keyStoreURL=\"file:");
        sb.append(keystoreFileName).append("\";");
        sb.append(LINE_SEPARATOR).append("};").append(LINE_SEPARATOR);
        defaultConfigFileContents = sb.toString();
        log.debug("Default config file contents: "+defaultConfigFileContents);
        return defaultConfigFileContents;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            try
            {
                String argFlag = args[i];
                if (argFlag.equals("-d"))
                {
                    _createDefaultLoginFile = true;
                }
                else if (argFlag.equals("-l"))
                {
                    _createLoginFile = true;
                }
                else if (argFlag.equals("-c"))
                {
                    _getCallbackHandler = true;
                }
                else
                {   // if no flags execute all methods
                    _createDefaultLoginFile = true;
                    _createLoginFile = true;
                    _getCallbackHandler = true;
                }
            }
            catch (RuntimeException ex)
            {
                // use defaults
            }
        }
        if (args.length == 0)
        {
            _createLoginFile = true;
        }
        LoginModuleManager loginModuleMgr = new LoginModuleManager();
        try
        {
            if (_createDefaultLoginFile)
            {
                log.info("starting createDefaultLoginConfigFile");
                loginModuleMgr.createDefaultLoginConfigFile();
            }
            if (_createLoginFile)
            {
                log.info("starting createLoginConfigFile");
                loginModuleMgr.createLoginConfigFile();
            }
            if (_getCallbackHandler)
            {
                log.info("starting getCallbackHandler");
                loginModuleMgr.getCallbackHandler();
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    private void updateUserCfgContents(
        AppConfigurationEntry[] userAppConfigEntries, String userCfgContents,
        String cfgFileName)
    {
        for (int i = 0; i < userAppConfigEntries.length; i++)
        {         
            Map userOptions = userAppConfigEntries[i].getOptions();
            String userKeystoreFile = (String)userOptions.get("keyStoreURL");
            if (userKeystoreFile != null)
            {
                try
                {
                    File keystoreFile = KeystoreUtil.getKeystoreFile();
                    KeystoreUtil.canReadKeystoreFile(keystoreFile);
                    String keystoreFileInPropFile = 
                        keystoreFile.toURL().getFile();
                    if (keystoreFileInPropFile != null)
                    {
                        if (! userKeystoreFile.equals(keystoreFileInPropFile))
                        {
                            String keyStoreURL = new String("keyStoreURL=\"file:");
                            int keyStartIndex = 
                                userCfgContents.indexOf(keyStoreURL);
                            keyStartIndex += keyStoreURL.length();
                            int keyEndIndex = 
                                userCfgContents.indexOf("\";", keyStartIndex);

                            String firstPart = userCfgContents.substring(0, 
                                keyStartIndex);
                            String lastPart = 
                                userCfgContents.substring(keyEndIndex);
                            // combine all parts
                            StringBuffer sb = new StringBuffer(firstPart);
                            sb.append(keystoreFileInPropFile).append(lastPart);
                            userCfgContents = sb.toString();
                            writeCfgFile(cfgFileName, userCfgContents);
                        }
                    }
                }
                catch (Throwable t)
                {
                    // ignore
                }
            }
        }
    }
    
    private void checkKeystoreOption()
    {
        String keyStoreURL = new String("keyStoreURL=\"file:");
        int keyStartIndex = 
            _bundledCfgFileContents.indexOf(keyStoreURL);
        keyStartIndex += keyStoreURL.length();
        int keyEndIndex = 
            _bundledCfgFileContents.indexOf("\";", keyStartIndex);
        try
        {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.canReadKeystoreFile(keystoreFile);      
            String firstPart = _bundledCfgFileContents.substring(0, keyStartIndex);
            String keystoreFileName = keystoreFile.toURL().getFile();
            String lastPart = _bundledCfgFileContents.substring(keyEndIndex);
            // combine all parts
            StringBuffer sb = new StringBuffer(firstPart);
            sb.append(keystoreFileName).append(lastPart);
            _bundledCfgFileContents = sb.toString();
        }
        catch (Throwable ex)
        {
            // ignore - use existing _bundledCfgFileContents
        }
    }
}
