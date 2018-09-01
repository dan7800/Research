package org.apache.james.mime4j.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

final class StringArrayMap$1
  implements Enumeration<String>
{
  private Object value = this.val$pValue;

  StringArrayMap$1(Object paramObject)
  {
  }

  public boolean hasMoreElements()
  {
    return this.value != null;
  }

  public String nextElement()
  {
    if (this.value == null)
      throw new NoSuchElementException();
    String str = (String)this.value;
    this.value = null;
    return str;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.StringArrayMap.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */