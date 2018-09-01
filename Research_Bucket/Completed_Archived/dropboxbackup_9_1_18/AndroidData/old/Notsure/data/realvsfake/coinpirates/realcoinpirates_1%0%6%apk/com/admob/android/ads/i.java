package com.admob.android.ads;

import android.util.Log;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

final class i extends f
{
  private HttpURLConnection m;
  private URL n;

  public i(String paramString1, String paramString2, String paramString3, h paramh, int paramInt, Map<String, String> paramMap, String paramString4)
  {
    super(paramString2, paramString3, paramh, paramInt, paramMap, paramString4);
    try
    {
      this.n = new URL(paramString1);
      this.i = this.n;
      this.m = null;
      this.e = 0;
      return;
    }
    catch (MalformedURLException localMalformedURLException)
    {
      while (true)
      {
        this.n = null;
        this.c = localMalformedURLException;
      }
    }
  }

  private void i()
  {
    if (this.m != null)
    {
      this.m.disconnect();
      this.m = null;
    }
  }

  // ERROR //
  public final boolean d()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 22	com/admob/android/ads/i:n	Ljava/net/URL;
    //   4: ifnonnull +882 -> 886
    //   7: aload_0
    //   8: getfield 49	com/admob/android/ads/i:h	Lcom/admob/android/ads/h;
    //   11: ifnull +22 -> 33
    //   14: aload_0
    //   15: getfield 49	com/admob/android/ads/i:h	Lcom/admob/android/ads/h;
    //   18: aload_0
    //   19: new 45	java/lang/Exception
    //   22: dup
    //   23: ldc 51
    //   25: invokespecial 52	java/lang/Exception:<init>	(Ljava/lang/String;)V
    //   28: invokeinterface 58 3 0
    //   33: iconst_0
    //   34: istore_2
    //   35: iload_2
    //   36: ifne +24 -> 60
    //   39: aload_0
    //   40: getfield 49	com/admob/android/ads/i:h	Lcom/admob/android/ads/h;
    //   43: ifnull +17 -> 60
    //   46: aload_0
    //   47: getfield 49	com/admob/android/ads/i:h	Lcom/admob/android/ads/h;
    //   50: aload_0
    //   51: aload_0
    //   52: getfield 35	com/admob/android/ads/i:c	Ljava/lang/Exception;
    //   55: invokeinterface 58 3 0
    //   60: iload_2
    //   61: ireturn
    //   62: iload 14
    //   64: sipush 302
    //   67: if_icmpne +802 -> 869
    //   70: aload_0
    //   71: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   74: ldc 60
    //   76: invokevirtual 64	java/net/HttpURLConnection:getHeaderField	(Ljava/lang/String;)Ljava/lang/String;
    //   79: astore 19
    //   81: ldc 66
    //   83: iconst_3
    //   84: invokestatic 71	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   87: ifeq +29 -> 116
    //   90: ldc 66
    //   92: new 73	java/lang/StringBuilder
    //   95: dup
    //   96: invokespecial 75	java/lang/StringBuilder:<init>	()V
    //   99: ldc 77
    //   101: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   104: aload 19
    //   106: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   109: invokevirtual 85	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   112: invokestatic 90	android/util/Log:d	(Ljava/lang/String;Ljava/lang/String;)I
    //   115: pop
    //   116: aload_0
    //   117: new 17	java/net/URL
    //   120: dup
    //   121: aload 19
    //   123: invokespecial 20	java/net/URL:<init>	(Ljava/lang/String;)V
    //   126: putfield 22	com/admob/android/ads/i:n	Ljava/net/URL;
    //   129: aload_0
    //   130: invokespecial 92	com/admob/android/ads/i:i	()V
    //   133: aload_0
    //   134: getfield 31	com/admob/android/ads/i:e	I
    //   137: aload_0
    //   138: getfield 95	com/admob/android/ads/i:f	I
    //   141: if_icmpge +740 -> 881
    //   144: iload_1
    //   145: ifne +736 -> 881
    //   148: ldc 66
    //   150: iconst_2
    //   151: invokestatic 71	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   154: ifeq +43 -> 197
    //   157: ldc 66
    //   159: new 73	java/lang/StringBuilder
    //   162: dup
    //   163: invokespecial 75	java/lang/StringBuilder:<init>	()V
    //   166: ldc 97
    //   168: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   171: aload_0
    //   172: getfield 31	com/admob/android/ads/i:e	I
    //   175: invokevirtual 100	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   178: ldc 102
    //   180: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   183: aload_0
    //   184: getfield 22	com/admob/android/ads/i:n	Ljava/net/URL;
    //   187: invokevirtual 105	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   190: invokevirtual 85	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   193: invokestatic 108	android/util/Log:v	(Ljava/lang/String;Ljava/lang/String;)I
    //   196: pop
    //   197: aload_0
    //   198: invokespecial 92	com/admob/android/ads/i:i	()V
    //   201: aload_0
    //   202: aload_0
    //   203: getfield 22	com/admob/android/ads/i:n	Ljava/net/URL;
    //   206: invokevirtual 112	java/net/URL:openConnection	()Ljava/net/URLConnection;
    //   209: checkcast 38	java/net/HttpURLConnection
    //   212: putfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   215: aload_0
    //   216: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   219: iconst_1
    //   220: invokevirtual 116	java/net/HttpURLConnection:setUseCaches	(Z)V
    //   223: aload_0
    //   224: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   227: iconst_1
    //   228: invokevirtual 119	java/net/HttpURLConnection:setInstanceFollowRedirects	(Z)V
    //   231: aload_0
    //   232: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   235: ifnull +640 -> 875
    //   238: aload_0
    //   239: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   242: ldc 121
    //   244: invokestatic 123	com/admob/android/ads/i:h	()Ljava/lang/String;
    //   247: invokevirtual 127	java/net/HttpURLConnection:setRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   250: aload_0
    //   251: getfield 131	com/admob/android/ads/i:g	Ljava/lang/String;
    //   254: ifnull +16 -> 270
    //   257: aload_0
    //   258: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   261: ldc 133
    //   263: aload_0
    //   264: getfield 131	com/admob/android/ads/i:g	Ljava/lang/String;
    //   267: invokevirtual 127	java/net/HttpURLConnection:setRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   270: aload_0
    //   271: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   274: aload_0
    //   275: getfield 136	com/admob/android/ads/i:b	I
    //   278: invokevirtual 140	java/net/HttpURLConnection:setConnectTimeout	(I)V
    //   281: aload_0
    //   282: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   285: aload_0
    //   286: getfield 136	com/admob/android/ads/i:b	I
    //   289: invokevirtual 143	java/net/HttpURLConnection:setReadTimeout	(I)V
    //   292: aload_0
    //   293: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   296: iconst_0
    //   297: invokevirtual 116	java/net/HttpURLConnection:setUseCaches	(Z)V
    //   300: aload_0
    //   301: getfield 146	com/admob/android/ads/i:d	Ljava/util/Map;
    //   304: ifnull +193 -> 497
    //   307: aload_0
    //   308: getfield 146	com/admob/android/ads/i:d	Ljava/util/Map;
    //   311: invokeinterface 152 1 0
    //   316: invokeinterface 158 1 0
    //   321: astore 23
    //   323: aload 23
    //   325: invokeinterface 163 1 0
    //   330: ifeq +167 -> 497
    //   333: aload 23
    //   335: invokeinterface 167 1 0
    //   340: checkcast 169	java/lang/String
    //   343: astore 24
    //   345: aload 24
    //   347: ifnull -24 -> 323
    //   350: aload_0
    //   351: getfield 146	com/admob/android/ads/i:d	Ljava/util/Map;
    //   354: aload 24
    //   356: invokeinterface 173 2 0
    //   361: checkcast 169	java/lang/String
    //   364: astore 25
    //   366: aload 25
    //   368: ifnull -45 -> 323
    //   371: aload_0
    //   372: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   375: aload 24
    //   377: aload 25
    //   379: invokevirtual 176	java/net/HttpURLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   382: goto -59 -> 323
    //   385: astore 6
    //   387: aconst_null
    //   388: astore 4
    //   390: ldc 66
    //   392: iconst_3
    //   393: invokestatic 71	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   396: ifeq +43 -> 439
    //   399: ldc 66
    //   401: new 73	java/lang/StringBuilder
    //   404: dup
    //   405: invokespecial 75	java/lang/StringBuilder:<init>	()V
    //   408: ldc 178
    //   410: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   413: aload_0
    //   414: getfield 31	com/admob/android/ads/i:e	I
    //   417: invokevirtual 100	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   420: ldc 180
    //   422: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   425: aload_0
    //   426: getfield 22	com/admob/android/ads/i:n	Ljava/net/URL;
    //   429: invokevirtual 105	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   432: invokevirtual 85	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   435: invokestatic 90	android/util/Log:d	(Ljava/lang/String;Ljava/lang/String;)I
    //   438: pop
    //   439: ldc 66
    //   441: iconst_2
    //   442: invokestatic 71	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   445: ifeq +13 -> 458
    //   448: ldc 66
    //   450: ldc 182
    //   452: aload 6
    //   454: invokestatic 185	android/util/Log:v	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   457: pop
    //   458: aload_0
    //   459: aload 6
    //   461: putfield 35	com/admob/android/ads/i:c	Ljava/lang/Exception;
    //   464: aload 4
    //   466: ifnull +8 -> 474
    //   469: aload 4
    //   471: invokevirtual 190	java/io/BufferedWriter:close	()V
    //   474: aload_0
    //   475: invokespecial 92	com/admob/android/ads/i:i	()V
    //   478: iconst_0
    //   479: istore 7
    //   481: aload_0
    //   482: iconst_1
    //   483: aload_0
    //   484: getfield 31	com/admob/android/ads/i:e	I
    //   487: iadd
    //   488: putfield 31	com/admob/android/ads/i:e	I
    //   491: iload 7
    //   493: istore_1
    //   494: goto -361 -> 133
    //   497: aload_0
    //   498: getfield 193	com/admob/android/ads/i:l	Ljava/lang/String;
    //   501: ifnull +264 -> 765
    //   504: aload_0
    //   505: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   508: ldc 195
    //   510: invokevirtual 198	java/net/HttpURLConnection:setRequestMethod	(Ljava/lang/String;)V
    //   513: aload_0
    //   514: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   517: iconst_1
    //   518: invokevirtual 201	java/net/HttpURLConnection:setDoOutput	(Z)V
    //   521: aload_0
    //   522: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   525: ldc 203
    //   527: aload_0
    //   528: getfield 205	com/admob/android/ads/i:a	Ljava/lang/String;
    //   531: invokevirtual 127	java/net/HttpURLConnection:setRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   534: aload_0
    //   535: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   538: ldc 207
    //   540: aload_0
    //   541: getfield 193	com/admob/android/ads/i:l	Ljava/lang/String;
    //   544: invokevirtual 211	java/lang/String:length	()I
    //   547: invokestatic 216	java/lang/Integer:toString	(I)Ljava/lang/String;
    //   550: invokevirtual 127	java/net/HttpURLConnection:setRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   553: new 187	java/io/BufferedWriter
    //   556: dup
    //   557: new 218	java/io/OutputStreamWriter
    //   560: dup
    //   561: aload_0
    //   562: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   565: invokevirtual 222	java/net/HttpURLConnection:getOutputStream	()Ljava/io/OutputStream;
    //   568: invokespecial 225	java/io/OutputStreamWriter:<init>	(Ljava/io/OutputStream;)V
    //   571: sipush 4096
    //   574: invokespecial 228	java/io/BufferedWriter:<init>	(Ljava/io/Writer;I)V
    //   577: astore 4
    //   579: aload 4
    //   581: aload_0
    //   582: getfield 193	com/admob/android/ads/i:l	Ljava/lang/String;
    //   585: invokevirtual 231	java/io/BufferedWriter:write	(Ljava/lang/String;)V
    //   588: aload 4
    //   590: invokevirtual 190	java/io/BufferedWriter:close	()V
    //   593: aload_0
    //   594: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   597: invokevirtual 234	java/net/HttpURLConnection:getResponseCode	()I
    //   600: istore 14
    //   602: ldc 66
    //   604: iconst_2
    //   605: invokestatic 71	com/admob/android/ads/InterstitialAd$c:a	(Ljava/lang/String;I)Z
    //   608: ifeq +45 -> 653
    //   611: aload_0
    //   612: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   615: ldc 236
    //   617: invokevirtual 64	java/net/HttpURLConnection:getHeaderField	(Ljava/lang/String;)Ljava/lang/String;
    //   620: astore 21
    //   622: aload 21
    //   624: ifnull +29 -> 653
    //   627: ldc 66
    //   629: new 73	java/lang/StringBuilder
    //   632: dup
    //   633: invokespecial 75	java/lang/StringBuilder:<init>	()V
    //   636: ldc 238
    //   638: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   641: aload 21
    //   643: invokevirtual 81	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   646: invokevirtual 85	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   649: invokestatic 108	android/util/Log:v	(Ljava/lang/String;Ljava/lang/String;)I
    //   652: pop
    //   653: iload 14
    //   655: sipush 200
    //   658: if_icmplt -596 -> 62
    //   661: iload 14
    //   663: sipush 300
    //   666: if_icmpge -604 -> 62
    //   669: aload_0
    //   670: aload_0
    //   671: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   674: invokevirtual 242	java/net/HttpURLConnection:getURL	()Ljava/net/URL;
    //   677: putfield 25	com/admob/android/ads/i:i	Ljava/net/URL;
    //   680: aload_0
    //   681: getfield 246	com/admob/android/ads/i:k	Z
    //   684: ifeq +100 -> 784
    //   687: new 248	java/io/BufferedInputStream
    //   690: dup
    //   691: aload_0
    //   692: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   695: invokevirtual 252	java/net/HttpURLConnection:getInputStream	()Ljava/io/InputStream;
    //   698: sipush 4096
    //   701: invokespecial 255	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;I)V
    //   704: astore 15
    //   706: sipush 4096
    //   709: newarray byte
    //   711: astore 16
    //   713: new 257	java/io/ByteArrayOutputStream
    //   716: dup
    //   717: sipush 4096
    //   720: invokespecial 259	java/io/ByteArrayOutputStream:<init>	(I)V
    //   723: astore 17
    //   725: aload 15
    //   727: aload 16
    //   729: invokevirtual 263	java/io/BufferedInputStream:read	([B)I
    //   732: istore 18
    //   734: iload 18
    //   736: iconst_m1
    //   737: if_icmpeq +38 -> 775
    //   740: aload 17
    //   742: aload 16
    //   744: iconst_0
    //   745: iload 18
    //   747: invokevirtual 266	java/io/ByteArrayOutputStream:write	([BII)V
    //   750: goto -25 -> 725
    //   753: astore 13
    //   755: aload 13
    //   757: astore 6
    //   759: aconst_null
    //   760: astore 4
    //   762: goto -372 -> 390
    //   765: aload_0
    //   766: getfield 27	com/admob/android/ads/i:m	Ljava/net/HttpURLConnection;
    //   769: invokevirtual 269	java/net/HttpURLConnection:connect	()V
    //   772: goto -179 -> 593
    //   775: aload_0
    //   776: aload 17
    //   778: invokevirtual 273	java/io/ByteArrayOutputStream:toByteArray	()[B
    //   781: putfield 277	com/admob/android/ads/i:j	[B
    //   784: aload_0
    //   785: getfield 49	com/admob/android/ads/i:h	Lcom/admob/android/ads/h;
    //   788: ifnull +103 -> 891
    //   791: aload_0
    //   792: getfield 49	com/admob/android/ads/i:h	Lcom/admob/android/ads/h;
    //   795: aload_0
    //   796: invokeinterface 280 2 0
    //   801: goto +90 -> 891
    //   804: aload_0
    //   805: invokespecial 92	com/admob/android/ads/i:i	()V
    //   808: aload_0
    //   809: invokespecial 92	com/admob/android/ads/i:i	()V
    //   812: iload 11
    //   814: istore 7
    //   816: goto -335 -> 481
    //   819: astore_3
    //   820: aconst_null
    //   821: astore 4
    //   823: aload 4
    //   825: ifnull +8 -> 833
    //   828: aload 4
    //   830: invokevirtual 190	java/io/BufferedWriter:close	()V
    //   833: aload_0
    //   834: invokespecial 92	com/admob/android/ads/i:i	()V
    //   837: aload_3
    //   838: athrow
    //   839: astore 8
    //   841: goto -367 -> 474
    //   844: astore 5
    //   846: goto -13 -> 833
    //   849: astore_3
    //   850: goto -27 -> 823
    //   853: astore 12
    //   855: aload 12
    //   857: astore_3
    //   858: aconst_null
    //   859: astore 4
    //   861: goto -38 -> 823
    //   864: astore 6
    //   866: goto -476 -> 390
    //   869: iload_1
    //   870: istore 11
    //   872: goto -68 -> 804
    //   875: iload_1
    //   876: istore 11
    //   878: goto -74 -> 804
    //   881: iload_1
    //   882: istore_2
    //   883: goto -848 -> 35
    //   886: iconst_0
    //   887: istore_1
    //   888: goto -755 -> 133
    //   891: iconst_1
    //   892: istore 11
    //   894: goto -90 -> 804
    //
    // Exception table:
    //   from	to	target	type
    //   197	270	385	java/lang/Exception
    //   270	323	385	java/lang/Exception
    //   323	345	385	java/lang/Exception
    //   350	366	385	java/lang/Exception
    //   371	382	385	java/lang/Exception
    //   497	579	385	java/lang/Exception
    //   765	772	385	java/lang/Exception
    //   70	116	753	java/lang/Exception
    //   116	129	753	java/lang/Exception
    //   593	622	753	java/lang/Exception
    //   627	653	753	java/lang/Exception
    //   669	725	753	java/lang/Exception
    //   725	734	753	java/lang/Exception
    //   740	750	753	java/lang/Exception
    //   775	784	753	java/lang/Exception
    //   784	801	753	java/lang/Exception
    //   804	808	753	java/lang/Exception
    //   197	270	819	finally
    //   270	323	819	finally
    //   323	345	819	finally
    //   350	366	819	finally
    //   371	382	819	finally
    //   497	579	819	finally
    //   765	772	819	finally
    //   469	474	839	java/lang/Exception
    //   828	833	844	java/lang/Exception
    //   390	439	849	finally
    //   439	458	849	finally
    //   458	464	849	finally
    //   579	593	849	finally
    //   70	116	853	finally
    //   116	129	853	finally
    //   593	622	853	finally
    //   627	653	853	finally
    //   669	725	853	finally
    //   725	734	853	finally
    //   740	750	853	finally
    //   775	784	853	finally
    //   784	801	853	finally
    //   804	808	853	finally
    //   579	593	864	java/lang/Exception
  }

  public final void e()
  {
    i();
    this.h = null;
  }

  public final void run()
  {
    try
    {
      d();
      return;
    }
    catch (Exception localException)
    {
      do
        if (InterstitialAd.c.a("AdMobSDK", 6))
          Log.e("AdMobSDK", "exception caught in AdMobURLConnector.run(), " + localException.getMessage());
      while (this.h == null);
      this.h.a(this, this.c);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.i
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */