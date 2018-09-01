package org.apache.james.mime4j.parser;

import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;

class RawField
  implements Field
{
  private String body;
  private int colonIdx;
  private String name;
  private final ByteSequence raw;

  public RawField(ByteSequence paramByteSequence, int paramInt)
  {
    this.raw = paramByteSequence;
    this.colonIdx = paramInt;
  }

  private String parseBody()
  {
    int i = 1 + this.colonIdx;
    int j = this.raw.length() - i;
    return ContentUtil.decode(this.raw, i, j);
  }

  private String parseName()
  {
    return ContentUtil.decode(this.raw, 0, this.colonIdx);
  }

  public String getBody()
  {
    if (this.body == null)
      this.body = parseBody();
    return this.body;
  }

  public String getName()
  {
    if (this.name == null)
      this.name = parseName();
    return this.name;
  }

  public ByteSequence getRaw()
  {
    return this.raw;
  }

  public String toString()
  {
    return getName() + ':' + getBody();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.RawField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */