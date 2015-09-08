/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,MSHPingTemplateImpl,======================================*/
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
/*.IA,	PUBLIC Include File MSHPingTemplateImpl.java			*/

package org.openebxml.ebxml.msh.v20.integration.template;

/************************************************
	Includes
\************************************************/

import org.openebxml.ebxml.msh.v20.integration.template.MSHPingTemplate;
import org.openebxml.ebxml.msh.v20.integration.*;

/**
 * Class MSHPingTemplateImpl
 *
 * Default implementation for creating a MSH Ping message.
 *
 * @author nikos dimitrakas DSV SU/KTH
 */

public abstract class MSHPingTemplateImpl extends MSHMessageHeaderTemplateImpl implements MSHPingTemplate
{
    /****/
    public static final String DEFAULT_SERVICE = "urn:oasis:names:tc:ebxml-msg:service";   
    /****/
    public static final String DEFAULT_ACTION  = "Ping";

    /** Initiates the templating mechanism and optionally sets predefined and default values in the related BPSSInstance.
     */
    public void Initiate() 
    {
        toflag = false;
        fromflag = false;
        cpaidflag = false;
        messagedataflag = false;
        completeflag = false;
    
        lMessageHeader = new TRPMessageHeader();
        lMessageHeader.setService(new TRPService(DEFAULT_SERVICE));
        lMessageHeader.setAction(new TRPAction(DEFAULT_ACTION));
    }

    /** Evaluates the template to determine if the template has been filled in competely.
     *
     * @return <code>true</code> if the template has been filled in completely, <code>false</code> otherwise.
     */
    public boolean isComplete() 
    {
        if (toflag && messagedataflag)
            return true;
        return false;
    }
    
    /**
     * Completes the templating mechanism and finishes of any pending changes in the related BPSSInstance.
     */
    public void Complete() 
    {
        if (! completeflag)
        {
            //...
            completeflag = true;
        }
    }

    /** Creates a From element for the Ping message.
     * @param partyId The value of the content of the PartId element. MUST NOT be null.
     * @param partyIdType The value of the type attribute of the PartyId element. MAY be null.
     * @param role The value of the content of the Role element. MAY be null.
     */
    public void setFrom(String partyId, String partyIdType, String role) 
    {
        lMessageHeader.setFrom(new TRPFrom(new TRPPartyId(partyIdType, partyId), new TRPRole(role)));
        fromflag = true;
    }
    
    /** Creates a To element for the Ping message.
     * @param partyId The value of the content of the PartId element. MUST NOT be null.
     * @param partyIdType The value of the type attribute of the PartyId element. MAY be null.
     * @param role The value of the content of the Role element. MAY be null.
     */
    public void setTo(String partyId, String partyIdType, String role) 
    {
        lMessageHeader.setTo(new TRPTo(new TRPPartyId(partyIdType, partyId), new TRPRole(role)));
        toflag = true;
    }

    /** Retrieves a specification identifier.
     */
    public String getSpecification() 
    {
        return SPECIFICATION;
    }
    
    /** Retrieves a specification version identifier.
     */
    public String getSpecificationVersion() 
    {
        return SPECIFICATION_VERSION;
    }
    
}

/*.IEnd,MSHPingTemplateImpl,====================================*/
