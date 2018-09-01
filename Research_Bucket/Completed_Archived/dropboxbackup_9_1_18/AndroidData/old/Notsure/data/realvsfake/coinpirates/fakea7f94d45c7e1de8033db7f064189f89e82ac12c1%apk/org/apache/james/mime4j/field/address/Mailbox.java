package org.apache.james.mime4j.field.address;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.field.address.parser.AddressListParser;
import org.apache.james.mime4j.field.address.parser.ParseException;

public class Mailbox extends Address
{
  private static final DomainList EMPTY_ROUTE_LIST = new DomainList(Collections.emptyList(), true);
  private static final long serialVersionUID = 1L;
  private final String domain;
  private final String localPart;
  private final String name;
  private final DomainList route;

  public Mailbox(String paramString1, String paramString2)
  {
    this(null, null, paramString1, paramString2);
  }

  public Mailbox(String paramString1, String paramString2, String paramString3)
  {
    this(paramString1, null, paramString2, paramString3);
  }

  public Mailbox(String paramString1, DomainList paramDomainList, String paramString2, String paramString3)
  {
    if ((paramString2 == null) || (paramString2.length() == 0))
      throw new IllegalArgumentException();
    String str1;
    DomainList localDomainList;
    if ((paramString1 == null) || (paramString1.length() == 0))
    {
      str1 = null;
      this.name = str1;
      if (paramDomainList != null)
        break label92;
      localDomainList = EMPTY_ROUTE_LIST;
      label52: this.route = localDomainList;
      this.localPart = paramString2;
      if ((paramString3 != null) && (paramString3.length() != 0))
        break label98;
    }
    label92: label98: for (String str2 = null; ; str2 = paramString3)
    {
      this.domain = str2;
      return;
      str1 = paramString1;
      break;
      localDomainList = paramDomainList;
      break label52;
    }
  }

  Mailbox(String paramString, Mailbox paramMailbox)
  {
    this(paramString, paramMailbox.getRoute(), paramMailbox.getLocalPart(), paramMailbox.getDomain());
  }

  public Mailbox(DomainList paramDomainList, String paramString1, String paramString2)
  {
    this(null, paramDomainList, paramString1, paramString2);
  }

  private Object getCanonicalizedAddress()
  {
    if (this.domain == null)
      return this.localPart;
    return this.localPart + '@' + this.domain.toLowerCase(Locale.US);
  }

  public static Mailbox parse(String paramString)
  {
    AddressListParser localAddressListParser = new AddressListParser(new StringReader(paramString));
    try
    {
      Mailbox localMailbox = Builder.getInstance().buildMailbox(localAddressListParser.parseMailbox());
      return localMailbox;
    }
    catch (ParseException localParseException)
    {
      throw new IllegalArgumentException(localParseException);
    }
  }

  protected final void doAddMailboxesTo(List<Mailbox> paramList)
  {
    paramList.add(this);
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject == this)
      return true;
    if (!(paramObject instanceof Mailbox))
      return false;
    Mailbox localMailbox = (Mailbox)paramObject;
    return getCanonicalizedAddress().equals(localMailbox.getCanonicalizedAddress());
  }

  public String getAddress()
  {
    if (this.domain == null)
      return this.localPart;
    return this.localPart + '@' + this.domain;
  }

  public String getDisplayString(boolean paramBoolean)
  {
    boolean bool1;
    boolean bool2;
    if (this.route != null)
    {
      bool1 = true;
      bool2 = paramBoolean & bool1;
      if ((this.name == null) && (!bool2))
        break label158;
    }
    label158: for (int i = 1; ; i = 0)
    {
      StringBuilder localStringBuilder = new StringBuilder();
      if (this.name != null)
      {
        localStringBuilder.append(this.name);
        localStringBuilder.append((char)' ');
      }
      if (i != 0)
        localStringBuilder.append((char)'<');
      if (bool2)
      {
        localStringBuilder.append(this.route.toRouteString());
        localStringBuilder.append((char)':');
      }
      localStringBuilder.append(this.localPart);
      if (this.domain != null)
      {
        localStringBuilder.append((char)'@');
        localStringBuilder.append(this.domain);
      }
      if (i != 0)
        localStringBuilder.append((char)'>');
      return localStringBuilder.toString();
      bool1 = false;
      break;
    }
  }

  public String getDomain()
  {
    return this.domain;
  }

  public String getEncodedString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    if (this.name != null)
    {
      localStringBuilder.append(EncoderUtil.encodeAddressDisplayName(this.name));
      localStringBuilder.append(" <");
    }
    localStringBuilder.append(EncoderUtil.encodeAddressLocalPart(this.localPart));
    if (this.domain != null)
    {
      localStringBuilder.append((char)'@');
      localStringBuilder.append(this.domain);
    }
    if (this.name != null)
      localStringBuilder.append((char)'>');
    return localStringBuilder.toString();
  }

  public String getLocalPart()
  {
    return this.localPart;
  }

  public String getName()
  {
    return this.name;
  }

  public DomainList getRoute()
  {
    return this.route;
  }

  public int hashCode()
  {
    return getCanonicalizedAddress().hashCode();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.Mailbox
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */