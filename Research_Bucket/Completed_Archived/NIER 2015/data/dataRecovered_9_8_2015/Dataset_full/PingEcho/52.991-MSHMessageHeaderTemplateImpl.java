/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,MSHMessageHeaderTemplateImpl,======================================*/
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
/*.IA,	PUBLIC Include File MSHMessageHeaderTemplateImpl.java			*/

package org.openebxml.ebxml.msh.v20.integration.template;

/************************************************
	Includes
\************************************************/
import org.openebxml.comp.util.*;
import org.openebxml.comp.soap.*;
import org.openebxml.comp.bml.*;


import org.openebxml.comp.bml.data.DataDateTime;
import org.openebxml.comp.bml.data.DataNMTOKEN;

import org.openebxml.comp.mime.MimeEntity;

import org.openebxml.ebxml.msh.v20.integration.template.MSHPingTemplate;
import org.openebxml.ebxml.msh.v20.integration.*;

/**
 * Class MSHMessageHeaderTemplateImpl
 *
 * Default implementation for creating a SOAP MSH Ping message.
 *
 * @author nikos dimitrakas DSV SU/KTH
 */

public abstract class MSHMessageHeaderTemplateImpl implements MSHMessageHeaderTemplate
{
    /****/
     public TRPMessageHeader lMessageHeader=null;
    /****/
     boolean toflag = false;
    /****/
     boolean fromflag = false;
    /****/
     boolean cpaidflag = false;
    /****/
     boolean messagedataflag = false;
    /****/
     boolean completeflag = false;

     /****/
     public void Initiate() {
        lMessageHeader=null;
        toflag = false;
        fromflag = false;
        cpaidflag = false;
        messagedataflag = false;
        completeflag = false;
     }
    /** Adds a Description element to the MessageHeader
     * @param description The value of the content of the Description element. MUST NOT be null.
     * @param lang The value of the lang attribute of the Description element. MUST NOT be null.
     */
    public void addDescription(String description, DataNMTOKEN lang) 
    {
        lMessageHeader.addDescription(new TRPDescription(lang, description));
    }
    
    /** Adds a PartyId element to the From element of the MessageHeader
     * @param partyId The value of the content of the PartId element. MUST NOT be null.
     * @param partyIdType The value of the type attribute of the PartyId element. MAY be null.
     */
    public void addFromPartyId(String partyId, String partyIdType) 
    {
        lMessageHeader.getFrom().addPartyId(new TRPPartyId(partyIdType, partyId));
    }
    
    /** Adds a PartyId element to the To element of the MessageHeader
     * @param partyId The value of the content of the PartId element. MUST NOT be null.
     * @param partyIdType The value of the type attribute of the PartyId element. MAY be null.
     */
    public void addToPartyId(String partyId, String partyIdType) 
    {
        lMessageHeader.getTo().addPartyId(new TRPPartyId(partyIdType, partyId));
    }
    
    
    /** Sets the CPAId element of the MessageHeader.
     * If CPAId is not set then the MSH shall recover the value based on the PartyIds orfill in the value by any other means
     * @param CPAId The value of the content of the CPAId element. MAY be null.
     */
    public void setCPAId(String CPAId) 
    {
        lMessageHeader.setCPAId(new TRPCPAId(CPAId));
        cpaidflag = true;
    }
    
    /** Adds or removes the DuplicateElimination element to/from the MessageHeader
     * @param v true adds DuplicateElimination. false removes DuplicateElimination.
     */
    public void setDuplicateElimination(boolean v) 
    {
        if (v)
            lMessageHeader.setDuplicateElimination(new TRPDuplicateElimination());
    }
    
    /** Sets the Role element of the From element of the MessageHeader
     * @param role The value of the content of the Role element. MAY be null.
     */
    public void setFromRole(String role) 
    {
        lMessageHeader.getFrom().setRole(new TRPRole(role));
    }
    
    /** 
     * Sets the MessageData element of the MessageHeader
     * @param messageId MAY be <CODE>null</CODE>. If null then MSH must fill in the value
     * @param timestamp MAY be <CODE>null</CODE>. If null then MSH must fill in the value
     * @param timeToLive MAY be <CODE>null</CODE>.
     */
    public void setMessageData(String messageId,String timestamp, DataDateTime timeToLive) throws BMLException{
        TRPMessageData lMessageData = new TRPMessageData();

	lMessageData.setMessageId(new TRPMessageId (messageId));
	lMessageData.setTimestamp(new TRPTimestamp (new DataDateTime(timestamp)));

        if (timeToLive != null)
	    {
            lMessageData.setTimeToLive(new TRPTimeToLive (new DataDateTime(timeToLive)));
	    }
        lMessageHeader.setMessageData(lMessageData);
        messagedataflag = true;
    }
    
    /** Sets the Role element of the To element of the MessageHeader
     * @param role The value of the content of the Role element. MAY be null.
     */
    public void setToRole(String role) {
        lMessageHeader.getTo().setRole(new TRPRole(role));
    }
   
}


/*.IEnd,MSHMessageHeaderTemplateImpl,====================================*/
