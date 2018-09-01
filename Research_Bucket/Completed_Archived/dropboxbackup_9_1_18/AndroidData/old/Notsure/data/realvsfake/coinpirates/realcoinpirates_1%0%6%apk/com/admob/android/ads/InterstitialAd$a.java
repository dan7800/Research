package com.admob.android.ads;

import android.content.Context;
import java.lang.ref.WeakReference;

final class InterstitialAd$a extends Thread
{
  boolean a;
  private InterstitialAd b;
  private WeakReference<Context> c;

  public InterstitialAd$a(InterstitialAd paramInterstitialAd, Context paramContext)
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

/* Location:
 * Qualified Name:     com.admob.android.ads.InterstitialAd.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */