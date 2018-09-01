package org.apache.http.entity.mime.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.annotation.NotThreadSafe;

@NotThreadSafe
public class FileBody extends AbstractContentBody
{
  private final File file;

  public FileBody(File paramFile)
  {
    this(paramFile, "application/octet-stream");
  }

  public FileBody(File paramFile, String paramString)
  {
    super(paramString);
    if (paramFile == null)
      throw new IllegalArgumentException("File may not be null");
    this.file = paramFile;
  }

  public String getCharset()
  {
    return null;
  }

  public long getContentLength()
  {
    return this.file.length();
  }

  public File getFile()
  {
    return this.file;
  }

  public String getFilename()
  {
    return this.file.getName();
  }

  public InputStream getInputStream()
    throws IOException
  {
    return new FileInputStream(this.file);
  }

  public String getTransferEncoding()
  {
    return "binary";
  }

  public void writeTo(OutputStream paramOutputStream)
    throws IOException
  {
    if (paramOutputStream == null)
      throw new IllegalArgumentException("Output stream may not be null");
    FileInputStream localFileInputStream = new FileInputStream(this.file);
    try
    {
      byte[] arrayOfByte = new byte[4096];
      while (true)
      {
        int i = localFileInputStream.read(arrayOfByte);
        if (i == -1)
          break;
        paramOutputStream.write(arrayOfByte, 0, i);
      }
    }
    finally
    {
      localFileInputStream.close();
    }
    paramOutputStream.flush();
    localFileInputStream.close();
  }

  @Deprecated
  public void writeTo(OutputStream paramOutputStream, int paramInt)
    throws IOException
  {
    writeTo(paramOutputStream);
  }
}

/* Location:
 * Qualified Name:     org.apache.http.entity.mime.content.FileBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */