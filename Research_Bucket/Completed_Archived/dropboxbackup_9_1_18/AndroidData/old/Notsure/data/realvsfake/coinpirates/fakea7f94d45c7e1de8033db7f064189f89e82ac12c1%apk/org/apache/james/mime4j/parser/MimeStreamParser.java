package org.apache.james.mime4j.parser;

import java.io.IOException;
import java.io.InputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.BodyDescriptor;

public class MimeStreamParser
{
  private boolean contentDecoding;
  private ContentHandler handler = null;
  private final MimeTokenStream mimeTokenStream;

  public MimeStreamParser()
  {
    this(null);
  }

  public MimeStreamParser(MimeEntityConfig paramMimeEntityConfig)
  {
    if (paramMimeEntityConfig != null);
    for (MimeEntityConfig localMimeEntityConfig = paramMimeEntityConfig.clone(); ; localMimeEntityConfig = new MimeEntityConfig())
    {
      this.mimeTokenStream = new MimeTokenStream(localMimeEntityConfig);
      this.contentDecoding = false;
      return;
    }
  }

  public boolean isContentDecoding()
  {
    return this.contentDecoding;
  }

  public boolean isRaw()
  {
    return this.mimeTokenStream.isRaw();
  }

  public void parse(InputStream paramInputStream)
    throws MimeException, IOException
  {
    this.mimeTokenStream.parse(paramInputStream);
    int i = this.mimeTokenStream.getState();
    InputStream localInputStream;
    switch (i)
    {
    default:
      throw new IllegalStateException("Invalid state: " + i);
    case 12:
      BodyDescriptor localBodyDescriptor = this.mimeTokenStream.getBodyDescriptor();
      if (this.contentDecoding)
      {
        localInputStream = this.mimeTokenStream.getDecodedInputStream();
        this.handler.body(localBodyDescriptor, localInputStream);
      }
    case 11:
    case 5:
    case 1:
    case 7:
    case 9:
    case 4:
    case 8:
    case 2:
    case 10:
    case 3:
    case 0:
    case 6:
      while (true)
      {
        label140: this.mimeTokenStream.next();
        break;
        localInputStream = this.mimeTokenStream.getInputStream();
        break label140;
        this.handler.endBodyPart();
        continue;
        this.handler.endHeader();
        continue;
        this.handler.endMessage();
        continue;
        this.handler.endMultipart();
        continue;
        this.handler.epilogue(this.mimeTokenStream.getInputStream());
        continue;
        this.handler.field(this.mimeTokenStream.getField());
        continue;
        this.handler.preamble(this.mimeTokenStream.getInputStream());
        continue;
        this.handler.raw(this.mimeTokenStream.getInputStream());
        continue;
        this.handler.startBodyPart();
        continue;
        this.handler.startHeader();
        continue;
        this.handler.startMessage();
        continue;
        this.handler.startMultipart(this.mimeTokenStream.getBodyDescriptor());
      }
    case -1:
    }
  }

  public void setContentDecoding(boolean paramBoolean)
  {
    this.contentDecoding = paramBoolean;
  }

  public void setContentHandler(ContentHandler paramContentHandler)
  {
    this.handler = paramContentHandler;
  }

  public void setRaw(boolean paramBoolean)
  {
    this.mimeTokenStream.setRecursionMode(2);
  }

  public void stop()
  {
    this.mimeTokenStream.stop();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.MimeStreamParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */