package com.nubee.coinpirates.common;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HttpRequestHelper
{
  private final int EXCEPTION = -3;
  private final int GETDATA_ERROR = -6;
  private final String HOST_HTTPS = "https";
  private final int HOST_HTTPS_PORT = 443;
  private final int HOST_HTTP_PORT = 80;
  private final int PARAMS_ERROR = -5;
  private final int TIMEOUT = -2;
  private final int UNKNOWN_HOST = -1;
  private String URL = null;
  private final int URL_ERROR = -4;
  private Map<String, String> fileList = new HashMap();
  private InputStream inputStreamXml = null;
  private List<NameValuePair> params = null;
  private HttpResponse response = null;
  private int statusCode = 0;

  public HttpRequestHelper(String paramString)
  {
    this.URL = paramString;
    this.params = new ArrayList();
    setParameter("gmtdiff", CommonConfig.getGmtDiff());
    setParameter("key", CommonConfig.getLanguageCode());
  }

  // ERROR //
  public boolean execute()
  {
    // Byte code:
    //   0: new 105	org/apache/http/impl/client/DefaultHttpClient
    //   3: dup
    //   4: invokespecial 106	org/apache/http/impl/client/DefaultHttpClient:<init>	()V
    //   7: astore_1
    //   8: new 108	org/apache/http/client/methods/HttpPost
    //   11: dup
    //   12: aload_0
    //   13: getfield 42	com/nubee/coinpirates/common/HttpRequestHelper:URL	Ljava/lang/String;
    //   16: invokespecial 110	org/apache/http/client/methods/HttpPost:<init>	(Ljava/lang/String;)V
    //   19: astore_2
    //   20: aload_2
    //   21: new 112	org/apache/http/client/entity/UrlEncodedFormEntity
    //   24: dup
    //   25: aload_0
    //   26: getfield 44	com/nubee/coinpirates/common/HttpRequestHelper:params	Ljava/util/List;
    //   29: ldc 114
    //   31: invokespecial 117	org/apache/http/client/entity/UrlEncodedFormEntity:<init>	(Ljava/util/List;Ljava/lang/String;)V
    //   34: invokevirtual 121	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
    //   37: ldc 123
    //   39: astore 5
    //   41: ldc 123
    //   43: astore 6
    //   45: ldc 123
    //   47: astore 7
    //   49: iconst_0
    //   50: istore 8
    //   52: aload_0
    //   53: getfield 44	com/nubee/coinpirates/common/HttpRequestHelper:params	Ljava/util/List;
    //   56: invokeinterface 129 1 0
    //   61: istore 9
    //   63: iload 8
    //   65: iload 9
    //   67: if_icmplt +361 -> 428
    //   70: ldc 131
    //   72: new 133	java/lang/StringBuilder
    //   75: dup
    //   76: aload_0
    //   77: getfield 42	com/nubee/coinpirates/common/HttpRequestHelper:URL	Ljava/lang/String;
    //   80: invokestatic 139	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   83: invokespecial 140	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   86: ldc 142
    //   88: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   91: ldc 148
    //   93: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   96: aload 5
    //   98: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   101: aload 6
    //   103: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   106: ldc 150
    //   108: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   111: aload 7
    //   113: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   116: invokevirtual 153	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   119: invokestatic 158	com/nubee/coinpirates/common/Coins7Log:d	(Ljava/lang/String;Ljava/lang/String;)V
    //   122: aload_0
    //   123: getfield 55	com/nubee/coinpirates/common/HttpRequestHelper:fileList	Ljava/util/Map;
    //   126: invokeinterface 161 1 0
    //   131: ifeq +65 -> 196
    //   134: new 163	org/apache/http/entity/mime/MultipartEntity
    //   137: dup
    //   138: invokespecial 164	org/apache/http/entity/mime/MultipartEntity:<init>	()V
    //   141: astore 10
    //   143: iconst_0
    //   144: istore 11
    //   146: aload_0
    //   147: getfield 44	com/nubee/coinpirates/common/HttpRequestHelper:params	Ljava/util/List;
    //   150: invokeinterface 129 1 0
    //   155: istore 12
    //   157: iload 11
    //   159: iload 12
    //   161: if_icmplt +401 -> 562
    //   164: aload_0
    //   165: getfield 55	com/nubee/coinpirates/common/HttpRequestHelper:fileList	Ljava/util/Map;
    //   168: invokeinterface 168 1 0
    //   173: invokeinterface 174 1 0
    //   178: astore 13
    //   180: aload 13
    //   182: invokeinterface 179 1 0
    //   187: ifne +469 -> 656
    //   190: aload_2
    //   191: aload 10
    //   193: invokevirtual 121	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
    //   196: aload_1
    //   197: invokevirtual 183	org/apache/http/impl/client/DefaultHttpClient:getParams	()Lorg/apache/http/params/HttpParams;
    //   200: sipush 10000
    //   203: invokestatic 189	org/apache/http/params/HttpConnectionParams:setConnectionTimeout	(Lorg/apache/http/params/HttpParams;I)V
    //   206: aload_1
    //   207: invokevirtual 183	org/apache/http/impl/client/DefaultHttpClient:getParams	()Lorg/apache/http/params/HttpParams;
    //   210: sipush 10000
    //   213: invokestatic 192	org/apache/http/params/HttpConnectionParams:setSoTimeout	(Lorg/apache/http/params/HttpParams;I)V
    //   216: new 194	java/net/URL
    //   219: dup
    //   220: aload_0
    //   221: getfield 42	com/nubee/coinpirates/common/HttpRequestHelper:URL	Ljava/lang/String;
    //   224: invokespecial 195	java/net/URL:<init>	(Ljava/lang/String;)V
    //   227: invokevirtual 198	java/net/URL:getProtocol	()Ljava/lang/String;
    //   230: astore 23
    //   232: new 194	java/net/URL
    //   235: dup
    //   236: aload_0
    //   237: getfield 42	com/nubee/coinpirates/common/HttpRequestHelper:URL	Ljava/lang/String;
    //   240: invokespecial 195	java/net/URL:<init>	(Ljava/lang/String;)V
    //   243: invokevirtual 201	java/net/URL:getHost	()Ljava/lang/String;
    //   246: astore 24
    //   248: new 194	java/net/URL
    //   251: dup
    //   252: aload_0
    //   253: getfield 42	com/nubee/coinpirates/common/HttpRequestHelper:URL	Ljava/lang/String;
    //   256: invokespecial 195	java/net/URL:<init>	(Ljava/lang/String;)V
    //   259: invokevirtual 204	java/net/URL:getPort	()I
    //   262: istore 25
    //   264: iload 25
    //   266: iconst_m1
    //   267: if_icmpne +18 -> 285
    //   270: aload 23
    //   272: ldc 34
    //   274: invokevirtual 208	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   277: ifeq +495 -> 772
    //   280: sipush 443
    //   283: istore 25
    //   285: new 210	org/apache/http/HttpHost
    //   288: dup
    //   289: aload 24
    //   291: iload 25
    //   293: aload 23
    //   295: invokespecial 213	org/apache/http/HttpHost:<init>	(Ljava/lang/String;ILjava/lang/String;)V
    //   298: astore 26
    //   300: aload_0
    //   301: aload_1
    //   302: aload 26
    //   304: aload_2
    //   305: invokevirtual 216	org/apache/http/impl/client/DefaultHttpClient:execute	(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;)Lorg/apache/http/HttpResponse;
    //   308: putfield 46	com/nubee/coinpirates/common/HttpRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   311: aload_0
    //   312: getfield 46	com/nubee/coinpirates/common/HttpRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   315: invokeinterface 222 1 0
    //   320: invokeinterface 228 1 0
    //   325: astore 27
    //   327: new 230	java/io/ByteArrayOutputStream
    //   330: dup
    //   331: invokespecial 231	java/io/ByteArrayOutputStream:<init>	()V
    //   334: astore 28
    //   336: sipush 1024
    //   339: newarray byte
    //   341: astore 29
    //   343: aload 27
    //   345: aload 29
    //   347: invokevirtual 237	java/io/InputStream:read	([B)I
    //   350: istore 30
    //   352: iload 30
    //   354: iconst_m1
    //   355: if_icmpne +424 -> 779
    //   358: aload_0
    //   359: new 239	java/io/ByteArrayInputStream
    //   362: dup
    //   363: aload 28
    //   365: invokevirtual 243	java/io/ByteArrayOutputStream:toByteArray	()[B
    //   368: invokespecial 246	java/io/ByteArrayInputStream:<init>	([B)V
    //   371: putfield 48	com/nubee/coinpirates/common/HttpRequestHelper:inputStreamXml	Ljava/io/InputStream;
    //   374: aload 27
    //   376: ifnull +8 -> 384
    //   379: aload 27
    //   381: invokevirtual 249	java/io/InputStream:close	()V
    //   384: aload 28
    //   386: ifnull +8 -> 394
    //   389: aload 28
    //   391: invokevirtual 250	java/io/ByteArrayOutputStream:close	()V
    //   394: aload_0
    //   395: aload_0
    //   396: getfield 46	com/nubee/coinpirates/common/HttpRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   399: invokeinterface 254 1 0
    //   404: invokeinterface 259 1 0
    //   409: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   412: aload_0
    //   413: getfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   416: istore 31
    //   418: iload 31
    //   420: sipush 200
    //   423: if_icmpne +378 -> 801
    //   426: iconst_1
    //   427: ireturn
    //   428: aload_0
    //   429: getfield 44	com/nubee/coinpirates/common/HttpRequestHelper:params	Ljava/util/List;
    //   432: iload 8
    //   434: invokeinterface 263 2 0
    //   439: checkcast 265	org/apache/http/NameValuePair
    //   442: astore 36
    //   444: aload 36
    //   446: invokeinterface 268 1 0
    //   451: astore 37
    //   453: aload 36
    //   455: invokeinterface 271 1 0
    //   460: astore 38
    //   462: aload 37
    //   464: ldc_w 273
    //   467: invokevirtual 208	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   470: ifeq +10 -> 480
    //   473: aload 38
    //   475: astore 7
    //   477: goto +375 -> 852
    //   480: aload 37
    //   482: ldc_w 275
    //   485: invokevirtual 208	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   488: ifeq +10 -> 498
    //   491: aload 38
    //   493: astore 5
    //   495: goto +357 -> 852
    //   498: aload 37
    //   500: ldc_w 277
    //   503: invokevirtual 208	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   506: ifne +346 -> 852
    //   509: aload 37
    //   511: ldc_w 279
    //   514: invokevirtual 208	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   517: ifne +335 -> 852
    //   520: new 133	java/lang/StringBuilder
    //   523: dup
    //   524: aload 6
    //   526: invokestatic 139	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   529: invokespecial 140	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   532: ldc_w 281
    //   535: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   538: aload 37
    //   540: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   543: ldc_w 283
    //   546: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   549: aload 38
    //   551: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   554: invokevirtual 153	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   557: astore 6
    //   559: goto +293 -> 852
    //   562: aload_0
    //   563: getfield 44	com/nubee/coinpirates/common/HttpRequestHelper:params	Ljava/util/List;
    //   566: iload 11
    //   568: invokeinterface 263 2 0
    //   573: checkcast 265	org/apache/http/NameValuePair
    //   576: astore 32
    //   578: aload 32
    //   580: invokeinterface 268 1 0
    //   585: astore 33
    //   587: aload 32
    //   589: invokeinterface 271 1 0
    //   594: astore 34
    //   596: ldc 131
    //   598: new 133	java/lang/StringBuilder
    //   601: dup
    //   602: ldc_w 285
    //   605: invokespecial 140	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   608: aload 33
    //   610: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   613: ldc_w 287
    //   616: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   619: aload 34
    //   621: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   624: invokevirtual 153	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   627: invokestatic 158	com/nubee/coinpirates/common/Coins7Log:d	(Ljava/lang/String;Ljava/lang/String;)V
    //   630: new 289	org/apache/http/entity/mime/content/StringBody
    //   633: dup
    //   634: aload 34
    //   636: invokespecial 290	org/apache/http/entity/mime/content/StringBody:<init>	(Ljava/lang/String;)V
    //   639: astore 35
    //   641: aload 10
    //   643: aload 33
    //   645: aload 35
    //   647: invokevirtual 294	org/apache/http/entity/mime/MultipartEntity:addPart	(Ljava/lang/String;Lorg/apache/http/entity/mime/content/ContentBody;)V
    //   650: iinc 11 1
    //   653: goto -507 -> 146
    //   656: aload 13
    //   658: invokeinterface 298 1 0
    //   663: checkcast 135	java/lang/String
    //   666: astore 14
    //   668: aload_0
    //   669: getfield 55	com/nubee/coinpirates/common/HttpRequestHelper:fileList	Ljava/util/Map;
    //   672: aload 14
    //   674: invokeinterface 301 2 0
    //   679: checkcast 135	java/lang/String
    //   682: astore 15
    //   684: new 303	java/io/File
    //   687: dup
    //   688: aload 15
    //   690: invokespecial 304	java/io/File:<init>	(Ljava/lang/String;)V
    //   693: astore 16
    //   695: ldc 131
    //   697: new 133	java/lang/StringBuilder
    //   700: dup
    //   701: ldc_w 285
    //   704: invokespecial 140	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   707: aload 14
    //   709: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   712: ldc_w 287
    //   715: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   718: aload 15
    //   720: invokevirtual 146	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   723: invokevirtual 153	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   726: invokestatic 158	com/nubee/coinpirates/common/Coins7Log:d	(Ljava/lang/String;Ljava/lang/String;)V
    //   729: new 306	org/apache/http/entity/mime/content/FileBody
    //   732: dup
    //   733: aload 16
    //   735: invokespecial 309	org/apache/http/entity/mime/content/FileBody:<init>	(Ljava/io/File;)V
    //   738: astore 17
    //   740: aload 10
    //   742: aload 14
    //   744: aload 17
    //   746: invokevirtual 294	org/apache/http/entity/mime/MultipartEntity:addPart	(Ljava/lang/String;Lorg/apache/http/entity/mime/content/ContentBody;)V
    //   749: goto -569 -> 180
    //   752: astore 4
    //   754: aload_0
    //   755: bipush 252
    //   757: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   760: iconst_0
    //   761: ireturn
    //   762: astore 40
    //   764: aload_0
    //   765: bipush 251
    //   767: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   770: iconst_0
    //   771: ireturn
    //   772: bipush 80
    //   774: istore 25
    //   776: goto -491 -> 285
    //   779: aload 28
    //   781: aload 29
    //   783: iconst_0
    //   784: iload 30
    //   786: invokevirtual 313	java/io/ByteArrayOutputStream:write	([BII)V
    //   789: goto -446 -> 343
    //   792: astore 22
    //   794: aload_0
    //   795: iconst_m1
    //   796: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   799: iconst_0
    //   800: ireturn
    //   801: iconst_0
    //   802: ireturn
    //   803: astore 21
    //   805: aload_0
    //   806: bipush 254
    //   808: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   811: iconst_0
    //   812: ireturn
    //   813: astore 20
    //   815: aload_0
    //   816: bipush 253
    //   818: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   821: iconst_0
    //   822: ireturn
    //   823: astore 19
    //   825: aload_0
    //   826: bipush 253
    //   828: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   831: iconst_0
    //   832: ireturn
    //   833: astore 18
    //   835: aload_0
    //   836: bipush 250
    //   838: putfield 50	com/nubee/coinpirates/common/HttpRequestHelper:statusCode	I
    //   841: iconst_0
    //   842: ireturn
    //   843: astore_3
    //   844: goto -80 -> 764
    //   847: astore 39
    //   849: goto -95 -> 754
    //   852: iinc 8 1
    //   855: goto -803 -> 52
    //
    // Exception table:
    //   from	to	target	type
    //   20	37	752	java/lang/IllegalArgumentException
    //   52	63	752	java/lang/IllegalArgumentException
    //   70	143	752	java/lang/IllegalArgumentException
    //   146	157	752	java/lang/IllegalArgumentException
    //   164	180	752	java/lang/IllegalArgumentException
    //   180	196	752	java/lang/IllegalArgumentException
    //   428	473	752	java/lang/IllegalArgumentException
    //   480	491	752	java/lang/IllegalArgumentException
    //   498	559	752	java/lang/IllegalArgumentException
    //   562	650	752	java/lang/IllegalArgumentException
    //   656	749	752	java/lang/IllegalArgumentException
    //   8	20	762	java/io/UnsupportedEncodingException
    //   196	264	792	org/apache/http/NoHttpResponseException
    //   270	280	792	org/apache/http/NoHttpResponseException
    //   285	343	792	org/apache/http/NoHttpResponseException
    //   343	352	792	org/apache/http/NoHttpResponseException
    //   358	374	792	org/apache/http/NoHttpResponseException
    //   379	384	792	org/apache/http/NoHttpResponseException
    //   389	394	792	org/apache/http/NoHttpResponseException
    //   394	418	792	org/apache/http/NoHttpResponseException
    //   779	789	792	org/apache/http/NoHttpResponseException
    //   196	264	803	java/net/SocketTimeoutException
    //   270	280	803	java/net/SocketTimeoutException
    //   285	343	803	java/net/SocketTimeoutException
    //   343	352	803	java/net/SocketTimeoutException
    //   358	374	803	java/net/SocketTimeoutException
    //   379	384	803	java/net/SocketTimeoutException
    //   389	394	803	java/net/SocketTimeoutException
    //   394	418	803	java/net/SocketTimeoutException
    //   779	789	803	java/net/SocketTimeoutException
    //   196	264	813	org/apache/http/client/ClientProtocolException
    //   270	280	813	org/apache/http/client/ClientProtocolException
    //   285	343	813	org/apache/http/client/ClientProtocolException
    //   343	352	813	org/apache/http/client/ClientProtocolException
    //   358	374	813	org/apache/http/client/ClientProtocolException
    //   379	384	813	org/apache/http/client/ClientProtocolException
    //   389	394	813	org/apache/http/client/ClientProtocolException
    //   394	418	813	org/apache/http/client/ClientProtocolException
    //   779	789	813	org/apache/http/client/ClientProtocolException
    //   196	264	823	java/io/IOException
    //   270	280	823	java/io/IOException
    //   285	343	823	java/io/IOException
    //   343	352	823	java/io/IOException
    //   358	374	823	java/io/IOException
    //   379	384	823	java/io/IOException
    //   389	394	823	java/io/IOException
    //   394	418	823	java/io/IOException
    //   779	789	823	java/io/IOException
    //   196	264	833	java/lang/IllegalStateException
    //   270	280	833	java/lang/IllegalStateException
    //   285	343	833	java/lang/IllegalStateException
    //   343	352	833	java/lang/IllegalStateException
    //   358	374	833	java/lang/IllegalStateException
    //   379	384	833	java/lang/IllegalStateException
    //   389	394	833	java/lang/IllegalStateException
    //   394	418	833	java/lang/IllegalStateException
    //   779	789	833	java/lang/IllegalStateException
    //   20	37	843	java/io/UnsupportedEncodingException
    //   52	63	843	java/io/UnsupportedEncodingException
    //   70	143	843	java/io/UnsupportedEncodingException
    //   146	157	843	java/io/UnsupportedEncodingException
    //   164	180	843	java/io/UnsupportedEncodingException
    //   180	196	843	java/io/UnsupportedEncodingException
    //   428	473	843	java/io/UnsupportedEncodingException
    //   480	491	843	java/io/UnsupportedEncodingException
    //   498	559	843	java/io/UnsupportedEncodingException
    //   562	650	843	java/io/UnsupportedEncodingException
    //   656	749	843	java/io/UnsupportedEncodingException
    //   8	20	847	java/lang/IllegalArgumentException
  }

  public InputStream getResponse()
  {
    return this.inputStreamXml;
  }

  public int getStatusCode()
  {
    return this.statusCode;
  }

  public void setParameter(String paramString1, String paramString2)
  {
    Coins7Log.d("SNS", "key:" + paramString1 + " value:" + paramString2);
    this.params.add(new BasicNameValuePair(paramString1, paramString2));
  }

  public void setParameterMaltipart(String paramString1, String paramString2)
  {
    this.fileList.put(paramString1, paramString2);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.HttpRequestHelper
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */