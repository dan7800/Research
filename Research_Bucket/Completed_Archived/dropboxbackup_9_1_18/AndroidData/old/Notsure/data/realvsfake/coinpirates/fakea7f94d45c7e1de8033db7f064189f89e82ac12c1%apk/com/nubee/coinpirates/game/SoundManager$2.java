package com.nubee.coinpirates.game;

import android.media.MediaPlayer;

class SoundManager$2
  implements Runnable
{
  SoundManager$2(int paramInt)
  {
  }

  public void run()
  {
    if ((SoundManager.access$0()[this.val$musicid] != null) && (SoundManager.access$0()[this.val$musicid].isPlaying()))
      SoundManager.access$0()[this.val$musicid].pause();
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.SoundManager.2
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */