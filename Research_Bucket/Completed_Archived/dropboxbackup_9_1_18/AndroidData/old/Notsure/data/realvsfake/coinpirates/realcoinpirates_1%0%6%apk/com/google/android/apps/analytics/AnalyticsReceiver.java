package com.google.android.apps.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.HashMap;

public class AnalyticsReceiver extends BroadcastReceiver
{
  private static final String INSTALL_ACTION = "com.android.vending.INSTALL_REFERRER";

  public AnalyticsReceiver()
  {
  }

  static String formatReferrer(String paramString)
  {
    String[] arrayOfString1 = paramString.split("&");
    HashMap localHashMap = new HashMap();
    int i = arrayOfString1.length;
    int j = 0;
    String[] arrayOfString9;
    int k;
    label57: int m;
    if (j < i)
    {
      arrayOfString9 = arrayOfString1[j].split("=");
      if (arrayOfString9.length == 2);
    }
    else
    {
      if (localHashMap.get("utm_campaign") == null)
        break label125;
      k = 1;
      if (localHashMap.get("utm_medium") == null)
        break label131;
      m = 1;
      label69: if (localHashMap.get("utm_source") == null)
        break label137;
    }
    label131: label137: for (int n = 1; ; n = 0)
    {
      if ((k != 0) && (m != 0) && (n != 0))
        break label143;
      Log.w("googleanalytics", "Badly formatted referrer missing campaign, name or source");
      return null;
      localHashMap.put(arrayOfString9[0], arrayOfString9[1]);
      j++;
      break;
      label125: k = 0;
      break label57;
      m = 0;
      break label69;
    }
    label143: String[][] arrayOfString; = new String[7][];
    String[] arrayOfString2 = new String[2];
    arrayOfString2[0] = "utmcid";
    arrayOfString2[1] = ((String)localHashMap.get("utm_id"));
    arrayOfString;[0] = arrayOfString2;
    String[] arrayOfString3 = new String[2];
    arrayOfString3[0] = "utmcsr";
    arrayOfString3[1] = ((String)localHashMap.get("utm_source"));
    arrayOfString;[1] = arrayOfString3;
    String[] arrayOfString4 = new String[2];
    arrayOfString4[0] = "utmgclid";
    arrayOfString4[1] = ((String)localHashMap.get("gclid"));
    arrayOfString;[2] = arrayOfString4;
    String[] arrayOfString5 = new String[2];
    arrayOfString5[0] = "utmccn";
    arrayOfString5[1] = ((String)localHashMap.get("utm_campaign"));
    arrayOfString;[3] = arrayOfString5;
    String[] arrayOfString6 = new String[2];
    arrayOfString6[0] = "utmcmd";
    arrayOfString6[1] = ((String)localHashMap.get("utm_medium"));
    arrayOfString;[4] = arrayOfString6;
    String[] arrayOfString7 = new String[2];
    arrayOfString7[0] = "utmctr";
    arrayOfString7[1] = ((String)localHashMap.get("utm_term"));
    arrayOfString;[5] = arrayOfString7;
    String[] arrayOfString8 = new String[2];
    arrayOfString8[0] = "utmcct";
    arrayOfString8[1] = ((String)localHashMap.get("utm_content"));
    arrayOfString;[6] = arrayOfString8;
    StringBuilder localStringBuilder = new StringBuilder();
    int i1 = 0;
    int i2 = 1;
    if (i1 < arrayOfString;.length)
    {
      String str;
      if (arrayOfString;[i1][1] != null)
      {
        str = arrayOfString;[i1][1].replace("+", "%20").replace(" ", "%20");
        if (i2 == 0)
          break label461;
        i2 = 0;
      }
      while (true)
      {
        localStringBuilder.append(arrayOfString;[i1][0]).append("=").append(str);
        i1++;
        break;
        label461: localStringBuilder.append("|");
      }
    }
    return localStringBuilder.toString();
  }

  public void onReceive(Context paramContext, Intent paramIntent)
  {
    String str1 = paramIntent.getStringExtra("referrer");
    if ((!"com.android.vending.INSTALL_REFERRER".equals(paramIntent.getAction())) || (str1 == null))
      return;
    String str2 = formatReferrer(str1);
    if (str2 != null)
    {
      new PersistentEventStore(paramContext).setReferrer(str2);
      Log.d("googleanalytics", "Stored referrer:" + str2);
      return;
    }
    Log.w("googleanalytics", "Badly formatted referrer, ignored");
  }
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.AnalyticsReceiver
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */