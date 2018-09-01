package com.nubee.coinpirates.common;

import android.util.Log;
import android.util.Xml;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlParser
{
  public static final String CODE = "code";
  public static final String ERRORMESSAGE = "errorMessage";
  public static final int RESCODE_DONE = 0;
  public static final String RESPONSECODE = "responseCode";
  public static final String RESPONSEDATA = "responseData";
  protected XmlPullParser BasicParser = Xml.newPullParser();
  private BufferedReader bufferdReaderResponse;
  private String code = "-1";
  private String errorMessage;
  private boolean responseCode = false;
  protected boolean responseData = false;

  public XmlParser(BufferedReader paramBufferedReader)
    throws XmlPullParserException
  {
    this.bufferdReaderResponse = paramBufferedReader;
  }

  public XmlParser(InputStream paramInputStream)
  {
    this.bufferdReaderResponse = new BufferedReader(new InputStreamReader(paramInputStream));
  }

  public void BasicParse()
  {
    int i;
    try
    {
      this.BasicParser.setInput(this.bufferdReaderResponse);
      i = this.BasicParser.getEventType();
      j = 0;
      break label186;
      while (true)
      {
        i = this.BasicParser.next();
        break label186;
        str = this.BasicParser.getName();
        if (!str.equalsIgnoreCase("responseCode"))
          break;
        this.responseCode = true;
      }
    }
    catch (Exception localException)
    {
      Log.e("SNS::XmlParser", localException.getMessage(), localException);
      throw new RuntimeException(localException);
    }
    int j;
    while (true)
    {
      String str;
      if (this.responseCode)
        if (str.equalsIgnoreCase("code"))
        {
          this.code = this.BasicParser.nextText();
        }
        else if (str.equalsIgnoreCase("errorMessage"))
        {
          this.errorMessage = this.BasicParser.nextText();
          continue;
          if ((this.BasicParser.getName().equalsIgnoreCase("responseCode")) && (this.responseCode))
          {
            this.responseCode = false;
            j = 1;
          }
        }
    }
    label186: if ((i == 1) || (j != 0))
      return;
    switch (i)
    {
    case 0:
    case 1:
    default:
    case 2:
    case 3:
    }
  }

  public int getCODE()
  {
    return Integer.parseInt(this.code);
  }

  public String getERRORMESSAGE()
  {
    return this.errorMessage;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.XmlParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */