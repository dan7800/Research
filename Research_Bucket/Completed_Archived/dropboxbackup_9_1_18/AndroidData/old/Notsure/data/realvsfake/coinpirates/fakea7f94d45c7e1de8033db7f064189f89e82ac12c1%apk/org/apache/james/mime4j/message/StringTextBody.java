package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import org.apache.james.mime4j.util.CharsetUtil;

class StringTextBody extends TextBody
{
  private final Charset charset;
  private final String text;

  public StringTextBody(String paramString, Charset paramCharset)
  {
    this.text = paramString;
    this.charset = paramCharset;
  }

  public StringTextBody copy()
  {
    return new StringTextBody(this.text, this.charset);
  }

  public String getMimeCharset()
  {
    return CharsetUtil.toMimeCharset(this.charset.name());
  }

  public Reader getReader()
    throws IOException
  {
    return new StringReader(this.text);
  }

  public void writeTo(OutputStream paramOutputStream)
    throws IOException
  {
    if (paramOutputStream == null)
      throw new IllegalArgumentException();
    StringReader localStringReader = new StringReader(this.text);
    OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(paramOutputStream, this.charset);
    char[] arrayOfChar = new char[1024];
    while (true)
    {
      int i = localStringReader.read(arrayOfChar);
      if (i == -1)
      {
        localStringReader.close();
        localOutputStreamWriter.flush();
        return;
      }
      localOutputStreamWriter.write(arrayOfChar, 0, i);
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.StringTextBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */