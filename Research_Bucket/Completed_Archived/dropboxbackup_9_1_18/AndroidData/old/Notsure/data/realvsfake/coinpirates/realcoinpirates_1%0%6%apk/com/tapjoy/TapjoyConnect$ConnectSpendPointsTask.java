package com.tapjoy;

import android.os.AsyncTask;

class TapjoyConnect$ConnectSpendPointsTask extends AsyncTask<Void, Void, Boolean>
{
  private TapjoyConnect$ConnectSpendPointsTask(TapjoyConnect paramTapjoyConnect)
  {
  }

  protected Boolean doInBackground(Void[] paramArrayOfVoid)
  {
    String str1 = String.valueOf(TapjoyConnect.access$0(this.this$0)) + "&tap_points=" + TapjoyConnect.access$6(this.this$0);
    String str2 = TapjoyConnect.access$2().connectToURL("https://ws.tapjoyads.com/purchase_vg/spend?", str1);
    boolean bool = false;
    if (str2 != null)
      bool = TapjoyConnect.access$7(this.this$0, str2);
    if (!bool)
      TapjoyConnect.access$8().getSpendPointsResponseFailed("Failed to spend points.");
    return Boolean.valueOf(bool);
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyConnect.ConnectSpendPointsTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */