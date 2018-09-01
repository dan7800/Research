package com.nubee.coinpirates.animation;

import android.os.SystemClock;
import org.xmlpull.v1.XmlPullParser;

class Wink extends Animator
{
  private static final String attr_name_cycle = "cycle";
  private static final String attr_name_frequency = "frequency";
  private static final String attr_name_length = "length";
  private long cycle_base = 3000L;
  private int frequency = 5;
  private int length = 50;
  private long[] wink_chance;
  private long wink_cycle = 3000L;
  private boolean winked = false;
  private long winked_time = 0L;

  public Wink(XmlPullParser paramXmlPullParser, Part paramPart)
  {
    this.mTarget = paramPart;
    for (int i = 0; ; i++)
    {
      if (i >= paramXmlPullParser.getAttributeCount())
      {
        this.wink_cycle = this.cycle_base;
        if (this.frequency < 0)
          this.frequency = 5;
        this.wink_chance = new long[this.frequency];
        return;
      }
      setAttributeValue(paramXmlPullParser.getAttributeName(i), paramXmlPullParser.getAttributeValue(i));
    }
  }

  public boolean animation(int paramInt)
  {
    super.animation(paramInt);
    long l = SystemClock.uptimeMillis() % this.wink_cycle;
    int k;
    if (!this.winked)
    {
      k = 0;
      if (k < this.wink_chance.length);
    }
    else
    {
      label35: if ((this.winked) && (l - this.winked_time > this.length))
      {
        this.winked = false;
        this.mTarget.selectImage(0);
      }
      if ((!this.winked) && (0L < l) && (l < 100L))
      {
        int i = (int)(Math.random() * this.cycle_base);
        this.wink_cycle = (this.cycle_base + i);
      }
    }
    for (int j = 0; ; j++)
    {
      if (j >= this.wink_chance.length)
      {
        return true;
        if ((this.wink_chance[k] < l) && (l < 100L + this.wink_chance[k]))
        {
          this.winked_time = l;
          this.winked = true;
          this.mTarget.selectImage(1);
          break label35;
        }
        k++;
        break;
      }
      this.wink_chance[j] = (int)(Math.random() * this.cycle_base);
    }
  }

  public String getAttributeValue(String paramString)
  {
    String str1 = paramString.toLowerCase();
    String str2 = super.getAttributeValue(str1);
    if (str2 == null)
    {
      if (str1.equals("cycle"))
        return this.cycle_base;
      if (str1.equals("frequency"))
        return this.frequency;
      if (str1.equals("length"))
        return this.length;
    }
    return str2;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (super.setAttributeValue(str, paramString2))
      return true;
    if (str.equals("cycle"))
    {
      this.cycle_base = Integer.parseInt(paramString2);
      return true;
    }
    if (str.equals("frequency"))
    {
      this.frequency = Integer.parseInt(paramString2);
      return true;
    }
    if (str.equals("length"))
    {
      this.length = Integer.parseInt(paramString2);
      return true;
    }
    return false;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Wink
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */