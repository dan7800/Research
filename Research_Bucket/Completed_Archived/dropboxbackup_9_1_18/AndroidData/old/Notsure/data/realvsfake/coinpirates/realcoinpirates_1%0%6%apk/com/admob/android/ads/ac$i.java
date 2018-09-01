package com.admob.android.ads;

import android.view.View;
import android.view.View.OnClickListener;
import java.lang.ref.WeakReference;

public final class ac$i
  implements View.OnClickListener
{
  private WeakReference<ac> a;
  private boolean b;

  public ac$i(ac paramac, boolean paramBoolean)
  {
    this.a = new WeakReference(paramac);
    this.b = paramBoolean;
  }

  public final void onClick(View paramView)
  {
    ac localac = (ac)this.a.get();
    if (localac == null)
      return;
    if (this.b)
      localac.f.a("skip", null);
    localac.c();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.i
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */