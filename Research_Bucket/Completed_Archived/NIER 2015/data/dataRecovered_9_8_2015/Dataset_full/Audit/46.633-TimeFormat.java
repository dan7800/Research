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
package com.vlee.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeFormat
{
	public static boolean debuggingOn = true;
	public static boolean auditTrailOn = true;

	public static Timestamp getTimestamp()
	{
		String strTS = TimeFormat.strDisplayTimeStamp();
		Timestamp tsRTN = TimeFormat.createTimeStamp(strTS);
		return tsRTN;
	}

	public static String strDisplayTimeStamp()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate2()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate1(Timestamp tsTime)
	{
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
			long a = tsTime.getTime();
			java.util.Date bufDate = new java.util.Date(a);
			// String b = bufDate.toString();
			String strTimeStamp = formatter.format(bufDate);
			return strTimeStamp;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(" Error " + ex.getMessage());
			return new String("errrrrrooooooooorrrrrrrr");
		}
	}

	public static String strDisplayDate2(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate3(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate4(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate5(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("E");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate6(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate7(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate8(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	/*
	 * 
	 * Date and Time Pattern Result "yyyy.MM.dd G 'at' HH:mm:ss z" 2001.07.04 AD
	 * at 12:08:56 PDT
	 * 
	 * "EEE, MMM d, ''yy" Wed, Jul 4, '01
	 * 
	 * "h:mm a" 12:08 PM
	 * 
	 * "hh 'o''clock' a, zzzz" 12 o'clock PM, Pacific Daylight Time
	 * 
	 * "K:mm a, z" 0:08 PM, PDT
	 * 
	 * "yyyyy.MMMMM.dd GGG hh:mm aaa" 02001.July.04 AD 12:08 PM
	 * 
	 * "EEE, d MMM yyyy HH:mm:ss Z" Wed, 4 Jul 2001 12:08:56 -0700
	 * 
	 * "yyMMddHHmmssZ" 010704120856-0700
	 */
	public static String format(Timestamp tsTime, String dateFormat)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime1(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate(Timestamp ts)
	{
		return (ts.toString().substring(0, 10));
	}

	public static String strDisplayTime(Timestamp ts)
	{
		return (ts.toString().substring(11, 19));
	}

	public static Calendar createCalendar(String strTime)
	{
		Calendar cal = Calendar.getInstance();
		Timestamp ts = createTimeStamp(strTime);
		cal.setTime(new Date(ts.getTime()));
		return cal;
	}
	
	public static Timestamp createTimestamp(String strTime)
	{
		return TimeFormat.createTimeStamp(strTime);
	}

	public static Timestamp createTimeStamp(String strTime)
	{
		String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		if (strTime.length() == 7)
		{
			strTime += "-01";
			strFormat = new String("yyyy-MM-dd");
		} else if (strTime.length() < 12)
		{
			strFormat = new String("yyyy-MM-dd");
		} else
		{
			strFormat = new String("yyyy-MM-dd HH:mm:ss");
		}
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimeStamp(String strTime, String strFormat) throws Exception
	{
		// String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		/*
		 * if(strTime.length()<12) { strFormat = new String("yyyy-MM-dd");}
		 * else { strFormat = new String("yyyy-MM-dd HH:mm:ss");}
		 */
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimestamp(String strDate, String strHour, String strMinute)
	{
		String buffer = strDate + " " + strHour + ":" + strMinute + ":00.0";
		return TimeFormat.createTimestamp(buffer);
	}

	public static Timestamp getCurrentDate()
	{
		String strFormat = new String("yyyy-MM-dd");
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		try
		{
			theDate = (java.util.Date) formatter.parse(formatter.format(theDate));
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp add(Timestamp theDate, int year, int month, int day)
	{
		Timestamp rtnTs = null;
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalNew = new GregorianCalendar(gcalDate.get(Calendar.YEAR) + year, gcalDate.get(Calendar.MONTH) + month, gcalDate.get(Calendar.DATE) + day);
		rtnTs = new Timestamp(gcalNew.getTimeInMillis());
		return rtnTs;
	}

	public static Timestamp set(Timestamp theDate, int option, int value)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalReturn = new GregorianCalendar((option == Calendar.YEAR) ? value : gcalDate.get(Calendar.YEAR), (option == Calendar.MONTH) ? value : gcalDate.get(Calendar.MONTH), (option == Calendar.DATE) ? value : gcalDate.get(Calendar.DATE));
		// gcalDate.get(Calendar.MONTH),
		// gcalDate.get(Calendar.DATE));
		Timestamp tsReturn = new Timestamp(gcalReturn.getTimeInMillis());
		return tsReturn;
	}

	public static int get(Timestamp theDate, int option)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		return gcalDate.get(option);
	}

	public static Timestamp createTimeStamp2(String strDate)
	{
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			return Timestamp.valueOf(strDate.trim() + suffix);
		} else
		{
			return Timestamp.valueOf(strDate.trim() + ".000000000");
		}
	}

	public static Timestamp createTimeStampNextDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static Timestamp createTimeStampPreviousDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static int compareDates(Timestamp ts1, Timestamp ts2)
	{
		// Return <0 if ts1 < ts2
		// 0 if ts1 = ts2
		// >0 if ts1 > ts2
		Log.printVerbose("ts1 before conversion: " + ts1.toString());
		Log.printVerbose("ts2 before conversion: " + ts2.toString());
		Timestamp ts1DateOnly = createTimestamp(strDisplayDate(ts1));
		Timestamp ts2DateOnly = createTimestamp(strDisplayDate(ts2));
		Log.printVerbose("ts1 after conversion: " + ts1.toString());
		Log.printVerbose("ts2 after conversion: " + ts2.toString());
		return ts1DateOnly.compareTo(ts2DateOnly);
	}

	public static String strDisplayHour()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

} // TimeFormat
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
package com.vlee.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeFormat
{
	public static boolean debuggingOn = true;
	public static boolean auditTrailOn = true;

	public static Timestamp getTimestamp()
	{
		String strTS = TimeFormat.strDisplayTimeStamp();
		Timestamp tsRTN = TimeFormat.createTimeStamp(strTS);
		return tsRTN;
	}

	public static String strDisplayTimeStamp()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate2()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate1(Timestamp tsTime)
	{
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
			long a = tsTime.getTime();
			java.util.Date bufDate = new java.util.Date(a);
			// String b = bufDate.toString();
			String strTimeStamp = formatter.format(bufDate);
			return strTimeStamp;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(" Error " + ex.getMessage());
			return new String("errrrrrooooooooorrrrrrrr");
		}
	}

	public static String strDisplayDate2(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate3(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate4(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate5(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("E");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate6(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate7(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate8(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	/*
	 * 
	 * Date and Time Pattern Result "yyyy.MM.dd G 'at' HH:mm:ss z" 2001.07.04 AD
	 * at 12:08:56 PDT
	 * 
	 * "EEE, MMM d, ''yy" Wed, Jul 4, '01
	 * 
	 * "h:mm a" 12:08 PM
	 * 
	 * "hh 'o''clock' a, zzzz" 12 o'clock PM, Pacific Daylight Time
	 * 
	 * "K:mm a, z" 0:08 PM, PDT
	 * 
	 * "yyyyy.MMMMM.dd GGG hh:mm aaa" 02001.July.04 AD 12:08 PM
	 * 
	 * "EEE, d MMM yyyy HH:mm:ss Z" Wed, 4 Jul 2001 12:08:56 -0700
	 * 
	 * "yyMMddHHmmssZ" 010704120856-0700
	 */
	public static String format(Timestamp tsTime, String dateFormat)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime1(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate(Timestamp ts)
	{
		return (ts.toString().substring(0, 10));
	}

	public static String strDisplayTime(Timestamp ts)
	{
		return (ts.toString().substring(11, 19));
	}

	public static Calendar createCalendar(String strTime)
	{
		Calendar cal = Calendar.getInstance();
		Timestamp ts = createTimeStamp(strTime);
		cal.setTime(new Date(ts.getTime()));
		return cal;
	}
	
	public static Timestamp createTimestamp(String strTime)
	{
		return TimeFormat.createTimeStamp(strTime);
	}

	public static Timestamp createTimeStamp(String strTime)
	{
		String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		if (strTime.length() == 7)
		{
			strTime += "-01";
			strFormat = new String("yyyy-MM-dd");
		} else if (strTime.length() < 12)
		{
			strFormat = new String("yyyy-MM-dd");
		} else
		{
			strFormat = new String("yyyy-MM-dd HH:mm:ss");
		}
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimeStamp(String strTime, String strFormat) throws Exception
	{
		// String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		/*
		 * if(strTime.length()<12) { strFormat = new String("yyyy-MM-dd");}
		 * else { strFormat = new String("yyyy-MM-dd HH:mm:ss");}
		 */
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimestamp(String strDate, String strHour, String strMinute)
	{
		String buffer = strDate + " " + strHour + ":" + strMinute + ":00.0";
		return TimeFormat.createTimestamp(buffer);
	}

	public static Timestamp getCurrentDate()
	{
		String strFormat = new String("yyyy-MM-dd");
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		try
		{
			theDate = (java.util.Date) formatter.parse(formatter.format(theDate));
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp add(Timestamp theDate, int year, int month, int day)
	{
		Timestamp rtnTs = null;
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalNew = new GregorianCalendar(gcalDate.get(Calendar.YEAR) + year, gcalDate.get(Calendar.MONTH) + month, gcalDate.get(Calendar.DATE) + day);
		rtnTs = new Timestamp(gcalNew.getTimeInMillis());
		return rtnTs;
	}

	public static Timestamp set(Timestamp theDate, int option, int value)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalReturn = new GregorianCalendar((option == Calendar.YEAR) ? value : gcalDate.get(Calendar.YEAR), (option == Calendar.MONTH) ? value : gcalDate.get(Calendar.MONTH), (option == Calendar.DATE) ? value : gcalDate.get(Calendar.DATE));
		// gcalDate.get(Calendar.MONTH),
		// gcalDate.get(Calendar.DATE));
		Timestamp tsReturn = new Timestamp(gcalReturn.getTimeInMillis());
		return tsReturn;
	}

	public static int get(Timestamp theDate, int option)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		return gcalDate.get(option);
	}

	public static Timestamp createTimeStamp2(String strDate)
	{
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			return Timestamp.valueOf(strDate.trim() + suffix);
		} else
		{
			return Timestamp.valueOf(strDate.trim() + ".000000000");
		}
	}

	public static Timestamp createTimeStampNextDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static Timestamp createTimeStampPreviousDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static int compareDates(Timestamp ts1, Timestamp ts2)
	{
		// Return <0 if ts1 < ts2
		// 0 if ts1 = ts2
		// >0 if ts1 > ts2
		Log.printVerbose("ts1 before conversion: " + ts1.toString());
		Log.printVerbose("ts2 before conversion: " + ts2.toString());
		Timestamp ts1DateOnly = createTimestamp(strDisplayDate(ts1));
		Timestamp ts2DateOnly = createTimestamp(strDisplayDate(ts2));
		Log.printVerbose("ts1 after conversion: " + ts1.toString());
		Log.printVerbose("ts2 after conversion: " + ts2.toString());
		return ts1DateOnly.compareTo(ts2DateOnly);
	}
} // TimeFormat
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
package com.vlee.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeFormat
{
	public static boolean debuggingOn = true;
	public static boolean auditTrailOn = true;

	public static Timestamp getTimestamp()
	{
		String strTS = TimeFormat.strDisplayTimeStamp();
		Timestamp tsRTN = TimeFormat.createTimeStamp(strTS);
		return tsRTN;
	}

	public static String strDisplayTimeStamp()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate2()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate1(Timestamp tsTime)
	{
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
			long a = tsTime.getTime();
			java.util.Date bufDate = new java.util.Date(a);
			// String b = bufDate.toString();
			String strTimeStamp = formatter.format(bufDate);
			return strTimeStamp;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(" Error " + ex.getMessage());
			return new String("errrrrrooooooooorrrrrrrr");
		}
	}

	public static String strDisplayDate2(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate3(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate4(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate5(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("E");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate6(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate7(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate8(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	/*
	 * 
	 * Date and Time Pattern Result "yyyy.MM.dd G 'at' HH:mm:ss z" 2001.07.04 AD
	 * at 12:08:56 PDT
	 * 
	 * "EEE, MMM d, ''yy" Wed, Jul 4, '01
	 * 
	 * "h:mm a" 12:08 PM
	 * 
	 * "hh 'o''clock' a, zzzz" 12 o'clock PM, Pacific Daylight Time
	 * 
	 * "K:mm a, z" 0:08 PM, PDT
	 * 
	 * "yyyyy.MMMMM.dd GGG hh:mm aaa" 02001.July.04 AD 12:08 PM
	 * 
	 * "EEE, d MMM yyyy HH:mm:ss Z" Wed, 4 Jul 2001 12:08:56 -0700
	 * 
	 * "yyMMddHHmmssZ" 010704120856-0700
	 */
	public static String format(Timestamp tsTime, String dateFormat)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime1(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate(Timestamp ts)
	{
		return (ts.toString().substring(0, 10));
	}

	public static String strDisplayTime(Timestamp ts)
	{
		return (ts.toString().substring(11, 19));
	}

	public static Calendar createCalendar(String strTime)
	{
		Calendar cal = Calendar.getInstance();
		Timestamp ts = createTimeStamp(strTime);
		cal.setTime(new Date(ts.getTime()));
		return cal;
	}
	
	public static Timestamp createTimestamp(String strTime)
	{
		return TimeFormat.createTimeStamp(strTime);
	}

	public static Timestamp createTimeStamp(String strTime)
	{
		String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		if (strTime.length() == 7)
		{
			strTime += "-01";
			strFormat = new String("yyyy-MM-dd");
		} else if (strTime.length() < 12)
		{
			strFormat = new String("yyyy-MM-dd");
		} else
		{
			strFormat = new String("yyyy-MM-dd HH:mm:ss");
		}
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimeStamp(String strTime, String strFormat) throws Exception
	{
		// String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		/*
		 * if(strTime.length()<12) { strFormat = new String("yyyy-MM-dd");}
		 * else { strFormat = new String("yyyy-MM-dd HH:mm:ss");}
		 */
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimestamp(String strDate, String strHour, String strMinute)
	{
		String buffer = strDate + " " + strHour + ":" + strMinute + ":00.0";
		return TimeFormat.createTimestamp(buffer);
	}

	public static Timestamp getCurrentDate()
	{
		String strFormat = new String("yyyy-MM-dd");
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		try
		{
			theDate = (java.util.Date) formatter.parse(formatter.format(theDate));
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp add(Timestamp theDate, int year, int month, int day)
	{
		Timestamp rtnTs = null;
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalNew = new GregorianCalendar(gcalDate.get(Calendar.YEAR) + year, gcalDate.get(Calendar.MONTH) + month, gcalDate.get(Calendar.DATE) + day);
		rtnTs = new Timestamp(gcalNew.getTimeInMillis());
		return rtnTs;
	}

	public static Timestamp set(Timestamp theDate, int option, int value)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalReturn = new GregorianCalendar((option == Calendar.YEAR) ? value : gcalDate.get(Calendar.YEAR), (option == Calendar.MONTH) ? value : gcalDate.get(Calendar.MONTH), (option == Calendar.DATE) ? value : gcalDate.get(Calendar.DATE));
		// gcalDate.get(Calendar.MONTH),
		// gcalDate.get(Calendar.DATE));
		Timestamp tsReturn = new Timestamp(gcalReturn.getTimeInMillis());
		return tsReturn;
	}

	public static int get(Timestamp theDate, int option)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		return gcalDate.get(option);
	}

	public static Timestamp createTimeStamp2(String strDate)
	{
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			return Timestamp.valueOf(strDate.trim() + suffix);
		} else
		{
			return Timestamp.valueOf(strDate.trim() + ".000000000");
		}
	}

	public static Timestamp createTimeStampNextDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static Timestamp createTimeStampPreviousDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static int compareDates(Timestamp ts1, Timestamp ts2)
	{
		// Return <0 if ts1 < ts2
		// 0 if ts1 = ts2
		// >0 if ts1 > ts2
		Log.printVerbose("ts1 before conversion: " + ts1.toString());
		Log.printVerbose("ts2 before conversion: " + ts2.toString());
		Timestamp ts1DateOnly = createTimestamp(strDisplayDate(ts1));
		Timestamp ts2DateOnly = createTimestamp(strDisplayDate(ts2));
		Log.printVerbose("ts1 after conversion: " + ts1.toString());
		Log.printVerbose("ts2 after conversion: " + ts2.toString());
		return ts1DateOnly.compareTo(ts2DateOnly);
	}

	public static String strDisplayHour()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

} // TimeFormat
/*==========================================================
 *
 * Copyright  of Vincent Lee (vincent@wavelet.biz). 
 *	All Rights Reserved.
 * (http://www.wavelet.biz)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeFormat
{
	public static boolean debuggingOn = true;
	public static boolean auditTrailOn = true;

	public static Timestamp getTimestamp()
	{
		String strTS = TimeFormat.strDisplayTimeStamp();
		Timestamp tsRTN = TimeFormat.createTimeStamp(strTS);
		return tsRTN;
	}

	public static String strDisplayTimeStamp()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDay()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}
	
	public static String strDisplayMonth()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("MM");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}
	
	public static String strDisplayMonthMalay(String month)
	{
		if(month.equals("01"))
		{
			return "Januari";
		}
		if(month.equals("02"))
		{
			return "Februari";
		}
		if(month.equals("03"))
		{
			return "Mac";
		}
		if(month.equals("04"))
		{
			return "April";
		}
		if(month.equals("05"))
		{
			return "Mei";
		}
		if(month.equals("06"))
		{
			return "Jun";
		}
		if(month.equals("07"))
		{
			return "Julai";
		}
		if(month.equals("08"))
		{
			return "Ogos";
		}
		if(month.equals("09"))
		{
			return "September";
		}
		if(month.equals("10"))
		{
			return "Oktober";
		}
		if(month.equals("11"))
		{
			return "November";
		}
		if(month.equals("12"))
		{
			return "Disember";
		}
		return "";
	}
	
	public static String strDisplayYear()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}
	
	public static String strDisplayDate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate2()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate1(Timestamp tsTime)
	{
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
			long a = tsTime.getTime();
			java.util.Date bufDate = new java.util.Date(a);
			// String b = bufDate.toString();
			String strTimeStamp = formatter.format(bufDate);
			return strTimeStamp;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(" Error " + ex.getMessage());
			return new String("errrrrrooooooooorrrrrrrr");
		}
	}

	public static String strDisplayDate2(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate3(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate4(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate5(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("E");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate6(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate7(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate8(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}
	
	public static String strDisplayDate9(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	/*
	 * 
	 * Date and Time Pattern Result "yyyy.MM.dd G 'at' HH:mm:ss z" 2001.07.04 AD
	 * at 12:08:56 PDT
	 * 
	 * "EEE, MMM d, ''yy" Wed, Jul 4, '01
	 * 
	 * "h:mm a" 12:08 PM
	 * 
	 * "hh 'o''clock' a, zzzz" 12 o'clock PM, Pacific Daylight Time
	 * 
	 * "K:mm a, z" 0:08 PM, PDT
	 * 
	 * "yyyyy.MMMMM.dd GGG hh:mm aaa" 02001.July.04 AD 12:08 PM
	 * 
	 * "EEE, d MMM yyyy HH:mm:ss Z" Wed, 4 Jul 2001 12:08:56 -0700
	 * 
	 * "yyMMddHHmmssZ" 010704120856-0700
	 */
	public static String format(Timestamp tsTime, String dateFormat)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayTime1(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}
	
	public static String strDisplayTime2(Timestamp tsTime)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
		long a = tsTime.getTime();
		java.util.Date bufDate = new java.util.Date(a);
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}

	public static String strDisplayDate(Timestamp ts)
	{
		return (ts.toString().substring(0, 10));
	}

	public static String strDisplayTime(Timestamp ts)
	{
		return (ts.toString().substring(11, 19));
	}

	public static Calendar createCalendar(String strTime)
	{
		Calendar cal = Calendar.getInstance();
		Timestamp ts = createTimeStamp(strTime);
		cal.setTime(new Date(ts.getTime()));
		return cal;
	}
	
	public static Timestamp createTimestamp(String strTime)
	{
		return TimeFormat.createTimeStamp(strTime);
	}

	public static Timestamp createTimeStamp(String strTime)
	{
		String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		if (strTime.length() == 7)
		{
			strTime += "-01";
			strFormat = new String("yyyy-MM-dd");
		} else if (strTime.length() < 12)
		{
			strFormat = new String("yyyy-MM-dd");
		} else
		{
			strFormat = new String("yyyy-MM-dd HH:mm:ss");
		}
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimeStamp(String strTime, String strFormat) throws Exception
	{
		// String strFormat;
		// Log.printDebug("TimeFormat: strTime b4 trim = " + strTime);
		strTime = strTime.trim();
		// Log.printDebug("TimeFormat: strTime after trim = " + strTime);
		/*
		 * if(strTime.length()<12) { strFormat = new String("yyyy-MM-dd");}
		 * else { strFormat = new String("yyyy-MM-dd HH:mm:ss");}
		 */
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		// Log.printDebug("theDate.getTime() ="+theDate.getTime());
		try
		{
			theDate = (java.util.Date) formatter.parse(strTime);
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp createTimestamp(String strDate, String strHour, String strMinute)
	{
		String buffer = strDate + " " + strHour + ":" + strMinute + ":00.0";
		return TimeFormat.createTimestamp(buffer);
	}

	public static Timestamp getCurrentDate()
	{
		String strFormat = new String("yyyy-MM-dd");
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		java.util.Date theDate = new java.util.Date();
		try
		{
			theDate = (java.util.Date) formatter.parse(formatter.format(theDate));
		} catch (Exception ex)
		{
			System.err.println("TimeFormat: " + ex.getMessage());
		}
		Timestamp rtnTS = new Timestamp(theDate.getTime());
		return rtnTS;
	}

	public static Timestamp add(Timestamp theDate, int year, int month, int day)
	{
		Timestamp rtnTs = null;
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalNew = new GregorianCalendar(gcalDate.get(Calendar.YEAR) + year, gcalDate.get(Calendar.MONTH) + month, gcalDate.get(Calendar.DATE) + day);
		rtnTs = new Timestamp(gcalNew.getTimeInMillis());
		return rtnTs;
	}	

	public static Timestamp add(Timestamp theDate, int year, int month, int day, int hour, int minute, int second)
	{
		Timestamp rtnTs = null;
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalNew = new GregorianCalendar(gcalDate.get(Calendar.YEAR) + year, gcalDate.get(Calendar.MONTH) + month, gcalDate.get(Calendar.DATE) + day,
				gcalDate.get(Calendar.HOUR) + hour, gcalDate.get(Calendar.MINUTE) + minute, gcalDate.get(Calendar.SECOND) + second);
		rtnTs = new Timestamp(gcalNew.getTimeInMillis());
		return rtnTs;
	}		

	public static Timestamp addTime(Timestamp theDate, int hour, int minute, int second)
	{
		Timestamp rtnTs = null;
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalNew = new GregorianCalendar(gcalDate.get(Calendar.YEAR), gcalDate.get(Calendar.MONTH), gcalDate.get(Calendar.DATE),
				gcalDate.get(Calendar.HOUR) + hour, gcalDate.get(Calendar.MINUTE) + minute, gcalDate.get(Calendar.SECOND) + second);
		rtnTs = new Timestamp(gcalNew.getTimeInMillis());
		return rtnTs;
	}	
	
	public static Timestamp set(Timestamp theDate, int option, int value)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		GregorianCalendar gcalReturn = new GregorianCalendar((option == Calendar.YEAR) ? value : gcalDate.get(Calendar.YEAR), (option == Calendar.MONTH) ? value : gcalDate.get(Calendar.MONTH), (option == Calendar.DATE) ? value : gcalDate.get(Calendar.DATE));
		// gcalDate.get(Calendar.MONTH),
		// gcalDate.get(Calendar.DATE));
		Timestamp tsReturn = new Timestamp(gcalReturn.getTimeInMillis());
		return tsReturn;
	}

	public static int get(Timestamp theDate, int option)
	{
		GregorianCalendar gcalDate = new GregorianCalendar();
		gcalDate.setTime(theDate);
		return gcalDate.get(option);
	}

	public static Timestamp createTimeStamp2(String strDate)
	{
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			return Timestamp.valueOf(strDate.trim() + suffix);
		} else
		{
			return Timestamp.valueOf(strDate.trim() + ".000000000");
		}
	}

	public static Timestamp createTimeStampNextDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static Timestamp createTimeStampPreviousDay(String strDate)
	{
		Timestamp ts = null;
		String suffix = " 00:00:00.000000000";
		if (strDate.length() < 11)
		{
			strDate = strDate + suffix;
		} else if (strDate.length() > 10)
		{
			strDate = strDate.substring(0, 10) + suffix;
		} else
		{
			strDate = "invalid date";
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		try
		{
			ts = Timestamp.valueOf(strDate);
			cal.setTimeInMillis(ts.getTime());
		} catch (Exception e)
		{
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ts = new Timestamp(cal.getTimeInMillis());
		return ts;
	}

	public static int compareDates(Timestamp ts1, Timestamp ts2)
	{
		// Return <0 if ts1 < ts2
		// 0 if ts1 = ts2
		// >0 if ts1 > ts2
		Log.printVerbose("ts1 before conversion: " + ts1.toString());
		Log.printVerbose("ts2 before conversion: " + ts2.toString());
		Timestamp ts1DateOnly = createTimestamp(strDisplayDate(ts1));
		Timestamp ts2DateOnly = createTimestamp(strDisplayDate(ts2));
		Log.printVerbose("ts1 after conversion: " + ts1.toString());
		Log.printVerbose("ts2 after conversion: " + ts2.toString());
		return ts1DateOnly.compareTo(ts2DateOnly);
	}

	public static String strDisplayHour()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH");
		java.util.Date bufDate = new java.util.Date();
		String strTimeStamp = formatter.format(bufDate);
		return strTimeStamp;
	}


	public static int dayDifference(Timestamp buf1, Timestamp buf2)
	{
		Timestamp tmp1 = createTimestamp("0001-01-01");
		Timestamp tmp2 = createTimestamp("0001-01-02");
		long DAY_LONG = tmp2.getTime() - tmp1.getTime();
		long count = (buf1.getTime() - buf2.getTime()) / DAY_LONG;
		if(count < 0){ count = -count;}
		return (int) count;
	}
	
	public static int dayDiffNoGetTime(Timestamp buf1, Timestamp buf2)
	{
		// TKW20080103: This code provides date difference without the use of milisecond counting.
	    int diff = 0;
	    while(buf1.before(buf2) || buf1.equals(buf2))
	    {
	    	buf1 = add(buf1,0,0,1);
	    	diff++;
	    }
	    System.out.println("Difference = " + diff + " days");
	    return diff;

	}
	
	public static Timestamp getLastDay(Timestamp date)
	{
		// TKW20080103: to get the last day of the date given.
		
		// First, set the date to the 28.
		Timestamp resDate = set(date,Calendar.DATE,28);
		// Then add 4 days. This ensures that you will get the next month, no matter what.
		resDate = add(resDate,0,0,4);
		// Then set the date of the new month to the 1st.
		resDate = set(resDate, Calendar.DATE,1);
		// Then minus one day. You will now get the last day of the month you want.
		resDate = add(resDate,0,0,-1);
		return resDate;
	}
	
} // TimeFormat
