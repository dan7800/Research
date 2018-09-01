package com.nubee.coinpirates.game;

import android.media.MediaPlayer;

class SoundManager$1
  implements Runnable
{
  SoundManager$1(int paramInt)
  {
  }

  public void run()
  {
    if (SoundManager.access$0()[this.val$musicid] == null)
    {
      SoundManager.access$0()[this.val$musicid] = MediaPlayer.create(SoundManager.sContext, SoundManager.BGM_RESOURCEID[this.val$musicid]);
      if (SoundManager.access$0()[this.val$musicid] != null)
        SoundManager.access$0()[this.val$musicid].setLooping(true);
    }
    if (SoundManager.access$0()[this.val$musicid] != null)
      SoundManager.access$0()[this.val$musicid].start();
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.SoundManager.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */