package com.nubee.coinpirates.animation;

import org.xmlpull.v1.XmlPullParser;

class Visibler extends Animator
{
  private static final String attr_name_visibles = "visibles";
  private int mDt;
  private boolean[] mVisibles;

  public Visibler(XmlPullParser paramXmlPullParser, Part paramPart)
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
    if ((paramInt == this.mStartTime) && (this.mVisibles != null) && (this.mVisibles.length > 0))
      this.mDt = ((this.mEndTime - this.mStartTime) / this.mVisibles.length);
    if ((this.mStartTime <= paramInt) && (paramInt <= this.mEndTime))
      if (this.mDt == 0)
        break label107;
    label107: for (int i = (paramInt - this.mStartTime) / this.mDt; ; i = 0)
    {
      if (i < this.mVisibles.length)
        this.mTarget.setVisible(this.mVisibles[i]);
      return true;
    }
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if ((str2 == null) && (str1.equals("visibles")))
    {
      String str3 = "";
      for (int i = 0; ; i++)
      {
        if (i >= this.mVisibles.length)
          return str3.substring(0, str3.length() - 1);
        str3 = String.valueOf(str3) + this.mVisibles[i] + ",";
      }
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("visibles"))
    {
      String[] arrayOfString = paramString2.split(",");
      this.mVisibles = new boolean[arrayOfString.length];
      for (int i = 0; ; i++)
      {
        if (i >= arrayOfString.length)
          return true;
        this.mVisibles[i] = Boolean.parseBoolean(arrayOfString[i]);
      }
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Visibler
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */