/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/FilterQueryProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 * ====================================================================
 */
/*
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/query/filter/FilterQueryProcessor.java,v 1.5 2003/11/11 13:46:50 doballve Exp $
 */
package org.freebxml.omar.server.query.filter;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.query.sql.SQLQueryProcessor;

import org.oasis.ebxml.registry.bindings.query.FilterQueryResult;
import org.oasis.ebxml.registry.bindings.query.FilterQueryResultType;
import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * Class Declaration for FilterQueryProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class FilterQueryProcessor {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private FilterQueryProcessor _filterQueryProcessor; */
    private static FilterQueryProcessor instance = null;
    private static SQLQueryProcessor sqlQueryProcessor = SQLQueryProcessor.getInstance();

    protected FilterQueryProcessor() {
    }

    public FilterQueryResultType executeQuery(UserType user,
        FilterQueryType filterQuery, ResponseOptionType responseOption)
        throws RegistryException {
        String sqlQuery = null;

        if (filterQuery.getRegistryObjectQuery() != null) {
            RegistryObjectQueryProcessor registryObjectQueryProcessor = new RegistryObjectQueryProcessor();
            sqlQuery = registryObjectQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getRegistryObjectQuery());
        } else if (filterQuery.getRegistryEntryQuery() != null) {
            RegistryEntryQueryProcessor registryEntryQueryProcessor = new RegistryEntryQueryProcessor();
            sqlQuery = registryEntryQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getRegistryEntryQuery());
        } else if (filterQuery.getAssociationQuery() != null) {
            AssociationQueryProcessor associationQueryProcessor = new AssociationQueryProcessor();
            sqlQuery = associationQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getAssociationQuery());
        } else if (filterQuery.getAuditableEventQuery() != null) {
            AuditableEventQueryProcessor auditableEventQueryProcessor = new AuditableEventQueryProcessor();
            sqlQuery = auditableEventQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getAuditableEventQuery());
        } else if (filterQuery.getClassificationQuery() != null) {
            ClassificationQueryProcessor classificationQueryProcessor = new ClassificationQueryProcessor();
            sqlQuery = classificationQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getClassificationQuery());
        } else if (filterQuery.getClassificationNodeQuery() != null) {
            ClassificationNodeQueryProcessor classificationNodeQueryProcessor = new ClassificationNodeQueryProcessor();
            sqlQuery = classificationNodeQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getClassificationNodeQuery());
        } else if (filterQuery.getClassificationSchemeQuery() != null) {
            ClassificationSchemeQueryProcessor classificationSchemeQueryProcessor =
                new ClassificationSchemeQueryProcessor();
            sqlQuery = classificationSchemeQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getClassificationSchemeQuery());
        } else if (filterQuery.getExtrinsicObjectQuery() != null) {
            ExtrinsicObjectQueryProcessor extrinsicObjectQueryProcessor = new ExtrinsicObjectQueryProcessor();
            sqlQuery = extrinsicObjectQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getExtrinsicObjectQuery());
        } else if (filterQuery.getOrganizationQuery() != null) {
            OrganizationQueryProcessor organizationQueryProcessor = new OrganizationQueryProcessor();
            sqlQuery = organizationQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getOrganizationQuery());
        } else if (filterQuery.getRegistryPackageQuery() != null) {
            RegistryPackageQueryProcessor registryPackageQueryProcessor = new RegistryPackageQueryProcessor();
            sqlQuery = registryPackageQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getRegistryPackageQuery());
        } else if (filterQuery.getServiceQuery() != null) {
            ServiceQueryProcessor serviceQueryProcessor = new ServiceQueryProcessor();
            sqlQuery = serviceQueryProcessor.convertToSQL((RegistryObjectQueryType) filterQuery.getServiceQuery());
        } else {
            throw new RegistryException("Unexpected Filter Query Request " +
                filterQuery);
        }

        RegistryObjectListType sqlResult = sqlQueryProcessor.executeQuery(user,
                sqlQuery, responseOption);

        FilterQueryResultType fqResult = fixAdhocQueryResponseXSLT(filterQuery,
                sqlResult);

        return fqResult;
    }

    public static FilterQueryProcessor getInstance() {
        if (instance == null) {
            synchronized (org.freebxml.omar.server.query.filter.FilterQueryProcessor.class) {
                if (instance == null) {
                    instance = new org.freebxml.omar.server.query.filter.FilterQueryProcessor();
                }
            }
        }

        return instance;
    }

    /**
     * Fixes the AdhocQueryResponse to have a FilterQueryResult instead of SQLQueryResult.
     * Uses XSLT instead of castor stuff.
     */
    private FilterQueryResultType fixAdhocQueryResponseXSLT(
        FilterQueryType filterQuery, RegistryObjectListType sqlResult)
        throws RegistryException {
        FilterQueryResult fqResult = null;

        try {
            //Determine param1 which is teh sub-element of FilterQueryResult
            String param1 = null;

            if (filterQuery.getAssociationQuery() != null) {
                param1 = "AssociationQueryResult";
            } else if (filterQuery.getAuditableEventQuery() != null) {
                param1 = "AuditableEventQueryResult";
            } else if (filterQuery.getClassificationNodeQuery() != null) {
                param1 = "ClassificationNodeQueryResult";
            } else if (filterQuery.getClassificationQuery() != null) {
                param1 = "ClassificationQueryResult";
            } else if (filterQuery.getClassificationSchemeQuery() != null) {
                param1 = "ClassificationSchemeQueryResult";
            } else if (filterQuery.getExtrinsicObjectQuery() != null) {
                param1 = "ExtrinsicObjectQueryResult";
            } else if (filterQuery.getOrganizationQuery() != null) {
                param1 = "OrganizationQueryResult";
            } else if (filterQuery.getRegistryEntryQuery() != null) {
                param1 = "RegistryEntryQueryResult";
            } else if (filterQuery.getRegistryObjectQuery() != null) {
                param1 = "RegistryObjectQueryResult";
            } else if (filterQuery.getRegistryPackageQuery() != null) {
                param1 = "RegistryPackageQueryResult";
            } else if (filterQuery.getServiceQuery() != null) {
                param1 = "ServiceQueryResult";
            } else {
                throw new RegistryException(
                    "Unknown sub-element of FilterQuery");
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();
            File xslFile = new File(RegistryProperties.getInstance()
                                                      .getProperty("omar.home"),
                    "data" + File.separator + "xsl" + File.separator +
                    "sqlQueryResultToFilterQueryResult.xsl");
            Transformer transformer = tFactory.newTransformer(new StreamSource(
                        xslFile));

            StringWriter sw = new StringWriter();
            BindingUtility.getInstance().getJAXBContext().createMarshaller()
                          .marshal(sqlResult, sw);

            StringReader sr = new StringReader(sw.toString());
            StreamSource input = new StreamSource(sr);
            sw = new StringWriter();

            transformer.setParameter("param1", param1);
            transformer.transform(input, new StreamResult(sw));

            sr = new StringReader(sw.toString());
            fqResult = (FilterQueryResult) BindingUtility.getInstance()
                                                         .getJAXBContext()
                                                         .createUnmarshaller()
                                                         .unmarshal(new StreamSource(
                        sr));
        } catch (TransformerConfigurationException e) {
            throw new RegistryException(e);
        } catch (TransformerException e) {
            throw new RegistryException(e);
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }

        return fqResult;
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/query/filter/FilterQueryProcessor.java,v 1.11 2002/12/27 13:27:00 ritzmann Exp $
 */

package com.sun.ebxml.registry.query.filter;

import com.sun.ebxml.registry.*;
import com.sun.ebxml.registry.util.*;

import org.oasis.ebxml.registry.bindings.rim.*;
import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.query.types.*;
import org.oasis.ebxml.registry.bindings.rs.*;

import com.sun.ebxml.registry.*;
import com.sun.ebxml.registry.query.filter.*;
import com.sun.ebxml.registry.query.sql.*;
import com.sun.ebxml.registry.persistence.rdb.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import java.io.*;

/**
 * Class Declaration for FilterQueryProcessor
 * @see
 * @author Nikola Stojanovic
 */
public class FilterQueryProcessor {
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private FilterQueryProcessor _filterQueryProcessor; */
    private static FilterQueryProcessor instance = null;
    private static SQLQueryProcessor sqlQueryProcessor = SQLQueryProcessor.getInstance();
    private String sqlQuery = null;
    
    
    public AdhocQueryResponse executeQuery(User user, FilterQuery filterQueryRequest, ResponseOption responseOption) throws RegistryException {
        
        AdhocQueryResponse response = new AdhocQueryResponse();
        
        if (filterQueryRequest.getRegistryObjectQuery() != null) {
            RegistryObjectQueryProcessor registryObjectQueryProcessor = new RegistryObjectQueryProcessor();
            sqlQuery = registryObjectQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getRegistryObjectQuery());
        }
        else if (filterQueryRequest.getRegistryEntryQuery() != null) {
            RegistryEntryQueryProcessor registryEntryQueryProcessor = new RegistryEntryQueryProcessor();
            sqlQuery = registryEntryQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getRegistryEntryQuery());
        }
        else if (filterQueryRequest.getAssociationQuery() != null) {
            AssociationQueryProcessor associationQueryProcessor = new AssociationQueryProcessor();
            sqlQuery = associationQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getAssociationQuery());
        }
        else if (filterQueryRequest.getAuditableEventQuery() != null) {
            AuditableEventQueryProcessor auditableEventQueryProcessor = new AuditableEventQueryProcessor();
            sqlQuery = auditableEventQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getAuditableEventQuery());
        }
        else if (filterQueryRequest.getClassificationQuery() != null) {
            ClassificationQueryProcessor classificationQueryProcessor = new ClassificationQueryProcessor();
            sqlQuery = classificationQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getClassificationQuery());
        }
        else if (filterQueryRequest.getClassificationNodeQuery() != null) {
            ClassificationNodeQueryProcessor classificationNodeQueryProcessor = new ClassificationNodeQueryProcessor();
            sqlQuery = classificationNodeQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getClassificationNodeQuery());
        }
        else if (filterQueryRequest.getClassificationSchemeQuery() != null) {
            ClassificationSchemeQueryProcessor classificationSchemeQueryProcessor = new ClassificationSchemeQueryProcessor();
            sqlQuery = classificationSchemeQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getClassificationSchemeQuery());
        }
        else if (filterQueryRequest.getExtrinsicObjectQuery() != null) {
            ExtrinsicObjectQueryProcessor extrinsicObjectQueryProcessor = new ExtrinsicObjectQueryProcessor();
            sqlQuery = extrinsicObjectQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getExtrinsicObjectQuery());
        }
        else if (filterQueryRequest.getOrganizationQuery() != null) {
            OrganizationQueryProcessor organizationQueryProcessor = new OrganizationQueryProcessor();
            sqlQuery = organizationQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getOrganizationQuery());
        }
        else if (filterQueryRequest.getRegistryPackageQuery() != null) {
            RegistryPackageQueryProcessor registryPackageQueryProcessor = new RegistryPackageQueryProcessor();
            sqlQuery = registryPackageQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getRegistryPackageQuery());
        }
        else if (filterQueryRequest.getServiceQuery() != null) {
            ServiceQueryProcessor serviceQueryProcessor = new ServiceQueryProcessor();
            sqlQuery = serviceQueryProcessor.convertToSQL((RegistryObjectQueryType)filterQueryRequest.getServiceQuery());
        }
        else {
            throw new RegistryException("Unexpected Filter Query Request " + filterQueryRequest);
        }
        
        response = sqlQueryProcessor.executeQuery(user, sqlQuery, responseOption);
        response = fixAdhocQueryResponseXSLT(filterQueryRequest, response);
        
        return response;
    }
    
    
    public static FilterQueryProcessor getInstance(){
        
        if (instance == null) {
            synchronized(com.sun.ebxml.registry.query.filter.FilterQueryProcessor.class) {
                if (instance == null) {
                    instance = new com.sun.ebxml.registry.query.filter.FilterQueryProcessor();
                }
            }
        }
        
        return instance;
    }
    
    
    protected FilterQueryProcessor(){}
    
    
    /**
     * Fixes the AdhocQueryResponse to have a FilterQueryResult instead of SQLQueryResult.
     * Uses XSLT instead of castor stuff.
     */
    private AdhocQueryResponse fixAdhocQueryResponseXSLT(FilterQuery fqReq, AdhocQueryResponse resp) throws RegistryException {
        
        try {
            
            //Determine param1 which is teh sub-element of FilterQueryResult
            String param1 = null;
            
            if (fqReq.getAssociationQuery() != null) {
                param1 = "AssociationQueryResult";
            }
            else if (fqReq.getAuditableEventQuery() != null) {
                param1 = "AuditableEventQueryResult";
            }
            else if (fqReq.getClassificationNodeQuery() != null) {
                param1 = "ClassificationNodeQueryResult";
            }
            else if (fqReq.getClassificationQuery()  != null) {
                param1 = "ClassificationQueryResult";
            }
            else if (fqReq.getClassificationSchemeQuery() != null) {
                param1 = "ClassificationSchemeQueryResult";
            }
            else if (fqReq.getExtrinsicObjectQuery() != null) {
                param1 = "ExtrinsicObjectQueryResult";
            }
            else if (fqReq.getOrganizationQuery() != null) {
                param1 = "OrganizationQueryResult";
            }
            else if (fqReq.getRegistryEntryQuery() != null) {
                param1 = "RegistryEntryQueryResult";
            }
            else if (fqReq.getRegistryObjectQuery() != null) {
                param1 = "RegistryObjectQueryResult";
            }
            else if (fqReq.getRegistryPackageQuery() != null) {
                param1 = "RegistryPackageQueryResult";
            }
            else if (fqReq.getServiceQuery() != null) {
                param1 = "ServiceQueryResult";
            }
            else {
                throw new RegistryException("Unknown sub-element of FilterQuery");
            }
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            File xslFile = new File(RegistryProperties.getInstance().getProperty("ebxmlrr.home"),
                                    "data" + File.separator + "xsl" + File.separator + "sqlQueryResultToFilterQueryResult.xsl");
            Transformer transformer = tFactory.newTransformer(new StreamSource(xslFile));
            
            StringWriter sw = new StringWriter();
            resp.marshal(sw);
            
            StringReader sr = new StringReader(sw.toString());
            StreamSource input = new StreamSource(sr);
            sw = new StringWriter();
            
            transformer.setParameter("param1", param1);
            transformer.transform(input, new StreamResult(sw));
            
            sr = new StringReader(sw.toString());
            AdhocQueryResponse newResp = resp.unmarshal(sr);
            return newResp;
        }
        catch (TransformerConfigurationException e) {
            throw new RegistryException(e);
        }
        catch (TransformerException e) {
            throw new RegistryException(e);
        }
        catch (org.exolab.castor.xml.MarshalException e) {
            throw new RegistryException(e);
        }
        catch (org.exolab.castor.xml.ValidationException e) {
            throw new RegistryException(e);
        }
    }
}