package com.admob.android.ads;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

public final class k extends RelativeLayout
  implements Animation.AnimationListener, ap.a, j.c, l
{
  private static float i = -1.0F;
  private static d j = null;
  protected j a;
  final AdView b;
  protected ProgressBar c;
  private Vector<String> d;
  private View e;
  private long f = -1L;
  private boolean g;
  private View h;

  public k(j paramj, Context paramContext, AdView paramAdView)
  {
    super(paramContext);
    this.b = paramAdView;
    setId(1);
    b(paramContext);
    this.e = null;
    a(null);
  }

  public static float a(Context paramContext)
  {
    b(paramContext);
    return i;
  }

  private static Vector<String> a(int paramInt1, int paramInt2, int paramInt3, long paramLong, Vector<String> paramVector)
  {
    if (paramVector == null);
    for (Object localObject = new Vector(); ; localObject = paramVector)
    {
      float f1 = (float)paramLong / 1000.0F;
      Object[] arrayOfObject2;
      if ((paramInt2 != -1) && (paramInt3 != -1))
      {
        arrayOfObject2 = new Object[4];
        arrayOfObject2[0] = Integer.valueOf(paramInt1);
        arrayOfObject2[1] = Integer.valueOf(paramInt2);
        arrayOfObject2[2] = Integer.valueOf(paramInt3);
        arrayOfObject2[3] = Float.valueOf(f1);
      }
      Object[] arrayOfObject1;
      for (String str = String.format("{%d,%d,%d,%f}", arrayOfObject2); ; str = String.format("{%d,%f}", arrayOfObject1))
      {
        ((Vector)localObject).add(str);
        if (InterstitialAd.c.a("AdMobSDK", 2))
          Log.v("AdMobSDK", "recordEvent:" + str);
        return localObject;
        arrayOfObject1 = new Object[2];
        arrayOfObject1[0] = Integer.valueOf(paramInt1);
        arrayOfObject1[1] = Float.valueOf(f1);
      }
    }
  }

  private Vector<String> a(KeyEvent paramKeyEvent, Vector<String> paramVector)
  {
    int k = paramKeyEvent.getAction();
    long l = paramKeyEvent.getEventTime() - this.f;
    if ((k == 0) || (k == 1))
    {
      if (k == 1);
      for (int m = 1; ; m = 0)
        return a(m, -1, -1, l, paramVector);
    }
    return paramVector;
  }

  private Vector<String> a(MotionEvent paramMotionEvent, boolean paramBoolean, Vector<String> paramVector)
  {
    int k = paramMotionEvent.getAction();
    long l = paramMotionEvent.getEventTime() - this.f;
    if ((k == 0) || (k == 1))
    {
      if (k == 1);
      for (int m = 1; ; m = 0)
        return a(m, (int)paramMotionEvent.getX(), (int)paramMotionEvent.getY(), l, paramVector);
    }
    return paramVector;
  }

  private static void a(View paramView, JSONObject paramJSONObject)
  {
    JSONObject localJSONObject;
    String str;
    if ((paramView instanceof l))
    {
      l locall = (l)paramView;
      localJSONObject = locall.j();
      str = locall.i();
      if ((localJSONObject == null) || (str == null));
    }
    try
    {
      paramJSONObject.put(str, localJSONObject);
      label50: if ((paramView instanceof ViewGroup))
      {
        ViewGroup localViewGroup = (ViewGroup)paramView;
        for (int k = 0; k < localViewGroup.getChildCount(); k++)
          a(localViewGroup.getChildAt(k), paramJSONObject);
      }
    }
    catch (Exception localException)
    {
      break label50;
    }
  }

  private static void b(Context paramContext)
  {
    if (i < 0.0F)
      i = paramContext.getResources().getDisplayMetrics().density;
  }

  public static float d()
  {
    return i;
  }

  private boolean k()
  {
    return (this.a == null) || (!this.a.m());
  }

  private boolean l()
  {
    return (this.a != null) && (SystemClock.uptimeMillis() - this.f > this.a.d());
  }

  private void m()
  {
    JSONObject localJSONObject;
    int k;
    if ((this.a != null) && (isPressed()))
    {
      setPressed(false);
      if (!this.g)
      {
        this.g = true;
        localJSONObject = n();
        if (this.h == null)
          break label190;
        k = 1;
        if (k == 0)
          break label195;
        AnimationSet localAnimationSet = new AnimationSet(true);
        float f1 = this.h.getWidth() / 2.0F;
        float f2 = this.h.getHeight() / 2.0F;
        ScaleAnimation localScaleAnimation1 = new ScaleAnimation(1.0F, 1.2F, 1.0F, 1.2F, f1, f2);
        localScaleAnimation1.setDuration(200L);
        localAnimationSet.addAnimation(localScaleAnimation1);
        ScaleAnimation localScaleAnimation2 = new ScaleAnimation(1.2F, 0.001F, 1.2F, 0.001F, f1, f2);
        localScaleAnimation2.setDuration(299L);
        localScaleAnimation2.setStartOffset(200L);
        localScaleAnimation2.setAnimationListener(this);
        localAnimationSet.addAnimation(localScaleAnimation2);
        postDelayed(new b(localJSONObject, this), 500L);
        this.h.startAnimation(localAnimationSet);
      }
    }
    label190: label195: 
    do
    {
      return;
      k = 0;
      break;
      this.a.a(localJSONObject);
    }
    while (this.b == null);
    this.b.performClick();
  }

  // ERROR //
  private JSONObject n()
  {
    // Byte code:
    //   0: new 156	org/json/JSONObject
    //   3: dup
    //   4: invokespecial 280	org/json/JSONObject:<init>	()V
    //   7: astore_1
    //   8: aload_0
    //   9: aload_1
    //   10: invokestatic 171	com/admob/android/ads/k:a	(Landroid/view/View;Lorg/json/JSONObject;)V
    //   13: new 156	org/json/JSONObject
    //   16: dup
    //   17: invokespecial 280	org/json/JSONObject:<init>	()V
    //   20: astore 6
    //   22: aload 6
    //   24: ldc_w 282
    //   27: aload_1
    //   28: invokevirtual 160	org/json/JSONObject:put	(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
    //   31: pop
    //   32: aload 6
    //   34: areturn
    //   35: astore_2
    //   36: aconst_null
    //   37: astore_3
    //   38: aload_2
    //   39: astore 4
    //   41: ldc 95
    //   43: ldc_w 284
    //   46: aload 4
    //   48: invokestatic 288	android/util/Log:w	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   51: pop
    //   52: aload_3
    //   53: areturn
    //   54: astore 4
    //   56: aload 6
    //   58: astore_3
    //   59: goto -18 -> 41
    //
    // Exception table:
    //   from	to	target	type
    //   0	22	35	java/lang/Exception
    //   22	32	54	java/lang/Exception
  }

  public final void a()
  {
    post(new c(this));
  }

  public final void a(View paramView, RelativeLayout.LayoutParams paramLayoutParams)
  {
    if ((paramView != null) && (paramView != this.h))
    {
      this.h = paramView;
      this.c = new ProgressBar(getContext());
      this.c.setIndeterminate(true);
      this.c.setId(2);
      if (paramLayoutParams != null)
        this.c.setLayoutParams(paramLayoutParams);
      this.c.setVisibility(4);
      post(new a(this));
    }
  }

  public final void a(j paramj)
  {
    this.a = paramj;
    if (paramj == null)
    {
      setFocusable(false);
      setClickable(false);
      return;
    }
    paramj.a(this);
    setFocusable(true);
    setClickable(true);
  }

  public final void b()
  {
    Context localContext = getContext();
    setBackgroundDrawable(localContext.getResources().getDrawable(17301602));
    Drawable localDrawable = localContext.getResources().getDrawable(17301602);
    localDrawable.setAlpha(128);
    this.e = new View(localContext);
    this.e.setBackgroundDrawable(localDrawable);
    this.e.setVisibility(4);
    addView(this.e, new RelativeLayout.LayoutParams(-1, -1));
  }

  public final j c()
  {
    return this.a;
  }

  public final boolean dispatchTouchEvent(MotionEvent paramMotionEvent)
  {
    int k;
    if (k())
    {
      k = paramMotionEvent.getAction();
      if (InterstitialAd.c.a("AdMobSDK", 2))
        Log.v("AdMobSDK", "dispatchTouchEvent: action=" + k + " x=" + paramMotionEvent.getX() + " y=" + paramMotionEvent.getY());
      if (l())
      {
        if (this.a == null)
          break label204;
        Rect localRect = this.a.h();
        if (this.a.a(localRect).contains((int)paramMotionEvent.getX(), (int)paramMotionEvent.getY()))
          break label204;
      }
    }
    label204: for (boolean bool = false; ; bool = true)
    {
      if (bool)
        this.d = a(paramMotionEvent, true, this.d);
      if ((k == 0) || (k == 2))
        setPressed(bool);
      while (true)
      {
        return true;
        if (k == 1)
        {
          if ((isPressed()) && (bool))
            m();
          setPressed(false);
        }
        else if (k == 3)
        {
          setPressed(false);
        }
      }
      return super.dispatchTouchEvent(paramMotionEvent);
    }
  }

  public final boolean dispatchTrackballEvent(MotionEvent paramMotionEvent)
  {
    if (k())
    {
      if (InterstitialAd.c.a("AdMobSDK", 2))
        Log.v("AdMobSDK", "dispatchTrackballEvent: action=" + paramMotionEvent.getAction());
      if (l())
      {
        this.d = a(paramMotionEvent, true, this.d);
        if (paramMotionEvent.getAction() != 0)
          break label84;
        setPressed(true);
      }
    }
    while (true)
    {
      return super.onTrackballEvent(paramMotionEvent);
      label84: if (paramMotionEvent.getAction() == 1)
      {
        if (hasFocus())
          m();
        setPressed(false);
      }
    }
  }

  public final void e()
  {
    if (this.a != null)
    {
      this.a.i();
      this.a = null;
    }
  }

  protected final void f()
  {
    this.g = false;
    if (this.c != null)
      this.c.setVisibility(4);
    if (this.h != null)
      this.h.setVisibility(0);
  }

  public final void g()
  {
    Vector localVector = new Vector();
    for (int k = 0; k < getChildCount(); k++)
      localVector.add(getChildAt(k));
    if (j == null)
      j = new d();
    Collections.sort(localVector, j);
    for (int m = localVector.size() - 1; m >= 0; m--)
      if (indexOfChild((View)localVector.elementAt(m)) != m)
        bringChildToFront((View)localVector.elementAt(m));
    if (this.e != null)
      this.e.bringToFront();
  }

  public final long h()
  {
    long l = SystemClock.uptimeMillis() - this.f;
    if ((this.f < 0L) || (l < 0L) || (l > 10000000L))
      l = 0L;
    return l;
  }

  public final String i()
  {
    return "container";
  }

  public final JSONObject j()
  {
    Vector localVector = this.d;
    JSONObject localJSONObject = null;
    if (localVector != null)
      localJSONObject = new JSONObject();
    try
    {
      localJSONObject.put("touches", new JSONArray(this.d));
      return localJSONObject;
    }
    catch (Exception localException)
    {
    }
    return localJSONObject;
  }

  public final void onAnimationEnd(Animation paramAnimation)
  {
  }

  public final void onAnimationRepeat(Animation paramAnimation)
  {
  }

  public final void onAnimationStart(Animation paramAnimation)
  {
  }

  protected final void onDraw(Canvas paramCanvas)
  {
    if ((isPressed()) || (isFocused()))
      paramCanvas.clipRect(3, 3, getWidth() - 3, getHeight() - 3);
    super.onDraw(paramCanvas);
    if (this.f == -1L)
    {
      this.f = SystemClock.uptimeMillis();
      if (this.a != null)
        this.a.j();
    }
  }

  public final boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    if (k())
    {
      if (InterstitialAd.c.a("AdMobSDK", 2))
        Log.v("AdMobSDK", "onKeyDown: keyCode=" + paramInt);
      if ((paramInt == 66) || (paramInt == 23))
      {
        this.d = a(paramKeyEvent, this.d);
        setPressed(true);
      }
    }
    return super.onKeyDown(paramInt, paramKeyEvent);
  }

  public final boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent)
  {
    if (k())
    {
      if (InterstitialAd.c.a("AdMobSDK", 2))
        Log.v("AdMobSDK", "onKeyUp: keyCode=" + paramInt);
      if ((l()) && ((paramInt == 66) || (paramInt == 23)))
      {
        this.d = a(paramKeyEvent, this.d);
        m();
      }
      setPressed(false);
    }
    return super.onKeyUp(paramInt, paramKeyEvent);
  }

  public final void setPressed(boolean paramBoolean)
  {
    if ((!k()) || ((this.g) && (paramBoolean)));
    while (isPressed() == paramBoolean)
      return;
    if (this.e != null)
    {
      if (!paramBoolean)
        break label63;
      this.e.bringToFront();
      this.e.setVisibility(0);
    }
    while (true)
    {
      super.setPressed(paramBoolean);
      invalidate();
      return;
      label63: this.e.setVisibility(4);
    }
  }

  static final class a
    implements Runnable
  {
    private WeakReference<k> a;

    public a(k paramk)
    {
      this.a = new WeakReference(paramk);
    }

    public final void run()
    {
      try
      {
        k localk = (k)this.a.get();
        if (localk != null)
          localk.addView(localk.c);
        return;
      }
      catch (Exception localException)
      {
        while (!InterstitialAd.c.a("AdMobSDK", 6));
        Log.e("AdMobSDK", "exception caught in AdContainer post run(), " + localException.getMessage());
      }
    }
  }

  public static final class b extends Thread
  {
    private JSONObject a;
    private WeakReference<k> b;

    public b(JSONObject paramJSONObject, k paramk)
    {
      this.a = paramJSONObject;
      this.b = new WeakReference(paramk);
    }

    public final void run()
    {
      try
      {
        k localk = (k)this.b.get();
        if ((localk != null) && (localk.a != null))
        {
          localk.a.a(this.a);
          if (localk.b != null)
            localk.b.performClick();
        }
        return;
      }
      catch (Exception localException)
      {
        while (!InterstitialAd.c.a("AdMobSDK", 6));
        Log.e("AdMobSDK", "exception caught in AdClickThread.run(), ", localException);
      }
    }
  }

  static final class c
    implements Runnable
  {
    private WeakReference<k> a;

    public c(k paramk)
    {
      this.a = new WeakReference(paramk);
    }

    public final void run()
    {
      try
      {
        k localk = (k)this.a.get();
        if (localk != null)
          localk.f();
        return;
      }
      catch (Exception localException)
      {
      }
    }
  }

  static final class d
    implements Comparator<View>
  {
    d()
    {
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.k
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */