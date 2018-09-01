package com.nubee.coinpirates.login;

import com.nubee.coinpirates.common.Coins7Log;
import com.nubee.coinpirates.common.NubeeXmlParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GuestRegistXmlParser extends NubeeXmlParser
{
  private static final String TAG_ID = "Id";
  private String id;

  public GuestRegistXmlParser(BufferedReader paramBufferedReader)
    throws XmlPullParserException
  {
    super(paramBufferedReader);
  }

  public GuestRegistXmlParser(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  public String getId()
  {
    return this.id;
  }

  public void parse()
  {
    while (true)
      try
      {
        this.BasicParser.getEventType();
        int i = this.BasicParser.next();
        if (i == 1)
          return;
        String str = this.BasicParser.getName();
        switch (i)
        {
        case 0:
        case 2:
          if ("Id".equalsIgnoreCase(str))
            this.id = this.BasicParser.nextText();
        case 1:
        }
      }
      catch (XmlPullParserException localXmlPullParserException)
      {
        Coins7Log.e("AndroidNews::PullFeedParser", localXmlPullParserException.getMessage(), localXmlPullParserException);
        throw new RuntimeException(localXmlPullParserException);
      }
      catch (IOException localIOException)
      {
        Coins7Log.e("AndroidNews::PullFeedParser", localIOException.getMessage(), localIOException);
        throw new RuntimeException(localIOException);
      }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.login.GuestRegistXmlParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */