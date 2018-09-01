package com.nubee.coinpirates.common;

import com.nubee.coinpirates.game.GameActivity;

class BaseActivity$4
  implements Runnable
{
  BaseActivity$4(BaseActivity paramBaseActivity)
  {
  }

  public void run()
  {
    if (this.this$0.mProgressDialog != null)
    {
      this.this$0.mProgressDialog.dismiss();
      GameActivity.setPause(false);
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.BaseActivity.4
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */