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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.1 2003/07/16 19:08:37 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC");
		//
		StringBuffer param = new StringBuffer();
//        getTrxType(), ,
//					, getCreditCardExpYY(), getPayAmt()
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.3 2004/05/15 06:29:15 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC - " + p_mpp.getHostAddress() + " " + p_mpp.getHostPort() + ", Timeout=" + getTimeout()
			+ "; Proxy=" + p_mpp.getProxyAddress() + " " + p_mpp.getProxyPort() + " " + p_mpp.getProxyLogon() + " " + p_mpp.getProxyPassword());
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPaymentValidate.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the PayFlowPro client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.3 2004/05/15 06:29:15 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC - " + p_mpp.getHostAddress() + " " + p_mpp.getHostPort() + ", Timeout=" + getTimeout()
			+ "; Proxy=" + p_mpp.getProxyAddress() + " " + p_mpp.getProxyPort() + " " + p_mpp.getProxyLogon() + " " + p_mpp.getProxyPassword());
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPaymentValidate.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the PayFlowPro client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.2 2003/08/31 06:48:59 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC - " + p_mpp.getHostAddress() + " " + p_mpp.getHostPort() + ", Timeout=" + getTimeout()
			+ "; Proxy=" + p_mpp.getProxyAddress() + " " + p_mpp.getProxyPort() + " " + p_mpp.getProxyLogon() + " " + p_mpp.getProxyPassword());
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the PayFlowPro client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.1 2003/07/16 19:08:37 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC");
		//
		StringBuffer param = new StringBuffer();
//        getTrxType(), ,
//					, getCreditCardExpYY(), getPayAmt()
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.1 2003/07/16 19:08:37 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC");
		//
		StringBuffer param = new StringBuffer();
//        getTrxType(), ,
//					, getCreditCardExpYY(), getPayAmt()
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.3 2004/05/15 06:29:15 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC - " + p_mpp.getHostAddress() + " " + p_mpp.getHostPort() + ", Timeout=" + getTimeout()
			+ "; Proxy=" + p_mpp.getProxyAddress() + " " + p_mpp.getProxyPort() + " " + p_mpp.getProxyLogon() + " " + p_mpp.getProxyPassword());
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPaymentValidate.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the PayFlowPro client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.3 2004/05/15 06:29:15 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC - " + p_mpp.getHostAddress() + " " + p_mpp.getHostPort() + ", Timeout=" + getTimeout()
			+ "; Proxy=" + p_mpp.getProxyAddress() + " " + p_mpp.getProxyPort() + " " + p_mpp.getProxyLogon() + " " + p_mpp.getProxyPassword());
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPaymentValidate.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the PayFlowPro client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.2 2003/08/31 06:48:59 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC - " + p_mpp.getHostAddress() + " " + p_mpp.getHostPort() + ", Timeout=" + getTimeout()
			+ "; Proxy=" + p_mpp.getProxyAddress() + " " + p_mpp.getProxyPort() + " " + p_mpp.getProxyLogon() + " " + p_mpp.getProxyPassword());
		//
		StringBuffer param = new StringBuffer();
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the PayFlowPro client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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

import org.compiere.util.Ini;

/**
 *  Payment Processor for VeriSign PayFlow Pro.
 * 	Needs Certification File (get from VeriSign)
 *
 *  @author  Jorg Janke
 *  @version $Id: PP_PayFlowPro.java,v 1.1 2003/07/16 19:08:37 jjanke Exp $
 */
public final class PP_PayFlowPro extends PaymentProcessor
	implements Serializable
{
	/**
	 *  PayFowPro Constructor
	 */
	public PP_PayFlowPro()
	{
		super();
		m_pp = new PFProAPI();
		String path = Ini.getCompiereHome() + File.separator + "lib";
		//	Needs Certification File (not dustributed)
		File file = new File (path, "f73e89fd.0");
		if (!file.exists())
			log.error("No cert file " + file.getAbsolutePath());
		m_pp.SetCertPath (path);
	}   //  PP_PayFowPro

	//	Payment System			*/
	private PFProAPI    m_pp = null;
	private boolean		m_ok = false;

	public final static String	RESULT_OK = "0";
	public final static String	RESULT_DECLINED = "12";
	public final static String	RESULT_INVALID_NO = "23";
	public final static String	RESULT_INVALID_EXP = "24";
	public final static String	RESULT_INSUFFICIENT_FUNDS = "50";
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
	 *  @return true if processed successfully
	 *  @throws IllegalArgumentException
	 */
	public boolean processCC () throws IllegalArgumentException
	{
		log.debug("processCC");
		//
		StringBuffer param = new StringBuffer();
//        getTrxType(), ,
//					, getCreditCardExpYY(), getPayAmt()
		//  Transaction Type
		if (p_mp.getTrxType().equals(MPayment.TRXTYPE_Sales))
			param.append("TRXTYPE=").append(p_mp.getTrxType());
		else
			throw new IllegalArgumentException("PP_PayFlowPro TrxType not supported - " + p_mp.getTrxType());

		//  Mandatory Fields
		param.append("&TENDER=C")										//	CreditCard
			.append("&ACCT=").append(MPayment.checkNumeric(p_mp.getCreditCardNumber()));	//	CreditCard No
		param.append("&EXPDATE=");										//	ExpNo
		String month = String.valueOf(p_mp.getCreditCardExpMM());
		if (month.length() == 1)
			param.append("0");
		param.append(month);
		int expYY = p_mp.getCreditCardExpYY();
		if (expYY > 2000)
			expYY -= 2000;
		String year = String.valueOf(expYY);
		if (year.length() == 1)
			param.append("0");
		param.append(year);
		param.append("&AMT=").append(p_mp.getPayAmt());					//	Amount

		//  Optional Control Fields		- AuthCode & Orig ID
		param.append(createPair("&AUTHCODE", p_mp.getVoiceAuthCode(), 6));
		param.append(createPair("&ORIGID", p_mp.getOrig_TrxID(), 12));	//	PNREF - returned
		//	CVV
		param.append(createPair("&CVV2", p_mp.getCreditCardVV(), 4));
	//	param.append(createPair("&SWIPE", p_mp.getXXX(), 80));			//	Track 1+2

		//	Address
		param.append(createPair("&NAME", p_mp.getA_Name(), 30));
		param.append(createPair("&STREET", p_mp.getA_Street(), 30));	//	Street
		param.append(createPair("&ZIP", p_mp.getA_Zip(), 9));			//	Zip 5-9
		//	CITY 20, STATE 2,
		param.append(createPair("&EMAIL", p_mp.getA_EMail(), 64));		//	EMail

		//	Amex Fields
		//	DESC, SHIPTOZIP, TAXAMT
	//	param.append(createPair("&DESC", p_mp.getXXX(), 23));			//	Description
		param.append(createPair("&SHIPTOZIP", p_mp.getA_Zip(), 6));		//	Zip 6
		param.append(createPair("&TAXAMT", p_mp.getTaxAmt(), 10));		//	Tax

		//	Invoice No
		param.append(createPair("&INVNUM", p_mp.getC_Invoice_ID(), 9));

		//	COMMENT1/2
		param.append(createPair("&COMMENT1", p_mp.getC_Payment_ID(), 128));		//	Comment
		param.append(createPair("&COMMENT2", p_mp.getC_BPartner_ID(), 128)); 	//	Comment2

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
			.append("&PARTNER=").append(p_mpp.getPartnerID())
			.append("&VENDOR=").append(p_mpp.getVendorID())
			.append("&USER=").append(p_mpp.getUserID())
			.append("&PWD=").append(p_mpp.getPassword());
		log.debug("process -> " + param.toString());

		// Call the client.
		int rc = m_pp.CreateContext (p_mpp.getHostAddress(), p_mpp.getHostPort(), getTimeout(),
			p_mpp.getProxyAddress(), p_mpp.getProxyPort(), p_mpp.getProxyLogon(), p_mpp.getProxyPassword());
		String response = m_pp.SubmitTransaction(param.toString());
		m_pp.DestroyContext();
		//
		log.debug("process <- " + rc + " - " + response);
		p_mp.setR_Result("");
		p_mp.setR_Info(response);		//	complete info

		//  RESULT=1&PNREF=PN0001480030&RESPMSG=Invalid User Authentication
		//  RESULT=0&PNREF=P60501480167&RESPMSG=Approved&AUTHCODE=010101&AVSADDR=X&AVSZIP=X
		//	RESULT=-31&RESPMSG=The certificate chain did not validate, no local certificate found, javax.net.ssl.SSLException: Cert Path = C:\Compiere2\lib, Working Directory = C:\Compiere\compiere-all2\client\temp
		StringTokenizer st = new StringTokenizer(response, "&", false);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int pos = token.indexOf("=");
			String name = token.substring(0, pos);
			String value = token.substring(pos+1);
			//
			if (name.equals("RESULT"))
			{
				p_mp.setR_Result (value);
				m_ok = RESULT_OK.equals(value);
			}
			else if (name.equals("PNREF"))
				p_mp.setR_PnRef(value);
			else if (name.equals("RESPMSG"))
				p_mp.setR_RespMsg(value);
			else if (name.equals("AUTHCODE"))
				p_mp.setR_AuthCode(value);
			else if (name.equals("AVSADDR"))
				p_mp.setR_AvsAddr(value);
			else if (name.equals("AVSZIP"))
				p_mp.setR_AvsZip(value);
			else if (name.equals("IAVS"))		//	N=YSA, Y=International
				;
			else if (name.equals("CVV2MATCH"))	//	Y/N X=not supported
				;
			else
				log.error("process - Response unknown = " + token);
		}
		//  Probelms with rc (e.g. 0 with Result=24)
		return m_ok;
	}   //  process

	/**
	 *  Payment is procesed successfully
	 *  @return true if OK
	 */
	public boolean isProcessedOK()
	{
		return m_ok;
	}   //  isProcessedOK

	/*************************************************************************/

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, BigDecimal value, int maxLength)
	{
		if (value == null)
			return createPair (name, "0", maxLength);
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

	/**
	 *  Check for delimiter fields &= and add length
	 *  @param name name
	 *  @param value value
	 *  @param maxLength maximum length
	 *  @return name[5]=value or name=value
	 */
	private String createPair(String name, int value, int maxLength)
	{
		if (value == 0)
			return "";
		else
			return createPair (name, String.valueOf(value), maxLength);
	}	//	createPair

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
		if (name == null || name.length() == 0
			|| value == null || value.length() == 0)
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
