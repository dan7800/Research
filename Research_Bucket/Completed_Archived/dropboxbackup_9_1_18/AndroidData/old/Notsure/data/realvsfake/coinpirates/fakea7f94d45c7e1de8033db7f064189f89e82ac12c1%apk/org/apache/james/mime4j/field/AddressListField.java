package org.apache.james.mime4j.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.parser.ParseException;
import org.apache.james.mime4j.util.ByteSequence;

public class AddressListField extends AbstractField
{
  static final FieldParser PARSER = new FieldParser()
  {
    public ParsedField parse(String paramAnonymousString1, String paramAnonymousString2, ByteSequence paramAnonymousByteSequence)
    {
      return new AddressListField(paramAnonymousString1, paramAnonymousString2, paramAnonymousByteSequence);
    }
  };
  private static Log log = LogFactory.getLog(AddressListField.class);
  private AddressList addressList;
  private ParseException parseException;
  private boolean parsed = false;

  AddressListField(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    super(paramString1, paramString2, paramByteSequence);
  }

  private void parse()
  {
    str = getBody();
    try
    {
      this.addressList = AddressList.parse(str);
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

  public AddressList getAddressList()
  {
    if (!this.parsed)
      parse();
    return this.addressList;
  }

  public ParseException getParseException()
  {
    if (!this.parsed)
      parse();
    return this.parseException;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.AddressListField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */