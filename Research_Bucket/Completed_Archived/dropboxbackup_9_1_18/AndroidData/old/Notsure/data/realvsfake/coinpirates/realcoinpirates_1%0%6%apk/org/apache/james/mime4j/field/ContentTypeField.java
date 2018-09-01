package org.apache.james.mime4j.field;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.contenttype.parser.ContentTypeParser;
import org.apache.james.mime4j.field.contenttype.parser.ParseException;
import org.apache.james.mime4j.field.contenttype.parser.TokenMgrError;
import org.apache.james.mime4j.util.ByteSequence;

public class ContentTypeField extends AbstractField
{
  public static final String PARAM_BOUNDARY = "boundary";
  public static final String PARAM_CHARSET = "charset";
  static final FieldParser PARSER = new FieldParser()
  {
    public ParsedField parse(String paramAnonymousString1, String paramAnonymousString2, ByteSequence paramAnonymousByteSequence)
    {
      return new ContentTypeField(paramAnonymousString1, paramAnonymousString2, paramAnonymousByteSequence);
    }
  };
  public static final String TYPE_MESSAGE_RFC822 = "message/rfc822";
  public static final String TYPE_MULTIPART_DIGEST = "multipart/digest";
  public static final String TYPE_MULTIPART_PREFIX = "multipart/";
  public static final String TYPE_TEXT_PLAIN = "text/plain";
  private static Log log = LogFactory.getLog(ContentTypeField.class);
  private String mimeType = "";
  private Map<String, String> parameters = new HashMap();
  private ParseException parseException;
  private boolean parsed = false;

  ContentTypeField(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    super(paramString1, paramString2, paramByteSequence);
  }

  public static String getCharset(ContentTypeField paramContentTypeField)
  {
    if (paramContentTypeField != null)
    {
      String str = paramContentTypeField.getCharset();
      if ((str != null) && (str.length() > 0))
        return str;
    }
    return "us-ascii";
  }

  public static String getMimeType(ContentTypeField paramContentTypeField1, ContentTypeField paramContentTypeField2)
  {
    if ((paramContentTypeField1 == null) || (paramContentTypeField1.getMimeType().length() == 0) || ((paramContentTypeField1.isMultipart()) && (paramContentTypeField1.getBoundary() == null)))
    {
      if ((paramContentTypeField2 != null) && (paramContentTypeField2.isMimeType("multipart/digest")))
        return "message/rfc822";
      return "text/plain";
    }
    return paramContentTypeField1.getMimeType();
  }

  private void parse()
  {
    String str1 = getBody();
    ContentTypeParser localContentTypeParser = new ContentTypeParser(new StringReader(str1));
    try
    {
      localContentTypeParser.parseAll();
      String str2 = localContentTypeParser.getType();
      String str3 = localContentTypeParser.getSubType();
      if ((str2 != null) && (str3 != null))
      {
        this.mimeType = (str2 + "/" + str3).toLowerCase();
        List localList1 = localContentTypeParser.getParamNames();
        List localList2 = localContentTypeParser.getParamValues();
        if ((localList1 != null) && (localList2 != null))
        {
          int i = Math.min(localList1.size(), localList2.size());
          for (int j = 0; j < i; j++)
          {
            String str4 = ((String)localList1.get(j)).toLowerCase();
            String str5 = (String)localList2.get(j);
            this.parameters.put(str4, str5);
          }
        }
      }
    }
    catch (ParseException localParseException)
    {
      while (true)
      {
        if (log.isDebugEnabled())
          log.debug("Parsing value '" + str1 + "': " + localParseException.getMessage());
        this.parseException = localParseException;
      }
    }
    catch (TokenMgrError localTokenMgrError)
    {
      while (true)
      {
        if (log.isDebugEnabled())
          log.debug("Parsing value '" + str1 + "': " + localTokenMgrError.getMessage());
        this.parseException = new ParseException(localTokenMgrError.getMessage());
      }
      this.parsed = true;
    }
  }

  public String getBoundary()
  {
    return getParameter("boundary");
  }

  public String getCharset()
  {
    return getParameter("charset");
  }

  public String getMimeType()
  {
    if (!this.parsed)
      parse();
    return this.mimeType;
  }

  public String getParameter(String paramString)
  {
    if (!this.parsed)
      parse();
    return (String)this.parameters.get(paramString.toLowerCase());
  }

  public Map<String, String> getParameters()
  {
    if (!this.parsed)
      parse();
    return Collections.unmodifiableMap(this.parameters);
  }

  public ParseException getParseException()
  {
    if (!this.parsed)
      parse();
    return this.parseException;
  }

  public boolean isMimeType(String paramString)
  {
    if (!this.parsed)
      parse();
    return this.mimeType.equalsIgnoreCase(paramString);
  }

  public boolean isMultipart()
  {
    if (!this.parsed)
      parse();
    return this.mimeType.startsWith("multipart/");
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.ContentTypeField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */