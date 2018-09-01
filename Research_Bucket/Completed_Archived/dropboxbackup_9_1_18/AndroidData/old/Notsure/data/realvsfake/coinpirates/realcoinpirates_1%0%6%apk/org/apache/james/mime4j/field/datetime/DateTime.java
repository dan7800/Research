package org.apache.james.mime4j.field.datetime;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTime
{
  private final Date date;
  private final int day;
  private final int hour;
  private final int minute;
  private final int month;
  private final int second;
  private final int timeZone;
  private final int year = convertToYear(paramString);

  public DateTime(String paramString, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.date = convertToDate(this.year, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    this.month = paramInt1;
    this.day = paramInt2;
    this.hour = paramInt3;
    this.minute = paramInt4;
    this.second = paramInt5;
    this.timeZone = paramInt6;
  }

  public static Date convertToDate(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
  {
    GregorianCalendar localGregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+0"));
    localGregorianCalendar.set(paramInt1, paramInt2 - 1, paramInt3, paramInt4, paramInt5, paramInt6);
    localGregorianCalendar.set(14, 0);
    if (paramInt7 != -2147483648)
      localGregorianCalendar.add(12, -1 * (60 * (paramInt7 / 100) + paramInt7 % 100));
    return localGregorianCalendar.getTime();
  }

  private int convertToYear(String paramString)
  {
    int i = Integer.parseInt(paramString);
    switch (paramString.length())
    {
    default:
      return i;
    case 1:
    case 2:
      if ((i >= 0) && (i < 50))
        return i + 2000;
      return i + 1900;
    case 3:
    }
    return i + 1900;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (paramObject == null)
      return false;
    if (getClass() != paramObject.getClass())
      return false;
    DateTime localDateTime = (DateTime)paramObject;
    if (this.date == null)
    {
      if (localDateTime.date != null)
        return false;
    }
    else if (!this.date.equals(localDateTime.date))
      return false;
    if (this.day != localDateTime.day)
      return false;
    if (this.hour != localDateTime.hour)
      return false;
    if (this.minute != localDateTime.minute)
      return false;
    if (this.month != localDateTime.month)
      return false;
    if (this.second != localDateTime.second)
      return false;
    if (this.timeZone != localDateTime.timeZone)
      return false;
    return this.year == localDateTime.year;
  }

  public Date getDate()
  {
    return this.date;
  }

  public int getDay()
  {
    return this.day;
  }

  public int getHour()
  {
    return this.hour;
  }

  public int getMinute()
  {
    return this.minute;
  }

  public int getMonth()
  {
    return this.month;
  }

  public int getSecond()
  {
    return this.second;
  }

  public int getTimeZone()
  {
    return this.timeZone;
  }

  public int getYear()
  {
    return this.year;
  }

  public int hashCode()
  {
    (1 * 31);
    if (this.date == null);
    for (int i = 0; ; i = this.date.hashCode())
      return 31 * (31 * (31 * (31 * (31 * (31 * (31 * (i + 31) + this.day) + this.hour) + this.minute) + this.month) + this.second) + this.timeZone) + this.year;
  }

  public void print()
  {
    System.out.println(toString());
  }

  public String toString()
  {
    return getYear() + " " + getMonth() + " " + getDay() + "; " + getHour() + " " + getMinute() + " " + getSecond() + " " + getTimeZone();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.datetime.DateTime
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */