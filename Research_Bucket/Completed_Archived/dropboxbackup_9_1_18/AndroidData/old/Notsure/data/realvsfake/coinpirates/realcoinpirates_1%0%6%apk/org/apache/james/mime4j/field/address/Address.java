package org.apache.james.mime4j.field.address;

import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import org.apache.james.mime4j.field.address.parser.AddressListParser;
import org.apache.james.mime4j.field.address.parser.ParseException;

public abstract class Address
  implements Serializable
{
  private static final long serialVersionUID = 634090661990433426L;

  public Address()
  {
  }

  public static Address parse(String paramString)
  {
    AddressListParser localAddressListParser = new AddressListParser(new StringReader(paramString));
    try
    {
      Address localAddress = Builder.getInstance().buildAddress(localAddressListParser.parseAddress());
      return localAddress;
    }
    catch (ParseException localParseException)
    {
      throw new IllegalArgumentException(localParseException);
    }
  }

  final void addMailboxesTo(List<Mailbox> paramList)
  {
    doAddMailboxesTo(paramList);
  }

  protected abstract void doAddMailboxesTo(List<Mailbox> paramList);

  public final String getDisplayString()
  {
    return getDisplayString(false);
  }

  public abstract String getDisplayString(boolean paramBoolean);

  public abstract String getEncodedString();

  public String toString()
  {
    return getDisplayString(false);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.Address
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */