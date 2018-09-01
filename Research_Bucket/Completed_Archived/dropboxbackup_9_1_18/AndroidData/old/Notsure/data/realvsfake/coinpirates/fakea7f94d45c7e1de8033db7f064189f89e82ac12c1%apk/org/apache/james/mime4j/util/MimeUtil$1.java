package org.apache.james.mime4j.util;

import java.text.DateFormat;

final class MimeUtil$1 extends ThreadLocal<DateFormat>
{
  MimeUtil$1()
  {
  }

  protected DateFormat initialValue()
  {
    return new MimeUtil.Rfc822DateFormat();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.MimeUtil.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */