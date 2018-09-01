package org.apache.james.mime4j.field.address;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.List;;
import org.apache.james.mime4j.field.address.parser.AddressListParser;
import org.apache.james.mime4j.field.address.parser.ParseException;

public class AddressList extends AbstractList<Address>
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private final List<? extends Address> addresses;

  public AddressList(List<? extends Address> paramList, boolean paramBoolean)
  {
    if (paramList != null)
    {
      if (paramBoolean);
      for (Object localObject = paramList; ; localObject = new ArrayList(paramList))
      {
        this.addresses = ((List)localObject);
        return;
      }
    }
    this.addresses = Collections.emptyList();
  }

  public static void main(String[] paramArrayOfString)
    throws Exception
  {
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
    while (true)
      try
      {
        System.out.print("> ");
        String str = localBufferedReader.readLine();
        if ((str.length() == 0) || (str.toLowerCase().equals("exit")) || (str.toLowerCase().equals("quit")))
        {
          System.out.println("Goodbye.");
          return;
        }
        parse(str).print();
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
        Thread.sleep(300L);
      }
  }

  public static AddressList parse(String paramString)
    throws ParseException
  {
    AddressListParser localAddressListParser = new AddressListParser(new StringReader(paramString));
    return Builder.getInstance().buildAddressList(localAddressListParser.parseAddressList());
  }

  public MailboxList flatten()
  {
    Iterator localIterator1 = this.addresses.iterator();
    do
    {
      boolean bool = localIterator1.hasNext();
      i = 0;
      if (!bool)
        break;
    }
    while (((Address)localIterator1.next() instanceof Mailbox));
    int i = 1;
    if (i == 0)
      return new MailboxList(this.addresses, true);
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator2 = this.addresses.iterator();
    while (localIterator2.hasNext())
      ((Address)localIterator2.next()).addMailboxesTo(localArrayList);
    return new MailboxList(localArrayList, false);
  }

  public Address get(int paramInt)
  {
    return (Address)this.addresses.get(paramInt);
  }

  public void print()
  {
    Iterator localIterator = this.addresses.iterator();
    while (localIterator.hasNext())
    {
      Address localAddress = (Address)localIterator.next();
      System.out.println(localAddress.toString());
    }
  }

  public int size()
  {
    return this.addresses.size();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.AddressList
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */