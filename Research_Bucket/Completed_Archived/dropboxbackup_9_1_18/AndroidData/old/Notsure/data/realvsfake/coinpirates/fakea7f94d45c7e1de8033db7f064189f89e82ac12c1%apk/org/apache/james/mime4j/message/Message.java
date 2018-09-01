package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collection;;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.AddressListField;
import org.apache.james.mime4j.field.DateTimeField;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.MailboxField;
import org.apache.james.mime4j.field.MailboxListField;
import org.apache.james.mime4j.field.UnstructuredField;
import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.StorageProvider;

public class Message extends Entity
  implements Body
{
  public Message()
  {
  }

  public Message(InputStream paramInputStream)
    throws IOException, MimeIOException
  {
    this(paramInputStream, null, DefaultStorageProvider.getInstance());
  }

  public Message(InputStream paramInputStream, MimeEntityConfig paramMimeEntityConfig)
    throws IOException, MimeIOException
  {
    this(paramInputStream, paramMimeEntityConfig, DefaultStorageProvider.getInstance());
  }

  public Message(InputStream paramInputStream, MimeEntityConfig paramMimeEntityConfig, StorageProvider paramStorageProvider)
    throws IOException, MimeIOException
  {
    try
    {
      MimeStreamParser localMimeStreamParser = new MimeStreamParser(paramMimeEntityConfig);
      localMimeStreamParser.setContentHandler(new MessageBuilder(this, paramStorageProvider));
      localMimeStreamParser.parse(paramInputStream);
      return;
    }
    catch (MimeException localMimeException)
    {
      throw new MimeIOException(localMimeException);
    }
  }

  public Message(Message paramMessage)
  {
    super(paramMessage);
  }

  private AddressList getAddressList(String paramString)
  {
    AddressListField localAddressListField = (AddressListField)obtainField(paramString);
    if (localAddressListField == null)
      return null;
    return localAddressListField.getAddressList();
  }

  private Mailbox getMailbox(String paramString)
  {
    MailboxField localMailboxField = (MailboxField)obtainField(paramString);
    if (localMailboxField == null)
      return null;
    return localMailboxField.getMailbox();
  }

  private MailboxList getMailboxList(String paramString)
  {
    MailboxListField localMailboxListField = (MailboxListField)obtainField(paramString);
    if (localMailboxListField == null)
      return null;
    return localMailboxListField.getMailboxList();
  }

  private void setAddressList(String paramString, Collection<Address> paramCollection)
  {
    Header localHeader = obtainHeader();
    if ((paramCollection == null) || (paramCollection.isEmpty()))
    {
      localHeader.removeFields(paramString);
      return;
    }
    localHeader.setField(Fields.addressList(paramString, paramCollection));
  }

  private void setAddressList(String paramString, Address paramAddress)
  {
    if (paramAddress == null);
    for (Object localObject = null; ; localObject = Collections.singleton(paramAddress))
    {
      setAddressList(paramString, (Collection)localObject);
      return;
    }
  }

  private void setAddressList(String paramString, Address[] paramArrayOfAddress)
  {
    if (paramArrayOfAddress == null);
    for (Object localObject = null; ; localObject = Arrays.asList(paramArrayOfAddress))
    {
      setAddressList(paramString, (Collection)localObject);
      return;
    }
  }

  private void setMailbox(String paramString, Mailbox paramMailbox)
  {
    Header localHeader = obtainHeader();
    if (paramMailbox == null)
    {
      localHeader.removeFields(paramString);
      return;
    }
    localHeader.setField(Fields.mailbox(paramString, paramMailbox));
  }

  private void setMailboxList(String paramString, Collection<Mailbox> paramCollection)
  {
    Header localHeader = obtainHeader();
    if ((paramCollection == null) || (paramCollection.isEmpty()))
    {
      localHeader.removeFields(paramString);
      return;
    }
    localHeader.setField(Fields.mailboxList(paramString, paramCollection));
  }

  private void setMailboxList(String paramString, Mailbox paramMailbox)
  {
    if (paramMailbox == null);
    for (Object localObject = null; ; localObject = Collections.singleton(paramMailbox))
    {
      setMailboxList(paramString, (Collection)localObject);
      return;
    }
  }

  private void setMailboxList(String paramString, Mailbox[] paramArrayOfMailbox)
  {
    if (paramArrayOfMailbox == null);
    for (Object localObject = null; ; localObject = Arrays.asList(paramArrayOfMailbox))
    {
      setMailboxList(paramString, (Collection)localObject);
      return;
    }
  }

  public void createMessageId(String paramString)
  {
    obtainHeader().setField(Fields.messageId(paramString));
  }

  public AddressList getBcc()
  {
    return getAddressList("Bcc");
  }

  public AddressList getCc()
  {
    return getAddressList("Cc");
  }

  public Date getDate()
  {
    DateTimeField localDateTimeField = (DateTimeField)obtainField("Date");
    if (localDateTimeField == null)
      return null;
    return localDateTimeField.getDate();
  }

  public MailboxList getFrom()
  {
    return getMailboxList("From");
  }

  public String getMessageId()
  {
    Field localField = obtainField("Message-ID");
    if (localField == null)
      return null;
    return localField.getBody();
  }

  public AddressList getReplyTo()
  {
    return getAddressList("Reply-To");
  }

  public Mailbox getSender()
  {
    return getMailbox("Sender");
  }

  public String getSubject()
  {
    UnstructuredField localUnstructuredField = (UnstructuredField)obtainField("Subject");
    if (localUnstructuredField == null)
      return null;
    return localUnstructuredField.getValue();
  }

  public AddressList getTo()
  {
    return getAddressList("To");
  }

  public void setBcc(Collection<Address> paramCollection)
  {
    setAddressList("Bcc", paramCollection);
  }

  public void setBcc(Address paramAddress)
  {
    setAddressList("Bcc", paramAddress);
  }

  public void setBcc(Address[] paramArrayOfAddress)
  {
    setAddressList("Bcc", paramArrayOfAddress);
  }

  public void setCc(Collection<Address> paramCollection)
  {
    setAddressList("Cc", paramCollection);
  }

  public void setCc(Address paramAddress)
  {
    setAddressList("Cc", paramAddress);
  }

  public void setCc(Address[] paramArrayOfAddress)
  {
    setAddressList("Cc", paramArrayOfAddress);
  }

  public void setDate(Date paramDate)
  {
    setDate(paramDate, null);
  }

  public void setDate(Date paramDate, TimeZone paramTimeZone)
  {
    Header localHeader = obtainHeader();
    if (paramDate == null)
    {
      localHeader.removeFields("Date");
      return;
    }
    localHeader.setField(Fields.date("Date", paramDate, paramTimeZone));
  }

  public void setFrom(Collection<Mailbox> paramCollection)
  {
    setMailboxList("From", paramCollection);
  }

  public void setFrom(Mailbox paramMailbox)
  {
    setMailboxList("From", paramMailbox);
  }

  public void setFrom(Mailbox[] paramArrayOfMailbox)
  {
    setMailboxList("From", paramArrayOfMailbox);
  }

  public void setReplyTo(Collection<Address> paramCollection)
  {
    setAddressList("Reply-To", paramCollection);
  }

  public void setReplyTo(Address paramAddress)
  {
    setAddressList("Reply-To", paramAddress);
  }

  public void setReplyTo(Address[] paramArrayOfAddress)
  {
    setAddressList("Reply-To", paramArrayOfAddress);
  }

  public void setSender(Mailbox paramMailbox)
  {
    setMailbox("Sender", paramMailbox);
  }

  public void setSubject(String paramString)
  {
    Header localHeader = obtainHeader();
    if (paramString == null)
    {
      localHeader.removeFields("Subject");
      return;
    }
    localHeader.setField(Fields.subject(paramString));
  }

  public void setTo(Collection<Address> paramCollection)
  {
    setAddressList("To", paramCollection);
  }

  public void setTo(Address paramAddress)
  {
    setAddressList("To", paramAddress);
  }

  public void setTo(Address[] paramArrayOfAddress)
  {
    setAddressList("To", paramArrayOfAddress);
  }

  public void writeTo(OutputStream paramOutputStream)
    throws IOException
  {
    MessageWriter.DEFAULT.writeEntity(this, paramOutputStream);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.Message
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */