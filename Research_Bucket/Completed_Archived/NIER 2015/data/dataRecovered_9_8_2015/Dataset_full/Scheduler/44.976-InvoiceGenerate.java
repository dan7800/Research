/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.process;

import java.sql.*;
import java.math.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Generate Invoices
 *	
 *  @author Jorg Janke
 *  @version $Id: InvoiceGenerate.java,v 1.4 2004/05/21 02:27:38 jjanke Exp $
 */
public class InvoiceGenerate extends SvrProcess
{
	/**	Manual Selection		*/
	private boolean 	p_Selection = false;
	/**	Date Invoiced			*/
	private Timestamp	p_DateInvoiced = null;
	/**	Org						*/
	private int			p_AD_Org_ID = 0;
	/** BPartner				*/
	private int			p_C_BPartner_ID = 0;
	/** Order					*/
	private int			p_C_Order_ID = 0;
	/** Consolidate				*/
	private boolean		p_ConsolidateDocument = true;
	
	/**	The current Invoice	*/
	private MInvoice 	m_invoice = null;
	/**	The current Shipment	*/
	private MInOut	 	m_ship = null;
	/** Numner of Invoices		*/
	private int			m_created = 0;
	/**	Line Number				*/
	private int			m_line = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("Selection"))
				p_Selection = "Y".equals(para[i].getParameter());
			else if (name.equals("DateInvoiced"))
				p_DateInvoiced = (Timestamp)para[i].getParameter();
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = para[i].getParameterAsInt();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = para[i].getParameterAsInt();
			else if (name.equals("C_Order_ID"))
				p_C_Order_ID = para[i].getParameterAsInt();
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = "Y".equals(para[i].getParameter());
			else
				log.error("prepare - Unknown Parameter: " + name);
		}

		//	Login Date
		if (p_DateInvoiced == null)
			p_DateInvoiced = Env.getContextAsDate(getCtx(), "#Date ");
		if (p_DateInvoiced == null)
			p_DateInvoiced = new Timestamp(System.currentTimeMillis());

	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		log.info("doIt - Selection=" + p_Selection + ", DateInvoiced=" + p_DateInvoiced
			+ ", AD_Org_ID=" + p_AD_Org_ID + ", C_BPartner_ID=" + p_C_BPartner_ID
			+ ", C_Order_ID=" + p_C_Order_ID + ", Consolidate=" + p_ConsolidateDocument);
		//
		String sql = null;
		if (p_Selection)	//	VInvoiceGen
		{
			sql = "SELECT * FROM C_Order "
				+ "WHERE IsSelected='Y' AND DocStatus='CO' AND IsSOTrx='Y' "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		else
		{
			sql = "SELECT * FROM C_Order o "
				+ "WHERE DocStatus='CO' AND IsSOTrx='Y'";
			if (p_AD_Org_ID != 0)
				sql += " AND AD_Org_ID=?";
			if (p_C_BPartner_ID != 0)
				sql += " AND C_BPartner_ID=?";
			if (p_C_Order_ID != 0)
				sql += " AND C_Order_ID=?";
			//
			sql += " AND EXISTS (SELECT * FROM C_OrderLine ol "
					+ "WHERE o.C_Order_ID=ol.C_Order_ID AND ol.QtyOrdered<>ol.QtyInvoiced) "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			int index = 1;
			if (!p_Selection && p_AD_Org_ID != 0)
				pstmt.setInt(index++, p_AD_Org_ID);
			if (!p_Selection && p_C_BPartner_ID != 0)
				pstmt.setInt(index++, p_C_BPartner_ID);
			if (!p_Selection && p_C_Order_ID != 0)
				pstmt.setInt(index++, p_C_Order_ID);
		}
		catch (Exception e)
		{
			log.error ("doIt - " + sql, e);
		}
		return generate(pstmt);
	}	//	doIt
	
	
	/**
	 * 	Generate Shipments
	 *	@return info
	 */
	private String generate (PreparedStatement pstmt)
	{
		try
		{
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MOrder order = new MOrder (getCtx(), rs);
				//	New Invoice Location
				if (!p_ConsolidateDocument 
					|| (m_invoice != null 
					&& m_invoice.getC_BPartner_Location_ID() != order.getBill_Location_ID()) )
					completeInvoice();
				boolean completeOrder = MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule());
				
				//	Schedule After Delivery
				boolean doInvoice = false;
				if (MOrder.INVOICERULE_CustomerScheduleAfterDelivery.equals(order.getInvoiceRule()))
				{
					MBPartner bp = new MBPartner (getCtx(), order.getBill_BPartner_ID());
					if (bp.getC_InvoiceSchedule_ID() == 0)
					{
						log.warn("generate - BPartner has no Schedule - set to After Delivery");
						order.setInvoiceRule(MOrder.INVOICERULE_AfterDelivery);
						order.save();
					}
					else
					{
						MInvoiceSchedule is = MInvoiceSchedule.get(getCtx(), bp.getC_InvoiceSchedule_ID());
						if (is.canInvoice(order.getDateOrdered(), order.getGrandTotal()))
							doInvoice = true;
						else
							continue;
					}
				}	//	Schedule
				
				//	After Delivery
				if (doInvoice || MOrder.INVOICERULE_AfterDelivery.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}
				//	After Order Delivered, Immediate
				else
				{
					MOrderLine[] lines = order.getLines(false);
					for (int i = 0; i < lines.length; i++)
					{
						MOrderLine line = lines[i];
						BigDecimal toInvoice = line.getQtyOrdered().subtract(line.getQtyInvoiced());
						if (toInvoice.compareTo(Env.ZERO) == 0)
							continue;
						BigDecimal notInvoicedShipment = line.getQtyDelivered().subtract(line.getQtyInvoiced());
						//
						boolean fullyDelivered = line.getQtyOrdered().compareTo(line.getQtyDelivered()) == 0;
					
						//	Complete Order
						if (completeOrder && !fullyDelivered)
						{
							log.debug("generate - Failed CompleteOrder - " + line);
							completeOrder = false;
							break;
						}
						//	Immediate
						else if (MOrder.INVOICERULE_Immediate.equals(order.getInvoiceRule()))
						{
							log.debug("generate - Immediate - ToInvoice=" + toInvoice + " - " + line);
							createLine (order, line, toInvoice);
						}
						else
							log.debug("generate - Failed: " + order.getInvoiceRule() 
								+ " - ToInvoice=" + toInvoice + " - " + line);
					}	//	for all order lines
				}
				
				//	Complete Order successful
				if (completeOrder && MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}	//	complete Order
			}	//	for all orders
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error ("generate", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		completeInvoice();
		return "@Created@ = " + m_created;
	}	//	generate
	
	
	
	/**************************************************************************
	 * 	Create Invoice Line from Order Line
	 *	@param order order
	 *	@param orderLine line
	 *	@param qty qty
	 */
	private void createLine (MOrder order, MOrderLine orderLine, BigDecimal qty)
	{
		if (qty.compareTo(Env.ZERO) == 0)
			return;
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (o)");
			m_line = 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setOrderLine(orderLine);
		line.setQtyInvoiced(qty);
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (o)");
		log.debug("createLine - " + line);
		m_line += 10;
	}	//	createLine

	/**
	 * 	Create Invoice Line from Shipment
	 *	@param order order
	 *	@param ship shipment header
	 *	@param sLine shipment line
	 */
	private void createLine (MOrder order, MInOut ship, MInOutLine sLine)
	{
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (s)");
			m_line = 10;
		}
		//	Create Comment Line
		if (m_ship == null || m_ship.getM_InOut_ID() != ship.getM_InOut_ID())
		{
			MDocType dt = MDocType.get(getCtx(), ship.getC_DocType_ID());
			String reference = dt.getPrintName() + ": " + ship.getDocumentNo();
			m_ship = ship;
			//
			MInvoiceLine line = new MInvoiceLine (m_invoice);
			line.setIsDescription(true);
			line.setDescription(reference);
			line.setLine(m_line);
			if (!line.save())
				throw new IllegalStateException("Could not create Invoice Line (sh)");
			m_line += 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setShipLine(sLine);
		line.setQtyInvoiced(sLine.getMovementQty());
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (s)");
		m_line += 10;
		//	Link
		sLine.setIsInvoiced(true);
		if (!sLine.save())
			throw new IllegalStateException("Could not update Shipment Line");
		
		log.debug("createLine - " + line);
	}	//	createLine

	
	/**
	 * 	Complete Invoice
	 */
	private void completeInvoice()
	{
		if (m_invoice != null)
		{
			//	TODO Should start workflow not execute directly
			if (MInvoice.DOCSTATUS_Completed.equals(m_invoice.completeIt()))
			{
				m_invoice.setDocStatus(MInvoice.DOCSTATUS_Completed);
				addLog(m_invoice.getC_Invoice_ID(), m_invoice.getDateInvoiced(), null, m_invoice.getDocumentNo());
				m_created++;
			}
			else
				log.error("completeInvoice - failed: " + m_invoice);
			m_invoice.save();
		}
		m_invoice = null;
		m_ship = null;
	}	//	completeInvoice
	
}	//	InvoiceGenerate
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.process;

import java.math.*;
import java.sql.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Generate Invoices
 *	
 *  @author Jorg Janke
 *  @version $Id: InvoiceGenerate.java,v 1.9 2004/08/27 21:24:13 jjanke Exp $
 */
public class InvoiceGenerate extends SvrProcess
{
	/**	Manual Selection		*/
	private boolean 	p_Selection = false;
	/**	Date Invoiced			*/
	private Timestamp	p_DateInvoiced = null;
	/**	Org						*/
	private int			p_AD_Org_ID = 0;
	/** BPartner				*/
	private int			p_C_BPartner_ID = 0;
	/** Order					*/
	private int			p_C_Order_ID = 0;
	/** Consolidate				*/
	private boolean		p_ConsolidateDocument = true;
	
	/**	The current Invoice	*/
	private MInvoice 	m_invoice = null;
	/**	The current Shipment	*/
	private MInOut	 	m_ship = null;
	/** Numner of Invoices		*/
	private int			m_created = 0;
	/**	Line Number				*/
	private int			m_line = 0;
	/**	Business Partner		*/
	private MBPartner	m_bp = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("Selection"))
				p_Selection = "Y".equals(para[i].getParameter());
			else if (name.equals("DateInvoiced"))
				p_DateInvoiced = (Timestamp)para[i].getParameter();
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = para[i].getParameterAsInt();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = para[i].getParameterAsInt();
			else if (name.equals("C_Order_ID"))
				p_C_Order_ID = para[i].getParameterAsInt();
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = "Y".equals(para[i].getParameter());
			else
				log.error("prepare - Unknown Parameter: " + name);
		}

		//	Login Date
		if (p_DateInvoiced == null)
			p_DateInvoiced = Env.getContextAsDate(getCtx(), "#Date");
		if (p_DateInvoiced == null)
			p_DateInvoiced = new Timestamp(System.currentTimeMillis());

	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		log.info("doIt - Selection=" + p_Selection + ", DateInvoiced=" + p_DateInvoiced
			+ ", AD_Org_ID=" + p_AD_Org_ID + ", C_BPartner_ID=" + p_C_BPartner_ID
			+ ", C_Order_ID=" + p_C_Order_ID + ", Consolidate=" + p_ConsolidateDocument);
		//
		String sql = null;
		if (p_Selection)	//	VInvoiceGen
		{
			sql = "SELECT * FROM C_Order "
				+ "WHERE IsSelected='Y' AND DocStatus='CO' AND IsSOTrx='Y' "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		else
		{
			sql = "SELECT * FROM C_Order o "
				+ "WHERE DocStatus='CO' AND IsSOTrx='Y'";
			if (p_AD_Org_ID != 0)
				sql += " AND AD_Org_ID=?";
			if (p_C_BPartner_ID != 0)
				sql += " AND C_BPartner_ID=?";
			if (p_C_Order_ID != 0)
				sql += " AND C_Order_ID=?";
			//
			sql += " AND EXISTS (SELECT * FROM C_OrderLine ol "
					+ "WHERE o.C_Order_ID=ol.C_Order_ID AND ol.QtyOrdered<>ol.QtyInvoiced) "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			int index = 1;
			if (!p_Selection && p_AD_Org_ID != 0)
				pstmt.setInt(index++, p_AD_Org_ID);
			if (!p_Selection && p_C_BPartner_ID != 0)
				pstmt.setInt(index++, p_C_BPartner_ID);
			if (!p_Selection && p_C_Order_ID != 0)
				pstmt.setInt(index++, p_C_Order_ID);
		}
		catch (Exception e)
		{
			log.error ("doIt - " + sql, e);
		}
		return generate(pstmt);
	}	//	doIt
	
	
	/**
	 * 	Generate Shipments
	 * 	@param pstmt order query 
	 *	@return info
	 */
	private String generate (PreparedStatement pstmt)
	{
		try
		{
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MOrder order = new MOrder (getCtx(), rs);
				
				//	New Invoice Location
				if (!p_ConsolidateDocument 
					|| (m_invoice != null 
					&& m_invoice.getC_BPartner_Location_ID() != order.getBill_Location_ID()) )
					completeInvoice();
				boolean completeOrder = MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule());
				
				//	Schedule After Delivery
				boolean doInvoice = false;
				if (MOrder.INVOICERULE_CustomerScheduleAfterDelivery.equals(order.getInvoiceRule()))
				{
					m_bp = new MBPartner (getCtx(), order.getBill_BPartner_ID());
					if (m_bp.getC_InvoiceSchedule_ID() == 0)
					{
						log.warn("generate - BPartner has no Schedule - set to After Delivery");
						order.setInvoiceRule(MOrder.INVOICERULE_AfterDelivery);
						order.save();
					}
					else
					{
						MInvoiceSchedule is = MInvoiceSchedule.get(getCtx(), m_bp.getC_InvoiceSchedule_ID());
						if (is.canInvoice(order.getDateOrdered(), order.getGrandTotal()))
							doInvoice = true;
						else
							continue;
					}
				}	//	Schedule
				
				//	After Delivery
				if (doInvoice || MOrder.INVOICERULE_AfterDelivery.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}
				//	After Order Delivered, Immediate
				else
				{
					MOrderLine[] oLines = order.getLines(false);
					for (int i = 0; i < oLines.length; i++)
					{
						MOrderLine oLine = oLines[i];
						BigDecimal toInvoice = oLine.getQtyOrdered().subtract(oLine.getQtyInvoiced());
						if (toInvoice.compareTo(Env.ZERO) == 0 && oLine.getM_Product_ID() != 0)
							continue;
						BigDecimal notInvoicedShipment = oLine.getQtyDelivered().subtract(oLine.getQtyInvoiced());
						//
						boolean fullyDelivered = oLine.getQtyOrdered().compareTo(oLine.getQtyDelivered()) == 0;
					
						//	Complete Order
						if (completeOrder && !fullyDelivered)
						{
							log.debug("generate - Failed CompleteOrder - " + oLine);
							completeOrder = false;
							break;
						}
						//	Immediate
						else if (MOrder.INVOICERULE_Immediate.equals(order.getInvoiceRule()))
						{
							log.debug("generate - Immediate - ToInvoice=" + toInvoice + " - " + oLine);
							createLine (order, oLine, toInvoice);
						}
						else
							log.debug("generate - Failed: " + order.getInvoiceRule() 
								+ " - ToInvoice=" + toInvoice + " - " + oLine);
					}	//	for all order lines
				}
				
				//	Complete Order successful
				if (completeOrder && MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}	//	complete Order
			}	//	for all orders
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error ("generate", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		completeInvoice();
		return "@Created@ = " + m_created;
	}	//	generate
	
	
	
	/**************************************************************************
	 * 	Create Invoice Line from Order Line
	 *	@param order order
	 *	@param orderLine line
	 *	@param qty qty
	 */
	private void createLine (MOrder order, MOrderLine orderLine, BigDecimal qty)
	{
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (o)");
			m_line = 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setOrderLine(orderLine);
		line.setQty(qty);
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (o)");
		log.debug("createLine - " + line);
		m_line += 10;
	}	//	createLine

	/**
	 * 	Create Invoice Line from Shipment
	 *	@param order order
	 *	@param ship shipment header
	 *	@param sLine shipment line
	 */
	private void createLine (MOrder order, MInOut ship, MInOutLine sLine)
	{
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (s)");
			m_line = 10;
		}
		//	Create Comment Line
		if (m_ship == null || m_ship.getM_InOut_ID() != ship.getM_InOut_ID())
		{
			MDocType dt = MDocType.get(getCtx(), ship.getC_DocType_ID());
			if (m_bp == null || m_bp.getC_BPartner_ID() != ship.getC_BPartner_ID())
				m_bp = new MBPartner (getCtx(), ship.getC_BPartner_ID());
			String reference = dt.getPrintName(m_bp.getAD_Language())
				+ ": " + ship.getDocumentNo();
			m_ship = ship;
			//
			MInvoiceLine line = new MInvoiceLine (m_invoice);
			line.setIsDescription(true);
			line.setDescription(reference);
			line.setLine(m_line);
			if (!line.save())
				throw new IllegalStateException("Could not create Invoice Line (sh)");
			m_line += 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setShipLine(sLine);
		line.setQtyEntered(sLine.getQtyEntered());
		line.setQtyInvoiced(sLine.getMovementQty());
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (s)");
		m_line += 10;
		//	Link
		sLine.setIsInvoiced(true);
		if (!sLine.save())
			throw new IllegalStateException("Could not update Shipment Line");
		
		log.debug("createLine - " + line);
	}	//	createLine

	
	/**
	 * 	Complete Invoice
	 */
	private void completeInvoice()
	{
		if (m_invoice != null)
		{
			if (!m_invoice.processIt(DocAction.ACTION_Complete))
				log.warn("completeInvoice - failed: " + m_invoice);
			m_invoice.save();
			//
			addLog(m_invoice.getC_Invoice_ID(), m_invoice.getDateInvoiced(), null, m_invoice.getDocumentNo());
			m_created++;
		}
		m_invoice = null;
		m_ship = null;
	}	//	completeInvoice
	
}	//	InvoiceGenerate
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.process;

import java.sql.*;
import java.math.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Generate Invoices
 *	
 *  @author Jorg Janke
 *  @version $Id: InvoiceGenerate.java,v 1.4 2004/05/21 02:27:38 jjanke Exp $
 */
public class InvoiceGenerate extends SvrProcess
{
	/**	Manual Selection		*/
	private boolean 	p_Selection = false;
	/**	Date Invoiced			*/
	private Timestamp	p_DateInvoiced = null;
	/**	Org						*/
	private int			p_AD_Org_ID = 0;
	/** BPartner				*/
	private int			p_C_BPartner_ID = 0;
	/** Order					*/
	private int			p_C_Order_ID = 0;
	/** Consolidate				*/
	private boolean		p_ConsolidateDocument = true;
	
	/**	The current Invoice	*/
	private MInvoice 	m_invoice = null;
	/**	The current Shipment	*/
	private MInOut	 	m_ship = null;
	/** Numner of Invoices		*/
	private int			m_created = 0;
	/**	Line Number				*/
	private int			m_line = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("Selection"))
				p_Selection = "Y".equals(para[i].getParameter());
			else if (name.equals("DateInvoiced"))
				p_DateInvoiced = (Timestamp)para[i].getParameter();
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = para[i].getParameterAsInt();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = para[i].getParameterAsInt();
			else if (name.equals("C_Order_ID"))
				p_C_Order_ID = para[i].getParameterAsInt();
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = "Y".equals(para[i].getParameter());
			else
				log.error("prepare - Unknown Parameter: " + name);
		}

		//	Login Date
		if (p_DateInvoiced == null)
			p_DateInvoiced = Env.getContextAsDate(getCtx(), "#Date ");
		if (p_DateInvoiced == null)
			p_DateInvoiced = new Timestamp(System.currentTimeMillis());

	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		log.info("doIt - Selection=" + p_Selection + ", DateInvoiced=" + p_DateInvoiced
			+ ", AD_Org_ID=" + p_AD_Org_ID + ", C_BPartner_ID=" + p_C_BPartner_ID
			+ ", C_Order_ID=" + p_C_Order_ID + ", Consolidate=" + p_ConsolidateDocument);
		//
		String sql = null;
		if (p_Selection)	//	VInvoiceGen
		{
			sql = "SELECT * FROM C_Order "
				+ "WHERE IsSelected='Y' AND DocStatus='CO' AND IsSOTrx='Y' "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		else
		{
			sql = "SELECT * FROM C_Order o "
				+ "WHERE DocStatus='CO' AND IsSOTrx='Y'";
			if (p_AD_Org_ID != 0)
				sql += " AND AD_Org_ID=?";
			if (p_C_BPartner_ID != 0)
				sql += " AND C_BPartner_ID=?";
			if (p_C_Order_ID != 0)
				sql += " AND C_Order_ID=?";
			//
			sql += " AND EXISTS (SELECT * FROM C_OrderLine ol "
					+ "WHERE o.C_Order_ID=ol.C_Order_ID AND ol.QtyOrdered<>ol.QtyInvoiced) "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			int index = 1;
			if (!p_Selection && p_AD_Org_ID != 0)
				pstmt.setInt(index++, p_AD_Org_ID);
			if (!p_Selection && p_C_BPartner_ID != 0)
				pstmt.setInt(index++, p_C_BPartner_ID);
			if (!p_Selection && p_C_Order_ID != 0)
				pstmt.setInt(index++, p_C_Order_ID);
		}
		catch (Exception e)
		{
			log.error ("doIt - " + sql, e);
		}
		return generate(pstmt);
	}	//	doIt
	
	
	/**
	 * 	Generate Shipments
	 *	@return info
	 */
	private String generate (PreparedStatement pstmt)
	{
		try
		{
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MOrder order = new MOrder (getCtx(), rs);
				//	New Invoice Location
				if (!p_ConsolidateDocument 
					|| (m_invoice != null 
					&& m_invoice.getC_BPartner_Location_ID() != order.getBill_Location_ID()) )
					completeInvoice();
				boolean completeOrder = MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule());
				
				//	Schedule After Delivery
				boolean doInvoice = false;
				if (MOrder.INVOICERULE_CustomerScheduleAfterDelivery.equals(order.getInvoiceRule()))
				{
					MBPartner bp = new MBPartner (getCtx(), order.getBill_BPartner_ID());
					if (bp.getC_InvoiceSchedule_ID() == 0)
					{
						log.warn("generate - BPartner has no Schedule - set to After Delivery");
						order.setInvoiceRule(MOrder.INVOICERULE_AfterDelivery);
						order.save();
					}
					else
					{
						MInvoiceSchedule is = MInvoiceSchedule.get(getCtx(), bp.getC_InvoiceSchedule_ID());
						if (is.canInvoice(order.getDateOrdered(), order.getGrandTotal()))
							doInvoice = true;
						else
							continue;
					}
				}	//	Schedule
				
				//	After Delivery
				if (doInvoice || MOrder.INVOICERULE_AfterDelivery.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}
				//	After Order Delivered, Immediate
				else
				{
					MOrderLine[] lines = order.getLines(false);
					for (int i = 0; i < lines.length; i++)
					{
						MOrderLine line = lines[i];
						BigDecimal toInvoice = line.getQtyOrdered().subtract(line.getQtyInvoiced());
						if (toInvoice.compareTo(Env.ZERO) == 0)
							continue;
						BigDecimal notInvoicedShipment = line.getQtyDelivered().subtract(line.getQtyInvoiced());
						//
						boolean fullyDelivered = line.getQtyOrdered().compareTo(line.getQtyDelivered()) == 0;
					
						//	Complete Order
						if (completeOrder && !fullyDelivered)
						{
							log.debug("generate - Failed CompleteOrder - " + line);
							completeOrder = false;
							break;
						}
						//	Immediate
						else if (MOrder.INVOICERULE_Immediate.equals(order.getInvoiceRule()))
						{
							log.debug("generate - Immediate - ToInvoice=" + toInvoice + " - " + line);
							createLine (order, line, toInvoice);
						}
						else
							log.debug("generate - Failed: " + order.getInvoiceRule() 
								+ " - ToInvoice=" + toInvoice + " - " + line);
					}	//	for all order lines
				}
				
				//	Complete Order successful
				if (completeOrder && MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}	//	complete Order
			}	//	for all orders
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error ("generate", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		completeInvoice();
		return "@Created@ = " + m_created;
	}	//	generate
	
	
	
	/**************************************************************************
	 * 	Create Invoice Line from Order Line
	 *	@param order order
	 *	@param orderLine line
	 *	@param qty qty
	 */
	private void createLine (MOrder order, MOrderLine orderLine, BigDecimal qty)
	{
		if (qty.compareTo(Env.ZERO) == 0)
			return;
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (o)");
			m_line = 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setOrderLine(orderLine);
		line.setQtyInvoiced(qty);
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (o)");
		log.debug("createLine - " + line);
		m_line += 10;
	}	//	createLine

	/**
	 * 	Create Invoice Line from Shipment
	 *	@param order order
	 *	@param ship shipment header
	 *	@param sLine shipment line
	 */
	private void createLine (MOrder order, MInOut ship, MInOutLine sLine)
	{
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (s)");
			m_line = 10;
		}
		//	Create Comment Line
		if (m_ship == null || m_ship.getM_InOut_ID() != ship.getM_InOut_ID())
		{
			MDocType dt = MDocType.get(getCtx(), ship.getC_DocType_ID());
			String reference = dt.getPrintName() + ": " + ship.getDocumentNo();
			m_ship = ship;
			//
			MInvoiceLine line = new MInvoiceLine (m_invoice);
			line.setIsDescription(true);
			line.setDescription(reference);
			line.setLine(m_line);
			if (!line.save())
				throw new IllegalStateException("Could not create Invoice Line (sh)");
			m_line += 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setShipLine(sLine);
		line.setQtyInvoiced(sLine.getMovementQty());
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (s)");
		m_line += 10;
		//	Link
		sLine.setIsInvoiced(true);
		if (!sLine.save())
			throw new IllegalStateException("Could not update Shipment Line");
		
		log.debug("createLine - " + line);
	}	//	createLine

	
	/**
	 * 	Complete Invoice
	 */
	private void completeInvoice()
	{
		if (m_invoice != null)
		{
			//	TODO Should start workflow not execute directly
			if (MInvoice.DOCSTATUS_Completed.equals(m_invoice.completeIt()))
			{
				m_invoice.setDocStatus(MInvoice.DOCSTATUS_Completed);
				addLog(m_invoice.getC_Invoice_ID(), m_invoice.getDateInvoiced(), null, m_invoice.getDocumentNo());
				m_created++;
			}
			else
				log.error("completeInvoice - failed: " + m_invoice);
			m_invoice.save();
		}
		m_invoice = null;
		m_ship = null;
	}	//	completeInvoice
	
}	//	InvoiceGenerate
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2004 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.process;

import java.math.*;
import java.sql.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Generate Invoices
 *	
 *  @author Jorg Janke
 *  @version $Id: InvoiceGenerate.java,v 1.9 2004/08/27 21:24:13 jjanke Exp $
 */
public class InvoiceGenerate extends SvrProcess
{
	/**	Manual Selection		*/
	private boolean 	p_Selection = false;
	/**	Date Invoiced			*/
	private Timestamp	p_DateInvoiced = null;
	/**	Org						*/
	private int			p_AD_Org_ID = 0;
	/** BPartner				*/
	private int			p_C_BPartner_ID = 0;
	/** Order					*/
	private int			p_C_Order_ID = 0;
	/** Consolidate				*/
	private boolean		p_ConsolidateDocument = true;
	
	/**	The current Invoice	*/
	private MInvoice 	m_invoice = null;
	/**	The current Shipment	*/
	private MInOut	 	m_ship = null;
	/** Numner of Invoices		*/
	private int			m_created = 0;
	/**	Line Number				*/
	private int			m_line = 0;
	/**	Business Partner		*/
	private MBPartner	m_bp = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("Selection"))
				p_Selection = "Y".equals(para[i].getParameter());
			else if (name.equals("DateInvoiced"))
				p_DateInvoiced = (Timestamp)para[i].getParameter();
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = para[i].getParameterAsInt();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = para[i].getParameterAsInt();
			else if (name.equals("C_Order_ID"))
				p_C_Order_ID = para[i].getParameterAsInt();
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = "Y".equals(para[i].getParameter());
			else
				log.error("prepare - Unknown Parameter: " + name);
		}

		//	Login Date
		if (p_DateInvoiced == null)
			p_DateInvoiced = Env.getContextAsDate(getCtx(), "#Date");
		if (p_DateInvoiced == null)
			p_DateInvoiced = new Timestamp(System.currentTimeMillis());

	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		log.info("doIt - Selection=" + p_Selection + ", DateInvoiced=" + p_DateInvoiced
			+ ", AD_Org_ID=" + p_AD_Org_ID + ", C_BPartner_ID=" + p_C_BPartner_ID
			+ ", C_Order_ID=" + p_C_Order_ID + ", Consolidate=" + p_ConsolidateDocument);
		//
		String sql = null;
		if (p_Selection)	//	VInvoiceGen
		{
			sql = "SELECT * FROM C_Order "
				+ "WHERE IsSelected='Y' AND DocStatus='CO' AND IsSOTrx='Y' "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		else
		{
			sql = "SELECT * FROM C_Order o "
				+ "WHERE DocStatus='CO' AND IsSOTrx='Y'";
			if (p_AD_Org_ID != 0)
				sql += " AND AD_Org_ID=?";
			if (p_C_BPartner_ID != 0)
				sql += " AND C_BPartner_ID=?";
			if (p_C_Order_ID != 0)
				sql += " AND C_Order_ID=?";
			//
			sql += " AND EXISTS (SELECT * FROM C_OrderLine ol "
					+ "WHERE o.C_Order_ID=ol.C_Order_ID AND ol.QtyOrdered<>ol.QtyInvoiced) "
				+ "ORDER BY M_Warehouse_ID, PriorityRule, C_BPartner_ID, C_Order_ID";
		}
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			int index = 1;
			if (!p_Selection && p_AD_Org_ID != 0)
				pstmt.setInt(index++, p_AD_Org_ID);
			if (!p_Selection && p_C_BPartner_ID != 0)
				pstmt.setInt(index++, p_C_BPartner_ID);
			if (!p_Selection && p_C_Order_ID != 0)
				pstmt.setInt(index++, p_C_Order_ID);
		}
		catch (Exception e)
		{
			log.error ("doIt - " + sql, e);
		}
		return generate(pstmt);
	}	//	doIt
	
	
	/**
	 * 	Generate Shipments
	 * 	@param pstmt order query 
	 *	@return info
	 */
	private String generate (PreparedStatement pstmt)
	{
		try
		{
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MOrder order = new MOrder (getCtx(), rs);
				
				//	New Invoice Location
				if (!p_ConsolidateDocument 
					|| (m_invoice != null 
					&& m_invoice.getC_BPartner_Location_ID() != order.getBill_Location_ID()) )
					completeInvoice();
				boolean completeOrder = MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule());
				
				//	Schedule After Delivery
				boolean doInvoice = false;
				if (MOrder.INVOICERULE_CustomerScheduleAfterDelivery.equals(order.getInvoiceRule()))
				{
					m_bp = new MBPartner (getCtx(), order.getBill_BPartner_ID());
					if (m_bp.getC_InvoiceSchedule_ID() == 0)
					{
						log.warn("generate - BPartner has no Schedule - set to After Delivery");
						order.setInvoiceRule(MOrder.INVOICERULE_AfterDelivery);
						order.save();
					}
					else
					{
						MInvoiceSchedule is = MInvoiceSchedule.get(getCtx(), m_bp.getC_InvoiceSchedule_ID());
						if (is.canInvoice(order.getDateOrdered(), order.getGrandTotal()))
							doInvoice = true;
						else
							continue;
					}
				}	//	Schedule
				
				//	After Delivery
				if (doInvoice || MOrder.INVOICERULE_AfterDelivery.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}
				//	After Order Delivered, Immediate
				else
				{
					MOrderLine[] oLines = order.getLines(false);
					for (int i = 0; i < oLines.length; i++)
					{
						MOrderLine oLine = oLines[i];
						BigDecimal toInvoice = oLine.getQtyOrdered().subtract(oLine.getQtyInvoiced());
						if (toInvoice.compareTo(Env.ZERO) == 0 && oLine.getM_Product_ID() != 0)
							continue;
						BigDecimal notInvoicedShipment = oLine.getQtyDelivered().subtract(oLine.getQtyInvoiced());
						//
						boolean fullyDelivered = oLine.getQtyOrdered().compareTo(oLine.getQtyDelivered()) == 0;
					
						//	Complete Order
						if (completeOrder && !fullyDelivered)
						{
							log.debug("generate - Failed CompleteOrder - " + oLine);
							completeOrder = false;
							break;
						}
						//	Immediate
						else if (MOrder.INVOICERULE_Immediate.equals(order.getInvoiceRule()))
						{
							log.debug("generate - Immediate - ToInvoice=" + toInvoice + " - " + oLine);
							createLine (order, oLine, toInvoice);
						}
						else
							log.debug("generate - Failed: " + order.getInvoiceRule() 
								+ " - ToInvoice=" + toInvoice + " - " + oLine);
					}	//	for all order lines
				}
				
				//	Complete Order successful
				if (completeOrder && MOrder.INVOICERULE_AfterOrderDelivered.equals(order.getInvoiceRule()))
				{
					MInOut[] shipments = order.getShipments();
					for (int i = 0; i < shipments.length; i++)
					{
						MInOut ship = shipments[i];
						MInOutLine[] shipLines = ship.getLines(false);
						for (int j = 0; j < shipLines.length; j++)
						{
							MInOutLine shipLine = shipLines[j];
							if (!shipLine.isInvoiced())
								createLine (order, ship, shipLine);
						}
					}
				}	//	complete Order
			}	//	for all orders
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error ("generate", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		completeInvoice();
		return "@Created@ = " + m_created;
	}	//	generate
	
	
	
	/**************************************************************************
	 * 	Create Invoice Line from Order Line
	 *	@param order order
	 *	@param orderLine line
	 *	@param qty qty
	 */
	private void createLine (MOrder order, MOrderLine orderLine, BigDecimal qty)
	{
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (o)");
			m_line = 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setOrderLine(orderLine);
		line.setQty(qty);
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (o)");
		log.debug("createLine - " + line);
		m_line += 10;
	}	//	createLine

	/**
	 * 	Create Invoice Line from Shipment
	 *	@param order order
	 *	@param ship shipment header
	 *	@param sLine shipment line
	 */
	private void createLine (MOrder order, MInOut ship, MInOutLine sLine)
	{
		if (m_invoice == null)
		{
			m_invoice = new MInvoice (order, 0, p_DateInvoiced);
			if (!m_invoice.save())
				throw new IllegalStateException("Could not create Invoice (s)");
			m_line = 10;
		}
		//	Create Comment Line
		if (m_ship == null || m_ship.getM_InOut_ID() != ship.getM_InOut_ID())
		{
			MDocType dt = MDocType.get(getCtx(), ship.getC_DocType_ID());
			if (m_bp == null || m_bp.getC_BPartner_ID() != ship.getC_BPartner_ID())
				m_bp = new MBPartner (getCtx(), ship.getC_BPartner_ID());
			String reference = dt.getPrintName(m_bp.getAD_Language())
				+ ": " + ship.getDocumentNo();
			m_ship = ship;
			//
			MInvoiceLine line = new MInvoiceLine (m_invoice);
			line.setIsDescription(true);
			line.setDescription(reference);
			line.setLine(m_line);
			if (!line.save())
				throw new IllegalStateException("Could not create Invoice Line (sh)");
			m_line += 10;
		}
		//	
		MInvoiceLine line = new MInvoiceLine (m_invoice);
		line.setShipLine(sLine);
		line.setQtyEntered(sLine.getQtyEntered());
		line.setQtyInvoiced(sLine.getMovementQty());
		line.setLine(m_line);
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (s)");
		m_line += 10;
		//	Link
		sLine.setIsInvoiced(true);
		if (!sLine.save())
			throw new IllegalStateException("Could not update Shipment Line");
		
		log.debug("createLine - " + line);
	}	//	createLine

	
	/**
	 * 	Complete Invoice
	 */
	private void completeInvoice()
	{
		if (m_invoice != null)
		{
			if (!m_invoice.processIt(DocAction.ACTION_Complete))
				log.warn("completeInvoice - failed: " + m_invoice);
			m_invoice.save();
			//
			addLog(m_invoice.getC_Invoice_ID(), m_invoice.getDateInvoiced(), null, m_invoice.getDocumentNo());
			m_created++;
		}
		m_invoice = null;
		m_ship = null;
	}	//	completeInvoice
	
}	//	InvoiceGenerate
