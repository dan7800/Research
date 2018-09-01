package com.tapjoy;

import android.os.AsyncTask;

class TapjoyFeaturedApp$ConnectTask extends AsyncTask<Void, Void, Boolean>
{
  private TapjoyFeaturedApp$ConnectTask(TapjoyFeaturedApp paramTapjoyFeaturedApp)
  {
  }

  protected Boolean doInBackground(Void[] paramArrayOfVoid)
  {
    String str = TapjoyFeaturedApp.access$0().connectToURL(TapjoyFeaturedApp.access$1(this.this$0), TapjoyFeaturedApp.access$2(this.this$0));
    boolean bool = false;
    if (str != null)
      bool = TapjoyFeaturedApp.access$3(this.this$0, str);
    if (!bool)
      TapjoyFeaturedApp.access$4().getFeaturedAppResponseFailed("Error retrieving featured app data from the server.");
    return Boolean.valueOf(bool);
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyFeaturedApp.ConnectTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */