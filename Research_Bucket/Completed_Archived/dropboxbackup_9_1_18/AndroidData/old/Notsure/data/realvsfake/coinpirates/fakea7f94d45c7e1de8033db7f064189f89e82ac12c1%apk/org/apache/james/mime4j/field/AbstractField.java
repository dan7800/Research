package org.apache.james.mime4j.field;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

public abstract class AbstractField
  implements ParsedField
{
  private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^([\\x21-\\x39\\x3b-\\x7e]+):");
  private static final DefaultFieldParser parser = new DefaultFieldParser();
  private final String body;
  private final String name;
  private final ByteSequence raw;

  protected AbstractField(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    this.name = paramString1;
    this.body = paramString2;
    this.raw = paramByteSequence;
  }

  public static DefaultFieldParser getParser()
  {
    return parser;
  }

  public static ParsedField parse(String paramString)
    throws MimeException
  {
    return parse(ContentUtil.encode(paramString), paramString);
  }

  public static ParsedField parse(ByteSequence paramByteSequence)
    throws MimeException
  {
    return parse(paramByteSequence, ContentUtil.decode(paramByteSequence));
  }

  private static ParsedField parse(ByteSequence paramByteSequence, String paramString)
    throws MimeException
  {
    String str1 = MimeUtil.unfold(paramString);
    Matcher localMatcher = FIELD_NAME_PATTERN.matcher(str1);
    if (!localMatcher.find())
      throw new MimeException("Invalid field in string");
    String str2 = localMatcher.group(1);
    String str3 = str1.substring(localMatcher.end());
    if ((str3.length() > 0) && (str3.charAt(0) == ' '))
      str3 = str3.substring(1);
    return parser.parse(str2, str3, paramByteSequence);
  }

  public String getBody()
  {
    return this.body;
  }

  public String getName()
  {
    return this.name;
  }

  public ParseException getParseException()
  {
    return null;
  }

  public ByteSequence getRaw()
  {
    return this.raw;
  }

  public boolean isValidField()
  {
    return getParseException() == null;
  }

  public String toString()
  {
    return this.name + ": " + this.body;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.AbstractField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */