package com.nubee.coinpirates.animation;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class Animator
{
  protected static final String attr_name_endtime = "endtime";
  protected static final String attr_name_id = "id";
  protected static final String attr_name_starttime = "starttime";
  private static final String attr_name_type = "type";
  private static final String attr_value_blend = "blend";
  private static final String attr_value_finish = "finish";
  private static final String attr_value_parabola = "parabola";
  private static final String attr_value_random = "random";
  private static final String attr_value_rotate = "rotate";
  private static final String attr_value_scale = "scale";
  private static final String attr_value_select = "select";
  private static final String attr_value_translate = "translate";
  private static final String attr_value_visible = "visible";
  private static final String attr_value_wink = "wink";
  AnimatorCallback mCallback = null;
  protected int mCurrentTime = 0;
  protected int mEndTime = -1;
  protected String mId = "";
  protected int mStartTime = 0;
  protected Part mTarget;

  protected Animator()
  {
  }

  public static Animator createAnimator(XmlPullParser paramXmlPullParser, Part paramPart)
    throws XmlPullParserException, IOException
  {
    for (int i = 0; ; i++)
    {
      int j = paramXmlPullParser.getAttributeCount();
      Object localObject = null;
      if (i >= j);
      while (true)
        if (paramXmlPullParser.next() == 3)
        {
          return localObject;
          if (!paramXmlPullParser.getAttributeName(i).toLowerCase().equals("type"))
            break;
          String str = paramXmlPullParser.getAttributeValue(i).toLowerCase();
          if (str.equals("translate"))
            localObject = new Translater(paramXmlPullParser, paramPart);
          else if (str.equals("scale"))
            localObject = new Scaler(paramXmlPullParser, paramPart);
          else if (str.equals("rotate"))
            localObject = new Rotater(paramXmlPullParser, paramPart);
          else if (str.equals("select"))
            localObject = new Selecter(paramXmlPullParser, paramPart);
          else if (str.equals("blend"))
            localObject = new Blender(paramXmlPullParser, paramPart);
          else if (str.equals("wink"))
            localObject = new Wink(paramXmlPullParser, paramPart);
          else if (str.equals("parabola"))
            localObject = new Parabola(paramXmlPullParser, paramPart);
          else if (str.equals("random"))
            localObject = new Random(paramXmlPullParser, paramPart);
          else if (str.equals("visible"))
            localObject = new Visibler(paramXmlPullParser, paramPart);
          else if (str.equals("finish"))
            localObject = new Finisher(paramXmlPullParser, paramPart);
          else
            localObject = new Nop(paramXmlPullParser, paramPart);
        }
    }
  }

  public boolean animation(int paramInt)
  {
    this.mCurrentTime = paramInt;
    if ((this.mCallback != null) && (isFinished()))
      this.mCallback.onAnimationFinished();
    return true;
  }

  public String getAttributeValue(String paramString)
  {
    String str = paramString.toLowerCase();
    if (str.equals("starttime"))
      return this.mStartTime;
    if (str.equals("endtime"))
      return this.mEndTime;
    if (str.equals("id"))
      return this.mId;
    return null;
  }

  public int getEndTime()
  {
    return this.mEndTime;
  }

  public String getId()
  {
    return this.mId;
  }

  public boolean isFinished()
  {
    return this.mCurrentTime >= this.mEndTime;
  }

  public boolean setAttributeValue(String paramString1, String paramString2)
  {
    String str = paramString1.toLowerCase();
    if (str.equals("starttime"))
    {
      this.mStartTime = Integer.parseInt(paramString2);
      if (this.mEndTime < this.mStartTime)
        this.mEndTime = (1 + this.mStartTime);
      return true;
    }
    if (str.equals("endtime"))
    {
      this.mEndTime = Integer.parseInt(paramString2);
      return true;
    }
    if (str.equals("id"))
    {
      this.mId = paramString2;
      return true;
    }
    return false;
  }

  public void setOnFinishListener(AnimatorCallback paramAnimatorCallback)
  {
    this.mCallback = paramAnimatorCallback;
  }

  public static abstract interface AnimatorCallback
  {
    public abstract void onAnimationFinished();
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Animator
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */