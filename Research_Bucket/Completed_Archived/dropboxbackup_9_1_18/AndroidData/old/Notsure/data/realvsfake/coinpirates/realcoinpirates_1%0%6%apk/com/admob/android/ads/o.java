package com.admob.android.ads;

import android.os.Bundle;

public final class o
  implements n
{
  public String a;
  public String b;
  public String c;
  public r d = new r();
  public String e;
  public String f;

  public o()
  {
  }

  public final Bundle a()
  {
    Bundle localBundle = new Bundle();
    localBundle.putString("ad", this.a);
    localBundle.putString("au", this.b);
    localBundle.putString("t", this.c);
    localBundle.putBundle("oi", AdView.a.a(this.d));
    localBundle.putString("ap", this.e);
    localBundle.putString("json", this.f);
    return localBundle;
  }

  public final boolean a(Bundle paramBundle)
  {
    if (paramBundle == null)
      return false;
    this.a = paramBundle.getString("ad");
    this.b = paramBundle.getString("au");
    this.c = paramBundle.getString("t");
    if (!this.d.a(paramBundle.getBundle("oi")))
      return false;
    this.e = paramBundle.getString("ap");
    this.f = paramBundle.getString("json");
    return true;
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.o
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */