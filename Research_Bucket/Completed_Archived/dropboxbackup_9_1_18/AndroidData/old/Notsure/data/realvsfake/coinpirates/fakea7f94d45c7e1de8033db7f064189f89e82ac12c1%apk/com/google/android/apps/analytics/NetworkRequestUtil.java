package com.google.android.apps.analytics;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

class NetworkRequestUtil
{
  private static final String FAKE_DOMAIN_HASH = "999";
  private static final String GOOGLE_ANALYTICS_GIF_PATH = "/__utm.gif";

  NetworkRequestUtil()
  {
  }

  public static String constructEventRequestPath(Event paramEvent, String paramString)
  {
    Locale localLocale = Locale.getDefault();
    StringBuilder localStringBuilder1 = new StringBuilder();
    StringBuilder localStringBuilder2 = new StringBuilder();
    Object[] arrayOfObject1 = new Object[2];
    arrayOfObject1[0] = paramEvent.category;
    arrayOfObject1[1] = paramEvent.action;
    localStringBuilder2.append(String.format("5(%s*%s", arrayOfObject1));
    if (paramEvent.label != null)
      localStringBuilder2.append("*").append(paramEvent.label);
    localStringBuilder2.append(")");
    if (paramEvent.value > -1)
    {
      Object[] arrayOfObject4 = new Object[1];
      arrayOfObject4[0] = Integer.valueOf(paramEvent.value);
      localStringBuilder2.append(String.format("(%d)", arrayOfObject4));
    }
    localStringBuilder1.append("/__utm.gif");
    localStringBuilder1.append("?utmwv=4.3");
    localStringBuilder1.append("&utmn=").append(paramEvent.randomVal);
    localStringBuilder1.append("&utmt=event");
    localStringBuilder1.append("&utme=").append(localStringBuilder2.toString());
    localStringBuilder1.append("&utmcs=UTF-8");
    Object[] arrayOfObject2 = new Object[2];
    arrayOfObject2[0] = Integer.valueOf(paramEvent.screenWidth);
    arrayOfObject2[1] = Integer.valueOf(paramEvent.screenHeight);
    localStringBuilder1.append(String.format("&utmsr=%dx%d", arrayOfObject2));
    Object[] arrayOfObject3 = new Object[2];
    arrayOfObject3[0] = localLocale.getLanguage();
    arrayOfObject3[1] = localLocale.getCountry();
    localStringBuilder1.append(String.format("&utmul=%s-%s", arrayOfObject3));
    localStringBuilder1.append("&utmac=").append(paramEvent.accountId);
    localStringBuilder1.append("&utmcc=").append(getEscapedCookieString(paramEvent, paramString));
    return localStringBuilder1.toString();
  }

  public static String constructPageviewRequestPath(Event paramEvent, String paramString)
  {
    String str1 = "";
    if (paramEvent.action != null)
      str1 = paramEvent.action;
    if (!str1.startsWith("/"))
      str1 = "/" + str1;
    String str2 = encode(str1);
    Locale localLocale = Locale.getDefault();
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("/__utm.gif");
    localStringBuilder.append("?utmwv=4.3");
    localStringBuilder.append("&utmn=").append(paramEvent.randomVal);
    localStringBuilder.append("&utmcs=UTF-8");
    Object[] arrayOfObject1 = new Object[2];
    arrayOfObject1[0] = Integer.valueOf(paramEvent.screenWidth);
    arrayOfObject1[1] = Integer.valueOf(paramEvent.screenHeight);
    localStringBuilder.append(String.format("&utmsr=%dx%d", arrayOfObject1));
    Object[] arrayOfObject2 = new Object[2];
    arrayOfObject2[0] = localLocale.getLanguage();
    arrayOfObject2[1] = localLocale.getCountry();
    localStringBuilder.append(String.format("&utmul=%s-%s", arrayOfObject2));
    localStringBuilder.append("&utmp=").append(str2);
    localStringBuilder.append("&utmac=").append(paramEvent.accountId);
    localStringBuilder.append("&utmcc=").append(getEscapedCookieString(paramEvent, paramString));
    return localStringBuilder.toString();
  }

  private static String encode(String paramString)
  {
    try
    {
      String str = URLEncoder.encode(paramString, "UTF-8");
      return str;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    return null;
  }

  public static String getEscapedCookieString(Event paramEvent, String paramString)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("__utma=");
    localStringBuilder.append("999").append(".");
    localStringBuilder.append(paramEvent.userId).append(".");
    localStringBuilder.append(paramEvent.timestampFirst).append(".");
    localStringBuilder.append(paramEvent.timestampPrevious).append(".");
    localStringBuilder.append(paramEvent.timestampCurrent).append(".");
    localStringBuilder.append(paramEvent.visits);
    if (paramString != null)
    {
      localStringBuilder.append("+__utmz=");
      localStringBuilder.append("999").append(".");
      localStringBuilder.append(paramEvent.timestampFirst).append(".");
      localStringBuilder.append("1.1.");
      localStringBuilder.append(paramString);
    }
    return encode(localStringBuilder.toString());
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.NetworkRequestUtil
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */