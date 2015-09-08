// $Id: NeogiaModelTest.java,v 1.2 2006-05-26 11:31:00 pgoron Exp $
// Generator-Id: GeneratorNeogiaModelTestJava.java,v 1.1 2006/05/26 07:08:14 pgoron Exp 
/* Copyright (c) 2004, 2006 Neogia - www.neogia.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.ofbiz.entity.test.generated;

import junit.framework.TestCase;

import org.neogia.PersistantObject;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;

public class NeogiaModelTest extends TestCase {
    public static final String DELEGATOR_NAME = "default";
    public GenericDelegator delegator = null;

    protected void setUp() throws Exception {
        this.delegator = GenericDelegator.getGenericDelegator(DELEGATOR_NAME);
        super.setUp();
    }

    public void testMakeValueAccountClass() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "ACCOUNTCLASS"));
        assertEquals("org.ofbiz.accounting.staticdata.developed.AccountClass", value.getClass().getName());
    }

    public void testMakeValueAccountType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "ATYPE"));
        assertEquals("org.ofbiz.accounting.staticdata.developed.AccountType", value.getClass().getName());
    }

    public void testMakeValueAcctgEntryForm() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "AENTFRM"));
        assertEquals("org.ofbiz.accounting.transaction.developed.AcctgEntryForm", value.getClass().getName());
    }

    public void testMakeValueAcctgEntryImport() {
        GenericValue value = delegator.makeValue("AcctgEntryImport", null);
        assertEquals("org.ofbiz.accounting.accimport.developed.AcctgEntryImport", value.getClass().getName());
    }

    public void testMakeValueAcctgImportLog() {
        GenericValue value = delegator.makeValue("AcctgImportLog", null);
        assertEquals("org.ofbiz.accounting.accimport.developed.AcctgImportLog", value.getClass().getName());
    }

    public void testAcctgImportLogSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("AcctgImportLog", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.common.log.developed.ApplicationLog", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueAcctgProjectPeriod() {
        GenericValue value = delegator.makeValue("AcctgProjectPeriod", null);
        assertEquals("org.ofbiz.manufacturing.project.developed.AcctgProjectPeriod", value.getClass().getName());
    }

    public void testMakeValueAcctgTransaction() {
        GenericValue value = delegator.makeValue("AcctgTransaction", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.AcctgTransaction", value.getClass().getName());
    }

    public void testMakeValueAcctgTransactionItem() {
        GenericValue value = delegator.makeValue("AcctgTransactionItem", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.AcctgTransactionItem", value.getClass().getName());
    }

    public void testMakeValueAcctgTransactionPeriod() {
        GenericValue value = delegator.makeValue("AcctgTransactionPeriod", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.AcctgTransactionPeriod", value.getClass().getName());
    }

    public void testMakeValueAcctgTransactionStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "ATRSTATUS"));
        assertEquals("org.ofbiz.accounting.transaction.developed.AcctgTransactionStatus", value.getClass().getName());
    }

    public void testMakeValueAcctgTransactionType() {
        GenericValue value = delegator.makeValue("AcctgTransactionType", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.AcctgTransactionType", value.getClass().getName());
    }

    public void testMakeValueAddendum() {
        GenericValue value = delegator.makeValue("Addendum", null);
        assertEquals("org.ofbiz.party.agreement.developed.Addendum", value.getClass().getName());
    }

    public void testMakeValueAgreement() {
        GenericValue value = delegator.makeValue("Agreement", null);
        assertEquals("org.ofbiz.party.agreement.developed.Agreement", value.getClass().getName());
    }

    public void testMakeValueAgreementAttribute() {
        GenericValue value = delegator.makeValue("AgreementAttribute", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementAttribute", value.getClass().getName());
    }

    public void testMakeValueAgreementGeographicalApplic() {
        GenericValue value = delegator.makeValue("AgreementGeographicalApplic", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementGeographicalApplic", value.getClass().getName());
    }

    public void testMakeValueAgreementItem() {
        GenericValue value = delegator.makeValue("AgreementItem", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementItem", value.getClass().getName());
    }

    public void testMakeValueAgreementItemAttribute() {
        GenericValue value = delegator.makeValue("AgreementItemAttribute", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementItemAttribute", value.getClass().getName());
    }

    public void testMakeValueAgreementItemType() {
        GenericValue value = delegator.makeValue("AgreementItemType", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementItemType", value.getClass().getName());
    }

    public void testMakeValueAgreementPartyApplic() {
        GenericValue value = delegator.makeValue("AgreementPartyApplic", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementPartyApplic", value.getClass().getName());
    }

    public void testMakeValueAgreementProductAppl() {
        GenericValue value = delegator.makeValue("AgreementProductAppl", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementProductAppl", value.getClass().getName());
    }

    public void testMakeValueAgreementRole() {
        GenericValue value = delegator.makeValue("AgreementRole", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementRole", value.getClass().getName());
    }

    public void testMakeValueAgreementTerm() {
        GenericValue value = delegator.makeValue("AgreementTerm", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementTerm", value.getClass().getName());
    }

    public void testMakeValueAgreementType() {
        GenericValue value = delegator.makeValue("AgreementType", null);
        assertEquals("org.ofbiz.party.agreement.developed.AgreementType", value.getClass().getName());
    }

    public void testMakeValueApplicationLog() {
        GenericValue value = delegator.makeValue("ApplicationLog", null);
        assertEquals("org.ofbiz.common.log.developed.ApplicationLog", value.getClass().getName());
    }

    public void testMakeValueApplicationLogCode() {
        GenericValue value = delegator.makeValue("ApplicationLogCode", null);
        assertEquals("org.ofbiz.common.log.developed.ApplicationLogCode", value.getClass().getName());
    }

    public void testMakeValueApplicationLogLevel() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "APPLICATIONLOGLEVEL"));
        assertEquals("org.ofbiz.common.log.developed.ApplicationLogLevel", value.getClass().getName());
    }

    public void testMakeValueApplicationLogPerm() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "APPLICATIONLOGPERM"));
        assertEquals("org.ofbiz.common.log.developed.ApplicationLogPerm", value.getClass().getName());
    }

    public void testMakeValueApplicationLogStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "APPLICATIONLOGSTATUS"));
        assertEquals("org.ofbiz.common.log.developed.ApplicationLogStatus", value.getClass().getName());
    }

    public void testMakeValueArticle() {
        GenericValue value = delegator.makeValue("Article", null);
        assertEquals("org.ofbiz.website.cms.developed.Article", value.getClass().getName());
    }

    public void testMakeValueArticleHeading() {
        GenericValue value = delegator.makeValue("ArticleHeading", null);
        assertEquals("org.ofbiz.website.cms.developed.ArticleHeading", value.getClass().getName());
    }

    public void testMakeValueArticleLangAbrev() {
        GenericValue value = delegator.makeValue("ArticleLangAbrev", null);
        assertEquals("org.ofbiz.website.cms.developed.ArticleLangAbrev", value.getClass().getName());
    }

    public void testMakeValueArticleStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "AS"));
        assertEquals("org.ofbiz.website.cms.developed.ArticleStatus", value.getClass().getName());
    }

    public void testMakeValueBaseDateType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "BASEDATETYPE"));
        assertEquals("org.ofbiz.accounting.payment.developed.BaseDateType", value.getClass().getName());
    }

    public void testMakeValueBillingAccount() {
        GenericValue value = delegator.makeValue("BillingAccount", null);
        assertEquals("org.ofbiz.accounting.payment.developed.BillingAccount", value.getClass().getName());
    }

    public void testMakeValueBoxStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "BOXSTATUS"));
        assertEquals("org.ofbiz.shipment.shipment.developed.BoxStatus", value.getClass().getName());
    }

    public void testMakeValueBudgetAmount() {
        GenericValue value = delegator.makeValue("BudgetAmount", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.BudgetAmount", value.getClass().getName());
    }

    public void testMakeValueBudgetCode() {
        GenericValue value = delegator.makeValue("ProductPriceType", UtilMisc.toMap("discriminator", "BCOD"));
        assertEquals("org.ofbiz.manufacturing.cost.developed.BudgetCode", value.getClass().getName());
    }

    public void testMakeValueBusinessObjectName() {
        GenericValue value = delegator.makeValue("BusinessObjectName", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.BusinessObjectName", value.getClass().getName());
    }

    public void testMakeValueBusinessObjectType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "BOTYPE"));
        assertEquals("org.ofbiz.accounting.accintegration.developed.BusinessObjectType", value.getClass().getName());
    }

    public void testMakeValueBusinessObjectValue() {
        GenericValue value = delegator.makeValue("BusinessObjectValue", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.BusinessObjectValue", value.getClass().getName());
    }

    public void testMakeValueCalendarException() {
        GenericValue value = delegator.makeValue("CalendarException", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.CalendarException", value.getClass().getName());
    }

    public void testMakeValueCalendarWeek() {
        GenericValue value = delegator.makeValue("CalendarWeek", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.CalendarWeek", value.getClass().getName());
    }

    public void testMakeValueCarrierShipmentBoxType() {
        GenericValue value = delegator.makeValue("CarrierShipmentBoxType", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.CarrierShipmentBoxType", value.getClass().getName());
    }

    public void testMakeValueCarrierShipmentFulfil() {
        GenericValue value = delegator.makeValue("CarrierShipmentFulfil", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.CarrierShipmentFulfil", value.getClass().getName());
    }

    public void testMakeValueCarrierShipmentMethod() {
        GenericValue value = delegator.makeValue("CarrierShipmentMethod", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.CarrierShipmentMethod", value.getClass().getName());
    }

    public void testMakeValueCharacterSet() {
        GenericValue value = delegator.makeValue("CharacterSet", null);
        assertEquals("org.ofbiz.content.data.developed.CharacterSet", value.getClass().getName());
    }

    public void testMakeValueChargeBack() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "RCHB"));
        assertEquals("org.ofbiz.servicemgnt.receipt.developed.ChargeBack", value.getClass().getName());
    }

    public void testMakeValueChartName() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "CHARTNAME"));
        assertEquals("org.ofbiz.accounting.staticdata.developed.ChartName", value.getClass().getName());
    }

    public void testMakeValueCheckAccount() {
        GenericValue value = delegator.makeValue("CheckAccount", null);
        assertEquals("org.ofbiz.accounting.staticdata.developed.CheckAccount", value.getClass().getName());
    }

    public void testMakeValueCheckAccountItem() {
        GenericValue value = delegator.makeValue("CheckAccountItem", null);
        assertEquals("org.ofbiz.accounting.staticdata.developed.CheckAccountItem", value.getClass().getName());
    }

    public void testMakeValueCheckMeasure() {
        GenericValue value = delegator.makeValue("CheckMeasure", null);
        assertEquals("org.ofbiz.quality.checkMeasure.developed.CheckMeasure", value.getClass().getName());
    }

    public void testMakeValueCheckMeasureGroup() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "CHECKMEASUREGROUP"));
        assertEquals("org.ofbiz.quality.checkMeasure.developed.CheckMeasureGroup", value.getClass().getName());
    }

    public void testMakeValueCheckMeasureType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "CHECKMEASURETYPE"));
        assertEquals("org.ofbiz.quality.checkMeasure.developed.CheckMeasureType", value.getClass().getName());
    }

    public void testMakeValueComEventStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "COM_EVENT_STATUS"));
        assertEquals("org.ofbiz.party.communication.developed.ComEventStatus", value.getClass().getName());
    }

    public void testMakeValueCommercialName() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "COMMERCIALNAME"));
        assertEquals("org.ofbiz.party.party.developed.CommercialName", value.getClass().getName());
    }

    public void testMakeValueCommercialSign() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "COMMERCIALSIGN"));
        assertEquals("org.ofbiz.party.party.developed.CommercialSign", value.getClass().getName());
    }

    public void testMakeValueCommunicationEvent() {
        GenericValue value = delegator.makeValue("CommunicationEvent", null);
        assertEquals("org.ofbiz.party.communication.developed.CommunicationEvent", value.getClass().getName());
    }

    public void testMakeValueCommunicationEventPrpTyp() {
        GenericValue value = delegator.makeValue("CommunicationEventPrpTyp", null);
        assertEquals("org.ofbiz.party.communication.developed.CommunicationEventPrpTyp", value.getClass().getName());
    }

    public void testMakeValueCommunicationEventPurpose() {
        GenericValue value = delegator.makeValue("CommunicationEventPurpose", null);
        assertEquals("org.ofbiz.party.communication.developed.CommunicationEventPurpose", value.getClass().getName());
    }

    public void testMakeValueCommunicationEventRole() {
        GenericValue value = delegator.makeValue("CommunicationEventRole", null);
        assertEquals("org.ofbiz.party.communication.developed.CommunicationEventRole", value.getClass().getName());
    }

    public void testMakeValueCommunicationEventType() {
        GenericValue value = delegator.makeValue("CommunicationEventType", null);
        assertEquals("org.ofbiz.party.communication.developed.CommunicationEventType", value.getClass().getName());
    }

    public void testMakeValueCompany() {
        GenericValue value = delegator.makeValue("Company", null);
        assertEquals("org.ofbiz.party.party.developed.Company", value.getClass().getName());
    }

    public void testCompanySuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("Company", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.party.party.developed.Party", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueContactMech() {
        GenericValue value = delegator.makeValue("ContactMech", null);
        assertEquals("org.ofbiz.party.contact.developed.ContactMech", value.getClass().getName());
    }

    public void testMakeValueContactMechPurposeType() {
        GenericValue value = delegator.makeValue("ContactMechPurposeType", null);
        assertEquals("org.ofbiz.party.contact.developed.ContactMechPurposeType", value.getClass().getName());
    }

    public void testMakeValueContactMechType() {
        GenericValue value = delegator.makeValue("ContactMechType", null);
        assertEquals("org.ofbiz.party.contact.developed.ContactMechType", value.getClass().getName());
    }

    public void testMakeValueContactMechTypePurpose() {
        GenericValue value = delegator.makeValue("ContactMechTypePurpose", null);
        assertEquals("org.ofbiz.party.contact.developed.ContactMechTypePurpose", value.getClass().getName());
    }

    public void testMakeValueContent() {
        GenericValue value = delegator.makeValue("Content", null);
        assertEquals("org.ofbiz.content.content.developed.Content", value.getClass().getName());
    }

    public void testMakeValueContentAssoc() {
        GenericValue value = delegator.makeValue("ContentAssoc", null);
        assertEquals("org.ofbiz.content.content.developed.ContentAssoc", value.getClass().getName());
    }

    public void testMakeValueContentAssocPredicate() {
        GenericValue value = delegator.makeValue("ContentAssocPredicate", null);
        assertEquals("org.ofbiz.content.content.developed.ContentAssocPredicate", value.getClass().getName());
    }

    public void testMakeValueContentAssocType() {
        GenericValue value = delegator.makeValue("ContentAssocType", null);
        assertEquals("org.ofbiz.content.content.developed.ContentAssocType", value.getClass().getName());
    }

    public void testMakeValueContentAttribute() {
        GenericValue value = delegator.makeValue("ContentAttribute", null);
        assertEquals("org.ofbiz.content.content.developed.ContentAttribute", value.getClass().getName());
    }

    public void testMakeValueContentMetaData() {
        GenericValue value = delegator.makeValue("ContentMetaData", null);
        assertEquals("org.ofbiz.content.content.developed.ContentMetaData", value.getClass().getName());
    }

    public void testMakeValueContentOperation() {
        GenericValue value = delegator.makeValue("ContentOperation", null);
        assertEquals("org.ofbiz.content.content.developed.ContentOperation", value.getClass().getName());
    }

    public void testMakeValueContentOperationStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "CONTENTOPERATIONSTATUS"));
        assertEquals("org.ofbiz.content.content.developed.ContentOperationStatus", value.getClass().getName());
    }

    public void testMakeValueContentPurpose() {
        GenericValue value = delegator.makeValue("ContentPurpose", null);
        assertEquals("org.ofbiz.content.content.developed.ContentPurpose", value.getClass().getName());
    }

    public void testMakeValueContentPurposeOperation() {
        GenericValue value = delegator.makeValue("ContentPurposeOperation", null);
        assertEquals("org.ofbiz.content.content.developed.ContentPurposeOperation", value.getClass().getName());
    }

    public void testMakeValueContentPurposeType() {
        GenericValue value = delegator.makeValue("ContentPurposeType", null);
        assertEquals("org.ofbiz.content.content.developed.ContentPurposeType", value.getClass().getName());
    }

    public void testMakeValueContentRole() {
        GenericValue value = delegator.makeValue("ContentRole", null);
        assertEquals("org.ofbiz.content.content.developed.ContentRole", value.getClass().getName());
    }

    public void testMakeValueContentStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "CONTENTSTATUS"));
        assertEquals("org.ofbiz.content.content.developed.ContentStatus", value.getClass().getName());
    }

    public void testMakeValueContentType() {
        GenericValue value = delegator.makeValue("ContentType", null);
        assertEquals("org.ofbiz.content.content.developed.ContentType", value.getClass().getName());
    }

    public void testMakeValueContract() {
        GenericValue value = delegator.makeValue("Contract", null);
        assertEquals("org.ofbiz.servicemgnt.contract.developed.Contract", value.getClass().getName());
    }

    public void testContractSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("Contract", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.manufacturing.project.developed.Project", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueContractContent() {
        GenericValue value = delegator.makeValue("ContractContent", null);
        assertEquals("org.ofbiz.servicemgnt.contract.developed.ContractContent", value.getClass().getName());
    }

    public void testMakeValueContractContentPurpose() {
        GenericValue value = delegator.makeValue("ContractContentPurpose", null);
        assertEquals("org.ofbiz.servicemgnt.contract.developed.ContractContentPurpose", value.getClass().getName());
    }

    public void testMakeValueCostType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "CTYP"));
        assertEquals("org.ofbiz.manufacturing.cost.developed.CostType", value.getClass().getName());
    }

    public void testMakeValueCreditCard() {
        GenericValue value = delegator.makeValue("CreditCard", null);
        assertEquals("org.ofbiz.accounting.payment.developed.CreditCard", value.getClass().getName());
    }

    public void testCreditCardSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("CreditCard", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.accounting.payment.developed.PaymentMethod", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueCurrency_Measure() {
        GenericValue value = delegator.makeValue("Uom", UtilMisc.toMap("uomTypeId", "CURRENCY_MEASURE"));
        assertEquals("org.ofbiz.common.uom.developed.Currency_Measure", value.getClass().getName());
    }

    public void testMakeValueCustRequest() {
        GenericValue value = delegator.makeValue("CustRequest", null);
        assertEquals("org.ofbiz.servicemgnt.request.developed.CustRequest", value.getClass().getName());
    }

    public void testMakeValueCustRequestContentAssoc() {
        GenericValue value = delegator.makeValue("CustRequestContentAssoc", null);
        assertEquals("org.ofbiz.servicemgnt.request.developed.CustRequestContentAssoc", value.getClass().getName());
    }

    public void testMakeValueCustRequestPriority() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "CRQPRI"));
        assertEquals("org.ofbiz.servicemgnt.request.developed.CustRequestPriority", value.getClass().getName());
    }

    public void testMakeValueCustRequestProduct() {
        GenericValue value = delegator.makeValue("CustRequestProduct", null);
        assertEquals("org.ofbiz.servicemgnt.request.developed.CustRequestProduct", value.getClass().getName());
    }

    public void testMakeValueCustRequestProductPurpose() {
        GenericValue value = delegator.makeValue("CustRequestProductPurpose", null);
        assertEquals("org.ofbiz.servicemgnt.request.developed.CustRequestProductPurpose", value.getClass().getName());
    }

    public void testMakeValueCustRequestSeverity() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "CRQSEV"));
        assertEquals("org.ofbiz.servicemgnt.request.developed.CustRequestSeverity", value.getClass().getName());
    }

    public void testMakeValueCustomMethod() {
        GenericValue value = delegator.makeValue("CustomMethod", null);
        assertEquals("org.ofbiz.common.method.developed.CustomMethod", value.getClass().getName());
    }

    public void testMakeValueDataCategory() {
        GenericValue value = delegator.makeValue("DataCategory", null);
        assertEquals("org.ofbiz.content.data.developed.DataCategory", value.getClass().getName());
    }

    public void testMakeValueDataResource() {
        GenericValue value = delegator.makeValue("DataResource", null);
        assertEquals("org.ofbiz.content.data.developed.DataResource", value.getClass().getName());
    }

    public void testMakeValueDataResourceMetaData() {
        GenericValue value = delegator.makeValue("DataResourceMetaData", null);
        assertEquals("org.ofbiz.content.data.developed.DataResourceMetaData", value.getClass().getName());
    }

    public void testMakeValueDataResourceType() {
        GenericValue value = delegator.makeValue("DataResourceType", null);
        assertEquals("org.ofbiz.content.data.developed.DataResourceType", value.getClass().getName());
    }

    public void testMakeValueDataSource() {
        GenericValue value = delegator.makeValue("DataSource", null);
        assertEquals("org.ofbiz.common.datasource.developed.DataSource", value.getClass().getName());
    }

    public void testMakeValueDataSourceType() {
        GenericValue value = delegator.makeValue("DataSourceType", null);
        assertEquals("org.ofbiz.common.datasource.developed.DataSourceType", value.getClass().getName());
    }

    public void testMakeValueDebitCredit() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "DEBITCREDIT"));
        assertEquals("org.ofbiz.accounting.transaction.developed.DebitCredit", value.getClass().getName());
    }

    public void testMakeValueDelivery() {
        GenericValue value = delegator.makeValue("Delivery", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.Delivery", value.getClass().getName());
    }

    public void testMakeValueDocument() {
        GenericValue value = delegator.makeValue("Document", null);
        assertEquals("org.ofbiz.content.document.developed.Document", value.getClass().getName());
    }

    public void testMakeValueDocumentAttribute() {
        GenericValue value = delegator.makeValue("DocumentAttribute", null);
        assertEquals("org.ofbiz.content.document.developed.DocumentAttribute", value.getClass().getName());
    }

    public void testMakeValueDocumentType() {
        GenericValue value = delegator.makeValue("DocumentType", null);
        assertEquals("org.ofbiz.content.document.developed.DocumentType", value.getClass().getName());
    }

    public void testMakeValueDueDcalMethod() {
        GenericValue value = delegator.makeValue("DueDcalMethod", null);
        assertEquals("org.ofbiz.accounting.payment.developed.DueDcalMethod", value.getClass().getName());
    }

    public void testMakeValueEanCode() {
        GenericValue value = delegator.makeValue("EanCode", null);
        assertEquals("org.ofbiz.facility.location.developed.EanCode", value.getClass().getName());
    }

    public void testMakeValueEanCodeProdAssoc() {
        GenericValue value = delegator.makeValue("EanCodeProdAssoc", null);
        assertEquals("org.ofbiz.facility.location.developed.EanCodeProdAssoc", value.getClass().getName());
    }

    public void testMakeValueEftAccount() {
        GenericValue value = delegator.makeValue("EftAccount", null);
        assertEquals("org.ofbiz.accounting.payment.developed.EftAccount", value.getClass().getName());
    }

    public void testEftAccountSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("EftAccount", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.accounting.payment.developed.PaymentMethod", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueElectronicText() {
        GenericValue value = delegator.makeValue("ElectronicText", null);
        assertEquals("org.ofbiz.content.data.developed.ElectronicText", value.getClass().getName());
    }

    public void testElectronicTextSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ElectronicText", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.content.data.developed.DataResource", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueEmailType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "EMAILTYPE"));
        assertEquals("org.ofbiz.order.order.developed.EmailType", value.getClass().getName());
    }

    public void testMakeValueEnumeration() {
        GenericValue value = delegator.makeValue("Enumeration", null);
        assertEquals("org.ofbiz.common.enumeration.developed.Enumeration", value.getClass().getName());
    }

    public void testMakeValueExtAttrType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "EXTATTRTYPE"));
        assertEquals("org.ofbiz.common.enumeration.developed.ExtAttrType", value.getClass().getName());
    }

    public void testMakeValueExtentAttribute() {
        GenericValue value = delegator.makeValue("ExtentAttribute", null);
        assertEquals("org.ofbiz.common.enumeration.developed.ExtentAttribute", value.getClass().getName());
    }

    public void testMakeValueExternalStockEvent() {
        GenericValue value = delegator.makeValue("ExternalStockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.ExternalStockEvent", value.getClass().getName());
    }

    public void testExternalStockEventSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ExternalStockEvent", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueExternalStockEventPlanned() {
        GenericValue value = delegator.makeValue("ExternalStockEventPlanned", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.ExternalStockEventPlanned", value.getClass().getName());
    }

    public void testExternalStockEventPlannedSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ExternalStockEventPlanned", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueFacilityCarrierShipment() {
        GenericValue value = delegator.makeValue("FacilityCarrierShipment", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.FacilityCarrierShipment", value.getClass().getName());
    }

    public void testMakeValueFacilityStockEvent() {
        GenericValue value = delegator.makeValue("FacilityStockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.FacilityStockEvent", value.getClass().getName());
    }

    public void testFacilityStockEventSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("FacilityStockEvent", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueFacilityStockEventPlanned() {
        GenericValue value = delegator.makeValue("FacilityStockEventPlanned", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.FacilityStockEventPlanned", value.getClass().getName());
    }

    public void testFacilityStockEventPlannedSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("FacilityStockEventPlanned", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueForecastPPlanPeriod() {
        GenericValue value = delegator.makeValue("ForecastPPlanPeriod", null);
        assertEquals("org.ofbiz.manufacturing.mps.developed.ForecastPPlanPeriod", value.getClass().getName());
    }

    public void testMakeValueForecastPlanning() {
        GenericValue value = delegator.makeValue("ForecastPlanning", null);
        assertEquals("org.ofbiz.manufacturing.mps.developed.ForecastPlanning", value.getClass().getName());
    }

    public void testForecastPlanningSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ForecastPlanning", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.manufacturing.planning.developed.Planning", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueGeo() {
        GenericValue value = delegator.makeValue("Geo", null);
        assertEquals("org.ofbiz.common.geo.developed.Geo", value.getClass().getName());
    }

    public void testMakeValueGeoAssoc() {
        GenericValue value = delegator.makeValue("GeoAssoc", null);
        assertEquals("org.ofbiz.common.geo.developed.GeoAssoc", value.getClass().getName());
    }

    public void testMakeValueGeoAssocType() {
        GenericValue value = delegator.makeValue("GeoAssocType", null);
        assertEquals("org.ofbiz.common.geo.developed.GeoAssocType", value.getClass().getName());
    }

    public void testMakeValueGeoType() {
        GenericValue value = delegator.makeValue("GeoType", null);
        assertEquals("org.ofbiz.common.geo.developed.GeoType", value.getClass().getName());
    }

    public void testMakeValueGiftCard() {
        GenericValue value = delegator.makeValue("GiftCard", null);
        assertEquals("org.ofbiz.accounting.payment.developed.GiftCard", value.getClass().getName());
    }

    public void testGiftCardSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("GiftCard", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.accounting.payment.developed.PaymentMethod", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueGlAccountRollup() {
        GenericValue value = delegator.makeValue("GlAccountRollup", null);
        assertEquals("org.ofbiz.accounting.staticdata.developed.GlAccountRollup", value.getClass().getName());
    }

    public void testMakeValueGlAccountWithParty() {
        GenericValue value = delegator.makeValue("GlAccountWithParty", null);
        assertEquals("org.ofbiz.accounting.staticdata.developed.GlAccountWithParty", value.getClass().getName());
    }

    public void testGlAccountWithPartySuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("GlAccountWithParty", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.accounting.staticdata.developed.NGlAccount", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueGlEntry() {
        GenericValue value = delegator.makeValue("GlEntry", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.GlEntry", value.getClass().getName());
    }

    public void testMakeValueGlEntryAccount() {
        GenericValue value = delegator.makeValue("GlEntryAccount", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.GlEntryAccount", value.getClass().getName());
    }

    public void testMakeValueGlPeriod() {
        GenericValue value = delegator.makeValue("GlPeriod", null);
        assertEquals("org.ofbiz.accounting.staticdata.developed.GlPeriod", value.getClass().getName());
    }

    public void testMakeValueGlPeriodType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "GLPERIODTYPE"));
        assertEquals("org.ofbiz.accounting.staticdata.developed.GlPeriodType", value.getClass().getName());
    }

    public void testMakeValueHeading() {
        GenericValue value = delegator.makeValue("Heading", null);
        assertEquals("org.ofbiz.website.cms.developed.Heading", value.getClass().getName());
    }

    public void testMakeValueHourCost() {
        GenericValue value = delegator.makeValue("HourCost", null);
        assertEquals("org.ofbiz.manufacturing.cost.developed.HourCost", value.getClass().getName());
    }

    public void testMakeValueImageDataResource() {
        GenericValue value = delegator.makeValue("ImageDataResource", null);
        assertEquals("org.ofbiz.content.data.developed.ImageDataResource", value.getClass().getName());
    }

    public void testImageDataResourceSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ImageDataResource", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.content.data.developed.DataResource", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueIntegEventType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "IETYPE"));
        assertEquals("org.ofbiz.accounting.accintegration.developed.IntegEventType", value.getClass().getName());
    }

    public void testMakeValueIntegTransactionItem() {
        GenericValue value = delegator.makeValue("IntegTransactionItem", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.IntegTransactionItem", value.getClass().getName());
    }

    public void testMakeValueIntegrationEntry() {
        GenericValue value = delegator.makeValue("IntegrationEntry", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.IntegrationEntry", value.getClass().getName());
    }

    public void testMakeValueIntegrationEntryAccount() {
        GenericValue value = delegator.makeValue("IntegrationEntryAccount", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.IntegrationEntryAccount", value.getClass().getName());
    }

    public void testMakeValueIntegrationEntryStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "IES"));
        assertEquals("org.ofbiz.accounting.accintegration.developed.IntegrationEntryStatus", value.getClass().getName());
    }

    public void testMakeValueIntegrationRule() {
        GenericValue value = delegator.makeValue("IntegrationRule", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.IntegrationRule", value.getClass().getName());
    }

    public void testMakeValueInventory() {
        GenericValue value = delegator.makeValue("Inventory", null);
        assertEquals("org.ofbiz.facility.inventory.developed.Inventory", value.getClass().getName());
    }

    public void testMakeValueInventoryFacility() {
        GenericValue value = delegator.makeValue("InventoryFacility", null);
        assertEquals("org.ofbiz.facility.inventory.developed.InventoryFacility", value.getClass().getName());
    }

    public void testMakeValueInventoryProduct() {
        GenericValue value = delegator.makeValue("InventoryProduct", null);
        assertEquals("org.ofbiz.facility.inventory.developed.InventoryProduct", value.getClass().getName());
    }

    public void testMakeValueInventoryRole() {
        GenericValue value = delegator.makeValue("InventoryRole", null);
        assertEquals("org.ofbiz.facility.inventory.developed.InventoryRole", value.getClass().getName());
    }

    public void testMakeValueInventoryType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "INVENTORYTYPE"));
        assertEquals("org.ofbiz.facility.inventory.developed.InventoryType", value.getClass().getName());
    }

    public void testMakeValueInvoice() {
        GenericValue value = delegator.makeValue("Invoice", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.Invoice", value.getClass().getName());
    }

    public void testMakeValueInvoiceContactMech() {
        GenericValue value = delegator.makeValue("InvoiceContactMech", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.InvoiceContactMech", value.getClass().getName());
    }

    public void testMakeValueInvoiceItem() {
        GenericValue value = delegator.makeValue("InvoiceItem", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.InvoiceItem", value.getClass().getName());
    }

    public void testMakeValueInvoiceItemType() {
        GenericValue value = delegator.makeValue("InvoiceItemType", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.InvoiceItemType", value.getClass().getName());
    }

    public void testMakeValueInvoiceItemTypeMap() {
        GenericValue value = delegator.makeValue("InvoiceItemTypeMap", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.InvoiceItemTypeMap", value.getClass().getName());
    }

    public void testMakeValueInvoiceRole() {
        GenericValue value = delegator.makeValue("InvoiceRole", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.InvoiceRole", value.getClass().getName());
    }

    public void testMakeValueInvoiceStatus() {
        GenericValue value = delegator.makeValue("InvoiceStatus", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.InvoiceStatus", value.getClass().getName());
    }

    public void testMakeValueInvoiceTerm() {
        GenericValue value = delegator.makeValue("InvoiceTerm", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.InvoiceTerm", value.getClass().getName());
    }

    public void testMakeValueJobSandbox() {
        GenericValue value = delegator.makeValue("JobSandbox", null);
        assertEquals("org.ofbiz.common.schedule.developed.JobSandbox", value.getClass().getName());
    }

    public void testMakeValueJuridicClassification() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "JURCL"));
        assertEquals("org.ofbiz.party.party.developed.JuridicClassification", value.getClass().getName());
    }

    public void testMakeValueLangAbrev() {
        GenericValue value = delegator.makeValue("LangAbrev", null);
        assertEquals("org.ofbiz.website.cms.developed.LangAbrev", value.getClass().getName());
    }

    public void testMakeValueMachine() {
        GenericValue value = delegator.makeValue("TechDataResource", UtilMisc.toMap("derivation", "MACHINE"));
        assertEquals("org.ofbiz.manufacturing.techdata.developed.Machine", value.getClass().getName());
    }

    public void testMakeValueMappingType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "MAPPINGTYPE"));
        assertEquals("org.ofbiz.accounting.accintegration.developed.MappingType", value.getClass().getName());
    }

    public void testMakeValueMarital() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "MARITAL"));
        assertEquals("org.ofbiz.party.party.developed.Marital", value.getClass().getName());
    }

    public void testMakeValueMetaDataPredicate() {
        GenericValue value = delegator.makeValue("MetaDataPredicate", null);
        assertEquals("org.ofbiz.content.data.developed.MetaDataPredicate", value.getClass().getName());
    }

    public void testMakeValueMimeType() {
        GenericValue value = delegator.makeValue("MimeType", null);
        assertEquals("org.ofbiz.content.data.developed.MimeType", value.getClass().getName());
    }

    public void testMakeValueMpsPPlanPeriod() {
        GenericValue value = delegator.makeValue("MpsPPlanPeriod", null);
        assertEquals("org.ofbiz.manufacturing.mps.developed.MpsPPlanPeriod", value.getClass().getName());
    }

    public void testMakeValueMpsPlanning() {
        GenericValue value = delegator.makeValue("MpsPlanning", null);
        assertEquals("org.ofbiz.manufacturing.mps.developed.MpsPlanning", value.getClass().getName());
    }

    public void testMpsPlanningSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("MpsPlanning", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.manufacturing.planning.developed.Planning", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueMpsStockEventPlan() {
        GenericValue value = delegator.makeValue("MpsStockEventPlan", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.MpsStockEventPlan", value.getClass().getName());
    }

    public void testMpsStockEventPlanSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("MpsStockEventPlan", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueMrpRun() {
        GenericValue value = delegator.makeValue("MrpRun", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.MrpRun", value.getClass().getName());
    }

    public void testMakeValueMrpRunLog() {
        GenericValue value = delegator.makeValue("MrpRunLog", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.MrpRunLog", value.getClass().getName());
    }

    public void testMrpRunLogSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("MrpRunLog", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.common.log.developed.ApplicationLog", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueMrpRunStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "MRPRUNSTATUS"));
        assertEquals("org.ofbiz.manufacturing.planning.developed.MrpRunStatus", value.getClass().getName());
    }

    public void testMakeValueNFacility() {
        GenericValue value = delegator.makeValue("NFacility", null);
        assertEquals("org.ofbiz.facility.location.developed.NFacility", value.getClass().getName());
    }

    public void testMakeValueNFacilityContactMech() {
        GenericValue value = delegator.makeValue("NFacilityContactMech", null);
        assertEquals("org.ofbiz.facility.location.developed.NFacilityContactMech", value.getClass().getName());
    }

    public void testMakeValueNFacilityCtMechPurpose() {
        GenericValue value = delegator.makeValue("NFacilityCtMechPurpose", null);
        assertEquals("org.ofbiz.facility.location.developed.NFacilityCtMechPurpose", value.getClass().getName());
    }

    public void testMakeValueNFacilityPurpose() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "NFACILITYPURPOSE"));
        assertEquals("org.ofbiz.facility.location.developed.NFacilityPurpose", value.getClass().getName());
    }

    public void testMakeValueNFacilityRole() {
        GenericValue value = delegator.makeValue("NFacilityRole", null);
        assertEquals("org.ofbiz.facility.location.developed.NFacilityRole", value.getClass().getName());
    }

    public void testMakeValueNFacilityType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "NFACILITYTYPE"));
        assertEquals("org.ofbiz.facility.location.developed.NFacilityType", value.getClass().getName());
    }

    public void testMakeValueNGlAccount() {
        GenericValue value = delegator.makeValue("NGlAccount", null);
        assertEquals("org.ofbiz.accounting.staticdata.developed.NGlAccount", value.getClass().getName());
    }

    public void testMakeValueNInventoryItem() {
        GenericValue value = delegator.makeValue("NInventoryItem", null);
        assertEquals("org.ofbiz.facility.inventory.developed.NInventoryItem", value.getClass().getName());
    }

    public void testMakeValueNaf() {
        GenericValue value = delegator.makeValue("Naf", null);
        assertEquals("org.ofbiz.party.party.developed.Naf", value.getClass().getName());
    }

    public void testMakeValueNafType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "NAFTYPE"));
        assertEquals("org.ofbiz.party.party.developed.NafType", value.getClass().getName());
    }

    public void testMakeValueNes() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "NES"));
        assertEquals("org.ofbiz.party.party.developed.Nes", value.getClass().getName());
    }

    public void testMakeValueNoteData() {
        GenericValue value = delegator.makeValue("NoteData", null);
        assertEquals("org.ofbiz.common.note.developed.NoteData", value.getClass().getName());
    }

    public void testMakeValueOneToOne() {
        GenericValue value = delegator.makeValue("OneToOne", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.OneToOne", value.getClass().getName());
    }

    public void testMakeValueOrderAdjustment() {
        GenericValue value = delegator.makeValue("OrderAdjustment", null);
        assertEquals("org.ofbiz.order.order.developed.OrderAdjustment", value.getClass().getName());
    }

    public void testMakeValueOrderContactMech() {
        GenericValue value = delegator.makeValue("OrderContactMech", null);
        assertEquals("org.ofbiz.order.order.developed.OrderContactMech", value.getClass().getName());
    }

    public void testMakeValueOrderHeader() {
        GenericValue value = delegator.makeValue("OrderHeader", null);
        assertEquals("org.ofbiz.order.order.developed.OrderHeader", value.getClass().getName());
    }

    public void testMakeValueOrderHeaderNote() {
        GenericValue value = delegator.makeValue("OrderHeaderNote", null);
        assertEquals("org.ofbiz.order.order.developed.OrderHeaderNote", value.getClass().getName());
    }

    public void testMakeValueOrderItem() {
        GenericValue value = delegator.makeValue("OrderItem", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItem", value.getClass().getName());
    }

    public void testMakeValueOrderItemAssociation() {
        GenericValue value = delegator.makeValue("OrderItemAssociation", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemAssociation", value.getClass().getName());
    }

    public void testMakeValueOrderItemBilling() {
        GenericValue value = delegator.makeValue("OrderItemBilling", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemBilling", value.getClass().getName());
    }

    public void testMakeValueOrderItemContactMech() {
        GenericValue value = delegator.makeValue("OrderItemContactMech", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemContactMech", value.getClass().getName());
    }

    public void testMakeValueOrderItemPriceInfo() {
        GenericValue value = delegator.makeValue("OrderItemPriceInfo", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemPriceInfo", value.getClass().getName());
    }

    public void testMakeValueOrderItemRole() {
        GenericValue value = delegator.makeValue("OrderItemRole", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemRole", value.getClass().getName());
    }

    public void testMakeValueOrderItemShipGroup() {
        GenericValue value = delegator.makeValue("OrderItemShipGroup", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemShipGroup", value.getClass().getName());
    }

    public void testMakeValueOrderItemShipGroupAssoc() {
        GenericValue value = delegator.makeValue("OrderItemShipGroupAssoc", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemShipGroupAssoc", value.getClass().getName());
    }

    public void testMakeValueOrderItemStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "ORDERITEMSTATUS"));
        assertEquals("org.ofbiz.order.order.developed.OrderItemStatus", value.getClass().getName());
    }

    public void testMakeValueOrderItemType() {
        GenericValue value = delegator.makeValue("OrderItemType", null);
        assertEquals("org.ofbiz.order.order.developed.OrderItemType", value.getClass().getName());
    }

    public void testMakeValueOrderNotification() {
        GenericValue value = delegator.makeValue("OrderNotification", null);
        assertEquals("org.ofbiz.order.order.developed.OrderNotification", value.getClass().getName());
    }

    public void testMakeValueOrderPaymentPreference() {
        GenericValue value = delegator.makeValue("OrderPaymentPreference", null);
        assertEquals("org.ofbiz.order.order.developed.OrderPaymentPreference", value.getClass().getName());
    }

    public void testMakeValueOrderRequirementCommitment() {
        GenericValue value = delegator.makeValue("OrderRequirementCommitment", null);
        assertEquals("org.ofbiz.order.requirement.developed.OrderRequirementCommitment", value.getClass().getName());
    }

    public void testMakeValueOrderRole() {
        GenericValue value = delegator.makeValue("OrderRole", null);
        assertEquals("org.ofbiz.order.order.developed.OrderRole", value.getClass().getName());
    }

    public void testMakeValueOrderSalesChannel() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "ORDERSALESCHANNEL"));
        assertEquals("org.ofbiz.order.order.developed.OrderSalesChannel", value.getClass().getName());
    }

    public void testMakeValueOrderShipment() {
        GenericValue value = delegator.makeValue("OrderShipment", null);
        assertEquals("org.ofbiz.order.order.developed.OrderShipment", value.getClass().getName());
    }

    public void testMakeValueOrderStatus() {
        GenericValue value = delegator.makeValue("OrderStatus", null);
        assertEquals("org.ofbiz.order.order.developed.OrderStatus", value.getClass().getName());
    }

    public void testMakeValueOrderStockEvent() {
        GenericValue value = delegator.makeValue("OrderStockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.OrderStockEvent", value.getClass().getName());
    }

    public void testOrderStockEventSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("OrderStockEvent", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueOrderStockEventPlanned() {
        GenericValue value = delegator.makeValue("OrderStockEventPlanned", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.OrderStockEventPlanned", value.getClass().getName());
    }

    public void testOrderStockEventPlannedSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("OrderStockEventPlanned", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueOrderTerm() {
        GenericValue value = delegator.makeValue("OrderTerm", null);
        assertEquals("org.ofbiz.order.order.developed.OrderTerm", value.getClass().getName());
    }

    public void testMakeValueOrderType() {
        GenericValue value = delegator.makeValue("OrderType", null);
        assertEquals("org.ofbiz.order.order.developed.OrderType", value.getClass().getName());
    }

    public void testMakeValuePackaging() {
        GenericValue value = delegator.makeValue("Packaging", null);
        assertEquals("org.ofbiz.common.uom.developed.Packaging", value.getClass().getName());
    }

    public void testMakeValuePackagingConversion() {
        GenericValue value = delegator.makeValue("PackagingConversion", null);
        assertEquals("org.ofbiz.common.uom.developed.PackagingConversion", value.getClass().getName());
    }

    public void testMakeValueParty() {
        GenericValue value = delegator.makeValue("Party", null);
        assertEquals("org.ofbiz.party.party.developed.Party", value.getClass().getName());
    }

    public void testMakeValuePartyAppLogPerm() {
        GenericValue value = delegator.makeValue("PartyAppLogPerm", null);
        assertEquals("org.ofbiz.common.log.developed.PartyAppLogPerm", value.getClass().getName());
    }

    public void testMakeValuePartyClassification() {
        GenericValue value = delegator.makeValue("PartyClassification", null);
        assertEquals("org.ofbiz.party.party.developed.PartyClassification", value.getClass().getName());
    }

    public void testMakeValuePartyClassificationGroup() {
        GenericValue value = delegator.makeValue("PartyClassificationGroup", null);
        assertEquals("org.ofbiz.party.party.developed.PartyClassificationGroup", value.getClass().getName());
    }

    public void testMakeValuePartyClassificationType() {
        GenericValue value = delegator.makeValue("PartyClassificationType", null);
        assertEquals("org.ofbiz.party.party.developed.PartyClassificationType", value.getClass().getName());
    }

    public void testMakeValuePartyContactMech() {
        GenericValue value = delegator.makeValue("PartyContactMech", null);
        assertEquals("org.ofbiz.party.contact.developed.PartyContactMech", value.getClass().getName());
    }

    public void testMakeValuePartyContactMechPurpose() {
        GenericValue value = delegator.makeValue("PartyContactMechPurpose", null);
        assertEquals("org.ofbiz.party.contact.developed.PartyContactMechPurpose", value.getClass().getName());
    }

    public void testMakeValuePartyContent() {
        GenericValue value = delegator.makeValue("PartyContent", null);
        assertEquals("org.ofbiz.party.party.developed.PartyContent", value.getClass().getName());
    }

    public void testMakeValuePartyDataSource() {
        GenericValue value = delegator.makeValue("PartyDataSource", null);
        assertEquals("org.ofbiz.party.party.developed.PartyDataSource", value.getClass().getName());
    }

    public void testMakeValuePartyGroup() {
        GenericValue value = delegator.makeValue("PartyGroup", null);
        assertEquals("org.ofbiz.party.party.developed.PartyGroup", value.getClass().getName());
    }

    public void testPartyGroupSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("PartyGroup", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.party.party.developed.Party", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValuePartyManResource() {
        GenericValue value = delegator.makeValue("PartyManResource", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.PartyManResource", value.getClass().getName());
    }

    public void testMakeValuePartyNote() {
        GenericValue value = delegator.makeValue("PartyNote", null);
        assertEquals("org.ofbiz.party.party.developed.PartyNote", value.getClass().getName());
    }

    public void testMakeValuePartyRelStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "PARTY_REL_STATUS"));
        assertEquals("org.ofbiz.party.party.developed.PartyRelStatus", value.getClass().getName());
    }

    public void testMakeValuePartyRelationship() {
        GenericValue value = delegator.makeValue("PartyRelationship", null);
        assertEquals("org.ofbiz.party.party.developed.PartyRelationship", value.getClass().getName());
    }

    public void testMakeValuePartyRelationshipType() {
        GenericValue value = delegator.makeValue("PartyRelationshipType", null);
        assertEquals("org.ofbiz.party.party.developed.PartyRelationshipType", value.getClass().getName());
    }

    public void testMakeValuePartyRole() {
        GenericValue value = delegator.makeValue("PartyRole", null);
        assertEquals("org.ofbiz.party.party.developed.PartyRole", value.getClass().getName());
    }

    public void testMakeValuePartyRoleAttr() {
        GenericValue value = delegator.makeValue("PartyRoleAttr", null);
        assertEquals("org.ofbiz.party.party.developed.PartyRoleAttr", value.getClass().getName());
    }

    public void testMakeValuePayCalculateVariable() {
        GenericValue value = delegator.makeValue("PayCalculateVariable", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayCalculateVariable", value.getClass().getName());
    }

    public void testPayCalculateVariableSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("PayCalculateVariable", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.humanres.pay.developed.PayVariable", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValuePayColumn() {
        GenericValue value = delegator.makeValue("PayColumn", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayColumn", value.getClass().getName());
    }

    public void testMakeValuePayCompareVariable() {
        GenericValue value = delegator.makeValue("PayCompareVariable", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayCompareVariable", value.getClass().getName());
    }

    public void testPayCompareVariableSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("PayCompareVariable", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.humanres.pay.developed.PayVariable", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValuePayDataVariable() {
        GenericValue value = delegator.makeValue("PayDataVariable", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayDataVariable", value.getClass().getName());
    }

    public void testPayDataVariableSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("PayDataVariable", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.humanres.pay.developed.PayVariable", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValuePayLine() {
        GenericValue value = delegator.makeValue("PayLine", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayLine", value.getClass().getName());
    }

    public void testMakeValuePayPartyVariable() {
        GenericValue value = delegator.makeValue("PayPartyVariable", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayPartyVariable", value.getClass().getName());
    }

    public void testPayPartyVariableSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("PayPartyVariable", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.humanres.pay.developed.PayVariable", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValuePayStatusVariable() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "PAYSTATUSVARIABLE"));
        assertEquals("org.ofbiz.humanres.pay.developed.PayStatusVariable", value.getClass().getName());
    }

    public void testMakeValuePayValue() {
        GenericValue value = delegator.makeValue("PayValue", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayValue", value.getClass().getName());
    }

    public void testMakeValuePayVariable() {
        GenericValue value = delegator.makeValue("PayVariable", null);
        assertEquals("org.ofbiz.humanres.pay.developed.PayVariable", value.getClass().getName());
    }

    public void testMakeValuePayment() {
        GenericValue value = delegator.makeValue("Payment", null);
        assertEquals("org.ofbiz.accounting.payment.developed.Payment", value.getClass().getName());
    }

    public void testMakeValuePaymentMethod() {
        GenericValue value = delegator.makeValue("PaymentMethod", null);
        assertEquals("org.ofbiz.accounting.payment.developed.PaymentMethod", value.getClass().getName());
    }

    public void testMakeValuePaymentMethodType() {
        GenericValue value = delegator.makeValue("PaymentMethodType", null);
        assertEquals("org.ofbiz.accounting.payment.developed.PaymentMethodType", value.getClass().getName());
    }

    public void testMakeValuePaymentMethodTypeService() {
        GenericValue value = delegator.makeValue("CustomMethod", UtilMisc.toMap("customMethodTypeId", "PAYMETHTYPESERV"));
        assertEquals("org.ofbiz.accounting.payment.developed.PaymentMethodTypeService", value.getClass().getName());
    }

    public void testMakeValuePaymentMode() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "PAYMENTMODE"));
        assertEquals("org.ofbiz.accounting.payment.developed.PaymentMode", value.getClass().getName());
    }

    public void testMakeValuePeriodStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "PERIODSTATUS"));
        assertEquals("org.ofbiz.accounting.staticdata.developed.PeriodStatus", value.getClass().getName());
    }

    public void testMakeValuePerson() {
        GenericValue value = delegator.makeValue("Person", null);
        assertEquals("org.ofbiz.party.party.developed.Person", value.getClass().getName());
    }

    public void testPersonSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("Person", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.party.party.developed.Party", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValuePickingList() {
        GenericValue value = delegator.makeValue("PickingList", null);
        assertEquals("org.ofbiz.facility.picking.developed.PickingList", value.getClass().getName());
    }

    public void testMakeValuePlanning() {
        GenericValue value = delegator.makeValue("Planning", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.Planning", value.getClass().getName());
    }

    public void testMakeValuePlanningPeriodDesc() {
        GenericValue value = delegator.makeValue("PlanningPeriodDesc", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.PlanningPeriodDesc", value.getClass().getName());
    }

    public void testMakeValuePlanningPeriodicity() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "PLANNINGPERIODICITY"));
        assertEquals("org.ofbiz.manufacturing.planning.developed.PlanningPeriodicity", value.getClass().getName());
    }

    public void testMakeValuePointsOfTitleTransfer() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "PTSOFTTFR"));
        assertEquals("org.ofbiz.accounting.tax.developed.PointsOfTitleTransfer", value.getClass().getName());
    }

    public void testMakeValuePostalAddress() {
        GenericValue value = delegator.makeValue("PostalAddress", null);
        assertEquals("org.ofbiz.party.contact.developed.PostalAddress", value.getClass().getName());
    }

    public void testPostalAddressSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("PostalAddress", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.party.contact.developed.ContactMech", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValuePostalAddressBoundary() {
        GenericValue value = delegator.makeValue("PostalAddressBoundary", null);
        assertEquals("org.ofbiz.party.contact.developed.PostalAddressBoundary", value.getClass().getName());
    }

    public void testMakeValuePriorityType() {
        GenericValue value = delegator.makeValue("PriorityType", null);
        assertEquals("org.ofbiz.party.party.developed.PriorityType", value.getClass().getName());
    }

    public void testMakeValueProdCatalog() {
        GenericValue value = delegator.makeValue("ProdCatalog", null);
        assertEquals("org.ofbiz.product.catalog.developed.ProdCatalog", value.getClass().getName());
    }

    public void testMakeValueProdCatalogCategory() {
        GenericValue value = delegator.makeValue("ProdCatalogCategory", null);
        assertEquals("org.ofbiz.product.catalog.developed.ProdCatalogCategory", value.getClass().getName());
    }

    public void testMakeValueProdCatalogCategoryType() {
        GenericValue value = delegator.makeValue("ProdCatalogCategoryType", null);
        assertEquals("org.ofbiz.product.catalog.developed.ProdCatalogCategoryType", value.getClass().getName());
    }

    public void testMakeValueProdCatalogInvFacility() {
        GenericValue value = delegator.makeValue("ProdCatalogInvFacility", null);
        assertEquals("org.ofbiz.product.catalog.developed.ProdCatalogInvFacility", value.getClass().getName());
    }

    public void testMakeValueProdCatalogRole() {
        GenericValue value = delegator.makeValue("ProdCatalogRole", null);
        assertEquals("org.ofbiz.product.catalog.developed.ProdCatalogRole", value.getClass().getName());
    }

    public void testMakeValueProdConfItemContent() {
        GenericValue value = delegator.makeValue("ProdConfItemContent", null);
        assertEquals("org.ofbiz.product.config.developed.ProdConfItemContent", value.getClass().getName());
    }

    public void testMakeValueProdGenCodType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "PRODGENCODTYPE"));
        assertEquals("org.ofbiz.facility.location.developed.ProdGenCodType", value.getClass().getName());
    }

    public void testMakeValueProdReqMethod() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "PROD_REQ_METHOD"));
        assertEquals("org.ofbiz.product.product.developed.ProdReqMethod", value.getClass().getName());
    }

    public void testMakeValueProduct() {
        GenericValue value = delegator.makeValue("Product", null);
        assertEquals("org.ofbiz.product.product.developed.Product", value.getClass().getName());
    }

    public void testMakeValueProductCategory() {
        GenericValue value = delegator.makeValue("ProductCategory", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategory", value.getClass().getName());
    }

    public void testMakeValueProductCategoryAttribute() {
        GenericValue value = delegator.makeValue("ProductCategoryAttribute", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategoryAttribute", value.getClass().getName());
    }

    public void testMakeValueProductCategoryContent() {
        GenericValue value = delegator.makeValue("ProductCategoryContent", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategoryContent", value.getClass().getName());
    }

    public void testMakeValueProductCategoryContentType() {
        GenericValue value = delegator.makeValue("ProductCategoryContentType", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategoryContentType", value.getClass().getName());
    }

    public void testMakeValueProductCategoryMember() {
        GenericValue value = delegator.makeValue("ProductCategoryMember", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategoryMember", value.getClass().getName());
    }

    public void testMakeValueProductCategoryRole() {
        GenericValue value = delegator.makeValue("ProductCategoryRole", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategoryRole", value.getClass().getName());
    }

    public void testMakeValueProductCategoryType() {
        GenericValue value = delegator.makeValue("ProductCategoryType", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategoryType", value.getClass().getName());
    }

    public void testMakeValueProductCategoryTypeAttr() {
        GenericValue value = delegator.makeValue("ProductCategoryTypeAttr", null);
        assertEquals("org.ofbiz.product.category.developed.ProductCategoryTypeAttr", value.getClass().getName());
    }

    public void testMakeValueProductCheckMeasure() {
        GenericValue value = delegator.makeValue("ProductCheckMeasure", null);
        assertEquals("org.ofbiz.quality.checkMeasure.developed.ProductCheckMeasure", value.getClass().getName());
    }

    public void testMakeValueProductConfig() {
        GenericValue value = delegator.makeValue("ProductConfig", null);
        assertEquals("org.ofbiz.product.config.developed.ProductConfig", value.getClass().getName());
    }

    public void testMakeValueProductConfigConfig() {
        GenericValue value = delegator.makeValue("ProductConfigConfig", null);
        assertEquals("org.ofbiz.product.config.developed.ProductConfigConfig", value.getClass().getName());
    }

    public void testMakeValueProductConfigItem() {
        GenericValue value = delegator.makeValue("ProductConfigItem", null);
        assertEquals("org.ofbiz.product.config.developed.ProductConfigItem", value.getClass().getName());
    }

    public void testMakeValueProductConfigOption() {
        GenericValue value = delegator.makeValue("ProductConfigOption", null);
        assertEquals("org.ofbiz.product.config.developed.ProductConfigOption", value.getClass().getName());
    }

    public void testMakeValueProductConfigOptionIactn() {
        GenericValue value = delegator.makeValue("ProductConfigOptionIactn", null);
        assertEquals("org.ofbiz.product.config.developed.ProductConfigOptionIactn", value.getClass().getName());
    }

    public void testMakeValueProductConfigProduct() {
        GenericValue value = delegator.makeValue("ProductConfigProduct", null);
        assertEquals("org.ofbiz.product.config.developed.ProductConfigProduct", value.getClass().getName());
    }

    public void testMakeValueProductConfigStats() {
        GenericValue value = delegator.makeValue("ProductConfigStats", null);
        assertEquals("org.ofbiz.product.config.developed.ProductConfigStats", value.getClass().getName());
    }

    public void testMakeValueProductLot() {
        GenericValue value = delegator.makeValue("ProductLot", null);
        assertEquals("org.ofbiz.facility.location.developed.ProductLot", value.getClass().getName());
    }

    public void testMakeValueProductManufacturingRule() {
        GenericValue value = delegator.makeValue("ProductManufacturingRule", null);
        assertEquals("org.ofbiz.manufacturing.configurator.developed.ProductManufacturingRule", value.getClass().getName());
    }

    public void testMakeValueProductNFacility() {
        GenericValue value = delegator.makeValue("ProductNFacility", null);
        assertEquals("org.ofbiz.facility.location.developed.ProductNFacility", value.getClass().getName());
    }

    public void testMakeValueProductPrice() {
        GenericValue value = delegator.makeValue("ProductPrice", null);
        assertEquals("org.ofbiz.product.price.developed.ProductPrice", value.getClass().getName());
    }

    public void testMakeValueProductPriceItem() {
        GenericValue value = delegator.makeValue("ProductPriceItem", null);
        assertEquals("org.ofbiz.product.price.developed.ProductPriceItem", value.getClass().getName());
    }

    public void testMakeValueProductPriceItemType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "PPT"));
        assertEquals("org.ofbiz.product.price.developed.ProductPriceItemType", value.getClass().getName());
    }

    public void testMakeValueProductPricePurpose() {
        GenericValue value = delegator.makeValue("ProductPricePurpose", null);
        assertEquals("org.ofbiz.product.price.developed.ProductPricePurpose", value.getClass().getName());
    }

    public void testMakeValueProductPriceType() {
        GenericValue value = delegator.makeValue("ProductPriceType", null);
        assertEquals("org.ofbiz.product.price.developed.ProductPriceType", value.getClass().getName());
    }

    public void testMakeValueProductRevision() {
        GenericValue value = delegator.makeValue("ProductRevision", null);
        assertEquals("org.ofbiz.product.product.developed.ProductRevision", value.getClass().getName());
    }

    public void testMakeValueProductRouting() {
        GenericValue value = delegator.makeValue("ProductRouting", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.ProductRouting", value.getClass().getName());
    }

    public void testMakeValueProductStore() {
        GenericValue value = delegator.makeValue("ProductStore", null);
        assertEquals("org.ofbiz.product.store.developed.ProductStore", value.getClass().getName());
    }

    public void testMakeValueProductStoreCatalog() {
        GenericValue value = delegator.makeValue("ProductStoreCatalog", null);
        assertEquals("org.ofbiz.product.store.developed.ProductStoreCatalog", value.getClass().getName());
    }

    public void testMakeValueProductStoreEmailSetting() {
        GenericValue value = delegator.makeValue("ProductStoreEmailSetting", null);
        assertEquals("org.ofbiz.product.store.developed.ProductStoreEmailSetting", value.getClass().getName());
    }

    public void testMakeValueProductStoreFacility() {
        GenericValue value = delegator.makeValue("ProductStoreFacility", null);
        assertEquals("org.ofbiz.product.store.developed.ProductStoreFacility", value.getClass().getName());
    }

    public void testMakeValueProductStoreShipmentMeth() {
        GenericValue value = delegator.makeValue("ProductStoreShipmentMeth", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ProductStoreShipmentMeth", value.getClass().getName());
    }

    public void testMakeValueProductionRun() {
        GenericValue value = delegator.makeValue("WRun", UtilMisc.toMap("discriminator", "PRODUCTIONRUN"));
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.ProductionRun", value.getClass().getName());
    }

    public void testMakeValueProject() {
        GenericValue value = delegator.makeValue("Project", null);
        assertEquals("org.ofbiz.manufacturing.project.developed.Project", value.getClass().getName());
    }

    public void testMakeValueProjectManResource() {
        GenericValue value = delegator.makeValue("ProjectManResource", null);
        assertEquals("org.ofbiz.manufacturing.project.developed.ProjectManResource", value.getClass().getName());
    }

    public void testMakeValueProjectPeriod() {
        GenericValue value = delegator.makeValue("ProjectPeriod", null);
        assertEquals("org.ofbiz.manufacturing.project.developed.ProjectPeriod", value.getClass().getName());
    }

    public void testMakeValueProjectPeriodAssoc() {
        GenericValue value = delegator.makeValue("ProjectPeriodAssoc", null);
        assertEquals("org.ofbiz.manufacturing.project.developed.ProjectPeriodAssoc", value.getClass().getName());
    }

    public void testMakeValueProjectRole() {
        GenericValue value = delegator.makeValue("ProjectRole", null);
        assertEquals("org.ofbiz.manufacturing.project.developed.ProjectRole", value.getClass().getName());
    }

    public void testMakeValueProjectRun() {
        GenericValue value = delegator.makeValue("WRun", UtilMisc.toMap("discriminator", "PROJECTRUN"));
        assertEquals("org.ofbiz.manufacturing.project.developed.ProjectRun", value.getClass().getName());
    }

    public void testMakeValueProjectStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "PRJS"));
        assertEquals("org.ofbiz.manufacturing.project.developed.ProjectStatus", value.getClass().getName());
    }

    public void testMakeValueProposedOrder() {
        GenericValue value = delegator.makeValue("ProposedOrder", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.ProposedOrder", value.getClass().getName());
    }

    public void testMakeValueProposedOrderType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "PROPOSEDORDERTYPE"));
        assertEquals("org.ofbiz.manufacturing.planning.developed.ProposedOrderType", value.getClass().getName());
    }

    public void testMakeValuePurchaseInvoice() {
        GenericValue value = delegator.makeValue("PurchaseInvoice", null);
        assertEquals("org.ofbiz.accounting.invoice.developed.PurchaseInvoice", value.getClass().getName());
    }

    public void testPurchaseInvoiceSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("PurchaseInvoice", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.accounting.invoice.developed.Invoice", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueQualityRequest() {
        GenericValue value = delegator.makeValue("QualityRequest", null);
        assertEquals("org.ofbiz.quality.request.developed.QualityRequest", value.getClass().getName());
    }

    public void testMakeValueQualityRequestOrigin() {
        GenericValue value = delegator.makeValue("QualityRequestOrigin", null);
        assertEquals("org.ofbiz.quality.request.developed.QualityRequestOrigin", value.getClass().getName());
    }

    public void testMakeValueQualityRequestRole() {
        GenericValue value = delegator.makeValue("QualityRequestRole", null);
        assertEquals("org.ofbiz.quality.request.developed.QualityRequestRole", value.getClass().getName());
    }

    public void testMakeValueQualityRequestStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "QRSTATUS"));
        assertEquals("org.ofbiz.quality.request.developed.QualityRequestStatus", value.getClass().getName());
    }

    public void testMakeValueQualityRequestType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "QRTYPE"));
        assertEquals("org.ofbiz.quality.request.developed.QualityRequestType", value.getClass().getName());
    }

    public void testMakeValueQuote() {
        GenericValue value = delegator.makeValue("Quote", null);
        assertEquals("org.ofbiz.order.quote.developed.Quote", value.getClass().getName());
    }

    public void testMakeValueQuoteAttribute() {
        GenericValue value = delegator.makeValue("QuoteAttribute", null);
        assertEquals("org.ofbiz.order.quote.developed.QuoteAttribute", value.getClass().getName());
    }

    public void testMakeValueQuoteItem() {
        GenericValue value = delegator.makeValue("QuoteItem", null);
        assertEquals("org.ofbiz.order.quote.developed.QuoteItem", value.getClass().getName());
    }

    public void testMakeValueQuoteRole() {
        GenericValue value = delegator.makeValue("QuoteRole", null);
        assertEquals("org.ofbiz.order.quote.developed.QuoteRole", value.getClass().getName());
    }

    public void testMakeValueQuoteStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "QUOTESTATUS"));
        assertEquals("org.ofbiz.order.quote.developed.QuoteStatus", value.getClass().getName());
    }

    public void testMakeValueQuoteStockEventPlanned() {
        GenericValue value = delegator.makeValue("QuoteStockEventPlanned", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.QuoteStockEventPlanned", value.getClass().getName());
    }

    public void testQuoteStockEventPlannedSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("QuoteStockEventPlanned", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueQuoteTerm() {
        GenericValue value = delegator.makeValue("QuoteTerm", null);
        assertEquals("org.ofbiz.order.quote.developed.QuoteTerm", value.getClass().getName());
    }

    public void testMakeValueQuoteTermAttribute() {
        GenericValue value = delegator.makeValue("QuoteTermAttribute", null);
        assertEquals("org.ofbiz.order.quote.developed.QuoteTermAttribute", value.getClass().getName());
    }

    public void testMakeValueQuoteType() {
        GenericValue value = delegator.makeValue("QuoteType", null);
        assertEquals("org.ofbiz.order.quote.developed.QuoteType", value.getClass().getName());
    }

    public void testMakeValueQuoteTypeAttr() {
        GenericValue value = delegator.makeValue("QuoteTypeAttr", null);
        assertEquals("org.ofbiz.order.quote.developed.QuoteTypeAttr", value.getClass().getName());
    }

    public void testMakeValueReceipt() {
        GenericValue value = delegator.makeValue("Receipt", null);
        assertEquals("org.ofbiz.servicemgnt.receipt.developed.Receipt", value.getClass().getName());
    }

    public void testMakeValueReceiptContentAssoc() {
        GenericValue value = delegator.makeValue("ReceiptContentAssoc", null);
        assertEquals("org.ofbiz.servicemgnt.receipt.developed.ReceiptContentAssoc", value.getClass().getName());
    }

    public void testMakeValueReceiptStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "RSTS"));
        assertEquals("org.ofbiz.servicemgnt.receipt.developed.ReceiptStatus", value.getClass().getName());
    }

    public void testMakeValueReceiptType() {
        GenericValue value = delegator.makeValue("ReceiptType", null);
        assertEquals("org.ofbiz.servicemgnt.receipt.developed.ReceiptType", value.getClass().getName());
    }

    public void testMakeValueReconcileAccEntry() {
        GenericValue value = delegator.makeValue("ReconcileAccEntry", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.ReconcileAccEntry", value.getClass().getName());
    }

    public void testMakeValueRecurrenceInfo() {
        GenericValue value = delegator.makeValue("RecurrenceInfo", null);
        assertEquals("org.ofbiz.common.schedule.developed.RecurrenceInfo", value.getClass().getName());
    }

    public void testMakeValueRecurrenceRule() {
        GenericValue value = delegator.makeValue("RecurrenceRule", null);
        assertEquals("org.ofbiz.common.schedule.developed.RecurrenceRule", value.getClass().getName());
    }

    public void testMakeValueRejectionReason() {
        GenericValue value = delegator.makeValue("RejectionReason", null);
        assertEquals("org.ofbiz.shipment.receipt.developed.RejectionReason", value.getClass().getName());
    }

    public void testMakeValueRequirement() {
        GenericValue value = delegator.makeValue("Requirement", null);
        assertEquals("org.ofbiz.order.requirement.developed.Requirement", value.getClass().getName());
    }

    public void testMakeValueRequirementCustRequest() {
        GenericValue value = delegator.makeValue("RequirementCustRequest", null);
        assertEquals("org.ofbiz.order.requirement.developed.RequirementCustRequest", value.getClass().getName());
    }

    public void testMakeValueRequirementRole() {
        GenericValue value = delegator.makeValue("RequirementRole", null);
        assertEquals("org.ofbiz.order.requirement.developed.RequirementRole", value.getClass().getName());
    }

    public void testMakeValueRequirementStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "REQUIREMENT_STATUS"));
        assertEquals("org.ofbiz.order.requirement.developed.RequirementStatus", value.getClass().getName());
    }

    public void testMakeValueRequirementStockEventPlan() {
        GenericValue value = delegator.makeValue("RequirementStockEventPlan", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.RequirementStockEventPlan", value.getClass().getName());
    }

    public void testRequirementStockEventPlanSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("RequirementStockEventPlan", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueRequirementType() {
        GenericValue value = delegator.makeValue("RequirementType", null);
        assertEquals("org.ofbiz.order.requirement.developed.RequirementType", value.getClass().getName());
    }

    public void testMakeValueResourceCost() {
        GenericValue value = delegator.makeValue("ResourceCost", null);
        assertEquals("org.ofbiz.manufacturing.cost.developed.ResourceCost", value.getClass().getName());
    }

    public void testMakeValueResourcePlanning() {
        GenericValue value = delegator.makeValue("ResourcePlanning", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.ResourcePlanning", value.getClass().getName());
    }

    public void testResourcePlanningSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ResourcePlanning", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.manufacturing.planning.developed.Planning", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueResourcePlanningPeriod() {
        GenericValue value = delegator.makeValue("ResourcePlanningPeriod", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.ResourcePlanningPeriod", value.getClass().getName());
    }

    public void testMakeValueResourceUsage() {
        GenericValue value = delegator.makeValue("ResourceUsage", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.ResourceUsage", value.getClass().getName());
    }

    public void testMakeValueReturnHeader() {
        GenericValue value = delegator.makeValue("ReturnHeader", null);
        assertEquals("org.ofbiz.order.orderReturn.developed.ReturnHeader", value.getClass().getName());
    }

    public void testMakeValueReturnHeaderType() {
        GenericValue value = delegator.makeValue("ReturnHeaderType", null);
        assertEquals("org.ofbiz.order.orderReturn.developed.ReturnHeaderType", value.getClass().getName());
    }

    public void testMakeValueReturnItem() {
        GenericValue value = delegator.makeValue("ReturnItem", null);
        assertEquals("org.ofbiz.order.orderReturn.developed.ReturnItem", value.getClass().getName());
    }

    public void testMakeValueReturnItemResponse() {
        GenericValue value = delegator.makeValue("ReturnItemResponse", null);
        assertEquals("org.ofbiz.order.orderReturn.developed.ReturnItemResponse", value.getClass().getName());
    }

    public void testMakeValueReturnReason() {
        GenericValue value = delegator.makeValue("ReturnReason", null);
        assertEquals("org.ofbiz.order.orderReturn.developed.ReturnReason", value.getClass().getName());
    }

    public void testMakeValueReturnStatus() {
        GenericValue value = delegator.makeValue("ReturnStatus", null);
        assertEquals("org.ofbiz.order.orderReturn.developed.ReturnStatus", value.getClass().getName());
    }

    public void testReturnStatusSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ReturnStatus", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.common.status.developed.StatusItem", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueReturnStockEvent() {
        GenericValue value = delegator.makeValue("ReturnStockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.ReturnStockEvent", value.getClass().getName());
    }

    public void testReturnStockEventSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ReturnStockEvent", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueReturnStockEventPlanned() {
        GenericValue value = delegator.makeValue("ReturnStockEventPlanned", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.ReturnStockEventPlanned", value.getClass().getName());
    }

    public void testReturnStockEventPlannedSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ReturnStockEventPlanned", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueReturnType() {
        GenericValue value = delegator.makeValue("ReturnType", null);
        assertEquals("org.ofbiz.order.orderReturn.developed.ReturnType", value.getClass().getName());
    }

    public void testMakeValueRoleType() {
        GenericValue value = delegator.makeValue("RoleType", null);
        assertEquals("org.ofbiz.party.party.developed.RoleType", value.getClass().getName());
    }

    public void testMakeValueRoleTypeAttr() {
        GenericValue value = delegator.makeValue("RoleTypeAttr", null);
        assertEquals("org.ofbiz.party.party.developed.RoleTypeAttr", value.getClass().getName());
    }

    public void testMakeValueRoleTypeRollup() {
        GenericValue value = delegator.makeValue("RoleTypeRollup", null);
        assertEquals("org.ofbiz.party.party.developed.RoleTypeRollup", value.getClass().getName());
    }

    public void testMakeValueRouting() {
        GenericValue value = delegator.makeValue("Routing", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.Routing", value.getClass().getName());
    }

    public void testMakeValueRoutingComposition() {
        GenericValue value = delegator.makeValue("RoutingComposition", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.RoutingComposition", value.getClass().getName());
    }

    public void testMakeValueRunCompoStkEvPlan() {
        GenericValue value = delegator.makeValue("RunCompoStkEvPlan", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.RunCompoStkEvPlan", value.getClass().getName());
    }

    public void testRunCompoStkEvPlanSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("RunCompoStkEvPlan", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueRunCompoStockEvent() {
        GenericValue value = delegator.makeValue("RunCompoStockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.RunCompoStockEvent", value.getClass().getName());
    }

    public void testRunCompoStockEventSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("RunCompoStockEvent", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueRunComponent() {
        GenericValue value = delegator.makeValue("RunComponent", null);
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.RunComponent", value.getClass().getName());
    }

    public void testMakeValueRunComponentStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "RCS"));
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.RunComponentStatus", value.getClass().getName());
    }

    public void testMakeValueRunStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "PRS"));
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.RunStatus", value.getClass().getName());
    }

    public void testMakeValueRunStockEvent() {
        GenericValue value = delegator.makeValue("RunStockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.RunStockEvent", value.getClass().getName());
    }

    public void testRunStockEventSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("RunStockEvent", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueRunStockEventPlanned() {
        GenericValue value = delegator.makeValue("RunStockEventPlanned", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.RunStockEventPlanned", value.getClass().getName());
    }

    public void testRunStockEventPlannedSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("RunStockEventPlanned", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueRuntimeData() {
        GenericValue value = delegator.makeValue("RuntimeData", null);
        assertEquals("org.ofbiz.common.schedule.developed.RuntimeData", value.getClass().getName());
    }

    public void testMakeValueSalesInvoice() {
        GenericValue value = delegator.makeValue("Invoice", UtilMisc.toMap("invoiceTypeId", "SALESINVOICE"));
        assertEquals("org.ofbiz.accounting.invoice.developed.SalesInvoice", value.getClass().getName());
    }

    public void testMakeValueSeasonality() {
        GenericValue value = delegator.makeValue("Seasonality", null);
        assertEquals("org.ofbiz.manufacturing.planning.developed.Seasonality", value.getClass().getName());
    }

    public void testMakeValueSerialNumCheckM() {
        GenericValue value = delegator.makeValue("SerialNumCheckM", null);
        assertEquals("org.ofbiz.quality.checkMeasure.developed.SerialNumCheckM", value.getClass().getName());
    }

    public void testMakeValueServiceProduct() {
        GenericValue value = delegator.makeValue("ServiceProduct", null);
        assertEquals("org.ofbiz.servicemgnt.service.developed.ServiceProduct", value.getClass().getName());
    }

    public void testServiceProductSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ServiceProduct", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.product.product.developed.Product", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueServiceRun() {
        GenericValue value = delegator.makeValue("WRun", UtilMisc.toMap("discriminator", "SR"));
        assertEquals("org.ofbiz.servicemgnt.service.developed.ServiceRun", value.getClass().getName());
    }

    public void testMakeValueServiceRunContentAssoc() {
        GenericValue value = delegator.makeValue("ServiceRunContentAssoc", null);
        assertEquals("org.ofbiz.servicemgnt.service.developed.ServiceRunContentAssoc", value.getClass().getName());
    }

    public void testMakeValueServiceRunContentPublication() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "SRCP"));
        assertEquals("org.ofbiz.servicemgnt.service.developed.ServiceRunContentPublication", value.getClass().getName());
    }

    public void testMakeValueServiceRunContentType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "SRCT"));
        assertEquals("org.ofbiz.servicemgnt.service.developed.ServiceRunContentType", value.getClass().getName());
    }

    public void testMakeValueServiceValidIntegRule() {
        GenericValue value = delegator.makeValue("CustomMethod", UtilMisc.toMap("customMethodTypeId", "INTEGRULEVALID"));
        assertEquals("org.ofbiz.accounting.accintegration.developed.ServiceValidIntegRule", value.getClass().getName());
    }

    public void testMakeValueShiftType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "SHIFTTYPE"));
        assertEquals("org.ofbiz.accounting.payment.developed.ShiftType", value.getClass().getName());
    }

    public void testMakeValueShipment() {
        GenericValue value = delegator.makeValue("Shipment", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.Shipment", value.getClass().getName());
    }

    public void testMakeValueShipmentAttribute() {
        GenericValue value = delegator.makeValue("ShipmentAttribute", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentAttribute", value.getClass().getName());
    }

    public void testMakeValueShipmentBox() {
        GenericValue value = delegator.makeValue("ShipmentBox", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentBox", value.getClass().getName());
    }

    public void testMakeValueShipmentBoxEvent() {
        GenericValue value = delegator.makeValue("ShipmentBoxEvent", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentBoxEvent", value.getClass().getName());
    }

    public void testMakeValueShipmentBoxType() {
        GenericValue value = delegator.makeValue("ShipmentBoxType", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentBoxType", value.getClass().getName());
    }

    public void testMakeValueShipmentContactMech() {
        GenericValue value = delegator.makeValue("ShipmentContactMech", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentContactMech", value.getClass().getName());
    }

    public void testMakeValueShipmentContactMechType() {
        GenericValue value = delegator.makeValue("ShipmentContactMechType", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentContactMechType", value.getClass().getName());
    }

    public void testMakeValueShipmentCostEstimate() {
        GenericValue value = delegator.makeValue("ShipmentCostEstimate", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentCostEstimate", value.getClass().getName());
    }

    public void testMakeValueShipmentDocument() {
        GenericValue value = delegator.makeValue("ShipmentDocument", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentDocument", value.getClass().getName());
    }

    public void testMakeValueShipmentItem() {
        GenericValue value = delegator.makeValue("ShipmentItem", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentItem", value.getClass().getName());
    }

    public void testMakeValueShipmentItemBilling() {
        GenericValue value = delegator.makeValue("ShipmentItemBilling", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentItemBilling", value.getClass().getName());
    }

    public void testMakeValueShipmentMethodType() {
        GenericValue value = delegator.makeValue("ShipmentMethodType", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentMethodType", value.getClass().getName());
    }

    public void testMakeValueShipmentPackage() {
        GenericValue value = delegator.makeValue("ShipmentPackage", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentPackage", value.getClass().getName());
    }

    public void testMakeValueShipmentPackageContent() {
        GenericValue value = delegator.makeValue("ShipmentPackageContent", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentPackageContent", value.getClass().getName());
    }

    public void testMakeValueShipmentPackageRouteSeg() {
        GenericValue value = delegator.makeValue("ShipmentPackageRouteSeg", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentPackageRouteSeg", value.getClass().getName());
    }

    public void testMakeValueShipmentReceipt() {
        GenericValue value = delegator.makeValue("ShipmentReceipt", null);
        assertEquals("org.ofbiz.shipment.receipt.developed.ShipmentReceipt", value.getClass().getName());
    }

    public void testMakeValueShipmentRouteSegment() {
        GenericValue value = delegator.makeValue("ShipmentRouteSegment", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentRouteSegment", value.getClass().getName());
    }

    public void testMakeValueShipmentStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "SHIPMENTSTATUS"));
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentStatus", value.getClass().getName());
    }

    public void testMakeValueShipmentStockEvent() {
        GenericValue value = delegator.makeValue("ShipmentStockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.ShipmentStockEvent", value.getClass().getName());
    }

    public void testShipmentStockEventSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ShipmentStockEvent", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueShipmentStockItem() {
        GenericValue value = delegator.makeValue("ShipmentStockItem", null);
        assertEquals("org.ofbiz.facility.location.developed.ShipmentStockItem", value.getClass().getName());
    }

    public void testShipmentStockItemSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("ShipmentStockItem", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.location.developed.StockItem", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueShipmentType() {
        GenericValue value = delegator.makeValue("ShipmentType", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentType", value.getClass().getName());
    }

    public void testMakeValueShipmentTypeAttr() {
        GenericValue value = delegator.makeValue("ShipmentTypeAttr", null);
        assertEquals("org.ofbiz.shipment.shipment.developed.ShipmentTypeAttr", value.getClass().getName());
    }

    public void testMakeValueSimpleSalesTaxLookup() {
        GenericValue value = delegator.makeValue("SimpleSalesTaxLookup", null);
        assertEquals("org.ofbiz.order.order.developed.SimpleSalesTaxLookup", value.getClass().getName());
    }

    public void testMakeValueSkill() {
        GenericValue value = delegator.makeValue("Skill", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.Skill", value.getClass().getName());
    }

    public void testSkillSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("Skill", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.manufacturing.techdata.developed.TechDataResource", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueStatusFacility() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "SFAC"));
        assertEquals("org.ofbiz.facility.location.developed.StatusFacility", value.getClass().getName());
    }

    public void testMakeValueStatusInventory() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "STATUSINVENTORY"));
        assertEquals("org.ofbiz.facility.inventory.developed.StatusInventory", value.getClass().getName());
    }

    public void testMakeValueStatusInventoryItem() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "STATUSINVENTORYITEM"));
        assertEquals("org.ofbiz.facility.inventory.developed.StatusInventoryItem", value.getClass().getName());
    }

    public void testMakeValueStatusInvoice() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "STATUSINVOICE"));
        assertEquals("org.ofbiz.accounting.invoice.developed.StatusInvoice", value.getClass().getName());
    }

    public void testMakeValueStatusItem() {
        GenericValue value = delegator.makeValue("StatusItem", null);
        assertEquals("org.ofbiz.common.status.developed.StatusItem", value.getClass().getName());
    }

    public void testMakeValueStatusOrder() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "STATUSORDER"));
        assertEquals("org.ofbiz.order.order.developed.StatusOrder", value.getClass().getName());
    }

    public void testMakeValueStatusPickingList() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "SPL"));
        assertEquals("org.ofbiz.facility.picking.developed.StatusPickingList", value.getClass().getName());
    }

    public void testMakeValueStatusStockEventPanned() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "STEVTP"));
        assertEquals("org.ofbiz.facility.stockevent.developed.StatusStockEventPanned", value.getClass().getName());
    }

    public void testMakeValueStatusStockItem() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "SSITM"));
        assertEquals("org.ofbiz.facility.location.developed.StatusStockItem", value.getClass().getName());
    }

    public void testMakeValueStockEvent() {
        GenericValue value = delegator.makeValue("StockEvent", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEvent", value.getClass().getName());
    }

    public void testMakeValueStockEventPlanned() {
        GenericValue value = delegator.makeValue("StockEventPlanned", null);
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventPlanned", value.getClass().getName());
    }

    public void testMakeValueStockEventType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "STOCKEVENTTYPE"));
        assertEquals("org.ofbiz.facility.stockevent.developed.StockEventType", value.getClass().getName());
    }

    public void testMakeValueStockItem() {
        GenericValue value = delegator.makeValue("StockItem", null);
        assertEquals("org.ofbiz.facility.location.developed.StockItem", value.getClass().getName());
    }

    public void testMakeValueStockMgntOutMethod() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "STOCKMGNTOUTMETHOD"));
        assertEquals("org.ofbiz.facility.location.developed.StockMgntOutMethod", value.getClass().getName());
    }

    public void testMakeValueTask() {
        GenericValue value = delegator.makeValue("Task", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.Task", value.getClass().getName());
    }

    public void testMakeValueTaskFulfilSubctr() {
        GenericValue value = delegator.makeValue("TaskFulfilSubctr", null);
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.TaskFulfilSubctr", value.getClass().getName());
    }

    public void testTaskFulfilSubctrSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("TaskFulfilSubctr", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.TaskFulfilment", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueTaskFulfilment() {
        GenericValue value = delegator.makeValue("TaskFulfilment", null);
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.TaskFulfilment", value.getClass().getName());
    }

    public void testMakeValueTaskResource() {
        GenericValue value = delegator.makeValue("TaskResource", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.TaskResource", value.getClass().getName());
    }

    public void testMakeValueTaskResourceFulfil() {
        GenericValue value = delegator.makeValue("TaskResourceFulfil", null);
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.TaskResourceFulfil", value.getClass().getName());
    }

    public void testMakeValueTaskSubcontract() {
        GenericValue value = delegator.makeValue("TaskSubcontract", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.TaskSubcontract", value.getClass().getName());
    }

    public void testTaskSubcontractSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("TaskSubcontract", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.manufacturing.techdata.developed.Task", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueTaskType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "TT"));
        assertEquals("org.ofbiz.manufacturing.techdata.developed.TaskType", value.getClass().getName());
    }

    public void testMakeValueTaxAuthority() {
        GenericValue value = delegator.makeValue("TaxAuthority", null);
        assertEquals("org.ofbiz.accounting.tax.developed.TaxAuthority", value.getClass().getName());
    }

    public void testMakeValueTaxAuthorityAssoc() {
        GenericValue value = delegator.makeValue("TaxAuthorityAssoc", null);
        assertEquals("org.ofbiz.accounting.tax.developed.TaxAuthorityAssoc", value.getClass().getName());
    }

    public void testMakeValueTaxAuthorityAssocType() {
        GenericValue value = delegator.makeValue("TaxAuthorityAssocType", null);
        assertEquals("org.ofbiz.accounting.tax.developed.TaxAuthorityAssocType", value.getClass().getName());
    }

    public void testMakeValueTaxAuthorityCategory() {
        GenericValue value = delegator.makeValue("TaxAuthorityCategory", null);
        assertEquals("org.ofbiz.accounting.tax.developed.TaxAuthorityCategory", value.getClass().getName());
    }

    public void testMakeValueTaxAuthorityRateProduct() {
        GenericValue value = delegator.makeValue("TaxAuthorityRateProduct", null);
        assertEquals("org.ofbiz.accounting.tax.developed.TaxAuthorityRateProduct", value.getClass().getName());
    }

    public void testMakeValueTaxAuthorityRateType() {
        GenericValue value = delegator.makeValue("TaxAuthorityRateType", null);
        assertEquals("org.ofbiz.accounting.tax.developed.TaxAuthorityRateType", value.getClass().getName());
    }

    public void testMakeValueTechDataCalendar() {
        GenericValue value = delegator.makeValue("TechDataCalendar", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.TechDataCalendar", value.getClass().getName());
    }

    public void testMakeValueTechDataResource() {
        GenericValue value = delegator.makeValue("TechDataResource", null);
        assertEquals("org.ofbiz.manufacturing.techdata.developed.TechDataResource", value.getClass().getName());
    }

    public void testMakeValueTelecomNumber() {
        GenericValue value = delegator.makeValue("TelecomNumber", null);
        assertEquals("org.ofbiz.party.contact.developed.TelecomNumber", value.getClass().getName());
    }

    public void testTelecomNumberSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("TelecomNumber", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.party.contact.developed.ContactMech", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueTermType() {
        GenericValue value = delegator.makeValue("TermType", null);
        assertEquals("org.ofbiz.party.agreement.developed.TermType", value.getClass().getName());
    }

    public void testMakeValueTicket() {
        GenericValue value = delegator.makeValue("Ticket", null);
        assertEquals("org.ofbiz.servicemgnt.ticket.developed.Ticket", value.getClass().getName());
    }

    public void testTicketSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("Ticket", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.servicemgnt.request.developed.CustRequest", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueTicketAssoc() {
        GenericValue value = delegator.makeValue("TicketAssoc", null);
        assertEquals("org.ofbiz.servicemgnt.ticket.developed.TicketAssoc", value.getClass().getName());
    }

    public void testMakeValueTicketStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "CRQST"));
        assertEquals("org.ofbiz.servicemgnt.ticket.developed.TicketStatus", value.getClass().getName());
    }

    public void testMakeValueTimeReceiptReport() {
        GenericValue value = delegator.makeValue("TimeReceiptReport", null);
        assertEquals("org.ofbiz.servicemgnt.receipt.developed.TimeReceiptReport", value.getClass().getName());
    }

    public void testMakeValueTimeReceiptReportStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "TRRS"));
        assertEquals("org.ofbiz.servicemgnt.receipt.developed.TimeReceiptReportStatus", value.getClass().getName());
    }

    public void testMakeValueTimeSheet() {
        GenericValue value = delegator.makeValue("TimeSheet", null);
        assertEquals("org.ofbiz.servicemgnt.timeSheet.developed.TimeSheet", value.getClass().getName());
    }

    public void testMakeValueTimeSheetBilling() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "TSB"));
        assertEquals("org.ofbiz.servicemgnt.timeSheet.developed.TimeSheetBilling", value.getClass().getName());
    }

    public void testMakeValueTimeSheetStatus() {
        GenericValue value = delegator.makeValue("StatusItem", UtilMisc.toMap("statusTypeId", "TSS"));
        assertEquals("org.ofbiz.servicemgnt.timeSheet.developed.TimeSheetStatus", value.getClass().getName());
    }

    public void testMakeValueTotalAmount() {
        GenericValue value = delegator.makeValue("TotalAmount", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.TotalAmount", value.getClass().getName());
    }

    public void testMakeValueTotalAmountEntries() {
        GenericValue value = delegator.makeValue("TotalAmountEntries", null);
        assertEquals("org.ofbiz.accounting.transaction.developed.TotalAmountEntries", value.getClass().getName());
    }

    public void testMakeValueTransfertList() {
        GenericValue value = delegator.makeValue("TransfertList", null);
        assertEquals("org.ofbiz.facility.picking.developed.TransfertList", value.getClass().getName());
    }

    public void testTransfertListSuperEntity() {
        PersistantObject value = (PersistantObject) delegator.makeValue("TransfertList", null);
        assertNotNull(value.getSuperEntity());
        assertEquals("org.ofbiz.facility.picking.developed.PickingList", value.getSuperEntity().getClass().getName());
    }

    public void testMakeValueTransfertListModel() {
        GenericValue value = delegator.makeValue("TransfertListModel", null);
        assertEquals("org.ofbiz.facility.picking.developed.TransfertListModel", value.getClass().getName());
    }

    public void testMakeValueTransfertUnitModel() {
        GenericValue value = delegator.makeValue("TransfertUnitModel", null);
        assertEquals("org.ofbiz.facility.picking.developed.TransfertUnitModel", value.getClass().getName());
    }

    public void testMakeValueTwoToOne() {
        GenericValue value = delegator.makeValue("TwoToOne", null);
        assertEquals("org.ofbiz.accounting.accintegration.developed.TwoToOne", value.getClass().getName());
    }

    public void testMakeValueUnitCostMngType() {
        GenericValue value = delegator.makeValue("Enumeration", UtilMisc.toMap("enumTypeId", "UCMT"));
        assertEquals("org.ofbiz.product.store.developed.UnitCostMngType", value.getClass().getName());
    }

    public void testMakeValueUom() {
        GenericValue value = delegator.makeValue("Uom", null);
        assertEquals("org.ofbiz.common.uom.developed.Uom", value.getClass().getName());
    }

    public void testMakeValueUomConversion() {
        GenericValue value = delegator.makeValue("UomConversion", null);
        assertEquals("org.ofbiz.common.uom.developed.UomConversion", value.getClass().getName());
    }

    public void testMakeValueUomConversionDated() {
        GenericValue value = delegator.makeValue("UomConversionDated", null);
        assertEquals("org.ofbiz.common.uom.developed.UomConversionDated", value.getClass().getName());
    }

    public void testMakeValueUomType() {
        GenericValue value = delegator.makeValue("UomType", null);
        assertEquals("org.ofbiz.common.uom.developed.UomType", value.getClass().getName());
    }

    public void testMakeValueUserLogin() {
        GenericValue value = delegator.makeValue("UserLogin", null);
        assertEquals("org.ofbiz.party.party.developed.UserLogin", value.getClass().getName());
    }

    public void testMakeValueWRun() {
        GenericValue value = delegator.makeValue("WRun", null);
        assertEquals("org.ofbiz.manufacturing.jobshopmgt.developed.WRun", value.getClass().getName());
    }

    public void testMakeValueWebPage() {
        GenericValue value = delegator.makeValue("WebPage", null);
        assertEquals("org.ofbiz.webapp.website.developed.WebPage", value.getClass().getName());
    }

    public void testMakeValueWebSite() {
        GenericValue value = delegator.makeValue("WebSite", null);
        assertEquals("org.ofbiz.webapp.website.developed.WebSite", value.getClass().getName());
    }

    public void testMakeValueWebSiteRole() {
        GenericValue value = delegator.makeValue("WebSiteRole", null);
        assertEquals("org.ofbiz.webapp.website.developed.WebSiteRole", value.getClass().getName());
    }

    public void testMakeValuepaymentApplication() {
        GenericValue value = delegator.makeValue("paymentApplication", null);
        assertEquals("org.ofbiz.accounting.payment.developed.paymentApplication", value.getClass().getName());
    }

}
