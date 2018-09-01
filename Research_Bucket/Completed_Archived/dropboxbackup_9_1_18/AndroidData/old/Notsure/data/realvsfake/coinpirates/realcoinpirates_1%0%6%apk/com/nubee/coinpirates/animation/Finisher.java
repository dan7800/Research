package com.nubee.coinpirates.animation;

import org.xmlpull.v1.XmlPullParser;

class Finisher extends Animator
{
  public Finisher(XmlPullParser paramXmlPullParser, Part paramPart)
  {
    this.mTarget = paramPart;
    for (int i = 0; ; i++)
    {
      if (i >= paramXmlPullParser.getAttributeCount())
        return;
      super.setAttributeValue(paramXmlPullParser.getAttributeName(i), paramXmlPullParser.getAttributeValue(i));
    }
  }

  public boolean animation(int paramInt)
  {
    super.animation(paramInt);
    return paramInt < this.mEndTime;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Finisher
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */