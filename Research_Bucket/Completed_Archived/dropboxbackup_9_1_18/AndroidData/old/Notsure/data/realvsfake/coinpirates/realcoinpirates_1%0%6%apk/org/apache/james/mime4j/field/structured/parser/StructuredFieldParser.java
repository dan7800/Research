package org.apache.james.mime4j.field.structured.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class StructuredFieldParser
  implements StructuredFieldParserConstants
{
  private static int[] jj_la1_0;
  private Vector<int[]> jj_expentries = new Vector();
  private int[] jj_expentry;
  private int jj_gen;
  SimpleCharStream jj_input_stream;
  private int jj_kind = -1;
  private final int[] jj_la1 = new int[2];
  public Token jj_nt;
  private int jj_ntk;
  private boolean preserveFolding = false;
  public Token token;
  public StructuredFieldParserTokenManager token_source;

  static
  {
    jj_la1_0();
  }

  public StructuredFieldParser(InputStream paramInputStream)
  {
    this(paramInputStream, null);
  }

  public StructuredFieldParser(InputStream paramInputStream, String paramString)
  {
    try
    {
      this.jj_input_stream = new SimpleCharStream(paramInputStream, paramString, 1, 1);
      this.token_source = new StructuredFieldParserTokenManager(this.jj_input_stream);
      this.token = new Token();
      this.jj_ntk = -1;
      this.jj_gen = 0;
      for (int i = 0; i < 2; i++)
        this.jj_la1[i] = -1;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new RuntimeException(localUnsupportedEncodingException);
    }
  }

  public StructuredFieldParser(Reader paramReader)
  {
    this.jj_input_stream = new SimpleCharStream(paramReader, 1, 1);
    this.token_source = new StructuredFieldParserTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 2; i++)
      this.jj_la1[i] = -1;
  }

  public StructuredFieldParser(StructuredFieldParserTokenManager paramStructuredFieldParserTokenManager)
  {
    this.token_source = paramStructuredFieldParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 2; i++)
      this.jj_la1[i] = -1;
  }

  private final String doParse()
    throws ParseException
  {
    StringBuffer localStringBuffer = new StringBuffer(50);
    int i = 0;
    int j = 1;
    while (true)
    {
      if (this.jj_ntk == -1);
      for (int k = jj_ntk(); ; k = this.jj_ntk)
        switch (k)
        {
        default:
          this.jj_la1[0] = this.jj_gen;
          return localStringBuffer.toString();
        case 11:
        case 12:
        case 13:
        case 14:
        case 15:
        }
      if (this.jj_ntk == -1);
      for (int m = jj_ntk(); ; m = this.jj_ntk)
        switch (m)
        {
        default:
          this.jj_la1[1] = this.jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        case 15:
        case 11:
        case 13:
        case 12:
        case 14:
        }
      Token localToken2 = jj_consume_token(15);
      if (j != 0)
        j = 0;
      while (true)
      {
        localStringBuffer.append(localToken2.image);
        break;
        if (i != 0)
        {
          localStringBuffer.append(" ");
          i = 0;
        }
      }
      localStringBuffer.append(jj_consume_token(11).image);
      continue;
      Token localToken1 = jj_consume_token(13);
      if (j != 0)
        j = 0;
      while (true)
      {
        localStringBuffer.append(localToken1.image);
        break;
        if (i != 0)
        {
          localStringBuffer.append(" ");
          i = 0;
        }
      }
      jj_consume_token(12);
      if (this.preserveFolding)
      {
        localStringBuffer.append("\r\n");
        continue;
        jj_consume_token(14);
        i = 1;
      }
    }
  }

  private final Token jj_consume_token(int paramInt)
    throws ParseException
  {
    Token localToken1 = this.token;
    if (localToken1.next != null);
    Token localToken3;
    for (this.token = this.token.next; ; this.token = localToken3)
    {
      this.jj_ntk = -1;
      if (this.token.kind != paramInt)
        break;
      this.jj_gen = (1 + this.jj_gen);
      return this.token;
      Token localToken2 = this.token;
      localToken3 = this.token_source.getNextToken();
      localToken2.next = localToken3;
    }
    this.token = localToken1;
    this.jj_kind = paramInt;
    throw generateParseException();
  }

  private static void jj_la1_0()
  {
    jj_la1_0 = new int[] { 63488, 63488 };
  }

  private final int jj_ntk()
  {
    Token localToken1 = this.token.next;
    this.jj_nt = localToken1;
    if (localToken1 == null)
    {
      Token localToken2 = this.token;
      Token localToken3 = this.token_source.getNextToken();
      localToken2.next = localToken3;
      int j = localToken3.kind;
      this.jj_ntk = j;
      return j;
    }
    int i = this.jj_nt.kind;
    this.jj_ntk = i;
    return i;
  }

  public void ReInit(InputStream paramInputStream)
  {
    ReInit(paramInputStream, null);
  }

  public void ReInit(InputStream paramInputStream, String paramString)
  {
    try
    {
      this.jj_input_stream.ReInit(paramInputStream, paramString, 1, 1);
      this.token_source.ReInit(this.jj_input_stream);
      this.token = new Token();
      this.jj_ntk = -1;
      this.jj_gen = 0;
      for (int i = 0; i < 2; i++)
        this.jj_la1[i] = -1;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new RuntimeException(localUnsupportedEncodingException);
    }
  }

  public void ReInit(Reader paramReader)
  {
    this.jj_input_stream.ReInit(paramReader, 1, 1);
    this.token_source.ReInit(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 2; i++)
      this.jj_la1[i] = -1;
  }

  public void ReInit(StructuredFieldParserTokenManager paramStructuredFieldParserTokenManager)
  {
    this.token_source = paramStructuredFieldParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 2; i++)
      this.jj_la1[i] = -1;
  }

  public final void disable_tracing()
  {
  }

  public final void enable_tracing()
  {
  }

  public ParseException generateParseException()
  {
    this.jj_expentries.removeAllElements();
    boolean[] arrayOfBoolean = new boolean[18];
    for (int i = 0; i < 18; i++)
      arrayOfBoolean[i] = false;
    if (this.jj_kind >= 0)
    {
      arrayOfBoolean[this.jj_kind] = true;
      this.jj_kind = -1;
    }
    for (int j = 0; j < 2; j++)
      if (this.jj_la1[j] == this.jj_gen)
        for (int n = 0; n < 32; n++)
          if ((jj_la1_0[j] & 1 << n) != 0)
            arrayOfBoolean[n] = true;
    for (int k = 0; k < 18; k++)
      if (arrayOfBoolean[k] != 0)
      {
        this.jj_expentry = new int[1];
        this.jj_expentry[0] = k;
        this.jj_expentries.addElement(this.jj_expentry);
      }
    int[][] arrayOfInt = new int[this.jj_expentries.size()][];
    for (int m = 0; m < this.jj_expentries.size(); m++)
      arrayOfInt[m] = ((int[])(int[])this.jj_expentries.elementAt(m));
    return new ParseException(this.token, arrayOfInt, tokenImage);
  }

  public final Token getNextToken()
  {
    if (this.token.next != null);
    Token localToken2;
    for (this.token = this.token.next; ; this.token = localToken2)
    {
      this.jj_ntk = -1;
      this.jj_gen = (1 + this.jj_gen);
      return this.token;
      Token localToken1 = this.token;
      localToken2 = this.token_source.getNextToken();
      localToken1.next = localToken2;
    }
  }

  public final Token getToken(int paramInt)
  {
    Token localToken1 = this.token;
    int i = 0;
    Object localObject = localToken1;
    if (i < paramInt)
    {
      Token localToken2;
      if (((Token)localObject).next != null)
        localToken2 = ((Token)localObject).next;
      while (true)
      {
        i++;
        localObject = localToken2;
        break;
        localToken2 = this.token_source.getNextToken();
        ((Token)localObject).next = localToken2;
      }
    }
    return localObject;
  }

  public boolean isFoldingPreserved()
  {
    return this.preserveFolding;
  }

  public String parse()
    throws ParseException
  {
    try
    {
      String str = doParse();
      return str;
    }
    catch (TokenMgrError localTokenMgrError)
    {
      throw new ParseException(localTokenMgrError);
    }
  }

  public void setFoldingPreserved(boolean paramBoolean)
  {
    this.preserveFolding = paramBoolean;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.structured.parser.StructuredFieldParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */