/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,MSHPingTemplate,======================================*/
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
/*.IA,	PUBLIC Include File MSHPingTemplate.java			*/

package org.openebxml.ebxml.msh.v20.integration.template;

/************************************************
	Includes
\************************************************/

import org.openebxml.comp.bml.data.DataDateTime;

/**
 * Interaface MSHPingTemplate
 *
 * General interface for creating an MSH Ping message.
 *
 * @author nikos dimitrakas DSV SU/KTH
 * @version $Id: MSHPingTemplate.java,v 1.1 2003/08/20 10:16:34 awtopensource Exp $
 */

public interface MSHPingTemplate extends MSHMessageHeaderTemplate 
{
    /****/
    public static final String SPECIFICATION        = "openebxml:ebxml:msh:template:ping";
    /****/
    public static final String SPECIFICATION_VERSION	= "1.0";

    /**
     * Creates a From element for the Ping message.
     * If this method has not been called the MSH MUST provide necessary values.
     * @param partyId The value of the content of the PartId element. MUST NOT be null.
     * @param partyIdType The value of the type attribute of the PartyId element. MAY be null.
     * @param role The value of the content of the Role element. MAY be null.
     */
    public void setFrom(String partyId, String partyIdType, String role);
    
    /**
     * Creates a To element for the Ping message.
     * This method MUST be called atleast once.
     * @param partyId The value of the content of the PartId element. MUST NOT be null.
     * @param partyIdType The value of the type attribute of the PartyId element. MAY be null.
     * @param role The value of the content of the Role element. MAY be null.
     */
    public void setTo(String partyId, String partyIdType, String role);
    
}

/*.IEnd,MSHPingTemplate,====================================*/
