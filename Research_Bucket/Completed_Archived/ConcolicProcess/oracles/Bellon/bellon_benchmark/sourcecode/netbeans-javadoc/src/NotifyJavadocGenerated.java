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
/*
 * NotifyJavadocGenerated.java
 *
 * Created on 3. leden 2001, 14:14
 */
package org.netbeans.modules.javadoc;
import java.util.*;
import java.text.MessageFormat;
import java.net.URL;
import java.lang.reflect.Field;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.CoronaEnvironment;
import org.netbeans.modules.java.CoronaClassPath;
import org.netbeans.modules.java.CoronaClassFile;
import org.netbeans.modules.java.ErrConsumer;
import org.netbeans.modules.javadoc.settings.JavadocSettingsService;
import org.openide.filesystems.FileObject;
/**
 *
 * @author  psuchomel
 * @version 
 */
class NotifyJavadocGenerated extends Object {
    public static void showNotifyDialog( boolean useInternal ) {    
        String destDir;
        if( useInternal ) {
            destDir = OptionListProducer.getDestinationDirectory(); }
        else {
            destDir = ExternalOptionListProducer.getDestinationDirectory(); }
        String mssg = MessageFormat.format( ResourceUtils.getBundledString( "FMT_GeneratingFinished" ),   //NOI18N
                                            new Object[] { destDir } );
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
        TopManager.getDefault().notify( nd );
        if ( nd.getValue().equals ( NotifyDescriptor.YES_OPTION ) ) {
            //System.out.println ( nd.getValue() );
            try {
                URL url = null;
                if ( ( useInternal == true ? OptionListProducer.isStyle1_1():ExternalOptionListProducer.isStyle1_1()) )
                    url = new URL( "file://localhost/" + destDir + java.io.File.separator + "packages.html" ); // NOI18N
                else
                    url = new URL( "file://localhost/" + destDir + java.io.File.separator + "index.html" ); // NOI18N
                TopManager.getDefault().showUrl( url ); }
            catch ( java.net.MalformedURLException e ) {
                throw new InternalError( "Can't find documentation index fier" );  } } } }// NOI18N
