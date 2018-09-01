package com.nubee.coinpirates.animation;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Image
{
  private static final String attr_name_id = "id";
  private static final String attr_name_src = "src";
  private Bitmap mBitmap;
  private String mId;

  public Image(Context paramContext, XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    int i = 0;
    if (i >= paramXmlPullParser.getAttributeCount())
    {
      while (paramXmlPullParser.next() != 3);
      return;
    }
    String str = paramXmlPullParser.getAttributeName(i).toLowerCase();
    if (str.equals("src"))
      if ((paramXmlPullParser instanceof XmlResourceParser))
      {
        XmlResourceParser localXmlResourceParser = (XmlResourceParser)paramXmlPullParser;
        if (localXmlResourceParser != null)
        {
          int j = localXmlResourceParser.getAttributeResourceValue(i, 0);
          this.mBitmap = BitmapFactory.decodeResource(paramContext.getResources(), j);
        }
      }
    while (true)
    {
      i++;
      break;
      if (str.equals("id"))
        this.mId = paramXmlPullParser.getAttributeValue(i);
    }
  }

  public void draw(Canvas paramCanvas, Paint paramPaint)
  {
    if (this.mBitmap != null)
      paramCanvas.drawBitmap(this.mBitmap, 0.0F, 0.0F, paramPaint);
  }

  public int getHeight()
  {
    if (this.mBitmap == null)
      return 0;
    return this.mBitmap.getHeight();
  }

  public String getId()
  {
    return this.mId;
  }

  public int getWidth()
  {
    if (this.mBitmap == null)
      return 0;
    return this.mBitmap.getWidth();
  }

  public void recycle()
  {
    if (this.mBitmap != null)
    {
      this.mBitmap.recycle();
      this.mBitmap = null;
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Image
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */