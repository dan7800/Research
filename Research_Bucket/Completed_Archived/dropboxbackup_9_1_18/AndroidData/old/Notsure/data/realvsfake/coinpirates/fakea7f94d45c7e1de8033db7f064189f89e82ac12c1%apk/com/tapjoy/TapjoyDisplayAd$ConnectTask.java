package com.tapjoy;

import android.os.AsyncTask;

class TapjoyDisplayAd$ConnectTask extends AsyncTask<Void, Void, Boolean>
{
  private TapjoyDisplayAd$ConnectTask(TapjoyDisplayAd paramTapjoyDisplayAd)
  {
  }

  protected Boolean doInBackground(Void[] paramArrayOfVoid)
  {
    boolean bool = false;
    String str = TapjoyDisplayAd.access$0().connectToURL(TapjoyDisplayAd.access$1(this.this$0), TapjoyDisplayAd.access$2(this.this$0));
    if ((str == null) || (str.length() == 0))
      TapjoyDisplayAd.access$3().getDisplayAdResponseFailed("Network error.");
    while (true)
    {
      return Boolean.valueOf(bool);
      bool = TapjoyDisplayAd.access$4(this.this$0, str);
      if (!bool)
        TapjoyDisplayAd.access$3().getDisplayAdResponseFailed("No ad to display.");
    }
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyDisplayAd.ConnectTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */