package com.tapjoy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TapjoyReferralTracker extends BroadcastReceiver
{
  final String REFERRAL_URL = "InstallReferral";
  final String TJC_PREFERENCE = "tjcPrefrences";

  public TapjoyReferralTracker()
  {
  }

  public void onReceive(Context paramContext, Intent paramIntent)
  {
    TapjoyLog.i("TapjoyReferralTracker", "Traversing TapjoyReferralTracker Broadcast Receiver intent's info.......");
    String str1 = paramIntent.toURI();
    if ((str1 != null) && (str1.length() > 0))
    {
      int i = str1.indexOf("referrer=");
      if (i <= -1)
        break label137;
      String str2 = str1.substring(i, str1.length() - 4);
      TapjoyLog.i("TapjoyReferralTracker", "Referral URI: " + str2);
      SharedPreferences.Editor localEditor = paramContext.getSharedPreferences("tjcPrefrences", 0).edit();
      localEditor.putString("InstallReferral", str2);
      localEditor.commit();
      TapjoyLog.i("TapjoyReferralTracker", "Cached Referral URI: " + str2);
    }
    while (true)
    {
      TapjoyLog.i("TapjoyReferralTracker", "End");
      return;
      label137: TapjoyLog.i("TapjoyReferralTracker", "No Referral URL.");
    }
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyReferralTracker
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */