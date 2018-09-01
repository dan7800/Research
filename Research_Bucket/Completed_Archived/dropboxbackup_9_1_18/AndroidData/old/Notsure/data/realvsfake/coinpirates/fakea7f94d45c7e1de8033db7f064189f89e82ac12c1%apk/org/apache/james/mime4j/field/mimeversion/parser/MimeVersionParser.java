package org.apache.james.mime4j.field.mimeversion.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class MimeVersionParser
  implements MimeVersionParserConstants
{
  public static final int INITIAL_VERSION_VALUE = -1;
  private static int[] jj_la1_0;
  private Vector<int[]> jj_expentries = new Vector();
  private int[] jj_expentry;
  private int jj_gen;
  SimpleCharStream jj_input_stream;
  private int jj_kind = -1;
  private final int[] jj_la1 = new int[1];
  public Token jj_nt;
  private int jj_ntk;
  private int major = -1;
  private int minor = -1;
  public Token token;
  public MimeVersionParserTokenManager token_source;

  static
  {
    jj_la1_0();
  }

  public MimeVersionParser(InputStream paramInputStream)
  {
    this(paramInputStream, null);
  }

  public MimeVersionParser(InputStream paramInputStream, String paramString)
  {
    try
    {
      this.jj_input_stream = new SimpleCharStream(paramInputStream, paramString, 1, 1);
      this.token_source = new MimeVersionParserTokenManager(this.jj_input_stream);
      this.token = new Token();
      this.jj_ntk = -1;
      this.jj_gen = 0;
      for (int i = 0; i < 1; i++)
        this.jj_la1[i] = -1;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new RuntimeException(localUnsupportedEncodingException);
    }
  }

  public MimeVersionParser(Reader paramReader)
  {
    this.jj_input_stream = new SimpleCharStream(paramReader, 1, 1);
    this.token_source = new MimeVersionParserTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 1; i++)
      this.jj_la1[i] = -1;
  }

  public MimeVersionParser(MimeVersionParserTokenManager paramMimeVersionParserTokenManager)
  {
    this.token_source = paramMimeVersionParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 1; i++)
      this.jj_la1[i] = -1;
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
    jj_la1_0 = new int[] { 2 };
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
      for (int i = 0; i < 1; i++)
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
    for (int i = 0; i < 1; i++)
      this.jj_la1[i] = -1;
  }

  public void ReInit(MimeVersionParserTokenManager paramMimeVersionParserTokenManager)
  {
    this.token_source = paramMimeVersionParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 1; i++)
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
    boolean[] arrayOfBoolean = new boolean[21];
    for (int i = 0; i < 21; i++)
      arrayOfBoolean[i] = false;
    if (this.jj_kind >= 0)
    {
      arrayOfBoolean[this.jj_kind] = true;
      this.jj_kind = -1;
    }
    for (int j = 0; j < 1; j++)
      if (this.jj_la1[j] == this.jj_gen)
        for (int n = 0; n < 32; n++)
          if ((jj_la1_0[j] & 1 << n) != 0)
            arrayOfBoolean[n] = true;
    for (int k = 0; k < 21; k++)
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

  public int getMajorVersion()
  {
    return this.major;
  }

  public int getMinorVersion()
  {
    return this.minor;
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

  public final void parse()
    throws ParseException
  {
    Token localToken1 = jj_consume_token(17);
    jj_consume_token(18);
    Token localToken2 = jj_consume_token(17);
    try
    {
      this.major = Integer.parseInt(localToken1.image);
      this.minor = Integer.parseInt(localToken2.image);
      return;
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new ParseException(localNumberFormatException.getMessage());
    }
  }

  public final void parseAll()
    throws ParseException
  {
    parse();
    jj_consume_token(0);
  }

  public final void parseLine()
    throws ParseException
  {
    parse();
    int i;
    if (this.jj_ntk == -1)
    {
      i = jj_ntk();
      switch (i)
      {
      default:
        this.jj_la1[0] = this.jj_gen;
      case 1:
      }
    }
    while (true)
    {
      jj_consume_token(2);
      return;
      i = this.jj_ntk;
      break;
      jj_consume_token(1);
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.mimeversion.parser.MimeVersionParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */