package org.apache.james.mime4j.field.address;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.List;;

public class MailboxList extends AbstractList<Mailbox>
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private final List<Mailbox> mailboxes;

  public MailboxList(List<Mailbox> paramList, boolean paramBoolean)
  {
    if (paramList != null)
    {
      if (paramBoolean);
      for (Object localObject = paramList; ; localObject = new ArrayList(paramList))
      {
        this.mailboxes = ((List)localObject);
        return;
      }
    }
    this.mailboxes = Collections.emptyList();
  }

  public Mailbox get(int paramInt)
  {
    return (Mailbox)this.mailboxes.get(paramInt);
  }

  public void print()
  {
    for (int i = 0; i < size(); i++)
    {
      Mailbox localMailbox = get(i);
      System.out.println(localMailbox.toString());
    }
  }

  public int size()
  {
    return this.mailboxes.size();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.MailboxList
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */