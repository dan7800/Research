package com.nubee.coinpirates.animation;

import org.xmlpull.v1.XmlPullParser;

class Selecter extends Animator
{
  private static final String attr_name_indices = "indices";
  private int mDt;
  private int[] mIndices;

  public Selecter(XmlPullParser paramXmlPullParser, Part paramPart)
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
    if ((paramInt == this.mStartTime) && (this.mIndices != null) && (this.mIndices.length > 0))
      this.mDt = ((this.mEndTime - this.mStartTime) / this.mIndices.length);
    if ((this.mStartTime <= paramInt) && (paramInt <= this.mEndTime))
    {
      int i = (paramInt - this.mStartTime) / this.mDt;
      if (i < this.mIndices.length)
        this.mTarget.selectImage(this.mIndices[i]);
    }
    return true;
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if ((str2 == null) && (str1.equals("indices")))
    {
      String str3 = "";
      for (int i = 0; ; i++)
      {
        if (i >= this.mIndices.length)
          return str3.substring(0, str3.length() - 1);
        str3 = String.valueOf(str3) + this.mIndices[i] + ",";
      }
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("indices"))
    {
      String[] arrayOfString = paramString2.split(",");
      this.mIndices = new int[arrayOfString.length];
      for (int i = 0; ; i++)
      {
        if (i >= arrayOfString.length)
          return true;
        this.mIndices[i] = Integer.parseInt(arrayOfString[i]);
      }
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Selecter
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */