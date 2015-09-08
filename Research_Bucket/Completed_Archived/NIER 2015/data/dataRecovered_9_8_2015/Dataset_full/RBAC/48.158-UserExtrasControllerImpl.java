/*
 * Copyright (c) 2003 - 2007 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: UserExtrasControllerImpl.java,v 1.26 2009/04/22 06:29:13 bastafidli Exp $
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

package org.opensubsystems.security.logic.impl;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.impl.StatelessControllerImpl;
import org.opensubsystems.security.logic.UserExtrasController;

/**
 * Implementation of UserExtrasController interface to manipulate users.
 *
 * View-type has to be set to local due to bug XDT-867 affecting WebSphere
 * Refs has to be set to local JNDI name since we do not want to use remote objects.
 * 
 * @ejb.bean type="Stateless"
 *           name="UserExtrasController"
 *           view-type="local" 
 *           jndi-name="org.opensubsystems.security.logic.UserExtrasControllerRemote"
 *           local-jndi-name="org.opensubsystems.security.logic.UserExtrasController"
 * @ejb.interface 
 *  local-extends="javax.ejb.EJBLocalObject, org.opensubsystems.security.logic.UserExtrasController"
 *  extends="javax.ejb.EJBObject, org.opensubsystems.security.logic.UserExtrasController"
 *
 * @jonas.bean ejb-name="UserExtrasController"
 *             jndi-name="org.opensubsystems.security.logic.UserExtrasControllerRemote"
 *
 * @version $Id: UserExtrasControllerImpl.java,v 1.26 2009/04/22 06:29:13 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class UserExtrasControllerImpl extends    StatelessControllerImpl
                                      implements UserExtrasController
{
   // Configuration settings ///////////////////////////////////////////////////

// TODO: For Miro: Once you resolve the to do below you can address tis as well   
//   /** 
//    * Name of the property containing flag if roles for imported users should be 
//    * reset explicitly.
//    */   
//   private static final String RESET_USER_ROLES 
//                                  = "oss.security.userimport.resetroles";

   // Constants ////////////////////////////////////////////////////////////////

   /** 
    * Default value for property containing flag if roles for imported users 
    * should be reset explicitly.
    */   
   public static final Boolean RESET_USER_ROLES_DEFAULT = Boolean.TRUE;

   /**
    * Prefix used to create output file.
    */
   public static final String OUTPUT_FILE_PREFIX = "out_";
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 322547463691937622L;

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    */
   public UserExtrasControllerImpl(
   ) 
   {
      super();

      // Do not cache anything here since if this controller is run as a stateless
      // session bean the referenced objects may not be ready
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    * 
    * @ejb.interface-method
    * @ejb.transaction type="Required"
    */
   public int[] importUserData(
      String  strFile,      
      int     iIgnoredRowsCount,
      boolean bInsert,
      boolean bClean
   ) throws OSSException
   {
      // TODO: For Miro: Rewrite this method using batch
      // this method is commented for now, because there should be improved using
      // batch.
      // see comment at: https://sourceforge.net/forum/forum.php?thread_id=1194093&forum_id=381683
      // There is limitation for PostgreSQL because there cannot be processed more sql commands
      // within 1 transaction after an error has already occurred. 
      /*
      boolean bAddHintSheet = false;
      boolean bOutputError  = false;
      User  data      = null;
      int   iImported  = 0;
      int   iIncorrect = 0;
      int   iSkipped   = 0;
      int   iDeleted   = 0;
      int[] outResult  = null;
      OSSException previousExc;
      
      StringBuffer buffer = new StringBuffer();
      
      File             input;
      File             output;
      Workbook         workbook = null;
      WritableWorkbook copy = null;

      Properties prpSettings = Config.getInstance().getPropertiesSafely();
      
      // Flag telling if roles for imported users should be reset explicitly.
      boolean bResetRoles = PropertyUtils.getBooleanProperty(
                         prpSettings, RESET_USER_ROLES, RESET_USER_ROLES_DEFAULT, 
                         "Reset roles for imported users").booleanValue();
               
      SimpleRule lsdDeleteSecurity = m_authorityControl.getRightsForCurrentUser(
                                        DataConstant.USER_DATA_TYPE,
                                        ActionConstants.RIGHT_ACTION_DELETE);

      input = new File(strFile);
      output = new File(input.getParentFile().getAbsolutePath() 
                        + File.separator + OUTPUT_FILE_PREFIX + input.getName());
      try
      {
         try
         {
            // initialization of the MS Excel file
             workbook = Workbook.getWorkbook(input);
         }
         catch (BiffException beExc)
         {
            throw new OSSInvalidDataException("Cannot open " + strFile, beExc);
         }
         catch (IOException ieExc)
         {
            throw new OSSInvalidDataException("Cannot open " + strFile, ieExc);
         }
   
         try
         {
            // initialization of the output MS Excel file
            copy = Workbook.createWorkbook(output, workbook);
         }         
         catch (IOException ieExc)
         {
            s_logger.log(Level.WARNING, "Cannot create file " + output.getName(), ieExc);
            bOutputError = true;
         }
   
         Sheet         sheet = workbook.getSheet(0);
         WritableSheet sheetOut = null;
         if (!bOutputError)
         {
            sheetOut = copy.getSheet(0);
         } 
      
         // row initialization   | LOGIN | FIRST_NAME | LAST_NAME | PASSWORD | INTERNAL |
         Cell cLogin = null;      // cell position [0,y] 
         Cell cFirstName = null;  // cell position [1,y]
         Cell cLastName = null;   // cell position [2,y]
         Cell cPassword = null;   // cell position [3,y]
         Cell cInternal = null;   // cell position [4,y]
         
         String strLogin = null;
         String strFirstName = null;
         String strLastName = null;
         String strPassword = null;
         String strInternal = null;
         
         int iStartRow = iIgnoredRowsCount;
         int iCounter = 0;
   
         // font and backgroung initialization
         WritableFont       wfFontBold = new WritableFont(WritableFont.ARIAL, 10, 
                                                          WritableFont.BOLD);
         WritableCellFormat wcfFormat = new WritableCellFormat(wfFontBold); 
         WritableCellFormat wcfFormatOrange = new WritableCellFormat(wfFontBold); 
         WritableCellFormat wcfFormatGreen = new WritableCellFormat(wfFontBold);
         int iTotalRows;
   
         try
         {
            wcfFormatOrange.setBackground(Colour.LIGHT_ORANGE);
            wcfFormatGreen.setBackground(Colour.LIGHT_GREEN);
         }
         catch (WriteException weExc)
         {
            throw new OSSInternalErrorException(
                         "Problems with writing to " + output.getName(), weExc);
         }
   
         iTotalRows = sheet.getRows();
         for (iCounter = iStartRow; iCounter < iTotalRows; iCounter++)
         {
            strLogin = "";
            strFirstName = "";
            strLastName = "";
            strPassword = "";
            strInternal = "";               
            try
            {
               cLogin = sheet.getCell(0, iCounter);
               strLogin = cLogin.getContents().trim();
   
               cFirstName = sheet.getCell(1, iCounter);
               strFirstName = cFirstName.getContents().trim();
   
               cLastName = sheet.getCell(2, iCounter);
               strLastName = cLastName.getContents().trim();
   
               cPassword = sheet.getCell(3, iCounter);
               strPassword = cPassword.getContents().trim();
   
               cInternal = sheet.getCell(4, iCounter);               
               strInternal = cInternal.getContents().trim();
            }
            catch (ArrayIndexOutOfBoundsException aeExc)
            {
               // This means that the given column is not there, let the
               // code below decide if the column was important
            }
            
            // do not test for empty rows
            if (!((strLogin.length() == 0)
                && (strFirstName.length() == 0)
                && (strLastName.length() == 0)
                && (strPassword.length() == 0)
                && (strInternal.length() == 0)
               ))
            {
               // if INTERNAL is empty - use No default value
               if (strInternal.length() == 0)
               {
                  strInternal = "No";
               }
               // test if the row is correctly written (all required cells must be filled
               // and also data types must be correct)
               if ((strLogin.length() == 0)
                   || (strFirstName.length() == 0)
                   || (strLastName.length() == 0)
                   || (strPassword.length() == 0)
                   || (!checkCellNumberFormat(strLogin, 4)) // login length is min. 4 digits 
                   || (!checkCellNumberFormat(strPassword, 6)) // passw length is min. 6 digits
                   || (!checkCellYesNo(strInternal)) // only yes or no values
                  )
               {
                  if (!bOutputError)
                  {
                     // set background color to specify incorrect row
                     sheetOut.getWritableCell(0, iCounter).setCellFormat(wcfFormatOrange);
                     sheetOut.getWritableCell(1, iCounter).setCellFormat(wcfFormatOrange);
                     sheetOut.getWritableCell(2, iCounter).setCellFormat(wcfFormatOrange);
                     sheetOut.getWritableCell(3, iCounter).setCellFormat(wcfFormatOrange);
                     sheetOut.getWritableCell(4, iCounter).setCellFormat(wcfFormatOrange);
                     bAddHintSheet = true;
                  }
                  iIncorrect++;
               }
               else
               {
                  if (bInsert)
                  {
                     // create user object that to be inserted into the DB
                     data = new User(DataObject.NEW_ID,
                                      CallContext.getInstance().getCurrentDomainId(),
                                      strFirstName,
                                      strLastName,
                                      "",
                                      "",
                                      "",
                                      strLogin,
                                      strLogin,
                                      strPassword,
                                      true, // for toolcrib
                                      true, // for toolcrib
                                      true, // for toolcrib
                                      strInternal.equalsIgnoreCase("YES"), 
                                      null,
                                      null, true);
                     if (checkAccess(data, ActionConstants.RIGHT_ACTION_CREATE)
                        == AccessRight.ACCESS_GRANTED)
                     {
                        previousExc = null;
                        try
                        {
                           data = (User)m_userFactory.create(data);
                           // create and assign personal role
                           m_roleFactory.createPersonal(data);
      
                           iImported++;
                        }
                        catch (OSSException eExc)
                        {
                           previousExc = eExc;
                           // MHALAS: Here we need to find out if it is really 
                           // imported or there was some other error
                           data = m_userFactory.getByLoginName(strLogin);
                           if (data != null)
                           {                                        
                              s_logger.log(Level.FINEST, "User '" + strLogin
                                                         + "' already exists.");
                        
                              if (!bOutputError)
                              {
                                 // set background color to specify row 
                                 // which was already added into the DB
                                 sheetOut.getWritableCell(0, iCounter).setCellFormat(
                                                                          wcfFormatGreen);
                                 sheetOut.getWritableCell(1, iCounter).setCellFormat(
                                                                          wcfFormatGreen);
                                 sheetOut.getWritableCell(2, iCounter).setCellFormat(
                                                                          wcfFormatGreen);
                                 sheetOut.getWritableCell(3, iCounter).setCellFormat(
                                                                          wcfFormatGreen);
                                 sheetOut.getWritableCell(4, iCounter).setCellFormat(
                                                                          wcfFormatGreen);
                                 bAddHintSheet = true;
                              } 
                              iSkipped++;
                           }
                           else
                           {
                              s_logger.log(Level.FINEST, "User " + strLogin
                                                         + " cannot be created.");
                           }
                        }
                        
                        // if create and get failed we throw an exception 
                        if (data == null)
                        {
                           throw new OSSDataCreateException(
                              "An error has occurred while importing user " + strLogin, 
                              previousExc);
                        }
                        else
                        {
                           // append user IDs to the string buffer
                           if (buffer.length() > 0)
                           {
                              buffer.append(",");
                           }
                           buffer.append(data.getId());
                        }
                     }
                     else
                     {
                        // We couldn't create it since we didn't have rights,
                        // count it as skipped, it will not be marked in output
                        // spreadsheet and therefore it will signal that it wasn't
                        // imported
                        iSkipped++;                     
                     }
                  }
                  else
                  {
                     // if bInsert = false do check for spreadsheet data only
                     bAddHintSheet = true;
                  }
               }
            }
         }
      
         // add Hint sheet if there was an error during MS Excel file processing
         if (bAddHintSheet && !bOutputError)
         {
            try
            {
               WritableSheet sheetOutHint = copy.createSheet("Hint", 1);
               Label lLabel = null;
               lLabel = new Label(1, 1, "Description to the colours", wcfFormat);
               sheetOutHint.addCell(lLabel);
               lLabel = new Label(2, 3, "", wcfFormatOrange); 
                  sheetOutHint.addCell(lLabel); 
    
                  lLabel = new Label(3, 3, " - this colour means that something is incorrect" +
                                        " in the specified row");
               sheetOutHint.addCell(lLabel);
               lLabel = new Label(3, 4, " Possible reasons:");
               sheetOutHint.addCell(lLabel);
               lLabel = new Label(4, 5, " a/ required cell is empty"); 
               sheetOutHint.addCell(lLabel);
               lLabel = new Label(4, 6, " b/ Login name is not of the number type");
               sheetOutHint.addCell(lLabel);
               lLabel = new Label(4, 7, " c/ Login name length is less then 4 digits");
               sheetOutHint.addCell(lLabel);
               lLabel = new Label(4, 8, " d/ Password is not of the number type");
               sheetOutHint.addCell(lLabel); 
               lLabel = new Label(4, 9, " e/ Password length is less then 6 digits");
               sheetOutHint.addCell(lLabel); 
               lLabel = new Label(4, 10, " e/ Internal can be only YES or NO or empty");
               sheetOutHint.addCell(lLabel); 
               
               lLabel = new Label(2, 11, "", wcfFormatGreen); 
               sheetOutHint.addCell(lLabel); 
               lLabel = new Label(3, 11, " - this colour means that row already exists" +
                                         " within the DB"); 
               sheetOutHint.addCell(lLabel); 
            }
            catch (WriteException weExc)
            {
               s_logger.log(Level.WARNING, "Problems with Hint tab creation. ", weExc);
            }
         }
      }
      finally
      {
         try
         {
            // close workbooks
            try
            {
               if (copy != null)
               {
                  copy.write();
                  copy.close();
               }
            }
            finally
            {
               if (workbook != null)
               {
                  workbook.close();
               }
            }
         }
         catch (IOException eExc)
         {
            s_logger.log(Level.WARNING, "Problems with out work book close. ", eExc);
         }
         finally
         {
            // delete out file if there was not error occurred
            if ((!bAddHintSheet) && (!bOutputError) && (output.exists()))
            {
               output.delete();
            }
         }
      }

      // Delete users not contained in the Excel sheet and not currently logged in
      // if at least one user was imported
      if ((bInsert) && (bClean) && (buffer.length() > 0))
      {
         if (bResetRoles)
         {
            // There will be used manual cascade deleting
            m_roleFactory.removeFromUsers(buffer.toString(), true);
         }

         iDeleted = m_userFactory.deleteAllExceptSpecified(
                  StringUtils.parseStringToIntArray(buffer.toString(), ","),
                  lsdDeleteSecurity);
      }
   
      outResult = new int[] {iImported, iIncorrect, iSkipped, 
                             iDeleted, (bOutputError && bAddHintSheet) ? 0 : 1};
      
      return outResult;
      */
      return new int[] {0, 0, 0, 0, 0};
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
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Test if cell contains valid number.
    *
    * @param strInputCell - input string from the cell  
    * @param iMinNumberCount - minimal number of digits the string has to contain
    * @return boolean - true if cell contains number with minimal digits
    * @throws OSSException - an error has occurred 
    */
   protected boolean checkCellNumberFormat(
      String strInputCell,
      int    iMinNumberCount
   ) throws OSSException
   {
      boolean bResult = true;
      // test number count
      if (strInputCell.length() < iMinNumberCount)
      {
         bResult = false;
      }
      // test for number representation of the string
      try
      {
         Integer.parseInt(strInputCell);
      }
      catch (NumberFormatException nfeExc)
      {
         bResult = false;
      }
      return bResult;
   }

   /**
    * Check if cell contains YES or NO string. Comparison is case insensitive.
    * 
    * @param strInputCell - cell text
    * @return boolean - true if text is one of YES or NO strings
    */
   protected boolean checkCellYesNo(
      String strInputCell
   )
   {
      return strInputCell.equalsIgnoreCase("NO") || strInputCell.equalsIgnoreCase("YES");
   }
}
