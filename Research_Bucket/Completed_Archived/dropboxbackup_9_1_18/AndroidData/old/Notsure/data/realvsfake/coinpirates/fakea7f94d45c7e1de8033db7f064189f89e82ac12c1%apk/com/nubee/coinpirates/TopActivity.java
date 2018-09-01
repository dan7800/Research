package com.nubee.coinpirates;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.nubee.coinpirates.animation.Animator;
import com.nubee.coinpirates.animation.Animator.AnimatorCallback;
import com.nubee.coinpirates.animation.Image;
import com.nubee.coinpirates.animation.Part;
import com.nubee.coinpirates.common.BaseActivity;
import com.nubee.coinpirates.common.Coins7Log;

public class TopActivity extends BaseActivity
{
  TopView mView;

  public TopActivity()
  {
  }

  protected void backActivity()
  {
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle, getClass().getSimpleName());
    setContentView(2130903048);
    ViewGroup localViewGroup = (ViewGroup)findViewById(2131296276);
    TopView localTopView = new TopView(this);
    this.mView = localTopView;
    localViewGroup.addView(localTopView);
    String str = getSharedPreferences("KEY_PREF_LAST_TIME", 0).getString("KEY_GAME_SAVE_DATA", "sav_file");
    String[] arrayOfString = fileList();
    int i = 0;
    if (i >= arrayOfString.length)
      return;
    if (arrayOfString[i].equals(str))
      Coins7Log.e("list", String.valueOf(arrayOfString[i]) + " is current file.");
    while (true)
    {
      i++;
      break;
      Coins7Log.e("list", String.valueOf(arrayOfString[i]) + " is old file.");
      deleteFile(arrayOfString[i]);
    }
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    if ((this.mView != null) && (this.mView.mLogoAnimation != null));
    for (int i = 0; ; i++)
    {
      if (i >= 5)
        return super.onTouchEvent(paramMotionEvent);
      this.mView.mLogoAnimation.animation();
    }
  }

  class TopView extends View
    implements Animator.AnimatorCallback
  {
    Part mLogoAnimation;
    PointF mLogoPosition = new PointF();

    public TopView(Context arg2)
    {
      super();
    }

    public void onAnimationFinished()
    {
      Intent localIntent = new Intent(TopActivity.this, TitleActivity.class);
      TopActivity.this.startActivity(localIntent);
      TopActivity.this.finish();
    }

    protected void onDraw(Canvas paramCanvas)
    {
      paramCanvas.save();
      paramCanvas.translate(this.mLogoPosition.x, this.mLogoPosition.y);
      paramCanvas.drawColor(-1);
      if (this.mLogoAnimation != null)
      {
        this.mLogoAnimation.animation();
        this.mLogoAnimation.draw(paramCanvas, null);
      }
      paramCanvas.restore();
      invalidate();
    }

    protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      try
      {
        this.mLogoAnimation = Part.createPart(getContext(), 2130968581);
        this.mLogoAnimation.findAnimator("finish").setOnFinishListener(this);
        Image localImage = this.mLogoAnimation.findImage("logo");
        this.mLogoPosition.x = (getWidth() - localImage.getWidth()) / 2;
        this.mLogoPosition.y = (getHeight() - localImage.getHeight()) / 2;
        return;
      }
      catch (Exception localException)
      {
        onAnimationFinished();
      }
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.TopActivity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */