package com.nubee.coinpirates.payment;

import android.util.Log;
import com.nubee.coinpirates.common.XmlParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PaymentInfoXmlParser extends XmlParser
{
  private static final String TAG_ITEM_TYPE = "item_type";
  private static final String TAG_QUANTITY = "quantity";
  private PaymentInfoEntity result;

  public PaymentInfoXmlParser(BufferedReader paramBufferedReader)
    throws XmlPullParserException
  {
    super(paramBufferedReader);
  }

  public PaymentInfoXmlParser(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  private boolean readEntityField(String paramString, PaymentInfoEntity paramPaymentInfoEntity)
    throws IOException, XmlPullParserException
  {
    if ("quantity".equalsIgnoreCase(paramString))
      paramPaymentInfoEntity.setQuantity(this.BasicParser.nextText());
    while (true)
    {
      return true;
      if (!"item_type".equalsIgnoreCase(paramString))
        break;
      paramPaymentInfoEntity.setItemType(this.BasicParser.nextText());
    }
    return false;
  }

  public PaymentInfoEntity getResult()
  {
    return this.result;
  }

  public void parse()
  {
    while (true)
      try
      {
        this.BasicParser.getEventType();
        this.result = new PaymentInfoEntity();
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
            Log.w("PaymentInfoXmlParser", "Not Read Field=" + str);
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
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentInfoXmlParser
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */