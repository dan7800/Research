package org.apache.james.mime4j.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStream;;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.io.BufferedLineReaderInputStream;
import org.apache.james.mime4j.io.LineNumberInputStream;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.MimeUtil;

public class MimeTokenStream
  implements EntityStates, RecursionMode
{
  private final MimeEntityConfig config;
  private EntityStateMachine currentStateMachine;
  private final LinkedList<EntityStateMachine> entities = new LinkedList();
  private BufferedLineReaderInputStream inbuffer;
  private int recursionMode = 0;
  private int state = -1;

  public MimeTokenStream()
  {
    this(new MimeEntityConfig());
  }

  protected MimeTokenStream(MimeEntityConfig paramMimeEntityConfig)
  {
    this.config = paramMimeEntityConfig;
  }

  public static final MimeTokenStream createMaximalDescriptorStream()
  {
    MimeEntityConfig localMimeEntityConfig = new MimeEntityConfig();
    localMimeEntityConfig.setMaximalBodyDescriptor(true);
    return new MimeTokenStream(localMimeEntityConfig);
  }

  public static final MimeTokenStream createStrictValidationStream()
  {
    MimeEntityConfig localMimeEntityConfig = new MimeEntityConfig();
    localMimeEntityConfig.setStrictParsing(true);
    return new MimeTokenStream(localMimeEntityConfig);
  }

  private void doParse(InputStream paramInputStream, String paramString)
  {
    this.entities.clear();
    boolean bool = this.config.isCountLineNumbers();
    Object localObject = null;
    if (bool)
    {
      LineNumberInputStream localLineNumberInputStream = new LineNumberInputStream(paramInputStream);
      localObject = localLineNumberInputStream;
      paramInputStream = localLineNumberInputStream;
    }
    this.inbuffer = new BufferedLineReaderInputStream(paramInputStream, 4096, this.config.getMaxLineLen());
    switch (this.recursionMode)
    {
    default:
    case 2:
    case 0:
    case 1:
    case 3:
    }
    while (true)
    {
      this.entities.add(this.currentStateMachine);
      this.state = this.currentStateMachine.getState();
      return;
      this.currentStateMachine = new RawEntity(this.inbuffer);
      continue;
      MimeEntity localMimeEntity = new MimeEntity(localObject, this.inbuffer, null, 0, 1, this.config);
      localMimeEntity.setRecursionMode(this.recursionMode);
      if (paramString != null)
        localMimeEntity.skipHeader(paramString);
      this.currentStateMachine = localMimeEntity;
    }
  }

  public static final String stateToString(int paramInt)
  {
    return AbstractEntity.stateToString(paramInt);
  }

  public BodyDescriptor getBodyDescriptor()
  {
    return this.currentStateMachine.getBodyDescriptor();
  }

  public InputStream getDecodedInputStream()
  {
    String str = getBodyDescriptor().getTransferEncoding();
    Object localObject = this.currentStateMachine.getContentStream();
    if (MimeUtil.isBase64Encoding(str))
      localObject = new Base64InputStream((InputStream)localObject);
    while (!MimeUtil.isQuotedPrintableEncoded(str))
      return localObject;
    return new QuotedPrintableInputStream((InputStream)localObject);
  }

  public Field getField()
  {
    return this.currentStateMachine.getField();
  }

  public InputStream getInputStream()
  {
    return this.currentStateMachine.getContentStream();
  }

  public Reader getReader()
  {
    String str = getBodyDescriptor().getCharset();
    if ((str == null) || ("".equals(str)));
    for (Charset localCharset = CharsetUtil.US_ASCII; ; localCharset = Charset.forName(str))
      return new InputStreamReader(getDecodedInputStream(), localCharset);
  }

  public int getRecursionMode()
  {
    return this.recursionMode;
  }

  public int getState()
  {
    return this.state;
  }

  public boolean isRaw()
  {
    return this.recursionMode == 2;
  }

  public int next()
    throws IOException, MimeException
  {
    if ((this.state == -1) || (this.currentStateMachine == null))
    {
      throw new IllegalStateException("No more tokens are available.");
      this.entities.removeLast();
      if (!this.entities.isEmpty())
        break label109;
      this.currentStateMachine = null;
    }
    while (true)
    {
      if (this.currentStateMachine == null)
        break label139;
      EntityStateMachine localEntityStateMachine = this.currentStateMachine.advance();
      if (localEntityStateMachine != null)
      {
        this.entities.add(localEntityStateMachine);
        this.currentStateMachine = localEntityStateMachine;
      }
      this.state = this.currentStateMachine.getState();
      if (this.state == -1)
        break;
      return this.state;
      label109: this.currentStateMachine = ((EntityStateMachine)this.entities.getLast());
      this.currentStateMachine.setRecursionMode(this.recursionMode);
    }
    label139: this.state = -1;
    return this.state;
  }

  public void parse(InputStream paramInputStream)
  {
    doParse(paramInputStream, null);
  }

  public void parseHeadless(InputStream paramInputStream, String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("Content type may not be null");
    doParse(paramInputStream, paramString);
  }

  public void setRecursionMode(int paramInt)
  {
    this.recursionMode = paramInt;
    if (this.currentStateMachine != null)
      this.currentStateMachine.setRecursionMode(paramInt);
  }

  public void stop()
  {
    this.inbuffer.truncate();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.MimeTokenStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */