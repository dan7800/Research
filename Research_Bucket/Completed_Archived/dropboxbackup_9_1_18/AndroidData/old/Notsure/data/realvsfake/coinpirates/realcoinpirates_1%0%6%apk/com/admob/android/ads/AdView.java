package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class AdView extends RelativeLayout
{
  private static Boolean a;
  private static Handler s = null;
  private k b;
  private int c;
  private boolean d;
  private d e;
  private int f;
  private int g;
  private int h;
  private String i;
  private String j;
  private AdListener k;
  private boolean l;
  private boolean m = true;
  private boolean n;
  private long o;
  private a p;
  private j.b q;
  private f r;

  public AdView(Activity paramActivity)
  {
    this(paramActivity, null, 0);
  }

  public AdView(Context paramContext, AttributeSet paramAttributeSet)
  {
    this(paramContext, paramAttributeSet, 0);
  }

  public AdView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    this(paramContext, paramAttributeSet, paramInt, f.a);
  }

  public AdView(Context paramContext, AttributeSet paramAttributeSet, int paramInt, f paramf)
  {
    super(paramContext, paramAttributeSet, paramInt);
    if (a == null)
      a = new Boolean(isInEditMode());
    if ((s == null) && (!a.booleanValue()))
    {
      Handler localHandler = new Handler();
      s = localHandler;
      j.a(localHandler);
    }
    this.r = paramf;
    if (paramf != f.a)
      this.q = j.b.a;
    setDescendantFocusability(262144);
    setClickable(true);
    setLongClickable(false);
    setGravity(17);
    int i1;
    int i3;
    int i2;
    if (paramAttributeSet != null)
    {
      String str = "http://schemas.android.com/apk/res/" + paramContext.getPackageName();
      if ((paramAttributeSet.getAttributeBooleanValue(str, "testing", false)) && (InterstitialAd.c.a("AdMobSDK", 5)))
        Log.w("AdMobSDK", "AdView's \"testing\" XML attribute has been deprecated and will be ignored.  Please delete it from your XML layout and use AdManager.setTestDevices instead.");
      int i4 = paramAttributeSet.getAttributeUnsignedIntValue(str, "backgroundColor", -16777216);
      int i5 = paramAttributeSet.getAttributeUnsignedIntValue(str, "textColor", -1);
      if (i5 >= 0)
        setTextColor(i5);
      int i6 = paramAttributeSet.getAttributeUnsignedIntValue(str, "primaryTextColor", -1);
      int i7 = paramAttributeSet.getAttributeUnsignedIntValue(str, "secondaryTextColor", -1);
      this.i = paramAttributeSet.getAttributeValue(str, "keywords");
      setRequestInterval(paramAttributeSet.getAttributeIntValue(str, "refreshInterval", 0));
      boolean bool = paramAttributeSet.getAttributeBooleanValue(str, "isGoneWithoutAd", false);
      if (bool)
        setGoneWithoutAd(bool);
      i1 = i7;
      i3 = i4;
      i2 = i6;
    }
    while (true)
    {
      setBackgroundColor(i3);
      setPrimaryTextColor(i2);
      setSecondaryTextColor(i1);
      this.b = null;
      this.p = null;
      if (a.booleanValue())
      {
        TextView localTextView = new TextView(paramContext, paramAttributeSet, paramInt);
        localTextView.setBackgroundColor(getBackgroundColor());
        localTextView.setTextColor(getPrimaryTextColor());
        localTextView.setPadding(10, 10, 10, 10);
        localTextView.setTextSize(16.0F);
        localTextView.setGravity(16);
        localTextView.setText("Ads by AdMob");
        addView(localTextView, new RelativeLayout.LayoutParams(-1, -1));
        return;
      }
      c();
      return;
      i1 = -1;
      i2 = -1;
      i3 = -16777216;
    }
  }

  private void a(k paramk)
  {
    this.b = paramk;
    if (this.l)
    {
      AlphaAnimation localAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
      localAlphaAnimation.setDuration(233L);
      localAlphaAnimation.startNow();
      localAlphaAnimation.setFillAfter(true);
      localAlphaAnimation.setInterpolator(new AccelerateInterpolator());
      startAnimation(localAlphaAnimation);
    }
  }

  private void a(boolean paramBoolean)
  {
    if (paramBoolean)
      try
      {
        if ((this.c > 0) && (getVisibility() == 0))
        {
          int i1 = this.c;
          d();
          if (e())
          {
            this.e = new d(this);
            s.postDelayed(this.e, i1);
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Ad refresh scheduled for " + i1 + " from now.");
          }
        }
        while (true)
        {
          return;
          if ((!paramBoolean) || (this.c == 0))
            d();
        }
      }
      finally
      {
      }
  }

  private void c()
  {
    c.a(getContext());
    if ((!this.m) && (super.getVisibility() != 0))
      if (InterstitialAd.c.a("AdMobSDK", 5))
        Log.w("AdMobSDK", "Cannot requestFreshAd() when the AdView is not visible.  Call AdView.setVisibility(View.VISIBLE) first.");
    do
    {
      return;
      if (!this.n)
        break;
    }
    while (!InterstitialAd.c.a("AdMobSDK", 5));
    Log.w("AdMobSDK", "Ignoring requestFreshAd() because we are requesting an ad right now already.");
    return;
    this.n = true;
    this.o = SystemClock.uptimeMillis();
    new b(this).start();
  }

  private void d()
  {
    if (this.e != null)
    {
      this.e.a = true;
      this.e = null;
      if (InterstitialAd.c.a("AdMobSDK", 2))
        Log.v("AdMobSDK", "Cancelled an ad refresh scheduled for the future.");
    }
  }

  private boolean e()
  {
    if (this.b != null)
    {
      j localj = this.b.c();
      if ((localj != null) && (localj.e()) && (this.b.h() < 120L))
      {
        if (InterstitialAd.c.a("AdMobSDK", 3))
          Log.d("AdMobSDK", "Cannot refresh CPM ads.  Ignoring request to refresh the ad.");
        return false;
      }
    }
    return true;
  }

  final j.b a()
  {
    return this.q;
  }

  final void a(j paramj, k paramk)
  {
    int i1 = super.getVisibility();
    double d1 = paramj.b();
    if (d1 >= 0.0D)
    {
      this.d = true;
      setRequestInterval((int)d1);
      a(true);
    }
    while (true)
    {
      boolean bool = this.m;
      if (bool)
        this.m = false;
      paramk.a(paramj);
      paramk.setVisibility(i1);
      paramk.setGravity(17);
      paramj.a(paramk);
      paramk.setLayoutParams(new RelativeLayout.LayoutParams(paramj.a(paramj.f()), paramj.a(paramj.g())));
      s.post(new e(this, paramk, i1, bool));
      return;
      this.d = false;
    }
  }

  final f b()
  {
    return this.r;
  }

  public void cleanup()
  {
    if (this.b != null)
    {
      this.b.e();
      this.b = null;
    }
  }

  public AdListener getAdListener()
  {
    return this.k;
  }

  public int getBackgroundColor()
  {
    return this.f;
  }

  public String getKeywords()
  {
    return this.i;
  }

  public int getPrimaryTextColor()
  {
    return this.g;
  }

  public int getRequestInterval()
  {
    return this.c / 1000;
  }

  public String getSearchQuery()
  {
    return this.j;
  }

  public int getSecondaryTextColor()
  {
    return this.h;
  }

  @Deprecated
  public int getTextColor()
  {
    if (InterstitialAd.c.a("AdMobSDK", 5))
      Log.w("AdMobSDK", "Calling the deprecated method getTextColor!  Please use getPrimaryTextColor and getSecondaryTextColor instead.");
    return getPrimaryTextColor();
  }

  public boolean hasAd()
  {
    return (this.b != null) && (this.b.c() != null);
  }

  @Deprecated
  public boolean isGoneWithoutAd()
  {
    if (InterstitialAd.c.a("AdMobSDK", 5))
      Log.w("AdMobSDK", "Deprecated method isGoneWithoutAd was called.  See JavaDoc for instructions to remove.");
    return false;
  }

  protected void onAttachedToWindow()
  {
    this.l = true;
    a(true);
    super.onAttachedToWindow();
  }

  protected void onDetachedFromWindow()
  {
    this.l = false;
    a(false);
    super.onDetachedFromWindow();
  }

  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(paramInt1, paramInt2);
    int i1 = getMeasuredWidth();
    int i2 = getMeasuredHeight();
    if (InterstitialAd.c.a("AdMobSDK", 3))
      Log.d("AdMobSDK", "AdView size is " + i1 + " by " + i2);
    if (!a.booleanValue())
    {
      if ((int)(i1 / k.d()) > 310.0F)
        break label115;
      if (InterstitialAd.c.a("AdMobSDK", 3))
        Log.d("AdMobSDK", "We need to have a minimum width of 320 device independent pixels to show an ad.");
    }
    try
    {
      this.b.setVisibility(8);
      while (true)
      {
        return;
        try
        {
          int i3 = this.b.getVisibility();
          this.b.setVisibility(super.getVisibility());
          if ((i3 != 0) && (this.b.getVisibility() == 0))
          {
            a(this.b);
            return;
          }
        }
        catch (NullPointerException localNullPointerException1)
        {
        }
      }
    }
    catch (NullPointerException localNullPointerException2)
    {
    }
    label115:
  }

  public void onWindowFocusChanged(boolean paramBoolean)
  {
    a(paramBoolean);
  }

  protected void onWindowVisibilityChanged(int paramInt)
  {
    if (paramInt == 0);
    for (boolean bool = true; ; bool = false)
    {
      a(bool);
      return;
    }
  }

  public void requestFreshAd()
  {
    if (this.d)
      if (InterstitialAd.c.a("AdMobSDK", 3))
        Log.d("AdMobSDK", "Request interval overridden by the server.  Ignoring requestFreshAd.");
    do
    {
      long l1;
      do
      {
        return;
        l1 = (SystemClock.uptimeMillis() - this.o) / 1000L;
        if ((l1 <= 0L) || (l1 >= 13L))
          break;
      }
      while (!InterstitialAd.c.a("AdMobSDK", 3));
      Log.d("AdMobSDK", "Ignoring requestFreshAd.  Called " + l1 + " seconds since last refresh.  " + "Refreshes must be at least " + 13 + " apart.");
      return;
    }
    while (!e());
    c();
  }

  public void setAdListener(AdListener paramAdListener)
  {
    try
    {
      this.k = paramAdListener;
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  public void setBackgroundColor(int paramInt)
  {
    this.f = (0xFF000000 | paramInt);
    invalidate();
  }

  public void setEnabled(boolean paramBoolean)
  {
    super.setEnabled(paramBoolean);
    if (paramBoolean)
    {
      setVisibility(0);
      return;
    }
    setVisibility(8);
  }

  @Deprecated
  public void setGoneWithoutAd(boolean paramBoolean)
  {
    if (InterstitialAd.c.a("AdMobSDK", 5))
      Log.w("AdMobSDK", "Deprecated method setGoneWithoutAd was called.  See JavaDoc for instructions to remove.");
  }

  public void setKeywords(String paramString)
  {
    this.i = paramString;
  }

  public void setPrimaryTextColor(int paramInt)
  {
    this.g = (0xFF000000 | paramInt);
  }

  public void setRequestInterval(int paramInt)
  {
    int i1 = paramInt * 1000;
    if (this.c != i1)
      if (paramInt > 0)
      {
        if (paramInt >= 13)
          break label129;
        if (InterstitialAd.c.a("AdMobSDK", 5))
          Log.w("AdMobSDK", "AdView.setRequestInterval(" + paramInt + ") seconds must be >= " + 13);
        i1 = 13000;
      }
    while (true)
    {
      this.c = i1;
      if (paramInt <= 0)
        d();
      if (InterstitialAd.c.a("AdMobSDK", 4))
        Log.i("AdMobSDK", "Requesting fresh ads every " + paramInt + " seconds.");
      return;
      label129: if (paramInt > 600)
      {
        if (InterstitialAd.c.a("AdMobSDK", 5))
          Log.w("AdMobSDK", "AdView.setRequestInterval(" + paramInt + ") seconds must be <= " + 600);
        i1 = 600000;
      }
    }
  }

  public void setSearchQuery(String paramString)
  {
    this.j = paramString;
  }

  public void setSecondaryTextColor(int paramInt)
  {
    this.h = (0xFF000000 | paramInt);
  }

  @Deprecated
  public void setTextColor(int paramInt)
  {
    if (InterstitialAd.c.a("AdMobSDK", 5))
      Log.w("AdMobSDK", "Calling the deprecated method setTextColor!  Please use setPrimaryTextColor and setSecondaryTextColor instead.");
    setPrimaryTextColor(paramInt);
    setSecondaryTextColor(paramInt);
  }

  public void setVisibility(int paramInt)
  {
    if (super.getVisibility() != paramInt)
      try
      {
        int i1 = getChildCount();
        for (i2 = 0; i2 < i1; i2++)
          getChildAt(i2).setVisibility(paramInt);
        super.setVisibility(paramInt);
        invalidate();
        if (paramInt == 0)
        {
          bool = true;
          a(bool);
          return;
        }
      }
      finally
      {
      }
    while (true)
    {
      int i2;
      boolean bool = false;
    }
  }

  public static class a
    implements m
  {
    private WeakReference<AdView> a;

    public a()
    {
    }

    public a(AdView paramAdView)
    {
      this.a = new WeakReference(paramAdView);
    }

    public static Bundle a(n paramn)
    {
      if (paramn == null)
        return null;
      return paramn.a();
    }

    public static ArrayList<Bundle> a(Vector<? extends n> paramVector)
    {
      Object localObject;
      if (paramVector == null)
        localObject = null;
      while (true)
      {
        return localObject;
        localObject = new ArrayList();
        Iterator localIterator = paramVector.iterator();
        while (localIterator.hasNext())
        {
          n localn = (n)localIterator.next();
          if (localn == null)
            ((ArrayList)localObject).add(null);
          else
            ((ArrayList)localObject).add(localn.a());
        }
      }
    }

    public final void a()
    {
      AdView localAdView = (AdView)this.a.get();
      if (localAdView != null)
        AdView.f(localAdView);
    }

    public final void a(j paramj)
    {
      AdView localAdView = (AdView)this.a.get();
      if (localAdView != null)
        try
        {
          if ((AdView.a(localAdView) != null) && (paramj.equals(AdView.a(localAdView).c())))
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Received the same ad we already had.  Discarding it.");
          while (true)
          {
            return;
            if (InterstitialAd.c.a("AdMobSDK", 4))
              Log.i("AdMobSDK", "Ad returned (" + (SystemClock.uptimeMillis() - AdView.g(localAdView)) + " ms):  " + paramj);
            localAdView.getContext();
            localAdView.a(paramj, paramj.c());
          }
        }
        finally
        {
        }
    }
  }

  static final class b extends Thread
  {
    private WeakReference<AdView> a;

    public b(AdView paramAdView)
    {
      this.a = new WeakReference(paramAdView);
    }

    public final void run()
    {
      localAdView = (AdView)this.a.get();
      if (localAdView != null)
        try
        {
          Context localContext = localAdView.getContext();
          k localk = new k(null, localContext, localAdView);
          int i = (int)(localAdView.getMeasuredWidth() / k.d());
          if (b.a(AdView.c(localAdView), localContext, AdView.d(localAdView), AdView.e(localAdView), localAdView.getPrimaryTextColor(), localAdView.getSecondaryTextColor(), localAdView.getBackgroundColor(), localk, i, localAdView.a(), null, localAdView.b()) == null)
            AdView.f(localAdView);
          return;
        }
        catch (Exception localException)
        {
          if (InterstitialAd.c.a("AdMobSDK", 6))
            Log.e("AdMobSDK", "Unhandled exception requesting a fresh ad.", localException);
          AdView.f(localAdView);
          return;
        }
        finally
        {
          AdView.a(localAdView, false);
          AdView.b(localAdView, true);
        }
    }
  }

  static final class c
    implements Runnable
  {
    private WeakReference<AdView> a;

    public c(AdView paramAdView)
    {
      this.a = new WeakReference(paramAdView);
    }

    public final void run()
    {
      AdView localAdView = (AdView)this.a.get();
      if ((localAdView == null) || (((AdView.a(localAdView) == null) || (AdView.a(localAdView).getParent() == null)) && (AdView.b(localAdView) != null)))
        try
        {
          AdView.b(localAdView).onFailedToReceiveAd(localAdView);
          return;
        }
        catch (Exception localException2)
        {
          Log.w("AdMobSDK", "Unhandled exception raised in your AdListener.onFailedToReceiveAd.", localException2);
          return;
        }
      try
      {
        AdView.b(localAdView).onFailedToReceiveRefreshedAd(localAdView);
        return;
      }
      catch (Exception localException1)
      {
        Log.w("AdMobSDK", "Unhandled exception raised in your AdListener.onFailedToReceiveRefreshedAd.", localException1);
      }
    }
  }

  static final class d
    implements Runnable
  {
    boolean a;
    private WeakReference<AdView> b;

    public d(AdView paramAdView)
    {
      this.b = new WeakReference(paramAdView);
    }

    public final void run()
    {
      try
      {
        AdView localAdView = (AdView)this.b.get();
        if ((!this.a) && (localAdView != null))
        {
          if (InterstitialAd.c.a("AdMobSDK", 3))
          {
            int i = AdView.h(localAdView) / 1000;
            if (InterstitialAd.c.a("AdMobSDK", 3))
              Log.d("AdMobSDK", "Requesting a fresh ad because a request interval passed (" + i + " seconds).");
          }
          AdView.i(localAdView);
        }
        return;
      }
      catch (Exception localException)
      {
        while (!InterstitialAd.c.a("AdMobSDK", 6));
        Log.e("AdMobSDK", "exception caught in RefreshHandler.run(), " + localException.getMessage());
      }
    }
  }

  static final class e
    implements Runnable
  {
    private WeakReference<AdView> a;
    private WeakReference<k> b;
    private int c;
    private boolean d;

    public e(AdView paramAdView, k paramk, int paramInt, boolean paramBoolean)
    {
      this.a = new WeakReference(paramAdView);
      this.b = new WeakReference(paramk);
      this.c = paramInt;
      this.d = paramBoolean;
    }

    public final void run()
    {
      try
      {
        localAdView = (AdView)this.a.get();
        localk = (k)this.b.get();
        if ((localAdView != null) && (localk != null))
        {
          localAdView.addView(localk);
          AdView.a(localAdView, localk.c());
          if (this.c == 0)
          {
            if (this.d)
            {
              AdView.a(localAdView, localk);
              return;
            }
            AdView.b(localAdView, localk);
            return;
          }
        }
      }
      catch (Exception localException)
      {
        AdView localAdView;
        k localk;
        if (InterstitialAd.c.a("AdMobSDK", 6))
        {
          Log.e("AdMobSDK", "Unhandled exception placing AdContainer into AdView.", localException);
          return;
          AdView.c(localAdView, localk);
        }
      }
    }
  }

  static final class f
  {
    public static final f a = new f(320, 48);
    private int b;
    private int c;

    static
    {
      new f(320, 270);
      new f(748, 110);
      new f(488, 80);
    }

    private f(int paramInt1, int paramInt2)
    {
      this.b = paramInt1;
      this.c = paramInt2;
    }

    public final String toString()
    {
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append(String.valueOf(this.b));
      localStringBuilder.append("x");
      localStringBuilder.append(String.valueOf(this.c));
      return localStringBuilder.toString();
    }
  }

  static final class g
    implements Runnable
  {
    private WeakReference<AdView> a;
    private WeakReference<k> b;

    public g(k paramk, AdView paramAdView)
    {
      this.b = new WeakReference(paramk);
      this.a = new WeakReference(paramAdView);
    }

    public final void run()
    {
      try
      {
        final AdView localAdView = (AdView)this.a.get();
        final k localk1 = (k)this.b.get();
        if ((localAdView != null) && (localk1 != null))
        {
          final k localk2 = AdView.a(localAdView);
          if (localk2 != null)
            localk2.setVisibility(8);
          localk1.setVisibility(0);
          an localan = new an(90.0F, 0.0F, localAdView.getWidth() / 2.0F, localAdView.getHeight() / 2.0F, -0.4F * localAdView.getWidth(), false);
          localan.setDuration(700L);
          localan.setFillAfter(true);
          localan.setInterpolator(new DecelerateInterpolator());
          localan.setAnimationListener(new Animation.AnimationListener()
          {
            public final void onAnimationEnd(Animation paramAnonymousAnimation)
            {
              if (localk2 != null)
                localAdView.removeView(localk2);
              AdView.c(localAdView, localk1);
              if (localk2 != null)
                localk2.e();
            }

            public final void onAnimationRepeat(Animation paramAnonymousAnimation)
            {
            }

            public final void onAnimationStart(Animation paramAnonymousAnimation)
            {
            }
          });
          localAdView.startAnimation(localan);
        }
        return;
      }
      catch (Exception localException)
      {
        while (!InterstitialAd.c.a("AdMobSDK", 6));
        Log.e("AdMobSDK", "exception caught in SwapViews.run(), " + localException.getMessage());
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdView
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */