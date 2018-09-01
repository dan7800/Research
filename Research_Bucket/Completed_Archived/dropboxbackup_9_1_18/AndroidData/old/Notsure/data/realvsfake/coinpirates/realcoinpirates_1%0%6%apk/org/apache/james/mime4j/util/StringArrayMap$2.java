package org.apache.james.mime4j.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

final class StringArrayMap$2
  implements Enumeration<String>
{
  private int offset;

  StringArrayMap$2(String[] paramArrayOfString)
  {
  }

  public boolean hasMoreElements()
  {
    return this.offset < this.val$values.length;
  }

  public String nextElement()
  {
    if (this.offset >= this.val$values.length)
      throw new NoSuchElementException();
    String[] arrayOfString = this.val$values;
    int i = this.offset;
    this.offset = (i + 1);
    return arrayOfString[i];
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.StringArrayMap.2
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */