package com.admob.android.ads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.File;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.PriorityQueue;

public final class s
{
  private static s a = null;
  private File b;
  private long c = 0L;
  private long d = 524288L;
  private PriorityQueue<File> e = null;
  private Hashtable<String, File> f = null;

  private s(Context paramContext, long paramLong)
  {
    File localFile = new File(paramContext.getCacheDir(), "admob_img_cache");
    if (!localFile.exists())
      localFile.mkdir();
    while (true)
    {
      this.b = localFile;
      a(this.b);
      return;
      if (!localFile.isDirectory())
      {
        localFile.delete();
        localFile.mkdir();
      }
    }
  }

  public static s a(Context paramContext)
  {
    if (a == null)
      a = new s(paramContext, 524288L);
    return a;
  }

  public static void a()
  {
    new a(a).start();
  }

  private void a(File paramFile)
  {
    File[] arrayOfFile = paramFile.listFiles();
    this.e = new PriorityQueue(20, new b());
    this.f = new Hashtable();
    int i = arrayOfFile.length;
    for (int j = 0; j < i; j++)
    {
      File localFile = arrayOfFile[j];
      if ((localFile != null) && (localFile.canRead()))
      {
        this.e.add(localFile);
        this.f.put(localFile.getName(), localFile);
        this.c += localFile.length();
      }
    }
  }

  private void b()
  {
    try
    {
      while ((this.c > this.d) && (this.e.size() > 0))
      {
        File localFile = (File)this.e.peek();
        if (InterstitialAd.c.a("AdMobSDK", 2))
          Log.v("AdMobSDK", "cache: evicting bitmap " + localFile.getName() + " totalBytes " + this.c);
        b(localFile);
        localFile.delete();
      }
    }
    finally
    {
    }
  }

  private void b(File paramFile)
  {
    if (paramFile != null)
      try
      {
        boolean bool1 = this.e.remove(paramFile);
        if (this.f.remove(paramFile.getName()) != null);
        for (boolean bool2 = true; ; bool2 = false)
        {
          if ((bool1 & bool2))
          {
            this.c -= paramFile.length();
            if (InterstitialAd.c.a("AdMobSDK", 2))
              Log.v("AdMobSDK", "Cache: removed file " + paramFile.getName() + " totalBytes " + this.c);
          }
          return;
        }
      }
      finally
      {
      }
  }

  private void c(File paramFile)
  {
    if (paramFile != null)
      try
      {
        if ((this.e.contains(paramFile)) || (this.f.get(paramFile.getName()) != null))
        {
          if (InterstitialAd.c.a("AdMobSDK", 2))
            Log.v("AdMobSDK", "Cache: trying to add a file that's already in index");
          b(paramFile);
        }
        this.e.add(paramFile);
        this.f.put(paramFile.getName(), paramFile);
        this.c += paramFile.length();
        if (InterstitialAd.c.a("AdMobSDK", 2))
          Log.v("AdMobSDK", "cache: added file: " + paramFile.getName() + " totalBytes " + this.c);
        return;
      }
      finally
      {
      }
  }

  public final Bitmap a(String paramString)
  {
    try
    {
      File localFile = (File)this.f.get(paramString);
      Bitmap localBitmap1;
      if (localFile != null)
      {
        localBitmap1 = BitmapFactory.decodeFile(localFile.getAbsolutePath());
        if (localBitmap1 != null)
        {
          this.e.remove(localFile);
          localFile.setLastModified(System.currentTimeMillis());
          this.e.add(localFile);
          if (InterstitialAd.c.a("AdMobSDK", 2))
            Log.v("AdMobSDK", "cache: found bitmap " + localFile.getName() + " totalBytes " + this.c + " new modified " + localFile.lastModified());
        }
      }
      for (Bitmap localBitmap2 = localBitmap1; ; localBitmap2 = null)
        return localBitmap2;
    }
    finally
    {
    }
  }

  // ERROR //
  public final void a(String paramString, Bitmap paramBitmap)
  {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: new 37	java/io/File
    //   5: dup
    //   6: aload_0
    //   7: getfield 57	com/admob/android/ads/s:b	Ljava/io/File;
    //   10: aload_1
    //   11: invokespecial 48	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   14: astore_3
    //   15: aload_0
    //   16: getfield 35	com/admob/android/ads/s:f	Ljava/util/Hashtable;
    //   19: aload_1
    //   20: invokevirtual 167	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   23: checkcast 37	java/io/File
    //   26: astore 5
    //   28: aload 5
    //   30: ifnull +51 -> 81
    //   33: ldc 123
    //   35: iconst_2
    //   36: invokestatic 128	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   39: ifeq +36 -> 75
    //   42: ldc 123
    //   44: new 130	java/lang/StringBuilder
    //   47: dup
    //   48: invokespecial 131	java/lang/StringBuilder:<init>	()V
    //   51: ldc 191
    //   53: invokevirtual 137	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   56: aload_3
    //   57: invokevirtual 105	java/io/File:getName	()Ljava/lang/String;
    //   60: invokevirtual 137	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   63: ldc 201
    //   65: invokevirtual 137	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   68: invokevirtual 145	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   71: invokestatic 151	android/util/Log:v	(Ljava/lang/String;Ljava/lang/String;)I
    //   74: pop
    //   75: aload_0
    //   76: aload 5
    //   78: invokespecial 153	com/admob/android/ads/s:b	(Ljava/io/File;)V
    //   81: aload_3
    //   82: invokevirtual 52	java/io/File:exists	()Z
    //   85: ifeq +8 -> 93
    //   88: aload_3
    //   89: invokevirtual 66	java/io/File:delete	()Z
    //   92: pop
    //   93: new 203	java/io/FileOutputStream
    //   96: dup
    //   97: aload_3
    //   98: invokespecial 205	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   101: astore 6
    //   103: aload_2
    //   104: getstatic 211	android/graphics/Bitmap$CompressFormat:PNG	Landroid/graphics/Bitmap$CompressFormat;
    //   107: bipush 100
    //   109: aload 6
    //   111: invokevirtual 217	android/graphics/Bitmap:compress	(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
    //   114: pop
    //   115: aload_0
    //   116: aload_3
    //   117: invokespecial 219	com/admob/android/ads/s:c	(Ljava/io/File;)V
    //   120: ldc 123
    //   122: iconst_2
    //   123: invokestatic 128	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   126: ifeq +55 -> 181
    //   129: ldc 123
    //   131: new 130	java/lang/StringBuilder
    //   134: dup
    //   135: invokespecial 131	java/lang/StringBuilder:<init>	()V
    //   138: ldc 221
    //   140: invokevirtual 137	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   143: aload_3
    //   144: invokevirtual 105	java/io/File:getName	()Ljava/lang/String;
    //   147: invokevirtual 137	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   150: ldc 139
    //   152: invokevirtual 137	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   155: aload_0
    //   156: getfield 31	com/admob/android/ads/s:c	J
    //   159: invokevirtual 142	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   162: ldc 223
    //   164: invokevirtual 137	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   167: aload_3
    //   168: invokevirtual 196	java/io/File:lastModified	()J
    //   171: invokevirtual 142	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   174: invokevirtual 145	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   177: invokestatic 151	android/util/Log:v	(Ljava/lang/String;Ljava/lang/String;)I
    //   180: pop
    //   181: aload_0
    //   182: monitorexit
    //   183: return
    //   184: astore 4
    //   186: aload_0
    //   187: monitorexit
    //   188: aload 4
    //   190: athrow
    //   191: astore 7
    //   193: goto -12 -> 181
    //
    // Exception table:
    //   from	to	target	type
    //   2	28	184	finally
    //   33	75	184	finally
    //   75	81	184	finally
    //   81	93	184	finally
    //   93	181	184	finally
    //   93	181	191	java/io/FileNotFoundException
  }

  static final class a extends Thread
  {
    private s a;

    private a(s params, byte paramByte)
    {
      this.a = params;
    }

    public final void run()
    {
      if (this.a != null)
        s.a(this.a);
    }
  }

  static final class b
    implements Comparator<File>
  {
    b()
    {
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.s
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */