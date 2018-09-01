package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStream;;
import java.util.Stack;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.field.ParsedField;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.util.ByteArrayBuffer;
import org.apache.james.mime4j.util.ByteSequence;

public class MessageBuilder
  implements ContentHandler
{
  private final BodyFactory bodyFactory;
  private final Entity entity;
  private Stack<Object> stack = new Stack();

  public MessageBuilder(Entity paramEntity)
  {
    this.entity = paramEntity;
    this.bodyFactory = new BodyFactory();
  }

  public MessageBuilder(Entity paramEntity, StorageProvider paramStorageProvider)
  {
    this.entity = paramEntity;
    this.bodyFactory = new BodyFactory(paramStorageProvider);
  }

  private void expect(Class<?> paramClass)
  {
    if (!paramClass.isInstance(this.stack.peek()))
      throw new IllegalStateException("Internal stack error: Expected '" + paramClass.getName() + "' found '" + this.stack.peek().getClass().getName() + "'");
  }

  private static ByteSequence loadStream(InputStream paramInputStream)
    throws IOException
  {
    ByteArrayBuffer localByteArrayBuffer = new ByteArrayBuffer(64);
    while (true)
    {
      int i = paramInputStream.read();
      if (i == -1)
        break;
      localByteArrayBuffer.append(i);
    }
    return localByteArrayBuffer;
  }

  public void body(BodyDescriptor paramBodyDescriptor, InputStream paramInputStream)
    throws MimeException, IOException
  {
    expect(Entity.class);
    String str = paramBodyDescriptor.getTransferEncoding();
    Object localObject1;
    if ("base64".equals(str))
    {
      localObject1 = new Base64InputStream(paramInputStream);
      if (!paramBodyDescriptor.getMimeType().startsWith("text/"))
        break label107;
    }
    label107: for (Object localObject2 = this.bodyFactory.textBody((InputStream)localObject1, paramBodyDescriptor.getCharset()); ; localObject2 = this.bodyFactory.binaryBody((InputStream)localObject1))
    {
      ((Entity)this.stack.peek()).setBody((Body)localObject2);
      return;
      if ("quoted-printable".equals(str))
      {
        localObject1 = new QuotedPrintableInputStream(paramInputStream);
        break;
      }
      localObject1 = paramInputStream;
      break;
    }
  }

  public void endBodyPart()
    throws MimeException
  {
    expect(BodyPart.class);
    this.stack.pop();
  }

  public void endHeader()
    throws MimeException
  {
    expect(Header.class);
    Header localHeader = (Header)this.stack.pop();
    expect(Entity.class);
    ((Entity)this.stack.peek()).setHeader(localHeader);
  }

  public void endMessage()
    throws MimeException
  {
    expect(Message.class);
    this.stack.pop();
  }

  public void endMultipart()
    throws MimeException
  {
    this.stack.pop();
  }

  public void epilogue(InputStream paramInputStream)
    throws MimeException, IOException
  {
    expect(Multipart.class);
    ByteSequence localByteSequence = loadStream(paramInputStream);
    ((Multipart)this.stack.peek()).setEpilogueRaw(localByteSequence);
  }

  public void field(Field paramField)
    throws MimeException
  {
    expect(Header.class);
    ParsedField localParsedField = AbstractField.parse(paramField.getRaw());
    ((Header)this.stack.peek()).addField(localParsedField);
  }

  public void preamble(InputStream paramInputStream)
    throws MimeException, IOException
  {
    expect(Multipart.class);
    ByteSequence localByteSequence = loadStream(paramInputStream);
    ((Multipart)this.stack.peek()).setPreambleRaw(localByteSequence);
  }

  public void raw(InputStream paramInputStream)
    throws MimeException, IOException
  {
    throw new UnsupportedOperationException("Not supported");
  }

  public void startBodyPart()
    throws MimeException
  {
    expect(Multipart.class);
    BodyPart localBodyPart = new BodyPart();
    ((Multipart)this.stack.peek()).addBodyPart(localBodyPart);
    this.stack.push(localBodyPart);
  }

  public void startHeader()
    throws MimeException
  {
    this.stack.push(new Header());
  }

  public void startMessage()
    throws MimeException
  {
    if (this.stack.isEmpty())
    {
      this.stack.push(this.entity);
      return;
    }
    expect(Entity.class);
    Message localMessage = new Message();
    ((Entity)this.stack.peek()).setBody(localMessage);
    this.stack.push(localMessage);
  }

  public void startMultipart(BodyDescriptor paramBodyDescriptor)
    throws MimeException
  {
    expect(Entity.class);
    Entity localEntity = (Entity)this.stack.peek();
    Multipart localMultipart = new Multipart(paramBodyDescriptor.getSubType());
    localEntity.setBody(localMultipart);
    this.stack.push(localMultipart);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.MessageBuilder
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */