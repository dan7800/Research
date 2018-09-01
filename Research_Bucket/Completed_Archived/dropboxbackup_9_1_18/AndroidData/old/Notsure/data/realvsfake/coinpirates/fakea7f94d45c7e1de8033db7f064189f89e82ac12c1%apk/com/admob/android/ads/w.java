package com.admob.android.ads;

import android.os.Bundle;

public final class w
  implements n
{
  public String a;
  public boolean b;

  public w()
  {
    this.a = null;
    this.b = false;
  }

  public w(String paramString, boolean paramBoolean)
  {
    this.a = paramString;
    this.b = paramBoolean;
  }

  public final Bundle a()
  {
    Bundle localBundle = new Bundle();
    localBundle.putString("u", this.a);
    localBundle.putBoolean("p", this.b);
    return localBundle;
  }

  public final boolean equals(Object paramObject)
  {
    if ((paramObject instanceof w))
    {
      w localw = (w)paramObject;
      int i;
      int j;
      if ((this.a == null) && (localw.a != null))
      {
        i = 1;
        if ((this.a == null) || (this.a.equals(localw.a)))
          break label87;
        j = 1;
        label52: if (this.b == localw.b)
          break label93;
      }
      label87: label93: for (int k = 1; ; k = 0)
      {
        if ((i != 0) || (j != 0) || (k != 0))
          break label99;
        return true;
        i = 0;
        break;
        j = 0;
        break label52;
      }
      label99: return false;
    }
    return false;
  }

  public final int hashCode()
  {
    if (this.a != null)
      return this.a.hashCode();
    return super.hashCode();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.w
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */