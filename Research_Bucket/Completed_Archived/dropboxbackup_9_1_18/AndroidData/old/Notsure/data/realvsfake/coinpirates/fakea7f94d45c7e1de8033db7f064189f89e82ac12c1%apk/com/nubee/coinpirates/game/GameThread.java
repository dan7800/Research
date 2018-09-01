package com.nubee.coinpirates.game;

import android.util.Log;
import java.util.concurrent.TimeUnit;

public class GameThread extends Thread
{
  boolean mLoop;
  float mMeasuredFps;
  Thread mRunning;

  public GameThread()
  {
  }

  public static native void step();

  public void finish()
  {
    Log.d("GameThread", "finish");
    this.mLoop = false;
  }

  public void run()
  {
    Thread localThread = Thread.currentThread();
    this.mRunning = localThread;
    long l1 = System.nanoTime();
    long l2 = l1;
    long l3 = ()Math.floor(TimeUnit.SECONDS.toNanos(2L) / 60.0D);
    long l4 = 0L;
    this.mLoop = true;
    while (true)
    {
      if ((localThread != this.mRunning) || (!this.mLoop))
        return;
      long l5 = System.nanoTime();
      long l6 = l5 - l1;
      if (l6 > l3)
      {
        l1 = l5;
        l4 += 1L;
        long l8 = l5 - l2;
        if (l8 >= TimeUnit.SECONDS.toNanos(1L))
        {
          this.mMeasuredFps = (float)(TimeUnit.SECONDS.toNanos(l4) / l8);
          l2 = l1;
          l4 = 0L;
        }
        step();
      }
      else
      {
        long l7 = l3 - l6;
        try
        {
          TimeUnit.NANOSECONDS.sleep(l7);
        }
        catch (InterruptedException localInterruptedException)
        {
          localInterruptedException.printStackTrace();
        }
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameThread
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */