package org.apache.james.mime4j.field.address.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Vector;

public class AddressListParser
  implements AddressListParserTreeConstants, AddressListParserConstants
{
  private static int[] jj_la1_0;
  private static int[] jj_la1_1;
  private final JJCalls[] jj_2_rtns = new JJCalls[2];
  private int jj_endpos;
  private Vector<int[]> jj_expentries = new Vector();
  private int[] jj_expentry;
  private int jj_gc = 0;
  private int jj_gen;
  SimpleCharStream jj_input_stream;
  private int jj_kind = -1;
  private int jj_la;
  private final int[] jj_la1 = new int[22];
  private Token jj_lastpos;
  private int[] jj_lasttokens = new int[100];
  private final LookaheadSuccess jj_ls = new LookaheadSuccess(null);
  public Token jj_nt;
  private int jj_ntk;
  private boolean jj_rescan = false;
  private Token jj_scanpos;
  private boolean jj_semLA;
  protected JJTAddressListParserState jjtree = new JJTAddressListParserState();
  public boolean lookingAhead = false;
  public Token token;
  public AddressListParserTokenManager token_source;

  static
  {
    jj_la1_0();
    jj_la1_1();
  }

  public AddressListParser(InputStream paramInputStream)
  {
    this(paramInputStream, null);
  }

  public AddressListParser(InputStream paramInputStream, String paramString)
  {
    try
    {
      this.jj_input_stream = new SimpleCharStream(paramInputStream, paramString, 1, 1);
      this.token_source = new AddressListParserTokenManager(this.jj_input_stream);
      this.token = new Token();
      this.jj_ntk = -1;
      this.jj_gen = 0;
      for (int i = 0; i < 22; i++)
        this.jj_la1[i] = -1;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new RuntimeException(localUnsupportedEncodingException);
    }
    for (int j = 0; j < this.jj_2_rtns.length; j++)
      this.jj_2_rtns[j] = new JJCalls();
  }

  public AddressListParser(Reader paramReader)
  {
    this.jj_input_stream = new SimpleCharStream(paramReader, 1, 1);
    this.token_source = new AddressListParserTokenManager(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 22; i++)
      this.jj_la1[i] = -1;
    for (int j = 0; j < this.jj_2_rtns.length; j++)
      this.jj_2_rtns[j] = new JJCalls();
  }

  public AddressListParser(AddressListParserTokenManager paramAddressListParserTokenManager)
  {
    this.token_source = paramAddressListParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jj_gen = 0;
    for (int i = 0; i < 22; i++)
      this.jj_la1[i] = -1;
    for (int j = 0; j < this.jj_2_rtns.length; j++)
      this.jj_2_rtns[j] = new JJCalls();
  }

  // ERROR //
  private final boolean jj_2_1(int paramInt)
  {
    // Byte code:
    //   0: aload_0
    //   1: iload_1
    //   2: putfield 129	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_la	I
    //   5: aload_0
    //   6: getfield 110	org/apache/james/mime4j/field/address/parser/AddressListParser:token	Lorg/apache/james/mime4j/field/address/parser/Token;
    //   9: astore_2
    //   10: aload_0
    //   11: aload_2
    //   12: putfield 131	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_scanpos	Lorg/apache/james/mime4j/field/address/parser/Token;
    //   15: aload_0
    //   16: aload_2
    //   17: putfield 133	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_lastpos	Lorg/apache/james/mime4j/field/address/parser/Token;
    //   20: aload_0
    //   21: invokespecial 137	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_3_1	()Z
    //   24: istore 5
    //   26: iload 5
    //   28: ifne +15 -> 43
    //   31: iconst_1
    //   32: istore 6
    //   34: aload_0
    //   35: iconst_0
    //   36: iload_1
    //   37: invokespecial 141	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_save	(II)V
    //   40: iload 6
    //   42: ireturn
    //   43: iconst_0
    //   44: istore 6
    //   46: goto -12 -> 34
    //   49: astore 4
    //   51: aload_0
    //   52: iconst_0
    //   53: iload_1
    //   54: invokespecial 141	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_save	(II)V
    //   57: iconst_1
    //   58: ireturn
    //   59: astore_3
    //   60: aload_0
    //   61: iconst_0
    //   62: iload_1
    //   63: invokespecial 141	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_save	(II)V
    //   66: aload_3
    //   67: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   20	26	49	org/apache/james/mime4j/field/address/parser/AddressListParser$LookaheadSuccess
    //   20	26	59	finally
  }

  // ERROR //
  private final boolean jj_2_2(int paramInt)
  {
    // Byte code:
    //   0: aload_0
    //   1: iload_1
    //   2: putfield 129	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_la	I
    //   5: aload_0
    //   6: getfield 110	org/apache/james/mime4j/field/address/parser/AddressListParser:token	Lorg/apache/james/mime4j/field/address/parser/Token;
    //   9: astore_2
    //   10: aload_0
    //   11: aload_2
    //   12: putfield 131	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_scanpos	Lorg/apache/james/mime4j/field/address/parser/Token;
    //   15: aload_0
    //   16: aload_2
    //   17: putfield 133	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_lastpos	Lorg/apache/james/mime4j/field/address/parser/Token;
    //   20: aload_0
    //   21: invokespecial 145	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_3_2	()Z
    //   24: istore 5
    //   26: iload 5
    //   28: ifne +15 -> 43
    //   31: iconst_1
    //   32: istore 6
    //   34: aload_0
    //   35: iconst_1
    //   36: iload_1
    //   37: invokespecial 141	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_save	(II)V
    //   40: iload 6
    //   42: ireturn
    //   43: iconst_0
    //   44: istore 6
    //   46: goto -12 -> 34
    //   49: astore 4
    //   51: aload_0
    //   52: iconst_1
    //   53: iload_1
    //   54: invokespecial 141	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_save	(II)V
    //   57: iconst_1
    //   58: ireturn
    //   59: astore_3
    //   60: aload_0
    //   61: iconst_1
    //   62: iload_1
    //   63: invokespecial 141	org/apache/james/mime4j/field/address/parser/AddressListParser:jj_save	(II)V
    //   66: aload_3
    //   67: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   20	26	49	org/apache/james/mime4j/field/address/parser/AddressListParser$LookaheadSuccess
    //   20	26	59	finally
  }

  private final boolean jj_3R_10()
  {
    Token localToken = this.jj_scanpos;
    if (jj_3R_12())
    {
      this.jj_scanpos = localToken;
      if (jj_scan_token(18))
        return true;
    }
    return false;
  }

  private final boolean jj_3R_11()
  {
    Token localToken1 = this.jj_scanpos;
    if (jj_scan_token(9))
      this.jj_scanpos = localToken1;
    Token localToken2 = this.jj_scanpos;
    if (jj_scan_token(14))
    {
      this.jj_scanpos = localToken2;
      if (jj_scan_token(31))
        return true;
    }
    return false;
  }

  private final boolean jj_3R_12()
  {
    if (jj_scan_token(14))
      return true;
    Token localToken;
    do
      localToken = this.jj_scanpos;
    while (!jj_3R_13());
    this.jj_scanpos = localToken;
    return false;
  }

  private final boolean jj_3R_13()
  {
    Token localToken = this.jj_scanpos;
    if (jj_scan_token(9))
      this.jj_scanpos = localToken;
    return jj_scan_token(14);
  }

  private final boolean jj_3R_8()
  {
    if (jj_3R_9())
      return true;
    if (jj_scan_token(8))
      return true;
    return jj_3R_10();
  }

  private final boolean jj_3R_9()
  {
    Token localToken1 = this.jj_scanpos;
    if (jj_scan_token(14))
    {
      this.jj_scanpos = localToken1;
      if (jj_scan_token(31))
        return true;
    }
    Token localToken2;
    do
      localToken2 = this.jj_scanpos;
    while (!jj_3R_11());
    this.jj_scanpos = localToken2;
    return false;
  }

  private final boolean jj_3_1()
  {
    return jj_3R_8();
  }

  private final boolean jj_3_2()
  {
    return jj_3R_8();
  }

  private void jj_add_error_token(int paramInt1, int paramInt2)
  {
    if (paramInt2 >= 100);
    do
    {
      return;
      if (paramInt2 == 1 + this.jj_endpos)
      {
        int[] arrayOfInt3 = this.jj_lasttokens;
        int m = this.jj_endpos;
        this.jj_endpos = (m + 1);
        arrayOfInt3[m] = paramInt1;
        return;
      }
    }
    while (this.jj_endpos == 0);
    this.jj_expentry = new int[this.jj_endpos];
    for (int i = 0; i < this.jj_endpos; i++)
      this.jj_expentry[i] = this.jj_lasttokens[i];
    int j = 0;
    Enumeration localEnumeration = this.jj_expentries.elements();
    label101: int[] arrayOfInt2;
    while (localEnumeration.hasMoreElements())
    {
      arrayOfInt2 = (int[])localEnumeration.nextElement();
      if (arrayOfInt2.length == this.jj_expentry.length)
        j = 1;
    }
    for (int k = 0; ; k++)
      if (k < this.jj_expentry.length)
      {
        if (arrayOfInt2[k] != this.jj_expentry[k])
          j = 0;
      }
      else
      {
        if (j == 0)
          break label101;
        if (j == 0)
          this.jj_expentries.addElement(this.jj_expentry);
        if (paramInt2 == 0)
          break;
        int[] arrayOfInt1 = this.jj_lasttokens;
        this.jj_endpos = paramInt2;
        arrayOfInt1[(paramInt2 - 1)] = paramInt1;
        return;
      }
  }

  private final Token jj_consume_token(int paramInt)
    throws ParseException
  {
    Token localToken1 = this.token;
    if (localToken1.next != null)
    {
      this.token = this.token.next;
      this.jj_ntk = -1;
      if (this.token.kind != paramInt)
        break label170;
      this.jj_gen = (1 + this.jj_gen);
      int i = 1 + this.jj_gc;
      this.jj_gc = i;
      if (i > 100)
        this.jj_gc = 0;
    }
    else
    {
      for (int j = 0; ; j++)
      {
        if (j >= this.jj_2_rtns.length)
          break label165;
        JJCalls localJJCalls = this.jj_2_rtns[j];
        while (true)
          if (localJJCalls != null)
          {
            if (localJJCalls.gen < this.jj_gen)
              localJJCalls.first = null;
            localJJCalls = localJJCalls.next;
            continue;
            Token localToken2 = this.token;
            Token localToken3 = this.token_source.getNextToken();
            localToken2.next = localToken3;
            this.token = localToken3;
            break;
          }
      }
    }
    label165: return this.token;
    label170: this.token = localToken1;
    this.jj_kind = paramInt;
    throw generateParseException();
  }

  private static void jj_la1_0()
  {
    jj_la1_0 = new int[] { 2, -2147467200, 8, -2147467200, 80, -2147467200, -2147467200, -2147467200, 8, -2147467200, 256, 264, 8, -2147467264, -2147467264, -2147467264, -2147466752, 512, -2147467264, 16896, 512, 278528 };
  }

  private static void jj_la1_1()
  {
    jj_la1_1 = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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

  private final void jj_rescan_token()
  {
    this.jj_rescan = true;
    i = 0;
    while (true)
      if (i < 2)
        try
        {
          JJCalls localJJCalls = this.jj_2_rtns[i];
          if (localJJCalls.gen > this.jj_gen)
          {
            this.jj_la = localJJCalls.arg;
            Token localToken = localJJCalls.first;
            this.jj_scanpos = localToken;
            this.jj_lastpos = localToken;
            switch (i)
            {
            default:
            case 0:
            case 1:
            }
          }
          while (true)
          {
            localJJCalls = localJJCalls.next;
            if (localJJCalls != null)
              break;
            break label114;
            jj_3_1();
            continue;
            jj_3_2();
          }
          this.jj_rescan = false;
          return;
          i++;
        }
        catch (LookaheadSuccess localLookaheadSuccess)
        {
          break label114;
        }
  }

  private final void jj_save(int paramInt1, int paramInt2)
  {
    for (Object localObject = this.jj_2_rtns[paramInt1]; ; localObject = ((JJCalls)localObject).next)
      if (((JJCalls)localObject).gen > this.jj_gen)
      {
        if (((JJCalls)localObject).next == null)
        {
          JJCalls localJJCalls = new JJCalls();
          ((JJCalls)localObject).next = localJJCalls;
          localObject = localJJCalls;
        }
      }
      else
      {
        ((JJCalls)localObject).gen = (paramInt2 + this.jj_gen - this.jj_la);
        ((JJCalls)localObject).first = this.token;
        ((JJCalls)localObject).arg = paramInt2;
        return;
      }
  }

  private final boolean jj_scan_token(int paramInt)
  {
    if (this.jj_scanpos == this.jj_lastpos)
    {
      this.jj_la -= 1;
      if (this.jj_scanpos.next == null)
      {
        Token localToken3 = this.jj_scanpos;
        Token localToken4 = this.token_source.getNextToken();
        localToken3.next = localToken4;
        this.jj_scanpos = localToken4;
        this.jj_lastpos = localToken4;
      }
    }
    while (this.jj_rescan)
    {
      int i = 0;
      Token localToken1 = this.token;
      while (true)
        if ((localToken1 != null) && (localToken1 != this.jj_scanpos))
        {
          i++;
          localToken1 = localToken1.next;
          continue;
          Token localToken2 = this.jj_scanpos.next;
          this.jj_scanpos = localToken2;
          this.jj_lastpos = localToken2;
          break;
          this.jj_scanpos = this.jj_scanpos.next;
          break;
        }
      if (localToken1 != null)
        jj_add_error_token(paramInt, i);
    }
    if (this.jj_scanpos.kind != paramInt)
      return true;
    if ((this.jj_la == 0) && (this.jj_scanpos == this.jj_lastpos))
      throw this.jj_ls;
    return false;
  }

  public static void main(String[] paramArrayOfString)
    throws ParseException
  {
    try
    {
      while (true)
      {
        AddressListParser localAddressListParser = new AddressListParser(System.in);
        localAddressListParser.parseLine();
        ((SimpleNode)localAddressListParser.jjtree.rootNode()).dump("> ");
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
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
      this.jjtree.reset();
      this.jj_gen = 0;
      for (int i = 0; i < 22; i++)
        this.jj_la1[i] = -1;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new RuntimeException(localUnsupportedEncodingException);
    }
    for (int j = 0; j < this.jj_2_rtns.length; j++)
      this.jj_2_rtns[j] = new JJCalls();
  }

  public void ReInit(Reader paramReader)
  {
    this.jj_input_stream.ReInit(paramReader, 1, 1);
    this.token_source.ReInit(this.jj_input_stream);
    this.token = new Token();
    this.jj_ntk = -1;
    this.jjtree.reset();
    this.jj_gen = 0;
    for (int i = 0; i < 22; i++)
      this.jj_la1[i] = -1;
    for (int j = 0; j < this.jj_2_rtns.length; j++)
      this.jj_2_rtns[j] = new JJCalls();
  }

  public void ReInit(AddressListParserTokenManager paramAddressListParserTokenManager)
  {
    this.token_source = paramAddressListParserTokenManager;
    this.token = new Token();
    this.jj_ntk = -1;
    this.jjtree.reset();
    this.jj_gen = 0;
    for (int i = 0; i < 22; i++)
      this.jj_la1[i] = -1;
    for (int j = 0; j < this.jj_2_rtns.length; j++)
      this.jj_2_rtns[j] = new JJCalls();
  }

  public final void addr_spec()
    throws ParseException
  {
    ASTaddr_spec localASTaddr_spec = new ASTaddr_spec(9);
    int i = 1;
    this.jjtree.openNodeScope(localASTaddr_spec);
    jjtreeOpenNodeScope(localASTaddr_spec);
    try
    {
      local_part();
      jj_consume_token(8);
      domain();
      return;
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTaddr_spec);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label121;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTaddr_spec, true);
        jjtreeCloseNodeScope(localASTaddr_spec);
      }
    }
    while (true)
      this.jjtree.popNode();
    label121: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
  }

  public final void address()
    throws ParseException
  {
    ASTaddress localASTaddress = new ASTaddress(2);
    int i = 1;
    this.jjtree.openNodeScope(localASTaddress);
    jjtreeOpenNodeScope(localASTaddress);
    try
    {
      if (jj_2_1(2147483647))
      {
        addr_spec();
        return;
      }
      if (this.jj_ntk == -1)
      {
        j = jj_ntk();
        break label264;
        this.jj_la1[5] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTaddress);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label244;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTaddress, true);
        jjtreeCloseNodeScope(localASTaddress);
      }
    }
    int j;
    int k;
    while (true)
    {
      j = this.jj_ntk;
      break label264;
      angle_addr();
      break;
      phrase();
      if (this.jj_ntk == -1)
      {
        k = jj_ntk();
        break label300;
        this.jj_la1[4] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      k = this.jj_ntk;
      break label300;
      group_body();
      break;
      angle_addr();
      break;
      this.jjtree.popNode();
    }
    label244: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
    label264: switch (j)
    {
    default:
    case 6:
    case 14:
    case 31:
    }
    label300: switch (k)
    {
    case 5:
    default:
    case 4:
    case 6:
    }
  }

  public final void address_list()
    throws ParseException
  {
    ASTaddress_list localASTaddress_list = new ASTaddress_list(1);
    int i = 1;
    this.jjtree.openNodeScope(localASTaddress_list);
    jjtreeOpenNodeScope(localASTaddress_list);
    int j;
    try
    {
      if (this.jj_ntk == -1)
      {
        j = jj_ntk();
        break label256;
        this.jj_la1[1] = this.jj_gen;
      }
      while (this.jj_ntk == -1)
      {
        k = jj_ntk();
        break label292;
        this.jj_la1[2] = this.jj_gen;
        return;
        j = this.jj_ntk;
        break label256;
        address();
      }
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTaddress_list);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label236;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTaddress_list, true);
        jjtreeCloseNodeScope(localASTaddress_list);
      }
    }
    int k;
    int m;
    while (true)
    {
      k = this.jj_ntk;
      break;
      jj_consume_token(3);
      if (this.jj_ntk == -1)
      {
        m = jj_ntk();
        break label312;
        this.jj_la1[3] = this.jj_gen;
      }
      else
      {
        m = this.jj_ntk;
        break label312;
        address();
        continue;
        this.jjtree.popNode();
      }
    }
    label236: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
    label256: switch (j)
    {
    default:
    case 6:
    case 14:
    case 31:
    }
    label292: switch (k)
    {
    default:
    case 3:
    }
    label312: switch (m)
    {
    default:
    case 6:
    case 14:
    case 31:
    }
  }

  public final void angle_addr()
    throws ParseException
  {
    ASTangle_addr localASTangle_addr = new ASTangle_addr(6);
    int i = 1;
    this.jjtree.openNodeScope(localASTangle_addr);
    jjtreeOpenNodeScope(localASTangle_addr);
    int j;
    try
    {
      jj_consume_token(6);
      if (this.jj_ntk == -1)
      {
        j = jj_ntk();
        break label188;
        this.jj_la1[10] = this.jj_gen;
      }
      while (true)
      {
        addr_spec();
        jj_consume_token(7);
        return;
        j = this.jj_ntk;
        break;
        route();
      }
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTangle_addr);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label168;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTangle_addr, true);
        jjtreeCloseNodeScope(localASTangle_addr);
      }
    }
    while (true)
      this.jjtree.popNode();
    label168: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
    label188: switch (j)
    {
    default:
    case 8:
    }
  }

  public final void disable_tracing()
  {
  }

  public final void domain()
    throws ParseException
  {
    ASTdomain localASTdomain = new ASTdomain(11);
    this.jjtree.openNodeScope(localASTdomain);
    jjtreeOpenNodeScope(localASTdomain);
    try
    {
      if (this.jj_ntk == -1)
      {
        i = jj_ntk();
        break label260;
        this.jj_la1[21] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    finally
    {
      if (1 != 0)
      {
        this.jjtree.closeNodeScope(localASTdomain, true);
        jjtreeCloseNodeScope(localASTdomain);
      }
    }
    int i = this.jj_ntk;
    break label260;
    Token localToken = jj_consume_token(14);
    int j;
    int k;
    if (this.jj_ntk == -1)
    {
      j = jj_ntk();
      break label288;
      this.jj_la1[19] = this.jj_gen;
    }
    else
    {
      while (true)
      {
        if (1 != 0)
        {
          this.jjtree.closeNodeScope(localASTdomain, true);
          jjtreeCloseNodeScope(localASTdomain);
        }
        return;
        j = this.jj_ntk;
        break label288;
        if (this.jj_ntk == -1)
        {
          k = jj_ntk();
          break label316;
          this.jj_la1[20] = this.jj_gen;
        }
        while (localToken.image.charAt(localToken.image.length() - 1) != '.')
        {
          throw new ParseException("Atoms in domain names must be separated by '.'");
          k = this.jj_ntk;
          break label316;
          localToken = jj_consume_token(9);
        }
        localToken = jj_consume_token(14);
        break;
        jj_consume_token(18);
      }
      label260: switch (i)
      {
      default:
      case 14:
      case 18:
      }
    }
    label288: switch (j)
    {
    default:
    case 9:
    case 14:
    }
    label316: switch (k)
    {
    default:
    case 9:
    }
  }

  public final void enable_tracing()
  {
  }

  public ParseException generateParseException()
  {
    this.jj_expentries.removeAllElements();
    boolean[] arrayOfBoolean = new boolean[34];
    for (int i = 0; i < 34; i++)
      arrayOfBoolean[i] = false;
    if (this.jj_kind >= 0)
    {
      arrayOfBoolean[this.jj_kind] = true;
      this.jj_kind = -1;
    }
    for (int j = 0; j < 22; j++)
      if (this.jj_la1[j] == this.jj_gen)
        for (int n = 0; n < 32; n++)
        {
          if ((jj_la1_0[j] & 1 << n) != 0)
            arrayOfBoolean[n] = true;
          if ((jj_la1_1[j] & 1 << n) != 0)
            arrayOfBoolean[(n + 32)] = true;
        }
    for (int k = 0; k < 34; k++)
      if (arrayOfBoolean[k] != 0)
      {
        this.jj_expentry = new int[1];
        this.jj_expentry[0] = k;
        this.jj_expentries.addElement(this.jj_expentry);
      }
    this.jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
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
    Token localToken1;
    int i;
    Object localObject;
    label17: Token localToken2;
    if (this.lookingAhead)
    {
      localToken1 = this.jj_scanpos;
      i = 0;
      localObject = localToken1;
      if (i >= paramInt)
        break label74;
      if (((Token)localObject).next == null)
        break label55;
      localToken2 = ((Token)localObject).next;
    }
    while (true)
    {
      i++;
      localObject = localToken2;
      break label17;
      localToken1 = this.token;
      break;
      label55: localToken2 = this.token_source.getNextToken();
      ((Token)localObject).next = localToken2;
    }
    label74: return localObject;
  }

  public final void group_body()
    throws ParseException
  {
    ASTgroup_body localASTgroup_body = new ASTgroup_body(5);
    int i = 1;
    this.jjtree.openNodeScope(localASTgroup_body);
    jjtreeOpenNodeScope(localASTgroup_body);
    int j;
    try
    {
      jj_consume_token(4);
      if (this.jj_ntk == -1)
      {
        j = jj_ntk();
        break label271;
        this.jj_la1[7] = this.jj_gen;
      }
      while (this.jj_ntk == -1)
      {
        k = jj_ntk();
        break label308;
        this.jj_la1[8] = this.jj_gen;
        jj_consume_token(5);
        return;
        j = this.jj_ntk;
        break label271;
        mailbox();
      }
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTgroup_body);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label251;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTgroup_body, true);
        jjtreeCloseNodeScope(localASTgroup_body);
      }
    }
    int k;
    int m;
    while (true)
    {
      k = this.jj_ntk;
      break;
      jj_consume_token(3);
      if (this.jj_ntk == -1)
      {
        m = jj_ntk();
        break label328;
        this.jj_la1[9] = this.jj_gen;
      }
      else
      {
        m = this.jj_ntk;
        break label328;
        mailbox();
        continue;
        this.jjtree.popNode();
      }
    }
    label251: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
    label271: switch (j)
    {
    default:
    case 6:
    case 14:
    case 31:
    }
    label308: switch (k)
    {
    default:
    case 3:
    }
    label328: switch (m)
    {
    default:
    case 6:
    case 14:
    case 31:
    }
  }

  void jjtreeCloseNodeScope(Node paramNode)
  {
    ((SimpleNode)paramNode).lastToken = getToken(0);
  }

  void jjtreeOpenNodeScope(Node paramNode)
  {
    ((SimpleNode)paramNode).firstToken = getToken(1);
  }

  public final void local_part()
    throws ParseException
  {
    ASTlocal_part localASTlocal_part = new ASTlocal_part(10);
    this.jjtree.openNodeScope(localASTlocal_part);
    jjtreeOpenNodeScope(localASTlocal_part);
    try
    {
      if (this.jj_ntk == -1)
      {
        i = jj_ntk();
        break label337;
        this.jj_la1[15] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    finally
    {
      if (1 != 0)
      {
        this.jjtree.closeNodeScope(localASTlocal_part, true);
        jjtreeCloseNodeScope(localASTlocal_part);
      }
    }
    int i = this.jj_ntk;
    break label337;
    Object localObject2 = jj_consume_token(14);
    int j;
    int k;
    int m;
    while (true)
      if (this.jj_ntk == -1)
      {
        j = jj_ntk();
        break;
        this.jj_la1[16] = this.jj_gen;
        if (1 != 0)
        {
          this.jjtree.closeNodeScope(localASTlocal_part, true);
          jjtreeCloseNodeScope(localASTlocal_part);
        }
        return;
        localObject2 = jj_consume_token(31);
      }
      else
      {
        j = this.jj_ntk;
        break;
        if (this.jj_ntk == -1)
        {
          k = jj_ntk();
          break label400;
          this.jj_la1[17] = this.jj_gen;
        }
        while ((((Token)localObject2).kind == 31) || (((Token)localObject2).image.charAt(((Token)localObject2).image.length() - 1) != '.'))
        {
          throw new ParseException("Words in local part must be separated by '.'");
          k = this.jj_ntk;
          break label400;
          localObject2 = jj_consume_token(9);
        }
        if (this.jj_ntk == -1)
        {
          m = jj_ntk();
          break label420;
          this.jj_la1[18] = this.jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        m = this.jj_ntk;
        break label420;
        localObject2 = jj_consume_token(14);
        continue;
        Token localToken = jj_consume_token(31);
        localObject2 = localToken;
      }
    label337: switch (i)
    {
    default:
    case 14:
    case 31:
    }
    switch (j)
    {
    default:
    case 9:
    case 14:
    case 31:
    }
    label400: switch (k)
    {
    default:
    case 9:
    }
    label420: switch (m)
    {
    default:
    case 14:
    case 31:
    }
  }

  public final void mailbox()
    throws ParseException
  {
    ASTmailbox localASTmailbox = new ASTmailbox(3);
    int i = 1;
    this.jjtree.openNodeScope(localASTmailbox);
    jjtreeOpenNodeScope(localASTmailbox);
    try
    {
      if (jj_2_2(2147483647))
      {
        addr_spec();
        return;
      }
      if (this.jj_ntk == -1)
      {
        j = jj_ntk();
        break label204;
        this.jj_la1[6] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTmailbox);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label184;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTmailbox, true);
        jjtreeCloseNodeScope(localASTmailbox);
      }
    }
    int j;
    while (true)
    {
      j = this.jj_ntk;
      break label204;
      angle_addr();
      break;
      name_addr();
      break;
      this.jjtree.popNode();
    }
    label184: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
    label204: switch (j)
    {
    default:
    case 6:
    case 14:
    case 31:
    }
  }

  public final void name_addr()
    throws ParseException
  {
    ASTname_addr localASTname_addr = new ASTname_addr(4);
    int i = 1;
    this.jjtree.openNodeScope(localASTname_addr);
    jjtreeOpenNodeScope(localASTname_addr);
    try
    {
      phrase();
      angle_addr();
      return;
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTname_addr);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label113;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTname_addr, true);
        jjtreeCloseNodeScope(localASTname_addr);
      }
    }
    while (true)
      this.jjtree.popNode();
    label113: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
  }

  public ASTaddress parseAddress()
    throws ParseException
  {
    try
    {
      parseAddress0();
      ASTaddress localASTaddress = (ASTaddress)this.jjtree.rootNode();
      return localASTaddress;
    }
    catch (TokenMgrError localTokenMgrError)
    {
      throw new ParseException(localTokenMgrError.getMessage());
    }
  }

  public final void parseAddress0()
    throws ParseException
  {
    address();
    jj_consume_token(0);
  }

  public ASTaddress_list parseAddressList()
    throws ParseException
  {
    try
    {
      parseAddressList0();
      ASTaddress_list localASTaddress_list = (ASTaddress_list)this.jjtree.rootNode();
      return localASTaddress_list;
    }
    catch (TokenMgrError localTokenMgrError)
    {
      throw new ParseException(localTokenMgrError.getMessage());
    }
  }

  public final void parseAddressList0()
    throws ParseException
  {
    address_list();
    jj_consume_token(0);
  }

  public final void parseLine()
    throws ParseException
  {
    address_list();
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

  public ASTmailbox parseMailbox()
    throws ParseException
  {
    try
    {
      parseMailbox0();
      ASTmailbox localASTmailbox = (ASTmailbox)this.jjtree.rootNode();
      return localASTmailbox;
    }
    catch (TokenMgrError localTokenMgrError)
    {
      throw new ParseException(localTokenMgrError.getMessage());
    }
  }

  public final void parseMailbox0()
    throws ParseException
  {
    mailbox();
    jj_consume_token(0);
  }

  public final void phrase()
    throws ParseException
  {
    ASTphrase localASTphrase = new ASTphrase(8);
    this.jjtree.openNodeScope(localASTphrase);
    jjtreeOpenNodeScope(localASTphrase);
    try
    {
      if (this.jj_ntk == -1)
      {
        i = jj_ntk();
        break label166;
        this.jj_la1[13] = this.jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    finally
    {
      if (1 != 0)
      {
        this.jjtree.closeNodeScope(localASTphrase, true);
        jjtreeCloseNodeScope(localASTphrase);
      }
    }
    int i = this.jj_ntk;
    break label166;
    jj_consume_token(14);
    while (this.jj_ntk == -1)
    {
      j = jj_ntk();
      break label192;
      this.jj_la1[14] = this.jj_gen;
      if (1 != 0)
      {
        this.jjtree.closeNodeScope(localASTphrase, true);
        jjtreeCloseNodeScope(localASTphrase);
      }
      return;
      jj_consume_token(31);
    }
    int j = this.jj_ntk;
    break label192;
    label166: switch (i)
    {
    default:
    case 14:
    case 31:
    }
    label192: switch (j)
    {
    case 14:
    case 31:
    }
  }

  public final void route()
    throws ParseException
  {
    ASTroute localASTroute = new ASTroute(7);
    int i = 1;
    this.jjtree.openNodeScope(localASTroute);
    jjtreeOpenNodeScope(localASTroute);
    try
    {
      jj_consume_token(8);
      domain();
      while (true)
      {
        if (this.jj_ntk == -1)
        {
          j = jj_ntk();
          break label237;
          this.jj_la1[11] = this.jj_gen;
          jj_consume_token(4);
          return;
        }
        j = this.jj_ntk;
        break label237;
        jj_consume_token(3);
        if (this.jj_ntk != -1)
          break;
        k = jj_ntk();
        break label264;
        this.jj_la1[12] = this.jj_gen;
        jj_consume_token(8);
        domain();
      }
    }
    catch (Throwable localThrowable)
    {
      if (i != 0)
      {
        this.jjtree.clearNodeScope(localASTroute);
        i = 0;
        if (!(localThrowable instanceof RuntimeException))
          break label217;
        throw ((RuntimeException)localThrowable);
      }
    }
    finally
    {
      if (i != 0)
      {
        this.jjtree.closeNodeScope(localASTroute, true);
        jjtreeCloseNodeScope(localASTroute);
      }
    }
    int j;
    int k;
    while (true)
    {
      k = this.jj_ntk;
      break;
      this.jjtree.popNode();
    }
    label217: if ((localThrowable instanceof ParseException))
      throw ((ParseException)localThrowable);
    throw ((Error)localThrowable);
    label237: switch (j)
    {
    default:
    case 3:
    case 8:
    }
    label264: switch (k)
    {
    case 3:
    }
  }

  static final class JJCalls
  {
    int arg;
    Token first;
    int gen;
    JJCalls next;

    JJCalls()
    {
    }
  }

  private static final class LookaheadSuccess extends Error
  {
    private LookaheadSuccess()
    {
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.parser.AddressListParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */