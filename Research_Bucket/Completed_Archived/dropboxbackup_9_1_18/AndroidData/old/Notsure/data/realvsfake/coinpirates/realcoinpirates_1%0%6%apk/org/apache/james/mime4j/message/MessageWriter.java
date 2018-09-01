package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.james.mime4j.codec.CodecUtil;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

public class MessageWriter
{
  private static final byte[] CRLF = { 13, 10 };
  private static final byte[] DASHES = { 45, 45 };
  public static final MessageWriter DEFAULT = new MessageWriter();

  protected MessageWriter()
  {
  }

  private ByteSequence getBoundary(ContentTypeField paramContentTypeField)
  {
    String str = paramContentTypeField.getBoundary();
    if (str == null)
      throw new IllegalArgumentException("Multipart boundary not specified");
    return ContentUtil.encode(str);
  }

  private ContentTypeField getContentType(Multipart paramMultipart)
  {
    Entity localEntity = paramMultipart.getParent();
    if (localEntity == null)
      throw new IllegalArgumentException("Missing parent entity in multipart");
    Header localHeader = localEntity.getHeader();
    if (localHeader == null)
      throw new IllegalArgumentException("Missing header in parent entity");
    ContentTypeField localContentTypeField = (ContentTypeField)localHeader.getField("Content-Type");
    if (localContentTypeField == null)
      throw new IllegalArgumentException("Content-Type field not specified");
    return localContentTypeField;
  }

  private void writeBytes(ByteSequence paramByteSequence, OutputStream paramOutputStream)
    throws IOException
  {
    if ((paramByteSequence instanceof ByteArrayBuffer))
    {
      ByteArrayBuffer localByteArrayBuffer = (ByteArrayBuffer)paramByteSequence;
      paramOutputStream.write(localByteArrayBuffer.buffer(), 0, localByteArrayBuffer.length());
      return;
    }
    paramOutputStream.write(paramByteSequence.toByteArray());
  }

  protected OutputStream encodeStream(OutputStream paramOutputStream, String paramString, boolean paramBoolean)
    throws IOException
  {
    if (MimeUtil.isBase64Encoding(paramString))
      return CodecUtil.wrapBase64(paramOutputStream);
    if (MimeUtil.isQuotedPrintableEncoded(paramString))
      return CodecUtil.wrapQuotedPrintable(paramOutputStream, paramBoolean);
    return paramOutputStream;
  }

  public void writeBody(Body paramBody, OutputStream paramOutputStream)
    throws IOException
  {
    if ((paramBody instanceof Message))
    {
      writeEntity((Message)paramBody, paramOutputStream);
      return;
    }
    if ((paramBody instanceof Multipart))
    {
      writeMultipart((Multipart)paramBody, paramOutputStream);
      return;
    }
    if ((paramBody instanceof SingleBody))
    {
      ((SingleBody)paramBody).writeTo(paramOutputStream);
      return;
    }
    throw new IllegalArgumentException("Unsupported body class");
  }

  public void writeEntity(Entity paramEntity, OutputStream paramOutputStream)
    throws IOException
  {
    Header localHeader = paramEntity.getHeader();
    if (localHeader == null)
      throw new IllegalArgumentException("Missing header");
    writeHeader(localHeader, paramOutputStream);
    Body localBody = paramEntity.getBody();
    if (localBody == null)
      throw new IllegalArgumentException("Missing body");
    boolean bool = localBody instanceof BinaryBody;
    OutputStream localOutputStream = encodeStream(paramOutputStream, paramEntity.getContentTransferEncoding(), bool);
    writeBody(localBody, localOutputStream);
    if (localOutputStream != paramOutputStream)
      localOutputStream.close();
  }

  public void writeHeader(Header paramHeader, OutputStream paramOutputStream)
    throws IOException
  {
    Iterator localIterator = paramHeader.iterator();
    while (localIterator.hasNext())
    {
      writeBytes(((Field)localIterator.next()).getRaw(), paramOutputStream);
      paramOutputStream.write(CRLF);
    }
    paramOutputStream.write(CRLF);
  }

  public void writeMultipart(Multipart paramMultipart, OutputStream paramOutputStream)
    throws IOException
  {
    ByteSequence localByteSequence = getBoundary(getContentType(paramMultipart));
    writeBytes(paramMultipart.getPreambleRaw(), paramOutputStream);
    paramOutputStream.write(CRLF);
    Iterator localIterator = paramMultipart.getBodyParts().iterator();
    while (localIterator.hasNext())
    {
      BodyPart localBodyPart = (BodyPart)localIterator.next();
      paramOutputStream.write(DASHES);
      writeBytes(localByteSequence, paramOutputStream);
      paramOutputStream.write(CRLF);
      writeEntity(localBodyPart, paramOutputStream);
      paramOutputStream.write(CRLF);
    }
    paramOutputStream.write(DASHES);
    writeBytes(localByteSequence, paramOutputStream);
    paramOutputStream.write(DASHES);
    paramOutputStream.write(CRLF);
    writeBytes(paramMultipart.getEpilogueRaw(), paramOutputStream);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.MessageWriter
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */