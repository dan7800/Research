package org.apache.james.mime4j.descriptor;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.MimeUtil;

public class DefaultBodyDescriptor
  implements MutableBodyDescriptor
{
  private static final String DEFAULT_MEDIA_TYPE = "text";
  private static final String DEFAULT_MIME_TYPE = "text/plain";
  private static final String DEFAULT_SUB_TYPE = "plain";
  private static final String EMAIL_MESSAGE_MIME_TYPE = "message/rfc822";
  private static final String MEDIA_TYPE_MESSAGE = "message";
  private static final String MEDIA_TYPE_TEXT = "text";
  private static final String SUB_TYPE_EMAIL = "rfc822";
  private static final String US_ASCII = "us-ascii";
  private static Log log = LogFactory.getLog(DefaultBodyDescriptor.class);
  private String boundary = null;
  private String charset = "us-ascii";
  private long contentLength = -1L;
  private boolean contentTransferEncSet;
  private boolean contentTypeSet;
  private String mediaType = "text";
  private String mimeType = "text/plain";
  private Map<String, String> parameters = new HashMap();
  private String subType = "plain";
  private String transferEncoding = "7bit";

  public DefaultBodyDescriptor()
  {
    this(null);
  }

  public DefaultBodyDescriptor(BodyDescriptor paramBodyDescriptor)
  {
    if ((paramBodyDescriptor != null) && (MimeUtil.isSameMimeType("multipart/digest", paramBodyDescriptor.getMimeType())))
    {
      this.mimeType = "message/rfc822";
      this.subType = "rfc822";
      this.mediaType = "message";
      return;
    }
    this.mimeType = "text/plain";
    this.subType = "plain";
    this.mediaType = "text";
  }

  private void parseContentType(String paramString)
  {
    this.contentTypeSet = true;
    Map localMap = MimeUtil.getHeaderParams(paramString);
    String str1 = (String)localMap.get("");
    String str2 = null;
    String str3 = null;
    if (str1 != null)
    {
      str1 = str1.toLowerCase().trim();
      int i = str1.indexOf('/');
      str2 = null;
      str3 = null;
      int j = 0;
      if (i != -1)
      {
        str3 = str1.substring(0, i).trim();
        str2 = str1.substring(i + 1).trim();
        int k = str3.length();
        j = 0;
        if (k > 0)
        {
          int m = str2.length();
          j = 0;
          if (m > 0)
          {
            str1 = str3 + "/" + str2;
            j = 1;
          }
        }
      }
      if (j == 0)
      {
        str1 = null;
        str3 = null;
        str2 = null;
      }
    }
    String str4 = (String)localMap.get("boundary");
    if ((str1 != null) && (((str1.startsWith("multipart/")) && (str4 != null)) || (!str1.startsWith("multipart/"))))
    {
      this.mimeType = str1;
      this.subType = str2;
      this.mediaType = str3;
    }
    if (MimeUtil.isMultipart(this.mimeType))
      this.boundary = str4;
    String str5 = (String)localMap.get("charset");
    this.charset = null;
    if (str5 != null)
    {
      String str6 = str5.trim();
      if (str6.length() > 0)
        this.charset = str6.toLowerCase();
    }
    if ((this.charset == null) && ("text".equals(this.mediaType)))
      this.charset = "us-ascii";
    this.parameters.putAll(localMap);
    this.parameters.remove("");
    this.parameters.remove("boundary");
    this.parameters.remove("charset");
  }

  public void addField(Field paramField)
  {
    String str1 = paramField.getName();
    String str2 = paramField.getBody();
    String str3 = str1.trim().toLowerCase();
    if ((str3.equals("content-transfer-encoding")) && (!this.contentTransferEncSet))
    {
      this.contentTransferEncSet = true;
      String str4 = str2.trim().toLowerCase();
      if (str4.length() > 0)
        this.transferEncoding = str4;
    }
    do
    {
      return;
      if ((str3.equals("content-length")) && (this.contentLength == -1L))
        try
        {
          this.contentLength = Long.parseLong(str2.trim());
          return;
        }
        catch (NumberFormatException localNumberFormatException)
        {
          log.error("Invalid content-length: " + str2);
          return;
        }
    }
    while ((!str3.equals("content-type")) || (this.contentTypeSet));
    parseContentType(str2);
  }

  public String getBoundary()
  {
    return this.boundary;
  }

  public String getCharset()
  {
    return this.charset;
  }

  public long getContentLength()
  {
    return this.contentLength;
  }

  public Map<String, String> getContentTypeParameters()
  {
    return this.parameters;
  }

  public String getMediaType()
  {
    return this.mediaType;
  }

  public String getMimeType()
  {
    return this.mimeType;
  }

  public String getSubType()
  {
    return this.subType;
  }

  public String getTransferEncoding()
  {
    return this.transferEncoding;
  }

  public String toString()
  {
    return this.mimeType;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.descriptor.DefaultBodyDescriptor
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */