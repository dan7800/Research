package com.admob.android.ads;

import java.util.TimerTask;

final class z$a$1 extends TimerTask
{
  z$a$1(z.a parama)
  {
  }

  public final void run()
  {
    if (!this.a.a)
    {
      this.a.a = true;
      if (this.a.c != null)
        this.a.c.a(false);
    }
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.z.a.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */