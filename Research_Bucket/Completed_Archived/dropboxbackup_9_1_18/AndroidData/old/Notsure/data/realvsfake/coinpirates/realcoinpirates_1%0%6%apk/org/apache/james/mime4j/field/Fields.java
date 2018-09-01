package org.apache.james.mime4j.field;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.codec.EncoderUtil.Usage;
import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

public class Fields
{
  private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("[\\x21-\\x39\\x3b-\\x7e]+");

  private Fields()
  {
  }

  public static AddressListField addressList(String paramString, Iterable<Address> paramIterable)
  {
    checkValidFieldName(paramString);
    return addressList0(paramString, paramIterable);
  }

  private static AddressListField addressList0(String paramString, Iterable<Address> paramIterable)
  {
    String str = encodeAddresses(paramIterable);
    return (AddressListField)parse(AddressListField.PARSER, paramString, str);
  }

  public static AddressListField bcc(Iterable<Address> paramIterable)
  {
    return addressList0("Bcc", paramIterable);
  }

  public static AddressListField bcc(Address paramAddress)
  {
    return addressList0("Bcc", Collections.singleton(paramAddress));
  }

  public static AddressListField bcc(Address[] paramArrayOfAddress)
  {
    return addressList0("Bcc", Arrays.asList(paramArrayOfAddress));
  }

  public static AddressListField cc(Iterable<Address> paramIterable)
  {
    return addressList0("Cc", paramIterable);
  }

  public static AddressListField cc(Address paramAddress)
  {
    return addressList0("Cc", Collections.singleton(paramAddress));
  }

  public static AddressListField cc(Address[] paramArrayOfAddress)
  {
    return addressList0("Cc", Arrays.asList(paramArrayOfAddress));
  }

  private static void checkValidFieldName(String paramString)
  {
    if (!FIELD_NAME_PATTERN.matcher(paramString).matches())
      throw new IllegalArgumentException("Invalid field name");
  }

  public static ContentDispositionField contentDisposition(String paramString)
  {
    return (ContentDispositionField)parse(ContentDispositionField.PARSER, "Content-Disposition", paramString);
  }

  public static ContentDispositionField contentDisposition(String paramString1, String paramString2)
  {
    return contentDisposition(paramString1, paramString2, -1L, null, null, null);
  }

  public static ContentDispositionField contentDisposition(String paramString1, String paramString2, long paramLong)
  {
    return contentDisposition(paramString1, paramString2, paramLong, null, null, null);
  }

  public static ContentDispositionField contentDisposition(String paramString1, String paramString2, long paramLong, Date paramDate1, Date paramDate2, Date paramDate3)
  {
    HashMap localHashMap = new HashMap();
    if (paramString2 != null)
      localHashMap.put("filename", paramString2);
    if (paramLong >= 0L)
      localHashMap.put("size", Long.toString(paramLong));
    if (paramDate1 != null)
      localHashMap.put("creation-date", MimeUtil.formatDate(paramDate1, null));
    if (paramDate2 != null)
      localHashMap.put("modification-date", MimeUtil.formatDate(paramDate2, null));
    if (paramDate3 != null)
      localHashMap.put("read-date", MimeUtil.formatDate(paramDate3, null));
    return contentDisposition(paramString1, localHashMap);
  }

  public static ContentDispositionField contentDisposition(String paramString, Map<String, String> paramMap)
  {
    if (!isValidDispositionType(paramString))
      throw new IllegalArgumentException();
    if ((paramMap == null) || (paramMap.isEmpty()))
      return (ContentDispositionField)parse(ContentDispositionField.PARSER, "Content-Disposition", paramString);
    StringBuilder localStringBuilder = new StringBuilder(paramString);
    Iterator localIterator = paramMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      localStringBuilder.append("; ");
      localStringBuilder.append(EncoderUtil.encodeHeaderParameter((String)localEntry.getKey(), (String)localEntry.getValue()));
    }
    return contentDisposition(localStringBuilder.toString());
  }

  public static ContentTransferEncodingField contentTransferEncoding(String paramString)
  {
    return (ContentTransferEncodingField)parse(ContentTransferEncodingField.PARSER, "Content-Transfer-Encoding", paramString);
  }

  public static ContentTypeField contentType(String paramString)
  {
    return (ContentTypeField)parse(ContentTypeField.PARSER, "Content-Type", paramString);
  }

  public static ContentTypeField contentType(String paramString, Map<String, String> paramMap)
  {
    if (!isValidMimeType(paramString))
      throw new IllegalArgumentException();
    if ((paramMap == null) || (paramMap.isEmpty()))
      return (ContentTypeField)parse(ContentTypeField.PARSER, "Content-Type", paramString);
    StringBuilder localStringBuilder = new StringBuilder(paramString);
    Iterator localIterator = paramMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      localStringBuilder.append("; ");
      localStringBuilder.append(EncoderUtil.encodeHeaderParameter((String)localEntry.getKey(), (String)localEntry.getValue()));
    }
    return contentType(localStringBuilder.toString());
  }

  public static DateTimeField date(String paramString, Date paramDate)
  {
    checkValidFieldName(paramString);
    return date0(paramString, paramDate, null);
  }

  public static DateTimeField date(String paramString, Date paramDate, TimeZone paramTimeZone)
  {
    checkValidFieldName(paramString);
    return date0(paramString, paramDate, paramTimeZone);
  }

  public static DateTimeField date(Date paramDate)
  {
    return date0("Date", paramDate, null);
  }

  private static DateTimeField date0(String paramString, Date paramDate, TimeZone paramTimeZone)
  {
    String str = MimeUtil.formatDate(paramDate, paramTimeZone);
    return (DateTimeField)parse(DateTimeField.PARSER, paramString, str);
  }

  private static String encodeAddresses(Iterable<? extends Address> paramIterable)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    Iterator localIterator = paramIterable.iterator();
    while (localIterator.hasNext())
    {
      Address localAddress = (Address)localIterator.next();
      if (localStringBuilder.length() > 0)
        localStringBuilder.append(", ");
      localStringBuilder.append(localAddress.getEncodedString());
    }
    return localStringBuilder.toString();
  }

  public static MailboxListField from(Iterable<Mailbox> paramIterable)
  {
    return mailboxList0("From", paramIterable);
  }

  public static MailboxListField from(Mailbox paramMailbox)
  {
    return mailboxList0("From", Collections.singleton(paramMailbox));
  }

  public static MailboxListField from(Mailbox[] paramArrayOfMailbox)
  {
    return mailboxList0("From", Arrays.asList(paramArrayOfMailbox));
  }

  private static boolean isValidDispositionType(String paramString)
  {
    if (paramString == null)
      return false;
    return EncoderUtil.isToken(paramString);
  }

  private static boolean isValidMimeType(String paramString)
  {
    if (paramString == null)
      return false;
    int i = paramString.indexOf('/');
    if (i == -1)
      return false;
    String str1 = paramString.substring(0, i);
    String str2 = paramString.substring(i + 1);
    return (EncoderUtil.isToken(str1)) && (EncoderUtil.isToken(str2));
  }

  public static MailboxField mailbox(String paramString, Mailbox paramMailbox)
  {
    checkValidFieldName(paramString);
    return mailbox0(paramString, paramMailbox);
  }

  private static MailboxField mailbox0(String paramString, Mailbox paramMailbox)
  {
    String str = encodeAddresses(Collections.singleton(paramMailbox));
    return (MailboxField)parse(MailboxField.PARSER, paramString, str);
  }

  public static MailboxListField mailboxList(String paramString, Iterable<Mailbox> paramIterable)
  {
    checkValidFieldName(paramString);
    return mailboxList0(paramString, paramIterable);
  }

  private static MailboxListField mailboxList0(String paramString, Iterable<Mailbox> paramIterable)
  {
    String str = encodeAddresses(paramIterable);
    return (MailboxListField)parse(MailboxListField.PARSER, paramString, str);
  }

  public static Field messageId(String paramString)
  {
    String str = MimeUtil.createUniqueMessageId(paramString);
    return parse(UnstructuredField.PARSER, "Message-ID", str);
  }

  private static <F extends Field> F parse(FieldParser paramFieldParser, String paramString1, String paramString2)
  {
    return paramFieldParser.parse(paramString1, paramString2, ContentUtil.encode(MimeUtil.fold(paramString1 + ": " + paramString2, 0)));
  }

  public static AddressListField replyTo(Iterable<Address> paramIterable)
  {
    return addressList0("Reply-To", paramIterable);
  }

  public static AddressListField replyTo(Address paramAddress)
  {
    return addressList0("Reply-To", Collections.singleton(paramAddress));
  }

  public static AddressListField replyTo(Address[] paramArrayOfAddress)
  {
    return addressList0("Reply-To", Arrays.asList(paramArrayOfAddress));
  }

  public static MailboxField sender(Mailbox paramMailbox)
  {
    return mailbox0("Sender", paramMailbox);
  }

  public static UnstructuredField subject(String paramString)
  {
    int i = 2 + "Subject".length();
    String str = EncoderUtil.encodeIfNecessary(paramString, EncoderUtil.Usage.TEXT_TOKEN, i);
    return (UnstructuredField)parse(UnstructuredField.PARSER, "Subject", str);
  }

  public static AddressListField to(Iterable<Address> paramIterable)
  {
    return addressList0("To", paramIterable);
  }

  public static AddressListField to(Address paramAddress)
  {
    return addressList0("To", Collections.singleton(paramAddress));
  }

  public static AddressListField to(Address[] paramArrayOfAddress)
  {
    return addressList0("To", Arrays.asList(paramArrayOfAddress));
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.Fields
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */