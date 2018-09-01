package com.nubee.coinpirates.animation;

import android.graphics.PointF;
import org.xmlpull.v1.XmlPullParser;

class Random extends Animator
{
  private static final String attr_name_max_x = "maxx";
  private static final String attr_name_max_y = "maxy";
  private static final String attr_name_min_x = "minx";
  private static final String attr_name_min_y = "miny";
  private final PointF mMax = new PointF();
  private final PointF mMin = new PointF();
  private final PointF mPosition = new PointF();
  private final PointF mPositionDf = new PointF();

  public Random(XmlPullParser paramXmlPullParser, Part paramPart)
  {
    this.mTarget = paramPart;
    PointF localPointF1 = this.mTarget.getPosition();
    PointF localPointF2 = this.mPosition;
    PointF localPointF3 = this.mMax;
    PointF localPointF4 = this.mMin;
    float f1 = localPointF1.x;
    localPointF4.x = f1;
    localPointF3.x = f1;
    localPointF2.x = f1;
    PointF localPointF5 = this.mPosition;
    PointF localPointF6 = this.mMax;
    PointF localPointF7 = this.mMin;
    float f2 = localPointF1.y;
    localPointF7.y = f2;
    localPointF6.y = f2;
    localPointF5.y = f2;
    for (int i = 0; ; i++)
    {
      if (i >= paramXmlPullParser.getAttributeCount())
        return;
      setAttributeValue(paramXmlPullParser.getAttributeName(i), paramXmlPullParser.getAttributeValue(i));
    }
  }

  public boolean animation(int paramInt)
  {
    super.animation(paramInt);
    float f1;
    if (paramInt == this.mStartTime)
    {
      PointF localPointF3 = this.mTarget.getPosition();
      this.mPosition.x = localPointF3.x;
      this.mPosition.y = localPointF3.y;
      if (this.mEndTime - this.mStartTime > 0)
      {
        if (this.mMax.x - this.mMin.x == 0.0F)
          break label334;
        float f2 = (float)(Math.random() * this.mMax.x - this.mMin.x + this.mMin.x);
        this.mPositionDf.x = ((f2 - this.mPosition.x) / this.mEndTime - this.mStartTime);
        if (this.mMax.y - this.mMin.y == 0.0F)
          break label345;
        f1 = (float)(Math.random() * this.mMax.y - this.mMin.y + this.mMin.y);
      }
    }
    label334: label345: for (this.mPositionDf.y = ((f1 - this.mPosition.y) / this.mEndTime - this.mStartTime); ; this.mPositionDf.y = 0.0F)
    {
      if ((this.mStartTime <= paramInt) && (paramInt <= this.mEndTime))
      {
        if (this.mPositionDf.x != 0.0F)
        {
          PointF localPointF2 = this.mPosition;
          localPointF2.x += this.mPositionDf.x;
          this.mTarget.setX(this.mPosition.x);
        }
        if (this.mPositionDf.y != 0.0F)
        {
          PointF localPointF1 = this.mPosition;
          localPointF1.y += this.mPositionDf.y;
          this.mTarget.setY(this.mPosition.y);
        }
      }
      return true;
      this.mPositionDf.x = 0.0F;
      break;
    }
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if (str2 == null)
    {
      if (str1.equals("maxx"))
        return this.mMax.x;
      if (str1.equals("minx"))
        return this.mMin.x;
      if (str1.equals("maxy"))
        return this.mMax.y;
      if (str1.equals("miny"))
        return this.mMin.y;
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("maxx"))
    {
      this.mMax.x = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    if (str.equals("minx"))
    {
      this.mMin.x = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    if (str.equals("maxy"))
    {
      this.mMax.y = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    if (str.equals("miny"))
    {
      this.mMin.y = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Random
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */