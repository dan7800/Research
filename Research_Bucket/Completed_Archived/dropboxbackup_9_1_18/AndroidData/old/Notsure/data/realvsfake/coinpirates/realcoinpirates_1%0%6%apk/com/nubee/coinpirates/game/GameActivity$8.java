package com.nubee.coinpirates.game;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

class GameActivity$8
  implements Runnable
{
  GameActivity$8(GameActivity paramGameActivity, int paramInt)
  {
  }

  public void run()
  {
    GameActivity localGameActivity = this.this$0;
    Object[] arrayOfObject = new Object[1];
    arrayOfObject[0] = Integer.valueOf(this.val$addMedalCount);
    String str = localGameActivity.getString(2131165273, arrayOfObject);
    new AlertDialog.Builder(this.this$0).setCancelable(false).setMessage(str).setPositiveButton(2131165186, null).create().show();
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.8
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */