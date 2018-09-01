package org.apache.james.mime4j.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CodecUtil
{
  static final int DEFAULT_ENCODING_BUFFER_SIZE = 1024;

  public CodecUtil()
  {
  }

  public static void copy(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    byte[] arrayOfByte = new byte[1024];
    while (true)
    {
      int i = paramInputStream.read(arrayOfByte);
      if (-1 == i)
        break;
      paramOutputStream.write(arrayOfByte, 0, i);
    }
  }

  public static void encodeBase64(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    Base64OutputStream localBase64OutputStream = new Base64OutputStream(paramOutputStream);
    copy(paramInputStream, localBase64OutputStream);
    localBase64OutputStream.close();
  }

  public static void encodeQuotedPrintable(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    new QuotedPrintableEncoder(1024, false).encode(paramInputStream, paramOutputStream);
  }

  public static void encodeQuotedPrintableBinary(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    new QuotedPrintableEncoder(1024, true).encode(paramInputStream, paramOutputStream);
  }

  public static OutputStream wrapBase64(OutputStream paramOutputStream)
    throws IOException
  {
    return new Base64OutputStream(paramOutputStream);
  }

  public static OutputStream wrapQuotedPrintable(OutputStream paramOutputStream, boolean paramBoolean)
    throws IOException
  {
    return new QuotedPrintableOutputStream(paramOutputStream, paramBoolean);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.codec.CodecUtil
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */