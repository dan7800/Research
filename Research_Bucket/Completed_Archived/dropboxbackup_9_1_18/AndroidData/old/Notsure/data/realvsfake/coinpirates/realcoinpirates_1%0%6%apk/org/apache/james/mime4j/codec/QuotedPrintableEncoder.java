package org.apache.james.mime4j.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class QuotedPrintableEncoder
{
  private static final byte CR = 13;
  private static final byte EQUALS = 61;
  private static final byte[] HEX_DIGITS = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };
  private static final byte LF = 10;
  private static final byte QUOTED_PRINTABLE_LAST_PLAIN = 126;
  private static final int QUOTED_PRINTABLE_MAX_LINE_LENGTH = 76;
  private static final int QUOTED_PRINTABLE_OCTETS_PER_ESCAPE = 3;
  private static final byte SPACE = 32;
  private static final byte TAB = 9;
  private final boolean binary;
  private final byte[] inBuffer;
  private int nextSoftBreak;
  private OutputStream out;
  private final byte[] outBuffer;
  private int outputIndex;
  private boolean pendingCR;
  private boolean pendingSpace;
  private boolean pendingTab;

  public QuotedPrintableEncoder(int paramInt, boolean paramBoolean)
  {
    this.inBuffer = new byte[paramInt];
    this.outBuffer = new byte[paramInt * 3];
    this.outputIndex = 0;
    this.nextSoftBreak = 77;
    this.out = null;
    this.binary = paramBoolean;
    this.pendingSpace = false;
    this.pendingTab = false;
    this.pendingCR = false;
  }

  private void clearPending()
    throws IOException
  {
    this.pendingSpace = false;
    this.pendingTab = false;
    this.pendingCR = false;
  }

  private void encode(byte paramByte)
    throws IOException
  {
    if (paramByte == 10)
    {
      if (this.binary)
      {
        writePending();
        escape(paramByte);
        return;
      }
      if (this.pendingCR)
      {
        if (this.pendingSpace)
          escape((byte)32);
        while (true)
        {
          lineBreak();
          clearPending();
          return;
          if (this.pendingTab)
            escape((byte)9);
        }
      }
      writePending();
      plain(paramByte);
      return;
    }
    if (paramByte == 13)
    {
      if (this.binary)
      {
        escape(paramByte);
        return;
      }
      this.pendingCR = true;
      return;
    }
    writePending();
    if (paramByte == 32)
    {
      if (this.binary)
      {
        escape(paramByte);
        return;
      }
      this.pendingSpace = true;
      return;
    }
    if (paramByte == 9)
    {
      if (this.binary)
      {
        escape(paramByte);
        return;
      }
      this.pendingTab = true;
      return;
    }
    if (paramByte < 32)
    {
      escape(paramByte);
      return;
    }
    if (paramByte > 126)
    {
      escape(paramByte);
      return;
    }
    if (paramByte == 61)
    {
      escape(paramByte);
      return;
    }
    plain(paramByte);
  }

  private void escape(byte paramByte)
    throws IOException
  {
    int i = this.nextSoftBreak - 1;
    this.nextSoftBreak = i;
    if (i <= 3)
      softBreak();
    int j = paramByte & 0xFF;
    write((byte)61);
    this.nextSoftBreak -= 1;
    write(HEX_DIGITS[(j >> 4)]);
    this.nextSoftBreak -= 1;
    write(HEX_DIGITS[(j % 16)]);
  }

  private void lineBreak()
    throws IOException
  {
    write((byte)13);
    write((byte)10);
    this.nextSoftBreak = 76;
  }

  private void plain(byte paramByte)
    throws IOException
  {
    int i = this.nextSoftBreak - 1;
    this.nextSoftBreak = i;
    if (i <= 1)
      softBreak();
    write(paramByte);
  }

  private void softBreak()
    throws IOException
  {
    write((byte)61);
    lineBreak();
  }

  private void write(byte paramByte)
    throws IOException
  {
    byte[] arrayOfByte = this.outBuffer;
    int i = this.outputIndex;
    this.outputIndex = (i + 1);
    arrayOfByte[i] = paramByte;
    if (this.outputIndex >= this.outBuffer.length)
      flushOutput();
  }

  private void writePending()
    throws IOException
  {
    if (this.pendingSpace)
      plain((byte)32);
    while (true)
    {
      clearPending();
      return;
      if (this.pendingTab)
        plain((byte)9);
      else if (this.pendingCR)
        plain((byte)13);
    }
  }

  void completeEncoding()
    throws IOException
  {
    writePending();
    flushOutput();
  }

  public void encode(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    initEncoding(paramOutputStream);
    while (true)
    {
      int i = paramInputStream.read(this.inBuffer);
      if (i <= -1)
        break;
      encodeChunk(this.inBuffer, 0, i);
    }
    completeEncoding();
  }

  void encodeChunk(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    for (int i = paramInt1; i < paramInt2 + paramInt1; i++)
      encode(paramArrayOfByte[i]);
  }

  void flushOutput()
    throws IOException
  {
    if (this.outputIndex < this.outBuffer.length)
      this.out.write(this.outBuffer, 0, this.outputIndex);
    while (true)
    {
      this.outputIndex = 0;
      return;
      this.out.write(this.outBuffer);
    }
  }

  void initEncoding(OutputStream paramOutputStream)
  {
    this.out = paramOutputStream;
    this.pendingSpace = false;
    this.pendingTab = false;
    this.pendingCR = false;
    this.nextSoftBreak = 77;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.codec.QuotedPrintableEncoder
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */