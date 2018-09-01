package com.nubee.coinpirates.animation;

import android.graphics.PointF;
import org.xmlpull.v1.XmlPullParser;

class Scaler extends Animator
{
  private static final String attr_name_centerx = "centerx";
  private static final String attr_name_centery = "centery";
  private static final String attr_name_endscalex = "endscalex";
  private static final String attr_name_endscaley = "endscaley";
  private static final String attr_name_startscalex = "startscalex";
  private static final String attr_name_startscaley = "startscaley";
  private final PointF mCenter = new PointF();
  private final PointF mEndScale = new PointF(1.0F, 1.0F);
  private final PointF mScale = new PointF(1.0F, 1.0F);
  private final PointF mScaleDf = new PointF();
  private final PointF mStartScale = new PointF(1.0F, 1.0F);

  public Scaler(XmlPullParser paramXmlPullParser, Part paramPart)
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
      this.mScale.x = this.mStartScale.x;
      this.mScale.y = this.mStartScale.y;
      if (this.mEndTime - this.mStartTime > 0)
      {
        this.mScaleDf.x = ((this.mEndScale.x - this.mStartScale.x) / this.mEndTime - this.mStartTime);
        this.mScaleDf.y = ((this.mEndScale.y - this.mStartScale.y) / this.mEndTime - this.mStartTime);
      }
    }
    if ((this.mStartTime <= paramInt) && (paramInt <= this.mEndTime))
    {
      PointF localPointF1 = this.mScale;
      localPointF1.x += this.mScaleDf.x;
      PointF localPointF2 = this.mScale;
      localPointF2.y += this.mScaleDf.y;
      this.mTarget.setScale(this.mScale.x, this.mScale.y, this.mCenter.x, this.mCenter.y);
    }
    return true;
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if (str2 == null)
    {
      if (str1.equals("startscalex"))
        return this.mStartScale.x;
      if (str1.equals("startscaley"))
        return this.mStartScale.y;
      if (str1.equals("endscalex"))
        return this.mEndScale.x;
      if (str1.equals("endscaley"))
        return this.mEndScale.y;
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
    if (str.equals("startscalex"))
    {
      this.mStartScale.x = Float.parseFloat(paramString2);
      return true;
    }
    if (str.equals("startscaley"))
    {
      this.mStartScale.y = Float.parseFloat(paramString2);
      return true;
    }
    if (str.equals("endscalex"))
    {
      this.mEndScale.x = Float.parseFloat(paramString2);
      return true;
    }
    if (str.equals("endscaley"))
    {
      this.mEndScale.y = Float.parseFloat(paramString2);
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
 * Qualified Name:     com.nubee.coinpirates.animation.Scaler
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */