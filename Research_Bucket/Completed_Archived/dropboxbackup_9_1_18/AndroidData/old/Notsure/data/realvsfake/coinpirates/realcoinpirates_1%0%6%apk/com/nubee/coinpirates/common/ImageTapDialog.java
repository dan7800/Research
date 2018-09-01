package com.nubee.coinpirates.common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

public class ImageTapDialog
{
  protected ImageView content;
  protected Dialog dialog;
  protected DialogHelper helper;

  protected ImageTapDialog(Context paramContext)
  {
    this.dialog = new Dialog(paramContext, 2131230722);
    this.dialog.setContentView(2130903040);
    this.dialog.setCancelable(false);
    this.dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
    {
      public void onDismiss(DialogInterface paramAnonymousDialogInterface)
      {
        if (ImageTapDialog.this.helper != null)
          ImageTapDialog.this.helper.executeAfter(ImageTapDialog.this.dialog);
      }
    });
    this.content = ((ImageView)this.dialog.findViewById(2131296259));
    this.content.setOnClickListener(defaultTapEventListener());
  }

  public ImageTapDialog(Context paramContext, int paramInt)
  {
    this(paramContext);
    this.content.setImageResource(paramInt);
  }

  public ImageTapDialog(Context paramContext, Bitmap paramBitmap)
  {
    this(paramContext);
    this.content.setImageBitmap(paramBitmap);
  }

  public ImageTapDialog(Context paramContext, Drawable paramDrawable)
  {
    this(paramContext);
    this.content.setImageDrawable(paramDrawable);
  }

  protected View.OnClickListener defaultTapEventListener()
  {
    return new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        ImageTapDialog.this.dialog.dismiss();
      }
    };
  }

  public void dismiss()
  {
    this.dialog.dismiss();
  }

  protected Dialog getDialog()
  {
    return this.dialog;
  }

  public void setDialogHelper(DialogHelper paramDialogHelper)
  {
    this.helper = paramDialogHelper;
  }

  public void setLongTapEventListener(View.OnLongClickListener paramOnLongClickListener)
  {
    this.content.setOnLongClickListener(paramOnLongClickListener);
  }

  public void setTapEventListener(View.OnClickListener paramOnClickListener)
  {
    this.content.setOnClickListener(paramOnClickListener);
  }

  public void show()
  {
    if (this.helper != null)
      this.helper.executeBefore(this.dialog);
    this.dialog.show();
  }

  public static abstract interface DialogHelper
  {
    public abstract void executeAfter(Dialog paramDialog);

    public abstract void executeBefore(Dialog paramDialog);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.ImageTapDialog
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */