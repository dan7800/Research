/*
 * Copyright (c) 2006 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: PortalDomainControllerImpl.java,v 1.23 2009/07/18 05:04:20 bastafidli Exp $
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License. 
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */

package org.opensubsystems.portal.logic.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.error.OSSInternalErrorException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.StringUtils;
import org.opensubsystems.core.util.ThreeIntStruct;
import org.opensubsystems.portal.data.PortalDomain;
import org.opensubsystems.portal.data.PossibleAnswer;
import org.opensubsystems.portal.data.Question;
import org.opensubsystems.portal.data.Survey;
import org.opensubsystems.portal.logic.PortalDomainController;
import org.opensubsystems.portal.logic.PortalSurveyController;
import org.opensubsystems.portal.persist.DomainSurveyFactory;
import org.opensubsystems.portal.persist.PortalDomainFactory;
import org.opensubsystems.security.data.Domain;
import org.opensubsystems.security.logic.impl.DomainControllerImpl;
import org.opensubsystems.security.persist.DomainFactory;

/**
 * Implementation of PortalDomainController interface to manage portal domain mapping records.
 *
 * @ejb.bean type="Stateless"
 *           name="PortalDomainController"
 *           jndi-name="org.opensubsystems.portal.logic.PortalDomainControllerRemote"
 *           local-jndi-name="org.opensubsystems.portal.logic.PortalDomainController"
 * @ejb.interface 
 *   local-extends="javax.ejb.EJBLocalObject, org.opensubsystems.portal.logic.PortalDomainController"
 *   extends="javax.ejb.EJBObject, org.opensubsystems.portal.logic.PortalDomainController"
 * @ejb.home local-extends="javax.ejb.EJBLocalHome"
 *           extends="javax.ejb.EJBHome"
 *           
 * @ejb.ejb-ref ejb-name="SurveyController"
 *              ref-name="org.opensubsystems.portal.logic.SurveyController"
 *
 * @jonas.bean ejb-name="PortalDomainController"
 *             jndi-name="org.opensubsystems.portal.logic.PortalDomainControllerRemote"
 *
 * @jonas.ejb-ref ejb-ref-name="org.opensubsystems.portal.logic.SurveyController"
 *                jndi-name="org.opensubsystems.portal.logic.SurveyControllerRemote"
 *
 * @jboss.ejb-ref-jndi ref-name="org.opensubsystems.portal.logic.SurveyController"
 *                     jndi-name="org.opensubsystems.portal.logic.SurveyController"
 * @jboss.ejb-local-ref ref-name="org.opensubsystems.portal.logic.SurveyController"
 *                     jndi-name="org.opensubsystems.portal.logic.SurveyController"
 *
 * @weblogic.ejb-reference-description 
 *               ejb-ref-name="org.opensubsystems.portal.logic.SurveyController"
 *               jndi-name="org.opensubsystems.portal.logic.SurveyController"
 * @weblogic.ejb-local-reference-description 
 *               ejb-ref-name="org.opensubsystems.portal.logic.SurveyController"
 *               jndi-name="org.opensubsystems.portal.logic.SurveyController"
 *
 * @version $Id: PortalDomainControllerImpl.java,v 1.23 2009/07/18 05:04:20 bastafidli Exp $
 * @author Julian Legeny
 * @code.reviewer TODO: Review this code
 * @code.reviewed
 */
public class PortalDomainControllerImpl extends DomainControllerImpl
                                        implements PortalDomainController
{
   // Cached values ////////////////////////////////////////////////////////////   

   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -6953467260864863044L;

   /**
    * Factory to use to execute persistence operations.
    */
   protected DomainFactory m_domainFactory;

   /**
    * Factory to use to execute persistence operations.
    */
   protected DomainSurveyFactory m_domainSurveyFactory;

   /**
    * Factory to use to execute persistence operations.
    */
   protected PortalDomainFactory m_portalDomainFactory;
   
   /**
    * Survey controller used to retrieve assigned surveys.
    */
   protected PortalSurveyController m_surveyControl;

   /**
    * Default constructor.
    */
   public PortalDomainControllerImpl(
   ) 
   {
      super();
      
      // Do not cache anything here since if this controller is run as a stateless
      // session bean the referenced objects may not be ready
      m_domainSurveyFactory = null;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public Domain getDomainWithAssociatedData(
      int iDomainId
   ) throws OSSException
   {
      List   lstSurveyList = null;
      Domain domain = null;
      Domain returnData = null;
      
      // get domain dataobject from super 
      domain = super.getDomainWithAssociatedData(iDomainId);
      if (domain != null)
      {
         // get survey list  assigned to the domain
         lstSurveyList = getAssignedSurveys(iDomainId);
         // load here survey list and set up it into the PortalDomain, this list
         // will be send and used on the gui
         returnData = new PortalDomain(domain, DataObject.NEW_ID, 
                                       DataObject.NEW_ID, lstSurveyList);
      }
      
      return returnData;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public Domain create(
      Domain data, 
      String strRoleIds
   ) throws OSSException
   {
      DataObject createdData = null;
      Domain     returnData = null;
      List       lstSurveyList = Collections.EMPTY_LIST;
      int        iDomainId = DataObject.NEW_ID;
      int        iSurveyId = DataObject.NEW_ID;
      Survey     originalSurvey = null;
      Survey     newSurvey = null;

      // call super to create domain data object
      createdData = super.create(data, strRoleIds);

      if ((createdData != null) && (data instanceof PortalDomain))
      {
         iDomainId = ((PortalDomain)data).getId();
         iSurveyId = ((PortalDomain)data).getSurveyId();
         
         if (iSurveyId != DataObject.NEW_ID)
         {
            // Since we are creating new domain and assigned survey belong to 
            // the current domain, we have to copy survey from current 
            // domain into the new created domain
            try
            {
               // get original survey assigned to current domain
               originalSurvey = (Survey)m_surveyControl.get(iSurveyId);
               if (originalSurvey != null)
               {
                  // copy survey into new created domain
                  newSurvey = m_surveyControl.create(new Survey(originalSurvey, 
                                                                iDomainId, false));
                  // list of assigned surveys is max. 1 item
                  lstSurveyList = new ArrayList(1);
                  lstSurveyList.add(newSurvey);
               }
            }
            catch (RemoteException rExc)
            {
               // We cannot propagate this exception otherwise XDoclet would generate 
               // the local interface incorrectly since it would include the declared
               // RemoteException in it (to propagate we would have to declare it)
               throw new OSSInternalErrorException("Remote error occurred", rExc);
            }

            // create domain-survey mapping record if survey ID has to be added
            m_domainSurveyFactory.create(new ThreeIntStruct(iDomainId, newSurvey.getId(), 
                                                            PortalDomain.MAPPING_TYPE_SELFREG));
         }
         returnData = new PortalDomain((Domain)createdData, iSurveyId, 
                                       DataObject.NEW_ID, lstSurveyList); 
      }

      return returnData;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public Domain save(
      Domain data, 
      String strRemoveRoleIds, 
      String strAddRoleIds
   ) throws OSSException
   {
      DataObject updatedData = null;
      Domain     returnData = null;
      List       lstSurveyList = Collections.EMPTY_LIST;
      int        iDomainId = DataObject.NEW_ID;
      int        iSurveyId = DataObject.NEW_ID;
      int        iOriginalSurveyId;
      Survey     originalSurvey = null;

      // call super to save domain data object
      updatedData = super.save(data, strRemoveRoleIds, strAddRoleIds);
      
      if ((updatedData != null) && (data instanceof PortalDomain))
      {
         iDomainId = ((PortalDomain)data).getId();
         iSurveyId = ((PortalDomain)data).getSurveyId();
         iOriginalSurveyId = ((PortalDomain)data).getOriginalSurveyId();
         
         if (iOriginalSurveyId != DataObject.NEW_ID)
         {
            // delete domain-survey mapping record if survey ID has to be removed
            m_domainSurveyFactory.delete(new ThreeIntStruct(iDomainId, iOriginalSurveyId, 
                                                            PortalDomain.MAPPING_TYPE_SELFREG));
         }
         if (iSurveyId != DataObject.NEW_ID)
         {
            if (iDomainId != CallContext.getInstance().getCurrentDomainId())
            {
               // Since we are updating another than current domain and 
               // assigned survey doesn't belong to the updated domain, 
               // we have to copy survey from current domain into the 
               // updated domain
               try
               {
                  // get original survey assigned to current domain
                  originalSurvey = (Survey)m_surveyControl.get(iSurveyId);
                  if (originalSurvey != null)
                  {
                     // copy survey into new created domain
                     m_surveyControl.create(new Survey(originalSurvey, 
                                                       iDomainId, false));
                  }
               }
               catch (RemoteException rExc)
               {
                  // We cannot propagate this exception otherwise XDoclet would generate 
                  // the local interface incorrectly since it would include the declared
                  // RemoteException in it (to propagate we would have to declare it)
                  throw new OSSInternalErrorException("Remote error occurred", rExc);
               }
            }

            // create domain-survey mapping record if new survey ID has to be added
            m_domainSurveyFactory.create(new ThreeIntStruct(iDomainId, iSurveyId, 
                                                            PortalDomain.MAPPING_TYPE_SELFREG));
         }
         // get survey list assigned to the domain
         lstSurveyList = getAssignedSurveys(iDomainId);
         
         returnData = new PortalDomain((Domain)updatedData, iSurveyId, 
                                       iSurveyId, lstSurveyList);
      }

      return returnData;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getAssignedSurveys(
      int iDomainId
   ) throws OSSException
   {
      List lstSurveyList = Collections.EMPTY_LIST;
      if (iDomainId != DataObject.NEW_ID)
      {
         try
         {
            // don't pass domain name prefix becouse this method is called only 
            // when domain id != DataObject.NEW_ID 
            lstSurveyList = m_surveyControl.getSelfregistrationSurveys(
                                 Integer.toString(iDomainId), "");
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }
      
      return lstSurveyList;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getAssignedSurveys(
      String strValue,
      boolean bIsPrefix
   ) throws OSSException
   {
      List lstSurveyList = Collections.EMPTY_LIST;
      try
      {
         if (bIsPrefix)
         {
            // pass here domain name prefix (when domain id == DataObject.NEW_ID 
            // for nonpublic domains) 
            lstSurveyList = m_surveyControl.getSelfregistrationSurveys(
                                 DataObject.NEW_ID_STR, strValue);
         }
         else
         {
            // pass here domain IDs 
            lstSurveyList = m_surveyControl.getSelfregistrationSurveys(
                               strValue, "");
         }
      }
      catch (RemoteException rExc)
      {
         // We cannot propagate this exception otherwise XDoclet would generate 
         // the local interface incorrectly since it would include the declared
         // RemoteException in it (to propagate we would have to declare it)
         throw new OSSInternalErrorException("Remote error occurred", rExc);
      }
      
      return lstSurveyList;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public Domain checkAnswers(
      int  iDomainId,
      String strNamePrefix,
      List lstAnsweredData
   ) throws OSSException
   {
      Domain returnDomain = null;
      // get string value of retrieved correctly answered domain ID 
      String strDomainId = checkCurrentAnswer(iDomainId, strNamePrefix, lstAnsweredData);
      
      if (strDomainId.length() > 0)
      {
         // now if AND ONLY IF the survey is correct, we need to retrieve the domain 
         returnDomain = (Domain)((m_domainFactory.getMultipleDomains(strDomainId)).get(0));
      }
      
      return returnDomain;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List checkAnswers(
      String  strDomainIDs,
      String strNamePrefix,
      List lstAnsweredData
   ) throws OSSException
   {
      List returnDomains = null;
      String strCurrentID;
      StringBuffer sbAnsweredDomainIDs = new StringBuffer();
      int[] arrDomainIDs = null;
      int   iIndex;

      // Parse domain IDs from string into int array and then we will iterate
      // through whole array and check answer for particular current domain.
      // All correctly answered domains will be retrieved from database at once.
      
      arrDomainIDs = StringUtils.parseStringToIntArray(strDomainIDs, ",");
      if ((arrDomainIDs != null) && (arrDomainIDs.length > 0))
      {
         for (iIndex = 0; iIndex < arrDomainIDs.length; iIndex++)
         {
            strCurrentID = checkCurrentAnswer(
                              arrDomainIDs[iIndex], strNamePrefix, lstAnsweredData);
            if (strCurrentID.length() > 0)
            {
               if (sbAnsweredDomainIDs.length() > 0)
               {
                  sbAnsweredDomainIDs.append(",");
               }
               sbAnsweredDomainIDs.append(strCurrentID);
            }
         }
      }
      
      if (sbAnsweredDomainIDs.length() > 0)
      {
         // now if AND ONLY IF the surveys are correct, we need to retrieve the domains 
         returnDomains = m_domainFactory.getMultipleDomains(sbAnsweredDomainIDs.toString());
      }
      
      return returnDomains;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public List getSelfregistrationDomains(
      String strIDs,
      String strName
   ) throws OSSException
   {
      // this method doesn't check access rights because it is used
      // for self registered domains
      List lstReturn = new ArrayList();
      List lstDomains = Collections.EMPTY_LIST;
      Domain domain = null;

      if (strIDs.indexOf(DataObject.NEW_ID_STR) != -1)
      {
         // we are requesting nonpublic domain with particular name
         lstDomains = m_portalDomainFactory.getNonPublicDomains(strName);

         if (lstDomains != null && !lstDomains.isEmpty())
         {
            Iterator itDomain = null;
            for (itDomain = lstDomains.iterator(); itDomain.hasNext();)
            {
               // Check retrieved domain (this check is called only when domain id = -1
               // and for super call is checked domain condition separately)

               // check actual domain from the list and add it into the return list
               // in case when check passes ok
               domain = checkDomainCondition((Domain)itDomain.next(), strName);
               if (domain != null)
               {
                  lstReturn.add(domain);
               }
            }
         }
      }
      else
      {
         lstReturn.addAll(super.getSelfregistrationDomains(strIDs, strName));
      }

      return lstReturn;
   }

   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public void constructor(
   ) throws OSSException
   {
      super.constructor();
      m_domainFactory = (DomainFactory)DataFactoryManager.getInstance(
                                  DomainFactory.class);
      m_domainSurveyFactory = (DomainSurveyFactory)DataFactoryManager.getInstance(
                                  DomainSurveyFactory.class);
      m_portalDomainFactory = (PortalDomainFactory)DataFactoryManager.getInstance(
                                  PortalDomainFactory.class);
      m_surveyControl = (PortalSurveyController)ControllerManager.getInstance(
                                  PortalSurveyController.class);
   }

   /**
    * Method to check current answers for particular domain and if answers are correct,
    * then return ID of correctly answered domain. 
    * 
    * @param iDomainId - ID of the domain the surveys are assigned to
    * @param strNamePrefix - prefix of the domain name
    * @param lstAnsweredData - list of answered data (string value of the answer)
    * @return iDomainId - string representation of ID of the correctly answered Domain, 
    *                     empty string otherwise (if survey was answered incorrectly)
    * @throws OSSException - error occurred
    */
   protected String checkCurrentAnswer(
      int  iDomainId,
      String strNamePrefix,
      List lstAnsweredData
   ) throws OSSException
   {
      boolean bAllMandatoryAnswered = true;
      boolean bAllRequestedAnswered = false;
      boolean bAllCorrectlyAnswered = false;
      String  returnID = "";

      if (iDomainId != DataObject.NEW_ID)
      {
         Survey survey = null;
         List lstQuestions = null;
         List lstPossibleAnswers = null;

         int iCorrectMandatory = 0;
         int iCorrectOptional = 0;
         int iAnsweredMandatory = 0;
         int iAnsweredOptional = 0;
         int iTotalMandatory = 0;
         int iTotalOptional = 0;
         
         try
         {
            // check here again for selfregistration surveys and also
            // check if all requested questions were correctly answered

            // get survey with questions
            survey = (Survey)((List)m_surveyControl.getSelfregistrationSurveys(
                                                       Integer.toString(iDomainId), 
                                                       strNamePrefix)).get(0);
            if (survey != null)
            {
               lstQuestions = survey.getQuestions();
               if ((lstQuestions != null) && (!lstQuestions.isEmpty()))
               {
                  // Go through all questions and get all possible answers for each one.
                  // Then check answered question if it is correct.
                  Iterator itQuestions = null;
                  Iterator itAnswers = null;
                  Question actualQuestion = null;
                  PossibleAnswer actualAnswer = null;
                  int iQuestionIndex = 0;
                  boolean isContainedAndCorrect = false;
                  String strCurrentAnsweredData = ""; 
                  
                  for (itQuestions = lstQuestions.iterator(); 
                       (itQuestions.hasNext() && bAllMandatoryAnswered);)
                  {
                     actualQuestion = (Question)itQuestions.next();
                     if (actualQuestion != null)
                     {
                        lstPossibleAnswers = actualQuestion.getPossibleAnswers();
                        
                        if ((lstPossibleAnswers != null) && (!lstPossibleAnswers.isEmpty()))
                        {
                           // get trimmed string from current answered data
                           strCurrentAnsweredData = (String)lstAnsweredData.get(iQuestionIndex);
                           // go through all possible answers and find out if
                           // answered question is contained within the possible answers and
                           // if yes if the answer is signed as correct
                           for (itAnswers = lstPossibleAnswers.iterator(); 
                                (itAnswers.hasNext() && !isContainedAndCorrect);)
                           {
                              actualAnswer = (PossibleAnswer)itAnswers.next();
                              if (strCurrentAnsweredData.equals(actualAnswer.getAnswerText())
                                    && (actualAnswer.isCorrect()))
                              {
                                 // the answered data is contained within the 
                                 // possible answers and the possible answer is correct 
                                 isContainedAndCorrect = true;
                              }
                           }
                        }
                        if (actualQuestion.isMandatory())
                        {
                           // current question is mandatory
                           // increase number of total mandatory questions
                           iTotalMandatory++;
                           if (actualQuestion.isAnyAnswer() || isContainedAndCorrect)
                           {
                              // increase count of correct and mandatory answers
                              iCorrectMandatory++;
                           }
                           if (strCurrentAnsweredData.trim().length() == 0)
                           {
                              // current answered data for mandatory question is empty
                              // but it has to be answered
                              bAllMandatoryAnswered = false;
                           }
                           else
                           {
                              // increase number of answered mandatory questions
                              iAnsweredMandatory++;
                           }
                        }
                        else
                        {
                           // current question is optional
                           iTotalOptional++;
                           if (actualQuestion.isAnyAnswer() || isContainedAndCorrect)
                           {
                              // increase count of correct and optional answers
                              iCorrectOptional++;
                           }
                           if (strCurrentAnsweredData.trim().length() > 0)
                           {
                              // current answered data for optional question is answered
                              // so increase number of answered optional questions
                              iAnsweredOptional++;
                           }
                        }
                     }
                     iQuestionIndex++;
                     isContainedAndCorrect = false;
                  }
               }

               if (bAllMandatoryAnswered)
               {
                  // check if there were answered all requested questions
                  switch (survey.getQuestionsAsked())
                  {
                     case Survey.AT_LEAST:
                     {
                        if (survey.getOptionalCount() == Survey.ALL_QUESTIONS)
                        {
                           // check case "at least all questions" 
                           bAllRequestedAnswered = ((iAnsweredMandatory + iAnsweredOptional) 
                              >= (iTotalMandatory + iTotalOptional));
                        }
                        else
                        {
                           // check case "at least all mandatory + x optional" 
                           bAllRequestedAnswered = ((iAnsweredMandatory + iAnsweredOptional) 
                              >= (iTotalMandatory + survey.getOptionalCount()));
                        }
                        break;
                     }
                     case Survey.EXACTLY:
                     {
                        if (survey.getOptionalCount() == Survey.ALL_QUESTIONS)
                        {
                           // check case "exactly all questions" 
                           bAllRequestedAnswered = ((iAnsweredMandatory + iAnsweredOptional) 
                              == (iTotalMandatory + iTotalOptional));
                        }
                        else
                        {
                           // check case "exactly all mandatory + x optional" 
                           bAllRequestedAnswered = ((iAnsweredMandatory + iAnsweredOptional) 
                              == (iTotalMandatory + survey.getOptionalCount()));
                        }
                        break;
                     }
                     case Survey.AT_MOST:
                     {
                        if (survey.getOptionalCount() == Survey.ALL_QUESTIONS)
                        {
                           // check case "at most all questions" 
                           bAllRequestedAnswered = ((iAnsweredMandatory + iAnsweredOptional) 
                              <= (iTotalMandatory + iTotalOptional));
                        }
                        else
                        {
                           // check case "at most all mandatory + x optional" 
                           bAllRequestedAnswered = ((iAnsweredMandatory + iAnsweredOptional) 
                              <= (iTotalMandatory + survey.getOptionalCount()));
                        }
                        break;
                     }
                     default:
                     {
                        assert false
                               : "Unknown type was specified for questions asked.";
                     }
                  }
               }
               
               if (bAllRequestedAnswered)
               {
                  // check if there were correctly answered all requested questions
                  switch (survey.getCorrectAnswers())
                  {
                     case Survey.AT_LEAST:
                     {
                        if (survey.getCorrectCount() == Survey.ALL_QUESTIONS)
                        {
                           // check case "at least all questions" 
                           bAllCorrectlyAnswered = ((iCorrectMandatory + iCorrectOptional) 
                              >= (iTotalMandatory + iTotalOptional));
                        }
                        else
                        {
                           // check case "at least all mandatory + x optional" 
                           bAllCorrectlyAnswered = ((iCorrectMandatory == iTotalMandatory) 
                              && ((iCorrectMandatory + iCorrectOptional) 
                                 >= (iTotalMandatory + survey.getCorrectCount())));
                        }
                        break;
                     }
                     case Survey.EXACTLY:
                     {
                        if (survey.getCorrectCount() == Survey.ALL_QUESTIONS)
                        {
                           // check case "exactly all questions" 
                           bAllCorrectlyAnswered = ((iCorrectMandatory + iCorrectOptional) 
                              == (iTotalMandatory + iTotalOptional));
                        }
                        else
                        {
                           // check case "exactly all mandatory + x optional" 
                           bAllCorrectlyAnswered = ((iCorrectMandatory == iTotalMandatory) 
                              && ((iCorrectMandatory + iCorrectOptional) 
                                 == (iTotalMandatory + survey.getCorrectCount())));
                        }
                        break;
                     }
                     case Survey.AT_MOST:
                     {
                        if (survey.getCorrectCount() == Survey.ALL_QUESTIONS)
                        {
                           // check case "at most all questions" 
                           bAllCorrectlyAnswered = ((iCorrectMandatory + iCorrectOptional) 
                              <= (iTotalMandatory + iTotalOptional));
                        }
                        else
                        {
                           // check case "at most all mandatory + x optional" 
                           bAllCorrectlyAnswered = ((iCorrectMandatory == iTotalMandatory) 
                              && ((iCorrectMandatory + iCorrectOptional) 
                                 <= (iTotalMandatory + survey.getCorrectCount())));
                        }
                        break;
                     }
                     default:
                     {
                        assert false 
                               : "Unknown type was specified for questions asked.";
                     }
                  }
               }
            }
         }
         catch (RemoteException rExc)
         {
            // We cannot propagate this exception otherwise XDoclet would generate 
            // the local interface incorrectly since it would include the declared
            // RemoteException in it (to propagate we would have to declare it)
            throw new OSSInternalErrorException("Remote error occurred", rExc);
         }
      }
      
      if (bAllCorrectlyAnswered)
      {
         // now if AND ONLY IF the survey is correct, we can set up domain ID into result string 
         returnID = Integer.toString(iDomainId);
      }

      return returnID;
   }

   /**
    * {@inheritDoc}
    */
   protected Domain checkDomainCondition(
      Domain domain,
      String strName
   ) throws OSSException
   {
      Domain domainReturn = null;
      
      // check first 2 conditions
      domainReturn = super.checkDomainCondition(domain, strName);
      
      if ((domainReturn == null) && (domain.isAllowSelfRegistration()) 
           && (!domain.isPublicDomain()) && (domain.getName().startsWith(strName)))
      {
         // If this third option is satisfied then we construct new Domain object
         // which will have only ID of the retrieved domain but no other data.
         // It is becouse user selected non public domain and we cannot populate
         // all domain data.
         domainReturn = new Domain(domain.getId(), null, null, null, null); 
      }
      
      return domainReturn;
   }
}
