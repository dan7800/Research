package org.apache.james.mime4j.util;

class CharsetUtil$Charset
  implements Comparable<Charset>
{
  private String[] aliases = null;
  private String canonical = null;
  private String mime = null;

  private CharsetUtil$Charset(String paramString1, String paramString2, String[] paramArrayOfString)
  {
    this.canonical = paramString1;
    this.mime = paramString2;
    this.aliases = paramArrayOfString;
  }

  public int compareTo(Charset paramCharset)
  {
    return this.canonical.compareTo(paramCharset.canonical);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.CharsetUtil.Charset
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */