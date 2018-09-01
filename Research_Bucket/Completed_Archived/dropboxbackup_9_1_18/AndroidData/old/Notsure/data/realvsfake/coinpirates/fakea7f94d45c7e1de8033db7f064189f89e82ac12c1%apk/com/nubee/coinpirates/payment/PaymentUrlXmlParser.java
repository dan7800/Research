package com.nubee.coinpirates.payment;

import android.util.Log;
import com.nubee.coinpirates.common.XmlParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PaymentUrlXmlParser extends XmlParser
{
  private static final String TAG_PAYMENT_KEY = "payment_key";
  private static final String TAG_URL = "url";
  private PaymentURLEntity result;

  public PaymentUrlXmlParser(BufferedReader paramBufferedReader)
    throws XmlPullParserException
  {
    super(paramBufferedReader);
  }

  public PaymentUrlXmlParser(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  private boolean readEntityField(String paramString, PaymentURLEntity paramPaymentURLEntity)
    throws IOException, XmlPullParserException
  {
    if ("payment_key".equalsIgnoreCase(paramString))
      paramPaymentURLEntity.setPaymentKey(this.BasicParser.nextText());
    while (true)
    {
      return true;
      if (!"url".equalsIgnoreCase(paramString))
        break;
      paramPaymentURLEntity.setPaymentUrl(this.BasicParser.nextText());
    }
    return false;
  }

  public PaymentURLEntity getResult()
  {
    return this.result;
  }

  public void parse()
  {
    while (true)
      try
      {
        this.BasicParser.getEventType();
        this.result = new PaymentURLEntity();
        int i = this.BasicParser.next();
        if (i == 1)
          return;
        String str = this.BasicParser.getName();
        switch (i)
        {
        case 0:
        case 3:
        case 2:
          if (!readEntityField(str, this.result))
            Log.w("PaymentUrlXmlParser", "Not Read Field=" + str);
        case 1:
        }
      }
      catch (XmlPullParserException localXmlPullParserException)
      {
        Log.e("AndroidNews::PullFeedParser", localXmlPullParserException.getMessage(), localXmlPullParserException);
        throw new RuntimeException(localXmlPullParserException);
      }
      catch (IOException localIOException)
      {
        Log.e("AndroidNews::PullFeedParser", localIOException.getMessage(), localIOException);
        throw new RuntimeException(localIOException);
      }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentUrlXmlParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */