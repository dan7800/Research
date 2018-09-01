package org.apache.james.mime4j.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.util.CharsetUtil;

public class DecoderUtil
{
  private static Log log = LogFactory.getLog(DecoderUtil.class);

  public DecoderUtil()
  {
  }

  public static String decodeB(String paramString1, String paramString2)
    throws UnsupportedEncodingException
  {
    return new String(decodeBase64(paramString1), paramString2);
  }

  public static byte[] decodeBase64(String paramString)
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    try
    {
      Base64InputStream localBase64InputStream = new Base64InputStream(new ByteArrayInputStream(paramString.getBytes("US-ASCII")));
      while (true)
      {
        int i = localBase64InputStream.read();
        if (i == -1)
          break;
        localByteArrayOutputStream.write(i);
      }
    }
    catch (IOException localIOException)
    {
      log.error(localIOException);
    }
    return localByteArrayOutputStream.toByteArray();
  }

  public static byte[] decodeBaseQuotedPrintable(String paramString)
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    try
    {
      QuotedPrintableInputStream localQuotedPrintableInputStream = new QuotedPrintableInputStream(new ByteArrayInputStream(paramString.getBytes("US-ASCII")));
      while (true)
      {
        int i = localQuotedPrintableInputStream.read();
        if (i == -1)
          break;
        localByteArrayOutputStream.write(i);
      }
    }
    catch (IOException localIOException)
    {
      log.error(localIOException);
    }
    return localByteArrayOutputStream.toByteArray();
  }

  private static String decodeEncodedWord(String paramString, int paramInt1, int paramInt2)
  {
    int i = paramString.indexOf('?', paramInt1 + 2);
    if (i == paramInt2 - 2)
      return null;
    int j = paramString.indexOf('?', i + 1);
    if (j == paramInt2 - 2)
      return null;
    String str1 = paramString.substring(paramInt1 + 2, i);
    String str2 = paramString.substring(i + 1, j);
    String str3 = paramString.substring(j + 1, paramInt2 - 2);
    String str4 = CharsetUtil.toJavaCharset(str1);
    if (str4 == null)
    {
      if (log.isWarnEnabled())
        log.warn("MIME charset '" + str1 + "' in encoded word '" + paramString.substring(paramInt1, paramInt2) + "' doesn't have a " + "corresponding Java charset");
      return null;
    }
    if (!CharsetUtil.isDecodingSupported(str4))
    {
      if (log.isWarnEnabled())
        log.warn("Current JDK doesn't support decoding of charset '" + str4 + "' (MIME charset '" + str1 + "' in encoded word '" + paramString.substring(paramInt1, paramInt2) + "')");
      return null;
    }
    if (str3.length() == 0)
    {
      if (log.isWarnEnabled())
        log.warn("Missing encoded text in encoded word: '" + paramString.substring(paramInt1, paramInt2) + "'");
      return null;
    }
    try
    {
      if (str2.equalsIgnoreCase("Q"))
        return decodeQ(str3, str4);
      if (str2.equalsIgnoreCase("B"))
        return decodeB(str3, str4);
      if (log.isWarnEnabled())
        log.warn("Warning: Unknown encoding in encoded word '" + paramString.substring(paramInt1, paramInt2) + "'");
      return null;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      if (log.isWarnEnabled())
        log.warn("Unsupported encoding in encoded word '" + paramString.substring(paramInt1, paramInt2) + "'", localUnsupportedEncodingException);
      return null;
    }
    catch (RuntimeException localRuntimeException)
    {
      if (log.isWarnEnabled())
        log.warn("Could not decode encoded word '" + paramString.substring(paramInt1, paramInt2) + "'", localRuntimeException);
    }
    return null;
  }

  public static String decodeEncodedWords(String paramString)
  {
    int i = 0;
    int j = 0;
    StringBuilder localStringBuilder = new StringBuilder();
    int k = paramString.indexOf("=?", i);
    int m;
    if (k == -1)
      m = -1;
    while (m == -1)
      if (i == 0)
      {
        return paramString;
        m = paramString.indexOf("?=", k + 2);
      }
      else
      {
        localStringBuilder.append(paramString.substring(i));
        return localStringBuilder.toString();
      }
    int n = m + 2;
    String str1 = paramString.substring(i, k);
    String str2 = decodeEncodedWord(paramString, k, n);
    if (str2 == null)
    {
      localStringBuilder.append(str1);
      localStringBuilder.append(paramString.substring(k, n));
      label122: i = n;
      if (str2 == null)
        break label164;
    }
    label164: for (j = 1; ; j = 0)
    {
      break;
      if ((j == 0) || (!CharsetUtil.isWhitespace(str1)))
        localStringBuilder.append(str1);
      localStringBuilder.append(str2);
      break label122;
    }
  }

  public static String decodeQ(String paramString1, String paramString2)
    throws UnsupportedEncodingException
  {
    StringBuilder localStringBuilder = new StringBuilder(128);
    int i = 0;
    if (i < paramString1.length())
    {
      char c = paramString1.charAt(i);
      if (c == '_')
        localStringBuilder.append("=20");
      while (true)
      {
        i++;
        break;
        localStringBuilder.append(c);
      }
    }
    return new String(decodeBaseQuotedPrintable(localStringBuilder.toString()), paramString2);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.codec.DecoderUtil
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */