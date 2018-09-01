package com.admob.android.ads;

import android.view.View;
import android.view.View.OnClickListener;
import java.lang.ref.WeakReference;

public final class ac$h
  implements View.OnClickListener
{
  private WeakReference<ac> a;

  public ac$h(ac paramac)
  {
    this.a = new WeakReference(paramac);
  }

  public final void onClick(View paramView)
  {
    ac localac = (ac)this.a.get();
    if (localac == null)
      return;
    localac.f.a("replay", null);
    if (localac.d != null)
      ac.b(localac.d);
    ac.a(localac, false);
    localac.h = true;
    ac.a(localac, localac.getContext());
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.h
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */