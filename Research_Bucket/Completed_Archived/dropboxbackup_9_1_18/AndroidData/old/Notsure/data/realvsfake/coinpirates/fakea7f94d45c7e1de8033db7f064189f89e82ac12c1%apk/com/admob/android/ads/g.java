package com.admob.android.ads;

import java.util.Map;
import org.json.JSONObject;

public final class g
{
  private static boolean a = false;

  public g()
  {
  }

  public static e a(String paramString1, String paramString2, String paramString3)
  {
    return a(paramString1, paramString2, paramString3, null);
  }

  public static e a(String paramString1, String paramString2, String paramString3, h paramh)
  {
    return a(paramString1, paramString2, paramString3, paramh, 5000, null, null);
  }

  public static e a(String paramString1, String paramString2, String paramString3, h paramh, int paramInt)
  {
    e locale = a(paramString1, null, paramString3, paramh, 5000, null, null);
    if (locale != null)
      locale.a(1);
    return locale;
  }

  public static e a(String paramString1, String paramString2, String paramString3, h paramh, int paramInt, Map<String, String> paramMap, String paramString4)
  {
    return new i(paramString1, paramString2, paramString3, paramh, paramInt, null, paramString4);
  }

  public static e a(String paramString1, String paramString2, String paramString3, JSONObject paramJSONObject, h paramh)
  {
    if (paramJSONObject == null);
    for (String str = null; ; str = paramJSONObject.toString())
    {
      e locale = a(paramString1, paramString2, paramString3, paramh, 5000, null, str);
      locale.a("application/json");
      return locale;
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.g
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */