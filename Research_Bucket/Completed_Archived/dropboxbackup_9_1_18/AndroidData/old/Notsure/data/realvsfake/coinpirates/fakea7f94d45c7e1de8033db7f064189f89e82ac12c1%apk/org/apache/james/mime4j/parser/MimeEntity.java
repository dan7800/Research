package org.apache.james.mime4j.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStream;;
import org.apache.commons.logging.Log;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.descriptor.MutableBodyDescriptor;
import org.apache.james.mime4j.io.BufferedLineReaderInputStream;
import org.apache.james.mime4j.io.LimitedInputStream;
import org.apache.james.mime4j.io.LineNumberSource;
import org.apache.james.mime4j.io.LineReaderInputStream;
import org.apache.james.mime4j.io.LineReaderInputStreamAdaptor;
import org.apache.james.mime4j.io.MimeBoundaryInputStream;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

public class MimeEntity extends AbstractEntity
{
  private static final int T_IN_BODYPART = -2;
  private static final int T_IN_MESSAGE = -3;
  private LineReaderInputStreamAdaptor dataStream;
  private final BufferedLineReaderInputStream inbuffer;
  private final LineNumberSource lineSource;
  private MimeBoundaryInputStream mimeStream;
  private int recursionMode;
  private boolean skipHeader;
  private byte[] tmpbuf;

  public MimeEntity(LineNumberSource paramLineNumberSource, BufferedLineReaderInputStream paramBufferedLineReaderInputStream, BodyDescriptor paramBodyDescriptor, int paramInt1, int paramInt2)
  {
    this(paramLineNumberSource, paramBufferedLineReaderInputStream, paramBodyDescriptor, paramInt1, paramInt2, new MimeEntityConfig());
  }

  public MimeEntity(LineNumberSource paramLineNumberSource, BufferedLineReaderInputStream paramBufferedLineReaderInputStream, BodyDescriptor paramBodyDescriptor, int paramInt1, int paramInt2, MimeEntityConfig paramMimeEntityConfig)
  {
    super(paramBodyDescriptor, paramInt1, paramInt2, paramMimeEntityConfig);
    this.lineSource = paramLineNumberSource;
    this.inbuffer = paramBufferedLineReaderInputStream;
    this.dataStream = new LineReaderInputStreamAdaptor(paramBufferedLineReaderInputStream, paramMimeEntityConfig.getMaxLineLen());
    this.skipHeader = false;
  }

  private void advanceToBoundary()
    throws IOException
  {
    if (!this.dataStream.eof())
    {
      if (this.tmpbuf == null)
        this.tmpbuf = new byte[2048];
      InputStream localInputStream = getLimitedContentStream();
      while (localInputStream.read(this.tmpbuf) != -1);
    }
  }

  private void clearMimeStream()
  {
    this.mimeStream = null;
    this.dataStream = new LineReaderInputStreamAdaptor(this.inbuffer, this.config.getMaxLineLen());
  }

  private void createMimeStream()
    throws MimeException, IOException
  {
    str = this.body.getBoundary();
    i = 2 * str.length();
    if (i < 4096)
      i = 4096;
    try
    {
      if (this.mimeStream != null);
      for (this.mimeStream = new MimeBoundaryInputStream(new BufferedLineReaderInputStream(this.mimeStream, i, this.config.getMaxLineLen()), str); ; this.mimeStream = new MimeBoundaryInputStream(this.inbuffer, str))
      {
        this.dataStream = new LineReaderInputStreamAdaptor(this.mimeStream, this.config.getMaxLineLen());
        return;
        this.inbuffer.ensureCapacity(i);
      }
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new MimeException(localIllegalArgumentException.getMessage(), localIllegalArgumentException);
    }
  }

  private InputStream getLimitedContentStream()
  {
    long l = this.config.getMaxContentLen();
    if (l >= 0L)
      return new LimitedInputStream(this.dataStream, l);
    return this.dataStream;
  }

  private EntityStateMachine nextMessage()
  {
    String str = this.body.getTransferEncoding();
    Object localObject;
    if (MimeUtil.isBase64Encoding(str))
    {
      this.log.debug("base64 encoded message/rfc822 detected");
      localObject = new Base64InputStream(this.dataStream);
    }
    while (this.recursionMode == 2)
    {
      return new RawEntity((InputStream)localObject);
      if (MimeUtil.isQuotedPrintableEncoded(str))
      {
        this.log.debug("quoted-printable encoded message/rfc822 detected");
        localObject = new QuotedPrintableInputStream(this.dataStream);
      }
      else
      {
        localObject = this.dataStream;
      }
    }
    MimeEntity localMimeEntity = new MimeEntity(this.lineSource, new BufferedLineReaderInputStream((InputStream)localObject, 4096, this.config.getMaxLineLen()), this.body, 0, 1, this.config);
    localMimeEntity.setRecursionMode(this.recursionMode);
    return localMimeEntity;
  }

  private EntityStateMachine nextMimeEntity()
  {
    if (this.recursionMode == 2)
      return new RawEntity(this.mimeStream);
    BufferedLineReaderInputStream localBufferedLineReaderInputStream = new BufferedLineReaderInputStream(this.mimeStream, 4096, this.config.getMaxLineLen());
    MimeEntity localMimeEntity = new MimeEntity(this.lineSource, localBufferedLineReaderInputStream, this.body, 10, 11, this.config);
    localMimeEntity.setRecursionMode(this.recursionMode);
    return localMimeEntity;
  }

  public EntityStateMachine advance()
    throws IOException, MimeException
  {
    switch (this.state)
    {
    case -1:
    case 1:
    case 2:
    case 11:
    default:
      if (this.state == this.endState)
        this.state = -1;
    case 0:
    case 10:
    case 3:
    case 4:
    case 5:
    case 6:
    case 8:
    case -2:
    case 9:
    case -3:
    case 7:
    case 12:
      while (true)
      {
        return null;
        if (this.skipHeader)
        {
          this.state = 5;
        }
        else
        {
          this.state = 3;
          continue;
          this.state = 3;
          continue;
          if (parseField());
          for (int i = 4; ; i = 5)
          {
            this.state = i;
            break;
          }
          String str = this.body.getMimeType();
          if (this.recursionMode == 3)
          {
            this.state = 12;
          }
          else if (MimeUtil.isMultipart(str))
          {
            this.state = 6;
            clearMimeStream();
          }
          else
          {
            if ((this.recursionMode != 1) && (MimeUtil.isMessage(str)))
            {
              this.state = -3;
              return nextMessage();
            }
            this.state = 12;
            continue;
            if (this.dataStream.isUsed())
            {
              advanceToBoundary();
              this.state = 7;
            }
            else
            {
              createMimeStream();
              this.state = 8;
              continue;
              advanceToBoundary();
              if (this.mimeStream.isLastPart())
              {
                clearMimeStream();
                this.state = 7;
              }
              else
              {
                clearMimeStream();
                createMimeStream();
                this.state = -2;
                return nextMimeEntity();
                advanceToBoundary();
                if ((this.mimeStream.eof()) && (!this.mimeStream.isLastPart()))
                  monitor(Event.MIME_BODY_PREMATURE_END);
                while (this.mimeStream.isLastPart())
                {
                  clearMimeStream();
                  this.state = 9;
                  break;
                }
                clearMimeStream();
                createMimeStream();
                this.state = -2;
                return nextMimeEntity();
                this.state = 7;
                continue;
                this.state = this.endState;
              }
            }
          }
        }
      }
    }
    throw new IllegalStateException("Invalid state: " + stateToString(this.state));
  }

  public InputStream getContentStream()
  {
    switch (this.state)
    {
    case 7:
    case 10:
    case 11:
    default:
      throw new IllegalStateException("Invalid state: " + stateToString(this.state));
    case 6:
    case 8:
    case 9:
    case 12:
    }
    return getLimitedContentStream();
  }

  protected LineReaderInputStream getDataStream()
  {
    return this.dataStream;
  }

  protected int getLineNumber()
  {
    if (this.lineSource == null)
      return -1;
    return this.lineSource.getLineNumber();
  }

  public int getRecursionMode()
  {
    return this.recursionMode;
  }

  public void setRecursionMode(int paramInt)
  {
    this.recursionMode = paramInt;
  }

  public void skipHeader(String paramString)
  {
    if (this.state != 0)
      throw new IllegalStateException("Invalid state: " + stateToString(this.state));
    this.skipHeader = true;
    ByteSequence localByteSequence = ContentUtil.encode("Content-Type: " + paramString);
    this.body.addField(new RawField(localByteSequence, 12));
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.MimeEntity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */