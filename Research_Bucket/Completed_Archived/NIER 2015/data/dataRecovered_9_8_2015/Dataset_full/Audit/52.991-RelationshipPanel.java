/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/graph/RelationshipPanel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 * ====================================================================
 */

/**
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/graph/RelationshipPanel.java,v 1.3 2003/10/26 13:19:29 farrukh_najmi Exp $
 *
 *
 */
package org.freebxml.omar.client.ui.swing.graph;

import org.freebxml.omar.client.ui.swing.AssociationPanel;
import org.freebxml.omar.client.ui.swing.CardManagerPanel;
import org.freebxml.omar.client.ui.swing.JAXRClient;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;

import java.awt.GridBagConstraints;

import java.util.ArrayList;

import javax.swing.JPanel;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;


/**
 * A panel that allows setting different types of relationships between two RegistryObjects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RelationshipPanel extends CardManagerPanel {
    public static final String RELATIONSHIP_TYPE_ASSOCIATION = "Association";
    public static final String RELATIONSHIP_TYPE_REFERENCE = "Reference";
    private static final String REL_CLASSIFICATION_RO = "classifiedObject";
    private static final String REL_CLASSIFICATION_SCHEME = "classificationScheme";
    private static final String REL_CLASSIFICATION_CONCEPT = "concept";
    private static final String REL_CONCEPT_PARENT = "parent";
    private static final String REL_EXTERNALID_IDSCHEME = "identificationScheme";
    private static final String REL_ORGANIZATION_CHILDORGS = "childOrganizations";
    private static final String REL_ORGANIZATION_CONTACT = "primaryContact";
    private static final String REL_BINDING_SPECLINKS = "specificationLinks";
    private static final String REL_SERVICE_BINDINGS = "serviceBindings";
    private static final String REL_SPECLINK_SPEC = "specificationObject";
    private GridBagConstraints c = new GridBagConstraints();
    private RegistryObject src = null;
    private RegistryObject target = null;
    private String relationshipType = RELATIONSHIP_TYPE_ASSOCIATION;
    private AssociationPanel assPanel;
    private ReferencePanel refPanel;
    private ArrayList map = new ArrayList();
    private String[][][] refMatrix = {
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //0 - Association
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //1 - AuditableEvent
        {
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_SCHEME, REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_CONCEPT, REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO }
        },
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //3 - ClassificationScheme
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //4 - Concept
        {
            {  },
            {  },
            {  },
            { REL_EXTERNALID_IDSCHEME },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //5 - ExternalIdentifier
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //6 - ExternalLink
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //7 - ExtrinsicObject
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            { REL_ORGANIZATION_CHILDORGS },
            {  },
            {  },
            {  },
            {  },
            { REL_ORGANIZATION_CONTACT }
        }, //8 - Organization
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //9 - RegistryPackage
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            { REL_BINDING_SPECLINKS },
            {  }
        }, //10 - ServiceBinding
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            { REL_SERVICE_BINDINGS },
            {  },
            {  },
            {  }
        }, //11 - Service
        {
            {  },
            {  },
            {  },
            {  },
            { REL_SPECLINK_SPEC },
            {  },
            {  },
            { REL_SPECLINK_SPEC },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //12- SpecificationLink
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //13 - User
    };

    /**
     * Class Constructor.
     */
    public RelationshipPanel(RegistryObject src, RegistryObject target) {
        super(new String[] {
                RELATIONSHIP_TYPE_ASSOCIATION, RELATIONSHIP_TYPE_REFERENCE
            },
            new JPanel[] { new AssociationPanel(), new ReferencePanel(src,
                    target) });

        assPanel = (AssociationPanel) cardPanels[0];
        refPanel = (ReferencePanel) cardPanels[1];

        this.src = src;
        this.target = target;

        map.add("Association"); //0
        map.add("AuditableEvent"); //1
        map.add("Classification"); //2
        map.add("ClassificationScheme"); //3
        map.add("Concept"); //4
        map.add("ExternalIdentifier"); //5
        map.add("ExternalLink"); //6
        map.add("ExtrinsicObject"); //7
        map.add("Organization"); //8
        map.add("RegistryPackage"); //9
        map.add("ServiceBinding"); //10
        map.add("Service"); //11
        map.add("SpecificationLink"); //12
        map.add("User"); //13

        int row = map.indexOf(getJAXRName(src));
        int col = map.indexOf(getJAXRName(target));

        String[] refs = refMatrix[row][col];

        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl bqm = (org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl) (client.getBusinessQueryManager());

            if (refs.length == 0) {
                remove(selectorPanel);
                initAssociationPanel();
            } else {
                relationshipType = RELATIONSHIP_TYPE_REFERENCE;
                refPanel.setReferenceAttributes(refs);
            }

            showCard(relationshipType);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    private void initAssociationPanel() throws JAXRException {
        JAXRClient client = RegistryBrowser.getInstance().getClient();
        BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
        org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl bqm = (org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl) (client.getBusinessQueryManager());

        relationshipType = RELATIONSHIP_TYPE_ASSOCIATION;

        Concept assType = null;

        //Set associationType for any pre-defined associations
        if (src instanceof RegistryPackage) {
            assType = bqm.findConceptByPath(
                    "/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" +
                    org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl.ASSOCIATION_TYPE_HAS_MEMBER);
        } else if (src instanceof ExternalLink) {
            assType = bqm.findConceptByPath(
                    "/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" +
                    org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl.ASSOCIATION_TYPE_EXTERNALLY_LINKS);
        } else if ((src instanceof Organization) &&
                (target instanceof Service)) {
            assType = bqm.findConceptByPath(
                    "/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" +
                    org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl.ASSOCIATION_TYPE_OFFERS_SERVICE);
        }

        //this.remove(selectorPanel);
        Association ass = lcm.createAssociation(target, assType);
        src.addAssociation(ass);
        assPanel.setModel(ass);
    }

    protected void showCardAction(String card) {
        if (card.equals(RELATIONSHIP_TYPE_ASSOCIATION)) {
            try {
                if (assPanel.getModel() == null) {
                    initAssociationPanel();
                }
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        }

        super.showCardAction(card);
    }

    public void setReferenceAttributeOnSourceObject() throws JAXRException {
        if (relationshipType == RELATIONSHIP_TYPE_REFERENCE) {
            refPanel.setReferenceAttributeOnSourceObject();
        }
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public String getRelationshipName() {
        String relName = "";

        try {
            if (relationshipType == RELATIONSHIP_TYPE_ASSOCIATION) {
                Concept assType = ((Association) (assPanel.getModel())).getAssociationType();

                if (assType != null) {
                    relName = assType.getValue();
                }
            } else if (relationshipType == RELATIONSHIP_TYPE_REFERENCE) {
                relName = refPanel.getReferenceAttribute();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relName;
    }

    public Association getAssociation() throws JAXRException {
        return (Association) (assPanel.getModel());
    }

    String getJAXRName(RegistryObject ro) {
        String newClassName = ro.getClass().getName();
        newClassName = newClassName.substring(newClassName.lastIndexOf(".") +
                1);

        if (newClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            newClassName = newClassName.substring(0, newClassName.length() - 4);
        }

        return newClassName;
    }
}
/**
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/client/browser/graph/RelationshipPanel.java,v 1.7 2003/08/24 22:14:56 farrukh_najmi Exp $
 *
 *
 */
package com.sun.xml.registry.client.browser.graph;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.beans.*;
import javax.swing.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import com.sun.xml.registry.client.browser.conf.bindings.*;
import com.sun.xml.registry.client.browser.conf.bindings.types.*;
//import org.oasis.ebxml.registry.bindings.rim.*;

import com.sun.xml.registry.client.browser.*;

/**
 * A panel that allows setting different types of relationships between two RegistryObjects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RelationshipPanel extends CardManagerPanel  {
    
    private GridBagConstraints c = new GridBagConstraints();
    private RegistryObject src = null;
    private RegistryObject target = null;
        
    public static final String RELATIONSHIP_TYPE_ASSOCIATION="Association";
    public static final String RELATIONSHIP_TYPE_REFERENCE="Reference";
    
    private String relationshipType = RELATIONSHIP_TYPE_ASSOCIATION;

    private AssociationPanel assPanel;
    private ReferencePanel refPanel;

    private ArrayList map = new ArrayList();        
            
    private static final String REL_CLASSIFICATION_RO = "classifiedObject";
    private static final String REL_CLASSIFICATION_SCHEME = "classificationScheme";
    private static final String REL_CLASSIFICATION_CONCEPT = "concept";
        
    private static final String REL_CONCEPT_PARENT = "parent";
    
    private static final String REL_EXTERNALID_IDSCHEME = "identificationScheme";
    
    private static final String REL_ORGANIZATION_CHILDORGS = "childOrganizations";
    private static final String REL_ORGANIZATION_CONTACT = "primaryContact";
    
    private static final String REL_BINDING_SPECLINKS = "specificationLinks";

    private static final String REL_SERVICE_BINDINGS = "serviceBindings";

    private static final String REL_SPECLINK_SPEC = "specificationObject";

    private String[][][] refMatrix = {
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //0 - Association
        { {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //1 - AuditableEvent
        {{REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_SCHEME, REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_CONCEPT, REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}, {REL_CLASSIFICATION_RO}},
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //3 - ClassificationScheme
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //4 - Concept
        {{}, {}, {}, {REL_EXTERNALID_IDSCHEME}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //5 - ExternalIdentifier
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //6 - ExternalLink
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //7 - ExtrinsicObject
        {{}, {}, {}, {}, {}, {}, {}, {}, {REL_ORGANIZATION_CHILDORGS}, {}, {}, {}, {}, {REL_ORGANIZATION_CONTACT}}, //8 - Organization
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //9 - RegistryPackage
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {REL_BINDING_SPECLINKS}, {}}, //10 - ServiceBinding
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {REL_SERVICE_BINDINGS}, {}, {}, {}}, //11 - Service
        {{}, {}, {}, {}, {REL_SPECLINK_SPEC}, {}, {}, {REL_SPECLINK_SPEC}, {}, {}, {}, {}, {}, {}}, //12- SpecificationLink
        {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}, //13 - User
    };
        
    /**
     * Class Constructor.
     */
    public RelationshipPanel(RegistryObject src, RegistryObject target) {
        
        super(
            new String[] {RELATIONSHIP_TYPE_ASSOCIATION, RELATIONSHIP_TYPE_REFERENCE}, 
            new JPanel[] {new AssociationPanel(), new ReferencePanel(src, target)}
            );

        assPanel = (AssociationPanel)cardPanels[0];
        refPanel = (ReferencePanel)cardPanels[1];
            
        this.src = src;
        this.target = target;

        map.add("Association");  //0
        map.add("AuditableEvent"); //1
        map.add("Classification"); //2
        map.add("ClassificationScheme"); //3
        map.add("Concept"); //4
        map.add("ExternalIdentifier"); //5
        map.add("ExternalLink"); //6
        map.add("ExtrinsicObject"); //7
        map.add("Organization"); //8
        map.add("RegistryPackage"); //9
        map.add("ServiceBinding"); //10
        map.add("Service"); //11
        map.add("SpecificationLink"); //12
        map.add("User"); //13
       
        
        int row = map.indexOf(getJAXRName(src));
        int col = map.indexOf(getJAXRName(target));
        
        String[] refs = refMatrix[row][col];
        
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            com.sun.xml.registry.ebxml.BusinessQueryManagerImpl bqm = (com.sun.xml.registry.ebxml.BusinessQueryManagerImpl)(client.getBusinessQueryManager());

            if (refs.length == 0) {
                remove(selectorPanel);
                initAssociationPanel();
            }
            else {
                relationshipType = RELATIONSHIP_TYPE_REFERENCE;
                refPanel.setReferenceAttributes(refs);
            }
            showCard(relationshipType);
        }
        catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
                
    }
    
    private void initAssociationPanel() throws JAXRException {
        JAXRClient client = RegistryBrowser.getInstance().getClient();
        BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
        com.sun.xml.registry.ebxml.BusinessQueryManagerImpl bqm = (com.sun.xml.registry.ebxml.BusinessQueryManagerImpl)(client.getBusinessQueryManager());

        relationshipType = RELATIONSHIP_TYPE_ASSOCIATION;
                
        Concept assType = null;

        //Set associationType for any pre-defined associations
        if (src instanceof RegistryPackage) {
            assType = bqm.findConceptByPath("/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" + com.sun.xml.registry.ebxml.infomodel.AssociationImpl.ASSOCIATION_TYPE_HAS_MEMBER);
        }
        else if (src instanceof ExternalLink) {
            assType = bqm.findConceptByPath("/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" +  com.sun.xml.registry.ebxml.infomodel.AssociationImpl.ASSOCIATION_TYPE_EXTERNALLY_LINKS);
        }
        else if ((src instanceof Organization) && (target instanceof Service)) {
            assType = bqm.findConceptByPath("/urn:uuid:6902675f-2f18-44b8-888b-c91db8b96b4d/" +  com.sun.xml.registry.ebxml.infomodel.AssociationImpl.ASSOCIATION_TYPE_OFFERS_SERVICE);
        }
        //this.remove(selectorPanel);

        Association ass = lcm.createAssociation(target, assType);
        src.addAssociation(ass);
        assPanel.setModel(ass);     
    }
    
    protected void showCardAction(String card) {
        if (card.equals(RELATIONSHIP_TYPE_ASSOCIATION)) {
            try {
                if (assPanel.getModel() == null) {
                    initAssociationPanel();
                }
            }
            catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        }
        super.showCardAction(card); 
    }
    
    
    public void setReferenceAttributeOnSourceObject() throws JAXRException {
        if (relationshipType == RELATIONSHIP_TYPE_REFERENCE) {
            refPanel.setReferenceAttributeOnSourceObject();
        }
        
    }

    
    public String getRelationshipType() {
        return relationshipType;
    }
    
    public String getRelationshipName() {
        String relName = "";
        
        try {
            if (relationshipType == RELATIONSHIP_TYPE_ASSOCIATION) {
                Concept assType = ((Association)(assPanel.getModel())).getAssociationType();
                if (assType != null) {
                    relName = assType.getValue();
                }
            }
            else if (relationshipType == RELATIONSHIP_TYPE_REFERENCE) {
                relName = refPanel.getReferenceAttribute();
            }
        }
        catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        return relName;
    }
    
    public Association getAssociation() throws JAXRException {
        return (Association)(assPanel.getModel());
    }

    String getJAXRName(RegistryObject ro) {
        String newClassName = ro.getClass().getName();
        newClassName = newClassName.substring(newClassName.lastIndexOf(".")+1);
        if (newClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            newClassName = newClassName.substring(0, newClassName.length() -4);
        }
        
        return newClassName;
    }
    
    
    
}




