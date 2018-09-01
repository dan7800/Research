package com.admob.android.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public final class ac$e
  implements View.OnClickListener
{
  private WeakReference<ac> a;
  private WeakReference<o> b;
  private WeakReference<Activity> c;

  public ac$e(ac paramac, o paramo, WeakReference<Activity> paramWeakReference)
  {
    this.a = new WeakReference(paramac);
    this.b = new WeakReference(paramo);
    this.c = paramWeakReference;
  }

  public final void onClick(View paramView)
  {
    ac localac = (ac)this.a.get();
    if (localac == null);
    o localo;
    do
    {
      return;
      ac.a(localac, false);
      localo = (o)this.b.get();
    }
    while (localo == null);
    Context localContext = localac.getContext();
    HashMap localHashMap;
    if (!localac.j)
    {
      localac.j = true;
      localHashMap = new HashMap();
      localHashMap.put("event", "interaction");
    }
    while (true)
    {
      q localq;
      while (true)
      {
        localac.f.a(localo.e, localHashMap);
        boolean bool = localac.e();
        if (bool)
          localac.f();
        localac.a(bool);
        localq = new q();
        try
        {
          localq.a(localContext, new JSONObject(localo.f), null);
          localq.b();
          if (this.c == null)
            break;
          Activity localActivity = (Activity)this.c.get();
          if (localActivity == null)
            break;
          localq.a(localActivity, localac);
          return;
        }
        catch (JSONException localJSONException)
        {
          while (true)
            if (InterstitialAd.c.a("AdMobSDK", 6))
              Log.e("AdMobSDK", "Could not create JSONObject from button click", localJSONException);
        }
      }
      localHashMap = null;
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.e
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */