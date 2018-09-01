package com.nubee.coinpirates.common;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HttpLoginRequestHelper
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
  private InputStream inputStreamXml = null;
  private List<NameValuePair> params = null;
  private HttpResponse response = null;
  private int statusCode = 0;

  public HttpLoginRequestHelper(String paramString)
  {
    this.URL = paramString;
    this.params = new ArrayList();
    setParameter("gmtdiff", CommonConfig.getGmtDiff());
    setParameter("language_code", CommonConfig.getLanguageCode());
  }

  // ERROR //
  public boolean execute()
  {
    // Byte code:
    //   0: new 97	org/apache/http/impl/client/DefaultHttpClient
    //   3: dup
    //   4: invokespecial 98	org/apache/http/impl/client/DefaultHttpClient:<init>	()V
    //   7: astore_1
    //   8: new 100	org/apache/http/client/methods/HttpPost
    //   11: dup
    //   12: aload_0
    //   13: getfield 39	com/nubee/coinpirates/common/HttpLoginRequestHelper:URL	Ljava/lang/String;
    //   16: invokespecial 102	org/apache/http/client/methods/HttpPost:<init>	(Ljava/lang/String;)V
    //   19: astore_2
    //   20: aload_2
    //   21: new 104	org/apache/http/client/entity/UrlEncodedFormEntity
    //   24: dup
    //   25: aload_0
    //   26: getfield 41	com/nubee/coinpirates/common/HttpLoginRequestHelper:params	Ljava/util/List;
    //   29: ldc 106
    //   31: invokespecial 109	org/apache/http/client/entity/UrlEncodedFormEntity:<init>	(Ljava/util/List;Ljava/lang/String;)V
    //   34: invokevirtual 113	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
    //   37: ldc 115
    //   39: astore 5
    //   41: ldc 115
    //   43: astore 6
    //   45: ldc 115
    //   47: astore 7
    //   49: iconst_0
    //   50: istore 8
    //   52: aload_0
    //   53: getfield 41	com/nubee/coinpirates/common/HttpLoginRequestHelper:params	Ljava/util/List;
    //   56: invokeinterface 121 1 0
    //   61: istore 9
    //   63: iload 8
    //   65: iload 9
    //   67: if_icmplt +287 -> 354
    //   70: ldc 123
    //   72: new 125	java/lang/StringBuilder
    //   75: dup
    //   76: aload_0
    //   77: getfield 39	com/nubee/coinpirates/common/HttpLoginRequestHelper:URL	Ljava/lang/String;
    //   80: invokestatic 131	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   83: invokespecial 132	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   86: ldc 134
    //   88: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   91: ldc 140
    //   93: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   96: aload 5
    //   98: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   101: aload 6
    //   103: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   106: ldc 142
    //   108: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   111: aload 7
    //   113: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   116: invokevirtual 145	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   119: invokestatic 150	com/nubee/coinpirates/common/Coins7Log:d	(Ljava/lang/String;Ljava/lang/String;)V
    //   122: aload_1
    //   123: invokevirtual 154	org/apache/http/impl/client/DefaultHttpClient:getParams	()Lorg/apache/http/params/HttpParams;
    //   126: sipush 10000
    //   129: invokestatic 160	org/apache/http/params/HttpConnectionParams:setConnectionTimeout	(Lorg/apache/http/params/HttpParams;I)V
    //   132: aload_1
    //   133: invokevirtual 154	org/apache/http/impl/client/DefaultHttpClient:getParams	()Lorg/apache/http/params/HttpParams;
    //   136: sipush 10000
    //   139: invokestatic 163	org/apache/http/params/HttpConnectionParams:setSoTimeout	(Lorg/apache/http/params/HttpParams;I)V
    //   142: new 165	java/net/URL
    //   145: dup
    //   146: aload_0
    //   147: getfield 39	com/nubee/coinpirates/common/HttpLoginRequestHelper:URL	Ljava/lang/String;
    //   150: invokespecial 166	java/net/URL:<init>	(Ljava/lang/String;)V
    //   153: invokevirtual 169	java/net/URL:getProtocol	()Ljava/lang/String;
    //   156: astore 15
    //   158: new 165	java/net/URL
    //   161: dup
    //   162: aload_0
    //   163: getfield 39	com/nubee/coinpirates/common/HttpLoginRequestHelper:URL	Ljava/lang/String;
    //   166: invokespecial 166	java/net/URL:<init>	(Ljava/lang/String;)V
    //   169: invokevirtual 172	java/net/URL:getHost	()Ljava/lang/String;
    //   172: astore 16
    //   174: new 165	java/net/URL
    //   177: dup
    //   178: aload_0
    //   179: getfield 39	com/nubee/coinpirates/common/HttpLoginRequestHelper:URL	Ljava/lang/String;
    //   182: invokespecial 166	java/net/URL:<init>	(Ljava/lang/String;)V
    //   185: invokevirtual 175	java/net/URL:getPort	()I
    //   188: istore 17
    //   190: iload 17
    //   192: iconst_m1
    //   193: if_icmpne +18 -> 211
    //   196: aload 15
    //   198: ldc 31
    //   200: invokevirtual 179	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   203: ifeq +283 -> 486
    //   206: sipush 443
    //   209: istore 17
    //   211: new 181	org/apache/http/HttpHost
    //   214: dup
    //   215: aload 16
    //   217: iload 17
    //   219: aload 15
    //   221: invokespecial 184	org/apache/http/HttpHost:<init>	(Ljava/lang/String;ILjava/lang/String;)V
    //   224: astore 18
    //   226: aload_0
    //   227: aload_1
    //   228: aload 18
    //   230: aload_2
    //   231: invokevirtual 187	org/apache/http/impl/client/DefaultHttpClient:execute	(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;)Lorg/apache/http/HttpResponse;
    //   234: putfield 43	com/nubee/coinpirates/common/HttpLoginRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   237: aload_0
    //   238: getfield 43	com/nubee/coinpirates/common/HttpLoginRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   241: invokeinterface 193 1 0
    //   246: invokeinterface 199 1 0
    //   251: astore 19
    //   253: new 201	java/io/ByteArrayOutputStream
    //   256: dup
    //   257: invokespecial 202	java/io/ByteArrayOutputStream:<init>	()V
    //   260: astore 20
    //   262: sipush 1024
    //   265: newarray byte
    //   267: astore 21
    //   269: aload 19
    //   271: aload 21
    //   273: invokevirtual 208	java/io/InputStream:read	([B)I
    //   276: istore 22
    //   278: iload 22
    //   280: iconst_m1
    //   281: if_icmpne +212 -> 493
    //   284: aload_0
    //   285: new 210	java/io/ByteArrayInputStream
    //   288: dup
    //   289: aload 20
    //   291: invokevirtual 214	java/io/ByteArrayOutputStream:toByteArray	()[B
    //   294: invokespecial 217	java/io/ByteArrayInputStream:<init>	([B)V
    //   297: putfield 45	com/nubee/coinpirates/common/HttpLoginRequestHelper:inputStreamXml	Ljava/io/InputStream;
    //   300: aload 19
    //   302: ifnull +8 -> 310
    //   305: aload 19
    //   307: invokevirtual 220	java/io/InputStream:close	()V
    //   310: aload 20
    //   312: ifnull +8 -> 320
    //   315: aload 20
    //   317: invokevirtual 221	java/io/ByteArrayOutputStream:close	()V
    //   320: aload_0
    //   321: aload_0
    //   322: getfield 43	com/nubee/coinpirates/common/HttpLoginRequestHelper:response	Lorg/apache/http/HttpResponse;
    //   325: invokeinterface 225 1 0
    //   330: invokeinterface 230 1 0
    //   335: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   338: aload_0
    //   339: getfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   342: istore 23
    //   344: iload 23
    //   346: sipush 200
    //   349: if_icmpne +166 -> 515
    //   352: iconst_1
    //   353: ireturn
    //   354: aload_0
    //   355: getfield 41	com/nubee/coinpirates/common/HttpLoginRequestHelper:params	Ljava/util/List;
    //   358: iload 8
    //   360: invokeinterface 234 2 0
    //   365: checkcast 236	org/apache/http/NameValuePair
    //   368: astore 24
    //   370: aload 24
    //   372: invokeinterface 239 1 0
    //   377: astore 25
    //   379: aload 24
    //   381: invokeinterface 242 1 0
    //   386: astore 26
    //   388: aload 25
    //   390: ldc 244
    //   392: invokevirtual 179	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   395: ifeq +10 -> 405
    //   398: aload 26
    //   400: astore 7
    //   402: goto +164 -> 566
    //   405: aload 25
    //   407: ldc 246
    //   409: invokevirtual 179	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   412: ifeq +10 -> 422
    //   415: aload 26
    //   417: astore 5
    //   419: goto +147 -> 566
    //   422: new 125	java/lang/StringBuilder
    //   425: dup
    //   426: aload 6
    //   428: invokestatic 131	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   431: invokespecial 132	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   434: ldc 248
    //   436: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   439: aload 25
    //   441: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   444: ldc 250
    //   446: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   449: aload 26
    //   451: invokevirtual 138	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   454: invokevirtual 145	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   457: astore 27
    //   459: aload 27
    //   461: astore 6
    //   463: goto +103 -> 566
    //   466: astore 29
    //   468: aload_0
    //   469: bipush 252
    //   471: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   474: iconst_0
    //   475: ireturn
    //   476: astore 28
    //   478: aload_0
    //   479: bipush 251
    //   481: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   484: iconst_0
    //   485: ireturn
    //   486: bipush 80
    //   488: istore 17
    //   490: goto -279 -> 211
    //   493: aload 20
    //   495: aload 21
    //   497: iconst_0
    //   498: iload 22
    //   500: invokevirtual 254	java/io/ByteArrayOutputStream:write	([BII)V
    //   503: goto -234 -> 269
    //   506: astore 14
    //   508: aload_0
    //   509: iconst_m1
    //   510: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   513: iconst_0
    //   514: ireturn
    //   515: iconst_0
    //   516: ireturn
    //   517: astore 13
    //   519: aload_0
    //   520: bipush 254
    //   522: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   525: iconst_0
    //   526: ireturn
    //   527: astore 12
    //   529: aload_0
    //   530: bipush 253
    //   532: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   535: iconst_0
    //   536: ireturn
    //   537: astore 11
    //   539: aload_0
    //   540: bipush 253
    //   542: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   545: iconst_0
    //   546: ireturn
    //   547: astore 10
    //   549: aload_0
    //   550: bipush 250
    //   552: putfield 47	com/nubee/coinpirates/common/HttpLoginRequestHelper:statusCode	I
    //   555: iconst_0
    //   556: ireturn
    //   557: astore 4
    //   559: goto -81 -> 478
    //   562: astore_3
    //   563: goto -95 -> 468
    //   566: iinc 8 1
    //   569: goto -517 -> 52
    //
    // Exception table:
    //   from	to	target	type
    //   8	20	466	java/lang/IllegalArgumentException
    //   8	20	476	java/io/UnsupportedEncodingException
    //   122	190	506	org/apache/http/NoHttpResponseException
    //   196	206	506	org/apache/http/NoHttpResponseException
    //   211	269	506	org/apache/http/NoHttpResponseException
    //   269	278	506	org/apache/http/NoHttpResponseException
    //   284	300	506	org/apache/http/NoHttpResponseException
    //   305	310	506	org/apache/http/NoHttpResponseException
    //   315	320	506	org/apache/http/NoHttpResponseException
    //   320	344	506	org/apache/http/NoHttpResponseException
    //   493	503	506	org/apache/http/NoHttpResponseException
    //   122	190	517	java/net/SocketTimeoutException
    //   196	206	517	java/net/SocketTimeoutException
    //   211	269	517	java/net/SocketTimeoutException
    //   269	278	517	java/net/SocketTimeoutException
    //   284	300	517	java/net/SocketTimeoutException
    //   305	310	517	java/net/SocketTimeoutException
    //   315	320	517	java/net/SocketTimeoutException
    //   320	344	517	java/net/SocketTimeoutException
    //   493	503	517	java/net/SocketTimeoutException
    //   122	190	527	org/apache/http/client/ClientProtocolException
    //   196	206	527	org/apache/http/client/ClientProtocolException
    //   211	269	527	org/apache/http/client/ClientProtocolException
    //   269	278	527	org/apache/http/client/ClientProtocolException
    //   284	300	527	org/apache/http/client/ClientProtocolException
    //   305	310	527	org/apache/http/client/ClientProtocolException
    //   315	320	527	org/apache/http/client/ClientProtocolException
    //   320	344	527	org/apache/http/client/ClientProtocolException
    //   493	503	527	org/apache/http/client/ClientProtocolException
    //   122	190	537	java/io/IOException
    //   196	206	537	java/io/IOException
    //   211	269	537	java/io/IOException
    //   269	278	537	java/io/IOException
    //   284	300	537	java/io/IOException
    //   305	310	537	java/io/IOException
    //   315	320	537	java/io/IOException
    //   320	344	537	java/io/IOException
    //   493	503	537	java/io/IOException
    //   122	190	547	java/lang/IllegalStateException
    //   196	206	547	java/lang/IllegalStateException
    //   211	269	547	java/lang/IllegalStateException
    //   269	278	547	java/lang/IllegalStateException
    //   284	300	547	java/lang/IllegalStateException
    //   305	310	547	java/lang/IllegalStateException
    //   315	320	547	java/lang/IllegalStateException
    //   320	344	547	java/lang/IllegalStateException
    //   493	503	547	java/lang/IllegalStateException
    //   20	37	557	java/io/UnsupportedEncodingException
    //   52	63	557	java/io/UnsupportedEncodingException
    //   70	122	557	java/io/UnsupportedEncodingException
    //   354	398	557	java/io/UnsupportedEncodingException
    //   405	415	557	java/io/UnsupportedEncodingException
    //   422	459	557	java/io/UnsupportedEncodingException
    //   20	37	562	java/lang/IllegalArgumentException
    //   52	63	562	java/lang/IllegalArgumentException
    //   70	122	562	java/lang/IllegalArgumentException
    //   354	398	562	java/lang/IllegalArgumentException
    //   405	415	562	java/lang/IllegalArgumentException
    //   422	459	562	java/lang/IllegalArgumentException
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
    this.params.add(new BasicNameValuePair(paramString1, paramString2));
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.HttpLoginRequestHelper
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */