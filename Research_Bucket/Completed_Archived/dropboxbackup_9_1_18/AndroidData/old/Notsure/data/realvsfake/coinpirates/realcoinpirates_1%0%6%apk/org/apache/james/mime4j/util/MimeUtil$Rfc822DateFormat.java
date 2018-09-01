package org.apache.james.mime4j.util;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

final class MimeUtil$Rfc822DateFormat extends SimpleDateFormat
{
  private static final long serialVersionUID = 1L;

  public MimeUtil$Rfc822DateFormat()
  {
    super("EEE, d MMM yyyy HH:mm:ss ", Locale.US);
  }

  public StringBuffer format(Date paramDate, StringBuffer paramStringBuffer, FieldPosition paramFieldPosition)
  {
    StringBuffer localStringBuffer = super.format(paramDate, paramStringBuffer, paramFieldPosition);
    int i = (this.calendar.get(15) + this.calendar.get(16)) / 1000 / 60;
    if (i < 0)
    {
      localStringBuffer.append((char)'-');
      i = -i;
    }
    while (true)
    {
      Object[] arrayOfObject = new Object[2];
      arrayOfObject[0] = Integer.valueOf(i / 60);
      arrayOfObject[1] = Integer.valueOf(i % 60);
      localStringBuffer.append(String.format("%02d%02d", arrayOfObject));
      return localStringBuffer;
      localStringBuffer.append((char)'+');
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.MimeUtil.Rfc822DateFormat
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */