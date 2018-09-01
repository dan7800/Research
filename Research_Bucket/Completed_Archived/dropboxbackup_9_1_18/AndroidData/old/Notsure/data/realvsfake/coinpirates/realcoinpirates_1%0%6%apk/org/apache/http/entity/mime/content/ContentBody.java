package org.apache.http.entity.mime.content;

import org.apache.james.mime4j.descriptor.ContentDescriptor;
import org.apache.james.mime4j.message.Body;

public abstract interface ContentBody extends Body, ContentDescriptor
{
  public abstract String getFilename();
}

/* Location:
 * Qualified Name:     org.apache.http.entity.mime.content.ContentBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */