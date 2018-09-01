package org.apache.http.entity.mime;

import org.apache.http.annotation.Immutable;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

@Immutable
public class MinimalField
  implements Field
{
  private final String name;
  private ByteSequence raw;
  private final String value;

  MinimalField(String paramString1, String paramString2)
  {
    this.name = paramString1;
    this.value = paramString2;
    this.raw = null;
  }

  public String getBody()
  {
    return this.value;
  }

  public String getName()
  {
    return this.name;
  }

  public ByteSequence getRaw()
  {
    if (this.raw == null)
      this.raw = ContentUtil.encode(toString());
    return this.raw;
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(this.name);
    localStringBuilder.append(": ");
    localStringBuilder.append(this.value);
    return localStringBuilder.toString();
  }
}

/* Location:
 * Qualified Name:     org.apache.http.entity.mime.MinimalField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */