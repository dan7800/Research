package org.apache.james.mime4j.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class MimeUtil
{
  public static final String ENC_7BIT = "7bit";
  public static final String ENC_8BIT = "8bit";
  public static final String ENC_BASE64 = "base64";
  public static final String ENC_BINARY = "binary";
  public static final String ENC_QUOTED_PRINTABLE = "quoted-printable";
  public static final String MIME_HEADER_CONTENT_DESCRIPTION = "content-description";
  public static final String MIME_HEADER_CONTENT_DISPOSITION = "content-disposition";
  public static final String MIME_HEADER_CONTENT_ID = "content-id";
  public static final String MIME_HEADER_LANGAUGE = "content-language";
  public static final String MIME_HEADER_LOCATION = "content-location";
  public static final String MIME_HEADER_MD5 = "content-md5";
  public static final String MIME_HEADER_MIME_VERSION = "mime-version";
  public static final String PARAM_CREATION_DATE = "creation-date";
  public static final String PARAM_FILENAME = "filename";
  public static final String PARAM_MODIFICATION_DATE = "modification-date";
  public static final String PARAM_READ_DATE = "read-date";
  public static final String PARAM_SIZE = "size";
  private static final ThreadLocal<DateFormat> RFC822_DATE_FORMAT = new ThreadLocal()
  {
    protected DateFormat initialValue()
    {
      return new MimeUtil.Rfc822DateFormat();
    }
  };
  private static int counter;
  private static final Log log = LogFactory.getLog(MimeUtil.class);
  private static final Random random = new Random();

  static
  {
    counter = 0;
  }

  private MimeUtil()
  {
  }

  public static String createUniqueBoundary()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("-=Part.");
    localStringBuilder.append(Integer.toHexString(nextCounterValue()));
    localStringBuilder.append((char)'.');
    localStringBuilder.append(Long.toHexString(random.nextLong()));
    localStringBuilder.append((char)'.');
    localStringBuilder.append(Long.toHexString(System.currentTimeMillis()));
    localStringBuilder.append((char)'.');
    localStringBuilder.append(Long.toHexString(random.nextLong()));
    localStringBuilder.append("=-");
    return localStringBuilder.toString();
  }

  public static String createUniqueMessageId(String paramString)
  {
    StringBuilder localStringBuilder = new StringBuilder("<Mime4j.");
    localStringBuilder.append(Integer.toHexString(nextCounterValue()));
    localStringBuilder.append((char)'.');
    localStringBuilder.append(Long.toHexString(random.nextLong()));
    localStringBuilder.append((char)'.');
    localStringBuilder.append(Long.toHexString(System.currentTimeMillis()));
    if (paramString != null)
    {
      localStringBuilder.append((char)'@');
      localStringBuilder.append(paramString);
    }
    localStringBuilder.append((char)'>');
    return localStringBuilder.toString();
  }

  public static String fold(String paramString, int paramInt)
  {
    int i = paramString.length();
    if (paramInt + i <= 76)
      return paramString;
    StringBuilder localStringBuilder = new StringBuilder();
    int j = -paramInt;
    int m;
    for (int k = indexOfWsp(paramString, 0); ; k = m)
    {
      if (k == i)
      {
        localStringBuilder.append(paramString.substring(Math.max(0, j)));
        return localStringBuilder.toString();
      }
      m = indexOfWsp(paramString, k + 1);
      if (m - j > 76)
      {
        localStringBuilder.append(paramString.substring(Math.max(0, j), k));
        localStringBuilder.append("\r\n");
        j = k;
      }
    }
  }

  public static String formatDate(Date paramDate, TimeZone paramTimeZone)
  {
    DateFormat localDateFormat = (DateFormat)RFC822_DATE_FORMAT.get();
    if (paramTimeZone == null)
      localDateFormat.setTimeZone(TimeZone.getDefault());
    while (true)
    {
      return localDateFormat.format(paramDate);
      localDateFormat.setTimeZone(paramTimeZone);
    }
  }

  public static Map<String, String> getHeaderParams(String paramString)
  {
    String str1 = unfold(paramString.trim());
    HashMap localHashMap = new HashMap();
    Object localObject1;
    Object localObject2;
    StringBuilder localStringBuilder1;
    StringBuilder localStringBuilder2;
    int i;
    int k;
    int m;
    label91: char c;
    label172: int i1;
    if (str1.indexOf(";") == -1)
    {
      localObject1 = str1;
      localObject2 = null;
      localHashMap.put("", localObject1);
      if (localObject2 == null)
        break label757;
      char[] arrayOfChar = localObject2.toCharArray();
      localStringBuilder1 = new StringBuilder(64);
      localStringBuilder2 = new StringBuilder(64);
      i = 0;
      int j = arrayOfChar.length;
      k = 0;
      m = 0;
      if (k < j)
        c = arrayOfChar[k];
    }
    else
    {
      switch (m)
      {
      default:
        i1 = i;
        n = m;
      case 99:
      case 0:
        while (true)
        {
          label180: k++;
          m = n;
          i = i1;
          break label91;
          String str2 = str1.substring(0, str1.indexOf(";"));
          String str3 = str1.substring(1 + str2.length());
          localObject1 = str2;
          localObject2 = str3;
          break;
          if (c != ';')
            break label172;
          i1 = i;
          n = 0;
          continue;
          if (c != '=')
            break label273;
          log.error("Expected header param name, got '='");
          n = 99;
          i1 = i;
        }
        label273: localStringBuilder1.setLength(0);
        localStringBuilder2.setLength(0);
      case 2:
      case 3:
      case 4:
      case 5:
      case 1:
      }
    }
    label411: label757: for (int n = 1; ; n = m)
    {
      if (c == '=')
      {
        if (localStringBuilder1.length() == 0)
        {
          n = 99;
          i1 = i;
          break label180;
        }
        n = 2;
        i1 = i;
        break label180;
      }
      localStringBuilder1.append(c);
      i1 = i;
      break label180;
      int i3 = 0;
      switch (c)
      {
      default:
        m = 3;
      case '\t':
      case ' ':
      case '"':
      }
      for (i3 = 1; ; i3 = 0)
      {
        if (i3 != 0)
          break label411;
        i1 = i;
        n = m;
        break;
        m = 4;
      }
      int i2 = 0;
      switch (c)
      {
      default:
        localStringBuilder2.append(c);
      case '\t':
      case ' ':
      case ';':
      }
      while (true)
      {
        if (i2 != 0)
          break label511;
        i1 = i;
        n = m;
        break;
        localHashMap.put(localStringBuilder1.toString().trim().toLowerCase(), localStringBuilder2.toString().trim());
        m = 5;
        i2 = 1;
      }
      label511: for (n = m; ; n = m)
        switch (c)
        {
        default:
          n = 99;
          i1 = i;
          break;
        case ';':
          i1 = i;
          n = 0;
          break;
        case '\t':
        case ' ':
          i1 = i;
          break;
          switch (c)
          {
          default:
            if (i != 0)
              localStringBuilder2.append((char)'\\');
            localStringBuilder2.append(c);
            i = 0;
            break;
          case '"':
            if (i == 0)
            {
              localHashMap.put(localStringBuilder1.toString().trim().toLowerCase(), localStringBuilder2.toString());
              n = 5;
              i1 = i;
              break label180;
            }
            localStringBuilder2.append(c);
            n = m;
            i1 = 0;
            break;
          case '\\':
            if (i != 0)
              localStringBuilder2.append((char)'\\');
            if (i == 0);
            for (i1 = 1; ; i1 = 0)
            {
              n = m;
              break;
            }
            if (m == 3)
              localHashMap.put(localStringBuilder1.toString().trim().toLowerCase(), localStringBuilder2.toString().trim());
            return localHashMap;
          }
          break;
        }
    }
  }

  private static int indexOfWsp(String paramString, int paramInt)
  {
    int i = paramString.length();
    for (int j = paramInt; j < i; j++)
    {
      int k = paramString.charAt(j);
      if ((k == 32) || (k == 9))
        return j;
    }
    return i;
  }

  public static boolean isBase64Encoding(String paramString)
  {
    return "base64".equalsIgnoreCase(paramString);
  }

  public static boolean isMessage(String paramString)
  {
    return (paramString != null) && (paramString.equalsIgnoreCase("message/rfc822"));
  }

  public static boolean isMultipart(String paramString)
  {
    return (paramString != null) && (paramString.toLowerCase().startsWith("multipart/"));
  }

  public static boolean isQuotedPrintableEncoded(String paramString)
  {
    return "quoted-printable".equalsIgnoreCase(paramString);
  }

  public static boolean isSameMimeType(String paramString1, String paramString2)
  {
    return (paramString1 != null) && (paramString2 != null) && (paramString1.equalsIgnoreCase(paramString2));
  }

  private static int nextCounterValue()
  {
    try
    {
      int i = counter;
      counter = i + 1;
      return i;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  public static String unfold(String paramString)
  {
    int i = paramString.length();
    for (int j = 0; j < i; j++)
    {
      int k = paramString.charAt(j);
      if ((k == 13) || (k == 10))
        return unfold0(paramString, j);
    }
    return paramString;
  }

  private static String unfold0(String paramString, int paramInt)
  {
    int i = paramString.length();
    StringBuilder localStringBuilder = new StringBuilder(i);
    if (paramInt > 0)
      localStringBuilder.append(paramString.substring(0, paramInt));
    for (int j = paramInt + 1; j < i; j++)
    {
      char c = paramString.charAt(j);
      if ((c != '\r') && (c != '\n'))
        localStringBuilder.append(c);
    }
    return localStringBuilder.toString();
  }

  private static final class Rfc822DateFormat extends SimpleDateFormat
  {
    private static final long serialVersionUID = 1L;

    public Rfc822DateFormat()
    {
      super(Locale.US);
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
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.MimeUtil
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */