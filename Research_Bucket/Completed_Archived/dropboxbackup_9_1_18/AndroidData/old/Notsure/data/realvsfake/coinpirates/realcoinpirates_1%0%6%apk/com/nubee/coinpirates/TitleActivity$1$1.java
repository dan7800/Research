package com.nubee.coinpirates;

import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

class TitleActivity$1$1
  implements Runnable
{
  TitleActivity$1$1(TitleActivity.1 param1)
  {
  }

  public void run()
  {
    TitleActivity localTitleActivity = TitleActivity.1.access$0(this.this$1);
    localTitleActivity.time = (1L + localTitleActivity.time);
    long l = TitleActivity.1.access$0(this.this$1).time % 10L;
    for (int i = 0; ; i++)
    {
      if (i >= 16)
        return;
      if (l == TitleActivity.ANIMATION_EXECUTE_TIMING[i])
      {
        AlphaAnimation localAlphaAnimation1 = new AlphaAnimation(0.0F, 1.2F);
        localAlphaAnimation1.setDuration(500L);
        AlphaAnimation localAlphaAnimation2 = new AlphaAnimation(1.2F, 0.0F);
        localAlphaAnimation2.setDuration(500L);
        AnimationSet localAnimationSet = new AnimationSet(true);
        localAnimationSet.addAnimation(localAlphaAnimation1);
        localAnimationSet.addAnimation(localAlphaAnimation2);
        TitleActivity.1.access$0(this.this$1).imageView[i].startAnimation(localAnimationSet);
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.TitleActivity.1.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */