package org.apache.james.mime4j.field.address;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.List;;

public class DomainList extends AbstractList<String>
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private final List<String> domains;

  public DomainList(List<String> paramList, boolean paramBoolean)
  {
    if (paramList != null)
    {
      if (paramBoolean);
      for (Object localObject = paramList; ; localObject = new ArrayList(paramList))
      {
        this.domains = ((List)localObject);
        return;
      }
    }
    this.domains = Collections.emptyList();
  }

  public String get(int paramInt)
  {
    return (String)this.domains.get(paramInt);
  }

  public int size()
  {
    return this.domains.size();
  }

  public String toRouteString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    Iterator localIterator = this.domains.iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      if (localStringBuilder.length() > 0)
        localStringBuilder.append((char)',');
      localStringBuilder.append("@");
      localStringBuilder.append(str);
    }
    return localStringBuilder.toString();
  }

  public String toString()
  {
    return toRouteString();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.DomainList
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */