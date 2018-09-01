package com.nubee.coinpirates.common;

import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HttpGuestRequestHelper
{
  public static final int EXCEPTION = -3;
  public static final int GETDATA_ERROR = -6;
  public static final int PARAMS_ERROR = -5;
  public static final int TIMEOUT = -2;
  public static final int UNKNOWN_HOST = -1;
  public static final int URL_ERROR = -4;
  private String URL = null;
  Map<String, String> fileList = new HashMap();
  InputStream inputStreamXml = null;
  private List<NameValuePair> params = null;
  HttpResponse response = null;
  private int statusCode = 0;

  public HttpGuestRequestHelper(String paramString)
  {
    this.URL = paramString;
    this.params = new ArrayList();
    setParameter("gmtdiff", CommonConfig.getGmtDiff());
    setParameter("key", Locale.getDefault().toString().substring(0, Locale.getDefault().toString().indexOf("_")));
    setParameter("language_code", Locale.getDefault().toString().substring(0, Locale.getDefault().toString().indexOf("_")));
  }

  public HttpGuestRequestHelper(String paramString1, String paramString2)
  {
    this(paramString1);
    setParameter("device_info", paramString2);
    setParameter("systemName", "Android");
    setParameter("contents_id", "6");
  }

  public void addDiaplaySize(String paramString)
  {
    setParameter("display_size", paramString);
  }

  public void addModel()
  {
    setParameter("machine", Build.MODEL);
  }

  public void addOsVersion()
  {
    setParameter("systemVersion", Build.VERSION.RELEASE);
  }

  // ERROR //
  public boolean execute()
  {
    // Byte code:
    //   0: new 139	org/apache/http/impl/client/DefaultHttpClient
    //   3: dup
    //   4: invokespecial 140	org/apache/http/impl/client/DefaultHttpClient:<init>	()V
    //   7: astore_1
    //   8: new 142	org/apache/http/client/methods/HttpPost
    //   11: dup
    //   12: aload_0
    //   13: getfield 37	com/nubee/coinpirates/common/HttpGuestRequestHelper:URL	Ljava/lang/String;
    //   16: invokespecial 143	org/apache/http/client/methods/HttpPost:<init>	(Ljava/lang/String;)V
    //   19: astore_2
    //   20: aload_2
    //   21: new 145	org/apache/http/client/entity/UrlEncodedFormEntity
    //   24: dup
    //   25: aload_0
    //   26: getfield 39	com/nubee/coinpirates/common/HttpGuestRequestHelper:params	Ljava/util/List;
    //   29: ldc 147
    //   31: invokespecial 150	org/apache/http/client/entity/UrlEncodedFormEntity:<init>	(Ljava/util/List;Ljava/lang/String;)V
    //   34: invokevirtual 154	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
    //   37: aload_0
    //   38: getfield 50	com/nubee/coinpirates/common/HttpGuestRequestHelper:fileList	Ljava/util/Map;
    //   41: invokeinterface 160 1 0
    //   46: ifeq +65 -> 111
    //   49: new 162	org/apache/http/entity/mime/MultipartEntity
    //   52: dup
    //   53: invokespecial 163	org/apache/http/entity/mime/MultipartEntity:<init>	()V
    //   56: astore 5
    //   58: iconst_0
    //   59: istore 6
    //   61: aload_0
    //   62: getfield 39	com/nubee/coinpirates/common/HttpGuestRequestHelper:params	Ljava/util/List;
    //   65: invokeinterface 166 1 0
    //   70: istore 7
    //   72: iload 6
    //   74: iload 7
    //   76: if_icmplt +267 -> 343
    //   79: aload_0
    //   80: getfield 50	com/nubee/coinpirates/common/HttpGuestRequestHelper:fileList	Ljava/util/Map;
    //   83: invokeinterface 170 1 0
    //   88: invokeinterface 176 1 0
    //   93: astore 8
    //   95: aload 8
    //   97: invokeinterface 181 1 0
    //   102: ifne +301 -> 403
    //   105: aload_2
    //   106: aload 5
    //   108: invokevirtual 154	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
    //   111: aload_1
    //   112: invokevirtual 185	org/apache/http/impl/client/DefaultHttpClient:getParams	()Lorg/apache/http/params/HttpParams;
    //   115: sipush 10000
    //   118: invokestatic 191	org/apache/http/params/HttpConnectionParams:setConnectionTimeout	(Lorg/apache/http/params/HttpParams;I)V
    //   121: aload_1
    //   122: invokevirtual 185	org/apache/http/impl/client/DefaultHttpClient:getParams	()Lorg/apache/http/params/HttpParams;
    //   125: sipush 10000
    //   128: invokestatic 194	org/apache/http/params/HttpConnectionParams:setSoTimeout	(Lorg/apache/http/params/HttpParams;I)V
    //   131: new 196	java/net/URL
    //   134: dup
    //   135: aload_0
    //   136: getfield 37	com/nubee/coinpirates/common/HttpGuestRequestHelper:URL	Ljava/lang/String;
    //   139: invokespecial 197	java/net/URL:<init>	(Ljava/lang/String;)V
    //   142: invokevirtual 200	java/net/URL:getProtocol	()Ljava/lang/String;
    //   145: astore 18
    //   147: new 196	java/net/URL
    //   150: dup
    //   151: aload_0
    //   152: getfield 37	com/nubee/coinpirates/common/HttpGuestRequestHelper:URL	Ljava/lang/String;
    //   155: invokespecial 197	java/net/URL:<init>	(Ljava/lang/String;)V
    //   158: invokevirtual 203	java/net/URL:getHost	()Ljava/lang/String;
    //   161: astore 19
    //   163: new 196	java/net/URL
    //   166: dup
    //   167: aload_0
    //   168: getfield 37	com/nubee/coinpirates/common/HttpGuestRequestHelper:URL	Ljava/lang/String;
    //   171: invokespecial 197	java/net/URL:<init>	(Ljava/lang/String;)V
    //   174: invokevirtual 206	java/net/URL:getPort	()I
    //   177: istore 20
    //   179: iload 20
    //   181: iconst_m1
    //   182: if_icmpne +18 -> 200
    //   185: aload 18
    //   187: ldc 208
    //   189: invokevirtual 212	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   192: ifeq +293 -> 485
    //   195: sipush 443
    //   198: istore 20
    //   200: new 214	org/apache/http/HttpHost
    //   203: dup
    //   204: aload 19
    //   206: iload 20
    //   208: aload 18
    //   210: invokespecial 217	org/apache/http/HttpHost:<init>	(Ljava/lang/String;ILjava/lang/String;)V
    //   213: astore 21
    //   215: aload_0
    //   216: aload_1
    //   217: aload 21
    //   219: aload_2
    //   220: invokevirtual 220	org/apache/http/impl/client/DefaultHttpClient:execute	(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;)Lorg/apache/http/HttpResponse;
    //   223: putfield 41	com/nubee/coinpirates/common/HttpGuestRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   226: aload_0
    //   227: getfield 41	com/nubee/coinpirates/common/HttpGuestRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   230: invokeinterface 226 1 0
    //   235: invokeinterface 232 1 0
    //   240: astore 22
    //   242: new 234	java/io/ByteArrayOutputStream
    //   245: dup
    //   246: invokespecial 235	java/io/ByteArrayOutputStream:<init>	()V
    //   249: astore 23
    //   251: sipush 1024
    //   254: newarray byte
    //   256: astore 24
    //   258: aload 22
    //   260: aload 24
    //   262: invokevirtual 241	java/io/InputStream:read	([B)I
    //   265: istore 25
    //   267: iload 25
    //   269: iconst_m1
    //   270: if_icmpne +222 -> 492
    //   273: aload_0
    //   274: new 243	java/io/ByteArrayInputStream
    //   277: dup
    //   278: aload 23
    //   280: invokevirtual 247	java/io/ByteArrayOutputStream:toByteArray	()[B
    //   283: invokespecial 250	java/io/ByteArrayInputStream:<init>	([B)V
    //   286: putfield 43	com/nubee/coinpirates/common/HttpGuestRequestHelper:inputStreamXml	Ljava/io/InputStream;
    //   289: aload 22
    //   291: ifnull +8 -> 299
    //   294: aload 22
    //   296: invokevirtual 253	java/io/InputStream:close	()V
    //   299: aload 23
    //   301: ifnull +8 -> 309
    //   304: aload 23
    //   306: invokevirtual 254	java/io/ByteArrayOutputStream:close	()V
    //   309: aload_0
    //   310: aload_0
    //   311: getfield 41	com/nubee/coinpirates/common/HttpGuestRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   314: invokeinterface 258 1 0
    //   319: invokeinterface 263 1 0
    //   324: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   327: aload_0
    //   328: getfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   331: istore 26
    //   333: iload 26
    //   335: sipush 200
    //   338: if_icmpne +176 -> 514
    //   341: iconst_1
    //   342: ireturn
    //   343: aload_0
    //   344: getfield 39	com/nubee/coinpirates/common/HttpGuestRequestHelper:params	Ljava/util/List;
    //   347: iload 6
    //   349: invokeinterface 267 2 0
    //   354: checkcast 269	org/apache/http/NameValuePair
    //   357: astore 27
    //   359: aload 27
    //   361: invokeinterface 272 1 0
    //   366: astore 28
    //   368: aload 27
    //   370: invokeinterface 275 1 0
    //   375: astore 29
    //   377: new 277	org/apache/http/entity/mime/content/StringBody
    //   380: dup
    //   381: aload 29
    //   383: invokespecial 278	org/apache/http/entity/mime/content/StringBody:<init>	(Ljava/lang/String;)V
    //   386: astore 30
    //   388: aload 5
    //   390: aload 28
    //   392: aload 30
    //   394: invokevirtual 282	org/apache/http/entity/mime/MultipartEntity:addPart	(Ljava/lang/String;Lorg/apache/http/entity/mime/content/ContentBody;)V
    //   397: iinc 6 1
    //   400: goto -339 -> 61
    //   403: aload 8
    //   405: invokeinterface 286 1 0
    //   410: checkcast 80	java/lang/String
    //   413: astore 9
    //   415: aload_0
    //   416: getfield 50	com/nubee/coinpirates/common/HttpGuestRequestHelper:fileList	Ljava/util/Map;
    //   419: aload 9
    //   421: invokeinterface 289 2 0
    //   426: checkcast 80	java/lang/String
    //   429: astore 10
    //   431: new 291	java/io/File
    //   434: dup
    //   435: aload 10
    //   437: invokespecial 292	java/io/File:<init>	(Ljava/lang/String;)V
    //   440: astore 11
    //   442: new 294	org/apache/http/entity/mime/content/FileBody
    //   445: dup
    //   446: aload 11
    //   448: invokespecial 297	org/apache/http/entity/mime/content/FileBody:<init>	(Ljava/io/File;)V
    //   451: astore 12
    //   453: aload 5
    //   455: aload 9
    //   457: aload 12
    //   459: invokevirtual 282	org/apache/http/entity/mime/MultipartEntity:addPart	(Ljava/lang/String;Lorg/apache/http/entity/mime/content/ContentBody;)V
    //   462: goto -367 -> 95
    //   465: astore 4
    //   467: aload_0
    //   468: bipush 252
    //   470: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   473: iconst_0
    //   474: ireturn
    //   475: astore 32
    //   477: aload_0
    //   478: bipush 251
    //   480: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   483: iconst_0
    //   484: ireturn
    //   485: bipush 80
    //   487: istore 20
    //   489: goto -289 -> 200
    //   492: aload 23
    //   494: aload 24
    //   496: iconst_0
    //   497: iload 25
    //   499: invokevirtual 301	java/io/ByteArrayOutputStream:write	([BII)V
    //   502: goto -244 -> 258
    //   505: astore 17
    //   507: aload_0
    //   508: iconst_m1
    //   509: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   512: iconst_0
    //   513: ireturn
    //   514: iconst_0
    //   515: ireturn
    //   516: astore 16
    //   518: aload_0
    //   519: bipush 254
    //   521: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   524: iconst_0
    //   525: ireturn
    //   526: astore 15
    //   528: aload_0
    //   529: bipush 253
    //   531: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   534: iconst_0
    //   535: ireturn
    //   536: astore 14
    //   538: aload_0
    //   539: bipush 253
    //   541: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   544: aload 14
    //   546: invokevirtual 304	java/io/IOException:printStackTrace	()V
    //   549: iconst_0
    //   550: ireturn
    //   551: astore 13
    //   553: aload_0
    //   554: bipush 250
    //   556: putfield 45	com/nubee/coinpirates/common/HttpGuestRequestHelper:statusCode	I
    //   559: iconst_0
    //   560: ireturn
    //   561: astore_3
    //   562: goto -85 -> 477
    //   565: astore 31
    //   567: goto -100 -> 467
    //
    // Exception table:
    //   from	to	target	type
    //   20	58	465	java/lang/IllegalArgumentException
    //   61	72	465	java/lang/IllegalArgumentException
    //   79	95	465	java/lang/IllegalArgumentException
    //   95	111	465	java/lang/IllegalArgumentException
    //   343	397	465	java/lang/IllegalArgumentException
    //   403	462	465	java/lang/IllegalArgumentException
    //   8	20	475	java/io/UnsupportedEncodingException
    //   111	179	505	org/apache/http/NoHttpResponseException
    //   185	195	505	org/apache/http/NoHttpResponseException
    //   200	258	505	org/apache/http/NoHttpResponseException
    //   258	267	505	org/apache/http/NoHttpResponseException
    //   273	289	505	org/apache/http/NoHttpResponseException
    //   294	299	505	org/apache/http/NoHttpResponseException
    //   304	309	505	org/apache/http/NoHttpResponseException
    //   309	333	505	org/apache/http/NoHttpResponseException
    //   492	502	505	org/apache/http/NoHttpResponseException
    //   111	179	516	java/net/SocketTimeoutException
    //   185	195	516	java/net/SocketTimeoutException
    //   200	258	516	java/net/SocketTimeoutException
    //   258	267	516	java/net/SocketTimeoutException
    //   273	289	516	java/net/SocketTimeoutException
    //   294	299	516	java/net/SocketTimeoutException
    //   304	309	516	java/net/SocketTimeoutException
    //   309	333	516	java/net/SocketTimeoutException
    //   492	502	516	java/net/SocketTimeoutException
    //   111	179	526	org/apache/http/client/ClientProtocolException
    //   185	195	526	org/apache/http/client/ClientProtocolException
    //   200	258	526	org/apache/http/client/ClientProtocolException
    //   258	267	526	org/apache/http/client/ClientProtocolException
    //   273	289	526	org/apache/http/client/ClientProtocolException
    //   294	299	526	org/apache/http/client/ClientProtocolException
    //   304	309	526	org/apache/http/client/ClientProtocolException
    //   309	333	526	org/apache/http/client/ClientProtocolException
    //   492	502	526	org/apache/http/client/ClientProtocolException
    //   111	179	536	java/io/IOException
    //   185	195	536	java/io/IOException
    //   200	258	536	java/io/IOException
    //   258	267	536	java/io/IOException
    //   273	289	536	java/io/IOException
    //   294	299	536	java/io/IOException
    //   304	309	536	java/io/IOException
    //   309	333	536	java/io/IOException
    //   492	502	536	java/io/IOException
    //   111	179	551	java/lang/IllegalStateException
    //   185	195	551	java/lang/IllegalStateException
    //   200	258	551	java/lang/IllegalStateException
    //   258	267	551	java/lang/IllegalStateException
    //   273	289	551	java/lang/IllegalStateException
    //   294	299	551	java/lang/IllegalStateException
    //   304	309	551	java/lang/IllegalStateException
    //   309	333	551	java/lang/IllegalStateException
    //   492	502	551	java/lang/IllegalStateException
    //   20	58	561	java/io/UnsupportedEncodingException
    //   61	72	561	java/io/UnsupportedEncodingException
    //   79	95	561	java/io/UnsupportedEncodingException
    //   95	111	561	java/io/UnsupportedEncodingException
    //   343	397	561	java/io/UnsupportedEncodingException
    //   403	462	561	java/io/UnsupportedEncodingException
    //   8	20	565	java/lang/IllegalArgumentException
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
    Log.e("LOGIN", String.valueOf(paramString1) + "=" + paramString2);
    this.params.add(new BasicNameValuePair(paramString1, paramString2));
  }

  public void setParameterMaltipart(String paramString1, String paramString2)
  {
    this.fileList.put(paramString1, paramString2);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.HttpGuestRequestHelper
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */