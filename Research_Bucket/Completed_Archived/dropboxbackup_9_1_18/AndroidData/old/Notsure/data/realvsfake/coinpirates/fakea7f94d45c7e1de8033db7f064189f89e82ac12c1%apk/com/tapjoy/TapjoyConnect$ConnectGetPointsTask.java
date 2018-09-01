package com.tapjoy;

import android.os.AsyncTask;

class TapjoyConnect$ConnectGetPointsTask extends AsyncTask<Void, Void, Boolean>
{
  private TapjoyConnect$ConnectGetPointsTask(TapjoyConnect paramTapjoyConnect)
  {
  }

  protected Boolean doInBackground(Void[] paramArrayOfVoid)
  {
    String str = TapjoyConnect.access$2().connectToURL("https://ws.tapjoyads.com/get_vg_store_items/user_account?", TapjoyConnect.access$0(this.this$0));
    boolean bool = false;
    if (str != null)
      bool = TapjoyConnect.access$4(this.this$0, str);
    if (!bool)
      TapjoyConnect.access$5().getUpdatePointsFailed("Failed to retrieve points from server");
    return Boolean.valueOf(bool);
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyConnect.ConnectGetPointsTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */