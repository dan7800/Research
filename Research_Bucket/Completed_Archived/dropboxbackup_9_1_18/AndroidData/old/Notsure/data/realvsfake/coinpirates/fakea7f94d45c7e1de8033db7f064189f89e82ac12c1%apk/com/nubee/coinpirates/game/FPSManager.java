package com.nubee.coinpirates.game;

import com.nubee.coinpirates.common.Coins7Log;
import java.util.concurrent.TimeUnit;

public class FPSManager
{
  private static long ONE_MILLI_TO_NANO = TimeUnit.MILLISECONDS.toNanos(1L);
  private static long ONE_SEC_TO_NANO = TimeUnit.SECONDS.toNanos(1L);
  private long elapsedTime;
  private int[] fpsBuffer;
  private int fpsCnt;
  private int maxFps;
  private long oneCycle;
  private boolean oneFrameInterval;
  private long sleepTime;
  private long startTime;

  public FPSManager(int paramInt)
  {
    this.maxFps = paramInt;
    this.fpsBuffer = new int[this.maxFps];
    this.fpsCnt = 0;
    this.startTime = System.nanoTime();
    this.oneCycle = ()Math.floor(ONE_SEC_TO_NANO / this.maxFps);
    this.oneFrameInterval = false;
  }

  public int getFps()
  {
    int i = 0;
    for (int j = 0; ; j++)
    {
      if (j >= this.maxFps)
        return i / this.maxFps;
      i += this.fpsBuffer[j];
    }
  }

  public boolean isOneframeIntervalMode()
  {
    return this.oneFrameInterval;
  }

  public void setOneframeIntervalMode(boolean paramBoolean)
  {
    this.oneFrameInterval = paramBoolean;
  }

  public long state()
  {
    this.fpsCnt = (1 + this.fpsCnt);
    if (this.maxFps <= this.fpsCnt)
      this.fpsCnt = 0;
    this.elapsedTime = (System.nanoTime() - this.startTime);
    this.sleepTime = (this.oneCycle - this.elapsedTime);
    if (this.sleepTime < ONE_MILLI_TO_NANO)
      this.sleepTime = ONE_MILLI_TO_NANO;
    int i = (int)(ONE_SEC_TO_NANO / (this.elapsedTime + this.sleepTime));
    this.fpsBuffer[this.fpsCnt] = i;
    this.startTime = (System.nanoTime() + this.sleepTime);
    return this.sleepTime;
  }

  public long stateWithSleep(Thread paramThread)
  {
    if (paramThread.getPriority() < 10)
      paramThread.setPriority(10);
    l1 = state();
    if (this.oneFrameInterval)
    {
      l2 = ONE_MILLI_TO_NANO;
      while (true)
      {
        if (l1 > l2);
        try
        {
          TimeUnit.NANOSECONDS.sleep(l1);
          if (Coins7Log.isLoggable("FPS", 3))
            Coins7Log.d("FPS", "FPS=" + getFps() + "/" + this.maxFps + "," + l1);
          return l1;
          l2 = 0L;
        }
        catch (InterruptedException localInterruptedException)
        {
          while (true)
            Coins7Log.e("FPS", "error", localInterruptedException);
        }
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.FPSManager
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */