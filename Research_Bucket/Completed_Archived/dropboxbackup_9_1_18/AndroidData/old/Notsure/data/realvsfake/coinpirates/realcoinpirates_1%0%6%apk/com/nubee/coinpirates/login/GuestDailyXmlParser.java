package com.nubee.coinpirates.login;

import com.nubee.coinpirates.common.Coins7Log;
import com.nubee.coinpirates.common.NubeeXmlParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GuestDailyXmlParser extends NubeeXmlParser
{
  private static final String TAG_DAILYBONUS = "bonus";
  private static final String TAG_FIRSTBONUS = "firstLoginBonus";
  private static final String TAG_TOTALCOINS = "totalChip";
  private String dailyBonus;
  private String firstLoginBonus;
  private String totalCoins;

  public GuestDailyXmlParser(BufferedReader paramBufferedReader)
    throws XmlPullParserException
  {
    super(paramBufferedReader);
  }

  public GuestDailyXmlParser(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  public String getDailyBonus()
  {
    return this.dailyBonus;
  }

  public String getFirstLoginBonus()
  {
    return this.firstLoginBonus;
  }

  public String getTotalCoins()
  {
    return this.totalCoins;
  }

  public void parse()
  {
    while (true)
    {
      try
      {
        this.BasicParser.getEventType();
        int i = this.BasicParser.next();
        if (i == 1)
          return;
        str = this.BasicParser.getName();
        switch (i)
        {
        case 0:
        case 2:
          if ("totalChip".equalsIgnoreCase(str))
          {
            this.totalCoins = this.BasicParser.nextText();
            continue;
          }
        case 1:
        }
      }
      catch (XmlPullParserException localXmlPullParserException)
      {
        Coins7Log.e("AndroidNews::PullFeedParser", localXmlPullParserException.getMessage(), localXmlPullParserException);
        throw new RuntimeException(localXmlPullParserException);
        if ("bonus".equalsIgnoreCase(str))
        {
          this.dailyBonus = this.BasicParser.nextText();
          continue;
        }
      }
      catch (IOException localIOException)
      {
        Coins7Log.e("AndroidNews::PullFeedParser", localIOException.getMessage(), localIOException);
        throw new RuntimeException(localIOException);
      }
      String str;
      if ("firstLoginBonus".equalsIgnoreCase(str))
        this.firstLoginBonus = this.BasicParser.nextText();
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.login.GuestDailyXmlParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */