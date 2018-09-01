package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.field.ParsedField;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.MimeUtil;

public abstract class SimpleContentHandler extends AbstractContentHandler
{
  private Header currHeader;

  public SimpleContentHandler()
  {
  }

  public final void body(BodyDescriptor paramBodyDescriptor, InputStream paramInputStream)
    throws IOException
  {
    if (MimeUtil.isBase64Encoding(paramBodyDescriptor.getTransferEncoding()))
    {
      bodyDecoded(paramBodyDescriptor, new Base64InputStream(paramInputStream));
      return;
    }
    if (MimeUtil.isQuotedPrintableEncoded(paramBodyDescriptor.getTransferEncoding()))
    {
      bodyDecoded(paramBodyDescriptor, new QuotedPrintableInputStream(paramInputStream));
      return;
    }
    bodyDecoded(paramBodyDescriptor, paramInputStream);
  }

  public abstract void bodyDecoded(BodyDescriptor paramBodyDescriptor, InputStream paramInputStream)
    throws IOException;

  public final void endHeader()
  {
    Header localHeader = this.currHeader;
    this.currHeader = null;
    headers(localHeader);
  }

  public final void field(Field paramField)
    throws MimeException
  {
    ParsedField localParsedField = AbstractField.parse(paramField.getRaw());
    this.currHeader.addField(localParsedField);
  }

  public abstract void headers(Header paramHeader);

  public final void startHeader()
  {
    this.currHeader = new Header();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.SimpleContentHandler
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */