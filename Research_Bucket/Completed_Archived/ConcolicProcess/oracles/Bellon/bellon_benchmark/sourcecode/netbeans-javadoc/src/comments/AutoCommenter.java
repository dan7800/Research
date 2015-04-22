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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.text.MessageFormat;
import javax.swing.DefaultListModel;
import java.util.ResourceBundle;
import java.lang.reflect.Modifier;
import org.openide.src.*;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.EventObject;
import java.util.EventListener;
import org.openide.util.WeakListener;
/** Contains static methods for generating default JavaDoc comments for
 * java data object hierarchy elements.
 *
 * Checks for Comment errors in JavaDataObjects
 *
 * @author Petr Hrebejk
 */
public class AutoCommenter extends Object implements JavaTagNames {
    public static final int JDC_OK = 1;
    public static final int JDC_MISSING = 2;
    public static final int JDC_ERROR = 4;
    ArrayList elements;
    final ArrayList dataObjects;
    Node[] nodes;
    /** Utility field holding the PropertyChangeListener. */
    private AutoCommentChangeListener autoCommentChangeListener =  null;
    /** Creates an empty AutoCommenter */
    AutoCommenter() {
        this( new Node[0] ); }
    /** Creates Auto commenter for nodes */
    AutoCommenter ( Node[] nodes ) {
        dataObjects = new ArrayList();
        this.nodes = nodes;
        for (int i = 0; i < nodes.length; i++ ) {
            DataFolder df = (DataFolder)nodes[i].getCookie( DataFolder.class );
            SourceCookie sc = null;
            if( df != null ){
                DataObject[] children = df.getChildren();
                for ( int n = 0; n < children.length; n++ ){
                    final DataObject child = children[n];
                    sc = (SourceCookie)child.getCookie(SourceCookie.Editor.class);
                    if( sc != null && !child.getPrimaryFile().isReadOnly()){
                        if( !dataObjects.contains(child) ){
                            child.addPropertyChangeListener(WeakListener.propertyChange(new PropertyChangeListener(){
                                public void propertyChange(PropertyChangeEvent evt){                                    
                                    if( evt.getPropertyName().equals(DataObject.PROP_VALID)){
                                        synchronized ( dataObjects ){
                                            dataObjects.remove(child); }
                                        AutoCommenter.this.fireAutocommentChangeEvent(); } }
                            }, child));
                            synchronized ( dataObjects ){
                                dataObjects.add(child); } } } }
                continue; }
            else {
                sc = (SourceCookie)nodes[i].getCookie( SourceCookie.Editor.class ); }
            final DataObject doj = (DataObject)nodes[i].getCookie( DataObject.class );
            if ( sc == null || doj.getPrimaryFile().isReadOnly() )
                continue;            
            if( !dataObjects.contains(doj) ) {
                doj.addPropertyChangeListener(WeakListener.propertyChange(new PropertyChangeListener(){
                    public void propertyChange(PropertyChangeEvent evt){
                        if( evt.getPropertyName().equals(DataObject.PROP_VALID)){
                            synchronized ( dataObjects ){
                                dataObjects.remove(doj);                                 }
                            fireAutocommentChangeEvent(); } }
                }, doj));
                synchronized ( dataObjects ){
                    dataObjects.add(doj); } } }
        //this.nodes = nodes;
         }//refreshFromSource();
    void refreshFromSource() {
        //for ( int i =0; elements != null && i < elements.size(); i++ ){
        //    ((Element)elements.get(i)).getSrcElement().removePropertyChangeListener(listen);
        //}
        elements = new ArrayList();
        for (int i = 0; i < dataObjects.size(); i++ ) {
            SourceCookie sc = (SourceCookie)((DataObject)dataObjects.get(i)).getCookie(SourceCookie.class);
            addCommentable(sc); } }
    void addCommentable(SourceCookie sc){
        SourceElement se = sc.getSource();
        if ( se != null ) {
            ClassElement[] ces = se.getAllClasses();
            for( int j = 0; j < ces.length; j++ ){
                addElements( ces[j] ); } } }
    void prepareListModel( DefaultListModel listModel, int mask, boolean pckg, int err_mask ) {
        Iterator it = elements.iterator();
        while( it.hasNext() ) {
            Element el = (Element)it.next();
            if ( acceptElement( el, mask, pckg, err_mask ) ) {
                listModel.addElement( el ); } } }
    static boolean acceptElement( Element el, int mask, boolean pckg, int err_mask ) {
        // Test whether the element is accepted by error mask
        if ( ( el.getErrorNumber() & err_mask ) == 0 )
            return false;
        // Test whether the element is accepted by access mask
        if ( ( el.getModifiers() & ( Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE ) ) == 0 ) {
            if ( !pckg )
                return false; }
        else if ( ( el.getModifiers() & mask ) == 0 )
            return false;
        return true; }
    DefaultListModel prepareListModel( int mask, boolean pckg, int err_mask ) {
        DefaultListModel dm = new DefaultListModel();
        prepareListModel( dm, mask, pckg, err_mask );
        return dm; }
    private void addElements( ClassElement classElement ) {
        elements.add( new Element.Class( classElement ) );
        FieldElement[] fe = classElement.getFields();
        for( int i = 0; i < fe.length; i++ ) {
            elements.add( new Element.Field ( fe[i] ) ); }
        ConstructorElement[] ce = classElement.getConstructors();
        for( int i = 0; i < ce.length; i++ ) {
            elements.add( new Element.Constructor ( ce[i] ) ); }
        MethodElement[] me = classElement.getMethods();
        for( int i = 0; i < me.length; i++ ) {
            elements.add( new Element.Method ( me[i] ) ); }
        /* getAllClasses is already recursive
        ClassElement[] ice = classElement.getClasses();
        for( int i = 0; i < ice.length; i++ ) {
          addElements( ice[i] ); }
        */ }
    /** innerclass holds the element and the informations about comment errors */
    static abstract class Element {
        protected DefaultListModel errorList;
        MemberElement srcElement = null;
        int srcError = JDC_OK;
        Element( MemberElement srcElement ) {
            this.srcElement = srcElement;
            PropertyChangeListener pp = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt){
                    //System.err.println(evt.getPropertyName());
                    if( evt.getSource() instanceof org.netbeans.modules.java.JavaDataObject ) //ignore
                        return;
                    //System.err.println("change 2");
                     }//AutoCommenter.this.fireAutocommentChangeEvent();            
            };
            //System.err.println("adding listeners");
            srcElement.addPropertyChangeListener(WeakListener.propertyChange(pp, srcElement));
            checkError(); }
        String getName() {
            return getNameFormat().format( srcElement ); }
        MemberElement getSrcElement() {
            return srcElement; }
        int getModifiers() {
            return srcElement.getModifiers(); }
        int getErrorNumber() {
            return srcError; }
        void viewSource() {
            OpenCookie oc = ((OpenCookie)srcElement.getCookie( OpenCookie.class ));
            oc.open(); }
        DefaultListModel getErrorList() {
            return errorList; }
        /** Returns the source for this particular element. Used for locking purposes.
        */
        SourceElement findSource() {
            ClassElement decl = srcElement.getDeclaringClass();
            if (decl == null && (srcElement instanceof ClassElement)) {
                decl = (ClassElement)srcElement; }
            if (decl == null) return null;
            return decl.getSource(); }
        abstract String[] getNotPermittedTags();
        abstract boolean elementTagsOk();
        abstract void autoCorrect() throws SourceException;
        abstract JavaDoc getJavaDoc();
        abstract ElementFormat getNameFormat();
        abstract String typeToString();
        static boolean isPermittedTag( JavaDocTag tag, String[] notPermittedTags ) {
            String tagName = tag.name();
            for ( int i = 0; i < notPermittedTags.length; i++ ) {
                if ( tagName.equals( notPermittedTags[i] ) )
                    return false; }
            return true; }
        void modifyJavaDoc(Runnable mutator) throws SourceException {
            SourceElement src = findSource();
            if (src == null) {
                mutator.run();
            } else {
                src.runAtomicAsUser(mutator); } }
        private static boolean isEmptyString( String string ) {
            return string == null || string.trim().length() <= 0; }
        /** Checks syntax of the tags
         */
        boolean isOkTag( JavaDocTag tag ) {
            if ( isEmptyString( tag.text() ) ) {
                errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_EmptyTag" ),   //NOI18N
                                      new Object[] { tag.name()  } ) );
                return false; }
            if ( tag instanceof JavaDocTag.See ) {
                JavaDocTag.See seetag = (JavaDocTag.See) tag;
                if ((seetag.referencedClassName() == null) && (seetag.referencedMemberName() == null)) {
                    errorList.addElement(MessageFormat.format( ResourceUtils.getBundledString( "ERR_InvalidTag" ),  //NOI18N
                                      new Object[] { seetag.name() + " " + seetag.text() } ));  //NOI18N
                    return false; } }
            else if ( tag instanceof JavaDocTag.Param ) {
                if ( isEmptyString( ((JavaDocTag.Param)tag).parameterName() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_ParamNoName" ),    //NOI18N
                                          new Object[] { tag.name() } ) );
                    return false; }
                if ( isEmptyString( ((JavaDocTag.Param)tag).parameterComment() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_ParamNoDescr" ),   //NOI18N
                                          new Object[] { tag.name(), ((JavaDocTag.Param)tag).parameterName()  } ) );
                    return false; } }
            else if ( tag instanceof JavaDocTag.Throws ) {
                if ( isEmptyString( ((JavaDocTag.Throws)tag).exceptionName() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_ThrowsNoName" ),   //NOI18N
                                          new Object[] { tag.name() } ) );
                    return false; }
                if ( isEmptyString( ((JavaDocTag.Throws)tag).exceptionComment() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_ThrowsNoDescr" ),  //NOI18N
                                          new Object[] { tag.name(), ((JavaDocTag.Throws)tag).exceptionName()  } ) );
                    return false; } }
            else if ( tag instanceof JavaDocTag.SerialField ) {
                if ( isEmptyString( ((JavaDocTag.SerialField)tag).fieldName() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_SerialFieldNoName" ),  //NOI18N
                                          new Object[] { tag.name() } ) );
                    return false; }
                if ( isEmptyString( ((JavaDocTag.SerialField)tag).fieldType() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_SerialFieldNoType" ),  //NOI18N
                                          new Object[] { tag.name(), ((JavaDocTag.SerialField)tag).fieldName()  } ) );
                    return false; }
                if ( isEmptyString( ((JavaDocTag.SerialField)tag).description() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_SerialFieldNoDescr" ), //NOI18N
                                          new Object[] { tag.name(), ((JavaDocTag.SerialField)tag).fieldName()  } ) );
                    return false; } }
            return true; }
        boolean isMultipleTags(String tag) {
            // Check for multiple tags
            boolean error = false;
            JavaDocTag[] tags = getJavaDoc().getTags(tag);
            if ( tags.length > 1) {
                errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_DuplicatedTag" ),  //NOI18N
                                      new Object[] { tags[0].name() } ) );
                error = true; }
            return error; }
        void checkError() {
            errorList = new DefaultListModel();
            JavaDoc jdoc = getJavaDoc();
            if ( jdoc.isEmpty() ) {
                srcError = JDC_MISSING;
                errorList.addElement( ResourceUtils.getBundledString( "ERR_JavadocMissing" ) ); //NOI18N
                return; }
            JavaDocTag[] tags = jdoc.getTags();
            boolean error = false;
            if ( jdoc.getText() == null || jdoc.getText().trim().length() <= 0 ) {
                errorList.addElement( ResourceUtils.getBundledString( "ERR_EmptyText" ) );  //NOI18N
                error = true; }
            for ( int i = 0; i < tags.length; i ++ ) {
                if ( !Element.isPermittedTag( tags[i], getNotPermittedTags() ) ) {
                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_BadTag" ), //NOI18N
                                          new Object[] { tags[i].name(), typeToString() } ) );
                    error = true;
                    continue; }
                if ( !isOkTag( tags[i] ) ) {
                    error = true;
                    continue; } }
            if (isMultipleTags(TAG_SINCE)) {
                error = true; }
            if (isMultipleTags(TAG_DEPRECATED)) {
                error = true; }
            if ( !elementTagsOk( ) ) {
                error = true; }
            if ( !error ) {
                errorList.addElement( ResourceUtils.getBundledString( "ERR_JavadocOK" ) );   }//NOI18N
            srcError = error ? JDC_ERROR : JDC_OK; }
        boolean isCorrectable() {
            JavaDocTag[] tags = getJavaDoc().getTags();
            for ( int i = 0; i < tags.length; i ++ ) {
                if ( !Element.isPermittedTag( tags[i], getNotPermittedTags() ) ) {
                    return true; } }
            return false; }
        void autoCorrect( JavaDoc jdoc ) throws SourceException {
            JavaDocTag[] tags = jdoc.getTags();
            ArrayList correctedTags = new ArrayList( tags.length );
            String correctedText;
            correctedText = jdoc.getText();
            if ( correctedText == null ) {
                correctedText = "";  }// NOI18N
            for ( int i = 0; i < tags.length; i ++ ) {
                if ( !Element.isPermittedTag( tags[i], getNotPermittedTags() ) ) {
                    continue; }
                correctedTags.add( tags[i] ); }
            //jdoc.setRawText( generateRawText( correctedText, (Collection)correctedTags ) );
            jdoc.changeTags( (JavaDocTag[])correctedTags.toArray( new JavaDocTag[ correctedTags.size() ] ), JavaDoc.SET  ); }
        static class Class extends Element {
            private static final String[] NOT_PERMITTED_TAGS = {
                TAG_EXCEPTION,
                TAG_PARAM,
                TAG_RETURN,
                TAG_SERIAL,
                TAG_SERIALDATA,
                TAG_SERIALFIELD,
                TAG_THROWS,
            };
            private static final ElementFormat nameFormat = new ElementFormat( "{m} {C}" ); // NOI18N
            Class( ClassElement element ) {
                super( element ); }
            String[] getNotPermittedTags() {
                return NOT_PERMITTED_TAGS; }
            boolean elementTagsOk() {
                boolean error = false;
                if (this.isMultipleTags(TAG_VERSION)) {
                    error = true; }
                return !error; }
            void autoCorrect() throws SourceException {
                super.autoCorrect( getJavaDoc() ); }
            boolean isCorrectable() {
                return super.isCorrectable(); }
            String typeToString() {
                return ((ClassElement)srcElement).isInterface() ? "interface" : "class";  }// NOI18N
            JavaDoc getJavaDoc() {
                return ((ClassElement)srcElement).getJavaDoc(); }
            ElementFormat getNameFormat () {
                return nameFormat; } }
        static class Field extends Element {
            private static final String[] NOT_PERMITTED_TAGS = {
                TAG_AUTHOR,
                TAG_EXCEPTION,
                TAG_PARAM,
                TAG_RETURN,
                TAG_SERIALDATA,
                TAG_THROWS,
                TAG_VERSION
            };
            private static final ElementFormat nameFormat = new ElementFormat( "{m} {t} {n}" ); // NOI18N
            Field( FieldElement element ) {
                super( element ); }
            JavaDoc getJavaDoc() {
                return ((FieldElement)srcElement).getJavaDoc(); }
            String[] getNotPermittedTags() {
                return NOT_PERMITTED_TAGS; }
            String typeToString() {
                return "field";  }// NOI18N
            boolean elementTagsOk() {
                boolean error = false;
                if (this.isMultipleTags(TAG_SERIAL)) {
                    error = true; }
                return !error; }
            void autoCorrect() throws SourceException {
                super.autoCorrect( getJavaDoc() ); }
            boolean isCorrectable() {
                return super.isCorrectable(); }
            ElementFormat getNameFormat () {
                return nameFormat; } }
        static class Constructor extends Element {
            private static final String[] NOT_PERMITTED_TAGS = {
                TAG_AUTHOR,
                TAG_SERIAL,
                TAG_SERIALFIELD,
                TAG_VERSION,
                TAG_RETURN
            };
            private static final ElementFormat nameFormat = new ElementFormat( "{m} {n} ( {p} )" ); // NOI18N
            Constructor( ConstructorElement element ) {
                super( element ); }
            JavaDoc getJavaDoc() {
                return ((ConstructorElement)srcElement).getJavaDoc(); }
            String[] getNotPermittedTags() {
                return NOT_PERMITTED_TAGS; }
            String typeToString() {
                return "constructor";  }// NOI18N
            boolean elementTagsOk() {
                return elementTagsOk( null, false ); }
            boolean elementTagsOk( ArrayList correctedTags, boolean checkOnly ) {
                boolean error = false;
                // Check param tags
                JavaDocTag.Param[] ptags = ((ConstructorElement)srcElement).getJavaDoc().getParamTags();
                boolean[] ptags_found = new boolean[ ptags.length ];
                MethodParameter[] params = ((ConstructorElement)srcElement).getParameters();
                //boolean[] params_ok = new boolean[ params.length ];
                // Check if all parameters for the method are in the javadoc
                // and if there are any parameter tag duplicates.
                for (int j = 0 ; j < params.length ; j++) {
                    boolean tagFound = false;
                    boolean duplicateTagAlreadyFound = false;
                    for (int i = 0 ; i < ptags.length ; i++) {
                        if ( ptags[i].parameterName() != null && ptags[i].parameterName().equals( params[j].getName() ) ) {
                            ptags_found[i] = true;
                            if (!tagFound) {
                                tagFound = true;
                            } else if (! duplicateTagAlreadyFound) {
                                if ( checkOnly ) {
                                    return false; }
                                else if ( correctedTags == null ) {
                                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_DuplicatedParamTag" ), //NOI18N
                                                          new Object[] { params[j].getName() } ) ); }
                                error = true;
                                duplicateTagAlreadyFound = true; } } }
                    if (! tagFound ) {
                        if ( checkOnly ) {
                            return false;
                        } else if ( correctedTags == null ) {
                            error = true;
                            errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_NoTagForParam" ),  //NOI18N
                                                  new Object[] { params[j].getName() } ) ); }
                        else {
                            correctedTags.add( JavaDocSupport.createParamTag( TAG_PARAM, params[j].getName() ) ); } } }
                for( int i = 0; i < ptags.length; i++ ) {
                    if ( !ptags_found[i] ) {
                        if ( checkOnly ) {
                            return false; }
                        else if ( correctedTags == null ) {
                            errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_NoSuchParam" ),    //NOI18N
                                                  new Object[] { ptags[i].name(), ptags[i].parameterName() } ) ); }
                        error = true; }
                    else if ( correctedTags != null ) {
                        correctedTags.add( ptags[i] ); } }
                // Check throws tags
                JavaDocTag.Throws[] ttags = ((ConstructorElement)srcElement).getJavaDoc().getThrowsTags();
                boolean[] ttags_found = new boolean[ ttags.length ];
                Identifier[] excs = ((ConstructorElement)srcElement).getExceptions();
                //boolean[] excs_ok = new boolean[ excs.length ];
                // Check if all exceptions for the method are in the javadoc
                // and if there are any exception tag duplicates.
                for (int j = 0 ; j < excs.length ; j++) {
                    boolean tagFound = false;
                    boolean duplicateTagAlreadyFound = false;
                    for (int i = 0 ; i < ttags.length ; i++) {
                        // if ( ttags[i].exceptionName() != null && ttags[i].exceptionName().equals( excs[j].getName() ) ) {
                        Identifier tagExId = Identifier.create(ttags[i].exceptionName() );
                        if ( tagExId != null && ( tagExId.compareTo( excs[j], false ) ||
                                  tagExId.getName().equals(excs[j].getName() ) ) ) {                        
                            ttags_found[i] = true;
                            if (!tagFound) {
                                tagFound = true;
                            } else if (! duplicateTagAlreadyFound){
                                if ( checkOnly ) {
                                    return false; }
                                else if ( correctedTags == null ) {
                                    errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_DuplicatedExceptionTag" ), //NOI18N
                                                          new Object[] { excs[j].getName() } ) ); }
                                error = true;
                                duplicateTagAlreadyFound = true; } } }
                    if (! tagFound ) {
                        if ( checkOnly ) {
                            return false;
                        } else if ( correctedTags == null ) {
                            error = true;
                            errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_NoTagForException" ),  //NOI18N
                                                  new Object[] { excs[j].getName() } ) ); }
                        else {
                            correctedTags.add( JavaDocSupport.createThrowsTag( TAG_THROWS, excs[j].getName() ) ); } } }
                for( int i = 0; i < ttags.length; i++ ) {
                    if ( !ttags_found[i] ) {
                        if ( checkOnly ) {
                            return false; }
                        else if ( correctedTags == null ) {
                            errorList.addElement( MessageFormat.format( ResourceUtils.getBundledString( "ERR_NoSuchException" ),    //NOI18N
                                                  new Object[] { ttags[i].name(), ttags[i].exceptionName() } ) ); }
                        error = true; }
                    else if ( correctedTags != null ) {
                        correctedTags.add( ttags[i] ); } }
                return !error; }
            boolean isCorrectable () {
                if ( super.isCorrectable() )
                    return true;
                return !elementTagsOk( null, true ); }
            void autoCorrect() throws SourceException {
                autoCorrect( getJavaDoc() ); }
            void autoCorrect( JavaDoc jDoc ) throws SourceException {
                JavaDoc.Method jdTemp = JavaDocSupport.createMethodJavaDoc(getJavaDoc().getRawText());
                // create comment without throws and params
                JavaDocTag tags[] = jdTemp.getTags();
                ArrayList stdTags = new ArrayList( tags.length );
                for( int i = 0; i < tags.length; i++ ) {
                    if ( !( tags[i] instanceof JavaDocTag.Param ) &&
                            !( tags[i] instanceof JavaDocTag.Throws ) ) {
                        stdTags.add( tags[i] ); } }
                // jdTemp.setRawText( generateRawText( jdTemp.getText(), stdTags ) );
                jdTemp.changeTags( (JavaDocTag[])stdTags.toArray( new JavaDocTag[ stdTags.size() ] ), JavaDoc.SET  );
                super.autoCorrect( jdTemp );
                ArrayList correctedTags = new ArrayList();
                elementTagsOk( correctedTags, false );
                // Build all tags collection
                ArrayList allTags = new ArrayList( correctedTags.size() + tags.length );
                tags = jdTemp.getTags();
                for( int i = 0; i < tags.length; i++ ) {
                    allTags.add( tags[i] ); }
                allTags.addAll( correctedTags );
                /*
                Iterator it = allTags.iterator(); 
                while( it.hasNext() ) {
                  System.out.println( (JavaDocTag)it.next() ); }
                */
                //getJavaDoc().setRawText( generateRawText( jdTemp.getText(), allTags ) );
                jDoc.changeTags( (JavaDocTag[])allTags.toArray( new JavaDocTag[ allTags.size() ] ), JavaDoc.SET  ); }
            ElementFormat getNameFormat () {
                return nameFormat; } }
        static class Method extends Constructor {
            private static final ElementFormat nameFormat = new ElementFormat( "{m} {r} {n} ( {p} )" ); // NOI18N
            private static final String[] NOT_PERMITTED_TAGS = {
                TAG_AUTHOR,
                TAG_SERIAL,
                TAG_SERIALFIELD,
                TAG_VERSION
            };
            Method( MethodElement element ) {
                super( element ); }
            JavaDoc getJavaDoc() {
                return ((MethodElement)srcElement).getJavaDoc(); }
            String typeToString() {
                return "method";  }// NOI18N
            String[] getNotPermittedTags() {
                return NOT_PERMITTED_TAGS; }
            boolean elementTagsOk() {
                boolean superOk = super.elementTagsOk();
                boolean retOk = checkReturnType( false );
                return !superOk ? false : retOk; }
            private boolean checkReturnType( boolean checkOnly ) {
                boolean retOk = true;
                Type ret = ((MethodElement)srcElement).getReturn();
                JavaDocTag[] retTags = ((MethodElement)srcElement).getJavaDoc().getTags( TAG_RETURN );
                if ( ret == Type.VOID && retTags.length > 0 ) {
                    if ( checkOnly ) {
                        return false; }
                    errorList.addElement( ResourceUtils.getBundledString( "ERR_ReturnForVoid" ) );  //NOI18N
                    retOk = false; }
                else if ( ret != Type.VOID && retTags.length <= 0 ) {
                    if ( checkOnly ) {
                        return false; }
                    errorList.addElement(  ResourceUtils.getBundledString( "ERR_NoReturn" ) );  //NOI18N
                    retOk = false;
                } else if ( ret != Type.VOID && retTags.length > 1 ) {
                    if ( checkOnly) {
                        return false; }
                    errorList.addElement( ResourceUtils.getBundledString( "ERR_DuplicatedReturn" ) );   //NOI18N
                    retOk = false; }
                return retOk; }
            boolean isCorrectable() {
                if ( super.isCorrectable() )
                    return true;
                return !checkReturnType( true ); }
            void autoCorrect() throws SourceException {
                JavaDoc jdTemp = JavaDocSupport.createMethodJavaDoc( getJavaDoc().getRawText() );
                super.autoCorrect( jdTemp );
                if (!checkReturnType( true ) ) {
                    if ( ((MethodElement)srcElement).getReturn() != Type.VOID ) {
                        jdTemp.changeTags(
                            new JavaDocTag[] { JavaDocSupport.createTag( TAG_RETURN, "" ) }, // NOI18N
                            JavaDoc.ADD ); }
                    else {
                        JavaDocTag toRemove[] = jdTemp.getTags( TAG_RETURN );
                        jdTemp.changeTags( toRemove, JavaDoc.REMOVE ); } }
                getJavaDoc().setRawText( jdTemp.getRawText() ); }
            ElementFormat getNameFormat () {
                return nameFormat; } } }
    public interface AutoCommentChangeListener extends EventListener {
        public void listChanged(); }
    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addAutoCommentChangeListener(AutoCommentChangeListener listener) throws java.util.TooManyListenersException {
        if (autoCommentChangeListener != null) {
            throw new java.util.TooManyListenersException(); }
        autoCommentChangeListener = listener; }
    /** Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeAutoCommentChangeListener(AutoCommentChangeListener listener) {
        autoCommentChangeListener = null; }
    /** Notifies the registered listener about the event.
     *
     * @param e The event to be fired
     */
    private void fireAutocommentChangeEvent() {
        if (autoCommentChangeListener == null) return;
        autoCommentChangeListener.listChanged(); } }
/*
 * Log
 *  14   Gandalf-post-FCS1.12.1.0    4/13/00  Svatopluk Dedic Fixed #6252
 *  13   Gandalf   1.12        1/14/00  Petr Hrebejk    Exceptions with 
 *       fullnames fix mk2
 *  12   Gandalf   1.11        1/13/00  Petr Hrebejk    AutoComment now 
 *       recognizes exception fullnames
 *  11   Gandalf   1.10        1/12/00  Petr Hrebejk    i18n
 *  10   Gandalf   1.9         1/3/00   Petr Hrebejk    Various bugfixes - 4709,
 *       4978, 5017, 4981, 4976, 5016, 4740,  5005
 *  9    Gandalf   1.8         11/9/99  Petr Hrebejk    Duplicated innerclasses 
 *       in autocomment fixed
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/16/99  Petr Hrebejk    Tag descriptions editing
 *       in HTML editor + localization
 *  6    Gandalf   1.5         8/17/99  Petr Hrebejk    @return tag check
 *  5    Gandalf   1.4         8/16/99  Petr Hrebejk    Default Comment changed 
 *       to Auto-Correct
 *  4    Gandalf   1.3         8/13/99  Petr Hrebejk    Window serialization 
 *       added & Tag change button in Jdoc editor removed 
 *  3    Gandalf   1.2         7/30/99  Petr Hrebejk    Autocomment made 
 *       TopComponent
 *  2    Gandalf   1.1         7/26/99  Petr Hrebejk    AutoComment tool 
 *       implemented
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $ 
 */ 
