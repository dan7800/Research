package com.tapjoy;

import android.os.AsyncTask;

class TapjoyConnect$ConnectTask extends AsyncTask<Void, Void, Boolean>
{
  private TapjoyConnect$ConnectTask(TapjoyConnect paramTapjoyConnect)
  {
  }

  protected Boolean doInBackground(Void[] paramArrayOfVoid)
  {
    String str1 = TapjoyConnect.access$0(this.this$0);
    if (!TapjoyConnect.access$1(this.this$0).equals(""))
      str1 = String.valueOf(str1) + "&" + TapjoyConnect.access$1(this.this$0);
    String str2 = TapjoyConnect.access$2().connectToURL("https://ws.tapjoyads.com/connect?", str1);
    boolean bool = false;
    if (str2 != null)
      bool = TapjoyConnect.access$3(this.this$0, str2);
    return Boolean.valueOf(bool);
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyConnect.ConnectTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */