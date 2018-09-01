package com.nubee.coinpirates.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Handler;

public class SoundManager
{
  static final int[] BGM_RESOURCEID = { 2131034131, 2131034121 };
  public static final int EMUSIC_JP = 1;
  public static final int EMUSIC_SLOT = 0;
  public static final int ESOUHD_SHIP = 16;
  public static final int ESOUND_ANCHOR_FALL = 23;
  public static final int ESOUND_ANCHOR_HIT = 24;
  public static final int ESOUND_COIN_BIG_DROP = 8;
  public static final int ESOUND_COIN_FALL_WIN = 9;
  public static final int ESOUND_COIN_HIT_COIN = 7;
  public static final int ESOUND_COIN_HIT_TABLE = 6;
  public static final int ESOUND_COIN_IN_HOLE = 5;
  public static final int ESOUND_COIN_RAIN_EFFECT = 11;
  public static final int ESOUND_JP_STOP = 13;
  public static final int ESOUND_JP_WIN = 14;
  public static final int ESOUND_JP_WIN2 = 21;
  public static final int ESOUND_LAUGH = 15;
  public static final int ESOUND_LEVELUP = 22;
  public static final int ESOUND_PUSH = 17;
  public static final int ESOUND_SLOT_JACKPOT = 4;
  public static final int ESOUND_SLOT_STOP = 0;
  public static final int ESOUND_SLOT_WIN1 = 1;
  public static final int ESOUND_SLOT_WIN2 = 2;
  public static final int ESOUND_SLOT_WIN3 = 3;
  public static final int ESOUND_THUNDER1 = 18;
  public static final int ESOUND_THUNDER2 = 19;
  public static final int ESOUND_THUNDER3 = 20;
  public static final int ESOUND_TREASUREMAP_EARN = 10;
  public static final int ESOUND_WALLUP = 12;
  public static final int MAX_EMUSIC = 2;
  public static final int MAX_ESOUND = 25;
  static final int[] SE_RESOURCEID = { 2131034128, 2131034132, 2131034119, 2131034120, 2131034133, 2131034127, 2131034115, 2131034114, 2131034116, 2131034118, 2131034126, 2131034117, 2131034137, 2131034122, 2131034123, 2131034138, 2131034130, 2131034129, 2131034134, 2131034135, 2131034136, 2131034124, 2131034125, 2131034112, 2131034113 };
  private static final MediaPlayer[] mBgmPlayer = new MediaPlayer[2];
  static Handler mHandler;
  private static final MediaPlayer[] mSePlayer = new MediaPlayer[25];
  static Context sContext;
  static boolean sEnableSe;
  static SoundManager sInstance;

  public SoundManager()
  {
  }

  public static void destroy()
  {
    int i = 0;
    if (i >= 2);
    for (int j = 0; ; j++)
    {
      if (j >= 25)
      {
        return;
        if (mBgmPlayer[i] != null)
        {
          if (mBgmPlayer[i].isPlaying())
            mBgmPlayer[i].stop();
          mBgmPlayer[i].release();
          mBgmPlayer[i] = null;
        }
        i++;
        break;
      }
      if (mSePlayer[j] != null)
      {
        if (mSePlayer[j].isPlaying())
          mSePlayer[j].stop();
        mSePlayer[j].release();
        mSePlayer[j] = null;
      }
    }
  }

  public static void enableSe(boolean paramBoolean)
  {
    sEnableSe = paramBoolean;
    sContext.getSharedPreferences("KEY_PREF_LAST_TIME", 0).edit().putBoolean("KEY_GAME_ENABLE_SE", sEnableSe).commit();
    if (!sEnableSe)
      destroy();
  }

  public static void initialize(Context paramContext)
  {
    sContext = paramContext;
    sEnableSe = sContext.getSharedPreferences("KEY_PREF_LAST_TIME", 0).getBoolean("KEY_GAME_ENABLE_SE", true);
    int i;
    if (sEnableSe)
    {
      i = 0;
      if (i < 2)
        break label56;
    }
    for (int j = 0; ; j++)
    {
      if (j >= 25)
      {
        mHandler = new Handler();
        return;
        label56: mBgmPlayer[i] = MediaPlayer.create(paramContext, BGM_RESOURCEID[i]);
        if (mBgmPlayer[i] != null)
          mBgmPlayer[i].setLooping(true);
        i++;
        break;
      }
      mSePlayer[j] = MediaPlayer.create(paramContext, SE_RESOURCEID[j]);
    }
  }

  public static void onPause()
  {
    for (int i = 0; ; i++)
    {
      if (i >= 2)
        return;
      if ((mBgmPlayer[i] != null) && (mBgmPlayer[i].isPlaying()))
        mBgmPlayer[i].pause();
    }
  }

  public static void onResume()
  {
  }

  public static void playSoundEffect(int paramInt)
  {
    if (!sEnableSe)
      return;
    mHandler.post(new Runnable()
    {
      public void run()
      {
        if (SoundManager.mSePlayer[this.val$soundid] == null)
          SoundManager.mSePlayer[this.val$soundid] = MediaPlayer.create(SoundManager.sContext, SoundManager.SE_RESOURCEID[this.val$soundid]);
        if (SoundManager.mSePlayer[this.val$soundid] != null)
          SoundManager.mSePlayer[this.val$soundid].start();
      }
    });
  }

  public static native void setCallback();

  public static void startMusic(int paramInt)
  {
    if (!sEnableSe)
      return;
    mHandler.post(new Runnable()
    {
      public void run()
      {
        if (SoundManager.mBgmPlayer[this.val$musicid] == null)
        {
          SoundManager.mBgmPlayer[this.val$musicid] = MediaPlayer.create(SoundManager.sContext, SoundManager.BGM_RESOURCEID[this.val$musicid]);
          if (SoundManager.mBgmPlayer[this.val$musicid] != null)
            SoundManager.mBgmPlayer[this.val$musicid].setLooping(true);
        }
        if (SoundManager.mBgmPlayer[this.val$musicid] != null)
          SoundManager.mBgmPlayer[this.val$musicid].start();
      }
    });
  }

  public static void startMusic(int paramInt1, int paramInt2)
  {
    startMusic(paramInt1);
  }

  public static void stopMusic(int paramInt)
  {
    if (!sEnableSe)
      return;
    mHandler.post(new Runnable()
    {
      public void run()
      {
        if ((SoundManager.mBgmPlayer[this.val$musicid] != null) && (SoundManager.mBgmPlayer[this.val$musicid].isPlaying()))
          SoundManager.mBgmPlayer[this.val$musicid].pause();
      }
    });
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.SoundManager
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */