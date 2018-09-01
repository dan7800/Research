package org.apache.james.mime4j.field.language.parser;

public class ParseException extends org.apache.james.mime4j.field.ParseException
{
  private static final long serialVersionUID = 1L;
  public Token currentToken;
  protected String eol = System.getProperty("line.separator", "\n");
  public int[][] expectedTokenSequences;
  protected boolean specialConstructor;
  public String[] tokenImage;

  public ParseException()
  {
    super("Cannot parse field");
    this.specialConstructor = false;
  }

  public ParseException(String paramString)
  {
    super(paramString);
    this.specialConstructor = false;
  }

  public ParseException(Throwable paramThrowable)
  {
    super(paramThrowable);
    this.specialConstructor = false;
  }

  public ParseException(Token paramToken, int[][] paramArrayOfInt, String[] paramArrayOfString)
  {
    super("");
    this.specialConstructor = true;
    this.currentToken = paramToken;
    this.expectedTokenSequences = paramArrayOfInt;
    this.tokenImage = paramArrayOfString;
  }

  protected String add_escapes(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    if (i < paramString.length())
    {
      char c;
      switch (paramString.charAt(i))
      {
      default:
        c = paramString.charAt(i);
        if ((c < ' ') || (c > '~'))
        {
          String str = "0000" + Integer.toString(c, 16);
          localStringBuffer.append("\\u" + str.substring(str.length() - 4, str.length()));
        }
      case '\000':
      case '\b':
      case '\t':
      case '\n':
      case '\f':
      case '\r':
      case '"':
      case '\'':
      case '\\':
      }
      while (true)
      {
        i++;
        break;
        localStringBuffer.append("\\b");
        continue;
        localStringBuffer.append("\\t");
        continue;
        localStringBuffer.append("\\n");
        continue;
        localStringBuffer.append("\\f");
        continue;
        localStringBuffer.append("\\r");
        continue;
        localStringBuffer.append("\\\"");
        continue;
        localStringBuffer.append("\\'");
        continue;
        localStringBuffer.append("\\\\");
        continue;
        localStringBuffer.append(c);
      }
    }
    return localStringBuffer.toString();
  }

  public String getMessage()
  {
    if (!this.specialConstructor)
      return super.getMessage();
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    for (int j = 0; j < this.expectedTokenSequences.length; j++)
    {
      if (i < this.expectedTokenSequences[j].length)
        i = this.expectedTokenSequences[j].length;
      for (int m = 0; m < this.expectedTokenSequences[j].length; m++)
        localStringBuffer.append(this.tokenImage[this.expectedTokenSequences[j][m]]).append(" ");
      if (this.expectedTokenSequences[j][(this.expectedTokenSequences[j].length - 1)] != 0)
        localStringBuffer.append("...");
      localStringBuffer.append(this.eol).append("    ");
    }
    String str1 = "Encountered \"";
    Token localToken = this.currentToken.next;
    int k = 0;
    String str3;
    if (k < i)
    {
      if (k != 0)
        str1 = str1 + " ";
      if (localToken.kind == 0)
        str1 = str1 + this.tokenImage[0];
    }
    else
    {
      String str2 = str1 + "\" at line " + this.currentToken.next.beginLine + ", column " + this.currentToken.next.beginColumn;
      str3 = str2 + "." + this.eol;
      if (this.expectedTokenSequences.length != 1)
        break label416;
    }
    label416: for (String str4 = str3 + "Was expecting:" + this.eol + "    "; ; str4 = str3 + "Was expecting one of:" + this.eol + "    ")
    {
      return str4 + localStringBuffer.toString();
      str1 = str1 + add_escapes(localToken.image);
      localToken = localToken.next;
      k++;
      break;
    }
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.language.parser.ParseException
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */