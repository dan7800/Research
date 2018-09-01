package org.apache.http.entity.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.message.Body;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Entity;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.message.MessageWriter;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.CharsetUtil;

@NotThreadSafe
public class HttpMultipart extends Multipart
{
  private static final ByteArrayBuffer CR_LF = encode(MIME.DEFAULT_CHARSET, "\r\n");
  private static final ByteArrayBuffer TWO_DASHES = encode(MIME.DEFAULT_CHARSET, "--");
  private HttpMultipartMode mode = HttpMultipartMode.STRICT;

  public HttpMultipart(String paramString)
  {
    super(paramString);
  }

  private void doWriteTo(HttpMultipartMode paramHttpMultipartMode, OutputStream paramOutputStream, boolean paramBoolean)
    throws IOException
  {
    List localList = getBodyParts();
    Charset localCharset = getCharset();
    ByteArrayBuffer localByteArrayBuffer = encode(localCharset, getBoundary());
    switch (1.$SwitchMap$org$apache$http$entity$mime$HttpMultipartMode[paramHttpMultipartMode.ordinal()])
    {
    default:
    case 1:
      String str2;
      do
      {
        return;
        String str1 = getPreamble();
        if ((str1 != null) && (str1.length() != 0))
        {
          writeBytes(encode(localCharset, str1), paramOutputStream);
          writeBytes(CR_LF, paramOutputStream);
        }
        for (int k = 0; ; k++)
        {
          int m = localList.size();
          if (k >= m)
            break;
          writeBytes(TWO_DASHES, paramOutputStream);
          writeBytes(localByteArrayBuffer, paramOutputStream);
          writeBytes(CR_LF, paramOutputStream);
          BodyPart localBodyPart2 = (BodyPart)localList.get(k);
          Iterator localIterator = localBodyPart2.getHeader().getFields().iterator();
          while (localIterator.hasNext())
          {
            writeBytes(((Field)localIterator.next()).getRaw(), paramOutputStream);
            writeBytes(CR_LF, paramOutputStream);
          }
          writeBytes(CR_LF, paramOutputStream);
          if (paramBoolean)
            MessageWriter.DEFAULT.writeBody(localBodyPart2.getBody(), paramOutputStream);
          writeBytes(CR_LF, paramOutputStream);
        }
        writeBytes(TWO_DASHES, paramOutputStream);
        writeBytes(localByteArrayBuffer, paramOutputStream);
        writeBytes(TWO_DASHES, paramOutputStream);
        writeBytes(CR_LF, paramOutputStream);
        str2 = getEpilogue();
      }
      while ((str2 == null) || (str2.length() == 0));
      writeBytes(encode(localCharset, str2), paramOutputStream);
      writeBytes(CR_LF, paramOutputStream);
      return;
    case 2:
    }
    for (int i = 0; ; i++)
    {
      int j = localList.size();
      if (i >= j)
        break;
      writeBytes(TWO_DASHES, paramOutputStream);
      writeBytes(localByteArrayBuffer, paramOutputStream);
      writeBytes(CR_LF, paramOutputStream);
      BodyPart localBodyPart1 = (BodyPart)localList.get(i);
      Field localField = localBodyPart1.getHeader().getField("Content-Disposition");
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append(localField.getName());
      localStringBuilder.append(": ");
      localStringBuilder.append(localField.getBody());
      writeBytes(encode(localCharset, localStringBuilder.toString()), paramOutputStream);
      writeBytes(CR_LF, paramOutputStream);
      writeBytes(CR_LF, paramOutputStream);
      if (paramBoolean)
        MessageWriter.DEFAULT.writeBody(localBodyPart1.getBody(), paramOutputStream);
      writeBytes(CR_LF, paramOutputStream);
    }
    writeBytes(TWO_DASHES, paramOutputStream);
    writeBytes(localByteArrayBuffer, paramOutputStream);
    writeBytes(TWO_DASHES, paramOutputStream);
    writeBytes(CR_LF, paramOutputStream);
  }

  private static ByteArrayBuffer encode(Charset paramCharset, String paramString)
  {
    ByteBuffer localByteBuffer = paramCharset.encode(CharBuffer.wrap(paramString));
    ByteArrayBuffer localByteArrayBuffer = new ByteArrayBuffer(localByteBuffer.remaining());
    localByteArrayBuffer.append(localByteBuffer.array(), localByteBuffer.position(), localByteBuffer.remaining());
    return localByteArrayBuffer;
  }

  private static void writeBytes(ByteArrayBuffer paramByteArrayBuffer, OutputStream paramOutputStream)
    throws IOException
  {
    paramOutputStream.write(paramByteArrayBuffer.buffer(), 0, paramByteArrayBuffer.length());
  }

  private static void writeBytes(ByteSequence paramByteSequence, OutputStream paramOutputStream)
    throws IOException
  {
    if ((paramByteSequence instanceof ByteArrayBuffer))
    {
      writeBytes((ByteArrayBuffer)paramByteSequence, paramOutputStream);
      return;
    }
    paramOutputStream.write(paramByteSequence.toByteArray());
  }

  protected String getBoundary()
  {
    return ((ContentTypeField)getParent().getHeader().getField("Content-Type")).getBoundary();
  }

  protected Charset getCharset()
  {
    ContentTypeField localContentTypeField = (ContentTypeField)getParent().getHeader().getField("Content-Type");
    switch (1.$SwitchMap$org$apache$http$entity$mime$HttpMultipartMode[this.mode.ordinal()])
    {
    default:
      return null;
    case 1:
      return MIME.DEFAULT_CHARSET;
    case 2:
    }
    if (localContentTypeField.getCharset() != null)
      return CharsetUtil.getCharset(localContentTypeField.getCharset());
    return CharsetUtil.getCharset("ISO-8859-1");
  }

  public HttpMultipartMode getMode()
  {
    return this.mode;
  }

  public long getTotalLength()
  {
    List localList = getBodyParts();
    long l1 = 0L;
    int i = 0;
    while (i < localList.size())
    {
      Body localBody = ((BodyPart)localList.get(i)).getBody();
      if ((localBody instanceof ContentBody))
      {
        long l2 = ((ContentBody)localBody).getContentLength();
        if (l2 >= 0L)
        {
          l1 += l2;
          i++;
        }
        else
        {
          return -1L;
        }
      }
      else
      {
        return -1L;
      }
    }
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    try
    {
      doWriteTo(this.mode, localByteArrayOutputStream, false);
      int j = localByteArrayOutputStream.toByteArray().length;
      return l1 + j;
    }
    catch (IOException localIOException)
    {
    }
    return -1L;
  }

  public void setMode(HttpMultipartMode paramHttpMultipartMode)
  {
    this.mode = paramHttpMultipartMode;
  }

  public void writeTo(OutputStream paramOutputStream)
    throws IOException
  {
    doWriteTo(this.mode, paramOutputStream, true);
  }
}

/* Location:
 * Qualified Name:     org.apache.http.entity.mime.HttpMultipart
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */