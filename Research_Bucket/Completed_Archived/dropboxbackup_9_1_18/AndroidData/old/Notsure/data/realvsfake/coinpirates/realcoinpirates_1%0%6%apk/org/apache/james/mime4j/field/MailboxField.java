package org.apache.james.mime4j.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.field.address.parser.ParseException;
import org.apache.james.mime4j.util.ByteSequence;

public class MailboxField extends AbstractField
{
  static final FieldParser PARSER = new FieldParser()
  {
    public ParsedField parse(String paramAnonymousString1, String paramAnonymousString2, ByteSequence paramAnonymousByteSequence)
    {
      return new MailboxField(paramAnonymousString1, paramAnonymousString2, paramAnonymousByteSequence);
    }
  };
  private static Log log = LogFactory.getLog(MailboxField.class);
  private Mailbox mailbox;
  private ParseException parseException;
  private boolean parsed = false;

  MailboxField(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    super(paramString1, paramString2, paramByteSequence);
  }

  private void parse()
  {
    str = getBody();
    try
    {
      MailboxList localMailboxList = AddressList.parse(str).flatten();
      if (localMailboxList.size() > 0)
        this.mailbox = localMailboxList.get(0);
      this.parsed = true;
      return;
    }
    catch (ParseException localParseException)
    {
      while (true)
      {
        if (log.isDebugEnabled())
          log.debug("Parsing value '" + str + "': " + localParseException.getMessage());
        this.parseException = localParseException;
      }
    }
  }

  public Mailbox getMailbox()
  {
    if (!this.parsed)
      parse();
    return this.mailbox;
  }

  public ParseException getParseException()
  {
    if (!this.parsed)
      parse();
    return this.parseException;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.MailboxField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */