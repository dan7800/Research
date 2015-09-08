/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,MSHJAXMPingTemplateImpl,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001-2003 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     The Initial Developer of the Original Code is Anders W. Tell.
     Portions created by Financial Toolsmiths AB are Copyright (C) 
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File MSHJAXMPingTemplateImpl.java			*/

package org.openebxml.ebxml.msh.v20.integration.template.jaxm;

/************************************************
	Includes
\************************************************/

import org.openebxml.ebxml.msh.v20.integration.template.MSHPingTemplateImpl;
import org.openebxml.ebxml.msh.v20.integration.*;
import javax.xml.soap.*;
import org.openebxml.ebxml.msh.v20.integration.template.jaxm.MSHJAXMTemplateUtil;

/**
 * Class MSHJAXMPingTemplateImpl
 *
 * Default implementation for creating a JAXM MSH Ping message.
 *
 * @author nikos dimitrakas DSV SU/KTH
 */

public class MSHJAXMPingTemplateImpl extends MSHPingTemplateImpl implements MSHJAXMTemplate
{
    /****/
    public static final String IMPLEMENTATION        = "openebxml:ebxml:msh:template:ping:jaxm:default";
    /****/
    public static final String IMPLEMENTATION_VERSION	= "1.0";
    /****/
    public static final String IMPLEMENTATION_VENDOR	= "openebxmllab";
    
    /*---------------------------------------------------*/
    MessageFactory fMessageFactory;
    
    /****/
    public MSHJAXMPingTemplateImpl(MessageFactory mf) {
        fMessageFactory = mf;
    }
    
    /** Retrieves a implementation identifier.
     */
    public String getImplementation() 
    {
        return IMPLEMENTATION;
    }
    
    /** Retrieves a implementation vendor identifier.
     */
    public String getImplementationVendor() 
    {
        return IMPLEMENTATION_VENDOR;
    }
    
    /** Retrieves a implementation version identifier.
     */
    public String getImplementationVersion() 
    {
        return IMPLEMENTATION_VERSION;
    }
    
    /** Returns the message as an instance of javax.xml.soap.SOAPMessage.
     *
     */
    public javax.xml.soap.SOAPMessage getJAXMMessage() 
    {
        try
        {
            SOAPMessage lMessage = fMessageFactory.createMessage();
            SOAPPart sp = lMessage.getSOAPPart();
            SOAPEnvelope lEnvelope = sp.getEnvelope();
            SOAPHeader lHeader = lEnvelope.getHeader();
            MSHJAXMTemplateUtil.PopulateJAXMMessageHeader(lEnvelope, lMessageHeader, lHeader);
            return lMessage;
        }
        catch (SOAPException sex)
        {
            System.out.println(sex);
            return null;
        }
   }
 

}


/*.IEnd,MSHJAXMPingTemplateImpl,====================================*/
