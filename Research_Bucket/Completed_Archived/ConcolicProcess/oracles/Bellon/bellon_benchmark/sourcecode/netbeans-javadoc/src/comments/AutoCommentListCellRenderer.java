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
import java.awt.*;
import javax.swing.*;
import java.util.ResourceBundle;
import org.openide.src.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.lang.ref.SoftReference;
import java.lang.reflect.Modifier;
/** Just sets the right icon to IndexItem
 @author Petr Hrebejk
*/
class AutoCommentListCellRenderer extends DefaultListCellRenderer {
    private static final int offsetPublic = 0;
    private static final int offsetPackage = 4;
    private static final int offsetProtected = 8;
    private static final int offsetPrivate = 12;
    private static final int iconNothing = 0;
    private static final int iconClass = 1;
    private static final int iconInterface = 2;
    private static final int iconField = 3;
    private static final int iconConstructor = iconField + offsetPrivate + 1;
    private static final int iconMethod = iconConstructor + offsetPrivate + 1;
    private static ImageIcon theIcon = new ImageIcon();
    static final long serialVersionUID =-5753071739523904697L;
    public Component getListCellRendererComponent( JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        JLabel cr = (JLabel)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
        if ( value == AutoCommentPanel.WAIT_STRING ) {
            cr.setText( NbBundle.getMessage( AutoCommentListCellRenderer.class, "CTL_Wait" ) ); //NOI18N
            theIcon.setImage( Utilities.loadImage("/org/openide/resources/src/wait.gif") ); //NOI18N
        } else {
            cr.setText(((AutoCommenter.Element)value).getName() );
            theIcon.setImage( getMergedImage( (AutoCommenter.Element)value ) ); }
        cr.setIcon( theIcon );
        return cr; }
    private final Image getMergedImage( AutoCommenter.Element el ) {
        int error = el.getErrorNumber();
        int type = resolveIconIndex( el );
        Image im1 = getImage(error);
        Image im2 = getMemberImage(type);
        return Utilities.mergeImages( im1, im2, 18, 0 ); }
    private int resolveIconIndex( AutoCommenter.Element el ) {
        MemberElement me = el.getSrcElement();
        int offset;
        int modifier = me.getModifiers();
        if ((Modifier.PUBLIC & modifier) != 0)
            offset = offsetPublic;
        else if ((Modifier.PRIVATE & modifier) != 0)
            offset = offsetPrivate;
        else if ((Modifier.PROTECTED & modifier) != 0)
            offset = offsetProtected;
        else offset = offsetPackage;
        if ( me instanceof ClassElement )
            return offset + (((ClassElement) me).isInterface() ? iconInterface : iconClass);
        else if ( me instanceof MethodElement )
            return offset + iconMethod;
        else if ( me instanceof ConstructorElement )
            return offset + iconConstructor;
        else if ( me instanceof FieldElement )
            return offset + iconField;
        else
            return iconNothing; }
    /**
     * @param index
     * @return  */    
    private Image getMemberImage(int index){
        switch(index){
            case iconClass + offsetPackage:
            case iconClass + offsetProtected:
            case iconClass + offsetPrivate:
            case iconClass + offsetPublic:
                return Utilities.loadImage("/org/openide/resources/src/class.gif"); // NOI18N
            case iconInterface + offsetPackage:
            case iconInterface + offsetProtected:
            case iconInterface + offsetPrivate:
            case iconInterface + offsetPublic:
                return Utilities.loadImage("/org/openide/resources/src/interface.gif"); // NOI18N
            case iconField + offsetPublic:
                return Utilities.loadImage("/org/openide/resources/src/variablePublic.gif"); // NOI18N
            case iconField + offsetPackage:
                return Utilities.loadImage("/org/openide/resources/src/variablePackage.gif"); // NOI18N
            case iconField + offsetProtected:
                return Utilities.loadImage("/org/openide/resources/src/variableProtected.gif"); // NOI18N
            case iconField + offsetPrivate:
                return Utilities.loadImage("/org/openide/resources/src/variablePrivate.gif"); // NOI18N
            case iconConstructor + offsetPublic:
                return Utilities.loadImage("/org/openide/resources/src/constructorPublic.gif"); // NOI18N
            case iconConstructor + offsetPackage:
                return Utilities.loadImage("/org/openide/resources/src/constructorPackage.gif"); // NOI18N
            case iconConstructor + offsetProtected:
                return Utilities.loadImage("/org/openide/resources/src/constructorProtected.gif"); // NOI18N
            case iconConstructor + offsetPrivate:
                return Utilities.loadImage("/org/openide/resources/src/constructorPrivate.gif"); // NOI18N
            case iconMethod + offsetPublic:
                return Utilities.loadImage("/org/openide/resources/src/methodPublic.gif"); // NOI18N
            case iconMethod + offsetPackage:
                return Utilities.loadImage("/org/openide/resources/src/methodPackage.gif"); // NOI18N
            case iconMethod + offsetProtected:
                return Utilities.loadImage("/org/openide/resources/src/methodProtected.gif"); // NOI18N
            case iconMethod + offsetPrivate:
                return Utilities.loadImage("/org/openide/resources/src/methodPrivate.gif"); // NOI18N
            default: 
                return null; } }
    /** Used to returnn image for given index, not cached here, by Utilities
     * @param index of Image
     * @return Image for index
     */    
    private Image getImage(int index){
        switch(index){
            case 1:
                return Utilities.loadImage("/org/netbeans/modules/javadoc/comments/resources/ok.gif"); // NOI18N
            case 2:
                return Utilities.loadImage("/org/netbeans/modules/javadoc/comments/resources/missing.gif"); // NOI18N
            case 4:
                return Utilities.loadImage("/org/netbeans/modules/javadoc/comments/resources/error.gif"); // NOI18N        
            default: 
                return null; } } }
/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Petr Hrebejk    i18n
 *  6    Gandalf   1.5         1/3/00   Petr Hrebejk    Various bugfixes - 4709,
 *       4978, 5017, 4981, 4976, 5016, 4740,  5005
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/17/99  Petr Hrebejk    @return tag check
 *  2    Gandalf   1.1         8/13/99  Petr Hrebejk    Exception icon added
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $ 
 */ 
