package com.nubee.coinpirates.animation;

import org.xmlpull.v1.XmlPullParser;

class Blender extends Animator
{
  private static final String attr_name_endalpha = "endalpha";
  private static final String attr_name_indices = "indices";
  private static final String attr_name_startalpha = "startalpha";
  private float mAlpha;
  private float mAlphaDf;
  private int mEndAlpha = 255;
  private int[] mIndices;
  private int mStartAlpha = 0;

  public Blender(XmlPullParser paramXmlPullParser, Part paramPart)
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
      if ((this.mIndices == null) || (this.mIndices.length < 2))
      {
        this.mIndices = new int[2];
        this.mIndices[0] = this.mTarget.getSelectedIndex();
        this.mIndices[1] = -1;
      }
      this.mAlpha = this.mStartAlpha;
      if (this.mEndTime - this.mStartTime > 0)
        this.mAlphaDf = ((this.mEndAlpha - this.mStartAlpha) / this.mEndTime - this.mStartTime);
    }
    if ((this.mStartTime <= paramInt) && (paramInt <= this.mEndTime))
    {
      this.mAlpha += this.mAlphaDf;
      if (this.mAlpha > 255.0F)
        this.mAlpha = 255.0F;
      if (this.mAlpha < 0.0F)
        this.mAlpha = 0.0F;
      this.mTarget.alphaBlend(this.mIndices[0], this.mIndices[1], (int)this.mAlpha);
    }
    return true;
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if (str2 == null)
    {
      if (str1.equals("startalpha"))
        return this.mStartAlpha;
      if (str1.equals("endalpha"))
        return this.mEndAlpha;
      if (str1.equals("indices"))
      {
        String str3 = "";
        for (int i = 0; ; i++)
        {
          if (i >= this.mIndices.length)
            return str3.substring(0, str3.length() - 1);
          str3 = String.valueOf(str3) + this.mIndices[i] + ",";
        }
      }
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("startalpha"))
    {
      this.mStartAlpha = Integer.parseInt(paramString2);
      return true;
    }
    if (str.equals("endalpha"))
    {
      this.mEndAlpha = Integer.parseInt(paramString2);
      return true;
    }
    if (str.equals("indices"))
    {
      String[] arrayOfString = paramString2.split(",");
      this.mIndices = new int[arrayOfString.length];
      if (arrayOfString.length >= 2);
      for (int i = 0; ; i++)
      {
        if (i >= 2)
          return true;
        this.mIndices[i] = Integer.parseInt(arrayOfString[i]);
      }
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Blender
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */