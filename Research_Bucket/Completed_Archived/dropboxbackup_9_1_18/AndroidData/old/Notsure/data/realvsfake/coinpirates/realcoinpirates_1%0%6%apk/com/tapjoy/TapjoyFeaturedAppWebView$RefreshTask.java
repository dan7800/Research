package com.tapjoy;

import android.os.AsyncTask;
import android.webkit.WebView;

class TapjoyFeaturedAppWebView$RefreshTask extends AsyncTask<Void, Void, Boolean>
{
  private TapjoyFeaturedAppWebView$RefreshTask(TapjoyFeaturedAppWebView paramTapjoyFeaturedAppWebView)
  {
  }

  protected Boolean doInBackground(Void[] paramArrayOfVoid)
  {
    try
    {
      Thread.sleep(200L);
      return Boolean.valueOf(true);
    }
    catch (InterruptedException localInterruptedException)
    {
      while (true)
        localInterruptedException.printStackTrace();
    }
  }

  protected void onPostExecute(Boolean paramBoolean)
  {
    if (TapjoyFeaturedAppWebView.access$0(this.this$0) != null)
      TapjoyFeaturedAppWebView.access$0(this.this$0).loadUrl("javascript:window.onorientationchange();");
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyFeaturedAppWebView.RefreshTask
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */