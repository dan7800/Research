package com.admob.android.ads;

import android.content.Context;
import android.media.AudioManager;

public class v
{
  public AudioManager a;

  public v(Context paramContext)
  {
    this.a = ((AudioManager)paramContext.getSystemService("audio"));
  }

  public int a()
  {
    return this.a.getMode();
  }

  public boolean b()
  {
    return this.a.isMusicActive();
  }

  public boolean c()
  {
    return this.a.isSpeakerphoneOn();
  }

  public int d()
  {
    return this.a.getRingerMode();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.v
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */