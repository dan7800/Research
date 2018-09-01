package org.apache.james.mime4j.field;

import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.contentdisposition.parser.ContentDispositionParser;
import org.apache.james.mime4j.field.datetime.DateTime;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.util.ByteSequence;

public class ContentDispositionField extends AbstractField
{
  public static final String DISPOSITION_TYPE_ATTACHMENT = "attachment";
  public static final String DISPOSITION_TYPE_INLINE = "inline";
  public static final String PARAM_CREATION_DATE = "creation-date";
  public static final String PARAM_FILENAME = "filename";
  public static final String PARAM_MODIFICATION_DATE = "modification-date";
  public static final String PARAM_READ_DATE = "read-date";
  public static final String PARAM_SIZE = "size";
  static final FieldParser PARSER = new FieldParser()
  {
    public ParsedField parse(String paramAnonymousString1, String paramAnonymousString2, ByteSequence paramAnonymousByteSequence)
    {
      return new ContentDispositionField(paramAnonymousString1, paramAnonymousString2, paramAnonymousByteSequence);
    }
  };
  private static Log log = LogFactory.getLog(ContentDispositionField.class);
  private Date creationDate;
  private boolean creationDateParsed;
  private String dispositionType = "";
  private Date modificationDate;
  private boolean modificationDateParsed;
  private Map<String, String> parameters = new HashMap();
  private ParseException parseException;
  private boolean parsed = false;
  private Date readDate;
  private boolean readDateParsed;

  ContentDispositionField(String paramString1, String paramString2, ByteSequence paramByteSequence)
  {
    super(paramString1, paramString2, paramByteSequence);
  }

  private void parse()
  {
    String str1 = getBody();
    ContentDispositionParser localContentDispositionParser = new ContentDispositionParser(new StringReader(str1));
    try
    {
      localContentDispositionParser.parseAll();
      String str2 = localContentDispositionParser.getDispositionType();
      if (str2 != null)
      {
        this.dispositionType = str2.toLowerCase(Locale.US);
        List localList1 = localContentDispositionParser.getParamNames();
        List localList2 = localContentDispositionParser.getParamValues();
        if ((localList1 != null) && (localList2 != null))
        {
          int i = Math.min(localList1.size(), localList2.size());
          for (int j = 0; j < i; j++)
          {
            String str3 = ((String)localList1.get(j)).toLowerCase(Locale.US);
            String str4 = (String)localList2.get(j);
            this.parameters.put(str3, str4);
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
    catch (org.apache.james.mime4j.field.contentdisposition.parser.TokenMgrError localTokenMgrError)
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

  private Date parseDate(String paramString)
  {
    String str = getParameter(paramString);
    if (str == null)
    {
      if (log.isDebugEnabled())
        log.debug("Parsing " + paramString + " null");
      return null;
    }
    try
    {
      Date localDate = new DateTimeParser(new StringReader(str)).parseAll().getDate();
      return localDate;
    }
    catch (ParseException localParseException)
    {
      if (log.isDebugEnabled())
        log.debug("Parsing " + paramString + " '" + str + "': " + localParseException.getMessage());
      return null;
    }
    catch (org.apache.james.mime4j.field.datetime.parser.TokenMgrError localTokenMgrError)
    {
      if (log.isDebugEnabled())
        log.debug("Parsing " + paramString + " '" + str + "': " + localTokenMgrError.getMessage());
    }
    return null;
  }

  public Date getCreationDate()
  {
    if (!this.creationDateParsed)
    {
      this.creationDate = parseDate("creation-date");
      this.creationDateParsed = true;
    }
    return this.creationDate;
  }

  public String getDispositionType()
  {
    if (!this.parsed)
      parse();
    return this.dispositionType;
  }

  public String getFilename()
  {
    return getParameter("filename");
  }

  public Date getModificationDate()
  {
    if (!this.modificationDateParsed)
    {
      this.modificationDate = parseDate("modification-date");
      this.modificationDateParsed = true;
    }
    return this.modificationDate;
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

  public Date getReadDate()
  {
    if (!this.readDateParsed)
    {
      this.readDate = parseDate("read-date");
      this.readDateParsed = true;
    }
    return this.readDate;
  }

  public long getSize()
  {
    String str = getParameter("size");
    if (str == null)
      return -1L;
    try
    {
      long l = Long.parseLong(str);
      if (l < 0L)
        return -1L;
      return l;
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    return -1L;
  }

  public boolean isAttachment()
  {
    if (!this.parsed)
      parse();
    return this.dispositionType.equals("attachment");
  }

  public boolean isDispositionType(String paramString)
  {
    if (!this.parsed)
      parse();
    return this.dispositionType.equalsIgnoreCase(paramString);
  }

  public boolean isInline()
  {
    if (!this.parsed)
      parse();
    return this.dispositionType.equals("inline");
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.ContentDispositionField
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */