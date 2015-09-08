/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/FilterProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/FilterProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 */
package org.freebxml.omar.server.query.filter;

import org.freebxml.omar.common.RegistryException;

import org.oasis.ebxml.registry.bindings.query.BooleanClauseType;
import org.oasis.ebxml.registry.bindings.query.ClauseType;
import org.oasis.ebxml.registry.bindings.query.CompoundClauseType;
import org.oasis.ebxml.registry.bindings.query.ConnectivePredicate;
import org.oasis.ebxml.registry.bindings.query.FilterType;
import org.oasis.ebxml.registry.bindings.query.LogicalPredicate;
import org.oasis.ebxml.registry.bindings.query.RationalClauseType;
import org.oasis.ebxml.registry.bindings.query.SimpleClauseType;
import org.oasis.ebxml.registry.bindings.query.StringClauseType;
import org.oasis.ebxml.registry.bindings.query.StringPredicate;

import java.math.BigInteger;

import java.sql.Timestamp;

import java.util.Iterator;
import java.util.List;


/**
 * Class Declaration for FilterProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class FilterProcessor implements SQLConverter {
    private String selectColumn = null;
    private boolean isNativeFilter = false;
    private boolean isReverseSelectNeeded = false;
    private ClauseHandler clauseHandler = new ClauseHandler();
    private SQLClause sqlClause = new SQLClause();

    public SQLClause getNewClause(FilterType filter) throws RegistryException {
        sqlClause.clause = convertToSQL(filter);
        sqlClause.isSubSelectNeeded = !isNativeFilter;
        sqlClause.isReverseSelectNeeded = isReverseSelectNeeded;

        return sqlClause;
    }

    public void setSelectColumn(String selColumn) {
        selectColumn = selColumn;
    }

    public boolean isReverseSelectNeeded() {
        return isReverseSelectNeeded;
    }

    public String convertToSQL(Object obj) throws RegistryException {
        if (!(obj instanceof org.oasis.ebxml.registry.bindings.query.FilterType)) {
            throw new RegistryException("Unexpected object " + obj +
                ". Was expecting org.oasis.ebxml.registry.bindings.query.FilterType.");
        }

        FilterType filter = (FilterType) obj;
        String sqlQuery = null;
        String whereClause = null;

        if (!isNativeFilter) {
            sqlQuery = "SELECT " + selectColumn + " FROM " +
                getTableName(filter);
            whereClause = "WHERE ";
        } else {
            whereClause = "";
        }

        ClauseType clause = filter.getClause();
        SimpleClauseType simpleClause = clause.getSimpleClause();
        CompoundClauseType compoundClause = clause.getCompoundClause();

        if (simpleClause != null) {
            whereClause += convertSimpleClause(simpleClause);
        } else if (compoundClause != null) {
            whereClause += convertCompoundClause(compoundClause);
        } else {
            throw new RegistryException("Unexpected Clause " + obj +
                ". Was expecting org.oasis.ebxml.registry.bindings.query.SimpleClause or org.oasis.ebxml.registry.bindings.query.CompoundClause.");
        }

        sqlClause.isReverseSelectNeeded = isReverseSelectNeeded;

        if (!isNativeFilter) {
            return sqlQuery + " " + whereClause;
        } else {
            return whereClause;
        }
    }

    public String addNativeWhereClause(String whereClause,
        FilterType nativeFilter) throws RegistryException {
        /* adds to the existing SQL where clause a clause for Filters that are for the same object as the enclosing Query
           like OrganizationFilter inside OrganizationQuery */
        setNativeFilter(true);
        sqlClause.clause = convertToSQL(nativeFilter);

        return clauseHandler.addWhereClause(whereClause, sqlClause);
    }

    public String addForeignWhereClause(String whereClause,
        FilterType foreignFilter) throws RegistryException {
        /* adds to the existing SQL where clause a clause for Filters that are for the different object then the enclosing Query
           like PostalAddressFilter inside OrganizationQuery */
        setNativeFilter(false);
        sqlClause.clause = convertToSQL(foreignFilter);

        return clauseHandler.addWhereClause(whereClause, sqlClause);
    }

    private void setNativeFilter(boolean isNatFilter) {
        isNativeFilter = isNatFilter;

        if (isNativeFilter) {
            sqlClause.isSubSelectNeeded = false;
            setSelectColumn("");
        } else {
            sqlClause.isSubSelectNeeded = true;
        }
    }

    private String getTableName(FilterType filter) throws RegistryException {
        if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.RegistryObjectFilter")) {
            return "RegistryObject";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.RegistryEntryFilter")) {
            return "RegistryEntry";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.AssociationFilter")) {
            return "Association";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.AuditableEventFilter")) {
            return "AuditableEvent";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ClassificationFilter")) {
            return "Classification";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ClassificationNodeFilter")) {
            return "ClassificationNode";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ClassificationSchemeFilter")) {
            return "ClassificationScheme";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.EmailAddress")) {
            return "EmailAddress";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ExternalIdentifierFilter")) {
            return "ExternalIdentifier";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ExternalLinkFilter")) {
            return "ExternalLink";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ExtrinsicObjectFilter")) {
            return "ExtrinsicObject";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.OrganizationFilter")) {
            return "Organization";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.RegistryPackageFilter")) {
            return "RegistryPackage";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ServiceFilter")) {
            return "Service";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ServiceBindingFilter")) {
            return "ServiceBinding";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.SpecificationLinkFilter")) {
            return "SpecificationLink";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.UserFilter")) {
            return "User";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.SlotFilter")) {
            return "Slot";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.SlotValueFilter")) {
            return "SlotValue";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.PostalAddressFilter")) {
            return "PostalAddress";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.TelephoneNumberFilter")) {
            return "TelephoneNumber";
        } else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.LocalizedStringFilter")) {
            // there are many different tables, like Name, Description, ...
            return "";
        } else {
            throw new RegistryException("Unexpected filter" + filter);
        }
    }

    private String convertSimpleClause(SimpleClauseType simpleClause)
        throws RegistryException {
        StringClauseType stringClause = simpleClause.getStringClause();
        BooleanClauseType booleanClause = simpleClause.getBooleanClause();
        RationalClauseType rationalClause = simpleClause.getRationalClause();
        String predicate = null;
        String whereClause = "";

        if (stringClause != null) {
            StringPredicate stringPredicate = stringClause.getStringPredicate();
            predicate = stringPredicate.toString();
            whereClause += (simpleClause.getLeftArgument() + " " +
            buildStringSQLPredicate(predicate, stringClause.getValue()));
        } else if (booleanClause != null) {
            Boolean boleanPredicate = new Boolean(booleanClause.isBooleanPredicate());
            whereClause += (simpleClause.getLeftArgument() + " = " +
            boleanPredicate.toString());
        } else if (rationalClause != null) {
            String rightArgument = null;
            LogicalPredicate logicalPredicate = rationalClause.getLogicalPredicate();
            predicate = logicalPredicate.toString();

            if (rationalClause.getIntClause() != null) {
                BigInteger number = rationalClause.getIntClause();
                rightArgument = number.toString();
            } else if (rationalClause.getFloatClause() != null) {
                Float number = rationalClause.getFloatClause();
                rightArgument = number.toString();
            } else if (rationalClause.getDateTimeClause() != null) {
                Timestamp dateTime = new Timestamp((rationalClause.getDateTimeClause()).getTimeInMillis());
                rightArgument = dateTime.toString();
            }

            whereClause += (simpleClause.getLeftArgument() + " " +
            buildLogicalSQLPredicate(predicate, rightArgument));
        }

        return whereClause;
    }

    private String convertCompoundClause(CompoundClauseType compoundClause)
        throws RegistryException {
        String whereClause = "";
        List subClause = compoundClause.getClause();
        Iterator iter = subClause.iterator();

        if (subClause.size() != 2) {
            throw new RegistryException("Invalid Compound Clause: " +
                compoundClause);
        } else {
            ConnectivePredicate predicate = compoundClause.getConnectivePredicate();
            String connectivePredicate = predicate.toString();

            int i = 0;

            while (iter.hasNext()) {
                if (i == 0) {
                    if (((ClauseType) iter.next()).getSimpleClause() != null) {
                        whereClause = convertSimpleClause(((ClauseType) iter.next()).getSimpleClause());
                    } else {
                        whereClause = convertCompoundClause(((ClauseType) iter.next()).getCompoundClause());
                    }

                    i++;
                } else {
                    if (((ClauseType) iter.next()).getSimpleClause() != null) {
                        whereClause = clauseHandler.buildConnectiveSQL(whereClause,
                                connectivePredicate,
                                convertSimpleClause(
                                    ((ClauseType) iter.next()).getSimpleClause()));
                    } else {
                        whereClause = clauseHandler.buildConnectiveSQL(whereClause,
                                connectivePredicate,
                                convertCompoundClause(
                                    ((ClauseType) iter.next()).getCompoundClause()));
                    }
                }
            }
        }

        return whereClause;
    }

    private String buildStringSQLPredicate(String stringPredicate,
        String rightArgument) throws RegistryException {
        /* for now use logic of "SOME", not "ALL/NONE" -> isReverseSelectNeeded not active */
        isReverseSelectNeeded = false;

        if (stringPredicate.equals("Contains")) {
            return "LIKE '%" + rightArgument + "%'";
        } else if (stringPredicate.equals("-Contains")) {
            isReverseSelectNeeded = true;

            // return "LIKE '%" + rightArgument + "%'";
            return "NOT LIKE '%" + rightArgument + "%'";
        } else if (stringPredicate.equals("StartsWith")) {
            return "LIKE '" + rightArgument + "%'";
        } else if (stringPredicate.equals("-StartsWith")) {
            isReverseSelectNeeded = true;

            // return "LIKE '" + rightArgument + "%'";
            return "NOT LIKE '" + rightArgument + "%'";
        } else if (stringPredicate.equals("Equal")) {
            return "= '" + rightArgument + "'";
        } else if (stringPredicate.equals("-Equal")) {
            isReverseSelectNeeded = true;

            // return "= '" + rightArgument + "'";
            return "<> '" + rightArgument + "'";
        } else if (stringPredicate.equals("EndsWith")) {
            return "LIKE '%" + rightArgument + "'";
        } else if (stringPredicate.equals("-EndsWith")) {
            isReverseSelectNeeded = true;

            // return "LIKE '%" + rightArgument + "'";
            return "NOT LIKE '%" + rightArgument + "'";
        } else {
            throw new RegistryException("Invalid string predicate: " +
                stringPredicate);
        }
    }

    private String buildLogicalSQLPredicate(String logicalPredicate,
        String rightArgument) throws RegistryException {
        /* for now use logic of "SOME", not "ALL/NONE" -> isReverseSelectNeeded not active */
        isReverseSelectNeeded = false;

        if (logicalPredicate.equals("EQ")) {
            return "= " + "\'" + rightArgument + "\'";
        } else if (logicalPredicate.equals("GE")) {
            return ">= " + "\'" + rightArgument + "\'";
        } else if (logicalPredicate.equals("GT")) {
            return ">" + "\'" + rightArgument + "\'";
        } else if (logicalPredicate.equals("LE")) {
            return "<=" + "\'" + rightArgument + "\'";
        } else if (logicalPredicate.equals("LT")) {
            return "<" + "\'" + rightArgument + "\'";
        } else if (logicalPredicate.equals("NE")) {
            isReverseSelectNeeded = true;

            // return "=" + "\'" + rightArgument + "\'";
            return "<>" + "\'" + rightArgument + "\'";
        } else {
            throw new RegistryException("Invalid logical predicate: " +
                logicalPredicate);
        }
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/query/filter/FilterProcessor.java,v 1.8 2002/04/05 21:13:52 nstojano Exp $
 */

package com.sun.ebxml.registry.query.filter;

import java.sql.*;

import com.sun.ebxml.registry.*;

import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.query.types.*;
import org.oasis.ebxml.registry.bindings.rs.*;


/**
 * Class Declaration for FilterProcessor
 * @see
 * @author Nikola Stojanovic
 */

public class FilterProcessor implements SQLConverter {
    
    private String selectColumn = null;
    private boolean isNativeFilter = false;
    private boolean isReverseSelectNeeded = false;
    private ClauseHandler clauseHandler = new ClauseHandler();
    private ClauseType sqlClause = new ClauseType();
    
    
    public ClauseType getNewClause(FilterType filter) throws RegistryException {
        
        sqlClause.clause = convertToSQL(filter);
        sqlClause.isSubSelectNeeded = !isNativeFilter;
        sqlClause.isReverseSelectNeeded = isReverseSelectNeeded;
        return sqlClause;
    }
    
    
    public void setSelectColumn(String selColumn) {
        
        selectColumn = selColumn;
    }
    
    
    public boolean isReverseSelectNeeded() {
        
        return isReverseSelectNeeded;
    }
    
    
    public String convertToSQL(Object obj) throws RegistryException {
        
        if (!(obj instanceof org.oasis.ebxml.registry.bindings.query.FilterType)) {
            throw new RegistryException("Unexpected object " + obj + ". Was expecting org.oasis.ebxml.registry.bindings.query.FilterType.");
        }
        
        FilterType filter = (FilterType)obj;
        String sqlQuery = null;
        String whereClause = null;
        
        if (!isNativeFilter) {
            sqlQuery = "SELECT " + selectColumn + " FROM " + getTableName(filter);
            whereClause = "WHERE ";
        }
        else {
            whereClause = "";
        }
        
        Clause clause = filter.getClause();
        SimpleClause simpleClause = clause.getSimpleClause();
        CompoundClause compoundClause = clause.getCompoundClause();
        
        if (simpleClause != null) {
            whereClause += convertSimpleClause(simpleClause);
        }
        else if (compoundClause != null) {
            whereClause += convertCompoundClause(compoundClause);
        }
        else {
            throw new RegistryException("Unexpected Clause " + obj + ". Was expecting org.oasis.ebxml.registry.bindings.query.SimpleClause or org.oasis.ebxml.registry.bindings.query.CompoundClause.");
        }
        
        sqlClause.isReverseSelectNeeded = isReverseSelectNeeded;
        
        if (!isNativeFilter) {
            return sqlQuery + " " + whereClause;
        }
        else {
            return whereClause;
        }
    }
    
    
    public String addNativeWhereClause(String whereClause, FilterType nativeFilter) throws RegistryException {
        
        /* adds to the existing SQL where clause a clause for Filters that are for the same object as the enclosing Query
           like OrganizationFilter inside OrganizationQuery */
        
        setNativeFilter(true);
        sqlClause.clause = convertToSQL(nativeFilter);
        return clauseHandler.addWhereClause(whereClause, sqlClause);
    }
    
    
    public String addForeignWhereClause(String whereClause, FilterType foreignFilter) throws RegistryException {
        
        /* adds to the existing SQL where clause a clause for Filters that are for the different object then the enclosing Query
           like PostalAddressFilter inside OrganizationQuery */
        
        setNativeFilter(false);
        sqlClause.clause = convertToSQL(foreignFilter);
        return clauseHandler.addWhereClause(whereClause, sqlClause);
    }
    
    
    private void setNativeFilter(boolean isNatFilter) {
        
        isNativeFilter = isNatFilter;
        
        if (isNativeFilter) {
            sqlClause.isSubSelectNeeded = false;
            setSelectColumn("");
        }
        else {
            sqlClause.isSubSelectNeeded = true;
        }
    }
    
    
    private String getTableName(FilterType filter) throws RegistryException {
        
        if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.RegistryObjectFilter")) {
            return "RegistryObject";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.RegistryEntryFilter")) {
            return "RegistryEntry";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.AssociationFilter")) {
            return "Association";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.AuditableEventFilter")) {
            return "AuditableEvent";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ClassificationFilter")) {
            return "Classification";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ClassificationNodeFilter")) {
            return "ClassificationNode";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ClassificationSchemeFilter")) {
            return "ClassificationScheme";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.EmailAddress")) {
            return "EmailAddress";
        }        
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ExternalIdentifierFilter")) {
            return "ExternalIdentifier";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ExternalLinkFilter")) {
            return "ExternalLink";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ExtrinsicObjectFilter")) {
            return "ExtrinsicObject";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.OrganizationFilter")) {
            return "Organization";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.RegistryPackageFilter")) {
            return "RegistryPackage";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ServiceFilter")) {
            return "Service";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.ServiceBindingFilter")) {
            return "ServiceBinding";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.SpecificationLinkFilter")) {
            return "SpecificationLink";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.UserFilter")) {
            return "User";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.SlotFilter")) {
            return "Slot";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.SlotValueFilter")) {
            return "SlotValue";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.PostalAddressFilter")) {
            return "PostalAddress";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.TelephoneNumberFilter")) {
            return "TelephoneNumber";
        }
        else if (filter.getClass().getName().equals("org.oasis.ebxml.registry.bindings.query.LocalizedStringFilter")) {
            // there are many different tables, like Name, Description, ...
            return "";
        }
        else {
            throw new RegistryException("Unexpected filter" + filter);
        }
    }
    
    
    private String convertSimpleClause(SimpleClause simpleClause) throws RegistryException {
        
        StringClause stringClause = simpleClause.getStringClause();
        BooleanClause booleanClause = simpleClause.getBooleanClause();
        RationalClause rationalClause = simpleClause.getRationalClause();
        String predicate = null;
        String whereClause = "";
        
        if (stringClause != null) {
            StringPredicateType stringPredicate = stringClause.getStringPredicate();
            predicate = stringPredicate.toString();
            whereClause += simpleClause.getLeftArgument() + " " + buildStringSQLPredicate(predicate, stringClause.getContent());
        }
        else if (booleanClause != null) {
            Boolean boleanPredicate = new Boolean(booleanClause.getBooleanPredicate());
            whereClause += simpleClause.getLeftArgument() + " = " + boleanPredicate.toString();
        }
        else if (rationalClause != null) {
            String rightArgument = null;
            LogicalPredicateType logicalPredicate = rationalClause.getLogicalPredicate();
            predicate = logicalPredicate.toString();
            
            if (rationalClause.hasIntClause()) {
                Integer number = new Integer(rationalClause.getIntClause());
                rightArgument = number.toString();
            }
            else if (rationalClause.hasFloatClause()) {
                Float number = new Float(rationalClause.getFloatClause());
                rightArgument = number.toString();
            }
            else {
                Timestamp dateTime = new Timestamp(((rationalClause.getDateTimeClause()).getTime()));
                rightArgument = dateTime.toString();
            }
            
            whereClause += simpleClause.getLeftArgument() + " " + buildLogicalSQLPredicate(predicate, rightArgument);
        }
        
        return whereClause;
    }
    
    
    private String convertCompoundClause(CompoundClause compoundClause) throws RegistryException {
        
        String whereClause = "";
        Clause subClause[] = compoundClause.getClause();
        ConnectivePredicateType predicate = compoundClause.getConnectivePredicate();
        String connectivePredicate = predicate.toString();
        
        if (subClause.length != 2) {
            throw new RegistryException("Invalid Compound Clause: " + compoundClause);
        }
        else {
            for (int i=0; i<subClause.length; i++) {
                if (i == 0) {
                    if (subClause[i].getSimpleClause() != null) {
                        whereClause = convertSimpleClause(subClause[i].getSimpleClause());
                    }
                    else {
                        whereClause = convertCompoundClause(subClause[i].getCompoundClause());
                    }
                }
                else {
                    if (subClause[i].getSimpleClause() != null) {
                        whereClause = clauseHandler.buildConnectiveSQL(whereClause, connectivePredicate, convertSimpleClause(subClause[i].getSimpleClause()));
                    }
                    else {
                        whereClause = clauseHandler.buildConnectiveSQL(whereClause, connectivePredicate, convertCompoundClause(subClause[i].getCompoundClause()));
                    }
                }
            }
        }
        
        return whereClause;
    }
    
    
    private String buildStringSQLPredicate(String stringPredicate, String rightArgument) throws RegistryException {
        
        /* for now use logic of "SOME", not "ALL/NONE" -> isReverseSelectNeeded not active */
        isReverseSelectNeeded = false;
        
        if (stringPredicate.equals("Contains")) {
            return "LIKE '%" + rightArgument + "%'";
        }
        else if (stringPredicate.equals("-Contains")) {
            isReverseSelectNeeded = true;
            // return "LIKE '%" + rightArgument + "%'";
            return "NOT LIKE '%" + rightArgument + "%'";
        }
        else if (stringPredicate.equals("StartsWith")) {
            return "LIKE '" + rightArgument + "%'";
        }
        else if (stringPredicate.equals("-StartsWith")) {
            isReverseSelectNeeded = true;
            // return "LIKE '" + rightArgument + "%'";
            return "NOT LIKE '" + rightArgument + "%'";
        }
        else if (stringPredicate.equals("Equal")) {
            return "= '" + rightArgument + "'";
        }
        else if (stringPredicate.equals("-Equal")) {
            isReverseSelectNeeded = true;
            // return "= '" + rightArgument + "'";
            return "<> '" + rightArgument + "'";
        }
        else if (stringPredicate.equals("EndsWith")) {
            return "LIKE '%" + rightArgument + "'";
        }
        else if (stringPredicate.equals("-EndsWith")) {
            isReverseSelectNeeded = true;
            // return "LIKE '%" + rightArgument + "'";
            return "NOT LIKE '%" + rightArgument + "'";
        }
        else {
            throw new RegistryException("Invalid string predicate: " + stringPredicate);
        }
    }
    
    
    private String  buildLogicalSQLPredicate(String logicalPredicate, String rightArgument) throws RegistryException {
        
        /* for now use logic of "SOME", not "ALL/NONE" -> isReverseSelectNeeded not active */
        isReverseSelectNeeded = false;
        
        if (logicalPredicate.equals("EQ")) {
            return "= " + "\'" + rightArgument + "\'";
        }
        else if (logicalPredicate.equals("GE")) {
            return ">= " + "\'" + rightArgument + "\'";
        }
        else if (logicalPredicate.equals("GT")) {
            return ">" + "\'" + rightArgument + "\'";
        }
        else if (logicalPredicate.equals("LE")) {
            return "<=" + "\'" + rightArgument + "\'";
        }
        else if (logicalPredicate.equals("LT")) {
            return "<" + "\'" + rightArgument + "\'";
        }
        else if (logicalPredicate.equals("NE")) {
            isReverseSelectNeeded = true;
            // return "=" + "\'" + rightArgument + "\'";
            return "<>" + "\'" + rightArgument + "\'";
        }
        else {
            throw new RegistryException("Invalid logical predicate: " + logicalPredicate);
        }
    }
}