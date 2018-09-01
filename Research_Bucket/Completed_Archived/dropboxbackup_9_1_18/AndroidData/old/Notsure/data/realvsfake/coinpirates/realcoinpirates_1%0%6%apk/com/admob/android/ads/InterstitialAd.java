package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class InterstitialAd
{
  public static final String ADMOB_INTENT_BOOLEAN = "admob_activity";
  private static Handler a = null;
  private static Timer b = null;
  private static a c = null;
  private Event d;
  private WeakReference<InterstitialAdListener> e;
  private boolean f;
  private boolean g;
  private j h;
  private String i;
  private String j;
  private c k;
  private long l;

  public InterstitialAd(Event paramEvent, InterstitialAdListener paramInterstitialAdListener)
  {
    this.d = paramEvent;
    this.e = new WeakReference(paramInterstitialAdListener);
    this.f = false;
    this.g = false;
    this.h = null;
    this.i = null;
    this.j = null;
    this.k = new c(this);
    this.l = -1L;
    if (a == null)
      a = new Handler();
  }

  private static void g()
  {
    if (b != null)
    {
      b.cancel();
      b = null;
    }
  }

  private static boolean h()
  {
    return c != null;
  }

  final void a()
  {
    if (a != null)
      a.post(new e(this));
  }

  final void a(Activity paramActivity)
  {
    if (!this.f)
      if (this.g)
        if (!c.a("AdMobSDK", 6));
    do
    {
      q localq;
      do
      {
        Log.e("AdMobSDK", "Show has already been called.  Please create and request a new interstitial");
        do
          return;
        while (!c.a("AdMobSDK", 6));
        Log.e("AdMobSDK", "Cannot call show before interstitial is ready");
        return;
        this.g = true;
        this.f = false;
        localq = this.h.a();
      }
      while (localq == null);
      PackageManager localPackageManager = paramActivity.getPackageManager();
      Iterator localIterator = localq.b.iterator();
      while (localIterator.hasNext())
      {
        Intent localIntent = (Intent)localIterator.next();
        if (localPackageManager.resolveActivity(localIntent, 65536) != null)
          try
          {
            paramActivity.startActivityForResult(localIntent, 0);
            return;
          }
          catch (Exception localException)
          {
          }
      }
    }
    while (!c.a("AdMobSDK", 6));
    Log.e("AdMobSDK", "Could not find a resolving intent on ad click");
  }

  final void b()
  {
    g();
    if ((this.l != -1L) && (c.a("AdMobSDK", 2)))
    {
      long l1 = SystemClock.uptimeMillis() - this.l;
      Log.v("AdMobSDK", "total request time: " + l1);
    }
    this.f = true;
    c = null;
    InterstitialAdListener localInterstitialAdListener = (InterstitialAdListener)this.e.get();
    if (localInterstitialAdListener != null);
    try
    {
      localInterstitialAdListener.onReceiveInterstitial(this);
      return;
    }
    catch (Exception localException)
    {
      Log.w("AdMobSDK", "Unhandled exception raised in your InterstitialAdListener.onReceiveInterstitial.", localException);
    }
  }

  final void c()
  {
    if (a != null)
      a.post(new b(this));
  }

  final void d()
  {
    c = null;
    InterstitialAdListener localInterstitialAdListener = (InterstitialAdListener)this.e.get();
    if (localInterstitialAdListener != null);
    try
    {
      localInterstitialAdListener.onFailedToReceiveInterstitial(this);
      return;
    }
    catch (Exception localException)
    {
      Log.w("AdMobSDK", "Unhandled exception raised in your InterstitialAdListener.onFailedToReceiveInterstitial.", localException);
    }
  }

  final Event e()
  {
    return this.d;
  }

  final c f()
  {
    return this.k;
  }

  public String getKeywords()
  {
    return this.j;
  }

  public String getSearchQuery()
  {
    return this.i;
  }

  public boolean isReady()
  {
    return this.f;
  }

  public void requestAd(Context paramContext)
  {
    if (h())
    {
      if (c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "A request is already in progress.  This request will fail.");
      c();
    }
    float f1;
    do
    {
      return;
      a locala = new a(this, paramContext);
      c = locala;
      locala.start();
      this.l = SystemClock.uptimeMillis();
      f1 = j.a(paramContext);
    }
    while (f1 <= 0.0F);
    f localf = new f(this);
    if (b == null)
      b = new Timer();
    b.schedule(localf, ()(f1 * 1000.0F));
  }

  public void setKeywords(String paramString)
  {
    this.j = paramString;
  }

  public void setListener(InterstitialAdListener paramInterstitialAdListener)
  {
    this.e = new WeakReference(paramInterstitialAdListener);
  }

  public void setSearchQuery(String paramString)
  {
    this.i = paramString;
  }

  public void show(Activity paramActivity)
  {
    a.post(new d(paramActivity, this));
  }

  public static enum Event
  {
    static
    {
      PRE_ROLL = new Event("PRE_ROLL", 2);
      POST_ROLL = new Event("POST_ROLL", 3);
      OTHER = new Event("OTHER", 4);
      Event[] arrayOfEvent = new Event[5];
      arrayOfEvent[0] = APP_START;
      arrayOfEvent[1] = SCREEN_CHANGE;
      arrayOfEvent[2] = PRE_ROLL;
      arrayOfEvent[3] = POST_ROLL;
      arrayOfEvent[4] = OTHER;
    }
  }

  static final class a extends Thread
  {
    boolean a;
    private InterstitialAd b;
    private WeakReference<Context> c;

    public a(InterstitialAd paramInterstitialAd, Context paramContext)
    {
      this.b = paramInterstitialAd;
      this.c = new WeakReference(paramContext);
      this.a = false;
    }

    // ERROR //
    public final void run()
    {
      // Byte code:
      //   0: aload_0
      //   1: getfield 25	com/admob/android/ads/InterstitialAd$a:c	Ljava/lang/ref/WeakReference;
      //   4: invokevirtual 34	java/lang/ref/WeakReference:get	()Ljava/lang/Object;
      //   7: checkcast 36	android/content/Context
      //   10: astore_1
      //   11: aload_1
      //   12: ifnull +95 -> 107
      //   15: aload_0
      //   16: getfield 18	com/admob/android/ads/InterstitialAd$a:b	Lcom/admob/android/ads/InterstitialAd;
      //   19: invokevirtual 42	com/admob/android/ads/InterstitialAd:f	()Lcom/admob/android/ads/InterstitialAd$c;
      //   22: aload_1
      //   23: aload_0
      //   24: getfield 18	com/admob/android/ads/InterstitialAd$a:b	Lcom/admob/android/ads/InterstitialAd;
      //   27: invokevirtual 46	com/admob/android/ads/InterstitialAd:getKeywords	()Ljava/lang/String;
      //   30: aload_0
      //   31: getfield 18	com/admob/android/ads/InterstitialAd$a:b	Lcom/admob/android/ads/InterstitialAd;
      //   34: invokevirtual 49	com/admob/android/ads/InterstitialAd:getSearchQuery	()Ljava/lang/String;
      //   37: aload_0
      //   38: getfield 18	com/admob/android/ads/InterstitialAd$a:b	Lcom/admob/android/ads/InterstitialAd;
      //   41: invokevirtual 53	com/admob/android/ads/InterstitialAd:e	()Lcom/admob/android/ads/InterstitialAd$Event;
      //   44: invokestatic 58	com/admob/android/ads/b:a	(Lcom/admob/android/ads/m;Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Lcom/admob/android/ads/InterstitialAd$Event;)Lcom/admob/android/ads/j;
      //   47: astore 5
      //   49: aload_0
      //   50: getfield 27	com/admob/android/ads/InterstitialAd$a:a	Z
      //   53: ifne +15 -> 68
      //   56: aload 5
      //   58: ifnonnull +10 -> 68
      //   61: aload_0
      //   62: getfield 18	com/admob/android/ads/InterstitialAd$a:b	Lcom/admob/android/ads/InterstitialAd;
      //   65: invokevirtual 60	com/admob/android/ads/InterstitialAd:c	()V
      //   68: return
      //   69: astore_3
      //   70: ldc 62
      //   72: bipush 6
      //   74: invokestatic 67	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
      //   77: ifeq +12 -> 89
      //   80: ldc 62
      //   82: ldc 69
      //   84: aload_3
      //   85: invokestatic 74	android/util/Log:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
      //   88: pop
      //   89: aload_0
      //   90: getfield 27	com/admob/android/ads/InterstitialAd$a:a	Z
      //   93: ifne -25 -> 68
      //   96: aload_0
      //   97: getfield 18	com/admob/android/ads/InterstitialAd$a:b	Lcom/admob/android/ads/InterstitialAd;
      //   100: invokevirtual 60	com/admob/android/ads/InterstitialAd:c	()V
      //   103: return
      //   104: astore_2
      //   105: aload_2
      //   106: athrow
      //   107: aload_0
      //   108: getfield 27	com/admob/android/ads/InterstitialAd$a:a	Z
      //   111: ifne -43 -> 68
      //   114: aload_0
      //   115: getfield 18	com/admob/android/ads/InterstitialAd$a:b	Lcom/admob/android/ads/InterstitialAd;
      //   118: invokevirtual 60	com/admob/android/ads/InterstitialAd:c	()V
      //   121: return
      //
      // Exception table:
      //   from	to	target	type
      //   15	56	69	java/lang/Exception
      //   61	68	69	java/lang/Exception
      //   15	56	104	finally
      //   61	68	104	finally
      //   70	89	104	finally
      //   89	103	104	finally
    }
  }

  static final class b
    implements Runnable
  {
    private WeakReference<InterstitialAd> a;

    public b(InterstitialAd paramInterstitialAd)
    {
      this.a = new WeakReference(paramInterstitialAd);
    }

    public final void run()
    {
      InterstitialAd localInterstitialAd = (InterstitialAd)this.a.get();
      if (localInterstitialAd != null)
        localInterstitialAd.d();
    }
  }

  public static class c
    implements m
  {
    InterstitialAd a;

    public c()
    {
    }

    public c(InterstitialAd paramInterstitialAd)
    {
      this.a = paramInterstitialAd;
    }

    public static boolean a(String paramString, int paramInt)
    {
      if (paramInt >= 5);
      for (int i = 1; (i != 0) || (Log.isLoggable(paramString, paramInt)); i = 0)
        return true;
      return false;
    }

    public final void a()
    {
      if (this.a != null)
        this.a.c();
    }

    public final void a(j paramj)
    {
      if (this.a != null)
      {
        InterstitialAd.a(this.a, paramj);
        this.a.a();
      }
    }
  }

  static final class d
    implements Runnable
  {
    private WeakReference<Activity> a;
    private WeakReference<InterstitialAd> b;

    public d(Activity paramActivity, InterstitialAd paramInterstitialAd)
    {
      this.a = new WeakReference(paramActivity);
      this.b = new WeakReference(paramInterstitialAd);
    }

    public final void run()
    {
      Activity localActivity = (Activity)this.a.get();
      InterstitialAd localInterstitialAd = (InterstitialAd)this.b.get();
      if ((localActivity != null) && (localInterstitialAd != null))
        localInterstitialAd.a(localActivity);
    }
  }

  static final class e
    implements Runnable
  {
    private WeakReference<InterstitialAd> a;

    public e(InterstitialAd paramInterstitialAd)
    {
      this.a = new WeakReference(paramInterstitialAd);
    }

    public final void run()
    {
      InterstitialAd localInterstitialAd = (InterstitialAd)this.a.get();
      if (localInterstitialAd != null)
        localInterstitialAd.b();
    }
  }

  static final class f extends TimerTask
  {
    private WeakReference<InterstitialAd> a;

    public f(InterstitialAd paramInterstitialAd)
    {
      this.a = new WeakReference(paramInterstitialAd);
    }

    public final void run()
    {
      InterstitialAd localInterstitialAd = (InterstitialAd)this.a.get();
      if (localInterstitialAd != null)
        InterstitialAd.a(localInterstitialAd);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */