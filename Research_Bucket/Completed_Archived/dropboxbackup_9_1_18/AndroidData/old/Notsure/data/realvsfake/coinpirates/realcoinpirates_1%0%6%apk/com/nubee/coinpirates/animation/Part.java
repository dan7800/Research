package com.nubee.coinpirates.animation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.Display;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Part
{
  private static final String attr_name_alpha = "alpha";
  private static final String attr_name_angle = "angle";
  private static final String attr_name_scalex = "scalex";
  private static final String attr_name_scaley = "scaley";
  private static final String attr_name_visible = "visible";
  private static final String attr_name_x = "x";
  private static final String attr_name_y = "y";
  private static float sDensity = 0.0F;
  private static final String tag_name_animation = "animation";
  private static final String tag_name_animations = "animations";
  private static final String tag_name_img = "img";
  private static final String tag_name_imgs = "imgs";
  private static final String tag_name_part = "part";
  private static final String tag_name_parts = "parts";
  private int mAlpha = 255;
  private boolean mAlphaEnable = false;
  private int mAlphaIndex = -1;
  protected int mAnimationCount = 0;
  protected boolean mAnimationEnable = true;
  protected int mAnimationLastCount = 0;
  private List<Animator> mAnimations = null;
  protected float mDegree = 0.0F;
  private List<Image> mImages = null;
  private List<Part> mParts = null;
  private final PointF mPosition = new PointF();
  protected final PointF mRotateCenter = new PointF();
  protected final PointF mScale = new PointF(1.0F, 1.0F);
  protected final PointF mScaleCenter = new PointF();
  private int mSelectedImage = 0;
  private boolean mVisible = true;

  public Part(Context paramContext, XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    int i = 0;
    if (i >= paramXmlPullParser.getAttributeCount());
    while (true)
    {
      int j = paramXmlPullParser.next();
      if (j == 3)
      {
        return;
        String str1 = paramXmlPullParser.getAttributeName(i).toLowerCase();
        String str2 = paramXmlPullParser.getAttributeValue(i);
        if (str1.equals("x"))
          this.mPosition.x = (Float.parseFloat(str2) * sDensity);
        while (true)
        {
          i++;
          break;
          if (str1.equals("y"))
          {
            this.mPosition.y = (Float.parseFloat(str2) * sDensity);
          }
          else if (str1.equals("visible"))
          {
            this.mVisible = Boolean.parseBoolean(str2);
          }
          else if (str1.equals("scalex"))
          {
            this.mScale.x = Float.parseFloat(str2);
          }
          else if (str1.equals("scaley"))
          {
            this.mScale.y = Float.parseFloat(str2);
          }
          else if (str1.equals("angle"))
          {
            this.mDegree = Float.parseFloat(str2);
          }
          else if (str1.equals("alpha"))
          {
            this.mAlpha = Integer.parseInt(str2);
            if ((this.mAlpha >= 0) && (this.mAlpha < 255))
              this.mAlphaEnable = true;
          }
        }
      }
      if (j == 2)
      {
        String str3 = paramXmlPullParser.getName().toLowerCase();
        if (str3.equals("imgs"))
          createImages(paramContext, paramXmlPullParser);
        else if (str3.equals("animations"))
          createAnimations(paramXmlPullParser);
        else if (str3.equals("parts"))
          createParts(paramContext, paramXmlPullParser);
      }
    }
  }

  private void createAnimations(XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    this.mAnimations = new ArrayList();
    while (true)
    {
      int i = paramXmlPullParser.next();
      if (i == 3)
        return;
      if ((i == 2) && (paramXmlPullParser.getName().toLowerCase().equals("animation")))
      {
        Animator localAnimator = Animator.createAnimator(paramXmlPullParser, this);
        if (localAnimator != null)
        {
          int j = localAnimator.getEndTime();
          if (j > this.mAnimationLastCount)
            this.mAnimationLastCount = j;
          this.mAnimations.add(localAnimator);
        }
      }
    }
  }

  private void createImages(Context paramContext, XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    this.mImages = new ArrayList();
    while (true)
    {
      int i = paramXmlPullParser.next();
      if (i == 3)
        return;
      if ((i == 2) && (paramXmlPullParser.getName().toLowerCase().equals("img")))
        this.mImages.add(new Image(paramContext, paramXmlPullParser));
    }
  }

  public static Part createPart(Context paramContext, int paramInt)
    throws XmlPullParserException, IOException
  {
    return parse(paramContext, paramContext.getResources().getXml(paramInt));
  }

  public static Part createPart(Context paramContext, BufferedReader paramBufferedReader)
    throws XmlPullParserException, IOException
  {
    XmlPullParser localXmlPullParser = Xml.newPullParser();
    localXmlPullParser.setInput(paramBufferedReader);
    return parse(paramContext, localXmlPullParser);
  }

  public static Part createPart(Context paramContext, InputStream paramInputStream)
    throws XmlPullParserException, IOException
  {
    XmlPullParser localXmlPullParser = Xml.newPullParser();
    localXmlPullParser.setInput(new BufferedReader(new InputStreamReader(paramInputStream)));
    return parse(paramContext, localXmlPullParser);
  }

  public static Part createPart(Context paramContext, String paramString)
    throws XmlPullParserException, IOException
  {
    XmlPullParser localXmlPullParser = Xml.newPullParser();
    localXmlPullParser.setInput(new BufferedReader(new InputStreamReader(new FileInputStream(paramString))));
    return parse(paramContext, localXmlPullParser);
  }

  private void createParts(Context paramContext, XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    this.mParts = new ArrayList();
    while (true)
    {
      int i = paramXmlPullParser.next();
      if (i == 3)
        return;
      if ((i == 2) && (paramXmlPullParser.getName().toLowerCase().equals("part")))
        this.mParts.add(new Part(paramContext, paramXmlPullParser));
    }
  }

  public static float getDensity()
  {
    return sDensity;
  }

  public static Part parse(Context paramContext, XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    DisplayMetrics localDisplayMetrics = new DisplayMetrics();
    ((Activity)paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
    sDensity = localDisplayMetrics.density;
    int i = paramXmlPullParser.getEventType();
    if (i == 1)
      return null;
    switch (i)
    {
    default:
    case 2:
    }
    do
    {
      i = paramXmlPullParser.next();
      break;
    }
    while (!paramXmlPullParser.getName().toLowerCase().equals("part"));
    return new Part(paramContext, paramXmlPullParser);
  }

  public void alphaBlend(int paramInt1, int paramInt2, int paramInt3)
  {
    this.mAlphaEnable = true;
    this.mSelectedImage = paramInt1;
    this.mAlphaIndex = paramInt2;
    this.mAlpha = paramInt3;
  }

  public void animation()
  {
    int k;
    int i;
    if ((this.mAnimationEnable) && (this.mAnimations != null))
    {
      k = 0;
      if (k < this.mAnimations.size());
    }
    else
    {
      this.mAnimationCount = (1 + this.mAnimationCount);
      if (this.mAnimationCount > this.mAnimationLastCount)
        this.mAnimationCount = 0;
      if (this.mParts != null)
        i = this.mParts.size();
    }
    for (int j = 0; ; j++)
    {
      if (j >= i)
      {
        return;
        if (!((Animator)this.mAnimations.get(k)).animation(this.mAnimationCount))
          this.mAnimationEnable = false;
        k++;
        break;
      }
      if ((this.mParts != null) && (this.mParts.get(j) != null))
        ((Part)this.mParts.get(j)).animation();
    }
  }

  public void draw(Canvas paramCanvas, Paint paramPaint)
  {
    if (!this.mVisible)
      return;
    Paint localPaint;
    int i;
    if (paramPaint != null)
    {
      localPaint = new Paint(paramPaint);
      localPaint.setAntiAlias(true);
      paramCanvas.save();
      setTranslateState(paramCanvas);
      if (this.mImages != null)
      {
        if ((this.mSelectedImage >= 0) && (this.mSelectedImage <= this.mImages.size()))
        {
          if (this.mAlphaEnable)
            localPaint.setAlpha(this.mAlpha);
          ((Image)this.mImages.get(this.mSelectedImage)).draw(paramCanvas, localPaint);
        }
        if ((this.mAlphaEnable) && (this.mAlphaIndex >= 0) && (this.mAlphaIndex <= this.mImages.size()))
          ((Image)this.mImages.get(this.mAlphaIndex)).draw(paramCanvas, null);
      }
      if (this.mParts != null)
      {
        if (this.mAlphaEnable)
          localPaint.setAlpha(this.mAlpha);
        i = this.mParts.size();
      }
    }
    for (int j = 0; ; j++)
    {
      if (j >= i)
      {
        paramCanvas.restore();
        return;
        localPaint = new Paint();
        break;
      }
      if ((this.mParts != null) && (this.mParts.get(j) != null))
        ((Part)this.mParts.get(j)).draw(paramCanvas, localPaint);
    }
  }

  public Animator findAnimator(String paramString)
  {
    int j;
    if (this.mAnimations != null)
    {
      j = 0;
      if (j < this.mAnimations.size());
    }
    else if (this.mParts == null);
    for (int i = 0; ; i++)
    {
      if (i >= this.mParts.size())
      {
        return null;
        Animator localAnimator2 = (Animator)this.mAnimations.get(j);
        if (localAnimator2.getId().equals(paramString))
          return localAnimator2;
        j++;
        break;
      }
      Animator localAnimator1 = ((Part)this.mParts.get(i)).findAnimator(paramString);
      if (localAnimator1 != null)
        return localAnimator1;
    }
  }

  public Image findImage(String paramString)
  {
    int j;
    if (this.mImages != null)
    {
      j = 0;
      if (j < this.mImages.size());
    }
    else if (this.mParts == null);
    for (int i = 0; ; i++)
    {
      if (i >= this.mParts.size())
      {
        return null;
        Image localImage2 = (Image)this.mImages.get(j);
        if ((localImage2 != null) && (localImage2.getId() != null) && (localImage2.getId().equals(paramString)))
          return localImage2;
        j++;
        break;
      }
      Image localImage1 = ((Part)this.mParts.get(i)).findImage(paramString);
      if (localImage1 != null)
        return localImage1;
    }
  }

  public PointF getPosition()
  {
    return new PointF(this.mPosition.x, this.mPosition.y);
  }

  public int getSelectedIndex()
  {
    return this.mSelectedImage;
  }

  public void recycle()
  {
    int j;
    if (this.mImages != null)
    {
      j = 0;
      if (j >= this.mImages.size())
      {
        this.mImages.clear();
        this.mImages = null;
      }
    }
    else if (this.mParts == null);
    for (int i = 0; ; i++)
    {
      if (i >= this.mParts.size())
      {
        this.mParts.clear();
        this.mParts = null;
        return;
        ((Image)this.mImages.get(j)).recycle();
        j++;
        break;
      }
      ((Part)this.mParts.get(i)).recycle();
    }
  }

  public void selectImage(int paramInt)
  {
    this.mSelectedImage = paramInt;
  }

  public void setPosition(float paramFloat1, float paramFloat2)
  {
    this.mPosition.x = paramFloat1;
    this.mPosition.y = paramFloat2;
  }

  public void setRotate(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    this.mDegree = paramFloat1;
    this.mRotateCenter.x = paramFloat2;
    this.mRotateCenter.y = paramFloat3;
  }

  public void setScale(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
  {
    this.mScale.x = paramFloat1;
    this.mScale.y = paramFloat2;
    this.mScaleCenter.x = paramFloat3;
    this.mScaleCenter.y = paramFloat4;
  }

  protected final void setTranslateState(Canvas paramCanvas)
  {
    paramCanvas.translate(this.mPosition.x, this.mPosition.y);
    if ((this.mScale.x != 1.0F) || (this.mScale.y != 1.0F))
      paramCanvas.scale(this.mScale.x, this.mScale.y, this.mScaleCenter.x, this.mScaleCenter.y);
    if (this.mDegree != 0.0F)
      paramCanvas.rotate(this.mDegree, this.mRotateCenter.x, this.mRotateCenter.y);
  }

  public void setVisible(boolean paramBoolean)
  {
    this.mVisible = paramBoolean;
  }

  public void setX(float paramFloat)
  {
    this.mPosition.x = paramFloat;
  }

  public void setY(float paramFloat)
  {
    this.mPosition.y = paramFloat;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.animation.Part
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */