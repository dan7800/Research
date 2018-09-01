package com.admob.android.ads;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public final class u
  implements h
{
  public Hashtable<String, Bitmap> a = new Hashtable();
  public HashSet<e> b = new HashSet();
  public s c = null;
  public WeakReference<Context> d;
  private a e;

  public u(a parama)
  {
    this.e = parama;
    this.d = null;
  }

  private void a(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
  {
    e locale = g.a(paramString1, paramString2, paramString3, this);
    if (paramBoolean)
      locale.a(Boolean.valueOf(true));
    this.b.add(locale);
  }

  // ERROR //
  public final void a(e parame)
  {
    // Byte code:
    //   0: aload_1
    //   1: invokeinterface 67 1 0
    //   6: astore_2
    //   7: aload_1
    //   8: invokeinterface 70 1 0
    //   13: astore_3
    //   14: aload_3
    //   15: ifnull +208 -> 223
    //   18: aload_3
    //   19: iconst_0
    //   20: aload_3
    //   21: arraylength
    //   22: invokestatic 76	android/graphics/BitmapFactory:decodeByteArray	([BII)Landroid/graphics/Bitmap;
    //   25: astore 15
    //   27: aload 15
    //   29: astore 7
    //   31: aload 7
    //   33: ifnull +146 -> 179
    //   36: aload_1
    //   37: invokeinterface 80 1 0
    //   42: astore 10
    //   44: aload 10
    //   46: instanceof 48
    //   49: ifeq +24 -> 73
    //   52: aload 10
    //   54: checkcast 48	java/lang/Boolean
    //   57: invokevirtual 84	java/lang/Boolean:booleanValue	()Z
    //   60: ifeq +13 -> 73
    //   63: aload_0
    //   64: getfield 36	com/admob/android/ads/u:c	Lcom/admob/android/ads/s;
    //   67: aload_2
    //   68: aload 7
    //   70: invokevirtual 89	com/admob/android/ads/s:a	(Ljava/lang/String;Landroid/graphics/Bitmap;)V
    //   73: aload_0
    //   74: getfield 29	com/admob/android/ads/u:a	Ljava/util/Hashtable;
    //   77: aload_2
    //   78: aload 7
    //   80: invokevirtual 93	java/util/Hashtable:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   83: pop
    //   84: aload_0
    //   85: getfield 34	com/admob/android/ads/u:b	Ljava/util/HashSet;
    //   88: ifnull +47 -> 135
    //   91: aload_0
    //   92: getfield 34	com/admob/android/ads/u:b	Ljava/util/HashSet;
    //   95: astore 12
    //   97: aload 12
    //   99: monitorenter
    //   100: aload_0
    //   101: getfield 34	com/admob/android/ads/u:b	Ljava/util/HashSet;
    //   104: aload_1
    //   105: invokevirtual 96	java/util/HashSet:remove	(Ljava/lang/Object;)Z
    //   108: pop
    //   109: aload 12
    //   111: monitorexit
    //   112: aload_0
    //   113: invokevirtual 98	com/admob/android/ads/u:a	()Z
    //   116: ifeq +19 -> 135
    //   119: aload_0
    //   120: getfield 38	com/admob/android/ads/u:e	Lcom/admob/android/ads/u$a;
    //   123: ifnull +12 -> 135
    //   126: aload_0
    //   127: getfield 38	com/admob/android/ads/u:e	Lcom/admob/android/ads/u$a;
    //   130: invokeinterface 103 1 0
    //   135: return
    //   136: astore 5
    //   138: ldc 105
    //   140: bipush 6
    //   142: invokestatic 110	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   145: istore 6
    //   147: aconst_null
    //   148: astore 7
    //   150: iload 6
    //   152: ifeq -121 -> 31
    //   155: ldc 105
    //   157: ldc 112
    //   159: aload 5
    //   161: invokestatic 117	android/util/Log:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   164: pop
    //   165: aconst_null
    //   166: astore 7
    //   168: goto -137 -> 31
    //   171: astore 13
    //   173: aload 12
    //   175: monitorexit
    //   176: aload 13
    //   178: athrow
    //   179: ldc 105
    //   181: iconst_3
    //   182: invokestatic 110	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   185: ifeq +33 -> 218
    //   188: ldc 105
    //   190: new 119	java/lang/StringBuilder
    //   193: dup
    //   194: invokespecial 120	java/lang/StringBuilder:<init>	()V
    //   197: ldc 122
    //   199: invokevirtual 126	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   202: aload_2
    //   203: invokevirtual 126	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   206: ldc 128
    //   208: invokevirtual 126	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   211: invokevirtual 131	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   214: invokestatic 134	android/util/Log:d	(Ljava/lang/String;Ljava/lang/String;)I
    //   217: pop
    //   218: aload_0
    //   219: invokevirtual 136	com/admob/android/ads/u:c	()V
    //   222: return
    //   223: ldc 105
    //   225: iconst_3
    //   226: invokestatic 110	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   229: ifeq +33 -> 262
    //   232: ldc 105
    //   234: new 119	java/lang/StringBuilder
    //   237: dup
    //   238: invokespecial 120	java/lang/StringBuilder:<init>	()V
    //   241: ldc 122
    //   243: invokevirtual 126	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   246: aload_2
    //   247: invokevirtual 126	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   250: ldc 138
    //   252: invokevirtual 126	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   255: invokevirtual 131	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   258: invokestatic 134	android/util/Log:d	(Ljava/lang/String;Ljava/lang/String;)I
    //   261: pop
    //   262: aload_0
    //   263: invokevirtual 136	com/admob/android/ads/u:c	()V
    //   266: return
    //
    // Exception table:
    //   from	to	target	type
    //   18	27	136	java/lang/Throwable
    //   100	112	171	finally
  }

  public final void a(e parame, Exception paramException)
  {
    String str7;
    String str6;
    String str5;
    if (paramException != null)
      if (InterstitialAd.c.a("AdMobSDK", 3))
      {
        if (parame == null)
          break label208;
        str7 = parame.b();
        URL localURL2 = parame.c();
        if (localURL2 == null)
          break label198;
        String str8 = localURL2.toString();
        str6 = str7;
        str5 = str8;
      }
    while (true)
    {
      Log.d("AdMobSDK", "Failed downloading assets for ad: " + str6 + " " + str5, paramException);
      do
      {
        c();
        return;
      }
      while (!InterstitialAd.c.a("AdMobSDK", 3));
      String str3;
      String str2;
      String str1;
      if (parame != null)
      {
        str3 = parame.b();
        URL localURL1 = parame.c();
        if (localURL1 != null)
        {
          String str4 = localURL1.toString();
          str2 = str3;
          str1 = str4;
        }
      }
      while (true)
      {
        Log.d("AdMobSDK", "Failed downloading assets for ad: " + str2 + " " + str1);
        break;
        str2 = str3;
        str1 = null;
        continue;
        str1 = null;
        str2 = null;
      }
      label198: str6 = str7;
      str5 = null;
      continue;
      label208: str5 = null;
      str6 = null;
    }
  }

  public final void a(JSONObject paramJSONObject, String paramString)
    throws JSONException
  {
    HashSet localHashSet;
    if (this.b != null)
    {
      localHashSet = this.b;
      if (paramJSONObject != null)
        while (true)
        {
          try
          {
            Iterator localIterator = paramJSONObject.keys();
            if (!localIterator.hasNext())
              break;
            str1 = (String)localIterator.next();
            localJSONObject = paramJSONObject.getJSONObject(str1);
            str2 = localJSONObject.getString("u");
            if (localJSONObject.optInt("c", 0) == 1)
            {
              i = 1;
              if ((i == 0) || (this.c == null))
                break label147;
              localBitmap = this.c.a(str1);
              if (localBitmap == null)
                break label134;
              this.a.put(str1, localBitmap);
              continue;
            }
          }
          finally
          {
          }
          String str1;
          String str2;
          while (true)
          {
            JSONObject localJSONObject;
            Bitmap localBitmap;
            int i = 0;
          }
          label134: a(str2, str1, paramString, true);
          continue;
          label147: a(str2, str1, paramString, false);
        }
    }
  }

  public final boolean a()
  {
    return (this.b == null) || (this.b.size() == 0);
  }

  public final void b()
  {
    if (this.b != null)
      synchronized (this.b)
      {
        Iterator localIterator = this.b.iterator();
        if (localIterator.hasNext())
          ((e)localIterator.next()).f();
      }
  }

  public final void c()
  {
    if (this.b != null)
    {
      synchronized (this.b)
      {
        Iterator localIterator = this.b.iterator();
        if (localIterator.hasNext())
          ((e)localIterator.next()).e();
      }
      this.b.clear();
      this.b = null;
    }
    d();
    if (this.e != null)
      this.e.l();
  }

  public final void d()
  {
    if (this.a != null)
    {
      this.a.clear();
      this.a = null;
    }
  }

  public static abstract interface a
  {
    public abstract void k();

    public abstract void l();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.u
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */