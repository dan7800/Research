package com.nubee.coinpirates.animation;

import android.graphics.PointF;
import org.xmlpull.v1.XmlPullParser;

class Rotater extends Animator
{
  private static final String attr_name_centerx = "centerx";
  private static final String attr_name_centery = "centery";
  private static final String attr_name_enddegree = "endangle";
  private static final String attr_name_startdegree = "startangle";
  private final PointF mCenter = new PointF();
  private float mDegree;
  private float mDegreeDf;
  private float mEndDegree;
  private float mStartDegree;

  public Rotater(XmlPullParser paramXmlPullParser, Part paramPart)
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
      this.mDegree = this.mStartDegree;
      if (this.mEndTime - this.mStartTime > 0)
        this.mDegreeDf = ((this.mEndDegree - this.mStartDegree) / this.mEndTime - this.mStartTime);
    }
    if ((this.mStartTime <= paramInt) && (paramInt < this.mEndTime))
    {
      this.mTarget.setRotate(this.mDegree, this.mCenter.x, this.mCenter.y);
      this.mDegree += this.mDegreeDf;
    }
    if (paramInt == this.mEndTime)
    {
      this.mDegree = this.mEndDegree;
      this.mTarget.setRotate(this.mDegree, this.mCenter.x, this.mCenter.y);
    }
    return true;
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if (str2 == null)
    {
      if (str1.equals("startangle"))
        return this.mStartDegree;
      if (str1.equals("endangle"))
        return this.mEndDegree;
      if (str1.equals("centerx"))
        return this.mCenter.x;
      if (str1.equals("centery"))
        return this.mCenter.y;
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("startangle"))
    {
      this.mStartDegree = Float.parseFloat(paramString2);
      return true;
    }
    if (str.equals("endangle"))
    {
      this.mEndDegree = Float.parseFloat(paramString2);
      return true;
    }
    if (str.equals("centerx"))
    {
      this.mCenter.x = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    if (str.equals("centery"))
    {
      this.mCenter.y = (Float.parseFloat(paramString2) * Part.getDensity());
      return true;
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Rotater
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */