package org.apache.james.mime4j.field.datetime.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import org.apache.james.mime4j.field.datetime.DateTime;

public class DateTimeParser
  implements DateTimeParserConstants
{
  private static final boolean ignoreMilitaryZoneOffset = true;
  private static int[] jj_la1_0;
  private static int[] jj_la1_1;
  private Vector<int[]> jj_expentries = new Vector();
  private int[] jj_expentry;
  private int jj_gen;
  SimpleCharStream jj_input_stream;
  private int jj_kind = -1;
  private final int[] jj_la1 = new int[7];
  public Token jj_nt;
  private int jj_ntk;
  public Token token;
  public DateTimeParserTokenManager token_source;

  static
  {
    jj_la1_0();
    jj_la1_1();
  }

  public DateTimeParser(InputStream paramInputStream)
  {
    this(paramInputStream, null);
  }

  public DateTimeParser(InputStream paramInputStream, String paramString)
  {
    try
    {
      this.jj_input_stream = new SimpleCharStream(paramInputStream, paramString, 1, 1);
      this.token_source = new DateTimeParserTokenManager(this.jj_input_stream);
      this.token = new Token();
      this.jj_ntk = -1;
      this.jj_gen = 0;
      for (int i = 0; i < 7; i++)
        this.jj_la1[i] = -1;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new RuntimeException(localUnsupportedEncodingException);
    }
  }

  public DateTimeParser(Reader paramReader)
  {
    this.jj_input_stream = new SimpleCharStream(paramReader, 1, 1);
    this.token_source = new DateTimeParserTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 7; i++)
      this.jj_la1[i] = -1;
  }

  public DateTimeParser(DateTimeParserTokenManager paramDateTimeParserTokenManager)
  {
    this.token_source = paramDateTimeParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 7; i++)
      this.jj_la1[i] = -1;
  }

  private static int getMilitaryZoneOffset(char paramChar)
  {
    return 0;
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
    jj_la1_0 = new int[] { 2, 2032, 2032, 8386560, 8388608, -16777216, -33554432 };
  }

  private static void jj_la1_1()
  {
    jj_la1_1 = new int[] { 0, 0, 0, 0, 0, 15, 15 };
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

  public static void main(String[] paramArrayOfString)
    throws ParseException
  {
    try
    {
      while (true)
        new DateTimeParser(System.in).parseLine();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  private static int parseDigits(Token paramToken)
  {
    return Integer.parseInt(paramToken.image, 10);
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
      for (int i = 0; i < 7; i++)
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
    for (int i = 0; i < 7; i++)
      this.jj_la1[i] = -1;
  }

  public void ReInit(DateTimeParserTokenManager paramDateTimeParserTokenManager)
  {
    this.token_source = paramDateTimeParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 7; i++)
      this.jj_la1[i] = -1;
  }

  public final Date date()
    throws ParseException
  {
    int i = day();
    int j = month();
    return new Date(year(), j, i);
  }

  public final DateTime date_time()
    throws ParseException
  {
    int i;
    if (this.jj_ntk == -1)
    {
      i = jj_ntk();
      switch (i)
      {
      default:
        this.jj_la1[1] = this.jj_gen;
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
      }
    }
    while (true)
    {
      Date localDate = date();
      Time localTime = time();
      return new DateTime(localDate.getYear(), localDate.getMonth(), localDate.getDay(), localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getZone());
      i = this.jj_ntk;
      break;
      day_of_week();
      jj_consume_token(3);
    }
  }

  public final int day()
    throws ParseException
  {
    return parseDigits(jj_consume_token(46));
  }

  public final String day_of_week()
    throws ParseException
  {
    if (this.jj_ntk == -1);
    for (int i = jj_ntk(); ; i = this.jj_ntk)
      switch (i)
      {
      default:
        this.jj_la1[2] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
      }
    jj_consume_token(4);
    while (true)
    {
      return this.token.image;
      jj_consume_token(5);
      continue;
      jj_consume_token(6);
      continue;
      jj_consume_token(7);
      continue;
      jj_consume_token(8);
      continue;
      jj_consume_token(9);
      continue;
      jj_consume_token(10);
    }
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
    boolean[] arrayOfBoolean = new boolean[49];
    for (int i = 0; i < 49; i++)
      arrayOfBoolean[i] = false;
    if (this.jj_kind >= 0)
    {
      arrayOfBoolean[this.jj_kind] = true;
      this.jj_kind = -1;
    }
    for (int j = 0; j < 7; j++)
      if (this.jj_la1[j] == this.jj_gen)
        for (int n = 0; n < 32; n++)
        {
          if ((jj_la1_0[j] & 1 << n) != 0)
            arrayOfBoolean[n] = true;
          if ((jj_la1_1[j] & 1 << n) != 0)
            arrayOfBoolean[(n + 32)] = true;
        }
    for (int k = 0; k < 49; k++)
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

  public final int hour()
    throws ParseException
  {
    return parseDigits(jj_consume_token(46));
  }

  public final int minute()
    throws ParseException
  {
    return parseDigits(jj_consume_token(46));
  }

  public final int month()
    throws ParseException
  {
    if (this.jj_ntk == -1);
    for (int i = jj_ntk(); ; i = this.jj_ntk)
      switch (i)
      {
      default:
        this.jj_la1[3] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      }
    jj_consume_token(11);
    return 1;
    jj_consume_token(12);
    return 2;
    jj_consume_token(13);
    return 3;
    jj_consume_token(14);
    return 4;
    jj_consume_token(15);
    return 5;
    jj_consume_token(16);
    return 6;
    jj_consume_token(17);
    return 7;
    jj_consume_token(18);
    return 8;
    jj_consume_token(19);
    return 9;
    jj_consume_token(20);
    return 10;
    jj_consume_token(21);
    return 11;
    jj_consume_token(22);
    return 12;
  }

  public final int obs_zone()
    throws ParseException
  {
    if (this.jj_ntk == -1);
    for (int i = jj_ntk(); ; i = this.jj_ntk)
      switch (i)
      {
      default:
        this.jj_la1[6] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32:
      case 33:
      case 34:
      case 35:
      }
    jj_consume_token(25);
    int j = 0;
    while (true)
    {
      return j * 100;
      jj_consume_token(26);
      j = 0;
      continue;
      jj_consume_token(27);
      j = -5;
      continue;
      jj_consume_token(28);
      j = -4;
      continue;
      jj_consume_token(29);
      j = -6;
      continue;
      jj_consume_token(30);
      j = -5;
      continue;
      jj_consume_token(31);
      j = -7;
      continue;
      jj_consume_token(32);
      j = -6;
      continue;
      jj_consume_token(33);
      j = -8;
      continue;
      jj_consume_token(34);
      j = -7;
      continue;
      j = getMilitaryZoneOffset(jj_consume_token(35).image.charAt(0));
    }
  }

  public final DateTime parseAll()
    throws ParseException
  {
    DateTime localDateTime = date_time();
    jj_consume_token(0);
    return localDateTime;
  }

  public final DateTime parseLine()
    throws ParseException
  {
    DateTime localDateTime = date_time();
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
      return localDateTime;
      i = this.jj_ntk;
      break;
      jj_consume_token(1);
    }
  }

  public final int second()
    throws ParseException
  {
    return parseDigits(jj_consume_token(46));
  }

  public final Time time()
    throws ParseException
  {
    int i = 0;
    int j = hour();
    jj_consume_token(23);
    int k = minute();
    int m;
    if (this.jj_ntk == -1)
    {
      m = jj_ntk();
      switch (m)
      {
      default:
        this.jj_la1[4] = this.jj_gen;
      case 23:
      }
    }
    while (true)
    {
      return new Time(j, k, i, zone());
      m = this.jj_ntk;
      break;
      jj_consume_token(23);
      i = second();
    }
  }

  public final String year()
    throws ParseException
  {
    return jj_consume_token(46).image;
  }

  public final int zone()
    throws ParseException
  {
    if (this.jj_ntk == -1);
    for (int i = jj_ntk(); ; i = this.jj_ntk)
      switch (i)
      {
      default:
        this.jj_la1[5] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32:
      case 33:
      case 34:
      case 35:
      }
    Token localToken = jj_consume_token(24);
    int j = parseDigits(jj_consume_token(46));
    if (localToken.image.equals("-"));
    for (int k = -1; ; k = 1)
      return j * k;
    return obs_zone();
  }

  private static class Date
  {
    private int day;
    private int month;
    private String year;

    public Date(String paramString, int paramInt1, int paramInt2)
    {
      this.year = paramString;
      this.month = paramInt1;
      this.day = paramInt2;
    }

    public int getDay()
    {
      return this.day;
    }

    public int getMonth()
    {
      return this.month;
    }

    public String getYear()
    {
      return this.year;
    }
  }

  private static class Time
  {
    private int hour;
    private int minute;
    private int second;
    private int zone;

    public Time(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      this.hour = paramInt1;
      this.minute = paramInt2;
      this.second = paramInt3;
      this.zone = paramInt4;
    }

    public int getHour()
    {
      return this.hour;
    }

    public int getMinute()
    {
      return this.minute;
    }

    public int getSecond()
    {
      return this.second;
    }

    public int getZone()
    {
      return this.zone;
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.datetime.parser.DateTimeParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */