package org.apache.james.mime4j.descriptor;

import java.util.Map;

public abstract interface ContentDescriptor
{
  public abstract String getCharset();

  public abstract long getContentLength();

  public abstract Map<String, String> getContentTypeParameters();

  public abstract String getMediaType();

  public abstract String getMimeType();

  public abstract String getSubType();

  public abstract String getTransferEncoding();
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.descriptor.ContentDescriptor
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */