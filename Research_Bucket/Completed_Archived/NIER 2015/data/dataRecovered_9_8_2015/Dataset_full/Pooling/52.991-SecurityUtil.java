package com.sun.ebxml.registry.security;

import com.sun.ebxml.registry.*;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

// jaxm
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;

// java mail
import javax.mail.internet.ParseException;
import javax.mail.internet.MimeMultipart;
import javax.mail.BodyPart;
import javax.mail.MessagingException;

// jaxp
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;

import org.apache.xpath.XPathAPI;

// XML security
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.c14n.*;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.*;
import org.apache.xml.security.keys.*;
import org.apache.xml.security.keys.content.*;
import org.apache.xml.security.keys.content.x509.*;
import org.apache.xml.security.keys.keyresolver.*;
import org.apache.xml.security.keys.storage.*;
import org.apache.xml.security.keys.storage.implementations.*;
import org.apache.xml.security.utils.*;
import org.apache.xml.security.transforms.*;
import org.apache.xml.security.Init;
import org.apache.xml.security.utils.resolver.implementations.ResolverFragment;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.serialize.*;

// xerces
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.LineSeparator;

import com.sun.ebxml.registry.util.Utility;

import org.apache.commons.logging.*;

import com.sun.ebxml.registry.util.*;

/**
 *
 * Some utility methods related to XML security
 *
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/security/SecurityUtil.java,v 1.19 2002/11/29 12:34:55 farrukh_najmi Exp $
 *
 */
public class SecurityUtil {
    private org.apache.commons.logging.Log  log = LogFactory.getLog(this.getClass());
    
    static {
        org.apache.xml.security.Init.init();
    }
    
    protected SecurityUtil() {}
    
    /**
     * Sign the SOAP messasge
     * @param soapStream is the InputStream to String representation of the SOAPMessage
     * @param signedSoapStream is the OutputStream for signed SOAPMessage of String representation
     * @param certs the certificate chain for verifying the signature
     */
    public void signSOAPMessage(InputStream soapStream, OutputStream signedSoapStream, PrivateKey privateKey,
    java.security.cert.Certificate [] certs, String signatureAlgo) throws RegistryException {
        try {
            
            //FileWriter fw = new FileWriter("sign_log.txt", true);
            log.info("Enter signSOAPMessage at: " + System.currentTimeMillis());
            
            //long begin = System.currentTimeMillis();
            
            
            
            // Firstly get the Document form of the SOAPMessage - Should we pool the DocumentBuilder??????????
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            
            //StringBuffer buf = Utility.getInstance().getStringBufferFromInputStream(soapStream);
            //System.err.println(buf);
            Document soapDoc = db.parse(new InputSource(soapStream));
            
            // Append the signature element to proper location before signing
            Element headerElement = (Element) soapDoc.getDocumentElement().getElementsByTagNameNS(
            "http://schemas.xmlsoap.org/soap/envelope/", "Header").item(0);
            String baseURI = "";
            if (!signatureAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_DSA) && !signatureAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_RSA )) {
                throw new RegistryException("Unsupported signature algorithm");
            }
            XMLSignature sig = new XMLSignature(soapDoc, baseURI, signatureAlgo);
            headerElement.appendChild(sig.getElement());
            
            // Start signing here
            
                        /*
            // Due to the bug in the Apache security lib, it does not allow us to sign whole message and make "enveloped-signature" transform.
            // It is mandatory requirement but the version we are using yields much better performance.
            sig.getSignedInfo().addResourceResolver(new ResolverFragment());
                        Transforms transforms = new Transforms(soapDoc);
                        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
                        // Sign whole message
            sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);
                         */
            
            // only sign the body - IT IS NOT CONFORMED TO THE SPEC!!!!!!
            sig.addDocument("#Body");
            // attach the certs chain
            for (int i=0; i < certs.length; i++) {
                sig.addKeyInfo((X509Certificate)certs[i]);
            }
            sig.addKeyInfo(((X509Certificate)certs[0]).getPublicKey());
            sig.sign(privateKey);
            
            // Get back SOAPMessage form of the signed message
            XMLUtils.outputDOMc14nWithComments(soapDoc, signedSoapStream);
            log.info("Leaving signSOAPMessage at: " + System.currentTimeMillis());
            
            //long finish = System.currentTimeMillis();
            
            //fw.write("Signing takes " + (finish - begin) / 1000 / 60 + " mins\n");
            //fw.flush();
            //fw.close();
            
        }
        catch (IOException e) {
            throw new RegistryException(e);
        }
        catch (SAXException e) {
            throw new RegistryException(e);
        }
        catch (ParserConfigurationException e) {
            throw new RegistryException(e);
        }
        catch (XMLSecurityException e) {
            throw new RegistryException(e);
        }
        
    }
    
    /**
     * Sign the payload, and the payload signature is put in the destination
     * connected by the payloadSigStream parameter.
     * @param mimeMultipart a MimeMultipart containing a MimeBodyPart, which contains
     * the payload to be signed. The MimeBodyPart should have content id "payload2".
     * @param payloadSigStream the destination connected by this OutputStream contains
     * the payload signature
     */
    public void signPayload(MimeMultipart mp, String id, OutputStream payloadSigStream,
    PrivateKey privateKey, java.security.cert.Certificate cert, String signingAlgo) throws RegistryException {
        
        try {
            if (mp.getCount() != 1) {
                throw new RegistryException("Cannot sign the payload. The MimeMultipart should have only one MimeBodyPart with content id 'payload2'");
            }
            if (mp.getBodyPart("payload2")==null) {
                throw new RegistryException("Cannot sign the payload. The MimeBodyPart contained in the MimeMultipart should have content id 'payload2'");
            }
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            // org.w3c.dom.Document sigDoc = db.parse(new InputSource(new StringReader(sigText)));
            org.w3c.dom.Document sigDoc = db.newDocument();
            
            // Append the signature element to proper location before signing
            String baseURI = "";
            if (!signingAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_DSA) && !signingAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_RSA )) {
                throw new RegistryException("Unsupported signature algorithm");
            }
            XMLSignature sig = new XMLSignature(sigDoc, baseURI, signingAlgo);
            // sigDoc.getDocumentElement().appendChild(sig.getElement());
            sigDoc.appendChild(sig.getElement());
            
            // here we only add the target cert of the cert chain
            sig.addKeyInfo((X509Certificate)cert);
            sig.addKeyInfo(((X509Certificate)cert).getPublicKey());
            
            ResourceResolver resolver = new ResourceResolver(new PayloadResolver(mp, id));
            //resolver.register("org.apache.xml.security.utils.resolver.implementations.ResolverLocalFilesystem");
            sig.addResourceResolver(resolver);
            
            //sig.addDocument("file:///" + fileName);
            sig.addDocument("payload2");
            
            sig.sign(privateKey);
            
            // FileOutputStream payloadSigStream = new FileOutputStream("payloadSig.xml");
            XMLUtils.outputDOMc14nWithComments(sigDoc, payloadSigStream);
            payloadSigStream.flush();
            payloadSigStream.close();
        }
        catch(IOException e) {
            throw new RegistryException("Cannot sign the payload", e);
        }
        catch(ParserConfigurationException e) {
            throw new RegistryException("Cannot sign the payload", e);
        }
        catch(MessagingException e) {
            throw new RegistryException("Cannot sign the payload", e);
        }
        catch(XMLSecurityException e) {
            throw new RegistryException("Cannot sign the payload", e);
        }
        
    }
    
    public XMLSignature verifySOAPMessage(SOAPMessage msg) throws RegistryException {
        XMLSignature signature = null;

        try {
            //long begin = System.currentTimeMillis();
            //FileWriter fw = new FileWriter("sign_log.txt", true);
            log.info("Entering verifySOAPMessage at: " + System.currentTimeMillis());
            
            // Get the Document form of the SOAPMessage
            ByteArrayOutputStream msgOutStream = new ByteArrayOutputStream();
            msg.writeTo(msgOutStream);
            ByteArrayInputStream msgInStream = new ByteArrayInputStream(msgOutStream.toByteArray());
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setAttribute("http://xml.org/sax/features/namespaces", Boolean.TRUE);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new org.apache.xml.security.utils.IgnoreAllErrorHandler());
            org.w3c.dom.Document doc = db.parse(new InputSource(msgInStream));
            
            // Get the signature
            Element headerElement = (Element) doc.getDocumentElement().getElementsByTagNameNS(
            "http://schemas.xmlsoap.org/soap/envelope/", "Header").item(0);
            if (headerElement == null) {
                throw new RegistryException("Missing SOAP Header");
            }
            Element sigElement = (Element) headerElement.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature").
            item(0);
            
            if (sigElement != null) {
                signature = new XMLSignature(sigElement, "");
                signature.addResourceResolver(new ResolverFragment());
                if (!verifyXMLSignature(signature)) {
                    handleHeaderVerificationException(new RegistryException("Invalid signature. The message is tampered with in transit."));
                }
            }
            log.info("Leaving verifySOAPMessage at: " + System.currentTimeMillis());
            
            //long finish = System.currentTimeMillis();
            //fw.write("verification takes " + (finish - begin) / 1000 / 60 + " mins\n");
            //fw.flush();
            //fw.close();
        }        
        catch (Exception e) {
            handleHeaderVerificationException(e);
        }
        
        return signature;        
    }
    
    private void handleHeaderVerificationException(Exception e) throws RegistryException {
        boolean ignoreInvalidHeaderSignatures = Boolean.valueOf(RegistryProperties.getInstance().getProperty("ebxmlrr.security.ignoreInvalidHeaderSignatures")).booleanValue();
        if (ignoreInvalidHeaderSignatures) {
            log.error("Invalid header signature. The message is tampered with in transit." + e.toString());
        }
        else {
            throw new RegistryException("Invalid header signature. The message is tampered with in transit.", e);
        }
    }
    
    /**
     * It expects the XMLSignature has been add ResourceResolver appropriately
     */
    protected boolean verifyXMLSignature(XMLSignature signature) throws RegistryException, KeyResolverException, Exception {
        
        KeyInfo ki = signature.getKeyInfo();
        
        if (ki != null) {
            if (!ki.containsX509Data()) {
                throw new RegistryException("Missing X509Data element in the KeyInfo element");
            }
            X509Certificate cert = signature.getKeyInfo().getX509Certificate();
            if (cert != null) {
                return signature.checkSignatureValue(cert);
            }
            else {
                throw new RegistryException("Missing X509Certificate element in Signature element");
            }
        }
        else {
            throw new RegistryException("Missing KeyIno element in Signature element");
        }
    }
    
    public boolean verifyPayloadSignature(String id, MimeMultipart multipart) throws RegistryException {
        boolean isValid = false;
        try {
            if (multipart.getCount() != 2) {
                throw new RegistryException("Cannot verify the payload signature. The MimeMultipart should have two MimeBodyPart");
            }
            BodyPart signaturePart = multipart.getBodyPart("payload1");
            if (signaturePart == null) {
                throw new RegistryException("Cannot verify the payload signature. The MimeMultipart does not have MimeBodyPart with content id 'payload1'");
            }
            BodyPart payloadPart = multipart.getBodyPart("payload2");
            if (payloadPart == null) {
                throw new RegistryException("Cannot verify the payload signature. The MimeMultipart does not have MimeBodyPart with content id 'payload2'");
            }
            
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document sigDoc = db.parse(new InputSource(signaturePart.getInputStream()));
            XMLSignature signature = new XMLSignature(sigDoc.getDocumentElement(), "");
            signature.addResourceResolver(new PayloadResolver(multipart, id));
            isValid = verifyXMLSignature(signature);
            
            if (!isValid) {
                boolean ignoreInvalidPayloadSignatures = Boolean.valueOf(RegistryProperties.getInstance().getProperty("ebxmlrr.security.ignoreInvalidPayloadSignatures")).booleanValue();
                if (ignoreInvalidPayloadSignatures) {
                    isValid = true;
                }
            }
        }
        catch(IOException e) {
            handlePayloadVerificationException(e);
        }
        catch(SAXException e) {
            handlePayloadVerificationException(e);
        }
        catch(MessagingException e) {
            handlePayloadVerificationException(e);
        }
        catch(ParserConfigurationException e) {
            handlePayloadVerificationException(e);
        }
        catch(XMLSignatureException e) {
            handlePayloadVerificationException(e);
        }
        catch(KeyResolverException e) {
            handlePayloadVerificationException(e);
        }
        catch(XMLSecurityException e) {
            handlePayloadVerificationException(e);
        }
        catch(Exception e) {
            handlePayloadVerificationException(e);
        }
        
        return isValid;
    }
    
    private void handlePayloadVerificationException(Exception e) throws RegistryException {
        boolean ignoreInvalidPayloadSignatures = Boolean.valueOf(RegistryProperties.getInstance().getProperty("ebxmlrr.security.ignoreInvalidPayloadSignatures")).booleanValue();
        if (ignoreInvalidPayloadSignatures) {
            log.error("Cannot verify the payload signature. " + e.toString());
        }
        else {
            throw new RegistryException("Cannot verify the payload signature", e);
        }
    }
    
    /**
     * Method main
     *
     * @param unused
     * @throws Exception
     */
    public static void main(String unused[]) throws Exception {
    }
    
    public static SecurityUtil getInstance() {
        if (instance == null) {
            synchronized (SecurityUtil.class) {
                if (instance == null) {
                    instance = new SecurityUtil();
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
    
    /* # private SecurityUtil _utility; */
    private static SecurityUtil  instance = null;
    
}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/common/security/SecurityUtil.java,v 1.3 2004/03/28 20:35:48 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.security;

import org.apache.commons.logging.LogFactory;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.security.utils.resolver.implementations.ResolverFragment;

import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.common.CommonProperties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPMessage;


/**
 *
 * Some utility methods related to XML security
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/common/security/SecurityUtil.java,v 1.3 2004/03/28 20:35:48 farrukh_najmi Exp $
 *
 */
public class SecurityUtil {
    static {
        org.apache.xml.security.Init.init();
    }

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /* # private SecurityUtil _utility; */
    private static SecurityUtil instance = null;
    private org.apache.commons.logging.Log log = LogFactory.getLog(this.getClass());

    protected SecurityUtil() {
    }

    /**
     * Sign the SOAP messasge
     * @param soapStream is the InputStream to String representation of the SOAPMessage
     * @param signedSoapStream is the OutputStream for signed SOAPMessage of String representation
     * @param certs the certificate chain for verifying the signature
     */
    public void signSOAPMessage(InputStream soapStream,
        OutputStream signedSoapStream, PrivateKey privateKey,
        java.security.cert.Certificate[] certs, String signatureAlgo)
        throws RegistryException {
        try {
            //FileWriter fw = new FileWriter("sign_log.txt", true);
            log.trace("Enter signSOAPMessage at: " + System.currentTimeMillis());

            //long begin = System.currentTimeMillis();
            // Firstly get the Document form of the SOAPMessage - Should we pool the DocumentBuilder??????????
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();

            //StringBuffer buf = Utility.getInstance().getStringBufferFromInputStream(soapStream);
            //System.err.println(buf);
            Document soapDoc = db.parse(new InputSource(soapStream));

            // Append the signature element to proper location before signing
            Element headerElement = (Element) soapDoc.getDocumentElement()
                                                     .getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/",
                    "Header").item(0);
            String baseURI = "";

            if (!signatureAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_DSA) &&
                    !signatureAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_RSA)) {
                throw new RegistryException("Unsupported signature algorithm");
            }

            XMLSignature sig = new XMLSignature(soapDoc, baseURI, signatureAlgo);
            headerElement.appendChild(sig.getElement());

            // Start signing here
            /*
            // Due to the bug in the Apache security lib, it does not allow us to sign whole message and make "enveloped-signature" transform.
            // It is mandatory requirement but the version we are using yields much better performance.
            sig.getSignedInfo().addResourceResolver(new ResolverFragment());
            Transforms transforms = new Transforms(soapDoc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            // Sign whole message
            sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);
             */
            // only sign the body - IT IS NOT CONFORMED TO THE SPEC!!!!!!
            sig.addDocument("#Body");

            // attach the certs chain
            for (int i = 0; i < certs.length; i++) {
                sig.addKeyInfo((X509Certificate) certs[i]);
            }

            sig.addKeyInfo(((X509Certificate) certs[0]).getPublicKey());
            sig.sign(privateKey);

            // Get back SOAPMessage form of the signed message
            XMLUtils.outputDOMc14nWithComments(soapDoc, signedSoapStream);
            log.trace("Leaving signSOAPMessage at: " +
                System.currentTimeMillis());

            //long finish = System.currentTimeMillis();
            //fw.write("Signing takes " + (finish - begin) / 1000 / 60 + " mins\n");
            //fw.flush();
            //fw.close();
        } catch (IOException e) {
            throw new RegistryException(e);
        } catch (SAXException e) {
            throw new RegistryException(e);
        } catch (ParserConfigurationException e) {
            throw new RegistryException(e);
        } catch (XMLSecurityException e) {
            throw new RegistryException(e);
        }
    }

    /**
     * Sign the payload, and the payload signature is put in the destination
     * connected by the payloadSigStream parameter.
     * @param mimeMultipart a MimeMultipart containing a MimeBodyPart, which contains
     * the payload to be signed. The MimeBodyPart should have content id "payload2".
     * @param payloadSigStream the destination connected by this OutputStream contains
     * the payload signature
     */
    public void signPayload(MimeMultipart mp, String id,
        OutputStream payloadSigStream, PrivateKey privateKey,
        java.security.cert.Certificate cert, String signingAlgo)
        throws RegistryException {
        try {
            if (mp.getCount() != 1) {
                throw new RegistryException(
                    "Cannot sign the payload. The MimeMultipart should have only one MimeBodyPart with content id 'payload2'");
            }

            if (mp.getBodyPart("payload2") == null) {
                throw new RegistryException(
                    "Cannot sign the payload. The MimeBodyPart contained in the MimeMultipart should have content id 'payload2'");
            }

            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();

            // org.w3c.dom.Document sigDoc = db.parse(new InputSource(new StringReader(sigText)));
            org.w3c.dom.Document sigDoc = db.newDocument();

            // Append the signature element to proper location before signing
            String baseURI = "";

            if (!signingAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_DSA) &&
                    !signingAlgo.equals(XMLSignature.ALGO_ID_SIGNATURE_RSA)) {
                throw new RegistryException("Unsupported signature algorithm");
            }

            XMLSignature sig = new XMLSignature(sigDoc, baseURI, signingAlgo);

            // sigDoc.getDocumentElement().appendChild(sig.getElement());
            sigDoc.appendChild(sig.getElement());

            // here we only add the target cert of the cert chain
            sig.addKeyInfo((X509Certificate) cert);
            sig.addKeyInfo(((X509Certificate) cert).getPublicKey());

            ResourceResolver resolver = new ResourceResolver(new PayloadResolver(
                        mp, id));

            //resolver.register("org.apache.xml.security.utils.resolver.implementations.ResolverLocalFilesystem");
            sig.addResourceResolver(resolver);

            //sig.addDocument("file:///" + fileName);
            sig.addDocument("payload2");

            sig.sign(privateKey);

            // FileOutputStream payloadSigStream = new FileOutputStream("payloadSig.xml");
            XMLUtils.outputDOMc14nWithComments(sigDoc, payloadSigStream);
            payloadSigStream.flush();
            payloadSigStream.close();
        } catch (IOException e) {
            throw new RegistryException("Cannot sign the payload", e);
        } catch (ParserConfigurationException e) {
            throw new RegistryException("Cannot sign the payload", e);
        } catch (MessagingException e) {
            throw new RegistryException("Cannot sign the payload", e);
        } catch (XMLSecurityException e) {
            throw new RegistryException("Cannot sign the payload", e);
        }
    }

    public XMLSignature verifySOAPMessage(SOAPMessage msg)
        throws RegistryException {
        XMLSignature signature = null;

        try {
            //long begin = System.currentTimeMillis();
            //FileWriter fw = new FileWriter("sign_log.txt", true);
            log.info("Entering verifySOAPMessage at: " +
                System.currentTimeMillis());

            // Get the Document form of the SOAPMessage
            ByteArrayOutputStream msgOutStream = new ByteArrayOutputStream();
            msg.writeTo(msgOutStream);

            ByteArrayInputStream msgInStream = new ByteArrayInputStream(msgOutStream.toByteArray());
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setAttribute("http://xml.org/sax/features/namespaces",
                Boolean.TRUE);

            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new org.apache.xml.security.utils.IgnoreAllErrorHandler());

            org.w3c.dom.Document doc = db.parse(new InputSource(msgInStream));

            // Get the signature
            Element headerElement = (Element) doc.getDocumentElement()
                                                 .getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/",
                    "Header").item(0);

            if (headerElement == null) {
                throw new RegistryException("Missing SOAP Header");
            }

            Element sigElement = (Element) headerElement.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#",
                    "Signature").item(0);

            if (sigElement != null) {
                signature = new XMLSignature(sigElement, "");
                signature.addResourceResolver(new ResolverFragment());

                if (!verifyXMLSignature(signature)) {
                    handleHeaderVerificationException(new RegistryException(
                            "Invalid signature. The message is tampered with in transit."));
                }
            }

            log.info("Leaving verifySOAPMessage at: " +
                System.currentTimeMillis());

            //long finish = System.currentTimeMillis();
            //fw.write("verification takes " + (finish - begin) / 1000 / 60 + " mins\n");
            //fw.flush();
            //fw.close();
        } catch (Exception e) {
            handleHeaderVerificationException(e);
        }

        return signature;
    }

    private void handleHeaderVerificationException(Exception e)
        throws RegistryException {
        boolean ignoreInvalidHeaderSignatures = Boolean.valueOf(CommonProperties.getInstance()
                                                                                  .getProperty("omar.security.ignoreInvalidHeaderSignatures", "true"))
                                                       .booleanValue();

        if (ignoreInvalidHeaderSignatures) {
            log.error(
                "Invalid header signature. The message is tampered with in transit." +
                e.toString());
        } else {
            throw new RegistryException("Invalid header signature. The message is tampered with in transit.",
                e);
        }
    }

    /**
     * It expects the XMLSignature has been add ResourceResolver appropriately
     */
    protected boolean verifyXMLSignature(XMLSignature signature)
        throws RegistryException, KeyResolverException, Exception {
        KeyInfo ki = signature.getKeyInfo();

        if (ki != null) {
            if (!ki.containsX509Data()) {
                throw new RegistryException(
                    "Missing X509Data element in the KeyInfo element");
            }

            X509Certificate cert = signature.getKeyInfo().getX509Certificate();

            if (cert != null) {
                return signature.checkSignatureValue(cert);
            } else {
                throw new RegistryException(
                    "Missing X509Certificate element in Signature element");
            }
        } else {
            throw new RegistryException(
                "Missing KeyIno element in Signature element");
        }
    }

    public boolean verifyPayloadSignature(String id, MimeMultipart multipart)
        throws RegistryException {
        boolean isValid = false;

        try {
            if (multipart.getCount() != 2) {
                throw new RegistryException(
                    "Cannot verify the payload signature. The MimeMultipart should have two MimeBodyPart");
            }

            BodyPart signaturePart = multipart.getBodyPart("payload1");

            if (signaturePart == null) {
                throw new RegistryException(
                    "Cannot verify the payload signature. The MimeMultipart does not have MimeBodyPart with content id 'payload1'");
            }

            BodyPart payloadPart = multipart.getBodyPart("payload2");

            if (payloadPart == null) {
                throw new RegistryException(
                    "Cannot verify the payload signature. The MimeMultipart does not have MimeBodyPart with content id 'payload2'");
            }

            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document sigDoc = db.parse(new InputSource(
                        signaturePart.getInputStream()));
            XMLSignature signature = new XMLSignature(sigDoc.getDocumentElement(),
                    "");
            signature.addResourceResolver(new PayloadResolver(multipart, id));
            isValid = verifyXMLSignature(signature);

            if (!isValid) {
                boolean ignoreInvalidPayloadSignatures = Boolean.valueOf(CommonProperties.getInstance()
                                                                                           .getProperty("omar.security.ignoreInvalidPayloadSignatures"))
                                                                .booleanValue();

                if (ignoreInvalidPayloadSignatures) {
                    isValid = true;
                }
            }
        } catch (IOException e) {
            handlePayloadVerificationException(e);
        } catch (SAXException e) {
            handlePayloadVerificationException(e);
        } catch (MessagingException e) {
            handlePayloadVerificationException(e);
        } catch (ParserConfigurationException e) {
            handlePayloadVerificationException(e);
        } catch (XMLSignatureException e) {
            handlePayloadVerificationException(e);
        } catch (KeyResolverException e) {
            handlePayloadVerificationException(e);
        } catch (XMLSecurityException e) {
            handlePayloadVerificationException(e);
        } catch (Exception e) {
            handlePayloadVerificationException(e);
        }

        return isValid;
    }

    private void handlePayloadVerificationException(Exception e)
        throws RegistryException {
        boolean ignoreInvalidPayloadSignatures = Boolean.valueOf(CommonProperties.getInstance()
                                                                                   .getProperty("omar.security.ignoreInvalidPayloadSignatures"))
                                                        .booleanValue();

        if (ignoreInvalidPayloadSignatures) {
            log.error("Cannot verify the payload signature. " + e.toString());
        } else {
            throw new RegistryException("Cannot verify the payload signature", e);
        }
    }

    /**
     * Method main
     *
     * @param unused
     * @throws Exception
     */
    public static void main(String[] unused) throws Exception {
    }

    public static SecurityUtil getInstance() {
        if (instance == null) {
            synchronized (SecurityUtil.class) {
                if (instance == null) {
                    instance = new SecurityUtil();
                }
            }
        }

        return instance;
    }
}
