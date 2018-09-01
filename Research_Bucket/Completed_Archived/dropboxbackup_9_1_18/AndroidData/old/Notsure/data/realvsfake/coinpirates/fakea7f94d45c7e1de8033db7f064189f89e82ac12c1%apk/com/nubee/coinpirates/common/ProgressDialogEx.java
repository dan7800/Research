package com.nubee.coinpirates.common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class ProgressDialogEx extends Dialog
{
  protected ProgressDialogEx(Context paramContext)
  {
    super(paramContext);
  }

  protected ProgressDialogEx(Context paramContext, int paramInt)
  {
    super(paramContext, paramInt);
  }

  protected ProgressDialogEx(Context paramContext, boolean paramBoolean, DialogInterface.OnCancelListener paramOnCancelListener)
  {
    super(paramContext, paramBoolean, paramOnCancelListener);
  }

  public static ProgressDialogEx show(Context paramContext, CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
    try
    {
      View localView = ((LayoutInflater)paramContext.getSystemService("layout_inflater")).inflate(2130903044, null);
      Bitmap localBitmap = BitmapFactory.decodeResource(paramContext.getResources(), 2130837512);
      ImageView localImageView = (ImageView)localView.findViewById(2131296304);
      RotateAnimation localRotateAnimation = new RotateAnimation(0.0F, 360.0F, localBitmap.getWidth() / 2, localBitmap.getHeight() / 2);
      localRotateAnimation.setDuration(1000L);
      localRotateAnimation.setInterpolator(new LinearInterpolator());
      localRotateAnimation.setRepeatCount(-1);
      localImageView.startAnimation(localRotateAnimation);
      ProgressDialogEx localProgressDialogEx = new ProgressDialogEx(paramContext, 2131230721);
      localProgressDialogEx.requestWindowFeature(1);
      localProgressDialogEx.setCancelable(false);
      localProgressDialogEx.setContentView(localView);
      localProgressDialogEx.show();
      WindowManager.LayoutParams localLayoutParams = localProgressDialogEx.getWindow().getAttributes();
      localLayoutParams.dimAmount = 0.1F;
      localProgressDialogEx.getWindow().setAttributes(localLayoutParams);
      return localProgressDialogEx;
    }
    catch (WindowManager.BadTokenException localBadTokenException)
    {
    }
    return null;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.ProgressDialogEx
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */