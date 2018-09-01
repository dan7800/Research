package org.apache.james.mime4j.field.contentdisposition.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class SimpleCharStream
{
  public static final boolean staticFlag;
  int available;
  protected int[] bufcolumn;
  protected char[] buffer;
  protected int[] bufline;
  public int bufpos = -1;
  int bufsize;
  protected int column = 0;
  protected int inBuf = 0;
  protected Reader inputStream;
  protected int line = 1;
  protected int maxNextCharInd = 0;
  protected boolean prevCharIsCR = false;
  protected boolean prevCharIsLF = false;
  protected int tabSize = 8;
  int tokenBegin;

  public SimpleCharStream(InputStream paramInputStream)
  {
    this(paramInputStream, 1, 1, 4096);
  }

  public SimpleCharStream(InputStream paramInputStream, int paramInt1, int paramInt2)
  {
    this(paramInputStream, paramInt1, paramInt2, 4096);
  }

  public SimpleCharStream(InputStream paramInputStream, int paramInt1, int paramInt2, int paramInt3)
  {
    this(new InputStreamReader(paramInputStream), paramInt1, paramInt2, paramInt3);
  }

  public SimpleCharStream(InputStream paramInputStream, String paramString)
    throws UnsupportedEncodingException
  {
    this(paramInputStream, paramString, 1, 1, 4096);
  }

  public SimpleCharStream(InputStream paramInputStream, String paramString, int paramInt1, int paramInt2)
    throws UnsupportedEncodingException
  {
    this(paramInputStream, paramString, paramInt1, paramInt2, 4096);
  }

  public SimpleCharStream(InputStream paramInputStream, String paramString, int paramInt1, int paramInt2, int paramInt3)
    throws UnsupportedEncodingException
  {
  }

  public SimpleCharStream(Reader paramReader)
  {
    this(paramReader, 1, 1, 4096);
  }

  public SimpleCharStream(Reader paramReader, int paramInt1, int paramInt2)
  {
    this(paramReader, paramInt1, paramInt2, 4096);
  }

  public SimpleCharStream(Reader paramReader, int paramInt1, int paramInt2, int paramInt3)
  {
    this.inputStream = paramReader;
    this.line = paramInt1;
    this.column = (paramInt2 - 1);
    this.bufsize = paramInt3;
    this.available = paramInt3;
    this.buffer = new char[paramInt3];
    this.bufline = new int[paramInt3];
    this.bufcolumn = new int[paramInt3];
  }

  public char BeginToken()
    throws IOException
  {
    this.tokenBegin = -1;
    char c = readChar();
    this.tokenBegin = this.bufpos;
    return c;
  }

  public void Done()
  {
    this.buffer = null;
    this.bufline = null;
    this.bufcolumn = null;
  }

  protected void ExpandBuff(boolean paramBoolean)
  {
    arrayOfChar = new char[2048 + this.bufsize];
    arrayOfInt1 = new int[2048 + this.bufsize];
    arrayOfInt2 = new int[2048 + this.bufsize];
    if (paramBoolean)
      try
      {
        System.arraycopy(this.buffer, this.tokenBegin, arrayOfChar, 0, this.bufsize - this.tokenBegin);
        System.arraycopy(this.buffer, 0, arrayOfChar, this.bufsize - this.tokenBegin, this.bufpos);
        this.buffer = arrayOfChar;
        System.arraycopy(this.bufline, this.tokenBegin, arrayOfInt1, 0, this.bufsize - this.tokenBegin);
        System.arraycopy(this.bufline, 0, arrayOfInt1, this.bufsize - this.tokenBegin, this.bufpos);
        this.bufline = arrayOfInt1;
        System.arraycopy(this.bufcolumn, this.tokenBegin, arrayOfInt2, 0, this.bufsize - this.tokenBegin);
        System.arraycopy(this.bufcolumn, 0, arrayOfInt2, this.bufsize - this.tokenBegin, this.bufpos);
        this.bufcolumn = arrayOfInt2;
        int j = this.bufpos + (this.bufsize - this.tokenBegin);
        this.bufpos = j;
        int i;
        for (this.maxNextCharInd = j; ; this.maxNextCharInd = i)
        {
          this.bufsize = (2048 + this.bufsize);
          this.available = this.bufsize;
          this.tokenBegin = 0;
          return;
          System.arraycopy(this.buffer, this.tokenBegin, arrayOfChar, 0, this.bufsize - this.tokenBegin);
          this.buffer = arrayOfChar;
          System.arraycopy(this.bufline, this.tokenBegin, arrayOfInt1, 0, this.bufsize - this.tokenBegin);
          this.bufline = arrayOfInt1;
          System.arraycopy(this.bufcolumn, this.tokenBegin, arrayOfInt2, 0, this.bufsize - this.tokenBegin);
          this.bufcolumn = arrayOfInt2;
          i = this.bufpos - this.tokenBegin;
          this.bufpos = i;
        }
      }
      catch (Throwable localThrowable)
      {
        throw new Error(localThrowable.getMessage());
      }
  }

  protected void FillBuff()
    throws IOException
  {
    if (this.maxNextCharInd == this.available)
    {
      if (this.available != this.bufsize)
        break label157;
      if (this.tokenBegin <= 2048)
        break label129;
      this.maxNextCharInd = 0;
      this.bufpos = 0;
      this.available = this.tokenBegin;
    }
    int i;
    while (true)
    {
      try
      {
        i = this.inputStream.read(this.buffer, this.maxNextCharInd, this.available - this.maxNextCharInd);
        if (i != -1)
          break;
        this.inputStream.close();
        throw new IOException();
      }
      catch (IOException localIOException)
      {
        this.bufpos -= 1;
        backup(0);
        if (this.tokenBegin == -1)
          this.tokenBegin = this.bufpos;
        throw localIOException;
      }
      label129: if (this.tokenBegin < 0)
      {
        this.maxNextCharInd = 0;
        this.bufpos = 0;
      }
      else
      {
        ExpandBuff(false);
        continue;
        label157: if (this.available > this.tokenBegin)
          this.available = this.bufsize;
        else if (this.tokenBegin - this.available < 2048)
          ExpandBuff(true);
        else
          this.available = this.tokenBegin;
      }
    }
    this.maxNextCharInd = (i + this.maxNextCharInd);
  }

  public String GetImage()
  {
    if (this.bufpos >= this.tokenBegin)
      return new String(this.buffer, this.tokenBegin, 1 + (this.bufpos - this.tokenBegin));
    return new String(this.buffer, this.tokenBegin, this.bufsize - this.tokenBegin) + new String(this.buffer, 0, 1 + this.bufpos);
  }

  public char[] GetSuffix(int paramInt)
  {
    char[] arrayOfChar = new char[paramInt];
    if (1 + this.bufpos >= paramInt)
    {
      System.arraycopy(this.buffer, 1 + (this.bufpos - paramInt), arrayOfChar, 0, paramInt);
      return arrayOfChar;
    }
    System.arraycopy(this.buffer, this.bufsize - (paramInt - this.bufpos - 1), arrayOfChar, 0, paramInt - this.bufpos - 1);
    System.arraycopy(this.buffer, 0, arrayOfChar, paramInt - this.bufpos - 1, 1 + this.bufpos);
    return arrayOfChar;
  }

  public void ReInit(InputStream paramInputStream)
  {
    ReInit(paramInputStream, 1, 1, 4096);
  }

  public void ReInit(InputStream paramInputStream, int paramInt1, int paramInt2)
  {
    ReInit(paramInputStream, paramInt1, paramInt2, 4096);
  }

  public void ReInit(InputStream paramInputStream, int paramInt1, int paramInt2, int paramInt3)
  {
    ReInit(new InputStreamReader(paramInputStream), paramInt1, paramInt2, paramInt3);
  }

  public void ReInit(InputStream paramInputStream, String paramString)
    throws UnsupportedEncodingException
  {
    ReInit(paramInputStream, paramString, 1, 1, 4096);
  }

  public void ReInit(InputStream paramInputStream, String paramString, int paramInt1, int paramInt2)
    throws UnsupportedEncodingException
  {
    ReInit(paramInputStream, paramString, paramInt1, paramInt2, 4096);
  }

  public void ReInit(InputStream paramInputStream, String paramString, int paramInt1, int paramInt2, int paramInt3)
    throws UnsupportedEncodingException
  {
    if (paramString == null);
    for (InputStreamReader localInputStreamReader = new InputStreamReader(paramInputStream); ; localInputStreamReader = new InputStreamReader(paramInputStream, paramString))
    {
      ReInit(localInputStreamReader, paramInt1, paramInt2, paramInt3);
      return;
    }
  }

  public void ReInit(Reader paramReader)
  {
    ReInit(paramReader, 1, 1, 4096);
  }

  public void ReInit(Reader paramReader, int paramInt1, int paramInt2)
  {
    ReInit(paramReader, paramInt1, paramInt2, 4096);
  }

  public void ReInit(Reader paramReader, int paramInt1, int paramInt2, int paramInt3)
  {
    this.inputStream = paramReader;
    this.line = paramInt1;
    this.column = (paramInt2 - 1);
    if ((this.buffer == null) || (paramInt3 != this.buffer.length))
    {
      this.bufsize = paramInt3;
      this.available = paramInt3;
      this.buffer = new char[paramInt3];
      this.bufline = new int[paramInt3];
      this.bufcolumn = new int[paramInt3];
    }
    this.prevCharIsCR = false;
    this.prevCharIsLF = false;
    this.maxNextCharInd = 0;
    this.inBuf = 0;
    this.tokenBegin = 0;
    this.bufpos = -1;
  }

  protected void UpdateLineColumn(char paramChar)
  {
    this.column = (1 + this.column);
    if (this.prevCharIsLF)
    {
      this.prevCharIsLF = false;
      int j = this.line;
      this.column = 1;
      this.line = (j + 1);
      switch (paramChar)
      {
      case '\013':
      case '\f':
      default:
      case '\r':
      case '\n':
      case '\t':
      }
    }
    while (true)
    {
      this.bufline[this.bufpos] = this.line;
      this.bufcolumn[this.bufpos] = this.column;
      return;
      if (!this.prevCharIsCR)
        break;
      this.prevCharIsCR = false;
      if (paramChar == '\n')
      {
        this.prevCharIsLF = true;
        break;
      }
      int i = this.line;
      this.column = 1;
      this.line = (i + 1);
      break;
      this.prevCharIsCR = true;
      continue;
      this.prevCharIsLF = true;
      continue;
      this.column -= 1;
      this.column += this.tabSize - this.column % this.tabSize;
    }
  }

  public void adjustBeginLineColumn(int paramInt1, int paramInt2)
  {
    int i = this.tokenBegin;
    if (this.bufpos >= this.tokenBegin);
    int k;
    int m;
    int n;
    for (int j = 1 + (this.bufpos - this.tokenBegin + this.inBuf); ; j = 1 + (this.bufsize - this.tokenBegin + this.bufpos) + this.inBuf)
    {
      k = 0;
      m = 0;
      n = 0;
      while (k < j)
      {
        int[] arrayOfInt5 = this.bufline;
        m = i % this.bufsize;
        int i6 = arrayOfInt5[m];
        int[] arrayOfInt6 = this.bufline;
        i++;
        int i7 = i % this.bufsize;
        if (i6 != arrayOfInt6[i7])
          break;
        this.bufline[m] = paramInt1;
        int i8 = n + this.bufcolumn[i7] - this.bufcolumn[m];
        this.bufcolumn[m] = (paramInt2 + n);
        n = i8;
        k++;
      }
    }
    int i1;
    if (k < j)
    {
      int[] arrayOfInt1 = this.bufline;
      i1 = paramInt1 + 1;
      arrayOfInt1[m] = paramInt1;
      this.bufcolumn[m] = (paramInt2 + n);
      int i2 = k;
      while (true)
      {
        int i3 = i2 + 1;
        if (i2 >= j)
          break;
        int[] arrayOfInt2 = this.bufline;
        m = i % this.bufsize;
        int i4 = arrayOfInt2[m];
        int[] arrayOfInt3 = this.bufline;
        i++;
        if (i4 != arrayOfInt3[(i % this.bufsize)])
        {
          int[] arrayOfInt4 = this.bufline;
          int i5 = i1 + 1;
          arrayOfInt4[m] = i1;
          i2 = i3;
          i1 = i5;
        }
        else
        {
          this.bufline[m] = i1;
          i2 = i3;
        }
      }
    }
    this.line = this.bufline[m];
    this.column = this.bufcolumn[m];
  }

  public void backup(int paramInt)
  {
    this.inBuf = (paramInt + this.inBuf);
    int i = this.bufpos - paramInt;
    this.bufpos = i;
    if (i < 0)
      this.bufpos += this.bufsize;
  }

  public int getBeginColumn()
  {
    return this.bufcolumn[this.tokenBegin];
  }

  public int getBeginLine()
  {
    return this.bufline[this.tokenBegin];
  }

  public int getColumn()
  {
    return this.bufcolumn[this.bufpos];
  }

  public int getEndColumn()
  {
    return this.bufcolumn[this.bufpos];
  }

  public int getEndLine()
  {
    return this.bufline[this.bufpos];
  }

  public int getLine()
  {
    return this.bufline[this.bufpos];
  }

  protected int getTabSize(int paramInt)
  {
    return this.tabSize;
  }

  public char readChar()
    throws IOException
  {
    if (this.inBuf > 0)
    {
      this.inBuf -= 1;
      int j = 1 + this.bufpos;
      this.bufpos = j;
      if (j == this.bufsize)
        this.bufpos = 0;
      return this.buffer[this.bufpos];
    }
    int i = 1 + this.bufpos;
    this.bufpos = i;
    if (i >= this.maxNextCharInd)
      FillBuff();
    char c = this.buffer[this.bufpos];
    UpdateLineColumn(c);
    return c;
  }

  protected void setTabSize(int paramInt)
  {
    this.tabSize = paramInt;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.contentdisposition.parser.SimpleCharStream
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */