/*
 * @(#)Entity.java     1.8 01/12/03
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html.parser;
import java.util.Hashtable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.CharArrayReader;
import java.net.URL;
/**
 * An entity is described in a DTD using the ENTITY construct.
 * It defines the type and value of the the entity.
 *
 * @see DTD
 * @version 1.8, 12/03/01
 * @author Arthur van Hoff
 */
public final
class Entity implements DTDConstants {
    public String name;
    public int type;
    public char data[];
    /**
     * Creates an entity.
     * @param name the name of the entity
     * @param type the type of the entity
     * @param data the char array of data
     */
    public Entity(String name, int type, char data[]) {
       this.name = name;
       this.type = type;
       this.data = data; }
    /**
     * Gets the name of the entity.
     * @return the name of the entity, as a <code>String</code>
     */
    public String getName() {
       return name; }
    /**
     * Gets the type of the entity.
     * @return the type of the entity
     */
    public int getType() {
       return type & 0xFFFF; }
    /**
     * Returns <code>true</code> if it is a parameter entity.
     * @return <code>true</code> if it is a parameter entity
     */
    public boolean isParameter() {
       return (type & PARAMETER) != 0; }
    /**
     * Returns <code>true</code> if it is a general entity.
     * @return <code>true</code> if it is a general entity
     */
    public boolean isGeneral() {
       return (type & GENERAL) != 0; }
    /**
     * Returns the <code>data</code>.
     * @return the <code>data</code>
     */
    public char getData()[] {
       return data; }
    /**
     * Returns the data as a <code>String</code>.
     * @return the data as a <code>String</code>
     */
    public String getString() {
       return new String(data, 0, data.length); }
    static Hashtable entityTypes = new Hashtable();
    static {
       entityTypes.put("PUBLIC", new Integer(PUBLIC));
       entityTypes.put("CDATA", new Integer(CDATA));
       entityTypes.put("SDATA", new Integer(SDATA));
       entityTypes.put("PI", new Integer(PI));
       entityTypes.put("STARTTAG", new Integer(STARTTAG));
       entityTypes.put("ENDTAG", new Integer(ENDTAG));
       entityTypes.put("MS", new Integer(MS));
       entityTypes.put("MD", new Integer(MD));
       entityTypes.put("SYSTEM", new Integer(SYSTEM)); }
    /**
     * Converts <code>nm</code> string to the corresponding
     * entity type.  If the string does not have a corresponding
     * entity type, returns the type corresponding to "CDATA".
     * Valid entity types are: "PUBLIC", "CDATA", "SDATA", "PI",
     * "STARTTAG", "ENDTAG", "MS", "MD", "SYSTEM".
     *
     * @param nm the string to be converted
     * @return the corresponding entity type, or the type corresponding
     *   to "CDATA", if none exists
     */
    public static int name2type(String nm) {
       Integer i = (Integer)entityTypes.get(nm);
       return (i == null) ? CDATA : i.intValue(); } }
