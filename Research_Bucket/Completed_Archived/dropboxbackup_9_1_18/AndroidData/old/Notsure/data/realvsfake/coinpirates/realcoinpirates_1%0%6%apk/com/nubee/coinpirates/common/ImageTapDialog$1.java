package com.nubee.coinpirates.common;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

class ImageTapDialog$1
  implements DialogInterface.OnDismissListener
{
  ImageTapDialog$1(ImageTapDialog paramImageTapDialog)
  {
  }

  public void onDismiss(DialogInterface paramDialogInterface)
  {
    if (this.this$0.helper != null)
      this.this$0.helper.executeAfter(this.this$0.dialog);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.ImageTapDialog.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */