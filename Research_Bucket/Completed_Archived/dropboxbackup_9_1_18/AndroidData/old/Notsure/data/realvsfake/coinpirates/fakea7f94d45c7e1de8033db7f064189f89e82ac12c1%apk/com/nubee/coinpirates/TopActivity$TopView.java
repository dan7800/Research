package com.nubee.coinpirates;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.View;
import com.nubee.coinpirates.animation.Animator;
import com.nubee.coinpirates.animation.Animator.AnimatorCallback;
import com.nubee.coinpirates.animation.Image;
import com.nubee.coinpirates.animation.Part;

class TopActivity$TopView extends View
  implements Animator.AnimatorCallback
{
  Part mLogoAnimation;
  PointF mLogoPosition = new PointF();

  public TopActivity$TopView(TopActivity paramTopActivity, Context paramContext)
  {
    super(paramContext);
  }

  public void onAnimationFinished()
  {
    Intent localIntent = new Intent(this.this$0, TitleActivity.class);
    this.this$0.startActivity(localIntent);
    this.this$0.finish();
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

/* Location:
 * Qualified Name:     com.nubee.coinpirates.TopActivity.TopView
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */