package com.tapjoy;

import android.util.Log;

public class TapjoyLog
{
  private static boolean showLog = false;

  public TapjoyLog()
  {
  }

  public static void d(String paramString1, String paramString2)
  {
    if (showLog)
      Log.d(paramString1, paramString2);
  }

  public static void e(String paramString1, String paramString2)
  {
    if (showLog)
      Log.e(paramString1, paramString2);
  }

  public static void enableLogging(boolean paramBoolean)
  {
    showLog = paramBoolean;
  }

  public static void i(String paramString1, String paramString2)
  {
    if (showLog)
      Log.i(paramString1, paramString2);
  }

  public static void v(String paramString1, String paramString2)
  {
    if (showLog)
      Log.v(paramString1, paramString2);
  }

  public static void w(String paramString1, String paramString2)
  {
    if (showLog)
      Log.w(paramString1, paramString2);
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyLog
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */