package org.apache.james.mime4j.message;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeStreamParser;

class Header$1 extends AbstractContentHandler
{
  Header$1(Header paramHeader, MimeStreamParser paramMimeStreamParser)
  {
  }

  public void endHeader()
  {
    this.val$parser.stop();
  }

  public void field(Field paramField)
    throws MimeException
  {
    this.this$0.addField(paramField);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.Header.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */