package com.admob.android.ads;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import java.lang.ref.WeakReference;

final class ac$a
  implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener
{
  private WeakReference<ac> a;

  public ac$a(ac paramac)
  {
    this.a = new WeakReference(paramac);
  }

  public final void onCompletion(MediaPlayer paramMediaPlayer)
  {
    ac localac = (ac)this.a.get();
    if (localac != null)
    {
      localac.i = true;
      localac.f();
      localac.a(true);
    }
  }

  public final boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2)
  {
    if (InterstitialAd.c.a("AdMobSDK", 6))
      Log.e("AdMobSDK", "error playing video, what: " + paramInt1 + ", extra: " + paramInt2);
    ac localac = (ac)this.a.get();
    if (localac == null)
      return false;
    localac.c();
    return true;
  }

  public final void onPrepared(MediaPlayer paramMediaPlayer)
  {
    ac localac = (ac)this.a.get();
    if (localac != null)
      localac.a();
  }
}

/* Location:
 * Qualified Name:     com.admob.android.ads.ac.a
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */