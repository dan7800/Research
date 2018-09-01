package org.apache.james.mime4j.codec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Locale;
import org.apache.james.mime4j.util.CharsetUtil;

public class EncoderUtil
{
  private static final BitSet ATEXT_CHARS = initChars("()<>@.,;:\\\"[]");
  private static final char BASE64_PAD = '=';
  private static final byte[] BASE64_TABLE = Base64OutputStream.BASE64_TABLE;
  private static final int ENCODED_WORD_MAX_LENGTH = 75;
  private static final String ENC_WORD_PREFIX = "=?";
  private static final String ENC_WORD_SUFFIX = "?=";
  private static final int MAX_USED_CHARACTERS = 50;
  private static final BitSet Q_REGULAR_CHARS = initChars("=_?");
  private static final BitSet Q_RESTRICTED_CHARS = initChars("=_?\"#$%&'(),.:;<>@[\\]^`{|}~");
  private static final BitSet TOKEN_CHARS = initChars("()<>@,;:\\\"/[]?=");

  private EncoderUtil()
  {
  }

  private static int bEncodedLength(byte[] paramArrayOfByte)
  {
    return 4 * ((2 + paramArrayOfByte.length) / 3);
  }

  private static Charset determineCharset(String paramString)
  {
    int i = 1;
    int j = paramString.length();
    for (int k = 0; k < j; k++)
    {
      int m = paramString.charAt(k);
      if (m > 255)
        return CharsetUtil.UTF_8;
      if (m > 127)
        i = 0;
    }
    if (i != 0)
      return CharsetUtil.US_ASCII;
    return CharsetUtil.ISO_8859_1;
  }

  private static Encoding determineEncoding(byte[] paramArrayOfByte, Usage paramUsage)
  {
    if (paramArrayOfByte.length == 0)
      return Encoding.Q;
    if (paramUsage == Usage.TEXT_TOKEN);
    int i;
    for (BitSet localBitSet = Q_REGULAR_CHARS; ; localBitSet = Q_RESTRICTED_CHARS)
    {
      i = 0;
      for (int j = 0; j < paramArrayOfByte.length; j++)
      {
        int k = 0xFF & paramArrayOfByte[j];
        if ((k != 32) && (!localBitSet.get(k)))
          i++;
      }
    }
    if (i * 100 / paramArrayOfByte.length > 30)
      return Encoding.B;
    return Encoding.Q;
  }

  private static byte[] encode(String paramString, Charset paramCharset)
  {
    ByteBuffer localByteBuffer = paramCharset.encode(paramString);
    byte[] arrayOfByte = new byte[localByteBuffer.limit()];
    localByteBuffer.get(arrayOfByte);
    return arrayOfByte;
  }

  public static String encodeAddressDisplayName(String paramString)
  {
    if (isAtomPhrase(paramString))
      return paramString;
    if (hasToBeEncoded(paramString, 0))
      return encodeEncodedWord(paramString, Usage.WORD_ENTITY);
    return quote(paramString);
  }

  public static String encodeAddressLocalPart(String paramString)
  {
    if (isDotAtomText(paramString))
      return paramString;
    return quote(paramString);
  }

  private static String encodeB(String paramString1, String paramString2, int paramInt, Charset paramCharset, byte[] paramArrayOfByte)
  {
    if (bEncodedLength(paramArrayOfByte) + paramString1.length() + "?=".length() <= 75 - paramInt)
      return paramString1 + encodeB(paramArrayOfByte) + "?=";
    String str1 = paramString2.substring(0, paramString2.length() / 2);
    String str2 = encodeB(paramString1, str1, paramInt, paramCharset, encode(str1, paramCharset));
    String str3 = paramString2.substring(paramString2.length() / 2);
    String str4 = encodeB(paramString1, str3, 0, paramCharset, encode(str3, paramCharset));
    return str2 + " " + str4;
  }

  public static String encodeB(byte[] paramArrayOfByte)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    int i = 0;
    int j = paramArrayOfByte.length;
    while (i < j - 2)
    {
      int n = (0xFF & paramArrayOfByte[i]) << 16 | (0xFF & paramArrayOfByte[(i + 1)]) << 8 | 0xFF & paramArrayOfByte[(i + 2)];
      localStringBuilder.append((char)BASE64_TABLE[(0x3F & n >> 18)]);
      localStringBuilder.append((char)BASE64_TABLE[(0x3F & n >> 12)]);
      localStringBuilder.append((char)BASE64_TABLE[(0x3F & n >> 6)]);
      localStringBuilder.append((char)BASE64_TABLE[(n & 0x3F)]);
      i += 3;
    }
    if (i == j - 2)
    {
      int m = (0xFF & paramArrayOfByte[i]) << 16 | (0xFF & paramArrayOfByte[(i + 1)]) << 8;
      localStringBuilder.append((char)BASE64_TABLE[(0x3F & m >> 18)]);
      localStringBuilder.append((char)BASE64_TABLE[(0x3F & m >> 12)]);
      localStringBuilder.append((char)BASE64_TABLE[(0x3F & m >> 6)]);
      localStringBuilder.append((char)'=');
    }
    while (true)
    {
      return localStringBuilder.toString();
      if (i == j - 1)
      {
        int k = (0xFF & paramArrayOfByte[i]) << 16;
        localStringBuilder.append((char)BASE64_TABLE[(0x3F & k >> 18)]);
        localStringBuilder.append((char)BASE64_TABLE[(0x3F & k >> 12)]);
        localStringBuilder.append((char)'=');
        localStringBuilder.append((char)'=');
      }
    }
  }

  public static String encodeEncodedWord(String paramString, Usage paramUsage)
  {
    return encodeEncodedWord(paramString, paramUsage, 0, null, null);
  }

  public static String encodeEncodedWord(String paramString, Usage paramUsage, int paramInt)
  {
    return encodeEncodedWord(paramString, paramUsage, paramInt, null, null);
  }

  public static String encodeEncodedWord(String paramString, Usage paramUsage, int paramInt, Charset paramCharset, Encoding paramEncoding)
  {
    if (paramString == null)
      throw new IllegalArgumentException();
    if ((paramInt < 0) || (paramInt > 50))
      throw new IllegalArgumentException();
    if (paramCharset == null)
      paramCharset = determineCharset(paramString);
    String str = CharsetUtil.toMimeCharset(paramCharset.name());
    if (str == null)
      throw new IllegalArgumentException("Unsupported charset");
    byte[] arrayOfByte = encode(paramString, paramCharset);
    if (paramEncoding == null)
      paramEncoding = determineEncoding(arrayOfByte, paramUsage);
    if (paramEncoding == Encoding.B)
      return encodeB("=?" + str + "?B?", paramString, paramInt, paramCharset, arrayOfByte);
    return encodeQ("=?" + str + "?Q?", paramString, paramUsage, paramInt, paramCharset, arrayOfByte);
  }

  public static String encodeHeaderParameter(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase(Locale.US);
    if (isToken(paramString2))
      return str + "=" + paramString2;
    return str + "=" + quote(paramString2);
  }

  public static String encodeIfNecessary(String paramString, Usage paramUsage, int paramInt)
  {
    if (hasToBeEncoded(paramString, paramInt))
      return encodeEncodedWord(paramString, paramUsage, paramInt);
    return paramString;
  }

  private static String encodeQ(String paramString1, String paramString2, Usage paramUsage, int paramInt, Charset paramCharset, byte[] paramArrayOfByte)
  {
    if (qEncodedLength(paramArrayOfByte, paramUsage) + paramString1.length() + "?=".length() <= 75 - paramInt)
      return paramString1 + encodeQ(paramArrayOfByte, paramUsage) + "?=";
    String str1 = paramString2.substring(0, paramString2.length() / 2);
    String str2 = encodeQ(paramString1, str1, paramUsage, paramInt, paramCharset, encode(str1, paramCharset));
    String str3 = paramString2.substring(paramString2.length() / 2);
    String str4 = encodeQ(paramString1, str3, paramUsage, 0, paramCharset, encode(str3, paramCharset));
    return str2 + " " + str4;
  }

  public static String encodeQ(byte[] paramArrayOfByte, Usage paramUsage)
  {
    BitSet localBitSet;
    StringBuilder localStringBuilder;
    int j;
    label26: int k;
    if (paramUsage == Usage.TEXT_TOKEN)
    {
      localBitSet = Q_REGULAR_CHARS;
      localStringBuilder = new StringBuilder();
      int i = paramArrayOfByte.length;
      j = 0;
      if (j >= i)
        break label125;
      k = 0xFF & paramArrayOfByte[j];
      if (k != 32)
        break label70;
      localStringBuilder.append((char)'_');
    }
    while (true)
    {
      j++;
      break label26;
      localBitSet = Q_RESTRICTED_CHARS;
      break;
      label70: if (!localBitSet.get(k))
      {
        localStringBuilder.append((char)'=');
        localStringBuilder.append(hexDigit(k >>> 4));
        localStringBuilder.append(hexDigit(k & 0xF));
      }
      else
      {
        localStringBuilder.append((char)k);
      }
    }
    label125: return localStringBuilder.toString();
  }

  public static boolean hasToBeEncoded(String paramString, int paramInt)
  {
    if (paramString == null)
      throw new IllegalArgumentException();
    if ((paramInt < 0) || (paramInt > 50))
      throw new IllegalArgumentException();
    int i = paramInt;
    int j = 0;
    if (j < paramString.length())
    {
      int k = paramString.charAt(j);
      if ((k == 9) || (k == 32))
        i = 0;
      do
      {
        j++;
        break;
        i++;
        if (i > 77)
          return true;
      }
      while ((k >= 32) && (k < 127));
      return true;
    }
    return false;
  }

  private static char hexDigit(int paramInt)
  {
    if (paramInt < 10)
      return (char)(paramInt + 48);
    return (char)(65 + (paramInt - 10));
  }

  private static BitSet initChars(String paramString)
  {
    BitSet localBitSet = new BitSet(128);
    for (int i = 33; i < 127; i = (char)(i + 1))
      if (paramString.indexOf(i) == -1)
        localBitSet.set(i);
    return localBitSet;
  }

  private static boolean isAtomPhrase(String paramString)
  {
    boolean bool = false;
    int i = paramString.length();
    int j = 0;
    if (j < i)
    {
      char c = paramString.charAt(j);
      if (ATEXT_CHARS.get(c))
        bool = true;
      while (CharsetUtil.isWhitespace(c))
      {
        j++;
        break;
      }
      return false;
    }
    return bool;
  }

  private static boolean isDotAtomText(String paramString)
  {
    int i = 46;
    int j = paramString.length();
    if (j == 0)
      return false;
    for (int k = 0; k < j; k++)
    {
      int m = paramString.charAt(k);
      if (m == 46)
      {
        if ((i == 46) || (k == j - 1))
          return false;
      }
      else if (!ATEXT_CHARS.get(m))
        return false;
      i = m;
    }
    return true;
  }

  public static boolean isToken(String paramString)
  {
    int i = paramString.length();
    if (i == 0)
      return false;
    for (int j = 0; j < i; j++)
    {
      int k = paramString.charAt(j);
      if (!TOKEN_CHARS.get(k))
        return false;
    }
    return true;
  }

  private static int qEncodedLength(byte[] paramArrayOfByte, Usage paramUsage)
  {
    BitSet localBitSet;
    int i;
    int j;
    label16: int k;
    if (paramUsage == Usage.TEXT_TOKEN)
    {
      localBitSet = Q_REGULAR_CHARS;
      i = 0;
      j = 0;
      if (j >= paramArrayOfByte.length)
        break label77;
      k = 0xFF & paramArrayOfByte[j];
      if (k != 32)
        break label56;
      i++;
    }
    while (true)
    {
      j++;
      break label16;
      localBitSet = Q_RESTRICTED_CHARS;
      break;
      label56: if (!localBitSet.get(k))
        i += 3;
      else
        i++;
    }
    label77: return i;
  }

  private static String quote(String paramString)
  {
    String str = paramString.replaceAll("[\\\\\"]", "\\\\$0");
    return "\"" + str + "\"";
  }

  public static enum Encoding
  {
    static
    {
      Encoding[] arrayOfEncoding = new Encoding[2];
      arrayOfEncoding[0] = B;
      arrayOfEncoding[1] = Q;
    }
  }

  public static enum Usage
  {
    static
    {
      Usage[] arrayOfUsage = new Usage[2];
      arrayOfUsage[0] = TEXT_TOKEN;
      arrayOfUsage[1] = WORD_ENTITY;
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.codec.EncoderUtil
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */