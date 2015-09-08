/*
 *
 * Copyright 2002 VLEE. All Rights Reserved.
 * (http://vlee.net)
 *
 * This software is the proprietary information of VLEE,  
 * Use is subject to license terms.
 * 
 */
package com.vlee.ejb.customer;

import java.rmi.RemoteException;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.BigDecimal;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;
import com.vlee.ejb.inventory.*;
import com.vlee.bean.autoworkshop.JobsheetGSTReportForm;
import com.vlee.bean.customer.*;
import com.vlee.bean.reports.*;
import com.vlee.bean.footwear.*;
import com.vlee.bean.loyalty.*;
import com.vlee.ejb.accounting.*;

public class InvoiceItemBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String INVOICE_ID = "invoice_id";
	public static final String POS_ITEM_ID = "pos_item_id";
	public static final String REMARKS = "remarks";
	public static final String TOTALQTY = "total_quantity";
	public static final String CURRENCY = "currency";
	public static final String UNIT_PRICE_QUOTED = "unit_price_quoted";
	public static final String STR_NAME_1 = "str_name_1";
	public static final String STR_VALUE_1 = "str_value_1";
	public static final String PIC1 = "pic1";
	public static final String PIC2 = "pic2";
	public static final String PIC3 = "pic3";
	public static final String CURRENCY2 = "currency2";
	public static final String UNIT_PRICE_QUOTED2 = "unit_price_quoted2";
	public static final String TAXAMT = "taxamt";
	public static final String TAXAMT2 = "taxamt2";
	public static final String STR_NAME_2 = "str_name_2";
	public static final String STR_VALUE_2 = "str_value_2";
	public static final String STR_NAME_3 = "str_name_3";
	public static final String STR_VALUE_3 = "str_value_3";
	public static final String INT_NAME_1 = "int_name_1";
	public static final String INT_VALUE_1 = "int_value_1";
	public static final String INT_NAME_2 = "int_name_2";
	public static final String INT_VALUE_2 = "int_value_2";
	public static final String BD_NAME_1 = "bd_name_1";
	public static final String BD_VALUE_1 = "bd_value_1";
	public static final String STATUS = "status";
	public static final String STATE = "state";
	public static final String POS_ITEM_TYPE = "pos_item_type";
	public static final String ITEM_ID = "item_id";
	public static final String ITEM_CODE = "item_code";
	public static final String BAR_CODE = "bar_code";
	public static final String SERIALIZED = "serialized";
	public static final String NAME = "name";
	public static final String OUTSTANDING_QTY = "outstanding_qty";
	public static final String PACKAGE = "package";
	public static final String PARENT_ID = "parent_id";
	public static final String UNIT_COST_MA = "unit_cost_ma";
	public static final String UNIT_PRICE_STD = "unit_price_std";
	public static final String UNIT_DISCOUNT = "unit_discount";
	public static final String UNIT_COMMISSION = "unit_commission";
	public static final String CODE_PROJECT = "code_project";
	public static final String CODE_DEPARTMENT = "code_department";
	public static final String CODE_DEALER = "code_dealer";
	public static final String CODE_SALESMAN = "code_salesman";
	public static final String STK_ID = "stk_id";
	public static final String STK_LOCATION_ID = "stk_location_id";
	public static final String STK_LOCATION_CODE = "stk_location_code";
	public static final String BOM_CONVERT_MODE = "bom_convert_mode";
	public static final String BOM_ID = "bom_id";
	public static final String BOM_CONVERT_STATUS = "bom_convert_status";
	public static final String BOM_CONVERT_TIME = "bom_convert_time";
	public static final String BOM_CONVERT_USER = "bom_convert_user";
	public static final String BOM_TARGET_LOC = "bom_target_loc";
	public static final String BOM_SOURCE_LOC = "bom_source_loc";
	public static final String WARRANTY_TYPE = "warranty_type";
	public static final String WARRANTY_OPTION = "warranty_option";
	public static final String WARRANTY_EXPIRY = "warranty_expiry";
	public static final String PSEUDO_LOGIC = "pseudo_logic";
	public static final String PSEUDO_CODE = "pseudo_code";
	public static final String PSEUDO_NAME = "pseudo_name";
	public static final String PSEUDO_DESCRIPTION = "pseudo_description";
	public static final String PSEUDO_CURRENCY = "pseudo_currency";
	public static final String PSEUDO_PRICE = "pseudo_price";
	public static final String PSEUDO_QTY = "pseudo_qty";
	public static final String LOYALTY_LOGIC = "loyalty_logic";
	public static final String LOYALTY_POINTS_AWARDED ="loyalty_points_awarded";
	public static final String LOYALTY_POINTS_REDEEMED = "loyalty_points_redeemed";
	public static final String PACKAGE_GROUP = "package_group";
	
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "act";
	public static final String STATUS_CANCELLED = "cxl";
	public static final String STATUS_INACTIVE = "ina";

	// Constants for PACKAGE_GROUPS
	public static final String PACKAGE_GROUP_NONE = "NONE";

	
	// Constants for  Loyalty Programme
	public static final String LOYALTY_LOGIC_REWARD = "REWARD";
	public static final String LOYALTY_LOGIC_REDEEM = "REDEEM";


	// Constants for STR_NAME_1
	public static final String STRNAME1_DEPTCODE = "dept_code";
	// Constants for BD_NAME_1
	public static final String BDNAME1_INFLATED_PRICE = "inf";
	public static final String BDNAME1_COMMISSION = "csn";
	// Constants for PKID
	public static final Long PKID_START = new Long(1000);
	// DEFAULTS
	public static final String DEF_REMARKS = "";
	private InvoiceItemObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	// private static final String TABLENAME = "cust_invoice_item";
	public static final String TABLENAME = "cust_invoice_item";
	// Other params
	private static final String strObjectName = "InvoiceItemBean: ";
	public static final String MODULENAME = "pos";
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public InvoiceItemObject getObject()
	{
		return this.valObj;
	}

	public void setObject(InvoiceItemObject newVal)
	{
		Long pkid = this.valObj.mPkid;
		this.valObj = newVal;
		this.valObj.mPkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.mPkid;
	}

	public Long getInvoiceId()
	{
		return this.valObj.mInvoiceId;
	}

	public Integer getPosItemId()
	{
		return this.valObj.mPosItemId;
	}

	public String getRemarks()
	{
		return this.valObj.mRemarks;
	}

	public BigDecimal getTotalQty()
	{
		return this.valObj.mTotalQty;
	}

	public String getCurrency()
	{
		return this.valObj.mCurrency;
	}

	public BigDecimal getUnitPriceQuoted()
	{
		return this.valObj.mUnitPriceQuoted;
	}

	public String getStatus()
	{
		return this.valObj.mStatus;
	}

	public String getStrName1()
	{
		return this.valObj.mStrName1;
	}

	public String getStrValue1()
	{
		return this.valObj.mStrValue1;
	}

	/***************************************************************************
	 * Setters
	 **************************************************************************/
	public void setPkid(Long pkid)
	{
		this.valObj.mPkid = pkid;
	}

	public void setInvoiceId(Long invoiceId)
	{
		this.valObj.mInvoiceId = invoiceId;
	}

	public void setPosItemId(Integer posItemId)
	{
		this.valObj.mPosItemId = posItemId;
	}

	public void setRemarks(String remarks)
	{
		this.valObj.mRemarks = remarks;
	}

	public void setTotalQty(BigDecimal totalQty)
	{
		this.valObj.mTotalQty = totalQty;
	}

	public void setCurrency(String currency)
	{
		this.valObj.mCurrency = currency;
	}

	public void setUnitPriceQuoted(BigDecimal unitPriceQuoted)
	{
		this.valObj.mUnitPriceQuoted = unitPriceQuoted;
	}

	public void setStatus(String status)
	{
		this.valObj.mStatus = status;
	}

	public void setStrName1(String strName1)
	{
		this.valObj.mStrName1 = strName1;
	}

	public void setStrValue1(String strValue1)
	{
		this.valObj.mStrValue1 = strValue1;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(InvoiceItemObject newObj) throws CreateException
	{
		Long newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.valObj = newObj;
			this.valObj.mPkid = newKey;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return newKey;
	}

	public void ejbPostCreate(InvoiceItemObject invObj)
	{
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		if (result)
		{
			return primaryKey;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + primaryKey.toString() + "not found.");
		}
	}

	/***************************************************************************
	 * ejbFindObjectsGiven
	 **************************************************************************/
	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3,
					(String) null, (String) null);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}






   public Collection ejbHomeGetObjects(QueryObject query)
   {
      Collection vecValObj = new Vector();
      try
      {
         vecValObj = selectObjects(query);
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return vecValObj;
   }

	public Vector ejbHomeGetDiscountSummaryByRemarks(Integer branchId, Timestamp dateFrom, Timestamp dateTo, Integer salesman)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectDiscountSummaryByRemarks(branchId, dateFrom, dateTo, salesman);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

	public Vector ejbHomeGetValueObjectsGiven(String posType, Integer posItemId, Integer svcCtrId, Timestamp dateFrom,
			Timestamp dateTo)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(posType, posItemId, svcCtrId, dateFrom, dateTo);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(String field1, String value1, String field2, String value2,
			String field3, String value3, String field4, String fuzzy4)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(field1, value1, field2, value2, field3, value3, field4, fuzzy4);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}
	
	public SalesRecordsByCustomerReport ejbHomeGetSalesRecordsByCustomerReport(SalesRecordsByCustomerReport srbcr)
	{
		try
		{
			srbcr = selectSalesRecordsByCustomerReport(srbcr);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return srbcr;
	}
	// 20080131 Jimmy
	public GrossProfitBySupervisorTechnicianDetailsForm ejbHomeGetGrossProfitBySupervisorTechnicianDetailsForm
		(GrossProfitBySupervisorTechnicianDetailsForm gpstd)
	{
		try
		{
			gpstd = selectGrossProfitBySupervisorTechnicianDetailsForm(gpstd);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return gpstd;
	}

	// 20080212 Jimmy
	public GrossProfitBySupervisorTechnicianSummaryForm ejbHomeGetGrossProfitBySupervisorTechnicianSummaryForm
			(GrossProfitBySupervisorTechnicianSummaryForm gpsts)
	{
		try
		{
			gpsts = selectGrossProfitBySupervisorTechnicianSummaryForm(gpsts);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return gpsts;
	}
	
	// 20080304 Jimmy
	public GrossProfitByDepartmentDetailsForm ejbHomeGetGrossProfitByDepartmentDetailsForm
			(GrossProfitByDepartmentDetailsForm gpdd)
	{
		try
		{
			gpdd = selectGrossProfitByDepartmentDetailsForm(gpdd);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return gpdd;
	}	

	// 20080305 Jimmy
	public GrossProfitByDepartmentSummaryForm ejbHomeGetGrossProfitByDepartmentSummaryForm
			(GrossProfitByDepartmentSummaryForm gpdd)
	{
		try
		{
			gpdd = selectGrossProfitByDepartmentSummaryForm(gpdd);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return gpdd;
	}	

	// 20080415 Jimmy
	public GrossProfitBySalesmanByInvoiceReport ejbHomeGetGrossProfitBySalesmanByInvoiceReport
			(GrossProfitBySalesmanByInvoiceReport gpsi)
	{
		try
		{
			gpsi = selectGrossProfitBySalesmanByInvoiceReport(gpsi);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return gpsi;
	}
	
	public JobsheetGSTReportForm.Row ejbHomeGetJobsheetGSTReport(Timestamp date, TreeMap treeBranches)
	{
		JobsheetGSTReportForm.Row rpt = null;
		try
		{
			rpt = selectJobsheetGSTReport(date, treeBranches);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return rpt;
	}	
	
	
	public Vector ejbHomeGetObjectsWithInvoiceConditions(QueryObject queryObj, Timestamp dateStart, Timestamp dateEnd, String keyword)
	{
		Vector vecResult = new Vector();
		try
		{
			vecResult = selectObjectsWithInvoiceConditions(queryObj,dateStart,dateEnd, keyword);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return vecResult;
	}

   public Vector ejbHomeGetPointsSummaryByItemCode(Timestamp dateFrom, Timestamp dateTo, Integer iBranch,
														Integer limit, String orderByQuery, String txnType)
   {
      Vector vecResult = new Vector();
      try
      {
         vecResult = selectPointsSummaryByItemCode(dateFrom, dateTo, iBranch, limit, orderByQuery, txnType);
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
      return vecResult;
   }

	// getSalesRecordsByCustomerReport
	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.valObj.mPkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * setEntityContext
	 **************************************************************************/
	public void setEntityContext(EntityContext context)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.context = context;
	}

	/***************************************************************************
	 * unsetEntityContext
	 **************************************************************************/
	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.context = null;
	}

	/***************************************************************************
	 * ejbActivate
	 **************************************************************************/
	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new InvoiceItemObject();
		this.valObj.mPkid = (Long) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
	}

	/***************************************************************************
	 * ejbLoad
	 **************************************************************************/
	public void ejbLoad()
	{
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbLoad: " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * ejbStore
	 **************************************************************************/
	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	/** ********************* Database Routines ************************ */
	private void makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection() throws NamingException, SQLException
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Long insertObject(InvoiceItemObject newObj) throws NamingException, SQLException
	{
		Long nextPKId = null;
		Log.printVerbose(strObjectName + " insertObject: ");
		makeConnection();
		try
		{
			nextPKId = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		newObj.setPkid(nextPKId);
		String insertStatement = " INSERT INTO " + TABLENAME + "(" + PKID + ", " + INVOICE_ID + ", " + POS_ITEM_ID
				+ ", " + REMARKS + ", " + TOTALQTY + ", " + CURRENCY + ", " + UNIT_PRICE_QUOTED + ", " + STR_NAME_1
				+ ", " + STR_VALUE_1 + ", " + // 09
				PIC1 + ", " + PIC2 + ", " + PIC3 + ", " + CURRENCY2 + ", " + UNIT_PRICE_QUOTED2 + ", " + TAXAMT + ", "
				+ TAXAMT2 + ", " + STR_NAME_2 + ", " + STR_VALUE_2 + ", " + STR_NAME_3 + ", " + // 19
				STR_VALUE_3 + ", " + INT_NAME_1 + ", " + INT_VALUE_1 + ", " + INT_NAME_2 + ", " + INT_VALUE_2 + ", "
				+ BD_NAME_1 + ", " + BD_VALUE_1 + ", " + STATUS + ", " + STATE + ", " + POS_ITEM_TYPE + ", " + // 29
				ITEM_ID + ", " + ITEM_CODE + ", " + BAR_CODE + ", " + SERIALIZED + ", " + NAME + ", " + OUTSTANDING_QTY
				+ ", " + PACKAGE + ", " + PARENT_ID + ", " + // 37
				UNIT_COST_MA + ", " + UNIT_PRICE_STD + ", " + UNIT_DISCOUNT + ", " + UNIT_COMMISSION + ", "
				+ CODE_PROJECT + ", " + CODE_DEPARTMENT + ", " + CODE_DEALER + ", " + CODE_SALESMAN + ", " + STK_ID
				+ ", " + STK_LOCATION_ID + ", " + STK_LOCATION_CODE + ", " + BOM_CONVERT_MODE + ", " + BOM_ID + ", "
				+ BOM_CONVERT_STATUS + ", " + BOM_CONVERT_TIME + ", " + BOM_CONVERT_USER + ", " + BOM_TARGET_LOC + ", "
				+ BOM_SOURCE_LOC + ", " + WARRANTY_TYPE + ", " + WARRANTY_OPTION + ", " + WARRANTY_EXPIRY + ", "
				+ PSEUDO_LOGIC + ", "+ PSEUDO_CODE + ", " + PSEUDO_NAME +", "+ PSEUDO_DESCRIPTION + ", "
				+ PSEUDO_CURRENCY + ", "+ PSEUDO_PRICE +", "+ PSEUDO_QTY + ", "
				+ LOYALTY_LOGIC + ", " + LOYALTY_POINTS_AWARDED+", " + LOYALTY_POINTS_REDEEMED
				+ ", " + PACKAGE_GROUP + ") values ( " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
				+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
				+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " 
				+ " ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setLong(1, nextPKId.longValue());
		prepStmt.setLong(2, newObj.mInvoiceId.longValue());
		prepStmt.setInt(3, newObj.mPosItemId.intValue());
		prepStmt.setString(4, newObj.mRemarks);
		prepStmt.setBigDecimal(5, newObj.mTotalQty);
		prepStmt.setString(6, newObj.mCurrency);
		prepStmt.setBigDecimal(7, newObj.mUnitPriceQuoted);
		prepStmt.setString(8, newObj.mStrName1);
		prepStmt.setString(9, newObj.mStrValue1);
		prepStmt.setInt(10, newObj.mPic1.intValue());
		prepStmt.setInt(11, newObj.mPic2.intValue());
		prepStmt.setInt(12, newObj.mPic3.intValue());
		prepStmt.setString(13, newObj.mCurrency2);
		prepStmt.setBigDecimal(14, newObj.mUnitPriceQuoted2);
		prepStmt.setBigDecimal(15, newObj.mTaxAmt);
		prepStmt.setBigDecimal(16, newObj.mTaxAmt2);
		prepStmt.setString(17, newObj.mStrName2);
		prepStmt.setString(18, newObj.mStrValue2);
		prepStmt.setString(19, newObj.mStrName3);
		prepStmt.setString(20, newObj.mStrValue3);
		prepStmt.setString(21, newObj.mIntName1);
		prepStmt.setInt(22, newObj.mIntValue1.intValue());
		prepStmt.setString(23, newObj.mIntName2);
		prepStmt.setInt(24, newObj.mIntValue2.intValue());
		prepStmt.setString(25, newObj.mBdName1);
		prepStmt.setBigDecimal(26, newObj.mBdValue1);
		prepStmt.setString(27, newObj.mStatus);
		prepStmt.setString(28, newObj.mState);
		prepStmt.setString(29, newObj.mPosItemType);
		prepStmt.setInt(30, newObj.mItemId.intValue());
		prepStmt.setString(31, newObj.mItemCode);
		prepStmt.setString(32, newObj.mBarCode);
		prepStmt.setBoolean(33, newObj.mSerialized);
		prepStmt.setString(34, newObj.mName);
		prepStmt.setBigDecimal(35, newObj.mOutstandingQty);
		prepStmt.setBoolean(36, newObj.mPackage);
		prepStmt.setLong(37, newObj.mParentId.longValue());
		prepStmt.setBigDecimal(38, newObj.mUnitCostMa);
		prepStmt.setBigDecimal(39, newObj.mUnitPriceStd);
		prepStmt.setBigDecimal(40, newObj.mUnitDiscount);
		prepStmt.setBigDecimal(41, newObj.mUnitCommission);
		prepStmt.setString(42, newObj.codeProject);
		prepStmt.setString(43, newObj.codeDepartment);
		prepStmt.setString(44, newObj.codeDealer);
		prepStmt.setString(45, newObj.codeSalesman);
		prepStmt.setInt(46, newObj.stkId.intValue());
		prepStmt.setInt(47, newObj.stkLocationId.intValue());
		prepStmt.setString(48, newObj.stkLocationCode);
		prepStmt.setString(49, newObj.bomConvertMode);
		prepStmt.setInt(50, newObj.bomId.intValue());
		prepStmt.setString(51, newObj.bomConvertStatus);
		prepStmt.setTimestamp(52, newObj.bomConvertTime);
		prepStmt.setInt(53, newObj.bomConvertUser.intValue());
		prepStmt.setInt(54, newObj.bomTargetLoc.intValue());
		prepStmt.setInt(55, newObj.bomSourceLoc.intValue());
		prepStmt.setString(56, newObj.warrantyType);
		prepStmt.setString(57, newObj.warrantyOption);
		prepStmt.setTimestamp(58, newObj.warrantyExpiry);
		prepStmt.setString(59, newObj.pseudoLogic);
		prepStmt.setString(60, newObj.pseudoCode );
		prepStmt.setString(61, newObj.pseudoName);
		prepStmt.setString(62, newObj.pseudoDescription);
		prepStmt.setString(63, newObj.pseudoCurrency);
		prepStmt.setBigDecimal(64, newObj.pseudoPrice);
		prepStmt.setBigDecimal(65, newObj.pseudoQty);
		prepStmt.setString(66, newObj.loyaltyLogic);
		prepStmt.setBigDecimal(67, newObj.loyaltyPointsAwarded);
		prepStmt.setBigDecimal(68, newObj.loyaltyPointsRedeemed);
		if(newObj.package_group!=null)
		{
			prepStmt.setString(69, newObj.package_group);
		}
		else
		{
			prepStmt.setString(69, "");
		}
		prepStmt.executeUpdate();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving insertObject: ");
		return nextPKId;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
		makeConnection();
		String selectStatement = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, primaryKey.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = rs.next();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
		return result;
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " deleteObject: ");
		makeConnection();
		String deleteStatement = "delete from " + TABLENAME + " where " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving deleteObject: ");
	}

	// 20080131 Jimmy
	private GrossProfitBySupervisorTechnicianDetailsForm selectGrossProfitBySupervisorTechnicianDetailsForm
			(GrossProfitBySupervisorTechnicianDetailsForm gpstd) throws NamingException, SQLException, Exception
	{
		Log.printVerbose(strObjectName + " get gross profit report by supervisor/technician details");

		Timestamp dayNextTo = TimeFormat.add(gpstd.getDateTo(),0,0,1);
		
		makeConnection();		
	
		String selectStatement = " SELECT " +
							   	 " rpt3.supervisor, " +
								 " rpt3.technician1, " +
								 " rpt3.technician2, " +
								 " rpt3.invoice_id, " +
								 " rpt3.date, " +
								 " rpt3.item_code, " +
								 " rpt3.qty_sales, " +
								 " rpt3.unit_price_sale, " +
								 " rpt3.unit_price_sale_no_tax, " +
								 " rpt3.invitem_id, " +
								 " delta.unit_cost_ma As unit_cost_sale " +
								 " FROM " +
								 " ( " + 
								 " SELECT " +
								 " rpt2.pic1 AS supervisor, " +
								 " rpt2.pic2 AS technician1, " +
								 " rpt2.pic3 AS technician2, " +
								 " rpt1.pkid AS invoice_id, " +
								 " rpt1.time_issued AS date, " +
								 " rpt2.item_code, " +
								 " rpt2.total_quantity AS qty_sales, " +
								 " rpt2.unit_price_quoted AS unit_price_sale, " +
								 " (rpt2.unit_price_std - rpt2.unit_discount) AS unit_price_sale_no_tax, " +
								 " rpt2.pkid AS invitem_id " +
								 " FROM " + 
								 " ( " + 
								 " SELECT * FROM cust_invoice_index WHERE " +
								 " time_issued >= '" + gpstd.getDateFrom("") + "' AND " +
								 " time_issued < '" + TimeFormat.strDisplayDate(dayNextTo) + "'"; 
		
		if (gpstd.getBranchId().compareTo(new Integer(0)) > 0)
		{
			selectStatement += " AND cust_svcctr_id = " + gpstd.getBranchId().toString();
		}
		
		selectStatement	+=		 " ) AS rpt1 " +
								 " INNER JOIN cust_invoice_item AS rpt2 " +
								 " ON (rpt1.pkid = rpt2.invoice_id) ";
		
		if (gpstd.getSalesmanId().compareTo(new Integer(0)) > 0)
		{	
			if (gpstd.getSalesType().equals(gpstd.SALES_SUPERVISOR))
			{
				selectStatement += " WHERE rpt2.pic1 = " + gpstd.getSalesmanId().toString();
			}
			else
			{
				selectStatement += " WHERE rpt2.pic2 = " + gpstd.getSalesmanId().toString() + " OR rpt2.pic3 = " + gpstd.getSalesmanId().toString();  
			}
		}

		selectStatement	+=		 " ) AS rpt3 " +
								 " INNER JOIN inv_stock_delta AS delta " +
								 " ON (rpt3.invitem_id = delta.doc_key) " +
								 " WHERE delta.doc_table = 'cust_invoice_item'";
		
		selectStatement	+=		 " ORDER BY rpt3.date, rpt3.invoice_id";
								 
		Log.printVerbose(selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		
		while (rs.next())
		{
			try
			{
				GrossProfitBySupervisorTechnicianDetailsForm.RptRow rptRow = new GrossProfitBySupervisorTechnicianDetailsForm.RptRow();  
				rptRow.supervisorId = new Integer(rs.getInt("supervisor"));
				rptRow.technician1Id = new Integer(rs.getInt("technician1"));
				rptRow.technician2Id = new Integer(rs.getInt("technician2"));
				rptRow.docId = new Long(rs.getLong("invoice_id"));
				rptRow.docDate = rs.getTimestamp("date");
				rptRow.itemCode = rs.getString("item_code");
				rptRow.qty = new Integer(rs.getInt("qty_sales"));
				rptRow.unitPrice = rs.getBigDecimal("unit_price_sale");
				rptRow.unitPriceNoTax = rs.getBigDecimal("unit_price_sale_no_tax");
				rptRow.invItemId = new Long(rs.getLong("invitem_id"));
				rptRow.unitCost = rs.getBigDecimal("unit_cost_sale");
			
				gpstd.vecInvoice.add(rptRow);
				
			} catch (Exception ex)
			{
				prepStmt.close();
				ex.printStackTrace();
				throw new Exception("Unable to load object!!");
			}
		}
		
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " done");
		
		return gpstd;
	}	

	// 20080212 Jimmy
	private GrossProfitBySupervisorTechnicianSummaryForm selectGrossProfitBySupervisorTechnicianSummaryForm
			(GrossProfitBySupervisorTechnicianSummaryForm gpsts) throws NamingException, SQLException, Exception
	{
		Log.printVerbose(strObjectName + " get gross profit report by supervisor/technician summary");
		
		Timestamp dayNextTo = TimeFormat.add(gpsts.getDateTo(),0,0,1);
		
		makeConnection();		
		
		String selectStatement = " SELECT " +
							   	 " rpt3.supervisor, " +
								 " rpt3.technician1, " +
								 " rpt3.technician2, " +
								 " rpt3.invoice_id, " +
								 " rpt3.date, " +
								 " rpt3.item_code, " +
								 " rpt3.qty_sales, " +
								 " rpt3.unit_price_sale, " +
								 " rpt3.unit_price_sale_no_tax, " +
								 " rpt3.invitem_id, " +
								 " delta.unit_cost_ma As unit_cost_sale " +
								 " FROM " +
								 " ( " + 
								 " SELECT " +
								 " rpt2.pic1 AS supervisor, " +
								 " rpt2.pic2 AS technician1, " +
								 " rpt2.pic3 AS technician2, " +
								 " rpt1.pkid AS invoice_id, " +
								 " rpt1.time_issued AS date, " +
								 " rpt2.item_code, " +
								 " rpt2.total_quantity AS qty_sales, " +
								 " rpt2.unit_price_quoted AS unit_price_sale, " +
								 " (rpt2.unit_price_std - rpt2.unit_discount) AS unit_price_sale_no_tax, " +
								 " rpt2.pkid AS invitem_id " +
								 " FROM " + 
								 " ( " + 
								 " SELECT * FROM cust_invoice_index WHERE " +
								 " time_issued >= '" + gpsts.getDateFrom("") + "' AND " +
								 " time_issued < '" + TimeFormat.strDisplayDate(dayNextTo) + "'"; 
		
		if (gpsts.getBranchId().compareTo(new Integer(0)) > 0)
		{
			selectStatement += " AND cust_svcctr_id = " + gpsts.getBranchId().toString();
		}
		
		selectStatement	+=		 " ) AS rpt1 " +
								 " INNER JOIN cust_invoice_item AS rpt2 " +
								 " ON (rpt1.pkid = rpt2.invoice_id) ";
		
		if (gpsts.getSalesmanId().compareTo(new Integer(0)) > 0)
		{	
			if (gpsts.getSalesType().equals(gpsts.SALES_SUPERVISOR))
			{
				selectStatement += " WHERE rpt2.pic1 = " + gpsts.getSalesmanId().toString();
			}
			else
			{
				selectStatement += " WHERE rpt2.pic2 = " + gpsts.getSalesmanId().toString() + " OR rpt2.pic3 = " + gpsts.getSalesmanId().toString();  
			}
		}
		
		selectStatement	+=		 " ) AS rpt3 " +
								 " INNER JOIN inv_stock_delta AS delta " +
								 " ON (rpt3.invitem_id = delta.doc_key) " +
								 " WHERE delta.doc_table = 'cust_invoice_item'";
		
		selectStatement	+=		 " ORDER BY rpt3.date, rpt3.invoice_id";
								 
		Log.printVerbose(selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		
		while (rs.next())
		{
			try
			{
				GrossProfitBySupervisorTechnicianSummaryForm.RptRow rptRow = new GrossProfitBySupervisorTechnicianSummaryForm.RptRow();  
				rptRow.supervisorId = new Integer(rs.getInt("supervisor"));
				rptRow.technician1Id = new Integer(rs.getInt("technician1"));
				rptRow.technician2Id = new Integer(rs.getInt("technician2"));
				rptRow.docId = new Long(rs.getLong("invoice_id"));
				rptRow.docDate = rs.getTimestamp("date");
				rptRow.itemCode = rs.getString("item_code");
				rptRow.qty = new Integer(rs.getInt("qty_sales"));
				rptRow.unitPrice = rs.getBigDecimal("unit_price_sale");
				rptRow.unitPriceNoTax = rs.getBigDecimal("unit_price_sale_no_tax");
				rptRow.invItemId = new Long(rs.getLong("invitem_id"));
				rptRow.unitCost = rs.getBigDecimal("unit_cost_sale");
			
				gpsts.vecInvoice.add(rptRow);
				
			} catch (Exception ex)
			{
				prepStmt.close();
				ex.printStackTrace();
				throw new Exception("Unable to load object!!");
			}
		}
		
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " done");
		
		return gpsts;
		}
	
	// 20080304 Jimmy
	private GrossProfitByDepartmentDetailsForm selectGrossProfitByDepartmentDetailsForm
				(GrossProfitByDepartmentDetailsForm gpdd) throws NamingException, SQLException, Exception
	{		
		Log.printVerbose(strObjectName + " get gross profit report by department details");
		
		Timestamp dayNextTo = TimeFormat.add(gpdd.getDateTo(),0,0,1);
		
		makeConnection();
		
		String sltStmt = " SELECT " +  
						 " rpt3.code_department,  rpt3.invoice_id,  rpt3.date,  rpt3.item_code,  rpt3.qty_sales,  rpt3.unit_price_sale,  rpt3.unit_price_sale_no_tax, rpt3.invitem_id,  delta.unit_cost_ma As unit_cost_sale " +  
						 " FROM " +  
						 " ( " +  
						 " SELECT " +  
						 " rpt2.code_department, rpt1.pkid AS invoice_id,  rpt1.time_issued AS date,  rpt2.item_code,  rpt2.total_quantity AS qty_sales,  rpt2.unit_price_quoted AS unit_price_sale,  (rpt2.unit_price_std - rpt2.unit_discount) AS unit_price_sale_no_tax,  rpt2.pkid AS invitem_id " +  
						 " FROM " +  
						 " ( " +  
						 " SELECT * FROM cust_invoice_index WHERE " +  
						 " time_issued >= '" + gpdd.getDateFrom("") + "' AND " +
						 " time_issued < '" + TimeFormat.strDisplayDate(dayNextTo) + "'"; 

		if (gpdd.getBranchId().compareTo(new Integer(0)) > 0)
		{
				sltStmt += " AND cust_svcctr_id = " + gpdd.getBranchId().toString();
		}
		
		
		sltStmt +=		 " ) AS rpt1 " +  
						 " INNER JOIN cust_invoice_item AS rpt2 " +  
						 " ON (rpt1.pkid = rpt2.invoice_id) " +  
						 " ) AS rpt3 " +  
						 " INNER JOIN inv_stock_delta AS delta " +  
						 " ON (rpt3.invitem_id = delta.doc_key) " +  
						 " WHERE " + 
						 " delta.doc_table = 'cust_invoice_item' "; 
		 
		if (!gpdd.getDeptCode().equals(""))
		{
			sltStmt += " AND rpt3.code_department = '" + gpdd.getDeptCode() + "'";
		}
		
		sltStmt += " ORDER BY rpt3.date, rpt3.invoice_id ";
		
		Log.printVerbose(sltStmt);
		PreparedStatement prepStmt = con.prepareStatement(sltStmt);
		ResultSet rs = prepStmt.executeQuery();
		
		while (rs.next())
		{			
			try
			{
				GrossProfitByDepartmentDetailsForm.RptRow rptRow = new GrossProfitByDepartmentDetailsForm.RptRow();
				
				rptRow.deptCode = rs.getString("code_department");
				rptRow.docId = new Long(rs.getLong("invoice_id"));
				rptRow.docDate = rs.getTimestamp("date");
				rptRow.itemCode = rs.getString("item_code");
				rptRow.qty = new Integer(rs.getInt("qty_sales"));
				rptRow.unitCost =  rs.getBigDecimal("unit_cost_sale");
				rptRow.unitPrice =  rs.getBigDecimal("unit_price_sale");
				rptRow.unitPriceNoTax =  rs.getBigDecimal("unit_price_sale_no_tax");
				rptRow.invItemId = new Long(rs.getLong("invitem_id"));
				
				gpdd.vecInvoice.add(rptRow);
				
			} catch (Exception ex)
			{
				prepStmt.close();
				ex.printStackTrace();
				throw new Exception("Unable to load object!!");
			}
			
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " done");
		return gpdd;
	}	

	// 20080305 Jimmy
	private GrossProfitByDepartmentSummaryForm selectGrossProfitByDepartmentSummaryForm
				(GrossProfitByDepartmentSummaryForm gpdd) throws NamingException, SQLException, Exception
	{		
		Log.printVerbose(strObjectName + " get gross profit report by department summary");
		
		Timestamp dayNextTo = TimeFormat.add(gpdd.getDateTo(),0,0,1);
		
		makeConnection();
		
		String sltStmt = " SELECT " +  
						 " rpt3.code_department,  rpt3.invoice_id,  rpt3.date,  rpt3.item_code,  rpt3.qty_sales,  rpt3.unit_price_sale,  rpt3.unit_price_sale_no_tax, rpt3.invitem_id,  delta.unit_cost_ma As unit_cost_sale " +  
						 " FROM " +  
						 " ( " +  
						 " SELECT " +  
						 " rpt2.code_department, rpt1.pkid AS invoice_id,  rpt1.time_issued AS date,  rpt2.item_code,  rpt2.total_quantity AS qty_sales,  rpt2.unit_price_quoted AS unit_price_sale,  (rpt2.unit_price_std - rpt2.unit_discount) AS unit_price_sale_no_tax,  rpt2.pkid AS invitem_id " +  
						 " FROM " +  
						 " ( " +  
						 " SELECT * FROM cust_invoice_index WHERE " +  
						 " time_issued >= '" + gpdd.getDateFrom("") + "' AND " +
						 " time_issued < '" + TimeFormat.strDisplayDate(dayNextTo) + "'"; 

		if (gpdd.getBranchId().compareTo(new Integer(0)) > 0)
		{
				sltStmt += " AND cust_svcctr_id = " + gpdd.getBranchId().toString();
		}
		
		
		sltStmt +=		 " ) AS rpt1 " +  
						 " INNER JOIN cust_invoice_item AS rpt2 " +  
						 " ON (rpt1.pkid = rpt2.invoice_id) " +  
						 " ) AS rpt3 " +  
						 " INNER JOIN inv_stock_delta AS delta " +  
						 " ON (rpt3.invitem_id = delta.doc_key) " +  
						 " WHERE " + 
						 " delta.doc_table = 'cust_invoice_item' "; 
		 
		if (!gpdd.getDeptCode().equals(""))
		{
			sltStmt += " AND rpt3.code_department = '" + gpdd.getDeptCode() + "'";
		}
		
		sltStmt += " ORDER BY rpt3.date, rpt3.invoice_id ";
		
		Log.printVerbose(sltStmt);
		PreparedStatement prepStmt = con.prepareStatement(sltStmt);
		ResultSet rs = prepStmt.executeQuery();
		
		while (rs.next())
		{			
			try
			{
				GrossProfitByDepartmentSummaryForm.RptRow rptRow = new GrossProfitByDepartmentSummaryForm.RptRow();
				
				rptRow.deptCode = rs.getString("code_department");
				rptRow.docId = new Long(rs.getLong("invoice_id"));
				rptRow.docDate = rs.getTimestamp("date");
				rptRow.itemCode = rs.getString("item_code");
				rptRow.qty = new Integer(rs.getInt("qty_sales"));
				rptRow.unitCost =  rs.getBigDecimal("unit_cost_sale");
				rptRow.unitPrice =  rs.getBigDecimal("unit_price_sale");
				rptRow.unitPriceNoTax =  rs.getBigDecimal("unit_price_sale_no_tax");
				rptRow.invItemId = new Long(rs.getLong("invitem_id"));
				
				gpdd.vecInvoice.add(rptRow);
				
			} catch (Exception ex)
			{
				prepStmt.close();
				ex.printStackTrace();
				throw new Exception("Unable to load object!!");
			}
			
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " done");
		return gpdd;
	}
	
	// 20080415 Jimmy
	private GrossProfitBySalesmanByInvoiceReport selectGrossProfitBySalesmanByInvoiceReport
				(GrossProfitBySalesmanByInvoiceReport gpsi) throws NamingException, SQLException, Exception
	{		
		Log.printVerbose(strObjectName + " get gross profit report by Salesman by Invoice");
		
		Timestamp dayNextTo = TimeFormat.add(gpsi.getDateTo(),0,0,1);
		
		makeConnection();
		
		String sltStmt = " SELECT " +  
						 " delta.person_in_charge AS salesman_id,  rpt3.invoice_id,  rpt3.date,  rpt3.item_code,  rpt3.qty_sales,  rpt3.unit_price_sale, rpt3.unit_price_sale_no_tax, rpt3.invitem_id,  delta.unit_cost_ma As unit_cost_sale, " +  
						 " rpt3.unit_price_std, rpt3.unit_discount,  rpt3.unit_commission, rpt3.name, rpt3.remarks " +
						 " FROM " +  
						 " ( " +  
						 " SELECT " +  
						 " rpt1.userid_edit AS salesman_id, rpt1.pkid AS invoice_id,  rpt1.time_issued AS date,  rpt2.item_code,  " + 
						 " rpt2.total_quantity AS qty_sales,  rpt2.unit_price_quoted AS unit_price_sale, (rpt2.unit_price_std - rpt2.unit_discount) AS unit_price_sale_no_tax, rpt2.pkid AS invitem_id, " +  
						 " rpt2.unit_price_std, rpt2.unit_discount,  rpt2.unit_commission, rpt2.name, rpt2.remarks " +
						 " FROM " +  
						 " ( " +  
						 " SELECT * FROM cust_invoice_index WHERE " +  
						 " time_issued >= '" + TimeFormat.strDisplayDate(gpsi.getDateFrom()) + "' AND " +
						 " time_issued < '" + TimeFormat.strDisplayDate(dayNextTo) + "'"; 

		if (gpsi.getBranch() != null)
		{
				sltStmt += " AND cust_svcctr_id = " + gpsi.getBranch().pkid;
		}
		
		sltStmt +=		 " ) AS rpt1 " +  
						 " INNER JOIN cust_invoice_item AS rpt2 " +  
						 " ON (rpt1.pkid = rpt2.invoice_id) " +  
						 " ) AS rpt3 " +  
						 " INNER JOIN inv_stock_delta AS delta " +  
						 " ON (rpt3.invitem_id = delta.doc_key) " +  
						 " WHERE " + 
						 " delta.doc_table = 'cust_invoice_item' "; 
		
		if (gpsi.getSalesmanId().compareTo(new Integer(0)) > 0)
		{
			sltStmt +=  " AND delta.person_in_charge = " + gpsi.getSalesmanId();
		}
		
		sltStmt += " ORDER BY rpt3.date, rpt3.invoice_id ";
		
		Log.printVerbose(sltStmt);
		PreparedStatement prepStmt = con.prepareStatement(sltStmt);
		ResultSet rs = prepStmt.executeQuery();
		
		while (rs.next())
		{			
			try
			{
				GrossProfitBySalesmanByInvoiceReport.RptRow rptRow = new GrossProfitBySalesmanByInvoiceReport.RptRow();
				
				rptRow.salesmanId = new Integer(rs.getInt("salesman_id"));
				rptRow.docId = new Long(rs.getLong("invoice_id"));
				rptRow.docDate = rs.getTimestamp("date");
				rptRow.itemCode = rs.getString("item_code");
				rptRow.qty = rs.getBigDecimal("qty_sales");
				rptRow.unitCost =  rs.getBigDecimal("unit_cost_sale");
				rptRow.unitPrice =  rs.getBigDecimal("unit_price_sale");
				rptRow.invItemId = new Long(rs.getLong("invitem_id"));
				rptRow.unitPriceNoTax =  rs.getBigDecimal("unit_price_sale_no_tax");
				rptRow.unitPriceStd = rs.getBigDecimal("unit_price_std");
				rptRow.unitDiscount = rs.getBigDecimal("unit_discount");
				rptRow.unitCommission = rs.getBigDecimal("unit_commission");
				rptRow.name = rs.getString("name");
				rptRow.remarks = rs.getString("remarks");
				
				gpsi.vecInvoice.add(rptRow);
				
			} catch (Exception ex)
			{
				prepStmt.close();
				ex.printStackTrace();
				throw new Exception("Unable to load object!!");
			}
			
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " done");
		return gpsi;
	}	

	private DeptCodeReport selectDeptCodeReport(DeptCodeReport dcr) throws NamingException, SQLException
	{
		makeConnection();
		String sltStmt = "SELECT cii.str_value_1 AS deptcode," + " positem.item_type, count(cii.pkid) AS number, "
				+ " sum(total_quantity) AS qty, " + " sum(total_quantity*unit_price_quoted) AS value "
				+ " FROM cust_invoice_item AS cii INNER JOIN " + " cust_pos_item_index AS positem ON  "
				+ " (positem.pkid = cii.pos_item_id)  " + " WHERE cii.invoice_id IN "
				+ " (SELECT ci.pkid FROM cust_invoice_index AS ci " + " INNER JOIN cust_sales_txn_index AS stxn ON "
				+ " (stxn.pkid = ci.sales_txn_id) WHERE " + " ci.status = 'active' ";
		if (dcr.dateAfterEqual != null)
		{
			sltStmt += " AND ci.time_issued >= '" + TimeFormat.strDisplayDate(dcr.dateAfterEqual) + "' ";
		}
		if (dcr.dateBefore != null)
		{
			sltStmt += " AND ci.time_issued < '" + TimeFormat.strDisplayDate(dcr.dateBefore) + "' ";
		}
		if (dcr.custSvcCtr != null)
		{
			sltStmt += " AND stxn.cust_svcctr_id = '" + dcr.custSvcCtr.toString() + "' ";
		}
		sltStmt += " ) " + " AND cii.str_name_1 = 'dept_code' " + " AND cii.status = 'active' "
				+ " GROUP BY cii.str_value_1, positem.item_type ";
		PreparedStatement prepStmt = con.prepareStatement(sltStmt);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			String deptCode = rs.getString("deptcode");
			String itemType = rs.getString("item_type");
			BigDecimal bdNumber = rs.getBigDecimal("number");
			BigDecimal bdQty = rs.getBigDecimal("qty");
			BigDecimal bdValue = rs.getBigDecimal("value");
			dcr.vecDeptCode.add(deptCode);
			dcr.vecPOSType.add(itemType);
			dcr.vecNumber.add(bdNumber);
			dcr.vecQuantity.add(bdQty);
			dcr.vecValue.add(bdValue);
		}
		prepStmt.close();
		closeConnection();
		return dcr;
	}

	// /////////////////////////////////////////////
	// 20080616 Jimmy
	private SalesRecordsByCustomerReport selectSalesRecordsByCustomerReport(
			SalesRecordsByCustomerReport srbcr) throws NamingException, SQLException
	{
		makeConnection();
		Timestamp dayNextTo = TimeFormat.add(srbcr.getDateTo(),0,0,1);
/*		String sltStmt = "SELECT branch.code,item.* FROM ( SELECT invoice.pkid AS invoiceid,"
				+ "invoice.cust_svcctr_id AS branch, invoice.time_issued AS date," + "invoice.reference_no, "
				+ "invoice_item.pos_item_id, invoice_item.item_type,invoice_item.item_id,"
				+ "invoice_item.item_code,invoice_item.total_quantity,invoice_item.name,"
				+ "invoice_item.unit_price_quoted FROM "
				+ " cust_invoice_index AS invoice INNER JOIN cust_invoice_item AS invoice_item ON "
				+ "(invoice.pkid = invoice_item.invoice_id) "
				+ " INNER JOIN inv_item ON (inv_item.pkid = invoice_item.item_id) "
				+ " WHERE " + " invoice.entity_key='" 
				+ srbcr.getCustomer().pkid + "' "; */

		String sltStmt = "SELECT branch.code, rpt1.* FROM "
		+ "(SELECT item.*, delta.* FROM "
		+ "(SELECT invoice.pkid AS invoiceid, "
		+ "invoice.cust_svcctr_id AS branch, invoice.time_issued AS date, "
		+ "invoice.reference_no, invoice_item.pos_item_id, invoice_item.item_type, "
		+ "invoice_item.item_id,invoice_item.item_code, invoice_item.total_quantity, "
		+ "invoice_item.name, invoice_item.unit_price_quoted, invoice_item.pkid AS invoiceitemid "
		+ "FROM cust_invoice_index AS invoice INNER JOIN cust_invoice_item AS invoice_item ON "
		+ "(invoice.pkid = invoice_item.invoice_id) INNER JOIN inv_item ON (inv_item.pkid = invoice_item.item_id) "
		+ "WHERE invoice.entity_key='"+ srbcr.getCustomer().pkid + "' ";
		//+ "and invoice.time_issued >= '2006-01-23' "
		//+ "and invoice.time_issued < '2008-06-24' "

		
		
		
		if(srbcr.getDateFrom() != null)
		{
			sltStmt += " and  invoice.time_issued >= '" + srbcr.getDateFrom("") + "' ";
		}

		if(srbcr.getDateTo() != null)
		{
			sltStmt += " and invoice.time_issued < '" + TimeFormat.strDisplayDate(dayNextTo) + "'";
		}
		if(srbcr.getUseCodeRange())
		{
			if ((srbcr.getCodeStart() != null) && (srbcr.getCodeEnd() != null))
			{
				sltStmt += "  and invoice_item.item_code >= '" + srbcr.getCodeStart() + "' ";
				sltStmt += " and invoice_item.item_code <= '" + srbcr.getCodeEnd() + "zzz'";
			}
		}

		// 20080616 Jimmy
		if(srbcr.getFilterCategory0() && srbcr.getCategory0()!=null)
		{ sltStmt += " AND inv_item.categoryid='"+ srbcr.getCategory0().toString()+"' ";}
		if(srbcr.getFilterCategory1() && srbcr.getCategory1()!=null)
		{ sltStmt += " AND inv_item.category1 = '"+srbcr.getCategory1()+"' ";}
		if(srbcr.getFilterCategory2() && srbcr.getCategory2()!=null)
		{ sltStmt += " AND inv_item.category2 = '"+srbcr.getCategory2()+"' ";}
		if(srbcr.getFilterCategory3() && srbcr.getCategory3()!=null)
		{ sltStmt += " AND inv_item.category3 = '"+srbcr.getCategory3()+"' ";}
		if(srbcr.getFilterCategory4() && srbcr.getCategory4()!=null)
		{ sltStmt += " AND inv_item.category4 = '"+srbcr.getCategory4()+"' ";}
		if(srbcr.getFilterCategory5() && srbcr.getCategory5()!=null)
		{ sltStmt += " AND inv_item.category5 = '"+srbcr.getCategory5()+"' ";}
		
		//sltStmt += " ORDER BY invoice.time_issued, invoice.pkid, invoice_item.item_code";
		//sltStmt += ") AS item INNER JOIN acc_branch_index AS branch ON (item.branch=branch.pkid)";
		//sltStmt += " order by date desc ";
		//sltStmt += " limit "+ srbcr.getLimitRecords().toString();

		sltStmt += "ORDER BY invoice.time_issued, invoice.pkid, invoice_item.item_code) ";
		sltStmt += "AS item INNER JOIN (SELECT doc_table, doc_key, unit_cost_ma from inv_stock_delta ";
		sltStmt += "WHERE doc_table = 'cust_invoice_item') AS delta ON (item.invoiceitemid = delta.doc_key)) ";
		sltStmt += "AS rpt1 INNER JOIN acc_branch_index AS branch ON (rpt1.branch=branch.pkid) order by date desc ";

		
		
		PreparedStatement prepStmt = con.prepareStatement(sltStmt);
		Log.printVerbose(sltStmt);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			SalesRecordsByCustomerReport.RptRow theRow = new SalesRecordsByCustomerReport.RptRow();
			theRow.code = rs.getString("code");// Naveen
			theRow.invoiceId = rs.getInt("invoiceid");
			theRow.reference = rs.getString("reference_no");
			theRow.branch = rs.getInt("branch");
			theRow.date = rs.getTimestamp("date");
			theRow.posItemId = rs.getInt("pos_item_id");
			theRow.itemType = rs.getString("item_type");
			theRow.itemId = rs.getInt("item_id");
			theRow.itemCode = rs.getString("item_code");
			theRow.name = rs.getString("name");
			theRow.totalquantity = rs.getBigDecimal("total_quantity");// Naveen
			theRow.price = rs.getBigDecimal("unit_price_quoted");
			theRow.cost = rs.getBigDecimal("unit_cost_ma");
			
			
			srbcr.vecRows.add(theRow);
		}
		prepStmt.close();
		closeConnection();
		
		// Get sales returns
		makeConnection();
/*
		sltStmt = " SELECT b.*, c.name, c.item_id, c.item_code, c.total_quantity, c.remarks, c.unit_amount FROM cust_invoice_index a " +
					" INNER JOIN cust_sales_return_index b ON (a.pkid = b.doc_id) " +
					" INNER JOIN cust_sales_return_item c ON (b.pkid = c.sales_return_id) " +
					" INNER JOIN inv_item ON (inv_item.pkid = c.item_id) " +
					" WHERE b.doc_table = 'cust_invoice_index' " +
					" AND a.entity_table = 'cust_account_index' " +
					" AND a.entity_key = '" + srbcr.getCustomer().pkid + "' ";
*/

		sltStmt = "SELECT cust_sales_return_index.*, cust_sales_return_item.name, cust_sales_return_item.item_id, cust_sales_return_item.item_code, cust_sales_return_item.total_quantity, cust_sales_return_item.remarks, "
			      + "cust_sales_return_item.unit_amount, inv_stock_delta.unit_cost_ma FROM cust_invoice_index INNER JOIN cust_sales_return_index ON "
			      + "(cust_invoice_index.pkid = cust_sales_return_index.doc_id)  INNER JOIN cust_sales_return_item ON (cust_sales_return_index.pkid = cust_sales_return_item.sales_return_id) "
			      + "INNER JOIN inv_item ON (inv_item.pkid = cust_sales_return_item.item_id) INNER JOIN inv_stock_delta ON (inv_stock_delta.doc_key = cust_sales_return_item.pkid)  WHERE cust_sales_return_index.doc_table = 'cust_invoice_index' "
			      + "AND cust_invoice_index.entity_table = 'cust_account_index'  AND "
			      + "cust_invoice_index.entity_key =  '" + srbcr.getCustomer().pkid + "' ";
		
		if(srbcr.getUseCodeRange())
		{
			if ((srbcr.getCodeStart() != null) && (srbcr.getCodeEnd() != null))
			{
				sltStmt += " AND cust_sales_return_item.item_code >= '" + srbcr.getCodeStart() + "' ";
				sltStmt += " AND cust_sales_return_item.item_code <= '" + srbcr.getCodeEnd() + "zzz' ";
			}
		}
		
		// 20080616 Jimmy
		if(srbcr.getFilterCategory0() && srbcr.getCategory0()!=null)
		{ sltStmt += " AND inv_item.categoryid='"+ srbcr.getCategory0().toString()+"' ";}
		if(srbcr.getFilterCategory1() && srbcr.getCategory1()!=null)
		{ sltStmt += " AND inv_item.category1 = '"+srbcr.getCategory1()+"' ";}
		if(srbcr.getFilterCategory2() && srbcr.getCategory2()!=null)
		{ sltStmt += " AND inv_item.category2 = '"+srbcr.getCategory2()+"' ";}
		if(srbcr.getFilterCategory3() && srbcr.getCategory3()!=null)
		{ sltStmt += " AND inv_item.category3 = '"+srbcr.getCategory3()+"' ";}
		if(srbcr.getFilterCategory4() && srbcr.getCategory4()!=null)
		{ sltStmt += " AND inv_item.category4 = '"+srbcr.getCategory4()+"' ";}
		if(srbcr.getFilterCategory5() && srbcr.getCategory5()!=null)
		{ sltStmt += " AND inv_item.category5 = '"+srbcr.getCategory5()+"' ";}
		
		sltStmt +=	" AND cust_sales_return_index.time_created >= '" + srbcr.getDateFrom("") + "' AND cust_sales_return_index.time_created < '" + TimeFormat.strDisplayDate(dayNextTo) + "'"; 
		prepStmt = con.prepareStatement(sltStmt);
		Log.printVerbose(sltStmt);
		rs = prepStmt.executeQuery();
		while (rs.next())
		{
			SalesRecordsByCustomerReport.RptRow theRow = new SalesRecordsByCustomerReport.RptRow();
			theRow.code = "";
			theRow.invoiceId = rs.getInt("pkid");
			theRow.reference = rs.getString("remarks");
			theRow.branch = 0;
			theRow.date = rs.getTimestamp("time_created");
			theRow.posItemId = 0;
			theRow.itemType = "";
			theRow.itemId = rs.getInt("item_id");
			theRow.itemCode = rs.getString("item_code");
			theRow.name = rs.getString("name");
			theRow.totalquantity = rs.getBigDecimal("total_quantity");// Naveen
			theRow.price = rs.getBigDecimal("unit_amount");
			theRow.cost = rs.getBigDecimal("unit_cost_ma");
			srbcr.vecSalesReturn.add(theRow);
		}
		prepStmt.close();
		closeConnection();
		
		// Get credit memos
		makeConnection();
		sltStmt = "SELECT * FROM acc_credit_memo_index WHERE " +
		CreditMemoIndexBean.TIME_CREATE + " >= '" + srbcr.getDateFrom("") + "' " +
								"AND " + CreditMemoIndexBean.TIME_CREATE + " < '" + TimeFormat.strDisplayDate(dayNextTo) + "' " +
								"AND " + CreditMemoIndexBean.ENTITY_TABLE + " = '" + CustAccountBean.TABLENAME + "' " +
								"AND " + CreditMemoIndexBean.ENTITY_KEY + " = '" + srbcr.getCustomer().pkid + "' " +
								"AND " + CreditMemoIndexBean.DOC_TABLE + " <> '" + SalesReturnBean.TABLENAME + "' ";
		prepStmt = con.prepareStatement(sltStmt);
		Log.printVerbose(sltStmt);
		rs = prepStmt.executeQuery();
		while (rs.next())
		{
			SalesRecordsByCustomerReport.RptRow theRow = new SalesRecordsByCustomerReport.RptRow();
			theRow.code = "";
			theRow.invoiceId = rs.getInt("pkid");
			theRow.reference = rs.getString("memo_remarks");
			theRow.branch = 0;
			theRow.date = rs.getTimestamp("time_create");
			theRow.posItemId = 0;
			theRow.itemType = "";
			theRow.itemId = 0;
			theRow.itemCode = "";
			theRow.name = "";
			theRow.totalquantity = new BigDecimal(0);// Naveen
			theRow.price = rs.getBigDecimal("amount");
			srbcr.vecCM.add(theRow);
		}
		prepStmt.close();
		closeConnection();		
		return srbcr;
	}
	
//	private SalesRecordsByCustomerReport.Report selectSalesRecordsByCustomerReport(
//			SalesRecordsByCustomerReport.Report theReport) throws NamingException, SQLException
//	{
//		makeConnection();
//		Timestamp dayNextTo = TimeFormat.add(theReport.dateTo,0,0,1);
//		String sltStmt = "SELECT branch.code,item.* FROM ( SELECT invoice.pkid AS invoiceid,"
//				+ "invoice.cust_svcctr_id AS branch, invoice.time_issued AS date," + "invoice.reference_no, "
//				+ "invoice_item.pos_item_id, invoice_item.item_type,invoice_item.item_id,"
//				+ "invoice_item.item_code,invoice_item.total_quantity,invoice_item.name,"
//				+ "invoice_item.unit_price_quoted FROM "
//				+ " cust_invoice_index AS invoice INNER JOIN cust_invoice_item AS invoice_item ON "
//				+ "(invoice.pkid = invoice_item.invoice_id) WHERE " + " invoice.entity_key='" 
//				+ theReport.customer.pkid + "' ";
//
//		if(theReport.dateFrom != null)
//		{
//			sltStmt += " and  invoice.time_issued > '" + theReport.dateFrom + "' ";
//		}
//
//		if(theReport.dateTo != null)
//		{
//			sltStmt += " and invoice.time_issued < '" + theReport.dateTo + "'";
//		}
//		if(theReport.getUseCodeRange())
//		{
//			if ((theReport.codeStart != null) && (theReport.codeEnd != null))
//			{
//				sltStmt += "  and invoice_item.item_code >= '" + theReport.codeStart + "' ";
//				sltStmt += " and invoice_item.item_code <= '" + theReport.codeEnd + "zzz'";
//			}
//		}
//
//		sltStmt += " ORDER BY invoice.time_issued, invoice.pkid, invoice_item.item_code";
//		sltStmt += ") AS item INNER JOIN acc_branch_index AS   branch ON (item.branch=branch.pkid)";
//		sltStmt += " order by date desc ";
//		sltStmt += " limit "+ theReport.limitRecords.toString();
//
//		PreparedStatement prepStmt = con.prepareStatement(sltStmt);
//		ResultSet rs = prepStmt.executeQuery();
//		while (rs.next())
//		{
//			SalesRecordsByCustomerReport.Report.ItemRow theRow = new SalesRecordsByCustomerReport.Report.ItemRow();
//			theRow.code = rs.getString("code");// Naveen
//			theRow.invoiceId = rs.getInt("invoiceid");
//			theRow.reference = rs.getString("reference_no");
//			theRow.branch = rs.getInt("branch");
//			theRow.date = rs.getTimestamp("date");
//			theRow.posItemId = rs.getInt("pos_item_id");
//			theRow.itemType = rs.getString("item_type");
//			theRow.itemId = rs.getInt("item_id");
//			theRow.itemCode = rs.getString("item_code");
//			theRow.name = rs.getString("name");
//			theRow.totalquantity = rs.getBigDecimal("total_quantity");// Naveen
//			theRow.price = rs.getBigDecimal("unit_price_quoted");
//			theReport.vecRows.add(theRow);
//		}
//		prepStmt.close();
//		closeConnection();
//		
//		// Get sales returns
//		makeConnection();
//		sltStmt = "SELECT b.*, c.name, c.item_id, c.item_code, c.total_quantity, c.remarks, c.unit_amount FROM cust_invoice_index a " +
//					"INNER JOIN cust_sales_return_index b ON (a.pkid = b.doc_id) " +
//					"INNER JOIN cust_sales_return_item c ON (b.pkid = c.sales_return_id) " +
//					"WHERE b.doc_table = 'cust_invoice_index' " +
//					"AND a.entity_table = 'cust_account_index' " +
//					"AND a.entity_key = '" + theReport.customer.pkid + "' ";
//		if(theReport.getUseCodeRange())
//		{
//			if ((theReport.codeStart != null) && (theReport.codeEnd != null))
//			{
//				sltStmt += "  AND c.item_code >= '" + theReport.codeStart + "' ";
//				sltStmt += " AND c.item_code <= '" + theReport.codeEnd + "zzz' ";
//			}
//		}
//		sltStmt +=	"AND b.time_created >= '" + theReport.dateFrom + "' AND b.time_created < '" + theReport.dateTo + "'"; 
//		prepStmt = con.prepareStatement(sltStmt);
//		Log.printVerbose(sltStmt);
//		rs = prepStmt.executeQuery();
//		while (rs.next())
//		{
//			SalesRecordsByCustomerReport.Report.ItemRow theRow = new SalesRecordsByCustomerReport.Report.ItemRow();
//			theRow.code = "";
//			theRow.invoiceId = rs.getInt("pkid");
//			theRow.reference = rs.getString("remarks");
//			theRow.branch = 0;
//			theRow.date = rs.getTimestamp("time_created");
//			theRow.posItemId = 0;
//			theRow.itemType = "";
//			theRow.itemId = rs.getInt("item_id");
//			theRow.itemCode = rs.getString("item_code");
//			theRow.name = rs.getString("name");
//			theRow.totalquantity = rs.getBigDecimal("total_quantity");// Naveen
//			theRow.price = rs.getBigDecimal("unit_amount");
//			theReport.vecSalesReturn.add(theRow);
//		}
//		prepStmt.close();
//		closeConnection();
//		
//		// Get credit memos
//		makeConnection();
//		sltStmt = "SELECT * FROM acc_credit_memo_index WHERE " +
//		CreditMemoIndexBean.TIME_CREATE + " >= '" + theReport.dateFrom + "' " +
//		"AND " + CreditMemoIndexBean.TIME_CREATE + " < '" + theReport.dateTo + "' " +
//		"AND " + CreditMemoIndexBean.ENTITY_TABLE + " = '" + CustAccountBean.TABLENAME + "' " +
//		"AND " + CreditMemoIndexBean.ENTITY_KEY + " = '" + theReport.customer.pkid + "' " +
//		"AND " + CreditMemoIndexBean.DOC_TABLE + " <> '" + SalesReturnBean.TABLENAME + "' ";
//		prepStmt = con.prepareStatement(sltStmt);
//		Log.printVerbose(sltStmt);
//		rs = prepStmt.executeQuery();
//		while (rs.next())
//		{
//			SalesRecordsByCustomerReport.Report.ItemRow theRow = new SalesRecordsByCustomerReport.Report.ItemRow();
//			theRow.code = "";
//			theRow.invoiceId = rs.getInt("pkid");
//			theRow.reference = rs.getString("memo_remarks");
//			theRow.branch = 0;
//			theRow.date = rs.getTimestamp("time_create");
//			theRow.posItemId = 0;
//			theRow.itemType = "";
//			theRow.itemId = 0;
//			theRow.itemCode = "";
//			theRow.name = "";
//			theRow.totalquantity = new BigDecimal(0);// Naveen
//			theRow.price = rs.getBigDecimal("amount");
//			theReport.vecCM.add(theRow);
//		}
//		prepStmt.close();
//		closeConnection();		
//		return theReport;
//	}

	// ////////////////////////////////////////////
	private Vector selectValueObjectsGiven(String posType, Integer posItemId, Integer svcCtrId, Timestamp dateFrom,
			Timestamp dateTo) throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Log.printVerbose(strObjectName + " loadObject: ");
		makeConnection();
		String selectStatement = "select " + "iitm2.* "
				+ " FROM (SELECT iitm.*,iidx.sales_txn_id from cust_invoice_item"
				+ " as iitm INNER JOIN cust_invoice_index as iidx ON "
				+ "(iitm.invoice_id = iidx.pkid) where iitm.pos_item_id " + " = '" + posItemId.toString() + "' ";
		if (dateFrom != null)
		{
			selectStatement += " AND iidx.time_issued>='" + TimeFormat.strDisplayDate(dateFrom) + "' ";
		}
		if (dateTo != null)
		{
			selectStatement += " AND iidx.time_issued<='" + TimeFormat.strDisplayDate(dateTo) + "' ";
		}
		selectStatement += ") AS iitm2 INNER JOIN cust_sales_txn_index AS stxn ON (iitm2.sales_txn_id= stxn.pkid)";
		if (svcCtrId != null)
		{
			selectStatement += " WHERE stxn.cust_svcctr_id='" + svcCtrId.toString() + "' ";
		}
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				InvoiceItemObject iiObj = getObject(rs, "");
				vecValObj.add(iiObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return vecValObj;
	}

	// /////////////////////////////////////////////
	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3, String fieldName4, String fuzzy4) throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Log.printVerbose(strObjectName + " loadObject: ");
		makeConnection();
		String selectStatement = " SELECT * " + " FROM " + TABLENAME + " WHERE " + fieldName1 + " = '" + value1 + "' ";
		if (fieldName2 != null && value2 != null)
		{
			selectStatement += " AND " + fieldName2 + " = '" + value2 + "' ";
		}
		if (fieldName3 != null && value3 != null)
		{
			selectStatement += " AND " + fieldName3 + " = '" + value3 + "' ";
		}
		if (fieldName4 != null && fuzzy4 != null)
		{
			selectStatement += " AND " + fieldName4 + " ~* '" + fuzzy4 + "' ";
		}
		selectStatement += " ORDER BY " + INVOICE_ID + " ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				InvoiceItemObject iiObj = getObject(rs, "");
				vecValObj.add(iiObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return vecValObj;
	}

	// /////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		makeConnection();
		String selectStatement = " SELECT * " + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.valObj.mPkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.valObj = getObject(rs, "");
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for pkid " + this.valObj.mPkid.toString() + " not found in database.");
		}
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
	}

   private Collection selectObjects(QueryObject query) 
			throws NamingException, SQLException
   {
		Collection coll = new Vector();
      Log.printVerbose(strObjectName + " loadObject: ");
      makeConnection();
      String selectStatement = " SELECT * " + " FROM " + TABLENAME ;
		selectStatement = query.appendQuery(selectStatement);
      PreparedStatement prepStmt = con.prepareStatement(selectStatement);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next())
      {
			InvoiceItemObject invItemObj = getObject(rs,"");
			coll.add(invItemObj);
      } 
      prepStmt.close();
      closeConnection();
      Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return coll;
   }

   private Vector selectPointsSummaryByItemCode(Timestamp dateFrom, Timestamp dateTo, Integer iBranch, Integer limit,
													String orderByQuery, String txnType)
         throws NamingException, SQLException
   {
		Timestamp dateToNextDay = TimeFormat.add(dateTo,0,0,1);
      Vector coll = new Vector();
      Log.printVerbose(strObjectName + " loadObject: ");
      makeConnection();
      String selectStatement = " SELECT result1.*, iitem.item_code, iitem.name FROM (SELECT detail.item_id, detail.loyalty_logic, sum(detail.total_quantity) AS quantity,sum(detail.total_quantity*detail.unit_price_quoted) AS amount, SUM(detail.loyalty_points_awarded) AS points_awarded, SUM(detail.loyalty_points_redeemed) AS points_redeemed, sum(total_quantity*unit_cost_ma) AS cost FROM cust_invoice_item AS detail INNER JOIN cust_invoice_index AS index ON (detail.invoice_id = index.pkid) WHERE ";
		selectStatement+= " index.time_issued >= '"+TimeFormat.strDisplayDate(dateFrom)+"' AND index.time_issued < '"+TimeFormat.strDisplayDate(dateTo)+"'  AND detail.loyalty_logic='"+txnType+"' ";
		if(iBranch!=null)
		{ selectStatement += " AND index.cust_svcctr_id = '"+iBranch.toString()+"' ";}
		selectStatement += " GROUP BY detail.item_id,detail.loyalty_logic ) AS result1 INNER JOIN inv_item AS iitem ON (result1.item_id = iitem.pkid) "+orderByQuery+" LIMIT "+limit.toString()+" ; ";

      PreparedStatement prepStmt = con.prepareStatement(selectStatement);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next())
      {
// item_id | loyalty_logic | quantity |    amount     | points_awarded | points_redeemed |     cost     | item_code |              name
			MembershipPointsSummaryByItemCodeForm.ReportRow row = new MembershipPointsSummaryByItemCodeForm.ReportRow();
			row.itemId = new Integer(rs.getInt("item_id")); 
			row.itemCode = rs.getString("item_code");
			row.itemName = rs.getString("name");
			row.loyaltyLogic = rs.getString("loyalty_logic");
			row.quantity = rs.getBigDecimal("quantity");
			row.amount = rs.getBigDecimal("amount");
			row.pointsAwarded = rs.getBigDecimal("points_awarded");
			row.pointsRedeemed = rs.getBigDecimal("points_redeemed");
			row.cost = rs.getBigDecimal("cost");
         coll.add(row);
      }

      prepStmt.close();
      closeConnection();
      Log.printVerbose(strObjectName + " Leaving loadObject: ");
      return coll;
   }



   private Vector selectObjectsWithInvoiceConditions(QueryObject query, Timestamp dateStart, Timestamp dateEnd, String keyword)
         throws NamingException, SQLException
   {
      Vector vecResult = new Vector();

		Timestamp dateToNextDay = TimeFormat.add(dateEnd,0,0,1);

      Log.printVerbose(strObjectName + " loadObject: ");
      makeConnection();
      String selectStatement = " SELECT result.* FROM (SELECT item.* FROM cust_invoice_item AS item "
					+" INNER JOIN cust_invoice_index AS invoice ON (item.invoice_id=invoice.pkid) WHERE "
					+" (invoice.reference_no ILIKE '%"+keyword+"%' OR "
					+" item.remarks ILIKE '%"+keyword+"%' OR "
					+" invoice.remarks ILIKE '%"+keyword+"%') AND "
					+" invoice.time_issued >= '"+TimeFormat.strDisplayDate(dateStart)+"' AND "
					+" invoice.time_issued < '"+TimeFormat.strDisplayDate(dateToNextDay)+"') AS result ";
      selectStatement = query.appendQuery(selectStatement);
		Log.printVerbose("SELECT STATEMENT = "+selectStatement);
      PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		Log.printVerbose("checkpoint ejb 2..... ");
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next())
      {
         InvoiceItemObject invItemObj = getObject(rs,"");
         vecResult.add(invItemObj);
      }
      prepStmt.close();
      closeConnection();
      Log.printVerbose(strObjectName + " Leaving loadObject: ");
		Log.printVerbose("checkpoint ejb 3..... ");
      return vecResult;
   }




//SELECT itm.remarks,sum(itm.total_quantity),sum(itm.total_quantity) AS qty,sum(itm.total_quantity*itm.unit_discount) AS total_discount, sum(itm.total_quantity*itm.unit_price_std) AS amount_std,sum(itm.total_quantity*itm.unit_price_quoted) AS amount_invoice FROM cust_invoice_item AS itm INNER JOIN cust_invoice_index AS idx ON (itm.invoice_id = idx.pkid) WHERE idx.cust_svcctr_id = '1' AND idx.time_issued > '2005-01-01' AND idx.time_issued < '2007-01-01' AND itm.unit_discount > '0' GROUP BY itm.remarks
   private Vector selectDiscountSummaryByRemarks(Integer branchId, Timestamp dateFrom, Timestamp dateTo, Integer salesman)
         throws NamingException, SQLException
   {
		Timestamp dateNextTo = TimeFormat.add(dateTo,0,0,1);
      Vector coll = new Vector();
      Log.printVerbose(strObjectName + " loadObject: ");
      makeConnection();
      String selectStatement = " SELECT itm.remarks,sum(itm.total_quantity) AS qty,sum(itm.total_quantity*itm.unit_discount) "
			+" AS total_discount, sum(itm.total_quantity*itm.unit_price_std) AS amount_std, "
			+" sum(itm.total_quantity*itm.unit_price_quoted) AS amount_invoice FROM cust_invoice_item AS itm INNER JOIN "
			+" cust_invoice_index AS idx ON (itm.invoice_id = idx.pkid) WHERE ";
		selectStatement += " idx.time_issued >= '"+TimeFormat.strDisplayDate(dateFrom)+"' AND idx.time_issued < '"+TimeFormat.strDisplayDate(dateNextTo)+"' ";
		selectStatement += " AND itm.unit_discount > '0' ";
		if(branchId!=null)
		{
			selectStatement += " AND idx.cust_svcctr_id = '"+branchId.toString()+"' ";
		}
		selectStatement += " GROUP BY itm.remarks ";
		Log.printVerbose(" STMT: "+selectStatement);
      PreparedStatement prepStmt = con.prepareStatement(selectStatement);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next())
      {
         InvoiceItemDiscountSummary.Row row = new InvoiceItemDiscountSummary.Row();
			row.remarks = rs.getString("remarks");
			row.qty = rs.getBigDecimal("qty");
			row.discount = rs.getBigDecimal("total_discount");
			row.amountStd = rs.getBigDecimal("amount_std");
			row.amountInvoice = rs.getBigDecimal("amount_invoice");
         coll.add(row);
      }
      prepStmt.close();
      closeConnection();
      Log.printVerbose(strObjectName + " Leaving loadObject: ");
      return coll;
   }



	private void storeObject() throws NamingException, SQLException
	{
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			makeConnection();
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?, " + INVOICE_ID + " = ?, "
					+ POS_ITEM_ID + " = ?, " + REMARKS + " = ?, " + TOTALQTY + " = ?, " + CURRENCY + " = ?, "
					+ UNIT_PRICE_QUOTED + " = ?, " + STR_NAME_1 + " = ?, " + STR_VALUE_1 + " = ?, " + PIC1 + " = ?, "
					+ PIC2 + " = ?, " + PIC3 + " = ?, " + CURRENCY2 + " = ?, " + UNIT_PRICE_QUOTED2 + " = ?, " + TAXAMT
					+ " = ?, " + TAXAMT2 + " = ?, " + STR_NAME_2 + " = ?, " + STR_VALUE_2 + " = ?, " + STR_NAME_3
					+ " = ?, " + // 19
					STR_VALUE_3 + " = ?, " + INT_NAME_1 + " = ?, " + INT_VALUE_1 + " = ?, " + INT_NAME_2 + " = ?, "
					+ INT_VALUE_2 + " = ?, " + BD_NAME_1 + " = ?, " + BD_VALUE_1 + " = ?, " + STATUS + " = ?, " + STATE
					+ " = ?, " + POS_ITEM_TYPE + " = ?, " + ITEM_ID + " = ?, " + ITEM_CODE + " = ?, " + BAR_CODE
					+ " = ?, " + SERIALIZED + " = ?, " + NAME + " = ?, " + OUTSTANDING_QTY + " = ?, " + PACKAGE
					+ " = ?, " + PARENT_ID + "= ?, " + // 37
					UNIT_COST_MA + " = ?, " + UNIT_PRICE_STD + " = ?, " + UNIT_DISCOUNT + " = ?, " + UNIT_COMMISSION
					+ " = ?, " + CODE_PROJECT + " = ?, " + CODE_DEPARTMENT + " = ?, " + CODE_DEALER + " = ?, "
					+ CODE_SALESMAN + " = ?, " + STK_ID + " = ?, " + STK_LOCATION_ID + " = ?, " + STK_LOCATION_CODE
					+ " = ?, " + BOM_CONVERT_MODE + " = ?, " + BOM_ID + " = ?, " + BOM_CONVERT_STATUS + " = ?, "
					+ BOM_CONVERT_TIME + " = ?, " + BOM_CONVERT_USER + " = ?, " + BOM_TARGET_LOC + " = ?, "
					+ BOM_SOURCE_LOC + " = ?, " + WARRANTY_TYPE + " = ?, " + WARRANTY_OPTION + " = ?, "
					+ WARRANTY_EXPIRY + " = ?, " 
					+ PSEUDO_LOGIC + " = ?, "
					+ PSEUDO_CODE + " = ?, "
					+ PSEUDO_NAME + " = ?, "
					+ PSEUDO_DESCRIPTION + " = ?, "
					+ PSEUDO_CURRENCY + " = ?, "
					+ PSEUDO_PRICE + " = ?, "
					+ PSEUDO_QTY + " = ?, "
					+ LOYALTY_LOGIC + " = ?, "
					+ LOYALTY_POINTS_AWARDED + " = ?, "
					+ LOYALTY_POINTS_REDEEMED + " = ?, "
					+ PACKAGE_GROUP + " = ? "
					+ " WHERE " + PKID + " = ? ";
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.mPkid.longValue());
			prepStmt.setLong(2, this.valObj.mInvoiceId.longValue());
			prepStmt.setInt(3, this.valObj.mPosItemId.intValue());
			prepStmt.setString(4, this.valObj.mRemarks);
			prepStmt.setBigDecimal(5, this.valObj.mTotalQty);
			prepStmt.setString(6, this.valObj.mCurrency);
			prepStmt.setBigDecimal(7, this.valObj.mUnitPriceQuoted);
			prepStmt.setString(8, this.valObj.mStrName1);
			prepStmt.setString(9, this.valObj.mStrValue1);
			prepStmt.setInt(10, this.valObj.mPic1.intValue());
			prepStmt.setInt(11, this.valObj.mPic2.intValue());
			prepStmt.setInt(12, this.valObj.mPic3.intValue());
			prepStmt.setString(13, this.valObj.mCurrency2);
			prepStmt.setBigDecimal(14, this.valObj.mUnitPriceQuoted2);
			prepStmt.setBigDecimal(15, this.valObj.mTaxAmt);
			prepStmt.setBigDecimal(16, this.valObj.mTaxAmt2);
			prepStmt.setString(17, this.valObj.mStrName2);
			prepStmt.setString(18, this.valObj.mStrValue2);
			prepStmt.setString(19, this.valObj.mStrName3);
			prepStmt.setString(20, this.valObj.mStrValue3);
			prepStmt.setString(21, this.valObj.mIntName1);
			prepStmt.setInt(22, this.valObj.mIntValue1.intValue());
			prepStmt.setString(23, this.valObj.mIntName2);
			prepStmt.setInt(24, this.valObj.mIntValue2.intValue());
			prepStmt.setString(25, this.valObj.mBdName1);
			prepStmt.setBigDecimal(26, this.valObj.mBdValue1);
			prepStmt.setString(27, this.valObj.mStatus);
			prepStmt.setString(28, this.valObj.mState);
			prepStmt.setString(29, this.valObj.mPosItemType);
			prepStmt.setInt(30, this.valObj.mItemId.intValue());
			prepStmt.setString(31, this.valObj.mItemCode);
			prepStmt.setString(32, this.valObj.mBarCode);
			prepStmt.setBoolean(33, this.valObj.mSerialized);
			prepStmt.setString(34, this.valObj.mName);
			prepStmt.setBigDecimal(35, this.valObj.mOutstandingQty);
			prepStmt.setBoolean(36, this.valObj.mPackage);
			prepStmt.setLong(37, this.valObj.mParentId.longValue());
			prepStmt.setBigDecimal(38, this.valObj.mUnitCostMa);
			prepStmt.setBigDecimal(39, this.valObj.mUnitPriceStd);
			prepStmt.setBigDecimal(40, this.valObj.mUnitDiscount);
			prepStmt.setBigDecimal(41, this.valObj.mUnitCommission);
			prepStmt.setString(42, this.valObj.codeProject);
			prepStmt.setString(43, this.valObj.codeDepartment);
			prepStmt.setString(44, this.valObj.codeDealer);
			prepStmt.setString(45, this.valObj.codeSalesman);
			prepStmt.setInt(46, this.valObj.stkId.intValue());
			prepStmt.setInt(47, this.valObj.stkLocationId.intValue());
			prepStmt.setString(48, this.valObj.stkLocationCode);
			prepStmt.setString(49, this.valObj.bomConvertMode);
			prepStmt.setInt(50, this.valObj.bomId.intValue());
			prepStmt.setString(51, this.valObj.bomConvertStatus);
			prepStmt.setTimestamp(52, this.valObj.bomConvertTime);
			prepStmt.setInt(53, this.valObj.bomConvertUser.intValue());
			prepStmt.setInt(54, this.valObj.bomTargetLoc.intValue());
			prepStmt.setInt(55, this.valObj.bomSourceLoc.intValue());
			prepStmt.setString(56, this.valObj.warrantyType);
			prepStmt.setString(57, this.valObj.warrantyOption);
			prepStmt.setTimestamp(58, this.valObj.warrantyExpiry);
			prepStmt.setString(59, this.valObj.pseudoLogic);
			prepStmt.setString(60, this.valObj.pseudoCode);
			prepStmt.setString(61, this.valObj.pseudoName);
			prepStmt.setString(62, this.valObj.pseudoDescription);
			prepStmt.setString(63, this.valObj.pseudoCurrency);
			prepStmt.setBigDecimal(64, this.valObj.pseudoPrice);
			prepStmt.setBigDecimal(65, this.valObj.pseudoQty);
			prepStmt.setString(66, this.valObj.loyaltyLogic);
			prepStmt.setBigDecimal(67, this.valObj.loyaltyPointsAwarded);
			prepStmt.setBigDecimal(68, this.valObj.loyaltyPointsRedeemed);
			if(this.valObj.package_group!=null)
			{
				prepStmt.setString(69, this.valObj.package_group);
			}
			else
			{
				prepStmt.setString(69, "");
			}
			prepStmt.setString(69, this.valObj.package_group);
			prepStmt.setLong(70, this.valObj.mPkid.longValue());
			int rowCount = prepStmt.executeUpdate();
			prepStmt.close();
			closeConnection();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + this.valObj.mPkid + " failed.");
			}
			Log.printVerbose(strObjectName + " Leaving storeObject: ");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws NamingException, SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = " select " + PKID + " from " + TABLENAME + "  where " + fieldName + " = ? ";
		Log.printVerbose("selectStmt = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		// prepStmt.setString(2, value);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Long(rs.getLong(1)));
		}
		prepStmt.close();
		closeConnection();
		return objectSet;
	}

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, PKID_START);
	}

	public static InvoiceItemObject getObject(ResultSet rs, String prefix)
	{
		InvoiceItemObject iiObj = null;
		try
		{
			iiObj = new InvoiceItemObject();
			iiObj.mPkid = new Long(rs.getLong(prefix + PKID)); // primary key
			iiObj.mInvoiceId = new Long(rs.getLong(prefix + INVOICE_ID));
			iiObj.mPosItemId = new Integer(rs.getInt(prefix + POS_ITEM_ID));
			iiObj.mRemarks = rs.getString(prefix + REMARKS);
			iiObj.mTotalQty = rs.getBigDecimal(prefix + TOTALQTY);
			iiObj.mCurrency = rs.getString(prefix + CURRENCY);
			iiObj.mUnitPriceQuoted = rs.getBigDecimal(prefix + UNIT_PRICE_QUOTED);
			iiObj.mStrName1 = rs.getString(prefix + STR_NAME_1);
			iiObj.mStrValue1 = rs.getString(prefix + STR_VALUE_1); // 10
			iiObj.mPic1 = new Integer(rs.getInt(prefix + PIC1));
			iiObj.mPic2 = new Integer(rs.getInt(prefix + PIC2));
			iiObj.mPic3 = new Integer(rs.getInt(prefix + PIC3));
			iiObj.mCurrency2 = rs.getString(prefix + CURRENCY2);
			iiObj.mUnitPriceQuoted2 = rs.getBigDecimal(prefix + UNIT_PRICE_QUOTED2);
			iiObj.mTaxAmt = rs.getBigDecimal(prefix + TAXAMT);
			iiObj.mTaxAmt2 = rs.getBigDecimal(prefix + TAXAMT2);
			iiObj.mStrName2 = rs.getString(prefix + STR_NAME_2);
			iiObj.mStrValue2 = rs.getString(prefix + STR_VALUE_2);
			iiObj.mStrName3 = rs.getString(prefix + STR_NAME_3);
			iiObj.mStrValue3 = rs.getString(prefix + STR_VALUE_3);
			iiObj.mIntName1 = rs.getString(prefix + INT_NAME_1);
			iiObj.mIntValue1 = new Integer(rs.getInt(prefix + INT_VALUE_1));
			iiObj.mIntName2 = rs.getString(prefix + INT_NAME_2);
			iiObj.mIntValue2 = new Integer(rs.getInt(prefix + INT_VALUE_2));
			iiObj.mBdName1 = rs.getString(prefix + BD_NAME_1);
			iiObj.mBdValue1 = rs.getBigDecimal(prefix + BD_VALUE_1);
			iiObj.mStatus = rs.getString(prefix + STATUS);
			iiObj.mState = rs.getString(prefix + STATE);
			iiObj.mPosItemType = rs.getString(prefix + POS_ITEM_TYPE);
			iiObj.mItemId = new Integer(rs.getInt(prefix + ITEM_ID));
			iiObj.mItemCode = rs.getString(prefix + ITEM_CODE);
			iiObj.mBarCode = rs.getString(prefix + BAR_CODE);
			iiObj.mSerialized = rs.getBoolean(prefix + SERIALIZED);
			iiObj.mName = rs.getString(prefix + NAME);
			iiObj.mOutstandingQty = rs.getBigDecimal(prefix + OUTSTANDING_QTY);
			iiObj.mPackage = rs.getBoolean(prefix + PACKAGE);
			iiObj.mParentId = new Long(rs.getLong(prefix + PARENT_ID));
			iiObj.mUnitCostMa = rs.getBigDecimal(prefix + UNIT_COST_MA);
			iiObj.mUnitPriceStd = rs.getBigDecimal(prefix + UNIT_PRICE_STD);
			iiObj.mUnitDiscount = rs.getBigDecimal(prefix + UNIT_DISCOUNT);
			iiObj.mUnitCommission = rs.getBigDecimal(prefix + UNIT_COMMISSION);
			iiObj.codeProject = rs.getString(prefix + CODE_PROJECT);
			iiObj.codeDepartment = rs.getString(prefix + CODE_DEPARTMENT);
			iiObj.codeDealer = rs.getString(prefix + CODE_DEALER);
			iiObj.codeSalesman = rs.getString(prefix + CODE_SALESMAN);
			iiObj.stkId = new Integer(rs.getInt(prefix + STK_ID));
			iiObj.stkLocationId = new Integer(rs.getInt(prefix + STK_LOCATION_ID));
			iiObj.stkLocationCode = rs.getString(prefix + STK_LOCATION_CODE);
			iiObj.bomConvertMode = rs.getString(prefix + BOM_CONVERT_MODE);
			iiObj.bomId = new Integer(rs.getInt(prefix + BOM_ID));
			iiObj.bomConvertStatus = rs.getString(prefix + BOM_CONVERT_STATUS);
			iiObj.bomConvertTime = rs.getTimestamp(prefix + BOM_CONVERT_TIME);
			iiObj.bomConvertUser = new Integer(rs.getInt(prefix + BOM_CONVERT_USER));
			iiObj.bomTargetLoc = new Integer(rs.getInt(prefix + BOM_TARGET_LOC));
			iiObj.bomSourceLoc = new Integer(rs.getInt(prefix + BOM_SOURCE_LOC));
			iiObj.warrantyType = rs.getString(prefix + WARRANTY_TYPE);
			iiObj.warrantyOption = rs.getString(prefix + WARRANTY_OPTION);
			iiObj.warrantyExpiry = rs.getTimestamp(prefix + WARRANTY_EXPIRY);
			iiObj.pseudoLogic = rs.getString(prefix+PSEUDO_LOGIC);
			iiObj.pseudoCode = rs.getString(prefix+PSEUDO_CODE);
			iiObj.pseudoName = rs.getString(prefix+PSEUDO_NAME);
			iiObj.pseudoDescription = rs.getString(prefix+PSEUDO_DESCRIPTION);
			iiObj.pseudoCurrency = rs.getString(prefix+PSEUDO_CURRENCY);
			iiObj.pseudoPrice = rs.getBigDecimal(prefix+PSEUDO_PRICE);
			iiObj.pseudoQty = rs.getBigDecimal(prefix+PSEUDO_QTY);
			iiObj.loyaltyLogic = rs.getString(prefix+LOYALTY_LOGIC);
			iiObj.loyaltyPointsAwarded = rs.getBigDecimal(prefix+LOYALTY_POINTS_AWARDED);
			iiObj.loyaltyPointsRedeemed = rs.getBigDecimal(prefix+LOYALTY_POINTS_REDEEMED);
			iiObj.package_group = rs.getString(prefix+PACKAGE_GROUP);

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return iiObj;
	}
	
	private JobsheetGSTReportForm.Row selectJobsheetGSTReport(Timestamp date,TreeMap treeBranches) throws NamingException, SQLException
	{
		// TKW20080102
		JobsheetGSTReportForm.Row rpt = new JobsheetGSTReportForm.Row(date);
		makeConnection();
		Log.printVerbose("In bean object...");
		// Get a single date. Return results that fall within that day.
		// External loop can be used to retrieve more than 1 day's results.
		
		String csvString = "";
		Vector vecSelectedBranches = new Vector(treeBranches.values());
		for(int cnt1=0;cnt1<vecSelectedBranches.size();cnt1++)
		{
			BranchObject branchObj = (BranchObject) vecSelectedBranches.get(cnt1);
		    if(cnt1>0){csvString += ",";}
		    csvString += branchObj.pkid.toString();
		}
	      
		Timestamp dateFrom;
		Timestamp dateTo;
		dateFrom = date;
		dateTo = TimeFormat.add(date,0,0,1);
		
		String sltStmt =	" SELECT " +
							" SUM(price_total) AS price_total, " +
							" SUM(tax_total) AS tax_total, " + 
							" SUM(invoice_total) AS invoice_total " +
							" FROM " + 
							" ( " +
							" SELECT jt.*, ji.* FROM " +
							" ( " +
							" SELECT SUM((unit_price_std - unit_discount) * total_quantity) AS price_total, " +
							" SUM(taxamt) AS tax_total, invoice_id " +
							" FROM cust_invoice_item " +
							" WHERE taxamt > 0 " +
							" GROUP BY invoice_id " +
							" ) AS jt " +
							" INNER JOIN " +
							" ( " +
							" SELECT pkid, total_amt AS invoice_total FROM cust_invoice_index WHERE " +
							" time_issued >= '"	 + dateFrom.toString() + "' AND " +
							" time_issued < '"	 + dateTo.toString() + "' AND " +
							" cust_svcctr_id IN ( " + csvString + " ) " + 
							" ) AS ji " +
							" ON (jt.invoice_id = ji.pkid) " +
							" ) AS rpt2 ";
							
		Log.printVerbose(sltStmt);

		PreparedStatement prepStmt = con.prepareStatement(sltStmt);
		ResultSet rs = prepStmt.executeQuery();
		BigDecimal invoiceTotal = null;
		BigDecimal taxTotal = null;
		BigDecimal priceTotal = null;
		
		while (rs.next())
		{			
			invoiceTotal = rs.getBigDecimal("invoice_total");
			taxTotal = rs.getBigDecimal("tax_total");
			priceTotal = rs.getBigDecimal("price_total");

			rpt = new JobsheetGSTReportForm.Row(invoiceTotal,priceTotal,taxTotal,dateFrom);
			
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose("Done with bean object...");

		return rpt;
	}
	
} // ObjectBean


