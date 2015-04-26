/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/query/sql/SQLQueryProcessor.java,v 1.9 2003/07/18 23:06:46 farrukh_najmi Exp $
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

package com.sun.ebxml.registry.query.sql;

import java.util.*;
import java.io.*;

import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.query.types.*;
import org.oasis.ebxml.registry.bindings.rim.*;


import com.sun.ebxml.registry.*;
import com.sun.ebxml.registry.persistence.rdb.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processor for SQL queries. Used by the QueryManagerImpl.
 *
 * @see
 * @author Farrukh S. Najmi
 */
public class SQLQueryProcessor {
        private Log log = LogFactory.getLog(this.getClass());
	protected SQLQueryProcessor(){}

	public AdhocQueryResponse executeQuery(User user, String sqlQuery, ResponseOption responseOption) throws RegistryException {
                log.info("executing query: '" + sqlQuery + "')");
		AdhocQueryResponse resp = new AdhocQueryResponse();
		SQLQueryResult sqlResult = new SQLQueryResult();

		try {
			//parse the queryString to sget at certain info like the select column and table name etc.
			InputStream stream = new ByteArrayInputStream(sqlQuery.getBytes());
			SQLParser parser = new SQLParser(stream);
			
			//Fix the query according to the responseOption to return the right type of objects		
			String fixedQuery = parser.processQuery(user, responseOption);

			//Get the ArrayList of objects (ObjectRef, RegistryObject, RegistryEntry, leaf class) as
			//specified by the responeOption
			ArrayList objectRefs = new ArrayList();
			ArrayList objs = SQLPersistenceManagerImpl.getInstance().executeSQLQuery(fixedQuery, responseOption, parser.firstTableName, objectRefs);
			
			if (objs != null) {
				Iterator iter = objs.iterator();
				while (iter.hasNext()) {
					
					Object obj = iter.next();
					
					if (obj.getClass().getName().equals("org.oasis.ebxml.registry.bindings.rim.RegistryObject")) {
						RegistryObjectListTypeItem item = new RegistryObjectListTypeItem();
						item.setRegistryObject((RegistryObject)obj);
						sqlResult.addRegistryObjectListTypeItem(item);				
					}
					else if (obj.getClass().getName().equals("org.oasis.ebxml.registry.bindings.rim.RegistryEntry")) {
						RegistryObjectListTypeItem item = new RegistryObjectListTypeItem();
						item.setRegistryEntry((RegistryEntry)obj);
						sqlResult.addRegistryObjectListTypeItem(item);				
					}
					else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Association) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setAssociation((Association)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.AuditableEvent) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setAuditableEvent((AuditableEvent)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Classification) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setClassification((Classification)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationNode) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setClassificationNode((ClassificationNode)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationScheme) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setClassificationScheme((ClassificationScheme)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setExternalIdentifier((ExternalIdentifier)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExternalLink) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setExternalLink((ExternalLink)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setExtrinsicObject((ExtrinsicObject)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRef) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setObjectRef((ObjectRef)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Organization) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setOrganization((Organization)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.RegistryPackage) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setRegistryPackage((RegistryPackage)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Service) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setService((Service)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ServiceBinding) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setServiceBinding((ServiceBinding)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.SpecificationLink) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setSpecificationLink((SpecificationLink)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.User) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setUser((User)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					} else {
						throw new RegistryException("Unexpected object" + obj);
					} 
				}
				
				// Attaching the ObjectRef to the response. objectsRefs contains duplicates!
				Iterator objectRefsIter = objectRefs.iterator();				
				// It is to store the ObjectRef 's id after removing duplicated ObjectRef. It is a dirty fix, change it later!!!!
				ArrayList finalObjectRefsIds = new ArrayList();
				ArrayList finalObjectRefs = new ArrayList();
				while(objectRefsIter.hasNext()) {
					Object obj = objectRefsIter.next();
					if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRef) {
						ObjectRef objectRef = (ObjectRef) obj;
						String id = objectRef.getId();
						if (!finalObjectRefsIds.contains(id)) {
							finalObjectRefsIds.add(id);
							ObjectRef or = new ObjectRef();
							or.setId(id);
							finalObjectRefs.add(or);
						} 						
					}
					else {
						throw new RegistryException("Unexpected object" + obj);
					}
				} 				
				
				Iterator finalObjectRefsIter = finalObjectRefs.iterator();
				while (finalObjectRefsIter.hasNext()) {
					Object obj = finalObjectRefsIter.next();
					if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRef) {
						LeafRegistryObjectListTypeItem li = new LeafRegistryObjectListTypeItem();
						li.setObjectRef((ObjectRef)obj);					
						sqlResult.addLeafRegistryObjectListTypeItem(li); 
					}
					else {
						throw new RegistryException("Unexpected object" + obj);
					}
				} 
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}


		resp.setSQLQueryResult(sqlResult);				
		
		return resp;
	}
	
    public static SQLQueryProcessor getInstance(){
            if (instance == null) {
                synchronized(com.sun.ebxml.registry.query.sql.SQLQueryProcessor.class) {
                    if (instance == null) {
                        instance = new com.sun.ebxml.registry.query.sql.SQLQueryProcessor();
                    }
                }
            }
            return instance;
        }

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory 
     */
    /*# private SQLQueryProcessor _sqlQueryProcessor; */
    private static SQLQueryProcessor instance = null;
}
