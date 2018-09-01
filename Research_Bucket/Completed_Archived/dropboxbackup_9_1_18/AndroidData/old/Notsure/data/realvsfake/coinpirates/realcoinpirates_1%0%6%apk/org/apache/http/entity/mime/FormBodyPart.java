package org.apache.http.entity.mime;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.james.mime4j.descriptor.ContentDescriptor;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Header;

@NotThreadSafe
public class FormBodyPart extends BodyPart
{
  private final String name;

  public FormBodyPart(String paramString, ContentBody paramContentBody)
  {
    if (paramString == null)
      throw new IllegalArgumentException("Name may not be null");
    if (paramContentBody == null)
      throw new IllegalArgumentException("Body may not be null");
    this.name = paramString;
    setHeader(new Header());
    setBody(paramContentBody);
    generateContentDisp(paramContentBody);
    generateContentType(paramContentBody);
    generateTransferEncoding(paramContentBody);
  }

  private void addField(String paramString1, String paramString2)
  {
    getHeader().addField(new MinimalField(paramString1, paramString2));
  }

  protected void generateContentDisp(ContentBody paramContentBody)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("form-data; name=\"");
    localStringBuilder.append(getName());
    localStringBuilder.append("\"");
    if (paramContentBody.getFilename() != null)
    {
      localStringBuilder.append("; filename=\"");
      localStringBuilder.append(paramContentBody.getFilename());
      localStringBuilder.append("\"");
    }
    addField("Content-Disposition", localStringBuilder.toString());
  }

  protected void generateContentType(ContentDescriptor paramContentDescriptor)
  {
    if (paramContentDescriptor.getMimeType() != null)
    {
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append(paramContentDescriptor.getMimeType());
      if (paramContentDescriptor.getCharset() != null)
      {
        localStringBuilder.append("; charset=");
        localStringBuilder.append(paramContentDescriptor.getCharset());
      }
      addField("Content-Type", localStringBuilder.toString());
    }
  }

  protected void generateTransferEncoding(ContentDescriptor paramContentDescriptor)
  {
    if (paramContentDescriptor.getTransferEncoding() != null)
      addField("Content-Transfer-Encoding", paramContentDescriptor.getTransferEncoding());
  }

  public String getName()
  {
    return this.name;
  }
}

/* Location:
 * Qualified Name:     org.apache.http.entity.mime.FormBodyPart
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */