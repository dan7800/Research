package org.apache.james.mime4j.descriptor;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.field.datetime.DateTime;
import org.apache.james.mime4j.field.datetime.parser.DateTimeParser;
import org.apache.james.mime4j.field.datetime.parser.ParseException;
import org.apache.james.mime4j.field.language.parser.ContentLanguageParser;
import org.apache.james.mime4j.field.mimeversion.parser.MimeVersionParser;
import org.apache.james.mime4j.field.structured.parser.StructuredFieldParser;
import org.apache.james.mime4j.parser.Field;

public class MaximalBodyDescriptor extends DefaultBodyDescriptor
{
  private static final int DEFAULT_MAJOR_VERSION = 1;
  private static final int DEFAULT_MINOR_VERSION;
  private String contentDescription = null;
  private DateTime contentDispositionCreationDate = null;
  private MimeException contentDispositionCreationDateParseException = null;
  private DateTime contentDispositionModificationDate = null;
  private MimeException contentDispositionModificationDateParseException = null;
  private Map<String, String> contentDispositionParameters = Collections.emptyMap();
  private DateTime contentDispositionReadDate = null;
  private MimeException contentDispositionReadDateParseException = null;
  private long contentDispositionSize = -1L;
  private MimeException contentDispositionSizeParseException = null;
  private String contentDispositionType = null;
  private String contentId = null;
  private List<String> contentLanguage = null;
  private MimeException contentLanguageParseException = null;
  private String contentLocation = null;
  private MimeException contentLocationParseException = null;
  private String contentMD5Raw = null;
  private boolean isContentDescriptionSet = false;
  private boolean isContentDispositionSet = false;
  private boolean isContentIdSet = false;
  private boolean isContentLanguageSet;
  private boolean isContentLocationSet = false;
  private boolean isContentMD5Set = false;
  private boolean isMimeVersionSet = false;
  private int mimeMajorVersion = 1;
  private int mimeMinorVersion = 0;
  private MimeException mimeVersionException;

  protected MaximalBodyDescriptor()
  {
    this(null);
  }

  public MaximalBodyDescriptor(BodyDescriptor paramBodyDescriptor)
  {
    super(paramBodyDescriptor);
  }

  private void parseContentDescription(String paramString)
  {
    if (paramString == null);
    for (this.contentDescription = ""; ; this.contentDescription = paramString.trim())
    {
      this.isContentDescriptionSet = true;
      return;
    }
  }

  // ERROR //
  private void parseContentDisposition(String paramString)
  {
    // Byte code:
    //   0: aload_0
    //   1: iconst_1
    //   2: putfield 94	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:isContentDispositionSet	Z
    //   5: aload_0
    //   6: aload_1
    //   7: invokestatic 129	org/apache/james/mime4j/util/MimeUtil:getHeaderParams	(Ljava/lang/String;)Ljava/util/Map;
    //   10: putfield 74	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionParameters	Ljava/util/Map;
    //   13: aload_0
    //   14: aload_0
    //   15: getfield 74	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionParameters	Ljava/util/Map;
    //   18: ldc 112
    //   20: invokeinterface 135 2 0
    //   25: checkcast 114	java/lang/String
    //   28: putfield 66	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionType	Ljava/lang/String;
    //   31: aload_0
    //   32: getfield 74	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionParameters	Ljava/util/Map;
    //   35: ldc 137
    //   37: invokeinterface 135 2 0
    //   42: checkcast 114	java/lang/String
    //   45: astore_2
    //   46: aload_2
    //   47: ifnull +12 -> 59
    //   50: aload_0
    //   51: aload_0
    //   52: aload_2
    //   53: invokespecial 141	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:parseDate	(Ljava/lang/String;)Lorg/apache/james/mime4j/field/datetime/DateTime;
    //   56: putfield 76	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionModificationDate	Lorg/apache/james/mime4j/field/datetime/DateTime;
    //   59: aload_0
    //   60: getfield 74	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionParameters	Ljava/util/Map;
    //   63: ldc 143
    //   65: invokeinterface 135 2 0
    //   70: checkcast 114	java/lang/String
    //   73: astore_3
    //   74: aload_3
    //   75: ifnull +12 -> 87
    //   78: aload_0
    //   79: aload_0
    //   80: aload_3
    //   81: invokespecial 141	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:parseDate	(Ljava/lang/String;)Lorg/apache/james/mime4j/field/datetime/DateTime;
    //   84: putfield 80	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionCreationDate	Lorg/apache/james/mime4j/field/datetime/DateTime;
    //   87: aload_0
    //   88: getfield 74	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionParameters	Ljava/util/Map;
    //   91: ldc 145
    //   93: invokeinterface 135 2 0
    //   98: checkcast 114	java/lang/String
    //   101: astore 4
    //   103: aload 4
    //   105: ifnull +13 -> 118
    //   108: aload_0
    //   109: aload_0
    //   110: aload 4
    //   112: invokespecial 141	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:parseDate	(Ljava/lang/String;)Lorg/apache/james/mime4j/field/datetime/DateTime;
    //   115: putfield 84	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionReadDate	Lorg/apache/james/mime4j/field/datetime/DateTime;
    //   118: aload_0
    //   119: getfield 74	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionParameters	Ljava/util/Map;
    //   122: ldc 147
    //   124: invokeinterface 135 2 0
    //   129: checkcast 114	java/lang/String
    //   132: astore 5
    //   134: aload 5
    //   136: ifnull +12 -> 148
    //   139: aload_0
    //   140: aload 5
    //   142: invokestatic 153	java/lang/Long:parseLong	(Ljava/lang/String;)J
    //   145: putfield 90	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionSize	J
    //   148: aload_0
    //   149: getfield 74	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionParameters	Ljava/util/Map;
    //   152: ldc 112
    //   154: invokeinterface 156 2 0
    //   159: pop
    //   160: return
    //   161: astore 10
    //   163: aload_0
    //   164: aload 10
    //   166: putfield 78	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionModificationDateParseException	Lorg/apache/james/mime4j/MimeException;
    //   169: goto -110 -> 59
    //   172: astore 9
    //   174: aload_0
    //   175: aload 9
    //   177: putfield 82	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionCreationDateParseException	Lorg/apache/james/mime4j/MimeException;
    //   180: goto -93 -> 87
    //   183: astore 8
    //   185: aload_0
    //   186: aload 8
    //   188: putfield 86	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionReadDateParseException	Lorg/apache/james/mime4j/MimeException;
    //   191: goto -73 -> 118
    //   194: astore 7
    //   196: aload_0
    //   197: new 158	org/apache/james/mime4j/MimeException
    //   200: dup
    //   201: aload 7
    //   203: invokevirtual 161	java/lang/NumberFormatException:getMessage	()Ljava/lang/String;
    //   206: aload 7
    //   208: invokespecial 164	org/apache/james/mime4j/MimeException:<init>	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   211: invokevirtual 168	org/apache/james/mime4j/MimeException:fillInStackTrace	()Ljava/lang/Throwable;
    //   214: checkcast 158	org/apache/james/mime4j/MimeException
    //   217: putfield 92	org/apache/james/mime4j/descriptor/MaximalBodyDescriptor:contentDispositionSizeParseException	Lorg/apache/james/mime4j/MimeException;
    //   220: goto -72 -> 148
    //
    // Exception table:
    //   from	to	target	type
    //   50	59	161	org/apache/james/mime4j/field/datetime/parser/ParseException
    //   78	87	172	org/apache/james/mime4j/field/datetime/parser/ParseException
    //   108	118	183	org/apache/james/mime4j/field/datetime/parser/ParseException
    //   139	148	194	java/lang/NumberFormatException
  }

  private void parseContentId(String paramString)
  {
    if (paramString == null);
    for (this.contentId = ""; ; this.contentId = paramString.trim())
    {
      this.isContentIdSet = true;
      return;
    }
  }

  private DateTime parseDate(String paramString)
    throws ParseException
  {
    return new DateTimeParser(new StringReader(paramString)).date_time();
  }

  private void parseLanguage(String paramString)
  {
    this.isContentLanguageSet = true;
    if (paramString != null);
    try
    {
      this.contentLanguage = new ContentLanguageParser(new StringReader(paramString)).parse();
      return;
    }
    catch (MimeException localMimeException)
    {
      this.contentLanguageParseException = localMimeException;
    }
  }

  private void parseLocation(String paramString)
  {
    this.isContentLocationSet = true;
    StructuredFieldParser localStructuredFieldParser;
    if (paramString != null)
    {
      localStructuredFieldParser = new StructuredFieldParser(new StringReader(paramString));
      localStructuredFieldParser.setFoldingPreserved(false);
    }
    try
    {
      this.contentLocation = localStructuredFieldParser.parse();
      return;
    }
    catch (MimeException localMimeException)
    {
      this.contentLocationParseException = localMimeException;
    }
  }

  private void parseMD5(String paramString)
  {
    this.isContentMD5Set = true;
    if (paramString != null)
      this.contentMD5Raw = paramString.trim();
  }

  private void parseMimeVersion(String paramString)
  {
    localMimeVersionParser = new MimeVersionParser(new StringReader(paramString));
    try
    {
      localMimeVersionParser.parse();
      int i = localMimeVersionParser.getMajorVersion();
      if (i != -1)
        this.mimeMajorVersion = i;
      int j = localMimeVersionParser.getMinorVersion();
      if (j != -1)
        this.mimeMinorVersion = j;
      this.isMimeVersionSet = true;
      return;
    }
    catch (MimeException localMimeException)
    {
      while (true)
        this.mimeVersionException = localMimeException;
    }
  }

  public void addField(Field paramField)
  {
    String str1 = paramField.getName();
    String str2 = paramField.getBody();
    String str3 = str1.trim().toLowerCase();
    if (("mime-version".equals(str3)) && (!this.isMimeVersionSet))
    {
      parseMimeVersion(str2);
      return;
    }
    if (("content-id".equals(str3)) && (!this.isContentIdSet))
    {
      parseContentId(str2);
      return;
    }
    if (("content-description".equals(str3)) && (!this.isContentDescriptionSet))
    {
      parseContentDescription(str2);
      return;
    }
    if (("content-disposition".equals(str3)) && (!this.isContentDispositionSet))
    {
      parseContentDisposition(str2);
      return;
    }
    if (("content-language".equals(str3)) && (!this.isContentLanguageSet))
    {
      parseLanguage(str2);
      return;
    }
    if (("content-location".equals(str3)) && (!this.isContentLocationSet))
    {
      parseLocation(str2);
      return;
    }
    if (("content-md5".equals(str3)) && (!this.isContentMD5Set))
    {
      parseMD5(str2);
      return;
    }
    super.addField(paramField);
  }

  public String getContentDescription()
  {
    return this.contentDescription;
  }

  public DateTime getContentDispositionCreationDate()
  {
    return this.contentDispositionCreationDate;
  }

  public MimeException getContentDispositionCreationDateParseException()
  {
    return this.contentDispositionCreationDateParseException;
  }

  public String getContentDispositionFilename()
  {
    return (String)this.contentDispositionParameters.get("filename");
  }

  public DateTime getContentDispositionModificationDate()
  {
    return this.contentDispositionModificationDate;
  }

  public MimeException getContentDispositionModificationDateParseException()
  {
    return this.contentDispositionModificationDateParseException;
  }

  public Map<String, String> getContentDispositionParameters()
  {
    return this.contentDispositionParameters;
  }

  public DateTime getContentDispositionReadDate()
  {
    return this.contentDispositionReadDate;
  }

  public MimeException getContentDispositionReadDateParseException()
  {
    return this.contentDispositionReadDateParseException;
  }

  public long getContentDispositionSize()
  {
    return this.contentDispositionSize;
  }

  public MimeException getContentDispositionSizeParseException()
  {
    return this.contentDispositionSizeParseException;
  }

  public String getContentDispositionType()
  {
    return this.contentDispositionType;
  }

  public String getContentId()
  {
    return this.contentId;
  }

  public List<String> getContentLanguage()
  {
    return this.contentLanguage;
  }

  public MimeException getContentLanguageParseException()
  {
    return this.contentLanguageParseException;
  }

  public String getContentLocation()
  {
    return this.contentLocation;
  }

  public MimeException getContentLocationParseException()
  {
    return this.contentLocationParseException;
  }

  public String getContentMD5Raw()
  {
    return this.contentMD5Raw;
  }

  public int getMimeMajorVersion()
  {
    return this.mimeMajorVersion;
  }

  public int getMimeMinorVersion()
  {
    return this.mimeMinorVersion;
  }

  public MimeException getMimeVersionParseException()
  {
    return this.mimeVersionException;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.descriptor.MaximalBodyDescriptor
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */