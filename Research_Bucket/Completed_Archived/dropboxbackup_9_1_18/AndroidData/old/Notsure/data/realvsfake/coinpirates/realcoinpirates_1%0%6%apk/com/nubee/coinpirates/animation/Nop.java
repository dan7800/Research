package com.nubee.coinpirates.animation;

import org.xmlpull.v1.XmlPullParser;

class Nop extends Animator
{
  public Nop(XmlPullParser paramXmlPullParser, Part paramPart)
  {
    for (int i = 0; ; i++)
    {
      if (i >= paramXmlPullParser.getAttributeCount())
        return;
      super.setAttributeValue(paramXmlPullParser.getAttributeName(i), paramXmlPullParser.getAttributeValue(i));
    }
  }

  public boolean animation(int paramInt)
  {
    return super.animation(paramInt);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Nop
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */