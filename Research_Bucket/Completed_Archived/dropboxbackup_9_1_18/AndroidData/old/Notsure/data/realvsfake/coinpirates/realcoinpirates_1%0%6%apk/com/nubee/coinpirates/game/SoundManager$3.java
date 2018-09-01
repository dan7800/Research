package com.nubee.coinpirates.game;

import android.media.MediaPlayer;

class SoundManager$3
  implements Runnable
{
  SoundManager$3(int paramInt)
  {
  }

  public void run()
  {
    if (SoundManager.access$1()[this.val$soundid] == null)
      SoundManager.access$1()[this.val$soundid] = MediaPlayer.create(SoundManager.sContext, SoundManager.SE_RESOURCEID[this.val$soundid]);
    if (SoundManager.access$1()[this.val$soundid] != null)
      SoundManager.access$1()[this.val$soundid].start();
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.SoundManager.3
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */