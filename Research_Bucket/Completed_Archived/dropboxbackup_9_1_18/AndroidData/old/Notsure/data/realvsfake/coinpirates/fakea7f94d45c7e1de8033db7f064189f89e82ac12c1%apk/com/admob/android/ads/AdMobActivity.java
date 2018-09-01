package com.admob.android.ads;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;;
import com.admob.android.ads.view.AdMobWebView;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Vector;

public class AdMobActivity extends Activity
{
  private r a;
  private Vector<ae> b;

  public AdMobActivity()
  {
  }

  public void finish()
  {
    if ((this.a != null) && (this.a.l))
    {
      Intent localIntent = new Intent();
      localIntent.putExtra("admob_activity", true);
      setResult(-1, localIntent);
    }
    super.finish();
  }

  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    Iterator localIterator = this.b.iterator();
    while (localIterator.hasNext())
      ((ae)localIterator.next()).a(paramConfiguration);
    super.onConfigurationChanged(paramConfiguration);
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    this.b = new Vector();
    Bundle localBundle = getIntent().getBundleExtra("o");
    r localr1 = new r();
    if (localr1.a(localBundle));
    for (this.a = localr1; this.a == null; this.a = null)
    {
      if (InterstitialAd.c.a("AdMobSDK", 6))
        Log.e("AdMobSDK", "Unable to get openerInfo from intent");
      return;
    }
    q.a(this.a.c, null, AdManager.getUserId(this));
    j.a locala = this.a.a;
    WeakReference localWeakReference = new WeakReference(this);
    Object localObject;
    switch (1.a[locala.ordinal()])
    {
    default:
      localObject = null;
      if (localObject != null)
        switch (1.b[this.a.e.ordinal()])
        {
        default:
          if (InterstitialAd.c.a("AdMobSDK", 2))
            Log.v("AdMobSDK", "Setting target orientation to sensor");
          setRequestedOrientation(4);
        case 1:
        case 2:
        }
    case 1:
    case 2:
      while (true)
      {
        setContentView((android.view.View)localObject);
        return;
        setTheme(16973831);
        localObject = AdMobWebView.a(getApplicationContext(), this.a.d, false, this.a.f, this.a.g, k.a(this), localWeakReference);
        break;
        r localr2 = this.a;
        ac localac = new ac(getApplicationContext(), localWeakReference);
        localac.a(localr2);
        this.b.add(localac);
        localObject = localac;
        break;
        if (InterstitialAd.c.a("AdMobSDK", 2))
          Log.v("AdMobSDK", "Setting target orientation to landscape");
        setRequestedOrientation(0);
        continue;
        if (InterstitialAd.c.a("AdMobSDK", 2))
          Log.v("AdMobSDK", "Setting target orientation to portrait");
        setRequestedOrientation(1);
      }
    }
    finish();
  }

  protected void onDestroy()
  {
    this.b.clear();
    super.onDestroy();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.AdMobActivity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */