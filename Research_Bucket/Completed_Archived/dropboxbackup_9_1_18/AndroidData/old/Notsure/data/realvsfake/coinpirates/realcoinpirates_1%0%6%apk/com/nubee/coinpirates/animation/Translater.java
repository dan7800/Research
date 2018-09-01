package com.nubee.coinpirates.animation;

import android.graphics.PointF;
import org.xmlpull.v1.XmlPullParser;

class Translater extends Animator
{
  private static final String attr_name_endx = "endx";
  private static final String attr_name_endy = "endy";
  private static final String attr_name_startx = "startx";
  private static final String attr_name_starty = "starty";
  private final PointF mEndPosition = new PointF();
  private final PointF mPosition = new PointF();
  private final PointF mPositionDf = new PointF();
  private final PointF mStartPosition = new PointF();

  public Translater(XmlPullParser paramXmlPullParser, Part paramPart)
  {
    this.mTarget = paramPart;
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
    if (paramInt == this.mStartTime)
    {
      this.mPosition.x = this.mStartPosition.x;
      this.mPosition.y = this.mStartPosition.y;
      if (this.mEndTime - this.mStartTime > 0)
      {
        this.mPositionDf.x = ((this.mEndPosition.x - this.mStartPosition.x) / this.mEndTime - this.mStartTime);
        this.mPositionDf.y = ((this.mEndPosition.y - this.mStartPosition.y) / this.mEndTime - this.mStartTime);
      }
    }
    if ((this.mStartTime <= paramInt) && (paramInt < this.mEndTime))
    {
      if (this.mPositionDf.x != 0.0F)
      {
        this.mTarget.setX(this.mPosition.x);
        PointF localPointF4 = this.mPosition;
        localPointF4.x += this.mPositionDf.x;
      }
      if (this.mPositionDf.y != 0.0F)
      {
        this.mTarget.setY(this.mPosition.y);
        PointF localPointF3 = this.mPosition;
        localPointF3.y += this.mPositionDf.y;
      }
    }
    if (paramInt == this.mEndTime)
    {
      if (this.mPositionDf.x != 0.0F)
      {
        Part localPart2 = this.mTarget;
        PointF localPointF2 = this.mPosition;
        float f2 = this.mEndPosition.x;
        localPointF2.x = f2;
        localPart2.setX(f2);
      }
      if (this.mPositionDf.y != 0.0F)
      {
        Part localPart1 = this.mTarget;
        PointF localPointF1 = this.mPosition;
        float f1 = this.mEndPosition.y;
        localPointF1.y = f1;
        localPart1.setY(f1);
      }
    }
    return true;
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if (str2 == null)
    {
      if (str1.equals("startx"))
        return this.mStartPosition.x;
      if (str1.equals("starty"))
        return this.mStartPosition.y;
      if (str1.equals("endx"))
        return this.mEndPosition.x;
      if (str1.equals("endy"))
        return this.mEndPosition.y;
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("startx"))
    {
      this.mStartPosition.x = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    if (str.equals("starty"))
    {
      this.mStartPosition.y = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    if (str.equals("endx"))
    {
      this.mEndPosition.x = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    if (str.equals("endy"))
    {
      this.mEndPosition.y = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Translater
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */