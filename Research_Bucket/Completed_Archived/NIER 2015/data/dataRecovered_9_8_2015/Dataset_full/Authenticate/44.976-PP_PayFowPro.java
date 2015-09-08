/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.math.*;
import java.util.*;
import java.io.*;

import com.Verisign.payment.PFProAPI;

import org.compiere.util.*;

/**
 *  Payment Processor for VeriSign PayFow Pro
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFowPro.java,v 1.4 2002/11/01 05:05:21 jjanke Exp $
 */
public final class PP_PayFowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
	//	File file = new File (path, "f73e89fd.0");
	//	if (!file.exists())
	//		System.err.println("Did not find cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String  RESULT_INSUFFICIENT_FUNDS = "50";
	public final static String	RESULT_TIMEOUT_PROCESSOR = "104";
	public final static String	RESULT_TIMEOUT_HOST = "109";

	/**
	 *  Get Version
	 *  @return version
	 */
	public String getVersion()
	{
		return "PayFlowPro " + m_pp.Version();
	}   //  getVersion

	/**
	 *  Process CreditCard (no date check)
	 *  @param  trxType     Transaction type (Sales, Credit, ..)
	 *  @param  account     Account number
	 *  @param  expMM       Exp Month
	 *  @param  expYY       Exp Year
	 *  @param  amount		Amount
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC (String trxType, String account, int expMM, int expYY,
		BigDecimal amount) throws IllegalArgumentException
	{
		Log.trace(Log.l3_Util, "PP_PayFowPro.processCC");
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (trxType.equals(MPayment.TRX_SALES))
			param.append("TRXTYPE=").append(trxType);
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + trxType);
		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(account));	//	CreditCard No
		String month = String.valueOf(expMM);
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		param.append("&EXPDate=");										//	ExpNo
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(amount);							//	Amount

		//  Optional Fields
		if (getAuthorizationCode().length() > 0)
			param.append(createPair("&AUTHCODE", getAuthorizationCode(), 6));
		if (getComment().length() > 0)
			param.append(createPair("&COMMENT1", getComment(), 128));	//	Comment
	//	if (getComment2().length() > 0)
	//		param.append(createPair("&COMMENT2", getComment(), 128));	//	Comment
		if (getOriginalID().length() > 0)
			param.append(createPair("&ORIGID", getOriginalID(), 12));	//	PNREF - 12
		if (getStreet().length() > 0)
			param.append(createPair("&STREET", getStreet(), 30));		//	Street
		if (getZip().length() > 0)
			param.append(createPair("&ZIP", getZip(), 9));				//	Zip 5-9

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT


		return process(param.toString());
	}   //  processCC

	/**
	 *  Process Transaction
	 *  @param parameter Command String
	 *  @return true if processed successfully
	 */
	public boolean process (String parameter)
	{
		StringBuffer param = new StringBuffer(parameter);
		//  Usr/Pwd
		param
			.append("&PARTNER=").append(getPartnerID())
			.append("&VENDOR=").append(getVendorID())
			.append("&USER=").append(getUserID())
			.append("&PWD=").append(getPassword());
		Log.trace(Log.l4_Data, "-> " + param.toString());

		// Call the client.
		int rc = m_pp.CreateContext (getHostAddress(), getHostPort(), getTimeout(),
			getProxyAddress(), getProxyPort(), getProxyLogon(), getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		Log.trace(Log.l4_Data, "<- " + rc + " - " + response);
		setResponseResult("");
		setResponseInfo(response);

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			StringTokenizer pair = new StringTokenizer(token, "=", false);
			if (pair.countTokens() != 2)
				Log.error("PP_PayFowPro - Response token invalid = " + token);
			else
			{
				String name = pair.nextToken();
				String value = pair.nextToken();
				//
				if (name.equals("RESULT"))
					setResponseResult(value);
				else if (name.equals("PNREF"))
					setResponseID(value);
				else if (name.equals("RESPMSG"))
					setResponseMsg(value);
				else if (name.equals("AUTHCODE"))
					setResponseAuthCode(value);
				else if (name.equals("AVSADDR"))
					setResponseAVSAddr(value);
				else if (name.equals("AVSZIP"))
					setResponseAVSZip(value);
				else
					Log.error("PP_PayFowPro - Response unknown = " + token);
			}
		}
		//  Probelams with rc (e.g. 0 with Result=24)
		return isProcessedOK();
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		String s = getResponseResult();
		if (s == null)
			return false;
		return s.equals("0");
	}   //  isProcessedOK

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, String value, int maxLength)
	{
		//  Nothing to say
		if (name == null || value == null || value.length() == 0)
			return "";

		StringBuffer retValue = new StringBuffer(name);
		//	optional [length]
		if (value.indexOf("&") != -1 || value.indexOf("=") != -1)
			retValue.append("[").append(value.length()).append("]");
		//
		retValue.append("=");
		if (value.length() > maxLength)
			retValue.append(value.substring(0, maxLength));
		else
			retValue.append(value);
		return retValue.toString();
	}   // createPair

}   //  PP_PayFowPro
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.math.*;
import java.util.*;
import java.io.*;

import com.Verisign.payment.PFProAPI;

import org.compiere.util.*;

/**
 *  Payment Processor for VeriSign PayFow Pro
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFowPro.java,v 1.4 2002/11/01 05:05:21 jjanke Exp $
 */
public final class PP_PayFowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
	//	File file = new File (path, "f73e89fd.0");
	//	if (!file.exists())
	//		System.err.println("Did not find cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String  RESULT_INSUFFICIENT_FUNDS = "50";
	public final static String	RESULT_TIMEOUT_PROCESSOR = "104";
	public final static String	RESULT_TIMEOUT_HOST = "109";

	/**
	 *  Get Version
	 *  @return version
	 */
	public String getVersion()
	{
		return "PayFlowPro " + m_pp.Version();
	}   //  getVersion

	/**
	 *  Process CreditCard (no date check)
	 *  @param  trxType     Transaction type (Sales, Credit, ..)
	 *  @param  account     Account number
	 *  @param  expMM       Exp Month
	 *  @param  expYY       Exp Year
	 *  @param  amount		Amount
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC (String trxType, String account, int expMM, int expYY,
		BigDecimal amount) throws IllegalArgumentException
	{
		Log.trace(Log.l3_Util, "PP_PayFowPro.processCC");
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (trxType.equals(MPayment.TRX_SALES))
			param.append("TRXTYPE=").append(trxType);
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + trxType);
		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(account));	//	CreditCard No
		String month = String.valueOf(expMM);
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		param.append("&EXPDate=");										//	ExpNo
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(amount);							//	Amount

		//  Optional Fields
		if (getAuthorizationCode().length() > 0)
			param.append(createPair("&AUTHCODE", getAuthorizationCode(), 6));
		if (getComment().length() > 0)
			param.append(createPair("&COMMENT1", getComment(), 128));	//	Comment
	//	if (getComment2().length() > 0)
	//		param.append(createPair("&COMMENT2", getComment(), 128));	//	Comment
		if (getOriginalID().length() > 0)
			param.append(createPair("&ORIGID", getOriginalID(), 12));	//	PNREF - 12
		if (getStreet().length() > 0)
			param.append(createPair("&STREET", getStreet(), 30));		//	Street
		if (getZip().length() > 0)
			param.append(createPair("&ZIP", getZip(), 9));				//	Zip 5-9

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT


		return process(param.toString());
	}   //  processCC

	/**
	 *  Process Transaction
	 *  @param parameter Command String
	 *  @return true if processed successfully
	 */
	public boolean process (String parameter)
	{
		StringBuffer param = new StringBuffer(parameter);
		//  Usr/Pwd
		param
			.append("&PARTNER=").append(getPartnerID())
			.append("&VENDOR=").append(getVendorID())
			.append("&USER=").append(getUserID())
			.append("&PWD=").append(getPassword());
		Log.trace(Log.l4_Data, "-> " + param.toString());

		// Call the client.
		int rc = m_pp.CreateContext (getHostAddress(), getHostPort(), getTimeout(),
			getProxyAddress(), getProxyPort(), getProxyLogon(), getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		Log.trace(Log.l4_Data, "<- " + rc + " - " + response);
		setResponseResult("");
		setResponseInfo(response);

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			StringTokenizer pair = new StringTokenizer(token, "=", false);
			if (pair.countTokens() != 2)
				Log.error("PP_PayFowPro - Response token invalid = " + token);
			else
			{
				String name = pair.nextToken();
				String value = pair.nextToken();
				//
				if (name.equals("RESULT"))
					setResponseResult(value);
				else if (name.equals("PNREF"))
					setResponseID(value);
				else if (name.equals("RESPMSG"))
					setResponseMsg(value);
				else if (name.equals("AUTHCODE"))
					setResponseAuthCode(value);
				else if (name.equals("AVSADDR"))
					setResponseAVSAddr(value);
				else if (name.equals("AVSZIP"))
					setResponseAVSZip(value);
				else
					Log.error("PP_PayFowPro - Response unknown = " + token);
			}
		}
		//  Probelams with rc (e.g. 0 with Result=24)
		return isProcessedOK();
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		String s = getResponseResult();
		if (s == null)
			return false;
		return s.equals("0");
	}   //  isProcessedOK

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, String value, int maxLength)
	{
		//  Nothing to say
		if (name == null || value == null || value.length() == 0)
			return "";

		StringBuffer retValue = new StringBuffer(name);
		//	optional [length]
		if (value.indexOf("&") != -1 || value.indexOf("=") != -1)
			retValue.append("[").append(value.length()).append("]");
		//
		retValue.append("=");
		if (value.length() > maxLength)
			retValue.append(value.substring(0, maxLength));
		else
			retValue.append(value);
		return retValue.toString();
	}   // createPair

}   //  PP_PayFowPro
