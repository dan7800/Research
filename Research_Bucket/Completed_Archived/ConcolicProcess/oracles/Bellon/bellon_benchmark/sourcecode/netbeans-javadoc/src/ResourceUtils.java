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
 * ResourceUtils.java
 *
 * Created on 12. leden 2001, 14:07
 */
package org.netbeans.modules.javadoc;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
/**
 *
 * @author  psuchomel
 * @version 
 */
class ResourceUtils extends Object {
    private static ResourceBundle bundle = null;
/** Used to get bundled strings in class, bundle is loaded for the first time it is neded
 * @param key key which have to be taken from bundle
 * @return String from bundle
 */    
    static String getBundledString( String key ){
        if( bundle == null )    //if bundle is null, load bundle
            bundle = NbBundle.getBundle( ResourceUtils.class );
        //return value by key
        return bundle.getString( key ); } }
/* ... */
class CommonUtils extends Object {
    private static ResourceBundle bundle = null;
/* Used to test the whole thing.
 * @param key key which have to be taken from bundle
 * @return String from bundle
 */    
    static String getBundledString( String key ){
	ResourceBundle bundle = NbBundle.getBundle( ResourceUtils.class );
        if( bundle == null )
	    return null;
        return bundle.getString( key ); } }
