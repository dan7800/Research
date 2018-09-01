package com.nubee.coinpirates;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.nubee.coinpirates.common.BaseActivity;
import com.nubee.coinpirates.common.Coins7Log;
import com.nubee.coinpirates.game.GameActivity;
import java.util.Timer;
import java.util.TimerTask;

public class TitleActivity extends BaseActivity
  implements View.OnClickListener
{
  static final int[] ANIMATION_EXECUTE_TIMING = arrayOfInt;
  static final float[][] INITIAL_POSITION_KIRA = { { 25.0F, 90.0F }, { 50.0F, 190.0F }, { 80.0F, 40.0F }, { 90.0F, 160.0F }, { 250.0F, 180.0F }, { 290.0F, 200.0F }, { 290.0F, 120.0F }, { 30.0F, 270.0F }, { 80.0F, 220.0F }, { 100.0F, 300.0F }, { 150.0F, 250.0F }, { 180.0F, 220.0F }, { 250.0F, 250.0F }, { 290.0F, 300.0F }, { 25.0F, 350.0F }, { 300.0F, 370.0F } };
  static final int KIRA_IMAGE_COUNT = 16;
  private final int WC = -2;
  ImageView[] imageView = new ImageView[16];
  Timer mTimer = null;
  long time = 0L;

  static
  {
    int[] arrayOfInt = new int[16];
    arrayOfInt[0] = 3;
    arrayOfInt[1] = 9;
    arrayOfInt[2] = 1;
    arrayOfInt[3] = 2;
    arrayOfInt[4] = 5;
    arrayOfInt[5] = 6;
    arrayOfInt[6] = 8;
    arrayOfInt[7] = 2;
    arrayOfInt[8] = 4;
    arrayOfInt[9] = 7;
    arrayOfInt[11] = 5;
    arrayOfInt[12] = 9;
    arrayOfInt[13] = 4;
    arrayOfInt[15] = 7;
  }

  public TitleActivity()
  {
  }

  protected void backActivity()
  {
    confirmFinish(2131165188, 2131165189);
  }

  public void onClick(View paramView)
  {
    switch (paramView.getId())
    {
    default:
      return;
    case 2131296311:
    }
    startActivity(new Intent(this, GameActivity.class));
    finish();
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle, getClass().getSimpleName());
    setContentView(2130903047);
    ((Button)findViewById(2131296311)).setOnClickListener(this);
    Bitmap localBitmap = BitmapFactory.decodeResource(getResources(), 2130837532);
    RelativeLayout localRelativeLayout = (RelativeLayout)findViewById(2131296310);
    for (int i = 0; ; i++)
    {
      if (i >= 16)
        return;
      RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
      localLayoutParams.setMargins((int)INITIAL_POSITION_KIRA[i][0], (int)INITIAL_POSITION_KIRA[i][1], 0, 0);
      this.imageView[i] = new ImageView(this);
      this.imageView[i].setImageBitmap(localBitmap);
      localRelativeLayout.addView(this.imageView[i], localLayoutParams);
    }
  }

  protected void onDestroy()
  {
    super.onDestroy();
  }

  protected void onPause()
  {
    if (this.mTimer != null)
    {
      Coins7Log.d("Timer", "timer stop.");
      this.mTimer.cancel();
      this.mTimer = null;
      Coins7Log.d("Timer", "timer stoped.");
    }
    super.onPause();
  }

  protected void onResume()
  {
    super.onResume();
    if (this.mTimer == null)
    {
      Coins7Log.d("Timer", "timer start.");
      this.mTimer = new Timer(true);
      this.mTimer.schedule(new TimerTask()
      {
        public void run()
        {
          TitleActivity.this.mHandler.post(new Runnable()
          {
            public void run()
            {
              TitleActivity localTitleActivity = TitleActivity.this;
              localTitleActivity.time = (1L + localTitleActivity.time);
              long l = TitleActivity.this.time % 10L;
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
                  TitleActivity.this.imageView[i].startAnimation(localAnimationSet);
                }
              }
            }
          });
        }
      }
      , 100L, 100L);
      Coins7Log.d("Timer", "timer started.");
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.TitleActivity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */