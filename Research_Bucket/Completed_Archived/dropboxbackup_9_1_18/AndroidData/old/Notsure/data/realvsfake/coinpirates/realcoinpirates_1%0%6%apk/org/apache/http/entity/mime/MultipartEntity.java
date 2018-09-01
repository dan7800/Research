package org.apache.http.entity.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.message.BasicHeader;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.message.Message;

@ThreadSafe
public class MultipartEntity
  implements HttpEntity
{
  private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private final org.apache.http.Header contentType = new BasicHeader("Content-Type", generateContentType(paramString, paramCharset));
  private volatile boolean dirty = true;
  private long length;
  private final Message message = new Message();
  private final HttpMultipart multipart = new HttpMultipart("form-data");

  public MultipartEntity()
  {
    this(HttpMultipartMode.STRICT, null, null);
  }

  public MultipartEntity(HttpMultipartMode paramHttpMultipartMode)
  {
    this(paramHttpMultipartMode, null, null);
  }

  public MultipartEntity(HttpMultipartMode paramHttpMultipartMode, String paramString, Charset paramCharset)
  {
    org.apache.james.mime4j.message.Header localHeader = new org.apache.james.mime4j.message.Header();
    this.message.setHeader(localHeader);
    this.multipart.setParent(this.message);
    if (paramHttpMultipartMode == null)
      paramHttpMultipartMode = HttpMultipartMode.STRICT;
    this.multipart.setMode(paramHttpMultipartMode);
    this.message.getHeader().addField(Fields.contentType(this.contentType.getValue()));
  }

  public void addPart(String paramString, ContentBody paramContentBody)
  {
    this.multipart.addBodyPart(new FormBodyPart(paramString, paramContentBody));
    this.dirty = true;
  }

  public void consumeContent()
    throws IOException, UnsupportedOperationException
  {
    if (isStreaming())
      throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
  }

  protected String generateContentType(String paramString, Charset paramCharset)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("multipart/form-data; boundary=");
    if (paramString != null)
      localStringBuilder.append(paramString);
    while (true)
    {
      if (paramCharset != null)
      {
        localStringBuilder.append("; charset=");
        localStringBuilder.append(paramCharset.name());
      }
      return localStringBuilder.toString();
      Random localRandom = new Random();
      int i = 30 + localRandom.nextInt(11);
      for (int j = 0; j < i; j++)
        localStringBuilder.append(MULTIPART_CHARS[localRandom.nextInt(MULTIPART_CHARS.length)]);
    }
  }

  public InputStream getContent()
    throws IOException, UnsupportedOperationException
  {
    throw new UnsupportedOperationException("Multipart form entity does not implement #getContent()");
  }

  public org.apache.http.Header getContentEncoding()
  {
    return null;
  }

  public long getContentLength()
  {
    if (this.dirty)
    {
      this.length = this.multipart.getTotalLength();
      this.dirty = false;
    }
    return this.length;
  }

  public org.apache.http.Header getContentType()
  {
    return this.contentType;
  }

  public boolean isChunked()
  {
    return !isRepeatable();
  }

  public boolean isRepeatable()
  {
    Iterator localIterator = this.multipart.getBodyParts().iterator();
    while (localIterator.hasNext())
      if (((ContentBody)((FormBodyPart)localIterator.next()).getBody()).getContentLength() < 0L)
        return false;
    return true;
  }

  public boolean isStreaming()
  {
    return !isRepeatable();
  }

  public void writeTo(OutputStream paramOutputStream)
    throws IOException
  {
    this.multipart.writeTo(paramOutputStream);
  }
}

/* Location:
 * Qualified Name:     org.apache.http.entity.mime.MultipartEntity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */