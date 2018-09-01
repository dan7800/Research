package org.apache.james.mime4j.parser;

public final class MimeEntityConfig
  implements Cloneable
{
  private boolean countLineNumbers = false;
  private long maxContentLen = -1L;
  private int maxHeaderCount = 1000;
  private int maxLineLen = 1000;
  private boolean maximalBodyDescriptor = false;
  private boolean strictParsing = false;

  public MimeEntityConfig()
  {
  }

  public MimeEntityConfig clone()
  {
    try
    {
      MimeEntityConfig localMimeEntityConfig = (MimeEntityConfig)super.clone();
      return localMimeEntityConfig;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
    }
    throw new InternalError();
  }

  public long getMaxContentLen()
  {
    return this.maxContentLen;
  }

  public int getMaxHeaderCount()
  {
    return this.maxHeaderCount;
  }

  public int getMaxLineLen()
  {
    return this.maxLineLen;
  }

  public boolean isCountLineNumbers()
  {
    return this.countLineNumbers;
  }

  public boolean isMaximalBodyDescriptor()
  {
    return this.maximalBodyDescriptor;
  }

  public boolean isStrictParsing()
  {
    return this.strictParsing;
  }

  public void setCountLineNumbers(boolean paramBoolean)
  {
    this.countLineNumbers = paramBoolean;
  }

  public void setMaxContentLen(long paramLong)
  {
    this.maxContentLen = paramLong;
  }

  public void setMaxHeaderCount(int paramInt)
  {
    this.maxHeaderCount = paramInt;
  }

  public void setMaxLineLen(int paramInt)
  {
    this.maxLineLen = paramInt;
  }

  public void setMaximalBodyDescriptor(boolean paramBoolean)
  {
    this.maximalBodyDescriptor = paramBoolean;
  }

  public void setStrictParsing(boolean paramBoolean)
  {
    this.strictParsing = paramBoolean;
  }

  public String toString()
  {
    return "[max body descriptor: " + this.maximalBodyDescriptor + ", strict parsing: " + this.strictParsing + ", max line length: " + this.maxLineLen + ", max header count: " + this.maxHeaderCount + ", max content length: " + this.maxContentLen + ", count line numbers: " + this.countLineNumbers + "]";
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.MimeEntityConfig
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */