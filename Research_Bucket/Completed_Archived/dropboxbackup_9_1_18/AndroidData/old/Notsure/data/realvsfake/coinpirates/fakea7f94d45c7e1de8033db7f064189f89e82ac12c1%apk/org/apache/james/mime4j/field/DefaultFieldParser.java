package org.apache.james.mime4j.field;

public class DefaultFieldParser extends DelegatingFieldParser
{
  public DefaultFieldParser()
  {
    setFieldParser("Content-Transfer-Encoding", ContentTransferEncodingField.PARSER);
    setFieldParser("Content-Type", ContentTypeField.PARSER);
    setFieldParser("Content-Disposition", ContentDispositionField.PARSER);
    FieldParser localFieldParser1 = DateTimeField.PARSER;
    setFieldParser("Date", localFieldParser1);
    setFieldParser("Resent-Date", localFieldParser1);
    FieldParser localFieldParser2 = MailboxListField.PARSER;
    setFieldParser("From", localFieldParser2);
    setFieldParser("Resent-From", localFieldParser2);
    FieldParser localFieldParser3 = MailboxField.PARSER;
    setFieldParser("Sender", localFieldParser3);
    setFieldParser("Resent-Sender", localFieldParser3);
    FieldParser localFieldParser4 = AddressListField.PARSER;
    setFieldParser("To", localFieldParser4);
    setFieldParser("Resent-To", localFieldParser4);
    setFieldParser("Cc", localFieldParser4);
    setFieldParser("Resent-Cc", localFieldParser4);
    setFieldParser("Bcc", localFieldParser4);
    setFieldParser("Resent-Bcc", localFieldParser4);
    setFieldParser("Reply-To", localFieldParser4);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.DefaultFieldParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */