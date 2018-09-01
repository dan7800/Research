package com.tapjoy;

import android.os.AsyncTask;

class TapjoyConnect$PayPerActionTask extends AsyncTask<Void, Void, Boolean>
{
  private TapjoyConnect$PayPerActionTask(TapjoyConnect paramTapjoyConnect)
  {
  }

  protected Boolean doInBackground(Void[] paramArrayOfVoid)
  {
    String str = TapjoyConnect.access$2().connectToURL("https://ws.tapjoyads.com/connect?", TapjoyConnect.access$9(this.this$0));
    boolean bool = false;
    if (str != null)
      bool = TapjoyConnect.access$10(this.this$0, str);
    return Boolean.valueOf(bool);
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyConnect.PayPerActionTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */