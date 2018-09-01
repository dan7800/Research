package com.nubee.coinpirates.animation;

import org.xmlpull.v1.XmlPullParser;

class Parabola extends Animator
{
  private static final String attr_name_a = "a";
  private static final String attr_name_b = "b";
  private static final String attr_name_bottom = "bottom";
  private static final String attr_name_t0 = "t0";
  private static final String attr_name_target = "target";
  private static final String attr_name_top = "top";
  private double mA;
  private double mB;
  private double mBottom;
  private double mT0;
  private boolean mTargetX = false;
  private double mTop;

  public Parabola(XmlPullParser paramXmlPullParser, Part paramPart)
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
    double d2;
    float f;
    if ((paramInt == this.mStartTime) && (this.mTop != this.mBottom))
    {
      this.mB = this.mTop;
      if (this.mT0 < this.mEndTime - this.mStartTime / 2.0F)
      {
        d2 = this.mEndTime - this.mStartTime - this.mT0;
        this.mA = ((this.mBottom - this.mB) / (d2 * d2));
      }
    }
    else if ((this.mStartTime <= paramInt) && (paramInt <= this.mEndTime))
    {
      double d1 = paramInt - this.mStartTime;
      f = (float)(this.mA * (d1 - this.mT0) * (d1 - this.mT0) + this.mB);
      if (!this.mTargetX)
        break label170;
      this.mTarget.setX(f);
    }
    while (true)
    {
      return true;
      d2 = 0.0D - this.mT0;
      break;
      label170: this.mTarget.setY(f);
    }
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if (str2 == null)
    {
      if (str1.equals("target"))
      {
        if (this.mTargetX)
          return "x";
        return "y";
      }
      if (str1.equals("a"))
        return this.mA;
      if (str1.equals("b"))
        return this.mB;
      if (str1.equals("top"))
        return this.mTop;
      if (str1.equals("bottom"))
        return this.mBottom;
      if (str1.equals("t0"))
        return this.mT0;
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("target"))
    {
      if (paramString2.toLowerCase().equals("x"))
        this.mTargetX = true;
      return true;
    }
    if (str.equals("a"))
    {
      this.mA = Float.parseFloat(paramString2) * Part.getDensity();
      return true;
    }
    if (str.equals("b"))
    {
      this.mB = Float.parseFloat(paramString2) * Part.getDensity();
      return true;
    }
    if (str.equals("top"))
    {
      this.mTop = Float.parseFloat(paramString2) * Part.getDensity();
      return true;
    }
    if (str.equals("bottom"))
    {
      this.mBottom = Float.parseFloat(paramString2) * Part.getDensity();
      return true;
    }
    if (str.equals("t0"))
    {
      this.mT0 = Float.parseFloat(paramString2);
      return true;
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Parabola
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */