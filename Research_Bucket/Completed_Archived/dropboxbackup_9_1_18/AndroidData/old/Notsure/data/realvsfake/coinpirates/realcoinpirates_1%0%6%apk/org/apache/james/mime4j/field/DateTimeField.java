package org.apache.james.mime4j.field;

import java.io.StringReader;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.datetime.DateTime;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.field.datetime.parser.ParseException;
import org.apache.james.mime4j.field.datetime.parser.TokenMgrError;
import org.apache.james.mime4j.util.ByteSequence;

public class DateTimeField extends AbstractField
{
  static final FieldParser PARSER = new FieldParser()
  {
    public ParsedField parse(String paramAnonymousString1, String paramAnonymousString2, ByteSequence paramAnonymousByteSequence)
    {
      return new DateTimeField(paramAnonymousString1, paramAnonymousString2, paramAnonymousByteSequence);
    }
  };
  private static Log log = LogFactory.getLog(DateTimeField.class);
  private Date date;
  private ParseException parseException;
  private boolean parsed = false;

  DateTimeField(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    super(paramString1, paramString2, paramByteSequence);
  }

  private void parse()
  {
    str = getBody();
    try
    {
      this.date = new DateTimeParser(new StringReader(str)).parseAll().getDate();
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
    catch (TokenMgrError localTokenMgrError)
    {
      while (true)
      {
        if (log.isDebugEnabled())
          log.debug("Parsing value '" + str + "': " + localTokenMgrError.getMessage());
        this.parseException = new ParseException(localTokenMgrError.getMessage());
      }
    }
  }

  public Date getDate()
  {
    if (!this.parsed)
      parse();
    return this.date;
  }

  public ParseException getParseException()
  {
    if (!this.parsed)
      parse();
    return this.parseException;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.DateTimeField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */