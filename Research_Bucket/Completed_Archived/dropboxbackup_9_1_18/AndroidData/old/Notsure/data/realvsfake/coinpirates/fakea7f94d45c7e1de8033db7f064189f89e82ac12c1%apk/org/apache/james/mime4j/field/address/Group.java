package org.apache.james.mime4j.field.address;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.james.mime4j.codec.EncoderUtil;

public class Group extends Address
{
  private static final long serialVersionUID = 1L;
  private final MailboxList mailboxList;
  private final String name;

  public Group(String paramString, Collection<Mailbox> paramCollection)
  {
    this(paramString, new MailboxList(new ArrayList(paramCollection), true));
  }

  public Group(String paramString, MailboxList paramMailboxList)
  {
    if (paramString == null)
      throw new IllegalArgumentException();
    if (paramMailboxList == null)
      throw new IllegalArgumentException();
    this.name = paramString;
    this.mailboxList = paramMailboxList;
  }

  public Group(String paramString, Mailbox[] paramArrayOfMailbox)
  {
    this(paramString, new MailboxList(Arrays.asList(paramArrayOfMailbox), true));
  }

  public static Group parse(String paramString)
  {
    Address localAddress = Address.parse(paramString);
    if (!(localAddress instanceof Group))
      throw new IllegalArgumentException("Not a group address");
    return (Group)localAddress;
  }

  protected void doAddMailboxesTo(List<Mailbox> paramList)
  {
    Iterator localIterator = this.mailboxList.iterator();
    while (localIterator.hasNext())
      paramList.add((Mailbox)localIterator.next());
  }

  public String getDisplayString(boolean paramBoolean)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(this.name);
    localStringBuilder.append((char)':');
    int i = 1;
    Iterator localIterator = this.mailboxList.iterator();
    if (localIterator.hasNext())
    {
      Mailbox localMailbox = (Mailbox)localIterator.next();
      if (i != 0)
        i = 0;
      while (true)
      {
        localStringBuilder.append((char)' ');
        localStringBuilder.append(localMailbox.getDisplayString(paramBoolean));
        break;
        localStringBuilder.append((char)',');
      }
    }
    localStringBuilder.append(";");
    return localStringBuilder.toString();
  }

  public String getEncodedString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(EncoderUtil.encodeAddressDisplayName(this.name));
    localStringBuilder.append((char)':');
    int i = 1;
    Iterator localIterator = this.mailboxList.iterator();
    if (localIterator.hasNext())
    {
      Mailbox localMailbox = (Mailbox)localIterator.next();
      if (i != 0)
        i = 0;
      while (true)
      {
        localStringBuilder.append((char)' ');
        localStringBuilder.append(localMailbox.getEncodedString());
        break;
        localStringBuilder.append((char)',');
      }
    }
    localStringBuilder.append((char)';');
    return localStringBuilder.toString();
  }

  public MailboxList getMailboxes()
  {
    return this.mailboxList;
  }

  public String getName()
  {
    return this.name;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.Group
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */