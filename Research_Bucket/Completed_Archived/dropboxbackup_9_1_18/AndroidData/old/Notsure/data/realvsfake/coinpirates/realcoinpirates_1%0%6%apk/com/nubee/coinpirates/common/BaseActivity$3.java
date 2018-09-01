package com.nubee.coinpirates.common;

import com.nubee.coinpirates.game.GameActivity;

class BaseActivity$3
  implements Runnable
{
  BaseActivity$3(BaseActivity paramBaseActivity)
  {
  }

  public void run()
  {
    if ((this.this$0.mProgressDialog == null) || (!this.this$0.mProgressDialog.isShowing()))
    {
      this.this$0.mProgressDialog = ProgressDialogEx.show(this.this$0, this.this$0.getString(2131165192), this.this$0.getString(2131165193));
      GameActivity.setPause(true);
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.BaseActivity.3
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */