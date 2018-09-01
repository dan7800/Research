package com.tapjoy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

class TapjoyDisplayAd$1
  implements View.OnClickListener
{
  TapjoyDisplayAd$1(TapjoyDisplayAd paramTapjoyDisplayAd)
  {
  }

  public void onClick(View paramView)
  {
    TapjoyLog.i("Display Ad", "Opening URL in new browser = [" + TapjoyDisplayAd.access$5() + "]");
    Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse(TapjoyDisplayAd.access$5()));
    localIntent.setFlags(268435456);
    TapjoyDisplayAd.access$6(this.this$0).startActivity(localIntent);
  }
}

/* Location:
 * Qualified Name:     com.tapjoy.TapjoyDisplayAd.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */