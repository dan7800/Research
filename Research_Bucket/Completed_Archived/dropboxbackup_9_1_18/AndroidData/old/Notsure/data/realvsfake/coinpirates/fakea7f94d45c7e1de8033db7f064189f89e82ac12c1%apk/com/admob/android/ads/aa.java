package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public final class aa
{
  ViewGroup a = null;
  boolean b = false;
  private RelativeLayout c = null;
  private RelativeLayout d = null;
  private Button e = null;
  private WeakReference<Activity> f;

  public aa()
  {
  }

  public final void a()
  {
    ac.b(this.c);
    ac.b(this.d);
    this.b = false;
  }

  public final void a(Context paramContext, String paramString, p paramp, float paramFloat, ac paramac, r paramr, WeakReference<Activity> paramWeakReference)
  {
    this.f = paramWeakReference;
    Rect localRect1 = new Rect(0, 0, (int)(320.0F * paramFloat), (int)(34.0F * paramFloat));
    Bitmap localBitmap1 = Bitmap.createBitmap(localRect1.width(), localRect1.height(), Bitmap.Config.ARGB_8888);
    if (localBitmap1 != null)
    {
      Canvas localCanvas1 = new Canvas(localBitmap1);
      j.a(localCanvas1, localRect1, -16777216, -1, 127, 0.5F);
      Paint localPaint1 = new Paint();
      localPaint1.setColor(-7829368);
      float[] arrayOfFloat1 = new float[8];
      arrayOfFloat1[0] = 0.0F;
      arrayOfFloat1[1] = 0.0F;
      arrayOfFloat1[2] = (320.0F * paramFloat);
      arrayOfFloat1[3] = 0.0F;
      arrayOfFloat1[4] = 0.0F;
      arrayOfFloat1[5] = (34.0F * paramFloat - 1.0F);
      arrayOfFloat1[6] = (320.0F * paramFloat);
      arrayOfFloat1[7] = (34.0F * paramFloat - 1.0F);
      localCanvas1.drawLines(arrayOfFloat1, localPaint1);
      RelativeLayout localRelativeLayout1 = new RelativeLayout(paramContext);
      this.c = localRelativeLayout1;
      BitmapDrawable localBitmapDrawable1 = new BitmapDrawable(localBitmap1);
      localBitmapDrawable1.setAlpha(200);
      this.c.setBackgroundDrawable(localBitmapDrawable1);
    }
    TextView localTextView1 = new TextView(paramContext);
    localTextView1.setText(paramp.b);
    RelativeLayout.LayoutParams localLayoutParams1 = new RelativeLayout.LayoutParams(-2, -2);
    localTextView1.setTextSize(14.0F);
    localTextView1.setTypeface(Typeface.DEFAULT_BOLD);
    localTextView1.setTextColor(-1);
    localTextView1.setPadding((int)(3.0F * paramFloat), 0, 0, 0);
    localLayoutParams1.addRule(9);
    localLayoutParams1.addRule(15);
    this.c.addView(localTextView1, localLayoutParams1);
    TextView localTextView2 = new TextView(paramContext);
    localTextView2.setText(t.a("Ads by AdMob"));
    RelativeLayout.LayoutParams localLayoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
    localTextView2.setTextSize(11.0F);
    localTextView2.setTextColor(-1);
    localTextView2.setPadding(0, 0, (int)(3.0F * paramFloat), 0);
    localLayoutParams2.addRule(11);
    localLayoutParams2.addRule(15);
    this.c.addView(localTextView2, localLayoutParams2);
    RelativeLayout.LayoutParams localLayoutParams3 = new RelativeLayout.LayoutParams(-1, (int)(34.0F * paramFloat));
    localLayoutParams3.addRule(10);
    this.c.setVisibility(4);
    paramac.addView(this.c, localLayoutParams3);
    Rect localRect2 = new Rect(0, 0, (int)(320.0F * paramFloat), (int)(50.0F * paramFloat));
    Bitmap localBitmap2 = Bitmap.createBitmap(localRect2.width(), localRect2.height(), Bitmap.Config.ARGB_8888);
    if (localBitmap2 != null)
    {
      Canvas localCanvas2 = new Canvas(localBitmap2);
      j.a(localCanvas2, localRect2, -16777216, -1, 127, 0.5F);
      Paint localPaint2 = new Paint();
      localPaint2.setColor(-7829368);
      float[] arrayOfFloat2 = new float[8];
      arrayOfFloat2[0] = 0.0F;
      arrayOfFloat2[1] = 0.0F;
      arrayOfFloat2[2] = (320.0F * paramFloat);
      arrayOfFloat2[3] = 0.0F;
      arrayOfFloat2[4] = 0.0F;
      arrayOfFloat2[5] = (50.0F * paramFloat - 1.0F);
      arrayOfFloat2[6] = (320.0F * paramFloat);
      arrayOfFloat2[7] = (50.0F * paramFloat - 1.0F);
      localCanvas2.drawLines(arrayOfFloat2, localPaint2);
      RelativeLayout localRelativeLayout2 = new RelativeLayout(paramContext);
      this.d = localRelativeLayout2;
      BitmapDrawable localBitmapDrawable2 = new BitmapDrawable(localBitmap2);
      localBitmapDrawable2.setAlpha(200);
      this.d.setBackgroundDrawable(localBitmapDrawable2);
    }
    Vector localVector = paramr.h.m;
    if (localVector != null)
    {
      LinearLayout localLinearLayout = new LinearLayout(paramContext);
      localLinearLayout.setOrientation(0);
      LinearLayout.LayoutParams localLayoutParams4 = new LinearLayout.LayoutParams(-2, -1);
      Iterator localIterator = localVector.iterator();
      if (localIterator.hasNext())
      {
        o localo = (o)localIterator.next();
        LinearLayout.LayoutParams localLayoutParams8 = new LinearLayout.LayoutParams((int)(64.0F * paramFloat), -2);
        Button localButton3 = new Button(paramContext);
        Bitmap localBitmap3 = (Bitmap)paramr.b().get(localo.b);
        Bitmap localBitmap4 = (Bitmap)paramr.b().get(localo.a);
        BitmapDrawable localBitmapDrawable3 = new BitmapDrawable(localBitmap4);
        localBitmapDrawable3.setBounds(0, 0, (int)(28.0F * paramFloat), (int)(28.0F * paramFloat));
        localButton3.setCompoundDrawables(null, localBitmapDrawable3, null, null);
        localButton3.setBackgroundDrawable(null);
        localButton3.setBackgroundColor(0);
        localButton3.setTextSize(12.0F);
        localButton3.setTextColor(-1);
        localButton3.setText(localo.c);
        localButton3.setPadding(0, (int)(2.0F * paramFloat), 0, (int)(2.0F * paramFloat));
        ac.e locale = new ac.e(paramac, localo, this.f);
        localButton3.setOnClickListener(locale);
        localLinearLayout.addView(new x(paramContext, localButton3, (int)(64.0F * paramFloat), (int)(50.0F * paramFloat), localBitmap3), localLayoutParams8);
        ImageView localImageView = new ImageView(paramContext);
        Bitmap localBitmap5 = Bitmap.createBitmap(1, (int)(34.0F * paramFloat), Bitmap.Config.ARGB_8888);
        if (localBitmap5 == null)
          localImageView = null;
        while (true)
        {
          localLinearLayout.addView(localImageView, localLayoutParams4);
          break;
          Canvas localCanvas3 = new Canvas(localBitmap5);
          Paint localPaint3 = new Paint();
          localPaint3.setColor(-7829368);
          float[] arrayOfFloat3 = new float[4];
          arrayOfFloat3[0] = 0.0F;
          arrayOfFloat3[1] = 0.0F;
          arrayOfFloat3[2] = 0.0F;
          arrayOfFloat3[3] = (34.0F * paramFloat);
          localCanvas3.drawLines(arrayOfFloat3, localPaint3);
          localImageView.setBackgroundDrawable(new BitmapDrawable(localBitmap5));
        }
      }
      RelativeLayout.LayoutParams localLayoutParams5 = new RelativeLayout.LayoutParams(-2, -2);
      localLayoutParams5.addRule(15);
      this.d.addView(localLinearLayout, localLayoutParams5);
    }
    Button localButton1 = new Button(paramContext);
    this.e = localButton1;
    Button localButton2 = this.e;
    ac.i locali = new ac.i(paramac, false);
    localButton2.setOnClickListener(locali);
    this.e.setBackgroundResource(17301509);
    this.e.setTextSize(1, 13.0F);
    this.e.setText(paramString);
    RelativeLayout.LayoutParams localLayoutParams6 = new RelativeLayout.LayoutParams((int)(54.0F * paramFloat), (int)(36.0F * paramFloat));
    localLayoutParams6.addRule(11);
    localLayoutParams6.addRule(15);
    localLayoutParams6.setMargins(0, 0, (int)(2.0F * paramFloat), 0);
    this.d.addView(this.e, localLayoutParams6);
    RelativeLayout.LayoutParams localLayoutParams7 = new RelativeLayout.LayoutParams(-1, (int)(50.0F * paramFloat));
    localLayoutParams7.addRule(12);
    this.d.setVisibility(4);
    paramac.addView(this.d, localLayoutParams7);
    ac.d locald = new ac.d(paramac);
    paramac.setOnTouchListener(locald);
  }

  public final void b()
  {
    this.d.bringToFront();
    this.c.bringToFront();
    ac.a(this.d);
    ac.a(this.c);
    this.b = true;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.aa
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */