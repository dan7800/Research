/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.customer;

import javax.servlet.ServletContext;
import javax.rmi.*;
import java.util.*;
import java.math.BigDecimal;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class MemberCardNut
{
	private static String strClassName = "MemberCardNut";

	public static MemberCardHome getHome()
	{
		try
		{
			Context lContext = new InitialContext();
			MemberCardHome lEJBHome = (MemberCardHome) PortableRemoteObject.narrow(lContext
					.lookup("java:comp/env/ejb/customer/MemberCard"), MemberCardHome.class);
			return lEJBHome;
		} catch (Exception e)
		{
			Log.printDebug("Caught exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static MemberCard getHandle(Long pkid)
	{
		return (MemberCard) getHandle(getHome(), pkid);
	}

	public static MemberCard getHandle(MemberCardHome lEJBHome, Long pkid)
	{
		try
		{
			return (MemberCard) lEJBHome.findByPrimaryKey(pkid);
		} catch (Exception e)
		{
			e.getMessage();
		}
		return null;
	}

	public static MemberCard fnCreate(MemberCardObject valObj)
	{
		MemberCard ejb = null;
		MemberCardHome home = getHome();
		try
		{
			ejb = home.create(valObj);
			valObj.pkid = ejb.getPkid();
			valObj.cardNo = ejb.getCardNo();

			MemberCardAuditTrailObject cardTrail = new MemberCardAuditTrailObject();
      	cardTrail.auditLevel = new Integer(0);
      	cardTrail.auditType = MemberCardAuditTrailBean.AUDIT_TYPE_CREATE;
      	cardTrail.cardPkid = valObj.pkid;
      	cardTrail.cardNo = valObj.cardNo;
      	cardTrail.cardName = valObj.nameDisplay;
      	cardTrail.branch = valObj.branch;
      	cardTrail.pcCenter = valObj.pcCenter;
      	cardTrail.userTxn = valObj.userCreate;
      	cardTrail.dateTxn = TimeFormat.getTimestamp();
      	cardTrail.dateCreate = cardTrail.dateTxn;
      	cardTrail.docRef1 = "";
      	cardTrail.docKey1 = new Long(0);
      	cardTrail.docRef2 = "";
      	cardTrail.docKey2 = new Long(0);
      	cardTrail.amountTxn = new BigDecimal(0);
      	cardTrail.amountDelta = new BigDecimal(0);
      	cardTrail.info1 = "";
      	cardTrail.info2 = "";
      	cardTrail.warning = "";
			MemberCardAuditTrail cardTrailEJB = MemberCardAuditTrailNut.fnCreate(cardTrail);	

			return ejb;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("MemberCardNut: " + "Cannot create this MemberCard");
			return (MemberCard) null;
		}
	}

	// ///////////////////////////////////////////////////
	public static MemberCardObject getObject(Long lPkid)
	{
		MemberCardObject valueObj = null;
		MemberCard objEJB = getHandle(lPkid);
		try
		{
			valueObj = objEJB.getObject();
		} catch (Exception ex)
		{
			Log.printDebug(ex.getMessage());
		}
		return (MemberCardObject) valueObj;
	}

	// /////////////////////////////////////////////////////////
/*	public static MemberCardObject getObjectByCardNo(String cardNo)
	{
		MemberCardObject theObj = null;
		try
		{
			MemberCardHome home = getHome();
			theObj = home.getObjectByCardNo(cardNo);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return theObj;
	}
*/

	public static Collection getObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			MemberCardHome home = getHome();
			result = home.getObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	// ////////////// reports and listing
	public static Vector getCRVBalanceListing(Timestamp crvExpiryFrom, Timestamp crvExpiryTo, BigDecimal crvBalanceGT,
			Integer salesmanId, String sortColumn, String sortPattern)
	{
		Vector vecResult = new Vector();
		try
		{
			MemberCardHome home = getHome();
			vecResult = home.getCRVBalanceListing(crvExpiryFrom, crvExpiryTo, crvBalanceGT, salesmanId, sortColumn,
					sortPattern);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public static MemberCardObject getObjectByCardNo(String cardNo)
	{
		MemberCardObject card = null;
      QueryObject queryCard = new QueryObject(new String[]{
            MemberCardBean.CARD_NO +" = '"+cardNo+"' "
                                 });
      queryCard.setOrder(" ORDER BY "+MemberCardBean.PKID);
      Vector vecCard = new Vector(MemberCardNut.getObjects(queryCard));
      if(vecCard.size()>0)
      {
         card = (MemberCardObject) vecCard.get(0);
      }
		return card;
	}

   public static boolean isValidMemberCardDate(Long cardPkid, String currentDate)
	{
	   	boolean result = false;
	   
		try
		{
			MemberCardHome home = getHome();
			result = home.isValidMemberCardDate(cardPkid, currentDate);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	} 

}
